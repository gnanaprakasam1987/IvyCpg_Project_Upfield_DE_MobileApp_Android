package com.ivy.cpg.view.reports;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Toast;

import com.ivy.core.base.view.BaseActivity;
import com.ivy.cpg.view.reports.attendancereport.AttendanceReport;
import com.ivy.cpg.view.reports.collectionreport.CollectionReportFragmentNew;
import com.ivy.cpg.view.reports.dayreport.DayReportFragment;
import com.ivy.cpg.view.reports.eodstockreport.EODStockReportFragmentRe;
import com.ivy.cpg.view.reports.invoicereport.InvoiceReportFragment;
import com.ivy.cpg.view.reports.orderreport.OrderReportFragment;
import com.ivy.cpg.view.reports.orderstatusreport.OrderStatusReportFragment;
import com.ivy.cpg.view.reports.retailerProperty.RetailerPropertyReportFragment;
import com.ivy.cpg.view.reports.retaileractivity.RetailerActivityReportFragment;
import com.ivy.cpg.view.reports.taskreport.TaskReportFragment;
import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.bo.ConfigureBO;
import com.ivy.sd.png.model.ApplicationConfigs;
import com.ivy.sd.png.model.BrandDialogInterface;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.model.FiveLevelFilterCallBack;
import com.ivy.sd.png.provider.TaxGstHelper;
import com.ivy.sd.png.provider.TaxHelper;
import com.ivy.sd.png.util.StandardListMasterConstants;
import com.ivy.sd.png.view.ContractReportFragment;
import com.ivy.sd.png.view.CurrentStockBatchViewFragment;
import com.ivy.sd.png.view.HomeScreenActivity;
import com.ivy.sd.png.view.SellerListFragment;
import com.ivy.cpg.view.reports.asset.AssetTrackingReportFragment;
import com.ivy.cpg.view.reports.closingstockreport.ClosingStockReportFragment;
import com.ivy.sd.png.view.reports.CreditNoteReportFragment;
import com.ivy.sd.png.view.reports.DeliveryStockReport;
import com.ivy.sd.png.view.reports.DistOrderReportFragment;
import com.ivy.sd.png.view.reports.DynamicReportFragment;
import com.ivy.cpg.view.reports.inventoryreport.InventoryReportFragment;
import com.ivy.cpg.view.reports.userlogreport.LogReportFragment;
import com.ivy.sd.png.view.reports.OutletPerformanceReportFragmnet;
import com.ivy.sd.png.view.reports.PndInvoiceReportFragment;
import com.ivy.cpg.view.reports.promotion.PromotionTrackingReport;
import com.ivy.sd.png.view.reports.QuestionReportFragment;
import com.ivy.sd.png.view.reports.SOreportFragment;
import com.ivy.cpg.view.reports.sfreport.SalesFundamentalGapReportFragment;
import com.ivy.sd.png.view.reports.soho.SalesReturnReportFragmentSOHO;
import com.ivy.sd.png.view.reports.SalesVolumeReportFragment;
import com.ivy.sd.png.view.reports.SellerMapViewReportFragment;
import com.ivy.sd.png.view.reports.SellerPerformanceReportFragment;
import com.ivy.sd.png.view.reports.TaskExecutionReportFragment;
import com.ivy.sd.png.view.reports.WebViewArchivalReportFragment;
import com.ivy.sd.png.view.reports.piramal.BrandwisePerformance;
import com.ivy.sd.png.view.reports.piramal.OpportunitiesReport;
import com.ivy.sd.png.view.reports.piramal.ProductivityReport;
import com.ivy.sd.png.view.reports.piramal.TimeAndTravelReport;
import com.ivy.ui.reports.beginstockreport.view.BeginningStockFragment;
import com.ivy.ui.reports.currentreport.view.CurrentReportViewFragment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class ReportActivity extends BaseActivity implements
        BrandDialogInterface, SellerListFragment.SellerSelectionInterface,FiveLevelFilterCallBack {

    private BusinessModel bmodel;
    private String fromMenu;

    @Override
    public int getLayoutId() {
        return R.layout.report_menu_fragment_activity_layout;
    }

    @Override
    protected void initVariables() {
        bmodel = (BusinessModel) getApplicationContext();
        bmodel.setContext(this);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

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

        Bundle bun = getIntent().getExtras();
        if (bun != null) {
            ConfigureBO config = (ConfigureBO) bun.getSerializable("config");
            fromMenu = bun.getString("FROM") != null ? bun.getString("FROM") : "";
            switchFragments(config);
        }

        if (bmodel.configurationMasterHelper.IS_GST || bmodel.configurationMasterHelper.IS_GST_HSN)
            bmodel.productHelper.taxHelper = TaxGstHelper.getInstance(this);
        else
            bmodel.productHelper.taxHelper = TaxHelper.getInstance(this);
    }

    @Override
    public void initializeDi() {

    }

    private void setLanguage() {
        SharedPreferences sharedPrefs = PreferenceManager.
                getDefaultSharedPreferences(this);
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
            SalesVolumeReportFragment salesVolumeReportFragment = (SalesVolumeReportFragment) fm
                    .findFragmentByTag(StandardListMasterConstants.MENU_SKU_REPORT);
            if (currentStockBatchViewFragment != null) {
                currentStockBatchViewFragment.onBackButtonClick();
            } else if (salesVolumeReportFragment != null) {
                salesVolumeReportFragment.onBackButtonClick();
            } else {
                if (fromMenu.equalsIgnoreCase("LOADMANAGEMENT")) {
                    finish();
                    overridePendingTransition(R.anim.trans_right_in, R.anim.trans_right_out);
                } else
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
     * @param config - ConfigureBo object
     */
    public void switchFragments(ConfigureBO config) {

        FragmentTransaction transaction = getSupportFragmentManager()
                .beginTransaction();


        setScreenTitle(config.getMenuName());

        if (config.getConfigCode().contains(
                StandardListMasterConstants.MENU_DYN_REPORT)) {
            bmodel.dynamicReportHelper.downloadDynamicReport(config.getConfigCode());

            DynamicReportFragment dynamicReportFragment = new DynamicReportFragment();
            Bundle bundle = new Bundle();
            bundle.putString("isFrom", "Reports");
            dynamicReportFragment.setArguments(bundle);
            transaction.replace(R.id.fragment_content, dynamicReportFragment);

            commitFragment(transaction, config);

        } else if (config.getConfigCode().equals(
                StandardListMasterConstants.MENU_ATTENDANCE_REPORT)) {

            AttendanceReport attendanceReport = new AttendanceReport();
            transaction.replace(R.id.fragment_content, attendanceReport);

            commitFragment(transaction, config);

        } else if (config.getConfigCode().equals(
                StandardListMasterConstants.MENU_RTR_RPT)) {
            RetailerPropertyReportFragment retailerReportFragment = new RetailerPropertyReportFragment();
            Bundle bundle = new Bundle();
            bundle.putInt("type_retailer", config.getModule_Order());
            retailerReportFragment.setArguments(bundle);
            transaction.replace(R.id.fragment_content, retailerReportFragment);

            commitFragment(transaction, config);

        } else if (config.getConfigCode().equals(
                StandardListMasterConstants.MENU_ORDER_REPORT)) {


            // OrderReportFragment orderFragment = new OrderReportFragment();
            OrderReportFragment orderFragment = new OrderReportFragment();
            orderFragment.setArguments(getIntent().getExtras());
            transaction.replace(R.id.fragment_content, orderFragment);

            commitFragment(transaction, config);

        } else if (config.getConfigCode().equals(
                StandardListMasterConstants.MENU_DELIVERY_STOCK_REPORT)) {

            DeliveryStockReport mDeliveryStockReport = new DeliveryStockReport();
            mDeliveryStockReport.setArguments(getIntent().getExtras());
            transaction.replace(R.id.fragment_content, mDeliveryStockReport);

            setSubTitle(transaction, config);

        } else if (config.getConfigCode().equals(
                StandardListMasterConstants.MENU_DAY_REPORT)) {

            //DailyReportFragmentNew dayFragment = new DailyReportFragmentNew();
            DayReportFragment dayFragment = new DayReportFragment();
            dayFragment.setArguments(getIntent().getExtras());
            transaction.replace(R.id.fragment_content, dayFragment);

            commitFragment(transaction, config);

        } else if (config.getConfigCode().equals(
                StandardListMasterConstants.MENU_INVOICE_REPORT)) {

            InvoiceReportFragment invoiceReportFragment = new InvoiceReportFragment();
            invoiceReportFragment.setArguments(getIntent().getExtras());
            transaction.replace(R.id.fragment_content, invoiceReportFragment);

            commitFragment(transaction, config);

        } else if (config.getConfigCode().equals(
                StandardListMasterConstants.MENU_PND_INVOICE_REPORT)) {

            PndInvoiceReportFragment pndInvoiceReportFragment = new PndInvoiceReportFragment();
            pndInvoiceReportFragment.setArguments(getIntent().getExtras());
            transaction.replace(R.id.fragment_content, pndInvoiceReportFragment);

            commitFragment(transaction, config);

        } else if (config.getConfigCode().equals(
                StandardListMasterConstants.MENU_SKU_REPORT)) {

            bmodel.reportHelper.downloadProductReportsWithFiveLevelFilter();

            SalesVolumeReportFragment salesVolumeReportFragment = new SalesVolumeReportFragment();
            salesVolumeReportFragment.setArguments(getIntent().getExtras());
            transaction.replace(R.id.fragment_content, salesVolumeReportFragment, StandardListMasterConstants.MENU_SKU_REPORT);

            commitFragment(transaction, config);

        } else if (config.getConfigCode().equals(
                StandardListMasterConstants.MENU_CURRENT_STOCK_REPORT)) {
            CurrentReportViewFragment stockReportFragment = new CurrentReportViewFragment();
            stockReportFragment.setArguments(getIntent().getExtras());
            transaction.replace(R.id.fragment_content, stockReportFragment);

            commitFragment(transaction, config);

        } else if (config.getConfigCode().equals(
                StandardListMasterConstants.MENU_BEGINNING_STOCK_REPORT)) {

//            BeginningStockFragment stockreportfragmentnew = new BeginningStockFragment();
            BeginningStockFragment stockreportfragmentnew
                    = new BeginningStockFragment();
            stockreportfragmentnew.setArguments(getIntent().getExtras());
            transaction.replace(R.id.fragment_content, stockreportfragmentnew);

            commitFragment(transaction, config);

        } else if (config.getConfigCode().equals(
                StandardListMasterConstants.MENU_COLLECTION_REPORT)) {

            //  CollectionReportFragment collectionReportFragment = new CollectionReportFragment();
            CollectionReportFragmentNew collectionReportFragment = new CollectionReportFragmentNew();
            collectionReportFragment.setArguments(getIntent().getExtras());
            transaction.replace(R.id.fragment_content, collectionReportFragment);

            commitFragment(transaction, config);

        } else if (config.getConfigCode().equals(
                StandardListMasterConstants.MENU_CREDIT_NOTE_REPORT)) {

            CreditNoteReportFragment creditNoteReportFragment = new CreditNoteReportFragment();
            creditNoteReportFragment.setArguments(getIntent().getExtras());
            transaction.replace(R.id.fragment_content, creditNoteReportFragment);

            commitFragment(transaction, config);

        } else if (config.getConfigCode().equals(
                StandardListMasterConstants.MENU_TASK_EXECUTION_REPORT)) {

            TaskExecutionReportFragment taskReportFragment = new TaskExecutionReportFragment();
            taskReportFragment.setArguments(getIntent().getExtras());
            transaction.replace(R.id.fragment_content, taskReportFragment);

            commitFragment(transaction, config);

        } else if (config.getConfigCode().equals(
                StandardListMasterConstants.MENU_EOD_STOCK_REPORT)) {
            bmodel.configurationMasterHelper.loadEODColumnConfiguration();
            bmodel.configurationMasterHelper.loadEODUOMConfiguration();

            // EODStockReportFragment mEODStockReportFragment = new EODStockReportFragment();
            EODStockReportFragmentRe mEODStockReportFragment = new EODStockReportFragmentRe();


            mEODStockReportFragment.setArguments(getIntent().getExtras());
            transaction.replace(R.id.fragment_content, mEODStockReportFragment);

            commitFragment(transaction, config);

        } else if (config.getConfigCode().equals("MENU_REPORT_CLOSE")) {

            startActivity(new Intent(this, HomeScreenActivity.class));
            finish();
        } else if (config.getConfigCode().equals(
                StandardListMasterConstants.MENU_TASK_REPORT)) {

            TaskReportFragment taskreportfragment = new TaskReportFragment();
            transaction.replace(R.id.fragment_content, taskreportfragment);

            commitFragment(transaction, config);

        } else if (config.getConfigCode().equals(
                StandardListMasterConstants.MENU_QUESTION_REPORT)) {

            QuestionReportFragment questionReportFragment = new QuestionReportFragment();
            transaction.replace(R.id.fragment_content, questionReportFragment);

            commitFragment(transaction, config);

        } else if (config.getConfigCode().equals(StandardListMasterConstants.MENU_CURRENT_STOCK_BATCH_REPORT)) {

            bmodel.productHelper.setFilterProductLevels(bmodel.productHelper.downloadFilterLevel("MENU_LOAD_MANAGEMENT"));
            bmodel.productHelper.setFilterProductsByLevelId(bmodel.productHelper.downloadFilterLevelProducts("MENU_LOAD_MANAGEMENT",
                    bmodel.productHelper.getFilterProductLevels()));
            bmodel.productHelper.downloadLoadMgmtProductsWithFiveLevel(
                    "MENU_LOAD_MANAGEMENT", "MENU_CUR_STK_BATCH");


            CurrentStockBatchViewFragment currentStockBatchViewFragment = new CurrentStockBatchViewFragment();
            transaction.replace(R.id.fragment_content, currentStockBatchViewFragment, StandardListMasterConstants.MENU_CURRENT_STOCK_BATCH_REPORT);

            commitFragment(transaction, config);

        } else if (config.getConfigCode().equals(StandardListMasterConstants.MENU_PS_ORD_RPT)) {

            DistOrderReportFragment distOrderReportFrag = new DistOrderReportFragment();
            transaction.replace(R.id.fragment_content, distOrderReportFrag);

            commitFragment(transaction, config);

        } else if (config.getConfigCode().equals(
                StandardListMasterConstants.MENU_BRAND_PERFORMANCE_REPORT)) {

            BrandwisePerformance brandwisePerformanceFragment = new BrandwisePerformance();
            brandwisePerformanceFragment.setArguments(getIntent().getExtras());
            transaction.replace(R.id.fragment_content, brandwisePerformanceFragment);

            setSubTitle(transaction, config);

        } else if (config.getConfigCode().equals(
                StandardListMasterConstants.MENU_OPPORTUNITIES_REPORT)) {

            OpportunitiesReport mOpportunitiesReport = new OpportunitiesReport();
            mOpportunitiesReport.setArguments(getIntent().getExtras());
            transaction.replace(R.id.fragment_content, mOpportunitiesReport);

            setSubTitle(transaction, config);

        } else if (config.getConfigCode().equals(
                StandardListMasterConstants.MENU_TIMEANDTRAVEL_REPORT)) {

            TimeAndTravelReport mTimeAndTravelReport = new TimeAndTravelReport();
            mTimeAndTravelReport.setArguments(getIntent().getExtras());
            transaction.replace(R.id.fragment_content, mTimeAndTravelReport);

            commitFragment(transaction, config);

        } else if (config.getConfigCode().equals(
                StandardListMasterConstants.MENU_PRODUCTIVITY_REPORT)) {

            ProductivityReport mProductivityReport = new ProductivityReport();
            mProductivityReport.setArguments(getIntent().getExtras());
            transaction.replace(R.id.fragment_content, mProductivityReport);

            setSubTitle(transaction, config);

        } else if (config.getConfigCode().equals(
                StandardListMasterConstants.MENU_LOG)) {

            LogReportFragment logReportFragment = new LogReportFragment();
            transaction.replace(R.id.fragment_content, logReportFragment);

            commitFragment(transaction, config);

        } else if (config.getConfigCode().equals(StandardListMasterConstants.MENU_WEBVIEW_RPT01) ||
                config.getConfigCode().equals(StandardListMasterConstants.MENU_WEBVIEW_RPT02)) {

            if (bmodel.isOnline()) {
                bmodel.reportHelper.downloadWebViewReportUrl(config.getConfigCode());
                if (!bmodel.reportHelper.getWebReportUrl().equals("")) {

                    SOreportFragment sOreportFragment = new SOreportFragment();
                    transaction.replace(R.id.fragment_content, sOreportFragment);

                    commitFragment(transaction, config);

                } else {
                    Toast.makeText(this, getResources().getString(R.string.error_message_bad_url), Toast.LENGTH_LONG).show();
                }
            } else {
                Toast.makeText(this, getResources().getString(R.string.please_connect_to_internet), Toast.LENGTH_LONG).show();
            }
        } else if (config.getConfigCode().equals(
                StandardListMasterConstants.MENU_CONTRACT_REPORT)) {

            ContractReportFragment mContractReport = new ContractReportFragment();
            transaction.replace(R.id.fragment_content, mContractReport);

            setSubTitle(transaction, config);

        } else if (config.getConfigCode().equals(StandardListMasterConstants.MENU_SFG_REPORT)) {

            SalesFundamentalGapReportFragment salesFundamentalGapReportFragment = new SalesFundamentalGapReportFragment();
            salesFundamentalGapReportFragment.setArguments(getIntent().getExtras());
            transaction.replace(R.id.fragment_content, salesFundamentalGapReportFragment);

            commitFragment(transaction, config);

        } else if (config.getConfigCode().equals(StandardListMasterConstants.MENU_PROMO_REPORT)) {

            PromotionTrackingReport promotionTrackingReportFragment = new PromotionTrackingReport();
            promotionTrackingReportFragment.setArguments(getIntent().getExtras());
            transaction.replace(R.id.fragment_content, promotionTrackingReportFragment);

            commitFragment(transaction, config);

        } else if (config.getConfigCode().equals(StandardListMasterConstants.MENU_ASSET_REPORT)) {

            AssetTrackingReportFragment assetTrackingReportFragment = new AssetTrackingReportFragment();
            assetTrackingReportFragment.setArguments(getIntent().getExtras());
            transaction.replace(R.id.fragment_content, assetTrackingReportFragment);

            commitFragment(transaction, config);

        } else if (config.getConfigCode().equals(
                StandardListMasterConstants.MENU_INVENTORY_RPT)) {

            InventoryReportFragment mInventoryReport = new InventoryReportFragment();
            transaction.replace(R.id.fragment_content, mInventoryReport);
            bmodel.mSelectedActivityName = config.getMenuName();

            setSubTitle(transaction, config);

        } else if (config.getConfigCode().equals(
                StandardListMasterConstants.MENU_SELLER_MAPVIEW_REPORT)) {
            bmodel.reportHelper.downloadUsers();
            if (bmodel.reportHelper.getLstUsers().size() > 0) {

                SellerMapViewReportFragment mSellerMapviewReport = new SellerMapViewReportFragment();
                transaction.replace(R.id.fragment_content, mSellerMapviewReport, StandardListMasterConstants.MENU_SELLER_MAPVIEW_REPORT);
                bmodel.mSelectedActivityName = config.getMenuName();

                commitFragment(transaction, config);

            } else {
                Toast.makeText(this, getResources().getString(R.string.data_not_mapped), Toast.LENGTH_LONG).show();
            }
        } else if (config.getConfigCode().equals(
                StandardListMasterConstants.MENU_SELLER_PERFOMANCE_REPORT)) {

            SellerPerformanceReportFragment mSellerPerformReport = new SellerPerformanceReportFragment();
            transaction.replace(R.id.fragment_content, mSellerPerformReport);
            bmodel.mSelectedActivityName = config.getMenuName();
            commitFragment(transaction, config);

        } else if (config.getConfigCode().equals(
                StandardListMasterConstants.MENU_RETPERFO_RPT)) {
            bmodel.reportHelper.downloadUsers();
            if (bmodel.reportHelper.getLstUsers().size() > 0) {

                OutletPerformanceReportFragmnet mOutletPerformanceReportFragmnet = new OutletPerformanceReportFragmnet();
                transaction.replace(R.id.fragment_content, mOutletPerformanceReportFragmnet, StandardListMasterConstants.MENU_RETPERFO_RPT);
                bmodel.mSelectedActivityName = config.getMenuName();

                commitFragment(transaction, config);

            } else {
                Toast.makeText(this, getResources().getString(R.string.data_not_mapped), Toast.LENGTH_LONG).show();
            }
        } else if (config.getConfigCode().equals(
                StandardListMasterConstants.MENU_SALES_REPORT)) {

            SalesReturnReportFragmentSOHO salesReturnReport = new SalesReturnReportFragmentSOHO();
            salesReturnReport.setArguments(getIntent().getExtras());
            transaction.replace(R.id.fragment_content, salesReturnReport);

            commitFragment(transaction, config);

        } else if (config.getConfigCode().equals(
                StandardListMasterConstants.MENU_ARCHV_RPT)) {

            WebViewArchivalReportFragment webViewArchivalReportFragment = new WebViewArchivalReportFragment();
            transaction.replace(R.id.fragment_content, webViewArchivalReportFragment);

            setSubTitle(transaction, config);

        } else if (config.getConfigCode().equals(
                StandardListMasterConstants.MENU_CLOSING_STK_RPT)) {

            ClosingStockReportFragment closingStockReportFragment = new ClosingStockReportFragment();
            transaction.replace(R.id.fragment_content, closingStockReportFragment);
            commitFragment(transaction, config);

        } else if (config.getConfigCode().equals(
                StandardListMasterConstants.MENU_RETAILER_ACTIVITY_REPORT)) {

            RetailerActivityReportFragment mRetailerActivityReport = new RetailerActivityReportFragment();
            transaction.replace(R.id.fragment_content, mRetailerActivityReport);
            bmodel.mSelectedActivityName = config.getMenuName();
            commitFragment(transaction, config);
        } else if (config.getConfigCode().equals(
                StandardListMasterConstants.MENU_ORD_STAT_RPT)) {

            OrderStatusReportFragment orderStatusReportFragment = OrderStatusReportFragment.newInstance(true);
            transaction.replace(R.id.fragment_content, orderStatusReportFragment);
            commitFragment(transaction, config);
        } else if (config.getConfigCode().equals(
                StandardListMasterConstants.MENU_INV_STAT_RPT)) {

            OrderStatusReportFragment orderStatusReportFragment = OrderStatusReportFragment.newInstance(false);
            transaction.replace(R.id.fragment_content, orderStatusReportFragment);
            commitFragment(transaction, config);
        }


    }

    private void setSubTitle(FragmentTransaction transaction, ConfigureBO config) {
        transaction.addToBackStack(null);
        getSupportActionBar().setSubtitle(config.getMenuName());
        overridePendingTransition(R.anim.trans_left_in, R.anim.trans_left_out);
        transaction.commit();
    }

    private void commitFragment(FragmentTransaction transaction, ConfigureBO config) {
        transaction.addToBackStack(null);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        setScreenTitle(config.getMenuName());
        overridePendingTransition(R.anim.trans_left_in, R.anim.trans_left_out);
        transaction.commit();
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
    public void updateFromFiveLevelFilter(int mFilteredPid, HashMap<Integer, Integer> mSelectedIdByLevelId, ArrayList<Integer> mAttributeProducts, String mFilterText) {
        android.support.v4.app.FragmentManager fm = getSupportFragmentManager();
        CurrentStockBatchViewFragment currentStockBatchViewFragment = (CurrentStockBatchViewFragment) fm
                .findFragmentByTag(StandardListMasterConstants.MENU_CURRENT_STOCK_BATCH_REPORT);
        if (currentStockBatchViewFragment != null)
            currentStockBatchViewFragment.updateFromFiveLevelFilter(mFilteredPid, mSelectedIdByLevelId, mAttributeProducts, mFilterText);
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


    @Override
    public void updateUserSelection(ArrayList<Integer> mSelectedUsers, boolean isAllUser) {
        android.support.v4.app.FragmentManager fm = getSupportFragmentManager();
        SellerMapViewReportFragment fragment = (SellerMapViewReportFragment) fm
                .findFragmentByTag(StandardListMasterConstants.MENU_SELLER_MAPVIEW_REPORT);
        OutletPerformanceReportFragmnet outlet_perf_fragmnet = (OutletPerformanceReportFragmnet) fm
                .findFragmentByTag(StandardListMasterConstants.MENU_RETPERFO_RPT);

        if (fragment != null)
            fragment.updateUserSelection(mSelectedUsers, isAllUser);
        else if (outlet_perf_fragmnet != null)
            outlet_perf_fragmnet.updateUserSelection(mSelectedUsers, isAllUser);
    }

    @Override
    public void updateClose() {
        android.support.v4.app.FragmentManager fm = getSupportFragmentManager();
        SellerMapViewReportFragment fragment = (SellerMapViewReportFragment) fm
                .findFragmentByTag(StandardListMasterConstants.MENU_SELLER_MAPVIEW_REPORT);
        OutletPerformanceReportFragmnet outlet_perf_fragmnet = (OutletPerformanceReportFragmnet) fm
                .findFragmentByTag(StandardListMasterConstants.MENU_RETPERFO_RPT);

        if (fragment != null)
            fragment.updateClose();
        else if (outlet_perf_fragmnet != null)
            outlet_perf_fragmnet.updateClose();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        bmodel = null;
    }

    @Override
    protected void getMessageFromAliens() {

    }

    @Override
    protected void setUpViews() {

    }
}