package com.ivy.cpg.view.emptyreturn;

import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.commons.IvyBaseActivityNoActionBar;
import com.ivy.sd.png.model.BrandDialogInterface;
import com.ivy.sd.png.model.FiveLevelFilterCallBack;

import java.util.ArrayList;
import java.util.HashMap;

public class EmptyReturnActivity extends IvyBaseActivityNoActionBar implements BrandDialogInterface,FiveLevelFilterCallBack {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_empty_return);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        overridePendingTransition(R.anim.trans_left_in, R.anim.trans_left_out);
    }

    public void numberPressed(View v) {
        android.support.v4.app.FragmentManager fm = getSupportFragmentManager();
        EmptyReturnFragment fragment = (EmptyReturnFragment) fm
                .findFragmentById(R.id.empty_return_fragment);
        fragment.numberPressed(v);
    }

    @Override
    public void updateBrandText(String mFilterText, int id) {
        android.support.v4.app.FragmentManager fm = getSupportFragmentManager();
        EmptyReturnFragment fragment = (EmptyReturnFragment) fm
                .findFragmentById(R.id.empty_return_fragment);
        fragment.updateBrandText(mFilterText, id);
    }

    @Override
    public void updateGeneralText(String mFilterText) {
        android.support.v4.app.FragmentManager fm = getSupportFragmentManager();
        EmptyReturnFragment fragment = (EmptyReturnFragment) fm
                .findFragmentById(R.id.empty_return_fragment);
        fragment.updateGeneralText(mFilterText);
    }

    @Override
    public void updateCancel() {
        android.support.v4.app.FragmentManager fm = getSupportFragmentManager();
        EmptyReturnFragment fragment = (EmptyReturnFragment) fm
                .findFragmentById(R.id.empty_return_fragment);
        fragment.updateCancel();
    }


    @Override
    public void updateFromFiveLevelFilter(int mFilteredPid, HashMap<Integer, Integer> mSelectedIdByLevelId, ArrayList<Integer> mAttributeProducts, String mFilterText) {

    }

}
