package com.ivy.cpg.view.supervisor;

import android.content.Context;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.ivy.maplib.MapWrapperLayout;
import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.commons.IvyBaseActivityNoActionBar;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.provider.ConfigurationMasterHelper;
import com.ivy.sd.png.util.Commons;

public class OutletMapListActivity extends IvyBaseActivityNoActionBar implements OnMapReadyCallback,GoogleMap.OnMarkerClickListener {

    private GoogleMap mMap;
    private CardView infoWindowLayout;
    private BusinessModel businessModel;
    private TabLayout tabLayout;
    private ViewGroup mymarkerview;
    private TextView tvMapInfoUserName;
    private MapWrapperLayout mapWrapperLayout;
    private int tabPos;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_outlet_map_list);

        getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        Toolbar toolbar = findViewById(R.id.toolbar);
        if (toolbar != null) {

            setSupportActionBar(toolbar);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        businessModel = (BusinessModel)getApplicationContext();

        overridePendingTransition(R.anim.trans_left_in, R.anim.trans_left_out);
        infoWindowLayout = findViewById(R.id.user_info_layout);
        infoWindowLayout.setVisibility(View.GONE);

        setScreenTitle("Total Outlets");
        Bundle extras = getIntent().getExtras();
        tabPos = extras!=null?extras.getInt("TabPos"):0;

        initViews();

        changeTabsFont(tabLayout);
    }

    private void initViews() {

        mymarkerview = (ViewGroup)getLayoutInflater().inflate(R.layout.map_custom_outlet_info_window, null);
        tvMapInfoUserName = mymarkerview.findViewById(R.id.tv_usr_name);

        mapWrapperLayout = findViewById(R.id.map_wrap_layout);
        mapWrapperLayout.init(mMap, getPixelsFromDp(this, getPixelsFromDp(this, 39 + 20)));

        tabLayout = findViewById(R.id.tab_layout);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {

                switch (tab.getPosition()) {
                    case 0:
                        setScreenTitle("Total Outlet (" + 3 + ")");
                        totalOutlet();
                        break;
                    case 1:
                        setScreenTitle("Covered (" + 2 + ")");
                        coveredOutlet();
                        break;
                    case 2:
                        setScreenTitle("UnBilled (" + 5 + ")");
                        unBilledOutlet();
                        break;
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }
        });
    }


    private void totalOutlet(){
        if(mMap!=null) {
            mMap.clear();

            LatLng destLatLng1 = new LatLng(12.922915, 80.127456);
            BitmapDescriptor icon1 = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN);
            mMap.addMarker(new MarkerOptions().flat(true).
                    title("Tamil arasu").position(destLatLng1).snippet("1695").icon(icon1));

            LatLng destLatLng2 = new LatLng(12.953195, 80.141601);
            BitmapDescriptor icon2 = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN);
            mMap.addMarker(new MarkerOptions().flat(true).
                    title("English ").position(destLatLng2).snippet("1698").icon(icon2));

            LatLng destLatLng3 = new LatLng(13.022480, 80.203187);
            BitmapDescriptor icon3 = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE);
            mMap.addMarker(new MarkerOptions().flat(true).
                    title("Karan").position(destLatLng3).snippet("1696").icon(icon3));

            LatLng destLatLng4 = new LatLng(12.975971, 80.221209);
            BitmapDescriptor icon4 = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE);
            mMap.addMarker(new MarkerOptions().flat(true).
                    title("Deepak").position(destLatLng4).snippet("1697").icon(icon4));

            LatLng destLatLng5 = new LatLng(12.965365, 80.246106);
            BitmapDescriptor icon5 = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED);
            mMap.addMarker(new MarkerOptions().flat(true).
                    title("Sandy").position(destLatLng5).snippet("1692").icon(icon5));


            final LatLngBounds.Builder builder = new LatLngBounds.Builder();
            builder.include(destLatLng1);
            builder.include(destLatLng2);
            builder.include(destLatLng3);
            builder.include(destLatLng4);
            builder.include(destLatLng5);
            mMap.setOnMapLoadedCallback(new GoogleMap.OnMapLoadedCallback() {
                @Override
                public void onMapLoaded() {
                    mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(builder.build(), 200));
                }
            });
        }
    }

    private void coveredOutlet(){
        if(mMap!=null) {
            mMap.clear();

            LatLng destLatLng3 = new LatLng(13.022480, 80.203187);
            BitmapDescriptor icon3 = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE);
            mMap.addMarker(new MarkerOptions().flat(true).
                    title("Karan").position(destLatLng3).snippet("1696").icon(icon3));

            LatLng destLatLng4 = new LatLng(12.975971, 80.221209);
            BitmapDescriptor icon4 = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_ORANGE);
            mMap.addMarker(new MarkerOptions().flat(true).
                    title("Deepak").position(destLatLng4).snippet("1697").icon(icon4));

            final LatLngBounds.Builder builder = new LatLngBounds.Builder();
            builder.include(destLatLng3);
            builder.include(destLatLng4);
            mMap.setOnMapLoadedCallback(new GoogleMap.OnMapLoadedCallback() {
                @Override
                public void onMapLoaded() {
                    mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(builder.build(), 200));
                }
            });
        }

    }

    private void unBilledOutlet(){
        if(mMap!=null) {
            mMap.clear();

            LatLng destLatLng5 = new LatLng(12.965365, 80.246106);
            BitmapDescriptor icon5 = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED);
            mMap.addMarker(new MarkerOptions().flat(true).
                    title("Sandy").position(destLatLng5).snippet("1692").icon(icon5));

            final LatLngBounds.Builder builder = new LatLngBounds.Builder();
            builder.include(destLatLng5);
            mMap.setOnMapLoadedCallback(new GoogleMap.OnMapLoadedCallback() {
                @Override
                public void onMapLoaded() {
                    mMap.moveCamera(CameraUpdateFactory.newLatLngBounds(builder.build(), 200));
                }
            });
        }
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        Commons.print("on Marker Click called");

        double angle = 130.0;
        double x = Math.sin(-angle * Math.PI / 180) * 0.5 + 3.9;
        double y = -(Math.cos(-angle * Math.PI / 180) * 0.5 - 1.1);
        marker.setInfoWindowAnchor((float)x, (float)y);

        mMap.animateCamera(CameraUpdateFactory.newLatLng(marker.getPosition()));

        marker.showInfoWindow();
        showInfoWindow(marker);

        return true;
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

        tabLayout.getTabAt(tabPos).select();

    }

    private void showInfoWindow(final Marker marker){

        infoWindowLayout.setVisibility(View.VISIBLE);

    }

    class CustomInfoWindowAdapter implements GoogleMap.InfoWindowAdapter {

        @Override
        public View getInfoContents(Marker marker) {
            return null;
        }

        @Override
        public View getInfoWindow(final Marker marker) {

            tvMapInfoUserName.setText("Big Text message for the info window to test size of info window");

            mapWrapperLayout.setMarkerWithInfoWindow(marker, mymarkerview);

            return mymarkerview;
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int i = item.getItemId();
        if (i == android.R.id.home) {
            finish();
            overridePendingTransition(R.anim.trans_right_in, R.anim.trans_right_out);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void changeTabsFont(TabLayout tabLayout) {

        ViewGroup vg = (ViewGroup) tabLayout.getChildAt(0);
        int tabsCount = vg.getChildCount();
        for (int j = 0; j < tabsCount; j++) {
            ViewGroup vgTab = (ViewGroup) vg.getChildAt(j);
            int tabChildCount = vgTab.getChildCount();
            for (int i = 0; i < tabChildCount; i++) {
                View tabViewChild = vgTab.getChildAt(i);
                if (tabViewChild instanceof TextView) {
                    ((TextView) tabViewChild).setTypeface(businessModel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
                }
            }
        }
    }

    private int getPixelsFromDp(Context context, float dp) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dp * scale + 0.5f);
    }
}
