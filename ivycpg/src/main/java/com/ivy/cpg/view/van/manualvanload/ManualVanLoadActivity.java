package com.ivy.cpg.view.van.manualvanload;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import androidx.annotation.NonNull;
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
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.ViewFlipper;

import com.ivy.cpg.view.van.LoadManagementHelper;
import com.ivy.cpg.view.van.manualvanload.manualvanloadbatchentrydialog.ManualVanLoadBatchEntryDialog;
import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.bo.LoadManagementBO;
import com.ivy.sd.png.bo.SubDepotBo;
import com.ivy.sd.png.commons.IvyBaseActivityNoActionBar;
import com.ivy.sd.png.commons.MaterialSpinner;
import com.ivy.sd.png.commons.SDUtil;
import com.ivy.sd.png.model.BrandDialogInterface;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.model.FiveLevelFilterCallBack;
import com.ivy.sd.png.util.Commons;
import com.ivy.sd.png.view.FilterFiveFragment;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Vector;

public class ManualVanLoadActivity extends IvyBaseActivityNoActionBar implements
        BrandDialogInterface, OnClickListener, OnEditorActionListener, FiveLevelFilterCallBack {

    private static final String BRAND = "Brand";
    private static final String GENERAL = "General";
    private HashMap<Integer, Integer> mSelectedIdByLevelId;
    private DrawerLayout mDrawerLayout;
    private ListView lvwplist;
    private EditText mEdtSearchproductName;
    private TextView productName;
    private TextView tvSelectedFilter;
    private ViewFlipper viewFlipper;
    private BusinessModel bmodel;
    private HashMap<String, String> mSelectedFilterMap = new HashMap<>();
    private ArrayList<String> mSearchTypeArray = new ArrayList<>();
    private InputMethodManager inputManager;
    private ArrayList<LoadManagementBO> list;
    private int selectedSubDepotId;
    private boolean totQtyflag;
    private boolean isClicked;
    private MyAdapter mSchedule;
    private ManualVanLoadHelper manualVanLoadHelper;

    public static final int VAN_RETURN_PRODUCT_RESULT_CODE = 118;

    android.content.DialogInterface.OnDismissListener vanloadDismissListener = new android.content.DialogInterface.OnDismissListener() {
        @Override
        public void onDismiss(DialogInterface dialog) {
            totQtyflag = true;
            mSchedule.notifyDataSetChanged();
            dialog.dismiss();
        }
    };


    private ManualVanLoadDialog vanLoadDialog;
    private boolean isAddBatchDialogClicked = false;

    android.content.DialogInterface.OnDismissListener addBatch = new android.content.DialogInterface.OnDismissListener() {

        @Override
        public void onDismiss(DialogInterface dialog) {
            isAddBatchDialogClicked = false;
            dialog.dismiss();
            showMessage(getString(R.string.batch_created_successfully));
            if (list != null && list.size() > 0) {
                mSchedule = new MyAdapter(list);
                lvwplist.setAdapter(mSchedule);
            }
        }
    };
    android.content.DialogInterface.OnDismissListener cancelBatch = new android.content.DialogInterface.OnDismissListener() {

        @Override
        public void onDismiss(DialogInterface dialog) {
            isAddBatchDialogClicked = false;
            dialog.dismiss();
        }
    };
    private EditText quantity;
    private String append = "";
    SearchAsync searchAsync;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manual_vanlaod);
        overridePendingTransition(R.anim.trans_left_in, R.anim.trans_left_out);

        bmodel = (BusinessModel) getApplicationContext();
        bmodel.setContext(this);
        Toolbar toolbar = findViewById(R.id.toolbar);
        hideAndSeekViews();
        final Intent i = getIntent();
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

        if (toolbar != null) {
            setSupportActionBar(toolbar);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            setScreenTitle(i.getStringExtra("screentitle"));
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setIcon(null);
        }
        manualVanLoadHelper = ManualVanLoadHelper.getInstance(this.getApplicationContext());
        ActionBarDrawerToggle mDrawerToggle = new ActionBarDrawerToggle(this,
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


        updateBrandText("Brand", -1);

        productName.setOnTouchListener(new OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                int inType = productName.getInputType();
                productName.setInputType(InputType.TYPE_NULL);
                productName.onTouchEvent(event);
                productName.setInputType(inType);
                return true;
            }
        });
        inputManager = (InputMethodManager) this
                .getSystemService(Context.INPUT_METHOD_SERVICE);
        searchText();
        mDrawerLayout.closeDrawer(GravityCompat.END);
        searchAsync = new SearchAsync();
    }

    private void hideAndSeekViews() {

        try {
            Button saveBtn = findViewById(R.id.van_btn_save);
            viewFlipper = findViewById(R.id.view_flipper);
            tvSelectedFilter = findViewById(R.id.tvSkuName);
            LinearLayout keypadlty = findViewById(R.id.ll_keypad);
            mEdtSearchproductName = findViewById(R.id.edt_searchproductName);
            Button mBtnSearch = findViewById(R.id.btn_search);
            Button mBtnFilterPopup = findViewById(R.id.btn_filter_popup);
            Button mBtnClear = findViewById(R.id.btn_clear);
            productName = findViewById(R.id.productName);

            saveBtn.setOnClickListener(this);
            mBtnSearch.setOnClickListener(this);
            mBtnFilterPopup.setOnClickListener(this);
            mBtnClear.setOnClickListener(this);
            mEdtSearchproductName.setOnEditorActionListener(this);

            lvwplist = findViewById(R.id.list);
            lvwplist.setCacheColorHint(0);

            if (bmodel.configurationMasterHelper.IS_BATCHWISE_VANLOAD) {
                totQtyflag = true;
                findViewById(R.id.qtyTitle).setVisibility(View.VISIBLE);
                findViewById(R.id.viewTitle).setVisibility(View.VISIBLE);
                keypadlty.setVisibility(View.GONE);
            } else {
                totQtyflag = false;
                findViewById(R.id.itemcasetitle).setVisibility(View.VISIBLE);
                findViewById(R.id.outeritemcasetitle).setVisibility(
                        View.VISIBLE);
                findViewById(R.id.itempiecetitle).setVisibility(View.VISIBLE);
                keypadlty.setVisibility(View.VISIBLE);
            }

            if (!bmodel.configurationMasterHelper.SHOW_ORDER_CASE) {
                findViewById(R.id.itemcasetitle).setVisibility(View.GONE);
            } else {
                try {
                    if (bmodel.labelsMasterHelper.applyLabels(findViewById(
                            R.id.itemcasetitle).getTag()) != null)
                        ((TextView) findViewById(R.id.itemcasetitle))
                                .setText(bmodel.labelsMasterHelper
                                        .applyLabels(findViewById(
                                                R.id.itemcasetitle).getTag()));
                } catch (Exception e) {
                    Commons.printException("" + e);
                }
            }

            if (!bmodel.configurationMasterHelper.SHOW_ORDER_PCS) {
                findViewById(R.id.itempiecetitle).setVisibility(View.GONE);
            } else {
                try {
                    if (bmodel.labelsMasterHelper.applyLabels(findViewById(
                            R.id.itempiecetitle).getTag()) != null)
                        ((TextView) findViewById(R.id.itempiecetitle))
                                .setText(bmodel.labelsMasterHelper
                                        .applyLabels(findViewById(
                                                R.id.itempiecetitle).getTag()));
                } catch (Exception e) {
                    Commons.printException("" + e);
                }
            }

            if (!bmodel.configurationMasterHelper.SHOW_OUTER_CASE) {
                findViewById(R.id.outeritemcasetitle).setVisibility(View.GONE);
            } else {
                try {
                    if (bmodel.labelsMasterHelper.applyLabels(findViewById(
                            R.id.outeritemcasetitle).getTag()) != null)
                        ((TextView) findViewById(R.id.outeritemcasetitle))
                                .setText(bmodel.labelsMasterHelper
                                        .applyLabels(findViewById(
                                                R.id.outeritemcasetitle).getTag()));
                } catch (Exception e) {
                    Commons.printException("" + e);
                }
            }

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
            TextView txtLbl =  findViewById(R.id.tv_subdepot);
            if (bmodel.configurationMasterHelper.SHOW_SUBDEPOT) {
                ArrayAdapter<SubDepotBo> subDepotAdapter;
                LinearLayout subDepotLayout =  findViewById(R.id.ll_depot);
                subDepotLayout.setVisibility(View.VISIBLE);
                final MaterialSpinner spnSubDepot =  findViewById(R.id.sp_subdepot);
                LoadManagementHelper loadManagementHelper = LoadManagementHelper.getInstance(this);
                if (bmodel.configurationMasterHelper.VANLOAD_TYPE == 0) {
                    txtLbl.setText(R.string.subdepot);
                    subDepotAdapter = new ArrayAdapter<>(
                            ManualVanLoadActivity.this,
                            R.layout.spinner_blacktext_layout,
                            loadManagementHelper.getSubDepotList());
                } else {
                    txtLbl.setText(R.string.distributor);
                    subDepotAdapter = new ArrayAdapter<>(
                            ManualVanLoadActivity.this,
                            android.R.layout.simple_spinner_item,
                            loadManagementHelper.getDistributorList());
                }
                subDepotAdapter
                        .setDropDownViewResource(R.layout.spinner_blacktext_list_item);
                spnSubDepot.setAdapter(subDepotAdapter);
                spnSubDepot
                        .setOnItemSelectedListener(new OnItemSelectedListener() {
                            public void onItemSelected(AdapterView<?> parent,
                                                       View view, int position, long id) {

                                SubDepotBo reString = (SubDepotBo) spnSubDepot
                                        .getSelectedItem();
                                selectedSubDepotId = reString.getSubDepotId();

                            }

                            public void onNothingSelected(AdapterView<?> parent) {
                            }
                        });
            }
        } catch (Exception e) {
            Commons.printException("" + e);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        bmodel = (BusinessModel) getApplicationContext();
        bmodel.setContext(this);
        isClicked = false;
    }

    public void onClick(View v) {
        int btnId = v.getId();
        bmodel = (BusinessModel) getApplicationContext();
        bmodel.setContext(this);
        if (btnId == R.id.btn_search) {
            viewFlipper.showNext();
        } else if (btnId == R.id.btn_filter_popup) {
            AlertDialog.Builder builderSingle = new AlertDialog.Builder(
                    ManualVanLoadActivity.this);
            final ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(
                    ManualVanLoadActivity.this,
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
            mEdtSearchproductName.setText("");
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
            if (bmodel.configurationMasterHelper.SHOW_SUBDEPOT) {

                if (selectedSubDepotId != 0) {
                    if (manualVanLoadHelper.hasVanLoadDone()
                            && selectedSubDepotId != 0) {
                        if (bmodel.configurationMasterHelper.VANLOAD_TYPE == 0) {
                            new CalculateLiabilityAsyncTask(ManualVanLoadActivity.this, selectedSubDepotId).execute();
                        } else {
                            showDialog(1);
                        }
                    } else
                        bmodel.showAlert(
                                getResources().getString(
                                        R.string.no_data_tosave), 0);
                } else {
                    if (bmodel.configurationMasterHelper.VANLOAD_TYPE == 0) {
                        bmodel.showAlert(
                                getResources().getString(R.string.select_sub_depot),
                                0);
                    } else {
                        bmodel.showAlert(
                                getResources().getString(R.string.select_distributors),
                                0);
                    }
                }

            } else {
                if (manualVanLoadHelper.hasVanLoadDone()) {
                    showDialog(1);
                } else {
                    bmodel.showAlert(
                            getResources().getString(R.string.no_data_tosave),
                            0);
                }
            }
        }
    }

    @Override
    public void updateBrandText(String mFilterText, int id) {
        // Close the drawer
        mDrawerLayout.closeDrawers();

        Vector<LoadManagementBO> vanlist = bmodel.productHelper.getLoadMgmtProducts();

        if (vanlist == null) {
            bmodel.showAlert(
                    getResources().getString(R.string.no_products_exists), 0);
            return;
        }

        int siz = vanlist.size();
        list = new ArrayList<>();

        for (int i = 0; i < siz; ++i) {
            LoadManagementBO ret = vanlist.elementAt(i);
            if (ret.getIssalable() == 1) {
                if (id == ret.getParentid() || id == -1) {
                    list.add(ret);
                }
            }
        }

        if (mFilterText.equals(BRAND))
            tvSelectedFilter.setText(getResources().getString(
                    R.string.product_name)
                    + "(" + list.size() + ")");
        else
            tvSelectedFilter.setText(mFilterText + "(" + list.size() + ")");
        if (list != null && list.size() > 0) {
            mSchedule = new MyAdapter(list);
            lvwplist.setAdapter(mSchedule);
        }
    }

    @Override
    public void updateGeneralText(String mFilterText) {
        updateBrandText(BRAND, -1);
    }

    @Override
    public void updateCancel() {
        // Close Drawer
        mDrawerLayout.closeDrawers();
    }

    @Override
    public void updateFromFiveLevelFilter(int mFilteredPid, HashMap<Integer, Integer> mSelectedIdByLevelId, ArrayList<Integer> mAttributeProducts, String mFilterText) {
        ArrayList<LoadManagementBO> filterlist = new ArrayList<>();
        Vector<LoadManagementBO> vanlist = bmodel.productHelper.getLoadMgmtProducts();

        if (mAttributeProducts != null) {
            if (mFilteredPid != 0) {
                for (LoadManagementBO productBO : vanlist) {
                    if (productBO.getIssalable() == 1) {
                        if (productBO.getParentHierarchy().contains("/" + mFilteredPid + "/")) {
                            // here we get all products mapped to parent id list, then that product will be added only if it is mapped to selected attribute
                            if (mAttributeProducts.contains(productBO.getProductid())) {
                                filterlist.add(productBO);
                            }
                        }
                    }
                }
            } else {
                for (int pid : mAttributeProducts) {
                    for (LoadManagementBO productBO : vanlist) {
                        if (productBO.getIssalable() == 1) {
                            if (pid == productBO.getProductid()) {
                                filterlist.add(productBO);
                            }
                        }
                    }
                }
            }
        } else {
            if (mFilteredPid != 0 && !mFilterText.isEmpty()) {
                for (LoadManagementBO loadMgtBO : vanlist) {
                    if (loadMgtBO.getIssalable() == 1) {
                        if (loadMgtBO.getParentHierarchy().contains("/" + mFilteredPid + "/")) {
                            filterlist.add(loadMgtBO);
                        }
                    }
                }
            } else {
                for (LoadManagementBO loadMgtBO : vanlist) {
                    if (loadMgtBO.getIssalable() == 1) {
                        filterlist.add(loadMgtBO);

                    }
                }
            }
        }
        if (filterlist != null && filterlist.size() > 0) {
            mSchedule = new MyAdapter(filterlist);
            lvwplist.setAdapter(mSchedule);
        }

        this.mSelectedIdByLevelId = mSelectedIdByLevelId;

        mDrawerLayout.closeDrawers();
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
        menu.findItem(R.id.menu_bottle_return).setVisible(!drawerOpen);

        menu.findItem(R.id.menu_fivefilter).setVisible(false);

        if (bmodel.configurationMasterHelper.SHOW_PRODUCTRETURN && bmodel.configurationMasterHelper.VANLOAD_TYPE == 0)
            menu.findItem(R.id.menu_bottle_return).setVisible(true);
        else
            menu.findItem(R.id.menu_bottle_return).setVisible(false);

        menu.findItem(R.id.menu_fivefilter).setVisible(true);

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
        } else if (i == R.id.menu_bottle_return) {
            if (!isClicked) {
                isClicked = true;
                int size = bmodel.productHelper.getBomReturnProducts().size();
                if (size > 0) {
                    new LoadReturnProductDialogAsyncTask(ManualVanLoadActivity.this).execute();
                } else {
                    showMessage(getString(R.string.data_not_mapped));
                    isClicked = false;
                }
            }
            return true;
        } else if (i == R.id.menu_fivefilter) {
            if (inputManager.isAcceptingText())// hide soft key board after select expend menu
                inputManager.hideSoftInputFromWindow(
                        mEdtSearchproductName.getWindowToken(), 0);

            Bundle bundle = new Bundle();
            bundle.putSerializable("serilizeContent", bmodel.configurationMasterHelper.getGenFilter());
            bundle.putString("isFrom", "STK");
            bundle.putSerializable("selectedFilter", mSelectedIdByLevelId);

            quantity = null;
            mDrawerLayout.openDrawer(GravityCompat.END);

            loadFiveFilterFragment(bundle, R.id.right_drawer);

            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void onBackButtonClick() {
        if (manualVanLoadHelper.hasVanLoadDone()) {
            showDialog(0);
        } else {

            bmodel.moduleTimeStampHelper.saveModuleTimeStamp("Out");
            bmodel.moduleTimeStampHelper.setTid("");
            bmodel.moduleTimeStampHelper.setModuleCode("");
            finish();
            overridePendingTransition(R.anim.trans_right_in, R.anim.trans_right_out);
        }
    }

    private void FiveFilterFragment() {
        try {

            mDrawerLayout.openDrawer(GravityCompat.END);

            FragmentManager fm = getSupportFragmentManager();
            FilterFiveFragment<?> frag = (FilterFiveFragment<?>) fm.findFragmentByTag("Fivefilter");
            FragmentTransaction ft = fm.beginTransaction();
            if (frag != null)
                ft.detach(frag);
            Bundle bundle = new Bundle();
            bundle.putSerializable("serilizeContent", bmodel.configurationMasterHelper.getGenFilter());
            bundle.putString("isFrom", "STK");
            bundle.putSerializable("selectedFilter", mSelectedIdByLevelId);

            // set Fragmentclass Arguments
            FilterFiveFragment<Object> fragobj = new FilterFiveFragment<Object>();
            fragobj.setArguments(bundle);

            ft.replace(R.id.right_drawer, fragobj, "Fivefilter");
            ft.commit();
        } catch (Exception e) {
            Commons.printException("" + e);
        }
    }

    protected Dialog onCreateDialog(int id) {
        switch (id) {
            case 0:
                AlertDialog.Builder builder = new AlertDialog.Builder(ManualVanLoadActivity.this)
                        .setIcon(null)
                        .setCancelable(false)
                        .setTitle(
                                getResources()
                                        .getString(
                                                R.string.doyouwantgoback))
                        .setPositiveButton(R.string.ok,
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog,
                                                        int whichButton) {
                                        bmodel.moduleTimeStampHelper.saveModuleTimeStamp("Out");
                                        bmodel.moduleTimeStampHelper.setTid("");
                                        bmodel.moduleTimeStampHelper.setModuleCode("");

                                        finish();/* User clicked OK so do some stuff */
                                    }
                                })
                        .setNegativeButton(R.string.cancel,
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog,
                                                        int whichButton) {
                                        /* User clicked Cancel so do some stuff */
                                    }
                                });
                bmodel.applyAlertDialogTheme(builder);
                break;

            case 1:
                AlertDialog.Builder builder1 = new AlertDialog.Builder(ManualVanLoadActivity.this)
                        .setIcon(null)
                        .setCancelable(false)
                        .setTitle(R.string.do_you_want_to_save_stock)
                        .setPositiveButton(R.string.ok,
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog,
                                                        int whichButton) {
                                        //new SaveVanLoad().execute();
                                        new SaveVanLoadAsyncTask(ManualVanLoadActivity.this, selectedSubDepotId).execute();

                                    }
                                })
                        .setNegativeButton(R.string.cancel,
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog,
                                                        int whichButton) {
                                        /* User clicked Cancel so do some stuff */
                                    }
                                });
                bmodel.applyAlertDialogTheme(builder1);
                break;

        }

        return null;
    }


    private void searchText() {
        mSearchTypeArray = new ArrayList<>();
        mSearchTypeArray.add(getResources().getString(R.string.all));
        mSearchTypeArray.add(getResources().getString(R.string.product_name));
        mSearchTypeArray.add(getResources().getString(R.string.prod_code));
        mSearchTypeArray.add(getResources().getString(
                R.string.order_dialog_barcode));
        try {
            mEdtSearchproductName.addTextChangedListener(new TextWatcher() {
                public void afterTextChanged(Editable s) {
                    supportInvalidateOptionsMenu();
                    if (s.length() >= 3) {
                        searchAsync = new SearchAsync();
                        searchAsync.execute();
                    }
                }

                @Override
                public void beforeTextChanged(CharSequence s, int start,
                                              int count, int after) {
                  /*  if (mEdtSearchproductName.getText().toString().length() < 3) {
                        list.clear();
                    }*/
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
            Commons.printException("" + e);
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
            mSchedule = new MyAdapter(list);
            lvwplist.setAdapter(mSchedule);


        }
    }

    public void loadSearchedList() {

        if (mSelectedIdByLevelId != null)
            mSelectedIdByLevelId.clear();

        Vector<LoadManagementBO> items = bmodel.productHelper.getLoadMgmtProducts();

        if (items == null) {
            bmodel.showAlert(
                    getResources().getString(R.string.no_products_exists),
                    0);
            return;
        }
        int siz = items.size();
        list = new ArrayList<>();
        String mSelectedFilter = bmodel.getProductFilter();
        for (int i = 0; i < siz; ++i) {
            LoadManagementBO ret = items.elementAt(i);
            // For breaking search..
            if (searchAsync.isCancelled()) {
                break;
            }

            if (ret.getIssalable() == 1) {
                if (getResources().getString(
                        R.string.order_dialog_barcode).equals(mSelectedFilter)) {
                    if (ret.getBarcode() != null && ret.getBarcode()
                            .toLowerCase()
                            .contains(mEdtSearchproductName.getText().toString().toLowerCase()))
                        list.add(ret);

                } else if (getResources().getString(
                        R.string.prod_code).equals(mSelectedFilter)) {
                    if ((ret.getRField1() != null && ret.getRField1()
                            .toLowerCase()
                            .contains(
                                    mEdtSearchproductName.getText().toString()
                                            .toLowerCase())) || (ret.getProductCode() != null
                            && ret.getProductCode().toLowerCase().contains(mEdtSearchproductName.getText().toString()
                            .toLowerCase())))
                        list.add(ret);

                } else if ((getResources().getString(
                        R.string.product_name).equals(mSelectedFilter))) {
                    if (ret.getProductshortname() != null && ret.getProductshortname()
                            .toLowerCase()
                            .contains(mEdtSearchproductName.getText().toString().toLowerCase()))
                        list.add(ret);
                } else {
                    if (ret.getBarcode() != null && ret.getBarcode()
                            .toLowerCase()
                            .contains(mEdtSearchproductName.getText().toString().toLowerCase()))
                        list.add(ret);
                    else if ((ret.getRField1() != null && ret.getRField1()
                            .toLowerCase()
                            .contains(
                                    mEdtSearchproductName.getText().toString()
                                            .toLowerCase())) || (ret.getProductCode() != null
                            && ret.getProductCode().toLowerCase().contains(mEdtSearchproductName.getText().toString()
                            .toLowerCase())))
                        list.add(ret);
                    else if (ret.getProductshortname() != null && ret.getProductshortname()
                            .toLowerCase()
                            .contains(mEdtSearchproductName.getText().toString().toLowerCase()))
                        list.add(ret);
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
            if (mEdtSearchproductName.getText().length() >= 3) {
                searchAsync = new SearchAsync();
                searchAsync.execute();
            } else {
                showMessage(getString(R.string.enter_atleast_three_letters));

            }
            return true;
        }
        return false;
    }

    public void numberPressed(View vw) {
        if (vanLoadDialog != null) {
            if (vanLoadDialog.isShowing()) {
                vanLoadDialog.numberPressed(vw);
            } else {
                activityKeypad(vw);
            }
        } else {
            activityKeypad(vw);
        }

    }

    private void activityKeypad(View view) {
        if (quantity == null) {
            bmodel.showAlert(
                    this.getResources().getString(R.string.please_select_item),
                    0);
        } else {
            int id = view.getId();
            if (id == R.id.calcdel) {
                int s = SDUtil.convertToInt(quantity.getText()
                        .toString());
                s = s / 10;
                String qty = s + "";
                quantity.setText(qty);
            } else {
                Button ed = findViewById(view.getId());
                append = ed.getText().toString();
                eff();
            }
        }
    }

    public void eff() {
        String s = quantity.getText().toString();
        if (!"0".equals(s) && !"0.0".equals(s)) {
            String qty = quantity.getText() + append;
            quantity.setText(qty);
        } else
            quantity.setText(append);
    }

    class MyAdapter extends ArrayAdapter<LoadManagementBO> {
        ArrayList<LoadManagementBO> items;
        //LoadManagementBO product;

        MyAdapter(ArrayList<LoadManagementBO> items) {
            super(ManualVanLoadActivity.this, R.layout.van_load, items);
            this.items = items;
        }

        public LoadManagementBO getItem(int position) {
            return items.get(position);
        }

        public long getItemId(int position) {
            return position;
        }

        public int getCount() {
            return items.size();
        }

        @Override
        public int getItemViewType(int position) {
            return position;
        }

        @NonNull
        public View getView(final int position, View convertView,
                            @NonNull ViewGroup parent) {

            final ViewHolder holder;

            //product = items.get(position);
            View row = convertView;
            try {
                if (row == null) {
                    LayoutInflater inflater = getLayoutInflater();
                    row = inflater.inflate(R.layout.van_load, parent, false);
                    holder = new ViewHolder();
                    holder.caseQty = row
                            .findViewById(R.id.productqtyCases);
                    holder.pieceQty = row
                            .findViewById(R.id.productqtyPieces);
                    holder.psname = row
                            .findViewById(R.id.productName);
                    holder.psname.setMaxLines(bmodel.configurationMasterHelper.MAX_NO_OF_PRODUCT_LINES);
                    holder.outerQty = row
                            .findViewById(R.id.outerproductqtyCases);
                    holder.sih = row
                            .findViewById(R.id.stock_and_order_listview_sih);
                    holder.totQty = row.findViewById(R.id.totalQty);
                    holder.listLayout = row.findViewById(R.id.inv_view_layout);
                    holder.rowLayout = row.findViewById(R.id.list_header_lty);
                    holder.productBO = items.get(position);

                    if (bmodel.configurationMasterHelper.IS_BATCHWISE_VANLOAD) {
                        holder.rowLayout.setOnClickListener(ManualVanLoadActivity.this);
                        holder.rowLayout.setClickable(true);
                        holder.caseQty.setVisibility(View.GONE);
                        holder.pieceQty.setVisibility(View.GONE);
                        holder.outerQty.setVisibility(View.GONE);
                        holder.totQty.setVisibility(View.VISIBLE);
                        holder.listLayout.setVisibility(View.VISIBLE);
                    } else {
                        holder.rowLayout.setOnClickListener(null);
                        holder.rowLayout.setClickable(false);
                        holder.caseQty.setVisibility(View.VISIBLE);
                        holder.pieceQty.setVisibility(View.VISIBLE);
                        holder.outerQty.setVisibility(View.VISIBLE);
                        holder.listLayout.setVisibility(View.GONE);
                    }

                    if (!bmodel.configurationMasterHelper.SHOW_ORDER_CASE)
                        holder.caseQty.setVisibility(View.GONE);
                    if (!bmodel.configurationMasterHelper.SHOW_ORDER_PCS)
                        holder.pieceQty.setVisibility(View.GONE);
                    if (!bmodel.configurationMasterHelper.SHOW_OUTER_CASE)
                        holder.outerQty.setVisibility(View.GONE);


                    holder.rowLayout.setOnClickListener(new OnClickListener() {
                        public void onClick(View v) {
                            if (totQtyflag) {
                                totQtyflag = false;
                                vanLoadDialog = new ManualVanLoadDialog(
                                        ManualVanLoadActivity.this,
                                        holder.productBO,
                                        vanloadDismissListener);
                                vanLoadDialog.show();
                                vanLoadDialog.setCancelable(false);
                            }
                        }
                    });

                    holder.psname.setOnClickListener(new OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            productName.setText(holder.pname);
                            holder.caseQty.selectAll();
                            holder.caseQty.requestFocus();

                            if (viewFlipper.getDisplayedChild() != 0) {
                                viewFlipper.setInAnimation(
                                        ManualVanLoadActivity.this,
                                        R.anim.in_from_left);
                                viewFlipper.setOutAnimation(
                                        ManualVanLoadActivity.this,
                                        R.anim.out_to_left);
                                viewFlipper.showPrevious();
                            }
                        }
                    });


                    holder.psname
                            .setOnLongClickListener(new View.OnLongClickListener() {

                                @Override
                                public boolean onLongClick(View v) {
                                    if (bmodel.configurationMasterHelper.IS_ADD_NEW_BATCH) {
                                        if (!isAddBatchDialogClicked) {
                                            isAddBatchDialogClicked = true;
                                            ManualVanLoadBatchEntryDialog batchEntryDialog = new ManualVanLoadBatchEntryDialog(
                                                    ManualVanLoadActivity.this,
                                                    holder.productBO, addBatch,
                                                    cancelBatch,
                                                    getSupportFragmentManager());

                                            batchEntryDialog
                                                    .setCancelable(false);
                                            batchEntryDialog.show();
                                        }
                                    }
                                    return false;
                                }
                            });

                    holder.caseQty.setOnTouchListener(new OnTouchListener() {
                        public boolean onTouch(View v, MotionEvent event) {
                            quantity = holder.caseQty;
                            int inType = holder.caseQty.getInputType();
                            holder.caseQty.setInputType(InputType.TYPE_NULL);
                            holder.caseQty.onTouchEvent(event);
                            holder.caseQty.setInputType(inType);
                            holder.caseQty.requestFocus();
                            if (holder.caseQty.getText().length() > 0)
                                holder.caseQty.setSelection(holder.caseQty.getText().length());
                            inputManager.hideSoftInputFromWindow(
                                    quantity.getWindowToken(), 0);
                            return true;
                        }
                    });
                    holder.pieceQty.setOnTouchListener(new OnTouchListener() {
                        public boolean onTouch(View v, MotionEvent event) {
                            quantity = holder.pieceQty;
                            int inType = holder.pieceQty.getInputType();
                            holder.pieceQty.setInputType(InputType.TYPE_NULL);
                            holder.pieceQty.onTouchEvent(event);
                            holder.pieceQty.setInputType(inType);
                            holder.pieceQty.requestFocus();
                            if (holder.pieceQty.getText().length() > 0)
                                holder.pieceQty.setSelection(holder.pieceQty.getText().length());
                            inputManager.hideSoftInputFromWindow(
                                    quantity.getWindowToken(), 0);
                            return true;
                        }
                    });
                    holder.outerQty.setOnTouchListener(new OnTouchListener() {

                        @Override
                        public boolean onTouch(View v, MotionEvent event) {
                            quantity = holder.outerQty;
                            int inType = holder.outerQty.getInputType();
                            holder.outerQty.setInputType(InputType.TYPE_NULL);
                            holder.outerQty.onTouchEvent(event);
                            holder.outerQty.setInputType(inType);
                            holder.outerQty.requestFocus();
                            if (holder.outerQty.getText().length() > 0)
                                holder.outerQty.setSelection(holder.outerQty.getText().length());
                            inputManager.hideSoftInputFromWindow(
                                    quantity.getWindowToken(), 0);
                            return true;
                        }
                    });
                    holder.outerQty.addTextChangedListener(new TextWatcher() {

                        @Override
                        public void onTextChanged(CharSequence s, int start,
                                                  int before, int count) {
                            // TO DO Auto-generated method stub
                        }

                        @Override
                        public void beforeTextChanged(CharSequence s,
                                                      int start, int count, int after) {
                            // TO DO Auto-generated method stub
                        }

                        @Override
                        public void afterTextChanged(Editable s) {
                            String qty = s.toString();
                            if (qty.length() > 0)
                                holder.outerQty.setSelection(qty.length());
                            if (!"".equals(qty)) {
                                holder.productBO.setOuterQty(SDUtil
                                        .convertToInt(qty));
                            }

                        }
                    });
                    holder.pieceQty.addTextChangedListener(new TextWatcher() {
                        public void afterTextChanged(Editable s) {

                            String qty = s.toString();
                            if (qty.length() > 0)
                                holder.pieceQty.setSelection(qty.length());

                            if (!"".equals(qty)) {
                                holder.productBO.setPieceqty(SDUtil
                                        .convertToInt(qty));
                            }
                        }

                        public void beforeTextChanged(CharSequence s,
                                                      int start, int count, int after) {
                            // TO DO Auto-generated method stub
                        }

                        public void onTextChanged(CharSequence s, int start,
                                                  int before, int count) {
                            // TO DO Auto-generated method stub
                        }
                    });
                    holder.caseQty.addTextChangedListener(new TextWatcher() {
                        public void afterTextChanged(Editable s) {

                            String qty = s.toString();
                            if (qty.length() > 0)
                                holder.caseQty.setSelection(qty.length());
                            if (!"".equals(qty)) {
                                holder.productBO.setCaseqty(SDUtil
                                        .convertToInt(qty));
                            }
                        }

                        public void beforeTextChanged(CharSequence s,
                                                      int start, int count, int after) {
                            // TO DO Auto-generated method stub
                        }

                        public void onTextChanged(CharSequence s, int start,
                                                  int before, int count) {
                            // TO DO Auto-generated method stub
                        }
                    });


                    row.setTag(holder);
                } else {
                    holder = (ViewHolder) row.getTag();
                }
                holder.productBO = items.get(position);
                holder.pname = holder.productBO.getProductname();
                holder.psname.setText(holder.productBO.getProductshortname());
                String tvt = holder.productBO.getSih() + "";
                holder.sih.setText(tvt);
                tvt = holder.productBO.getCaseqty() + "";
                holder.caseQty.setText(tvt);
                tvt = holder.productBO.getOuterQty() + "";
                holder.outerQty.setText(tvt);
                tvt = holder.productBO.getPieceqty() + "";
                holder.pieceQty.setText(tvt);
                tvt = (holder.productBO.getCaseqty() * holder.productBO
                        .getCaseSize())
                        + (holder.productBO.getOuterQty() * holder.productBO.getOuterSize())
                        + holder.productBO.getPieceqty() + "";
                holder.totQty.setText(tvt);

                // Disable the User Entry if UomID is Zero
                if (!holder.productBO.isPieceMapped()) {
                    holder.pieceQty.setEnabled(false);
                } else {
                    holder.pieceQty.setEnabled(true);
                }
                if (!holder.productBO.isCaseMapped()) {
                    holder.caseQty.setEnabled(false);
                } else {
                    holder.caseQty.setEnabled(true);
                }
                if (!holder.productBO.isOuterMapped()) {
                    holder.outerQty.setEnabled(false);
                } else {
                    holder.outerQty.setEnabled(true);
                }

            } catch (Exception e) {
                Commons.printException("" + e);
            }

            return row;
        }
    }

    class ViewHolder {
        LoadManagementBO productBO;
        String pname;
        TextView psname;
        TextView sih;
        EditText pieceQty;
        EditText caseQty;
        EditText outerQty;
        TextView totQty;
        LinearLayout listLayout, rowLayout;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == VAN_RETURN_PRODUCT_RESULT_CODE)
            overridePendingTransition(0, R.anim.zoom_exit);
    }
}