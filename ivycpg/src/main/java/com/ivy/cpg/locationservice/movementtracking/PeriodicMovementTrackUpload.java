package com.ivy.cpg.locationservice.movementtracking;

import android.content.Context;
import android.content.SharedPreferences;
import android.location.Location;

import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.util.Commons;

public class PeriodicMovementTrackUpload implements MovementTracking{

    public PeriodicMovementTrackUpload(){

    }

    @Override
    public void uploadLocationDetails(Context context,Location location) {
        BusinessModel bmodel = (BusinessModel)context;
        try {
            if(location != null){
                bmodel.saveUserLocation(
                        String.valueOf(location.getLatitude()),
                        String.valueOf(location.getLongitude()),
                        String.valueOf(location.getAccuracy()));

                if (bmodel.isOnline()) {
                    if(bmodel.isUserLocationAvailable()){
                        bmodel.userMasterHelper.downloadUserDetails();
                        bmodel.userMasterHelper.downloadDistributionDetails();
                        bmodel.uploadLocationTracking();
                    }
                }
            }
        } catch (Exception e) {
            Commons.printException(""+e);
        }
    }
}
