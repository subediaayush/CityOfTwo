package com.messenger.cityoftwo;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.os.AsyncTask;
import android.util.Base64;
import android.util.Log;

import com.google.android.gms.gcm.GoogleCloudMessaging;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Created by Aayush on 2/5/2016.
 */
public class CityOfTwo extends Application {

    public static final String KEY_ACCESS_TOKEN = "ACCESS_TOKEN",
            KEY_PROFILE = "PROFILE",
            KEY_SELECTED_ANSWER = "SELECTED",
            KEY_TEST = "TEST",
            KEY_MESSAGE = "MESSAGE",
            KEY_TYPE = "TYPE",
            KEY_TEXT = "TEXT",
            KEY_SESSION_TOKEN = "SESSION_TOKEN";

    public static final int ACTIVITY_LOBBY = 1,
            ACTIVITY_TEST = 2,
            ACTIVITY_CONVERSATION = 3;

    public static final String PACKAGE_NAME = "com.messenger.cityoftwo",
            HOST = "coyrudy.com",
            API = "api";

    public static final String HEADER_ACCESS_TOKEN = "access_token",
            HEADER_TEST = "test",
            HEADER_TEST_RESULT = "questions",
            HEADER_SEND_MESSAGE = "message",
            HEADER_GCM_ID = "gcm_id";

    public static final String API_KEY = "AIzaSyB_Wco0Sdad38QGHsCcode9P1iZ3tsqqXY";
    private static final String SENDER_ID = "584281533020";
    public static GoogleCloudMessaging GCM;

    /**
     * @param value
     * @return Array with first element value
     */
    public static String[] StringToArray(String value) {
        String[] tempArray = new String[1];
        tempArray[0] = value;
        return tempArray;
    }

    public static boolean GCMRegistered(final Context context) {
        String Version = "";

        try {
            PackageInfo pInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            Version = pInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        SharedPreferences sp = context.getSharedPreferences(CityOfTwo.PACKAGE_NAME, MODE_PRIVATE);
        if (!Version.isEmpty()) {
            String PREV_VERSION = sp.getString("PREV_VERSION", "EMPTY");
            if (!PREV_VERSION.equals(Version)) {
                sp.edit()
                        .putString("PREV_VERSION", Version)
                        .apply();
                return false;
            }
        }

        String REG_ID = sp.getString("REG_ID", "EMPTY");

        return !REG_ID.equals("EMPTY");
    }

    public static void RegisterGCM(final Context context) {
        //TODO Remove this if
        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... params) {
                String msg = "";
                String regid;
                try {
                    if (GCM == null) {
                        GCM = GoogleCloudMessaging.getInstance(context.getApplicationContext());
                    }

                    regid = GCM.register(SENDER_ID);

                    context.getSharedPreferences(CityOfTwo.PACKAGE_NAME, Context.MODE_PRIVATE)
                            .edit()
                            .putString("REG_ID", regid)
                            .apply();

                    msg = "Device registered, registration ID=" + regid;
                    Log.i("GCM", msg + " " + regid);

                } catch (IOException ex) {
                    msg = "Error :" + ex.getMessage();
                }
                return msg;
            }

        }.execute();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        printHashKey();
    }

    private void printHashKey() {
        try {
            PackageInfo info = getPackageManager().getPackageInfo(
                    PACKAGE_NAME,
                    PackageManager.GET_SIGNATURES);
            for (Signature signature : info.signatures) {
                MessageDigest md = MessageDigest.getInstance("SHA");
                md.update(signature.toByteArray());
                Log.d("ATTENTION:", "ATTENTION ATTENTION !!! !!! ^^**&^*%#");
                Log.d("KeyHash:", Base64.encodeToString(md.digest(), Base64.DEFAULT));
            }
        } catch (PackageManager.NameNotFoundException e) {

        } catch (NoSuchAlgorithmException e) {

        }
    }

}
