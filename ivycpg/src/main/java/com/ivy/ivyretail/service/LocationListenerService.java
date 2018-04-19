package com.ivy.ivyretail.service;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.widget.Toast;

import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.util.Commons;

import java.util.Calendar;
import java.util.GregorianCalendar;

public class LocationListenerService extends Service {

	UserDefinedBroadcastReceiver broadCastReceiver = new UserDefinedBroadcastReceiver();

	private static final long MAX_TIME = 120000;
	private static final int MAX_COUNT = 50;

	private boolean timeup = false, isLocationUploaded = false;;
	private int count = 0;
	private String INTENT_ACTION ="LOCATION CAPTURED";

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public void onCreate() {
		LocalBroadcastManager.getInstance(this).registerReceiver(broadCastReceiver,new IntentFilter(INTENT_ACTION));
	}

	public int onStartCommand(Intent intent, int flags, int startId) {

		startLocationListener();

		return Service.START_STICKY;
	}

	class UserDefinedBroadcastReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			if(intent.getAction() != null && intent.getAction().equals(INTENT_ACTION)) {
				Commons.print("Location listener On receive");
				sendLocation();
			}
		}
	}

	private int getTimeInHrs() {
		try {
			Calendar calendar = new GregorianCalendar();
			return calendar.get(Calendar.HOUR_OF_DAY);
		} catch (Exception e) {
			Commons.printException(""+e);
		}
		return 0;
	}

	/**
	 * Start the Location listener. Use GPS_PROVIDER if GPS is enabled by user
	 * or it will use NERWORK_PROVIDER.
	 */
	public void startLocationListener() {

		timeup = false;
		isLocationUploaded = false;

		Commons.print("AlarmManager startLocationListener--> ");

		LocationUtil.getInstance(getApplicationContext()).startLocationListener();

		setTimer();
	}


	private void setTimer() {

		new CountDownTimer(MAX_TIME, 1000) {

			public void onTick(long millisUntilFinished) {

				//Log.d("TIMEW", "" + millisUntilFinished / 1000);
			}

			public void onFinish() {
				timeup = true;
				if (!isLocationUploaded)
					sendLocation();
			}
		}.start();

	}

	private void sendLocation() {
		try {
			String latitude = String.valueOf(LocationUtil.latitude);
			String longitude = String.valueOf(LocationUtil.longitude);
			Float accuracy = LocationUtil.accuracy;

			SharedPreferences pref = getApplicationContext()
					.getSharedPreferences("TimePref", 0);
			int start_Time = pref.getInt("StartTime", 0);
			int end_Time = pref.getInt("EndTime", 0);

			count = count + 1;

			Commons.print("AlarmManager Start Time --> "+start_Time+" -- End Time--> "+end_Time);
			Commons.print("AlarmManager accuracy---"+accuracy+" count-- "+count+" timeup---"+timeup);

			if (((accuracy) <= 10) || (count >= MAX_COUNT || timeup)) {
				String[] coordinates = {latitude, longitude, accuracy + ""};

				isLocationUploaded = true;
				LocationUtil.getInstance(getApplicationContext()).stopLocationListener();

				int timeInHrs = getTimeInHrs();
				if (timeInHrs >= start_Time && timeInHrs < end_Time) {
					Context stopServiceContext = getApplicationContext();
					String stopServiceClassStr = LocationUploadService.class
							.getName();
					if (BusinessModel.isMyServiceRunning(
							stopServiceContext, stopServiceClassStr)) {
						Intent stopServiceIntent = new Intent(
								stopServiceContext,
								LocationUploadService.class);
						stopServiceIntent.putExtra("Coordinates",
								coordinates);
						stopService(stopServiceIntent);
					}
					// Call the notification downloader service.
					Intent sendGpsServiceIntent = new Intent(stopServiceContext,
							LocationUploadService.class);
					sendGpsServiceIntent.putExtra("Coordinates",
							coordinates);
					startService(sendGpsServiceIntent);
				}

				stopService();

			}
		} catch (Exception e) {
			Commons.printException(e);
		}
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		LocalBroadcastManager.getInstance(this).unregisterReceiver(broadCastReceiver);
	}

	private void stopService(){
		stopService(new Intent(getApplicationContext(),LocationListenerService.class));
	}
}
