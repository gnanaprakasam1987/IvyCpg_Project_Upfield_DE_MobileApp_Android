package com.ivyretail.views;

import android.Manifest;
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
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;
import android.widget.ViewFlipper;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.bo.ConfigureBO;
import com.ivy.sd.png.bo.LevelBO;
import com.ivy.sd.png.bo.LocationBO;
import com.ivy.sd.png.bo.ProductMasterBO;
import com.ivy.sd.png.bo.ReasonMaster;
import com.ivy.sd.png.bo.SchemeBO;
import com.ivy.sd.png.bo.StandardListBO;
import com.ivy.sd.png.commons.IvyBaseActivityNoActionBar;
import com.ivy.sd.png.commons.IvyBaseFragment;
import com.ivy.sd.png.commons.SDUtil;
import com.ivy.sd.png.model.BrandDialogInterface;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.model.CompetitorFilterInterface;
import com.ivy.sd.png.provider.ConfigurationMasterHelper;
import com.ivy.sd.png.survey.SurveyActivityNew;
import com.ivy.sd.png.util.CommonDialog;
import com.ivy.sd.png.util.Commons;
import com.ivy.sd.png.view.CompetitorFilterFragment;
import com.ivy.sd.png.view.FilterFiveFragment;
import com.ivy.sd.png.view.FilterFragment;
import com.ivy.sd.png.view.HomeScreenTwo;
import com.ivy.sd.png.view.ProductSchemeDetailsActivity;
import com.ivy.sd.png.view.ReasonPhotoDialog;
import com.ivy.sd.png.view.RemarksDialog;
import com.ivy.sd.png.view.SchemeDialog;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;

public class StockCheckFragment extends IvyBaseFragment implements
        BrandDialogInterface, OnClickListener, OnEditorActionListener, CompetitorFilterInterface {


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
    // HashMap used to set Selected Filter name and ID
    private final HashMap<String, String> mSelectedFilterMap = new HashMap<>();
    // Get SKUBO Total List
    private ArrayList<ProductMasterBO> mylist;
    private Vector<ProductMasterBO> items;
    // Adapter used for Load Reason
    private ArrayAdapter<ReasonMaster> spinnerAdapter;

    private ArrayList<String> mSearchTypeArray = new ArrayList<>();
    private InputMethodManager inputManager;
    private Button mBtn_Search, mBtn_clear;

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
    TextView tv_total_stockCheckedProducts, tv_total_products;
    Button btn_save;
    private ViewFlipper viewFlipper;
    private TextView productName;
    FrameLayout drawer;
    private ArrayList<String> fiveFilter_productIDs;
    private int mSelectedBrandID = 0;
    private boolean isFromChild;

    private String selectedCompetitorId = "";
    private Vector<LevelBO> parentidList;
    private ArrayList<Integer> mAttributeProducts;
    private String filtertext;
    private Object selectedTabTag;
    private int x, y;
    private HorizontalScrollView hscrl_spl_filter;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_stockcheck,
                container, false);
        mDrawerLayout = (DrawerLayout) view.findViewById(
                R.id.drawer_layout);
        bmodel = (BusinessModel) getActivity().getApplicationContext();
        bmodel.setContext(getActivity());

        lvwplist = (ListView) view.findViewById(R.id.list);
        lvwplist.setCacheColorHint(0);

        try {
            isFromChild = getActivity().getIntent().getBooleanExtra("isFromChild", false);
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

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public void onStart() {
        super.onStart();


        ActionBarDrawerToggle mDrawerToggle;


        drawer = (FrameLayout) getView().findViewById(R.id.right_drawer);

        int width = getResources().getDisplayMetrics().widthPixels;
        DrawerLayout.LayoutParams params = (android.support.v4.widget.DrawerLayout.LayoutParams) drawer.getLayoutParams();
        params.width = width;
        drawer.setLayoutParams(params);


        mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow,
                GravityCompat.START);
        mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow,
                GravityCompat.END);

        mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);

        if (getActionBar() != null) {
            getActionBar().setDisplayShowTitleEnabled(false);
            setScreenTitle(bmodel.mSelectedActivityName);
            getActionBar().setElevation(0);
        }

        mDrawerToggle = new ActionBarDrawerToggle(getActivity(), /* host Activity */
                mDrawerLayout, /* DrawerLayout object */
                R.string.ok, /* "open drawer" description for accessibility */
                R.string.close /* "close drawer" description for accessibility */
        ) {
            public void onDrawerClosed(View view) {
                if (getActionBar() != null) {
                    setScreenTitle(bmodel.mSelectedActivityName);
                }

                getActivity().supportInvalidateOptionsMenu();
            }

            public void onDrawerOpened(View drawerView) {
                if (getActionBar() != null) {
                    setScreenTitle(getResources().getString(R.string.filter));
                }

                getActivity().supportInvalidateOptionsMenu();
            }
        };

        inputManager = (InputMethodManager) getActivity().getSystemService(
                Context.INPUT_METHOD_SERVICE);

        mDrawerLayout.addDrawerListener(mDrawerToggle);

        viewFlipper = (ViewFlipper) getView().findViewById(R.id.view_flipper);
        productName = (TextView) getView().findViewById(R.id.productName);
        productName.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));


        mEdt_searchproductName = (EditText) getView().findViewById(
                R.id.edt_searchproductName);
        mEdt_searchproductName.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
        mBtn_Search = (Button) getView().findViewById(R.id.btn_search);
        mBtn_Search.setOnClickListener(this);
        mBtn_clear = (Button) getView().findViewById(R.id.btn_clear);
        mBtn_clear.setOnClickListener(this);

        tv_total_stockCheckedProducts = (TextView) getView().findViewById(R.id.tv_stockCheckedProductscount);
        tv_total_products = (TextView) getView().findViewById(R.id.tv_productsCount);

        tv_total_stockCheckedProducts.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.THIN));
        tv_total_products.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.THIN));


        btn_save = (Button) getView().findViewById(R.id.btn_save);
        btn_save.setTypeface(bmodel.configurationMasterHelper.getFontBaloobhai(ConfigurationMasterHelper.FontType.REGULAR));
        btn_save.setOnClickListener(this);
        mEdt_searchproductName.setOnEditorActionListener(this);

        mLocationAdapter = new ArrayAdapter<>(getActivity(),
                android.R.layout.select_dialog_singlechoice);

        for (StandardListBO temp : bmodel.productHelper.getInStoreLocation())
            mLocationAdapter.add(temp);
        if (bmodel.configurationMasterHelper.IS_GLOBAL_LOCATION) {
            mSelectedLocationIndex = bmodel.productHelper.getmSelectedGLobalLocationIndex();
        }

        loadReason(); // Initialize Adapter and Load Reason


        mSearchTypeArray = new ArrayList<>();
        mSearchTypeArray.add("Product Name");
        mSearchTypeArray.add("GCAS Code");
        mSearchTypeArray.add("BarCode");


        searchText();
        hideAndSeek();
        hideShemeButton();
        updateFooter();
        mDrawerLayout.closeDrawer(GravityCompat.END);
    }

    private ActionBar getActionBar() {
        return ((AppCompatActivity) getActivity()).getSupportActionBar();
    }

    private void hideAndSeek() {
        try {
            // On/off the stock related text box


            if (!bmodel.configurationMasterHelper.SHOW_STOCK_SC) {
                view.findViewById(R.id.shelf_layout).setVisibility(View.GONE);
                view.findViewById(R.id.shelfCaseTitle).setVisibility(View.GONE);
            } else {
                try {
                    if (bmodel.labelsMasterHelper.applyLabels(view.findViewById(
                            R.id.shelfCaseTitle).getTag()) != null)
                        ((TextView) view.findViewById(R.id.shelfCaseTitle))
                                .setText(bmodel.labelsMasterHelper
                                        .applyLabels(view.findViewById(
                                                R.id.shelfCaseTitle).getTag()));
                } catch (Exception e) {
                    Commons.printException(e + "");
                }
            }

            if (!bmodel.configurationMasterHelper.SHOW_STOCK_SP) {
                view.findViewById(R.id.shelfPcsTitle).setVisibility(View.GONE);
            } else {
                try {
                    if (bmodel.labelsMasterHelper.applyLabels(view.findViewById(
                            R.id.shelfPcsTitle).getTag()) != null)
                        ((TextView) view.findViewById(R.id.shelfPcsTitle))
                                .setText(bmodel.labelsMasterHelper
                                        .applyLabels(view.findViewById(
                                                R.id.shelfPcsTitle).getTag()));
                } catch (Exception e) {
                    Commons.printException(e + "");
                }
            }

            if (!bmodel.configurationMasterHelper.SHOW_STOCK_CB)
                view.findViewById(R.id.shelfPcsCB).setVisibility(View.GONE);


            if (!bmodel.configurationMasterHelper.SHOW_SHELF_OUTER) {
                view.findViewById(R.id.shelfOuterTitle).setVisibility(View.GONE);
            } else {
                try {
                    if (bmodel.labelsMasterHelper.applyLabels(view.findViewById(
                            R.id.shelfOuterTitle).getTag()) != null)
                        ((TextView) view.findViewById(R.id.shelfOuterTitle))
                                .setText(bmodel.labelsMasterHelper
                                        .applyLabels(view.findViewById(
                                                R.id.shelfOuterTitle).getTag()));
                } catch (Exception e) {
                    Commons.printException(e + "");
                }
            }
            if (!bmodel.configurationMasterHelper.SHOW_STOCK_TOTAL) {
                view.findViewById(R.id.exp_stktotalTitle).setVisibility(View.GONE);
            } else {
                try {
                    if (bmodel.labelsMasterHelper.applyLabels(view.findViewById(
                            R.id.exp_stktotalTitle).getTag()) != null)
                        ((TextView) view.findViewById(R.id.exp_stktotalTitle))
                                .setText(bmodel.labelsMasterHelper
                                        .applyLabels(view.findViewById(
                                                R.id.exp_stktotalTitle)
                                                .getTag()));
                } catch (Exception e) {
                    Commons.printException(e + "");
                }
            }


            if (!bmodel.configurationMasterHelper.SHOW_STOCK_FC) {
                view.findViewById(R.id.et_facingQty).setVisibility(View.GONE);
            } else {
                try {
                    if (bmodel.labelsMasterHelper.applyLabels(view.findViewById(
                            R.id.et_facingQty).getTag()) != null)
                        ((TextView) view.findViewById(R.id.et_facingQty))
                                .setText(bmodel.labelsMasterHelper
                                        .applyLabels(view.findViewById(
                                                R.id.et_facingQty).getTag()));
                } catch (Exception e) {
                    Commons.printException(e + "");
                }
            }
            if (bmodel.configurationMasterHelper.SHOW_STOCK_CB && !bmodel.configurationMasterHelper.SHOW_STOCK_FC &&
                    !bmodel.configurationMasterHelper.SHOW_STOCK_SC && !bmodel.configurationMasterHelper.SHOW_STOCK_SP &&
                    !bmodel.configurationMasterHelper.SHOW_SHELF_OUTER) {
                view.findViewById(R.id.ll_keypad).setVisibility(View.GONE);
            }


        } catch (Exception e) {
            Commons.printException(e + "");
        }
    }

    private void searchText() {
        try {
            mEdt_searchproductName.addTextChangedListener(new TextWatcher() {
                public void afterTextChanged(Editable s) {
                    getActivity().supportInvalidateOptionsMenu();
                    if (s.length() >= 3 || s.length() == 0) {
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
            Commons.printException(e + "");
        }
    }

    private void loadSearchedList() {
        ProductMasterBO ret;
        if (mEdt_searchproductName.getText().length() >= 3) {
            Vector<ProductMasterBO> items = new Vector<>();
            if (bmodel.configurationMasterHelper.LOAD_STOCK_COMPETITOR == 0) {
                for (ProductMasterBO productBo : getTaggedProducts()) {
                    if (productBo.getIsSaleable() == 1 && productBo.getOwn() == 1)
                        items.add(productBo);
                }
            } else if (bmodel.configurationMasterHelper.LOAD_STOCK_COMPETITOR == 1) {
                for (ProductMasterBO productBo : getTaggedProducts()) {
                    if (productBo.getIsSaleable() == 1 && productBo.getOwn() == 0)
                        items.add(productBo);
                }
            } else if (bmodel.configurationMasterHelper.LOAD_STOCK_COMPETITOR == 2) {
                items = getTaggedProducts();
            }
            if (items.isEmpty()) {
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
                        R.string.order_gcas))) {
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
            refreshList();
        } else if (mEdt_searchproductName.getText().length() == 0) {
            loadProductList();
        } else {
            Toast.makeText(getActivity(), getResources().getString(R.string.enter_atleast_three_letters), Toast.LENGTH_SHORT)
                    .show();
        }
    }

    private void refreshList() {
        MyAdapter mSchedule = new MyAdapter(mylist);
        lvwplist.setAdapter(mSchedule);
    }

    private Vector<ProductMasterBO> getTaggedProducts() {
        return bmodel.productHelper.getTaggedProducts();
    }

    /**
     * Populate list with specific reason type of the module.
     */
    private void loadReason() {
        spinnerAdapter = new ArrayAdapter<ReasonMaster>(getActivity(),
                R.layout.spinner_bluetext_layout);
        spinnerAdapter
                .setDropDownViewResource(R.layout.spinner_bluetext_list_item);

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

            if (bmodel.configurationMasterHelper.LOAD_STOCK_COMPETITOR == 0) {
                for (ProductMasterBO sku : items) {
                    if (sku.getBarCode().equals(strBarCodeSearch)
                            || sku.getCasebarcode().equals(strBarCodeSearch)
                            || sku.getOuterbarcode().equals(strBarCodeSearch)
                            || "ALL".equals(strBarCodeSearch)) {
                        if (bid == sku.getParentid() || (bid == -1 && "Brand".equals(mFilterText))) {
                            if (sku.getIsSaleable() == 1 && sku.getOwn() == 1) {
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
                    if (sku.getBarCode().equals(strBarCodeSearch)
                            || sku.getCasebarcode().equals(strBarCodeSearch)
                            || sku.getOuterbarcode().equals(strBarCodeSearch)
                            || "ALL".equals(strBarCodeSearch)) {
                        if (bid == sku.getParentid() || (bid == -1 && "Brand".equals(mFilterText))) {
                            if (sku.getIsSaleable() == 1 && sku.getOwn() == 0) {
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
                    if (sku.getBarCode().equals(strBarCodeSearch)
                            || sku.getCasebarcode().equals(strBarCodeSearch)
                            || sku.getOuterbarcode().equals(strBarCodeSearch)
                            || "ALL".equals(strBarCodeSearch)) {
                        if (bid == sku.getParentid() || (bid == -1 && "Brand".equals(mFilterText))) {
                            if (sku.getIsSaleable() == 1) {
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
            updateFooter();
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

    @Override
    public void updateCompetitorProducts(String filterId) {
        selectedCompetitorId = filterId;
        if (mylist != null) {
            mylist.clear();
        }
        if (!selectedCompetitorId.equals("")) {
            mSelectedIdByLevelId = new HashMap<>();
        }
            Vector<ProductMasterBO> items = bmodel.productHelper.getTaggedProducts();
            if (filterId != null && !filterId.isEmpty()) {
                for (ProductMasterBO sku : items) {
                    if (Integer.parseInt(filterId) == sku.getCompParentId()) {
                        mylist.add(sku);
                    }
                }
            } else {
                mylist.addAll(items);
            }
            mDrawerLayout.closeDrawers();
            refreshList();
        hscrl_spl_filter.scrollTo(x, y);
        selectTab(bmodel.configurationMasterHelper.getGenFilter().get(0).getConfigCode());

        getActivity().invalidateOptionsMenu();


    }

    class MyAdapter extends ArrayAdapter<ProductMasterBO> {
        private final ArrayList<ProductMasterBO> items;

        public MyAdapter(ArrayList<ProductMasterBO> items) {
            super(getActivity(), R.layout.row_closingstock, items);
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
                    LayoutInflater inflater = getActivity().getLayoutInflater();
                    row = inflater.inflate(
                            R.layout.activity_stock_check_listview, parent,
                            false);
                    holder = new ViewHolder();
                    holder.audit = (ImageButton) row
                            .findViewById(R.id.btn_audit);
                    holder.psname = (TextView) row
                            .findViewById(R.id.stock_and_order_listview_productname);
                    holder.psname.setTypeface(bmodel.configurationMasterHelper.getProductNameFont());
                    holder.psname.setMaxLines(bmodel.configurationMasterHelper.MAX_NO_OF_PRODUCT_LINES);
                    holder.ppq = (TextView) row
                            .findViewById(R.id.stock_and_order_listview_ppq);
                    holder.ppq.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
                    holder.psq = (TextView) row
                            .findViewById(R.id.stock_and_order_listview_psq);
                    holder.psq.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));

                    holder.mReason = (Spinner) row.findViewById(R.id.reason);

                    holder.shelfPcsQty = (EditText) row
                            .findViewById(R.id.stock_and_order_listview_sp_qty);
                    holder.shelfCaseQty = (EditText) row
                            .findViewById(R.id.stock_and_order_listview_sc_qty);
                    holder.shelfouter = (EditText) row
                            .findViewById(R.id.stock_and_order_listview_shelfouter_qty);
                    holder.shelfPcsQty.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
                    holder.shelfCaseQty.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
                    holder.shelfouter.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));

                    holder.ll_stkCB = (LinearLayout) row
                            .findViewById(R.id.ll_stock_and_order_listview_cb);
                    holder.avail_cb = (CheckBox) row
                            .findViewById(R.id.stock_and_order_listview_cb);

                    holder.total = (TextView) row
                            .findViewById(R.id.stock_check_listview_total);
                    holder.total.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));


                    holder.facingQty = (EditText) row
                            .findViewById(R.id.stock_check_listview_fc_qty);

                    // On/off the stock related text box
                    if (bmodel.configurationMasterHelper.IS_SHOW_PSQ) {
                        holder.psq.setVisibility(View.VISIBLE);
                    } else {
                        holder.psq.setVisibility(View.GONE);
                    }
                    if (bmodel.configurationMasterHelper.IS_SHOW_PPQ) {
                        holder.ppq.setVisibility(View.VISIBLE);
                    } else {
                        holder.ppq.setVisibility(View.GONE);
                    }

                    if (!bmodel.configurationMasterHelper.SHOW_STOCK_FC)
                        holder.facingQty.setVisibility(View.GONE);
                    if (!bmodel.configurationMasterHelper.SHOW_STOCK_SC)
                        holder.shelfCaseQty.setVisibility(View.GONE);
                    if (!bmodel.configurationMasterHelper.SHOW_STOCK_SP)
                        holder.shelfPcsQty.setVisibility(View.GONE);
                    if (!bmodel.configurationMasterHelper.SHOW_STOCK_CB)
                        holder.ll_stkCB.setVisibility(View.GONE);

                    if (!bmodel.configurationMasterHelper.SHOW_STOCK_RSN)
                        holder.mReason.setVisibility(View.GONE);


                    if (!bmodel.configurationMasterHelper.SHOW_SHELF_OUTER)
                        holder.shelfouter.setVisibility(View.GONE);
                    if (!bmodel.configurationMasterHelper.SHOW_STOCK_TOTAL)
                        holder.total.setVisibility(View.GONE);


                    holder.audit.setOnClickListener(new OnClickListener() {

                        @Override
                        public void onClick(View view) {
                            if (holder.productObj.getLocations()
                                    .get(mSelectedLocationIndex).getAudit() == 2) {
                                holder.productObj.getLocations()
                                        .get(mSelectedLocationIndex).setAudit(1);
                                holder.audit
                                        .setImageResource(R.drawable.ic_audit_yes);
                            } else if (holder.productObj.getLocations()
                                    .get(mSelectedLocationIndex).getAudit() == 1) {
                                holder.productObj.getLocations()
                                        .get(mSelectedLocationIndex).setAudit(0);
                                holder.audit
                                        .setImageResource(R.drawable.ic_audit_no);
                            } else if (holder.productObj.getLocations()
                                    .get(mSelectedLocationIndex).getAudit() == 0) {
                                holder.productObj.getLocations()
                                        .get(mSelectedLocationIndex).setAudit(2);
                                holder.audit
                                        .setImageResource(R.drawable.ic_audit_none);
                            }
                        }
                    });

                    holder.avail_cb
                            .setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                                @Override
                                public void onCheckedChanged(
                                        CompoundButton buttonView,
                                        boolean isChecked) {
                                    if (isChecked
                                            && holder.productObj
                                            .getLocations()
                                            .get(mSelectedLocationIndex)
                                            .getShelfPiece() == -1) {
                                        if (bmodel.configurationMasterHelper.SHOW_STOCK_SP) {
                                            if (holder.shelfPcsQty.getText().toString().length() == 0)
                                                holder.shelfPcsQty.setText("1");
                                        } else if (bmodel.configurationMasterHelper.SHOW_STOCK_SC) {
                                            if (holder.shelfCaseQty.getText().toString().length() == 0)
                                                holder.shelfCaseQty.setText("1");
                                        } else if (bmodel.configurationMasterHelper.SHOW_SHELF_OUTER) {
                                            if (holder.shelfouter.getText().toString().length() == 0)
                                                holder.shelfouter.setText("1");
                                        } else if (!bmodel.configurationMasterHelper.SHOW_STOCK_SP
                                                && !bmodel.configurationMasterHelper.SHOW_STOCK_SC
                                                && !bmodel.configurationMasterHelper.SHOW_SHELF_OUTER) {
                                            holder.productObj.getLocations()
                                                    .get(mSelectedLocationIndex)
                                                    .setShelfPiece(1);
                                        }
                                        if (bmodel.configurationMasterHelper.SHOW_STOCK_RSN) {
                                            holder.mReason.setEnabled(false);
                                            holder.mReason.setSelected(false);
                                            holder.mReason.setSelection(0);
                                            holder.productObj.setReasonID("0");
                                        }
                                    } else if (isChecked
                                            && holder.productObj
                                            .getLocations()
                                            .get(mSelectedLocationIndex)
                                            .getShelfPiece() > 0) {
                                        if (bmodel.configurationMasterHelper.SHOW_STOCK_RSN) {
                                            holder.mReason.setEnabled(false);
                                            holder.mReason.setSelected(false);
                                            holder.mReason.setSelection(0);
                                            holder.productObj.setReasonID("0");
                                        }
                                    } else if (!isChecked) {
                                        if (bmodel.configurationMasterHelper.SHOW_STOCK_SP) {
                                            if (holder.shelfPcsQty.getText().toString().length() == 0)
                                                holder.shelfPcsQty.setText("");
                                            else if (holder.shelfPcsQty.getText().toString().length() > 0)
                                                holder.shelfPcsQty.setText("");
                                        } else if (bmodel.configurationMasterHelper.SHOW_STOCK_SC) {
                                            if (holder.shelfCaseQty.getText().toString().length() == 0)
                                                holder.shelfCaseQty.setText("");
                                            else if (holder.shelfCaseQty.getText().toString().length() > 0)
                                                holder.shelfCaseQty.setText("");
                                        } else if (bmodel.configurationMasterHelper.SHOW_SHELF_OUTER) {
                                            if (holder.shelfouter.getText().toString().length() == 0)
                                                holder.shelfouter.setText("");
                                            else if (holder.shelfouter.getText().toString().length() > 0)
                                                holder.shelfouter.setText("");
                                        } else if (!bmodel.configurationMasterHelper.SHOW_STOCK_SP
                                                && !bmodel.configurationMasterHelper.SHOW_STOCK_SC
                                                && !bmodel.configurationMasterHelper.SHOW_SHELF_OUTER) {
                                            holder.productObj.getLocations()
                                                    .get(mSelectedLocationIndex)
                                                    .setShelfPiece(-1);
                                        }
                                        if (bmodel.configurationMasterHelper.SHOW_STOCK_RSN) {
                                            holder.mReason.setEnabled(true);
                                            holder.mReason.setSelected(true);
                                            holder.mReason.setSelection(0);
                                        }
                                    }
                                    updateFooter();
                                }
                            });

                    holder.mReason.setAdapter(spinnerAdapter);
                    holder.mReason
                            .setOnItemSelectedListener(new OnItemSelectedListener() {
                                public void onItemSelected(
                                        AdapterView<?> parent, View view,
                                        int position, long id) {

                                    ReasonMaster reString = (ReasonMaster) holder.mReason
                                            .getSelectedItem();

                                    holder.productObj.setReasonID(reString
                                            .getReasonID());

                                    Commons.print("Reason ID>>>>>>>>>>>>>," + "" + holder.productObj.getReasonID());
                                }

                                public void onNothingSelected(
                                        AdapterView<?> parent) {
                                }
                            });


                    holder.shelfPcsQty
                            .addTextChangedListener(new TextWatcher() {

                                public void afterTextChanged(Editable s) {
                                    String qty = s.toString();
                                    if (!qty.equals("")) {
                                        int sp_qty = SDUtil
                                                .convertToInt(holder.shelfPcsQty
                                                        .getText().toString());

                                        holder.productObj.getLocations()
                                                .get(mSelectedLocationIndex)
                                                .setShelfPiece(sp_qty);

                                        int totValue = getProductTotalValue(holder.productObj);

                                        holder.total
                                                .setText(totValue + "");
                                        if (bmodel.configurationMasterHelper.SHOW_STOCK_CB) {
                                            if (!holder.avail_cb.isChecked()
                                                    && totValue > 0)
                                                holder.avail_cb.setChecked(true);
                                            else if (holder.avail_cb.isChecked()
                                                    && totValue > 0)
                                                holder.avail_cb.setChecked(true);
                                            else if (totValue <= 0) {
                                                holder.avail_cb.setChecked(false);
                                            }
                                        }
                                    } else {
                                        holder.productObj.getLocations()
                                                .get(mSelectedLocationIndex)
                                                .setShelfPiece(-1);
                                        int totValue = getProductTotalValue(holder.productObj);

                                        holder.total
                                                .setText(totValue + "");
                                        if (totValue <= 0) {
                                            holder.avail_cb.setChecked(false);
                                        }
                                    }
                                }

                                @Override
                                public void beforeTextChanged(CharSequence s,
                                                              int start, int count, int after) {
                                }

                                @Override
                                public void onTextChanged(CharSequence s,
                                                          int start, int before, int count) {
                                }
                            });

                    holder.shelfCaseQty
                            .addTextChangedListener(new TextWatcher() {

                                public void afterTextChanged(Editable s) {
                                    String qty = s.toString();
                                    if (!qty.equals("")) {
                                        int scqty = SDUtil
                                                .convertToInt(holder.shelfCaseQty
                                                        .getText().toString());

                                        holder.productObj.getLocations()
                                                .get(mSelectedLocationIndex)
                                                .setShelfCase(scqty);
                                        int totValue = getProductTotalValue(holder.productObj);

                                        holder.total
                                                .setText(totValue + "");
                                        if (bmodel.configurationMasterHelper.SHOW_STOCK_CB) {
                                            if (!holder.avail_cb.isChecked()
                                                    && totValue > 0)
                                                holder.avail_cb.setChecked(true);
                                            else if (holder.avail_cb.isChecked()
                                                    && totValue > 0)
                                                holder.avail_cb.setChecked(true);
                                            else if (totValue <= 0) {
                                                holder.avail_cb.setChecked(false);
                                            }
                                        }
                                    } else {
                                        holder.productObj.getLocations()
                                                .get(mSelectedLocationIndex)
                                                .setShelfCase(-1);
                                        int totValue = getProductTotalValue(holder.productObj);

                                        holder.total
                                                .setText(totValue + "");
                                        if (totValue <= 0) {
                                            holder.avail_cb.setChecked(false);
                                        }
                                    }
                                }

                                @Override
                                public void beforeTextChanged(CharSequence s,
                                                              int start, int count, int after) {
                                }

                                @Override
                                public void onTextChanged(CharSequence s,
                                                          int start, int before, int count) {
                                }
                            });

                    holder.shelfouter.addTextChangedListener(new TextWatcher() {

                        @Override
                        public void onTextChanged(CharSequence s, int start,
                                                  int before, int count) {

                        }

                        @Override
                        public void beforeTextChanged(CharSequence s,
                                                      int start, int count, int after) {

                        }

                        @Override
                        public void afterTextChanged(Editable s) {
                            String qty = s.toString();
                            if (!qty.equals("")) {
                                int shelfoqty = SDUtil
                                        .convertToInt(holder.shelfouter
                                                .getText().toString());
                                holder.productObj.getLocations()
                                        .get(mSelectedLocationIndex)
                                        .setShelfOuter(shelfoqty);
                                int totValue = getProductTotalValue(holder.productObj);
                                holder.total
                                        .setText(totValue + "");
                                if (bmodel.configurationMasterHelper.SHOW_STOCK_CB) {
                                    if (!holder.avail_cb.isChecked()
                                            && totValue > 0)
                                        holder.avail_cb.setChecked(true);
                                    else if (totValue <= 0) {
                                        holder.avail_cb.setChecked(false);
                                    }
                                }
                            } else {
                                holder.productObj.getLocations()
                                        .get(mSelectedLocationIndex)
                                        .setShelfOuter(-1);
                                int totValue = getProductTotalValue(holder.productObj);

                                holder.total
                                        .setText(totValue + "");
                                if (totValue <= 0) {
                                    holder.avail_cb.setChecked(false);
                                }
                            }

                        }
                    });


                    holder.facingQty.addTextChangedListener(new TextWatcher() {

                        @Override
                        public void onTextChanged(CharSequence s, int start,
                                                  int before, int count) {

                        }

                        @Override
                        public void beforeTextChanged(CharSequence s,
                                                      int start, int count, int after) {

                        }

                        @Override
                        public void afterTextChanged(Editable s) {
                            String qty = s.toString();
                            if (!qty.equals("")) {
                                int wcqty = SDUtil
                                        .convertToInt(holder.facingQty
                                                .getText().toString());

                                holder.productObj.getLocations()
                                        .get(mSelectedLocationIndex)
                                        .setFacingQty(wcqty);
                                String strProductObj = getProductTotalValue(holder.productObj)
                                        + "";
                                holder.total
                                        .setText(strProductObj);
                            } else {
                                holder.facingQty.setText("0");
                            }
                        }
                    });

                    holder.facingQty
                            .setOnTouchListener(new OnTouchListener() {
                                public boolean onTouch(View v, MotionEvent event) {
                                    productName.setText(holder.pname);
                                    QUANTITY = holder.facingQty;
                                    QUANTITY.setTag(holder.productObj);
                                    int inType = holder.facingQty
                                            .getInputType();
                                    holder.facingQty
                                            .setInputType(InputType.TYPE_NULL);
                                    holder.facingQty.onTouchEvent(event);
                                    holder.facingQty.setInputType(inType);
                                    holder.facingQty.selectAll();
                                    holder.facingQty.requestFocus();
                                    inputManager.hideSoftInputFromWindow(
                                            mEdt_searchproductName
                                                    .getWindowToken(), 0);
                                    return true;
                                }
                            });

                    holder.shelfPcsQty
                            .setOnTouchListener(new OnTouchListener() {
                                public boolean onTouch(View v, MotionEvent event) {

                                    QUANTITY = holder.shelfPcsQty;
                                    QUANTITY.setTag(holder.productObj);
                                    int inType = holder.shelfPcsQty
                                            .getInputType();
                                    holder.shelfPcsQty
                                            .setInputType(InputType.TYPE_NULL);
                                    holder.shelfPcsQty.onTouchEvent(event);
                                    holder.shelfPcsQty.setInputType(inType);
                                    holder.shelfPcsQty.selectAll();
                                    holder.shelfPcsQty.requestFocus();
                                    inputManager.hideSoftInputFromWindow(
                                            mEdt_searchproductName
                                                    .getWindowToken(), 0);
                                    return true;
                                }
                            });

                    holder.shelfCaseQty
                            .setOnTouchListener(new OnTouchListener() {
                                public boolean onTouch(View v, MotionEvent event) {

                                    QUANTITY = holder.shelfCaseQty;
                                    QUANTITY.setTag(holder.productObj);
                                    int inType = holder.shelfCaseQty
                                            .getInputType();
                                    holder.shelfCaseQty
                                            .setInputType(InputType.TYPE_NULL);
                                    holder.shelfCaseQty.onTouchEvent(event);
                                    holder.shelfCaseQty.setInputType(inType);
                                    holder.shelfCaseQty.selectAll();
                                    holder.shelfCaseQty.requestFocus();
                                    inputManager.hideSoftInputFromWindow(
                                            mEdt_searchproductName
                                                    .getWindowToken(), 0);
                                    return true;
                                }
                            });

                    holder.shelfouter.setOnTouchListener(new OnTouchListener() {
                        public boolean onTouch(View v, MotionEvent event) {

                            QUANTITY = holder.shelfouter;
                            QUANTITY.setTag(holder.productObj);
                            int inType = holder.shelfouter.getInputType();
                            holder.shelfouter.setInputType(InputType.TYPE_NULL);
                            holder.shelfouter.onTouchEvent(event);
                            holder.shelfouter.setInputType(inType);
                            holder.shelfouter.selectAll();
                            holder.shelfouter.requestFocus();
                            inputManager.hideSoftInputFromWindow(
                                    mEdt_searchproductName.getWindowToken(), 0);
                            return true;
                        }
                    });


                    row.setOnClickListener(new OnClickListener() {
                        public void onClick(View v) {

                            productName.setText(holder.pname);

                            inputManager.hideSoftInputFromWindow(
                                    mEdt_searchproductName.getWindowToken(), 0);
                            //mEdt_searchproductName.setText("");

                            if (viewFlipper.getDisplayedChild() != 0) {
                                viewFlipper.showPrevious();
                            }
                        }
                    });

                    row.setOnLongClickListener(new OnLongClickListener() {
                        public boolean onLongClick(View v) {
                            bmodel = (BusinessModel) getActivity().getApplicationContext();
                            bmodel.setContext(getActivity());
                            List<SchemeBO> schemeList = null;
                            try {
                                schemeList = bmodel.schemeDetailsMasterHelper
                                        .getSchemesByProduct(holder.productId);
                            } catch (Exception e) {
                                Commons.printException(e + "");
                            }
                            if (bmodel.configurationMasterHelper.IS_SCHEME_DIALOG) {
                                if (schemeList == null
                                        || schemeList.size() == 0) {
                                    Toast.makeText(getActivity(),
                                            "scheme not available.",
                                            Toast.LENGTH_SHORT).show();
                                    return true;
                                }
                                bmodel.productHelper.setSchemes(bmodel.schemeDetailsMasterHelper.getmSchemeList());
                                bmodel.productHelper.setPdname(holder.pname);
                                bmodel.productHelper.setProdId(holder.productId);
                                bmodel.productHelper.setProductObj(holder.productObj);
                                bmodel.productHelper.setFlag(1);
                                bmodel.productHelper.setTotalScreenSize(0);

                                Intent intent = new Intent(getActivity(), ProductSchemeDetailsActivity.class);
                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                startActivity(intent);

                            } else {
                                bmodel.productHelper.setPdname(holder.pname);
                                bmodel.productHelper.setProdId(holder.productId);
                                bmodel.productHelper.setProductObj(holder.productObj);
                                bmodel.productHelper.setFlag(1);
                                bmodel.productHelper.setTotalScreenSize(0);

                                SchemeDialog sc = new SchemeDialog(
                                        getActivity(), schemeList,
                                        holder.pname, holder.productId,
                                        holder.productObj, 1, 0);
                                FragmentManager fm = getActivity().getSupportFragmentManager();
                                sc.show(fm, "");
                            }
                            return true;
                        }
                    });

                    if (bmodel.configurationMasterHelper.IS_TEAMLEAD) {
                        holder.audit.setVisibility(View.VISIBLE);
                        holder.avail_cb.setEnabled(false);

                        holder.shelfPcsQty.setEnabled(false);
                        holder.shelfCaseQty.setEnabled(false);

                        holder.mReason.setEnabled(false);


                    }
                    row.setTag(holder);
                } else {
                    holder = (ViewHolder) row.getTag();
                }

                holder.productObj = product;

                holder.productId = holder.productObj.getProductID();

                try {
                    holder.psname.setTextColor(holder.productObj.getTextColor());
                } catch (Exception e) {
                    Commons.printException(e);
                    holder.psname.setTextColor(ContextCompat.getColor(getActivity(),
                            android.R.color.black));
                }


                if (holder.productObj
                        .getLocations()
                        .get(mSelectedLocationIndex)
                        .getAudit() == 2)
                    holder.audit.setImageResource(R.drawable.ic_audit_none);
                else if (holder.productObj
                        .getLocations()
                        .get(mSelectedLocationIndex)
                        .getAudit() == 1)
                    holder.audit.setImageResource(R.drawable.ic_audit_yes);
                else if (holder.productObj
                        .getLocations()
                        .get(mSelectedLocationIndex)
                        .getAudit() == 0)
                    holder.audit.setImageResource(R.drawable.ic_audit_no);

                holder.pname = holder.productObj.getProductName();

                holder.psname.setText(holder.productObj.getProductShortName());
                String strPPQ = getResources().getString(R.string.ppq) + ": "
                        + holder.productObj.getRetailerWiseProductWiseP4Qty() + "";
                holder.ppq.setText(strPPQ);
                String strPSQ = getResources().getString(R.string.psq) + ": "
                        + holder.productObj.getRetailerWiseP4StockQty();
                holder.psq.setText(strPSQ);


                if (!bmodel.configurationMasterHelper.SHOW_STOCK_SP
                        && !bmodel.configurationMasterHelper.SHOW_STOCK_SC
                        && !bmodel.configurationMasterHelper.SHOW_SHELF_OUTER) {
                    if (holder.productObj.getLocations()
                            .get(mSelectedLocationIndex)
                            .getShelfPiece() == 1)
                        holder.avail_cb.setChecked(true);
                    else if (holder.productObj.getLocations()
                            .get(mSelectedLocationIndex)
                            .getShelfPiece() == -1)
                        holder.avail_cb.setChecked(false);
                }

                if (bmodel.configurationMasterHelper.SHOW_STOCK_RSN) {
                    if (holder.productObj.getLocations()
                            .get(mSelectedLocationIndex)
                            .getShelfPiece() > 0) {
                        holder.mReason.setEnabled(false);
                        holder.mReason.setSelected(false);
                        holder.mReason.setSelection(0);
                    } else {
                        holder.mReason.setEnabled(true);
                        holder.mReason.setSelected(true);
                        holder.mReason.setSelection(getReasonIndex(holder.productObj
                                .getReasonID()));
                    }
                }
                if (bmodel.configurationMasterHelper.SHOW_STOCK_FC) {
                    String strFacingQty = holder.productObj.getLocations().get(mSelectedLocationIndex).getFacingQty() + "";
                    holder.facingQty.setText(strFacingQty);
                }

                if (bmodel.configurationMasterHelper.SHOW_STOCK_SP) {
                    if (holder.productObj.getLocations()
                            .get(mSelectedLocationIndex).getShelfPiece() >= 0) {
                        String strShelfPiece = holder.productObj.getLocations()
                                .get(mSelectedLocationIndex).getShelfPiece()
                                + "";
                        holder.shelfPcsQty.setText(strShelfPiece.equals("0") ? "" : strShelfPiece);
                    } else {
                        holder.shelfPcsQty.setText("");
                    }
                }

                if (bmodel.configurationMasterHelper.SHOW_STOCK_SC) {
                    if (holder.productObj.getLocations()
                            .get(mSelectedLocationIndex).getShelfCase() >= 0) {
                        String strShelfCase = holder.productObj.getLocations()
                                .get(mSelectedLocationIndex).getShelfCase()
                                + "";
                        holder.shelfCaseQty.setText(strShelfCase.equals("0") ? "" : strShelfCase);
                    } else {
                        holder.shelfCaseQty.setText("");
                    }
                }
                if (bmodel.configurationMasterHelper.SHOW_SHELF_OUTER) {
                    if (holder.productObj.getLocations()
                            .get(mSelectedLocationIndex).getShelfOuter() >= 0) {
                        String strShelfOuter = holder.productObj.getLocations()
                                .get(mSelectedLocationIndex).getShelfOuter()
                                + "";
                        holder.shelfouter.setText(strShelfOuter.equals("0") ? "" : strShelfOuter);
                    } else {
                        holder.shelfouter.setText("");
                    }
                }

                TypedArray typearr = getActivity().getTheme().obtainStyledAttributes(R.styleable.MyTextView);
                if (position % 2 == 0) {
                    row.setBackgroundColor(typearr.getColor(R.styleable.MyTextView_listcolor_alt, 0));
                } else {
                    row.setBackgroundColor(typearr.getColor(R.styleable.MyTextView_listcolor, 0));
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

            } catch (Exception e) {
                Commons.printException(e + "");
            }
            return row;
        }
    }

    public class ViewHolder {
        private CheckBox avail_cb;


        private String productId;
        private String pname;
        private ProductMasterBO productObj;
        private TextView psname;
        private TextView ppq;
        private TextView psq;
        private EditText shelfPcsQty;
        private EditText shelfCaseQty;

        private EditText shelfouter;
        private EditText facingQty;


        private TextView total;


        private LinearLayout ll_stkCB;

        private Spinner mReason;
        ImageButton audit;
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
            if (!generalbutton.equals(GENERAL))
                menu.findItem(R.id.menu_spl_filter).setIcon(
                        R.drawable.ic_action_star_select);

            if (!brandbutton.equals(BRAND))
                menu.findItem(R.id.menu_product_filter).setIcon(
                        R.drawable.ic_action_filter_select);

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

            if (scheme_button_enable)
                menu.findItem(R.id.menu_scheme).setVisible(!drawerOpen);
            else
                menu.findItem(R.id.menu_scheme).setVisible(false);

            if (isSpecialFilter_enabled && !bmodel.configurationMasterHelper.IS_SPL_FILTER_TAB)
                menu.findItem(R.id.menu_spl_filter).setVisible(!drawerOpen);
            else
                menu.findItem(R.id.menu_spl_filter).setVisible(false);

            menu.findItem(R.id.menu_apply_so).setVisible(false);

            menu.findItem(R.id.menu_apply_std_qty).setVisible(false);

            if (bmodel.configurationMasterHelper.IS_GLOBAL_LOCATION)
                menu.findItem(R.id.menu_loc_filter).setVisible(false);
            else {
                if (bmodel.productHelper.getInStoreLocation().size() < 2)
                    menu.findItem(R.id.menu_loc_filter).setVisible(false);
            }

            menu.findItem(R.id.menu_sih_apply).setVisible(false);

            menu.findItem(R.id.menu_sih_apply).setVisible(false);
            menu.findItem(R.id.menu_product_filter).setVisible(false);
            menu.findItem(R.id.menu_fivefilter).setVisible(false);

            if (bmodel.configurationMasterHelper.IS_FIVE_LEVEL_FILTER && bmodel.productHelper.isFilterAvaiable("MENU_STK_ORD")) {
                menu.findItem(R.id.menu_fivefilter).setVisible(true);
                menu.findItem(R.id.menu_fivefilter).setVisible(!drawerOpen);
            } /*else {
                menu.findItem(R.id.menu_product_filter).setVisible(!drawerOpen);
                menu.findItem(R.id.menu_fivefilter).setVisible(false);
            }*/

            if (bmodel.configurationMasterHelper.IS_FIVE_LEVEL_FILTER && mSelectedIdByLevelId != null) {
                for (Integer id : mSelectedIdByLevelId.keySet()) {
                    if (mSelectedIdByLevelId.get(id) > 0) {
                        menu.findItem(R.id.menu_fivefilter).setIcon(
                                R.drawable.ic_action_filter_select);
                        break;
                    }
                }
            }
            if (bmodel.productHelper.getCompetitorFilterList() != null && bmodel.configurationMasterHelper.SHOW_COMPETITOR_FILTER) {
                menu.findItem(R.id.menu_competitor_filter).setVisible(true);
            }
            if (bmodel.configurationMasterHelper.IS_FIVE_LEVEL_FILTER && mSelectedIdByLevelId != null && !bmodel.isMapEmpty(mSelectedIdByLevelId)) {
                menu.findItem(R.id.menu_competitor_filter).setIcon(
                        R.drawable.ic_action_filter_select);

            }
            if (!bmodel.configurationMasterHelper.SHOW_REMARKS_STK_CHK) {
                hideRemarksButton();
                menu.findItem(R.id.menu_remarks).setVisible(false);
            } else
                menu.findItem(R.id.menu_remarks).setVisible(true);
            if (!bmodel.configurationMasterHelper.SHOW_MENU_ICON_SCHEME
                    || bmodel.configurationMasterHelper.IS_PRODUCT_DIALOG) {
                hideShemeButton();
                menu.findItem(R.id.menu_scheme).setVisible(false);
            } else
                menu.findItem(R.id.menu_scheme).setVisible(true);
            menu.findItem(R.id.menu_reason).setVisible(bmodel.configurationMasterHelper.floating_np_reason_photo);
            if (drawerOpen)
                menu.clear();
        } catch (Exception e) {
            Commons.printException(e + "");
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int i = item.getItemId();
        if (i == android.R.id.home) {
            if (mDrawerLayout.isDrawerOpen(GravityCompat.END))
                mDrawerLayout.closeDrawers();
            else {
                bmodel.productHelper.clearOrderTable();
                bmodel.outletTimeStampHelper.updateTimeStampModuleWise(SDUtil
                        .now(SDUtil.TIME));
                if (isFromChild)
                    startActivity(new Intent(getActivity(), HomeScreenTwo.class)
                            .putExtra("isStoreMenu", true));
                else
                    startActivity(new Intent(getActivity(), HomeScreenTwo.class));
                getActivity().finish();
            }
            getActivity().overridePendingTransition(R.anim.trans_right_in, R.anim.trans_right_out);
            return true;
        } else if (i == R.id.menu_survey) {
            startActivity(new Intent(getActivity(), SurveyActivityNew.class));
            return true;
        } else if (i == R.id.menu_product_filter) {
            if (bmodel.configurationMasterHelper.IS_UNLINK_FILTERS) {
                generalbutton = GENERAL;
                mSelectedFilterMap.put("General", GENERAL);
            }
            productFilterClickedFragment();
            getActivity().supportInvalidateOptionsMenu();
            return true;
        } else if (i == R.id.menu_loc_filter) {
            showLocation();
            return true;
        } else if (i == R.id.menu_spl_filter) {
            generalFilterClickedFragment();
            getActivity().supportInvalidateOptionsMenu();
            return true;
        } else if (i == R.id.menu_remarks) {
            onNoteButtonClick();
            return true;
        } else if (i == R.id.menu_apply_so) {
            return true;
        } else if (i == R.id.menu_apply_std_qty) {
            return true;
        } else if (i == R.id.menu_scheme) {
            loadSchemeDialog();
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
            ((IvyBaseActivityNoActionBar) getActivity()).checkAndRequestPermissionAtRunTime(2);
            int permissionStatus = ContextCompat.checkSelfPermission(getActivity(),
                    Manifest.permission.CAMERA);
            if (permissionStatus == PackageManager.PERMISSION_GRANTED) {
                //new IntentIntegrator(getActivity()).setBeepEnabled(false).initiateScan();
                IntentIntegrator integrator = new IntentIntegrator(getActivity()) {
                    @Override
                    protected void startActivityForResult(Intent intent, int code) {
                        StockCheckFragment.this.startActivityForResult(intent, IntentIntegrator.REQUEST_CODE); // REQUEST_CODE override
                    }
                };
                integrator.setBeepEnabled(false).initiateScan();
            } else {
                Toast.makeText(getActivity(),
                        getResources().getString(R.string.permission_enable_msg)
                                + " " + getResources().getString(R.string.permission_camera)
                        , Toast.LENGTH_LONG).show();
            }
            return true;
        } else if (i == R.id.menu_reason) {
            bmodel.reasonHelper.downloadNpReason(bmodel.retailerMasterBO.getRetailerID(), "MENU_STOCK");
            ReasonPhotoDialog dialog = new ReasonPhotoDialog();
            dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialog) {
                    if (bmodel.reasonHelper.isNpReasonPhotoAvaiable(bmodel.retailerMasterBO.getRetailerID(), "MENU_STOCK")) {
                        bmodel.saveModuleCompletion("MENU_STOCK");
                        getActivity().finish();
                    }
                }
            });
            Bundle args = new Bundle();
            args.putString("modulename", "MENU_STOCK");
            dialog.setCancelable(false);
            dialog.setArguments(args);
            dialog.show(getActivity().getSupportFragmentManager(), "ReasonDialogFragment");
            return true;
        } else if (i == R.id.menu_competitor_filter) {
            competitorFilterClickedFragment();
            getActivity().supportInvalidateOptionsMenu();
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

            android.support.v4.app.FragmentManager fm = getActivity().getSupportFragmentManager();
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

        builder = new AlertDialog.Builder(getActivity());
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
        getActivity().sendBroadcast(i);
    }

    private void onNoteButtonClick() {
        FragmentTransaction ft = getActivity()
                .getSupportFragmentManager().beginTransaction();
        RemarksDialog dialog = new RemarksDialog("MENU_CLOSING");
        dialog.setCancelable(false);
        dialog.show(ft, "stk_chk_remark");
    }


    private void loadSchemeDialog() {
        bmodel = (BusinessModel) getActivity().getApplicationContext();
        bmodel.setContext(getActivity());
        SchemeDialog sc = new SchemeDialog(getActivity(), null, "",
                "", null, 0, 0);

        //sc.show();
        FragmentManager fm = getActivity().getSupportFragmentManager();
        sc.show(fm, "");
    }

    private void onNextButtonClick() {
        if (bmodel.hasStockCheck() || bmodel.configurationMasterHelper.IS_LOAD_STOCK_COMPETITOR) {
            if (!bmodel.configurationMasterHelper.IS_REASON_FOR_ALL_NON_STOCK_PRODUCTS || bmodel.isReasonSelectedForAllProducts()) {
                new SaveAsyncTask().execute();
            } else {
                mDialog1(1);
            }
        } else {
            mDialog1(0);
        }
    }

    private void mDialog1(int type) {
        AlertDialog.Builder alertDialogBuilder1 = new AlertDialog.Builder(
                getActivity());
        if (type == 0) {
            alertDialogBuilder1
                    .setIcon(null)
                    .setCancelable(false)

                    .setTitle(getResources().getString(R.string.no_data_tosave))
                    .setPositiveButton(getResources().getString(R.string.ok),
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog,
                                                    int whichButton) {

                                }
                            });
        } else if (type == 1) {
            String text = " ";
            for (ConfigureBO configureBO : bmodel.configurationMasterHelper.getGenFilter()) {


                if (configureBO.getConfigCode().equalsIgnoreCase("Filt11")) {
                    if (text.length() > 1)
                        text += ",";
                    text += configureBO.getMenuName();
                } else if (configureBO.getConfigCode().equalsIgnoreCase("Filt12")) {
                    if (text.length() > 1)
                        text += ",";
                    text += configureBO.getMenuName();
                }
            }
            alertDialogBuilder1
                    .setIcon(null)
                    .setCancelable(false)

                    .setTitle(getResources().getString(R.string.reason_required_for) + text)
                    .setPositiveButton(getResources().getString(R.string.ok),
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog,
                                                    int whichButton) {

                                }
                            });
        }
        bmodel.applyAlertDialogTheme(alertDialogBuilder1);
        //AlertDialog alertDialog1 = alertDialogBuilder1.create();
      /*  TextView textView = (TextView) alertDialog1.findViewById(android.R.id.message);
        Typeface face=Typeface.createFromAsset(getActivity().getAssets(),"fonts/Roboto-Light.ttf");
        textView.setTypeface(face);*/
        //  alertDialog1.show();
    }

    private void loadSpecialFilterView(View view) {
        hscrl_spl_filter = (HorizontalScrollView) view.findViewById(R.id.hscrl_spl_filter);
        hscrl_spl_filter.setVisibility(View.VISIBLE);
        ll_spl_filter = (LinearLayout) view.findViewById(R.id.ll_spl_filter);
        ll_tab_selection = (LinearLayout) view.findViewById(R.id.ll_tab_selection);
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
//        for (int i = 0; i < 2; i++) {
            ConfigureBO config = bmodel.configurationMasterHelper.getGenFilter().get(i);
          /*  ViewGroup.LayoutParams layoutParams = new ViewGroup.LayoutParams(
                    (getResources().getDimensionPixelSize(getResources().getDimension(R.dimen.special_filter_item_width)*scale+0.5f)), ViewGroup.LayoutParams.WRAP_CONTENT);*/

            TypedArray typearr = getActivity().getTheme().obtainStyledAttributes(R.styleable.MyTextView);
            final int color = typearr.getColor(R.styleable.MyTextView_textColor, 0);
            Button tab;
            tab = new Button(getActivity());
            tab.setText(config.getMenuName());
            tab.setTag(config.getConfigCode());
            tab.setGravity(Gravity.CENTER);
            tab.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
            tab.setTextColor(color);
            tab.setMaxLines(1);
            tab.setTextSize(getResources().getDimension(R.dimen.special_filter_item_text_size));
            tab.setBackgroundColor(ContextCompat.getColor(getActivity(), android.R.color.transparent));
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

            Button tv_selection_identifier = new Button(getActivity());
            tv_selection_identifier.setTag(config.getConfigCode() + config.getMenuName());
            tv_selection_identifier.setWidth(width);
            tv_selection_identifier.setBackgroundColor(color);
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
                    ((TextView) view).setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
                    ((TextView) view).setText(config.getMenuName() + "(" + mylist.size() + ")");
                }
                if (view1 instanceof Button) {
                    view1.setVisibility(View.VISIBLE);
                }


            } else {
                if (view instanceof TextView) {
                    ((TextView) view).setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
                    ((TextView) view).setText(config.getMenuName());
                }
                if (view1 instanceof Button) {
                    view1.setVisibility(View.INVISIBLE);
                }

            }
        }
        if (!tag.toString().equalsIgnoreCase("All")) {
            selectedCompetitorId = "";
        }
        getActivity().supportInvalidateOptionsMenu();
    }

    private void selectTab(View pview, Object tag) {
        for (ConfigureBO config : bmodel.configurationMasterHelper.getGenFilter()) {
            View view = pview.findViewWithTag(config.getConfigCode());
            View view1 = pview.findViewWithTag(config.getConfigCode() + config.getMenuName());
            if (tag == config.getConfigCode()) {
                if (view instanceof TextView) {
                    ((TextView) view).setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
                    ((TextView) view).setText(config.getMenuName() + "(" + mylist.size() + ")");
                }
                if (view1 instanceof Button) {
                    view1.setVisibility(View.VISIBLE);
                }


            } else {
                if (view instanceof TextView) {
                    ((TextView) view).setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
                    ((TextView) view).setText(config.getMenuName());
                }
                if (view1 instanceof Button) {
                    view1.setVisibility(View.INVISIBLE);
                }

            }
        }
        if (!tag.toString().equalsIgnoreCase("All")) {
            selectedCompetitorId = "";
        }
        getActivity().supportInvalidateOptionsMenu();

    }

    private void productFilterClickedFragment() {
        try {
            QUANTITY = null;

            mDrawerLayout.openDrawer(GravityCompat.END);

            android.support.v4.app.FragmentManager fm = getActivity()
                    .getSupportFragmentManager();
            FilterFragment frag = (FilterFragment) fm
                    .findFragmentByTag("filter");
            android.support.v4.app.FragmentTransaction ft = fm
                    .beginTransaction();
            if (frag != null)
                ft.detach(frag);
            Bundle bundle = new Bundle();
            bundle.putString("filterName", BRAND);
            if (bmodel.productHelper.getChildLevelBo().size() > 0)
                bundle.putString("filterHeader", bmodel.productHelper
                        .getChildLevelBo().get(0).getProductLevel());
            else
                bundle.putString("filterHeader", bmodel.productHelper
                        .getParentLevelBo().get(0).getPl_productLevel());
            bundle.putSerializable("serilizeContent",
                    bmodel.productHelper.getChildLevelBo());

            if (bmodel.productHelper.getParentLevelBo() != null
                    && bmodel.productHelper.getParentLevelBo().size() > 0) {

                bundle.putBoolean("isFormBrand", true);

                bundle.putString("pfilterHeader", bmodel.productHelper
                        .getParentLevelBo().get(0).getPl_productLevel());

                bmodel.productHelper.setPlevelMaster(bmodel.productHelper
                        .getParentLevelBo());
            } else {
                bundle.putBoolean("isFormBrand", false);
                bundle.putString("isFrom", "STK");
            }

            // set Fragmentclass Arguments
            FilterFragment fragobj = new FilterFragment(mSelectedFilterMap);
            fragobj.setArguments(bundle);
            ft.replace(R.id.right_drawer, fragobj, "filter");
            ft.commit();
        } catch (Exception e) {
            Commons.printException(e + "");
        }
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
                    bmodel.mPriceTrackingHelper.savePriceTransaction(mylist);

                // save near expiry
                bmodel.saveNearExpiry();

                // Save closing stock
                bmodel.saveClosingStock();

                // Upadte isVisited Flag
                bmodel.updateIsVisitedFlag();

                // update review plan in DB
                bmodel.setReviewPlanInDB();
                bmodel.saveModuleCompletion(HomeScreenTwo.MENU_STOCK);

                return Boolean.TRUE;
            } catch (Exception e) {
                Commons.printException(e + "");
                return Boolean.FALSE;
            }
        }

        protected void onPreExecute() {
            builder = new AlertDialog.Builder(getActivity());

            customProgressDialog(builder, getResources().getString(R.string.saving));
            alertDialog = builder.create();
            alertDialog.show();
        }

        protected void onPostExecute(Boolean result) {
            // result is the value returned from doInBackground
            alertDialog.dismiss();
            if (result == Boolean.TRUE) {
                bmodel.outletTimeStampHelper.updateTimeStampModuleWise(SDUtil
                        .now(SDUtil.TIME));

                new CommonDialog(getActivity().getApplicationContext(), getActivity(),
                        "", getResources().getString(R.string.saved_successfully),
                        false, getActivity().getResources().getString(R.string.ok),
                        null, new CommonDialog.positiveOnClickListener() {
                    @Override
                    public void onPositiveButtonClick() {
                        Intent intent = new Intent(getActivity(), HomeScreenTwo.class);

                        Bundle extras = getActivity().getIntent().getExtras();
                        if (extras != null) {
                            intent.putExtra("IsMoveNextActivity", bmodel.configurationMasterHelper.MOVE_NEXT_ACTIVITY);
                            intent.putExtra("CurrentActivityCode", extras.getString("CurrentActivityCode", ""));
                        }

                        startActivity(intent);
                        getActivity().finish();

                    }
                }, new CommonDialog.negativeOnClickListener() {
                    @Override
                    public void onNegativeButtonClick() {
                    }
                }).show();
            }
        }
    }

    @Override
    public void loadStartVisit() {
    }

    public void numberPressed(View vw) {
        if (QUANTITY == null) {
            bmodel.showAlert(
                    getResources().getString(R.string.please_select_item), 0);
        } else {
            int id = vw.getId();
            if (id == R.id.calcdel) {
                String s = QUANTITY.getText().toString();
                if (!(s.length() == 0)) {
                    s = s.substring(0, s.length() - 1);
                    if (s.length() == 0) {
                        s = "";
                    }
                }
                QUANTITY.setText(s);
            } else {
                if (getView() != null) {
                    Button ed = (Button) getView().findViewById(vw.getId());
                    append = ed.getText().toString();
                }
                eff();
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

    @Override
    public void updateMultiSelectionCategory(List<Integer> mCategory) {

    }

    @Override
    public void updateMultiSelectionBrand(List<String> mFilterName,
                                          List<Integer> mFilterId) {

    }

    public void onClick(View v) {
        Button vw = (Button) v;
        bmodel = (BusinessModel) getActivity().getApplicationContext();
        bmodel.setContext(getActivity());
        if (vw == mBtn_Search) {
            viewFlipper.showNext();
        } else if (vw == mBtn_clear) {
            if (mEdt_searchproductName.getText().length() > 0)
                mEdt_searchproductName.setText("");
            loadProductList();

            try {
                InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(getActivity().getCurrentFocus().getWindowToken(), 0);
            } catch (Exception e) {
                Commons.printException(e);
            }
        } else if (vw == btn_save) {
            onNextButtonClick();
        }
    }

    @Override
    public boolean onEditorAction(TextView arg0, int arg1, KeyEvent arg2) {
        if (arg1 == EditorInfo.IME_ACTION_DONE) {
            if (arg0.getText().length() > 0) {
                getActivity().supportInvalidateOptionsMenu();
                InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(mEdt_searchproductName.getWindowToken(), 0);
            }
            loadSearchedList();
            return true;
        }
        return false;
    }

    @Override
    public void onResume() {
        super.onResume();

        //GA screen tracking
        BusinessModel.getInstance().trackScreenView("Stock Check");

        if (bmodel.userMasterHelper.getUserMasterBO().getUserid() == 0) {
            Toast.makeText(this.getActivity(),
                    getResources().getString(R.string.sessionout_loginagain),
                    Toast.LENGTH_SHORT).show();
            getActivity().finish();
        }
        Commons.print("OnResume Called");
        switchProfile();
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

            if (product.getLocations().get(i).getShelfPiece() > -1)
                totalQty += product.getLocations().get(i).getShelfPiece();
            if (product.getLocations().get(i).getShelfCase() > -1)
                totalQty += (product.getLocations().get(i).getShelfCase() * product
                        .getCaseSize());
            if (product.getLocations().get(i).getShelfOuter() > -1)
                totalQty += (product.getLocations().get(i).getShelfOuter() * product
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
                    if (levelBO.getProductID() == sku.getParentid()) {
                        if (sku.getIsSaleable() == 1 && sku.getOwn() == 1)
                            mylist.add(sku);
                    }
                }
            }
        } else if (bmodel.configurationMasterHelper.LOAD_STOCK_COMPETITOR == 1) {
            for (LevelBO levelBO : mParentIdList) {
                for (ProductMasterBO sku : items) {
                    if (levelBO.getProductID() == sku.getParentid()) {
                        if (sku.getIsSaleable() == 1 && sku.getOwn() == 0)
                            mylist.add(sku);

                    }
                }
            }
        } else if (bmodel.configurationMasterHelper.LOAD_STOCK_COMPETITOR == 2) {
            for (LevelBO levelBO : mParentIdList) {
                for (ProductMasterBO sku : items) {
                    if (levelBO.getProductID() == sku.getParentid()) {
                        if (sku.getIsSaleable() == 1)
                            mylist.add(sku);
                    }
                }
            }
        }
        mDrawerLayout.closeDrawers();
        refreshList();
    }

    @Override
    public void updateFromFiveLevelFilter(Vector<LevelBO> mParentIdList, HashMap<Integer, Integer> mSelectedIdByLevelId, ArrayList<Integer> mAttributeProducts, String mFilterText) {
        Vector<ProductMasterBO> items = bmodel.productHelper.getTaggedProducts();
        fiveFilter_productIDs = new ArrayList<>();
        brandbutton = filtertext;
        this.parentidList = parentidList;
        this.mAttributeProducts = mAttributeProducts;
        this.filtertext = filtertext;
        this.mSelectedIdByLevelId = mSelectedIdByLevelId;
        if (items == null) {
            bmodel.showAlert(
                    getResources().getString(R.string.no_products_exists), 0);
            return;
        }
        if (mSelectedIdByLevelId != null && bmodel.isMapEmpty(mSelectedIdByLevelId) == false) {
            selectedCompetitorId = "";
        }
            mylist = new ArrayList<>();
            //
            if (bmodel.configurationMasterHelper.LOAD_STOCK_COMPETITOR == 0) {// Only own products
                if (mAttributeProducts != null && !parentidList.isEmpty()) {//Both Product and attribute filter selected
                    for (LevelBO levelBO : parentidList) {
                        for (ProductMasterBO sku : items) {
                            if (levelBO.getProductID() == sku.getParentid()) {
                                if (sku.getIsSaleable() == 1 && sku.getOwn() == 1)
                                    if (mAttributeProducts.contains(Integer.parseInt(sku.getProductID()))) {
                                        mylist.add(sku);
                                        fiveFilter_productIDs.add(sku.getProductID());
                                    }
                            }
                        }
                    }
                } else if (mAttributeProducts == null && !parentidList.isEmpty()) {// product filter alone selected
                    for (LevelBO levelBO : parentidList) {
                        for (ProductMasterBO sku : items) {
                            if (levelBO.getProductID() == sku.getParentid()) {
                                if (sku.getIsSaleable() == 1 && sku.getOwn() == 1)
                                    mylist.add(sku);
                                fiveFilter_productIDs.add(sku.getProductID());
                            }
                        }
                    }
                } else if (mAttributeProducts != null && !parentidList.isEmpty()) {// Attribute filter alone selected
                    for (int pid : mAttributeProducts) {
                        for (ProductMasterBO sku : items) {
                            if (pid == Integer.parseInt(sku.getProductID())) {
                                if (sku.getIsSaleable() == 1 && sku.getOwn() == 1)
                                    mylist.add(sku);
                                fiveFilter_productIDs.add(sku.getProductID());
                            }
                        }
                    }
                }
            } else if (bmodel.configurationMasterHelper.LOAD_STOCK_COMPETITOR == 1) {// Only competitor products
                if (mAttributeProducts != null && !parentidList.isEmpty()) {//Both Product and attribute filter selected
                    for (LevelBO levelBO : parentidList) {
                        for (ProductMasterBO sku : items) {
                            if (levelBO.getProductID() == sku.getParentid()) {
                                if (sku.getIsSaleable() == 1 && sku.getOwn() == 0)
                                    if (mAttributeProducts.contains(Integer.parseInt(sku.getProductID()))) {
                                        mylist.add(sku);
                                        fiveFilter_productIDs.add(sku.getProductID());
                                    }
                            }
                        }
                    }
                } else if (mAttributeProducts == null && !parentidList.isEmpty()) {// product filter alone selected
                    for (LevelBO levelBO : parentidList) {
                        for (ProductMasterBO sku : items) {
                            if (levelBO.getProductID() == sku.getParentid()) {
                                if (sku.getIsSaleable() == 1 && sku.getOwn() == 0)
                                    mylist.add(sku);
                                fiveFilter_productIDs.add(sku.getProductID());
                            }
                        }
                    }
                } else if (mAttributeProducts != null && !parentidList.isEmpty()) {// Attribute filter alone selected
                    for (int pid : mAttributeProducts) {
                        for (ProductMasterBO sku : items) {
                            if (pid == Integer.parseInt(sku.getProductID())) {
                                if (sku.getIsSaleable() == 1 && sku.getOwn() == 0)
                                    mylist.add(sku);
                                fiveFilter_productIDs.add(sku.getProductID());
                            }
                        }
                    }
                }
            } else if (bmodel.configurationMasterHelper.LOAD_STOCK_COMPETITOR == 2) {//Both Own and Competitor products
                if (mAttributeProducts != null && !parentidList.isEmpty()) {//Both Product and attribute filter selected
                    for (LevelBO levelBO : parentidList) {
                        for (ProductMasterBO sku : items) {
                            if (levelBO.getProductID() == sku.getParentid()) {
                                if (sku.getIsSaleable() == 1)
                                    if (mAttributeProducts.contains(Integer.parseInt(sku.getProductID()))) {
                                        mylist.add(sku);
                                        fiveFilter_productIDs.add(sku.getProductID());
                                    }
                            }
                        }
                    }
                } else if (mAttributeProducts == null && !parentidList.isEmpty()) {
                    for (LevelBO levelBO : parentidList) {// product filter alone selected
                        for (ProductMasterBO sku : items) {
                            if (levelBO.getProductID() == sku.getParentid()) {
                                if (sku.getIsSaleable() == 1)
                                    mylist.add(sku);
                                fiveFilter_productIDs.add(sku.getProductID());
                            }
                        }
                    }
                } else if (mAttributeProducts != null && !parentidList.isEmpty()) {
                    for (int pid : mAttributeProducts) {// Attribute filter alone selected
                        for (ProductMasterBO sku : items) {
                            if (pid == Integer.parseInt(sku.getProductID())) {
                                if (sku.getIsSaleable() == 1)
                                    mylist.add(sku);
                                fiveFilter_productIDs.add(sku.getProductID());
                            }
                        }
                    }
                }
            }

            if (bmodel.configurationMasterHelper.SHOW_SPL_FILTER) {
                Vector<ProductMasterBO> temp = new Vector<>();
                String generaltxt = generalbutton;
                for (ProductMasterBO ret : mylist) {
                    if (isSpecialFilterAppliedProduct(generaltxt, ret))
                        temp.add(ret);
                }
                mylist.clear();
                mylist.addAll(temp);
            }


            refreshList();
        if (selectedTabTag != null) {
            selectTab(selectedTabTag);
        }
        //}
        getActivity().invalidateOptionsMenu();
        mDrawerLayout.closeDrawers();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (result != null) {
            if (result.getContents() == null) {
                Toast.makeText(getActivity(), "Cancelled", Toast.LENGTH_LONG).show();
            } else {
                strBarCodeSearch = result.getContents();
            }
        } else {
            Toast.makeText(getActivity(), getResources().getString(R.string.no_match_found), Toast.LENGTH_LONG).show();
        }
    }

    private void updateFooter() {

        int totalAvailableProduts = 0;
        for (ProductMasterBO bo : mylist) {

            for (LocationBO locationBO : bo.getLocations()) {

                if (locationBO.getShelfCase() > 0 || locationBO.getShelfOuter() > 0 || locationBO.getShelfPiece() > 0) {
                    totalAvailableProduts += 1;
                    break;
                }
            }
        }

        tv_total_stockCheckedProducts.setText(totalAvailableProduts + "");
        tv_total_products.setText("/" + mylist.size());

    }

    private String getDefaultFilter() {
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

    /**
     * Special Filter Fragment.
     */
    private void generalFilterClickedFragment() {
        try {
            QUANTITY = null;
            mDrawerLayout.openDrawer(GravityCompat.END);
            android.support.v4.app.FragmentManager fm = getActivity().getSupportFragmentManager();
            FilterFragment frag = (FilterFragment) fm
                    .findFragmentByTag("filter");
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
            ft.add(R.id.right_drawer, fragobj, "filter");
            ft.commit();
        } catch (Exception e) {
            Commons.printException(e + "");
        }
    }

    private boolean applyProductAndSpecialFilter(ProductMasterBO ret) {
        if (!GENERAL.equals(generalbutton) && !BRAND.equals(brandbutton)) {
            // both filter selected
            if (bmodel.configurationMasterHelper.IS_FIVE_LEVEL_FILTER) {
                if (fiveFilter_productIDs != null && fiveFilter_productIDs.contains(ret.getProductID())
                        && isSpecialFilterAppliedProduct(generalbutton, ret))
                    return true;
            } else {
                if (ret.getParentid() == mSelectedBrandID && isSpecialFilterAppliedProduct(generalbutton, ret))
                    return true;
            }
        } else if (!GENERAL.equals(generalbutton) && BRAND.equals(brandbutton)) {
            //special filter alone selected
            if (isSpecialFilterAppliedProduct(generalbutton, ret))
                return true;
        } else if (GENERAL.equals(generalbutton) && !BRAND.equals(brandbutton)) {
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

    private void loadProductList() {
        try {
            Vector<ProductMasterBO> items = getTaggedProducts();

            int siz = items.size();
            mylist = new ArrayList<>();
            for (int i = 0; i < siz; ++i) {
                ProductMasterBO ret = items.elementAt(i);
                if (ret.getIsSaleable() == 1) {
                    if (generalbutton.equals(GENERAL) && brandbutton.equals(BRAND))//No filters selected
                        mylist.add(ret);
                    else if (applyProductAndSpecialFilter(ret))
                        mylist.add(ret);
                }
            }
            refreshList();
            updateFooter();
        } catch (Exception e) {
            Commons.printException(e);
        }
    }

    private void competitorFilterClickedFragment() {
        try {
            QUANTITY = null;

            mDrawerLayout.openDrawer(GravityCompat.END);

            android.support.v4.app.FragmentManager fm = getActivity()
                    .getSupportFragmentManager();
            CompetitorFilterFragment frag = (CompetitorFilterFragment) fm
                    .findFragmentByTag("competitor filter");
            android.support.v4.app.FragmentTransaction ft = fm
                    .beginTransaction();
            if (frag != null)
                ft.detach(frag);


            // set Fragmentclass Arguments
            CompetitorFilterFragment fragobj = new CompetitorFilterFragment();
            Bundle b = new Bundle();
            b.putString("selectedCompetitorId", selectedCompetitorId);
            fragobj.setCompetitorFilterInterface(this);
            fragobj.setArguments(b);
            ft.replace(R.id.right_drawer, fragobj, "competitor filter");
            ft.commit();
        } catch (Exception e) {
            Commons.printException(e + "");
        }
    }
}
