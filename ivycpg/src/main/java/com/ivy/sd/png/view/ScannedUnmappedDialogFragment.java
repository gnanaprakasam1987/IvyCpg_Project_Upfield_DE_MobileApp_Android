package com.ivy.sd.png.view;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.ivy.lib.DialogFragment;
import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.bo.AssetTrackingBO;
import com.ivy.sd.png.bo.ReasonMaster;
import com.ivy.sd.png.commons.SDUtil;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.provider.ConfigurationMasterHelper;
import com.ivy.sd.png.util.Commons;
import com.ivy.sd.png.util.DateUtil;

import java.util.ArrayList;

/**
 * Created by anish.k on 9/22/2017.
 */

public class ScannedUnmappedDialogFragment extends DialogFragment implements View.OnClickListener {

    protected BusinessModel bmodel;
    protected EditText ETDesc;
    protected TextView TOutletCode, TEquiType, TSerialNo;
    protected Button BTCancel, BTSave;
    protected Spinner spinnerCustom;
    protected String serialNo, EquiType, reasonId, remarks, brand, retailerName;
    private final AssetTrackingBO assetBo = new AssetTrackingBO();
    protected Integer assetId = -1;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        setCancelable(false);
        View view = inflater.inflate(R.layout.dialog_asset_tracking, container);
        getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        final Context context = getActivity();
        bmodel = (BusinessModel) context.getApplicationContext();
        ETDesc = (EditText) view.findViewById(R.id.input_description);
        TEquiType = (TextView) view.findViewById(R.id.input_equipment_type);
        TOutletCode = (TextView) view.findViewById(R.id.input__outletcode);
        TSerialNo = (TextView) view.findViewById(R.id.input_serialNo);
        BTCancel = (Button) view.findViewById(R.id.btn_dialog_cancel);
        BTSave = (Button) view.findViewById(R.id.btn_dialog_save);
        BTCancel.setOnClickListener(this);
        BTSave.setOnClickListener(this);

        serialNo = getArguments().getString("serialNo");
        EquiType = getArguments().getString("assetName");
        brand = getArguments().getString("brand");
        retailerName = getArguments().getString("retailerName");
        assetId = getArguments().getInt("assetId");

        TEquiType.setTypeface(bmodel.configurationMasterHelper.getFontBaloobhai(ConfigurationMasterHelper.FontType.REGULAR));
        TSerialNo.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
        ETDesc.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.THIN));
        BTCancel.setTypeface(bmodel.configurationMasterHelper.getFontBaloobhai(ConfigurationMasterHelper.FontType.REGULAR));
        BTSave.setTypeface(bmodel.configurationMasterHelper.getFontBaloobhai(ConfigurationMasterHelper.FontType.REGULAR));

        TSerialNo.setText(getString(R.string.serial_no) + ": " + serialNo);
        TEquiType.setText(EquiType);
        TOutletCode.setText("Current Retailer: " + retailerName);

        initCustomSpinner(view);
        return view;
    }

    private void initCustomSpinner(View view) {

        spinnerCustom = (Spinner) view.findViewById(R.id.spinnerCustomDialog);
        bmodel.reasonHelper.loadAssetReasonsBasedOnType("ASSET_ADD");
        ArrayList<String> reasonList = new ArrayList<String>();
        reasonList.add("--Select Reason--");
        try {
            for (ReasonMaster temp : bmodel.reasonHelper
                    .getAssetReasonsBasedOnType()) {
                reasonList.add(temp.getReasonDesc());
            }
        } catch (NullPointerException e) {
            Commons.printException("" + e);
        }
        //  languages.add(bmodel.reasonHelper.getNonProductiveReasonMaster())
        // Creating adapter for spinner
        ArrayAdapter<String> dataAdapter;
        if (reasonList != null && reasonList.size() > 0)
            dataAdapter = new ArrayAdapter<String>(getActivity(), R.layout.spinner_bluetext_layout, reasonList);
        else
            dataAdapter = new ArrayAdapter<String>(getActivity(), R.layout.spinner_bluetext_layout);
        dataAdapter.setDropDownViewResource(R.layout.spinner_bluetext_list_item);
        spinnerCustom.setAdapter(dataAdapter);
        spinnerCustom.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                reasonId = getReasonId(parent.getItemAtPosition(position).toString());
                Toast.makeText(parent.getContext(), "Output..." + reasonId, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    private String getReasonId(String reasonDesc) {
        for (int i = 0; i < bmodel.reasonHelper
                .getAssetReasonsBasedOnType().size(); i++) {
            if (bmodel.reasonHelper
                    .getAssetReasonsBasedOnType().get(i).getReasonDesc().equals(reasonDesc)) {
                return bmodel.reasonHelper
                        .getAssetReasonsBasedOnType().get(i).getReasonID();
            }
        }
        return "0";
    }
    private void setAddAssetDetails() {

        String todayDate = DateUtil.convertFromServerDateToRequestedFormat(
                SDUtil.now(SDUtil.DATE_GLOBAL),
                ConfigurationMasterHelper.outDateFormat);
        remarks = ETDesc.getText().toString().trim();
        assetBo.setMposm(String.valueOf(assetId));
        assetBo.setMbrand(bmodel.assetTrackingHelper.getAssetBrandIds(brand));
        assetBo.setMnewinstaldate(todayDate);
        assetBo.setMsno(serialNo);
        assetBo.setMreasonId(reasonId);
        assetBo.setMremarks(remarks);
        bmodel.assetTrackingHelper.setAssetTrackingBO(assetBo);

    }

    @Override
    public void onStart() {
        super.onStart();
        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        Window window = getDialog().getWindow();
        lp.copyFrom(window.getAttributes());
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
        window.setAttributes(lp);
    }

    private boolean validateDesc() {
        if (ETDesc.getText().toString().trim().isEmpty()) {
//            TLDesc.setError("Invalid Entry");
            Toast.makeText(bmodel, "Add description", Toast.LENGTH_SHORT).show();
            requestFocus(ETDesc);
            return false;
        }
        return true;
    }

    private void requestFocus(View view) {
        if (view.requestFocus()) {
            getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        }
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.btn_dialog_cancel) {
            dismiss();
        } else if (v.getId() == R.id.btn_dialog_save) {
            if (validateDesc()) {
                if (spinnerCustom.getSelectedItemPosition() != 0) {
                    setAddAssetDetails();
                    bmodel.saveModuleCompletion(HomeScreenTwo.MENU_ASSET);
                    bmodel.assetTrackingHelper
                            .saveAssetAddAndDeleteDetails("MENU_ASSET");
                    Toast.makeText(getActivity(), getResources().getString(R.string.saved_successfully),
                            Toast.LENGTH_SHORT).show();
                    dismiss();
                } else {
                    Toast.makeText(bmodel, "Select Reason and Try again", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }
}
