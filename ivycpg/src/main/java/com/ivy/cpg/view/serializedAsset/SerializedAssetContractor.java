package com.ivy.cpg.view.serializedAsset;

import android.os.Bundle;

import com.ivy.sd.png.bo.ReasonMaster;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by rajkumar.s on 11/16/2017.
 */

public interface SerializedAssetContractor {

    interface SerializedAssetPresenter {
        void setView(SerializedAssetView mAssetView);

        void initialLoad();

        void updateList();

        void updateFiveFilter(int mProductId, HashMap<Integer, Integer> mSelectedIdByLevelId, ArrayList<Integer> mAttributeProducts, String mFilterText);

        void updateImageName();

        void removeExistingImage(String mAssetId, String imageNameStarts, String photoPath);

        void updateLocationIndex(int mIndex);

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

        void fetchAssetSurveyData(String moduleCode, String assetType, String serialNo);

        void deleteAssetSurvey(String serialNo);
    }

    interface SerializedAssetView {
        void updateInitialLoad();

        void updateAssets(ArrayList<SerializedAssetBO> mList, boolean isUnMapped, Bundle mBundle);

        void updateFiveFilteredList(ArrayList<SerializedAssetBO> mFilteredList);

        void isDataExistToSave(boolean isExist, boolean isPhotoExist, boolean isReasonExist, String errorMsg);

        void cancelProgressDialog();

        void navigateSurveyScreen(String moduleCode, String assetType, String serialNo);

        void showError(String errorMsg);

        void save();

        ArrayList<String>getAssetSerialNoList();

        void updateListAdapter();
    }
}
