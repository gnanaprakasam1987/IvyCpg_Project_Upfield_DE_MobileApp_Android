package com.ivy.cpg.view.supervisor;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetBehavior;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.Projection;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.firebase.database.DataSnapshot;
import com.ivy.maplib.MapWrapperLayout;
import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.commons.IvyBaseFragment;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.provider.ConfigurationMasterHelper;
import com.ivy.sd.png.util.Commons;
import com.ivy.sd.png.view.HomeScreenActivity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class SupervisorMapFragment extends IvyBaseFragment implements
        OnMapReadyCallback, Seller,View.OnClickListener,GoogleMap.OnMarkerClickListener,GoogleMap.OnInfoWindowClickListener {

    private BusinessModel bmodel;
    View view;
    private HashMap<String, DetailsBo> userHashmap = new HashMap<>();
    private GoogleMap mMap;
    boolean isFirst = true;
    private BottomSheetBehavior bottomSheetBehavior;
    private TextView totalSeller,absentSeller,marketSeller
//            ,outSeller
            ;
    private int totalSellerCount = 0;
    MapWrapperLayout mapWrapperLayout;
    private ViewGroup mymarkerview;
    private TextView tvMapInfoUserName,tvUserName , tvTimeIn, tvAddress, tvOutletCovered,tvOutletTarget
            ,tvCoveredOutlet,tvUnbilledOutlet,tvTotalOutlet,tvOrderValue;
    private LinearLayout routeLayout,infoWindowLayout,bottomLayout;

    private int trackingType; //0 - RealTime, 1 - Movement Tracking, 2 - Call analysis
    ViewPagerAdapter viewPagerAdapter;
    ViewPager viewPager;


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
            if (extras != null){
                trackingType = extras.getInt("TrackingType");
                setScreenTitle(extras.getString("screentitle"));
            }
        } catch (Exception e) {

            setScreenTitle("MENU_SUPERVISOR");
            Commons.printException(e);
        }

        initViews(view);
        setviewValues();

        return view;
    }

    private void initViews(final View view){
        SupportMapFragment mapFragment = (SupportMapFragment) this.getChildFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        this.mymarkerview = (ViewGroup)getLayoutInflater().inflate(R.layout.map_custom_info_window, null);

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
        bottomLayout = view.findViewById(R.id.bottom_layout);

        //Bottom sheet layout Typeface
        ((TextView)view.findViewById(R.id.tv_txt_ttl_seller)).setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.REGULAR));
        ((TextView)view.findViewById(R.id.tv_txt_ttl_outlet)).setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.REGULAR));
        ((TextView)view.findViewById(R.id.tv_txt_covered_outlet)).setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.REGULAR));
        ((TextView)view.findViewById(R.id.tv_txt_unbilled_outlet)).setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.REGULAR));
        ((TextView)view.findViewById(R.id.tv_txt_order_value)).setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.REGULAR));
        ((TextView)view.findViewById(R.id.tv_txt_ttl_market_seller)).setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.REGULAR));
        ((TextView)view.findViewById(R.id.tv_txt_ttl_absent_seller)).setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.REGULAR));


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

        ((TextView)view.findViewById(R.id.tv_message)).setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.REGULAR));
        ((TextView)view.findViewById(R.id.tv_route)).setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.REGULAR));

        mapWrapperLayout = view.findViewById(R.id.map_wrap_layout);
        mapWrapperLayout.init(mMap, getPixelsFromDp(SupervisorMapFragment.this.getActivity(), 39 + 20));

        tvMapInfoUserName = mymarkerview.findViewById(R.id.tv_usr_name);

        bottomSheetBehavior = BottomSheetBehavior.from(view.findViewById(R.id.bottomSheetLayout));

        if(trackingType == 2)
            view.findViewById(R.id.bottomSheetLayout).setVisibility(View.VISIBLE);
        else
            view.findViewById(R.id.bottomSheetLayout).setVisibility(View.GONE);

    }

    private int getPixelsFromDp(Context context, float dp) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dp * scale + 0.5f);
    }

    private void setviewValues(){

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

        viewPager = view.findViewById(R.id.view_pager);

        viewPager.setClipToPadding(false);
//        viewPager.setPadding(48, 0, 48, 0);
        viewPager.setPageMargin(1);
        viewPager.setOffscreenPageLimit(1);

        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);

        //Total Seller count under Supervisor
        totalSellerCount = SupervisorActivityHelper.getInstance().getSellersCount(getContext(),bmodel);

        totalSeller.setText(String.valueOf(totalSellerCount));

        //Initialize the firebase instance and update the seller details
        SupervisorActivityHelper.getInstance().subscribeSellersUpdates(getContext(),this);

        totalSeller.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(),SellerListActivity.class);
                intent.putExtra("TabPos",0);
                intent.putExtra("Screen","Seller");
                startActivity(intent);
            }
        });

        absentSeller.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(),SellerListActivity.class);
                intent.putExtra("TabPos",1);
                intent.putExtra("Screen","Seller");
                startActivity(intent);
            }
        });

        marketSeller.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(),SellerListActivity.class);
                intent.putExtra("TabPos",2);
                intent.putExtra("Screen","Seller");
                startActivity(intent);
            }
        });

        tvTotalOutlet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(),OutletMapListActivity.class);
                intent.putExtra("TabPos",1);
                intent.putExtra("Screen","Outlet");
                startActivity(intent);
            }
        });

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            startActivity(new Intent(getActivity(),
                    HomeScreenActivity.class));
            getActivity().finish();
            return true;
        }

        return false;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMaxZoomPreference(24);
        mMap.getUiSettings().setMapToolbarEnabled(false);
        mMap.setOnMarkerClickListener(this);
        mMap.setInfoWindowAdapter(new CustomInfoWindowAdapter());
        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {

            @Override
            public void onMapClick(LatLng latLng) {
                if(infoWindowLayout.getVisibility() == View.VISIBLE)
                    infoWindowLayout.setVisibility(View.GONE);
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
            double lat = Double.parseDouble(value.get("latitude")!= null?value.get("latitude").toString():"0");
            double lng = Double.parseDouble(value.get("latitude")!= null?value.get("longitude").toString():"0");

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

            BitmapDescriptor icon = BitmapDescriptorFactory.fromResource(R.drawable.marker);

            userHashmap.get(key).getMarker().setIcon(icon);

            LatLngBounds.Builder builder = new LatLngBounds.Builder();

            //Will animate only for the first time when app opens
            if(isFirst) {
                for (DetailsBo detailsBo : userHashmap.values()) {
                    builder.include(detailsBo.getMarker().getPosition());
                }

                mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(builder.build(), 200));
            }

//            viewPagerAdapter = new ViewPagerAdapter(getContext(), new ArrayList<>(userHashmap.values()));
//            viewPager.setAdapter(viewPagerAdapter);
//            viewPagerAdapter.notifyDataSetChanged();
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

        double angle = 130.0;
        double x = Math.sin(-angle * Math.PI / 180) * 0.5 + 4.0;
        double y = -(Math.cos(-angle * Math.PI / 180) * 0.5 - 0.6);
        marker.setInfoWindowAnchor((float)x, (float)y);

        mMap.animateCamera(CameraUpdateFactory.newLatLng(marker.getPosition()));
        marker.showInfoWindow();
        showInfoWindow(marker);

        if(bottomSheetBehavior.getState() == BottomSheetBehavior.STATE_EXPANDED)
            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);

        return true;
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

//        Intent intent = new Intent(getActivity(),SellerMapViewActivity.class);
//        intent.putExtra("SellerId",marker.getSnippet());
//        intent.putExtra("screentitle",marker.getTitle());
//        intent.putExtra("TrackingType",trackingType);
//        startActivity(intent);

    }

    class CustomInfoWindowAdapter implements GoogleMap.InfoWindowAdapter {

        @Override
        public View getInfoContents(Marker marker) {
            return null;
        }

        @Override
        public View getInfoWindow(final Marker marker) {

            tvMapInfoUserName.setText("Big Text To test the info");

            mapWrapperLayout.setMarkerWithInfoWindow(marker, mymarkerview);

            return mymarkerview;
        }

    }

    public class ViewPagerAdapter extends PagerAdapter {

        private Context mContext;
        private List<DetailsBo> userHashmap = new ArrayList<>();

        public ViewPagerAdapter(Context context, List<DetailsBo> userHashmap) {
            mContext = context;
            this.userHashmap = userHashmap;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((View) object);
        }

        @Override
        public int getCount() {
            return userHashmap.size();
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }

        @Override
        public Object instantiateItem(ViewGroup container, final int position) {
            LayoutInflater inflater = LayoutInflater.from(mContext);
            ViewGroup view = (ViewGroup) inflater.inflate(R.layout.map_seller_info_layout, container, false);

            userHashmap.get(position).getMarker().showInfoWindow();

            container.addView(view);
            return view;
        }
    }

}
