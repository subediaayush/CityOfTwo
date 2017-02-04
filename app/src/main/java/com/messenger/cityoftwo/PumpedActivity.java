package com.messenger.cityoftwo;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

/**
 * Created by Aayush on 1/30/2017.
 */

public abstract class PumpedActivity extends AppCompatActivity {
	protected String TAG;
	int mActivityCode = -1;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mActivityCode = getActivityCode();
		TAG = CityOfTwo.ACTIVITY[mActivityCode];
	}

	abstract int getActivityCode();

	@Override
	protected void onPause() {
		super.onPause();
		Log.i(TAG, "Application paused");
		CityOfTwo.setApplicationState(CityOfTwo.APPLICATION_BACKGROUND);
	}

	@Override
	protected void onResume() {
		super.onResume();

		Log.i(TAG, "Application resumed");

		CityOfTwo.setApplicationState(CityOfTwo.APPLICATION_FOREGROUND);
		CityOfTwo.setCurrentActivity(mActivityCode);
	}
}
