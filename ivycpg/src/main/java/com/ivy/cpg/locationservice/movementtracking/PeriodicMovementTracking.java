package com.ivy.cpg.locationservice.movementtracking;


import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.widget.Toast;

import com.ivy.cpg.locationservice.LocationServiceHelper;
import com.ivy.sd.png.asean.view.R;

public class PeriodicMovementTracking {

    public static final int STATUS_SUCCESS = 0;  // Service started Successfully
    public static final int STATUS_LOCATION_PERMISSION = 1; // Location Permission is not enabled
    public static final int STATUS_GPS = 2; // GPS Not enabled
    public static final int STATUS_LOCATION_ACCURACY = 3; // Location Accuracy level is low
    public static final int STATUS_MOCK_LOCATION = 4; // Mock Location is enabled
    public static final int STATUS_SERVICE_ERROR = 5; // Problem in starting Service
    public static final int STATUS_TIME_MISMATCH = 6; // Start time is greater than end
    public static final int STATUS_ALARM_TIME = 7; //Alarm time shoukd not be zero


    public static void startTrackingLocation(MovementTracking movementTracking, Context context){

        registerAlarmReceiver(movementTracking, context);
    }

    public static int startTrackingLocation(MovementTracking movementTracking, Context context,
                                             int startTime, int endTime, int alarmTime){

        //Check whether location permission is enabled
        if(!LocationServiceHelper.getInstance().hasLocationPermissionEnabled(context)){
            Toast.makeText(context,
                    context.getResources().getString(R.string.permission_enable_msg),
                    Toast.LENGTH_SHORT).show();
            return STATUS_LOCATION_PERMISSION;
        }

        //Check GPS is Enabled or not
        if(!LocationServiceHelper.getInstance().isGpsEnabled(context)) {
            Toast.makeText(context,
                    context.getString(R.string.enable_gps),
                    Toast.LENGTH_SHORT).show();
            return STATUS_GPS;
        }

        //Checks whether if location accuracy is not set as high
        if (!LocationServiceHelper.getInstance().isLocationHighAccuracyEnabled(context)) {
            Toast.makeText(context, context.getString(R.string.status_location_accuracy), Toast.LENGTH_SHORT).show();
            return STATUS_LOCATION_ACCURACY;
        }

        //Check whether Mock Location is enabled or not
        if (!LocationServiceHelper.getInstance().isMockSettingsON(context)) {
            Toast.makeText(context, context.getString(R.string.status_mock_location), Toast.LENGTH_SHORT).show();
            return STATUS_MOCK_LOCATION;
        }

        //Checks Alarm interval time should be zero
        if(alarmTime == 0){
            Toast.makeText(context, context.getString(R.string.status_alarm_time_zero), Toast.LENGTH_SHORT).show();
            return STATUS_ALARM_TIME;
        }

        //checks the start time of Alarm should not be greater than end time
        if(startTime > endTime) {
            Toast.makeText(context, context.getString(R.string.status_time_mismatch), Toast.LENGTH_SHORT).show();
            return STATUS_TIME_MISMATCH;
        }

        SharedPreferences pref = context.getSharedPreferences("TimePref", 0);
        SharedPreferences.Editor editor = pref.edit();
        editor.putInt("AlarmTime", alarmTime); // AlarmTime
        editor.putInt("StartTime", startTime); // Start Time
        editor.putInt("EndTime", endTime); // End Time
        editor.putBoolean("UploadUserLoc", true);
        editor.apply();

        registerAlarmReceiver(movementTracking, context);

        return STATUS_SUCCESS;
    }


    /*
    * Checks whether already receiver is enabled
    * Set Alarm Interval time to wake Alarm
    */
    private static void registerAlarmReceiver(MovementTracking movementTracking, Context context) {
        boolean alarmUp = (PendingIntent.getBroadcast(context, 1001, new Intent(context,
                MovementTrackingAlarmReceiver.class), PendingIntent.FLAG_NO_CREATE) != null);

        if (!alarmUp) {
            LocationServiceHelper.getInstance().setPendingIntent(context,
                    LocationServiceHelper.getInstance().getAlarmTime(context),
                    movementTracking);

            // Below has been added to enable the receiver manually as we have
            // disabled in the manifest
            ComponentName receiver = new ComponentName(context,
                    MovementTrackingAlarmReceiver.class);
            PackageManager pm = context.getPackageManager();
            pm.setComponentEnabledSetting(receiver,
                    PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
                    PackageManager.DONT_KILL_APP);
        }
    }


}
