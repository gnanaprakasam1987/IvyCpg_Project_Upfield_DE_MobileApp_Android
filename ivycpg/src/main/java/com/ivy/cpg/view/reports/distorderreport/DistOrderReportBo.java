package com.ivy.cpg.view.reports.distorderreport;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by Hanifa on 31/7/18.
 */

public class DistOrderReportBo implements Parcelable {

    private String orderId;
    private int distributorId;
    private String distributorName;
    private double orderTotal;
    private String lpc;
    private String upload;
    private String dist;
    private String productShortName;
    private String productName;
    private int qty, PQty;
    private float tot;
    private String productId;
    private int batchId;
    private String batchNo;
    private int outerQty;
    private int isCrown;
    private int isBottleReturn;

    public int getIsCrown() {
        return isCrown;
    }

    public void setIsCrown(int isCrown) {
        this.isCrown = isCrown;
    }

    public int getIsBottleReturn() {
        return isBottleReturn;
    }

    public void setIsBottleReturn(int isBottleReturn) {
        this.isBottleReturn = isBottleReturn;
    }

    public String getProductCode() {
        return productCode;
    }

    public void setProductCode(String productCode) {
        this.productCode = productCode;
    }

    private String productCode = "";
    private String retailerId;
    private String retailerName;

    public int getQty() {
        return qty;
    }

    public void setQty(int qty) {
        this.qty = qty;
    }

    public String getRetailerId() {
        return retailerId;
    }

    public void setRetailerId(String retailerId) {
        this.retailerId = retailerId;
    }

    public String getRetailerName() {
        return retailerName;
    }

    public void setRetailerName(String retailerName) {
        this.retailerName = retailerName;
    }

    public String getProductShortName() {
        return productShortName;
    }

    public void setProductShortName(String productShortName) {
        this.productShortName = productShortName;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public void setCQty(int qty) {
        this.qty = qty;
    }

    public int getCQty() {
        return qty;
    }

    public int getPQty() {
        return PQty;
    }

    public void setPQty(int PQty) {
        this.PQty = PQty;
    }

    public float getTot() {
        return tot;
    }

    public void setTot(float tot) {
        this.tot = tot;
    }

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public int getBatchId() {
        return batchId;
    }

    public void setBatchId(int batchId) {
        this.batchId = batchId;
    }

    public String getBatchNo() {
        return batchNo;
    }

    public void setBatchNo(String batchNo) {
        this.batchNo = batchNo;
    }

    public DistOrderReportBo() {

    }

    protected DistOrderReportBo(Parcel in) {
        qty = in.readInt();
        PQty = in.readInt();
        tot = in.readFloat();
        orderTotal = in.readDouble();
        productName = in.readString();
        productShortName = in.readString();
        lpc = in.readString();
        orderId = in.readString();
        productCode = in.readString();
        retailerId = in.readString();
        retailerName = in.readString();
        isCrown = in.readInt();
        isBottleReturn = in.readInt();

    }

    public static final Creator<DistOrderReportBo> CREATOR = new Creator<DistOrderReportBo>() {
        @Override
        public DistOrderReportBo createFromParcel(Parcel in) {
            return new DistOrderReportBo(in);
        }

        @Override
        public DistOrderReportBo[] newArray(int size) {
            return new DistOrderReportBo[size];
        }
    };

    public String getDist() {
        return dist;
    }

    public void setDist(String dist) {
        this.dist = dist;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public int getDistributorId() {
        return distributorId;
    }

    public void setDistributorId(int distributorId) {
        this.distributorId = distributorId;
    }

    public String getDistributorName() {
        return distributorName;
    }

    public void setDistributorName(String distributorName) {
        this.distributorName = distributorName;
    }

    public double getOrderTotal() {
        return orderTotal;
    }

    public void setOrderTotal(double orderTotal) {
        this.orderTotal = orderTotal;
    }

    public String getLpc() {
        return lpc;
    }

    public void setLpc(String lpc) {
        this.lpc = lpc;
    }

    public String getUpload() {
        return upload;
    }

    public void setUpload(String upload) {
        this.upload = upload;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(qty);
        dest.writeInt(PQty);
        dest.writeFloat(tot);
        dest.writeDouble(orderTotal);
        dest.writeString(productName);
        dest.writeString(productShortName);
        dest.writeString(lpc);
        dest.writeString(orderId);
        dest.writeString(productCode);
        dest.writeString(retailerId);
        dest.writeString(retailerName);
        dest.writeInt(isCrown);
        dest.writeInt(isBottleReturn);
    }


    public int getOuterOrderedCaseQty() {
        return outerQty;
    }

    public void setOuterOrderedCaseQty(int outerQty) {
        this.outerQty = outerQty;
    }
}
