package com.ivy.sd.png.view;

import android.os.Bundle;
import android.support.v7.widget.AppCompatEditText;
import android.support.v7.widget.Toolbar;

import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.commons.IvyBaseActivityNoActionBar;
import com.ivy.sd.png.model.BusinessModel;

public class NewOutlet extends IvyBaseActivityNoActionBar {

    private Toolbar toolbar;
    private Bundle instate;
    private BusinessModel bmodel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_profile);

        toolbar = (Toolbar)findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (getSupportActionBar()!=null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            // Used to remove the app logo actionbar icon and set title as home
            // (title support click)
            getSupportActionBar().setTitle(null);
            getSupportActionBar().setDisplayShowTitleEnabled(false);

            try {
                if (bmodel.labelsMasterHelper
                        .applyLabels("new_retailer") != null)
                    setScreenTitle(bmodel.labelsMasterHelper
                            .applyLabels("new_retailer"));
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
        NewOutletFragment newOutletFragment=(NewOutletFragment) getSupportFragmentManager().findFragmentById(R.id.newoutlet_fragment);

        newOutletFragment.onViewStateRestored(instate);

    }

    protected void passData(AppCompatEditText editText[],Bundle instate) {
        bmodel.newOutletHelper.setEditText(editText);
        this.instate=instate;
    }
}
