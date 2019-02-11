package com.ivy.ui.attendance.inout.view;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.support.design.widget.TextInputLayout;
import android.view.View;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.bo.ReasonMaster;
import com.ivy.sd.png.util.Commons;
import com.ivy.utils.FontUtils;

import java.util.ArrayList;

/**
 * Created by nivetha.s on 06-04-2016.
 */
public class InOutReasonDialog extends Dialog {

    private Spinner reason_spnr;
    private ArrayAdapter<ReasonMaster> dataAdapter;
    private OnMyDialogResult Result;
    private TextInputLayout remarksInputLayout;

    InOutReasonDialog(final Context context, OnMyDialogResult mDialogResult, boolean isRemarks, ArrayList<ReasonMaster> reasonList) {
        super(context);
        this.Result = mDialogResult;
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        setContentView(R.layout.in_out_reason_dialog);

        reason_spnr = findViewById(R.id.reason_spnr);
        reason_spnr.setVisibility(View.VISIBLE);

        remarksInputLayout = findViewById(R.id.remarkWrapper);
        remarksInputLayout.setHint(context.getResources().getString(R.string.remark_hint));
        remarksInputLayout.setTypeface(FontUtils.getFontRoboto(context, FontUtils.FontType.MEDIUM));
        remarksInputLayout.getEditText().setTypeface(FontUtils.getFontRoboto(context, FontUtils.FontType.MEDIUM));
        remarksInputLayout.getEditText().setText("");

        if (!isRemarks)
            remarksInputLayout.setVisibility(View.GONE);

        dataAdapter = new ArrayAdapter<>(context,
                R.layout.spinner_bluetext_layout);
        dataAdapter.add(new ReasonMaster(0 + "", context.getResources().getString(R.string.select_reason)));
        if (reasonList.size() > 0)
            dataAdapter.addAll(reasonList);
        dataAdapter
                .setDropDownViewResource(R.layout.spinner_bluetext_list_item);

        reason_spnr.setAdapter(dataAdapter);
        Button btn_ok = findViewById(R.id.btn_ok);
        TextView titleBar = findViewById(R.id.titleBar);
        TextView must_sell_message_tv = findViewById(R.id.must_sell_message_tv);

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

        btn_ok.setTypeface(FontUtils.getFontBalooHai(context, FontUtils.FontType.REGULAR));
        titleBar.setTypeface(FontUtils.getFontBalooHai(context, FontUtils.FontType.REGULAR));
        must_sell_message_tv.setTypeface(FontUtils.getFontRoboto(context, FontUtils.FontType.LIGHT));
    }

    public interface OnMyDialogResult {


        void cancel(String reasonid, String remarks);
    }

    void setDialogResult(OnMyDialogResult onMyDialogResult) {
        Result = onMyDialogResult;

    }
}
