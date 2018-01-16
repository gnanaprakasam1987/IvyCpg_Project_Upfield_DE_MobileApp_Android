package com.ivy.sd.png.view;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.bo.ConfigureBO;
import com.ivy.sd.png.bo.ReasonMaster;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.provider.ConfigurationMasterHelper;
import com.ivy.sd.png.util.Commons;

import java.util.ArrayList;

/**
 * Created by Rajkumar on 12/1/18.
 * Order confirmation
 */

public class OrderConfirmationDialog extends Dialog implements View.OnClickListener {


    private boolean isInvoice;
    private OnConfirmationResult dialogInterface;
    private Button button_save,button_cancel;
    BusinessModel businessModel;

    TextView textView_shipment_label,textView_payment_label;
    Spinner spinner_shipment,spinner_payment;
    LinearLayout layout_shipment,layout_payment;
    private ArrayAdapter<ReasonMaster> shipment_adapter,payment_adapter;
    private boolean isMandatory_shipment,isMandatory_payterm;
    private Context context;
    private TextView titleBar;

    public OrderConfirmationDialog(Context context,boolean isInvoice){
        super(context);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_order_confirmation);

        try {
            this.context = context;
            this.isInvoice = isInvoice;
            dialogInterface = (OrderSummary) context;
            businessModel = (BusinessModel) context.getApplicationContext();

            button_save = (Button) findViewById(R.id.btn_ok);
            button_save.setOnClickListener(this);
            button_cancel = (Button) findViewById(R.id.btn_cancel);
            button_cancel.setOnClickListener(this);

            spinner_shipment = (Spinner) findViewById(R.id.spinner_shipment_type);
            spinner_payment = (Spinner) findViewById(R.id.spinner_payment_type);
            textView_shipment_label = (TextView) findViewById(R.id.label_shipment);
            textView_payment_label = (TextView) findViewById(R.id.label_payment);

            layout_shipment = (LinearLayout) findViewById(R.id.layout_shipment_type);
            layout_payment = (LinearLayout) findViewById(R.id.layout_payment_type);
            titleBar=(TextView)findViewById(R.id.titleBar);

            titleBar.setTypeface(businessModel.configurationMasterHelper.getFontBaloobhai(ConfigurationMasterHelper.FontType.REGULAR));
            textView_shipment_label.setTypeface(businessModel.configurationMasterHelper.getFontBaloobhai(ConfigurationMasterHelper.FontType.LIGHT));
            textView_payment_label.setTypeface(businessModel.configurationMasterHelper.getFontBaloobhai(ConfigurationMasterHelper.FontType.LIGHT));

            ArrayList<ConfigureBO> list = businessModel.productHelper.downloadOrderSummaryDialogFields(context);
            for (ConfigureBO configureBO : list) {
                if (configureBO.getConfigCode().equals("SHIPMENT_TYPE")) {
                    layout_shipment.setVisibility(View.VISIBLE);
                    textView_shipment_label.setText(configureBO.getMenuName());

                    if (configureBO.getMandatory() == 1) {
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
                }

                if (configureBO.getConfigCode().equals("PAYTERM_TYPE")) {
                    layout_payment.setVisibility(View.VISIBLE);
                    textView_payment_label.setText(configureBO.getMenuName());

                    if (configureBO.getMandatory() == 1) {
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

                }
            }
        }
        catch (Exception ex){
            Commons.printException(ex);
        }

    }

    @Override
    public void onClick(View view) {
        if(view.getId()==R.id.btn_ok){

            try {
                if (isMandatory_shipment && ((ReasonMaster) spinner_shipment.getSelectedItem()).getReasonID().equals("0")) {
                    Toast.makeText(context, context.getResources().getString(R.string.shipment_mandatory), Toast.LENGTH_LONG).show();
                    return;
                }

                if (isMandatory_payterm && ((ReasonMaster) spinner_payment.getSelectedItem()).getReasonID().equals("0")) {
                    Toast.makeText(context, context.getResources().getString(R.string.pay_term_mandatory), Toast.LENGTH_LONG).show();
                    return;
                }

                businessModel.setRField1(((ReasonMaster) spinner_shipment.getSelectedItem()).getReasonID());
                businessModel.setRField2(((ReasonMaster) spinner_payment.getSelectedItem()).getReasonID());

                dialogInterface.save(isInvoice);
            }
            catch (Exception ex){
                Commons.printException(ex);
            }
        }
        else  if(view.getId()==R.id.btn_cancel){
            dialogInterface.dismiss();
            dismiss();
        }

    }

    public interface OnConfirmationResult {
        void save(boolean isInvoice);
        void dismiss();
    }
}
