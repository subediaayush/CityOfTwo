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
import java.util.Date;
import java.util.Map;

import static com.messenger.cityoftwo.CityOfTwo.APPLICATION_FOREGROUND;
import static com.messenger.cityoftwo.CityOfTwo.KEY_MESSAGE;
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
            Integer chatroomId = Integer.parseInt(data.get(CityOfTwo.KEY_CHATROOM_ID)),
                    oldChatroomId = sharedPreferences.getInt(CityOfTwo.KEY_CHATROOM_ID, -1);

            DatabaseHelper db = new DatabaseHelper(this);

            switch (messageType) {
                case KEY_MESSAGE: {
                    String text = data.get(CityOfTwo.KEY_TEXT);
                    Integer flags = Integer.parseInt(data.get(CityOfTwo.KEY_MESSAGE_FLAGS));
                    Date time = new Date(Long.parseLong(data.get(CityOfTwo.KEY_TIME)));

                    // Current activity is not Conversation Activity
                    // Current activity is Conversation Activty but is in background
                    int currentActivity = getCurrentActivity(),
                            applicationState = getApplicationState();
                    if ((currentActivity == CityOfTwo.ACTIVITY_CONVERSATION &&
                            applicationState == CityOfTwo.APPLICATION_BACKGROUND) ||
                            currentActivity != CityOfTwo.ACTIVITY_CONVERSATION) {

                        Conversation c = new Conversation(text, flags, time);
                        c.removeFlag(CityOfTwo.FLAG_SENT);
                        c.addFlag(CityOfTwo.FLAG_RECEIVED);

                        db.insertMessage(chatroomId, c);

                        // If not current conversation then let BEGIN_CHAT handle it
                        if (!chatroomId.equals(oldChatroomId)) return;

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

                        nm.notify(CityOfTwo.NOTIFICATION_NEW_MESSAGE, 0, n);
                    } else {
                        // If not current conversation then let BEGIN_CHAT handle it
                        if (!chatroomId.equals(oldChatroomId)) return;

                        messageCounter = 0;
                        intent.setAction(CityOfTwo.ACTION_NEW_MESSAGE);

                        intent.putExtra(CityOfTwo.KEY_TYPE, messageType);
                        intent.putExtra(CityOfTwo.KEY_TEXT, text);
                        intent.putExtra(CityOfTwo.KEY_MESSAGE_FLAGS, flags);
                        intent.putExtra(CityOfTwo.KEY_TIME, time.getTime());

                        mBroadcaster.sendBroadcast(intent);
                    }
                    Log.i("GCM", messageType + " handled");
                    break;
                }
                case CityOfTwo.KEY_CHAT_BEGIN: {
                    if (chatroomId == oldChatroomId) break;
                    String commonLikes = data.get(CityOfTwo.KEY_COMMON_LIKES);
                    messageCounter = 0;
                    if (pendingMessages != null) pendingMessages.clear();

                    nm.cancel(CityOfTwo.NOTIFICATION_NEW_MESSAGE, 0);

                    sharedPreferences.edit()
                            .putBoolean(CityOfTwo.KEY_CHAT_PENDING, true)
                            .putString(CityOfTwo.KEY_COMMON_LIKES, commonLikes)
                            .putInt(CityOfTwo.KEY_CHATROOM_ID, chatroomId)
                            .apply();

                    if (getApplicationState() == APPLICATION_FOREGROUND) {
                        intent = new Intent(CityOfTwo.ACTION_BEGIN_CHAT);
                        mBroadcaster.sendBroadcast(intent);
                    }
                    Log.i("GCM", messageType + " handled");
                    break;
                }
                case CityOfTwo.KEY_CHAT_END: {
                    messageCounter = 0;
                    if (pendingMessages != null) pendingMessages.clear();

                    if (getApplicationState() == APPLICATION_FOREGROUND) {
                        mBroadcaster.sendBroadcast(new Intent(CityOfTwo.ACTION_END_CHAT));
                    }

                    nm.cancel(CityOfTwo.NOTIFICATION_NEW_MESSAGE, 0);

                    sharedPreferences.edit()
                            .remove(CityOfTwo.KEY_CHAT_PENDING)
                            .remove(CityOfTwo.KEY_CHATROOM_ID)
                            .apply();

                    Log.i("GCM", messageType + " handled");
                    break;
                }
                case CityOfTwo.KEY_USER_OFFLINE: {
                    messageCounter = 0;
                    if (pendingMessages != null) pendingMessages.clear();

                    if (getApplicationState() == APPLICATION_FOREGROUND) {
                        mBroadcaster.sendBroadcast(new Intent(CityOfTwo.ACTION_USER_OFFLINE));
                    }

                    nm.cancel(CityOfTwo.NOTIFICATION_NEW_MESSAGE, 0);

                    sharedPreferences.edit()
                            .putBoolean(CityOfTwo.KEY_USER_OFFLINE, true)
                            .remove(CityOfTwo.KEY_CHAT_PENDING)
                            .remove(CityOfTwo.KEY_CHATROOM_ID)
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
