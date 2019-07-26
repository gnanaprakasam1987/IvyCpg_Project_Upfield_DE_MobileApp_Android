package com.ivy.ui.reports.dynamicreport.view;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.ivy.sd.png.asean.view.R;
import com.ivy.ui.reports.dynamicreport.model.DynamicReportBO;
import com.ivy.utils.StringUtils;

import java.util.ArrayList;

import androidx.appcompat.widget.AppCompatSpinner;

public class ColumnSearchDialog extends Dialog {

    private ArrayList<DynamicReportBO> mColumnList;
    private Context mcontext;
    private DynamicReportBO dynamicReportBO;
    private DynamicReportBO prevReportBO;
    private EditText et_search;
    private DynamicReportFragmentNew.DialogListener dialogListener;
    private MenuIconListener menuIconListener;

    public interface MenuIconListener {
        void changeMenuTint(boolean isClear);
    }

    public ColumnSearchDialog(Context context, ArrayList<DynamicReportBO> columnList, DynamicReportFragmentNew.DialogListener mCallBack,
                              DynamicReportBO prevReportBO, MenuIconListener callBackListener) {
        super(context);
        this.mcontext = context;
        DynamicReportBO tempBO = new DynamicReportBO();
        tempBO.setDisplayName(mcontext.getResources().getString(R.string.select_column));
        columnList.add(0, tempBO);
        this.mColumnList = columnList;
        this.dialogListener = mCallBack;
        this.prevReportBO = prevReportBO;
        this.menuIconListener = callBackListener;
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setCancelable(false);
        setContentView(R.layout.dynamic_report_columns_search);

        AppCompatSpinner columnSpinner = findViewById(R.id.spinner_columns);
        et_search = findViewById(R.id.et_searchText);
        ArrayAdapter<DynamicReportBO> columnAdapter = new ArrayAdapter<>(mcontext, R.layout.spinner_blacktext_layout, mColumnList);
        columnAdapter
                .setDropDownViewResource(R.layout.spinner_blacktext_list_item);
        columnSpinner.setAdapter(columnAdapter);
        columnSpinner
                .setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    public void onItemSelected(AdapterView<?> parent,
                                               View view, int position, long id) {

                        if (position > 0) {
                            dynamicReportBO = (DynamicReportBO) columnSpinner
                                    .getSelectedItem();
                            dynamicReportBO.setSearched(true);
                            if (prevReportBO != null && !dynamicReportBO.getFieldName().equals(prevReportBO.getFieldName())) {
                                prevReportBO.setSearched(false);
                                prevReportBO.setSearchText("");
                            }
                        } else {
                            dynamicReportBO = null;
                        }

                    }

                    public void onNothingSelected(AdapterView<?> parent) {
                    }
                });
        if (prevReportBO != null) {
            et_search.setText(prevReportBO.getSearchText());
            columnSpinner.setSelection(columnAdapter.getPosition(prevReportBO));
        }
        Button mOkBtn = findViewById(R.id.btn_ok);
        Button mDismisBtn = findViewById(R.id.btn_dismiss);

        mOkBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String search_text = et_search.getText().toString();
                if (dynamicReportBO != null) {
                    if (!StringUtils.isNullOrEmpty(search_text)) {
                        dynamicReportBO.setSearchText(search_text);
                        dialogListener.onColumnSearch(false);
                        menuIconListener.changeMenuTint(false);
                        dismiss();
                    } else {
                        et_search.setError(mcontext.getResources().getString(R.string.please_enter));
                    }
                } else {
                    Toast.makeText(mcontext, "Please select column", Toast.LENGTH_LONG).show();
                }
            }
        });

        mDismisBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (prevReportBO != null) {
                    prevReportBO.setSearchText("");
                    prevReportBO.setSearched(false);
                    prevReportBO = null;
                    dialogListener.onColumnSearch(true);
                    menuIconListener.changeMenuTint(true);
                }
                dismiss();
            }
        });

    }

}
