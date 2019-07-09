package com.ivy.maplib;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.net.ConnectivityManager;
import android.os.Build;
import android.os.Bundle;
import androidx.core.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.mapapi.map.BaiduMap;
import com.baidu.mapapi.map.BaiduMap.OnMarkerClickListener;
import com.baidu.mapapi.map.InfoWindow;
import com.baidu.mapapi.map.InfoWindow.OnInfoWindowClickListener;
import com.baidu.mapapi.map.MapStatusUpdate;
import com.baidu.mapapi.map.MapStatusUpdateFactory;
import com.baidu.mapapi.map.MapView;
import com.baidu.mapapi.map.Marker;
import com.baidu.mapapi.map.MarkerOptions;
import com.baidu.mapapi.map.OverlayOptions;
import com.baidu.mapapi.map.PolylineOptions;
import com.baidu.mapapi.map.SupportMapFragment;
import com.baidu.mapapi.model.LatLng;
import com.baidu.mapapi.model.LatLngBounds;
import com.baidu.mapapi.search.route.DrivingRoutePlanOption;
import com.baidu.mapapi.search.route.DrivingRouteResult;
import com.baidu.mapapi.search.route.OnGetRoutePlanResultListener;
import com.baidu.mapapi.search.route.PlanNode;
import com.baidu.mapapi.search.route.RoutePlanSearch;
import com.baidu.mapapi.search.route.TransitRouteResult;
import com.baidu.mapapi.search.route.WalkingRouteResult;
import com.ivy.location.LocationUpdater;
import com.ivy.location.LocationUtil;
import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.util.Commons;

import java.util.List;


public class BaiduMapFragment extends SupportMapFragment implements LocationUpdater, OnMarkerClickListener, OnInfoWindowClickListener, ViewTreeObserver.OnGlobalLayoutListener, OnGetRoutePlanResultListener {

    BaiduMap baidumap;
    BusinessModel bmodel;
    TextView fromTv;
    TextView toTv;
    RoutePlanSearch mRouteSearch;
    BaiduDataPulling dataPull;
    com.baidu.mapapi.map.MarkerOptions mCurrentLocationMarkerOption;
    ViewGroup info = null;
    LayoutInflater inflate;
    TextView infoTitle;
    TextView infoSnippet;
    ImageView badge;
    Marker stMarker;
    Marker enMarker;
    OverlayOptions mOvPolyLine;
    DrivingRouteOverlay drivingRouteOverlay;
    private MapView mapView;
    private List<MarkerOptions> markerList = null;
    private double myLatitude;
    private double myLongitude;
    private int mClick = 0;
    private LatLng[] markerLatLng = new LatLng[2];
    private String toText;
    private boolean showToast = true;
    private LatLngBounds bounds = null;
    private LatLngBounds.Builder builder = null;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        bmodel = (BusinessModel) getActivity().getApplicationContext();
        bmodel.setContext(getActivity());
        super.onCreateView(inflater, container, savedInstanceState);
        inflate = inflater;
        View rootView = inflater.inflate(R.layout.fragment_baidu_map, container, false);
        mapView = (MapView) rootView.findViewById(R.id.map_View);
        baidumap = mapView.getMap();
        setHasOptionsMenu(true);
        return rootView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        fromTv = (TextView) view.findViewById(R.id.fromTV);
        toTv = (TextView) view.findViewById(R.id.toTV);
    }

    @Override
    public void onStart() {
        super.onStart();
        markerList = dataPull.getBaiduData();
        removeMarkersfromMap();
        addMarkersToMap();
        onGlobalLayout();
        mRouteSearch = RoutePlanSearch.newInstance();
        mRouteSearch.setOnGetRoutePlanResultListener(this);

        mOvPolyLine = new PolylineOptions().width(10).color(0xAAFF0000);
    }

    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();
        LocationUtil locationUtil = LocationUtil.getInstance(getActivity());
        locationUtil.instantiateLocationUpdater(BaiduMapFragment.this);
        int permissionStatus = ContextCompat.checkSelfPermission(getActivity(),
                Manifest.permission.ACCESS_FINE_LOCATION);
        if (permissionStatus == PackageManager.PERMISSION_GRANTED) {
            locationUtil.startLocationListener();
        }

    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);

    }

    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            if (activity instanceof BaiduDataPulling) {
                dataPull = (BaiduDataPulling) activity;
            }
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement DataPullingInterface");
        }

    }

    private void addMarkersToMap() {
        try {
            for (int i = 0; i < markerList.size(); i++) {
                OverlayOptions OV = markerList.get(i);
                baidumap.addOverlay(OV);
            }
            baidumap.setOnMarkerClickListener(BaiduMapFragment.this);

        } catch (Exception e) {
            Commons.printException("" + e);
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.map_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);

    }

    private void removeMarkersfromMap() {
        baidumap.clear();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.show_me) {
            showMeOnMap();
            return true;
        } else if (item.getItemId() == R.id.show_places) {
            markerList = dataPull.getBaiduData();
            removeMarkersfromMap();
            addMarkersToMap();
            onGlobalLayout();
            return true;
        } else if (item.getItemId() == R.id.clear_route) {
            clearRoute();
            return true;
        } else {
            return super.onOptionsItemSelected(item);
        }
    }

    public void showMeOnMap() {

        if (mCurrentLocationMarkerOption != null) {
            markerList.remove(mCurrentLocationMarkerOption);
            mCurrentLocationMarkerOption = null;

        }

        LatLng ll = new LatLng(myLatitude, myLongitude);
        Bundle bndl = new Bundle();
        bndl.putCharSequence("addr", myLatitude + "," + myLongitude);
        mCurrentLocationMarkerOption = new com.baidu.mapapi.map.MarkerOptions().position(ll)
                .title("My Current Location")
                .extraInfo(bndl)
                .icon(com.baidu.mapapi.map.BitmapDescriptorFactory.fromResource(R.drawable.marker2)).animateType(MarkerOptions.MarkerAnimateType.grow);
        markerList.add(mCurrentLocationMarkerOption);
        baidumap.addOverlay(mCurrentLocationMarkerOption);
        updateMapStatus(ll);
    }

    public void updateMapStatus(LatLng location) {
        MapStatusUpdate statusUpdate = MapStatusUpdateFactory.newLatLng(location);
        baidumap.setMapStatus(statusUpdate);
    }

    @Override
    public void locationUpdate() {
        if (getActivity() != null) {
            myLatitude = LocationUtil.latitude;
            myLongitude = LocationUtil.longitude;

        }

    }

    @Override
    public boolean onMarkerClick(final Marker marker) {
        final ViewGroup nullParent = null;
        String textStr;
        info = (ViewGroup) inflate.inflate(
                R.layout.custom_info_window, nullParent);
        this.infoTitle = (TextView) info.findViewById(R.id.title);
        this.infoSnippet = (TextView) info
                .findViewById(R.id.snippet);
        this.badge = (ImageView) info.findViewById(R.id.badge);
        this.badge.setImageResource(R.drawable.route_normal_map);
        if (marker.getTitle() != null) {
            textStr = marker.getTitle() + "";
            this.infoTitle.setText(textStr);
        }
        if (marker.getExtraInfo() != null) {
            textStr = marker.getExtraInfo().get("addr") + "";
            this.infoSnippet.setText(textStr);
        }

        InfoWindow infoWindow = new InfoWindow(info, marker.getPosition(),
                -40);
        baidumap.showInfoWindow(infoWindow);

        this.badge.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                badge.setImageResource(R.drawable.route_selected_map);
                onWindowClick(marker);
            }
        });

        return false;
    }

    private boolean checkInternetConnection() {
        ConnectivityManager cm = (ConnectivityManager) getActivity()
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm.getActiveNetworkInfo() != null
                && cm.getActiveNetworkInfo().isAvailable()
                && cm.getActiveNetworkInfo().isConnected()) {
            return true;
        } else {
            Commons.print("Internet Check," + "Internet Connection Not Present");
            return false;
        }
    }

    @Override
    public void onInfoWindowClick() {

        // TO DO onInfoWindowClick
    }

    public void onWindowClick(Marker marker) {
        String textClStr;
        if (checkInternetConnection()) {
            try {
                String mClickString = Integer.toString(mClick);
                Toast.makeText(getActivity(), mClickString, Toast.LENGTH_LONG).show();
                if (mClick == 0) {
                    markerLatLng[0] = marker.getPosition();
                    textClStr = "From : "
                            + trimString(marker.getTitle(), 20);
                    fromTv.setText(textClStr);
                    mClick = 1;
                    baidumap.hideInfoWindow();
                    stMarker = marker;
                } else if (mClick == 1) {
                    if (!isSameLocation(marker))
                        drawRoute(marker);
                } else if (mClick == 2) {
                    mClick = -1;
                    textClStr = "From : " + trimString(toText, 20);
                    fromTv.setText(textClStr);
                    clearRoute();
                    if (mClick == 1) {
                        if (!isSameLocation(marker))
                            drawRoute(marker);
                    }
                }
            } catch (Exception e) {
                Commons.printException("" + e);
            }
        } else {
            Toast.makeText(getActivity().getApplicationContext(),
                    "Check Network Connection", Toast.LENGTH_SHORT).show();
        }
    }

    private boolean isSameLocation(Marker mMarker) {
        return (markerLatLng[0].latitude == mMarker.getPosition().latitude && markerLatLng[0].longitude == mMarker
                .getPosition().longitude);
    }

    private void drawRoute(Marker mMarker) {
        String textRTStr;
        baidumap.hideInfoWindow();
        markerLatLng[1] = mMarker.getPosition();
        enMarker = mMarker;
        textRTStr = "To : " + trimString(mMarker.getTitle(), 20);
        toTv.setText(textRTStr);
        toText = mMarker.getTitle();
        PlanNode stNode = PlanNode.withLocation(markerLatLng[0]);
        PlanNode dtNode = PlanNode.withLocation(markerLatLng[1]);
        mRouteSearch.drivingSearch((new DrivingRoutePlanOption()).from(stNode).to(dtNode));
        mClick = 2;
    }

    private String trimString(String string, int limit) {
        if (string.length() > limit) {
            string = string.substring(0, limit) + "...";
        }
        return string;
    }

    private void clearRoute() {
        String clrRt;
        try {
            if (mClick == -1) {
                markerLatLng[0] = markerLatLng[1];
                clrRt = "To : ";
                toTv.setText(clrRt);
                mClick = 1;
            } else {
                clrRt = "From : ";
                fromTv.setText(clrRt);
                clrRt = "To : ";
                toTv.setText(clrRt);
                toText = "";
                if (showToast) {
                    Toast.makeText(getActivity().getApplicationContext(),
                            "Route Cleared, Select Two Marker",
                            Toast.LENGTH_SHORT).show();
                } else {
                    showToast = true;
                }
                mClick = 0;
            }
            if (baidumap != null) {
                drivingRouteOverlay.removeFromMap();
            }
            stMarker = null;
            enMarker = null;
        } catch (Exception e) {
            Commons.printException("" + e);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mRouteSearch.destroy();

    }

    @Override
    public void onGlobalLayout() {
        try {
            if (!markerList.isEmpty()) {
                if (builder == null) {
                    builder = new LatLngBounds.Builder();
                } else {
                    builder = null;
                    builder = new LatLngBounds.Builder();
                }

                for (int i = 0; i < markerList.size(); i++) {
                    builder.include(markerList.get(i).getPosition());
                }
                if (bounds == null) {
                    bounds = builder.build();
                } else {
                    bounds = null;
                    bounds = builder.build();
                }

                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
                    mapView.getViewTreeObserver().removeGlobalOnLayoutListener(
                            this);
                } else {
                    mapView.getViewTreeObserver().removeOnGlobalLayoutListener(
                            this);
                }
                if (bounds != null) {
                    MapStatusUpdate statusUpdate = MapStatusUpdateFactory.newLatLngBounds(bounds);
                    baidumap.setMapStatus(statusUpdate);
                }
            } else {
                Toast.makeText(getActivity().getApplicationContext(),
                        "No Location to display", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Commons.printException("" + e);
        }
    }

    @Override
    public void onGetDrivingRouteResult(DrivingRouteResult drivingRouteResult) {
        try {
            if (drivingRouteResult != null && drivingRouteResult.getRouteLines() != null) {
                drivingRouteOverlay = new DrivingRouteOverlay(baidumap);
                baidumap.setOnMarkerClickListener(drivingRouteOverlay);
                drivingRouteOverlay.setData(drivingRouteResult.getRouteLines().get(0));
                drivingRouteOverlay.addToMap();
                drivingRouteOverlay.zoomToSpan();
            }
        } catch (Exception ex) {
            Toast.makeText(getActivity(), "Excep " + ex.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onGetWalkingRouteResult(WalkingRouteResult walkingRouteResult) {

        // TO DO onGetWalkingRouteResult
    }

    @Override
    public void onGetTransitRouteResult(TransitRouteResult transitRouteResult) {

        // TO DO onGetTransitRouteResult
    }

    public interface BaiduDataPulling {
        List<MarkerOptions> getBaiduData();
    }
}
