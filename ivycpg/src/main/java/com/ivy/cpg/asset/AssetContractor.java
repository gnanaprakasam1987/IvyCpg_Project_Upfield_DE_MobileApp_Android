package com.ivy.cpg.asset;

import android.content.Intent;

import com.ivy.sd.png.bo.LevelBO;
import com.ivy.sd.png.bo.ReasonMaster;
import com.ivy.sd.png.bo.StandardListBO;
import com.ivy.sd.png.bo.asset.AssetTrackingBO;

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
       //  void getCurrentLocation(Integer mIndex);
         void updateList();
         void updateFiveFilter(Vector<LevelBO> mParentIdList, HashMap<Integer, Integer> mSelectedIdByLevelId, ArrayList<Integer> mAttributeProducts, String mFilterText);
         void getList();
         void updateImageName();

         void removeExistingImage(String mAssetId, String imageNameStarts,String photoPath);

         void checkDataExistToSave();
         void save(String mModuleCode);
         void updateTimeStamp();

         void updateLocationIndex(int mIndex);
         String getLocationId();

         ArrayList<ReasonMaster> getAssetReasonList();
         ArrayList<ReasonMaster> getAssetConditionList();
         int getItemIndex(String id, ArrayList<ReasonMaster> mList, boolean isReason);

         void setNFCTag(String tag);

         int getAssetListSize();

         void loadMasters(String mMenuCode);


     }

    interface AssetView {
         void updateInitialLoad(Vector<StandardListBO> mList);
      //   void updateCurrentLocation(StandardListBO mCurrentLocation);
         void updateAssets(ArrayList<AssetTrackingBO> mList,ArrayList<AssetTrackingBO> mAllAssetTrackingList);
         void updateFiveFilteredList(ArrayList<AssetTrackingBO> mFilteredList);
         void isDataExistToSave(boolean isExist);

    }
}
