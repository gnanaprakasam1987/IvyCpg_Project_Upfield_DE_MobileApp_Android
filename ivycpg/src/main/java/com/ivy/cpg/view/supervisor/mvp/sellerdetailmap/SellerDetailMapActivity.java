package com.ivy.cpg.view.supervisor.mvp.sellerdetailmap;

import android.annotation.SuppressLint;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.ivy.cpg.view.profile.ProfileActivity;
import com.ivy.cpg.view.supervisor.chat.StartChatActivity;
import com.ivy.cpg.view.supervisor.mvp.FilterScreenFragment;
import com.ivy.cpg.view.supervisor.mvp.SupervisorActivityHelper;
import com.ivy.cpg.view.supervisor.mvp.models.RetailerBo;
import com.ivy.cpg.view.supervisor.mvp.sellerperformance.sellerperformancedetail.SellerPerformanceDetailActivity;
import com.ivy.lib.DialogFragment;
import com.ivy.maplib.MapWrapperLayout;
import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.commons.IvyBaseActivityNoActionBar;
import com.ivy.sd.png.util.Commons;
import com.ivy.utils.DateTimeUtils;
import com.ivy.utils.FontUtils;
import com.ivy.utils.view.OnSingleClickListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class SellerDetailMapActivity extends IvyBaseActivityNoActionBar implements SellerDetailMapContractor.SellerDetailMapView,
        OnMapReadyCallback, GoogleMap.OnMarkerClickListener, GoogleMap.OnInfoWindowClickListener, FilterScreenFragment.FilterItemSelectedListener {

    private GoogleMap mMap;
    private int userId;
    private String userName, seletedDate, sellerUId = "";
    private MapWrapperLayout mapWrapperLayout;
    private ViewGroup mymarkerview;
    private TextView tvMapInfoUserName, tvInfoVisitTime, tvSellerName, tvSellerStartTime, tvSellerLastVisit, tvTarget, tvCovered;
    private BottomSheetBehavior bottomSheetBehavior;

    private SellerDetailMapPresenter sellerMapViewPresenter;
    private OutletListAdapter outletListAdapter;
    private ArrayList<RetailerBo> outletListBos = new ArrayList<>();

    private Marker sellerMarker;
    private DrawerLayout mDrawerLayout;

    @SuppressLint("UseSparseArrays")
    HashMap<Integer, Integer> mSelectedIdByLevelId = new HashMap<>();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.seller_map_view_activity);

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

        Bundle extras = getIntent().getExtras();
        try {
            if (extras == null)
                setScreenTitle("Seller");
            else {
                userId = extras.getInt("SellerId");
                userName = extras.getString("screentitle");
                seletedDate = extras.getString("Date");
                sellerUId = extras.getString("UUID");
                setScreenTitle(userName);
            }
        } catch (Exception e) {
            setScreenTitle("Seller");
            Commons.printException(e);
        }

        sellerMapViewPresenter = new SellerDetailMapPresenter();
        sellerMapViewPresenter.setView(this, SellerDetailMapActivity.this);

        initViews();
        setViewValues();
    }

    private void initViews() {

        mymarkerview = (ViewGroup) getLayoutInflater().inflate(R.layout.map_custom_outlet_info_window, null);

        tvMapInfoUserName = mymarkerview.findViewById(R.id.tv_usr_name);
        tvInfoVisitTime = mymarkerview.findViewById(R.id.tv_visit_time);

        tvSellerName = findViewById(R.id.tv_user_name);
        tvSellerStartTime = findViewById(R.id.tv_start_time);
        tvSellerLastVisit = findViewById(R.id.tv_address);
        TextView tvSellerPerformanceBtn = findViewById(R.id.seller_performance_btn);
        tvTarget = findViewById(R.id.tv_target_outlet);
        tvCovered = findViewById(R.id.tv_outlet_covered);

        ImageView imgMessage = findViewById(R.id.message_img);

        tvSellerName.setTypeface(FontUtils.getFontRoboto(this,FontUtils.FontType.REGULAR));
        tvSellerStartTime.setTypeface(FontUtils.getFontRoboto(this,FontUtils.FontType.REGULAR));
        tvSellerLastVisit.setTypeface(FontUtils.getFontRoboto(this,FontUtils.FontType.REGULAR));
        tvSellerPerformanceBtn.setTypeface(FontUtils.getFontRoboto(this,FontUtils.FontType.REGULAR));
        tvTarget.setTypeface(FontUtils.getFontRoboto(this,FontUtils.FontType.REGULAR));
        tvCovered.setTypeface(FontUtils.getFontRoboto(this,FontUtils.FontType.REGULAR));

        tvMapInfoUserName.setTypeface(FontUtils.getFontRoboto(this,FontUtils.FontType.REGULAR));
        tvInfoVisitTime.setTypeface(FontUtils.getFontRoboto(this,FontUtils.FontType.REGULAR));

        ((TextView)findViewById(R.id.number_text)).setTypeface(FontUtils.getFontRoboto(this,FontUtils.FontType.REGULAR));
        ((TextView)findViewById(R.id.store_text)).setTypeface(FontUtils.getFontRoboto(this,FontUtils.FontType.REGULAR));
        ((TextView)findViewById(R.id.time_in_text)).setTypeface(FontUtils.getFontRoboto(this,FontUtils.FontType.REGULAR));
        ((TextView)findViewById(R.id.time_out_text)).setTypeface(FontUtils.getFontRoboto(this,FontUtils.FontType.REGULAR));

        mapWrapperLayout = findViewById(R.id.map_wrap_layout);
        mapWrapperLayout.init(mMap, getPixelsFromDp(this, getPixelsFromDp(this, 39 + 20)));

        RecyclerView recyclerView = findViewById(R.id.outlet_list);

        outletListAdapter = new OutletListAdapter(this, outletListBos);
        recyclerView.setAdapter(outletListAdapter);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());

        imgMessage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (SupervisorActivityHelper.getInstance().isChatConfigAvail(SellerDetailMapActivity.this)) {
                    if (!sellerUId.equals("")) {
                        Intent intent = new Intent(SellerDetailMapActivity.this, StartChatActivity.class);
                        intent.putExtra("name", userName);
                        intent.putExtra("UUID", sellerUId);
                        startActivity(intent);
                    } else
                        Toast.makeText(SellerDetailMapActivity.this, "No Chat Found..", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(SellerDetailMapActivity.this, "No Chat Config Enabled..", Toast.LENGTH_SHORT).show();
                }
            }
        });

        tvSellerPerformanceBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SellerDetailMapActivity.this, SellerPerformanceDetailActivity.class);
                intent.putExtra("SellerId", userId);
                intent.putExtra("Date", seletedDate);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
        });


        bottomSheetBehavior = BottomSheetBehavior.from(findViewById(R.id.user_info_layout));

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        mDrawerLayout = findViewById(R.id.drawer_layout);
        FrameLayout drawer = findViewById(R.id.right_drawer);
        int width = getResources().getDisplayMetrics().widthPixels;
        DrawerLayout.LayoutParams params = (DrawerLayout.LayoutParams) drawer.getLayoutParams();
        params.width = width;
        drawer.setLayoutParams(params);

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
                sellerMapViewPresenter.getMarkerForFocus();
            }
        });
    }

    private void setViewValues() {
        bottomSheetBehavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                switch (newState) {
                    case BottomSheetBehavior.STATE_COLLAPSED:
                        findViewById(R.id.cardview).setVisibility(View.VISIBLE);
                        break;
                    case BottomSheetBehavior.STATE_DRAGGING:
                        findViewById(R.id.cardview).setVisibility(View.GONE);
                        break;
                    case BottomSheetBehavior.STATE_EXPANDED:
                        findViewById(R.id.cardview).setVisibility(View.GONE);
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
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMaxZoomPreference(24);

        mMap.getUiSettings().setMyLocationButtonEnabled(false);

        //map style restricting landmarks
//        mMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(getContext(), R.raw.map_style));

        mMap.setOnMarkerClickListener(this);
        mMap.setInfoWindowAdapter(new CustomInfoWindowAdapter());
        mMap.setOnInfoWindowClickListener(this);
        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {

            @Override
            public void onMapClick(LatLng latLng) {

            }
        });

        //Download retailers from Master Seller wise
        //Plot pins base on retailer master location
        sellerMapViewPresenter.downloadSellerOutletAWS(userId, seletedDate);

        sellerMapViewPresenter.isRealtimeLocation();

        //Focus all the retailer location in map
        sellerMapViewPresenter.getMarkerForFocus();

        //Current Seller last visit info listener
        sellerMapViewPresenter.setSellerActivityListener(userId, seletedDate);

        //Current Seller Realtime Location listener
        sellerMapViewPresenter.setSellerMovementListener(userId, seletedDate);

        //Draw route based on sellers activity
        sellerMapViewPresenter.setSellerActivityDetailListener(userId, seletedDate);

//        sellerMapViewPresenter.downloadSellerRoute(String.valueOf(userId), seletedDate);

    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        Commons.print("on Marker Click called");

        if (marker.getTitle().equalsIgnoreCase("SELLER")) {
            marker.hideInfoWindow();
            return true;
        }

        double angle = 130.0;
        double x = Math.sin(-angle * Math.PI / 180) * 0.5 + getResources().getDimension(R.dimen.outlet_map_info_x);
        double y = -(Math.cos(-angle * Math.PI / 180) * 0.5 - getResources().getDimension(R.dimen.outlet_map_info_y));
        marker.setInfoWindowAnchor((float) x, (float) y);

        mMap.animateCamera(CameraUpdateFactory.newLatLng(marker.getPosition()));

        marker.showInfoWindow();

        return true;
    }

    @Override
    public void onInfoWindowClick(Marker marker) {

        if (sellerMapViewPresenter.getRetailerVisitDetailsByRId(Integer.valueOf(marker.getSnippet())) == null) {
            Toast.makeText(this, "No visited details found for this retailer", Toast.LENGTH_SHORT).show();
            return;
        }

        OutletPagerDialogFragment outletPagerDialogFragment = new OutletPagerDialogFragment(marker, sellerMapViewPresenter);
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
    public void focusMarker(final LatLngBounds.Builder builder) {

        mMap.setOnMapLoadedCallback(new GoogleMap.OnMapLoadedCallback() {
            @Override
            public void onMapLoaded() {
                if (builder != null) {
                    if (sellerMapViewPresenter.checkAreaBoundsTooSmall(builder.build())) {
                        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(builder.build().getCenter(), 19));
                    } else {
                        mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(builder.build(), 60));
                    }
                }
            }
        });
    }

    @Override
    public void setOutletListAdapter(ArrayList<RetailerBo> retailerMasterList, int lastVisitSeq) {
        outletListBos.clear();
        outletListBos.addAll(retailerMasterList);
        outletListAdapter.notifyDataSetChanged();
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void updateSellerInfo(String timeIn, String retailerName, String target, String covered, LatLng sellerLatLng) {
        tvSellerName.setText(userName);
        tvSellerStartTime.setText(getResources().getString(R.string.visit_time) + " " + timeIn);
        tvSellerLastVisit.setText(getResources().getString(R.string.last_vist) + " " + retailerName);
        tvTarget.setText(getResources().getString(R.string.targeted) + " : " + target);
        tvCovered.setText(getResources().getString(R.string.covered) + " : " + covered);

        if (sellerLatLng != null) {
            if (sellerMarker == null) {
                MarkerOptions markerOptions = new MarkerOptions()
                        .position(sellerLatLng)
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.marker))
                        .title("SELLER");
                sellerMarker = mMap.addMarker(markerOptions);
            } else {
//                sellerMarker.setPosition(sellerLatLng);
                sellerMapViewPresenter.animateSellerMarker(sellerLatLng, sellerMarker);
            }
        }

    }

    public void updateSellerLocation(LatLng sellerLatLng) {
        if (sellerLatLng != null) {
            if (sellerMarker == null) {
                MarkerOptions markerOptions = new MarkerOptions()
                        .position(sellerLatLng)
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.marker))
                        .title("SELLER");
                sellerMarker = mMap.addMarker(markerOptions);
            } else {
                sellerMapViewPresenter.animateSellerMarker(sellerLatLng, sellerMarker);

                sellerMapViewPresenter.addRoutePoint(sellerLatLng);
            }
        }
    }

    @Override
    public void drawRoute(ArrayList<LatLng> points) {

        int colorNormalId = R.color.map_poly_line;

//        if (lineOptions == null) {

        if (points != null) {
            PolylineOptions lineOptions = new PolylineOptions();

            lineOptions.addAll(points);
            lineOptions.width(8);
            lineOptions.color(getResources().getColor(colorNormalId));
            lineOptions.zIndex(10000000);
            lineOptions.geodesic(true);

            Polyline mapPolyLine = mMap.addPolyline(lineOptions);
            mapPolyLine.setClickable(true);
        }

//        } else {
//            mapPolyLine.setPoints(points);
//        }
    }

    @Override
    public void selectedChannels(HashMap<Integer, Integer> mSelectedIdByLevelId) {
        mDrawerLayout.closeDrawers();
        invalidateOptionsMenu();

        if (mSelectedIdByLevelId != null) {
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
                        DateTimeUtils.getTimeFromMillis(Long.valueOf(stringSplit[1])));
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
        ImageView searchClose = searchView.findViewById(R.id.search_close_btn);
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

        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        menu.findItem(R.id.menu_dashboard).setVisible(false);
        menu.findItem(R.id.menu_date).setVisible(false);

        if (mDrawerLayout.isDrawerOpen(GravityCompat.END)) {
            menu.findItem(R.id.menu_search).setVisible(false);
        } else {
            menu.findItem(R.id.menu_search).setVisible(false);
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int i = item.getItemId();
        if (i == android.R.id.home) {
            if (bottomSheetBehavior.getState() == BottomSheetBehavior.STATE_EXPANDED)
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
            else if (mDrawerLayout.isDrawerOpen(GravityCompat.END)) {
                mDrawerLayout.closeDrawers();
            } else {
                finish();
                sellerMapViewPresenter.removeFirestoreListener();
                overridePendingTransition(R.anim.trans_right_in, R.anim.trans_right_out);
            }
        }
        return super.onOptionsItemSelected(item);
    }

    private int getPixelsFromDp(Context context, float dp) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dp * scale + 0.5f);
    }

    public class OutletListAdapter extends RecyclerView.Adapter<OutletListAdapter.MyViewHolder> {

        private Context context;
        private ArrayList<RetailerBo> outletListBos;

        OutletListAdapter(Context context, ArrayList<RetailerBo> outletListBos) {
            this.context = context;
            this.outletListBos = outletListBos;
        }

        public class MyViewHolder extends RecyclerView.ViewHolder {
            private LinearLayout linearLayout;
            private TextView tvSerialNumber, tvStoreName, tvTimeIn, tvTimeOut, tvSkipped;

            public MyViewHolder(View view) {
                super(view);
                linearLayout = view.findViewById(R.id.outlet_item_layout);

                tvSerialNumber = view.findViewById(R.id.number_text);
                tvStoreName = view.findViewById(R.id.store_text);
                tvTimeIn = view.findViewById(R.id.time_in_text);
                tvTimeOut = view.findViewById(R.id.time_out_text);
                tvSkipped = view.findViewById(R.id.skipped_text);

                tvSerialNumber.setTypeface(FontUtils.getFontRoboto(context,FontUtils.FontType.REGULAR));
                tvStoreName.setTypeface(FontUtils.getFontRoboto(context,FontUtils.FontType.REGULAR));
                tvTimeIn.setTypeface(FontUtils.getFontRoboto(context,FontUtils.FontType.REGULAR));
                tvTimeOut.setTypeface(FontUtils.getFontRoboto(context,FontUtils.FontType.REGULAR));
                tvSkipped.setTypeface(FontUtils.getFontRoboto(context,FontUtils.FontType.REGULAR));

            }
        }

        @Override
        public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.outlet_list_item, parent, false);

            return new MyViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(final MyViewHolder holder, final int position) {

            if (holder.getAdapterPosition() % 2 == 0)
                holder.linearLayout.setBackgroundColor(getResources().getColor(R.color.white));
            else
                holder.linearLayout.setBackgroundColor(getResources().getColor(R.color.outlet_item_bg));

            String sequenceStr = "";

            if (outletListBos.get(holder.getAdapterPosition()).getMasterSequence() != 0)
                sequenceStr = String.valueOf(outletListBos.get(holder.getAdapterPosition()).getMasterSequence());
            else if (!outletListBos.get(holder.getAdapterPosition()).getIsDeviated())
                sequenceStr = (holder.getAdapterPosition() + 1) + "";

            holder.tvSerialNumber.setText(sequenceStr);
            holder.tvStoreName.setText(outletListBos.get(holder.getAdapterPosition()).getRetailerName());
            holder.tvTimeIn.setText(DateTimeUtils.getTimeFromMillis(outletListBos.get(holder.getAdapterPosition()).getInTime()));
            holder.tvTimeOut.setText(DateTimeUtils.getTimeFromMillis(outletListBos.get(holder.getAdapterPosition()).getOutTime()));

            if (sellerMapViewPresenter.getLastVisited() != 0 && sellerMapViewPresenter.getLastVisited() > outletListBos.get(holder.getAdapterPosition()).getMasterSequence()
                    && !outletListBos.get(holder.getAdapterPosition()).isVisited()) {
                holder.tvTimeIn.setVisibility(View.GONE);
                holder.tvTimeOut.setVisibility(View.GONE);
                holder.tvSkipped.setVisibility(View.VISIBLE);
            } else {
                holder.tvTimeIn.setVisibility(View.VISIBLE);
                holder.tvTimeOut.setVisibility(View.VISIBLE);
                holder.tvSkipped.setVisibility(View.GONE);
            }

            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    if (outletListBos.get(holder.getAdapterPosition()).getMarker() != null) {

                        double angle = 130.0;
                        double x = Math.sin(-angle * Math.PI / 180) * 0.5 + getResources().getDimension(R.dimen.outlet_map_info_x);
                        double y = -(Math.cos(-angle * Math.PI / 180) * 0.5 - getResources().getDimension(R.dimen.outlet_map_info_y));
                        outletListBos.get(holder.getAdapterPosition()).getMarker().setInfoWindowAnchor((float) x, (float) y);

                        mMap.animateCamera(CameraUpdateFactory.newLatLng(outletListBos.get(holder.getAdapterPosition()).getMarker().getPosition()));
                        outletListBos.get(holder.getAdapterPosition()).getMarker().showInfoWindow();
                    }


                    if (bottomSheetBehavior.getState() == BottomSheetBehavior.STATE_EXPANDED)
                        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);

                }
            });

            holder.tvStoreName.setOnClickListener(new OnSingleClickListener() {
                @Override
                public void onSingleClick(View v) {

                    sellerMapViewPresenter.setRetailerMaster(outletListBos.get(holder.getAdapterPosition()));

                    Intent i = new Intent(SellerDetailMapActivity.this, ProfileActivity.class);
                    i.putExtra("ViewOnly",true);
                    startActivity(i);
                }
            });

        }

        @Override
        public int getItemCount() {
            return outletListBos.size();
        }
    }

    private void filterFragment() {
        invalidateOptionsMenu();
        try {
            mDrawerLayout.openDrawer(GravityCompat.END);

            Set<Integer> integers = new HashSet<>();
            for (RetailerBo retailerBo : outletListBos) {
                integers.addAll(retailerBo.getProductIds());
            }

            String productIds = TextUtils.join(",", integers.toArray(new Integer[integers.size()]));

            FragmentManager fm = getSupportFragmentManager();
            FilterScreenFragment frag = (FilterScreenFragment) fm
                    .findFragmentByTag("FilterScreen");
            FragmentTransaction ft = fm
                    .beginTransaction();
            if (frag != null)
                ft.detach(frag);

            FilterScreenFragment fragobj = new FilterScreenFragment();
            Bundle bundle = new Bundle();
            bundle.putSerializable("ChannelId", mSelectedIdByLevelId);
            bundle.putString("Date", sellerMapViewPresenter.convertPlaneDateToGlobal(seletedDate));
            bundle.putString("ProductId", productIds);
            fragobj.setArguments(bundle);

            ft.replace(R.id.right_drawer, fragobj, "FilterScreen");
            ft.commit();
        } catch (Exception e) {
            Commons.printException(e);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        sellerMapViewPresenter.removeFirestoreListener();
    }
}