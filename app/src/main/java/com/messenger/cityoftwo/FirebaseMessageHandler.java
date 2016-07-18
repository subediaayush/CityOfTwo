package com.messenger.cityoftwo;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
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
import static com.messenger.cityoftwo.CityOfTwo.mBackgroundConversation;

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
            Log.i("GCM", "GCM Message received" + data);

            String messageType = (String) data.get(CityOfTwo.KEY_TYPE);
            NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            Intent intent = new Intent();
            Integer chatroomId = Integer.parseInt(data.get(CityOfTwo.KEY_CHATROOM_ID)),
                    oldChatroomId = getSharedPreferences(CityOfTwo.PACKAGE_NAME, MODE_PRIVATE)
                            .getInt(CityOfTwo.KEY_CHATROOM_ID, -1);

            switch (messageType) {
                case KEY_MESSAGE: {
                    String text = (String) data.get(CityOfTwo.KEY_TEXT);
                    Integer flags = Integer.parseInt(data.get(CityOfTwo.KEY_MESSAGE_FLAGS));
                    Long time = Long.parseLong(data.get(CityOfTwo.KEY_TIME));

                    if (!chatroomId.equals(oldChatroomId)) {
                        mBackgroundConversation.clear();
                        return;
                    }

                    if (getCurrentActivity() != CityOfTwo.ACTIVITY_CONVERSATION ||
                            (getCurrentActivity() == CityOfTwo.ACTIVITY_CONVERSATION &&
                                    getApplicationState() == CityOfTwo.APPLICATION_BACKGROUND)) {

                        if (mBackgroundConversation == null)
                            mBackgroundConversation = new ArrayList<>();


                        Intent notificationIntent = new Intent(this, ConversationActivity.class);

                        mBackgroundConversation.add(new Conversation(
                                text,
                                flags,
                                new Date(time)
                        ));

                        PendingIntent p = PendingIntent.getActivity(
                                this,
                                (int) System.currentTimeMillis(),
                                notificationIntent,
                                PendingIntent.FLAG_UPDATE_CURRENT
                        );

                        Notification n = new Notification.Builder(this)
                                .setContentTitle("You have a message")
                                .setContentText(text)
                                .setSmallIcon(R.drawable.ic_small)
                                .setContentIntent(p)
                                .setPriority(Notification.PRIORITY_HIGH)
                                .setAutoCancel(true)
                                .build();

                        n.defaults |= Notification.DEFAULT_SOUND;
                        n.defaults |= Notification.DEFAULT_VIBRATE;

                        nm.notify("CHAT_NOTIFICATION", 0, n);
                    } else {
                        intent.setAction(CityOfTwo.PACKAGE_NAME);

                        intent.putExtra(CityOfTwo.KEY_TYPE, messageType);
                        intent.putExtra(CityOfTwo.KEY_TEXT, text);
                        intent.putExtra(CityOfTwo.KEY_MESSAGE_FLAGS, flags);
                        intent.putExtra(CityOfTwo.KEY_TIME, time);
                        intent.putExtra(CityOfTwo.KEY_CHATROOM_ID, chatroomId);

                        mBroadcaster.sendBroadcast(intent);
                    }
                    break;
                }
                case CityOfTwo.KEY_CHAT_BEGIN: {
                    String commonLikes = data.get(CityOfTwo.KEY_COMMON_LIKES);
                    if ((getCurrentActivity() == CityOfTwo.ACTIVITY_LOBBY ||
                            getCurrentActivity() == CityOfTwo.ACTIVITY_CONVERSATION) &&
                            (getApplicationState() == APPLICATION_FOREGROUND)) {

                        intent = new Intent();
                        intent.setAction(CityOfTwo.PACKAGE_NAME);
                        intent.putExtra(CityOfTwo.KEY_TYPE, messageType);
                        intent.putExtra(CityOfTwo.KEY_COMMON_LIKES, commonLikes);


                        getSharedPreferences(CityOfTwo.PACKAGE_NAME, MODE_PRIVATE)
                                .edit()
                                .putBoolean(CityOfTwo.KEY_CHAT_PENDING, true)
                                .putString(CityOfTwo.KEY_COMMON_LIKES, commonLikes)
                                .putString(CityOfTwo.KEY_TYPE, messageType)
                                .putInt(CityOfTwo.KEY_CHATROOM_ID, chatroomId)
                                .apply();

                        if (chatroomId != oldChatroomId) {
                            if (mBackgroundConversation == null)
                                mBackgroundConversation = new ArrayList<>();
                            mBackgroundConversation.clear();
                        }

                        mBroadcaster.sendBroadcast(intent);
                    } else {
                        getSharedPreferences(CityOfTwo.PACKAGE_NAME, MODE_PRIVATE)
                                .edit()
                                .putBoolean(CityOfTwo.KEY_CHAT_PENDING, true)
                                .putString(CityOfTwo.KEY_COMMON_LIKES, commonLikes)
                                .putInt(CityOfTwo.KEY_CHATROOM_ID, chatroomId)
                                .apply();
                    }
                }
                break;
                case CityOfTwo.KEY_CHAT_END:
                    if (getApplicationState() == APPLICATION_FOREGROUND) {
                        mBroadcaster.sendBroadcast(new Intent());
                    }
                    getSharedPreferences(CityOfTwo.PACKAGE_NAME, MODE_PRIVATE)
                            .edit()
                            .remove(CityOfTwo.KEY_CHAT_PENDING)
                            .remove(CityOfTwo.KEY_CHATROOM_ID)
                            .remove(CityOfTwo.KEY_COMMON_LIKES)
                            .apply();
                    break;
                default:
                    Log.i("GCM", "GCM Message received" + data);
                    break;
            }
        } catch (Exception e) {
            Log.i("GCM", "GCM Message received" + data);
            Log.e("GCM Exception", e.toString(), e);
        }

        Log.i("FCM Message Received", remoteMessage.getData().toString());


    }
}
