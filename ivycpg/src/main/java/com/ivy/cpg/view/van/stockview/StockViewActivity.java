package com.ivy.cpg.view.van.stockview;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
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
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ExpandableListView;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;
import android.widget.ViewFlipper;

import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.bo.LoadManagementBO;
import com.ivy.sd.png.commons.IvyBaseActivityNoActionBar;
import com.ivy.sd.png.model.BrandDialogInterface;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.model.FiveLevelFilterCallBack;
import com.ivy.sd.png.util.Commons;

import java.util.ArrayList;
import java.util.HashMap;

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


        Toolbar toolbar = findViewById(R.id.toolbar);

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

        FrameLayout drawer = findViewById(R.id.right_drawer);

        int width = getResources().getDisplayMetrics().widthPixels;
        DrawerLayout.LayoutParams params = (DrawerLayout.LayoutParams) drawer.getLayoutParams();
        params.width = width;
        drawer.setLayoutParams(params);


        viewFlipper = findViewById(R.id.view_flipper);

        mEdt_searchproductName = findViewById(R.id.edt_searchproductName);
        Button mBtn_Search = findViewById(R.id.btn_search);
        Button mBtnFilterPopup = findViewById(R.id.btn_filter_popup);
        Button mBtn_clear = findViewById(R.id.btn_clear);

        mBtn_Search.setOnClickListener(this);
        mBtnFilterPopup.setOnClickListener(this);
        mBtn_clear.setOnClickListener(this);
        mEdt_searchproductName.setOnEditorActionListener(this);

        expandlvwplist = findViewById(R.id.expand_lvwplist);
        expandlvwplist.setCacheColorHint(0);

        productName = findViewById(R.id.productName);
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
        mSearchTypeArray.add(getResources().getString(R.string.all));
        mSearchTypeArray.add(getResources().getString(R.string.product_name));
        mSearchTypeArray.add(getResources().getString(R.string.prod_code));
        mSearchTypeArray.add(getResources().getString(
                R.string.order_dialog_barcode));

      /*  Vector<String> vect = new Vector<>();
        vect.addAll(Arrays.asList(getResources().getStringArray(
                R.array.productFilterArray)));*/
        mSelectedFilterMap.put("General", GENERAL);

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

        // It is just a view screen, so updating once the screen is visited once
        bmodel.saveModuleCompletion("MENU_STOCK_VIEW", false);
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

            Bundle bundle = new Bundle();
            bundle.putSerializable("serilizeContent",
                    bmodel.configurationMasterHelper.getGenFilter());
            bundle.putString("isFrom", "STK");
            bundle.putSerializable("selectedFilter", mSelectedIdByLevelId);

            mDrawerLayout.openDrawer(GravityCompat.END);
            loadFiveFilterFragment(bundle, R.id.right_drawer);

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
            childList = new ArrayList<>();
            for (LoadManagementBO childBO : temp2) {
                if (parentBo.getProductid() == childBO.getProductid()
                        && childBO.getBatchlist() != null
                        && !childBO.getBatchId().isEmpty()
                        && !childBO.getBatchId().equals("0"))
                    childList.add(childBO);
            }
            String pid = String.valueOf(parentBo.getProductid());
            if (parentBo.getIsFree() == 1) //  if free product to have unique key
                pid = pid + "F";
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
                if (temp.get(i).getProductid() == temp.get(j).getProductid() &&
                        temp.get(i).getIsFree() == temp.get(j).getIsFree()) {
                    temp.remove(j);
                    j--;
                }
            }
        }


        refreshView(temp, listDataChild);

    }

    private void refreshView(ArrayList<LoadManagementBO> temp, HashMap<String, ArrayList<LoadManagementBO>> listDataChild) {
        expandableListAdapter = new ExpandableListAdapter(StockViewActivity.this, temp, listDataChild, stockViewInterface);
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
            if (getResources().getString(
                    R.string.order_dialog_barcode).equals(mSelectedFilter)) {
                if (ret.getSih() > 0) {
                    if (ret.getBarcode()
                            .toLowerCase()
                            .contains(
                                    mEdt_searchproductName.getText()
                                            .toString().toLowerCase()))
                        temp.add(ret);
                }

            } else if (getResources().getString(
                    R.string.prod_code).equals(mSelectedFilter)) {
                if (ret.getSih() > 0) {
                    if (ret.getRField1()
                            .toLowerCase()
                            .contains(
                                    mEdt_searchproductName.getText()
                                            .toString().toLowerCase()) || (ret.getProductCode() != null &&
                            ret.getProductCode().toLowerCase().contains(mEdt_searchproductName.getText().toString()
                                    .toLowerCase())))
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
            } else {
                if (ret.getSih() > 0) {
                    if (ret.getBarcode()
                            .toLowerCase()
                            .contains(
                                    mEdt_searchproductName.getText()
                                            .toString().toLowerCase()))
                        temp.add(ret);
                    else if (ret.getRField1()
                            .toLowerCase()
                            .contains(
                                    mEdt_searchproductName.getText()
                                            .toString().toLowerCase()) || (ret.getProductCode() != null &&
                            ret.getProductCode().toLowerCase().contains(mEdt_searchproductName.getText().toString()
                                    .toLowerCase())))
                        temp.add(ret);
                    else if (ret.getProductshortname()
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
                if (temp.get(i).getProductid() == temp.get(j).getProductid() && temp.get(i).getIsFree() == temp.get(j).getIsFree()) {
                    temp.remove(j);
                    j--;
                }
            }
        }

        refreshView(temp, listDataChild);
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
                if (filterlist.get(i).getProductid() == filterlist.get(j).getProductid() &&
                        filterlist.get(i).getIsFree() == filterlist.get(j).getIsFree()) {
                    filterlist.remove(j);
                    j--;
                }
            }
        }

        refreshView(filterlist, listDataChild);

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

    private void showExpandButton() {
        expand_collapse_button_enable = true;
    }

    StockViewInterface stockViewInterface = new StockViewInterface() {
        @Override
        public void onRowClick(String pName) {
            productName.setText(pName);
            if (viewFlipper.getDisplayedChild() != 0) {
                viewFlipper.setInAnimation(StockViewActivity.this,
                        R.anim.in_from_left);
                viewFlipper.setOutAnimation(StockViewActivity.this,
                        R.anim.out_to_left);
                viewFlipper.showPrevious();
            }
        }
    };

}
