package com.ivy.ui.AssetServiceRequest;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.ivy.core.base.view.BaseActivity;
import com.ivy.core.data.app.AppDataProvider;
import com.ivy.cpg.view.serializedAsset.SerializedAssetBO;
import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.util.CommonDialog;
import com.ivy.sd.png.view.HomeScreenTwo;
import com.ivy.ui.AssetServiceRequest.di.AssetServiceRequestModule;
//import com.ivy.ui.AssetServiceRequest.di.DaggerAssetServiceRequestComponent;
import com.ivy.ui.AssetServiceRequest.di.DaggerAssetServiceRequestComponent;
import com.ivy.ui.task.TaskConstant;
import com.ivy.ui.task.view.SwipeRevealLayout;
import com.ivy.ui.task.view.ViewBinderHelper;
import com.ivy.utils.StringUtils;

import java.util.ArrayList;
import java.util.Objects;

import javax.inject.Inject;

public class AssetServiceRequestActivity extends BaseActivity  {



    String screenTitle;


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

    }

    @Override
    public void initializeDi() {




    }

    @Override
    protected void initVariables() {

    }

    @Override
    protected void onStart() {
        super.onStart();




    }

    @Override
    protected void onResume() {
        super.onResume();
    }





    @Override
    protected void getMessageFromAliens() {

        if (getIntent().getExtras() != null) {
            screenTitle = getIntent().getExtras().getString(TaskConstant.SCREEN_TITLE, getString(R.string.asset_service_request));
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




}
