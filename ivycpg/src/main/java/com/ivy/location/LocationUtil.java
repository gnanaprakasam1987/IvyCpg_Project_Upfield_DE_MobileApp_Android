
package com.ivy.location;

import android.content.Context;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

public class LocationUtil  implements LocationListener, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    static final String TAG = "LocationUtil";
    public static int gpsconfigcode;
    public static String mProviderName;
    public static double latitude = 0;
    public static double longitude = 0;
    public static float accuracy = 0;
    private LocationManager locManager;
    GoogleApiClient mGoogleApiClient;
    LocationRequest mLocationRequest;    
    private Context context;
    private static LocationUtil instance = null;
    private MyLocationListener locListener;
    private LocationUpdater iLocationUpdater;
    public static boolean isMockLocation;

    protected LocationUtil(Context context) {
        this.context = context;
    }

    public static LocationUtil getInstance(Context ctx) {
        if (instance == null) {
            instance = new LocationUtil(ctx);
        }
        return instance;
    }

    public void instantiateLocationUpdater(Fragment fragment) {
        try {
            if (fragment instanceof LocationUpdater) {
                this.iLocationUpdater = (LocationUpdater) fragment;
            }
        } catch (ClassCastException e) {
            Log.e(fragment.toString(),
                    "Must implement LocationUpdaterInterface");
        }

    }

    /*
        * Define a request code to send to Google Play services This code is
        * returned in Activity.onActivityResult
        */
    public final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;
    /**
     * Constants for location update parameters
     */
    // Milliseconds per second
    public static final int MILLISECONDS_PER_SECOND = 1000;
    // The update interval
    public static final int UPDATE_INTERVAL_IN_SECONDS = 60;
    // A fast interval ceiling
    public static final int FAST_CEILING_IN_SECONDS = 1;
    // Update interval in milliseconds
    public static final long UPDATE_INTERVAL_IN_MILLISECONDS = MILLISECONDS_PER_SECOND
            * UPDATE_INTERVAL_IN_SECONDS;
    // A fast ceiling of update intervals, used when the app is visible
    public static final long FAST_INTERVAL_CEILING_IN_MILLISECONDS = MILLISECONDS_PER_SECOND
            * FAST_CEILING_IN_SECONDS;


    public void startLocationListener() {

        if (!isGooglePlayServicesAvailable()) {
            Toast.makeText(context,
                    "There is no googleplay service in this device",
                    Toast.LENGTH_LONG).show();
        }
        
       
        
        if ((gpsconfigcode==0||gpsconfigcode==3)&&isGooglePlayServicesAvailable()) {
            mGoogleApiClient = new GoogleApiClient.Builder(context)
                    .addApi(LocationServices.API).addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this).build();
            mGoogleApiClient.connect();
            
            createLocationRequest();
            Log.d(TAG, " Fused Api fired ..............");
        } else {
        	
            locManager = (LocationManager) context
                    .getSystemService(Context.LOCATION_SERVICE);
            locListener = new MyLocationListener();           
            
            if (locManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                locManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                        0, 0, locListener);
                mProviderName = LocationManager.GPS_PROVIDER;
                Log.d(TAG, "GPS_PROVIDER Listener started");
            } else if (locManager
                    .isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                locManager.requestLocationUpdates(
                        LocationManager.NETWORK_PROVIDER, 0, 0, locListener);
                mProviderName = LocationManager.NETWORK_PROVIDER;
                Log.d(TAG, "NETWORK_PROVIDER Listener started");
            }
            Log.d(TAG, "Location Manager fired ..............");
        }
    }

    class MyLocationListener implements android.location.LocationListener {
        public void onLocationChanged(Location loc) {

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                if (loc.isFromMockProvider()) {
                    isMockLocation=true;
                    Log.d(TAG, "Mock Location Manager latitude :" + latitude + ",Location Manager longitude :" + longitude);
                }
                else {
                    isMockLocation=false;
                    Log.d(TAG, "Location Manager latitude :" + latitude + ",Location Manager longitude :" + longitude);
                }
            }
            else
                Log.d(TAG, "Location Manager latitude :" + latitude + ",Location Manager longitude :" + longitude);

            latitude = loc.getLatitude();
            longitude = loc.getLongitude();
            accuracy = loc.getAccuracy();
            mProviderName = loc.getProvider();
            if (iLocationUpdater != null)
                iLocationUpdater.locationUpdate();


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
     * Check GPS is Enable on not
     */
    public boolean isGPSProviderEnabled() {
        locManager = (LocationManager) context
                .getSystemService(Context.LOCATION_SERVICE);
        return locManager.isProviderEnabled(LocationManager.GPS_PROVIDER);

    }

    /**
     * Start the Location listener. Use GPS_PROVIDER if GPS is enabled by user
     * or it will use NERWORK_PROVIDER.
     */

    /**
     * Stop the location listener.
     */
    public void stopLocationListener() {

        if ((gpsconfigcode == 0 || gpsconfigcode == 3)&&isGooglePlayServicesAvailable()) {
         if(mGoogleApiClient!=null){
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
            if (locManager != null && locListener != null) {
                locManager.removeUpdates(locListener);
                locListener = null;
                locManager = null;
                latitude = 0;
                longitude = 0;
                Log.d(TAG, "Location Manager GPS listener Stopped");
            }
        }


    }

    private boolean isGooglePlayServicesAvailable() {
        int status = GooglePlayServicesUtil.isGooglePlayServicesAvailable(context);
        if (ConnectionResult.SUCCESS == status) {
            return true;
        } else {
//            GooglePlayServicesUtil.getErrorDialog(status, LocationUtil.this, 0).show();
            return false;

        }
    }

    protected void createLocationRequest() {

        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(1000);
        mLocationRequest.setFastestInterval(1000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    @Override
    public void onConnected(Bundle bundle) {
        Log.d(TAG, "onConnected - isConnected ...............: "
                + mGoogleApiClient.isConnected());
        if (mGoogleApiClient.isConnected() == false) {
            mGoogleApiClient.connect();
        }
        startLocationUpdates();
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onLocationChanged(Location location) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            if (location.isFromMockProvider()) {
                isMockLocation=true;
                Log.d(TAG, "Mock Fused Api latitude :" + latitude + ", Fused Api longitude :" + longitude);
            }
            else {
                isMockLocation=false;
                Log.d(TAG, "Fused Api latitude :" + latitude + ",Fused Api longitude :" + longitude);

            }
        }
        else
            Log.d(TAG, "Fused Api latitude :" + latitude + ",Fused Api longitude :" + longitude);

        latitude = location.getLatitude();
        longitude = location.getLongitude();
        accuracy = location.getAccuracy();
        mProviderName = location.getProvider();
        if (iLocationUpdater != null)
            iLocationUpdater.locationUpdate();


    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {

    }

    protected void startLocationUpdates() {
        if (mGoogleApiClient.isConnected()) {
            LocationServices.FusedLocationApi
                    .requestLocationUpdates(mGoogleApiClient, mLocationRequest,
                            this);
            Log.d(TAG, " Provider===> " + mProviderName + " Location Latitude ===> " + latitude + " Longitude===> " + longitude);
            if (iLocationUpdater != null)
                iLocationUpdater.locationUpdate();
            Log.d(TAG, "Location update started ..............: ");
        }
    }

    protected void stopLocationUpdates() {
        if (mGoogleApiClient.isConnected()) {
            LocationServices.FusedLocationApi.removeLocationUpdates(
                    mGoogleApiClient, this);
            Log.d(TAG, "Location update stopped .......................");
        }
    }

    public static float calculateDistance(double retailerLatitude,
                                          double retailerLongitude) {
        try {
            Location retLoc = new Location("");
            retLoc.setLatitude(retailerLatitude);
            retLoc.setLongitude(retailerLongitude);
            Location userLoc = new Location("");
            userLoc.setLatitude(LocationUtil.latitude);
            userLoc.setLongitude(LocationUtil.longitude);
            float distance = userLoc.distanceTo(retLoc);
            return distance;
        } catch (Exception e) {
            return 0;
        }
    }

		
}
