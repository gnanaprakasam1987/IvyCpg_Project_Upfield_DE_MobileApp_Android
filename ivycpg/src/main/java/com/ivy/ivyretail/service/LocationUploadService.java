package com.ivy.ivyretail.service;


import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.Nullable;

import com.ivy.sd.png.util.Commons;


public class LocationUploadService extends IntentService {

	Context context;
	String[] currentLocation;

	public LocationUploadService() {
		super(LocationUploadService.class.getName());
	}

	@Override
	protected void onHandleIntent(@Nullable Intent intent) {
		context = getApplicationContext();
		try{
			if(intent!=null && intent.getExtras()!=null) {
				currentLocation = intent.getExtras().getStringArray("Coordinates");

				BackgroundServiceHelper.getInstance(context).uploadUserLocation(currentLocation);
			}
		}catch(Exception e)
		{
			Commons.printException(""+e);
		}
	}
}
