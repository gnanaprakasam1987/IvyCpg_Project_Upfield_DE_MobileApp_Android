package com.ivy.ivyretail.service;

import android.content.Context;

import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.util.Commons;

public class BackgroundServiceHelper {

    private static BackgroundServiceHelper instance = null;
    private BusinessModel bmodel;

    private BackgroundServiceHelper(Context context) {

        this.bmodel = (BusinessModel) context.getApplicationContext();
    }

    public static BackgroundServiceHelper getInstance(Context context) {
        if (instance == null) {
            instance = new BackgroundServiceHelper(context);
        }
        return instance;
    }

    void uploadUserLocation(String[] currentLocation){
        Commons.print("LOCATION "+currentLocation[0]+" - "+currentLocation[1]+" - "+currentLocation[2]);

        try {
            if(currentLocation.length > 0){
                bmodel.saveUserLocation(currentLocation[0],currentLocation[1],currentLocation[2]);

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
