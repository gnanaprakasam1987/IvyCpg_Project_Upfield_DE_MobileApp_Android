package com.ivy.ivyretail.service;

import android.Manifest;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.IBinder;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.Toast;

import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.util.Commons;

import java.util.Calendar;
import java.util.GregorianCalendar;

public class LocationListenerService extends Service {

	private BusinessModel bmodel;
	UserDefinedBroadcastReceiver broadCastReceiver = new UserDefinedBroadcastReceiver();
	private LocationManager locManager;
	private CustomLocationListener locListener;

	private static final long MAX_TIME = 120000;
	private static final int MAX_COUNT = 50;

	private boolean timeup = false, isLocationUploaded = false;
	private int count = 0;

	private String latitude = "";
	private String longitude = "";
	private Float accuracy;

	private int mLocationPermission;

	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}

	public int onStartCommand(Intent intent, int flags, int startId) {

		bmodel = (BusinessModel) getApplicationContext();

		startLocationListener();

		return Service.START_STICKY;
	}

	/**
	 * This method enables the Broadcast receiver for
	 * "android.intent.action.TIME_TICK" intent. This intent get broadcasted
	 * every minute.
	 *
	 * @param view
	 */
	public void registerBroadcastReceiver(View view) {

		this.registerReceiver(broadCastReceiver, new IntentFilter(
				"android.intent.action.TIME_TICK"));
		Toast.makeText(this, "Registered broadcast receiver",
				Toast.LENGTH_SHORT).show();
	}

	/**
	 * This method disables the Broadcast receiver
	 *
	 * @param view
	 */
	public void unregisterBroadcastReceiver(View view) {

		this.unregisterReceiver(broadCastReceiver);

		Toast.makeText(this, "unregistered broadcst receiver",
				Toast.LENGTH_SHORT).show();
	}

	class UserDefinedBroadcastReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {

			context.unregisterReceiver(broadCastReceiver);
			// bmodel.mapHelper.stopLocationListener();

			// You can do the processing here update the widget/remote views.
			StringBuilder msgStr = new StringBuilder("Current time : ");

			Toast.makeText(context, msgStr, Toast.LENGTH_SHORT).show();
		}
	}

	public class CustomLocationListener implements LocationListener {

		public void onLocationChanged(Location loc) {

			latitude = loc.getLatitude() + "";
			longitude = loc.getLongitude() + "";
			accuracy = loc.getAccuracy();

			SharedPreferences pref = getApplicationContext()
					.getSharedPreferences("TimePref", 0);
			int start_Time = pref.getInt("StartTime", 0);
			int end_Time = pref.getInt("EndTime", 0);

			count = count + 1;
			if (loc.getAccuracy() <= 10 || count >= MAX_COUNT || timeup) {

				isLocationUploaded = true;

				String[] coordinates = {latitude, longitude, accuracy + ""};
				stopLocationListener();
				int timeInHrs = getTimeInHrs();
				if (timeInHrs > start_Time && timeInHrs < end_Time) {
					//if (bmodel.isOnline()) { // Ignore if not online

						Context stopServiceContext = bmodel;
						String stopServiceClassStr = LocationUploadService.class
								.getName();
						if (BusinessModel.isMyServiceRunning(
								stopServiceContext, stopServiceClassStr)) {
							Intent stopServiceIntent = new Intent(
									stopServiceContext,
									LocationUploadService.class);
							stopServiceIntent.putExtra("Coordinates",
									coordinates);
							bmodel.stopService(stopServiceIntent);
						}
						// Call the notification downloader service.
						Intent sendGpsServiceIntent = new Intent(bmodel,
								LocationUploadService.class);
						sendGpsServiceIntent.putExtra("Coordinates",
								coordinates);
						bmodel.startService(sendGpsServiceIntent);

					/*} else {
						Toast.makeText(
								bmodel,
								getResources().getString(
										R.string.no_network_connection),
								Toast.LENGTH_LONG).show();
					}*/

				}

			}

		}

		public void onProviderDisabled(String provider) {
			// print "Currently GPS is Disabled";
		}

		public void onProviderEnabled(String provider) {
			// print "GPS got Enabled";
		}

		public void onStatusChanged(String provider, int status, Bundle extras) {
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
		setTimer();

		mLocationPermission = ContextCompat.checkSelfPermission(getApplicationContext(),
				Manifest.permission.ACCESS_FINE_LOCATION);

		if (mLocationPermission == PackageManager.PERMISSION_GRANTED) {

			locManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
			locListener = new CustomLocationListener();
			if (locManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {

				locManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0,
						0, locListener);
			} else if (locManager
					.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
				// alert will be here
				locManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,
						0, 0, locListener);
			}
		}


	}

	/**
	 * Stop the location listener.
	 */
	public void stopLocationListener() {

		mLocationPermission = ContextCompat.checkSelfPermission(getApplicationContext(),
				Manifest.permission.ACCESS_FINE_LOCATION);

		if (mLocationPermission == PackageManager.PERMISSION_GRANTED) {
			if (locManager != null && locListener != null) {
				locManager.removeUpdates(locListener);
				locListener = null;
				locManager = null;
			}
		}
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

	public void sendLocation() {
		try {
			stopLocationListener();

			SharedPreferences pref = getApplicationContext()
					.getSharedPreferences("TimePref", 0);
			int start_Time = pref.getInt("StartTime", 0);
			int end_Time = pref.getInt("EndTime", 0);

			count = count + 1;

			if ((accuracy) <= 10 || count >= MAX_COUNT || timeup) {
				String[] coordinates = {latitude, longitude, accuracy + ""};

				int timeInHrs = getTimeInHrs();
				if (timeInHrs > start_Time && timeInHrs < end_Time) {
					//if (bmodel.isOnline()) { // Ignore if not online

						Context stopServiceContext = bmodel;
						String stopServiceClassStr = LocationUploadService.class
								.getName();
						if (BusinessModel.isMyServiceRunning(
								stopServiceContext, stopServiceClassStr)) {
							Intent stopServiceIntent = new Intent(
									stopServiceContext,
									LocationUploadService.class);
							stopServiceIntent.putExtra("Coordinates",
									coordinates);
							bmodel.stopService(stopServiceIntent);
						}
						// Call the notification downloader service.
						Intent sendGpsServiceIntent = new Intent(bmodel,
								LocationUploadService.class);
						sendGpsServiceIntent.putExtra("Coordinates",
								coordinates);
						bmodel.startService(sendGpsServiceIntent);

					/*} else {
						Toast.makeText(
								bmodel,
								getResources().getString(
										R.string.no_network_connection),
								Toast.LENGTH_LONG).show();
					}*/

				}

			}
		} catch (Exception e) {
			// TODO: handle exception
		}
	}
}
