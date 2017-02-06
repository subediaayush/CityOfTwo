package com.messenger.cityoftwo;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.firebase.messaging.RemoteMessage;

import java.util.ArrayList;
import java.util.Map;

import static com.messenger.cityoftwo.CityOfTwo.ACTIVITY_HOME;
import static com.messenger.cityoftwo.CityOfTwo.APPLICATION_BACKGROUND;
import static com.messenger.cityoftwo.CityOfTwo.getApplicationState;
import static com.messenger.cityoftwo.CityOfTwo.getCurrentActivity;
import static com.messenger.cityoftwo.CityOfTwo.pendingMessages;

/**
 * Created by Aayush on 7/16/2016.
 */
public class FirebaseMessageHandler extends com.google.firebase.messaging.FirebaseMessagingService {
	protected static final String TAG_NOTIFICATION_CHAT_END = CityOfTwo.PACKAGE_NAME + ".notification_chat_end";
	protected static final String TAG_NOTIFICATION_CHAT_BEGIN = CityOfTwo.PACKAGE_NAME + ".notification_new_chat";
	protected static final String TAG_NOTIFICATION_NEW_MESSAGE = CityOfTwo.PACKAGE_NAME + ".notification_new_message";
	
	private static final String TAG = "FirebaseMessageHandler";
	
	private final int ID_NOTIFICATION_CHAT_BEGIN = 1;
	private final int ID_NOTIFICATION_CHAT_END = 2;
	private final int ID_NOTIFICATION_NEW_MESSAGE = 3;

	private LocalBroadcastManager mBroadcaster;

	@Override
	public void onMessageReceived(RemoteMessage remoteMessage) {
		Map<String, String> data = remoteMessage.getData();
		if (mBroadcaster == null) mBroadcaster = LocalBroadcastManager.getInstance(this);

		try {
			Log.i(TAG, "GCM Message received " + data);

			final SharedPreferences sharedPreferences = getSharedPreferences(CityOfTwo.PACKAGE_NAME, MODE_PRIVATE);

			String messageType = data.get(CityOfTwo.KEY_TYPE);
			NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
			Intent intent = new Intent();
			int oldChatroomId = sharedPreferences.getInt(CityOfTwo.KEY_LAST_CHATROOM, -1);

			DatabaseHelper db = new DatabaseHelper(this);

			int currentActivity = getCurrentActivity(), applicationState = getApplicationState();

			boolean handled = true;
			switch (messageType) {
				case CityOfTwo.KEY_CHAT_BEGIN: {
					int chatroomId = Integer.parseInt(data.get(CityOfTwo.KEY_CHATROOM_ID));

					if (chatroomId == oldChatroomId) break;

					Contact guest = new Contact(data.get(CityOfTwo.KEY_GUEST));

					nm.cancel(TAG_NOTIFICATION_NEW_MESSAGE, ID_NOTIFICATION_NEW_MESSAGE);
					
					db.insertMessages(chatroomId, guest.lastMessages);
					db.saveGuest(guest);

					Log.i(TAG, "Current Activity: " + CityOfTwo.ACTIVITY[currentActivity]);

					if (currentActivity == ACTIVITY_HOME) sharedPreferences.edit()
							.putInt(CityOfTwo.KEY_LAST_CHATROOM, chatroomId)
							.putInt(CityOfTwo.KEY_LAST_GUEST, guest.id)
							.apply();

					if (applicationState == CityOfTwo.APPLICATION_BACKGROUND) {
						Intent notificationIntent = new Intent(this, HomeActivity.class);

						PendingIntent p = PendingIntent.getActivity(
								this,
								(int) System.currentTimeMillis(),
								notificationIntent,
								PendingIntent.FLAG_UPDATE_CURRENT
						);

						Notification n = new NotificationCompat.Builder(this)
								.setContentTitle("New Match")
								.setContentText("Your match is ready! Tap here to start chatting")
								.setSmallIcon(R.drawable.ic_small)
								.setContentIntent(p)
								.setPriority(Notification.PRIORITY_HIGH)
								.setAutoCancel(true)
								.build();

						n.defaults |= Notification.DEFAULT_SOUND;
						n.defaults |= Notification.DEFAULT_VIBRATE;

						nm.notify(TAG_NOTIFICATION_CHAT_BEGIN, ID_NOTIFICATION_CHAT_BEGIN, n);
					} else {
						intent = new Intent(CityOfTwo.ACTION_BEGIN_CHAT);
						mBroadcaster.sendBroadcast(intent);
					}
					break;
				}
				case CityOfTwo.KEY_MESSAGE: {
					int chatroomId = Integer.parseInt(data.get(CityOfTwo.KEY_CHATROOM_ID));
					if (oldChatroomId != chatroomId) break;
					
					String text = data.get(CityOfTwo.KEY_MESSAGE_DATA);
					Integer flags = Integer.parseInt(data.get(CityOfTwo.KEY_MESSAGE_FLAGS));
					long time = System.currentTimeMillis();
//					long time = Long.parseLong(data.get(CityOfTwo.KEY_MESSAGE_TIME));
					
					Conversation c = new Conversation(text, flags, time);
					c.removeFlag(CityOfTwo.FLAG_SENT);
					c.addFlag(CityOfTwo.FLAG_RECEIVED);
					
					// Current activity is not Conversation Activity or
					// Current activity is Conversation Activty but is in background
					Log.i(TAG, "Current Activity: " + CityOfTwo.ACTIVITY[currentActivity]);

					if ((currentActivity == CityOfTwo.ACTIVITY_PROFILE &&
							applicationState == CityOfTwo.APPLICATION_BACKGROUND) ||
							currentActivity != CityOfTwo.ACTIVITY_PROFILE) {
						
						db.insertMessage(chatroomId, c);
						
						if ((flags & CityOfTwo.FLAG_PROFILE) == CityOfTwo.FLAG_PROFILE)
							text = "Stranger shared their profile with you";

						Intent notificationIntent = new Intent(this, ProfileActivity.class);

						PendingIntent p = PendingIntent.getActivity(
								this,
								(int) System.currentTimeMillis(),
								notificationIntent,
								PendingIntent.FLAG_UPDATE_CURRENT
						);

						if (pendingMessages == null) pendingMessages = new ArrayList<>();
						pendingMessages.add(text);

                        /* Add Big View Specific Configuration */
						NotificationCompat.InboxStyle inboxStyle = new NotificationCompat.InboxStyle();
						// Sets a title for the Inbox style big view
						inboxStyle.setBigContentTitle("New messages");
						for (String message : pendingMessages) inboxStyle.addLine(message);

						Notification n = new NotificationCompat.Builder(this)
								.setContentTitle("You have new message")
								.setStyle(inboxStyle)
								.setContentText(text)
								.setNumber(pendingMessages.size())
								.setSmallIcon(R.drawable.ic_small)
								.setContentIntent(p)
								.setPriority(Notification.PRIORITY_HIGH)
								.setAutoCancel(true)
								.build();
						
						n.defaults |= Notification.DEFAULT_SOUND;
						n.defaults |= Notification.DEFAULT_VIBRATE;

						nm.notify(TAG_NOTIFICATION_NEW_MESSAGE, ID_NOTIFICATION_NEW_MESSAGE, n);
					} else {
						intent.setAction(CityOfTwo.ACTION_NEW_MESSAGE);

						intent.putExtra(CityOfTwo.KEY_MESSAGE_DATA, c.getText());
						intent.putExtra(CityOfTwo.KEY_MESSAGE_FLAGS, c.getFlags());
						intent.putExtra(CityOfTwo.KEY_MESSAGE_TIME, c.getTime());

						mBroadcaster.sendBroadcast(intent);
					}
					break;
				}
				case CityOfTwo.KEY_LAST_SEEN: {
					int chatroomId = Integer.parseInt(data.get(CityOfTwo.KEY_CHATROOM_ID));

					if (chatroomId != oldChatroomId) return;

					sharedPreferences.edit()
							.putLong(CityOfTwo.KEY_LAST_SEEN, System.currentTimeMillis())
							.putLong(CityOfTwo.KEY_LAST_SEEN, Long.parseLong(data.get(CityOfTwo.KEY_LAST_SEEN)))
							.apply();

					mBroadcaster.sendBroadcast(new Intent(CityOfTwo.ACTION_LAST_SEEN));
					break;
				}
				case CityOfTwo.KEY_IS_TYPING: {
					int chatroomId = Integer.parseInt(data.get(CityOfTwo.KEY_CHATROOM_ID));

					if (chatroomId != oldChatroomId) return;

					sharedPreferences.edit()
							.putBoolean(CityOfTwo.KEY_IS_TYPING, Boolean.parseBoolean(data.get(CityOfTwo.KEY_IS_TYPING)))
							.apply();

					mBroadcaster.sendBroadcast(new Intent(CityOfTwo.ACTION_IS_TYPING));
					break;

				}
				case CityOfTwo.KEY_CHAT_END: {
					int chatroomId = Integer.parseInt(data.get(CityOfTwo.KEY_CHATROOM_ID));

					if (chatroomId != oldChatroomId) return;
					
					if (pendingMessages != null) pendingMessages.clear();

					nm.cancel(TAG_NOTIFICATION_NEW_MESSAGE, 10044);

					if (getApplicationState() == APPLICATION_BACKGROUND) {
						Intent notificationIntent = new Intent(this, LobbyActivity.class);

						PendingIntent p = PendingIntent.getActivity(
								this,
								(int) System.currentTimeMillis(),
								notificationIntent,
								PendingIntent.FLAG_UPDATE_CURRENT
						);

						Notification n = new NotificationCompat.Builder(this)
								.setContentTitle("Chat ended")
								.setContentText("Your last chat has ended. Tap here to start a new chat.")
								.setSmallIcon(R.drawable.ic_small)
								.setContentIntent(p)
								.setPriority(Notification.PRIORITY_HIGH)
								.setAutoCancel(true)
								.build();

						n.defaults |= Notification.DEFAULT_SOUND;
						n.defaults |= Notification.DEFAULT_VIBRATE;

						nm.notify(TAG_NOTIFICATION_CHAT_END, 10046, n);
					} else {
						mBroadcaster.sendBroadcast(new Intent(CityOfTwo.ACTION_END_CHAT));
					}

					sharedPreferences.edit()
							.remove(CityOfTwo.KEY_LAST_CHATROOM)
							.apply();

					break;
				}
				case CityOfTwo.KEY_USER_OFFLINE: {
//					if (data.containsKey("POOL_ID")) return;
					if (pendingMessages != null) pendingMessages.clear();

					mBroadcaster.sendBroadcast(new Intent(CityOfTwo.ACTION_USER_OFFLINE));

					nm.cancel(TAG_NOTIFICATION_NEW_MESSAGE, 0);
					nm.cancel(TAG_NOTIFICATION_CHAT_BEGIN, 10045);

					sharedPreferences.edit()
							.putBoolean(CityOfTwo.KEY_SESSION_ACTIVE, false)
							.apply();

					break;
				}
				case CityOfTwo.KEY_CHAT_REQUEST: {
					if (pendingMessages != null) pendingMessages.clear();
					int requestId = Integer.parseInt(data.get(CityOfTwo.KEY_REQUEST_ID));
					Contact guest = new Contact(data.get(CityOfTwo.KEY_GUEST));

					sharedPreferences.edit().putInt(CityOfTwo.KEY_LAST_REQUEST, requestId)
							.putInt(CityOfTwo.KEY_LAST_GUEST, guest.id)
							.putLong(CityOfTwo.KEY_RESQUEST_DISPATCH, System.currentTimeMillis())
							.remove(CityOfTwo.KEY_LAST_CHATROOM)
							.apply();

					db.saveGuest(guest);

					intent = new Intent(CityOfTwo.ACTION_REQUEST_CHAT);
					mBroadcaster.sendBroadcast(intent);
					break;
				}
				default:
					Log.i(TAG, "Unsupported message type " + messageType);
					handled = false;
					break;
			}
			if (handled) Log.i(TAG, messageType + " handled");
		} catch (Exception e) {
			Log.e(TAG, e.toString(), e);
		}
	}
}
