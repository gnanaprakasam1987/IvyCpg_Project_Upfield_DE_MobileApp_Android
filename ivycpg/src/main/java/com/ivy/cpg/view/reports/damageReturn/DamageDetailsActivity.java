package com.ivy.cpg.view.reports.damageReturn;

import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.appcompat.widget.Toolbar;
import android.view.MenuItem;

import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.commons.IvyBaseActivityNoActionBar;

/**
 * Created by nagaganesh.n on 4/27/2017.
 */

public class DamageDetailsActivity extends IvyBaseActivityNoActionBar {

    private Toolbar toolbar;
    String invoiceNo;
    String status;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_edit);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setTitle(null);
            setScreenTitle(getResources().getString(R.string.details));
        }
        if (getIntent().getExtras() != null) {
            invoiceNo = getIntent().getExtras().getString("InvoiceNo");
            status = getIntent().getExtras().getString("status");
        }
        Bundle bndl = new Bundle();
        bndl.putString("invoiceNo", invoiceNo);
        bndl.putString("status", status);
        Fragment fragment = new DamageReturnDetail();
        fragment.setArguments(bndl);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_frame, fragment, fragment.getClass().getSimpleName())
                .addToBackStack(null)
                .commit();

    }

    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    public void updateCancel() {
        setResult(2);
        finish();
    }
    @Override
    public void onBackPressed() {}


}
