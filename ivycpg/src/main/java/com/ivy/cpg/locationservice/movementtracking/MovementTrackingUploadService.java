package com.ivy.cpg.locationservice.movementtracking;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.Nullable;

import com.ivy.cpg.locationservice.LocationDetailBO;
import com.ivy.cpg.locationservice.LocationServiceHelper;
import com.ivy.sd.png.bo.UserMasterBO;
import com.ivy.sd.png.util.Commons;
import com.ivy.utils.NetworkUtils;

public class MovementTrackingUploadService extends IntentService{


    public MovementTrackingUploadService(){
        super(MovementTrackingUploadService.class.getName());
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        Context context = getApplicationContext();
        try {
            if(intent!=null && intent.getExtras()!=null) {
                LocationDetailBO location = (LocationDetailBO)intent.getSerializableExtra("LOCATION");
                if (location != null) {

                    UserMasterBO userMasterBO = LocationServiceHelper.getInstance().downloadUserDetails(context);

                    if(userMasterBO != null) {
                        LocationServiceHelper.getInstance().saveUserLocation(context, location, userMasterBO );
                        if (NetworkUtils.isNetworkConnected(context) &&
                                LocationServiceHelper.getInstance().isUserLocationAvailable(context,"LocationTracking")) {
                            LocationServiceHelper.getInstance().uploadLocationTracking(context,userMasterBO);
                        }
                    }
                }
            }
        } catch (Exception e) {
            Commons.printException(""+e);
        }
    }
}
