package com.ivy.cpg.locationservice.realtime;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.widget.Toast;

import com.ivy.cpg.locationservice.LocationConstants;
import com.ivy.cpg.locationservice.LocationServiceHelper;
import com.ivy.sd.png.asean.view.R;

import static com.ivy.cpg.locationservice.LocationConstants.*;

public class RealTimeLocationTracking {

    public static int startLocationTracking(RealTimeLocation realTimeLocation, Context context){

        //Check whether location permission is enabled
        if(!LocationServiceHelper.getInstance().hasLocationPermissionEnabled(context)){
            Toast.makeText(context,
                    "RTM : "+context.getResources().getString(R.string.permission_enable_msg),
                    Toast.LENGTH_SHORT).show();
            return LocationConstants.STATUS_LOCATION_PERMISSION;
        }

        //Check GPS is Enabled or not
        if(!LocationServiceHelper.getInstance().isGpsEnabled(context)) {
            Toast.makeText(context,
                    "RTM : "+context.getString(R.string.enable_gps),
                    Toast.LENGTH_SHORT).show();
            return STATUS_GPS;
        }

        //Checks whether if location accuracy is not set as high
        if (!LocationServiceHelper.getInstance().isLocationHighAccuracyEnabled(context)) {
            Toast.makeText(context, "RTM : "+context.getString(R.string.status_location_accuracy), Toast.LENGTH_SHORT).show();
            return STATUS_LOCATION_ACCURACY;
        }

        //Check whether Mock Location is enabled or not
        if (!LocationServiceHelper.getInstance().isMockSettingsON(context)) {
            Toast.makeText(context, "RTM : "+context.getString(R.string.mock_location_enabled), Toast.LENGTH_SHORT).show();
            return STATUS_MOCK_LOCATION;
        }

        Intent intent = new Intent(context, RealTimeLocationService.class);
        Bundle b = new Bundle();
        b.putSerializable("REALTIME",realTimeLocation);
        intent.putExtras(b);

        //Stops the service if already running
        if(LocationServiceHelper.getInstance().isMyServiceRunning(context, RealTimeLocationService.class.getName())){
            context.stopService(intent);
        }

        //starts the service
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            //starts the service
            if(context.startForegroundService(intent) != null) {

                //update the status as true if user Started work
                updateWorkStatus(context, true);

                return STATUS_SUCCESS;
            }
        }else{
            //starts the service
            if(context.startService(intent) != null) {

                //update the status as true if user Started work
                updateWorkStatus(context, true);

                return STATUS_SUCCESS;
            }
        }

        return STATUS_SERVICE_ERROR;
    }

    public static void stopLocationTracking(Context context){
        //update the status as false if user Paused or completed
        updateWorkStatus(context, false);

        //Stopping Realtime Location listener service
        context.stopService(new Intent(context, RealTimeLocationService.class));

    }

    /*
    * Update the Work Status in preferences
    */
    private static void updateWorkStatus(Context context, boolean b) {
        SharedPreferences pref = context.getSharedPreferences("TimePref", 0);
        SharedPreferences.Editor editor = pref.edit();
        editor.putBoolean("INWORK", b);
        editor.apply();
    }


}
