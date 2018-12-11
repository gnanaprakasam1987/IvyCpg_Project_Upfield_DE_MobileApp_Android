package com.ivy.cpg.view.serializedAsset;

import android.Manifest;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
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

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.commons.IvyBaseActivityNoActionBar;
import com.ivy.sd.png.commons.SDUtil;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.provider.ConfigurationMasterHelper;
import com.ivy.sd.png.util.Commons;
import com.ivy.sd.png.util.DateUtil;
import com.ivy.sd.png.view.HomeScreenTwo;

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
    private EditText mSNO,editext_NFC_number;
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
        editext_NFC_number= findViewById(R.id.etxt_nfc_number);
        btnSave = (Button) findViewById(R.id.btn_save);
        btnSave.setOnClickListener(this);
        imageView_barcode_scan=findViewById(R.id.imageView_barcode_scan);
        imageView_barcode_scan.setOnClickListener(this);

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
                SDUtil.now(SDUtil.DATE_GLOBAL),
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
                        setAddAssetDetails();
                        mBModel.saveModuleCompletion(HomeScreenTwo.MENU_ASSET);


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
        }
        else if(view.getId()==R.id.imageView_barcode_scan){
            scanBarCode();
        }
    }

    private void scanBarCode(){
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
        }
        catch (Exception ex){
            Commons.printException(ex);
        }

    }
}
