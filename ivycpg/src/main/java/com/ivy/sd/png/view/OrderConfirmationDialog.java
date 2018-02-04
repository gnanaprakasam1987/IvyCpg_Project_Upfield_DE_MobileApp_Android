package com.ivy.sd.png.view;

import android.app.Dialog;
import android.content.Context;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.ivy.cpg.view.order.OrderSummary;
import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.bo.ConfigureBO;
import com.ivy.sd.png.bo.ProductMasterBO;
import com.ivy.sd.png.bo.ReasonMaster;
import com.ivy.sd.png.bo.SupplierMasterBO;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.provider.ConfigurationMasterHelper;
import com.ivy.sd.png.util.Commons;

import java.util.ArrayList;
import java.util.LinkedList;

/**
 * Created by Rajkumar on 12/1/18.
 * Order confirmation dialog
 */

public class OrderConfirmationDialog extends Dialog implements View.OnClickListener {


    private OnConfirmationResult dialogInterface;
    private BusinessModel businessModel;
    private Context context;

    private TextView textView_shipment_label, textView_payment_label, textView_channel_label, textView_delivery_label, textView_delivery;
    private TextView textView_supplier_label, textView_note, textView_note_label, textView_order_value, textView_order_value_label;
    private Spinner spinner_shipment, spinner_payment, spinner_dist_channel;
    private LinearLayout layout_shipment, layout_payment, layout_channel, layout_delivery_date, layout_supplier, layout_note, layout_order_value;
    private AutoCompleteTextView autoCompleteTextView_suppliers;

    private boolean isMandatory_shipment, isMandatory_payterm, isMandatory_channel;
    private boolean isInvoice;


    private static final String SHIPMENT_TYPE = "SHIPMENT_TYPE";
    private static final String PAYTERM_TYPE = "PAYTERM_TYPE";
    private static final String DIST_CHANNEL_TYPE = "DIST_CHANNEL_TYPE";
    private static final String DELIVERY_DATE = "DELIVERY_DATE";
    private static final String SUPPLIER_SELECTION = "SUPPLIER_SELECTION";
    private static final String NOTE = "NOTE";
    private static final String ORDER_VALUE = "ORDER_VALUE";


    public OrderConfirmationDialog(Context context, boolean isInvoice, LinkedList<ProductMasterBO> mOrderedProductList, double orderValue) {
        super(context);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_order_confirmation);

        try {
            this.context = context;
            this.isInvoice = isInvoice;
            dialogInterface = (OrderSummary) context;
            businessModel = (BusinessModel) context.getApplicationContext();

            initializeViews();

            ArrayAdapter<ReasonMaster> shipment_adapter, payment_adapter, channel_adapter;
            ArrayList<ConfigureBO> list = businessModel.productHelper.downloadOrderSummaryDialogFields(context);
            for (ConfigureBO configureBO : list) {
                if (configureBO.getConfigCode().equals(SHIPMENT_TYPE)) {
                    layout_shipment.setVisibility(View.VISIBLE);
                    textView_shipment_label.setText(configureBO.getMenuName());

                    if (configureBO.getMandatory() == 1) {
                        findViewById(R.id.shipment_mandatory).setVisibility(View.VISIBLE);
                        isMandatory_shipment = true;
                    }
                    businessModel.reasonHelper.downloadShipmentType();
                    shipment_adapter = new ArrayAdapter<>(context,
                            R.layout.spinner_bluetext_layout);
                    shipment_adapter.add(new ReasonMaster(0 + "", context.getResources().getString(R.string.select_shipment_type)));
                    int count = 0, selectedPos = -1;
                    for (ReasonMaster temp : businessModel.reasonHelper
                            .getShipMentType()) {
                        if (temp.getReasonDesc().equals(businessModel.getRField1()))
                            selectedPos = count + 1;
                        shipment_adapter.add(temp);
                        count++;
                    }
                    shipment_adapter
                            .setDropDownViewResource(R.layout.spinner_bluetext_list_item);
                    spinner_shipment.setAdapter(shipment_adapter);
                    spinner_shipment.setSelection(selectedPos);
                    spinner_shipment.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                        }

                        @Override
                        public void onNothingSelected(AdapterView<?> adapterView) {

                        }
                    });
                } else if (configureBO.getConfigCode().equals(PAYTERM_TYPE)) {
                    layout_payment.setVisibility(View.VISIBLE);
                    textView_payment_label.setText(configureBO.getMenuName());

                    if (configureBO.getMandatory() == 1) {
                        findViewById(R.id.payment_mandatory).setVisibility(View.VISIBLE);
                        isMandatory_payterm = true;
                    }

                    businessModel.reasonHelper.downloadPaymentType();
                    payment_adapter = new ArrayAdapter<>(context,
                            R.layout.spinner_bluetext_layout);
                    payment_adapter.add(new ReasonMaster(0 + "", context.getResources().getString(R.string.select_pay_term_type)));
                    int count = 0, selectedPos = -1;
                    for (ReasonMaster temp : businessModel.reasonHelper
                            .getPayTermType()) {
                        if (temp.getReasonDesc().equals(businessModel.getRField2()))
                            selectedPos = count + 1;
                        payment_adapter.add(temp);
                        count++;
                    }
                    payment_adapter
                            .setDropDownViewResource(R.layout.spinner_bluetext_list_item);
                    spinner_payment.setAdapter(payment_adapter);
                    spinner_payment.setSelection(selectedPos);
                    spinner_payment.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                        }

                        @Override
                        public void onNothingSelected(AdapterView<?> adapterView) {

                        }
                    });

                } else if (configureBO.getConfigCode().equals(DIST_CHANNEL_TYPE)) {
                    layout_channel.setVisibility(View.VISIBLE);
                    textView_channel_label.setText(configureBO.getMenuName());

                    if (configureBO.getMandatory() == 1) {
                        findViewById(R.id.channel_mandatory).setVisibility(View.VISIBLE);
                        isMandatory_channel = true;
                    }

                    businessModel.reasonHelper.downloadDistChannelType();
                    channel_adapter = new ArrayAdapter<>(context,
                            R.layout.spinner_bluetext_layout);
                    channel_adapter.add(new ReasonMaster(0 + "", context.getResources().getString(R.string.select_channel)));
                    int count = 0, selectedPos = -1;
                    for (ReasonMaster temp : businessModel.reasonHelper
                            .getDistributionChannelType()) {
                        if (temp.getReasonDesc().equals(businessModel.getRField3()))
                            selectedPos = count + 1;
                        channel_adapter.add(temp);
                        count++;
                    }
                    channel_adapter
                            .setDropDownViewResource(R.layout.spinner_bluetext_list_item);
                    spinner_dist_channel.setAdapter(channel_adapter);
                    spinner_dist_channel.setSelection(selectedPos);
                    spinner_dist_channel.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                        @Override
                        public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                        }

                        @Override
                        public void onNothingSelected(AdapterView<?> adapterView) {

                        }
                    });

                } else if (configureBO.getConfigCode().equals(DELIVERY_DATE)) {
                    layout_delivery_date.setVisibility(View.VISIBLE);
                    textView_delivery_label.setText(configureBO.getMenuName());
                    textView_delivery.setText(businessModel.getOrderHeaderBO().getDeliveryDate());
                } else if (configureBO.getConfigCode().equals(SUPPLIER_SELECTION)) {
                    layout_supplier.setVisibility(View.VISIBLE);
                    textView_supplier_label.setText(configureBO.getMenuName());

                    ArrayList<SupplierMasterBO> mSupplierList = businessModel.downloadSupplierDetails();
                    ArrayAdapter<SupplierMasterBO> mSupplierAdapter = new ArrayAdapter<>(context,
                            R.layout.autocompelete_bluetext_layout, mSupplierList);
                    mSupplierAdapter.setDropDownViewResource(R.layout.autocomplete_bluetext_list_item);
                    autoCompleteTextView_suppliers.setAdapter(mSupplierAdapter);
                    //  autoCompleteTextView_suppliers.setThreshold(1);

                    autoCompleteTextView_suppliers.setOnTouchListener(new View.OnTouchListener() {
                        @Override
                        public boolean onTouch(View view, MotionEvent motionEvent) {
                            autoCompleteTextView_suppliers.showDropDown();
                            return false;
                        }
                    });

                    autoCompleteTextView_suppliers.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> parent, View view, int pos, long id) {
                            businessModel.getRetailerMasterBO().setDistributorId(((SupplierMasterBO) parent.getItemAtPosition(pos)).getSupplierID());
                            businessModel.getRetailerMasterBO().setDistParentId(((SupplierMasterBO) parent.getItemAtPosition(pos)).getDistParentID());
                        }
                    });

                    int position = 0;
                    for (SupplierMasterBO supplierBO : mSupplierList) {
                        if (businessModel.getRetailerMasterBO().getDistributorId() == supplierBO.getSupplierID()) {
                            autoCompleteTextView_suppliers.setText(supplierBO.getSupplierName());
                            break;
                        } else {
                            position++;
                        }
                    }
                    autoCompleteTextView_suppliers.setSelection(position);
                    mSupplierAdapter.notifyDataSetChanged();
                    autoCompleteTextView_suppliers.dismissDropDown();

                    //
                } else if (configureBO.getConfigCode().equals(NOTE)) {
                    layout_note.setVisibility(View.VISIBLE);
                    textView_note_label.setText(configureBO.getMenuName());
                    if (isExceptionalOrder(mOrderedProductList)) {
                        textView_note.setText(context.getResources().getString(R.string.this_is_exceptional_order));
                        textView_note.setTextColor(context.getResources().getColor(R.color.RED));
                    } else {
                        textView_note.setText("-");
                    }

                } else if (configureBO.getConfigCode().equals(ORDER_VALUE)) {
                    layout_order_value.setVisibility(View.VISIBLE);
                    textView_order_value.setText(String.valueOf(orderValue));
                    textView_order_value_label.setText(configureBO.getMenuName());


                }
            }
        } catch (Exception ex) {
            Commons.printException(ex);
        }

    }

    private void initializeViews() {

        Button button_save = (Button) findViewById(R.id.btn_ok);
        button_save.setOnClickListener(this);
        Button button_cancel = (Button) findViewById(R.id.btn_cancel);
        button_cancel.setOnClickListener(this);

        spinner_shipment = (Spinner) findViewById(R.id.spinner_shipment_type);
        spinner_payment = (Spinner) findViewById(R.id.spinner_payment_type);
        spinner_dist_channel = (Spinner) findViewById(R.id.spinner_distribution_channel__type);
        textView_delivery = (TextView) findViewById(R.id.text_delivery_date);
        autoCompleteTextView_suppliers = (AutoCompleteTextView) findViewById(R.id.autoCompleteTextView_supplier);
        textView_note = (TextView) findViewById(R.id.text_note);
        textView_order_value = (TextView) findViewById(R.id.text_order_value);

        textView_shipment_label = (TextView) findViewById(R.id.label_shipment);
        textView_payment_label = (TextView) findViewById(R.id.label_payment);
        textView_channel_label = (TextView) findViewById(R.id.label_distribution_channel);
        textView_delivery_label = (TextView) findViewById(R.id.label_delivery_date);
        textView_supplier_label = (TextView) findViewById(R.id.label_supplier);
        textView_note_label = (TextView) findViewById(R.id.label_note);
        textView_order_value_label = (TextView) findViewById(R.id.label_order_value);

        layout_shipment = (LinearLayout) findViewById(R.id.layout_shipment_type);
        layout_payment = (LinearLayout) findViewById(R.id.layout_payment_type);
        layout_channel = (LinearLayout) findViewById(R.id.layout_distribution_channel_type);
        layout_delivery_date = (LinearLayout) findViewById(R.id.layout_delivery_date);
        layout_supplier = (LinearLayout) findViewById(R.id.layout_supplier);
        layout_note = (LinearLayout) findViewById(R.id.layout_note);
        layout_order_value = (LinearLayout) findViewById(R.id.layout_order_value);

        TextView text_label = (TextView) findViewById(R.id.text_label);

        textView_delivery.setTypeface(businessModel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
        textView_note.setTypeface(businessModel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
        textView_order_value.setTypeface(businessModel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));

        textView_shipment_label.setTypeface(businessModel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
        textView_payment_label.setTypeface(businessModel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
        text_label.setTypeface(businessModel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
        textView_channel_label.setTypeface(businessModel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
        textView_supplier_label.setTypeface(businessModel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
        textView_delivery_label.setTypeface(businessModel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
        textView_note_label.setTypeface(businessModel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
        textView_order_value_label.setTypeface(businessModel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));

    }

    private boolean isExceptionalOrder(LinkedList<ProductMasterBO> mOrderedProductList) {
        for (ProductMasterBO bo : mOrderedProductList) {
            if ((bo.getD1() + bo.getD2() + bo.getD3()) >= 100 || bo.getDiscount_order_value() <= 0) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.btn_ok) {

            try {
                if (isMandatory_shipment && ((ReasonMaster) spinner_shipment.getSelectedItem()).getReasonID().equals("0")) {
                    Toast.makeText(context, context.getResources().getString(R.string.shipment_mandatory), Toast.LENGTH_LONG).show();
                    return;
                }

                if (isMandatory_payterm && ((ReasonMaster) spinner_payment.getSelectedItem()).getReasonID().equals("0")) {
                    Toast.makeText(context, context.getResources().getString(R.string.pay_term_mandatory), Toast.LENGTH_LONG).show();
                    return;
                }
                if (isMandatory_channel && ((ReasonMaster) spinner_dist_channel.getSelectedItem()).getReasonID().equals("0")) {
                    Toast.makeText(context, context.getResources().getString(R.string.dist_channel_mandatory), Toast.LENGTH_LONG).show();
                    return;
                }

                if (spinner_shipment.getSelectedItem() != null)
                    businessModel.setRField1(((ReasonMaster) spinner_shipment.getSelectedItem()).getReasonID());

                if (spinner_payment.getSelectedItem() != null)
                    businessModel.setRField2(((ReasonMaster) spinner_payment.getSelectedItem()).getReasonID());

                if (spinner_dist_channel.getSelectedItem() != null)
                    businessModel.setRField3(((ReasonMaster) spinner_dist_channel.getSelectedItem()).getReasonID());

                dialogInterface.save(isInvoice);
            } catch (Exception ex) {
                Commons.printException(ex);
            }
        } else if (view.getId() == R.id.btn_cancel) {
            dialogInterface.dismiss();
            dismiss();
        }

    }

    public interface OnConfirmationResult {
        void save(boolean isInvoice);

        void dismiss();
    }
}
