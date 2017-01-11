package com.messenger.cityoftwo;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

/**
 * Created by Aayush on 9/10/2015.
 */
public class NetworkStateReceiver extends BroadcastReceiver {

	public static boolean IS_CONNECTED = false;

	public static void checkConnectivity(Context context) {
		ConnectivityManager connectivityManager
				= (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();

		IS_CONNECTED = activeNetworkInfo != null && activeNetworkInfo.isConnected();

	}

	@Override
	public void onReceive(Context context, Intent intent) {
		checkConnectivity(context);

		onNetworkStatechanged(IS_CONNECTED);
	}

	public void onNetworkStatechanged(boolean isConnected) {
	}
}
