package com.ivy.sd.png.view;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
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

public class
AssetTrackingScreen extends IvyBaseActivityNoActionBar implements
        OnEditorActionListener, BrandDialogInterface {

    private BusinessModel mBusinessModel;
    private NFCManager nfcManager;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_assettrackingfrag);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);

        setSupportActionBar(toolbar);

        mBusinessModel = (BusinessModel) getApplicationContext();
        mBusinessModel.setContext(this);
        overridePendingTransition(R.anim.trans_left_in, R.anim.trans_left_out);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        if (mBusinessModel.configurationMasterHelper.SHOW_NFC_SEARCH_IN_ASSET) {
            nfcManager = new NFCManager(AssetTrackingScreen.this);
            nfcManager.onActivityCreate();
            nfcManager.setOnTagReadListener(new NFCManager.TagReadListener() {
                @Override
                public void onTagRead(String tagRead) {
                    if (!tagRead.equals("")) {
                        android.support.v4.app.FragmentManager fm = getSupportFragmentManager();
                        AssetTrackingScreenFragment asf = (AssetTrackingScreenFragment) fm
                                .findFragmentById(R.id.asset_tracking_fragment);
                        asf.updateListByNFCTag(tagRead);
                    }
                }
            });
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        mBusinessModel.useNetworkProvidedValues();

        if (mBusinessModel.configurationMasterHelper.SHOW_NFC_SEARCH_IN_ASSET && nfcManager != null) {
            nfcManager.onActivityResume();
        }

    }

    @Override
    protected void onPause() {
        super.onPause();

        if (mBusinessModel.configurationMasterHelper.SHOW_NFC_SEARCH_IN_ASSET && nfcManager != null) {
            nfcManager.onActivityPause();
        }

    }

    @Override
    public void onNewIntent(Intent intent) {
        if (mBusinessModel.configurationMasterHelper.SHOW_NFC_SEARCH_IN_ASSET) {
            nfcManager.onActivityNewIntent(intent);
        }
    }

    @Override
    public void onBackPressed() {
    }

    protected void onDestroy() {
        super.onDestroy();
        mBusinessModel.assetTrackingHelper=null;
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


    @Override
    public void updateBrandText(String mFilterText, int id) {
        android.support.v4.app.FragmentManager fm = getSupportFragmentManager();
        AssetTrackingScreenFragment asf = (AssetTrackingScreenFragment) fm
                .findFragmentById(R.id.asset_tracking_fragment);
        asf.updateBrandText(mFilterText, id);
    }

    @Override
    public void updateGeneralText(String mFilterText) {
    }

    @Override
    public void updateCancel() {
        android.support.v4.app.FragmentManager fm = getSupportFragmentManager();
        AssetTrackingScreenFragment asf = (AssetTrackingScreenFragment) fm
                .findFragmentById(R.id.asset_tracking_fragment);
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
        AssetTrackingScreenFragment asf = (AssetTrackingScreenFragment) fm
                .findFragmentById(R.id.asset_tracking_fragment);
        asf.updateFromFiveLevelFilter(mParentIdList);
    }

    @Override
    public void updateFromFiveLevelFilter(Vector<LevelBO> mParentIdList, HashMap<Integer, Integer> mSelectedIdByLevelId, ArrayList<Integer> mAttributeProducts, String mFilterText) {
        android.support.v4.app.FragmentManager fm = getSupportFragmentManager();
        AssetTrackingScreenFragment asf = (AssetTrackingScreenFragment) fm
                .findFragmentById(R.id.asset_tracking_fragment);
        asf.updateFromFiveLevelFilter(mParentIdList,mSelectedIdByLevelId,mAttributeProducts, mFilterText);
    }

}
