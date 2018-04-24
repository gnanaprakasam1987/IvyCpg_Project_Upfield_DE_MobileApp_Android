package com.ivy.cpg.locationservice.movementtracking;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import com.ivy.cpg.locationservice.LocationServiceHelper;
import com.ivy.cpg.locationservice.activitytracking.ActivityRecognitionService;
import com.ivy.sd.png.util.Commons;

public class MovementTrackingAlarmReceiver extends BroadcastReceiver {
    private int alarm_time, start_Time, end_Time;

    @Override
    public void onReceive(Context context, Intent intent) {

        SharedPreferences pref = context.getSharedPreferences("TimePref", 0);
        alarm_time = pref.getInt("AlarmTime", 0);
        start_Time = pref.getInt("StartTime", 0);
        end_Time = pref.getInt("EndTime", 0);
        boolean isUplaodloc = pref.getBoolean("UploadUserLoc", false);

        if (isUplaodloc) {
            if (intent.getAction() != null && intent.getAction().equals("android.intent.action.BOOT_COMPLETED")) {
                MovementTracking.startTrackingLocation(context);
            } else {
                setAlarmAgain(context);
            }
        }
    }

    /**
     * Starting the location service and again setting time in alarm manager
     */
    private void setAlarmAgain(Context context) {
        try {

            int timeInHrs = LocationServiceHelper.getInstance().getCurrentTimeInHrs();
            int timeInMinutes = LocationServiceHelper.getInstance().getCurrentTimeInMints();

// 			Check time to start service, starts only if it falls in the range
            if (timeInHrs >= start_Time && timeInHrs < end_Time) {

                if (LocationServiceHelper.getInstance().isMyServiceRunning(context,
                        MovementTrackingListenerService.class.getName())) {
                    context.stopService(new Intent(context, MovementTrackingListenerService.class));
                }
                // starting Movement location service to track location.
                context.startService(new Intent(context, MovementTrackingListenerService.class));


                //Starting the Activity tracking service.
//                if(!LocationServiceHelper.getInstance().isMyServiceRunning(context, ActivityRecognitionService.class.getName())){
//                    context.startService(new Intent(context, ActivityRecognitionService.class));
//                }

            } else if (timeInHrs < start_Time) {
                alarm_time = (start_Time - timeInHrs) * 60 - timeInMinutes;
            } else if (timeInHrs >= end_Time) {
                alarm_time = ((24 - timeInHrs) + start_Time) * 60 - timeInMinutes;
            }

            LocationServiceHelper.getInstance().setPendingIntent(context, alarm_time);

        } catch (Exception e) {
            Commons.printException(e);
        }
    }

}
