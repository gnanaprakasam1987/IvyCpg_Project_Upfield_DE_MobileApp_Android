package com.ivy.sd.png.view.van;

import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.ViewGroup;

import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.bo.LevelBO;
import com.ivy.sd.png.commons.IvyBaseActivityNoActionBar;
import com.ivy.sd.png.model.BrandDialogInterface;
import com.ivy.sd.png.util.Commons;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;

public class VanStockAdjustActivity extends IvyBaseActivityNoActionBar implements BrandDialogInterface {

    private Toolbar toolbar;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_van_stockadjust);
        toolbar = (Toolbar) findViewById(R.id.toolbar);

        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            // Used to remove the app logo actionbar icon and set title as home
            // (title support click)
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindDrawables(findViewById(R.id.root));
    }

    private void unbindDrawables(View view) {
        if (view != null) {
            if (view.getBackground() != null) {
                view.getBackground().setCallback(null);
            }
            if (view instanceof ViewGroup) {
                for (int i = 0; i < ((ViewGroup) view).getChildCount(); i++) {
                    unbindDrawables(((ViewGroup) view).getChildAt(i));
                }
                try {
                    ((ViewGroup) view).removeAllViews();
                } catch (Exception e) {
                    Commons.printException("" + e);
                }
            }
        }
    }

    public void numberPressed(View vw) {
        android.support.v4.app.FragmentManager fm = getSupportFragmentManager();
        VanStockAdjustFragment vsaf = (VanStockAdjustFragment) fm
                .findFragmentById(R.id.van_stockadjust);
        vsaf.numberPressed(vw);
    }

    @Override
    public void updateBrandText(String mFilterText, int id) {
        android.support.v4.app.FragmentManager fm = getSupportFragmentManager();
        VanStockAdjustFragment vsaf = (VanStockAdjustFragment) fm
                .findFragmentById(R.id.van_stockadjust);
        vsaf.updateBrandText(mFilterText, id);
    }

    @Override
    public void updateGeneralText(String mFilterText) {
        android.support.v4.app.FragmentManager fm = getSupportFragmentManager();

        VanStockAdjustFragment vsaf = (VanStockAdjustFragment) fm
                .findFragmentById(R.id.van_stockadjust);
        vsaf.updateGeneralText(mFilterText);
    }

    @Override
    public void updateCancel() {
        android.support.v4.app.FragmentManager fm = getSupportFragmentManager();
        VanStockAdjustFragment vsaf = (VanStockAdjustFragment) fm
                .findFragmentById(R.id.van_stockadjust);
        vsaf.updateCancel();
    }

    @Override
    public void loadStartVisit() {

    }

    @Override
    public void updateMultiSelectionCategory(List<Integer> mCategory) {

    }

    @Override
    public void updateMultiSelectionBrand(List<String> mFilterName,
                                          List<Integer> mFilterId) {

    }

    @Override
    public void updateFromFiveLevelFilter(Vector<LevelBO> mParentIdList) {
        android.support.v4.app.FragmentManager fm = getSupportFragmentManager();
        VanStockAdjustFragment vsaf = (VanStockAdjustFragment) fm
                .findFragmentById(R.id.van_stockadjust);
        vsaf.updateFromFiveLevelFilter(mParentIdList);

    }

    @Override
    public void updateFromFiveLevelFilter(Vector<LevelBO> mParentIdList, HashMap<Integer, Integer> mSelectedIdByLevelId, ArrayList<Integer> mAttributeProducts, String mFilterText) {
        android.support.v4.app.FragmentManager fm = getSupportFragmentManager();
        VanStockAdjustFragment vsaf = (VanStockAdjustFragment) fm
                .findFragmentById(R.id.van_stockadjust);
        vsaf.updateFromFiveLevelFilter(mParentIdList, mSelectedIdByLevelId, mAttributeProducts, mFilterText);
    }
}
