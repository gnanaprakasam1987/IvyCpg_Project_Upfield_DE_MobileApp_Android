package com.ivy.cpg.view.delivery.salesreturn;


import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;

import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.commons.IvyBaseActivityNoActionBar;
import com.ivy.sd.png.view.HomeScreenTwo;

import java.util.List;

public class SalesReturnDeliveryActivity extends IvyBaseActivityNoActionBar {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sales_returndelivery);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setTitle(null);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        // Used to on / off the back arrow icon
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        // Used to remove the app logo actionbar icon and set title as home
        // (title support click)
        // getSupportActionBar().setDisplayShowHomeEnabled(true);
        // Used to hide the app logo icon from actionbar
        getSupportActionBar().setDisplayShowTitleEnabled(true);
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            String title = extras.getString("menuName") == null ? "" : extras.getString("menuName");
            setScreenTitle(title);
        }

        SalesReturnDeliveryFragment salesReturnDeliveryFragment = new SalesReturnDeliveryFragment();

        addFragment(salesReturnDeliveryFragment, false);

    }

    public void addFragment(Fragment fragment, boolean isReplace) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();

        if (isReplace) {
            transaction.replace(R.id.container_salesReturn, fragment, fragment.getClass().toString());
        } else {
            transaction.add(R.id.container_salesReturn, fragment, fragment.getClass().toString());
        }
        transaction.addToBackStack(null);
        overridePendingTransition(R.anim.trans_left_in, R.anim.trans_left_out);
        transaction.commit();
        fragmentManager.executePendingTransactions();
    }

    public void numberPressed(View vw) {

        for (int i = 0; i < getSupportFragmentManager().getBackStackEntryCount(); i++) {
            List<Fragment> fragmentList = getSupportFragmentManager().getFragments();
            ((SalesReturnDeliveryDetailsFragment) fragmentList.get(i)).numberPressed(vw);
            break;
        }


    }

    @Override
    public void onBackPressed() {
        onBackButtonClick();
        super.onBackPressed();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        onBackButtonClick();
        return super.onOptionsItemSelected(item);
    }

    private void onBackButtonClick() {

        FragmentManager fragmentManager = this.getSupportFragmentManager();
        for (int i = 0; i < fragmentManager.getBackStackEntryCount(); i++) {
            if (fragmentManager.getBackStackEntryCount() == 1) {
                startActivity(new Intent(this, HomeScreenTwo.class));
                finish();
                overridePendingTransition(R.anim.trans_right_in, R.anim.trans_right_out);
            } else {
                fragmentManager.popBackStack();
                break;
            }

        }
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.commit();
    }
}
