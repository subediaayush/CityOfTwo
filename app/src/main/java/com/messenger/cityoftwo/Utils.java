package com.messenger.cityoftwo;

import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.LabeledIntent;
import android.content.pm.ResolveInfo;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import static android.content.Context.MODE_PRIVATE;
import static com.messenger.cityoftwo.CityOfTwo.KEY_CODE;

/**
 * Created by Aayush on 1/17/2017.
 */

public class Utils {

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

	public static void registerToken(Context context, String fcmid) {
		final SharedPreferences sp = context.getSharedPreferences(CityOfTwo.PACKAGE_NAME, MODE_PRIVATE);

		String broadcast_gcm = context.getString(R.string.url_broadcast_gcm),
				header = CityOfTwo.HEADER_GCM_ID,
				value = fcmid;

		String[] path = {CityOfTwo.API, broadcast_gcm};

		HttpHandler registerDeviceHttpHandler = new HttpHandler(
				CityOfTwo.HOST,
				path,
				HttpHandler.POST,
				CityOfTwo.HEADER_GCM_ID,
				fcmid
		) {
			@Override
			protected void onSuccess(String response) {
				try {
					JSONObject Response = new JSONObject(response);

					Boolean status = Response.getBoolean("parsadi");

					if (!status) {
						setRegisteredStatus(true);
					} else {
						setRegisteredStatus(false);
					}

				} catch (JSONException e) {
					setRegisteredStatus(false);
					e.printStackTrace();
				}
			}

			@Override
			protected void onFailure(Integer status) {
				setRegisteredStatus(false);
			}

			private void setRegisteredStatus(boolean status) {
				sp.edit().putBoolean(CityOfTwo.KEY_DEVICE_REGISTERED, status).apply();
			}
		};

		String token = "Token " + sp.getString(CityOfTwo.KEY_SESSION_TOKEN, "");

		registerDeviceHttpHandler.addHeader("Authorization", token);
		registerDeviceHttpHandler.execute();
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
