package com.messenger.cityoftwo;

import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.LabeledIntent;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.facebook.AccessToken;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.HttpMethod;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static com.messenger.cityoftwo.CityOfTwo.KEY_CODE;

/**
 * Created by Aayush on 1/17/2017.
 */

public class Utils {

	private static String lastFBID = "";
	private static String lastFBURL = "";

	public static String getReadableList(String[] list) {
		int i;
		String output = list[0];
		for (i = 1; i < list.length; i++)
			output += ", " + list[i];

		return output;
	}

	public static String humanizeDateTime(long time) {
		long now = System.currentTimeMillis();

		long diff = Math.abs(now - time);

		if (diff < CityOfTwo.MILLIS_IN_DAY) return new SimpleDateFormat(
				"hh:mm a",
				Locale.getDefault()
		).format(time);

		else if (diff < CityOfTwo.DAYS_IN_WEEK) return new SimpleDateFormat(
				"hh:mm a\nEEEE",
				Locale.getDefault()
		).format(time);

		else if (diff < CityOfTwo.MONTHS_IN_YEAR) return new SimpleDateFormat(
				"hh:mm a\nMMMM dd",
				Locale.getDefault()
		).format(time);

		else return new SimpleDateFormat(
					"hh:mm a\nMMM dd, yyyy",
					Locale.getDefault()
			).format(time);

	}

	public static void loadRandomPicture(final Context context, final Integer id, final ImageView imageView) {
		String uri = "http://" + CityOfTwo.HOST + "/" + CityOfTwo.API + "/" + context.getString(R.string.url_get_icon);
		Picasso.with(context)
				.load(uri)
				.into(imageView);
	}

	public static void loadFacebookPicture(final Context context, final String fbid, final ImageView imageView) {
		if (lastFBID.equals(fbid) && !lastFBURL.isEmpty()) {
			Picasso.with(context)
					.load(lastFBURL)
					.into(imageView);

			return;
		}

		final String TAG = "GraphAPI";

		String requestUri = "/" + fbid + "/picture";

		int height = imageView.getLayoutParams().height;
		int width = imageView.getLayoutParams().width;

		Bundle args = new Bundle();
		args.putBoolean("redirect", false);
		args.putInt("height", height);
		args.putInt("width", width);

		new GraphRequest(
				AccessToken.getCurrentAccessToken(),
				requestUri,
				args,
				HttpMethod.GET,
				new GraphRequest.Callback() {
					public void onCompleted(GraphResponse response) {
						try {
							Log.i(TAG, response.getRequest().toString());


							JSONObject j = response.getJSONObject().getJSONObject("data");

							try {
								lastFBURL = j.getString("url");
								Picasso.with(context)
										.load(lastFBURL)
										.into(imageView);

								lastFBID = fbid;
							} catch (JSONException e) {
								e.printStackTrace();
							}
						} catch (JSONException e) {
							e.printStackTrace();
						}


					}
				}
		).executeAsync();
	}

	public static void hideLater(final View view, int delay) {
		new Handler().postDelayed(new Runnable() {
			@Override
			public void run() {
				view.setVisibility(View.GONE);
			}
		}, delay);
	}

	public static class CoyRudy {
		public static void shareCoyRudy(Context context, String uniqueUrl) {

			List<String> targetedApps = new ArrayList<>();

			targetedApps.add("com.facebook.katana");
			targetedApps.add("com.facebook.orca");
			targetedApps.add("com.twitter.android");
			targetedApps.add("com.google.android.apps.plus");

			List<LabeledIntent> shareIntentList = new ArrayList<>();
			Intent shareIntent = new Intent(Intent.ACTION_SEND);
			shareIntent.setType("name/plain");

			List<ResolveInfo> resolveInfoList = context.getPackageManager().queryIntentActivities(shareIntent, 0);

			if (!resolveInfoList.isEmpty()) {
				for (ResolveInfo resolveInfo : resolveInfoList) {
					String packageName = resolveInfo.activityInfo.packageName;
					if (targetedApps.contains(packageName)) {
						Intent targetedShareIntent = new Intent(Intent.ACTION_SEND);
						targetedShareIntent.setComponent(new ComponentName(packageName, resolveInfo.activityInfo.name));
						targetedShareIntent.setType("name/plain");
						targetedShareIntent.putExtra(Intent.EXTRA_SUBJECT, "CoyRudy!");
						targetedShareIntent.putExtra(Intent.EXTRA_TEXT, uniqueUrl);

						shareIntentList.add(new LabeledIntent(
								targetedShareIntent,
								packageName,
								resolveInfo.loadLabel(context.getPackageManager()),
								resolveInfo.icon
						));
					}
				}
			}
			if (shareIntentList.isEmpty()) {
				new AlertDialog.Builder(context, R.style.AppTheme_Dialog)
						.setTitle("Error")
						.setMessage("No app found for sharing CoyRudy. Referral link has been copied to your clipboard")
						.setNeutralButton("Ok", null)
						.show();

				android.content.ClipboardManager clipboard = (android.content.ClipboardManager) context
						.getSystemService(Context.CLIPBOARD_SERVICE);
				android.content.ClipData clip = android.content.ClipData
						.newPlainText("Unique URL", uniqueUrl);
				clipboard.setPrimaryClip(clip);

				return;
			}

			// convert shareIntentList to array
			LabeledIntent[] extraIntents = shareIntentList.toArray(new LabeledIntent[shareIntentList.size()]);

			Intent chooserIntent = Intent.createChooser(new Intent(), "Select app to share");
			chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, extraIntents);
			context.startActivity(chooserIntent);
		}

		public static void referCoyRudy(final Context context) {
			AlertDialog.Builder adb = new AlertDialog.Builder(context, R.style.AppTheme_Dialog);
			adb.setTitle("Refer CoyRudy");

			View referCoyRudyView = LayoutInflater.from(context)
					.inflate(R.layout.layout_share_coyrudy, null);
			TextView linkTextView = (TextView) referCoyRudyView.findViewById(R.id.coyrudy_link);

			String uniqueCode = new SecurePreferences(context, CityOfTwo.SECURED_PREFERENCE)
					.getString(KEY_CODE, "");

			final String shareText = context.getString(R.string.url_share_coyrudy) + uniqueCode;

			linkTextView.setText(shareText);

			adb.setView(referCoyRudyView);
			adb.setPositiveButton("Share", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					shareCoyRudy(context, shareText);
				}
			});
			adb.setNegativeButton("Copy Link", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					android.content.ClipboardManager clipboard = (android.content.ClipboardManager) context
							.getSystemService(Context.CLIPBOARD_SERVICE);
					android.content.ClipData clip = android.content.ClipData
							.newPlainText("Unique URL", shareText);
					clipboard.setPrimaryClip(clip);
				}
			});
			adb.show();

		}

	}

}
