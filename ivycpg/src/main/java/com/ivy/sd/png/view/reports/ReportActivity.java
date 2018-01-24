package com.ivy.sd.png.view.reports;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.bo.ConfigureBO;
import com.ivy.sd.png.bo.InitiativeReportBO;
import com.ivy.sd.png.bo.LevelBO;
import com.ivy.sd.png.commons.IvyBaseActivityNoActionBar;
import com.ivy.sd.png.model.ApplicationConfigs;
import com.ivy.sd.png.model.BrandDialogInterface;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.util.Commons;
import com.ivy.sd.png.util.StandardListMasterConstants;
import com.ivy.sd.png.view.ContractReportFragment;
import com.ivy.sd.png.view.CurrentStockBatchViewFragment;
import com.ivy.sd.png.view.HomeScreenActivity;
import com.ivy.sd.png.view.SellerListFragment;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Vector;

public class ReportActivity extends IvyBaseActivityNoActionBar implements
        BrandDialogInterface, SellerListFragment.SellerSelectionInterface {

    private OrderReportFragment orderFragment;
    private SalesVolumeReportFragment salesVolumeReportFragment;
    private PreviousDayOrderReportFragment pvsorderFragment;
    private DailyReportFragmentNew dayFragment;
    private InvoiceReportFragment invoiceReportFragment;
    private PndInvoiceReportFragment pndInvoiceReportFragment;
    private SKUReportFragment skuReportFragment;
    private CurrentStockView stockReportFragment;
    private BeginningStockFragment stockreportfragmentnew;
    private CollectionReportFragment collectionReportFragment;
    private TaskExecutionReportFragment taskReportFragment;
    private EODStockReportFragment mEODStockReportFragment;
    private VolumeReportFragment volumeReportFragment;
    private SBDReportFragment sbdreportfragment;
    private DSRTodayReportFragment dsrtodayreportfragment;
    private DSRMTDReportFragment dsrmtdreportfragment;
    private InitiativeReportFragment initiativereportfragment;
    private OrderReportFrag ordrepfrag;
    private TaskReportFragment taskreportfragment;
    private QuestionReportFragment questionReportFragment;
    private CurrentStockBatchViewFragment currentStockBatchViewFragment;
    private DashboardReportFragment dashboardReportFragment;
    private DistOrderReportFragment distOrderReportFrag;
    private DynamicReportFragment dynamicReportFragment;
    private RetailerReportFragment retailerReportFragment;
    private LogReportFragment logReportFragment;
    private SOreportFragment sOreportFragment;
    CreditNoteReportFragment creditNoteReportFragment;
    private AttendanceReport attendanceReport;
    private ContractReportFragment mContractReport;
    private SalesFundamentalGapReportFragment salesFundamentalGapReportFragment;
    private PromotionTrackingReport promotionTrackingReportFragment;
    private AssetTrackingReportFragment assetTrackingReportFragment;
    private OutletPerformanceReportFragmnet mOutletPerformanceReportFragmnet;

    private BusinessModel bmodel;
    private String menuTitle;
    private FragmentTransaction transaction;

    private BrandwisePerformance brandwisePerformanceFragment;
    private OpportunitiesReport mOpportunitiesReport;
    private TimeAndTravelReport mTimeAndTravelReport;
    private ProductivityReport mProductivityReport;
    private DeliveryStockReport mDeliveryStockReport;
    private CSCustomerVisited csCustomerVisited;
    private InventoryReportFragment mInventoryReport;
    private SellerMapViewReportFragment mSellerMapviewReport;
    private SellerPerformanceReportFragment mSellerPerformReport;
    private SalesReturnReportFragment salesReturnReport;
    private Toolbar toolbar;

    @SuppressLint("NewApi")
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.report_menu_fragment_activity_layout);
        bmodel = (BusinessModel) getApplicationContext();
        bmodel.setContext(this);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        try {
            LinearLayout rootBg = (LinearLayout) findViewById(R.id.root);
            File f = new File(
                    getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)
                            + "/"
                            + bmodel.userMasterHelper.getUserMasterBO()
                            .getUserid() + "APP");
            if (f.isDirectory()) {
                File files[] = f.listFiles(new FilenameFilter() {
                    public boolean accept(File directory, String fileName) {
                        return fileName.startsWith("bg_menu");
                    }
                });
                for (File temp : files) {
                    Bitmap bitmapImage = BitmapFactory.decodeFile(temp
                            .getAbsolutePath());
                    Drawable bgrImage1 = new BitmapDrawable(this.getResources(), bitmapImage);
                    int sdk = android.os.Build.VERSION.SDK_INT;
                    if (sdk < android.os.Build.VERSION_CODES.JELLY_BEAN) {
                        rootBg.setBackgroundDrawable(bgrImage1);
                    } else {
                        rootBg.setBackground(bgrImage1);
                    }
                    break;
                }

            }
        } catch (Exception e) {
            Commons.printException(e);
        }

        // Set title to actionbar
        setScreenTitle(getResources().getString(R.string.report));
        // Used to on / off the back arrow icon
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        // Used to remove the app logo actionbar icon and set title as home
        // (title support click)
        // getSupportActionBar().setDisplayShowHomeEnabled(true);
        // Used to hide the app logo icon from actionbar
        getSupportActionBar().setDisplayShowTitleEnabled(true);
        // getSupportActionBar().setDisplayUseLogoEnabled(false);

        if (Build.VERSION.SDK_INT < 16) {
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                    WindowManager.LayoutParams.FLAG_FULLSCREEN);
        } else {
            View decorView = getWindow().getDecorView();
            // Hide the status bar.
            int uiOptions = View.SYSTEM_UI_FLAG_FULLSCREEN;
            decorView.setSystemUiVisibility(uiOptions);
        }

        setLanguage();

        mContractReport = new ContractReportFragment();
        orderFragment = new OrderReportFragment();
        orderFragment.setArguments(getIntent().getExtras());
        salesVolumeReportFragment = new SalesVolumeReportFragment();
        salesVolumeReportFragment.setArguments(getIntent().getExtras());
        mDeliveryStockReport = new DeliveryStockReport();
        mDeliveryStockReport.setArguments(getIntent().getExtras());
        pvsorderFragment = new PreviousDayOrderReportFragment();
        pvsorderFragment.setArguments(getIntent().getExtras());
        dayFragment = new DailyReportFragmentNew();
        dayFragment.setArguments(getIntent().getExtras());
        invoiceReportFragment = new InvoiceReportFragment();
        invoiceReportFragment.setArguments(getIntent().getExtras());
        pndInvoiceReportFragment = new PndInvoiceReportFragment();
        pndInvoiceReportFragment.setArguments(getIntent().getExtras());
        skuReportFragment = new SKUReportFragment();
        skuReportFragment.setArguments(getIntent().getExtras());
        stockReportFragment = new CurrentStockView();
        stockReportFragment.setArguments(getIntent().getExtras());
        stockreportfragmentnew = new BeginningStockFragment();
        stockreportfragmentnew.setArguments(getIntent().getExtras());
        collectionReportFragment = new CollectionReportFragment();
        collectionReportFragment.setArguments(getIntent().getExtras());
        salesReturnReport = new SalesReturnReportFragment();
        salesReturnReport.setArguments(getIntent().getExtras());
        creditNoteReportFragment = new CreditNoteReportFragment();
        creditNoteReportFragment.setArguments(getIntent().getExtras());
        taskReportFragment = new TaskExecutionReportFragment();
        taskReportFragment.setArguments(getIntent().getExtras());
        mEODStockReportFragment = new EODStockReportFragment();
        mEODStockReportFragment.setArguments(getIntent().getExtras());
        volumeReportFragment = new VolumeReportFragment();
        sbdreportfragment = new SBDReportFragment();
        dsrtodayreportfragment = new DSRTodayReportFragment();
        dsrmtdreportfragment = new DSRMTDReportFragment();
        initiativereportfragment = new InitiativeReportFragment();
        ordrepfrag = new OrderReportFrag();
        taskreportfragment = new TaskReportFragment();
        questionReportFragment = new QuestionReportFragment();
        currentStockBatchViewFragment = new CurrentStockBatchViewFragment();
        dashboardReportFragment = new DashboardReportFragment();
        distOrderReportFrag = new DistOrderReportFragment();
        dynamicReportFragment = new DynamicReportFragment();
        retailerReportFragment = new RetailerReportFragment();
        logReportFragment = new LogReportFragment();
        sOreportFragment = new SOreportFragment();
        attendanceReport = new AttendanceReport();
        brandwisePerformanceFragment = new BrandwisePerformance();
        brandwisePerformanceFragment.setArguments(getIntent().getExtras());
        mOpportunitiesReport = new OpportunitiesReport();
        mOpportunitiesReport.setArguments(getIntent().getExtras());
        mTimeAndTravelReport = new TimeAndTravelReport();
        mTimeAndTravelReport.setArguments(getIntent().getExtras());
        mProductivityReport = new ProductivityReport();
        mProductivityReport.setArguments(getIntent().getExtras());
        csCustomerVisited = new CSCustomerVisited();
        mInventoryReport = new InventoryReportFragment();
        mSellerMapviewReport = new SellerMapViewReportFragment();
        mSellerPerformReport = new SellerPerformanceReportFragment();
        mOutletPerformanceReportFragmnet=new OutletPerformanceReportFragmnet();


        salesFundamentalGapReportFragment = new SalesFundamentalGapReportFragment();
        salesFundamentalGapReportFragment.setArguments(getIntent().getExtras());
        promotionTrackingReportFragment = new PromotionTrackingReport();
        promotionTrackingReportFragment.setArguments(getIntent().getExtras());
        assetTrackingReportFragment = new AssetTrackingReportFragment();
        assetTrackingReportFragment.setArguments(getIntent().getExtras());
        Bundle bun = getIntent().getExtras();
        ConfigureBO config = (ConfigureBO) bun.getSerializable("config");
        switchFragments(config);
    }

    private void setLanguage() {
        SharedPreferences sharedPrefs = PreferenceManager
                .getDefaultSharedPreferences(this);
        String language = sharedPrefs.getString("languagePref",
                ApplicationConfigs.LANGUAGE);
        Locale myLocale = new Locale(language);
        Resources res = getResources();
        DisplayMetrics dm = res.getDisplayMetrics();
        Configuration conf = res.getConfiguration();
        conf.locale = myLocale;
        res.updateConfiguration(conf, dm);
        onConfigurationChanged(conf);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int i = item.getItemId();
        if (i == android.R.id.home) {
            android.support.v4.app.FragmentManager fm = getSupportFragmentManager();
            CurrentStockBatchViewFragment currentStockBatchViewFragment = (CurrentStockBatchViewFragment) fm
                    .findFragmentByTag(StandardListMasterConstants.MENU_CURRENT_STOCK_BATCH_REPORT);
            if (currentStockBatchViewFragment != null) {
                currentStockBatchViewFragment.onBackButtonClick();
            } else {
                onBackButtonClick();
            }

            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void onBackButtonClick() {
        Intent i = new Intent(ReportActivity.this, HomeScreenActivity.class);
        i.putExtra("menuCode", "MENU_REPORT");
        i.putExtra("title", "aaa");
        startActivity(i);
        finish();
        overridePendingTransition(R.anim.trans_right_in, R.anim.trans_right_out);

    }

    /**
     * Used to switch fragment
     *
     * @param config
     */
    public void switchFragments(ConfigureBO config) {

        FragmentTransaction transaction = getSupportFragmentManager()
                .beginTransaction();

        if (config.getConfigCode().contains(
                StandardListMasterConstants.MENU_DYN_REPORT)) {
            bmodel.dynamicReportHelper.downloadDynamicReport(config.getConfigCode());
            dynamicReportFragment = new DynamicReportFragment();
            Bundle bundle = new Bundle();
            bundle.putString("isFrom", "Reports");
            dynamicReportFragment.setArguments(bundle);
            transaction.replace(R.id.fragment_content, dynamicReportFragment);
            transaction.addToBackStack(null);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            setScreenTitle(config.getMenuName());
            overridePendingTransition(R.anim.trans_left_in, R.anim.trans_left_out);
            transaction.commit();
        } else if (config.getConfigCode().equals(
                StandardListMasterConstants.MENU_ATTENDANCE_REPORT)) {
            bmodel.reportHelper.downloadAttendanceReport();
            transaction.replace(R.id.fragment_content, attendanceReport);
            transaction.addToBackStack(null);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            setScreenTitle(config.getMenuName());
            overridePendingTransition(R.anim.trans_left_in, R.anim.trans_left_out);
            transaction.commit();
        } else if (config.getConfigCode().equals(
                StandardListMasterConstants.MENU_RTR_RPT)) {
            retailerReportFragment = new RetailerReportFragment();
            Bundle bundle = new Bundle();
            bundle.putInt("type_retailer", config.getModule_Order());
            retailerReportFragment.setArguments(bundle);
            transaction.replace(R.id.fragment_content, retailerReportFragment);
            transaction.addToBackStack(null);
            overridePendingTransition(R.anim.trans_left_in, R.anim.trans_left_out);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            setScreenTitle(config.getMenuName());
            transaction.commit();
        } else if (config.getConfigCode().equals(
                StandardListMasterConstants.MENU_ORDER_REPORT)) {
            if (bmodel.configurationMasterHelper.SHOW_PREV_ORDER_REPORT) {
                transaction.replace(R.id.fragment_content, ordrepfrag);
            } else
                transaction.replace(R.id.fragment_content, orderFragment);
            transaction.addToBackStack(null);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            setScreenTitle(config.getMenuName());
            overridePendingTransition(R.anim.trans_left_in, R.anim.trans_left_out);
            transaction.commit();
        } else if (config.getConfigCode().equals(
                StandardListMasterConstants.MENU_DELIVERY_STOCK_REPORT)) {

            transaction.replace(R.id.fragment_content, mDeliveryStockReport);
            transaction.addToBackStack(null);
            getSupportActionBar().setSubtitle(config.getMenuName());
            transaction.commit();
        } else if (config.getConfigCode().equals(
                StandardListMasterConstants.MENU_PREVIOUS_ORDER_REPORT)) {
            transaction.replace(R.id.fragment_content, pvsorderFragment);
            transaction.addToBackStack(null);
            getSupportActionBar().setSubtitle(config.getMenuName());
            transaction.commit();
        } else if (config.getConfigCode().equals(
                StandardListMasterConstants.MENU_DAY_REPORT)) {
            transaction.replace(R.id.fragment_content, dayFragment);
            transaction.addToBackStack(null);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            setScreenTitle(config.getMenuName());
            overridePendingTransition(R.anim.trans_left_in, R.anim.trans_left_out);
            transaction.commit();
        } else if (config.getConfigCode().equals(
                StandardListMasterConstants.MENU_INVOICE_REPORT)) {
            transaction.replace(R.id.fragment_content, invoiceReportFragment);
            transaction.addToBackStack(null);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            setScreenTitle(config.getMenuName());
            overridePendingTransition(R.anim.trans_left_in, R.anim.trans_left_out);
            transaction.commit();
        } else if (config.getConfigCode().equals(
                StandardListMasterConstants.MENU_PND_INVOICE_REPORT)) {
            transaction.replace(R.id.fragment_content, pndInvoiceReportFragment);
            transaction.addToBackStack(null);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            setScreenTitle(config.getMenuName());
            overridePendingTransition(R.anim.trans_left_in, R.anim.trans_left_out);
            transaction.commit();
        } else if (config.getConfigCode().equals(
                StandardListMasterConstants.MENU_SKU_REPORT)) {
            transaction.replace(R.id.fragment_content, skuReportFragment);
            transaction.addToBackStack(null);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            setScreenTitle(config.getMenuName());
            overridePendingTransition(R.anim.trans_left_in, R.anim.trans_left_out);
            transaction.commit();
        } else if (config.getConfigCode().equals(
                StandardListMasterConstants.MENU_CURRENT_STOCK_REPORT)) {
            bmodel.productHelper
                    .downloadProductFilter("MENU_LOAD_MANAGEMENT");

            transaction.replace(R.id.fragment_content, stockReportFragment);
            transaction.addToBackStack(null);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            setScreenTitle(config.getMenuName());
            overridePendingTransition(R.anim.trans_left_in, R.anim.trans_left_out);
            transaction.commit();
        } else if (config.getConfigCode().equals(
                StandardListMasterConstants.MENU_BEGINNING_STOCK_REPORT)) {
            transaction.replace(R.id.fragment_content, stockreportfragmentnew);
            transaction.addToBackStack(null);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            setScreenTitle(config.getMenuName());
            overridePendingTransition(R.anim.trans_left_in, R.anim.trans_left_out);
            transaction.commit();
        } else if (config.getConfigCode().equals(
                StandardListMasterConstants.MENU_COLLECTION_REPORT)) {
            transaction
                    .replace(R.id.fragment_content, collectionReportFragment);
            transaction.addToBackStack(null);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            setScreenTitle(config.getMenuName());
            overridePendingTransition(R.anim.trans_left_in, R.anim.trans_left_out);
            transaction.commit();
        } else if (config.getConfigCode().equals(
                StandardListMasterConstants.MENU_CREDIT_NOTE_REPORT)) {
            transaction
                    .replace(R.id.fragment_content, creditNoteReportFragment);
            transaction.addToBackStack(null);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            setScreenTitle(config.getMenuName());
            overridePendingTransition(R.anim.trans_left_in, R.anim.trans_left_out);
            transaction.commit();
        } else if (config.getConfigCode().equals(
                StandardListMasterConstants.MENU_TASK_EXECUTION_REPORT)) {
            transaction.replace(R.id.fragment_content, taskReportFragment);
            transaction.addToBackStack(null);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            setScreenTitle(config.getMenuName());
            overridePendingTransition(R.anim.trans_left_in, R.anim.trans_left_out);
            transaction.commit();
        } else if (config.getConfigCode().equals(
                StandardListMasterConstants.MENU_EOD_STOCK_REPORT)) {
            bmodel.configurationMasterHelper.loadEODColumnConfiguration();
            transaction.replace(R.id.fragment_content, mEODStockReportFragment);
            transaction.addToBackStack(null);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            setScreenTitle(config.getMenuName());
            overridePendingTransition(R.anim.trans_left_in, R.anim.trans_left_out);
            transaction.commit();
        } else if (config.getConfigCode().equals("MENU_REPORT_CLOSE")) {

            startActivity(new Intent(this, HomeScreenActivity.class));
            finish();
        } else if (config.getConfigCode().equals(
                StandardListMasterConstants.MENU_VOLUME_REPORT)) {
            transaction.replace(R.id.fragment_content, volumeReportFragment);
            transaction.addToBackStack(null);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            setScreenTitle(config.getMenuName());
            overridePendingTransition(R.anim.trans_left_in, R.anim.trans_left_out);
            transaction.commit();
        } else if (config.getConfigCode().equals(
                StandardListMasterConstants.MENU_SBD_REPORT)) {
            transaction.replace(R.id.fragment_content, sbdreportfragment);
            transaction.addToBackStack(null);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            setScreenTitle(config.getMenuName());
            overridePendingTransition(R.anim.trans_left_in, R.anim.trans_left_out);
            transaction.commit();
        } else if (config.getConfigCode().equals(
                StandardListMasterConstants.MENU_DSRTODAY_REPORT)) {
            transaction.replace(R.id.fragment_content, dsrtodayreportfragment);
            transaction.addToBackStack(null);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            setScreenTitle(config.getMenuName());
            overridePendingTransition(R.anim.trans_left_in, R.anim.trans_left_out);
            transaction.commit();
        } else if (config.getConfigCode().equals(
                StandardListMasterConstants.MENU_DSRMTD_REPORT)) {
            transaction.replace(R.id.fragment_content, dsrmtdreportfragment);
            transaction.addToBackStack(null);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            setScreenTitle(config.getMenuName());
            overridePendingTransition(R.anim.trans_left_in, R.anim.trans_left_out);
            transaction.commit();
        } else if (config.getConfigCode().equals(
                StandardListMasterConstants.MENU_INITIATIVE_REPORT)) {

            this.transaction = transaction;
            menuTitle = config.getMenuName();
            new DownloadInitiative().execute();
        } else if (config.getConfigCode().equals(
                StandardListMasterConstants.MENU_TASK_REPORT)) {

            transaction.replace(R.id.fragment_content, taskreportfragment);
            transaction.addToBackStack(null);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            setScreenTitle(config.getMenuName());
            overridePendingTransition(R.anim.trans_left_in, R.anim.trans_left_out);
            transaction.commit();
        } else if (config.getConfigCode().equals(
                StandardListMasterConstants.MENU_QUESTION_REPORT)) {

            transaction
                    .replace(R.id.fragment_content, questionReportFragment);
            transaction.addToBackStack(null);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            setScreenTitle(config.getMenuName());
            overridePendingTransition(R.anim.trans_left_in, R.anim.trans_left_out);
            transaction.commit();
        } else if (config.getConfigCode().equals(StandardListMasterConstants.MENU_CURRENT_STOCK_BATCH_REPORT)) {

            if (bmodel.configurationMasterHelper.IS_FIVE_LEVEL_FILTER) {
                bmodel.productHelper
                        .downloadFiveFilterLevels("MENU_LOAD_MANAGEMENT");
            } else {
                bmodel.productHelper
                        .downloadProductFilter("MENU_LOAD_MANAGEMENT");
            }


            if (bmodel.configurationMasterHelper.IS_FIVE_LEVEL_FILTER)
                bmodel.productHelper.loadProductsWithFiveLevel(
                        "MENU_LOAD_MANAGEMENT", "MENU_CUR_STK_BATCH");
            else
                bmodel.productHelper.loadProducts("MENU_LOAD_MANAGEMENT",
                        "MENU_CUR_STK_BATCH");


            transaction
                    .replace(R.id.fragment_content, currentStockBatchViewFragment, StandardListMasterConstants.MENU_CURRENT_STOCK_BATCH_REPORT);
            transaction.addToBackStack(null);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            setScreenTitle(config.getMenuName());
            overridePendingTransition(R.anim.trans_left_in, R.anim.trans_left_out);
            transaction.commit();
        } else if (config.getConfigCode().equals(
                StandardListMasterConstants.MENU_SUP_TEST_SCORE)) {

            transaction.replace(R.id.fragment_content, dashboardReportFragment);
            transaction.addToBackStack(null);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            setScreenTitle(config.getMenuName());
            overridePendingTransition(R.anim.trans_left_in, R.anim.trans_left_out);
            transaction.commit();
        } else if (config.getConfigCode().equals(StandardListMasterConstants.MENU_PS_ORD_RPT)) {


            transaction.replace(R.id.fragment_content, distOrderReportFrag);
            transaction.addToBackStack(null);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            setScreenTitle(config.getMenuName());
            overridePendingTransition(R.anim.trans_left_in, R.anim.trans_left_out);
            transaction.commit();

        } else if (config.getConfigCode().equals(
                StandardListMasterConstants.MENU_BRAND_PERFORMANCE_REPORT)) {
            /*if (bmodel.configurationMasterHelper.SHOW_PREV_ORDER_REPORT) {
                transaction.replace(R.id.fragment_content, ordrepfrag);
            } else*/
            transaction.replace(R.id.fragment_content, brandwisePerformanceFragment);
            transaction.addToBackStack(null);
            getSupportActionBar().setSubtitle(config.getMenuName());
            transaction.commit();
        } else if (config.getConfigCode().equals(
                StandardListMasterConstants.MENU_OPPORTUNITIES_REPORT)) {
            /*if (bmodel.configurationMasterHelper.SHOW_PREV_ORDER_REPORT) {
                transaction.replace(R.id.fragment_content, ordrepfrag);
            } else*/
            transaction.replace(R.id.fragment_content, mOpportunitiesReport);
            transaction.addToBackStack(null);
            getSupportActionBar().setSubtitle(config.getMenuName());
            transaction.commit();
        } else if (config.getConfigCode().equals(
                StandardListMasterConstants.MENU_TIMEANDTRAVEL_REPORT)) {
            /*if (bmodel.configurationMasterHelper.SHOW_PREV_ORDER_REPORT) {
                transaction.replace(R.id.fragment_content, ordrepfrag);
            } else*/
            transaction.replace(R.id.fragment_content, mTimeAndTravelReport);
            transaction.addToBackStack(null);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            setScreenTitle(config.getMenuName());
            transaction.commit();
        } else if (config.getConfigCode().equals(
                StandardListMasterConstants.MENU_PRODUCTIVITY_REPORT)) {
            /*if (bmodel.configurationMasterHelper.SHOW_PREV_ORDER_REPORT) {
                transaction.replace(R.id.fragment_content, ordrepfrag);
            } else*/
            transaction.replace(R.id.fragment_content, mProductivityReport);
            transaction.addToBackStack(null);
            getSupportActionBar().setSubtitle(config.getMenuName());
            transaction.commit();
        } else if (config.getConfigCode().equals(
                StandardListMasterConstants.MENU_LOG)) {
            transaction.replace(R.id.fragment_content, logReportFragment);
            transaction.addToBackStack(null);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            setScreenTitle(config.getMenuName());
            overridePendingTransition(R.anim.trans_left_in, R.anim.trans_left_out);
            transaction.commit();
        } else if (config.getConfigCode().equals(
                StandardListMasterConstants.MENU_WEBVIEW_RPT01)) {


            if (bmodel.isOnline()) {
                bmodel.reportHelper.downloadWebViewReportUrl(StandardListMasterConstants.MENU_WEBVIEW_RPT01);
                if (!bmodel.reportHelper.getWebReportUrl().equals("")) {
                    transaction
                            .replace(R.id.fragment_content, sOreportFragment);
                    transaction.addToBackStack(null);
                    getSupportActionBar().setSubtitle(config.getMenuName());
                    transaction.commit();
                } else {
                    Toast.makeText(this, getResources().getString(R.string.error_message_bad_url), Toast.LENGTH_LONG).show();
                }
            } else {
                Toast.makeText(this, getResources().getString(R.string.please_connect_to_internet), Toast.LENGTH_LONG).show();
            }
        } else if (config.getConfigCode().equals(
                StandardListMasterConstants.MENU_CONTRACT_REPORT)) {
            transaction.replace(R.id.fragment_content, mContractReport);
            transaction.addToBackStack(null);
            getSupportActionBar().setSubtitle(config.getMenuName());
            transaction.commit();
        } else if (config.getConfigCode().equals(
                StandardListMasterConstants.MENU_CS_RPT)) {
            transaction.replace(R.id.fragment_content, csCustomerVisited);
            bmodel.mSelectedActivityName = config.getMenuName();
            transaction.addToBackStack(null);
            transaction.commit();
        } else if (config.getConfigCode().equals(StandardListMasterConstants.MENU_SFG_REPORT)) {
            transaction.replace(R.id.fragment_content, salesFundamentalGapReportFragment);
            transaction.addToBackStack(null);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            setScreenTitle(config.getMenuName());
            overridePendingTransition(R.anim.trans_left_in, R.anim.trans_left_out);
            transaction.commit();
        } else if (config.getConfigCode().equals(StandardListMasterConstants.MENU_PROMO_REPORT)) {
            transaction.replace(R.id.fragment_content, promotionTrackingReportFragment);
            transaction.addToBackStack(null);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            setScreenTitle(config.getMenuName());
            overridePendingTransition(R.anim.trans_left_in, R.anim.trans_left_out);
            transaction.commit();
        } else if (config.getConfigCode().equals(StandardListMasterConstants.MENU_ASSET_REPORT)) {
            transaction.replace(R.id.fragment_content, assetTrackingReportFragment);
            transaction.addToBackStack(null);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            setScreenTitle(config.getMenuName());
            overridePendingTransition(R.anim.trans_left_in, R.anim.trans_left_out);
            transaction.commit();
        } else if (config.getConfigCode().equals(
                StandardListMasterConstants.MENU_INVENTORY_RPT)) {
            getSupportActionBar().setSubtitle(config.getMenuName());
            transaction.replace(R.id.fragment_content, mInventoryReport);
            bmodel.mSelectedActivityName = config.getMenuName();
            transaction.addToBackStack(null);
            overridePendingTransition(R.anim.trans_left_in, R.anim.trans_left_out);
            transaction.commit();
        } else if (config.getConfigCode().equals(StandardListMasterConstants.MENU_PRDVOL_RPT)) {
            bmodel.reportHelper.downloadProductReportsWithFiveLevelFilter();
            transaction.replace(R.id.fragment_content, salesVolumeReportFragment);
            transaction.addToBackStack(null);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            setScreenTitle(config.getMenuName());
            transaction.commit();
        }
        else if (config.getConfigCode().equals(
                StandardListMasterConstants.MENU_SELLER_MAPVIEW_REPORT)) {
            bmodel.reportHelper.downloadUsers();
            if(bmodel.reportHelper.getLstUsers().size()>0) {
                transaction.replace(R.id.fragment_content, mSellerMapviewReport, StandardListMasterConstants.MENU_SELLER_MAPVIEW_REPORT);
                getSupportActionBar().setDisplayShowTitleEnabled(false);
                setScreenTitle(config.getMenuName());
                bmodel.mSelectedActivityName = config.getMenuName();
                transaction.addToBackStack(null);
                overridePendingTransition(R.anim.trans_left_in, R.anim.trans_left_out);
                transaction.commit();
            }
            else{
                Toast.makeText(this, getResources().getString(R.string.data_not_mapped), Toast.LENGTH_LONG).show();
            }
        } else if (config.getConfigCode().equals(
                StandardListMasterConstants.MENU_SELLER_PERFOMANCE_REPORT)) {

            transaction.replace(R.id.fragment_content, mSellerPerformReport);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            setScreenTitle(config.getMenuName());
            bmodel.mSelectedActivityName = config.getMenuName();
            transaction.addToBackStack(null);
            overridePendingTransition(R.anim.trans_left_in, R.anim.trans_left_out);
            transaction.commit();

        }
        else if (config.getConfigCode().equals(
                StandardListMasterConstants.MENU_RETPERFO_RPT)) {
            bmodel.reportHelper.downloadUsers();
            if (bmodel.reportHelper.getLstUsers().size() > 0) {
                transaction.replace(R.id.fragment_content, mOutletPerformanceReportFragmnet, StandardListMasterConstants.MENU_RETPERFO_RPT);
                getSupportActionBar().setDisplayShowTitleEnabled(false);
                setScreenTitle(config.getMenuName());
                bmodel.mSelectedActivityName = config.getMenuName();
                transaction.addToBackStack(null);
                overridePendingTransition(R.anim.trans_left_in, R.anim.trans_left_out);
                transaction.commit();
            } else {
                Toast.makeText(this, getResources().getString(R.string.data_not_mapped), Toast.LENGTH_LONG).show();
            }
        }else if (config.getConfigCode().equals(
                StandardListMasterConstants.MENU_SALES_REPORT)) {
            transaction.replace(R.id.fragment_content, salesReturnReport);
            transaction.addToBackStack(null);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            setScreenTitle(config.getMenuName());
            overridePendingTransition(R.anim.trans_left_in, R.anim.trans_left_out);
            transaction.commit();
        }
        // Commit the transaction
    }

    @Override
    public void onBackPressed() {

    }

    @Override
    public void updateMultiSelectionBrand(List<String> mFilterName, List<Integer> mFilterId) {

    }

    @Override
    public void updateMultiSelectionCategory(List<Integer> mCategory) {

    }

    @Override
    public void updateBrandText(String mFilterText, int id) {
        android.support.v4.app.FragmentManager fm = getSupportFragmentManager();
        CurrentStockBatchViewFragment currentStockBatchViewFragment = (CurrentStockBatchViewFragment) fm
                .findFragmentByTag(StandardListMasterConstants.MENU_CURRENT_STOCK_BATCH_REPORT);
        if (currentStockBatchViewFragment != null) {
            currentStockBatchViewFragment.updateBrandText(mFilterText, id);
        }


    }

    @Override
    public void updateFromFiveLevelFilter(Vector<LevelBO> mParentIdList) {
        android.support.v4.app.FragmentManager fm = getSupportFragmentManager();
        CurrentStockBatchViewFragment currentStockBatchViewFragment = (CurrentStockBatchViewFragment) fm
                .findFragmentByTag(StandardListMasterConstants.MENU_CURRENT_STOCK_BATCH_REPORT);
        if (currentStockBatchViewFragment != null)
            currentStockBatchViewFragment.updateFromFiveLevelFilter(mParentIdList);
    }

    @Override
    public void updateFromFiveLevelFilter(Vector<LevelBO> mParentIdList, HashMap<Integer, Integer> mSelectedIdByLevelId, ArrayList<Integer> mAttributeProducts, String mFilterText) {
        android.support.v4.app.FragmentManager fm = getSupportFragmentManager();
        CurrentStockBatchViewFragment currentStockBatchViewFragment = (CurrentStockBatchViewFragment) fm
                .findFragmentByTag(StandardListMasterConstants.MENU_CURRENT_STOCK_BATCH_REPORT);
        if (currentStockBatchViewFragment != null)
            currentStockBatchViewFragment.updateFromFiveLevelFilter(mParentIdList, mSelectedIdByLevelId, mAttributeProducts, mFilterText);
    }


    @Override
    public void updateGeneralText(String mFilterText) {

    }

    @Override
    public void updateCancel() {

    }

    @Override
    public void loadStartVisit() {

    }


    class DownloadInitiative extends AsyncTask<Integer, Integer, Integer> {

        //private ProgressDialog progressDialogue;
        private AlertDialog.Builder builder;
        private AlertDialog alertDialog;

        protected void onPreExecute() {
            /*progressDialogue = ProgressDialog.show(
                    ReportMenuFragmentActivity.this, DataMembers.SD, "Loading",
					true, false);*/
            builder = new AlertDialog.Builder(ReportActivity.this);

            customProgressDialog(builder, getResources().getString(R.string.loading));
            alertDialog = builder.create();
            alertDialog.show();
        }

        @Override
        protected Integer doInBackground(Integer... params) {
            try {
                bmodel.initiativeHelper.generateIntiativeView();
                bmodel.initiativeHelper.setInitlist(bmodel.initiativeHelper
                        .downloadInitReport());
                Collections.sort(bmodel.initiativeHelper.getInitlist(),
                        new Comparator<InitiativeReportBO>() {
                            @Override
                            public int compare(
                                    final InitiativeReportBO object1,
                                    final InitiativeReportBO object2) {

                                return object1.getWalkingSequence()
                                        - object2.getWalkingSequence();
                            }
                        });
                bmodel.initiativeHelper.downloadInitTotalValue();
                bmodel.initiativeHelper.downloadInitMTDValue();

            } catch (Exception e) {
                Commons.printException(e);
            }
            return 0; // Return your real result here
        }

        protected void onProgressUpdate(Integer... progress) {

        }

        protected void onPostExecute(Integer result) {
            // result is the value returned from doInBackground
            /*if (progressDialogue != null)
                progressDialogue.dismiss();*/
            if (alertDialog != null)
                alertDialog.dismiss();
            transaction
                    .replace(R.id.fragment_content, initiativereportfragment);
            transaction.addToBackStack(null);
            getSupportActionBar().setTitle(menuTitle);
            overridePendingTransition(R.anim.trans_left_in, R.anim.trans_left_out);
            transaction.commit();
        }

    }

    @Override
    public void updateUserSelection(ArrayList<Integer> mSelectedUsers, boolean isAllUser) {
        android.support.v4.app.FragmentManager fm = getSupportFragmentManager();
        SellerMapViewReportFragment fragment = (SellerMapViewReportFragment) fm
                .findFragmentByTag(StandardListMasterConstants.MENU_SELLER_MAPVIEW_REPORT);
        OutletPerformanceReportFragmnet outlet_perf_fragmnet = (OutletPerformanceReportFragmnet) fm
                .findFragmentByTag(StandardListMasterConstants.MENU_RETPERFO_RPT);

        if(fragment!=null)
            fragment.updateUserSelection(mSelectedUsers,isAllUser);
        else if(outlet_perf_fragmnet!=null)
            outlet_perf_fragmnet.updateUserSelection(mSelectedUsers,isAllUser);
    }

    @Override
    public void updateClose() {
        android.support.v4.app.FragmentManager fm = getSupportFragmentManager();
        SellerMapViewReportFragment fragment = (SellerMapViewReportFragment) fm
                .findFragmentByTag(StandardListMasterConstants.MENU_SELLER_MAPVIEW_REPORT);
        OutletPerformanceReportFragmnet outlet_perf_fragmnet = (OutletPerformanceReportFragmnet) fm
                .findFragmentByTag(StandardListMasterConstants.MENU_RETPERFO_RPT);

        if(fragment!=null)
            fragment.updateClose();
        else if(outlet_perf_fragmnet!=null)
            outlet_perf_fragmnet.updateClose();
    }
}
