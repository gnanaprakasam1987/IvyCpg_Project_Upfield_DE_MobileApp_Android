package com.ivy.cpg.primarysale.view;

import android.os.Bundle;
import androidx.appcompat.widget.Toolbar;
import android.widget.Toast;

import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.commons.IvyBaseActivityNoActionBar;
import com.ivy.sd.png.model.BusinessModel;

/**
 * Created by dharmapriya.k on 22-09-2015.
 */
public class InvoiceStatusActivity extends IvyBaseActivityNoActionBar {
    private BusinessModel bmodel;
    private Toolbar toolbar;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_invoice_status);
        bmodel = (BusinessModel) getApplicationContext();
        bmodel.setContext(this);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null)
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            setScreenTitle(bmodel.mSelectedActivityName);
        getSupportActionBar().setIcon(null);
        getSupportActionBar().setDisplayUseLogoEnabled(false);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        bmodel = (BusinessModel) this.getApplicationContext();
        bmodel.setContext(this);
        if (bmodel.userMasterHelper.getUserMasterBO().getUserid() == 0) {
            Toast.makeText(this,
                    getResources().getString(R.string.sessionout_loginagain),
                    Toast.LENGTH_SHORT).show();
            finish();
        }
    }


}
