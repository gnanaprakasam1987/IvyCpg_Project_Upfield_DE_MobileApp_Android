package com.ivy.cpg.view.serializedAsset;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.commons.IvyBaseActivityNoActionBar;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.provider.ConfigurationMasterHelper;

import java.util.ArrayList;
import java.util.Objects;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

public class SerialNoChangeActivity extends IvyBaseActivityNoActionBar implements BarCodeChangeListener {

    private BusinessModel bModel;
    private SerializedAssetHelper serializedAssetHelper;
    private SerialNoChangeAdapter serialNoChangeAdapter;
    private ArrayList<SerializedAssetBO> assetList;
    private int selectedPosition = -1;
    private CompositeDisposable compositeDisposable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_serial_no_change);
        serializedAssetHelper = SerializedAssetHelper.getInstance(this);
        compositeDisposable = new CompositeDisposable();
        bModel = (BusinessModel) getApplicationContext();
        bModel.setContext(this);

        Toolbar toolbar = findViewById(R.id.toolbar);
        RecyclerView recyclerViewChangeList = findViewById(R.id.serial_no_change_list_view);
        recyclerViewChangeList.setHasFixedSize(true);
        recyclerViewChangeList.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewChangeList.addItemDecoration(new DividerItemDecoration(this, DividerItemDecoration.VERTICAL));
        setSupportActionBar(toolbar);
        setUpToolbar(getIntent().getExtras().getString("screenTitle", getString(R.string.serial_no_change_request)));

        if (!serializedAssetHelper.SHOW_SERIAL_NO_IN_UPDATE_REQUEST)
            findViewById(R.id.tv_isAvail).setVisibility(View.GONE);

        fetchTransactionData();
        assetList = new ArrayList<>();
        assetList = serializedAssetHelper.getAssetTrackingList();
        serialNoChangeAdapter = new SerialNoChangeAdapter(this, assetList, this, ConfigurationMasterHelper.outDateFormat, serializedAssetHelper, bModel);
        recyclerViewChangeList.setAdapter(serialNoChangeAdapter);

        findViewById(R.id.btn_save_sno_change).setOnClickListener(v -> {

            onSaveBtnClick();
        });

    }

    private void fetchTransactionData() {

        compositeDisposable.add(serializedAssetHelper.fetchSerialNo(this)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<String>() {
                    @Override
                    public void accept(String s) throws Exception {

                    }
                }));

    }

    private boolean validateRentalPrice() {
        boolean isFlag = true;
        if (serializedAssetHelper.SHOW_ASSET_RENTAL_PRICE) {
            for (SerializedAssetBO assetBO : assetList) {

                if (assetBO.getRentalPrice() <= 0) {
                    isFlag = false;
                    break;
                }

            }
        }
        return isFlag;
    }


    private boolean validateEffToDate() {
        boolean isFlag = true;
        if (serializedAssetHelper.SHOW_ASSET_EFFECTIVE_DATE) {
            for (SerializedAssetBO assetBO : assetList) {

                if (assetBO.getEffectiveToDate().isEmpty()) {
                    isFlag = false;
                    break;
                }
            }
        }
        return isFlag;
    }

    private boolean validateSerialNo() {
        boolean isFlag = true;
        if (serializedAssetHelper.SHOW_SERIAL_NO_IN_UPDATE_REQUEST) {
            for (SerializedAssetBO assetBO : assetList) {

                if (assetBO.getNewSerialNo().isEmpty()) {
                    isFlag = false;
                    break;
                }
            }
        }
        return isFlag;
    }

    private void onSaveBtnClick() {
        if (validateRentalPrice()
                && validateEffToDate()
                && validateSerialNo()) {
            compositeDisposable = new CompositeDisposable();
            compositeDisposable.add(serializedAssetHelper.updateSerialNo(this, assetList)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(isUpdated -> {
                        if (isUpdated) {
                            showMessage(getString(R.string.saved_successfully));
                            finish();
                        } else
                            showMessage(getString(R.string.error));
                    }));
        } else {
            showMessage(getString(R.string.mandatory_fileds_empty));
        }
    }


    public void setUpToolbar(String title) {
        Objects.requireNonNull(getSupportActionBar()).setDisplayShowTitleEnabled(false);
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getSupportActionBar().setElevation(0);
        }

        if (title != null)
            setScreenTitle(title);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
    }

    @Override
    public void barCodeScan(int position) {
        selectedPosition = position;
        scanBarCode();
    }

    private void scanBarCode() {
        {
            (this).checkAndRequestPermissionAtRunTime(2);
            int permissionStatus = ContextCompat.checkSelfPermission(this,
                    Manifest.permission.CAMERA);
            if (permissionStatus == PackageManager.PERMISSION_GRANTED) {
                IntentIntegrator integrator = new IntentIntegrator(this) {
                    @Override
                    protected void startActivityForResult(Intent intent, int code) {
                        SerialNoChangeActivity.this.startActivityForResult(intent, IntentIntegrator.REQUEST_CODE); // REQUEST_CODE override
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


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if (requestCode == IntentIntegrator.REQUEST_CODE) {
            if (result != null) {
                if (result.getContents() == null) {
                    Toast.makeText(this, getResources().getString(R.string.cancelled), Toast.LENGTH_LONG).show();
                } else {
                    if (isUniqueSerialNo(result.getContents())) {
                        assetList.get(selectedPosition).setNewSerialNo(result.getContents());
                        serialNoChangeAdapter.notifyDataSetChanged();
                    } else
                        showMessage(getResources().getString(R.string.serial_no) + " " + getResources().getString(R.string.already_exist));

                    selectedPosition = -1;
                }
            }
        }

    }

    public boolean isUniqueSerialNo(String serialNo) {
        return (!serializedAssetHelper
                .getUniqueSerialNo(serialNo));
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            if (isDataAvailable())
                backNavigationAlertDialog();
            else
                finish();
        }

        return super.onOptionsItemSelected(item);
    }

    private boolean isDataAvailable() {

        for (SerializedAssetBO bo : assetList) {
            if (!bo.getNewSerialNo().equals("0"))
                return true;
        }
        return false;
    }

    private void backNavigationAlertDialog() {


        showAlert("", getString(R.string.data_cleared_do_u_want_go_back), () -> {
            for (SerializedAssetBO bo : assetList) {
                bo.setNewSerialNo("0");
            }
            finish();
        }, () -> {

        });
    }

}
