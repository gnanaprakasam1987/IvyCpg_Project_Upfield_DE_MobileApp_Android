package com.ivy.ivyretail.service;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.util.Commons;

import org.jetbrains.annotations.NotNull;

public class LocationUtil implements LocationListener, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private static final String TAG = "LocationUtil";

    private Context context;
    private static LocationUtil instance = null;

    /* Fused location provide using Google play service initialisation.*/
    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;

    /* Native location provider */
    private LocationManager nativeLocationManager;
    private MyLocationListener nativeLocationListener;

    /* Values from location services */
    public static double latitude = 0;
    public static double longitude = 0;
    public static float accuracy = 0;
    private static String mProviderName;


    protected LocationUtil(Context context) {
        this.context = context;
    }

    public static LocationUtil getInstance(Context ctx) {
        if (instance == null) {
            instance = new LocationUtil(ctx);
        }
        return instance;
    }


    /**
     * To start the location listener.
     */
    @TargetApi(23)
    public void startLocationListener() {

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

            Log.d(TAG, " Fused Api fired ..............");

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

                if (nativeLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                    nativeLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                            0, 0, nativeLocationListener);
                    mProviderName = LocationManager.GPS_PROVIDER;
                    Log.d(TAG, "GPS_PROVIDER Listener started");
                } else if (nativeLocationManager
                        .isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                    nativeLocationManager.requestLocationUpdates(
                            LocationManager.NETWORK_PROVIDER, 0, 0, nativeLocationListener);
                    mProviderName = LocationManager.NETWORK_PROVIDER;
                    Log.d(TAG, "NETWORK_PROVIDER Listener started");
                }
                Log.d(TAG, "Native Location Manager fired.");
            } catch (SecurityException e) {
                Commons.printException(e);
            } catch (Exception e) {
                Commons.printException(e);
            }

        }
    }

    class MyLocationListener implements android.location.LocationListener {
        public void onLocationChanged(Location loc) {

            latitude = loc.getLatitude();
            longitude = loc.getLongitude();
            accuracy = loc.getAccuracy();
            mProviderName = loc.getProvider();

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
                    mGoogleApiClient.disconnect();
                    latitude = 0;
                    longitude = 0;
                    stopLocationUpdates();
                }
                Log.d(TAG,
                        "isConnected ...............: "
                                + mGoogleApiClient.isConnected());
                Log.d(TAG, "onStop fired ..............");
            }


        } else {
            if (nativeLocationManager != null && nativeLocationListener != null) {
                nativeLocationManager.removeUpdates(nativeLocationListener);
                nativeLocationListener = null;
                nativeLocationManager = null;
                latitude = 0;
                longitude = 0;
                Log.d(TAG, "Location Manager GPS listener Stopped");
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
        Log.d(TAG, "onConnected - isConnected ...............: "
                + mGoogleApiClient.isConnected());
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

        latitude = location.getLatitude();
        longitude = location.getLongitude();
        accuracy = location.getAccuracy();
        mProviderName = location.getProvider();

    }

    @Override
    public void onConnectionFailed(@NotNull ConnectionResult connectionResult) {

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
            Log.d(TAG, " Provider===> " + mProviderName + " Location Latitude ===> " + latitude + " Longitude===> " + longitude);

            Log.d(TAG, "Location update started ..............: ");
        }
    }

    private void stopLocationUpdates() {
        if (mGoogleApiClient.isConnected()) {
            LocationServices.FusedLocationApi.removeLocationUpdates(
                    mGoogleApiClient, this);
            Log.d(TAG, "Location update stopped .......................");
        }
    }


}
