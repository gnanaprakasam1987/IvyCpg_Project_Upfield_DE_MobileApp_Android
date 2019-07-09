package com.ivy.cpg.view.survey;

import android.os.Bundle;
import androidx.appcompat.widget.Toolbar;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Toast;

import androidx.fragment.app.FragmentManager;

import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.commons.IvyBaseActivityNoActionBar;
import com.ivy.sd.png.model.BrandDialogInterface;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.model.FiveLevelFilterCallBack;
import com.ivy.sd.png.util.Commons;

import java.util.ArrayList;
import java.util.HashMap;

public class SurveyActivityNew extends IvyBaseActivityNoActionBar implements BrandDialogInterface, FiveLevelFilterCallBack {

    BusinessModel bmodel;
    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_surveyactivity);
        toolbar = (Toolbar) findViewById(R.id.toolbar);

        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(null);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setElevation(0);
        }

        bmodel = (BusinessModel) this.getApplicationContext();
        bmodel.setContext(this);
        overridePendingTransition(R.anim.trans_left_in, R.anim.trans_left_out);
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

    protected void onDestroy() {
        super.onDestroy();
        unbindDrawables(findViewById(R.id.root));
        System.gc();
    }

    private void unbindDrawables(View view) {
        if (view.getBackground() != null) {
            view.getBackground().setCallback(null);
        }
        if (view instanceof ViewGroup) {
            for (int i = 0; i < ((ViewGroup) view).getChildCount(); i++) {
                unbindDrawables(((ViewGroup) view).getChildAt(i));
            }
            try {
                if (!(view instanceof AdapterView<?>))
                    ((ViewGroup) view).removeAllViews();
            } catch (Exception e) {
                Commons.printException(e);
            }
        }
    }

    @Override
    public void updateBrandText(String mFilterText, int id) {
        FragmentManager fm = getSupportFragmentManager();
        SurveyActivityNewFragment asf = (SurveyActivityNewFragment) fm
                .findFragmentById(R.id.survey_fragment);
        asf.updateBrandText(mFilterText, id);
    }

    @Override
    public void updateGeneralText(String mFilterText) {
    }

    @Override
    public void updateCancel() {
        FragmentManager fm = getSupportFragmentManager();
        SurveyActivityNewFragment asf = (SurveyActivityNewFragment) fm.findFragmentById(R.id.survey_fragment);
        asf.updateCancel();
    }

    @Override
    public void updateFromFiveLevelFilter(int mProductId, HashMap<Integer, Integer> mSelectedIdByLevelId, ArrayList<Integer> mAttributeProducts, String mFilterText) {
        FragmentManager fm = getSupportFragmentManager();
        SurveyActivityNewFragment asf = (SurveyActivityNewFragment) fm
                .findFragmentById(R.id.survey_fragment);
//		asf.updateFromFiveLevelFilter(parentidList);
        asf.updateFromFiveLevelFilter(mProductId, mSelectedIdByLevelId, mAttributeProducts, mFilterText);
    }


}