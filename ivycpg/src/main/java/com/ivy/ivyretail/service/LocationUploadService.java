package com.ivy.ivyretail.service;


import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.IBinder;

import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.util.Commons;


public class LocationUploadService extends Service {

	Context context;
	private BusinessModel bmodel;
	String[] currentLocation;
	
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	public int onStartCommand(Intent intent, int flags, int startId) {

		bmodel = (BusinessModel) getApplicationContext();
		context = getApplicationContext();
		try{
			currentLocation = intent.getExtras().getStringArray("Coordinates");
			new UploadUserLocation().execute();
		}catch(Exception e)
		{
			Commons.printException(""+e);
		}
		return Service.START_STICKY;
	}

	class UploadUserLocation extends AsyncTask<String, Integer, Boolean> {
		
		@Override
		protected Boolean doInBackground(String... arg0) {
			try {
				if(currentLocation != null){
					bmodel.saveUserLocation(currentLocation[0],currentLocation[1],currentLocation[2]);

					if (bmodel.isOnline()) {
						if(bmodel.isUserLocationAvailable()){
							bmodel.userMasterHelper.downloadUserDetails();
							bmodel.userMasterHelper.downloadDistributionDetails();
							bmodel.uploadLocationTracking();
						}
					}
				}
				return Boolean.TRUE;
			} catch (Exception e) {
				Commons.printException(""+e);
				return Boolean.FALSE;
			}

		}

		protected void onPreExecute() {

		}

		protected void onProgressUpdate(Integer... progress) {

		}

		protected void onPostExecute(Boolean result) {
			// result is the value returned from doInBackground
		}

	}

}
