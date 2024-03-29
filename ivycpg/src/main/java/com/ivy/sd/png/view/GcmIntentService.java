package com.ivy.sd.png.view;

/*
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


import android.app.IntentService;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;

import androidx.core.app.NotificationCompat;

//import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.util.Commons;

/**
 * This {@code IntentService} does the actual handling of the GCM message.
 * {@code GcmBroadcastReceiver} (a {@code WakefulBroadcastReceiver}) holds a
 * partial wake lock for this service while the service does its work. When the
 * service is finished, it calls {@code completeWakefulIntent()} to release the
 * wake lock.
 */
public class GcmIntentService extends IntentService {
    public static final int NOTIFICATION_ID = 1;
    NotificationManager mNotificationManager;
    NotificationCompat.Builder builder;
    BusinessModel bmodel;

    public GcmIntentService() {
        super("GcmIntentService");
    }

    public static final String TAG = "GCM Demo";

    @Override
    protected void onHandleIntent(Intent intent) {
//        bmodel = (BusinessModel) getApplicationContext();
//        Bundle extras = intent.getExtras();
//        GoogleCloudMessaging gcm = GoogleCloudMessaging.getInstance(this);
//        // The getMessageType() intent parameter must be the intent you received
//        // in your BroadcastReceiver.
//        String messageType = gcm.getMessageType(intent);
//        Commons.print("gcm received" + extras.isEmpty() + " type" + messageType);
//        if (!extras.isEmpty()) { // has effect of unparcelling Bundle
//            /*
//			 * Filter messages based on message type. Since it is likely that
//			 * GCM will be extended in the future with new message types, just
//			 * ignore any message types you're not interested in, or that you
//			 * don't recognize.
//			 */
//            if (GoogleCloudMessaging.MESSAGE_TYPE_SEND_ERROR
//                    .equals(messageType)) {
//                sendNotification("Send error: " + extras.toString());
//            } else if (GoogleCloudMessaging.MESSAGE_TYPE_DELETED
//                    .equals(messageType)) {
//                sendNotification("Deleted messages on server: "
//                        + extras.toString());
//                // If it's a regular GCM message, do some work.
//            } else if (GoogleCloudMessaging.MESSAGE_TYPE_MESSAGE
//                    .equals(messageType)) {
//                // This loop represents the service doing some work.
//                for (int i = 0; i < 5; i++) {
//                    Commons.print(TAG +
//                            ",Working... " + (i + 1) + "/5 @ "
//                            + SystemClock.elapsedRealtime());
//                    try {
//                        Thread.sleep(5000);
//                    } catch (InterruptedException e) {
//                        Commons.printException(e);
//                    }
//                }
//                Commons.print(TAG + ",Completed work @ " + SystemClock.elapsedRealtime());
//
//                // Post notification of received message.
//                try {
//                    Commons.print(TAG + ",Received: " + extras.toString());
//                    String str, jsonType,jsonMsg;
//                    str = extras.getString("message");
//
//                    JSONArray array = new JSONArray(str);
//                    JSONObject jsonObject = new org.json.JSONObject(array
//                            .get(0).toString());
//                    jsonType = jsonObject.getString("Type");
//                    jsonMsg=jsonObject.getString("Message");
//
//                    if (jsonType.equals("STKALLOC")) {
//                        sendNotification(getResources().getString(
//                                R.string.stock_allocated));
//                    } else if (jsonType.equals("JPC")) {
//                        sendNotification(getResources().getString(
//                                R.string.visit_plan_updated));
//                    } else if (jsonType.equals("VST_PLN_REQ_STATUS")) {
//                        sendNotification(jsonMsg);
//                    } else if (jsonType.equals("VST_PLN_REQ_REMINDER")) {
//                        sendNotification(jsonMsg);
//                    } else if (jsonType.equalsIgnoreCase("MVP BADGE")) {
//                        sendNotification(jsonMsg);
//                        bmodel.saveNotification(jsonMsg, DatabaseUtils.sqlEscapeString(jsonObject.getString("icon")),"MVP BADGE");
//                    } else if (jsonType.equalsIgnoreCase("MESSAGE_NOTIFY")) {
//                        sendNotification(jsonMsg);
//                        bmodel.saveNotification(jsonMsg, DatabaseUtils.sqlEscapeString(jsonObject.getString("icon")),"MESSAGE_NOTIFY");
//                    } else if (jsonType.equals("NEWTSK")) {
//                        bmodel.parseJSONAndInsert(jsonObject
//                                .getJSONObject("Data"));
//                    }
//                } catch (Exception e) {
//                    Commons.printException("" + e);
//                }
//
//            }
//        }
//        // Release the wake lock provided by the WakefulBroadcastReceiver.
//        GcmBroadcastReceiver.completeWakefulIntent(intent);
    }

    // Put the message into a notification and post it.
    // This is just one simple example of what you might choose to do with
    // a GCM message.
    private void sendNotification(String msg) {
        mNotificationManager = (NotificationManager) this
                .getSystemService(Context.NOTIFICATION_SERVICE);

//		PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
//				new Intent(this, HomeScreenActivity.class), 0);

        NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(
                this).setSmallIcon(R.drawable.gcm_cloud)
                .setContentTitle("GCM Notification")
                .setTicker(msg)
                .setStyle(new NotificationCompat.BigTextStyle().bigText(msg))

                .setContentText(msg);
        Commons.print("msg" + msg);
//		mBuilder.setContentIntent(contentIntent);

        mNotificationManager.notify(NOTIFICATION_ID, mBuilder.build());
    }

}