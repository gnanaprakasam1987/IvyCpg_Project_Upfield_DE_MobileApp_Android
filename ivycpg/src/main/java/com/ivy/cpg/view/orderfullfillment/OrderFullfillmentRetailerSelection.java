package com.ivy.cpg.view.orderfullfillment;

import android.content.Intent;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.bo.OrderFullfillmentBO;
import com.ivy.sd.png.commons.IvyBaseActivityNoActionBar;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.provider.ConfigurationMasterHelper;
import com.ivy.sd.png.util.Commons;
import com.ivy.sd.png.view.HomeScreenActivity;

import java.util.ArrayList;

/**
 * Created by nivetha.s on 11-08-2015.
 */
public class OrderFullfillmentRetailerSelection extends IvyBaseActivityNoActionBar implements SearchView.OnQueryTextListener {
    private BusinessModel bmodel;
    private ListView listView;
    private OrderFullfillmentBO retailerObj;
    private ArrayList<OrderFullfillmentBO> retailer;
    private Toolbar toolbar;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_orderfullfillment_retailer_selection);
        bmodel = (BusinessModel) getApplicationContext();
        bmodel.setContext(this);
        toolbar = findViewById(R.id.toolbar);

        if (toolbar != null) {
            setSupportActionBar(toolbar);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            getSupportActionBar().setIcon(null);
            setScreenTitle("Order Fullfillment");
            getSupportActionBar().setDisplayUseLogoEnabled(false);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        listView = (ListView) findViewById(R.id.listView1);
        listView.setCacheColorHint(0);
        bmodel.orderfullfillmenthelper.downloadOrderFullfillmentRetailers();
        loadFilteredData((null));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_order_fullfillment, menu);
        SearchView searchView = (SearchView) MenuItemCompat.getActionView(menu
                .findItem(R.id.search));
        setSearchTextColour(searchView);
        searchView.setOnQueryTextListener(this);
        searchView.setQueryHint("Search Retailer");
        // style searchbtn
        ImageView searchBtn = (ImageView) searchView
                .findViewById(R.id.search_button);
        searchBtn.setImageResource(R.drawable.icon_search);

        // style Clear Btn
        ImageView close_btn = (ImageView) searchView
                .findViewById(R.id.search_close_btn);
        close_btn.setImageResource(R.drawable.ic_clear_red);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        menu.findItem(R.id.search).setVisible(false);
        menu.findItem(R.id.menu_save).setVisible(false);
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // TODO Auto-generated method stub
        switch (item.getItemId()) {
            case android.R.id.home:

                startActivity(new Intent(OrderFullfillmentRetailerSelection.this,
                        HomeScreenActivity.class));
                finish();
                return true;
        }
        return false;
    }

    class RetailerSelectionAdapter extends ArrayAdapter<OrderFullfillmentBO> {

        private ArrayList<OrderFullfillmentBO> items;

        private RetailerSelectionAdapter(ArrayList<OrderFullfillmentBO> items) {
            super(OrderFullfillmentRetailerSelection.this, R.layout.activity_orderfullfillment_retailer_selection_list_item, items);
            this.items = items;
        }

        public OrderFullfillmentBO getItem(int position) {
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
            retailerObj = (OrderFullfillmentBO) items.get(position);

            if (convertView == null) {
                LayoutInflater inflater = getLayoutInflater();
                convertView = inflater.inflate(
                        R.layout.activity_orderfullfillment_retailer_selection_list_item, null, false);
                holder = new ViewHolder();
                holder.outletNameTextView = (TextView) convertView.findViewById(R.id.outletName_tv);
                holder.outletNameTextView.setTypeface(bmodel.configurationMasterHelper
                        .getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));

                convertView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        bmodel.setOrderfullfillmentbo(holder.retailerObjectHolder);
                        Intent i = new Intent(OrderFullfillmentRetailerSelection.this,
                                OrderFullfillment.class);

                        startActivity(i);
                    }

                });

                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            holder.retailerObjectHolder = retailerObj;

            holder.outletNameTextView.setText(holder.retailerObjectHolder.getRetailername()
                    + ((bmodel.configurationMasterHelper.SHOW_RETAILER_FREQUENCY) ? " - F" + holder.retailerObjectHolder.getVisit_frequencey() : ""));

            return convertView;
        }

        class ViewHolder {
            private OrderFullfillmentBO retailerObjectHolder;

            private TextView outletNameTextView;


        }

    }

    @Override
    public boolean onQueryTextChange(String s) {
        // TODO Auto-generated method stub

        loadFilteredData(s);
        return true;
    }

    @Override
    public boolean onQueryTextSubmit(String s) {
        // TODO Auto-generated method stub

        if (s.isEmpty()) {
            loadFilteredData(null);
        }
        return true;
    }

    public void loadFilteredData(String filter) {
        try {
            OrderFullfillmentBO bo;
            retailer = new ArrayList<OrderFullfillmentBO>();
            for (int i = 0; i < bmodel.orderfullfillmenthelper.getOrderFullfillment().size(); i++) {
                bo = bmodel.orderfullfillmenthelper.getOrderFullfillment().get(i);

                if (filter != null) {
                    if (bo.getRetailername().toLowerCase().contains(filter.toLowerCase())) {
                        retailer.add(bo);
                    }
                } else
                    retailer.add(bo);

            }
            RetailerSelectionAdapter mSchedule = new RetailerSelectionAdapter(
                    retailer);
            // mSchedule.notifyDataSetChanged();
            listView.setAdapter(mSchedule);
        } catch (Exception e) {
            Commons.printException(e);
        }
    }

    private void setSearchTextColour(SearchView searchView) {
        int searchPlateId = searchView.getContext().getResources()
                .getIdentifier("android:id/search_src_text", null, null);
        LinearLayout searchPlate = (LinearLayout) searchView
                .findViewById(R.id.search_plate);
        searchPlate

                .setBackgroundResource(R.drawable.abc_ab_share_pack_holo_light);

        // .setBackgroundResource(R.drawable.abc_ab_share_pack_holo_dark);

    }
}
