package com.ivy.cpg.locationservice.movementtracking;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.ivy.cpg.locationservice.LocationServiceHelper;
import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.util.Commons;


public class MovementTrackingLocationUtil implements LocationListener, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private Context context;
    private static MovementTrackingLocationUtil instance = null;

    /* Fused location provide using Google play service initialisation.*/
    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;

    /* Native location provider */
    private LocationManager nativeLocationManager;
    private MyLocationListener nativeLocationListener;

    /* Values from location services */
    public static Location location;

    private Location previousBestLocation = null;

    protected MovementTrackingLocationUtil(Context context) {
        this.context = context;
    }

    public static MovementTrackingLocationUtil getInstance(Context ctx) {
        if (instance == null) {
            instance = new MovementTrackingLocationUtil(ctx);
        }
        return instance;
    }


    /**
     * To start the location listener.
     */
    @TargetApi(23)
    public void startLocationListener() {

        stopLocationListener();

        /* Notify user that google play service is not available */
        if (!isGooglePlayServicesAvailable()) {
            Toast.makeText(context,
                    "Google play service not available.",
                    Toast.LENGTH_LONG).show();
        }

        if (isGooglePlayServicesAvailable()) {
            mGoogleApiClient = new GoogleApiClient.Builder(context)
                    .addApi(LocationServices.API).addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this).build();
            mGoogleApiClient.connect();
            createLocationRequest();

//            Commons.print("AlarmManager Fused Api fired ..............");

        } else {
            try {

                if (Build.VERSION.SDK_INT >= 23 &&
                        ContextCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                        ContextCompat.checkSelfPermission(context, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(context,
                            context.getResources().getString(R.string.permission_enable_msg),
                            Toast.LENGTH_LONG).show();
                    return;
                }


                nativeLocationManager = (LocationManager) context
                        .getSystemService(Context.LOCATION_SERVICE);
                nativeLocationListener = new MyLocationListener();

//                Commons.print("AlarmManager Native Location Manager fired.");
            } catch (SecurityException e) {
                Commons.printException(e);
            } catch (Exception e) {
                Commons.printException(e);
            }

        }
    }

    class MyLocationListener implements android.location.LocationListener {
        public void onLocationChanged(Location loc) {

            //Notifies if GPS is Disabled
            LocationServiceHelper.getInstance().notifyGPSStatus(context);
            //Notifies if Mock Location is enabled
            LocationServiceHelper.getInstance().notifyMockLocationStatus(context,loc);

            location = loc;

            if(isBetterLocation(loc,previousBestLocation)) {

                Intent locationIntent = new Intent("LOCATION CAPTURED");
                LocalBroadcastManager.getInstance(context).sendBroadcast(locationIntent);
            }

        }

        public void onProviderDisabled(String provider) {
            // print "Currently GPS is Disabled";
        }

        public void onProviderEnabled(String provider) {
            // print "GPS got Enabled";
        }

        public void onStatusChanged(String provider, int status, Bundle extras) {
        }
    }


    /**
     * Stop the location listener.
     */
    public void stopLocationListener() {

        if (isGooglePlayServicesAvailable()) {
            if (mGoogleApiClient != null) {
                if (mGoogleApiClient.isConnected()) {
                    stopLocationUpdates();
                    mGoogleApiClient.disconnect();
                    location = null;
                }
            }

        } else {
            if (nativeLocationManager != null && nativeLocationListener != null) {
                nativeLocationManager.removeUpdates(nativeLocationListener);
                nativeLocationListener = null;
                nativeLocationManager = null;
                location = null;
            }
        }
    }

    /**
     * Check google play service is available or not.
     *
     * @return true if available
     */
    private boolean isGooglePlayServicesAvailable() {

        int status = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(context);
        return ConnectionResult.SUCCESS == status;

    }

    private void createLocationRequest() {

        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(1000);
        mLocationRequest.setFastestInterval(1000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    @Override
    public void onConnected(Bundle bundle) {
        if (!mGoogleApiClient.isConnected()) {
            mGoogleApiClient.connect();
        }
        startLocationUpdates();
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onLocationChanged(Location location) {

        //Notifies if GPS is Disabled
        LocationServiceHelper.getInstance().notifyGPSStatus(context);
        //Notifies if Mock Location is enabled
        LocationServiceHelper.getInstance().notifyMockLocationStatus(context,location);


        this.location = location;

        if(isBetterLocation(location,previousBestLocation)){
//            Commons.print("AlarmManager Better Location found");

            Intent locationIntent = new Intent("LOCATION CAPTURED");
            LocalBroadcastManager.getInstance(context).sendBroadcast(locationIntent);
        }

    }

    @Override
    public void onConnectionFailed( ConnectionResult connectionResult) {

    }

    private void startLocationUpdates() {
        if (mGoogleApiClient.isConnected()) {
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            LocationServices.FusedLocationApi
                    .requestLocationUpdates(mGoogleApiClient, mLocationRequest,
                            this);
        }
    }

    private void stopLocationUpdates() {
        if (mGoogleApiClient.isConnected()) {
            LocationServices.FusedLocationApi.removeLocationUpdates(
                    mGoogleApiClient, this);
//            Commons.print("AlarmManager Location update stopped .......................");
        }
    }


    private boolean isBetterLocation(Location location, Location currentBestLocation) {
        if (currentBestLocation == null) {
            // A new location is always better than no location
            previousBestLocation = location;
            return true;
        }

        boolean isBetterLocation = false;

        // Check whether the new location fix is newer or older
        long timeDelta = location.getTime() - currentBestLocation.getTime();

        boolean isNewer = timeDelta > 0;

        // Check whether the new location fix is more or less accurate
        int accuracyDelta = (int) (location.getAccuracy() - currentBestLocation.getAccuracy());
        boolean isLessAccurate = accuracyDelta > 0;
        boolean isMoreAccurate = accuracyDelta < 0;
//        Commons.print("AlarmManager location -- "+location);
//        Commons.print("AlarmManager currentBestLocation -- "+currentBestLocation);
//        Commons.print("AlarmManager isMoreAccurate "+accuracyDelta);

        // Determine location quality using a combination of timeliness and accuracy
        if (isMoreAccurate) {
            isBetterLocation =  true;
            previousBestLocation = location;
        }
        else if (isNewer && !isLessAccurate) {
            isBetterLocation = true;
            previousBestLocation = location;
        }

        return isBetterLocation;
    }

    /** Checks whether two providers are the same */
    private boolean isSameProvider(String provider1, String provider2) {
        if (provider1 == null) {
            return provider2 == null;
        }
        return provider1.equals(provider2);
    }


}
