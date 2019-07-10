package com.ivy.ui.notes.view;

import android.os.Bundle;
import androidx.appcompat.widget.Toolbar;

import com.ivy.core.base.presenter.BaseIvyPresenter;
import com.ivy.core.base.view.BaseActivity;
import com.ivy.core.base.view.BaseIvyView;
import com.ivy.cpg.view.basedi.BaseModule;
import com.ivy.cpg.view.basedi.DaggerBaseComponent;
import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.ui.notes.NoteConstant;

import javax.inject.Inject;

public class NotesActivity extends BaseActivity implements BaseIvyView {

    @Inject
    BaseIvyPresenter<BaseIvyView> viewBasePresenter;


    @Override
    public int getLayoutId() {
        return R.layout.activity_notes;
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

    }

    @Override
    protected void setUpViews() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            setScreenTitle(getIntent().getExtras().getString(NoteConstant.SCREEN_TITLE, getString(R.string.note_label)));
        }
        overridePendingTransition(R.anim.trans_left_in, R.anim.trans_left_out);
    }
}
