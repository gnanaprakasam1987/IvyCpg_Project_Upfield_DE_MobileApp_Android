package com.ivy.cpg.view.supervisor.mvp.outletmapview;

import android.app.SearchManager;
import android.content.Context;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
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
import com.ivy.cpg.view.supervisor.customviews.recyclerviewpager.RecyclerViewPager;
import com.ivy.cpg.view.supervisor.fragments.OutletPagerDialogFragment;
import com.ivy.lib.DialogFragment;
import com.ivy.maplib.MapWrapperLayout;
import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.commons.IvyBaseActivityNoActionBar;
import com.ivy.sd.png.util.Commons;
import com.ivy.utils.FontUtils;

public class OutletMapListActivity extends IvyBaseActivityNoActionBar implements OnMapReadyCallback,GoogleMap.OnMarkerClickListener,GoogleMap.OnInfoWindowClickListener {

    private GoogleMap mMap;
    private TabLayout tabLayout;
    private ViewGroup mymarkerview;
    private TextView tvMapInfoUserName;
    private MapWrapperLayout mapWrapperLayout;
    private int tabPos;
    private RecyclerViewPager mRecyclerView;


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

        overridePendingTransition(R.anim.trans_left_in, R.anim.trans_left_out);

        setScreenTitle("Total Outlets");
        Bundle extras = getIntent().getExtras();
        tabPos = extras!=null?extras.getInt("TabPos"):0;

        initViews();
        initViewPager();

        changeTabsFont(tabLayout);
    }

    private void initViews() {

        mymarkerview = (ViewGroup)getLayoutInflater().inflate(R.layout.map_custom_outlet_info_window, null);
        tvMapInfoUserName = mymarkerview.findViewById(R.id.tv_usr_name);

        ((TextView)findViewById(R.id.tv_planned_text)).setTypeface(FontUtils.getFontRoboto(FontUtils.FontType.REGULAR,this));
        ((TextView)findViewById(R.id.tv_covered_text)).setTypeface(FontUtils.getFontRoboto(FontUtils.FontType.REGULAR,this));
        ((TextView)findViewById(R.id.tv_unbilled_text)).setTypeface(FontUtils.getFontRoboto(FontUtils.FontType.REGULAR,this));

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
            BitmapDescriptor icon1 = BitmapDescriptorFactory.fromResource(R.drawable.marker_green);
            mMap.addMarker(new MarkerOptions().flat(true).
                    title("Tamil arasu").position(destLatLng1).snippet("1695").icon(icon1));

            LatLng destLatLng2 = new LatLng(12.953195, 80.141601);
            BitmapDescriptor icon2 = BitmapDescriptorFactory.fromResource(R.drawable.marker_green);
            mMap.addMarker(new MarkerOptions().flat(true).
                    title("English ").position(destLatLng2).snippet("1698").icon(icon2));

            LatLng destLatLng3 = new LatLng(13.022480, 80.203187);
            BitmapDescriptor icon3 = BitmapDescriptorFactory.fromResource(R.drawable.marker_orange);
            mMap.addMarker(new MarkerOptions().flat(true).
                    title("Karan").position(destLatLng3).snippet("1696").icon(icon3));

            LatLng destLatLng4 = new LatLng(12.975971, 80.221209);
            BitmapDescriptor icon4 = BitmapDescriptorFactory.fromResource(R.drawable.marker_orange);
            mMap.addMarker(new MarkerOptions().flat(true).
                    title("Deepak").position(destLatLng4).snippet("1697").icon(icon4));

            LatLng destLatLng5 = new LatLng(12.965365, 80.246106);
            BitmapDescriptor icon5 = BitmapDescriptorFactory.fromResource(R.drawable.marker_red);
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
            BitmapDescriptor icon3 = BitmapDescriptorFactory.fromResource(R.drawable.marker_orange);
            mMap.addMarker(new MarkerOptions().flat(true).
                    title("Karan").position(destLatLng3).snippet("1696").icon(icon3));

            LatLng destLatLng4 = new LatLng(12.975971, 80.221209);
            BitmapDescriptor icon4 = BitmapDescriptorFactory.fromResource(R.drawable.marker_orange);
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
            BitmapDescriptor icon5 = BitmapDescriptorFactory.fromResource(R.drawable.marker_red);
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
        double x = Math.sin(-angle * Math.PI / 180) * 0.5 + getResources().getDimension(R.dimen.outlet_map_info_x);
        double y = -(Math.cos(-angle * Math.PI / 180) * 0.5 - getResources().getDimension(R.dimen.outlet_map_info_y));
        marker.setInfoWindowAnchor((float)x, (float)y);

        mMap.animateCamera(CameraUpdateFactory.newLatLng(marker.getPosition()));

        marker.showInfoWindow();

        mRecyclerView.setVisibility(View.VISIBLE);

        findViewById(R.id.cardview).setVisibility(View.GONE);

        return true;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMaxZoomPreference(24);
        mMap.getUiSettings().setMapToolbarEnabled(false);
        mMap.setOnMarkerClickListener(this);
        mMap.setOnInfoWindowClickListener(this);
        mMap.setInfoWindowAdapter(new CustomInfoWindowAdapter());

        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {

            @Override
            public void onMapClick(LatLng latLng) {
                if(mRecyclerView.getVisibility() == View.VISIBLE)
                    mRecyclerView.setVisibility(View.GONE);

                findViewById(R.id.cardview).setVisibility(View.VISIBLE);
            }
        });

        tabLayout.getTabAt(tabPos).select();

    }

    @Override
    public void onInfoWindowClick(Marker marker) {
        OutletPagerDialogFragment outletPagerDialogFragment = new OutletPagerDialogFragment();
        outletPagerDialogFragment.setStyle(DialogFragment.STYLE_NO_FRAME, 0);
        outletPagerDialogFragment.setCancelable(false);
        outletPagerDialogFragment.show(getSupportFragmentManager(),"OutletPager");
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
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_supervisor_screen, menu);

        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView = (SearchView) menu.findItem(R.id.menu_search).getActionView();
        ImageView searchClose = searchView.findViewById(android.support.v7.appcompat.R.id.search_close_btn);
        searchClose.setImageResource(android.R.drawable.ic_menu_close_clear_cancel);
        searchView.setSearchableInfo(searchManager != null ? searchManager.getSearchableInfo(getComponentName()) : null);
        SearchView.OnQueryTextListener textChangeListener = new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextChange(String newText) {
//                displaySearchItem(newText);
                return true;
            }

            @Override
            public boolean onQueryTextSubmit(String query) {
                return true;
            }
        };
        searchView.setOnQueryTextListener(textChangeListener);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        menu.findItem(R.id.menu_dashboard).setVisible(false);
        menu.findItem(R.id.menu_date).setVisible(false);
        return super.onPrepareOptionsMenu(menu);
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
                    ((TextView) tabViewChild).setTypeface(FontUtils.getFontRoboto(FontUtils.FontType.MEDIUM,this));
                }
            }
        }
    }

    private int getPixelsFromDp(Context context, float dp) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dp * scale + 0.5f);
    }

    protected void initViewPager() {
        mRecyclerView = findViewById(R.id.viewpager);
        mRecyclerView.setVisibility(View.GONE);
        LinearLayoutManager layout = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL,
                false);
        mRecyclerView.setLayoutManager(layout);

        OutletInfoHorizontalAdapter myAdapter = new OutletInfoHorizontalAdapter(this);
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
                int childCount = mRecyclerView.getChildCount();
                int width = mRecyclerView.getChildAt(0).getWidth();
                int padding = (mRecyclerView.getWidth() - width) / 2;

                for (int j = 0; j < childCount; j++) {
                    View v = recyclerView.getChildAt(j);
                    float rate = 0;
                    if (v.getLeft() <= padding) {
                        if (v.getLeft() >= padding - v.getWidth()) {
                            rate = (padding - v.getLeft()) * 1f / v.getWidth();
                        } else {
                            rate = 1;
                        }
                        v.setScaleY(1 - rate * 0.1f);
                        v.setScaleX(1 - rate * 0.1f);

                    } else {
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
}
