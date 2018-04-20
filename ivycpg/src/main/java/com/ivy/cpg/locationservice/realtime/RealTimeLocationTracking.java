package com.ivy.cpg.locationservice.realtime;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Toast;

import com.ivy.cpg.locationservice.LocationServiceHelper;
import com.ivy.cpg.locationservice.activitytracking.ActivityRecognitionService;
import com.ivy.sd.png.asean.view.R;

public class RealTimeLocationTracking {

    public static final int STATUS_SUCCESS = 0;  // Service started Successfully
    public static final int STATUS_LOCATION_PERMISSION = 1; // Location Permission is not enabled
    public static final int STATUS_GPS = 2; // GPS Not enabled
    public static final int STATUS_LOCATION_ACCURACY = 3; // Location Accuracy level is low
    public static final int STATUS_MOCK_LOCATION = 4; // Mock Location is enabled
    public static final int STATUS_SERVICE_ERROR = 5; // Problem in starting Service

    public static int startLocationTracking(RealTimeLocation realTimeLocation, Context context){

        //Check whether location permission is enabled
        if(!LocationServiceHelper.getInstance().hasLocationPermissionEnabled(context)){
            Toast.makeText(context,
                    context.getResources().getString(R.string.permission_enable_msg),
                    Toast.LENGTH_SHORT).show();
            return STATUS_LOCATION_PERMISSION;
        }

        //Check GPS is Enabled or not
        if(!LocationServiceHelper.getInstance().isGpsEnabled(context)) {
            Toast.makeText(context,
                    context.getString(R.string.enable_gps),
                    Toast.LENGTH_SHORT).show();
            return STATUS_GPS;
        }

        //Checks whether if location accuracy is not set as high
        if (!LocationServiceHelper.getInstance().isLocationHighAccuracyEnabled(context)) {
            Toast.makeText(context, "Change high location Accuracy", Toast.LENGTH_SHORT).show();
            return STATUS_LOCATION_ACCURACY;
        }

        //Check whether Mock Location is enabled or not
        if (!LocationServiceHelper.getInstance().isMockSettingsON(context)) {
            Toast.makeText(context, "Mock Location is Enabled", Toast.LENGTH_SHORT).show();
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
        if(context.startService(intent) != null) {

//        update the status as true if user started work
            updateWorkStatus(context, true);


            if(!LocationServiceHelper.getInstance().isMyServiceRunning(context, ActivityRecognitionService.class.getName())){
                context.startService(new Intent(context, ActivityRecognitionService.class));
            }

            return STATUS_SUCCESS;
        }

        return STATUS_SERVICE_ERROR;
    }

    public static void stopLocationTracking(Context context){
//        update the status as false if user Paused or completed
        updateWorkStatus(context, false);

        Intent intent = new Intent(context, RealTimeLocationService.class);
        context.stopService(intent);

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
