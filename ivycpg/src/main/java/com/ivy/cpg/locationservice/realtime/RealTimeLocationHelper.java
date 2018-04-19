package com.ivy.cpg.locationservice.realtime;


import android.Manifest;
import android.app.ActivityManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Build;
import android.provider.Settings;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.ivy.sd.png.asean.view.R;

import static android.content.Context.LOCATION_SERVICE;

public class RealTimeLocationHelper {

    private static RealTimeLocationHelper instance = null;
    private final int MOCK_NOTIFICATION_ID = 1113;
    private final int GPS_NOTIFICATION_ID = 1111;

    private RealTimeLocationHelper(Context context) {
    }

    public static RealTimeLocationHelper getInstance(Context context) {
        if (instance == null) {
            instance = new RealTimeLocationHelper(context);
        }
        return instance;
    }

    //Check whether service is running or nt
    boolean isMyServiceRunning(Context context,
                                             String serviceClassName) {
        ActivityManager manager = (ActivityManager) context
                .getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClassName.equals(service.service.getClassName()))
                return true;
        }
        return false;
    }

    boolean isGooglePlayServicesAvailable(Context context) {

        int status = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(context);
        return ConnectionResult.SUCCESS == status;

    }

    //Checks whether if location accuracy is not set as high
    boolean isLocationHighAccuracyEnabled(Context context){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            int locationMode = 0;
            try {
                locationMode = Settings.Secure.getInt(context.getContentResolver(), Settings.Secure.LOCATION_MODE);

            } catch (Exception e) {
                e.printStackTrace();
            }

            return (locationMode != Settings.Secure.LOCATION_MODE_OFF && locationMode == Settings.Secure.LOCATION_MODE_HIGH_ACCURACY); //check location mode

        }
        return false;
    }

    //Check whether Mock Location is enabled or not
    boolean isMockSettingsON(Context context) {
        // returns true if mock location enabled, false if not enabled.
        return (Settings.Secure.getString(context.getContentResolver(),
                Settings.Secure.ALLOW_MOCK_LOCATION).equals("0"));
    }

    //Check GPS is Enabled or not
    boolean isGpsEnabled(Context context){
        LocationManager service = (LocationManager) context.getSystemService(LOCATION_SERVICE);
        if(service!=null)
            return (service.isProviderEnabled(LocationManager.GPS_PROVIDER));

        return false;
    }

    //Check Location Permissions are granted or not
    boolean hasLocationPermissionEnabled(Context context) {
        int locationMode = 0;
        String locationProviders;
        boolean isAvailable;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT){
            try {
                locationMode = Settings.Secure.getInt(context.getContentResolver(), Settings.Secure.LOCATION_MODE);
            } catch (Exception e) {
                e.printStackTrace();
            }

            isAvailable = (locationMode != Settings.Secure.LOCATION_MODE_OFF);
        } else {
            locationProviders = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.LOCATION_PROVIDERS_ALLOWED);
            isAvailable = !TextUtils.isEmpty(locationProviders);
        }

        boolean coarsePermissionCheck = (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED);
        boolean finePermissionCheck = (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED);

        return isAvailable && (coarsePermissionCheck || finePermissionCheck);
    }


    //Notifies the user, if GPS not enabled send user to the GPS settings
    void notifyGPSStatus(Context context){

        if (!RealTimeLocationHelper.getInstance(context).isGpsEnabled(context)) {

            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);

            NotificationCompat.Builder builder = new NotificationCompat.Builder(context,"GPS ENABLED")
                    .setContentTitle(context.getString(R.string.app_name))
                    .setContentText(context.getString(R.string.enable_gps))
                    .setOngoing(true)
                    .setContentIntent(pendingIntent)
                    .setAutoCancel(true)
                    .setSmallIcon(R.drawable.ic_gps_disabled);

            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
            notificationManager.notify(GPS_NOTIFICATION_ID, builder.build());
        }
        else {
            NotificationManager nMgr = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            if(nMgr != null)
                nMgr.cancel(GPS_NOTIFICATION_ID);
        }
    }

    //Notifies the user, if Mock Location is enabled send user to the settings
    void notifyMockLocationStatus(Context context){

        if (!RealTimeLocationHelper.getInstance(context).isMockSettingsON(context)) {

            Intent intent = new Intent(android.provider.Settings.ACTION_APPLICATION_DEVELOPMENT_SETTINGS);
            PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);

            NotificationCompat.Builder builder = new NotificationCompat.Builder(context,"MOCK LOCATION")
                    .setContentTitle(context.getString(R.string.app_name))
                    .setContentText("Disable mock Location")
                    .setOngoing(true)
                    .setContentIntent(pendingIntent)
                    .setAutoCancel(true)
                    .setSmallIcon(R.drawable.ic_warning);

            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
            notificationManager.notify(MOCK_NOTIFICATION_ID, builder.build());
        }
        else {
            NotificationManager nMgr = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            if(nMgr != null)
                nMgr.cancel(MOCK_NOTIFICATION_ID);
        }
    }
}
