package com.ivy.cpg.locationservice;


import android.Manifest;
import android.app.ActivityManager;
import android.app.AlarmManager;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.BatteryManager;
import android.os.Build;
import android.provider.Settings;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.ivy.cpg.locationservice.movementtracking.MovementTrackingAlarmReceiver;
import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.util.Commons;

import java.util.Calendar;
import java.util.GregorianCalendar;

import static android.content.Context.LOCATION_SERVICE;
import static com.ivy.cpg.locationservice.LocationConstants.GPS_NOTIFICATION_ID;
import static com.ivy.cpg.locationservice.LocationConstants.MOCK_NOTIFICATION_ID;

public class LocationServiceHelper {

    private static LocationServiceHelper instance = null;

    private LocationServiceHelper() {
    }

    public static LocationServiceHelper getInstance() {
        if (instance == null) {
            instance = new LocationServiceHelper();
        }
        return instance;
    }

    //Check whether service is running or nt
    public boolean isMyServiceRunning(Context context,
                                      String serviceClassName) {
        ActivityManager manager = (ActivityManager) context
                .getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClassName.equals(service.service.getClassName()))
                return true;
        }
        return false;
    }

    /**
     * Return true if Google play services available in device
     */
    public boolean isGooglePlayServicesAvailable(Context context) {

        int status = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(context);
        return ConnectionResult.SUCCESS == status;

    }

    //Checks whether if location accuracy is not set as high
    public boolean isLocationHighAccuracyEnabled(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            int locationMode = 0;
            try {
                locationMode = Settings.Secure.getInt(context.getContentResolver(), Settings.Secure.LOCATION_MODE);

            } catch (Exception e) {
                e.printStackTrace();
            }

            return (locationMode != Settings.Secure.LOCATION_MODE_OFF && locationMode == Settings.Secure.LOCATION_MODE_HIGH_ACCURACY); //check location mode

        }
        return true;
    }

    //Check whether Mock Location is enabled or not
    public boolean isMockSettingsON(Context context) {
        // returns true if mock location enabled, false if not enabled.
        return (Settings.Secure.getString(context.getContentResolver(),
                Settings.Secure.ALLOW_MOCK_LOCATION).equals("0"));
    }

    //Check GPS is Enabled or not
    public boolean isGpsEnabled(Context context) {
        LocationManager service = (LocationManager) context.getSystemService(LOCATION_SERVICE);
        if (service != null)
            return (service.isProviderEnabled(LocationManager.GPS_PROVIDER));

        return false;
    }

    //Check Location Permissions are granted or not
    public boolean hasLocationPermissionEnabled(Context context) {
        int locationMode = 0;
        String locationProviders;
        boolean isAvailable;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
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


    /*Notifies the user, if GPS not enabled send user to the GPS settings
    * Also cancel the notification if permission enabled
    */
    public boolean notifyGPSStatus(Context context) {

        if (!LocationServiceHelper.getInstance().isGpsEnabled(context)) {

            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);

            NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "GPS ENABLED")
                    .setContentTitle(context.getString(R.string.app_name))
                    .setContentText(context.getString(R.string.enable_gps))
                    .setOngoing(true)
                    .setContentIntent(pendingIntent)
                    .setAutoCancel(true)
                    .setSmallIcon(R.drawable.ic_gps_disabled);

            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
            notificationManager.notify(GPS_NOTIFICATION_ID, builder.build());

            return false;
        } else {
            NotificationManager nMgr = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            if (nMgr != null)
                nMgr.cancel(GPS_NOTIFICATION_ID);

            return true;
        }
    }

    /*Notifies the user, if Mock Location is enabled send user to the settings
    *Also cancel the notification if permission enabled
    */
    public boolean notifyMockLocationStatus(Context context) {

        if (!LocationServiceHelper.getInstance().isMockSettingsON(context)) {

            Intent intent = new Intent(android.provider.Settings.ACTION_APPLICATION_DEVELOPMENT_SETTINGS);
            PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);

            NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "MOCK LOCATION")
                    .setContentTitle(context.getString(R.string.app_name))
                    .setContentText("Disable mock Location")
                    .setOngoing(true)
                    .setContentIntent(pendingIntent)
                    .setAutoCancel(true)
                    .setSmallIcon(R.drawable.ic_warning);

            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(context);
            notificationManager.notify(MOCK_NOTIFICATION_ID, builder.build());

            return true;
        } else {
            NotificationManager nMgr = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            if (nMgr != null)
                nMgr.cancel(MOCK_NOTIFICATION_ID);

            return false;
        }
    }

    /**
     * Get current battery percentage
     */
    public int getBatteryPercentage(Context context) {

        IntentFilter iFilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        Intent batteryStatus = context.registerReceiver(null, iFilter);

        int level = batteryStatus != null ? batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) : -1;
        int scale = batteryStatus != null ? batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1) : -1;

        float batteryPct = level / (float) scale;

        return (int) (batteryPct * 100);
    }

    /**
     * Call Alarm manager and set Alarm for Every X minutes
     */
    public void setPendingIntent(Context context, int alarm_time) {

        Intent i = new Intent(context, MovementTrackingAlarmReceiver.class);

        PendingIntent pi = PendingIntent.getBroadcast(context, 1001, i, 0);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        long timeInMillis = System.currentTimeMillis() + (alarm_time * 60 * 1000);
//        Commons.print("AlarmManager Time in millis--> " + timeInMillis);

        if (alarmManager != null) {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
                alarmManager.set(AlarmManager.RTC_WAKEUP, timeInMillis, pi);
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT && Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, timeInMillis, pi);
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, timeInMillis, pi);
            }
        }
    }

    /**
     * returns interval value to set in alarm
     */
    public int getAlarmTime(Context context) {
        int timeInHrs = getCurrentTimeInHrs();
        int timeInMins = getCurrentTimeInMints();

        SharedPreferences pref = context.getSharedPreferences("TimePref", 0);

        int alarm_time = pref.getInt("AlarmTime", 0);
        int start_Time = pref.getInt("StartTime", 0);
        int end_Time = pref.getInt("EndTime", 0);

        if (timeInHrs < start_Time) {
            alarm_time = (start_Time - timeInHrs) * 60 - timeInMins;
        } else if (timeInHrs >= end_Time) {
            alarm_time = ((24 - timeInHrs) + start_Time) * 60 - timeInMins;
        }

        return alarm_time;
    }


    /**
     * Get the Current hour of the day
     *
     * @return in 24hr format
     */
    public int getCurrentTimeInHrs() {
        try {
            Calendar calendar = new GregorianCalendar();
            return calendar.get(Calendar.HOUR_OF_DAY);
        } catch (Exception e) {
            Commons.printException("" + e);
        }
        return 0;
    }

    /**
     * Get the Current Minute
     *
     * @return in minutes
     */
    public int getCurrentTimeInMints() {
        try {
            Calendar calendar = new GregorianCalendar();
            return calendar.get(Calendar.MINUTE);
        } catch (Exception e) {
            Commons.printException("" + e);
        }
        return 0;
    }


    /**
     * Cancel the registered Alaram class from Alaram manager
     */
    public void cancelAlarm(Context context, Class aClass) {
        AlarmManager alarm = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

        Intent i = new Intent(context, aClass);
        PendingIntent pi = PendingIntent.getBroadcast(context, 0, i, 0);
        /* cancel any pending alarm */
        if (alarm != null)
            alarm.cancel(pi);
    }
}
