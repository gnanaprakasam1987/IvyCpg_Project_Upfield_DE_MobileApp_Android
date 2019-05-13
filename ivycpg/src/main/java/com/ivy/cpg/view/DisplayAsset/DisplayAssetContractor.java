package com.ivy.cpg.view.DisplayAsset;

import android.content.Context;

import com.ivy.cpg.view.asset.bo.AssetTrackingBO;

import java.util.ArrayList;

public interface DisplayAssetContractor {

    interface presenter{

        void setView(DisplayAssetContractor.View view);
        void refreshStatus();
        boolean saveDisplayAssets(Context context);
    }

    interface View{

        void updateStatus(String companyName,double ownCompanyWeightage,double otherCompanyMaxWeightage,int flag);

    }

}
