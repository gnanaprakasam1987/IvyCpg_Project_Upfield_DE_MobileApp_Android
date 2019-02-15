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
import com.bumptech.glide.request.target.BitmapImageViewTarget;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.ivy.sd.camera.CameraActivity;
import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.commons.IvyBaseActivityNoActionBar;
import com.ivy.sd.png.commons.SDUtil;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.provider.ConfigurationMasterHelper;
import com.ivy.sd.png.util.CommonDialog;
import com.ivy.sd.png.util.Commons;
import com.ivy.sd.png.util.DataMembers;
import com.ivy.sd.png.util.DateUtil;
import com.ivy.sd.png.view.HomeScreenTwo;
import com.ivy.utils.DateTimeUtils;

import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Vector;

/**
 * Created by rajkumar.s on 3/28/2017.
 * This dialog is used to add new asset.
 */

public class AddSerializedAssetActivity extends IvyBaseActivityNoActionBar implements View.OnClickListener, TextView.OnEditorActionListener {

    BusinessModel mBModel;

    private static final String SELECT = "-Select-";
    private Spinner mAsset;
    private Spinner mBrand;
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

    private final String moduleName = "NAT_";
    private static final int CAMERA_REQUEST_CODE = 1;
    private String photoPath;
    private String imageName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.add_serialized_asset_dialog);
        mBModel = (BusinessModel) getApplicationContext();
        mBModel.setContext(this);

        assetTrackingHelper = SerializedAssetHelper.getInstance(this);

        Toolbar mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            setScreenTitle(getResources().getString(R.string.addnewasset));
        }

        mAsset = (Spinner) findViewById(R.id.spinner_asset);
        mBrand = (Spinner) findViewById(R.id.spinner_brand);
        btnAddInstallDate = (Button) findViewById(R.id.date_button);
        mSNO = (EditText) findViewById(R.id.etxt_sno);
        editext_NFC_number = findViewById(R.id.etxt_nfc_number);
        btnSave = (Button) findViewById(R.id.btn_save);
        btnSave.setOnClickListener(this);
        imageView_barcode_scan = findViewById(R.id.imageView_barcode_scan);
        imageView_barcode_scan.setOnClickListener(this);
        iv_photo = findViewById(R.id.iv_photo);
        iv_photo.setOnClickListener(this);

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

        assetTrackingHelper.downloadUniqueAssets(getApplicationContext(), "MENU_ASSET");

        Vector mPOSMList = assetTrackingHelper.getAssetNames();

        int siz = mPOSMList.size();

        ArrayAdapter<CharSequence> mAssetSpinAdapter = new ArrayAdapter<>(
                this, R.layout.spinner_bluetext_layout);
        mAssetSpinAdapter
                .setDropDownViewResource(R.layout.spinner_bluetext_list_item);
        mAssetSpinAdapter.add(SELECT);

        for (int k = 0; k < siz; ++k) {
            mAssetSpinAdapter.add(mPOSMList.elementAt(k).toString());

        }

        Commons.print("mAssetSpinAdapter" + mAssetSpinAdapter + ","
                + mAsset);
        mAsset.setAdapter(mAssetSpinAdapter);
        mAsset.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> arg0, View arg1,
                                       int position, long arg3) {

               /* assetTrackingHelper
                        .downloadAssetBrand(getActivity().getApplicationContext(), assetTrackingHelper
                                .getAssetIds(mAsset.getSelectedItem()
                                        .toString()));

                if (position != 0
                        && assetTrackingHelper.getAssetBrandNames().size() > 0) {
                    loadBrandData();
                } else {
                    if (position == 0 || assetTrackingHelper.getAssetBrandNames().size() == 0)
                        ((TextView) getView().findViewById(R.id.brand_spinner_txt)).setVisibility(View.GONE);
                    mBrand.setVisibility(View.GONE);
                }*/


            }

            @Override
            public void onNothingSelected(AdapterView<?> arg0) {

            }
        });

        String todayDate = DateUtil.convertFromServerDateToRequestedFormat(
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
                                    btnAddInstallDate.setText(DateUtil
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
                                        btnAddInstallDate.setText(DateUtil
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
     * Load brands
     */
    private void loadBrandData() {
        ((TextView) findViewById(R.id.brand_spinner_txt)).setVisibility(View.VISIBLE);
        mBrand.setVisibility(View.VISIBLE);
        ArrayAdapter<CharSequence> mAssetBrandsAdapter = new ArrayAdapter<>(
                this, R.layout.spinner_bluetext_layout);
        mAssetBrandsAdapter
                .setDropDownViewResource(R.layout.spinner_bluetext_list_item);
        Vector mBrand = assetTrackingHelper.getAssetBrandNames();
        if (mBrand == null || mBrand.size() < 1) {
            this.mBrand.setAdapter(null);
            mAssetBrandsAdapter.add(SELECT);
            this.mBrand.setAdapter(mAssetBrandsAdapter);
            return;
        }
        int mBrandSize = mBrand.size();
        if (mBrandSize == 0)
            return;

        mAssetBrandsAdapter.add(SELECT);

        for (int i = 0; i < mBrandSize; ++i) {

            mAssetBrandsAdapter.add(mBrand.elementAt(i).toString());

        }
        this.mBrand.setAdapter(mAssetBrandsAdapter);
    }

    /**
     * Set values for adding asset
     */
    private void setAddAssetDetails() {

        assetBo.setPOSM(assetTrackingHelper.getAssetIds(mAsset
                .getSelectedItem().toString()));

            /*if (mBrand.getSelectedItem() != null) {
                if (!mBrand.getSelectedItem().toString()
                        .equals(SELECT))
                    assetBo.setBrand(assetTrackingHelper.getAssetBrandIds(mBrand
                            .getSelectedItem().toString()));
                else
                    assetBo.setBrand("0");
            } else*/
        assetBo.setBrand("0");

        assetBo.setNewInstallDate(btnAddInstallDate.getText().toString());

        assetBo.setSNO(mSNO.getText().toString());

        assetBo.setNFCTagId(editext_NFC_number.getText().toString());

        assetTrackingHelper.setAssetTrackingBO(assetBo);


    }

    @Override
    public void onClick(View view) {

        if (view.getId() == R.id.btn_save) {

            try {
                if (!mAsset.getSelectedItem().toString()
                        .equals(SELECT)
                        && !mSNO.getText().toString().equals("")) {
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
                                this,
                                getResources()
                                        .getString(
                                                R.string.serial_number_already_exists),
                                Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(
                            this,
                            getResources().getString(
                                    R.string.no_assets_exists),
                            Toast.LENGTH_SHORT).show();
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
            if (!mAsset.getSelectedItem().toString()
                    .equals(SELECT)
                    && !mSNO.getText().toString().equals("")) {
                if (!assetTrackingHelper
                        .getUniqueSerialNo(mSNO.getText()
                                .toString())) {
                    takePhoto();
                } else {
                    Toast.makeText(
                            this,
                            getResources()
                                    .getString(
                                            R.string.serial_number_already_exists),
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

    private void saveNewAsset() {
        setAddAssetDetails();
        mBModel.saveModuleCompletion(HomeScreenTwo.MENU_SERIALIZED_ASSET);


        assetTrackingHelper
                .saveNewAsset(getApplicationContext());
        Toast.makeText(
                this,
                getResources()
                        .getString(
                                R.string.saved_successfully),
                Toast.LENGTH_SHORT).show();
        //dismiss();
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
                Button ed = (Button) findViewById(vw.getId());
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
                        Toast.makeText(this, getResources().getString(R.string.cancelled), Toast.LENGTH_LONG).show();
                    } else {
                        mSNO.setText(result.getContents());
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

                imageName = moduleName
                        + mBModel.getRetailerMasterBO()
                        .getRetailerID() + "_"
                        + assetTrackingHelper.getAssetIds(mAsset.getSelectedItem().toString()) + "_"
                        + mSNO.getText().toString() + "_"
                        + Commons.now(Commons.DATE_TIME)
                        + "_img.jpg";

                String fileNameStarts = moduleName
                        + mBModel.getRetailerMasterBO()
                        .getRetailerID() + "_"
                        + assetTrackingHelper.getAssetIds(mAsset.getSelectedItem().toString()) + "_"
                        + mSNO.getText().toString() + "_"
                        + Commons.now(Commons.DATE);

                boolean nFilesThere = mBModel.checkForNFilesInFolder(photoPath, 1,
                        fileNameStarts);
                if (nFilesThere) {
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
// dialog.dismiss();
            }
        });
        commonDialog.show();
        commonDialog.setCancelable(false);
    }
}
