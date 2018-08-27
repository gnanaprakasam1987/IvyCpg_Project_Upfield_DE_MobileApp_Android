package com.ivy.cpg.view.van;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
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
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.BaseExpandableListAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;
import android.widget.ViewFlipper;

import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.bo.LoadManagementBO;
import com.ivy.sd.png.commons.IvyBaseActivityNoActionBar;
import com.ivy.sd.png.commons.SDUtil;
import com.ivy.sd.png.model.BrandDialogInterface;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.model.FiveLevelFilterCallBack;
import com.ivy.sd.png.util.Commons;
import com.ivy.sd.png.view.FilterFiveFragment;
import com.ivy.utils.FontUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Vector;

public class StockViewActivity extends IvyBaseActivityNoActionBar implements
        BrandDialogInterface, OnEditorActionListener, FiveLevelFilterCallBack, View.OnClickListener {
    private ArrayList<LoadManagementBO> mylist;
    private ExpandableListAdapter expandableListAdapter;
    private boolean isExpandList = false;
    private DrawerLayout mDrawerLayout;
    private ViewFlipper viewFlipper;
    public HashMap<Integer, Integer> mSelectedIdByLevelId;
    protected BusinessModel bmodel;
    private ExpandableListView expandlvwplist;
    private EditText mEdt_searchproductName;
    private InputMethodManager inputManager;
    private static final String BRAND = "Brand";
    private static final String GENERAL = "General";
    private TextView productName;
    boolean expand_collapse_button_enable = false;
    private HashMap<String, String> mSelectedFilterMap = new HashMap<>();
    private ArrayList<String> mSearchTypeArray;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final Intent i = getIntent();
        setContentView(R.layout.layout_stockview);
        overridePendingTransition(R.anim.trans_left_in, R.anim.trans_left_out);
        bmodel = (BusinessModel) getApplicationContext();
        bmodel.setContext(this);


        Toolbar toolbar = toolbar = (Toolbar) findViewById(R.id.toolbar);

        if (toolbar != null) {

            setSupportActionBar(toolbar);
            getSupportActionBar().setIcon(null);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            // Used to on / off the back arrow icon
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            // Used to remove the app logo actionbar icon and set title as home
            // (title support click)
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            setScreenTitle(i.getStringExtra("screentitle"));
        }

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        // set a custom shadow that overlays the main content when the drawer
        // opens
        mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow,
                GravityCompat.START);
        mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow,
                GravityCompat.END);
        mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
        inputManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

        viewFlipper = (ViewFlipper) findViewById(R.id.view_flipper);

        mEdt_searchproductName = (EditText) findViewById(R.id.edt_searchproductName);
        Button mBtn_Search = (Button) findViewById(R.id.btn_search);
        Button mBtnFilterPopup = (Button) findViewById(R.id.btn_filter_popup);
        Button mBtn_clear = (Button) findViewById(R.id.btn_clear);

        mBtn_Search.setOnClickListener(this);
        mBtnFilterPopup.setOnClickListener(this);
        mBtn_clear.setOnClickListener(this);
        mEdt_searchproductName.setOnEditorActionListener(this);

        expandlvwplist = (ExpandableListView) findViewById(R.id.expand_lvwplist);
        expandlvwplist.setCacheColorHint(0);

        productName = (TextView) findViewById(R.id.productName);
        productName.setTypeface(FontUtils.getFontRoboto(FontUtils.FontType.LIGHT, this));
        mEdt_searchproductName.setTypeface(FontUtils.getFontRoboto(FontUtils.FontType.LIGHT, this));
        productName.setOnTouchListener(new View.OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                int inType = productName.getInputType();
                productName.setInputType(InputType.TYPE_NULL);
                productName.onTouchEvent(event);
                productName.setInputType(inType);
                return true;
            }
        });

        mEdt_searchproductName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                supportInvalidateOptionsMenu();
                if (s.length() >= 3) {
                    loadSearchedList();
                }
            }
        });

        mSearchTypeArray = new ArrayList<>();
        mSearchTypeArray.add(getResources().getString(R.string.product_name));
        mSearchTypeArray.add(getResources().getString(R.string.order_gcas));
        mSearchTypeArray.add(getResources().getString(
                R.string.order_dialog_barcode));

        Vector<String> vect = new Vector<>();
        vect.addAll(Arrays.asList(getResources().getStringArray(
                R.array.productFilterArray)));
        mSelectedFilterMap.put("General", GENERAL);
        ((TextView) findViewById(R.id.product_name)).setTypeface(FontUtils.getFontRoboto(FontUtils.FontType.MEDIUM, this));
        ((TextView) findViewById(R.id.sihCaseTitle)).setTypeface(FontUtils.getFontRoboto(FontUtils.FontType.MEDIUM, this));
        ((TextView) findViewById(R.id.sihOuterTitle)).setTypeface(FontUtils.getFontRoboto(FontUtils.FontType.MEDIUM, this));
        ((TextView) findViewById(R.id.sihTitle)).setTypeface(FontUtils.getFontRoboto(FontUtils.FontType.MEDIUM, this));

        if (bmodel.configurationMasterHelper.CONVERT_STOCK_SIH_OU ||
                bmodel.configurationMasterHelper.CONVERT_STOCK_SIH_CS ||
                bmodel.configurationMasterHelper.CONVERT_STOCK_SIH_PS) {

            if (bmodel.configurationMasterHelper.CONVERT_STOCK_SIH_PS) {
                try {
                    if (bmodel.labelsMasterHelper.applyLabels(findViewById(
                            R.id.sihTitle).getTag()) != null)
                        ((TextView) findViewById(R.id.sihTitle))
                                .setText(bmodel.labelsMasterHelper
                                        .applyLabels(findViewById(R.id.sihTitle)
                                                .getTag()));
                } catch (Exception e) {
                    Commons.printException("" + e);
                    Commons.printException("" + e);
                }

                findViewById(R.id.sihCaseTitle).setVisibility(View.GONE);
                findViewById(R.id.sihOuterTitle).setVisibility(View.GONE);
            } else if (bmodel.configurationMasterHelper.CONVERT_STOCK_SIH_CS) {
                try {
                    if (bmodel.labelsMasterHelper.applyLabels(findViewById(
                            R.id.sihCaseTitle).getTag()) != null)
                        ((TextView) findViewById(R.id.sihCaseTitle))
                                .setText(bmodel.labelsMasterHelper
                                        .applyLabels(findViewById(R.id.sihCaseTitle)
                                                .getTag()));
                } catch (Exception e) {
                    Commons.printException("" + e);
                    Commons.printException("" + e);
                }

                findViewById(R.id.sihTitle).setVisibility(View.GONE);
                findViewById(R.id.sihOuterTitle).setVisibility(View.GONE);
            } else if (bmodel.configurationMasterHelper.CONVERT_STOCK_SIH_OU) {
                try {
                    if (bmodel.labelsMasterHelper.applyLabels(findViewById(
                            R.id.sihOuterTitle).getTag()) != null)
                        ((TextView) findViewById(R.id.sihOuterTitle))
                                .setText(bmodel.labelsMasterHelper
                                        .applyLabels(findViewById(R.id.sihOuterTitle)
                                                .getTag()));
                } catch (Exception e) {
                    Commons.printException("" + e);
                    Commons.printException("" + e);
                }

                findViewById(R.id.sihCaseTitle).setVisibility(View.GONE);
                findViewById(R.id.sihTitle).setVisibility(View.GONE);
            }
        } else if (bmodel.configurationMasterHelper.SHOW_SIH_SPLIT) {
            if (!bmodel.configurationMasterHelper.SHOW_ORDER_CASE) {
                findViewById(R.id.sihCaseTitle).setVisibility(View.GONE);
            } else {
                try {
                    if (bmodel.labelsMasterHelper.applyLabels(findViewById(
                            R.id.sihCaseTitle).getTag()) != null)
                        ((TextView) findViewById(R.id.sihCaseTitle))
                                .setText(bmodel.labelsMasterHelper
                                        .applyLabels(findViewById(
                                                R.id.sihCaseTitle).getTag()));
                } catch (Exception e) {
                    Commons.printException("" + e);
                }
            }

            if (!bmodel.configurationMasterHelper.SHOW_OUTER_CASE) {
                findViewById(R.id.sihOuterTitle).setVisibility(View.GONE);
            } else {
                try {
                    if (bmodel.labelsMasterHelper.applyLabels(findViewById(
                            R.id.sihOuterTitle).getTag()) != null)
                        ((TextView) findViewById(R.id.sihOuterTitle))
                                .setText(bmodel.labelsMasterHelper
                                        .applyLabels(findViewById(
                                                R.id.sihOuterTitle).getTag()));
                } catch (Exception e) {
                    Commons.printException("" + e);
                }
            }

            if (!bmodel.configurationMasterHelper.SHOW_ORDER_PCS) {
                findViewById(R.id.sihTitle).setVisibility(View.GONE);
            } else {
                try {
                    if (bmodel.labelsMasterHelper.applyLabels(findViewById(
                            R.id.sihTitle).getTag()) != null)
                        ((TextView) findViewById(R.id.sihTitle))
                                .setText(bmodel.labelsMasterHelper
                                        .applyLabels(findViewById(R.id.sihTitle)
                                                .getTag()));
                } catch (Exception e) {
                    Commons.printException("" + e);
                    Commons.printException("" + e);
                }
            }
        } else {
            findViewById(R.id.sihCaseTitle).setVisibility(View.GONE);
            findViewById(R.id.sihOuterTitle).setVisibility(View.GONE);
            try {
                if (bmodel.labelsMasterHelper.applyLabels(findViewById(
                        R.id.sihTitle).getTag()) != null)
                    ((TextView) findViewById(R.id.sihTitle))
                            .setText(bmodel.labelsMasterHelper
                                    .applyLabels(findViewById(R.id.sihTitle)
                                            .getTag()));
            } catch (Exception e) {
                Commons.printException("" + e);
            }
        }

        ActionBarDrawerToggle mDrawerToggle = new ActionBarDrawerToggle(StockViewActivity.this,
                mDrawerLayout,
                R.string.ok,
                R.string.close
        ) {
            public void onDrawerClosed(View view) {
                if (getSupportActionBar() != null) {
                    setScreenTitle(i.getStringExtra("screentitle"));
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
        if (getSupportActionBar() != null) {
            getSupportActionBar().setIcon(null);
        }
        mylist = new ArrayList<>(bmodel.productHelper.getLoadMgmtProducts());
        Commons.print("stock view oncreate," + String.valueOf(mylist.size()));

        updateBrandText("Brand", -1);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_actionbar_with_filter, menu);
        return super.onCreateOptionsMenu(menu);
    }


    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {

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

        menu.findItem(R.id.menu_spl_filter).setVisible(false);
        menu.findItem(R.id.menu_next).setVisible(false);
        menu.findItem(R.id.menu_remarks).setVisible(false);
        menu.findItem(R.id.menu_scheme).setVisible(false);
        menu.findItem(R.id.menu_apply_so).setVisible(false);
        menu.findItem(R.id.menu_apply_std_qty).setVisible(false);
        menu.findItem(R.id.menu_loc_filter).setVisible(false);
        menu.findItem(R.id.menu_sih_apply).setVisible(false);

        menu.findItem(R.id.menu_fivefilter).setVisible(false);

        if (bmodel.productHelper.isFilterAvaiable("MENU_LOAD_MANAGEMENT"))
            menu.findItem(R.id.menu_fivefilter).setVisible(true);

        if (expand_collapse_button_enable)
            menu.findItem(R.id.menu_expand).setVisible(true);
        else
            menu.findItem(R.id.menu_expand).setVisible(false);


        if (drawerOpen)
            menu.clear();
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();
        if (id == android.R.id.home) {
            if (mDrawerLayout.isDrawerOpen(GravityCompat.END))
                mDrawerLayout.closeDrawers();
            else {
                onBackButtonClick();
            }
        } else if (id == R.id.menu_expand) {
            if (!isExpandList) {// used to view batch wise stock

                if (inputManager.isAcceptingText())// hide soft key board after select expend menu
                    inputManager.hideSoftInputFromWindow(
                            mEdt_searchproductName.getWindowToken(), 0);

                for (int i = 0; i < expandableListAdapter.getGroupCount(); i++) {
                    expandlvwplist.expandGroup(i);
                }
                isExpandList = true;
            } else {// used to close batch wise stock
                for (int i = 0; i < expandableListAdapter.getGroupCount(); i++) {
                    expandlvwplist.collapseGroup(i);
                }
                isExpandList = false;
            }

        } else if (id == R.id.menu_fivefilter) {
            if (bmodel.configurationMasterHelper.IS_UNLINK_FILTERS) {

                mSelectedFilterMap.put("General", GENERAL);
            }
            FiveFilterFragment();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    @Override
    public void updateBrandText(String mFilterText, int bid) {

        mDrawerLayout.closeDrawers();


        productName.setText("");
        if (mylist == null) {
            bmodel.showAlert(
                    getResources().getString(R.string.no_products_exists), 0);
            return;
        }
        int siz = mylist.size();
        final ArrayList<LoadManagementBO> temp = new ArrayList<>();
        for (int i = 0; i < siz; ++i) {

            LoadManagementBO ret = mylist.get(i);
            if (bid == -1 || bid == ret.getParentid()) {
                if (ret.getSih() > 0) {
                    temp.add(ret);
                }

            }
        }

        ArrayList<LoadManagementBO> temp2 = new ArrayList<>();
        for (LoadManagementBO batchBo : mylist) {
            if (bid == -1 || bid == batchBo.getParentid()) {
                if (batchBo.getStocksih() > 0)
                    temp2.add(batchBo);
            }
        }

        HashMap<String, ArrayList<LoadManagementBO>> listDataChild = new HashMap<>();
        ArrayList<LoadManagementBO> childList = null;
        for (LoadManagementBO parentBo : temp) {
            childList = new ArrayList<LoadManagementBO>();
            for (LoadManagementBO childBO : temp2) {
                if (parentBo.getProductid() == childBO.getProductid()
                        && childBO.getBatchlist() != null
                        && !childBO.getBatchId().isEmpty()
                        && !childBO.getBatchId().equals("0"))
                    childList.add(childBO);
            }
            String pid = String.valueOf(parentBo.getProductid());
            listDataChild.put(pid, childList);//load child batch List data
        }

        if (childList != null)
            if (childList.size() > 0)
                showExpandButton();
//---------- remove duplicate product name from given list-----------//
        /**
         * product getting duplicated if more than batch is available single product so in this case
         * only we removed duplicated product
         */

        for (int i = 0; i < temp.size(); i++) {

            for (int j = i + 1; j < temp.size(); j++) {
                if (temp.get(i).getProductid() == temp.get(j).getProductid()) {
                    temp.remove(j);
                    j--;
                }
            }
        }

        expandableListAdapter = new ExpandableListAdapter(this, temp, listDataChild);
        expandlvwplist.setAdapter(expandableListAdapter);


    }

    @Override
    public void updateGeneralText(String mFilterText) {
        // set the spl filter name on the button for display

        if (mSelectedIdByLevelId != null)
            mSelectedIdByLevelId.clear();

        updateBrandText(BRAND, -1);
    }

    @Override
    public void updateCancel() {
        // Close Drawer
        mDrawerLayout.closeDrawers();
    }


    private void onBackButtonClick() {
        finish();
        overridePendingTransition(R.anim.trans_right_in, R.anim.trans_right_out);
    }


    public void loadSearchedList() {

        if (mylist == null) {
            bmodel.showAlert(
                    getResources().getString(R.string.no_products_exists),
                    0);
            return;
        }
        int siz = mylist.size();
        ArrayList<LoadManagementBO> temp = new ArrayList<>();
        String mSelectedFilter = bmodel.getProductFilter();
        for (int i = 0; i < siz; ++i) {
            LoadManagementBO ret = mylist.get(i);
            if ("BarCode".equals(mSelectedFilter)) {
                if (ret.getSih() > 0) {
                    if (ret.getBarcode()
                            .toLowerCase()
                            .contains(
                                    mEdt_searchproductName.getText()
                                            .toString().toLowerCase()))
                        temp.add(ret);
                }

            } else if ("GCAS Code".equals(mSelectedFilter)) {
                if (ret.getSih() > 0) {
                    if (ret.getRField1()
                            .toLowerCase()
                            .contains(
                                    mEdt_searchproductName.getText()
                                            .toString().toLowerCase()))
                        temp.add(ret);
                }

            } else if (getResources().getString(
                    R.string.product_name).equals(mSelectedFilter)) {
                if (ret.getSih() > 0) {
                    if (ret.getProductshortname()
                            .toLowerCase()
                            .contains(
                                    mEdt_searchproductName.getText()
                                            .toString().toLowerCase()))
                        temp.add(ret);
                }
            }
        }
        HashMap<String, ArrayList<LoadManagementBO>> listDataChild = new HashMap<>();

        for (LoadManagementBO parentBo : temp) {
            ArrayList<LoadManagementBO> childList = new ArrayList<>();
            for (LoadManagementBO childBO : temp) {
                if (parentBo.getProductid() == childBO.getProductid()
                        && childBO.getBatchlist() != null && !childBO.getBatchId().isEmpty()
                        && !childBO.getBatchId().equals("0"))
                    childList.add(childBO);
            }
            String pid = String.valueOf(parentBo.getProductid());

            listDataChild.put(pid, childList);//load child batch List data
        }

//---------- remove duplicate product name from given list-----------///
        /**
         * product getting duplicated if more than batch is available single product so in this case
         * only we removed duplicated product
         */

        for (int i = 0; i < temp.size(); i++) {

            for (int j = i + 1; j < temp.size(); j++) {
                if (temp.get(i).getProductid() == temp.get(j).getProductid()) {
                    temp.remove(j);
                    j--;
                }
            }
        }

        expandableListAdapter = new ExpandableListAdapter(this, temp, listDataChild);
        expandlvwplist.setAdapter(expandableListAdapter);
    }

    @Override
    public void updateFromFiveLevelFilter(int mFilteredPid, HashMap<Integer, Integer> mSelectedIdByLevelId, ArrayList<Integer> mAttributeProducts, String mFilterText) {
        ArrayList<LoadManagementBO> filterlist = new ArrayList<>();
        if (mAttributeProducts != null) {
            if (mFilteredPid != 0) {
                for (LoadManagementBO productBO : mylist) {
                    if (productBO.getParentHierarchy().contains("/" + mFilteredPid + "/")) {
                        // here we get all products mapped to parent id list, then that product will be added only if it is mapped to selected attribute
                        if (mAttributeProducts.contains(productBO.getProductid())) {
                            filterlist.add(productBO);
                        }
                    }
                }
            } else {
                for (int pid : mAttributeProducts) {
                    for (LoadManagementBO productBO : mylist) {
                        if (pid == productBO.getProductid() && productBO.getSih() > 0) {
                            filterlist.add(productBO);
                        }
                    }
                }
            }
        } else {
            if (mFilteredPid != 0 && !mFilterText.equalsIgnoreCase("")) {
                for (LoadManagementBO productBO : mylist) {
                    if (productBO.getParentHierarchy().contains("/" + mFilteredPid + "/")) {

                        if (productBO.getSih() > 0)
                            filterlist.add(productBO);
                    }
                }
            } else {
                for (LoadManagementBO productBO : mylist) {
                    if (productBO.getSih() > 0)
                        filterlist.add(productBO);
                }
            }
        }

        HashMap<String, ArrayList<LoadManagementBO>> listDataChild = new HashMap<>();
        ArrayList<LoadManagementBO> childList = null;
        for (LoadManagementBO parentBo : filterlist) {
            childList = new ArrayList<>();
            for (LoadManagementBO childBO : filterlist) {
                if (parentBo.getProductid() == childBO.getProductid()
                        && childBO.getBatchlist() != null
                        && !childBO.getBatchId().isEmpty()
                        && !childBO.getBatchId().equals("0"))
                    childList.add(childBO);
            }
            String pid = String.valueOf(parentBo.getProductid());

            listDataChild.put(pid, childList);//load child batch List data
        }
        if (childList != null)
            if (childList.size() > 0)
                showExpandButton();

//---------- remove duplicate product name from given list-----------///
        /**
         * product getting duplicated if more than batch is available single product so in this case
         * only we removed duplicated product
         */
        for (int i = 0; i < filterlist.size(); i++) {

            for (int j = i + 1; j < filterlist.size(); j++) {
                if (filterlist.get(i).getProductid() == filterlist.get(j).getProductid()) {
                    filterlist.remove(j);
                    j--;
                }
            }
        }
        expandableListAdapter = new ExpandableListAdapter(this, filterlist, listDataChild);
        expandlvwplist.setAdapter(expandableListAdapter);

        mDrawerLayout.closeDrawers();
        if (mSelectedIdByLevelId != null)
            this.mSelectedIdByLevelId = mSelectedIdByLevelId;
    }

    public void loadProductList() {
        updateGeneralText(GENERAL);
    }

    @Override
    public boolean onEditorAction(TextView arg0, int arg1, KeyEvent arg2) {
        if (arg1 == EditorInfo.IME_ACTION_DONE) {
            if (mEdt_searchproductName.getText().length() >= 3) {
                loadSearchedList();
            } else {
                Toast.makeText(this, "Enter atleast 3 letters.", Toast.LENGTH_SHORT)
                        .show();
            }
            inputManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
            return true;
        }
        return false;
    }

    @Override
    public void onClick(View v) {
        int btnId = v.getId();
        bmodel = (BusinessModel) getApplicationContext();
        bmodel.setContext(this);
        if (btnId == R.id.btn_search) {
            viewFlipper.showNext();
        } else if (btnId == R.id.btn_filter_popup) {
            AlertDialog.Builder builderSingle = new AlertDialog.Builder(
                    StockViewActivity.this);
            final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(
                    StockViewActivity.this,
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

        } else if (btnId == R.id.btn_clear) {
            viewFlipper.showPrevious();
            mEdt_searchproductName.setText("");
            /** set the following value to clear the **/
            mSelectedFilterMap.put("General", "All");
            mSelectedFilterMap.put("Brand", "All");
            mSelectedFilterMap.put("Category", "All");
            if (mSelectedFilterMap != null && mSelectedIdByLevelId != null) {
                mSelectedIdByLevelId.clear();
            }

            try {
                InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
            } catch (Exception e) {
                // TODO: handle exception
            }

            supportInvalidateOptionsMenu();
            updateGeneralText(GENERAL);
        }
    }

    private class ExpandableListAdapter extends BaseExpandableListAdapter {
        private Context context;
        private ArrayList<LoadManagementBO> listDataHeader; // header titles
        // child data in format of header title, child title
        private HashMap<String, ArrayList<LoadManagementBO>> listDataChild;


        ExpandableListAdapter(Context context, ArrayList<LoadManagementBO> listDataHeader,
                              HashMap<String, ArrayList<LoadManagementBO>> listChildData) {

            this.context = context;
            this.listDataHeader = listDataHeader;
            this.listDataChild = listChildData;


        }


        @Override
        public Object getChild(int groupPosition, int childPosition) {
            String keyPid = this.listDataHeader.get(groupPosition).getProductid() + "";
            return this.listDataChild.get(keyPid)
                    .get(childPosition);
        }


        @Override
        public long getChildId(int groupPosition, int childPosition) {
            return childPosition;
        }


        @Override
        public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {

            LoadManagementBO childBoObj;
            String tv;
            childBoObj = (LoadManagementBO) getChild(groupPosition, childPosition);
            final ViewHolder holder;
            View row = convertView;
            if (row == null) {

                LayoutInflater inflater = getLayoutInflater();
                row = inflater
                        .inflate(R.layout.custom_child_listitem, parent, false);
                holder = new ViewHolder();

                holder.batchNo = (TextView) row.findViewById(R.id.batch_no);
                holder.sihCase = (TextView) row.findViewById(R.id.sih_case);
                holder.sihOuter = (TextView) row.findViewById(R.id.sih_outer);
                holder.sih = (TextView) row.findViewById(R.id.sih);

                holder.batchNo.setTypeface(FontUtils.getFontRoboto(FontUtils.FontType.MEDIUM, StockViewActivity.this));
                holder.sihCase.setTypeface(FontUtils.getFontRoboto(FontUtils.FontType.LIGHT, StockViewActivity.this));
                holder.sihOuter.setTypeface(FontUtils.getFontRoboto(FontUtils.FontType.LIGHT, StockViewActivity.this));
                holder.sih.setTypeface(FontUtils.getFontRoboto(FontUtils.FontType.LIGHT, StockViewActivity.this));


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


                row.setTag(holder);
            } else {
                holder = (ViewHolder) row.getTag();
            }
            tv = getResources().getString(
                    R.string.batch_no)
                    + ": " + childBoObj.getBatchNo() + "";
            holder.batchNo.setText(tv);
            if (bmodel.configurationMasterHelper.CONVERT_STOCK_SIH_OU ||
                    bmodel.configurationMasterHelper.CONVERT_STOCK_SIH_CS ||
                    bmodel.configurationMasterHelper.CONVERT_STOCK_SIH_PS) {
                holder.sihCase.setVisibility(View.GONE);
                holder.sihOuter.setVisibility(View.GONE);
                if (bmodel.configurationMasterHelper.CONVERT_STOCK_SIH_OU) {
                    if (childBoObj.getOuterSize() != 0) {
                        tv = SDUtil.mathRoundoff((double) childBoObj.getStocksih() / childBoObj.getOuterSize()) + "";
                        holder.sih.setText(tv);
                    } else {
                        tv = childBoObj.getStocksih() + "";
                        holder.sih.setText(tv);
                    }
                } else if (bmodel.configurationMasterHelper.CONVERT_STOCK_SIH_CS) {
                    if (childBoObj.getCaseSize() != 0) {
                        tv = SDUtil.mathRoundoff((double) childBoObj.getStocksih() / childBoObj.getCaseSize()) + "";
                        holder.sih.setText(tv);
                    } else {
                        tv = childBoObj.getStocksih() + "";
                        holder.sih.setText(tv);
                    }
                } else if (bmodel.configurationMasterHelper.CONVERT_STOCK_SIH_PS) {
                    tv = childBoObj.getStocksih() + "";
                    holder.sih.setText(tv);
                }

            } else if (bmodel.configurationMasterHelper.SHOW_SIH_SPLIT) {
                if (bmodel.configurationMasterHelper.SHOW_ORDER_CASE
                        && bmodel.configurationMasterHelper.SHOW_OUTER_CASE
                        && bmodel.configurationMasterHelper.SHOW_ORDER_PCS) {
                    if (childBoObj.getStocksih() == 0) {
                        holder.sihCase.setText("0");
                        holder.sihOuter.setText("0");
                        holder.sih.setText("0");
                    } else if (childBoObj.getCaseSize() == 0) {
                        holder.sihCase.setText("0");
                        if (childBoObj.getOuterSize() == 0) {
                            holder.sihOuter.setText("0");
                            tv = childBoObj.getStocksih() + "";
                            holder.sih.setText(tv);
                        } else {
                            tv = childBoObj.getStocksih()
                                    / childBoObj.getOuterSize() + "";
                            holder.sihOuter.setText(tv);
                            tv = childBoObj.getStocksih()
                                    % childBoObj.getOuterSize() + "";
                            holder.sih.setText(tv);
                        }
                    } else {
                        tv = childBoObj.getStocksih()
                                / childBoObj.getCaseSize() + "";
                        holder.sihCase.setText(tv);
                        if (childBoObj.getOuterSize() > 0
                                && (childBoObj.getStocksih() % childBoObj.getCaseSize()) >= childBoObj
                                .getOuterSize()) {
                            tv = (childBoObj.getStocksih() % childBoObj
                                    .getCaseSize())
                                    / childBoObj.getOuterSize()
                                    + "";
                            holder.sihOuter.setText(tv);
                            tv = (childBoObj.getStocksih() % childBoObj
                                    .getCaseSize())
                                    % childBoObj.getOuterSize()
                                    + "";
                            holder.sih.setText(tv);
                        } else {
                            holder.sihOuter.setText("0");
                            tv = childBoObj.getStocksih()
                                    % childBoObj.getCaseSize() + "";
                            holder.sih.setText(tv);
                        }
                    }
                } else if (bmodel.configurationMasterHelper.SHOW_ORDER_CASE
                        && bmodel.configurationMasterHelper.SHOW_OUTER_CASE) {
                    if (childBoObj.getStocksih() == 0) {
                        holder.sihCase.setText("0");
                        holder.sihOuter.setText("0");
                    } else if (childBoObj.getCaseSize() == 0) {
                        holder.sihCase.setText("0");
                        if (childBoObj.getOuterSize() == 0) {
                            holder.sihOuter.setText("0");
                        } else {
                            tv = childBoObj.getStocksih()
                                    / childBoObj.getOuterSize() + "";
                            holder.sihOuter.setText(tv);
                        }
                    } else {
                        tv = childBoObj.getStocksih()
                                / childBoObj.getCaseSize() + "";
                        holder.sihCase.setText(tv);
                        if (childBoObj.getOuterSize() > 0
                                && (childBoObj.getStocksih() % childBoObj.getCaseSize()) >= childBoObj
                                .getOuterSize()) {
                            tv = (childBoObj.getStocksih() % childBoObj
                                    .getCaseSize())
                                    / childBoObj.getOuterSize()
                                    + "";
                            holder.sihOuter.setText(tv);
                        } else {
                            holder.sihOuter.setText("0");
                        }
                    }
                } else if (bmodel.configurationMasterHelper.SHOW_OUTER_CASE
                        && bmodel.configurationMasterHelper.SHOW_ORDER_PCS) {
                    if (childBoObj.getStocksih() == 0) {
                        holder.sih.setText("0");
                        holder.sihOuter.setText("0");
                    } else if (childBoObj.getOuterSize() == 0) {
                        tv = childBoObj.getStocksih() + "";
                        holder.sih.setText(tv);
                        holder.sihOuter.setText("0");
                    } else {
                        tv = childBoObj.getStocksih()
                                / childBoObj.getOuterSize() + "";
                        holder.sihOuter.setText(tv);
                        tv = childBoObj.getStocksih()
                                % childBoObj.getOuterSize() + "";
                        holder.sih.setText(tv);
                    }
                } else if (bmodel.configurationMasterHelper.SHOW_ORDER_CASE
                        && bmodel.configurationMasterHelper.SHOW_ORDER_PCS) {
                    if (childBoObj.getStocksih() == 0) {
                        holder.sih.setText("0");
                        holder.sihCase.setText("0");
                    } else if (childBoObj.getCaseSize() == 0) {
                        tv = childBoObj.getStocksih() + "";
                        holder.sih.setText(tv);
                        holder.sihCase.setText("0");
                    } else {
                        tv = childBoObj.getStocksih()
                                / childBoObj.getCaseSize() + "";
                        holder.sihCase.setText(tv);
                        tv = childBoObj.getStocksih()
                                % childBoObj.getCaseSize() + "";
                        holder.sih.setText(tv);
                    }
                } else if (bmodel.configurationMasterHelper.SHOW_ORDER_CASE) {
                    if (childBoObj.getStocksih() == 0) {
                        holder.sihCase.setText("0");
                    } else if (childBoObj.getCaseSize() == 0) {
                        holder.sihCase.setText("0");
                    } else {
                        tv = childBoObj.getStocksih()
                                / childBoObj.getCaseSize() + "";
                        holder.sihCase.setText(tv);
                    }
                } else if (bmodel.configurationMasterHelper.SHOW_OUTER_CASE) {
                    if (childBoObj.getStocksih() == 0) {
                        holder.sihOuter.setText("0");
                    } else if (childBoObj.getOuterSize() == 0) {
                        holder.sihOuter.setText("0");
                    } else {
                        tv = childBoObj.getStocksih()
                                / childBoObj.getOuterSize() + "";
                        holder.sihOuter.setText(tv);
                    }
                } else if (bmodel.configurationMasterHelper.SHOW_ORDER_PCS) {
                    tv = childBoObj.getStocksih() + "";
                    holder.sih.setText(tv);
                }
            } else {
                tv = childBoObj.getStocksih() + "";
                holder.sih.setText(tv);
            }

            return row;
        }


        @Override
        public int getGroupCount() {
            return this.listDataHeader.size();
        }

        @Override
        public int getChildrenCount(int groupPosition) {
            String proid = String.valueOf(this.listDataHeader.get(groupPosition).getProductid());
            return this.listDataChild.get(proid)
                    .size();
        }

        @Override
        public Object getGroup(int groupPosition) {
            return this.listDataHeader.get(groupPosition);
        }


        @Override
        public long getGroupId(int groupPosition) {
            return groupPosition;
        }

        @Override
        public boolean hasStableIds() {
            return false;
        }

        @Override
        public View getGroupView(final int groupPosition, final boolean isExpanded, final View convertView, final ViewGroup parent) {
            LoadManagementBO groupBoObj;
            String tv;
            groupBoObj = (LoadManagementBO) getGroup(groupPosition);
            final GroupViewHolder holder;
            View row = convertView;

            if (row == null) {
                LayoutInflater inflater = getLayoutInflater();
                row = inflater
                        .inflate(R.layout.row_stock_report, parent, false);
                holder = new GroupViewHolder();

                holder.psname = (TextView) row.findViewById(R.id.orderPRODNAME);
                holder.sihCase = (TextView) row.findViewById(R.id.sih_case);
                holder.sihOuter = (TextView) row.findViewById(R.id.sih_outer);
                holder.sih = (TextView) row.findViewById(R.id.sih);
                holder.prodcode = (TextView) row.findViewById(R.id.prdcode);


                holder.psname.setTypeface(FontUtils.getFontRoboto(FontUtils.FontType.MEDIUM, StockViewActivity.this));
                holder.prodcode.setTypeface(FontUtils.getFontRoboto(FontUtils.FontType.MEDIUM, StockViewActivity.this));
                holder.sihCase.setTypeface(FontUtils.getFontRoboto(FontUtils.FontType.LIGHT, StockViewActivity.this));
                holder.sihOuter.setTypeface(FontUtils.getFontRoboto(FontUtils.FontType.LIGHT, StockViewActivity.this));
                holder.sih.setTypeface(FontUtils.getFontRoboto(FontUtils.FontType.LIGHT, StockViewActivity.this));

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

                if (!bmodel.configurationMasterHelper.IS_SHOW_SKU_CODE)
                    holder.prodcode.setVisibility(View.GONE);


                row.setOnClickListener(new View.OnClickListener() {
                    public void onClick(View v) {
                        productName.setText(holder.pname);
                        if (viewFlipper.getDisplayedChild() != 0) {
                            viewFlipper.setInAnimation(StockViewActivity.this,
                                    R.anim.in_from_left);
                            viewFlipper.setOutAnimation(StockViewActivity.this,
                                    R.anim.out_to_left);
                            viewFlipper.showPrevious();
                        }
                    }
                });
                row.setTag(holder);
            } else {
                holder = (GroupViewHolder) row.getTag();
            }

            holder.psname.setText(groupBoObj.getProductshortname());
            holder.pname = groupBoObj.getProductname();
            if (bmodel.configurationMasterHelper.IS_SHOW_SKU_CODE) {
                String prodCode = getResources().getString(R.string.prod_code) + ": " +
                        groupBoObj.getProductCode() + " ";
                holder.prodcode.setText(prodCode);
            }

            if (bmodel.configurationMasterHelper.CONVERT_STOCK_SIH_OU ||
                    bmodel.configurationMasterHelper.CONVERT_STOCK_SIH_CS ||
                    bmodel.configurationMasterHelper.CONVERT_STOCK_SIH_PS) {
                holder.sihCase.setVisibility(View.GONE);
                holder.sihOuter.setVisibility(View.GONE);
                if (bmodel.configurationMasterHelper.CONVERT_STOCK_SIH_OU) {
                    if (groupBoObj.getOuterSize() != 0) {
                        tv = SDUtil.mathRoundoff((double) groupBoObj.getStocksih() / groupBoObj.getOuterSize()) + "";
                        holder.sih.setText(tv);
                    } else {
                        tv = groupBoObj.getStocksih() + "";
                        holder.sih.setText(tv);

                    }
                } else if (bmodel.configurationMasterHelper.CONVERT_STOCK_SIH_CS) {
                    if (groupBoObj.getCaseSize() != 0) {
                        tv = SDUtil.mathRoundoff((double) groupBoObj.getStocksih() / groupBoObj.getCaseSize()) + "";
                        holder.sih.setText(tv);
                    } else {
                        tv = groupBoObj.getStocksih() + "";
                        holder.sih.setText(tv);

                    }
                } else {
                    tv = groupBoObj.getStocksih() + "";
                    holder.sih.setText(tv);

                }
            } else if (bmodel.configurationMasterHelper.SHOW_SIH_SPLIT) {
                if (bmodel.configurationMasterHelper.SHOW_ORDER_CASE
                        && bmodel.configurationMasterHelper.SHOW_OUTER_CASE
                        && bmodel.configurationMasterHelper.SHOW_ORDER_PCS) {
                    if (groupBoObj.getSih() == 0) {
                        holder.sihCase.setText("0");
                        holder.sihOuter.setText("0");
                        holder.sih.setText("0");
                    } else if (groupBoObj.getCaseSize() == 0) {
                        holder.sihCase.setText("0");
                        if (groupBoObj.getOuterSize() == 0) {
                            holder.sihOuter.setText("0");
                            tv = groupBoObj.getSih() + "";
                            holder.sih.setText(tv);
                        } else {
                            tv = groupBoObj.getSih()
                                    / groupBoObj.getOuterSize() + "";
                            holder.sihOuter.setText(tv);
                            tv = groupBoObj.getSih()
                                    % groupBoObj.getOuterSize() + "";
                            holder.sih.setText(tv);
                        }
                    } else {
                        tv = groupBoObj.getSih()
                                / groupBoObj.getCaseSize() + "";
                        holder.sihCase.setText(tv);
                        if (groupBoObj.getOuterSize() > 0
                                && (groupBoObj.getSih() % groupBoObj.getCaseSize()) >= groupBoObj
                                .getOuterSize()) {
                            tv = (groupBoObj.getSih() % groupBoObj
                                    .getCaseSize())
                                    / groupBoObj.getOuterSize()
                                    + "";
                            holder.sihOuter.setText(tv);
                            tv = (groupBoObj.getSih() % groupBoObj
                                    .getCaseSize())
                                    % groupBoObj.getOuterSize()
                                    + "";
                            holder.sih.setText(tv);
                        } else {
                            holder.sihOuter.setText("0");
                            tv = groupBoObj.getSih()
                                    % groupBoObj.getCaseSize() + "";
                            holder.sih.setText(tv);
                        }
                    }
                } else if (bmodel.configurationMasterHelper.SHOW_ORDER_CASE
                        && bmodel.configurationMasterHelper.SHOW_OUTER_CASE) {
                    if (groupBoObj.getSih() == 0) {
                        holder.sihCase.setText("0");
                        holder.sihOuter.setText("0");
                    } else if (groupBoObj.getCaseSize() == 0) {
                        holder.sihCase.setText("0");
                        if (groupBoObj.getOuterSize() == 0) {
                            holder.sihOuter.setText("0");
                        } else {
                            tv = groupBoObj.getSih()
                                    / groupBoObj.getOuterSize() + "";
                            holder.sihOuter.setText(tv);
                        }
                    } else {
                        tv = groupBoObj.getSih()
                                / groupBoObj.getCaseSize() + "";
                        holder.sihCase.setText(tv);
                        if (groupBoObj.getOuterSize() > 0
                                && (groupBoObj.getSih() % groupBoObj.getCaseSize()) >= groupBoObj
                                .getOuterSize()) {
                            tv = (groupBoObj.getSih() % groupBoObj
                                    .getCaseSize())
                                    / groupBoObj.getOuterSize()
                                    + "";
                            holder.sihOuter.setText(tv);
                        } else {
                            holder.sihOuter.setText("0");
                        }
                    }
                } else if (bmodel.configurationMasterHelper.SHOW_OUTER_CASE
                        && bmodel.configurationMasterHelper.SHOW_ORDER_PCS) {
                    if (groupBoObj.getSih() == 0) {
                        holder.sih.setText("0");
                        holder.sihOuter.setText("0");
                    } else if (groupBoObj.getOuterSize() == 0) {
                        tv = groupBoObj.getSih() + "";
                        holder.sih.setText(tv);
                        holder.sihOuter.setText("0");
                    } else {
                        tv = groupBoObj.getSih()
                                / groupBoObj.getOuterSize() + "";
                        holder.sihOuter.setText(tv);
                        tv = groupBoObj.getSih()
                                % groupBoObj.getOuterSize() + "";
                        holder.sih.setText(tv);
                    }
                } else if (bmodel.configurationMasterHelper.SHOW_ORDER_CASE
                        && bmodel.configurationMasterHelper.SHOW_ORDER_PCS) {
                    if (groupBoObj.getSih() == 0) {
                        holder.sih.setText("0");
                        holder.sihCase.setText("0");
                    } else if (groupBoObj.getCaseSize() == 0) {
                        tv = groupBoObj.getSih() + "";
                        holder.sih.setText(tv);
                        holder.sihCase.setText("0");
                    } else {
                        tv = groupBoObj.getSih()
                                / groupBoObj.getCaseSize() + "";
                        holder.sihCase.setText(tv);
                        tv = groupBoObj.getSih()
                                % groupBoObj.getCaseSize() + "";
                        holder.sih.setText(tv);
                    }
                } else if (bmodel.configurationMasterHelper.SHOW_ORDER_CASE) {
                    if (groupBoObj.getSih() == 0) {
                        holder.sihCase.setText("0");
                    } else if (groupBoObj.getCaseSize() == 0) {
                        holder.sihCase.setText("0");
                    } else {
                        tv = groupBoObj.getSih()
                                / groupBoObj.getCaseSize() + "";
                        holder.sihCase.setText(tv);
                    }
                } else if (bmodel.configurationMasterHelper.SHOW_OUTER_CASE) {
                    if (groupBoObj.getSih() == 0) {
                        holder.sihOuter.setText("0");
                    } else if (groupBoObj.getOuterSize() == 0) {
                        holder.sihOuter.setText("0");
                    } else {
                        tv = groupBoObj.getSih()
                                / groupBoObj.getOuterSize() + "";
                        holder.sihOuter.setText(tv);
                    }
                } else if (bmodel.configurationMasterHelper.SHOW_ORDER_PCS) {
                    tv = groupBoObj.getSih() + "";
                    holder.sih.setText(tv);
                }
            } else {
                tv = groupBoObj.getSih() + "";
                holder.sih.setText(tv);
            }


            return row;
        }


        @Override
        public boolean isChildSelectable(int groupPosition, int childPosition) {
            return false;
        }


    }


    class GroupViewHolder {
        private String pname;
        private TextView psname;
        private TextView sih;
        private TextView sihCase;
        private TextView sihOuter;
        private TextView prodcode;
    }


    class ViewHolder {
        private TextView sih;
        private TextView sihCase;
        private TextView sihOuter;
        private TextView batchNo;

    }

    private void showExpandButton() {
        expand_collapse_button_enable = true;
    }

    private void FiveFilterFragment() {
        try {

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
}
