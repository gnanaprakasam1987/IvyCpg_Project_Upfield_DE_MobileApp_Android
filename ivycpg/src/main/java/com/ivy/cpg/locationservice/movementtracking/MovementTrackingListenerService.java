package com.ivy.cpg.locationservice.movementtracking;

import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.IBinder;
import androidx.annotation.NonNull;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.google.android.gms.location.ActivityRecognitionClient;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.ivy.cpg.locationservice.LocationDetailBO;
import com.ivy.cpg.locationservice.LocationServiceHelper;
import com.ivy.cpg.locationservice.activitytracking.ActivityIntentService;
import com.ivy.sd.png.util.Commons;

public class MovementTrackingListenerService extends Service {

	private static final long MAX_TIME = 90000;

	private boolean timeup = false, isLocationUploaded = false;
	private int count = 0;

	/**
	 * Broadcast Receiver for receiving Activity Recognition
	 */
	private ActivityBroadcastReceiver activityBroadcastReceiver = new ActivityBroadcastReceiver();
	private final String BROADCAST_DETECTED_ACTIVITY = "com.ivy.BROADCAST_DETECTED_ACTIVITY";
	private String activityName = "";

	/**
	 * Broadcast Receiver for receiving location
	 */
	UserDefinedBroadcastReceiver broadCastReceiver = new UserDefinedBroadcastReceiver();
	private final String INTENT_ACTION ="LOCATION CAPTURED";

	//Activity Recognition Declaration
	private PendingIntent mPendingIntent;
	private ActivityRecognitionClient mActivityRecognitionClient;


	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public void onCreate() {
		super.onCreate();
	}

	public int onStartCommand(Intent intent, int flags, int startId) {

		startLocationListener();
		LocalBroadcastManager.getInstance(this).registerReceiver(broadCastReceiver,new IntentFilter(INTENT_ACTION));

		LocalBroadcastManager.getInstance(this).registerReceiver(activityBroadcastReceiver,
				new IntentFilter(BROADCAST_DETECTED_ACTIVITY));

		//Activity Recognition client initialization and registering pending intent to receive result
		mActivityRecognitionClient = new ActivityRecognitionClient(this);
		Intent mIntentService = new Intent(this, ActivityIntentService.class);
		mPendingIntent = PendingIntent.getService(this, 11, mIntentService, PendingIntent.FLAG_UPDATE_CURRENT);
		requestActivityUpdatesButtonHandler();

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

		MovementTrackingLocationUtil.getInstance(getApplicationContext()).startLocationListener();

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

			Location location = MovementTrackingLocationUtil.location;

			if(location != null){
				Float accuracy = location.getAccuracy();

				SharedPreferences pref = getApplicationContext().getSharedPreferences("TimePref", 0);
				int start_Time = pref.getInt("StartTime", 0);
				int end_Time = pref.getInt("EndTime", 0);

				count = count + 1;

				if (accuracy <= 15 || timeup) {

					isLocationUploaded = true;
					MovementTrackingLocationUtil.getInstance(getApplicationContext()).stopLocationListener();

					int timeInHrs = LocationServiceHelper.getInstance().getCurrentTimeInHrs();
					if (timeInHrs >= start_Time && timeInHrs < end_Time) {

						Intent sendGpsServiceIntent = new Intent(getApplicationContext(),MovementTrackingUploadService.class);

						boolean isGpsEnabled = LocationServiceHelper.getInstance().notifyGPSStatus(getApplicationContext());
						//Notifies if Mock Location is enabled
						boolean isMockLocationEnabled = LocationServiceHelper.getInstance().notifyMockLocationStatus(getApplicationContext(),location);

						LocationDetailBO locationDetailBO = new LocationDetailBO();
						locationDetailBO.setLatitude(String.valueOf(location.getLatitude()));
						locationDetailBO.setLongitude(String.valueOf(location.getLongitude()));
						locationDetailBO.setAccuracy(String.valueOf(location.getAccuracy()));
						locationDetailBO.setTime(String.valueOf(System.currentTimeMillis()));
						locationDetailBO.setActivityType(activityName);
						locationDetailBO.setGpsEnabled(isGpsEnabled);
						locationDetailBO.setMockLocationEnabled(isMockLocationEnabled);
						locationDetailBO.setProvider(location.getProvider());
						locationDetailBO.setBatteryStatus(LocationServiceHelper.getInstance().getBatteryPercentage(getApplicationContext()));

						Bundle b = new Bundle();
						b.putSerializable("LOCATION",locationDetailBO);
						b.putString("Activity",activityName);
						sendGpsServiceIntent.putExtras(b);

						// Start intent service to upload location details
						if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
							startForegroundService(sendGpsServiceIntent);
						}else
							startService(sendGpsServiceIntent);

						//Stop the Movement track listener service after location send
						stopService(new Intent(getApplicationContext(),MovementTrackingListenerService.class));

					}
				}
			}
		} catch (Exception e) {
			Commons.printException(e);
		}
	}

	class ActivityBroadcastReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent.getAction() != null && intent.getAction().equals(BROADCAST_DETECTED_ACTIVITY)) {
				activityName = intent.getStringExtra("type");
			}
		}
	}

	/**
	 * Starting the Activity Recognition Listener
	 */
	public void requestActivityUpdatesButtonHandler() {
		int DETECTION_INTERVAL_IN_MILLISECONDS = 5000;
		Task<Void> task = mActivityRecognitionClient.requestActivityUpdates(
				DETECTION_INTERVAL_IN_MILLISECONDS,
				mPendingIntent);

		task.addOnSuccessListener(new OnSuccessListener<Void>() {
			@Override
			public void onSuccess(Void result) {
				Commons.print("Successfully requested activity updates");
			}
		});

		task.addOnFailureListener(new OnFailureListener() {
			@Override
			public void onFailure(@NonNull Exception e) {
				Commons.print("Requesting activity updates failed to start");
			}
		});
	}

	/**
	 * Stopping the Activity Recognition Listener
	 */
	public void removeActivityUpdatesButtonHandler() {

		try {
			if (mActivityRecognitionClient != null) {
				Task<Void> task = mActivityRecognitionClient.removeActivityUpdates(
						mPendingIntent);
				task.addOnSuccessListener(new OnSuccessListener<Void>() {
					@Override
					public void onSuccess(Void result) {
						Commons.print("Removed activity updates successfully");
					}
				});

				task.addOnFailureListener(new OnFailureListener() {
					@Override
					public void onFailure(@NonNull Exception e) {
						Commons.print("Failed to remove activity updates");
					}
				});
			}
		}catch (Exception e){
			Commons.printException(e);
		}
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		LocalBroadcastManager.getInstance(this).unregisterReceiver(broadCastReceiver);
		LocalBroadcastManager.getInstance(this).unregisterReceiver(activityBroadcastReceiver);
		removeActivityUpdatesButtonHandler();
	}
}
