package com.ivy.cpg.view.van.manualvanload;

import android.app.Dialog;
import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.commons.SDUtil;
import com.ivy.sd.png.model.BusinessModel;

/**
 * Created by Hanifa on 21/8/18.
 */

public class ProductSummaryDialog extends Dialog {


    public ProductSummaryDialog(final Context context, final int selectedSubDepotId) {
        super(context);

        requestWindowFeature(Window.FEATURE_NO_TITLE);

        if (getWindow() != null) {
            getWindow().setSoftInputMode(
                    WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        }

        final BusinessModel bModel = (BusinessModel) context.getApplicationContext();

        setContentView(R.layout.dialog_vanload_summary);
        setCancelable(false);


        TextView tvProductPrice = findViewById(R.id.tv_product_price);

        TextView tvReturnProductPrice = findViewById(R.id.tv_returnprd_price);

        TextView tvTotalPrice = findViewById(R.id.tv_total_price);

        EditText edtPrice = findViewById(R.id.edt_price);

        Button saveBtn = findViewById(R.id.add_btn);
        Button closeBtn = findViewById(R.id.close_btn);

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

        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                float totalPrice = SDUtil
                        .convertToFloat(tvTotalPrice.getText()
                                .toString());

                if (edtPrice.getText().toString().isEmpty()) {
                    Toast.makeText(bModel,
                            context.getString(R.string.enter_amount),
                            Toast.LENGTH_SHORT).show();
                    return;
                }

                if (ManualVanLoadHelper.getInstance(context.getApplicationContext()).getmVanLoadAmount() == totalPrice) {
                    new SaveVanLoadAsyncTask(context, selectedSubDepotId).execute();
                    dismiss();
                } else {
                    Toast.makeText(bModel,
                            context.getString(R.string.amount_mismatch),
                            Toast.LENGTH_SHORT).show();
                }
            }
        });

        closeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

    }
}
