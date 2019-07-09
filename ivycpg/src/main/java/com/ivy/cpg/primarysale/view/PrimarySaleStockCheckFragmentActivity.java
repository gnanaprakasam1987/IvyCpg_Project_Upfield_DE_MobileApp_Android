package com.ivy.cpg.primarysale.view;

import android.os.Bundle;
import androidx.appcompat.widget.Toolbar;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.fragment.app.FragmentManager;

import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.commons.IvyBaseActivityNoActionBar;
import com.ivy.sd.png.model.BrandDialogInterface;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.model.FiveLevelFilterCallBack;

import java.util.ArrayList;
import java.util.HashMap;


/**
 * Created by vinodh.r on 21-09-2015.
 */
public class PrimarySaleStockCheckFragmentActivity extends IvyBaseActivityNoActionBar implements BrandDialogInterface,FiveLevelFilterCallBack {
    BusinessModel bmodel;
    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_activity_distributor_stock_check);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // getSupportActionBar().setTitle(bmodel.mSelectedActivityName);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setIcon(null);
        }

        bmodel = (BusinessModel) this.getApplicationContext();
        bmodel.setContext(this);
        if (bmodel.userMasterHelper.getUserMasterBO().getUserid() == 0) {
            Toast.makeText(this,
                    getResources().getString(R.string.sessionout_loginagain),
                    Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    @Override
    public void onBackPressed() {
    }

    public void numberPressed(View vw) {
        FragmentManager fm = getSupportFragmentManager();
        PrimarySaleStockCheckFragment asf = (PrimarySaleStockCheckFragment) fm
                .findFragmentById(R.id.distributor_stock_check_fragment);
        asf.numberPressed(vw);
    }

    protected void onDestroy() {
        super.onDestroy();
        unbindDrawables(findViewById(R.id.root));
        System.gc();
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
                }
            }
        }
    }


    @Override
    public void updateBrandText(String mFilterText, int id) {

    }

    @Override
    public void updateGeneralText(String mFilterText) {

    }

    @Override
    public void updateCancel() {

    }

    @Override
    public void updateFromFiveLevelFilter(int mFilteredPid, HashMap<Integer, Integer> mSelectedIdByLevelId, ArrayList<Integer> mAttributeProducts, String mFilterText) {
        FragmentManager fm = getSupportFragmentManager();
        PrimarySaleStockCheckFragment asf = (PrimarySaleStockCheckFragment) fm
                .findFragmentById(R.id.distributor_stock_check_fragment);
        asf.updateFromFiveLevelFilter(mFilteredPid, mSelectedIdByLevelId, mAttributeProducts, mFilterText);
    }
}
