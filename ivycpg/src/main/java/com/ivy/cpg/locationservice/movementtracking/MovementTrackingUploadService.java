package com.ivy.cpg.locationservice.movementtracking;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import androidx.annotation.Nullable;

import com.ivy.cpg.locationservice.LocationDetailBO;
import com.ivy.cpg.locationservice.LocationServiceHelper;
import com.ivy.sd.png.bo.UserMasterBO;
import com.ivy.sd.png.util.Commons;
import com.ivy.utils.NetworkUtils;

import static com.ivy.sd.png.util.DataMembers.tbl_location_tracking;

public class MovementTrackingUploadService extends IntentService {


    public MovementTrackingUploadService() {
        super(MovementTrackingUploadService.class.getName());
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        Context context = getApplicationContext();
        try {
            if (intent != null && intent.getExtras() != null) {
                LocationDetailBO location = (LocationDetailBO) intent.getSerializableExtra("LOCATION");
                if (location != null) {

                    UserMasterBO userMasterBO = LocationServiceHelper.getInstance().downloadUserDetails(context);

                    if(userMasterBO != null) {
                        LocationServiceHelper.getInstance().saveUserLocation(context, location, userMasterBO );
                        if (NetworkUtils.isNetworkConnected(context) &&
                                LocationServiceHelper.getInstance().isUserLocationAvailable(context,tbl_location_tracking)) {
                            LocationServiceHelper.getInstance().uploadLocationTrackingAws(context,userMasterBO,"UPLDTRAN",false);
                        }
                    }
                }
            }
        } catch (Exception e) {
            Commons.printException("" + e);
        }
    }
}
