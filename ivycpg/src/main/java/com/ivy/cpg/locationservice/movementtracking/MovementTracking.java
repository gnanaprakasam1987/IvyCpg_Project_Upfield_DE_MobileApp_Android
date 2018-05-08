package com.ivy.cpg.locationservice.movementtracking;


import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.widget.Toast;

import com.ivy.cpg.locationservice.LocationConstants;
import com.ivy.cpg.locationservice.LocationServiceHelper;
import com.ivy.sd.png.asean.view.R;

public class MovementTracking {


    static void startTrackingLocation(Context context){

        registerAlarmReceiver(context);
    }

    public static int startTrackingLocation(Context context,
                                             int startTime, int endTime, int alarmTime){

        //Check whether location permission is enabled
        if(!LocationServiceHelper.getInstance().hasLocationPermissionEnabled(context)){
            Toast.makeText(context,
                    "Movement Tracking : "+context.getResources().getString(R.string.permission_enable_msg),
                    Toast.LENGTH_SHORT).show();
            return LocationConstants.STATUS_LOCATION_PERMISSION;
        }

        //Check GPS is Enabled or not
        if(!LocationServiceHelper.getInstance().isGpsEnabled(context)) {
            Toast.makeText(context,
                    "Movement Tracking : "+context.getString(R.string.enable_gps),
                    Toast.LENGTH_SHORT).show();
            return LocationConstants.STATUS_GPS;
        }

        //Checks whether if location accuracy is not set as high
        if (!LocationServiceHelper.getInstance().isLocationHighAccuracyEnabled(context)) {
            Toast.makeText(context, "Movement Tracking : "+context.getString(R.string.status_location_accuracy), Toast.LENGTH_SHORT).show();
            return LocationConstants.STATUS_LOCATION_ACCURACY;
        }

        //Check whether Mock Location is enabled or not
        if (!LocationServiceHelper.getInstance().isMockSettingsON(context)) {
            Toast.makeText(context, "Movement Tracking : "+context.getString(R.string.status_mock_location), Toast.LENGTH_SHORT).show();
            return LocationConstants.STATUS_MOCK_LOCATION;
        }

        //Checks Alarm interval time should be zero
        if(alarmTime == 0){
            Toast.makeText(context, "Movement Tracking : "+context.getString(R.string.status_alarm_time_zero), Toast.LENGTH_SHORT).show();
            return LocationConstants.STATUS_ALARM_TIME;
        }

        //checks the start time of Alarm should not be greater than end time
        if(startTime > endTime) {
            Toast.makeText(context, "Movement Tracking : "+context.getString(R.string.status_time_mismatch), Toast.LENGTH_SHORT).show();
            return LocationConstants.STATUS_TIME_MISMATCH;
        }

        SharedPreferences pref = context.getSharedPreferences("TimePref", 0);
        SharedPreferences.Editor editor = pref.edit();
        editor.putInt("AlarmTime", alarmTime); // AlarmTime
        editor.putInt("StartTime", startTime); // Start Time
        editor.putInt("EndTime", endTime); // End Time
        editor.putBoolean("UploadUserLoc", true);
        editor.apply();

        registerAlarmReceiver(context);

        return LocationConstants.STATUS_SUCCESS;
    }


    /*
    * Checks whether already receiver is enabled.
    * Set Alarm Interval time to wake Alarm.
    */
    private static void registerAlarmReceiver(Context context) {
        boolean alarmUp = (PendingIntent.getBroadcast(context, 1001, new Intent(context,
                MovementTrackingAlarmReceiver.class), PendingIntent.FLAG_NO_CREATE) != null);

        if (!alarmUp) {
            LocationServiceHelper.getInstance().setPendingIntent(context,
                    LocationServiceHelper.getInstance().getAlarmTime(context));

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
