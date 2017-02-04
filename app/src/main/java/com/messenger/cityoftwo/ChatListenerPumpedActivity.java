package com.messenger.cityoftwo;

import android.animation.ObjectAnimator;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.CoordinatorLayout;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.View;
import android.view.ViewStub;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Created by Aayush on 2/2/2017.
 */

public abstract class ChatListenerPumpedActivity extends PumpedActivity {

	private static final String ARG_INTENT_MODIFIED = "intent modified";
	private static final String ARG_BS_STATE = "bs_state";
	private static final String ARG_TIMER_PROGRESS = "timer_progress";

	private static final int ALARM_REQUEST = 0;

	private static final int ALARM_TICK = 5;
	private static final int MAX_PROGRESS = 1200;

	protected BroadcastReceiver mChatBroadcastReceiver;
	ViewStub mStub;
	private BottomSheetBehavior mBottomSheetBehavior;
	private CoordinatorLayout mParentLayout;
	private View mBottomSheet;

	private TextView mLikes;
	private ProgressBar mTimer;
	private View mResponse;
	private TextView mRequest;
	private CircleImageView mGuestIcon;

	private int mRequestId;
	private Contact mGuest;
	private int mDuration = 30;

	protected abstract int getContentLayout();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_chat_listener);

		mParentLayout = (CoordinatorLayout) findViewById(R.id.main_content);

		mStub = (ViewStub) findViewById(R.id.layout_stub);
		mStub.setLayoutResource(getContentLayout());
		mStub.inflate();

		mBottomSheet = findViewById(R.id.bottom_sheet);

		mBottomSheetBehavior = BottomSheetBehavior.from(mBottomSheet);
		mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
		initBottomSheet();

		mChatBroadcastReceiver = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				String action = intent.getAction();
				switch (action) {
					case CityOfTwo.ACTION_REQUEST_CHAT: {
						DatabaseHelper db = new DatabaseHelper(ChatListenerPumpedActivity.this);
						SharedPreferences sp = getSharedPreferences(CityOfTwo.PACKAGE_NAME, MODE_PRIVATE);
						int guestId = sp.getInt(CityOfTwo.KEY_LAST_GUEST, -1);
						mGuest = db.loadGuest(guestId);
						setRequestDuration(30);
						setupRequestView();
						break;
					}
					case CityOfTwo.ACTION_REQUEST_TIMEOUT: {
						getSharedPreferences(CityOfTwo.PACKAGE_NAME, MODE_PRIVATE).edit()
								.remove(CityOfTwo.KEY_LAST_REQUEST)
								.apply();
						Log.i(TAG, "Request Timeout Received");
						clearRequestView();
						break;
					}
					case CityOfTwo.ACTION_BEGIN_CHAT: {
						clearRequestView();
						Bundle data = new Bundle();

						DatabaseHelper db = new DatabaseHelper(ChatListenerPumpedActivity.this);
						SharedPreferences sp = getSharedPreferences(CityOfTwo.PACKAGE_NAME, MODE_PRIVATE);
						int guestId = sp.getInt(CityOfTwo.KEY_LAST_GUEST, -1);
						int chatroomId = sp.getInt(CityOfTwo.KEY_LAST_CHATROOM, -1);
						mGuest = db.loadGuest(guestId);

						data.putInt(ProfileActivity.ARG_CHATROOM_ID, chatroomId);
						data.putParcelable(ProfileActivity.ARG_CURRENT_GUEST, mGuest);

						startNewChat(data);
						break;
					}

					case CityOfTwo.ACTION_REQUEST_DENIED: {
						break;
					}
				}
			}
		};
	}

	@Override
	protected void onPause() {
		super.onStop();

		LocalBroadcastManager.getInstance(this).unregisterReceiver(mChatBroadcastReceiver);
	}

	@Override
	protected void onResume() {
		super.onResume();

		IntentFilter filter = new IntentFilter();
		filter.addAction(CityOfTwo.ACTION_BEGIN_CHAT);
		filter.addAction(CityOfTwo.ACTION_REQUEST_CHAT);
		filter.addAction(CityOfTwo.ACTION_REQUEST_DENIED);
		filter.addAction(CityOfTwo.ACTION_REQUEST_TIMEOUT);
		LocalBroadcastManager.getInstance(this).registerReceiver(mChatBroadcastReceiver, filter);

		initBottomSheet();
	}

	private void clearRequestView() {
		mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
	}

	private Intent maintainBottomSheetData(Intent intent) {
		SharedPreferences sp = getSharedPreferences(CityOfTwo.PACKAGE_NAME, MODE_PRIVATE);
		int requestId = sp.getInt(CityOfTwo.KEY_LAST_REQUEST, -1);

		Boolean intentModified = intent.getBooleanExtra(ARG_INTENT_MODIFIED, false);
		if (!intentModified && requestId != -1) {
			intent.putExtra(ARG_BS_STATE, mBottomSheetBehavior.getState());
			intent.putExtra(ARG_TIMER_PROGRESS, mTimer.getProgress());
		}

		return intent;
	}

	@Override
	public void startActivity(Intent intent) {
		startActivityForResult(intent, -1);
	}

	@Override
	public void finish() {
		setConsistentResult(-1);
		super.finish();
	}

	private void initBottomSheet() {
		mGuestIcon = (CircleImageView) findViewById(R.id.request_icon);
		mRequest = (TextView) findViewById(R.id.request_message);
		mResponse = findViewById(R.id.request_response);
		mTimer = (ProgressBar) findViewById(R.id.request_timer);
		mLikes = (TextView) findViewById(R.id.request_likes);

		SharedPreferences sp = getSharedPreferences(CityOfTwo.PACKAGE_NAME, MODE_PRIVATE);

		int requestId = sp.getInt(CityOfTwo.KEY_LAST_REQUEST, -1);
		if (requestId != -1) {
			int guestId = sp.getInt(CityOfTwo.KEY_LAST_GUEST, -1);
			DatabaseHelper db = new DatabaseHelper(this);
			mGuest = db.loadGuest(guestId);
			setupRequestView();
		} else {
			mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
		}
	}

	private void startNewChat(Bundle data) {
		Intent intent = new Intent(this, ProfileActivity.class);
		intent.putExtras(data);

		startActivityForResult(intent, CityOfTwo.ACTIVITY_PROFILE);
	}

	private void setupRequestView() {
		SharedPreferences sp = getSharedPreferences(CityOfTwo.PACKAGE_NAME, MODE_PRIVATE);
		final int requestId = sp.getInt(CityOfTwo.KEY_LAST_REQUEST, -1);

		if (requestId == -1) {
			mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
			cancelAlarm();

			return;
		}

		int progress = getIntent().getIntExtra(ARG_TIMER_PROGRESS, MAX_PROGRESS);
		int remainingTime = progress * mDuration / MAX_PROGRESS;

		mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);

		mRequest.setText(mGuest.nickName + " wants to chat.");
		String commonLikesMessages = "Likes " +
				Utils.getReadableList(mGuest.topLikes) + " and " +
				(mGuest.commonLikes - mGuest.topLikes.length) + " others";

		mLikes.setText(commonLikesMessages);

		if (mGuest.hasRevealed) {
			new FacebookHelper(this, mGuest.fid, -1, "name") {
				@Override
				public void onResponse(String response) {
					mRequest.setText(response + " wants to chat.");
				}

				@Override
				public void onError() {

				}
			}.execute();
			FacebookHelper.loadFacebookProfilePicture(this, mGuest.fid, -1, mGuestIcon);
		}

		mResponse.setVisibility(View.VISIBLE);
		mTimer.setIndeterminate(false);
		mTimer.setProgress(progress);

		final ObjectAnimator animation = ObjectAnimator.ofInt(
				mTimer,
				"progress",
				0
		);

		Log.i(TAG, "Setting listener duration for " + remainingTime);
		animation.setDuration(remainingTime * 1000);
		animation.start();

		mResponse.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				mTimer.setIndeterminate(true);
				animation.cancel();
				sendAcceptRequest(requestId);
				mResponse.setVisibility(View.GONE);
				cancelAlarm();
			}
		});

		setupAlarm(remainingTime * 1000);
	}

	protected void sendAcceptRequest(int requestId) {
		final String token = getSharedPreferences(CityOfTwo.PACKAGE_NAME, MODE_PRIVATE)
				.getString(CityOfTwo.KEY_SESSION_TOKEN, "");
		String host = CityOfTwo.HOST;
		String[] path = new String[]{
				CityOfTwo.API,
				getString(R.string.url_accept_chat_request)
		};

		HttpHandler acceptHttpHandler = new HttpHandler(
				host,
				path,
				HttpHandler.GET,
				CityOfTwo.KEY_REQUEST_ID,
				String.valueOf(requestId)
		) {
			@Override
			protected void onSuccess(String response) {
				try {
					JSONObject j = new JSONObject(response);
					Boolean success = j.getBoolean("parsadi");
					if (!success) onFailure(-1);
				} catch (JSONException e) {
					onFailure(-1);
				}
			}

			@Override
			protected void onFailure(Integer status) {
				Toast.makeText(
						ChatListenerPumpedActivity.this,
						"Could not find " + mGuest.nickName,
						Toast.LENGTH_SHORT
				).show();
				clearRequestView();
			}
		};
		acceptHttpHandler.addHeader("Authorization", "Token " + token);
		acceptHttpHandler.execute();
	}

	private void cancelAlarm() {
		PendingIntent pendingIntent = getAlarmPendingIntent();
		AlarmManager am = (AlarmManager) getSystemService(ALARM_SERVICE);

		am.cancel(pendingIntent);
	}

	private void setupAlarm(long duration) {
		PendingIntent pendingIntent = getAlarmPendingIntent();
		AlarmManager am = (AlarmManager) getSystemService(ALARM_SERVICE);

		am.set(
				AlarmManager.ELAPSED_REALTIME,
				SystemClock.elapsedRealtime() + duration,
				pendingIntent
		);
	}

	private PendingIntent getAlarmPendingIntent() {
		Intent alarmIntent = new Intent(this, AlarmReceiver.class);
		alarmIntent.setAction(CityOfTwo.ACTION_REQUEST_ALARM);
		PendingIntent pendingIntent = PendingIntent.getBroadcast(
				this,
				ALARM_REQUEST,
				alarmIntent,
				PendingIntent.FLAG_UPDATE_CURRENT
		);
		return pendingIntent;
	}

	protected void showSentRequest(Contact guest, int requestId) {
		getSharedPreferences(CityOfTwo.PACKAGE_NAME, MODE_PRIVATE)
				.edit().putInt(CityOfTwo.KEY_LAST_REQUEST, requestId)
				.apply();

		mGuest = guest;

		setRequestDuration(60);
		setupRequestView();
	}

	public void setConsistentResult(int resultCode) {
		setConsistentResult(resultCode, new Intent());
	}

	public void setConsistentResult(int resultCode, Intent data) {
		data.putExtra(ARG_BS_STATE, mBottomSheetBehavior.getState());
		data.putExtra(ARG_TIMER_PROGRESS, mTimer.getProgress());

		setResult(resultCode, data);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (getIntent() == null) setIntent(new Intent());
		getIntent().putExtras(data);
	}

	@Override
	public void startActivityForResult(Intent intent, int requestCode) {
		super.startActivityForResult(maintainBottomSheetData(intent), requestCode);
	}

	public void setRequestDuration(int requestDuration) {
		this.mDuration = requestDuration;
	}
}
