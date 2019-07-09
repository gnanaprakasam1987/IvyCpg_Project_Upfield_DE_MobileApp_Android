package com.ivy.cpg.view.digitalcontent;

import android.os.Bundle;
import androidx.appcompat.widget.Toolbar;

import androidx.fragment.app.FragmentManager;

import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.commons.IvyBaseActivityNoActionBar;
import com.ivy.sd.png.model.BrandDialogInterface;
import com.ivy.sd.png.model.FiveLevelFilterCallBack;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * The Class DigitalContentActivity is used to show digital images Two types of
 * digital content( Seller wise and Retailer wise), both types same screen is
 * using Screen able to view (Images, Audio & Video, Excel, Power point) Product
 * filter for this Screen
 *
 * @author gnanaprakasam.d
 */
public class DigitalContentActivity extends IvyBaseActivityNoActionBar implements BrandDialogInterface,FiveLevelFilterCallBack {


    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_digital_content);

        Toolbar toolbar = findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            if (getSupportActionBar() != null) {
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                getSupportActionBar().setDisplayShowHomeEnabled(true);
                getSupportActionBar().setDisplayShowTitleEnabled(false);
            }
        }
        overridePendingTransition(R.anim.trans_left_in, R.anim.trans_left_out);
    }

    @Override
    public void updateBrandText(String mFilterText, int id) {
        FragmentManager fm = getSupportFragmentManager();
        DigitalContentFragment mFragment = (DigitalContentFragment) fm
                .findFragmentById(R.id.digital_content_fragment);
        mFragment.updateBrandText(mFilterText, id);
    }

    @Override
    public void updateGeneralText(String mFilterText) {
        FragmentManager fm = getSupportFragmentManager();

        DigitalContentFragment mFragment = (DigitalContentFragment) fm
                .findFragmentById(R.id.digital_content_fragment);
        mFragment.updateGeneralText(mFilterText);
    }

    @Override
    public void updateCancel() {
        FragmentManager fm = getSupportFragmentManager();
        DigitalContentFragment mFragment = (DigitalContentFragment) fm
                .findFragmentById(R.id.digital_content_fragment);
        mFragment.updateCancel();
    }

    @Override
    public void updateFromFiveLevelFilter(int mFilteredPid, HashMap<Integer, Integer> mSelectedIdByLevelId, ArrayList<Integer> mAttributeProducts, String mFilterText) {
        FragmentManager fm = getSupportFragmentManager();
        DigitalContentFragment mFragment = (DigitalContentFragment) fm
                .findFragmentById(R.id.digital_content_fragment);
        mFragment.updateFromFiveLevelFilter(mFilteredPid, mSelectedIdByLevelId, mAttributeProducts, mFilterText);
    }

}