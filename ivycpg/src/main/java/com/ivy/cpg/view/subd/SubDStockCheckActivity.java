package com.ivy.cpg.view.subd;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.AppCompatCheckBox;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.InputType;
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
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.ivy.cpg.view.order.OrderHelper;
import com.ivy.cpg.view.order.discount.DiscountHelper;
import com.ivy.cpg.view.price.PriceTrackingHelper;
import com.ivy.cpg.view.salesreturn.SalesReturnHelper;
import com.ivy.cpg.view.stockcheck.StockCheckHelper;
import com.ivy.cpg.view.survey.SurveyActivityNew;
import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.bo.AttributeBO;
import com.ivy.sd.png.bo.ConfigureBO;
import com.ivy.sd.png.bo.GuidedSellingBO;
import com.ivy.sd.png.bo.LevelBO;
import com.ivy.sd.png.bo.OrderHeader;
import com.ivy.sd.png.bo.ProductMasterBO;
import com.ivy.sd.png.bo.StandardListBO;
import com.ivy.sd.png.commons.IvyBaseActivityNoActionBar;
import com.ivy.sd.png.commons.SDUtil;
import com.ivy.sd.png.model.BrandDialogInterface;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.model.FiveLevelFilterCallBack;
import com.ivy.sd.png.provider.ConfigurationMasterHelper;
import com.ivy.sd.png.util.CommonDialog;
import com.ivy.sd.png.util.Commons;
import com.ivy.sd.png.view.CustomKeyBoard;
import com.ivy.sd.png.view.FilterFiveFragment;
import com.ivy.sd.png.view.ReasonPhotoDialog;
import com.ivy.sd.png.view.RemarksDialog;
import com.ivy.sd.png.view.SpecialFilterFragment;
import com.ivy.utils.DateTimeUtils;
import com.ivy.utils.FontUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Vector;

public class SubDStockCheckActivity extends IvyBaseActivityNoActionBar implements View.OnClickListener,
        BrandDialogInterface, TextView.OnEditorActionListener,FiveLevelFilterCallBack {

    private ListView lvwplist;
    private Button mBtn_Search;
    private Button mBtnFilterPopup;
    private Button mBtn_clear;
    private TextView lpcText;
    private TextView productName;
    private TextView pnametitle;
    private BusinessModel bmodel;
    private Vector<ProductMasterBO> mylist;
    private EditText QUANTITY;
    private EditText mEdt_searchproductName;
    private String append = "";
    private String brandbutton;
    private String generalbutton;
    LinearLayout ll_spl_filter, ll_tab_selection;
    private DrawerLayout mDrawerLayout;
    private ViewFlipper viewFlipper;

    private ArrayList<String> mSearchTypeArray = new ArrayList<>();
    private InputMethodManager inputManager;

    private static final String BRAND = "Brand";
    private static final String GENERAL = "General";

    private final HashMap<String, String> mSelectedFilterMap = new HashMap<>();

    private final String mSbd = "Filt02";
    private final String mSbdGaps = "Filt03";
    private final String mOrdered = "Filt04";
    private final String mPurchased = "Filt05";
    private final String mInitiative = "Filt06";
    private final String mOnAllocation = "Filt07";
    private final String mInStock = "Filt08";
    private final String mPromo = "Filt09";
    private final String mMustSell = "Filt10";
    private final String mFocusBrand = "Filt11";
    private final String mFocusBrand2 = "Filt12";
    private final String msih = "Filt13";
    private final String mOOS = "Filt14";
    private final String mNMustSell = "Filt16";
    private final String mStock = "Filt17";
    private final String mDiscount = "Filt18";
    private final String mSuggestedOrder = "Filt25";

    private boolean isSbd;
    private boolean isSbdGaps;
    private boolean isOrdered;
    private boolean isPurchased;
    private boolean isInitiative;
    private boolean isOnAllocation;
    private boolean isInStock;
    private boolean isPromo;
    private boolean isMustSell;
    private boolean isFocusBrand;
    private boolean isFocusBrand2;
    private boolean isSIH;
    private boolean isOOS;
    private boolean isNMustSell;
    private boolean isStock;
    private boolean isDiscount;

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
    private final String FROM_HOME_SCREEN = "IsFromHomeScreen";


    private int mSelectedBrandID = 0;
    private String mSelectedFiltertext = "Brand";

    private ArrayAdapter<StandardListBO> mLocationAdapter;
    private int mSelectedLocationIndex;
    private SubDStockCheckActivity.MyAdapter mSchedule;
    private String tempPo;
    private String tempRemark;
    private String tempRField1;
    private String tempRField2;
    private HashMap<Integer, Integer> mSelectedIdByLevelId;
    private LevelBO mSelectedLevelBO = new LevelBO();
    private HashMap<Integer, Vector<LevelBO>> loadedFilterValues;
    private Vector<LevelBO> sequence;
    private SubDStockCheckActivity.FilterAdapter filterAdapter;
    private RecyclerView rvFilterList;
    private String strProductObj;
    private int SbdDistPre = 0; // Dist stock
    private int sbdDistAchieved = 0;
    private Button mBtnNext;
    private Button mBtnGuidedSelling_next, mBtnGuidedSelling_prev;

    private ArrayList<String> fiveFilter_productIDs;

    private LinkedList<String> mProductList = new LinkedList<>();

    private final String mStockCode = "STK";
    private final String mOrderCode = "ORD";

    private boolean isFilter = true;// only for guided selling. Default value is true, so it will ot affect normal flow

    private String title;
    private String totalOrdCount;

    private Vector<ProductMasterBO> productList = new Vector<>();

    boolean isFromHomeScreen = false;
    private OrderHelper orderHelper;

    private static final int SALES_RETURN = 3;
    SearchAsync searchAsync;
    private StockCheckHelper stockCheckHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sub_dstock_check);


        bmodel = (BusinessModel) getApplicationContext();
        bmodel.setContext(this);
        overridePendingTransition(R.anim.trans_left_in, R.anim.trans_left_out);
        orderHelper = OrderHelper.getInstance(this);
        stockCheckHelper = StockCheckHelper.getInstance(this);

        if (bmodel.configurationMasterHelper.SHOW_BARCODE)
            checkAndRequestPermissionAtRunTime(2);

        Toolbar toolbar = findViewById(R.id.toolbar);
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
                isFromHomeScreen = extras.getBoolean(FROM_HOME_SCREEN, false);
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
            isFromHomeScreen = savedInstanceState.getBoolean(FROM_HOME_SCREEN, false);
        }

        FrameLayout drawer = findViewById(R.id.right_drawer);

        int width = getResources().getDisplayMetrics().widthPixels;
        DrawerLayout.LayoutParams params = (android.support.v4.widget.DrawerLayout.LayoutParams) drawer.getLayoutParams();
        params.width = width;
        drawer.setLayoutParams(params);

        mDrawerLayout = findViewById(R.id.drawer_layout);
        // set a custom shadow that overlays the main content when the drawer
        // opens
        mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow,
                GravityCompat.START);
        mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow,
                GravityCompat.END);

        mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);

        inputManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

        viewFlipper = findViewById(R.id.view_flipper);

        mEdt_searchproductName = findViewById(R.id.edt_searchproductName);
        mBtn_Search = findViewById(R.id.btn_search);
        mBtnFilterPopup = findViewById(R.id.btn_filter_popup);
        mBtn_clear = findViewById(R.id.btn_clear);
        mBtnNext = findViewById(R.id.btn_next);
        mBtnGuidedSelling_next = findViewById(R.id.btn_guided_selling_next);
        mBtnGuidedSelling_prev = findViewById(R.id.btn_guided_selling_prev);
        mBtnGuidedSelling_next.setOnClickListener(this);
        mBtnGuidedSelling_prev.setOnClickListener(this);

        mBtn_Search.setOnClickListener(this);
        mBtnNext.setOnClickListener(this);
        mBtnNext.setText(R.string.save);
        mBtnFilterPopup.setOnClickListener(this);
        mBtn_clear.setOnClickListener(this);
        mEdt_searchproductName.setOnEditorActionListener(this);

        mBtnNext.setTypeface(FontUtils.getFontBalooHai(this,FontUtils.FontType.REGULAR));

        String title = bmodel.configurationMasterHelper
                .getHomescreentwomenutitle("MENU_SUBD_STOCK");

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

                if (getSupportActionBar() != null) {
                    updateScreenTitle();
                }

                supportInvalidateOptionsMenu();
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


        if (!isFromHomeScreen && bmodel.configurationMasterHelper.IS_REMOVE_TAX_ON_SRP) {
            bmodel.resetSRPvalues();
        }

        // load location filter
        mLocationAdapter = new ArrayAdapter<>(this, android.R.layout.select_dialog_singlechoice);

        for (StandardListBO temp : bmodel.productHelper.getInStoreLocation())
            mLocationAdapter.add(temp);
        if (bmodel.configurationMasterHelper.IS_GLOBAL_LOCATION) {
            mSelectedLocationIndex = bmodel.productHelper.getmSelectedGLobalLocationIndex();
        }

        lpcText = findViewById(R.id.lcp);
        rvFilterList = findViewById(R.id.rvFilter);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext(), LinearLayoutManager.HORIZONTAL, false);
        rvFilterList.setLayoutManager(mLayoutManager);
        rvFilterList.setItemAnimator(new DefaultItemAnimator());

        hideAndSeek();

        (findViewById(R.id.calcdot))
                .setVisibility(View.VISIBLE);

        productName = findViewById(R.id.productName);
        productName.setTypeface(FontUtils.getProductNameFont(this));
        mEdt_searchproductName.setTypeface(FontUtils.getFontRoboto(this, FontUtils.FontType.LIGHT));


        lvwplist = findViewById(R.id.list);
        lvwplist.setCacheColorHint(0);

        productList = filterWareHouseProducts();
        /* Calculate the SBD Dist Acheivement value */
        loadSBDAchievementLocal();
        /* Calculate the total and LPC value */
        updateValue();

        mSearchTypeArray = new ArrayList<>();
        mSearchTypeArray.add(getResources().getString(R.string.product_name));
        mSearchTypeArray.add(getResources().getString(R.string.prod_code));
        mSearchTypeArray.add(getResources().getString(
                R.string.order_dialog_barcode));

        prepareScreen();

        try {
            mEdt_searchproductName.addTextChangedListener(new TextWatcher() {
                public void afterTextChanged(Editable s) {
                    if (s.length() >= 3) {
                        searchAsync = new SearchAsync();
                        searchAsync.execute();
                    }
                }

                @Override
                public void beforeTextChanged(CharSequence s, int start,
                                              int count, int after) {

                }

                @Override
                public void onTextChanged(CharSequence s, int start,
                                          int before, int count) {
                    if (mEdt_searchproductName.getText().toString().length() < 3) {
                        mylist.clear();
                    }
                    if (searchAsync.getStatus() == AsyncTask.Status.RUNNING) {
                        searchAsync.cancel(true);
                    }
                }
            });
        } catch (Exception e) {
            Commons.printException(e);
        }


        DisplayMetrics dm = new DisplayMetrics();
        getWindow().getWindowManager().getDefaultDisplay().getMetrics(dm);

        if (bmodel.configurationMasterHelper.SHOW_STORE_WISE_DISCOUNT_DLG) {
            DiscountHelper.getInstance(this).setMinimumRangeAsBillWiseDiscount();
        }

        findViewById(R.id.ll_dist).setVisibility(View.GONE);

        mDrawerLayout.closeDrawer(GravityCompat.END);
        searchAsync = new SearchAsync();
    }


    private void prepareScreen() {
        try {
            if ("FromSummary".equals(OrderedFlag)) {
                if (bmodel.configurationMasterHelper.SHOW_SPL_FILTER && !bmodel.configurationMasterHelper.SHOW_SPL_FLIER_NOT_NEEDED) {

                    getMandatoryFilters();
                    mSelectedFilterMap.put("General", mOrdered);
                    if (bmodel.configurationMasterHelper.IS_SPL_FILTER_TAB) {
                        loadSpecialFilterView();
                        updateGeneralText(mOrdered);
                        selectTab(mOrdered);
                    } else {
                        updateGeneralText(mOrdered);
                    }
                } else {
                    mSelectedFilterMap.put("General", GENERAL);
                    updateGeneralText(GENERAL);
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
                        getMandatoryFilters();
                        String defaultfilter = getDefaultFilter();
                        if (!"".equals(defaultfilter)) {
                            mSelectedFilterMap.put("General", defaultfilter);
                            if (bmodel.configurationMasterHelper.IS_SPL_FILTER_TAB) {
                                loadSpecialFilterView();
                                updateGeneralText(defaultfilter);
                                selectTab(defaultfilter);
                            } else {
                                updateGeneralText(defaultfilter);
                            }


                        } else {
                            mSelectedFilterMap.put("General", GENERAL);
                            if (bmodel.configurationMasterHelper.IS_SPL_FILTER_TAB) {
                                loadSpecialFilterView();
                                updateGeneralText(GENERAL);
                                selectTab(bmodel.configurationMasterHelper.getGenFilter().get(0).getConfigCode());
                            } else {
                                updateGeneralText(GENERAL);
                            }
                        }

                    } else {
                        mSelectedFilterMap.put("General", GENERAL);
                        updateGeneralText(GENERAL);
                    }
                }
            }
        } catch (Exception e) {
            Commons.printException(e + "");
        }
    }

    @SuppressLint("RestrictedApi")
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
                    if (!bo.getFilterCode().equals(mSuggestedOrder) || (bo.getFilterCode().equals(mSuggestedOrder) && isProductsAvailable(bo.getFilterCode()))) {
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
                            updateGeneralText(GENERAL);
                            if (bmodel.configurationMasterHelper.IS_SPL_FILTER_TAB)
                                selectTab(bmodel.configurationMasterHelper.getGenFilter().get(0).getConfigCode());
                        } else {
                            mSelectedFilterMap.put("General", bo.getFilterCode());
                            updateGeneralText(bo.getFilterCode());
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
                            updateGuidedSellingView(false, true);
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
                        updateGeneralText(GENERAL);
                        selectTab(bmodel.configurationMasterHelper.getGenFilter().get(0).getConfigCode());
                    } else {
                        updateGeneralText(GENERAL);
                    }
                } else {
                    mSelectedFilterMap.put("General", GENERAL);
                    updateGeneralText(GENERAL);
                }
            }
        } else {
            mSelectedFilterMap.put("General", GENERAL);
            updateGeneralText(GENERAL);
        }
    }

    private boolean isProductsAvailable(String filterCode) {
        try {
            if (filterCode.equalsIgnoreCase("ALL") && bmodel.productHelper.getTaggedProducts().size() > 0) {
                return true;
            } else {
                for (ProductMasterBO bo : bmodel.productHelper.getTaggedProducts()) {
                    if (isSpecialFilterAppliedProduct(filterCode, bo)) {
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
        switch (bo.getSubActivity()) {
            case mStockCode:
                if (isCurrentLogicForStockDone(bo.getFilterCode(), bo.getApplyLevel())) {
                    bo.setDone(true);
                } else
                    bo.setDone(false);
                break;
            case mOrderCode:
                if (isCurrentLogicForOrderDone(bo.getFilterCode(), bo.getApplyLevel())) {
                    bo.setDone(true);
                } else
                    bo.setDone(false);
                break;
            default:
                // To skip,in case of other codes mapped
                bo.setDone(true);
                break;
        }
    }

    private boolean isCurrentLogicForStockDone(String filterCode, String applyLevel) {
        if (filterCode.equals("ALL")) {
            switch (applyLevel) {
                case "ALL":
                    for (ProductMasterBO product : bmodel.productHelper.getTaggedProducts()) {
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
                case "ANY":
                    //ANY
                    boolean isStockChecked = true;
                    for (ProductMasterBO product : bmodel.productHelper.getTaggedProducts()) {
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
                default:
                    return true;
            }
        } else {
            switch (applyLevel) {
                case "ALL": {
                    boolean isStockChecked;
                    for (ProductMasterBO product : bmodel.productHelper.getTaggedProducts()) {
                        if (isSpecialFilterAppliedProduct(filterCode, product) && product.getIsSaleable() == 1) {
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
                    return true;
                    //filtered list have 0 products not allowed to navigate
                }
                case "ANY": {
                    //ANY
                    boolean isStockChecked = true;
                    for (ProductMasterBO product : bmodel.productHelper.getTaggedProducts()) {
                        if (isSpecialFilterAppliedProduct(filterCode, product) && product.getIsSaleable() == 1) {
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
                }
                default:
                    return true;
            }
        }
    }

    private boolean isCurrentLogicForOrderDone(String filterCode, String applyLevel) {
        if (filterCode.equals("ALL")) {
            switch (applyLevel) {
                case "ALL":
                    for (ProductMasterBO product : bmodel.productHelper.getTaggedProducts()) {
                        if (product.getOrderedCaseQty() <= 0 && product.getOrderedPcsQty() <= 0 && product.getOrderedOuterQty() <= 0) {
                            return false;
                        }
                    }
                    return false;
                case "ANY":
                    //ANY
                    for (ProductMasterBO product : bmodel.productHelper.getTaggedProducts()) {
                        if (product.getOrderedCaseQty() > 0 || product.getOrderedPcsQty() > 0 || product.getOrderedOuterQty() > 0) {
                            return true;
                        }
                    }
                    return false;
                default:
                    return true;
            }
        } else {
            switch (applyLevel) {
                case "ALL":
                    for (ProductMasterBO product : bmodel.productHelper.getTaggedProducts()) {
                        if (isSpecialFilterAppliedProduct(filterCode, product)) {
                            if (product.getOrderedCaseQty() <= 0 && product.getOrderedPcsQty() <= 0 && product.getOrderedOuterQty() <= 0) {
                                return false;
                            }
                        }
                    }
                    return false;
                case "ANY":
                    //ANY
                    for (ProductMasterBO product : bmodel.productHelper.getTaggedProducts()) {
                        if (isSpecialFilterAppliedProduct(filterCode, product)) {
                            if (product.getOrderedCaseQty() > 0 || product.getOrderedPcsQty() > 0 || product.getOrderedOuterQty() > 0) {
                                return true;
                            }
                        }
                    }
                    return false;
                default:
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
            isFromHomeScreen = extras.getBoolean(FROM_HOME_SCREEN, false);
            savedInstanceState.putSerializable(ORDER_FLAG, OrderedFlag);
            savedInstanceState.putSerializable(TEMP_PO, tempPo);
            savedInstanceState.putSerializable(TEMP_REMARK, tempRemark);
            savedInstanceState.putSerializable(TEMP_RFIELD1, tempRField1);
            savedInstanceState.putSerializable(TEMP_RFIELD2, tempRField2);
            savedInstanceState.putSerializable(SCREEN_CODE, screenCode);
            savedInstanceState.putSerializable(FROM_HOME_SCREEN, isFromHomeScreen);
        }
        super.onSaveInstanceState(savedInstanceState);
    }


    //update screen title

    private void updateScreenTitle() {
        String title;
        if (generalbutton.equals(GENERAL)) {
            title = bmodel.configurationMasterHelper
                    .getHomescreentwomenutitle("MENU_SUBD_STOCK");
            if (mSelectedFiltertext.equals("Brand")) {
                if (totalOrdCount.equals("0"))
                    setScreenTitle(title + " ("
                            + mylist.size() + ")");
                else
                    setScreenTitle(title + " ("
                            + totalOrdCount + "/" + mylist.size() + ")");

            } else if (!mSelectedFiltertext.equals("Brand")) {
                String strPname = mSelectedFiltertext + " (" + mylist.size() + ")";
                pnametitle.setText(strPname);
                if (bmodel.configurationMasterHelper.SHOW_SPL_FILTER && !bmodel.configurationMasterHelper.IS_SPL_FILTER_TAB) {
                    if (totalOrdCount.equals("0"))
                        setScreenTitle(strPname);
                    else
                        setScreenTitle(mSelectedFiltertext + " (" + totalOrdCount + "/" + mylist.size() + ")");

                }
            }
        } else if (!generalbutton.equals(GENERAL)) {
            String strPname = getFilterName(generalbutton) + " ("
                    + mylist.size() + ")";
            pnametitle.setText(strPname);

            if (bmodel.configurationMasterHelper.SHOW_SPL_FILTER && !bmodel.configurationMasterHelper.IS_SPL_FILTER_TAB) {
                if (totalOrdCount.equals("0"))
                    setScreenTitle(strPname);
                else
                    setScreenTitle(getFilterName(generalbutton) + " ("
                            + totalOrdCount + "/" + mylist.size() + ")");
            }

        }
    }


    /**
     * This method will on/off the items based in the configuration.
     */
    private void hideAndSeek() {
        try {
            if (!bmodel.configurationMasterHelper.SHOW_ORDER_TOTAL) {
                findViewById(R.id.totalTitle).setVisibility(View.GONE);
            }

            if (!screenCode.equals(ConfigurationMasterHelper.MENU_STOCK)) {
                if (bmodel.configurationMasterHelper.SHOW_OBJECTIVE) {
                    findViewById(R.id.ll_objective)
                            .setVisibility(View.VISIBLE);


                    ((TextView) findViewById(R.id.objectiveValue))
                            .setText(bmodel
                                    .formatValue(bmodel.retailerMasterBO
                                            .getMonthly_target()));
                }
                try {
                    ((TextView) findViewById(R.id.totalTitle)).setTypeface(FontUtils.getFontRoboto(this, FontUtils.FontType.MEDIUM));
                    if (bmodel.labelsMasterHelper.applyLabels(findViewById(
                            R.id.totalTitle).getTag()) != null)
                        ((TextView) findViewById(R.id.totalTitle))
                                .setText(bmodel.labelsMasterHelper
                                        .applyLabels(findViewById(
                                                R.id.totalTitle).getTag()));
                } catch (Exception e) {
                    Commons.printException(e + "");
                }

                if (!stockCheckHelper.SHOW_STOCK_SC
                        || screenCode
                        .equals(ConfigurationMasterHelper.MENU_ORDER)) {
                    findViewById(R.id.shelfCaseTitle).setVisibility(View.GONE);
                } else {
                    try {
                        ((TextView) findViewById(R.id.shelfCaseTitle)).setTypeface(FontUtils.getFontRoboto(this, FontUtils.FontType.MEDIUM));
                        if (bmodel.labelsMasterHelper.applyLabels(findViewById(
                                R.id.shelfCaseTitle).getTag()) != null)
                            ((TextView) findViewById(R.id.shelfCaseTitle))
                                    .setText(bmodel.labelsMasterHelper
                                            .applyLabels(findViewById(
                                                    R.id.shelfCaseTitle)
                                                    .getTag()));
                    } catch (Exception e) {
                        Commons.printException(e + "");
                    }
                }

                if (!stockCheckHelper.SHOW_STOCK_SP
                        || screenCode
                        .equals(ConfigurationMasterHelper.MENU_ORDER)) {
                    findViewById(R.id.shelfPcsTitle).setVisibility(View.GONE);
                } else {
                    try {
                        ((TextView) findViewById(R.id.shelfPcsTitle)).setTypeface(FontUtils.getFontRoboto(this, FontUtils.FontType.LIGHT));
                        if (bmodel.labelsMasterHelper.applyLabels(findViewById(
                                R.id.shelfPcsTitle).getTag()) != null)
                            ((TextView) findViewById(R.id.shelfPcsTitle))
                                    .setText(bmodel.labelsMasterHelper
                                            .applyLabels(findViewById(
                                                    R.id.shelfPcsTitle)
                                                    .getTag()));
                    } catch (Exception e) {
                        Commons.printException(e + "");
                    }
                }
                if (!stockCheckHelper.SHOW_STOCK_CB
                        || screenCode
                        .equals(ConfigurationMasterHelper.MENU_ORDER))
                    findViewById(R.id.shelfPcsCB).setVisibility(View.GONE);

                ((TextView) findViewById(R.id.shelfPcsCB)).setTypeface(FontUtils.getFontRoboto(this, FontUtils.FontType.LIGHT));


                // On/Off order case and pcs
                if (!bmodel.configurationMasterHelper.SHOW_ORDER_CASE) {
                    findViewById(R.id.caseTitle).setVisibility(View.GONE);
                } else {
                    try {
                        ((TextView) findViewById(R.id.caseTitle)).setTypeface(FontUtils.getFontRoboto(this, FontUtils.FontType.LIGHT));
                        if (bmodel.labelsMasterHelper.applyLabels(findViewById(
                                R.id.caseTitle).getTag()) != null)
                            ((TextView) findViewById(R.id.caseTitle))
                                    .setText(bmodel.labelsMasterHelper
                                            .applyLabels(findViewById(
                                                    R.id.caseTitle).getTag()));
                    } catch (Exception e) {
                        Commons.printException(e + "");
                    }
                }

                if (!bmodel.configurationMasterHelper.SHOW_STK_ORD_SRP) {
                    findViewById(R.id.srpTitle).setVisibility(View.GONE);
                } else {
                    try {
                        ((TextView) findViewById(R.id.srpTitle)).setTypeface(FontUtils.getFontRoboto(this, FontUtils.FontType.LIGHT));
                        if (bmodel.labelsMasterHelper.applyLabels(findViewById(
                                R.id.srpTitle).getTag()) != null)
                            ((TextView) findViewById(R.id.srpTitle))
                                    .setText(bmodel.labelsMasterHelper
                                            .applyLabels(findViewById(
                                                    R.id.srpTitle).getTag()));
                    } catch (Exception e) {
                        Commons.printException(e + "");
                    }
                }

                if (!stockCheckHelper.SHOW_SHELF_OUTER
                        || screenCode
                        .equals(ConfigurationMasterHelper.MENU_ORDER)) {
                    findViewById(R.id.shelfOuterTitle).setVisibility(View.GONE);
                } else {
                    try {
                        ((TextView) findViewById(R.id.shelfOuterTitle)).setTypeface(FontUtils.getFontRoboto(this, FontUtils.FontType.MEDIUM));
                        if (bmodel.labelsMasterHelper.applyLabels(findViewById(
                                R.id.shelfOuterTitle).getTag()) != null)
                            ((TextView) findViewById(R.id.shelfOuterTitle))
                                    .setText(bmodel.labelsMasterHelper
                                            .applyLabels(findViewById(
                                                    R.id.shelfOuterTitle)
                                                    .getTag()));
                    } catch (Exception e) {
                        Commons.printException(e + "");
                    }
                }
                if (!bmodel.configurationMasterHelper.SHOW_ICO) {
                    findViewById(R.id.icoTitle).setVisibility(View.GONE);
                } else {
                    try {
                        ((TextView) findViewById(R.id.icoTitle)).setTypeface(FontUtils.getFontRoboto(this, FontUtils.FontType.MEDIUM));
                        if (bmodel.labelsMasterHelper.applyLabels(findViewById(
                                R.id.icoTitle).getTag()) != null)
                            ((TextView) findViewById(R.id.icoTitle))
                                    .setText(bmodel.labelsMasterHelper
                                            .applyLabels(findViewById(
                                                    R.id.icoTitle).getTag()));
                    } catch (Exception e) {
                        Commons.printException(e + "");
                    }
                }
                if (!bmodel.configurationMasterHelper.SHOW_BARCODE) {
                    findViewById(R.id.productBarcodetitle).setVisibility(
                            View.GONE);
                } else {
                    try {
                        ((TextView) findViewById(R.id.productBarcodetitle)).setTypeface(FontUtils.getFontRoboto(this, FontUtils.FontType.MEDIUM));
                        if (bmodel.labelsMasterHelper.applyLabels(findViewById(
                                R.id.productBarcodetitle).getTag()) != null)
                            ((TextView) findViewById(R.id.productBarcodetitle))
                                    .setText(bmodel.labelsMasterHelper
                                            .applyLabels(findViewById(
                                                    R.id.productBarcodetitle)
                                                    .getTag()));
                    } catch (Exception e) {
                        Commons.printException(e + "");
                    }
                }

                //Replaced qty labels visibility change
                try {

                    if (bmodel.configurationMasterHelper.SHOW_REPLACED_QTY_PC || bmodel.configurationMasterHelper.SHOW_REPLACED_QTY_OU || bmodel.configurationMasterHelper.SHOW_REPLACED_QTY_CS) {
                        SalesReturnHelper salesReturnHelper = SalesReturnHelper.getInstance(this);
                        salesReturnHelper.clearSalesReturnTable(true);
                        bmodel.productHelper.updateSalesReturnInfoInProductObj(null, "0", false);
                    }



                } catch (Exception e) {
                    Commons.printException(e + "");
                }
            }

            if (bmodel.configurationMasterHelper.SHOW_CUSTOM_KEYBOARD_NEW) {
                findViewById(R.id.card_keyboard).setVisibility(View.GONE);
            } else {
                findViewById(R.id.card_keyboard).setVisibility(View.VISIBLE);
            }

            if (bmodel.configurationMasterHelper.SHOW_TOTAL_QTY_ORDER) {
                findViewById(R.id.ll_totqty).setVisibility(View.VISIBLE);
            } else {
                findViewById(R.id.ll_totqty).setVisibility(View.GONE);
            }

            if (bmodel.configurationMasterHelper.SHOW_TOTAL_VALUE_ORDER) {
                findViewById(R.id.ll_value).setVisibility(View.GONE);
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
                sequence = new Vector<>();
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
                Vector<LevelBO> filterValues = new Vector<>(loadedFilterValues.get(levelID));
                filterAdapter = new SubDStockCheckActivity.FilterAdapter(filterValues);
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
    }

    private class MyAdapter extends ArrayAdapter<ProductMasterBO> {
        private final Vector<ProductMasterBO> items;
        private CustomKeyBoard dialogCustomKeyBoard;

        public MyAdapter(Vector<ProductMasterBO> items) {
            super(SubDStockCheckActivity.this,
                    R.layout.row_subd_stockcheck_layout, items);
            this.items = items;
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

        @SuppressLint("RestrictedApi")
        public View getView(final int position, View convertView,
                            ViewGroup parent) {
            final SubDStockCheckActivity.ViewHolder holder;
            ProductMasterBO product = items.get(position);

            View row = convertView;
            if (row == null) {

                final LayoutInflater inflater = getLayoutInflater();

                row = inflater.inflate(
                        R.layout.row_subd_stockcheck_layout, parent,
                        false);
                holder = new ViewHolder();
                holder.psname =  row
                        .findViewById(R.id.stock_and_order_listview_productname);

                holder.imageButton_availability =  row.findViewById(R.id.btn_availability);
//
                //check - qty entry
                holder.shelfCaseQty =  row
                        .findViewById(R.id.stock_and_order_listview_sc_qty);
                holder.shelfPcsQty =  row
                        .findViewById(R.id.stock_and_order_listview_sp_qty);
                holder.shelfouter =  row
                        .findViewById(R.id.stock_and_order_listview_shelfouter_qty);

                holder.total =  row
                        .findViewById(R.id.stock_and_order_listview_total);


                holder.psname.setMaxLines(bmodel.configurationMasterHelper.MAX_NO_OF_PRODUCT_LINES);
                ( row.findViewById(R.id.view_dotted_line)).setLayerType(View.LAYER_TYPE_SOFTWARE, null);
                //setting typefaces
                holder.psname.setTypeface(FontUtils.getProductNameFont(SubDStockCheckActivity.this));
                holder.shelfCaseQty.setTypeface(FontUtils.getFontRoboto(SubDStockCheckActivity.this, FontUtils.FontType.MEDIUM));
                holder.shelfPcsQty.setTypeface(FontUtils.getFontRoboto(SubDStockCheckActivity.this, FontUtils.FontType.MEDIUM));
                holder.shelfouter.setTypeface(FontUtils.getFontRoboto(SubDStockCheckActivity.this, FontUtils.FontType.MEDIUM));
                holder.total.setTypeface(FontUtils.getFontRoboto(SubDStockCheckActivity.this, FontUtils.FontType.MEDIUM));


                if (!stockCheckHelper.SHOW_STOCK_CB)
                    ( row.findViewById(R.id.llAvail)).setVisibility(View.GONE);

                ((TextView) row.findViewById(R.id.shelfPcsCB)).setTypeface(FontUtils.getFontRoboto(SubDStockCheckActivity.this, FontUtils.FontType.LIGHT));

                if (!stockCheckHelper.SHOW_STOCK_SC)
                    ( row.findViewById(R.id.llShelfCase)).setVisibility(View.GONE);
                else {
                    try {
                        ((TextView) row.findViewById(R.id.shelfCaseTitle)).setTypeface(FontUtils.getFontRoboto(SubDStockCheckActivity.this, FontUtils.FontType.LIGHT));
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
                if (!stockCheckHelper.SHOW_STOCK_SP)
                    ( row.findViewById(R.id.llShelfPc)).setVisibility(View.GONE);
                else {
                    try {
                        ((TextView) row.findViewById(R.id.shelfPcsTitle)).setTypeface(FontUtils.getFontRoboto(SubDStockCheckActivity.this, FontUtils.FontType.LIGHT));

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
                if (!stockCheckHelper.SHOW_SHELF_OUTER)
                    ( row.findViewById(R.id.llShelfOuter)).setVisibility(View.GONE);
                else {
                    try {
                        ((TextView) row.findViewById(R.id.shelfOuterTitle)).setTypeface(FontUtils.getFontRoboto(SubDStockCheckActivity.this, FontUtils.FontType.LIGHT));
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

                if (!stockCheckHelper.SHOW_STOCK_TOTAL)
                    ( row.findViewById(R.id.llTotal)).setVisibility(View.GONE);
                else {
                    try {
                        ((TextView) row.findViewById(R.id.totalTitle)).setTypeface(FontUtils.getFontRoboto(SubDStockCheckActivity.this, FontUtils.FontType.LIGHT));
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

                holder.imageButton_availability.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (holder.productObj.getLocations()
                                .get(mSelectedLocationIndex).getAvailability() == -1) {
                            holder.productObj.getLocations()
                                    .get(mSelectedLocationIndex).setAvailability(1);

                            holder.imageButton_availability.setSupportButtonTintList(ColorStateList.valueOf(ContextCompat.getColor(SubDStockCheckActivity.this, R.color.green_productivity)));
                            holder.imageButton_availability.setChecked(true);

                        } else if (holder.productObj.getLocations()
                                .get(mSelectedLocationIndex).getAvailability() == 1) {
                            holder.productObj.getLocations()
                                    .get(mSelectedLocationIndex).setAvailability(0);

                            holder.imageButton_availability.setSupportButtonTintList(ColorStateList.valueOf(ContextCompat.getColor(SubDStockCheckActivity.this, R.color.RED)));
                            holder.imageButton_availability.setChecked(true);

                        } else if (holder.productObj.getLocations()
                                .get(mSelectedLocationIndex).getAvailability() == 0) {
                            holder.productObj.getLocations()
                                    .get(mSelectedLocationIndex).setAvailability(-1);

                            holder.imageButton_availability.setSupportButtonTintList(ColorStateList.valueOf(ContextCompat.getColor(SubDStockCheckActivity.this, R.color.checkbox_default_color)));
                            holder.imageButton_availability.setChecked(false);

                        }

                        updateValue();

                    }
                });


                holder.shelfCaseQty.addTextChangedListener(new TextWatcher() {
                    public void afterTextChanged(Editable s) {
                        String qty = s.toString();
                        if (qty.length() > 0)
                            holder.shelfCaseQty.setSelection(qty.length());

                        if (!"".equals(qty)) {
                            holder.productObj
                                    .getLocations()
                                    .get(mSelectedLocationIndex)
                                    .setShelfCase(
                                            SDUtil.convertToInt(holder.shelfCaseQty
                                                    .getText().toString()));


                            holder.shelfCaseQty.removeTextChangedListener(this);
                            holder.shelfCaseQty.addTextChangedListener(this);

                        } else {
                            holder.productObj
                                    .getLocations()
                                    .get(mSelectedLocationIndex)
                                    .setShelfCase(-1);
                        }

                        int totValue = getProductTotalValue(holder.productObj);
                        holder.total
                                .setText(totValue + "");
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
                                productName.setText(strProductObj);
                            } else
                                productName.setText(holder.pname);

                            if (dialogCustomKeyBoard == null || !dialogCustomKeyBoard.isDialogCreated()) {
                                dialogCustomKeyBoard = new CustomKeyBoard(SubDStockCheckActivity.this, holder.shelfCaseQty);
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

                    holder.shelfCaseQty.setOnTouchListener(new View.OnTouchListener() {
                        public boolean onTouch(View v, MotionEvent event) {
                            if (bmodel.configurationMasterHelper.SHOW_SIH_IN_PNAME) {
                                strProductObj = "[SIH :"
                                        + holder.productObj.getSIH() + "] "
                                        + holder.pname;
                                productName.setText(strProductObj);
                            } else
                                productName.setText(holder.pname);

                            QUANTITY = holder.shelfCaseQty;
                            QUANTITY.setTag(holder.productObj);
                            int inType = holder.shelfCaseQty.getInputType();
                            holder.shelfCaseQty.setInputType(InputType.TYPE_NULL);
                            holder.shelfCaseQty.onTouchEvent(event);
                            holder.shelfCaseQty.setInputType(inType);
                            holder.shelfCaseQty.requestFocus();
                            if (holder.shelfCaseQty.getText().length() > 0)
                                holder.shelfCaseQty.setSelection(holder.shelfCaseQty.getText().length());
                            inputManager.hideSoftInputFromWindow(
                                    mEdt_searchproductName.getWindowToken(), 0);
                            return true;
                        }
                    });
                }

                holder.shelfPcsQty.addTextChangedListener(new TextWatcher() {
                    public void afterTextChanged(Editable s) {
                        String qty = s.toString();
                        if (qty.length() > 0)
                            holder.shelfPcsQty.setSelection(qty.length());

                        if (!"".equals(qty)) {
                            int sp_qty = SDUtil.convertToInt(holder.shelfPcsQty
                                    .getText().toString());
                            holder.productObj.getLocations()
                                    .get(mSelectedLocationIndex)
                                    .setShelfPiece(sp_qty);


                            holder.shelfPcsQty.removeTextChangedListener(this);
                            holder.shelfPcsQty.addTextChangedListener(this);

                        } else {
                            holder.productObj.getLocations()
                                    .get(mSelectedLocationIndex)
                                    .setShelfPiece(-1);
                        }

                        int totValue = getProductTotalValue(holder.productObj);
                        holder.total
                                .setText(totValue + "");

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
                                productName.setText(strProductObj);
                            } else
                                productName.setText(holder.pname);

                            if (dialogCustomKeyBoard == null || !dialogCustomKeyBoard.isDialogCreated()) {
                                dialogCustomKeyBoard = new CustomKeyBoard(SubDStockCheckActivity.this, holder.shelfPcsQty);
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

                    holder.shelfPcsQty.setOnTouchListener(new View.OnTouchListener() {
                        public boolean onTouch(View v, MotionEvent event) {
                            if (bmodel.configurationMasterHelper.SHOW_SIH_IN_PNAME) {
                                strProductObj = "[SIH :"
                                        + holder.productObj.getSIH() + "] "
                                        + holder.pname;
                                productName.setText(strProductObj);
                            } else
                                productName.setText(holder.pname);

                            QUANTITY = holder.shelfPcsQty;
                            QUANTITY.setTag(holder.productObj);
                            int inType = holder.shelfPcsQty.getInputType();
                            holder.shelfPcsQty.setInputType(InputType.TYPE_NULL);
                            holder.shelfPcsQty.onTouchEvent(event);
                            holder.shelfPcsQty.setInputType(inType);
                            holder.shelfPcsQty.requestFocus();
                            if (holder.shelfPcsQty.getText().length() > 0)
                                holder.shelfPcsQty.setSelection(holder.shelfPcsQty.getText().length());
                            inputManager.hideSoftInputFromWindow(
                                    mEdt_searchproductName.getWindowToken(), 0);
                            return true;
                        }
                    });
                }

                holder.shelfouter.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void afterTextChanged(Editable s) {
                        String qty = s.toString();
                        if (qty.length() > 0)
                            holder.shelfouter.setSelection(qty.length());

                        if (!"".equals(qty)) {
                            int shelfoqty = SDUtil
                                    .convertToInt(holder.shelfouter.getText()
                                            .toString());
                            holder.productObj.getLocations()
                                    .get(mSelectedLocationIndex)
                                    .setShelfOuter(shelfoqty);


                            holder.shelfouter.removeTextChangedListener(this);
                            holder.shelfouter.addTextChangedListener(this);

                        } else {
                            holder.productObj.getLocations()
                                    .get(mSelectedLocationIndex)
                                    .setShelfOuter(-1);
                        }

                        int totValue = getProductTotalValue(holder.productObj);
                        holder.total
                                .setText(totValue + "");
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
                                productName.setText(strProductObj);
                            } else
                                productName.setText(holder.pname);

                            if (dialogCustomKeyBoard == null || !dialogCustomKeyBoard.isDialogCreated()) {
                                dialogCustomKeyBoard = new CustomKeyBoard(SubDStockCheckActivity.this, holder.shelfouter);
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

                    holder.shelfouter.setOnTouchListener(new View.OnTouchListener() {
                        public boolean onTouch(View v, MotionEvent event) {
                            if (bmodel.configurationMasterHelper.SHOW_SIH_IN_PNAME) {
                                strProductObj = "[SIH :"
                                        + holder.productObj.getSIH() + "] "
                                        + holder.pname;
                                productName.setText(strProductObj);
                            } else
                                productName.setText(holder.pname);

                            QUANTITY = holder.shelfouter;
                            QUANTITY.setTag(holder.productObj);
                            int inType = holder.shelfouter.getInputType();
                            holder.shelfouter.setInputType(InputType.TYPE_NULL);
                            holder.shelfouter.onTouchEvent(event);
                            holder.shelfouter.setInputType(inType);
                            holder.shelfouter.requestFocus();
                            if (holder.shelfouter.getText().length() > 0)
                                holder.shelfouter.setSelection(holder.shelfouter.getText().length());
                            inputManager.hideSoftInputFromWindow(
                                    mEdt_searchproductName.getWindowToken(), 0);
                            return true;
                        }
                    });
                }


                row.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        if (bmodel.configurationMasterHelper.SHOW_SIH_IN_PNAME) {
                            strProductObj = "[SIH :"
                                    + holder.productObj.getSIH() + "] "
                                    + holder.pname;
                            productName.setText(strProductObj);
                        } else
                            productName.setText(holder.pname);

                        if (viewFlipper.getDisplayedChild() != 0) {
                            viewFlipper.showPrevious();

                        }
                    }
                });


                row.setTag(holder);
            } else {
                holder = (SubDStockCheckActivity.ViewHolder) row.getTag();
            }

            holder.productObj = product;

            try {
                holder.psname.setTextColor(product.getTextColor());
            } catch (Exception e) {
                Commons.printException(e);
                holder.psname.setTextColor(ContextCompat.getColor(getApplicationContext(),
                        android.R.color.black));

            }

            if (bmodel.configurationMasterHelper.IS_PRODUCT_DISPLAY_FOR_PIRAMAL) {
                //for piramal
                try {
                    holder.psname.setTextColor(Color.parseColor(product.getColorCode()));
                } catch (Exception e) {
                    Commons.printException(e);
                    holder.psname.setTextColor(ContextCompat.getColor(getApplicationContext(),
                            android.R.color.black));

                }
            }


            holder.psname.setText(holder.productObj.getProductShortName());
            holder.pname = holder.productObj.getProductName();

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

            if (holder.productObj.getLocations()
                    .get(mSelectedLocationIndex).getAvailability() == 1) {
                holder.imageButton_availability.setChecked(true);
                holder.imageButton_availability.setSupportButtonTintList(ColorStateList.valueOf(ContextCompat.getColor(SubDStockCheckActivity.this, R.color.green_productivity)));

            } else if (holder.productObj.getLocations()
                    .get(mSelectedLocationIndex).getAvailability() == 0) {
                holder.imageButton_availability.setChecked(true);
                holder.imageButton_availability.setSupportButtonTintList(ColorStateList.valueOf(ContextCompat.getColor(SubDStockCheckActivity.this, R.color.RED)));
            } else if (holder.productObj.getLocations()
                    .get(mSelectedLocationIndex).getAvailability() == -1) {
                holder.imageButton_availability.setChecked(false);
                holder.imageButton_availability.setSupportButtonTintList(ColorStateList.valueOf(ContextCompat.getColor(SubDStockCheckActivity.this, R.color.checkbox_default_color)));
            }

            if (holder.productObj.getOuUomid() == 0 || !holder.productObj.isOuterMapped()) {
                holder.shelfouter.setEnabled(false);
            } else {
                holder.shelfouter.setEnabled(true);
            }
            if (holder.productObj.getCaseUomId() == 0 || !holder.productObj.isCaseMapped()) {
                holder.shelfCaseQty.setEnabled(false);
            } else {
                holder.shelfCaseQty.setEnabled(true);
            }
            if (holder.productObj.getPcUomid() == 0 || !holder.productObj.isPieceMapped()) {
                holder.shelfPcsQty.setEnabled(false);
            } else {
                holder.shelfPcsQty.setEnabled(true);
            }

            return row;
        }
    }

    class ViewHolder {
        private AppCompatCheckBox imageButton_availability;
        private String pname;
        private ProductMasterBO productObj;
        private TextView psname;
        private EditText shelfPcsQty;
        private EditText shelfCaseQty;
        private EditText shelfouter;
        private TextView total;
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
            int totalAllQty = 0;
            int lpccount = 0;
            double totalvalue = 0;
            HashSet<String> sbdAcheived = new HashSet<>();
            HashSet<String> sbdStockAchieved = new HashSet<>();
            HashSet<String> sbdStkAndOrderAchieved = new HashSet<>();

            Vector<ProductMasterBO> items = productList;
            if (items == null) {
                return;
            }
            int siz = items.size();
            if (siz == 0)
                return;
            double temp;
            for (int i = 0; i < siz; i++) {
                ProductMasterBO ret = items.elementAt(i);
                if (ret.getOrderedPcsQty() != 0 || ret.getOrderedCaseQty() != 0
                        || ret.getOrderedOuterQty() != 0) {
                    lpccount = lpccount + 1;
                    temp = (ret.getOrderedPcsQty() * ret.getSrp())
                            + (ret.getOrderedCaseQty() * ret.getCsrp())
                            + ret.getOrderedOuterQty() * ret.getOsrp();
                    totalvalue = totalvalue + temp;

                    totalAllQty = totalAllQty + (ret.getOrderedPcsQty() + (ret.getOrderedCaseQty() * ret.getCaseSize()) + (ret.getOrderedOuterQty() * ret.getOutersize()));
                }
                if (ret.isRPS()) {
                    int size = ret.getLocations().size();
                    for (int j = 0; j < size; j++) {
                        if (ret.getLocations().get(j).getWHCase() > 0
                                || ret.getLocations().get(j).getWHOuter() > 0
                                || ret.getLocations().get(j).getWHPiece() > 0
                                || ret.getLocations().get(j).getShelfCase() > -1
                                || ret.getLocations().get(j).getShelfOuter() > -1
                                || ret.getLocations().get(j).getShelfPiece() > -1
                                || ret.getLocations().get(j).getAvailability() > -1) {

                            sbdStockAchieved.add(ret.getSbdGroupName());
                        }


                        if (ret.getLocations().get(j).getWHCase() > 0
                                || ret.getLocations().get(j).getWHOuter() > 0
                                || ret.getLocations().get(j).getWHPiece() > 0
                                || ret.getLocations().get(j).getShelfCase() > -1
                                || ret.getLocations().get(j)
                                .getShelfOuter() > -1
                                || ret.getLocations().get(j)
                                .getShelfPiece() > -1
                                || ret.getLocations().get(j).getAvailability() > -1
                                || ret.isSBDAcheived()
                                || ret.isSBDAcheivedLocal()) {
                            sbdStkAndOrderAchieved.add(ret
                                    .getSbdGroupName());
                        }
                        if (ret.isSBDAcheived() || ret.isSBDAcheivedLocal()) {
                            sbdAcheived.add(ret.getSbdGroupName());
                        }

                    }
                }
            }

            SbdDistPre = sbdStockAchieved.size();

            sbdDistAchieved = sbdAcheived.size();


            String strLpcCouunt = lpccount + "";
            lpcText.setText(strLpcCouunt);

            if (bmodel.configurationMasterHelper.HIDE_ORDER_DIST) {
                findViewById(R.id.ll_dist).setVisibility(View.GONE);
            }

            if (!bmodel.configurationMasterHelper.SHOW_LPC_ORDER) {
                findViewById(R.id.ll_lpc).setVisibility(View.GONE);
            }

            ((TextView) findViewById(R.id.lpc_title)).setTypeface(FontUtils.getFontRoboto(SubDStockCheckActivity.this, FontUtils.FontType.LIGHT));
            ((TextView) findViewById(R.id.lcp)).setTypeface(FontUtils.getFontRoboto(SubDStockCheckActivity.this, FontUtils.FontType.LIGHT));

        } catch (Exception e) {
            Commons.printException(e + "");
        }
    }

    public void onClick(View v) {
        Button vw = (Button) v;
        bmodel = (BusinessModel) getApplicationContext();
        bmodel.setContext(this);
        if (vw == mBtn_Search) {
            viewFlipper.showNext();

        } else if (vw == mBtnFilterPopup) {
            AlertDialog.Builder builderSingle = new AlertDialog.Builder(
                    SubDStockCheckActivity.this);
            final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(
                    SubDStockCheckActivity.this,
                    android.R.layout.select_dialog_singlechoice,
                    mSearchTypeArray);
            builderSingle.setAdapter(arrayAdapter,
                    new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            bmodel.setProductFilter(arrayAdapter.getItem(which));
                        }
                    });
            int selectedFiltPos = mSearchTypeArray.indexOf(bmodel
                    .getProductFilter());
            builderSingle.setSingleChoiceItems(arrayAdapter, selectedFiltPos,
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            bmodel.setProductFilter(arrayAdapter.getItem(which));
                        }

                    });
            builderSingle.setPositiveButton(
                    getResources().getString(R.string.ok),
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog,
                                            int whichButton) {
                        }
                    });
            bmodel.applyAlertDialogTheme(builderSingle);

        } else if (vw == mBtn_clear) {
            viewFlipper.showPrevious();
            mEdt_searchproductName.setText("");
            productName.setText("");
            try {
                InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
            } catch (Exception e) {
                Commons.printException(e);
            }
            loadProductList();

        } else if (vw == mBtnNext) {

            nextButtonClick();

        } else if (vw == mBtnGuidedSelling_next) {
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

    private void onnext() {
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
                new SubDStockCheckActivity.calculateReturnProductValusAndQty().execute();
            } else {
                nextButtonClick();
            }
        }
    }

    private void nextButtonClick() {
        try {
            if (hasStockOnly()) {
                new SaveStock().execute();
            } else
                bmodel.showAlert(
                        getResources().getString(
                                R.string.no_items_added), 0);

        } catch (Exception e) {
            Commons.printException(e + "");
        }
    }

    private void backButtonClick() {
        try {
            if (bmodel.hasOrder()) {
                showDialog(0);
            } else {
                bmodel.productHelper.clearOrderTable();

                bmodel.outletTimeStampHelper
                        .updateTimeStampModuleWise(DateTimeUtils.now(DateTimeUtils.TIME));
                startActivity(new Intent(SubDStockCheckActivity.this,
                        SubDHomeActivity.class));
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

                        startActivity(new Intent(
                                SubDStockCheckActivity.this,
                                SubDHomeActivity.class));
                        overridePendingTransition(R.anim.trans_right_in, R.anim.trans_right_out);
                        finish();
                    }
                }, new CommonDialog.negativeOnClickListener() {
                    @Override
                    public void onNegativeButtonClick() {
                    }
                }).show();

                break;

            default:
                break;
        }
        return null;
    }

    class SaveStock extends AsyncTask<String, Integer, Boolean> {
        private AlertDialog.Builder builder;
        private AlertDialog alertDialog;

        @Override
        protected Boolean doInBackground(String... arg0) {
            try {

                if (bmodel.isOrderTaken() && bmodel.isEdit())
                    orderHelper.deleteOrder(getApplicationContext(), bmodel.getRetailerMasterBO().getRetailerID());

                if (bmodel.configurationMasterHelper.IS_COMBINED_STOCK_CHECK_FROM_ORDER) {
                    // save price check
                    PriceTrackingHelper priceTrackingHelper = PriceTrackingHelper.getInstance(SubDStockCheckActivity.this);
                    if (bmodel.configurationMasterHelper.SHOW_PRICECHECK_IN_STOCKCHECK)
                        priceTrackingHelper.savePriceTransaction(getApplicationContext(), mylist);

                    // save near expiry
                    stockCheckHelper.saveNearExpiry(getApplicationContext());
                }

                // Save closing stock
                stockCheckHelper.saveClosingStock(getApplicationContext(),true);

                bmodel.saveModuleCompletion("MENU_SUBD_STOCK", true);

                return Boolean.TRUE;
            } catch (Exception e) {
                Commons.printException(e + "");
                return Boolean.FALSE;
            }
        }

        protected void onPreExecute() {
            builder = new AlertDialog.Builder(SubDStockCheckActivity.this);

            customProgressDialog(builder, getResources().getString(R.string.saving));
            alertDialog = builder.create();
            alertDialog.show();
        }

        protected void onPostExecute(Boolean result) {
            // result is the value returned from doInBackground
            alertDialog.dismiss();
            if (result == Boolean.TRUE) {
                bmodel.outletTimeStampHelper.updateTimeStampModuleWise(DateTimeUtils.now(DateTimeUtils.TIME));
                startActivity(new Intent(
                        SubDStockCheckActivity.this,
                        SubDHomeActivity.class));
                overridePendingTransition(R.anim.trans_right_in, R.anim.trans_right_out);
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
                Button ed =  findViewById(vw.getId());
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
                refreshList();
                Bundle extras = data.getExtras();
                int holderPosition = extras.getInt("position", 0);
                int holderTop = extras.getInt("top", 0);
                if (mylist.size() > 0)
                    lvwplist.setSelectionFromTop(holderPosition, holderTop);
            }
        } else {
            if (result != null) {
                if (result.getContents() == null) {
                    Toast.makeText(this, "Cancelled", Toast.LENGTH_LONG).show();
                } else {
                    // Toast.makeText(this, "Scanned: " + result.getContents(), Toast.LENGTH_LONG).show();
                    strBarCodeSearch = result.getContents();
                    if (strBarCodeSearch != null && !"".equals(strBarCodeSearch)) {
                        bmodel.setProductFilter(getResources().getString(R.string.order_dialog_barcode));
                        mEdt_searchproductName.setText(strBarCodeSearch);
                        if (viewFlipper.getDisplayedChild() == 0) {
                            viewFlipper.showNext();
                        }
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

    private void loadProductList() {
        try {
            Vector<ProductMasterBO> items = productList;
            int siz = items.size();
            mylist = new Vector<>();
            for (int i = 0; i < siz; ++i) {
                ProductMasterBO ret = items.elementAt(i);
                if (bmodel.configurationMasterHelper.IS_LOAD_PRICE_GROUP_PRD_OLY && ret.getGroupid() == 0)
                    continue;
                if (!bmodel.configurationMasterHelper.IS_STOCK_AVAILABLE_PRODUCTS_ONLY
                        || (bmodel.configurationMasterHelper.IS_STOCK_AVAILABLE_PRODUCTS_ONLY && bmodel.getRetailerMasterBO().getIsVansales() == 1
                        && ret.getSIH() > 0)
                        || (bmodel.configurationMasterHelper.IS_STOCK_AVAILABLE_PRODUCTS_ONLY && bmodel.getRetailerMasterBO().getIsVansales() == 0 && ret.getWSIH() > 0)) {

                    if (!bmodel.configurationMasterHelper.IS_SHOW_ONLY_INDICATIVE_ORDER) {

                        if (ret.getIsSaleable() == 1) {
                            if (generalbutton.equals(GENERAL) && brandbutton.equals(BRAND))//No filters selected
                                mylist.add(ret);
                            else if (applyProductAndSpecialFilter(ret))
                                mylist.add(ret);
                        }
                    }
                }
            }

            if (bmodel.configurationMasterHelper.IS_PRODUCT_SEQUENCE_UNIPAL)
                getProductBySequence();


            mSchedule = new SubDStockCheckActivity.MyAdapter(mylist);
            lvwplist.setAdapter(mSchedule);
            updateOrderedCount();
            if (generalbutton.equals(GENERAL) && brandbutton.equals(BRAND)) {
                String strPname = getResources().getString(
                        R.string.product_name)
                        + " (" + mylist.size() + ")";
                pnametitle.setText(strPname);
                if (bmodel.configurationMasterHelper.SHOW_SPL_FILTER && !bmodel.configurationMasterHelper.IS_SPL_FILTER_TAB) {
                    if (totalOrdCount.equals("0"))
                        setScreenTitle(strPname);
                    else
                        setScreenTitle(totalOrdCount + "/" + strPname);
                }
            } else if (!generalbutton.equals(GENERAL)) {
                String strPname = getFilterName(generalbutton) + " ("
                        + mylist.size() + ")";
                pnametitle.setText(strPname);
                if (bmodel.configurationMasterHelper.SHOW_SPL_FILTER && !bmodel.configurationMasterHelper.IS_SPL_FILTER_TAB) {
                    if (totalOrdCount.equals("0"))
                        setScreenTitle(strPname);
                    else
                        setScreenTitle(totalOrdCount + "/" + strPname);
                }
            } else {
                String strPname = brandbutton + " (" + mylist.size() + ")";
                pnametitle.setText(strPname);
                if (bmodel.configurationMasterHelper.SHOW_SPL_FILTER && !bmodel.configurationMasterHelper.IS_SPL_FILTER_TAB) {
                    if (totalOrdCount.equals("0"))
                        setScreenTitle(strPname);
                    else
                        setScreenTitle(totalOrdCount + "/" + strPname);
                }
            }
        } catch (Exception e) {
            Commons.printException(e);
        }
    }

    private class SearchAsync extends
            AsyncTask<Integer, Integer, Boolean> {


        protected void onPreExecute() {

        }

        protected void onProgressUpdate(Integer... progress) {

        }

        @Override
        protected Boolean doInBackground(Integer... params) {
            loadSearchedList();

            return true;
        }

        protected void onPostExecute(Boolean result) {

            mSchedule = new MyAdapter(mylist);
            lvwplist.setAdapter(mSchedule);

        }
    }

    private void loadSearchedList() {
        Vector<ProductMasterBO> items = productList;
        if (items == null) {
            bmodel.showAlert(
                    getResources().getString(R.string.no_products_exists),
                    0);
            return;
        }
        int siz = items.size();
        mylist = new Vector<>();
        String mSelectedFilter = bmodel.getProductFilter();
        for (int i = 0; i < siz; ++i) {
            ProductMasterBO ret = items.elementAt(i);
            // For breaking search..
            if (searchAsync.isCancelled()) {
                break;
            }

            if (bmodel.configurationMasterHelper.IS_LOAD_PRICE_GROUP_PRD_OLY && ret.getGroupid() == 0)
                continue;

            if (!bmodel.configurationMasterHelper.IS_STOCK_AVAILABLE_PRODUCTS_ONLY
                    || (bmodel.configurationMasterHelper.IS_STOCK_AVAILABLE_PRODUCTS_ONLY && bmodel.getRetailerMasterBO().getIsVansales() == 1
                    && ret.getSIH() > 0 && bmodel.configurationMasterHelper.HAS_SELLER_TYPE_SELECTION_ENABLED)
                    || (bmodel.configurationMasterHelper.HAS_SELLER_TYPE_SELECTION_ENABLED && bmodel.getRetailerMasterBO().getIsVansales() == 0 && ret.getWSIH() > 0) ||
                    (bmodel.configurationMasterHelper.IS_STOCK_AVAILABLE_PRODUCTS_ONLY && bmodel.configurationMasterHelper.IS_INVOICE && ret.getSIH() > 0)) {
                if (!bmodel.configurationMasterHelper.IS_SHOW_ONLY_INDICATIVE_ORDER) {
                    if (mSelectedFilter.equals(getResources().getString(
                            R.string.order_dialog_barcode))) {

                        if (ret.getBarCode() != null
                                && (ret.getBarCode().toLowerCase()
                                .contains(mEdt_searchproductName.getText().toString().toLowerCase())
                                || ret.getCasebarcode().toLowerCase().
                                contains(mEdt_searchproductName.getText().toString().toLowerCase())
                                || ret.getOuterbarcode().toLowerCase().
                                contains(mEdt_searchproductName.getText().toString().toLowerCase())) && ret.getIsSaleable() == 1) {

                            if (generalbutton.equals(GENERAL) && brandbutton.equals(BRAND)) {//No filters selected
                                if (bmodel.configurationMasterHelper.IS_QTY_INCREASE) {
                                    if (mEdt_searchproductName.getText().toString().equals(ret.getBarCode())) {
                                        ret.setOrderedPcsQty(ret.getOrderedPcsQty() + 1);
                                    } else if (mEdt_searchproductName.getText().toString().equals(ret.getCasebarcode())) {
                                        ret.setOrderedCaseQty(ret.getOrderedCaseQty() + 1);
                                    } else if (mEdt_searchproductName.getText().toString().equals(ret.getOuterbarcode())) {
                                        ret.setOrderedOuterQty(ret.getOrderedOuterQty() + 1);
                                    }
                                }
                                mylist.add(ret);
                            } else if (applyProductAndSpecialFilter(ret)) {
                                if (bmodel.configurationMasterHelper.IS_QTY_INCREASE) {
                                    if (mEdt_searchproductName.getText().toString().equals(ret.getBarCode())) {
                                        ret.setOrderedPcsQty(ret.getOrderedPcsQty() + 1);
                                    } else if (mEdt_searchproductName.getText().toString().equals(ret.getCasebarcode())) {
                                        ret.setOrderedCaseQty(ret.getOrderedCaseQty() + 1);
                                    } else if (mEdt_searchproductName.getText().toString().equals(ret.getOuterbarcode())) {
                                        ret.setOrderedOuterQty(ret.getOrderedOuterQty() + 1);
                                    }
                                }
                                mylist.add(ret);
                            }
                        }
                    } else if (mSelectedFilter.equals(getResources().getString(
                            R.string.prod_code))) {
                        if (ret.getRField1() != null && ret.getRField1()
                                .toLowerCase()
                                .contains(
                                        mEdt_searchproductName.getText().toString()
                                                .toLowerCase()) && ret.getIsSaleable() == 1) {
                            if (generalbutton.equals(GENERAL) && brandbutton.equals(BRAND))//No filters selected
                                mylist.add(ret);
                            else if (applyProductAndSpecialFilter(ret))
                                mylist.add(ret);
                        }
                    } else if (mSelectedFilter.equals(getResources().getString(
                            R.string.product_name))) {
                        if (ret.getProductShortName() != null && ret.getProductShortName()
                                .toLowerCase()
                                .contains(
                                        mEdt_searchproductName.getText().toString()
                                                .toLowerCase()) && ret.getIsSaleable() == 1)
                            if (generalbutton.equals(GENERAL) && brandbutton.equals(BRAND))//No filters selected
                                mylist.add(ret);
                            else if (applyProductAndSpecialFilter(ret))
                                mylist.add(ret);
                    }
                }
            }
        }
        if (bmodel.configurationMasterHelper.IS_PRODUCT_SEQUENCE_UNIPAL)
            getProductBySequence();

    }

    private boolean applyProductAndSpecialFilter(ProductMasterBO ret) {
        if (!GENERAL.equals(generalbutton) && !BRAND.equals(brandbutton)) {
            // both filter selected
            return fiveFilter_productIDs != null && fiveFilter_productIDs.contains(ret.getProductID())
                    && isSpecialFilterAppliedProduct(generalbutton, ret);

        } else if (!GENERAL.equals(generalbutton) && BRAND.equals(brandbutton)) {
            //special filter alone selected
            return isSpecialFilterAppliedProduct(generalbutton, ret);
        } else if (GENERAL.equals(generalbutton) && !BRAND.equals(brandbutton)) {
            // product filter alone selected
            return fiveFilter_productIDs != null && fiveFilter_productIDs.contains(ret.getProductID());

        }
        return false;
    }

    private void getMandatoryFilters() {
        for (ConfigureBO bo : bmodel.configurationMasterHelper.getGenFilter()) {
            if (bo.getMandatory() == 1) {
                switch (bo.getConfigCode()) {
                    case mSbd:
                        isSbd = true;
                        break;
                    case mSbdGaps:
                        isSbdGaps = true;
                        break;
                    case mOrdered:
                        isOrdered = true;
                        break;
                    case mPurchased:
                        isPurchased = true;
                        break;
                    case mInitiative:
                        isInitiative = true;
                        break;
                    case mOnAllocation:
                        isOnAllocation = true;
                        break;
                    case mInStock:
                        isInStock = true;
                        break;
                    case mPromo:
                        isPromo = true;
                        break;
                    case mMustSell:
                        isMustSell = true;
                        break;
                    case mFocusBrand:
                        isFocusBrand = true;
                        break;
                    case mFocusBrand2:
                        isFocusBrand2 = true;
                        break;
                    case msih:
                        isSIH = true;
                        break;
                    case mOOS:
                        isOOS = true;
                        break;
                    case mNMustSell:
                        isNMustSell = true;
                        break;
                    case mDiscount:
                        isDiscount = true;
                        break;
                    case mStock:
                        isStock = true;
                        break;
                }
            }
        }
    }

    @Override
    public void updateBrandText(String mFilterText, int bid) {
        mSelectedBrandID = bid;
        mSelectedFiltertext = mFilterText;

        Commons.print("Stock and order  :," + " update brand text called :"
                + productList.size()
                + ">>>>>>>>>>>>>>>>>>>>>" + bid);

        try {
            // Close the drawer
            mDrawerLayout.closeDrawers();

            // Change the Brand button Name
            brandbutton = mFilterText;

            // Consider generalbutton text if it is dependent filter.
            String generaltxt = generalbutton;

            // Clear the productName
            productName.setText("");

            Vector<ProductMasterBO> items = productList;
            if (items == null) {
                bmodel.showAlert(
                        getResources().getString(R.string.no_products_exists),
                        0);
                return;
            }

            int siz = items.size();
            mylist = new Vector<>();

            pnametitle = (TextView) findViewById(R.id.productnametitle);
            ((TextView) findViewById(R.id.productnametitle)).setTypeface(FontUtils.getFontRoboto(SubDStockCheckActivity.this, FontUtils.FontType.MEDIUM));


            // Add the products into list
            for (int i = 0; i < siz; ++i) {
                ProductMasterBO ret = items.elementAt(i);
                if (ret.getBarCode().equals(strBarCodeSearch)
                        || ret.getCasebarcode().equals(strBarCodeSearch)
                        || ret.getOuterbarcode().equals(strBarCodeSearch)
                        || "ALL".equals(strBarCodeSearch)) {
                    if (bmodel.configurationMasterHelper.IS_LOAD_PRICE_GROUP_PRD_OLY && ret.getGroupid() == 0)
                        continue;

                    if (!bmodel.configurationMasterHelper.IS_STOCK_AVAILABLE_PRODUCTS_ONLY
                            || (bmodel.configurationMasterHelper.IS_STOCK_AVAILABLE_PRODUCTS_ONLY && bmodel.getRetailerMasterBO().getIsVansales() == 1
                            && ret.getSIH() > 0 && bmodel.configurationMasterHelper.HAS_SELLER_TYPE_SELECTION_ENABLED)
                            || (bmodel.configurationMasterHelper.HAS_SELLER_TYPE_SELECTION_ENABLED && bmodel.getRetailerMasterBO().getIsVansales() == 0 && ret.getWSIH() > 0)
                            || (bmodel.configurationMasterHelper.IS_STOCK_AVAILABLE_PRODUCTS_ONLY && bmodel.configurationMasterHelper.IS_INVOICE && ret.getSIH() > 0)) {

                        if (!bmodel.configurationMasterHelper.IS_SHOW_ONLY_INDICATIVE_ORDER) {
                            if ((bid == -1 || bid == ret.getParentid()) && GENERAL.equalsIgnoreCase(generaltxt) && ret.getIsSaleable() == 1) {
                                // product filter alone
                                if (mEdt_searchproductName.getText().length() >= 3) {
                                    if (isUserEntryFilterSatisfied(ret)) {
                                        if (bmodel.configurationMasterHelper.IS_QTY_INCREASE) {
                                            if (strBarCodeSearch.equals(ret.getBarCode())) {
                                                ret.setOrderedPcsQty(ret.getOrderedPcsQty() + 1);
                                            } else if (strBarCodeSearch.equals(ret.getCasebarcode())) {
                                                ret.setOrderedCaseQty(ret.getOrderedCaseQty() + 1);
                                            } else if (strBarCodeSearch.equals(ret.getOuterbarcode())) {
                                                ret.setOrderedOuterQty(ret.getOrderedOuterQty() + 1);
                                            }
                                        }
                                        mylist.add(ret);
                                    }
                                } else {
                                    if (bmodel.configurationMasterHelper.IS_QTY_INCREASE) {
                                        if (strBarCodeSearch.equals(ret.getBarCode())) {
                                            ret.setOrderedPcsQty(ret.getOrderedPcsQty() + 1);
                                        } else if (strBarCodeSearch.equals(ret.getCasebarcode())) {
                                            ret.setOrderedCaseQty(ret.getOrderedCaseQty() + 1);
                                        } else if (strBarCodeSearch.equals(ret.getOuterbarcode())) {
                                            ret.setOrderedOuterQty(ret.getOrderedOuterQty() + 1);
                                        }
                                    }
                                    mylist.add(ret);
                                }
                            } else if ((bid == -1 || bid == ret.getParentid()) && !GENERAL.equalsIgnoreCase(generaltxt) && ret.getIsSaleable() == 1) {
                                //special(GENERAL) filter with or without product filter
                                if (isSpecialFilterAppliedProduct(generaltxt, ret)) {
                                    if (mEdt_searchproductName.getText().length() >= 3) {
                                        if (isUserEntryFilterSatisfied(ret)) {
                                            if (bmodel.configurationMasterHelper.IS_QTY_INCREASE) {
                                                if (strBarCodeSearch.equals(ret.getBarCode())) {
                                                    ret.setOrderedPcsQty(ret.getOrderedPcsQty() + 1);
                                                } else if (strBarCodeSearch.equals(ret.getCasebarcode())) {
                                                    ret.setOrderedCaseQty(ret.getOrderedCaseQty() + 1);
                                                } else if (strBarCodeSearch.equals(ret.getOuterbarcode())) {
                                                    ret.setOrderedOuterQty(ret.getOrderedOuterQty() + 1);
                                                }
                                            }
                                            mylist.add(ret);
                                        }
                                    } else {
                                        if (bmodel.configurationMasterHelper.IS_QTY_INCREASE) {
                                            if (strBarCodeSearch.equals(ret.getBarCode())) {
                                                ret.setOrderedPcsQty(ret.getOrderedPcsQty() + 1);
                                            } else if (strBarCodeSearch.equals(ret.getCasebarcode())) {
                                                ret.setOrderedCaseQty(ret.getOrderedCaseQty() + 1);
                                            } else if (strBarCodeSearch.equals(ret.getOuterbarcode())) {
                                                ret.setOrderedOuterQty(ret.getOrderedOuterQty() + 1);
                                            }
                                        }
                                        mylist.add(ret);
                                    }
                                }

                            }
                        }
                    }
                }
            }

            updateOrderedCount();
            if (GENERAL.equalsIgnoreCase(generaltxt) && BRAND.equals(mFilterText)) {
                String strPname = getResources().getString(
                        R.string.product_name)
                        + " (" + mylist.size() + ")";
                pnametitle.setText(strPname);

                if (bmodel.configurationMasterHelper.SHOW_SPL_FILTER && !bmodel.configurationMasterHelper.IS_SPL_FILTER_TAB) {
                    if (totalOrdCount.equals("0"))
                        setScreenTitle(title + " (" + mylist.size() + ")");
                    else
                        setScreenTitle(title + " (" + totalOrdCount + "/" + mylist.size() + ")");
                }
            } else if (!GENERAL.equalsIgnoreCase(generaltxt)) {
                String strPname = getFilterName(generaltxt) + " ("
                        + mylist.size() + ")";
                pnametitle.setText(strPname);

                if (bmodel.configurationMasterHelper.SHOW_SPL_FILTER && !bmodel.configurationMasterHelper.IS_SPL_FILTER_TAB) {
                    if (totalOrdCount.equals("0"))
                        setScreenTitle(strPname);
                    else
                        setScreenTitle(totalOrdCount + "/" + strPname);
                }
            } else {
                String strPname = mFilterText + " (" + mylist.size() + ")";
                pnametitle.setText(strPname);

                if (bmodel.configurationMasterHelper.SHOW_SPL_FILTER && !bmodel.configurationMasterHelper.IS_SPL_FILTER_TAB) {
                    if (totalOrdCount.equals("0"))
                        setScreenTitle(strPname);
                    else
                        setScreenTitle(totalOrdCount + "/" + strPname);
                }
            }
            if (bmodel.configurationMasterHelper.IS_PRODUCT_SEQUENCE_UNIPAL)
                getProductBySequence();

            // set the new list to listview
            mSchedule = new SubDStockCheckActivity.MyAdapter(mylist);
            lvwplist.setAdapter(mSchedule);

            strBarCodeSearch = "ALL";
            updateValue();
        } catch (Exception e) {
            Commons.printException(e);
        }
    }

    private boolean isUserEntryFilterSatisfied(ProductMasterBO ret) {
        String mSelectedFilter = bmodel.getProductFilter();
        if (getResources().getString(
                R.string.order_dialog_barcode).equals(mSelectedFilter)) {
            return ret.getBarCode() != null
                    && (ret.getBarCode().toLowerCase()
                    .contains(mEdt_searchproductName.getText().toString().toLowerCase())
                    || ret.getCasebarcode().toLowerCase().
                    contains(mEdt_searchproductName.getText().toString().toLowerCase())
                    || ret.getOuterbarcode().toLowerCase().
                    contains(mEdt_searchproductName.getText().toString().toLowerCase()) && ret.getIsSaleable() == 1);
        } else if (getResources().getString(
                R.string.prod_code).equals(mSelectedFilter)) {
            return ret.getRField1() != null && ret.getRField1()
                    .toLowerCase()
                    .contains(
                            mEdt_searchproductName.getText().toString()
                                    .toLowerCase()) && ret.getIsSaleable() == 1;

        } else if (getResources().getString(
                R.string.product_name).equals(mSelectedFilter)) {
            return ret.getProductShortName() != null && ret.getProductShortName()
                    .toLowerCase()
                    .contains(
                            mEdt_searchproductName.getText().toString()
                                    .toLowerCase()) && ret.getIsSaleable() == 1;
        }
        return false;
    }

    private boolean isSpecialFilterAppliedProduct(String generaltxt, ProductMasterBO ret) {
        final String mCommon = "Filt01";
        final String mCompertior = "Filt23";
        final String mFocusBrand3 = "Filt20";
        final String mFocusBrand4 = "Filt21";
        final String mSMP = "Filt22";
        final String mNearExpiryTag = "Filt19";
        final String mShelf = "Filt24";

        return generaltxt.equalsIgnoreCase(mSbd) && ret.isRPS()
                || (generaltxt.equalsIgnoreCase(mOrdered) && (ret.getOrderedPcsQty() > 0 || ret.getOrderedCaseQty() > 0 || ret.getOrderedOuterQty() > 0))
                || (generaltxt.equalsIgnoreCase(mPurchased) && ret.getIsPurchased() == 1)
                || (generaltxt.equalsIgnoreCase(mInitiative) && ret.getIsInitiativeProduct() == 1)
                || (generaltxt.equalsIgnoreCase(mCommon) && applyCommonFilterConfig(ret))
                || (generaltxt.equalsIgnoreCase(mSbdGaps) && (ret.isRPS() && !ret.isSBDAcheived()))
                || (generaltxt.equalsIgnoreCase(mInStock) && ret.getWSIH() > 0)
                || (generaltxt.equalsIgnoreCase(mOnAllocation) && ret.isAllocation() == 1 && bmodel.configurationMasterHelper.IS_SIH_VALIDATION)
                || (generaltxt.equalsIgnoreCase(mPromo) && ret.isPromo())
                || (generaltxt.equalsIgnoreCase(mMustSell) && ret.getIsMustSell() == 1)
                || (generaltxt.equalsIgnoreCase(mFocusBrand) && ret.getIsFocusBrand() == 1)
                || (generaltxt.equalsIgnoreCase(mFocusBrand2) && ret.getIsFocusBrand2() == 1)
                || (generaltxt.equalsIgnoreCase(msih) && ret.getSIH() > 0)
                || (generaltxt.equalsIgnoreCase(mOOS) && ret.getOos() == 0)
                || (generaltxt.equalsIgnoreCase(mNMustSell) && ret.getIsNMustSell() == 1)
                || (generaltxt.equalsIgnoreCase(mDiscount) && ret.getIsDiscountable() == 1)
                || (generaltxt.equalsIgnoreCase(mStock) && (ret.getLocations().get(mSelectedLocationIndex).getShelfPiece() > -1 || ret.getLocations().get(mSelectedLocationIndex).getShelfCase() > -1 || ret.getLocations().get(mSelectedLocationIndex).getShelfOuter() > -1 || ret.getLocations().get(mSelectedLocationIndex).getWHPiece() > 0 || ret.getLocations().get(mSelectedLocationIndex).getWHCase() > 0 || ret.getLocations().get(mSelectedLocationIndex).getWHOuter() > 0 || ret.getLocations().get(mSelectedLocationIndex).getAvailability() > -1))
                || (generaltxt.equalsIgnoreCase(mNearExpiryTag) && ret.getIsNearExpiryTaggedProduct() == 1)
                || (generaltxt.equalsIgnoreCase(mFocusBrand3) && ret.getIsFocusBrand3() == 1)
                || (generaltxt.equalsIgnoreCase(mFocusBrand4) && ret.getIsFocusBrand4() == 1)
                || (generaltxt.equalsIgnoreCase(mSMP) && ret.getIsSMP() == 1)
                || (generaltxt.equalsIgnoreCase(mCompertior) && ret.getOwn() == 0)
                || (generaltxt.equalsIgnoreCase(mShelf) && (ret.getLocations().get(mSelectedLocationIndex).getShelfCase() > -1 || ret.getLocations().get(mSelectedLocationIndex).getShelfPiece() > -1 || ret.getLocations().get(mSelectedLocationIndex).getShelfOuter() > -1 || ret.getLocations().get(mSelectedLocationIndex).getAvailability() > -1))
                || (generaltxt.equalsIgnoreCase(mSuggestedOrder) && ret.getSoInventory() > 0);
    }

    private String getFilterName(String filtername) {
        Vector<ConfigureBO> genfilter = bmodel.configurationMasterHelper
                .getGenFilter();
        for (int i = 0; i < genfilter.size(); i++) {
            if (genfilter.get(i).getConfigCode().equals(filtername))
                filtername = genfilter.get(i).getMenuName();
        }
        return filtername;
    }

    private String getDefaultFilter() {
        String defaultfilter = "";
        try {
            Vector<ConfigureBO> genfilter = bmodel.configurationMasterHelper
                    .getGenFilter();
            for (int i = 0; i < genfilter.size(); i++) {
                if (genfilter.get(i).getHasLink() == 1) {
                    if (!bmodel.configurationMasterHelper.HAS_SELLER_TYPE_SELECTION_ENABLED) {
                        defaultfilter = genfilter.get(i).getConfigCode();
                        break;
                    } else {
                        if (bmodel.getRetailerMasterBO().getIsVansales() == 1) {
                            if (genfilter.get(i).getConfigCode().equals("Filt13")) {
                                defaultfilter = genfilter.get(i).getConfigCode();
                                break;
                            } else if (!genfilter.get(i).getConfigCode().equals("Filt08")) {
                                defaultfilter = genfilter.get(i).getConfigCode();
                                break;
                            }
                        } else {
                            if (genfilter.get(i).getConfigCode().equals("Filt08")) {
                                defaultfilter = genfilter.get(i).getConfigCode();
                                break;
                            } else if (!genfilter.get(i).getConfigCode().equals("Filt13")) {
                                defaultfilter = genfilter.get(i).getConfigCode();
                                break;
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            Commons.printException(e + "");
        }
        return defaultfilter;
    }


    @Override
    public void updateGeneralText(String mFilterText) {
        // set the spl filter name on the button for display
        generalbutton = mFilterText;

        // clearing fivefilterList
        fiveFilter_productIDs = null;
        if (mSelectedIdByLevelId != null)
            mSelectedIdByLevelId.clear();
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
                sequence = new Vector<>();
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
                Vector<LevelBO> filterValues = new Vector<>(loadedFilterValues.get(levelID));
                filterAdapter = new SubDStockCheckActivity.FilterAdapter(filterValues);
                rvFilterList.setAdapter(filterAdapter);
            }
        } else {
            rvFilterList.setVisibility(View.GONE);
        }

        title = bmodel.configurationMasterHelper
                .getHomescreentwomenutitle("MENU_SUBD_STOCK");

        updateBrandText(BRAND, -1);

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
        if (!generalbutton.equals(GENERAL))
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
        if (screenCode.equals(ConfigurationMasterHelper.MENU_ORDER))
            menu.findItem(R.id.menu_loc_filter).setVisible(false);

        menu.findItem(R.id.menu_survey).setVisible(bmodel.configurationMasterHelper.floating_Survey);
        menu.findItem(R.id.menu_reason).setVisible(bmodel.configurationMasterHelper.floating_np_reason_photo);
        menu.findItem(R.id.menu_barcode).setVisible(bmodel.configurationMasterHelper.IS_BAR_CODE);

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
                    bmodel.productHelper.getRetailerModuleSequenceValues(),false));
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
                    new SubDStockCheckActivity.calculateReturnProductValusAndQty().execute();
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
                        bmodel.saveModuleCompletion("MENU_SUBD_STOCK", true);
                        bmodel.outletTimeStampHelper
                                .updateTimeStampModuleWise(DateTimeUtils.now(DateTimeUtils.TIME));
                        startActivity(new Intent(SubDStockCheckActivity.this,
                                SubDHomeActivity.class));
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
                generalbutton = GENERAL;
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
                    if (pi.packageName.toLowerCase().contains("calcul")) {
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
                        updateBrandText(mSelectedFiltertext, mSelectedBrandID);
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
            bundle.putBoolean("isTag", true);

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
        super.onDestroy();
        unbindDrawables(findViewById(R.id.root));
    }

    /**
     * this would clear all the resources used of the layout.
     *
     * @param view view
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

    @Override
    public boolean onEditorAction(TextView arg0, int arg1, KeyEvent arg2) {
        if (arg1 == EditorInfo.IME_ACTION_DONE) {
            if (mEdt_searchproductName.getText().length() >= 3) {
                searchAsync = new SearchAsync();
                searchAsync.execute();
            } else {
                Toast.makeText(this, "Enter atleast 3 letters.", Toast.LENGTH_SHORT)
                        .show();
            }
            return true;
        }
        return false;
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

            updateBrandText(BRAND, -1);
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

    private boolean applyCommonFilterConfig(ProductMasterBO ret) {
        return (isSbd && ret.isRPS()) || (isSbdGaps && ret.isRPS() && !ret.isSBDAcheived()) || (isOrdered && (ret.getOrderedPcsQty() > 0 || ret.getOrderedCaseQty() > 0 || ret.getOrderedOuterQty() > 0))
                || (isPurchased && ret.getIsPurchased() == 1) || (isInitiative && ret.getIsInitiativeProduct() == 1) || (isOnAllocation && ret.isAllocation() == 1
                && bmodel.configurationMasterHelper.IS_SIH_VALIDATION) || (isInStock && ret.getWSIH() > 0) || (isPromo && ret.isPromo()) || (isMustSell && ret.getIsMustSell() == 1)
                || (isFocusBrand && ret.getIsFocusBrand() == 1) || (isFocusBrand2 && ret.getIsFocusBrand2() == 1) || (isSIH && ret.getSIH() > 0) || (isOOS && ret.getOos() == 0)
                || (isNMustSell && ret.getIsNMustSell() == 1) || (isStock && (ret.getLocations().get(mSelectedLocationIndex).getShelfPiece() > -1
                || ret.getLocations().get(mSelectedLocationIndex).getShelfCase() > -1 || ret.getLocations().get(mSelectedLocationIndex).getShelfOuter() > -1 || ret.getLocations().get(mSelectedLocationIndex).getWHPiece() > 0
                || ret.getLocations().get(mSelectedLocationIndex).getWHCase() > 0 || ret.getLocations().get(mSelectedLocationIndex).getWHOuter() > 0 || ret.getLocations().get(mSelectedLocationIndex).getAvailability() > -1)) || (isDiscount && ret.getIsDiscountable() == 1);
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
        private AlertDialog.Builder builder;
        private AlertDialog alertDialog;

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
            builder = new AlertDialog.Builder(SubDStockCheckActivity.this);

            customProgressDialog(builder, getResources().getString(R.string.loading));
            alertDialog = builder.create();
            alertDialog.show();
        }

        protected void onPostExecute(Boolean result) {
            // result is the value returned from doInBackground
            alertDialog.dismiss();
            nextButtonClick();
        }
    }

    /**
     * @param product
     * @return
     * @author rajesh.k update stock total value in lisview
     */
    private int getProductTotalValue(ProductMasterBO product) {
        int totalQty = 0;
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

            if (product.getLocations().get(i).getAvailability() > -1)
                totalQty += product.getLocations().get(i).getAvailability();

            totalQty += product.getLocations().get(i).getWHPiece();
            totalQty += (product.getLocations().get(i).getWHCase() * product
                    .getCaseSize());
            totalQty += (product.getLocations().get(i).getWHOuter() * product
                    .getOutersize());
        }
        return totalQty;
    }

    @Override
    public void updateFromFiveLevelFilter(int mFilteredPid, HashMap<Integer, Integer> mSelectedIdByLevelId, ArrayList<Integer> mAttributeProducts, String mFilterText) {
        // 22.11.2017 mansoor.k mFilterText length == 0 then no filter selected so no need to loop parent ids loop
        String filtertext = getResources().getString(R.string.product_name);
        if (!mFilterText.equals("")) {
            filtertext = mFilterText;
            mSelectedFiltertext = mFilterText;
        } else
            mSelectedFiltertext = BRAND;

        brandbutton = filtertext;
        fiveFilter_productIDs = new ArrayList<>();

        int count = 0;
        mylist = new Vector<>();
        Vector<ProductMasterBO> items = productList;
        if (mAttributeProducts != null) {
            count = 0;
            if (mFilteredPid != 0) {
                if (mFilterText.length() > 0) {
                    count++;
                    for (ProductMasterBO productBO : items) {
                        if (!bmodel.configurationMasterHelper.IS_STOCK_AVAILABLE_PRODUCTS_ONLY
                                || (bmodel.configurationMasterHelper.IS_STOCK_AVAILABLE_PRODUCTS_ONLY && bmodel.getRetailerMasterBO().getIsVansales() == 1
                                && productBO.getSIH() > 0)
                                || (bmodel.configurationMasterHelper.HAS_SELLER_TYPE_SELECTION_ENABLED && bmodel.getRetailerMasterBO().getIsVansales() == 0 && productBO.getWSIH() > 0) ||
                                (bmodel.configurationMasterHelper.IS_STOCK_AVAILABLE_PRODUCTS_ONLY && bmodel.configurationMasterHelper.IS_INVOICE && productBO.getSIH() > 0)) {

                            if (!bmodel.configurationMasterHelper.IS_SHOW_ONLY_INDICATIVE_ORDER) {

                                if (productBO.getIsSaleable() == 1 && productBO.getParentHierarchy().contains("/" + mFilteredPid + "/")) {
                                    // here we get all products mapped to parent id list, then that product will be added only if it is mapped to selected attribute
                                    if (mAttributeProducts.contains(SDUtil.convertToInt(productBO.getProductID()))) {

                                        if (bmodel.configurationMasterHelper.IS_LOAD_PRICE_GROUP_PRD_OLY && productBO.getGroupid() == 0)
                                            continue;
                                        mylist.add(productBO);
                                        fiveFilter_productIDs.add(productBO.getProductID());
                                    }
                                }
                            }
                        }

                    }
                } else {
                    for (ProductMasterBO productBO : items) {
                        if (!bmodel.configurationMasterHelper.IS_STOCK_AVAILABLE_PRODUCTS_ONLY
                                || (bmodel.configurationMasterHelper.IS_STOCK_AVAILABLE_PRODUCTS_ONLY && bmodel.getRetailerMasterBO().getIsVansales() == 1
                                && productBO.getSIH() > 0)
                                || (bmodel.configurationMasterHelper.HAS_SELLER_TYPE_SELECTION_ENABLED && bmodel.getRetailerMasterBO().getIsVansales() == 0 && productBO.getWSIH() > 0) ||
                                (bmodel.configurationMasterHelper.IS_STOCK_AVAILABLE_PRODUCTS_ONLY && bmodel.configurationMasterHelper.IS_INVOICE && productBO.getSIH() > 0)) {

                            if (!bmodel.configurationMasterHelper.IS_SHOW_ONLY_INDICATIVE_ORDER) {

                                if (productBO.getIsSaleable() == 1) {
                                    // here we get all products mapped to parent id list, then that product will be added only if it is mapped to selected attribute
                                    if (mAttributeProducts.contains(SDUtil.convertToInt(productBO.getProductID()))) {

                                        if (bmodel.configurationMasterHelper.IS_LOAD_PRICE_GROUP_PRD_OLY && productBO.getGroupid() == 0)
                                            continue;
                                        mylist.add(productBO);
                                        fiveFilter_productIDs.add(productBO.getProductID());
                                    }
                                }
                            }
                        }

                    }
                }
            } else {
                for (int pid : mAttributeProducts) {
                    for (ProductMasterBO productBO : items) {
                        if (!bmodel.configurationMasterHelper.IS_STOCK_AVAILABLE_PRODUCTS_ONLY
                                || (bmodel.configurationMasterHelper.IS_STOCK_AVAILABLE_PRODUCTS_ONLY && bmodel.getRetailerMasterBO().getIsVansales() == 1
                                && productBO.getSIH() > 0)
                                || (bmodel.configurationMasterHelper.HAS_SELLER_TYPE_SELECTION_ENABLED && bmodel.getRetailerMasterBO().getIsVansales() == 0 && productBO.getWSIH() > 0) ||
                                (bmodel.configurationMasterHelper.IS_STOCK_AVAILABLE_PRODUCTS_ONLY && bmodel.configurationMasterHelper.IS_INVOICE && productBO.getSIH() > 0)) {


                            if (!bmodel.configurationMasterHelper.IS_SHOW_ONLY_INDICATIVE_ORDER) {
                                if (pid == SDUtil.convertToInt(productBO.getProductID()) && productBO.getIsSaleable() == 1) {
                                    if (bmodel.configurationMasterHelper.IS_LOAD_PRICE_GROUP_PRD_OLY && productBO.getGroupid() == 0)
                                        continue;
                                    mylist.add(productBO);
                                    fiveFilter_productIDs.add(productBO.getProductID());
                                }
                            }
                        }
                    }
                }
            }
        } else {
            if (mFilterText.length() > 0) {
                count++;
                for (ProductMasterBO productBO : items) {

                    if (!bmodel.configurationMasterHelper.IS_STOCK_AVAILABLE_PRODUCTS_ONLY
                            || (bmodel.configurationMasterHelper.IS_STOCK_AVAILABLE_PRODUCTS_ONLY
                            && bmodel.getRetailerMasterBO().getIsVansales() == 1
                            && productBO.getSIH() > 0)
                            || (bmodel.configurationMasterHelper.HAS_SELLER_TYPE_SELECTION_ENABLED
                            && bmodel.getRetailerMasterBO().getIsVansales() == 0 && productBO.getWSIH() > 0)
                            || (bmodel.configurationMasterHelper.IS_STOCK_AVAILABLE_PRODUCTS_ONLY
                            && bmodel.configurationMasterHelper.IS_INVOICE
                            && productBO.getSIH() > 0)) {

                        if (!bmodel.configurationMasterHelper.IS_SHOW_ONLY_INDICATIVE_ORDER
                                ) {
                            if (productBO.getIsSaleable() == 1 && productBO.getParentHierarchy().contains("/" + mFilteredPid + "/")) {
                                if (bmodel.configurationMasterHelper.IS_LOAD_PRICE_GROUP_PRD_OLY && productBO.getGroupid() == 0)
                                    continue;
                                mylist.add(productBO);
                                fiveFilter_productIDs.add(productBO.getProductID());
                            }
                        }
                    }
                }
            } else {
                for (ProductMasterBO productBO : items) {

                    if (!bmodel.configurationMasterHelper.IS_STOCK_AVAILABLE_PRODUCTS_ONLY
                            || (bmodel.configurationMasterHelper.IS_STOCK_AVAILABLE_PRODUCTS_ONLY
                            && bmodel.getRetailerMasterBO().getIsVansales() == 1
                            && productBO.getSIH() > 0)
                            || (bmodel.configurationMasterHelper.HAS_SELLER_TYPE_SELECTION_ENABLED
                            && bmodel.getRetailerMasterBO().getIsVansales() == 0 && productBO.getWSIH() > 0)
                            || (bmodel.configurationMasterHelper.IS_STOCK_AVAILABLE_PRODUCTS_ONLY
                            && bmodel.configurationMasterHelper.IS_INVOICE
                            && productBO.getSIH() > 0)) {

                        if (!bmodel.configurationMasterHelper.IS_SHOW_ONLY_INDICATIVE_ORDER
                                ) {
                            if (productBO.getIsSaleable() == 1) {
                                if (bmodel.configurationMasterHelper.IS_LOAD_PRICE_GROUP_PRD_OLY && productBO.getGroupid() == 0)
                                    continue;
                                mylist.add(productBO);
                                fiveFilter_productIDs.add(productBO.getProductID());
                            }
                        }
                    }
                }
            }
        }

//        Applying special filter in product filtered list(mylist)
        if (bmodel.configurationMasterHelper.SHOW_SPL_FILTER && !bmodel.configurationMasterHelper.SHOW_SPL_FLIER_NOT_NEEDED) {

            Vector<ProductMasterBO> temp = new Vector<>();
            String generaltxt = generalbutton;
            for (ProductMasterBO ret : mylist) {
                if (generaltxt.equals(GENERAL))//No special filters selected
                {
                    if (mEdt_searchproductName.getText().length() >= 3) {// User entry filter
                        if (isUserEntryFilterSatisfied(ret))
                            temp.add(ret);
                    } else
                        temp.add(ret);
                } else {
                    if (isSpecialFilterAppliedProduct(generaltxt, ret)) { //special filter selected

                        if (mEdt_searchproductName.getText().length() >= 3) {
                            if (isUserEntryFilterSatisfied(ret))
                                temp.add(ret);
                        } else
                            temp.add(ret);
                    }
                }
            }
            mylist.clear();
            mylist.addAll(temp);
        }
        if (bmodel.configurationMasterHelper.IS_PRODUCT_SEQUENCE_UNIPAL)
            getProductBySequence();

        mSchedule = new SubDStockCheckActivity.MyAdapter(mylist);
        lvwplist.setAdapter(mSchedule);
        strBarCodeSearch = "ALL";
        updateValue();
        mDrawerLayout.closeDrawers();
        this.mSelectedIdByLevelId = mSelectedIdByLevelId;
        updateOrderedCount();

        if (!bmodel.configurationMasterHelper.SHOW_SPL_FILTER) {
            if (count == 1) {
                String strPname = filtertext + " (" + mylist.size() + ")";
                pnametitle.setText(strPname);
                if (bmodel.configurationMasterHelper.SHOW_SPL_FILTER && !bmodel.configurationMasterHelper.IS_SPL_FILTER_TAB) {
                    if (totalOrdCount.equals("0"))
                        setScreenTitle(strPname);
                    else
                        setScreenTitle(totalOrdCount + "/" + strPname);
                }
            } else {
                String strPname = getResources().getString(R.string.product_name) + " (" + mylist.size() + ")";
                pnametitle.setText(strPname);
                if (bmodel.configurationMasterHelper.SHOW_SPL_FILTER && !bmodel.configurationMasterHelper.IS_SPL_FILTER_TAB) {
                    if (totalOrdCount.equals("0"))
                        setScreenTitle(strPname);
                    else
                        setScreenTitle(totalOrdCount + "/" + strPname);
                }
            }
        } else {
            if (generalbutton.equals(GENERAL) && filtertext.equals(BRAND)) {
                String strPname = getResources().getString(
                        R.string.product_name)
                        + " (" + mylist.size() + ")";
                pnametitle.setText(strPname);
                if (bmodel.configurationMasterHelper.SHOW_SPL_FILTER && !bmodel.configurationMasterHelper.IS_SPL_FILTER_TAB) {
                    if (totalOrdCount.equals("0"))
                        setScreenTitle(strPname);
                    else
                        setScreenTitle(totalOrdCount + "/" + strPname);
                }
            } else if (!generalbutton.equals(GENERAL)) {
                String strPname = getFilterName(generalbutton) + " ("
                        + mylist.size() + ")";
                pnametitle.setText(strPname);
                if (bmodel.configurationMasterHelper.SHOW_SPL_FILTER && !bmodel.configurationMasterHelper.IS_SPL_FILTER_TAB) {
                    if (totalOrdCount.equals("0"))
                        setScreenTitle(strPname);
                    else
                        setScreenTitle(totalOrdCount + "/" + strPname);
                }
            } else {
                String strPname = filtertext + " (" + mylist.size() + ")";
                pnametitle.setText(strPname);
                if (bmodel.configurationMasterHelper.SHOW_SPL_FILTER && !bmodel.configurationMasterHelper.IS_SPL_FILTER_TAB) {
                    if (totalOrdCount.equals("0"))
                        setScreenTitle(strPname);
                    else
                        setScreenTitle(totalOrdCount + "/" + strPname);
                }
            }
        }
        if (bmodel.configurationMasterHelper.IS_TOP_ORDER_FILTER) {
            filterAdapter.notifyDataSetChanged();
        }
    }

    class LoadAsyncTask extends AsyncTask<String, Integer, Boolean> {
        @Override
        protected Boolean doInBackground(String... arg0) {
            try {


                return Boolean.TRUE;
            } catch (Exception e) {
                Commons.printException(e);
                return Boolean.FALSE;
            }
        }
    }


    private void updateData(ProductMasterBO productBO) {
        int qty = productBO.getOrderedPcsQty() + (productBO.getOrderedCaseQty() * productBO.getCaseSize()) + (productBO.getOrderedOuterQty() * productBO.getOutersize());

        if (qty == 0) {
            bmodel.productHelper.getmProductidOrderByEntry().remove(productBO.getProductID());
            bmodel.productHelper.getmProductidOrderByEntryMap().remove(SDUtil.convertToInt(productBO.getProductID()));
        } else {
            int lastQty = 0;
            if (bmodel.productHelper.getmProductidOrderByEntryMap().get(SDUtil.convertToInt(productBO.getProductID())) != null)
                lastQty = bmodel.productHelper.getmProductidOrderByEntryMap().get(SDUtil.convertToInt(productBO.getProductID()));
            if (lastQty == qty) {
                // Dont do any thing
            } else {
                if (bmodel.productHelper.getmProductidOrderByEntry().contains(productBO.getProductID())) {
                    bmodel.productHelper.getmProductidOrderByEntry().remove(productBO.getProductID());
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
        final TabLayout tabLay =  findViewById(R.id.dummy_tab_lay);
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
            tab.setTypeface(FontUtils.getFontRoboto(SubDStockCheckActivity.this, FontUtils.FontType.MEDIUM));
            tab.setTextColor(color);
            tab.setMaxLines(1);
            tab.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.font_small));
            tab.setBackgroundColor(ContextCompat.getColor(this, android.R.color.transparent));
            tab.setWidth(width);
            tab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {


                    if (view.getTag().toString().equalsIgnoreCase("ALL")) {
                        updateGeneralText(GENERAL);
                    } else {
                        generalbutton = view.getTag().toString();
                        updateBrandText(BRAND, -1);
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

    //Product sequence for Unipal
    private void getProductBySequence() {
        LinkedHashSet<String> hs = new LinkedHashSet<>();
        Vector<ProductMasterBO> items = mylist;
        Vector<ProductMasterBO> newProductList = new Vector<>();
        for (ProductMasterBO productMasterBO : items) {
            for (int j = 0; j < productMasterBO.getLocations().size(); j++) {
                if (productMasterBO.isRPS() && (productMasterBO.getLocations().get(j).getShelfPiece() == -1
                        || productMasterBO.getLocations().get(j).getShelfCase() == -1
                        || productMasterBO.getLocations().get(j).getShelfOuter() == -1
                        || productMasterBO.getLocations().get(j).getAvailability() == -1)) {
                    hs.add(productMasterBO.getProductID());
                }
            }
            if (productMasterBO.isRPS() && productMasterBO.isSBDAcheived())
                hs.add(productMasterBO.getProductID());
            if (productMasterBO.getIsInitiativeProduct() == 1)
                hs.add(productMasterBO.getProductID());
            if (productMasterBO.isPromo())
                hs.add(productMasterBO.getProductID());
        }
        ArrayList<String> seqIDList = new ArrayList<>(hs);

        for (int i = 0; i < seqIDList.size(); i++) {
            String tempId = seqIDList.get(i);
            for (int j = 0; j < items.size(); j++) {
                ProductMasterBO productMasterBO = items.get(j);
                String prodID = productMasterBO.getProductID();
                if (prodID.equals(tempId)) {
                    newProductList.add(i, productMasterBO);
                }
            }
        }

        for (ProductMasterBO productMasterBO : items) {
            if (!newProductList.contains(productMasterBO)) {
                newProductList.add(productMasterBO);
            }
        }
        mylist.clear();
        mylist.addAll(newProductList);
    }

    private Vector<ProductMasterBO> filterWareHouseProducts() {
        Vector<ProductMasterBO> newItems = new Vector<>();
        if (bmodel.configurationMasterHelper.IS_LOAD_WAREHOUSE_PRD_ONLY) {
            for (ProductMasterBO products : bmodel.productHelper
                    .getTaggedProducts()) {
                if (products.isAvailableinWareHouse()) newItems.add(products);
            }
        } else {
            newItems.addAll(bmodel.productHelper
                    .getTaggedProducts());
        }
        return newItems;
    }

    public class FilterAdapter extends RecyclerView.Adapter<SubDStockCheckActivity.FilterAdapter.MyViewHolder> {

        private Vector<LevelBO> filterList;

        public class MyViewHolder extends RecyclerView.ViewHolder {
            public Button btnFilter;

            public MyViewHolder(View view) {
                super(view);
                btnFilter =  view.findViewById(R.id.btn_filter);
                btnFilter.setTypeface(FontUtils.getFontBalooHai(SubDStockCheckActivity.this,FontUtils.FontType.REGULAR));
            }
        }

        public FilterAdapter(Vector<LevelBO> filterList) {
            this.filterList = filterList;
        }

        @Override
        public SubDStockCheckActivity.FilterAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.filter_rv_item, parent, false);

            return new SubDStockCheckActivity.FilterAdapter.MyViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(SubDStockCheckActivity.FilterAdapter.MyViewHolder holder, final int position) {
            final LevelBO levelBO = filterList.get(position);
            holder.btnFilter.setText(levelBO.getLevelName());
            try {
                if (mSelectedIdByLevelId != null && mSelectedLevelBO != null) {
                    int levelId = mSelectedIdByLevelId.get(mSelectedLevelBO
                            .getProductID());

                    if (levelId == levelBO.getProductID()) {
                        holder.btnFilter.setBackgroundResource(R.drawable.button_rounded_corner_blue);
                        holder.btnFilter.setTextColor(ContextCompat.getColor(SubDStockCheckActivity.this, R.color.white));
                    } else {
                        holder.btnFilter.setBackgroundResource(R.drawable.button_round_corner_grey);
                        holder.btnFilter.setTextColor(ContextCompat.getColor(SubDStockCheckActivity.this, R.color.half_Black));
                    }
                }
            } catch (Exception e) {
                Commons.printException(e);
            }

            holder.btnFilter.setOnClickListener(new View.OnClickListener() {
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
                                //finalParentList = updateProductLoad((sequence.size() - bmodel.productHelper.getmAttributeTypes().size()));
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

    private Vector<LevelBO> updateProductLoad(int pos) {

        Vector<LevelBO> finalValuelist = new Vector<>();

        if (isFilterContentSelected(pos)) {

            int selectedGridLevelID = 0;
            ArrayList<Integer> parentIdList = null;

            for (int i = 0; i < pos; i++) {
                LevelBO levelBO = sequence.get(i);

                if (i != 0) {

                    parentIdList = getParenIdList(selectedGridLevelID,
                            parentIdList, levelBO);

                }
                selectedGridLevelID = mSelectedIdByLevelId.get(levelBO
                        .getProductID());

                if (i == pos - 1) {

                    Vector<LevelBO> gridViewlist = loadedFilterValues
                            .get(levelBO.getProductID());
                    finalValuelist = new Vector<>();
                    if (selectedGridLevelID != 0) {
                        for (LevelBO gridViewBO : gridViewlist) {
                            if (selectedGridLevelID == gridViewBO.getProductID()) {
                                finalValuelist.add(gridViewBO);
                            }

                        }

                    } else {
                        if (parentIdList != null)
                            if (!parentIdList.isEmpty()) {
                                for (int productID : parentIdList) {
                                    for (LevelBO gridViewBO : gridViewlist) {
                                        if (productID == gridViewBO.getProductID()) {
                                            finalValuelist.add(gridViewBO);
                                        }

                                    }
                                }
                            }
                    }
                }

            }


        } else {
            if (pos > 0)
                finalValuelist = loadedFilterValues.get(sequence.get(pos - 1)
                        .getProductID());

        }
        return finalValuelist;

    }

    private ArrayList<Integer> getParenIdList(int selectedGridLevelID,
                                              ArrayList<Integer> list, LevelBO levelBO) {
        ArrayList<Integer> parentIdList = new ArrayList<>();
        Vector<LevelBO> gridViewlist = loadedFilterValues.get(levelBO
                .getProductID());
        if (selectedGridLevelID != 0) {
            if (gridViewlist != null) {
                for (LevelBO gridlevelBO : gridViewlist) {
                    if (selectedGridLevelID == gridlevelBO.getParentID()) {
                        parentIdList.add(gridlevelBO.getProductID());
                    }

                }
            }

        } else {

            if (gridViewlist != null && list != null && list.size() > 0) {
                for (int id : list) {
                    for (LevelBO gridlevelBO : gridViewlist) {
                        if (gridlevelBO.getParentID() == id) {
                            parentIdList
                                    .add(gridlevelBO.getProductID());
                        }
                    }
                }
            }

        }
        return parentIdList;
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
//                        + (product.getOrderedPcsQty() * product
//                        .getSrp())
                        + (product
                        .getOrderedOuterQty() * product
                        .getOsrp());
//                holder.total.setText(bmodel.formatValue(tot));
                product.setTotalamount(tot);
            } else {
                if (!"0".equals(qty)) {
                    Toast.makeText(
                            SubDStockCheckActivity.this,
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
//                        + (product.getOrderedPcsQty() * product
//                        .getSrp())
                        + (product.getOrderedOuterQty() * product
                        .getOsrp());
                product.setTotalamount(tot);
            } else {
                if (!"0".equals(qty)) {
                    Toast.makeText(
                            SubDStockCheckActivity.this,
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
//                    + (product.getOrderedPcsQty() * product
//                    .getSrp())
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
                        new SubDStockCheckActivity.CheckAndEnter().execute();
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
        private AlertDialog.Builder builder;
        private AlertDialog alertDialog;

        @Override
        protected Boolean doInBackground(String... arg0) {
            try {
//              pname = CRISTAL ALTIN 20LI KUTU
//                      barcode = 3086123446168
//              pname = CRISTAL MED TUK 4LU PST MV
//                barcode = 3086121601033
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
            builder = new AlertDialog.Builder(SubDStockCheckActivity.this);
            customProgressDialog(builder, getResources().getString(R.string.loading));
            alertDialog = builder.create();
            alertDialog.show();
        }

        protected void onPostExecute(Boolean result) {
            // result is the value returned from doInBackground
            barcode = "";
            alertDialog.dismiss();
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


    public void refreshList() {
        String strPname = getResources().getString(
                R.string.product_name)
                + " (" + mylist.size() + ")";
        pnametitle.setText(strPname);
        lvwplist.setAdapter(new SubDStockCheckActivity.MyAdapter(mylist));
    }

}
