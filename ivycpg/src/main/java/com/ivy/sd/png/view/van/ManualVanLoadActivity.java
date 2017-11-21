package com.ivy.sd.png.view.van;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
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
import android.widget.Toast;
import android.widget.ViewFlipper;

import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.bo.LevelBO;
import com.ivy.sd.png.bo.LoadManagementBO;
import com.ivy.sd.png.bo.SubDepotBo;
import com.ivy.sd.png.commons.IvyBaseActivityNoActionBar;
import com.ivy.sd.png.commons.MaterialSpinner;
import com.ivy.sd.png.commons.SDUtil;
import com.ivy.sd.png.model.BrandDialogInterface;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.provider.ConfigurationMasterHelper;
import com.ivy.sd.png.util.Commons;
import com.ivy.sd.png.view.FilterFiveFragment;
import com.ivy.sd.png.view.FilterFragment;
import com.ivy.sd.png.view.HomeScreenActivity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;

public class ManualVanLoadActivity extends IvyBaseActivityNoActionBar implements
        BrandDialogInterface, OnClickListener, OnEditorActionListener {

    private static final String BRAND = "Brand";
    private static final String GENERAL = "General";
    private HashMap<Integer, Integer> mSelectedIdByLevelId;
    private ArrayList<LoadManagementBO> filterlist;
    private String brandFilterText = "BRAND";
    private DrawerLayout mDrawerLayout;
    private ListView lvwplist;
    private EditText mEdtSearchproductName;
    private TextView productName;
    private TextView tvSelectedFilter, sihTitle, qtyTitle, itemCaseTitle, outeritemTitle, itempieceTitle, viewTitle;
    private Button mBtnSearch;
    private Button mBtnFilterPopup;
    private Button mBtnClear;
    private ViewFlipper viewFlipper;
    private BusinessModel bmodel;
    private HashMap<String, String> mSelectedFilterMap = new HashMap<>();
    private ArrayList<String> mSearchTypeArray = new ArrayList<>();
    private InputMethodManager inputManager;
    private Vector<LoadManagementBO> vanlist;
    private ArrayList<LoadManagementBO> list;
    private int selectedSubDepotId;
    private boolean totQtyflag;
    private boolean isClicked;
    private MyAdapter mSchedule;
    private Toolbar toolbar;
    private TextView toolbarTxt;
    private Button saveBtn;
    LinearLayout keypadlty;
    private boolean isFromPlanning = false;
    private Intent loadActivity;
    android.content.DialogInterface.OnDismissListener vanloadDismissListener = new android.content.DialogInterface.OnDismissListener() {
        @Override
        public void onDismiss(DialogInterface dialog) {
            totQtyflag = true;
            mSchedule.notifyDataSetChanged();
            dialog.dismiss();
        }
    };


    private VanLoadReturnProductDialog returnProductDialog;
    private ManualVanLoadDialog vanLoadDialog;
    private boolean isAddBatchDialogClicked = false;
    private ManualVanLoadBatchEntryDialog batchEntryDialog;
    android.content.DialogInterface.OnDismissListener addBatch = new android.content.DialogInterface.OnDismissListener() {

        @Override
        public void onDismiss(DialogInterface dialog) {
            isAddBatchDialogClicked = false;
            batchEntryDialog.dismiss();
            Toast.makeText(ManualVanLoadActivity.this, R.string.batch_created_successfully, Toast.LENGTH_LONG).show();
            mSchedule = new MyAdapter(list);
            lvwplist.setAdapter(mSchedule);
        }
    };
    android.content.DialogInterface.OnDismissListener cancelBatch = new android.content.DialogInterface.OnDismissListener() {

        @Override
        public void onDismiss(DialogInterface dialog) {
            isAddBatchDialogClicked = false;
            batchEntryDialog.dismiss();
        }
    };
    private EditText quantity;
    private String append = "";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manual_vanlaod);
        overridePendingTransition(R.anim.trans_left_in, R.anim.trans_left_out);

        bmodel = (BusinessModel) getApplicationContext();
        bmodel.setContext(this);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbarTxt = (TextView) findViewById(R.id.tv_toolbar_title);
        hideAndSeekViews();
        final Intent i = getIntent();
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow,
                GravityCompat.START);
        mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow,
                GravityCompat.END);
        mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);

        FrameLayout drawer = (FrameLayout) findViewById(R.id.right_drawer);

        int width = getResources().getDisplayMetrics().widthPixels;
        DrawerLayout.LayoutParams params = (android.support.v4.widget.DrawerLayout.LayoutParams) drawer.getLayoutParams();
        params.width = width;
        drawer.setLayoutParams(params);
        isFromPlanning = getIntent().getBooleanExtra("planingsub", false);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            setScreenTitle(i.getStringExtra("screentitle"));
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setIcon(null);
        }

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

        vanlist = bmodel.productHelper.getProducts();

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
    }

    private void hideAndSeekViews() {

        try {
            saveBtn = (Button) findViewById(R.id.van_btn_save);
            viewFlipper = (ViewFlipper) findViewById(R.id.view_flipper);
            tvSelectedFilter = (TextView) findViewById(R.id.tvSkuName);
            keypadlty = (LinearLayout) findViewById(R.id.ll_keypad);
            mEdtSearchproductName = (EditText) findViewById(R.id.edt_searchproductName);
            sihTitle = (TextView) findViewById(R.id.sihTitle);
            qtyTitle = (TextView) findViewById(R.id.qtyTitle);
            viewTitle = (TextView) findViewById(R.id.viewTitle);
            itemCaseTitle = (TextView) findViewById(R.id.itemcasetitle);
            outeritemTitle = (TextView) findViewById(R.id.outeritemcasetitle);
            itempieceTitle = (TextView) findViewById(R.id.itempiecetitle);
            mBtnSearch = (Button) findViewById(R.id.btn_search);
            mBtnFilterPopup = (Button) findViewById(R.id.btn_filter_popup);
            mBtnClear = (Button) findViewById(R.id.btn_clear);
            productName = (TextView) findViewById(R.id.productName);

            saveBtn.setOnClickListener(this);
            mBtnSearch.setOnClickListener(this);
            mBtnFilterPopup.setOnClickListener(this);
            mBtnClear.setOnClickListener(this);
            mEdtSearchproductName.setOnEditorActionListener(this);

            lvwplist = (ListView) findViewById(R.id.lvwplist);
            lvwplist.setCacheColorHint(0);

            toolbarTxt.setTypeface(bmodel.configurationMasterHelper.getFontBaloobhai(ConfigurationMasterHelper.FontType.REGULAR));
            tvSelectedFilter.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
            sihTitle.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
            qtyTitle.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
            itemCaseTitle.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
            outeritemTitle.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
            itempieceTitle.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
            saveBtn.setTypeface(bmodel.configurationMasterHelper.getFontBaloobhai(ConfigurationMasterHelper.FontType.REGULAR));
            productName.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
            mEdtSearchproductName.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));


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

            if (!bmodel.configurationMasterHelper.SHOW_OUTER_CASE)
                findViewById(R.id.outeritemcasetitle).setVisibility(View.GONE);

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
            TextView txtLbl = (TextView) findViewById(R.id.tv_subdepot);
            if (bmodel.configurationMasterHelper.SHOW_SUBDEPOT) {
                ArrayAdapter<SubDepotBo> subDepotAdapter;
                LinearLayout subDepotLayout = (LinearLayout) findViewById(R.id.ll_depot);
                subDepotLayout.setVisibility(View.VISIBLE);
                final MaterialSpinner spnSubDepot = (MaterialSpinner) findViewById(R.id.sp_subdepot);
                if (bmodel.configurationMasterHelper.VANLOAD_TYPE == 0) {
                    txtLbl.setText(R.string.subdepot);
                    subDepotAdapter = new ArrayAdapter<>(
                            ManualVanLoadActivity.this,
                            R.layout.spinner_blacktext_layout,
                            bmodel.vanmodulehelper.getSubDepotList());
                } else {
                    txtLbl.setText(R.string.distributor);
                    subDepotAdapter = new ArrayAdapter<>(
                            ManualVanLoadActivity.this,
                            android.R.layout.simple_spinner_item,
                            bmodel.vanmodulehelper.getDistributorList());
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
        Button vw = (Button) v;
        bmodel = (BusinessModel) getApplicationContext();
        bmodel.setContext(this);
        if (vw == mBtnSearch) {
            viewFlipper.showNext();
        } else if (vw == mBtnFilterPopup) {
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

        } else if (vw == mBtnClear) {
            mEdtSearchproductName.setText("");
            /** set the following value to clear the **/
            mSelectedFilterMap.put("General", "All");
            mSelectedFilterMap.put("Brand", "All");
            mSelectedFilterMap.put("Category", "All");
            mSelectedIdByLevelId.clear();

            try {
                InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
            } catch (Exception e) {
                // TODO: handle exception
            }

            supportInvalidateOptionsMenu();
            updateGeneralText(GENERAL);
        } else if (vw == saveBtn) {
            if (bmodel.configurationMasterHelper.SHOW_SUBDEPOT) {


                if (selectedSubDepotId != 0) {
                    if (bmodel.vanmodulehelper.hasVanLoadDone()
                            && selectedSubDepotId != 0) {
                        if (bmodel.configurationMasterHelper.VANLOAD_TYPE == 0) {
                            new calculateLiability().execute();
                        } else {

                            if (bmodel.userMasterHelper.getUserMasterBO()
                                    .getDistributorid() == selectedSubDepotId) {
                                showDialog(1);
                            } else {
                                if (!bmodel.vanmodulehelper
                                        .isSecondaryDistributorDone()) {
                                    showDialog(1);
                                } else {
                                    bmodel.showAlert(
                                            getResources().getString(
                                                    R.string.sec_dist_loaded), 0);
                                }
                            }


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
                if (bmodel.vanmodulehelper.hasVanLoadDone()) {
                    showDialog(1);
                } else {
                    bmodel.showAlert(
                            getResources().getString(R.string.no_data_tosave),
                            0);
                }
            }
        }
    }

    /**
     * Used to Filter Category and Brand.
     */
    private void productFilterClickedFragment() {
        try {

            mDrawerLayout.openDrawer(GravityCompat.END);
            android.support.v4.app.FragmentManager fm = ManualVanLoadActivity.this
                    .getSupportFragmentManager();
            FilterFragment<?> frag = (FilterFragment<?>) fm
                    .findFragmentByTag("filter");
            android.support.v4.app.FragmentTransaction ft = fm
                    .beginTransaction();
            if (frag != null)
                ft.detach(frag);
            Bundle bundle = new Bundle();
            bundle.putString("filterName", BRAND);
            bundle.putBoolean("isFormBrand", true);
            bundle.putSerializable("serilizeContent",
                    bmodel.productHelper.getChildLevelBo());
            FilterFragment fragobj = new FilterFragment(mSelectedFilterMap);
            fragobj.setArguments(bundle);
            ft.replace(R.id.right_drawer, fragobj, "filter");
            ft.commit();
        } catch (Exception e) {
            Commons.printException(e);
        }
    }

    @Override
    public void updateBrandText(String mFilterText, int id) {
        // Close the drawer
        mDrawerLayout.closeDrawers();
        brandFilterText = mFilterText;

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

        mSchedule = new MyAdapter(list);
        lvwplist.setAdapter(mSchedule);
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
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_vanload, menu);
        return super.onCreateOptionsMenu(menu);
    }

    // Called whenever we call invalidateOptionsMenu()
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {

        if (!brandFilterText.equals(BRAND))
            menu.findItem(R.id.menu_product_filter).setIcon(
                    R.drawable.ic_action_filter_select);

        // If the nav drawer is open, hide action items related to the content
        // view
        boolean drawerOpen = mDrawerLayout.isDrawerOpen(GravityCompat.END);
        menu.findItem(R.id.menu_fivefilter).setVisible(!drawerOpen);
        menu.findItem(R.id.menu_product_filter).setVisible(!drawerOpen);
        menu.findItem(R.id.menu_bottle_return).setVisible(!drawerOpen);

        menu.findItem(R.id.menu_fivefilter).setVisible(false);
        menu.findItem(R.id.menu_product_filter).setVisible(false);

        if (bmodel.configurationMasterHelper.SHOW_PRODUCTRETURN && bmodel.configurationMasterHelper.VANLOAD_TYPE == 0)
            menu.findItem(R.id.menu_bottle_return).setVisible(true);
        else
            menu.findItem(R.id.menu_bottle_return).setVisible(false);


        if (bmodel.configurationMasterHelper.IS_FIVE_LEVEL_FILTER)
            menu.findItem(R.id.menu_fivefilter).setVisible(true);
        else
            menu.findItem(R.id.menu_product_filter).setVisible(true);


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
        } else if (i == R.id.menu_product_filter) {
            productFilterClickedFragment();
            return true;
        } else if (i == R.id.menu_bottle_return) {
            if (!isClicked) {
                isClicked = true;
                int size = bmodel.productHelper.getBomReturnProducts().size();
                if (size > 0) {
                    new LoadReturnProductDialog().execute();
                } else {
                    Toast.makeText(ManualVanLoadActivity.this,
                            getResources().getString(R.string.data_not_mapped),
                            Toast.LENGTH_SHORT).show();
                    isClicked = false;
                }
            }
            return true;
        } else if (i == R.id.menu_fivefilter) {
            FiveFilterFragment();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void onBackButtonClick() {
        if (bmodel.vanmodulehelper.hasVanLoadDone()) {
            showDialog(0);
        } else {
            loadActivity = new Intent(ManualVanLoadActivity.this, HomeScreenActivity.class);
            if (isFromPlanning)
                loadActivity.putExtra("menuCode", "MENU_PLANNING_SUB");
            else
                loadActivity.putExtra("menuCode", "MENU_LOAD_MANAGEMENT");
            bmodel.moduleTimeStampHelper.saveModuleTimeStamp("Out");
            bmodel.moduleTimeStampHelper.setTid("");
            bmodel.moduleTimeStampHelper.setModuleCode("");
            startActivity(loadActivity);
            finish();
            overridePendingTransition(R.anim.trans_right_in, R.anim.trans_right_out);
        }
    }

    private void FiveFilterFragment() {
        try {

            mDrawerLayout.openDrawer(GravityCompat.END);

            android.support.v4.app.FragmentManager fm = getSupportFragmentManager();
            FilterFiveFragment<?> frag = (FilterFiveFragment<?>) fm.findFragmentByTag("Fivefilter");
            android.support.v4.app.FragmentTransaction ft = fm.beginTransaction();
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
                                        loadActivity = new Intent(ManualVanLoadActivity.this, HomeScreenActivity.class);
                                        if (isFromPlanning)
                                            loadActivity.putExtra("menuCode", "MENU_PLANNING_SUB");
                                        else
                                            loadActivity.putExtra("menuCode", "MENU_LOAD_MANAGEMENT");
                                        bmodel.moduleTimeStampHelper.saveModuleTimeStamp("Out");
                                        bmodel.moduleTimeStampHelper.setTid("");
                                        bmodel.moduleTimeStampHelper.setModuleCode("");
                                        startActivity(loadActivity);
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
                                        new SaveVanLoad().execute();
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
        mSearchTypeArray.add(getResources().getString(R.string.product_name));
        mSearchTypeArray.add(getResources().getString(R.string.order_gcas));
        mSearchTypeArray.add(getResources().getString(
                R.string.order_dialog_barcode));
        try {
            mEdtSearchproductName.addTextChangedListener(new TextWatcher() {
                public void afterTextChanged(Editable s) {
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
            Commons.printException("" + e);
        }
    }

    public void loadSearchedList() {
        if (mEdtSearchproductName.getText().length() >= 3) {
            Vector<LoadManagementBO> items = vanlist;

            if (items == null) {
                bmodel.showAlert(
                        getResources().getString(R.string.no_products_exists),
                        0);
                return;
            }
            int siz = items.size();
            ArrayList<LoadManagementBO> mylist = new ArrayList<>();
            String mSelectedFilter = bmodel.getProductFilter();
            for (int i = 0; i < siz; ++i) {
                LoadManagementBO ret = items.elementAt(i);
                if (ret.getIssalable() == 1) {
                    if ("BarCode".equals(mSelectedFilter)) {
                        if (ret.getBarcode() != null && ret.getBarcode()
                                .toLowerCase()
                                .contains(
                                        mEdtSearchproductName.getText().toString()
                                                .toLowerCase()))
                            mylist.add(ret);

                    } else if ("GCAS Code".equals(mSelectedFilter)) {
                        if (ret.getRField1() != null && ret.getRField1()
                                .toLowerCase()
                                .contains(
                                        mEdtSearchproductName.getText().toString()
                                                .toLowerCase()))
                            mylist.add(ret);

                    } else if ((getResources().getString(
                            R.string.product_name).equals(mSelectedFilter))) {
                        if (ret.getProductshortname() != null && ret.getProductshortname()
                                .toLowerCase()
                                .contains(
                                        mEdtSearchproductName.getText().toString()
                                                .toLowerCase()))
                            mylist.add(ret);
                    }
                }
            }
            mSchedule = new MyAdapter(mylist);
            lvwplist.setAdapter(mSchedule);

        } else {
            Toast.makeText(this,
                    getResources().getString(R.string.enter_atleast_3_letters),
                    Toast.LENGTH_SHORT).show();
        }
    }

    private void showProductSummaryDialog() {

        // get prompts.xml view
        LayoutInflater layoutInflater = LayoutInflater
                .from(ManualVanLoadActivity.this);
        final ViewGroup nullParent = null;

        View promptView = layoutInflater.inflate(
                R.layout.dialog_vanload_summary, nullParent, false);
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                ManualVanLoadActivity.this);
        alertDialogBuilder.setView(promptView);

        final TextView tvProductPrice = (TextView) promptView
                .findViewById(R.id.tv_product_price);
        final TextView tvReturnProductPrice = (TextView) promptView
                .findViewById(R.id.tv_returnprd_price);
        final TextView tvTotalPrice = (TextView) promptView
                .findViewById(R.id.tv_total_price);

        final EditText edtPrice = (EditText) promptView
                .findViewById(R.id.edt_price);
        android.view.inputmethod.InputMethodManager imm = (android.view.inputmethod.InputMethodManager) ManualVanLoadActivity.this
                .getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(edtPrice.getWindowToken(), 0);
        edtPrice.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence s, int start, int before,
                                      int count) {

            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count,
                                          int after) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                String qty = s.toString();
                if (!"".equals(qty)) {
                    bmodel.vanmodulehelper.setmVanLoadAmount(SDUtil
                            .convertToFloat(qty));

                }

            }
        });

        String tv = SDUtil.roundIt(
                bmodel.vanmodulehelper.calculateVanLoadProductPrice(), 2)
                + "";

        tvProductPrice.setText(tv);
        tv = bmodel.getOrderHeaderBO()
                .getRemainigValue() + "";
        tvReturnProductPrice.setText(tv);

        tv = SDUtil.roundIt(
                bmodel.vanmodulehelper.calculateVanLoadProductPrice()
                        + bmodel.getOrderHeaderBO().getRemainigValue(), 2)
                + "";
        tvTotalPrice.setText(tv);

        // setup a dialog window
        alertDialogBuilder
                .setCancelable(false)
                .setPositiveButton(R.string.ok,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                float totalPrice = SDUtil
                                        .convertToFloat(tvTotalPrice.getText()
                                                .toString());
                                if (bmodel.vanmodulehelper.getmVanLoadAmount() == totalPrice) {
                                    new SaveVanLoad().execute();

                                } else {
                                    Toast.makeText(ManualVanLoadActivity.this,
                                            "Amount Mis match",
                                            Toast.LENGTH_SHORT).show();
                                }
                            }
                        })
                .setNegativeButton(R.string.cancel,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });

        // create an alert dialog
        bmodel.applyAlertDialogTheme(alertDialogBuilder);

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
    public void updateMultiSelectionBrand(List<String> mFilterName,
                                          List<Integer> mFilterId) {

    }

    @Override
    public void updateMultiSelectionCategory(List<Integer> mCategory) {

    }

    @Override
    public void loadStartVisit() {

    }

    @Override
    public void updateFromFiveLevelFilter(Vector<LevelBO> mParentIdList) {

        filterlist = new ArrayList<>();
        for (LevelBO levelBO : mParentIdList) {
            for (LoadManagementBO loadMgtBO : vanlist) {
                if (loadMgtBO.getIssalable() == 1) {
                    if (levelBO.getProductID() == loadMgtBO.getParentid()) {
                        filterlist.add(loadMgtBO);
                    }
                }
            }
        }
        mSchedule = new MyAdapter(filterlist);
        lvwplist.setAdapter(mSchedule);

        mDrawerLayout.closeDrawers();
        // TO DO Auto-generated method stub

    }

    public void numberPressed(View vw) {
        if (returnProductDialog != null) {
            if (returnProductDialog.isShowing()) {
                returnProductDialog.numberPressed(vw);
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
                Button ed = (Button) findViewById(view.getId());
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

    @Override
    public void updateFromFiveLevelFilter(Vector<LevelBO> mParentIdList, HashMap<Integer, Integer> mSelectedIdByLevelId, ArrayList<Integer> mAttributeProducts, String mFilterText) {
        filterlist = new ArrayList<>();
        if (mAttributeProducts != null) {
            if (!mParentIdList.isEmpty()) {
                for (LevelBO levelBO : mParentIdList) {
                    for (LoadManagementBO productBO : vanlist) {
                        if (productBO.getIssalable() == 1) {
                            if (levelBO.getProductID() == productBO.getParentid()) {
                                // here we get all products mapped to parent id list, then that product will be added only if it is mapped to selected attribute
                                if (mAttributeProducts.contains(productBO.getProductid())) {
                                    filterlist.add(productBO);
                                }
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
            if (mParentIdList.size() > 0 && !mFilterText.isEmpty()) {
                for (LevelBO levelBO : mParentIdList) {
                    for (LoadManagementBO loadMgtBO : vanlist) {
                        if (loadMgtBO.getIssalable() == 1) {
                            if (levelBO.getProductID() == loadMgtBO.getParentid()) {
                                filterlist.add(loadMgtBO);
                            }
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
        mSchedule = new MyAdapter(filterlist);
        lvwplist.setAdapter(mSchedule);

        this.mSelectedIdByLevelId = mSelectedIdByLevelId;

        mDrawerLayout.closeDrawers();
    }

    class MyAdapter extends ArrayAdapter<LoadManagementBO> {
        ArrayList<LoadManagementBO> items;
        LoadManagementBO product;

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

            product = items.get(position);
            View row = convertView;
            try {
                if (row == null) {
                    LayoutInflater inflater = getLayoutInflater();
                    row = inflater.inflate(R.layout.van_load, parent, false);
                    holder = new ViewHolder();
                    holder.caseQty = (EditText) row
                            .findViewById(R.id.productqtyCases);
                    holder.pieceQty = (EditText) row
                            .findViewById(R.id.productqtyPieces);
                    holder.psname = (TextView) row
                            .findViewById(R.id.productName);
                    holder.psname.setMaxLines(bmodel.configurationMasterHelper.MAX_NO_OF_PRODUCT_LINES);
                    holder.outerQty = (EditText) row
                            .findViewById(R.id.outerproductqtyCases);
                    holder.sih = (TextView) row
                            .findViewById(R.id.stock_and_order_listview_sih);
                    holder.totQty = (TextView) row.findViewById(R.id.totalQty);
                    holder.listLayout = (LinearLayout) row.findViewById(R.id.inv_view_layout);
                    holder.rowLayout = (LinearLayout) row.findViewById(R.id.list_header_lty);

                    holder.psname.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
                    holder.sih.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
                    holder.totQty.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
                    holder.caseQty.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
                    holder.pieceQty.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
                    holder.outerQty.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));


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


//                    holder.totQty.setOnTouchListener(new OnTouchListener() {
//                        public boolean onTouch(View v, MotionEvent event) {
//                            if (totQtyflag) {
//                                totQtyflag = false;
//                                if (event.getAction() == MotionEvent.ACTION_DOWN) {
//                                    vanLoadDialog = new ManualVanLoadDialog(
//                                            ManualVanLoadActivity.this,
//                                            holder.productBO,
//                                            vanloadDismissListener);
//                                    vanLoadDialog.show();
//                                    vanLoadDialog.setCancelable(false);
//                                }
//                            }
//                            return false;
//                        }
//                    });
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

//
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
                                            batchEntryDialog = new ManualVanLoadBatchEntryDialog(
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
                            holder.caseQty.selectAll();
                            holder.caseQty.requestFocus();
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
                            holder.pieceQty.selectAll();
                            holder.pieceQty.requestFocus();
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
                            holder.outerQty.selectAll();
                            holder.outerQty.requestFocus();
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
                            if (!"".equals(qty)) {
                                holder.productBO.setOuterQty(SDUtil
                                        .convertToInt(qty));
                            }

                        }
                    });
                    holder.pieceQty.addTextChangedListener(new TextWatcher() {
                        public void afterTextChanged(Editable s) {

                            String qty = s.toString();
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
                holder.productBO = product;
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

            if (position % 2 == 0) {
                row.setBackgroundColor(ContextCompat.getColor(ManualVanLoadActivity.this, R.color.list_even_item_bg));
            } else {
                row.setBackgroundColor(ContextCompat.getColor(ManualVanLoadActivity.this, R.color.list_odd_item_bg));
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

    class SaveVanLoad extends AsyncTask<Integer, Integer, Boolean> {

        private AlertDialog.Builder builder;
        private AlertDialog alertDialog;

        protected void onPreExecute() {
            builder = new AlertDialog.Builder(ManualVanLoadActivity.this);

            customProgressDialog(builder, getResources().getString(R.string.loading));
            alertDialog = builder.create();
            alertDialog.show();

        }

        @Override
        protected Boolean doInBackground(Integer... params) {
            try {
                bmodel.vanmodulehelper.saveVanLoad(vanlist, selectedSubDepotId);
                // Clear the Values from the Objects after save in DB
                if (bmodel.configurationMasterHelper.SHOW_PRODUCTRETURN) {
                    bmodel.vanmodulehelper.clearBomReturnProductsTable();
                }
            } catch (Exception e) {
                Commons.printException("" + e);
                return Boolean.FALSE;
            }
            return Boolean.TRUE; // Return your real result here
        }

        protected void onProgressUpdate(Integer... progress) {

        }

        protected void onPostExecute(Boolean result) {
            // result is the value returned from doInBackground
            try {

                alertDialog.dismiss();
                Toast.makeText(ManualVanLoadActivity.this,
                        getResources().getString(R.string.saved_successfully),
                        Toast.LENGTH_SHORT).show();
                loadActivity = new Intent(ManualVanLoadActivity.this, HomeScreenActivity.class);
                if (isFromPlanning)
                    loadActivity.putExtra("menuCode", "MENU_PLANNING_SUB");
                else
                    loadActivity.putExtra("menuCode", "MENU_LOAD_MANAGEMENT");
                bmodel.moduleTimeStampHelper.saveModuleTimeStamp("Out");
                bmodel.moduleTimeStampHelper.setTid("");
                bmodel.moduleTimeStampHelper.setModuleCode("");
                startActivity(loadActivity);
                finish();
                overridePendingTransition(R.anim.trans_right_in, R.anim.trans_right_out);
            } catch (Exception e) {
                Commons.printException("" + e);
            }

        }

    }

    class LoadReturnProductDialog extends AsyncTask<Integer, Integer, Boolean> {

        private AlertDialog.Builder builder;
        private AlertDialog alertDialog;

        protected void onPreExecute() {
            builder = new AlertDialog.Builder(ManualVanLoadActivity.this);

            customProgressDialog(builder, getResources().getString(R.string.loading));
            alertDialog = builder.create();
            alertDialog.show();
        }

        @Override
        protected Boolean doInBackground(Integer... params) {
            try {
                bmodel.vanmodulehelper.setReturnQty();
                bmodel.productHelper.calculateOrderReturnValue();
            } catch (Exception e) {
                Commons.printException("" + e);
                return Boolean.FALSE;
            }
            return Boolean.TRUE; // Return your real result here
        }

        protected void onPostExecute(Boolean result) {
            // result is the value returned from doInBackground
            try {
                alertDialog.dismiss();
                returnProductDialog = new VanLoadReturnProductDialog(
                        ManualVanLoadActivity.this, ManualVanLoadActivity.this);
                returnProductDialog.show();
                returnProductDialog.setCancelable(false);

            } catch (Exception e) {
                Commons.printException("" + e);
            }

        }

    }

    class calculateLiability extends AsyncTask<Integer, Integer, Boolean> {

        private AlertDialog.Builder builder;
        private AlertDialog alertDialog;

        protected void onPreExecute() {
            builder = new AlertDialog.Builder(ManualVanLoadActivity.this);

            customProgressDialog(builder, getResources().getString(R.string.loading));
            alertDialog = builder.create();
            alertDialog.show();
        }

        @Override
        protected Boolean doInBackground(Integer... params) {
            try {
                bmodel.vanmodulehelper.setReturnQty();
                bmodel.productHelper.calculateOrderReturnValue();
                if (bmodel.configurationMasterHelper.SHOW_GROUPPRODUCTRETURN)
                    bmodel.productHelper.setGroupWiseReturnQty();
            } catch (Exception e) {
                Commons.printException("" + e);
                return Boolean.FALSE;
            }
            return Boolean.TRUE; // Return your real result here
        }

        protected void onPostExecute(Boolean result) {
            // result is the value returned from doInBackground
            try {

                alertDialog.dismiss();
                showProductSummaryDialog();
            } catch (Exception e) {
                Commons.printException("" + e);
            }

        }

    }
}