package com.ivy.ui.AssetServiceRequest;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.ivy.core.base.view.BaseActivity;
import com.ivy.core.data.app.AppDataProvider;
import com.ivy.core.data.db.AppDataManager;
import com.ivy.cpg.view.asset.assetservicedi.DaggerAssetServiceComponent;
import com.ivy.cpg.view.asset.bo.AssetTrackingBO;
import com.ivy.cpg.view.serializedAsset.SerializedAssetBO;
import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.commons.IvyBaseActivityNoActionBar;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.view.HomeScreenTwo;
import com.ivy.ui.AssetServiceRequest.di.AssetServiceRequestModule;
//import com.ivy.ui.AssetServiceRequest.di.DaggerAssetServiceRequestComponent;
import com.ivy.ui.AssetServiceRequest.di.DaggerAssetServiceRequestComponent;
import com.ivy.ui.notes.di.NotesModule;
import com.ivy.ui.task.TaskConstant;
import com.ivy.utils.AppUtils;

import java.util.ArrayList;
import java.util.Objects;

import javax.inject.Inject;

public class AssetServiceRequestActivity extends BaseActivity implements AssetServiceRequestContractor.AssetServiceListView {

    FloatingActionButton fab_newServiceRequest;
    RecyclerView recyclerView;
    @Inject
    AppDataProvider appDataProvider;
    private boolean isFromReport;

    String screenTitle;

    @Inject
    AssetServiceRequestContractor.Presenter<AssetServiceRequestContractor.AssetServiceView> presenter;

    @Override
    public int getLayoutId() {
        return R.layout.activity_asset_service_request;
    }

    @Override
    protected void setUpViews() {

        Toolbar toolbar =  findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            setScreenTitle(screenTitle);
        }
        overridePendingTransition(R.anim.trans_left_in, R.anim.trans_left_out);

        recyclerView=findViewById(R.id.list_service_requests);
        fab_newServiceRequest=findViewById(R.id.fab);
        fab_newServiceRequest.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent intent=new Intent(AssetServiceRequestActivity.this,NewAssetServiceRequest.class);
                startActivity(intent);

            }
        });

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

    }

    @Override
    protected void onStart() {
        super.onStart();



        presenter.loadServiceRequests(appDataProvider.getRetailMaster().getRetailerID(),isFromReport);
    }

    @Override
    public void listServiceRequests(ArrayList<SerializedAssetBO> requestList) {

        recyclerView.setAdapter(new RecyclerAdapter(requestList));
    }

    @Override
    public void showErrorMessage(int type) {

        if(type==0){
            Toast.makeText(this,getResources().getString(R.string.something_went_wrong),Toast.LENGTH_LONG).show();
        }
        else if(type==1){
            Toast.makeText(this,getResources().getString(R.string.no_data_exists),Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void getMessageFromAliens() {

        if (getIntent().getExtras() != null) {
            screenTitle = getIntent().getExtras().getString(TaskConstant.SCREEN_TITLE, getString(R.string.asset_service_request));
            isFromReport = getIntent().getExtras().getBoolean("isFromReport", false);
        }
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int i = item.getItemId();
        if (i == android.R.id.home) {
            startActivity(new Intent(this,
                    HomeScreenTwo.class));
            finish();
            overridePendingTransition(R.anim.trans_right_in, R.anim.trans_right_out);
        }

        return super.onOptionsItemSelected(item);
    }

    class RecyclerAdapter extends RecyclerView.Adapter<RecyclerAdapter.MyViewHolder> {

        ArrayList<SerializedAssetBO> data;

        RecyclerAdapter(ArrayList<SerializedAssetBO> data) {
            this.data = data;
        }

        @NonNull
        @Override
        public RecyclerAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(
                    parent.getContext()).inflate(R.layout.row_asset_service_request, parent, false);
            return new RecyclerAdapter.MyViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull final RecyclerAdapter.MyViewHolder holder, int position) {
            holder.assetTrackingBO = data.get(position);
            holder.tv_assetName.setText(holder.assetTrackingBO.getAssetName());
            holder.tv_serialNum.setText(holder.assetTrackingBO.getSerialNo());
            holder.tv_status.setText(holder.assetTrackingBO.getAssetServiceReqStatus());
        }

        @Override
        public int getItemCount() {
            return data.size();
        }

        class MyViewHolder extends RecyclerView.ViewHolder {
            TextView tv_assetName,tv_serialNum,tv_status;
            SerializedAssetBO assetTrackingBO;

            MyViewHolder(View itemView) {
                super(itemView);
                tv_assetName = itemView.findViewById(R.id.tv_asset_name);
                tv_serialNum = itemView.findViewById(R.id.tv_serialNo);
                tv_status = itemView.findViewById(R.id.tv_status);

            }
        }
    }
}
