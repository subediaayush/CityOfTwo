package com.messenger.cityoftwo;

import android.content.Intent;
import android.support.v4.content.LocalBroadcastManager;

import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.FirebaseInstanceIdService;

/**
 * Created by Aayush on 7/16/2016.
 */
public class FirebaseInstanceIDService extends FirebaseInstanceIdService {

    @Override
    public void onTokenRefresh() {

        String token = FirebaseInstanceId.getInstance().getToken();

        getSharedPreferences(CityOfTwo.PACKAGE_NAME, MODE_PRIVATE)
                .edit()
                .putString(CityOfTwo.KEY_REG_ID, token)
                .apply();

        LocalBroadcastManager.getInstance(this).sendBroadcast(new Intent(CityOfTwo.ACTION_FCM_ID));

//        if (tokenObtainedListener != null) tokenObtainedListener.onTokenObtained();

    }
}
