package com.ivy.sd.png.view;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import com.google.android.material.tabs.TabLayout;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.widget.Toolbar;
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
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
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
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;
import android.widget.ViewFlipper;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.ivy.cpg.view.order.discount.DiscountHelper;
import com.ivy.cpg.view.order.OrderHelper;
import com.ivy.cpg.view.order.productdetails.ProductSchemeDetailsActivity;
import com.ivy.cpg.view.stockcheck.StockCheckHelper;
import com.ivy.cpg.view.survey.SurveyActivityNew;
import com.ivy.lib.Utils;
import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.bo.ConfigureBO;
import com.ivy.sd.png.bo.OrderHeader;
import com.ivy.sd.png.bo.ProductMasterBO;
import com.ivy.sd.png.bo.StandardListBO;
import com.ivy.sd.png.commons.IvyBaseActivityNoActionBar;
import com.ivy.sd.png.commons.SDUtil;
import com.ivy.sd.png.model.BrandDialogInterface;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.model.FiveLevelFilterCallBack;
import com.ivy.sd.png.provider.ConfigurationMasterHelper;
import com.ivy.cpg.view.order.scheme.SchemeDetailsMasterHelper;
import com.ivy.sd.png.util.CommonDialog;
import com.ivy.sd.png.util.Commons;
import com.ivy.utils.DateTimeUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Vector;

public class OrderNewOutlet extends IvyBaseActivityNoActionBar implements OnClickListener,
        BrandDialogInterface, OnEditorActionListener, FiveLevelFilterCallBack {

    private ListView lvwplist;
    private Button mBtn_Search;
    private Button mBtnFilterPopup;
    private Button mBtn_clear;
    private TextView totalValueText;
    private TextView lpcText;
    private TextView distValue;
    private TextView productName;
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

    private int mSelectedBrandID = 0;
    private String mSelectedFiltertext = "Brand";

    private ArrayAdapter<StandardListBO> mLocationAdapter;
    private int mSelectedLocationIndex;
    private MyAdapter mSchedule;
    private String tempPo;
    private String tempRemark;
    private String tempRField1;
    private String tempRField2;
    private HashMap<Integer, Integer> mSelectedIdByLevelId;
    private int mTotalScreenWidth = 0;
    private String strProductObj;
    private int SbdDistPre = 0; // Dist stock
    private int sbdDistAchieved = 0;
    private Button mBtnNext;


    private ArrayList<String> fiveFilter_productIDs;

    private LinkedList<String> mProductList = new LinkedList<>();

    private ArrayList<ProductMasterBO> orderedProductList;
    private OrderHeader orderHeader;

    private TextView totalQtyTV;
    private OrderHelper orderHelper;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_newoutlet_order);

        bmodel = (BusinessModel) getApplicationContext();
        bmodel.setContext(this);
        overridePendingTransition(R.anim.trans_left_in, R.anim.trans_left_out);
        orderHelper = OrderHelper.getInstance(this);
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
        }

        FrameLayout drawer = findViewById(R.id.right_drawer);

        int width = getResources().getDisplayMetrics().widthPixels;
        DrawerLayout.LayoutParams params = (DrawerLayout.LayoutParams) drawer.getLayoutParams();
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
        Button mBtnGuidedSelling_next = findViewById(R.id.btn_guided_selling_next);
        Button mBtnGuidedSelling_prev = findViewById(R.id.btn_guided_selling_prev);
        mBtnGuidedSelling_next.setOnClickListener(this);
        mBtnGuidedSelling_prev.setOnClickListener(this);

        mBtn_Search.setOnClickListener(this);
        mBtnNext.setOnClickListener(this);
        mBtnFilterPopup.setOnClickListener(this);
        mBtn_clear.setOnClickListener(this);
        mEdt_searchproductName.setOnEditorActionListener(this);

        mBtnNext.setTypeface(bmodel.configurationMasterHelper.getFontBaloobhai(ConfigurationMasterHelper.FontType.REGULAR));
        mBtnNext.setText(getResources().getString(R.string.save));


        if (toolbar != null) {

            setSupportActionBar(toolbar);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            updateTitle();
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
                    if (generalbutton.equals(GENERAL)) {
                        updateTitle();
                    }
                }

                supportInvalidateOptionsMenu();
            }

            public void onDrawerOpened(View drawerView) {
                if (getSupportActionBar() != null) {
                    setScreenTitle(getResources().getString(R.string.filter));
                }

                supportInvalidateOptionsMenu();
            }
        };
        mDrawerLayout.addDrawerListener(mDrawerToggle);

        // load location filter
        mLocationAdapter = new ArrayAdapter<>(this,
                android.R.layout.select_dialog_singlechoice);

        for (StandardListBO temp : bmodel.productHelper.getInStoreLocation())
            mLocationAdapter.add(temp);
        if (bmodel.configurationMasterHelper.IS_GLOBAL_LOCATION) {
            mSelectedLocationIndex = bmodel.productHelper.getmSelectedGLobalLocationIndex();
        }

        totalValueText = findViewById(R.id.totalValue);
        lpcText = findViewById(R.id.lcp);
        distValue = findViewById(R.id.distValue);
        totalQtyTV = findViewById(R.id.tv_totalqty);

        hideAndSeek();

        productName = findViewById(R.id.productName);
        productName.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
        mEdt_searchproductName.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));


        lvwplist = findViewById(R.id.list);
        lvwplist.setCacheColorHint(0);

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

        try {
            if ("FromSummary".equals(OrderedFlag)) {
                if (bmodel.configurationMasterHelper.SHOW_SPL_FILTER) {

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

                mBtnNext.setVisibility(View.VISIBLE);
            } else {
                if (bmodel.configurationMasterHelper.SHOW_SPL_FILTER) {
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
        } catch (Exception e) {
            Commons.printException(e + "");
        }

        try {
            mEdt_searchproductName.addTextChangedListener(new TextWatcher() {
                public void afterTextChanged(Editable s) {
                    if (s.length() >= 3) {
                        loadSearchedList();
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
        } catch (Exception e) {
            Commons.printException(e);
        }


        DisplayMetrics dm = new DisplayMetrics();
        getWindow().getWindowManager().getDefaultDisplay().getMetrics(dm);
        mTotalScreenWidth = dm.widthPixels;

        if (bmodel.configurationMasterHelper.SHOW_STORE_WISE_DISCOUNT_DLG) {
            DiscountHelper.getInstance(this).setMinimumRangeAsBillWiseDiscount();
        }

        mDrawerLayout.closeDrawer(GravityCompat.END);
    }


    private void updateTitle() {

        String title;
        if ("MENU_ORDER".equals(screenCode)) {
            title = bmodel.configurationMasterHelper
                    .getHomescreentwomenutitle("MENU_ORDER");
            if (title.equalsIgnoreCase("MENU_ORDER"))
                title = bmodel.configurationMasterHelper
                        .getHomescreentwomenutitle("MENU_STK_ORD");
        } else {
            title = bmodel.configurationMasterHelper
                    .getHomescreentwomenutitle("MENU_STK_ORD");
        }
        if (title.isEmpty())
            title = getResources().getString(R.string.order);

        setScreenTitle(title);
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
            savedInstanceState.putSerializable(ORDER_FLAG, OrderedFlag);
            savedInstanceState.putSerializable(TEMP_PO, tempPo);
            savedInstanceState.putSerializable(TEMP_REMARK, tempRemark);
            savedInstanceState.putSerializable(TEMP_RFIELD1, tempRField1);
            savedInstanceState.putSerializable(TEMP_RFIELD2, tempRField2);
            savedInstanceState.putSerializable(SCREEN_CODE, screenCode);
        }
        super.onSaveInstanceState(savedInstanceState);
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

            if (bmodel.configurationMasterHelper.SHOW_TOTAL_QTY_ORDER) {
                findViewById(R.id.ll_totqty).setVisibility(View.VISIBLE);
            } else {
                findViewById(R.id.ll_totqty).setVisibility(View.GONE);
            }

            if (bmodel.configurationMasterHelper.SHOW_TOTAL_VALUE_ORDER) {
                findViewById(R.id.ll_value).setVisibility(View.VISIBLE);
            } else {
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
            super(OrderNewOutlet.this,
                    R.layout.activity_stock_and_order_listview_new, items);
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

        public View getView(final int position, View convertView,
                            ViewGroup parent) {
            final ViewHolder holder;
            ProductMasterBO product = items.get(position);

            View row = convertView;
            if (row == null) {

                final LayoutInflater inflater = getLayoutInflater();
                row = inflater.inflate(
                        R.layout.activity_stock_and_order_listview_newoutlet, parent,
                        false);
                holder = new ViewHolder();

                holder.tvbarcode = row
                        .findViewById(R.id.stock_and_order_listview_productbarcode);

                holder.psname = row
                        .findViewById(R.id.stock_and_order_listview_productname);


                //Store - Order
                holder.caseQty = row
                        .findViewById(R.id.stock_and_order_listview_case_qty);
                holder.pcsQty = row
                        .findViewById(R.id.stock_and_order_listview_pcs_qty);
                holder.outerQty = row
                        .findViewById(R.id.stock_and_order_listview_outer_case_qty);

                holder.srp = row
                        .findViewById(R.id.stock_and_order_listview_srp);

                holder.total = row
                        .findViewById(R.id.stock_and_order_listview_total);


                holder.weight = row
                        .findViewById(R.id.stock_and_order_listview_weight);

                holder.iv_info = row.findViewById(R.id.ivInfoicon);

                holder.psname.setMaxLines(bmodel.configurationMasterHelper.MAX_NO_OF_PRODUCT_LINES);
                (row.findViewById(R.id.view_dotted_line)).setLayerType(View.LAYER_TYPE_SOFTWARE, null);
                //setting typefaces
                holder.tvbarcode.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
                holder.psname.setTypeface(bmodel.configurationMasterHelper.getProductNameFont());
                holder.caseQty.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
                holder.pcsQty.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
                holder.outerQty.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
                holder.srp.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
                holder.total.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
                holder.weight.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));


                if (!bmodel.configurationMasterHelper.SHOW_BARCODE)
                    holder.tvbarcode.setVisibility(View.GONE);

                // SIH - Enable/Disable - Start
                // Order Field - Enable/Disable
                if (!bmodel.configurationMasterHelper.SHOW_ORDER_CASE)
                    ((LinearLayout) row.findViewById(R.id.llCase)).setVisibility(View.GONE);
                else {
                    try {
                        ((TextView) row.findViewById(R.id.caseTitle)).setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
                        if (bmodel.labelsMasterHelper.applyLabels(row.findViewById(
                                R.id.caseTitle).getTag()) != null)
                            ((TextView) row.findViewById(R.id.caseTitle))
                                    .setText(bmodel.labelsMasterHelper
                                            .applyLabels(row.findViewById(
                                                    R.id.caseTitle).getTag()));
                    } catch (Exception e) {
                        Commons.printException(e + "");
                    }
                }
                if (!bmodel.configurationMasterHelper.SHOW_ORDER_PCS)
                    (row.findViewById(R.id.llPcs)).setVisibility(View.GONE);
                else {
                    try {
                        ((TextView) row.findViewById(R.id.pcsTitle)).setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
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
                if (!bmodel.configurationMasterHelper.SHOW_OUTER_CASE)
                    (row.findViewById(R.id.llOuter)).setVisibility(View.GONE);
                else {
                    try {
                        ((TextView) row.findViewById(R.id.outercaseTitle)).setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
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


                if (!bmodel.configurationMasterHelper.SHOW_STK_ORD_SRP)
                    (row.findViewById(R.id.llSrp)).setVisibility(View.GONE);
                else {
                    try {
                        ((TextView) row.findViewById(R.id.srpTitle)).setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
                        if (bmodel.labelsMasterHelper.applyLabels(row.findViewById(
                                R.id.srpTitle).getTag()) != null)
                            ((TextView) row.findViewById(R.id.srpTitle))
                                    .setText(bmodel.labelsMasterHelper
                                            .applyLabels(row.findViewById(
                                                    R.id.srpTitle).getTag()));
                    } catch (Exception e) {
                        Commons.printException(e + "");
                    }
                }

                if (!bmodel.configurationMasterHelper.SHOW_ORDER_TOTAL)
                    (row.findViewById(R.id.llTotal)).setVisibility(View.GONE);
                else {
                    try {
                        ((TextView) row.findViewById(R.id.totalTitle)).setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
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
                        ((TextView) row.findViewById(R.id.weight)).setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
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
                        if (qty.length() > 0)
                            holder.caseQty.setSelection(qty.length());

                        float totalQty = (SDUtil.convertToInt(qty) * holder.productObj
                                .getCaseSize())
                                + (holder.productObj.getOrderedPcsQty())
                                + (holder.productObj.getOrderedOuterQty() * holder.productObj
                                .getOutersize());

                        holder.weight.setText(Utils.formatAsTwoDecimal((double) (totalQty * holder.productObj.getWeight())));


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

                        if (bmodel.configurationMasterHelper.IS_SHOW_ORDERING_SEQUENCE)
                            updateData(holder.productObj);
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

                    holder.caseQty.setOnClickListener(new OnClickListener() {
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
                                dialogCustomKeyBoard = new CustomKeyBoard(OrderNewOutlet.this, holder.caseQty);
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
                                productName.setText(strProductObj);
                            } else
                                productName.setText(holder.pname);

                            QUANTITY = holder.caseQty;
                            QUANTITY.setTag(holder.productObj);
                            int inType = holder.caseQty.getInputType();
                            holder.caseQty.setInputType(InputType.TYPE_NULL);
                            holder.caseQty.onTouchEvent(event);
                            holder.caseQty.setInputType(inType);
                            holder.caseQty.requestFocus();
                            if (holder.caseQty.getText().length() > 0)
                                holder.caseQty.setSelection(holder.caseQty.getText().length());
                            inputManager.hideSoftInputFromWindow(
                                    mEdt_searchproductName.getWindowToken(), 0);
                            return true;
                        }
                    });
                }

                holder.pcsQty.addTextChangedListener(new TextWatcher() {
                    public void afterTextChanged(Editable s) {
                        if (holder.productObj.getPcUomid() == 0) {
                            holder.pcsQty.removeTextChangedListener(this);
                            holder.pcsQty.setText("0");
                            holder.pcsQty.addTextChangedListener(this);
                            return;
                        }

                        String qty = s.toString();
                        if (qty.length() > 0)
                            holder.pcsQty.setSelection(qty.length());

                        /** Calculate the total pcs qty **/
                        float totalQty = (holder.productObj.getOrderedCaseQty() * holder.productObj
                                .getCaseSize())
                                + (SDUtil.convertToInt(qty))
                                + (holder.productObj.getOrderedOuterQty() * holder.productObj
                                .getOutersize());

                        holder.weight.setText(Utils.formatAsTwoDecimal((double) (totalQty * holder.productObj.getWeight())));


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

                        if (bmodel.configurationMasterHelper.IS_SHOW_ORDERING_SEQUENCE)
                            updateData(holder.productObj);
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

                    holder.pcsQty.setOnClickListener(new OnClickListener() {
                        @Override
                        public void onClick(View v) {

                            if (bmodel.configurationMasterHelper.SHOW_SIH_IN_PNAME) {
                                strProductObj = "[SIH :" + holder.productObj.getSIH() + "] "
                                        + holder.pname;
                                productName.setText(strProductObj);
                            } else
                                productName.setText(holder.pname);

                            if (dialogCustomKeyBoard == null || !dialogCustomKeyBoard.isDialogCreated()) {
                                dialogCustomKeyBoard = new CustomKeyBoard(OrderNewOutlet.this, holder.pcsQty);
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
                                productName.setText(strProductObj);
                            } else
                                productName.setText(holder.pname);

                            QUANTITY = holder.pcsQty;
                            QUANTITY.setTag(holder.productObj);
                            int inType = holder.pcsQty.getInputType();
                            holder.pcsQty.setInputType(InputType.TYPE_NULL);
                            holder.pcsQty.onTouchEvent(event);
                            holder.pcsQty.setInputType(inType);
                            holder.pcsQty.requestFocus();
                            if (holder.pcsQty.getText().length() > 0)
                                holder.pcsQty.setSelection(holder.pcsQty.getText().length());
                            inputManager.hideSoftInputFromWindow(
                                    mEdt_searchproductName.getWindowToken(), 0);
                            return true;
                        }
                    });
                }

                holder.outerQty.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void onTextChanged(CharSequence s, int start,
                                              int before, int count) {
                        if (holder.productObj.getOutersize() == 0) {
                            holder.outerQty.removeTextChangedListener(this);
                            holder.outerQty.setText("0");
                            holder.outerQty.addTextChangedListener(this);
                            return;
                        }
                        String qty = s.toString();
                        if (qty.length() > 0)
                            holder.outerQty.setSelection(qty.length());

                        float totalQty = (SDUtil.convertToInt(qty) * holder.productObj
                                .getOutersize())
                                + (holder.productObj.getOrderedCaseQty() * holder.productObj
                                .getCaseSize())
                                + +(holder.productObj.getOrderedPcsQty());
                        holder.weight.setText(Utils.formatAsTwoDecimal((double) (totalQty * holder.productObj.getWeight())));

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
                        if (bmodel.configurationMasterHelper.IS_SHOW_ORDERING_SEQUENCE)
                            updateData(holder.productObj);
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

                    holder.outerQty.setOnClickListener(new OnClickListener() {
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
                                dialogCustomKeyBoard = new CustomKeyBoard(OrderNewOutlet.this, holder.outerQty);
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
                                productName.setText(strProductObj);
                            } else
                                productName.setText(holder.pname);

                            QUANTITY = holder.outerQty;
                            QUANTITY.setTag(holder.productObj);
                            int inType = holder.outerQty.getInputType();
                            holder.outerQty.setInputType(InputType.TYPE_NULL);
                            holder.outerQty.onTouchEvent(event);
                            holder.outerQty.setInputType(inType);
                            holder.outerQty.requestFocus();
                            if (holder.outerQty.getText().length() > 0)
                                holder.outerQty.setSelection(holder.outerQty.getText().length());
                            inputManager.hideSoftInputFromWindow(
                                    mEdt_searchproductName.getWindowToken(), 0);
                            return true;
                        }
                    });
                }


                row.setOnClickListener(new OnClickListener() {
                    public void onClick(View v) {
                        if (bmodel.configurationMasterHelper.SHOW_SIH_IN_PNAME) {
                            strProductObj = "[SIH :"
                                    + holder.productObj.getSIH() + "] "
                                    + holder.pname;
                            productName.setText(strProductObj);
                        } else
                            productName.setText(holder.pname);
                        QUANTITY = holder.pcsQty;
                        QUANTITY.setTag(holder.productObj);
                        holder.pcsQty.selectAll();
                        holder.pcsQty.requestFocus();

                        if (viewFlipper.getDisplayedChild() != 0) {
                            viewFlipper.showPrevious();

                        }
                    }
                });


                holder.iv_info.setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        bmodel = (BusinessModel) getApplicationContext();
                        bmodel.setContext(OrderNewOutlet.this);

                        SchemeDetailsMasterHelper schemeHelper = SchemeDetailsMasterHelper.getInstance(getApplicationContext());
                        if (bmodel.configurationMasterHelper.IS_SCHEME_DIALOG || bmodel.configurationMasterHelper.IS_PRODUCT_SCHEME_DIALOG) {
                            if (schemeHelper
                                    .getSchemeList() == null
                                    || schemeHelper
                                    .getSchemeList().size() == 0) {
                                Toast.makeText(OrderNewOutlet.this,
                                        R.string.scheme_not_available,
                                        Toast.LENGTH_SHORT).show();
                            }

                            bmodel.productHelper.setSchemes(schemeHelper.getSchemeList());
                            bmodel.productHelper.setPdname(holder.pname);
                            bmodel.productHelper.setProdId(holder.productId);
                            bmodel.productHelper.setProductObj(holder.productObj);
                            bmodel.productHelper.setFlag(1);
                            bmodel.productHelper.setTotalScreenSize(mTotalScreenWidth);

                            Intent intent = new Intent(OrderNewOutlet.this, ProductSchemeDetailsActivity.class);
                            intent.putExtra("productId", holder.productId);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            startActivity(intent);

                        } else {

                            bmodel.productHelper.setPdname(holder.pname);
                            bmodel.productHelper.setProdId(holder.productId);
                            bmodel.productHelper.setProductObj(holder.productObj);
                            bmodel.productHelper.setFlag(1);
                            bmodel.productHelper.setTotalScreenSize(mTotalScreenWidth);

                            SchemeDialog sc = new SchemeDialog(
                                    OrderNewOutlet.this,
                                    schemeHelper
                                            .getSchemeList(), holder.pname,
                                    holder.productId, holder.productObj, 1, mTotalScreenWidth);
                            FragmentManager fm = getSupportFragmentManager();
                            sc.show(fm, "");
                        }
                    }
                });
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

            holder.tvbarcode.setText(holder.productObj.getBarCode());

            holder.psname.setText(holder.productObj.getProductShortName());
            holder.pname = holder.productObj.getProductName();

            // Set order qty
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

            if (bmodel.configurationMasterHelper.SHOW_STK_ORD_SRP)
                holder.srp.setText(bmodel.formatValue(holder.productObj
                        .getSrp()));
            if (holder.productObj.getOuUomid() == 0 || !holder.productObj.isOuterMapped()) {
                holder.outerQty.setEnabled(false);
            } else {
                holder.outerQty.setEnabled(true);
            }
            if (holder.productObj.getCaseUomId() == 0 || !holder.productObj.isCaseMapped()) {
                holder.caseQty.setEnabled(false);
            } else {
                holder.caseQty.setEnabled(true);
            }
            if (holder.productObj.getPcUomid() == 0 || !holder.productObj.isPieceMapped()) {
                holder.pcsQty.setEnabled(false);
            } else {
                holder.pcsQty.setEnabled(true);
            }
            return row;
        }
    }

    class ViewHolder {
        private String productId;
        private String pname;
        private ProductMasterBO productObj;
        private TextView tvbarcode;
        private TextView psname;
        private TextView srp;
        private TextView weight;
        private EditText pcsQty;
        private EditText caseQty;
        private EditText outerQty;
        private TextView total;
        private ImageView iv_info;
    }


    private void updateSBDAcheived(String grpName, boolean status) {
        Vector<ProductMasterBO> items = bmodel.productHelper.getProductMaster();
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

    public void updateValue() {
        try {
            int totalAllQty = 0;
            int lpccount = 0;
            double totalvalue = 0;
            HashSet<String> sbdAcheived = new HashSet<>();
            HashSet<String> sbdStockAchieved = new HashSet<>();
            HashSet<String> sbdStkAndOrderAchieved = new HashSet<>();

            Vector<ProductMasterBO> items = bmodel.productHelper
                    .getProductMaster();
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

                    int totalOrderedQty = orderHelper.getTotalOrderedQty(ret);
                    totalAllQty = (totalOrderedQty != -1) ? (totalAllQty + totalOrderedQty) : (totalAllQty + (ret.getOrderedPcsQty() + (ret.getOrderedCaseQty() * ret.getCaseSize()) + (ret.getOrderedOuterQty() * ret.getOutersize())));
                }
                if (ret.isRPS()) {
                    int size = ret.getLocations().size();
                    for (int j = 0; j < size; j++) {
                        if (ret.getLocations().get(j).getWHCase() > 0
                                || ret.getLocations().get(j).getWHOuter() > 0
                                || ret.getLocations().get(j).getWHPiece() > 0
                                || ret.getLocations().get(j).getShelfCase() > -1
                                || ret.getLocations().get(j).getShelfOuter() > -1
                                || ret.getLocations().get(j).getShelfPiece() > -1) {

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
            float per;

            SbdDistPre = sbdStockAchieved.size();


            per = (float) sbdAcheived.size()
                    / bmodel.getRetailerMasterBO()
                    .getSbdDistributionTarget();
            sbdDistAchieved = sbdAcheived.size();


            String strLpcCouunt = lpccount + "";
            lpcText.setText(strLpcCouunt);
            Commons.print("numberpressed=" + totalvalue);
            String strFormatValue = bmodel.formatValue(totalvalue) + "";
            totalValueText.setText(strFormatValue);
            totalQtyTV.setText("" + totalAllQty);

            if (bmodel.configurationMasterHelper.HIDE_ORDER_DIST) {
                findViewById(R.id.ll_dist).setVisibility(View.GONE);
            } else {
                if (bmodel.configurationMasterHelper.IS_DIST_PRE_POST_ORDER) {

                    String strDistValue = sbdStockAchieved.size() + "/"
                            + sbdAcheived.size();
                    distValue.setText(strDistValue);

                } else {
                    String strDistValue = Math.round(per * 100) + "";
                    distValue.setText(strDistValue);
                }
            }

            if (!bmodel.configurationMasterHelper.SHOW_LPC_ORDER) {
                findViewById(R.id.ll_lpc).setVisibility(View.GONE);
            }

            ((TextView) findViewById(R.id.totalText)).setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
            ((TextView) findViewById(R.id.totalValue)).setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
            ((TextView) findViewById(R.id.lpc_title)).setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
            ((TextView) findViewById(R.id.lcp)).setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
            ((TextView) findViewById(R.id.distText)).setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
            ((TextView) findViewById(R.id.distValue)).setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));

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
                    OrderNewOutlet.this);
            final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(
                    OrderNewOutlet.this,
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

            onnext();
        }
    }


    private void onnext() {
        if (bmodel.getOrderHeaderBO() == null)
            bmodel.setOrderHeaderBO(new OrderHeader());

        bmodel.getRetailerMasterBO().setSbdDistStock(this.SbdDistPre);
        bmodel.getRetailerMasterBO().setSbdDistAchieved(
                this.sbdDistAchieved);


        nextButtonClick();


    }

    private void nextButtonClick() {
        try {
            if (bmodel.hasOrder()) {
                if (bmodel.getOrderHeaderBO() == null)
                    bmodel.setOrderHeaderBO(new OrderHeader());

                orderHeader=new OrderHeader();

                orderHeader.setPO(
                        tempPo == null ? "" : tempPo);
                orderHeader.setRemark(
                        tempRemark == null ? "" : tempRemark);
                orderHeader.setRField1(
                        tempRField1 == null ? "" : tempRField1);
                orderHeader.setRField2(
                        tempRField2 == null ? "" : tempRField2);

                Vector<ProductMasterBO> productList = bmodel.productHelper
                        .getProductMaster();
                int productsCount = 0;
                if (productList != null)
                    productsCount = productList.size();
                ProductMasterBO productBO;
                double line_total_price = 0;
                float totalWeight = 0;
                if (orderedProductList == null)
                    orderedProductList = new ArrayList<>();
                else
                    orderedProductList.clear();
                for (int i = 0; i < productsCount; i++) {
                    productBO = productList.elementAt(i);
                    if (productBO.getOrderedCaseQty() > 0
                            || productBO.getOrderedPcsQty() > 0
                            || productBO.getOrderedOuterQty() > 0) {
                        int totalQty = productBO.getOrderedPcsQty() + productBO.getOrderedCaseQty() * productBO.getCaseSize() + productBO.getOrderedOuterQty() * productBO.getOutersize();
                        totalWeight = totalWeight + (totalQty * productBO.getWeight());

                        double each_total = (productBO.getOrderedCaseQty() * productBO
                                .getCsrp())
                                + (productBO.getOrderedPcsQty() * productBO
                                .getSrp())
                                + (productBO.getOrderedOuterQty() * productBO
                                .getOsrp());

                        line_total_price = line_total_price + SDUtil.formatAsPerCalculationConfig(each_total);

                        productBO.setNetValue(each_total);

                        orderedProductList.add(productBO);
                        bmodel.newOutletHelper.getOrderedProductList().add(productBO);
                    }
                }
                orderHeader.setTotalWeight(totalWeight);
                orderHeader.setOrderValue(line_total_price);
                orderHeader.setLinesPerCall(SDUtil.convertToInt(lpcText.getText().toString()));


                if (bmodel.configurationMasterHelper.IS_MUST_SELL
                        && !bmodel.productHelper.isMustSellFilled()) {
                    if (dialog == null) {
                        dialog = new MustSellReasonDialog(
                                OrderNewOutlet.this, false,
                                diagDismissListen, bmodel);
                        dialog.setMustSellReasonSelectedListener(new MustSellReasonDialog.OnMustSellReasonSelectedListener() {
                            @Override
                            public void onReasonSelected(String reasonId) {
                                orderHeader.setRemark(reasonId);
                            }
                        });
                    }
                    dialog.show();
                    return;
                }

                bmodel.setOrderHeaderBO(orderHeader);
                nextBtnSubTask();
            } else {
                bmodel.showAlert(
                        getResources().getString(
                                R.string.no_items_added), 0);
            }
        } catch (Exception e) {
            Commons.printException(e + "");
        }
    }

    private final OnCancelListener diagDismissListen = new OnCancelListener() {

        @Override
        public void onCancel(DialogInterface dialog) {
            nextBtnSubTask();
        }
    };

    private void nextBtnSubTask() {

        //Intent i = new Intent(OrderNewOutlet.this, OrderSummary.class);
        // i.putExtra("ScreenCode", screenCode);
        // startActivity(i);
        Toast.makeText(OrderNewOutlet.this, R.string.order_will_be_saved_later, Toast.LENGTH_LONG).show();
        Intent returnIntent = new Intent();
        if (orderedProductList != null && orderedProductList.size() > 0) {
            returnIntent.putExtra("ordered_products", orderedProductList);
            returnIntent.putExtra("order_header",orderHeader);
        }
        setResult(RESULT_OK, returnIntent);
        overridePendingTransition(R.anim.trans_left_in, R.anim.trans_left_out);
        finish();

    }

    private void backButtonClick() {
        try {
            if (bmodel.hasOrder()) {
                showDialog(0);
            } else {
                bmodel.outletTimeStampHelper
                        .updateTimeStampModuleWise(DateTimeUtils.now(DateTimeUtils.TIME));
                finish();
            }
            overridePendingTransition(R.anim.trans_right_in, R.anim.trans_right_out);

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
                        OrderHelper.getInstance(OrderNewOutlet.this).setSerialNoListByProductId(null);

                        if (bmodel.configurationMasterHelper.SHOW_PRODUCTRETURN)
                            bmodel.productHelper
                                    .clearBomReturnProductsTable();

                        overridePendingTransition(R.anim.trans_right_in, R.anim.trans_right_out);
                        finish();
                    }
                }, new CommonDialog.negativeOnClickListener() {
                    @Override
                    public void onNegativeButtonClick() {
                    }
                }).show();

                break;
            case 1:
                AlertDialog.Builder builder1 = new AlertDialog.Builder(OrderNewOutlet.this)
                        .setIcon(null)
                        .setCancelable(false)
                        .setTitle(
                                getResources().getString(
                                        R.string.do_you_want_to_save_stock))
                        .setPositiveButton(getResources().getString(R.string.ok),
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog,
                                                        int whichButton) {
                                        StockCheckHelper.getInstance(OrderNewOutlet.this).saveClosingStock(OrderNewOutlet.this, true);
                                        Toast.makeText(
                                                OrderNewOutlet.this,
                                                getResources().getString(
                                                        R.string.stock_saved),
                                                Toast.LENGTH_SHORT).show();

                                        startActivity(new Intent(
                                                OrderNewOutlet.this,
                                                HomeScreenTwo.class));
                                        overridePendingTransition(R.anim.trans_right_in, R.anim.trans_right_out);
                                        finish();
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


            } else {
                Button ed = findViewById(vw.getId());
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
        for (ProductMasterBO temp : bmodel.productHelper.getProductMaster()) {
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

    @Override
    public void updateCancel() {
        mDrawerLayout.closeDrawers();
    }

    private void loadProductList() {
        try {
            Vector<ProductMasterBO> items = bmodel.productHelper
                    .getProductMaster();
            int siz = items.size();
            mylist = new Vector<>();
            for (int i = 0; i < siz; ++i) {
                ProductMasterBO ret = items.elementAt(i);
                if (bmodel.configurationMasterHelper.IS_LOAD_PRICE_GROUP_PRD_OLY && ret.getGroupid() == 0)
                    continue;

                if (ret.getIsSaleable() == 1) {
                    if (generalbutton.equals(GENERAL) && brandbutton.equals(BRAND))//No filters selected
                        mylist.add(ret);
                    else if (applyProductAndSpecialFilter(ret))
                        mylist.add(ret);
                }
            }

            if (bmodel.configurationMasterHelper.IS_PRODUCT_SEQUENCE_UNIPAL)
                getProductBySequence();

            mSchedule = new MyAdapter(mylist);
            lvwplist.setAdapter(mSchedule);


        } catch (Exception e) {
            Commons.printException(e);
        }
    }

    private void loadSearchedList() {
        if (mEdt_searchproductName.getText().length() >= 3) {
            Vector<ProductMasterBO> items = bmodel.productHelper
                    .getProductMaster();
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

                if (bmodel.configurationMasterHelper.IS_LOAD_PRICE_GROUP_PRD_OLY && ret.getGroupid() == 0)
                    continue;

                if (mSelectedFilter.equals(getResources().getString(
                        R.string.order_dialog_barcode))) {

                    if (ret.getBarCode() != null
                            && (ret.getBarCode().toLowerCase()
                            .contains(mEdt_searchproductName.getText().toString().toLowerCase())
                            || ret.getCasebarcode().toLowerCase().
                            contains(mEdt_searchproductName.getText().toString().toLowerCase())
                            || ret.getOuterbarcode().toLowerCase().
                            contains(mEdt_searchproductName.getText().toString().toLowerCase())) && ret.getIsSaleable() == 1) {

                        if (generalbutton.equals(GENERAL) && brandbutton.equals(BRAND))//No filters selected
                            mylist.add(ret);
                        else if (applyProductAndSpecialFilter(ret))
                            mylist.add(ret);
                    }
                } else if (mSelectedFilter.equals(getResources().getString(
                        R.string.prod_code))) {
                    if ((ret.getRField1() != null && ret.getRField1()
                            .toLowerCase()
                            .contains(
                                    mEdt_searchproductName.getText().toString()
                                            .toLowerCase()) || (ret.getProductCode() != null
                            && ret.getProductCode().toLowerCase().contains(mEdt_searchproductName.getText().toString().toLowerCase())))
                            && ret.getIsSaleable() == 1) {
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
                } else {
                    if (ret.getBarCode() != null
                            && (ret.getBarCode().toLowerCase()
                            .contains(mEdt_searchproductName.getText().toString().toLowerCase())
                            || ret.getCasebarcode().toLowerCase().
                            contains(mEdt_searchproductName.getText().toString().toLowerCase())
                            || ret.getOuterbarcode().toLowerCase().
                            contains(mEdt_searchproductName.getText().toString().toLowerCase())) && ret.getIsSaleable() == 1) {

                        if (generalbutton.equals(GENERAL) && brandbutton.equals(BRAND))//No filters selected
                            mylist.add(ret);
                        else if (applyProductAndSpecialFilter(ret))
                            mylist.add(ret);
                    } else if ((ret.getRField1() != null && ret.getRField1()
                            .toLowerCase()
                            .contains(
                                    mEdt_searchproductName.getText().toString()
                                            .toLowerCase()) || (ret.getProductCode() != null
                            && ret.getProductCode().toLowerCase().contains(mEdt_searchproductName.getText().toString().toLowerCase())))
                            && ret.getIsSaleable() == 1) {
                        if (generalbutton.equals(GENERAL) && brandbutton.equals(BRAND))//No filters selected
                            mylist.add(ret);
                        else if (applyProductAndSpecialFilter(ret))
                            mylist.add(ret);
                    } else if (ret.getProductShortName() != null && ret.getProductShortName()
                            .toLowerCase()
                            .contains(
                                    mEdt_searchproductName.getText().toString()
                                            .toLowerCase()) && ret.getIsSaleable() == 1) {
                        if (generalbutton.equals(GENERAL) && brandbutton.equals(BRAND))//No filters selected
                            mylist.add(ret);
                        else if (applyProductAndSpecialFilter(ret))
                            mylist.add(ret);
                    }
                }
            }
            if (bmodel.configurationMasterHelper.IS_PRODUCT_SEQUENCE_UNIPAL)
                getProductBySequence();

            mSchedule = new MyAdapter(mylist);
            lvwplist.setAdapter(mSchedule);
        } else {
            Toast.makeText(this, "Enter atleast 3 letters.", Toast.LENGTH_SHORT)
                    .show();
        }
    }

    private boolean applyProductAndSpecialFilter(ProductMasterBO ret) {
        if (!GENERAL.equals(generalbutton) && !BRAND.equals(brandbutton)) {
            // both filter selected
            if (fiveFilter_productIDs != null && fiveFilter_productIDs.contains(ret.getProductID())
                    && isSpecialFilterAppliedProduct(generalbutton, ret))
                return true;

        } else if (!GENERAL.equals(generalbutton) && BRAND.equals(brandbutton)) {
            //special filter alone selected
            if (isSpecialFilterAppliedProduct(generalbutton, ret))
                return true;
        } else if (GENERAL.equals(generalbutton) && !BRAND.equals(brandbutton)) {
            // product filter alone selected
            if (fiveFilter_productIDs != null && fiveFilter_productIDs.contains(ret.getProductID()))
                return true;

        }
        return false;
    }

    private void getMandatoryFilters() {
        for (ConfigureBO bo : bmodel.configurationMasterHelper.getGenFilter()) {
            if (bo.getMandatory() == 1) {
                if (bo.getConfigCode().equals(mSbd))
                    isSbd = true;
                else if (bo.getConfigCode().equals(mSbdGaps))
                    isSbdGaps = true;
                else if (bo.getConfigCode().equals(mOrdered))
                    isOrdered = true;
                else if (bo.getConfigCode().equals(mPurchased))
                    isPurchased = true;
                else if (bo.getConfigCode().equals(mInitiative))
                    isInitiative = true;
                else if (bo.getConfigCode().equals(mOnAllocation))
                    isOnAllocation = true;
                else if (bo.getConfigCode().equals(mInStock))
                    isInStock = true;
                else if (bo.getConfigCode().equals(mPromo))
                    isPromo = true;
                else if (bo.getConfigCode().equals(mMustSell))
                    isMustSell = true;
                else if (bo.getConfigCode().equals(mFocusBrand))
                    isFocusBrand = true;
                else if (bo.getConfigCode().equals(mFocusBrand2))
                    isFocusBrand2 = true;
                else if (bo.getConfigCode().equals(msih))
                    isSIH = true;
                else if (bo.getConfigCode().equals(mOOS))
                    isOOS = true;
                else if (bo.getConfigCode().equals(mNMustSell))
                    isNMustSell = true;
                else if (bo.getConfigCode().equals(mDiscount))
                    isDiscount = true;
                else if (bo.getConfigCode().equals(mStock))
                    isStock = true;
            }
        }
    }

    @Override
    public void updateBrandText(String mFilterText, int bid) {
        mSelectedBrandID = bid;
        mSelectedFiltertext = mFilterText;

        Commons.print("Stock and order  :," + " update brand text called :"
                + bmodel.productHelper.getProductMaster().size()
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

            Vector<ProductMasterBO> items = bmodel.productHelper.getProductMaster();
            if (items == null) {
                bmodel.showAlert(
                        getResources().getString(R.string.no_products_exists),
                        0);
                return;
            }

            int siz = items.size();
            mylist = new Vector<>();


            // Add the products into list
            for (int i = 0; i < siz; ++i) {
                ProductMasterBO ret = items.elementAt(i);
                if (ret.getBarCode().equals(strBarCodeSearch)
                        || ret.getCasebarcode().equals(strBarCodeSearch)
                        || ret.getOuterbarcode().equals(strBarCodeSearch)
                        || "ALL".equals(strBarCodeSearch)) {
                    if (bmodel.configurationMasterHelper.IS_LOAD_PRICE_GROUP_PRD_OLY && ret.getGroupid() == 0)
                        continue;

                    if ((bid == -1 || bid == ret.getParentid()) && GENERAL.equalsIgnoreCase(generaltxt) && ret.getIsSaleable() == 1) {
                        // product filter alone
                        if (mEdt_searchproductName.getText().length() >= 3) {
                            if (isUserEntryFilterSatisfied(ret))
                                mylist.add(ret);
                        } else {
                            mylist.add(ret);
                        }
                    } else if ((bid == -1 || bid == ret.getParentid()) && !GENERAL.equalsIgnoreCase(generaltxt) && ret.getIsSaleable() == 1) {
                        //special(GENERAL) filter with or without product filter
                        if (isSpecialFilterAppliedProduct(generaltxt, ret)) {
                            if (mEdt_searchproductName.getText().length() >= 3) {
                                if (isUserEntryFilterSatisfied(ret))
                                    mylist.add(ret);
                            } else {
                                mylist.add(ret);
                            }
                        }

                    }
                }
            }

            if (bmodel.configurationMasterHelper.IS_PRODUCT_SEQUENCE_UNIPAL)
                getProductBySequence();

            // set the new list to listview
            mSchedule = new MyAdapter(mylist);
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
            if (ret.getBarCode() != null
                    && (ret.getBarCode().toLowerCase()
                    .contains(mEdt_searchproductName.getText().toString().toLowerCase())
                    || ret.getCasebarcode().toLowerCase().
                    contains(mEdt_searchproductName.getText().toString().toLowerCase())
                    || ret.getOuterbarcode().toLowerCase().
                    contains(mEdt_searchproductName.getText().toString().toLowerCase()) && ret.getIsSaleable() == 1)) {
                return true;
            }
        } else if (getResources().getString(
                R.string.order_gcas).equals(mSelectedFilter)) {
            if (ret.getRField1() != null && ret.getRField1()
                    .toLowerCase()
                    .contains(
                            mEdt_searchproductName.getText().toString()
                                    .toLowerCase()) && ret.getIsSaleable() == 1)
                return true;

        } else if (getResources().getString(
                R.string.product_name).equals(mSelectedFilter)) {
            if (ret.getProductShortName() != null && ret.getProductShortName()
                    .toLowerCase()
                    .contains(
                            mEdt_searchproductName.getText().toString()
                                    .toLowerCase()) && ret.getIsSaleable() == 1)
                return true;
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
                || (generaltxt.equalsIgnoreCase(mStock) && (ret.getLocations().get(mSelectedLocationIndex).getShelfPiece() > -1 || ret.getLocations().get(mSelectedLocationIndex).getShelfCase() > -1 || ret.getLocations().get(mSelectedLocationIndex).getShelfOuter() > -1 || ret.getLocations().get(mSelectedLocationIndex).getWHPiece() > 0 || ret.getLocations().get(mSelectedLocationIndex).getWHCase() > 0 || ret.getLocations().get(mSelectedLocationIndex).getWHOuter() > 0))
                || (generaltxt.equalsIgnoreCase(mNearExpiryTag) && ret.getIsNearExpiryTaggedProduct() == 1)
                || (generaltxt.equalsIgnoreCase(mFocusBrand3) && ret.getIsFocusBrand3() == 1)
                || (generaltxt.equalsIgnoreCase(mFocusBrand4) && ret.getIsFocusBrand4() == 1)
                || (generaltxt.equalsIgnoreCase(mSMP) && ret.getIsSMP() == 1)
                || (generaltxt.equalsIgnoreCase(mCompertior) && ret.getOwn() == 0)
                || (generaltxt.equalsIgnoreCase(mShelf) && (ret.getLocations().get(mSelectedLocationIndex).getShelfCase() > -1 || ret.getLocations().get(mSelectedLocationIndex).getShelfPiece() > -1 || ret.getLocations().get(mSelectedLocationIndex).getShelfOuter() > -1));
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

        if (bmodel.configurationMasterHelper.SHOW_SPL_FILTER && !bmodel.configurationMasterHelper.IS_SPL_FILTER_TAB) {
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

        menu.findItem(R.id.menu_survey).setVisible(false);
        menu.findItem(R.id.menu_reason).setVisible(bmodel.configurationMasterHelper.floating_np_reason_photo);
        menu.findItem(R.id.menu_barcode).setVisible(bmodel.configurationMasterHelper.IS_BAR_CODE);

        menu.findItem(R.id.menu_fivefilter).setVisible(false);

        if (bmodel.productHelper.isFilterAvaiable("MENU_STK_ORD")) {
            menu.findItem(R.id.menu_fivefilter).setVisible(true);
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
                        bmodel.saveModuleCompletion(OrderedFlag, true);
                        bmodel.outletTimeStampHelper
                                .updateTimeStampModuleWise(DateTimeUtils.now(DateTimeUtils.TIME));
                        /*startActivity(new Intent(OrderNewOutlet.this,
                                HomeScreenTwo.class));*/
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

            FragmentManager fm = getSupportFragmentManager();
            SpecialFilterFragment frag = (SpecialFilterFragment) fm
                    .findFragmentByTag("generalfilter");
            FragmentTransaction ft = fm
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

            FragmentManager fm = getSupportFragmentManager();
            FilterFiveFragment<?> frag = (FilterFiveFragment<?>) fm
                    .findFragmentByTag("Fivefilter");
            FragmentTransaction ft = fm
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
        super.onDestroy();
        unbindDrawables(findViewById(R.id.root));
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

    @Override
    public boolean onEditorAction(TextView arg0, int arg1, KeyEvent arg2) {
        if (arg1 == EditorInfo.IME_ACTION_DONE) {
            loadSearchedList();
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
                || ret.getLocations().get(mSelectedLocationIndex).getWHCase() > 0 || ret.getLocations().get(mSelectedLocationIndex).getWHOuter() > 0)) || (isDiscount && ret.getIsDiscountable() == 1);
    }

    private boolean hasStockOnly() {
        int siz = bmodel.productHelper
                .getProductMaster().size();
        if (siz == 0)
            return false;
        for (int i = 0; i < siz; ++i) {
            ProductMasterBO product = bmodel.productHelper
                    .getProductMaster().get(i);
            int siz1 = product.getLocations().size();
            for (int j = 0; j < siz1; j++) {
                if (product.getLocations().get(j).getShelfPiece() > -1
                        || product.getLocations().get(j).getShelfCase() > -1
                        || product.getLocations().get(j).getShelfOuter() > -1
                        || product.getLocations().get(j).getWHPiece() > 0
                        || product.getLocations().get(j).getWHCase() > 0
                        || product.getLocations().get(j).getWHOuter() > 0)
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
            builder = new AlertDialog.Builder(OrderNewOutlet.this);

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

    @Override
    public void updateFromFiveLevelFilter(int mFilteredPid, HashMap<Integer, Integer> mSelectedIdByLevelId, ArrayList<Integer> mAttributeProducts, String mFilterText) {
        String filtertext = getResources().getString(R.string.product_name);
        if (!mFilterText.equals(""))
            filtertext = mFilterText;

        brandbutton = filtertext;
        fiveFilter_productIDs = new ArrayList<>();

        mylist = new Vector<>();
        Vector<ProductMasterBO> items = bmodel.productHelper.getProductMaster();
        if (mAttributeProducts != null) {
            if (mFilteredPid != 0) {
                for (ProductMasterBO productBO : items) {

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
            } else {
                for (int pid : mAttributeProducts) {
                    for (ProductMasterBO productBO : items) {
                        if (pid == SDUtil.convertToInt(productBO.getProductID()) && productBO.getIsSaleable() == 1) {
                            if (bmodel.configurationMasterHelper.IS_LOAD_PRICE_GROUP_PRD_OLY && productBO.getGroupid() == 0)
                                continue;
                            mylist.add(productBO);
                            fiveFilter_productIDs.add(productBO.getProductID());
                        }
                    }
                }
            }
        } else {
            if (mFilterText.length() > 0) {
                for (ProductMasterBO productBO : items) {

                    if (productBO.getIsSaleable() == 1) {
                        if (productBO.getParentHierarchy().contains("/" + mFilteredPid + "/")) {
                            if (bmodel.configurationMasterHelper.IS_LOAD_PRICE_GROUP_PRD_OLY && productBO.getGroupid() == 0)
                                continue;
                            mylist.add(productBO);
                            fiveFilter_productIDs.add(productBO.getProductID());
                        }
                    }
                }
            } else {
                for (ProductMasterBO productBO : items) {

                    if (productBO.getIsSaleable() == 1) {
                        if (bmodel.configurationMasterHelper.IS_LOAD_PRICE_GROUP_PRD_OLY && productBO.getGroupid() == 0)
                            continue;
                        mylist.add(productBO);
                        fiveFilter_productIDs.add(productBO.getProductID());
                    }
                }
            }

        }

//        Applying special filter in product filtered list(mylist)
        if (bmodel.configurationMasterHelper.SHOW_SPL_FILTER) {

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

        mSchedule = new MyAdapter(mylist);

        lvwplist.setAdapter(mSchedule);
        strBarCodeSearch = "ALL";
        updateValue();
        mDrawerLayout.closeDrawers();
        this.mSelectedIdByLevelId = mSelectedIdByLevelId;

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
        ll_spl_filter = findViewById(R.id.ll_spl_filter);
        ll_tab_selection = findViewById(R.id.ll_tab_selection);
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
            ConfigureBO config = bmodel.configurationMasterHelper.getGenFilter().get(i);

            TypedArray typearr = getTheme().obtainStyledAttributes(R.styleable.MyTextView);
            final int color = typearr.getColor(R.styleable.MyTextView_textColor, 0);
            final int indicator_color = typearr.getColor(R.styleable.MyTextView_accentcolor, 0);
            Button tab;
            tab = new Button(this);
            tab.setText(config.getMenuName());
            tab.setTag(config.getConfigCode());
            tab.setGravity(Gravity.CENTER);
            tab.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
            tab.setTextColor(color);
            tab.setMaxLines(1);
            tab.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.font_small));
            tab.setBackgroundColor(ContextCompat.getColor(this, android.R.color.transparent));
            tab.setWidth(width);
            tab.setOnClickListener(new OnClickListener() {
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
        ArrayList<String> seqIDList = new ArrayList<>();
        LinkedHashSet<String> hs = new LinkedHashSet<>();
        Vector<ProductMasterBO> items = mylist;
        Vector<ProductMasterBO> newProductList = new Vector<>();
        for (ProductMasterBO productMasterBO : items) {
            for (int j = 0; j < productMasterBO.getLocations().size(); j++) {
                if (productMasterBO.isRPS() && (productMasterBO.getLocations().get(j).getShelfPiece() == -1
                        || productMasterBO.getLocations().get(j).getShelfCase() == -1
                        || productMasterBO.getLocations().get(j).getShelfOuter() == -1)) {
                    hs.add(productMasterBO.getProductID());
                }
            }
        }

        for (ProductMasterBO productMasterBO : items) {
            if (productMasterBO.isRPS() && productMasterBO.isSBDAcheived())
                hs.add(productMasterBO.getProductID());

        }

        for (ProductMasterBO productMasterBO : items) {
            if (productMasterBO.getIsInitiativeProduct() == 1)
                hs.add(productMasterBO.getProductID());
        }

        for (ProductMasterBO productMasterBO : items) {
            if (productMasterBO.isPromo())
                hs.add(productMasterBO.getProductID());
        }
        seqIDList.addAll(hs);

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
}