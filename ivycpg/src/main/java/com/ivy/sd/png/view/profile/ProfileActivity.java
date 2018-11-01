package com.ivy.sd.png.view.profile;

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
import android.graphics.Bitmap;
import android.graphics.Canvas;
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
import android.support.v4.content.res.ResourcesCompat;
import android.support.v4.graphics.drawable.DrawableCompat;
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
import com.ivy.cpg.nfc.NFCManager;
import com.ivy.cpg.nfc.NFCReadDialogActivity;
import com.ivy.cpg.view.dashboard.DashBoardHelper;
import com.ivy.cpg.view.dashboard.sellerdashboard.SellerDashboardFragment;
import com.ivy.cpg.view.order.scheme.RetailerInfo;
import com.ivy.cpg.view.order.scheme.SchemeDetailsMasterHelper;
import com.ivy.cpg.view.reports.dynamicReport.DynamicReportFragment;
import com.ivy.cpg.view.reports.dynamicReport.DynamicReportHelper;
import com.ivy.location.LocationUtil;
import com.ivy.sd.camera.CameraActivity;
import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.bo.ConfigureBO;
import com.ivy.sd.png.bo.GenericObjectPair;
import com.ivy.sd.png.bo.ProductMasterBO;
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
import com.ivy.sd.png.provider.SBDHelper;
import com.ivy.sd.png.provider.SynchronizationHelper;
import com.ivy.sd.png.util.CommonDialog;
import com.ivy.sd.png.util.Commons;
import com.ivy.sd.png.util.DataMembers;
import com.ivy.sd.png.util.TimerCount;
import com.ivy.sd.png.view.AssetHistoryFragment;
import com.ivy.sd.png.view.HomeScreenActivity;
import com.ivy.sd.png.view.HomeScreenTwo;
import com.ivy.sd.png.view.MSLUnsoldFragment;
import com.ivy.sd.png.view.NearByRetailerDialog;
import com.ivy.sd.png.view.OTPPasswordDialog;
import com.ivy.sd.png.view.PlanningVisitActivity;
import com.ivy.sd.png.view.SBDGapFragment;
import com.ivy.sd.png.view.SalesPerCategory;
import com.ivy.sd.png.view.TaskListFragment;
import com.ivy.sd.png.view.UserDialogue;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;

import static android.content.res.Configuration.SCREENLAYOUT_SIZE_LARGE;


public class ProfileActivity extends IvyBaseActivityNoActionBar
        implements NearByRetailerDialog.NearByRetailerInterface,
        MapWrapperLayout.OnDragListener,
        CommonReasonDialog.AddNonVisitListener,
        View.OnClickListener, RetailerInfo {

    private static final String MENU_VISIT = "Trade Coverage";
    private static final String MENU_PLANNING = "Day Planning";
    private static final String MENU_PLANNING_SUB = "Day Planning Sub";
    private static final String MENU_STK_ORD = "MENU_STK_ORD";

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
    private String temp = "";
    private final String moduleName = "RT_";
    private String photoPath = "";
    private String fnameStarts = "";
    private String imageName = "";
    private String calledBy;

    private boolean isMapView = false;
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
    private OTPPasswordDialog otpPasswordDialog;
    private android.content.DialogInterface.OnDismissListener otpPasswordDismissListenerNew;

    private AppBarLayout appbar;
    private Drawable upArrow;
    private ImageView profileEditBtn;
    private ImageView drawRouteBtn;
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
                validationToProceed();
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

        setCustomFont();

        bundle = new Bundle();
        bundle.putBoolean("fromHomeClick", fromHomeClick);
        bundle.putBoolean("non_visit", non_visit);
        addTabLayout();

        hideVisibleComponents();


        downloadProductsAndPrice = new DownloadProductsAndPrice();

        new LoadProfileConfigs().execute();

        bmodel.isModuleDone();
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

        if (bmodel.configurationMasterHelper.IS_TEAMLEAD
                && bmodel.configurationMasterHelper.IS_AUDIT_USER) {
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
        if (bmodel.configurationMasterHelper.SHOW_HISTORY) {
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
                lp.height = (int) (displaymetrics.heightPixels / 2.5);//WindowManager.LayoutParams.WRAP_CONTENT;
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
                lp.height = (int) (displaymetrics.heightPixels / 2.5);//WindowManager.LayoutParams.WRAP_CONTENT;
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
                    options.icon(BitmapDescriptorFactory.fromBitmap(getBitmapFromVectorDrawable()));

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

    private Bitmap getBitmapFromVectorDrawable() {
        Drawable drawable = ResourcesCompat.getDrawable(getResources(), R.drawable.store_loc, null);
        // Drawable drawable = AppCompatDrawableManager.get().getDrawable(context, R.drawable.store_loc);
        if (drawable != null) {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
                drawable = (DrawableCompat.wrap(drawable)).mutate();
            }

            Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(),
                    drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmap);
            drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
            drawable.draw(canvas);

            return bitmap;
        }
        return null;
    }

    /**
     * Method used to validate lat long values.
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
                    options.icon(getBitmapDescriptor(R.drawable.store_loc));//storelocation));
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
                options.icon(getBitmapDescriptor(R.drawable.store_loc));
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
                    isMapView = true;
                    retailerLat = retailerObj.getLatitude();

                } else if (conBo.getConfigCode().equals("PROFILE31") && conBo.isFlag() == 1) {
                    isMapView = true;
                    retailerLng = retailerObj.getLongitude();
                } else if (conBo.getConfigCode().equals("PROFILE21") && conBo.isFlag() == 1) {
                    isNonVisitReason = true;
                }
            }
            if (!isMapView) {
                View mapFrag = findViewById(R.id.profile_map);
                mapFrag.setVisibility(View.GONE);
                retailerCodeTxt.setVisibility(View.GONE);
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

            if (visitClick && isMapView)
                getMapView();

            else if (fromHomeClick && isMapView)
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
                return new HistoryFragment();
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
                TaskListFragment taskListFragment = new TaskListFragment();
                Bundle args1 = new Bundle();
                args1.putInt("type", 1);
                args1.putBoolean("isRetailer", true);
                args1.putBoolean("fromReview", false);
                args1.putBoolean("fromProfileScreen", true);
                taskListFragment.setArguments(args1);
                return taskListFragment;
            } else if (tabName.equalsIgnoreCase(SALES_PER_LEVEL)) {
                return new SalesPerCategory();
            } else if (tabName.equalsIgnoreCase(DISTRIBUTOR_PROFILE)) {
                return new DsitributorProfileFragment();
            } else if (tabName.equalsIgnoreCase("SBD Gap")) {
                return new SBDGapFragment();
            } else if (tabName.equals(retailer_contact_title)) {
                return new RetailerContactFragment();
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
                && bmodel.configurationMasterHelper.IS_LOC_TIMER_ON) {
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
                    if (lat != 0.0 && lng != 0.0)
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
                int count = bmodel.synchronizationHelper.getImageCountFromPath(photoPath, fnameStarts);
                Commons.print("ImageCount ," + count + "");
                if (count < bmodel.configurationMasterHelper.RETAILER_PHOTO_COUNT) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(this);
                    builder.setMessage(String.format(getResources().getString(R.string.still_you_can), (bmodel.configurationMasterHelper.RETAILER_PHOTO_COUNT - count)));
                    builder.setCancelable(false);
                    builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {

                        public void onClick(DialogInterface dialog, int which) {
                            imageName = moduleName + bmodel.getRetailerMasterBO().getRetailerID() + "_" + temp + "_" + (bmodel.synchronizationHelper.getImageCountFromPath(photoPath, fnameStarts) + 1) + "_img.jpg";
                            callCamera();

                        }
                    });
                    builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            //loadHomeScreenTwo(bmodel.getRetailerMasterBO());
                            validationToProceed();
                        }
                    });
                    builder.show();
                } else {
                    //loadHomeScreenTwo(bmodel.getRetailerMasterBO());
                    validationToProceed();
                }
            } else if (resultCode == 0) {
                Toast.makeText(this, R.string.photo_mandatory, Toast.LENGTH_LONG).show();
            }

        } else if (requestCode == NFCManager.NFC_REQUEST_CODE) {
            if (resultCode == NFCManager.NFC_CODE_MATCHED || resultCode == NFCManager.NFC_CODE_SELECTING_REASON) {
                mVisitMode = data.getStringExtra("VisitMode");
                mNFCReasonId = data.getStringExtra("NFCReasonId");
                mNFCValidationPassed = true;
                validationToProceed();
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
                return;
            } else
                validationToProceed();
        }
    }

    private void validationToProceed() {

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


        if (bmodel.configurationMasterHelper.SHOW_GPS_ENABLE_DIALOG) {
            if (!bmodel.locationUtil.isGPSProviderEnabled()) {
                Integer resultCode = GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(this);
                if (resultCode == ConnectionResult.SUCCESS)
                    bmodel.requestLocation(this);
                else
                    onCreateDialogNew(2);
                return;
            }
        }

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR2) {
            if (bmodel.isMockSettingsON()) {
                showMocLocationAlert();
                return;
            }
        } else {
            if (LocationUtil.isMockLocation) {
                showMocLocationAlert();
                return;
            }
        }


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

        if (bmodel.configurationMasterHelper.SHOW_RET_SKIP_VALIDATION
                && !bmodel.retailerMasterBO.getIsDeviated().equalsIgnoreCase("Y")
                && bmodel.getVisitretailerMaster().size() > 0) {
            if (!validateSequenceSkip(bmodel.getRetailerMasterBO()))
                return;
        }

        if (bmodel.configurationMasterHelper.SHOW_LOCATION_PASSWORD_DIALOG) {

            if (!checkUserIsNearByRetailer(bmodel.getRetailerMasterBO()))
                return;
        }

        if (bmodel.configurationMasterHelper.SHOW_NFC_VALIDATION_FOR_RETAILER && !mNFCValidationPassed) {
            Intent intent = new Intent(this, NFCReadDialogActivity.class);
            intent.putExtra("nfcvalue", bmodel.getRetailerMasterBO().getNFCTagId().replaceAll(":", ""));
            startActivityForResult(intent, NFCManager.NFC_REQUEST_CODE);
            return;
        }

        if (bmodel.configurationMasterHelper.SHOW_RETAILER_VISIT_CONFIRMATION && !mLocationConfirmationPassed) {
            confirmAtRetailerLocation();
            return;
        }

       /* if (bmodel.configurationMasterHelper.IS_RETAILER_PHOTO_NEEDED) {
            takePhotoForRetailer();
            return;
        }*/

        if (bmodel.configurationMasterHelper.IS_APPLY_DISTRIBUTOR_WISE_PRICE) {

            ArrayList<SupplierMasterBO> mSupplierList = bmodel.downloadSupplierDetails();
            if (mSupplierList != null && mSupplierList.size() == 1) {
                SupplierMasterBO supplierBo = mSupplierList.get(0);
                bmodel.getRetailerMasterBO().setDistributorId(supplierBo.getSupplierID());
                bmodel.getRetailerMasterBO().setDistParentId(supplierBo.getDistParentID());
                bmodel.updatePriceGroupId(true);
                showMessage(getString(R.string.distributor_name) + " "
                        + getString(R.string.selected) + " "
                        + mSupplierList.get(0).getSupplierName());
                loadHomeScreenTwo(bmodel.getRetailerMasterBO());
                return;
            } else {

                mSupplierAdapter = new ArrayAdapter<>(this,
                        android.R.layout.select_dialog_singlechoice, mSupplierList);

                AlertDialog.Builder builder = new AlertDialog.Builder(ProfileActivity.this)
                        .setIcon(null)
                        .setCancelable(false)
                        .setTitle(getResources().getString(R.string.select_distributor))

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
                                        bmodel.updatePriceGroupId(true);

                                        dialog.dismiss();

                                        loadHomeScreenTwo(bmodel.getRetailerMasterBO());


                                    }
                                });
                bmodel.applyAlertDialogTheme(builder);

                return;
            }
        }

        loadHomeScreenTwo(bmodel.getRetailerMasterBO());
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
        if ((SDUtil.compareDate(bmodel.userMasterHelper.getUserMasterBO()
                .getDownloadDate(), SDUtil.now(SDUtil.DATE_GLOBAL), "yyyy/MM/dd") > 0)
                && bmodel.configurationMasterHelper.IS_DATE_VALIDATION_REQUIRED) {
            Toast.makeText(this,
                    getResources().getString(R.string.next_day_coverage),
                    Toast.LENGTH_SHORT).show();

        } else if (bmodel.configurationMasterHelper.IS_TEAMLEAD && bmodel.configurationMasterHelper.IS_AUDIT_USER) {
            bmodel.setRetailerMasterBO(ret);

            ArrayList<UserMasterBO> mUserList = mUserByRetailerID.get(ret
                    .getRetailerID());
            if (mUserList != null) {
                UserDialogue userDialogue = new UserDialogue(
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
                userDialogue.setCancelable(false);
                userDialogue.show();
                isClicked = false;

            } else {

                Toast.makeText(
                        this,
                        getResources().getString(R.string.Users_not_available),
                        Toast.LENGTH_SHORT).show();
            }

        } else {
            if (bmodel.timer == null) {
                bmodel.timer = new TimerCount();
            }
            isClicked = true;
            // Set the select retailer Obj in bmodel
            bmodel.setRetailerMasterBO(ret);
            if (downloadProductsAndPrice.getStatus() != AsyncTask.Status.RUNNING)
            downloadProductsAndPrice.execute();
            // new DownloadProductsAndPrice().execute();
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

        }
        return null;
    }


    private void takePhotoForRetailer() {
        temp = SDUtil.now(SDUtil.DATE_TIME_ID);
        bmodel.outletTimeStampHelper.setUid(bmodel.QT("OTS" + temp));

        if (bmodel.synchronizationHelper
                .isExternalStorageAvailable()) {
            photoPath = this.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
                    + "/"
                    + DataMembers.photoFolderName
                    + "/";

            fnameStarts = moduleName + bmodel.getRetailerMasterBO().getRetailerID() + "_" + temp;

            imageName = moduleName + bmodel.getRetailerMasterBO().getRetailerID() + "_" + temp + "_" + (bmodel.synchronizationHelper.getImageCountFromPath(photoPath, fnameStarts) + 1) + "_img.jpg";

            callCamera();
        } else {
            Toast.makeText(this,
                    getResources().getString(R.string.external_storage_not_available),
                    Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void callCamera() {
        Intent intent = new Intent(this, CameraActivity.class);
        intent.putExtra("quality", 40);
        String mPath = photoPath + "/" + imageName;
        intent.putExtra("path", mPath);
        startActivityForResult(intent, CAMERA_REQUEST_CODE);
    }


    private boolean validateSequenceSkip(RetailerMasterBO ret) {
        if (!getPreviousRetailerVisitedStatus(ret)) {
            if (ret.getSkipActivatedDate() == null
                    || !ret.getSkipActivatedDate().equals(SDUtil.now(SDUtil.DATE_GLOBAL))) {
                if (bmodel.configurationMasterHelper.ret_skip_flag == 1
                        || bmodel.configurationMasterHelper.ret_skip_flag == 2) {
                    callOTPDialog(
                            ret,
                            bmodel.configurationMasterHelper.ret_skip_flag == 1 ? getResources()
                                    .getString(R.string.enter_otp_skip_seq)
                                    : getResources().getString(
                                    R.string.select_reason_skip_seq), 2);
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

    private void callOTPDialog(RetailerMasterBO ret, String strTitle, int flag) {
        if (otpPasswordDialog != null && otpPasswordDialog.isShowing()) {
            otpPasswordDialog.cancel();
            otpPasswordDialog = null;
        }
        otpPasswordDialog = new OTPPasswordDialog(this, bmodel,
                otpPasswordDismissListenerNew, ret, strTitle, flag);
        otpPasswordDialog.setCancelable(false);
        otpPasswordDialog.show();
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
                validationToProceed();
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
                    SDUtil.now(SDUtil.DATE_GLOBAL))) {
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

        callOTPDialog(ret, strTitle, 1);
    }


    @SuppressLint("StaticFieldLeak")
    private class DownloadProductsAndPrice extends AsyncTask<Integer, Integer, Boolean> {
        private AlertDialog.Builder builder;
        private AlertDialog alertDialog;

        @Override
        protected Boolean doInBackground(Integer... params) {
            try {
                if (!isCancelled()) {

                    if (bmodel.configurationMasterHelper.IS_SHOW_SELLER_DIALOG) {
                        bmodel.getRetailerWiseSellerType();
                        bmodel.configurationMasterHelper.updateConfigurationSelectedSellerType(bmodel.getRetailerMasterBO().getIsVansales() != 1);
                    }

                    if (!bmodel.configurationMasterHelper.IS_GLOBAL_CATEGORY) {

                        bmodel.productHelper.setFilterProductLevels(bmodel.productHelper.downloadFilterLevel(MENU_STK_ORD));
                        bmodel.productHelper.setFilterProductsByLevelId(bmodel.productHelper.downloadFilterLevelProducts(MENU_STK_ORD,
                                bmodel.productHelper.getFilterProductLevels()));
                        GenericObjectPair<Vector<ProductMasterBO>, Map<String, ProductMasterBO>> genericObjectPair = bmodel.productHelper.downloadProducts(MENU_STK_ORD);
                        if (genericObjectPair != null) {
                            bmodel.productHelper.setProductMaster(genericObjectPair.object1);
                            bmodel.productHelper.setProductMasterById(genericObjectPair.object2);
                        }

                    } else if (bmodel.configurationMasterHelper.IS_GLOBAL_CATEGORY) {
                        //to reload product filter if diffrent retailer selected
                        bmodel.productHelper.setmLoadedGlobalProductId(0);
                    }

                    bmodel.configurationMasterHelper
                            .loadOrderAndStockConfiguration(bmodel.retailerMasterBO
                                    .getSubchannelid());

                    if (bmodel.productHelper.isSBDFilterAvaiable())
                        SBDHelper.getInstance(ProfileActivity.this).loadSBDFocusData(getApplicationContext());

                    if (bmodel.configurationMasterHelper.SHOW_BATCH_ALLOCATION) {
                        bmodel.batchAllocationHelper.downloadBatchDetails(bmodel
                                .getRetailerMasterBO().getGroupId());
                        bmodel.batchAllocationHelper.downloadProductBatchCount();
                    }

                    bmodel.productHelper.downloadBomMaster();

                    if (bmodel.configurationMasterHelper.SHOW_PRODUCTRETURN
                            && bmodel.configurationMasterHelper.IS_SIH_VALIDATION) {
                        bmodel.productHelper.downlaodReturnableProducts(MENU_STK_ORD);
                        if (bmodel.configurationMasterHelper.SHOW_GROUPPRODUCTRETURN) {
                            bmodel.productHelper.downloadTypeProducts();
                            bmodel.productHelper.downloadGenericProductID();
                        }
                    }


                    if (!bmodel.configurationMasterHelper.SHEME_NOT_APPLY_DEVIATEDSTORE
                            || !"Y".equals(bmodel.getRetailerMasterBO().getIsDeviated())) {

                        SchemeDetailsMasterHelper.getInstance(getApplicationContext()).initializeScheme(ProfileActivity.this,
                                bmodel.userMasterHelper.getUserMasterBO().getUserid(), bmodel.configurationMasterHelper.SHOW_BATCH_ALLOCATION);

                    }

                    if (bmodel.configurationMasterHelper.SHOW_DISCOUNT) {
                        bmodel.productHelper.downloadProductDiscountDetails();
                        bmodel.productHelper.downloadDiscountIdListByTypeId();
                    }

                    if (bmodel.configurationMasterHelper.IS_DISCOUNT_FOR_UNPRICED_PRODUCTS) {
                        bmodel.productHelper.downloadDocketPricing();
                    }

                    //Getting Attributes mapped for the retailer
                    bmodel.getAttributeHierarchyForRetailer();

                    bmodel.reasonHelper.downloadReasons();

                }
            } catch (Exception e) {
                Commons.printException("" + e);
            }
            return Boolean.TRUE;
        }

        protected void onPreExecute() {
            if (!isCancelled()) {
                builder = new AlertDialog.Builder(ProfileActivity.this);
                customProgressDialog(builder, getResources().getString(R.string.loading));
                alertDialog = builder.create();
                alertDialog.show();
            }
        }

        protected void onProgressUpdate(Integer... progress) {
        }

        protected void onPostExecute(Boolean result) {
            if (!isCancelled()) {

                float distance = 0.0f;
                try {
                    // to get last user visited retailer sequence and location to calculate distance..
                    bmodel.outletTimeStampHelper.getlastRetailerDatas();
                    distance = calculateDistanceBetweenRetailers();
                } catch (Exception e) {
                    Commons.printException(e);
                }

                String date = SDUtil.now(SDUtil.DATE_GLOBAL);
                String time = SDUtil.now(SDUtil.TIME);
                temp = SDUtil.now(SDUtil.DATE_TIME_ID);

                bmodel.outletTimeStampHelper.setTimeIn(date + " " + time);
                bmodel.outletTimeStampHelper.setUid(bmodel.QT("OTS" + temp));


                boolean outletTimeStampSaved = bmodel.outletTimeStampHelper.saveTimeStamp(
                        SDUtil.now(SDUtil.DATE_GLOBAL), time
                        , distance, photoPath, fnameStarts, mVisitMode, mNFCReasonId);

                if (alertDialog != null && alertDialog.isShowing()) {
                    alertDialog.dismiss();
                }
                //set selected retailer location and its used on retailer modules
                bmodel.mSelectedRetailerLatitude = LocationUtil.latitude;
                bmodel.mSelectedRetailerLongitude = LocationUtil.longitude;

                Commons.print("Attribute<><><><><><<<><><><><<" + bmodel.getRetailerAttributeList());

                if (outletTimeStampSaved) {
                    Intent i = new Intent(ProfileActivity.this, HomeScreenTwo.class);
                    i.putExtra("isLocDialog", true);
                    i.putExtra("isMandatoryDialog", true);
                    i.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                    startActivity(i);
                    finish();
                } else {
                    Toast.makeText(getApplicationContext(), getString(R.string.not_able_to_register_visit), Toast.LENGTH_LONG).show();
                }
            }
        }
    }

    class DownloadSupervisorData extends AsyncTask<Integer, Integer, Boolean> {
        private AlertDialog.Builder builder;

        @Override
        protected Boolean doInBackground(Integer... params) {
            try {
                bmodel.synchronizationHelper.updateAuthenticateToken(false);
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

    private void showAlert(String msg) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(msg);

        builder.setPositiveButton(getResources().getString(R.string.ok), new DialogInterface.OnClickListener() {

            public void onClick(DialogInterface dialog, int which) {
                //  updateCancel();
                if (calledBy.equalsIgnoreCase(MENU_VISIT)) {
                    Intent i = new Intent(ProfileActivity.this, HomeScreenActivity.class);
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
        if (bmodel.configurationMasterHelper.IS_TEAMLEAD
                && bmodel.configurationMasterHelper.IS_AUDIT_USER) {
            unregisterReceiver(receiver);
        }

        if (downloadProductsAndPrice.getStatus() == AsyncTask.Status.RUNNING)
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
                    bmodel.synchronizationHelper.downloadFinishUpdate(SynchronizationHelper.FROM_SCREEN.VISIT_SCREEN, SynchronizationHelper.DOWNLOAD_FINISH_UPDATE);
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

                if (bmodel.timer == null) {
                    bmodel.timer = new TimerCount();
                }
                isClicked = false;

                bmodel.updateUserAudit(1);
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
                                retailerClick();
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
                                    retailerClick();
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

    @Override
    public String getRetailerId() {
        return bmodel.getRetailerMasterBO().getRetailerID();
    }

    @Override
    public int getDistributorId() {
        return bmodel.getRetailerMasterBO().getDistributorId();
    }

    @Override
    public int getSubChannelId() {
        return bmodel.getRetailerMasterBO().getSubchannelid();
    }

    @Override
    public int getLocationId() {
        return bmodel.getRetailerMasterBO().getLocationId();
    }

    @Override
    public int getAccountId() {
        return bmodel.getRetailerMasterBO().getAccountid();
    }

    @Override
    public int getPriorityProductId() {
        return bmodel.getRetailerMasterBO().getPrioriryProductId();
    }
}
