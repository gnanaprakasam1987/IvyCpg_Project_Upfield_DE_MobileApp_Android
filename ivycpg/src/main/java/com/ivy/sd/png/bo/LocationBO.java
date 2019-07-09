package com.ivy.sd.png.bo;


import androidx.annotation.NonNull;

import com.ivy.cpg.view.nearexpiry.NearExpiryDateBO;

import java.util.ArrayList;

public class LocationBO implements Comparable {

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
    private int availability = -1;
    private int priceChanged;
    private int priceCompliance;



    private int priceTagAvailability;

    private String price_ca = "0";
    private String price_pc = "0";
    private String price_oo = "0";

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

    public int getAvailability() {
        return availability;
    }

    public void setAvailability(int availability) {
        this.availability = availability;
    }

    public String getPrice_ca() {
        return price_ca;
    }

    public void setPrice_ca(String price_ca) {
        this.price_ca = price_ca;
    }

    public String getPrice_pc() {
        return price_pc;
    }

    public void setPrice_pc(String price_pc) {
        this.price_pc = price_pc;
    }

    public String getPrice_oo() {
        return price_oo;
    }

    public void setPrice_oo(String price_oo) {
        this.price_oo = price_oo;
    }

    public void setPriceChanged(int priceChanged) {
        this.priceChanged = priceChanged;
    }

    public int getPriceChanged() {
        return priceChanged;
    }

    public int getPriceCompliance() {
        return priceCompliance;
    }

    public void setPriceCompliance(int priceCompliance) {
        this.priceCompliance = priceCompliance;
    }

    private String priceChangeReasonID = "0";


    public String getMrp_ca() {
        return mrp_ca;
    }

    public void setMrp_ca(String mrp_ca) {
        this.mrp_ca = mrp_ca;
    }

    public String getMrp_ou() {
        return mrp_ou;
    }

    public void setMrp_ou(String mrp_ou) {
        this.mrp_ou = mrp_ou;
    }

    public String getMrp_pc() {
        return mrp_pc;
    }

    public void setMrp_pc(String mrp_pc) {
        this.mrp_pc = mrp_pc;
    }

    private String mrp_ca = "0", mrp_ou = "0", mrp_pc = "0";

    public String getPriceChangeReasonID() {
        return priceChangeReasonID;
    }

    public void setPriceChangeReasonID(String priceChangeReasonID) {
        this.priceChangeReasonID = priceChangeReasonID;
    }

    public String getPrevPrice_ca() {
        return prevPrice_ca;
    }

    public void setPrevPrice_ca(String prevPrice_ca) {
        this.prevPrice_ca = prevPrice_ca;
    }

    public String getPrevPrice_pc() {
        return prevPrice_pc;
    }

    public void setPrevPrice_pc(String prevPrice_pc) {
        this.prevPrice_pc = prevPrice_pc;
    }

    public String getPrevPrice_oo() {
        return prevPrice_oo;
    }

    public void setPrevPrice_oo(String prevPrice_oo) {
        this.prevPrice_oo = prevPrice_oo;
    }

    private String prevPrice_ca = "0";
    private String prevPrice_pc = "0";
    private String prevPrice_oo = "0";

    public int getPriceTagAvailability() {
        return priceTagAvailability;
    }

    public void setPriceTagAvailability(int priceTagAvailability) {
        this.priceTagAvailability = priceTagAvailability;
    }


    @Override
    public int compareTo(@NonNull Object o) {
        int compareAvailability=((LocationBO)o).getAvailability();
        /* For Ascending order*/
        return this.availability - compareAvailability;
    }
}
