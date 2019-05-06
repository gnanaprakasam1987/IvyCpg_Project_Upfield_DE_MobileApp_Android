package com.ivy.cpg.view.serializedAsset;

import android.Manifest;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.target.BitmapImageViewTarget;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.ivy.cpg.view.asset.bo.AssetAddDetailBO;
import com.ivy.sd.camera.CameraActivity;
import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.bo.ReasonMaster;
import com.ivy.sd.png.commons.IvyBaseActivityNoActionBar;
import com.ivy.sd.png.commons.SDUtil;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.provider.ConfigurationMasterHelper;
import com.ivy.sd.png.util.CommonDialog;
import com.ivy.sd.png.util.Commons;
import com.ivy.sd.png.util.DataMembers;
import com.ivy.sd.png.view.HomeScreenTwo;
import com.ivy.utils.DateTimeUtils;
import com.ivy.utils.FontUtils;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;

/**
 * Created by rajkumar.s on 3/28/2017.
 * This dialog is used to add new asset.
 */

public class AddSerializedAssetActivity extends IvyBaseActivityNoActionBar implements View.OnClickListener, TextView.OnEditorActionListener {

    BusinessModel mBModel;

    private static final String SELECT = "-Select-";
    private static final String ALL = "All";
    private Spinner mAsset;
    private EditText mSNO, editext_NFC_number;
    private Button btnAddInstallDate;
    EditText edittext;

    private int mYear;
    private int mMonth;
    private int mDay;
    Button btnSave;
    private String append = "";

    private final SerializedAssetBO assetBo = new SerializedAssetBO();
    SerializedAssetHelper assetTrackingHelper;

    ImageView imageView_barcode_scan;
    ImageView iv_photo;

    private static final int CAMERA_REQUEST_CODE = 1;
    private String photoPath;
    private String imageName;

    private ArrayList<SerializedAssetBO> modelList;
    private ArrayList<SerializedAssetBO> vendorList;
    private ArrayList<SerializedAssetBO> typeList;
    private ArrayList<String> capacityList;

    private ArrayList<ReasonMaster> noBarCodeReasonList;

    private Spinner modelSpinner;
    private Spinner capacitySpinner;
    private Spinner vendorSpinner;
    private Spinner typeSpinner;

    private Spinner barcodeNoReasonSpinner;
    private Spinner nfcNoReasonSpinner;

    private AssetAddDetailBO mSelectedPOSM = null;
    private ArrayList<AssetAddDetailBO> posmList;
    private String mSelectedModel = "0";
    private String mSelectedVendor = "0";
    private String mSelectedType = "0";
    private String mSelectedCapacity = "0";
    private String mSelectedScanReasonId = "0";
    private boolean isSerialNumberCaptured;

    private ArrayAdapter<AssetAddDetailBO> mAssetSpinAdapter;
    private TextView txtNFCLabel, txtSerialNo;
    String nfcTag = "", serialNoTag = "";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.add_serialized_asset_dialog);
        mBModel = (BusinessModel) getApplicationContext();
        mBModel.setContext(this);

        assetTrackingHelper = SerializedAssetHelper.getInstance(this);

        Toolbar mToolbar = findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            setScreenTitle(getResources().getString(R.string.addnewasset));
        }

        txtNFCLabel = findViewById(R.id.txtNFCLabel);
        ((TextView) findViewById(R.id.txtNFCLabel)).setTypeface(FontUtils.getFontRoboto(this, FontUtils.FontType.LIGHT));
        if (mBModel.labelsMasterHelper.applyLabels(findViewById(R.id.txtNFCLabel).getTag()) != null){
            nfcTag = mBModel.labelsMasterHelper.applyLabels(findViewById(R.id.txtNFCLabel).getTag());
            ((TextView) findViewById(R.id.txtNFCLabel)).setText(nfcTag);
        }

        txtSerialNo = findViewById(R.id.label_scan);
        serialNoTag = getResources().getString(R.string.serial_no);
        ((TextView) findViewById(R.id.label_scan)).setTypeface(FontUtils.getFontRoboto(this, FontUtils.FontType.LIGHT));
        if (mBModel.labelsMasterHelper.applyLabels(findViewById(R.id.label_scan).getTag()) != null) {
            serialNoTag = mBModel.labelsMasterHelper.applyLabels(findViewById(R.id.label_scan).getTag());
            ((TextView) findViewById(R.id.label_scan)).setText(serialNoTag);
        }

        if (mBModel.labelsMasterHelper.applyLabels(findViewById(
                R.id.label_asset_type).getTag()) != null)
            ((TextView) findViewById(R.id.label_asset_type))
                    .setText(mBModel.labelsMasterHelper
                            .applyLabels(findViewById(
                                    R.id.label_asset_type).getTag()));

        if (mBModel.labelsMasterHelper.applyLabels(findViewById(
                R.id.label_asset_model).getTag()) != null)
            ((TextView) findViewById(R.id.label_asset_model))
                    .setText(mBModel.labelsMasterHelper
                            .applyLabels(findViewById(
                                    R.id.label_asset_model).getTag()));

        if (mBModel.labelsMasterHelper.applyLabels(findViewById(
                R.id.label_asset_vendor).getTag()) != null)
            ((TextView) findViewById(R.id.label_asset_vendor))
                    .setText(mBModel.labelsMasterHelper
                            .applyLabels(findViewById(
                                    R.id.label_asset_vendor).getTag()));

        if (mBModel.labelsMasterHelper.applyLabels(findViewById(
                R.id.label_asset_capacity).getTag()) != null)
            ((TextView) findViewById(R.id.label_asset_capacity))
                    .setText(mBModel.labelsMasterHelper
                            .applyLabels(findViewById(
                                    R.id.label_asset_capacity).getTag()));

        mAsset = findViewById(R.id.spinner_asset);
        btnAddInstallDate = findViewById(R.id.date_button);
        mSNO = findViewById(R.id.etxt_sno);
        editext_NFC_number = findViewById(R.id.etxt_nfc_number);
        btnSave = findViewById(R.id.btn_save);
        btnSave.setOnClickListener(this);
        imageView_barcode_scan = findViewById(R.id.imageView_barcode_scan);
        imageView_barcode_scan.setOnClickListener(this);
        iv_photo = findViewById(R.id.iv_photo);
        iv_photo.setOnClickListener(this);
        modelSpinner = findViewById(R.id.spinner_model);
        vendorSpinner = findViewById(R.id.spinner_vendor);
        capacitySpinner = findViewById(R.id.spinner_capcity);
        typeSpinner = findViewById(R.id.spinner_type);
        barcodeNoReasonSpinner = findViewById(R.id.spinner_bar_code_reason);
        nfcNoReasonSpinner = findViewById(R.id.spinner_nfc_reason);


        if (!assetTrackingHelper.SHOW_ASSET_TYPE)
            findViewById(R.id.ll_asset_type).setVisibility(View.GONE);
        if (!assetTrackingHelper.SHOW_ASSET_CAPACITY)
            findViewById(R.id.ll_asset_capacity).setVisibility(View.GONE);
        if (!assetTrackingHelper.SHOW_ASSET_MODEL)
            findViewById(R.id.ll_asset_model).setVisibility(View.GONE);
        if (!assetTrackingHelper.SHOW_ASSET_VENDOR)
            findViewById(R.id.ll_asset_vendor).setVisibility(View.GONE);

        loadData();


    }


    @Override
    public void onStart() {
        super.onStart();


    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);

    }

    /**
     * Preparing screen
     */

    private void loadData() {

        if (!assetTrackingHelper.NEW_ASSET_PHOTO)
            findViewById(R.id.llnewassetPhoto).setVisibility(View.GONE);

        posmList = assetTrackingHelper.downloadUniqueAssets(getApplicationContext());


        AssetAddDetailBO tempPosm = new AssetAddDetailBO();
        tempPosm.setPOSMId("0");
        tempPosm.setPOSMDescription(SELECT);
        posmList.add(0, tempPosm);

        mSelectedPOSM = tempPosm;

        mAssetSpinAdapter = new ArrayAdapter<>(
                this, R.layout.spinner_bluetext_layout, posmList);
        mAssetSpinAdapter
                .setDropDownViewResource(R.layout.spinner_bluetext_list_item);


        Commons.print("mAssetSpinAdapter" + mAssetSpinAdapter + ","
                + mAsset);
        mAsset.setAdapter(mAssetSpinAdapter);
        mAsset.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> arg0, View arg1,
                                       int position, long arg3) {

                mSelectedPOSM = mAssetSpinAdapter.getItem(position);
                enableBarCodeViews(true);


            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {

            }
        });

        modelList = assetTrackingHelper.getAssetModels(this);
        vendorList = assetTrackingHelper.getAssetVendors(this);
        typeList = assetTrackingHelper.getAssetTypes(this);
        capacityList = assetTrackingHelper.getAssetCapacity(this);
        noBarCodeReasonList = assetTrackingHelper.getmAssetBCReasonList();

        SerializedAssetBO tempBO = new SerializedAssetBO(1);
        tempBO.setVendorName(ALL);
        tempBO.setVendorId("0");
        vendorList.add(0, tempBO);

        tempBO = new SerializedAssetBO(2);
        tempBO.setModelName(ALL);
        tempBO.setModelId("0");
        modelList.add(0, tempBO);

        tempBO = new SerializedAssetBO(3);
        tempBO.setAssetType(ALL);
        tempBO.setAssetTypeId("0");
        typeList.add(0, tempBO);

        ReasonMaster reasonMaster = new ReasonMaster();
        reasonMaster.setReasonDesc(getString(R.string.select_reason));
        reasonMaster.setReasonID("0");
        noBarCodeReasonList.add(0, reasonMaster);

        capacityList.add(0,ALL);

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

        ArrayAdapter<ReasonMaster> mBarcodeReasonAdapter = new ArrayAdapter<>(this,
                R.layout.spinner_bluetext_layout, noBarCodeReasonList);
        mBarcodeReasonAdapter
                .setDropDownViewResource(R.layout.spinner_bluetext_list_item);

        ArrayAdapter<ReasonMaster> mNFCCodeReasonAdapter = new ArrayAdapter<>(this,
                R.layout.spinner_bluetext_layout, noBarCodeReasonList);
        mNFCCodeReasonAdapter
                .setDropDownViewResource(R.layout.spinner_bluetext_list_item);

        capacitySpinner.setAdapter(mCapacityAdapter);
        capacitySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> arg0, View arg1,
                                       int position, long arg3) {
                    mSelectedCapacity = capacityList.get(position);
                    filterPosm();
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
                        mSelectedVendor = vendorList.get(position).getVendorId();
                        filterPosm();
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
                        mSelectedModel = modelList.get(position).getModelId();
                        filterPosm();
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
                mSelectedType = typeList.get(position).getAssetTypeId();
                filterPosm();
            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {

            }
        });

        barcodeNoReasonSpinner.setAdapter(mBarcodeReasonAdapter);
        barcodeNoReasonSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {

                mSelectedScanReasonId = noBarCodeReasonList.get(position).getReasonID();

                if (!mSelectedScanReasonId.equals("0")) {
                    mSNO.setEnabled(true);
                } /*else {
                    mSNO.setEnabled(true);
                }*/

            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        nfcNoReasonSpinner.setAdapter(mNFCCodeReasonAdapter);
        nfcNoReasonSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {

                mSelectedScanReasonId = noBarCodeReasonList.get(position).getReasonID();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });


           String todayDate = DateTimeUtils.convertFromServerDateToRequestedFormat(
                DateTimeUtils.now(DateTimeUtils.DATE_GLOBAL),
                ConfigurationMasterHelper.outDateFormat);


        btnAddInstallDate.setText(todayDate);
        final Calendar c = Calendar.getInstance();
        mYear = c.get(Calendar.YEAR);
        mMonth = c.get(Calendar.MONTH);
        mDay = c.get(Calendar.DAY_OF_MONTH);


        btnAddInstallDate.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                Button b = (Button) v;
                if (b == btnAddInstallDate) {

                    // Launch Date Picker Dialog
                    DatePickerDialog dpd = new DatePickerDialog(
                            AddSerializedAssetActivity.this, R.style.DatePickerDialogStyle,
                            new DatePickerDialog.OnDateSetListener() {

                                @Override
                                public void onDateSet(DatePicker view,
                                                      int year, int monthOfYear,
                                                      int dayOfMonth) {
                                    mYear = year;
                                    mMonth = monthOfYear;
                                    mDay = dayOfMonth;
                                    Calendar selectedDate = new GregorianCalendar(
                                            year, monthOfYear, dayOfMonth);
                                    btnAddInstallDate.setText(DateTimeUtils
                                            .convertDateObjectToRequestedFormat(
                                                    selectedDate.getTime(),
                                                    ConfigurationMasterHelper.outDateFormat));
                                    Calendar mCurrentCalendar = Calendar
                                            .getInstance();
                                    if (selectedDate.after(mCurrentCalendar)) {
                                        Toast.makeText(
                                                AddSerializedAssetActivity.this,
                                                R.string.future_date_not_allowed,
                                                Toast.LENGTH_SHORT).show();
                                        btnAddInstallDate.setText(DateTimeUtils
                                                .convertDateObjectToRequestedFormat(
                                                        mCurrentCalendar.getTime(),
                                                        ConfigurationMasterHelper.outDateFormat));

                                        mYear = mCurrentCalendar.get(Calendar.YEAR);
                                        mMonth = mCurrentCalendar.get(Calendar.MONTH);
                                        mDay = mCurrentCalendar.get(Calendar.DAY_OF_MONTH);

                                    }

                                }
                            }, mYear, mMonth, mDay);
                    dpd.show();
                }

            }
        });

        mSNO.addTextChangedListener(new TextWatcher() {

            @Override
            public void onTextChanged(CharSequence s, int start, int before,
                                      int count) {
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count,
                                          int after) {
            }

            @Override
            public void afterTextChanged(Editable s) {

                if (s.toString().length() > 0)
                    mSNO.setSelection(s.toString().length());
            }
        });
    }

    /**
     * Set values for adding asset
     */
    private void setAddAssetDetails() {

        assetBo.setPOSM(mSelectedPOSM.getPOSMId());

        assetBo.setBrand("0");

        assetBo.setNewInstallDate(btnAddInstallDate.getText().toString());

        assetBo.setReasonId(mSelectedScanReasonId);

        assetBo.setSNO(mSNO.getText().toString());

        assetBo.setNFCTagId(editext_NFC_number.getText().toString());

        assetTrackingHelper.setAssetTrackingBO(assetBo);


    }

    @Override
    public void onClick(View view) {

        if (view.getId() == R.id.btn_save) {

            try {
                if (mAsset.getSelectedItemPosition() != 0
                        && serialNoValidation()) {
                    if(editext_NFC_number.getText().toString().trim().equals("")){
                        Toast.makeText(AddSerializedAssetActivity.this,
                                getResources()
                                        .getString(
                                                R.string.enter) + " " + nfcTag,
                                Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if(mSNO.getText().toString().trim().equals("")){
                        Toast.makeText(AddSerializedAssetActivity.this,
                                getResources()
                                        .getString(
                                                R.string.enter) + " " + serialNoTag,
                                Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (!assetTrackingHelper
                            .getUniqueSerialNo(mSNO.getText()
                                    .toString())) {
                        if (assetTrackingHelper.NEW_ASSET_PHOTO && assetTrackingHelper.NEW_ASSET_PHOTO_MANDATORY) {
                            if (assetBo.getImageName() != null && assetBo.getImageName().length() > 0) {
                                saveNewAsset();
                            } else {
                                Toast.makeText(
                                        this,
                                        getResources()
                                                .getString(
                                                        R.string.photo_mandatory),
                                        Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            saveNewAsset();
                        }


                    } else {
                        Toast.makeText(
                                this,serialNoTag + " " + getResources().getString(R.string.already_exist),
                                Toast.LENGTH_SHORT).show();
                        //enabled edit option if serial no already exists
                        enableBarCodeViews(true);
                    }
                } else {

                    if (mAsset.getSelectedItemPosition() == 0)
                        showMessage(getString(R.string.choose_asset));

                    else if (assetTrackingHelper.SHOW_SERIAL_NO_REASON
                            && mSelectedScanReasonId.equals("0"))
                        showMessage(getString(
                                R.string.serial_no_reason_mandatory));

                    else if ((assetTrackingHelper.SHOW_SERIAL_NO_REASON
                            && !assetTrackingHelper.IS_SERIAL_NO_NOT_MANDATORY))
                        showMessage(getString(
                                R.string.enter_serial_no));
                }

            } catch (Exception e) {
                Toast.makeText(
                        this,
                        getResources().getString(
                                R.string.no_assets_exists),
                        Toast.LENGTH_SHORT).show();
            }
        } else if (view.getId() == R.id.btn_cancel) {
            //dismiss();
            finish();
        } else if (view.getId() == R.id.imageView_barcode_scan) {
            scanBarCode();
        } else if (view.getId() == R.id.iv_photo) {
            if (mAsset.getSelectedItemPosition() != 0
                    && (!mSNO.getText().toString().isEmpty() || !mSelectedScanReasonId.equals("0"))) {
                if (!assetTrackingHelper
                        .getUniqueSerialNo(mSNO.getText()
                                .toString())) {
                    takePhoto();
                } else {
                    Toast.makeText(
                            this,serialNoTag + " " + getResources().getString(R.string.already_exist),
                            Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(
                        this,
                        getResources().getString(
                                R.string.no_assets_exists),
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

    private boolean serialNoValidation() {

        if (assetTrackingHelper.SHOW_ASSET_BARCODE
                && assetTrackingHelper.SHOW_SERIAL_NO_REASON) {

            if (isSerialNumberCaptured
                    && mSNO.getText().length() > 3)
                return true;
            else if (!assetTrackingHelper.IS_SERIAL_NO_NOT_MANDATORY
                    && mSNO.getText().length() > 3 && !mSelectedScanReasonId.equals("0"))
                return true;

            else return assetTrackingHelper.IS_SERIAL_NO_NOT_MANDATORY
                        && !mSelectedScanReasonId.equals("0");
        } else
            return mSNO.getText().length() > 3;

    }

    private void saveNewAsset() {
        setAddAssetDetails();
        mBModel.saveModuleCompletion(HomeScreenTwo.MENU_SERIALIZED_ASSET, true);


        assetTrackingHelper
                .saveNewAsset(getApplicationContext());
        Toast.makeText(
                this,
                getResources()
                        .getString(
                                R.string.saved_successfully),
                Toast.LENGTH_SHORT).show();
        finish();
    }

    private void scanBarCode() {
        {
            checkAndRequestPermissionAtRunTime(2);
            int permissionStatus = ContextCompat.checkSelfPermission(this,
                    Manifest.permission.CAMERA);
            if (permissionStatus == PackageManager.PERMISSION_GRANTED) {
                IntentIntegrator integrator = new IntentIntegrator(this) {
                    @Override
                    protected void startActivityForResult(Intent intent, int code) {
                        AddSerializedAssetActivity.this.startActivityForResult(intent, IntentIntegrator.REQUEST_CODE); // REQUEST_CODE override
                    }
                };
                integrator.setBeepEnabled(false).initiateScan();
            } else {
                Toast.makeText(this,
                        getResources().getString(R.string.permission_enable_msg)
                                + " " + getResources().getString(R.string.permission_camera)
                        , Toast.LENGTH_LONG).show();
            }
        }
        if (!assetTrackingHelper.SHOW_SERIAL_NO_REASON)
            findViewById(R.id.barcode_reason_layout).setVisibility(View.GONE);

    }

    /**
     * Key pad click event
     *
     * @param vw Selected View
     */
    public void numberPressed(View vw) {
        if (edittext == null) {
            mBModel.showAlert(
                    getResources().getString(R.string.please_select_item), 0);
        } else {
            int id = vw.getId();
            if (id == R.id.calcdel) {
                int s = SDUtil.convertToInt(edittext.getText()
                        .toString());
                s = s / 10;
                String strQty = s + "";
                edittext.setText(strQty);
            } else {
                Button ed = findViewById(vw.getId());
                append = ed.getText().toString();
                eff();
            }
        }
    }

    /**
     * Set values to the selected view
     */
    private void eff() {
        String s = edittext.getText().toString();
        if (!"0".equals(s) && !"0.0".equals(s)) {
            String strQty = edittext.getText() + append;
            edittext.setText(strQty);
        } else
            edittext.setText(append);
    }


    @Override
    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        return false;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        try {
            IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
            if (requestCode == IntentIntegrator.REQUEST_CODE) {
                if (result != null) {
                    if (result.getContents() == null) {
                        Toast.makeText(this, getResources().getString(R.string.serial_no_not_captured_kindly_choose_reason), Toast.LENGTH_LONG).show();
                        barcodeNoReasonSpinner.setSelection(0);
                        enableBarCodeViews(true);
                        isSerialNumberCaptured = false;
                    } else {
                        mSNO.setText(result.getContents());
                        barcodeNoReasonSpinner.setSelection(0);
                        enableBarCodeViews(false);
                        isSerialNumberCaptured = true;
                    }
                }
            }
            if (requestCode == CAMERA_REQUEST_CODE) {
                if (resultCode == 1) {
                    Commons.print("AddSerialAsset" + ",Camers Activity : Sucessfully Captured.");

                    //For adding server ref path to image name
                    String imagePath = "Asset/"
                            + mBModel.userMasterHelper.getUserMasterBO().getDownloadDate()
                            .replace("/", "") + "/"
                            + mBModel.userMasterHelper.getUserMasterBO().getUserid() + "/" + imageName;


                    Glide.with(getApplicationContext())
                            .load(this.getExternalFilesDir(Environment.DIRECTORY_PICTURES) + "/" + DataMembers.photoFolderName + "/" + imageName)
                            .asBitmap()
                            .centerCrop()
                            .placeholder(R.drawable.ic_photo_camera)
                            .diskCacheStrategy(DiskCacheStrategy.NONE)
                            .skipMemoryCache(true)
                            .transform(mBModel.circleTransform)
                            .into(new BitmapImageViewTarget(iv_photo));

                    assetBo.setImageName(imagePath);
                    assetBo.setImgName(imageName);
                } else {
                    Commons.print("AddSerialAsset" + ",Camers Activity : Canceled");
                }
            }
        } catch (Exception ex) {
            Commons.printException(ex);
        }

    }

    private void takePhoto() {
        checkAndRequestPermissionAtRunTime(2);
        int permissionStatus = ContextCompat.checkSelfPermission(this,
                Manifest.permission.CAMERA);
        if (permissionStatus == PackageManager.PERMISSION_GRANTED) {
            if (mBModel.synchronizationHelper
                    .isExternalStorageAvailable()) {

                photoPath = getExternalFilesDir(
                        Environment.DIRECTORY_PICTURES)
                        + "/" + DataMembers.photoFolderName + "/";

                imageName = "NAT_"
                        + mBModel.getAppDataProvider().getRetailMaster()
                        .getRetailerID() + "_"
                        + mSelectedPOSM.getPOSMId() + "_"
                        + mSNO.getText().toString() + "_"
                        + Commons.now(Commons.DATE_TIME)
                        + "_img.jpg";

                String fileNameStarts = "NAT_"
                        + mBModel.getAppDataProvider().getRetailMaster()
                        .getRetailerID() + "_"
                        + mSelectedPOSM.getPOSMId() + "_"
                        + mSNO.getText().toString() + "_"
                        + Commons.now(Commons.DATE);

                boolean nFilesThere = mBModel.checkForNFilesInFolder(photoPath, 1,
                        fileNameStarts);
                if (nFilesThere) {
                    imageName = assetBo.getImgName();
                    showFileDeleteAlertWithImage(fileNameStarts, imageName);

                } else {
                    Intent intent = new Intent(this,
                            CameraActivity.class);
                    intent.putExtra(CameraActivity.QUALITY, 40);
                    String path = photoPath + "/" + imageName;
                    intent.putExtra(CameraActivity.PATH, path);
                    startActivityForResult(intent, CAMERA_REQUEST_CODE);
                }

            } else {
                Toast.makeText(this, getResources().getString(R.string.external_storage_not_available)
                        , Toast.LENGTH_SHORT)
                        .show();

            }
        } else {
            Toast.makeText(this,
                    getResources().getString(R.string.permission_enable_msg)
                            + " " + getResources().getString(R.string.permission_camera)
                    , Toast.LENGTH_LONG).show();
        }

    }

    private void showFileDeleteAlertWithImage(final String imageNameStarts,
                                              final String imageSrc) {
        final CommonDialog commonDialog = new CommonDialog(AddSerializedAssetActivity.this.getApplication(), //Context
                AddSerializedAssetActivity.this, //Context
                "", //Title
                getResources().getString(R.string.word_already) + " " + 1 + " " + getResources().getString(R.string.word_photocaptured_delete_retake), //Message
                true, //ToDisplayImage
                getResources().getString(R.string.yes), //Positive Button
                getResources().getString(R.string.no), //Negative Button
                false, //MoveToNextActivity
                this.getExternalFilesDir(Environment.DIRECTORY_PICTURES) + "/" + DataMembers.photoFolderName + "/" + imageSrc, //LoadImage
                new CommonDialog.PositiveClickListener() {
                    @Override
                    public void onPositiveButtonClick() {
                        assetBo.setImageName("");
                        assetBo.setImgName("");
                        mBModel.deleteFiles(photoPath,
                                imageNameStarts);
                        Intent intent = new Intent(getApplicationContext(),
                                CameraActivity.class);
                        intent.putExtra(CameraActivity.QUALITY, 40);
                        String path = photoPath + "/" + imageName;
                        intent.putExtra(CameraActivity.PATH, path);
                        startActivityForResult(intent,
                                CAMERA_REQUEST_CODE);

                    }
                }, new CommonDialog.negativeOnClickListener() {
            @Override
            public void onNegativeButtonClick() {
            }
        });
        commonDialog.show();
        commonDialog.setCancelable(false);
    }

    private void filterPosm() {

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

        if(filterList.get(0)!= null && !filterList.get(0).getPOSMDescription().equalsIgnoreCase(SELECT)){
            AssetAddDetailBO tempPosm = new AssetAddDetailBO();
            tempPosm.setPOSMId("0");
            tempPosm.setPOSMDescription(SELECT);
            filterList.add(0, tempPosm);
        }

        updatedData(filterList);

    }

    public void updatedData(ArrayList<AssetAddDetailBO> filterList) {
        mAssetSpinAdapter.clear();
        if (filterList != null){
            for (AssetAddDetailBO assetAddDetailBO : filterList) {
                mAssetSpinAdapter.insert(assetAddDetailBO, mAssetSpinAdapter.getCount());
            }
        }
        mAssetSpinAdapter.notifyDataSetChanged();
    }

    private void enableBarCodeViews(boolean flag) {
        barcodeNoReasonSpinner.setEnabled(flag);
        mSNO.setEnabled(flag);
    }
}
