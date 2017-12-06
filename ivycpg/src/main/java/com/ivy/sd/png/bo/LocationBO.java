package com.ivy.sd.png.bo;

import com.ivy.countersales.bo.CS_StockReasonBO;
import com.ivy.cpg.view.nearexpiry.NearExpiryDateBO;

import java.util.ArrayList;

public class LocationBO {

    int audit = 2;
    private int locationId;
    private int mShelfPiece = -1, mShelfCase = -1, mShelfOuter = -1;
    private int mWHPiece;
    private int mWHCase;
    private int mWHOuter;
    private int mwHFacing;
    private int mSIH;
    private int isPouring;
    private int cockTailQty;
    private boolean hasData = false;
    private int LocId;
    private int parentId;
    private String actual = "0", parentTotal = "0";
    private String gap = "0", target = "0", percentage = "0";
    private String imageName = "";
    private String LocCode, LocName;
    private int reasonId;
    private String remarks = "";
    private String imagepath = "";
    private String fromDate = "";
    private String toDate = "";
    private String productName;
    private String skuname = "";
    private String abv = "";
    private String feedback = "";
    private int productID;
    private int photoid;

    ArrayList<CS_StockReasonBO> lstStockReasons;

    private ArrayList<NearExpiryDateBO> nearexpiryDate;

    public LocationBO(LocationBO locObj) {
        this.locationId = locObj.locationId;
        this.mShelfPiece = locObj.mShelfPiece;
        this.mShelfCase = locObj.mShelfCase;
        this.mShelfOuter = locObj.mShelfOuter;
        this.mWHPiece = locObj.mWHPiece;
        this.mWHCase = locObj.mWHCase;
        this.mWHOuter = locObj.mWHOuter;
        this.fromDate = locObj.getFromDate();
        this.toDate = locObj.getToDate();
        this.productID = locObj.getProductID();
        this.productName = locObj.getProductName();
        this.imagepath = locObj.getImagepath();
        this.photoid = locObj.getPhotoid();
        this.skuname = locObj.getSkuname();
        this.abv = locObj.getAbv();
        this.feedback = locObj.getFeedback();

        this.isPouring = locObj.getIsPouring();
        this.cockTailQty = locObj.getCockTailQty();
    }

    public LocationBO() {

    }

    public LocationBO(int locid, String LocName) {
        super();
        this.LocId = locid;
        this.LocName = LocName;
    }

    public int getLocId() {
        return LocId;
    }

    public void setLocId(int locId) {
        LocId = locId;
    }

    public String getLocCode() {
        return LocCode;
    }

    public void setLocCode(String locCode) {
        LocCode = locCode;
    }

    public String getLocName() {
        return LocName;
    }

    public void setLocName(String locName) {
        LocName = locName;
    }

    public int getmSIH() {
        return mSIH;
    }

    public void setmSIH(int mSIH) {
        this.mSIH = mSIH;
    }

    public int getFacingQty() {
        return mwHFacing;
    }

    public void setFacingQty(int mwHFacing) {
        this.mwHFacing = mwHFacing;
    }

    public int getLocationId() {
        return locationId;
    }

    public void setLocationId(int locationId) {
        this.locationId = locationId;
    }

    public int getShelfPiece() {
        return mShelfPiece;
    }

    public void setShelfPiece(int mShelfPiece) {
        this.mShelfPiece = mShelfPiece;
    }

    public int getShelfCase() {
        return mShelfCase;
    }

    public void setShelfCase(int mShelfCase) {
        this.mShelfCase = mShelfCase;
    }

    public int getShelfOuter() {
        return mShelfOuter;
    }

    public void setShelfOuter(int mShelfOuter) {
        this.mShelfOuter = mShelfOuter;
    }

    public int getWHPiece() {
        return mWHPiece;
    }

    public void setWHPiece(int mWHPiece) {
        this.mWHPiece = mWHPiece;
    }

    public int getWHCase() {
        return mWHCase;
    }

    public void setWHCase(int mWHCase) {
        this.mWHCase = mWHCase;
    }

    public int getWHOuter() {
        return mWHOuter;
    }

    public void setWHOuter(int mWHOuter) {
        this.mWHOuter = mWHOuter;
    }

    public ArrayList<NearExpiryDateBO> getNearexpiryDate() {
        return nearexpiryDate;
    }

    public void setNearexpiryDate(ArrayList<NearExpiryDateBO> nearexpiryDate) {
        this.nearexpiryDate = nearexpiryDate;
    }

    public boolean isHasData() {
        return hasData;
    }

    public void setHasData(boolean hasData) {
        this.hasData = hasData;
    }

    public String toString() {
        return LocName;
    }

    public int getParentId() {
        return parentId;
    }

    public void setParentId(int parentId) {
        this.parentId = parentId;
    }

    public int getAudit() {
        return audit;
    }

    public void setAudit(int audit) {
        this.audit = audit;
    }

    public int getReasonId() {
        return reasonId;
    }

    public void setReasonId(int reasonId) {
        this.reasonId = reasonId;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    public String getActual() {
        return actual;
    }

    public void setActual(String actual) {
        this.actual = actual;
    }

    public String getTarget() {
        return target;
    }

    public void setTarget(String target) {
        this.target = target;
    }

    public String getPercentage() {
        return percentage;
    }

    public void setPercentage(String percentage) {
        this.percentage = percentage;
    }

    public String getGap() {
        return gap;
    }

    public void setGap(String gap) {
        this.gap = gap;
    }

    public String getImageName() {
        return imageName;
    }

    public void setImageName(String imageName) {
        this.imageName = imageName;
    }

    public String getParentTotal() {
        return parentTotal;
    }

    public void setParentTotal(String parentTotal) {
        this.parentTotal = parentTotal;
    }

    public String getFromDate() {
        return fromDate;
    }

    public void setFromDate(String fromDate) {
        this.fromDate = fromDate;
    }

    public String getToDate() {
        return toDate;
    }

    public void setToDate(String toDate) {
        this.toDate = toDate;
    }

    public int getProductID() {
        return productID;
    }

    public void setProductID(int productID) {
        this.productID = productID;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getImagepath() {
        return imagepath;
    }

    public void setImagepath(String imagepath) {
        this.imagepath = imagepath;
    }

    public int getPhotoid() {
        return photoid;
    }

    public void setPhotoid(int photoid) {
        this.photoid = photoid;
    }

    public String getSkuname() {
        return skuname;
    }

    public void setSkuname(String skuname) {
        this.skuname = skuname;
    }

    public String getAbv() {
        return abv;
    }

    public void setAbv(String abv) {
        this.abv = abv;
    }

    public String getFeedback() {
        return feedback;
    }

    public void setFeedback(String feedback) {
        this.feedback = feedback;
    }

    public int getIsPouring() {
        return isPouring;
    }

    public void setIsPouring(int isPouring) {
        this.isPouring = isPouring;
    }

    public int getCockTailQty() {
        return cockTailQty;
    }

    public void setCockTailQty(int cockTailQty) {
        this.cockTailQty = cockTailQty;
    }

    public ArrayList<CS_StockReasonBO> getLstStockReasons() {
        return lstStockReasons;
    }

    public void setLstStockReasons(ArrayList<CS_StockReasonBO> lstStockReasons) {
        this.lstStockReasons = lstStockReasons;
    }

}
