package com.ivy.cpg.view.asset;

import android.os.Bundle;

import com.ivy.sd.png.bo.ReasonMaster;
import com.ivy.sd.png.bo.StandardListBO;
import com.ivy.cpg.view.asset.bo.AssetTrackingBO;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Vector;

/**
 * Created by rajkumar.s on 11/16/2017.
 */

public interface AssetContractor {

     interface AssetPresenter {
         void setView(AssetView mAssetView);

         void initialLoad();
         void updateList();
         void updateFiveFilter(int mProductId, HashMap<Integer, Integer> mSelectedIdByLevelId, ArrayList<Integer> mAttributeProducts, String mFilterText);

         void updateImageName();
         void removeExistingImage(String mAssetId, String imageNameStarts,String photoPath);
         void updateLocationIndex(int mIndex);
         String getLocationId();
         void setNFCTag(String tag);
         int getAssetListSize();
         void setBarcode(String mBarcode);

         //Reasons
         ArrayList<ReasonMaster> getAssetReasonList();
         ArrayList<ReasonMaster> getAssetConditionList();
         int getItemIndex(String id, ArrayList<ReasonMaster> mList, boolean isReason);

         //save
         void checkDataExistToSave();
         void save(String mModuleCode);
         void updateTimeStamp();


     }

    interface AssetView {
         void updateInitialLoad(Vector<StandardListBO> mList);
         void updateAssets(ArrayList<AssetTrackingBO> mList, boolean isUnMapped, Bundle mBundle);
         void updateFiveFilteredList(ArrayList<AssetTrackingBO> mFilteredList);
         void isDataExistToSave(boolean isExist,boolean isPhotoExist,boolean isReasonExist,String errorMsg);
         void cancelProgressDialog();

        void showError(String errorMsg);

        void save();
    }
}
