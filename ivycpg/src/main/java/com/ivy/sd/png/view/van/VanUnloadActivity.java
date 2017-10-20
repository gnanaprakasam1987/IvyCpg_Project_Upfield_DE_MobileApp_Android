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
import android.support.v7.app.ActionBarDrawerToggle;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
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
import android.widget.Toast;

import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.bo.LevelBO;
import com.ivy.sd.png.bo.LoadManagementBO;
import com.ivy.sd.png.commons.SDUtil;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.provider.ConfigurationMasterHelper;
import com.ivy.sd.png.util.Commons;
import com.ivy.sd.png.view.HomeScreenActivity;
import com.ivy.sd.png.view.ToolBarwithFilter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Vector;

public class VanUnloadActivity extends ToolBarwithFilter {
    private String tv;
    private MyAdapter mSchedule;
    private ArrayList<LoadManagementBO> filterlist;
    private Vector<LoadManagementBO> vanunloadlist;
    private TextView mTotalSihTV;
    private TextView mTotalPcsTV;
    private TextView mTotalCaseTV;
    private TextView mTotalOuterTV;
    private Button saveBtn;
    private LinearLayout sihLayout,caseLayout,outerLayout,pieceLayout;
    private LinearLayout ll;
    private Intent loadActivity;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final Intent i = getIntent();
        ll = (LinearLayout) findViewById(R.id.ListHeader);
        LayoutInflater layoutInflater = (LayoutInflater) VanUnloadActivity.this
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final ViewGroup nullParent = null;
        ll.addView(layoutInflater.inflate(
                R.layout.include_vanload_unload_header, nullParent));
        overridePendingTransition(R.anim.trans_left_in, R.anim.trans_left_out);
        bmodel = (BusinessModel) getApplicationContext();
        bmodel.setContext(this);

        findViewById(R.id.ll_value).setVisibility(View.GONE);
        findViewById(R.id.ll_lpc).setVisibility(View.GONE);
        totalValueText.setVisibility(View.GONE);
        lpcText.setVisibility(View.GONE);


        sihLayout = (LinearLayout)findViewById(R.id.ll_vanloadsih_lty);
        caseLayout = (LinearLayout) findViewById(R.id.ll_vanloadcase_lty);
        outerLayout = (LinearLayout) findViewById(R.id.ll_vanloadouter_lty);
        pieceLayout = (LinearLayout) findViewById(R.id.ll_vanloadpiece_lty);

        sihLayout.setVisibility(View.VISIBLE);
        caseLayout.setVisibility(View.VISIBLE);
        outerLayout.setVisibility(View.VISIBLE);
        pieceLayout.setVisibility(View.VISIBLE);

        mTotalSihTV = (TextView) findViewById(R.id.tv_unload_sih);
        mTotalCaseTV = (TextView) findViewById(R.id.tv_unload_total_case);
        mTotalOuterTV = (TextView) findViewById(R.id.tv_unload_total_outer);
        mTotalPcsTV = (TextView) findViewById(R.id.tv_unload_total_piece);
        saveBtn =(Button) findViewById(R.id.btn_next);
        saveBtn.setText(getResources().getString(R.string.save));

        ((TextView)findViewById(R.id.productListTitle)).setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
//        ((TextView)findViewById(R.id.batchnotitle)).setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));//not used in xml so commented
        ((TextView)findViewById(R.id.sihTitle)).setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
        ((TextView)findViewById(R.id.itemcasetitle)).setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
        ((TextView)findViewById(R.id.outeritemcasetitle)).setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
        ((TextView)findViewById(R.id.itempiecetitle)).setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));

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
        mDrawerToggle = new ActionBarDrawerToggle(VanUnloadActivity.this,
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
        if (getSupportActionBar() != null) {
            getSupportActionBar().setIcon(null);
        }
        vanunloadlist = new Vector<>();
        for (LoadManagementBO bo : bmodel.productHelper.getProducts()) {
            if (bo.getSih() > 0)
                vanunloadlist.add(bo);
        }

        try {
            if (bmodel.userMasterHelper.getUserMasterBO().getUserid() == 0) {
                Toast.makeText(this, "Session out. Login again.",
                        Toast.LENGTH_SHORT).show();
                finish();
            }
        } catch (Exception e) {
            Commons.printException("" + e);
            Toast.makeText(this, "Session out. Login again.",
                    Toast.LENGTH_SHORT).show();
            finish();
        }

        if (!bmodel.configurationMasterHelper.SHOW_ORDER_CASE) {
            findViewById(R.id.itemcasetitle).setVisibility(View.GONE);
            caseLayout.setVisibility(View.GONE);
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
            pieceLayout.setVisibility(View.GONE);
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
            outerLayout.setVisibility(View.GONE);
        }

        setActionBarTitle(i.getStringExtra("screentitle"));
        hideSpecialFilter();
        hideRemarksButton();
        hideShemeButton();
        hideLocationButton();
        sih_apply = true;
        supportInvalidateOptionsMenu();
        updatebrandtext("Brand", -1);



        saveBtn.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                onNextButtonClick();
            }
        });

    }


    public void loadProductList(){
       updategeneraltext(GENERAL);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        menu.findItem(R.id.menu_loc_filter).setVisible(false);

        menu.findItem(R.id.menu_fivefilter).setVisible(false);
        menu.findItem(R.id.menu_product_filter).setVisible(false);

        if (bmodel.configurationMasterHelper.IS_FIVE_LEVEL_FILTER)
            menu.findItem(R.id.menu_fivefilter).setVisible(true);
        else
            menu.findItem(R.id.menu_product_filter).setVisible(true);

        return super.onPrepareOptionsMenu(menu);
    }

    public void onBackButtonClick() {
        bmodel.moduleTimeStampHelper.saveModuleTimeStamp("Out");
        bmodel.moduleTimeStampHelper.setTid("");
        bmodel.moduleTimeStampHelper.setModuleCode("");
        finish();
        overridePendingTransition(R.anim.trans_right_in, R.anim.trans_right_out);
    }

    @Override
    public void updatebrandtext(String filtertext, int bid) {

        mDrawerLayout.closeDrawers();

        brandbutton = filtertext;
        productName.setText("");
        if (vanunloadlist == null) {
            bmodel.showAlert(
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

        mSchedule = new MyAdapter(list);
        lvwplist.setAdapter(mSchedule);
        updateTotalQtyDetails(list);

    }

    public void onNextButtonClick() {
        if (bmodel.vanunloadmodulehelper.hasVanunload()) {
            new SaveVanUnload().execute();

        } else {
            bmodel.showAlert(
                    getResources().getString(R.string.no_products_exists), 0);
        }
    }

    public void loadSearchedList() {
        if (mEdt_searchproductName.getText().length() >= 3) {
            Vector<LoadManagementBO> items = bmodel.productHelper.getProducts();

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
                if ("BarCode".equals(mSelectedFilter)) {
                    if (ret.getBarcode() != null && ret.getBarcode()
                            .toLowerCase()
                            .contains(
                                    mEdt_searchproductName.getText().toString()
                                            .toLowerCase()))
                        mylist.add(ret);

                } else if ("GCAS Code".equals(mSelectedFilter)) {
                    if (ret.getRField1() != null && ret.getRField1()
                            .toLowerCase()
                            .contains(
                                    mEdt_searchproductName.getText().toString()
                                            .toLowerCase()))
                        mylist.add(ret);

                } else if (getResources().getString(
                        R.string.product_name).equals(mSelectedFilter)) {
                    if (ret.getProductshortname() != null && ret.getProductshortname()
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
            Toast.makeText(this, "Enter atleast 3 letters.", Toast.LENGTH_SHORT)
                    .show();
        }
    }

    protected Dialog onCreateDialog(int id) {
        switch (id) {
            case 0:

                AlertDialog.Builder builder = new AlertDialog.Builder(VanUnloadActivity.this)
                        .setIcon(null)
                        .setCancelable(false)
                        .setTitle(
                                getResources().getString(
                                        R.string.saved_successfully))
                        .setPositiveButton("Ok",
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog,
                                                        int whichButton) {
                                        loadActivity = new Intent(VanUnloadActivity.this, HomeScreenActivity.class);
                                        loadActivity.putExtra("menuCode", "MENU_LOAD_MANAGEMENT");
                                        startActivity(loadActivity);
                                        bmodel.moduleTimeStampHelper.saveModuleTimeStamp("Out");
                                        bmodel.moduleTimeStampHelper.setTid("");
                                        bmodel.moduleTimeStampHelper.setModuleCode("");
                                        finish();
                                        overridePendingTransition(R.anim.trans_right_in, R.anim.trans_right_out);

                                    }
                                });
                bmodel.applyAlertDialogTheme(builder);
                break;
        }
        return null;

    }

    @Override
    public void updatefromFiveLevelFilter(Vector<LevelBO> parentidList) {

        filterlist = new ArrayList<>();
        for (LevelBO levelBO : parentidList) {
            for (LoadManagementBO productBO : vanunloadlist) {
                if (levelBO.getProductID() == productBO.getParentid()) {
                    filterlist.add(productBO);
                }
            }
        }

        mSchedule = new MyAdapter(filterlist);
        lvwplist.setAdapter(mSchedule);

        mDrawerLayout.closeDrawers();

    }

    @Override
    public void updatefromFiveLevelFilter(Vector<LevelBO> parentidList, HashMap<Integer, Integer> mSelectedIdByLevelId, ArrayList<Integer> mAttributeProducts, String filtertext) {

        filterlist = new ArrayList<>();
        if (mAttributeProducts != null) {
            if (!parentidList.isEmpty()) {
                for (LevelBO levelBO : parentidList) {
                    for (LoadManagementBO productBO : vanunloadlist) {
                        if (levelBO.getProductID() == productBO.getParentid()) {
                            // here we get all products mapped to parent id list, then that product will be added only if it is mapped to selected attribute
                            if (mAttributeProducts.contains(productBO.getProductid())) {
                                filterlist.add(productBO);
                            }
                        }
                    }
                }
            } else {
                for (int pid : mAttributeProducts) {
                    for (LoadManagementBO productBO : vanunloadlist) {
                        if (pid == productBO.getProductid()) {
                            filterlist.add(productBO);
                        }
                    }
                }
            }
        } else {
            if (parentidList.size() > 0 && !filtertext.equalsIgnoreCase("")) {
                for (LevelBO levelBO : parentidList) {
                    for (LoadManagementBO productBO : vanunloadlist) {
                        if (levelBO.getProductID() == productBO.getParentid()) {
                            filterlist.add(productBO);
                        }
                    }
                }
            } else {
                int bid = -1;
                for (LoadManagementBO productBO : vanunloadlist) {
                    if (bid == productBO.getParentid() || bid == -1) {

                        filterlist.add(productBO);
                    }
                }

            }
        }
        mSchedule = new MyAdapter(filterlist);
        lvwplist.setAdapter(mSchedule);
        updateTotalQtyDetails(filterlist);

        mDrawerLayout.closeDrawers();
        if (mSelectedIdByLevelId != null)
            super.mSelectedIdByLevelId = mSelectedIdByLevelId;
    }

    public void applySIH() {
        LoadManagementBO vanunloadbo;
        for (int i = 0; i < vanunloadlist.size(); i++) {
            vanunloadbo = vanunloadlist.get(i);
            vanunloadbo.setPieceqty(vanunloadbo.getStocksih());
        }
        mSchedule.notifyDataSetChanged();
    }

    private void updateTotalQtyDetails(ArrayList<LoadManagementBO> filterList) {
        int totalSih = 0;
        int totalPiece = 0;
        int totalCase = 0;
        int totelOuter = 0;

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

    class MyAdapter extends ArrayAdapter<LoadManagementBO> {
        ArrayList<LoadManagementBO> items;
        LoadManagementBO product;

        MyAdapter(ArrayList<LoadManagementBO> items) {
            super(VanUnloadActivity.this, R.layout.van_unload, items);
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

        @NonNull
        public View getView(int position, View convertView, @NonNull ViewGroup parent) {

            final ViewHolder holder;

            product = items.get(position);
            View row = convertView;
            if (row == null) {
                LayoutInflater inflater = getLayoutInflater();
                row = inflater.inflate(R.layout.van_unload, parent, false);
                holder = new ViewHolder();

                holder.listheaderLty = (LinearLayout)row.findViewById(R.id.van_unload_list_header);
                holder.caseQty = (EditText) row
                        .findViewById(R.id.productqtyCases);
                holder.pieceQty = (EditText) row
                        .findViewById(R.id.productqtyPieces);
                holder.psname = (TextView) row.findViewById(R.id.productName);
                holder.psname.setMaxLines(bmodel.configurationMasterHelper.MAX_NO_OF_PRODUCT_LINES);
                holder.outerQty = (EditText) row
                        .findViewById(R.id.productqtyouter);

                holder.sih = (TextView) row
                        .findViewById(R.id.stock_and_order_listview_sih);
                holder.batchno = (TextView) row
                        .findViewById(R.id.stock_and_order_listview_batchno);

                holder.psname.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.BOLD));
                holder.batchno.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.REGULAR));
                holder.sih.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.REGULAR));
                holder.caseQty.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.REGULAR));
                holder.outerQty.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.REGULAR));
                holder.pieceQty.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.REGULAR));


                if (!bmodel.configurationMasterHelper.SHOW_ORDER_CASE)
                    holder.caseQty.setVisibility(View.GONE);
                if (!bmodel.configurationMasterHelper.SHOW_ORDER_PCS)
                    holder.pieceQty.setVisibility(View.GONE);
                if (!bmodel.configurationMasterHelper.SHOW_OUTER_CASE)
                    holder.outerQty.setVisibility(View.GONE);



                holder.outerQty.addTextChangedListener(new TextWatcher() {

                    @Override
                    public void onTextChanged(CharSequence s, int start,
                                              int before, int count) {

                    }

                    @Override
                    public void beforeTextChanged(CharSequence s, int start,
                                                  int count, int after) {

                    }

                    @Override
                    public void afterTextChanged(Editable s) {
                        if (holder.productBO.getOuterSize() == 0) {
                            holder.outerQty.removeTextChangedListener(this);
                            holder.outerQty.setText("0");
                            holder.outerQty.addTextChangedListener(this);
                            return;
                        }
                        String qty = s.toString();

                        if (!"".equals(qty)) {
                            holder.productBO.setOuterQty(SDUtil
                                    .convertToInt(qty));
                            if(!"0".equals(qty)) {
                                int sum = (holder.productBO.getOuterQty() * holder.productBO
                                        .getOuterSize())
                                        + (holder.productBO.getPieceqty())
                                        + (holder.productBO.getCaseqty() * holder.productBO
                                        .getCaseSize());
                                if (sum > holder.productBO.getSih()) {
                                    Toast.makeText(
                                            VanUnloadActivity.this,
                                            String.format(
                                                    getResources().getString(
                                                            R.string.exceed),
                                                    holder.productBO.getSih()),
                                            Toast.LENGTH_SHORT).show();

                                    qty = qty.length() > 1 ? qty
                                            .substring(0, qty.length() - 1) : "0";
                                    holder.outerQty.setText(qty);
                                    holder.productBO.setOuterQty(SDUtil
                                            .convertToInt(qty));
                                }
                            }
                            updateTotalQtyDetails(items);
                        }

                    }
                });

                holder.pieceQty.addTextChangedListener(new TextWatcher() {
                    public void afterTextChanged(Editable s) {

                        String qty = s.toString();

                        if (!"".equals(qty)) {
                            holder.productBO.setPieceqty(SDUtil
                                    .convertToInt(qty));
                            if(!"0".equals(qty)) {
                                int sum = (holder.productBO.getOuterQty() * holder.productBO
                                        .getOuterSize())
                                        + (holder.productBO.getPieceqty())
                                        + (holder.productBO.getCaseqty() * holder.productBO
                                        .getCaseSize());
                                if (sum > holder.productBO.getSih()) {
                                    Toast.makeText(
                                            VanUnloadActivity.this,
                                            String.format(
                                                    getResources().getString(
                                                            R.string.exceed),
                                                    holder.productBO.getSih()),
                                            Toast.LENGTH_SHORT).show();

                                    qty = qty.length() > 1 ? qty
                                            .substring(0, qty.length() - 1) : "0";
                                    holder.pieceQty.setText(qty);
                                    holder.productBO.setPieceqty(SDUtil
                                            .convertToInt(qty));

                                }
                            }
                            updateTotalQtyDetails(items);
                        }
                    }

                    public void beforeTextChanged(CharSequence s, int start,
                                                  int count, int after) {
                    }

                    public void onTextChanged(CharSequence s, int start,
                                              int before, int count) {
                    }
                });

                holder.caseQty.addTextChangedListener(new TextWatcher() {
                    public void afterTextChanged(Editable s) {
                        if (holder.productBO.getCaseSize() == 0) {
                            holder.caseQty.removeTextChangedListener(this);
                            holder.caseQty.setText("0");
                            holder.caseQty.addTextChangedListener(this);
                            return;
                        }

                        String qty = s.toString();


                        if (!"".equals(qty)) {
                            holder.productBO.setCaseqty(SDUtil
                                    .convertToInt(qty));

                            if(!"0".equals(qty)) {
                                int sum = (holder.productBO.getOuterQty() * holder.productBO
                                        .getOuterSize())
                                        + (holder.productBO.getPieceqty())
                                        + (holder.productBO.getCaseqty() * holder.productBO
                                        .getCaseSize());
                                if (sum > holder.productBO.getSih()) {
                                    Toast.makeText(
                                            VanUnloadActivity.this,
                                            String.format(
                                                    getResources().getString(
                                                            R.string.exceed),
                                                    holder.productBO.getSih()),
                                            Toast.LENGTH_SHORT).show();

                                    qty = qty.length() > 1 ? qty
                                            .substring(0, qty.length() - 1) : "0";
                                    holder.caseQty.setText(qty);
                                    holder.productBO.setCaseqty(SDUtil
                                            .convertToInt(qty));
                                }
                            }

                            updateTotalQtyDetails(items);
                        }
                    }

                    public void beforeTextChanged(CharSequence s, int start,
                                                  int count, int after) {
                    }

                    public void onTextChanged(CharSequence s, int start,
                                              int before, int count) {
                    }
                });

                holder.caseQty.setOnTouchListener(new OnTouchListener() {
                    public boolean onTouch(View v, MotionEvent event) {
                        productName.setText(holder.pname);
                        QUANTITY = holder.caseQty;
                        int inType = holder.caseQty.getInputType();
                        holder.caseQty.setInputType(InputType.TYPE_NULL);
                        holder.caseQty.onTouchEvent(event);
                        holder.caseQty.setInputType(inType);
                        holder.caseQty.selectAll();
                        holder.caseQty.requestFocus();
                        inputManager.hideSoftInputFromWindow(
                                mEdt_searchproductName.getWindowToken(), 0);
                        return true;
                    }
                });
                holder.pieceQty.setOnTouchListener(new OnTouchListener() {
                    public boolean onTouch(View v, MotionEvent event) {
                        productName.setText(holder.pname);
                        QUANTITY = holder.pieceQty;
                        int inType = holder.pieceQty.getInputType();
                        holder.pieceQty.setInputType(InputType.TYPE_NULL);
                        holder.pieceQty.onTouchEvent(event);
                        holder.pieceQty.setInputType(inType);
                        holder.pieceQty.selectAll();
                        holder.pieceQty.requestFocus();
                        inputManager.hideSoftInputFromWindow(
                                mEdt_searchproductName.getWindowToken(), 0);
                        return true;
                    }
                });
                holder.outerQty.setOnTouchListener(new OnTouchListener() {

                    @Override
                    public boolean onTouch(View v, MotionEvent event) {
                        productName.setText(holder.pname);
                        QUANTITY = holder.outerQty;
                        int inType = holder.outerQty.getInputType();
                        holder.outerQty.setInputType(InputType.TYPE_NULL);
                        holder.outerQty.onTouchEvent(event);
                        holder.outerQty.setInputType(inType);
                        holder.outerQty.selectAll();
                        holder.outerQty.requestFocus();
                        inputManager.hideSoftInputFromWindow(
                                mEdt_searchproductName.getWindowToken(), 0);
                        return true;
                    }
                });
                row.setOnClickListener(new OnClickListener() {
                    public void onClick(View v) {
                        productName.setText(holder.pname);
                        QUANTITY = holder.caseQty;
                        holder.caseQty.selectAll();
                        holder.caseQty.requestFocus();

                        if (viewFlipper.getDisplayedChild() != 0) {
                            viewFlipper.setInAnimation(VanUnloadActivity.this,
                                    R.anim.in_from_left);
                            viewFlipper.setOutAnimation(VanUnloadActivity.this,
                                    R.anim.out_to_left);
                            viewFlipper.showPrevious();
                        }
                    }
                });


                row.setTag(holder);
            } else {
                holder = (ViewHolder) row.getTag();
            }

            holder.position = position;
            holder.productBO = product;
            holder.pname = product.getProductname();
            holder.psname.setText(product.getProductshortname());
            tv = product.getStocksih() + "";
            holder.sih.setText(tv);
            tv = product.getCaseqty() + "";
            holder.caseQty.setText(tv);
            tv = product.getPieceqty() + "";
            holder.pieceQty.setText(tv);
            tv = product.getOuterQty() + "";
            holder.outerQty.setText(tv);
            if (product.getBatchNo() != null&&!product.getBatchNo().trim().equals(""))  {
                tv = "Batch No: " + product.getBatchNo() + "";
                holder.batchno.setText(tv);
            } else {
                holder.batchno.setText("");
            }

            if (holder.productBO.getdUomid() == 0||!holder.productBO.isCaseMapped()) {
                holder.caseQty.setEnabled(false);
            } else {
                holder.caseQty.setEnabled(true);
            }
            if (holder.productBO.getdOuonid() == 0||!holder.productBO.isOuterMapped()) {
                holder.outerQty.setEnabled(false);
            } else {
                holder.outerQty.setEnabled(true);
            }
            if (holder.productBO.getPiece_uomid() == 0||!holder.productBO.isPieceMapped()) {
                holder.pieceQty.setEnabled(false);
            } else {
                holder.pieceQty.setEnabled(true);
            }

            if (position % 2 == 0) {
                row.setBackgroundColor(ContextCompat.getColor(VanUnloadActivity.this, R.color.list_even_item_bg));
            } else {
                row.setBackgroundColor(ContextCompat.getColor(VanUnloadActivity.this, R.color.list_odd_item_bg));
            }

            return row;
        }
    }

    class ViewHolder {
        LoadManagementBO productBO;

        int position;
        String pname;
        TextView psname;
        TextView sih;
        TextView batchno;
        EditText pieceQty;
        EditText caseQty;
        EditText outerQty;
        LinearLayout listheaderLty;
    }

    class SaveVanUnload extends AsyncTask<Integer, Integer, Boolean> {

        private AlertDialog.Builder builder;
        private AlertDialog alertDialog;

        protected void onPreExecute() {
            builder = new AlertDialog.Builder(VanUnloadActivity.this);

            bmodel.customProgressDialog(alertDialog, builder, VanUnloadActivity.this, getResources().getString(R.string.saving));
            alertDialog = builder.create();
            alertDialog.show();
        }

        @Override
        protected Boolean doInBackground(Integer... params) {
            try {
                bmodel.vanunloadmodulehelper.saveVanUnLoad(vanunloadlist);
                bmodel.vanunloadmodulehelper.UpdateSIH(vanunloadlist);


                // If unloading empty
                bmodel.vanunloadmodulehelper.updateEmptyReconilationTable(vanunloadlist);

            } catch (Exception e) {
                Commons.printException("" + e);
                return Boolean.FALSE;
            }
            return Boolean.TRUE;
        }

        protected void onProgressUpdate(Integer... progress) {

            // TO DO Auto-generated method stub
        }

        protected void onPostExecute(Boolean result) {
            alertDialog.dismiss();
            showDialog(0);

        }

    }

}
