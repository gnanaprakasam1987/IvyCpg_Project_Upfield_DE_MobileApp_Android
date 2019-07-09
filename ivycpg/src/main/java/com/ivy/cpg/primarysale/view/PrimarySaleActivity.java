package com.ivy.cpg.primarysale.view;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import androidx.core.content.ContextCompat;
import androidx.appcompat.widget.Toolbar;

import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.commons.IvyBaseActivityNoActionBar;
import com.ivy.sd.png.model.BusinessModel;

public class PrimarySaleActivity extends IvyBaseActivityNoActionBar {

    private BusinessModel bmodel;
    private Toolbar toolbar;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stockist_order);
        bmodel = (BusinessModel) getApplicationContext();
        bmodel.setContext(this);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            setScreenTitle(bmodel.configurationMasterHelper.getPrimarysaleTitle());
            getSupportActionBar().setIcon(null);
            getSupportActionBar().setDisplayUseLogoEnabled(false);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        if (bmodel.configurationMasterHelper.SHOW_CAPTURED_LOCATION) {
            checkAndRequestPermissionAtRunTime(3);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (bmodel.configurationMasterHelper.SHOW_CAPTURED_LOCATION) {
            int permissionStatus = ContextCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_FINE_LOCATION);
            if (permissionStatus == PackageManager.PERMISSION_GRANTED) {
                bmodel.locationUtil.startLocationListener();
            }
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (bmodel.configurationMasterHelper.SHOW_CAPTURED_LOCATION) {
            int permissionStatus = ContextCompat.checkSelfPermission(this,
                    Manifest.permission.ACCESS_FINE_LOCATION);
            if (permissionStatus == PackageManager.PERMISSION_GRANTED)
                bmodel.locationUtil.stopLocationListener();
        }
    }

}
