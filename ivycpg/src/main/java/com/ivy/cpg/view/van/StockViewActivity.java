package com.ivy.cpg.view.van;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarDrawerToggle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
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
import com.ivy.sd.png.view.HomeScreenActivity;
import com.ivy.sd.png.view.ToolBarwithFilter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Vector;

public class StockViewActivity extends ToolBarwithFilter implements
        BrandDialogInterface, OnEditorActionListener {
    private ArrayList<LoadManagementBO> filterlist;
    private ArrayList<LoadManagementBO> mylist;
    private Vector<LoadManagementBO> mylist2;
    private ArrayList<LoadManagementBO> childList = null;
    private HashMap<String, ArrayList<LoadManagementBO>> listDataChild;
    private ExpandableListAdapter expandableListAdapter;
    private boolean isExpandList = false;
    private Intent loadActivity;
    private boolean isFromPlanning = false;
    private boolean isOutersize;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final Intent i = getIntent();
        LinearLayout ll = (LinearLayout) findViewById(R.id.ListHeader);
        LayoutInflater layoutInflater = (LayoutInflater) StockViewActivity.this
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final ViewGroup nullParent = null;
        ll.addView(layoutInflater.inflate(R.layout.include_stockview_header,
                nullParent, false));
        overridePendingTransition(R.anim.trans_left_in, R.anim.trans_left_out);
        bmodel = (BusinessModel) getApplicationContext();
        bmodel.setContext(this);
        findViewById(R.id.keypad).setVisibility(View.GONE);
        footerLty.setVisibility(View.GONE);
        lvwplist.setVisibility(View.GONE);
        expandlvwplist.setVisibility(View.VISIBLE);

        ((TextView) findViewById(R.id.product_name)).setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
        ((TextView) findViewById(R.id.sihCaseTitle)).setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
        ((TextView) findViewById(R.id.sihOuterTitle)).setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
        ((TextView) findViewById(R.id.sihTitle)).setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));

        isFromPlanning = getIntent().getBooleanExtra("planingsub", false);
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

        mDrawerToggle = new ActionBarDrawerToggle(StockViewActivity.this,
                mDrawerLayout,
                R.string.ok,
                R.string.close
        ) {
            public void onDrawerClosed(View view) {
                if (getSupportActionBar() != null) {
                    getSupportActionBar().setTitle(i.getStringExtra("screentitle"));
                }
                supportInvalidateOptionsMenu();
            }

            public void onDrawerOpened(View drawerView) {
                if (getSupportActionBar() != null) {
                    getSupportActionBar().setTitle("Filter");
                }
                supportInvalidateOptionsMenu();
            }
        };

        mDrawerLayout.setDrawerListener(mDrawerToggle);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setIcon(null);
        }
        mylist = new ArrayList<>(bmodel.productHelper.getProducts());
        Commons.print("stock view oncreate," + String.valueOf(mylist.size()));

        /** Load products from product master **/
//        LoadManagementBO lbo;
//        mylist2 = new Vector<>();
//        for (int j = 0; j < bmodel.productHelper.getProducts().size(); j++) {
//            lbo = bmodel.productHelper.getProducts().get(j);
//            if (lbo.getStocksih() > 0)
//                mylist2.add(lbo);
//        }


        setActionBarTitle(i.getStringExtra("screentitle"));
        hideSpecialFilter();
        hideNextButton();
        hideRemarksButton();
        hideShemeButton();
        hideLocationButton();
        updateBrandText("Brand", -1);

    }


    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        // Change color if Filter is selected
        if (!generalbutton.equals(GENERAL))
            menu.findItem(R.id.menu_spl_filter).setIcon(
                    R.drawable.ic_action_star_select);
        if (!brandbutton.equals(BRAND))
            menu.findItem(R.id.menu_product_filter).setIcon(
                    R.drawable.ic_action_filter_select);


        menu.findItem(R.id.menu_loc_filter).setVisible(false);
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();
        if (id == android.R.id.home) {
            loadActivity = new Intent(StockViewActivity.this, HomeScreenActivity.class);
            if (isFromPlanning)
                loadActivity.putExtra("menuCode", "MENU_PLANNING_SUB");
            else
                loadActivity.putExtra("menuCode", "MENU_LOAD_MANAGEMENT");
            startActivity(loadActivity);
            finish();
        } else if (id == R.id.menu_expand) {
            if (!isExpandList) {
                for (int i = 0; i < expandableListAdapter.getGroupCount(); i++) {
                    expandlvwplist.expandGroup(i);
                }
                isExpandList = true;
            } else {
                for (int i = 0; i < expandableListAdapter.getGroupCount(); i++) {
                    expandlvwplist.collapseGroup(i);
                }
                isExpandList = false;
            }

        }

        return super.onOptionsItemSelected(item);
    }

    private void handleMenuIcon(Menu menu) {

    }

    @Override
    public void updateBrandText(String mFilterText, int bid) {

        mDrawerLayout.closeDrawers();

        brandbutton = mFilterText;

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

        listDataChild = new HashMap<>();

        for (LoadManagementBO parentBo : temp) {
            childList = new ArrayList<LoadManagementBO>();
            for (LoadManagementBO childBO : temp2) {
                if (parentBo.getProductid() == childBO.getProductid()
                        && childBO.getBatchlist() != null && !childBO.getBatchId().isEmpty())
                    childList.add(childBO);
            }
            String pid = String.valueOf(parentBo.getProductid());
            listDataChild.put(pid, childList);//load child batch List data
        }

        if (childList != null)
            if (childList.size() > 0)
                showExpandButton();
//---------- remove duplicate product name from given list-----------//

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
    public void onBackButtonClick() {
        finish();
        overridePendingTransition(R.anim.trans_right_in, R.anim.trans_right_out);
    }

    public void loadSearchedList() {

        if (mEdt_searchproductName.getText().length() >= 3) {

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
            listDataChild = new HashMap<>();

            for (LoadManagementBO parentBo : temp) {
                childList = new ArrayList<>();
                for (LoadManagementBO childBO : temp) {
                    if (parentBo.getProductid() == childBO.getProductid()
                            && childBO.getBatchlist() != null && !childBO.getBatchId().isEmpty())
                        childList.add(childBO);
                }
                String pid = String.valueOf(parentBo.getProductid());

                listDataChild.put(pid, childList);//load child batch List data
            }

//---------- remove duplicate product name from given list-----------///

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

        } else {
            Toast.makeText(this, "Enter atleast 3 letters.", Toast.LENGTH_SHORT)
                    .show();
        }
    }

    @Override
    public void updateFromFiveLevelFilter(Vector<LevelBO> mParentIdList) {

        filterlist = new ArrayList<>();
        for (LevelBO levelBO : mParentIdList) {
            for (LoadManagementBO productBO : mylist) {
                if (levelBO.getProductID() == productBO.getParentid()) {
                    if (productBO.getSih() > 0)
                        filterlist.add(productBO);
                }
            }
        }

        listDataChild = new HashMap<>();
        for (LoadManagementBO parentBo : filterlist) {
            childList = new ArrayList<>();
            for (LoadManagementBO childBO : filterlist) {
                if (parentBo.getProductid() == childBO.getProductid()
                        && childBO.getBatchlist() != null && !childBO.getBatchId().isEmpty())
                    childList.add(childBO);
            }
            String pid = String.valueOf(parentBo.getProductid());

            listDataChild.put(pid, childList);//load child batch List data
        }

//---------- remove duplicate product name from given list-----------///

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

    }

    @Override
    public void updateFromFiveLevelFilter(Vector<LevelBO> mParentIdList, HashMap<Integer, Integer> mSelectedIdByLevelId, ArrayList<Integer> mAttributeProducts, String mFilterText) {
        filterlist = new ArrayList<>();
        if (mAttributeProducts != null) {
            if (!mParentIdList.isEmpty()) {
                for (LevelBO levelBO : mParentIdList) {
                    for (LoadManagementBO productBO : mylist) {
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
                    for (LoadManagementBO productBO : mylist) {
                        if (pid == productBO.getProductid() && productBO.getSih() > 0) {
                            filterlist.add(productBO);
                        }
                    }
                }
            }
        } else {
            if (mParentIdList.size() > 0 && !mFilterText.equalsIgnoreCase("")) {
                for (LevelBO levelBO : mParentIdList) {
                    for (LoadManagementBO productBO : mylist) {
                        if (levelBO.getProductID() == productBO.getParentid()) {

                            if (productBO.getSih() > 0)
                                filterlist.add(productBO);
                        }
                    }
                }
            } else {
                for (LoadManagementBO productBO : mylist) {
                    if (productBO.getSih() > 0)
                        filterlist.add(productBO);
                }
            }
        }

        listDataChild = new HashMap<>();
        for (LoadManagementBO parentBo : filterlist) {
            childList = new ArrayList<>();
            for (LoadManagementBO childBO : filterlist) {
                if (parentBo.getProductid() == childBO.getProductid()
                        && childBO.getBatchlist() != null && !childBO.getBatchId().isEmpty())
                    childList.add(childBO);
            }
            String pid = String.valueOf(parentBo.getProductid());

            listDataChild.put(pid, childList);//load child batch List data
        }
        if (childList != null)
            if (childList.size() > 0)
                showExpandButton();

//---------- remove duplicate product name from given list-----------///
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
            super.mSelectedIdByLevelId = mSelectedIdByLevelId;
    }

    public void loadProductList() {
        updateGeneralText(GENERAL);
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

                holder.batchNo.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
                holder.sihCase.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
                holder.sihOuter.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
                holder.sih.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));


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


                holder.psname.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
                holder.prodcode.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
                holder.sihCase.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
                holder.sihOuter.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
                holder.sih.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));

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


}
