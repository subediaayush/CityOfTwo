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

import static com.messenger.cityoftwo.CityOfTwo.APPLICATION_BACKGROUND;
import static com.messenger.cityoftwo.CityOfTwo.getApplicationState;
import static com.messenger.cityoftwo.CityOfTwo.getCurrentActivity;
import static com.messenger.cityoftwo.CityOfTwo.messageCounter;
import static com.messenger.cityoftwo.CityOfTwo.pendingMessages;

/**
 * Created by Aayush on 7/16/2016.
 */
public class FirebaseMessageHandler extends com.google.firebase.messaging.FirebaseMessagingService {
	private LocalBroadcastManager mBroadcaster;

	@Override
	public void onMessageReceived(RemoteMessage remoteMessage) {
		Map<String, String> data = remoteMessage.getData();
		if (mBroadcaster == null) mBroadcaster = LocalBroadcastManager.getInstance(this);

		try {
			Log.i("GCM", "GCM Message received " + data);

			final SharedPreferences sharedPreferences = getSharedPreferences(CityOfTwo.PACKAGE_NAME, MODE_PRIVATE);

			String messageType = data.get(CityOfTwo.KEY_TYPE);
			NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
			Intent intent = new Intent();
			int oldChatroomId = sharedPreferences.getInt(CityOfTwo.KEY_CHATROOM_ID, -1);

			DatabaseHelper db = new DatabaseHelper(this);

			int currentActivity = getCurrentActivity(), applicationState = getApplicationState();

			switch (messageType) {
				case CityOfTwo.KEY_MESSAGE: {
					int chatroomId = Integer.parseInt(data.get(CityOfTwo.KEY_CHATROOM_ID));
					String text = data.get(CityOfTwo.KEY_DATA);
					Integer flags = Integer.parseInt(data.get(CityOfTwo.KEY_MESSAGE_FLAGS));
					long time = System.currentTimeMillis();
//					long time = Long.parseLong(data.get(CityOfTwo.KEY_TIME));

					Conversation c = new Conversation(text, flags, time);
					c.removeFlag(CityOfTwo.FLAG_SENT);
					c.addFlag(CityOfTwo.FLAG_RECEIVED);

					// Current activity is not Conversation Activity
					// Current activity is Conversation Activty but is in background
					if ((currentActivity == CityOfTwo.ACTIVITY_CONVERSATION &&
							applicationState == CityOfTwo.APPLICATION_BACKGROUND) ||
							currentActivity != CityOfTwo.ACTIVITY_CONVERSATION) {

						db.insertMessage(chatroomId, c);

						// If not current conversation then let BEGIN_CHAT handle it
						if (chatroomId != oldChatroomId) return;

						if ((flags & CityOfTwo.FLAG_PROFILE) == CityOfTwo.FLAG_PROFILE)
							text = "Stranger shared their profile with you";

						Intent notificationIntent = new Intent(this, ConversationActivity.class);

						PendingIntent p = PendingIntent.getActivity(
								this,
								(int) System.currentTimeMillis(),
								notificationIntent,
								PendingIntent.FLAG_UPDATE_CURRENT
						);

						if (messageCounter == null) messageCounter = 0;
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
								.setNumber(++messageCounter)
								.setSmallIcon(R.drawable.ic_small)
								.setContentIntent(p)
								.setPriority(Notification.PRIORITY_HIGH)
								.setAutoCancel(true)
								.build();

						Log.i("Message Counter", String.valueOf(messageCounter));

						n.defaults |= Notification.DEFAULT_SOUND;
						n.defaults |= Notification.DEFAULT_VIBRATE;

						nm.notify(CityOfTwo.NOTIFICATION_NEW_MESSAGE, 10044, n);
					} else {
						// If not current conversation then let BEGIN_CHAT handle it
						if (chatroomId != oldChatroomId) return;

						messageCounter = 0;
						intent.setAction(CityOfTwo.ACTION_NEW_MESSAGE);

						intent.putExtra(CityOfTwo.KEY_TYPE, messageType);
						intent.putExtra(CityOfTwo.KEY_DATA, text);
						intent.putExtra(CityOfTwo.KEY_MESSAGE_FLAGS, flags);
						intent.putExtra(CityOfTwo.KEY_TIME, time);

						mBroadcaster.sendBroadcast(intent);
					}
					Log.i("GCM", messageType + " handled");
					break;
				}
				case CityOfTwo.KEY_CHAT_BEGIN: {
					int chatroomId = Integer.parseInt(data.get(CityOfTwo.KEY_CHATROOM_ID));

					if (chatroomId == oldChatroomId) break;

					String commonLikes = data.get(CityOfTwo.KEY_COMMON_LIKES);
					messageCounter = 0;
					if (pendingMessages != null) pendingMessages.clear();

					nm.cancel(CityOfTwo.NOTIFICATION_NEW_MESSAGE, 10044);

					sharedPreferences.edit()
							.putBoolean(CityOfTwo.KEY_CHAT_PENDING, true)
							.putString(CityOfTwo.KEY_COMMON_LIKES, commonLikes)
							.putInt(CityOfTwo.KEY_CHATROOM_ID, chatroomId)
							.apply();

					if (applicationState == CityOfTwo.APPLICATION_BACKGROUND) {
						Intent notificationIntent = new Intent(this, ConversationActivity.class);

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

						nm.notify(CityOfTwo.NOTIFICATION_NEW_CHAT, 10045, n);
					} else {
						intent = new Intent(CityOfTwo.ACTION_BEGIN_CHAT);
						mBroadcaster.sendBroadcast(intent);
					}
					Log.i("GCM", messageType + " handled");
					break;
				}
				case CityOfTwo.KEY_LAST_SEEN: {
					int chatroomId = Integer.parseInt(data.get(CityOfTwo.KEY_CHATROOM_ID));

					if (chatroomId != oldChatroomId) return;

					sharedPreferences.edit()
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

					messageCounter = 0;
					if (pendingMessages != null) pendingMessages.clear();

					nm.cancel(CityOfTwo.NOTIFICATION_NEW_MESSAGE, 10044);

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

						nm.notify(CityOfTwo.NOTIFICATION_CHAT_END, 10046, n);
					} else {
						mBroadcaster.sendBroadcast(new Intent(CityOfTwo.ACTION_END_CHAT));
					}

					sharedPreferences.edit()
							.remove(CityOfTwo.KEY_CHAT_PENDING)
							.remove(CityOfTwo.KEY_CHATROOM_ID)
							.apply();

					Log.i("GCM", messageType + " handled");
					break;
				}
				case CityOfTwo.KEY_USER_OFFLINE: {
					messageCounter = 0;

//					if (data.containsKey("POOL_ID")) return;
					if (pendingMessages != null) pendingMessages.clear();

					mBroadcaster.sendBroadcast(new Intent(CityOfTwo.ACTION_USER_OFFLINE));

					nm.cancel(CityOfTwo.NOTIFICATION_NEW_MESSAGE, 0);
					nm.cancel(CityOfTwo.NOTIFICATION_NEW_CHAT, 10045);

					sharedPreferences.edit()
							.putBoolean(CityOfTwo.KEY_SESSION_ACTIVE, false)
							.apply();

					Log.i("GCM", messageType + " handled");
					break;
				}
				default:
					break;
			}
		} catch (Exception e) {
			Log.e("GCM Exception", e.toString(), e);
		}
	}
}
