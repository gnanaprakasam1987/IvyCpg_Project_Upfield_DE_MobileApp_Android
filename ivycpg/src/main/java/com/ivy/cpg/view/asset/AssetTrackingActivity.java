package com.ivy.cpg.view.asset;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.widget.Toolbar;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

import androidx.fragment.app.FragmentManager;

import com.ivy.cpg.nfc.NFCManager;
import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.commons.IvyBaseActivityNoActionBar;
import com.ivy.sd.png.model.BrandDialogInterface;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.model.FiveLevelFilterCallBack;
import com.ivy.sd.png.util.Commons;
import com.ivy.sd.png.view.DataPickerDialogFragment;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

public class
AssetTrackingActivity extends IvyBaseActivityNoActionBar implements
        OnEditorActionListener, BrandDialogInterface, DataPickerDialogFragment.UpdateDateInterface,FiveLevelFilterCallBack {

    private BusinessModel mBModel;
    private NFCManager nfcManager;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_asset_tracking);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);

        setSupportActionBar(toolbar);

        mBModel = (BusinessModel) getApplicationContext();
        mBModel.setContext(this);
        overridePendingTransition(R.anim.trans_left_in, R.anim.trans_left_out);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        if (mBModel.configurationMasterHelper.SHOW_NFC_SEARCH_IN_ASSET) {
            nfcManager = new NFCManager(AssetTrackingActivity.this);
            nfcManager.onActivityCreate();
            nfcManager.setOnTagReadListener(new NFCManager.TagReadListener() {
                @Override
                public void onTagRead(String tagRead) {
                    if (!tagRead.equals("")) {
                        FragmentManager fm = getSupportFragmentManager();
                        AssetTrackingFragment asf = (AssetTrackingFragment) fm
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
        mBModel.useNetworkProvidedValues();

        if (mBModel.configurationMasterHelper.SHOW_NFC_SEARCH_IN_ASSET && nfcManager != null) {
            nfcManager.onActivityResume();
        }

    }

    @Override
    protected void onPause() {
        super.onPause();

        if (mBModel.configurationMasterHelper.SHOW_NFC_SEARCH_IN_ASSET && nfcManager != null) {
            nfcManager.onActivityPause();
        }

    }

    @Override
    public void onNewIntent(Intent intent) {
        if (mBModel.configurationMasterHelper.SHOW_NFC_SEARCH_IN_ASSET) {
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
    public void updateBrandText(String mFilterText, int id) {
        FragmentManager fm = getSupportFragmentManager();
        AssetTrackingFragment asf = (AssetTrackingFragment) fm
                .findFragmentById(R.id.asset_tracking_fragment);
        asf.updateBrandText(mFilterText, id);
    }

    @Override
    public void updateGeneralText(String mFilterText) {
    }

    @Override
    public void updateCancel() {
        FragmentManager fm = getSupportFragmentManager();
        AssetTrackingFragment asf = (AssetTrackingFragment) fm
                .findFragmentById(R.id.asset_tracking_fragment);
        asf.updateCancel();
    }

    @Override
    public void updateFromFiveLevelFilter(int mProductId, HashMap<Integer, Integer> mSelectedIdByLevelId, ArrayList<Integer> mAttributeProducts, String mFilterText) {
        FragmentManager fm = getSupportFragmentManager();
        AssetTrackingFragment asf = (AssetTrackingFragment) fm
                .findFragmentById(R.id.asset_tracking_fragment);
        asf.updateFromFiveLevelFilter(mProductId,mSelectedIdByLevelId,mAttributeProducts, mFilterText);    }

    @Override
    public boolean onEditorAction(TextView arg0, int arg1, KeyEvent arg2) {
        return false;
    }

    @Override
    public void updateDate(Date date, String tag) {
        FragmentManager fm = getSupportFragmentManager();
        AssetTrackingFragment fragment = (AssetTrackingFragment) fm
                .findFragmentById(R.id.asset_tracking_fragment);
        fragment.updateDate(date,tag);

    }
}
