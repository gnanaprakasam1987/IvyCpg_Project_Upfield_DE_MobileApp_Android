package com.ivy.ui.attendance.inout.view;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import com.google.android.material.textfield.TextInputLayout;
import android.view.View;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.bo.ReasonMaster;
import com.ivy.sd.png.util.Commons;

import java.util.ArrayList;
import java.util.Objects;

/**
 * Created by nivetha.s on 06-04-2016.
 */
class InOutReasonDialog extends Dialog {

    private Spinner reason_spnr;
    private OnMyDialogResult Result;
    private TextInputLayout remarksInputLayout;

    InOutReasonDialog(final Context context, OnMyDialogResult mDialogResult, boolean isRemarks, ArrayList<ReasonMaster> reasonList) {
        super(context);
        this.Result = mDialogResult;
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        Objects.requireNonNull(getWindow()).setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        setContentView(R.layout.in_out_reason_dialog);

        reason_spnr = findViewById(R.id.reason_spnr);
        reason_spnr.setVisibility(View.VISIBLE);

        remarksInputLayout = findViewById(R.id.remarkWrapper);
        remarksInputLayout.setHint(context.getResources().getString(R.string.remark_hint));
        Objects.requireNonNull(remarksInputLayout.getEditText()).setText("");

        if (!isRemarks)
            remarksInputLayout.setVisibility(View.GONE);

        ArrayAdapter<ReasonMaster> dataAdapter = new ArrayAdapter<>(context,
                R.layout.spinner_bluetext_layout);
        dataAdapter.add(new ReasonMaster(0 + "", context.getResources().getString(R.string.select_reason)));
        if (reasonList.size() > 0)
            dataAdapter.addAll(reasonList);
        dataAdapter
                .setDropDownViewResource(R.layout.spinner_bluetext_list_item);

        reason_spnr.setAdapter(dataAdapter);
        Button btn_ok = findViewById(R.id.btn_ok);

        btn_ok.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Commons.print(reason_spnr
                        .getSelectedItem()
                        + " "
                        + reason_spnr.getSelectedItemPosition());
                if (!((ReasonMaster) reason_spnr.getSelectedItem()).getReasonID()
                        .equals("0")) {

                    Result.cancel(((ReasonMaster) reason_spnr
                            .getSelectedItem()).getReasonID(), remarksInputLayout.getEditText().getText().toString());
                }

            }
        });

    }

    public interface OnMyDialogResult {


        void cancel(String reasonid, String remarks);
    }

    void setDialogResult(OnMyDialogResult onMyDialogResult) {
        Result = onMyDialogResult;

    }
}
