package com.ivy.cpg.view.salesreturn;

import android.os.Bundle;
import android.support.v7.widget.Toolbar;

import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.commons.IvyBaseActivityNoActionBar;
import com.ivy.sd.png.model.BrandDialogInterface;
import com.ivy.sd.png.model.FiveLevelFilterCallBack;

import java.util.ArrayList;
import java.util.HashMap;

public class SalesReturnActivity extends IvyBaseActivityNoActionBar implements BrandDialogInterface,FiveLevelFilterCallBack {

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
            getSupportActionBar().setTitle(null);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }
        overridePendingTransition(R.anim.trans_left_in, R.anim.trans_left_out);
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
    public void updateFromFiveLevelFilter(int mProductId, HashMap<Integer, Integer> mSelectedIdByLevelId, ArrayList<Integer> mAttributeProducts, String mFilterText) {
        android.support.v4.app.FragmentManager fm = getSupportFragmentManager();
        SalesReturnFragment fragment = (SalesReturnFragment) fm
                .findFragmentById(R.id.sales_return_fragment);
        fragment.updateFromFiveLevelFilter(mProductId, mSelectedIdByLevelId, mAttributeProducts, mFilterText);
    }

}
