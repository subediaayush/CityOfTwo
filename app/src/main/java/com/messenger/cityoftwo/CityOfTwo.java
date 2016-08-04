package com.messenger.cityoftwo;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.graphics.Bitmap;
import android.net.Uri;
import android.util.Base64;
import android.util.Log;
import android.util.TypedValue;

import com.facebook.FacebookSdk;
import com.facebook.appevents.AppEventsLogger;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

/**
 * Created by Aayush on 2/5/2016.
 */
public class CityOfTwo extends Application {
    public static final List<String> FACEBOOK_PERMISSION_LIST = Arrays.asList(
            "user_likes"
    );

    public static final String KEY_ACCESS_TOKEN = "ACCESS_TOKEN";
    public static final String KEY_PROFILE_IMAGE = "image_uri";
    public static final String KEY_PROFILE_NAME = "user_name";
    public static final String KEY_PROFILE = "PROFILE";
    public static final String KEY_SELECTED_ANSWER = "SELECTED";
    public static final String KEY_TEST = "TEST";
    public static final String KEY_MESSAGE = "MESSAGE";
    public static final String KEY_CHAT_BEGIN = "CHAT_BEGIN";
    public static final String KEY_TYPE = "TYPE";
    public static final String KEY_TEXT = "TEXT";
    public static final String KEY_CURRENT_CHAT = "CURRENT_CHAT";
    public static final String KEY_CURRENT_CHAT_ID = "CURRENT_CHAT_ID";
    public static final String KEY_SESSION_TOKEN = "SESSION_TOKEN";
    public static final String KEY_CURRENT_ANSWER = "CURRENT_ANSWER";
    public static final String KEY_LOCATION_X = "LOCATION_X";
    public static final String KEY_LOCATION_Y = "LOCATION_Y";
    public static final String KEY_WIDTH = "WIDTH";
    public static final String KEY_HEIGHT = "HEIGHT";
    public static final String KEY_COMMON_LIKES = "LIKES";
    public static final String KEY_TEST_RESULT = "TEST_RESULT";
    public static final String KEY_CHATROOM_ID = "CHATROOM_ID";

    public static final int APPLICATION_FOREGROUND = 0;
    public static final int APPLICATION_BACKGROUND = 1;

    public static final int ACTIVITY_LOBBY = 1;
    public static final int ACTIVITY_TEST = 2;
    public static final int ACTIVITY_CONVERSATION = 3;
    public static final int ACTIVITY_INTRODUCTION = 4;

    public static final int FLAG_SENT = 0b1;
    public static final int FLAG_RECEIVED = 0b10;
    public static final int FLAG_TEXT = 0b100;
    public static final int FLAG_PROFILE = 0b1000;
    public static final int FLAG_AD = 0b10000;
    public static final int FLAG_START = 0b100000;
    public static final int FLAG_END = 0b1000000;

    public static final String PACKAGE_NAME = "com.messenger.cityoftwo";
    public static final String HOST = "coyrudy.com";
    public static final String API = "api";

    public static final String HEADER_ACCESS_TOKEN = "access_token";
    public static final String HEADER_TEST = "test";
    public static final String HEADER_TEST_RESULT = "questions";
    public static final String HEADER_SEND_MESSAGE = "message";
    public static final String HEADER_GCM_ID = "gcm_id";

    public static final String API_KEY = "AIzaSyB_Wco0Sdad38QGHsCcode9P1iZ3tsqqXY";
    public static final String HEADER_CHATROOM_ID = "chatroom_id";
    public static final String KEY_MIN_AGE = "min_age";
    public static final String KEY_MAX_AGE = "max_age";
    public static final String KEY_DISTANCE = "distance";
    public static final String KEY_MATCH_MALE = "gender_male";
    public static final String KEY_MATCH_FEMALE = "gender_female";
    public static final String KEY_DISTANCE_IN_MILES = "distance_miles";
    public static final Integer MINIMUM_AGE = 18;
    public static final Integer MAXIMUM_AGE = 100;
    public static final Integer MINIMUM_DISTANCE = 0;
    public static final Integer MAXIMUM_DISTANCE = 100;
    public static final String KEY_PROFILE_ID = "profile_id";
    public static final String KEY_PROFILE_URI = "profile_uri";
    public static final String KEY_MESSAGE_FLAGS = "FLAGS";
    public static final String KEY_TIME = "TIME";
    public static final String HEADER_FLAGS = "flags";
    public static final String HEADER_TIME = "time";
    public static final String KEY_TOKEN = "token";
    public static final String KEY_CODE = "code";
    public static final String KEY_CREDITS = "credits";
    public static final String KEY_FILTERS_APPLIED = "filters_applied";
    public static final String KEY_FILTERS = "filters";
    public static final String KEY_CHAT_PENDING = "chat_pending";
    public static final String KEY_REG_ID = "reg_id";
    public static final Integer ACTIVITY_LOGIN = 0;
    public static final String KEY_CHAT_END = "CHAT_END";
    public static final String KEY_FROM_INTRO = "from_intro";
    public static final String TABLE_MESSAGES = "message";
    public static final String COLUMN_ID = "id";
    public static final String COLUMN_CHATROOM_ID = "chatroom_id";
    public static final String COLUMN_MESSAGE = "text";
    public static final String COLUMN_FLAGS = "flags";
    public static final String COLUMN_TIME = "time";
    public static final String ACTION_BEGIN_CHAT = "begin_chat";
    public static final String ACTION_END_CHAT = "end_chat";
    public static final String ACTION_NEW_MESSAGE = "new_message";
    public static final String NOTIFICATION_NEW_MESSAGE = "notification_new_message";
    public static final int RESULT_EXIT_APP = 10010;
    public static final String KEY_SHOW_REVEAL_DIALOG = "show_reveal_dialog";
    public static final String SECURED_PREFERENCE = "com.messenger.cityoftwo.secured";
    public static final String ACTION_FCM_ID = "fcm_id";
    public static final String KEY_CHAT_HEADER = "chat_header";
    public static final String KEY_USER_OFFLINE = "USER_OFFLINE";
    public static final String ACTION_USER_OFFLINE = "user_offline";
    private static final String SENDER_ID = "584281533020";
    public static ArrayList<Conversation> mBackgroundConversation;
    public static Bitmap logoBitmap;
    public static Bitmap logoSmallBitmap;
    public static HashMap<String, Bitmap> answerBitmapList;
    public static Integer messageCounter;
    public static List<String> pendingMessages;
    private static Integer currentActivity;
    private static Integer applicationState;

    /**
     * @param value
     * @return Array with first element value
     */
    public static String[] StringToArray(String value) {
        String[] tempArray = new String[1];
        tempArray[0] = value;
        return tempArray;
    }

    public static Integer getApplicationState() {
        return applicationState;
    }

    public static void setApplicationState(int applicationState) {
        CityOfTwo.applicationState = applicationState;
    }

    public static Integer getCurrentActivity() {
        return currentActivity;
    }

    public static void setCurrentActivity(Integer currentActivity) {
        CityOfTwo.currentActivity = currentActivity;
    }

//    public static boolean GCMRegistered(final Context context) {
//        String Version = "";
//
//        try {
//            PackageInfo pInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
//            Version = pInfo.versionName;
//        } catch (PackageManager.NameNotFoundException e) {
//            e.printStackTrace();
//        }
//
//        SharedPreferences sp = context.getSharedPreferences(CityOfTwo.PACKAGE_NAME, MODE_PRIVATE);
//        if (!Version.isEmpty()) {
//            String PREV_VERSION = sp.getString("PREV_VERSION", "EMPTY");
//            if (!PREV_VERSION.equals(Version)) {
//                sp.edit()
//                        .putString("PREV_VERSION", Version)
//                        .apply();
//                return false;
//            }
//        }
//
//        String REG_ID = sp.getString("REG_ID", "EMPTY");
//
//        return !REG_ID.equals("EMPTY");
//    }
//
//    public static void RegisterGCM(final Context context) {
//        //TODO Remove this if
//        new AsyncTask<Void, Void, String>() {
//            @Override
//            protected String doInBackground(Void... params) {
//                String msg = "";
//                String regid;
//                try {
//                    if (GCM == null) {
//                        GCM = GoogleCloudMessaging.getInstance(context.getApplicationContext());
//                    }
//
//                    regid = GCM.register(SENDER_ID);
//
//                    context.getSharedPreferences(CityOfTwo.PACKAGE_NAME, Context.MODE_PRIVATE)
//                            .edit()
//                            .putString("REG_ID", regid)
//                            .apply();
//
//                    msg = "Device registered, registration ID=" + regid;
//                    Log.i("GCM", msg + " " + regid);
//
//                } catch (IOException ex) {
//                    msg = "Error :" + ex.getMessage();
//                }
//                return msg;
//            }
//
//        }.execute();
//    }

    public static float dpToPixel(Context context, int dp) {
        return TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP, dp,
                context.getResources().getDisplayMetrics()
        );
    }

    public static Uri getFacebookPageURI(Context context, String profileId) {

        String FACEBOOK_URL = "https://www.facebook.com/";

        PackageManager packageManager = context.getPackageManager();
        try {
            int versionCode = packageManager.getPackageInfo("com.facebook.katana", 0).versionCode;
            if (versionCode >= 3002850) { //newer versions of fb app
                return Uri.parse("fb://facewebmodal/f?href=" + FACEBOOK_URL + profileId);
            } else { //older versions of fb app
                return Uri.parse("fb://page/" + profileId);
            }
        } catch (PackageManager.NameNotFoundException e) {
            return Uri.parse(FACEBOOK_URL + profileId); //normal web url
        }
    }

    public static boolean isGooglePlayServicesAvailable(Activity activity, DialogInterface.OnCancelListener cancelListener) {
        GoogleApiAvailability googleApiAvailability = GoogleApiAvailability.getInstance();
        int status = googleApiAvailability.isGooglePlayServicesAvailable(activity);
        if (status != ConnectionResult.SUCCESS) {
            if (googleApiAvailability.isUserResolvableError(status)) {
                googleApiAvailability.getErrorDialog(activity, status, 2404, cancelListener).show();
            }
            return false;
        }
        return true;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        printHashKey();
        FacebookSdk.sdkInitialize(getApplicationContext());
        AppEventsLogger.activateApp(this);
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
        } catch (PackageManager.NameNotFoundException | NoSuchAlgorithmException ignored) {

        }
    }
}
