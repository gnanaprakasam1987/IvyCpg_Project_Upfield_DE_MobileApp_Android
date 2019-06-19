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

import com.ivy.core.base.presenter.BaseIvyPresenter;
import com.ivy.core.base.view.BaseActivity;
import com.ivy.core.base.view.BaseIvyView;
import com.ivy.cpg.view.basedi.BaseModule;
import com.ivy.cpg.view.basedi.DaggerBaseComponent;
import com.ivy.cpg.view.reports.asset.AssetTrackingReportFragment;
import com.ivy.cpg.view.reports.attendancereport.AttendanceReport;
import com.ivy.cpg.view.reports.closingstockreport.ClosingStockReportFragment;
import com.ivy.cpg.view.reports.collectionreport.CollectionReportFragmentNew;
import com.ivy.cpg.view.reports.contractreport.ContractReportFragment;
import com.ivy.cpg.view.reports.creditNoteReport.CreditNoteReportFragment;
import com.ivy.cpg.view.reports.damageReturn.DamageReturnContainerFragment;
import com.ivy.cpg.view.reports.dayreport.DayReportFragment;
import com.ivy.cpg.view.reports.deliveryStockReport.DeliveryStockReport;
import com.ivy.cpg.view.reports.distorderreport.DistOrderReportFragment;
import com.ivy.cpg.view.reports.dynamicReport.DynamicReportFragment;
import com.ivy.cpg.view.reports.dynamicReport.DynamicReportHelper;
import com.ivy.cpg.view.reports.eodstockreport.EODStockReportFragmentRe;
import com.ivy.cpg.view.reports.inventoryreport.InventoryReportFragment;
import com.ivy.cpg.view.reports.invoicereport.InvoiceReportFragment;
import com.ivy.cpg.view.reports.orderfulfillmentreport.OrderFulfillmentReport;
import com.ivy.cpg.view.reports.orderreport.OrderReportFragment;
import com.ivy.cpg.view.reports.orderstatusreport.OrderStatusReportFragment;
import com.ivy.cpg.view.reports.performancereport.OutletPerfomanceHelper;
import com.ivy.cpg.view.reports.performancereport.OutletPerformanceReportFragmnet;
import com.ivy.cpg.view.reports.performancereport.SellerListFragment;
import com.ivy.cpg.view.reports.performancereport.SellerMapViewReportFragment;
import com.ivy.cpg.view.reports.performancereport.SellerPerformanceReportFragment;
import com.ivy.cpg.view.reports.piramal.BrandwisePerformance;
import com.ivy.cpg.view.reports.piramal.OpportunitiesReport;
import com.ivy.cpg.view.reports.piramal.ProductivityReport;
import com.ivy.cpg.view.reports.piramal.TimeAndTravelReport;
import com.ivy.cpg.view.reports.pndInvoiceReport.PndInvoiceReportFragment;
import com.ivy.cpg.view.reports.promotion.PromotionTrackingReport;
import com.ivy.cpg.view.reports.questionReport.QuestionReportFragment;
import com.ivy.cpg.view.reports.retailerProperty.RetailerPropertyReportFragment;
import com.ivy.cpg.view.reports.retaileractivity.RetailerActivityReportFragment;
import com.ivy.cpg.view.reports.salesreturnreport.SalesReturnReportFragment;
import com.ivy.cpg.view.reports.sfreport.SalesFundamentalGapReportFragment;
import com.ivy.cpg.view.reports.slaesvolumereport.SalesVolumeReportFragment;
import com.ivy.cpg.view.reports.slaesvolumereport.SalesVolumeReportHelper;
import com.ivy.cpg.view.reports.soho.SalesReturnReportFragmentSOHO;
import com.ivy.ui.reports.syncreport.view.SyncReportFragment;
import com.ivy.cpg.view.reports.taskexcutionreport.TaskExecutionReportFragment;
import com.ivy.cpg.view.reports.taskreport.TaskReportFragment;
import com.ivy.cpg.view.reports.userlogreport.LogReportFragment;
import com.ivy.cpg.view.reports.webviewreport.SOreportFragment;
import com.ivy.cpg.view.reports.webviewreport.WebViewArchivalReportFragment;
import com.ivy.cpg.view.reports.webviewreport.WebViewReportHelper;
import com.ivy.cpg.view.stockcheck.StockCheckHelper;
import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.bo.ConfigureBO;
import com.ivy.sd.png.model.ApplicationConfigs;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.cpg.view.order.tax.TaxGstHelper;
import com.ivy.cpg.view.order.tax.TaxHelper;
import com.ivy.sd.png.util.StandardListMasterConstants;
import com.ivy.cpg.view.homescreen.HomeScreenActivity;
import com.ivy.ui.AssetServiceRequest.AssetServiceReqFragment;

import java.util.ArrayList;
import java.util.Locale;

import javax.inject.Inject;

public class ReportActivity extends BaseActivity implements BaseIvyView,
        SellerListFragment.SellerSelectionInterface {

    private BusinessModel bmodel;
    @Inject
    BaseIvyPresenter<BaseIvyView> viewBasePresenter;
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
        getSupportActionBar().setTitle(null);
        // Set title to actionbar
        setScreenTitle(getResources().getString(R.string.report));
        // Used to on / off the back arrow icon
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        // Used to remove the app logo actionbar icon and set title as home
        // (title support click)
        // getSupportActionBar().setDisplayShowHomeEnabled(true);
        // Used to hide the app logo icon from actionbar
        getSupportActionBar().setDisplayShowTitleEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
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
            switchFragments(config);
        }

        if (bmodel.configurationMasterHelper.IS_GST || bmodel.configurationMasterHelper.IS_GST_HSN)
            bmodel.productHelper.taxHelper = TaxGstHelper.getInstance(this);
        else
            bmodel.productHelper.taxHelper = TaxHelper.getInstance(this);
    }

    @Override
    public void initializeDi() {
        DaggerBaseComponent.builder()
                .baseModule(new BaseModule(this))
                .ivyAppComponent(((BusinessModel) getApplication()).getComponent())
                .build()
                .inject(this);
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

            SalesVolumeReportFragment salesVolumeReportFragment = (SalesVolumeReportFragment) fm
                    .findFragmentByTag(StandardListMasterConstants.MENU_SKU_REPORT);
            if (salesVolumeReportFragment != null) {
                salesVolumeReportFragment.onBackButtonClick();
            } else {
                finish();
                overridePendingTransition(R.anim.trans_right_in, R.anim.trans_right_out);
            }

            return true;
        }
        return super.onOptionsItemSelected(item);
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
            DynamicReportHelper.getInstance(this).downloadDynamicReport(config.getConfigCode());

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

            commitFragment(transaction, config);

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

            SalesVolumeReportHelper.getInstance(this).downloadProductReportsWithFiveLevelFilter();

            SalesVolumeReportFragment salesVolumeReportFragment = new SalesVolumeReportFragment();
            salesVolumeReportFragment.setArguments(getIntent().getExtras());
            transaction.replace(R.id.fragment_content, salesVolumeReportFragment, StandardListMasterConstants.MENU_SKU_REPORT);

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

        } else if (config.getConfigCode().equals(StandardListMasterConstants.MENU_PS_ORD_RPT)) {

            DistOrderReportFragment distOrderReportFrag = new DistOrderReportFragment();
            transaction.replace(R.id.fragment_content, distOrderReportFrag);

            commitFragment(transaction, config);

        } else if (config.getConfigCode().equals(
                StandardListMasterConstants.MENU_BRAND_PERFORMANCE_REPORT)) {

            BrandwisePerformance brandwisePerformanceFragment = new BrandwisePerformance();
            brandwisePerformanceFragment.setArguments(getIntent().getExtras());
            transaction.replace(R.id.fragment_content, brandwisePerformanceFragment);

            commitFragment(transaction, config);

        } else if (config.getConfigCode().equals(
                StandardListMasterConstants.MENU_OPPORTUNITIES_REPORT)) {

            OpportunitiesReport mOpportunitiesReport = new OpportunitiesReport();
            mOpportunitiesReport.setArguments(getIntent().getExtras());
            transaction.replace(R.id.fragment_content, mOpportunitiesReport);

            commitFragment(transaction, config);

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

            commitFragment(transaction, config);

        } else if (config.getConfigCode().equals(
                StandardListMasterConstants.MENU_LOG)) {

            LogReportFragment logReportFragment = new LogReportFragment();
            transaction.replace(R.id.fragment_content, logReportFragment);

            commitFragment(transaction, config);

        } else if (config.getConfigCode().equals(StandardListMasterConstants.MENU_WEBVIEW_RPT01) ||
                config.getConfigCode().equals(StandardListMasterConstants.MENU_WEBVIEW_RPT02)) {
            WebViewReportHelper webViewReportHelper = WebViewReportHelper.getInstance(this);
            if (bmodel.isOnline()) {
                webViewReportHelper.downloadWebViewReportUrl(config.getConfigCode());
                if (!webViewReportHelper.getWebReportUrl().equals("")) {

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

            commitFragment(transaction, config);

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

            commitFragment(transaction, config);

        } else if (config.getConfigCode().equals(
                StandardListMasterConstants.MENU_SELLER_MAPVIEW_REPORT)) {
            OutletPerfomanceHelper perfomanceHelper = OutletPerfomanceHelper.getInstance(this);
            perfomanceHelper.downloadUsers();
            if (perfomanceHelper.getLstUsers().size() > 0) {

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
            OutletPerfomanceHelper outletPerfomanceHelper = OutletPerfomanceHelper.getInstance(this);
            outletPerfomanceHelper.downloadUsers();
            if (outletPerfomanceHelper.getLstUsers().size() > 0) {

                OutletPerformanceReportFragmnet mOutletPerformanceReportFragmnet = new OutletPerformanceReportFragmnet();
                transaction.replace(R.id.fragment_content, mOutletPerformanceReportFragmnet, StandardListMasterConstants.MENU_RETPERFO_RPT);
                outletPerfomanceHelper.mSelectedActivityName = config.getMenuName();

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

            commitFragment(transaction, config);

        } else if (config.getConfigCode().equals(
                StandardListMasterConstants.MENU_CLOSING_STK_RPT)) {
            StockCheckHelper.getInstance(this).loadStockCheckConfiguration(this, 0);
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
        } else if (config.getConfigCode().equals(
                StandardListMasterConstants.MENU_INV_SALES_RETURN_REPORT)) {

            SalesReturnReportFragment salesReturnReportFragment = new SalesReturnReportFragment();
            transaction.replace(R.id.fragment_content, salesReturnReportFragment);
            commitFragment(transaction, config);
        }
        else if (config.getConfigCode().equals(
                StandardListMasterConstants.MENU_DELIVERY_RPT)) {
            DamageReturnContainerFragment returnsAndPendingDeliverieReportFragment = new DamageReturnContainerFragment();
            transaction.replace(R.id.fragment_content, returnsAndPendingDeliverieReportFragment);
            commitFragment(transaction, config);
        } else if (config.getConfigCode().equals(StandardListMasterConstants.MENU_ORDER_FULFILL_REPORT)){
            OrderFulfillmentReport mOrderFulfillmentReportFragmnet = new OrderFulfillmentReport();
            transaction.replace(R.id.fragment_content, mOrderFulfillmentReportFragmnet);
            commitFragment(transaction, config);
        } else if (config.getConfigCode().equals(StandardListMasterConstants.MENU_SYNC_REPORT)){
            SyncReportFragment syncReportFragment = new SyncReportFragment();
            transaction.replace(R.id.fragment_content, syncReportFragment);
            commitFragment(transaction, config);
        }
        else if (config.getConfigCode().equals(StandardListMasterConstants.MENU_ASSET_SERVICE_REQ_RPT)){
            AssetServiceReqFragment serviceReqFragment = new AssetServiceReqFragment();
            Bundle bundle=new Bundle();
            bundle.putBoolean("isFromReport",true);
            serviceReqFragment.setArguments(bundle);
            transaction.replace(R.id.fragment_content, serviceReqFragment);
            commitFragment(transaction, config);
        }
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
