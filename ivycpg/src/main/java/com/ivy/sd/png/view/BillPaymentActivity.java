package com.ivy.sd.png.view;

import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.commons.IvyBaseActivityNoActionBar;
import com.ivy.sd.png.model.BusinessModel;

/**
 * Created by nagaganesh.n on 19-04-2017.
 */
public class BillPaymentActivity extends IvyBaseActivityNoActionBar implements ReceiptNoDialogFragment.UpdateReceiptNoInterface {

    private Toolbar toolbar;
    private BusinessModel bmodel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bill_payment);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }
        bmodel = (BusinessModel) getApplicationContext();
        bmodel.setContext(this);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_bill_payment, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();

        /*BillPaymentActivityFragment bPAFragment = new BillPaymentActivityFragment();

        boolean isFromCollection = getIntent().getExtras() != null && getIntent().getExtras().getBoolean("FromCollection", false);
        Bundle bundle = new Bundle();
        bundle.putBoolean("FromCollection",isFromCollection);
        bPAFragment.setArguments(bundle);*/

        ft.replace(R.id.fragment, new BillPaymentActivityFragment());
        ft.commit();

        super.onResume();
    }

    @Override
    public void updateReceiptNo(String receiptno) {
        bmodel.collectionHelper.receiptno = receiptno;
    }
}
