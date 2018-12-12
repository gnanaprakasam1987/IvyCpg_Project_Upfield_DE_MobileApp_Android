package com.ivy.cpg.view.serializedAsset;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.database.Cursor;
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

import com.ivy.cpg.view.asset.AssetTrackingHelper;
import com.ivy.lib.DialogFragment;
import com.ivy.lib.existing.DBUtil;
import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.bo.ReasonMaster;
import com.ivy.sd.png.bo.RetailerMasterBO;
import com.ivy.sd.png.bo.asset.AssetTrackingBO;
import com.ivy.sd.png.commons.SDUtil;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.provider.ConfigurationMasterHelper;
import com.ivy.sd.png.util.Commons;
import com.ivy.sd.png.util.DataMembers;
import com.ivy.sd.png.util.DateUtil;
import com.ivy.sd.png.view.HomeScreenTwo;
import com.ivy.sd.png.view.MyDialogCloseListener;
import com.ivy.utils.AppUtils;

import java.util.ArrayList;

/**
 * Created by anish.k on 9/25/2017.
 * This screen is used to manage asset movement from one retailer to another retailer
 */

public class SerializedAssetMovementDialog extends DialogFragment {

    protected BusinessModel mBModel;
    protected TextView TVOutletName, TVSerialNo, TVAssetName;
    protected Spinner SpToOutletName, SpReason;
    protected EditText ETDesc;
    protected Button BTCancel, BTSave;
    protected ArrayList<ReasonMaster> mAssetReasonList;
    protected ArrayList<RetailerMasterBO> retailerMasterBOs;
    protected String serialNo,reasonId,toRetailerId="0",assetName,/*brand,*/retailerName;
    protected Integer retailerSelected=-1,assetId,referenceId;
    private final SerializedAssetBO assetBo = new SerializedAssetBO();
    SerializedAssetHelper assetTrackingHelper;
    private String movementType="RTR_WH";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if(getDialog().getWindow()!=null) {
            getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        }
        setCancelable(false);
        View view = inflater.inflate(R.layout.move_serialized_asset_dialog, container);
        getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        Context context = getActivity();
        mBModel = (BusinessModel) context.getApplicationContext();

        assetTrackingHelper = SerializedAssetHelper.getInstance(context);

        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        TVOutletName = (TextView) view.findViewById(R.id.input_current_outletcode);
        TVSerialNo = (TextView) view.findViewById(R.id.input_movement_serialNo);
        TVAssetName = (TextView) view.findViewById(R.id.input_movement_assetName);
        SpToOutletName = (Spinner) view.findViewById(R.id.spinnerMovementOutletName);
        SpReason = (Spinner) view.findViewById(R.id.spinnerMovementReason);
        ETDesc = (EditText) view.findViewById(R.id.input_move_description);
        BTCancel = (Button) view.findViewById(R.id.btn_dialog_move_cancel);
        BTSave = (Button) view.findViewById(R.id.btn_dialog_move);
        BTCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
        BTSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveFunction();
            }
        });

        TVAssetName.setTypeface(mBModel.configurationMasterHelper.getFontBaloobhai(ConfigurationMasterHelper.FontType.LIGHT));
        TVSerialNo.setTypeface(mBModel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
        ((TextView) view.findViewById(R.id.input_current_outletcode_title)).setTypeface(mBModel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
        ((TextView) view.findViewById(R.id.to_outletcode_title)).setTypeface(mBModel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
        TVOutletName.setTypeface(mBModel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
        BTCancel.setTypeface(mBModel.configurationMasterHelper.getFontBaloobhai(ConfigurationMasterHelper.FontType.REGULAR));
        BTSave.setTypeface(mBModel.configurationMasterHelper.getFontBaloobhai(ConfigurationMasterHelper.FontType.REGULAR));


        serialNo = getArguments().getString("serialNo");
        retailerName = getArguments().getString("retailerName");
        assetName = getArguments().getString("assetName");
        //brand=getArguments().getString("brand");
        assetId=getArguments().getInt("assetId");
        referenceId=getArguments().getInt("referenceId");
        String mSerialNumber=getString(R.string.serial_no) + ": " + serialNo;
        TVSerialNo.setText(mSerialNumber);
        TVOutletName.setText(retailerName);
        TVAssetName.setText(assetName);
        initSpinner();

        super.onViewCreated(view, savedInstanceState);
    }

    private void initSpinner() {
        //Generating Reason List
        mAssetReasonList=new ArrayList<>();
        mBModel.reasonHelper.loadAssetReasonsBasedOnType("ASSET_MOVE");
        mAssetReasonList.add(new ReasonMaster("0","--Select Reason--"));
        mAssetReasonList.addAll(mBModel.reasonHelper.getAssetReasonsBasedOnType());

        ArrayAdapter<ReasonMaster> mAssetReasonSpinAdapter = new ArrayAdapter<>(getActivity(), R.layout.spinner_bluetext_layout, mAssetReasonList);
        mAssetReasonSpinAdapter.setDropDownViewResource(R.layout.spinner_bluetext_list_item);
        SpReason.setAdapter(mAssetReasonSpinAdapter);
        SpReason.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                reasonId =mAssetReasonList.get(position).getReasonID();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
        //Generating Retailer List
        RetailerMasterBO retailer = new RetailerMasterBO();
        // retailer.setMovRetailerCode(Integer.toString(0));
        retailer.setMovRetailerName("Warehouse");
        retailer.setMovRetailerId(Integer.toString(0));

        retailerMasterBOs = mBModel.downloadRetailerMasterData();
        retailerMasterBOs.add(0, retailer);

        ArrayList<String> mRetailerNameList = new ArrayList<>();
        ArrayList<String> mRetailerIdList = new ArrayList<>();
        for (int i = 0; i < retailerMasterBOs.size(); i++) {
            if(!retailerMasterBOs.get(i).getMovRetailerId().equals(mBModel.getRetailerMasterBO().getRetailerID())) {
                mRetailerNameList.add(retailerMasterBOs.get(i).getMovRetailerName());
                mRetailerIdList.add(retailerMasterBOs.get(i).getMovRetailerId());
            }
        }


        ArrayAdapter<String> mRetailerSpinAdapter = new ArrayAdapter<>(getActivity(), R.layout.spinner_bluetext_layout, mRetailerNameList);
        mRetailerSpinAdapter.setDropDownViewResource(R.layout.spinner_bluetext_list_item);
        SpToOutletName.setAdapter(mRetailerSpinAdapter);

        SpToOutletName.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                retailerSelected=position;
                toRetailerId=mRetailerIdList.get(position);

                if(position>0)
                    movementType="RTR_RTR";
                else movementType="RTR_WH";
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    private void saveFunction() {
        if (validateDesc())
            //if (SpToOutletName.getSelectedItemPosition() != 0 && retailerSelected>0) {
                if (SpReason.getSelectedItemPosition() != 0) {

                    setAddAssetDetails();
                    mBModel.saveModuleCompletion(HomeScreenTwo.MENU_ASSET);
                    assetTrackingHelper
                            .saveAssetMovementDetails(getContext().getApplicationContext(), movementType,referenceId);
                    Toast.makeText(getActivity(), getResources().getString(R.string.saved_successfully),
                            Toast.LENGTH_SHORT).show();
                    dismiss();
                } else {
                    Toast.makeText(mBModel, "Select Reason and Try again", Toast.LENGTH_SHORT).show();
                }
           /* } else {
                Toast.makeText(mBModel, "Select OutletName and Try again", Toast.LENGTH_SHORT).show();
            }*/
    }
    private void setAddAssetDetails() {

        String todayDate = DateUtil.convertFromServerDateToRequestedFormat(
                SDUtil.now(SDUtil.DATE_GLOBAL),
                ConfigurationMasterHelper.outDateFormat);
        String remarks=ETDesc.getText().toString().trim();

        assetBo.setPOSM(String.valueOf(assetId));
        assetBo.setBrand("");//assetTrackingHelper.getAssetBrandIds(brand));
        assetBo.setNewInstallDate(todayDate);
        assetBo.setSNO(serialNo);
        assetBo.setReasonId(reasonId);
        assetBo.setRemarks(remarks);
        assetBo.setToRetailerId(toRetailerId);
        assetTrackingHelper.setAssetTrackingBO(assetBo);

    }
    private boolean validateDesc() {
        if (ETDesc.getText().toString().trim().isEmpty()) {
            Toast.makeText(mBModel, "Add description", Toast.LENGTH_SHORT).show();
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
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
        Activity activity = getActivity();
        if(activity instanceof MyDialogCloseListener)
            ((MyDialogCloseListener)activity).handleDialogClose(dialog);
    }

    @Override
    public void onStart() {
        super.onStart();
        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;

        Window window = getDialog().getWindow();
        if(window!=null) {
            lp.copyFrom(window.getAttributes());
            window.setAttributes(lp);
        }
    }



}
