package com.ivy.ui.attendance.inout.view;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;

import com.google.android.material.textfield.TextInputLayout;

import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.Toast;

import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.bo.ReasonMaster;
import com.ivy.sd.png.util.Commons;

import java.util.ArrayList;
import java.util.Objects;

/**
 * Created by nivetha.s on 06-04-2016.
 */
class InOutReasonDialog extends Dialog {

    private OnMyDialogResult Result;
    private TextInputLayout remarksInputLayout;
    private ReasonMaster selectedReasonMaster;


    InOutReasonDialog(final Context context, boolean isRemarks, ArrayList<ReasonMaster> reasonList) {
        super(context);
        //this.Result = mDialogResult;
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        this.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        Objects.requireNonNull(getWindow()).setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        setContentView(R.layout.in_out_reason_dialog);

        ListView listView = findViewById(R.id.dlg_priority_lvw);

        remarksInputLayout = findViewById(R.id.remarkWrapper);
        remarksInputLayout.setHint(context.getResources().getString(R.string.remark_hint));
        Objects.requireNonNull(remarksInputLayout.getEditText()).setText("");

        if (!isRemarks)
            remarksInputLayout.setVisibility(View.GONE);

        for (ReasonMaster rm : reasonList) {
            String str = rm.toString();
        }


        listView.setAdapter(new ArrayAdapter<ReasonMaster>(context, android.R.layout.simple_list_item_single_choice, reasonList));

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                selectedReasonMaster = (ReasonMaster) parent.getItemAtPosition(position);

            }
        });


        Button btn_ok = findViewById(R.id.btn_ok);
        btn_ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (selectedReasonMaster == null) {
                    Toast.makeText(context, context.getResources().getString(R.string.please_select_item), Toast.LENGTH_SHORT).show();
                    return;
                }

                if (Result != null)
                    Result.cancel(selectedReasonMaster.getReasonID(), remarksInputLayout.getEditText().getText().toString());

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
