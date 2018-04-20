package com.ivy.cpg.locationservice.movementtracking;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;

import com.ivy.cpg.locationservice.LocationServiceHelper;
import com.ivy.sd.png.util.Commons;

public class LocationListenerService extends Service {

	UserDefinedBroadcastReceiver broadCastReceiver = new UserDefinedBroadcastReceiver();

	private static final long MAX_TIME = 120000;
	private static final int MAX_COUNT = 50;

	private boolean timeup = false, isLocationUploaded = false;
	private int count = 0;
	private String INTENT_ACTION ="LOCATION CAPTURED";
	private MovementTracking movementTracking;


	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public void onCreate() {
		super.onCreate();
	}

	public int onStartCommand(Intent intent, int flags, int startId) {

		if(intent.getExtras() != null) {
			Bundle bundle = intent.getExtras();
			movementTracking = (MovementTracking) bundle.getSerializable("TRACKING");

			startLocationListener();

			LocalBroadcastManager.getInstance(this).registerReceiver(broadCastReceiver,new IntentFilter(INTENT_ACTION));
		}

		return Service.START_STICKY;
	}

	/**
	 * Get Triggered when location is changed
	 */
	class UserDefinedBroadcastReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			if(intent.getAction() != null && intent.getAction().equals(INTENT_ACTION)) {
				Commons.print("Location listener On receive");
				sendLocation();
			}
		}
	}

	/**
	 * Start the Location listener. Use GPS_PROVIDER if GPS is enabled by user
	 * or it will use NERWORK_PROVIDER.
	 */
	public void startLocationListener() {

		timeup = false;
		isLocationUploaded = false;

		LocationUtil.getInstance(getApplicationContext()).startLocationListener();

		setTimer();
	}


	/*
	* Timer to wait for better location accuracy
	*/
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

	/*
	* Starts the upload service to sync Location details
	*/
	private void sendLocation() {
		try {

			Location location = LocationUtil.location;

			if(location != null){
				Float accuracy = location.getAccuracy();

				SharedPreferences pref = getApplicationContext().getSharedPreferences("TimePref", 0);
				int start_Time = pref.getInt("StartTime", 0);
				int end_Time = pref.getInt("EndTime", 0);

				count = count + 1;

				if (((accuracy) <= 10) || (count >= MAX_COUNT || timeup)) {

					isLocationUploaded = true;
					LocationUtil.getInstance(getApplicationContext()).stopLocationListener();

					int timeInHrs = LocationServiceHelper.getInstance().getCurrentTimeInHrs();
					if (timeInHrs >= start_Time && timeInHrs < end_Time) {

						if (LocationServiceHelper.getInstance().isMyServiceRunning(
								getApplicationContext(), LocationUploadService.class.getName())) {
							stopService(new Intent(getApplicationContext(),LocationUploadService.class));
						}

						Intent sendGpsServiceIntent = new Intent(getApplicationContext(),LocationUploadService.class);

						Bundle b = new Bundle();
						b.putSerializable("TRACKING",movementTracking);
						b.putParcelable("LOCATION",location);
						sendGpsServiceIntent.putExtras(b);

						startService(sendGpsServiceIntent);

					}

					stopService(new Intent(getApplicationContext(),LocationListenerService.class));
				}
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
}
