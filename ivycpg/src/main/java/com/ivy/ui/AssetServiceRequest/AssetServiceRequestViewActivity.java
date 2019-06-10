package com.ivy.ui.AssetServiceRequest;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.ivy.core.base.view.BaseActivity;
import com.ivy.core.data.app.AppDataProvider;
import com.ivy.cpg.view.serializedAsset.SerializedAssetBO;
import com.ivy.lib.ImageAdapterListener;
import com.ivy.lib.adapter.GridImageViewAdapter;
import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.provider.ConfigurationMasterHelper;
import com.ivy.sd.png.view.HomeScreenTwo;
import com.ivy.ui.AssetServiceRequest.di.AssetServiceRequestModule;
import com.ivy.ui.AssetServiceRequest.di.DaggerAssetServiceRequestComponent;
import com.ivy.ui.notes.NoteConstant;
import com.ivy.ui.task.TaskConstant;
import com.ivy.utils.DateTimeUtils;
import com.ivy.utils.FileUtils;

import java.util.ArrayList;
import java.util.Objects;

import javax.inject.Inject;

import butterknife.BindView;

public class AssetServiceRequestViewActivity extends BaseActivity implements AssetServiceRequestContractor.AssetServiceFullDetailView,ImageAdapterListener {

    @Inject
    AssetServiceRequestContractor.Presenter<AssetServiceRequestContractor.AssetServiceView> presenter;

    @Inject
    AppDataProvider appDataProvider;

    @BindView(R.id.recylerView_photo)
    RecyclerView recyclerView;

    GridImageViewAdapter adapter;
    ArrayList<String> imageNameList;

    String requestId;
    boolean isFromReport;
    @BindView(R.id.textview_asset_name)
    TextView textview_asset_name;
    @BindView(R.id.textview_retailer_name)
    TextView textview_retailer_name ;
    @BindView(R.id.textview_serial_num)
    TextView textview_serial_num;
    @BindView(R.id.textview_type)
    TextView textview_type;
    @BindView(R.id.textview_resolution_date)
    TextView textview_resolution_date;
    @BindView(R.id.textview_description)
    TextView textview_description;

    SerializedAssetBO assetBO;

    @Override
    public int getLayoutId() {
        return R.layout.activity_asset_service_request_view;
    }

    @Override
    protected void initVariables() {

        textview_asset_name.setText(assetBO.getAssetName());
        textview_resolution_date.setText(DateTimeUtils.convertFromServerDateToRequestedFormat(assetBO.getNewInstallDate(),ConfigurationMasterHelper.outDateFormat));
        textview_retailer_name.setText(assetBO.getServiceRequestedRetailer());
        textview_type.setText(assetBO.getReasonDesc());
        textview_serial_num.setText(assetBO.getSerialNo());
        textview_description.setText(assetBO.getIssueDescription());

        imageNameList=new ArrayList<>();
        imageNameList.add(assetBO.getImageName());

        adapter=new GridImageViewAdapter(this,imageNameList,FileUtils.photoFolderPath+"/");
        recyclerView.setAdapter(adapter);

    }

    @Override
    public void initializeDi() {

        DaggerAssetServiceRequestComponent.builder().ivyAppComponent(((BusinessModel) Objects.requireNonNull(this).getApplication()).getComponent())
                .assetServiceRequestModule(new AssetServiceRequestModule(this))
                .build()
                .inject(this);

    }


    @Override
    protected void getMessageFromAliens() {
        if (getIntent().getExtras() != null) {
            requestId = getIntent().getExtras().getString("requestId", "");
            isFromReport= getIntent().getExtras().getBoolean("isFromReport", false);
            assetBO = getIntent().getExtras().getParcelable("obj");
        }
    }

    @Override
    protected void setUpViews() {
        Toolbar mToolbar = findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            setScreenTitle(getResources().getString(R.string.service_request_details));
        }

        LinearLayoutManager layout = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL,
                false);
        recyclerView.setLayoutManager(layout);
    }

    @Override
    public void listServiceRequests(ArrayList<SerializedAssetBO> requestList) {

    }

    @Override
    public void showErrorMessage(int type) {

    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int i = item.getItemId();
        if (i == android.R.id.home) {
            finish();
            overridePendingTransition(R.anim.trans_right_in, R.anim.trans_right_out);
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onTakePhoto() {

    }

    @Override
    public void deletePhoto(String fileName, int position) {

    }
}
