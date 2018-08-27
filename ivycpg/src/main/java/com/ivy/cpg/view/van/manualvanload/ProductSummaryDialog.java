package com.ivy.cpg.view.van.manualvanload;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.commons.SDUtil;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.utils.FontUtils;

import jxl.format.Font;

/**
 * Created by Hanifa on 21/8/18.
 */

public class ProductSummaryDialog {


    public ProductSummaryDialog(final Context context, final int selectedSubDepotId) {

        final BusinessModel bModel = (BusinessModel) context.getApplicationContext();

        LayoutInflater layoutInflater = LayoutInflater
                .from(context);
        final ViewGroup nullParent = null;

        View promptView = layoutInflater.inflate(
                R.layout.dialog_vanload_summary, nullParent, false);
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                context);
        alertDialogBuilder.setView(promptView);

        ((TextView) promptView.findViewById(R.id.title_tv)).setTypeface(FontUtils.getFontBalooHai(context, FontUtils.FontType.REGULAR));

        final TextView tvProductPrice = (TextView) promptView
                .findViewById(R.id.tv_product_price);
        ((TextView)promptView.findViewById(R.id.load_price_label_tv)).setTypeface(FontUtils.getFontRoboto(FontUtils.FontType.REGULAR,context));
        tvProductPrice.setTypeface(FontUtils.getFontRoboto(FontUtils.FontType.THIN,context));

        ((TextView)promptView.findViewById(R.id.unload_price_label_tv)).setTypeface(FontUtils.getFontRoboto(FontUtils.FontType.REGULAR,context));
        final TextView tvReturnProductPrice = (TextView) promptView
                .findViewById(R.id.tv_returnprd_price);
        tvReturnProductPrice.setTypeface(FontUtils.getFontRoboto(FontUtils.FontType.THIN,context));

        ((TextView)promptView.findViewById(R.id.total_amt_label_tv)).setTypeface(FontUtils.getFontRoboto(FontUtils.FontType.REGULAR,context));
        final TextView tvTotalPrice = (TextView) promptView
                .findViewById(R.id.tv_total_price);
        tvTotalPrice.setTypeface(FontUtils.getFontRoboto(FontUtils.FontType.THIN,context));

        ((TextView)promptView.findViewById(R.id.entered_amt_label_tv)).setTypeface(FontUtils.getFontRoboto(FontUtils.FontType.REGULAR,context));
        final EditText edtPrice = (EditText) promptView
                .findViewById(R.id.edt_price);
        edtPrice.setTypeface(FontUtils.getFontRoboto(FontUtils.FontType.MEDIUM,context));

        android.view.inputmethod.InputMethodManager imm = (android.view.inputmethod.InputMethodManager) context
                .getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(edtPrice.getWindowToken(), 0);
        edtPrice.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence s, int start, int before,
                                      int count) {

            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count,
                                          int after) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                String qty = s.toString();
                if (!"".equals(qty)) {
                    ManualVanLoadHelper.getInstance(context.getApplicationContext()).setmVanLoadAmount(SDUtil
                            .convertToFloat(qty));

                }

            }
        });

        ManualVanLoadHelper manualVanLoadHelper = ManualVanLoadHelper.getInstance(context.getApplicationContext());
        String tv = SDUtil.roundIt(
                manualVanLoadHelper.calculateVanLoadProductPrice(), 2)
                + "";

        tvProductPrice.setText(tv);
        tv = bModel.getOrderHeaderBO()
                .getRemainigValue() + "";
        tvReturnProductPrice.setText(tv);

        tv = SDUtil.roundIt(
                manualVanLoadHelper.calculateVanLoadProductPrice()
                        + bModel.getOrderHeaderBO().getRemainigValue(), 2)
                + "";
        tvTotalPrice.setText(tv);

        // setup a dialog window
        alertDialogBuilder
                .setCancelable(false)
                .setPositiveButton(R.string.ok,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                float totalPrice = SDUtil
                                        .convertToFloat(tvTotalPrice.getText()
                                                .toString());
                                if (ManualVanLoadHelper.getInstance(context.getApplicationContext()).getmVanLoadAmount() == totalPrice) {
                                    new SaveVanLoadAsyncTask(context, selectedSubDepotId).execute();

                                } else {
                                    Toast.makeText(bModel,
                                            "Amount Mis match",
                                            Toast.LENGTH_SHORT).show();
                                }
                            }
                        })
                .setNegativeButton(R.string.cancel,
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                dialog.cancel();
                            }
                        });

        // create an alert dialog
        bModel.applyAlertDialogTheme(alertDialogBuilder);

    }
}
