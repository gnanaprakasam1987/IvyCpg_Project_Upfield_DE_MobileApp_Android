package com.ivy.cpg.view.orderfullfillment;

import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
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
import com.ivy.cpg.view.homescreen.HomeScreenActivity;

import java.util.ArrayList;

/**
 * Created by nivetha.s on 11-08-2015.
 */
public class OrderFullfillmentRetailerSelection extends IvyBaseActivityNoActionBar implements SearchView.OnQueryTextListener {
    private BusinessModel bmodel;
    private ListView listView;
    private OrderFullfillmentHelper orderFullfillmentHelper;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_orderfullfillment_retailer_selection);
        bmodel = (BusinessModel) getApplicationContext();
        bmodel.setContext(this);
        orderFullfillmentHelper = OrderFullfillmentHelper.getInstance(this);
        Toolbar toolbar = findViewById(R.id.toolbar);

        if (toolbar != null) {
            setSupportActionBar(toolbar);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            getSupportActionBar().setIcon(null);
            setScreenTitle("Order Fullfillment");
            getSupportActionBar().setDisplayUseLogoEnabled(false);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        listView = findViewById(R.id.listView1);
        listView.setCacheColorHint(0);
        orderFullfillmentHelper.downloadOrderFullfillmentRetailers();
        loadFilteredData((null));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_order_fullfillment, menu);
        SearchView searchView = (SearchView) menu.findItem(R.id.search).getActionView();
        setSearchTextColour(searchView);
        searchView.setOnQueryTextListener(this);
        searchView.setQueryHint("Search Retailer");
        // style searchbtn
        ImageView searchBtn = searchView
                .findViewById(R.id.search_button);
        searchBtn.setImageResource(R.drawable.icon_search);

        // style Clear Btn
        ImageView close_btn = searchView
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

        public @NonNull
        View getView(int position, View convertView, @NonNull ViewGroup parent) {

            final ViewHolder holder;
            OrderFullfillmentBO retailerObj = items.get(position);

            if (convertView == null) {
                LayoutInflater inflater = getLayoutInflater();
                convertView = inflater.inflate(
                        R.layout.activity_orderfullfillment_retailer_selection_list_item, parent, false);
                holder = new ViewHolder();
                holder.outletNameTextView = convertView.findViewById(R.id.outletName_tv);
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
            ArrayList<OrderFullfillmentBO> retailer = new ArrayList<>();
            for (int i = 0; i < orderFullfillmentHelper.getOrderFullfillment().size(); i++) {
                bo = orderFullfillmentHelper.getOrderFullfillment().get(i);

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
        LinearLayout searchPlate = searchView
                .findViewById(R.id.search_plate);
        searchPlate

                .setBackgroundResource(R.drawable.abc_ab_share_pack_holo_light);

        // .setBackgroundResource(R.drawable.abc_ab_share_pack_holo_dark);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        orderFullfillmentHelper.clearInstance();
    }
}
