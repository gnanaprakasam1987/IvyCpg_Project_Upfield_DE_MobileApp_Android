package com.ivy.cpg.view.supervisor.mvp.supervisorhomepage;


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
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
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
import com.ivy.cpg.view.supervisor.customviews.recyclerviewpager.RecyclerViewPager;
import com.ivy.cpg.view.supervisor.helper.SupervisorActivityHelper;
import com.ivy.cpg.view.supervisor.mvp.SupervisorModelBo;
import com.ivy.cpg.view.supervisor.mvp.outletmapview.OutletMapListActivity;
import com.ivy.cpg.view.supervisor.mvp.sellermapview.SellerListActivity;
import com.ivy.cpg.view.supervisor.mvp.sellerperformance.SellerPerformanceListActivity;
import com.ivy.maplib.MapWrapperLayout;
import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.commons.IvyBaseFragment;
import com.ivy.sd.png.util.Commons;
import com.ivy.sd.png.view.HomeScreenActivity;
import com.ivy.utils.FontUtils;

import java.util.ArrayList;

public class SupervisorHomeFragment extends IvyBaseFragment implements
        OnMapReadyCallback, View.OnClickListener, GoogleMap.OnMarkerClickListener,
        GoogleMap.OnInfoWindowClickListener,SupervisorHomeContract.SupervisorHomeView{

    private GoogleMap mMap;
    private BottomSheetBehavior bottomSheetBehavior;
    private TextView totalSeller, absentSeller, marketSeller
            , tvCoveredOutlet, tvUnbilledOutlet, tvTotalOutlet, tvOrderValue,tvSellerProductivePercent;
    private MapWrapperLayout mapWrapperLayout;
    private ViewGroup mymarkerview;
    private TextView tvMapInfoUserName;
    private RecyclerViewPager mRecyclerView;
    private SupervisorHomePresenter supervisorHomePresenter;
    private SellerInfoHorizontalAdapter sellerInfoHorizontalAdapter;

    private ArrayList<SupervisorModelBo> sellerArrayList = new ArrayList<>();


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.activity_supervisor_home, container, false);

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
                setScreenTitle(extras.getString("screentitle"));
            }
        } catch (Exception e) {
            setScreenTitle("MENU_SUPERVISOR");
            Commons.printException(e);
        }

        initViews(view);
        initViewPager(view);

        supervisorHomePresenter = new SupervisorHomePresenter();
        supervisorHomePresenter.setView(this,getContext());

//        supervisorHomePresenter.getSellerListAWS();

        return view;
    }

    private void initViews(final View view) {

        this.mymarkerview = (ViewGroup) getLayoutInflater().inflate(R.layout.map_custom_info_window, null);

        totalSeller = view.findViewById(R.id.tv_ttl_seller);
        absentSeller = view.findViewById(R.id.tv_ttl_absent_seller);
        marketSeller = view.findViewById(R.id.tv_ttl_market_seller);
        tvCoveredOutlet = view.findViewById(R.id.tv_covered_outlet);
        tvUnbilledOutlet = view.findViewById(R.id.tv_unbilled_outlet);
        tvTotalOutlet = view.findViewById(R.id.tv_ttl_outlet);
        tvOrderValue = view.findViewById(R.id.tv_order_value);
        tvSellerProductivePercent = view.findViewById(R.id.seller_perform_percent);

        tvMapInfoUserName = mymarkerview.findViewById(R.id.tv_usr_name);

        view.findViewById(R.id.ttl_seller_layout).setOnClickListener(this);
        view.findViewById(R.id.ttl_outlet_layout).setOnClickListener(this);
        view.findViewById(R.id.covered_outlet_layout).setOnClickListener(this);
        view.findViewById(R.id.unbilled_layout).setOnClickListener(this);
        view.findViewById(R.id.seller_view_btn).setOnClickListener(this);

        //Bottom sheet layout Typeface
        ((TextView) view.findViewById(R.id.tv_txt_ttl_seller)).setTypeface(FontUtils.getFontRoboto(FontUtils.FontType.REGULAR,getContext()));
        ((TextView) view.findViewById(R.id.tv_txt_ttl_outlet)).setTypeface(FontUtils.getFontRoboto(FontUtils.FontType.REGULAR,getContext()));
        ((TextView) view.findViewById(R.id.tv_txt_covered_outlet)).setTypeface(FontUtils.getFontRoboto(FontUtils.FontType.REGULAR,getContext()));
        ((TextView) view.findViewById(R.id.tv_txt_unbilled_outlet)).setTypeface(FontUtils.getFontRoboto(FontUtils.FontType.REGULAR,getContext()));
        ((TextView) view.findViewById(R.id.tv_txt_order_value)).setTypeface(FontUtils.getFontRoboto(FontUtils.FontType.REGULAR,getContext()));
        ((TextView) view.findViewById(R.id.tv_txt_ttl_market_seller)).setTypeface(FontUtils.getFontRoboto(FontUtils.FontType.LIGHT,getContext()));
        ((TextView) view.findViewById(R.id.tv_txt_ttl_absent_seller)).setTypeface(FontUtils.getFontRoboto(FontUtils.FontType.LIGHT,getContext()));
        ((TextView) view.findViewById(R.id.tv_inmarket_seller)).setTypeface(FontUtils.getFontRoboto(FontUtils.FontType.LIGHT,getContext()));

        totalSeller.setTypeface(FontUtils.getFontRoboto(FontUtils.FontType.MEDIUM,getContext()));
        absentSeller.setTypeface(FontUtils.getFontRoboto(FontUtils.FontType.MEDIUM,getContext()));
        marketSeller.setTypeface(FontUtils.getFontRoboto(FontUtils.FontType.MEDIUM,getContext()));
        tvCoveredOutlet.setTypeface(FontUtils.getFontRoboto(FontUtils.FontType.MEDIUM,getContext()));
        tvUnbilledOutlet.setTypeface(FontUtils.getFontRoboto(FontUtils.FontType.MEDIUM,getContext()));
        tvTotalOutlet.setTypeface(FontUtils.getFontRoboto(FontUtils.FontType.MEDIUM,getContext()));
        tvOrderValue.setTypeface(FontUtils.getFontRoboto(FontUtils.FontType.MEDIUM,getContext()));
        tvSellerProductivePercent.setTypeface(FontUtils.getFontRoboto(FontUtils.FontType.MEDIUM,getContext()));


        mapWrapperLayout = view.findViewById(R.id.map_wrap_layout);
        mapWrapperLayout.init(mMap, getPixelsFromDp(getContext(), 39 + 20));

        bottomSheetBehavior = BottomSheetBehavior.from(view.findViewById(R.id.bottomSheetLayout));

        bottomSheetBehavior.setHideable(false);
        bottomSheetBehavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                switch (newState) {
                    case BottomSheetBehavior.STATE_COLLAPSED:
                        if(mRecyclerView.getVisibility() == View.VISIBLE) {
                            bottomSheetBehavior.setHideable(true);
                            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
                        }
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

        view.findViewById(R.id.seller_view_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getContext(), SellerPerformanceListActivity.class);
                intent.putExtra("Screen", "Seller Performance");
                startActivity(intent);
            }
        });

        SupportMapFragment mapFragment = (SupportMapFragment) this.getChildFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

    }

    private void initViewPager(final View view) {
        mRecyclerView = view.findViewById(R.id.viewpager);
        mRecyclerView.setVisibility(View.GONE);
        LinearLayoutManager layout = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL,
                false);
        mRecyclerView.setLayoutManager(layout);

        sellerInfoHorizontalAdapter = new SellerInfoHorizontalAdapter(getContext().getApplicationContext(),sellerArrayList);
        mRecyclerView.setAdapter(sellerInfoHorizontalAdapter);

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

                double angle = 130.0;
                double x = Math.sin(-angle * Math.PI / 180) * 0.5 + getResources().getDimension(R.dimen._3_4sdp);
                double y = -(Math.cos(-angle * Math.PI / 180) * 0.5 - getResources().getDimension(R.dimen._0_7sdp));
                sellerArrayList.get(newPosition).getMarker().setInfoWindowAnchor((float)x, (float)y);

                mMap.animateCamera(CameraUpdateFactory.newLatLng(sellerArrayList.get(newPosition).getMarker().getPosition()));
                sellerArrayList.get(newPosition).getMarker().showInfoWindow();


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

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_supervisor_screen, menu);

        SearchManager searchManager = (SearchManager) getContext().getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView = (SearchView) menu.findItem(R.id.menu_search).getActionView();
        ImageView searchClose = searchView.findViewById(android.support.v7.appcompat.R.id.search_close_btn);
        searchClose.setImageResource(android.R.drawable.ic_menu_close_clear_cancel);
        searchView.setSearchableInfo(searchManager != null ? searchManager.getSearchableInfo(getActivity().getComponentName()) : null);
        SearchView.OnQueryTextListener textChangeListener = new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextChange(String newText) {
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

    @Override
    public void onClick(View v) {

    }

    @Override
    public void onInfoWindowClick(Marker marker) {

    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        if(bottomSheetBehavior.getState() == BottomSheetBehavior.STATE_EXPANDED || bottomSheetBehavior.getState() == BottomSheetBehavior.STATE_COLLAPSED) {
            bottomSheetBehavior.setHideable(true);
            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
        }

        supervisorHomePresenter.getSellerMarkerInfo(marker.getSnippet());

        mRecyclerView.setVisibility(View.VISIBLE);

        int pagerPos = 0;
        int count=0;
        for(SupervisorModelBo detailsBo : sellerArrayList){
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
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMaxZoomPreference(24);
        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(getContext().getApplicationContext(), "Enable location permission from App Settings", Toast.LENGTH_SHORT).show();

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

        supervisorHomePresenter.getSellerListAWS();
        supervisorHomePresenter.getSellerWiseRetailerAWS();
        supervisorHomePresenter.isRealtimeLocation();

        supervisorHomePresenter.loginToFirebase(getContext().getApplicationContext());
    }

    @Override
    public void firebaseLoginFailure() {
        Toast.makeText(getContext().getApplicationContext(), "Firebase Login Failed", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void updateSellerCount() {

    }

    @Override
    public void updateSellerMarkerInfo(SupervisorModelBo supervisorModelBo) {

    }

    @Override
    public void updateSellerFirebaseInfo(SupervisorModelBo supervisorModelBo) {

    }

    @Override
    public void createMarker(SupervisorModelBo supervisorModelBo) {

        BitmapDescriptor icon = BitmapDescriptorFactory.fromResource(R.drawable.marker);
//        BitmapDescriptor icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE);
        Marker marker = mMap.addMarker(supervisorModelBo.getMarkerOptions());
        marker.setIcon(icon);
        supervisorModelBo.setMarker(marker);

    }

    @Override
    public void updateMaker(SupervisorModelBo supervisorModelBo) {

        LatLng destLatLng = new LatLng(supervisorModelBo.getLatitude(), supervisorModelBo.getLongitude());

        SupervisorActivityHelper.getInstance().animateMarkerNew(destLatLng,supervisorModelBo.getMarker(),mMap);
    }

    @Override
    public void focusMarker(LatLngBounds.Builder builder) {

        mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(builder.build(), 200));
    }

    @Override
    public void setSellerListAdapter(ArrayList<SupervisorModelBo> modelBoArrayList) {
        sellerArrayList.clear();
        sellerArrayList.addAll(modelBoArrayList);
        sellerInfoHorizontalAdapter.notifyDataSetChanged();
    }

    @Override
    public void displayTotalSellerCount(int totalSellerCount) {
        totalSeller.setText(String.valueOf(totalSellerCount));
    }

    @Override
    public void updateSellerAttendance(int absentSellerCount, int marketSellerCount) {
        absentSeller.setText(String.valueOf(absentSellerCount));
        marketSeller.setText(String.valueOf(marketSellerCount));
    }

    @Override
    public void updateOrderValue(int totalOrderValue) {
        tvOrderValue.setText(String.valueOf(totalOrderValue));
    }

    @Override
    public void displayTotalOutletCount(int totalOutlet) {
        tvTotalOutlet.setText(String.valueOf(totalOutlet));
    }

    @Override
    public void updateCoveredCount(int coveredOutlet) {
        tvCoveredOutlet.setText(String.valueOf(coveredOutlet));
    }

    @Override
    public void updateUnbilledCount(int unBilledOutlet) {
        tvUnbilledOutlet.setText(String.valueOf(unBilledOutlet));
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void sellerProductivity(int productivityPercent) {
        tvSellerProductivePercent.setText(productivityPercent+"%");
    }

    class CustomInfoWindowAdapter implements GoogleMap.InfoWindowAdapter {

        @Override
        public View getInfoContents(Marker marker) {
            return null;
        }

        @SuppressLint("SetTextI18n")
        @Override
        public View getInfoWindow(final Marker marker) {

            tvMapInfoUserName.setText(marker.getTitle());

            mapWrapperLayout.setMarkerWithInfoWindow(marker, mymarkerview);

            return mymarkerview;
        }

    }

    private int getPixelsFromDp(Context context, float dp) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dp * scale + 0.5f);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        supervisorHomePresenter.removeFirestoreListener();
    }
}
