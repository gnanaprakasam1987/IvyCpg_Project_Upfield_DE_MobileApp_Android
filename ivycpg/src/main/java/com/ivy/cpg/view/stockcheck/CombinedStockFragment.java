package com.ivy.cpg.view.stockcheck;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.TypedArray;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;
import android.widget.ViewFlipper;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.ivy.cpg.view.order.scheme.SchemeDetailsMasterHelper;
import com.ivy.cpg.view.price.PriceTrackingHelper;
import com.ivy.cpg.view.survey.SurveyActivityNew;
import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.bo.CompetitorFilterLevelBO;
import com.ivy.sd.png.bo.ConfigureBO;
import com.ivy.sd.png.bo.LevelBO;
import com.ivy.sd.png.bo.LocationBO;
import com.ivy.sd.png.bo.ProductMasterBO;
import com.ivy.sd.png.bo.ReasonMaster;
import com.ivy.sd.png.bo.StandardListBO;
import com.ivy.sd.png.commons.IvyBaseActivityNoActionBar;
import com.ivy.sd.png.commons.IvyBaseFragment;
import com.ivy.sd.png.commons.SDUtil;
import com.ivy.sd.png.model.BrandDialogInterface;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.model.CompetitorFilterInterface;
import com.ivy.sd.png.model.FiveLevelFilterCallBack;
import com.ivy.sd.png.provider.ConfigurationMasterHelper;
import com.ivy.sd.png.provider.ProductTaggingHelper;
import com.ivy.sd.png.util.CommonDialog;
import com.ivy.sd.png.util.Commons;
import com.ivy.sd.png.view.CompetitorFilterFragment;
import com.ivy.sd.png.view.FilterFiveFragment;
import com.ivy.sd.png.view.HomeScreenTwo;
import com.ivy.sd.png.view.RemarksDialog;
import com.ivy.sd.png.view.SchemeDialog;
import com.ivy.sd.png.view.SpecialFilterFragment;
import com.ivy.utils.AppUtils;
import com.ivy.utils.DateTimeUtils;
import com.ivy.utils.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Vector;

public class CombinedStockFragment extends IvyBaseFragment implements
        BrandDialogInterface, OnClickListener, OnEditorActionListener, CompetitorFilterInterface, FiveLevelFilterCallBack {


    private static final String BRAND = "Brand";
    // For Special Filter
    private static final String GENERAL = "General";
    private String strBarCodeSearch = "ALL";

    // Global variables
    // Drawer Implementation
    private DrawerLayout mDrawerLayout;
    private ListView lvwplist;
    private EditText QUANTITY;
    private EditText mEdt_searchproductName;
    private String append = "";
    private BusinessModel bmodel;
    private PriceTrackingHelper priceTrackingHelper;
    // HashMap used to set Selected Filter name and ID
    private final HashMap<String, String> mSelectedFilterMap = new HashMap<>();
    // Get SKUBO Total List
    private ArrayList<ProductMasterBO> mylist;
    private Vector<ProductMasterBO> items;
    // Adapter used for Load Reason
    private ArrayAdapter<ReasonMaster> spinnerAdapter;
    private HashMap<Integer, Integer> mCompetitorSelectedIdByLevelId;

    private ArrayList<String> mSearchTypeArray = new ArrayList<>();
    private InputMethodManager inputManager;
    private Button mBtn_Search, mBtn_clear, btnSave;

    private String brandbutton;
    private String generalbutton;
    private int mSelectedLocationIndex;
    private boolean isSpecialFilter_enabled = true;
    private boolean remarks_button_enable = true;
    private boolean scheme_button_enable = true;
    private ArrayAdapter<StandardListBO> mLocationAdapter;
    private View view;
    private HashMap<Integer, Integer> mSelectedIdByLevelId;
    LinearLayout ll_spl_filter, ll_tab_selection;
    private ViewFlipper viewFlipper;
    private TextView productName;
    FrameLayout drawer;
    private ArrayList<String> fiveFilter_productIDs;
    private int mSelectedBrandID = 0;
    private int mTotalScreenWidth = 0;
    private boolean isFromChild;
    private Button mBtnFilterPopup;
    private Object selectedTabTag;
    private int x, y;
    private HorizontalScrollView hscrl_spl_filter;
    SearchAsync searchAsync;
    private boolean loadBothSalable;
    private StockCheckHelper stockCheckHelper;

    private Context context;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        this.context = context;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_combinedstock,
                container, false);
        mDrawerLayout = (DrawerLayout) view.findViewById(
                R.id.drawer_layout);

        bmodel = (BusinessModel) context.getApplicationContext();
        bmodel.setContext(((Activity)context));
        priceTrackingHelper = PriceTrackingHelper.getInstance(getContext());
        stockCheckHelper = StockCheckHelper.getInstance(getContext());

        loadBothSalable = bmodel.configurationMasterHelper.SHOW_SALABLE_AND_NON_SALABLE_SKU;
        try {
            isFromChild = ((Activity)context).getIntent().getBooleanExtra("isFromChild", false);
            if (bmodel.configurationMasterHelper.SHOW_SPL_FILTER) {
                String defaultfilter = getDefaultFilter();
                if (!"".equals(defaultfilter)) {
                    mSelectedFilterMap.put("General", defaultfilter);
                    if (bmodel.configurationMasterHelper.IS_SPL_FILTER_TAB) {
                        loadSpecialFilterView(view);
                        updateGeneralText(defaultfilter);
                        selectTab(view, defaultfilter);
                    } else {
                        updateGeneralText(defaultfilter);
                    }


                } else {
                    mSelectedFilterMap.put("General", GENERAL);
                    if (bmodel.configurationMasterHelper.IS_SPL_FILTER_TAB) {
                        loadSpecialFilterView(view);
                        updateGeneralText(GENERAL);
                        selectTab(view, bmodel.configurationMasterHelper.getGenFilter().get(0).getConfigCode());
                    } else {
                        updateGeneralText(GENERAL);
                    }


                }
            } else {
                mSelectedFilterMap.put("General", GENERAL);
                updateGeneralText(GENERAL);
            }
        } catch (Exception e) {
            Commons.printException(e + "");
        }
        return view;
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
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onStart() {
        super.onStart();

        DisplayMetrics dm = new DisplayMetrics();
        ((Activity)context).getWindow().getWindowManager().getDefaultDisplay().getMetrics(dm);
        mTotalScreenWidth = dm.widthPixels;
        ActionBarDrawerToggle mDrawerToggle;

        if (getView() != null)
            lvwplist = (ListView) getView().findViewById(R.id.list);
        lvwplist.setCacheColorHint(0);

        drawer = (FrameLayout) getView().findViewById(R.id.right_drawer);

        int width = getResources().getDisplayMetrics().widthPixels;
        DrawerLayout.LayoutParams params = (DrawerLayout.LayoutParams) drawer.getLayoutParams();
        params.width = width;
        drawer.setLayoutParams(params);


        mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow,
                GravityCompat.START);
        mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow,
                GravityCompat.END);

        mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);

        if (getActionBar() != null) {
            // getActionBar().setIcon(R.drawable.icon_stock);
            getActionBar().setDisplayShowTitleEnabled(false);
        }
        setScreenTitle(bmodel.mSelectedActivityName);
        getActionBar().setElevation(0);

        mDrawerToggle = new ActionBarDrawerToggle(((Activity)context), /* host Activity */
                mDrawerLayout, /* DrawerLayout object */
                R.string.ok, /* "open drawer" description for accessibility */
                R.string.close /* "close drawer" description for accessibility */
        ) {
            public void onDrawerClosed(View view) {
                if (getActionBar() != null)
                    setScreenTitle(bmodel.mSelectedActivityName);
                getActivity().supportInvalidateOptionsMenu();
            }

            public void onDrawerOpened(View drawerView) {
                /*if (getActionBar() != null)
                    setScreenTitle(getResources().getString(R.string.filter));*/
                getActivity().supportInvalidateOptionsMenu();
            }
        };

        inputManager = (InputMethodManager) context.getSystemService(
                Context.INPUT_METHOD_SERVICE);

        viewFlipper = getView().findViewById(R.id.view_flipper);
        productName = getView().findViewById(R.id.productName);

        mEdt_searchproductName = (EditText) getView().findViewById(
                R.id.edt_searchproductName);
        mEdt_searchproductName.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
        mBtn_Search = getView().findViewById(R.id.btn_search);
        mBtn_Search.setOnClickListener(this);
        mBtn_clear = getView().findViewById(R.id.btn_clear);
        mBtn_clear.setOnClickListener(this);
        mBtnFilterPopup = getView().findViewById(R.id.btn_filter_popup);
        mBtnFilterPopup.setOnClickListener(this);
        btnSave = getView().findViewById(R.id.btn_save);
        // btnSave.setTypeface(bmodel.configurationMasterHelper.getFontBaloobhai(ConfigurationMasterHelper.FontType.REGULAR));

        //((TextView) getView().findViewById(R.id.tvTitle)).setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
        mEdt_searchproductName.setOnEditorActionListener(this);

        mLocationAdapter = new ArrayAdapter<>(context,
                android.R.layout.select_dialog_singlechoice);

        for (StandardListBO temp : bmodel.productHelper.getInStoreLocation())
            mLocationAdapter.add(temp);
        if (bmodel.configurationMasterHelper.IS_GLOBAL_LOCATION) {
            mSelectedLocationIndex = bmodel.productHelper.getmSelectedGLobalLocationIndex();
        }
        mDrawerLayout.addDrawerListener(mDrawerToggle);
        //updateGeneralText(GENERAL);
        mDrawerLayout.closeDrawer(GravityCompat.END);
        loadReason(); // Initialize Adapter and Load Reason

        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (bmodel.hasCombinedStkChecked()) {
                    onNextButtonClick();
                } else
                    Toast.makeText(context,
                            getResources().getString(R.string.no_data_tosave)
                            , Toast.LENGTH_LONG).show();
            }
        });

        mSearchTypeArray = new ArrayList<>();
        mSearchTypeArray.add(getResources().getString(R.string.all));
        mSearchTypeArray.add(getResources().getString(R.string.product_name));
        mSearchTypeArray.add(getResources().getString(R.string.prod_code));
        mSearchTypeArray.add(getResources().getString(R.string.order_dialog_barcode));


        searchText();
        hideShemeButton();
        updateFooter();
        searchAsync = new SearchAsync();

    }

    private ActionBar getActionBar() {
        return ((AppCompatActivity) context).getSupportActionBar();
    }


    private void searchText() {
        try {
            mEdt_searchproductName.addTextChangedListener(new TextWatcher() {
                public void afterTextChanged(Editable s) {
                    getActivity().supportInvalidateOptionsMenu();
                    if (s.length() >= 3 || s.length() == 0) {
                        searchAsync = new SearchAsync();
                        searchAsync.execute();
                    }

                }

                @Override
                public void beforeTextChanged(CharSequence s, int start,
                                              int count, int after) {
                    if (mEdt_searchproductName.getText().toString().length() < 3) {
                        mylist.clear();
                    }
                    if (searchAsync.getStatus() == AsyncTask.Status.RUNNING) {
                        searchAsync.cancel(true);
                    }
                }

                @Override
                public void onTextChanged(CharSequence s, int start,
                                          int before, int count) {

                }
            });
        } catch (Exception e) {
            Commons.printException(e + "");
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

            refreshList();
        }
    }

    private void loadSearchedList() {
        ProductMasterBO ret;
        Vector<ProductMasterBO> items = new Vector<>();

        if (bmodel.configurationMasterHelper.LOAD_STOCK_COMPETITOR == 0) {
            for (ProductMasterBO productBo : getTaggedProducts()) {
                if (bmodel.configurationMasterHelper.IS_GLOBAL_CATEGORY && !productBo.getParentHierarchy().contains("/" + bmodel.productHelper.getmSelectedGlobalProductId() + "/"))
                    continue;
                if ((loadBothSalable
                        ? (productBo.getIsSaleable() == 1 || productBo.getIsSaleable() == 0)
                        : productBo.getIsSaleable() == 1) && productBo.getOwn() == 1)
                    items.add(productBo);
            }
        } else if (bmodel.configurationMasterHelper.LOAD_STOCK_COMPETITOR == 1) {
            for (ProductMasterBO productBo : getTaggedProducts()) {
                if (bmodel.configurationMasterHelper.IS_GLOBAL_CATEGORY && !productBo.getParentHierarchy().contains("/" + bmodel.productHelper.getmSelectedGlobalProductId() + "/"))
                    continue;
                if ((loadBothSalable
                        ? (productBo.getIsSaleable() == 1 || productBo.getIsSaleable() == 0)
                        : productBo.getIsSaleable() == 1) && productBo.getOwn() == 0)
                    items.add(productBo);
            }
        } else if (bmodel.configurationMasterHelper.LOAD_STOCK_COMPETITOR == 2) {
            if (bmodel.configurationMasterHelper.IS_GLOBAL_CATEGORY)
                for (ProductMasterBO sku : getTaggedProducts()) {
                    if (!sku.getParentHierarchy().contains("/" + bmodel.productHelper.getmSelectedGlobalProductId() + "/"))
                        continue;
                    items.add(sku);
                }
            else
                items = getTaggedProducts();
        }


        if (items == null) {
            bmodel.showAlert(
                    getResources().getString(R.string.no_products_exists),
                    0);
            return;
        }
        int siz = items.size();
        Commons.print("siz" + siz);
        mylist = new ArrayList<>();
        String mSelectedFilter = bmodel.getProductFilter();
        for (int i = 0; i < siz; ++i) {
            ret = items.elementAt(i);
            // For breaking search..
            if (searchAsync.isCancelled()) {
                break;
            }
            if (mSelectedFilter.equals(getResources().getString(
                    R.string.order_dialog_barcode))) {

                if (ret.getBarCode() != null
                        && (ret.getBarCode().toLowerCase()
                        .contains(mEdt_searchproductName.getText().toString().toLowerCase())
                        || ret.getCasebarcode().toLowerCase().
                        contains(mEdt_searchproductName.getText().toString().toLowerCase())
                        || ret.getOuterbarcode().toLowerCase().
                        contains(mEdt_searchproductName.getText().toString().toLowerCase())) &&
                        (loadBothSalable
                                ? (ret.getIsSaleable() == 1 || ret.getIsSaleable() == 0)
                                : ret.getIsSaleable() == 1)) {

                    if (generalbutton.equalsIgnoreCase(GENERAL) && brandbutton.equals(BRAND))//No filters selected
                        mylist.add(ret);
                    else if (applyProductAndSpecialFilter(ret))
                        mylist.add(ret);
                }
            } else if (mSelectedFilter.equals(getResources().getString(
                    R.string.prod_code))) {
                if (((ret.getRField1() != null && ret.getRField1()
                        .toLowerCase()
                        .contains(mEdt_searchproductName.getText().toString()
                                .toLowerCase())) || (ret.getProductCode() != null && ret.getProductCode().toLowerCase().contains(mEdt_searchproductName.getText().toString()
                        .toLowerCase()))) && (loadBothSalable
                        ? (ret.getIsSaleable() == 1 || ret.getIsSaleable() == 0)
                        : ret.getIsSaleable() == 1)) {
                    if (generalbutton.equalsIgnoreCase(GENERAL) && brandbutton.equals(BRAND))//No filters selected
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
                                        .toLowerCase()) &&
                        (loadBothSalable
                                ? (ret.getIsSaleable() == 1 || ret.getIsSaleable() == 0)
                                : ret.getIsSaleable() == 1))
                    if (generalbutton.equalsIgnoreCase(GENERAL) && brandbutton.equals(BRAND))//No filters selected
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
                        contains(mEdt_searchproductName.getText().toString().toLowerCase())) &&
                        (loadBothSalable
                                ? (ret.getIsSaleable() == 1 || ret.getIsSaleable() == 0)
                                : ret.getIsSaleable() == 1)) {

                    if (generalbutton.equalsIgnoreCase(GENERAL) && brandbutton.equals(BRAND))//No filters selected
                        mylist.add(ret);
                    else if (applyProductAndSpecialFilter(ret))
                        mylist.add(ret);
                } else if (((ret.getRField1() != null && ret.getRField1()
                        .toLowerCase()
                        .contains(mEdt_searchproductName.getText().toString()
                                .toLowerCase())) || (ret.getProductCode() != null && ret.getProductCode().toLowerCase().contains(mEdt_searchproductName.getText().toString()
                        .toLowerCase()))) && (loadBothSalable
                        ? (ret.getIsSaleable() == 1 || ret.getIsSaleable() == 0)
                        : ret.getIsSaleable() == 1)) {
                    if (generalbutton.equalsIgnoreCase(GENERAL) && brandbutton.equals(BRAND))//No filters selected
                        mylist.add(ret);
                    else if (applyProductAndSpecialFilter(ret))
                        mylist.add(ret);
                } else if (ret.getProductShortName() != null && ret.getProductShortName()
                        .toLowerCase()
                        .contains(
                                mEdt_searchproductName.getText().toString()
                                        .toLowerCase()) &&
                        (loadBothSalable
                                ? (ret.getIsSaleable() == 1 || ret.getIsSaleable() == 0)
                                : ret.getIsSaleable() == 1)) {
                    if (generalbutton.equalsIgnoreCase(GENERAL) && brandbutton.equals(BRAND))//No filters selected
                        mylist.add(ret);
                    else if (applyProductAndSpecialFilter(ret))
                        mylist.add(ret);
                }
            }
        }
    }

    private void refreshList() {
        MyAdapter mSchedule = new MyAdapter(mylist);
        lvwplist.setAdapter(mSchedule);
    }

    private Vector<ProductMasterBO> getTaggedProducts() {
        ProductTaggingHelper productTaggingHelper=ProductTaggingHelper.getInstance(getActivity());
        return productTaggingHelper.getTaggedProducts();
    }

    /**
     * Populate list with specific reason type of the module.
     */
    private void loadReason() {
        spinnerAdapter = new ArrayAdapter<>(context,
                android.R.layout.simple_spinner_item);
        spinnerAdapter
                .setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        for (ReasonMaster temp : bmodel.reasonHelper.getReasonList()) {
            if (temp.getReasonCategory().equalsIgnoreCase("INVT")
                    || temp.getReasonCategory().equalsIgnoreCase("NONE"))
                spinnerAdapter.add(temp);
        }
    }

    @Override
    public void updateBrandText(String mFilterText, int bid) {
        mSelectedBrandID = bid;
        try {

            if (mSelectedBrandID == -1) {
                mCompetitorSelectedIdByLevelId = new HashMap<>();
            }
            // Close the drawer
            mDrawerLayout.closeDrawers();

            // Change the Brand button Name
            brandbutton = mFilterText;

            // Consider generalbutton text if it is dependent filter.
            String generaltxt = generalbutton;


            items = getTaggedProducts();
            if (items == null) {
                bmodel.showAlert(
                        getResources().getString(R.string.no_products_exists),
                        0);
                return;
            }
            int siz = items.size();
            Commons.print("siz" + siz);
            mylist = new ArrayList<>();
            mylist.clear();
//
            if (bmodel.configurationMasterHelper.LOAD_STOCK_COMPETITOR == 0) {
                for (ProductMasterBO sku : items) {
                    if (bmodel.configurationMasterHelper.IS_GLOBAL_CATEGORY && !sku.getParentHierarchy().contains("/" + bmodel.productHelper.getmSelectedGlobalProductId() + "/"))
                        continue;
                    if (sku.getBarCode().equals(strBarCodeSearch)
                            || sku.getCasebarcode().equals(strBarCodeSearch)
                            || sku.getOuterbarcode().equals(strBarCodeSearch)
                            || "ALL".equals(strBarCodeSearch)) {
                        if (bid == sku.getParentid() || (bid == -1 && "Brand".equals(mFilterText))) {
                            if ((loadBothSalable
                                    ? (sku.getIsSaleable() == 1 || sku.getIsSaleable() == 0)
                                    : sku.getIsSaleable() == 1) && sku.getOwn() == 1) {
                                if (isSpecialFilter_enabled) {
                                    if (isSpecialFilterAppliedProduct(generaltxt, sku))
                                        mylist.add(sku);
                                } else {
                                    mylist.add(sku);
                                }
                            }
                        }
                    }
                }
            } else if (bmodel.configurationMasterHelper.LOAD_STOCK_COMPETITOR == 1) {
                for (ProductMasterBO sku : items) {
                    if (bmodel.configurationMasterHelper.IS_GLOBAL_CATEGORY && !sku.getParentHierarchy().contains("/" + bmodel.productHelper.getmSelectedGlobalProductId() + "/"))
                        continue;
                    if (sku.getBarCode().equals(strBarCodeSearch)
                            || sku.getCasebarcode().equals(strBarCodeSearch)
                            || sku.getOuterbarcode().equals(strBarCodeSearch)
                            || "ALL".equals(strBarCodeSearch)) {
                        if (bid == sku.getParentid() || (bid == -1 && "Brand".equals(mFilterText))) {
                            if ((loadBothSalable
                                    ? (sku.getIsSaleable() == 1 || sku.getIsSaleable() == 0)
                                    : sku.getIsSaleable() == 1) && sku.getOwn() == 0) {
                                if (isSpecialFilter_enabled) {
                                    if (isSpecialFilterAppliedProduct(generaltxt, sku))
                                        mylist.add(sku);
                                } else {
                                    mylist.add(sku);
                                }
                            }
                        }
                    }
                }
            } else if (bmodel.configurationMasterHelper.LOAD_STOCK_COMPETITOR == 2) {
                for (ProductMasterBO sku : items) {
                    if (bmodel.configurationMasterHelper.IS_GLOBAL_CATEGORY && !sku.getParentHierarchy().contains("/" + bmodel.productHelper.getmSelectedGlobalProductId() + "/"))
                        continue;
                    if (sku.getBarCode().equals(strBarCodeSearch)
                            || sku.getCasebarcode().equals(strBarCodeSearch)
                            || sku.getOuterbarcode().equals(strBarCodeSearch)
                            || "ALL".equals(strBarCodeSearch)) {
                        if (bid == sku.getParentid() || (bid == -1 && "Brand".equals(mFilterText))) {
                            if ((loadBothSalable
                                    ? (sku.getIsSaleable() == 1 || sku.getIsSaleable() == 0)
                                    : sku.getIsSaleable() == 1)) {
                                if (isSpecialFilter_enabled) {
                                    if (isSpecialFilterAppliedProduct(generaltxt, sku))
                                        mylist.add(sku);
                                } else {
                                    mylist.add(sku);
                                }
                            }
                        }
                    }
                }
            }
            refreshList();
        } catch (Exception e) {
            Commons.printException(e + "");
        }
    }

    private boolean isSpecialFilterAppliedProduct(String generaltxt, ProductMasterBO ret) {
        final String mCommon = "Filt01";
        final String mSbd = "Filt02";
        final String mSbdGaps = "Filt03";
        final String mOrdered = "Filt04";
        final String mPurchased = "Filt05";
        final String mInitiative = "Filt06";
        final String mOnAllocation = "Filt07";
        final String mInStock = "Filt08";
        final String mPromo = "Filt09";
        final String mMustSell = "Filt10";
        final String mFocusBrand = "Filt11";
        final String mFocusBrand2 = "Filt12";
        final String msih = "Filt13";
        final String mOOS = "Filt14";
        final String mNMustSell = "Filt16";
        final String mNearExpiryTag = "Filt19";
        final String mFocusBrand3 = "Filt20";
        final String mFocusBrand4 = "Filt21";
        final String mSMP = "Filt22";
        final String mCompertior = "Filt23";
        final String mShelf = "Filt24";

        return generaltxt.equalsIgnoreCase(mSbd) && ret.isRPS()
                || (generaltxt.equalsIgnoreCase(mOrdered) && (ret.getOrderedPcsQty() > 0 || ret.getOrderedCaseQty() > 0 || ret.getOrderedOuterQty() > 0))
                || (generaltxt.equalsIgnoreCase(mPurchased) && ret.getIsPurchased() == 1)
                || (generaltxt.equalsIgnoreCase(mInitiative) && ret.getIsInitiativeProduct() == 1)
                || (generaltxt.equalsIgnoreCase(mCommon) && (ret.isRPS() || (ret.getIsInitiativeProduct() == 1) || (ret.getIsPurchased() == 1)))
                || (generaltxt.equalsIgnoreCase(mSbdGaps) && (ret.isRPS() && !ret.isSBDAcheived()))
                || (generaltxt.equalsIgnoreCase(GENERAL))
                || (generaltxt.equalsIgnoreCase(mInStock) && ret.getWSIH() > 0)
                || (generaltxt.equalsIgnoreCase(mOnAllocation) && ret.getSIH() > 0 && ret.isAllocation() == 1 && bmodel.configurationMasterHelper.IS_SIH_VALIDATION)
                || (generaltxt.equalsIgnoreCase(mPromo) && ret.isPromo())
                || (generaltxt.equalsIgnoreCase(mMustSell) && ret.getIsMustSell() == 1)
                || (generaltxt.equalsIgnoreCase(mFocusBrand) && ret.getIsFocusBrand() == 1)
                || (generaltxt.equalsIgnoreCase(mFocusBrand2) && ret.getIsFocusBrand2() == 1)
                || (generaltxt.equalsIgnoreCase(msih) && ret.getSIH() > 0)
                || (generaltxt.equalsIgnoreCase(mOOS) && ret.getOos() == 0)
                || (generaltxt.equalsIgnoreCase(mNMustSell) && ret.getIsNMustSell() == 1)
                || (generaltxt.equalsIgnoreCase(mNearExpiryTag) && ret.getIsNearExpiryTaggedProduct() == 1)
                || (generaltxt.equalsIgnoreCase(mFocusBrand3) && ret.getIsFocusBrand3() == 1)
                || (generaltxt.equalsIgnoreCase(mFocusBrand4) && ret.getIsFocusBrand4() == 1)
                || (generaltxt.equalsIgnoreCase(mSMP) && ret.getIsSMP() == 1)
                || (generaltxt.equalsIgnoreCase(mCompertior) && ret.getOwn() == 0)
                || (generaltxt.equalsIgnoreCase(mShelf) && (ret.getLocations().get(mSelectedLocationIndex).getShelfCase() > 0 || ret.getLocations().get(mSelectedLocationIndex).getShelfPiece() > 0 || ret.getLocations().get(mSelectedLocationIndex).getShelfOuter() > 0));
    }

    @Override
    public void updateGeneralText(String mFilterText) {
        // set the spl filter name on the button for display
        fiveFilter_productIDs = null;
        generalbutton = mFilterText;
        if (mSelectedIdByLevelId != null)
            mSelectedIdByLevelId.clear();

        updateBrandText(BRAND, -1);
    }

    @Override
    public void updateCancel() {
        // Close Drawer
        mDrawerLayout.closeDrawers();
    }

    class MyAdapter extends ArrayAdapter<ProductMasterBO> {
        private final ArrayList<ProductMasterBO> items;

        public MyAdapter(ArrayList<ProductMasterBO> items) {
            super(context, R.layout.row_closingstock, items);
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

        public View getView(int position, View convertView, ViewGroup parent) {
            View row = convertView;
            try {
                final ViewHolder holder;
                final ProductMasterBO product = items.get(position);

                if (row == null) {
                    LayoutInflater inflater = ((Activity)context).getLayoutInflater();
                    row = inflater.inflate(
                            R.layout.activity_combined_stock_listview, parent,
                            false);
                    holder = new ViewHolder();
                    holder.psname = row
                            .findViewById(R.id.stock_and_order_listview_productname);
                    holder.tvProductCode = row
                            .findViewById(R.id.tvProductCode);
                    holder.tvbarcode = row
                            .findViewById(R.id.tvbarcode);
                    holder.ivAvailable = row
                            .findViewById(R.id.ivAvailable);


                    if (!bmodel.configurationMasterHelper.IS_SHOW_SKU_CODE)
                        holder.tvProductCode.setVisibility(View.GONE);

                    holder.psname.setOnLongClickListener(new View.OnLongClickListener() {
                        @Override
                        public boolean onLongClick(View v) {


                            bmodel.productHelper.setPdname(holder.pname);
                            bmodel.productHelper.setProdId(holder.productId);
                            bmodel.productHelper.setProductObj(holder.productObj);
                            bmodel.productHelper.setFlag(1);
                            bmodel.productHelper.setTotalScreenSize(mTotalScreenWidth);

                            SchemeDialog sc = new SchemeDialog(
                                    context,
                                    SchemeDetailsMasterHelper.getInstance(context.getApplicationContext()).getSchemeList(), holder.pname,
                                    holder.productId, holder.productObj, 1, mTotalScreenWidth);
                            FragmentManager fm = ((FragmentActivity)context).getSupportFragmentManager();
                            sc.show(fm, "");

                            return true;
                        }
                    });

                    row.setOnClickListener(new OnClickListener() {
                        public void onClick(View v) {

                            productName.setText(holder.pname);

                            inputManager.hideSoftInputFromWindow(
                                    mEdt_searchproductName.getWindowToken(), 0);
                            //mEdt_searchproductName.setText("");

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

                            if (viewFlipper.getDisplayedChild() != 0) {
                                viewFlipper.showPrevious();
                            }
                            Intent intent = new Intent(context,
                                    CombinedStockDetailActivity.class);
                            intent.putExtra("screenTitle", holder.productObj.getProductName());
                            intent.putExtra("pid", holder.productObj.getProductID());
                            intent.putExtra("selectedLocationIndex", mSelectedLocationIndex);

                            if (isPreVisit)
                                intent.putExtra("PreVisit",true);

                            startActivity(intent);

                        }
                    });


                    row.setTag(holder);
                } else {
                    holder = (ViewHolder) row.getTag();
                }

                holder.productObj = product;

                holder.productId = holder.productObj.getProductID();


                holder.pname = holder.productObj.getProductName();

                holder.psname.setText(holder.productObj.getProductShortName());

                if (bmodel.configurationMasterHelper.IS_SHOW_SKU_CODE) {
                    String prodCode = getResources().getString(R.string.prod_code) + ": " +
                            holder.productObj.getProductCode() + " ";
                    if (bmodel.labelsMasterHelper.applyLabels(holder.tvProductCode.getTag()) != null)
                        prodCode = bmodel.labelsMasterHelper
                                .applyLabels(holder.tvProductCode.getTag()) + ": " +
                                holder.productObj.getProductCode() + " ";
                    holder.tvProductCode.setText(prodCode);
                }

                if (!StringUtils.isEmptyString(holder.productObj.getBarCode())) {
                    holder.tvbarcode.setVisibility(View.VISIBLE);
                    String parCode = " " + getResources().getString(R.string.barcode) + ": " +
                            holder.productObj.getBarCode() + " ";
                    holder.tvbarcode.setText(parCode);
                } else {
                    holder.tvbarcode.setText(View.GONE);
                }

                if ((holder.productObj.getLocations()
                        .get(mSelectedLocationIndex)
                        .getShelfPiece() > 0 || holder.productObj.getPriceChanged() == 1)
                        ||
                        (!holder.productObj.getLocations().get(mSelectedLocationIndex).getNearexpiryDate().get(0).getNearexpPC().equals("0")
                                || !holder.productObj.getLocations().get(mSelectedLocationIndex).getNearexpiryDate().get(0).getNearexpCA().equals("0")
                                || !holder.productObj.getLocations().get(mSelectedLocationIndex).getNearexpiryDate().get(0).getNearexpOU().equals("0"))
                        || (holder.productObj.getLocations().get(mSelectedLocationIndex).getFacingQty() > 0)
                        || holder.productObj.getIsListed() == 1
                        || holder.productObj.getIsDistributed() == 1
                        || holder.productObj.getLocations().get(mSelectedLocationIndex).getAvailability() > -1
                        || holder.productObj.getLocations().get(mSelectedLocationIndex).getPriceTagAvailability() == 1) {
                    holder.ivAvailable.setVisibility(View.VISIBLE);
                } else
                    holder.ivAvailable.setVisibility(View.GONE);

            } catch (Exception e) {
                Commons.printException(e + "");
            }
            return row;
        }
    }

    public class ViewHolder {

        private String productId;
        private String pname;
        private ProductMasterBO productObj;
        private TextView psname, tvbarcode, tvProductCode;
        ImageView ivAvailable;
    }


    /**
     * Load selected reason name in the Screen
     *
     * @param reasonId
     * @return position of the reason id
     */
    private int getReasonIndex(String reasonId) {
        if (spinnerAdapter.getCount() == 0)
            return 0;
        int len = spinnerAdapter.getCount();
        if (len == 0)
            return 0;
        for (int i = 0; i < len; ++i) {
            ReasonMaster s = spinnerAdapter.getItem(i);
            if (s.getReasonID().equals(reasonId))
                return i;
        }
        return -1;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // Inflate the menu; this adds items to the action bar if it is present.
        inflater.inflate(R.menu.menu_actionbar_with_filter, menu);
    }

    /**
     * Called whenever we call invalidateOptionsMenu()
     */
    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);

        // Change color if Filter is selected
        try {
            if (!generalbutton.equalsIgnoreCase(GENERAL))
                menu.findItem(R.id.menu_spl_filter).setIcon(
                        R.drawable.ic_action_star_select);

            if (bmodel.configurationMasterHelper.SHOW_REMARKS_STK_ORD) {
                menu.findItem(R.id.menu_remarks).setVisible(true);
            } else {
                menu.findItem(R.id.menu_remarks).setVisible(false);
            }

            if (bmodel.configurationMasterHelper.floating_Survey)
                menu.findItem(R.id.menu_survey).setVisible(true);

            if (!bmodel.configurationMasterHelper.SHOW_SPL_FILTER)
                hideSpecialFilter();

            boolean drawerOpen = mDrawerLayout.isDrawerOpen(GravityCompat.END);

            menu.findItem(R.id.menu_next).setVisible(false);
            if (remarks_button_enable)
                menu.findItem(R.id.menu_remarks).setVisible(!drawerOpen);
            else
                menu.findItem(R.id.menu_remarks).setVisible(false);

            if (isSpecialFilter_enabled && !bmodel.configurationMasterHelper.IS_SPL_FILTER_TAB)
                menu.findItem(R.id.menu_spl_filter).setVisible(!drawerOpen);
            else
                menu.findItem(R.id.menu_spl_filter).setVisible(false);


            menu.findItem(R.id.menu_scheme).setVisible(false);

            menu.findItem(R.id.menu_apply_so).setVisible(false);

            menu.findItem(R.id.menu_apply_std_qty).setVisible(false);

            if (bmodel.configurationMasterHelper.IS_GLOBAL_LOCATION)
                menu.findItem(R.id.menu_loc_filter).setVisible(false);
            else {
                if (bmodel.productHelper.getInStoreLocation().size() > 1
                        || !stockCheckHelper.SHOW_COMB_LOCATION_FILTER)
                    menu.findItem(R.id.menu_loc_filter).setVisible(false);
            }

            menu.findItem(R.id.menu_sih_apply).setVisible(false);

            menu.findItem(R.id.menu_fivefilter).setVisible(false);

            if (bmodel.productHelper.isFilterAvaiable("MENU_STK_ORD"))
                menu.findItem(R.id.menu_fivefilter).setVisible(true);

            if (mSelectedIdByLevelId != null) {
                for (Integer id : mSelectedIdByLevelId.keySet()) {
                    if (mSelectedIdByLevelId.get(id) > 0) {
                        menu.findItem(R.id.menu_fivefilter).setIcon(
                                R.drawable.ic_action_filter_select);
                        break;
                    }
                }
            }

            menu.findItem(R.id.menu_fivefilter).setVisible(!drawerOpen);

            if (!bmodel.configurationMasterHelper.SHOW_REMARKS_STK_CHK) {
                hideRemarksButton();
                menu.findItem(R.id.menu_remarks).setVisible(false);
            } else
                menu.findItem(R.id.menu_remarks).setVisible(true);


            if (bmodel.configurationMasterHelper.SHOW_COMPETITOR_FILTER) {
                menu.findItem(R.id.menu_competitor_filter).setVisible(true);
            }

            if (bmodel.configurationMasterHelper.SHOW_COMPETITOR_FILTER && mCompetitorSelectedIdByLevelId != null) {
                for (Integer id : mCompetitorSelectedIdByLevelId.keySet()) {
                    if (mSelectedIdByLevelId.get(id) > 0) {
                        menu.findItem(R.id.menu_competitor_filter).setIcon(
                                R.drawable.ic_action_filter_select);
                        break;
                    }
                }

            }

        } catch (Exception e) {
            Commons.printException(e + "");
        }
    }


    private void onBackButonClick() {

        if (bmodel.hasCombinedStkChecked()) {
            showDialog(0);
        } else {

            if (!isPreVisit)
                bmodel.outletTimeStampHelper.updateTimeStampModuleWise(DateTimeUtils
                    .now(DateTimeUtils.TIME));

            Intent intent = new Intent(context, HomeScreenTwo.class);

            if (isPreVisit)
                intent.putExtra("PreVisit",true);

            if (isFromChild)
                startActivity(intent.putExtra("isStoreMenu", true));
            else
                startActivity(intent);

            ((Activity)context).finish();
        }

        ((Activity)context).overridePendingTransition(R.anim.trans_right_in, R.anim.trans_right_out);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int i = item.getItemId();
        if (i == android.R.id.home) {
            if (mDrawerLayout.isDrawerOpen(GravityCompat.END))
                mDrawerLayout.closeDrawers();
            else {
                onBackButonClick();
            }
            ((Activity)context).overridePendingTransition(R.anim.trans_right_in, R.anim.trans_right_out);
            return true;
        } else if (i == R.id.menu_survey) {
            startActivity(new Intent(context, SurveyActivityNew.class));
            return true;
        } else if (i == R.id.menu_competitor_filter) {
            competitorFilterClickedFragment();
            getActivity().supportInvalidateOptionsMenu();
            return true;
        } else if (i == R.id.menu_loc_filter) {
            showLocation();
            return true;
        } else if (i == R.id.menu_remarks) {
            onNoteButtonClick();
            return true;
        } else if (i == R.id.menu_spl_filter) {
            generalFilterClickedFragment();
            getActivity().supportInvalidateOptionsMenu();
            return true;
        } else if (i == R.id.menu_apply_so) {
            return true;
        } else if (i == R.id.menu_apply_std_qty) {
            return true;
        } else if (i == R.id.menu_scheme) {
            return true;
        } else if (i == R.id.menu_sih_apply) {
            return true;
        } else if (i == R.id.menu_fivefilter) {
            if (bmodel.configurationMasterHelper.IS_UNLINK_FILTERS) {
                generalbutton = GENERAL;
                mSelectedFilterMap.put("General", GENERAL);
            }
            FiveFilterFragment();
            return true;
        } else if (i == R.id.menu_barcode) {
            ((IvyBaseActivityNoActionBar) context).checkAndRequestPermissionAtRunTime(2);
            int permissionStatus = ContextCompat.checkSelfPermission(context,
                    Manifest.permission.CAMERA);
            if (permissionStatus == PackageManager.PERMISSION_GRANTED) {
                //new IntentIntegrator(getActivity()).setBeepEnabled(false).initiateScan();
                IntentIntegrator integrator = new IntentIntegrator(((Activity)context)) {
                    @Override
                    protected void startActivityForResult(Intent intent, int code) {
                        CombinedStockFragment.this.startActivityForResult(intent, IntentIntegrator.REQUEST_CODE); // REQUEST_CODE override
                    }
                };
                integrator.setBeepEnabled(false).initiateScan();
            } else {
                Toast.makeText(context,
                        getResources().getString(R.string.permission_enable_msg)
                                + " " + getResources().getString(R.string.permission_camera)
                        , Toast.LENGTH_LONG).show();
            }
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * used to hide the specialFilter
     */
    private void hideSpecialFilter() {
        isSpecialFilter_enabled = false;
        generalbutton = "GENERAL";

    }

    private void hideRemarksButton() {
        remarks_button_enable = false;
    }

    private void hideShemeButton() {
        scheme_button_enable = false;
    }

    private void FiveFilterFragment() {
        try {


            QUANTITY = null;
            mDrawerLayout.openDrawer(GravityCompat.END);
            if (getActionBar() != null)
                setScreenTitle(getResources().getString(R.string.filter));

            android.support.v4.app.FragmentManager fm = ((FragmentActivity)context).getSupportFragmentManager();
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
            bundle.putBoolean("isTag", true);
            bundle.putSerializable("selectedFilter", mSelectedIdByLevelId);
            // set Fragmentclass Arguments
            FilterFiveFragment<Object> fragobj = new FilterFiveFragment<>();
            fragobj.setArguments(bundle);

            ft.replace(R.id.right_drawer, fragobj, "Fivefilter");
            ft.commit();
        } catch (Exception e) {
            Commons.printException(e + "");
        }
    }

    private void showLocation() {
        AlertDialog.Builder builder;

        builder = new AlertDialog.Builder(context);
        builder.setTitle(null);
        builder.setSingleChoiceItems(mLocationAdapter, mSelectedLocationIndex,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int item) {
                        mSelectedLocationIndex = item;
                        dialog.dismiss();
                        refreshList();
                    }
                });

        bmodel.applyAlertDialogTheme(builder);
    }

    private void switchProfile() {
        final String switchToProfile = "com.motorolasolutions.emdk.datawedge.api.ACTION_SWITCHTOPROFILE";
        final String extraData = "com.motorolasolutions.emdk.datawedge.api.EXTRA_PROFILENAME";

        Intent i = new Intent();
        i.setAction(switchToProfile);
        // add additional info
        i.putExtra(extraData, "dist_sc");
        context.sendBroadcast(i);
    }

    private void onNoteButtonClick() {
        FragmentTransaction ft = ((FragmentActivity)context).getSupportFragmentManager().beginTransaction();
        RemarksDialog dialog = new RemarksDialog("MENU_CLOSING");
        dialog.setCancelable(false);
        dialog.show(ft, "stk_chk_remark");
    }


    private void onNextButtonClick() {
        if (bmodel.hasCombinedStkChecked()) {
            if (!bmodel.configurationMasterHelper.IS_REASON_FOR_ALL_NON_STOCK_PRODUCTS || stockCheckHelper.isReasonSelectedForAllProducts(true))
                new SaveAsyncTask().execute();
            else
                showDialog(2);

        } else {
            showDialog(1);
        }
    }

    private void showDialog(int id) {

        switch (id) {
            case 0:
                CommonDialog dialog = new CommonDialog(context, getResources().getString(R.string.doyouwantgoback),
                        "", getResources().getString(R.string.ok), new CommonDialog.PositiveClickListener() {
                    @Override
                    public void onPositiveButtonClick() {
                        bmodel.productHelper.clearCombindStockCheckedTable();

                        if (!isPreVisit)
                            bmodel.outletTimeStampHelper.updateTimeStampModuleWise(DateTimeUtils
                                .now(DateTimeUtils.TIME));

                        Intent intent = new Intent(context, HomeScreenTwo.class);

                        if (isPreVisit)
                            intent.putExtra("PreVisit",true);

                        if (isFromChild)
                            startActivity(intent.putExtra("isStoreMenu", true));
                        else
                            startActivity(intent);

                        ((Activity)context).finish();
                        ((Activity)context).overridePendingTransition(R.anim.trans_right_in, R.anim.trans_right_out);
                    }
                }, getResources().getString(R.string.cancel), new CommonDialog.negativeOnClickListener() {
                    @Override
                    public void onNegativeButtonClick() {

                    }
                });
                dialog.show();
                dialog.setCancelable(false);

                break;
            case 1:
                CommonDialog commonDialog = new CommonDialog(context,
                        getResources().getString(R.string.no_items_added),
                        "",
                        getResources().getString(R.string.ok));

                commonDialog.show();
                commonDialog.setCancelable(false);
                break;
            case 2:
                CommonDialog commonDialog1 = new CommonDialog(context,
                        getResources().getString(R.string.reason_required_for) + getResources().getString(R.string.non_stock_products),
                        "",
                        getResources().getString(R.string.ok));
                commonDialog1.show();
                commonDialog1.setCancelable(false);
                break;
        }

    }

    private void loadSpecialFilterView(View view) {
        hscrl_spl_filter = view.findViewById(R.id.hscrl_spl_filter);
        hscrl_spl_filter.setVisibility(View.VISIBLE);
        ll_spl_filter = view.findViewById(R.id.ll_spl_filter);
        ll_tab_selection = view.findViewById(R.id.ll_tab_selection);
        float scale;
        int width;

        bmodel.configurationMasterHelper.getGenFilter().add(0, new ConfigureBO("ALL", "All", "0", 0, 1, 1));

        scale = getContext().getResources().getDisplayMetrics().widthPixels;
        width = (int) (scale / bmodel.configurationMasterHelper.getGenFilter().size());

        float den = getContext().getResources().getDisplayMetrics().density;
        float dimen_wd = getResources().getDimension(R.dimen.special_filter_item_width);
        if (width < (int) (dimen_wd * den + 25.5f)) {
            scale = den;
            width = (int) (dimen_wd * scale + 25.5f);
        }
        final TabLayout tabLay = (TabLayout) view.findViewById(R.id.dummy_tab_lay);
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

            TypedArray typearr = context.getTheme().obtainStyledAttributes(R.styleable.MyTextView);
            final int color = typearr.getColor(R.styleable.MyTextView_textColor, 0);
            final int indicator_color = typearr.getColor(R.styleable.MyTextView_accentcolor, 0);
            Button tab;
            tab = new Button(context);
            tab.setText(config.getMenuName());
            tab.setTag(config.getConfigCode());
            tab.setGravity(Gravity.CENTER);
            tab.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
            tab.setTextColor(color);
            tab.setMaxLines(1);
            tab.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.font_small));
            tab.setBackgroundColor(ContextCompat.getColor(context, android.R.color.transparent));
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
                    if (bmodel.configurationMasterHelper.IS_SPL_FILTER_TAB) {
                        selectedTabTag = view.getTag();
                        selectTab(view.getTag());
                    }
                }
            });


            ll_spl_filter.addView(tab);
            if (i == 0) {
                x = tab.getLeft();
                y = tab.getTop();
            }
            Button tv_selection_identifier = new Button(context);
            tv_selection_identifier.setTag(config.getConfigCode() + config.getMenuName());
            tv_selection_identifier.setWidth(width);
            tv_selection_identifier.setBackgroundColor(indicator_color);
            if (i == 0) {
                tv_selection_identifier.setVisibility(View.VISIBLE);
                updateGeneralText(GENERAL);
            } else {
                tv_selection_identifier.setVisibility(View.GONE);
            }

            ll_tab_selection.addView(tv_selection_identifier);


        }


    }

    private void selectTab(Object tag) {
        for (ConfigureBO config : bmodel.configurationMasterHelper.getGenFilter()) {
            View view = getView().findViewWithTag(config.getConfigCode());
            View view1 = getView().findViewWithTag(config.getConfigCode() + config.getMenuName());
            if (tag == config.getConfigCode()) {
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
        if (!tag.toString().equalsIgnoreCase("All")) {
            mCompetitorSelectedIdByLevelId = new HashMap<>();
        }
        getActivity().supportInvalidateOptionsMenu();

    }

    private void selectTab(View pview, Object tag) {
        for (ConfigureBO config : bmodel.configurationMasterHelper.getGenFilter()) {
            View view = pview.findViewWithTag(config.getConfigCode());
            View view1 = pview.findViewWithTag(config.getConfigCode() + config.getMenuName());
            if (tag == config.getConfigCode()) {
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

        if (!tag.toString().equalsIgnoreCase("All")) {
            mCompetitorSelectedIdByLevelId = new HashMap<>();
        }
        getActivity().supportInvalidateOptionsMenu();

    }


    /**
     * Save the values in Aysnc task through Background
     *
     * @author gnanaprakasam.d
     */
    class SaveAsyncTask extends AsyncTask<String, Integer, Boolean> {
        private AlertDialog.Builder builder;
        private AlertDialog alertDialog;

        @Override
        protected Boolean doInBackground(String... arg0) {
            try {
                // save price check
                if (bmodel.configurationMasterHelper.SHOW_PRICECHECK_IN_STOCKCHECK)
                    priceTrackingHelper.savePriceTransaction(getContext().getApplicationContext(), mylist);

                // save near expiry
                stockCheckHelper.saveNearExpiry(getContext().getApplicationContext());

                // Save closing stock
                stockCheckHelper.saveClosingStock(getContext().getApplicationContext(), false);

                // update review plan in DB
                stockCheckHelper.setReviewPlanInDB(getContext().getApplicationContext());
                bmodel.saveModuleCompletion(HomeScreenTwo.MENU_COMBINED_STOCK, true);

                return Boolean.TRUE;
            } catch (Exception e) {
                Commons.printException(e + "");
                return Boolean.FALSE;
            }
        }

        protected void onPreExecute() {
            builder = new AlertDialog.Builder(context);

            customProgressDialog(builder, getResources().getString(R.string.saving));
            alertDialog = builder.create();
            alertDialog.show();
        }

        protected void onPostExecute(Boolean result) {
            // result is the value returned from doInBackground
            alertDialog.dismiss();
            if (result == Boolean.TRUE) {
                if (!isPreVisit)
                    bmodel.outletTimeStampHelper.updateTimeStampModuleWise(DateTimeUtils
                        .now(DateTimeUtils.TIME));

                new CommonDialog(context.getApplicationContext(), context,
                        "", getResources().getString(R.string.saved_successfully),
                        false, getResources().getString(R.string.ok),
                        null,

                        new CommonDialog.PositiveClickListener() {
                            @Override
                            public void onPositiveButtonClick() {

                                Intent intent = new Intent(context, HomeScreenTwo.class);

                                Bundle extras = ((Activity)context).getIntent().getExtras();
                                if (extras != null) {
                                    intent.putExtra("IsMoveNextActivity", bmodel.configurationMasterHelper.MOVE_NEXT_ACTIVITY);
                                    intent.putExtra("CurrentActivityCode", extras.getString("CurrentActivityCode", ""));

                                    if (extras.getBoolean("PreVisit",false))
                                        intent.putExtra("PreVisit",true);
                                }

                                startActivity(intent);
                                ((Activity)context).finish();

                            }
                        }, new CommonDialog.negativeOnClickListener() {
                    @Override
                    public void onNegativeButtonClick() {
                    }
                }).show();

            }
        }
    }


    public void onClick(View v) {
        Button vw = (Button) v;
        bmodel = (BusinessModel) context.getApplicationContext();
        bmodel.setContext(((Activity)context));
        if (vw == mBtn_Search) {
            viewFlipper.showNext();
        } else if (vw == mBtnFilterPopup) {
            AlertDialog.Builder builderSingle = new AlertDialog.Builder(
                    context);
            final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(
                    context,
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
            if (mEdt_searchproductName.getText().length() > 0)
                mEdt_searchproductName.setText("");
        }
    }

    @Override
    public boolean onEditorAction(TextView arg0, int arg1, KeyEvent arg2) {
        if (arg1 == EditorInfo.IME_ACTION_DONE) {
            if (mEdt_searchproductName.getText().length() >= 3) {
                searchAsync = new SearchAsync();
                searchAsync.execute();
            } else {
                Toast.makeText(context, "Enter atleast 3 letters.", Toast.LENGTH_SHORT)
                        .show();
            }
            return true;
        }
        return false;
    }

    @Override
    public void onResume() {
        super.onResume();


        if (bmodel.userMasterHelper.getUserMasterBO().getUserid() == 0) {
            Toast.makeText(this.context,
                    getResources().getString(R.string.sessionout_loginagain),
                    Toast.LENGTH_SHORT).show();
            ((Activity)context).finish();
        }
        Commons.print("OnResume Called");
        switchProfile();
        updateBrandText(BRAND, -1);
    }

    /**
     * @param product
     * @return
     * @author rajesh.k update total value in lisview
     */
    private int getProductTotalValue(ProductMasterBO product) {
        int totalQty = 0;
        Vector<StandardListBO> locationList = bmodel.productHelper
                .getInStoreLocation();

        int size = locationList.size();
        for (int i = 0; i < size; i++) {
            totalQty = totalQty
                    + product.getLocations().get(i).getShelfPiece()
                    + product.getLocations().get(i).getWHPiece()
                    + (product.getLocations().get(i).getShelfCase() * product
                    .getCaseSize())
                    + (product.getLocations().get(i).getWHCase() * product
                    .getCaseSize())
                    + (product.getLocations().get(i).getShelfOuter() * product
                    .getOutersize())
                    + (product.getLocations().get(i).getWHOuter() * product
                    .getOutersize());
        }
        return totalQty;

    }

    public void updateFromFiveLevelFilter(Vector<LevelBO> mParentIdList) {
        items = getTaggedProducts();

        mylist = new ArrayList<>();

        if (bmodel.configurationMasterHelper.LOAD_STOCK_COMPETITOR == 0) {
            for (LevelBO levelBO : mParentIdList) {
                for (ProductMasterBO sku : items) {
                    if (bmodel.configurationMasterHelper.IS_GLOBAL_CATEGORY && !sku.getParentHierarchy().contains("/" + bmodel.productHelper.getmSelectedGlobalProductId() + "/"))
                        continue;
                    if (levelBO.getProductID() == sku.getParentid()) {
                        if ((loadBothSalable
                                ? (sku.getIsSaleable() == 1 || sku.getIsSaleable() == 0)
                                : sku.getIsSaleable() == 1) && sku.getOwn() == 1)
                            mylist.add(sku);
                    }
                }
            }
        } else if (bmodel.configurationMasterHelper.LOAD_STOCK_COMPETITOR == 1) {
            for (LevelBO levelBO : mParentIdList) {
                for (ProductMasterBO sku : items) {
                    if (bmodel.configurationMasterHelper.IS_GLOBAL_CATEGORY && !sku.getParentHierarchy().contains("/" + bmodel.productHelper.getmSelectedGlobalProductId() + "/"))
                        continue;
                    if (levelBO.getProductID() == sku.getParentid()) {
                        if ((loadBothSalable
                                ? (sku.getIsSaleable() == 1 || sku.getIsSaleable() == 0)
                                : sku.getIsSaleable() == 1) && sku.getOwn() == 0)
                            mylist.add(sku);

                    }
                }
            }
        } else if (bmodel.configurationMasterHelper.LOAD_STOCK_COMPETITOR == 2) {
            for (LevelBO levelBO : mParentIdList) {
                for (ProductMasterBO sku : items) {
                    if (bmodel.configurationMasterHelper.IS_GLOBAL_CATEGORY && !sku.getParentHierarchy().contains("/" + bmodel.productHelper.getmSelectedGlobalProductId() + "/"))
                        continue;
                    if (levelBO.getProductID() == sku.getParentid()) {
                        if (loadBothSalable
                                ? (sku.getIsSaleable() == 1 || sku.getIsSaleable() == 0)
                                : sku.getIsSaleable() == 1)
                            mylist.add(sku);
                    }
                }
            }
        }
        mDrawerLayout.closeDrawers();
        refreshList();
    }

    @Override
    public void updateFromFiveLevelFilter(int mFilteredPid, HashMap<Integer, Integer> mSelectedIdByLevelId, ArrayList<Integer> mAttributeProducts, String mFilterText) {
        ProductTaggingHelper productTaggingHelper=ProductTaggingHelper.getInstance(getActivity());
        Vector<ProductMasterBO> items = productTaggingHelper.getTaggedProducts();
        fiveFilter_productIDs = new ArrayList<>();
        brandbutton = mFilterText;
        if (items == null) {
            bmodel.showAlert(
                    getResources().getString(R.string.no_products_exists), 0);
            return;
        }

        if (mSelectedIdByLevelId != null && AppUtils.isMapEmpty(mSelectedIdByLevelId) == false) {
            mCompetitorSelectedIdByLevelId = new HashMap<>();

        }
        mylist = new ArrayList<>();
        //
        if (bmodel.configurationMasterHelper.LOAD_STOCK_COMPETITOR == 0) {// Only own products
            if (mAttributeProducts != null && mFilteredPid != 0) {//Both Product and attribute filter selected
                for (ProductMasterBO sku : items) {
                    if (bmodel.configurationMasterHelper.IS_GLOBAL_CATEGORY && !sku.getParentHierarchy().contains("/" + bmodel.productHelper.getmSelectedGlobalProductId() + "/"))
                        continue;
                    if (sku.getParentHierarchy().contains("/" + mFilteredPid + "/")) {
                        if ((loadBothSalable
                                ? (sku.getIsSaleable() == 1 || sku.getIsSaleable() == 0)
                                : sku.getIsSaleable() == 1) && sku.getOwn() == 1)
                            if (mAttributeProducts.contains(SDUtil.convertToInt(sku.getProductID()))) {
                                mylist.add(sku);
                                fiveFilter_productIDs.add(sku.getProductID());
                            }
                    }
                }
            } else if (mAttributeProducts == null && mFilteredPid != 0) {// product filter alone selected
                for (ProductMasterBO sku : items) {
                    if (bmodel.configurationMasterHelper.IS_GLOBAL_CATEGORY && !sku.getParentHierarchy().contains("/" + bmodel.productHelper.getmSelectedGlobalProductId() + "/"))
                        continue;
                    if (sku.getParentHierarchy().contains("/" + mFilteredPid + "/")) {
                        if ((loadBothSalable
                                ? (sku.getIsSaleable() == 1 || sku.getIsSaleable() == 0)
                                : sku.getIsSaleable() == 1) && sku.getOwn() == 1)
                            mylist.add(sku);
                        fiveFilter_productIDs.add(sku.getProductID());
                    }
                }
            } else if (mAttributeProducts != null && mFilteredPid != 0) {// Attribute filter alone selected
                for (int pid : mAttributeProducts) {
                    for (ProductMasterBO sku : items) {
                        if (bmodel.configurationMasterHelper.IS_GLOBAL_CATEGORY && !sku.getParentHierarchy().contains("/" + bmodel.productHelper.getmSelectedGlobalProductId() + "/"))
                            continue;
                        if (pid == SDUtil.convertToInt(sku.getProductID())) {
                            if ((loadBothSalable
                                    ? (sku.getIsSaleable() == 1 || sku.getIsSaleable() == 0)
                                    : sku.getIsSaleable() == 1) && sku.getOwn() == 1)
                                mylist.add(sku);
                            fiveFilter_productIDs.add(sku.getProductID());
                        }
                    }
                }
            } else {
                if (mFilterText.equals("")) {
                    for (ProductMasterBO sku : items) {
                        if (bmodel.configurationMasterHelper.IS_GLOBAL_CATEGORY && !sku.getParentHierarchy().contains("/" + bmodel.productHelper.getmSelectedGlobalProductId() + "/"))
                            continue;
                        if ((loadBothSalable
                                ? (sku.getIsSaleable() == 1 || sku.getIsSaleable() == 0)
                                : sku.getIsSaleable() == 1) && sku.getOwn() == 1)
                            mylist.add(sku);
                        fiveFilter_productIDs.add(sku.getProductID());
                    }
                }
            }
        } else if (bmodel.configurationMasterHelper.LOAD_STOCK_COMPETITOR == 1) {// Only competitor products
            if (mAttributeProducts != null && mFilteredPid != 0) {//Both Product and attribute filter selected
                for (ProductMasterBO sku : items) {
                    if (bmodel.configurationMasterHelper.IS_GLOBAL_CATEGORY && !sku.getParentHierarchy().contains("/" + bmodel.productHelper.getmSelectedGlobalProductId() + "/"))
                        continue;
                    if (sku.getParentHierarchy().contains("/" + mFilteredPid + "/")) {
                        if ((loadBothSalable
                                ? (sku.getIsSaleable() == 1 || sku.getIsSaleable() == 0)
                                : sku.getIsSaleable() == 1) && sku.getOwn() == 0)
                            if (mAttributeProducts.contains(SDUtil.convertToInt(sku.getProductID()))) {
                                mylist.add(sku);
                                fiveFilter_productIDs.add(sku.getProductID());
                            }
                    }
                }
            } else if (mAttributeProducts == null && mFilteredPid != 0) {// product filter alone selected
                for (ProductMasterBO sku : items) {
                    if (bmodel.configurationMasterHelper.IS_GLOBAL_CATEGORY && !sku.getParentHierarchy().contains("/" + bmodel.productHelper.getmSelectedGlobalProductId() + "/"))
                        continue;
                    if (sku.getParentHierarchy().contains("/" + mFilteredPid + "/")) {
                        if ((loadBothSalable
                                ? (sku.getIsSaleable() == 1 || sku.getIsSaleable() == 0)
                                : sku.getIsSaleable() == 1) && sku.getOwn() == 0)
                            mylist.add(sku);
                        fiveFilter_productIDs.add(sku.getProductID());
                    }
                }
            } else if (mAttributeProducts != null && mFilteredPid != 0) {// Attribute filter alone selected
                for (int pid : mAttributeProducts) {
                    for (ProductMasterBO sku : items) {
                        if (bmodel.configurationMasterHelper.IS_GLOBAL_CATEGORY && !sku.getParentHierarchy().contains("/" + bmodel.productHelper.getmSelectedGlobalProductId() + "/"))
                            continue;
                        if (pid == SDUtil.convertToInt(sku.getProductID())) {
                            if ((loadBothSalable
                                    ? (sku.getIsSaleable() == 1 || sku.getIsSaleable() == 0)
                                    : sku.getIsSaleable() == 1) && sku.getOwn() == 0)
                                mylist.add(sku);
                            fiveFilter_productIDs.add(sku.getProductID());
                        }
                    }
                }
            } else {
                if (mFilterText.equals("")) {
                    for (ProductMasterBO sku : items) {
                        if (bmodel.configurationMasterHelper.IS_GLOBAL_CATEGORY && !sku.getParentHierarchy().contains("/" + bmodel.productHelper.getmSelectedGlobalProductId() + "/"))
                            continue;
                        if ((loadBothSalable
                                ? (sku.getIsSaleable() == 1 || sku.getIsSaleable() == 0)
                                : sku.getIsSaleable() == 1) && sku.getOwn() == 0)
                            mylist.add(sku);
                        fiveFilter_productIDs.add(sku.getProductID());
                    }
                }
            }
        } else if (bmodel.configurationMasterHelper.LOAD_STOCK_COMPETITOR == 2) {//Both Own and Competitor products
            if (mAttributeProducts != null && mFilteredPid != 0) {//Both Product and attribute filter selected
                for (ProductMasterBO sku : items) {
                    if (bmodel.configurationMasterHelper.IS_GLOBAL_CATEGORY && !sku.getParentHierarchy().contains("/" + bmodel.productHelper.getmSelectedGlobalProductId() + "/"))
                        continue;
                    if (sku.getParentHierarchy().contains("/" + mFilteredPid + "/")) {
                        if (loadBothSalable
                                ? (sku.getIsSaleable() == 1 || sku.getIsSaleable() == 0)
                                : sku.getIsSaleable() == 1)
                            if (mAttributeProducts.contains(SDUtil.convertToInt(sku.getProductID()))) {
                                mylist.add(sku);
                                fiveFilter_productIDs.add(sku.getProductID());
                            }
                    }
                }
            } else if (mAttributeProducts == null && mFilteredPid != 0) {
                for (ProductMasterBO sku : items) {// product filter alone selected
                    if (bmodel.configurationMasterHelper.IS_GLOBAL_CATEGORY && !sku.getParentHierarchy().contains("/" + bmodel.productHelper.getmSelectedGlobalProductId() + "/"))
                        continue;
                    if (sku.getParentHierarchy().contains("/" + mFilteredPid + "/")) {
                        if (loadBothSalable
                                ? (sku.getIsSaleable() == 1 || sku.getIsSaleable() == 0)
                                : sku.getIsSaleable() == 1)
                            mylist.add(sku);
                        fiveFilter_productIDs.add(sku.getProductID());
                    }
                }
            } else if (mAttributeProducts != null && mFilteredPid != 0) {
                for (int pid : mAttributeProducts) {// Attribute filter alone selected
                    for (ProductMasterBO sku : items) {
                        if (bmodel.configurationMasterHelper.IS_GLOBAL_CATEGORY && !sku.getParentHierarchy().contains("/" + bmodel.productHelper.getmSelectedGlobalProductId() + "/"))
                            continue;
                        if (pid == SDUtil.convertToInt(sku.getProductID())) {
                            if (loadBothSalable
                                    ? (sku.getIsSaleable() == 1 || sku.getIsSaleable() == 0)
                                    : sku.getIsSaleable() == 1)
                                mylist.add(sku);
                            fiveFilter_productIDs.add(sku.getProductID());
                        }
                    }
                }
            } else {
                if (mFilterText.equals("")) {
                    for (ProductMasterBO sku : items) {
                        if (bmodel.configurationMasterHelper.IS_GLOBAL_CATEGORY && !sku.getParentHierarchy().contains("/" + bmodel.productHelper.getmSelectedGlobalProductId() + "/"))
                            continue;
                        if (loadBothSalable
                                ? (sku.getIsSaleable() == 1 || sku.getIsSaleable() == 0)
                                : sku.getIsSaleable() == 1)
                            mylist.add(sku);
                        fiveFilter_productIDs.add(sku.getProductID());
                    }
                }
            }
        }

        if (bmodel.configurationMasterHelper.SHOW_SPL_FILTER) {
            Vector<ProductMasterBO> temp = new Vector<>();
            String generaltxt = generalbutton;
            for (ProductMasterBO ret : mylist) {
                if (bmodel.configurationMasterHelper.IS_GLOBAL_CATEGORY && !ret.getParentHierarchy().contains("/" + bmodel.productHelper.getmSelectedGlobalProductId() + "/"))
                    continue;
                if (isSpecialFilterAppliedProduct(generaltxt, ret))
                    temp.add(ret);
            }
            mylist.clear();
            mylist.addAll(temp);
        }

        this.mSelectedIdByLevelId = mSelectedIdByLevelId;
        mDrawerLayout.closeDrawers();

        refreshList();

        if (selectedTabTag != null) {
            selectTab(selectedTabTag);
        }
        ((Activity)context).invalidateOptionsMenu();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (result != null) {
            if (result.getContents() == null) {
                Toast.makeText(context, "Cancelled", Toast.LENGTH_LONG).show();
            } else {
                strBarCodeSearch = result.getContents();
            }
        } else {
            Toast.makeText(context, getResources().getString(R.string.no_match_found), Toast.LENGTH_LONG).show();
        }
    }

    private void updateFooter() {

        int totalAvailableProduts = 0;
        for (ProductMasterBO bo : getTaggedProducts()) {

            for (LocationBO locationBO : bo.getLocations()) {

                if (locationBO.getShelfCase() > 0 || locationBO.getShelfOuter() > 0 || locationBO.getShelfPiece() > 0) {
                    totalAvailableProduts += 1;
                    break;
                }
            }
        }
    }

    /**
     * Special Filter Fragment.
     */
    private void generalFilterClickedFragment() {
        try {
            QUANTITY = null;
            mDrawerLayout.openDrawer(GravityCompat.END);
            if (getActionBar() != null)
                setScreenTitle(getResources().getString(R.string.filter));
            android.support.v4.app.FragmentManager fm = ((FragmentActivity)context).getSupportFragmentManager();
            SpecialFilterFragment frag = (SpecialFilterFragment) fm
                    .findFragmentByTag("filter");
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
            ft.add(R.id.right_drawer, fragobj, "filter");
            ft.commit();
        } catch (Exception e) {
            Commons.printException(e + "");
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

    private void competitorFilterClickedFragment() {
        try {
            QUANTITY = null;

            mDrawerLayout.openDrawer(GravityCompat.END);
            if (getActionBar() != null)
                setScreenTitle(getResources().getString(R.string.competitor_filter));

            android.support.v4.app.FragmentManager fm = ((FragmentActivity)context)
                    .getSupportFragmentManager();
            CompetitorFilterFragment frag = (CompetitorFilterFragment) fm
                    .findFragmentByTag("competitor filter");
            android.support.v4.app.FragmentTransaction ft = fm
                    .beginTransaction();
            if (frag != null)
                ft.detach(frag);

            /*if (mSelectedIdByLevelId != null && bmodel.isMapEmpty(mSelectedIdByLevelId) == false) {
                selectedCompetitorId = "";
            }*/

            // set Fragmentclass Arguments
            CompetitorFilterFragment fragobj = new CompetitorFilterFragment();
            Bundle b = new Bundle();
            b.putSerializable("selectedFilter", mCompetitorSelectedIdByLevelId);
            fragobj.setCompetitorFilterInterface(this);
            fragobj.setArguments(b);
            ft.replace(R.id.right_drawer, fragobj, "competitor filter");
            ft.commit();
        } catch (Exception e) {
            Commons.printException(e + "");
        }
    }

    @Override
    public void updateCompetitorProducts(Vector<CompetitorFilterLevelBO> parentIdList, HashMap<Integer, Integer> mSelectedIdByLevelId, String filterText) {

        this.mCompetitorSelectedIdByLevelId = mSelectedIdByLevelId;
        this.mSelectedIdByLevelId = new HashMap<>();// clearing product filter

        mSelectedBrandID = -1;
        generalbutton = GENERAL;
        if (mylist != null) {
            mylist.clear();
        }

        ProductTaggingHelper productTaggingHelper=ProductTaggingHelper.getInstance(getActivity());
        Vector<ProductMasterBO> items = productTaggingHelper.getTaggedProducts();
        if (parentIdList != null && !parentIdList.isEmpty()) {
            for (CompetitorFilterLevelBO mParentBO : parentIdList) {
                for (ProductMasterBO sku : items) {
                    if (bmodel.configurationMasterHelper.IS_GLOBAL_CATEGORY && !sku.getParentHierarchy().contains("/" + bmodel.productHelper.getmSelectedGlobalProductId() + "/"))
                        continue;
                    if (mParentBO.getProductId() == sku.getCompParentId()) {
                        mylist.add(sku);
                    }
                }
            }
        } else {
            if (bmodel.configurationMasterHelper.IS_GLOBAL_CATEGORY)
                for (ProductMasterBO sku : items) {
                    if (!sku.getParentHierarchy().contains("/" + bmodel.productHelper.getmSelectedGlobalProductId() + "/"))
                        continue;
                    mylist.add(sku);
                }
            else
                mylist.addAll(items);
        }

        mDrawerLayout.closeDrawers();
        refreshList();
        if (bmodel.configurationMasterHelper.IS_SPL_FILTER_TAB) {
            if (hscrl_spl_filter != null)
                hscrl_spl_filter.scrollTo(x, y);
            selectTab(bmodel.configurationMasterHelper.getGenFilter().get(0).getConfigCode());
        }

        ((Activity)context).invalidateOptionsMenu();
    }

}
