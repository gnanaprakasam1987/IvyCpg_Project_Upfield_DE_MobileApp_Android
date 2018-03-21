package com.ivy.cpg.view.orderdelivery;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.ivy.cpg.view.order.OrderHelper;
import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.bo.OrderHeader;
import com.ivy.sd.png.bo.ProductMasterBO;
import com.ivy.sd.png.commons.IvyBaseActivityNoActionBar;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.provider.ConfigurationMasterHelper;
import com.ivy.sd.png.util.CommonDialog;
import com.ivy.sd.png.util.Commons;
import com.ivy.sd.png.util.DateUtil;
import com.ivy.sd.png.view.HomeScreenTwo;

import java.util.ArrayList;
import java.util.Vector;

public class OrderDeliveryActivity extends IvyBaseActivityNoActionBar {

    private BusinessModel bmodel;
    private RecyclerView recyclerView;
    OrderHelper orderHelper;
    ArrayList<OrderHeader> orderHeaders;
    private MyAdapter myAdapter;
    private String orderId = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_delivery);

        bmodel = (BusinessModel) getApplicationContext();
        bmodel.setContext(this);
        overridePendingTransition(R.anim.trans_left_in, R.anim.trans_left_out);

        Toolbar toolbar = findViewById(R.id.toolbar);

        if (toolbar != null) {

            setSupportActionBar(toolbar);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            setScreenTitle("Order Delivery");
//            // Used to on / off the back arrow icon
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
//           // Used to remove the app logo actionbar icon and set title as home
//          // (title support click)
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        orderHelper = OrderHelper.getInstance(this);


        orderHelper.downloadOrderDeliveryHeader(this);
        orderHeaders = orderHelper.getOrderHeaders();


        recyclerView = findViewById(R.id.order_list);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());

    }

    @Override
    protected void onResume() {
        super.onResume();
        bmodel = (BusinessModel) getApplicationContext();
        bmodel.setContext(this);

        //session out if user id becomes 0
        if (bmodel.userMasterHelper.getUserMasterBO().getUserid() == 0) {
            Toast.makeText(this,
                    getResources().getString(R.string.sessionout_loginagain),
                    Toast.LENGTH_SHORT).show();
            finish();
        }

        prepareScreenData();
    }
    private void prepareScreenData(){
        myAdapter = new MyAdapter();
        recyclerView.setAdapter(myAdapter);
        myAdapter.notifyDataSetChanged();
    }

    public class MyAdapter extends RecyclerView.Adapter<MyAdapter.MyViewHolder> {


        public class MyViewHolder extends RecyclerView.ViewHolder {

            private TextView orderId,orderDate,orderValue,orderLine;
            private Button orderAccept,orderEdit;

            public MyViewHolder(View view) {
                super(view);

                orderId = view.findViewById(R.id.order_delivery_listview_orderid);
                orderDate = view.findViewById(R.id.order_delivery_listview_orderdate);
                orderValue = view.findViewById(R.id.order_delivery_listview_ordervalue);
                orderLine = view.findViewById(R.id.order_delivery_listview_orderlines);

                orderAccept = view.findViewById(R.id.accept_btn);
                orderEdit = view.findViewById(R.id.edit_btn);

                orderId.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
                orderDate.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
                orderValue.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
                orderLine.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));

                ((TextView)view.findViewById(R.id.order_delivery_listview_id_heading)).setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
                ((TextView)view.findViewById(R.id.order_delivery_listview_date_heading)).setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
                ((TextView)view.findViewById(R.id.order_delivery_listview_line_head)).setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
                ((TextView)view.findViewById(R.id.order_delivery_listview_value_heading)).setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));

                (view.findViewById(R.id.view_dotted_line)).setLayerType(View.LAYER_TYPE_SOFTWARE, null);
            }
        }


        @Override
        public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.order_delivery_item, parent, false);

            return new MyViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(MyViewHolder holder, final int position) {
            holder.orderId.setText(orderHeaders.get(position).getOrderid());
            holder.orderValue.setText(String.valueOf(orderHeaders.get(position).getOrderValue()));
            holder.orderDate.setText(DateUtil.convertFromServerDateToRequestedFormat(orderHeaders.get(position).getOrderDate()
                    , ConfigurationMasterHelper.outDateFormat));
            holder.orderLine.setText(String.valueOf(orderHeaders.get(position).getLinesPerCall()));

            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    new downloadOrderDeliveryDetail(orderHeaders.get(position).getOrderid(),"ViewDetail").execute();

                }
            });

            holder.orderEdit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    if(orderHeaders.get(position).getInvoiceStatus() == 1) {
                        Toast.makeText(
                                OrderDeliveryActivity.this,
                                "Already invoice has been generated",
                                Toast.LENGTH_SHORT).show();
                        return;
                    }

                    new downloadOrderDeliveryDetail(orderHeaders.get(position).getOrderid(),"Edit").execute();
                }
            });

            holder.orderAccept.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    if(orderHeaders.get(position).getInvoiceStatus() == 1) {
                        Toast.makeText(
                                OrderDeliveryActivity.this,
                                "Already invoice has been generated",
                                Toast.LENGTH_SHORT).show();
                        return;
                    }

                    new downloadOrderDeliveryDetail(orderHeaders.get(position).getOrderid(),"Approve").execute();

                }
            });

        }

        @Override
        public int getItemCount() {
            return orderHeaders.size();
        }
    }

    private class downloadOrderDeliveryDetail extends AsyncTask<Void,Void,Void>{

        private String orderId;
        private String from;

        private downloadOrderDeliveryDetail(String orderId,String from){
            this.orderId = orderId;
            this.from = from;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            bmodel.productHelper.clearOrderTable();
            orderHelper.clearSalesReturnTable();
            orderHelper.downloadOrderDeliveryDetail(OrderDeliveryActivity.this,orderId);
            orderHelper.downloadOrderedFreeProducts(OrderDeliveryActivity.this,orderId);
            orderHelper.downloadOrderDeliveryAmountDetail(OrderDeliveryActivity.this,orderId);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {

            if(from.equalsIgnoreCase("Approve")) {

                if (orderHelper.isSIHAvailable(false)) {

                    CommonDialog dialog = new CommonDialog(getApplicationContext(), OrderDeliveryActivity.this, "", getResources().getString(R.string.order_delivery_approve), false,
                            getResources().getString(R.string.ok), getResources().getString(R.string.cancel), new CommonDialog.positiveOnClickListener() {
                        @Override
                        public void onPositiveButtonClick() {

                            orderHelper.updateTableValues(OrderDeliveryActivity.this, orderId,false);
                            Toast.makeText(
                                    OrderDeliveryActivity.this,
                                    "Approved",
                                    Toast.LENGTH_SHORT).show();

                            myAdapter.notifyDataSetChanged();

                        }
                    }, new CommonDialog.negativeOnClickListener() {
                        @Override
                        public void onNegativeButtonClick() {

                        }
                    });
                    dialog.show();
                    dialog.setCancelable(false);
                } else {
                    Toast.makeText(
                            OrderDeliveryActivity.this,
                            getResources().getString(R.string.ordered_value_exceeds_sih_value_please_edit_the_order),
                            Toast.LENGTH_SHORT).show();
                }

            }

            else {
                Intent intent = new Intent(OrderDeliveryActivity.this, OrderDeliveryDetailActivity.class);
                intent.putExtra("From", from);
                intent.putExtra("OrderId",orderId);
                startActivity(intent);
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int i = item.getItemId();
        if (i == android.R.id.home) {
            backButtonClick();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void backButtonClick() {
        try {

            startActivity(new Intent(this,
                    HomeScreenTwo.class));
            finish();

            overridePendingTransition(R.anim.trans_right_in, R.anim.trans_right_out);

        } catch (Exception e) {
            Commons.printException(e);
        }
    }

}
