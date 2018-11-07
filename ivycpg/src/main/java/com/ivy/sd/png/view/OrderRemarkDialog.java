package com.ivy.sd.png.view;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.Context;
import android.text.InputFilter;
import android.text.Spanned;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.ivy.cpg.view.order.OrderHelper;
import com.ivy.lib.Utils;
import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.provider.ConfigurationMasterHelper;
import com.ivy.sd.png.util.Commons;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.StringTokenizer;

public class OrderRemarkDialog extends Dialog implements OnClickListener {

    private BusinessModel bmodel;

    private EditText mEdtPO;
    private EditText mEdtRemark;
    private Button mBtnDate, mBtnClose;

    Date date;
    private String mnextDate;
    public String mdate_selected;
    private Context con;
    private boolean isFrmDelivery = false;

    public OrderRemarkDialog(Context context, boolean isFromDelivery) {
        super(context);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_order_remarks);

        mEdtPO = (EditText) findViewById(R.id.edt_po);
        mEdtRemark = (EditText) findViewById(R.id.edt_remark);
        mBtnDate = (Button) findViewById(R.id.Btn_deliveryDate);
        mBtnClose = (Button) findViewById(R.id.closeButton);

        InputFilter filter = new InputFilter() {
            @Override
            public CharSequence filter(CharSequence source, int start, int end, Spanned dest, int dstart, int dend) {
                for (int i = start; i < end; i++) {
                    String specialChars = "\"'<>";

                    int type = Character.getType(source.charAt(i));
                    if (type == Character.SURROGATE || type == Character.OTHER_SYMBOL
                            || specialChars.contains("" + source)
                            || Character.isWhitespace(0)) {
                        return "";
                    }
                }
                return null;
            }
        };

        mEdtRemark.setFilters(new InputFilter[]{filter});


        bmodel = (BusinessModel) context.getApplicationContext();
        con = context;
        isFrmDelivery = isFromDelivery;

        try {
            if (bmodel.labelsMasterHelper.applyLabels(findViewById(
                    R.id.txt_po).getTag()) != null)
                ((TextView) findViewById(R.id.txt_po))
                        .setText(bmodel.labelsMasterHelper
                                .applyLabels(findViewById(R.id.txt_po)
                                        .getTag()));
            ((TextView) findViewById(R.id.txt_po)).setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));

            if (bmodel.labelsMasterHelper.applyLabels(findViewById(
                    R.id.textView5).getTag()) != null)
                ((TextView) findViewById(R.id.textView5))
                        .setText(bmodel.labelsMasterHelper
                                .applyLabels(findViewById(R.id.textView5)
                                        .getTag()));
            ((TextView) findViewById(R.id.textView5)).setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));

            String rField = bmodel.configurationMasterHelper.LOAD_ORDER_SUMMARY_REMARKS_FIELD_STRING;
            StringTokenizer stringtokenizer = new StringTokenizer(rField, ",");
            while (stringtokenizer.hasMoreElements()) {
                String token = stringtokenizer.nextToken();
                if (token.contains("PO")) {
                    findViewById(R.id.po_lty).setVisibility(View.VISIBLE);
                }
            }

            if (isFrmDelivery) {
                findViewById(R.id.po_lty).setVisibility(View.GONE);
                mEdtPO.setVisibility(View.GONE);
                mBtnDate.setVisibility(View.GONE);
            } else {
                mEdtPO.setVisibility(View.VISIBLE);
                mBtnDate.setVisibility(View.VISIBLE);
            }


        } catch (Exception e) {
            Commons.printException(e);
        }

        if (!isFrmDelivery) {
            getNextDate();
            if (bmodel.isEdit()) {
                mBtnDate.setText(Utils.formatDateAsUserRequired(bmodel
                        .getDeliveryDate(OrderHelper.getInstance(con).selectedOrderId,bmodel.getRetailerMasterBO()
                                .getRetailerID()), "yyyy/MM/dd", "MM/dd/yyyy"));
            } else {
                mBtnDate.setText(mnextDate + "");
            }
            mEdtPO.setText(bmodel.getOrderHeaderBO().getPO());
        }
        mEdtRemark.setText(bmodel.getOrderHeaderNote());
        mBtnDate.setOnClickListener(this);
        mBtnClose.setOnClickListener(this);


    }

    private String getNextDate() {
        Calendar origDay = Calendar.getInstance();
        Calendar nextDay = (Calendar) origDay.clone();
        SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy");
        nextDay.add(Calendar.DAY_OF_YEAR, 1);
        return mnextDate = sdf.format(nextDay.getTime()) + "";

    }

    protected Dialog onCreateDialog() {

        Calendar c = Calendar.getInstance();
        c.add(Calendar.DAY_OF_YEAR, 1);
        int cyear = c.get(Calendar.YEAR);
        int cmonth = c.get(Calendar.MONTH);
        int cday = c.get(Calendar.DAY_OF_MONTH);

        // todayDate = cday + "/" + cmonth + "/" + cyear;
        mnextDate = (cmonth + 1)
                + "/" + (cday)
                + "/" + cyear;

        return new DatePickerDialog(con, R.style.DatePickerDialogStyle, mDateSetListener, cyear, cmonth, cday);

    }

    private DatePickerDialog.OnDateSetListener mDateSetListener = new DatePickerDialog.OnDateSetListener() {
        public void onDateSet(DatePicker view, int year, int monthOfYear,
                              int dayOfMonth) {
            mdate_selected = (monthOfYear + 1)
                    + "/"
                    + dayOfMonth
                    + "/" + year;
            mBtnDate.setText(mdate_selected);

            Calendar currentcal = Calendar.getInstance();
            Calendar cal = Calendar.getInstance();
            cal.set(year, monthOfYear, dayOfMonth);

            if (currentcal.after(cal)) {
                Toast.makeText(
                        con.getApplicationContext(),
                        con.getResources().getString(
                                R.string.Please_select_next_day),
                        Toast.LENGTH_SHORT).show();
                mBtnDate.setText(mnextDate);
            }
        }
    };

    @Override
    public void onClick(View v) {
        if (v == mBtnDate) {
            onCreateDialog().show();
        } else if (v == mBtnClose) {
            if (isFrmDelivery) {
                bmodel.getOrderHeaderBO().setRemark(mEdtRemark.getText().toString());
                bmodel.setOrderHeaderNote(mEdtRemark.getText().toString());
                mEdtRemark.setText("");

            } else {
                bmodel.getOrderHeaderBO().setPO(mEdtPO.getText().toString());
                bmodel.getOrderHeaderBO().setRemark(mEdtRemark.getText().toString());
                bmodel.getOrderHeaderBO().setDeliveryDate(Utils.formatDateAsUserRequired(
                        mBtnDate.getText().toString(), "MM/dd/yyyy",
                        "yyyy/MM/dd"));
                bmodel.setOrderHeaderNote(mEdtRemark.getText().toString());
                mEdtPO.setText("");
                mEdtRemark.setText("");
            }
            dismiss();
        }

    }

}
