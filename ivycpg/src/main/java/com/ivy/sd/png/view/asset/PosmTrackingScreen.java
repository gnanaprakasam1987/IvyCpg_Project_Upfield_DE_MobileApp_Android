package com.ivy.sd.png.view.asset;

import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

import com.ivy.cpg.nfc.NFCManager;
import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.bo.LevelBO;
import com.ivy.sd.png.commons.IvyBaseActivityNoActionBar;
import com.ivy.sd.png.model.BrandDialogInterface;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.util.Commons;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Vector;

public class PosmTrackingScreen extends IvyBaseActivityNoActionBar implements
        OnEditorActionListener, BrandDialogInterface {
    BusinessModel mBModel;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_posmtracking);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);

        mBModel = (BusinessModel) getApplicationContext();
        mBModel.setContext(this);

        overridePendingTransition(R.anim.trans_left_in, R.anim.trans_left_out);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            if(getSupportActionBar()!=null) {
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            }
        }

        if (mBModel.configurationMasterHelper.SHOW_NFC_SEARCH_IN_ASSET) {
            NFCManager nfcManager = new NFCManager(PosmTrackingScreen.this);
            nfcManager.onActivityCreate();
            nfcManager.setOnTagReadListener(new NFCManager.TagReadListener() {
                @Override
                public void onTagRead(String tagRead) {
                    if (!tagRead.equals("")) {
                        android.support.v4.app.FragmentManager fm = getSupportFragmentManager();
                        PosmFragment asf = (PosmFragment) fm
                                .findFragmentById(R.id.posm_tracking);
                        asf.updateListByNFCTag(tagRead);
                    }
                }
            });
        }

    }

    @Override
    public void onBackPressed() {
    }

    protected void onDestroy() {
        super.onDestroy();

        mBModel.assetTrackingHelper=null;
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
                    if (!(view instanceof AdapterView<?>))
                        ((ViewGroup) view).removeAllViews();
                } catch (Exception e) {
                    Commons.printException(e);
                }
            }
        }
    }

    public void numberPressed(View vw) {
        android.support.v4.app.FragmentManager fm = getSupportFragmentManager();
        PosmFragment asf = (PosmFragment) fm
                .findFragmentById(R.id.posm_tracking);
        asf.numberPressed(vw);
    }

    @Override
    public void updateBrandText(String mFilterText, int id) {
        android.support.v4.app.FragmentManager fm = getSupportFragmentManager();
        PosmFragment asf = (PosmFragment) fm
                .findFragmentById(R.id.posm_tracking);
        asf.updateBrandText(mFilterText, id);
    }

    @Override
    public void updateGeneralText(String mFilterText) {
    }

    @Override
    public void updateCancel() {
        android.support.v4.app.FragmentManager fm = getSupportFragmentManager();
        PosmFragment asf = (PosmFragment) fm
                .findFragmentById(R.id.posm_tracking);
        asf.updateCancel();
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
    public boolean onEditorAction(TextView arg0, int arg1, KeyEvent arg2) {
        return false;
    }

    @Override
    public void updateFromFiveLevelFilter(Vector<LevelBO> mParentIdList) {
        android.support.v4.app.FragmentManager fm = getSupportFragmentManager();
        PosmFragment asf = (PosmFragment) fm
                .findFragmentById(R.id.posm_tracking);
        asf.updateFromFiveLevelFilter(mParentIdList);
    }

    @Override
    public void updateFromFiveLevelFilter(Vector<LevelBO> mParentIdList, HashMap<Integer, Integer> mSelectedIdByLevelId, ArrayList<Integer> mAttributeProducts, String mFilterText) {
        android.support.v4.app.FragmentManager fm = getSupportFragmentManager();
        PosmFragment asf = (PosmFragment) fm
                .findFragmentById(R.id.posm_tracking);
        asf.updateFromFiveLevelFilter(mParentIdList,mSelectedIdByLevelId,mAttributeProducts, mFilterText);
    }
}
