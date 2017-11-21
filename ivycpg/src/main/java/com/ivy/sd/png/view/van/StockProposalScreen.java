package com.ivy.sd.png.view.van;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources.NotFoundException;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v7.app.ActionBarDrawerToggle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;

import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.bo.LevelBO;
import com.ivy.sd.png.bo.LoadManagementBO;
import com.ivy.sd.png.commons.SDUtil;
import com.ivy.sd.png.model.BrandDialogInterface;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.provider.ConfigurationMasterHelper;
import com.ivy.sd.png.util.Commons;
import com.ivy.sd.png.view.FilterFragment;
import com.ivy.sd.png.view.HomeScreenActivity;
import com.ivy.sd.png.view.ToolBarwithFilter;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Vector;

import static com.ivy.sd.png.asean.view.R.id.sihTitle;

public class StockProposalScreen extends ToolBarwithFilter implements
        BrandDialogInterface, OnEditorActionListener {
    private Vector<LoadManagementBO> stockPropVector;
    private ArrayList<LoadManagementBO> mylist;
    private Intent loadActivity;

    private Intent intent;

    private MyAdapter mSchedule;
    private boolean isCreditLimitExceedToast = false;
    private boolean isToastDisabled = false;
    private boolean isMaxExceedToast = false;
    private AlertDialog alertDialog;
    private int stdqtytotal, currentstdqty, currentouterqty, currentcaseqty, currentpcsqty;
    private int calculatedCaseQty, calculatedPieceQty, calculatedOuterQty;
    private double calculatedTotalvalue = 0;
    private LoadManagementBO stock;
    private String sihTitel, hvp3mTitlel, distInvTitle;
    private Button saveBtn;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        intent = getIntent();
        LinearLayout ll = (LinearLayout) findViewById(R.id.ListHeader);
        LayoutInflater layoutInflater = (LayoutInflater) StockProposalScreen.this
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        ll.addView(layoutInflater.inflate(
                R.layout.include_stock_proposal_list_header, ll, false));
        overridePendingTransition(R.anim.trans_left_in, R.anim.trans_left_out);
        bmodel = (BusinessModel) getApplicationContext();
        bmodel.setContext(this);


        ((TextView) findViewById(R.id.productname)).setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
        ((TextView) findViewById(R.id.itemcasetitle)).setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
        ((TextView) findViewById(R.id.outeritemcasetitle)).setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
        ((TextView) findViewById(R.id.itempiecetitle)).setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
        ((TextView) findViewById(R.id.unitpricetitle)).setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));


        AlertDialog.Builder builder = new AlertDialog.Builder(StockProposalScreen.this);

        customProgressDialog(builder, StockProposalScreen.this, getResources().getString(R.string.loading_data));
        alertDialog = builder.create();
        mDrawerToggle = new ActionBarDrawerToggle(StockProposalScreen.this,
                mDrawerLayout, /* DrawerLayout object */
                R.string.ok, /* "open drawer" description for accessibility */
                R.string.close /* "close drawer" description for accessibility */
        ) {
            public void onDrawerClosed(View view) {
                if (getSupportActionBar() != null) {
                    setScreenTitle(intent.getStringExtra("screentitle"));
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
        if (getSupportActionBar() != null) {
            getSupportActionBar().setIcon(null);
        }
        stockPropVector = bmodel.productHelper.getProducts();
        bmodel.stockProposalModuleHelper.loadInitiative();
        bmodel.stockProposalModuleHelper.loadSBDData();
        bmodel.stockProposalModuleHelper.loadPurchased();


        // On/Off order case and pcs
        if (bmodel.configurationMasterHelper.SHOW_SIH_SPLIT) {
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
                    Log.i("e", e.getMessage());
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
                    Log.i("e", e.getMessage());
                }
            }

            if (!bmodel.configurationMasterHelper.SHOW_ORDER_PCS) {
                findViewById(sihTitle).setVisibility(View.GONE);
            } else {
                try {
                    if (bmodel.labelsMasterHelper.applyLabels(findViewById(
                            sihTitle).getTag()) != null)
                        ((TextView) findViewById(sihTitle))
                                .setText(bmodel.labelsMasterHelper
                                        .applyLabels(findViewById(sihTitle)
                                                .getTag()));
                } catch (Exception e) {
                    Log.i("e", e.getMessage());
                }
            }
        } else {
            findViewById(R.id.sihCaseTitle).setVisibility(View.GONE);
            findViewById(R.id.sihOuterTitle).setVisibility(View.GONE);
            try {
                if (bmodel.labelsMasterHelper.applyLabels(findViewById(
                        sihTitle).getTag()) != null)
                    ((TextView) findViewById(sihTitle))
                            .setText(bmodel.labelsMasterHelper
                                    .applyLabels(findViewById(sihTitle)
                                            .getTag()));
            } catch (Exception e) {
                Log.i("e", e.getMessage());
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
                Log.i("e", e.getMessage());
            }
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
                Log.i("e", e.getMessage());
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
                Log.i("e", e.getMessage());
            }
        }
        if (!bmodel.configurationMasterHelper.SHOW_STOCK_SO)
            findViewById(R.id.hvp3mtitle).setVisibility(View.GONE);
        else
            try {
                if (bmodel.labelsMasterHelper.applyLabels(findViewById(
                        R.id.hvp3mtitle).getTag()) != null)
                    ((TextView) findViewById(R.id.hvp3mtitle))
                            .setText(bmodel.labelsMasterHelper
                                    .applyLabels(findViewById(R.id.hvp3mtitle)
                                            .getTag()));
            } catch (Exception e) {
                Log.i("e", e.getMessage());
            }
        if (!bmodel.configurationMasterHelper.STOCK_DIST_INV)
            findViewById(R.id.distinvTitle).setVisibility(View.GONE);
        else
            try {
                if (bmodel.labelsMasterHelper.applyLabels(findViewById(
                        R.id.distinvTitle).getTag()) != null)
                    ((TextView) findViewById(R.id.distinvTitle))
                            .setText(bmodel.labelsMasterHelper
                                    .applyLabels(findViewById(R.id.distinvTitle)
                                            .getTag()));
            } catch (Exception e) {
                Log.i("e", e.getMessage());
            }

        saveBtn = (Button) findViewById(R.id.btn_next);
        saveBtn.setText(getResources().getString(R.string.save));
        if (!bmodel.configurationMasterHelper.SHOW_UNIT_PRICE)
            findViewById(R.id.unitpricetitle).setVisibility(View.GONE);
        findViewById(R.id.lpc_title).setVisibility(View.GONE);
        findViewById(R.id.lcp).setVisibility(View.GONE);
        setActionBarTitle(intent.getStringExtra("screentitle"));
        if (!bmodel.configurationMasterHelper.SHOW_STKPRO_SPL_FILTER) {
            hideSpecialFilter();
        } else {
            isSpecialFilter_enabled = true;
            generalbutton = GENERAL;
        }
        hideRemarksButton();
        hideShemeButton();
        hideLocationButton();
        supportInvalidateOptionsMenu();
        updateBrandText("Brand", -1);
        if (bmodel.configurationMasterHelper.SHOW_SO_APPLY)
            so_apply = true;
        if (bmodel.configurationMasterHelper.SHOW_STD_QTY_APPLY)
            std_apply = true;

        saveBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                onNextButtonClick();
            }
        });

    }

    @Override
    public void updateBrandText(String mFilterText, int bid) {

        // Close the drawer
        mDrawerLayout.closeDrawers();

        // Change the Brand button Name
        brandbutton = mFilterText;

        // Consider generalbutton text if it is dependent filter.
        String generaltxt = generalbutton;

        productName.setText("");

        if (stockPropVector == null) {
            bmodel.showAlert(
                    getResources().getString(R.string.no_products_exists), 0);
            return;
        }
        int siz = stockPropVector.size();
        mylist = new ArrayList<>();

        for (int i = 0; i < siz; ++i) {
            LoadManagementBO ret = stockPropVector
                    .elementAt(i);
            if (ret.getIssalable() == 1) {
                if (isSpecialFilter_enabled) {
                    if (bid == ret.getParentid() || bid == -1) {

                        if (generaltxt.equals(mSbd) && ret.isRPS()) {
                            mylist.add(ret);
                        } else if (generaltxt.equals(mOrdered)
                                && (ret.getOrderedPcsQty() > 0
                                || ret.getOrderedCaseQty() > 0 || ret
                                .getOuterOrderedCaseQty() > 0)) {
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
                            mylist.add(ret);
                        } else if (generaltxt.equals(GENERAL)) {

                            mylist.add(ret);
                        } else if (generaltxt.equals(mInStock) && ret.getWsih() > 0) {
                            mylist.add(ret);
                        } else if (generaltxt.equals(mOnAllocation)
                                && ret.isAllocation() == 1) {
                            mylist.add(ret);
                        } else if (generaltxt.equals(mPromo) && ret.isPromo()) {
                            mylist.add(ret);
                        }
                    }
                } else {
                    if (bid == ret.getParentid() || bid == -1) {

                        mylist.add(ret);
                    }
                }
            }
        }

        // Filter name and product count in product name header

        mSchedule = new MyAdapter(mylist);
        lvwplist.setAdapter(mSchedule);
        supportInvalidateOptionsMenu();
    }

    @Override
    public void updateFromFiveLevelFilter(Vector<LevelBO> mParentIdList, HashMap<Integer, Integer> mSelectedIdByLevelId, ArrayList<Integer> mAttributeProducts, String mFilterText) {
        Commons.print("selected filter " + mParentIdList + ", " + mSelectedIdByLevelId + ", " + mAttributeProducts + ", " + mFilterText);

        mylist = new ArrayList<>();
        if (mAttributeProducts != null) {

            if (mParentIdList.size() > 0) {
                for (LevelBO levelBO : mParentIdList) {
                    for (LoadManagementBO productBO : stockPropVector) {
                        if (productBO.getIssalable() == 1) {
                            if (levelBO.getProductID() == productBO.getParentid()) {

                                // here we get all products mapped to parent id list, then that product will be added only if it is mapped to selected attribute
                                if (mAttributeProducts.contains(productBO.getProductid())) {
                                    mylist.add(productBO);
                                }
                            }
                        }
                    }
                }
            } else {
                for (int pid : mAttributeProducts) {
                    for (LoadManagementBO productBO : stockPropVector) {
                        if (productBO.getIssalable() == 1) {
                            if (pid == productBO.getProductid()) {
                                mylist.add(productBO);
                            }
                        }
                    }
                }
            }
        } else {
            if (mParentIdList.size() > 0 && !mFilterText.equalsIgnoreCase("")) {
                for (LevelBO levelBO : mParentIdList) {
                    for (LoadManagementBO productBO : stockPropVector) {
                        Commons.print("pdt id " + levelBO.getProductID() + ", " + productBO.getParentid());
                        if (productBO.getIssalable() == 1) {
                            if (levelBO.getProductID() == productBO.getParentid()) {
                                mylist.add(productBO);
                            }
                        }

                    }
                }

            } else {
                updateGeneralText(GENERAL);
            }
        }

        this.mSelectedIdByLevelId = mSelectedIdByLevelId;

        mDrawerLayout.closeDrawers();
        if (!mFilterText.equalsIgnoreCase("")) {
            refreshList();
            updateValue();
        }


    }

    public void refreshList() {
        lvwplist.setAdapter(new MyAdapter(mylist));
    }

    @Override
    protected void onResume() {
        supportInvalidateOptionsMenu();
        super.onResume();
    }

    public void onBackButtonClick() {

        if (hasStockProposalDone()) {
            onCreateDialog(0);
        } else {
            loadActivity = new Intent(StockProposalScreen.this, HomeScreenActivity.class);
            loadActivity.putExtra("menuCode", "MENU_LOAD_MANAGEMENT");
            startActivity(loadActivity);

            finish();
            overridePendingTransition(R.anim.trans_right_in, R.anim.trans_right_out);
        }
    }

    private boolean hasStockProposalDone() {
        int siz = stockPropVector.size();
        if (siz == 0)
            return false;
        for (int i = 0; i < siz; ++i) {
            LoadManagementBO product = stockPropVector
                    .get(i);
            if (product.getStkprocaseqty() > 0 || product.getStkpropcsqty() > 0
                    || product.getStkproouterqty() > 0)
                return true;
        }
        return false;
    }

    @Override
    public void onNextButtonClick() {
        if (hasStockProposalDone()) {
            if (bmodel.configurationMasterHelper.IS_MUST_STOCK
                    && !bmodel.stockProposalModuleHelper
                    .isMustStockFilled(stockPropVector)) {
                onCreateDialog(1);

            } else
                new SaveStockProposal().execute();
        } else {

            bmodel.showAlert("No items found.", 0);
        }
    }

    protected Dialog onCreateDialog(int id) {
        AlertDialog.Builder builder;
        switch (id) {
            case 0:
                builder = new AlertDialog.Builder(StockProposalScreen.this)
                        .setIcon(null)
                        .setCancelable(false)
                        .setTitle(
                                getResources()
                                        .getString(
                                                R.string.doyouwantgoback))
                        .setPositiveButton("Ok",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog,
                                                        int whichButton) {
                                        loadActivity = new Intent(StockProposalScreen.this, HomeScreenActivity.class);
                                        loadActivity.putExtra("menuCode", "MENU_LOAD_MANAGEMENT");
                                        startActivity(loadActivity);
                                        finish();
                                        overridePendingTransition(R.anim.trans_right_in, R.anim.trans_right_out);
                                    }
                                })
                        .setNegativeButton("Cancel",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog,
                                                        int whichButton) {
                                    }
                                });
                bmodel.applyAlertDialogTheme(builder);

                break;

            case 1:
                builder = new AlertDialog.Builder(StockProposalScreen.this)
                        .setIcon(null)
                        .setCancelable(false)
                        .setTitle(
                                getResources().getString(
                                        R.string.Please_fill_must_stock))
                        .setPositiveButton("Ok",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog,
                                                        int whichButton) {
                                    }
                                });
                bmodel.applyAlertDialogTheme(builder);
                break;
            case 2:
                builder = new AlertDialog.Builder(StockProposalScreen.this)
                        .setIcon(null)
                        .setCancelable(false)
                        .setTitle(
                                getResources()
                                        .getString(
                                                R.string.credit_limit_exceed_do_you_wish_to_apply_partially))
                        .setPositiveButton("Apply",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog,
                                                        int whichButton) {
                                        showProgDialog();
                                        doApplyStdQtyStuff(true);
                                        mSchedule.notifyDataSetChanged();
                                        doToast();
                                    }
                                })
                        .setNegativeButton("Cancel",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog,
                                                        int whichButton) {
                                    }
                                });
                bmodel.applyAlertDialogTheme(builder);
                break;

            case 3:
                builder = new AlertDialog.Builder(StockProposalScreen.this)
                        .setIcon(null)
                        .setCancelable(false)
                        .setTitle(
                                getResources()
                                        .getString(
                                                R.string.credit_limit_exceed_do_you_wish_to_apply_partially))
                        .setPositiveButton("Apply",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog,
                                                        int whichButton) {
                                        showProgDialog();
                                        doApplyStockQtyStuff(true);
                                        mSchedule.notifyDataSetChanged();
                                        doToast();
                                    }
                                })
                        .setNegativeButton("Cancel",
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

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case 0:
                if (resultCode == RESULT_OK) {
                    finish();
                }
        }
    }

    public void loadSearchedList() {
        try {
            if (mEdt_searchproductName.getText().length() >= 3) {
                Vector<LoadManagementBO> items = bmodel.productHelper
                        .getProducts();

                if (items == null) {
                    bmodel.showAlert(
                            getResources().getString(
                                    R.string.no_products_exists), 0);
                    return;
                }
                int siz = items.size();
                ArrayList<LoadManagementBO> mylist = new ArrayList<>();
                String mSelectedFilter = bmodel.getProductFilter();
                for (int i = 0; i < siz; ++i) {
                    LoadManagementBO ret = items
                            .elementAt(i);
                    if (ret.getIssalable() == 1) {
                        if (mSelectedFilter.equals(getResources().getString(
                                R.string.order_dialog_barcode))) {
                            if (ret.getBarcode() != null && ret.getBarcode()
                                    .toLowerCase()
                                    .contains(
                                            mEdt_searchproductName.getText()
                                                    .toString().toLowerCase()))
                                mylist.add(ret);

                        } else if (mSelectedFilter.equals(getResources().getString(
                                R.string.order_gcas))) {
                            if (ret.getRField1() != null && ret.getRField1()
                                    .toLowerCase()
                                    .contains(
                                            mEdt_searchproductName.getText()
                                                    .toString().toLowerCase()))
                                mylist.add(ret);

                        } else if (mSelectedFilter.equals(getResources().getString(
                                R.string.product_name))) {
                            if (ret.getProductshortname() != null && ret.getProductshortname()
                                    .toLowerCase()
                                    .contains(
                                            mEdt_searchproductName.getText()
                                                    .toString().toLowerCase()))
                                mylist.add(ret);
                        }
                    }


                }
                MyAdapter mSchedule = new MyAdapter(mylist);
                lvwplist.setAdapter(mSchedule);

            } else {
                Toast.makeText(this, "Enter atleast 3 letters.",
                        Toast.LENGTH_SHORT).show();
            }
        } catch (NotFoundException e) {
            Log.i("e", e.getMessage());
        }
    }

    @Override
    public void updateGeneralText(String mFilterText) {
        // set the spl filter name on the button for display
        generalbutton = mFilterText;
        updateBrandText(BRAND, -1);
    }

    private void generalFilterClickedFragment() {
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
            bundle.putString("filterName", GENERAL);
            bundle.putBoolean("isFormBrand", false);
            bundle.putString("isFrom", "stockproposal");
            bundle.putSerializable("filterContent",
                    bmodel.configurationMasterHelper
                            .getSpecialFilterList("STKPRO12"));

            // set Fragmentclass Arguments
            FilterFragment fragobj = new FilterFragment(mSelectedFilterMap);

            fragobj.setArguments(bundle);
            ft.replace(R.id.right_drawer, fragobj, "filter");
            ft.commit();
        } catch (Exception e) {
            Log.i("e", e.getMessage());
        }
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
        } else if (i == R.id.menu_spl_filter) {
            generalFilterClickedFragment();
            return true;
        } else if (i == R.id.menu_product_filter) {
            productFilterClickedFragment();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void applyStockSo() {

        isToastDisabled = true;
        if (bmodel.configurationMasterHelper.SHOW_STOCK_PRO_CREDIT_VALIDATION) {
            if (doApplyStockQtyCalculation() > bmodel.userMasterHelper
                    .getUserMasterBO().getCreditlimit()) {
                onCreateDialog(3);
            } else {
                showProgDialog();
                doApplyStockQtyStuff(false);
                mSchedule.notifyDataSetChanged();
                doToast();
            }
        } else {
            showProgDialog();
            doApplyStockQtyStuff(false);
            mSchedule.notifyDataSetChanged();
            doToast();
        }

    }

    private void doApplyStockQtyStuff(boolean isCreditLimitValidation) {

        LoadManagementBO stock;
        double tmpTotalvalue = 0;
        double previousTotal;
        for (int i = 0; i < mylist.size(); i++) {
            stock = mylist.get(i);
            if (bmodel.configurationMasterHelper.STOCK_MAX_VALID) {
                if (stock.getSuggestqty() > stock.getMaxQty())
                    stock.setStkpropcsqty(stock.getMaxQty());
                else
                    stock.setStkpropcsqty(stock.getSuggestqty());
                stock.setStkprocaseqty(0);
                stock.setStkproouterqty(0);
            } else {
                stock.setStkprocaseqty(0);
                stock.setStkproouterqty(0);
                stock.setStkpropcsqty(stock.getSuggestqty());
            }
            if (isCreditLimitValidation) {

                if (stock.getStkpropcsqty() > 0) {
                    previousTotal = tmpTotalvalue
                            + (stock.getStkpropcsqty() * stock.getBaseprice());
                    if (previousTotal > bmodel.userMasterHelper
                            .getUserMasterBO().getCreditlimit()) {
                        stock.setStkpropcsqty(0);
                    } else {
                        tmpTotalvalue = tmpTotalvalue
                                + (stock.getStkpropcsqty() * stock
                                .getBaseprice());
                    }
                }
            }
        }

    }

    private void showProgDialog() {

        if (alertDialog != null)
            alertDialog.show();
    }

    private void cancelProgDialog() {

        if (alertDialog != null)
            alertDialog.dismiss();
    }

    private double doApplyStockQtyCalculation() {
        LoadManagementBO stock;
        int calculatedPieceQty;
        double calculatedTotalvalue = 0;
        try {
            for (int i = 0; i < mylist.size(); i++) {
                stock = mylist.get(i);
                if (bmodel.configurationMasterHelper.STOCK_MAX_VALID) {
                    if (stock.getSuggestqty() > stock.getMaxQty())
                        calculatedPieceQty = stock.getMaxQty();
                    else
                        calculatedPieceQty = stock.getSuggestqty();
                } else {
                    calculatedPieceQty = stock.getSuggestqty();
                }
                if (calculatedPieceQty > 0) {
                    calculatedTotalvalue = calculatedTotalvalue
                            + calculatedPieceQty * stock.getBaseprice();
                }
            }
        } catch (Exception e) {
            Log.i("e", e.getMessage());
        }
        Commons.print("CalculatedTotalvalue >>>>>>" + calculatedTotalvalue);
        return calculatedTotalvalue;
    }

    private void doToast() {
        lvwplist.post(new Runnable() {
            @Override
            public void run() {
                Commons.print("Gor Run");
                subHandler();
                if (isMaxExceedToast)
                    Toast.makeText(
                            StockProposalScreen.this,
                            getResources().getString(
                                    R.string.exceed_allocation),
                            Toast.LENGTH_SHORT).show();
                if (isCreditLimitExceedToast)
                    Toast.makeText(
                            StockProposalScreen.this,
                            getResources().getString(
                                    R.string.exceeded_credit_limit),
                            Toast.LENGTH_SHORT).show();
                isToastDisabled = false;
                isMaxExceedToast = false;
                isCreditLimitExceedToast = false;
            }
        });

    }

    private void subHandler() {
        Handler sHandler = getHandler();
        sHandler.sendEmptyMessage(1);
    }

    /**
     * An example getter to provide it to some external class
     * or just use 'new MyHandler(this)' if you are using it internally.
     * If you only use it internally you might even want it as final member:
     * private final MyHandler mHandler = new MyHandler(this);
     */
    private Handler getHandler() {
        return new MyHandler(this);
    }


    public void applyStdQty() {

        isToastDisabled = true;
        if (bmodel.configurationMasterHelper.SHOW_STOCK_PRO_CREDIT_VALIDATION) {
            if (doStdQtyCalculation() > bmodel.userMasterHelper
                    .getUserMasterBO().getCreditlimit()) {
                onCreateDialog(2);
            } else {
                showProgDialog();
                doApplyStdQtyStuff(false);
                mSchedule.notifyDataSetChanged();
                doToast();
            }
        } else {
            showProgDialog();
            doApplyStdQtyStuff(false);
            mSchedule.notifyDataSetChanged();
            doToast();
        }

    }

    private void doApplyStdQtyStuff(boolean isCreditLimitValidation) {

        LoadManagementBO stock;
        int stdqtytotal, currentstdqty, currentouterqty, currentcaseqty, currentpcsqty;
        double tmpTotalvalue = 0;
        double previousTotal;
        for (int i = 0; i < mylist.size(); i++) {
            stock = mylist.get(i);
            currentstdqty = 0;
            currentouterqty = 0;
            currentcaseqty = 0;
            currentpcsqty = 0;
            stdqtytotal = (stock.getStdcase() * stock.getCaseSize())
                    + (stock.getStdouter() * stock.getOuterSize())
                    + stock.getStdpcs();
            if (bmodel.configurationMasterHelper.SHOW_CURRENT_STDQTY) {
                if (stdqtytotal > 0)
                    if (stdqtytotal - stock.getSih() < 0)
                        currentstdqty = 0;
                    else
                        currentstdqty = stdqtytotal - stock.getSih();
                if (stock.getCaseSize() > 0) {
                    currentcaseqty = currentstdqty / stock.getCaseSize();
                }
                if (stock.getOuterSize() > 0 && stock.getCaseSize() > 0) {
                    currentouterqty = (currentstdqty % stock.getCaseSize())
                            / stock.getOuterSize();

                    currentpcsqty = (currentstdqty % stock.getCaseSize())
                            % stock.getOuterSize();
                }
            }
            if (bmodel.configurationMasterHelper.STOCK_MAX_VALID
                    && bmodel.configurationMasterHelper.SHOW_VALIDATION_DIST_INV) {
                if (currentstdqty > 0) {
                    if (currentstdqty > stock.getSuggestqty()) {
                        stock.setStkprocaseqty(0);
                        stock.setStkproouterqty(0);
                        stock.setStkpropcsqty(stock.getSuggestqty());
                    } else {
                        stock.setStkprocaseqty(currentcaseqty);
                        stock.setStkproouterqty(currentouterqty);
                        stock.setStkpropcsqty(currentpcsqty);
                    }
                } else {
                    if (stdqtytotal > stock.getSuggestqty()) {
                        stock.setStkprocaseqty(0);
                        stock.setStkproouterqty(0);
                        stock.setStkpropcsqty(stock.getSuggestqty());
                    } else {
                        stock.setStkprocaseqty(stock.getStdcase());
                        stock.setStkproouterqty(stock.getStdouter());
                        stock.setStkpropcsqty(stock.getStdpcs());
                    }

                }

                if (currentstdqty > 0) {
                    if (currentstdqty > stock.getMaxQty()) {
                        stock.setStkprocaseqty(0);
                        stock.setStkproouterqty(0);
                        stock.setStkpropcsqty(stock.getMaxQty());
                    } else {
                        stock.setStkprocaseqty(currentcaseqty);
                        stock.setStkproouterqty(currentouterqty);
                        stock.setStkpropcsqty(currentpcsqty);
                    }

                } else {
                    if (stdqtytotal > stock.getMaxQty()) {
                        stock.setStkprocaseqty(0);
                        stock.setStkproouterqty(0);
                        stock.setStkpropcsqty(stock.getMaxQty());
                    } else {
                        stock.setStkprocaseqty(stock.getStdcase());
                        stock.setStkproouterqty(stock.getStdouter());
                        stock.setStkpropcsqty(stock.getStdpcs());
                    }
                }

            }
            if (bmodel.configurationMasterHelper.STOCK_MAX_VALID
                    || bmodel.configurationMasterHelper.SHOW_VALIDATION_DIST_INV) {
                if (bmodel.configurationMasterHelper.STOCK_MAX_VALID) {
                    if (bmodel.configurationMasterHelper.SHOW_CURRENT_STDQTY) {

                        if (currentstdqty > stock.getMaxQty()) {
                            stock.setStkprocaseqty(0);
                            stock.setStkproouterqty(0);
                            stock.setStkpropcsqty(stock.getMaxQty());
                        } else {
                            stock.setStkprocaseqty(currentcaseqty);
                            stock.setStkproouterqty(currentouterqty);
                            stock.setStkpropcsqty(currentpcsqty);
                        }
                    } else {
                        if (stdqtytotal > stock.getMaxQty()) {
                            stock.setStkprocaseqty(0);

                            stock.setStkpropcsqty(stock.getMaxQty());
                            stock.setStkproouterqty(0);
                        } else {
                            stock.setStkprocaseqty(stock.getStdcase());
                            stock.setStkproouterqty(stock.getStdouter());
                            stock.setStkpropcsqty(stock.getStdpcs());
                        }
                    }
                } else if (bmodel.configurationMasterHelper.SHOW_VALIDATION_DIST_INV) {
                    if (bmodel.configurationMasterHelper.SHOW_CURRENT_STDQTY) {

                        if (currentstdqty > stock.getSuggestqty()) {
                            stock.setStkprocaseqty(0);
                            stock.setStkproouterqty(0);
                            stock.setStkpropcsqty(stock.getSuggestqty());
                        } else {
                            stock.setStkprocaseqty(currentcaseqty);
                            stock.setStkproouterqty(currentouterqty);
                            stock.setStkpropcsqty(currentpcsqty);
                        }
                    } else {
                        if ((stdqtytotal > stock.getSuggestqty())) {
                            stock.setStkprocaseqty(0);

                            stock.setStkpropcsqty(stock.getSuggestqty());
                            stock.setStkproouterqty(0);
                        } else {
                            stock.setStkprocaseqty(stock.getStdcase());
                            stock.setStkproouterqty(stock.getStdouter());
                            stock.setStkpropcsqty(stock.getStdpcs());
                        }
                    }
                }
            } else {
                if (bmodel.configurationMasterHelper.SHOW_CURRENT_STDQTY) {
                    stock.setStkprocaseqty(currentcaseqty);
                    stock.setStkproouterqty(currentouterqty);
                    stock.setStkpropcsqty(currentpcsqty);
                } else {
                    stock.setStkprocaseqty(stock.getStdcase());
                    stock.setStkproouterqty(stock.getStdouter());
                    stock.setStkpropcsqty(stock.getStdpcs());
                }
            }
            if (isCreditLimitValidation) {
                if (stock.getStkpropcsqty() > 0) {
                    previousTotal = tmpTotalvalue
                            + (stock.getStkpropcsqty() * stock.getBaseprice());
                    if (previousTotal > bmodel.userMasterHelper
                            .getUserMasterBO().getCreditlimit()) {
                        stock.setStkpropcsqty(0);
                    } else {
                        tmpTotalvalue = tmpTotalvalue
                                + (stock.getStkpropcsqty() * stock
                                .getBaseprice());
                    }
                }
                if (stock.getStkproouterqty() > 0) {
                    previousTotal = tmpTotalvalue
                            + ((stock.getStkproouterqty() * stock
                            .getOuterSize()) * stock.getBaseprice());
                    if (previousTotal > bmodel.userMasterHelper
                            .getUserMasterBO().getCreditlimit()) {
                        stock.setStkproouterqty(0);
                    } else {
                        tmpTotalvalue = tmpTotalvalue
                                + ((stock.getStkproouterqty() * stock
                                .getOuterSize()) * stock.getBaseprice());
                    }
                }
                if (stock.getStkprocaseqty() > 0) {
                    previousTotal = tmpTotalvalue
                            + ((stock.getStkprocaseqty() * stock.getCaseSize()) * stock
                            .getBaseprice());
                    if (previousTotal > bmodel.userMasterHelper
                            .getUserMasterBO().getCreditlimit()) {
                        stock.setStkprocaseqty(0);
                    } else {
                        tmpTotalvalue = tmpTotalvalue
                                + ((stock.getStkprocaseqty() * stock
                                .getCaseSize()) * stock.getBaseprice());
                    }
                }
            }
        }

    }

    private void showCurrentSTDqty() {
        if (stdqtytotal > 0)
            if (stdqtytotal - stock.getSih() < 0)
                currentstdqty = 0;
            else
                currentstdqty = stdqtytotal - stock.getSih();
        if (stock.getCaseSize() > 0) {
            currentcaseqty = currentstdqty / stock.getCaseSize();
        }
        if (stock.getOuterSize() > 0 && stock.getCaseSize() > 0) {
            currentouterqty = (currentstdqty % stock.getCaseSize())
                    / stock.getOuterSize();

            currentpcsqty = (currentstdqty % stock.getCaseSize())
                    % stock.getOuterSize();
        }
    }

    private void maxValidAndDistInv() {
        if (currentstdqty > 0) {
            if (currentstdqty > stock.getMaxQty()) {
                calculatedCaseQty = 0;
                calculatedOuterQty = 0;
                calculatedPieceQty = stock.getMaxQty();
            } else {
                calculatedCaseQty = currentcaseqty;
                calculatedOuterQty = currentouterqty;
                calculatedPieceQty = currentpcsqty;
            }

        } else {
            if (stdqtytotal > stock.getMaxQty()) {
                calculatedCaseQty = 0;
                calculatedOuterQty = 0;
                calculatedPieceQty = stock.getMaxQty();
            } else {
                calculatedCaseQty = stock.getStdcase();
                calculatedOuterQty = stock.getStdouter();
                calculatedPieceQty = stock.getStdpcs();
            }
        }

    }

    private void stockMaxValid() {
        if (bmodel.configurationMasterHelper.SHOW_CURRENT_STDQTY) {

            if (currentstdqty > stock.getMaxQty()) {
                calculatedCaseQty = 0;
                calculatedOuterQty = 0;
                calculatedPieceQty = stock.getMaxQty();
            } else {
                calculatedCaseQty = currentcaseqty;
                calculatedOuterQty = currentouterqty;
                calculatedPieceQty = currentpcsqty;
            }
        } else {
            if (stdqtytotal > stock.getMaxQty()) {
                calculatedCaseQty = 0;
                calculatedOuterQty = 0;
                calculatedPieceQty = stock.getMaxQty();
            } else {
                calculatedCaseQty = stock.getStdcase();
                calculatedOuterQty = stock.getStdouter();
                calculatedPieceQty = stock.getStdpcs();
            }
        }
    }

    private void DistValidInv() {
        if (bmodel.configurationMasterHelper.SHOW_CURRENT_STDQTY) {

            if (currentstdqty > stock.getSuggestqty()) {
                calculatedCaseQty = 0;
                calculatedOuterQty = 0;
                calculatedPieceQty = stock.getSuggestqty();
            } else {
                calculatedCaseQty = currentcaseqty;
                calculatedOuterQty = currentouterqty;
                calculatedPieceQty = currentpcsqty;
            }
        } else {
            if ((stdqtytotal > stock.getSuggestqty())) {
                calculatedCaseQty = 0;
                calculatedOuterQty = 0;
                calculatedPieceQty = stock.getSuggestqty();

            } else {
                calculatedCaseQty = stock.getStdcase();
                calculatedOuterQty = stock.getStdouter();
                calculatedPieceQty = stock.getStdpcs();
            }
        }
    }

    private double doStdQtyCalculation() {
        try {
            for (int i = 0; i < mylist.size(); i++) {
                stock = mylist.get(i);
                currentstdqty = 0;
                currentouterqty = 0;
                currentcaseqty = 0;
                currentpcsqty = 0;
                calculatedCaseQty = 0;
                calculatedPieceQty = 0;
                calculatedOuterQty = 0;
                stdqtytotal = (stock.getStdcase() * stock.getCaseSize())
                        + (stock.getStdouter() * stock.getOuterSize())
                        + stock.getStdpcs();
                if (bmodel.configurationMasterHelper.SHOW_CURRENT_STDQTY) {
                    showCurrentSTDqty();
                }
                if (bmodel.configurationMasterHelper.STOCK_MAX_VALID
                        && bmodel.configurationMasterHelper.SHOW_VALIDATION_DIST_INV) {
                    maxValidAndDistInv();
                }
                if (bmodel.configurationMasterHelper.STOCK_MAX_VALID
                        || bmodel.configurationMasterHelper.SHOW_VALIDATION_DIST_INV) {
                    if (bmodel.configurationMasterHelper.STOCK_MAX_VALID) {
                        stockMaxValid();
                    } else if (bmodel.configurationMasterHelper.SHOW_VALIDATION_DIST_INV) {
                        DistValidInv();
                    }
                } else {
                    if (bmodel.configurationMasterHelper.SHOW_CURRENT_STDQTY) {
                        calculatedCaseQty = currentcaseqty;
                        calculatedOuterQty = currentouterqty;
                        calculatedPieceQty = currentpcsqty;
                    } else {
                        calculatedCaseQty = stock.getStdcase();
                        calculatedOuterQty = stock.getStdouter();
                        calculatedPieceQty = stock.getStdpcs();
                    }
                }

                if (calculatedCaseQty > 0 || calculatedOuterQty > 0
                        || calculatedPieceQty > 0) {
                    calculatedTotalvalue = calculatedTotalvalue
                            + ((calculatedCaseQty * stock.getCaseSize())
                            + calculatedPieceQty + (calculatedOuterQty * stock
                            .getOuterSize())) * stock.getBaseprice();

                }

            }
            Commons.print("CalculatedTotalvalue >>>>>>" + calculatedTotalvalue);
        } catch (Exception e) {
            Log.i("e", e.getMessage());
        }
        return calculatedTotalvalue;
    }

    private double updateValue() {
        int size = stockPropVector.size();
        LoadManagementBO stock;
        double temp, totalvalue = 0;
        for (int i = 0; i < size; i++) {
            stock = stockPropVector.get(i);
            if (stock.getStkprocaseqty() > 0 || stock.getStkproouterqty() > 0
                    || stock.getStkpropcsqty() > 0) {
                temp = ((stock.getStkprocaseqty() * stock.getCaseSize())
                        + stock.getStkpropcsqty() + (stock.getStkproouterqty() * stock
                        .getOuterSize())) * stock.getBaseprice();
                totalvalue = totalvalue + temp;
            }
        }
        totalValueText.setText(bmodel.formatValue(totalvalue) + "");
        return totalvalue;
    }

    //static inner class doesn't hold an implicit reference to the outer class
    private static class MyHandler extends Handler {
        //Using a weak reference means you won't prevent garbage collection
        private final WeakReference<StockProposalScreen> myClassWeakReference;

        public MyHandler(StockProposalScreen myClassInstance) {
            myClassWeakReference = new WeakReference<>(myClassInstance);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            try {
                if (msg.what == 1) {
                    Commons.print("Gor Handler");
                    myClassWeakReference.get().cancelProgDialog();
                }
            } catch (Exception e) {
                Log.i("e", e.getMessage());
            }
        }
    }

    class MyAdapter extends ArrayAdapter<LoadManagementBO> {
        private final ArrayList<LoadManagementBO> items;
        private LoadManagementBO stockProposalBO;

        public MyAdapter(ArrayList<LoadManagementBO> items) {
            super(StockProposalScreen.this, R.layout.row_stockproposal, items);
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

        public View getView(int position, View convertView, ViewGroup parent) {
            final ViewHolder holder;
            stockProposalBO = items.get(position);
            View row = convertView;
            if (row == null) {
                LayoutInflater inflater = getLayoutInflater();
                row = inflater.inflate(R.layout.row_stockproposal, parent,
                        false);
                holder = new ViewHolder();

                holder.pname = (TextView) row.findViewById(R.id.closePRODNAME);
                holder.newProposalQty = (EditText) row
                        .findViewById(R.id.productqtyCases);
                holder.newproposalpcsQty = (EditText) row
                        .findViewById(R.id.productqtyPieces);
                holder.proposalQty = (TextView) row
                        .findViewById(R.id.proposalQty);
                holder.outerQty = (EditText) row
                        .findViewById(R.id.outerproductqtyCases);

                holder.sih = (TextView) row
                        .findViewById(R.id.stock_and_order_listview_sih);
                holder.sihCase = (TextView) row
                        .findViewById(R.id.stock_and_order_listview_sih_case);
                holder.sihOuter = (TextView) row
                        .findViewById(R.id.stock_and_order_listview_sih_outer);
                holder.unitprice = (TextView) row.findViewById(R.id.unitprice);

                holder.distinv = (TextView) row
                        .findViewById(R.id.stock_and_order_listview_distinv);

                holder.pname.setMaxLines(bmodel.configurationMasterHelper.MAX_NO_OF_PRODUCT_LINES);

                holder.pname.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
                holder.newProposalQty.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
                holder.newproposalpcsQty.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
                holder.proposalQty.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
                holder.outerQty.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
                holder.sih.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
                holder.sihCase.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
                holder.sihOuter.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
                holder.unitprice.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
                holder.distinv.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));


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

                // On/Off order case and pce
                if (!bmodel.configurationMasterHelper.SHOW_ORDER_CASE)
                    holder.newProposalQty.setVisibility(View.GONE);
                if (!bmodel.configurationMasterHelper.SHOW_ORDER_PCS)
                    holder.newproposalpcsQty.setVisibility(View.GONE);
                if (!bmodel.configurationMasterHelper.SHOW_OUTER_CASE)
                    holder.outerQty.setVisibility(View.GONE);


                if (!bmodel.configurationMasterHelper.SHOW_STOCK_SO)
                    holder.proposalQty.setVisibility(View.GONE);

                if (!bmodel.configurationMasterHelper.STOCK_DIST_INV)
                    holder.distinv.setVisibility(View.GONE);

                if (!bmodel.configurationMasterHelper.SHOW_UNIT_PRICE)
                    holder.unitprice.setVisibility(View.GONE);
                holder.outerQty.addTextChangedListener(new TextWatcher() {

                    @Override
                    public void onTextChanged(CharSequence s, int start,
                                              int before, int count) {
                        // TODO Auto-generated method stub

                    }

                    @Override
                    public void beforeTextChanged(CharSequence s, int start,
                                                  int count, int after) {
                        // TODO Auto-generated method stub

                    }

                    @Override
                    public void afterTextChanged(Editable s) {
                        Commons.print("outer:" + holder.spbo.getOuterSize());
                        if (holder.spbo.getOuterSize() == 0) {
                            holder.outerQty.removeTextChangedListener(this);
                            holder.outerQty.setText("0");
                            holder.outerQty.addTextChangedListener(this);
                            return;
                        }
                        try {

                            int sum;
                            String qty = s.toString();
                            if (!qty.equals("")) {

                                holder.spbo.setStkproouterqty(SDUtil
                                        .convertToInt(qty));
                                sum = (holder.spbo.getStkprocaseqty() * holder.spbo
                                        .getCaseSize())
                                        + holder.spbo.getStkpropcsqty()
                                        + (holder.spbo.getStkproouterqty() * holder.spbo
                                        .getOuterSize());
                                holder.unitprice.setText(bmodel.formatValue(sum
                                        * holder.spbo.getBaseprice())
                                        + "");
                                if ((bmodel.configurationMasterHelper.STOCK_MAX_VALID || bmodel.configurationMasterHelper.SHOW_VALIDATION_DIST_INV)) {

                                    if ((bmodel.configurationMasterHelper.STOCK_MAX_VALID && sum > holder.spbo
                                            .getMaxQty())
                                            || (bmodel.configurationMasterHelper.SHOW_VALIDATION_DIST_INV && sum > holder.spbo
                                            .getSuggestqty())) {

                                        /**
                                         * Delete the last entered number and
                                         * reset the qty
                                         **/
                                        holder.outerQty
                                                .removeTextChangedListener(this);
                                        if (!isToastDisabled) {
                                            Toast.makeText(
                                                    StockProposalScreen.this,
                                                    String.format(
                                                            getResources()
                                                                    .getString(
                                                                            R.string.exceed),
                                                            holder.spbo
                                                                    .getMaxQty()),
                                                    Toast.LENGTH_SHORT).show();
                                        } else {
                                            isMaxExceedToast = true;
                                        }
                                        try {
                                            holder.spbo.setStkproouterqty(SDUtil
                                                    .convertToInt(qty.length() > 1 ? qty.substring(
                                                            0, qty.length() - 1)
                                                            : "0"));
                                            holder.outerQty.setText(qty
                                                    .length() > 1 ? qty
                                                    .substring(0,
                                                            qty.length() - 1)
                                                    : "0");
                                        } catch (Exception e) {
                                            holder.spbo.setStkproouterqty(0);
                                            holder.outerQty.setText("0");
                                        }
                                        updateValue();
                                        holder.outerQty
                                                .addTextChangedListener(this);
                                    } else {
                                        holder.spbo.setStkproouterqty(SDUtil
                                                .convertToInt(qty));
                                    }
                                } else {
                                    holder.spbo.setStkproouterqty(SDUtil
                                            .convertToInt(qty));
                                }

                                if (bmodel.configurationMasterHelper.SHOW_STOCK_PRO_CREDIT_VALIDATION) {
                                    if (updateValue() > bmodel.userMasterHelper
                                            .getUserMasterBO().getCreditlimit()) {
                                        holder.outerQty
                                                .removeTextChangedListener(this);
                                        try {
                                            holder.spbo.setStkproouterqty(SDUtil
                                                    .convertToInt(qty.length() > 1 ? qty.substring(
                                                            0, qty.length() - 1)
                                                            : "0"));
                                            holder.outerQty.setText(qty
                                                    .length() > 1 ? qty
                                                    .substring(0,
                                                            qty.length() - 1)
                                                    : "0");
                                            Commons.print("Outer QTY"
                                                    + holder.spbo
                                                    .getStkproouterqty());
                                        } catch (Exception e) {
                                            holder.spbo.setStkproouterqty(0);
                                            holder.outerQty.setText("0");
                                        }

                                        if (!isToastDisabled) {
                                            Toast.makeText(
                                                    StockProposalScreen.this,
                                                    getResources()
                                                            .getString(
                                                                    R.string.exceeded_credit_limit),
                                                    Toast.LENGTH_SHORT).show();
                                        } else {
                                            isCreditLimitExceedToast = true;
                                        }
                                        updateValue();
                                        holder.outerQty
                                                .addTextChangedListener(this);

                                    } else {
                                        holder.spbo.setStkproouterqty(SDUtil
                                                .convertToInt(qty));
                                        updateValue();

                                    }
                                } else {
                                    holder.spbo.setStkproouterqty(SDUtil
                                            .convertToInt(qty));
                                    updateValue();
                                }

                            }
                        } catch (Exception e) {
                            Log.i("e", e.getMessage());
                        }
                    }
                });

                holder.outerQty.setOnTouchListener(new OnTouchListener() {

                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        productName.setText(holder.Pname);
                        if (bmodel.configurationMasterHelper.DISABLE_MANUAL_ORDER
                                || (bmodel.configurationMasterHelper.MUST_STOCK_ONLY && holder.spbo
                                .getIsMust() == 0)) {
                            QUANTITY1 = null;
                            QUANTITY = null;
                        } else {
                            QUANTITY = holder.outerQty;
                            QUANTITY1 = holder.outerQty;
                            QUANTITY.setTag(holder.spbo);
                            QUANTITY1.setTag(holder.spbo);
                        }

                        int inType = holder.outerQty.getInputType(); // backup
                        holder.outerQty.setInputType(InputType.TYPE_NULL); // disable
                        holder.outerQty.onTouchEvent(event); // call
                        holder.outerQty.setInputType(inType); // restore
                        holder.outerQty.selectAll();
                        holder.outerQty.requestFocus(); // type
                        inputManager.hideSoftInputFromWindow(
                                mEdt_searchproductName.getWindowToken(), 0);
                        return true;
                    }
                });
                holder.newProposalQty.addTextChangedListener(new TextWatcher() {

                    public void afterTextChanged(Editable s) {
                        Commons.print("Case:" + holder.spbo.getCaseSize());
                        if (holder.spbo.getCaseSize() == 0) {
                            holder.newProposalQty
                                    .removeTextChangedListener(this);
                            holder.newProposalQty.setText("0");
                            holder.newProposalQty.addTextChangedListener(this);
                            return;
                        }

                        try {

                            int sum;
                            String qty = s.toString();
                            if (!qty.equals("")) {

                                holder.spbo.setStkprocaseqty(SDUtil
                                        .convertToInt(qty));
                                sum = (holder.spbo.getStkprocaseqty() * holder.spbo
                                        .getCaseSize())
                                        + holder.spbo.getStkpropcsqty()
                                        + (holder.spbo.getStkproouterqty() * holder.spbo
                                        .getOuterSize());
                                holder.unitprice.setText(bmodel.formatValue(sum
                                        * holder.spbo.getBaseprice())
                                        + "");
                                if ((bmodel.configurationMasterHelper.STOCK_MAX_VALID || bmodel.configurationMasterHelper.SHOW_VALIDATION_DIST_INV)) {

                                    if ((bmodel.configurationMasterHelper.STOCK_MAX_VALID && sum > holder.spbo
                                            .getMaxQty())
                                            || (bmodel.configurationMasterHelper.SHOW_VALIDATION_DIST_INV && sum > holder.spbo
                                            .getSuggestqty())) {

                                        /**
                                         * Delete the last entered number and
                                         * reset the qty
                                         **/
                                        holder.newProposalQty
                                                .removeTextChangedListener(this);
                                        if (!isToastDisabled) {
                                            Toast.makeText(
                                                    StockProposalScreen.this,
                                                    String.format(
                                                            getResources()
                                                                    .getString(
                                                                            R.string.exceed),
                                                            holder.spbo
                                                                    .getMaxQty()),
                                                    Toast.LENGTH_SHORT).show();
                                        } else {
                                            isMaxExceedToast = true;
                                        }
                                        try {
                                            holder.spbo.setStkprocaseqty(SDUtil
                                                    .convertToInt(qty.length() > 1 ? qty.substring(
                                                            0, qty.length() - 1)
                                                            : "0"));
                                            holder.newProposalQty.setText(qty
                                                    .length() > 1 ? qty
                                                    .substring(0,
                                                            qty.length() - 1)
                                                    : "0");
                                        } catch (Exception e) {
                                            holder.spbo.setStkprocaseqty(0);
                                            holder.newProposalQty.setText("0");
                                        }
                                        updateValue();
                                        holder.newProposalQty
                                                .addTextChangedListener(this);
                                    } else {
                                        holder.spbo.setStkprocaseqty(SDUtil
                                                .convertToInt(qty));
                                    }
                                } else {
                                    holder.spbo.setStkprocaseqty(SDUtil
                                            .convertToInt(qty));
                                }
                                if (bmodel.configurationMasterHelper.SHOW_STOCK_PRO_CREDIT_VALIDATION) {
                                    if (updateValue() > bmodel.userMasterHelper
                                            .getUserMasterBO().getCreditlimit()) {
                                        holder.newProposalQty
                                                .removeTextChangedListener(this);

                                        try {
                                            holder.spbo.setStkprocaseqty(SDUtil
                                                    .convertToInt(qty.length() > 1 ? qty.substring(
                                                            0, qty.length() - 1)
                                                            : "0"));
                                            holder.newProposalQty.setText(qty
                                                    .length() > 1 ? qty
                                                    .substring(0,
                                                            qty.length() - 1)
                                                    : "0");
                                            Commons.print("CASE QTY"
                                                    + holder.spbo
                                                    .getStkprocaseqty());
                                        } catch (Exception e) {
                                            holder.spbo.setStkprocaseqty(0);
                                            holder.newProposalQty.setText("0");
                                        }

                                        if (!isToastDisabled) {
                                            Toast.makeText(
                                                    StockProposalScreen.this,
                                                    getResources()
                                                            .getString(
                                                                    R.string.exceeded_credit_limit),
                                                    Toast.LENGTH_SHORT).show();
                                        } else {
                                            isCreditLimitExceedToast = true;
                                        }
                                        updateValue();
                                        holder.newProposalQty
                                                .addTextChangedListener(this);

                                    } else {
                                        holder.spbo.setStkprocaseqty(SDUtil
                                                .convertToInt(qty));
                                        updateValue();
                                    }
                                } else {
                                    holder.spbo.setStkprocaseqty(SDUtil
                                            .convertToInt(qty));
                                    updateValue();
                                }

                            }
                        } catch (Exception e) {
                            Log.i("e", e.getMessage());
                        }
                    }

                    public void beforeTextChanged(CharSequence s, int start,
                                                  int count, int after) {
                    }

                    public void onTextChanged(CharSequence s, int start,
                                              int before, int count) {
                    }
                });
                holder.newProposalQty.setOnTouchListener(new OnTouchListener() {
                    // @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        productName.setText(holder.Pname);
                        if (bmodel.configurationMasterHelper.DISABLE_MANUAL_ORDER
                                || (bmodel.configurationMasterHelper.MUST_STOCK_ONLY && holder.spbo
                                .getIsMust() == 0)) {
                            QUANTITY1 = null;
                            QUANTITY = null;
                        } else {
                            QUANTITY = holder.newProposalQty;
                            QUANTITY1 = holder.newProposalQty;
                            QUANTITY.setTag(holder.spbo);
                            QUANTITY1.setTag(holder.spbo);
                        }

                        int inType = holder.newProposalQty.getInputType(); // backup
                        holder.newProposalQty.setInputType(InputType.TYPE_NULL); // disable
                        holder.newProposalQty.onTouchEvent(event); // call
                        holder.newProposalQty.setInputType(inType); // restore
                        holder.newProposalQty.selectAll();
                        holder.newProposalQty.requestFocus(); // type
                        inputManager.hideSoftInputFromWindow(
                                mEdt_searchproductName.getWindowToken(), 0);
                        return true;
                    }
                });

                holder.newproposalpcsQty
                        .addTextChangedListener(new TextWatcher() {

                            public void afterTextChanged(Editable s) {
                                try {
                                    int sum;
                                    String qty = s.toString();
                                    if (!qty.equals("")) {
                                        holder.spbo.setStkpropcsqty(SDUtil
                                                .convertToInt(qty));
                                        sum = (holder.spbo.getStkprocaseqty() * holder.spbo
                                                .getCaseSize())
                                                + holder.spbo.getStkpropcsqty()
                                                + (holder.spbo
                                                .getStkproouterqty() * holder.spbo
                                                .getOuterSize());
                                        holder.unitprice.setText(bmodel
                                                .formatValue(sum
                                                        * holder.spbo
                                                        .getBaseprice())
                                                + "");
                                        if ((bmodel.configurationMasterHelper.STOCK_MAX_VALID || bmodel.configurationMasterHelper.SHOW_VALIDATION_DIST_INV)) {

                                            if ((bmodel.configurationMasterHelper.STOCK_MAX_VALID && sum > holder.spbo
                                                    .getMaxQty())
                                                    || (bmodel.configurationMasterHelper.SHOW_VALIDATION_DIST_INV && sum > holder.spbo
                                                    .getSuggestqty())) {

                                                /**
                                                 * Delete the last entered
                                                 * number and reset the qty
                                                 **/
                                                holder.newproposalpcsQty
                                                        .removeTextChangedListener(this);
                                                if (!isToastDisabled) {
                                                    Toast.makeText(
                                                            StockProposalScreen.this,
                                                            String.format(
                                                                    getResources()
                                                                            .getString(
                                                                                    R.string.exceed),
                                                                    holder.spbo
                                                                            .getMaxQty()),
                                                            Toast.LENGTH_SHORT)
                                                            .show();
                                                } else {
                                                    isMaxExceedToast = true;
                                                }
                                                try {
                                                    holder.spbo
                                                            .setStkpropcsqty(SDUtil
                                                                    .convertToInt(qty
                                                                            .length() > 1 ? qty
                                                                            .substring(
                                                                                    0,
                                                                                    qty.length() - 1)
                                                                            : "0"));
                                                    holder.newproposalpcsQty.setText(qty
                                                            .length() > 1 ? qty.substring(
                                                            0, qty.length() - 1)
                                                            : "0");
                                                    Commons.print("PCS QTY"
                                                            + holder.spbo
                                                            .getStkpropcsqty());
                                                } catch (Exception e) {
                                                    holder.spbo
                                                            .setStkpropcsqty(0);
                                                    holder.newproposalpcsQty
                                                            .setText("0");
                                                    Commons.print("PCS QTY e"
                                                            + holder.spbo
                                                            .getStkpropcsqty());
                                                }
                                                updateValue();
                                                holder.newproposalpcsQty
                                                        .addTextChangedListener(this);
                                            } else {
                                                holder.spbo.setStkpropcsqty(SDUtil
                                                        .convertToInt(qty));
                                            }
                                        } else {
                                            holder.spbo.setStkpropcsqty(SDUtil
                                                    .convertToInt(qty));
                                        }
                                        if (bmodel.configurationMasterHelper.SHOW_STOCK_PRO_CREDIT_VALIDATION) {
                                            if (updateValue() > bmodel.userMasterHelper
                                                    .getUserMasterBO()
                                                    .getCreditlimit()) {
                                                holder.newproposalpcsQty
                                                        .removeTextChangedListener(this);
                                                try {
                                                    holder.spbo
                                                            .setStkpropcsqty(SDUtil
                                                                    .convertToInt(qty
                                                                            .length() > 1 ? qty
                                                                            .substring(
                                                                                    0,
                                                                                    qty.length() - 1)
                                                                            : "0"));
                                                    holder.newproposalpcsQty.setText(qty
                                                            .length() > 1 ? qty.substring(
                                                            0, qty.length() - 1)
                                                            : "0");
                                                    Commons.print("PCS QTY"
                                                            + holder.spbo
                                                            .getStkpropcsqty());
                                                } catch (Exception e) {
                                                    holder.spbo
                                                            .setStkpropcsqty(0);
                                                    holder.newproposalpcsQty
                                                            .setText("0");
                                                    Commons.print("PCS QTY E"
                                                            + holder.spbo
                                                            .getStkpropcsqty());
                                                }
                                                if (!isToastDisabled) {
                                                    Toast.makeText(
                                                            StockProposalScreen.this,
                                                            getResources()
                                                                    .getString(
                                                                            R.string.exceeded_credit_limit),
                                                            Toast.LENGTH_SHORT)
                                                            .show();
                                                } else {
                                                    isCreditLimitExceedToast = true;
                                                }
                                                updateValue();
                                                holder.newproposalpcsQty
                                                        .addTextChangedListener(this);

                                            } else {
                                                holder.spbo.setStkpropcsqty(SDUtil
                                                        .convertToInt(qty));
                                                updateValue();
                                            }
                                        } else {
                                            holder.spbo.setStkpropcsqty(SDUtil
                                                    .convertToInt(qty));
                                            updateValue();
                                        }

                                    }
                                } catch (Exception e) {
                                    Log.i("e", e.getMessage());
                                }
                            }

                            public void beforeTextChanged(CharSequence s,
                                                          int start, int count, int after) {
                            }

                            public void onTextChanged(CharSequence s,
                                                      int start, int before, int count) {
                            }
                        });
                holder.newproposalpcsQty
                        .setOnTouchListener(new OnTouchListener() {
                            // @Override
                            public boolean onTouch(View v, MotionEvent event) {
                                productName.setText(holder.Pname);
                                if (bmodel.configurationMasterHelper.DISABLE_MANUAL_ORDER
                                        || (bmodel.configurationMasterHelper.MUST_STOCK_ONLY && holder.spbo
                                        .getIsMust() == 0)) {
                                    QUANTITY1 = null;
                                    QUANTITY = null;
                                } else {
                                    QUANTITY = holder.newproposalpcsQty;
                                    QUANTITY1 = holder.newproposalpcsQty;
                                    QUANTITY.setTag(holder.spbo);
                                    QUANTITY1.setTag(holder.spbo);
                                }

                                int inType = holder.newproposalpcsQty
                                        .getInputType(); // backup
                                holder.newproposalpcsQty
                                        .setInputType(InputType.TYPE_NULL); // disable
                                holder.newproposalpcsQty.onTouchEvent(event); // call
                                holder.newproposalpcsQty.setInputType(inType); // restore
                                holder.newproposalpcsQty.selectAll();
                                holder.newproposalpcsQty.requestFocus(); // type
                                inputManager.hideSoftInputFromWindow(
                                        mEdt_searchproductName.getWindowToken(),
                                        0);
                                return true;
                            }
                        });

                row.setOnClickListener(new OnClickListener() {
                    public void onClick(View v) {
                        productName.setText(holder.Pname);
                        if (bmodel.configurationMasterHelper.DISABLE_MANUAL_ORDER
                                || (bmodel.configurationMasterHelper.MUST_STOCK_ONLY && holder.spbo
                                .getIsMust() == 0)) {
                            QUANTITY1 = null;
                            QUANTITY = null;
                        } else {
                            QUANTITY = holder.newproposalpcsQty;
                            QUANTITY1 = holder.newproposalpcsQty;
                            QUANTITY.setTag(holder.spbo);
                            QUANTITY1.setTag(holder.spbo);
                        }

                        holder.newproposalpcsQty.selectAll();
                        holder.newproposalpcsQty.requestFocus();

                        if (viewFlipper.getDisplayedChild() != 0) {
                            viewFlipper.setInAnimation(
                                    StockProposalScreen.this,
                                    R.anim.in_from_left);
                            viewFlipper.setOutAnimation(
                                    StockProposalScreen.this,
                                    R.anim.out_to_left);
                            viewFlipper.showPrevious();
                            inputManager.hideSoftInputFromWindow(
                                    mEdt_searchproductName.getWindowToken(), 0);
                        }

                    }
                });
                row.setTag(holder);

            } else {
                holder = (ViewHolder) row.getTag();
            }
            holder.spbo = stockProposalBO;
            holder.Pname = holder.spbo.getProductname();
            holder.pname.setText(holder.spbo.getProductshortname());


            try {
                if (bmodel.labelsMasterHelper.applyLabels(holder.sih.getTag()) != null)
                    sihTitel = bmodel.labelsMasterHelper
                            .applyLabels(holder.sih.getTag()).toString();
                else
                    sihTitel = getResources().getString(R.string.sih);
            } catch (Exception e) {
                Log.i("e", e.getMessage());
                sihTitel = getResources().getString(R.string.sih);
            }
            try {
                if (bmodel.labelsMasterHelper.applyLabels(holder.proposalQty.getTag()) != null)
                    hvp3mTitlel = bmodel.labelsMasterHelper
                            .applyLabels(holder.proposalQty.getTag()).toString();

                else
                    hvp3mTitlel = getResources().getString(R.string.hvp3m);

            } catch (Exception e) {
                Log.i("e", e.getMessage());
                hvp3mTitlel = getResources().getString(R.string.hvp3m);
            }
            try {
                if (bmodel.labelsMasterHelper.applyLabels(holder.distinv.getTag()) != null)
                    distInvTitle = bmodel.labelsMasterHelper
                            .applyLabels(holder.distinv.getTag()).toString();
                else
                    distInvTitle = getResources().getString(R.string.dist_inv);

            } catch (Exception e) {
                Log.i("e", e.getMessage());
                distInvTitle = getResources().getString(R.string.dist_inv);
            }
            holder.proposalQty.setText(hvp3mTitlel + ": " + holder.spbo.getSuggestqty() + "");
            if (bmodel.configurationMasterHelper.SHOW_SIH_SPLIT)

            {
                if (bmodel.configurationMasterHelper.SHOW_ORDER_CASE
                        && bmodel.configurationMasterHelper.SHOW_OUTER_CASE
                        && bmodel.configurationMasterHelper.SHOW_ORDER_PCS) {
                    if (holder.spbo.getSih() == 0) {
                        holder.sihCase.setText(sihTitel + ": 0");
                        holder.sihOuter.setText("/0");
                        holder.sih.setText("/0");
                    } else if (holder.spbo.getCaseSize() == 0) {
                        holder.sihCase.setText(sihTitel + ":0");
                        if (holder.spbo.getOuterSize() == 0) {
                            holder.sihOuter.setText("/0");
                            holder.sih.setText("/" + holder.spbo.getSih() + "");
                        } else {
                            holder.sihOuter.setText("/" + holder.spbo.getSih()
                                    / holder.spbo.getOuterSize() + "");
                            holder.sih.setText("/" + holder.spbo.getSih()
                                    % holder.spbo.getOuterSize() + "");
                        }
                    } else {
                        holder.sihCase.setText(sihTitel + ": " + holder.spbo.getSih()
                                / holder.spbo.getCaseSize() + "");
                        if (holder.spbo.getOuterSize() > 0
                                && (holder.spbo.getSih() % holder.spbo
                                .getCaseSize()) >= holder.spbo
                                .getOuterSize()) {
                            holder.sihOuter
                                    .setText("/" + (holder.spbo.getSih() % holder.spbo
                                            .getCaseSize())
                                            / holder.spbo.getOuterSize() + "");
                            holder.sih
                                    .setText("/" + (holder.spbo.getSih() % holder.spbo
                                            .getCaseSize())
                                            % holder.spbo.getOuterSize() + "");
                        } else {
                            holder.sihOuter.setText("/0");
                            holder.sih.setText("/" + holder.spbo.getSih()
                                    % holder.spbo.getCaseSize() + "");
                        }
                    }
                } else if (bmodel.configurationMasterHelper.SHOW_ORDER_CASE
                        && bmodel.configurationMasterHelper.SHOW_OUTER_CASE) {
                    if (holder.spbo.getSih() == 0) {
                        holder.sihCase.setText(sihTitel + ": 0");
                        holder.sihOuter.setText("/0");
                    } else if (holder.spbo.getCaseSize() == 0) {
                        holder.sihCase.setText(sihTitel + ": 0");
                        if (holder.spbo.getOuterSize() == 0)
                            holder.sihOuter.setText("/0");
                        else
                            holder.sihOuter.setText("/ " + holder.spbo.getSih()
                                    / holder.spbo.getOuterSize() + "");
                    } else {
                        holder.sihCase.setText(sihTitel + ": " + holder.spbo.getSih()
                                / holder.spbo.getCaseSize() + "");
                        if (holder.spbo.getOuterSize() > 0
                                && (holder.spbo.getSih() % holder.spbo
                                .getCaseSize()) >= holder.spbo
                                .getOuterSize()) {
                            holder.sihOuter
                                    .setText("/" + (holder.spbo.getSih() % holder.spbo
                                            .getCaseSize())
                                            / holder.spbo.getOuterSize() + "");
                        } else {
                            holder.sihOuter.setText("/0");
                        }
                    }
                } else if (bmodel.configurationMasterHelper.SHOW_OUTER_CASE
                        && bmodel.configurationMasterHelper.SHOW_ORDER_PCS) {
                    if (holder.spbo.getSih() == 0) {
                        holder.sih.setText("/0");
                        holder.sihOuter.setText(sihTitel + ": 0");
                    } else if (holder.spbo.getOuterSize() == 0) {
                        holder.sih.setText("/" + holder.spbo.getSih() + "");
                        holder.sihOuter.setText(sihTitel + ":0");
                    } else {
                        holder.sihOuter.setText(sihTitel + ":" + holder.spbo.getSih()
                                / holder.spbo.getOuterSize() + "");
                        holder.sih.setText("/" + holder.spbo.getSih()
                                % holder.spbo.getOuterSize() + "");
                    }
                } else if (bmodel.configurationMasterHelper.SHOW_ORDER_CASE
                        && bmodel.configurationMasterHelper.SHOW_ORDER_PCS) {
                    if (holder.spbo.getSih() == 0) {
                        holder.sih.setText("/0");
                        holder.sihCase.setText(sihTitel + ": 0");
                    } else if (holder.spbo.getCaseSize() == 0) {
                        holder.sih.setText("/" + holder.spbo.getSih() + "");
                        holder.sihCase.setText(sihTitel + ": 0");
                    } else {
                        holder.sihCase.setText(sihTitel + ": " + holder.spbo.getSih()
                                / holder.spbo.getCaseSize() + "");
                        holder.sih.setText("/" + holder.spbo.getSih()
                                % holder.spbo.getCaseSize() + "");
                    }
                } else if (bmodel.configurationMasterHelper.SHOW_ORDER_CASE) {
                    if (holder.spbo.getSih() == 0)
                        holder.sihCase.setText(sihTitel + ": 0");
                    else if (holder.spbo.getCaseSize() == 0)
                        holder.sihCase.setText(sihTitel + ": 0");
                    else
                        holder.sihCase.setText(sihTitel + ": " + holder.spbo.getSih()
                                / holder.spbo.getCaseSize() + "");
                } else if (bmodel.configurationMasterHelper.SHOW_OUTER_CASE) {
                    if (holder.spbo.getSih() == 0)
                        holder.sihOuter.setText(sihTitel + ": 0");
                    else if (holder.spbo.getOuterSize() == 0)
                        holder.sihOuter.setText(sihTitel + ": 0");
                    else
                        holder.sihOuter.setText(sihTitel + ":" + holder.spbo.getSih()
                                / holder.spbo.getOuterSize() + "");
                } else if (bmodel.configurationMasterHelper.SHOW_ORDER_PCS) {
                    holder.sih.setText(sihTitel + ":" + holder.spbo.getSih() + "");
                }
            } else
                holder.sih.setText(sihTitel + ":" + holder.spbo.getSih() + "");
            holder.newProposalQty.setText(holder.spbo.getStkprocaseqty() + "");
            holder.newproposalpcsQty
                    .setText(holder.spbo.getStkpropcsqty() + "");
            holder.outerQty.setText(holder.spbo.getStkproouterqty() + "");
            holder.distinv.setText(distInvTitle + ": " + holder.spbo.getWsih() + "");
            double unitprice = ((holder.spbo.getStkprocaseqty() * holder.spbo
                    .getCaseSize()) + holder.spbo.getStkpropcsqty() + (holder.spbo
                    .getStkproouterqty() * holder.spbo.getOuterSize()))
                    * holder.spbo.getBaseprice();
            holder.unitprice.setText(bmodel.formatValue(unitprice) + "");

            // Disable the User Entry if UomID is Zero
            if (holder.spbo.getPiece_uomid() == 0 || !holder.spbo.isPieceMapped())

            {
                holder.newproposalpcsQty.setEnabled(false);
            } else

            {
                holder.newproposalpcsQty.setEnabled(true);
            }
            if (holder.spbo.getdUomid() == 0 || !holder.spbo.isCaseMapped())

            {
                holder.newProposalQty.setEnabled(false);
            } else

            {
                holder.newProposalQty.setEnabled(true);
            }
            if (holder.spbo.getdOuonid() == 0 || !holder.spbo.isOuterMapped())

            {
                holder.outerQty.setEnabled(false);
            } else

            {
                holder.outerQty.setEnabled(true);
            }

            if (position % 2 == 0) {
                row.setBackgroundColor(ContextCompat.getColor(StockProposalScreen.this, R.color.list_even_item_bg));
            } else {
                row.setBackgroundColor(ContextCompat.getColor(StockProposalScreen.this, R.color.list_odd_item_bg));
            }
            return (row);
        }
    }

    class ViewHolder {
        private LoadManagementBO spbo;
        private TextView pname, proposalQty, sih, sihCase, sihOuter, unitprice,
                distinv;
        private EditText newProposalQty, newproposalpcsQty, outerQty;
        private String Pname;
    }

    class SaveStockProposal extends AsyncTask<Integer, Integer, Boolean> {

        private AlertDialog.Builder builder;
        private AlertDialog alertDialog;

        protected void onPreExecute() {

            builder = new AlertDialog.Builder(StockProposalScreen.this);

            customProgressDialog(builder, StockProposalScreen.this, getResources().getString(R.string.loading));
            alertDialog = builder.create();
            alertDialog.show();
        }

        @Override
        protected Boolean doInBackground(Integer... params) {
            try {

                bmodel.stockProposalModuleHelper
                        .saveStockProposal(stockPropVector);
            } catch (Exception e) {
                Log.i("e", e.getMessage());
                return Boolean.FALSE;
            }
            return Boolean.TRUE; // Return your real result here
        }

        protected void onProgressUpdate(Integer... progress) {

        }

        protected void onPostExecute(Boolean result) {
            // result is the value returned from doInBackground
            alertDialog.dismiss();

            finish();
            overridePendingTransition(R.anim.trans_right_in, R.anim.trans_right_out);
            Toast.makeText(StockProposalScreen.this,
                    getResources().getString(R.string.saved_successfully),
                    Toast.LENGTH_SHORT).show();
        }

    }
}
