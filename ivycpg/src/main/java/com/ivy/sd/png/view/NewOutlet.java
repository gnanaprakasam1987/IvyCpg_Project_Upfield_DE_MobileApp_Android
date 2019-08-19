package com.ivy.sd.png.view;

import android.os.Bundle;

import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.appcompat.widget.Toolbar;
import android.view.MenuItem;

import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.commons.IvyBaseActivityNoActionBar;
import com.ivy.sd.png.model.BusinessModel;

public class NewOutlet extends IvyBaseActivityNoActionBar {

    private Toolbar toolbar;
    private Bundle instate;
    private BusinessModel bmodel;

    private int channelid = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_profile);

        toolbar = (Toolbar)findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        init();

        getSupportFragmentManager().findFragmentById(R.id.newoutlet_fragment);
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        NewoutletContainerFragment containerFragment = new NewoutletContainerFragment();

        Bundle bundleNewoutLet=new Bundle();
        bundleNewoutLet.putInt("channelid",channelid);
        if (getIntent().getStringExtra("retailerId") != null)
            bundleNewoutLet.putString("retailerId",getIntent().getStringExtra("retailerId"));
        containerFragment.setArguments(bundleNewoutLet);

        fragmentTransaction.replace(R.id.newoutlet_fragment,containerFragment);
        fragmentTransaction.commit();

    }


    private void init(){

        channelid = this.getIntent().getIntExtra("channelid",0) ;

        if (getSupportActionBar()!=null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            // Used to remove the app logo actionbar icon and set title as home
            // (title support click)
            getSupportActionBar().setTitle(null);
            getSupportActionBar().setDisplayShowTitleEnabled(false);

            try {
                if (bmodel.labelsMasterHelper.applyLabels("new_retailer") != null)
                    setScreenTitle(bmodel.labelsMasterHelper.applyLabels("new_retailer"));
                else
                    setScreenTitle(getResources().getString(R.string.new_retailer));
            } catch (Exception e) {
                setScreenTitle(getResources().getString(R.string.new_retailer));
            }
        }
        bmodel = (BusinessModel) getApplicationContext();
        bmodel.setContext(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        /*ProfileContainerFragment newOutletFragment=(ProfileContainerFragment) getSupportFragmentManager().findFragmentById(R.id.newoutlet_fragment);
        newOutletFragment.onViewStateRestored(instate);*/
    }

    protected void passData(AppCompatEditText editText[],Bundle instate) {
        bmodel.newOutletHelper.setEditText(editText);
        this.instate=instate;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
        }
        return super.onOptionsItemSelected(item);
    }
}
