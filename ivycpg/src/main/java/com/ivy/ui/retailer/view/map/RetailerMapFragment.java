package com.ivy.ui.retailer.view.map;

import android.content.Context;
import android.location.Location;
import android.support.constraint.ConstraintLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.ivy.core.base.view.BaseFragment;
import com.ivy.core.base.view.BaseMapFragment;
import com.ivy.maplib.MapWrapperLayout;
import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.bo.RetailerMasterBO;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.ui.retailer.RetailerContract;
import com.ivy.ui.retailer.di.DaggerRetailerComponent;
import com.ivy.ui.retailer.di.RetailerModule;
import com.ivy.ui.retailer.presenter.RetailerPresenterImpl;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import javax.inject.Inject;

import butterknife.BindView;

public class RetailerMapFragment extends BaseMapFragment implements RetailerContract.RetailerView,
        OnMapReadyCallback,GoogleMap.OnMarkerClickListener,
        GoogleMap.OnInfoWindowClickListener {

    private String screenTitle;
    private GoogleMap mMap;
    private ViewGroup infoWindow;
    private LayoutInflater layInflater;
    private Context context;

    private List<RetailerMasterBO> retailerList;

    @BindView(R.id.ll_view)
    LinearLayout switchLayout;

    @BindView(R.id.planningmapnew)
    MapWrapperLayout mapWrapperLayout;

    @BindView(R.id.img_legends_info)
    ImageView imgLegendInfo;

    @BindView(R.id.constraint_legends)
    ConstraintLayout legendConstraintLayout;

    @BindView(R.id.switch_plan)
    Switch switchPlanSelect;

    @BindView(R.id.tv_all)
    TextView selectedPlanNameTv;

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
        return R.layout.fragment_planning_map;
    }



    @Override
    public void initVariables(View view) {

        layInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE );

        switchLayout.setVisibility(View.GONE);

        this.infoWindow = (ViewGroup) layInflater.inflate(
                R.layout.custom_info_window, (ViewGroup) null);

        imgLegendInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (legendConstraintLayout.getVisibility() == View.GONE)
                    legendConstraintLayout.setVisibility(View.VISIBLE);
                else
                    legendConstraintLayout.setVisibility(View.GONE);
            }
        });

        switchPlanSelect.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                String strPlannned;
                if (isChecked)
                    selectedPlanNameTv.setText(getResources().getString(R.string.all));
                else
                    selectedPlanNameTv.setText(getResources().getString(R.string.day_plan));

                if (mMap != null)
                    mMap.clear();

            }
        });
    }

    @Override
    protected void getMessageFromAliens() {
        if (getArguments() != null)
            screenTitle = getArguments().getString("screentitle");
    }

    @Override
    protected void setUpViews() {
        setUpToolbar(screenTitle);

        SupportMapFragment mapFragment = (SupportMapFragment) this.getChildFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }


    @Override
    public void populateRetailers(List<RetailerMasterBO> retailerList) {
        this.retailerList = retailerList;
        ArrayList<MarkerOptions> retailerMarkerList = new ArrayList<>();
        LatLngBounds.Builder builder = new LatLngBounds.Builder();

        for (RetailerMasterBO retailerMasterBO : retailerList) {

            if (retailerMasterBO.getLatitude() != 0 && retailerMasterBO.getLongitude() != 0) {

                LatLng latLng = new LatLng(retailerMasterBO.getLatitude(), retailerMasterBO.getLongitude());
                MarkerOptions mMarkerOptions = new MarkerOptions()
                        .position(latLng)
                        .title(retailerMasterBO.getRetailerName() + "," + retailerMasterBO.getRetailerID())
                        .snippet(retailerMasterBO.getAddress1())
                        .icon(BitmapDescriptorFactory
                                .fromResource(getMarkerIcon(retailerMasterBO)));

                addMarkerToMap(mMarkerOptions);
                builder.include(latLng);

                retailerMarkerList.add(mMarkerOptions);
            }
        }

        if (retailerMarkerList.size() > 0)
            focusMarker(getMap(),builder);


        presenter.prepareRetailerMarker(mMap,retailerList);
    }

    @Override
    public void populateRetailersMarker(List<MarkerOptions> retailerList) {
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
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        mapWrapperLayout.init(mMap, presenter.getPixelsFromDpInt(getContext()));

        mMap.setOnMarkerClickListener(this);
        mMap.setInfoWindowAdapter(new CustomInfoWindowAdapter());
        mMap.setOnInfoWindowClickListener(this);
        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {

            @Override
            public void onMapClick(LatLng latLng) {


            }
        });

        presenter.fetchRetailerList();
    }

    @Override
    public void onInfoWindowClick(Marker marker) {

    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        return false;
    }

    @Override
    public int getMapContainerResId() {
        return 0;
    }

    @Override
    public void onLocationResult(Location location) {

    }

    @Override
    public void onMapReady() {

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
            mapWrapperLayout.setMarkerWithInfoWindow(marker, infoWindow);
            return infoWindow;
        }

    }

    public void focusMarker(GoogleMap map,final LatLngBounds.Builder builder) {

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
