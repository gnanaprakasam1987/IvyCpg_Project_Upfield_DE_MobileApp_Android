package com.ivy.cpg.locationservice.movementtracking;


import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.support.annotation.Nullable;

import com.ivy.ivyretail.service.BackgroundServiceHelper;
import com.ivy.sd.png.util.Commons;


public class LocationUploadService extends IntentService {

	Context context;

	public LocationUploadService() {
		super(LocationUploadService.class.getName());
	}

	@Override
	protected void onHandleIntent(@Nullable Intent intent) {
		context = getApplicationContext();
		try{
			if(intent!=null && intent.getExtras()!=null) {

				MovementTracking movementTracking = (MovementTracking) intent.getSerializableExtra("TRACKING");
				Location location = intent.getParcelableExtra("LOCATION");

				movementTracking.uploadLocationDetails(getApplicationContext(),location);
			}
		}catch(Exception e)
		{
			Commons.printException(""+e);
		}
	}
}
