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

/**
 * The Class DigitalContentDisplay is used to show digital images Two types of
 * digital content( Seller wise and Retailer wise), both types same screen is
 * using Screen able to view (Images, Audio & Video, Excel, Power point) Product
 * filter for this Screen
 *
 * @author gnanaprakasam.d
 */
public class DigitalContentDisplay extends IvyBaseActivityNoActionBar implements BrandDialogInterface {

    private Toolbar toolbar;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_digital_content_display_recyclerview);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }
        overridePendingTransition(R.anim.trans_left_in, R.anim.trans_left_out);
    }

    @Override
    public void updatebrandtext(String filtertext, int id) {
        android.support.v4.app.FragmentManager fm = getSupportFragmentManager();
        DigitalContentDisplayFragment dcdf = (DigitalContentDisplayFragment) fm
                .findFragmentById(R.id.digital_content_fragment);
        dcdf.updatebrandtext(filtertext, id);
    }

    @Override
    public void updategeneraltext(String filtertext) {
        android.support.v4.app.FragmentManager fm = getSupportFragmentManager();

        DigitalContentDisplayFragment dcdf = (DigitalContentDisplayFragment) fm
                .findFragmentById(R.id.digital_content_fragment);
        dcdf.updategeneraltext(filtertext);
    }

    @Override
    public void updateCancel() {
        android.support.v4.app.FragmentManager fm = getSupportFragmentManager();
        DigitalContentDisplayFragment dcdf = (DigitalContentDisplayFragment) fm
                .findFragmentById(R.id.digital_content_fragment);
        dcdf.updateCancel();
    }

    @Override
    public void loadStartVisit() {
        // TODO Auto-generated method stub
    }

    @Override
    public void updateMultiSelectionCatogry(List<Integer> mcatgory) {
        // TODO Auto-generated method stub

    }

    @Override
    public void updateMultiSelectionBrand(List<String> filtername,
                                          List<Integer> filterid) {
        // TODO Auto-generated method stub

    }

    @Override
    public void updatefromFiveLevelFilter(Vector<LevelBO> parentidList) {
        // TODO Auto-generated method stub
        android.support.v4.app.FragmentManager fm = getSupportFragmentManager();
        DigitalContentDisplayFragment dcdf = (DigitalContentDisplayFragment) fm
                .findFragmentById(R.id.digital_content_fragment);
        dcdf.updatefromFiveLevelFilter(parentidList);

    }

    @Override
    public void updatefromFiveLevelFilter(Vector<LevelBO> parentidList, HashMap<Integer, Integer> mSelectedIdByLevelId, ArrayList<Integer> mAttributeProducts, String filtertext) {
        // TODO Auto-generated method stub
        android.support.v4.app.FragmentManager fm = getSupportFragmentManager();
        DigitalContentDisplayFragment dcdf = (DigitalContentDisplayFragment) fm
                .findFragmentById(R.id.digital_content_fragment);
        dcdf.updatefromFiveLevelFilter(parentidList, mSelectedIdByLevelId, mAttributeProducts, filtertext);
    }

}