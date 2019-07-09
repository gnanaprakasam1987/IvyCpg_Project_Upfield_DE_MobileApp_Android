package com.ivy.cpg.view.van.stockproposal;

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
import com.ivy.sd.png.util.Commons;

import java.util.ArrayList;
import java.util.HashMap;

public class StockProposalScreen extends IvyBaseActivityNoActionBar implements BrandDialogInterface, FiveLevelFilterCallBack {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stock_proposal);
        overridePendingTransition(R.anim.trans_left_in, R.anim.trans_left_out);

        BusinessModel bmodel = (BusinessModel) this.getApplicationContext();
        bmodel.setContext(this);

        try {
            Toolbar toolbar = findViewById(R.id.toolbar);
            if (toolbar != null) {
                setSupportActionBar(toolbar);
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                getSupportActionBar().setDisplayShowHomeEnabled(true);
                getSupportActionBar().setDisplayShowTitleEnabled(false);
            }
        }catch (Exception e){
            Commons.printException(e);
        }

        if (bmodel.userMasterHelper.getUserMasterBO().getUserid() == 0) {
            Toast.makeText(this,
                    getResources().getString(R.string.sessionout_loginagain),
                    Toast.LENGTH_SHORT).show();
            finish();
        }

    }

    @Override
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
                    Commons.printException(e);
                }
            }
        }
    }

    public void numberPressed(View vw) {
        FragmentManager fm = getSupportFragmentManager();
        StockProposalFragment asf = (StockProposalFragment) fm
                .findFragmentById(R.id.stock_proposal_fragment);
        asf.numberPressed(vw);
    }

    @Override
    public void updateBrandText(String mFilterText, int id) {
        FragmentManager fm = getSupportFragmentManager();
        StockProposalFragment asf = (StockProposalFragment) fm
                .findFragmentById(R.id.stock_proposal_fragment);
        asf.updateBrandText(mFilterText, id);
    }

    @Override
    public void updateGeneralText(String mFilterText) {
        FragmentManager fm = getSupportFragmentManager();
        StockProposalFragment asf = (StockProposalFragment) fm
                .findFragmentById(R.id.stock_proposal_fragment);
        asf.updateGeneralText(mFilterText);
    }

    @Override
    public void updateCancel() {
        FragmentManager fm = getSupportFragmentManager();
        StockProposalFragment asf = (StockProposalFragment) fm
                .findFragmentById(R.id.stock_proposal_fragment);
        asf.updateCancel();
    }

    @Override
    public void updateFromFiveLevelFilter(int mProductId, HashMap<Integer, Integer> mSelectedIdByLevelId, ArrayList<Integer> mAttributeProducts, String mFilterText) {
        FragmentManager fm = getSupportFragmentManager();
        StockProposalFragment asf = (StockProposalFragment) fm
                .findFragmentById(R.id.stock_proposal_fragment);
        asf.updateFromFiveLevelFilter(mProductId, mSelectedIdByLevelId, mAttributeProducts, mFilterText);
    }
}
