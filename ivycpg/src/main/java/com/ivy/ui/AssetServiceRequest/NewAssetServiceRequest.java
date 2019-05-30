package com.ivy.ui.AssetServiceRequest;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;

import com.ivy.core.base.view.BaseActivity;
import com.ivy.core.data.app.AppDataProvider;
import com.ivy.cpg.view.serializedAsset.SerializedAssetBO;
import com.ivy.lib.ImageAdapterListener;
import com.ivy.lib.adapter.GridImageViewAdapter;
import com.ivy.sd.camera.CameraActivity;
import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.bo.ReasonMaster;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.provider.ConfigurationMasterHelper;
import com.ivy.sd.png.util.CommonDialog;
import com.ivy.sd.png.util.MyDatePickerDialog;
import com.ivy.ui.AssetServiceRequest.di.AssetServiceRequestModule;
import com.ivy.ui.AssetServiceRequest.di.DaggerAssetServiceRequestComponent;
import com.ivy.utils.DateTimeUtils;
import com.ivy.utils.FileUtils;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Objects;

import javax.inject.Inject;

public class NewAssetServiceRequest extends BaseActivity implements AssetServiceRequestContractor.AssetNewServiceView,ImageAdapterListener {

    private SerializedAssetBO currentAssetBO;
    private Button button_save,button_resolution_date;
    private Spinner spinner_assets,spinner_issue_type,spinner_service_provider;
    private EditText edittext_serialNumber,edittext_description;
    GridImageViewAdapter adapter;
    ArrayList<String> imageNameList;
    RecyclerView recyclerView;
    private boolean isEditMode,isFromReport;
    LinearLayout layout_service_provider;


    @Inject
    AppDataProvider appDataProvider;

    @Inject
    AssetServiceRequestContractor.Presenter<AssetServiceRequestContractor.AssetServiceView> presenter;

    @Override
    public int getLayoutId() {
        return R.layout.activity_new_asset_service_request;
    }

    @Override
    protected void setUpViews() {

        Toolbar toolbar =  findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
           getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
           getSupportActionBar().setDisplayShowHomeEnabled(true);
           if(isEditMode)
               setScreenTitle(getString(R.string.edit_service_request));
           else
            setScreenTitle( getString(R.string.new_service_request));
        }
        overridePendingTransition(R.anim.trans_left_in, R.anim.trans_left_out);

        spinner_assets=findViewById(R.id.spinner_assets);
        spinner_issue_type=findViewById(R.id.spinner_issue_type);
        spinner_service_provider=findViewById(R.id.spinner_service_provider);
        edittext_serialNumber=findViewById(R.id.edittext_serialNumber);
        button_resolution_date=findViewById(R.id.button_resolution_date);
        edittext_description=findViewById(R.id.edittext_description);
        button_save=findViewById(R.id.btn_next);
        layout_service_provider=findViewById(R.id.layout_service_provider);
        button_save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setValues();
                presenter.validateRequests(currentAssetBO);


            }
        });

        Calendar mCalendar = Calendar.getInstance();
        mCalendar.add(Calendar.DAY_OF_YEAR,1);
        button_resolution_date.setText(DateTimeUtils.convertDateObjectToRequestedFormat(mCalendar.getTime(), ConfigurationMasterHelper.outDateFormat));
        button_resolution_date.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Calendar mCalendar = Calendar.getInstance();

                if(isEditMode){
                   Date date= DateTimeUtils.convertStringToDateObject(button_resolution_date.getText().toString(),ConfigurationMasterHelper.outDateFormat);
                   mCalendar.setTime(date);
                }
                else {
                    mCalendar.add(Calendar.DAY_OF_YEAR,1);
                }

                int year = mCalendar.get(Calendar.YEAR);
                int month = mCalendar.get(Calendar.MONTH);
                int day = mCalendar.get(Calendar.DAY_OF_MONTH);

                MyDatePickerDialog dialog = new MyDatePickerDialog(NewAssetServiceRequest.this, R.style.DatePickerDialogStyle,
                        mDeliverDatePickerListener, year, month, day);
                dialog.setPermanentTitle(getResources().getString(R.string.choose_date));
                dialog.getDatePicker().setMinDate(mCalendar.getTimeInMillis());
                dialog.show();
            }
        });

        recyclerView=findViewById(R.id.recylerView_photo);
        LinearLayoutManager layout = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL,
                false);
        recyclerView.setLayoutManager(layout);



        presenter.fetchLists(isFromReport);

    }


    @Override
    public void initializeDi() {

           DaggerAssetServiceRequestComponent.builder().ivyAppComponent(((BusinessModel) Objects.requireNonNull(this).getApplication()).getComponent())
                .assetServiceRequestModule(new AssetServiceRequestModule(this))
                .build()
                .inject(this);
    }

    @Override
    protected void initVariables() {


        if(!isEditMode)
            currentAssetBO =new SerializedAssetBO();

    }

    @Override
    protected void getMessageFromAliens() {

        if (getIntent().getExtras() != null) {
            isEditMode = getIntent().getExtras().getBoolean("isEditMode", false);
            currentAssetBO = getIntent().getExtras().getParcelable("obj");
            isFromReport = getIntent().getExtras().getBoolean("isFromReport", false);
        }

    }

    private void setValues(){

        currentAssetBO.setAssetID(((SerializedAssetBO)spinner_assets.getSelectedItem()).getAssetID());
        currentAssetBO.setAssetServiceReqStatus("PENDING");
        currentAssetBO.setReasonID(Integer.parseInt(((ReasonMaster)spinner_issue_type.getSelectedItem()).getReasonID()));
        currentAssetBO.setSerialNo(edittext_serialNumber.getText().toString());
        currentAssetBO.setNewInstallDate(DateTimeUtils.convertToServerDateFormat(button_resolution_date.getText().toString(), ConfigurationMasterHelper.outDateFormat));
        currentAssetBO.setIssueDescription(edittext_description.getText().toString());

    }


    @Override
    public void populateViews(ArrayList<SerializedAssetBO> assetList, ArrayList<ReasonMaster> issueTypes,ArrayList<ReasonMaster> serviceProviders) {

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

        imageNameList=new ArrayList<>();
        imageNameList.add("captureImage");
        adapter=new GridImageViewAdapter(this,imageNameList,FileUtils.photoFolderPath,this);
        recyclerView.setAdapter(adapter);

        //
        ArrayAdapter<ReasonMaster> providerAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item);
        providerAdapter
                .setDropDownViewResource(R.layout.spinner_new_retailer_text_list_item);
        providerAdapter.add(new ReasonMaster("0",getResources().getString(R.string.select)));
        providerAdapter.addAll(serviceProviders);
        spinner_service_provider.setAdapter(providerAdapter);

        if(isEditMode){
            for(int i=0;i<assetList.size();i++){
                if(currentAssetBO.getAssetID()==assetList.get(i).getAssetID()){
                    spinner_assets.setSelection(i+1);
                    break;
                }
            }

            for(int i=0;i<issueTypes.size();i++){
                if(currentAssetBO.getReasonID()==Integer.parseInt(issueTypes.get(i).getReasonID())){
                    spinner_issue_type.setSelection(i+1);
                    break;
                }
            }

            for(int i=0;i<serviceProviders.size();i++){
                if(currentAssetBO.getServiceProviderId()==Integer.parseInt(serviceProviders.get(i).getReasonID())){
                    spinner_issue_type.setSelection(i+1);
                    break;
                }
            }

            edittext_description.setText(currentAssetBO.getIssueDescription());
            edittext_serialNumber.setText(currentAssetBO.getSerialNo());
            button_resolution_date.setText(DateTimeUtils.convertFromServerDateToRequestedFormat(currentAssetBO.getNewInstallDate(),ConfigurationMasterHelper.outDateFormat));
        }

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int i = item.getItemId();
        if (i == android.R.id.home) {
            if(currentAssetBO.getImageName()!=null&&!currentAssetBO.getImageName().equals("")){

                showBackButtonAlert();
            }
            else {
                finish();
                overridePendingTransition(R.anim.trans_right_in, R.anim.trans_right_out);
            }
        }

        return super.onOptionsItemSelected(item);
    }

    private void showBackButtonAlert(){

        CommonDialog dialog = new CommonDialog(this, getResources().getString(R.string.doyouwantgoback),
                "", getResources().getString(R.string.ok), new CommonDialog.PositiveClickListener() {
            @Override
            public void onPositiveButtonClick() {
                FileUtils.deleteFiles(FileUtils.photoFolderPath,
                        currentAssetBO.getImageName());
               finish();
               overridePendingTransition(R.anim.trans_right_in, R.anim.trans_right_out);

            }
        }, getResources().getString(R.string.cancel), new CommonDialog.negativeOnClickListener() {
            @Override
            public void onNegativeButtonClick() {

            }
        });
        dialog.show();
        dialog.setCancelable(false);

    }

    private void saveButtonAlert(){
        CommonDialog dialog = new CommonDialog(this, isEditMode?getResources().getString(R.string.do_u_want_to_update):getResources().getString(R.string.do_u_want_to_save),
                "", getResources().getString(R.string.yes), new CommonDialog.PositiveClickListener() {
            @Override
            public void onPositiveButtonClick() {
                if(isEditMode){
                    presenter.updateRequest(currentAssetBO);
                }
                else
                presenter.saveNewRequest(currentAssetBO);

            }
        }, getResources().getString(R.string.no), new CommonDialog.negativeOnClickListener() {
            @Override
            public void onNegativeButtonClick() {

            }
        });
        dialog.show();
        dialog.setCancelable(false);
    }
    @Override
    public void saveRequest() {
              saveButtonAlert();
    }

    @Override
    public void onSavedSuccessfully() {
        Toast.makeText(this,getResources().getString(R.string.saved_successfully),Toast.LENGTH_LONG).show();
        finish();
    }

    @Override
    public void onUpdatedSuccessfully() {
        Toast.makeText(this,getResources().getString(R.string.updated_successfully),Toast.LENGTH_LONG).show();
        finish();
    }

    @Override
    public void showErrorMessage(int type) {
        if(type==0){
            Toast.makeText(this,getResources().getString(R.string.something_went_wrong),Toast.LENGTH_LONG).show();
        }
    }

    private String PHOTO_PATH = "";
    private static final int CAMERA_REQUEST_CODE = 1;
    private final String moduleName = "ASR";
    String imageName="";

    @Override
    public void onTakePhoto() {

        // one photo allowed but by default one item added to show new photo option, so checking as 2
        if(imageNameList.size()<2) {
            String uID = DateTimeUtils.now(DateTimeUtils.DATE_TIME_ID);
            imageName = moduleName + uID + "_img.jpg";

            Intent intent = new Intent(this, CameraActivity.class);
            intent.putExtra(CameraActivity.QUALITY, 40);
            String _path = FileUtils.photoFolderPath + "/" + imageName;
            intent.putExtra(CameraActivity.PATH, _path);
            startActivityForResult(intent, CAMERA_REQUEST_CODE);
        }
        else {
            Toast.makeText(this,"Only one photo required for a request.",Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CAMERA_REQUEST_CODE && resultCode == 1) {
           imageNameList.add(imageName);
           currentAssetBO.setImageName(imageName);
           adapter.notifyDataSetChanged();
        }
    }

    @Override
    public void deletePhoto(String fileName) {
        FileUtils.deleteFiles(FileUtils.photoFolderPath,
                fileName);
    }

    private final DatePickerDialog.OnDateSetListener mDeliverDatePickerListener = new DatePickerDialog.OnDateSetListener() {
        public void onDateSet(DatePicker view, int year, int monthOfYear,
                              int dayOfMonth) {
            Calendar selectedDate = new GregorianCalendar(year, monthOfYear,
                    dayOfMonth);
            button_resolution_date.setText(DateTimeUtils.convertDateObjectToRequestedFormat(selectedDate.getTime(), ConfigurationMasterHelper.outDateFormat));

            view.updateDate(year, monthOfYear, dayOfMonth);
        }
    };
    @Override
    public void showEmptySerialNumberMessage() {
        Toast.makeText(this,getResources().getString(R.string.please_enter_serial_number),Toast.LENGTH_LONG).show();
    }

    @Override
    public void showEmptyAssetMessage() {
        Toast.makeText(this,getResources().getString(R.string.choose_asset),Toast.LENGTH_LONG).show();
    }

    @Override
    public void showEmptyIssueTypeMessage() {
        Toast.makeText(this,getResources().getString(R.string.select_issue_type),Toast.LENGTH_LONG).show();
    }

    @Override
    public void showServiceProvider() {
        layout_service_provider.setVisibility(View.VISIBLE);
    }
}
