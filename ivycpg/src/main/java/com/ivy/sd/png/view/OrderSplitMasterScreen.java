/**
 *
 */
package com.ivy.sd.png.view;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.AsyncTask.Status;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.bo.OrderSplitMasterBO;
import com.ivy.sd.png.bo.RetailerMasterBO;
import com.ivy.sd.png.commons.IvyBaseActivityNoActionBar;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.provider.OrderSplitHelper;
import com.ivy.sd.png.util.Commons;

import java.util.List;

/**
 * @author sivakumar.j
 */
public class OrderSplitMasterScreen extends IvyBaseActivityNoActionBar {

    View parentView = null;
    ListView listView = null;
    LoadOrderSplitMaster loadOrderSplitMaster = null;
    private BusinessModel bmodel;
    private boolean isCreated;
    private Toolbar toolbar;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        bmodel = (BusinessModel) getApplicationContext();
        bmodel.setContext(this);

        if (bmodel.userMasterHelper.getUserMasterBO().getUserid() == 0) {
            Toast.makeText(this,
                    getResources().getString(R.string.sessionout_loginagain),
                    Toast.LENGTH_SHORT).show();
            finish();
        }

        setContentView(R.layout.activity_order_split_master);
        listView = (ListView) this.findViewById(R.id.listView1);
        listView.setCacheColorHint(0);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            setScreenTitle(bmodel.getOrderSplitScreenTitle());
            getSupportActionBar().setIcon(null);
//            // Used to on / off the back arrow icon
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
//           // Used to remove the app logo actionbar icon and set title as home
//          // (title support click)
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        loadOrderSplitMaster = new LoadOrderSplitMaster();
        loadOrderSplitMaster.execute();
    }

    @Override
    protected void onResume() {
        // TODO Auto-generated method stub
        super.onResume();
        bmodel = (BusinessModel) getApplicationContext();
        bmodel.setContext(this);

        if (bmodel.userMasterHelper.getUserMasterBO().getUserid() == 0) {
            Toast.makeText(this,
                    getResources().getString(R.string.sessionout_loginagain),
                    Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    @Override
    protected void onDestroy() {

        if (this.loadOrderSplitMaster != null) {
            Status status = this.loadOrderSplitMaster.getStatus();
            if (status == AsyncTask.Status.RUNNING) {
                this.loadOrderSplitMaster.cancel(true);
            }
        }
        this.loadOrderSplitMaster = null;
        listView = null;

        super.onDestroy();
        // force the garbage collector to run
        System.gc();
    }

    public void settingListViewAdapter() {
        this.listView.setAdapter(new MenuBaseAdapter(bmodel.orderSplitHelper
                .getOrderSplitMasterBOList()));
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        // TODO Auto-generated method stub
        switch (item.getItemId()) {
            case android.R.id.home:
                Intent i = new Intent(OrderSplitMasterScreen.this, HomeScreenActivity.class);
                startActivity(i);

                // bmodel.orderSplitHelper.clearAll();
                bmodel.orderSplitHelper = OrderSplitHelper.clearInstance();
                bmodel.setOrderSplitScreenTitle(null);

                finish();
                return true;

        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * Alert for order delete.
     *
     * @param orderID
     */
    public void showDeleteOrderAlert(final String orderID,
                                     final String retailerid) {

        AlertDialog.Builder builder = new AlertDialog.Builder(
                OrderSplitMasterScreen.this);
        builder.setTitle("");
        builder.setMessage(getResources().getString(
                R.string.do_you_want_delete_order));

        builder.setPositiveButton(getResources().getString(R.string.yes),
                new android.content.DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        bmodel.orderSplitHelper.deleteOrder(orderID);
                        bmodel.orderSplitHelper.insertSplittedOrder(retailerid,
                                orderID);
                        loadOrderSplitMaster = new LoadOrderSplitMaster();
                        loadOrderSplitMaster.execute();
                        return;
                    }
                });

        builder.setNegativeButton(getResources().getString(R.string.no),
                new android.content.DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        return;
                    }
                });

        builder.setCancelable(false);
        bmodel.applyAlertDialogTheme(builder);
    }

    class LoadOrderSplitMaster extends AsyncTask<Void, Void, Void> {

        //	private ProgressDialog progressDialogue;
        private AlertDialog.Builder builder;
        private AlertDialog alertDialog;

        protected void onPreExecute() {
            /*progressDialogue = ProgressDialog.show(OrderSplitMasterScreen.this,
                    DataMembers.SD, "Loading", true, false);*/
            builder = new AlertDialog.Builder(OrderSplitMasterScreen.this);

            customProgressDialog(builder,  getResources().getString(R.string.loading));
            alertDialog = builder.create();
            alertDialog.show();
        }

        @Override
        protected Void doInBackground(Void... params) {
            bmodel.orderSplitHelper.loadOrderSplitMasterBOListFromDB();
            return null;
        }

        protected void onPostExecute(Void result) {
            // result is the value returned from doInBackground
            alertDialog.dismiss();
            //	progressDialogue.dismiss();
            settingListViewAdapter();
        }

    }

    class ViewHolder {
        private OrderSplitMasterBO orderSplitMasterBO;
        private int position;
        private TextView customerTextView, orderNumberTextView, nSkuTextView,
                amountTextView;
        private ImageView editBTN, orderEditBTN;
        private ImageView deleteBTN;
    }

    class MenuBaseAdapter extends BaseAdapter {

        List<OrderSplitMasterBO> orderSplitMasterBOList = null;

        public MenuBaseAdapter(List<OrderSplitMasterBO> items) {
            orderSplitMasterBOList = items;
        }

        @Override
        public int getCount() {
            // TODO Auto-generated method stub
            return orderSplitMasterBOList.size();
        }

        @Override
        public Object getItem(int position) {
            // TODO Auto-generated method stub
            return position;
        }

        @Override
        public long getItemId(int position) {
            // TODO Auto-generated method stub
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            // TODO Auto-generated method stub

            final ViewHolder holder;
            if (convertView == null) {
                LayoutInflater inflater = getLayoutInflater();
                convertView = inflater.inflate(
                        R.layout.order_split_master_list_item_menu, parent,
                        false);
                holder = new ViewHolder();

                holder.amountTextView = (TextView) convertView
                        .findViewById(R.id.list_item_amount);
                holder.customerTextView = (TextView) convertView
                        .findViewById(R.id.list_item_customer);
                holder.deleteBTN = (ImageView) convertView
                        .findViewById(R.id.list_item_delete_btn);
                holder.editBTN = (ImageView) convertView
                        .findViewById(R.id.list_item_edit_btn);
                holder.nSkuTextView = (TextView) convertView
                        .findViewById(R.id.list_item_n_sku);
                holder.orderNumberTextView = (TextView) convertView
                        .findViewById(R.id.list_item_order_number);

                // Select Order Edit to Load Order summary on Edit Mode
                holder.orderEditBTN.setOnClickListener(new OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        /** Load the stock check if opened in edit mode. **/
                        if (!isCreated) {
                            isCreated = true;

                            RetailerMasterBO bo = null;
                            for (int i = 0; i < bmodel.getRetailerMaster()
                                    .size(); i++) {
                                bo = bmodel.getRetailerMaster().get(i);
                                if (bo.getRetailerID().equals(
                                        holder.orderSplitMasterBO
                                                .getRetailerId())) {
                                    break;
                                }
                            }
                            RetailerMasterBO orderId = new RetailerMasterBO();
                            orderId.setRetailerID(holder.orderSplitMasterBO
                                    .getOrderID()); // Order Id
                            new DownloadProductsAndPrice().execute(bo, orderId);

                        }
                    }
                });

                holder.editBTN.setOnClickListener(new OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        // TODO Auto-generated method stub

                        bmodel.orderSplitHelper
                                .setSrcOrderId(holder.orderSplitMasterBO
                                        .getOrderID());

                        bmodel.orderSplitHelper
                                .setSelectedOrderId(holder.orderSplitMasterBO
                                        .getOrderID());
                        bmodel.orderSplitHelper
                                .setSelectedRetailerId(holder.orderSplitMasterBO
                                        .getRetailerId());
                        bmodel.orderSplitHelper
                                .setSelectedOrderSplitMasterBO(holder.orderSplitMasterBO);
                        bmodel.orderSplitHelper
                                .setCurrently_selected_brand_id_from_filter(OrderSplitHelper.ALL);

                        bmodel.orderSplitHelper
                                .setCurrently_selected_category_id_from_filter(OrderSplitHelper.ALL);

                        bmodel.orderSplitHelper
                                .createNewSrcOrderSplitDetailsBOList();

                        // Intent i = new
                        // Intent(OrderSplitMasterScreen.this,OrderSplitDetailsScreen.class);
                        Intent i = new Intent(OrderSplitMasterScreen.this,
                                OrderSplitDetailsScreenWithFilters.class);
                        finish();
                        startActivity(i);

                    }

                });

                holder.deleteBTN.setOnClickListener(new OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        showDeleteOrderAlert(
                                holder.orderSplitMasterBO.getOrderID(),
                                holder.orderSplitMasterBO.getRetailerId());
                    }
                });
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            holder.position = position;
            holder.orderSplitMasterBO = orderSplitMasterBOList
                    .get(holder.position);

            String str = bmodel.formatValue(holder.orderSplitMasterBO
                    .getOrderValue());
            holder.amountTextView.setText(str);

            // holder.customerTextView.setText(holder.orderSplitMasterBO.getRetailerId());
            holder.customerTextView.setText(holder.orderSplitMasterBO
                    .getRetailerName());

            holder.nSkuTextView.setText(holder.orderSplitMasterBO
                    .getLinesPerCall() + "");
            holder.orderNumberTextView.setText(holder.orderSplitMasterBO
                    .getOrderID());

            holder.orderNumberTextView.setText(holder.orderSplitMasterBO
                    .getPo());
            return convertView;
        }

    }

    /**
     * @author vinodh.r Load the Order Product and related Object for Edit
     */
    class DownloadProductsAndPrice extends
            AsyncTask<RetailerMasterBO, Integer, Boolean> {
        //	private ProgressDialog progressDialogue;
        private AlertDialog.Builder builder;
        private AlertDialog alertDialog;

        @Override
        protected Boolean doInBackground(RetailerMasterBO... obj) {
            try {
                RetailerMasterBO bo = obj[0];
                String orderId = obj[1].getRetailerID(); // Order ID
                bmodel.setOrderid(orderId);
                bmodel.deleteSpliteOrderID = orderId;
                bmodel.orderSplitHelper.updateEditOrderUploadFlag(orderId);


                // load scheme details
                if (bmodel.configurationMasterHelper.IS_SCHEME_ON) {

                    if (bmodel.configurationMasterHelper.SHEME_NOT_APPLY_DEVIATEDSTORE) {
                        if (!bmodel.getRetailerMasterBO().getIsDeviated()
                                .equals("Y")) {

                            bmodel.schemeDetailsMasterHelper
                                    .downloadSchemeMethods();

                        }
                    } else {

                        bmodel.schemeDetailsMasterHelper
                                .downloadSchemeMethods();

                    }
                } else {
                    bmodel.schemeDetailsMasterHelper.setIsScheme();
                }
                bmodel.productHelper.loadSBDFocusData();
                bmodel.batchAllocationHelper.downloadBatchDetails(bmodel
                        .getRetailerMasterBO().getGroupId());
                bmodel.batchAllocationHelper.downloadProductBatchCount();
                bmodel.setEditStockCheck(false);
                if (bmodel.hasAlreadyStockChecked(bo.getRetailerID())) {
                    bmodel.setEditStockCheck(true);
                    bmodel.loadStockCheckedProducts(bo.getRetailerID(), "MENU_STOCK");
                }

                bmodel.setEdit(true);


                if (bmodel.isEdit()) {
                    bmodel.loadOrderedProducts(bo.getRetailerID(), orderId);
                }


                bmodel.productHelper.loadRetailerWiseProductWisePurchased();
                bmodel.productHelper
                        .loadRetailerWiseProductWiseP4StockAndOrderQty();

                /** Load Initiative **/
                bmodel.productHelper.loadInitiativeProducts();
                bmodel.initiativeHelper.downloadInitiativeHeader(bo
                        .getSubchannelid());

                /** Load SO Norm **/
                if (bmodel.configurationMasterHelper.IS_SUGGESTED_ORDER) {
                    bmodel.productHelper.loadRetailerWiseInventoryOrderQty();
                }

                if (bmodel.configurationMasterHelper.IS_PRODUCT_DISPLAY_FOR_PIRAMAL)
                    bmodel.productHelper.updateProductColorAndSequance();

                /** Load Order History **/
                bmodel.initiativeHelper.loadLocalOrdersQty(bo.getRetailerID());

                /** Settign color **/
                bmodel.configurationMasterHelper.downloadFilterList();
                bmodel.productHelper.updateProductColor();
                bmodel.setRetailerMasterBO(bo);
                bmodel.configurationMasterHelper
                        .downloadProductDetailsList();
                bmodel.productHelper.downloadInStoreLocations();
            } catch (Exception e) {
                Commons.printException(e);
            }
            return Boolean.TRUE; // Return your real result here
        }

        protected void onPreExecute() {
        /*	progressDialogue = ProgressDialog.show(OrderSplitMasterScreen.this,
                    DataMembers.SD, getResources().getString(R.string.loading),
					true, false);*/
            builder = new AlertDialog.Builder(OrderSplitMasterScreen.this);

            customProgressDialog(builder,  getResources().getString(R.string.loading));
            alertDialog = builder.create();
            alertDialog.show();

        }

        protected void onProgressUpdate(Integer... progress) {

        }

        protected void onPostExecute(Boolean result) {

            alertDialog.dismiss();
            //progressDialogue.dismiss();
            bmodel.mSelectedModule = 3;
            if (bmodel.isEdit()) {
                Intent intent = new Intent(OrderSplitMasterScreen.this,
                        OrderSummary.class);
                startActivity(intent);
                finish();

            }

        }

    }

}
