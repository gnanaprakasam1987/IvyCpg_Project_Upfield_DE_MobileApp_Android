package com.ivy.cpg.view.serializedAsset;

import android.content.Context;
import android.os.Bundle;
import androidx.appcompat.widget.AppCompatAutoCompleteTextView;
import androidx.appcompat.widget.AppCompatButton;
import androidx.appcompat.widget.AppCompatSpinner;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.appcompat.widget.Toolbar;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.ivy.cpg.view.asset.bo.AssetAddDetailBO;
import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.commons.IvyBaseActivityNoActionBar;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.provider.ConfigurationMasterHelper;
import com.ivy.sd.png.util.MyDatePickerDialog;
import com.ivy.sd.png.view.HomeScreenTwo;
import com.ivy.utils.DateTimeUtils;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

public class SerializedAssetRequestActivity extends IvyBaseActivityNoActionBar {

    BusinessModel mBModel;

    private final SerializedAssetBO assetBo = new SerializedAssetBO();
    SerializedAssetHelper assetTrackingHelper;

    private ArrayList<SerializedAssetBO> modelList;
    private ArrayList<SerializedAssetBO> vendorList;
    private ArrayList<SerializedAssetBO> typeList;
    private ArrayList<String> capacityList;

    private ArrayList<AssetAddDetailBO> posmList;
    private String mSelectedModel = "0";
    private String mSelectedVendor = "0";
    private String mSelectedType = "0";
    private String mSelectedCapacity = "0";
    private AssetAddDetailBO mSelectedPOSM = null;
    private ArrayAdapter<AssetAddDetailBO> mAssetSpinAdapter;
    private int mYear;
    private int mMonth;
    private int mDay;
    private MyDatePickerDialog datePickerDialog;


    AppCompatTextView assetTypeTv;


    AppCompatTextView assetModelTv;


    AppCompatTextView assetVendorTv;


    AppCompatTextView assetCapacityTv;

    AppCompatTextView assetTv;

    AppCompatTextView deliveryDateTv;


    AppCompatSpinner modelSpinner;


    AppCompatSpinner capacitySpinner;


    AppCompatSpinner vendorSpinner;


    AppCompatSpinner typeSpinner;


    AppCompatAutoCompleteTextView assetAutoCompleteTv;


    AppCompatButton deliveryBtn;


    Toolbar toolbar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_serialized_asset_request);


        mBModel = (BusinessModel) getApplicationContext();
        mBModel.setContext(this);

        assetTrackingHelper = SerializedAssetHelper.getInstance(this);
        initializeView();
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            setScreenTitle(getResources().getString(R.string.new_request));
        }

        handleVisibility();
        loadData();

    }

    private void hideSoftInputFromWindow() {

        InputMethodManager inputManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        inputManager.hideSoftInputFromWindow(
                assetAutoCompleteTv.getWindowToken(), 0);
    }


    private void initializeView() {
        toolbar = findViewById(R.id.toolbar);
        assetTypeTv = findViewById(R.id.label_asset_type);
        assetModelTv = findViewById(R.id.label_asset_model);
        assetVendorTv = findViewById(R.id.label_asset_vendor);
        assetCapacityTv = findViewById(R.id.label_asset_capacity);
        assetTv = findViewById(R.id.asset_reason_label);
        deliveryDateTv = findViewById(R.id.delivery_date_Tv);
        deliveryBtn = findViewById(R.id.delivery_date_button);

        modelSpinner = findViewById(R.id.spinner_model);
        vendorSpinner = findViewById(R.id.spinner_vendor);
        capacitySpinner = findViewById(R.id.spinner_capcity);
        typeSpinner = findViewById(R.id.spinner_type);
        assetAutoCompleteTv = findViewById(R.id.auto_complete_tv_asset);


        if (mBModel.labelsMasterHelper.applyLabels(assetTypeTv.getTag()) != null)
            assetTypeTv.setText(mBModel.labelsMasterHelper.applyLabels(assetTypeTv.getTag()));

        if (mBModel.labelsMasterHelper.applyLabels(assetModelTv) != null)
            assetModelTv.setText(mBModel.labelsMasterHelper
                    .applyLabels(assetModelTv.getTag()));

        if (mBModel.labelsMasterHelper.applyLabels(assetVendorTv.getTag()) != null)
            assetVendorTv.setText(mBModel.labelsMasterHelper
                    .applyLabels(assetVendorTv.getTag()));

        if (mBModel.labelsMasterHelper.applyLabels(assetCapacityTv.getTag()) != null)
            assetCapacityTv.setText(mBModel.labelsMasterHelper
                    .applyLabels(assetCapacityTv.getTag()));

        if (mBModel.labelsMasterHelper.applyLabels(assetTv.getTag()) != null)
            assetTv.setText(mBModel.labelsMasterHelper
                    .applyLabels(assetTv));

        if (mBModel.labelsMasterHelper.applyLabels(deliveryDateTv.getTag()) != null)
            deliveryDateTv.setText(mBModel.labelsMasterHelper
                    .applyLabels(deliveryDateTv.getTag()));
    }

    private void handleVisibility() {

        if (!assetTrackingHelper.SHOW_ASSET_TYPE) {
            assetTypeTv.setVisibility(View.GONE);
            typeSpinner.setVisibility(View.GONE);
        }
        if (!assetTrackingHelper.SHOW_ASSET_CAPACITY) {
            assetCapacityTv.setVisibility(View.GONE);
            capacitySpinner.setVisibility(View.GONE);
        }
        if (!assetTrackingHelper.SHOW_ASSET_MODEL) {
            assetModelTv.setVisibility(View.GONE);
            modelSpinner.setVisibility(View.GONE);
        }
        if (!assetTrackingHelper.SHOW_ASSET_VENDOR) {
            assetVendorTv.setVisibility(View.GONE);
            vendorSpinner.setVisibility(View.GONE);
        }
    }

    private void loadData() {

        posmList = assetTrackingHelper.downloadUniqueAssets(getApplicationContext());

        mAssetSpinAdapter = new ArrayAdapter<>(
                this, R.layout.autocompelete_bluetext_layout, posmList);
        mAssetSpinAdapter
                .setDropDownViewResource(R.layout.autocomplete_bluetext_list_item);


        assetAutoCompleteTv.setThreshold(1);
        assetAutoCompleteTv.setSelection(0);
        assetAutoCompleteTv.setAdapter(mAssetSpinAdapter);

        assetAutoCompleteTv.setOnTouchListener((v, event) -> {
            assetAutoCompleteTv.showDropDown();
            return false;
        });

        assetAutoCompleteTv.setOnItemClickListener((parent, view, position, id) -> {
            mSelectedPOSM = mAssetSpinAdapter.getItem(position);
            hideSoftInputFromWindow();
        });

        modelList = assetTrackingHelper.getAssetModels(this);
        vendorList = assetTrackingHelper.getAssetVendors(this);
        typeList = assetTrackingHelper.getAssetTypes(this);
        capacityList = assetTrackingHelper.getAssetCapacity(this);


        SerializedAssetBO tempBO = new SerializedAssetBO(1);
        tempBO.setVendorName(getString(R.string.all));
        tempBO.setVendorId("0");
        vendorList.add(0, tempBO);

        tempBO = new SerializedAssetBO(2);
        tempBO.setModelName(getString(R.string.all));
        tempBO.setModelId("0");
        modelList.add(0, tempBO);

        tempBO = new SerializedAssetBO(3);
        tempBO.setAssetType(getString(R.string.all));
        tempBO.setAssetTypeId("0");
        typeList.add(0, tempBO);


        capacityList.add(0, getString(R.string.all));

        ArrayAdapter<SerializedAssetBO> mModelAdapter = new ArrayAdapter<>(this,
                R.layout.spinner_bluetext_layout, modelList);
        mModelAdapter
                .setDropDownViewResource(R.layout.spinner_bluetext_list_item);

        ArrayAdapter<SerializedAssetBO> mVendorAdapter = new ArrayAdapter<>(this,
                R.layout.spinner_bluetext_layout, vendorList);
        mVendorAdapter
                .setDropDownViewResource(R.layout.spinner_bluetext_list_item);

        ArrayAdapter<String> mCapacityAdapter = new ArrayAdapter<>(this,
                R.layout.spinner_bluetext_layout, capacityList);
        mCapacityAdapter
                .setDropDownViewResource(R.layout.spinner_bluetext_list_item);

        ArrayAdapter<SerializedAssetBO> mAssetTypeAdapter = new ArrayAdapter<>(this,
                R.layout.spinner_bluetext_layout, typeList);
        mAssetTypeAdapter
                .setDropDownViewResource(R.layout.spinner_bluetext_list_item);


        capacitySpinner.setAdapter(mCapacityAdapter);
        capacitySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> arg0, View arg1,
                                       int position, long arg3) {
                ((TextView) capacitySpinner.getSelectedView().findViewById(android.R.id.text1)).setGravity(Gravity.START);
                mSelectedCapacity = capacityList.get(position);
                filterAsset();
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {

            }
        });

        vendorSpinner.setAdapter(mVendorAdapter);
        vendorSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> arg0, View arg1,
                                       int position, long arg3) {
                ((TextView) vendorSpinner.getSelectedView().findViewById(android.R.id.text1)).setGravity(Gravity.START);
                mSelectedVendor = vendorList.get(position).getVendorId();
                filterAsset();
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {

            }
        });

        modelSpinner.setAdapter(mModelAdapter);
        modelSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> arg0, View arg1,
                                       int position, long arg3) {
                ((TextView) modelSpinner.getSelectedView().findViewById(android.R.id.text1)).setGravity(Gravity.START);
                mSelectedModel = modelList.get(position).getModelId();
                filterAsset();
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {

            }
        });

        typeSpinner.setAdapter(mAssetTypeAdapter);
        typeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> arg0, View arg1,
                                       int position, long arg3) {
                ((TextView) typeSpinner.getSelectedView().findViewById(android.R.id.text1)).setGravity(Gravity.START);
                mSelectedType = typeList.get(position).getAssetTypeId();
                filterAsset();
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {

            }
        });


        String todayDate = DateTimeUtils.convertFromServerDateToRequestedFormat(
                DateTimeUtils.now(DateTimeUtils.DATE_GLOBAL),
                ConfigurationMasterHelper.outDateFormat);


        deliveryBtn.setText(todayDate);
        final Calendar c = Calendar.getInstance();
        mYear = c.get(Calendar.YEAR);
        mMonth = c.get(Calendar.MONTH);
        mDay = c.get(Calendar.DAY_OF_MONTH);

        deliveryBtn.setOnClickListener(v -> {
            hideSoftInputFromWindow();

            if (datePickerDialog == null)
                createDatePickerDialog();

            datePickerDialog.show();
        });

        findViewById(R.id.btn_save).setOnClickListener(v -> {
            saveRequestedAsset();
        });

    }

    private void createDatePickerDialog() {
        // Launch Date Picker Dialog
        datePickerDialog = new MyDatePickerDialog(
                SerializedAssetRequestActivity.this, R.style.DatePickerDialogStyle,
                (view, year, monthOfYear, dayOfMonth) -> {
                    mYear = year;
                    mMonth = monthOfYear;
                    mDay = dayOfMonth;
                    Calendar selectedDate = new GregorianCalendar(
                            year, monthOfYear, dayOfMonth);
                    deliveryBtn.setText(DateTimeUtils
                            .convertDateObjectToRequestedFormat(
                                    selectedDate.getTime(),
                                    ConfigurationMasterHelper.outDateFormat));
                    Calendar mCurrentCalendar = Calendar
                            .getInstance();
                    if (mCurrentCalendar.after(selectedDate)) {
                        Toast.makeText(
                                SerializedAssetRequestActivity.this,
                                R.string.Please_select_next_day,
                                Toast.LENGTH_SHORT).show();
                        deliveryBtn.setText(DateTimeUtils
                                .convertDateObjectToRequestedFormat(
                                        mCurrentCalendar.getTime(),
                                        ConfigurationMasterHelper.outDateFormat));

                        mYear = mCurrentCalendar.get(Calendar.YEAR);
                        mMonth = mCurrentCalendar.get(Calendar.MONTH);
                        mDay = mCurrentCalendar.get(Calendar.DAY_OF_MONTH);

                    }

                }, mYear, mMonth, mDay);
        datePickerDialog.setPermanentTitle(getString(R.string.choose_date));
    }


    private void filterAsset() {

        ArrayList<AssetAddDetailBO> filterList = new ArrayList<>();
        for (AssetAddDetailBO assetAddDetailBO : posmList) {
            if (vendorSpinner.getSelectedItemPosition() != 0 && mSelectedVendor.equals(assetAddDetailBO.getVendorId()))
                filterList.add(assetAddDetailBO);
            if (modelSpinner.getSelectedItemPosition() != 0 && mSelectedModel.equals(assetAddDetailBO.getModelId()) && !filterList.contains(assetAddDetailBO))
                filterList.add(assetAddDetailBO);
            if (capacitySpinner.getSelectedItemPosition() != 0 && mSelectedCapacity.equals(assetAddDetailBO.getCapacity()) && !filterList.contains(assetAddDetailBO))
                filterList.add(assetAddDetailBO);
            if (typeSpinner.getSelectedItemPosition() != 0 && mSelectedType.equals(assetAddDetailBO.getTypeId()) && !filterList.contains(assetAddDetailBO))
                filterList.add(assetAddDetailBO);

        }

        if (capacitySpinner.getSelectedItemPosition() == 0 && vendorSpinner.getSelectedItemPosition() == 0 && modelSpinner.getSelectedItemPosition() == 0 && typeSpinner.getSelectedItemPosition() == 0)
            filterList.addAll(posmList);

        if (filterList.get(0) != null && !filterList.get(0).getPOSMDescription().equalsIgnoreCase(getString(R.string.plain_select))) {
            AssetAddDetailBO tempPosm = new AssetAddDetailBO();
            tempPosm.setPOSMId("0");
            tempPosm.setPOSMDescription(getString(R.string.plain_select));
            filterList.add(0, tempPosm);
        }

        updatedData(filterList);

    }


    public void updatedData(ArrayList<AssetAddDetailBO> filterList) {
        mAssetSpinAdapter.clear();
        if (filterList != null) {
            for (AssetAddDetailBO assetAddDetailBO : filterList) {
                mAssetSpinAdapter.insert(assetAddDetailBO, mAssetSpinAdapter.getCount());
            }
        }
        mAssetSpinAdapter.notifyDataSetChanged();
    }

    private void saveRequestedAsset() {

        if (assetAutoCompleteTv.getText().toString().isEmpty()) {
            showMessage(getString(R.string.choose_asset));
            return;
        }

        setRequestAssetDetails();
        onSaveBtnClick();
    }

    private void onSaveBtnClick() {
        new CompositeDisposable().add(assetTrackingHelper.saveNewRequestAsset(this)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(isSaved -> {
                    if (isSaved) {
                        mBModel.saveModuleCompletion(HomeScreenTwo.MENU_SERIALIZED_ASSET, true);
                        showAlert("", getString(R.string.saved_successfully), () -> {
                            finish();
                        });
                    } else
                        showMessage(getString(R.string.something_went_wrong));
                }));
    }


    /**
     * Set values for adding asset
     */
    private void setRequestAssetDetails() {

        assetBo.setPOSM(mSelectedPOSM.getPOSMId());

        assetBo.setBrand("0");

        assetBo.setDeliveryDate(deliveryBtn.getText().toString());

        assetTrackingHelper.setAssetTrackingBO(assetBo);
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int i = item.getItemId();
        if (i == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }
}
