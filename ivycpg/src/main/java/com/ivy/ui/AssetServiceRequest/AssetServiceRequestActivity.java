package com.ivy.ui.AssetServiceRequest;

import android.content.Intent;

import androidx.appcompat.widget.Toolbar;

import android.view.MenuItem;

import com.ivy.core.base.view.BaseActivity;
import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.view.HomeScreenTwo;
//import com.ivy.ui.AssetServiceRequest.di.DaggerAssetServiceRequestComponent;
import com.ivy.ui.task.TaskConstant;

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
