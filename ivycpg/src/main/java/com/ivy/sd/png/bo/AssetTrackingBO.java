package com.ivy.sd.png.bo;

public class AssetTrackingBO {
    private int assetID;
    private int Target;
    private int audit = 2;
    private int competitorQty;
    private int executorQty;
    private String reason1ID;
    private String remarkID;
    private String reasonDesc;
    private String conditionID = "0";
    private String minstalldate;
    private String mservicedate;
    private String mposm;
    private String mbrand;
    private String mnewinstaldate;
    private String msno,mreasonId,mremarks,mToRetailerId;
    private String mposmname;
    private String mflag;
    private String msbdid;
    private String groupLevelName;
    private String mNFCTagId;
    private int groupLevelId;
    private int availQty,scanComplete=0;
    private String imageName = "";
    private String AssetName;
    private String SerialNo = "0";
    private int productid;
    private boolean isSelectedToRemove;

    public AssetTrackingBO() {

    }

    public AssetTrackingBO(AssetTrackingBO assetTrackingBO) {
        this.assetID = assetTrackingBO.getAssetID();
        this.Target = assetTrackingBO.getTarget();
        this.AssetName = assetTrackingBO.getAssetName();
        this.audit = assetTrackingBO.getAudit();
        this.availQty = assetTrackingBO.getAvailQty();
        this.scanComplete=assetTrackingBO.getscanComplete();
        this.competitorQty = assetTrackingBO.getCompetitorQty();
        this.conditionID = assetTrackingBO.getConditionID();
        this.imageName = assetTrackingBO.getImageName();
        this.mbrand = assetTrackingBO.getMbrand();
        this.minstalldate = assetTrackingBO.getMinstalldate();
        this.mnewinstaldate = assetTrackingBO.getMnewinstaldate();
        this.mposm = assetTrackingBO.getMposm();
        this.msbdid = assetTrackingBO.getMsbdid();
        this.mservicedate = assetTrackingBO.getMservicedate();
        this.mposmname = assetTrackingBO.getMposmname();
        this.mflag = assetTrackingBO.getMflag();
        this.productid = assetTrackingBO.getProductid();
        this.msno = assetTrackingBO.getMsno();
        this.reason1ID = assetTrackingBO.getReason1ID();
        this.reasonDesc = assetTrackingBO.getReasonDesc();
        this.groupLevelId = assetTrackingBO.getGroupLevelId();
        this.groupLevelName = assetTrackingBO.getGroupLevelName();
        this.executorQty = assetTrackingBO.getExecutorQty();
        this.SerialNo = assetTrackingBO.getSerialNo();
        this.mNFCTagId = assetTrackingBO.getNFCTagId();
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

    public String getMsbdid() {
        return msbdid;
    }

    public void setMsbdid(String msbdid) {
        this.msbdid = msbdid;
    }

    public String getMposmname() {
        return mposmname;
    }

    public void setMposmname(String mposmname) {
        this.mposmname = mposmname;
    }

    public String getMflag() {
        return mflag;
    }

    public void setMflag(String mflag) {
        this.mflag = mflag;
    }

    public String getConditionID() {
        return conditionID;
    }

    public void setConditionID(String conditionID) {
        this.conditionID = conditionID;
    }

    public String getMposm() {
        return mposm;
    }

    public void setMposm(String mposm) {
        this.mposm = mposm;
    }

    public String getMbrand() {
        return mbrand;
    }

    public void setMbrand(String mbrand) {
        this.mbrand = mbrand;
    }

    public String getMnewinstaldate() {
        return mnewinstaldate;
    }

    public void setMnewinstaldate(String minstaldate) {
        this.mnewinstaldate = minstaldate;
    }

    public String getMsno() {
        return msno;
    }

    public void setMsno(String msno) {
        this.msno = msno;
    }
    public String getMreasonId() {
        return mreasonId;
    }

    public void setMreasonId(String mreasonId) {
        this.mreasonId = mreasonId;
    }
    public String getMremarks() {
        return mremarks;
    }

    public void setMremarks(String mremarks) {
        this.mremarks = mremarks;
    }

    public String getmToRetailerId() {
        return mToRetailerId;
    }

    public void setmToRetailerId(String mToRetailerId) {
        this.mToRetailerId = mToRetailerId;
    }

    public String getMinstalldate() {
        return minstalldate;
    }

    public void setMinstalldate(String minstalldate) {
        this.minstalldate = minstalldate;
    }

    public String getMservicedate() {
        return mservicedate;
    }

    public void setMservicedate(String mservicedate) {
        this.mservicedate = mservicedate;
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

    public String getRemarkID() {
        return remarkID;
    }

    public void setRemarkID(String remarkID) {
        this.remarkID = remarkID;
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
    public int getscanComplete() {
        return scanComplete;
    }

    public void setscanComplete(int scanComplete) {
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

    public int getProductid() {
        return productid;
    }

    public void setProductid(int productid) {
        this.productid = productid;
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
}
