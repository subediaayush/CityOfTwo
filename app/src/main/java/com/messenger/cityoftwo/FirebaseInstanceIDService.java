package com.messenger.cityoftwo;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v4.content.LocalBroadcastManager;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

/**
 * Created by Aayush on 7/16/2016.
 */
public class FirebaseInstanceIDService extends FirebaseInstanceIdService {

	@Override
	public void onTokenRefresh() {
		SharedPreferences sp = getSharedPreferences(CityOfTwo.PACKAGE_NAME, MODE_PRIVATE);


		String token = FirebaseInstanceId.getInstance().getToken();

		sp.edit().putString(CityOfTwo.KEY_REG_ID, token)
				.putBoolean(CityOfTwo.KEY_DEVICE_REGISTERED, false)
				.apply();

		Utils.registerToken(this, token);

		LocalBroadcastManager.getInstance(this).sendBroadcast(new Intent(CityOfTwo.ACTION_FCM_ID));

//        if (tokenObtainedListener != null) tokenObtainedListener.onTokenObtained();

	}
}
