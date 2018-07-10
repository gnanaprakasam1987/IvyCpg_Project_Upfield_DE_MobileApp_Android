package com.ivy.cpg.view.van;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.ActionBarDrawerToggle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;

import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.bo.LevelBO;
import com.ivy.sd.png.bo.LoadManagementBO;
import com.ivy.sd.png.model.BrandDialogInterface;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.util.Commons;
import com.ivy.sd.png.view.HomeScreenActivity;
import com.ivy.sd.png.view.ToolBarwithFilter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Vector;

public class CurrentStockBatchViewActivity extends ToolBarwithFilter
        implements BrandDialogInterface, OnEditorActionListener {
    private Vector<LoadManagementBO> mylist;
    private String tv;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final Intent i = getIntent();

        LinearLayout ll = (LinearLayout) findViewById(R.id.ListHeader);
        LayoutInflater layoutInflater = (LayoutInflater) CurrentStockBatchViewActivity.this
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final ViewGroup nullParent = null;
        ll.addView(layoutInflater.inflate(
                R.layout.current_stock_batchwise_layout, nullParent, false));
        findViewById(R.id.keypad).setVisibility(View.GONE);
        bmodel = (BusinessModel) getApplicationContext();
        bmodel.setContext(this);
        try {
            if (bmodel.labelsMasterHelper.applyLabels(findViewById(
                    R.id.sihtitle).getTag()) != null) {
                tv = bmodel.labelsMasterHelper
                        .applyLabels(findViewById(R.id.sihtitle)
                                .getTag());
                ((TextView) findViewById(R.id.sihtitle))
                        .setText(tv);
            }
        } catch (Exception e) {
            Commons.printException("" + e);
        }

        mDrawerToggle = new ActionBarDrawerToggle(
                CurrentStockBatchViewActivity.this,
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
        setActionBarTitle(i.getStringExtra("screentitle"));
        hideSpecialFilter();
        hideNextButton();
        hideRemarksButton();
        hideShemeButton();
        hideLocationButton();

        /** Load products from product master **/
        LoadManagementBO lbo;
        mylist = new Vector<>();
        for (int j = 0; j < bmodel.productHelper.getProducts().size(); j++) {
            lbo = bmodel.productHelper.getProducts().get(j);
            if (lbo.getStocksih() > 0)
                mylist.add(lbo);
        }
        updateBrandText("Brand", -1);

    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        menu.findItem(R.id.menu_loc_filter).setVisible(false);
        menu.findItem(R.id.menu_fivefilter).setVisible(false);
        menu.findItem(R.id.menu_product_filter).setVisible(false);
        menu.findItem(R.id.menu_fivefilter).setVisible(true);

        return super.onPrepareOptionsMenu(menu);
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
        ArrayList<LoadManagementBO> temp = new ArrayList<>();
        for (int i = 0; i < siz; ++i) {

            LoadManagementBO ret = mylist.get(i);
            if (bid == -1 || bid == ret.getParentid()) {
                temp.add(ret);

            }
        }
        MyAdapter mSchedule = new MyAdapter(temp);
        lvwplist.setAdapter(mSchedule);

        updateTotalSIHValue(temp);

    }

    @Override
    public void onBackButtonClick() {
        startActivity(new Intent(CurrentStockBatchViewActivity.this,
                HomeScreenActivity.class).putExtra("menuCode", "MENU_LOAD_MANAGEMENT"));
        finish();
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
                    if (ret.getBarcode() != null && ret.getBarcode()
                            .toLowerCase()
                            .contains(
                                    mEdt_searchproductName.getText().toString()
                                            .toLowerCase()))
                        temp.add(ret);

                } else if ("GCAS Code".equals(mSelectedFilter)) {
                    if (ret.getRField1() != null && ret.getRField1()
                            .toLowerCase()
                            .contains(
                                    mEdt_searchproductName.getText().toString()
                                            .toLowerCase()))
                        temp.add(ret);

                } else if (getResources().getString(
                        R.string.product_name).equals(mSelectedFilter)) {
                    if (ret.getProductname() != null && ret.getProductname()
                            .toLowerCase()
                            .contains(
                                    mEdt_searchproductName.getText().toString()
                                            .toLowerCase()))
                        temp.add(ret);
                }
            }
            MyAdapter mSchedule = new MyAdapter(temp);
            lvwplist.setAdapter(mSchedule);

        } else {
            Toast.makeText(this, "Enter atleast 3 letters.", Toast.LENGTH_SHORT)
                    .show();
        }
    }

    @Override
    public void updateFromFiveLevelFilter(Vector<LevelBO> mParentIdList) {

        ArrayList<LoadManagementBO> filterlist = new ArrayList<>();
        for (LevelBO levelBO : mParentIdList) {
            for (LoadManagementBO loadMgtBO : mylist) {
                if (levelBO.getProductID() == loadMgtBO.getParentid()) {
                    filterlist.add(loadMgtBO);
                }
            }
        }

        MyAdapter mSchedule = new MyAdapter(filterlist);
        lvwplist.setAdapter(mSchedule);
        mDrawerLayout.closeDrawers();

    }

    @Override
    public void updateFromFiveLevelFilter(Vector<LevelBO> mParentIdList, HashMap<Integer, Integer> mSelectedIdByLevelId, ArrayList<Integer> mAttributeProducts, String mFilterText) {
        ArrayList<LoadManagementBO> filterlist = new ArrayList<>();
        if (mAttributeProducts != null) {
            if (!mParentIdList.isEmpty()) {
                for (LevelBO levelBO : mParentIdList) {
                    for (LoadManagementBO productBO : mylist) {
                        if (levelBO.getProductID() == productBO.getParentid()) {
                            // here we get all products mapped to parent id list, then that product will be added only if it is mapped to selected attribute
                            if (mAttributeProducts.contains(productBO.getProductid())) {
                                mylist.add(productBO);
                            }
                        }
                    }
                }
            } else {
                for (int pid : mAttributeProducts) {
                    for (LoadManagementBO productBO : mylist) {
                        if (pid == productBO.getProductid()) {
                            filterlist.add(productBO);
                        }
                    }
                }
            }
        } else {
            for (LevelBO levelBO : mParentIdList) {
                for (LoadManagementBO loadMgtBO : mylist) {
                    if (levelBO.getProductID() == loadMgtBO.getParentid()) {
                        filterlist.add(loadMgtBO);
                    }
                }
            }
        }

        this.mSelectedIdByLevelId = mSelectedIdByLevelId;
        MyAdapter mSchedule = new MyAdapter(filterlist);
        lvwplist.setAdapter(mSchedule);
        updateTotalSIHValue(filterlist);
        mDrawerLayout.closeDrawers();
    }

    private void updateTotalSIHValue(ArrayList<LoadManagementBO> mylist) {

        TextView totalSihTV = (TextView) findViewById(R.id.tv_sih);
        int totalSih = 0;

        for (LoadManagementBO loadManagementBO : mylist) {
            if (loadManagementBO.getStocksih() > 0) {
                totalSih = totalSih + loadManagementBO.getStocksih();

            }
        }
        tv = totalSih + "";
        totalSihTV.setText(tv);
    }

    private class MyAdapter extends ArrayAdapter<LoadManagementBO> {
        LoadManagementBO product;
        private ArrayList<LoadManagementBO> items;

        public MyAdapter(ArrayList<LoadManagementBO> items) {
            super(CurrentStockBatchViewActivity.this,
                    R.layout.row_batchwise_current_stock_report, items);
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
                row = inflater.inflate(
                        R.layout.row_batchwise_current_stock_report, parent,
                        false);
                holder = new ViewHolder();

                holder.psname = (TextView) row.findViewById(R.id.orderPRODNAME);
                holder.sih = (TextView) row.findViewById(R.id.sih);
                holder.batchnumber = (TextView) row
                        .findViewById(R.id.batchnumber);

                row.setOnClickListener(new OnClickListener() {
                    public void onClick(View v) {
                        productName.setText(holder.pname);
                        if (viewFlipper.getDisplayedChild() != 0) {
                            viewFlipper.setInAnimation(
                                    CurrentStockBatchViewActivity.this,
                                    R.anim.in_from_left);
                            viewFlipper.setOutAnimation(
                                    CurrentStockBatchViewActivity.this,
                                    R.anim.out_to_left);
                            viewFlipper.showPrevious();
                        }
                    }
                });
                row.setTag(holder);
            } else {
                holder = (ViewHolder) row.getTag();
            }

            holder.ref = position;
            holder.psname.setText(product.getProductshortname());
            holder.pname = product.getProductname();
            tv = product.getStocksih() + "";
            holder.sih.setText(tv);

            tv = getResources().getString(
                    R.string.batch_no)
                    + ": " + product.getBatchNo() + "";
            holder.batchnumber.setText(tv);

            return row;
        }
    }

    class ViewHolder {
        int ref;
        private String pname;
        private TextView psname;
        private TextView sih;
        private TextView batchnumber;
    }


}
