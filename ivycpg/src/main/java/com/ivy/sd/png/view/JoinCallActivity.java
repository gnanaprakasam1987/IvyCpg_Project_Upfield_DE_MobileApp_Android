package com.ivy.sd.png.view;

import android.os.Bundle;
import android.support.v7.widget.Toolbar;

import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.commons.IvyBaseActivityNoActionBar;
import com.ivy.sd.png.model.BusinessModel;

public class JoinCallActivity extends IvyBaseActivityNoActionBar {

    private Toolbar toolbar;
    private BusinessModel bmodel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_join_call);
        bmodel = (BusinessModel) getApplicationContext();
        bmodel.setContext(this);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            setScreenTitle(bmodel.configurationMasterHelper.getJoincallTitile());
            // Used to remove the app logo actionbar icon and set title as home
            // (title support click)
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

    }
}