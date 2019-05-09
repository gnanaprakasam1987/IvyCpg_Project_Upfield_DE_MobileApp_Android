package com.ivy.cpg.view.profile;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.ivy.cpg.locationservice.LocationServiceHelper;
import com.ivy.cpg.nfc.NFCManager;
import com.ivy.cpg.nfc.NFCReadDialogActivity;
import com.ivy.cpg.view.dashboard.DashBoardHelper;
import com.ivy.cpg.view.dashboard.sellerdashboard.SellerDashboardFragment;
import com.ivy.cpg.view.profile.otpValidation.OTPValidationHelper;
import com.ivy.cpg.view.profile.otpValidation.RetailerSequenceSkipDialog;
import com.ivy.cpg.view.homescreen.HomeScreenActivity;
import com.ivy.cpg.view.reports.dynamicReport.DynamicReportFragment;
import com.ivy.cpg.view.reports.dynamicReport.DynamicReportHelper;
import com.ivy.cpg.view.retailercontact.RetailerContactFragment;
import com.ivy.cpg.view.van.LoadManagementHelper;
import com.ivy.location.LocationUtil;
import com.ivy.sd.camera.CameraActivity;
import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.bo.ConfigureBO;
import com.ivy.sd.png.bo.RetailerMasterBO;
import com.ivy.sd.png.bo.SupplierMasterBO;
import com.ivy.sd.png.bo.UserMasterBO;
import com.ivy.sd.png.commons.CustomMapFragment;
import com.ivy.sd.png.commons.IvyBaseActivityNoActionBar;
import com.ivy.sd.png.commons.MapWrapperLayout;
import com.ivy.sd.png.commons.SDUtil;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.model.UserDialogInterface;
import com.ivy.sd.png.provider.ConfigurationMasterHelper;
import com.ivy.sd.png.provider.DownloadProductsAndPrice;
import com.ivy.sd.png.provider.SynchronizationHelper;
import com.ivy.sd.png.util.CommonDialog;
import com.ivy.sd.png.util.Commons;
import com.ivy.sd.png.util.DataMembers;
import com.ivy.sd.png.util.TimerCount;
import com.ivy.cpg.view.profile.assetHistory.AssetHistoryFragment;
import com.ivy.cpg.view.profile.mslUnsold.MSLUnsoldFragment;
import com.ivy.sd.png.view.NearByRetailerDialog;
import com.ivy.sd.png.view.PlanningVisitActivity;
import com.ivy.cpg.view.profile.userSelection.UserSelectionDialogue;
import com.ivy.cpg.view.profile.orderandinvoicehistory.InvoiceHistoryFragment;
import com.ivy.cpg.view.profile.orderandinvoicehistory.OrderHistoryFragment;
import com.ivy.cpg.view.profile.otpValidation.OTPValidationDialog;
import com.ivy.ui.profile.edit.view.ProfileEditActivity;
import com.ivy.ui.task.TaskConstant;
import com.ivy.utils.DateTimeUtils;
import com.ivy.ui.task.view.TaskFragment;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;

import static android.content.res.Configuration.SCREENLAYOUT_SIZE_LARGE;


public class ProfileActivity extends IvyBaseActivityNoActionBar
        implements NearByRetailerDialog.NearByRetailerInterface,
        MapWrapperLayout.OnDragListener,
        CommonReasonDialog.AddNonVisitListener,
        View.OnClickListener {

    private static final String MENU_VISIT = "Trade Coverage";
    private static final String MENU_PLANNING = "Day Planning";
    private static final String MENU_PLANNING_SUB = "Day Planning Sub";

    private static final int CAMERA_REQUEST_CODE = 100;
    private CollapsingToolbarLayout collapsingToolbarLayout;

    private BusinessModel bmodel;
    private Bundle bundle;
    private DisplayMetrics displaymetrics;

    private int scrollRange = -1;

    private double lat = 0.0, lng = 0.0;
    public static double retailerLat = 0, retailerLng = 0;

    private String mVisitMode = "";
    private String mNFCReasonId = "0";
    private String title;

    // To save transaction
    private String dateTimeStampForId = "";

    private String fnameStarts = "";

    private String calledBy;

    private boolean isdrawRoute = false;
    private boolean mNFCValidationPassed;
    private boolean mLocationConfirmationPassed;
    private boolean non_visit;
    private boolean is7InchTablet;
    private boolean isClicked;
    private boolean isVisible = false;
    private boolean isLatLong;
    private static boolean firstLevZoom;
    private boolean fromHomeClick = false, visitClick = false, isFromPlanning = false, isFromPlanningSub = false;

    private List<LatLng> markerList = new ArrayList<>();
    private HashMap<String, ArrayList<UserMasterBO>> mUserByRetailerID;
    private ArrayAdapter<SupplierMasterBO> mSupplierAdapter;

    private GoogleMap mMap;
    private RetailerMasterBO retailerObj;

    private LatLng retLatLng, curLatLng;
    private OTPValidationDialog otpValidationDialog;
    private RetailerSequenceSkipDialog retSeqSkipDialog;
    private android.content.DialogInterface.OnDismissListener otpPasswordDismissListenerNew;

    private AppBarLayout appbar;
    private Drawable upArrow;
    private ImageView profileEditBtn;
    private ImageView drawRouteBtn;
    private ImageView mapSwitchBtn;
    private ProgressBar mapProgressBar;
    private TextView retailerNameTxt, retailerCodeTxt;
    private LinearLayout iconLinearLayout;
    private TabLayout tabLayout;
    private ViewPager viewPager;
    private LinearLayout linearLayout;
    private View bottomView;
    private AlertDialog alertDialog;
    private Button startVisitBtn, cancelVisitBtn, deviateBtn, addPlaneBtn;
    private boolean isNonVisitReason = false;

    private UserRetailerTransactionReceiver receiver;
    private String ASSET_HISTORY = "";
    private String TASK = "";
    private String SALES_PER_LEVEL = "";
    private String invoice_history_title = "", msl_title = "", retailer_kpi_title = "", plan_outlet_title = "", order_history_title = "", profile_title = "", retailer_contact_title;

    private Timer mLocTimer;
    private LocationFetchTimer timerTask;

    private AlertDialog mLocationAlertDialog;

    private DownloadProductsAndPrice downloadProductsAndPrice;

    private String DISTRIBUTOR_PROFILE = "";

    Handler handler = null;
    Runnable runnable = null;

    String dynamicReportTitle = "";

    String selectedUserId = "";
    private boolean fromMap;
    private boolean isLocValDone;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_profile);

        bmodel = (BusinessModel) getApplicationContext();
        bmodel.setContext(this);

        overridePendingTransition(R.anim.trans_left_in, R.anim.trans_left_out);

        firstLevZoom = true;
        fromHomeClick = false;

        // Duplicate call - Commented
        //getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        displaymetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        is7InchTablet = this.getResources().getConfiguration().isLayoutSizeAtLeast(SCREENLAYOUT_SIZE_LARGE);

        otpPasswordDismissListenerNew = new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                dialog.dismiss();
                isLocValDone = true;
                validationToStartVisit();
            }
        };


        if (bmodel.userMasterHelper.getUserMasterBO().getUserid() == 0) {
            Toast.makeText(this,
                    getResources().getString(R.string.sessionout_loginagain),
                    Toast.LENGTH_SHORT).show();
            finish();
        }

        if (bmodel.configurationMasterHelper.SHOW_CAPTURED_LOCATION) {
            checkAndRequestPermissionAtRunTime(3);
        }


        retriveIntentValue();

        initilizeToolBar();

        initilizeViews();

        if ("P".equals(bmodel.getAppDataProvider().getRetailMaster().getIsVisited()))
            startVisitBtn.setText(getResources().getString(R.string.resume_visit));

        setCustomFont();

        bundle = new Bundle();
        bundle.putBoolean("fromHomeClick", fromHomeClick);
        bundle.putBoolean("non_visit", non_visit);
        addTabLayout();

        hideVisibleComponents();


        //downloadProductsAndPrice = new DownloadProductsAndPrice();

        new LoadProfileConfigs().execute();

        bmodel.isModuleDone(true);
        new loadActivityMenu().execute();


        try {
            CustomMapFragment mCustomMapFragment = ((CustomMapFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.profile_map));
            mCustomMapFragment.setOnDragListener(ProfileActivity.this);
            mCustomMapFragment.getMapAsync(new OnMapReadyCallback() {
                @Override
                public void onMapReady(GoogleMap googleMap) {
                    mMap = googleMap;
                    //Disable Map Toolbar:
                    mMap.getUiSettings().setMapToolbarEnabled(false);
                    mMap.getUiSettings().setZoomControlsEnabled(false);
                }
            });

            if (mMap != null) {
                isLatLong = true;
                markerList = new ArrayList<>();
            }
        } catch (Exception e) {
            Commons.printException(e);
        }

        if (bmodel.configurationMasterHelper.isAuditEnabled()) {
            mUserByRetailerID = bmodel.getUserByRetailerID();
            registerReceiver();
        }


    }

    private void setCustomFont() {

        startVisitBtn.setTypeface(bmodel.configurationMasterHelper.getFontBaloobhai(ConfigurationMasterHelper.FontType.REGULAR));
        cancelVisitBtn.setTypeface(bmodel.configurationMasterHelper.getFontBaloobhai(ConfigurationMasterHelper.FontType.REGULAR));
        deviateBtn.setTypeface(bmodel.configurationMasterHelper.getFontBaloobhai(ConfigurationMasterHelper.FontType.REGULAR));
        addPlaneBtn.setTypeface(bmodel.configurationMasterHelper.getFontBaloobhai(ConfigurationMasterHelper.FontType.REGULAR));

        retailerNameTxt.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
        retailerCodeTxt.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
    }

    private void initilizeViews() {
        mapProgressBar = findViewById(R.id.progress_map);
        retailerNameTxt = findViewById(R.id.retailer_name);
        retailerCodeTxt = findViewById(R.id.retailer_code);
        iconLinearLayout = findViewById(R.id.img_layout);

        collapsingToolbarLayout = findViewById(R.id.collapsing);
        collapsingToolbarLayout.setTitleEnabled(false);
        collapsingToolbarLayout.setCollapsedTitleTextColor(Color.WHITE);
        collapsingToolbarLayout.setCollapsedTitleTypeface(bmodel.configurationMasterHelper.getFontBaloobhai(ConfigurationMasterHelper.FontType.REGULAR));

        appbar = findViewById(R.id.appbar);

        linearLayout = findViewById(R.id.bottom_layout);
        bottomView = findViewById(R.id.reason_view);
        tabLayout = findViewById(R.id.tab_layout);
        viewPager = findViewById(R.id.pager);

        startVisitBtn = findViewById(R.id.start_visit);
        startVisitBtn.setOnClickListener(this);
        cancelVisitBtn = findViewById(R.id.cancel_visit);
        cancelVisitBtn.setOnClickListener(this);
        deviateBtn = findViewById(R.id.profile_deviate);
        deviateBtn.setOnClickListener(this);
        addPlaneBtn = findViewById(R.id.add_plane);
        addPlaneBtn.setOnClickListener(this);
        profileEditBtn = findViewById(R.id.profile_edit_click);
        profileEditBtn.setOnClickListener(this);
        drawRouteBtn = findViewById(R.id.draw_routeimg_btn);
        drawRouteBtn.setOnClickListener(this);
        mapSwitchBtn = findViewById(R.id.profile_mapswitch);
        mapSwitchBtn.setOnClickListener(this);
    }

    private void initilizeToolBar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            try {
                //noinspection ConstantConditions
                getSupportActionBar().setHomeButtonEnabled(true);
            } catch (NullPointerException e) {
                Commons.printException(e);
            }
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            getSupportActionBar().setHomeAsUpIndicator(upArrow);
        }
    }

    /**
     * This method will retrive the intent value and update the local variable.
     */
    private void retriveIntentValue() {

        non_visit = getIntent().getBooleanExtra("non_visit", false);
        visitClick = getIntent().getBooleanExtra("locvisit", false);
        fromHomeClick = getIntent().getBooleanExtra("hometwo", false);
        isFromPlanning = getIntent().getBooleanExtra("isPlanning", false);
        isFromPlanningSub = getIntent().getBooleanExtra("isPlanningSub", false);
        fromMap = getIntent().getBooleanExtra("map", false);

        try {
            Intent arg = getIntent();
            if (arg != null)
                calledBy = arg.getStringExtra("From");

            if (calledBy == null)
                calledBy = MENU_VISIT;
        } catch (Exception e) {
            calledBy = MENU_VISIT;
        }
    }

    /**
     * Method used to add Tabs based on configurations.
     */
    private void addTabLayout() {

        try {
            if ((bmodel.labelsMasterHelper.applyLabels("profile") != null) &&
                    (bmodel.labelsMasterHelper.applyLabels("profile").length() > 0)) {
                profile_title = bmodel.labelsMasterHelper.applyLabels("profile");
                tabLayout.addTab(tabLayout.newTab().setText(profile_title));
            } else {
                profile_title = getResources().getString(R.string.profile);
                tabLayout.addTab(tabLayout.newTab().setText(profile_title));
            }
        } catch (Exception ex) {
            Commons.printException("Error while setting label for Profile Tab", ex);
        }
        if (bmodel.configurationMasterHelper.SHOW_RETAILER_CONTACT) {
            try {
                if ((bmodel.labelsMasterHelper.applyLabels("retailer_contact") != null) &&
                        (bmodel.labelsMasterHelper.applyLabels("retailer_contact").length() > 0)) {
                    retailer_contact_title = bmodel.labelsMasterHelper.applyLabels("retailer_contact");
                    tabLayout.addTab(tabLayout.newTab()
                            .setText(retailer_contact_title));
                } else {
                    retailer_contact_title = "Contacts";
                    tabLayout.addTab(tabLayout.newTab().setText(retailer_contact_title));
                }
            } catch (Exception ex) {
                Commons.printException("Error while setting label for Msl Tab", ex);
            }
        }
        if (bmodel.configurationMasterHelper.SHOW_ORDER_HISTORY) {
            try {
                bmodel.configurationMasterHelper.loadProfileHistoryConfiguration();
                if ((bmodel.labelsMasterHelper.applyLabels("order_history") != null) &&
                        (bmodel.labelsMasterHelper.applyLabels("order_history").length() > 0)) {
                    order_history_title = bmodel.labelsMasterHelper.applyLabels("order_history");
                    tabLayout.addTab(tabLayout.newTab()
                            .setText(order_history_title));
                } else {
                    order_history_title = getResources().getString(R.string.history);
                    tabLayout.addTab(tabLayout.newTab().setText(order_history_title));
                }
            } catch (Exception ex) {
                Commons.printException("Error while setting label for Order History Tab", ex);
            }
        }
        if ((bmodel.configurationMasterHelper.SHOW__QDVP3_SCORE_CARD_TAB && (bmodel.retailerMasterBO.getRField4() != null))
                && (bmodel.retailerMasterBO.getRField4().equals("1"))) {
            String survey_score_title;
            try {
                if ((bmodel.labelsMasterHelper.applyLabels("survey_score") != null) &&
                        (bmodel.labelsMasterHelper.applyLabels("survey_score").length() > 0)) {
                    survey_score_title = bmodel.labelsMasterHelper.applyLabels("survey_score");
                    tabLayout.addTab(tabLayout.newTab()
                            .setText(survey_score_title));
                } else {
                    survey_score_title = "Survey Score Card";
                    tabLayout.addTab(tabLayout.newTab().setText(survey_score_title));
                }
            } catch (Exception ex) {
                Commons.printException("Error while setting label for Survey Score Tab", ex);
            }
        }
        if (bmodel.configurationMasterHelper.SHOW_OUTLET_PLANNING_TAB) {
            try {
                if ((bmodel.labelsMasterHelper.applyLabels("plan_outlet") != null) &&
                        (bmodel.labelsMasterHelper.applyLabels("plan_outlet").length() > 0)) {
                    plan_outlet_title = bmodel.labelsMasterHelper.applyLabels("plan_outlet");
                    tabLayout.addTab(tabLayout.newTab()
                            .setText(plan_outlet_title));
                } else {
                    plan_outlet_title = "Planning Outlet";
                    tabLayout.addTab(tabLayout.newTab().setText(plan_outlet_title));
                }
            } catch (Exception ex) {
                Commons.printException("Error while setting label for Outlet Tab", ex);
            }
        }
        if (bmodel.configurationMasterHelper.SHOW_LAST_3MONTHS_BILLS) {
            try {
                if ((bmodel.labelsMasterHelper.applyLabels("retailer_kpi") != null) &&
                        (bmodel.labelsMasterHelper.applyLabels("retailer_kpi").length() > 0)) {
                    retailer_kpi_title = bmodel.labelsMasterHelper.applyLabels("retailer_kpi");
                    tabLayout.addTab(tabLayout.newTab()
                            .setText(retailer_kpi_title));
                } else {
                    retailer_kpi_title = "Retailer Kpi";
                    tabLayout.addTab(tabLayout.newTab().setText(retailer_kpi_title));
                }
            } catch (Exception ex) {
                Commons.printException("Error while setting label for Kpi Tab", ex);
            }
        }
        if (bmodel.configurationMasterHelper.SHOW_ASSET_HISTORY) {
            try {
                if ((bmodel.labelsMasterHelper.applyLabels("asset_history") != null) &&
                        (bmodel.labelsMasterHelper.applyLabels("asset_history").length() > 0)) {
                    ASSET_HISTORY = bmodel.labelsMasterHelper.applyLabels("asset_history");
                    tabLayout.addTab(tabLayout.newTab()
                            .setText(ASSET_HISTORY));
                } else {
                    ASSET_HISTORY = "Asset History";
                    tabLayout.addTab(tabLayout.newTab().setText(ASSET_HISTORY));
                }
            } catch (Exception e) {
                Commons.printException("Error while setting label for Asset History tab", e);
            }
        }
        if (bmodel.configurationMasterHelper.SHOW_MSL_NOT_SOLD) {
            try {
                if ((bmodel.labelsMasterHelper.applyLabels("msl") != null) &&
                        (bmodel.labelsMasterHelper.applyLabels("msl").length() > 0)) {
                    msl_title = bmodel.labelsMasterHelper.applyLabels("msl");
                    tabLayout.addTab(tabLayout.newTab()
                            .setText(msl_title));
                } else {
                    msl_title = "MSL";
                    tabLayout.addTab(tabLayout.newTab().setText(msl_title));
                }
            } catch (Exception ex) {
                Commons.printException("Error while setting label for Msl Tab", ex);
            }
        }
        if (bmodel.configurationMasterHelper.SHOW_INVOICE_HISTORY) {
            try {
                bmodel.configurationMasterHelper.loadInvoiceHistoryConfiguration();
                if ((bmodel.labelsMasterHelper.applyLabels("invoice_history") != null) &&
                        (bmodel.labelsMasterHelper.applyLabels("invoice_history").length() > 0)) {
                    invoice_history_title = bmodel.labelsMasterHelper.applyLabels("invoice_history");
                    tabLayout.addTab(tabLayout.newTab()
                            .setText(invoice_history_title));
                } else {
                    invoice_history_title = getResources().getString(R.string.invoice_history);
                    tabLayout.addTab(tabLayout.newTab().setText(invoice_history_title));
                }
            } catch (Exception ex) {
                Commons.printException("Error while setting label for InvoiceHist Tab", ex);
            }
        }

        if (bmodel.configurationMasterHelper.SHOW_TASK) {
            try {
                if ((bmodel.labelsMasterHelper.applyLabels("task_tab") != null) &&
                        (bmodel.labelsMasterHelper.applyLabels("task_tab").length() > 0)) {
                    TASK = bmodel.labelsMasterHelper.applyLabels("task_tab");
                    tabLayout.addTab(tabLayout.newTab()
                            .setText(TASK));
                } else {
                    TASK = getString(R.string.task);
                    tabLayout.addTab(tabLayout.newTab().setText(TASK));
                }
            } catch (Exception e) {
                Commons.printException("Error while setting label for Task Tab", e);
            }
        }

        if (bmodel.configurationMasterHelper.SHOW_AVG_SALES_PER_LEVEL) {
            try {

                if ((bmodel.labelsMasterHelper.applyLabels("sales_per_level") != null) &&
                        (bmodel.labelsMasterHelper.applyLabels("sales_per_level").length() > 0)) {
                    SALES_PER_LEVEL = bmodel.labelsMasterHelper.applyLabels("sales_per_level");
                    tabLayout.addTab(tabLayout.newTab()
                            .setText(SALES_PER_LEVEL));
                } else {
                    SALES_PER_LEVEL = getString(R.string.sales);
                    tabLayout.addTab(tabLayout.newTab().setText(SALES_PER_LEVEL));
                }
            } catch (Exception e) {
                Commons.printException("Error while setting label for SalesPerLevel Tab", e);
            }
        }

        if (bmodel.configurationMasterHelper.SHOW_DISTRIBUTOR_PROFILE) {
            try {
                if ((bmodel.labelsMasterHelper.applyLabels("distributor_profile") != null) &&
                        (bmodel.labelsMasterHelper.applyLabels("distributor_profile").length() > 0)) {
                    DISTRIBUTOR_PROFILE = bmodel.labelsMasterHelper.applyLabels("distributor_profile");
                    tabLayout.addTab(tabLayout.newTab()
                            .setText(DISTRIBUTOR_PROFILE));
                } else {
                    DISTRIBUTOR_PROFILE = "Distributor";
                    tabLayout.addTab(tabLayout.newTab().setText(DISTRIBUTOR_PROFILE));
                }
            } catch (Exception e) {
                Commons.printException("Error while setting label for DISTRIBUTOR_PROFILE Tab", e);
            }
        }

        if (bmodel.configurationMasterHelper.SHOW_SBD_GAP_IN_PROFILE) {
            tabLayout.addTab(tabLayout.newTab().setText("SBD Gap"));
        }

        /*
         *
         * Show dynamic report based on Retailer
         * */
        dynamicReportTitle = bmodel.configurationMasterHelper.getDynamicReportTitle();

        if (bmodel.configurationMasterHelper.SHOW_SALES_VALUE_DR) {
            tabLayout.addTab(tabLayout.newTab().setText(dynamicReportTitle));
        }


        View root = tabLayout.getChildAt(0);
        if (root instanceof LinearLayout) {
            ((LinearLayout) root).setShowDividers(LinearLayout.SHOW_DIVIDER_MIDDLE);
            GradientDrawable drawable = new GradientDrawable();
            drawable.setColor(Color.parseColor("#80000000"));
            drawable.setSize(1, 1);
            ((LinearLayout) root).setDividerPadding(0);
            ((LinearLayout) root).setDividerDrawable(drawable);
        }

        tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);

        if (!is7InchTablet && tabLayout.getTabCount() > 3) {
            tabLayout.setTabMode(TabLayout.MODE_SCROLLABLE);
        }

        final ViewPagerAdapter adapter = new ViewPagerAdapter
                (this.getSupportFragmentManager(), tabLayout.getTabCount(), bundle);
        viewPager.setAdapter(adapter);
        viewPager.setOffscreenPageLimit(tabLayout.getTabCount());

        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if (tab.getPosition() == 0) {
                    if (fromHomeClick || non_visit) {
                        profileEditBtn.setVisibility(View.GONE);
                    } else {
                        if (bmodel.configurationMasterHelper.SHOW_PROFILE_EDIT) {
                            profileEditBtn.setVisibility(View.VISIBLE);
                        } else {
                            profileEditBtn.setVisibility(View.GONE);
                        }
                    }
                } else {
                    profileEditBtn.setVisibility(View.GONE);
                }
                viewPager.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }
        });

    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {

            case R.id.draw_routeimg_btn: {
                isdrawRoute = true;
                String uri = "http://maps.google.com/maps?saddr=" + lat + "," + lng + "(" + "Current " + ")&daddr=" + retailerLat + "," + retailerLng + " (" + retailerObj.getAddress1() + ")";
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
                intent.setPackage("com.google.android.apps.maps");
                try {
                    startActivity(intent);
                } catch (ActivityNotFoundException ex) {
                    try {
                        Intent unrestrictedIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(uri));
                        startActivity(unrestrictedIntent);
                    } catch (ActivityNotFoundException innerEx) {
                        Toast.makeText(this, "Please install a maps application", Toast.LENGTH_LONG).show();
                    }
                }

                // drawMapRoute();
                break;
            }
            case R.id.start_visit: {
                retailerClick();
                break;
            }
            case R.id.cancel_visit: {

                CommonReasonDialog comReasonDialog = new CommonReasonDialog(ProfileActivity.this, "nonVisit");
                comReasonDialog.setNonvisitListener(ProfileActivity.this);
                comReasonDialog.show();
                WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
                Window window = comReasonDialog.getWindow();
                lp.copyFrom(window != null ? window.getAttributes() : null);
                lp.width = displaymetrics.widthPixels - 100;
                lp.height = (int) (displaymetrics.heightPixels / 2);//WindowManager.LayoutParams.WRAP_CONTENT;
                if (window != null) {
                    window.setAttributes(lp);
                }
                break;
            }
            case R.id.profile_deviate: {

                CommonReasonDialog comReasonDialog = new CommonReasonDialog(ProfileActivity.this, "deviate");
                comReasonDialog.setNonvisitListener(ProfileActivity.this);
                comReasonDialog.show();
                WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
                Window window = comReasonDialog.getWindow();
                lp.copyFrom(window != null ? window.getAttributes() : null);
                lp.width = displaymetrics.widthPixels - 100;
                lp.height = (int) (displaymetrics.heightPixels / 2);//WindowManager.LayoutParams.WRAP_CONTENT;
                if (window != null) {
                    window.setAttributes(lp);
                }
                break;
            }
            case R.id.add_plane: {
                retailerClick();
                break;
            }
            case R.id.profile_edit_click: {
                Intent i = new Intent(ProfileActivity.this, ProfileEditActivity.class);
                startActivity(i);
                break;
            }
            case R.id.profile_mapswitch:
                mMap.setMapType((mMap.getMapType() == GoogleMap.MAP_TYPE_NORMAL) ? GoogleMap.MAP_TYPE_SATELLITE :
                        GoogleMap.MAP_TYPE_NORMAL);
                break;
            default:
                break;
        }

    }

    /**
     * Method used to hide or visible components, based on screens and configurations.
     */
    private void hideVisibleComponents() {

        if (fromHomeClick || non_visit) {
            profileEditBtn.setVisibility(View.GONE);
            drawRouteBtn.setVisibility(View.GONE);
        } else {
            if (bmodel.configurationMasterHelper.SHOW_PROFILE_EDIT) {
                profileEditBtn.setVisibility(View.VISIBLE);
            } else {
                profileEditBtn.setVisibility(View.GONE);
            }
            drawRouteBtn.setVisibility(View.GONE);
        }

        retailerObj = bmodel.getRetailerMasterBO();

        upArrow = ContextCompat.getDrawable(this, R.drawable.ic_home_arrow);
        upArrow.setColorFilter(ContextCompat.getColor(this, R.color.FullBlack), PorterDuff.Mode.SRC_ATOP);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setHomeAsUpIndicator(upArrow);
        }

        try {
            if (bmodel.retailerMasterBO.getRetailerName() != null) {
                int length = bmodel.retailerMasterBO.getRetailerName().indexOf("/");
                if (length == -1)
                    length = bmodel.retailerMasterBO.getRetailerName().length();
                title = bmodel.retailerMasterBO.getRetailerName().substring(0,
                        length);
            }
        } catch (Exception e) {
            Commons.printException(e);
        }
        retailerNameTxt.setText(title);

        if (retailerObj.getAddress3() != null && !retailerObj.getAddress3().isEmpty()) {
            retailerCodeTxt.setVisibility(View.VISIBLE);
            retailerCodeTxt.setText(retailerObj.getAddress3());
        } else {
            retailerCodeTxt.setVisibility(View.GONE);
        }

        CoordinatorLayout.LayoutParams params = (CoordinatorLayout.LayoutParams) appbar.getLayoutParams();
        AppBarLayout.Behavior behavior = new AppBarLayout.Behavior();
        behavior.setDragCallback(new AppBarLayout.Behavior.DragCallback() {
            @Override
            public boolean canDrag(@NonNull AppBarLayout appBarLayout) {
                return false;
            }
        });
        params.setBehavior(behavior);

        appbar.addOnOffsetChangedListener(new AppBarLayout.OnOffsetChangedListener() {

            @Override
            public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
                if (scrollRange == -1) {
                    scrollRange = appBarLayout.getTotalScrollRange();
                }
                if (scrollRange + verticalOffset == 0) {
                    retailerNameTxt.setVisibility(View.GONE);
                    retailerCodeTxt.setVisibility(View.GONE);
                    iconLinearLayout.setVisibility(View.GONE);
                    drawRouteBtn.setVisibility(View.GONE);
                    getSupportActionBar().setTitle(title);
                    collapsingToolbarLayout.setTitleEnabled(true);
                    getSupportActionBar().setDisplayShowTitleEnabled(true);
                    isVisible = true;
                } else if (isVisible) {
                    retailerNameTxt.setVisibility(View.VISIBLE);
                    if (retailerObj.getAddress3() != null && !retailerObj.getAddress3().isEmpty()) {
                        retailerCodeTxt.setVisibility(View.VISIBLE);
                        retailerCodeTxt.setText(retailerObj.getAddress3());
                    } else {
                        retailerCodeTxt.setVisibility(View.GONE);
                    }
                    iconLinearLayout.setVisibility(View.VISIBLE);
                    getSupportActionBar().setTitle("");
                    collapsingToolbarLayout.setTitleEnabled(false);
                    getSupportActionBar().setDisplayShowTitleEnabled(false);
                    isVisible = false;

                    if (retLatLng != null && curLatLng != null) {
                        if ((retLatLng.latitude != 0.0 && retLatLng.longitude != 0.0) && (curLatLng.latitude != 0.0 && curLatLng.longitude != 0.0)) {
                            drawRouteBtn.setVisibility(View.VISIBLE);
                        }
                    }
                }
            }
        });


    }


    /**
     * Method user to load map in store location.
     * <p>
     * param retLat
     * param retLng
     */
    private void loadStoreLocMapView(double retLat, double retLng) {

        try {
            LatLng storeLatLng;

            if (isValidLatLng(retLat, retLng)) {
                if (retLat != 0 && retLng != 0) {
                    storeLatLng = new LatLng(retLat, retLng);

                    if (markerList.size() > 1) {
                        markerList.clear();
                        mMap.clear();
                    }
                    //For the start location, the color of marker is GREEN and
                    //for the end location, the color of marker is RED.
                    markerList.add(storeLatLng);
                    MarkerOptions options = new MarkerOptions();
                    options.position(storeLatLng);// Setting the position of the marker
                    options.icon(BitmapDescriptorFactory.fromResource(getMarkerIcon(retailerObj)));

                    if (mMap != null) {
                        mMap.addMarker(options);
                    }

                    CameraPosition cameraPosition = new CameraPosition.Builder()
                            .target(new LatLng(storeLatLng.latitude, storeLatLng.longitude)).zoom(15f).build();

                    mMap.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

                    if (isdrawRoute) {
                        drawMapRoute();
                    }
                } else {
                    drawRouteBtn.setVisibility(View.GONE);
                }

                mMap.setOnMapLoadedCallback(new GoogleMap.OnMapLoadedCallback() {
                    public void onMapLoaded() {
                        mapProgressBar.setVisibility(View.GONE);
                    }
                });
            }
        } catch (Exception e) {
            Commons.printException(e);
        }

    }

    /**
     * Method used to validateData lat long values.
     * <p>
     * param lat
     * param lng
     */
    private boolean isValidLatLng(double lat, double lng) {
        if (lat < -90 || lat > 90) {
            return false;
        } else if (lng < -180 || lng > 180) {
            return false;
        }
        return true;
    }


    /**
     * Load Map view.
     * <p>
     * param retlatlng
     * param curlatlng
     */
    @SuppressLint("NewApi")
    private void loadMapView(final LatLng retlatlng, final LatLng curlatlng) {

        if (markerList.size() > 1) {
            markerList.clear();
            mMap.clear();
        }
        if ((retlatlng.latitude != 0.0 && retlatlng.longitude != 0.0) && (curlatlng.latitude != 0.0 && curlatlng.longitude != 0.0)) {
            // Creating MarkerOptions
            MarkerOptions options = new MarkerOptions();
            MarkerOptions options1 = new MarkerOptions();

            // Adding new item to the ArrayList
            markerList.add(retlatlng);
            markerList.add(curlatlng);

            // Setting the position of the marker
            options.position(markerList.get(0));
            options1.position(markerList.get(1));

            for (int i = 0; i < markerList.size(); i++) {
                if (i == 0) {
                    options.icon(BitmapDescriptorFactory.fromResource(getMarkerIcon(retailerObj)));//storelocation));
                } else if (i == 1) {
                    options1.icon(getBitmapDescriptor(R.drawable.user_loc));//(R.drawable.userlocation));
                }
            }
            // Add new marker to the Google Map Android API V2
            if (mMap != null) {
                mMap.addMarker(options);
                mMap.addMarker(options1);

                //the include method will calculate the min and max bound.
                LatLngBounds.Builder builder = new LatLngBounds.Builder();
                builder.include(options.getPosition());
                builder.include(options1.getPosition());
                LatLngBounds bounds = builder.build();

                int width = getResources().getDisplayMetrics().widthPixels;
                int height = getResources().getDisplayMetrics().heightPixels;
                int padding = (int) (width * 0.10); // offset from edges of the map 12% of screen

                if (firstLevZoom) {
                    CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, width - 500,
                            height - 500, padding);
                    mMap.moveCamera(cu);
                    mMap.animateCamera(cu);

                }
            }
        } else {
            LatLng latLng;

            MarkerOptions options = new MarkerOptions();

            if (retlatlng.latitude != 0.0 && retlatlng.longitude != 0.0) {
                latLng = retlatlng;
                options.icon(BitmapDescriptorFactory.fromResource(getMarkerIcon(retailerObj)));
            } else {
                latLng = curlatlng;
                options.icon(getBitmapDescriptor(R.drawable.user_loc));
            }

            // Adding new item to the ArrayList
            markerList.add(latLng);
            options.position(markerList.get(0));

            // Add new marker to the Google Map Android API V2
            if (mMap != null && curlatlng != null) {
                mMap.addMarker(options);

                //the include method will calculate the min and max bound.
                LatLngBounds.Builder builder = new LatLngBounds.Builder();
                builder.include(options.getPosition());
                LatLngBounds bounds = builder.build();

                int width = getResources().getDisplayMetrics().widthPixels;
                int height = getResources().getDisplayMetrics().heightPixels;
                int padding = (int) (width * 0.30); // offset from edges of the map 12% of screen

                if (firstLevZoom) {
                    CameraUpdate cu = CameraUpdateFactory.newLatLngBounds(bounds, width + 200, height + 200, padding);
                    mMap.moveCamera(cu);
                    mMap.animateCamera(cu);
                }
            }
        }

        if (isdrawRoute) {
            drawMapRoute();
        }

        mMap.setOnMapLoadedCallback(new GoogleMap.OnMapLoadedCallback() {
            public void onMapLoaded() {
                mapProgressBar.setVisibility(View.GONE);
                if ((retlatlng.latitude != 0.0 && retlatlng.longitude != 0.0) && (curlatlng.latitude != 0.0 && curlatlng.longitude != 0.0)) {
                    drawRouteBtn.setVisibility(View.VISIBLE);
                }
            }
        });

    }

    private BitmapDescriptor getBitmapDescriptor(int id) {
        //Mansoor - wating for vector icon
       /* if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            VectorDrawable vectorDrawable = (VectorDrawable) getDrawable(id);
            vectorDrawable.setColorFilter(new
                    PorterDuffColorFilter(getResources().getColor(R.color.colorAccent), PorterDuff.Mode.MULTIPLY));

            int h = (vectorDrawable.getIntrinsicHeight());/*//*70)/100;
            int w = (vectorDrawable.getIntrinsicWidth());/*//*70)/100;
            vectorDrawable.setBounds(0, 0, w, h);

            Bitmap bm = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bm);
            vectorDrawable.draw(canvas);

            return BitmapDescriptorFactory.fromBitmap(bm);

        } else {*/
        return BitmapDescriptorFactory.fromResource(id);
        //}
    }

    private String getDirectionsUrl(LatLng origin, LatLng dest) {

        String mapKey = "key=" + getString(R.string.google_maps_api_key);

        String str_origin = "origin=" + origin.latitude + "," + origin.longitude;
        String str_dest = "destination=" + dest.latitude + "," + dest.longitude; // Destination of route
        String sensor = "sensor=false";
        StringBuilder waypoints = new StringBuilder();

        for (int i = 2; i < markerList.size(); i++) {
            LatLng point = markerList.get(i);
            if (i == 2)
                waypoints = new StringBuilder("waypoints=");
            waypoints.append(point.latitude).append(",").append(point.longitude).append("|");
        }

        // Building the parameters to the web service
        String parameters = str_origin + "&" + str_dest + "&" + sensor + "&" + waypoints + "&" + mapKey;
        String output = "json";

        return "https://maps.googleapis.com/maps/api/directions/" + output + "?" + parameters;
    }


    /**
     * A method to download json data from url
     */
    @SuppressLint("LongLogTag")
    private String downloadUrl(String strUrl) {
        String data = "";
        InputStream iStream;
        HttpURLConnection urlConnection;
        try {
            // Creating an http connection to communicate with url
            URL url = new URL(strUrl);
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.connect();
            iStream = urlConnection.getInputStream();

            BufferedReader br = new BufferedReader(new InputStreamReader(iStream));
            StringBuilder sb = new StringBuilder();

            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }
            data = sb.toString();
            br.close();
            iStream.close();
            urlConnection.disconnect();
        } catch (Exception e) {
            Commons.printException("Exception while downloading url", e);
        }
        return data;
    }

    /**
     * Method used to call draw route.
     */
    private void drawMapRoute() {
        if (markerList.size() >= 2 && isdrawRoute) {
            LatLng origin = markerList.get(0);
            LatLng dest = markerList.get(1);

            // Getting URL to the Google Directions API
            String url = getDirectionsUrl(origin, dest);
            DownloadTask downloadTask = new DownloadTask();
            downloadTask.execute(url);
        }
    }


    // Fetches data from url passed
    private class DownloadTask extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... url) {

            String data = "";
            try {
                data = downloadUrl(url[0]);
            } catch (Exception e) {
                Commons.printException("Background Task", e);
            }
            return data;
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            ParserTask parserTask = new ParserTask();
            parserTask.execute(result);
        }
    }

    // load ActivityMenu
    private class loadActivityMenu extends AsyncTask<String, Void, String> {

        private Vector<ConfigureBO> menuDB;

        @Override
        protected String doInBackground(String... strings) {
            menuDB = bmodel.configurationMasterHelper.downloadNewActivityMenu(ConfigurationMasterHelper.MENU_ACTIVITY);
            return "Success";
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            try {

                for (int i = 0; i < menuDB.size(); i++) {
                    menuDB.get(i).setDone(false);
                }

                if (menuDB.get(0).getHasLink() == 0) {
                    menuDB.get(0).setDone(true);
                }

                for (int i = 0; i < menuDB.size(); i++) {
                    if (menuDB.get(i).getHasLink() == 1) {
                        if (menuDB.get(i).getConfigCode().equals("MENU_CALL_ANLYS")) {
                            if (bmodel.isModuleCompleted(menuDB.get(i).getConfigCode())) {
                                cancelVisitBtn.setVisibility(View.GONE);
                                break;
                            }
                        }
                    }
                }
            } catch (Exception e) {
                Commons.printException(e);
            }
        }
    }

    /**
     * load profile config and map related data
     */
    @SuppressLint("StaticFieldLeak")
    private class LoadProfileConfigs extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... url) {

            bmodel.configurationMasterHelper.downloadProfileModuleConfig();
            return "Success";


        }

        @Override
        protected void onPostExecute(String result) {
            //super.onPostExecute(result);
            Vector<ConfigureBO> profileConfig = bmodel.configurationMasterHelper.getProfileModuleConfig();
            for (ConfigureBO conBo : profileConfig) {
                if (conBo.getConfigCode().equals("PROFILE08") && conBo.isFlag() == 1) {
                    retailerLat = retailerObj.getLatitude();

                } else if (conBo.getConfigCode().equals("PROFILE31") && conBo.isFlag() == 1) {
                    retailerLng = retailerObj.getLongitude();
                } else if (conBo.getConfigCode().equals("PROFILE21") && conBo.isFlag() == 1) {
                    isNonVisitReason = true;
                }
            }

            if (fromHomeClick) {
                bottomView.setVisibility(View.GONE);
                linearLayout.setVisibility(View.GONE);
            } else if (visitClick) {
                deviateBtn.setVisibility(View.GONE);

                if (isNonVisitReason)
                    cancelVisitBtn.setVisibility(View.VISIBLE);
                startVisitBtn.setVisibility(View.VISIBLE);
            } else if (non_visit) {
                deviateBtn.setVisibility(View.VISIBLE);
                cancelVisitBtn.setVisibility(View.GONE);
                startVisitBtn.setVisibility(View.GONE);
            } else if (isFromPlanning || isFromPlanningSub) {
                addPlaneBtn.setVisibility(View.GONE);
                deviateBtn.setVisibility(View.GONE);
                cancelVisitBtn.setVisibility(View.GONE);
                startVisitBtn.setVisibility(View.GONE);
                bottomView.setVisibility(View.GONE);
            }

            isClicked = false;

            if (visitClick)
                getMapView();

            else if (fromHomeClick)
                loadStoreLocMapView(retailerLat, retailerLng);
        }
    }

    /**
     * A class to parse the Google Places in JSON format
     */
    private class ParserTask extends AsyncTask<String, Integer, List<List<HashMap<String, String>>>> {

        // Parsing the data in non-ui thread
        @Override
        protected List<List<HashMap<String, String>>> doInBackground(String... jsonData) {

            JSONObject jObject;
            List<List<HashMap<String, String>>> routes = new ArrayList<>();
            try {
                jObject = new JSONObject(jsonData[0]);
                DirectionsJSONParser parser = new DirectionsJSONParser();
                routes = parser.parse(jObject);
            } catch (Exception e) {
                e.printStackTrace();
            }
            return routes;
        }

        // Executes in UI thread, after the parsing process
        @Override
        protected void onPostExecute(List<List<HashMap<String, String>>> result) {

            ArrayList<LatLng> points;
            PolylineOptions lineOptions = null;

            // Traversing through all the routes\
            if (result != null) {
                for (int i = 0; i < result.size(); i++) {
                    points = new ArrayList<>();
                    lineOptions = new PolylineOptions();

                    // Fetching i-th route
                    List<HashMap<String, String>> path = result.get(i);
                    // Fetching all the points in i-th route
                    for (int j = 0; j < path.size(); j++) {
                        HashMap<String, String> point = path.get(j);

                        double lat = SDUtil.convertToDouble(point.get("lat"));
                        double lng = SDUtil.convertToDouble(point.get("lng"));
                        LatLng position = new LatLng(lat, lng);

                        points.add(position);
                    }
                    // Adding all the points in the route to LineOptions
                    lineOptions.addAll(points);
                    lineOptions.width(2);
                    lineOptions.color(Color.RED);
                }
                // Drawing polyline in the Google Map for the i-th route
                if (lineOptions != null) {
                    mMap.addPolyline(lineOptions);
                }
            }
        }
    }


    @Override
    public void onDrag(MotionEvent motionEvent) {
        if (motionEvent.getAction() == MotionEvent.ACTION_DOWN)
            firstLevZoom = false;
        if (motionEvent.getAction() == MotionEvent.ACTION_UP)
            firstLevZoom = false;
    }

    /**
     * TimerTask Memory leaks has been fixed
     * Removed Part-->  class MyTimerTask extends TimerTask from getMapView Method
     * Addred Code-->New Handler and Runnable has been added in the @onStart Method
     * Runnable has to remove when the activity is going  to stop
     */

    private void getMapView() {

        Commons.print("activity resume");
        if (bmodel.userMasterHelper.getUserMasterBO().getUserid() == 0) {
            Toast.makeText(this,
                    getResources().getString(R.string.sessionout_loginagain),
                    Toast.LENGTH_SHORT).show();
            finish();
        }
        if (bmodel.configurationMasterHelper.SHOW_CAPTURED_LOCATION) {
            int permissionStatus = ContextCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_FINE_LOCATION);
            if (permissionStatus == PackageManager.PERMISSION_GRANTED) {
                bmodel.locationUtil.startLocationListener();
            }
        } else {
            mapProgressBar.setVisibility(View.GONE);
        }

        if (bmodel.configurationMasterHelper.SHOW_GPS_ENABLE_DIALOG && isLatLong)
            if (!bmodel.locationUtil.isGPSProviderEnabled()) {
                Integer resultCode = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(this);
                if (resultCode == ConnectionResult.SUCCESS) {
                    bmodel.requestLocation(this);
                } else
                    onCreateDialogNew(1).show();
            }
    }

    /**
     * ViewPagerAdapter class used to call fragment dynamically.
     */
    public class ViewPagerAdapter extends FragmentPagerAdapter {

        private final int mNumOfTabs;
        private final Bundle bundleAdapter;

        ViewPagerAdapter(FragmentManager fm, int NumOfTabs, Bundle bundleAdapter) {
            super(fm);
            this.mNumOfTabs = NumOfTabs;
            this.bundleAdapter = bundleAdapter;
        }

        @Override
        public Fragment getItem(int position) {

            String tabName = tabLayout.getTabAt(position).getText().toString();
            if (tabName.equals(profile_title)) {
                ProfileFragment profileFragment = new ProfileFragment();
                profileFragment.setArguments(bundleAdapter);
                //profileFragment.onProfileFragemntListener(ProfileActivity.this);
                return profileFragment;
            } else if (tabName.equals(order_history_title)) {
                return new OrderHistoryFragment();
            } else if (tabName.equals(plan_outlet_title)) {
                return new PlanningOutletFragment();
            } else if (tabName.equals(retailer_kpi_title)) {
                DashBoardHelper dashBoardHelper = DashBoardHelper.getInstance(ProfileActivity.this);
                SellerDashboardFragment retailerKpiFragment = new SellerDashboardFragment();
                dashBoardHelper.checkDayAndP3MSpinner(true);
                dashBoardHelper.loadRetailerDashBoard(bmodel.getRetailerMasterBO().getRetailerID() + "", "MONTH");
                Bundle bnd = new Bundle();
                bnd.putString("screentitle", "");
                bnd.putBoolean("isFromHomeScreenTwo", true);
                bnd.putBoolean("isFromTab", true);
                bnd.putString("retid", bmodel.getRetailerMasterBO().getRetailerID());
                retailerKpiFragment.setArguments(bnd);
                return retailerKpiFragment;
            } else if (tabName.equals(msl_title)) {
                return new MSLUnsoldFragment();
            } else if (tabName.equals(invoice_history_title)) {
                return new InvoiceHistoryFragment();
            } else if (tabName.equals(ASSET_HISTORY)) {
                return new AssetHistoryFragment();
            } else if (tabName.equalsIgnoreCase(TASK)) {
                TaskFragment taskListFragment = new TaskFragment();
                Bundle args1 = new Bundle();
                args1.putBoolean(TaskConstant.RETAILER_WISE_TASK, true);
                args1.putBoolean(TaskConstant.FROM_PROFILE_SCREEN, true);
                taskListFragment.setArguments(args1);
                return taskListFragment;
            } else if (tabName.equalsIgnoreCase(SALES_PER_LEVEL)) {
                return new SalesPerCategory();
            } else if (tabName.equalsIgnoreCase(DISTRIBUTOR_PROFILE)) {
                return new DsitributorProfileFragment();
            } else if (tabName.equalsIgnoreCase("SBD Gap")) {
                return new SBDGapFragment();
            } else if (tabName.equals(retailer_contact_title)) {
                Bundle bundle = new Bundle();
                bundle.putString("RetailerId",bmodel.getAppDataProvider().getRetailMaster().getRetailerID());
                RetailerContactFragment retailerContactFragment = new RetailerContactFragment();
                retailerContactFragment.setArguments(bundle);
                return retailerContactFragment;
            } else if (dynamicReportTitle.equalsIgnoreCase(tabName)) {
                DynamicReportHelper.getInstance(ProfileActivity.this).downloadDynamicReport("MENU_DYN_RPT_RTR");
                DynamicReportFragment dynamicReportFragment = new DynamicReportFragment();
                Bundle bundle = new Bundle();
                bundle.putString("isFrom", "Profile");
                dynamicReportFragment.setArguments(bundle);
                return dynamicReportFragment;
            }
            return null;
        }

        @Override
        public int getCount() {
            return mNumOfTabs;
        }
    }

    @Override
    public void updaterProgressMsg(String msg) {
        super.updaterProgressMsg(msg);
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (mLocTimer != null) {
            mLocTimer.cancel();
            mLocTimer.purge();
        }

        // Raj - Location listener started in on create, but here started again to handle resuming from route screen..
        if (bmodel.configurationMasterHelper.SHOW_CAPTURED_LOCATION) {
            int permissionStatus = ContextCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_FINE_LOCATION);
            if (permissionStatus == PackageManager.PERMISSION_GRANTED) {
                bmodel.locationUtil.startLocationListener();
            }
        }

        if (bmodel.configurationMasterHelper.SHOW_CAPTURED_LOCATION
                && bmodel.configurationMasterHelper.IS_LOC_TIMER_ON && calledBy.equals(MENU_VISIT)) {
            mLocTimer = new Timer();
            timerTask = new LocationFetchTimer();
            mLocTimer.schedule(timerTask, 0, 1000);

        }

    }

    @Override
    protected void onStart() {
        super.onStart();
        mUserByRetailerID = bmodel.getUserByRetailerID();

        handler = new Handler();
        runnable = new Runnable() {
            public void run() {
                lat = LocationUtil.latitude;
                lng = LocationUtil.longitude;
                try {
                    Commons.print("lat:" + LocationUtil.latitude);
                    retLatLng = new LatLng(retailerLat, retailerLng);
                    curLatLng = new LatLng(lat, lng);
                    loadMapView(retLatLng, curLatLng);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                handler.postDelayed(this, 1000);
            }
        };
        handler.postDelayed(runnable, 1000);

    }

    @Override
    protected void onStop() {
        super.onStop();
        if (handler != null) {
            handler.removeCallbacks(runnable);
            handler = null;
            runnable = null;
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (visitClick) {
            if (bmodel.configurationMasterHelper.SHOW_CAPTURED_LOCATION) {
                int permissionStatus = ContextCompat.checkSelfPermission(this,
                        Manifest.permission.ACCESS_FINE_LOCATION);
                if (permissionStatus == PackageManager.PERMISSION_GRANTED)
                    bmodel.locationUtil.stopLocationListener();
            }
        }

    }


    @Override
    public void onBackPressed() {
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CAMERA_REQUEST_CODE) {
            if (resultCode == 1) {
                int count = bmodel.synchronizationHelper.getImageCountFromPath(getPhotoPath(), fnameStarts);
                Commons.print("ImageCount ," + count + "");
                if (count < bmodel.configurationMasterHelper.RETAILER_PHOTO_COUNT) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setMessage(String.format(getResources().getString(R.string.still_you_can), (bmodel.configurationMasterHelper.RETAILER_PHOTO_COUNT - count)));
                    builder.setCancelable(false);
                    builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {

                        public void onClick(DialogInterface dialog, int which) {

                            callCamera(getImageName());

                        }
                    });
                    builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            //loadHomeScreenTwo(bmodel.getRetailerMasterBO());
                            validationToStartVisit();
                        }
                    });
                    builder.show();
                } else {
                    //loadHomeScreenTwo(bmodel.getRetailerMasterBO());
                    validationToStartVisit();
                }
            } else if (resultCode == 0) {
                Toast.makeText(this, R.string.photo_mandatory, Toast.LENGTH_LONG).show();
            }

        } else if (requestCode == NFCManager.NFC_REQUEST_CODE) {
            if (resultCode == NFCManager.NFC_CODE_MATCHED || resultCode == NFCManager.NFC_CODE_SELECTING_REASON) {
                mVisitMode = data.getStringExtra("VisitMode");
                mNFCReasonId = data.getStringExtra("NFCReasonId");
                mNFCValidationPassed = true;
                validationToStartVisit();
            }
        }
    }

    @Override
    public void updateNearByRetailer(Vector<RetailerMasterBO> list) {
        ProfileFragment mProfileFragment;
        if (getSupportFragmentManager().findFragmentById(R.id.viewpager) instanceof ProfileFragment) {
            mProfileFragment = (ProfileFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.viewpager);
            mProfileFragment.updateNearByRetailer(list);
        }
    }


    private void retailerClick() {

        if (!isClicked && calledBy.equals(MENU_VISIT)) {

            if (bmodel.configurationMasterHelper.IS_RETAILER_PHOTO_NEEDED) {
                takePhotoForRetailer();
            } else
                validationToStartVisit();
        }
    }

    private void validationToStartVisit() {

        if(bmodel.configurationMasterHelper.IS_ENABLE_TRIP) {
            if (!LoadManagementHelper.getInstance(getApplicationContext()).isTripStarted(this)) {
                Toast.makeText(this, getResources().getString(R.string.pls_start_the_trip), Toast.LENGTH_LONG).show();
                return;
            }

            if (!LoadManagementHelper.getInstance(getApplicationContext()).isAllMandatoryPlanningSubModulesCompleted(this)) {
                Toast.makeText(this, getResources().getString(R.string.pls_complete_all_mandatory_modules_of_start_day), Toast.LENGTH_LONG).show();
                return;
            }
        }


        // Downloaded date vs Mobile Date validation.
        if ((DateTimeUtils.compareDate(bmodel.userMasterHelper.getUserMasterBO()
                .getDownloadDate(), DateTimeUtils.now(DateTimeUtils.DATE_GLOBAL), "yyyy/MM/dd") > 0)
                && bmodel.configurationMasterHelper.IS_DATE_VALIDATION_REQUIRED) {
            Toast.makeText(this,
                    getResources().getString(R.string.next_day_coverage),
                    Toast.LENGTH_SHORT).show();
            return;

        }

        // ODA Meter start journey validation.
        if (bmodel.configurationMasterHelper.SHOW_RETAILER_SELECTION_VALID) {
            if (!bmodel.configurationMasterHelper.isOdaMeterOn()
                    || !bmodel.startjourneyclicked) {
                bmodel.showAlert(
                        getResources()
                                .getString(
                                        R.string.odometerjourneynotstarted),
                        0);
                return;
            }
        }

        // Force user to enable GPS and set location accuracy as High.
        if (bmodel.configurationMasterHelper.SHOW_GPS_ENABLE_DIALOG) {
            if (!bmodel.locationUtil.isGPSProviderEnabled()) {

                Integer resultCode = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(this);
                if (resultCode == ConnectionResult.SUCCESS)
                    bmodel.requestLocation(this);
                else
                    onCreateDialogNew(2);

                return;
            }

            if (!LocationServiceHelper.getInstance().isLocationHighAccuracyEnabled(this)) {
                onCreateDialogNew(3);
                return;
            }
        }

        //Allow user to wait for specific time if location capturing is mandatory.
        if (bmodel.configurationMasterHelper.SHOW_CAPTURED_LOCATION
                && bmodel.configurationMasterHelper.IS_LOC_TIMER_ON) {

            if ((LocationUtil.latitude == 0 && LocationUtil.longitude == 0)) {
                if (timerTask != null && timerTask.isRunning) {

                    AlertDialog.Builder builder = new AlertDialog.Builder(ProfileActivity.this);
                    customProgressDialog(builder, getResources().getString(R.string.fetching_location));
                    mLocationAlertDialog = builder.create();
                    mLocationAlertDialog.setButton(DialogInterface.BUTTON_NEGATIVE, getResources().getString(R.string.dismiss), new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            mLocationAlertDialog.dismiss();
                        }
                    });
                    mLocationAlertDialog.show();
                    return;
                } else {
                    Toast.makeText(this, getResources().getString(R.string.location_not_captured), Toast.LENGTH_LONG).show();
                }
            }
        }


        // Restrict user to start visit if mock location provider is set.
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR2) {
            if (isMockSettingsON()) {
                showMocLocationAlert();
                return;
            }
        } else {
            if (LocationUtil.isMockLocation) {
                showMocLocationAlert();
                return;
            }
        }


        // Retailer sequence skip validation.
        if (bmodel.configurationMasterHelper.SHOW_RET_SKIP_VALIDATION
                && (bmodel.retailerMasterBO.getIsDeviated() != null && !bmodel.retailerMasterBO.getIsDeviated().equalsIgnoreCase("Y"))
                && bmodel.getVisitretailerMaster().size() > 0) {
            if (!validateSequenceSkip(bmodel.getRetailerMasterBO()))
                return;
        }

        // Retailer and User location validation using radius
        if (bmodel.configurationMasterHelper.SHOW_LOCATION_PASSWORD_DIALOG && !isLocValDone) {
            if (!checkUserIsNearByRetailer(bmodel.getRetailerMasterBO()))
                return;
        }


        // NFC Validation
        if (bmodel.configurationMasterHelper.SHOW_NFC_VALIDATION_FOR_RETAILER && !mNFCValidationPassed) {
            Intent intent = new Intent(this, NFCReadDialogActivity.class);
            intent.putExtra("nfcvalue", bmodel.getRetailerMasterBO().getNFCTagId().replaceAll(":", ""));
            startActivityForResult(intent, NFCManager.NFC_REQUEST_CODE);
            return;
        }

        // Diageo Project specific validation
        if (bmodel.configurationMasterHelper.SHOW_RETAILER_VISIT_CONFIRMATION && !mLocationConfirmationPassed) {
            confirmAtRetailerLocation();
            return;
        }


        if (bmodel.configurationMasterHelper.IS_APPLY_DISTRIBUTOR_WISE_PRICE) {

            ArrayList<SupplierMasterBO> mSupplierList = bmodel.downloadSupplierDetails();
            if (mSupplierList != null && mSupplierList.size() == 1) {
                SupplierMasterBO supplierBo = mSupplierList.get(0);
                bmodel.getRetailerMasterBO().setDistributorId(supplierBo.getSupplierID());
                bmodel.getRetailerMasterBO().setDistParentId(supplierBo.getDistParentID());
                bmodel.getRetailerMasterBO().setSupplierTaxLocId(supplierBo.getSupplierTaxLocId());
                bmodel.getRetailerMasterBO().setRpTypeCode(supplierBo.getRpTypeCode());
                bmodel.updatePriceGroupId(true);
                showMessage(getString(R.string.distributor_name) + " "
                        + getString(R.string.selected) + " "
                        + mSupplierList.get(0).getSupplierName());
                loadHomeScreenTwo(bmodel.getRetailerMasterBO());
                return;
            } else {

                mSupplierAdapter = new ArrayAdapter<>(this,
                        android.R.layout.select_dialog_singlechoice, mSupplierList);

                String dialog_title;
                if ((bmodel.labelsMasterHelper.applyLabels("select_dist") != null) &&
                        (bmodel.labelsMasterHelper.applyLabels("select_dist").length() > 0)) {
                    dialog_title = bmodel.labelsMasterHelper.applyLabels("select_dist");
                } else {
                    dialog_title = getResources().getString(R.string.select_distributor);
                }

                AlertDialog.Builder builder = new AlertDialog.Builder(ProfileActivity.this)
                        .setIcon(null)
                        .setCancelable(false)
                        .setTitle(dialog_title)

                        .setSingleChoiceItems(mSupplierAdapter,
                                0,
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog,
                                                        int which) {

                                        SupplierMasterBO supplierBo = mSupplierAdapter
                                                .getItem(which);
                                        bmodel.getRetailerMasterBO().setDistributorId(supplierBo.getSupplierID());
                                        bmodel.getRetailerMasterBO().setDistParentId(supplierBo.getDistParentID());
                                        bmodel.getRetailerMasterBO().setSupplierTaxLocId(supplierBo.getSupplierTaxLocId());
                                        bmodel.getRetailerMasterBO().setRpTypeCode(supplierBo.getRpTypeCode());
                                        bmodel.updatePriceGroupId(true);

                                        dialog.dismiss();

                                        loadHomeScreenTwo(bmodel.getRetailerMasterBO());


                                    }
                                });
                bmodel.applyAlertDialogTheme(builder);

                return;
            }
        }


        // If all valiation are success.
        loadHomeScreenTwo(bmodel.getRetailerMasterBO());

    }

    private boolean isMockSettingsON() {
        // returns true if mock location enabled, false if not enabled.
        return !Settings.Secure.getString(getContentResolver(),
                Settings.Secure.ALLOW_MOCK_LOCATION).equals("0");
    }



    private void showMocLocationAlert() {
        CommonDialog dialog = new CommonDialog(getApplicationContext(), this, "", getResources().getString(R.string.mock_location_enabled), false,
                getResources().getString(R.string.log_out), new CommonDialog.PositiveClickListener() {
            @Override
            public void onPositiveButtonClick() {

                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.KITKAT) {
                    ActivityCompat.finishAffinity(ProfileActivity.this);
                } else {
                    finishAffinity();
                }
            }
        });
        dialog.show();
        dialog.setCancelable(false);
    }

    private void loadHomeScreenTwo(RetailerMasterBO ret) {

        // Time count Starts for the retailer
        if (bmodel.configurationMasterHelper.isAuditEnabled()) {
            bmodel.setRetailerMasterBO(ret);

            ArrayList<UserMasterBO> mUserList = mUserByRetailerID.get(ret
                    .getRetailerID());
            if (mUserList != null) {
                UserSelectionDialogue userSelectionDialogue = new UserSelectionDialogue(
                        this, mUserList,
                        new UserDialogInterface() {

                            @Override
                            public void updateValue() {
                                if (bmodel.isAlreadyDownloadUser()) {
                                    if (bmodel
                                            .getRetailerMasterBO()
                                            .getRetailerID()
                                            .equals(BusinessModel.selectedDownloadRetailerID)
                                            && bmodel
                                            .getRetailerMasterBO()
                                            .getSelectedUserID() == BusinessModel.selectedDownloadUserID) {
                                        onCreateDialogNew(0).show();
                                    } else {
                                        onCreateDialogNew(1).show();
                                    }

                                } else {

                                    new DownloadSupervisorData().execute();
                                }
                            }
                        });
                userSelectionDialogue.setCancelable(false);
                userSelectionDialogue.show();
                isClicked = false;

            } else {

                Toast.makeText(
                        this,
                        getResources().getString(R.string.Users_not_available),
                        Toast.LENGTH_SHORT).show();
            }

        } else {

            if (bmodel.timer == null && !bmodel.configurationMasterHelper.IS_DISABLE_CALL_ANALYSIS_TIMER) {
                if ("P".equals(bmodel.getAppDataProvider().getRetailMaster().getIsVisited())) {
                    long pausedTime = getSharedPreferences("RetailerPause", MODE_PRIVATE).getLong("pausetime", 0);
                    bmodel.timer = new TimerCount(ProfileActivity.this, pausedTime);
                } else
                    bmodel.timer = new TimerCount(ProfileActivity.this, 0);
            }
            isClicked = true;
            // Set the select retailer Obj in bmodel
            bmodel.setRetailerMasterBO(ret);

            downloadProductsAndPrice = new DownloadProductsAndPrice(ProfileActivity.this, getPhotoPath(), fnameStarts,
                    mVisitMode, mNFCReasonId, true);
            if (downloadProductsAndPrice.getStatus() != AsyncTask.Status.RUNNING)
                downloadProductsAndPrice.execute();

        }
    }


    private Dialog onCreateDialogNew(int id) {
        switch (id) {
            case 0:
                return new AlertDialog.Builder(this)
                        .setIcon(null)
                        .setCancelable(false)
                        .setTitle(
                                getResources()
                                        .getString(
                                                R.string.do_u_want_directly_go_or_redownload))

                        .setPositiveButton(getResources().getString(R.string.open),
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog,
                                                        int whichButton) {
                                        // new DownloadProductsAndPrice().execute();
                                        downloadProductsAndPrice = new DownloadProductsAndPrice(ProfileActivity.this, getPhotoPath(), fnameStarts,
                                                mVisitMode, mNFCReasonId, true);
                                        downloadProductsAndPrice.execute();

                                    }
                                })
                        .setNegativeButton(
                                getResources().getString(R.string.redownload),
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog,
                                                        int whichButton) {

                                        new DownloadSupervisorData().execute();
                                    }
                                }).create();

            case 1:
                return new AlertDialog.Builder(this)
                        .setIcon(null)
                        .setCancelable(false)
                        .setTitle(
                                getResources()
                                        .getString(
                                                R.string.do_u_want_delete_and_redownload_or_cancel))

                        .setPositiveButton(
                                getResources().getString(R.string.cancel),
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog,
                                                        int whichButton) {

                                    }
                                })
                        .setNegativeButton(
                                getResources().getString(
                                        R.string.delete_and_redownload),
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog,
                                                        int whichButton) {

                                        new DownloadSupervisorData().execute();
                                    }
                                }).create();
            case 2:
                AlertDialog.Builder builderGPS = new AlertDialog.Builder(this)
                        .setIcon(null)
                        .setTitle(getResources().getString(R.string.enable_gps))
                        .setPositiveButton(getResources().getString(R.string.ok),
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog,
                                                        int whichButton) {
                                        Intent myIntent = new Intent(
                                                android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                                        startActivity(myIntent);

                                    }
                                });
                bmodel.applyAlertDialogTheme(builderGPS);
                return builderGPS.create();

            case 3:
                AlertDialog.Builder highAccuracy = new AlertDialog.Builder(this)
                        .setIcon(null)
                        .setTitle(getResources().getString(R.string.status_location_accuracy))
                        .setPositiveButton(getResources().getString(R.string.ok),
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog,
                                                        int whichButton) {
                                        Intent myIntent = new Intent(
                                                android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                                        startActivity(myIntent);

                                    }
                                });
                bmodel.applyAlertDialogTheme(highAccuracy);
                return highAccuracy.create();

        }
        return null;
    }


    private void takePhotoForRetailer() {
        dateTimeStampForId = DateTimeUtils.now(DateTimeUtils.DATE_TIME_ID);
        bmodel.outletTimeStampHelper.setUid(bmodel.QT("OTS" + dateTimeStampForId));

        if (bmodel.synchronizationHelper
                .isExternalStorageAvailable()) {
            callCamera(getImageName());
        } else {
            Toast.makeText(this,
                    getResources().getString(R.string.external_storage_not_available),
                    Toast.LENGTH_SHORT).show();
            finish();
        }
    }


    /**
     * get photo path.
     *
     * @return path
     */
    private String getPhotoPath() {
        return this.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
                + "/"
                + DataMembers.photoFolderName
                + "/";
    }

    /**
     * Generate photo capture path.
     * photoPath and fnameSatarts should be global to access from save method.
     *
     * @return imagefileName
     */
    private String getImageName() {
        fnameStarts = "RT_" + bmodel.getRetailerMasterBO().getRetailerID() + "_" + dateTimeStampForId;
        return fnameStarts + "_"
                + (bmodel.synchronizationHelper.getImageCountFromPath(getPhotoPath(), fnameStarts) + 1)
                + "_img.jpg";
    }

    private void callCamera(String imageName) {
        Intent intent = new Intent(this, CameraActivity.class);
        intent.putExtra(CameraActivity.QUALITY, 40);
        String mPath = getPhotoPath() + "/" + imageName;
        intent.putExtra(CameraActivity.PATH, mPath);
        startActivityForResult(intent, CAMERA_REQUEST_CODE);
    }


    private boolean validateSequenceSkip(RetailerMasterBO ret) {
        if (!getPreviousRetailerVisitedStatus(ret)) {
            if (ret.getSkipActivatedDate() == null
                    || !ret.getSkipActivatedDate().equals(DateTimeUtils.now(DateTimeUtils.DATE_GLOBAL))) {
                if (bmodel.configurationMasterHelper.ret_skip_flag == 1
                        || bmodel.configurationMasterHelper.ret_skip_flag == 2) {

                    if (retSeqSkipDialog != null && retSeqSkipDialog.isShowing()) {
                        retSeqSkipDialog.cancel();
                        retSeqSkipDialog = null;
                    }
                    retSeqSkipDialog = new RetailerSequenceSkipDialog(this,
                            otpPasswordDismissListenerNew, ret, bmodel.configurationMasterHelper.ret_skip_flag == 1 ? getResources()
                            .getString(R.string.enter_otp_skip_seq)
                            : getResources().getString(
                            R.string.select_reason_skip_seq));
                    retSeqSkipDialog.show();
                } else {
                    Toast.makeText(
                            this,
                            getResources().getString(
                                    R.string.cant_skip_go_by_sequence),
                            Toast.LENGTH_SHORT).show();
                }
                return false;
            }
        }
        return true;
    }

    private boolean getPreviousRetailerVisitedStatus(RetailerMasterBO retailerBO) {

        try {
            ArrayList<RetailerMasterBO> retailerVisit = bmodel.getVisitretailerMaster();
            String retailerId = retailerBO.getRetailerID();
            for (int i = 0; i < retailerVisit.size(); i++) {
                if (retailerVisit.get(i).getRetailerID().equals(retailerId)) {
                    return ("Y").equals(retailerVisit.get(i - 1).getIsVisited()) && (!retailerVisit.get(i - 1).isSkip() || getPreviousRetailerVisitedStatus(retailerVisit.get(i - 1)));
                }
            }
        } catch (Exception e) {
            Commons.printException("" + e);
            return true;
        }
        return false;
    }

    private void confirmAtRetailerLocation() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
        alertDialog.setTitle(getResources().getString(R.string.app_name));
        alertDialog.setMessage(getResources().getString(R.string.pleaseconfirm) + " " + bmodel.getRetailerMasterBO().getRetailerName() + " " + getResources().getString(R.string.location));
        alertDialog.setPositiveButton(getResources().getString(R.string.yes), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                mLocationConfirmationPassed = true;
                validationToStartVisit();
            }
        });

        alertDialog.setNegativeButton(getResources().getString(R.string.no), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();

            }
        });
        bmodel.applyAlertDialogTheme(alertDialog);
    }

    private boolean checkUserIsNearByRetailer(RetailerMasterBO ret) {
        try {
            if (ret.getOtpActivatedDate() == null
                    || !ret.getOtpActivatedDate().equals(
                    DateTimeUtils.now(DateTimeUtils.DATE_GLOBAL))) {
                if (ret.getLatitude() == 0 && ret.getLongitude() == 0) {
                    showToastMessage(ret, -1);
                    return false;
                } else if (LocationUtil.latitude == 0 && LocationUtil.longitude == 0) {
                    showToastMessage(ret, -2);
                    return false;
                } else {
                    float distance = LocationUtil.calculateDistance(
                            ret.getLatitude(), ret.getLongitude());
                    if (distance > ret.getGpsDistance()) {
                        showToastMessage(ret, distance);
                        return false;
                    }
                }
            }
            return true;
        } catch (Exception e) {
            Commons.printException("Check IsAllowed :", e);
            return false;
        }
    }

    /*public boolean isClicked() {
        return isClicked;
    }*/

    /*public void setClicked(boolean isClicked) {
        this.isClicked = isClicked;
    }*/


    private void showToastMessage(RetailerMasterBO ret, float distance) {
        if (bmodel.configurationMasterHelper.ret_skip_otp_flag == 0 || bmodel.configurationMasterHelper.ret_skip_otp_flag == 1) {
            String strTitle;
            if (distance == -1)
                strTitle = getResources().getString(
                        R.string.retailer_location_not_assigned);
            else if (distance == -2)
                strTitle = getResources().getString(
                        R.string.not_able_to_find_user_location);
            else
                strTitle = getResources().getString(R.string.need_otp)
                        + getResources().getString(R.string.or_you_are) + " "
                        + distance + getResources().getString(R.string.mts_away);

            if (otpValidationDialog != null && otpValidationDialog.isShowing()) {
                otpValidationDialog.cancel();
                otpValidationDialog = null;
            }
            otpValidationDialog = new OTPValidationDialog(this,
                    otpPasswordDismissListenerNew, ret, strTitle, (int) distance);
            otpValidationDialog.show();
        } else if (bmodel.configurationMasterHelper.ret_skip_otp_flag == 2) {
            CommonDialog commonDialog1 = new CommonDialog(this,
                    getResources().getString(R.string.location_validation),
                    getResources().getString(R.string.location_mismatch_msg),
                    getResources().getString(R.string.bt_proceed),
                    new CommonDialog.PositiveClickListener() {
                        @Override
                        public void onPositiveButtonClick() {
                            OTPValidationHelper otpValidationHelper = OTPValidationHelper.getInstance(ProfileActivity.this);
                            String status = otpValidationHelper.saveOTPSkipReason(0, ret, "ALERT", (int) distance);
                            if ("1".equals(status)) {
                                isLocValDone = true;
                                validationToStartVisit();
                            }

                        }
                    });

            commonDialog1.show();
            commonDialog1.setCancelable(false);
        } else if (bmodel.configurationMasterHelper.ret_skip_otp_flag == 3) {
            CommonDialog commonDialog1 = new CommonDialog(this,
                    getResources().getString(R.string.location_validation),
                    getResources().getString(R.string.location_mismatch_msg),
                    getResources().getString(R.string.ok));
            commonDialog1.show();
            commonDialog1.setCancelable(false);
        }

    }

    class DownloadSupervisorData extends AsyncTask<Integer, Integer, Boolean> {
        private AlertDialog.Builder builder;

        @Override
        protected Boolean doInBackground(Integer... params) {
            try {

                selectedUserId = bmodel.retailerMasterBO.getSelectedUserID() + "";

                String loginId = bmodel.synchronizationHelper.
                        getSelectedUserLoginId(bmodel.retailerMasterBO.getSelectedUserID() + "", ProfileActivity.this);
                bmodel.synchronizationHelper.updateAuthenticateTokenWithoutPassword(loginId);

                bmodel.synchronizationHelper.downloadUserRetailerTranUrl();
                return Boolean.TRUE;
            } catch (Exception e) {
                Commons.printException("" + e);
                return Boolean.FALSE;
            }
        }

        protected void onPreExecute() {
            builder = new AlertDialog.Builder(ProfileActivity.this);
            customProgressDialog(builder, getResources().getString(R.string.loading));
            alertDialog = builder.create();
            alertDialog.show();
        }

        protected void onProgressUpdate(Integer... progress) {
        }

        protected void onPostExecute(Boolean result) {
            if (bmodel.synchronizationHelper.getAuthErroCode().equals(SynchronizationHelper.AUTHENTICATION_SUCCESS_CODE)) {
                ArrayList<String> userRetailerTranUrlList = bmodel.synchronizationHelper.getUserRetailerTranDownloadurlList();
                if (!userRetailerTranUrlList.isEmpty()) {
                    bmodel.synchronizationHelper.downloadUserRetailerTranFromUrl(bmodel
                            .getRetailerMasterBO()
                            .getRetailerID());
                } else {
                    alertDialog.dismiss();
                    Toast.makeText(ProfileActivity.this, getResources().getString(R.string.retailer_trans_not_exist), Toast.LENGTH_SHORT).show();
                    downloadProductsAndPrice = new DownloadProductsAndPrice(ProfileActivity.this, getPhotoPath(), fnameStarts,
                            mVisitMode, mNFCReasonId, true);
                    if (downloadProductsAndPrice.getStatus() != AsyncTask.Status.RUNNING)
                        downloadProductsAndPrice.execute();
                }
            } else {
                String errorMsg = bmodel.synchronizationHelper.getErrormessageByErrorCode().get(bmodel.synchronizationHelper.getAuthErroCode());
                if (errorMsg != null) {
                    Toast.makeText(ProfileActivity.this, errorMsg, Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(ProfileActivity.this, getResources().getString(R.string.data_not_downloaded), Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    private float calculateDistanceBetweenRetailers() {
        float notValidLocation = -1;

        if (bmodel.outletTimeStampHelper.getLastRetailerLattitude() != 0 && bmodel.outletTimeStampHelper.getLastRetailerLongitude() != 0 && LocationUtil.latitude != 0 && LocationUtil.longitude != 0) {
            return LocationUtil.calculateDistance(bmodel.outletTimeStampHelper.getLastRetailerLattitude(), bmodel.outletTimeStampHelper.getLastRetailerLongitude());
        }
        return notValidLocation;

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            if (mLocTimer != null) {
                mLocTimer.cancel();
            }

            if (fromHomeClick || non_visit) {
                finish();
            } else {
                if (!visitClick && !isFromPlanning && !isFromPlanningSub) {
                    startActivity(new Intent(ProfileActivity.this,
                            HomeScreenActivity.class).putExtra("menuCode", "MENU_VISIT"));
                } else if (isFromPlanning) {
                    startActivity(new Intent(ProfileActivity.this,
                            HomeScreenActivity.class).putExtra("menuCode", "MENU_PLANNING"));

                }
                finish();
            }
            overridePendingTransition(R.anim.trans_right_in, R.anim.trans_right_out);
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void addReatailerReason() {
        showAlert(getResources().getString(
                R.string.saved_successfully));

    }

    @Override
    public void onDismiss() {

    }

    private void showAlert(String msg) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(msg);

        builder.setPositiveButton(getResources().getString(R.string.ok), new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int which) {
                //  updateCancel();
                if (calledBy.equalsIgnoreCase(MENU_VISIT)) {
                    Intent i = new Intent(ProfileActivity.this, HomeScreenActivity.class);
                    if (fromMap)
                        i.putExtra("menuCode", "MENU_PLANE_MAP");
                    else
                    i.putExtra("menuCode", "MENU_VISIT");
                    startActivity(i);
                    finish();
                } else if (calledBy.equalsIgnoreCase(MENU_PLANNING)) {
                    Intent i = new Intent(ProfileActivity.this, PlanningVisitActivity.class);
                    i.putExtra("isPlanning", true);
                    startActivity(i);
                    finish();
                } else if (calledBy.equalsIgnoreCase(MENU_PLANNING_SUB)) {
                    Intent i = new Intent(ProfileActivity.this, PlanningVisitActivity.class);
                    i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    i.putExtra("isPlanningSub", true);
                    startActivity(i);
                    finish();
                }

                overridePendingTransition(R.anim.trans_right_in, R.anim.trans_right_out);
            }
        });
        bmodel.applyAlertDialogTheme(builder);
    }

    private void registerReceiver() {
        IntentFilter intentFilter = new IntentFilter(
                UserRetailerTransactionReceiver.PROCESS_RESPONSE);
        intentFilter.addCategory(Intent.CATEGORY_DEFAULT);
        this.receiver = new UserRetailerTransactionReceiver();
        registerReceiver(receiver, intentFilter);
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (bmodel.configurationMasterHelper.isAuditEnabled()) {
            unregisterReceiver(receiver);
        }

        if (downloadProductsAndPrice != null && downloadProductsAndPrice.getStatus() == AsyncTask.Status.RUNNING)
            downloadProductsAndPrice.cancel(true);

    }

    public class UserRetailerTransactionReceiver extends BroadcastReceiver {
        public static final String PROCESS_RESPONSE = "com.ivy.intent.action.VisitFragment";

        @Override
        public void onReceive(Context arg0, Intent arg1) {
            updateReceiver(arg1);
        }
    }

    private void updateReceiver(Intent intent) {
        Bundle bundle = intent.getExtras();
        int method = bundle.getInt(SynchronizationHelper.SYNXC_STATUS, 0);
        String errorCode = bundle.getString(SynchronizationHelper.ERROR_CODE);

        switch (method) {
            case SynchronizationHelper.USER_RETAILER_TRAN_DOWNLOAD_INSERT:
                if (errorCode != null && errorCode
                        .equals(SynchronizationHelper.AUTHENTICATION_SUCCESS_CODE)) {
                    bmodel.synchronizationHelper
                            .downloadFinishUpdate(SynchronizationHelper.FROM_SCREEN.VISIT_SCREEN, SynchronizationHelper.DOWNLOAD_FINISH_UPDATE, selectedUserId);
                    selectedUserId = "";
                } else {
                    String errorDownlodCode = bundle
                            .getString(SynchronizationHelper.ERROR_CODE);
                    String errorDownloadMessage = bmodel.synchronizationHelper
                            .getErrormessageByErrorCode().get(errorDownlodCode);
                    if (errorDownloadMessage != null) {
                        Toast.makeText(ProfileActivity.this, errorDownloadMessage,
                                Toast.LENGTH_LONG).show();
                    }

                    alertDialog.dismiss();

                }
                break;
            case SynchronizationHelper.DOWNLOAD_FINISH_UPDATE:
                alertDialog.dismiss();

                if (bmodel.timer == null && !bmodel.configurationMasterHelper.IS_DISABLE_CALL_ANALYSIS_TIMER) {
                    if ("P".equals(bmodel.getAppDataProvider().getRetailMaster().getIsVisited())) {
                        long pausedTime = getSharedPreferences("RetailerPause", MODE_PRIVATE).getLong("pausetime", 0);
                        bmodel.timer = new TimerCount(ProfileActivity.this, pausedTime);
                    } else
                    bmodel.timer = new TimerCount(ProfileActivity.this, 0);
                }
                isClicked = false;

                bmodel.updateUserAudit(1);
                downloadProductsAndPrice = new DownloadProductsAndPrice(ProfileActivity.this, getPhotoPath(), fnameStarts,
                        mVisitMode, mNFCReasonId, true);
                downloadProductsAndPrice.execute();
                // new DownloadProductsAndPrice().execute();
                break;
            default:
                break;
        }

    }

    class LocationFetchTimer extends TimerTask {

        private int count = bmodel.configurationMasterHelper.LOCATION_TIMER_PERIOD;
        public boolean isRunning = true;

        @Override
        public void run() {

            runOnUiThread(new Runnable() {

                @Override
                public void run() {
                    try {
                        count -= 1;
                        if (count <= 0) {

                            if (mLocTimer != null) {
                                mLocTimer.cancel();
                                isRunning = false;
                            }
                            if (mLocationAlertDialog != null && mLocationAlertDialog.isShowing()) {
                                mLocationAlertDialog.dismiss();
                                validationToStartVisit();
                            }

                            if (LocationUtil.latitude == 0 && LocationUtil.longitude == 0) {
                                Toast.makeText(ProfileActivity.this, getResources().getString(R.string.location_not_captured), Toast.LENGTH_LONG).show();
                            }

                        } else {

                            if (LocationUtil.latitude != 0 && LocationUtil.longitude != 0) {
                                if (mLocTimer != null) {
                                    mLocTimer.cancel();
                                    isRunning = false;
                                }
                                if (mLocationAlertDialog != null && mLocationAlertDialog.isShowing()) {
                                    mLocationAlertDialog.dismiss();
                                    validationToStartVisit();
                                }
                                Toast.makeText(ProfileActivity.this, getResources().getString(R.string.location_captured), Toast.LENGTH_LONG).show();

                            } else {
                                if (mLocationAlertDialog != null && mLocationAlertDialog.isShowing()) {
                                    //  updaterProgressMsg(String.format(getResources().getString(R.string.fetching_location_pls_wait),count));
                                    updaterProgressMsg(getResources().getQuantityString(R.plurals.fetching_location, count, count));
                                }
                            }
                        }

                    } catch (Exception ex) {
                        Commons.printException(ex);
                    }
                }
            });
        }

    }

    private int getMarkerIcon(RetailerMasterBO retailerMasterBO) {
        int drawable = R.drawable.marker_visit_unscheduled;

        if ("Y".equals(retailerMasterBO.getIsVisited())) {
            if (("N").equals(retailerMasterBO.isOrdered()))
                drawable = R.drawable.marker_visit_non_productive;
            else
                drawable = R.drawable.marker_visit_completed;
        } else if (retailerMasterBO.getIsToday() == 1 || "Y".equals(retailerMasterBO.getIsDeviated()))
            drawable = R.drawable.marker_visit_planned;

        if (retailerMasterBO.isHasNoVisitReason())
            drawable = R.drawable.marker_visit_cancelled;


        return drawable;
    }
}
