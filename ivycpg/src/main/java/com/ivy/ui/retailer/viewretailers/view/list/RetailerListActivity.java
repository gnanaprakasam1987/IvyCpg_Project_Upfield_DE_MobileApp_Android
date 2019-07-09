package com.ivy.ui.retailer.viewretailers.view.list;

import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;

import com.ivy.core.base.view.BaseActivity;
import com.ivy.sd.png.asean.view.R;

/**
 * Created by mansoor on 09/05/2019
 */
public class RetailerListActivity extends BaseActivity {

    @Override
    public int getLayoutId() {
        return R.layout.activity_retailer_list;
    }

    @Override
    protected void initVariables() {

    }

    @Override
    public void initializeDi() {

    }

    @Override
    protected void getMessageFromAliens() {

    }

    @Override
    protected void setUpViews() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            setScreenTitle(getString(R.string.add_plan));
        }
        overridePendingTransition(R.anim.trans_left_in, R.anim.trans_left_out);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }
}
