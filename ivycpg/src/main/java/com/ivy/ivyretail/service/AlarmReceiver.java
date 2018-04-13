package com.ivy.ivyretail.service;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;

import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.util.Commons;

import java.util.Calendar;
import java.util.GregorianCalendar;

public class AlarmReceiver extends BroadcastReceiver {

	BusinessModel bmodel;

	@Override
	public void onReceive(Context context, Intent intent) {

		bmodel = (BusinessModel) context.getApplicationContext();

		if (intent.getAction()!=null && intent.getAction().equals("android.intent.action.BOOT_COMPLETED")) {
			setAlarm(context);
		}else{
			setAlarmAgain(context);
		}
	}

	public static void setAlarm(Context context) {

		SharedPreferences pref = context.getSharedPreferences("TimePref", 0);
		int alarm_time = pref.getInt("AlarmTime", 0);

		boolean alarmUp = (PendingIntent.getBroadcast(context, 0, new Intent(context,
				AlarmReceiver.class), PendingIntent.FLAG_NO_CREATE) != null);

		if (!alarmUp) {
			setPendingIntent(context,alarm_time);

			// Below has been added to enable the receiver manually as we have
			// disabled in the manifest
			ComponentName receiver = new ComponentName(context,
					AlarmReceiver.class);
			PackageManager pm = context.getPackageManager();
			pm.setComponentEnabledSetting(receiver,
					PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
					PackageManager.DONT_KILL_APP);
		}

	}

	private static void setPendingIntent(Context context,int alarm_time){
		Intent i = new Intent(context, AlarmReceiver.class);
		PendingIntent pi = PendingIntent.getBroadcast(context, 0, i, 0);
		AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
		long timeInMillis = System.currentTimeMillis() + (alarm_time * 60 * 1000);
		Commons.print("Time in millis--> "+timeInMillis);

		if(alarmManager!=null) {
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
	 * Call Alarm manager and set Alarm for Every X minutes
	 */
	private void setAlarmAgain(Context context) {
		try {

			int timeInHrs = getTimeInHrs();
			SharedPreferences pref = context.getSharedPreferences("TimePref", 0);
			int alarm_time = pref.getInt("AlarmTime", 0);
			int start_Time = pref.getInt("StartTime", 0);
			int end_Time = pref.getInt("EndTime", 0);
			boolean uplaodloc = pref.getBoolean("UploadUserLoc", false);

			Commons.print("Start Time--> "+start_Time+" -- End Time--> "+end_Time +" ---- TimeHrs--> "+timeInHrs +" uplaodloc - "+uplaodloc);

			if (timeInHrs > start_Time && timeInHrs < end_Time) { // Check for task only at day
				String stopServiceClassStr;

				if (uplaodloc) {
					stopServiceClassStr = LocationListenerService.class
							.getName();
					if (BusinessModel.isMyServiceRunning(context,
							stopServiceClassStr)) {

						Intent stopServiceIntent = new Intent(
								context,
								LocationListenerService.class);
						context.stopService(stopServiceIntent);
					}

					// Call the notification downloader service.
					Intent locationCapture = new Intent(context,
							LocationListenerService.class);
					context.startService(locationCapture);
				}
			}
			else if(timeInHrs < start_Time)
                alarm_time = (start_Time - timeInHrs) * 60;
			else if (timeInHrs > end_Time)
			    alarm_time = ((24 - timeInHrs) + start_Time) * 60;

			setPendingIntent(context,alarm_time);
		} catch (Exception e) {
			Commons.printException(e);
		}
	}

	/**
	 * Get the hour of the day
	 * 
	 * @return in 24hr format
	 */
	private int getTimeInHrs() {
		try {
			Calendar calendar = new GregorianCalendar();
			return calendar.get(Calendar.HOUR_OF_DAY);
		} catch (Exception e) {
			Commons.printException(""+e);
		}
		return 0;
	}

}
