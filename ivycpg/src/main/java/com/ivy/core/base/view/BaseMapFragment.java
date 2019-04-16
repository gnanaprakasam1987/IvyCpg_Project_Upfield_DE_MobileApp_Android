package com.ivy.core.base.view;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentTransaction;
import android.view.View;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationAvailability;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.LocationSource;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.ivy.sd.png.asean.view.R;

public abstract class BaseMapFragment extends BaseFragment implements BaseIvyView, BaseMapView,OnMapReadyCallback,
        GoogleMap.OnMyLocationClickListener, GoogleMap.OnMyLocationButtonClickListener,GoogleMap.OnMarkerClickListener,
        GoogleMap.OnInfoWindowClickListener {

    private GoogleMap mMap;

    private FusedLocationProviderClient mLocationProviderClient;

    private LocationCallback mLocationCallback;

    private LocationSource.OnLocationChangedListener mLocationChangeListener;

    private float zoomLevel = 16;

    public abstract int getMapContainerResId();

    public abstract void onLocationResult(Location location);

    private Context context;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.context = context;
    }

    @Override
    public void loadMap() {
        final View view = ((Activity)context).findViewById(getMapContainerResId());

        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(getMapContainerResId());
        if (mapFragment == null) {
            mapFragment = SupportMapFragment.newInstance();
            FragmentTransaction fragmentTransaction = getChildFragmentManager().beginTransaction();
            fragmentTransaction.replace(getMapContainerResId(), mapFragment);
            fragmentTransaction.commitAllowingStateLoss();
        }


        if (GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(context) == ConnectionResult.SUCCESS) {
            mapFragment.getMapAsync(this);
        } else {
            onMapUnavailable();
        }

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        mMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(context, R.raw.map_style));

        onMapReady();

        mMap.setOnMarkerClickListener(this);
        mMap.setOnInfoWindowClickListener(this);
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        return false;
    }

    @Override
    public void onInfoWindowClick(Marker marker) {

    }

    protected GoogleMap getMap() {
        return mMap;
    }

    public void enableUserLocation() {
        if (mMap != null) {
            mLocationProviderClient = LocationServices.getFusedLocationProviderClient(context);
            mLocationCallback = new LocationCallback() {
                @Override
                public void onLocationResult(LocationResult locationResult) {
                    super.onLocationResult(locationResult);

                    final Location location = locationResult.getLastLocation();

                    BaseMapFragment.this.onLocationResult(location);
                }

                @Override
                public void onLocationAvailability(LocationAvailability locationAvailability) {
                    super.onLocationAvailability(locationAvailability);
                }
            };
        }
    }


    public void requestLocationUpdates() {
        LocationRequest locationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(5000)
                .setFastestInterval(1000);

        if (mLocationProviderClient != null && mLocationCallback != null) {
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED) {
                return;
            }
            mLocationProviderClient.requestLocationUpdates(locationRequest, mLocationCallback, null);
        }
    }

    public void removeLocationUpdates() {
        if (mLocationProviderClient != null && mLocationCallback != null) {
            mLocationProviderClient.removeLocationUpdates(mLocationCallback);
        }
    }

    public void setZoomLevel(int zoomLevel) {
        this.zoomLevel = zoomLevel;
    }

    public void moveMapToLocation(LatLng location) {
        moveMapToLocation(location, this.zoomLevel);
    }

    public void moveMapToLocation(LatLng location, float zoomLevel) {
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(location, zoomLevel));
    }

    public void enableZoomControls(boolean isEnabled) {
        mMap.getUiSettings().setZoomControlsEnabled(isEnabled);
    }

    public void enableCompass(boolean isEnabled) {
        mMap.getUiSettings().setCompassEnabled(isEnabled);
    }

    public void enableMyLocationButton(boolean isEnabled) {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        mMap.setMyLocationEnabled(true);
        mMap.setOnMyLocationButtonClickListener(this);
        mMap.setOnMyLocationClickListener(this);

    }

    public void enableMapToolBar(boolean isEnabled) {
        mMap.getUiSettings().setMapToolbarEnabled(isEnabled);
    }

    @Override
    public void onMyLocationClick(@NonNull Location location) {
        showLoading("Current location:\n" + location);
    }

    @Override
    public boolean onMyLocationButtonClick() {
        showMessage("MyLocation button clicked");
        // Return false so that we don't consume the event and the default behavior still occurs
        // (the camera animates to the user's current position).
        return false;
    }

    public Marker addMarkerToMap(MarkerOptions markerOptions) {
        return mMap.addMarker(markerOptions);
    }

    public void addSellerMarkerToMap(MarkerOptions markerOptions){
        Marker marker = addMarkerToMap(markerOptions);
    }

    public void addMarkerToMap(MarkerOptions markerOptions, boolean focus) {
        mMap.addMarker(markerOptions);
        if (focus)
            moveMapToLocation(markerOptions.getPosition());
    }

    @Override
    public void onStop() {
        super.onStop();
        removeLocationUpdates();
    }
}
