package com.ivy.sd.png.view;

import android.os.Bundle;
import android.support.v7.widget.Toolbar;

import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.bo.LevelBO;
import com.ivy.sd.png.commons.IvyBaseActivityNoActionBar;
import com.ivy.sd.png.model.BrandDialogInterface;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;

public class PlanogramActivity extends IvyBaseActivityNoActionBar implements
        BrandDialogInterface {

    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_planogram);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            // Used to remove the app logo actionbar icon and set title as home
            // (title support click)
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        overridePendingTransition(R.anim.trans_left_in, R.anim.trans_left_out);
    }

    @Override
    public void onBackPressed() {
        // TODO Auto-generated method stub
    }

    @Override
    public void updatebrandtext(String filtertext, int id) {
        android.support.v4.app.FragmentManager fm = getSupportFragmentManager();
        PlanogramFragment asf = (PlanogramFragment) fm
                .findFragmentById(R.id.planogram_fragment);
        asf.updatebrandtext(filtertext, id);

    }

    @Override
    public void updategeneraltext(String filtertext) {
        android.support.v4.app.FragmentManager fm = getSupportFragmentManager();
        PlanogramFragment asf = (PlanogramFragment) fm
                .findFragmentById(R.id.planogram_fragment);
        asf.updategeneraltext(filtertext);
    }

    @Override
    public void updateCancel() {
        android.support.v4.app.FragmentManager fm = getSupportFragmentManager();
        PlanogramFragment asf = (PlanogramFragment) fm
                .findFragmentById(R.id.planogram_fragment);
        asf.updateCancel();

    }

    @Override
    public void updateMultiSelectionBrand(List<String> filtername,
                                          List<Integer> filterid) {
        // TODO Auto-generated method stub

    }

    @Override
    public void updateMultiSelectionCatogry(List<Integer> mcatgory) {
        // TODO Auto-generated method stub

    }

    @Override
    public void loadStartVisit() {
        // TODO Auto-generated method stub

    }

    @Override
    public void updatefromFiveLevelFilter(Vector<LevelBO> parentidList) {
        android.support.v4.app.FragmentManager fm = getSupportFragmentManager();
        PlanogramFragment asf = (PlanogramFragment) fm
                .findFragmentById(R.id.planogram_fragment);
        asf.updatefromFiveLevelFilter(parentidList);

    }

    @Override
    public void updatefromFiveLevelFilter(Vector<LevelBO> parentidList, HashMap<Integer, Integer> mSelectedIdByLevelId, ArrayList<Integer> mAttributeProducts, String filtertext) {
        android.support.v4.app.FragmentManager fm = getSupportFragmentManager();
        PlanogramFragment asf = (PlanogramFragment) fm
                .findFragmentById(R.id.planogram_fragment);
        asf.updatefromFiveLevelFilter(parentidList,mSelectedIdByLevelId,mAttributeProducts,filtertext);
    }
}
