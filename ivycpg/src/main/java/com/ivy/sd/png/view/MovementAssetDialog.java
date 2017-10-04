package com.ivy.sd.png.view;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputLayout;
import android.util.Log;
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
import com.ivy.sd.png.bo.RetailerMasterBO;
import com.ivy.sd.png.commons.SDUtil;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.provider.ConfigurationMasterHelper;
import com.ivy.sd.png.util.Commons;
import com.ivy.sd.png.util.DateUtil;

import java.util.ArrayList;
import java.util.Vector;

/**
 * Created by anish.k on 9/25/2017.
 */

public class MovementAssetDialog extends DialogFragment {

    protected BusinessModel bmodel;
    protected TextView TVOutletName, TVSerialNo, TVAssetName;
    protected Spinner SpToOutletName, SpReason;
    protected EditText ETDesc;
    protected TextInputLayout TLDesc;
    private static final String SELECT = "-Select-";
    protected Button BTCancel, BTSave;
    protected ArrayList<ReasonMaster> mAssetReasonList;
    protected ArrayList<RetailerMasterBO> retailerMasterBOs;
    private ArrayList<RetailerMasterBO> retailer = new ArrayList<>();
    protected String serialNo,reasonId,retailerId,assetName,brand,retailerName;
    protected Integer retailerSelected=-1,assetId;
    private final AssetTrackingBO assetBo = new AssetTrackingBO();
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        getDialog().getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        setCancelable(false);
        View view = inflater.inflate(R.layout.move_asset_dialog, container);
        Context context = getActivity();
        bmodel = (BusinessModel) context.getApplicationContext();
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
        TLDesc = (TextInputLayout) view.findViewById(R.id.input_layout_dialog_move_description);
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
                dismiss();
            }
        });

        serialNo = getArguments().getString("serialNo");
        retailerName = getArguments().getString("retailerName");
        assetName = getArguments().getString("assetName");
        brand=getArguments().getString("brand");
        assetId=getArguments().getInt("assetId");
        TVSerialNo.setText(getString(R.string.serial_no) + ": " + serialNo);
        TVOutletName.setText("Current "+getString(R.string.retailer_name)+": "+retailerName);
        TVAssetName.setText(assetName);
        initSpinner();

        super.onViewCreated(view, savedInstanceState);
    }

    private void initSpinner() {
        //Generating Reason List
        mAssetReasonList=new ArrayList<>();
        bmodel.reasonHelper.loadAssetReasonsBasedOnType("Asset_Move");
        mAssetReasonList.add(new ReasonMaster("0","--Select Reason--"));
        mAssetReasonList.addAll(bmodel.reasonHelper.getAssetReasonsBasedOnType());

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
        retailer.setMovRetailerName("Select Retailer");
        retailer.setMovRetailerId(Integer.toString(0));

        retailerMasterBOs = bmodel.downloadRetailerMasterData();
        retailerMasterBOs.add(0, retailer);

        ArrayList<String> mRetailerNameList = new ArrayList<>();
        for (int i = 0; i < retailerMasterBOs.size(); i++) {
            mRetailerNameList.add(retailerMasterBOs.get(i).getMovRetailerName());
        }
        if(mRetailerNameList.contains(retailerName))
            mRetailerNameList.remove(mRetailerNameList.indexOf(retailerName));

        ArrayAdapter<String> mRetailerSpinAdapter = new ArrayAdapter<>(getActivity(), R.layout.spinner_bluetext_layout, mRetailerNameList);
        mRetailerSpinAdapter.setDropDownViewResource(R.layout.spinner_bluetext_list_item);
        SpToOutletName.setAdapter(mRetailerSpinAdapter);

        SpToOutletName.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                retailerSelected=position;
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    private void saveFunction() {
        if (validateDesc())
            if (SpToOutletName.getSelectedItemPosition() != 0 && retailerSelected>0) {
                if (SpReason.getSelectedItemPosition() != 0) {
                    setAddAssetDetails();
                    bmodel.saveModuleCompletion(HomeScreenTwo.MENU_ASSET);
                    bmodel.assetTrackingHelper
                            .saveAssetMovementDetails("MENU_ASSET");
                    Toast.makeText(getActivity(), getResources().getString(R.string.saved_successfully),
                            Toast.LENGTH_SHORT).show();
                    dismiss();
                } else {
                    Toast.makeText(bmodel, "Select Reason and Try again", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(bmodel, "Select OutletName and Try again", Toast.LENGTH_SHORT).show();
            }
    }
    private void setAddAssetDetails() {

        String todayDate = DateUtil.convertFromServerDateToRequestedFormat(
                SDUtil.now(SDUtil.DATE_GLOBAL),
                ConfigurationMasterHelper.outDateFormat);
        String remarks=ETDesc.getText().toString().trim();
        retailerId=retailerMasterBOs.get(retailerSelected).getMovRetailerId();

        assetBo.setMposm(String.valueOf(assetId));
        assetBo.setMbrand(bmodel.assetTrackingHelper.getassetbrandids(brand));
        assetBo.setMnewinstaldate(todayDate);
        assetBo.setMsno(serialNo);
        assetBo.setMreasonId(reasonId);
        assetBo.setMremarks(remarks);
        assetBo.setmToRetailerId(retailerId);
        bmodel.assetTrackingHelper.setMassetTrackingBO(assetBo);

    }
    private boolean validateDesc() {
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
        Window window = getDialog().getWindow();
        lp.copyFrom(window.getAttributes());
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
        window.setAttributes(lp);
    }
}
