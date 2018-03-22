package com.ivy.sd.png.view;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.ivy.cpg.view.asset.AssetTrackingActivity;
import com.ivy.cpg.view.asset.AssetTrackingHelper;
import com.ivy.cpg.view.asset.PosmTrackingActivity;
import com.ivy.cpg.view.competitor.CompetitorTrackingActivity;
import com.ivy.cpg.view.digitalcontent.DigitalContentActivity;
import com.ivy.cpg.view.digitalcontent.DigitalContentHelper;
import com.ivy.cpg.view.nearexpiry.NearExpiryTrackingActivity;
import com.ivy.cpg.view.nearexpiry.NearExpiryTrackingHelper;
import com.ivy.cpg.view.order.DiscountHelper;
import com.ivy.cpg.view.order.OrderHelper;
import com.ivy.cpg.view.order.OrderSummary;
import com.ivy.cpg.view.order.StockAndOrder;
import com.ivy.cpg.view.photocapture.PhotoCaptureActivity;
import com.ivy.cpg.view.photocapture.PhotoCaptureHelper;
import com.ivy.cpg.view.planogram.PlanoGramActivity;
import com.ivy.cpg.view.planogram.PlanoGramHelper;
import com.ivy.cpg.view.price.PriceTrackActivity;
import com.ivy.cpg.view.price.PriceTrackCompActivity;
import com.ivy.cpg.view.price.PriceTrackingHelper;
import com.ivy.cpg.view.promotion.PromotionHelper;
import com.ivy.cpg.view.promotion.PromotionTrackingActivity;
import com.ivy.cpg.view.salesreturn.SalesReturnActivity;
import com.ivy.cpg.view.salesreturn.SalesReturnHelper;
import com.ivy.cpg.view.sf.SODActivity;
import com.ivy.cpg.view.sf.SODAssetActivity;
import com.ivy.cpg.view.sf.SODAssetHelper;
import com.ivy.cpg.view.sf.SOSActivity;
import com.ivy.cpg.view.sf.SOSActivity_PRJSpecific;
import com.ivy.cpg.view.sf.SOSKUActivity;
import com.ivy.cpg.view.sf.SalesFundamentalHelper;
import com.ivy.cpg.view.sf.ShelfShareHelper;
import com.ivy.cpg.view.stockcheck.CombinedStockFragmentActivity;
import com.ivy.cpg.view.stockcheck.StockCheckActivity;
import com.ivy.cpg.view.survey.SurveyActivityNew;
import com.ivy.cpg.view.survey.SurveyHelperNew;
import com.ivy.sd.intermecprint.BtPrint4Ivy;
import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.bo.ConfigureBO;
import com.ivy.sd.png.commons.IvyBaseActivityNoActionBar;
import com.ivy.sd.png.commons.SDUtil;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.provider.ConfigurationMasterHelper;
import com.ivy.sd.png.util.Commons;
import com.ivy.sd.png.util.StandardListMasterConstants;
import com.ivy.sd.png.view.merch.MerchandisingActivity;
import com.ivy.sd.print.PrintPreviewScreen;
import com.ivy.sd.print.PrintPreviewScreenDiageo;

import java.util.HashMap;
import java.util.Vector;

public class SubDHomeActivity extends IvyBaseActivityNoActionBar {

    public static final String MENU_STOCK = "MENU_STOCK";
    private static final String MENU_STK_ORD = "MENU_STK_ORD";
    private static final String MENU_ORDER = "MENU_ORDER";
    private RecyclerView activityView;
    private Vector<ConfigureBO> menuDB = new Vector<>();
    private Vector<ConfigureBO> mTempMenuList = new Vector<>();
    private Toolbar toolbar;
    private Vector<ConfigureBO> menuWithSequence;
    BusinessModel bmodel;
    private int selecteditem = 0;
    private boolean isCreated;
    private ArrayAdapter<Integer> indicativeOrderAdapter;
    private static final HashMap<String, Integer> menuIcons = new HashMap<String, Integer>();
    private SubDHomeActivity.ActivityAdapter mActivityAdapter;
    private HashMap<String, String> menuCodeList = new HashMap<>();
    String menuCode = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sub_dhome);

        bmodel = (BusinessModel) this.getApplicationContext();
        bmodel.setContext(this);

        activityView = (RecyclerView) findViewById(R.id.activity_list);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        overridePendingTransition(R.anim.trans_left_in, R.anim.trans_left_out);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayUseLogoEnabled(false);
            setScreenTitle(bmodel.getRetailerMasterBO().getRetailerName());
        }

        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getSupportActionBar().setElevation(0);
        }

        prepareMenuIcons();

        if (bmodel.userMasterHelper.getUserMasterBO().getUserid() == 0) {
            Toast.makeText(this,
                    getResources().getString(R.string.sessionout_loginagain),
                    Toast.LENGTH_SHORT).show();
            finish();
        }

        // Load the HHTTable
        menuDB = bmodel.configurationMasterHelper
                .downloadNewActivityMenu(ConfigurationMasterHelper.MENU_SUBD);

        mTempMenuList = new Vector<>(menuDB);

        activityView = (RecyclerView) findViewById(R.id.activity_list);
        activityView.setHasFixedSize(true);
        activityView.setNestedScrollingEnabled(false);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        activityView.setLayoutManager(linearLayoutManager);


        mActivityAdapter = new ActivityAdapter(mTempMenuList);
        activityView.setAdapter(mActivityAdapter);

        if (savedInstanceState == null) {
            Bundle extras = getIntent().getExtras();
            if (extras != null) {
                boolean isMoveNext = extras.getBoolean("IsMoveNextActivity", false);
                String mCurrentMenuCode = extras.getString("CurrentActivityCode", "");
                if (isMoveNext) {
                    prepareMenusInOrder();
                    ConfigureBO mNextMenu = getNextActivity(mCurrentMenuCode);
                    if (mNextMenu != null) {
                        gotoNextActivity(mNextMenu, mNextMenu.getHasLink());
                    }
                }
            }
        }

    }

    private void prepareMenusInOrder() {
        menuWithSequence = new Vector<>();

        for (ConfigureBO menu : menuDB) {

            menuWithSequence.add(menu);

        }
    }

    private ConfigureBO getNextActivity(String mCurrentMenuCode) {
        int size = menuWithSequence.size();

        for (int i = 0; i < size; i++) {
            if (menuWithSequence.get(i).getConfigCode().equals(mCurrentMenuCode)) {
                if (i != (size - 1)) {
                    return menuWithSequence.get(i + 1);
                }
            }
        }
        return null;
    }

    private void prepareMenuIcons() {
        menuIcons.put(MENU_STOCK, R.drawable.activity_icon_stock_check);
        menuIcons.put(MENU_STK_ORD, R.drawable.activity_icon_order_taking);

    }

    public class ActivityAdapter extends RecyclerView.Adapter<SubDHomeActivity.ActivityAdapter.MyViewHolder> {

        private Vector<ConfigureBO> mActivityList;
        private ConfigureBO configTemp;
        private View itemView;

        public ActivityAdapter(Vector<ConfigureBO> mActivityList) {
            this.mActivityList = mActivityList;
        }

        @Override
        public SubDHomeActivity.ActivityAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.homescreentwo_listitem, parent, false);

            return new MyViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(final SubDHomeActivity.ActivityAdapter.MyViewHolder holder, final int position) {
            configTemp = mActivityList.get(position);

            holder.config = configTemp;

            holder.activityname.setText(configTemp.getMenuName());
            holder.menuCode = configTemp.getConfigCode();
            holder.hasLink = configTemp.getHasLink();

            Integer i = menuIcons.get(configTemp.getConfigCode());
            holder.iconIV.setImageResource(i);

            holder.icon_ll.setBackgroundResource(R.drawable.activity_icon_bg_normal);
            holder.iconIV.setColorFilter(Color.argb(0, 0, 0, 0));


            if (holder.config.isDone()) {
                holder.icon_ll.setBackgroundResource(R.drawable.activity_icon_bg_completed);
                holder.iconIV.setColorFilter(Color.argb(255, 255, 255, 255));

            } else {
                holder.icon_ll.setBackgroundResource(R.drawable.activity_icon_bg_normal);
                holder.iconIV.setColorFilter(Color.argb(0, 0, 0, 0));
            }

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //Toast.makeText(getApplicationContext(),position+"",Toast.LENGTH_SHORT).show();
                    if (!isCreated) {
                        isCreated = true;

                        gotoNextActivity(holder.config, holder.hasLink);
                    }
                }
            });

        }

        @Override
        public int getItemViewType(int position) {
            return position;
        }

        @Override
        public int getItemCount() {
            return mActivityList.size();
        }

        public class MyViewHolder extends RecyclerView.ViewHolder {
            public TextView activityname;
            public ImageView iconIV, img_arrow;
            public String menuCode;
            public int hasLink;
            public ConfigureBO config;
            public ListView childListView;
            public LinearLayout icon_ll;

            public MyViewHolder(View view) {
                super(view);
                iconIV = (ImageView) view.findViewById(R.id.list_item_icon_iv);
                icon_ll = (LinearLayout) view.findViewById(R.id.icon_ll);
                img_arrow = (ImageView) view.findViewById(R.id.img_arrow);
                activityname = (TextView) view.findViewById(R.id.activityName);
                childListView = (ListView) view.findViewById(R.id.childList);
                activityname.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
            }
        }
    }

    private boolean isPreviousDone(ConfigureBO config) {

        try {
            for (int i = 0; i < menuDB.size(); i++) {
                if (menuDB.get(i).getConfigCode()
                        .equals(config.getConfigCode())) {
                    Commons.print("prev" + menuDB.get(i).getConfigCode() + "i="
                            + i);
                    for (int j = 0; j < i; j++) {

                        if (menuDB.get(j).getMandatory() == 0
                                && !menuDB.get(j).isDone()
                                && menuDB.get(j).getHasLink() == 1) {

                            return false;
                        }

                    }
                    break;
                }
            }
            return true;
        } catch (Exception e) {
            return false;
        }

    }


    private void gotoNextActivity(ConfigureBO menu, int hasLink) {

        boolean isDeviatedStore = bmodel.getRetailerMasterBO().getIsDeviated()
                .equals("Y");// true deviated store,false not deviated store

        //location dialog show from store click
//        isLocDialogShow = false;


        // this conditon added to load download product
        // filter method once when GLOBAL CATEGORY SELECTION enabled
        if (bmodel.configurationMasterHelper.IS_GLOBAL_CATEGORY && bmodel.configurationMasterHelper.IS_FIVE_LEVEL_FILTER) {
            if (menu.getConfigCode().equals(MENU_STOCK)
                    || menu.getConfigCode().equals(MENU_STK_ORD)
                    && hasLink == 1) {
                if (bmodel.productHelper.getmLoadedGlobalProductId() != bmodel.productHelper.getmSelectedGlobalProductId()) {
                    bmodel.productHelper
                            .downloadFiveFilterLevels(MENU_STK_ORD);
                    bmodel.productHelper
                            .downloadProductsWithFiveLevelFilter(MENU_STK_ORD);
                }

            }
        }
        if (menu.getConfigCode().equals(MENU_STOCK)) {
            if (isPreviousDone(menu)
                    || bmodel.configurationMasterHelper.IS_JUMP) {


                bmodel.productHelper.downloadTaggedProducts(MENU_STOCK);

                /** Download location to load in the filter. **/
                bmodel.productHelper.downloadInStoreLocations();


                if (bmodel.configurationMasterHelper.IS_LOAD_STOCK_COMPETITOR) {
                    if (bmodel.configurationMasterHelper.SHOW_COMPETITOR_FILTER) {
                        bmodel.productHelper.downloadCompetitorFiveFilterLevels();
                    }
                    bmodel.productHelper.downloadCompetitorProducts(MENU_STOCK);
                    bmodel.productHelper.downloadCompetitorTaggedProducts(menu.getConfigCode());
                  /*  if (menu.getConfigCode().equals(MENU_COMBINED_STOCK))
                        bmodel.productHelper.downloadCompetitorTaggedProducts("MENU_COMB_STK");
                    else
                        bmodel.productHelper.downloadCompetitorTaggedProducts(menu.getConfigCode());
                */
                }

                if (bmodel.productHelper.getTaggedProducts().size() > 0) {
                 /*   if (bmodel.configurationMasterHelper.SHOW_STOCK_AVGDAYS && menu.getConfigCode().equals(MENU_COMBINED_STOCK))
                        bmodel.productHelper.loadRetailerWiseInventoryFlexQty();
*/
                    if (bmodel.configurationMasterHelper
                            .downloadFloatingSurveyConfig(MENU_STOCK)) {
                        SurveyHelperNew surveyHelperNew = SurveyHelperNew.getInstance(this);
                        surveyHelperNew.setFromHomeScreen(false);
                        surveyHelperNew.downloadModuleId("STANDARD");
                        surveyHelperNew.downloadQuestionDetails(MENU_STOCK);
                        surveyHelperNew.loadSurveyAnswers(0);
                    }

                    bmodel.configurationMasterHelper.downloadFloatingNPReasonWithPhoto(menu.getConfigCode());

                    if (bmodel.hasAlreadyStockChecked(bmodel.getRetailerMasterBO()
                            .getRetailerID())) {
                        bmodel.setEditStockCheck(true);
                        bmodel.loadStockCheckedProducts(bmodel
                                .getRetailerMasterBO().getRetailerID(), menu.getConfigCode());

                        if (bmodel.configurationMasterHelper.SHOW_NEAREXPIRY_IN_STOCKCHECK
                                && bmodel.configurationMasterHelper.IS_RETAIN_NEAREXPIRY_CURRENT_TRAN_IN_STOCKCHECK) {
                            NearExpiryTrackingHelper mNearExpiryHelper = NearExpiryTrackingHelper.getInstance(this);
                            mNearExpiryHelper.loadSKUTracking(getApplicationContext(), true);
                        }

                        if (bmodel.configurationMasterHelper.SHOW_PRICECHECK_IN_STOCKCHECK) {
                            PriceTrackingHelper priceTrackingHelper = PriceTrackingHelper.getInstance(this);
                            priceTrackingHelper.loadPriceTransaction(getApplicationContext());
                            if (bmodel.configurationMasterHelper.IS_PRICE_CHECK_RETAIN_LAST_VISIT_IN_EDIT_MODE && !priceTrackingHelper.isPriceCheckDone(getApplicationContext())) {
                                priceTrackingHelper.updateLastVisitPriceAndMRP();
                            }
                        }
                    } else if (bmodel.configurationMasterHelper.IS_STOCK_CHECK_RETAIN_LAST_VISIT_TRAN) {
                        // load last visit data
                        bmodel.loadLastVisitStockCheckedProducts(bmodel.getRetailerMasterBO().getRetailerID());
                    }

                    if (bmodel.configurationMasterHelper.IS_INITIATIVE) {
                        /** Following should load module wise **/
                        bmodel.initiativeHelper.loadLocalOrdersQty(bmodel
                                .getRetailerMasterBO().getRetailerID());
                        /**
                         * loadInitiativeProducts is not required to be called on
                         * every module
                         **/
                        bmodel.productHelper.loadInitiativeProducts();
                    }

                    /** Following is not required to be called in every module **/
                    bmodel.productHelper.loadRetailerWiseProductWisePurchased();

                    bmodel.outletTimeStampHelper.saveTimeStampModuleWise(
                            SDUtil.now(SDUtil.DATE_GLOBAL),
                            SDUtil.now(SDUtil.TIME), menu.getConfigCode());

                    /**
                     * Download product long-press information dialog
                     * configurations.
                     **/
                    bmodel.configurationMasterHelper.downloadProductDetailsList();

                    // Load Data for Special Filter
                    bmodel.configurationMasterHelper.downloadFilterList();
                    bmodel.productHelper.updateProductColor();


                    /** Load the screen **/
                    Intent intent;
                    intent = new Intent(SubDHomeActivity.this,
                            StockCheckActivity.class);
                    intent.putExtra("CurrentActivityCode", menu.getConfigCode());
//                    if (isFromChild)
//                        intent.putExtra("isFromChild", isFromChild);
                 /* *//*  if (menu.getConfigCode().equals(MENU_COMBINED_STOCK)) {
                        intent = new Intent(HomeScreenTwo.this,
                                CombinedStockFragmentActivity.class);
                        intent.putExtra("CurrentActivityCode", menu.getConfigCode());
                        if (isFromChild)
                            intent.putExtra("isFromChild", isFromChild);
                    }*//* else {
                        intent = new Intent(HomeScreenTwo.this,
                                StockCheckActivity.class);
                        intent.putExtra("CurrentActivityCode", menu.getConfigCode());
                        if (isFromChild)
                            intent.putExtra("isFromChild", isFromChild);
                    }*/
                    //intent.putExtra("screentitle", menu.getMenuName());
                    bmodel.mSelectedActivityName = menu.getMenuName();
                    startActivity(intent);
                    finish();

                } else {
                    dataNotMapped();
                    isCreated = false;
                    menuCode = (menuCodeList.get(menu.getConfigCode()) == null ? "" : menuCodeList.get(menu.getConfigCode()));
                    if (!menuCode.equals(menu.getConfigCode()))
                        menuCodeList.put(menu.getConfigCode(), menu.getConfigCode());
                }
            } else {
                Toast.makeText(
                        this,
                        getResources().getString(
                                R.string.please_complete_previous_activity),
                        Toast.LENGTH_SHORT).show();
                isCreated = false;
            }

        } else if (menu.getConfigCode().equals(MENU_STK_ORD)) {

            if (isPreviousDone(menu)
                    || bmodel.configurationMasterHelper.IS_JUMP) {

                OrderHelper orderHelper = OrderHelper.getInstance(this);

                if (bmodel.configurationMasterHelper.IS_RESTRICT_ORDER_TAKING
                        && (bmodel.getRetailerMasterBO().getRField4().equals("1")
                        || (bmodel.getRetailerMasterBO().getTinExpDate() != null && !bmodel.getRetailerMasterBO().getTinExpDate().isEmpty() && SDUtil.compareDate(SDUtil.now(SDUtil.DATE_GLOBAL), bmodel.getRetailerMasterBO().getTinExpDate(), "yyyy/MM/dd") > 0))) {
                    bmodel.showAlert(getResources().getString(R.string.order_not_allowed_for_retailer), 0);
                    isCreated = false;
                    return;
                }

                if (bmodel.configurationMasterHelper
                        .downloadFloatingSurveyConfig(MENU_STK_ORD)) {
                    SurveyHelperNew surveyHelperNew = SurveyHelperNew.getInstance(this);
                    surveyHelperNew.setFromHomeScreen(false);
                    surveyHelperNew.downloadModuleId("STANDARD");
                    surveyHelperNew.downloadQuestionDetails(MENU_STK_ORD);
                    surveyHelperNew.loadSurveyAnswers(0);
                }


                if (bmodel.configurationMasterHelper.IS_SUPPLIER_CREDIT_LIMIT
                        && !bmodel.configurationMasterHelper.IS_SUPPLIER_NOT_AVAILABLE
                        && bmodel.getRetailerMasterBO().getSupplierBO() != null &&
                        bmodel.getRetailerMasterBO().getSupplierBO().getCreditLimit() > 0) {
                    bmodel.getRetailerMasterBO().setCreditLimit(bmodel.getRetailerMasterBO().getSupplierBO().getCreditLimit());
                }

                if (bmodel.configurationMasterHelper.SHOW_SALES_RETURN_IN_ORDER) {
                    SalesReturnHelper salesReturnHelper = SalesReturnHelper.getInstance(this);
                    salesReturnHelper.loadSalesReturnConfigurations(getApplicationContext());
                    bmodel.reasonHelper.downloadSalesReturnReason();
                    if (bmodel.reasonHelper.getReasonSalesReturnMaster().size() > 0) {
                        bmodel.productHelper.cloneReasonMaster(true);
//
                        salesReturnHelper.getInstance(this).clearSalesReturnTable(true);
//
////                        if (!bmodel.configurationMasterHelper.IS_INVOICE) {
                        salesReturnHelper.getInstance(this).removeSalesReturnTable(true);
                        salesReturnHelper.getInstance(this).loadSalesReturnData(getApplicationContext(), "ORDER");
////                        }
                    }
                }

                if (bmodel.productHelper.getProductMaster().size() > 0) {
                    bmodel.configurationMasterHelper.downloadFloatingNPReasonWithPhoto(MENU_STK_ORD);
                    if (!bmodel.configurationMasterHelper.IS_VALIDATE_CREDIT_DAYS
                            || bmodel.getRetailerMasterBO().getCreditDays() == 0
                            || bmodel.productHelper.isCheckCreditPeriod()) {

                        /** Load the stock check if opened in edit mode. **/
                        bmodel.setEditStockCheck(false);
                        if (bmodel.hasAlreadyStockChecked(bmodel
                                .getRetailerMasterBO().getRetailerID())) {

                            bmodel.setEditStockCheck(true);
                            bmodel.loadStockCheckedProducts(bmodel
                                    .getRetailerMasterBO().getRetailerID(), menu.getConfigCode());


                            if (bmodel.configurationMasterHelper.IS_COMBINED_STOCK_CHECK_FROM_ORDER) {
                                if (bmodel.configurationMasterHelper.SHOW_NEAREXPIRY_IN_STOCKCHECK
                                        && bmodel.configurationMasterHelper.IS_RETAIN_NEAREXPIRY_CURRENT_TRAN_IN_STOCKCHECK) {
                                    NearExpiryTrackingHelper mNearExpiryHelper = NearExpiryTrackingHelper.getInstance(this);
                                    mNearExpiryHelper.loadSKUTracking(getApplicationContext(), false);
                                }

                                if (bmodel.configurationMasterHelper.SHOW_PRICECHECK_IN_STOCKCHECK) {
                                    PriceTrackingHelper priceTrackingHelper = PriceTrackingHelper.getInstance(this);
                                    priceTrackingHelper.loadPriceTransaction(getApplicationContext());
                                    if (bmodel.configurationMasterHelper.IS_PRICE_CHECK_RETAIN_LAST_VISIT_IN_EDIT_MODE && !priceTrackingHelper.isPriceCheckDone(getApplicationContext())) {
                                        priceTrackingHelper.updateLastVisitPriceAndMRP();
                                    }
                                }
                            }
                        }
                        bmodel.productHelper.setProductImageUrl();
                        bmodel.setEdit(false);
                        if (orderHelper.hasAlreadyOrdered(this, bmodel.getRetailerMasterBO()
                                .getRetailerID())) {
                            bmodel.setEdit(true);
                        }


                        if (bmodel.configurationMasterHelper.IS_SHOW_ORDERING_SEQUENCE) {
                            bmodel.productHelper.getmProductidOrderByEntry().clear();
                            bmodel.productHelper.getmProductidOrderByEntryMap().clear();
                        }

                        bmodel.productHelper.downloadIndicativeOrderList();//moved here to check size of indicative order
                        orderHelper.selectedOrderId = "";
                        if (bmodel.productHelper.getIndicativeList() != null
                                && bmodel.productHelper.getIndicativeList().size() < 1
                                && bmodel.configurationMasterHelper.IS_MULTI_STOCKORDER) {
                            if (bmodel.isEdit()) {
                                orderHelper.selectedOrderId = "";//cleared to avoid reuse of id
                                final String menuConfigCode = menu.getConfigCode();
                                final String menuName = menu.getMenuName();
                                OrderTransactionListDialog obj = new OrderTransactionListDialog(getApplicationContext(), SubDHomeActivity.this, new OrderTransactionListDialog.newOrderOnClickListener() {
                                    @Override
                                    public void onNewOrderButtonClick() {
                                        //the methods that were called during normal stock and order loading in non edit mode are called here
                                        //loadOrderedProducts,loadSerialNo,enableSchemeModule are used in edit mode so avoided here as in this case screen should be loaded fresh
                                        bmodel.setOrderHeaderBO(null);
                                        loadRequiredMethodsForStockAndOrder(menuConfigCode, menuName);
                                        loadstockorderscreen(menuConfigCode);
                                    }
                                }, new OrderTransactionListDialog.oldOrderOnClickListener() {
                                    @Override
                                    public void onOldOrderButtonClick(String id) {
                                        OrderHelper.getInstance(SubDHomeActivity.this).selectedOrderId = id;
                                        //the methods that were called during normal stock and order loading in edit mode are called here
                                        //selectedOrderId is passed to loadOrderedProducts method  to load ordered products for that id
                                        //loadSerialNo,enableSchemeModule included as these were called in edit mode
                                        OrderHelper.getInstance(SubDHomeActivity.this).loadOrderedProducts(SubDHomeActivity.this, bmodel.getRetailerMasterBO()
                                                .getRetailerID(), id);
                                        OrderHelper.getInstance(SubDHomeActivity.this).loadSerialNo(SubDHomeActivity.this);
                                        enableSchemeModule();
                                        loadRequiredMethodsForStockAndOrder(menuConfigCode, menuName);
                                        loadOrderSummaryScreen(menuConfigCode);
                                    }
                                });
                                obj.show();
                            } else {
                                //the methods that were called during normal stock and order loading in non edit mode are called here
                                //loadOrderedProducts,loadSerialNo,enableSchemeModule are used in edit mode so avoided here as in this case screen should be loaded fresh
                                bmodel.setOrderHeaderBO(null);
                                loadRequiredMethodsForStockAndOrder(menu.getConfigCode(), menu.getMenuName());
                                loadstockorderscreen(menu.getConfigCode());
                            }
                        } else {
                            //doubt
                            orderHelper.loadOrderedProducts(this, bmodel.getRetailerMasterBO()
                                    .getRetailerID(), null);
                            orderHelper.loadSerialNo(this);
                            enableSchemeModule();

                            loadRequiredMethodsForStockAndOrder(menu.getConfigCode(), menu.getMenuName());
                            if (bmodel.isEdit()) {
                                loadOrderSummaryScreen(menu.getConfigCode());

                            } else {
                                loadstockorderscreen(menu.getConfigCode());
                            }
                        }
                    } else {
                        Toast.makeText(
                                this,
                                getResources().getString(
                                        R.string.please_pay_old_invoice),
                                Toast.LENGTH_SHORT).show();
                        isCreated = false;
                    }

                } else {
                    dataNotMapped();
                    isCreated = false;
                    menuCode = (menuCodeList.get(menu.getConfigCode()) == null ? "" : menuCodeList.get(menu.getConfigCode()));
                    if (!menuCode.equals(menu.getConfigCode()))
                        menuCodeList.put(menu.getConfigCode(), menu.getConfigCode());
                }

            } else {
                Toast.makeText(
                        this,
                        getResources().getString(
                                R.string.please_complete_previous_activity),
                        Toast.LENGTH_SHORT).show();
                isCreated = false;
            }

        } else {
            isCreated = false;
        }

    }

    public void dataNotMapped() {
        bmodel.showAlert(
                getResources().getString(R.string.data_not_mapped),
                0);
    }


    private void loadRequiredMethodsForStockAndOrder(String configCode, String menuName) {
        try {

            if (bmodel.configurationMasterHelper.IS_GUIDED_SELLING) {
                bmodel.downloadGuidedSelling();
            }

            if (bmodel.configurationMasterHelper.SHOW_DISC_AMOUNT_ALLOW) {
                bmodel.collectionHelper.downloadDiscountSlab();
            }

            //  bmodel.productHelper.downloadProductFilter("MENU_STK_ORD"); /*03/09/2015*/
            bmodel.productHelper.loadRetailerWiseProductWisePurchased();
            bmodel.productHelper
                    .loadRetailerWiseProductWiseP4StockAndOrderQty();

            bmodel.configurationMasterHelper
                    .downloadProductDetailsList();

            if (bmodel.configurationMasterHelper.IS_INITIATIVE) {
                /** Load Initiative **/
                bmodel.productHelper.loadInitiativeProducts();
                bmodel.initiativeHelper.downloadInitiativeHeader(bmodel
                        .getRetailerMasterBO().getSubchannelid());
                /** Load Order History **/
                bmodel.initiativeHelper.loadLocalOrdersQty(bmodel
                        .getRetailerMasterBO().getRetailerID());
            }

            /** Load SO Norm **/
            if (bmodel.configurationMasterHelper.IS_SUGGESTED_ORDER) {
                bmodel.productHelper
                        .loadRetailerWiseInventoryOrderQty();
            }

            if (bmodel.configurationMasterHelper.IS_PRODUCT_DISPLAY_FOR_PIRAMAL)
                bmodel.productHelper.updateProductColorAndSequance();

            /** Settign color **/
            bmodel.configurationMasterHelper.downloadFilterList();
            bmodel.productHelper.updateProductColor();

            DiscountHelper discountHelper = DiscountHelper.getInstance(this);
            if (bmodel.configurationMasterHelper.SHOW_STORE_WISE_DISCOUNT_DLG) {

                discountHelper.downloadBillWiseDiscount(this);
                discountHelper.loadExistingBillWiseRangeDiscount(this);
            }
            // apply bill wise pay term discount
            discountHelper.downloadBillWisePayTermDiscount(this);

            bmodel.productHelper.downloadInStoreLocations();

            if (bmodel.configurationMasterHelper.IS_SCHEME_ON_MASTER)
                bmodel.schemeDetailsMasterHelper.loadSchemeHistoryDetails();

            //  if (bmodel.configurationMasterHelper.IS_SCHEME_ON) {
            bmodel.schemeDetailsMasterHelper.downloadOffInvoiceSchemeDetails();
            // }

            if (bmodel.configurationMasterHelper.SHOW_COLLECTION_BEFORE_INVOICE) {
                bmodel.collectionHelper.downloadBankDetails();
                bmodel.collectionHelper.downloadBranchDetails();
                bmodel.collectionHelper.loadCreditNote();
            }

            bmodel.updateProductUOM(StandardListMasterConstants.mActivityCodeByMenuCode.get(MENU_STK_ORD), 1);


            if (bmodel.configurationMasterHelper.IS_FORMAT_USING_CURRENCY_VALUE) {
                bmodel.downloadCurrencyConfig();
            }

        } catch (Exception e) {
            Commons.printException(e);
        }
        // Reset the Configuration if Directly goes from
        // HomeScreenTwo
        bmodel.mSelectedModule = -1;
        OrderSummary.mCurrentActivityCode = configCode;
        bmodel.mSelectedActivityName = menuName;
    }

    public void loadstockorderscreen(String menu) {
        {
            indicativeOrderAdapter = new ArrayAdapter<Integer>(this,
                    android.R.layout.select_dialog_singlechoice);

            for (Integer temp : bmodel.productHelper
                    .getIndicativeList())
                indicativeOrderAdapter.add(temp);
            if (bmodel.configurationMasterHelper.SHOW_INVOICE_CREDIT_BALANCE) {
                if (bmodel.getRetailerMasterBO()
                        .getCredit_balance() == -1
                        || bmodel.getRetailerMasterBO()
                        .getCredit_balance() > 0) {

                    if (bmodel.productHelper.getIndicativeList() != null) {
                        if (bmodel.productHelper.getIndicativeList().size() > 1) {
                            showIndicativeOrderFilterAlert(menu);
                            return;
                        }
                        if (bmodel.productHelper.getIndicativeList().size() > 0) {
                            if (!bmodel.productHelper.isAlreadyIndicativeOrderTaken(bmodel.productHelper.getIndicativeList().get(0))
                                    && !bmodel.isEdit()) {
                                bmodel.productHelper.downloadIndicativeOrder(bmodel.productHelper.getIndicativeList().get(0));
                                bmodel.productHelper.updateIndicateOrder();
                            }
                        }
                    }
                    bmodel.outletTimeStampHelper
                            .saveTimeStampModuleWise(
                                    SDUtil.now(SDUtil.DATE_GLOBAL),
                                    SDUtil.now(SDUtil.TIME),
                                    menu);

                    Intent intent = new Intent(SubDHomeActivity.this,
                            StockAndOrder.class);
                    startActivity(intent);
                    overridePendingTransition(R.anim.trans_left_in, R.anim.trans_left_out);
                    finish();
                } else {
                    showDialog(1);
                    isCreated = false;
                }
            } else {
                bmodel.outletTimeStampHelper
                        .saveTimeStampModuleWise(
                                SDUtil.now(SDUtil.DATE_GLOBAL),
                                SDUtil.now(SDUtil.TIME),
                                menu);

                if (bmodel.productHelper.getIndicativeList() != null) {
                    if (bmodel.productHelper.getIndicativeList().size() > 1) {
                        showIndicativeOrderFilterAlert(menu);
                        return;
                    }
                    if (bmodel.productHelper.getIndicativeList().size() > 0) {
                        if (!bmodel.productHelper.isAlreadyIndicativeOrderTaken(bmodel.productHelper.getIndicativeList().get(0))
                                && !bmodel.isEdit()) {
                            bmodel.productHelper.downloadIndicativeOrder(bmodel.productHelper.getIndicativeList().get(0));
                            bmodel.productHelper.updateIndicateOrder();
                        }
                    }
                }
//                            Intent intent = new Intent(HomeScreenTwo.this,
//                                    StockAndOrder.class);
                Intent intent;
                intent = new Intent(SubDHomeActivity.this,
                        StockAndOrder.class);

                startActivity(intent);
                finish();
            }
        }
    }

    private void enableSchemeModule() {
        if (bmodel.configurationMasterHelper.IS_SCHEME_ON) {
            if (bmodel.configurationMasterHelper.IS_SHOW_SELLER_DIALOG) {
                if (bmodel.configurationMasterHelper.IS_SIH_VALIDATION) {

                    bmodel.schemeDetailsMasterHelper.loadSchemeDetails(bmodel
                            .getRetailerMasterBO().getRetailerID());
                }
            } else {
                bmodel.schemeDetailsMasterHelper.loadSchemeDetails(bmodel
                        .getRetailerMasterBO().getRetailerID());
            }
        }
    }

    private void loadOrderSummaryScreen(String menuConfigCode) {
        Intent intent = new Intent(SubDHomeActivity.this,
                OrderSummary.class);
        intent.putExtra("ScreenCode", "MENU_STK_ORD");
        //intent.putExtra("ScreenCode", "MENU_STK_ORD");
        startActivity(intent);
        finish();
    }

    private void showIndicativeOrderFilterAlert(final String menuCode) {
        AlertDialog.Builder builder;
        builder = new AlertDialog.Builder(this);
        builder.setTitle(null);
        builder.setSingleChoiceItems(indicativeOrderAdapter, selecteditem,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int item) {
                        Integer selectedId = indicativeOrderAdapter
                                .getItem(item);
                        selecteditem = item;

                        if (!bmodel.productHelper.isAlreadyIndicativeOrderTaken(selectedId)
                                && !bmodel.isEdit()) {
                            bmodel.productHelper.downloadIndicativeOrder(selectedId);
                            bmodel.productHelper.updateIndicateOrder();
                        }
                        //      setImagefromCamera(mProductID, mTypeID);
                        if (menuCode.equals(ConfigurationMasterHelper.MENU_ORDER)) {
                            Intent i = new Intent(SubDHomeActivity.this,
                                    StockAndOrder.class);
                            i.putExtra("OrderFlag", "Nothing");
                            i.putExtra("ScreenCode",
                                    ConfigurationMasterHelper.MENU_ORDER);
                            startActivity(i);
                            overridePendingTransition(R.anim.trans_left_in, R.anim.trans_left_out);
                            finish();
                        } else {

                            Intent intent = new Intent(SubDHomeActivity.this,
                                    StockAndOrder.class);
                            startActivity(intent);
                            overridePendingTransition(R.anim.trans_left_in, R.anim.trans_left_out);
                            finish();
                        }
                        dialog.dismiss();


                    }
                });

        bmodel.applyAlertDialogTheme(builder);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                startActivity(new Intent(this, HomeScreenActivity.class));
                finish();
        }
        return super.onOptionsItemSelected(item);
    }
}
