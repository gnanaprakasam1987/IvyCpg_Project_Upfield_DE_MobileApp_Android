package com.ivy.location;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.core.content.ContextCompat;
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

import java.text.DecimalFormat;

public class LocationUtil implements LocationListener, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private static final String TAG = "LocationUtil";

    /* Configuration details */
    public enum LOCATION_TYPE_CONFIG {
        FUSED(0),
        NATIVE_GPS(1);
        private int value;

        LOCATION_TYPE_CONFIG(int value) {
            this.value = value;
        }
    }

    public static int gpsconfigcode;


    private Context context;
    private static LocationUtil instance = null;

    /* Fused location provide using Google play service initialisation.*/
    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;

    /* Native location provider */
    private LocationManager nativeLocationManager;
    private MyLocationListener nativeLocationListener;

    /* Interface to update location to fragments*/
    private LocationUpdater iLocationUpdater;


    /* Values from location services */
    public static double latitude = 0;
    public static double longitude = 0;
    public static float accuracy = 0;
    public static boolean isMockLocation;
    public static String mProviderName;


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
     * Used to receive location update when there is a change in location.
     *
     * @param fragment implements LocationUpdater interface
     */
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

    /**
     * To start the location listener.
     */
    @TargetApi(23)
    public void startLocationListener() {

        /* Notify user that google play service is not available */
        if (gpsconfigcode == 0 && !isGooglePlayServicesAvailable()) {
            Toast.makeText(context,
                    "Google play service not available.",
                    Toast.LENGTH_LONG).show();
        }

        if (gpsconfigcode == 0 && isGooglePlayServicesAvailable()) {
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

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
                if (loc.isFromMockProvider()) {
                    isMockLocation = true;
                    Log.d(TAG, "Mock Location Manager latitude :" + latitude + ",Location Manager longitude :" + longitude);
                } else {
                    isMockLocation = false;
                    Log.d(TAG, "Location Manager latitude :" + latitude + ",Location Manager longitude :" + longitude);
                }
            } else
                Log.d(TAG, "Location Manager latitude :" + latitude + ",Location Manager longitude :" + longitude);

            latitude = truncateLocation(loc.getLatitude());
            longitude = truncateLocation(loc.getLongitude());
            accuracy = loc.getAccuracy();
            mProviderName = loc.getProvider();

            /* Notify subscribed classes via callbacks*/
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
        boolean bool = false;
        try {
            nativeLocationManager = (LocationManager) context
                    .getSystemService(Context.LOCATION_SERVICE);
            bool = nativeLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        } catch (Exception e) {
            Commons.printException(e);
        }
        return bool;
    }

    /**
     * Start the Location listener. Use GPS_PROVIDER if GPS is enabled by user
     * or it will use NERWORK_PROVIDER.
     */

    /**
     * Stop the location listener.
     */
    public void stopLocationListener() {

        if (gpsconfigcode == 0 && isGooglePlayServicesAvailable()) {
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
    public boolean isGooglePlayServicesAvailable() {

        int status = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(context);
        if (ConnectionResult.SUCCESS == status) {
            return true;
        }
        return false;

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
                isMockLocation = true;
                Log.d(TAG, "Mock Fused Api latitude :" + latitude + ", Fused Api longitude :" + longitude);
            } else {
                isMockLocation = false;
                Log.d(TAG, "Fused Api latitude :" + latitude + ",Fused Api longitude :" + longitude);

            }
        } else
            Log.d(TAG, "Fused Api latitude :" + latitude + ",Fused Api longitude :" + longitude);

        latitude = truncateLocation(location.getLatitude());
        longitude = truncateLocation(location.getLongitude());
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
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, this);
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
            retLoc.setLatitude(truncateLocation(retailerLatitude));
            retLoc.setLongitude(truncateLocation(retailerLongitude));
            Location userLoc = new Location("");
            userLoc.setLatitude(truncateLocation(LocationUtil.latitude));
            userLoc.setLongitude(truncateLocation(LocationUtil.longitude));
            float distance = userLoc.distanceTo(retLoc);
            return distance;
        } catch (Exception e) {
            return 0;
        }
    }

    /**
     * To avoid exponential value and truncate decimal digits to 10
     *
     * @param originalLocation Original Location
     * @return Truncated location
     */
    private static double truncateLocation(Double originalLocation) {

        Double val = 0.0;
        try {

            DecimalFormat decimalFormat = new DecimalFormat("0.0000000000");
            decimalFormat.setMinimumFractionDigits(2);
            decimalFormat.setMaximumFractionDigits(10);

            val = Double.valueOf(decimalFormat.format(originalLocation));
        } catch (Exception e) {
            Commons.printException(e);
        }

        return val;
    }

}
