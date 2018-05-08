package com.ivy.sd.png.view;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.CardView;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.ivy.cpg.view.digitalcontent.DigitalContentActivity;
import com.ivy.cpg.view.order.DiscountHelper;
import com.ivy.cpg.view.order.OrderSummary;
import com.ivy.cpg.view.order.StockAndOrder;
import com.ivy.cpg.view.salesreturn.SalesReturnEntryActivity;
import com.ivy.sd.png.asean.view.BuildConfig;
import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.bo.ConfigureBO;
import com.ivy.sd.png.bo.LevelBO;
import com.ivy.sd.png.bo.OrderHeader;
import com.ivy.sd.png.bo.ProductMasterBO;
import com.ivy.sd.png.bo.ProductTaggingBO;
import com.ivy.sd.png.commons.IvyBaseActivityNoActionBar;
import com.ivy.sd.png.commons.SDUtil;
import com.ivy.sd.png.model.BrandDialogInterface;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.model.CatalogOrderValueUpdate;
import com.ivy.sd.png.model.HideShowScrollListener;
import com.ivy.sd.png.provider.ConfigurationMasterHelper;
import com.ivy.sd.png.provider.SBDHelper;
import com.ivy.sd.png.util.Commons;
import com.ivy.sd.png.util.DataMembers;
import com.ivy.sd.png.util.ScreenOrientation;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;

/**
 * Created by dharmapriya.k on 10/14/2016,11:34 AM.
 */
public class CatalogOrder extends IvyBaseActivityNoActionBar implements CatalogOrderValueUpdate, BrandDialogInterface, View.OnClickListener, MOQHighlightDialog.savePcsValue {
    private static final String BRAND = "Brand";
    public static final String GENERAL = "General";
    private final String mCommon = "Filt01";
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
    private final String mFocusBrand3 = "Filt20";
    private final String mFocusBrand4 = "Filt21";
    private final String mSMP = "Filt22";
    private final String msih = "Filt13";
    private final String mOOS = "Filt14";
    private final String mNMustSell = "Filt16";
    private final String mStock = "Filt17";
    private final String mDiscount = "Filt18";
    private final String mNearExpiryTag = "Filt19";
    private final String mCompertior = "Filt23";
    //public int mSelectedLocationIndex;
    private RecyclerViewAdapter adapter;
    private HashMap<Integer, Vector<LevelBO>> loadedFilterValues;
    private HashMap<Integer, Integer> mSelectedIdByLevelId = new HashMap<Integer, Integer>();
    private Vector<LevelBO> sequence;
    private Vector<LevelBO> filterValues;
    private Spinadapter spinadapter;
    private ArrayList<Integer> selectedBrands = new ArrayList<>();
    private int SbdDistPre = 0; // Dist stock
    private int sbdDistAchieved = 0;
    private boolean isSbd, isSbdGaps, isOrdered, isPurchased, isInitiative, isOnAllocation, isInStock, isPromo, isMustSell, isFocusBrand,
            isFocusBrand2, isSIH, isOOS, isNMustSell, isStock, isDiscount, isNearExpiryTag, isFocusBrand3, isFocusBrand4, isSMP;
    //private TypedArray typearr;
    private BusinessModel bmodel;

    private RecyclerView pdt_recycler_view;
    private String tempPo, tempRemark, tempRField1, tempRField2;
    private MustSellReasonDialog dialog;
    private Vector<ProductMasterBO> mylist;
    private String brandbutton, generalbutton;
    private int mSelectedBrandID = 0;
    private String strBarCodeSearch = "ALL";


    private Toolbar toolbar;

    private Vector<ProductMasterBO> asyncList = new Vector<>();

    private ArrayList<Integer> brandIds = new ArrayList<>();
    private LinearLayout bottom_layout;
    private Animation slide_down, slide_up;
    private Button btn_filter_popup, btn_search, btn_clear;
    private EditText search_txt;
    private CardView search_toolbar;
    private String searchedtext = "";
    private ArrayList<String> mSearchTypeArray = new ArrayList<>();

    private int categoryIndex, brandIndex;
    private double totalvalue = 0;
    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mDrawerToggle;
    private HashMap<String, String> mSelectedFilterMap = new HashMap<String, String>();
    private String OrderedFlag, screenCode;

    private TextView totalValueText, lpcText, distValue;
    private ArrayList<String> fiveFilter_productIDs;

    private final String KEY_RECYCLER_STATE = "recycler_state";

    private static Bundle mBundleRecyclerViewState = null;
    private ArrayList<String> productIdList;
    private FrameLayout drawer;
    private Button nextBtn;
    private int totalAllQty = 0;
    private TextView totalQtyTV;
    private File appImageFolderPath;
    public Timer orderTimer;
    private MOQHighlightDialog mMOQHighlightDialog;
    SearchAsync searchAsync;
    private int sbdHistory = 0;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_catalog_order);

        bmodel = (BusinessModel) getApplicationContext();
        bmodel.setContext(this);

        overridePendingTransition(R.anim.trans_left_in, R.anim.trans_left_out);

        pdt_recycler_view = (RecyclerView) findViewById(R.id.pdt_recycler_view);

        search_toolbar = (CardView) findViewById(R.id.search_toolbar);
        bottom_layout = (LinearLayout) findViewById(R.id.bottom_layout);

        search_txt = (EditText) search_toolbar.findViewById(R.id.search_txt);

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);

        totalValueText = (TextView) findViewById(R.id.totalValue);
        lpcText = (TextView) findViewById(R.id.lcp);
        distValue = (TextView) findViewById(R.id.distValue);
        totalQtyTV = (TextView) findViewById(R.id.tv_totalqty);

        btn_filter_popup = (Button) search_toolbar.findViewById(R.id.btn_filter_popup);
        btn_filter_popup.setOnClickListener(this);

        btn_search = (Button) search_toolbar.findViewById(R.id.btn_search);
        btn_search.setOnClickListener(this);

        btn_clear = (Button) search_toolbar.findViewById(R.id.btn_clear);
        btn_clear.setOnClickListener(this);

        nextBtn = (Button) findViewById(R.id.btn_next);
        nextBtn.setOnClickListener(this);
        nextBtn.setTypeface(bmodel.configurationMasterHelper.getFontBaloobhai(ConfigurationMasterHelper.FontType.REGULAR));

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }

        setScreenTitle("" + bmodel.mSelectedActivityName);

        appImageFolderPath = bmodel.synchronizationHelper.getStorageDir(getResources().getString(R.string.app_name));

        // set a custom shadow that overlays the main content when the drawer
        // opens
        mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow,
                GravityCompat.START);
        mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow,
                GravityCompat.END);

        mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);

        screenCode = HomeScreenTwo.MENU_CATALOG_ORDER;
        OrderedFlag = HomeScreenTwo.MENU_CATALOG_ORDER;
        SBDHelper.getInstance(this).calculateSBDDistribution(); //sbd calculation
        sbdHistory = SBDHelper.getInstance(this).getHistorySBD(); // sbd history

        Bundle extras = getIntent().getExtras();
        if (savedInstanceState == null) {
            if (extras != null) {
                OrderedFlag = (extras.getString("OrderFlag") == null ? OrderedFlag
                        : extras.getString("OrderFlag"));
                screenCode = (extras.getString("ScreenCode") == null ? screenCode
                        : extras.getString("ScreenCode"));
                tempPo = (extras.getString("tempPo") == null ? "" : extras
                        .getString("tempPo"));
                tempRemark = (extras.getString("tempRemark") == null ? ""
                        : extras.getString("tempRemark"));
                tempRField1 = (extras.getString("tempRField1") == null ? ""
                        : extras.getString("tempRField1"));
                tempRField2 = (extras.getString("tempRField2") == null ? ""
                        : extras.getString("tempRField2"));
            }


        } else {
            OrderedFlag = (String) (savedInstanceState
                    .getSerializable("OrderFlag") == null ? OrderedFlag
                    : savedInstanceState.getSerializable("OrderFlag"));
            screenCode = (String) (savedInstanceState
                    .getSerializable("ScreenCode") == null ? screenCode
                    : savedInstanceState.getSerializable("ScreenCode"));
            tempPo = (String) (savedInstanceState.getSerializable("tempPo") == null ? ""
                    : savedInstanceState.getSerializable("tempPo"));
            tempRemark = (String) (savedInstanceState
                    .getSerializable("tempRemark") == null ? ""
                    : savedInstanceState.getSerializable("tempRemark"));
            tempRField1 = (String) (savedInstanceState
                    .getSerializable("tempRField1") == null ? ""
                    : savedInstanceState.getSerializable("tempRField1"));
            tempRField2 = (String) (savedInstanceState
                    .getSerializable("tempRField2") == null ? ""
                    : savedInstanceState.getSerializable("tempRField2"));

        }

        drawer = (FrameLayout) findViewById(R.id.right_drawer);
        int width = getResources().getDisplayMetrics().widthPixels;
        DrawerLayout.LayoutParams params = (android.support.v4.widget.DrawerLayout.LayoutParams) drawer.getLayoutParams();
        params.width = width;
        drawer.setLayoutParams(params);

        // ActionBarDrawerToggle ties together the the proper interactions
        // between the sliding drawer and the action bar app icon
        mDrawerToggle = new ActionBarDrawerToggle(this, /* host Activity */
                mDrawerLayout, /* DrawerLayout object */
                R.string.ok, /* "open drawer" description for accessibility */
                R.string.close /* "close drawer" description for accessibility */
        ) {
            public void onDrawerClosed(View view) {
                setScreenTitle("" + bmodel.mSelectedActivityName);
                supportInvalidateOptionsMenu();
            }

            public void onDrawerOpened(View drawerView) {
                setScreenTitle(getResources().getString(R.string.filter));
                supportInvalidateOptionsMenu();
            }
        };
        mDrawerLayout.setDrawerListener(mDrawerToggle);

        mSearchTypeArray = new ArrayList<String>();
        mSearchTypeArray.add(getResources().getString(R.string.product_name));
        mSearchTypeArray.add(getResources().getString(R.string.order_gcas));
        mSearchTypeArray.add(getResources().getString(
                R.string.order_dialog_barcode));

        // Following lines will call method to load products with appropriate filters.
        try {
            if (OrderedFlag.equals("FromSummary") && bmodel.configurationMasterHelper.SHOW_SPL_FILTER) {
                mSelectedFilterMap.put("General", mOrdered);
                updateGeneralText(mOrdered);
            } else if (bmodel.configurationMasterHelper.SHOW_SPL_FILTER) {
                String defaultFilter = bmodel.configurationMasterHelper.getDefaultFilter();
                mSelectedFilterMap.put("General", defaultFilter);
                updateGeneralText(defaultFilter);
            } else {
                mSelectedFilterMap.put("General", GENERAL);
                updateGeneralText(GENERAL);
            }
        } catch (Exception e) {
            Commons.printException(e);
        }

        ((TextView) findViewById(R.id.totalText)).setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
        ((TextView) findViewById(R.id.totalValue)).setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
        ((TextView) findViewById(R.id.lpc_title)).setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
        ((TextView) findViewById(R.id.lcp)).setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
        ((TextView) findViewById(R.id.distText)).setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
        ((TextView) findViewById(R.id.distValue)).setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));

        if (!bmodel.configurationMasterHelper.SHOW_LPC_ORDER) {
            findViewById(R.id.ll_lpc).setVisibility(View.GONE);
        }

        if (bmodel.configurationMasterHelper.HIDE_ORDER_DIST) {
            findViewById(R.id.ll_dist).setVisibility(View.GONE);
        }

        if (bmodel.configurationMasterHelper.IS_ENABLE_PRODUCT_TAGGING_VALIDATION) {
            bmodel.fitscoreHelper.getTaggingDetails("MAX_ORD_VAL"); //MAX_ORD_VAL
        }

        search_txt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                if (search_txt.getText().toString().length() < 3) {
                    mylist.clear();
                }
                if (searchAsync.getStatus() == AsyncTask.Status.RUNNING) {
                    searchAsync.cancel(true);
                }
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                searchedtext = s.toString();
                if (searchedtext.length() == 0 || searchedtext.equals("")) {
                    btn_search.setVisibility(View.VISIBLE);
                    btn_clear.setVisibility(View.GONE);
                    loadProductList();
                } else {
                    btn_search.setVisibility(View.GONE);
                    btn_clear.setVisibility(View.VISIBLE);
                    if (searchedtext.length() >= 3) {
                        searchAsync = new SearchAsync();
                        searchAsync.execute();
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        GridLayoutManager gridLayoutManager;
        if (ScreenOrientation.isCatalogDevice(CatalogOrder.this)) {
            gridLayoutManager = new GridLayoutManager(getApplicationContext(), 2);
        } else {
            gridLayoutManager = new GridLayoutManager(getApplicationContext(), 1);
        }

        if (pdt_recycler_view != null) {
            pdt_recycler_view.setHasFixedSize(true);
            pdt_recycler_view.setItemViewCacheSize(10);
            pdt_recycler_view.setDrawingCacheEnabled(true);
            pdt_recycler_view.setItemAnimator(new DefaultItemAnimator());
            pdt_recycler_view.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);
        }
        pdt_recycler_view.setLayoutManager(gridLayoutManager);

        slide_down = AnimationUtils.loadAnimation(getApplicationContext(),
                R.anim.slide_down);
        slide_up = AnimationUtils.loadAnimation(getApplicationContext(),
                R.anim.slide_up);
        pdt_recycler_view.addOnScrollListener(new HideShowScrollListener() {
            @Override
            public void onHide() {
                if (bottom_layout.getVisibility() == View.VISIBLE) {
                    bottom_layout.startAnimation(slide_down);
                    bottom_layout.setVisibility(View.GONE);
                }
            }

            @Override
            public void onShow() {
                if (bottom_layout.getVisibility() == View.GONE) {
                    bottom_layout.setVisibility(View.VISIBLE);
                    bottom_layout.startAnimation(slide_up);
                }
            }

            @Override
            public void onScrolled() {
                // To load more data
            }
        });


        //mylist = bmodel.productHelper.getProductMaster();
        //generalbutton = GENERAL;
        //brandbutton = BRAND;
        //if (mylist == null) {
        //  bmodel.showAlert(getResources().getString(R.string.no_products_exists),0);
        //  return;
        //}

        getMandatoryFilters();
        //Following line not required.
        //updatebrandtext(BRAND, -1);

        //FUN07 - Bill wise discount
        if (bmodel.configurationMasterHelper.SHOW_STORE_WISE_DISCOUNT_DLG) {
            DiscountHelper.getInstance(this).setMinimumRangeAsBillWiseDiscount();
        }

        mSelectedIdByLevelId = (HashMap<Integer, Integer>) getIntent().getSerializableExtra("FiveFilter");
        productIdList = getIntent().getStringArrayListExtra("ProductIdList");
        if (productIdList != null && productIdList.size() > 0) {
            mylist = new Vector<>();
            for (String productid : productIdList) {
                ProductMasterBO productBO = bmodel.productHelper.getProductMasterBOById(productid);
                if (productBO != null) {
                    mylist.add(productBO);
                }
            }
            adapter = new RecyclerViewAdapter(mylist);
            pdt_recycler_view.setAdapter(adapter);

            //Loadmore(0);
            brandIds = null;

        }

        //for parital order save based on interval
        if (bmodel.configurationMasterHelper.IS_TEMP_ORDER_SAVE) {
            long timeInterval = bmodel.configurationMasterHelper.tempOrderInterval * 1000;
            orderTimer = new Timer();
            orderTimer.scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run() {
                    bmodel.insertTempOrder();
                }

            }, 0, timeInterval);

        }
        searchAsync = new SearchAsync();
    }


    @Override
    protected void onPause() {
        super.onPause();

        // save RecyclerView state
        mBundleRecyclerViewState = new Bundle();
        Parcelable listState = pdt_recycler_view.getLayoutManager().onSaveInstanceState();
        mBundleRecyclerViewState.putParcelable(KEY_RECYCLER_STATE, listState);
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        mBundleRecyclerViewState = null;
        if (pdt_recycler_view != null) {
            pdt_recycler_view.setItemAnimator(null);
            pdt_recycler_view.setAdapter(null);
            pdt_recycler_view = null;
        }
        if (orderTimer != null) {
            orderTimer.cancel();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        // restore RecyclerView state
        if (mBundleRecyclerViewState != null) {
            Parcelable listState = mBundleRecyclerViewState.getParcelable(KEY_RECYCLER_STATE);
            pdt_recycler_view.getLayoutManager().onRestoreInstanceState(listState);
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

            adapter = new RecyclerViewAdapter(mylist);
            pdt_recycler_view.setAdapter(adapter);
        }
    }

    private void loadSearchedList() {

        Commons.print("Search method called.");

        Vector<ProductMasterBO> productMasterList = bmodel.productHelper
                .getProductMaster();
        if (productMasterList == null) {
            bmodel.showAlert(
                    getResources().getString(R.string.no_products_exists),
                    0);
            return;
        }
        int siz = productMasterList.size();
        mylist = new Vector<>();
        String mSelectedFilter = bmodel.getProductFilter();
        for (int i = 0; i < siz; ++i) {
            ProductMasterBO ret = productMasterList.elementAt(i);

            // For breaking search..
            if (searchAsync.isCancelled()) {
                break;
            }

            if (!bmodel.configurationMasterHelper.IS_STOCK_AVAILABLE_PRODUCTS_ONLY
                    || (bmodel.configurationMasterHelper.IS_STOCK_AVAILABLE_PRODUCTS_ONLY && bmodel.getRetailerMasterBO().getIsVansales() == 1
                    && ret.getSIH() > 0)
                    || (bmodel.configurationMasterHelper.IS_STOCK_AVAILABLE_PRODUCTS_ONLY && bmodel.getRetailerMasterBO().getIsVansales() == 0 && ret.getWSIH() > 0)) {

                if (mSelectedFilter.equals(getResources().getString(
                        R.string.order_dialog_barcode))) {

                    if (ret.getBarCode() != null
                            && (ret.getBarCode().toLowerCase()
                            .contains(searchedtext.toLowerCase())
                            || ret.getCasebarcode().toLowerCase().
                            contains(searchedtext.toLowerCase())
                            || ret.getOuterbarcode().toLowerCase().
                            contains(searchedtext.toLowerCase())) && ret.getIsSaleable() == 1) {

                        if (generalbutton.equals(GENERAL) && brandbutton.equals(BRAND))//No filters selected
                            mylist.add(ret);
                        else if (applyProductAndSpecialFilter(ret))
                            mylist.add(ret);


                    }

                } else if (mSelectedFilter.equals(getResources().getString(
                        R.string.order_gcas))) {
                    if (ret.getRField1() != null && ret.getRField1()
                            .toLowerCase()
                            .contains(
                                    searchedtext
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
                                    searchedtext
                                            .toLowerCase()) && ret.getIsSaleable() == 1)
                        if (generalbutton.equals(GENERAL) && brandbutton.equals(BRAND))//No filters selected
                            mylist.add(ret);
                        else if (applyProductAndSpecialFilter(ret))
                            mylist.add(ret);
                }
            }

        }

    }

    @Override
    public void onStart() {

        super.onStart();
        loadedFilterValues = bmodel.productHelper.getFiveLevelFilters();
        if (bmodel.productHelper.getSequenceValues() != null && bmodel.productHelper.getSequenceValues().size() != 0) {
            sequence = bmodel.productHelper.getSequenceValues();
            for (int i = 0; i < sequence.size(); i++) {
                if (sequence.get(i).getLevelName().equals("Category")) {
                    categoryIndex = i;
                } else if (sequence.get(i).getLevelName().equals("Brand")) {
                    brandIndex = i;
                }
            }

            if (sequence.size() > 0) {
                int levelID = sequence.get(categoryIndex).getProductID();
                filterValues = new Vector<LevelBO>();
                filterValues.addAll(loadedFilterValues.get(levelID));
                LevelBO levelBO = new LevelBO();
                levelBO.setLevelName("All");
                //levelBO.setProductLevel("0");
                levelBO.setParentID(0);
                filterValues.add(0, levelBO);
                spinadapter = new Spinadapter(CatalogOrder.this, android.R.layout.simple_spinner_item,//loadedFilterValues.get(levelID));
                        filterValues);
            }
        }
        mDrawerLayout.closeDrawer(GravityCompat.END);

    }

    @Override
    public void updateMultiSelectionBrand(List<String> filtername, List<Integer> filterid) {

    }

    @Override
    public void updateMultiSelectionCategory(List<Integer> mcatgory) {

    }


    @Override
    public void updateBrandText(String filtertext, int bid) {

        mSelectedBrandID = bid;

        Commons.print("updatebrandtext method called with : " + filtertext);

        Commons.print("Stock and order  :," + " update brand text called :"
                + bmodel.productHelper.getProductMaster().size()
                + ">>>>>>>>>>>>>>>>>>>>>" + bid);

        try {
            // Close the drawer
            mDrawerLayout.closeDrawers();

            // Change the Brand button Name
            brandbutton = filtertext;

            // Consider generalbutton text if it is dependent filter.
            String generaltxt = generalbutton;

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
                ProductMasterBO productMasterBO = items.elementAt(i);
                if (productMasterBO.getBarCode().equals(strBarCodeSearch)
                        || productMasterBO.getCasebarcode().equals(strBarCodeSearch)
                        || productMasterBO.getOuterbarcode().equals(strBarCodeSearch)
                        || strBarCodeSearch.equals("ALL")) {

                    if (!bmodel.configurationMasterHelper.IS_STOCK_AVAILABLE_PRODUCTS_ONLY
                            || (bmodel.configurationMasterHelper.IS_STOCK_AVAILABLE_PRODUCTS_ONLY && bmodel.getRetailerMasterBO().getIsVansales() == 1
                            && productMasterBO.getSIH() > 0)
                            || (bmodel.configurationMasterHelper.IS_STOCK_AVAILABLE_PRODUCTS_ONLY && bmodel.getRetailerMasterBO().getIsVansales() == 0 && productMasterBO.getWSIH() > 0)) {

                        if ((bid == -1 || bid == productMasterBO.getParentid()) && generaltxt.equals(GENERAL) && productMasterBO.getIsSaleable() == 1) {
                            // product filter alone
                            if (searchedtext.length() >= 3) {
                                if (isUserEntryFilterSatisfied(productMasterBO))
                                    mylist.add(productMasterBO);
                            } else {
                                mylist.add(productMasterBO);
                            }
                        } else if ((bid == -1 || bid == productMasterBO.getParentid()) && !generaltxt.equals(GENERAL) && productMasterBO.getIsSaleable() == 1) {
                            //special(GENERAL) filter with or without product filter
                            if (isSpecialFilterAppliedProduct(generaltxt, productMasterBO)) {
                                if (searchedtext.length() >= 3) {
                                    if (isUserEntryFilterSatisfied(productMasterBO)) {
                                        mylist.add(productMasterBO);
                                    }
                                } else {
                                    mylist.add(productMasterBO);
                                }
                            }

                        }
                    }


                }
            }
            adapter = new RecyclerViewAdapter(mylist);
            pdt_recycler_view.setAdapter(adapter);
            brandIds = null;


            strBarCodeSearch = "ALL";
            updateValue();
        } catch (Exception e) {
            Commons.printException(e);
        }
    }

    private boolean isUserEntryFilterSatisfied(ProductMasterBO ret) {
        String mSelectedFilter = bmodel.getProductFilter();
        if (mSelectedFilter.equals(getResources().getString(
                R.string.order_dialog_barcode))) {
            if (ret.getBarCode() != null
                    && (ret.getBarCode().toLowerCase()
                    .contains(searchedtext.toLowerCase())
                    || ret.getCasebarcode().toLowerCase().
                    contains(searchedtext.toLowerCase())
                    || ret.getOuterbarcode().toLowerCase().
                    contains(searchedtext.toLowerCase()) && ret.getIsSaleable() == 1)) {
                return true;
            }
        } else if (mSelectedFilter.equals(getResources().getString(
                R.string.order_gcas))) {
            if (ret.getRField1() != null && ret.getRField1()
                    .toLowerCase()
                    .contains(
                            searchedtext
                                    .toLowerCase()) && ret.getIsSaleable() == 1)
                return true;

        } else if (mSelectedFilter.equals(getResources().getString(
                R.string.product_name))) {
            if (ret.getProductShortName() != null && ret.getProductShortName()
                    .toLowerCase()
                    .contains(
                            searchedtext
                                    .toLowerCase()) && ret.getIsSaleable() == 1)
                return true;
        }
        return false;
    }

    private boolean isSpecialFilterAppliedProduct(String generaltxt, ProductMasterBO ret) {
        if (generaltxt.equalsIgnoreCase(mSbd) && ret.isRPS()
                || (generaltxt.equalsIgnoreCase(mOrdered) && ret.getOrderedPcsQty() > 0)
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
                || (generaltxt.equalsIgnoreCase(mStock) && ret.getLocations().get(0).getShelfPiece() > 0)
                || (generaltxt.equalsIgnoreCase(mNearExpiryTag) && ret.getIsNearExpiryTaggedProduct() == 1)
                || (generaltxt.equalsIgnoreCase(mFocusBrand3) && ret.getIsFocusBrand3() == 1)
                || (generaltxt.equalsIgnoreCase(mFocusBrand4) && ret.getIsFocusBrand4() == 1)
                || (generaltxt.equalsIgnoreCase(mSMP) && ret.getIsSMP() == 1)
                || (generaltxt.equalsIgnoreCase(mCompertior) && ret.getOwn() == 0)) {
            return true;
        }
        return false;

    }


    private boolean applyCommonFilterConfig(ProductMasterBO ret) {
        if ((isSbd && ret.isRPS()) || (isSbdGaps && ret.isRPS() && !ret.isSBDAcheived()) || (isOrdered && ret.getOrderedPcsQty() > 0)
                || (isPurchased && ret.getIsPurchased() == 1) || (isInitiative && ret.getIsInitiativeProduct() == 1) || (isOnAllocation && ret.isAllocation() == 1
                && bmodel.configurationMasterHelper.IS_SIH_VALIDATION) || (isInStock && ret.getWSIH() > 0) || (isPromo && ret.isPromo()) || (isMustSell && ret.getIsMustSell() == 1)
                || (isFocusBrand && ret.getIsFocusBrand() == 1) || (isFocusBrand2 && ret.getIsFocusBrand2() == 1) || (isSIH && ret.getSIH() > 0) || (isOOS && ret.getOos() == 0)
                || (isNMustSell && ret.getIsNMustSell() == 1) || (isStock && ret.getLocations().get(0).getShelfPiece() > 0) || (isDiscount && ret.getIsDiscountable() == 1)) {

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
                else if (bo.getConfigCode().equals(mNearExpiryTag))
                    isNearExpiryTag = true;
                else if (bo.getConfigCode().equals(mFocusBrand3))
                    isFocusBrand3 = true;
                else if (bo.getConfigCode().equals(mFocusBrand4))
                    isFocusBrand4 = true;
                else if (bo.getConfigCode().equals(mSMP))
                    isSMP = true;
            }
        }
    }


    @Override
    public void updateGeneralText(String filterText) {
        // set the spl filter name on the button for display
        generalbutton = filterText;

        Commons.print("updategeneraltext method called with : " + filterText);

        // clearing fivefilterList
        fiveFilter_productIDs = null;
        if (mSelectedIdByLevelId != null)
            mSelectedIdByLevelId.clear();

        updateBrandText(BRAND, -1);
    }

    @Override
    public void updateCancel() {
        mDrawerLayout.closeDrawers();
    }

    @Override
    public void loadStartVisit() {

    }

    @Override
    public void updateFromFiveLevelFilter(Vector<LevelBO> parentIdList) {
        mylist = new Vector<>();

        Vector<ProductMasterBO> items = bmodel.productHelper.getProductMaster();
        for (LevelBO levelBO : parentIdList) {
            for (ProductMasterBO productBO : items) {
                if (productBO.getIsSaleable() == 1) {
                    if (levelBO.getProductID() == Integer.parseInt(productBO.getProductID())) {
                        //  filtertext = levelBO.getLevelName();
                        mylist.add(productBO);
                    }
                }
            }
        }

        adapter = new RecyclerViewAdapter(mylist);
        pdt_recycler_view.setAdapter(adapter);
        strBarCodeSearch = "ALL";
    }

    @Override
    public void updateFromFiveLevelFilter(Vector<LevelBO> parentidList, HashMap<Integer, Integer> mSelectedIdByLevelId, ArrayList<Integer> mAttributeProducts, String filtertext) {
        //String filtertext = getResources().getString(R.string.product_name);
        /*if (!filter.equals(""))
            filtertext = filter;*/

        brandbutton = filtertext;
        fiveFilter_productIDs = new ArrayList<>();


        int count = 0;
        mylist = new Vector<>();
        Vector<ProductMasterBO> items = bmodel.productHelper.getProductMaster();
        if (mAttributeProducts != null) {
            count = 0;
            if (parentidList.size() > 0) {
                for (LevelBO levelBO : parentidList) {
                    count++;
                    for (ProductMasterBO productBO : items) {
                        if (!bmodel.configurationMasterHelper.IS_STOCK_AVAILABLE_PRODUCTS_ONLY
                                || (bmodel.configurationMasterHelper.IS_STOCK_AVAILABLE_PRODUCTS_ONLY && bmodel.getRetailerMasterBO().getIsVansales() == 1
                                && productBO.getSIH() > 0)
                                || (bmodel.configurationMasterHelper.IS_STOCK_AVAILABLE_PRODUCTS_ONLY && bmodel.getRetailerMasterBO().getIsVansales() == 0 && productBO.getWSIH() > 0)) {


                            if (productBO.getIsSaleable() == 1 && levelBO.getProductID() == productBO.getParentid()) {
                                // here we get all products mapped to parent id list, then that product will be added only if it is mapped to selected attribute
                                if (mAttributeProducts.contains(Integer.parseInt(productBO.getProductID()))) {
                                    //filtertext = levelBO.getLevelName();
                                    mylist.add(productBO);
                                    fiveFilter_productIDs.add(productBO.getProductID());
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
                                || (bmodel.configurationMasterHelper.IS_STOCK_AVAILABLE_PRODUCTS_ONLY && bmodel.getRetailerMasterBO().getIsVansales() == 0 && productBO.getWSIH() > 0)) {

                            if (pid == Integer.parseInt(productBO.getProductID()) && productBO.getIsSaleable() == 1) {
                                mylist.add(productBO);
                                fiveFilter_productIDs.add(productBO.getProductID());
                            }
                        }
                    }
                }
            }
        } else {


            for (ProductMasterBO productBO : items) {
                for (LevelBO levelBO : parentidList) {
                    if (!bmodel.configurationMasterHelper.IS_STOCK_AVAILABLE_PRODUCTS_ONLY
                            || (bmodel.configurationMasterHelper.IS_STOCK_AVAILABLE_PRODUCTS_ONLY && bmodel.getRetailerMasterBO().getIsVansales() == 1
                            && productBO.getSIH() > 0)
                            || (bmodel.configurationMasterHelper.IS_STOCK_AVAILABLE_PRODUCTS_ONLY && bmodel.getRetailerMasterBO().getIsVansales() == 0 && productBO.getWSIH() > 0)) {

                        if (productBO.getIsSaleable() == 1) {
                            if (levelBO.getProductID() == productBO.getParentid()) {
                                //  filtertext = levelBO.getLevelName();
                                mylist.add(productBO);
                                fiveFilter_productIDs.add(productBO.getProductID());
                                break;
                            }
                        }
                    }
                }
            }
        }


//        Applying special filter in product filtered list(mylist)
        if (bmodel.configurationMasterHelper.SHOW_SPL_FILTER) {

            Vector<ProductMasterBO> temp = new Vector<ProductMasterBO>();
            String generaltxt = generalbutton;
            for (ProductMasterBO ret : mylist) {
                if (generaltxt.equals(GENERAL))//No special filters selected
                {
                    if (searchedtext.length() >= 3) {// User entry filter
                        if (isUserEntryFilterSatisfied(ret))
                            temp.add(ret);
                    } else
                        temp.add(ret);

                } else {
                    if (isSpecialFilterAppliedProduct(generaltxt, ret)) { //special filter selected

                        if (searchedtext.length() >= 3) {
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

        adapter = new RecyclerViewAdapter(mylist);
        pdt_recycler_view.setAdapter(adapter);


        strBarCodeSearch = "ALL";
        updateValue();
        mDrawerLayout.closeDrawers();
        this.mSelectedIdByLevelId = mSelectedIdByLevelId;

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.order_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        // Change color if Filter is selected
        if (!generalbutton.equals(GENERAL))
            menu.findItem(R.id.menu_spl_filter).setIcon(
                    R.drawable.ic_action_star_select);

        /*if (!brandbutton.equals(BRAND)) {
            menu.findItem(R.id.menu_product_filter).setIcon(
                    R.drawable.ic_action_filter_select);
        }*/


        // If the nav drawer is open, hide action items related to the content
        // view
        boolean drawerOpen = mDrawerLayout.isDrawerOpen(GravityCompat.END);

        menu.findItem(R.id.menu_fivefilter).setVisible(!drawerOpen);
        menu.findItem(R.id.menu_next).setVisible(false);
        menu.findItem(R.id.menu_spl_filter).setVisible(!drawerOpen);

        menu.findItem(R.id.menu_spl_filter).setVisible(bmodel.configurationMasterHelper.SHOW_SPL_FILTER);

        menu.findItem(R.id.menu_remarks).setVisible(false);
        menu.findItem(R.id.menu_loc_filter).setVisible(false);
        menu.findItem(R.id.menu_survey).setVisible(false);
        menu.findItem(R.id.menu_barcode).setVisible(false);
        menu.findItem(R.id.menu_fivefilter).setVisible(false);
        menu.findItem(R.id.menu_product_filter).setVisible(false);

        if (bmodel.configurationMasterHelper.IS_FIVE_LEVEL_FILTER)
            menu.findItem(R.id.menu_fivefilter).setVisible(true);

        if (bmodel.configurationMasterHelper.IS_FIVE_LEVEL_FILTER && mSelectedIdByLevelId != null) {
            for (Integer id : mSelectedIdByLevelId.keySet()) {
                if (mSelectedIdByLevelId.get(id) > 0) {
                    menu.findItem(R.id.menu_fivefilter).setIcon(
                            R.drawable.ic_action_filter_select);
                    break;

                }
            }
        }

        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int i = item.getItemId();
        if (i == android.R.id.home) {
            if (mDrawerLayout.isDrawerOpen(GravityCompat.END)) {
                mDrawerLayout.closeDrawers();
            } else {
                backButtonClick();
            }
            return true;
        } else if (i == R.id.menu_next) {
            if (bmodel.getOrderHeaderBO() == null)
                bmodel.setOrderHeaderBO(new OrderHeader());

            bmodel.getRetailerMasterBO().setSbdDistStock(SbdDistPre);
            bmodel.getRetailerMasterBO().setSbdDistAchieved(
                    sbdDistAchieved);

            // If Crown Management is Enable , then go for Crown Management
            // Screen and calcuale liability to crown Products also
            if ((bmodel.configurationMasterHelper.SHOW_CROWN_MANAGMENT || bmodel.configurationMasterHelper.SHOW_FREE_PRODUCT_GIVEN)
                    && bmodel.configurationMasterHelper.IS_SIH_VALIDATION) {
                nextButtonClick();
            } else {
                // Other wise check if Bottle Return enable or not,according to
                // that it will work.
                /*if (bmodel.configurationMasterHelper.SHOW_PRODUCTRETURN) {
                    new calculateReturnProductValusAndQty().execute();
                } else {*/
                nextButtonClick();
                //}
            }
            return true;
        } else if (i == R.id.menu_fivefilter) {
            if (bmodel.configurationMasterHelper.IS_UNLINK_FILTERS) {
                generalbutton = GENERAL;
                mSelectedFilterMap.put("General", GENERAL);
            }
            FiveFilterFragment();
            return true;
        } else if (i == R.id.menu_spl_filter) {

            // Get the Special Filter Type 1- Single Selection, 2- Multi
            // Selection
            if (ConfigurationMasterHelper.GET_GENERALFILTET_TYPE == 2)
                generalFilterClickedFragment();
            else
                generalFilterClickedFragment();
            item.setVisible(false);
            supportInvalidateOptionsMenu();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void generalFilterClickedFragment() {
        try {

            //QUANTITY = null;
            Vector<String> vect = new Vector();
            Collections.addAll(vect, getResources().getStringArray(
                    R.array.productFilterArray));

            mDrawerLayout.openDrawer(GravityCompat.END);

            android.support.v4.app.FragmentManager fm = getSupportFragmentManager();
            FilterFragment frag = (FilterFragment) fm
                    .findFragmentByTag("generalfilter");
            android.support.v4.app.FragmentTransaction ft = fm
                    .beginTransaction();
            if (frag != null)
                ft.detach(frag);
            Bundle bundle = new Bundle();

            bundle.putString("filterName", GENERAL);
            bundle.putBoolean("isFormBrand", false);
            bundle.putSerializable("serilizeContent",
                    bmodel.configurationMasterHelper.getGenFilter());

            // set Fragmentclass Arguments
            FilterFragment fragobj = new FilterFragment(mSelectedFilterMap);
            fragobj.setArguments(bundle);
            ft.replace(R.id.right_drawer, fragobj, "generalfilter");
            ft.commit();

        } catch (Exception e) {
            Commons.printException(e);
        }
    }

    private void FiveFilterFragment() {
        try {

            //QUANTITY = null;
            Vector<String> vect = new Vector();
            Collections.addAll(vect, getResources().getStringArray(
                    R.array.productFilterArray));

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
    public void onSaveInstanceState(Bundle savedInstanceState) {

        Bundle extras = getIntent().getExtras();
        screenCode = HomeScreenTwo.MENU_CATALOG_ORDER;
        OrderedFlag = HomeScreenTwo.MENU_CATALOG_ORDER;
        if (extras != null) {
            OrderedFlag = (extras.getString("OrderFlag") == null ? OrderedFlag
                    : extras.getString("OrderFlag"));
            screenCode = (extras.getString("ScreenCode") == null ? screenCode
                    : extras.getString("ScreenCode"));
            tempPo = (extras.getString("tempPo") == null ? "" : extras
                    .getString("tempPo"));
            tempRemark = (extras.getString("tempRemark") == null ? "" : extras
                    .getString("tempRemark"));
            tempRField1 = (extras.getString("tempRField1") == null ? "" : extras
                    .getString("tempRField1"));
            tempRField2 = (extras.getString("tempRField2") == null ? "" : extras
                    .getString("tempRField2"));
            savedInstanceState.putSerializable("OrderFlag", OrderedFlag);
            savedInstanceState.putSerializable("tempPo", tempPo);
            savedInstanceState.putSerializable("tempRemark", tempRemark);
            savedInstanceState.putSerializable("tempRField1", tempRField1);
            savedInstanceState.putSerializable("tempRField2", tempRField2);
            savedInstanceState.putSerializable("ScreenCode", screenCode);

        }
        super.onSaveInstanceState(savedInstanceState);
    }


    private void backButtonClick() {
        try {

            if (bmodel.hasOrder()) {
                showDialog(0);
            } else {

                if (bmodel.configurationMasterHelper.IS_TEMP_ORDER_SAVE)
                    orderTimer.cancel();

                bmodel.productHelper.clearOrderTable();
                if (bmodel.mSelectedModule == 1) {
                    startActivity(new Intent(CatalogOrder.this,
                            HomeScreenActivity.class));
                    finish();
                } else if (bmodel.mSelectedModule == 2) {
                    startActivity(new Intent(CatalogOrder.this,
                            HomeScreenTwo.class));
                    finish();
                } else {
                    bmodel.outletTimeStampHelper
                            .updateTimeStampModuleWise(SDUtil.now(SDUtil.TIME));
                    startActivity(new Intent(CatalogOrder.this,
                            HomeScreenTwo.class));
                    finish();
                }
                overridePendingTransition(R.anim.trans_right_in, R.anim.trans_right_out);
            }
        } catch (Exception e) {
            // TODO: handle exception
            Commons.printException(e);
        }
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        switch (id) {
            case 0:
                AlertDialog.Builder builder = new AlertDialog.Builder(CatalogOrder.this)
                        .setIcon(null)
                        .setCancelable(false)
                        .setTitle(
                                getResources().getString(
                                        R.string.order_not_saved_go_back))
                        .setPositiveButton(getResources().getString(R.string.ok),
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog,
                                                        int whichButton) {

                                        if (bmodel.isEdit()) {
                                            bmodel.productHelper
                                                    .clearOrderTableAndUpdateSIH();
                                        }

                                        if (bmodel.configurationMasterHelper.IS_TEMP_ORDER_SAVE)
                                            orderTimer.cancel();

                                        bmodel.productHelper.clearOrderTable();

                                        if (bmodel.configurationMasterHelper.SHOW_PRODUCTRETURN)
                                            bmodel.productHelper
                                                    .clearBomReturnProductsTable();

                                        // if User comes from Adhoc Screen it again
                                        // redirect to the HomeScreenFragment
                                        if (bmodel.mSelectedModule == 1) {
                                            startActivity(new Intent(
                                                    CatalogOrder.this,
                                                    HomeScreenActivity.class));
                                        } else if (bmodel.mSelectedModule == 2) {
                                            startActivity(new Intent(
                                                    CatalogOrder.this,
                                                    HomeScreenTwo.class));
                                        } else {
                                            startActivity(new Intent(
                                                    CatalogOrder.this,
                                                    HomeScreenTwo.class));
                                        }
                                        finish();
                                        overridePendingTransition(R.anim.trans_right_in, R.anim.trans_right_out);
                                    }
                                })
                        .setNegativeButton(
                                getResources().getString(R.string.cancel),
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog,
                                                        int whichButton) {
                                    }
                                });
                bmodel.applyAlertDialogTheme(builder);
                break;
            case 1:
                AlertDialog.Builder builder1 = new AlertDialog.Builder(CatalogOrder.this)
                        .setIcon(null)
                        .setCancelable(false)
                        .setTitle(
                                getResources().getString(
                                        R.string.do_you_want_to_save_stock))
                        .setPositiveButton(getResources().getString(R.string.ok),
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog,
                                                        int whichButton) {
                                        bmodel.saveClosingStock(true);

                                        Toast.makeText(
                                                CatalogOrder.this,
                                                getResources().getString(
                                                        R.string.stock_saved),
                                                Toast.LENGTH_SHORT).show();

                                        startActivity(new Intent(
                                                CatalogOrder.this,
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
        }
        return null;
    }

    private void nextButtonClick() {
        try {

            if (bmodel.configurationMasterHelper.SHOW_STOCK_SP
                    && bmodel.configurationMasterHelper.IS_MUST_SELL_STK
                    && !bmodel.productHelper.isMustSellFilledStockCheck(false)) {
                Toast.makeText(this, R.string.fill_must_sell, Toast.LENGTH_SHORT).show();
                return;
            }

            if (bmodel.hasOrder()) {

                //if this config IS_RFIELD1_ENABLED enabled below code will work
                //and
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
                        new MOQConfigEnabled().execute();
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
                                Toast.makeText(CatalogOrder.this, product.getProductName() + " exceeded Allocation", Toast.LENGTH_LONG).show();
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

                if (bmodel.configurationMasterHelper.IS_MUST_SELL
                        && !bmodel.productHelper.isMustSellFilled()) {
                    if (dialog == null) {
                        dialog = new MustSellReasonDialog(
                                CatalogOrder.this, false,
                                diagDismissListen, bmodel);
                    }
                    dialog.show();
                    return;
                }
                if (bmodel.configurationMasterHelper.SHOW_INVOICE_CREDIT_BALANCE)
                    if (bmodel.getRetailerMasterBO().getCredit_balance() != -1
                            && totalvalue > bmodel.getRetailerMasterBO()
                            .getCredit_balance())
                        bmodel.showAlert(
                                getResources()
                                        .getString(
                                                R.string.order_exceeds_credit_balance),
                                0);
                    else
                        nextBtnSubTask();
                else
                    nextBtnSubTask();

            } else {
                if (hasStockOnly()) {
                    showDialog(1);
                } else
                    bmodel.showAlert(
                            getResources().getString(
                                    R.string.no_items_added), 0);
            }
        } catch (Exception e) {
            Commons.printException(e);
        }
    }

    DialogInterface.OnCancelListener diagDismissListen = new DialogInterface.OnCancelListener() {

        @Override
        public void onCancel(DialogInterface dialog) {
            nextBtnSubTask();
        }
    };

    public boolean hasStockOnly() {
        int siz = mylist.size();
        if (siz == 0)
            return false;
        for (int i = 0; i < siz; ++i) {
            ProductMasterBO product = mylist.get(i);

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

    public void nextBtnSubTask() {
        if (bmodel.mSelectedModule != 3)
            bmodel.outletTimeStampHelper.updateTimeStampModuleWise(SDUtil
                    .now(SDUtil.TIME));
        if (bmodel.configurationMasterHelper.SHOW_BATCH_ALLOCATION && bmodel.configurationMasterHelper.IS_SIH_VALIDATION) {
            if (bmodel.productHelper.isSIHAvailable()) {
                bmodel.configurationMasterHelper.setBatchAllocationtitle("Batch Allocation");
                bmodel.batchAllocationHelper.loadOrderedBatchList(bmodel.productHelper
                        .getProductMaster());
                Intent intent = new Intent(CatalogOrder.this,
                        BatchAllocation.class);
                intent.putExtra("OrderFlag", "Nothing");
                intent.putExtra("ScreenCode", screenCode);
                startActivity(intent);
                finish();
            } else {
                Toast.makeText(
                        CatalogOrder.this,
                        "Ordered value exceeds SIH value.Please edit the order",
                        Toast.LENGTH_SHORT).show();
            }

        } else if ((bmodel.configurationMasterHelper.SHOW_CROWN_MANAGMENT || bmodel.configurationMasterHelper.SHOW_FREE_PRODUCT_GIVEN)
                && bmodel.configurationMasterHelper.IS_SIH_VALIDATION) {

            Intent intent = new Intent(CatalogOrder.this,
                    CrownReturnActivity.class);
            intent.putExtra("OrderFlag", "Nothing");
            intent.putExtra("ScreenCode", screenCode);
            startActivity(intent);
            finish();
        } else if (bmodel.configurationMasterHelper.IS_SCHEME_ON
                && bmodel.configurationMasterHelper.IS_SCHEME_SHOW_SCREEN) {
            Intent init = new Intent(CatalogOrder.this, SchemeApply.class);
            init.putExtra("ScreenCode", screenCode);
            init.putExtra("ForScheme", screenCode);
            startActivity(init);
            finish();
        } else if (bmodel.configurationMasterHelper.SHOW_DISCOUNT_ACTIVITY) {
            Intent init = new Intent(CatalogOrder.this, OrderDiscount.class);
            init.putExtra("ScreenCode", screenCode);
            startActivity(init);
            finish();
        } else if (bmodel.configurationMasterHelper.IS_INITIATIVE) {
            Intent init = new Intent(CatalogOrder.this,
                    InitiativeActivity.class);
            init.putExtra("ScreenCode", screenCode);
            startActivity(init);
            finish();
        } else if (bmodel.configurationMasterHelper.IS_PRESENTATION_INORDER) {
            Intent i = new Intent(CatalogOrder.this,
                    DigitalContentActivity.class);
            i.putExtra("ScreenCode", screenCode);
            i.putExtra("FromInit", "Initiative");
            startActivity(i);
            finish();
        } else {
            Intent i = new Intent(CatalogOrder.this, OrderSummary.class);
            i.putExtra("ScreenCode", screenCode);
            startActivity(i);
            finish();
        }

    }

    private void updateValue() {
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

        @Override
        protected Void doInBackground(Void... voids) {
            if (items == null) {
                return null;
            }
            int siz = items.size();
            if (siz == 0)
                return null;

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
                    sbdAchievement += SBDHelper.getInstance(CatalogOrder.this).getAchievedSBD(ret);
                }
            }

            return null;
        }

        @Override
        protected void onPreExecute() {
            totalAllQty = 0;
            lpccount = 0;
            totalvalue = 0;
            items = bmodel.productHelper
                    .getProductMaster();
        }

        @Override
        protected void onPostExecute(Void voids) {
            lpcText.setText(lpccount + "");
            String strFormatValue = bmodel.formatValue(totalvalue) + "";
            totalValueText.setText(strFormatValue);
            totalQtyTV.setText("" + totalAllQty);
            distValue.setText((sbdAchievement + sbdHistory) + "/" + bmodel.getRetailerMasterBO()
                    .getSbdDistributionTarget());
        }
    }

    private void loadProductList() {
        try {

            Vector<ProductMasterBO> items = bmodel.productHelper
                    .getProductMaster();
            int siz = items.size();
            mylist = new Vector<>();
            for (int i = 0; i < siz; ++i) {
                ProductMasterBO ret = items.elementAt(i);

                if (!bmodel.configurationMasterHelper.IS_STOCK_AVAILABLE_PRODUCTS_ONLY
                        || (bmodel.configurationMasterHelper.IS_STOCK_AVAILABLE_PRODUCTS_ONLY && bmodel.getRetailerMasterBO().getIsVansales() == 1
                        && ret.getSIH() > 0)
                        || (bmodel.configurationMasterHelper.IS_STOCK_AVAILABLE_PRODUCTS_ONLY && bmodel.getRetailerMasterBO().getIsVansales() == 0 && ret.getSIH() > 0)) {

                    if (ret.getIsSaleable() == 1) {
                        if (generalbutton.equals(GENERAL) && brandbutton.equals(BRAND))//No filters selected
                            mylist.add(ret);
                        else if (applyProductAndSpecialFilter(ret))
                            mylist.add(ret);
                    }
                }
            }

            adapter = new RecyclerViewAdapter(mylist);
            pdt_recycler_view.setAdapter(adapter);

        } catch (Exception e) {
            Commons.printException(e);
        }
    }

    private boolean applyProductAndSpecialFilter(ProductMasterBO ret) {
        if (!generalbutton.equals(GENERAL) && !brandbutton.equals(BRAND)) {
            // both filter selected
            if (bmodel.configurationMasterHelper.IS_FIVE_LEVEL_FILTER) {
                if (fiveFilter_productIDs != null && fiveFilter_productIDs.contains(ret.getProductID())
                        && isSpecialFilterAppliedProduct(generalbutton, ret))
                    return true;

            } else {
                if (ret.getParentid() == mSelectedBrandID && isSpecialFilterAppliedProduct(generalbutton, ret))
                    return true;
            }
        } else if (!generalbutton.equals(GENERAL) && brandbutton.equals(BRAND)) {
            //special filter alone selected
            if (isSpecialFilterAppliedProduct(generalbutton, ret))
                return true;
        } else if (generalbutton.equals(GENERAL) && !brandbutton.equals(BRAND)) {
            // product filter alone selected
            if (bmodel.configurationMasterHelper.IS_FIVE_LEVEL_FILTER) {
                if (fiveFilter_productIDs != null && fiveFilter_productIDs.contains(ret.getProductID()))
                    return true;

            } else {
                if (ret.getParentid() == mSelectedBrandID)
                    return true;
            }
        }
        return false;
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.btn_filter_popup) {
            AlertDialog.Builder builderSingle = new AlertDialog.Builder(
                    CatalogOrder.this);
            final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(
                    CatalogOrder.this,
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
        } else if (v.getId() == R.id.btn_clear) {
            search_txt.setText("");
            btn_search.setVisibility(View.VISIBLE);
            btn_clear.setVisibility(View.GONE);
            loadProductList();
        } else if (v.getId() == R.id.btn_next) {

            if (bmodel.getOrderHeaderBO() == null)
                bmodel.setOrderHeaderBO(new OrderHeader());

            bmodel.getRetailerMasterBO().setSbdDistStock(SbdDistPre);
            bmodel.getRetailerMasterBO().setSbdDistAchieved(
                    sbdDistAchieved);
            // If Crown Management is Enable , then go for Crown Management
            // Screen and calcuale liability to crown Products also
            if ((bmodel.configurationMasterHelper.SHOW_CROWN_MANAGMENT || bmodel.configurationMasterHelper.SHOW_FREE_PRODUCT_GIVEN)
                    && bmodel.configurationMasterHelper.IS_SIH_VALIDATION) {
                nextButtonClick();
            } else {
                nextButtonClick();
            }
        }

    }

    private boolean checkTaggingDetails(ProductMasterBO productMasterBO) {
        ArrayList<ProductTaggingBO> productTaggingList = bmodel.fitscoreHelper.getProductTaggingList();
        for (ProductTaggingBO productTagging : productTaggingList) {
            float totalQty = (productMasterBO.getOrderedCaseQty() * productMasterBO.getCaseSize())
                    + (productMasterBO.getOrderedPcsQty())
                    + (productMasterBO.getOrderedOuterQty() * productMasterBO.getOutersize());
            if (productMasterBO.getProductID().equals(productTagging.getPid()) &&
                    totalQty > 0
                    && totalQty > Integer.parseInt(productTagging.getToNorm())) {
                return false;
            }
        }
        return true;
    }

    @Override
    public void updateTotalValue(String value) {
        updateValue();
    }

    @Override
    public void saveChanges() {
        adapter.notifyDataSetChanged();
    }

    public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.MyViewHolder> {

        private CustomKeyBoardCatalog dialogCustomKeyBoard;
        private ArrayList<ProductMasterBO> productList;

        public RecyclerViewAdapter(Vector<ProductMasterBO> items) {
            productList = new ArrayList<>(items);
        }

        @Override
        public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.activity_catalog_order_list_items_hr, parent, false);
            return new MyViewHolder(v);
        }

        @Override
        public void onBindViewHolder(MyViewHolder holder, int position) {
            holder.productObj = productList.get(position);
            holder.catalog_order_listview_productname.setText(holder.productObj.getProductShortName());
            if (holder.ppq != null) {
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
            }


            if (holder.moq != null) {
                String strMoqQty = "";
                if (bmodel.labelsMasterHelper
                        .applyLabels("moq") != null) {
                    strMoqQty = bmodel.labelsMasterHelper
                            .applyLabels("moq") + ": "
                            + holder.productObj.getRField1() + "";
                } else {
                    strMoqQty = getResources().getString(R.string.moq) + ": "
                            + holder.productObj.getRField1() + "";
                }
                holder.moq.setText(strMoqQty);
            }
            if (holder.ssrp != null) {
                String price = "";
                if (bmodel.labelsMasterHelper
                        .applyLabels("catalog_srp") != null) {
                    price = bmodel.labelsMasterHelper
                            .applyLabels("catalog_srp") + ": "
                            + bmodel.formatValue(holder.productObj.getSrp()) + "";
                } else {
                    price = "Price : " + bmodel.formatValue(holder.productObj.getSrp());
                }
                holder.ssrp.setText(price);
            }
            if (holder.mrp != null) {
                holder.mrp.setText(getResources().getString(R.string.mrp) + ": " + bmodel.formatValue(holder.productObj.getMRP()));
            }
            if (holder.sih != null) {
                holder.sih.setText(getResources().getString(R.string.sih) + ": " + holder.productObj.getSIH());
            }
            if (holder.slant_view != null) {
                if (holder.productObj.getIsscheme() == 1) {
                    holder.slant_view.setVisibility(View.VISIBLE);
                } else {
                    holder.sih.setVisibility(View.GONE);
                }
            }
            if (holder.list_view_stock_btn != null) {
                if (holder.productObj.getLocations().get(0).getShelfPiece() == -1) {
                    holder.list_view_stock_btn.setText("STOCK");
                } else {
                    holder.list_view_stock_btn.setText("Stock - "
                            + ((holder.productObj.getLocations().get(0).getShelfCase() * holder.productObj.getCaseSize())
                            + (holder.productObj.getLocations().get(0).getShelfOuter() * holder.productObj.getOutersize())
                            + (holder.productObj.getLocations().get(0).getShelfPiece())) + "");
                }
            }
            if (holder.list_view_order_btn != null && holder.total != null) {
                if (holder.productObj.getOrderedPcsQty() != 0) {
                    holder.list_view_order_btn.setText(getResources().getString(R.string.ordered) + " - "
                            + ((holder.productObj.getOrderedCaseQty() * holder.productObj.getCaseSize())
                            + (holder.productObj.getOrderedOuterQty() * holder.productObj.getOutersize())
                            + holder.productObj.getOrderedPcsQty())
                            + "");
                    holder.total.setText("" + bmodel.formatValue(holder.productObj.getTotalamount()));
                } else {
                    holder.total.setText("0");
                    holder.list_view_order_btn.setText(getResources().getString(R.string.order));
                }
            }
            if (holder.pdt_image != null) {

                Uri path;
                if (Build.VERSION.SDK_INT >= 24) {
                    path = FileProvider.getUriForFile(CatalogOrder.this, BuildConfig.APPLICATION_ID + ".provider", new File(
                            appImageFolderPath
                                    + "/"
                                    + DataMembers.CATALOG + "/" + holder.productObj.getProductCode() + ".jpg"));
                } else {
                    path = Uri.fromFile(new File(
                            appImageFolderPath
                                    + "/"
                                    + DataMembers.CATALOG + "/" + holder.productObj.getProductCode() + ".jpg"));
                }

                Glide.with(getApplicationContext())
                        .load(path)
                        .error(ContextCompat.getDrawable(getApplicationContext(), R.drawable.no_image_available))
                        .dontAnimate()
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .into(holder.pdt_image);

                //set SIH value
                if (bmodel.configurationMasterHelper.IS_STOCK_IN_HAND) {
                    if ((holder.productObj.isAllocation() == 1
                            && bmodel.configurationMasterHelper.IS_SIH_VALIDATION) || bmodel.configurationMasterHelper.IS_INVOICE) {

                        holder.sih.setText("SIH : " + holder.productObj.getSIH() + "");
                    }
                }
                holder.wsih.setText("WSIH : " + holder.productObj.getWSIH());
                if (holder.slant_view != null) {

                    try {
                        if (holder.productObj.getTextColor() == getResources().getColor(android.R.color.black)) {
                            if (holder.productObj.isPromo()) {
                                holder.slant_view.setVisibility(View.VISIBLE);
                                holder.slant_view_bg.setBackgroundColor(Color.RED);
                            } else {
                                holder.slant_view.setVisibility(View.GONE);
                            }
                        } else {
                            holder.slant_view_bg.setBackgroundColor(holder.productObj.getTextColor());
                        }
                    } catch (Exception e) {
                        Commons.printException(e);
                        if (holder.productObj.isPromo()) {
                            holder.slant_view.setVisibility(View.VISIBLE);
                            holder.slant_view_bg.setBackgroundColor(Color.RED);
                        } else {
                            holder.slant_view.setVisibility(View.GONE);
                        }
                    }

                }
            }
        }


        @Override
        public void onViewRecycled(MyViewHolder holder) {
            super.onViewRecycled(holder);

            Glide.clear(holder.pdt_image);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public int getItemCount() {
            return productList.size();
        }

        public class MyViewHolder extends RecyclerView.ViewHolder {

            private ImageView pdt_image;
            private TextView catalog_order_listview_productname, ppq,ssrp,
                    mrp, total, sih, wsih, moq;
            private Button list_view_order_btn, list_view_stock_btn, list_view_sales_return_qty;
            private LinearLayout pdt_details_layout;
            private ProductMasterBO productObj;
            private RelativeLayout slant_view;
            private SlantView slant_view_bg;

            public MyViewHolder(View v) {
                super(v);
                pdt_image = (ImageView) v.findViewById(R.id.pdt_image);
                catalog_order_listview_productname = (TextView) v.findViewById(R.id.catalog_order_listview_productname);
                ppq = (TextView) v.findViewById(R.id.catalog_order_listview_ppq);
                moq = (TextView) v.findViewById(R.id.catalog_order_listview_moq);
                ssrp = (TextView) v.findViewById(R.id.catalog_order_listview_srp);
                mrp = (TextView) v.findViewById(R.id.catalog_order_listview_mrp);
                total = (TextView) v.findViewById(R.id.catalog_order_listview_product_value);
                list_view_order_btn = (Button) v.findViewById(R.id.list_view_order_btn);
                list_view_stock_btn = (Button) v.findViewById(R.id.list_view_stock_btn);
                list_view_sales_return_qty = (Button) v.findViewById(R.id.list_view_sales_return_qty);
                pdt_details_layout = (LinearLayout) v.findViewById(R.id.pdt_details_layout);
                sih = (TextView) v.findViewById(R.id.catalog_order_listview_sih);
                wsih = (TextView) v.findViewById(R.id.catalog_order_listview_wsih);
                slant_view = (RelativeLayout) v.findViewById(R.id.slant_view);
                slant_view_bg = (SlantView) v.findViewById(R.id.slant_view_bg);

                catalog_order_listview_productname.setTypeface(bmodel.configurationMasterHelper.getProductNameFont());
                ppq.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
                ssrp.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
                mrp.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
                total.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
                list_view_order_btn.setTypeface(bmodel.configurationMasterHelper.getFontBaloobhai(ConfigurationMasterHelper.FontType.REGULAR));
                list_view_stock_btn.setTypeface(bmodel.configurationMasterHelper.getFontBaloobhai(ConfigurationMasterHelper.FontType.REGULAR));
                list_view_sales_return_qty.setTypeface(bmodel.configurationMasterHelper.getFontBaloobhai(ConfigurationMasterHelper.FontType.REGULAR));
                moq.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));


                /*Drawable mDrawable = getApplicationContext().getResources().getDrawable(R.drawable.ic_action_star_01);
                mDrawable.setColorFilter(new
                        PorterDuffColorFilter(getResources().getColor(R.color.colorPrimary), PorterDuff.Mode.MULTIPLY));
                scheme_star.setImageDrawable(mDrawable);*/

                if (!bmodel.configurationMasterHelper.SHOW_STOCK_SP
                        || screenCode
                        .equals(ConfigurationMasterHelper.MENU_ORDER)) {
                    list_view_stock_btn.setVisibility(View.GONE);
                } else {
                    list_view_stock_btn.setVisibility(View.VISIBLE);
                }
                if (!bmodel.configurationMasterHelper.SHOW_ORDER_PCS) {
                    list_view_order_btn.setVisibility(View.GONE);
                } else {
                    list_view_order_btn.setVisibility(View.VISIBLE);
                }

                if (!bmodel.configurationMasterHelper.SHOW_SALES_RETURN_IN_ORDER)
                    list_view_sales_return_qty.setVisibility(View.GONE);
                else
                    list_view_sales_return_qty.setVisibility(View.VISIBLE);

                if (!bmodel.configurationMasterHelper.IS_STOCK_IN_HAND) {
                    sih.setVisibility(View.GONE);
                } else {
                    if (bmodel.configurationMasterHelper.SHOW_SIH_SPLIT) {

                        if (!bmodel.configurationMasterHelper.SHOW_ORDER_PCS) {
                            sih.setVisibility(View.GONE);
                        } else {
                            sih.setVisibility(View.VISIBLE);
                        }
                    } else {
                        sih.setVisibility(View.VISIBLE);
                    }
                }
                if (bmodel.configurationMasterHelper.IS_WSIH) {
                    wsih.setVisibility(View.VISIBLE);
                } else {
                    wsih.setVisibility(View.GONE);
                }
                if (!bmodel.configurationMasterHelper.IS_SHOW_PPQ) {
                    ppq.setVisibility(View.GONE);
                }
                if (!bmodel.configurationMasterHelper.SHOW_STK_ORD_SRP)
                    ssrp.setVisibility(View.GONE);
                if (!bmodel.configurationMasterHelper.SHOW_STK_ORD_MRP)
                    mrp.setVisibility(View.GONE);
                if (!bmodel.configurationMasterHelper.SHOW_ORDER_TOTAL)
                    total.setVisibility(View.GONE);

                if (!bmodel.configurationMasterHelper.IS_MOQ_ENABLED)
                    moq.setVisibility(View.GONE);

                pdt_details_layout.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (bottom_layout.getVisibility() == View.VISIBLE) {
                            bottom_layout.startAnimation(slide_down);
                            bottom_layout.setVisibility(View.GONE);
                        }
                    }
                });

                list_view_order_btn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        if (dialogCustomKeyBoard == null || !dialogCustomKeyBoard.isDialogCreated()) {
                            dialogCustomKeyBoard = new CustomKeyBoardCatalog(CatalogOrder.this, total, list_view_order_btn, productObj, bmodel, false);
                            dialogCustomKeyBoard.show();
                            dialogCustomKeyBoard.setCancelable(false);

                            //Grab the window of the dialog, and change the width
                            WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
                            Window window = dialogCustomKeyBoard.getWindow();
                            lp.copyFrom(window.getAttributes());
                            lp.width = WindowManager.LayoutParams.WRAP_CONTENT;
                            lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
                            window.setAttributes(lp);
                        }

                    }
                });

                list_view_stock_btn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (dialogCustomKeyBoard == null || !dialogCustomKeyBoard.isDialogCreated()) {
                            dialogCustomKeyBoard = new CustomKeyBoardCatalog(CatalogOrder.this, null, list_view_stock_btn, productObj, bmodel, false);
                            dialogCustomKeyBoard.show();
                            dialogCustomKeyBoard.setCancelable(false);

                            //Grab the window of the dialog, and change the width
                            WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
                            Window window = dialogCustomKeyBoard.getWindow();
                            lp.copyFrom(window.getAttributes());
                            lp.width = WindowManager.LayoutParams.WRAP_CONTENT;
                            lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
                            window.setAttributes(lp);
                        }
                        if (bmodel.configurationMasterHelper.ALLOW_SO_COPY) {
                            productObj.setOrderedPcsQty(productObj.getSoInventory());
                            updateValue();
                        }
                    }
                });

                list_view_sales_return_qty.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        View vChild = pdt_recycler_view.getChildAt(0);
                        GridLayoutManager layoutManager = ((GridLayoutManager) pdt_recycler_view.getLayoutManager());
                        int holderPosition = layoutManager.findFirstVisibleItemPosition();
                        int holderTop = (vChild == null) ? 0 : (vChild.getTop() - pdt_recycler_view.getPaddingTop());
                        showSalesReturnDialog(productObj.getProductID(), holderPosition, holderTop);
                    }
                });

                pdt_image.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        bmodel.selectedPdt = productObj;
                        productIdList = new ArrayList<String>();
                        for (ProductMasterBO product : mylist) {
                            productIdList.add(product.getProductID());
                        }
                        bottom_layout.setVisibility(View.GONE);
                        Intent i = new Intent(CatalogOrder.this, ProductDetailsCatalogActivity.class);
                        i.putExtra("FiveFilter", mSelectedIdByLevelId);
                        i.putStringArrayListExtra("ProductIdList", productIdList);

                        //  i.putExtra("mylist",mylist);
                        startActivity(i);
                    }
                });
            }
        }
    }

    private static final int SALES_RETURN = 3;

    private void showSalesReturnDialog(String productId, int holderPostion, int holderTop) {
        Intent intent = new Intent(this, SalesReturnEntryActivity.class);
        intent.putExtra("pid", productId);
        intent.putExtra("position", holderPostion);
        intent.putExtra("top", holderTop);
        intent.putExtra("from", "ORDER");

        ActivityOptionsCompat opts = ActivityOptionsCompat.makeCustomAnimation(this, R.anim.zoom_enter, R.anim.hold);
        ActivityCompat.startActivityForResult(this, intent, SALES_RETURN, opts.toBundle());
    }

    public void numberPressed(View v) {
        if (mMOQHighlightDialog != null && mMOQHighlightDialog.isVisible()) {
            mMOQHighlightDialog.numberPressed(v);
        }
    }


    //if Rfield1 enabled show this dialog
    private class MOQConfigEnabled extends AsyncTask<Void, Void, Boolean> {

        @Override
        protected Boolean doInBackground(Void... voids) {
            return null;
        }

        @Override
        protected void onPostExecute(Boolean aBoolean) {
            super.onPostExecute(aBoolean);

            android.support.v4.app.FragmentManager ft = getSupportFragmentManager();
            mMOQHighlightDialog = new MOQHighlightDialog();
            mMOQHighlightDialog.setCancelable(false);
            mMOQHighlightDialog.show(ft, "Sample Fragment");
        }
    }

}