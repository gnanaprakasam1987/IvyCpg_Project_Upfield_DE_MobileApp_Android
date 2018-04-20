package com.ivy.cpg.locationservice.movementtracking;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import com.ivy.cpg.locationservice.LocationServiceHelper;
import com.ivy.sd.png.util.Commons;

public class MovementTrackingAlarmReceiver extends BroadcastReceiver {
	private int alarm_time , start_Time , end_Time ;

	@Override
	public void onReceive(Context context, Intent intent) {

		SharedPreferences pref = context.getSharedPreferences("TimePref", 0);
		alarm_time = pref.getInt("AlarmTime", 0);
		start_Time = pref.getInt("StartTime", 0);
		end_Time = pref.getInt("EndTime", 0);
		boolean isUplaodloc = pref.getBoolean("UploadUserLoc", false);

		if(isUplaodloc) {
			if (intent.getAction() != null && intent.getAction().equals("android.intent.action.BOOT_COMPLETED")) {

				PeriodicMovementTracking.startTrackingLocation(new PeriodicMovementTrackUpload(),context);
			} else {
				MovementTracking movementTracking = null;
				if (intent.getExtras() != null) {
					Bundle bundle = intent.getExtras();
					movementTracking = (MovementTracking) bundle.getSerializable("TRACKING");
				}
				setAlarmAgain(context, movementTracking);
			}
		}
	}

/*Starting the location service and again setting time in alarm manager*/
	private void setAlarmAgain(Context context, MovementTracking movementTracking) {
		try {

			int timeInHrs = LocationServiceHelper.getInstance().getCurrentTimeInHrs();
			int timeInMinutes = LocationServiceHelper.getInstance().getCurrentTimeInMints();

			if (timeInHrs >= start_Time && timeInHrs < end_Time) { // Check for task only at day

				if (LocationServiceHelper.getInstance().isMyServiceRunning(context,
						LocationListenerService.class.getName())) {

					context.stopService(new Intent(context,LocationListenerService.class));
				}

				// starting Movement location service to track location.
				Intent intent = new Intent(context, LocationListenerService.class);
				Bundle b = new Bundle();
				b.putSerializable("TRACKING",movementTracking);
				intent.putExtras(b);

				context.startService(intent);

			}
			else if(timeInHrs < start_Time) {
				alarm_time = (start_Time - timeInHrs) * 60 - timeInMinutes;
			}
			else if (timeInHrs >= end_Time) {
				alarm_time = ((24 - timeInHrs) + start_Time) * 60 - timeInMinutes;
			}

			LocationServiceHelper.getInstance().setPendingIntent(context,alarm_time,movementTracking);

		} catch (Exception e) {
			Commons.printException(e);
		}
	}

}
