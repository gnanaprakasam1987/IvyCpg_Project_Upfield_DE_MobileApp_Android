package com.ivy.cpg.locationservice.movementtracking;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.Nullable;

import com.ivy.cpg.locationservice.LocationDetailBO;
import com.ivy.sd.png.bo.UserMasterBO;
import com.ivy.sd.png.util.Commons;

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

                    UserMasterBO userMasterBO = MovementTrackingUploadHelper.getInstance().downloadUserDetails(context);

                    if(userMasterBO != null) {
                        MovementTrackingUploadHelper.getInstance().saveUserLocation(context, location, userMasterBO );
                        if (MovementTrackingUploadHelper.getInstance().isOnline(context) && MovementTrackingUploadHelper.getInstance().isUserLocationAvailable(context)) {
                                MovementTrackingUploadHelper.getInstance().uploadLocationTracking(context,userMasterBO);
                        }
                    }
                }
            }
        } catch (Exception e) {
            Commons.printException(""+e);
        }
    }
}
