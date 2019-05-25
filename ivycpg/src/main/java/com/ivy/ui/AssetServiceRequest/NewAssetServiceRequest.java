package com.ivy.ui.AssetServiceRequest;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.ivy.core.base.view.BaseActivity;
import com.ivy.core.data.app.AppDataProvider;
import com.ivy.cpg.view.serializedAsset.SerializedAssetBO;
import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.bo.ReasonMaster;
import com.ivy.sd.png.bo.RetailerMasterBO;
import com.ivy.sd.png.bo.SpinnerBO;

import java.util.ArrayList;

import javax.inject.Inject;

public class NewAssetServiceRequest extends BaseActivity implements AssetServiceRequestContractor.AssetNewServiceView {

    private SerializedAssetBO assetBO;
    private Button button_save,button_cancel;
    private Spinner spinner_assets,spinner_issue_type;
    private EditText edittext_serialNumber,button_resolution_date,edittext_description;

   /* @Inject
    AppDataProvider appDataProvider;*/

    /*@Inject
    AssetServiceRequestContractor.Presenter<AssetServiceRequestContractor.AssetNewServiceView> presenter;*/

    @Override
    public int getLayoutId() {
        return R.layout.activity_new_asset_service_request;
    }

    @Override
    protected void setUpViews() {

        spinner_assets=findViewById(R.id.spinner_assets);
        spinner_issue_type=findViewById(R.id.spinner_issue_type);
        edittext_serialNumber=findViewById(R.id.edittext_serialNumber);
        button_resolution_date=findViewById(R.id.button_resolution_date);
        edittext_description=findViewById(R.id.edittext_description);
        button_save=findViewById(R.id.btn_next);
        button_cancel=findViewById(R.id.btn_cancel);
        button_save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setValues();

            }
        });
        button_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

      //  presenter.fetchLists(appDataProvider.getRetailMaster().getRetailerID());

    }

    @Override
    public void initializeDi() {

    }

    @Override
    protected void initVariables() {

    }

    @Override
    protected void getMessageFromAliens() {

    }

    private void setValues(){

        assetBO=new SerializedAssetBO();

        assetBO.setAssetID(((SerializedAssetBO)spinner_assets.getSelectedItem()).getAssetID());
        assetBO.setAssetServiceReqStatus("PENDING");
        assetBO.setReasonID(Integer.parseInt(((ReasonMaster)spinner_issue_type.getSelectedItem()).getReasonID()));
        assetBO.setSerialNo(edittext_serialNumber.getText().toString());
        assetBO.setNewInstallDate(button_resolution_date.getText().toString());
        assetBO.setIssueDescription(edittext_description.getText().toString());

    }


    @Override
    public void populateViews(ArrayList<SerializedAssetBO> assetList, ArrayList<ReasonMaster> issueTypes) {

        ArrayAdapter<SerializedAssetBO> assetAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item);
        assetAdapter
                .setDropDownViewResource(R.layout.spinner_new_retailer_text_list_item);
        SerializedAssetBO assetBO=new SerializedAssetBO();
        assetBO.setAssetID(0);
        assetBO.setAssetName(getResources().getString(R.string.select));
        assetAdapter.add(assetBO);
        assetAdapter.addAll(assetList);

        spinner_assets.setAdapter(assetAdapter);

        //
        ArrayAdapter<ReasonMaster> issueTypeAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item);
        issueTypeAdapter
                .setDropDownViewResource(R.layout.spinner_new_retailer_text_list_item);
        issueTypeAdapter.add(new ReasonMaster("0",getResources().getString(R.string.select)));
        issueTypeAdapter.addAll(issueTypes);
        spinner_issue_type.setAdapter(issueTypeAdapter);

    }

    @Override
    public void onSavedSuccessfully() {

    }

    @Override
    public void showErrorMessage(int type) {
        if(type==0){
            Toast.makeText(this,getResources().getString(R.string.something_went_wrong),Toast.LENGTH_LONG).show();
        }
    }
}
