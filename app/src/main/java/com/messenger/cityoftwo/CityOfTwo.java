package com.messenger.cityoftwo;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.util.Base64;
import android.util.Log;

import com.google.android.gms.gcm.GoogleCloudMessaging;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;

/**
 * Created by Aayush on 2/5/2016.
 */
public class CityOfTwo extends Application {

    public static final String KEY_ACCESS_TOKEN = "ACCESS_TOKEN";
    public static final String KEY_PROFILE = "PROFILE";
    public static final String KEY_SELECTED_ANSWER = "SELECTED";
    public static final String KEY_TEST = "TEST";
    public static final String KEY_MESSAGE = "MESSAGE";
    public static final String KEY_TYPE = "TYPE";
    public static final String KEY_TEXT = "TEXT";
    public static final String KEY_CURRENT_CHAT = "CURRENT_CHAT";
    public static final String KEY_SESSION_TOKEN = "SESSION_TOKEN";
    public static final String KEY_CURRENT_ANSWER = "current_answer";
    public static final String KEY_LOCATION_X = "location_x";
    public static final String KEY_LOCATION_Y = "location_y";
    public static final String KEY_WIDTH = "width";
    public static final String KEY_HEIGHT = "height";

    public static final String KEY_TEST_RESULT = "test_result";
    public static final int APPLICATION_FOREGROUND = 0,

            APPLICATION_BACKGROUND = 1;
    public static final int ACTIVITY_LOBBY = 1;
    public static final int ACTIVITY_TEST = 2;
    public static final int ACTIVITY_CONVERSATION = 3;

    public static final int ACTIVITY_INTRODUCTION = 4;
    public static final int SENT = 1,
            RECEIVED = 2,
            START = 0,

    END = -1;
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
    public static Integer APPLICATION_STATE;

    public static ArrayList<Conversation> BackgroundConversation;
    public static Bitmap logoBitmap;

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
