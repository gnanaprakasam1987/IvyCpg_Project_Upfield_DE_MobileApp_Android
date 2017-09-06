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

public class AssetTrackingScreen extends IvyBaseActivityNoActionBar implements
        OnEditorActionListener, BrandDialogInterface {

    private BusinessModel bmodel;
    private NFCManager nfcManager;
    private Toolbar toolbar;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_assettrackingfrag);

        toolbar = (Toolbar) findViewById(R.id.toolbar);

        setSupportActionBar(toolbar);

        bmodel = (BusinessModel) getApplicationContext();
        bmodel.setContext(this);
        overridePendingTransition(R.anim.trans_left_in, R.anim.trans_left_out);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        if (bmodel.configurationMasterHelper.SHOW_NFC_SEARCH_IN_ASSET) {
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
        bmodel.useNetworkProvidedValues();

        if (bmodel.configurationMasterHelper.SHOW_NFC_SEARCH_IN_ASSET && nfcManager != null) {
            nfcManager.onActivityResume();
        }

    }

    @Override
    protected void onPause() {
        super.onPause();

        if (bmodel.configurationMasterHelper.SHOW_NFC_SEARCH_IN_ASSET && nfcManager != null) {
            nfcManager.onActivityPause();
        }

    }

    @Override
    public void onNewIntent(Intent intent) {
        if (bmodel.configurationMasterHelper.SHOW_NFC_SEARCH_IN_ASSET) {
            nfcManager.onActivityNewIntent(intent);
        }
    }

    @Override
    public void onBackPressed() {
    }

    protected void onDestroy() {
        super.onDestroy();
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
    public void updatebrandtext(String filtertext, int id) {
        android.support.v4.app.FragmentManager fm = getSupportFragmentManager();
        AssetTrackingScreenFragment asf = (AssetTrackingScreenFragment) fm
                .findFragmentById(R.id.asset_tracking_fragment);
        asf.updatebrandtext(filtertext, id);
    }

    @Override
    public void updategeneraltext(String filtertext) {
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
    public void updateMultiSelectionCatogry(List<Integer> mcatgory) {

    }

    @Override
    public void updateMultiSelectionBrand(List<String> filtername,
                                          List<Integer> filterid) {

    }

    @Override
    public boolean onEditorAction(TextView arg0, int arg1, KeyEvent arg2) {
        return false;
    }

    @Override
    public void updatefromFiveLevelFilter(Vector<LevelBO> parentidList) {
        android.support.v4.app.FragmentManager fm = getSupportFragmentManager();
        AssetTrackingScreenFragment asf = (AssetTrackingScreenFragment) fm
                .findFragmentById(R.id.asset_tracking_fragment);
        asf.updatefromFiveLevelFilter(parentidList);
    }

    @Override
    public void updatefromFiveLevelFilter(Vector<LevelBO> parentidList, HashMap<Integer, Integer> mSelectedIdByLevelId, ArrayList<Integer> mAttributeProducts, String filtertext) {
        android.support.v4.app.FragmentManager fm = getSupportFragmentManager();
        AssetTrackingScreenFragment asf = (AssetTrackingScreenFragment) fm
                .findFragmentById(R.id.asset_tracking_fragment);
        asf.updatefromFiveLevelFilter(parentidList,mSelectedIdByLevelId,mAttributeProducts,filtertext);
    }

}
