package com.messenger.cityoftwo;

import android.content.SharedPreferences;

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
		Utils.registerToken(this);
	}
}
