package com.ivy.ui.reports.dynamicreport.view;

import android.os.Bundle;
import android.support.v7.widget.Toolbar;

import com.ivy.core.base.presenter.BaseIvyPresenter;
import com.ivy.core.base.presenter.BasePresenter;
import com.ivy.core.base.view.BaseActivity;
import com.ivy.core.base.view.BaseIvyView;
import com.ivy.cpg.view.basedi.BaseModule;
import com.ivy.cpg.view.basedi.DaggerBaseComponent;
import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.model.BusinessModel;

import javax.inject.Inject;

public class DynamicReportActivity extends BaseActivity implements BaseIvyView {

    private String screenTitle;

    @Inject
    BaseIvyPresenter<BaseIvyView> viewBasePresenter;

    @Override
    public int getLayoutId() {
        return R.layout.dynamic_report_activity_new;
    }

    @Override
    protected void initVariables() {

    }

    @Override
    public void initializeDi() {
        DaggerBaseComponent.builder()
                .baseModule(new BaseModule(this))
                .ivyAppComponent(((BusinessModel) getApplication()).getComponent())
                .build()
                .inject(this);
        setBasePresenter((BasePresenter) viewBasePresenter);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(null);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
    }


    @Override
    protected void getMessageFromAliens() {
        if (getIntent() != null) {
            screenTitle = getIntent().getStringExtra("screentitle");
        }
    }

    @Override
    protected void setUpViews() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            setScreenTitle(screenTitle);
        }
        overridePendingTransition(R.anim.trans_left_in, R.anim.trans_left_out);
    }
}
