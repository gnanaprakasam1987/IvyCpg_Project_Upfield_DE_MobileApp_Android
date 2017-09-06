package com.ivy.sd.png.bo;

public class SupplierMasterBO {
    private int supplierID;
    private String supplierName;
    private int supplierType;
    private int isPrimary;

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

    public int getSupplierType() {
        return supplierType;
    }

    public void setSupplierType(int supplierType) {
        this.supplierType = supplierType;
    }
}
