package com.ivy.sd.png.bo;

import android.os.Parcel;
import android.os.Parcelable;

public class ReportonorderbookingBO implements Parcelable {

    public ReportonorderbookingBO() {

    }

    public String getorderID() {
        return orderid;
    }

    public void setorderID(String orderID) {
        this.orderid = orderID;
    }

    public String getreatilerId() {
        return reatilerid;
    }

    public void setreatilerId(String reatilerId) {
        this.reatilerid = reatilerId;
    }

    public String getlpc() {
        return lpc;
    }

    public void setlpc(String lpc) {
        this.lpc = lpc;
    }

    public String getretailerName() {
        return retailerName;
    }

    public void setretailerName(String retailerName) {
        this.retailerName = retailerName;
    }

    public double getordertot() {
        return ordertot;
    }

    public void setordertot(double ordertot) {
        this.ordertot = ordertot;
    }

    public void setProductname(String productname) {
        this.productname = productname;
    }

    public String getProductname() {
        return productname;
    }

    public void setCQty(int qty) {
        this.qty = qty;
    }

    public int getCQty() {
        return qty;
    }

    public void setPQty(int pqty) {
        this.pqty = pqty;
    }

    public int getPQty() {
        return pqty;
    }

    public void setTot(float tot) {
        this.tot = tot;
    }

    public float getTot() {
        return tot;
    }

    public void setProductshortname(String productshortname) {
        this.productshortname = productshortname;
    }

    public String getProductshortname() {
        return productshortname;
    }

    @Override
    public int describeContents() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int arg1) {
        // TODO Auto-generated method stub

        dest.writeInt(qty);
        dest.writeInt(pqty);
        dest.writeFloat(tot);
        dest.writeDouble(ordertot);

        dest.writeString(productname);
        dest.writeString(productshortname);
        dest.writeString(lpc);
        dest.writeString(retailerName);
        dest.writeString(reatilerid);
        dest.writeString(orderid);

        dest.writeInt(isCrown);
        dest.writeInt(isBottleReturn);


    }

    public ReportonorderbookingBO(Parcel source) {
        qty = source.readInt();
        pqty = source.readInt();
        tot = source.readFloat();
        ordertot = source.readDouble();

        productname = source.readString();
        productshortname = source.readString();
        lpc = source.readString();
        retailerName = source.readString();
        reatilerid = source.readString();
        orderid = source.readString();

        isCrown = source.readInt();
        isBottleReturn = source.readInt();
    }

    private String orderid;
    private String reatilerid;
    private String retailerName;
    private String lpc;
    private double ordertot;

    private String productshortname;
    private String productname;
    private int qty, pqty;
    private float tot;
    private String dist;

    private String upload;
    private int outerQty;
    private int isCrown;
    private int isBottleReturn;
    private String productid;
    private int batchid;
    private String batchNo;

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

    public String getDistributorname() {
        return distributorname;
    }

    public void setDistributorname(String distributorname) {
        this.distributorname = distributorname;
    }



    private int distributorId;
    private String distributorname;



    public int getBatchid() {
        return batchid;
    }

    public void setBatchid(int batchid) {
        this.batchid = batchid;
    }

    public String getProductid() {
        return productid;
    }

    public void setProductid(String productid) {
        this.productid = productid;
    }

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

    /**
     * This field is needed for Android to be able to create new objects,
     * individually or as arrays.
     * <p/>
     * This also means that you can use use the default constructor to create
     * the object and use another method to hyrdate it as necessary.
     * <p/>
     * I just find it easier to use the constructor. It makes sense for the way
     * my brain thinks ;-)
     */
    public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
        public ReportonorderbookingBO createFromParcel(Parcel in) {
            return new ReportonorderbookingBO(in);
        }

        public ReportonorderbookingBO[] newArray(int size) {
            return new ReportonorderbookingBO[size];
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

    public int getmFocusBrandCount() {
        return mFocusBrandCount;
    }

    public void setmFocusBrandCount(int mFocusBrandCount) {
        this.mFocusBrandCount = mFocusBrandCount;
    }

    public int getmMustSellCount() {
        return mMustSellCount;
    }

    public void setmMustSellCount(int mMustSellCount) {
        this.mMustSellCount = mMustSellCount;
    }

    private int mFocusBrandCount,mMustSellCount;
}
