package com.ivy.sd.png.view;

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

    private Toolbar toolbar;
    private NFCManager nfcManager;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // include List Header
        setContentView(R.layout.activity_posmtracking);

        toolbar = (Toolbar) findViewById(R.id.toolbar);

        BusinessModel bmodel = (BusinessModel) getApplicationContext();
        bmodel.setContext(this);

        overridePendingTransition(R.anim.trans_left_in, R.anim.trans_left_out);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        if (bmodel.configurationMasterHelper.SHOW_NFC_SEARCH_IN_ASSET) {
            nfcManager = new NFCManager(PosmTrackingScreen.this);
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
    public void updatebrandtext(String filtertext, int id) {
        android.support.v4.app.FragmentManager fm = getSupportFragmentManager();
        PosmFragment asf = (PosmFragment) fm
                .findFragmentById(R.id.posm_tracking);
        asf.updatebrandtext(filtertext, id);
    }

    @Override
    public void updategeneraltext(String filtertext) {
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
    public void updateMultiSelectionCatogry(List<Integer> mcatgory) {

    }

    @Override
    public void updateMultiSelectionBrand(List<String> filtername,
                                          List<Integer> filterid) {

    }

    @Override
    public boolean onEditorAction(TextView arg0, int arg1, KeyEvent arg2) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public void updatefromFiveLevelFilter(Vector<LevelBO> parentidList) {
        android.support.v4.app.FragmentManager fm = getSupportFragmentManager();
        PosmFragment asf = (PosmFragment) fm
                .findFragmentById(R.id.posm_tracking);
        asf.updatefromFiveLevelFilter(parentidList);
    }

    @Override
    public void updatefromFiveLevelFilter(Vector<LevelBO> parentidList, HashMap<Integer, Integer> mSelectedIdByLevelId, ArrayList<Integer> mAttributeProducts, String filtertext) {
        android.support.v4.app.FragmentManager fm = getSupportFragmentManager();
        PosmFragment asf = (PosmFragment) fm
                .findFragmentById(R.id.posm_tracking);
        asf.updatefromFiveLevelFilter(parentidList,mSelectedIdByLevelId,mAttributeProducts,filtertext);
    }
}
