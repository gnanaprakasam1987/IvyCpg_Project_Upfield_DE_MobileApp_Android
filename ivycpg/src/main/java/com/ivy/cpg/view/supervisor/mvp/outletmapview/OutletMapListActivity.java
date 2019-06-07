package com.ivy.cpg.view.supervisor.mvp.outletmapview;

import android.annotation.SuppressLint;
import android.app.SearchManager;
import android.content.Context;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.ivy.cpg.view.supervisor.customviews.recyclerviewpager.RecyclerViewPager;
import com.ivy.cpg.view.supervisor.mvp.FilterScreenFragment;
import com.ivy.cpg.view.supervisor.mvp.models.RetailerBo;
import com.ivy.lib.DialogFragment;
import com.ivy.maplib.MapWrapperLayout;
import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.commons.IvyBaseActivityNoActionBar;
import com.ivy.sd.png.util.Commons;
import com.ivy.utils.FontUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class OutletMapListActivity extends IvyBaseActivityNoActionBar implements OnMapReadyCallback,
        GoogleMap.OnMarkerClickListener, GoogleMap.OnInfoWindowClickListener, OutletMapViewContractor.OutletMapView,FilterScreenFragment.FilterItemSelectedListener {

    private GoogleMap mMap;
    private TabLayout tabLayout;
    private ViewGroup mymarkerview;
    private TextView tvMapInfoUserName, tvInfoVisitTime;
    private MapWrapperLayout mapWrapperLayout;
    private int tabPos;
    private RecyclerViewPager outletHorizontalRecycleView;
    private OutletInfoHorizontalAdapter outletInfoHorizontalAdapter;
    private ArrayList<RetailerBo> outletListBos = new ArrayList<>();

    private OutletMapViewPresenter outletMapViewPresenter;
    private int sellerid;
    private String selectedDate;

    private DrawerLayout mDrawerLayout;

    @SuppressLint("UseSparseArrays")
    HashMap<Integer, Integer> mSelectedIdByLevelId = new HashMap<>();

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
        tabPos = extras != null ? extras.getInt("TabPos") : 0;
        sellerid = extras != null ? extras.getInt("Sellerid") : 0;
        selectedDate = extras != null ? extras.getString("Date") : "";

        outletMapViewPresenter = new OutletMapViewPresenter();
        outletMapViewPresenter.setView(this, OutletMapListActivity.this);

        initViews();
        initViewPager();

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        changeTabsFont(tabLayout);
    }

    private void initViews() {

        mymarkerview = (ViewGroup) getLayoutInflater().inflate(R.layout.map_custom_outlet_info_window, null);
        tvMapInfoUserName = mymarkerview.findViewById(R.id.tv_usr_name);
        tvInfoVisitTime = mymarkerview.findViewById(R.id.tv_visit_time);

        mDrawerLayout = findViewById(R.id.drawer_layout);
        FrameLayout drawer = findViewById(R.id.right_drawer);
        int width = getResources().getDisplayMetrics().widthPixels;
        DrawerLayout.LayoutParams params = (android.support.v4.widget.DrawerLayout.LayoutParams) drawer.getLayoutParams();
        params.width = width;
        drawer.setLayoutParams(params);

        tvMapInfoUserName.setTypeface(FontUtils.getFontRoboto(this, FontUtils.FontType.REGULAR));
        tvInfoVisitTime.setTypeface(FontUtils.getFontRoboto(this, FontUtils.FontType.REGULAR));

        ((TextView) findViewById(R.id.tv_planned_text)).setTypeface(FontUtils.getFontRoboto(this, FontUtils.FontType.REGULAR));
        ((TextView) findViewById(R.id.tv_covered_text)).setTypeface(FontUtils.getFontRoboto(this, FontUtils.FontType.REGULAR));
        ((TextView) findViewById(R.id.tv_unbilled_text)).setTypeface(FontUtils.getFontRoboto(this, FontUtils.FontType.REGULAR));

        mapWrapperLayout = findViewById(R.id.map_wrap_layout);
        mapWrapperLayout.init(mMap, getPixelsFromDp(this, getPixelsFromDp(this, 39 + 20)));

        tabLayout = findViewById(R.id.tab_layout);

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {

                setScreenTitle();

                outletMapViewPresenter.setTabPosition(tab.getPosition());

                outletHorizontalRecycleView.setVisibility(View.GONE);

                findViewById(R.id.cardview).setVisibility(View.VISIBLE);
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }
        });

        mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow,
                GravityCompat.START);
        mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow,
                GravityCompat.END);

        mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);

        ActionBarDrawerToggle mDrawerToggle = new ActionBarDrawerToggle(this, /* host Activity */
                mDrawerLayout, /* DrawerLayout object */
                R.string.ok, /* "open drawer" description for accessibility */
                R.string.close /* "close drawer" description for accessibility */
        ) {
            public void onDrawerClosed(View view) {
                supportInvalidateOptionsMenu();
            }

            public void onDrawerOpened(View drawerView) {
                setScreenTitle(getResources().getString(R.string.filter_by));
                supportInvalidateOptionsMenu();
            }
        };
        mDrawerLayout.setDrawerListener(mDrawerToggle);

        findViewById(R.id.filter_layout).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                filterFragment();
            }
        });

        findViewById(R.id.recenter_location).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                outletMapViewPresenter.getMarkerForFocus();
            }
        });

    }

    @Override
    public void clearMap() {
        if (mMap != null)
            mMap.clear();
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
                if (outletHorizontalRecycleView.getVisibility() == View.VISIBLE)
                    outletHorizontalRecycleView.setVisibility(View.GONE);

                findViewById(R.id.cardview).setVisibility(View.VISIBLE);
            }
        });

        tabLayout.getTabAt(tabPos).select();

        outletMapViewPresenter.downloadOutletListAws();

        outletMapViewPresenter.setTabPosition(tabPos);

        outletMapViewPresenter.setOutletActivityDetail(sellerid, selectedDate);

    }

    @Override
    public boolean onMarkerClick(Marker marker) {

        Commons.print("on Marker Click called");

        if (marker.getTitle().equalsIgnoreCase("SELLER")) {
            marker.hideInfoWindow();
            return true;
        }

        outletHorizontalRecycleView.setVisibility(View.VISIBLE);

        findViewById(R.id.cardview).setVisibility(View.GONE);

        int pagerPos = 0;
        int count = 0;
        for (RetailerBo detailsBo : outletListBos) {
            if (detailsBo.getMarker() != null
                    && marker.getSnippet() != null
                    && detailsBo.getMarker().getSnippet().equalsIgnoreCase(marker.getSnippet())) {
                pagerPos = count;
                break;
            }
            count = count + 1;
        }

        outletHorizontalRecycleView.scrollToPosition(pagerPos);

        return true;
    }

    @Override
    public void focusMarker(final LatLngBounds.Builder builder) {

        mMap.setOnMapLoadedCallback(new GoogleMap.OnMapLoadedCallback() {
            @Override
            public void onMapLoaded() {
                if (builder != null) {
                    if (outletMapViewPresenter.checkAreaBoundsTooSmall(builder.build(), 300)) {
                        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(builder.build().getCenter(), 19));
                    } else {
                        mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(builder.build(), 60));
                    }
                }
            }
        });
    }

    @Override
    public void onInfoWindowClick(Marker marker) {

        if (outletMapViewPresenter.getRetailerVisitDetailsByRId(Integer.valueOf(marker.getSnippet())) == null) {
            Toast.makeText(this, "No visited details found for this retailer", Toast.LENGTH_SHORT).show();
            return;
        }

        OutletPagerDialogFragment outletPagerDialogFragment = new OutletPagerDialogFragment(Integer.parseInt(marker.getSnippet()), outletMapViewPresenter);
        outletPagerDialogFragment.setStyle(DialogFragment.STYLE_NO_FRAME, 0);
        outletPagerDialogFragment.setCancelable(false);
        outletPagerDialogFragment.show(getSupportFragmentManager(), "OutletPager");
    }

    @Override
    public void setRetailerMarker(RetailerBo retailerBo, MarkerOptions markerOptions) {
        Marker marker = mMap.addMarker(markerOptions);
        retailerBo.setMarker(marker);
    }

    @Override
    public void setOutletListAdapter(ArrayList<RetailerBo> retailerMasterList) {
        outletListBos.clear();
        outletListBos.addAll(retailerMasterList);
        outletInfoHorizontalAdapter.notifyDataSetChanged();

        setScreenTitle();

    }

    @Override
    public void selectedChannels(HashMap<Integer, Integer> mSelectedIdByLevelId) {
        mDrawerLayout.closeDrawers();
        invalidateOptionsMenu();

        if(mSelectedIdByLevelId!=null) {

            outletMapViewPresenter.channelFilterIds(mSelectedIdByLevelId);
            outletMapViewPresenter.setTabPosition(tabLayout.getSelectedTabPosition());

            this.mSelectedIdByLevelId = mSelectedIdByLevelId;
        }
    }

    class CustomInfoWindowAdapter implements GoogleMap.InfoWindowAdapter {

        @Override
        public View getInfoContents(Marker marker) {
            return null;
        }

        @SuppressLint("SetTextI18n")
        @Override
        public View getInfoWindow(final Marker marker) {

            String[] stringSplit = marker.getTitle().split("//");

            if (stringSplit.length > 1) {
                tvMapInfoUserName.setText(stringSplit[0]);
                tvInfoVisitTime.setText(getResources().getString(R.string.visit_time) + " " +
                        outletMapViewPresenter.convertMillisToTime(Long.valueOf(stringSplit[1])));
            } else {
                tvMapInfoUserName.setText(stringSplit[0]);
                tvInfoVisitTime.setText(getResources().getString(R.string.visit_time));
            }

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

    private void displaySearchItem(String searchText){
        ArrayList<RetailerBo> detailsBos = new ArrayList<>(outletMapViewPresenter.getRetailerList());

        outletListBos.clear();

        for(int i = 0;i<detailsBos.size();i++){
            if (detailsBos.get(i).getRetailerName().toLowerCase()
                    .contains(searchText.toLowerCase()) ){
                detailsBos.add(detailsBos.get(i));
            }
        }

        outletListBos.addAll(detailsBos);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        menu.findItem(R.id.menu_dashboard).setVisible(false);
        menu.findItem(R.id.menu_date).setVisible(false);

        if (mDrawerLayout.isDrawerOpen(GravityCompat.END)) {
            menu.findItem(R.id.menu_search).setVisible(false);
        }
        else {
            menu.findItem(R.id.menu_search).setVisible(true);
        }

        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int i = item.getItemId();
        if (i == android.R.id.home) {
            if (mDrawerLayout.isDrawerOpen(GravityCompat.END)) {
                mDrawerLayout.closeDrawers();
                setScreenTitle();
            } else {
                finish();
                overridePendingTransition(R.anim.trans_right_in, R.anim.trans_right_out);
            }
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
                    ((TextView) tabViewChild).setTypeface(FontUtils.getFontRoboto(this, FontUtils.FontType.MEDIUM));
                }
            }
        }
    }

    private int getPixelsFromDp(Context context, float dp) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dp * scale + 0.5f);
    }

    protected void initViewPager() {
        outletHorizontalRecycleView = findViewById(R.id.viewpager);
        outletHorizontalRecycleView.setVisibility(View.GONE);
        LinearLayoutManager layout = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL,
                false);
        outletHorizontalRecycleView.setLayoutManager(layout);

        outletInfoHorizontalAdapter = new OutletInfoHorizontalAdapter(OutletMapListActivity.this, outletListBos, outletMapViewPresenter);
        outletHorizontalRecycleView.setAdapter(outletInfoHorizontalAdapter);

        outletHorizontalRecycleView.setHasFixedSize(true);
        outletHorizontalRecycleView.setLongClickable(true);
        outletHorizontalRecycleView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int scrollState) {
//                updateState(scrollState);
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int i, int i2) {
                int childCount = outletHorizontalRecycleView.getChildCount();
                int width = outletHorizontalRecycleView.getChildAt(0).getWidth();
                int padding = (outletHorizontalRecycleView.getWidth() - width) / 2;

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

        outletHorizontalRecycleView.addOnPageChangedListener(new RecyclerViewPager.OnPageChangedListener() {
            @Override
            public void OnPageChanged(int oldPosition, int newPosition) {

                if (outletListBos.get(newPosition).getMarker() != null) {
                    double angle = 130.0;
                    double x = Math.sin(-angle * Math.PI / 180) * 0.5 + getResources().getDimension(R.dimen.outlet_map_info_x);
                    double y = -(Math.cos(-angle * Math.PI / 180) * 0.5 - getResources().getDimension(R.dimen.outlet_map_info_y));
                    outletListBos.get(newPosition).getMarker().setInfoWindowAnchor((float) x, (float) y);

                    mMap.animateCamera(CameraUpdateFactory.newLatLng(outletListBos.get(newPosition).getMarker().getPosition()));
                    outletListBos.get(newPosition).getMarker().showInfoWindow();
                }

            }
        });

        outletHorizontalRecycleView.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                if (outletHorizontalRecycleView.getChildCount() < 3) {
                    if (outletHorizontalRecycleView.getChildAt(1) != null) {
                        if (outletHorizontalRecycleView.getCurrentPosition() == 0) {
                            View v1 = outletHorizontalRecycleView.getChildAt(1);
                            v1.setScaleY(0.9f);
                            v1.setScaleX(0.9f);
                        } else {
                            View v1 = outletHorizontalRecycleView.getChildAt(0);
                            v1.setScaleY(0.9f);
                            v1.setScaleX(0.9f);
                        }
                    }
                } else {
                    if (outletHorizontalRecycleView.getChildAt(0) != null) {
                        View v0 = outletHorizontalRecycleView.getChildAt(0);
                        v0.setScaleY(0.9f);
                        v0.setScaleX(0.9f);
                    }
                    if (outletHorizontalRecycleView.getChildAt(2) != null) {
                        View v2 = outletHorizontalRecycleView.getChildAt(2);
                        v2.setScaleY(0.9f);
                        v2.setScaleX(0.9f);
                    }
                }

            }
        });
    }

    private void filterFragment() {
        invalidateOptionsMenu();
        try {
            mDrawerLayout.openDrawer(GravityCompat.END);

            Set<Integer> integers = new HashSet<>();
            for(RetailerBo retailerBo : outletListBos){
                integers.addAll(retailerBo.getProductIds());
            }

            String productIds = TextUtils.join(",", integers.toArray(new Integer[integers.size()])) ;

            android.support.v4.app.FragmentManager fm = getSupportFragmentManager();
            FilterScreenFragment frag = (FilterScreenFragment) fm
                    .findFragmentByTag("FilterScreen");
            android.support.v4.app.FragmentTransaction ft = fm
                    .beginTransaction();
            if (frag != null)
                ft.detach(frag);

            FilterScreenFragment fragobj = new FilterScreenFragment();
            Bundle bundle =new Bundle();
            bundle.putSerializable("ChannelId",mSelectedIdByLevelId);
            bundle.putString("Date",outletMapViewPresenter.convertPlaneDateToGlobal(selectedDate));
            bundle.putString("ProductId",productIds);
            fragobj.setArguments(bundle);

            ft.replace(R.id.right_drawer, fragobj, "FilterScreen");
            ft.commit();
        } catch (Exception e) {
            Commons.printException(e);
        }
    }

    private void setScreenTitle(){
        switch (tabLayout.getSelectedTabPosition()) {
            case 0:
                setScreenTitle("Total Outlet (" + outletListBos.size() + ")");
                break;
            case 1:
                setScreenTitle("Covered (" + outletListBos.size() + ")");
                break;
            case 2:
                setScreenTitle("UnBilled (" + outletListBos.size() + ")");
                break;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        outletMapViewPresenter.removeFirestoreListener();
    }
}
