package com.ivy.ui.photocapture.model;

import java.util.ArrayList;

public class PhotoCaptureProductBO {


    private String productName;
    private ArrayList<PhotoCaptureLocationBO> inStoreLocations;
    private int productID;
    // For ADHOC PhotoCapturing
    private String retailerName;

    public PhotoCaptureProductBO(PhotoCaptureProductBO captureBO) {
        this.productID = captureBO.getProductID();
        this.productName = captureBO.getProductName();
        this.inStoreLocations = captureBO.getInStoreLocations();
    }

    public PhotoCaptureProductBO(int pid,String productName) {
        this.productID=pid;
        this.productName=productName;
    }

    public PhotoCaptureProductBO(){

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

    @Override
    public String toString() {
        return productName;
    }

    public ArrayList<PhotoCaptureLocationBO> getInStoreLocations() {
        return inStoreLocations;
    }

    public void setInStoreLocations(ArrayList<PhotoCaptureLocationBO> inStoreLocations) {
        this.inStoreLocations = inStoreLocations;
    }


    public String getRetailerName() {
        return retailerName;
    }

    public void setRetailerName(String retailerName) {
        this.retailerName = retailerName;
    }
}
