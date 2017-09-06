package com.ivy.sd.png.view;


import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;

import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.bo.LevelBO;
import com.ivy.sd.png.commons.IvyBaseActivityNoActionBar;
import com.ivy.sd.png.model.BrandDialogInterface;
import com.ivy.sd.png.util.Commons;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;

public class NearExpiryTrackingActivity extends IvyBaseActivityNoActionBar implements
        BrandDialogInterface {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_nearexpiry_tracking);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        overridePendingTransition(R.anim.trans_left_in, R.anim.trans_left_out);
    }

    public void numberPressed(View vw) {
        android.support.v4.app.FragmentManager fm = getSupportFragmentManager();
        NearExpiryTrackingFragment fragment = (NearExpiryTrackingFragment) fm
                .findFragmentById(R.id.nearexpiry_tracking_fragment);
        fragment.getDialog().numberPressed(vw);
    }

    @Override
    public void updatebrandtext(String filtertext, int id) {
        android.support.v4.app.FragmentManager fm = getSupportFragmentManager();
        NearExpiryTrackingFragment fragment = (NearExpiryTrackingFragment) fm
                .findFragmentById(R.id.nearexpiry_tracking_fragment);
        fragment.updatebrandtext(filtertext, id);
    }

    @Override
    public void updategeneraltext(String filtertext) {
        android.support.v4.app.FragmentManager fm = getSupportFragmentManager();
        NearExpiryTrackingFragment fragment = (NearExpiryTrackingFragment) fm
                .findFragmentById(R.id.nearexpiry_tracking_fragment);
        fragment.updategeneraltext(filtertext);
    }

    @Override
    public void updateCancel() {
        android.support.v4.app.FragmentManager fm = getSupportFragmentManager();
        NearExpiryTrackingFragment fragment = (NearExpiryTrackingFragment) fm
                .findFragmentById(R.id.nearexpiry_tracking_fragment);
        fragment.updateCancel();

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

    @Override
    public void updateMultiSelectionBrand(List<String> filtername,
                                          List<Integer> filterid) {
        android.support.v4.app.FragmentManager fm = getSupportFragmentManager();
        NearExpiryTrackingFragment fragment = (NearExpiryTrackingFragment) fm
                .findFragmentById(R.id.nearexpiry_tracking_fragment);
        fragment.updateMultiSelectionBrand(filtername, filterid);
    }

    @Override
    public void updateMultiSelectionCatogry(List<Integer> mcatgory) {
        android.support.v4.app.FragmentManager fm = getSupportFragmentManager();
        NearExpiryTrackingFragment fragment = (NearExpiryTrackingFragment) fm
                .findFragmentById(R.id.nearexpiry_tracking_fragment);
        fragment.updateMultiSelectionCatogry(mcatgory);
    }

    @Override
    public void loadStartVisit() {
        android.support.v4.app.FragmentManager fm = getSupportFragmentManager();
        NearExpiryTrackingFragment fragment = (NearExpiryTrackingFragment) fm
                .findFragmentById(R.id.nearexpiry_tracking_fragment);
        fragment.loadStartVisit();
    }

    @Override
    public void updatefromFiveLevelFilter(Vector<LevelBO> parentidList) {
        android.support.v4.app.FragmentManager fm = getSupportFragmentManager();
        NearExpiryTrackingFragment fragment = (NearExpiryTrackingFragment) fm
                .findFragmentById(R.id.nearexpiry_tracking_fragment);
        fragment.updatefromFiveLevelFilter(parentidList);
    }

    @Override
    public void updatefromFiveLevelFilter(Vector<LevelBO> parentidList, HashMap<Integer, Integer> mSelectedIdByLevelId, ArrayList<Integer> mAttributeProducts, String filtertext) {
        android.support.v4.app.FragmentManager fm = getSupportFragmentManager();
        NearExpiryTrackingFragment fragment = (NearExpiryTrackingFragment) fm
                .findFragmentById(R.id.nearexpiry_tracking_fragment);
        fragment.updatefromFiveLevelFilter(parentidList, mSelectedIdByLevelId, mAttributeProducts, filtertext);
    }
}
