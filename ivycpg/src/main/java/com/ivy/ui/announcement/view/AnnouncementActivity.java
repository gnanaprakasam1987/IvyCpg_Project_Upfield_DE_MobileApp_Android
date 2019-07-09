package com.ivy.ui.announcement.view;

import android.os.Bundle;

import androidx.appcompat.widget.Toolbar;

import com.ivy.core.base.presenter.BaseIvyPresenter;
import com.ivy.core.base.view.BaseActivity;
import com.ivy.core.base.view.BaseIvyView;
import com.ivy.cpg.view.basedi.BaseModule;
import com.ivy.cpg.view.basedi.DaggerBaseComponent;
import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.ui.announcement.AnnouncementConstant;

import javax.inject.Inject;

public class AnnouncementActivity extends BaseActivity {

    @Inject
    BaseIvyPresenter<BaseIvyView> viewBasePresenter;

    @Override
    public int getLayoutId() {
        return R.layout.activity_announcement;
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
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            setScreenTitle(getIntent().getExtras().getString(AnnouncementConstant.SCREEN_TITLE, getString(R.string.note_label)));
        }
        overridePendingTransition(R.anim.trans_left_in, R.anim.trans_left_out);
    }
}
