package com.ivy.sd.png.bo;

public class SupplierMasterBO {
    private int supplierID;
    private String supplierName;
    private int supplierType;
    private int isPrimary;
    private int distParentID;
    private float creditLimit;

    public SupplierMasterBO(){

    }
    public SupplierMasterBO(int sId,String sName){
       this.supplierID=sId;
        this.supplierName=sName;
    }
    public int getSupplierID() {
        return supplierID;
    }

    public void setSupplierID(int supplierID) {
        this.supplierID = supplierID;
    }

    public String getSupplierName() {
        return supplierName;
    }

    public void setSupplierName(String supplierName) {
        this.supplierName = supplierName;
    }

    public int getIsPrimary() {
        return isPrimary;
    }

    public void setIsPrimary(int isPrimary) {
        this.isPrimary = isPrimary;
    }

    @Override
    public String toString() {

        return this.supplierName;
    }

    public int getDistParentID() {
        return distParentID;
    }

    public void setDistParentID(int distParentID) {
        this.distParentID = distParentID;
    }

    public int getSupplierType() {
        return supplierType;
    }

    public void setSupplierType(int supplierType) {
        this.supplierType = supplierType;
    }

    public float getCreditLimit() {
        return creditLimit;
    }

    public void setCreditLimit(float creditLimit) {
        this.creditLimit = creditLimit;
    }
}
