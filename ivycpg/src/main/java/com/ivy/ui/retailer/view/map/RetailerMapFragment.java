package com.ivy.ui.retailer.view.map;

import android.app.Activity;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.location.Location;
import android.support.constraint.Group;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.SwitchCompat;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
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
import com.ivy.cpg.view.homescreen.HomeScreenActivity;
import com.ivy.maplib.MapWrapperLayout;
import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.bo.RetailerMasterBO;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.util.Commons;
import com.ivy.ui.offlineplan.addplan.DateWisePlanBo;
import com.ivy.ui.offlineplan.addplan.view.AddPlanDialogFragment;
import com.ivy.ui.offlineplan.calendar.view.CalendarPlanFragment;
import com.ivy.ui.retailer.RetailerContract;
import com.ivy.ui.retailer.di.DaggerRetailerComponent;
import com.ivy.ui.retailer.di.RetailerModule;
import com.ivy.ui.retailer.presenter.RetailerPresenterImpl;
import com.ivy.ui.retailer.filter.RetailerPlanFilterBo;
import com.ivy.ui.retailer.filter.view.RetailerPlanFilterFragment;
import com.ivy.utils.DeviceUtils;
import com.ivy.utils.NetworkUtils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Vector;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.OnClick;

import static android.content.res.Configuration.SCREENLAYOUT_SIZE_LARGE;
import static com.ivy.cpg.view.homescreen.HomeMenuConstants.MENU_MAP_PLAN;

public class RetailerMapFragment extends BaseMapFragment implements RetailerContract.RetailerView,
        GoogleMap.OnMarkerClickListener,
        GoogleMap.OnInfoWindowClickListener {

    private String screenTitle,date;
    private ViewGroup infoWindow;
    private Context context;

    private MapWrapperLayout mapWrapperLayout;

    private TextView infoTitle;

    private boolean isByWalk = false, isClickable;

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

    private RetailerMasterBO retailerMasterBO;

    private AddPlanDialogFragment addPlanDialogFragment;

    private RetailerPlanFilterFragment planFilterFragment;

    @Inject
    RetailerPresenterImpl<RetailerContract.RetailerView> presenter;

    private RetailerPlanFilterBo planFilterBo;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.context = context;
    }

    @Override
    public void initializeDi() {
        DaggerRetailerComponent.builder()
                .ivyAppComponent(((BusinessModel) Objects.requireNonNull((FragmentActivity)context).getApplication()).getComponent())
                .retailerModule(new RetailerModule(this))
                .build()
                .inject(RetailerMapFragment.this);

        setBasePresenter(presenter);
    }

    @Override
    protected int setContentViewLayout() {
        return R.layout.fragment_retailer_map;
    }

    @Override
    public void init(View view) {
        setHasOptionsMenu(true);

        ActionBar actionBar = ((AppCompatActivity) context).getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowTitleEnabled(false);
            actionBar.setDisplayShowHomeEnabled(true);
        }

        mapWrapperLayout = view.findViewById(R.id.mapContainerLayout);

        LayoutInflater layInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        this.infoWindow = (ViewGroup) layInflater.inflate(
                R.layout.retailer_custom_info_window, null);

        infoTitle = infoWindow.findViewById(R.id.title);

        builder = new LatLngBounds.Builder();

        storeFilterSwitch.setOnCheckedChangeListener(storeFilterCheckListener);

        // For 7" tablet
        boolean is7InchTablet = this.getResources().getConfiguration()
                .isLayoutSizeAtLeast(SCREENLAYOUT_SIZE_LARGE);
        if (!is7InchTablet) {
            bottomLayout.getLayoutParams().height = (int) getResources().getDimension(R.dimen.ratiler_map_bottomview_height);
        }

        walkDirBtn.setOnClickListener(directionWalkListener);

        carDirBtn.setOnClickListener(directionCarListener);
    }

    private View.OnClickListener directionWalkListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            isByWalk = true;
            isClickable = true;
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
    };

    private View.OnClickListener directionCarListener =new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            isByWalk = false;
            isClickable = true;
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
    };

    private CompoundButton.OnCheckedChangeListener storeFilterCheckListener = new CompoundButton.OnCheckedChangeListener() {
        @Override
        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

            if (getMap() != null)
                getMap().clear();

            if (addPlanDialogFragment != null && addPlanDialogFragment.isVisible())
                addPlanDialogFragment.dismiss();

            builder = new LatLngBounds.Builder();
            isFocusRetailer = false;

            if (planFilterBo != null && planFilterBo.getRetailerIds().isEmpty())
                return;

            if (isChecked) {
                presenter.fetchTodayPlannedRetailers();
                storeFilterSwitch.setText(getResources().getString(R.string.day_plan));
            }else {
                presenter.fetchRetailerList();
                storeFilterSwitch.setText(getResources().getString(R.string.all_retailer));
            }
        }
    };

    @OnClick(R.id.clear_route_id)
    void clearRouteButton(){
        isClickable = false;
        isByWalk = false;
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
        if (bottomLayout.getVisibility() == View.GONE) {
            bottomLayout.setVisibility(View.VISIBLE);
        } else {
            isClickable = false;
            isByWalk = false;
            clearRoute();
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
        if (getArguments() != null) {
            screenTitle = getArguments().getString("screentitle");
            date = getArguments().getString("DATE");
        }
    }

    @Override
    protected void setUpViews() {
        setUpToolbar(screenTitle);
//        presenter.fetchSelectedDateRetailerPlan(date);
        presenter.fetchSelectedDateRetailerPlan("2019/03/22");
        loadMap();
    }

    @Override
    public void populateRetailers(List<RetailerMasterBO> retailerList) {

        for (RetailerMasterBO retailerMasterBO : retailerList) {

            if (retailerMasterBO.getLatitude() != 0 && retailerMasterBO.getLongitude() != 0) {

                if (planFilterBo != null){
                    if (!planFilterBo.getRetailerIds().contains(retailerMasterBO.getRetailerID()))
                        continue;
                }
                addMarkerToMap(prepareMarkerOption(retailerMasterBO,builder));

                isFocusRetailer = true;
            }
        }

        focusMarker();
    }

    private MarkerOptions prepareMarkerOption(RetailerMasterBO retailerMasterBO, LatLngBounds.Builder builder){

        LatLng latLng = new LatLng(retailerMasterBO.getLatitude(), retailerMasterBO.getLongitude());
        MarkerOptions mMarkerOptions = new MarkerOptions()
                .position(latLng)
                .title(retailerMasterBO.getRetailerName() + "," + retailerMasterBO.getRetailerID())
                .icon(BitmapDescriptorFactory.fromResource(getMarkerIcon(retailerMasterBO)));


        builder.include(latLng);

        return mMarkerOptions;
    }

    private boolean isFocusRetailer = false;

    @Override
    public void populateTodayPlannedRetailers(RetailerMasterBO todayPlannedRetailer){

        if (todayPlannedRetailer.getLatitude() != 0 && todayPlannedRetailer.getLongitude() != 0) {
            if (planFilterBo != null ){
                if (!planFilterBo.getRetailerIds().contains(todayPlannedRetailer.getRetailerID()))
                    return;
            }
            addMarkerToMap(prepareMarkerOption(todayPlannedRetailer, builder));
            isFocusRetailer = true;
        }
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

        for (RetailerMasterBO retailerMasterBO : presenter.loadRetailerList()) {
            if (retailerMasterBO.getRetailerID().equals(marker.getTitle().split(",")[1])) {
                this.retailerMasterBO = retailerMasterBO;
                break;
            }
        }

        if (bottomLayout.getVisibility() == View.VISIBLE) {
            isClickable = false;
            onInfoWindowClick(marker);
        }else {
//            marker.showInfoWindow();

            ArrayList<DateWisePlanBo> planList = presenter.getSelectedDateRetailerPlanList();

            /*DateWisePlanBo dateWisePlanBo = new DateWisePlanBo();
            dateWisePlanBo.setStartTime("11:00");
            dateWisePlanBo.setEndTime("12:00");

            DateWisePlanBo dateWisePlanBo1 = new DateWisePlanBo();
            dateWisePlanBo1.setStartTime("14:00");
            dateWisePlanBo1.setEndTime("16:00");

            DateWisePlanBo dateWisePlanBo2 = new DateWisePlanBo();
            dateWisePlanBo2.setStartTime("18:00");
            dateWisePlanBo2.setEndTime("21:00");

            planList.add(dateWisePlanBo);
            planList.add(dateWisePlanBo1);
            planList.add(dateWisePlanBo2);*/

            addPlanDialogFragment =
                    new AddPlanDialogFragment(retailerMasterBO,
                            presenter.getSelectedRetailerPlan(retailerMasterBO.getRetailerID())
                    ,planList);
            addPlanDialogFragment.show(((FragmentActivity)context).getSupportFragmentManager(),
                    "add_plan_fragment");

            presenter.setRetailerMasterBo(retailerMasterBO);
        }
        return false;
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

                if (addPlanDialogFragment != null && addPlanDialogFragment.isVisible())
                    addPlanDialogFragment.dismiss();

            }
        });

        storeFilterSwitch.setChecked(true);

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

            String str_title = marker.getTitle().split(",")[0];
            infoTitle.setText(str_title);

            mapWrapperLayout.setMarkerWithInfoWindow(marker, infoWindow);
            return infoWindow;
        }

    }

    @Override
    public void focusMarker() {

        if (isFocusRetailer) {
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
            } catch (Exception e) {
                Commons.printException(e);
            }
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
            } else if (!isClickable) {
                testClearRoute = " ";
                fromTv.setText(testClearRoute);
                testClearRoute = " ";
                toTv.setText(testClearRoute);
                toText = "";
                if (bottomLayout.getVisibility() == View.GONE)
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
        mMarker.hideInfoWindow();
        markerLatLng[1] = mMarker.getPosition();
        toText = (getResources().getString(R.string.my_location).equals(mMarker.getTitle())) ? mMarker.getTitle()
                : mMarker.getTitle().split(",")[0];

        String mapKey = "key=" + getString(R.string.google_maps_api_key);

        showLoading(getResources().getString(R.string.fetching_route));
        presenter.fetchRoutePath(presenter.makeURL(markerLatLng[0].latitude, markerLatLng[0].longitude,
                markerLatLng[1].latitude, markerLatLng[1].longitude,mapKey, isByWalk));

        mClick = 2;
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

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);

        menu.findItem(R.id.map_retailer).setVisible(false);

    }

    private String searchText ="";

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_retailer_plan, menu);

        SearchManager searchManager = (SearchManager) context.getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView = (SearchView) menu.findItem(R.id.search).getActionView();
        ImageView searchClose = searchView.findViewById(android.support.v7.appcompat.R.id.search_close_btn);
        searchClose.setImageResource(android.R.drawable.ic_menu_close_clear_cancel);

        searchView.setSearchableInfo(searchManager != null ? searchManager.getSearchableInfo(((Activity)context).getComponentName()) : null);
        SearchView.OnQueryTextListener textChangeListener = new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextChange(String newText) {

                getMap().clear();

                presenter.prepareFilteredRetailerList(planFilterBo,newText.toLowerCase());
                searchText = newText;

                return true;
            }

            @Override
            public boolean onQueryTextSubmit(String query) {
                return true;
            }

        };
        searchView.setOnQueryTextListener(textChangeListener);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            startActivity(new Intent(getActivity(),
                    HomeScreenActivity.class));
            ((Activity)context).finish();
            return true;
        } else if (item.getItemId() == R.id.filter) {

            planFilterFragment =
                    new RetailerPlanFilterFragment(planFilterBo);
            planFilterFragment.show(((FragmentActivity)context).getSupportFragmentManager(),
                    "filter_plan_fragment");

            return true;
        }else if (item.getItemId() == R.id.calendar) {
            FragmentManager fm = ((FragmentActivity)context).getSupportFragmentManager();
            FragmentTransaction ft = fm.beginTransaction();

            CalendarPlanFragment fragment = new CalendarPlanFragment();
            ft.replace(R.id.fragment_content, fragment,MENU_MAP_PLAN);
            ft.commit();

            return true;
        }

        return false;
    }

    @Subscribe
    public void onMessageEvent(Object obj) {

        if (obj instanceof RetailerPlanFilterBo){

            planFilterBo = ((RetailerPlanFilterBo)obj);

            if (planFilterBo.getRetailerIds().isEmpty()) {
                onMessageEvent("NODATA");
                return;
            }

            getMap().clear();

            presenter.prepareFilteredRetailerList(((RetailerPlanFilterBo)obj),searchText.toLowerCase());
        }else if(obj instanceof String){
            if (((String)obj).equalsIgnoreCase("CLEAR")){

                if (planFilterBo != null) {
                    planFilterBo = null;
                }

                getMap().clear();
                if (storeFilterSwitch.isChecked()) {
                    presenter.fetchTodayPlannedRetailers();
                    storeFilterSwitch.setText(getResources().getString(R.string.day_plan));
                }else {
                    presenter.fetchRetailerList();
                    storeFilterSwitch.setText(getResources().getString(R.string.all_retailer));
                }

            }else if ("NODATA".equalsIgnoreCase((String)obj)){
                getMap().clear();
            }
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        if (!EventBus.getDefault().isRegistered(this))
            EventBus.getDefault().register(this);

    }

    @Override
    public void onStop() {
        if (EventBus.getDefault().isRegistered(this))
            EventBus.getDefault().unregister(this);
        super.onStop();
    }
}
