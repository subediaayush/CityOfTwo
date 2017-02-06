package com.messenger.cityoftwo;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.graphics.Bitmap;
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

	public static final String PACKAGE_NAME = "com.messenger.cityoftwo";

	public static final String KEY_ACCESS_TOKEN = "access_token";
	public static final String KEY_PROFILE_IMAGE = "image_uri";
	public static final String KEY_PROFILE_NAME = "user_name";
	public static final String KEY_PROFILE = "profile";
	public static final String KEY_SELECTED_ANSWER = "selected";
	public static final String KEY_TEST = "test";
	public static final String KEY_MESSAGE = "message";
	public static final String KEY_CHAT_BEGIN = "chat_begin";
	public static final String KEY_TYPE = "type";
	public static final String KEY_MESSAGE_DATA = "data";
	public static final String KEY_CURRENT_CHAT = "current_chat";
	public static final String KEY_CURRENT_CHAT_ID = "current_chat_id";
	public static final String KEY_SESSION_TOKEN = "session_token";
	public static final String KEY_CURRENT_ANSWER = "current_answer";
	public static final String KEY_LOCATION_X = "location_x";
	public static final String KEY_LOCATION_Y = "location_y";
	public static final String KEY_WIDTH = "width";
	public static final String KEY_HEIGHT = "height";
	public static final String KEY_COMMON_LIKES = "likes";
	public static final String KEY_TEST_RESULT = "test_result";
	public static final String KEY_LAST_CHATROOM = "chatroom_id";

	public static final int APPLICATION_FOREGROUND = 0;
	public static final int APPLICATION_BACKGROUND = 1;

	public static final String[] ACTIVITY = new String[]{
			"ACTIVITY_LOGIN",
			"ACTIVITY_LOBBY",
			"ACTIVITY_TEST",
			"ACTIVITY_PROFILE",
			"ACTIVITY_INTRODUCTION",
			"ACTIVITY_HOME",
			"ACTIVITY_FILTER",
			"ACTIVITY_SPLASH"
	};

	public static final int ACTIVITY_LOGIN = 0;
	public static final int ACTIVITY_LOBBY = 1;
	public static final int ACTIVITY_TEST = 2;
	public static final int ACTIVITY_PROFILE = 3;
	public static final int ACTIVITY_INTRODUCTION = 4;
	public static final int ACTIVITY_HOME = 5;
	public static final int ACTIVITY_FILTER = 6;
	public static final int ACTIVITY_SPLASH = 7;

	public static final long MILLIS_IN_DAY = 86400000;
	public static final long DAYS_IN_WEEK = MILLIS_IN_DAY * 7;
	public static final long MONTHS_IN_YEAR = MILLIS_IN_DAY * 365;


	public static final int FLAG_SENT =
			0b1;
	public static final int FLAG_RECEIVED =
			0b10;
	public static final int FLAG_TEXT =
			0b100;
	public static final int FLAG_PROFILE =
			0b1000;
	public static final int FLAG_REQUEST =
			0b10000;
	public static final int FLAG_START =
			0b100000;
	public static final int FLAG_END =
			0b1000000;
	public static final int FLAG_INDICATOR =
			0b10000000;
	public static final int FLAG_TYPING =
			0b100000000;
	public static final int FLAG_LAST_SEEN =
			0b1000000000;

	public static final int RESULT_EXIT_APP = 10010;

	public static final Integer MAXIMUM_AGE = 100;
	public static final Integer MAXIMUM_DISTANCE = 100;
	public static final Integer MINIMUM_AGE = 18;
	public static final Integer MINIMUM_DISTANCE = 0;

	public static final String ACTION_BEGIN_CHAT = PACKAGE_NAME + ".BEGIN_CHAT";
	public static final String ACTION_END_CHAT = PACKAGE_NAME + ".END_CHAT";
	public static final String ACTION_FCM_ID = PACKAGE_NAME + ".FCM_ID";
	public static final String ACTION_IS_TYPING = PACKAGE_NAME + ".IS_TYPING";
	public static final String ACTION_LAST_SEEN = PACKAGE_NAME + ".LAST_SEEN";
	public static final String ACTION_NEW_MESSAGE = PACKAGE_NAME + ".NEW_MESSAGE";
	public static final String ACTION_REQUEST_CHAT = PACKAGE_NAME + ".REQUEST_CHAT";
	public static final String ACTION_USER_OFFLINE = PACKAGE_NAME + ".USER_OFFLINE";
	public static final String ACTION_REQUEST_DENIED = PACKAGE_NAME + ".REQUEST_DENIED";
	public static final String ACTION_REQUEST_ALARM = PACKAGE_NAME + ".REQUEST_ALARM";
	public static final String ACTION_REQUEST_TIMEOUT = PACKAGE_NAME + ".REQUEST_TIMEOUT";

	public static final String KEY_CHAT_REQUEST = "chat_request";
	public static final String KEY_REQUEST_ID = "request_id";

	public static final String HOST = "coyrudy.com";
	//	public static final String HOST = "192.168.0.100:5000";
	public static final String API = "api";

	public static final String API_KEY = "AIzaSyB_Wco0Sdad38QGHsCcode9P1iZ3tsqqXY";

	public static final String HEADER_ACCESS_TOKEN = "access_token";
	public static final String HEADER_CHATROOM_ID = "chatroom_id";
	public static final String HEADER_DATA = "data";
	public static final String HEADER_FLAGS = "flags";
	public static final String HEADER_GCM_ID = "gcm_id";
	public static final String HEADER_IS_TYPING = "is_typing";
	public static final String HEADER_LAST_SEEN = "last_seen";
	public static final String HEADER_TEST = "test";
	public static final String HEADER_TEST_RESULT = "questions";
	public static final String HEADER_TIME = "time";
	public static final String HEADER_TO = "to";
	public static final String HEADER_TOKEN = "token";

	public static final String KET_NAME = "name";
	public static final String KEY_BACKGROUND_MESSAGES = "background_messages";
	public static final String KEY_CATEGORY = "category";
	public static final String KEY_CHAT_END = "chat_end";
	public static final String KEY_CHAT_HEADER = "chat_header";
	public static final String KEY_CHATROOM_ID = "chatroom_id";
	public static final String KEY_CODE = "code";
	public static final String KEY_CREDITS = "credits";
	public static final String KEY_CURRENT_GUEST = "current_guest";
	public static final String KEY_DEVICE_REGISTERED = "device_registerd";
	public static final String KEY_DISTANCE = "distance";
	public static final String KEY_DISTANCE_IN_MILES = "distance_miles";
	public static final String KEY_FBID = "fbid";
	public static final String KEY_FILTERS = "filters";
	public static final String KEY_FILTERS_APPLIED = "filters_applied";
	public static final String KEY_FIRST_RUN = "first_run";
	public static final String KEY_FRIEND_ID = "friend_id";
	public static final String KEY_FROM_INTRO = "from_intro";
	public static final String KEY_GUEST = "guest";
	public static final String KEY_ICON = "image";
	public static final String KEY_ID = "id";
	public static final String KEY_IS_AVAILABLE = "is_available";
	public static final String KEY_IS_FRIEND = "is_friend";
	public static final String KEY_IS_ONLINE = "is_online";
	public static final String KEY_IS_TYPING = "is_typing";
	public static final String KEY_LAST_GUEST = "guest_id";
	public static final String KEY_LAST_REQUEST = "last_request";
	public static final String KEY_LAST_SEEN = "last_seen";
	public static final String KEY_MATCH_FEMALE = "gender_female";
	public static final String KEY_MATCH_MALE = "gender_male";
	public static final String KEY_MAX_AGE = "max_age";
	public static final String KEY_MESSAGE_FLAGS = "flags";
	public static final String KEY_MESSAGE_TIME = "time";
	public static final String KEY_MIN_AGE = "min_age";
	public static final String KEY_NAME = "name";
	public static final String KEY_PROFILE_ID = "profile_id";
	public static final String KEY_PROFILE_MODE = "profile_mode";
	public static final String KEY_PROFILE_URI = "profile_uri";
	public static final String KEY_REG_ID = "reg_id";
	public static final String KEY_REQUESTS = "contacts";
	public static final String KEY_RESPONSE = "response";
	public static final String KEY_SESSION_ACTIVE = "session_active";
	public static final String KEY_SHOW_REVEAL_DIALOG = "show_reveal_dialog";
	public static final String KEY_TOKEN = "token";
	public static final String KEY_USER_OFFLINE = "user_offline";

	public static final String SECURED_PREFERENCE = PACKAGE_NAME + ".secured";
	public static final String KEY_RESQUEST_DISPATCH = "request_dispatch";
	private static final String SENDER_ID = "584281533020";
	public static ArrayList<Conversation> mBackgroundConversation;
	public static Bitmap logoBitmap;
	public static Bitmap logoSmallBitmap;
	public static HashMap<String, Bitmap> answerBitmapList;
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

		NetworkStateReceiver.checkConnectivity(this);
	}

	private void printHashKey() {
		try {
			PackageInfo info = getPackageManager().getPackageInfo(
					PACKAGE_NAME,
					PackageManager.GET_SIGNATURES
			);
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
