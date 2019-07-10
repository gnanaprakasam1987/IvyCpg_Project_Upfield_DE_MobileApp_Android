package com.ivy.cpg.view.delivery.salesreturn;


import android.content.Intent;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.appcompat.widget.Toolbar;
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

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(null);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            // Used to on / off the back arrow icon
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            // Used to remove the app logo actionbar icon and set title as home
            // (title support click)
            // getSupportActionBar().setDisplayShowHomeEnabled(true);
            // Used to hide the app logo icon from actionbar
            getSupportActionBar().setDisplayShowTitleEnabled(true);
        }
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            String title = extras.getString("menuName") == null ? "" : extras.getString("menuName");
            setScreenTitle(title);
        }

        SalesReturnDeliveryFragment salesReturnDeliveryFragment = new SalesReturnDeliveryFragment();

        addFragment(salesReturnDeliveryFragment);

    }

    public void addFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();

        transaction.add(R.id.container_salesReturn, fragment, fragment.getClass().toString());
        transaction.addToBackStack(null);
        overridePendingTransition(R.anim.trans_left_in, R.anim.trans_left_out);
        transaction.commit();
        fragmentManager.executePendingTransactions();
    }

    public void numberPressed(View vw) {

        List<Fragment> fragmentList = getSupportFragmentManager().getFragments();
        if (!fragmentList.isEmpty())
            ((SalesReturnDeliveryDetailsFragment) fragmentList.get(0)).numberPressed(vw);


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
