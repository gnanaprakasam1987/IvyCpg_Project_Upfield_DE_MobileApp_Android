package com.ivy.cpg.view.supervisor.mvp.sellerhomescreen;


import android.Manifest;
import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetBehavior;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
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
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.ProgressBar;
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
import com.ivy.cpg.view.supervisor.customviews.ticker.TickerView;
import com.ivy.cpg.view.supervisor.mvp.ManagerialUsersFragment;
import com.ivy.cpg.view.supervisor.mvp.models.SellerBo;
import com.ivy.cpg.view.supervisor.mvp.SupervisorActivityHelper;
import com.ivy.cpg.view.supervisor.mvp.outletmapview.OutletMapListActivity;
import com.ivy.cpg.view.supervisor.mvp.sellerlistview.SellerListActivity;
import com.ivy.cpg.view.supervisor.mvp.sellerperformance.sellerperformancelist.SellerPerformanceListActivity;
import com.ivy.lib.Utils;
import com.ivy.maplib.MapWrapperLayout;
import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.commons.IvyBaseFragment;
import com.ivy.sd.png.commons.SDUtil;
import com.ivy.sd.png.util.Commons;
import com.ivy.cpg.view.homescreen.HomeScreenActivity;
import com.ivy.utils.DateTimeUtils;
import com.ivy.utils.FontUtils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;

public class SellersMapHomeFragment extends IvyBaseFragment implements
        OnMapReadyCallback, View.OnClickListener, GoogleMap.OnMarkerClickListener,
        GoogleMap.OnInfoWindowClickListener, SellerMapHomeContract.SellerMapHomeView {

    private GoogleMap mMap;
    private BottomSheetBehavior bottomSheetBehavior;
    private TickerView totalSeller, tvOrderValue, tvUnbilledOutlet, tvTotalOutlet, tvCoveredOutlet, absentSeller, marketSeller;
    private TextView tvSellerProductivePercent;
    private MapWrapperLayout mapWrapperLayout;
    private ViewGroup mymarkerview;
    private TextView tvMapInfoUserName;
    private RecyclerViewPager sellerListRecyclerView;
    private ProgressBar progressBar;
    private SellerMapHomePresenter sellerMapHomePresenter;
    private InMarketSellerAdapter inMarketSellerAdapter;

    private ArrayList<SellerBo> inMarketSellerArrayList = new ArrayList<>();
    private DatePickerDialog picker;
    private String selectedDate = "";
    private int loginUserId;


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

        selectedDate = DateTimeUtils.now(DateTimeUtils.DATE_DOB_FORMAT_PLAIN);

        sellerMapHomePresenter = new SellerMapHomePresenter();
        sellerMapHomePresenter.setView(this, getContext());
        sellerMapHomePresenter.setSelectedDate(selectedDate);

        loginUserId = sellerMapHomePresenter.getLoginUserId();

        initViews(view);
        initViewPager(view);

        SupportMapFragment mapFragment = (SupportMapFragment) this.getChildFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

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
        progressBar = view.findViewById(R.id.progressBar);

        tvMapInfoUserName = mymarkerview.findViewById(R.id.tv_usr_name);

        view.findViewById(R.id.ttl_seller_layout).setOnClickListener(this);
        view.findViewById(R.id.ttl_outlet_layout).setOnClickListener(this);
        view.findViewById(R.id.covered_outlet_layout).setOnClickListener(this);
        view.findViewById(R.id.unbilled_layout).setOnClickListener(this);
        view.findViewById(R.id.seller_view_btn).setOnClickListener(this);

        //Bottom sheet layout Typeface
        ((TextView) view.findViewById(R.id.tv_txt_ttl_seller)).setTypeface(FontUtils.getFontRoboto(getContext(),FontUtils.FontType.REGULAR));
        ((TextView) view.findViewById(R.id.tv_txt_ttl_outlet)).setTypeface(FontUtils.getFontRoboto(getContext(),FontUtils.FontType.REGULAR));
        ((TextView) view.findViewById(R.id.tv_txt_covered_outlet)).setTypeface(FontUtils.getFontRoboto(getContext(),FontUtils.FontType.REGULAR));
        ((TextView) view.findViewById(R.id.tv_txt_unbilled_outlet)).setTypeface(FontUtils.getFontRoboto(getContext(),FontUtils.FontType.REGULAR));
        ((TextView) view.findViewById(R.id.tv_txt_order_value)).setTypeface(FontUtils.getFontRoboto(getContext(),FontUtils.FontType.REGULAR));
        ((TextView) view.findViewById(R.id.tv_txt_ttl_market_seller)).setTypeface(FontUtils.getFontRoboto(getContext(),FontUtils.FontType.LIGHT));
        ((TextView) view.findViewById(R.id.tv_txt_ttl_absent_seller)).setTypeface(FontUtils.getFontRoboto(getContext(),FontUtils.FontType.LIGHT));
        ((TextView) view.findViewById(R.id.tv_inmarket_seller)).setTypeface(FontUtils.getFontRoboto(getContext(),FontUtils.FontType.LIGHT));

        totalSeller.setTypeface(FontUtils.getFontRoboto(getContext(),FontUtils.FontType.MEDIUM));
        absentSeller.setTypeface(FontUtils.getFontRoboto(getContext(),FontUtils.FontType.MEDIUM));
        marketSeller.setTypeface(FontUtils.getFontRoboto(getContext(),FontUtils.FontType.MEDIUM));
        tvCoveredOutlet.setTypeface(FontUtils.getFontRoboto(getContext(),FontUtils.FontType.MEDIUM));
        tvUnbilledOutlet.setTypeface(FontUtils.getFontRoboto(getContext(),FontUtils.FontType.MEDIUM));
        tvTotalOutlet.setTypeface(FontUtils.getFontRoboto(getContext(),FontUtils.FontType.MEDIUM));
        tvOrderValue.setTypeface(FontUtils.getFontRoboto(getContext(),FontUtils.FontType.MEDIUM));
        tvSellerProductivePercent.setTypeface(FontUtils.getFontRoboto(getContext(),FontUtils.FontType.MEDIUM));

        tvMapInfoUserName.setTypeface(FontUtils.getFontRoboto(getContext(),FontUtils.FontType.REGULAR));


        mapWrapperLayout = view.findViewById(R.id.map_wrap_layout);
        mapWrapperLayout.init(mMap, getPixelsFromDp(getContext(), 39 + 20));

        bottomSheetBehavior = BottomSheetBehavior.from(view.findViewById(R.id.bottomSheetLayout));

        bottomSheetBehavior.setHideable(false);
        bottomSheetBehavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                switch (newState) {
                    case BottomSheetBehavior.STATE_COLLAPSED:
                        if (sellerListRecyclerView.getVisibility() == View.VISIBLE) {
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

                Bundle values = new Bundle();
                values.putInt("TabPos", 0);
                values.putString("Screen", "Seller");
                values.putParcelableArrayList("SellerList", sellerMapHomePresenter.getAllSellerList());
                values.putInt("Sellerid", loginUserId);
                values.putString("Date", selectedDate);
                intent.putExtra("SellerInfo", values);

                startActivity(intent);
            }
        });

        view.findViewById(R.id.ttl_outlet_layout).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), OutletMapListActivity.class);
                intent.putExtra("TabPos", 0);
                intent.putExtra("Screen", "Outlet");
                intent.putExtra("Sellerid", loginUserId);
                intent.putExtra("Date", selectedDate);
                startActivity(intent);
            }
        });

        view.findViewById(R.id.covered_outlet_layout).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), OutletMapListActivity.class);
                intent.putExtra("TabPos", 1);
                intent.putExtra("Screen", "Outlet");
                intent.putExtra("Sellerid", loginUserId);
                intent.putExtra("Date", selectedDate);
                startActivity(intent);
            }
        });

        view.findViewById(R.id.unbilled_layout).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), OutletMapListActivity.class);
                intent.putExtra("TabPos", 2);
                intent.putExtra("Screen", "Outlet");
                intent.putExtra("Sellerid", loginUserId);
                intent.putExtra("Date", selectedDate);
                startActivity(intent);
            }
        });

        view.findViewById(R.id.seller_view_btn).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getContext(), SellerPerformanceListActivity.class);
                intent.putExtra("Screen", "Seller Performance");
                intent.putExtra("Sellerid", loginUserId);
                intent.putExtra("Date", selectedDate);
                startActivity(intent);
            }
        });

        view.findViewById(R.id.recenter_location).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sellerMapHomePresenter.getMarkerValuesToFocus();
            }
        });

    }

    private void initViewPager(final View view) {
        sellerListRecyclerView = view.findViewById(R.id.viewpager);
        sellerListRecyclerView.setVisibility(View.GONE);
        LinearLayoutManager layout = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL,
                false);
        sellerListRecyclerView.setLayoutManager(layout);

        inMarketSellerAdapter = new InMarketSellerAdapter(getContext().getApplicationContext(), inMarketSellerArrayList, sellerMapHomePresenter);
        sellerListRecyclerView.setAdapter(inMarketSellerAdapter);

        sellerListRecyclerView.setHasFixedSize(true);
        sellerListRecyclerView.setLongClickable(true);
        sellerListRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int scrollState) {
//                updateState(scrollState);
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int i, int i2) {
                int childCount = sellerListRecyclerView.getChildCount();
                int width = sellerListRecyclerView.getChildAt(0).getWidth();
                int padding = (sellerListRecyclerView.getWidth() - width) / 2;

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

        sellerListRecyclerView.addOnPageChangedListener(new RecyclerViewPager.OnPageChangedListener() {
            @Override
            public void OnPageChanged(int oldPosition, int newPosition) {

                double angle = 130.0;

                double x = Math.sin(-angle * Math.PI / 180) * 0.5 + getResources().getDimension(R.dimen.supervisor_home_map_info_x);
                double y = -(Math.cos(-angle * Math.PI / 180) * 0.5 - getResources().getDimension(R.dimen.supervisor_home_map_info_y));

                inMarketSellerArrayList.get(newPosition).getMarker().setInfoWindowAnchor((float) x, (float) y);

                mMap.animateCamera(CameraUpdateFactory.newLatLng(inMarketSellerArrayList.get(newPosition).getMarker().getPosition()));
                inMarketSellerArrayList.get(newPosition).getMarker().showInfoWindow();


            }
        });

        sellerListRecyclerView.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                if (sellerListRecyclerView.getChildCount() < 3) {
                    if (sellerListRecyclerView.getChildAt(1) != null) {
                        if (sellerListRecyclerView.getCurrentPosition() == 0) {
                            View v1 = sellerListRecyclerView.getChildAt(1);
                            v1.setScaleY(0.9f);
                            v1.setScaleX(0.9f);
                        } else {
                            View v1 = sellerListRecyclerView.getChildAt(0);
                            v1.setScaleY(0.9f);
                            v1.setScaleX(0.9f);
                        }
                    }
                } else {
                    if (sellerListRecyclerView.getChildAt(0) != null) {
                        View v0 = sellerListRecyclerView.getChildAt(0);
                        v0.setScaleY(0.9f);
                        v0.setScaleX(0.9f);
                    }
                    if (sellerListRecyclerView.getChildAt(2) != null) {
                        View v2 = sellerListRecyclerView.getChildAt(2);
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
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);

        if (!sellerMapHomePresenter.isToday())
            menu.findItem(R.id.menu_date).setTitle(sellerMapHomePresenter.convertPlaneDateToGlobal(selectedDate));
        else
            menu.findItem(R.id.menu_date).setTitle("Today");
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            startActivity(new Intent(getActivity(),
                    HomeScreenActivity.class));
            getActivity().finish();
            return true;
        } else if (item.getItemId() == R.id.menu_dashboard) {
            if (bottomSheetBehavior.getState() == BottomSheetBehavior.STATE_HIDDEN ||
                    bottomSheetBehavior.getState() == BottomSheetBehavior.STATE_COLLAPSED)
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
            else {
                if (sellerListRecyclerView.getVisibility() == View.GONE)
                    bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                else {
                    bottomSheetBehavior.setHideable(true);
                    bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
                }

            }
        } else if (item.getItemId() == R.id.menu_date) {
            showDatePicker();
        } else if (item.getItemId() == R.id.menu_user) {
            displayUsers();
        }

        return false;
    }

    @Override
    public void onClick(View v) {

    }

    @Override
    public void onInfoWindowClick(Marker marker) {

        if (bottomSheetBehavior.getState() == BottomSheetBehavior.STATE_EXPANDED) {
            bottomSheetBehavior.setHideable(true);
            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
        }
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        if (bottomSheetBehavior.getState() == BottomSheetBehavior.STATE_EXPANDED || bottomSheetBehavior.getState() == BottomSheetBehavior.STATE_COLLAPSED) {
            bottomSheetBehavior.setHideable(true);
            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
        }

        sellerMapHomePresenter.getSellerMarkerInfo(marker.getSnippet());

        sellerListRecyclerView.setVisibility(View.VISIBLE);

        int pagerPos = 0;
        int count = 0;
        for (SellerBo detailsBo : inMarketSellerArrayList) {
            if (detailsBo.getMarker().getSnippet().equalsIgnoreCase(marker.getSnippet())) {
                pagerPos = count;
                break;
            }
            count = count + 1;
        }

        sellerListRecyclerView.scrollToPosition(pagerPos);

        return true;
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMaxZoomPreference(24);
        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(getContext().getApplicationContext(), "Enable location permission from App Settings", Toast.LENGTH_SHORT).show();

        } else {
            mMap.setMyLocationEnabled(false);
        }

        mMap.getUiSettings().setMyLocationButtonEnabled(false);

        //map style restricting landmarks
//        mMap.setMapStyle(MapStyleOptions.loadRawResourceStyle(getContext(), R.raw.map_style));

        mMap.setOnMarkerClickListener(this);
        mMap.setInfoWindowAdapter(new CustomInfoWindowAdapter());
        mMap.setOnInfoWindowClickListener(this);
        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {

            @Override
            public void onMapClick(LatLng latLng) {

                sellerListRecyclerView.setVisibility(View.GONE);

                if (bottomSheetBehavior.getState() == BottomSheetBehavior.STATE_EXPANDED)
                    bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                else if (bottomSheetBehavior.getState() == BottomSheetBehavior.STATE_COLLAPSED) {
                    bottomSheetBehavior.setHideable(true);
                    bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
                } else if (bottomSheetBehavior.getState() == BottomSheetBehavior.STATE_HIDDEN)
                    bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);

            }
        });

        sellerMapHomePresenter.getSellerListAWS(DateTimeUtils.now(DateTimeUtils.DATE_GLOBAL));

        sellerMapHomePresenter.loginToFirebase(getContext().getApplicationContext(), loginUserId);
    }

    @Override
    public void firebaseLoginSuccess() {

        updateSellerInfoByDate(selectedDate);
//        sellerMapHomePresenter.setSupervisorLastVisit();

    }

    @Override
    public void firebaseLoginFailure() {
        Toast.makeText(getContext().getApplicationContext(), "Firebase Login Failed", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void createMarker(SellerBo sellerBo, MarkerOptions markerOptions) {

        Marker marker = mMap.addMarker(markerOptions);

        sellerBo.setMarker(marker);
    }

    @Override
    public void updateMaker(LatLng destinationLatLng, Marker marker) {

        sellerMapHomePresenter.animateSellerMarker(destinationLatLng, marker);
    }

    @Override
    public void focusMarker(final LatLngBounds.Builder builder) {

        mMap.setOnMapLoadedCallback(new GoogleMap.OnMapLoadedCallback() {
            @Override
            public void onMapLoaded() {

                if (sellerMapHomePresenter.checkAreaBoundsTooSmall(builder.build(), 300)) {
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(builder.build().getCenter(), 19));
                } else {
                    mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(builder.build(), 60));
                }

            }
        });
    }

    @Override
    public void setSellerListAdapter(ArrayList<SellerBo> modelBoArrayList) {
        inMarketSellerArrayList.clear();
        inMarketSellerArrayList.addAll(modelBoArrayList);
        inMarketSellerAdapter.notifyDataSetChanged();
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
    public void updateOrderValue(double totalOrderValue) {
        tvOrderValue.setText(Utils.formatAsTwoDecimal(totalOrderValue));
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

        if (productivityPercent > 100)
            productivityPercent = 100;

        tvSellerProductivePercent.setText(productivityPercent + "%");
        progressBar.setProgress(productivityPercent);
    }

    @Override
    public void updateSellerInfoByDate(String selectedDate) {

        this.selectedDate = selectedDate;
        sellerMapHomePresenter.setSelectedDate(selectedDate);

        sellerMapHomePresenter.sellerAttendanceInfoListener(loginUserId, selectedDate);

        sellerMapHomePresenter.sellerActivityInfoListener(loginUserId, selectedDate);

        if (sellerMapHomePresenter.isRealtimeLocation())
            sellerMapHomePresenter.realtimeLocationInfoListener(loginUserId, selectedDate);

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

    private void showDatePicker() {

        String[] splitDate = sellerMapHomePresenter.convertPlaneDateToGlobal(selectedDate).split("/");

        int day = SDUtil.convertToInt(splitDate[2]);
        int month = SDUtil.convertToInt(splitDate[1]);
        int year = SDUtil.convertToInt(splitDate[0]);
        // date picker dialog

        Calendar cal = Calendar.getInstance();
        cal.set(day, month, year);

        picker = new DatePickerDialog(getContext(), R.style.SellerDatePickerStyle, mDateSetListener, day, month, year);

        Calendar maxDate = Calendar.getInstance();
        maxDate.set(Calendar.HOUR_OF_DAY, 23);
        maxDate.set(Calendar.MINUTE, 59);
        maxDate.set(Calendar.SECOND, 59);

        picker.getDatePicker().setMaxDate(maxDate.getTimeInMillis());
        picker.updateDate(year, month - 1, day);

        picker.show();

    }

    private DatePickerDialog.OnDateSetListener mDateSetListener = new DatePickerDialog.OnDateSetListener() {
        @Override
        public void onDateSet(DatePicker view, int year, int month, int day) {

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd", Locale.ENGLISH);
            Calendar dateConversion = new GregorianCalendar(year, month, day);
            String convertedDate = sdf.format(dateConversion.getTime());

            Toast.makeText(getContext(),
                    "Selected Date " + convertedDate,
                    Toast.LENGTH_SHORT).show();
            picker.hide();


            if (!sellerMapHomePresenter.isSameDateSelected(convertedDate)) {

                if (mMap != null)
                    mMap.clear();

                tvOrderValue.setText("0");
                tvCoveredOutlet.setText("0");
                tvSellerProductivePercent.setText("0");
                tvTotalOutlet.setText("0");
                tvUnbilledOutlet.setText("0");
                absentSeller.setText("0");
                marketSeller.setText("0");
                totalSeller.setText("0");
                progressBar.setProgress(0);

                sellerMapHomePresenter.removeFirestoreListener();

                selectedDate = sellerMapHomePresenter.convertGlobalDateToPlane(convertedDate);
                sellerMapHomePresenter.setSelectedDate(selectedDate);

                getActivity().invalidateOptionsMenu();

                if (!sellerMapHomePresenter.checkSelectedDateExist(convertedDate))
                    sellerMapHomePresenter.downloadSupRetailerMaster(convertedDate);
                else {
                    SupervisorActivityHelper.getInstance().downloadOutletListAws(getContext(), convertedDate);
                    sellerMapHomePresenter.getSellerListAWS(convertedDate);
                    updateSellerInfoByDate(sellerMapHomePresenter.convertGlobalDateToPlane(convertedDate));
                }
            }
        }
    };


    @Override
    public void onDetach() {
        super.onDetach();
        if (sellerMapHomePresenter != null)
            sellerMapHomePresenter.removeFirestoreListener();
    }

//    public static double dpToPx(double dp)
//    {
//        DisplayMetrics displaymetrics = new DisplayMetrics();
//        dp = TypedValue.applyDimension( TypedValue.COMPLEX_UNIT_DIP, (float) dp, displaymetrics );
//
//        return dp;
//    }

    public double dpToPx(double dp) {
        float density = getResources()
                .getDisplayMetrics()
                .density;
        return dp * density;
    }

    private void displayUsers() {
        try {

            FragmentManager fm = getActivity().getSupportFragmentManager();
            SupportMapFragment frag = (SupportMapFragment) fm
                    .findFragmentById(R.id.map);
            FragmentTransaction ft = fm
                    .beginTransaction();
            if (frag != null)
                ft.remove(frag);

            ManagerialUsersFragment fragment = (ManagerialUsersFragment) fm
                    .findFragmentByTag("userfragment");
            if (fragment != null)
                ft.detach(fragment);

            ManagerialUsersFragment fragobj = new ManagerialUsersFragment();
            ft.add(R.id.map_wrap_layout, fragobj, "userfragment");
            ft.commitAllowingStateLoss();

        } catch (Exception e) {
            Commons.printException(e);
        }
    }
}
