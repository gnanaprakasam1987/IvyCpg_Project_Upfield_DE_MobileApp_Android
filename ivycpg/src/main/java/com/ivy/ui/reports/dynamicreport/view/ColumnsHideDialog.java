package com.ivy.ui.reports.dynamicreport.view;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ListView;

import com.ivy.sd.png.asean.view.R;
import com.ivy.ui.reports.dynamicreport.adapter.ColumnsHideAdapter;
import com.ivy.ui.reports.dynamicreport.model.DynamicReportBO;

import java.util.ArrayList;

public class ColumnsHideDialog extends Dialog {

    private ArrayList<DynamicReportBO> mreportList;
    private Context mcontext;
    private DynamicReportFragmentNew.DialogListener dialogListener;


    public ColumnsHideDialog(Context context, ArrayList<DynamicReportBO> reportList, DynamicReportFragmentNew.DialogListener mCallBack) {
        super(context);
        this.mcontext = context;
        this.mreportList = reportList;
        this.dialogListener = mCallBack;
        requestWindowFeature(Window.FEATURE_NO_TITLE);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setCancelable(false);
        setContentView(R.layout.dialog_columns_hide);

        Button mOkBtn = findViewById(R.id.btn_ok);
        Button mDismisBtn = findViewById(R.id.btn_dismiss);
        ListView columnsLV = findViewById(R.id.lv_columns);

        ColumnsHideAdapter columnsHideAdapter = new ColumnsHideAdapter(mcontext, mreportList);
        columnsLV.setAdapter(columnsHideAdapter);

        mOkBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialogListener.onColumnHide();
                dismiss();
            }
        });

        mDismisBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

    }

}
