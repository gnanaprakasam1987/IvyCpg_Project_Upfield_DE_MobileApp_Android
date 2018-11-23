package com.ivy.cpg.view.serializedAsset;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.KeyEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

import com.itextpdf.text.pdf.PRIndirectReference;
import com.ivy.cpg.nfc.NFCManager;
import com.ivy.cpg.view.asset.AssetTrackingFragment;
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
SerializedAssetActivity extends IvyBaseActivityNoActionBar implements
        OnEditorActionListener, BrandDialogInterface, DataPickerDialogFragment.UpdateDateInterface,FiveLevelFilterCallBack {

    private BusinessModel mBModel;
    private NFCManager nfcManager;
    private SerializedAssetHelper serializedAssetHelper;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_serialized_asset);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);

        setSupportActionBar(toolbar);

        mBModel = (BusinessModel) getApplicationContext();
        mBModel.setContext(this);
        overridePendingTransition(R.anim.trans_left_in, R.anim.trans_left_out);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        serializedAssetHelper=SerializedAssetHelper.getInstance(this);

        if (serializedAssetHelper.SHOW_NFC_SEARCH_IN_ASSET) {
            nfcManager = new NFCManager(SerializedAssetActivity.this);
            nfcManager.onActivityCreate();
            nfcManager.setOnTagReadListener(new NFCManager.TagReadListener() {
                @Override
                public void onTagRead(String tagRead) {
                    if (!tagRead.equals("")) {
                        android.support.v4.app.FragmentManager fm = getSupportFragmentManager();
                        SerializedAssetFragment asf = (SerializedAssetFragment) fm
                                .findFragmentById(R.id.serialized_asset_fragment);
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

        if (serializedAssetHelper.SHOW_NFC_SEARCH_IN_ASSET && nfcManager != null) {
            nfcManager.onActivityResume();
        }

    }

    @Override
    protected void onPause() {
        super.onPause();

        if (serializedAssetHelper.SHOW_NFC_SEARCH_IN_ASSET && nfcManager != null) {
            nfcManager.onActivityPause();
        }

    }

    @Override
    public void onNewIntent(Intent intent) {
        if (serializedAssetHelper.SHOW_NFC_SEARCH_IN_ASSET) {
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
        android.support.v4.app.FragmentManager fm = getSupportFragmentManager();
        AssetTrackingFragment asf = (AssetTrackingFragment) fm
                .findFragmentById(R.id.asset_tracking_fragment);
        asf.updateBrandText(mFilterText, id);
    }

    @Override
    public void updateGeneralText(String mFilterText) {
    }

    @Override
    public void updateCancel() {
        android.support.v4.app.FragmentManager fm = getSupportFragmentManager();
        SerializedAssetFragment asf = (SerializedAssetFragment) fm
                .findFragmentById(R.id.serialized_asset_fragment);
        asf.updateCancel();
    }

    @Override
    public void updateFromFiveLevelFilter(int mProductId, HashMap<Integer, Integer> mSelectedIdByLevelId, ArrayList<Integer> mAttributeProducts, String mFilterText) {
        android.support.v4.app.FragmentManager fm = getSupportFragmentManager();
        SerializedAssetFragment asf = (SerializedAssetFragment) fm
                .findFragmentById(R.id.serialized_asset_fragment);
        asf.updateFromFiveLevelFilter(mProductId,mSelectedIdByLevelId,mAttributeProducts, mFilterText);    }

    @Override
    public boolean onEditorAction(TextView arg0, int arg1, KeyEvent arg2) {
        return false;
    }

    @Override
    public void updateDate(Date date, String tag) {
        android.support.v4.app.FragmentManager fm = getSupportFragmentManager();
        SerializedAssetFragment fragment = (SerializedAssetFragment) fm
                .findFragmentById(R.id.serialized_asset_fragment);
        fragment.updateDate(date,tag);

    }
}
