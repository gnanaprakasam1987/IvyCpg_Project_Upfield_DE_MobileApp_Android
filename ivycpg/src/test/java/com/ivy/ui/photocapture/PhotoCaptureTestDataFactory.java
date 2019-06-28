package com.ivy.ui.photocapture;

import com.ivy.ui.photocapture.model.PhotoCaptureLocationBO;
import com.ivy.ui.photocapture.model.PhotoCaptureProductBO;
import com.ivy.ui.photocapture.model.PhotoTypeMasterBO;
import com.ivy.sd.png.bo.RetailerMasterBO;

import java.util.ArrayList;

public abstract class PhotoCaptureTestDataFactory {

    public static RetailerMasterBO retailerMasterBO = new RetailerMasterBO("1","abcd");

    public static ArrayList<PhotoCaptureProductBO> getPhotoCaptureProductList() {
        ArrayList<PhotoCaptureProductBO> photoCaptureProductBOS = new ArrayList<>();
        photoCaptureProductBOS.add(new PhotoCaptureProductBO(1, "abc"));
        photoCaptureProductBOS.add(new PhotoCaptureProductBO(2, "def"));
        photoCaptureProductBOS.add(new PhotoCaptureProductBO(3, "ghi"));
        return photoCaptureProductBOS;
    }


    public static ArrayList<PhotoTypeMasterBO> getPhotoCaptureTypeList() {
        ArrayList<PhotoTypeMasterBO> photoTypeMasterBOS = new ArrayList<>();
        photoTypeMasterBOS.add(new PhotoTypeMasterBO(1, "abc"));
        photoTypeMasterBOS.add(new PhotoTypeMasterBO(2, "def"));
        photoTypeMasterBOS.add(new PhotoTypeMasterBO(3, "ghi"));
        return photoTypeMasterBOS;
    }


    public static ArrayList<PhotoCaptureLocationBO> getPhotoCaptureLocationList() {
        ArrayList<PhotoCaptureLocationBO> photoCaptureLocationBOS = new ArrayList<>();
        photoCaptureLocationBOS.add(new PhotoCaptureLocationBO());
        return photoCaptureLocationBOS;
    }


}
