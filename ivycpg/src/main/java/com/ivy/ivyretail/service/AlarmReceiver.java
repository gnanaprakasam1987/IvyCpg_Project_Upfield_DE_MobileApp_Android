package com.ivy.ivyretail.service;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
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

		int timeInHrs = getTimeInHrs();

		SharedPreferences pref = context.getSharedPreferences("TimePref", 0); 
		int start_Time = pref.getInt("StartTime", 0);
		int end_Time = pref.getInt("EndTime", 0);
		boolean uplaodloc = pref.getBoolean("UploadUserLoc", false);

		if (timeInHrs > start_Time && timeInHrs < end_Time) { // Check for task
																// only at day
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
		setAlarmAgain(context);
	}

	/**
	 * Call Alarm manager and set Alarm for Every 15 minutes
	 */
	private void setAlarmAgain(Context context) {
		try {
			SharedPreferences pref = context
					.getSharedPreferences("TimePref", 0); // 0 - for private
															// mode
			int alarm_time = pref.getInt("AlarmTime", 0);

			Intent i = new Intent(context, AlarmReceiver.class);
			PendingIntent pi = PendingIntent.getBroadcast(context, 0, i, 0);
			AlarmManager alarmManager = (AlarmManager) context
					.getSystemService(Service.ALARM_SERVICE);
			long timeInMillis = System.currentTimeMillis() + (alarm_time * 60 * 1000);

			if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
				alarmManager.set(AlarmManager.RTC_WAKEUP, timeInMillis, pi);
			}else if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT && Build.VERSION.SDK_INT < Build.VERSION_CODES.M){
				alarmManager.setExact(AlarmManager.RTC_WAKEUP, timeInMillis, pi);
			}else if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
				alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, timeInMillis, pi);
			}
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
