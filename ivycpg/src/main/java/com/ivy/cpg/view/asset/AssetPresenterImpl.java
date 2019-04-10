package com.ivy.cpg.view.asset;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;

import com.ivy.cpg.view.asset.bo.AssetTrackingBO;
import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.bo.ReasonMaster;
import com.ivy.sd.png.bo.StandardListBO;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.util.Commons;
import com.ivy.utils.AppUtils;
import com.ivy.utils.DateTimeUtils;
import com.ivy.utils.FileUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by rajkumar.s on 11/20/2017.
 */

public class AssetPresenterImpl implements AssetContractor.AssetPresenter {

    BusinessModel mBModel;
    AssetTrackingHelper mAssetTrackingHelper;
    AssetContractor.AssetView mAssetView;

    int mSelectedLocationIndex;
    private static final String ALL = "ALL";
    private String mCapturedBarcode = "ALL";
    private String mCapturedNFCTag = "";
    ArrayList<AssetTrackingBO> mAssetList = new ArrayList<>();

    public int mSelectedAssetID = 0;
    public String mSelectedImageName = "";
    public String mSelectedSerial = "";
    String photoPath = "";
    private Context mContext;

    public AssetPresenterImpl(Context mContext, BusinessModel mBModel, AssetTrackingHelper mAssetTrackingHelper) {
        this.mBModel = mBModel;
        this.mAssetTrackingHelper = mAssetTrackingHelper;
        this.mContext = mContext;
    }

    @Override
    public void setView(AssetContractor.AssetView mAssetView) {
        this.mAssetView = mAssetView;
    }


    @Override
    public void save(String mModuleCode) {
        new SaveAssetAsync().execute(mModuleCode);
    }

    private class SaveAssetAsync extends AsyncTask<String, Void, String> {
        protected void onPreExecute() {

        }

        @Override
        protected String doInBackground(String... params) {
            deleteUnUsedImages();
            mAssetTrackingHelper.saveAsset(mContext.getApplicationContext(), params[0]);
            mBModel.saveModuleCompletion(params[0], true);
            return "";
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            mAssetView.cancelProgressDialog();
        }

    }

    @Override
    public void updateTimeStamp() {
        mBModel.outletTimeStampHelper.updateTimeStampModuleWise(DateTimeUtils.now(DateTimeUtils.TIME));
    }

    @Override
    public void initialLoad() {
        mAssetView.updateInitialLoad(mBModel.productHelper.getInStoreLocation());
    }

    @Override
    public void setBarcode(String mBarcode) {
        mCapturedBarcode = mBarcode;
    }

    /* @Override
    public void getCurrentLocation(Integer mIndex) {
        mSelectedLocationIndex=mIndex;
        mAssetView.updateCurrentLocation(mBModel.productHelper.getInStoreLocation().get(mIndex));
    }*/

    @Override
    public void updateLocationIndex(int mIndex) {
        mSelectedLocationIndex = mIndex;

    }

    @Override
    public String getLocationId() {
        return mBModel.productHelper.getInStoreLocation().get(mSelectedLocationIndex).getListID();
    }

    @Override
    public ArrayList<ReasonMaster> getAssetReasonList() {
        return mAssetTrackingHelper.getAssetReasonList();
    }

    @Override
    public ArrayList<ReasonMaster> getAssetConditionList() {
        return mAssetTrackingHelper.getAssetConditionList();
    }

    @Override
    public int getItemIndex(String id, ArrayList<ReasonMaster> mList, boolean isReason) {
        if (isReason) {
            for (int i = 0; i < mList.size(); i++) {
                ReasonMaster reasonBO = mList.get(i);
                if (reasonBO.getReasonID().equals(id)) {
                    return i;
                }
            }
        } else {
            for (int i = 0; i < mList.size(); i++) {
                ReasonMaster reasonBO = mList.get(i);
                if (reasonBO.getConditionID().equals(id)) {
                    return i;
                }
            }
        }
        return -1;
    }

    @Override
    public void setNFCTag(String tag) {
        mCapturedNFCTag = tag;
    }

    @Override
    public int getAssetListSize() {
        return mAssetList.size();
    }

    @Override
    public void updateList() {
        mAssetList.clear();

        StandardListBO standardListBO = mBModel.productHelper.getInStoreLocation().get(mSelectedLocationIndex);
        ArrayList<AssetTrackingBO> mAssetTrackingList = standardListBO.getAssetTrackingList();
        ArrayList<AssetTrackingBO> mAllAssetTrackingList = standardListBO.getAllAssetTrackingList();

        if (mAssetTrackingList != null && mAllAssetTrackingList != null
                && mAllAssetTrackingList.size() > 0) {

            for (AssetTrackingBO assetBO : mAssetTrackingList) {
                if (assetBO.getProductId() > 0 && mBModel.configurationMasterHelper.IS_GLOBAL_CATEGORY && !assetBO.getParentHierarchy().contains("/" + mBModel.productHelper.getmSelectedGlobalProductId() + "/"))
                    continue;
                if (ALL.equals(mCapturedBarcode)) {

                    if ("".equals(mCapturedNFCTag)) {
                        mAssetList.add(assetBO);
                    } else if (mCapturedNFCTag.equalsIgnoreCase(assetBO.getNFCTagId().replaceAll(":", ""))) {
                        assetBO.setAvailQty(1);
                        mAssetList.add(assetBO);
                    }

                } else if (mCapturedBarcode.equals(assetBO.getSerialNo())) {
                    assetBO.setScanComplete(1);
                    assetBO.setAvailQty(1);
                    mAssetList.add(assetBO);
                } else {
                    mAllAssetTrackingList.remove(assetBO);
                }
            }
        }

        Bundle bundle = null;
        boolean isUnmapped = false;


        mAssetView.updateAssets(mAssetList, isUnmapped, bundle);
    }

    @Override
    public void updateFiveFilter(int mProductId, HashMap<Integer, Integer> mSelectedIdByLevelId, ArrayList<Integer> mAttributeProducts, String mFilterText) {

        try {
            mAssetList.clear();
            ArrayList<AssetTrackingBO> mAssetTrackingList = mBModel.productHelper.getInStoreLocation().get(mSelectedLocationIndex).getAssetTrackingList();
            if (mAssetTrackingList != null
                    && mAssetTrackingList.size() > 0) {
                if (mAttributeProducts != null && mProductId != 0) {//Both Product and attribute filter selected
                    for (AssetTrackingBO assetBO : mAssetTrackingList) {
                        if (assetBO.getProductId() > 0 && mBModel.configurationMasterHelper.IS_GLOBAL_CATEGORY && !assetBO.getParentHierarchy().contains("/" + mBModel.productHelper.getmSelectedGlobalProductId() + "/"))
                            continue;
                        if (assetBO.getParentHierarchy().contains("/" + mProductId + "/")) {

                            if (ALL.equals(mCapturedBarcode)) {
                                if ("".equals(mCapturedNFCTag)) {
                                    if (mAttributeProducts.contains(assetBO.getProductId())) {
                                        mAssetList.add(assetBO);
                                    }
                                } else if (mCapturedNFCTag.equalsIgnoreCase(assetBO.getNFCTagId().replaceAll(":", ""))) {
                                    assetBO.setAvailQty(1);
                                    mAssetList.add(assetBO);
                                }
                            } else if (mCapturedBarcode.equals(assetBO.getSerialNo())) {
                                mAssetList.add(assetBO);
                            }
                        }
                    }
                } else if (mAttributeProducts == null && mProductId != 0) {// product filter alone selected
                    if (mSelectedIdByLevelId.size() == 0 || AppUtils.isMapEmpty(mSelectedIdByLevelId)) {
                        mAssetList.addAll(mAssetTrackingList);
                    } else {
                        for (AssetTrackingBO assetBO : mAssetTrackingList) {
                            if (assetBO.getProductId() > 0 && mBModel.configurationMasterHelper.IS_GLOBAL_CATEGORY && !assetBO.getParentHierarchy().contains("/" + mBModel.productHelper.getmSelectedGlobalProductId() + "/"))
                                continue;
                            if (assetBO.getParentHierarchy() != null && assetBO.getParentHierarchy().contains("/" + mProductId + "/")) {

                                if (ALL.equals(mCapturedBarcode)) {
                                    if ("".equals(mCapturedNFCTag)) {
                                        mAssetList.add(assetBO);

                                    } else if (mCapturedNFCTag.equalsIgnoreCase(assetBO.getNFCTagId().replaceAll(":", ""))) {
                                        assetBO.setAvailQty(1);
                                        mAssetList.add(assetBO);
                                    }
                                } else if (mCapturedBarcode.equals(assetBO.getSerialNo())) {
                                    mAssetList.add(assetBO);
                                }
                            }
                        }
                    }
                } else if (mAttributeProducts != null && mProductId != 0) {// Attribute filter alone selected
                    for (int pid : mAttributeProducts) {
                        for (AssetTrackingBO assetBO : mAssetTrackingList) {
                            if (assetBO.getProductId() > 0 && mBModel.configurationMasterHelper.IS_GLOBAL_CATEGORY && !assetBO.getParentHierarchy().contains("/" + mBModel.productHelper.getmSelectedGlobalProductId() + "/"))
                                continue;
                            if (pid == assetBO.getProductId()) {

                                if (ALL.equals(mCapturedBarcode)) {
                                    if ("".equals(mCapturedNFCTag)) {
                                        mAssetList.add(assetBO);
                                    } else if (mCapturedNFCTag.equalsIgnoreCase(assetBO.getNFCTagId().replaceAll(":", ""))) {
                                        assetBO.setAvailQty(1);
                                        mAssetList.add(assetBO);
                                    }
                                } else if (mCapturedBarcode.equals(assetBO.getSerialNo())) {
                                    mAssetList.add(assetBO);
                                }
                            }
                        }
                    }
                } else {

                    if (mFilterText.equals("")) {
                        for (AssetTrackingBO assetBO : mAssetTrackingList) {
                            if (assetBO.getProductId() > 0 && mBModel.configurationMasterHelper.IS_GLOBAL_CATEGORY && !assetBO.getParentHierarchy().contains("/" + mBModel.productHelper.getmSelectedGlobalProductId() + "/"))
                                continue;
                            if (ALL.equals(mCapturedBarcode)) {
                                if ("".equals(mCapturedNFCTag)) {
                                    mAssetList.add(assetBO);

                                } else if (mCapturedNFCTag.equalsIgnoreCase(assetBO.getNFCTagId().replaceAll(":", ""))) {
                                    assetBO.setAvailQty(1);
                                    mAssetList.add(assetBO);
                                }
                            } else if (mCapturedBarcode.equals(assetBO.getSerialNo())) {
                                mAssetList.add(assetBO);
                            }
                        }
                    }
                }
            }

            mAssetView.updateFiveFilteredList(mAssetList);
        } catch (Exception ex) {
            Commons.printException(ex);
        }
    }


    @Override
    public void updateImageName() {

        String imagePath = "Asset/"
                + mBModel.userMasterHelper.getUserMasterBO().getDownloadDate()
                .replace("/", "") + "/"
                + mBModel.userMasterHelper.getUserMasterBO().getUserid() + "/" + mSelectedImageName;

        if (mSelectedAssetID != 0) {
            for (AssetTrackingBO assetBO : mAssetList) {
                if (mSelectedAssetID == assetBO.getAssetID() &&
                        mSelectedSerial.equals(assetBO.getSerialNo())) {
                    assetBO.setImageName(imagePath);
                    assetBO.setImgName(mSelectedImageName);
                    ArrayList<String> imageList = assetBO.getImageList();
                    imageList.add(imagePath);
                    assetBO.setImageList(imageList);
                    break;
                }
            }
        }

    }

    private void deleteUnUsedImages() {
        if (mAssetList != null)
            for (AssetTrackingBO temp : mAssetList) {
                if (temp.getAvailQty() == 0 && !"".equals(temp.getImgName())) {
                    String fileName = temp.getImgName();
                    deleteFiles(fileName);
                }
            }
    }


    /**
     * Deleting image files
     *
     * @param filename File Name
     */
    private void deleteFiles(String filename) {

        File folder = new File(FileUtils.photoFolderPath + "/");
        File[] files = folder.listFiles();
        for (File tempFile : files) {
            if (tempFile != null && tempFile.getName().equals(filename)) {
                boolean isDeleted = tempFile.delete();
                if (isDeleted)
                    Commons.print("Image Delete," + "Success");
            }
        }
    }

    @Override
    public void removeExistingImage(String mAssetId, String imageNameStarts, String photoPath) {
        for (AssetTrackingBO assetBO : mAssetList) {
            if (mAssetId.equals(Integer.toString(assetBO.getAssetID()))) {
                assetBO.setImageName("");
            }
        }
        mAssetTrackingHelper.deleteImageName(mContext.getApplicationContext(), imageNameStarts);
        mBModel.synchronizationHelper.deleteFiles(photoPath,
                imageNameStarts);
    }

    @Override
    public void checkDataExistToSave() {
        //mAssetView.isDataExistToSave(hasAssetTaken(), hasAssetPhotoTaken(), hasAssetReasonTaken(), errorMsg);

        if (!validateAsset()) {
            mAssetView.showError(errorMsg);
        } else {
            mAssetView.save();
        }

    }

    /**
     * Method to check Asset already taken or not
     *
     * @return true if asset already taken
     */
    String errorMsg = "";

    /**
     * Method to check Asset reason taken or not
     *
     * @return true if asset validation successful
     */
    private boolean validateAsset() {
        ArrayList<AssetTrackingBO> mAssetTrackingList;
        boolean isFlag = true;
        for (StandardListBO standardListBO : mBModel.productHelper.getInStoreLocation()) {
            mAssetTrackingList = standardListBO.getAssetTrackingList();
            if (mAssetTrackingList != null
                    && mAssetTrackingList.size() > 0) {
                for (AssetTrackingBO assetBO : mAssetTrackingList) {
                    if (mAssetTrackingHelper.SHOW_ASSET_REASON && (assetBO.getReason1ID().equals(Integer.toString(0))
                            && assetBO.getAvailQty() == 0 && assetBO.getAudit() == 2
                            && assetBO.getCompetitorQty() == 0 && assetBO.getExecutorQty() == 0)) {
                        isFlag = false;
                        errorMsg = mContext.getString(R.string.please_provide_valid_reason_or_Availability);
                        break;
                    } else if (mBModel.configurationMasterHelper.ASSET_PHOTO_VALIDATION && (assetBO.getAvailQty() == 1 && (assetBO.getImageName().equals("") && assetBO.getImgName().equals("")))) {
                        isFlag = false;
                        errorMsg = mContext.getString(R.string.photo_mandatory);
                        break;
                    } else if (mBModel.configurationMasterHelper.ASSET_PHOTO_VALIDATION && (assetBO.getAvailQty() == 1 && (!assetBO.getImageName().equals("") || !assetBO.getImgName().equals("")))
                            && assetBO.getConditionID().equals("0")) {
                        isFlag = false;
                        errorMsg = mContext.getString(R.string.condition_mandatory);
                        break;
                    }
                }
            }
        }
        return isFlag;
    }


}
