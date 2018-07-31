package com.ivy.cpg.view.planogram;

import android.os.Bundle;
import android.support.v7.widget.Toolbar;

import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.bo.LevelBO;
import com.ivy.sd.png.commons.IvyBaseActivityNoActionBar;
import com.ivy.sd.png.model.BrandDialogInterface;
import com.ivy.sd.png.model.FiveLevelFilterCallBack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;

public class PlanoGramActivity extends IvyBaseActivityNoActionBar implements
        BrandDialogInterface,FiveLevelFilterCallBack {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_planogram);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        overridePendingTransition(R.anim.trans_left_in, R.anim.trans_left_out);
    }

    @Override
    public void onBackPressed() {
    }

    @Override
    public void updateBrandText(String mFilterText, int id) {
        android.support.v4.app.FragmentManager fm = getSupportFragmentManager();
        PlanoGramFragment asf = (PlanoGramFragment) fm
                .findFragmentById(R.id.planogram_fragment);
        asf.updateBrandText(mFilterText, id);

    }

    @Override
    public void updateGeneralText(String mFilterText) {
        android.support.v4.app.FragmentManager fm = getSupportFragmentManager();
        PlanoGramFragment asf = (PlanoGramFragment) fm
                .findFragmentById(R.id.planogram_fragment);
        asf.updateGeneralText(mFilterText);
    }

    @Override
    public void updateCancel() {
        android.support.v4.app.FragmentManager fm = getSupportFragmentManager();
        PlanoGramFragment asf = (PlanoGramFragment) fm
                .findFragmentById(R.id.planogram_fragment);
        asf.updateCancel();

    }

    @Override
    public void updateFromFiveLevelFilter(int mProductId, HashMap<Integer, Integer> mSelectedIdByLevelId, ArrayList<Integer> mAttributeProducts, String mFilterText) {
        android.support.v4.app.FragmentManager fm = getSupportFragmentManager();
        PlanoGramFragment asf = (PlanoGramFragment) fm
                .findFragmentById(R.id.planogram_fragment);
        asf.updateFromFiveLevelFilter(mProductId,mSelectedIdByLevelId,mAttributeProducts, mFilterText);
    }

    @Override
    public void updateMultiSelectionBrand(List<String> mFilterName,
                                          List<Integer> mFilterId) {

    }

    @Override
    public void updateMultiSelectionCategory(List<Integer> mCategory) {

    }

    @Override
    public void loadStartVisit() {

    }
}
