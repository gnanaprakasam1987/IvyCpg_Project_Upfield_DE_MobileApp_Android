package com.ivy.countersales;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Paint;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.ivy.countersales.bo.CounterSaleBO;
import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.bo.ConfigureBO;
import com.ivy.sd.png.bo.LevelBO;
import com.ivy.sd.png.bo.ProductMasterBO;
import com.ivy.sd.png.commons.IvyBaseActivityNoActionBar;
import com.ivy.sd.png.commons.SDUtil;
import com.ivy.sd.png.model.BrandDialogInterface;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.provider.ConfigurationMasterHelper;
import com.ivy.sd.png.util.CommonDialog;
import com.ivy.sd.png.util.Commons;
import com.ivy.sd.png.view.CsAllSchemeDetailActivity;
import com.ivy.sd.png.view.FilterFiveFragment;
import com.ivy.sd.png.view.FilterFragment;
import com.ivy.sd.png.view.MustSellReasonDialog;
import com.ivy.sd.png.view.ProductSchemeDetailsActivity;
import com.ivy.sd.png.view.SchemeApply;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;

/**
 * Created by rajkumar.s on 18-03-2016.
 */
public class CSsale extends IvyBaseActivityNoActionBar implements BrandDialogInterface, View.OnClickListener, TextView.OnEditorActionListener, CSchildProductsDialog.ProductGroupInterface {
    BusinessModel bmodel;
    private DrawerLayout mDrawerLayout;
    ActionBarDrawerToggle mDrawerToggle;
    private static final String GENERAL = "General";
    private static final String BRAND = "Brand";
    boolean isProductFilter_enabled = true;
    ListView listView;
    private TextView productName;
    private EditText QUANTITY, mEdt_searchproductName;
    private InputMethodManager inputManager;
    private Button mBtn_Search, mBtnFilterPopup, mBtn_clear;
    private ViewFlipper viewFlipper;
    public Vector<ProductMasterBO> items;
    public ArrayList<ProductMasterBO> mylist;
    public String brandbutton, generalbutton;
    private String mSelectedFilter;
    public ProductMasterBO ret;
    private String strBarCodeSearch = "ALL";
    private ArrayList<String> mSearchTypeArray = new ArrayList<String>();
    private HashMap<String, String> mSelectedFilterMap = new HashMap<String, String>();
    private String append = "";
    TextView txt_total_lines, txt_total_values;
    private Toolbar toolbar;
    private Button saveBtn;
    private HashMap<Integer, Integer> mSelectedIdByLevelId;
    TextView tv_total;
    private MustSellReasonDialog dialog;
    private int mSelectedLocationIndex;
    private double finalValue;

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

    private int mTotalScreenWidth;

    int productType = 1;
    Button btnNavPrev, btnNavNext;
    TextView tv_productType;

    String[] productTypeArray = {"Normal Product", "Free Product", "Accessories"};
    private int mSelectedBrandID = 0;

    MyAdapter myAdapter;
    FrameLayout right_drawer_layout;

    private ArrayList<String> fiveFilter_productIDs;
    TextView pnametitle;
    boolean isData = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.cs_sale);
        bmodel = (BusinessModel) getApplicationContext();
        bmodel.setContext(this);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }
        setScreenTitle("" + bmodel.mSelectedActivityName);

        mDrawerLayout = (DrawerLayout) findViewById(
                R.id.drawer_layout);
        pnametitle = (TextView) findViewById(R.id.tv_prodname_title);
        listView = (ListView) findViewById(R.id.list);
        txt_total_lines = (TextView) findViewById(R.id.txt_total_lines);
        txt_total_values = (TextView) findViewById(R.id.txt_total_values);
        txt_total_values.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
        txt_total_lines.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
        ((TextView) findViewById(R.id.tv_total_lines)).setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
        ((TextView) findViewById(R.id.tv_total_values)).setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));

        // set a custom shadow that overlays the main content when the drawer
        // opens
        mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow,
                GravityCompat.START);
        mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow,
                GravityCompat.END);

        mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);

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
        updateGeneralText(GENERAL);
        mDrawerLayout.closeDrawer(GravityCompat.END);
        bmodel.mSelectedActivityName = "Sales";

        inputManager = (InputMethodManager) getSystemService(
                Context.INPUT_METHOD_SERVICE);
        viewFlipper = (ViewFlipper) findViewById(R.id.view_flipper);

        mEdt_searchproductName = (EditText) findViewById(
                R.id.edt_searchproductName);
        mBtn_Search = (Button) findViewById(R.id.btn_search);
        mBtnFilterPopup = (Button) findViewById(R.id.btn_filter_popup);
        mBtn_clear = (Button) findViewById(R.id.btn_clear);
        tv_total = (TextView) findViewById(R.id.total);

        mBtn_Search.setOnClickListener(this);
        mBtnFilterPopup.setOnClickListener(this);
        mBtn_clear.setOnClickListener(this);
        mEdt_searchproductName.setOnEditorActionListener(this);
        productName = (TextView) findViewById(R.id.productName);
        productName.setOnTouchListener(new View.OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                int inType = productName.getInputType();
                productName.setInputType(InputType.TYPE_NULL);
                productName.onTouchEvent(event);
                productName.setInputType(inType);
                return true;
            }
        });

        mSearchTypeArray = new ArrayList<String>();
        mSearchTypeArray.add(getResources()
                .getString(R.string.product_name));
        //  mSearchTypeArray.add("GCAS Code");
        mSearchTypeArray.add(getResources().getString(
                R.string.order_dialog_barcode));


        if (!bmodel.configurationMasterHelper.SHOW_ORDER_PCS) {
            findViewById(R.id.pcsTitle).setVisibility(View.GONE);
        } else {
            try {
                if (bmodel.labelsMasterHelper.applyLabels(findViewById(
                        R.id.pcsTitle).getTag()) != null)
                    ((TextView) findViewById(R.id.pcsTitle))
                            .setText(bmodel.labelsMasterHelper
                                    .applyLabels(findViewById(
                                            R.id.pcsTitle).getTag()));
            } catch (Exception e) {
                Commons.print("" + e);
            }
        }


        if (bmodel.configurationMasterHelper.IS_GROUP_PRODUCTS_IN_COUNTER_SALES) {
            findViewById(R.id.pcsTitle).setVisibility(View.GONE);
            findViewById(R.id.freePcsTitle).setVisibility(View.GONE);
            findViewById(R.id.free_sihTitle).setVisibility(View.GONE);
            tv_total.setVisibility(View.VISIBLE);
            try {
                if (bmodel.labelsMasterHelper.applyLabels(findViewById(
                        R.id.total).getTag()) != null)
                    ((TextView) findViewById(R.id.total))
                            .setText(bmodel.labelsMasterHelper
                                    .applyLabels(findViewById(
                                            R.id.total)
                                            .getTag()));
            } catch (Exception e) {
                Commons.print("" + e);
            }

        } else {
            findViewById(R.id.pcsTitle).setVisibility(View.VISIBLE);
            findViewById(R.id.freePcsTitle).setVisibility(View.VISIBLE);
            findViewById(R.id.free_sihTitle).setVisibility(View.VISIBLE);
            tv_total.setVisibility(View.GONE);
        }

        tv_productType = (TextView) findViewById(R.id.tv_product_type);
        tv_productType.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
        btnNavPrev = (Button) findViewById(R.id.btn_nav_prev);
        btnNavPrev.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                productType -= 1;

                if (productType <= 1)
                    btnNavPrev.setVisibility(View.INVISIBLE);
                else
                    btnNavNext.setVisibility(View.VISIBLE);

                tv_productType.setText(productTypeArray[productType - 1]);

                if (productType == 2) {
                    findViewById(R.id.free_sihTitle).setVisibility(View.VISIBLE);
                    findViewById(R.id.sihTitle).setVisibility(View.GONE);
                } else {
                    findViewById(R.id.free_sihTitle).setVisibility(View.GONE);
                    findViewById(R.id.sihTitle).setVisibility(View.VISIBLE);
                }

                loadSelectedProductType(brandbutton, mSelectedBrandID);

            }
        });
        btnNavNext = (Button) findViewById(R.id.btn_nav_next);
        btnNavNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                productType += 1;

                if (productType >= 3)
                    btnNavNext.setVisibility(View.INVISIBLE);
                else
                    btnNavPrev.setVisibility(View.VISIBLE);

                tv_productType.setText(productTypeArray[productType - 1]);


                if (productType == 2) {
                    findViewById(R.id.free_sihTitle).setVisibility(View.VISIBLE);
                    findViewById(R.id.sihTitle).setVisibility(View.GONE);
                } else {
                    findViewById(R.id.free_sihTitle).setVisibility(View.GONE);
                    findViewById(R.id.sihTitle).setVisibility(View.VISIBLE);
                }

                loadSelectedProductType(brandbutton, mSelectedBrandID);
            }
        });

        if (bmodel.configurationMasterHelper.SHOW_SPL_FILTER) {
            getMandatoryFilters();
        }
        searchText();

        saveBtn = (Button) findViewById(R.id.btn_save);
        saveBtn.setOnClickListener(this);
        saveBtn.setTypeface(bmodel.configurationMasterHelper.getFontBaloobhai(ConfigurationMasterHelper.FontType.REGULAR));

        DisplayMetrics dm = new DisplayMetrics();
        getWindow().getWindowManager().getDefaultDisplay().getMetrics(dm);
        mTotalScreenWidth = dm.widthPixels;


        right_drawer_layout = (FrameLayout) findViewById(R.id.right_drawer);

        // int width = getResources().getDisplayMetrics().widthPixels;
        DrawerLayout.LayoutParams params = (android.support.v4.widget.DrawerLayout.LayoutParams) right_drawer_layout.getLayoutParams();
        params.width = mTotalScreenWidth;
        right_drawer_layout.setLayoutParams(params);


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
    public void onClick(View view) {
        Button vw = (Button) view;
        /*bmodel = (BusinessModel)getApplicationContext();
        bmodel.setContext(this);*/
        if (vw == mBtn_Search) {
            viewFlipper.showNext();
        } else if (vw == mBtnFilterPopup) {
            AlertDialog.Builder builderSingle = new AlertDialog.Builder(
                    this);
            final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(
                    this, android.R.layout.select_dialog_singlechoice,
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
            /** set the following value to clear the **/
            mSelectedFilterMap.put("General", "All");
            mSelectedFilterMap.put("Brand", "All");
            mSelectedFilterMap.put("Category", "All");

            strBarCodeSearch = "ALL";

            supportInvalidateOptionsMenu();
            updateGeneralText(GENERAL);
        } else if (vw == saveBtn) {

            if (bmodel.configurationMasterHelper.IS_MUST_SELL
                    && !bmodel.productHelper.isCSMustSellFilled()) {

                new CommonDialog(getApplicationContext(), this,
                        "", getResources().getString(R.string.fill_must_sell),
                        false, getResources().getString(R.string.ok),
                        null, new CommonDialog.positiveOnClickListener() {
                    @Override
                    public void onPositiveButtonClick() {

                    }
                }, null).show();

                return;
            }

            //By clicking done button, it will considered as a new transaction. So draft will be deleted
            if (bmodel.getCounterSaleBO() != null && bmodel.getCounterSaleBO().isDraft()) {
                bmodel.mCounterSalesHelper.deleteCurrentDraft();
                bmodel.getCounterSaleBO().setDraft(false);
            }

            getEnterdSalableProducts();

            if (!isData) {
                bmodel.showAlert(getResources().getString(R.string.no_data_tosave), 0);
                //Toast.makeText(this, getResources().getString(R.string.no_data_tosave), Toast.LENGTH_LONG).show();
                return;
            }


            bmodel.schemeDetailsMasterHelper.isFromCounterSale = true;
            Intent intent = new Intent(CSsale.this,
                    SchemeApply.class);
            intent.putExtra("ScreenCode", "CSale");
            intent.putExtra("ForScheme", "CSale");
            intent.putExtra("refid", getIntent().getStringExtra("refid"));
            intent.putExtra("finalValue", finalValue);
            startActivity(intent);
            finish();


            //
        }
    }


   /* private void loadProducts(){
        lstProducts=new Vector<>();
        for(ProductMasterBO productMasterBO:bmodel.productHelper.getProductMaster()){
            lstProducts.add(productMasterBO);
        }

        MyAdapter adapter=new MyAdapter(lstProducts);
        listView.setAdapter(adapter);

    }*/

    private void searchText() {
        try {
            mEdt_searchproductName.addTextChangedListener(new TextWatcher() {
                public void afterTextChanged(Editable s) {
                    supportInvalidateOptionsMenu();
                    if (s.length() >= 3) {
                        loadSearchedList();
                    }
                }

                @Override
                public void beforeTextChanged(CharSequence s, int start,
                                              int count, int after) {
                    // TODO Auto-generated method stub

                }

                @Override
                public void onTextChanged(CharSequence s, int start,
                                          int before, int count) {
                    // TODO Auto-generated method stub

                }
            });
        } catch (Exception e) {
            Commons.print("" + e);
        }
    }

    @Override
    public boolean onEditorAction(TextView arg0, int arg1, KeyEvent event) {
        if (arg1 == EditorInfo.IME_ACTION_SEARCH) {
            loadSearchedList();
            return true;
        }
        return false;
    }

    public void loadSearchedList() {
        if (mEdt_searchproductName.getText().length() >= 3) {
            Vector<ProductMasterBO> items = bmodel.productHelper.getProductMaster();
            if (items == null) {
                bmodel.showAlert(
                        getResources().getString(R.string.no_products_exists),
                        0);
                return;
            }
            int siz = items.size();
            Commons.print("siz" + siz);
            mylist = new ArrayList<ProductMasterBO>();
            mSelectedFilter = bmodel.getProductFilter();
            for (int i = 0; i < siz; ++i) {
                ret = (ProductMasterBO) items.elementAt(i);

                if (!bmodel.configurationMasterHelper.IS_GROUP_PRODUCTS_IN_COUNTER_SALES
                        || (bmodel.configurationMasterHelper.IS_GROUP_PRODUCTS_IN_COUNTER_SALES && !ret.isChildProduct())) {


                    if (mSelectedFilter.equals(getResources().getString(
                            R.string.order_dialog_barcode))) {
                        if (ret.getBarCode() != null && ret.getBarCode()
                                .toLowerCase()
                                .contains(
                                        mEdt_searchproductName.getText().toString()
                                                .toLowerCase())) {
                            if (ret.getSIH() > 0)
                                if (generalbutton.equals(GENERAL) && brandbutton.equals(BRAND))//No filters selected
                                    mylist.add(ret);
                                else if (applyProductAndSpecialFilter(ret))
                                    mylist.add(ret);

                        }

                        Commons.print("siz Barcode : : : " + mEdt_searchproductName.getText().toString().toLowerCase());
                    } else if (mSelectedFilter.equals(getResources().getString(
                            R.string.order_gcas))) {
                        if (ret.getRField1() != null && ret.getRField1()
                                .toLowerCase()
                                .contains(
                                        mEdt_searchproductName.getText().toString()
                                                .toLowerCase())) {
                            if (ret.getSIH() > 0)
                                if (generalbutton.equals(GENERAL) && brandbutton.equals(BRAND))//No filters selected
                                    mylist.add(ret);
                                else if (applyProductAndSpecialFilter(ret))
                                    mylist.add(ret);
                        }
                        Commons.print("siz GCASCode : : : " + mEdt_searchproductName.getText().toString().toLowerCase());
                    } else if (mSelectedFilter.equals(getResources().getString(
                            R.string.product_name))) {
                        Commons.print("siz product_name : : : " + mEdt_searchproductName.getText().toString().toLowerCase());
                        if (ret.getProductShortName() != null && ret.getProductShortName()
                                .toLowerCase()
                                .contains(
                                        mEdt_searchproductName.getText().toString()
                                                .toLowerCase())) {
                            if (ret.getSIH() > 0)
                                if (generalbutton.equals(GENERAL) && brandbutton.equals(BRAND))//No filters selected
                                    mylist.add(ret);
                                else if (applyProductAndSpecialFilter(ret))
                                    mylist.add(ret);
                        }
                    }
                }
            }

            if (productType == 3) {//Accessories
                loadAccessoryProducts();
            } else {
                loadNormalProducts();
            }

            // final list prepared with all filters applied, now applying existing values
            loadExistingValues();


            refreshList();
            if (mylist.size() <= 0)
                Toast.makeText(this, getResources().getString(R.string.no_match_found), Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(this, "Enter atleast 3 letters.", Toast.LENGTH_SHORT)
                    .show();
        }
    }

    private boolean applyProductAndSpecialFilter(ProductMasterBO ret) {
        if (!GENERAL.equals(generalbutton) && !BRAND.equals(brandbutton)) {
            // both filter selected
            if (bmodel.configurationMasterHelper.IS_FIVE_LEVEL_FILTER) {
              /*  if (fiveFilter_productIDs != null && fiveFilter_productIDs.contains(ret.getProductID())
                        && isSpecialFilterAppliedProduct(generalbutton, ret))
                    return true;*/
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
              /*  if (fiveFilter_productIDs != null && fiveFilter_productIDs.contains(ret.getProductID()))
                    return true;*/
            } else {
                if (ret.getParentid() == mSelectedBrandID)
                    return true;
            }
        }
        return false;
    }

    public void refreshList() {
        myAdapter = new MyAdapter(mylist);
        listView.setAdapter(myAdapter);
        updateTotalLines();
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {


        if (!generalbutton.equals(GENERAL))
            menu.findItem(R.id.menu_spl_filter).setIcon(
                    R.drawable.ic_action_star_select);


        boolean drawerOpen = mDrawerLayout.isDrawerOpen(GravityCompat.END);
        menu.findItem(R.id.menu_next).setVisible(false);

        menu.findItem(R.id.menu_fivefilter).setVisible(!drawerOpen);
        menu.findItem(R.id.menu_product_filter).setVisible(!drawerOpen);
        menu.findItem(R.id.menu_scheme_view).setVisible(!drawerOpen);
        menu.findItem(R.id.menu_barcode).setVisible(!drawerOpen);

        if (bmodel.configurationMasterHelper.IS_FIVE_LEVEL_FILTER) {
            menu.findItem(R.id.menu_fivefilter).setVisible(true);
            menu.findItem(R.id.menu_product_filter).setVisible(false);
        } else {
            menu.findItem(R.id.menu_fivefilter).setVisible(false);
            menu.findItem(R.id.menu_product_filter).setVisible(true);
        }


//        if (bmodel.configurationMasterHelper.IS_FIVE_LEVEL_FILTER)
//            menu.findItem(R.id.menu_fivefilter).setVisible(false);
//        else
//            menu.findItem(R.id.menu_product_filter).setVisible(true);

        if (bmodel.configurationMasterHelper.SHOW_SPL_FILTER) {
            menu.findItem(R.id.menu_spl_filter).setVisible(true);
        } else {
            menu.findItem(R.id.menu_spl_filter).setVisible(false);
        }

        menu.findItem(R.id.menu_barcode).setVisible(bmodel.configurationMasterHelper.IS_BAR_CODE);

        menu.findItem(R.id.menu_scheme_view).setVisible(true);

        if (bmodel.configurationMasterHelper.IS_FIVE_LEVEL_FILTER && mSelectedIdByLevelId != null) {
            for (Integer id : mSelectedIdByLevelId.keySet()) {
                if (mSelectedIdByLevelId.get(id) > 0) {
                    menu.findItem(R.id.menu_fivefilter).setIcon(
                            R.drawable.ic_action_filter_select);
                    break;
                }
            }
        }


        if (drawerOpen)
            menu.clear();

        return super.onPrepareOptionsMenu(menu);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.menu_counter_sales, menu);
        return super.onCreateOptionsMenu(menu);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int i = item.getItemId();
        if (i == android.R.id.home) {
            if (mDrawerLayout.isDrawerOpen(GravityCompat.END))
                mDrawerLayout.closeDrawers();
            else {
               /* if (bmodel.getCounterSaleBO() != null) {
                    //just clearing sale list while going back..
                    bmodel.getCounterSaleBO().setmSalesproduct(new ArrayList<ProductMasterBO>());
                }*/
                Intent intent = new Intent(this, CustomerVisitActivity.class);
                startActivity(intent);
                finish();
            }
            return true;
        } else if (i == R.id.menu_next) {

            return true;
        } else if (i == R.id.menu_product_filter) {
            productFilterClickedFragment();
            supportInvalidateOptionsMenu();
            return true;
        } else if (i == R.id.menu_fivefilter) {
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
        } else if (i == R.id.menu_scheme_view) {

            Intent intent = new Intent(CSsale.this,
                    CsAllSchemeDetailActivity.class);
            startActivity(intent);

        }

        return super.onOptionsItemSelected(item);
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

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (result != null) {
            if (result.getContents() == null) {
                Toast.makeText(this, getResources().getString(R.string.no_match_found), Toast.LENGTH_LONG).show();
            } else {
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
            Toast.makeText(this, getResources().getString(R.string.no_match_found), Toast.LENGTH_LONG).show();
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void generalFilterClickedFragment() {
        try {
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
            if (bmodel.productHelper.getChildLevelBo().size() > 0)
                bundle.putString("filterHeader", bmodel.productHelper
                        .getChildLevelBo().get(0).getProductLevel());
            else
                bundle.putString("filterHeader", bmodel.productHelper
                        .getParentLevelBo().get(0).getPl_productLevel());
            // bundle.putSerializable("serilizeContent",bmodel.brandMasterHelper.getBrandMaster());
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
            Commons.print("" + e);
        }
    }

    @Override
    public void updateBrandText(String mFilterText, int bid) {

        try {
            mSelectedBrandID = bid;

            // Close the drawer
            mDrawerLayout.closeDrawers();

            // Change the Brand button Name
            brandbutton = mFilterText;

            // Consider generalbutton text if it is dependent filter.
            String generaltxt = generalbutton;

            // Clear the productName
            if (productName == null)
                productName = (TextView) findViewById(R.id.productName);
            productName.setText("");

            items = bmodel.productHelper.getProductMaster();
            if (items == null) {
                bmodel.showAlert(
                        getResources().getString(R.string.no_products_exists),
                        0);
                return;
            }
            int siz = items.size();
            Commons.print("siz" + siz);
            mylist = new ArrayList<ProductMasterBO>();
            mylist.clear();
            // Add the products into list
            for (int i = 0; i < siz; ++i) {
                ProductMasterBO ret = (ProductMasterBO) items.elementAt(i);
                /**
                 * After scanning product,Barcode value stored in
                 * strBarCodeSearch Variable
                 */
                if (!bmodel.configurationMasterHelper.IS_GROUP_PRODUCTS_IN_COUNTER_SALES
                        || (bmodel.configurationMasterHelper.IS_GROUP_PRODUCTS_IN_COUNTER_SALES && !ret.isChildProduct())) {

                    if (ret.getBarCode().equals(strBarCodeSearch)
                            || ret.getCasebarcode().equals(strBarCodeSearch)
                            || ret.getOuterbarcode().equals(strBarCodeSearch)
                            || strBarCodeSearch.equals("ALL")) {


                        if (bid == -1) {
                            if (mFilterText.equals("Brand")) {
                                if (ret.getSIH() > 0)
                                    if (GENERAL.equalsIgnoreCase(generaltxt) || (!GENERAL.equalsIgnoreCase(generaltxt) && isSpecialFilterAppliedProduct(generaltxt, ret))) {
                                        mylist.add(ret);
                                    }
                            }
                        } else if (bid == ret.getParentid()
                                ) {
                            if (ret.getSIH() > 0)
                                if (GENERAL.equalsIgnoreCase(generaltxt) || (!GENERAL.equalsIgnoreCase(generaltxt) && isSpecialFilterAppliedProduct(generaltxt, ret))) {
                                    mylist.add(ret);
                                }
                        }


                    }
                }


            }

            if (productType == 3) {//Accessories
                loadAccessoryProducts();
            } else {
                loadNormalProducts();
            }


            // final list prepared with all filters applied, now applying existing values
            loadExistingValues();

            updateProductCountInTitle(generaltxt, mFilterText);

            refreshList();

            finalValue = SDUtil.convertToDouble(txt_total_values.getText().toString());
        } catch (Exception e) {
            Commons.print("" + e);
        }
    }

    private void updateProductCountInTitle(String generaltxt, String filtertext) {
        if (GENERAL.equalsIgnoreCase(generaltxt) && BRAND.equals(filtertext)) {
            String strPname = getResources().getString(
                    R.string.product_name)
                    + " (" + mylist.size() + ")";
            pnametitle.setText(strPname);
        } else if (!GENERAL.equalsIgnoreCase(generaltxt)) {
            String strPname = getFilterName(generaltxt) + " ("
                    + mylist.size() + ")";
            pnametitle.setText(strPname);
        } else {
            String strPname = filtertext + " (" + mylist.size() + ")";
            pnametitle.setText(strPname);
        }

    }

    private void loadAccessoryProducts() {

        ArrayList<ProductMasterBO> lstAccessory = null;
        for (ProductMasterBO bo : mylist) {
            if (bo.isAccessory()) {
                if (lstAccessory == null)
                    lstAccessory = new ArrayList<>();

                lstAccessory.add(bo);

            }
        }

        mylist.clear();
        if (lstAccessory != null)
            mylist.addAll(lstAccessory);

        // listView.invalidateViews();
        //  refreshList();

    }

    private void loadNormalProducts() {


        ArrayList<ProductMasterBO> lstAccessory = null;
        for (ProductMasterBO bo : mylist) {
            if (!bo.isAccessory()) {
                if (lstAccessory == null)
                    lstAccessory = new ArrayList<>();

                lstAccessory.add(bo);

            }
        }

        mylist.clear();
        if (lstAccessory != null)
            mylist.addAll(lstAccessory);

        // listView.invalidateViews();
        // refreshList();

    }

    // call from navigation button
    private void loadSelectedProductType(String filtertext, int bid) {
        try {
            if (mEdt_searchproductName.getText().length() >= 3) {

                loadSearchedList();
            } else {

                mSelectedBrandID = bid;
                // Change the Brand button Name
                brandbutton = filtertext;
                // Consider generalbutton text if it is dependent filter.
                String generaltxt = generalbutton;

                items = bmodel.productHelper.getProductMaster();
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

                // Add the products into list
                for (int i = 0; i < siz; ++i) {
                    ProductMasterBO ret = (ProductMasterBO) items.elementAt(i);
                    /**
                     * After scanning product,Barcode value stored in
                     * strBarCodeSearch Variable
                     */
                    if (!bmodel.configurationMasterHelper.IS_GROUP_PRODUCTS_IN_COUNTER_SALES
                            || (bmodel.configurationMasterHelper.IS_GROUP_PRODUCTS_IN_COUNTER_SALES && !ret.isChildProduct())) {


                        if (ret.getBarCode().equals(strBarCodeSearch)
                                || ret.getCasebarcode().equals(strBarCodeSearch)
                                || ret.getOuterbarcode().equals(strBarCodeSearch)
                                || strBarCodeSearch.equals("ALL")) {


                            if (bid == -1) {
                                if (filtertext.equals("Brand")) {
                                    if (GENERAL.equalsIgnoreCase(generaltxt) || (!GENERAL.equalsIgnoreCase(generaltxt) && isSpecialFilterAppliedProduct(generaltxt, ret))) {
                                        mylist.add(ret);
                                    }
                                }
                            } else if (bid == ret.getParentid()
                                    ) {
                                if (GENERAL.equalsIgnoreCase(generaltxt) || (!GENERAL.equalsIgnoreCase(generaltxt) && isSpecialFilterAppliedProduct(generaltxt, ret))) {
                                    mylist.add(ret);
                                }
                            }


                        }
                    }


                }

                if (productType == 3) {//Accessories
                    loadAccessoryProducts();
                } else {
                    loadNormalProducts();
                }


                // final list prepared with all filters applied, now applying existing values
                loadExistingValues();


                updateProductCountInTitle(GENERAL, BRAND);

                refreshList();
            }

            //clearing other filters
            if (mSelectedIdByLevelId != null)
                mSelectedIdByLevelId.clear();
            generalbutton = GENERAL;
            mSelectedFilterMap.put("General", GENERAL);

        } catch (Exception ex) {

        }

    }

    private void loadExistingValues() {

        // set existing values
        if (bmodel.configurationMasterHelper.IS_GROUP_PRODUCTS_IN_COUNTER_SALES) {
            if (bmodel.getCounterSaleBO() != null && bmodel.getCounterSaleBO().getmSalesproduct() != null) {

                for (ProductMasterBO productMasterBO : mylist) {
                    productMasterBO.setCsPiece(0);
                    productMasterBO.setOrderedPcsQty(0);
                    productMasterBO.setCsTotal(0);
                    productMasterBO.setCsFreePiece(0);
                    productMasterBO.setCsFreeTotal(0);

                    for (ProductMasterBO bo : bmodel.getCounterSaleBO().getmSalesproduct()) {


                        if (bo.getParentid() == Integer.parseInt(productMasterBO.getProductID())) {


                            int total = bo.getCsPiece() + (bo.getCsCase() * bo.getCaseSize()) + (bo.getCsOuter() * bo.getOutersize());

                            // Parent csPiece object is reused for showing total qty
                            productMasterBO.setCsPiece(productMasterBO.getCsPiece() + total);
                            productMasterBO.setOrderedPcsQty(productMasterBO.getCsPiece());

                            productMasterBO.setCsTotal(productMasterBO.getCsTotal() + bo.getCsTotal());


                            int freetotal = bo.getCsFreePiece();

                            // Parent csFreePiece object is reused for showing free total qty
                            productMasterBO.setCsFreePiece(productMasterBO.getCsFreePiece() + freetotal);

                            productMasterBO.setCsFreeTotal(productMasterBO.getCsFreeTotal() + bo.getCsFreeTotal());


                        }
                    }


                }
                for (ProductMasterBO bo : bmodel.getCounterSaleBO().getmSalesproduct()) {
                    ProductMasterBO productMasterBO = bmodel.productHelper.getProductMasterBOById(bo.getProductID());
                    if (productMasterBO != null) {
                        productMasterBO.setCsPiece(bo.getCsPiece());
                        productMasterBO.setOrderedPcsQty(bo.getCsPiece());
                        productMasterBO.setCsCase(bo.getCsCase());
                        productMasterBO.setCsOuter(bo.getCsOuter());
                        productMasterBO.setCsFreePiece(bo.getCsFreePiece());
                        productMasterBO.setCsTotal(bo.getCsTotal());

                    }
                }
            }
        } else {
            if (bmodel.getCounterSaleBO() != null && bmodel.getCounterSaleBO().getmSalesproduct() != null) {
                for (ProductMasterBO bo : bmodel.getCounterSaleBO().getmSalesproduct()) {
                    for (ProductMasterBO productMasterBO : mylist) {
                        if (bo.getProductID().equals(productMasterBO.getProductID())) {
                            productMasterBO.setCsPiece(bo.getCsPiece());
                            productMasterBO.setOrderedPcsQty(bo.getCsPiece());
                            productMasterBO.setCsCase(bo.getCsCase());
                            productMasterBO.setCsOuter(bo.getCsOuter());
                        }
                    }
                }
            }
        }
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

    private boolean applyCommonFilterConfig(ProductMasterBO ret) {
        return (isSbd && ret.isRPS()) || (isSbdGaps && ret.isRPS() && !ret.isSBDAcheived()) || (isOrdered && (ret.getOrderedPcsQty() > 0 || ret.getOrderedCaseQty() > 0 || ret.getOrderedOuterQty() > 0))
                || (isPurchased && ret.getIsPurchased() == 1) || (isInitiative && ret.getIsInitiativeProduct() == 1) || (isOnAllocation && ret.isAllocation() == 1
                && bmodel.configurationMasterHelper.IS_SIH_VALIDATION) || (isInStock && ret.getWSIH() > 0) || (isPromo && ret.isPromo()) || (isMustSell && ret.getIsMustSell() == 1)
                || (isFocusBrand && ret.getIsFocusBrand() == 1) || (isFocusBrand2 && ret.getIsFocusBrand2() == 1) || (isSIH && ret.getSIH() > 0) || (isOOS && ret.getOos() == 0)
                || (isNMustSell && ret.getIsNMustSell() == 1) || (isStock && (ret.getLocations().get(mSelectedLocationIndex).getShelfPiece() > -1
                || ret.getLocations().get(mSelectedLocationIndex).getShelfCase() > -1 || ret.getLocations().get(mSelectedLocationIndex).getShelfOuter() > -1 || ret.getLocations().get(mSelectedLocationIndex).getWHPiece() > 0
                || ret.getLocations().get(mSelectedLocationIndex).getWHCase() > 0 || ret.getLocations().get(mSelectedLocationIndex).getWHOuter() > 0)) || (isDiscount && ret.getIsDiscountable() == 1);
    }

    @Override
    public void updateGeneralText(String mFilterText) {
        generalbutton = mFilterText;

        //clearing other filters if special filter selected
        if (mSelectedIdByLevelId != null)
            mSelectedIdByLevelId.clear();

        if (mEdt_searchproductName != null)
            mEdt_searchproductName.setText("");

        updateBrandText(BRAND, -1);
    }

    @Override
    public void updateCancel() {
        mDrawerLayout.closeDrawers();
    }

    @Override
    public void updateFromFiveLevelFilter(Vector<LevelBO> mParentIdList) {

    }

    @Override
    public void updateFromFiveLevelFilter(Vector<LevelBO> mParentIdList, HashMap<Integer, Integer> mSelectedIdByLevelId, ArrayList<Integer> mAttributeProducts, String mFilterText) {

        String filtertext = getResources().getString(R.string.product_name);
        if (!mFilterText.equals(""))
            filtertext = mFilterText;

        // brandbutton = filtertext;
        fiveFilter_productIDs = new ArrayList<>();

        int count = 0;
        mylist = new ArrayList<>();
        Vector<ProductMasterBO> items = bmodel.productHelper.getProductMaster();
        if (mAttributeProducts != null) {
            count = 0;
            if (!mParentIdList.isEmpty()) {
                for (LevelBO levelBO : mParentIdList) {
                    count++;
                    for (ProductMasterBO productBO : items) {

                        if (levelBO.getProductID() == productBO.getParentid()) {
                            // here we get all products mapped to parent id list, then that product will be added only if it is mapped to selected attribute
                            if (mAttributeProducts.contains(Integer.parseInt(productBO.getProductID()))) {
                                if (productBO.getSIH() > 0) {
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

                        if (pid == Integer.parseInt(productBO.getProductID())) {
                            if (bmodel.configurationMasterHelper.IS_LOAD_PRICE_GROUP_PRD_OLY && productBO.getGroupid() == 0)
                                continue;
                            if (productBO.getSIH() > 0) {
                                mylist.add(productBO);
                                fiveFilter_productIDs.add(productBO.getProductID());
                            }
                        }

                    }
                }
            }
        } else {
            for (LevelBO levelBO : mParentIdList) {
                count++;
                for (ProductMasterBO productBO : items) {

                    if (levelBO.getProductID() == productBO.getParentid()) {
                        //  filtertext = levelBO.getLevelName();
                        if (productBO.getSIH() > 0) {
                            mylist.add(productBO);
                            fiveFilter_productIDs.add(productBO.getProductID());
                        }
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


        if (productType == 3) {//Accessories
            loadAccessoryProducts();
        } else {
            loadNormalProducts();
        }


        myAdapter = new MyAdapter(mylist);

        listView.setAdapter(myAdapter);
        strBarCodeSearch = "ALL";
        //  updateValue();
        mDrawerLayout.closeDrawers();
        this.mSelectedIdByLevelId = mSelectedIdByLevelId;

        if (!bmodel.configurationMasterHelper.SHOW_SPL_FILTER) {
            if (count == 1) {
                String strPname = filtertext + " (" + mylist.size() + ")";
                pnametitle.setText(strPname);
            } else {
                String strPname = getResources().getString(R.string.product_name) + " (" + mylist.size() + ")";
                pnametitle.setText(strPname);
            }
        } else {

            updateProductCountInTitle(GENERAL, BRAND);
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

    private String getFilterName(String filtername) {
        Vector<ConfigureBO> genfilter = bmodel.configurationMasterHelper
                .getGenFilter();
        for (int i = 0; i < genfilter.size(); i++) {
            if (genfilter.get(i).getConfigCode().equals(filtername))
                filtername = genfilter.get(i).getMenuName();
        }
        return filtername;
    }

    @Override
    public void updateMultiSelectionBrand(List<String> mFilterName, List<Integer> mFilterId) {

    }

    @Override
    public void updateMultiSelectionCategory(List<Integer> mCategory) {

    }

    @Override
    public void loadStartVisit() {

    }

    class MyAdapter extends ArrayAdapter<ProductMasterBO> {
        private ArrayList<ProductMasterBO> items;

        public MyAdapter(Vector<ProductMasterBO> items) {
            super(CSsale.this, R.layout.row_cs_sales, items);
        }

        public MyAdapter(ArrayList<ProductMasterBO> items) {
            super(CSsale.this, R.layout.row_cs_sales, items);
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
                final ProductMasterBO counterBo = items.get(position);

                if (row == null) {
                    LayoutInflater inflater = getLayoutInflater();
                    row = inflater.inflate(
                            R.layout.row_cs_sales, parent,
                            false);
                    holder = new ViewHolder();

                    holder.psname = (TextView) row
                            .findViewById(R.id.stock_and_order_listview_productname);
                    holder.psname.setMaxLines(bmodel.configurationMasterHelper.MAX_NO_OF_PRODUCT_LINES);

                    holder.pcsQty = (EditText) row
                            .findViewById(R.id.stock_and_order_listview_pcs_qty);

                   /* holder.txt_price = (TextView) row
                            .findViewById(R.id.txt_price);*/
                    holder.txt_total = (TextView) row
                            .findViewById(R.id.txt_total);
                    holder.tv_barcode = (TextView) row
                            .findViewById(R.id.tv_barcode);
                    holder.tv_sih = (TextView) row
                            .findViewById(R.id.txt_sih);
                    holder.tv_free_sih = (TextView) row
                            .findViewById(R.id.txt_free_sih);
                    holder.freePcs = (EditText) row
                            .findViewById(R.id.tv_free_pcs);
                    holder.tv_mrp = (TextView) row
                            .findViewById(R.id.txt_mrp);
                    holder.txt_total_val = (TextView) row
                            .findViewById(R.id.txt_total_val);
                    holder.txt_total.setPaintFlags(holder.txt_total.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);


                    holder.psname.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
                    holder.pcsQty.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
                    holder.tv_barcode.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
                    holder.tv_sih.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
                    holder.freePcs.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
                    holder.tv_mrp.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
                    holder.txt_total.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
                    holder.tv_free_sih.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
                    holder.txt_total_val.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));


                    if (!bmodel.configurationMasterHelper.SHOW_ORDER_PCS)
                        holder.pcsQty.setVisibility(View.GONE);


                    if (bmodel.configurationMasterHelper.IS_GROUP_PRODUCTS_IN_COUNTER_SALES) {
                        holder.pcsQty.setVisibility(View.GONE);

                        holder.freePcs.setVisibility(View.GONE);
                        holder.txt_total.setVisibility(View.VISIBLE);

                        holder.tv_mrp.setVisibility(View.VISIBLE);


                    } else {
                        holder.txt_total.setVisibility(View.GONE);
                        holder.pcsQty.setVisibility(View.VISIBLE);
                        holder.freePcs.setVisibility(View.VISIBLE);

                    }

                    if (productType == 2) {
                        holder.tv_free_sih.setVisibility(View.VISIBLE);
                        holder.tv_sih.setVisibility(View.GONE);
                    } else {
                        holder.tv_free_sih.setVisibility(View.GONE);
                        holder.tv_sih.setVisibility(View.VISIBLE);
                    }

                    row.setOnLongClickListener(new View.OnLongClickListener() {
                        @Override
                        public boolean onLongClick(View view) {
                            if (!bmodel.configurationMasterHelper.IS_GROUP_PRODUCTS_IN_COUNTER_SALES)
                                if (bmodel.configurationMasterHelper.IS_SCHEME_DIALOG || bmodel.configurationMasterHelper.IS_PRODUCT_SCHEME_DIALOG) {
                                    if (bmodel.schemeDetailsMasterHelper
                                            .getmSchemeList() == null
                                            || bmodel.schemeDetailsMasterHelper
                                            .getmSchemeList().size() == 0) {
                                        Toast.makeText(CSsale.this,
                                                R.string.scheme_not_available,
                                                Toast.LENGTH_SHORT).show();
                                        return true;
                                    }
                                    bmodel.productHelper.setSchemes(bmodel.schemeDetailsMasterHelper.getmSchemeList());
                                    bmodel.productHelper.setPdname(counterBo.getProductName());
                                    bmodel.productHelper.setProdId(counterBo.getProductID());
                                    bmodel.productHelper.setProductObj(counterBo);
                                    bmodel.productHelper.setFlag(1);
                                    bmodel.productHelper.setTotalScreenSize(mTotalScreenWidth);

                                    Intent intent = new Intent(CSsale.this, ProductSchemeDetailsActivity.class);
                                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                                    startActivity(intent);

                                }


                            return true;
                        }
                    });

                    holder.txt_total.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {

                            CSchildProductsDialog dialog = new CSchildProductsDialog(CSsale.this, holder.counterSaleBO.getProductID(), holder.counterSaleBO.getProductName(), productType);
                            dialog.show();
                            dialog.setCancelable(false);
                        }
                    });
                    holder.pcsQty
                            .setOnTouchListener(new View.OnTouchListener() {
                                public boolean onTouch(View v, MotionEvent event) {
                                    productName.setText(holder.pname);
                                    QUANTITY = holder.pcsQty;
                                    QUANTITY.setTag(holder.counterSaleBO);
                                    int inType = holder.pcsQty
                                            .getInputType();
                                    holder.pcsQty
                                            .setInputType(InputType.TYPE_NULL);
                                    holder.pcsQty.onTouchEvent(event);
                                    holder.pcsQty.setInputType(inType);
                                    holder.pcsQty.selectAll();
                                    holder.pcsQty.requestFocus();
                                    inputManager.hideSoftInputFromWindow(
                                            mEdt_searchproductName
                                                    .getWindowToken(), 0);
                                    return true;
                                }
                            });


                    holder.pcsQty
                            .addTextChangedListener(new TextWatcher() {

                                public void afterTextChanged(Editable s) {
                                    String qty = s.toString();
                                    if (!qty.equals("")) {
                                        int pc_qty = SDUtil
                                                .convertToInt(qty);

                                        if (productType == 2)
                                            holder.counterSaleBO.setCsFreePiece(pc_qty);
                                        else {
                                            holder.counterSaleBO.setCsPiece(pc_qty);
                                            holder.counterSaleBO.setOrderedPcsQty(pc_qty);
                                        }
                                        updateTotalLines();

                                    }
                                }

                                @Override
                                public void beforeTextChanged(CharSequence s,
                                                              int start, int count, int after) {
                                    // TODO Auto-generated method stub
                                }

                                @Override
                                public void onTextChanged(CharSequence s,
                                                          int start, int before, int count) {
                                    // TODO Auto-generated method stub
                                }
                            });


                    row.setOnClickListener(new View.OnClickListener() {
                        public void onClick(View v) {

                            productName.setText(holder.pname);
                            inputManager.hideSoftInputFromWindow(
                                    mEdt_searchproductName.getWindowToken(), 0);

                            if (viewFlipper.getDisplayedChild() != 0) {
                                viewFlipper.showPrevious();
                            }

                        }
                    });

                    row.setTag(holder);
                } else {
                    holder = (ViewHolder) row.getTag();
                }

                holder.counterSaleBO = counterBo;

                holder.pname = holder.counterSaleBO.getProductName();
                holder.psname.setText(holder.counterSaleBO.getProductShortName());
                holder.tv_barcode.setText(holder.counterSaleBO.getBarCode());

                try {
                    if (holder.counterSaleBO.getTextColor() != 0)
                        holder.psname.setTextColor(holder.counterSaleBO.getTextColor());
                    else
                        holder.psname.setTextColor(ContextCompat.getColor(CSsale.this,
                                android.R.color.black));
                } catch (Exception e) {
                    Commons.printException(e);
                    holder.psname.setTextColor(ContextCompat.getColor(CSsale.this,
                            android.R.color.black));

                }

                if (bmodel.configurationMasterHelper.IS_GROUP_PRODUCTS_IN_COUNTER_SALES) {
                    // Parent csPiece object is reused for showing total qty
                    if (productType == 2) {
                        holder.txt_total.setText(holder.counterSaleBO.getCsFreePiece() + "");

                        holder.txt_total_val.setText(SDUtil.format(holder.counterSaleBO.getCsFreeTotal(), 2, 0) + "");
                    } else {

                        holder.txt_total.setText(holder.counterSaleBO.getCsPiece() + "");

                        holder.txt_total_val.setText(SDUtil.format(holder.counterSaleBO.getCsTotal(), 2, 0) + "");
                    }
                } else {

                    holder.pcsQty.setText(holder.counterSaleBO.getCsPiece() + "");
                   /* holder.caseQty.setText(holder.counterSaleBO.getCsCase() + "");
                    holder.outerQty.setText(holder.counterSaleBO.getCsOuter() + "");*/
                }
                holder.tv_sih.setText(holder.counterSaleBO.getSIH() + "");
                holder.tv_free_sih.setText(holder.counterSaleBO.getCsFreeSIH() + "");
                holder.tv_mrp.setText(SDUtil.format(holder.counterSaleBO.getMRP(), 2, 0) + "");
                holder.tv_mrp.setVisibility(View.GONE);

            } catch (Exception e) {
                Commons.print("" + e);
            }
            return (row);
        }
    }


    public class ViewHolder {
        private String productId, pname;
        private ProductMasterBO counterSaleBO;
        TextView psname, txt_total, tv_barcode, tv_sih, tv_mrp, tv_free_sih, txt_total_val;
        private EditText pcsQty, freePcs;

    }

    private String getProductTotalValue(ProductMasterBO product) {
        double totalQty = 0;
        if (product.getCsPiece() > 0 || product.getCsCase() > 0 || product.getCsOuter() > 0) {
            totalQty = (product.getCsPiece() * product.getSrp()) + (product.getCsCase() * product.getCsrp()) + (product.getCsOuter() * product.getOsrp());
        }
        return bmodel.formatValue(totalQty);
    }

    public void eff() {
        String s = (String) QUANTITY.getText().toString();
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
                int s = SDUtil.convertToInt((String) QUANTITY.getText()
                        .toString());
                s = s / 10;
                QUANTITY.setText(s + "");
                val = s;
            } else {
                Button ed = (Button) findViewById(vw.getId());
                append = ed.getText().toString();
                eff();
                val = SDUtil.convertToInt((String) append);
            }

        }
    }

    private void updateTotalLines() {
        int count = 0;
        double totalValues = 0;

        for (ProductMasterBO bo : bmodel.productHelper.getProductMaster()) {

            if (!bmodel.configurationMasterHelper.IS_GROUP_PRODUCTS_IN_COUNTER_SALES
                    || (bmodel.configurationMasterHelper.IS_GROUP_PRODUCTS_IN_COUNTER_SALES && bo.isChildProduct())) {

                if (bo.getCsPiece() > 0 || bo.getCsCase() > 0 || bo.getCsOuter() > 0) {
                    count += 1;

                    totalValues += bo.getCsTotal();
                }
            }
        }
        txt_total_lines.setText(count + "");
        txt_total_values.setText(bmodel.formatValue(totalValues));
    }

    private void getEnterdSalableProducts() {
        ArrayList<ProductMasterBO> lst = new ArrayList<>();
        for (ProductMasterBO bo : bmodel.productHelper.getProductMaster()) {

            if (!bmodel.configurationMasterHelper.IS_GROUP_PRODUCTS_IN_COUNTER_SALES
                    || (bmodel.configurationMasterHelper.IS_GROUP_PRODUCTS_IN_COUNTER_SALES && bo.isChildProduct())) {

                if (bo.getCsPiece() > 0 || bo.getCsFreePiece() > 0) {
                    lst.add(new ProductMasterBO(bo));


                    isData = true;
                }
            }
        }

        if (isData) {
            if (bmodel.getCounterSaleBO() != null)
                bmodel.getCounterSaleBO().setmSalesproduct(lst);
            else {
                CounterSaleBO csBo = new CounterSaleBO();
                csBo.setmSalesproduct(lst);
                bmodel.setCounterSaleBO(csBo);
            }
        } else {
            if (bmodel.getCounterSaleBO() != null)
                bmodel.getCounterSaleBO().setmSalesproduct(null);
        }

    }

    @Override
    public void onDismiss() {
        listView.invalidateViews();
        updateTotalLines();
    }
}
