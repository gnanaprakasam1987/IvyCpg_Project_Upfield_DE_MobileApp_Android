package com.ivy.cpg.view.order;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.Paint;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.CompoundButtonCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.AppCompatCheckBox;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.InputFilter;
import android.text.InputType;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.ivy.cpg.view.digitalcontent.DigitalContentActivity;
import com.ivy.cpg.view.digitalcontent.DigitalContentHelper;
import com.ivy.cpg.view.initiative.InitiativeActivity;
import com.ivy.cpg.view.order.discount.DiscountHelper;
import com.ivy.cpg.view.order.moq.MOQHighlightActivity;
import com.ivy.cpg.view.order.productdetails.ProductSchemeDetailsActivity;
import com.ivy.cpg.view.order.scheme.QPSSchemeApply;
import com.ivy.cpg.view.order.scheme.SchemeApply;
import com.ivy.cpg.view.order.scheme.SchemeDetailsActivity;
import com.ivy.cpg.view.order.scheme.SchemeDetailsMasterHelper;
import com.ivy.cpg.view.order.scheme.UpSellingActivity;
import com.ivy.cpg.view.price.PriceTrackingHelper;
import com.ivy.cpg.view.salesreturn.SalesReturnEntryActivity;
import com.ivy.cpg.view.salesreturn.SalesReturnHelper;
import com.ivy.cpg.view.salesreturn.SalesReturnReasonBO;
import com.ivy.cpg.view.stockcheck.CombinedStockDetailActivity;
import com.ivy.cpg.view.stockcheck.StockCheckHelper;
import com.ivy.cpg.view.survey.SurveyActivityNew;
import com.ivy.lib.Utils;
import com.ivy.lib.existing.DBUtil;
import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.bo.AttributeBO;
import com.ivy.sd.png.bo.ConfigureBO;
import com.ivy.sd.png.bo.GuidedSellingBO;
import com.ivy.sd.png.bo.LevelBO;
import com.ivy.sd.png.bo.OrderHeader;
import com.ivy.sd.png.bo.ProductMasterBO;
import com.ivy.sd.png.bo.ProductTaggingBO;
import com.ivy.sd.png.bo.SchemeBO;
import com.ivy.sd.png.bo.SchemeProductBO;
import com.ivy.sd.png.bo.StandardListBO;
import com.ivy.sd.png.commons.IvyBaseActivityNoActionBar;
import com.ivy.sd.png.commons.SDUtil;
import com.ivy.sd.png.model.BrandDialogInterface;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.model.FiveLevelFilterCallBack;
import com.ivy.sd.png.model.ProductSearchCallBack;
import com.ivy.sd.png.provider.ConfigurationMasterHelper;
import com.ivy.sd.png.provider.SBDHelper;
import com.ivy.sd.png.provider.SynchronizationHelper;
import com.ivy.sd.png.util.CommonDialog;
import com.ivy.sd.png.util.Commons;
import com.ivy.sd.png.util.DataMembers;
import com.ivy.sd.png.view.BatchAllocation;
import com.ivy.sd.png.view.CustomKeyBoard;
import com.ivy.sd.png.view.FilterFiveFragment;
import com.ivy.sd.png.view.HomeScreenTwo;
import com.ivy.sd.png.view.MustSellReasonDialog;
import com.ivy.sd.png.view.OrderDiscount;
import com.ivy.sd.png.view.ReasonPhotoDialog;
import com.ivy.sd.png.view.RemarksDialog;
import com.ivy.sd.png.view.SlantView;
import com.ivy.sd.png.view.SpecialFilterFragment;
import com.ivy.utils.DateTimeUtils;
import com.ivy.utils.FontUtils;
import com.ivy.utils.view.OnSingleClickListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Vector;

import static com.ivy.cpg.view.order.moq.MOQHighlightActivity.MOQ_RESULT_CODE;

public class StockAndOrder extends IvyBaseActivityNoActionBar implements OnClickListener,
        BrandDialogInterface, FiveLevelFilterCallBack, ProductSearchCallBack {

    private ListView lvwplist;

    private TextView totalValueText;
    private TextView lpcText;
    private TextView distValue;
    private BusinessModel bmodel;
    private Vector<ProductMasterBO> mylist=new Vector<>();
    private EditText QUANTITY;

    private String append = "";
    LinearLayout ll_spl_filter, ll_tab_selection;
    private DrawerLayout mDrawerLayout;


    private ArrayList<String> mSearchTypeArray = new ArrayList<>();
    private InputMethodManager inputManager;

    private static final String BRAND = "Brand";
    private static final String GENERAL = "General";

    // Selected spl filter will be maintained in this hasmap. This will max one record.
    private final HashMap<String, String> mSelectedFilterMap = new HashMap<>();


    private MustSellReasonDialog dialog;
    /**
     * ourIntentAction is receiver for ET1 scanning
     */
    private static final String ourIntentAction = "com.ivy.sd.png.asean.view.RECVR";
    private static final String DATA_STRING_TAG = "com.motorolasolutions.emdk.datawedge.data_string";
    private String strBarCodeSearch = "ALL";

    // Intent Values
    private String OrderedFlag;
    private String screenCode;
    private final String ORDER_FLAG = "OrderFlag";
    private final String SCREEN_CODE = "ScreenCode";
    private final String TEMP_PO = "tempPo";
    private final String TEMP_REMARK = "tempRemark";
    private final String TEMP_RFIELD1 = "tempRField1";
    private final String TEMP_RFIELD2 = "tempRField2";
    private final String TEMP_ORDDERIMG = "tempOrdImg";
    private final String TEMP_ADDRESSID = "tempAddressId";
    private double totalvalue = 0;



    private ArrayAdapter<StandardListBO> mLocationAdapter;
    private int mSelectedLocationIndex;
    private MyAdapter mSchedule;
    private String tempPo;
    private String tempRemark;
    private String tempRField1;
    private String tempRField2;
    private String tempOrdImg;
    private int tempAddressId;
    private HashMap<Integer, Integer> mSelectedIdByLevelId;
    private LevelBO mSelectedLevelBO = new LevelBO();
    private HashMap<Integer, Vector<LevelBO>> loadedFilterValues;
    private Vector<LevelBO> sequence;
    private FilterAdapter filterAdapter;
    private RecyclerView rvFilterList;
    private int mTotalScreenWidth = 0;
    private String strProductObj;
    private int SbdDistPre = 0; // Dist stock
    private int sbdDistAchieved = 0;
    private Button mBtnNext;
    private Button mBtnGuidedSelling_next, mBtnGuidedSelling_prev;

    private Toolbar toolbar;


    private LinkedList<String> mProductList = new LinkedList<>();

    private final String mStockCode = "STK";
    private final String mOrderCode = "ORD";
    private int totalAllQty = 0;
    private float totalWeight = 0;

    private boolean isFilter = true;// only for guided selling. Default value is true, so it will ot affect normal flow
    private TextView totalQtyTV;
    private TextView totalWeightTV;

    private String totalOrdCount;

    private Vector<ProductMasterBO> productList = new Vector<>();


    private OrderHelper orderHelper;

    private static final int SALES_RETURN = 3;
    private static final int REQUEST_CODE_UPSELLING = 4;


    private AlertDialog alertDialog;

    private wareHouseStockBroadCastReceiver mWareHouseStockReceiver;
    private StockCheckHelper stockCheckHelper;
    private ProductSearch productSearch;

    private String mSelectedSpecialFilter;
    private int mSelectedProductId;
    private ArrayList<Integer> mSelectedAttributeProductIds;
    private int mSelectedFilter;

    CustomKeyBoard dialogCustomKeyBoard =null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_stock_and_order);

        bmodel = (BusinessModel) getApplicationContext();
        bmodel.setContext(this);
        overridePendingTransition(R.anim.trans_left_in, R.anim.trans_left_out);

        orderHelper = OrderHelper.getInstance(this);
        stockCheckHelper = StockCheckHelper.getInstance(this);
        productSearch=new ProductSearch(this,productList,bmodel,ProductSearch.SCREEN_CODE_ORDER);

        if (bmodel.configurationMasterHelper.SHOW_BARCODE)
            checkAndRequestPermissionAtRunTime(2);

        if (bmodel.configurationMasterHelper.IS_VOICE_TO_TEXT > -1 )
            checkAndRequestPermissionAtRunTime(4);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        Bundle extras = getIntent().getExtras();
        OrderedFlag = "MENU_STK_ORD";
        screenCode = "MENU_STK_ORD";
        if (savedInstanceState == null) {
            if (extras != null) {
                OrderedFlag = extras.getString(ORDER_FLAG) == null ? OrderedFlag
                        : extras.getString(ORDER_FLAG);
                screenCode = extras.getString(SCREEN_CODE) == null ? screenCode
                        : extras.getString(SCREEN_CODE);
                tempPo = extras.getString(TEMP_PO) == null ? "" : extras
                        .getString(TEMP_PO);
                tempRemark = extras.getString(TEMP_REMARK) == null ? ""
                        : extras.getString(TEMP_REMARK);
                tempRField1 = extras.getString(TEMP_RFIELD1) == null ? ""
                        : extras.getString(TEMP_RFIELD1);
                tempRField2 = extras.getString(TEMP_RFIELD2) == null ? ""
                        : extras.getString(TEMP_RFIELD2);
                tempOrdImg = extras.getString(TEMP_ORDDERIMG) == null ? ""
                        : extras.getString(TEMP_ORDDERIMG);
                tempAddressId = extras.getInt(TEMP_ADDRESSID);

            }
        } else {
            OrderedFlag = (String) (savedInstanceState
                    .getSerializable(ORDER_FLAG) == null ? OrderedFlag
                    : savedInstanceState.getSerializable(ORDER_FLAG));
            screenCode = (String) (savedInstanceState
                    .getSerializable(SCREEN_CODE) == null ? screenCode
                    : savedInstanceState.getSerializable(SCREEN_CODE));
            tempPo = (String) (savedInstanceState.getSerializable(TEMP_PO) == null ? ""
                    : savedInstanceState.getSerializable(TEMP_PO));
            tempRemark = (String) (savedInstanceState
                    .getSerializable(TEMP_REMARK) == null ? ""
                    : savedInstanceState.getSerializable(TEMP_REMARK));
            tempRField1 = (String) (savedInstanceState
                    .getSerializable(TEMP_RFIELD1) == null ? ""
                    : savedInstanceState.getSerializable(TEMP_RFIELD1));
            tempRField2 = (String) (savedInstanceState
                    .getSerializable(TEMP_RFIELD2) == null ? ""
                    : savedInstanceState.getSerializable(TEMP_RFIELD2));
            tempOrdImg = (String) (savedInstanceState
                    .getSerializable(TEMP_ORDDERIMG) == null ? ""
                    : savedInstanceState.getSerializable(TEMP_ORDDERIMG));
            tempAddressId = (int) (savedInstanceState
                    .getSerializable(TEMP_ADDRESSID) == null ? 0
                    : savedInstanceState.getSerializable(TEMP_ADDRESSID));

        }

        FrameLayout drawer = (FrameLayout) findViewById(R.id.right_drawer);

        int width = getResources().getDisplayMetrics().widthPixels;
        DrawerLayout.LayoutParams params = (android.support.v4.widget.DrawerLayout.LayoutParams) drawer.getLayoutParams();
        params.width = width;
        drawer.setLayoutParams(params);

        mDrawerLayout =  findViewById(R.id.drawer_layout);
        // set a custom shadow that overlays the main content when the drawer
        // opens
        mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow,
                GravityCompat.START);
        mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow,
                GravityCompat.END);

        mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);

        inputManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);




        mBtnNext = (Button) findViewById(R.id.btn_next);
        mBtnGuidedSelling_next = (Button) findViewById(R.id.btn_guided_selling_next);
        mBtnGuidedSelling_prev = (Button) findViewById(R.id.btn_guided_selling_prev);
        mBtnGuidedSelling_next.setOnClickListener(this);
        mBtnGuidedSelling_prev.setOnClickListener(this);







        mBtnNext.setTypeface(FontUtils.getFontBalooHai(StockAndOrder.this, FontUtils.FontType.REGULAR));
        mBtnNext.setOnClickListener(new OnSingleClickListener() {
            @Override
            public void onSingleClick(View v) {

                if (bmodel.configurationMasterHelper.IS_ENABLE_LICENSE_VALIDATION) {
                    boolean isDrugLicenseExpired = false;
                    LinkedList<ProductMasterBO> mOrderedProductList = new LinkedList<>();
                    for (int j = 0; j < bmodel.productHelper.getProductMaster().size(); ++j) {
                        ProductMasterBO product = bmodel.productHelper.getProductMaster().get(j);
                        if (product.getOrderedPcsQty() > 0 || product.getOrderedCaseQty() > 0 ||
                                product.getOrderedOuterQty() > 0) {
                            mOrderedProductList.add(product);
                        }
                    }
                    if (bmodel.productHelper.isDrugOrder(mOrderedProductList) && bmodel.productHelper.isDLDateExpired()) {
                        isDrugLicenseExpired = true;
                    }
                    if (isDrugLicenseExpired) {
                        if (!bmodel.configurationMasterHelper.IS_SOFT_LICENSE_VALIDATION) {
                            bmodel.showAlert(getResources().getString(R.string.drug_license_expired), 0);
                            return;
                        } else {
                            Toast.makeText(StockAndOrder.this, getResources().getString(R.string.drug_license_expired), Toast.LENGTH_SHORT).show();
                        }
                    }
                }
                if (bmodel.configurationMasterHelper.IS_SR_VALIDATE_BY_RETAILER_TYPE) {
                    updatesalesReturnValue();
                    if (bmodel.retailerMasterBO.getRpTypeCode() != null && bmodel.retailerMasterBO.getRpTypeCode().equals("CASH")) {
                        if (!orderHelper.returnReplacementAmountValidation(true, true, StockAndOrder.this)) {
                            onnext();
                        } else {
                            Toast.makeText(StockAndOrder.this, getResources().getString(R.string.return_products_not_matching_replacing_product_price), Toast.LENGTH_SHORT).show();
                        }
                    } else if (bmodel.retailerMasterBO.getRpTypeCode() != null && bmodel.retailerMasterBO.getRpTypeCode().equals("CREDIT")) {
                        if (orderHelper.returnReplacementAmountValidation(false, true, StockAndOrder.this)) {
                            onnext();
                        } else {
                            Toast.makeText(StockAndOrder.this, getResources().getString(R.string.return_products_price_less_than_replacing_product_price), Toast.LENGTH_SHORT).show();
                        }
                    } else
                        onnext();
                } else
                    onnext();


            }
        });
        ((TextView) findViewById(R.id.totalText)).setTypeface(FontUtils.getFontRoboto(StockAndOrder.this, FontUtils.FontType.LIGHT));
        ((TextView) findViewById(R.id.totalValue)).setTypeface(FontUtils.getFontRoboto(StockAndOrder.this, FontUtils.FontType.LIGHT));
        ((TextView) findViewById(R.id.lpc_title)).setTypeface(FontUtils.getFontRoboto(StockAndOrder.this, FontUtils.FontType.LIGHT));
        ((TextView) findViewById(R.id.lcp)).setTypeface(FontUtils.getFontRoboto(StockAndOrder.this, FontUtils.FontType.LIGHT));
        ((TextView) findViewById(R.id.distText)).setTypeface(FontUtils.getFontRoboto(StockAndOrder.this, FontUtils.FontType.LIGHT));
        ((TextView) findViewById(R.id.distValue)).setTypeface(FontUtils.getFontRoboto(StockAndOrder.this, FontUtils.FontType.LIGHT));
        ((TextView) findViewById(R.id.title_totalweigh)).setTypeface(FontUtils.getFontRoboto(StockAndOrder.this, FontUtils.FontType.LIGHT));
        ((TextView) findViewById(R.id.tv_totalweigh)).setTypeface(FontUtils.getFontRoboto(StockAndOrder.this, FontUtils.FontType.LIGHT));

        if (!bmodel.configurationMasterHelper.SHOW_LPC_ORDER) {
            findViewById(R.id.ll_lpc).setVisibility(View.GONE);
        }

        if (bmodel.configurationMasterHelper.HIDE_ORDER_DIST) {
            findViewById(R.id.ll_dist).setVisibility(View.GONE);
        }

        if (bmodel.configurationMasterHelper.SHOW_ORDER_WEIGHT) {
            findViewById(R.id.ll_totweight).setVisibility(View.VISIBLE);
            try {
                if (bmodel.labelsMasterHelper.applyLabels(findViewById(
                        R.id.title_totalweigh).getTag()) != null)
                    ((TextView) findViewById(R.id.title_totalweigh))
                            .setText(bmodel.labelsMasterHelper
                                    .applyLabels(findViewById(
                                            R.id.title_totalweigh).getTag()));
            } catch (Exception e) {
                Commons.printException(e + "");
            }
        }

        if (bmodel.configurationMasterHelper.IS_ENABLE_PRODUCT_TAGGING_VALIDATION) {
            int mContentLevel = bmodel.productHelper.getContentLevel(bmodel.getContext(), "MENU_STK_ORD");
            bmodel.productHelper.getTaggingDetails("MAX_ORD_VAL", mContentLevel); //MAX_ORD_VAL
        }

        String title;
        if ("MENU_ORDER".equals(screenCode))
            title = bmodel.configurationMasterHelper
                    .getHomescreentwomenutitle("MENU_ORDER");
        else
            title = bmodel.configurationMasterHelper
                    .getHomescreentwomenutitle("MENU_STK_ORD");

        if (title.isEmpty())
            title = getResources().getString(R.string.order);

        if (toolbar != null) {

            setSupportActionBar(toolbar);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            setScreenTitle(title);
//            // Used to on / off the back arrow icon
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
//           // Used to remove the app logo actionbar icon and set title as home
//          // (title support click)
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }


        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        getSupportActionBar().setDisplayShowHomeEnabled(true);



        // ActionBarDrawerToggle ties together the the proper interactions
        // between the sliding drawer and the action bar app icon
        ActionBarDrawerToggle mDrawerToggle = new ActionBarDrawerToggle(this, /* host Activity */
                mDrawerLayout,
                R.string.ok,
                R.string.close
        ) {
            public void onDrawerClosed(View view) {

                supportInvalidateOptionsMenu();


                if(mSelectedFilter==1)
                    productSearch.startSpecialFilterSearch(productList,mSelectedSpecialFilter);
                else if(mSelectedFilter==2)
                productSearch.startSearch(productList,mSelectedProductId,mSelectedAttributeProductIds);


            }

            public void onDrawerOpened(View drawerView) {
                try {
                    setScreenTitle(getResources().getString(R.string.filter));
                } catch (Exception e) {
                    setScreenTitle(getResources().getString(R.string.filter));
                }

                supportInvalidateOptionsMenu();
            }
        };
        mDrawerLayout.addDrawerListener(mDrawerToggle);


        if (bmodel.configurationMasterHelper.IS_REMOVE_TAX_ON_SRP) {
            bmodel.resetSRPvalues();
        }

        // load location filter
        mLocationAdapter = new ArrayAdapter<>(this,
                android.R.layout.select_dialog_singlechoice);

        for (StandardListBO temp : bmodel.productHelper.getInStoreLocation())
            mLocationAdapter.add(temp);
        if (bmodel.configurationMasterHelper.IS_GLOBAL_LOCATION) {
            mSelectedLocationIndex = bmodel.productHelper.getmSelectedGLobalLocationIndex();
        }


        totalValueText = (TextView) findViewById(R.id.totalValue);
        lpcText = (TextView) findViewById(R.id.lcp);
        distValue = (TextView) findViewById(R.id.distValue);
        totalQtyTV = (TextView) findViewById(R.id.tv_totalqty);
        totalWeightTV = (TextView) findViewById(R.id.tv_totalweigh);
        rvFilterList = (RecyclerView) findViewById(R.id.rvFilter);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext(), LinearLayoutManager.HORIZONTAL, false);
        rvFilterList.setLayoutManager(mLayoutManager);
        rvFilterList.setItemAnimator(new DefaultItemAnimator());

        hideAndSeek();

        (findViewById(R.id.calcdot))
                .setVisibility(View.VISIBLE);




        lvwplist = (ListView) findViewById(R.id.list);
        lvwplist.setCacheColorHint(0);

        SBDHelper.getInstance(this).calculateSBDDistribution(getApplicationContext()); //sbd calculation
        productList = filterWareHouseProducts();
        if (bmodel.configurationMasterHelper.IS_ENABLE_PRODUCT_TAGGING_VALIDATION) {
            setTaggingDetails();
        }
        /* Calculate the SBD Dist Acheivement value */
        loadSBDAchievementLocal();
        /* Calculate the total and LPC value */
        updateValue();

        mSearchTypeArray = new ArrayList<>();
        mSearchTypeArray.add(getResources().getString(R.string.all));
        mSearchTypeArray.add(getResources().getString(R.string.product_name));
        mSearchTypeArray.add(getResources().getString(R.string.prod_code));
        mSearchTypeArray.add(getResources().getString(
                R.string.order_dialog_barcode));

        prepareScreen();


        DisplayMetrics dm = new DisplayMetrics();
        getWindow().getWindowManager().getDefaultDisplay().getMetrics(dm);
        mTotalScreenWidth = dm.widthPixels;

        if (bmodel.configurationMasterHelper.SHOW_STORE_WISE_DISCOUNT_DLG) {
            DiscountHelper.getInstance(this).setMinimumRangeAsBillWiseDiscount();
        }

        mDrawerLayout.closeDrawer(GravityCompat.END);
    }

    private void prepareScreen() {
        try {
            if ("FromSummary".equals(OrderedFlag)) {
                if (bmodel.configurationMasterHelper.SHOW_SPL_FILTER && !bmodel.configurationMasterHelper.SHOW_SPL_FLIER_NOT_NEEDED
                        && !bmodel.configurationMasterHelper.IS_SHOW_ALL_SKU_ON_EDIT) {

                    mSelectedFilterMap.put("General", productSearch.mOrdered);
                    if (bmodel.configurationMasterHelper.IS_SPL_FILTER_TAB) {
                        loadSpecialFilterView();
                        productSearch.startSpecialFilterSearch(productList,productSearch.mOrdered);
                        selectTab(productSearch.mOrdered);
                    } else {
                        productSearch.startSpecialFilterSearch(productList,productSearch.mOrdered);
                    }
                } else {
                    mSelectedFilterMap.put("General", GENERAL);
                    productSearch.startSpecialFilterSearch(productList,GENERAL);
                }

                mBtnGuidedSelling_next.setVisibility(View.GONE);
                mBtnGuidedSelling_prev.setVisibility(View.GONE);
                mBtnNext.setVisibility(View.VISIBLE);
            } else {

                if (bmodel.configurationMasterHelper.IS_GUIDED_SELLING) {
                    //By default, setting first level as a current logic
                    if (bmodel.getmGuidedSelling().size() > 0) {
                        bmodel.getmGuidedSelling().get(0).setCurrent(true);
                    }
                    updateGuidedSellingView(true, false);
                } else {
                    if (bmodel.configurationMasterHelper.SHOW_SPL_FILTER && !bmodel.configurationMasterHelper.SHOW_SPL_FLIER_NOT_NEEDED) {
                        String defaultfilter = productSearch.getDefaultSpecialFilter();
                        if (!"".equals(defaultfilter)) {
                            mSelectedFilterMap.put("General", defaultfilter);
                            if (bmodel.configurationMasterHelper.IS_SPL_FILTER_TAB) {
                                loadSpecialFilterView();
                                productSearch.startSpecialFilterSearch(productList,defaultfilter);
                                selectTab(defaultfilter);
                            } else {
                                productSearch.startSpecialFilterSearch(productList,defaultfilter);

                            }


                        } else {
                            mSelectedFilterMap.put("General", GENERAL);
                            if (bmodel.configurationMasterHelper.IS_SPL_FILTER_TAB) {
                                loadSpecialFilterView();
                                productSearch.startSpecialFilterSearch(productList,GENERAL);
                                selectTab(bmodel.configurationMasterHelper.getGenFilter().get(0).getConfigCode());
                            } else {
                                productSearch.startSpecialFilterSearch(productList,GENERAL);
                            }


                        }


                        //

                    } else {
                        mSelectedFilterMap.put("General", GENERAL);
                        productSearch.startSpecialFilterSearch(productList,GENERAL);
                    }
                }
            }
        } catch (Exception e) {
            Commons.printException(e + "");
        }

    }

    private void updateGuidedSellingView(boolean isCreateView, boolean isPrevious) {
        mBtnGuidedSelling_next.setVisibility(View.VISIBLE);
        mBtnNext.setVisibility(View.GONE);
        if (bmodel.getmGuidedSelling().size() > 0) {
            // Get previous sequence
            int prevSequance = 0;
            if (isPrevious)
                prevSequance = getPreviousSequance();
            boolean isAllDone = true;
            for (int position = 0; position < bmodel.getmGuidedSelling().size(); position++) {
                GuidedSellingBO bo = bmodel.getmGuidedSelling().get(position);
                if (bo.isCurrent() || (isPrevious && bo.getSequance() == prevSequance)) {
                    // checking for product availability..
                    if (!bo.getFilterCode().equals(productSearch.mSuggestedOrder) || (bo.getFilterCode().equals(productSearch.mSuggestedOrder) && isProductsAvailable(bo.getFilterCode()))) {
                        //in case of specialfilter as a tab
                        if (bo.isProductFilter() || bmodel.configurationMasterHelper.SHOW_SPL_FILTER) {
                            if (bmodel.configurationMasterHelper.IS_SPL_FILTER_TAB) {
                                if (isCreateView) {
                                    loadSpecialFilterView();
                                } else {
                                    findViewById(R.id.hscrl_spl_filter).setVisibility(View.VISIBLE);
                                }
                            } else {
                                findViewById(R.id.hscrl_spl_filter).setVisibility(View.GONE);
                            }
                        } else {
                            findViewById(R.id.hscrl_spl_filter).setVisibility(View.GONE);
                        }
                        // incase of menu item
                        if (!bo.isProductFilter()) {
                            isFilter = false;
                        } else {
                            isFilter = true;
                        }
                        getSupportActionBar().invalidateOptionsMenu();

                        if (bo.getFilterCode().equalsIgnoreCase("ALL")) {
                            mSelectedFilterMap.put("General", GENERAL);
                            productSearch.startSpecialFilterSearch(productList,GENERAL);
                            if (bmodel.configurationMasterHelper.IS_SPL_FILTER_TAB)
                                selectTab(bmodel.configurationMasterHelper.getGenFilter().get(0).getConfigCode());
                        } else {
                            mSelectedFilterMap.put("General", bo.getFilterCode());
                            productSearch.startSpecialFilterSearch(productList,bo.getFilterCode());
                            if (bmodel.configurationMasterHelper.IS_SPL_FILTER_TAB)
                                selectTab(bo.getFilterCode());
                        }
                        setCurrentFlag(bo);
                        isAllDone = false;
                        if (position > 0) {
                            mBtnGuidedSelling_prev.setVisibility(View.VISIBLE);
                        } else {
                            mBtnGuidedSelling_prev.setVisibility(View.GONE);
                        }
                        break;
                    } else {
                        // No products..
                        bo.setDone(true);
                        if (isPrevious) {
                            setCurrentFlag(bo);
                            updateGuidedSellingView(false, isPrevious);
                            return;
                        } else {
                            if (bmodel.getmGuidedSelling().get(position + 1) != null) {
                                setCurrentFlag(bmodel.getmGuidedSelling().get(position + 1));
                            } else {
                                //last level has no product..
                                // so all levels are done.. calling on next..
                                onnext();
                            }
                        }
                    }
                }
            }
            if (isAllDone) {// in case if all guided selling logic done, all products should be loaded
                if (bmodel.configurationMasterHelper.SHOW_SPL_FILTER) {
                    mSelectedFilterMap.put("General", GENERAL);
                    if (bmodel.configurationMasterHelper.IS_SPL_FILTER_TAB) {
                        loadSpecialFilterView();
                        productSearch.startSpecialFilterSearch(productList,GENERAL);
                        selectTab(bmodel.configurationMasterHelper.getGenFilter().get(0).getConfigCode());
                    } else {
                        productSearch.startSpecialFilterSearch(productList,GENERAL);
                    }
                } else {
                    mSelectedFilterMap.put("General", GENERAL);
                    productSearch.startSpecialFilterSearch(productList,GENERAL);
                }
            }
        } else {
            mSelectedFilterMap.put("General", GENERAL);
            productSearch.startSpecialFilterSearch(productList,GENERAL);
        }
    }


    private boolean isProductsAvailable(String filterCode) {
        try {
            if (filterCode.equalsIgnoreCase("ALL") && bmodel.productHelper.getProductMaster().size() > 0) {
                return true;
            } else {
                for (ProductMasterBO bo : bmodel.productHelper.getProductMaster()) {
                    if (productSearch.isSpecialFilterAppliedProduct(filterCode, bo)) {
                        return true;
                    }
                }
            }
        } catch (Exception ex) {
            Commons.printException(ex);
        }
        return false;
    }

    private int getPreviousSequance() {
        int prevSequance = 0;
        for (int position = 0; position < bmodel.getmGuidedSelling().size(); position++) {
            GuidedSellingBO bo = bmodel.getmGuidedSelling().get(position);
            if (bo.isCurrent() && position > 0) {
                prevSequance = bmodel.getmGuidedSelling().get(position - 1).getSequance();
            }
        }
        return prevSequance;
    }

    private void setCurrentFlag(GuidedSellingBO bo) {
        for (GuidedSellingBO guidedSellingBO : bmodel.getmGuidedSelling()) {
            if (bo.getSequance() == guidedSellingBO.getSequance()) {
                guidedSellingBO.setCurrent(true);
            } else {
                guidedSellingBO.setCurrent(false);
            }
        }
    }

    private void updateGuidedSellingStatus(GuidedSellingBO bo) {
        if (bo.getSubActivity().equals(mStockCode)) {
            if (isCurrentLogicForStockDone(bo.getFilterCode(), bo.getApplyLevel())) {
                bo.setDone(true);
            } else
                bo.setDone(false);
        } else if (bo.getSubActivity().equals(mOrderCode)) {
            if (isCurrentLogicForOrderDone(bo.getFilterCode(), bo.getApplyLevel())) {
                bo.setDone(true);
            } else
                bo.setDone(false);
        } else {
            // To skip,in case of other codes mapped
            bo.setDone(true);
        }
    }

    private boolean isCurrentLogicForStockDone(String filterCode, String applyLevel) {
        if (filterCode.equals("ALL")) {
            if (applyLevel.equals("ALL")) {
                for (ProductMasterBO product : bmodel.productHelper.getProductMaster()) {
                    for (int j = 0; j < product.getLocations().size(); j++) {
                        if ((stockCheckHelper.SHOW_STOCK_SP && product.getLocations().get(j).getShelfPiece() < 0)
                                || (stockCheckHelper.SHOW_STOCK_SC && product.getLocations().get(j).getShelfCase() < 0)
                                || (stockCheckHelper.SHOW_SHELF_OUTER && product.getLocations().get(j).getShelfOuter() < 0)
                                || (stockCheckHelper.SHOW_STOCK_CB && product.getLocations().get(j).getAvailability() < 0)) {
                            return false;
                        }
                    }
                }
                return true;
            } else if (applyLevel.equals("ANY")) {
                //ANY
                boolean isStockChecked = true;
                for (ProductMasterBO product : bmodel.productHelper.getProductMaster()) {
                    for (int j = 0; j < product.getLocations().size(); j++) {
                        isStockChecked = false;
                        if ((stockCheckHelper.SHOW_STOCK_SP && product.getLocations().get(j).getShelfPiece() > -1)
                                || (stockCheckHelper.SHOW_STOCK_SC && product.getLocations().get(j).getShelfCase() > -1)
                                || (stockCheckHelper.SHOW_SHELF_OUTER && product.getLocations().get(j).getShelfOuter() > -1)
                                || (stockCheckHelper.SHOW_STOCK_CB && product.getLocations().get(j).getAvailability() > -1)) {
                            return true;
                        }
                    }
                }
                return isStockChecked;
            } else {
                return true;
            }
        } else {
            if (applyLevel.equals("ALL")) {
                boolean isStockChecked = true;
                for (ProductMasterBO product : bmodel.productHelper.getProductMaster()) {
                    if (productSearch.isSpecialFilterAppliedProduct(filterCode, product) && product.getIsSaleable() == 1) {
                        isStockChecked = false;
                        for (int j = 0; j < product.getLocations().size(); j++) {
                            if ((stockCheckHelper.SHOW_STOCK_SP && product.getLocations().get(j).getShelfPiece() > -1)
                                    || (stockCheckHelper.SHOW_STOCK_SC && product.getLocations().get(j).getShelfCase() > -1)
                                    || (stockCheckHelper.SHOW_SHELF_OUTER && product.getLocations().get(j).getShelfOuter() > -1)
                                    || (stockCheckHelper.SHOW_STOCK_CB && product.getLocations().get(j).getAvailability() > -1)) {
                                isStockChecked = true;
                            }
                        }
                        if (!isStockChecked) {
                            return false;
                        }
                    }
                }
                return isStockChecked;
                //filtered list have 0 products not allowed to navigate
            } else if (applyLevel.equals("ANY")) {
                //ANY
                boolean isStockChecked = true;
                for (ProductMasterBO product : bmodel.productHelper.getProductMaster()) {
                    if (productSearch.isSpecialFilterAppliedProduct(filterCode, product) && product.getIsSaleable() == 1) {
                        isStockChecked = false;
                        for (int j = 0; j < product.getLocations().size(); j++) {
                            if ((stockCheckHelper.SHOW_STOCK_SP && product.getLocations().get(j).getShelfPiece() > -1)
                                    || (stockCheckHelper.SHOW_STOCK_SC && product.getLocations().get(j).getShelfCase() > -1)
                                    || (stockCheckHelper.SHOW_SHELF_OUTER && product.getLocations().get(j).getShelfOuter() > -1)
                                    || (stockCheckHelper.SHOW_STOCK_CB && product.getLocations().get(j).getAvailability() > -1)) {
                                return true;
                            }
                        }
                    }
                }
                return isStockChecked;
            } else {
                return true;
            }
        }
    }

    private boolean isCurrentLogicForOrderDone(String filterCode, String applyLevel) {
        if (filterCode.equals("ALL")) {
            if (applyLevel.equals("ALL")) {
                for (ProductMasterBO product : bmodel.productHelper.getProductMaster()) {
                    if (product.getOrderedCaseQty() <= 0 && product.getOrderedPcsQty() <= 0 && product.getOrderedOuterQty() <= 0) {
                        return false;
                    }
                    if (bmodel.configurationMasterHelper.IS_ENABLE_PRODUCT_TAGGING_VALIDATION && !checkTaggingDetails(product)) {
                        Toast.makeText(StockAndOrder.this, product.getProductName() + " exceeded Allocation", Toast.LENGTH_LONG).show();
                        return false;
                    }
                }
                return false;
            } else if (applyLevel.equals("ANY")) {
                //ANY
                for (ProductMasterBO product : bmodel.productHelper.getProductMaster()) {
                    if (product.getOrderedCaseQty() > 0 || product.getOrderedPcsQty() > 0 || product.getOrderedOuterQty() > 0) {
                        if (bmodel.configurationMasterHelper.IS_ENABLE_PRODUCT_TAGGING_VALIDATION && !checkTaggingDetails(product)) {
                            Toast.makeText(StockAndOrder.this, product.getProductName() + " exceeded Allocation", Toast.LENGTH_LONG).show();
                            return false;
                        }
                        return true;
                    }
                }
                return false;
            } else {
                return true;
            }
        } else {
            if (applyLevel.equals("ALL")) {
                for (ProductMasterBO product : bmodel.productHelper.getProductMaster()) {
                    if (productSearch.isSpecialFilterAppliedProduct(filterCode, product)) {
                        if (product.getOrderedCaseQty() <= 0 && product.getOrderedPcsQty() <= 0 && product.getOrderedOuterQty() <= 0) {
                            return false;
                        }
                        if (bmodel.configurationMasterHelper.IS_ENABLE_PRODUCT_TAGGING_VALIDATION && !checkTaggingDetails(product)) {
                            Toast.makeText(StockAndOrder.this, product.getProductName() + " exceeded Allocation", Toast.LENGTH_LONG).show();
                            return false;
                        }
                    }
                }
                return false;
            } else if (applyLevel.equals("ANY")) {
                //ANY
                for (ProductMasterBO product : bmodel.productHelper.getProductMaster()) {
                    if (productSearch.isSpecialFilterAppliedProduct(filterCode, product)) {
                        if (product.getOrderedCaseQty() > 0 || product.getOrderedPcsQty() > 0 || product.getOrderedOuterQty() > 0) {
                            if (bmodel.configurationMasterHelper.IS_ENABLE_PRODUCT_TAGGING_VALIDATION && !checkTaggingDetails(product)) {
                                Toast.makeText(StockAndOrder.this, product.getProductName() + " exceeded Allocation", Toast.LENGTH_LONG).show();
                                return false;
                            }
                            return true;
                        }
                    }
                }
                return false;
            } else {
                return true;
            }
        }
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState) {
        Bundle extras = getIntent().getExtras();
        OrderedFlag = "MENU_STK_ORD";
        screenCode = "MENU_STK_ORD";
        if (extras != null) {
            OrderedFlag = extras.getString(ORDER_FLAG) == null ? OrderedFlag
                    : extras.getString(ORDER_FLAG);
            screenCode = extras.getString(SCREEN_CODE) == null ? screenCode
                    : extras.getString(SCREEN_CODE);
            tempPo = extras.getString(TEMP_PO) == null ? "" : extras
                    .getString(TEMP_PO);
            tempRemark = extras.getString(TEMP_REMARK) == null ? "" : extras
                    .getString(TEMP_REMARK);
            tempRField1 = extras.getString(TEMP_RFIELD1) == null ? "" : extras
                    .getString(TEMP_RFIELD1);
            tempRField2 = extras.getString(TEMP_RFIELD2) == null ? "" : extras
                    .getString(TEMP_RFIELD2);
            tempOrdImg = extras.getString(TEMP_ORDDERIMG) == null ? "" : extras
                    .getString(TEMP_ORDDERIMG);
            tempAddressId = extras.getInt(TEMP_ADDRESSID);

            savedInstanceState.putSerializable(ORDER_FLAG, OrderedFlag);
            savedInstanceState.putSerializable(TEMP_PO, tempPo);
            savedInstanceState.putSerializable(TEMP_REMARK, tempRemark);
            savedInstanceState.putSerializable(TEMP_RFIELD1, tempRField1);
            savedInstanceState.putSerializable(TEMP_RFIELD2, tempRField2);
            savedInstanceState.putString(TEMP_ORDDERIMG, tempOrdImg);
            savedInstanceState.putSerializable(TEMP_ADDRESSID, tempAddressId);
            savedInstanceState.putSerializable(SCREEN_CODE, screenCode);

        }
        super.onSaveInstanceState(savedInstanceState);
    }


    //update screen title

    private void updateScreenTitle() {
        String title;

        if ("MENU_ORDER".equals(screenCode))
            title = bmodel.configurationMasterHelper
                    .getHomescreentwomenutitle("MENU_ORDER");
        else
            title = bmodel.configurationMasterHelper
                    .getHomescreentwomenutitle("MENU_STK_ORD");

        if (totalOrdCount.equals("0"))
            setScreenTitle(title + " ("
                    + mylist.size() + ")");
        else
            setScreenTitle(title + " ("
                    + totalOrdCount + "/" + mylist.size() + ")");


    }


    /**
     * This method will on/off the items based in the configuration.
     */
    private void hideAndSeek() {
        try {

            if (bmodel.configurationMasterHelper.SHOW_CUSTOM_KEYBOARD_NEW) {
                findViewById(R.id.card_keyboard).setVisibility(View.GONE);
            } else {
                findViewById(R.id.card_keyboard).setVisibility(View.VISIBLE);
            }


        } catch (Exception e) {
            Commons.printException(e + "");
        }
    }

    @Override
    protected void onStart() {
        super.onStart();


        Commons.print("OnStart Called");
        if (bmodel.configurationMasterHelper.IS_TOP_ORDER_FILTER) {
            loadedFilterValues = bmodel.productHelper.getFilterProductsByLevelId();
            sequence = bmodel.productHelper.getFilterProductLevels();

            if (loadedFilterValues != null) {
                if (loadedFilterValues.get(-1) == null) {
                    if (bmodel.productHelper.getmAttributesList() != null && bmodel.productHelper.getmAttributesList().size() > 0) {
                        int newAttributeId = 0;
                        for (AttributeBO bo : bmodel.productHelper.getmAttributeTypes()) {
                            newAttributeId -= 1;
                            sequence.add(new LevelBO(bo.getAttributeTypename(), newAttributeId, -1));
                            Vector<LevelBO> lstAttributes = new Vector<>();
                            LevelBO attLevelBO;
                            for (AttributeBO attrBO : bmodel.productHelper.getmAttributesList()) {
                                attLevelBO = new LevelBO();
                                if (bo.getAttributeTypeId() == attrBO.getAttributeLovId()) {
                                    attLevelBO.setProductID(attrBO.getAttributeId());
                                    attLevelBO.setLevelName(attrBO.getAttributeName());
                                    lstAttributes.add(attLevelBO);
                                }
                            }
                            loadedFilterValues.put(newAttributeId, lstAttributes);

                        }

                    }
                }
            }


            if (sequence == null) {
                sequence = new Vector<LevelBO>();
            }

            if (mSelectedIdByLevelId == null || mSelectedIdByLevelId.size() == 0) {
                mSelectedIdByLevelId = new HashMap<>();

                for (LevelBO levelBO : sequence) {

                    mSelectedIdByLevelId.put(levelBO.getProductID(), 0);
                }
            }

            if (!sequence.isEmpty()) {
                mSelectedLevelBO = sequence.get(0);
                int levelID = sequence.get(0).getProductID();
                Vector<LevelBO> filterValues = new Vector<>();
                filterValues.addAll(loadedFilterValues.get(levelID));
                filterAdapter = new FilterAdapter(filterValues);
                rvFilterList.setAdapter(filterAdapter);
            }
        } else {
            rvFilterList.setVisibility(View.GONE);
        }

        mDrawerLayout.closeDrawer(GravityCompat.END);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Commons.print("OnResume Called");
        bmodel = (BusinessModel) getApplicationContext();
        bmodel.setContext(this);
        switchProfile();

        if (bmodel.configurationMasterHelper.IS_DOWNLOAD_WAREHOUSE_STOCK) {
            registerReceiver();
        }
    }

    private class MyAdapter extends ArrayAdapter<ProductMasterBO> {
        private final Vector<ProductMasterBO> items;
        private final int SOLogic;

        public MyAdapter(Vector<ProductMasterBO> items) {
            super(StockAndOrder.this,
                    R.layout.activity_stock_and_order_listview_new, items);
            this.items = items;
            SOLogic = bmodel.configurationMasterHelper.getSOLogic();
        }

        public ProductMasterBO getItem(int position) {
            return items.get(position);
        }

        public long getItemId(int position) {
            return position;
        }

        public int getCount() {
            return items.size();
        }

        @Override
        public int getItemViewType(int position) {
            return items.get(position).getIsSaleable()==1?0:1;
        }

        @Override
        public int getViewTypeCount() {
            if(bmodel.configurationMasterHelper.SHOW_NON_SALABLE_PRODUCT){
                return 2;
            }
            else return 1;
        }

        @SuppressLint("RestrictedApi")
        public View getView(final int position, View convertView,
                            ViewGroup parent) {
            final ViewHolder holder;
            final ProductMasterBO product = items.get(position);

            int viewType=getItemViewType(position);

            View row = convertView;
            if (row == null) {

                final LayoutInflater inflater = getLayoutInflater();
                holder = new ViewHolder();

                if(viewType==0) {
                    //Configuration based row rendering
                    if (bmodel.configurationMasterHelper.IS_STK_ORD_BS)
                        row = inflater.inflate(
                                R.layout.activity_stock_and_order_listview_gmi, parent,
                                false);
                    else if (bmodel.configurationMasterHelper.IS_STK_ORD_PROJECT)
                        row = inflater.inflate(
                                R.layout.order_listview_project, parent,
                                false);
                    else
                        row = inflater.inflate(
                                R.layout.activity_stock_and_order_listview_new, parent,
                                false);

                }
                else {
                    row = inflater.inflate(
                            R.layout.order_listview_non_salable_product, parent,
                            false);
                }

                (row.findViewById(R.id.view_dotted_line)).setLayerType(View.LAYER_TYPE_SOFTWARE, null);
                initializeRowViews(row,holder,SOLogic,viewType);




                row.setTag(holder);
            } else {
                holder = (ViewHolder) row.getTag();
            }

            holder.productObj = product;
            holder.productId = holder.productObj.getProductID();


            try {
                holder.psname.setTextColor(product.getTextColor());
            } catch (Exception e) {
                Commons.printException(e);
                holder.psname.setTextColor(ContextCompat.getColor(getApplicationContext(),
                        android.R.color.black));
            }
            holder.psname.setText(holder.productObj.getProductShortName());
            holder.pname = holder.productObj.getProductName();
            holder.tvbarcode.setText(holder.productObj.getBarCode());

            if(viewType==0) {


                if (bmodel.configurationMasterHelper.IS_PRODUCT_DISPLAY_FOR_PIRAMAL) {
                    //for piramal
                    try {
                        if (product.getColorCode() != null)
                            holder.psname.setTextColor(Color.parseColor(product.getColorCode()));
                        else
                            holder.psname.setTextColor(product.getTextColor());
                    } catch (Exception e) {
                        Commons.printException(e);
                        holder.psname.setTextColor(ContextCompat.getColor(getApplicationContext(),
                                android.R.color.black));

                    }
                }



                if (bmodel.configurationMasterHelper.IS_SHOW_SKU_CODE) {
                    String prodCode = getResources().getString(R.string.prod_code) + ": " +
                            holder.productObj.getProductCode() + " ";
                    if (bmodel.labelsMasterHelper.applyLabels(holder.tvProductCode.getTag()) != null)
                        prodCode = bmodel.labelsMasterHelper
                                .applyLabels(holder.tvProductCode.getTag()) + ": " +
                                holder.productObj.getProductCode() + " ";
                    holder.tvProductCode.setText(prodCode);
                }


                if (bmodel.configurationMasterHelper.SHOW_ORDER_CASE && holder.productObj.getCaseSize() > 0) {
                    String label = holder.caseTitleText + "(" + holder.productObj.getCaseSize() + getResources().getQuantityString(R.plurals.pcs, holder.productObj.getCaseSize()) + ")";
                    ((TextView) row.findViewById(R.id.caseTitle)).setText(label);
                }

                if (bmodel.configurationMasterHelper.SHOW_STK_ORD_MRP) {
                    String strMrp = getResources().getString(R.string.mrp)
                            + ": " + bmodel.formatValue(holder.productObj.getMRP());
                    holder.mrp.setText(strMrp);
                }

                if (bmodel.configurationMasterHelper.SHOW_INDICATIVE_ORDER) {
                    String strFlexOc = getString(R.string.io) + ": ";

                    if (bmodel.labelsMasterHelper.applyLabels(holder.indicativeOrder_oc.getTag()) != null)
                        strFlexOc = (bmodel.labelsMasterHelper
                                .applyLabels(holder.indicativeOrder_oc.getTag())) + ": ";

                    strFlexOc += holder.productObj.getIndicative_flex_oc() + "";

                    holder.indicativeOrder_oc.setText(strFlexOc);
                }

                if (bmodel.configurationMasterHelper.SHOW_CLEANED_ORDER) {
                    String strOrderOc = getString(R.string.co) + ": ";

                    if (bmodel.labelsMasterHelper.applyLabels(holder.cleanedOrder_oc.getTag()) != null)
                        strOrderOc = bmodel.labelsMasterHelper
                                .applyLabels(holder.cleanedOrder_oc.getTag()) + ": ";

                    strOrderOc += holder.productObj.getIndicativeOrder_oc() + "";

                    holder.cleanedOrder_oc.setText(strOrderOc);
                }

                String strPPQ = "";
                if (bmodel.labelsMasterHelper
                        .applyLabels("ppq") != null) {
                    strPPQ = bmodel.labelsMasterHelper
                            .applyLabels("ppq") + ": "
                            + holder.productObj.getRetailerWiseProductWiseP4Qty() + "";
                } else {
                    strPPQ = getResources().getString(R.string.ppq) + ": "
                            + holder.productObj.getRetailerWiseProductWiseP4Qty() + "";
                }
                holder.ppq.setText(strPPQ);


                String strPSQ = "";
                if (bmodel.labelsMasterHelper
                        .applyLabels("psq") != null) {
                    strPSQ = bmodel.labelsMasterHelper
                            .applyLabels("psq") + ": "
                            + holder.productObj.getRetailerWiseP4StockQty() + "";
                } else {
                    strPSQ = getResources().getString(R.string.psq) + ": "
                            + holder.productObj.getRetailerWiseP4StockQty();
                }
                holder.psq.setText(strPSQ);


                String strMSQty = "";
                if (bmodel.labelsMasterHelper
                        .applyLabels("msq") != null) {
                    strMSQty = bmodel.labelsMasterHelper
                            .applyLabels("msq") + ": "
                            + holder.productObj.getMSQty() + "";
                } else {
                    strMSQty = getResources().getString(R.string.msq) + ": "
                            + holder.productObj.getMSQty() + "";
                }
                holder.msq.setText(strMSQty);

                if (bmodel.configurationMasterHelper.IS_MOQ_ENABLED) {
                    String moqTitle = getString(R.string.moq) + ": ";

                    if (bmodel.labelsMasterHelper
                            .applyLabels("moq") != null) {
                        moqTitle = bmodel.labelsMasterHelper.applyLabels("moq") + ": ";
                    }

                    holder.moq.setText(moqTitle + holder.productObj.getRField1());
                }


                //set store stock qty
                if (stockCheckHelper.SHOW_STOCK_SC
                        || !screenCode.equals(ConfigurationMasterHelper.MENU_ORDER)) {
                    int strShelfCase = holder.productObj.getLocations()
                            .get(mSelectedLocationIndex).getShelfCase();
                    if (strShelfCase >= 0) {
                        holder.shelfCaseQty.setText(strShelfCase + "");
                    } else {
                        holder.shelfCaseQty.setText("");
                    }
                }
                if (stockCheckHelper.SHOW_STOCK_SP
                        || !screenCode.equals(ConfigurationMasterHelper.MENU_ORDER)) {
                    int strShelfPiece = holder.productObj.getLocations()
                            .get(mSelectedLocationIndex).getShelfPiece();

                    if (strShelfPiece >= 0) {
                        holder.shelfPcsQty.setText(strShelfPiece + "");
                    } else {
                        holder.shelfPcsQty.setText("");
                    }
                }
                if (stockCheckHelper.SHOW_SHELF_OUTER
                        || !screenCode.equals(ConfigurationMasterHelper.MENU_ORDER)) {

                    int strShelfOuter = holder.productObj.getLocations()
                            .get(mSelectedLocationIndex).getShelfOuter();
                    if (strShelfOuter >= 0) {
                        holder.shelfouter.setText(strShelfOuter + "");
                    } else {
                        holder.shelfouter.setText("");
                    }
                }

                if (bmodel.configurationMasterHelper.SHOW_SALES_RETURN_IN_ORDER
                        || bmodel.configurationMasterHelper.SHOW_SALES_RETURN_TV_IN_ORDER) {
                    int total = 0;
                    if (product.getSalesReturnReasonList() != null) {
                        for (SalesReturnReasonBO obj : product.getSalesReturnReasonList())
                            total = total + obj.getPieceQty() + (obj.getCaseQty() * obj.getCaseSize()) + (obj.getOuterQty() * obj.getOuterSize());
                    }
                    String strTotal = Integer.toString(total);
                    holder.salesReturn.setText(strTotal);

                    String srQtyTv = "";
                    if (bmodel.labelsMasterHelper
                            .applyLabels("sales_return") != null) {
                        srQtyTv = bmodel.labelsMasterHelper
                                .applyLabels("sales_return") + ": "
                                + strTotal + "";
                    } else {
                        srQtyTv = getResources().getString(R.string.sr) + ": "
                                + strTotal;
                    }
                    holder.sales_return_tv.setText(srQtyTv);
                }

                if (holder.productObj.getLocations()
                        .get(mSelectedLocationIndex).getAvailability() == 1) {
                    holder.imageButton_availability.setChecked(true);
                    CompoundButtonCompat.setButtonTintList(holder.imageButton_availability, ColorStateList.valueOf(ContextCompat.getColor(StockAndOrder.this, R.color.colorAccent)));

                } else if (holder.productObj.getLocations()
                        .get(mSelectedLocationIndex).getAvailability() == 0) {
                    holder.imageButton_availability.setChecked(true);
                    CompoundButtonCompat.setButtonTintList(holder.imageButton_availability, ColorStateList.valueOf(ContextCompat.getColor(StockAndOrder.this, R.color.RED)));
                } else if (holder.productObj.getLocations()
                        .get(mSelectedLocationIndex).getAvailability() == -1) {
                    holder.imageButton_availability.setChecked(false);
                    CompoundButtonCompat.setButtonTintList(holder.imageButton_availability, ColorStateList.valueOf(ContextCompat.getColor(StockAndOrder.this, R.color.checkbox_default_color)));
                }

                // set SO value
                if (bmodel.configurationMasterHelper.IS_SUGGESTED_ORDER) {
                    String soPieceTitle = getString(R.string.so) + ": ";

                    if (bmodel.labelsMasterHelper.applyLabels(holder.so.getTag()) != null)
                        soPieceTitle = bmodel.labelsMasterHelper
                                .applyLabels(holder.so.getTag()) + ": ";

                    if (bmodel.configurationMasterHelper.SHOW_SO_SPLIT) {
                        String soCaseTitle = getString(R.string.so_case);

                        if (bmodel.labelsMasterHelper.applyLabels(holder.socs.getTag()) != null)
                            soCaseTitle = bmodel.labelsMasterHelper
                                    .applyLabels(holder.socs.getTag()) + ": ";


                        int soQty = holder.productObj.getSoInventory()
                                + (holder.productObj.getSocInventory()
                                * holder.productObj.getCaseSize());
                        if ((soQty < holder.productObj
                                .getCaseSize())
                                || holder.productObj.getCaseSize() == 0) {
                            holder.socs.setText(soCaseTitle + "0");
                            String strInventory = holder.productObj.getSoInventory()
                                    + "";
                            holder.so.setText(soPieceTitle + strInventory);
                        } else if (soQty == holder.productObj
                                .getCaseSize()) {
                            String strSocs = soQty / holder.productObj
                                    .getCaseSize()
                                    + "";
                            holder.socs.setText(soCaseTitle + strSocs);
                            holder.so.setText(soPieceTitle + "0");
                        } else {
                            String strSocs = soQty / holder.productObj
                                    .getCaseSize()
                                    + "";
                            holder.socs.setText(soCaseTitle + strSocs);
                            String strSo = soQty % holder.productObj
                                    .getCaseSize()
                                    + "";
                            holder.so.setText(soPieceTitle + strSo);
                        }
                    } else {
                        String strSoi = holder.productObj.getSoInventory() + "";
                        holder.so.setText(soPieceTitle + strSoi);
                    }
                }


                //set SIH value
                if (bmodel.configurationMasterHelper.IS_STOCK_IN_HAND) {
                    String sihPSTitle = getString(R.string.sih) + ": ";

                    if (bmodel.labelsMasterHelper.applyLabels(holder.sih.getTag()) != null)
                        sihPSTitle = bmodel.labelsMasterHelper.applyLabels(holder.sih.getTag());

                    if (bmodel.configurationMasterHelper.SHOW_SIH_SPLIT) {
                        String sihCSTitle = getString(R.string.sih_case) + ": ";
                        String sihOUTitle = getString(R.string.sih_outer) + ": ";

                        if (bmodel.labelsMasterHelper.applyLabels(holder.sihCase.getTag()) != null)
                            sihCSTitle = bmodel.labelsMasterHelper.applyLabels(holder.sihCase.getTag());

                        if (bmodel.labelsMasterHelper.applyLabels(holder.sihOuter.getTag()) != null)
                            sihOUTitle = bmodel.labelsMasterHelper.applyLabels(holder.sihOuter.getTag());

                        if (bmodel.configurationMasterHelper.SHOW_ORDER_CASE
                                && bmodel.configurationMasterHelper.SHOW_OUTER_CASE
                                && bmodel.configurationMasterHelper.SHOW_ORDER_PCS) {
                            if (holder.productObj.getSIH() == 0) {
                                holder.sihCase.setText(sihCSTitle + "0");
                                holder.sihOuter.setText(sihOUTitle + "0");
                                holder.sih.setText(sihPSTitle + "0");
                            } else if (holder.productObj.getCaseSize() == 0) {
                                holder.sihCase.setText(sihCSTitle + "0");
                                if (holder.productObj.getOutersize() == 0) {
                                    holder.sihOuter.setText(sihOUTitle + "0");
                                    String strSIh = holder.productObj.getSIH() + "";
                                    holder.sih.setText(sihPSTitle + strSIh);
                                } else {
                                    String strSihOuter = holder.productObj.getSIH()
                                            / holder.productObj.getOutersize() + "";
                                    holder.sihOuter.setText(sihOUTitle + strSihOuter);
                                    String strSih = holder.productObj.getSIH()
                                            % holder.productObj.getOutersize() + "";
                                    holder.sih.setText(sihPSTitle + strSih);
                                }
                            } else {
                                String strSihCase = holder.productObj.getSIH()
                                        / holder.productObj.getCaseSize() + "";
                                holder.sihCase.setText(sihCSTitle + strSihCase);
                                if (holder.productObj.getOutersize() > 0
                                        && (holder.productObj.getSIH() % holder.productObj
                                        .getCaseSize()) >= holder.productObj
                                        .getOutersize()) {
                                    String strSihOuter = (holder.productObj.getSIH() % holder.productObj
                                            .getCaseSize())
                                            / holder.productObj.getOutersize() + "";
                                    holder.sihOuter
                                            .setText(sihOUTitle + strSihOuter);
                                    String strSih = (holder.productObj.getSIH() % holder.productObj
                                            .getCaseSize())
                                            % holder.productObj.getOutersize() + "";
                                    holder.sih
                                            .setText(sihPSTitle + strSih);
                                } else {
                                    holder.sihOuter.setText(sihOUTitle + "0");
                                    String strSih = holder.productObj.getSIH()
                                            % holder.productObj.getCaseSize() + "";
                                    holder.sih.setText(sihPSTitle + strSih);
                                }
                            }
                        } else if (bmodel.configurationMasterHelper.SHOW_ORDER_CASE
                                && bmodel.configurationMasterHelper.SHOW_OUTER_CASE) {
                            if (holder.productObj.getSIH() == 0) {
                                holder.sihCase.setText(sihCSTitle + "0");
                                holder.sihOuter.setText(sihOUTitle + "0");
                            } else if (holder.productObj.getCaseSize() == 0) {
                                holder.sihCase.setText(sihCSTitle + "0");
                                if (holder.productObj.getOutersize() == 0)
                                    holder.sihOuter.setText(sihOUTitle + "0");
                                else {
                                    String strSihOuter = holder.productObj.getSIH()
                                            / holder.productObj.getOutersize() + "";
                                    holder.sihOuter.setText(sihOUTitle + strSihOuter);
                                }
                            } else {
                                String strSihCase = holder.productObj.getSIH()
                                        / holder.productObj.getCaseSize() + "";
                                holder.sihCase.setText(sihCSTitle + strSihCase);
                                if (holder.productObj.getOutersize() > 0
                                        && (holder.productObj.getSIH() % holder.productObj
                                        .getCaseSize()) >= holder.productObj
                                        .getOutersize()) {
                                    String strSihOuter = (holder.productObj.getSIH() % holder.productObj
                                            .getCaseSize())
                                            / holder.productObj.getOutersize() + "";
                                    holder.sihOuter
                                            .setText(sihOUTitle + strSihOuter);
                                } else {
                                    holder.sihOuter.setText(sihOUTitle + "0");
                                }
                            }
                        } else if (bmodel.configurationMasterHelper.SHOW_OUTER_CASE
                                && bmodel.configurationMasterHelper.SHOW_ORDER_PCS) {
                            if (holder.productObj.getSIH() == 0) {
                                holder.sih.setText(sihPSTitle + "0");
                                holder.sihOuter.setText(sihOUTitle + "0");
                            } else if (holder.productObj.getOutersize() == 0) {
                                String strSih = holder.productObj.getSIH() + "";
                                holder.sih.setText(sihPSTitle + strSih);
                                holder.sihOuter.setText(sihOUTitle + "0");
                            } else {
                                String strSihOuter = holder.productObj.getSIH()
                                        / holder.productObj.getOutersize() + "";
                                holder.sihOuter.setText(sihOUTitle + strSihOuter);
                                String strSih = holder.productObj.getSIH()
                                        % holder.productObj.getOutersize() + "";
                                holder.sih.setText(sihPSTitle + strSih);
                            }
                        } else if (bmodel.configurationMasterHelper.SHOW_ORDER_CASE
                                && bmodel.configurationMasterHelper.SHOW_ORDER_PCS) {
                            if (holder.productObj.getSIH() == 0) {
                                holder.sih.setText(sihPSTitle + "0");
                                holder.sihCase.setText(sihCSTitle + "0");
                            } else if (holder.productObj.getCaseSize() == 0) {
                                String strsih = holder.productObj.getSIH() + "";
                                holder.sih.setText(sihPSTitle + strsih);
                                holder.sihCase.setText(sihCSTitle + "0");
                            } else {
                                String strSihCase = holder.productObj.getSIH()
                                        / holder.productObj.getCaseSize() + "";
                                holder.sihCase.setText(sihCSTitle + strSihCase);
                                String strSih = holder.productObj.getSIH()
                                        % holder.productObj.getCaseSize() + "";
                                holder.sih.setText(sihPSTitle + strSih);
                            }
                        } else if (bmodel.configurationMasterHelper.SHOW_ORDER_CASE) {
                            if (holder.productObj.getSIH() == 0)
                                holder.sihCase.setText(sihCSTitle + "0");
                            else if (holder.productObj.getCaseSize() == 0)
                                holder.sihCase.setText(sihCSTitle + "0");
                            else {
                                String strSih = holder.productObj.getSIH()
                                        / holder.productObj.getCaseSize() + "";
                                holder.sihCase.setText(sihCSTitle + strSih);
                            }
                        } else if (bmodel.configurationMasterHelper.SHOW_OUTER_CASE) {
                            if (holder.productObj.getSIH() == 0)
                                holder.sihOuter.setText(sihOUTitle + "0");
                            else if (holder.productObj.getOutersize() == 0)
                                holder.sihOuter.setText(sihOUTitle + "0");
                            else {
                                String strSihOuter = holder.productObj.getSIH()
                                        / holder.productObj.getOutersize() + "";
                                holder.sihOuter.setText(sihOUTitle + strSihOuter);
                            }
                        } else if (bmodel.configurationMasterHelper.SHOW_ORDER_PCS) {
                            String strSih = holder.productObj.getSIH() + "";
                            holder.sih.setText(sihPSTitle + strSih);
                        }
                    } else {
                        String strSih = holder.productObj.getSIH() + "";
                        holder.sih.setText(sihPSTitle + strSih);
                    }

                }

                // Set order qty
                if (!bmodel.configurationMasterHelper.IS_SHOW_DEFAULT_UOM) {
                    if (bmodel.configurationMasterHelper.SHOW_ORDER_CASE) {
                        String strCaseQty = holder.productObj.getOrderedCaseQty() + "";
                        holder.caseQty.setText(strCaseQty);
                    }
                    if (bmodel.configurationMasterHelper.SHOW_ORDER_PCS) {
                        String strPcsQty = holder.productObj.getOrderedPcsQty() + "";
                        holder.pcsQty.setText(strPcsQty);
                    }
                    if (bmodel.configurationMasterHelper.SHOW_OUTER_CASE) {
                        String strOuterQty = holder.productObj.getOrderedOuterQty() + "";
                        holder.outerQty.setText(strOuterQty);
                    }
                }



                if (bmodel.configurationMasterHelper.IS_WSIH) {
                    String wSIH = getString(R.string.wsih_label) + ": ";

                    if (bmodel.labelsMasterHelper.applyLabels(holder.wsih.getTag()) != null) {
                        wSIH = bmodel.labelsMasterHelper.applyLabels(holder.wsih.getTag()) + ": ";
                    }

                    wSIH += holder.productObj.getWSIH() + "";
                    holder.wsih.setText(wSIH);
                }


                try {
                    if (holder.productObj.isPromo()) {
                        holder.slant_view_bg.setVisibility(View.VISIBLE);
                        holder.slant_view_bg.setBackgroundColor(Color.RED);
                    } else {
                        holder.slant_view_bg.setVisibility(View.GONE);
                    }
                } catch (Exception e) {
                    Commons.printException(e);
                }

                //set order Qty based on UOM wise

                if (bmodel.configurationMasterHelper.IS_SHOW_DEFAULT_UOM
                        && holder.productObj.getProductWiseUomList().size() > 0) {

                    if (holder.productObj.getOrderedPcsQty() > 0 ||
                            holder.productObj.getOrderedCaseQty() > 0 ||
                            holder.productObj.getOrderedOuterQty() > 0) {
                        if (holder.productObj.getOrderedPcsQty() > 0) {
                            holder.tv_uo_names.setText(updateUOM(holder.productObj, false));
                            holder.uom_qty.setText(holder.productObj.getOrderedPcsQty() + "");
                        } else if (holder.productObj.getOrderedCaseQty() > 0) {
                            holder.tv_uo_names.setText(updateUOM(holder.productObj, false));
                            holder.uom_qty.setText(holder.productObj.getOrderedCaseQty() + "");
                        } else if (holder.productObj.getOrderedOuterQty() > 0) {
                            holder.tv_uo_names.setText(updateUOM(holder.productObj, false));
                            holder.uom_qty.setText(holder.productObj.getOrderedOuterQty() + "");
                        }
                    } else {
                        if ((holder.productObj.getDefaultUomId() != 0 && holder.productObj.getPcUomid() != 0) &&
                                holder.productObj.getDefaultUomId() == holder.productObj.getPcUomid()) {
                            holder.productObj.setSelectedUomId(holder.productObj.getDefaultUomId());
                            holder.tv_uo_names.setText(updateUOM(holder.productObj, false));
                            holder.uom_qty.setText("0");
                        } else if ((holder.productObj.getDefaultUomId() != 0 && holder.productObj.getCaseUomId() != 0) &&
                                holder.productObj.getDefaultUomId() == holder.productObj.getCaseUomId()) {
                            holder.productObj.setSelectedUomId(holder.productObj.getDefaultUomId());
                            holder.tv_uo_names.setText(updateUOM(holder.productObj, false));
                            holder.uom_qty.setText("0");
                        } else if ((holder.productObj.getDefaultUomId() != 0 && holder.productObj.getOuUomid() != 0) &&
                                holder.productObj.getDefaultUomId() == holder.productObj.getOuUomid()) {
                            holder.productObj.setSelectedUomId(holder.productObj.getDefaultUomId());
                            holder.tv_uo_names.setText(updateUOM(holder.productObj, false));
                            holder.uom_qty.setText("0");
                        }
                    }


                }


                if (bmodel.configurationMasterHelper.SHOW_STK_ORD_SRP) {
                    String srpTitle = getString(R.string.srp) + ": ";

                    if (bmodel.labelsMasterHelper.applyLabels(holder.srp.getTag()) != null)
                        srpTitle = bmodel.labelsMasterHelper
                                .applyLabels(holder.srp.getTag()) + ": ";

                    holder.srp.setText(srpTitle + bmodel.formatValue(holder.productObj
                            .getSrp()));
                }
                if (bmodel.configurationMasterHelper.SHOW_STK_ORD_SRP_EDT)
                    holder.srpEdit.setText(bmodel.formatValue(holder.productObj
                            .getSrp()));

                if (holder.productObj.getOuUomid() == 0 || !holder.productObj.isOuterMapped()) {
                    holder.outerQty.setEnabled(false);
                    holder.shelfouter.setEnabled(false);
                } else {
                    holder.outerQty.setEnabled(true);
                    holder.shelfouter.setEnabled(true);
                }
                if (holder.productObj.getCaseUomId() == 0 || !holder.productObj.isCaseMapped()) {
                    holder.caseQty.setEnabled(false);
                    holder.shelfCaseQty.setEnabled(false);
                } else {
                    holder.caseQty.setEnabled(true);
                    holder.shelfCaseQty.setEnabled(true);
                }
                if (holder.productObj.getPcUomid() == 0 || !holder.productObj.isPieceMapped()) {
                    holder.pcsQty.setEnabled(false);
                    holder.shelfPcsQty.setEnabled(false);
                } else {
                    holder.pcsQty.setEnabled(true);
                    holder.shelfPcsQty.setEnabled(true);
                }

                if (bmodel.configurationMasterHelper.IS_SHOW_DEFAULT_UOM) {
                    if (holder.productObj.getProductWiseUomList().size() == 0) {
                        holder.tv_uo_names.setClickable(false);
                        holder.tv_uo_names.setEnabled(false);
                        holder.uom_qty.setEnabled(false);
                    } else {
                        holder.tv_uo_names.setClickable(true);
                        holder.tv_uo_names.setEnabled(true);
                        holder.uom_qty.setEnabled(true);
                    }
                }

                if (bmodel.configurationMasterHelper.SHOW_REPLACED_QTY_CS) {
                    String strRepCaseQty = getString(R.string.rep_case) + ": ";

                    if (bmodel.labelsMasterHelper.applyLabels(holder.rep_cs.getTag()) != null)
                        strRepCaseQty = bmodel.labelsMasterHelper
                                .applyLabels(holder.rep_cs.getTag() + ": ");

                    strRepCaseQty += holder.productObj.getRepCaseQty() + "";

                    holder.rep_cs.setText(strRepCaseQty);
                }
                if (bmodel.configurationMasterHelper.SHOW_REPLACED_QTY_OU) {
                    String strRepOuterQty = getString(R.string.rep_outer) + ": ";

                    if (bmodel.labelsMasterHelper.applyLabels(holder.rep_ou.getTag()) != null)
                        strRepOuterQty = bmodel.labelsMasterHelper
                                .applyLabels(holder.rep_ou.getTag() + ": ");

                    strRepOuterQty += holder.productObj.getRepOuterQty() + "";

                    holder.rep_ou.setText(strRepOuterQty);
                }
                if (bmodel.configurationMasterHelper.SHOW_REPLACED_QTY_PC) {
                    String strRepPcsQty = getString(R.string.rep_pcs) + ": ";

                    if (bmodel.labelsMasterHelper.applyLabels(holder.rep_ou.getTag()) != null)
                        strRepPcsQty = bmodel.labelsMasterHelper
                                .applyLabels(holder.rep_ou.getTag() + ": ");

                    strRepPcsQty += holder.productObj.getRepPieceQty() + "";

                    holder.rep_pcs.setText(strRepPcsQty);
                }

                if (hasStockChecked(holder.productObj)) {
                    String totSTKQty = getString(R.string.stock) + ": ";

                    if (bmodel.labelsMasterHelper.applyLabels(holder.text_stock.getTag()) != null) {
                        holder.text_stock.setText(bmodel.labelsMasterHelper.applyLabels(holder.text_stock.getTag()) + ": " + holder.productObj.getTotalStockQty() + "");
                    } else {
                        holder.text_stock.setText(getString(R.string.stock) + ": " + holder.productObj.getTotalStockQty() + "");
                    }

                    if (bmodel.labelsMasterHelper.applyLabels(holder.text_stock.getTag()) != null)
                        totSTKQty = bmodel.labelsMasterHelper
                                .applyLabels(holder.text_stock.getTag()) + ": ";
                    holder.text_stock.setText(totSTKQty + String.valueOf(holder.productObj.getTotalStockQty()));
                }

                if (bmodel.configurationMasterHelper.IS_ENABLE_PRODUCT_TAGGING_VALIDATION) {

                    String allocationTitle = getString(R.string.allocation) + ": ";

                    if (bmodel.labelsMasterHelper.applyLabels(holder.text_allocation.getTag()) != null)
                        allocationTitle = bmodel.labelsMasterHelper
                                .applyLabels(holder.text_allocation.getTag()) + ": ";

                    holder.text_allocation.setText(allocationTitle + (holder.productObj.getAllocationQty() != null &&
                            holder.productObj.getAllocationQty().length() > 0
                            ? holder.productObj.getAllocationQty() : "0"));
                }
            }
            else {

                if (bmodel.configurationMasterHelper.SHOW_FOC) {
                    String strFoc = holder.productObj.getFoc() + "";
                    holder.foc.setText(strFoc);
                }
            }


            return row;
        }
    }

    class ViewHolder {
        private AppCompatCheckBox imageButton_availability;
        private String productId;
        private String caseTitleText;
        private String pname;
        private ProductMasterBO productObj;
        private TextView tvbarcode;
        private TextView psname, tvProductCode;
        private TextView so;
        private TextView sih;
        private TextView ppq;
        private TextView msq;
        private TextView srp;
        private TextView psq;
        private TextView socs;
        private TextView wsih;
        private TextView sihCase;
        private TextView sihOuter;
        private TextView mrp;
        private TextView weight;
        private TextView indicativeOrder_oc;
        private TextView cleanedOrder_oc;
        private EditText shelfPcsQty;
        private EditText shelfCaseQty;
        private EditText pcsQty;
        private EditText foc;
        private EditText caseQty;
        private EditText outerQty;
        private EditText shelfouter;
        private EditText srpEdit;
        private TextView total;
        private TextView rep_pcs;
        private TextView rep_cs;
        private TextView rep_ou;
        private ImageView imageView_stock;
        private EditText salesReturn;
        private TextView moq;
        private TextView text_stock;
        private TextView sales_return_tv;

        private TextView text_allocation;

        private EditText uom_qty;
        private Button tv_uo_names;
        private SlantView slant_view_bg;


    }


    private void initializeRowViews(View row, ViewHolder holder,int SOLogic,int viewType){


        if(viewType==0) {


            holder.tvbarcode = row
                    .findViewById(R.id.stock_and_order_listview_productbarcode);

            holder.psname = row
                    .findViewById(R.id.stock_and_order_listview_productname);
            holder.tvProductCode = row
                    .findViewById(R.id.stock_and_order_listview_productcode);
            holder.mrp = row
                    .findViewById(R.id.stock_and_order_listview_mrp);
            holder.ppq = row
                    .findViewById(R.id.stock_and_order_listview_ppq);
            holder.msq = row
                    .findViewById(R.id.stock_and_order_listview_msq);
            holder.psq = row
                    .findViewById(R.id.stock_and_order_listview_psq);
            holder.moq = row.
                    findViewById(R.id.stock_and_order_listview_moq);


            //Store - Stock Check

            holder.imageButton_availability = row.findViewById(R.id.btn_availability);
            holder.imageView_stock = row.findViewById(R.id.iv_stock);
            //check - qty entry
            holder.shelfCaseQty = row
                    .findViewById(R.id.stock_and_order_listview_sc_qty);
            holder.shelfPcsQty = row
                    .findViewById(R.id.stock_and_order_listview_sp_qty);
            holder.shelfouter = row
                    .findViewById(R.id.stock_and_order_listview_shelfouter_qty);

            //Suggested Order
            holder.so = row
                    .findViewById(R.id.stock_and_order_listview_so);
            holder.socs = row
                    .findViewById(R.id.stock_and_order_listview_socs);

            //WareHouse Stock In Hand
            holder.wsih = row
                    .findViewById(R.id.stock_and_order_listview_wsih);
            //Van Stock In Hand
            holder.sih = row
                    .findViewById(R.id.stock_and_order_listview_sih);
            holder.sihCase = row.findViewById(R.id.stock_and_order_listview_sih_case);
            holder.sihOuter = row.findViewById(R.id.stock_and_order_listview_sih_outer);

            //Store - Order
            holder.caseQty = row
                    .findViewById(R.id.stock_and_order_listview_case_qty);
            holder.pcsQty = row
                    .findViewById(R.id.stock_and_order_listview_pcs_qty);
            holder.foc = row
                    .findViewById(R.id.stock_and_order_listview_foc);
            holder.outerQty = row
                    .findViewById(R.id.stock_and_order_listview_outer_case_qty);

            holder.tv_uo_names = row
                    .findViewById(R.id.tv_uo_name);
            holder.uom_qty = row
                    .findViewById(R.id.stock_and_order_listview_uom_qty);

            holder.srp = row
                    .findViewById(R.id.stock_and_order_listview_srp);
            holder.srpEdit = row
                    .findViewById(R.id.stock_and_order_listview_srpedit);

            holder.salesReturn = row
                    .findViewById(R.id.stock_and_order_listview_sales_return_qty);
            holder.salesReturn.setFocusable(false);
            holder.sales_return_tv = row
                    .findViewById(R.id.stock_and_order_listview_sr_tv);

            holder.total = row
                    .findViewById(R.id.stock_and_order_listview_total);


            holder.weight = row
                    .findViewById(R.id.stock_and_order_listview_weight);

            holder.rep_cs = row.findViewById(R.id.rep_case);
            holder.rep_ou = row.findViewById(R.id.rep_outer);
            holder.rep_pcs = row.findViewById(R.id.rep_pcs);
            holder.indicativeOrder_oc = row.findViewById(R.id.indicativeOrder_oc);
            holder.cleanedOrder_oc = row.findViewById(R.id.cleanedOrder_oc);


            holder.text_stock = row.findViewById(R.id.text_stock);

            holder.text_allocation = row.findViewById(R.id.stock_and_order_listview_allocation);

            //slant view
            holder.slant_view_bg = row.findViewById(R.id.slant_view_bg);

            holder.psname.setMaxLines(bmodel.configurationMasterHelper.MAX_NO_OF_PRODUCT_LINES);

            //setting typefaces
            holder.tvbarcode.setTypeface(FontUtils.getFontRoboto(StockAndOrder.this, FontUtils.FontType.LIGHT));
            holder.psname.setTypeface(FontUtils.getProductNameFont(StockAndOrder.this));
            holder.tvProductCode.setTypeface(FontUtils.getFontRoboto(StockAndOrder.this, FontUtils.FontType.LIGHT));
            holder.mrp.setTypeface(FontUtils.getFontRoboto(StockAndOrder.this, FontUtils.FontType.MEDIUM));
            holder.ppq.setTypeface(FontUtils.getFontRoboto(StockAndOrder.this, FontUtils.FontType.MEDIUM));
            holder.msq.setTypeface(FontUtils.getFontRoboto(StockAndOrder.this, FontUtils.FontType.MEDIUM));
            holder.psq.setTypeface(FontUtils.getFontRoboto(StockAndOrder.this, FontUtils.FontType.MEDIUM));
            holder.moq.setTypeface(FontUtils.getFontRoboto(StockAndOrder.this, FontUtils.FontType.MEDIUM));
            holder.shelfCaseQty.setTypeface(FontUtils.getFontRoboto(StockAndOrder.this, FontUtils.FontType.MEDIUM));
            holder.shelfPcsQty.setTypeface(FontUtils.getFontRoboto(StockAndOrder.this, FontUtils.FontType.MEDIUM));
            holder.shelfouter.setTypeface(FontUtils.getFontRoboto(StockAndOrder.this, FontUtils.FontType.MEDIUM));
            holder.srpEdit.setTypeface(FontUtils.getFontRoboto(StockAndOrder.this, FontUtils.FontType.MEDIUM));
            holder.so.setTypeface(FontUtils.getFontRoboto(StockAndOrder.this, FontUtils.FontType.MEDIUM));
            holder.socs.setTypeface(FontUtils.getFontRoboto(StockAndOrder.this, FontUtils.FontType.MEDIUM));
            holder.wsih.setTypeface(FontUtils.getFontRoboto(StockAndOrder.this, FontUtils.FontType.MEDIUM));
            holder.sih.setTypeface(FontUtils.getFontRoboto(StockAndOrder.this, FontUtils.FontType.MEDIUM));
            holder.sihCase.setTypeface(FontUtils.getFontRoboto(StockAndOrder.this, FontUtils.FontType.MEDIUM));
            holder.sihOuter.setTypeface(FontUtils.getFontRoboto(StockAndOrder.this, FontUtils.FontType.MEDIUM));
            holder.caseQty.setTypeface(FontUtils.getFontRoboto(StockAndOrder.this, FontUtils.FontType.MEDIUM));
            holder.pcsQty.setTypeface(FontUtils.getFontRoboto(StockAndOrder.this, FontUtils.FontType.MEDIUM));
            holder.foc.setTypeface(FontUtils.getFontRoboto(StockAndOrder.this, FontUtils.FontType.MEDIUM));
            holder.outerQty.setTypeface(FontUtils.getFontRoboto(StockAndOrder.this, FontUtils.FontType.MEDIUM));
            holder.srp.setTypeface(FontUtils.getFontRoboto(StockAndOrder.this, FontUtils.FontType.MEDIUM));
            holder.total.setTypeface(FontUtils.getFontRoboto(StockAndOrder.this, FontUtils.FontType.MEDIUM));
            holder.weight.setTypeface(FontUtils.getFontRoboto(StockAndOrder.this, FontUtils.FontType.MEDIUM));
            holder.rep_cs.setTypeface(FontUtils.getFontRoboto(StockAndOrder.this, FontUtils.FontType.MEDIUM));
            holder.rep_ou.setTypeface(FontUtils.getFontRoboto(StockAndOrder.this, FontUtils.FontType.MEDIUM));
            holder.rep_pcs.setTypeface(FontUtils.getFontRoboto(StockAndOrder.this, FontUtils.FontType.MEDIUM));
            holder.indicativeOrder_oc.setTypeface(FontUtils.getFontRoboto(StockAndOrder.this, FontUtils.FontType.MEDIUM));
            holder.cleanedOrder_oc.setTypeface(FontUtils.getFontRoboto(StockAndOrder.this, FontUtils.FontType.MEDIUM));
            holder.salesReturn.setTypeface(FontUtils.getFontRoboto(StockAndOrder.this, FontUtils.FontType.MEDIUM));
            holder.text_stock.setTypeface(FontUtils.getFontRoboto(StockAndOrder.this, FontUtils.FontType.MEDIUM));
            holder.text_allocation.setTypeface(FontUtils.getFontRoboto(StockAndOrder.this, FontUtils.FontType.MEDIUM));
            holder.uom_qty.setTypeface(FontUtils.getFontRoboto(StockAndOrder.this, FontUtils.FontType.MEDIUM));
            holder.tv_uo_names.setTypeface(FontUtils.getFontRoboto(StockAndOrder.this, FontUtils.FontType.MEDIUM));
            holder.sales_return_tv.setTypeface(FontUtils.getFontRoboto(StockAndOrder.this, FontUtils.FontType.MEDIUM));

            if (!bmodel.configurationMasterHelper.IS_ENABLE_PRODUCT_TAGGING_VALIDATION) {
                holder.text_allocation.setVisibility(View.GONE);
            }

            if (bmodel.configurationMasterHelper.IS_SHOW_PSQ) {
                holder.psq.setVisibility(View.VISIBLE);
            } else {
                holder.psq.setVisibility(View.GONE);
            }

            if (!bmodel.configurationMasterHelper.IS_SHOW_PPQ) {
                holder.ppq.setVisibility(View.GONE);
            }

            if (bmodel.configurationMasterHelper.IS_MOQ_ENABLED)
                holder.moq.setVisibility(View.VISIBLE);

            if (!bmodel.configurationMasterHelper.SHOW_BARCODE)
                holder.tvbarcode.setVisibility(View.GONE);

            if (bmodel.configurationMasterHelper.SHOW_SALES_RETURN_IN_ORDER)
                holder.salesReturn.setVisibility(View.VISIBLE);

            if (bmodel.configurationMasterHelper.SHOW_SALES_RETURN_TV_IN_ORDER)
                holder.sales_return_tv.setVisibility(View.VISIBLE);

            if (!bmodel.configurationMasterHelper.SHOW_STK_ORD_MRP)
                holder.mrp.setVisibility(View.GONE);

            if (!bmodel.configurationMasterHelper.IS_COMBINED_STOCK_CHECK_FROM_ORDER)
                holder.imageView_stock.setVisibility(View.GONE);

            if (!bmodel.configurationMasterHelper.SHOW_INDICATIVE_ORDER) {
                holder.indicativeOrder_oc.setVisibility(View.GONE);
            }
            //wsih or Distributor Inventory
            if (!bmodel.configurationMasterHelper.IS_WSIH) {
                holder.wsih.setVisibility(View.GONE);
            }

            if (!bmodel.configurationMasterHelper.SHOW_CLEANED_ORDER) {
                holder.cleanedOrder_oc.setVisibility(View.GONE);
            }

            if (!stockCheckHelper.SHOW_STOCK_CB
                    || screenCode
                    .equals(ConfigurationMasterHelper.MENU_ORDER))
                (row.findViewById(R.id.llAvail)).setVisibility(View.GONE);

            else {
                try {
                    ((TextView) row.findViewById(R.id.shelfPcsCB)).setTypeface(FontUtils.getFontRoboto(StockAndOrder.this, FontUtils.FontType.LIGHT));
                    if (bmodel.labelsMasterHelper.applyLabels(row.findViewById(
                            R.id.shelfPcsCB).getTag()) != null)
                        ((TextView) row.findViewById(R.id.shelfPcsCB))
                                .setText(bmodel.labelsMasterHelper
                                        .applyLabels(row.findViewById(
                                                R.id.shelfPcsCB)
                                                .getTag()));
                } catch (Exception e) {
                    Commons.printException(e + "");
                }
            }


            if (!stockCheckHelper.SHOW_STOCK_SC
                    || screenCode
                    .equals(ConfigurationMasterHelper.MENU_ORDER))
                (row.findViewById(R.id.llShelfCase)).setVisibility(View.GONE);
            else {
                try {
                    ((TextView) row.findViewById(R.id.shelfCaseTitle)).setTypeface(FontUtils.getFontRoboto(StockAndOrder.this, FontUtils.FontType.LIGHT));
                    if (bmodel.labelsMasterHelper.applyLabels(row.findViewById(
                            R.id.shelfCaseTitle).getTag()) != null)
                        ((TextView) row.findViewById(R.id.shelfCaseTitle))
                                .setText(bmodel.labelsMasterHelper
                                        .applyLabels(row.findViewById(
                                                R.id.shelfCaseTitle)
                                                .getTag()));
                } catch (Exception e) {
                    Commons.printException(e + "");
                }
            }
            if (!stockCheckHelper.SHOW_STOCK_SP
                    || screenCode
                    .equals(ConfigurationMasterHelper.MENU_ORDER))
                (row.findViewById(R.id.llShelfPc)).setVisibility(View.GONE);
            else {
                try {
                    ((TextView) row.findViewById(R.id.shelfPcsTitle)).setTypeface(FontUtils.getFontRoboto(StockAndOrder.this, FontUtils.FontType.LIGHT));
                    if (bmodel.labelsMasterHelper.applyLabels(row.findViewById(
                            R.id.shelfPcsTitle).getTag()) != null)
                        ((TextView) row.findViewById(R.id.shelfPcsTitle))
                                .setText(bmodel.labelsMasterHelper
                                        .applyLabels(row.findViewById(
                                                R.id.shelfPcsTitle)
                                                .getTag()));
                } catch (Exception e) {
                    Commons.printException(e + "");
                }
            }
            if (!stockCheckHelper.SHOW_SHELF_OUTER
                    || screenCode
                    .equals(ConfigurationMasterHelper.MENU_ORDER))
                (row.findViewById(R.id.llShelfOuter)).setVisibility(View.GONE);
            else {
                try {
                    ((TextView) row.findViewById(R.id.shelfOuterTitle)).setTypeface(FontUtils.getFontRoboto(StockAndOrder.this, FontUtils.FontType.LIGHT));
                    if (bmodel.labelsMasterHelper.applyLabels(row.findViewById(
                            R.id.shelfOuterTitle).getTag()) != null)
                        ((TextView) row.findViewById(R.id.shelfOuterTitle))
                                .setText(bmodel.labelsMasterHelper
                                        .applyLabels(row.findViewById(
                                                R.id.shelfOuterTitle)
                                                .getTag()));
                } catch (Exception e) {
                    Commons.printException(e + "");
                }
            }

            //Stock Field - Enable/Disable - End
            //Suggested Order
            if (!bmodel.configurationMasterHelper.IS_SUGGESTED_ORDER)
                holder.so.setVisibility(View.GONE);

            if (!bmodel.configurationMasterHelper.SHOW_SO_SPLIT)
                holder.socs.setVisibility(View.GONE);

            if (bmodel.configurationMasterHelper.ALLOW_SO_COPY) {
                holder.socs.setPaintFlags(holder.socs.getPaintFlags()
                        | Paint.UNDERLINE_TEXT_FLAG);
                holder.so.setPaintFlags(holder.so.getPaintFlags()
                        | Paint.UNDERLINE_TEXT_FLAG);
            }

            // SIH - Enable/Disable - Start
            if (bmodel.configurationMasterHelper.IS_STOCK_IN_HAND) {
                if (bmodel.configurationMasterHelper.SHOW_SIH_SPLIT) {
                    if (!bmodel.configurationMasterHelper.SHOW_ORDER_CASE)
                        holder.sihCase.setVisibility(View.GONE);
                    if (!bmodel.configurationMasterHelper.SHOW_OUTER_CASE)
                        holder.sihOuter.setVisibility(View.GONE);
                    if (!bmodel.configurationMasterHelper.SHOW_ORDER_PCS)
                        holder.sih.setVisibility(View.GONE);
                } else {
                    holder.sihCase.setVisibility(View.GONE);
                    holder.sihOuter.setVisibility(View.GONE);
                }
            } else {
                holder.sih.setVisibility(View.GONE);
                holder.sihCase.setVisibility(View.GONE);
                holder.sihOuter.setVisibility(View.GONE);
            }
            // SIH - Enable/Disable - Start
            // Order Field - Enable/Disable
            if (!bmodel.configurationMasterHelper.SHOW_ORDER_CASE)
                (row.findViewById(R.id.llCase)).setVisibility(View.GONE);
            else {
                try {
                    if (bmodel.configurationMasterHelper.IS_ORD_DIGIT)
                        holder.caseQty.setFilters(new InputFilter[]{new InputFilter.LengthFilter(bmodel.configurationMasterHelper.ORD_DIGIT)});

                    ((TextView) row.findViewById(R.id.caseTitle)).setTypeface(FontUtils.getFontRoboto(StockAndOrder.this, FontUtils.FontType.LIGHT));
                    if (bmodel.labelsMasterHelper.applyLabels(row.findViewById(
                            R.id.caseTitle).getTag()) != null) {
                        ((TextView) row.findViewById(R.id.caseTitle))
                                .setText(bmodel.labelsMasterHelper
                                        .applyLabels(row.findViewById(
                                                R.id.caseTitle).getTag()));
                        holder.caseTitleText = bmodel.labelsMasterHelper
                                .applyLabels(row.findViewById(
                                        R.id.caseTitle).getTag());
                    } else
                        holder.caseTitleText = getResources().getString(R.string.item_case);
                } catch (Exception e) {
                    Commons.printException(e + "");
                    holder.caseTitleText = getResources().getString(R.string.item_case);
                }
            }
            if (!bmodel.configurationMasterHelper.SHOW_ORDER_PCS)
                (row.findViewById(R.id.llPcs)).setVisibility(View.GONE);
            else {
                try {
                    if (bmodel.configurationMasterHelper.IS_ORD_DIGIT)
                        holder.pcsQty.setFilters(new InputFilter[]{new InputFilter.LengthFilter(bmodel.configurationMasterHelper.ORD_DIGIT)});

                    ((TextView) row.findViewById(R.id.pcsTitle)).setTypeface(FontUtils.getFontRoboto(StockAndOrder.this, FontUtils.FontType.LIGHT));
                    if (bmodel.labelsMasterHelper.applyLabels(row.findViewById(
                            R.id.pcsTitle).getTag()) != null)
                        ((TextView) row.findViewById(R.id.pcsTitle))
                                .setText(bmodel.labelsMasterHelper
                                        .applyLabels(row.findViewById(
                                                R.id.pcsTitle).getTag()));
                } catch (Exception e) {
                    Commons.printException(e + "");
                }
            }

            if (!bmodel.configurationMasterHelper.SHOW_FOC)
                (row.findViewById(R.id.llFoc)).setVisibility(View.GONE);
            else {
                try {
                    ((TextView) row.findViewById(R.id.focTitle)).setTypeface(FontUtils.getFontRoboto(StockAndOrder.this, FontUtils.FontType.LIGHT));
                    if (bmodel.labelsMasterHelper.applyLabels(row.findViewById(
                            R.id.focTitle).getTag()) != null)
                        ((TextView) row.findViewById(R.id.focTitle))
                                .setText(bmodel.labelsMasterHelper
                                        .applyLabels(row.findViewById(
                                                R.id.focTitle).getTag()));
                } catch (Exception e) {
                    Commons.printException(e + "");
                }
            }

            if (!bmodel.configurationMasterHelper.SHOW_OUTER_CASE)
                (row.findViewById(R.id.llOuter)).setVisibility(View.GONE);
            else {
                try {
                    if (bmodel.configurationMasterHelper.IS_ORD_DIGIT)
                        holder.outerQty.setFilters(new InputFilter[]{new InputFilter.LengthFilter(bmodel.configurationMasterHelper.ORD_DIGIT)});

                    ((TextView) row.findViewById(R.id.outercaseTitle)).setTypeface(FontUtils.getFontRoboto(StockAndOrder.this, FontUtils.FontType.LIGHT));
                    if (bmodel.labelsMasterHelper.applyLabels(row.findViewById(
                            R.id.outercaseTitle).getTag()) != null)
                        ((TextView) row.findViewById(R.id.outercaseTitle))
                                .setText(bmodel.labelsMasterHelper
                                        .applyLabels(row.findViewById(
                                                R.id.outercaseTitle)
                                                .getTag()));
                } catch (Exception e) {
                    Commons.printException(e + "");
                }
            }

            if (!bmodel.configurationMasterHelper.SHOW_STK_ORD_SRP_EDT)
                (row.findViewById(R.id.llSrpEdit)).setVisibility(View.GONE);
            else {
                try {
                    ((TextView) row.findViewById(R.id.srpeditTitle)).setTypeface(FontUtils.getFontRoboto(StockAndOrder.this, FontUtils.FontType.LIGHT));
                    if (bmodel.labelsMasterHelper.applyLabels(row.findViewById(
                            R.id.srpeditTitle).getTag()) != null)
                        ((TextView) row.findViewById(R.id.srpeditTitle))
                                .setText(bmodel.labelsMasterHelper
                                        .applyLabels(row.findViewById(
                                                R.id.srpeditTitle).getTag()));
                } catch (Exception e) {
                    Commons.printException(e + "");
                }
            }

            if (!bmodel.configurationMasterHelper.SHOW_STK_ORD_SRP)
                holder.srp.setVisibility(View.GONE);

            if (!bmodel.configurationMasterHelper.SHOW_SALES_RETURN_IN_ORDER)
                (row.findViewById(R.id.llStkRtEdit)).setVisibility(View.GONE);
            else {
                try {
                    ((TextView) row.findViewById(R.id.stkRtTitle)).setTypeface(FontUtils.getFontRoboto(StockAndOrder.this, FontUtils.FontType.LIGHT));
                    if (bmodel.labelsMasterHelper.applyLabels(row.findViewById(R.id.stkRtTitle).getTag()) != null)
                        ((TextView) row.findViewById(R.id.stkRtTitle))
                                .setText(bmodel.labelsMasterHelper
                                        .applyLabels(row.findViewById(
                                                R.id.stkRtTitle).getTag()));
                } catch (Exception e) {
                    Commons.printException(e + "");
                }
            }

            if (!bmodel.configurationMasterHelper.SHOW_ORDER_TOTAL)
                (row.findViewById(R.id.llTotal)).setVisibility(View.GONE);
            else {
                try {
                    ((TextView) row.findViewById(R.id.totalTitle)).setTypeface(FontUtils.getFontRoboto(StockAndOrder.this, FontUtils.FontType.LIGHT));
                    if (bmodel.labelsMasterHelper.applyLabels(row.findViewById(
                            R.id.totalTitle).getTag()) != null)
                        ((TextView) row.findViewById(R.id.totalTitle))
                                .setText(bmodel.labelsMasterHelper
                                        .applyLabels(row.findViewById(
                                                R.id.totalTitle).getTag()));
                } catch (Exception e) {
                    Commons.printException(e + "");
                }
            }


            if (!bmodel.configurationMasterHelper.SHOW_ORDER_WEIGHT)
                (row.findViewById(R.id.llWeight)).setVisibility(View.GONE);
            else {
                try {
                    ((TextView) row.findViewById(R.id.weight)).setTypeface(FontUtils.getFontRoboto(StockAndOrder.this, FontUtils.FontType.LIGHT));
                    if (bmodel.labelsMasterHelper.applyLabels(row.findViewById(
                            R.id.weight).getTag()) != null)
                        ((TextView) row.findViewById(R.id.weight))
                                .setText(bmodel.labelsMasterHelper
                                        .applyLabels(row.findViewById(
                                                R.id.weight)
                                                .getTag()));
                } catch (Exception e) {
                    Commons.printException(e + "");
                }
            }

            if (!bmodel.configurationMasterHelper.SHOW_REPLACED_QTY_CS)
                holder.rep_cs.setVisibility(View.GONE);

            if (!bmodel.configurationMasterHelper.SHOW_REPLACED_QTY_OU)
                holder.rep_ou.setVisibility(View.GONE);

            if (!bmodel.configurationMasterHelper.SHOW_REPLACED_QTY_PC)
                holder.rep_pcs.setVisibility(View.GONE);

            if (!bmodel.configurationMasterHelper.SHOW_STK_QTY_IN_ORDER)
                holder.text_stock.setVisibility(View.GONE);

            if (!bmodel.configurationMasterHelper.IS_SHOW_DEFAULT_UOM) {
                (row.findViewById(R.id.llUom_Qty)).setVisibility(View.GONE);
                (row.findViewById(R.id.llUom_dropdwon)).setVisibility(View.GONE);
            } else {
                (row.findViewById(R.id.llPcs)).setVisibility(View.GONE);
                (row.findViewById(R.id.llCase)).setVisibility(View.GONE);
                (row.findViewById(R.id.llOuter)).setVisibility(View.GONE);
                ((TextView) row.findViewById(R.id.uomTitle)).setTypeface(FontUtils.getFontRoboto(StockAndOrder.this, FontUtils.FontType.LIGHT));
                try {
                    if (bmodel.labelsMasterHelper.applyLabels(row.findViewById(
                            R.id.uomTitle).getTag()) != null)
                        ((TextView) row.findViewById(R.id.uomTitle))
                                .setText(bmodel.labelsMasterHelper
                                        .applyLabels(row.findViewById(
                                                R.id.uomTitle)
                                                .getTag()));
                } catch (Exception e) {
                    Commons.printException(e);
                }
            }

            if (!bmodel.configurationMasterHelper.IS_SHOW_SKU_CODE)
                holder.tvProductCode.setVisibility(View.GONE);

            holder.tv_uo_names.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (holder.productObj.getProductWiseUomList().size() > 1) {
                        int qty = SDUtil.convertToInt(holder.uom_qty.getText().toString());
                        String uomName = updateUOM(holder.productObj, true);
                        holder.tv_uo_names.setText(uomName);

                        if (qty > 0)
                            holder.uom_qty.setText(qty + "");
                        else
                            holder.uom_qty.setText("0");

                        updateValue();
                    } else
                        Toast.makeText(
                                StockAndOrder.this,
                                getResources().getString(
                                        R.string.uom_not_available),
                                Toast.LENGTH_SHORT).show();

                }
            });


            holder.imageView_stock.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View view) {

                    productSearch.hideSoftInputFromWindow();

                    bmodel.setEditStockCheck(false);
                    if ((holder.productObj.getLocations()
                            .get(mSelectedLocationIndex)
                            .getShelfPiece() > 0 || holder.productObj.getPriceChanged() == 1)
                            ||
                            (!holder.productObj.getLocations().get(mSelectedLocationIndex).getNearexpiryDate().get(0).getNearexpPC().equals("0")
                                    || !holder.productObj.getLocations().get(mSelectedLocationIndex).getNearexpiryDate().get(0).getNearexpCA().equals("0")
                                    || !holder.productObj.getLocations().get(mSelectedLocationIndex).getNearexpiryDate().get(0).getNearexpOU().equals("0"))
                            || (holder.productObj.getLocations().get(mSelectedLocationIndex).getFacingQty() > 0)
                            ) {

                        bmodel.setEditStockCheck(true);
                    }

                    StockCheckHelper.getInstance(StockAndOrder.this).loadCmbStkChkConfiguration(StockAndOrder.this, bmodel.retailerMasterBO.getSubchannelid());

                    Intent intent = new Intent(StockAndOrder.this,
                            CombinedStockDetailActivity.class);
                    intent.putExtra("screenTitle", holder.productObj.getProductName());
                    intent.putExtra("pid", holder.productObj.getProductID());
                    intent.putExtra("selectedLocationIndex", mSelectedLocationIndex);
                    startActivity(intent);
                }
            });

            holder.imageButton_availability.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (stockCheckHelper.CHANGE_AVAL_FLOW) {
                        if (holder.productObj.getLocations()
                                .get(mSelectedLocationIndex).getAvailability() == -1) {
                            holder.productObj.getLocations()
                                    .get(mSelectedLocationIndex).setAvailability(0);

                            CompoundButtonCompat.setButtonTintList(holder.imageButton_availability, ColorStateList.valueOf(ContextCompat.getColor(StockAndOrder.this, R.color.RED)));
                            holder.imageButton_availability.setChecked(true);

                            if (stockCheckHelper.SHOW_STOCK_SP)
                                holder.shelfPcsQty.setText("0");
                            if (stockCheckHelper.SHOW_STOCK_SC)
                                holder.shelfCaseQty.setText("0");
                            if (stockCheckHelper.SHOW_SHELF_OUTER)
                                holder.shelfouter.setText("0");

                        } else if (holder.productObj.getLocations()
                                .get(mSelectedLocationIndex).getAvailability() == 1) {
                            holder.productObj.getLocations()
                                    .get(mSelectedLocationIndex).setAvailability(-1);

                            CompoundButtonCompat.setButtonTintList(holder.imageButton_availability, ColorStateList.valueOf(ContextCompat.getColor(StockAndOrder.this, R.color.checkbox_default_color)));
                            holder.imageButton_availability.setChecked(false);

                            if (stockCheckHelper.SHOW_STOCK_SP)
                                holder.shelfPcsQty.setText("");
                            if (stockCheckHelper.SHOW_STOCK_SC)
                                holder.shelfCaseQty.setText("");
                            if (stockCheckHelper.SHOW_SHELF_OUTER)
                                holder.shelfouter.setText("");

                        } else if (holder.productObj.getLocations()
                                .get(mSelectedLocationIndex).getAvailability() == 0) {
                            holder.productObj.getLocations()
                                    .get(mSelectedLocationIndex).setAvailability(1);

                            CompoundButtonCompat.setButtonTintList(holder.imageButton_availability, ColorStateList.valueOf(ContextCompat.getColor(StockAndOrder.this, R.color.colorAccent)));
                            holder.imageButton_availability.setChecked(true);

                            if (stockCheckHelper.SHOW_STOCK_SP)
                                holder.shelfPcsQty.setText("1");
                            else if (stockCheckHelper.SHOW_STOCK_SC)
                                holder.shelfCaseQty.setText("1");
                            else if (stockCheckHelper.SHOW_SHELF_OUTER)
                                holder.shelfouter.setText("1");

                        }
                    } else {
                        if (holder.productObj.getLocations()
                                .get(mSelectedLocationIndex).getAvailability() == -1) {
                            holder.productObj.getLocations()
                                    .get(mSelectedLocationIndex).setAvailability(1);

                            CompoundButtonCompat.setButtonTintList(holder.imageButton_availability, ColorStateList.valueOf(ContextCompat.getColor(StockAndOrder.this, R.color.colorAccent)));
                            holder.imageButton_availability.setChecked(true);

                            if (stockCheckHelper.SHOW_STOCK_SP)
                                holder.shelfPcsQty.setText("1");
                            else if (stockCheckHelper.SHOW_STOCK_SC)
                                holder.shelfCaseQty.setText("1");
                            else if (stockCheckHelper.SHOW_SHELF_OUTER)
                                holder.shelfouter.setText("1");

                        } else if (holder.productObj.getLocations()
                                .get(mSelectedLocationIndex).getAvailability() == 1) {
                            holder.productObj.getLocations()
                                    .get(mSelectedLocationIndex).setAvailability(0);

                            CompoundButtonCompat.setButtonTintList(holder.imageButton_availability, ColorStateList.valueOf(ContextCompat.getColor(StockAndOrder.this, R.color.RED)));
                            holder.imageButton_availability.setChecked(true);

                            if (stockCheckHelper.SHOW_STOCK_SP)
                                holder.shelfPcsQty.setText("0");
                            if (stockCheckHelper.SHOW_STOCK_SC)
                                holder.shelfCaseQty.setText("0");
                            if (stockCheckHelper.SHOW_SHELF_OUTER)
                                holder.shelfouter.setText("0");

                        } else if (holder.productObj.getLocations()
                                .get(mSelectedLocationIndex).getAvailability() == 0) {
                            holder.productObj.getLocations()
                                    .get(mSelectedLocationIndex).setAvailability(-1);

                            CompoundButtonCompat.setButtonTintList(holder.imageButton_availability, ColorStateList.valueOf(ContextCompat.getColor(StockAndOrder.this, R.color.checkbox_default_color)));
                            holder.imageButton_availability.setChecked(false);

                            // if (bmodel.configurationMasterHelper.SHOW_STOCK_SP)
                            holder.shelfPcsQty.setText("");
                            // if (bmodel.configurationMasterHelper.SHOW_STOCK_SC)
                            holder.shelfCaseQty.setText("");
                            //if (bmodel.configurationMasterHelper.SHOW_SHELF_OUTER)
                            holder.shelfouter.setText("");

                        }
                    }


                    updateValue();

                }
            });


            holder.shelfCaseQty.addTextChangedListener(new TextWatcher() {
                public void afterTextChanged(Editable s) {
                    String qty = s.toString();
                    if (qty.length() > 0) {
                        holder.shelfCaseQty.setSelection(qty.length());
                    }
                    if (!"".equals(qty)) {
                        int shelf_case_qty = SDUtil.convertToInt(s.toString());
                        holder.productObj
                                .getLocations()
                                .get(mSelectedLocationIndex)
                                .setShelfCase(
                                        SDUtil.convertToInt(holder.shelfCaseQty
                                                .getText().toString()));


                        if (shelf_case_qty > 0
                                || SDUtil.convertToInt(holder.shelfPcsQty.getText().toString()) > 0
                                || SDUtil.convertToInt(holder.shelfouter.getText().toString()) > 0) {
                            holder.productObj.getLocations()
                                    .get(mSelectedLocationIndex).setAvailability(1);
                            CompoundButtonCompat.setButtonTintList(holder.imageButton_availability, ColorStateList.valueOf(ContextCompat.getColor(StockAndOrder.this, R.color.colorAccent)));
                            holder.imageButton_availability.setChecked(true);

                        } else if (shelf_case_qty == 0) {
                            holder.productObj.getLocations()
                                    .get(mSelectedLocationIndex).setAvailability(0);
                            CompoundButtonCompat.setButtonTintList(holder.imageButton_availability, ColorStateList.valueOf(ContextCompat.getColor(StockAndOrder.this, R.color.RED)));
                            holder.imageButton_availability.setChecked(true);
                        }

                        holder.shelfCaseQty.removeTextChangedListener(this);
                        holder.shelfCaseQty.addTextChangedListener(this);
                        if (bmodel.configurationMasterHelper.IS_SUGGESTED_ORDER_LOGIC)
                            calculateSO(holder.productObj, SOLogic, holder);

                        if (bmodel.configurationMasterHelper.SHOW_STK_QTY_IN_ORDER
                                && (!bmodel.configurationMasterHelper.IS_SUGGESTED_ORDER_LOGIC
                                || (bmodel.configurationMasterHelper.IS_SUGGESTED_ORDER_LOGIC && SOLogic != 1))) {

                            String totalStockInPiece = getProductTotalValue(holder);
                            holder.text_stock.setText(totalStockInPiece);

                        }

                    } else {
                        holder.productObj
                                .getLocations()
                                .get(mSelectedLocationIndex)
                                .setShelfCase(-1);

                        if (holder.productObj.getLocations()
                                .get(mSelectedLocationIndex).getShelfCase() == -1
                                && holder.productObj.getLocations()
                                .get(mSelectedLocationIndex).getShelfPiece() == -1
                                && holder.productObj.getLocations()
                                .get(mSelectedLocationIndex).getShelfOuter() == -1) {
                            holder.productObj.getLocations()
                                    .get(mSelectedLocationIndex).setAvailability(-1);
                            CompoundButtonCompat.setButtonTintList(holder.imageButton_availability, ColorStateList.valueOf(ContextCompat.getColor(StockAndOrder.this, R.color.checkbox_default_color)));
                            holder.imageButton_availability.setChecked(false);
                        }
                    }
                }

                @Override
                public void beforeTextChanged(CharSequence s, int start,
                                              int count, int after) {
                }

                @Override
                public void onTextChanged(CharSequence s, int start,
                                          int before, int count) {
                }
            });

            if (bmodel.configurationMasterHelper.SHOW_CUSTOM_KEYBOARD_NEW) {

                holder.shelfCaseQty.setFocusable(false);

                holder.shelfCaseQty.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        if (bmodel.configurationMasterHelper.SHOW_SIH_IN_PNAME) {
                            strProductObj = "[SIH :"
                                    + holder.productObj.getSIH() + "] "
                                    + holder.pname;
                            productSearch.setProductNameOnBar(strProductObj);
                        } else
                            productSearch.setProductNameOnBar(holder.pname);

                        if (dialogCustomKeyBoard == null || !dialogCustomKeyBoard.isDialogCreated()) {
                            dialogCustomKeyBoard = new CustomKeyBoard(StockAndOrder.this, holder.shelfCaseQty);
                            dialogCustomKeyBoard.show();
                            dialogCustomKeyBoard.setCancelable(false);

                            //Grab the window of the dialog, and change the width
                            WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
                            Window window = dialogCustomKeyBoard.getWindow();
                            lp.copyFrom(window.getAttributes());
                            lp.width = (int) getResources().getDimension(R.dimen.custom_keyboard_width);
                            lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
                            window.setAttributes(lp);
                        }
                    }
                });
            } else {
                holder.shelfCaseQty.setFocusable(true);

                holder.shelfCaseQty.setOnTouchListener(new OnTouchListener() {
                    public boolean onTouch(View v, MotionEvent event) {
                        if (bmodel.configurationMasterHelper.SHOW_SIH_IN_PNAME) {
                            strProductObj = "[SIH :"
                                    + holder.productObj.getSIH() + "] "
                                    + holder.pname;
                            productSearch.setProductNameOnBar(strProductObj);
                        } else
                            productSearch.setProductNameOnBar(holder.pname);

                        QUANTITY = holder.shelfCaseQty;
                        QUANTITY.setTag(holder.productObj);
                        int inType = holder.shelfCaseQty.getInputType();
                        holder.shelfCaseQty.setInputType(InputType.TYPE_NULL);
                        holder.shelfCaseQty.onTouchEvent(event);
                        holder.shelfCaseQty.setInputType(inType);
                        holder.shelfCaseQty.requestFocus();
                        if (holder.shelfCaseQty.getText().length() > 0)
                            holder.shelfCaseQty.setSelection(holder.shelfCaseQty.getText().length());
                        productSearch.hideSoftInputFromWindow();
                        return true;
                    }
                });
            }

            holder.shelfPcsQty.addTextChangedListener(new TextWatcher() {
                public void afterTextChanged(Editable s) {
                    String qty = s.toString();
                    if (qty.length() > 0) {
                        holder.shelfPcsQty.setSelection(qty.length());
                    }
                    if (!"".equals(qty)) {
                        int sp_qty = SDUtil.convertToInt(holder.shelfPcsQty
                                .getText().toString());
                        holder.productObj.getLocations()
                                .get(mSelectedLocationIndex)
                                .setShelfPiece(sp_qty);

                        if (sp_qty > 0
                                || SDUtil.convertToInt(holder.shelfCaseQty.getText().toString()) > 0
                                || SDUtil.convertToInt(holder.shelfouter.getText().toString()) > 0) {
                            holder.productObj.getLocations()
                                    .get(mSelectedLocationIndex).setAvailability(1);
                            CompoundButtonCompat.setButtonTintList(holder.imageButton_availability, ColorStateList.valueOf(ContextCompat.getColor(StockAndOrder.this, R.color.colorAccent)));
                            holder.imageButton_availability.setChecked(true);

                        } else if (sp_qty == 0) {
                            holder.productObj.getLocations()
                                    .get(mSelectedLocationIndex).setAvailability(0);
                            CompoundButtonCompat.setButtonTintList(holder.imageButton_availability, ColorStateList.valueOf(ContextCompat.getColor(StockAndOrder.this, R.color.RED)));
                            holder.imageButton_availability.setChecked(true);
                        }


                        holder.shelfPcsQty.removeTextChangedListener(this);
                        holder.shelfPcsQty.addTextChangedListener(this);
                        if (bmodel.configurationMasterHelper.IS_SUGGESTED_ORDER_LOGIC)
                            calculateSO(holder.productObj, SOLogic, holder);

                        if (bmodel.configurationMasterHelper.SHOW_STK_QTY_IN_ORDER
                                && (!bmodel.configurationMasterHelper.IS_SUGGESTED_ORDER_LOGIC
                                || (bmodel.configurationMasterHelper.IS_SUGGESTED_ORDER_LOGIC && SOLogic != 1))) {

                            String totalStockInPiece = getProductTotalValue(holder);
                            holder.text_stock.setText(totalStockInPiece);
                        }

                    } else {
                        holder.productObj.getLocations()
                                .get(mSelectedLocationIndex)
                                .setShelfPiece(-1);

                        if (holder.productObj.getLocations()
                                .get(mSelectedLocationIndex).getShelfPiece() == -1
                                && holder.productObj.getLocations()
                                .get(mSelectedLocationIndex).getShelfCase() == -1
                                && holder.productObj.getLocations()
                                .get(mSelectedLocationIndex).getShelfOuter() == -1) {

                            holder.productObj.getLocations()
                                    .get(mSelectedLocationIndex).setAvailability(-1);
                            CompoundButtonCompat.setButtonTintList(holder.imageButton_availability, ColorStateList.valueOf(ContextCompat.getColor(StockAndOrder.this, R.color.checkbox_default_color)));
                            holder.imageButton_availability.setChecked(false);
                        }

                    }

                }

                @Override
                public void beforeTextChanged(CharSequence s, int start,
                                              int count, int after) {
                }

                @Override
                public void onTextChanged(CharSequence s, int start,
                                          int before, int count) {
                }
            });

            if (bmodel.configurationMasterHelper.SHOW_CUSTOM_KEYBOARD_NEW) {

                holder.shelfPcsQty.setFocusable(false);

                holder.shelfPcsQty.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        if (bmodel.configurationMasterHelper.SHOW_SIH_IN_PNAME) {
                            strProductObj = "[SIH :"
                                    + holder.productObj.getSIH() + "] "
                                    + holder.pname;
                            productSearch.setProductNameOnBar(strProductObj);
                        } else
                            productSearch.setProductNameOnBar(holder.pname);

                        if (dialogCustomKeyBoard == null || !dialogCustomKeyBoard.isDialogCreated()) {
                            dialogCustomKeyBoard = new CustomKeyBoard(StockAndOrder.this, holder.shelfPcsQty);
                            dialogCustomKeyBoard.show();
                            dialogCustomKeyBoard.setCancelable(false);

                            //Grab the window of the dialog, and change the width
                            WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
                            Window window = dialogCustomKeyBoard.getWindow();
                            lp.copyFrom(window.getAttributes());
                            lp.width = (int) getResources().getDimension(R.dimen.custom_keyboard_width);
                            lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
                            window.setAttributes(lp);
                        }
                    }
                });
            } else {
                holder.shelfPcsQty.setFocusable(true);

                holder.shelfPcsQty.setOnTouchListener(new OnTouchListener() {
                    public boolean onTouch(View v, MotionEvent event) {
                        if (bmodel.configurationMasterHelper.SHOW_SIH_IN_PNAME) {
                            strProductObj = "[SIH :"
                                    + holder.productObj.getSIH() + "] "
                                    + holder.pname;
                            productSearch.setProductNameOnBar(strProductObj);
                        } else
                            productSearch.setProductNameOnBar(holder.pname);

                        QUANTITY = holder.shelfPcsQty;
                        QUANTITY.setTag(holder.productObj);
                        int inType = holder.shelfPcsQty.getInputType();
                        holder.shelfPcsQty.setInputType(InputType.TYPE_NULL);
                        holder.shelfPcsQty.onTouchEvent(event);
                        holder.shelfPcsQty.setInputType(inType);
                        holder.shelfPcsQty.requestFocus();
                        if (holder.shelfPcsQty.getText().length() > 0)
                            holder.shelfPcsQty.setSelection(holder.shelfPcsQty.getText().length());
                        productSearch.hideSoftInputFromWindow();
                        return true;
                    }
                });
            }

            holder.shelfouter.addTextChangedListener(new TextWatcher() {
                @Override
                public void afterTextChanged(Editable s) {
                    String qty = s.toString();

                    if (qty.length() > 0) {
                        holder.shelfouter.setSelection(qty.length());
                    }
                    if (!"".equals(qty)) {
                        int shelfoqty = SDUtil
                                .convertToInt(holder.shelfouter.getText()
                                        .toString());
                        holder.productObj.getLocations()
                                .get(mSelectedLocationIndex)
                                .setShelfOuter(shelfoqty);

                        if (shelfoqty > 0
                                || SDUtil.convertToInt(holder.shelfPcsQty.getText().toString()) > 0
                                || SDUtil.convertToInt(holder.shelfCaseQty.getText().toString()) > 0) {
                            holder.productObj.getLocations()
                                    .get(mSelectedLocationIndex).setAvailability(1);
                            CompoundButtonCompat.setButtonTintList(holder.imageButton_availability, ColorStateList.valueOf(ContextCompat.getColor(StockAndOrder.this, R.color.colorAccent)));
                            holder.imageButton_availability.setChecked(true);

                        } else if (shelfoqty == 0) {
                            holder.productObj.getLocations()
                                    .get(mSelectedLocationIndex).setAvailability(0);
                            CompoundButtonCompat.setButtonTintList(holder.imageButton_availability, ColorStateList.valueOf(ContextCompat.getColor(StockAndOrder.this, R.color.RED)));
                            holder.imageButton_availability.setChecked(true);
                        }


                        holder.shelfouter.removeTextChangedListener(this);
                        holder.shelfouter.addTextChangedListener(this);
                        if (bmodel.configurationMasterHelper.IS_SUGGESTED_ORDER_LOGIC)
                            calculateSO(holder.productObj, SOLogic, holder);

                        if (bmodel.configurationMasterHelper.SHOW_STK_QTY_IN_ORDER
                                && (!bmodel.configurationMasterHelper.IS_SUGGESTED_ORDER_LOGIC
                                || (bmodel.configurationMasterHelper.IS_SUGGESTED_ORDER_LOGIC && SOLogic != 1))) {

                            String totalStockInPiece = getProductTotalValue(holder);
                            holder.text_stock.setText(totalStockInPiece);
                        }

                    } else {
                        holder.productObj.getLocations()
                                .get(mSelectedLocationIndex)
                                .setShelfOuter(-1);

                        if (holder.productObj.getLocations()
                                .get(mSelectedLocationIndex).getShelfOuter() == -1
                                && holder.productObj.getLocations()
                                .get(mSelectedLocationIndex).getShelfPiece() == -1
                                && holder.productObj.getLocations()
                                .get(mSelectedLocationIndex).getShelfCase() == -1) {
                            holder.productObj.getLocations()
                                    .get(mSelectedLocationIndex).setAvailability(-1);
                            CompoundButtonCompat.setButtonTintList(holder.imageButton_availability, ColorStateList.valueOf(ContextCompat.getColor(StockAndOrder.this, R.color.checkbox_default_color)));
                            holder.imageButton_availability.setChecked(false);
                        }
                    }
                }

                @Override
                public void onTextChanged(CharSequence s, int start,
                                          int before, int count) {
                }

                @Override
                public void beforeTextChanged(CharSequence s, int start,
                                              int count, int after) {
                }
            });

            if (bmodel.configurationMasterHelper.SHOW_CUSTOM_KEYBOARD_NEW) {

                holder.shelfouter.setFocusable(false);

                holder.shelfouter.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        if (bmodel.configurationMasterHelper.SHOW_SIH_IN_PNAME) {
                            strProductObj = "[SIH :"
                                    + holder.productObj.getSIH() + "] "
                                    + holder.pname;
                            productSearch.setProductNameOnBar(strProductObj);
                        } else
                            productSearch.setProductNameOnBar(holder.pname);

                        if (dialogCustomKeyBoard == null || !dialogCustomKeyBoard.isDialogCreated()) {
                            dialogCustomKeyBoard = new CustomKeyBoard(StockAndOrder.this, holder.shelfouter);
                            dialogCustomKeyBoard.show();
                            dialogCustomKeyBoard.setCancelable(false);

                            //Grab the window of the dialog, and change the width
                            WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
                            Window window = dialogCustomKeyBoard.getWindow();
                            lp.copyFrom(window.getAttributes());
                            lp.width = (int) getResources().getDimension(R.dimen.custom_keyboard_width);
                            lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
                            window.setAttributes(lp);
                        }
                    }

                });
            } else {
                holder.shelfouter.setFocusable(true);

                holder.shelfouter.setOnTouchListener(new OnTouchListener() {
                    public boolean onTouch(View v, MotionEvent event) {
                        if (bmodel.configurationMasterHelper.SHOW_SIH_IN_PNAME) {
                            strProductObj = "[SIH :"
                                    + holder.productObj.getSIH() + "] "
                                    + holder.pname;
                            productSearch.setProductNameOnBar(strProductObj);
                        } else
                            productSearch.setProductNameOnBar(holder.pname);

                        QUANTITY = holder.shelfouter;
                        QUANTITY.setTag(holder.productObj);
                        int inType = holder.shelfouter.getInputType();
                        holder.shelfouter.setInputType(InputType.TYPE_NULL);
                        holder.shelfouter.onTouchEvent(event);
                        holder.shelfouter.setInputType(inType);
                        holder.shelfouter.requestFocus();
                        if (holder.shelfouter.getText().length() > 0)
                            holder.shelfouter.setSelection(holder.shelfouter.getText().length());
                        productSearch.hideSoftInputFromWindow();
                        return true;
                    }
                });
            }

            holder.caseQty.addTextChangedListener(new TextWatcher() {
                @SuppressLint("StringFormatInvalid")
                public void afterTextChanged(Editable s) {
                    if (holder.productObj.getCaseSize() == 0) {
                        holder.caseQty.removeTextChangedListener(this);
                        holder.caseQty.setText("0");
                        holder.caseQty.addTextChangedListener(this);
                        return;
                    }

                    String qty = s.toString();
                    if (qty.length() > 0) {
                        holder.caseQty.setSelection(qty.length());
                    }
                    if (bmodel.configurationMasterHelper.IS_SHOW_ONLY_INDICATIVE_ORDER) {
                        if (SDUtil.convertToInt(qty) > holder.productObj.getIndicativeOrder_oc()) {
                            //Delete the last entered number and reset the qty
                            qty = qty.length() > 1 ? qty.substring(0,
                                    qty.length() - 1) : "0";

                            holder.caseQty.setText(qty);

                            Toast.makeText(
                                    StockAndOrder.this,
                                    getResources().getString(
                                            R.string.exceed_indicative_order),
                                    Toast.LENGTH_SHORT).show();
                            return;
                        }
                    }
                    float totalQty = (SDUtil.convertToInt(qty) * holder.productObj
                            .getCaseSize())
                            + (holder.productObj.getOrderedPcsQty())
                            + (holder.productObj.getOrderedOuterQty() * holder.productObj
                            .getOutersize());

                    holder.weight.setText(Utils.formatAsTwoDecimal((double) (totalQty * holder.productObj.getWeight())));

                    if (holder.productObj.isAllocation() == 1
                            && bmodel.configurationMasterHelper.IS_SIH_VALIDATION) {
                        if ((totalQty + holder.productObj.getRepCaseQty()) <= holder.productObj.getSIH()) {
                            if (!"".equals(qty)) {
                                holder.productObj.setOrderedCaseQty(SDUtil
                                        .convertToInt(qty));
                            }

                            double tot = (holder.productObj
                                    .getOrderedCaseQty() * holder.productObj
                                    .getCsrp())
                                    + (holder.productObj.getOrderedPcsQty() * holder.productObj
                                    .getSrp())
                                    + (holder.productObj
                                    .getOrderedOuterQty() * holder.productObj
                                    .getOsrp());
                            holder.total.setText(bmodel.formatValue(tot));
                            holder.productObj.setTotalamount(tot);
                        } else {
                            if (!"0".equals(qty)) {
                                Toast.makeText(
                                        StockAndOrder.this,
                                        String.format(
                                                getResources().getString(
                                                        R.string.exceed),
                                                holder.productObj.getSIH()),
                                        Toast.LENGTH_SHORT).show();

                                //Delete the last entered number and reset the qty
                                qty = qty.length() > 1 ? qty.substring(0,
                                        qty.length() - 1) : "0";

                                if (!bmodel.configurationMasterHelper.IS_SHOW_DEFAULT_UOM)
                                    holder.caseQty.setText(qty);
                                else
                                    holder.uom_qty.setText(qty);

                                holder.productObj.setOrderedCaseQty(SDUtil
                                        .convertToInt(qty));
                            }
                        }
                    } else if (holder.productObj.isCbsihAvailable()) {
                        if ((totalQty + holder.productObj.getRepCaseQty()) <= holder.productObj.getCpsih()) {
                            if (!"".equals(qty)) {
                                holder.productObj.setOrderedCaseQty(SDUtil
                                        .convertToInt(qty));
                            }

                            double tot = (holder.productObj.getOrderedCaseQty() * holder.productObj
                                    .getCsrp())
                                    + (holder.productObj.getOrderedPcsQty() * holder.productObj
                                    .getSrp())
                                    + (holder.productObj.getOrderedOuterQty() * holder.productObj
                                    .getOsrp());
                            String strFormatValue = bmodel.formatValue(tot) + "";
                            holder.total.setText(strFormatValue);
                            holder.productObj.setTotalamount(tot);
                        } else {
                            if (!"0".equals(qty)) {
                                Toast.makeText(
                                        StockAndOrder.this,
                                        String.format(
                                                getResources().getString(
                                                        R.string.exceed),
                                                holder.productObj.getCpsih()),
                                        Toast.LENGTH_SHORT).show();

                                //Delete the last entered number and reset the qty
                                qty = qty.length() > 1 ? qty.substring(0,
                                        qty.length() - 1) : "0";

                                if (!bmodel.configurationMasterHelper.IS_SHOW_DEFAULT_UOM)
                                    holder.caseQty.setText(qty);
                                else
                                    holder.uom_qty.setText(qty);

                                holder.productObj.setOrderedCaseQty(SDUtil
                                        .convertToInt(qty));
                            }
                        }
                    } else {
                        if (!"".equals(qty)) {
                            holder.productObj.setOrderedCaseQty(SDUtil
                                    .convertToInt(qty));
                        }

                        double tot = (holder.productObj.getOrderedCaseQty() * holder.productObj
                                .getCsrp())
                                + (holder.productObj.getOrderedPcsQty() * holder.productObj
                                .getSrp())
                                + (holder.productObj.getOrderedOuterQty() * holder.productObj
                                .getOsrp());
                        String strFormatValue = bmodel.formatValue(tot) + "";
                        holder.total.setText(strFormatValue);
                        holder.productObj.setTotalamount(tot);
                    }
                    if (bmodel.configurationMasterHelper.IS_SHOW_ORDERING_SEQUENCE)
                        updateData(holder.productObj);

                    updateOrderedCount();
                    updateScreenTitle();

                }

                public void beforeTextChanged(CharSequence s, int start,
                                              int count, int after) {
                }

                public void onTextChanged(CharSequence s, int start,
                                          int before, int count) {
                }
            });

            if (bmodel.configurationMasterHelper.SHOW_CUSTOM_KEYBOARD_NEW) {

                holder.caseQty.setFocusable(false);

                holder.caseQty.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        if (bmodel.configurationMasterHelper.SHOW_SIH_IN_PNAME) {
                            strProductObj = "[SIH :"
                                    + holder.productObj.getSIH() + "] "
                                    + holder.pname;
                            productSearch.setProductNameOnBar(strProductObj);
                        } else
                            productSearch.setProductNameOnBar(holder.pname);

                        if (dialogCustomKeyBoard == null || !dialogCustomKeyBoard.isDialogCreated()) {
                            dialogCustomKeyBoard = new CustomKeyBoard(StockAndOrder.this, holder.caseQty);
                            dialogCustomKeyBoard.show();
                            dialogCustomKeyBoard.setCancelable(false);

                            //Grab the window of the dialog, and change the width
                            WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
                            Window window = dialogCustomKeyBoard.getWindow();
                            lp.copyFrom(window.getAttributes());
                            lp.width = (int) getResources().getDimension(R.dimen.custom_keyboard_width);
                            lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
                            window.setAttributes(lp);
                        }
                    }
                });
            } else {
                holder.caseQty.setFocusable(true);

                holder.caseQty.setOnTouchListener(new OnTouchListener() {
                    public boolean onTouch(View v, MotionEvent event) {
                        if (bmodel.configurationMasterHelper.SHOW_SIH_IN_PNAME) {
                            strProductObj = "[SIH :"
                                    + holder.productObj.getSIH() + "] "
                                    + holder.pname;
                            productSearch.setProductNameOnBar(strProductObj);
                        } else
                            productSearch.setProductNameOnBar(holder.pname);

                        QUANTITY = holder.caseQty;
                        QUANTITY.setTag(holder.productObj);
                        int inType = holder.caseQty.getInputType();
                        holder.caseQty.setInputType(InputType.TYPE_NULL);
                        holder.caseQty.onTouchEvent(event);
                        holder.caseQty.setInputType(inType);
                        holder.caseQty.requestFocus();
                        if (holder.caseQty.getText().length() > 0)
                            holder.caseQty.setSelection(holder.caseQty.getText().length());
                        productSearch.hideSoftInputFromWindow();
                        return true;
                    }
                });
            }


            holder.uom_qty.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {

                }

                @Override
                public void afterTextChanged(Editable s) {

                    int qty = SDUtil.convertToInt(s.toString());

                    if (s.toString().length() > 0) {
                        holder.uom_qty.setSelection(s.toString().length());
                    }

                    if ((holder.productObj.getPcUomid() != 0 && holder.productObj.getSelectedUomId() != 0) &&
                            holder.productObj.getPcUomid() == holder.productObj.getSelectedUomId()) {
                        holder.productObj.setOrderedCaseQty(0);
                        holder.productObj.setOrderedOuterQty(0);
                        holder.pcsQty.setText(qty + "");
                    } else if ((holder.productObj.getCaseUomId() != 0 && holder.productObj.getSelectedUomId() != 0) &&
                            holder.productObj.getCaseUomId() == holder.productObj.getSelectedUomId()) {
                        holder.productObj.setOrderedPcsQty(0);
                        holder.productObj.setOrderedOuterQty(0);
                        holder.caseQty.setText(qty + "");
                    } else if ((holder.productObj.getOuUomid() != 0 && holder.productObj.getSelectedUomId() != 0) &&
                            holder.productObj.getOuUomid() == holder.productObj.getSelectedUomId()) {
                        holder.productObj.setOrderedPcsQty(0);
                        holder.productObj.setOrderedCaseQty(0);
                        holder.outerQty.setText(qty + "");

                    }


                }
            });

            holder.uom_qty.setOnTouchListener(new OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    if (bmodel.configurationMasterHelper.SHOW_SIH_IN_PNAME) {
                        strProductObj = "[SIH :"
                                + holder.productObj.getSIH() + "] "
                                + holder.pname;
                        productSearch.setProductNameOnBar(strProductObj);
                    } else
                        productSearch.setProductNameOnBar(holder.pname);

                    QUANTITY = holder.uom_qty;
                    QUANTITY.setTag(holder.productObj);
                    int inType = holder.uom_qty.getInputType();
                    holder.uom_qty.setInputType(InputType.TYPE_NULL);
                    holder.uom_qty.onTouchEvent(event);
                    holder.uom_qty.setInputType(inType);
                    holder.uom_qty.requestFocus();
                    if (holder.uom_qty.getText().length() > 0)
                        holder.uom_qty.setSelection(holder.uom_qty.getText().length());

                    return true;
                }
            });


            holder.pcsQty.addTextChangedListener(new TextWatcher() {
                public void afterTextChanged(Editable s) {
                    if (holder.productObj.getPcUomid() == 0) {
                        holder.pcsQty.removeTextChangedListener(this);
                        holder.pcsQty.setText("0");
                        holder.pcsQty.addTextChangedListener(this);
                        return;
                    }

                    String qty = s.toString();
                    if (qty.length() > 0) {
                        holder.pcsQty.setSelection(qty.length());
                    }
                    /** Calculate the total pcs qty **/
                    float totalQty = (holder.productObj.getOrderedCaseQty() * holder.productObj
                            .getCaseSize())
                            + (SDUtil.convertToInt(qty))
                            + (holder.productObj.getOrderedOuterQty() * holder.productObj
                            .getOutersize());

                    holder.weight.setText(Utils.formatAsTwoDecimal((double) (totalQty * holder.productObj.getWeight())));

                    if (holder.productObj.isAllocation() == 1
                            && bmodel.configurationMasterHelper.IS_SIH_VALIDATION) {
                        if ((totalQty + holder.productObj.getRepPieceQty()) <= holder.productObj.getSIH()) {
                            if (!"".equals(qty)) {
                                holder.productObj.setOrderedPcsQty(SDUtil
                                        .convertToInt(qty));
                            }
                            double tot = (holder.productObj
                                    .getOrderedCaseQty() * holder.productObj
                                    .getCsrp())
                                    + (holder.productObj.getOrderedPcsQty() * holder.productObj
                                    .getSrp())
                                    + (holder.productObj
                                    .getOrderedOuterQty() * holder.productObj
                                    .getOsrp());
                            holder.total.setText(bmodel.formatValue(tot));
                            holder.productObj.setTotalamount(tot);
                        } else {
                            if (!"0".equals(qty)) {
                                Toast.makeText(
                                        StockAndOrder.this,
                                        String.format(
                                                getResources().getString(
                                                        R.string.exceed),
                                                holder.productObj.getSIH()),
                                        Toast.LENGTH_SHORT).show();
                                //Delete the last entered number and reset the qty
                                qty = qty.length() > 1 ? qty.substring(0,
                                        qty.length() - 1) : "0";
                                holder.productObj.setOrderedPcsQty(SDUtil
                                        .convertToInt(qty));
                                if (!bmodel.configurationMasterHelper.IS_SHOW_DEFAULT_UOM)
                                    holder.pcsQty.setText(qty);
                                else
                                    holder.uom_qty.setText(qty);
                            }
                        }
                    } else if (holder.productObj.isCbsihAvailable()) {
                        if ((totalQty + holder.productObj.getRepPieceQty()) <= holder.productObj.getCpsih()) {
                            if (!"".equals(qty)) {
                                holder.productObj.setOrderedPcsQty(SDUtil
                                        .convertToInt(qty));
                            }

                            double tot = (holder.productObj.getOrderedCaseQty() * holder.productObj
                                    .getCsrp())
                                    + (holder.productObj.getOrderedPcsQty() * holder.productObj
                                    .getSrp())
                                    + (holder.productObj.getOrderedOuterQty() * holder.productObj
                                    .getOsrp());
                            String strTotal = bmodel.formatValue(tot) + "";
                            holder.total.setText(strTotal);
                            holder.productObj.setTotalamount(tot);
                        } else {
                            if (!"0".equals(qty)) {
                                Toast.makeText(
                                        StockAndOrder.this,
                                        String.format(
                                                getResources().getString(
                                                        R.string.exceed),
                                                holder.productObj.getCpsih()),
                                        Toast.LENGTH_SHORT).show();

                                //Delete the last entered number and reset the qty
                                qty = qty.length() > 1 ? qty.substring(0,
                                        qty.length() - 1) : "0";

                                if (!bmodel.configurationMasterHelper.IS_SHOW_DEFAULT_UOM)
                                    holder.pcsQty.setText(qty);
                                else
                                    holder.uom_qty.setText(qty);

                                holder.productObj.setOrderedPcsQty(SDUtil
                                        .convertToInt(qty));
                            }
                        }
                    } else {
                        if (!"".equals(qty)) {
                            holder.productObj.setOrderedPcsQty(SDUtil
                                    .convertToInt(qty));
                        }
                        double tot = (holder.productObj.getOrderedCaseQty() * holder.productObj
                                .getCsrp())
                                + (holder.productObj.getOrderedPcsQty() * holder.productObj
                                .getSrp())
                                + (holder.productObj.getOrderedOuterQty() * holder.productObj
                                .getOsrp());
                        String strTotal = bmodel.formatValue(tot) + "";
                        holder.total.setText(strTotal);
                        holder.productObj.setTotalamount(tot);
                    }
                    if (bmodel.configurationMasterHelper.IS_SHOW_ORDERING_SEQUENCE)
                        updateData(holder.productObj);

                    updateOrderedCount();
                    updateScreenTitle();

                }

                public void beforeTextChanged(CharSequence s, int start,
                                              int count, int after) {
                }

                public void onTextChanged(CharSequence s, int start,
                                          int before, int count) {
                }
            });
            if (bmodel.configurationMasterHelper.SHOW_CUSTOM_KEYBOARD_NEW) {

                holder.pcsQty.setFocusable(false);

                holder.pcsQty.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        if (bmodel.configurationMasterHelper.SHOW_SIH_IN_PNAME) {
                            strProductObj = "[SIH :" + holder.productObj.getSIH() + "] "
                                    + holder.pname;
                            productSearch.setProductNameOnBar(strProductObj);
                        } else
                            productSearch.setProductNameOnBar(holder.pname);

                        if (dialogCustomKeyBoard == null || !dialogCustomKeyBoard.isDialogCreated()) {
                            dialogCustomKeyBoard = new CustomKeyBoard(StockAndOrder.this, holder.pcsQty);
                            dialogCustomKeyBoard.show();
                            dialogCustomKeyBoard.setCancelable(false);

                            //Grab the window of the dialog, and change the width
                            WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
                            Window window = dialogCustomKeyBoard.getWindow();
                            lp.copyFrom(window.getAttributes());
                            lp.width = (int) getResources().getDimension(R.dimen.custom_keyboard_width);
                            lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
                            window.setAttributes(lp);
                        }
                    }
                });
            } else {
                holder.pcsQty.setFocusable(true);

                holder.pcsQty.setOnTouchListener(new OnTouchListener() {
                    public boolean onTouch(View v, MotionEvent event) {
                        if (bmodel.configurationMasterHelper.SHOW_SIH_IN_PNAME) {
                            strProductObj = "[SIH :"
                                    + holder.productObj.getSIH() + "] "
                                    + holder.pname;
                            productSearch.setProductNameOnBar(strProductObj);
                        } else
                            productSearch.setProductNameOnBar(holder.pname);

                        QUANTITY = holder.pcsQty;
                        QUANTITY.setTag(holder.productObj);
                        int inType = holder.pcsQty.getInputType();
                        holder.pcsQty.setInputType(InputType.TYPE_NULL);
                        holder.pcsQty.onTouchEvent(event);
                        holder.pcsQty.setInputType(inType);
                        holder.pcsQty.requestFocus();
                        if (holder.pcsQty.getText().length() > 0)
                            holder.pcsQty.setSelection(holder.pcsQty.getText().length());
                        productSearch.hideSoftInputFromWindow();
                        return true;
                    }
                });
            }

            holder.outerQty.addTextChangedListener(new TextWatcher() {
                @Override
                public void onTextChanged(CharSequence s, int start,
                                          int before, int count) {
                    if (holder.productObj.getOuUomid() == 0) {
                        holder.outerQty.removeTextChangedListener(this);
                        holder.outerQty.setText("0");
                        holder.outerQty.addTextChangedListener(this);
                        return;
                    }
                    String qty = s.toString();
                    if (qty.length() > 0) {
                        holder.outerQty.setSelection(qty.length());
                    }
                    float totalQty = (SDUtil.convertToInt(qty) * holder.productObj
                            .getOutersize())
                            + (holder.productObj.getOrderedCaseQty() * holder.productObj
                            .getCaseSize())
                            + +(holder.productObj.getOrderedPcsQty());
                    holder.weight.setText(Utils.formatAsTwoDecimal((double) (totalQty * holder.productObj.getWeight())));
                    if (holder.productObj.isAllocation() == 1
                            && bmodel.configurationMasterHelper.IS_SIH_VALIDATION) {
                        if ((totalQty + holder.productObj.getRepOuterQty()) <= holder.productObj.getSIH()) {
                            if (!"".equals(qty)) {
                                holder.productObj.setOrderedOuterQty(SDUtil
                                        .convertToInt(qty));
                            }

                            double tot = (holder.productObj
                                    .getOrderedCaseQty() * holder.productObj
                                    .getCsrp())
                                    + (holder.productObj.getOrderedPcsQty() * holder.productObj
                                    .getSrp())
                                    + (holder.productObj
                                    .getOrderedOuterQty() * holder.productObj
                                    .getOsrp());
                            holder.total.setText(bmodel.formatValue(tot));
                            holder.productObj.setTotalamount(tot);
                        } else {
                            if (!"0".equals(qty)) {
                                Toast.makeText(
                                        StockAndOrder.this,
                                        String.format(
                                                getResources().getString(
                                                        R.string.exceed),
                                                holder.productObj.getSIH()),
                                        Toast.LENGTH_SHORT).show();

                                qty = qty.length() > 1 ? qty.substring(0,
                                        qty.length() - 1) : "0";

                                holder.productObj.setOrderedOuterQty(SDUtil
                                        .convertToInt(qty));
                                if (!bmodel.configurationMasterHelper.IS_SHOW_DEFAULT_UOM)
                                    holder.outerQty.setText(qty);
                                else
                                    holder.uom_qty.setText(qty);

                            }
                        }
                    } else if (holder.productObj.isCbsihAvailable()) {
                        if ((totalQty + holder.productObj.getRepOuterQty()) <= holder.productObj.getCpsih()) {
                            if (!"".equals(qty)) {
                                holder.productObj.setOrderedOuterQty(SDUtil
                                        .convertToInt(qty));
                            }

                            double tot = (holder.productObj.getOrderedCaseQty() * holder.productObj
                                    .getCsrp())
                                    + (holder.productObj.getOrderedPcsQty() * holder.productObj
                                    .getSrp())
                                    + (holder.productObj.getOrderedOuterQty() * holder.productObj
                                    .getOsrp());
                            String strFormatValue = bmodel.formatValue(tot) + "";
                            holder.total.setText(strFormatValue);
                            holder.productObj.setTotalamount(tot);
                        } else {
                            if (!"0".equals(qty)) {
                                Toast.makeText(
                                        StockAndOrder.this,
                                        String.format(
                                                getResources().getString(
                                                        R.string.exceed),
                                                holder.productObj.getCpsih()),
                                        Toast.LENGTH_SHORT).show();

                                //Delete the last entered number and reset the qty
                                qty = qty.length() > 1 ? qty.substring(0,
                                        qty.length() - 1) : "0";

                                if (!bmodel.configurationMasterHelper.IS_SHOW_DEFAULT_UOM)
                                    holder.outerQty.setText(qty);
                                else
                                    holder.uom_qty.setText(qty);

                                holder.productObj.setOrderedOuterQty(SDUtil
                                        .convertToInt(qty));
                            }
                        }
                    } else {
                        if (!"".equals(qty)) {
                            holder.productObj.setOrderedOuterQty(SDUtil
                                    .convertToInt(qty));
                        }

                        double tot = (holder.productObj.getOrderedCaseQty() * holder.productObj
                                .getCsrp())
                                + (holder.productObj.getOrderedPcsQty() * holder.productObj
                                .getSrp())
                                + (holder.productObj.getOrderedOuterQty() * holder.productObj
                                .getOsrp());
                        String strFormatValue = bmodel.formatValue(tot) + "";
                        holder.total.setText(strFormatValue);
                        holder.productObj.setTotalamount(tot);
                    }
                    if (bmodel.configurationMasterHelper.IS_SHOW_ORDERING_SEQUENCE)
                        updateData(holder.productObj);

                    updateOrderedCount();
                    updateScreenTitle();
                }

                @Override
                public void afterTextChanged(Editable s) {
                }

                @Override
                public void beforeTextChanged(CharSequence s, int start,
                                              int count, int after) {
                }

            });
            if (bmodel.configurationMasterHelper.SHOW_CUSTOM_KEYBOARD_NEW) {

                holder.outerQty.setFocusable(false);

                holder.outerQty.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        if (bmodel.configurationMasterHelper.SHOW_SIH_IN_PNAME) {
                            strProductObj = "[SIH :"
                                    + holder.productObj.getSIH() + "] "
                                    + holder.pname;
                            productSearch.setProductNameOnBar(strProductObj);
                        } else
                            productSearch.setProductNameOnBar(holder.pname);

                        if (dialogCustomKeyBoard == null || !dialogCustomKeyBoard.isDialogCreated()) {
                            dialogCustomKeyBoard = new CustomKeyBoard(StockAndOrder.this, holder.outerQty);
                            dialogCustomKeyBoard.show();
                            dialogCustomKeyBoard.setCancelable(false);

                            //Grab the window of the dialog, and change the width
                            WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
                            Window window = dialogCustomKeyBoard.getWindow();
                            lp.copyFrom(window.getAttributes());
                            lp.width = (int) getResources().getDimension(R.dimen.custom_keyboard_width);
                            lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
                            window.setAttributes(lp);
                        }
                    }
                });
            } else {
                holder.outerQty.setFocusable(true);

                holder.outerQty.setOnTouchListener(new OnTouchListener() {
                    public boolean onTouch(View v, MotionEvent event) {
                        if (bmodel.configurationMasterHelper.SHOW_SIH_IN_PNAME) {
                            strProductObj = "[SIH :"
                                    + holder.productObj.getSIH() + "] "
                                    + holder.pname;
                            productSearch.setProductNameOnBar(strProductObj);
                        } else
                            productSearch.setProductNameOnBar(holder.pname);

                        QUANTITY = holder.outerQty;
                        QUANTITY.setTag(holder.productObj);
                        int inType = holder.outerQty.getInputType();
                        holder.outerQty.setInputType(InputType.TYPE_NULL);
                        holder.outerQty.onTouchEvent(event);
                        holder.outerQty.setInputType(inType);
                        holder.outerQty.requestFocus();
                        if (holder.outerQty.getText().length() > 0)
                            holder.outerQty.setSelection(holder.outerQty.getText().length());
                        productSearch.hideSoftInputFromWindow();
                        return true;
                    }
                });
            }
            if (bmodel.configurationMasterHelper.SHOW_CUSTOM_KEYBOARD_NEW) {

                holder.srpEdit.setFocusable(false);

                holder.srpEdit.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        if (bmodel.configurationMasterHelper.SHOW_SIH_IN_PNAME) {
                            strProductObj = "[SIH :"
                                    + holder.productObj.getSIH() + "] "
                                    + holder.pname;
                            productSearch.setProductNameOnBar(strProductObj);
                        } else
                            productSearch.setProductNameOnBar(holder.pname);

                        if (dialogCustomKeyBoard == null || !dialogCustomKeyBoard.isDialogCreated()) {
                            dialogCustomKeyBoard = new CustomKeyBoard(StockAndOrder.this, holder.shelfPcsQty);
                            dialogCustomKeyBoard.show();
                            dialogCustomKeyBoard.setCancelable(false);

                            //Grab the window of the dialog, and change the width
                            WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
                            Window window = dialogCustomKeyBoard.getWindow();
                            lp.copyFrom(window.getAttributes());
                            lp.width = (int) getResources().getDimension(R.dimen.custom_keyboard_width);
                            lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
                            window.setAttributes(lp);
                        }
                    }
                });
            } else {
                holder.srpEdit.setFocusable(true);

                holder.srpEdit.setOnTouchListener(new OnTouchListener() {
                    public boolean onTouch(View v, MotionEvent event) {
                        if (bmodel.configurationMasterHelper.SHOW_SIH_IN_PNAME) {
                            strProductObj = "[SIH :"
                                    + holder.productObj.getSIH() + "] "
                                    + holder.pname;
                            productSearch.setProductNameOnBar(strProductObj);
                        } else
                            productSearch.setProductNameOnBar(holder.pname);

                        QUANTITY = holder.srpEdit;
                        QUANTITY.setTag(holder.productObj);
                        int inType = holder.srpEdit.getInputType();
                        holder.srpEdit.setInputType(InputType.TYPE_NULL);
                        holder.srpEdit.onTouchEvent(event);
                        holder.srpEdit.setInputType(inType);
                        holder.srpEdit.requestFocus();
                        if (holder.srpEdit.getText().length() > 0)
                            holder.srpEdit.setSelection(holder.srpEdit.getText().length());
                        productSearch.hideSoftInputFromWindow();
                        return true;
                    }
                });
            }

            holder.srpEdit.addTextChangedListener(new TextWatcher() {
                @Override
                public void afterTextChanged(Editable s) {
                    String qty = s.toString();
                    if (qty.length() > 0) {
                        holder.srpEdit.setSelection(qty.length());
                    }
                    if (!"".equals(qty)) {
                        if (SDUtil.isValidDecimal(qty, 8, 2)) {

                            holder.productObj.setSrp(SDUtil.convertToFloat(SDUtil.format(SDUtil.convertToFloat(qty), bmodel.configurationMasterHelper.PRECISION_COUNT_FOR_CALCULATION, 0)));

                            float csrp = holder.productObj.getCaseSize() * SDUtil.convertToFloat(qty);
                            holder.productObj.setCsrp(SDUtil.convertToFloat(SDUtil.format(csrp, bmodel.configurationMasterHelper.PRECISION_COUNT_FOR_CALCULATION, 0)));

                            float osrp = holder.productObj.getOutersize() * SDUtil.convertToFloat(qty);
                            holder.productObj.setOsrp(SDUtil.convertToFloat(SDUtil.format(osrp, bmodel.configurationMasterHelper.PRECISION_COUNT_FOR_CALCULATION, 0)));
                        } else {
                            holder.srpEdit.setText(qty.length() > 1 ? qty
                                    .substring(0, qty.length() - 1) : "0");
                        }
                    } else {
                        holder.productObj.setSrp(0);
                        holder.productObj.setCsrp(0);
                        holder.productObj.setOsrp(0);
                    }
                    double tot = (holder.productObj.getOrderedCaseQty() * holder.productObj
                            .getCsrp())
                            + (holder.productObj.getOrderedPcsQty() * holder.productObj
                            .getSrp())
                            + (holder.productObj.getOrderedOuterQty() * holder.productObj
                            .getOsrp());
                    String strTotal = bmodel.formatValue(tot) + "";
                    holder.total.setText(strTotal);
                    holder.productObj.setTotalamount(tot);

                }

                @Override
                public void onTextChanged(CharSequence s, int start,
                                          int before, int count) {
                }

                @Override
                public void beforeTextChanged(CharSequence s, int start,
                                              int count, int after) {
                }
            });


            holder.so.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (bmodel.configurationMasterHelper.ALLOW_SO_COPY) {
                        holder.pcsQty.setText(holder.so.getText()
                                .toString());
                        updateValue();
                    }
                }
            });

            holder.socs.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (bmodel.configurationMasterHelper.ALLOW_SO_COPY) {
                        holder.caseQty.setText(holder.socs.getText()
                                .toString());
                        updateValue();
                    }
                }
            });

            row.setOnClickListener(new OnClickListener() {
                public void onClick(View v) {
                    if (bmodel.configurationMasterHelper.SHOW_SIH_IN_PNAME) {
                        productSearch.setProductNameOnBar(strProductObj = "[SIH :"
                                + holder.productObj.getSIH() + "] "
                                + holder.pname);
                    } else
                        productSearch.setProductNameOnBar(holder.pname);


                }
            });


            holder.salesReturn.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    View vChild = lvwplist.getChildAt(0);
                    int holderPosition = lvwplist.getFirstVisiblePosition();
                    int holderTop = (vChild == null) ? 0 : (vChild.getTop() - lvwplist.getPaddingTop());

                    productSearch.setProductNameOnBar(holder.pname);
                    showSalesReturnDialog(holder.productObj.getProductID(), v, holderPosition, holderTop);
                }
            });


            holder.psname.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View view) {
                    bmodel = (BusinessModel) getApplicationContext();
                    bmodel.setContext(StockAndOrder.this);

                    SchemeDetailsMasterHelper schemeHelper = SchemeDetailsMasterHelper.getInstance(getApplicationContext());

                    //if (bmodel.configurationMasterHelper.IS_PRODUCT_SCHEME_DIALOG || bmodel.configurationMasterHelper.IS_SCHEME_DIALOG) {
                    if (schemeHelper
                            .getSchemeList() == null
                            || schemeHelper
                            .getSchemeList().size() == 0) {
                        Toast.makeText(StockAndOrder.this,
                                R.string.scheme_not_available,
                                Toast.LENGTH_SHORT).show();
                    }

                    //This objects reference is used only in Product Detail screen.
                    // This should be removed while cleaning product detail screen
                    bmodel.productHelper.setSchemes(schemeHelper.getSchemeList());
                    bmodel.productHelper.setPdname(holder.pname);
                    bmodel.productHelper.setProdId(holder.productId);
                    bmodel.productHelper.setProductObj(holder.productObj);
                    bmodel.productHelper.setFlag(1);
                    bmodel.productHelper.setTotalScreenSize(mTotalScreenWidth);

                    Intent intent = new Intent(StockAndOrder.this, ProductSchemeDetailsActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    intent.putExtra("productId", holder.productId);
                    startActivity(intent);

                    //}
                        /*else {
                            bmodel.productHelper.setPdname(holder.pname);
                            bmodel.productHelper.setProdId(holder.productId);
                            bmodel.productHelper.setProductObj(holder.productObj);
                            bmodel.productHelper.setFlag(1);
                            bmodel.productHelper.setTotalScreenSize(mTotalScreenWidth);

                            SchemeDialog sc = new SchemeDialog(
                                    StockAndOrder.this,
                                    schemeHelper
                                            .getSchemeList(), holder.pname,
                                    holder.productId, holder.productObj, 1, mTotalScreenWidth);
                            FragmentManager fm = getSupportFragmentManager();
                            sc.show(fm, "");
                        }*/
                }

            });
        }
        else {

            holder.tvbarcode = row
                    .findViewById(R.id.stock_and_order_listview_productbarcode);

            holder.tvbarcode.setTypeface(FontUtils.getFontRoboto(StockAndOrder.this, FontUtils.FontType.LIGHT));

            if (!bmodel.configurationMasterHelper.SHOW_BARCODE)
                holder.tvbarcode.setVisibility(View.GONE);

            holder.psname = row
                    .findViewById(R.id.stock_and_order_listview_productname);
           /* holder.tvProductCode = row
                    .findViewById(R.id.stock_and_order_listview_productcode);*/
            holder.foc = row
                    .findViewById(R.id.stock_and_order_listview_foc);
            holder.foc.setTypeface(FontUtils.getFontRoboto(StockAndOrder.this, FontUtils.FontType.MEDIUM));



        }


        /////////// common fields ///////////////

        row.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                if (bmodel.configurationMasterHelper.SHOW_SIH_IN_PNAME) {
                    productSearch.setProductNameOnBar(strProductObj = "[SIH :"
                            + holder.productObj.getSIH() + "] "
                            + holder.pname);
                } else
                    productSearch.setProductNameOnBar(holder.pname);


            }
        });

        if (!bmodel.configurationMasterHelper.SHOW_FOC)
            (row.findViewById(R.id.llFoc)).setVisibility(View.GONE);
        else {
            try {
                ((TextView) row.findViewById(R.id.focTitle)).setTypeface(FontUtils.getFontRoboto(StockAndOrder.this, FontUtils.FontType.LIGHT));
                if (bmodel.labelsMasterHelper.applyLabels(row.findViewById(
                        R.id.focTitle).getTag()) != null)
                    ((TextView) row.findViewById(R.id.focTitle))
                            .setText(bmodel.labelsMasterHelper
                                    .applyLabels(row.findViewById(
                                            R.id.focTitle).getTag()));
            } catch (Exception e) {
                Commons.printException(e + "");
            }
        }

        holder.foc.addTextChangedListener(new TextWatcher() {
            public void afterTextChanged(Editable s) {

                String qty = s.toString();
                if (qty.length() > 0) {
                    holder.foc.setSelection(qty.length());
                }
                if (qty == null || qty.trim().equals(""))
                    holder.productObj.setFoc(0);
                else
                    holder.productObj.setFoc(SDUtil.convertToInt(qty));

            }

            public void beforeTextChanged(CharSequence s, int start,
                                          int count, int after) {
            }

            public void onTextChanged(CharSequence s, int start,
                                      int before, int count) {
            }
        });

        if (bmodel.configurationMasterHelper.SHOW_CUSTOM_KEYBOARD_NEW) {

            holder.foc.setFocusable(false);

            holder.foc.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    if (bmodel.configurationMasterHelper.SHOW_SIH_IN_PNAME) {
                        strProductObj = "[SIH :" + holder.productObj.getSIH() + "] "
                                + holder.pname;
                        productSearch.setProductNameOnBar(strProductObj);
                    } else
                        productSearch.setProductNameOnBar(holder.pname);

                    if (dialogCustomKeyBoard == null || !dialogCustomKeyBoard.isDialogCreated()) {
                        dialogCustomKeyBoard = new CustomKeyBoard(StockAndOrder.this, holder.foc);
                        dialogCustomKeyBoard.show();
                        dialogCustomKeyBoard.setCancelable(false);

                        //Grab the window of the dialog, and change the width
                        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
                        Window window = dialogCustomKeyBoard.getWindow();
                        lp.copyFrom(window.getAttributes());
                        lp.width = (int) getResources().getDimension(R.dimen.custom_keyboard_width);
                        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
                        window.setAttributes(lp);
                    }
                }
            });
        } else {
            holder.foc.setFocusable(true);

            holder.foc.setOnTouchListener(new OnTouchListener() {
                public boolean onTouch(View v, MotionEvent event) {
                    if (bmodel.configurationMasterHelper.SHOW_SIH_IN_PNAME) {
                        strProductObj = "[SIH :"
                                + holder.productObj.getSIH() + "] "
                                + holder.pname;
                        productSearch.setProductNameOnBar(strProductObj);
                    } else
                        productSearch.setProductNameOnBar(holder.pname);

                    QUANTITY = holder.foc;
                    QUANTITY.setTag(holder.productObj);
                    int inType = holder.foc.getInputType();
                    holder.foc.setInputType(InputType.TYPE_NULL);
                    holder.foc.onTouchEvent(event);
                    holder.foc.setInputType(inType);
                    holder.foc.requestFocus();
                    if (holder.foc.getText().length() > 0)
                        holder.foc.setSelection(holder.foc.getText().length());
                    productSearch.hideSoftInputFromWindow();
                    return true;
                }
            });
        }

    }

    private void calculateSO(ProductMasterBO productObj, int SOLogic, ViewHolder holder) {

        int so = 0;
        if (SOLogic == 1) {
            String totalStockInPcs = getProductTotalValue(holder);
            so = productObj.getIco() - holder.productObj.getTotalStockQty();

            if (bmodel.configurationMasterHelper.SHOW_STK_QTY_IN_ORDER) {
                holder.text_stock.setText(totalStockInPcs);
            }

        } else if (SOLogic == 2) {
            so = productObj.getIco();
        } else {
            so = productObj.getIco();
        }

        if (so < 0)
            so = 0;

        String soPieceTitle = getString(R.string.so) + ": ";

        if (bmodel.labelsMasterHelper.applyLabels(holder.so.getTag()) != null)
            soPieceTitle = bmodel.labelsMasterHelper
                    .applyLabels(holder.so.getTag()) + ": ";

        if (bmodel.configurationMasterHelper.SHOW_SO_SPLIT) {

            String soCaseTitle = getString(R.string.so_case);

            if (bmodel.labelsMasterHelper.applyLabels(holder.socs.getTag()) != null)
                soCaseTitle = bmodel.labelsMasterHelper
                        .applyLabels(holder.socs.getTag()) + ": ";

            if (so < holder.productObj.getCaseSize()
                    || so == 0
                    || holder.productObj.getCaseSize() == 0) {

                holder.socs.setText(soCaseTitle + "0");
                String strSo = so + "";
                holder.so.setText(soPieceTitle + strSo);

                holder.productObj.setSoInventory(so);
                holder.productObj.setSocInventory(0);
            } else if (so == holder.productObj
                    .getCaseSize()) {
                String strSocs = so / holder.productObj
                        .getCaseSize() + "";
                holder.socs.setText(soCaseTitle + strSocs);
                holder.so.setText(soPieceTitle + "0");

                holder.productObj.setSoInventory(0);
                holder.productObj.setSocInventory(so
                        / holder.productObj.getCaseSize());
            } else {
                holder.productObj.setSoInventory(so
                        % holder.productObj.getCaseSize());
                holder.productObj.setSocInventory(so
                        / holder.productObj.getCaseSize());

                String strSocs = so / holder.productObj
                        .getCaseSize() + "";
                holder.socs.setText(soCaseTitle + strSocs);
                String strSo = so % holder.productObj
                        .getCaseSize() + "";
                holder.so.setText(soPieceTitle + strSo);
            }
        } else {
            String strSo = so + "";
            holder.so.setText(soPieceTitle + strSo);
            holder.productObj.setSoInventory(so);
        }
    }

    private void updateSBDAcheived(String grpName, boolean status) {
        Vector<ProductMasterBO> items = productList;
        if (items == null) {
            return;
        }
        int siz = items.size();
        if (siz == 0)
            return;

        for (int i = 0; i < siz; i++) {
            ProductMasterBO ret = items.elementAt(i);
            if (ret.getSbdGroupName().equals(grpName)) {
                if (status) {
                    ret.setSBDAcheivedLocal(true);
                } else {
                    if (ret.getOrderedPcsQty() == 0
                            && ret.getOrderedCaseQty() == 0
                            && ret.getOrderedOuterQty() == 0) {
                        ret.setSBDAcheivedLocal(false);
                    } else {
                        updateSBDAcheived(grpName, true);
                        break;
                    }
                }
            }
        }

    }

    private void updateOrderedCount() {
        int orderCount = 0;
        for (ProductMasterBO countBo : mylist) {
            if (countBo.getOrderedPcsQty() != 0 || countBo.getOrderedCaseQty() != 0
                    || countBo.getOrderedOuterQty() != 0) {
                orderCount = orderCount + 1;
            }
        }
        totalOrdCount = String.valueOf(orderCount);
    }


    public void updateValue() {
        try {
            new UpdateValueTask().execute();
        } catch (Exception e) {
            Commons.printException(e);
        }
    }

    class UpdateValueTask extends AsyncTask<Void, Void, Void> {
        int lpccount;
        Vector<ProductMasterBO> items;
        double temp;
        int sbdAchievement = 0;
        Vector<ProductMasterBO> orderedList;

        @Override
        protected Void doInBackground(Void... voids) {
            if (items == null) {
                return null;
            }
            int siz = items.size();
            if (siz == 0)
                return null;

            orderedList = new Vector<>();

            for (int i = 0; i < siz; i++) {
                ProductMasterBO ret = items.elementAt(i);
                if (ret.getOrderedPcsQty() != 0 || ret.getOrderedCaseQty() != 0
                        || ret.getOrderedOuterQty() != 0) {
                    lpccount = lpccount + 1;
                    temp = (ret.getOrderedPcsQty() * ret.getSrp())
                            + (ret.getOrderedCaseQty() * ret.getCsrp())
                            + ret.getOrderedOuterQty() * ret.getOsrp();
                    totalvalue = totalvalue + temp;


                    totalWeight = totalWeight + ((ret.getOrderedPcsQty() + (ret.getOrderedCaseQty() * ret.getCaseSize()) + (ret.getOrderedOuterQty() * ret.getOutersize())) * ret.getWeight());
                    int totalOrderedQty = orderHelper.getTotalOrderedQty(ret);
                    totalAllQty = (totalOrderedQty != -1) ? (totalAllQty + totalOrderedQty) : (totalAllQty + (ret.getOrderedPcsQty() + (ret.getOrderedCaseQty() * ret.getCaseSize()) + (ret.getOrderedOuterQty() * ret.getOutersize())));

                    orderedList.add(ret);
                }
            }

            sbdAchievement = SBDHelper.getInstance(StockAndOrder.this).getAchievedSBD(orderedList);

            return null;
        }

        @Override
        protected void onPreExecute() {
            totalAllQty = 0;
            totalWeight = 0;
            lpccount = 0;
            totalvalue = 0;
            items = productList;
        }

        @Override
        protected void onPostExecute(Void voids) {
            lpcText.setText(lpccount + "");
            String strFormatValue = bmodel.formatValue(totalvalue) + "";
            totalValueText.setText(strFormatValue);
            totalQtyTV.setText("" + totalAllQty);
            totalWeightTV.setText(Utils.formatAsTwoDecimal((double) (totalWeight)));
            distValue.setText(sbdAchievement + "/" + bmodel.getRetailerMasterBO()
                    .getSbdDistributionTarget());
        }
    }

    public void onClick(View v) {
        Button vw = (Button) v;
        bmodel = (BusinessModel) getApplicationContext();
        bmodel.setContext(this);

        if (vw == mBtnGuidedSelling_next) {
            boolean isAllDone = true;
            boolean isCurrentLogicDone = false;
            QUANTITY = null;
            for (int i = 0; i < bmodel.getmGuidedSelling().size(); i++) {
                GuidedSellingBO bo = bmodel.getmGuidedSelling().get(i);
                if (bo.isCurrent()) {
                    updateGuidedSellingStatus(bo);
                    if (!bo.isDone()) {
                        isAllDone = false;
                        showToastForGuidedSelling(bo);
                        break;
                    } else {
                        isCurrentLogicDone = true;
                    }
                    if (i == (bmodel.getmGuidedSelling().size() - 1)) {
                        //last level
                        break;
                    }
                } else {
                    if (isCurrentLogicDone) {
                        //Move to next level..
                        isAllDone = false;
                        setCurrentFlag(bo);
                        updateGuidedSellingView(false, false);
                        break;
                    }
                }
            }
            if (isAllDone) {
                onnext();
            }
        } else if (vw == mBtnGuidedSelling_prev) {
            updateGuidedSellingView(false, true);
        }
    }


    private boolean checkTaggingDetails(ProductMasterBO productMasterBO) {
        try {
            ArrayList<ProductTaggingBO> productTaggingList = bmodel.productHelper.getProductTaggingList();
            for (ProductTaggingBO productTagging : productTaggingList) {
                float totalQty = (productMasterBO.getOrderedCaseQty() * productMasterBO.getCaseSize())
                        + (productMasterBO.getOrderedPcsQty())
                        + (productMasterBO.getOrderedOuterQty() * productMasterBO.getOutersize());
                if (productMasterBO.getProductID().equals(productTagging.getPid()) &&
                        totalQty > 0
                        && totalQty > productTagging.getToNorm()) {
                    return false;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return true;
    }

    private void setTaggingDetails() {
        try {
            ArrayList<ProductTaggingBO> productTaggingList = bmodel.productHelper.getProductTaggingList();
            for (ProductTaggingBO productTagging : productTaggingList) {
                for (ProductMasterBO productMasterBO : productList) {
                    if (productMasterBO.getProductID().equals(productTagging.getPid())) {
                        productMasterBO.setAllocationQty(String.valueOf(productTagging.getToNorm()));
                        break;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showToastForGuidedSelling(GuidedSellingBO bo) {
        if (bo.getSubActivity().equals(mOrderCode)) {
            if (bo.getApplyLevel().equals("ALL")) {

                Toast.makeText(this, getResources().getString(R.string.all_products_should_be_ordered), Toast.LENGTH_LONG).show();
            } else {

                Toast.makeText(this, getResources().getString(R.string.atleast_one_product_should_be_ordered), Toast.LENGTH_LONG).show();
            }

        } else if (bo.getSubActivity().equals(mStockCode)) {

            if (bo.getApplyLevel().equals("ALL")) {

                Toast.makeText(this, getResources().getString(R.string.stock_should_be_checked_for_all_products), Toast.LENGTH_LONG).show();
            } else {

                Toast.makeText(this, getResources().getString(R.string.stock_should_be_checked_for_atleast_one_product), Toast.LENGTH_LONG).show();
            }

        }
    }

    private void updatesalesReturnValue() {
        double totalvalue = 0;
        Vector<ProductMasterBO> items = productList;
        if (items == null) {
            bmodel.showAlert(
                    getResources().getString(R.string.no_products_exists), 0);
            return;
        }
        int siz = items.size();
        if (siz == 0)
            return;

        for (int i = 0; i < siz; i++) {
            ProductMasterBO ret = items.elementAt(i);

            for (SalesReturnReasonBO bo : ret.getSalesReturnReasonList()) {
                double temp;
                if (bo.getPieceQty() != 0 || bo.getCaseQty() != 0
                        || bo.getOuterQty() > 0) {
                    temp = ((bo.getCaseQty() * bo.getCaseSize())
                            + (bo.getOuterQty() * bo.getOuterSize()) + bo
                            .getPieceQty()) * bo.getSrpedit();
                    totalvalue = totalvalue + temp;
                }

            }

        }
        SalesReturnHelper salesReturnHelper = SalesReturnHelper.getInstance(this);
        salesReturnHelper.setReturnValue(totalvalue);

    }

    private void onnext() {

        if (!"MENU_ORDER".equals(screenCode) && bmodel.configurationMasterHelper.IS_MUST_SELL_STK
                && !bmodel.productHelper.isMustSellFilledStockCheck(false, this)) {
            Toast.makeText(this, R.string.fill_must_sell, Toast.LENGTH_SHORT).show();
            return;
        }

        if (bmodel.getOrderHeaderBO() == null)
            bmodel.setOrderHeaderBO(new OrderHeader());

        bmodel.getRetailerMasterBO().setSbdDistStock(this.SbdDistPre);
        bmodel.getRetailerMasterBO().setSbdDistAchieved(
                this.sbdDistAchieved);

        // If Crown Management is Enable , then go for Crown Management
        // Screen and calcuale liability to crown Products also
        if ((bmodel.configurationMasterHelper.SHOW_CROWN_MANAGMENT || bmodel.configurationMasterHelper.SHOW_FREE_PRODUCT_GIVEN)
                && bmodel.configurationMasterHelper.IS_SIH_VALIDATION) {
            nextButtonClick();
        } else {
            // Other wise check if Bottle Return enable or not,according to
            // that it will work.
            if (bmodel.configurationMasterHelper.SHOW_PRODUCTRETURN) {
                new calculateReturnProductValusAndQty().execute();
            } else {
                nextButtonClick();
            }
        }
    }

    private void nextButtonClick() {
        try {

            if (bmodel.configurationMasterHelper.isRetailerBOMEnabled && SDUtil.convertToInt(bmodel.getRetailerMasterBO().getCredit_invoice_count()) <= 0) {
                bmodel.isDeadGoldenAchieved();
            }
            if (bmodel.hasOrder()) {
                //if this config IS_RFIELD1_ENABLED enabled below code will work
                //and

                if (bmodel.configurationMasterHelper.IS_ORD_SR_VALUE_VALIDATE &&
                        !bmodel.configurationMasterHelper.IS_INVOICE &&
                        bmodel.productHelper.getSalesReturnValue() >= totalvalue) {
                    Toast.makeText(this,
                            getResources().getString(R.string.order_value_cannot_be_lesser_than_the_sales_return_value),
                            Toast.LENGTH_LONG).show();
                    return;
                }

                if (bmodel.configurationMasterHelper.IS_MOQ_ENABLED) {
                    int size = bmodel.productHelper
                            .getProductMaster().size();
                    int count = 0;
                    for (int i = 0; i < size; ++i) {
                        ProductMasterBO product = bmodel.productHelper
                                .getProductMaster().get(i);

                        if (product.getOrderedPcsQty() > 0 && !TextUtils.isEmpty(product.getRField1())) {
                            //converting string Rfield1 value to integra
                            int res = SDUtil.convertToInt(product.getRField1());
                            if (product.getOrderedPcsQty() % res != 0)
                                count++;

                        }
                    }
                    if (count > 0) {
                        Intent intent = new Intent(StockAndOrder.this, MOQHighlightActivity.class);
                        ActivityOptionsCompat opts = ActivityOptionsCompat.makeCustomAnimation(this, R.anim.zoom_enter, R.anim.hold);
                        ActivityCompat.startActivityForResult(this, intent, MOQ_RESULT_CODE, opts.toBundle());
                        count = 0;
                        return;
                    }
                } else if (bmodel.configurationMasterHelper.IS_ENABLE_PRODUCT_TAGGING_VALIDATION) {
                    int size = bmodel.productHelper
                            .getProductMaster().size();
                    for (int i = 0; i < size; ++i) {
                        ProductMasterBO product = bmodel.productHelper
                                .getProductMaster().get(i);

                        if (product.getOrderedPcsQty() > 0 || product.getOrderedCaseQty() > 0 ||
                                product.getOrderedOuterQty() > 0) {
                            if (!checkTaggingDetails(product)) {
                                Toast.makeText(StockAndOrder.this, product.getProductName() + " exceeded Allocation", Toast.LENGTH_LONG).show();
                                return;
                            }
                        }
                    }

                }
                if (bmodel.getOrderHeaderBO() == null)
                    bmodel.setOrderHeaderBO(new OrderHeader());

                bmodel.getOrderHeaderBO().setPO(
                        tempPo == null ? "" : tempPo);
                bmodel.getOrderHeaderBO().setRemark(
                        tempRemark == null ? "" : tempRemark);
                bmodel.getOrderHeaderBO().setRField1(
                        tempRField1 == null ? "" : tempRField1);
                bmodel.getOrderHeaderBO().setRField2(
                        tempRField2 == null ? "" : tempRField2);
                bmodel.getOrderHeaderBO().setOrderImageName(
                        tempOrdImg == null ? "" : tempOrdImg);
                bmodel.getOrderHeaderBO().setAddressID(tempAddressId);

                if (bmodel.configurationMasterHelper.IS_MUST_SELL
                        && !bmodel.productHelper.isMustSellFilled()) {
                    if (dialog == null) {
                        dialog = new MustSellReasonDialog(
                                StockAndOrder.this, false,
                                diagDismissListen, bmodel);
                    }
                    dialog.show();
                    return;
                }
                if (bmodel.configurationMasterHelper.SHOW_INVOICE_CREDIT_BALANCE)
                    if (bmodel.getRetailerMasterBO().getCredit_balance() != -1
                            && totalvalue > bmodel.getRetailerMasterBO()
                            .getCredit_balance()) {

                        if (bmodel.configurationMasterHelper.IS_CREDIT_LIMIT_WITH_SOFT_ALERT) {
                            Toast.makeText(this, getResources().getString(R.string.order_exceeds_credit_balance), Toast.LENGTH_LONG).show();
                            nextBtnSubTask();
                        } else
                            bmodel.showAlert(getResources().getString(R.string.order_exceeds_credit_balance), 0);
                    } else
                        nextBtnSubTask();
                else
                    nextBtnSubTask();
            } else {
                if (hasStockOnly()) {
                    if (bmodel.configurationMasterHelper.IS_ORD_SR_VALUE_VALIDATE &&
                            !bmodel.configurationMasterHelper.IS_INVOICE &&
                            bmodel.productHelper.getSalesReturnValue() > totalvalue) {
                        Toast.makeText(this,
                                getResources().getString(R.string.order_value_cannot_be_lesser_than_the_sales_return_value),
                                Toast.LENGTH_LONG).show();
                        return;
                    }
                    showDialog(1);
                } else
                    bmodel.showAlert(
                            getResources().getString(
                                    R.string.no_items_added), 0);
            }


        } catch (Exception e) {
            Commons.printException(e + "");
        }
    }

    private final DialogInterface.OnCancelListener diagDismissListen = new OnCancelListener() {

        @Override
        public void onCancel(DialogInterface dialog) {
            nextBtnSubTask();
        }
    };

    private void nextBtnSubTask() {

        if (bmodel.configurationMasterHelper.IS_REMOVE_TAX_ON_SRP) {
            bmodel.productHelper.taxHelper.removeTaxFromPrice();
        }

        if (SchemeDetailsMasterHelper.getInstance(this).IS_UP_SELLING) {
            ArrayList<String> nearestSchemes = SchemeDetailsMasterHelper.getInstance(this).upSelling(bmodel.productHelper.getProductMaster());
            if (nearestSchemes.size() > 0) {
                Intent intent = new Intent(this, UpSellingActivity.class);
                intent.putStringArrayListExtra("nearestSchemes", nearestSchemes);
                startActivityForResult(intent, REQUEST_CODE_UPSELLING);
                return;
                //  finish();
            }
        }

        moveToNextScreen();

    }


    /**
     * Moving to next screen based on the config
     * NOTE: Please don't add any validations inside this method. This method should only contain intents
     */
    private void moveToNextScreen() {


        bmodel.outletTimeStampHelper.updateTimeStampModuleWise(DateTimeUtils.now(DateTimeUtils.TIME));

        SchemeDetailsMasterHelper schemeHelper = SchemeDetailsMasterHelper.getInstance(getApplicationContext());

        if (bmodel.configurationMasterHelper.SHOW_BATCH_ALLOCATION && bmodel.configurationMasterHelper.IS_SIH_VALIDATION) {
            if (bmodel.productHelper.isSIHAvailable()) {
                bmodel.configurationMasterHelper.setBatchAllocationtitle("Batch Allocation");
                bmodel.batchAllocationHelper.loadOrderedBatchList(productList);
                Intent intent = new Intent(StockAndOrder.this,
                        BatchAllocation.class);
                intent.putExtra("OrderFlag", "Nothing");
                intent.putExtra("ScreenCode", screenCode);
                startActivity(intent);
                overridePendingTransition(R.anim.trans_left_in, R.anim.trans_left_out);
                finish();
            } else {
                Toast.makeText(
                        StockAndOrder.this,
                        "Ordered value exceeds SIH value.Please edit the order",
                        Toast.LENGTH_SHORT).show();
            }

        } else if (schemeHelper.IS_SCHEME_ON
                && schemeHelper.IS_SCHEME_SHOW_SCREEN) {
            if (schemeHelper.IS_SCHEME_QPS_TRACKING) {
                Intent init = new Intent(StockAndOrder.this, QPSSchemeApply.class);
                init.putExtra("ScreenCode", screenCode);
                init.putExtra("ForScheme", screenCode);
                startActivity(init);
                overridePendingTransition(R.anim.trans_left_in, R.anim.trans_left_out);
                finish();
            } else {
                Intent init = new Intent(StockAndOrder.this, SchemeApply.class);
                init.putExtra("ScreenCode", screenCode);
                init.putExtra("ForScheme", screenCode);
                startActivity(init);
                overridePendingTransition(R.anim.trans_left_in, R.anim.trans_left_out);
                finish();
            }
        } else if (bmodel.configurationMasterHelper.SHOW_DISCOUNT_ACTIVITY) {
            Intent init = new Intent(StockAndOrder.this, OrderDiscount.class);
            init.putExtra("ScreenCode", screenCode);
            startActivity(init);
            overridePendingTransition(R.anim.trans_left_in, R.anim.trans_left_out);
            finish();
        } else if (bmodel.configurationMasterHelper.IS_INITIATIVE) {
            Intent init = new Intent(StockAndOrder.this,
                    InitiativeActivity.class);
            init.putExtra("ScreenCode", screenCode);
            startActivity(init);
            overridePendingTransition(R.anim.trans_left_in, R.anim.trans_left_out);
            finish();
        } else if (bmodel.configurationMasterHelper.IS_PRESENTATION_INORDER) {
            DigitalContentHelper mDigitalContentHelper = DigitalContentHelper.getInstance(this);
            mDigitalContentHelper.downloadDigitalContent(getApplicationContext(), "RETAILER");
            Intent i = new Intent(StockAndOrder.this,
                    DigitalContentActivity.class);
            i.putExtra("ScreenCode", screenCode);
            i.putExtra("FromInit", "Initiative");
            startActivity(i);
            overridePendingTransition(R.anim.trans_left_in, R.anim.trans_left_out);
            finish();
        } else {
            Intent i = new Intent(StockAndOrder.this, OrderSummary.class);
            i.putExtra("ScreenCode", screenCode);
            startActivity(i);
            overridePendingTransition(R.anim.trans_left_in, R.anim.trans_left_out);
            finish();
        }
    }

    private void backButtonClick() {
        try {
            if (bmodel.hasOrder() || hasStockOnly()) {
                showDialog(0);
            } else {
                bmodel.productHelper.clearOrderTable();


                if (!orderHelper.isQuickCall) {
                    bmodel.outletTimeStampHelper
                            .updateTimeStampModuleWise(DateTimeUtils.now(DateTimeUtils.TIME));
                    startActivity(new Intent(StockAndOrder.this,
                            HomeScreenTwo.class));
                }
                finish();

                overridePendingTransition(R.anim.trans_right_in, R.anim.trans_right_out);
            }
        } catch (Exception e) {
            Commons.printException(e);
        }
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        switch (id) {
            case 0:
                new CommonDialog(getApplicationContext(), this, "", getResources().getString(
                        R.string.doyouwantgoback), false, getResources().getString(R.string.ok), getResources().getString(R.string.cancel), new CommonDialog.PositiveClickListener() {
                    @Override
                    public void onPositiveButtonClick() {
                        if (bmodel.isEdit()) {
                            bmodel.productHelper
                                    .clearOrderTableAndUpdateSIH();
                        }
                        bmodel.productHelper.clearOrderTable();
                        orderHelper.setSerialNoListByProductId(null);

                        if (bmodel.configurationMasterHelper.SHOW_PRODUCTRETURN)
                            bmodel.productHelper
                                    .clearBomReturnProductsTable();
                        if (!orderHelper.isQuickCall) {
                            startActivity(new Intent(
                                    StockAndOrder.this,
                                    HomeScreenTwo.class));
                            overridePendingTransition(R.anim.trans_right_in, R.anim.trans_right_out);
                        }
                        finish();
                    }
                }, new CommonDialog.negativeOnClickListener() {
                    @Override
                    public void onNegativeButtonClick() {
                    }
                }).show();

                break;
            case 1:
                AlertDialog.Builder builder1 = new AlertDialog.Builder(StockAndOrder.this)
                        .setIcon(null)
                        .setCancelable(false)
                        .setTitle(
                                getResources().getString(
                                        R.string.do_you_want_to_save_stock))
                        .setPositiveButton(getResources().getString(R.string.ok),
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog,
                                                        int whichButton) {

                                        new SaveStock().execute();

                                    }
                                })
                        .setNegativeButton(
                                getResources().getString(R.string.cancel),
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog,
                                                        int whichButton) {
                                    }
                                });
                bmodel.applyAlertDialogTheme(builder1);
                break;
            default:
                break;
        }
        return null;
    }

    class SaveStock extends AsyncTask<String, Integer, Boolean> {

        @Override
        protected Boolean doInBackground(String... arg0) {
            try {

                if (bmodel.isOrderTaken() && bmodel.isEdit())
                    orderHelper.deleteOrder(getApplicationContext(), bmodel.getRetailerMasterBO().getRetailerID());
                StockCheckHelper stockCheckHelper = StockCheckHelper.getInstance(StockAndOrder.this);
                if (bmodel.configurationMasterHelper.IS_COMBINED_STOCK_CHECK_FROM_ORDER) {
                    // save price check
                    PriceTrackingHelper priceTrackingHelper = PriceTrackingHelper.getInstance(StockAndOrder.this);
                    if (bmodel.configurationMasterHelper.SHOW_PRICECHECK_IN_STOCKCHECK)
                        priceTrackingHelper.savePriceTransaction(getApplicationContext(), mylist);

                    // save near expiry
                    stockCheckHelper.saveNearExpiry(getApplicationContext());
                }

                // Save closing stock
                stockCheckHelper.saveClosingStock(getApplicationContext(), true);

                bmodel.saveModuleCompletion(OrderedFlag, true);

                return Boolean.TRUE;
            } catch (Exception e) {
                Commons.printException(e + "");
                return Boolean.FALSE;
            }
        }

        protected void onPreExecute() {
            showProgressDialog(getResources().getString(R.string.saving));
        }

        protected void onPostExecute(Boolean result) {
            // result is the value returned from doInBackground
            dismissProgressDialog();
            if (result == Boolean.TRUE) {
                Toast.makeText(
                        StockAndOrder.this,
                        getResources().getString(
                                R.string.stock_saved),
                        Toast.LENGTH_SHORT).show();
                if (!orderHelper.isQuickCall) {
                    startActivity(new Intent(
                            StockAndOrder.this,
                            HomeScreenTwo.class));
                    overridePendingTransition(R.anim.trans_right_in, R.anim.trans_right_out);
                }
                finish();

            }
        }
    }

    private void eff() {
        String s = QUANTITY.getText().toString();
        if (!"0".equals(s) && !"0.0".equals(s)) {
            String strQuantity = QUANTITY.getText() + append;
            QUANTITY.setText(strQuantity);
        } else
            QUANTITY.setText(append);
    }

    public void numberPressed(View vw) {

        int val;
        if (QUANTITY == null) {
            bmodel.showAlert(
                    getResources().getString(R.string.please_select_item), 0);
        } else {
            int id = vw.getId();
            if (id == R.id.calcdel) {

                int s = SDUtil.convertToInt(QUANTITY.getText()
                        .toString());
                s = s / 10;
                String strS = s + "";
                QUANTITY.setText(strS);
                val = s;


            } else if (id == R.id.calcdot) {
                val = SDUtil.convertToInt(append);
                if (QUANTITY.getTag() != null) {
                    if (QUANTITY.getId() == R.id.stock_and_order_listview_srpedit) {
                        Button ed = (Button) findViewById(vw.getId());
                        append = ed.getText().toString();
                        eff();
                        val = SDUtil.convertToInt(append);
                    }

                }
            } else {
                Button ed = (Button) findViewById(vw.getId());
                append = ed.getText().toString();
                eff();
                val = SDUtil.convertToInt(append);
            }

            ProductMasterBO temp = (ProductMasterBO) QUANTITY.getTag();

            if (val > 0
                    && temp.isRPS()
                    && !temp.isSBDAcheivedLocal()
                    && (temp.getOrderedPcsQty() > 0
                    || temp.getOrderedCaseQty() > 0 || temp
                    .getOrderedOuterQty() > 0)) {
                updateSBDAcheived(temp.getSbdGroupName(), true);
            } else if (val == 0
                    && temp.isRPS()
                    && temp.isSBDAcheivedLocal()
                    && (temp.getOrderedPcsQty()
                    + (temp.getOrderedCaseQty() * temp.getCaseSize()) + (temp
                    .getOrderedOuterQty() * temp.getOutersize())) == 0) {
                updateSBDAcheived(temp.getSbdGroupName(), false);
            }
            updateValue();
        }

    }

    private void loadSBDAchievementLocal() {
        for (ProductMasterBO temp : productList) {
            int val = temp.getOrderedPcsQty()
                    + (temp.getOrderedCaseQty() * temp.getCaseSize())
                    + (temp.getOrderedOuterQty() * temp.getOutersize());
            if (val > 0 && temp.isRPS() && !temp.isSBDAcheivedLocal()) {
                updateSBDAcheived(temp.getSbdGroupName(), true);
            } else if (val == 0 && temp.isRPS() && temp.isSBDAcheivedLocal()) {
                updateSBDAcheived(temp.getSbdGroupName(), false);
            }
        }
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);

        if (requestCode == SALES_RETURN) {
            if (resultCode == RESULT_OK) {
                overridePendingTransition(0, R.anim.zoom_exit);
                updateValue();

                lvwplist.setAdapter(new MyAdapter(mylist));

                Bundle extras = data.getExtras();
                int holderPosition = extras.getInt("position", 0);
                int holderTop = extras.getInt("top", 0);
                if (mylist.size() > 0)
                    lvwplist.setSelectionFromTop(holderPosition, holderTop);
            }
        } else if (requestCode == REQUEST_CODE_UPSELLING) {
            if (resultCode == 1) {
                moveToNextScreen();
            } else if (resultCode == 2) {
                try {

                    if (data != null) {
                        String slabId = data.getStringExtra("slabId");
                        SchemeDetailsMasterHelper schemeHelper = SchemeDetailsMasterHelper.getInstance(this);
                        SchemeBO slabBO = schemeHelper.getSchemeById().get(slabId);
                        if (slabBO != null) {
                            mylist = new Vector<>();
                            for (SchemeProductBO schemeProductBO : slabBO.getBuyingProducts()) {

                                if (bmodel.productHelper.getProductMasterBOById(schemeProductBO.getProductId()) != null) {
                                    if (bmodel.configurationMasterHelper.IS_GLOBAL_CATEGORY && !orderHelper.isQuickCall &&
                                            !bmodel.productHelper.getProductMasterBOById(schemeProductBO.getProductId()).getParentHierarchy().contains("/" + bmodel.productHelper.getmSelectedGlobalProductId() + "/"))
                                        continue;
                                    mylist.add(bmodel.productHelper.getProductMasterBOById(schemeProductBO.getProductId()));
                                } else {
                                    for (ProductMasterBO productMasterBO : productList) {
                                        if (bmodel.configurationMasterHelper.IS_GLOBAL_CATEGORY && !orderHelper.isQuickCall &&
                                                !productMasterBO.getParentHierarchy().contains("/" + bmodel.productHelper.getmSelectedGlobalProductId() + "/"))
                                            continue;
                                        if (productMasterBO.getParentHierarchy().contains("/" + schemeProductBO.getProductId() + "/"))
                                            mylist.add(productMasterBO);
                                    }
                                }
                            }
                            lvwplist.setAdapter(new MyAdapter(mylist));
                        }

                    }
                } catch (Exception ex) {
                    Commons.printException(ex);
                }
            }

        } else if (requestCode == MOQ_RESULT_CODE) {
            overridePendingTransition(0, R.anim.zoom_exit);
            if (resultCode == 1) {
                lvwplist.invalidateViews();
            }
        }
        else {
            if (result != null) {
                if (result.getContents() != null) {
                    strBarCodeSearch = result.getContents();
                    if (strBarCodeSearch != null && !"".equals(strBarCodeSearch)) {

                        productSearch.startSearch(productList,strBarCodeSearch);

                    }
                }
            } else {
                super.onActivityResult(requestCode, resultCode, data);
            }
        }
    }

    @Override
    public void updateCancel() {
        mDrawerLayout.closeDrawers();
    }


    @Override
    public void updateGeneralText(String mFilterText) {


       mSelectedFilter=1;
       mSelectedSpecialFilter=mFilterText;

        // This should moved ...
        if (bmodel.configurationMasterHelper.IS_TOP_ORDER_FILTER) {
            prepareTopOrderFilter();
        } else {
            rvFilterList.setVisibility(View.GONE);
        }

        Vector<ProductMasterBO> items = productList;
        if (items == null) {
            bmodel.showAlert(
                    getResources().getString(R.string.no_products_exists),
                    0);
            return;
        }

// clearing five filter List
        if (mSelectedIdByLevelId != null)
            mSelectedIdByLevelId.clear();



        // Clear the productName
        productSearch.setProductNameOnBar("");

        mDrawerLayout.closeDrawers();


    }

    @Override
    public void updateBrandText(String mFilterText, int bid) {
    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.order_menu, menu);

        return super.onCreateOptionsMenu(menu);
    }

    /**
     * Called whenever we call invalidateOptionsMenu()
     */
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {

        // Change color if Filter is selected
        if (productSearch.isSpecialFilter())
            menu.findItem(R.id.menu_spl_filter).setIcon(
                    R.drawable.ic_action_star_select);

        // If the nav drawer is open, hide action items related to the content
        // view
        boolean drawerOpen = mDrawerLayout.isDrawerOpen(GravityCompat.END);

        menu.findItem(R.id.menu_fivefilter).setVisible(!drawerOpen);
        menu.findItem(R.id.menu_next).setVisible(!drawerOpen);
        menu.findItem(R.id.menu_spl_filter).setVisible(!drawerOpen);
        menu.findItem(R.id.menu_remarks).setVisible(!drawerOpen);
        menu.findItem(R.id.menu_loc_filter).setVisible(!drawerOpen);
        menu.findItem(R.id.menu_barcode).setVisible(!drawerOpen);
        menu.findItem(R.id.menu_refresh).setVisible(!drawerOpen);
        menu.findItem(R.id.menu_scheme).setVisible(!drawerOpen);

        if (bmodel.configurationMasterHelper.SHOW_ORD_CALC)
            menu.findItem(R.id.menu_calculator).setVisible(true);

        if (bmodel.configurationMasterHelper.SHOW_SPL_FILTER && isFilter && !bmodel.configurationMasterHelper.IS_SPL_FILTER_TAB && !bmodel.configurationMasterHelper.SHOW_SPL_FLIER_NOT_NEEDED) {
            menu.findItem(R.id.menu_spl_filter).setVisible(true);
        } else {
            menu.findItem(R.id.menu_spl_filter).setVisible(false);
        }

        if (!bmodel.configurationMasterHelper.SHOW_REMARKS_STK_ORD)
            menu.findItem(R.id.menu_remarks).setVisible(false);

        if (bmodel.configurationMasterHelper.IS_GLOBAL_LOCATION)
            menu.findItem(R.id.menu_loc_filter).setVisible(false);
        else {
            if (bmodel.productHelper.getInStoreLocation().size() < 2)
                menu.findItem(R.id.menu_loc_filter).setVisible(false);
        }
        if (screenCode != null && screenCode.equals(ConfigurationMasterHelper.MENU_ORDER))
            menu.findItem(R.id.menu_loc_filter).setVisible(false);

        menu.findItem(R.id.menu_survey).setVisible(bmodel.configurationMasterHelper.floating_Survey);
        menu.findItem(R.id.menu_reason).setVisible(bmodel.configurationMasterHelper.floating_np_reason_photo);
        menu.findItem(R.id.menu_barcode).setVisible(bmodel.configurationMasterHelper.IS_BAR_CODE);
        SchemeDetailsMasterHelper schemeHelper = SchemeDetailsMasterHelper.getInstance(getApplicationContext());
        menu.findItem(R.id.menu_scheme).setVisible(schemeHelper.IS_SHOW_ALL_SCHEMES_ORDER);

        menu.findItem(R.id.menu_fivefilter).setVisible(false);

        if (bmodel.productHelper.isFilterAvaiable("MENU_STK_ORD")) {
            if (isFilter) {
                menu.findItem(R.id.menu_fivefilter).setVisible(true);
            }
            if (bmodel.configurationMasterHelper.IS_TOP_ORDER_FILTER && sequence.size() == 1) {
                menu.findItem(R.id.menu_fivefilter).setVisible(false);
            }
        }


        if (mSelectedIdByLevelId != null) {
            for (Integer id : mSelectedIdByLevelId.keySet()) {
                if (mSelectedIdByLevelId.get(id) > 0) {
                    menu.findItem(R.id.menu_fivefilter).setIcon(
                            R.drawable.ic_action_filter_select);
                    break;
                }
            }
        }
        menu.findItem(R.id.menu_next).setVisible(false);

        if (!bmodel.configurationMasterHelper.IS_DOWNLOAD_WAREHOUSE_STOCK)
            menu.findItem(R.id.menu_refresh).setVisible(false);

        menu.findItem(R.id.menu_digtal_content).setVisible(DigitalContentHelper.getInstance(this).SHOW_FLT_DGT_CONTENT);


        if (drawerOpen)
            menu.clear();


        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int i = item.getItemId();
        if (i == android.R.id.home) {

            if (mDrawerLayout.isDrawerOpen(GravityCompat.END))
                mDrawerLayout.closeDrawers();
            else
                backButtonClick();
            return true;
        } else if (i == R.id.menu_survey) {
            /*bmodel.productHelper
                    .downloadFiveLevelFilterNonProducts("MENU_SURVEY");*/
            bmodel.productHelper.setFilterProductLevelsRex(bmodel.productHelper.downloadFilterLevel("MENU_SURVEY"));
            bmodel.productHelper.setFilterProductsByLevelIdRex(bmodel.productHelper.downloadFilterLevelProducts(
                    bmodel.productHelper.getRetailerModuleSequenceValues(), false));
            bmodel.mSelectedActivityName = "Survey";
            startActivity(new Intent(this, SurveyActivityNew.class));
            return true;
        } else if (i == R.id.menu_next) {
            if (bmodel.configurationMasterHelper.IS_SHOW_ORDERING_SEQUENCE)
                bmodel.productHelper.setmProductidOrderByEntry(mProductList);

            if (bmodel.getOrderHeaderBO() == null)
                bmodel.setOrderHeaderBO(new OrderHeader());

            bmodel.getRetailerMasterBO().setSbdDistStock(this.SbdDistPre);
            bmodel.getRetailerMasterBO().setSbdDistAchieved(
                    this.sbdDistAchieved);

            // If Crown Management is Enable , then go for Crown Management
            // Screen and calcuale liability to crown Products also
            if ((bmodel.configurationMasterHelper.SHOW_CROWN_MANAGMENT || bmodel.configurationMasterHelper.SHOW_FREE_PRODUCT_GIVEN)
                    && bmodel.configurationMasterHelper.IS_SIH_VALIDATION) {
                nextButtonClick();
            } else {
                // Other wise check if Bottle Return enable or not,according to
                // that it will work.
                if (bmodel.configurationMasterHelper.SHOW_PRODUCTRETURN) {
                    new calculateReturnProductValusAndQty().execute();
                } else {
                    nextButtonClick();
                }
            }
            return true;
        } else if (i == R.id.menu_spl_filter) {

            generalFilterClickedFragment();
            item.setVisible(false);
            supportInvalidateOptionsMenu();
            return true;
        } else if (i == R.id.menu_loc_filter) {
            showLocation();
            return true;
        } else if (i == R.id.menu_remarks) {

            FragmentTransaction ft = this
                    .getSupportFragmentManager().beginTransaction();

            RemarksDialog dialog = new RemarksDialog("MENU_STK_ORD");
            dialog.setCancelable(false);
            dialog.show(ft, "stk_and_ord");
            return true;
        } else if (i == R.id.menu_reason) {
            bmodel.reasonHelper.downloadNpReason(bmodel.retailerMasterBO.getRetailerID(), OrderedFlag);
            ReasonPhotoDialog dialog = new ReasonPhotoDialog();
            dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialog) {
                    if (bmodel.reasonHelper.isNpReasonPhotoAvaiable(bmodel.retailerMasterBO.getRetailerID(), OrderedFlag)) {
                        if (!orderHelper.isQuickCall) {
                            bmodel.saveModuleCompletion(OrderedFlag, true);
                            bmodel.outletTimeStampHelper
                                    .updateTimeStampModuleWise(DateTimeUtils.now(DateTimeUtils.TIME));
                            startActivity(new Intent(StockAndOrder.this,
                                    HomeScreenTwo.class));
                        }
                        finish();
                    }
                }
            });
            Bundle args = new Bundle();
            args.putString("modulename", OrderedFlag);
            dialog.setCancelable(false);
            dialog.setArguments(args);
            dialog.show(getSupportFragmentManager(), "ReasonDialogFragment");
            return true;
        } else if (i == R.id.menu_fivefilter) {

            if (bmodel.configurationMasterHelper.IS_UNLINK_FILTERS) {
                mSelectedFilterMap.put("General", GENERAL);
            }
            FiveFilterFragment();
            return true;
        } else if (i == R.id.menu_barcode) {
            checkAndRequestPermissionAtRunTime(2);
            int permissionStatus = ContextCompat.checkSelfPermission(getApplicationContext(),
                    Manifest.permission.CAMERA);
            if (permissionStatus == PackageManager.PERMISSION_GRANTED) {
                new IntentIntegrator(this).setBeepEnabled(false).initiateScan();
            } else {
                Toast.makeText(this,
                        getResources().getString(R.string.permission_enable_msg)
                                + " " + getResources().getString(R.string.permission_camera)
                        , Toast.LENGTH_LONG).show();
            }
            return true;
        } else if (i == R.id.menu_calculator) {
            try {
                ArrayList<HashMap<String, Object>> items = new ArrayList<>();
                final PackageManager pm = getPackageManager();
                List<PackageInfo> packs = pm.getInstalledPackages(0);
                for (PackageInfo pi : packs) {
                    if (pi.packageName.toString().toLowerCase().contains("calcul")) {
                        HashMap<String, Object> map = new HashMap<>();
                        map.put("appName", pi.applicationInfo.loadLabel(pm));
                        map.put("packageName", pi.packageName);
                        items.add(map);
                    }
                }
                if (!items.isEmpty()) {
                    String packageName = (String) items.get(0).get(
                            "packageName");
                    Intent i1 = pm.getLaunchIntentForPackage(packageName);
                    if (i1 != null)
                        startActivity(i1);
                } else {
                    Toast.makeText(this, "Calculator application not found.",
                            Toast.LENGTH_SHORT).show();
                }
            } catch (Exception e) {
                Commons.printException("" + e);
            }
            return true;
        } else if (i == R.id.menu_refresh) {
            if (bmodel.isOnline()) {
                new DownloadNewStock().execute();
            } else {
                bmodel.showAlert(
                        getResources()
                                .getString(R.string.no_network_connection), 0);
            }
        } else if (i == R.id.menu_scheme) {
            SchemeDetailsMasterHelper schemeHelper = SchemeDetailsMasterHelper.getInstance(getApplicationContext());

            if (schemeHelper
                    .getSchemeList() == null
                    || schemeHelper
                    .getSchemeList().size() == 0) {
                Toast.makeText(StockAndOrder.this,
                        R.string.scheme_not_available,
                        Toast.LENGTH_SHORT).show();
            } else {
                Intent intent = new Intent(StockAndOrder.this, SchemeDetailsActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);
            }

        } else if (i == R.id.menu_digtal_content) {
            DigitalContentHelper.getInstance(this).downloadDigitalContent(getApplicationContext(), "RETAILER");
            Intent i1 = new Intent(StockAndOrder.this,
                    DigitalContentActivity.class);
            i1.putExtra("ScreenCode", screenCode);
            i1.putExtra("FromInit", "FloatDigi");
            startActivity(i1);
        }
        return super.onOptionsItemSelected(item);
    }


    private void showLocation() {
        AlertDialog.Builder builder;

        builder = new AlertDialog.Builder(this);
        builder.setTitle(null);
        builder.setSingleChoiceItems(mLocationAdapter, mSelectedLocationIndex,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int item) {
                        mSelectedLocationIndex = item;
                        bmodel.productHelper.setmSelectedLocationIndex(item);
                        dialog.dismiss();
                    }
                });

        bmodel.applyAlertDialogTheme(builder);
    }

    private void generalFilterClickedFragment() {
        try {
            mDrawerLayout.openDrawer(GravityCompat.END);

            android.support.v4.app.FragmentManager fm = getSupportFragmentManager();
            SpecialFilterFragment frag = (SpecialFilterFragment) fm
                    .findFragmentByTag("generalfilter");
            android.support.v4.app.FragmentTransaction ft = fm
                    .beginTransaction();
            if (frag != null)
                ft.detach(frag);
            Bundle bundle = new Bundle();

            bundle.putString("filterName", GENERAL);
            bundle.putSerializable("serilizeContent",
                    bmodel.configurationMasterHelper.getGenFilter());

            // set Fragmentclass Arguments
            SpecialFilterFragment fragobj = new SpecialFilterFragment(mSelectedFilterMap);
            fragobj.setArguments(bundle);
            ft.replace(R.id.right_drawer, fragobj, "generalfilter");
            ft.commit();

        } catch (Exception e) {
            Commons.printException(e);
        }
    }

    private void FiveFilterFragment() {
        try {
            QUANTITY = null;

            mDrawerLayout.openDrawer(GravityCompat.END);
            android.support.v4.app.FragmentManager fm = getSupportFragmentManager();
            FilterFiveFragment<?> frag = (FilterFiveFragment<?>) fm
                    .findFragmentByTag("Fivefilter");
            android.support.v4.app.FragmentTransaction ft = fm
                    .beginTransaction();
            if (frag != null)
                ft.detach(frag);
            Bundle bundle = new Bundle();
            bundle.putSerializable("serilizeContent",
                    bmodel.configurationMasterHelper.getGenFilter());
            bundle.putString("isFrom", "STK");
            bundle.putBoolean("isAttributeFilter", true);
            bundle.putSerializable("selectedFilter", mSelectedIdByLevelId);

            // set Fragmentclass Arguments
            FilterFiveFragment<Object> fragobj = new FilterFiveFragment<>();
            fragobj.setArguments(bundle);

            ft.replace(R.id.right_drawer, fragobj, "Fivefilter");
            ft.commit();
        } catch (Exception e) {
            Commons.printException(e);
        }
    }

    @Override
    protected void onDestroy() {
        dismissProgressDialog();
        super.onDestroy();
        unbindDrawables(findViewById(R.id.root));

        if (bmodel.configurationMasterHelper.IS_DOWNLOAD_WAREHOUSE_STOCK) {
            unregisterReceiver(mWareHouseStockReceiver);
        }
    }

    /**
     * this would clear all the resources used of the layout.
     *
     * @param view
     */
    private void unbindDrawables(View view) {
        if (view != null) {
            if (view.getBackground() != null) {
                view.getBackground().setCallback(null);
            }
            if (view instanceof ViewGroup) {
                for (int i = 0; i < ((ViewGroup) view).getChildCount(); i++) {
                    unbindDrawables(((ViewGroup) view).getChildAt(i));
                }
                try {
                    if (!(view instanceof AdapterView<?>))
                        ((ViewGroup) view).removeAllViews();
                } catch (Exception e) {
                    Commons.printException(e);
                }
            }
        }
    }



    /**
     * while click button in motorola ET1 device,this onNewIntent method called
     */
    @Override
    protected void onNewIntent(Intent i) {

        super.onNewIntent(i);
        try {
            Toast.makeText(getBaseContext(), " On New Intent called ",
                    Toast.LENGTH_SHORT).show();
            if (i != null)
                handleDecodeData(i);
        } catch (Exception e) {
            Commons.printException(e);
        }
    }

    private void handleDecodeData(Intent i) {
        String data;
        // check the intent action is for us
        if (i.getAction().contentEquals(ourIntentAction)) {
            // get the data from the intent
            data = i.getStringExtra(DATA_STRING_TAG);
            strBarCodeSearch = data;
            Toast.makeText(getBaseContext(),
                    " scanned barcode value :" + strBarCodeSearch,
                    Toast.LENGTH_SHORT).show();

            productSearch.startSearch(productList,strBarCodeSearch);
            strBarCodeSearch = "ALL";
        }
    }

    private void switchProfile() {
        final String switchToProfile = "com.motorolasolutions.emdk.datawedge.api.ACTION_SWITCHTOPROFILE";
        final String extraData = "com.motorolasolutions.emdk.datawedge.api.EXTRA_PROFILENAME";

        Intent i = new Intent();
        i.setAction(switchToProfile);
        i.putExtra(extraData, "dist_stok");
        this.sendBroadcast(i);
    }


    private boolean hasStockOnly() {
        int siz = productList.size();
        if (siz == 0)
            return false;
        for (int i = 0; i < siz; ++i) {
            ProductMasterBO product = productList.get(i);
            int siz1 = product.getLocations().size();
            for (int j = 0; j < siz1; j++) {
                if (product.getLocations().get(j).getShelfPiece() > -1
                        || product.getLocations().get(j).getShelfCase() > -1
                        || product.getLocations().get(j).getShelfOuter() > -1
                        || product.getLocations().get(j).getWHPiece() > 0
                        || product.getLocations().get(j).getWHCase() > 0
                        || product.getLocations().get(j).getWHOuter() > 0
                        || product.getLocations().get(j).getAvailability() > -1)
                    return true;
            }
        }
        return false;
    }

    /**
     * Save the values in Aysnc task through Background
     *
     * @author gnanaprakasam.d
     */
    class calculateReturnProductValusAndQty extends
            AsyncTask<String, Integer, Boolean> {

        @Override
        protected Boolean doInBackground(String... arg0) {
            try {
                bmodel.productHelper.setReturnQty();
                bmodel.productHelper.calculateOrderReturnValue();

                if (bmodel.configurationMasterHelper.SHOW_GROUPPRODUCTRETURN) {
                    bmodel.productHelper.setGroupWiseReturnQty();
                    bmodel.productHelper.calculateOrderReturnTypeWiseValue();
                }
                return Boolean.TRUE;
            } catch (Exception e) {
                Commons.printException(e);
                return Boolean.FALSE;
            }
        }

        protected void onPreExecute() {
            showProgressDialog(getResources().getString(R.string.loading));
        }

        protected void onPostExecute(Boolean result) {
            // result is the value returned from doInBackground
            dismissProgressDialog();
            nextButtonClick();
        }
    }


    private String getProductTotalValue(ViewHolder holder) {
        ProductMasterBO product = holder.productObj;
        int totalQty = 0;
        String totSTKQty = getString(R.string.stock) + ": ";

        if (bmodel.labelsMasterHelper.applyLabels(holder.text_stock.getTag()) != null)
            totSTKQty = bmodel.labelsMasterHelper
                    .applyLabels(holder.text_stock.getTag()) + ": ";

        Vector<StandardListBO> locationList = bmodel.productHelper
                .getInStoreLocation();

        int size = locationList.size();
        for (int i = 0; i < size; i++) {

            //Defailt value is -1 for stock fields, so adding only if value>0
            if (product.getLocations().get(i).getShelfPiece() > -1)
                totalQty += product.getLocations().get(i).getShelfPiece();

            if (product.getLocations().get(i).getShelfCase() > -1)
                totalQty += (product.getLocations().get(i).getShelfCase() * product
                        .getCaseSize());

            if (product.getLocations().get(i).getShelfOuter() > -1)
                totalQty += (product.getLocations().get(i).getShelfOuter() * product
                        .getOutersize());

//            if (product.getLocations().get(i).getAvailability() > -1)
//                totalQty += product.getLocations().get(i).getAvailability(); //Along with stock quantity this also gets added and showing wrong count

            totalQty += product.getLocations().get(i).getWHPiece();
            totalQty += (product.getLocations().get(i).getWHCase() * product
                    .getCaseSize());
            totalQty += (product.getLocations().get(i).getWHOuter() * product
                    .getOutersize());
        }
        holder.productObj.setTotalStockQty(totalQty);
        return (totSTKQty + String.valueOf(totalQty));
    }

    @Override
    public void updateFromFiveLevelFilter(int mProductId, HashMap<Integer, Integer> mSelectedIdByLevelId, ArrayList<Integer> mAttributeProducts, String mFilterText) {

        mSelectedFilter=2;
        mSelectedProductId=mProductId;
        mSelectedAttributeProductIds=mAttributeProducts;

        this.mSelectedIdByLevelId = mSelectedIdByLevelId;

        if (bmodel.configurationMasterHelper.IS_TOP_ORDER_FILTER) {
            filterAdapter.notifyDataSetChanged();
        }

        mDrawerLayout.closeDrawers();

    }


    private void updateData(ProductMasterBO productBO) {
        int qty = productBO.getOrderedPcsQty() + (productBO.getOrderedCaseQty() * productBO.getCaseSize()) + (productBO.getOrderedOuterQty() * productBO.getOutersize());

        if (qty == 0) {
            bmodel.productHelper.getmProductidOrderByEntry().remove((String) productBO.getProductID());
            bmodel.productHelper.getmProductidOrderByEntryMap().remove(SDUtil.convertToInt(productBO.getProductID()));
        } else {
            int lastQty = 0;
            if (bmodel.productHelper.getmProductidOrderByEntryMap().get(SDUtil.convertToInt(productBO.getProductID())) != null)
                lastQty = bmodel.productHelper.getmProductidOrderByEntryMap().get(SDUtil.convertToInt(productBO.getProductID()));
            if (lastQty == qty) {
                // Dont do any thing
            } else {
                if (bmodel.productHelper.getmProductidOrderByEntry().contains(productBO.getProductID())) {
                    bmodel.productHelper.getmProductidOrderByEntry().remove((String) productBO.getProductID());
                    bmodel.productHelper.getmProductidOrderByEntry().add(productBO.getProductID());
                    bmodel.productHelper.getmProductidOrderByEntryMap().put(SDUtil.convertToInt(productBO.getProductID()), qty);
                } else {
                    bmodel.productHelper.getmProductidOrderByEntry().add(productBO.getProductID());
                    bmodel.productHelper.getmProductidOrderByEntryMap().put(SDUtil.convertToInt(productBO.getProductID()), qty);
                }
            }
        }


    }

    private void loadSpecialFilterView() {
        findViewById(R.id.hscrl_spl_filter).setVisibility(View.VISIBLE);
        ll_spl_filter =  findViewById(R.id.ll_spl_filter);
        ll_tab_selection =  findViewById(R.id.ll_tab_selection);
        float scale;
        int width;


        if (!bmodel.configurationMasterHelper.getGenFilter().get(0).getConfigCode().equals("ALL")) {
            bmodel.configurationMasterHelper.getGenFilter().add(0, new ConfigureBO("ALL", "All", "0", 0, 1, 1));
        }

        scale = getResources().getDisplayMetrics().widthPixels;
        width = (int) (scale / bmodel.configurationMasterHelper.getGenFilter().size());

        float den = getResources().getDisplayMetrics().density;
        float dimen_wd = getResources().getDimension(R.dimen.special_filter_item_width);
        if (width < (int) (dimen_wd * den + 25.5f)) {
            scale = den;
            width = (int) (dimen_wd * scale + 25.5f);
        }
        final TabLayout tabLay = (TabLayout) findViewById(R.id.dummy_tab_lay);
        final TabLayout.Tab tab1 = tabLay.newTab();
        tab1.setText("ABCD");
        tabLay.addTab(tab1);

        tabLay.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {

            @Override
            public void onGlobalLayout() {

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    tabLay.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                } else {
                    tabLay.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                }
                LinearLayout.LayoutParams obj = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                        (int) (tabLay.getHeight() - getResources().getDimension(R.dimen.tab_indicator_height)));
                ll_spl_filter.setLayoutParams(obj);
                tabLay.setVisibility(View.GONE);

            }

        });

        for (int i = 0; i < bmodel.configurationMasterHelper.getGenFilter().size(); i++) {
//        for (int i = 0; i < 2; i++) {
            ConfigureBO config = bmodel.configurationMasterHelper.getGenFilter().get(i);
          /*  ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(
                    (getResources().getDimensionPixelSize(getResources().getDimension(R.dimen.special_filter_item_width)*scale+0.5f)), ViewGroup.LayoutParams.WRAP_CONTENT);*/

            TypedArray typearr = getTheme().obtainStyledAttributes(R.styleable.MyTextView);
            final int color = typearr.getColor(R.styleable.MyTextView_textColor, 0);
            final int indicator_color = typearr.getColor(R.styleable.MyTextView_accentcolor, 0);
            Button tab;
            tab = new Button(this);
            tab.setText(config.getMenuName());
            tab.setTag(config.getConfigCode());
            tab.setGravity(Gravity.CENTER);
            tab.setTypeface(FontUtils.getFontRoboto(StockAndOrder.this, FontUtils.FontType.MEDIUM));
            tab.setTextColor(color);
            tab.setMaxLines(1);
            tab.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.font_small));
            tab.setBackgroundColor(ContextCompat.getColor(this, android.R.color.transparent));
            tab.setWidth(width);
            tab.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View view) {


                    if (view.getTag().toString().equalsIgnoreCase("ALL")) {
                        productSearch.startSpecialFilterSearch(productList,GENERAL);
                    } else {
                        productSearch.startSpecialFilterSearch(productList,view.getTag().toString());
                    }
                    if (bmodel.configurationMasterHelper.IS_SPL_FILTER_TAB)
                        selectTab(view.getTag());
                }
            });


            ll_spl_filter.addView(tab);

            Button tv_selection_identifier = new Button(this);
            tv_selection_identifier.setTag(config.getConfigCode() + config.getMenuName());
            tv_selection_identifier.setWidth(width);
            tv_selection_identifier.setBackgroundColor(indicator_color);


            ll_tab_selection.addView(tv_selection_identifier);


        }
    }

    private void selectTab(Object tag) {
        for (ConfigureBO config : bmodel.configurationMasterHelper.getGenFilter()) {
            View view = findViewById(R.id.root).findViewWithTag(config.getConfigCode());
            View view1 = findViewById(R.id.root).findViewWithTag(config.getConfigCode() + config.getMenuName());
            if (((String) tag).equalsIgnoreCase(config.getConfigCode())) {
                if (view instanceof TextView) {
                    ((TextView) view).setText(config.getMenuName() + "(" + mylist.size() + ")");
                }
                if (view1 instanceof Button) {
                    view1.setVisibility(View.VISIBLE);
                }


            } else {
                if (view instanceof TextView) {
                    ((TextView) view).setText(config.getMenuName());
                }
                if (view1 instanceof Button) {
                    view1.setVisibility(View.INVISIBLE);
                }

            }
        }

    }



    private Vector<ProductMasterBO> filterWareHouseProducts() {
        Vector<ProductMasterBO> newItems = new Vector<>();
        if (bmodel.configurationMasterHelper.IS_LOAD_WAREHOUSE_PRD_ONLY) {
            for (ProductMasterBO products : bmodel.productHelper
                    .getProductMaster()) {
                if (products.isAvailableinWareHouse()) newItems.add(products);
            }
        } else {
            newItems.addAll(bmodel.productHelper
                    .getProductMaster());
        }
        return newItems;
    }

    public class FilterAdapter extends RecyclerView.Adapter<FilterAdapter.MyViewHolder> {

        private Vector<LevelBO> filterList;

        public class MyViewHolder extends RecyclerView.ViewHolder {
            public Button btnFilter;

            public MyViewHolder(View view) {
                super(view);
                btnFilter = (Button) view.findViewById(R.id.btn_filter);
                btnFilter.setTypeface(FontUtils.getFontBalooHai(StockAndOrder.this, FontUtils.FontType.REGULAR));
            }
        }

        public FilterAdapter(Vector<LevelBO> filterList) {
            this.filterList = filterList;
        }

        @Override
        public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.filter_rv_item, parent, false);

            return new MyViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(MyViewHolder holder, final int position) {
            final LevelBO levelBO = filterList.get(position);
            holder.btnFilter.setText(levelBO.getLevelName());
            try {
                if (mSelectedIdByLevelId != null && mSelectedLevelBO != null) {
                    int levelId = mSelectedIdByLevelId.get(mSelectedLevelBO
                            .getProductID());

                    if (levelId == levelBO.getProductID()) {
                        holder.btnFilter.setBackgroundResource(R.drawable.button_rounded_corner_blue);
                        holder.btnFilter.setTextColor(ContextCompat.getColor(StockAndOrder.this, R.color.white));
                    } else {
                        holder.btnFilter.setBackgroundResource(R.drawable.button_round_corner_grey);
                        holder.btnFilter.setTextColor(ContextCompat.getColor(StockAndOrder.this, R.color.half_Black));
                    }
                }
            } catch (Exception e) {
                Commons.printException(e);
            }

            holder.btnFilter.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mSelectedIdByLevelId == null || mSelectedIdByLevelId.size() == 0) {
                        mSelectedIdByLevelId = new HashMap<>();

                        for (LevelBO levelBO : sequence) {

                            mSelectedIdByLevelId.put(levelBO.getProductID(), 0);
                        }
                    }
                    int levelId = mSelectedIdByLevelId.get(mSelectedLevelBO
                            .getProductID());
                    if (levelId == filterList.get(position).getProductID()) {
                        mSelectedIdByLevelId.put(
                                mSelectedLevelBO.getProductID(), 0);
                    } else {
                        mSelectedIdByLevelId.put(mSelectedLevelBO
                                .getProductID(), filterList.get(position)
                                .getProductID());
                    }

                    updateSelectedID();

                    int mFilteredPId = 0;
                    int size = sequence.size();
                    for (int i = size - 1; i >= 0; i--) {
                        if (mSelectedIdByLevelId.get(sequence.get(i).getProductID()) != null && mSelectedIdByLevelId.get(sequence.get(i).getProductID()) > 0) {
                            for (LevelBO bo : loadedFilterValues.get(sequence.get(i).getProductID())) {
                                if (bo.getProductID() == mSelectedIdByLevelId.get(sequence.get(i).getProductID())) {
                                    mFilteredPId = bo.getProductID();
                                    i = -1;
                                    break;
                                }
                            }
                        }
                    }
                    if (bmodel.productHelper.getmAttributeTypes() != null && bmodel.productHelper.getmAttributeTypes().size() > 0) {

                        if (isAttributeFilterSelected()) {
                            //if product filter is also selected then, final parent id list will prepared to show products based on both attribute and product filter
                            if (isFilterContentSelected(sequence.size() - bmodel.productHelper.getmAttributeTypes().size())) {
                                // mFilteredPId = updateProductLoad((sequence.size() - bmodel.productHelper.getmAttributeTypes().size()));
                            }

                            ArrayList<Integer> lstSelectedAttributesIds = new ArrayList<>();
                            for (LevelBO bo : sequence) {
                                for (int i = 0; i < mSelectedIdByLevelId.size(); i++) {
                                    if (mSelectedIdByLevelId.get(bo.getProductID()) > 0) {
                                        lstSelectedAttributesIds.add(mSelectedIdByLevelId.get(bo.getProductID()));
                                    }

                                }
                            }

                            ArrayList<Integer> lstFinalProductIds = new ArrayList<>();
                            for (int j = 0; j < lstSelectedAttributesIds.size(); j++) {
                                for (int k = 0; k < bmodel.productHelper.getLstProductAttributeMapping().size(); k++) {

                                    if (bmodel.productHelper.getLstProductAttributeMapping().get(k).getAttributeId() == lstSelectedAttributesIds.get(j)
                                            && !lstFinalProductIds.contains(bmodel.productHelper.getLstProductAttributeMapping().get(k).getProductId())) {
                                        lstFinalProductIds.add(bmodel.productHelper.getLstProductAttributeMapping().get(k).getProductId());
                                    }
                                }
                            }
                            updateFromFiveLevelFilter(mFilteredPId, mSelectedIdByLevelId, lstFinalProductIds, levelBO.getLevelName());
                            return;
                        }
                    }

                    updateFromFiveLevelFilter(mFilteredPId, mSelectedIdByLevelId, null, levelBO.getLevelName());
                }
            });

        }

        @Override
        public int getItemCount() {
            return filterList.size();
        }
    }

    private void updateSelectedID() {
        boolean flag = false;

        for (LevelBO levelBO : sequence) {
            if (flag) {
                mSelectedIdByLevelId.put(levelBO.getProductID(), 0);
            }
            if (mSelectedLevelBO.getProductID() == levelBO.getProductID()) {
                int selectedLeveId = mSelectedIdByLevelId.get(levelBO.getProductID());
                if (selectedLeveId != 0) {
                    flag = true;
                }

            }

        }

    }

    private boolean isAttributeFilterSelected() {
        for (LevelBO bo : sequence) {
            if (mSelectedIdByLevelId.get(bo.getProductID()) > 0 && bo.getProductID() < 0) {
                return true;
            }
        }
        return false;
    }

    private boolean isFilterContentSelected(int pos) {
        for (int i = 0; i <= pos - 1; i++) {
            if (i <= sequence.size()) {
                LevelBO levelbo = sequence.get(i);
                if (mSelectedIdByLevelId.get(levelbo.getProductID()) != 0) {
                    return true;
                }
            }

        }
        return false;
    }



    //This method Adds product after barcode reads value and updates view
    private void barcodeScannerorder(final ProductMasterBO product) {
        String qty = "1";
        if (product.getPcUomid() == 0) {
//            product.setOrderedPcsQty(0);
            return;
        }
        /** Calculate the total pcs qty **/
        float totalQty = (product.getOrderedCaseQty() * product
                .getCaseSize())
                + (product.getOrderedPcsQty() + SDUtil.convertToInt(qty))
                + (product.getOrderedOuterQty() * product
                .getOutersize());
        if (product.isAllocation() == 1
                && bmodel.configurationMasterHelper.IS_SIH_VALIDATION) {
            if (totalQty <= product.getSIH()) {
                if (!"".equals(qty)) {
                    product.setOrderedPcsQty(product.getOrderedPcsQty() + SDUtil
                            .convertToInt(qty));
                }
                double tot = (product
                        .getOrderedCaseQty() * product
                        .getCsrp())
                        + (product.getOrderedPcsQty() * product
                        .getSrp())
                        + (product
                        .getOrderedOuterQty() * product
                        .getOsrp());
//                holder.total.setText(bmodel.formatValue(tot));
                product.setTotalamount(tot);
            } else {
                if (!"0".equals(qty)) {
                    Toast.makeText(
                            StockAndOrder.this,
                            String.format(
                                    getResources().getString(
                                            R.string.exceed),
                                    product.getSIH()),
                            Toast.LENGTH_SHORT).show();
                    //Delete the last entered number and reset the qty
                    qty = qty.length() > 1 ? qty.substring(0,
                            qty.length() - 1) : "0";
                    product.setOrderedPcsQty(product.getOrderedPcsQty() + SDUtil
                            .convertToInt(qty));
//                    holder.pcsQty.setText(qty);
                }
            }
        } else if (product.isCbsihAvailable()) {
            if (totalQty <= product.getCpsih()) {
                if (!"".equals(qty)) {
                    product.setOrderedPcsQty(product.getOrderedPcsQty() + SDUtil
                            .convertToInt(qty));
                }

                double tot = (product.getOrderedCaseQty() * product
                        .getCsrp())
                        + (product.getOrderedPcsQty() * product
                        .getSrp())
                        + (product.getOrderedOuterQty() * product
                        .getOsrp());
                product.setTotalamount(tot);
            } else {
                if (!"0".equals(qty)) {
                    Toast.makeText(
                            StockAndOrder.this,
                            String.format(
                                    getResources().getString(
                                            R.string.exceed),
                                    product.getCpsih()),
                            Toast.LENGTH_SHORT).show();
                    //Delete the last entered number and reset the qty
                    qty = qty.length() > 1 ? qty.substring(0,
                            qty.length() - 1) : "0";
                    product.setOrderedPcsQty(product.getOrderedPcsQty() + SDUtil
                            .convertToInt(qty));
                }
            }
        } else {
            if (!"".equals(qty)) {
                product.setOrderedPcsQty(product.getOrderedPcsQty() + SDUtil
                        .convertToInt(qty));
            }
            double tot = (product.getOrderedCaseQty() * product
                    .getCsrp())
                    + (product.getOrderedPcsQty() * product
                    .getSrp())
                    + (product.getOrderedOuterQty() * product
                    .getOsrp());
            product.setTotalamount(tot);
        }
        if (bmodel.configurationMasterHelper.IS_SHOW_ORDERING_SEQUENCE)
            updateData(product);

        updateOrderedCount();

        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                //stuff that updates ui
                int pos = mSchedule.getPosition(product);
                updateScreenTitle();
                mSchedule.notifyDataSetChanged();
                updateValue();
                lvwplist.setSelection(pos);
                lvwplist.setItemChecked(pos, true);
                lvwplist.setFocusable(true);
            }
        });
    }

    String barcode = "";

    @Override
    public boolean dispatchKeyEvent(KeyEvent e) {//android:configChanges="orientation|screenSize|keyboard|keyboardHidden|navigation"

        if (e.getDevice() != null) {
            //dispatchKeyEvent event will be called two times ACTION_DOWN and ACTION_UP
            // so to avoid repeating characters we called ACTION_DOWN
            // ACTION_UP may give small characters suppose if we scanned caps caps charc
            //ACTION_DOWN is device key pressed event to scan barcode value
            if (e.getAction() == KeyEvent.ACTION_DOWN) {
                if ((e.getDevice() != null) && e.getDevice().getName().startsWith("CYCLOPS")) {
                    char pressedKey = (char) e.getUnicodeChar();
                    barcode += pressedKey;
                    if (e.getKeyCode() == KeyEvent.KEYCODE_ENTER) {
                        new CheckAndEnter().execute();
                    }
                    return false;
                } else {
                    return super.dispatchKeyEvent(e);
                }
            }
            return super.dispatchKeyEvent(e);
        }
        return super.dispatchKeyEvent(e);
    }

    //if barcode value present asyntask will be called
    class CheckAndEnter extends
            AsyncTask<String, Integer, Boolean> {

        @Override
        protected Boolean doInBackground(String... arg0) {
            try {

                barcode = barcode.replace("\n", "");
                if (barcode != null)
                    for (final ProductMasterBO p : mSchedule.items) {
                        if (p.getBarCode().equals(barcode)) {
                            //Calls this methode to add Product piece
                            barcodeScannerorder(p);
                            return Boolean.TRUE;
                        }
                    }
                return Boolean.FALSE;
            } catch (Exception e) {
                Commons.printException(e);
                barcode = "";
                return Boolean.FALSE;
            }
        }

        protected void onPreExecute() {
            showProgressDialog(getResources().getString(R.string.loading));
        }

        protected void onPostExecute(Boolean result) {
            // result is the value returned from doInBackground
            barcode = "";
            dismissProgressDialog();
            if (!result) {
                Toast.makeText(getApplicationContext(), "Product not available", Toast.LENGTH_LONG)
                        .show();
            }
        }
    }

    @Override
    protected void onRestart() {
        super.onRestart();

    }

    private void showSalesReturnDialog(String productId, View v, int holderPostion, int holderTop) {
        Intent intent = new Intent(this, SalesReturnEntryActivity.class);
        intent.putExtra("pid", productId);
        intent.putExtra("position", holderPostion);
        intent.putExtra("top", holderTop);
        intent.putExtra("from", "ORDER");

        ActivityOptionsCompat opts = ActivityOptionsCompat.makeCustomAnimation(this, R.anim.zoom_enter, R.anim.hold);
        ActivityCompat.startActivityForResult(this, intent, SALES_RETURN, opts.toBundle());
    }

    public void refreshList() {
    }


    public class wareHouseStockBroadCastReceiver extends BroadcastReceiver {
        public static final String RESPONSE = "com.ivy.intent.action.WareHouseStock";

        @Override
        public void onReceive(Context arg0, Intent arg1) {
            updateReceiver(arg1);
        }

    }

    private void registerReceiver() {
        IntentFilter filter = new IntentFilter(
                wareHouseStockBroadCastReceiver.RESPONSE);
        filter.addCategory(Intent.CATEGORY_DEFAULT);
        mWareHouseStockReceiver = new wareHouseStockBroadCastReceiver();
        registerReceiver(mWareHouseStockReceiver, filter);
    }

    private void updateReceiver(Intent intent) {
        Bundle bundle = intent.getExtras();
        int method = bundle.getInt(SynchronizationHelper.SYNXC_STATUS, 0);
        String errorCode = bundle.getString(SynchronizationHelper.ERROR_CODE);

        switch (method) {
            case SynchronizationHelper.WAREHOUSE_STOCK_DOWNLOAD:
                if (errorCode != null && errorCode.equals(SynchronizationHelper.AUTHENTICATION_SUCCESS_CODE)) {
                    dismissProgressDialog();
                    bmodel.showAlert(getResources().getString(R.string.stock_download_successfully), 0);
                    orderHelper.updateWareHouseStock(getApplicationContext());
                    lvwplist.invalidateViews();

                } else {
                    String errorDownloadCode = bundle.getString(SynchronizationHelper.ERROR_CODE);
                    String errorDownloadMessage = bmodel.synchronizationHelper.getErrormessageByErrorCode().get(errorDownloadCode);
                    if (errorDownloadMessage != null) {
                        Toast.makeText(this, errorDownloadMessage, Toast.LENGTH_SHORT).show();
                    }
                    dismissProgressDialog();
                    break;
                }
                break;
            default:
                break;
        }

    }

    class DownloadNewStock extends AsyncTask<Integer, Integer, Integer> {

        private int downloadStatus = 0;

        protected void onPreExecute() {
            showProgressDialog(getResources().getString(R.string.loading));
        }

        @Override
        protected Integer doInBackground(Integer... params) {
            try {
                bmodel.synchronizationHelper.updateAuthenticateToken(false);

            } catch (Exception e) {
                Commons.printException("" + e);
                return downloadStatus;
            }
            return downloadStatus;
        }

        protected void onProgressUpdate(Integer... progress) {

        }

        @Override
        protected void onPostExecute(Integer integer) {
            super.onPostExecute(integer);
            if (bmodel.synchronizationHelper.getAuthErroCode().equals(SynchronizationHelper.AUTHENTICATION_SUCCESS_CODE)) {
                String warehouseWebApi = bmodel.synchronizationHelper.downloadWareHouseStockURL();
                if (!warehouseWebApi.equals("")) {
                    bmodel.synchronizationHelper.downloadWareHouseStock(warehouseWebApi);
                } else {
                    Toast.makeText(StockAndOrder.this, getResources().getString(R.string.url_not_mapped), Toast.LENGTH_SHORT).show();
                    dismissProgressDialog();
                }
            } else {
                String errorMsg = bmodel.synchronizationHelper.getErrormessageByErrorCode().get(bmodel.synchronizationHelper.getAuthErroCode());
                if (errorMsg != null) {
                    Toast.makeText(StockAndOrder.this, errorMsg, Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(StockAndOrder.this, getResources().getString(R.string.data_not_downloaded), Toast.LENGTH_SHORT).show();
                }
                dismissProgressDialog();
            }
        }
    }

    /**
     * @param productBO
     * @param isClick
     * @return isClick - true than return available uom Name
     * change Uom name based on SelectedUomPosition position
     * isClick - false than return Default Uom Name
     */

    private String updateUOM(ProductMasterBO productBO, boolean isClick) {
        String uomName = "";

        if (isClick) {
            if (productBO.getSelectedUomPosition() == productBO.getProductWiseUomList().size()) {
                productBO.setSelectedUomId(SDUtil.convertToInt(
                        productBO.getProductWiseUomList().get(0).getListID()));
                productBO.setSelectedUomPosition(1);
                uomName = productBO.getProductWiseUomList().get(0).getListName();
            } else if (productBO.getSelectedUomPosition() == 1) {
                productBO.setSelectedUomId(SDUtil.convertToInt(
                        productBO.getProductWiseUomList().get(1).getListID()));
                productBO.setSelectedUomPosition(2);
                uomName = productBO.getProductWiseUomList().get(1).getListName();
            } else if (productBO.getSelectedUomPosition() == 2) {
                productBO.setSelectedUomId(SDUtil.convertToInt(
                        productBO.getProductWiseUomList().get(2).getListID()));
                productBO.setSelectedUomPosition(3);
                uomName = productBO.getProductWiseUomList().get(2).getListName();
            }
        } else if (!isClick) {
            for (int i = 0; i < productBO.getProductWiseUomList().size(); i++) {
                StandardListBO proUomBo = productBO.getProductWiseUomList().get(i);
                if (proUomBo.getListID().equals(productBO.getSelectedUomId() + "")
                        && productBO.getSelectedUomId() == productBO.getPcUomid()) {
                    productBO.setSelectedUomPosition(i + 1);
                    uomName = proUomBo.getListName();
                    break;
                } else if (proUomBo.getListID().equals(productBO.getSelectedUomId() + "")
                        && productBO.getSelectedUomId() == productBO.getCaseUomId()) {
                    productBO.setSelectedUomId(productBO.getCaseUomId());
                    productBO.setSelectedUomPosition(i + 1);
                    uomName = proUomBo.getListName();
                    break;
                } else if (proUomBo.getListID().equals(productBO.getSelectedUomId() + "")
                        && productBO.getSelectedUomId() == productBO.getOuUomid()) {
                    productBO.setSelectedUomId(productBO.getOuUomid());
                    productBO.setSelectedUomPosition(i + 1);
                    uomName = proUomBo.getListName();
                    break;
                }
            }
        }

        return uomName;
    }

    private void showProgressDialog(String msg) {
        if (alertDialog == null) {
            AlertDialog.Builder build = new AlertDialog.Builder(StockAndOrder.this);
            customProgressDialog(build, msg);
            alertDialog = build.create();
        }
        alertDialog.show();
    }

    private void dismissProgressDialog() {
        if (alertDialog != null && alertDialog.isShowing()) {
            alertDialog.dismiss();
        }
    }

    private boolean hasStockChecked(ProductMasterBO productMasterBO) {
        int siz1 = productMasterBO.getLocations().size();
        for (int j = 0; j < siz1; j++) {
            if (productMasterBO.getLocations().get(j).getShelfPiece() > -1
                    || productMasterBO.getLocations().get(j).getShelfCase() > -1
                    || productMasterBO.getLocations().get(j).getShelfOuter() > -1)
                return true;
        }

        return false;
    }

    @Override
    public void productSearchResult(Vector<ProductMasterBO> searchedList) {

        mSelectedFilter=-1;// clearing filter flag

        mylist.clear();
        mylist.addAll(searchedList);

            mSchedule = new MyAdapter(mylist);
            lvwplist.setAdapter(mSchedule);

            updateValue();
            updateOrderedCount();
            if(getSupportActionBar()!=null) {
                updateScreenTitle();
            }



        supportInvalidateOptionsMenu();

    }



    @Override
    protected void onPause() {
        super.onPause();
        productSearch.dismissDialog();
    }

    private void prepareTopOrderFilter(){

        loadedFilterValues = bmodel.productHelper.getFilterProductsByLevelId();
        sequence = bmodel.productHelper.getFilterProductLevels();

        if (loadedFilterValues != null) {
            if (loadedFilterValues.get(-1) == null) {
                if (bmodel.productHelper.getmAttributesList() != null && bmodel.productHelper.getmAttributesList().size() > 0) {
                    int newAttributeId = 0;
                    for (AttributeBO bo : bmodel.productHelper.getmAttributeTypes()) {
                        newAttributeId -= 1;
                        sequence.add(new LevelBO(bo.getAttributeTypename(), newAttributeId, -1));
                        Vector<LevelBO> lstAttributes = new Vector<>();
                        LevelBO attLevelBO;
                        for (AttributeBO attrBO : bmodel.productHelper.getmAttributesList()) {
                            attLevelBO = new LevelBO();
                            if (bo.getAttributeTypeId() == attrBO.getAttributeLovId()) {
                                attLevelBO.setProductID(attrBO.getAttributeId());
                                attLevelBO.setLevelName(attrBO.getAttributeName());
                                lstAttributes.add(attLevelBO);
                            }
                        }
                        loadedFilterValues.put(newAttributeId, lstAttributes);

                    }

                }
            }
        }

        if (sequence == null) {
            sequence = new Vector<LevelBO>();
        }

        if (mSelectedIdByLevelId == null || mSelectedIdByLevelId.size() == 0) {
            mSelectedIdByLevelId = new HashMap<>();
            for (LevelBO levelBO : sequence) {

                mSelectedIdByLevelId.put(levelBO.getProductID(), 0);
            }
        }

        if (!sequence.isEmpty()) {
            mSelectedLevelBO = sequence.get(0);
            int levelID = sequence.get(0).getProductID();
            Vector<LevelBO> filterValues = new Vector<>();
            filterValues.addAll(loadedFilterValues.get(levelID));
            filterAdapter = new FilterAdapter(filterValues);
            rvFilterList.setAdapter(filterAdapter);
        }
    }
}