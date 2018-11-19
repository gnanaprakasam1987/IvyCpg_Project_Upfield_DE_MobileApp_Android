package com.ivy.cpg.view.delivery.kellogs;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
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

import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.bo.OrderHeader;
import com.ivy.sd.png.commons.IvyBaseActivityNoActionBar;
import com.ivy.sd.png.commons.SDUtil;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.provider.ConfigurationMasterHelper;
import com.ivy.sd.png.util.CommonDialog;
import com.ivy.sd.png.util.Commons;
import com.ivy.sd.png.util.DataMembers;
import com.ivy.sd.png.util.DateUtil;
import com.ivy.sd.png.util.StandardListMasterConstants;
import com.ivy.sd.png.view.HomeScreenTwo;
import com.ivy.sd.print.CommonPrintPreviewActivity;
import com.ivy.utils.FontUtils;

import java.util.ArrayList;

public class OrderDeliveryActivity extends IvyBaseActivityNoActionBar {

    private BusinessModel bmodel;
    private RecyclerView recyclerView;
    OrderDeliveryHelper orderDeliveryHelper;
    ArrayList<OrderHeader> orderHeaders = new ArrayList<>();
    private MyAdapter myAdapter;
    final String ACCEPT = "ACCEPT";
    final String VIEW = "VIEW";
    final String EDIT = "EDIT";
    final String REJECT = "REJECT";
    private boolean isClicked = false, isClickedAccept = false, isClickedEdit = false;

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
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            String title = extras.getString("menuName") == null ? "" : extras.getString("menuName");
            setScreenTitle(title);
        }

        orderDeliveryHelper = OrderDeliveryHelper.getInstance(this);

        orderDeliveryHelper.downloadOrderDeliveryHeader(this);
        orderHeaders.addAll(orderDeliveryHelper.getOrderHeaders());

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
        isClicked = false;
        isClickedAccept = false;
        isClickedEdit = false;
        //session out if user id becomes 0
        if (bmodel.userMasterHelper.getUserMasterBO().getUserid() == 0) {
            Toast.makeText(this,
                    getResources().getString(R.string.sessionout_loginagain),
                    Toast.LENGTH_SHORT).show();
            finish();
        }

        prepareScreenData();
    }

    private void prepareScreenData() {
        myAdapter = new MyAdapter();
        recyclerView.setAdapter(myAdapter);
        myAdapter.notifyDataSetChanged();
    }

    public class MyAdapter extends RecyclerView.Adapter<MyAdapter.MyViewHolder> {


        public class MyViewHolder extends RecyclerView.ViewHolder {

            private TextView orderId, orderDate, orderValue, orderLine, invoiceGeneratedText;
            private Button orderAccept, orderEdit,orderReject;

            public MyViewHolder(View view) {
                super(view);

                orderId = view.findViewById(R.id.order_delivery_listview_orderid);
                orderDate = view.findViewById(R.id.order_delivery_listview_orderdate);
                orderValue = view.findViewById(R.id.order_delivery_listview_ordervalue);
                orderLine = view.findViewById(R.id.order_delivery_listview_orderlines);
                invoiceGeneratedText = view.findViewById(R.id.invoice_generated_text);
                orderReject = view.findViewById(R.id.reject_btn);

                orderAccept = view.findViewById(R.id.accept_btn);
                orderEdit = view.findViewById(R.id.edit_btn);

                orderId.setTypeface(FontUtils.getFontRoboto(FontUtils.FontType.MEDIUM, OrderDeliveryActivity.this));
                orderDate.setTypeface(FontUtils.getFontRoboto(FontUtils.FontType.MEDIUM, OrderDeliveryActivity.this));
                orderValue.setTypeface(FontUtils.getFontRoboto(FontUtils.FontType.MEDIUM, OrderDeliveryActivity.this));
                orderLine.setTypeface(FontUtils.getFontRoboto(FontUtils.FontType.MEDIUM, OrderDeliveryActivity.this));

                ((TextView) view.findViewById(R.id.order_delivery_listview_id_heading)).setTypeface(FontUtils.getFontRoboto(FontUtils.FontType.LIGHT, OrderDeliveryActivity.this));
                ((TextView) view.findViewById(R.id.order_delivery_listview_date_heading)).setTypeface(FontUtils.getFontRoboto(FontUtils.FontType.LIGHT, OrderDeliveryActivity.this));
                ((TextView) view.findViewById(R.id.order_delivery_listview_line_head)).setTypeface(FontUtils.getFontRoboto(FontUtils.FontType.LIGHT, OrderDeliveryActivity.this));
                ((TextView) view.findViewById(R.id.order_delivery_listview_value_heading)).setTypeface(FontUtils.getFontRoboto(FontUtils.FontType.LIGHT, OrderDeliveryActivity.this));

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
                    if (!isClicked) {
                    if(orderHeaders.get(position).getInvoiceStatus()!=1 && !orderHeaders.get(position).getOrderStatus().equals("R")) {
                        new DownloadOrderDeliveryDetail(orderHeaders.get(position).getOrderid(),
                                VIEW,
                                orderHeaders.get(position).getInvoiceStatus(),
                                orderHeaders.get(position).getrField3()).execute();
                    }
                    else {
                        if(orderHeaders.get(position).getInvoiceStatus()==1) {
                            Toast.makeText(
                                    OrderDeliveryActivity.this,
                                    getResources().getString(R.string.already_invoice_has_been_generated),
                                    Toast.LENGTH_SHORT).show();
                        }else if(orderHeaders.get(position).getOrderStatus().equals("R"))
                            Toast.makeText(
                                    OrderDeliveryActivity.this,
                                    getResources().getString(R.string.rejected_order),
                                    Toast.LENGTH_SHORT).show();
                    }    isClicked = true;
                    }

                }
            });

            holder.orderEdit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
if (!isClickedEdit) {
                    if(orderHeaders.get(position).getInvoiceStatus() == 1) {
                        Toast.makeText(
                                OrderDeliveryActivity.this,
                                "Already invoice has been generated",
                                Toast.LENGTH_SHORT).show();
                        return;
                    }
isClickedEdit = true;
                    new DownloadOrderDeliveryDetail(orderHeaders.get(position).getOrderid(),EDIT,orderHeaders.get(position).getInvoiceStatus(),
                            orderHeaders.get(position).getrField3()).execute();}
                }
            });

            holder.orderAccept.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (!isClickedAccept) {
                    if (orderHeaders.get(position).getInvoiceStatus() == 1) {
                        Toast.makeText(
                                OrderDeliveryActivity.this,
                                "Already invoice has been generated",
                                Toast.LENGTH_SHORT).show();
                        return;
                    }
                        isClickedAccept = true;
                    new DownloadOrderDeliveryDetail(orderHeaders.get(position).getOrderid(),
                            ACCEPT,
                            orderHeaders.get(position).getInvoiceStatus(),
                            orderHeaders.get(position).getrField3()).execute();

                }}
            });

            holder.orderReject.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    new DownloadOrderDeliveryDetail(orderHeaders.get(position).getOrderid(),
                            REJECT,
                            orderHeaders.get(position).getInvoiceStatus(),
                            orderHeaders.get(position).getrField3()).execute();

                }
            });


            if (orderHeaders.get(position).getInvoiceStatus() == 1
                    || orderHeaders.get(position).getOrderStatus().equals("R")) {
                holder.orderAccept.setVisibility(View.GONE);
                holder.orderEdit.setVisibility(View.GONE);
                holder.orderReject.setVisibility(View.GONE);
                holder.invoiceGeneratedText.setVisibility(View.VISIBLE);

                if (orderHeaders.get(position).getOrderStatus().equals("R"))
                    holder.invoiceGeneratedText.setText(getResources().getString(R.string.order_rejected));
                else
                    holder.invoiceGeneratedText.setText(getResources().getString(R.string.invoice_already_generated));

            }

        }

        @Override
        public int getItemCount() {
            return orderHeaders.size();
        }
    }

    private void rejectOrder(final String orderId, final String refId){
        CommonDialog dialog = new CommonDialog(getApplicationContext(), OrderDeliveryActivity.this, "", getResources().getString(R.string.do_you_want_to_reject), false,
                getResources().getString(R.string.ok), getResources().getString(R.string.cancel), new CommonDialog.PositiveClickListener() {
            @Override
            public void onPositiveButtonClick() {

                orderDeliveryHelper.updateRejectedOrder(OrderDeliveryActivity.this,orderId,refId);

                for (OrderHeader orderHeader : orderHeaders){
                    if (orderHeader.getOrderid().equals(orderId)) {
                        orderHeader.setOrderStatus("R");
                        break;
                    }
                }
                myAdapter.notifyDataSetChanged();

            }
        }, new CommonDialog.negativeOnClickListener() {
            @Override
            public void onNegativeButtonClick() {

            }
        });
        dialog.show();
        dialog.setCancelable(false);
    }

    class DownloadOrderDeliveryDetail extends AsyncTask<Void, Void, Void> {

        private String orderId;
        private String from;
        private int invoiceStatus;
        private String referenceId;

        private DownloadOrderDeliveryDetail(String orderId, String from, int invoiceStatus, String referenceId) {
            this.orderId = orderId;
            this.from = from;
            this.invoiceStatus = invoiceStatus;
            this.referenceId = referenceId;
        }

        @Override
        protected Void doInBackground(Void... voids) {
            bmodel.productHelper.clearOrderTable();
            orderDeliveryHelper.clearSalesReturnTable();
            orderDeliveryHelper.downloadOrderDeliveryDetail(OrderDeliveryActivity.this, orderId);
            orderDeliveryHelper.downloadSchemeFreeProducts(OrderDeliveryActivity.this, orderId);
            orderDeliveryHelper.downloadOrderDeliveryAmountDetail(OrderDeliveryActivity.this, orderId);
            orderDeliveryHelper.downloadOrderedProducts();
            if (from.equalsIgnoreCase(ACCEPT) || from.equalsIgnoreCase(REJECT)) {
                orderDeliveryHelper.getProductTotalValue(OrderDeliveryActivity.this, false);
            }
            if (bmodel.configurationMasterHelper.SHOW_DISC_AMOUNT_ALLOW) {
                bmodel.collectionHelper.downloadDiscountSlab();
            }
            if (from.equalsIgnoreCase(ACCEPT)) {
                orderDeliveryHelper.updateDiscountInLineValue(OrderDeliveryActivity.this, orderId);
            }

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {

            if (from.equalsIgnoreCase(REJECT)) {
                rejectOrder(orderId,referenceId);
            }
            else if (from.equalsIgnoreCase(ACCEPT)) {

                if (orderDeliveryHelper.getTotalProductQty() == 0) {
                    Toast.makeText(
                            OrderDeliveryActivity.this,
                            getResources().getString(R.string.no_ordered_products_found),
                            Toast.LENGTH_SHORT).show();
                    isClickedAccept = false;
                } else if (orderDeliveryHelper.isSIHAvailable(false)) {

                    CommonDialog dialog = new CommonDialog(getApplicationContext(), OrderDeliveryActivity.this, "", getResources().getString(R.string.order_delivery_approve), false,
                            getResources().getString(R.string.ok), getResources().getString(R.string.cancel), new CommonDialog.PositiveClickListener() {
                        @Override
                        public void onPositiveButtonClick() {

                            boolean status = orderDeliveryHelper.updateTableValues(OrderDeliveryActivity.this, orderId, false, getIntent().getExtras().getString("menuCode"), referenceId);
                            if (status) {
                                bmodel.saveModuleCompletion(getIntent().getExtras().getString("menuCode"));
                                Toast.makeText(
                                        OrderDeliveryActivity.this,
                                        getResources().getString(R.string.invoice_generated),
                                        Toast.LENGTH_SHORT).show();

                                orderDeliveryHelper.updateTaxInLineValue(OrderDeliveryActivity.this, orderId);

                                orderDeliveryHelper.getOrderedProductMasterBOS().get(orderDeliveryHelper.getOrderedProductMasterBOS().size() - 1).
                                        setSchemeProducts(orderDeliveryHelper.downloadSchemeFreePrint(OrderDeliveryActivity.this, orderId));

                                bmodel.outletTimeStampHelper.updateTimeStampModuleWise(SDUtil
                                        .now(SDUtil.TIME));

                                bmodel.mCommonPrintHelper.xmlRead("invoice", false, orderDeliveryHelper.getOrderedProductMasterBOS(), null, null, null,null);

                                bmodel.writeToFile(String.valueOf(bmodel.mCommonPrintHelper.getInvoiceData()),
                                        StandardListMasterConstants.PRINT_FILE_INVOICE + bmodel.invoiceNumber, "/" + DataMembers.PRINT_FILE_PATH);
                                Intent i = new Intent(OrderDeliveryActivity.this,
                                        CommonPrintPreviewActivity.class);
                                i.putExtra("IsFromOrder", false);
                                i.putExtra("IsUpdatePrintCount", true);
                                i.putExtra("isHomeBtnEnable", true);
                                i.putExtra("sendMailAndLoadClass", "PRINT_FILE_INVOICE");
                                startActivity(i);
                                overridePendingTransition(R.anim.trans_left_in, R.anim.trans_left_out);
                            } else
                                Toast.makeText(
                                        OrderDeliveryActivity.this,
                                        getResources().getString(R.string.not_able_to_generate_invoice),
                                        Toast.LENGTH_SHORT).show();

                        }
                    }, new CommonDialog.negativeOnClickListener() {
                        @Override
                        public void onNegativeButtonClick() {
                            isClickedAccept = false;
                        }
                    });
                    dialog.show();
                    dialog.setCancelable(false);
                } else {
                    Toast.makeText(
                            OrderDeliveryActivity.this,
                            getResources().getString(R.string.ordered_value_exceeds_sih_value_please_edit_the_order),
                            Toast.LENGTH_SHORT).show();
                    isClickedAccept = false;
                }
            } else {
                Intent intent = new Intent(OrderDeliveryActivity.this, OrderDeliveryDetailActivity.class);
                intent.putExtra("From", from);
                intent.putExtra("OrderId", orderId);
                intent.putExtra("RefId", referenceId);
                intent.putExtra("InvoiceStatus", invoiceStatus);
                intent.putExtra("menuCode", getIntent().getExtras().getString("menuCode"));
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
