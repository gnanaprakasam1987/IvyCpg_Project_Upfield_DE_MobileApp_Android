package com.ivy.cpg.view.serializedAsset;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;

import com.ivy.cpg.view.survey.SurveyHelperNew;
import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.bo.ReasonMaster;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.util.Commons;
import com.ivy.utils.AppUtils;
import com.ivy.utils.DateTimeUtils;
import com.ivy.utils.FileUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by rajkumar.s on 11/20/2017.
 */

public class SerializedAssetPresenterImpl implements SerializedAssetContractor.SerializedAssetPresenter {

    BusinessModel mBModel;
    SerializedAssetHelper mAssetTrackingHelper;
    SerializedAssetContractor.SerializedAssetView mAssetView;

    private static final String ALL = "ALL";
    private String mCapturedBarcode = "ALL";
    private String mCapturedNFCTag = "";
    ArrayList<SerializedAssetBO> mAssetList = new ArrayList<>();

    public int mSelectedAssetID = 0;
    public String mSelectedImageName = "";
    public String mSelectedSerial = "";
    String photoPath = "";
    private Context mContext;

    public SerializedAssetPresenterImpl(Context mContext, BusinessModel mBModel, SerializedAssetHelper mAssetTrackingHelper) {
        this.mBModel = mBModel;
        this.mAssetTrackingHelper = mAssetTrackingHelper;
        this.mContext = mContext;
    }

    @Override
    public void setView(SerializedAssetContractor.SerializedAssetView mAssetView) {
        this.mAssetView = mAssetView;
    }


    @Override
    public void save(String mModuleCode) {
        new AssetAsyncTask(mModuleCode, null, null).execute();
    }

    private class AssetAsyncTask extends AsyncTask<String, Void, String> {
        private String moduleCode;
        private String assetType;
        private String serialNo;

        public AssetAsyncTask(String moduleCode, String assetType, String serialNo) {
            this.moduleCode = moduleCode;
            this.assetType = assetType;
            this.serialNo = serialNo;
        }

        protected void onPreExecute() {
        }

        @Override
        protected String doInBackground(String... params) {
            if (moduleCode.equalsIgnoreCase("MENU_SERIALIZED_ASSET")) {
                deleteUnUsedImages();
                mAssetTrackingHelper.saveAsset(mContext.getApplicationContext(), moduleCode);
                mBModel.saveModuleCompletion(moduleCode, true);
            } else {
                SurveyHelperNew surveyHelperNew = SurveyHelperNew.getInstance(mContext);
                surveyHelperNew.loadSurveyConfig("OTHERS");
                surveyHelperNew.setFromHomeScreen(false);
                surveyHelperNew.downloadModuleId((moduleCode.toUpperCase()));
                surveyHelperNew.downloadQuestionDetails(moduleCode, assetType);
                surveyHelperNew.loadSurveyAnswers(0, serialNo);
            }
            return "";
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            if (!moduleCode.equalsIgnoreCase("MENU_SERIALIZED_ASSET")) {
                mAssetView.navigateSurveyScreen(moduleCode, assetType, serialNo);
            } else {
                mAssetView.cancelProgressDialog();
            }


        }

    }

    @Override
    public void updateTimeStamp() {
        mBModel.outletTimeStampHelper.updateTimeStampModuleWise(DateTimeUtils.now(DateTimeUtils.TIME));
    }

    @Override
    public void fetchAssetSurveyData(String moduleCode, String assetType, String serialNo) {
        new AssetAsyncTask(moduleCode, assetType, serialNo).execute();
    }

    @Override
    public void deleteAssetSurvey(String serialNo) {
        if (serialNo == null) {
            new CompositeDisposable().add(mAssetTrackingHelper.deleteAssetSurvey(mContext, mAssetView.getAssetSerialNoList())
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Consumer<Boolean>() {
                        @Override
                        public void accept(Boolean isDeleted) throws Exception {

                        }
                    }));
        } else {
            new CompositeDisposable().add(mAssetTrackingHelper.deleteAssetSurvey(mContext, serialNo)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Consumer<Boolean>() {
                        @Override
                        public void accept(Boolean isDeleted) throws Exception {
                            if (isDeleted)
                                mAssetView.updateListAdapter();
                        }
                    }));
        }
    }

    @Override
    public void initialLoad() {
        mAssetView.updateInitialLoad();
    }

    @Override
    public void setBarcode(String mBarcode) {
        mCapturedBarcode = mBarcode;
    }


    @Override
    public void updateLocationIndex(int mIndex) {

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


        if (mAssetTrackingHelper.getAssetTrackingList() != null && mAssetTrackingHelper.getAllAssetTrackingList() != null
                && mAssetTrackingHelper.getAllAssetTrackingList().size() > 0) {

            for (SerializedAssetBO assetBO : mAssetTrackingHelper.getAssetTrackingList()) {
                if (assetBO.getParentHierarchy() != null && mBModel.configurationMasterHelper.IS_GLOBAL_CATEGORY && !assetBO.getParentHierarchy().contains("/" + mBModel.productHelper.getmSelectedGlobalProductId() + "/"))
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
                    mAssetTrackingHelper.getAllAssetTrackingList().remove(assetBO);
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
            ArrayList<SerializedAssetBO> mAssetTrackingList = mAssetTrackingHelper.getAssetTrackingList();
            if (mAssetTrackingList != null
                    && mAssetTrackingList.size() > 0) {
                if (mAttributeProducts != null && mProductId != 0) {//Both Product and attribute filter selected
                    for (SerializedAssetBO assetBO : mAssetTrackingList) {
                        if (assetBO.getParentHierarchy() != null && mBModel.configurationMasterHelper.IS_GLOBAL_CATEGORY && !assetBO.getParentHierarchy().contains("/" + mBModel.productHelper.getmSelectedGlobalProductId() + "/"))
                            continue;
                        if (assetBO.getParentHierarchy().contains("/" + mProductId + "/")) {

                            if (ALL.equals(mCapturedBarcode)) {
                                if ("".equals(mCapturedNFCTag)) {
                                    if (checkAsset(mAttributeProducts, assetBO.getParentHierarchy()))
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
                } else if (mAttributeProducts == null && mProductId != 0) {// product filter alone selected
                    if (mSelectedIdByLevelId.size() == 0 || AppUtils.isMapEmpty(mSelectedIdByLevelId)) {
                        if (mBModel.configurationMasterHelper.IS_GLOBAL_CATEGORY)
                            for (SerializedAssetBO assetBO : mAssetTrackingList) {
                                if (assetBO.getParentHierarchy() != null && !assetBO.getParentHierarchy().contains("/" + mBModel.productHelper.getmSelectedGlobalProductId() + "/"))
                                    continue;
                                mAssetList.add(assetBO);
                            }
                        mAssetList.addAll(mAssetTrackingList);
                    } else {
                        for (SerializedAssetBO assetBO : mAssetTrackingList) {
                            if (assetBO.getParentHierarchy() != null && mBModel.configurationMasterHelper.IS_GLOBAL_CATEGORY && !assetBO.getParentHierarchy().contains("/" + mBModel.productHelper.getmSelectedGlobalProductId() + "/"))
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
                        for (SerializedAssetBO assetBO : mAssetTrackingList) {
                            if (assetBO.getParentHierarchy() != null && mBModel.configurationMasterHelper.IS_GLOBAL_CATEGORY && !assetBO.getParentHierarchy().contains("/" + mBModel.productHelper.getmSelectedGlobalProductId() + "/"))
                                continue;
                            if (assetBO.getParentHierarchy().contains("/" + pid + "/")) {

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
                        for (SerializedAssetBO assetBO : mAssetTrackingList) {
                            if (assetBO.getParentHierarchy() != null && mBModel.configurationMasterHelper.IS_GLOBAL_CATEGORY && !assetBO.getParentHierarchy().contains("/" + mBModel.productHelper.getmSelectedGlobalProductId() + "/"))
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

    private boolean checkAsset(ArrayList<Integer> mAttributeProducts, String parentHierarchy) {
        for (Integer productId : mAttributeProducts) {
            if (parentHierarchy.contains("/" + productId + "/")) {
                return true;
            }
        }
        return false;
    }


    @Override
    public void updateImageName() {

        String imagePath = "Asset/"
                + mBModel.userMasterHelper.getUserMasterBO().getDownloadDate()
                .replace("/", "") + "/"
                + mBModel.userMasterHelper.getUserMasterBO().getUserid() + "/" + mSelectedImageName;

        if (mSelectedAssetID != 0) {
            for (SerializedAssetBO assetBO : mAssetList) {
                if (mSelectedAssetID == assetBO.getAssetID() &&
                        mSelectedSerial.equals(assetBO.getSerialNo())) {
                    assetBO.setImageName(imagePath);
                    assetBO.setImgName(mSelectedImageName);
                    ArrayList<String> imageList = new ArrayList<>();//assetBO.getImageList();
                    imageList.add(imagePath);
                    assetBO.setImageList(imageList);
                    break;
                }
            }
        }

    }

    private void deleteUnUsedImages() {
        if (mAssetList != null)
            for (SerializedAssetBO temp : mAssetList) {
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
        for (SerializedAssetBO assetBO : mAssetList) {
            if (mAssetId.equals(Integer.toString(assetBO.getAssetID()))) {
                assetBO.setImageName("");
            }
        }
        mAssetTrackingHelper.deleteImageProof(mContext.getApplicationContext(), imageNameStarts);
        mBModel.synchronizationHelper.deleteFiles(photoPath,
                imageNameStarts);
    }

    @Override
    public void checkDataExistToSave() {

        if (!hasAssetPhotoTaken() || !hasAssetReasonTaken() || !hasAssetSurveyTaken()) {
            mAssetView.showError(errorMsg);
        } else if (hasAssetTaken() || hasAssetPhotoTaken()
                || hasAssetReasonTaken() || hasAssetSurveyTaken()) {
            mAssetView.save();
        }

    }

    /**
     * Method to check Asset already taken or not
     *
     * @return true if asset already taken
     */
    String errorMsg = "";

    private boolean hasAssetTaken() {
        ArrayList<SerializedAssetBO> mAssetTrackingList = mAssetTrackingHelper.getAssetTrackingList();

        if (mAssetTrackingList != null
                && mAssetTrackingList.size() > 0) {
            for (SerializedAssetBO assetBO : mAssetTrackingList) {
                if (assetBO.getAvailQty() > 0 || assetBO.getAudit() != 2
                        || assetBO.getCompetitorQty() > 0 || assetBO.getExecutorQty() > 0 || (!assetBO.getReason1ID().equalsIgnoreCase("0"))) {
                    return true;
                }
            }
        }
        return false;
    }


    /**
     * Method to check Asset Photo already taken or not
     *
     * @return true if asset photo taken
     */
    private boolean hasAssetReasonTaken() {
        ArrayList<SerializedAssetBO> mAssetTrackingList = mAssetTrackingHelper.getAssetTrackingList();
        boolean isFlag = true;
        if (mAssetTrackingHelper.SHOW_ASSET_REASON) {

            if (mAssetTrackingList != null
                    && mAssetTrackingList.size() > 0) {
                for (SerializedAssetBO assetBO : mAssetTrackingList) {
                    if (assetBO.getReason1ID().equals(Integer.toString(0))
                            && assetBO.getAvailQty() == 0 && assetBO.getAudit() == 2
                            && assetBO.getCompetitorQty() == 0 && assetBO.getExecutorQty() == 0) {
                        isFlag = false;
                        errorMsg = mContext.getString(R.string.please_provide_valid_reason_or_Availability);
                    }
                }
            }
        }
        return isFlag;
    }

    /**
     * Method to check Asset reason taken or not
     *
     * @return true if asset reason already taken
     */
    private boolean hasAssetPhotoTaken() {
        ArrayList<SerializedAssetBO> mAssetTrackingList = mAssetTrackingHelper.getAssetTrackingList();
        boolean isFlag = true;
        if (mAssetTrackingHelper.ASSET_PHOTO_VALIDATION) {
            if (mAssetTrackingList != null
                    && mAssetTrackingList.size() > 0) {
                for (SerializedAssetBO assetBO : mAssetTrackingList) {
                    if (assetBO.getAvailQty() > 0 && (assetBO.getImgName().equals("") || assetBO.getImgName().equals(""))) {
                        isFlag = false;
                        errorMsg = mContext.getString(R.string.photo_mandatory);
                    }
                }
            }
        }
        return isFlag;
    }

    /**
     * Method to check Asset survey taken or not
     *
     * @return true if asset survey already taken
     */
    private boolean hasAssetSurveyTaken() {
        ArrayList<SerializedAssetBO> mAssetTrackingList = mAssetTrackingHelper.getAssetTrackingList();
        boolean isFlag = true;
        if (mAssetTrackingHelper.SHOW_ASSET_SURVEY_OPTION) {
            if (mAssetTrackingList != null
                    && mAssetTrackingList.size() > 0) {
                for (SerializedAssetBO assetBO : mAssetTrackingList) {
                    if (assetBO.getAvailQty() > 0 && !assetBO.isSurveyDone()) {
                        isFlag = false;
                        errorMsg = mContext.getString(R.string.survey_mandatory);
                    }
                }
            }
        }
        return isFlag;
    }


}
