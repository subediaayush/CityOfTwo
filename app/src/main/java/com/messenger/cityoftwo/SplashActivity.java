package com.messenger.cityoftwo;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;

import com.google.firebase.iid.FirebaseInstanceId;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

/**
 * Created by Aayush on 10/7/2016.
 */
public class SplashActivity extends PumpedActivity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

//		if (BuildConfig.DEBUG) {
//			Intent profileIntent = new Intent(this, ProfileActivity.class);
//
//			startActivity(profileIntent);
//			finish();
//			return;
//		}
//		Log.i(TAG, AccessToken.getCurrentAccessToken().getToken());
//
		setContentView(R.layout.activity_splash);

		checkNetworkState();


//		final Intent intent = new Intent(this, HomeActivity.class);
//		new Handler().postDelayed(new Runnable() {
//			@Override
//			public void run() {
//				findViewById(R.id.splash_description).setVisibility(View.GONE);
//				new Handler().postDelayed(new Runnable() {
//					@Override
//					public void run() {
//						startActivity(intent);
//						finish();
//					}
//				}, 100);
//			}
//		}, 900);

	}

	@Override
	int getActivityCode() {
		return CityOfTwo.ACTIVITY_SPLASH;
	}

	private void checkNetworkState() {
		if (!NetworkStateReceiver.IS_CONNECTED) {
			new AlertDialog.Builder(this, R.style.AppTheme_Dialog)
					.setMessage("It seems that network connectivity is not available. " +
							"Please check your connection settings")
					.setPositiveButton("Try Again", new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							checkNetworkState();
						}
					})
					.setNegativeButton("Exit", new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int which) {
							finish();
						}
					}).show();
		} else handleConnectionPresence();
	}

	private void handleConnectionPresence() {
		waitForGcm();
	}

	private void waitForGcm() {
		boolean deviceRegistered = getSharedPreferences(CityOfTwo.PACKAGE_NAME, MODE_PRIVATE)
				.getBoolean(CityOfTwo.KEY_DEVICE_REGISTERED, false);

		if (!deviceRegistered) {
			String token = FirebaseInstanceId.getInstance().getToken();

			if (token == null || token.isEmpty()) {
				BroadcastReceiver receiver = new BroadcastReceiver() {
					@Override
					public void onReceive(Context context, Intent intent) {
						LocalBroadcastManager.getInstance(SplashActivity.this)
								.unregisterReceiver(this);

						processToken();
					}
				};

				LocalBroadcastManager.getInstance(this).registerReceiver(
						receiver,
						new IntentFilter(CityOfTwo.ACTION_FCM_ID)
				);
			} else {
				Utils.registerToken(this, token);
				processToken();
			}
		}
	}

	private void processToken() {
		String token = getSharedPreferences(CityOfTwo.PACKAGE_NAME, MODE_PRIVATE)
				.getString(CityOfTwo.KEY_SESSION_TOKEN, "");

		if (token.isEmpty()) {
			startAppIntro();
		} else {
			initializeApp(token);
		}
	}

	private void initializeApp(final String token) {
		String[] path = {
				CityOfTwo.API,
				getString(R.string.url_get_init)
		};

		HttpHandler initHttpHandler = new HttpHandler(
				CityOfTwo.HOST,
				path,
				HttpHandler.GET
		) {
			@Override
			protected void onSuccess(String response) {
				try {
					JSONObject j = new JSONObject(response);

					String uniqueCode = j.getString(CityOfTwo.KEY_CODE);
					Integer credits = j.getInt(CityOfTwo.KEY_CREDITS);
					Boolean filters_applied = j.getBoolean(CityOfTwo.KEY_FILTERS_APPLIED);
					String fbid = j.getString(CityOfTwo.KEY_FBID);

					SharedPreferences.Editor securedEditor = new SecurePreferences(
							SplashActivity.this,
							CityOfTwo.SECURED_PREFERENCE
					).edit();

					SharedPreferences.Editor editor = getSharedPreferences(CityOfTwo.PACKAGE_NAME, MODE_PRIVATE)
							.edit();

					editor.putBoolean(CityOfTwo.KEY_FILTERS_APPLIED, filters_applied)
							.putBoolean(CityOfTwo.KEY_USER_OFFLINE, false)
							.putString(CityOfTwo.KEY_FBID, fbid)
							.putString(CityOfTwo.KEY_SESSION_TOKEN, token);

					if (filters_applied) {
						JSONObject filters = j.getJSONObject(CityOfTwo.KEY_FILTERS);
						Integer minAge = filters.getInt(CityOfTwo.KEY_MIN_AGE);
						Integer maxAge = filters.getInt(CityOfTwo.KEY_MAX_AGE);
						Integer distance = filters.getInt(CityOfTwo.KEY_DISTANCE);
						Boolean matchMale = filters.getBoolean(CityOfTwo.KEY_MATCH_MALE);
						Boolean matchFemale = filters.getBoolean(CityOfTwo.KEY_MATCH_FEMALE);

						editor.putInt(CityOfTwo.KEY_MIN_AGE, minAge)
								.putInt(CityOfTwo.KEY_MAX_AGE, maxAge)
								.putInt(CityOfTwo.KEY_DISTANCE, distance)
								.putBoolean(CityOfTwo.KEY_MATCH_MALE, matchMale)
								.putBoolean(CityOfTwo.KEY_MATCH_FEMALE, matchFemale);
					}

					securedEditor.putString(CityOfTwo.KEY_CODE, uniqueCode)
							.putInt(CityOfTwo.KEY_CREDITS, credits);

					editor.apply();
					securedEditor.apply();

					Intent intent = new Intent(SplashActivity.this, HomeActivity.class);
					startActivity(intent);
					finish();

				} catch (JSONException e) {
					e.printStackTrace();
					startAppIntro();
				}


			}

			@Override
			protected void onFailure(Integer status) {
				startAppIntro();
			}

		};

		initHttpHandler.addHeader("Authorization", "Token " + token);
		initHttpHandler.execute();
	}

	private void startAppIntro() {
		Intent intent = new Intent(this, IntroductionActivity.class);
		startActivity(intent);

		finish();
	}

	private void setupTest(JSONArray questions) {
		CityOfTwo.answerBitmapList = new HashMap<>();
		try {
			for (int i = 0; i < questions.length(); i++) {
				JSONObject question = questions.getJSONObject(i);

				String q = question.keys().next();
				JSONArray answers = question.getJSONArray(q);

				for (int j = 0; j < answers.length(); j++) {
					String url = (String) ((JSONArray) answers.get(j)).get(1);

					final String key = String.valueOf(i) + String.valueOf(j);
					Picasso.with(this)
							.load(url)
							.into(new Target() {
								@Override
								public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
									CityOfTwo.answerBitmapList.put(key, bitmap);
								}

								@Override
								public void onBitmapFailed(Drawable errorDrawable) {
								}

								@Override
								public void onPrepareLoad(Drawable placeHolderDrawable) {
								}
							});
				}
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
}
