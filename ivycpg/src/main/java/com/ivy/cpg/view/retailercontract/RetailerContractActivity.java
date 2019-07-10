package com.ivy.cpg.view.retailercontract;

import android.content.Intent;
import android.os.Bundle;
import com.google.android.material.tabs.TabLayout;
import androidx.fragment.app.FragmentTransaction;
import androidx.appcompat.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.commons.IvyBaseActivityNoActionBar;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.view.HomeScreenTwo;
import com.ivy.utils.DateTimeUtils;
import com.ivy.utils.FontUtils;

/**
 * Created by chiranjeevulu.l on 18-04-2016.
 */
public class RetailerContractActivity extends IvyBaseActivityNoActionBar {
    private BusinessModel bmodel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_retailer_contract);

        Toolbar toolbar =  findViewById(R.id.toolbar);

        bmodel = (BusinessModel) getApplicationContext();
        bmodel.setContext(this);
        overridePendingTransition(R.anim.trans_left_in, R.anim.trans_left_out);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            getSupportActionBar().setIcon(R.drawable.icon_order);
            // Used to on / off the back arrow icon
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            // Used to remove the app logo actionbar icon and set title as home
            // (title support click)
            getSupportActionBar().setDisplayShowHomeEnabled(true);

            getSupportActionBar().setTitle(getIntent().getStringExtra("screentitle"));
        }

        TabLayout tabLayout =  findViewById(R.id.tab_layout);
        tabLayout.addTab(tabLayout.newTab().setText(getResources().getString(R.string.text_existing_contract)));
        tabLayout.addTab(tabLayout.newTab().setText(getResources().getString(R.string.text_renewed_contract)));
        tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);

        //custom font tab text
        ViewGroup vg = (ViewGroup) tabLayout.getChildAt(0);
        int tabsCount = vg.getChildCount();
        for (int j = 0; j < tabsCount; j++) {
            ViewGroup vgTab = (ViewGroup) vg.getChildAt(j);
            int tabChildsCount = vgTab.getChildCount();
            for (int i = 0; i < tabChildsCount; i++) {
                View tabViewChild = vgTab.getChildAt(i);
                if (tabViewChild instanceof TextView) {
                    ((TextView) tabViewChild).setTypeface(FontUtils.getFontRoboto(this, FontUtils.FontType.MEDIUM));
                }
            }
        }

        if (bmodel.userMasterHelper.getUserMasterBO().getUserid() == 0) {
            Toast.makeText(this,
                    getResources().getString(R.string.sessionout_loginagain),
                    Toast.LENGTH_SHORT).show();
            finish();
        }
        FragmentTransaction transaction = getSupportFragmentManager()
                .beginTransaction();
        transaction.replace(R.id.fragment_content, new RetailerContractFragment());
        transaction.addToBackStack(null);
        transaction.commit();

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                FragmentTransaction transaction = getSupportFragmentManager()
                        .beginTransaction();
                if (tab.getPosition() == 0) {
                    transaction.replace(R.id.fragment_content, new RetailerContractFragment());
                    transaction.addToBackStack(null);
                    transaction.commit();
                } else {
                    transaction.replace(R.id.fragment_content, new RenewContractFragment());
                    transaction.addToBackStack(null);
                    transaction.commit();
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_target_plan, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        menu.findItem(R.id.menu_skutgt).setVisible(false);
        menu.findItem(R.id.menu_save).setVisible(false);
        menu.findItem(R.id.menu_filter).setVisible(false);
        menu.findItem(R.id.menu_next).setVisible(false);
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBack();
                return true;

        }
        return super.onOptionsItemSelected(item);
    }


    public void onBack() {
        bmodel.outletTimeStampHelper.updateTimeStampModuleWise(DateTimeUtils
                .now(DateTimeUtils.TIME));

        Intent myIntent = new Intent(this, HomeScreenTwo.class);
        startActivityForResult(myIntent, 0);
        finish();
        overridePendingTransition(R.anim.trans_right_in, R.anim.trans_right_out);
    }
}