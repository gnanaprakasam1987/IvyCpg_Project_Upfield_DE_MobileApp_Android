package com.ivy.cpg.locationservice.realtime;

import android.Manifest;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.LocalBroadcastManager;

import com.google.android.gms.location.ActivityRecognitionClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.ivy.cpg.locationservice.LocationDetailBO;
import com.ivy.cpg.locationservice.LocationServiceHelper;
import com.ivy.cpg.locationservice.activitytracking.ActivityIntentService;
import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.util.Commons;

import static com.ivy.cpg.locationservice.LocationConstants.REALTIME_NOTIFICATION_ID;

public class RealTimeLocationService extends Service {

    private FusedLocationProviderClient client;
    private LocationCallback locationCallback;
    private Location previousBestLocation = null;
    private RealTimeLocation realTimeLocation;
    private ActivityBroadcastReceiver activityBroadcastReceiver = new ActivityBroadcastReceiver();
    private final String BROADCAST_DETECTED_ACTIVITY = "com.ivy.BROADCAST_DETECTED_ACTIVITY";
    private String activityName = "";
    private PendingIntent mPendingIntent;
    private ActivityRecognitionClient mActivityRecognitionClient;

    @Override
    public IBinder onBind(Intent intent) {return null;}

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        if(intent.getExtras() != null) {
            Bundle bundle = intent.getExtras();
            realTimeLocation = (RealTimeLocation) bundle.getSerializable("REALTIME");

            buildNotification();
            requestLocationUpdates();
            LocalBroadcastManager.getInstance(this).registerReceiver(activityBroadcastReceiver,
                    new IntentFilter(BROADCAST_DETECTED_ACTIVITY));

            mActivityRecognitionClient = new ActivityRecognitionClient(this);
            Intent mIntentService = new Intent(this, ActivityIntentService.class);
            mPendingIntent = PendingIntent.getService(this, 10, mIntentService, PendingIntent.FLAG_UPDATE_CURRENT);
            requestActivityUpdatesButtonHandler();
        }

        return START_STICKY;
    }

//     Create the persistent notification
    private void buildNotification() {

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this,"")
                .setContentTitle(getString(R.string.app_name))
                .setContentText(getString(R.string.notification_text))
                .setOngoing(true)
                .setSmallIcon(R.drawable.ic_location_tracking);
        startForeground(REALTIME_NOTIFICATION_ID, builder.build());
    }

    private void requestLocationUpdates() {
        LocationRequest request = new LocationRequest();
        request.setInterval(5000);
        request.setFastestInterval(2500);
        request.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        client = LocationServices.getFusedLocationProviderClient(this);
        createLocationCallBack();
        int permission = ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION);
        if (permission == PackageManager.PERMISSION_GRANTED) {
            // Request location updates and when an update is
            // received, update the location in View
            client.requestLocationUpdates(request,locationCallback , null);
        }
    }

    private void createLocationCallBack(){
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {

                //Notifies if GPS is Disabled
                boolean isGpsEnabled = LocationServiceHelper.getInstance().notifyGPSStatus(getApplicationContext());
                //Notifies if Mock Location is enabled
                boolean isMockLocationEnabled = LocationServiceHelper.getInstance().notifyMockLocationStatus(getApplicationContext());

                Location location = locationResult.getLastLocation();
                if (location != null) {
                    if(isBetterLocation(location,previousBestLocation)) {
                        LocationDetailBO locationDetailBO = new LocationDetailBO();
                        locationDetailBO.setLatitude(String.valueOf(location.getLatitude()));
                        locationDetailBO.setLongitude(String.valueOf(location.getLongitude()));
                        locationDetailBO.setAccuracy(String.valueOf(location.getAccuracy()));
                        locationDetailBO.setTime(String.valueOf(System.currentTimeMillis()));
                        locationDetailBO.setActivityType(activityName);
                        locationDetailBO.setGpsEnabled(isGpsEnabled);
                        locationDetailBO.setMockLocationEnabled(isMockLocationEnabled);
                        locationDetailBO.setBatteryStatus(LocationServiceHelper.getInstance().getBatteryPercentage(getApplicationContext()));

                        Commons.print("Service LocationDetailBO -- "+locationDetailBO);

                        realTimeLocation.onRealTimeLocationReceived(locationDetailBO,getApplicationContext());
                    }
                }
            }
        };
    }

    private void stopLocationUpdates(){
        if(client!=null && locationCallback !=null){
            client.removeLocationUpdates(locationCallback);
        }
    }

    private boolean isBetterLocation(Location location, Location currentBestLocation) {
        if (currentBestLocation == null) {
            // A new location is always better than no location
            previousBestLocation = location;
            return true;
        }

        boolean isBetterLocation = false;

        // Check whether the new location fix is newer or older
        long timeDelta = location.getTime() - currentBestLocation.getTime();
        boolean isNewer = timeDelta > 0;

        // Check whether the new location fix is more or less accurate
        int accuracyDelta = (int) (location.getAccuracy() - currentBestLocation.getAccuracy());
        boolean isLessAccurate = accuracyDelta > 0;
        boolean isMoreAccurate = accuracyDelta < 0;

        // Determine location quality using a combination of timeliness and accuracy
        if (isMoreAccurate) {
            isBetterLocation =  true;
            previousBestLocation = location;
        }
        else if (isNewer && !isLessAccurate) {
            isBetterLocation = true;
            previousBestLocation = location;
        }

        return isBetterLocation;
    }


    class ActivityBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction() != null && intent.getAction().equals(BROADCAST_DETECTED_ACTIVITY)) {
                activityName = intent.getStringExtra("type");
            }
        }
    }

    public void requestActivityUpdatesButtonHandler() {
        int DETECTION_INTERVAL_IN_MILLISECONDS = 5000;
        Task<Void> task = mActivityRecognitionClient.requestActivityUpdates(
                DETECTION_INTERVAL_IN_MILLISECONDS,
                mPendingIntent);

        task.addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void result) {
                Commons.print("Successfully requested activity updates");
            }
        });

        task.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Commons.print("Requesting activity updates failed to start");
            }
        });
    }

    public void removeActivityUpdatesButtonHandler() {
        Task<Void> task = mActivityRecognitionClient.removeActivityUpdates(
                mPendingIntent);
        task.addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void result) {
                Commons.print("Removed activity updates successfully");
            }
        });

        task.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Commons.print("Failed to remove activity updates");
            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopLocationUpdates();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(activityBroadcastReceiver);

        removeActivityUpdatesButtonHandler();
    }
}
