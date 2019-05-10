package com.ivy.ui.photocapture.model;

import java.io.Serializable;

/**
 * Created by Rajkumar.S on 27/11/17.
 * Photo capture screen specific BO
 */

public class PhotoCaptureLocationBO implements Serializable{


    private String mImagePath = "";
    private String fromDate = "";
    private String toDate = "";
    private String productName;
    private String mSKUName = "";
    private String abv = "";
    private String mLotCode = "";
    private String mSequenceNO = "";
    private String feedback = "";
    private int productID;
    private String mTypeName ="";

    //Refactor field introduced to remove cloning of lists
    private int photoTypeId;

    public String getmTypeName() {
        return mTypeName;
    }

    public void setmTypeName(String mTypeName) {
        this.mTypeName = mTypeName;
    }

    public int getPhotoTypeId() {
        return photoTypeId;
    }

    public void setPhotoTypeId(int photoTypeId) {
        this.photoTypeId = photoTypeId;
    }

    public String getImageName() {
        return mImageName;
    }

    public void setImageName(String mImageName) {
        this.mImageName = mImageName;
    }

    private String mImageName = "";

    public int getLocationId() {
        return mLocationId;
    }

    public void setLocationId(int mLocationId) {
        this.mLocationId = mLocationId;
    }

    private int mLocationId;

    public String getLocationName() {
        return mLocationName;
    }

    public void setLocationName(String mLocationName) {
        this.mLocationName = mLocationName;
    }

    private String mLocationName;

    public PhotoCaptureLocationBO(PhotoCaptureLocationBO locObj) {
        this.mLocationId = locObj.mLocationId;
        this.fromDate = locObj.getFromDate();
        this.toDate = locObj.getToDate();
        this.productID = locObj.getProductID();
        this.productName = locObj.getProductName();
        this.mImagePath = locObj.getImagePath();
        this.mSKUName = locObj.getSKUName();
        this.abv = locObj.getAbv();
        this.mLotCode = locObj.getLotCode();
        this.mSequenceNO = locObj.getSequenceNO();
        this.feedback = locObj.getFeedback();
        this.mLocationName = locObj.getLocationName();

    }

    public PhotoCaptureLocationBO() {

    }

    public String getImagePath() {
        return mImagePath;
    }

    public void setImagePath(String imagepath) {
        this.mImagePath = imagepath;
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

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getSKUName() {
        return mSKUName;
    }

    public void setSKUName(String mSKUName) {
        this.mSKUName = mSKUName;
    }

    public String getAbv() {
        return abv;
    }

    public void setAbv(String abv) {
        this.abv = abv;
    }

    public String getLotCode() {
        return mLotCode;
    }

    public void setLotCode(String mLotCode) {
        this.mLotCode = mLotCode;
    }

    public String getSequenceNO() {
        return mSequenceNO;
    }

    public void setSequenceNO(String mSequenceNO) {
        this.mSequenceNO = mSequenceNO;
    }

    public String getFeedback() {
        return feedback;
    }

    public void setFeedback(String feedback) {
        this.feedback = feedback;
    }

    public int getProductID() {
        return productID;
    }

    public void setProductID(int productID) {
        this.productID = productID;
    }

}
