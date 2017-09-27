package com.ivy.sd.png.view;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputLayout;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.SpinnerAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.ivy.lib.DialogFragment;
import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.bo.AssetTrackingBO;
import com.ivy.sd.png.bo.ReasonMaster;
import com.ivy.sd.png.commons.SDUtil;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.provider.ConfigurationMasterHelper;
import com.ivy.sd.png.util.DateUtil;

import java.util.ArrayList;

/**
 * Created by anish.k on 9/22/2017.
 */

public class ScannedUnmappedDialogFragment extends DialogFragment implements View.OnClickListener{

    protected BusinessModel bmodel;
    protected TextInputLayout TLDesc;
    protected EditText ETDesc;
    protected TextView TOutletCode,TEquiType,TSerialNo;
    protected Button BTCancel,BTSave;
    protected Spinner spinnerCustom;
    protected String serialNo, EquiType,reasonId,remarks,brand,retailerName;
    private final AssetTrackingBO assetBo = new AssetTrackingBO();
    protected Integer assetId=-1;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        setCancelable(false);
        View view = inflater.inflate(R.layout.dialog_asset_tracking, container);
        final Context context = getActivity();
        bmodel = (BusinessModel) context.getApplicationContext();
        TLDesc=(TextInputLayout)view.findViewById(R.id.input_layout_dialog_description);
        ETDesc=(EditText)view.findViewById(R.id.input_description);
        TEquiType=(TextView)view.findViewById(R.id.input_equipment_type);
        TOutletCode=(TextView)view.findViewById(R.id.input__outletcode);
        TSerialNo=(TextView)view.findViewById(R.id.input_serialNo);
        BTCancel=(Button)view.findViewById(R.id.btn_dialog_cancel);
        BTSave=(Button)view.findViewById(R.id.btn_dialog_save);
        BTCancel.setOnClickListener(this);
        BTSave.setOnClickListener(this);

        serialNo=getArguments().getString("serialNo");
        EquiType=getArguments().getString("assetName");
        brand=getArguments().getString("brand");
        retailerName=getArguments().getString("retailerName");
        assetId=getArguments().getInt("assetId");

        TSerialNo.setText(getString(R.string.serial_no)+": "+serialNo);
        TEquiType.setText(EquiType);
        TOutletCode.setText(retailerName);

        initCustomSpinner(view);

        return view;
    }

    private void initCustomSpinner(View view) {

        spinnerCustom= (Spinner) view.findViewById(R.id.spinnerCustomDialog);
        ArrayList<String> reasonList = new ArrayList<String>();
        reasonList.add("--Select Reason--");
        try {
            for (ReasonMaster temp : bmodel.reasonHelper
                    .getNonProductiveReasonMaster()) {
                reasonList.add(temp.getReasonDesc());
            }
        }
        catch (NullPointerException e)
        {
            Log.e("Null","NullPointer Throwed");
            e.printStackTrace();
        }
      //  languages.add(bmodel.reasonHelper.getNonProductiveReasonMaster())
        // Creating adapter for spinner
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item, reasonList);
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerCustom.setAdapter(dataAdapter);
        spinnerCustom.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                reasonId = parent.getItemAtPosition(position).toString();
                Toast.makeText(parent.getContext(), "Output..." + reasonId, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }
    private void setAddAssetDetails() {

        String todayDate = DateUtil.convertFromServerDateToRequestedFormat(
                SDUtil.now(SDUtil.DATE_GLOBAL),
                ConfigurationMasterHelper.outDateFormat);
        remarks=ETDesc.getText().toString().trim();
        assetBo.setMposm(String.valueOf(assetId));
        assetBo.setMbrand(bmodel.assetTrackingHelper.getassetbrandids(brand));
        assetBo.setMnewinstaldate(todayDate);
        assetBo.setMsno(serialNo);
        assetBo.setMreasonId(reasonId);
        assetBo.setMremarks(remarks);
        bmodel.assetTrackingHelper.setMassetTrackingBO(assetBo);

    }

    @Override
    public void onStart() {
        super.onStart();

        //
        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        Window window = getDialog().getWindow();
        lp.copyFrom(window.getAttributes());
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
        window.setAttributes(lp);
    }
    private boolean validateDesc()
    {
        if (ETDesc.getText().toString().trim().isEmpty()) {
            TLDesc.setError("Invalid Entry");
            requestFocus(ETDesc);
            return false;
        } else {
            TLDesc.setErrorEnabled(false);
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
        if(v.getId()==R.id.btn_dialog_cancel)
        {
         dismiss();
        }
        else if(v.getId()==R.id.btn_dialog_save)
        {
            if(validateDesc())
            {
                if(spinnerCustom.getSelectedItemPosition()!=0)
                {
                        setAddAssetDetails();
                        bmodel.saveModuleCompletion(HomeScreenTwo.MENU_ASSET);
                        bmodel.assetTrackingHelper
                                .saveAssetAddandDeletedetails("MENU_ASSET");
                        Toast.makeText(getActivity(), getResources().getString(R.string.saved_successfully),
                                Toast.LENGTH_SHORT).show();
                        dismiss();
                }
                else
                {
                    Toast.makeText(bmodel, "Select Reason and Try again", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }
}
