package com.ivy.ui.task.view;

import android.os.Bundle;
import android.support.v7.widget.Toolbar;

import com.ivy.core.base.view.BaseActivity;
import com.ivy.sd.png.asean.view.R;

public class TaskActivity extends BaseActivity {

    @Override
    public int getLayoutId() {
        return R.layout.activity_task;
    }

    @Override
    protected void initVariables() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(null);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setElevation(0);
        }

    }


    @Override
    public void initializeDi() {

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void getMessageFromAliens() {

    }

    @Override
    protected void setUpViews() {
        overridePendingTransition(R.anim.trans_left_in, R.anim.trans_left_out);
    }
}
