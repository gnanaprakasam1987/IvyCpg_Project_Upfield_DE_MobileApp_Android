package com.ivy.ivyretail.service;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.widget.Toast;

import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.util.Commons;

import java.util.Calendar;
import java.util.GregorianCalendar;

public class LocationListenerService extends Service {

	UserDefinedBroadcastReceiver broadCastReceiver = new UserDefinedBroadcastReceiver();

	private static final long MAX_TIME = 120000;
	private static final int MAX_COUNT = 50;

	private boolean timeup = false;
	private int count = 0;

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	public int onStartCommand(Intent intent, int flags, int startId) {

		startLocationListener();

		return Service.START_STICKY;
	}

	class UserDefinedBroadcastReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {

			context.unregisterReceiver(broadCastReceiver);

			// You can do the processing here update the widget/remote views.
			StringBuilder msgStr = new StringBuilder("Current time : ");

			Toast.makeText(context, msgStr, Toast.LENGTH_SHORT).show();
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

		Commons.print("Start Time LOC Listener--> ");

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

				sendLocation();

			}
		}.start();

	}

	private void sendLocation() {
		try {
			LocationUtil.getInstance(getApplicationContext()).stopLocationListener();
			String latitude = String.valueOf(LocationUtil.latitude);
			String longitude = String.valueOf(LocationUtil.longitude);
			Float accuracy = LocationUtil.accuracy;

			SharedPreferences pref = getApplicationContext()
					.getSharedPreferences("TimePref", 0);
			int start_Time = pref.getInt("StartTime", 0);
			int end_Time = pref.getInt("EndTime", 0);

			Commons.print("Start Time LOC Listener--> "+start_Time+" -- End Time--> "+end_Time +" ---- TimeHrs--> ");

			count = count + 1;

			if ((accuracy) <= 10 || count >= MAX_COUNT || timeup) {
				String[] coordinates = {latitude, longitude, accuracy + ""};

				int timeInHrs = getTimeInHrs();
				if (timeInHrs > start_Time && timeInHrs < end_Time) {
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

	private void stopService(){
		stopService(new Intent(getApplicationContext(),LocationListenerService.class));
	}
}
