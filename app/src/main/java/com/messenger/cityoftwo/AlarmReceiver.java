package com.messenger.cityoftwo;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

/**
 * Created by Aayush on 2/2/2017.
 */
public class AlarmReceiver extends BroadcastReceiver {
	private static final String TAG = "AlarmReceiver";

	@Override
	public void onReceive(Context context, Intent intent) {
		context.getSharedPreferences(CityOfTwo.PACKAGE_NAME, Context.MODE_PRIVATE)
				.edit().remove(CityOfTwo.KEY_LAST_REQUEST)
				.remove(CityOfTwo.KEY_RESQUEST_DISPATCH)
				.apply();

		Log.i(TAG, "Request Timeout Received " + intent.getAction());
		intent.setAction(CityOfTwo.ACTION_REQUEST_TIMEOUT);
		LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
	}
}
