package com.ivy.cpg.view.supervisor.mvp.sellerdetailmap;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetBehavior;
import android.support.v4.app.ActivityCompat;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
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
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.ivy.cpg.view.supervisor.mvp.RetailerBo;
import com.ivy.cpg.view.supervisor.mvp.sellerperformance.SellerPerformanceListActivity;
import com.ivy.lib.DialogFragment;
import com.ivy.maplib.MapWrapperLayout;
import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.commons.IvyBaseActivityNoActionBar;
import com.ivy.sd.png.util.Commons;
import com.ivy.utils.FontUtils;

import java.util.ArrayList;

public class SellerDetailMapActivity extends IvyBaseActivityNoActionBar implements SellerDetailMapContractor.SellerDetailMapView,
        OnMapReadyCallback,GoogleMap.OnMarkerClickListener,GoogleMap.OnInfoWindowClickListener  {

    private GoogleMap mMap;
    private int userId;
    private String userName;
    private MapWrapperLayout mapWrapperLayout;
    private ViewGroup mymarkerview;
    private TextView tvMapInfoUserName, tvInfoVisitTime, tvSellerName, tvSellerStartTime, tvSellerLastVisit, tvTarget, tvCovered;
    private BottomSheetBehavior bottomSheetBehavior;

    private SellerDetailMapPresenter sellerMapViewPresenter;
    private OutletListAdapter outletListAdapter;
    private ArrayList<RetailerBo> outletListBos = new ArrayList<>();

    private Polyline mapPolyLine = null;
    private PolylineOptions lineOptions = null;
    private Marker sellerMarker;


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
                setScreenTitle(userName);
            }
        } catch (Exception e) {
            setScreenTitle("Seller");
            Commons.printException(e);
        }

        sellerMapViewPresenter = new SellerDetailMapPresenter();
        sellerMapViewPresenter.setView(this,SellerDetailMapActivity.this);

        initViews();
        setViewValues();
    }

    private void initViews() {

        mymarkerview = (ViewGroup)getLayoutInflater().inflate(R.layout.map_custom_outlet_info_window, null);

        tvMapInfoUserName = mymarkerview.findViewById(R.id.tv_usr_name);
        tvInfoVisitTime = mymarkerview.findViewById(R.id.tv_visit_time);

        tvSellerName = findViewById(R.id.tv_user_name);
        tvSellerStartTime = findViewById(R.id.tv_start_time);
        tvSellerLastVisit = findViewById(R.id.tv_address);
        TextView tvSellerPerformanceBtn = findViewById(R.id.seller_performance_btn);
        tvTarget = findViewById(R.id.tv_target_outlet);
        tvCovered = findViewById(R.id.tv_outlet_covered);

        ImageView imgMessage = findViewById(R.id.message_img);

        tvSellerName.setTypeface(FontUtils.getFontRoboto(FontUtils.FontType.REGULAR,this));
        tvSellerStartTime.setTypeface(FontUtils.getFontRoboto(FontUtils.FontType.REGULAR,this));
        tvSellerLastVisit.setTypeface(FontUtils.getFontRoboto(FontUtils.FontType.REGULAR,this));
        tvSellerPerformanceBtn.setTypeface(FontUtils.getFontRoboto(FontUtils.FontType.REGULAR,this));
        tvTarget.setTypeface(FontUtils.getFontRoboto(FontUtils.FontType.REGULAR,this));
        tvCovered.setTypeface(FontUtils.getFontRoboto(FontUtils.FontType.REGULAR,this));

        ((TextView)findViewById(R.id.number_text)).setTypeface(FontUtils.getFontRoboto(FontUtils.FontType.REGULAR,this));
        ((TextView)findViewById(R.id.store_text)).setTypeface(FontUtils.getFontRoboto(FontUtils.FontType.REGULAR,this));
        ((TextView)findViewById(R.id.time_in_text)).setTypeface(FontUtils.getFontRoboto(FontUtils.FontType.REGULAR,this));
        ((TextView)findViewById(R.id.time_out_text)).setTypeface(FontUtils.getFontRoboto(FontUtils.FontType.REGULAR,this));

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

            }
        });

        tvSellerPerformanceBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SellerDetailMapActivity.this,SellerPerformanceListActivity.class);
                startActivity(intent);
            }
        });

        bottomSheetBehavior = BottomSheetBehavior.from(findViewById(R.id.user_info_layout));

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
    }

    private void setViewValues(){
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
        sellerMapViewPresenter.downloadSellerOutletAWS(userId);

        sellerMapViewPresenter.isRealtimeLocation();

        //Focus all the retailer location in map
        sellerMapViewPresenter.getMarkerForFocus();

        //Sellers last visit info listener
        sellerMapViewPresenter.setSellerActivityListener(userId);

        //Draw route based on sellers activity
        sellerMapViewPresenter.setSellerActivityDetailListener(userId);

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
        marker.setInfoWindowAnchor((float)x, (float)y);

        mMap.animateCamera(CameraUpdateFactory.newLatLng(marker.getPosition()));

        marker.showInfoWindow();

        return true;
    }

    @Override
    public void onInfoWindowClick(Marker marker) {

        if(sellerMapViewPresenter.getRetailerVisitDetailsByRId(Integer.valueOf(marker.getSnippet())) == null){
            Toast.makeText(this, "No visited details found for this retailer", Toast.LENGTH_SHORT).show();
            return;
        }

        OutletPagerDialogFragment outletPagerDialogFragment = new OutletPagerDialogFragment(marker,sellerMapViewPresenter);
        outletPagerDialogFragment.setStyle(DialogFragment.STYLE_NO_FRAME, 0);
        outletPagerDialogFragment.setCancelable(false);
        outletPagerDialogFragment.show(getSupportFragmentManager(),"OutletPager");

    }

    @Override
    public void setRetailerMarker(RetailerBo retailerBo,MarkerOptions markerOptions) {

        Marker marker = mMap.addMarker(markerOptions);
        retailerBo.setMarker(marker);
    }

    @Override
    public void focusMarker(final LatLngBounds.Builder builder) {

        mMap.setOnMapLoadedCallback(new GoogleMap.OnMapLoadedCallback() {
            @Override
            public void onMapLoaded() {
                mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(builder.build(), 200));
            }
        });
    }

    @Override
    public void setOutletListAdapter(ArrayList<RetailerBo> retailerMasterList) {
        outletListBos.clear();
        outletListBos.addAll(retailerMasterList);
        outletListAdapter.notifyDataSetChanged();
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void updateSellerInfo(String timeIn,String retailerName,String target,String covered,LatLng sellerLatLng ) {
        tvSellerName.setText(userName);
        tvSellerStartTime.setText(getResources().getString(R.string.visit_time)+" "+timeIn);
        tvSellerLastVisit.setText(getResources().getString(R.string.last_vist)+" "+retailerName);
        tvTarget.setText(getResources().getString(R.string.targeted)+" "+target);
        tvCovered.setText(getResources().getString(R.string.covered)+" "+covered);

        if(sellerLatLng != null) {
            if (sellerMarker == null) {
                MarkerOptions markerOptions = new MarkerOptions()
                        .position(sellerLatLng)
                        .icon(BitmapDescriptorFactory.fromResource(R.drawable.marker))
                        .title("SELLER");
                sellerMarker = mMap.addMarker(markerOptions);
            } else
                sellerMarker.setPosition(sellerLatLng);
        }

    }

    @Override
    public void drawRoute(ArrayList<LatLng> points) {

        int colorNormalId = R.color.map_poly_line;

        if (lineOptions == null) {

            lineOptions = new PolylineOptions();

            lineOptions.addAll(points);
            lineOptions.width(8);
            lineOptions.color(getResources().getColor(colorNormalId));
            lineOptions.zIndex(10000000);
            lineOptions.geodesic(true);

            mapPolyLine = mMap.addPolyline(lineOptions);
            mapPolyLine.setClickable(true);

        } else {
            mapPolyLine.setPoints(points);
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

            if(stringSplit.length > 1){
                tvMapInfoUserName.setText(stringSplit[0]);
                tvInfoVisitTime.setText(getResources().getString(R.string.visit_time)+" "+
                        sellerMapViewPresenter.convertMillisToTime(Long.valueOf(stringSplit[1])));
            }else {
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
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        menu.findItem(R.id.menu_dashboard).setVisible(false);
        menu.findItem(R.id.menu_date).setVisible(false);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int i = item.getItemId();
        if (i == android.R.id.home) {
            if(bottomSheetBehavior.getState() == BottomSheetBehavior.STATE_EXPANDED)
                    bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
            else{
                finish();
                sellerMapViewPresenter.removeFirestoreListener();
                overridePendingTransition(R.anim.trans_right_in, R.anim.trans_right_out);
            }

//        }else if(i == R.userId.menu_route){
//            drawRoute();
//        }else if(i == R.userId.menu_navigate){
//            moveMarkerInPath();
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
        OutletListAdapter(Context context,ArrayList<RetailerBo> outletListBos){
            this.context = context;
            this.outletListBos = outletListBos;
        }

        public class MyViewHolder extends RecyclerView.ViewHolder {
            private LinearLayout linearLayout;
            private TextView tvSerialNumber,tvStoreName,tvTimeIn,tvTimeOut,tvSkipped;

            public MyViewHolder(View view) {
                super(view);
                linearLayout = view.findViewById(R.id.outlet_item_layout);

                tvSerialNumber = view.findViewById(R.id.number_text);
                tvStoreName = view.findViewById(R.id.store_text);
                tvTimeIn = view.findViewById(R.id.time_in_text);
                tvTimeOut = view.findViewById(R.id.time_out_text);
                tvSkipped = view.findViewById(R.id.skipped_text);

                tvSerialNumber.setTypeface(FontUtils.getFontRoboto(FontUtils.FontType.REGULAR,context));
                tvStoreName.setTypeface(FontUtils.getFontRoboto(FontUtils.FontType.REGULAR,context));
                tvTimeIn.setTypeface(FontUtils.getFontRoboto(FontUtils.FontType.REGULAR,context));
                tvTimeOut.setTypeface(FontUtils.getFontRoboto(FontUtils.FontType.REGULAR,context));
                tvSkipped.setTypeface(FontUtils.getFontRoboto(FontUtils.FontType.REGULAR,context));

            }
        }

        @Override
        public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.outlet_list_item, parent, false);

            return new MyViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(MyViewHolder holder, final int position) {

            if(position%2 == 0)
                holder.linearLayout.setBackgroundColor(getResources().getColor(R.color.white));
            else
                holder.linearLayout.setBackgroundColor(getResources().getColor(R.color.outlet_item_bg));

            holder.tvSerialNumber.setText(String.valueOf(outletListBos.get(position).getMasterSequence()));
            holder.tvStoreName.setText(outletListBos.get(position).getRetailerName());
            holder.tvTimeIn.setText(sellerMapViewPresenter.convertMillisToTime(outletListBos.get(position).getTimeIn()));
            holder.tvTimeOut.setText(sellerMapViewPresenter.convertMillisToTime(outletListBos.get(position).getTimeOut()));

            if(sellerMapViewPresenter.convertMillisToTime(outletListBos.get(position).getTimeIn()).isEmpty()){
                holder.tvTimeIn.setVisibility(View.GONE);
                holder.tvTimeOut.setVisibility(View.GONE);
                holder.tvSkipped.setVisibility(View.VISIBLE);
            }else {
                holder.tvTimeIn.setVisibility(View.VISIBLE);
                holder.tvTimeOut.setVisibility(View.VISIBLE);
                holder.tvSkipped.setVisibility(View.GONE);
            }

        }

        @Override
        public int getItemCount() {
            return outletListBos.size();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        sellerMapViewPresenter.removeFirestoreListener();
    }
}