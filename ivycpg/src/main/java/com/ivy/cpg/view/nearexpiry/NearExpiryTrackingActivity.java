package com.ivy.cpg.view.nearexpiry;


import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;

import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.commons.IvyBaseActivityNoActionBar;
import com.ivy.sd.png.model.BrandDialogInterface;
import com.ivy.sd.png.model.FiveLevelFilterCallBack;
import com.ivy.sd.png.util.Commons;

import java.util.ArrayList;
import java.util.HashMap;

public class NearExpiryTrackingActivity extends IvyBaseActivityNoActionBar implements
        BrandDialogInterface, FiveLevelFilterCallBack {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_nearexpiry_tracking);
        overridePendingTransition(R.anim.trans_left_in, R.anim.trans_left_out);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
    }

    public void numberPressed(View vw) {
        android.support.v4.app.FragmentManager fm = getSupportFragmentManager();
        NearExpiryTrackingFragment fragment = (NearExpiryTrackingFragment) fm
                .findFragmentById(R.id.nearexpiry_tracking_fragment);
        fragment.getDialog().numberPressed(vw);
    }

    @Override
    public void updateBrandText(String mFilterText, int id) {
        android.support.v4.app.FragmentManager fm = getSupportFragmentManager();
        NearExpiryTrackingFragment fragment = (NearExpiryTrackingFragment) fm
                .findFragmentById(R.id.nearexpiry_tracking_fragment);
        fragment.updateBrandText(mFilterText, id);
    }

    @Override
    public void updateGeneralText(String mFilterText) {
        android.support.v4.app.FragmentManager fm = getSupportFragmentManager();
        NearExpiryTrackingFragment fragment = (NearExpiryTrackingFragment) fm
                .findFragmentById(R.id.nearexpiry_tracking_fragment);
        fragment.updateGeneralText(mFilterText);
    }

    @Override
    public void updateCancel() {
        android.support.v4.app.FragmentManager fm = getSupportFragmentManager();
        NearExpiryTrackingFragment fragment = (NearExpiryTrackingFragment) fm
                .findFragmentById(R.id.nearexpiry_tracking_fragment);
        fragment.updateCancel();

    }

    @Override
    public void updateFromFiveLevelFilter(int mProductId, HashMap<Integer, Integer> mSelectedIdByLevelId, ArrayList<Integer> mAttributeProducts, String mFilterText) {
        android.support.v4.app.FragmentManager fm = getSupportFragmentManager();
        NearExpiryTrackingFragment fragment = (NearExpiryTrackingFragment) fm
                .findFragmentById(R.id.nearexpiry_tracking_fragment);
        fragment.updateFromFiveLevelFilter(mProductId, mSelectedIdByLevelId, mAttributeProducts, mFilterText);
    }

    @Override
    public void onBackPressed() {
        // TODO Auto-generated method stub
    }

    protected void onDestroy() {
        super.onDestroy();
        unbindDrawables(findViewById(R.id.root));
    }

    private void unbindDrawables(View view) {
        if (view.getBackground() != null) {
            view.getBackground().setCallback(null);
        }
        if (view instanceof ViewGroup) {
            for (int i = 0; i < ((ViewGroup) view).getChildCount(); i++) {
                unbindDrawables(((ViewGroup) view).getChildAt(i));
            }
            try {
                if (!(view instanceof AdapterView<?>))
                    ((ViewGroup) view).removeAllViews();
            } catch (Exception e) {
                Commons.printException(e);
            }
        }
    }

}
