package com.ivy.sd.png.bo;

import android.os.Parcel;
import android.os.Parcelable;

public class SupplierMasterBO implements Parcelable {
    private int supplierID;
    private String supplierName;
    private int supplierType;
    private int isPrimary;
    private int distParentID;
    private float creditLimit;
    private int supplierTaxLocId;
    private String rpTypeCode;

    public String getRpTypeCode() {
        return rpTypeCode;
    }

    public void setRpTypeCode(String rpTypeCode) {
        this.rpTypeCode = rpTypeCode;
    }


    public boolean isCompositeRetailer() {
        return isCompositeRetailer;
    }

    public void setCompositeRetailer(boolean compositeRetailer) {
        isCompositeRetailer = compositeRetailer;
    }

    private boolean isCompositeRetailer;

    public SupplierMasterBO(){

    }
    public SupplierMasterBO(int sId,String sName){
       this.supplierID=sId;
        this.supplierName=sName;
    }

    protected SupplierMasterBO(Parcel in) {
        supplierID = in.readInt();
        supplierName = in.readString();
        supplierType = in.readInt();
        isPrimary = in.readInt();
        distParentID = in.readInt();
        creditLimit = in.readFloat();
        supplierTaxLocId = in.readInt();
    }

    public static final Creator<SupplierMasterBO> CREATOR = new Creator<SupplierMasterBO>() {
        @Override
        public SupplierMasterBO createFromParcel(Parcel in) {
            return new SupplierMasterBO(in);
        }

        @Override
        public SupplierMasterBO[] newArray(int size) {
            return new SupplierMasterBO[size];
        }
    };

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

    public int getSupplierTaxLocId() {
        return supplierTaxLocId;
    }

    public void setSupplierTaxLocId(int supplierTaxLocId) {
        this.supplierTaxLocId = supplierTaxLocId;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(supplierID);
        dest.writeString(supplierName);
        dest.writeInt(supplierType);
        dest.writeInt(isPrimary);
        dest.writeInt(distParentID);
        dest.writeFloat(creditLimit);
        dest.writeInt(supplierTaxLocId);
    }
}
