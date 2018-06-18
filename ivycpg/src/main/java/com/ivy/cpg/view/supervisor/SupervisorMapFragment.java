package com.ivy.cpg.view.supervisor;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetBehavior;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.PagerAdapter;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.DataSnapshot;
import com.ivy.cpg.view.supervisor.adapter.EnhancedWrapContentViewPager;
import com.ivy.cpg.view.supervisor.helper.DetailsBo;
import com.ivy.cpg.view.supervisor.helper.RecyclerViewPager;
import com.ivy.maplib.MapWrapperLayout;
import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.commons.IvyBaseFragment;
import com.ivy.sd.png.commons.SDUtil;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.provider.ConfigurationMasterHelper;
import com.ivy.sd.png.util.Commons;
import com.ivy.sd.png.view.HomeScreenActivity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class SupervisorMapFragment extends IvyBaseFragment implements
        OnMapReadyCallback, Seller, View.OnClickListener, GoogleMap.OnMarkerClickListener, GoogleMap.OnInfoWindowClickListener {

    private BusinessModel bmodel;
    View view;
    private HashMap<String, DetailsBo> userHashmap = new HashMap<>();
    private GoogleMap mMap;
    boolean isFirst = true;
    private BottomSheetBehavior bottomSheetBehavior;
    private TextView totalSeller, absentSeller, marketSeller
//            ,outSeller
            ;
    private int totalSellerCount = 0;
    MapWrapperLayout mapWrapperLayout;
    private ViewGroup mymarkerview;
    private TextView tvMapInfoUserName, tvUserName, tvTimeIn, tvAddress, tvOutletCovered, tvOutletTarget, tvCoveredOutlet, tvUnbilledOutlet, tvTotalOutlet, tvOrderValue;
    private LinearLayout routeLayout, infoWindowLayout;

    private int trackingType; //0 - RealTime, 1 - Movement Tracking, 2 - Call analysis
    private RecyclerViewPager mRecyclerView;

    private MyAdapter myAdapter;

    ArrayList<DetailsBo> detailsBos = new ArrayList<>();


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        bmodel = (BusinessModel) getActivity().getApplicationContext();
        bmodel.setContext(getActivity());
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        SupervisorActivityHelper.getInstance().loginToFirebase(getContext());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        view = inflater.inflate(R.layout.activity_supervisor_home, container, false);

        getActivity().getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowTitleEnabled(false);
            actionBar.setDisplayShowHomeEnabled(true);
        }

        Bundle extras = getArguments();
        //Set Screen Title
        try {
            if (extras != null) {
                trackingType = extras.getInt("TrackingType");
                setScreenTitle(extras.getString("screentitle"));
            }
        } catch (Exception e) {

            setScreenTitle("MENU_SUPERVISOR");
            Commons.printException(e);
        }

        initViews(view);
        initViewPager();
        setviewValues();

        return view;
    }

    private void initViews(final View view) {
        SupportMapFragment mapFragment = (SupportMapFragment) this.getChildFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        this.mymarkerview = (ViewGroup) getLayoutInflater().inflate(R.layout.map_custom_info_window, null);

        totalSeller = view.findViewById(R.id.tv_ttl_seller);
        absentSeller = view.findViewById(R.id.tv_ttl_absent_seller);
        marketSeller = view.findViewById(R.id.tv_ttl_market_seller);
        tvCoveredOutlet = view.findViewById(R.id.tv_covered_outlet);
        tvUnbilledOutlet = view.findViewById(R.id.tv_unbilled_outlet);
        tvTotalOutlet = view.findViewById(R.id.tv_ttl_outlet);
        tvOrderValue = view.findViewById(R.id.tv_order_value);

        infoWindowLayout = view.findViewById(R.id.user_info_layout);
        infoWindowLayout.setVisibility(View.GONE);

        tvUserName = view.findViewById(R.id.tv_user_name);
        tvTimeIn = view.findViewById(R.id.tv_start_time);
        tvAddress = view.findViewById(R.id.tv_address);
        tvOutletTarget = view.findViewById(R.id.tv_target_outlet);
        tvOutletCovered = view.findViewById(R.id.tv_outlet_covered);

        routeLayout = view.findViewById(R.id.route_layout);

        //Bottom sheet layout Typeface
        ((TextView) view.findViewById(R.id.tv_txt_ttl_seller)).setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.REGULAR));
        ((TextView) view.findViewById(R.id.tv_txt_ttl_outlet)).setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.REGULAR));
        ((TextView) view.findViewById(R.id.tv_txt_covered_outlet)).setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.REGULAR));
        ((TextView) view.findViewById(R.id.tv_txt_unbilled_outlet)).setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.REGULAR));
        ((TextView) view.findViewById(R.id.tv_txt_order_value)).setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.REGULAR));
        ((TextView) view.findViewById(R.id.tv_txt_ttl_market_seller)).setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.REGULAR));
        ((TextView) view.findViewById(R.id.tv_txt_ttl_absent_seller)).setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.REGULAR));


        totalSeller.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
        absentSeller.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
        marketSeller.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
        tvCoveredOutlet.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
        tvUnbilledOutlet.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
        tvTotalOutlet.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
        tvOrderValue.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));

        //Type face for info window Layout
        tvUserName.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
        tvTimeIn.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.REGULAR));
        tvAddress.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.REGULAR));
        tvOutletTarget.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.REGULAR));
        tvOutletCovered.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.REGULAR));

        ((TextView) view.findViewById(R.id.tv_message)).setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.REGULAR));
        ((TextView) view.findViewById(R.id.tv_route)).setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.REGULAR));

        mapWrapperLayout = view.findViewById(R.id.map_wrap_layout);
        mapWrapperLayout.init(mMap, getPixelsFromDp(SupervisorMapFragment.this.getActivity(), 39 + 20));

        tvMapInfoUserName = mymarkerview.findViewById(R.id.tv_usr_name);

        bottomSheetBehavior = BottomSheetBehavior.from(view.findViewById(R.id.bottomSheetLayout));

    }

    private int getPixelsFromDp(Context context, float dp) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dp * scale + 0.5f);
    }

    private void setviewValues() {

        if (trackingType == 2)
            view.findViewById(R.id.bottomSheetLayout).setVisibility(View.VISIBLE);
        else
            view.findViewById(R.id.bottomSheetLayout).setVisibility(View.GONE);

        bottomSheetBehavior.setHideable(false);
        bottomSheetBehavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                switch (newState) {
                    case BottomSheetBehavior.STATE_COLLAPSED:
                        break;
                    case BottomSheetBehavior.STATE_DRAGGING:
                        break;
                    case BottomSheetBehavior.STATE_EXPANDED:
                        break;
                    case BottomSheetBehavior.STATE_HIDDEN:
                        break;
                    case BottomSheetBehavior.STATE_SETTLING:
                        break;
                }
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {
            }
        });

        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);

        //Total Seller count under Supervisor
        totalSellerCount = SupervisorActivityHelper.getInstance().getSellersCount(getContext(), bmodel);

        totalSeller.setText(String.valueOf(totalSellerCount));

        //Initialize the firebase instance and update the seller details
        SupervisorActivityHelper.getInstance().subscribeSellersUpdates(getContext(), this);

        view.findViewById(R.id.ttl_seller_layout).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), SellerListActivity.class);
                intent.putExtra("TabPos", 0);
                intent.putExtra("Screen", "Seller");
                startActivity(intent);
            }
        });

        view.findViewById(R.id.ttl_outlet_layout).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), OutletMapListActivity.class);
                intent.putExtra("TabPos", 0);
                intent.putExtra("Screen", "Outlet");
                startActivity(intent);
            }
        });

        view.findViewById(R.id.covered_outlet_layout).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), OutletMapListActivity.class);
                intent.putExtra("TabPos", 1);
                intent.putExtra("Screen", "Outlet");
                startActivity(intent);
            }
        });

        view.findViewById(R.id.unbilled_layout).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), OutletMapListActivity.class);
                intent.putExtra("TabPos", 2);
                intent.putExtra("Screen", "Outlet");
                startActivity(intent);
            }
        });
        ;

        view.findViewById(R.id.seller_view_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getContext(), SellerPerformanceListActivity.class);
                intent.putExtra("Screen", "Seller Performance");
                startActivity(intent);
            }
        });

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMaxZoomPreference(24);
        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {


        }else{
            mMap.setMyLocationEnabled(true);
        }

        mMap.getUiSettings().setMyLocationButtonEnabled(false);

        //map style restricting landmarks
//        mMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(getContext(), R.raw.map_style));

        mMap.setOnMarkerClickListener(this);
        mMap.setInfoWindowAdapter(new CustomInfoWindowAdapter());
//        mMap.setOnInfoWindowClickListener(this);
        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {

            @Override
            public void onMapClick(LatLng latLng) {

                mRecyclerView.setVisibility(View.GONE);

                if(bottomSheetBehavior.getState() == BottomSheetBehavior.STATE_EXPANDED)
                    bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                else if(bottomSheetBehavior.getState() == BottomSheetBehavior.STATE_COLLAPSED) {
                    bottomSheetBehavior.setHideable(true);
                    bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
                }
                else if(bottomSheetBehavior.getState() == BottomSheetBehavior.STATE_HIDDEN)
                    bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);

            }
        });

        mMap.setOnMyLocationButtonClickListener(new GoogleMap.OnMyLocationButtonClickListener(){
            @Override
            public boolean onMyLocationButtonClick()
            {
                LatLngBounds.Builder builder = new LatLngBounds.Builder();

                for (DetailsBo detailsBo : userHashmap.values()) {
                    builder.include(detailsBo.getMarker().getPosition());
                }

                mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(builder.build(), 200));

                return false;
            }
        });

        SupervisorActivityHelper.getInstance().subscribeSellerLocationUpdates(getContext(),this,trackingType);
    }

    @Override
    public void setSellerMarker(DataSnapshot dataSnapshot) {

    }

    /**
     * When a location update is received, put or update
     its value in mMarkers, which contains all the markers
     for locations received, so that we can build the
     boundaries required to show them all on the map at once*/
    @Override
    public void setMarker(DataSnapshot dataSnapshot) {
        String key = dataSnapshot.getKey();

        if(dataSnapshot.getValue() != null) {

            HashMap<String, Object> value = (HashMap<String, Object>) dataSnapshot.getValue();

            String userName = value.get("userName")!=null?value.get("userName").toString():"";
            String status = value.get("status")!=null?value.get("status").toString():"";
            String activityStatus = value.get("activityType")!=null?value.get("activityType").toString():"";
            String inTime = value.get("inTime")!=null?value.get("inTime").toString():"0";
            String outTime = value.get("outTime")!=null?value.get("outTime").toString():"0";
            int batteryStatus = Integer.valueOf(value.get("batterStatus")!=null?value.get("batterStatus").toString():"0");
            double lat = SDUtil.convertToDouble(value.get("latitude")!= null?value.get("latitude").toString():"0");
            double lng = SDUtil.convertToDouble(value.get("latitude")!= null?value.get("longitude").toString():"0");

            String syncTime =  value.get("time")!=null?value.get("time").toString():"0";

            LatLng destLatLng = new LatLng(lat, lng);

            if (!userHashmap.containsKey(key)) {

                DetailsBo detailsBo = new DetailsBo();
                detailsBo.setUserName(userName);
                detailsBo.setActivityName(activityStatus);
                detailsBo.setStatus(status);
                detailsBo.setBatterStatus(batteryStatus);
                detailsBo.setInTime(inTime);
                detailsBo.setOutTime(outTime);
                detailsBo.setTime(syncTime);
                detailsBo.setUserId(Integer.valueOf(key));
                detailsBo.setMarker(mMap.addMarker(new MarkerOptions().flat(true).
                        title(userName).position(destLatLng).snippet(key)));

                userHashmap.put(key,detailsBo);

//                userHashmap.get(key).getMarker().showInfoWindow();

                BitmapDescriptor icon = BitmapDescriptorFactory.fromResource(R.drawable.marker);

//                LinearLayout tv = (LinearLayout) this.getLayoutInflater().inflate(R.layout.seller_map_info_window, null, false);
//                tv.measure(View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
//                        View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
//                tv.layout(0, 0, tv.getMeasuredWidth(), tv.getMeasuredHeight());
//
//                tv.setDrawingCacheEnabled(true);
//                tv.buildDrawingCache();
//                Bitmap bm = tv.getDrawingCache();
//
//                BitmapDescriptor icon = BitmapDescriptorFactory.fromBitmap(bm);

                userHashmap.get(key).getMarker().setIcon(icon);


            } else {

                isFirst = false;

                userHashmap.get(key).setActivityName(activityStatus);
                userHashmap.get(key).setStatus(status);
                userHashmap.get(key).setBatterStatus(batteryStatus);
                userHashmap.get(key).setInTime(inTime);
                userHashmap.get(key).setOutTime(outTime);
                userHashmap.get(key).setTime(syncTime);
                userHashmap.get(key).setUserId(Integer.valueOf(key));

                //Animate the marker movement
                SupervisorActivityHelper.getInstance().animateMarkerNew(destLatLng,userHashmap.get(key).getMarker(),mMap);

            }



//            double angle = 130.0;
//            double x = Math.sin(-angle * Math.PI / 180) * 0.5 + 4.2;
//            double y = -(Math.cos(-angle * Math.PI / 180) * 0.5 - 0.7);
//            userHashmap.get(key).getMarker().setInfoWindowAnchor((float)x, (float)y);
//
//            if(userHashmap.get(key).getMarker()!=null &
//                    !userHashmap.get(key).getMarker().isInfoWindowShown())
//                userHashmap.get(key).getMarker().showInfoWindow();

            LatLngBounds.Builder builder = new LatLngBounds.Builder();

            //Will animate only for the first time when app opens
            if(isFirst) {
                for (DetailsBo detailsBo : userHashmap.values()) {
                    builder.include(detailsBo.getMarker().getPosition());
                }

                mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(builder.build(), 200));
            }

        }
    }

    /**
     * This method receives the absent Seller count and Closed Seller count from firebase data
     */
    @Override
    public void updateSellerInfo(DataSnapshot dataSnapshot) {

        marketSeller.setText(String.valueOf((int)dataSnapshot.getChildrenCount()));

        int absentSellerCount = totalSellerCount - (int)dataSnapshot.getChildrenCount();
        absentSeller.setText(String.valueOf(absentSellerCount));

        int outSellerCount = 0;
        for (DataSnapshot s : dataSnapshot.getChildren()) {
            if (s.child("status").getValue() != null) {
                if (!s.child("status").getValue().toString().equalsIgnoreCase("IN")) {
                    outSellerCount = outSellerCount + 1;
                }
            }

            DetailsBo detailsBo = SupervisorActivityHelper.getInstance().getDetailsBoHashMap().get(s.getKey());
            if(detailsBo!=null){
                detailsBo.setStatus("In Market");
            }
        }
    }

    @Override
    public void onClick(View view) {


    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        Commons.print("on Marker Click called");

        if(bottomSheetBehavior.getState() == BottomSheetBehavior.STATE_EXPANDED || bottomSheetBehavior.getState() == BottomSheetBehavior.STATE_COLLAPSED) {
            bottomSheetBehavior.setHideable(true);
            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
        }

        mRecyclerView.setVisibility(View.VISIBLE);

        detailsBos =  new ArrayList<>(userHashmap.values());

        myAdapter.notifyDataSetChanged();

        int pagerPos = 0;
        int count=0;
        for(DetailsBo detailsBo : userHashmap.values()){
            if(detailsBo.getMarker().getSnippet().equalsIgnoreCase(marker.getSnippet())){
                pagerPos = count;
                break;
            }
            count = count+1;
        }

        mRecyclerView.scrollToPosition(pagerPos);

        return true;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_supervisor_screen, menu);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            startActivity(new Intent(getActivity(),
                    HomeScreenActivity.class));
            getActivity().finish();
            return true;
        }
        else if(item.getItemId() == R.id.menu_dashboard){
            if(bottomSheetBehavior.getState() == BottomSheetBehavior.STATE_HIDDEN ||
                    bottomSheetBehavior.getState() == BottomSheetBehavior.STATE_COLLAPSED)
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
            else {
                if(mRecyclerView.getVisibility() == View.GONE)
                    bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                else{
                    bottomSheetBehavior.setHideable(true);
                    bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
                }

            }
        }

        return false;
    }

    private void showInfoWindow(final Marker marker){

        DetailsBo detailsBo = userHashmap.get(marker.getSnippet());

        if(detailsBo != null) {

            infoWindowLayout.setVisibility(View.VISIBLE);

            tvUserName.setText(detailsBo.getUserName());

            String address = "Last Visit : " +
                    SupervisorActivityHelper.getInstance().getAddressLatLong(getContext(), detailsBo.getMarker().getPosition()) ;
            tvAddress.setText(address);

            tvTimeIn.setText("Day Start : " +
                    SupervisorActivityHelper.getInstance().getTimeFromMillis(detailsBo.getInTime()));

            routeLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(getActivity(), SellerMapViewActivity.class);
                    intent.putExtra("SellerId", marker.getSnippet());
                    intent.putExtra("screentitle", marker.getTitle());
                    intent.putExtra("TrackingType", trackingType);
                    startActivity(intent);
                }
            });
        }
    }

    @Override
    public void onInfoWindowClick(Marker marker) {

        if(bottomSheetBehavior.getState() == BottomSheetBehavior.STATE_EXPANDED || bottomSheetBehavior.getState() == BottomSheetBehavior.STATE_COLLAPSED) {
            bottomSheetBehavior.setHideable(true);
            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
        }

        mRecyclerView.setVisibility(View.VISIBLE);

        detailsBos =  new ArrayList<>(userHashmap.values());

        myAdapter.notifyDataSetChanged();

        int pagerPos = 0;
        int count=0;
        for(DetailsBo detailsBo : userHashmap.values()){
            if(detailsBo.getMarker().getSnippet().equalsIgnoreCase(marker.getSnippet())){
                pagerPos = count;
                break;
            }
            count = count+1;
        }

        mRecyclerView.scrollToPosition(pagerPos);

    }

    class CustomInfoWindowAdapter implements GoogleMap.InfoWindowAdapter {

        @Override
        public View getInfoContents(Marker marker) {
            return null;
        }

        @Override
        public View getInfoWindow(final Marker marker) {

            tvMapInfoUserName.setText(marker.getTitle()+" Id "+marker.getSnippet());

            mapWrapperLayout.setMarkerWithInfoWindow(marker, mymarkerview);

            return mymarkerview;
        }

    }

    protected void initViewPager() {
        mRecyclerView = view.findViewById(R.id.viewpager);
        mRecyclerView.setVisibility(View.GONE);
        LinearLayoutManager layout = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL,
                false);
        mRecyclerView.setLayoutManager(layout);

        myAdapter = new MyAdapter();
        mRecyclerView.setAdapter(myAdapter);

        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLongClickable(true);
        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int scrollState) {
//                updateState(scrollState);
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int i, int i2) {
//                mPositionText.setText("First: " + mRecyclerViewPager.getFirstVisiblePosition());
                int childCount = mRecyclerView.getChildCount();
                int width = mRecyclerView.getChildAt(0).getWidth();
                int padding = (mRecyclerView.getWidth() - width) / 2;
//                mCountText.setText("Count: " + childCount);

                for (int j = 0; j < childCount; j++) {
                    View v = recyclerView.getChildAt(j);
                    //往左 从 padding 到 -(v.getWidth()-padding) 的过程中，由大到小
                    float rate = 0;
                    ;
                    if (v.getLeft() <= padding) {
                        if (v.getLeft() >= padding - v.getWidth()) {
                            rate = (padding - v.getLeft()) * 1f / v.getWidth();
                        } else {
                            rate = 1;
                        }
                        v.setScaleY(1 - rate * 0.1f);
                        v.setScaleX(1 - rate * 0.1f);

                    } else {
                        //往右 从 padding 到 recyclerView.getWidth()-padding 的过程中，由大到小
                        if (v.getLeft() <= recyclerView.getWidth() - padding) {
                            rate = (recyclerView.getWidth() - padding - v.getLeft()) * 1f / v.getWidth();
                        }
                        v.setScaleY(0.9f + rate * 0.1f);
                        v.setScaleX(0.9f + rate * 0.1f);
                    }
                }
            }
        });

        mRecyclerView.addOnPageChangedListener(new RecyclerViewPager.OnPageChangedListener() {
            @Override
            public void OnPageChanged(int oldPosition, int newPosition) {

                double angle = 130.0;
//                double x = Math.sin(-angle * Math.PI / 180) * 0.5 + 4.2;
//                double y = -(Math.cos(-angle * Math.PI / 180) * 0.5 - 0.7);

                double x = Math.sin(-angle * Math.PI / 180) * 0.5 + getResources().getDimension(R.dimen._3_4sdp);
                double y = -(Math.cos(-angle * Math.PI / 180) * 0.5 - getResources().getDimension(R.dimen._0_7sdp));
                detailsBos.get(newPosition).getMarker().setInfoWindowAnchor((float)x, (float)y);

                mMap.animateCamera(CameraUpdateFactory.newLatLng(detailsBos.get(newPosition).getMarker().getPosition()));
                detailsBos.get(newPosition).getMarker().showInfoWindow();

//                onMarkerClick(detailsBos.get(newPosition).getMarker());

            }
        });

        mRecyclerView.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                if (mRecyclerView.getChildCount() < 3) {
                    if (mRecyclerView.getChildAt(1) != null) {
                        if (mRecyclerView.getCurrentPosition() == 0) {
                            View v1 = mRecyclerView.getChildAt(1);
                            v1.setScaleY(0.9f);
                            v1.setScaleX(0.9f);
                        } else {
                            View v1 = mRecyclerView.getChildAt(0);
                            v1.setScaleY(0.9f);
                            v1.setScaleX(0.9f);
                        }
                    }
                } else {
                    if (mRecyclerView.getChildAt(0) != null) {
                        View v0 = mRecyclerView.getChildAt(0);
                        v0.setScaleY(0.9f);
                        v0.setScaleX(0.9f);
                    }
                    if (mRecyclerView.getChildAt(2) != null) {
                        View v2 = mRecyclerView.getChildAt(2);
                        v2.setScaleY(0.9f);
                        v2.setScaleX(0.9f);
                    }
                }

            }
        });
    }

    public class MyAdapter extends RecyclerView.Adapter<MyAdapter.MyViewHolder> {

        public class MyViewHolder extends RecyclerView.ViewHolder {

            private TextView userName;

            public MyViewHolder(View view) {
                super(view);

                userName = view.findViewById(R.id.tv_user_name);

            }
        }

        @Override
        public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.map_seller_info_layout, parent, false);

            return new MyViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(MyViewHolder holder, final int position) {

            final View itemView = holder.itemView;

            if(detailsBos!=null && detailsBos.size()>0) {

                holder.userName.setText(detailsBos.get(position).getUserName() + "---" + detailsBos.get(position).getMarker().getSnippet());

                itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(getContext(), SellerMapViewActivity.class);
                        intent.putExtra("SellerId", "1695");
                        intent.putExtra("screentitle", "Seller");
                        intent.putExtra("TrackingType", 1);
                        getContext().startActivity(intent);
                    }
                });
                final DetailsBo detailsBo = detailsBos.get(position);
            }
        }

        @Override
        public int getItemViewType(int position) {
            return position;
        }

        @Override
        public int getItemCount() {
            return detailsBos.size();
        }
    }

}
