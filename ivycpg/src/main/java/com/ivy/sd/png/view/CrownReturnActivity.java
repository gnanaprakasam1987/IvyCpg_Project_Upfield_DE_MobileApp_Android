package com.ivy.sd.png.view;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.TypedArray;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;
import android.widget.ViewFlipper;

import com.ivy.cpg.view.digitalcontent.DigitalContentActivity;
import com.ivy.cpg.view.order.OrderSummary;
import com.ivy.cpg.view.order.StockAndOrder;
import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.bo.ConfigureBO;
import com.ivy.sd.png.bo.LevelBO;
import com.ivy.sd.png.bo.ProductMasterBO;
import com.ivy.sd.png.commons.IvyBaseActivityNoActionBar;
import com.ivy.sd.png.commons.SDUtil;
import com.ivy.sd.png.model.BrandDialogInterface;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.provider.ConfigurationMasterHelper;
import com.ivy.sd.png.provider.SchemeDetailsMasterHelper;
import com.ivy.sd.png.util.Commons;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Vector;

public class CrownReturnActivity extends IvyBaseActivityNoActionBar implements
        BrandDialogInterface, OnClickListener, OnEditorActionListener {

    private BusinessModel bmodel;

    // Constants
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
    private final String msih = "Filt13";
    private final String mOOS = "Filt14";
    private final String mFocusBrand3 = "Filt20";
    private final String mFocusBrand4 = "Filt21";
    private final String mSMP = "Filt22";
    private String strBarCodeSearch = "ALL";

    // Views
    private ListView lvwplist;
    private Button mBtn_Search, mBtnFilterPopup,
            mBtn_clear;
    private String brandbutton, generalbutton;
    private EditText QUANTITY, mEdt_searchproductName;
    private String append = "";
    // Drawer Implimentation
    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mDrawerToggle;
    private LinearLayout llCrownProduct, llFreeProduct;
    // View Flipper
    private ViewFlipper viewFlipper;
    private TextView totalValueText, lpcText, distValue, productName,
            pnametitle, totalText, distText;
    private InputMethodManager inputManager;
    // Global Variables
    private HashMap<String, String> mSelectedFilterMap = new HashMap<String, String>();
    private Vector<ProductMasterBO> mylist;
    private Vector<ProductMasterBO> mSelectedCategoryWiseProductList;
    private int mSelectedBrandID = 0;
    private String mSelectedFiltertext = "Brand", screenCode = "MENU_STK_ORD";
    private MyAdapter mSchedule;
    private double totalvalue = 0;
    private String OrderedFlag;
    private String mSelectedFilter;
    private ArrayList<String> mSearchTypeArray = new ArrayList<String>();
    private Toolbar toolbar;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_crown_return);

        bmodel = (BusinessModel) getApplicationContext();
        bmodel.setContext(this);
        viewInitializaton();
        hideView();

        setSupportActionBar(toolbar);

        Bundle extras = getIntent().getExtras();
        OrderedFlag = "Nothing";
        if (savedInstanceState == null) {
            if (extras != null) {
                OrderedFlag = extras.getString("OrderFlag");
                screenCode = extras.getString("ScreenCode");
            }
        } else {
            OrderedFlag = (String) savedInstanceState
                    .getSerializable("OrderFlag");
        }
        // set a custom shadow that overlays the main content when the drawer
        // opens
        mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow,
                GravityCompat.START);
        mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow,
                GravityCompat.END);

        mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);

        // Set title to toolbar
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        getSupportActionBar().setIcon(null);
        // Used to on / off the back arrow icon
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        // Used to remove the app logo actionbar icon and set title as home
        // (title support click)
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        // Used to hide the app logo icon from actionbar
        // getSupportActionBar().setDisplayUseLogoEnabled(false);

        // ActionBarDrawerToggle ties together the the proper interactions
        // between the sliding drawer and the action bar app icon
        mDrawerToggle = new ActionBarDrawerToggle(this, /* host Activity */
                mDrawerLayout, /* DrawerLayout object */
                R.drawable.ic_action_bottle_return, /* nav drawer image to replace 'Up' caret */
                R.string.ok, /* "open drawer" description for accessibility */
                R.string.close /* "close drawer" description for accessibility */
        ) {
            public void onDrawerClosed(View view) {
                getSupportActionBar().setTitle(
                        getResources().getString(R.string.crown));
                supportInvalidateOptionsMenu();
                // invalidateOptionsMenu(); // creates call to
                // onPrepareOptionsMenu()
            }

            public void onDrawerOpened(View drawerView) {
                getSupportActionBar().setTitle(
                        getResources().getString(R.string.filter));
                supportInvalidateOptionsMenu();
                // invalidateOptionsMenu(); // creates call to
                // onPrepareOptionsMenu()
            }
        };
        mDrawerLayout.setDrawerListener(mDrawerToggle);
        setScreenTitle(getResources().getString(R.string.crown));
        productName.setOnTouchListener(new OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                int inType = productName.getInputType();
                productName.setInputType(InputType.TYPE_NULL);
                productName.onTouchEvent(event);
                productName.setInputType(inType);
                return true;
            }
        });

        mSearchTypeArray = new ArrayList<>();
        mSearchTypeArray.add(getResources().getString(R.string.product_name));
        mSearchTypeArray.add(getResources().getString(R.string.order_gcas));
        mSearchTypeArray.add(getResources().getString(
                R.string.order_dialog_barcode));

        try {
            if (OrderedFlag.equals("FromSummary")) {
                if (bmodel.configurationMasterHelper.SHOW_SPL_FILTER) {
                    mSelectedFilterMap.put("General", mOrdered);
                    updateGeneralText(mOrdered);
                } else {
                    mSelectedFilterMap.put("General", GENERAL);
                    updateGeneralText(GENERAL);
                }
            } else {
                if (bmodel.configurationMasterHelper.SHOW_SPL_FILTER) {

                    String defaultfilter = getDefaultFilter();
                    if (!defaultfilter.equals("")) {
                        mSelectedFilterMap.put("General", defaultfilter);
                        updateGeneralText(defaultfilter);
                    } else {
                        mSelectedFilterMap.put("General", GENERAL);
                        updateGeneralText(GENERAL);
                    }
                } else {
                    mSelectedFilterMap.put("General", GENERAL);
                    updateGeneralText(GENERAL);
                }
            }
        } catch (Exception e) {
            Commons.printException(e);
        }

        try {
            mEdt_searchproductName.addTextChangedListener(new TextWatcher() {
                public void afterTextChanged(Editable s) {
                    brandbutton = BRAND;
                    generalbutton = GENERAL;
                    supportInvalidateOptionsMenu();
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
        updateBrandText("Brand", -1);

    }

    public void viewInitializaton() {

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        llCrownProduct = (LinearLayout) findViewById(R.id.ll_crown_product);
        llFreeProduct = (LinearLayout) findViewById(R.id.ll_free_product);
        inputManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        viewFlipper = (ViewFlipper) findViewById(R.id.view_flipper);
        totalValueText = (TextView) findViewById(R.id.totalValue);
        lpcText = (TextView) findViewById(R.id.lcp);
        distValue = (TextView) findViewById(R.id.distValue);
        totalText = (TextView) findViewById(R.id.totalText);
        distText = (TextView) findViewById(R.id.distText);
        mEdt_searchproductName = (EditText) findViewById(R.id.edt_searchproductName);
        mBtn_Search = (Button) findViewById(R.id.btn_search);
        mBtnFilterPopup = (Button) findViewById(R.id.btn_filter_popup);
        mBtn_clear = (Button) findViewById(R.id.btn_clear);
        productName = (TextView) findViewById(R.id.productName);
        mBtn_Search.setOnClickListener(this);
        mBtnFilterPopup.setOnClickListener(this);
        mBtn_clear.setOnClickListener(this);
        mEdt_searchproductName.setOnEditorActionListener(this);
        lvwplist = (ListView) findViewById(R.id.list);
        lvwplist.setCacheColorHint(0);

        ((LinearLayout) findViewById(R.id.ll_totqty)).setVisibility(View.GONE);//ll_totqty is not used, hence made invisible
        distText.setVisibility(View.GONE);
        distValue.setVisibility(View.GONE);

        mEdt_searchproductName = (EditText) findViewById(R.id.edt_searchproductName);
        mBtn_Search = (Button) findViewById(R.id.btn_search);
        mBtnFilterPopup = (Button) findViewById(R.id.btn_filter_popup);
        mBtn_clear = (Button) findViewById(R.id.btn_clear);
        mBtn_Search.setOnClickListener(this);
        mBtnFilterPopup.setOnClickListener(this);
        mBtn_clear.setOnClickListener(this);
        mEdt_searchproductName.setOnEditorActionListener(this);

        //green
        ((TextView) findViewById(R.id.totalText)).setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
        //value
        totalValueText.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.THIN));
        ((TextView) findViewById(R.id.lpc_title)).setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
        lpcText.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.THIN));
        ((TextView) findViewById(R.id.productBarcodetitle)).setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
        ((TextView) findViewById(R.id.productnametitle)).setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
        ((TextView) findViewById(R.id.crownTitle)).setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
        ((TextView) findViewById(R.id.tv_crown_caseTitle)).setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
        ((TextView) findViewById(R.id.tv_crown_outercaseTitle)).setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
        ((TextView) findViewById(R.id.tv_crown_pcsTitle)).setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
        ((TextView) findViewById(R.id.tv_crown_total)).setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
        ((TextView) findViewById(R.id.tv_freeTitle)).setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
        ((TextView) findViewById(R.id.tv_free_caseTitle)).setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
        ((TextView) findViewById(R.id.tv_free_outercaseTitle)).setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
        ((TextView) findViewById(R.id.tv_free_pcsTitle)).setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
        ((TextView) findViewById(R.id.tv_freeTotal)).setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
        productName.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
        mEdt_searchproductName.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));

        ((Button) findViewById(R.id.btn_next)).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {// if (bmodel.productHelper.hasCrownOrder()) {
                if (!bmodel.configurationMasterHelper.SHOW_CROWN_MANAGMENT
                        && bmodel.configurationMasterHelper.SHOW_FREE_PRODUCT_GIVEN) {
                    new calculateReturnProductValusAndQty().execute();
                } else {
                    if (bmodel.getOrderHeaderBO().getCrownCount() < bmodel.productHelper
                            .getTotalCrownQty())
                        bmodel.showAlert(
                                getResources().getString(
                                        R.string.crown_count_mismatch), 0);

                    else
                        new calculateReturnProductValusAndQty().execute();
                }
            /*
             * } else bmodel.showAlert(
			 * getResources().getString(R.string.no_products_exists), 0);
			 */

            }
        });

    }

    /**
     * Hide the Unused view Beacause the Overide the existing
     */
    private void hideView() {
        try {

            // On/Off order case and pcs
            if (!bmodel.configurationMasterHelper.SHOW_ORDER_CASE) {
                findViewById(R.id.tv_crown_caseTitle).setVisibility(View.GONE);
                findViewById(R.id.tv_free_caseTitle).setVisibility(View.GONE);
            } else {
                try {
                    if (bmodel.labelsMasterHelper.applyLabels(findViewById(
                            R.id.tv_crown_caseTitle).getTag()) != null)
                        ((TextView) findViewById(R.id.tv_crown_caseTitle))
                                .setText(bmodel.labelsMasterHelper
                                        .applyLabels(findViewById(
                                                R.id.tv_crown_caseTitle)
                                                .getTag()));

                    if (bmodel.labelsMasterHelper.applyLabels(findViewById(
                            R.id.tv_free_caseTitle).getTag()) != null)
                        ((TextView) findViewById(R.id.tv_free_caseTitle))
                                .setText(bmodel.labelsMasterHelper
                                        .applyLabels(findViewById(
                                                R.id.tv_free_caseTitle)
                                                .getTag()));
                } catch (Exception e) {
                    Commons.printException(e);
                }
            }
            if (!bmodel.configurationMasterHelper.SHOW_ORDER_PCS) {
                findViewById(R.id.tv_crown_pcsTitle).setVisibility(View.GONE);
                findViewById(R.id.tv_free_pcsTitle).setVisibility(View.GONE);
            } else {
                try {
                    if (bmodel.labelsMasterHelper.applyLabels(findViewById(
                            R.id.tv_crown_pcsTitle).getTag()) != null)
                        ((TextView) findViewById(R.id.tv_crown_pcsTitle))
                                .setText(bmodel.labelsMasterHelper
                                        .applyLabels(findViewById(
                                                R.id.tv_crown_pcsTitle)
                                                .getTag()));

                    if (bmodel.labelsMasterHelper.applyLabels(findViewById(
                            R.id.tv_free_pcsTitle).getTag()) != null)
                        ((TextView) findViewById(R.id.tv_free_pcsTitle))
                                .setText(bmodel.labelsMasterHelper
                                        .applyLabels(findViewById(
                                                R.id.tv_free_pcsTitle).getTag()));

                } catch (Exception e) {
                    Commons.printException(e);
                }
            }
            if (!bmodel.configurationMasterHelper.SHOW_OUTER_CASE) {
                findViewById(R.id.tv_crown_outercaseTitle).setVisibility(
                        View.GONE);
                findViewById(R.id.tv_free_outercaseTitle).setVisibility(
                        View.GONE);
            } else {
                try {
                    if (bmodel.labelsMasterHelper.applyLabels(findViewById(
                            R.id.tv_crown_outercaseTitle).getTag()) != null)
                        ((TextView) findViewById(R.id.tv_crown_outercaseTitle))
                                .setText(bmodel.labelsMasterHelper
                                        .applyLabels(findViewById(
                                                R.id.tv_crown_outercaseTitle)
                                                .getTag()));

                    if (bmodel.labelsMasterHelper.applyLabels(findViewById(
                            R.id.tv_free_outercaseTitle).getTag()) != null)
                        ((TextView) findViewById(R.id.tv_free_outercaseTitle))
                                .setText(bmodel.labelsMasterHelper
                                        .applyLabels(findViewById(
                                                R.id.tv_free_outercaseTitle)
                                                .getTag()));

                } catch (Exception e) {
                    Commons.printException(e);
                }
            }
            if (!bmodel.configurationMasterHelper.SHOW_BARCODE) {
                findViewById(R.id.productBarcodetitle).setVisibility(View.GONE);
            } else {
                try {
                    if (bmodel.labelsMasterHelper.applyLabels(findViewById(
                            R.id.productBarcodetitle).getTag()) != null)
                        ((TextView) findViewById(R.id.productBarcodetitle))
                                .setText(bmodel.labelsMasterHelper
                                        .applyLabels(findViewById(
                                                R.id.productBarcodetitle)
                                                .getTag()));
                } catch (Exception e) {
                    Commons.printException(e);
                }
            }
            try {
                if (bmodel.labelsMasterHelper.applyLabels(findViewById(
                        R.id.tv_crown_total).getTag()) != null)
                    ((TextView) findViewById(R.id.tv_crown_total))
                            .setText(bmodel.labelsMasterHelper
                                    .applyLabels(findViewById(
                                            R.id.tv_crown_total).getTag()));
            } catch (Exception e) {
                Commons.printException(e);
            }
            try {
                if (bmodel.labelsMasterHelper.applyLabels(findViewById(
                        R.id.tv_freeTotal).getTag()) != null)
                    ((TextView) findViewById(R.id.tv_freeTotal))
                            .setText(bmodel.labelsMasterHelper
                                    .applyLabels(findViewById(R.id.tv_freeTotal)
                                            .getTag()));
            } catch (Exception e) {
                Commons.printException(e);
            }

            if (!bmodel.configurationMasterHelper.SHOW_CROWN_MANAGMENT) {
                llCrownProduct.setVisibility(View.GONE);
                findViewById(R.id.tv_crown_total)
                        .setVisibility(View.GONE);
            }
            if (!bmodel.configurationMasterHelper.SHOW_FREE_PRODUCT_GIVEN) {
                llFreeProduct.setVisibility(View.GONE);
                findViewById(R.id.tv_freeTotal)
                        .setVisibility(View.GONE);
            }

        } catch (Exception e) {
            Commons.printException(e);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (bmodel.userMasterHelper.getUserMasterBO().getUserid() == 0) {
            Toast.makeText(this,
                    getResources().getString(R.string.sessionout_loginagain),
                    Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void generalFilterClickedFragment() {
        try {

            QUANTITY = null;
            Vector<String> vect = new Vector();
            for (String string : getResources().getStringArray(
                    R.array.productFilterArray)) {
                vect.add(string);
            }

            mDrawerLayout.openDrawer(GravityCompat.END);

            android.support.v4.app.FragmentManager fm = getSupportFragmentManager();
            FilterFragment frag = (FilterFragment) fm
                    .findFragmentByTag("filter");
            android.support.v4.app.FragmentTransaction ft = fm
                    .beginTransaction();
            if (frag != null)
                ft.detach(frag);
            Bundle bundle = new Bundle();
            bundle.putString("filterName", GENERAL);
            bundle.putBoolean("isFormBrand", false);
            // bundle.putStringArrayList("filterContent", new ArrayList<String>(
            // vect));

            bundle.putSerializable("serilizeContent",
                    bmodel.configurationMasterHelper.getGenFilter());

            // set Fragmentclass Arguments
            FilterFragment fragobj = new FilterFragment(mSelectedFilterMap);
            fragobj.setArguments(bundle);
            ft.add(R.id.right_drawer, fragobj, "filter");
            ft.commit();
        } catch (Exception e) {
            Commons.printException(e);
        }
    }

    private void productFilterClickedFragment() {
        try {

            QUANTITY = null;
            mDrawerLayout.openDrawer(GravityCompat.END);
            android.support.v4.app.FragmentManager fm = getSupportFragmentManager();
            FilterFragment frag = (FilterFragment) fm
                    .findFragmentByTag("filter");
            android.support.v4.app.FragmentTransaction ft = fm
                    .beginTransaction();
            if (frag != null)
                ft.detach(frag);
            Bundle bundle = new Bundle();
            bundle.putString("filterName", BRAND);
            bundle.putString("filterHeader", bmodel.productHelper
                    .getChildLevelBo().get(0).getProductLevel());
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
            ft.add(R.id.right_drawer, fragobj, "filter");
            ft.commit();
        } catch (Exception e) {
            Commons.printException(e);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
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
        if (!brandbutton.equals(BRAND))
            menu.findItem(R.id.menu_product_filter).setIcon(
                    R.drawable.ic_action_filter_select);

        menu.findItem(R.id.menu_crown).setVisible(true);
        menu.findItem(R.id.menu_remarks).setVisible(false);
        // If the nav drawer is open, hide action items related to the content
        // view
        boolean drawerOpen = mDrawerLayout.isDrawerOpen(GravityCompat.END);
        menu.findItem(R.id.menu_product_filter).setVisible(!drawerOpen);
        menu.findItem(R.id.menu_next).setVisible(!drawerOpen);
        menu.findItem(R.id.menu_spl_filter).setVisible(!drawerOpen);
        menu.findItem(R.id.menu_crown).setVisible(!drawerOpen);

        if (!bmodel.configurationMasterHelper.SHOW_SPL_FILTER)
            menu.findItem(R.id.menu_spl_filter).setVisible(false);

        if (!bmodel.configurationMasterHelper.SHOW_CROWN_MANAGMENT
                && bmodel.configurationMasterHelper.SHOW_FREE_PRODUCT_GIVEN)
            menu.findItem(R.id.menu_crown).setVisible(false);

        menu.findItem(R.id.menu_next).setVisible(false);
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int i = item.getItemId();
        if (i == android.R.id.home) {
            if (mDrawerLayout.isDrawerOpen(GravityCompat.END))
                mDrawerLayout.closeDrawers();
            else {
                if (bmodel.configurationMasterHelper.SHOW_BATCH_ALLOCATION) {
                    Intent intent = new Intent(CrownReturnActivity.this,
                            BatchAllocation.class);
                    intent.putExtra("OrderFlag", "Nothing");
                    intent.putExtra("ScreenCode", screenCode);
                    startActivity(intent);
                } else {
                    Intent intent;
                    if (screenCode.equals(HomeScreenTwo.MENU_CATALOG_ORDER)) {
                        intent = new Intent(CrownReturnActivity.this, CatalogOrder.class);
                    } else {
                        intent = new Intent(CrownReturnActivity.this, StockAndOrder.class);
                    }
                    intent.putExtra("OrderFlag", "Nothing");
                    intent.putExtra("ScreenCode", screenCode);
                    startActivity(intent);
                }
                finish();
                overridePendingTransition(R.anim.trans_right_in, R.anim.trans_right_out);
            }
            return true;
        } else if (i == R.id.menu_next) {// if (bmodel.productHelper.hasCrownOrder()) {
            if (!bmodel.configurationMasterHelper.SHOW_CROWN_MANAGMENT
                    && bmodel.configurationMasterHelper.SHOW_FREE_PRODUCT_GIVEN) {
                new calculateReturnProductValusAndQty().execute();
            } else {
                if (bmodel.getOrderHeaderBO().getCrownCount() < bmodel.productHelper
                        .getTotalCrownQty())
                    bmodel.showAlert(
                            getResources().getString(
                                    R.string.crown_count_mismatch), 0);

                else
                    new calculateReturnProductValusAndQty().execute();
            }
            /*
             * } else bmodel.showAlert(
			 * getResources().getString(R.string.no_products_exists), 0);
			 */

            return true;
        } else if (i == R.id.menu_product_filter) {// Normal Filter Fragment
            if (bmodel.configurationMasterHelper.IS_UNLINK_FILTERS) {
                generalbutton = GENERAL;
                mSelectedFilterMap.put("General", GENERAL);
            }
            productFilterClickedFragment();
            return true;
        } else if (i == R.id.menu_spl_filter) {
            generalFilterClickedFragment();
            return true;
        } else if (i == R.id.menu_crown) {
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            RemarksDialog dialog = new RemarksDialog("MENU_CROWN");
            dialog.setCancelable(false);
            dialog.show(ft, "MENU_CROWN");
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void nextBtnSubTask() {
        SchemeDetailsMasterHelper schemeHelper=SchemeDetailsMasterHelper.getInstance(getApplicationContext());
        if (schemeHelper.IS_SCHEME_ON
                && schemeHelper.IS_SCHEME_SHOW_SCREEN) {
            Intent init = new Intent(CrownReturnActivity.this,
                    SchemeApply.class);
            init.putExtra("ScreenCode", screenCode);
            startActivity(init);
//            finish();
        } else if (bmodel.configurationMasterHelper.SHOW_DISCOUNT_ACTIVITY) {
            Intent init = new Intent(CrownReturnActivity.this,
                    OrderDiscount.class);
            init.putExtra("ScreenCode", screenCode);
            startActivity(init);
//            finish();
        } else if (bmodel.configurationMasterHelper.IS_INITIATIVE) {
            Intent init = new Intent(CrownReturnActivity.this,
                    InitiativeActivity.class);
            init.putExtra("ScreenCode", screenCode);
            startActivity(init);
//            finish();
        } else if (bmodel.configurationMasterHelper.IS_PRESENTATION_INORDER) {
            Intent i = new Intent(CrownReturnActivity.this,
                    DigitalContentActivity.class);
            i.putExtra("ScreenCode", screenCode);
            i.putExtra("FromInit", "Initiative");
            startActivity(i);
//            finish();
        } else {
            Intent i = new Intent(CrownReturnActivity.this, OrderSummary.class);
            i.putExtra("ScreenCode", screenCode);
            startActivity(i);
//            finish();
        }
        overridePendingTransition(R.anim.trans_left_in, R.anim.trans_left_out);
        finish();
    }

    @Override
    public void updateMultiSelectionBrand(List<String> mFilterName,
                                          List<Integer> mFilterId) {

    }

    @Override
    public void updateMultiSelectionCategory(List<Integer> mCategory) {

    }

    @Override
    public void updateBrandText(String mFilterText, int bid) {

        mSelectedBrandID = bid;
        mSelectedFiltertext = mFilterText;
        try {
            // Close the drawer
            mDrawerLayout.closeDrawers();
            // Change the Brand button Name
            brandbutton = mFilterText;
            // Consider generalbutton text if it is dependent filter.
            String generaltxt = generalbutton;
            // Clear the productName
            productName.setText("");
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
            pnametitle = (TextView) findViewById(R.id.productnametitle);

            // Add the products into list
            for (int i = 0; i < siz; ++i) {
                ProductMasterBO ret = items.elementAt(i);

                if (ret.getBarCode().equals(strBarCodeSearch)
                        || ret.getCasebarcode().equals(strBarCodeSearch)
                        || ret.getOuterbarcode().equals(strBarCodeSearch)
                        || strBarCodeSearch.equals("ALL")) {
                    if ((bid == ret.getParentid() || bid == -1)
                            && ret.getIsSaleable() == 1) {
                        if (generaltxt.equals(mSbd) && ret.isRPS()) {
                            mylist.add(ret);
                        } else if (generaltxt.equals(mOrdered)
                                && (ret.getOrderedPcsQty() > 0
                                || ret.getOrderedCaseQty() > 0 || ret
                                .getOrderedOuterQty() > 0)) {
                            mylist.add(ret);
                        } else if (generaltxt.equals(mPurchased)
                                && ret.getIsPurchased() == 1) {
                            mylist.add(ret);
                        } else if (generaltxt.equals(mInitiative)
                                && ret.getIsInitiativeProduct() == 1) {
                            mylist.add(ret);
                        } else if (generaltxt.equals(mCommon)
                                && (ret.isRPS()
                                || (ret.getIsInitiativeProduct() == 1) || (ret
                                .getIsPurchased() == 1))) {
                            mylist.add(ret);
                        } else if (generaltxt.equals(mSbdGaps)
                                && (ret.isRPS() && !ret.isSBDAcheived())) {
                            // if (!ret.isSBDAcheivedLocal())
                            mylist.add(ret);
                        } else if (generaltxt.equals(GENERAL)) {
                            mylist.add(ret);
                        } else if (generaltxt.equals(mInStock)
                                && ret.getWSIH() > 0) {
                            mylist.add(ret);
                        } else if (generaltxt.equals(mOnAllocation)
                                && ret.isAllocation() == 1
                                && bmodel.configurationMasterHelper.IS_SIH_VALIDATION) {
                            mylist.add(ret);
                        } else if (generaltxt.equals(mPromo) && ret.isPromo()) {
                            mylist.add(ret);
                        } else if (generaltxt.equals(mMustSell)
                                && ret.getIsMustSell() == 1) {
                            mylist.add(ret);
                        } else if (generaltxt.equals(mFocusBrand)
                                && ret.getIsFocusBrand() == 1) {
                            mylist.add(ret);
                        } else if (generaltxt.equals(mFocusBrand2)
                                && ret.getIsFocusBrand2() == 1) {
                            mylist.add(ret);
                        } else if (generaltxt.equals(msih) && ret.getSIH() > 0) {
                            mylist.add(ret);
                        } else if (generaltxt.equals(mOOS) && ret.getOos() == 0) {
                            mylist.add(ret);
                        } else if (generaltxt.equals(mFocusBrand3) && ret.getIsFocusBrand3() == 1) {
                            mylist.add(ret);
                        } else if (generaltxt.equals(mFocusBrand4) && ret.getIsFocusBrand4() == 1) {
                            mylist.add(ret);
                        } else if (generaltxt.equals(mSMP) && ret.getIsSMP() == 1) {
                            mylist.add(ret);
                        }
                    }
                }
            }

            if (generaltxt.equals(GENERAL) && mFilterText.equals(BRAND))
                pnametitle.setText(getResources().getString(
                        R.string.product_name)
                        + "(" + mylist.size() + ")");
            else if (!generaltxt.equals(GENERAL)) {
                pnametitle.setText(getFilterName(generaltxt) + "("
                        + mylist.size() + ")");

            } else
                pnametitle.setText(mFilterText + "(" + mylist.size() + ")");

            // set the new list to listview

            mSelectedCategoryWiseProductList = mylist;
            mSchedule = new MyAdapter(mylist);
            lvwplist.setAdapter(mSchedule);

            strBarCodeSearch = "ALL";
            updateValue();
        } catch (Exception e) {
            Commons.printException(e);
        }
    }

    private ProductMasterBO product;

    private class MyAdapter extends ArrayAdapter<ProductMasterBO> {

        private Vector<ProductMasterBO> items;

        public MyAdapter(Vector<ProductMasterBO> items) {
            super(CrownReturnActivity.this,
                    R.layout.activity_stock_and_order_listview, items);
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
            product = items.get(position);

            View row = convertView;
            if (row == null) {
                LayoutInflater inflater = getLayoutInflater();
                row = inflater.inflate(R.layout.activity_crown_return_listview,
                        parent, false);
                holder = new ViewHolder();
                holder.tvbarcode = (TextView) row
                        .findViewById(R.id.stock_and_order_listview_productbarcode);
                holder.psname = (TextView) row
                        .findViewById(R.id.stock_and_order_listview_productname);

                holder.etCrownPcsQty = (EditText) row
                        .findViewById(R.id.stock_and_order_listview_pcs_qty);
                holder.etCrownCaseQty = (EditText) row
                        .findViewById(R.id.stock_and_order_listview_case_qty);

                holder.etCrownOuterQty = (EditText) row
                        .findViewById(R.id.stock_and_order_listview_outer_case_qty);
                holder.total = (TextView) row
                        .findViewById(R.id.stock_and_order_listview_total);

                holder.etFreePcsQty = (EditText) row
                        .findViewById(R.id.et_crownreturn_free_pcs_qty);
                holder.etFreeCaseQty = (EditText) row
                        .findViewById(R.id.et_crownreturn_free_case_qty);
                holder.etFreeOuterQty = (EditText) row
                        .findViewById(R.id.et_crownreturn_free_outer_case_qty);

                holder.freeTotal = (TextView) row
                        .findViewById(R.id.tv_free_product_total);

                holder.psname.setTypeface(bmodel.configurationMasterHelper.getProductNameFont());
                holder.tvbarcode.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
                holder.etCrownPcsQty.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
                holder.etCrownCaseQty.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
                holder.etCrownOuterQty.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
                holder.total.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
                holder.etFreePcsQty.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
                holder.etFreeCaseQty.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
                holder.etFreeOuterQty.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
                holder.freeTotal.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
                if (!bmodel.configurationMasterHelper.SHOW_BARCODE) {
                    holder.tvbarcode.setVisibility(View.GONE);
                }

                // On/Off order case and pce
                if (!bmodel.configurationMasterHelper.SHOW_ORDER_CASE) {
                    holder.etCrownCaseQty.setVisibility(View.GONE);
                    holder.etFreeCaseQty.setVisibility(View.GONE);
                }
                if (!bmodel.configurationMasterHelper.SHOW_ORDER_PCS) {
                    holder.etCrownPcsQty.setVisibility(View.GONE);
                    holder.etFreePcsQty.setVisibility(View.GONE);
                }

                if (!bmodel.configurationMasterHelper.SHOW_OUTER_CASE) {
                    holder.etCrownOuterQty.setVisibility(View.GONE);
                    holder.etFreeOuterQty.setVisibility(View.GONE);
                }

                if (!bmodel.configurationMasterHelper.SHOW_CROWN_MANAGMENT) {
                    holder.etCrownPcsQty.setVisibility(View.GONE);
                    holder.etCrownCaseQty.setVisibility(View.GONE);
                    holder.etCrownOuterQty.setVisibility(View.GONE);
                    holder.total.setVisibility(View.GONE);

                }
                if (!bmodel.configurationMasterHelper.SHOW_FREE_PRODUCT_GIVEN) {
                    holder.etFreePcsQty.setVisibility(View.GONE);
                    holder.etFreeCaseQty.setVisibility(View.GONE);
                    holder.etFreeOuterQty.setVisibility(View.GONE);
                    holder.freeTotal.setVisibility(View.GONE);
                }

                holder.etCrownOuterQty
                        .setOnTouchListener(new OnTouchListener() {

                            @Override
                            public boolean onTouch(View v, MotionEvent event) {
                                if (bmodel.configurationMasterHelper.SHOW_SIH_IN_PNAME)
                                    productName.setText("[SIH :"
                                            + holder.productObj.getSIH() + "] "
                                            + holder.pname);
                                else
                                    productName.setText(holder.pname);
                                QUANTITY = holder.etCrownOuterQty;
                                QUANTITY.setTag(holder.productObj);
                                int inType = holder.etCrownOuterQty
                                        .getInputType();
                                holder.etCrownOuterQty
                                        .setInputType(InputType.TYPE_NULL);
                                holder.etCrownOuterQty.onTouchEvent(event);
                                holder.etCrownOuterQty.setInputType(inType);
                                holder.etCrownOuterQty.selectAll();
                                holder.etCrownOuterQty.requestFocus();
                                inputManager.hideSoftInputFromWindow(
                                        mEdt_searchproductName.getWindowToken(),
                                        0);
                                return true;
                            }
                        });

                holder.etCrownPcsQty.setOnTouchListener(new OnTouchListener() {
                    public boolean onTouch(View v, MotionEvent event) {
                        if (bmodel.configurationMasterHelper.SHOW_SIH_IN_PNAME)
                            productName.setText("[SIH :"
                                    + holder.productObj.getSIH() + "] "
                                    + holder.pname);
                        else
                            productName.setText(holder.pname);
                        QUANTITY = holder.etCrownPcsQty;
                        QUANTITY.setTag(holder.productObj);
                        int inType = holder.etCrownPcsQty.getInputType();
                        holder.etCrownPcsQty.setInputType(InputType.TYPE_NULL);
                        holder.etCrownPcsQty.onTouchEvent(event);
                        holder.etCrownPcsQty.setInputType(inType);
                        holder.etCrownPcsQty.selectAll();
                        holder.etCrownPcsQty.requestFocus();
                        inputManager.hideSoftInputFromWindow(
                                mEdt_searchproductName.getWindowToken(), 0);
                        return true;
                    }
                });

                holder.etCrownCaseQty.setOnTouchListener(new OnTouchListener() {
                    public boolean onTouch(View v, MotionEvent event) {
                        if (bmodel.configurationMasterHelper.SHOW_SIH_IN_PNAME)
                            productName.setText("[SIH :"
                                    + holder.productObj.getSIH() + "] "
                                    + holder.pname);
                        else
                            productName.setText(holder.pname);
                        QUANTITY = holder.etCrownCaseQty;
                        QUANTITY.setTag(holder.productObj);
                        int inType = holder.etCrownCaseQty.getInputType();
                        holder.etCrownCaseQty.setInputType(InputType.TYPE_NULL);
                        holder.etCrownCaseQty.onTouchEvent(event);
                        holder.etCrownCaseQty.setInputType(inType);
                        holder.etCrownCaseQty.selectAll();
                        holder.etCrownCaseQty.requestFocus();
                        inputManager.hideSoftInputFromWindow(
                                mEdt_searchproductName.getWindowToken(), 0);
                        return true;
                    }
                });

                holder.etCrownPcsQty.addTextChangedListener(new TextWatcher() {
                    public void afterTextChanged(Editable s) {
                        if (holder.productObj.getPcUomid() == 0) {
                            holder.etCrownPcsQty.removeTextChangedListener(this);
                            holder.etCrownPcsQty.setText("0");
                            holder.etCrownPcsQty.addTextChangedListener(this);
                            return;
                        }

                        String qty = s.toString();

                        if (holder.productObj.isAllocation() == 1
                                && bmodel.configurationMasterHelper.IS_SIH_VALIDATION) {

                            /** Calculate the total Crown pcs qty **/
                            float totalQty = (((holder.productObj
                                    .getCrownOrderedCaseQty()
                                    + holder.productObj.getOrderedCaseQty() + holder.productObj
                                    .getFreeCaseQty()) * holder.productObj
                                    .getCaseSize())
                                    + (holder.productObj.getOrderedPcsQty()
                                    + holder.productObj
                                    .getFreePieceQty() + SDUtil
                                    .convertToInt(qty)) + ((holder.productObj
                                    .getCrownOrderedOuterQty()
                                    + holder.productObj.getOrderedOuterQty() + holder.productObj
                                    .getFreeOuterQty()) * holder.productObj
                                    .getOutersize()));

                            if (totalQty <= holder.productObj.getSIH()) {
                                if (!qty.equals("")) {
                                    holder.productObj
                                            .setCrownOrderedPieceQty(SDUtil
                                                    .convertToInt(qty));
                                }
                                double tot = (holder.productObj
                                        .getCrownOrderedCaseQty() * holder.productObj
                                        .getCsrp())
                                        + (holder.productObj
                                        .getCrownOrderedPieceQty() * holder.productObj
                                        .getSrp())
                                        + (holder.productObj
                                        .getCrownOrderedOuterQty() * holder.productObj
                                        .getOsrp());
                                holder.total.setText(bmodel.formatValue(tot));
                            } else {
                                /** Show Toast **/
                                Toast.makeText(
                                        CrownReturnActivity.this,
                                        String.format(
                                                getResources().getString(
                                                        R.string.exceed),
                                                holder.productObj.getSIH()),
                                        Toast.LENGTH_SHORT).show();
                                /**
                                 * Delete the last entered number and reset the
                                 * qty
                                 **/
                                holder.etCrownPcsQty.removeTextChangedListener(this);
                                holder.etCrownPcsQty.setText(qty.length() > 1 ? qty
                                        .substring(0, qty.length() - 1) : "0");
                                holder.etCrownPcsQty.addTextChangedListener(this);
                            }
                        } else {
                            if (!qty.equals("")) {
                                holder.productObj
                                        .setCrownOrderedPieceQty(SDUtil
                                                .convertToInt(qty));
                            }
                            double tot = (holder.productObj
                                    .getCrownOrderedCaseQty() * holder.productObj
                                    .getCsrp())
                                    + (holder.productObj
                                    .getCrownOrderedPieceQty() * holder.productObj
                                    .getSrp())
                                    + (holder.productObj
                                    .getCrownOrderedOuterQty() * holder.productObj
                                    .getOsrp());
                            holder.total.setText(bmodel.formatValue(tot) + "");
                        }
                    }

                    public void beforeTextChanged(CharSequence s, int start,
                                                  int count, int after) {
                    }

                    public void onTextChanged(CharSequence s, int start,
                                              int before, int count) {

                    }
                });

                holder.etCrownCaseQty.addTextChangedListener(new TextWatcher() {
                    public void afterTextChanged(Editable s) {
                        if (holder.productObj.getCaseSize() == 0) {
                            holder.etCrownCaseQty
                                    .removeTextChangedListener(this);
                            holder.etCrownCaseQty.setText("0");
                            holder.etCrownCaseQty.addTextChangedListener(this);
                            return;
                        }

                        String qty = s.toString();

                        if (holder.productObj.isAllocation() == 1
                                && bmodel.configurationMasterHelper.IS_SIH_VALIDATION) {

                            float totalQty = (((SDUtil.convertToInt(qty)
                                    + holder.productObj.getOrderedCaseQty() + holder.productObj
                                    .getFreeCaseQty()) * holder.productObj
                                    .getCaseSize())
                                    + (holder.productObj.getOrderedPcsQty()
                                    + holder.productObj.getFreePieceQty() + holder.productObj
                                    .getCrownOrderedPieceQty()) + ((holder.productObj
                                    .getCrownOrderedOuterQty()
                                    + holder.productObj.getOrderedOuterQty() + holder.productObj
                                    .getFreeOuterQty()) * holder.productObj
                                    .getOutersize()));

                            if (totalQty <= holder.productObj.getSIH()) {
                                if (!qty.equals("")) {
                                    holder.productObj
                                            .setCrownOrderedCaseQty(SDUtil
                                                    .convertToInt(qty));
                                }
                                double tot = (holder.productObj
                                        .getCrownOrderedCaseQty() * holder.productObj
                                        .getCsrp())
                                        + (holder.productObj
                                        .getCrownOrderedPieceQty() * holder.productObj
                                        .getSrp())
                                        + (holder.productObj
                                        .getCrownOrderedOuterQty() * holder.productObj
                                        .getOsrp());
                                holder.total.setText(bmodel.formatValue(tot));
                            } else {
                                /** Show Toast **/
                                Toast.makeText(
                                        CrownReturnActivity.this,
                                        String.format(
                                                getResources().getString(
                                                        R.string.exceed),
                                                holder.productObj.getSIH()),
                                        Toast.LENGTH_SHORT).show();

                                /**
                                 * Delete the last entered number and reset the
                                 * qty
                                 **/
                                holder.etCrownCaseQty.removeTextChangedListener(this);
                                holder.etCrownCaseQty.setText(qty.length() > 1 ? qty
                                        .substring(0, qty.length() - 1) : "0");
                                holder.etCrownCaseQty.addTextChangedListener(this);
                            }
                        } else {
                            if (!qty.equals("")) {
                                holder.productObj.setCrownOrderedCaseQty(SDUtil
                                        .convertToInt(qty));
                            }

                            double tot = (holder.productObj
                                    .getCrownOrderedCaseQty() * holder.productObj
                                    .getCsrp())
                                    + (holder.productObj
                                    .getCrownOrderedPieceQty() * holder.productObj
                                    .getSrp())
                                    + (holder.productObj
                                    .getCrownOrderedOuterQty() * holder.productObj
                                    .getOsrp());
                            holder.total.setText(bmodel.formatValue(tot) + "");
                        }
                    }

                    public void beforeTextChanged(CharSequence s, int start,
                                                  int count, int after) {
                    }

                    public void onTextChanged(CharSequence s, int start,
                                              int before, int count) {

                    }
                });

                holder.etCrownOuterQty
                        .addTextChangedListener(new TextWatcher() {

                            @Override
                            public void afterTextChanged(Editable s) {

                            }

                            @Override
                            public void beforeTextChanged(CharSequence s,
                                                          int start, int count, int after) {

                            }

                            @Override
                            public void onTextChanged(CharSequence s,
                                                      int start, int before, int count) {
                                if (holder.productObj.getOutersize() == 0) {
                                    holder.etCrownOuterQty
                                            .removeTextChangedListener(this);
                                    holder.etCrownOuterQty.setText("0");
                                    holder.etCrownOuterQty
                                            .addTextChangedListener(this);
                                    return;
                                }
                                String qty = s.toString();
                                if (holder.productObj.isAllocation() == 1
                                        && bmodel.configurationMasterHelper.IS_SIH_VALIDATION) {

                                    float totalQty = (((holder.productObj
                                            .getCrownOrderedCaseQty()
                                            + holder.productObj.getOrderedCaseQty() + holder.productObj
                                            .getFreeCaseQty()) * holder.productObj
                                            .getCaseSize())
                                            + (holder.productObj.getOrderedPcsQty()
                                            + holder.productObj
                                            .getFreePieceQty() + holder.productObj.getCrownOrderedPieceQty()) + ((SDUtil
                                            .convertToInt(qty)
                                            + holder.productObj
                                            .getOrderedOuterQty() + holder.productObj
                                            .getFreeOuterQty()) * holder.productObj
                                            .getOutersize()));

                                    if (totalQty <= holder.productObj.getSIH()) {
                                        if (!qty.equals("")) {
                                            holder.productObj
                                                    .setCrownOrderedOuterQty(SDUtil
                                                            .convertToInt(qty));
                                        }

                                        double tot = (holder.productObj
                                                .getCrownOrderedCaseQty() * holder.productObj
                                                .getCsrp())
                                                + (holder.productObj
                                                .getCrownOrderedPieceQty() * holder.productObj
                                                .getSrp())
                                                + (holder.productObj
                                                .getCrownOrderedOuterQty() * holder.productObj
                                                .getOsrp());
                                        holder.total.setText(bmodel
                                                .formatValue(tot));
                                    } else {
                                        /** Show Toast **/
                                        Toast.makeText(
                                                CrownReturnActivity.this,
                                                String.format(
                                                        getResources()
                                                                .getString(
                                                                        R.string.exceed),
                                                        holder.productObj
                                                                .getSIH()),
                                                Toast.LENGTH_SHORT).show();

                                        /**
                                         * Delete the last entered number and
                                         * reset the
                                         *
                                         * qty
                                         **/
                                        holder.etCrownOuterQty.removeTextChangedListener(this);
                                        holder.etCrownOuterQty.setText(qty
                                                .length() > 1 ? qty.substring(
                                                0, qty.length() - 1) : "0");
                                        holder.etCrownOuterQty.addTextChangedListener(this);
                                    }
                                } else {
                                    if (!qty.equals("")) {
                                        holder.productObj
                                                .setCrownOrderedOuterQty(SDUtil
                                                        .convertToInt(qty));
                                    }

                                    double tot = (holder.productObj
                                            .getCrownOrderedCaseQty() * holder.productObj
                                            .getCsrp())
                                            + (holder.productObj
                                            .getCrownOrderedPieceQty() * holder.productObj
                                            .getSrp())
                                            + (holder.productObj
                                            .getCrownOrderedOuterQty() * holder.productObj
                                            .getOsrp());
                                    holder.total.setText(bmodel
                                            .formatValue(tot) + "");
                                }

                            }

                        });

                row.setOnClickListener(new OnClickListener() {
                    public void onClick(View v) {
                        if (bmodel.configurationMasterHelper.SHOW_SIH_IN_PNAME)
                            productName.setText("[SIH :"
                                    + holder.productObj.getSIH() + "] "
                                    + holder.pname);
                        else
                            productName.setText(holder.pname);
                        QUANTITY = holder.etCrownPcsQty;
                        QUANTITY.setTag(holder.productObj);
                        holder.etCrownPcsQty.selectAll();
                        holder.etCrownPcsQty.requestFocus();

                        if (viewFlipper.getDisplayedChild() != 0) {
                            viewFlipper.showPrevious();
                        }

                    }
                });

                row.setOnLongClickListener(new OnLongClickListener() {
                    public boolean onLongClick(View v) {
                        bmodel = (BusinessModel) getApplicationContext();
                        bmodel.setContext(CrownReturnActivity.this);

                        SchemeDetailsMasterHelper schemeHelper=SchemeDetailsMasterHelper.getInstance(getApplicationContext());
                        if (bmodel.configurationMasterHelper.IS_SCHEME_DIALOG) {
                            if (schemeHelper.getSchemeList() == null || schemeHelper.getSchemeList().size() == 0) {
                                Toast.makeText(CrownReturnActivity.this,
                                        R.string.scheme_not_available,
                                        Toast.LENGTH_SHORT).show();
                                return true;
                            }

                            bmodel.productHelper.setSchemes(schemeHelper.getSchemeList());
                            bmodel.productHelper.setPdname(holder.pname);
                            bmodel.productHelper.setProdId(holder.productId);
                            bmodel.productHelper.setProductObj(holder.productObj);
                            bmodel.productHelper.setFlag(1);
                            bmodel.productHelper.setTotalScreenSize(0);

                            Intent intent = new Intent(CrownReturnActivity.this, ProductSchemeDetailsActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            startActivity(intent);
                            overridePendingTransition(R.anim.trans_left_in, R.anim.trans_left_out);

                        } else {
                            bmodel.productHelper.setPdname(holder.pname);
                            bmodel.productHelper.setProdId(holder.productId);
                            bmodel.productHelper.setProductObj(holder.productObj);
                            bmodel.productHelper.setFlag(1);
                            bmodel.productHelper.setTotalScreenSize(0);
                            SchemeDialog sc = new SchemeDialog(
                                    CrownReturnActivity.this, schemeHelper.getSchemeList(),
                                    holder.pname, holder.productId,
                                    holder.productObj, 1, 0);

                            //sc.show();
                            FragmentManager fm = getSupportFragmentManager();
                            sc.show(fm, "");
                        }
                        return true;
                    }
                });

                holder.etFreeOuterQty.setOnTouchListener(new OnTouchListener() {

                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        if (bmodel.configurationMasterHelper.SHOW_SIH_IN_PNAME)
                            productName.setText("[SIH :"
                                    + holder.productObj.getSIH() + "] "
                                    + holder.pname);
                        else
                            productName.setText(holder.pname);
                        QUANTITY = holder.etFreeOuterQty;
                        QUANTITY.setTag(holder.productObj);
                        int inType = holder.etFreeOuterQty.getInputType();
                        holder.etFreeOuterQty.setInputType(InputType.TYPE_NULL);
                        holder.etFreeOuterQty.onTouchEvent(event);
                        holder.etFreeOuterQty.setInputType(inType);
                        holder.etFreeOuterQty.selectAll();
                        holder.etFreeOuterQty.requestFocus();
                        inputManager.hideSoftInputFromWindow(
                                mEdt_searchproductName.getWindowToken(), 0);
                        return true;
                    }
                });

                holder.etFreePcsQty.setOnTouchListener(new OnTouchListener() {
                    public boolean onTouch(View v, MotionEvent event) {
                        if (bmodel.configurationMasterHelper.SHOW_SIH_IN_PNAME)
                            productName.setText("[SIH :"
                                    + holder.productObj.getSIH() + "] "
                                    + holder.pname);
                        else
                            productName.setText(holder.pname);
                        QUANTITY = holder.etFreePcsQty;
                        QUANTITY.setTag(holder.productObj);
                        int inType = holder.etFreePcsQty.getInputType();
                        holder.etFreePcsQty.setInputType(InputType.TYPE_NULL);
                        holder.etFreePcsQty.onTouchEvent(event);
                        holder.etFreePcsQty.setInputType(inType);
                        holder.etFreePcsQty.selectAll();
                        holder.etFreePcsQty.requestFocus();
                        inputManager.hideSoftInputFromWindow(
                                mEdt_searchproductName.getWindowToken(), 0);
                        return true;
                    }
                });

                holder.etFreeCaseQty.setOnTouchListener(new OnTouchListener() {
                    public boolean onTouch(View v, MotionEvent event) {
                        if (bmodel.configurationMasterHelper.SHOW_SIH_IN_PNAME)
                            productName.setText("[SIH :"
                                    + holder.productObj.getSIH() + "] "
                                    + holder.pname);
                        else
                            productName.setText(holder.pname);
                        QUANTITY = holder.etFreeCaseQty;
                        QUANTITY.setTag(holder.productObj);
                        int inType = holder.etFreeCaseQty.getInputType();
                        holder.etFreeCaseQty.setInputType(InputType.TYPE_NULL);
                        holder.etFreeCaseQty.onTouchEvent(event);
                        holder.etFreeCaseQty.setInputType(inType);
                        holder.etFreeCaseQty.selectAll();
                        holder.etFreeCaseQty.requestFocus();
                        inputManager.hideSoftInputFromWindow(
                                mEdt_searchproductName.getWindowToken(), 0);
                        return true;
                    }
                });

                holder.etFreeOuterQty.addTextChangedListener(new TextWatcher() {

                    @Override
                    public void afterTextChanged(Editable s) {

                    }

                    @Override
                    public void beforeTextChanged(CharSequence s, int start,
                                                  int count, int after) {

                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start,
                                              int before, int count) {
                        if (holder.productObj.getOutersize() == 0) {
                            holder.etFreeOuterQty
                                    .removeTextChangedListener(this);
                            holder.etFreeOuterQty.setText("0");
                            holder.etFreeOuterQty.addTextChangedListener(this);
                            return;
                        }
                        String qty = s.toString();
                        if (holder.productObj.isAllocation() == 1
                                && bmodel.configurationMasterHelper.IS_SIH_VALIDATION) {

                            float totalQty = (((holder.productObj
                                    .getCrownOrderedCaseQty()
                                    + holder.productObj.getOrderedCaseQty() + holder.productObj
                                    .getFreeCaseQty()) * holder.productObj
                                    .getCaseSize())
                                    + (holder.productObj.getOrderedPcsQty()
                                    + holder.productObj
                                    .getFreePieceQty() + holder.productObj.getCrownOrderedPieceQty()) + ((SDUtil
                                    .convertToInt(qty)
                                    + holder.productObj
                                    .getOrderedOuterQty() + holder.productObj
                                    .getFreeOuterQty()) * holder.productObj
                                    .getOutersize()));

                            if (totalQty <= holder.productObj.getSIH()) {
                                if (!qty.equals("")) {
                                    holder.productObj.setFreeOuterQty(SDUtil
                                            .convertToInt(qty));
                                }

                                double tot = (holder.productObj
                                        .getFreeCaseQty() * holder.productObj
                                        .getCsrp())
                                        + (holder.productObj.getFreePieceQty() * holder.productObj
                                        .getSrp())
                                        + (holder.productObj.getFreeOuterQty() * holder.productObj
                                        .getOsrp());
                                holder.freeTotal.setText(bmodel
                                        .formatValue(tot));
                            } else {
                                /** Show Toast **/
                                Toast.makeText(
                                        CrownReturnActivity.this,
                                        String.format(
                                                getResources().getString(
                                                        R.string.exceed),
                                                holder.productObj.getSIH()),
                                        Toast.LENGTH_SHORT).show();

                                /**
                                 * Delete the last entered number and reset the
                                 *
                                 * qty
                                 **/
                                holder.etFreeOuterQty.removeTextChangedListener(this);
                                holder.etFreeOuterQty.setText(qty.length() > 1 ? qty
                                        .substring(0, qty.length() - 1) : "0");
                                holder.etFreeOuterQty.addTextChangedListener(this);
                            }
                        } else {
                            if (!qty.equals("")) {
                                holder.productObj.setFreeOuterQty(SDUtil
                                        .convertToInt(qty));
                            }

                            double tot = (holder.productObj.getFreeCaseQty() * holder.productObj
                                    .getCsrp())
                                    + (holder.productObj.getFreePieceQty() * holder.productObj
                                    .getSrp())
                                    + (holder.productObj.getFreeOuterQty() * holder.productObj
                                    .getOsrp());

                            holder.freeTotal.setText(bmodel.formatValue(tot)
                                    + "");
                        }

                    }

                });

                holder.etFreeCaseQty.addTextChangedListener(new TextWatcher() {

                    @Override
                    public void afterTextChanged(Editable s) {

                    }

                    @Override
                    public void beforeTextChanged(CharSequence s, int start,
                                                  int count, int after) {

                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start,
                                              int before, int count) {
                        if (holder.productObj.getCaseSize() == 0) {
                            holder.etFreeCaseQty
                                    .removeTextChangedListener(this);
                            holder.etFreeCaseQty.setText("0");
                            holder.etFreeCaseQty.addTextChangedListener(this);
                            return;
                        }
                        String qty = s.toString();


                        if (holder.productObj.isAllocation() == 1
                                && bmodel.configurationMasterHelper.IS_SIH_VALIDATION) {

                            float totalQty = (((SDUtil.convertToInt(qty)
                                    + holder.productObj.getOrderedCaseQty() + holder.productObj
                                    .getFreeCaseQty()) * holder.productObj
                                    .getCaseSize())
                                    + (holder.productObj.getOrderedPcsQty()
                                    + holder.productObj.getFreePieceQty() + holder.productObj
                                    .getCrownOrderedPieceQty()) + ((holder.productObj
                                    .getCrownOrderedOuterQty()
                                    + holder.productObj.getOrderedOuterQty() + holder.productObj
                                    .getFreeOuterQty()) * holder.productObj
                                    .getOutersize()));

                            if (totalQty <= holder.productObj.getSIH()) {
                                if (!qty.equals("")) {
                                    holder.productObj.setFreeCaseQty(SDUtil
                                            .convertToInt(qty));
                                }

                                double tot = (holder.productObj
                                        .getFreeCaseQty() * holder.productObj
                                        .getCsrp())
                                        + (holder.productObj.getFreePieceQty() * holder.productObj
                                        .getSrp())
                                        + (holder.productObj.getFreeOuterQty() * holder.productObj
                                        .getOsrp());
                                holder.freeTotal.setText(bmodel
                                        .formatValue(tot));
                            } else {
                                /** Show Toast **/
                                Toast.makeText(
                                        CrownReturnActivity.this,
                                        String.format(
                                                getResources().getString(
                                                        R.string.exceed),
                                                holder.productObj.getSIH()),
                                        Toast.LENGTH_SHORT).show();

                                /**
                                 * Delete the last entered number and reset the
                                 *
                                 * qty
                                 **/
                                holder.etFreeCaseQty.removeTextChangedListener(this);
                                holder.etFreeCaseQty.setText(qty.length() > 1 ? qty
                                        .substring(0, qty.length() - 1) : "0");
                                holder.etFreeCaseQty.addTextChangedListener(this);
                            }
                        } else {
                            if (!qty.equals("")) {
                                holder.productObj.setFreeCaseQty(SDUtil
                                        .convertToInt(qty));
                            }

                            double tot = (holder.productObj.getFreeCaseQty() * holder.productObj
                                    .getCsrp())
                                    + (holder.productObj.getFreePieceQty() * holder.productObj
                                    .getSrp())
                                    + (holder.productObj.getFreeOuterQty() * holder.productObj
                                    .getOsrp());

                            holder.freeTotal.setText(bmodel.formatValue(tot)
                                    + "");

                        }

                    }

                });

                holder.etFreePcsQty.addTextChangedListener(new TextWatcher() {

                    @Override
                    public void afterTextChanged(Editable s) {

                    }

                    @Override
                    public void beforeTextChanged(CharSequence s, int start,
                                                  int count, int after) {

                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start,
                                              int before, int count) {
                        if (holder.productObj.getPcUomid() == 0) {
                            holder.etFreePcsQty.removeTextChangedListener(this);
                            holder.etFreePcsQty.setText("0");
                            holder.etFreePcsQty.addTextChangedListener(this);
                            return;
                        }
                        String qty = s.toString();

                        if (holder.productObj.isAllocation() == 1
                                && bmodel.configurationMasterHelper.IS_SIH_VALIDATION) {

                            float totalQty = (((holder.productObj
                                    .getCrownOrderedCaseQty()
                                    + holder.productObj.getOrderedCaseQty() + holder.productObj
                                    .getFreeCaseQty()) * holder.productObj
                                    .getCaseSize())
                                    + (holder.productObj.getOrderedPcsQty()
                                    + holder.productObj
                                    .getFreePieceQty() + SDUtil
                                    .convertToInt(qty)) + ((holder.productObj
                                    .getCrownOrderedOuterQty()
                                    + holder.productObj.getOrderedOuterQty() + holder.productObj
                                    .getFreeOuterQty()) * holder.productObj
                                    .getOutersize()));

                            if (totalQty <= holder.productObj.getSIH()) {
                                if (!qty.equals("")) {
                                    holder.productObj.setFreePieceQty(SDUtil
                                            .convertToInt(qty));
                                }

                                double tot = (holder.productObj
                                        .getFreeCaseQty() * holder.productObj
                                        .getCsrp())
                                        + (holder.productObj.getFreePieceQty() * holder.productObj
                                        .getSrp())
                                        + (holder.productObj.getFreeOuterQty() * holder.productObj
                                        .getOsrp());
                                holder.freeTotal.setText(bmodel
                                        .formatValue(tot));
                            } else {
                                /** Show Toast **/
                                Toast.makeText(
                                        CrownReturnActivity.this,
                                        String.format(
                                                getResources().getString(
                                                        R.string.exceed),
                                                holder.productObj.getSIH()),
                                        Toast.LENGTH_SHORT).show();

                                /**
                                 * Delete the last entered number and reset the
                                 *
                                 * qty
                                 **/
                                holder.etFreePcsQty.removeTextChangedListener(this);
                                holder.etFreePcsQty.setText(qty.length() > 1 ? qty
                                        .substring(0, qty.length() - 1) : "0");
                                holder.etFreePcsQty.addTextChangedListener(this);
                            }
                        } else {
                            if (!qty.equals("")) {
                                holder.productObj.setFreePieceQty(SDUtil
                                        .convertToInt(qty));
                            }

                            double tot = (holder.productObj.getFreeCaseQty() * holder.productObj
                                    .getCsrp())
                                    + (holder.productObj.getFreePieceQty() * holder.productObj
                                    .getSrp())
                                    + (holder.productObj.getFreeOuterQty() * holder.productObj
                                    .getOsrp());

                            holder.freeTotal.setText(bmodel.formatValue(tot)
                                    + "");

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
                holder.psname.setTextColor(getResources().getColor(
                        R.color.blue_btn_txt));

            }
            holder.tvbarcode.setText(holder.productObj.getBarCode());
            holder.psname.setText(holder.productObj.getProductShortName());
            holder.pname = holder.productObj.getProductName();

            // Set user entry values
            holder.etCrownPcsQty.setText(holder.productObj
                    .getCrownOrderedPieceQty() + "");
            holder.etCrownCaseQty.setText(holder.productObj
                    .getCrownOrderedCaseQty() + "");
            holder.etCrownOuterQty.setText(holder.productObj
                    .getCrownOrderedOuterQty() + "");

            holder.etFreePcsQty.setText(holder.productObj.getFreePieceQty()
                    + "");
            holder.etFreeCaseQty.setText(holder.productObj.getFreeCaseQty()
                    + "");
            holder.etFreeOuterQty.setText(holder.productObj.getFreeOuterQty()
                    + "");

            if (holder.productObj.getOuUomid() == 0 || !holder.productObj.isOuterMapped()) {
                holder.etFreeOuterQty.setEnabled(false);
                holder.etCrownOuterQty.setEnabled(false);
            } else {
                holder.etFreeOuterQty.setEnabled(true);
                holder.etCrownOuterQty.setEnabled(true);
            }
            if (holder.productObj.getCaseUomId() == 0 || !holder.productObj.isCaseMapped()) {
                holder.etFreeCaseQty.setEnabled(false);
                holder.etCrownCaseQty.setEnabled(false);
            } else {
                holder.etFreeCaseQty.setEnabled(true);
                holder.etCrownCaseQty.setEnabled(true);
            }
            if (holder.productObj.getPcUomid() == 0 || !holder.productObj.isPieceMapped()) {
                holder.etFreePcsQty.setEnabled(false);
                holder.etCrownPcsQty.setEnabled(false);
            } else {
                holder.etFreePcsQty.setEnabled(true);
                holder.etCrownPcsQty.setEnabled(true);
            }
            TypedArray typearr = getTheme().obtainStyledAttributes(R.styleable.MyTextView);
            if (position % 2 == 0) {
                row.setBackgroundColor(typearr.getColor(R.styleable.MyTextView_listcolor_alt, 0));
            } else {
                row.setBackgroundColor(typearr.getColor(R.styleable.MyTextView_listcolor, 0));
            }

            return (row);
        }
    }

    class ViewHolder {
        private String productId, pname;
        private ProductMasterBO productObj;
        private TextView tvbarcode, psname;
        private EditText etCrownPcsQty, etCrownCaseQty, etCrownOuterQty,
                etFreePcsQty, etFreeCaseQty, etFreeOuterQty;
        private TextView total, freeTotal;
    }

    public String getFilterName(String filtername) {
        Vector<ConfigureBO> genfilter = bmodel.configurationMasterHelper
                .getGenFilter();
        for (int i = 0; i < genfilter.size(); i++) {
            if (genfilter.get(i).getConfigCode().equals(filtername))
                filtername = genfilter.get(i).getMenuName();
        }

        return filtername;
    }

    public String getDefaultFilter() {
        String defaultfilter = "";
        try {
            Vector<ConfigureBO> genfilter = bmodel.configurationMasterHelper
                    .getGenFilter();
            for (int i = 0; i < genfilter.size(); i++) {
                if (genfilter.get(i).getHasLink() == 1) {
                    defaultfilter = genfilter.get(i).getConfigCode();
                    break;
                }
            }

        } catch (Exception e) {
            Commons.printException(e);
        }
        return defaultfilter;
    }

    @Override
    public void updateGeneralText(String mFilterText) {
        generalbutton = mFilterText;
        updateBrandText(BRAND, -1);
    }

    @Override
    public void updateCancel() {
        // Close the drawer
        mDrawerLayout.closeDrawers();
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

    int SbdDistPre = 0; // Dist stock

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
            for (int i = 0; i < siz; i++) {
                ProductMasterBO ret = items.elementAt(i);
                double temp = 0;
                if (ret.getCrownOrderedPieceQty() != 0
                        || ret.getCrownOrderedCaseQty() != 0
                        || ret.getCrownOrderedOuterQty() != 0
                        || ret.getFreePieceQty() != 0
                        || ret.getFreeCaseQty() != 0
                        || ret.getFreeOuterQty() != 0) {
                    lpccount = lpccount + 1;
                    temp = ((ret.getCrownOrderedPieceQty() + ret
                            .getFreePieceQty()) * ret.getSrp())
                            + ((ret.getCrownOrderedCaseQty() + ret
                            .getFreeCaseQty()) * ret.getCsrp())
                            + (ret.getCrownOrderedOuterQty() + ret
                            .getFreeOuterQty()) * ret.getOsrp();

                    totalvalue = totalvalue + temp;
                }
            }
            float per;
            SbdDistPre = sbdStockAchieved.size();
            if (bmodel.configurationMasterHelper.HAS_STOCK_IN_DIST_POST) {
                per = (float) sbdStkAndOrderAchieved.size() / sbdTarget.size();
            } else {
                per = (float) sbdAcheived.size() / sbdTarget.size();
            }

            lpcText.setText(lpccount + "");
            Commons.print("numberpressed=" + totalvalue);
            totalValueText.setText(bmodel.formatValue(totalvalue) + "");

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

            if (!bmodel.configurationMasterHelper.SHOW_LPC_ORDER) {
                lpcText.setVisibility(View.GONE);
                findViewById(R.id.lpc_title).setVisibility(View.GONE);
            }

        } catch (Exception e) {
            Commons.printException(e);
        }
    }

    public void eff() {
        String s = QUANTITY.getText().toString();
        if (!s.equals("0") && !s.equals("0.0")) {
            QUANTITY.setText(QUANTITY.getText() + append);
        } else
            QUANTITY.setText(append);
    }

    public void numberPressed(View vw) {
        int val = 0;
        if (QUANTITY == null) {
            bmodel.showAlert(
                    getResources().getString(R.string.please_select_item), 0);
        } else {
            int id = vw.getId();
            if (id == R.id.calcdel) {
                int s = SDUtil.convertToInt(QUANTITY.getText()
                        .toString());
                s = s / 10;
                QUANTITY.setText(s + "");
                val = s;
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

    @Override
    public void loadStartVisit() {

    }

    public void onClick(View v) {
        Button vw = (Button) v;
        bmodel = (BusinessModel) getApplicationContext();
        bmodel.setContext(this);
        if (vw == mBtn_Search) {
            // viewFlipper.setInAnimation(this, R.anim.in_from_left);
            // viewFlipper.setOutAnimation(this, R.anim.out_to_right);
            viewFlipper.showNext();
        } else if (vw == mBtnFilterPopup) {
            AlertDialog.Builder builderSingle = new AlertDialog.Builder(
                    CrownReturnActivity.this);
            final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(
                    CrownReturnActivity.this,
                    android.R.layout.select_dialog_singlechoice,
                    mSearchTypeArray);
            builderSingle.setAdapter(arrayAdapter,
                    new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            // mSelectedFilter = arrayAdapter.getItem(which);
                            bmodel.setProductFilter(arrayAdapter.getItem(which));
                        }
                    });
            int selectedFiltPos = mSearchTypeArray.indexOf(bmodel
                    .getProductFilter());
            builderSingle.setSingleChoiceItems(arrayAdapter, selectedFiltPos,
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            // mSelectedFilter = arrayAdapter.getItem(which);
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
            brandbutton = BRAND;
            generalbutton = GENERAL;

            /** set the following value to clear the **/
            mSelectedFilterMap.put("General", "All");
            mSelectedFilterMap.put("Brand", "All");
            mSelectedFilterMap.put("Category", "All");
            try {
                InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
            } catch (Exception e) {
                Commons.printException(e);
            }
            supportInvalidateOptionsMenu();
            updateGeneralText(GENERAL);
        }
    }

    @Override
    public boolean onEditorAction(TextView arg0, int arg1, KeyEvent arg2) {
        if (arg1 == EditorInfo.IME_ACTION_DONE) {
            if (arg0.getText().length() > 0) {
                brandbutton = BRAND;
                generalbutton = GENERAL;
                supportInvalidateOptionsMenu();
            }
            loadSearchedList();
            return true;
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
        //	private ProgressDialog progressDialogue;
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
        /*	progressDialogue = ProgressDialog.show(CrownReturnActivity.this,
                    DataMembers.SD, getResources().getString(R.string.loading),
					true, false);*/
            builder = new AlertDialog.Builder(CrownReturnActivity.this);

            customProgressDialog(builder, getResources().getString(R.string.loading));
            alertDialog = builder.create();
            alertDialog.show();
        }

        protected void onProgressUpdate(Integer... progress) {

        }

        protected void onPostExecute(Boolean result) {
            // result is the value returned from doInBackground
            //	progressDialogue.dismiss();
            alertDialog.dismiss();
            nextBtnSubTask();
        }

    }

    public void loadSearchedList() {

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
            mSelectedFilter = bmodel.getProductFilter();
            for (int i = 0; i < siz; ++i) {
                ProductMasterBO ret = items.elementAt(i);
                if (mSelectedFilter.equals(getResources().getString(
                        R.string.order_dialog_barcode))) {
                    if (ret.getBarCode() != null && ret.getBarCode()
                            .toLowerCase()
                            .contains(
                                    mEdt_searchproductName.getText().toString()
                                            .toLowerCase()))
                        mylist.add(ret);

                } else if (mSelectedFilter.equals(getResources().getString(
                        R.string.order_gcas))) {
                    if (ret.getRField1() != null && ret.getRField1()
                            .toLowerCase()
                            .contains(
                                    mEdt_searchproductName.getText().toString()
                                            .toLowerCase()))
                        mylist.add(ret);

                } else if (mSelectedFilter.equals(getResources().getString(
                        R.string.product_name))) {
                    if (ret.getProductName() != null && ret.getProductName()
                            .toLowerCase()
                            .contains(
                                    mEdt_searchproductName.getText().toString()
                                            .toLowerCase()))
                        mylist.add(ret);
                }
            }

            mSchedule = new MyAdapter(mylist);
            lvwplist.setAdapter(mSchedule);
        } else {
            Toast.makeText(this,
                    getResources().getString(R.string.enter_three_char),
                    Toast.LENGTH_SHORT).show();
        }

    }

    @Override
    public void updateFromFiveLevelFilter(Vector<LevelBO> mParentIdList) {

    }

    @Override
    public void updateFromFiveLevelFilter(Vector<LevelBO> mParentIdList, HashMap<Integer, Integer> mSelectedIdByLevelId, ArrayList<Integer> mAttributeProducts, String mFilterText) {

    }

}
