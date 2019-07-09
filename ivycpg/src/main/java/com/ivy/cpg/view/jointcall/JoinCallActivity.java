package com.ivy.cpg.view.jointcall;

import android.os.Bundle;
import androidx.appcompat.widget.Toolbar;

import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.commons.IvyBaseActivityNoActionBar;
import com.ivy.sd.png.model.BusinessModel;

public class JoinCallActivity extends IvyBaseActivityNoActionBar {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_join_call);
        BusinessModel bmodel = (BusinessModel) getApplicationContext();
        bmodel.setContext(this);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            setScreenTitle(bmodel.configurationMasterHelper.getJoincallTitile());
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

    }
}