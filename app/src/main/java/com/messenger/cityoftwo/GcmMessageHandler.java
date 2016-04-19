package com.messenger.cityoftwo;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.android.gms.gcm.GoogleCloudMessaging;

import java.util.ArrayList;

import static com.messenger.cityoftwo.CityOfTwo.BackgroundConversation;

public class GcmMessageHandler extends IntentService {

    private static Integer mBeginChatSignalCounter = 0,
            mEndChatSignalCounter = 0,
            mReceiveMessageSignalCounter = 0;
    private LocalBroadcastManager mBroadcaster;

    public GcmMessageHandler() {
        super("GcmMessageHandler");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mBroadcaster = LocalBroadcastManager.getInstance(this);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Bundle extras = intent.getExtras();

        GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(this);

//        // The getMessageType() intent parameter must be the intent you received
//        // in your BroadcastReceiver.
//        String messageType = gcm.getMessageType(intent);

        // Do something with the received message
        try {
            String messageType = extras.getString("TYPE");

            NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

            switch (messageType) {
                case "MESSAGE":
                    if (CityOfTwo.APPLICATION_STATE == CityOfTwo.APPLICATION_FOREGROUND) {
                        intent.setAction(CityOfTwo.PACKAGE_NAME);
                        mBroadcaster.sendBroadcast(intent);
                        Log.i("Chat", "Received message: " + extras.getString("TEXT"));
                        Log.i("Chat", "Receving message" + ++mReceiveMessageSignalCounter);
                    } else {

                        if (BackgroundConversation == null)
                            BackgroundConversation = new ArrayList<>();

                        Intent notificationIntent = new Intent(this, ConversationActivity.class);

                        BackgroundConversation.add(new Conversation(
                                extras.getString("TEXT"),
                                CityOfTwo.RECEIVED
                        ));

                        PendingIntent p = PendingIntent.getActivity(
                                this,
                                (int) System.currentTimeMillis(),
                                notificationIntent,
                                PendingIntent.FLAG_UPDATE_CURRENT
                        );

                        Notification n = new Notification.Builder(this)
                                .setContentTitle("You have a message")
                                .setContentText(extras.getString("TEXT"))
                                .setSmallIcon(R.drawable.ic_small)
                                .setContentIntent(p)
                                .setPriority(Notification.PRIORITY_HIGH)
                                .setAutoCancel(true)
                                .build();

                        try {
                            Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                            Ringtone r = RingtoneManager.getRingtone(getApplicationContext(), notification);
                            r.play();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                        nm.notify("CHAT_NOTIFICATION", 0, n);
                    }
                    break;
                case "CHAT_BEGIN":
                    intent = new Intent();
                    intent.setAction(CityOfTwo.PACKAGE_NAME);
                    intent.putExtra("TYPE", messageType);

                    Log.i("Chat", "Starting chat with stranger");
                    Log.i("Chat", "Beginning chat " + ++mBeginChatSignalCounter);
                    mBroadcaster.sendBroadcast(intent);
                    break;
                case "CHAT_END":
                    Log.i("Chat", "Ending chat " + ++mEndChatSignalCounter);
                    break;
                default:
                    Log.i("GCM", "GCM Message received" + extras);
                    break;
            }
        } catch (Exception e) {
            Log.i("GCM", "GCM Message received" + extras);
            Log.e("GCM Exception", e.toString());
        }

        GCMBroadcastReceiver.completeWakefulIntent(intent);
    }
}