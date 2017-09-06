package com.ivy.sd.png.view;

import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.bo.LevelBO;
import com.ivy.sd.png.commons.IvyBaseActivityNoActionBar;
import com.ivy.sd.png.model.BrandDialogInterface;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;

public class PriceTrackActivity extends IvyBaseActivityNoActionBar implements BrandDialogInterface {

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
    public void updateMultiSelectionBrand(List<String> filtername,
                                          List<Integer> filterid) {
        android.support.v4.app.FragmentManager fm = getSupportFragmentManager();
        PriceTrackFragment fragment = (PriceTrackFragment) fm
                .findFragmentById(R.id.price_track_fragment);
        fragment.updateMultiSelectionBrand(filtername, filterid);

    }

    @Override
    public void updateMultiSelectionCatogry(List<Integer> mcatgory) {
        android.support.v4.app.FragmentManager fm = getSupportFragmentManager();
        PriceTrackFragment fragment = (PriceTrackFragment) fm
                .findFragmentById(R.id.price_track_fragment);
        fragment.updateMultiSelectionCatogry(mcatgory);
    }

    @Override
    public void updatebrandtext(String filtertext, int id) {
        android.support.v4.app.FragmentManager fm = getSupportFragmentManager();
        PriceTrackFragment fragment = (PriceTrackFragment) fm
                .findFragmentById(R.id.price_track_fragment);
        fragment.updatebrandtext(filtertext, id);
    }

    @Override
    public void updategeneraltext(String filtertext) {
        android.support.v4.app.FragmentManager fm = getSupportFragmentManager();
        PriceTrackFragment fragment = (PriceTrackFragment) fm
                .findFragmentById(R.id.price_track_fragment);
        fragment.updategeneraltext(filtertext);
    }

    @Override
    public void updateCancel() {
        android.support.v4.app.FragmentManager fm = getSupportFragmentManager();
        PriceTrackFragment fragment = (PriceTrackFragment) fm
                .findFragmentById(R.id.price_track_fragment);
        fragment.updateCancel();
    }

    @Override
    public void loadStartVisit() {
        android.support.v4.app.FragmentManager fm = getSupportFragmentManager();
        PriceTrackFragment fragment = (PriceTrackFragment) fm
                .findFragmentById(R.id.price_track_fragment);
        fragment.loadStartVisit();
    }

    @Override
    public void updatefromFiveLevelFilter(Vector<LevelBO> parentidList) {
        android.support.v4.app.FragmentManager fm = getSupportFragmentManager();
        PriceTrackFragment fragment = (PriceTrackFragment) fm
                .findFragmentById(R.id.price_track_fragment);
        fragment.updatefromFiveLevelFilter(parentidList);
    }

    @Override
    public void updatefromFiveLevelFilter(Vector<LevelBO> parentidList, HashMap<Integer, Integer> mSelectedIdByLevelId, ArrayList<Integer> mAttributeProducts, String filtertext) {
        android.support.v4.app.FragmentManager fm = getSupportFragmentManager();
        PriceTrackFragment fragment = (PriceTrackFragment) fm
                .findFragmentById(R.id.price_track_fragment);
        fragment.updatefromFiveLevelFilter(parentidList, mSelectedIdByLevelId, mAttributeProducts, filtertext);
    }
}
