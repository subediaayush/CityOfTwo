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
import android.support.annotation.NonNull;
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

	private static final int ALARM_REQUEST = 0;
	private static final int ALARM_TICK = 5;
	private static final int MAX_PROGRESS = 1200;
	private final String ARG_INTENT_MODIFIED = this.getClass().getName() + "intent modified";
	private final String ARG_BS_STATE = this.getClass().getName() + "bs_state";
	private final String ARG_TIMER_PROGRESS = this.getClass().getName() + "timer_progress";
	private final String ARG_CURRENT_GUEST = this.getClass().getName() + "timer_progress";
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
		mBottomSheetBehavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
			@Override
			public void onStateChanged(@NonNull View bottomSheet, int newState) {
				if (newState == BottomSheetBehavior.STATE_HIDDEN) {
					getSharedPreferences(CityOfTwo.PACKAGE_NAME, MODE_PRIVATE)
							.edit().remove(CityOfTwo.KEY_LAST_REQUEST)
							.apply();
				}
			}

			@Override
			public void onSlide(@NonNull View bottomSheet, float slideOffset) {

			}
		});
		initBottomSheet();

		mChatBroadcastReceiver = new BroadcastReceiver() {
			@Override
			public void onReceive(Context context, Intent intent) {
				String action = intent.getAction();
				switch (action) {
					case CityOfTwo.ACTION_REQUEST_CHAT: {
						DatabaseHelper db = new DatabaseHelper(ChatListenerPumpedActivity.this);
						SharedPreferences sp = getSharedPreferences(CityOfTwo.PACKAGE_NAME, MODE_PRIVATE);

						int requestId = sp.getInt(CityOfTwo.KEY_LAST_REQUEST, 0);
						if (requestId == 0) break;

						int guestId = sp.getInt(CityOfTwo.KEY_LAST_GUEST, -1);
						Contact guest = db.loadGuest(guestId);
						setRequestDuration(30);
						showReceivedRequest(guest);
						break;
					}
					case CityOfTwo.ACTION_REQUEST_TIMEOUT: {
						getSharedPreferences(CityOfTwo.PACKAGE_NAME, MODE_PRIVATE).edit()
								.remove(CityOfTwo.KEY_LAST_REQUEST)
								.remove(CityOfTwo.KEY_RESQUEST_DISPATCH)
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
						Contact guest = db.loadGuest(guestId);

						data.putInt(ProfileActivity.ARG_CHATROOM_ID, chatroomId);
						data.putParcelable(ProfileActivity.ARG_CURRENT_GUEST, guest);

						getSharedPreferences(CityOfTwo.PACKAGE_NAME, MODE_PRIVATE).edit()
								.remove(CityOfTwo.KEY_LAST_REQUEST)
								.remove(CityOfTwo.KEY_RESQUEST_DISPATCH)
								.apply();

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

	private void initBottomSheet() {
		mGuestIcon = (CircleImageView) findViewById(R.id.request_icon);
		mRequest = (TextView) findViewById(R.id.request_message);
		mResponse = findViewById(R.id.request_response);
		mTimer = (ProgressBar) findViewById(R.id.request_timer);
		mLikes = (TextView) findViewById(R.id.request_likes);

		SharedPreferences sp = getSharedPreferences(CityOfTwo.PACKAGE_NAME, MODE_PRIVATE);

		int requestId = sp.getInt(CityOfTwo.KEY_LAST_REQUEST, 0);
		if (requestId != 0) {
			int guestId = sp.getInt(CityOfTwo.KEY_LAST_GUEST, -1);
			DatabaseHelper db = new DatabaseHelper(this);
			Contact guest = db.loadGuest(guestId);
			if (requestId > 0) showReceivedRequest(guest);
			else showSentRequest(guest);
		} else {
			mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
		}
	}

	private void startNewChat(Bundle data) {
		Intent intent = new Intent(this, ProfileActivity.class);
		intent.putExtras(data);

		startActivityForResult(intent, CityOfTwo.ACTIVITY_PROFILE);
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
		return PendingIntent.getBroadcast(
				this,
				ALARM_REQUEST,
				alarmIntent,
				PendingIntent.FLAG_UPDATE_CURRENT
		);
	}

	protected void sendAcceptRequest(final Contact guest, int requestId) {
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
						"Could not find " + guest.nickName,
						Toast.LENGTH_SHORT
				).show();
				clearRequestView();
			}

			@Override
			protected void onPostExecute() {
				getSharedPreferences(CityOfTwo.PACKAGE_NAME, MODE_PRIVATE)
						.edit().remove(CityOfTwo.KEY_LAST_REQUEST)
						.remove(CityOfTwo.KEY_RESQUEST_DISPATCH)
						.apply();
			}
		};
		acceptHttpHandler.addHeader("Authorization", "Token " + token);
		acceptHttpHandler.execute();
	}

	protected void showSentRequest(Contact guest, int requestId) {
		getSharedPreferences(CityOfTwo.PACKAGE_NAME, MODE_PRIVATE)
				.edit().putInt(CityOfTwo.KEY_LAST_REQUEST, -requestId)
				.putInt(CityOfTwo.KEY_LAST_GUEST, guest.id)
				.putLong(CityOfTwo.KEY_RESQUEST_DISPATCH, System.currentTimeMillis())
				.apply();

		setRequestDuration(60);
		showSentRequest(guest);
	}


	private void showReceivedRequest(final Contact guest) {
		SharedPreferences sp = getSharedPreferences(CityOfTwo.PACKAGE_NAME, MODE_PRIVATE);
		final int requestId = sp.getInt(CityOfTwo.KEY_LAST_REQUEST, 0);

		mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);

		mResponse.setVisibility(View.VISIBLE);

		mResponse.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				mTimer.setIndeterminate(true);
				mTimer.clearAnimation();
				sendAcceptRequest(guest, requestId);
				mResponse.setVisibility(View.GONE);
				cancelAlarm();
			}
		});

		setupRequestView(guest, requestId, "Connecting to %s");

	}

	private void showSentRequest(Contact guest) {
		SharedPreferences sp = getSharedPreferences(CityOfTwo.PACKAGE_NAME, MODE_PRIVATE);
		final int requestId = sp.getInt(CityOfTwo.KEY_LAST_REQUEST, 0);

		mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
		mResponse.setVisibility(View.GONE);

		setupRequestView(guest, requestId, "Connecting to %s");
	}

	private boolean setupRequestView(Contact guest, int requestId, @NonNull final String message) {
		if (requestId == 0) {
			mBottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
			cancelAlarm();

			return false;
		}

		long remainingTime = getRequestRemainingTime();
		int progress = (int) (remainingTime / ((float) mDuration * 1000) * MAX_PROGRESS);

		Log.i(TAG, "Setting timer progress:" + progress);
		Log.i(TAG, "Setting remaining time:" + remainingTime);

		mRequest.setText(guest.nickName);
		String commonLikesMessages = "Likes " +
				Utils.getReadableList(guest.topLikes) + " and " +
				(guest.commonLikes - guest.topLikes.length) + " others";

		mLikes.setText(commonLikesMessages);

		if (guest.hasRevealed) {
			new FacebookHelper(this, guest.fid, -1, "name") {
				@Override
				public void onResponse(String response) {
					String responseMessage = String.format(message, response);
					mRequest.setText(responseMessage);
				}

				@Override
				public void onError() {

				}
			}.execute();
			FacebookHelper.loadFacebookProfilePicture(this, guest.fid, -1, mGuestIcon);
		}

		mTimer.setIndeterminate(false);

		mTimer.setProgress(progress);

		final ObjectAnimator animation = ObjectAnimator.ofInt(
				mTimer,
				"progress",
				0
		);

		Log.i(TAG, "Setting listener duration for " + remainingTime);
		animation.setDuration(remainingTime);
		animation.start();

		setupAlarm(remainingTime);
		return true;
	}

	public void setRequestDuration(int requestDuration) {
		this.mDuration = requestDuration;
	}

	public long getRequestRemainingTime() {
		SharedPreferences sp = getSharedPreferences(CityOfTwo.PACKAGE_NAME, MODE_PRIVATE);
		long progress = System.currentTimeMillis() - sp.getLong(CityOfTwo.KEY_RESQUEST_DISPATCH, Long.MAX_VALUE);
		return Math.max(mDuration * 1000 - Math.max(progress, 0), 0);
	}
}
