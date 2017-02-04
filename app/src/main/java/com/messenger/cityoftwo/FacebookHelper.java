package com.messenger.cityoftwo;

import android.content.Context;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;

import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;
import java.util.HashMap;

/**
 * Created by Aayush on 2/1/2017.
 */
public abstract class FacebookHelper {

	private static HashMap<String, String> uriCache;
	private static HashMap<String, Integer> scopeCache;
	final String TAG = "FacebookHelper";
	String key;
	String[] args;
	int identifier;

	private Context mContext;

	public FacebookHelper(Context context, String fbid, int identifier, String... args) {
		mContext = context;

		if (args.length > 1 && (args.length - 1) % 2 != 0)
			throw new IllegalArgumentException("Proper name value pair not found");

		if (uriCache == null) uriCache = new HashMap<>();
		if (scopeCache == null) scopeCache = new HashMap<>();
		String[] arr = new String[args.length + 1];

		arr[0] = fbid;
		int counter = 1;
		for (String arg : args) {
			arr[counter++] = arg;
		}

		this.args = arr;
		this.key = getValidKey(arr);
		this.identifier = identifier;
	}

	public static void loadFacebookProfilePicture(final Context context, String fbid, int identifier, final ImageView image) {
		String width = String.valueOf(image.getLayoutParams().width);
		String height = String.valueOf(image.getLayoutParams().height);

		new FacebookHelper(context, fbid, identifier, "picture", "height", height, "width", width) {
			@Override
			public void onResponse(String response) {
				Picasso.with(context)
						.load(response)
						.into(image);
			}

			@Override
			public void onError() {

			}
		}.execute();
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

	private void cacheScope(int identifier) {
		if (scopeCache.containsKey(key) && scopeCache.get(key) == -1) return;

		scopeCache.put(key, identifier);
	}

	public void execute() {
		Log.i(TAG, "Checking for cache for key " + getNode());
		if (keyCached()) {
			Log.i(TAG, "Applying data from cache");
			onResponse(getCachedData());
		} else {
			Log.i(TAG, "No cached data found");
			final String fbid = getFbid();
			String node = getNode();
			String[] args = getArgs();

			String requestUri = "/" + fbid;

			Bundle param = new Bundle();
			param.putBoolean("redirect", false);
			param.putString("fields", node);

			for (int i = 0; i < args.length; i += 2) {
				param.putString(args[i], args[i + 1]);
			}

			String host = CityOfTwo.HOST;
			String[] path = new String[]{
					CityOfTwo.API,
					mContext.getString(R.string.url_get_user_data)
			};

			JSONObject j = new JSONObject();
			try {
				j.put("type", node);
				for (int i = 0; i < args.length; i += 2) {
					j.put(args[i], args[i + 1]);
				}
			} catch (JSONException e) {
				e.printStackTrace();
			}

			HttpHandler revealHandler = new HttpHandler(host, path, HttpHandler.GET, j) {
				@Override
				protected void onSuccess(String response) {
					try {
						JSONObject j = new JSONObject(response);
						Boolean success = j.getBoolean("parsadi");
						if (success) {
							String output = getRequiredResponse(j);
							cacheScope(identifier);
							uriCache.put(key, output);
							onResponse(output);
						}
					} catch (JSONException e) {
						onFailure(-1);
					}

				}

				@Override
				protected void onFailure(Integer status) {
					onError();
				}
			};

			String token = "Token " + mContext.getSharedPreferences(CityOfTwo.PACKAGE_NAME, Context.MODE_PRIVATE)
					.getString(CityOfTwo.KEY_SESSION_TOKEN, "");

			revealHandler.addHeader("Authorization", token);
			revealHandler.execute();

//			new GraphRequest(
//					getAppAccessToken(),
//					requestUri,
//					param,
//					HttpMethod.GET,
//					new GraphRequest.Callback() {
//						@Override
//						public void onCompleted(GraphResponse response) {
//							Log.i(TAG, "Applying data from GraphAPI call:" + response.getRequest().toString());
//							try {
//								Log.i(TAG, "Data:" + response.toString());
//								String output = getRequiredResponse(response);
//								uriCache.put(key, output);
//								onResponse(output);
//							} catch (JSONException e) {
//								onError(e);
//							}
//						}
//					}
//			).executeAsync();
		}
	}

	private String getCachedData() {
		return uriCache.get(key);
	}

	private boolean keyCached() {
		if (scopeCache.containsKey(key)) {
			int id = scopeCache.get(key);
			return id == -1 || id == identifier;
		} else return false;
	}

	private String getRequiredResponse(JSONObject response) throws JSONException {
		String node = getNode();
		return response.getString(node);
	}

	private String getValidKey(String... args) {
		return Utils.getReadableList(args);
	}

	private String getFbid() {
		return args[0];
	}

	private String getNode() {
		return args[1];
	}

	private String[] getArgs() {
		int size = args.length;
		if (args.length == 2) return new String[0];

		return Arrays.copyOfRange(args, 2, size);
	}

	public abstract void onResponse(String response);

	public abstract void onError();
}
