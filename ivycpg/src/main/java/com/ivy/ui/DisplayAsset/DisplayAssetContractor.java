package com.ivy.ui.DisplayAsset;

import android.content.Context;

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
