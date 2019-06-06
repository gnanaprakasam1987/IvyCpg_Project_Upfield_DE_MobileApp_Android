package com.ivy.cpg.view.subd;

import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.ivy.cpg.view.collection.CollectionHelper;
import com.ivy.cpg.view.order.OrderHelper;
import com.ivy.cpg.view.order.OrderSummary;
import com.ivy.cpg.view.order.discount.DiscountHelper;
import com.ivy.cpg.view.order.scheme.SchemeDetailsMasterHelper;
import com.ivy.cpg.view.stockcheck.StockCheckHelper;
import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.bo.ConfigureBO;
import com.ivy.sd.png.commons.IvyBaseActivityNoActionBar;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.provider.ConfigurationMasterHelper;
import com.ivy.sd.png.provider.ProductTaggingHelper;
import com.ivy.sd.png.util.Commons;
import com.ivy.sd.png.util.StandardListMasterConstants;
import com.ivy.utils.DateTimeUtils;
import com.ivy.utils.FontUtils;

import java.util.HashMap;
import java.util.Vector;

public class SubDHomeActivity extends IvyBaseActivityNoActionBar {

    public static final String MENU_SUBD_STOCK = "MENU_SUBD_STOCK";
    private static final String MENU_SUBD_ORD = "MENU_SUBD_ORD";
    private Vector<ConfigureBO> menuDB = new Vector<>();
    private Vector<ConfigureBO> menuWithSequence;
    BusinessModel bmodel;
    private boolean isCreated;
    private static final HashMap<String, Integer> menuIcons = new HashMap<>();
    private HashMap<String, String> menuCodeList = new HashMap<>();
    String menuCode = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sub_dhome);

        bmodel = (BusinessModel) this.getApplicationContext();
        bmodel.setContext(this);


        Toolbar toolbar = findViewById(R.id.toolbar);
        overridePendingTransition(R.anim.trans_left_in, R.anim.trans_left_out);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayUseLogoEnabled(false);
            toolbar.setTitle(bmodel.getRetailerMasterBO().getRetailerName());
        }

        TextView tvRetailerName = findViewById(R.id.retailer_name);
        tvRetailerName.setText(bmodel.getRetailerMasterBO().getRetailerName());
        tvRetailerName.setTypeface(FontUtils.getFontBalooHai(this,FontUtils.FontType.REGULAR));

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

        Vector<ConfigureBO> mTempMenuList = new Vector<>(menuDB);

        RecyclerView  activityView = findViewById(R.id.activity_list);
        activityView.setHasFixedSize(true);
        activityView.setNestedScrollingEnabled(false);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        activityView.setLayoutManager(linearLayoutManager);


        ActivityAdapter mActivityAdapter = new ActivityAdapter(mTempMenuList);
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


        CollapsingToolbarLayout collapsingToolbar = findViewById(R.id.collapse_toolbar);

        toolbar.setTitle("");
        collapsingToolbar.setTitleEnabled(false);

        collapsingToolbar.setCollapsedTitleTextColor(Color.WHITE);
        collapsingToolbar.setCollapsedTitleTypeface(FontUtils.getFontBalooHai(this,FontUtils.FontType.REGULAR));
        collapsingToolbar.setExpandedTitleColor(Color.WHITE);
        collapsingToolbar.setExpandedTitleTypeface(FontUtils.getFontRoboto(this, FontUtils.FontType.REGULAR));

    }

    private void prepareMenusInOrder() {
        menuWithSequence = new Vector<>();

        menuWithSequence.addAll(menuDB);
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
        menuIcons.put(MENU_SUBD_STOCK, R.drawable.activity_icon_stock_check);
        menuIcons.put(MENU_SUBD_ORD, R.drawable.activity_icon_order_taking);

    }

    public class ActivityAdapter extends RecyclerView.Adapter<SubDHomeActivity.ActivityAdapter.MyViewHolder> {

        private Vector<ConfigureBO> mActivityList;
        private ConfigureBO configTemp;
        private View itemView;

        ActivityAdapter(Vector<ConfigureBO> mActivityList) {
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
            ImageView iconIV, img_arrow;
            public String menuCode;
            public int hasLink;
            public ConfigureBO config;
            ListView childListView;
            public LinearLayout icon_ll;

            public MyViewHolder(View view) {
                super(view);
                iconIV =  view.findViewById(R.id.list_item_icon_iv);
                icon_ll =  view.findViewById(R.id.icon_ll);
                img_arrow =  view.findViewById(R.id.img_arrow);
                activityname =  view.findViewById(R.id.activityName);
                childListView =  view.findViewById(R.id.childList);
                activityname.setTypeface(FontUtils.getFontRoboto(SubDHomeActivity.this, FontUtils.FontType.LIGHT));
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


        if (menu.getConfigCode().equals(MENU_SUBD_STOCK) && hasLink == 1) {
            if (isPreviousDone(menu)
                    || bmodel.configurationMasterHelper.IS_JUMP) {


                StockCheckHelper.getInstance(SubDHomeActivity.this).loadStockCheckConfiguration(SubDHomeActivity.this,bmodel.retailerMasterBO.getSubchannelid());

                ProductTaggingHelper.getInstance(this).downloadTaggedProducts(this,MENU_SUBD_STOCK);
                // Download location to load in the filter.
                bmodel.productHelper.downloadInStoreLocations();


                if (bmodel.configurationMasterHelper.IS_LOAD_STOCK_COMPETITOR) {
                    if (bmodel.configurationMasterHelper.SHOW_COMPETITOR_FILTER) {
                        bmodel.productHelper.downloadCompetitorFiveFilterLevels();
                    }
                    bmodel.productHelper.downloadCompetitorProducts(MENU_SUBD_STOCK);
                    ProductTaggingHelper.getInstance(this).downloadCompetitorTaggedProducts(this,menu.getConfigCode());
                }

                if (ProductTaggingHelper.getInstance(this).getTaggedProducts().size() > 0) {


                    if (bmodel.hasAlreadyStockChecked(bmodel.getRetailerMasterBO()
                            .getRetailerID())) {
                        bmodel.setEditStockCheck(true);
                        bmodel.loadStockCheckedProducts(bmodel
                                .getRetailerMasterBO().getRetailerID(), menu.getConfigCode());
                    }


                    bmodel.outletTimeStampHelper.saveTimeStampModuleWise(
                            DateTimeUtils.now(DateTimeUtils.DATE_GLOBAL),
                            DateTimeUtils.now(DateTimeUtils.TIME), menu.getConfigCode());


                    bmodel.configurationMasterHelper.downloadProductDetailsList();

                    // Load Data for Special Filter
                    bmodel.configurationMasterHelper.downloadFilterList();
                    bmodel.productHelper.updateProductColor();


                    //Load the screen
                    Intent intent;
                    intent = new Intent(SubDHomeActivity.this,
                            SubDStockCheckActivity.class);

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

        } else if (menu.getConfigCode().equals(MENU_SUBD_ORD) && hasLink == 1) {

            if (isPreviousDone(menu)
                    || bmodel.configurationMasterHelper.IS_JUMP) {

                OrderHelper orderHelper = OrderHelper.getInstance(this);


                if (bmodel.productHelper.getProductMaster().size() > 0) {

                    bmodel.productHelper.setProductImageUrl();
                    bmodel.setEdit(false);
                    if (orderHelper.hasAlreadyOrdered(this, bmodel.getRetailerMasterBO()
                            .getRetailerID())) {
                        bmodel.setEdit(true);
                    }
                    orderHelper.loadOrderedProducts(this, bmodel.getRetailerMasterBO()
                            .getRetailerID(), null);
                    orderHelper.loadSerialNo(this);

                    loadRequiredMethodsForStockAndOrder(menu.getConfigCode(), menu.getMenuName());

                    loadstockorderscreen(menu.getConfigCode());


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

            CollectionHelper collectionHelper = CollectionHelper.getInstance(SubDHomeActivity.this);

            if (bmodel.configurationMasterHelper.IS_GUIDED_SELLING) {
                bmodel.downloadGuidedSelling();
            }

            if (bmodel.configurationMasterHelper.SHOW_DISC_AMOUNT_ALLOW) {
                collectionHelper.downloadDiscountSlab();
            }

            //  bmodel.productHelper.downloadProductFilter("MENU_STK_ORD"); /*03/09/2015*/
            bmodel.productHelper.loadRetailerWiseProductWisePurchased();
            bmodel.productHelper
                    .loadRetailerWiseProductWiseP4StockAndOrderQty();

            bmodel.configurationMasterHelper
                    .downloadProductDetailsList();

            if (bmodel.configurationMasterHelper.IS_INITIATIVE) {
                // Load Initiative
                bmodel.productHelper.loadInitiativeProducts();
                bmodel.initiativeHelper.downloadInitiativeHeader(bmodel
                        .getRetailerMasterBO().getSubchannelid());
                // Load Order History
                bmodel.initiativeHelper.loadLocalOrdersQty(bmodel
                        .getRetailerMasterBO().getRetailerID());
            }

            // Load SO Norm
            if (bmodel.configurationMasterHelper.IS_SUGGESTED_ORDER) {
                bmodel.productHelper
                        .loadRetailerWiseInventoryOrderQty();
            }

            if (bmodel.configurationMasterHelper.IS_PRODUCT_DISPLAY_FOR_PIRAMAL)
                bmodel.productHelper.updateProductColorAndSequance();

            // Settign color
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

            SchemeDetailsMasterHelper schemeHelper=SchemeDetailsMasterHelper.getInstance(getApplicationContext());
            if (schemeHelper.IS_SCHEME_ON_MASTER)
                schemeHelper.downloadSchemeHistoryDetails(getApplicationContext(),bmodel.getRetailerMasterBO().getRetailerID(),false,"");

            schemeHelper.downloadOffInvoiceSchemeDetails(getApplicationContext(),bmodel.getRetailerMasterBO().getRetailerID());

            if (bmodel.configurationMasterHelper.SHOW_COLLECTION_BEFORE_INVOICE) {
                collectionHelper.downloadBankDetails();
                collectionHelper.downloadBranchDetails();
                collectionHelper.downloadRetailerAccountDetails();
                collectionHelper.loadCreditNote();
            }

            bmodel.updateProductUOM(StandardListMasterConstants.mActivityCodeByMenuCode.get(MENU_SUBD_ORD), 1);


            if (bmodel.configurationMasterHelper.IS_FORMAT_USING_CURRENCY_VALUE) {
                bmodel.downloadCurrencyConfig();
            }

        } catch (Exception e) {
            Commons.printException(e);
        }

        OrderSummary.mCurrentActivityCode = configCode;
        bmodel.mSelectedActivityName = menuName;
    }

    public void loadstockorderscreen(String menu) {
        {

            bmodel.outletTimeStampHelper
                    .saveTimeStampModuleWise(
                            DateTimeUtils.now(DateTimeUtils.DATE_GLOBAL),
                            DateTimeUtils.now(DateTimeUtils.TIME),
                            menu);

            Intent intent;
            intent = new Intent(SubDHomeActivity.this,
                    SubDStockOrderActivity.class);
            startActivity(intent);
            finish();

        }
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                //startActivity(new Intent(this, HomeScreenActivity.class));
                finish();
        }
        return super.onOptionsItemSelected(item);
    }
}
