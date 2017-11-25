package com.ivy.sd.png.view;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.widget.Toolbar;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;

import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.bo.SKUWiseTargetBO;
import com.ivy.sd.png.commons.IvyBaseActivityNoActionBar;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.util.Commons;

import java.util.ArrayList;
import java.util.Vector;

public class RetailerKPISKUActivity extends IvyBaseActivityNoActionBar implements
        OnEditorActionListener, OnClickListener {
    private Vector<SKUWiseTargetBO> mylist;
    private BusinessModel bmodel;
    private String screentitlebk = "";
    private LinearLayout ll;
    private ListView lvwplist;
    private boolean isFromDash;
    private Button previous;
    private TextView textview[] = null;
    private int curSeq = 0;
    private Toolbar toolbar;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final Intent i = getIntent();

        setContentView(R.layout.retailerkpisku_header);


        bmodel = (BusinessModel) getApplicationContext();
        bmodel.setContext(this);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        previous = (Button) findViewById(R.id.previousBTN);
        ll = (LinearLayout) findViewById(R.id.ll_nav);
        previous.setOnClickListener(this);

        try {
            if (bmodel.labelsMasterHelper
                    .applyLabels(findViewById(R.id.indextv).getTag()) != null)
                ((TextView) findViewById(R.id.indextv))
                        .setText(bmodel.labelsMasterHelper
                                .applyLabels(findViewById(R.id.indextv)
                                        .getTag()));
        } catch (Exception e) {
            Commons.printException(e);
        }

        try {
            if (bmodel.labelsMasterHelper.applyLabels(findViewById(
                    R.id.targettv).getTag()) != null)
                ((TextView) findViewById(R.id.targettv))
                        .setText(bmodel.labelsMasterHelper
                                .applyLabels(findViewById(R.id.targettv)
                                        .getTag()));
        } catch (Exception e) {
            Commons.printException(e);
        }

        try {
            if (bmodel.labelsMasterHelper.applyLabels(findViewById(
                    R.id.achievedtv).getTag()) != null)
                ((TextView) findViewById(R.id.achievedtv))
                        .setText(bmodel.labelsMasterHelper
                                .applyLabels(findViewById(R.id.achievedtv)
                                        .getTag()));
        } catch (Exception e) {
            Commons.printException(e);
        }

        lvwplist = (ListView) findViewById(R.id.list);
        lvwplist.setCacheColorHint(0);
        setSupportActionBar(toolbar);

        // Used to on / off the back arrow icon
        if (getSupportActionBar() != null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        // Used to remove the app logo actionbar icon and set title as home
        // (title support click)
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        screentitlebk = i.getExtras().getString("screentitlebk");
        String screenTitle = i.getStringExtra("screentitle");
        if (i.getExtras() != null)
            isFromDash = i.getExtras().getBoolean("isFromDash");
        if (screenTitle.equals("")) {
            screenTitle = "SKU Target";
        }

        LinearLayout.LayoutParams weight1 = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        weight1.weight = 1;
        weight1.gravity = Gravity.CENTER;

        LinearLayout.LayoutParams weight2 = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        weight2.weight = 2;
        weight2.gravity = Gravity.CENTER;

        textview = new TextView[100];

        this.getSupportActionBar().setTitle(screenTitle);
        this.getSupportActionBar().setIcon(
                R.drawable.icon_stock);

        mylist = bmodel.dashBoardHelper.getRetailerKpiSku();
        updateList(bmodel.dashBoardHelper.mRetailerKpiMinSeqLevel);

        DrawerLayout mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_inventory, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle presses on the action bar items
        int i = item.getItemId();
        if (i == android.R.id.home) {
            onBackButtonClick();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {

        menu.findItem(R.id.menu_location_filter).setVisible(false);
        menu.findItem(R.id.menu_remarks).setVisible(false);
        menu.findItem(R.id.menu_spl_filter).setVisible(false);
        menu.findItem(R.id.menu_next).setVisible(false);
        if (isFromDash)
            menu.findItem(R.id.menu_product_filter).setVisible(false);
        return super.onPrepareOptionsMenu(menu);
    }


    public void onClick(View v) {
        bmodel = (BusinessModel) getApplicationContext();
        bmodel.setContext(this);

        if (v.getId() == R.id.previousBTN) {
            updateList(curSeq - 1);
        }
    }


    private void updateList(int bid) {
        if (mylist == null) {
            bmodel.showAlert(
                    getResources().getString(R.string.no_products_exists), 0);
            return;
        }
        int siz = mylist.size();
        ArrayList<SKUWiseTargetBO> temp = new ArrayList<>();
        for (int i = 0; i < siz; ++i) {

            SKUWiseTargetBO ret = mylist.get(i);
            if (ret.getSequence() == bid) {
                temp.add(ret);
            }
        }

        if (bid == bmodel.dashBoardHelper.mRetailerKpiMinSeqLevel)
            previous.setVisibility(View.GONE);

        if (curSeq != bmodel.dashBoardHelper.mRetailerKpiMinSeqLevel && textview[curSeq] != null) {

            textview[curSeq].setText("");
            ll.removeView(textview[curSeq]);
        }

        curSeq = bid;

        MyAdapter mSchedule = new MyAdapter(temp);
        lvwplist.setAdapter(mSchedule);
    }

    private void updateNextList(int parentID, String pname) {
        // Close the drawer
        if (mylist == null) {
            bmodel.showAlert(
                    getResources().getString(R.string.no_products_exists), 0);
            return;
        }
        int siz = mylist.size();
        ArrayList<SKUWiseTargetBO> temp = new ArrayList<>();
        for (int i = 0; i < siz; ++i) {

            SKUWiseTargetBO ret = mylist.get(i);
            if (ret.getParentID() == parentID) {
                temp.add(ret);
                curSeq = ret.getSequence();
            }
        }
        if (!temp.isEmpty()) {
            MyAdapter mSchedule = new MyAdapter(temp);
            lvwplist.setAdapter(mSchedule);
            if (curSeq != bmodel.dashBoardHelper.mRetailerKpiMinSeqLevel)
                previous.setVisibility(View.VISIBLE);
            ll.addView(getTextView(curSeq, parentID, pname));
        } else {
            Toast.makeText(this,
                    "No  data to Show",
                    Toast.LENGTH_SHORT).show();
        }
    }

    private class MyAdapter extends ArrayAdapter<SKUWiseTargetBO> {
        private final ArrayList<SKUWiseTargetBO> items;

        public MyAdapter(ArrayList<SKUWiseTargetBO> items) {
            super(RetailerKPISKUActivity.this, R.layout.row_sellerkpisku, items);
            this.items = items;
            Commons.print("my adapter," + String.valueOf(items.size()));
        }

        public SKUWiseTargetBO getItem(int position) {
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
            SKUWiseTargetBO product = items.get(position);

            if (convertView == null) {
                LayoutInflater inflater = getLayoutInflater();
                convertView = inflater.inflate(R.layout.row_skuwisetgt, parent,
                        false);
                holder = new ViewHolder();

                holder.psname = (TextView) convertView
                        .findViewById(R.id.orderPRODNAME);
                holder.target = (TextView) convertView.findViewById(R.id.tgt);
                holder.ach = (TextView) convertView.findViewById(R.id.ach);
                holder.index = (TextView) convertView.findViewById(R.id.ind);

                holder.psname.setOnClickListener(new OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        updateNextList(holder.productbo.getPid(), holder.productbo.getProductShortName());
                    }
                });
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            holder.productbo = product;
            holder.psname.setText(product.getProductShortName());
            String strTarget = bmodel.formatValue(product.getTarget()) + "";
            holder.target.setText(strTarget);
            String strAchieved = bmodel.formatValue(product.getAchieved()) + "";
            holder.ach.setText(strAchieved);
            String strCaluPercentage = product.getCalculatedPercentage() + "";
            holder.index.setText(strCaluPercentage);

            return convertView;
        }
    }

    class ViewHolder {
        private TextView psname;
        private TextView target;
        private TextView ach;
        private TextView index;
        private SKUWiseTargetBO productbo;
    }

    private TextView getTextView(final int mNumber
            , int pid, String textname) {
        textview[mNumber] = new TextView(RetailerKPISKUActivity.this);
        textview[mNumber].setClickable(true);
        textview[mNumber].setId(pid);
        String strText = textname + "  >  ";
        textview[mNumber].setText(strText);
        textview[mNumber].setTextSize(TypedValue.COMPLEX_UNIT_SP, getResources().getDimension(R.dimen.font_small));
        textview[mNumber].setPadding(0, 20, 0, 20);
        return textview[mNumber];
    }


    private void onBackButtonClick() {
        Intent i = new Intent(RetailerKPISKUActivity.this,
                RetailerDashBoardActivity.class);
        i.putExtra("from", "1");
        i.putExtra("screentitle", screentitlebk);
        startActivity(i);
        finish();
    }

    @Override
    public boolean onEditorAction(TextView arg0, int arg1, KeyEvent arg2) {
        return false;
    }
}
