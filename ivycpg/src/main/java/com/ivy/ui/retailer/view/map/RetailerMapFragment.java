package com.ivy.ui.retailer.view.map;

import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.support.constraint.Group;
import android.support.v7.widget.SwitchCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.ivy.core.base.view.BaseMapFragment;
import com.ivy.core.data.app.AppDataProvider;
import com.ivy.core.data.app.AppDataProviderImpl;
import com.ivy.cpg.view.profile.ProfileActivity;
import com.ivy.maplib.MapWrapperLayout;
import com.ivy.maplib.OnInfoWindowElemTouchListener;
import com.ivy.maplib.PlanningMapFragment;
import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.bo.RetailerMasterBO;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.ui.retailer.RetailerContract;
import com.ivy.ui.retailer.di.DaggerRetailerComponent;
import com.ivy.ui.retailer.di.RetailerModule;
import com.ivy.ui.retailer.presenter.RetailerPresenterImpl;
import com.ivy.utils.DeviceUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.OnClick;

public class RetailerMapFragment extends BaseMapFragment implements RetailerContract.RetailerView,
        GoogleMap.OnMarkerClickListener,
        GoogleMap.OnInfoWindowClickListener {

    private String screenTitle;
    private ViewGroup infoWindow;
    private Context context;

    private List<RetailerMasterBO> retailerList;

    @BindView(R.id.legendGroup)
    Group legendGroup;

    @BindView(R.id.storeFilterSwitch)
    SwitchCompat storeFilterSwitch;

    MapWrapperLayout mapWrapperLayout;

    private TextView infoTitle;
    private TextView infoSnippet;
    private TextView infoProfile;
    private TextView infoAddToPlan;

    private OnInfoWindowElemTouchListener infoButtonListener;
    private OnInfoWindowElemTouchListener infoDeviateListener;

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
                .ivyAppComponent(((BusinessModel) Objects.requireNonNull(getActivity()).getApplication()).getComponent())
                .retailerModule(new RetailerModule(this, getActivity()))
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
                    profileClicked(marker);
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
    }


    @OnClick(R.id.retailer_legend_info_img)
    void onInfoImgClicked() {
        legendGroup.setVisibility(legendGroup.getVisibility() == View.VISIBLE ? View.GONE : View.VISIBLE);
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

    }


    @Override
    public void populateRetailers(List<RetailerMasterBO> retailerList) {

        boolean isFocusRetailer = false;
        LatLngBounds.Builder builder = new LatLngBounds.Builder();

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
            focusMarker(getMap(), builder);
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
        LatLngBounds.Builder builder = new LatLngBounds.Builder();
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
            focusMarker(getMap(), builder);
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
    public void onInfoWindowClick(Marker marker) {

    }

    @Override
    public boolean onMarkerClick(Marker marker) {

       if (getResources().getString(R.string.my_location).equals(marker.getTitle()))
        return true;

        marker.showInfoWindow();
        return false;
    }

    @Override
    public int getMapContainerResId() {
        return R.id.mapContainerLayout;
    }

    @Override
    public void onLocationResult(Location location) {
        LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
        MarkerOptions mMarkerOptions = new MarkerOptions()
                .position(latLng)
                .title(getResources().getString(R.string.my_location))
                .icon(BitmapDescriptorFactory
                        .fromResource(R.drawable.marker));
        addMarkerToMap(mMarkerOptions);
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


            }
        });

        presenter.fetchRetailerList();

        enableUserLocation();
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

            infoProfile.setVisibility(View.VISIBLE);
            String[] str_snippet = marker.getSnippet().split("\n");
            String str_title = marker.getTitle().split(",")[0];
            infoTitle.setText(str_title);
            infoSnippet.setText(str_snippet[0]);
            String isPlanned = marker.getTitle().split(",")[2];
            if ("Y".equals(isPlanned))
                infoAddToPlan.setVisibility(View.VISIBLE);
            else
                infoAddToPlan.setVisibility(View.GONE);

            infoButtonListener.setMarker(marker);
            infoDeviateListener.setMarker(marker);
            mapWrapperLayout.setMarkerWithInfoWindow(marker, infoWindow);
            return infoWindow;
        }

    }

    private void profileClicked(Marker marker){
        for (RetailerMasterBO startVisitBo : retailerList) {
            if (startVisitBo.getRetailerID().equals(marker.getTitle().split(",")[1])) {
                presenter.setRetailerMasterBo(startVisitBo);
                presenter.fetchLinkRetailer();
                Intent i = new Intent(getActivity(), ProfileActivity.class);
                i.putExtra("From", "RetailerMap");
                i.putExtra("locvisit", true);
                i.putExtra("map", true);
                if ("N".equals(marker.getTitle().split(",")[2]))
                    i.putExtra("hometwo", true);
                startActivity(i);
                break;
            }
        }
    }

    public void focusMarker(GoogleMap map, final LatLngBounds.Builder builder) {

        map.setOnMapLoadedCallback(new GoogleMap.OnMapLoadedCallback() {
            @Override
            public void onMapLoaded() {

                if (checkAreaBoundsTooSmall(builder.build())) {
                    map.animateCamera(CameraUpdateFactory.newLatLngZoom(builder.build().getCenter(), 19));
                } else {
                    map.animateCamera(CameraUpdateFactory.newLatLngBounds(builder.build(), 60));
                }
            }
        });
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
}
