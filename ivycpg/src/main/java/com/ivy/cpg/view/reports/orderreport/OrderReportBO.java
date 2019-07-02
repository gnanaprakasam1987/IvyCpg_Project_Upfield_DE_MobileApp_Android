package com.ivy.cpg.view.reports.orderreport;

import android.os.Parcel;
import android.os.Parcelable;

import com.ivy.cpg.view.order.scheme.SchemeProductBO;

import java.util.List;

public class OrderReportBO implements Parcelable {

    public OrderReportBO() {

    }

    public String getOrderID() {
        return orderId;
    }

    public void setOrderID(String orderID) {
        this.orderId = orderID;
    }

    public String getRetailerId() {
        return retailerId;
    }

    public void setRetailerId(String retailerId) {
        this.retailerId = retailerId;
    }

    public String getLPC() {
        return lpc;
    }

    public void setLPC(String lpc) {
        this.lpc = lpc;
    }

    public String getRetailerName() {
        return retailerName;
    }

    public void setRetailerName(String retailerName) {
        this.retailerName = retailerName;
    }

    public double getOrderTotal() {
        return orderTotal;
    }

    public void setOrderTotal(double orderTotal) {
        this.orderTotal = orderTotal;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getProductName() {
        return productName;
    }

    public void setCQty(int qty) {
        this.qty = qty;
    }

    public int getCQty() {
        return qty;
    }

    public void setPQty(int PQty) {
        this.PQty = PQty;
    }

    public int getPQty() {
        return PQty;
    }

    public void setTot(float tot) {
        this.tot = tot;
    }

    public float getTot() {
        return tot;
    }

    public void setProductShortName(String productShortName) {
        this.productShortName = productShortName;
    }

    public String getProductShortName() {
        return productShortName;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int arg1) {

        dest.writeInt(qty);
        dest.writeInt(PQty);
        dest.writeFloat(tot);
        dest.writeDouble(orderTotal);

        dest.writeString(productName);
        dest.writeString(productShortName);
        dest.writeString(lpc);
        dest.writeString(retailerName);
        dest.writeString(retailerId);
        dest.writeString(orderId);

        dest.writeInt(isCrown);
        dest.writeInt(isBottleReturn);
        dest.writeString(productCode);

    }

    public OrderReportBO(Parcel source) {
        qty = source.readInt();
        PQty = source.readInt();
        tot = source.readFloat();
        orderTotal = source.readDouble();

        productName = source.readString();
        productShortName = source.readString();
        lpc = source.readString();
        retailerName = source.readString();
        retailerId = source.readString();
        orderId = source.readString();

        isCrown = source.readInt();
        isBottleReturn = source.readInt();
        productCode = source.readString();
    }

    private String orderId;
    private String retailerId;
    private String retailerName;
    private String lpc;
    private double orderTotal;

    private String productShortName;
    private String productName;
    private int qty, PQty;
    private float tot;
    private String dist;

    private String upload;
    private int outerQty;
    private int isCrown;
    private int isBottleReturn;
    private String productId;
    private int batchId;
    private String batchNo;
    private int volumePcsQty;
    private int volumeCaseQty;
    private int volumeOuterQty;
    private String orderedImage = "";
    private String productCode = "";

    public int getVolumeOuterQty() {
        return volumeOuterQty;
    }

    public void setVolumeOuterQty(int volumeOuterQty) {
        this.volumeOuterQty = volumeOuterQty;
    }

    public int getVolumePcsQty() {
        return volumePcsQty;
    }

    public void setVolumePcsQty(int volumePcsQty) {
        this.volumePcsQty = volumePcsQty;
    }

    public int getVolumeCaseQty() {
        return volumeCaseQty;
    }

    public void setVolumeCaseQty(int volumeCaseQty) {
        this.volumeCaseQty = volumeCaseQty;
    }

    public int getIsVanSeller() {
        return isVanSeller;
    }

    public void setIsVanSeller(int isVanSeller) {
        this.isVanSeller = isVanSeller;
    }

    private int isVanSeller;

    public int getTotalQty() {
        return totalQty;
    }

    public void setTotalQty(int totalQty) {
        this.totalQty = totalQty;
    }

    public float getWeight() {
        return weight;
    }

    public void setWeight(float weight) {
        this.weight = weight;
    }

    private int totalQty;
    private float weight;

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



    private int distributorId;
    private String distributorName;



    public int getBatchId() {
        return batchId;
    }

    public void setBatchId(int batchId) {
        this.batchId = batchId;
    }

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public int getIsCrown() {
        return isCrown;
    }

    public int getIsBottleReturn() {
        return isBottleReturn;
    }

    public String getUpload() {
        return upload;
    }

    public void setUpload(String upload) {
        this.upload = upload;
    }

    public String getDist() {
        return dist;
    }

    public void setDist(String dist) {
        this.dist = dist;
    }

    public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
        public OrderReportBO createFromParcel(Parcel in) {
            return new OrderReportBO(in);
        }

        public OrderReportBO[] newArray(int size) {
            return new OrderReportBO[size];
        }
    };

    public int getOuterOrderedCaseQty() {
        return outerQty;
    }

    public void setOuterOrderedCaseQty(int outerQty) {
        this.outerQty = outerQty;
    }

    public String getBatchNo() {
        return batchNo;
    }

    public void setBatchNo(String batchNo) {
        this.batchNo = batchNo;
    }

    public int getFocusBrandCount() {
        return mFocusBrandCount;
    }

    public void setFocusBrandCount(int mFocusBrandCount) {
        this.mFocusBrandCount = mFocusBrandCount;
    }

    public int getMustSellCount() {
        return mMustSellCount;
    }

    public void setMustSellCount(int mMustSellCount) {
        this.mMustSellCount = mMustSellCount;
    }

    private int mFocusBrandCount, mMustSellCount;


    private double taxValue;
    private double discountValue;

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    public double getTaxValue() {
        return taxValue;
    }

    public void setTaxValue(double taxValue) {
        this.taxValue = taxValue;
    }

    public double getDiscountValue() {
        return discountValue;
    }

    public void setDiscountValue(double discountValue) {
        this.discountValue = discountValue;
    }

    private List<SchemeProductBO> schemeProducts;

    public List<SchemeProductBO> getSchemeProducts() {
        return schemeProducts;
    }

    public void setSchemeProducts(List<SchemeProductBO> schemeProducts) {
        this.schemeProducts = schemeProducts;
    }

    public String getOrderedImage() {
        return orderedImage;
    }

    public void setOrderedImage(String orderedImage) {
        this.orderedImage = orderedImage;
    }


    public void setProductCode(String productCode) {
        this.productCode = productCode;
    }

    public String getProductCode() {
        return productCode;
    }
}
