package com.ivy.cpg.view.supervisor.mvp.supervisorhomepage;


import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.design.widget.BottomSheetBehavior;
import android.support.v4.app.ActivityCompat;
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
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.ivy.cpg.view.supervisor.helper.DetailsBo;
import com.ivy.cpg.view.supervisor.customviews.recyclerviewpager.RecyclerViewPager;
import com.ivy.maplib.MapWrapperLayout;
import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.commons.IvyBaseFragment;
import com.ivy.sd.png.util.Commons;
import com.ivy.sd.png.view.HomeScreenActivity;

public class SupervisorHomeFragment extends IvyBaseFragment implements
        OnMapReadyCallback, View.OnClickListener, GoogleMap.OnMarkerClickListener,
        GoogleMap.OnInfoWindowClickListener,SupervisorHomeContract.SupervisorHomeView{

    private GoogleMap mMap;
    boolean isFirst = true;
    private BottomSheetBehavior bottomSheetBehavior;
    private TextView totalSeller, absentSeller, marketSeller;
    private MapWrapperLayout mapWrapperLayout;
    private ViewGroup mymarkerview;
    private TextView tvMapInfoUserName;
    private RecyclerViewPager mRecyclerView;

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

        SupervisorHomePresenter supervisorHomePresenter = new SupervisorHomePresenter();
        supervisorHomePresenter.loginToFirebase(getContext().getApplicationContext(),this);

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

        view.findViewById(R.id.ttl_seller_layout).setOnClickListener(this);
        view.findViewById(R.id.ttl_outlet_layout).setOnClickListener(this);
        view.findViewById(R.id.covered_outlet_layout).setOnClickListener(this);
        view.findViewById(R.id.unbilled_layout).setOnClickListener(this);
        view.findViewById(R.id.seller_view_btn).setOnClickListener(this);

        mapWrapperLayout = view.findViewById(R.id.map_wrap_layout);
        mapWrapperLayout.init(mMap, getPixelsFromDp(getContext(), 39 + 20));

        tvMapInfoUserName = mymarkerview.findViewById(R.id.tv_usr_name);

        bottomSheetBehavior = BottomSheetBehavior.from(view.findViewById(R.id.bottomSheetLayout));

    }

    protected void initViewPager(final View view) {
        mRecyclerView = view.findViewById(R.id.viewpager);
        mRecyclerView.setVisibility(View.GONE);
        LinearLayoutManager layout = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL,
                false);
        mRecyclerView.setLayoutManager(layout);

        MyAdapter myAdapter = new MyAdapter(getContext().getApplicationContext());
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
        return false;
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
    }

    @Override
    public void firebaseLoginFailure() {
        Toast.makeText(getContext().getApplicationContext(), "Firebase Login Failed", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void updateSellerCount() {

    }

    @Override
    public void updateSellerMarkerInfo(DetailsBo detailsBo) {

    }

    @Override
    public void updateSellerFirebaseInfo(DetailsBo detailsBo) {

    }

    class CustomInfoWindowAdapter implements GoogleMap.InfoWindowAdapter {

        @Override
        public View getInfoContents(Marker marker) {
            return null;
        }

        @SuppressLint("SetTextI18n")
        @Override
        public View getInfoWindow(final Marker marker) {

            tvMapInfoUserName.setText(marker.getTitle()+" Id "+marker.getSnippet());

            mapWrapperLayout.setMarkerWithInfoWindow(marker, mymarkerview);

            return mymarkerview;
        }

    }

    private int getPixelsFromDp(Context context, float dp) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dp * scale + 0.5f);
    }

}
