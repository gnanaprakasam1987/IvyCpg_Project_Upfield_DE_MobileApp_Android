package com.ivy.sd.png.bo;

import java.util.ArrayList;

public class PhotoTypeMasterBO {

    private int photoTypeId;
    private String photoTypeDesc;
    private String photoTypeCode;
    private ArrayList<PhotoCaptureProductBO> photoCaptureProductList;

    public PhotoTypeMasterBO() {

    }

    public PhotoTypeMasterBO(int photoid, String photdesc) {
        this.photoTypeId = photoid;
        this.photoTypeDesc = photdesc;
    }

    @Override
    public String toString() {
        return photoTypeDesc.toString();
    }

    public int getPhotoTypeId() {
        return photoTypeId;
    }

    public void setPhotoTypeId(int photoTypeId) {
        this.photoTypeId = photoTypeId;
    }

    public String getPhotoTypeDesc() {
        return photoTypeDesc;
    }

    public void setPhotoTypeDesc(String photoTypeDesc) {
        this.photoTypeDesc = photoTypeDesc;
    }

    public ArrayList<PhotoCaptureProductBO> getPhotoCaptureProductList() {
        return photoCaptureProductList;
    }

    public void setPhotoCaptureProductList(ArrayList<PhotoCaptureProductBO> photoCaptureProductList) {
        this.photoCaptureProductList = photoCaptureProductList;
    }

    public String getPhotoTypeCode() {
        return photoTypeCode;
    }

    public void setPhotoTypeCode(String photoTypeCode) {
        this.photoTypeCode = photoTypeCode;
    }
}
