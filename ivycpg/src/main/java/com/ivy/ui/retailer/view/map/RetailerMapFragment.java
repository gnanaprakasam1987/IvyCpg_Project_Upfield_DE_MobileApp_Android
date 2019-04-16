package com.ivy.ui.retailer.view.map;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.location.Location;
import android.support.annotation.NonNull;
import android.support.constraint.Group;
import android.support.design.widget.BottomSheetBehavior;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.CardView;
import android.support.v7.widget.SwitchCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.ivy.core.base.view.BaseMapFragment;
import com.ivy.cpg.view.profile.ProfileActivity;
import com.ivy.maplib.MapWrapperLayout;
import com.ivy.maplib.OnInfoWindowElemTouchListener;
import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.bo.RetailerMasterBO;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.util.Commons;
import com.ivy.ui.retailer.RetailerContract;
import com.ivy.ui.retailer.di.DaggerRetailerComponent;
import com.ivy.ui.retailer.di.RetailerModule;
import com.ivy.ui.retailer.presenter.RetailerPresenterImpl;
import com.ivy.utils.DeviceUtils;
import com.ivy.utils.NetworkUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Vector;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.BindViews;
import butterknife.OnClick;

import static android.content.res.Configuration.SCREENLAYOUT_SIZE_LARGE;

public class RetailerMapFragment extends BaseMapFragment implements RetailerContract.RetailerView,
        GoogleMap.OnMarkerClickListener,
        GoogleMap.OnInfoWindowClickListener {

    private String screenTitle;
    private ViewGroup infoWindow;
    private Context context;

    private List<RetailerMasterBO> retailerList;

    private MapWrapperLayout mapWrapperLayout;

    private TextView infoTitle;
    private TextView infoSnippet;
    private TextView infoProfile;
    private TextView infoAddToPlan;

    private OnInfoWindowElemTouchListener infoButtonListener;
    private OnInfoWindowElemTouchListener infoDeviateListener;

    private boolean isRoute = false;
    private boolean isBywalk = false,isclickable;

    private Vector<Polyline> line = new Vector<>();
    private int mClick = 0;
    private String toText;
    private Marker rmarker;
    private LatLng[] markerLatLng = new LatLng[2];

    private Marker userMarker;
    private LatLngBounds.Builder builder;

    @BindView(R.id.legendGroup)
    Group legendGroup;

    @BindView(R.id.storeFilterSwitch)
    SwitchCompat storeFilterSwitch;

    @BindView(R.id.car_direction)
    ImageView carDirBtn;

    @BindView(R.id.walk_direction)
    ImageView walkDirBtn;

    @BindView(R.id.bottom_view)
    LinearLayout bottomLayout;

    @BindView(R.id.from_txt_value)
    TextView fromTv;

    @BindView(R.id.to_txt_value)
    TextView toTv;

    @BindView(R.id.add_plan)
    TextView addPlan;

    @BindView(R.id.tv_outlet_name)
    TextView tvOutletName;

    @BindView(R.id.tv_outlet_address)
    TextView tvOutletAddress;

    @BindView(R.id.tv_last_visit_txt)
    TextView tvLastVisitDate;

    @BindView(R.id.visitElementGroup)
    Group visitElementGroup;

    @BindView(R.id.outlet_plan_window)
    CardView outletPlanWindow;

    private BottomSheetBehavior bottomSheetBehavior;

    @Inject
    RetailerPresenterImpl<RetailerContract.RetailerView> presenter;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.context = context;
    }

    @Override
    public void initializeDi() {
        DaggerRetailerComponent.builder()
                .ivyAppComponent(((BusinessModel) Objects.requireNonNull((FragmentActivity)context).getApplication()).getComponent())
                .retailerModule(new RetailerModule(this, context))
                .build()
                .inject(RetailerMapFragment.this);

        setBasePresenter(presenter);
    }

    @Override
    protected int setContentViewLayout() {
        return R.layout.fragment_retailer_map;
    }

    @Override
    public void initVariables(View view) {

        mapWrapperLayout = view.findViewById(R.id.mapContainerLayout);

        LayoutInflater layInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        this.infoWindow = (ViewGroup) layInflater.inflate(
                R.layout.retailer_custom_info_window, (ViewGroup) null);

        infoTitle = infoWindow.findViewById(R.id.title);
        infoSnippet = infoWindow.findViewById(R.id.snippet);
        infoProfile = infoWindow.findViewById(R.id.btn_profile);
        infoAddToPlan = infoWindow.findViewById(R.id.btn_deviate);

        this.infoButtonListener = new OnInfoWindowElemTouchListener(infoProfile) {
            @Override
            protected void onClickConfirmed(View v, Marker marker) {
                if (!getResources().getString(R.string.my_location).equals(marker.getTitle())) {
                    Toast.makeText(context, "Profile Button", Toast.LENGTH_SHORT).show();
                    profileClicked(retailerMasterBO);
                }
            }
        };
        infoProfile.setOnTouchListener(infoButtonListener);

        this.infoDeviateListener = new OnInfoWindowElemTouchListener(infoAddToPlan) {
            @Override
            protected void onClickConfirmed(View v, Marker marker) {
                if (!getResources().getString(R.string.my_location).equals(marker.getTitle())) {
                    Toast.makeText(context, "Plan Button", Toast.LENGTH_SHORT).show();
                }
            }
        };
        infoAddToPlan.setOnTouchListener(infoDeviateListener);

        storeFilterSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                if (getMap() != null)
                    getMap().clear();

                if (isChecked) {
                    populateTodayPlannedRetailers(retailerList);
                    storeFilterSwitch.setText(getResources().getString(R.string.day_plan));
                }else {
                    populateRetailers(retailerList);
                    storeFilterSwitch.setText(getResources().getString(R.string.all_retailer));
                }
            }
        });

        // For 7" tablet
        boolean is7InchTablet = this.getResources().getConfiguration()
                .isLayoutSizeAtLeast(SCREENLAYOUT_SIZE_LARGE);
        if (!is7InchTablet) {
            bottomLayout.getLayoutParams().height = (int) getResources().getDimension(R.dimen.ratiler_map_bottomview_height);
        }

        walkDirBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isBywalk = true;
                isclickable = true;
                clearRoute();
                if (mClick == 2) {
                    if (isSameLocation(rmarker))
                        drawRoute(rmarker);
                }
                //car icon's
                walkDirBtn.setBackground(ContextCompat.getDrawable(context, R.drawable.map_button_round_corner_white));
                walkDirBtn.setColorFilter(ContextCompat.getColor(context, R.color.highlighter));
                //walk iocn's
                carDirBtn.setBackground(ContextCompat.getDrawable(context, R.drawable.button_round_corner_transparent));
                carDirBtn.setColorFilter(ContextCompat.getColor(context, R.color.divider_view_color));
            }
        });

        carDirBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isBywalk = false;
                isclickable = true;
                clearRoute();
                if (mClick == 2) {
                    if (isSameLocation(rmarker))
                        drawRoute(rmarker);
                }
                //car icon's
                carDirBtn.setBackground(ContextCompat.getDrawable(context, R.drawable.map_button_round_corner_white));
                carDirBtn.setColorFilter(ContextCompat.getColor(context, R.color.highlighter));
                //walk iocn's
                walkDirBtn.setBackground(ContextCompat.getDrawable(context, R.drawable.button_round_corner_transparent));
                walkDirBtn.setColorFilter(ContextCompat.getColor(context, R.color.divider_view_color));
            }
        });

        tvOutletName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                profileClicked(retailerMasterBO);
            }
        });

        bottomSheetBehavior = BottomSheetBehavior.from(view.findViewById(R.id.outlet_plan_window));

    }

    @OnClick(R.id.clear_route_id)
    void clearRouteButton(){
        isclickable = false;
        isBywalk = false;
        clearRoute();
        //car icon's
        carDirBtn.setBackground(ContextCompat.getDrawable(context, R.drawable.map_button_round_corner_white));
        carDirBtn.setColorFilter(ContextCompat.getColor(context, R.color.highlighter));
        //walk iocn's
        walkDirBtn.setBackground(ContextCompat.getDrawable(context, R.drawable.button_round_corner_transparent));
        walkDirBtn.setColorFilter(ContextCompat.getColor(context, R.color.divider_view_color));
    }

    @OnClick(R.id.retailer_legend_info_img)
    void onInfoImgClicked() {
        legendGroup.setVisibility(legendGroup.getVisibility() == View.VISIBLE ? View.GONE : View.VISIBLE);
    }

    @OnClick(R.id.fab)
    void routeIconClicked(){
        if (!isRoute) {
            bottomLayout.setVisibility(View.VISIBLE);
            isRoute = true;
        } else {
            isclickable = false;
            isBywalk = false;
            clearRoute();
            isRoute = false;
            bottomLayout.setVisibility(View.GONE);
            //car icon's
            carDirBtn.setBackground(ContextCompat.getDrawable(context, R.drawable.map_button_round_corner_white));

            carDirBtn.setColorFilter(ContextCompat.getColor(context, R.color.highlighter));
            //walk iocn's
            walkDirBtn.setBackground(ContextCompat.getDrawable(context, R.drawable.button_round_corner_transparent));

            walkDirBtn.setColorFilter(ContextCompat.getColor(context, R.color.divider_view_color));
        }

    }

    @Override
    protected void getMessageFromAliens() {
        if (getArguments() != null)
            screenTitle = getArguments().getString("screentitle");
    }

    @Override
    protected void setUpViews() {
        setUpToolbar(screenTitle);
        loadMap();

        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);

        bottomSheetBehavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                switch (newState) {
                    default:
                        break;
                }
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {
            }
        });


    }

    @Override
    public void populateRetailers(List<RetailerMasterBO> retailerList) {

        boolean isFocusRetailer = false;
        builder = new LatLngBounds.Builder();

        for (RetailerMasterBO retailerMasterBO : retailerList) {

            if (retailerMasterBO.getLatitude() != 0 && retailerMasterBO.getLongitude() != 0) {

                CharSequence retailerState = "Y";
                if ("Y".equals(retailerMasterBO.getIsVisited())
                        || "Y".equals(retailerMasterBO.getIsDeviated()))
                    retailerState = "Z";

                addMarkerToMap(prepareMarkerOption(retailerMasterBO,builder,retailerState));

                isFocusRetailer = true;
            }
        }

        this.retailerList = retailerList;

        if (isFocusRetailer)
            focusMarker(builder);
    }

    private MarkerOptions prepareMarkerOption(RetailerMasterBO retailerMasterBO, LatLngBounds.Builder builder,CharSequence stateName){

        LatLng latLng = new LatLng(retailerMasterBO.getLatitude(), retailerMasterBO.getLongitude());
        MarkerOptions mMarkerOptions = new MarkerOptions()
                .position(latLng)
                .title(retailerMasterBO.getRetailerName() + "," + retailerMasterBO.getRetailerID()+ ","+stateName )
                .snippet(retailerMasterBO.getAddress1())
                .icon(BitmapDescriptorFactory
                        .fromResource(getMarkerIcon(retailerMasterBO)));


        builder.include(latLng);

        return mMarkerOptions;
    }

    @Override
    public void populateTodayPlannedRetailers(List<RetailerMasterBO> todayPlannedRetailers){

        boolean isFocusRetailer = false;
        builder = new LatLngBounds.Builder();
        for (RetailerMasterBO retailerMasterBO : retailerList) {
            if ("Y".equals(retailerMasterBO.getIsVisited())
                    || retailerMasterBO.getIsToday() == 1
                    || "Y".equals(retailerMasterBO.getIsDeviated())) {

                CharSequence retailerState = "Y";
                if ("Y".equals(retailerMasterBO.getIsVisited())
                        || "Y".equals(retailerMasterBO.getIsDeviated()))
                    retailerState = "Z";

                if (retailerMasterBO.getLatitude() != 0 && retailerMasterBO.getLongitude() != 0) {
                    addMarkerToMap(prepareMarkerOption(retailerMasterBO, builder,retailerState));
                    isFocusRetailer = true;
                }
            }
        }

        if (isFocusRetailer)
            focusMarker(builder);
    }

    @Override
    public void populatePlannedRetailers(List<RetailerMasterBO> plannedRetailers) {

    }

    @Override
    public void populateUnPlannedRetailers(List<RetailerMasterBO> unPlannedRetailers) {

    }

    @Override
    public void populateCompletedRetailers(List<RetailerMasterBO> unPlannedRetailers) {

    }

    @Override
    public void drawRoutePath(String path) {

        if (path == null) {
            hideLoading();
            return;
        }

        try {
            // Transform the String into a json object
            final JSONObject json = new JSONObject(path);
            JSONArray routeArray = json.getJSONArray("routes");

            if (routeArray.length() == 0)
                return;

            JSONObject routes = routeArray.getJSONObject(0);
            JSONObject overviewPolylines = routes
                    .getJSONObject("overview_polyline");
            String encodedString = overviewPolylines.getString("points");
            List<LatLng> list = decodePoly(encodedString);
            for (int z = 0; z < list.size() - 1; z++) {
                LatLng src = list.get(z);
                LatLng dest = list.get(z + 1);
                line.add(getMap().addPolyline(new PolylineOptions()
                        .add(new LatLng(src.latitude, src.longitude),
                                new LatLng(dest.latitude, dest.longitude))
                        .width(6).color(Color.argb(200, 200, 90, 50))
                        .geodesic(true)));
            }

            hideLoading();

        } catch (JSONException e) {
            Commons.printException(e);
            hideLoading();

        }
    }

    @Override
    public void onInfoWindowClick(Marker mMarker) {
        String testStr;
        if (NetworkUtils.isNetworkConnected(context)) {
            try {
                if (mClick == 0) {
                    markerLatLng[0] = mMarker.getPosition();
                    testStr = (getResources().getString(R.string.my_location).equals(mMarker.getTitle())) ? mMarker.getTitle()
                            : mMarker.getTitle().split(",")[0];
                    fromTv.setText(testStr);
                    mClick = 1;
                    mMarker.hideInfoWindow();
                } else if (mClick == 1) {
                    rmarker = mMarker;
                    markerLatLng[1] = mMarker.getPosition();
                    String testToRoute = (getResources().getString(R.string.my_location).equals(mMarker.getTitle())) ? mMarker.getTitle()
                            : mMarker.getTitle().split(",")[0];
                    toTv.setText(testToRoute);
                    if (isSameLocation(mMarker))
                        drawRoute(mMarker);

                } else if (mClick == 2) {
                    mClick = -1;
                    rmarker = mMarker;
                    testStr = toText;
                    fromTv.setText(testStr);
                    markerLatLng[1] = mMarker.getPosition();
                    String testToRoute = mMarker.getTitle();
                    toTv.setText(testToRoute);
                    clearRoute();
                    if (mClick == 1) {
                        if (isSameLocation(mMarker))
                            drawRoute(mMarker);
                    }
                }
            } catch (Exception e) {
                Commons.printException(e);
            }
        }
    }

    @Override
    public boolean onMarkerClick(Marker marker) {

       if (getResources().getString(R.string.my_location).equals(marker.getTitle()))
        return true;

        for (RetailerMasterBO retailerMasterBO : retailerList) {
            if (retailerMasterBO.getRetailerID().equals(marker.getTitle().split(",")[1])) {
                this.retailerMasterBO = retailerMasterBO;
                break;
            }
        }

        if (isRoute) {
            isclickable = false;
            onInfoWindowClick(marker);
        }else {
            marker.showInfoWindow();

            setPlanWindowValues(retailerMasterBO);
        }
        return false;
    }


    RetailerMasterBO retailerMasterBO;

    private void setPlanWindowValues(RetailerMasterBO retailerMasterBO){
        if (bottomSheetBehavior.getState() == BottomSheetBehavior.STATE_HIDDEN) {
            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
        }

        tvOutletName.setText(retailerMasterBO.getRetailerName());
        tvOutletAddress.setText(retailerMasterBO.getAddress1());

        if (retailerMasterBO.getIsToday() == 1){
            visitElementGroup.setVisibility(View.VISIBLE);
        }else {
            visitElementGroup.setVisibility(View.GONE);

            tvLastVisitDate.setText(retailerMasterBO.getLastVisitDate());
        }
    }

    @Override
    public int getMapContainerResId() {
        return R.id.mapContainerLayout;
    }

    @Override
    public void onLocationResult(Location location) {

        LatLng latLng = new LatLng(location.getLatitude(),location.getLongitude());

        if (userMarker == null) {
            MarkerOptions mMarkerOptions = new MarkerOptions()
                    .position(latLng)
                    .title(getResources().getString(R.string.my_location))
                    .icon(BitmapDescriptorFactory
                            .fromResource(R.drawable.marker));

            userMarker = addMarkerToMap(mMarkerOptions);
        }else
            userMarker.setPosition(latLng);
    }

    @Override
    public void onMapReady() {

        mapWrapperLayout.init(getMap(), DeviceUtils.dpToPixel(context, 60));

        getMap().setOnMarkerClickListener(this);
        getMap().setInfoWindowAdapter(new CustomInfoWindowAdapter());
        getMap().setOnInfoWindowClickListener(this);
        getMap().setOnMapClickListener(new GoogleMap.OnMapClickListener() {

            @Override
            public void onMapClick(LatLng latLng) {

                if (legendGroup.getVisibility() == View.VISIBLE)
                    legendGroup.setVisibility(View.GONE);

                if (bottomSheetBehavior.getState() == BottomSheetBehavior.STATE_EXPANDED)
                    bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);

            }
        });

        presenter.fetchRetailerList();

        enableUserLocation();
        requestLocationUpdates();
    }

    @Override
    public void onMapUnavailable() {
        showMessage("Map Not Available");
    }

    private class CustomInfoWindowAdapter implements GoogleMap.InfoWindowAdapter {

        CustomInfoWindowAdapter() {
            // CustomInfoWindowAdapter
        }

        @Override
        public View getInfoContents(Marker marker) {
            return null;
        }

        @Override
        public View getInfoWindow(final Marker marker) {

            infoProfile.setVisibility(View.GONE);
//            String[] str_snippet = marker.getSnippet().split("\n");
            String str_title = marker.getTitle().split(",")[0];
            infoTitle.setText(str_title);
//            infoSnippet.setText(str_snippet[0]);
//            String isPlanned = marker.getTitle().split(",")[2];
//            if ("Y".equals(isPlanned))
//                infoAddToPlan.setVisibility(View.VISIBLE);
//            else
                infoAddToPlan.setVisibility(View.GONE);

//            infoButtonListener.setMarker(marker);
//            infoDeviateListener.setMarker(marker);
            mapWrapperLayout.setMarkerWithInfoWindow(marker, infoWindow);
            return infoWindow;
        }

    }

    private void profileClicked(RetailerMasterBO startVisitBo){
        presenter.setRetailerMasterBo(startVisitBo);
        Intent i = new Intent(context, ProfileActivity.class);
        i.putExtra("From", "RetailerMap");
        i.putExtra("locvisit", true);
        i.putExtra("map", true);
        i.putExtra("HideVisit", startVisitBo.getIsToday() != 1);

        startActivity(i);
    }

    public void focusMarker(final LatLngBounds.Builder builder) {

        try {
            getMap().setOnMapLoadedCallback(new GoogleMap.OnMapLoadedCallback() {
                @Override
                public void onMapLoaded() {

                    if (checkAreaBoundsTooSmall(builder.build())) {
                        getMap().animateCamera(CameraUpdateFactory.newLatLngZoom(builder.build().getCenter(), 19));
                    } else {
                        getMap().animateCamera(CameraUpdateFactory.newLatLngBounds(builder.build(), 60));
                    }
                }
            });
        }catch(Exception e){
            Commons.printException(e);
        }
    }

    private boolean checkAreaBoundsTooSmall(LatLngBounds bounds) {
        float[] result = new float[1];
        Location.distanceBetween(bounds.southwest.latitude, bounds.southwest.longitude, bounds.northeast.latitude, bounds.northeast.longitude, result);
        return result[0] < 300;
    }

    private int getMarkerIcon(RetailerMasterBO retailerMasterBO) {
        int drawable = R.drawable.marker_visit_unscheduled;

        if ("Y".equals(retailerMasterBO.getIsVisited())) {
            if (("N").equals(retailerMasterBO.isOrdered()))
                drawable = R.drawable.marker_visit_non_productive;
            else
                drawable = R.drawable.marker_visit_completed;
        } else if (retailerMasterBO.getIsToday() == 1 || "Y".equals(retailerMasterBO.getIsDeviated()))
            drawable = R.drawable.marker_visit_planned;


        if (retailerMasterBO.isHasNoVisitReason())
            drawable = R.drawable.marker_visit_cancelled;


        return drawable;
    }

    private void clearRoute() {
        String testClearRoute;
        try {
            if (mClick == -1) {
                markerLatLng[0] = markerLatLng[1];
                testClearRoute = " ";
                toTv.setText(testClearRoute);
                mClick = 1;
            } else if (!isclickable) {
                testClearRoute = " ";
                fromTv.setText(testClearRoute);
                testClearRoute = " ";
                toTv.setText(testClearRoute);
                toText = "";
                if (isRoute)
                    Toast.makeText(getActivity(),
                        getResources().getString(R.string.route_cleared),
                        Toast.LENGTH_SHORT).show();
                mClick = 0;
            }
            for (Polyline polyLine : line) {
                polyLine.remove();
            }
            line = null;
            System.gc();
            line = new Vector<>();
        } catch (Exception e) {
            Commons.printException(e);
        }
    }

    private boolean isSameLocation(Marker mMarker) {
        return (markerLatLng[0].latitude != mMarker.getPosition().latitude || markerLatLng[0].longitude != mMarker
                .getPosition().longitude);
    }

    private void drawRoute(Marker mMarker) {
        String url;
        mMarker.hideInfoWindow();
        markerLatLng[1] = mMarker.getPosition();
        toText = (getResources().getString(R.string.my_location).equals(mMarker.getTitle())) ? mMarker.getTitle()
                : mMarker.getTitle().split(",")[0];
        url = makeURL(markerLatLng[0].latitude, markerLatLng[0].longitude,
                markerLatLng[1].latitude, markerLatLng[1].longitude);

        showLoading(getResources().getString(R.string.fetching_route));
        presenter.fetchRoutePath(url);

        mClick = 2;
    }

    public String makeURL(double sourcelat, double sourcelog, double destlat,
                          double destlog) {
        String mode;
        if (isBywalk)
            mode = "mode=walking";
        else
            mode = "mode=driving";

        String mapKey = "key=" + getString(R.string.google_maps_api_key);

        return "https://maps.googleapis.com/maps/api/directions/json" +
                "?origin=" + Double.toString(sourcelat) + "," + Double.toString(sourcelog) +
                "&destination=" + Double.toString(destlat) + "," + Double.toString(destlog) +
                "&sensor=false&" + mode + "&alternatives=true" + "&" + mapKey;
    }

    private List<LatLng> decodePoly(String encoded) {

        List<LatLng> poly = new ArrayList<>();
        int index = 0, len = encoded.length();
        int lat = 0, lng = 0;

        while (index < len) {
            int b, shift = 0, result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lat += dlat;

            shift = 0;
            result = 0;
            do {
                b = encoded.charAt(index++) - 63;
                result |= (b & 0x1f) << shift;
                shift += 5;
            } while (b >= 0x20);
            int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lng += dlng;

            LatLng p = new LatLng((((double) lat / 1E5)),
                    (((double) lng / 1E5)));
            poly.add(p);
        }

        return poly;
    }
}
