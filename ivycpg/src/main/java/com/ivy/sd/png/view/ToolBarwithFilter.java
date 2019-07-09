package com.ivy.sd.png.view;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.widget.Toolbar;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ViewFlipper;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.bo.ConfigureBO;
import com.ivy.sd.png.bo.ProductMasterBO;
import com.ivy.sd.png.bo.StandardListBO;
import com.ivy.sd.png.commons.IvyBaseActivityNoActionBar;
import com.ivy.sd.png.commons.SDUtil;
import com.ivy.sd.png.model.BrandDialogInterface;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.model.FiveLevelFilterCallBack;
import com.ivy.sd.png.provider.ConfigurationMasterHelper;
import com.ivy.sd.png.util.Commons;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Vector;


public class ToolBarwithFilter extends IvyBaseActivityNoActionBar implements
        View.OnClickListener, BrandDialogInterface, TextView.OnEditorActionListener, FiveLevelFilterCallBack {

    public static final String BRAND = "Brand";
    public static final String GENERAL = "General";


    protected BusinessModel bmodel;

    public ListView lvwplist;
    public ExpandableListView expandlvwplist;
    public Button mBtn_Search, mBtnFilterPopup, mBtn_clear, mBtn_next;
    public TextView totalValueText, lpcText, productName;
    public EditText QUANTITY, mEdt_searchproductName, QUANTITY1;
    public String brandbutton, generalbutton;
    public Toolbar toolbar;
    public RelativeLayout footerLty;

    public DrawerLayout mDrawerLayout;
    public ViewFlipper viewFlipper;

    public ActionBarDrawerToggle mDrawerToggle;
    public boolean isSpecialFilter_enabled = true;
    boolean isProductFilter_enabled = true;
    boolean next_button_enabled = true;
    boolean remarks_button_enable = true;
    boolean scheme_button_enable = true;
    boolean location_button_enable = true;
    boolean expand_collapse_button_enable = false;
    public boolean so_apply = false, std_apply = false, sih_apply = false;

    public String append = "";
    Vector<String> mgeneralFilterList;
    public ArrayList<String> mSearchTypeArray;
    public ArrayList<ProductMasterBO> mylist;
    public String mSelectedFilter;
    public InputMethodManager inputManager;
    public ProductMasterBO ret;
    public Vector<ProductMasterBO> items;


    public HashMap<String, String> mSelectedFilterMap = new HashMap<>();

    public final String mCommon = "Filt01";
    public final String mSbd = "Filt02";
    public final String mSbdGaps = "Filt03";
    public final String mOrdered = "Filt04";
    public final String mPurchased = "Filt05";
    public final String mInitiative = "Filt06";
    public final String mOnAllocation = "Filt07";
    public final String mInStock = "Filt08";
    public final String mPromo = "Filt09";
    public final String mMustSell = "Filt10";
    public final String mFocusBrand = "Filt11";
    public final String mFocusBrand2 = "Filt12";
    public final String msih = "Filt13";
    public final String mOOS = "Filt14";
    public final String mDiscount = "Filt18";
    public String strBarCodeSearch = "ALL";
    private final String mFocusBrand3 = "Filt20";
    private final String mFocusBrand4 = "Filt21";
    private final String mSMP = "Filt22";
    public final String mNMustSell = "Filt16";
    public final String mStock = "Filt17";

    public boolean isSbd, isSbdGaps, isOrdered, isPurchased, isInitiative, isOnAllocation, isInStock, isPromo, isMustSell, isFocusBrand, isFocusBrand2, isSIH, isOOS, isNMustSell, isStock, isDiscount, isFocusBrand3, isFocusBrand4, isSMP;

    public ArrayAdapter<StandardListBO> mLocationAdapter;
    public int mSelectedLocationIndex;
    public HashMap<Integer, Integer> mSelectedIdByLevelId;
    ArrayList<String> fiveFilter_productIDs;
    int mSelectedBrandID = 0;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tool_barwith_filter);

        bmodel = (BusinessModel) getApplicationContext();
        bmodel.setContext(this);

        FrameLayout drawer = (FrameLayout) findViewById(R.id.right_drawer);

        int width = getResources().getDisplayMetrics().widthPixels;
        DrawerLayout.LayoutParams params = (DrawerLayout.LayoutParams) drawer.getLayoutParams();
        params.width = width;
        drawer.setLayoutParams(params);
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        // set a custom shadow that overlays the main content when the drawer
        // opens
        mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow,
                GravityCompat.START);
        mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow,
                GravityCompat.END);

        inputManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

        viewFlipper = (ViewFlipper) findViewById(R.id.view_flipper);

        mEdt_searchproductName = (EditText) findViewById(R.id.edt_searchproductName);
        mBtn_Search = (Button) findViewById(R.id.btn_search);
        mBtnFilterPopup = (Button) findViewById(R.id.btn_filter_popup);
        mBtn_clear = (Button) findViewById(R.id.btn_clear);
        mBtn_next = (Button) findViewById(R.id.btn_next);
        footerLty = (RelativeLayout) findViewById(R.id.footer1);

        mBtn_Search.setOnClickListener(this);
        mBtnFilterPopup.setOnClickListener(this);
        mBtn_clear.setOnClickListener(this);
        mBtn_clear.setOnEditorActionListener(this);
        mEdt_searchproductName.setOnEditorActionListener(this);

        mBtn_next.setTypeface(bmodel.configurationMasterHelper.getFontBaloobhai(ConfigurationMasterHelper.FontType.REGULAR));

        getOverflowMenu();
        toolbar = (Toolbar) findViewById(R.id.toolbar);

        if (toolbar != null) {

            setSupportActionBar(toolbar);
            getSupportActionBar().setIcon(null);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            // Used to on / off the back arrow icon
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            // Used to remove the app logo actionbar icon and set title as home
            // (title support click)
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        // ActionBarDrawerToggle ties together the the proper interactions
        // between the sliding drawer and the action bar app icon
        mDrawerToggle = new ActionBarDrawerToggle(this, /* host Activity */
                mDrawerLayout, /* DrawerLayout object *//*
         * nav drawer image to replace
         * 'Up' caret
         */
                R.string.ok, /* "open drawer" description for accessibility */
                R.string.close /* "close drawer" description for accessibility */
        ) {
            public void onDrawerClosed(View view) {
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
        mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);

        totalValueText = (TextView) findViewById(R.id.totalValue);
        lpcText = (TextView) findViewById(R.id.lcp);

        lpcText.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.THIN));
        totalValueText.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.THIN));
        ((TextView) findViewById(R.id.tv_unload_sih)).setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.THIN));
        ((TextView) findViewById(R.id.tv_unload_total_case)).setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.THIN));
        ((TextView) findViewById(R.id.tv_unload_total_outer)).setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.THIN));
        ((TextView) findViewById(R.id.tv_unload_total_piece)).setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.THIN));
        ((TextView) findViewById(R.id.totalText)).setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
        ((TextView) findViewById(R.id.lpc_title)).setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
        ((TextView) findViewById(R.id.unload_total_sihTxt)).setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
        ((TextView) findViewById(R.id.unload_total_caseTxt)).setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
        ((TextView) findViewById(R.id.unload_total_outerTxt)).setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
        ((TextView) findViewById(R.id.unload_total_pieceTxt)).setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
        productName = (TextView) findViewById(R.id.productName);
        productName.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
        mEdt_searchproductName.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
        productName.setOnTouchListener(new View.OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                int inType = productName.getInputType();
                productName.setInputType(InputType.TYPE_NULL);
                productName.onTouchEvent(event);
                productName.setInputType(inType);
                return true;
            }
        });

        lvwplist = (ListView) findViewById(R.id.list);
        lvwplist.setCacheColorHint(0);

        expandlvwplist = (ExpandableListView) findViewById(R.id.expand_lvwplist);
        expandlvwplist.setCacheColorHint(0);

        mSearchTypeArray = new ArrayList<>();
        mSearchTypeArray.add(getResources().getString(R.string.all));
        mSearchTypeArray.add(getResources().getString(R.string.product_name));
        mSearchTypeArray.add(getResources().getString(R.string.prod_code));
        mSearchTypeArray.add(getResources().getString(
                R.string.order_dialog_barcode));

        Vector<String> vect = new Vector<>();
        vect.addAll(Arrays.asList(getResources().getStringArray(
                R.array.productFilterArray)));
        addSpecialFilterList(vect);
        mSelectedFilterMap.put("General", GENERAL);
        QUANTITY1 = new EditText(this);
        QUANTITY1.setText("");
        if (!bmodel.configurationMasterHelper.SHOW_SPL_FILTER)
            hideSpecialFilter();

        try {
            mEdt_searchproductName.addTextChangedListener(new TextWatcher() {
                public void afterTextChanged(Editable s) {
//                    brandbutton = BRAND;
//                    generalbutton = GENERAL;
                    supportInvalidateOptionsMenu();
                    if (s.length() >= 3) {
                        loadSearchedList();
                    } else
                        updateGeneralText(GENERAL);
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
            Commons.printException("" + e);
        }

        // load location filter
        mLocationAdapter = new ArrayAdapter<>(this,
                android.R.layout.select_dialog_singlechoice);

        for (StandardListBO temp : bmodel.productHelper.getInStoreLocation())
            mLocationAdapter.add(temp);

        mEdt_searchproductName
                .setOnEditorActionListener(new TextView.OnEditorActionListener() {
                    @Override
                    public boolean onEditorAction(TextView v, int actionId,
                                                  KeyEvent event) {
                        if (actionId == EditorInfo.IME_ACTION_DONE) {
                            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                            imm.hideSoftInputFromWindow(
                                    mEdt_searchproductName.getWindowToken(),
                                    InputMethodManager.RESULT_UNCHANGED_SHOWN);

                            return true;
                        }
                        return false;
                    }
                });
        mBtn_clear
                .setOnEditorActionListener(new TextView.OnEditorActionListener() {
                    @Override
                    public boolean onEditorAction(TextView v, int actionId,
                                                  KeyEvent event) {
                        if (actionId == EditorInfo.IME_ACTION_DONE) {
                            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                            imm.hideSoftInputFromWindow(
                                    mEdt_searchproductName.getWindowToken(),
                                    InputMethodManager.RESULT_UNCHANGED_SHOWN);

                            return true;
                        }
                        return false;
                    }
                });
        getMandatoryFilters();
    }

    public void getMandatoryFilters() {
        if (bmodel.configurationMasterHelper.getGenFilter() != null) {

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
                    else if (bo.getConfigCode().equals(mDiscount))
                        isDiscount = true;
                    else if (bo.getConfigCode().equals(mFocusBrand3))
                        isFocusBrand3 = true;
                    else if (bo.getConfigCode().equals(mFocusBrand4))
                        isFocusBrand4 = true;
                    else if (bo.getConfigCode().equals(mSMP))
                        isSMP = true;
                    else if (bo.getConfigCode().equals(mNMustSell))
                        isNMustSell = true;
                    else if (bo.getConfigCode().equals(mStock))
                        isStock = true;

                }
            }
        }
    }

    public void setActionBarTitle(String title) {
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            setScreenTitle(title);
        }

    }

    @Override
    protected void onStart() {
        super.onStart();
        mDrawerLayout.closeDrawer(GravityCompat.END);
    }

    /**
     * add items for SpecialFilter
     *
     * @param filterList
     */
    public void addSpecialFilterList(Vector filterList) {
        mgeneralFilterList = filterList;
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    private void updateValue() {

    }

    public void onClick(View v) {
        Button vw = (Button) v;
        bmodel = (BusinessModel) getApplicationContext();
        bmodel.setContext(this);
        if (vw == mBtn_Search) {
            //viewFlipper.setInAnimation(this, R.anim.in_from_right);
            //viewFlipper.setOutAnimation(this, R.anim.out_to_right);
            viewFlipper.showNext();
            mEdt_searchproductName.requestFocus();
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.showSoftInput(mEdt_searchproductName,
                    InputMethodManager.SHOW_FORCED);
        } else if (vw == mBtnFilterPopup) {

            AlertDialog.Builder builderSingle = new AlertDialog.Builder(
                    ToolBarwithFilter.this);
            final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(
                    ToolBarwithFilter.this,
                    android.R.layout.select_dialog_singlechoice,
                    mSearchTypeArray);
            builderSingle.setAdapter(arrayAdapter,
                    new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            bmodel.setProductFilter(arrayAdapter.getItem(which));
                        }
                    });
            int selectedFiltPos = 0;
            if (mSearchTypeArray != null)
                selectedFiltPos = mSearchTypeArray.indexOf(bmodel
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
            try {
                InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);

                mEdt_searchproductName.setText("");

                supportInvalidateOptionsMenu();
                loadProductList();
            } catch (Exception e) {
                // TODO: handle exception
            }
        }
    }

    public void onNextButtonClick() {

    }

    public void onNoteButtonClick() {

    }

    public void onDotBtnEnable() {
        try {
            (findViewById(R.id.calcdot)).setVisibility(View.VISIBLE);
        } catch (Exception e) {
            Commons.printException("" + e);
        }
    }

    public void onDotBtnDisable() {
        try {
            (findViewById(R.id.calcdot)).setVisibility(View.GONE);
        } catch (Exception e) {
            Commons.printException("" + e);
        }
    }

    public void onKeyInvisibleSub() {
        try {
            findViewById(R.id.keypad).setVisibility(View.GONE);
        } catch (Exception e) {
            Commons.printException("" + e);
        }
    }

    public void onBackButtonClick() {
        startActivity(new Intent(ToolBarwithFilter.this, HomeScreenTwo.class));
        finish();
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        switch (id) {
            case 0:
                AlertDialog.Builder builder = new AlertDialog.Builder(ToolBarwithFilter.this)
                        .setIcon(null)
                        .setCancelable(false)
                        .setTitle(
                                getResources().getString(
                                        R.string.order_not_saved_go_back))
                        .setPositiveButton(getResources().getString(R.string.ok),
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog,
                                                        int whichButton) {
                                        bmodel.productHelper.clearOrderTable();
                                        startActivity(new Intent(
                                                ToolBarwithFilter.this,
                                                HomeScreenTwo.class));
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
                bmodel.applyAlertDialogTheme(builder);
                break;
        }
        return null;
    }

    public void eff() {
        String s = QUANTITY.getText().toString();
        if (!s.equals("0") && !s.equals("0.0")) {
            String qty = s + append;
            QUANTITY.setText(qty);
        } else
            QUANTITY.setText(append);
    }

    public void numberPressed(View vw) {
        Commons.print("number pressed");

        if (QUANTITY1 == null
                && (bmodel.configurationMasterHelper.DISABLE_MANUAL_ORDER || bmodel.configurationMasterHelper.MUST_STOCK_ONLY))
            bmodel.showAlert(
                    getResources().getString(R.string.order_entry_not_allowed),
                    0);
        if (QUANTITY == null) {
            if (QUANTITY1 != null)
                bmodel.showAlert(
                        getResources().getString(R.string.please_select_item),
                        0);
        } else {
            int id = vw.getId();
            if (id == R.id.calcdel) {
                String enterText = QUANTITY.getText().toString();
                String strQty;
                if (enterText.contains(".")) {
                    String[] splitValue = enterText.split("\\.");
                    try {

                        int s = SDUtil.convertToInt(splitValue[1]);
                        if (s == 0) {
                            s = SDUtil.convertToInt(splitValue[0]);
                            strQty = s + "";
                            QUANTITY.setText(strQty);
                        } else {
                            s = s / 10;
                            strQty = splitValue[0] + "." + s;
                            QUANTITY.setText(strQty);
                        }

                    } catch (ArrayIndexOutOfBoundsException e) {
                        Commons.printException("" + e);
                        strQty = SDUtil.convertToInt(enterText) + "";
                        QUANTITY.setText(strQty);
                    }


                } else {
                    int s = SDUtil.convertToInt(QUANTITY.getText()
                            .toString());
                    s = s / 10;
                    strQty = s + "";
                    QUANTITY.setText(strQty);
                }
            } else if (id == R.id.calcdot) {
                String s = QUANTITY.getText().toString();
                if (!s.contains(".")) {
                    s = s + ".";
                    QUANTITY.setText(s);
                }
            } else {
                Button ed = (Button) findViewById(vw.getId());
                append = ed.getText().toString();
                eff();
            }
            updateValue();
        }
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (result != null) {
            if (result.getContents() == null) {
                Toast.makeText(ToolBarwithFilter.this, "Cancelled", Toast.LENGTH_LONG).show();
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
            Toast.makeText(ToolBarwithFilter.this, getResources().getString(R.string.no_match_found), Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void updateCancel() {
        // Close the drawer
        mDrawerLayout.closeDrawers();
    }

    public Vector<ProductMasterBO> getProducts() {
        return bmodel.productHelper.getProductMaster();
    }

    public void loadSearchedList() {
        if (mEdt_searchproductName.getText().length() >= 3) {
            Vector<ProductMasterBO> items = getProducts();
            if (items == null) {
                bmodel.showAlert(
                        getResources().getString(R.string.no_products_exists),
                        0);
                return;
            }
            int siz = items.size();
            Commons.print("siz" + siz);
            mylist = new ArrayList<>();
            mSelectedFilter = bmodel.getProductFilter();
            for (int i = 0; i < siz; ++i) {
                ret = items.elementAt(i);
                if (mSelectedFilter.equals(getResources().getString(
                        R.string.order_dialog_barcode))) {
                    if (ret.getBarCode() != null && ret.getBarCode()
                            .toLowerCase()
                            .contains(
                                    mEdt_searchproductName.getText().toString()
                                            .toLowerCase())) {
                        if (generalbutton.equals(GENERAL) && brandbutton.equals(BRAND))//No filters selected
                            mylist.add(ret);
                        else if (applyProductAndSpecialFilter(ret))
                            mylist.add(ret);
                        Commons.print("siz Barcode : : : " + mEdt_searchproductName.getText().toString().toLowerCase());
                    }
                } else if (mSelectedFilter.equals(getResources().getString(
                        R.string.prod_code))) {
                    if (ret.getRField1() != null && ret.getRField1()
                            .toLowerCase()
                            .contains(
                                    mEdt_searchproductName.getText().toString()
                                            .toLowerCase()) || (ret.getProductCode() != null
                            && ret.getProductCode().toLowerCase().contains(mEdt_searchproductName.getText().toString()
                            .toLowerCase()))) {
                        if (generalbutton.equals(GENERAL) && brandbutton.equals(BRAND))//No filters selected
                            mylist.add(ret);
                        else if (applyProductAndSpecialFilter(ret))
                            mylist.add(ret);
                        Commons.print("siz GCASCode : : : " + mEdt_searchproductName.getText().toString().toLowerCase());
                    }
                } else if (mSelectedFilter.equals(getResources().getString(
                        R.string.product_name))) {
                    Commons.print("siz product_name : : : " + mEdt_searchproductName.getText().toString().toLowerCase());
                    if (ret.getProductShortName() != null && ret.getProductShortName()
                            .toLowerCase()
                            .contains(
                                    mEdt_searchproductName.getText().toString()
                                            .toLowerCase())) {
                        if (generalbutton.equals(GENERAL) && brandbutton.equals(BRAND))//No filters selected
                            mylist.add(ret);
                        else if (applyProductAndSpecialFilter(ret))
                            mylist.add(ret);
                    }
                } else {
                    if (ret.getBarCode() != null && ret.getBarCode()
                            .toLowerCase()
                            .contains(
                                    mEdt_searchproductName.getText().toString()
                                            .toLowerCase())) {
                        if (generalbutton.equals(GENERAL) && brandbutton.equals(BRAND))//No filters selected
                            mylist.add(ret);
                        else if (applyProductAndSpecialFilter(ret))
                            mylist.add(ret);
                    } else if (ret.getRField1() != null && ret.getRField1()
                            .toLowerCase()
                            .contains(
                                    mEdt_searchproductName.getText().toString()
                                            .toLowerCase()) || (ret.getProductCode() != null
                            && ret.getProductCode().toLowerCase().contains(mEdt_searchproductName.getText().toString()
                            .toLowerCase()))) {
                        if (generalbutton.equals(GENERAL) && brandbutton.equals(BRAND))//No filters selected
                            mylist.add(ret);
                        else if (applyProductAndSpecialFilter(ret))
                            mylist.add(ret);
                    } else if (ret.getProductShortName() != null && ret.getProductShortName()
                            .toLowerCase()
                            .contains(
                                    mEdt_searchproductName.getText().toString()
                                            .toLowerCase())) {
                        if (generalbutton.equals(GENERAL) && brandbutton.equals(BRAND))//No filters selected
                            mylist.add(ret);
                        else if (applyProductAndSpecialFilter(ret))
                            mylist.add(ret);
                    }
                }
            }
            refreshList();
        } else {
            Toast.makeText(this, "Enter atleast 3 letters.", Toast.LENGTH_SHORT)
                    .show();
        }
    }

    public void refreshList() {
        Commons.print("Parent," + "Parent RefreshList");

    }

    @Override
    public void updateBrandText(String mFilterText, int bid) {
        mSelectedBrandID = bid;
        String mCompertior = "Filt23";
        try {
            // Close the drawer
            mDrawerLayout.closeDrawers();

            // Change the Brand button Name
            brandbutton = mFilterText;

            // Consider generalbutton text if it is dependent filter.
            String generaltxt = generalbutton;

            // Clear the productName
            productName.setText("");

            items = getProducts();
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
                ProductMasterBO ret = items.elementAt(i);
                /**
                 * After scanning product,Barcode value stored in
                 * strBarCodeSearch Variable
                 */

                if (ret.getBarCode().equals(strBarCodeSearch)
                        || ret.getCasebarcode().equals(strBarCodeSearch)
                        || ret.getOuterbarcode().equals(strBarCodeSearch)
                        || strBarCodeSearch.equals("ALL")) {

                    if (isSpecialFilter_enabled) {
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
                                    && applyCommonFilterConfig(ret)) {
                                mylist.add(ret);
                            } else if (generaltxt.equals(mSbdGaps)
                                    && (ret.isRPS() && !ret.isSBDAcheived())) {
                                mylist.add(ret);
                            } else if (generaltxt.equals(GENERAL)) {
                                mylist.add(ret);
                            } else if (generaltxt.equals(mInStock)
                                    && ret.getWSIH() > 0) {
                                mylist.add(ret);
                            } else if (generaltxt.equals(mOnAllocation)
                                    && ret.getSIH() > 0
                                    && ret.isAllocation() == 1
                                    && bmodel.configurationMasterHelper.IS_SIH_VALIDATION) {
                                mylist.add(ret);
                            } else if (generaltxt.equals(mPromo)
                                    && ret.isPromo()) {
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

                            } else if (generaltxt.equals(msih)
                                    && ret.getSIH() > 0) {
                                mylist.add(ret);

                            } else if (generaltxt.equals(mOOS)
                                    && ret.getOos() == 0) {
                                mylist.add(ret);
                            } else if (generaltxt.equals(mDiscount) && ret.getIsDiscountable() == 1) {
                                mylist.add(ret);
                            } else if (generaltxt.equals(mFocusBrand3) && ret.getIsFocusBrand3() == 1) {
                                mylist.add(ret);
                            } else if (generaltxt.equals(mFocusBrand4) && ret.getIsFocusBrand4() == 1) {
                                mylist.add(ret);
                            } else if (generaltxt.equals(mSMP) && ret.getIsSMP() == 1) {
                                mylist.add(ret);
                            } else if (generaltxt.equals(mCompertior) && ret.getOwn() == 0) {
                                mylist.add(ret);
                            }
                        }
                    } else {
                        if (bid == -1 && ret.getIsSaleable() == 1) {
                            if (mFilterText.equals("Brand")) {
                                mylist.add(ret);
                            }
                        } else if (bid == ret.getParentid()
                                && ret.getIsSaleable() == 1) {
                            mylist.add(ret);
                        }

                    }
                }
            }

            refreshList();

            updateValue();
        } catch (Exception e) {
            Commons.printException("" + e);
        }
    }

    public boolean applyCommonFilterConfig(ProductMasterBO ret) {
        if ((isSbd && ret.isRPS()) || (isSbdGaps && ret.isRPS() && !ret.isSBDAcheived()) || (isOrdered && (ret.getOrderedPcsQty() > 0 || ret.getOrderedCaseQty() > 0 || ret.getOrderedOuterQty() > 0))
                || (isPurchased && ret.getIsPurchased() == 1) || (isInitiative && ret.getIsInitiativeProduct() == 1) || (isOnAllocation && ret.isAllocation() == 1
                && bmodel.configurationMasterHelper.IS_SIH_VALIDATION) || (isInStock && ret.getWSIH() > 0) || (isPromo && ret.isPromo()) || (isMustSell && ret.getIsMustSell() == 1)
                || (isFocusBrand && ret.getIsFocusBrand() == 1) || (isFocusBrand2 && ret.getIsFocusBrand2() == 1) || (isSIH && ret.getSIH() > 0) || (isOOS && ret.getOos() == 0)
                || (isNMustSell && ret.getIsNMustSell() == 1) || (isStock && ((ret.getLocations().get(mSelectedLocationIndex).getShelfPiece() > 0
                || ret.getLocations().get(mSelectedLocationIndex).getShelfCase() > 0 || ret.getLocations().get(mSelectedLocationIndex).getShelfOuter() > 0 || ret.getLocations().get(mSelectedLocationIndex).getWHPiece() > 0
                || ret.getLocations().get(mSelectedLocationIndex).getWHCase() > 0 || ret.getLocations().get(mSelectedLocationIndex).getWHOuter() > 0) || ret.getLocations().get(mSelectedLocationIndex).getAvailability() > -1)) || (isDiscount && ret.getIsDiscountable() == 1)) {

            return true;
        }
        return false;
    }

    public void showLocation() {
        AlertDialog.Builder builder;

        builder = new AlertDialog.Builder(this);
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
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_actionbar_with_filter, menu);
        return super.onCreateOptionsMenu(menu);
    }

    /**
     * used to hide the specialFilter
     */
    public void hideSpecialFilter() {
        isSpecialFilter_enabled = false;
        generalbutton = GENERAL;

    }

    public void hideNextButton() {
        next_button_enabled = false;
    }

    public void hideRemarksButton() {
        remarks_button_enable = false;
    }

    public void hideShemeButton() {
        scheme_button_enable = false;
    }

    public void showExpandButton() {
        expand_collapse_button_enable = true;
    }

    public void hideLocationButton() {
        location_button_enable = false;
    }

    // Called whenever we call invalidateOptionsMenu()
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {

        // Change color if Filter is selected
        if (!generalbutton.equals(GENERAL))
            menu.findItem(R.id.menu_spl_filter).setIcon(
                    R.drawable.ic_action_star_select);

        if (mSelectedIdByLevelId != null) {
            for (Integer id : mSelectedIdByLevelId.keySet()) {
                if (mSelectedIdByLevelId.get(id) > 0) {
                    menu.findItem(R.id.menu_fivefilter).setIcon(
                            R.drawable.ic_action_filter_select);
                    break;
                }
            }
        }

        Drawable drawable = ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_down_arrow);
        drawable.setColorFilter(ContextCompat.getColor(getApplicationContext(), R.color.white), PorterDuff.Mode.SRC_ATOP);
        menu.findItem(R.id.menu_expand).setIcon(drawable);
        // If the nav drawer is open, hide action items related to the content
        // view
        boolean drawerOpen = mDrawerLayout.isDrawerOpen(GravityCompat.END);

        if (isSpecialFilter_enabled)
            menu.findItem(R.id.menu_spl_filter).setVisible(!drawerOpen);
        else
            menu.findItem(R.id.menu_spl_filter).setVisible(false);

        if (next_button_enabled)
            menu.findItem(R.id.menu_next).setVisible(!drawerOpen);
        else
            menu.findItem(R.id.menu_next).setVisible(false);
        if (remarks_button_enable)
            menu.findItem(R.id.menu_remarks).setVisible(!drawerOpen);
        else

            menu.findItem(R.id.menu_remarks).setVisible(false);

        if (scheme_button_enable)
            menu.findItem(R.id.menu_scheme).setVisible(!drawerOpen);
        else
            menu.findItem(R.id.menu_scheme).setVisible(false);

        if (so_apply)
            menu.findItem(R.id.menu_apply_so).setVisible(!drawerOpen);
        else
            menu.findItem(R.id.menu_apply_so).setVisible(false);

        if (std_apply)
            menu.findItem(R.id.menu_apply_std_qty).setVisible(!drawerOpen);
        else
            menu.findItem(R.id.menu_apply_std_qty).setVisible(false);

        if (bmodel.productHelper.getInStoreLocation().size() == 1)
            menu.findItem(R.id.menu_loc_filter).setVisible(false);

        if (location_button_enable)
            menu.findItem(R.id.menu_loc_filter).setVisible(!drawerOpen);
        else
            menu.findItem(R.id.menu_loc_filter).setVisible(false);

        if (!sih_apply)
            menu.findItem(R.id.menu_sih_apply).setVisible(false);

        menu.findItem(R.id.menu_fivefilter).setVisible(false);

        if (bmodel.productHelper.isFilterAvaiable("MENU_STK_ORD"))
            menu.findItem(R.id.menu_fivefilter).setVisible(true);

        if (expand_collapse_button_enable)
            menu.findItem(R.id.menu_expand).setVisible(true);
        else
            menu.findItem(R.id.menu_expand).setVisible(false);

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
                onBackButtonClick();
            return true;
        } else if (i == R.id.menu_next) {
            onNextButtonClick();
            return true;
        } else if (i == R.id.menu_spl_filter) {// generalFilterClicked();
            generalFilterClickedFragment();
            return true;
        } else if (i == R.id.menu_loc_filter) {
            showLocation();
            return true;
        } else if (i == R.id.menu_remarks) {
            onNoteButtonClick();
            return true;
        } else if (i == R.id.menu_apply_so) {
            applyStockSo();
            return true;
        } else if (i == R.id.menu_apply_std_qty) {
            applyStdQty();
            return true;
        } else if (i == R.id.menu_scheme) {
            loadSchemeDialog();
            return true;
        } else if (i == R.id.menu_sih_apply) {
            applySIH();
            return true;
        } else if (i == R.id.menu_fivefilter) {
            if (bmodel.configurationMasterHelper.IS_UNLINK_FILTERS) {
                generalbutton = GENERAL;
                mSelectedFilterMap.put("General", GENERAL);
            }
            FiveFilterFragment();
            return true;
        } else if (i == R.id.menu_barcode) {
            ToolBarwithFilter.this.checkAndRequestPermissionAtRunTime(2);
            int permissionStatus = ContextCompat.checkSelfPermission(ToolBarwithFilter.this,
                    Manifest.permission.CAMERA);
            if (permissionStatus == PackageManager.PERMISSION_GRANTED) {
                IntentIntegrator integrator = new IntentIntegrator(ToolBarwithFilter.this) {
                    @Override
                    protected void startActivityForResult(Intent intent, int code) {
                        ToolBarwithFilter.this.startActivityForResult(intent, IntentIntegrator.REQUEST_CODE); // REQUEST_CODE override
                    }
                };
                integrator.setBeepEnabled(false).initiateScan();
            } else {
                Toast.makeText(ToolBarwithFilter.this,
                        getResources().getString(R.string.permission_enable_msg)
                                + " " + getResources().getString(R.string.permission_camera)
                        , Toast.LENGTH_LONG).show();
            }
            return true;
        }
        return super.onOptionsItemSelected(item);
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
            bundle.putSerializable("selectedFilter", mSelectedIdByLevelId);

            // set Fragmentclass Arguments
            FilterFiveFragment<Object> fragobj = new FilterFiveFragment<>();
            fragobj.setArguments(bundle);
            ft.replace(R.id.right_drawer, fragobj, "Fivefilter");
            ft.commit();
        } catch (Exception e) {
            Commons.printException("" + e);
        }
    }

    protected void loadSchemeDialog() {
        // TODO Auto-generated method stub

    }

    public void applyStdQty() {
        // TODO Auto-generated method stub

    }

    public void applyStockSo() {
        // TODO Auto-generated method stub

    }

    public void applySIH() {
        // TODO Auto-generated method stub

    }

    private void generalFilterClickedFragment() {
        try {
            QUANTITY = null;
            mDrawerLayout.openDrawer(GravityCompat.END);
            FragmentManager fm = getSupportFragmentManager();
            SpecialFilterFragment frag = (SpecialFilterFragment) fm
                    .findFragmentByTag("filter");
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
            ft.add(R.id.right_drawer, fragobj, "filter");
            ft.commit();
        } catch (Exception e) {
            Commons.printException("" + e);
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
            if (arg0.getText().length() > 0) {
                supportInvalidateOptionsMenu();
            }
            loadSearchedList();
            return true;
        }
        return false;
    }

    private void getOverflowMenu() {
        try {
            ViewConfiguration config = ViewConfiguration.get(this);
            java.lang.reflect.Field menuKeyField = ViewConfiguration.class
                    .getDeclaredField("sHasPermanentMenuKey");
            if (menuKeyField != null) {
                menuKeyField.setAccessible(true);
                menuKeyField.setBoolean(config, false);
            }
        } catch (Exception e) {
            Commons.printException("" + e);
        }
    }

    @Override
    public void updateFromFiveLevelFilter(int mFilteredPid, HashMap<Integer, Integer> mSelectedIdByLevelId, ArrayList<Integer> mAttributeProducts, String mFilterText) {
        mylist = new ArrayList<>();
        fiveFilter_productIDs = new ArrayList<>();
        brandbutton = mFilterText;
        if (mAttributeProducts != null) {

            if (mFilteredPid != 0) {
                for (ProductMasterBO productBO : items) {
                    if (productBO.getIsSaleable() == 1 && productBO.getParentHierarchy().contains("/" + mFilteredPid + "/")) {

                        // here we get all products mapped to parent id list, then that product will be added only if it is mapped to selected attribute
                        if (mAttributeProducts.contains(SDUtil.convertToInt(productBO.getProductID()))) {
                            mylist.add(productBO);
                            fiveFilter_productIDs.add(productBO.getProductID());
                        }
                    }
                }
            } else {
                for (int pid : mAttributeProducts) {
                    for (ProductMasterBO productBO : items) {

                        if (pid == SDUtil.convertToInt(productBO.getProductID()) && productBO.getIsSaleable() == 1) {
                            mylist.add(productBO);
                            fiveFilter_productIDs.add(productBO.getProductID());
                        }
                    }
                }
            }
        } else {
            for (ProductMasterBO productBO : items) {
                if (productBO.getIsSaleable() == 1) {
                    if (productBO.getParentHierarchy().contains("/" + mFilteredPid + "/")) {
                        mylist.add(productBO);
                        fiveFilter_productIDs.add(productBO.getProductID());
                    }
                }
            }
        }
        mDrawerLayout.closeDrawers();

        refreshList();

        updateValue();
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
                || (generaltxt.equalsIgnoreCase(mShelf) && ((ret.getLocations().get(mSelectedLocationIndex).getShelfCase() > 0 || ret.getLocations().get(mSelectedLocationIndex).getShelfPiece() > 0 || ret.getLocations().get(mSelectedLocationIndex).getShelfOuter() > 0) || ret.getLocations().get(mSelectedLocationIndex).getAvailability() > -1));
    }

    public void loadProductList() {
        try {
            Vector<ProductMasterBO> items = getProducts();

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
        } catch (Exception e) {
            Commons.printException(e);
        }
    }
}