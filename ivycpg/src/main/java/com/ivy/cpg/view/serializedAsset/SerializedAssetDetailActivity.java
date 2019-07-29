package com.ivy.cpg.view.serializedAsset;

import android.os.Bundle;
import android.os.Environment;
import android.view.MenuItem;
import android.view.View;

import androidx.appcompat.widget.AppCompatTextView;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.ivy.lib.adapter.GridImageViewAdapter;
import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.commons.IvyBaseActivityNoActionBar;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.util.DataMembers;

import java.util.ArrayList;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;

import static android.content.res.Configuration.SCREENLAYOUT_SIZE_LARGE;

public class SerializedAssetDetailActivity extends IvyBaseActivityNoActionBar {

    BusinessModel mBModel;
    private RecyclerView assetImgListView;
    private AppCompatTextView assetNameTv;
    private AppCompatTextView serialNoTv;
    private AppCompatTextView installDateTv;
    private AppCompatTextView lastServiceDateTv;
    private AppCompatTextView sihQtyTv;
    private AppCompatTextView priceTv;
    private AppCompatTextView assetModelTv;
    private AppCompatTextView assetTypeTv;
    private AppCompatTextView assetVendorTv;
    private AppCompatTextView assetCapacityTv;
    private AppCompatTextView assetRentPriceTv;
    private AppCompatTextView effFromDateTv;
    private AppCompatTextView effToDateTv;
    private SerializedAssetBO detailBo;
    SerializedAssetHelper assetTrackingHelper;
    private Toolbar toolbar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_serialized_asset_detail);
        mBModel = (BusinessModel) getApplicationContext();
        mBModel.setContext(this);
        assetTrackingHelper = SerializedAssetHelper.getInstance(this);
        initializeView();

        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            setScreenTitle(getResources().getString(R.string.details));
        }

        updateView();
    }

    private void initializeView() {
        toolbar = findViewById(R.id.toolbar);
        assetImgListView = findViewById(R.id.asset_image_list);
        assetNameTv = findViewById(R.id.asset_name_tv);
        serialNoTv = findViewById(R.id.asset_serial_no_tv);
        installDateTv = findViewById(R.id.install_date_tv);
        lastServiceDateTv = findViewById(R.id.service_date_tv);
        sihQtyTv = findViewById(R.id.sih_qty_tv);
        priceTv = findViewById(R.id.asset_price_tv);
        assetModelTv = findViewById(R.id.asset_model_tv);
        assetTypeTv = findViewById(R.id.asset_type_tv);
        assetVendorTv = findViewById(R.id.asset_vendor_tv);
        assetCapacityTv = findViewById(R.id.asset_capacity_tv);
        assetRentPriceTv = findViewById(R.id.asset_rental_price_tv);
        effFromDateTv = findViewById(R.id.eff_from_date_tv);
        effToDateTv = findViewById(R.id.eff_to_date_tv);
        setUpRecyclerView();

        if (getIntent().getExtras() != null)
            detailBo = getIntent().getExtras().getParcelable("detailBo");

        if (!assetTrackingHelper.SHOW_ASSET_TYPE) {
            assetTypeTv.setVisibility(View.GONE);
            findViewById(R.id.asset_type_label).setVisibility(View.GONE);
        } else {
            if (mBModel.labelsMasterHelper.applyLabels(findViewById(
                    R.id.asset_type_label).getTag()) != null)
                ((AppCompatTextView) findViewById(R.id.asset_type_label))
                        .setText(mBModel.labelsMasterHelper
                                .applyLabels(findViewById(
                                        R.id.asset_type_label).getTag()));
        }

        if (!assetTrackingHelper.SHOW_ASSET_CAPACITY) {
            findViewById(R.id.asset_capacity_label).setVisibility(View.GONE);
            assetCapacityTv.setVisibility(View.GONE);
        } else {
            if (mBModel.labelsMasterHelper.applyLabels(findViewById(
                    R.id.asset_capacity_label).getTag()) != null)
                ((AppCompatTextView) findViewById(R.id.asset_capacity_label))
                        .setText(mBModel.labelsMasterHelper
                                .applyLabels(findViewById(
                                        R.id.asset_capacity_label).getTag()));

        }


        if (!assetTrackingHelper.SHOW_ASSET_MODEL) {
            findViewById(R.id.asset_model_label).setVisibility(View.GONE);
            assetModelTv.setVisibility(View.GONE);
        } else {

            if (mBModel.labelsMasterHelper.applyLabels(findViewById(
                    R.id.asset_model_label).getTag()) != null)
                ((AppCompatTextView) findViewById(R.id.asset_model_label))
                        .setText(mBModel.labelsMasterHelper
                                .applyLabels(findViewById(
                                        R.id.asset_model_label).getTag()));
        }


        if (!assetTrackingHelper.SHOW_ASSET_VENDOR) {
            findViewById(R.id.asset_vendor_label).setVisibility(View.GONE);
            assetVendorTv.setVisibility(View.GONE);
        } else {
            if (mBModel.labelsMasterHelper.applyLabels(findViewById(
                    R.id.asset_vendor_label).getTag()) != null)
                ((AppCompatTextView) findViewById(R.id.asset_vendor_label))
                        .setText(mBModel.labelsMasterHelper
                                .applyLabels(findViewById(
                                        R.id.asset_vendor_label).getTag()));

        }

        if (mBModel.labelsMasterHelper.applyLabels(findViewById(
                R.id.install_date_label).getTag()) != null)
            ((AppCompatTextView) findViewById(R.id.install_date_label))
                    .setText(mBModel.labelsMasterHelper
                            .applyLabels(findViewById(
                                    R.id.install_date_label).getTag()));


        if (mBModel.labelsMasterHelper.applyLabels(findViewById(
                R.id.service_date_label).getTag()) != null)
            ((AppCompatTextView) findViewById(R.id.service_date_label))
                    .setText(mBModel.labelsMasterHelper
                            .applyLabels(findViewById(
                                    R.id.service_date_label).getTag()));


        if (assetTrackingHelper.SHOW_ASSET_SIH) {
            if (mBModel.labelsMasterHelper.applyLabels(findViewById(
                    R.id.sih_qty_label).getTag()) != null)
                ((AppCompatTextView) findViewById(R.id.sih_qty_label))
                        .setText(mBModel.labelsMasterHelper
                                .applyLabels(findViewById(
                                        R.id.sih_qty_label).getTag()));
        } else {
            findViewById(R.id.sih_qty_label).setVisibility(View.GONE);
            sihQtyTv.setVisibility(View.GONE);
        }

        if (mBModel.labelsMasterHelper.applyLabels(findViewById(
                R.id.asset_price_label).getTag()) != null)
            ((AppCompatTextView) findViewById(R.id.asset_price_label))
                    .setText(mBModel.labelsMasterHelper
                            .applyLabels(findViewById(
                                    R.id.asset_price_label).getTag()));

        if (mBModel.labelsMasterHelper.applyLabels(findViewById(
                R.id.asset_rental_price_label).getTag()) != null)
            ((AppCompatTextView) findViewById(R.id.asset_rental_price_label))
                    .setText(mBModel.labelsMasterHelper
                            .applyLabels(findViewById(
                                    R.id.asset_rental_price_label).getTag()));

        if (mBModel.labelsMasterHelper.applyLabels(findViewById(
                R.id.eff_from_date_label).getTag()) != null)
            ((AppCompatTextView) findViewById(R.id.eff_from_date_label))
                    .setText(mBModel.labelsMasterHelper
                            .applyLabels(findViewById(
                                    R.id.eff_from_date_label).getTag()));

        if (mBModel.labelsMasterHelper.applyLabels(findViewById(
                R.id.eff_to_date_label).getTag()) != null)
            ((AppCompatTextView) findViewById(R.id.eff_to_date_label))
                    .setText(mBModel.labelsMasterHelper
                            .applyLabels(findViewById(
                                    R.id.eff_to_date_label).getTag()));


    }

    private void setUpRecyclerView() {
        boolean is7InchTablet = this.getResources().getConfiguration()
                .isLayoutSizeAtLeast(SCREENLAYOUT_SIZE_LARGE);
        assetImgListView.setHasFixedSize(true);
        assetImgListView.setNestedScrollingEnabled(false);
        assetImgListView.setItemAnimator(new DefaultItemAnimator());
        if (is7InchTablet) {
            assetImgListView.setLayoutManager(new GridLayoutManager(this, 3));
        } else {
            assetImgListView.setLayoutManager(new GridLayoutManager(this, 2));
        }
    }


    private void updateView() {
        assetNameTv.setText(detailBo.getAssetName());
        String serialNoStr = getString(R.string.serial_no) + ": " + detailBo.getSerialNo();
        serialNoTv.setText(serialNoStr);
        sihQtyTv.setText(String.valueOf(detailBo.getSihQty()));
        priceTv.setText(String.valueOf(detailBo.getAssetPrice()));
        assetRentPriceTv.setText(String.valueOf(detailBo.getRentalPrice()));
        installDateTv.setText(detailBo.getmLastInstallDate());
        lastServiceDateTv.setText(detailBo.getServiceDate());
        assetModelTv.setText(detailBo.getModelName());
        assetTypeTv.setText(detailBo.getAssetType());
        assetVendorTv.setText(detailBo.getVendorName());
        assetCapacityTv.setText(String.valueOf(detailBo.getCapacity()));
        effFromDateTv.setText(detailBo.getEffectiveFromDate());
        effToDateTv.setText(detailBo.getEffectiveToDate());

        loadImageIntoListView();
    }

    private void loadImageIntoListView() {
        String filePath = getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS) + "/"
                + mBModel.getAppDataProvider().getUser().getUserid()
                + DataMembers.DIGITAL_CONTENT + "/"
                + DataMembers.SERIALIZED_ASSET_DIG_CONTENT + "/";

        ArrayList<String> imageList = new ArrayList<>();
        new CompositeDisposable().add(assetTrackingHelper.fetchAssetImages(this, detailBo.getAssetID())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new DisposableObserver<ArrayList<String>>() {
                    @Override
                    public void onNext(ArrayList<String> imageContentList) {
                        if (!imageContentList.isEmpty()) {
                            imageList.clear();
                            imageList.addAll(imageContentList);
                        }
                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onComplete() {

                    }
                }));
        assetImgListView.setAdapter(new GridImageViewAdapter(this, imageList, filePath));

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            finish();
        }

        return super.onOptionsItemSelected(item);
    }
}
