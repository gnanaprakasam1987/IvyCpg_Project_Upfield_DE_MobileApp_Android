package com.ivy.sd.png.view;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Parcelable;
import android.os.StatFs;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.CardView;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
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
import android.widget.AdapterView;
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
import com.ivy.carousel.CarouselLayoutManager;
import com.ivy.carousel.CarouselZoomPostLayoutListener;
import com.ivy.lib.Utils;
import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.bo.ConfigureBO;
import com.ivy.sd.png.bo.LevelBO;
import com.ivy.sd.png.bo.OrderHeader;
import com.ivy.sd.png.bo.ProductMasterBO;
import com.ivy.sd.png.commons.IvyBaseActivityNoActionBar;
import com.ivy.sd.png.commons.SDUtil;
import com.ivy.sd.png.model.BrandDialogInterface;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.model.CatalogOrderValueUpdate;
import com.ivy.sd.png.model.HideShowScrollListener;
import com.ivy.sd.png.provider.ConfigurationMasterHelper;
import com.ivy.sd.png.util.Commons;
import com.ivy.sd.png.util.DataMembers;
import com.ivy.sd.png.util.ScreenOrientation;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;

/**
 * Created by dharmapriya.k on 10/14/2016,11:34 AM.
 */
public class CatalogOrder extends IvyBaseActivityNoActionBar implements CatalogOrderValueUpdate, BrandDialogInterface, View.OnClickListener {
    private static final String BRAND = "Brand";
    private static final String GENERAL = "General";
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
    RecyclerViewAdapter adapter;
    HashMap<Integer, Vector<LevelBO>> loadedFilterValues;
    HashMap<Integer, Integer> mSelectedIdByLevelId = new HashMap<Integer, Integer>();
    Vector<LevelBO> sequence;
    Vector<LevelBO> filterValues;
    Spinadapter spinadapter;
    BrandRecyclerViewAdapter brandRecyclerViewAdapter;
    ArrayList<Integer> selectedBrands = new ArrayList<>();
    int SbdDistPre = 0; // Dist stock
    int sbdDistAchieved = 0;
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
    private CustomSpinner category_filter;
    private Vector<LevelBO> brandList;
    private RecyclerView brand_recycler_view;
    // private TextView selectedStkLayout;
    private LevelBO mSelectedLevelBO = new LevelBO();
    private TextView brand_name, totalValueText_brand;//, total_value_bottom;
    private Toolbar top_toolbar, toolbar;
    private int previousTotal = 0;
    private boolean loading = true;
    private int visibleThreshold = 5;
    private GridLayoutManager gridlaymanager;
    private Vector<ProductMasterBO> asyncList = new Vector<>();
    private int startIndex = 0;
    private ArrayList<Integer> brandIds = new ArrayList<>();
    private LinearLayout bottom_layout;
    private Animation slide_down, slide_up;
    private Button btn_filter_popup, btn_search, btn_clear;
    private EditText search_txt;
    private CardView search_toolbar;
    private String searchedtext = "";
    private ArrayList<String> mSearchTypeArray = new ArrayList<>();
    private String mSelectedFilter;
    private int categoryIndex, brandIndex;
    private double totalvalue = 0;
    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mDrawerToggle;
    private HashMap<String, String> mSelectedFilterMap = new HashMap<String, String>();
    private String OrderedFlag, screenCode;
    private TypedArray typearr;
    private TextView totalValueText, lpcText, distValue;
    private ArrayList<String> fiveFilter_productIDs;

    private final String KEY_RECYCLER_STATE = "recycler_state";
    private RecyclerView mRecyclerView;
    private static Bundle mBundleRecyclerViewState = null;
    ArrayList<String> productIdList;
    private FrameLayout drawer;
    private Button nextBtn;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_catalog_order);

        bmodel = (BusinessModel) getApplicationContext();
        bmodel.setContext(this);
        overridePendingTransition(R.anim.trans_left_in, R.anim.trans_left_out);
        pdt_recycler_view = (RecyclerView) findViewById(R.id.pdt_recycler_view);
        brand_recycler_view = (RecyclerView) findViewById(R.id.brand_recycler_view);
        top_toolbar = (Toolbar) findViewById(R.id.top_toolbar);
        search_toolbar = (CardView) findViewById(R.id.search_toolbar);
        bottom_layout = (LinearLayout) findViewById(R.id.bottom_layout);
        btn_filter_popup = (Button) search_toolbar.findViewById(R.id.btn_filter_popup);
        btn_search = (Button) search_toolbar.findViewById(R.id.btn_search);
        btn_clear = (Button) search_toolbar.findViewById(R.id.btn_clear);
        search_txt = (EditText) search_toolbar.findViewById(R.id.search_txt);
        //total_value_bottom = (TextView) findViewById(R.id.total_value_bottom);
        typearr = getTheme().obtainStyledAttributes(R.styleable.MyTextView);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        totalValueText = (TextView) findViewById(R.id.totalValue);
        lpcText = (TextView) findViewById(R.id.lcp);
        distValue = (TextView) findViewById(R.id.distValue);
        nextBtn = (Button) findViewById(R.id.btn_next);
        nextBtn.setOnClickListener(this);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }
        setScreenTitle("" + bmodel.mSelectedActivityName);

        // set a custom shadow that overlays the main content when the drawer
        // opens
        mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow,
                GravityCompat.START);
        mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow,
                GravityCompat.END);

        mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);

        screenCode = HomeScreenTwo.MENU_CATALOG_ORDER;
        OrderedFlag = HomeScreenTwo.MENU_CATALOG_ORDER;
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

        try {
            if (OrderedFlag.equals("FromSummary")) {
                if (bmodel.configurationMasterHelper.SHOW_SPL_FILTER) {
                    mSelectedFilterMap.put("General", mOrdered);
                    updategeneraltext(mOrdered);
                } else {
                    mSelectedFilterMap.put("General", GENERAL);
                    updategeneraltext(GENERAL);
                }
            } else {
                if (bmodel.configurationMasterHelper.SHOW_SPL_FILTER) {

                    String defaultfilter = getDefaultFilter();
                    if (!defaultfilter.equals("")) {
                        mSelectedFilterMap.put("General", defaultfilter);
                        updategeneraltext(defaultfilter);
                    } else {
                        mSelectedFilterMap.put("General", GENERAL);
                        updategeneraltext(GENERAL);
                    }
                } else {
                    mSelectedFilterMap.put("General", GENERAL);
                    updategeneraltext(GENERAL);
                }
            }
        } catch (Exception e) {
            Commons.printException(e);
        }

        search_txt.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

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
                        loadSearchedList();
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        btn_filter_popup.setOnClickListener(this);
        btn_search.setOnClickListener(this);
        btn_clear.setOnClickListener(this);

        loadSBDAchievementLocal();

        //toolbar_content=(LinearLayout)top_toolbar.findViewById(R.id.toolbar_content);
        //top_toolbar.removeAllViews();
        //top_toolbar.addView(View.inflate(getApplicationContext(), R.layout.catalog_toolbar_item,top_toolbar));
        category_filter = (CustomSpinner) top_toolbar.findViewById(R.id.category_filter);
        brand_name = (TextView) findViewById(R.id.brand_name);
        totalValueText_brand = (TextView) findViewById(R.id.total_value_txt);
        if (ScreenOrientation.isCatalogDevice(CatalogOrder.this)) {
            gridlaymanager = new GridLayoutManager(getApplicationContext(), 2);
        } else {
            gridlaymanager = new GridLayoutManager(getApplicationContext(), 1);
        }
        pdt_recycler_view.setHasFixedSize(true);
        pdt_recycler_view.setLayoutManager(gridlaymanager);
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

       /* pdt_recycler_view.addOnScrollListener(new RecyclerView.OnScrollListener() {

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                visibleItemCount = pdt_recycler_view.getChildCount();
                totalItemCount = gridlaymanager.getItemCount();
                firstVisibleItem = gridlaymanager.findFirstVisibleItemPosition();

                if (loading) {
                    if (totalItemCount > previousTotal) {
                        loading = false;
                        previousTotal = totalItemCount;
                    }
                }
                if (!loading && (totalItemCount - visibleItemCount)
                        <= (firstVisibleItem + visibleThreshold)) {
                    // End has been reached
                    Loadmore(startIndex);


                    // Do something

                    loading = true;
                }
            }
        });*/

//        pdt_recycler_view.setDrawingCacheEnabled(true);
//        pdt_recycler_view.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);
        //pdt_recycler_view.fling()
        //LinearLayoutManager linearLayoutManager=new LinearLayoutManager(getApplicationContext(),LinearLayoutManager.HORIZONTAL,false);
        CarouselLayoutManager layoutManager = new CarouselLayoutManager(CarouselLayoutManager.HORIZONTAL, false);
        layoutManager.setPostLayoutListener(new CarouselZoomPostLayoutListener());
        brand_recycler_view.setLayoutManager(layoutManager);
        brand_recycler_view.setHasFixedSize(true);


        category_filter.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                brand_name.setText("All");
                if (spinadapter.getItem(position).getProductID() == 0) {
                    //brandIds=new ArrayList<Integer>();
                    brandIds = null;
                    updatebrandtext(BRAND, -1);


                } else {
                    mSelectedLevelBO = spinadapter.getItem(position);
                    mSelectedIdByLevelId.put(sequence.get(categoryIndex).getProductID(), spinadapter.getItem(position).getProductID());
                }
                Vector<LevelBO> filterList = updateFilterSelection(position, spinadapter.getItem(position).getProductID());

                brandRecyclerViewAdapter = new BrandRecyclerViewAdapter(filterList);
                brand_recycler_view.setAdapter(brandRecyclerViewAdapter);
                if (spinadapter.getItem(position).getProductID() != 0) {
                    brandIds = selectedBrands;
                    filterProducts(selectedBrands);
                    updateValue();
                }

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        mylist = bmodel.productHelper.getProductMaster();
        //generalbutton = GENERAL;
        //brandbutton = BRAND;
        if (mylist == null) {
            bmodel.showAlert(
                    getResources().getString(R.string.no_products_exists),
                    0);
            return;
        }

        getMandatoryFilters();
        updatebrandtext(BRAND, -1);

        if (bmodel.configurationMasterHelper.SHOW_STORE_WISE_DISCOUNT_DLG) {
            bmodel.productHelper.updateMinimumRangeAsBillwiseDisc();
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
            adapter = new RecyclerViewAdapter();//new RecyclerViewAdapter(mylist);
            //adapter.setHasStableIds(true);
            //pdt_recycler_view.getRecycledViewPool().setMaxRecycledViews(0, 2 * 2);
            pdt_recycler_view.setAdapter(adapter);

            //Loadmore(0);
            brandIds = null;

        }

        //for  parital order save based on interval
        if (bmodel.configurationMasterHelper.IS_TEMP_ORDER_SAVE) {
            long timeInterval = bmodel.configurationMasterHelper.tempOrderInterval * 1000;
            bmodel.orderTimer = new Timer();
            bmodel.orderTimer.scheduleAtFixedRate(new TimerTask() {
                @Override
                public void run() {
                    bmodel.insertTempOrder();
                }

            }, 0, timeInterval);

        }

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

    public String getDefaultFilter() {
        String defaultfilter = "";
        try {
            Vector<ConfigureBO> genfilter = bmodel.configurationMasterHelper
                    .getGenFilter();
            for (int i = 0; i < genfilter.size(); i++) {
                if (genfilter.get(i).getHasLink() == 1) {
                    if (!bmodel.configurationMasterHelper.IS_SHOW_SELLER_DIALOG) {
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

    public void loadSearchedList() {

        if (searchedtext.length() >= 3) {
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

            adapter.notifyDataSetChanged();
        } else {
            Toast.makeText(this, "Enter atleast 3 letters.", Toast.LENGTH_SHORT)
                    .show();
        }

        //if (mEdt_searchproductName.getText().length() >= 3) {
       /* Vector<ProductMasterBO> items = bmodel.productHelper.getProductMaster();
        if (items == null) {
            bmodel.showAlert(
                    getResources().getString(R.string.no_products_exists),
                    0);
            return;
        }
        int siz = items.size();
        mylist = new Vector<ProductMasterBO>();
        mSelectedFilter = bmodel.getProductFilter();
        for (int i = 0; i < siz; ++i) {
            ProductMasterBO ret = (ProductMasterBO) items.elementAt(i);
            if (mSelectedFilter.equals(getResources().getString(
                    R.string.order_dialog_barcode))) {
                if (ret.getBarCode() != null && ret.getBarCode()
                        .toLowerCase()
                        .contains(
                                searched_text))
                    mylist.add(ret);

            } else if (mSelectedFilter.equals(getResources().getString(
                    R.string.order_gcas))) {
                if (ret.getRField1() != null && ret.getRField1()
                        .toLowerCase()
                        .contains(
                                searched_text))
                    mylist.add(ret);

            } else if (mSelectedFilter.equals(getResources().getString(
                    R.string.product_name))) {
                if (ret.getProductShortName() != null && ret.getProductShortName()
                        .toLowerCase()
                        .contains(
                                searched_text))
                    mylist.add(ret);
            }

        }

//            adapter = new RecyclerViewAdapter(mylist);
//            pdt_recycler_view.setAdapter(adapter);
        adapter.notifyDataSetChanged();*/


    }

    @Override
    public void onStart() {
        // TODO Auto-generated method stub
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

                mSelectedLevelBO = sequence.get(categoryIndex);

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
                category_filter.setAdapter(spinadapter);

            }
        }
        mDrawerLayout.closeDrawer(GravityCompat.END);

    }

    /*@Override
    protected void onDestroy() {
        super.onDestroy();
        unbindDrawables(findViewById(R.id.root));
        // force the garbage collector to run
        System.gc();
    }*/

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

    private ArrayList<Integer> getParenIdList(int selectedGridLevelID,
                                              ArrayList<Integer> list) {
        ArrayList<Integer> parentIdList = new ArrayList<Integer>();
        Vector<LevelBO> gridViewlist = loadedFilterValues.get(sequence.get(brandIndex).getProductID());
        if (selectedGridLevelID != 0) {
            if (gridViewlist != null) {
                for (LevelBO gridlevelBO : gridViewlist) {
                    if (selectedGridLevelID == gridlevelBO.getParentID()) {
                        parentIdList.add(gridlevelBO.getProductID());
                    }

                }
            }

        } else {

            if (gridViewlist != null) {
                if (list != null) {
                    if (list.size() > 0) {
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
            }

        }

        return parentIdList;
    }

    private Vector<LevelBO> updateFilterSelection(int pos, int catid) {
        ArrayList<Integer> categorylist = new ArrayList<>();
        for (int j = 0; j < loadedFilterValues.get(sequence.get(categoryIndex).getProductID()).size(); j++) {
            if (catid == 0) {
                categorylist.add(loadedFilterValues.get(sequence.get(categoryIndex).getProductID()).get(j).getProductID());
            } else if (loadedFilterValues.get(sequence.get(categoryIndex).getProductID()).get(j).getProductID() == catid) {
                categorylist.add(loadedFilterValues.get(sequence.get(categoryIndex).getProductID()).get(j).getProductID());
            }

        }
        Vector<LevelBO> finalValuelist;
        ArrayList<Integer> parentIdList;
        parentIdList = getParenIdList(0, categorylist);

        Vector<LevelBO> gridViewlist = loadedFilterValues
                .get(sequence.get(brandIndex).getProductID());
        finalValuelist = new Vector<>();
        selectedBrands = new ArrayList<>();
        if (parentIdList.size() > 0) {
            for (int productID : parentIdList) {
                for (LevelBO gridViewBO : gridViewlist) {
                    if (productID == gridViewBO.getProductID()) {
                        finalValuelist.add(gridViewBO);
                        selectedBrands.add(gridViewBO.getProductID());
                    }

                }
            }
        }
        return finalValuelist;
    }

    @Override
    public void updateMultiSelectionBrand(List<String> filtername, List<Integer> filterid) {

    }

    @Override
    public void updateMultiSelectionCatogry(List<Integer> mcatgory) {

    }


    @Override
    public void updatebrandtext(String filtertext, int bid) {

        mSelectedBrandID = bid;

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
                ProductMasterBO ret = items.elementAt(i);
                if (ret.getBarCode().equals(strBarCodeSearch)
                        || ret.getCasebarcode().equals(strBarCodeSearch)
                        || ret.getOuterbarcode().equals(strBarCodeSearch)
                        || strBarCodeSearch.equals("ALL")) {

                    if (!bmodel.configurationMasterHelper.IS_STOCK_AVAILABLE_PRODUCTS_ONLY
                            || (bmodel.configurationMasterHelper.IS_STOCK_AVAILABLE_PRODUCTS_ONLY && bmodel.getRetailerMasterBO().getIsVansales() == 1
                            && ret.getSIH() > 0)
                            || (bmodel.configurationMasterHelper.IS_STOCK_AVAILABLE_PRODUCTS_ONLY && bmodel.getRetailerMasterBO().getIsVansales() == 0 && ret.getWSIH() > 0)) {

                        if ((bid == -1 || bid == ret.getParentid()) && generaltxt.equals(GENERAL) && ret.getIsSaleable() == 1) {
                            // product filter alone
                            if (searchedtext.length() >= 3) {
                                if (isUserEntryFilterSatisfied(ret))
                                    mylist.add(ret);
                            } else {
                                mylist.add(ret);
                            }
                        } else if ((bid == -1 || bid == ret.getParentid()) && !generaltxt.equals(GENERAL) && ret.getIsSaleable() == 1) {
                            //special(GENERAL) filter with or without product filter
                            if (isSpecialFilterAppliedProduct(generaltxt, ret)) {
                                if (searchedtext.length() >= 3) {
                                    if (isUserEntryFilterSatisfied(ret)) {
                                        mylist.add(ret);
                                    }
                                } else {
                                    mylist.add(ret);
                                }
                            }

                        }
                    }


                }
            }
            adapter = new RecyclerViewAdapter();//new RecyclerViewAdapter(mylist);
            //adapter.setHasStableIds(true);
            //pdt_recycler_view.getRecycledViewPool().setMaxRecycledViews(0, 2 * 2);
            pdt_recycler_view.setAdapter(adapter);
            //Loadmore(0);
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

    private void Loadmore(int startIndexin) {
        int count = 0;

        for (int i = startIndexin; i < mylist.size(); i++) {
            if (count <= 19) {
                //if(!asyncList.contains(mylist.get(i))){
                asyncList.add(mylist.get(i));
                count++;
                startIndex = i;
                //}

            }/*else{
                break;
            }*/
        }
        if (startIndexin == 0) {
            adapter = new RecyclerViewAdapter();//new RecyclerViewAdapter(asyncList);
            pdt_recycler_view.setAdapter(adapter);
        } else {
            adapter.notifyDataSetChanged();

        }
    }

    @Override
    public void updategeneraltext(String filtertext) {
        // set the spl filter name on the button for display
        generalbutton = filtertext;

        // clearing fivefilterList
        fiveFilter_productIDs = null;
        if (mSelectedIdByLevelId != null)
            mSelectedIdByLevelId.clear();

        updatebrandtext(BRAND, -1);
        //updatebrandtext(BRAND, -1);
    }

    @Override
    public void updateCancel() {
        mDrawerLayout.closeDrawers();
    }

    @Override
    public void loadStartVisit() {

    }

    @Override
    public void updatefromFiveLevelFilter(Vector<LevelBO> parentidList) {
        mylist = new Vector<>();
        Vector<LevelBO> pdtlist = new Vector<>();
        if (brandList != null) {
            for (int i = 0; i < brandList.size(); i++) {
                bmodel.productHelper.loadBrands(brandList.get(i).getProductID(), "Brand", -1);
                pdtlist.addAll(bmodel.productHelper.getPdtids());
            }
        } else {
            pdtlist = parentidList;
        }
        Vector<ProductMasterBO> items = bmodel.productHelper.getProductMaster();
        for (LevelBO levelBO : pdtlist) {
            for (ProductMasterBO productBO : items) {
                if (productBO.getIsSaleable() == 1) {
                    if (levelBO.getProductID() == Integer.parseInt(productBO.getProductID())) {
                        //  filtertext = levelBO.getLevelName();
                        mylist.add(productBO);
                    }
                }
            }
        }
        //adapter = new RecyclerViewAdapter(mylist);
        adapter.notifyDataSetChanged();
        //pdt_recycler_view.setAdapter(adapter);
        strBarCodeSearch = "ALL";
    }

    @Override
    public void updatefromFiveLevelFilter(Vector<LevelBO> parentidList, HashMap<Integer, Integer> mSelectedIdByLevelId, ArrayList<Integer> mAttributeProducts, String filtertext) {
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

        adapter.notifyDataSetChanged();

        /*mSchedule = new MyAdapter(mylist);


        lvwplist.setAdapter(mSchedule);*/
        strBarCodeSearch = "ALL";
        updateValue();
        mDrawerLayout.closeDrawers();
        this.mSelectedIdByLevelId = mSelectedIdByLevelId;

        /*mylist = new Vector<ProductMasterBO>();
        Vector<ProductMasterBO> items = bmodel.productHelper.getProductMaster();
        for (LevelBO levelBO : parentidList) {
            for (ProductMasterBO productBO : items) {
                if (productBO.getIsSaleable() == 1) {
                    if (levelBO.getProductID() == productBO.getParentid()) {
                        mylist.add(productBO);
                    }
                }
            }
        }

        adapter.notifyDataSetChanged();
        strBarCodeSearch = "ALL";
        updateValue();
        mDrawerLayout.closeDrawers();
        this.mSelectedIdByLevelId = mSelectedIdByLevelId;*/
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
        // TODO Auto-generated method stub
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
                    bmodel.orderTimer.cancel();

                bmodel.productHelper.clearOrderTable();
                if (bmodel.mSelectedModule == 1) {
                    startActivity(new Intent(CatalogOrder.this,
                            HomeScreenActivity.class));
                    finish();
                } else if (bmodel.mSelectedModule == 2) {
                    startActivity(new Intent(CatalogOrder.this,
                            HomeScreenTwo.class));
                    finish();
                } else if (bmodel.mSelectedModule == 3) {
                    startActivity(new Intent(CatalogOrder.this,
                            OrderSplitMasterScreen.class));
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
                                            bmodel.orderTimer.cancel();

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
                                            if (bmodel.mSelectedModule == 3) {
                                                bmodel.orderSplitHelper
                                                        .updateEditOrderUploadFlagAsY(bmodel.deleteSpliteOrderID);

                                            }
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
                                        bmodel.saveClosingStock();

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
            if (bmodel.hasOrder()) {
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
        // TODO Auto-generated method stub
        int siz = mylist.size();
        if (siz == 0)
            return false;
        for (int i = 0; i < siz; ++i) {
            ProductMasterBO product = mylist.get(i);

            int siz1 = product.getLocations().size();
            for (int j = 0; j < siz1; j++) {
                if (product.getLocations().get(j).getShelfPiece() > 0
                        || product.getLocations().get(j).getShelfCase() > 0
                        || product.getLocations().get(j).getShelfOuter() > 0
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
                    DigitalContentDisplay.class);
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

            int lpccount = 0;
            totalvalue = 0;
            HashSet<String> sbdTarget = new HashSet<>();
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
            if (brandIds != null && brandIds.size() != 0) {
                for (int j = 0; j < brandIds.size(); j++) {


                    for (int i = 0; i < items.size(); i++) {
                        if (items.get(i).getIsSaleable() == 1) {
                            if (items.get(i).getParentid() == brandIds.get(j)) {
                                if (items.get(i).getOrderedPcsQty() != 0 || items.get(i).getOrderedCaseQty() != 0
                                        || items.get(i).getOrderedOuterQty() != 0) {
                                    lpccount += 1;
                                    totalvalue += (items.get(i).getOrderedPcsQty() * items.get(i).getSrp())
                                            + (items.get(i).getOrderedCaseQty() * items.get(i).getCsrp())
                                            + items.get(i).getOrderedOuterQty() * items.get(i).getOsrp();
                                    //totalvalue = totalvalue + temp;
                                }
                                //mylist.add(items.get(i));
                            }
                        }
                    }
                }

            } else {
                for (int i = 0; i < siz; i++) {
                    ProductMasterBO ret = items.elementAt(i);
                    if (ret.getOrderedPcsQty() != 0 || ret.getOrderedCaseQty() != 0
                            || ret.getOrderedOuterQty() != 0) {
                        lpccount = lpccount + 1;
                        temp = (ret.getOrderedPcsQty() * ret.getSrp())
                                + (ret.getOrderedCaseQty() * ret.getCsrp())
                                + ret.getOrderedOuterQty() * ret.getOsrp();
                        totalvalue = totalvalue + temp;
                    }
                    if (ret.isRPS()) {
                        sbdTarget.add(ret.getSbdGroupName());
                        int size = ret.getLocations().size();
                        for (int j = 0; j < size; j++) {
                            if (ret.getLocations().get(j).getWHCase() > 0
                                    || ret.getLocations().get(j).getWHOuter() > 0
                                    || ret.getLocations().get(j).getWHPiece() > 0
                                    || ret.getLocations().get(j).getShelfCase() > 0
                                    || ret.getLocations().get(j).getShelfOuter() > 0
                                    || ret.getLocations().get(j).getShelfPiece() > 0) {

                                sbdStockAchieved.add(ret.getSbdGroupName());
                            }

                            if (bmodel.configurationMasterHelper.SHOW_STK_ACHIEVED_WIHTOUT_HISTORY) {
                                if (ret.getLocations().get(j).getWHCase() > 0
                                        || ret.getLocations().get(j).getWHOuter() > 0
                                        || ret.getLocations().get(j).getWHPiece() > 0
                                        || ret.getLocations().get(j).getShelfCase() > 0
                                        || ret.getLocations().get(j)
                                        .getShelfOuter() > 0
                                        || ret.getLocations().get(j)
                                        .getShelfPiece() > 0
                                        || ret.isSBDAcheivedLocal()) {
                                    sbdStkAndOrderAchieved.add(ret
                                            .getSbdGroupName());
                                }
                                if (ret.isSBDAcheivedLocal()) {
                                    sbdAcheived.add(ret.getSbdGroupName());
                                }
                            } else {
                                if (ret.getLocations().get(j).getWHCase() > 0
                                        || ret.getLocations().get(j).getWHOuter() > 0
                                        || ret.getLocations().get(j).getWHPiece() > 0
                                        || ret.getLocations().get(j).getShelfCase() > 0
                                        || ret.getLocations().get(j)
                                        .getShelfOuter() > 0
                                        || ret.getLocations().get(j)
                                        .getShelfPiece() > 0
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
                }
            }
            float per;

            SbdDistPre = sbdStockAchieved.size();

            if (bmodel.configurationMasterHelper.HAS_STOCK_IN_DIST_POST) {
                // per = (float) sbdStkAndOrderAchieved.size() /
                // sbdTarget.size();
                per = (float) sbdStkAndOrderAchieved.size()
                        / bmodel.getRetailerMasterBO()
                        .getSbdDistributionTarget();
                sbdDistAchieved = sbdStkAndOrderAchieved.size();
            } else {
                // per = (float) sbdAcheived.size() / sbdTarget.size();
                per = (float) sbdAcheived.size()
                        / bmodel.getRetailerMasterBO()
                        .getSbdDistributionTarget();
                sbdDistAchieved = sbdAcheived.size();
            }

            lpcText.setText(lpccount + "");
            //totalValueText_brand.setText(bmodel.formatValue(totalvalue) + "");
            totalValueText.setText(" " + bmodel.formatValue(totalvalue));

            if (bmodel.configurationMasterHelper.HIDE_ORDER_DIST) {
                findViewById(R.id.distText).setVisibility(View.GONE);
                distValue.setVisibility(View.GONE);
            } else {
                if (bmodel.configurationMasterHelper.IS_DIST_PRE_POST_ORDER) {
                    if (bmodel.configurationMasterHelper.HAS_STOCK_IN_DIST_POST) {
                        distValue.setText(sbdStockAchieved.size() + "/"
                                + sbdStkAndOrderAchieved.size());
                    } else {
                        distValue.setText(sbdStockAchieved.size() + "/"
                                + sbdAcheived.size());
                    }
                } else
                    distValue.setText(Math.round(per * 100) + "");
            }


            if (!bmodel.configurationMasterHelper.SHOW_LPC_ORDER) {
                lpcText.setVisibility(View.GONE);
                findViewById(R.id.lpc_title).setVisibility(View.GONE);
            }

        } catch (Exception e) {
            Commons.printException(e);
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

            adapter.notifyDataSetChanged();

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

    private void filterProducts(ArrayList<Integer> brandid) {
        mylist = new Vector<ProductMasterBO>();
        //brandIds=brandid;
        //double totalvalue=0;
        Vector<ProductMasterBO> items = bmodel.productHelper.getProductMaster();
        if (brandid != null && brandid.size() != 0) {
            for (int j = 0; j < brandid.size(); j++) {


                for (int i = 0; i < items.size(); i++) {
                    if (items.get(i).getIsSaleable() == 1) {
                        if (items.get(i).getParentid() == brandid.get(j)) {
                            mylist.add(items.get(i));
                        }
                    }
                }
            }
        }
        adapter.notifyDataSetChanged();
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

    @Override
    public void updateTotalValue(String value) {
        updateValue();
    }

    class calculateReturnProductValusAndQty extends AsyncTask<String, Integer, Boolean> {
        //private ProgressDialog progressDialogue;
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
           /* progressDialogue = ProgressDialog.show(CatalogOrder.this,
                    DataMembers.SD, getResources().getString(R.string.loading),
                    true, false);*/
            builder = new AlertDialog.Builder(CatalogOrder.this);

            customProgressDialog( builder, CatalogOrder.this, getResources().getString(R.string.loading));
            alertDialog = builder.create();
            alertDialog.show();

        }

        protected void onProgressUpdate(Integer... progress) {

        }

        protected void onPostExecute(Boolean result) {
            // result is the value returned from doInBackground
            //progressDialogue.dismiss();
            alertDialog.dismiss();
            nextButtonClick();
        }

    }

    class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder> {

        private CustomKeyBoardCatalog dialogCustomKeyBoard;

        public RecyclerViewAdapter() {

        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.activity_catalog_order_list_items_hr, parent, false);
            return new ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(final ViewHolder holder, int position) {
            holder.productObj = mylist.get(position);
            holder.catalog_order_listview_productname.setText(holder.productObj.getProductShortName());
            if (holder.ppq != null) {
                holder.ppq.setText(getResources().getString(R.string.ppq) + ": "
                        + holder.productObj.getRetailerWiseProductWiseP4Qty() + "");
            }
            if (holder.ssrp != null) {
                final String price = "Price : " + bmodel.formatValue(holder.productObj.getSrp());
                holder.ssrp.setText(price);
            }
            if (holder.mrp != null) {
                holder.mrp.setText(getResources().getString(R.string.mrp) + ": " + bmodel.formatValue(holder.productObj.getMRP()));
            }
            if (holder.sih != null) {
                holder.sih.setText(getResources().getString(R.string.sih) + ": " + holder.productObj.getSIH());
            }
            /*if (holder.slant_view != null) {
                if (holder.productObj.getIsscheme() == 1) {
                    holder.slant_view.setVisibility(View.VISIBLE);
                } else {
                    holder.sih.setVisibility(View.GONE);
                }
            }*/
            if (holder.list_view_stock_btn != null) {
                if (holder.productObj.getLocations().get(0).getShelfPiece() == 0) {
                    holder.list_view_stock_btn.setText("STOCK");
                } else {
                    holder.list_view_stock_btn.setText("Stock - " + holder.productObj.getLocations().get(0).getShelfPiece() + "");
                }
            }
            if (holder.list_view_order_btn != null && holder.total != null) {
                if (holder.productObj.getOrderedPcsQty() != 0) {
                    holder.list_view_order_btn.setText("Ordered - " + holder.productObj.getOrderedPcsQty() + "");
                    holder.total.setText("" + bmodel.formatValue(holder.productObj.getTotalamount()));
                } else {
                    holder.total.setText("0");
                    holder.list_view_order_btn.setText("ORDER");
                }
            }
            if (holder.pdt_image != null) {
                if (bmodel.configurationMasterHelper.IS_CATALOG_IMG_DOWNLOAD) {
                    if (isExternalStorageAvailable()) {
                        File prd = new File(getExternalFilesDir(
                                Environment.DIRECTORY_DOWNLOADS)
                                + "/"
                                + bmodel.userMasterHelper.getUserMasterBO()
                                .getUserid()
                                + DataMembers.DIGITAL_CONTENT
                                + "/"
                                + DataMembers.CATALOG + "/" + holder.productObj.getProductCode() + ".png");
                        if (!prd.exists()) {
                            prd = new File(getExternalFilesDir(
                                    Environment.DIRECTORY_DOWNLOADS)
                                    + "/"
                                    + bmodel.userMasterHelper.getUserMasterBO()
                                    .getUserid()
                                    + DataMembers.DIGITAL_CONTENT
                                    + "/"
                                    + DataMembers.CATALOG + "/" + holder.productObj.getProductCode() + ".jpg");
                        }
                        Glide.with(getApplicationContext())
                                .load(prd)
                                .error(ContextCompat.getDrawable(getApplicationContext(), R.drawable.no_image_available))
                                .dontAnimate()
                                .diskCacheStrategy(DiskCacheStrategy.RESULT)
                                .into(holder.pdt_image);
                    }

                } else {
                    holder.pdt_image.setImageResource(R.drawable.no_image_available);
                }

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
        public long getItemId(int position) {
            return position;
        }

        @Override
        public int getItemViewType(int position) {
            return position;
        }

        @Override
        public int getItemCount() {
            return mylist.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {

            ImageView pdt_image;
            TextView catalog_order_listview_productname, ppq, ssrp,
                    mrp, total, sih, wsih;
            Button list_view_order_btn, list_view_stock_btn;
            LinearLayout pdt_details_layout;
            ProductMasterBO productObj;
            RelativeLayout slant_view;
            SlantView slant_view_bg;

            public ViewHolder(View v) {
                super(v);
                pdt_image = (ImageView) v.findViewById(R.id.pdt_image);
                catalog_order_listview_productname = (TextView) v.findViewById(R.id.catalog_order_listview_productname);
                ppq = (TextView) v.findViewById(R.id.catalog_order_listview_ppq);
                ssrp = (TextView) v.findViewById(R.id.catalog_order_listview_srp);
                mrp = (TextView) v.findViewById(R.id.catalog_order_listview_mrp);
                total = (TextView) v.findViewById(R.id.catalog_order_listview_product_value);
                list_view_order_btn = (Button) v.findViewById(R.id.list_view_order_btn);
                list_view_stock_btn = (Button) v.findViewById(R.id.list_view_stock_btn);
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
                if (!bmodel.configurationMasterHelper.SHOW_STK_ORD_SRP_SEC)
                    ssrp.setVisibility(View.GONE);
                if (!bmodel.configurationMasterHelper.SHOW_STK_ORD_MRP)
                    mrp.setVisibility(View.GONE);
                if (!bmodel.configurationMasterHelper.SHOW_ORDER_TOTAL)
                    total.setVisibility(View.GONE);

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

                pdt_image.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        bmodel.selectedPdt = productObj;
                        productIdList = new ArrayList<String>();
                        for (ProductMasterBO product : mylist) {
                            productIdList.add(product.getProductID());
                        }
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

    class BrandRecyclerViewAdapter extends RecyclerView.Adapter<BrandRecyclerViewAdapter.ViewHolder> {

        ArrayList<Integer> brandId = new ArrayList<>();
        private Vector<LevelBO> items;
        private String drawableId;

        public BrandRecyclerViewAdapter(Vector<LevelBO> items) {
            this.items = items;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.brand_recycler_item, parent, false);
            return new ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(final ViewHolder holder, int position) {
            holder.productObj = items.get(position);
            holder.brand_name.setText(items.get(position).getLevelName());
            if (items.size() == 1) {
                brand_name.setText(holder.productObj.getLevelName());
            }


            drawableId = "file:///android_asset/pdtImages/" + holder.productObj.getProductID() + ".png";//R.drawable.no_image_available;


            Glide.with(CatalogOrder.this)
                    .load(drawableId)
                    .error(ContextCompat.getDrawable(getApplicationContext(), R.drawable.no_image_available))
                    .override(100, 100)
                    .dontAnimate()
                    .diskCacheStrategy(DiskCacheStrategy.RESULT)
                    .placeholder(ContextCompat.getDrawable(getApplicationContext(), R.drawable.no_image_available))
                    //.crossFade()
                    .into(holder.brand_image);
            holder.brand_image.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //bmodel.productHelper.loadBrands(holder.productObj.getProductID(), "Brand",-1);
                    //brandList=null;
                    updatefromFiveLevelFilter(bmodel.productHelper.getPdtids());
                    brand_name.setText(holder.productObj.getLevelName());
                    brandId.clear();
                    brandId.add(holder.productObj.getProductID());
                    filterProducts(brandId);
                    brandIds = brandId;
                    updateValue();
                    /*mSelectedIdByLevelId.put(mSelectedLevelBO
                            .getProductID(), holder.productObj
                            .getProductID());*/
                }
            });

        }

        @Override
        public int getItemViewType(int position) {
            return position;
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {

            ImageView brand_image;
            TextView brand_name;
            LevelBO productObj;

            public ViewHolder(View v) {
                super(v);
                brand_image = (ImageView) v.findViewById(R.id.brand_img);
                brand_name = (TextView) v.findViewById(R.id.brand_name);

            }
        }
    }

    private boolean isExternalStorageAvailable() {

        StatFs stat = new StatFs(Environment.getExternalStorageDirectory()
                .getPath());
        double sdAvailSize = (double) stat.getAvailableBlocks()
                * (double) stat.getBlockSize();
        // One binary gigabyte equals 1,073,741,824 bytes.
        double mbAvailable = sdAvailSize / 1048576;

        String state = Environment.getExternalStorageState();
        boolean mExternalStorageAvailable = false;
        boolean mExternalStorageWriteable = false;

        if (Environment.MEDIA_MOUNTED.equals(state)) {
            // We can read and write the media
            mExternalStorageAvailable = mExternalStorageWriteable = true;
        } else if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
            // We can only read the media
            mExternalStorageAvailable = true;
            mExternalStorageWriteable = false;
        } else {
            // Something else is wrong. It may be one of many other states, but
            // all we need
            // to know is we can neither read nor write
            mExternalStorageAvailable = mExternalStorageWriteable = false;
        }

        if (mExternalStorageAvailable == true
                && mExternalStorageWriteable == true && mbAvailable > 10) {
            return true;
        } else {
            return false;
        }
    }
}
