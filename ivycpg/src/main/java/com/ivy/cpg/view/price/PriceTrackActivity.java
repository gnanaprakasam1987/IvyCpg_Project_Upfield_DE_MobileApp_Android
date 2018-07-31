package com.ivy.cpg.view.price;

import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.bo.CompetitorFilterLevelBO;
import com.ivy.sd.png.commons.IvyBaseActivityNoActionBar;
import com.ivy.sd.png.model.BrandDialogInterface;
import com.ivy.sd.png.model.CompetitorFilterInterface;
import com.ivy.sd.png.model.FiveLevelFilterCallBack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Vector;

public class PriceTrackActivity extends IvyBaseActivityNoActionBar implements BrandDialogInterface,CompetitorFilterInterface,FiveLevelFilterCallBack {

    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_price_track);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        overridePendingTransition(R.anim.trans_left_in, R.anim.trans_left_out);
    }

    public void numberPressed(View v) {
        android.support.v4.app.FragmentManager fm = getSupportFragmentManager();
        PriceTrackFragment fragment = (PriceTrackFragment) fm
                .findFragmentById(R.id.price_track_fragment);
        fragment.numberPressed(v);
    }

    @Override
    public void updateBrandText(String mFilterText, int id) {
        android.support.v4.app.FragmentManager fm = getSupportFragmentManager();
        PriceTrackFragment fragment = (PriceTrackFragment) fm
                .findFragmentById(R.id.price_track_fragment);
        fragment.updateBrandText(mFilterText, id);
    }

    @Override
    public void updateGeneralText(String mFilterText) {
        android.support.v4.app.FragmentManager fm = getSupportFragmentManager();
        PriceTrackFragment fragment = (PriceTrackFragment) fm
                .findFragmentById(R.id.price_track_fragment);
        fragment.updateGeneralText(mFilterText);
    }

    @Override
    public void updateCancel() {
        android.support.v4.app.FragmentManager fm = getSupportFragmentManager();
        PriceTrackFragment fragment = (PriceTrackFragment) fm
                .findFragmentById(R.id.price_track_fragment);
        fragment.updateCancel();
    }

    @Override
    public void updateFromFiveLevelFilter(int mProductId, HashMap<Integer, Integer> mSelectedIdByLevelId, ArrayList<Integer> mAttributeProducts, String mFilterText) {
        android.support.v4.app.FragmentManager fm = getSupportFragmentManager();
        PriceTrackFragment fragment = (PriceTrackFragment) fm
                .findFragmentById(R.id.price_track_fragment);
        fragment.updateFromFiveLevelFilter(mProductId, mSelectedIdByLevelId, mAttributeProducts, mFilterText);
    }

    @Override
    public void loadStartVisit() {
        android.support.v4.app.FragmentManager fm = getSupportFragmentManager();
        PriceTrackFragment fragment = (PriceTrackFragment) fm
                .findFragmentById(R.id.price_track_fragment);
        fragment.loadStartVisit();
    }

    @Override
    public void updateCompetitorProducts(Vector<CompetitorFilterLevelBO> parentIdList, HashMap<Integer, Integer> mSelectedIdByLevelId, String filterText) {
        android.support.v4.app.FragmentManager fm = getSupportFragmentManager();
        PriceTrackFragment fragment = (PriceTrackFragment) fm
                .findFragmentById(R.id.price_track_fragment);
        fragment.updateCompetitorProducts(parentIdList,mSelectedIdByLevelId,filterText);
    }
}
