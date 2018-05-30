package com.ivy.sd.png.bo.asset;

import java.util.ArrayList;

public class AssetTrackingBO {
    private int assetID;
    private int Target;
    private int audit = 2;
    private int competitorQty;
    private int executorQty;
    private String reason1ID = "0";
    private String reasonDesc;
    private String conditionID = "0";
    private String mInstallDate;
    private String mServiceDate;
    private String mPOSM;
    private String mBrand;
    private String mNewInstallDate;
    private String mSNO, mReasonId = "0", mRemarks = "", mToRetailerId;
    private String mPOSMName;
    private String mFlag;
    private String mSBDId;
    private String groupLevelName;
    private String mNFCTagId;
    private int groupLevelId;
    private int availQty, scanComplete = 0;
    private String imageName = "";
    private String AssetName;
    private String SerialNo = "0";
    private int mProductId;
    private boolean isSelectedToRemove;
    private int targetLocId;
    private String locationName;
    private ArrayList<String> imageList;

    public AssetTrackingBO() {

    }

    public AssetTrackingBO(AssetTrackingBO assetTrackingBO) {
        this.assetID = assetTrackingBO.getAssetID();
        this.Target = assetTrackingBO.getTarget();
        this.AssetName = assetTrackingBO.getAssetName();
        this.audit = assetTrackingBO.getAudit();
        this.availQty = assetTrackingBO.getAvailQty();
        this.scanComplete = assetTrackingBO.getScanComplete();
        this.competitorQty = assetTrackingBO.getCompetitorQty();
        this.conditionID = assetTrackingBO.getConditionID();
        this.imageName = assetTrackingBO.getImageName();
        this.imgName = assetTrackingBO.getImgName();
        this.mBrand = assetTrackingBO.getBrand();
        this.mInstallDate = assetTrackingBO.getInstallDate();
        this.mNewInstallDate = assetTrackingBO.getNewInstallDate();
        this.mPOSM = assetTrackingBO.getPOSM();
        this.mSBDId = assetTrackingBO.getSBDId();
        this.mServiceDate = assetTrackingBO.getServiceDate();
        this.mPOSMName = assetTrackingBO.getPOSMName();
        this.mFlag = assetTrackingBO.getFlag();
        this.mProductId = assetTrackingBO.getProductId();
        this.mSNO = assetTrackingBO.getSNO();
        this.reason1ID = assetTrackingBO.getReason1ID();
        this.reasonDesc = assetTrackingBO.getReasonDesc();
        this.groupLevelId = assetTrackingBO.getGroupLevelId();
        this.groupLevelName = assetTrackingBO.getGroupLevelName();
        this.executorQty = assetTrackingBO.getExecutorQty();
        this.SerialNo = assetTrackingBO.getSerialNo();
        this.mNFCTagId = assetTrackingBO.getNFCTagId();
        this.targetLocId = assetTrackingBO.getTargetLocId();
        this.locationName = assetTrackingBO.getLocationName();
        this.imageList = assetTrackingBO.getImageList();
    }

    public int getExecutorQty() {
        return executorQty;
    }

    public void setExecutorQty(int executorQty) {
        this.executorQty = executorQty;
    }

    public int getCompetitorQty() {
        return competitorQty;
    }

    public void setCompetitorQty(int competitorQty) {
        this.competitorQty = competitorQty;
    }

    public String getNFCTagId() {
        return mNFCTagId;
    }

    public void setNFCTagId(String mNFCTagId) {
        this.mNFCTagId = mNFCTagId;
    }

    public int getGroupLevelId() {
        return groupLevelId;
    }

    public void setGroupLevelId(int groupLevelId) {
        this.groupLevelId = groupLevelId;
    }

    public String getGroupLevelName() {
        return groupLevelName;
    }

    public void setGroupLevelName(String groupLevelName) {
        this.groupLevelName = groupLevelName;
    }

    public String getSBDId() {
        return mSBDId;
    }

    public void setSBDId(String mSBDId) {
        this.mSBDId = mSBDId;
    }

    public String getPOSMName() {
        return mPOSMName;
    }

    public void setPOSMName(String mPOSMName) {
        this.mPOSMName = mPOSMName;
    }

    public String getFlag() {
        return mFlag;
    }

    public void setFlag(String mFlag) {
        this.mFlag = mFlag;
    }

    public String getConditionID() {
        return conditionID;
    }

    public void setConditionID(String conditionID) {
        this.conditionID = conditionID;
    }

    public String getPOSM() {
        return mPOSM;
    }

    public void setPOSM(String mPOSM) {
        this.mPOSM = mPOSM;
    }

    public void setBrand(String mBrand) {
        this.mBrand = mBrand;
    }

    public String getBrand() {
        return mBrand;
    }

    public String getNewInstallDate() {
        return mNewInstallDate;
    }

    public void setNewInstallDate(String mInstallDate) {
        this.mNewInstallDate = mInstallDate;
    }

    public String getSNO() {
        return mSNO;
    }

    public void setSNO(String mSNO) {
        this.mSNO = mSNO;
    }

    public String getReasonId() {
        return mReasonId;
    }

    public void setReasonId(String mReasonId) {
        this.mReasonId = mReasonId;
    }

    public String getRemarks() {
        return mRemarks;
    }

    public void setRemarks(String mRemarks) {
        this.mRemarks = mRemarks;
    }

    public String getToRetailerId() {
        return mToRetailerId;
    }

    public void setToRetailerId(String mToRetailerId) {
        this.mToRetailerId = mToRetailerId;
    }

    public String getInstallDate() {
        return mInstallDate;
    }

    public void setInstallDate(String mInstallDate) {
        this.mInstallDate = mInstallDate;
    }

    public String getServiceDate() {
        return mServiceDate;
    }

    public void setServiceDate(String mServiceDate) {
        this.mServiceDate = mServiceDate;
    }

    private String getReasonDesc() {
        return reasonDesc;
    }

    public void setReasonDesc(String reasonDesc) {
        this.reasonDesc = reasonDesc;
    }

    public String getReason1ID() {
        return reason1ID;
    }

    public void setReason1ID(String reason1id) {
        reason1ID = reason1id;
    }

    public int getAssetID() {
        return assetID;
    }

    public void setAssetID(int assetID) {
        this.assetID = assetID;
    }

    public int getTarget() {
        return Target;
    }

    public void setTarget(int target) {
        Target = target;
    }

    public String getAssetName() {
        return AssetName;
    }

    public void setAssetName(String assetName) {
        AssetName = assetName;
    }

    public String getSerialNo() {
        return SerialNo;
    }

    public void setSerialNo(String serialNo) {
        SerialNo = serialNo;
    }

    public int getAvailQty() {
        return availQty;
    }

    public void setAvailQty(int availQty) {
        this.availQty = availQty;
    }

    public int getScanComplete() {
        return scanComplete;
    }

    public void setScanComplete(int scanComplete) {
        this.scanComplete = scanComplete;
    }

    public String getImageName() {
        return imageName;
    }

    public void setImageName(String imageName) {
        this.imageName = imageName;
    }

    public int getAudit() {
        return audit;
    }

    public void setAudit(int audit) {
        this.audit = audit;
    }

    public int getProductId() {
        return mProductId;
    }

    public void setProductId(int mProductId) {
        this.mProductId = mProductId;
    }

    public boolean isSelectedToRemove() {
        return isSelectedToRemove;
    }

    public void setSelectedToRemove(boolean selectedToRemove) {
        isSelectedToRemove = selectedToRemove;
    }

    //SOD Asset Columns
    private String isPromo = "N", isDisplay = "N";
    private int reasonID, locationID, actual;

    public String getIsPromo() {
        return isPromo;
    }

    public void setIsPromo(String isPromo) {
        this.isPromo = isPromo;
    }

    public String getIsDisplay() {
        return isDisplay;
    }

    public void setIsDisplay(String isDisplay) {
        this.isDisplay = isDisplay;
    }

    public int getActual() {
        return actual;
    }

    public void setActual(int actual) {
        this.actual = actual;
    }

    public int getReasonID() {
        return reasonID;
    }

    public void setReasonID(int reasonID) {
        this.reasonID = reasonID;
    }

    public int getLocationID() {
        return locationID;
    }

    public void setLocationID(int locationID) {
        this.locationID = locationID;
    }

    public String getImgName() {
        return imgName;
    }

    public void setImgName(String imgName) {
        this.imgName = imgName;
    }

    private String imgName = "";

    public int getTargetLocId() {
        return targetLocId;
    }

    public void setTargetLocId(int targetLocId) {
        this.targetLocId = targetLocId;
    }

    public String getLocationName() {
        return locationName;
    }

    public void setLocationName(String locationName) {
        this.locationName = locationName;
    }

    public ArrayList<String> getImageList() {
        if(imageList==null)
            return new ArrayList<String>();
        return imageList;
    }

    public void setImageList(ArrayList<String> imageList) {
        this.imageList = imageList;
    }
}
