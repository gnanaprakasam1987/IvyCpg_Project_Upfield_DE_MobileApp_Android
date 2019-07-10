package com.ivy.cpg.view.van.vanunload;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.ViewFlipper;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.bo.LoadManagementBO;
import com.ivy.sd.png.commons.IvyBaseActivityNoActionBar;
import com.ivy.sd.png.model.BrandDialogInterface;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.model.FiveLevelFilterCallBack;
import com.ivy.sd.png.util.Commons;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Vector;

public class VanUnloadActivity extends IvyBaseActivityNoActionBar implements
        BrandDialogInterface, OnClickListener, TextView.OnEditorActionListener, FiveLevelFilterCallBack {
    private static final String BRAND = "Brand";
    private static final String GENERAL = "General";
    private HashMap<Integer, Integer> mSelectedIdByLevelId;
    private DrawerLayout mDrawerLayout;
    private ListView lvwplist;
    private EditText mEdt_searchproductName;
    private TextView productName;
    private ViewFlipper viewFlipper;
    private HashMap<String, String> mSelectedFilterMap = new HashMap<>();
    private ArrayList<String> mSearchTypeArray = new ArrayList<>();
    private InputMethodManager inputManager;
    private VanUnloadAdaptor mSchedule;
    private Vector<LoadManagementBO> vanunloadlist;
    private VanUnLoadModuleHelper mVanUnLoadModuleHelper;
    private BusinessModel bModel;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final Intent i = getIntent();

        setContentView(R.layout.activity_van_unload);
        overridePendingTransition(R.anim.trans_left_in, R.anim.trans_left_out);
        bModel = (BusinessModel) getApplicationContext();
        bModel.setContext(this);
        mVanUnLoadModuleHelper = VanUnLoadModuleHelper.getInstance(this);
        Toolbar toolbar = findViewById(R.id.toolbar);

        inputManager = (InputMethodManager) this
                .getSystemService(Context.INPUT_METHOD_SERVICE);

        if (toolbar != null) {
            setSupportActionBar(toolbar);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            setScreenTitle(i.getStringExtra("screentitle"));
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setIcon(null);
        }

        initializeView();

        ActionBarDrawerToggle mDrawerToggle = new ActionBarDrawerToggle(VanUnloadActivity.this,
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
                    setScreenTitle("Filter");
                }
                supportInvalidateOptionsMenu();
            }
        };

        mDrawerLayout.addDrawerListener(mDrawerToggle);

        vanunloadlist = new Vector<>();
        for (LoadManagementBO bo : bModel.productHelper.getLoadMgmtProducts()) {
            if (bo.getSih() > 0)
                vanunloadlist.add(bo);
        }

        try {
            if (bModel.userMasterHelper.getUserMasterBO().getUserid() == 0) {
                showMessage(getString(R.string.sessionout_loginagain));
                finish();
            }
        } catch (Exception e) {
            Commons.printException("" + e);
            showMessage(getString(R.string.sessionout_loginagain));
            finish();
        }


        supportInvalidateOptionsMenu();
        updateBrandText("Brand", -1);

        mSearchTypeArray = new ArrayList<>();
        mSearchTypeArray.add(getResources().getString(R.string.all));
        mSearchTypeArray.add(getResources().getString(R.string.product_name));
        mSearchTypeArray.add(getResources().getString(R.string.prod_code));
        mSearchTypeArray.add(getResources().getString(
                R.string.order_dialog_barcode));


    }


    private void initializeView() {
        lvwplist =  findViewById(R.id.list);
        lvwplist.setCacheColorHint(0);

        Button saveBtn =  findViewById(R.id.van_btn_save);
        viewFlipper =  findViewById(R.id.view_flipper);
        mEdt_searchproductName =  findViewById(R.id.edt_searchproductName);
        Button mBtnSearch =  findViewById(R.id.btn_search);
        Button mBtnFilterPopup =  findViewById(R.id.btn_filter_popup);
        Button mBtnClear =  findViewById(R.id.btn_clear);
        productName =  findViewById(R.id.productName);


        saveBtn.setOnClickListener(this);
        mBtnSearch.setOnClickListener(this);
        mBtnFilterPopup.setOnClickListener(this);
        mBtnClear.setOnClickListener(this);
        mEdt_searchproductName.setOnEditorActionListener(this);


        if (!bModel.configurationMasterHelper.SHOW_NON_SALABLE_UNLOAD)
            (findViewById(R.id.tv_nonsalable_title)).setVisibility(View.GONE);

        mDrawerLayout = findViewById(R.id.drawer_layout);
        mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow,
                GravityCompat.START);
        mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow,
                GravityCompat.END);
        mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);

        FrameLayout drawer = findViewById(R.id.right_drawer);

        int width = getResources().getDisplayMetrics().widthPixels;
        DrawerLayout.LayoutParams params = (DrawerLayout.LayoutParams) drawer.getLayoutParams();
        params.width = width;
        drawer.setLayoutParams(params);

        // labels update for text view
        try {
            if (bModel.labelsMasterHelper.applyLabels(findViewById(
                    R.id.sihTitle).getTag()) != null)
                ((TextView) findViewById(R.id.sihTitle))
                        .setText(bModel.labelsMasterHelper
                                .applyLabels(findViewById(R.id.sihTitle)
                                        .getTag()));
        } catch (Exception e) {
            Commons.printException("" + e);
        }


        if (!bModel.configurationMasterHelper.SHOW_ORDER_CASE) {
            findViewById(R.id.itemcasetitle).setVisibility(View.GONE);
            (findViewById(R.id.ll_vanloadcase_lty)).setVisibility(View.GONE);
        } else {
            try {
                if (bModel.labelsMasterHelper.applyLabels(findViewById(
                        R.id.itemcasetitle).getTag()) != null)
                    ((TextView) findViewById(R.id.itemcasetitle))
                            .setText(bModel.labelsMasterHelper
                                    .applyLabels(findViewById(
                                            R.id.itemcasetitle).getTag()));

            } catch (Exception e) {
                Commons.printException("" + e);
            }
        }
        if (!bModel.configurationMasterHelper.SHOW_ORDER_PCS) {
            findViewById(R.id.itempiecetitle).setVisibility(View.GONE);
            (findViewById(R.id.ll_vanloadpiece_lty)).setVisibility(View.GONE);
        } else {
            try {
                if (bModel.labelsMasterHelper.applyLabels(findViewById(
                        R.id.itempiecetitle).getTag()) != null)
                    ((TextView) findViewById(R.id.itempiecetitle))
                            .setText(bModel.labelsMasterHelper
                                    .applyLabels(findViewById(
                                            R.id.itempiecetitle).getTag()));
            } catch (Exception e) {
                Commons.printException("" + e);
            }
        }

        if (!bModel.configurationMasterHelper.SHOW_OUTER_CASE) {
            findViewById(R.id.outeritemcasetitle).setVisibility(View.GONE);
            (findViewById(R.id.ll_vanloadouter_lty)).setVisibility(View.GONE);
        } else {
            try {
                if (bModel.labelsMasterHelper.applyLabels(findViewById(
                        R.id.outeritemcasetitle).getTag()) != null)
                    ((TextView) findViewById(R.id.outeritemcasetitle))
                            .setText(bModel.labelsMasterHelper
                                    .applyLabels(findViewById(
                                            R.id.outeritemcasetitle).getTag()));
            } catch (Exception e) {
                Commons.printException("" + e);
            }
        }

        mEdt_searchproductName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() >= 3) {
                    loadSearchedList();
                }
            }
        });

    }


    public void loadProductList() {
        updateGeneralText(GENERAL);
    }

    public void onBackButtonClick() {
        bModel.moduleTimeStampHelper.saveModuleTimeStamp("Out");
        bModel.moduleTimeStampHelper.setTid("");
        bModel.moduleTimeStampHelper.setModuleCode("");
        finish();
        overridePendingTransition(R.anim.trans_right_in, R.anim.trans_right_out);
    }

    @Override
    public void updateBrandText(String mFilterText, int bid) {

        mDrawerLayout.closeDrawers();


        productName.setText("");
        if (vanunloadlist == null) {
            bModel.showAlert(
                    getResources().getString(R.string.no_products_exists), 0);
            return;
        }
        int siz = vanunloadlist.size();
        ArrayList<LoadManagementBO> list = new ArrayList<>();

        for (int i = 0; i < siz; ++i) {
            LoadManagementBO ret = vanunloadlist
                    .elementAt(i);
            if (bid == ret.getParentid() || bid == -1) {

                list.add(ret);
            }
        }

        refreshList(list);
        updateTotQtyDetails(list);

    }

    @Override
    public void updateGeneralText(String filterCode) {
        updateBrandText(BRAND, -1);
    }

    @Override
    public void updateCancel() {

    }

    public void onNextButtonClick() {
        if (mVanUnLoadModuleHelper.hasVanunload()) {
            new SaveVanUnloadAsyncTask(VanUnloadActivity.this, vanunloadlist, bModel).execute();

        } else {
            bModel.showAlert(
                    getResources().getString(R.string.no_products_exists), 0);
        }
    }

    public void loadSearchedList() {
        if (mEdt_searchproductName.getText().length() >= 3) {
            Vector<LoadManagementBO> items = bModel.productHelper.getLoadMgmtProducts();

            if (items == null) {
                bModel.showAlert(
                        getResources().getString(R.string.no_products_exists),
                        0);
                return;
            }
            int siz = items.size();
            ArrayList<LoadManagementBO> mylist = new ArrayList<>();
            String mSelectedFilter = bModel.getProductFilter();
            for (int i = 0; i < siz; ++i) {
                LoadManagementBO ret = items.elementAt(i);
                if (ret.getSih() > 0) {
                    if (getResources().getString(
                            R.string.order_dialog_barcode).equals(mSelectedFilter)) {
                        if (ret.getBarcode() != null && ret.getBarcode()
                                .toLowerCase()
                                .contains(
                                        mEdt_searchproductName.getText().toString()
                                                .toLowerCase()))
                            mylist.add(ret);

                    } else if (getResources().getString(R.string.prod_code).equals(mSelectedFilter)) {
                        if ((ret.getRField1() != null && ret.getRField1()
                                .toLowerCase()
                                .contains(
                                        mEdt_searchproductName.getText().toString()
                                                .toLowerCase())) || (ret.getProductCode() != null
                                && ret.getProductCode().toLowerCase().contains(mEdt_searchproductName.getText().toString()
                                .toLowerCase())))
                            mylist.add(ret);

                    } else if (getResources().getString(
                            R.string.product_name).equals(mSelectedFilter)) {
                        if (ret.getProductshortname() != null && ret.getProductshortname()
                                .toLowerCase()
                                .contains(
                                        mEdt_searchproductName.getText().toString()
                                                .toLowerCase()))
                            mylist.add(ret);
                    } else {
                        if (ret.getBarcode() != null && ret.getBarcode()
                                .toLowerCase()
                                .contains(
                                        mEdt_searchproductName.getText().toString()
                                                .toLowerCase()))
                            mylist.add(ret);
                        else if ((ret.getRField1() != null && ret.getRField1()
                                .toLowerCase()
                                .contains(
                                        mEdt_searchproductName.getText().toString()
                                                .toLowerCase())) || (ret.getProductCode() != null
                                && ret.getProductCode().toLowerCase().contains(mEdt_searchproductName.getText().toString()
                                .toLowerCase())))
                            mylist.add(ret);
                        else if (ret.getProductshortname() != null && ret.getProductshortname()
                                .toLowerCase()
                                .contains(
                                        mEdt_searchproductName.getText().toString()
                                                .toLowerCase()))
                            mylist.add(ret);
                    }
                }
            }

            refreshList(mylist);

        } else {
            showMessage(getString(R.string.enter_atleast_three_letters));
        }
    }


    @Override
    public void updateFromFiveLevelFilter(int mFilteredPid, HashMap<Integer, Integer> mSelectedIdByLevelId, ArrayList<Integer> mAttributeProducts, String mFilterText) {

        ArrayList<LoadManagementBO> filterList = new ArrayList<>();
        if (mAttributeProducts != null) {
            if (mFilteredPid != 0) {
                for (LoadManagementBO productBO : vanunloadlist) {
                    if (productBO.getParentHierarchy().contains("/" + mFilteredPid + "/")) {
                        // here we get all products mapped to parent id list, then that product will be added only if it is mapped to selected attribute
                        if (mAttributeProducts.contains(productBO.getProductid())) {
                            filterList.add(productBO);
                        }
                    }
                }
            } else {
                for (int pid : mAttributeProducts) {
                    for (LoadManagementBO productBO : vanunloadlist) {
                        if (pid == productBO.getProductid()) {
                            filterList.add(productBO);
                        }
                    }
                }
            }
        } else {
            if (mFilteredPid != 0 && !mFilterText.equalsIgnoreCase("")) {
                for (LoadManagementBO productBO : vanunloadlist) {
                    if (productBO.getParentHierarchy().contains("/" + mFilteredPid + "/")) {
                        filterList.add(productBO);
                    }
                }
            } else {
                int bid = -1;
                for (LoadManagementBO productBO : vanunloadlist) {
                    if (bid == productBO.getParentid() || bid == -1) {

                        filterList.add(productBO);
                    }
                }

            }
        }
        refreshList(filterList);
        updateTotQtyDetails(filterList);

        mDrawerLayout.closeDrawers();
        if (mSelectedIdByLevelId != null)
            this.mSelectedIdByLevelId = mSelectedIdByLevelId;
    }

    /**
     * Refresh list view
     */
    private void refreshList(ArrayList<LoadManagementBO> finalList) {
        if (mSchedule == null) {
            mSchedule = new VanUnloadAdaptor(finalList, VanUnloadActivity.this, vanUnloadInterface);
            lvwplist.setAdapter(mSchedule);
        } else {
            mSchedule.setListData(finalList);
            mSchedule.notifyDataSetChanged();
        }
    }

    private void applySIH() {
        LoadManagementBO vanunloadbo;
        for (int i = 0; i < vanunloadlist.size(); i++) {
            vanunloadbo = vanunloadlist.get(i);
            vanunloadbo.setPieceqty(vanunloadbo.getStocksih());
        }
        mSchedule.notifyDataSetChanged();
    }

    private void updateTotQtyDetails(ArrayList<LoadManagementBO> filterList) {

        TextView mTotalSihTV =  findViewById(R.id.tv_unload_sih);
        TextView mTotalCaseTV =  findViewById(R.id.tv_unload_total_case);
        TextView mTotalOuterTV =  findViewById(R.id.tv_unload_total_outer);
        TextView mTotalPcsTV =  findViewById(R.id.tv_unload_total_piece);

        int totalSih = 0;
        int totalPiece = 0;
        int totalCase = 0;
        int totelOuter = 0;
        String tv;

        for (LoadManagementBO loadManagementBO : filterList) {
            totalSih = totalSih + loadManagementBO.getStocksih();
            totalPiece = totalPiece + loadManagementBO.getPieceqty();
            totalCase = totalCase + loadManagementBO.getCaseqty();
            totelOuter = totelOuter + loadManagementBO.getOuterQty();
        }
        tv = totalSih + "";
        mTotalSihTV.setText(tv);
        tv = totalPiece + "";
        mTotalPcsTV.setText(tv);
        tv = totalCase + "";
        mTotalCaseTV.setText(tv);
        tv = totelOuter + "";
        mTotalOuterTV.setText(tv);

    }

    @Override
    public void onClick(View v) {

        int btnId = v.getId();
        bModel = (BusinessModel) getApplicationContext();
        bModel.setContext(this);
        if (btnId == R.id.btn_search) {
            viewFlipper.showNext();
        } else if (btnId == R.id.btn_filter_popup) {
            AlertDialog.Builder builderSingle = new AlertDialog.Builder(
                    VanUnloadActivity.this);
            final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(
                    VanUnloadActivity.this,
                    android.R.layout.select_dialog_singlechoice,
                    mSearchTypeArray);
            builderSingle.setAdapter(arrayAdapter,
                    new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            bModel.setProductFilter(arrayAdapter.getItem(which));
                        }
                    });
            int selectedFiltPos = mSearchTypeArray.indexOf(bModel
                    .getProductFilter());
            builderSingle.setSingleChoiceItems(arrayAdapter, selectedFiltPos,
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            bModel.setProductFilter(arrayAdapter.getItem(which));
                        }

                    });
            builderSingle.setPositiveButton(
                    getResources().getString(R.string.ok),
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog,
                                            int whichButton) {
                        }
                    });
            bModel.applyAlertDialogTheme(builderSingle);

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
        } else if (btnId == R.id.van_btn_save) {
            onNextButtonClick();
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_vanload, menu);
        return super.onCreateOptionsMenu(menu);
    }

    // Called whenever we call invalidateOptionsMenu()
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
        // If the nav drawer is open, hide action items related to the content
        // view
        boolean drawerOpen = mDrawerLayout.isDrawerOpen(GravityCompat.END);
        menu.findItem(R.id.menu_fivefilter).setVisible(!drawerOpen);
        menu.findItem(R.id.menu_barcode).setVisible(!drawerOpen);

        menu.findItem(R.id.menu_fivefilter).setVisible(false);
        menu.findItem(R.id.menu_loc_filter).setVisible(false);
        menu.findItem(R.id.menu_bottle_return).setVisible(false);


        menu.findItem(R.id.menu_fivefilter).setVisible(true);
        menu.findItem(R.id.menu_sih_apply).setVisible(true);
        menu.findItem(R.id.menu_unload_history).setVisible(true);


        if (bModel.configurationMasterHelper.IS_BAR_CODE_VAN_UNLOAD) {
            menu.findItem(R.id.menu_barcode).setVisible(true);
        } else {
            menu.findItem(R.id.menu_barcode).setVisible(false);
        }

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
        } else if (i == R.id.menu_fivefilter) {
            if (inputManager.isAcceptingText())// hide soft key board after select expend menu
                inputManager.hideSoftInputFromWindow(
                        mEdt_searchproductName.getWindowToken(), 0);

            Bundle bundle = new Bundle();
            bundle.putSerializable("serilizeContent", bModel.configurationMasterHelper.getGenFilter());
            bundle.putString("isFrom", "STK");
            bundle.putSerializable("selectedFilter", mSelectedIdByLevelId);


            mDrawerLayout.openDrawer(GravityCompat.END);

            loadFiveFilterFragment(bundle, R.id.right_drawer);

            return true;
        } else if (i == R.id.menu_sih_apply) {
            applySIH();
            return true;
        } else if (i == R.id.menu_barcode) {
            this.checkAndRequestPermissionAtRunTime(2);
            int permissionStatus = ContextCompat.checkSelfPermission(this,
                    Manifest.permission.CAMERA);
            if (permissionStatus == PackageManager.PERMISSION_GRANTED) {
                IntentIntegrator integrator = new IntentIntegrator(this) {
                    @Override
                    protected void startActivityForResult(Intent intent, int code) {
                        this.startActivityForResult(intent, IntentIntegrator.REQUEST_CODE); // REQUEST_CODE override
                    }
                };
                integrator.setBeepEnabled(false).initiateScan();
            } else {
                showMessage(getString(R.string.permission_enable_msg)
                        + " " + getString(R.string.permission_camera));
            }
            return true;
        } else if (i == R.id.menu_unload_history) {
            if (mVanUnLoadModuleHelper.getUnloadHistoryList().size() > 0) {
                Intent intent = new Intent(VanUnloadActivity.this, VanUnloadHistoryActivity.class);
                startActivity(intent);
            } else {
                showMessage(getString(R.string.data_not_mapped));
            }
        }
        return super.onOptionsItemSelected(item);
    }

    private String strBarCodeSearch = "";


    public void numberPressed(View vw) {
        if (mSchedule != null)
            mSchedule.numberPressed(vw);

    }

    VanUnloadInterface vanUnloadInterface = new VanUnloadInterface() {
        @Override
        public void setProductName(String pName) {
            productName.setText(pName);
        }

        @Override
        public void hideViewFlipper() {
            if (viewFlipper.getDisplayedChild() != 0) {
                viewFlipper.setInAnimation(VanUnloadActivity.this,
                        R.anim.in_from_left);
                viewFlipper.setOutAnimation(VanUnloadActivity.this,
                        R.anim.out_to_left);
                viewFlipper.showPrevious();
            }
        }

        @Override
        public void hideKeyboard() {
            inputManager.hideSoftInputFromWindow(
                    mEdt_searchproductName.getWindowToken(), 0);
        }

        @Override
        public void updateTotalQtyDetails(ArrayList<LoadManagementBO> filterList) {
            updateTotQtyDetails(filterList);
        }
    };


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (result != null) {
            if (result.getContents() == null) {
                showMessage(getString(R.string.cancelled));
            } else {
                strBarCodeSearch = result.getContents();
                if (strBarCodeSearch != null && !"".equals(strBarCodeSearch)) {
                    bModel.setProductFilter(getResources().getString(R.string.order_dialog_barcode));
                    mEdt_searchproductName.setText(strBarCodeSearch);
                    if (viewFlipper.getDisplayedChild() == 0) {
                        viewFlipper.showNext();
                    }
                }
            }
        } else {
            showMessage(getString(R.string.no_match_found));
        }
    }


}
