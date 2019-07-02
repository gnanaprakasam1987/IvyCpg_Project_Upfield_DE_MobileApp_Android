package com.ivy.sd.png.view;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.text.InputFilter;
import android.text.InputType;
import android.text.Spanned;
import android.text.method.DigitsKeyListener;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;

import com.ivy.cpg.view.salesreturn.SalesReturnHelper;
import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.bo.ReasonMaster;
import com.ivy.sd.png.commons.SDUtil;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.util.Commons;
import com.ivy.utils.FontUtils;
import com.ivy.utils.StringUtils;

import java.util.StringTokenizer;

@SuppressLint("ValidFragment")
public class RemarksDialog extends DialogFragment implements OnClickListener {
    private EditText remarks;
    private EditText rField1;
    private EditText rField2;
    private Spinner spnRField1;
    private BusinessModel bmodel;
    @SuppressLint("ValidFragment")
    private final String mModuleName;

    ArrayAdapter<ReasonMaster> spinnerAdapter;
    private Spinner spinner_remark_type;
    boolean isSpinnerAvailable;
    private String selectedRemark;
    private RemarksListener mCallback;

    boolean isSalesRetRfAvailable;

    public interface RemarksListener {
        void updateRemarks(String remark);
    }

    public RemarksDialog(String moduleName) {
        super();

        this.mModuleName = moduleName;
    }

    public RemarksDialog(String remark, String mModuleName, RemarksListener listener) {
        super();
        this.selectedRemark = remark;
        this.mModuleName = mModuleName;
        this.mCallback = listener;
    }

    @Override
    public void onStart() {
        super.onStart();
        if (getDialog().getWindow() != null)
            getDialog().getWindow()
                    .setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        getDialog().getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        View view = inflater.inflate(R.layout.remarks_fragment_dialog, container, false);
        bmodel = (BusinessModel) getActivity().getApplicationContext();
        bmodel.setContext(getActivity());
        remarks = view.findViewById(R.id.remarks);
        rField1 = view.findViewById(R.id.rField1);
        rField2 = view.findViewById(R.id.rField2);
        spnRField1 = view.findViewById(R.id.spnrField1);
        remarks.setTypeface(FontUtils.getFontRoboto(getActivity(), FontUtils.FontType.LIGHT));
        rField1.setTypeface(FontUtils.getFontRoboto(getActivity(), FontUtils.FontType.LIGHT));
        rField2.setTypeface(FontUtils.getFontRoboto(getActivity(), FontUtils.FontType.LIGHT));
        LinearLayout textInputLayout2 = view.findViewById(R.id.editText_layout2);
        LinearLayout textInputLayout3 = view.findViewById(R.id.editText_layout3);
        LinearLayout lnrRField1 = view.findViewById(R.id.lnrRField1);

        LinearLayout layout_remark = view.findViewById(R.id.editText_layout1);
        LinearLayout layout_remark_type = view.findViewById(R.id.layout_remark_type);
        spinner_remark_type = view.findViewById(R.id.spinner_remark_type);

        DisplayMetrics outMetrics = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay()
                .getMetrics(outMetrics);
        remarks.setWidth(outMetrics.widthPixels);

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

        remarks.setFilters(new InputFilter[]{filter});

        try {
            if (bmodel.labelsMasterHelper.applyLabels(view.findViewById(
                    R.id.titleBar).getTag()) != null)
                ((TextView) view.findViewById(R.id.titleBar))
                        .setText(bmodel.labelsMasterHelper
                                .applyLabels(view.findViewById(
                                        R.id.titleBar)
                                        .getTag()));


            if (bmodel.labelsMasterHelper.applyLabels(view.findViewById(
                    R.id.remark_title).getTag()) != null)
                ((TextView) view.findViewById(R.id.remark_title))
                        .setText(bmodel.labelsMasterHelper
                                .applyLabels(view.findViewById(
                                        R.id.remark_title)
                                        .getTag()));

            if (bmodel.labelsMasterHelper.applyLabels(view.findViewById(
                    R.id.rfield1_title).getTag()) != null)
                ((TextView) view.findViewById(R.id.rfield1_title))
                        .setText(bmodel.labelsMasterHelper
                                .applyLabels(view.findViewById(
                                        R.id.rfield1_title)
                                        .getTag()));

            if (bmodel.labelsMasterHelper.applyLabels(view.findViewById(
                    R.id.rField2).getTag()) != null)
                ((EditText) view.findViewById(R.id.rField2))
                        .setHint(bmodel.labelsMasterHelper
                                .applyLabels(view.findViewById(
                                        R.id.rField2)
                                        .getTag()));

            if (bmodel.labelsMasterHelper.applyLabels(view.findViewById(
                    R.id.remark_type_label).getTag()) != null)
                ((TextView) view.findViewById(R.id.remark_type_label))
                        .setText(bmodel.labelsMasterHelper
                                .applyLabels(view.findViewById(
                                        R.id.remark_type_label)
                                        .getTag()));


        } catch (Exception e) {
            Commons.printException(e);
        }
        switch (mModuleName) {
            case "MENU_STK_ORD":
                textInputLayout2.setVisibility(View.GONE);
                textInputLayout3.setVisibility(View.GONE);
                lnrRField1.setVisibility(View.GONE);
                String rField = bmodel.configurationMasterHelper.LOAD_REMARKS_FIELD_STRING;
                StringTokenizer stringtokenizer = new StringTokenizer(rField, ",");
                while (stringtokenizer.hasMoreElements()) {
                    String token = stringtokenizer.nextToken();
                    if (token.contains("RF1")) {
                        isSpinnerAvailable = (token.substring(token.indexOf("1") + 1, token.length()).equals("D"));
                        lnrRField1.setVisibility(isSpinnerAvailable ? View.VISIBLE : View.GONE);
                        textInputLayout2.setVisibility(!isSpinnerAvailable ? View.VISIBLE : View.GONE);
                        if (isSpinnerAvailable) {
                            bmodel.reasonHelper.downloadRemarks();
                            spinnerAdapter = new ArrayAdapter<>(getActivity(),
                                    R.layout.spinner_bluetext_layout);
                            spinnerAdapter.add(new ReasonMaster(0 + "", getResources().getString(R.string.select_remarks)));
                            int count = 0, selectedPos = -1;
                            for (ReasonMaster temp : bmodel.reasonHelper
                                    .getRemarksReasonMaster()) {
                                if (temp.getReasonID().equals(bmodel.getRField1()))
                                    selectedPos = count + 1;
                                spinnerAdapter.add(temp);
                                count++;
                            }
                            spinnerAdapter
                                    .setDropDownViewResource(R.layout.spinner_bluetext_list_item);
                            spnRField1.setAdapter(spinnerAdapter);
                            spnRField1.setSelection(selectedPos);
                            spnRField1.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                                @Override
                                public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                                    // spnText = spinnerAdapter.getItem(i).getReasonDesc();
                                }

                                @Override
                                public void onNothingSelected(AdapterView<?> adapterView) {

                                }
                            });
                        }

                    } else if (token.contains("RF2")) {
                        textInputLayout3.setVisibility(View.VISIBLE);
                    } else if (token.contains("REM")) {
                        if (token.equalsIgnoreCase("REMD")) {
                            layout_remark.setVisibility(View.GONE);
                            layout_remark_type.setVisibility(View.VISIBLE);

                            bmodel.reasonHelper.downloadRemarksType();
                            spinnerAdapter = new ArrayAdapter<>(getActivity(),
                                    R.layout.spinner_bluetext_layout);
                            spinnerAdapter.add(new ReasonMaster(0 + "", getResources().getString(R.string.select_remarks_type)));
                            int count = 0, selectedPos = -1;
                            for (ReasonMaster temp : bmodel.reasonHelper
                                    .getRemarksType()) {
                                if (temp.getReasonID().equals(bmodel.getRemarkType()))
                                    selectedPos = count + 1;
                                spinnerAdapter.add(temp);
                                count++;
                            }
                            spinnerAdapter
                                    .setDropDownViewResource(R.layout.spinner_bluetext_list_item);
                            spinner_remark_type.setAdapter(spinnerAdapter);
                            spinner_remark_type.setSelection(selectedPos);
                            spinner_remark_type.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                                @Override
                                public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                                }

                                @Override
                                public void onNothingSelected(AdapterView<?> adapterView) {

                                }
                            });
                        } else {
                            layout_remark.setVisibility(View.VISIBLE);

                        }
                    }
                }
                if (bmodel.getOrderHeaderNote() != null) {
                    remarks.setText(bmodel.getOrderHeaderNote());
                } else {
                    remarks.setText("");
                }
                if (bmodel.getRField1() != null) {
                    rField1.setText(bmodel.getRField1());
                } else {
                    rField1.setText("");
                }
                if (bmodel.getRField2() != null) {
                    rField2.setText(bmodel.getRField2());
                } else {
                    rField2.setText("");
                }

                break;
            case "MENU_CLOSING":
                Commons.print("Remarks Dialog ," + " MENU CLOSING called");
                if (bmodel.getStockCheckRemark() != null) {
                    remarks.setText(bmodel.getStockCheckRemark());
                } else {
                    remarks.setText("");
                }
                break;
            case "MENU_SALES_RET":
                if (bmodel.getSaleReturnNote() != null) {
                    remarks.setText(bmodel.getSaleReturnNote());
                } else {
                    remarks.setText("");
                }

                textInputLayout2.setVisibility(View.GONE);
                SalesReturnHelper salesReturnHelper = SalesReturnHelper.getInstance(getActivity());
                if (salesReturnHelper.REMARKS_SAL_RET_FILEDS.length() > 0) {
                    if (salesReturnHelper.REMARKS_SAL_RET_FILEDS.equalsIgnoreCase("RF")) {
                        isSalesRetRfAvailable = true;
                        textInputLayout2.setVisibility(View.VISIBLE);
                        if (bmodel.getSaleReturnRfValue() != null) {
                            rField1.setText(bmodel.getSaleReturnRfValue());
                        } else {
                            rField1.setText("");
                        }
                    }

                }

                break;
            case "MENU_ASSET":
                if (bmodel.getAssetRemark() != null) {
                    remarks.setText(bmodel.getAssetRemark());
                } else {
                    remarks.setText("");
                }
                break;
            case "MENU_SERIALIZED_ASSET":
                if (bmodel.getAssetRemark() != null) {
                    remarks.setText(bmodel.getAssetRemark());
                } else {
                    remarks.setText("");
                }
                break;
            case HomeScreenTwo.MENU_PROMO:
                if (bmodel.getNote() != null) {
                    remarks.setText(bmodel.getNote());
                } else {
                    remarks.setText("");
                }
                break;
            case HomeScreenTwo.MENU_SOS:
                remarks.setText(bmodel.getNote());
                break;
            case HomeScreenTwo.MENU_SOD:
                remarks.setText(bmodel.getNote());
                break;
            case HomeScreenTwo.MENU_SOSKU:
                remarks.setText(bmodel.getNote());
                break;
            case HomeScreenTwo.MENU_COMPETITOR:
                remarks.setText(bmodel.getNote());
                break;
            case "MENU_CROWN":
                ((TextView) view.findViewById(R.id.titleBar))
                        .setText(getResources().getString(R.string.crown_count));
                TextView titleBar = view.findViewById(R.id.titleBar);
                titleBar.setTypeface(FontUtils.getFontBalooHai(getActivity(), FontUtils.FontType.REGULAR));
                remarks.setHint(getResources().getString(R.string.enter) + " " + getResources().getString(R.string.crown_count));
                remarks.setInputType(InputType.TYPE_CLASS_PHONE);
                remarks.setKeyListener(DigitsKeyListener.getInstance("0123456789"));
                if (bmodel.getOrderHeaderBO().getCrownCount() != 0) {
                    String strCrownCount = bmodel.getOrderHeaderBO().getCrownCount() + "";
                    remarks.setText(strCrownCount);
                } else
                    remarks.setText("");
                break;
            case "MENU_COUNTER":
                remarks.setText(bmodel.getNote());
                break;
            case "MENU_PROMO_REMARKS":
                textInputLayout2.setVisibility(View.GONE);
                textInputLayout3.setVisibility(View.GONE);
                lnrRField1.setVisibility(View.GONE);
                if (!StringUtils.isEmptyString(selectedRemark))
                    remarks.setText(selectedRemark);
                else
                    remarks.setText("");
                break;
            case "TASK":
                textInputLayout2.setVisibility(View.GONE);
                textInputLayout3.setVisibility(View.GONE);
                lnrRField1.setVisibility(View.GONE);
                if (!StringUtils.isEmptyString(selectedRemark))
                    remarks.setText(selectedRemark);
                else
                    remarks.setText("");
                break;
            default:
                break;
        }
        Button ok = view.findViewById(R.id.btn_ok);
        Button cancel = view.findViewById(R.id.btn_cancel);
        cancel.setTypeface(FontUtils.getFontBalooHai(getActivity(), FontUtils.FontType.REGULAR));
        ok.setTypeface(FontUtils.getFontBalooHai(getActivity(), FontUtils.FontType.REGULAR));
        ok.setOnClickListener(this);
        cancel.setOnClickListener(this);
        return view;
    }

    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.btn_ok) {
            switch (mModuleName) {
                case "MENU_STK_ORD":
                    bmodel.setOrderHeaderNote(remarks.getText().toString());
                    bmodel.setRField1((isSpinnerAvailable) ?
                            ((ReasonMaster) spnRField1.getSelectedItem()).getReasonDesc() : rField1.getText().toString());
                    bmodel.setRField2(rField2.getText().toString());
                    String remarkType = spinner_remark_type.getSelectedItem() != null ?
                            ((ReasonMaster) spinner_remark_type.getSelectedItem()).getReasonID() : "";
                    bmodel.setRemarkType(remarkType);
                    break;
                case "MENU_CLOSING":
                    bmodel.setStockCheckRemark(remarks.getText().toString());
                    break;
                case "MENU_SALES_RET":
                    bmodel.setSaleReturnNote(remarks.getText().toString());
                    bmodel.setSaleReturnRfValue((isSalesRetRfAvailable) ? rField1.getText().toString() : "");
                    break;
                case "MENU_ASSET":
                    bmodel.setAssetRemark(remarks.getText().toString());
                    break;
                case "MENU_SERIALIZED_ASSET":
                    bmodel.setAssetRemark(remarks.getText().toString());
                    break;
                case "MENU_PROMO":
                    bmodel.setNote(remarks.getText().toString());
                    break;
                case "MENU_AVAILABILITY":
                    bmodel.setNote(remarks.getText().toString());
                    break;
                case HomeScreenTwo.MENU_SOS:
                    bmodel.setNote(remarks.getText().toString());
                    break;
                case HomeScreenTwo.MENU_SOD:
                    bmodel.setNote(remarks.getText().toString());
                    break;
                case HomeScreenTwo.MENU_SOSKU:
                    bmodel.setNote(remarks.getText().toString());
                    break;
                case HomeScreenTwo.MENU_COMPETITOR:
                    bmodel.setNote(remarks.getText().toString());
                    break;
                case "MENU_CROWN":
                    bmodel.getOrderHeaderBO().setCrownCount(SDUtil.convertToInt(remarks.getText().toString()));
                    break;
                case "MENU_COUNTER":
                    bmodel.setNote(remarks.getText().toString());
                    break;
                case "MENU_PROMO_REMARKS":
                    mCallback.updateRemarks(remarks.getText().toString());
                case "TASK":
                    mCallback.updateRemarks(remarks.getText().toString());
                    break;
                default:
                    break;
            }
            remarks.setText("");
            dismiss();

        } else if (i == R.id.btn_cancel) {
            dismiss();
        }
    }
}
