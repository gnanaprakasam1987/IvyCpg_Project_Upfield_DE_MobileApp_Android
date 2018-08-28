package com.ivy.cpg.fcmnotification;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.database.DatabaseUtils;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.v4.app.NotificationCompat;
import android.util.Log;

import com.firebase.jobdispatcher.JobParameters;
import com.firebase.jobdispatcher.JobService;
import com.google.firebase.messaging.RemoteMessage;
import com.ivy.cpg.view.login.LoginScreen;
import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.util.Commons;

import org.json.JSONArray;
import org.json.JSONObject;


@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class NotificationJobService extends JobService {

    private static final String TAG = "MyJobService";
    private int notifyId;

    @Override
    public boolean onStartJob(JobParameters jobParameters) {
        Log.d(TAG, "Performing long running task in scheduled job");

        BusinessModel bmodel = (BusinessModel) getApplicationContext();

        // Post notification of received message.
        try {

            Bundle extras = jobParameters.getExtras();
            RemoteMessage remoteMessage = null;
            String str = "", jsonType, jsonMsg;
            if (extras != null) {
                remoteMessage = extras.getParcelable("DataPayLoad");

                Commons.print(TAG + ",Received: " + extras.toString());
            }

            if (remoteMessage == null)
                return false;

            JSONArray array = new JSONArray(remoteMessage.getData().get("body"));
            JSONObject jsonObject = new org.json.JSONObject(array
                    .get(0).toString());
            jsonType = jsonObject.getString("Type");

            if (jsonType.equals("STKALLOC")) {
                notifyId = 1;
                sendNotification(getResources().getString(R.string.stock_allocated));
            } else if (jsonType.equals("JPC")) {
                notifyId = 2;
                sendNotification(getResources().getString(R.string.visit_plan_updated));
            } else if (jsonType.equals("VST_PLN_REQ_STATUS")) {
                notifyId = 3;
                jsonMsg = jsonObject.getString("Message");
                sendNotification(jsonMsg);
            } else if (jsonType.equals("VST_PLN_REQ_REMINDER")) {
                notifyId = 4;
                jsonMsg = jsonObject.getString("Message");
                sendNotification(jsonMsg);
            }else if (jsonType.equalsIgnoreCase("MVP BADGE")) {
                notifyId = 5;
                jsonMsg = jsonObject.getString("Message");
                sendNotification(jsonMsg);
                bmodel.saveNotification(jsonMsg, DatabaseUtils.sqlEscapeString(jsonObject.getString("icon")), "MVP BADGE");
            } else if (jsonType.equalsIgnoreCase("MESSAGE_NOTIFY")) {
                notifyId = 6;
                jsonMsg = jsonObject.getString("Message");
                sendNotification(jsonMsg);
                bmodel.saveNotification(jsonMsg, DatabaseUtils.sqlEscapeString(jsonObject.getString("icon")), "MESSAGE_NOTIFY");
            } else if (jsonType.equals("NEWTSK")) {
                bmodel.parseJSONAndInsert(jsonObject.getJSONObject("Data"));
            }
        } catch (Exception e) {
            Commons.printException("" + e);
        }


        return false;
    }

    private void sendNotification(RemoteMessage remoteMessage) {
        Intent intent = new Intent(this, LoginScreen.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0 /* Request code */, intent,
                PendingIntent.FLAG_ONE_SHOT);

        String channelId = "ChannelId";
        Uri defaultSoundUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(this, channelId)
                        .setSmallIcon(R.drawable.launchericon)
                        .setContentTitle(
                                remoteMessage.getData().get("title") != null ? remoteMessage.getData().get("title") : "Notify")
                        .setContentText(
                                remoteMessage.getData().get("body") != null ? remoteMessage.getData().get("body") : "Success")
                        .setAutoCancel(true)
                        .setSound(defaultSoundUri)
                        .setContentIntent(pendingIntent);

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        if (notificationManager != null) {
            // Since android Oreo notification channel is needed.
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                NotificationChannel channel = new NotificationChannel(channelId,
                        "Channel human readable title",
                        NotificationManager.IMPORTANCE_DEFAULT);
                notificationManager.createNotificationChannel(channel);
            }

            notificationManager.notify(notifyId /* ID of notification */, notificationBuilder.build());
        }
    }

    private void sendNotification(String message) {
        Intent intent = new Intent(this, LoginScreen.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0 /* Request code */, intent,
                PendingIntent.FLAG_ONE_SHOT);

        String channelId = "channelId";
        Uri defaultSoundUri= RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(this, channelId)
                        .setSmallIcon(R.drawable.launchericon)
                        .setContentTitle(getResources().getString(R.string.app_name))
                        .setContentText(message)
                        .setAutoCancel(true)
                        .setSound(defaultSoundUri)
                        .setContentIntent(pendingIntent);

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        if (notificationManager != null) {

            // Since android Oreo notification channel is needed.
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                NotificationChannel channel = new NotificationChannel(channelId,
                        "Channel human readable title",
                        NotificationManager.IMPORTANCE_DEFAULT);
                notificationManager.createNotificationChannel(channel);
            }

            notificationManager.notify(notifyId /* ID of notification */, notificationBuilder.build());
        }
    }

    @Override
    public boolean onStopJob(JobParameters jobParameters) {
        return false;
    }

}
