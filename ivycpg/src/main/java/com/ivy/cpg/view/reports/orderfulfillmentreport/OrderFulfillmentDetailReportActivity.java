package com.ivy.cpg.view.reports.orderfulfillmentreport;

import android.os.Build;
import android.os.Bundle;
import androidx.appcompat.widget.Toolbar;
import android.widget.Toast;

import androidx.fragment.app.FragmentManager;

import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.commons.IvyBaseActivityNoActionBar;
import com.ivy.sd.png.model.BusinessModel;

public class OrderFulfillmentDetailReportActivity extends IvyBaseActivityNoActionBar {

    private Toolbar toolbar;
    private BusinessModel bmodel;
    private OrderFulfillmentDetailReport fragmentObject;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_seller_dashboard);

        bmodel = (BusinessModel) this.getApplicationContext();
        bmodel.setContext(this);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(null);
        getSupportActionBar().setIcon(null);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        // Used to remove the app logo actionbar icon and set title as home
        // (title support click)
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getSupportActionBar().setElevation(0);
        }

        if (bmodel.userMasterHelper.getUserMasterBO().getUserid() == 0) {
            Toast.makeText(this,
                    getResources().getString(R.string.sessionout_loginagain),
                    Toast.LENGTH_SHORT).show();
            finish();
        }

        Bundle bundle = new Bundle();
        bundle.putString("retid", getIntent().getStringExtra("retid"));//retailer id is passed to load Retailer Dashboard by re-using DashboardFragment
        bundle.putString("orderid", getIntent().getStringExtra("orderid"));
        bundle.putString("date", getIntent().getStringExtra("date"));
        fragmentObject = new OrderFulfillmentDetailReport();
        fragmentObject.setArguments(bundle);
        if (fragmentObject != null) {
            FragmentManager fm = getSupportFragmentManager();
            fm.beginTransaction().add(R.id.content_fragemnt, fragmentObject).commit();
        }

    }
}
