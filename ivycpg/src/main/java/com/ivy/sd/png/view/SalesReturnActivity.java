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

public class SalesReturnActivity extends IvyBaseActivityNoActionBar implements BrandDialogInterface {

    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_sales_return);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        overridePendingTransition(R.anim.trans_left_in, R.anim.trans_left_out);
    }

   

    @Override
    public void updateMultiSelectionBrand(List<String> mFilterName,
                                          List<Integer> mFilterId) {
        android.support.v4.app.FragmentManager fm = getSupportFragmentManager();
        SalesReturnFragment fragment = (SalesReturnFragment) fm
                .findFragmentById(R.id.sales_return_fragment);
        fragment.updateMultiSelectionBrand(mFilterName, mFilterId);

    }

    @Override
    public void updateMultiSelectionCategory(List<Integer> mCategory) {
        android.support.v4.app.FragmentManager fm = getSupportFragmentManager();
        SalesReturnFragment fragment = (SalesReturnFragment) fm
                .findFragmentById(R.id.sales_return_fragment);
        fragment.updateMultiSelectionCategory(mCategory);
    }

    @Override
    public void updateBrandText(String mFilterText, int id) {
        android.support.v4.app.FragmentManager fm = getSupportFragmentManager();
        SalesReturnFragment fragment = (SalesReturnFragment) fm
                .findFragmentById(R.id.sales_return_fragment);
        fragment.updateBrandText(mFilterText, id);
    }

    @Override
    public void updateGeneralText(String mFilterText) {
        android.support.v4.app.FragmentManager fm = getSupportFragmentManager();
        SalesReturnFragment fragment = (SalesReturnFragment) fm
                .findFragmentById(R.id.sales_return_fragment);
        fragment.updateGeneralText(mFilterText);
    }

    @Override
    public void updateCancel() {
        android.support.v4.app.FragmentManager fm = getSupportFragmentManager();
        SalesReturnFragment fragment = (SalesReturnFragment) fm
                .findFragmentById(R.id.sales_return_fragment);
        fragment.updateCancel();
    }

    @Override
    public void loadStartVisit() {
        android.support.v4.app.FragmentManager fm = getSupportFragmentManager();
        SalesReturnFragment fragment = (SalesReturnFragment) fm
                .findFragmentById(R.id.sales_return_fragment);
        fragment.loadStartVisit();
    }

    @Override
    public void updateFromFiveLevelFilter(Vector<LevelBO> mParentIdList) {
        android.support.v4.app.FragmentManager fm = getSupportFragmentManager();
        SalesReturnFragment fragment = (SalesReturnFragment) fm
                .findFragmentById(R.id.sales_return_fragment);
        fragment.updateFromFiveLevelFilter(mParentIdList);
    }

    @Override
    public void updateFromFiveLevelFilter(Vector<LevelBO> mParentIdList, HashMap<Integer, Integer> mSelectedIdByLevelId, ArrayList<Integer> mAttributeProducts, String mFilterText) {
        android.support.v4.app.FragmentManager fm = getSupportFragmentManager();
        SalesReturnFragment fragment = (SalesReturnFragment) fm
                .findFragmentById(R.id.sales_return_fragment);
        fragment.updateFromFiveLevelFilter(mParentIdList, mSelectedIdByLevelId, mAttributeProducts, mFilterText);
    }
}
