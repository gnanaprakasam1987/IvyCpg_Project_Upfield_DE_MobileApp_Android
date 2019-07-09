package com.ivy.cpg.locationservice.realtime;

import android.Manifest;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.google.android.gms.location.ActivityRecognitionClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.ivy.cpg.locationservice.LocationDetailBO;
import com.ivy.cpg.locationservice.LocationServiceHelper;
import com.ivy.cpg.locationservice.activitytracking.ActivityIntentService;
import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.util.Commons;

import java.util.Date;
import java.util.Map;

import static com.ivy.cpg.locationservice.LocationConstants.LOCATION_DISPLACEMENT;
import static com.ivy.cpg.locationservice.LocationConstants.LOCATION_INTERVAL;
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


    private long supervisorLastUpdate ;
    private final long timeDiff = 1000 * 60 * 30;

    @Override
    public IBinder onBind(Intent intent) {return null;}

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        if(intent != null && intent.getExtras() != null) {
            Bundle bundle = intent.getExtras();
            realTimeLocation = (RealTimeLocation) bundle.getSerializable("REALTIME");

            buildNotification();

            //listenSupervisorState();

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

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, "LocationChannel")
                .setContentTitle(getString(R.string.app_name))
                .setContentText(getString(R.string.notification_text))
                .setOngoing(true)
                .setSmallIcon(R.drawable.ic_location_tracking);

        if (notificationManager != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel("LocationChannel",
                    "IvyCpg Data Channel",
                    NotificationManager.IMPORTANCE_DEFAULT);
            notificationManager.createNotificationChannel(channel);

        }

        startForeground(REALTIME_NOTIFICATION_ID, builder.build());
    }

    private void requestLocationUpdates() {
        LocationRequest request = new LocationRequest();
        request.setInterval(LOCATION_INTERVAL);
//        request.setFastestInterval(2500);
//        request.setMaxWaitTime(LOCATION_MAX_WAIT_TIME);
        request.setSmallestDisplacement(LOCATION_DISPLACEMENT);
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

                Location location = locationResult.getLastLocation();
                if (location != null) {
                    //Notifies if Mock Location is enabled
                    boolean isMockLocationEnabled = LocationServiceHelper.getInstance().notifyMockLocationStatus(getApplicationContext(),location);

                   // if(isBetterLocation(location,previousBestLocation)) {
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


//                        //Will update Location in Firestore only if Supervisor Visit Time less than 30 minutes
//                        if (supervisorLastUpdate != 0) {
//                            if ((System.currentTimeMillis() - supervisorLastUpdate) < timeDiff){

                        realTimeLocation.onRealTimeLocationReceived(locationDetailBO,getApplicationContext());

                        Intent sendGpsServiceIntent = new Intent(getApplicationContext(),RealtimeLocationUploadIntentService.class);

                        Bundle b = new Bundle();
                        b.putSerializable("LOCATION",locationDetailBO);
                        b.putString("Activity",activityName);
                        sendGpsServiceIntent.putExtras(b);

                        // Start intent service to upload location details
                        startService(sendGpsServiceIntent);

//                            }
//
//                        }
                    //}
                }
            }
        };
    }

    private void stopLocationUpdates(){
        if(client!=null && locationCallback !=null){
            client.removeLocationUpdates(locationCallback);
        }
    }

    private boolean isBetterLocationOld(Location location, Location currentBestLocation) {
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

        if (mActivityRecognitionClient !=null && mPendingIntent !=null) {

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
    }

    private void listenSupervisorState(){
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        ListenerRegistration listenerRegistration = db.collection("SupervisorState")
                .document("State")
                .addSnapshotListener(new EventListener<DocumentSnapshot>() {
                    @Override
                    public void onEvent(DocumentSnapshot documentSnapshot, FirebaseFirestoreException e) {
                        if (documentSnapshot != null){
                            Map<String,Object> map = documentSnapshot.getData();
                            if (map !=null) {
                                Date date = (Date) map.get("lastUpdate");
                                supervisorLastUpdate = date.getTime();
                            }
                        }
                    }
                });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stopUpdateWorks();
    }

    private void stopUpdateWorks(){
        stopLocationUpdates();

        try {
            LocalBroadcastManager.getInstance(this).unregisterReceiver(activityBroadcastReceiver);
        }catch(Exception e){
            Commons.printException(e);
        }

        //listenerRegistration.remove();
        removeActivityUpdatesButtonHandler();
    }

    private static final int TOO_OLD_LOCATION_DELTA = 1000 * 60 * 2;

    private boolean isBetterLocation(Location location, Location currentBestLocation) {
        if (currentBestLocation == null) {
            // A new location is always better than no location
            return true;
        }

        // Check whether the new location fix is newer or older
        long timeDelta = location.getTime() - currentBestLocation.getTime();
        boolean isSignificantlyNewer = timeDelta > TOO_OLD_LOCATION_DELTA;
        boolean isSignificantlyOlder = timeDelta < -TOO_OLD_LOCATION_DELTA;
        boolean isNewer = timeDelta > 0;

        // If it's been more than two minutes since the current location, use the new location
        // because the user has likely moved
        if (isSignificantlyNewer) {
            return true;
            // If the new location is more than two minutes older, it must be worse
        } else if (isSignificantlyOlder) {
            return false;
        }

        // Check whether the new location fix is more or less accurate
        int accuracyDelta = (int) (location.getAccuracy() - currentBestLocation.getAccuracy());
        boolean isLessAccurate = accuracyDelta > 0;
        boolean isMoreAccurate = accuracyDelta < 0;
        boolean isSignificantlyLessAccurate = accuracyDelta > 200;

        // Check if the old and new location are from the same provider
        boolean isFromSameProvider = isSameProvider(location.getProvider(),
                currentBestLocation.getProvider());

        // Determine location quality using a combination of timeliness and accuracy
        if (isMoreAccurate) {
            return true;
        } else if (isNewer && !isLessAccurate) {
            return true;
        } else if (isNewer && !isSignificantlyLessAccurate && isFromSameProvider) {
            return true;
        }
        return false;
    }

    /** Checks whether two providers are the same */
    private boolean isSameProvider(String provider1, String provider2) {
        if (provider1 == null) {
            return provider2 == null;
        }
        return provider1.equals(provider2);
    }
}
