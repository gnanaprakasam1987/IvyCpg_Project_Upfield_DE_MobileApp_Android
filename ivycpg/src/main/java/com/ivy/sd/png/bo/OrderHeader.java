package com.ivy.sd.png.bo;

import java.io.Serializable;

public class OrderHeader implements Serializable {

    // Bill Net value
    private double OrderValue;
    // Bill Original Value
    private double originalOrderValue;

    private int LinesPerCall;
    private double discount;
    private String deliveryDate;
    private String PO = "";
    private String Remark = "";
    private String rField1 = "";
    private String rField2 = "";
    private int totalFreeProductsCount, discountId, isCompanyGiven;
    private float totalFreeProductsAmount;
    private String orderid;
    private double remainigValue;
    private int crownCount;
    private int isSplitted;
    private String SignatureName = "";
    private String SignaturePath = "";
    private boolean isSignCaptured;
    private float totalWeight;
    private String orderDate;
    private int invoiceStatus;
    private int addressID = 0;
    private String rField3 = "";
    private String orderStatus="";

    private double billLevelDiscountValue;
    private double billLevelTaxValue;


    public int getInvoiceStatus() {
        return invoiceStatus;
    }

    public void setInvoiceStatus(int invoiceStatus) {
        this.invoiceStatus = invoiceStatus;
    }

    public String getOrderDate() {
        return orderDate;
    }

    public void setOrderDate(String orderDate) {
        this.orderDate = orderDate;
    }



    private int orderedFocusBrands, orderedMustSellCount;

    public float getTotalWeight() {
        return totalWeight;
    }

    public void setTotalWeight(float totalWeight) {
        this.totalWeight = totalWeight;
    }

    public boolean isSignCaptured() {
        return isSignCaptured;
    }

    public void setIsSignCaptured(boolean isSignCaptured) {
        this.isSignCaptured = isSignCaptured;
    }

    public String getSignatureName() {
        return SignatureName;
    }

    public void setSignatureName(String signatureName) {
        SignatureName = signatureName;
    }


    public int getCrownCount() {
        return crownCount;
    }

    public void setCrownCount(int crownCount) {
        this.crownCount = crownCount;
    }

    public double getRemainigValue() {
        return remainigValue;
    }

    public void setRemainigValue(double remainigValue) {
        this.remainigValue = remainigValue;
    }

    public String getOrderid() {
        return orderid;
    }

    public void setOrderid(String orderid) {
        this.orderid = orderid;
    }

    public int getTotalFreeProductsCount() {
        return totalFreeProductsCount;
    }

    public void setTotalFreeProductsCount(int totalFreeProductsCount) {
        this.totalFreeProductsCount = totalFreeProductsCount;
    }

    public String getPO() {
        return PO;
    }

    public void setPO(String pO) {
        PO = pO;
    }

    public String getRemark() {
        return Remark;
    }

    public void setRemark(String remark) {
        Remark = remark;
    }

    public String getRField1() {
        return rField1;
    }

    public void setRField1(String rField1) {
        this.rField1 = rField1;
    }

    public String getRField2() {
        return rField2;
    }

    public void setRField2(String rField2) {
        this.rField2 = rField2;
    }

    public double getOrderValue() {
        return OrderValue;
    }

    public void setOrderValue(double orderValue) {
        OrderValue = orderValue;
    }

    public int getLinesPerCall() {
        return LinesPerCall;
    }

    public void setLinesPerCall(int linesPerCall) {
        LinesPerCall = linesPerCall;
    }

    public double getDiscount() {
        return discount;
    }

    public void setDiscount(double discount) {
        this.discount = discount;
    }

    public String getDeliveryDate() {
        return deliveryDate;
    }

    public void setDeliveryDate(String deliveryDate) {
        this.deliveryDate = deliveryDate;
    }

    public float getTotalFreeProductsAmount() {
        return totalFreeProductsAmount;
    }

    public void setTotalFreeProductsAmount(float totalFreeProductsAmount) {
        this.totalFreeProductsAmount = totalFreeProductsAmount;
    }

    public int getIsSplitted() {
        return isSplitted;
    }

    public void setIsSplitted(int isSplitted) {
        this.isSplitted = isSplitted;
    }

    public int getOrderedFocusBrands() {
        return orderedFocusBrands;
    }

    public void setOrderedFocusBrands(int orderedFocusBrands) {
        this.orderedFocusBrands = orderedFocusBrands;
    }

    public int getOrderedMustSellCount() {
        return orderedMustSellCount;
    }

    public void setOrderedMustSellCount(int orderedMustSellCount) {
        this.orderedMustSellCount = orderedMustSellCount;
    }

    private double totalMustSellValue = 0;

    public double getTotalFocusProdValues() {
        return totalFocusProdValues;
    }

    public void setTotalFocusProdValues(double totalFocusProdValues) {
        this.totalFocusProdValues = totalFocusProdValues;
    }

    private double totalFocusProdValues = 0;

    public double getTotalMustSellValue() {
        return totalMustSellValue;
    }

    public void setTotalMustSellValue(double totalMustSellValue) {
        this.totalMustSellValue = totalMustSellValue;
    }


    public String getSignaturePath() {
        return SignaturePath;
    }

    public void setSignaturePath(String signaturePath) {
        SignaturePath = signaturePath;
    }

    public int getDiscountId() {
        return discountId;
    }

    public void setDiscountId(int discountId) {
        this.discountId = discountId;
    }

    public int getIsCompanyGiven() {
        return isCompanyGiven;
    }

    public void setIsCompanyGiven(int isCompanyGiven) {
        this.isCompanyGiven = isCompanyGiven;
    }

    private String orderImageName = "";

    public void setOrderImageName(String orderImageName) {
        this.orderImageName = orderImageName;
    }

    public String getOrderImageName() {
        return orderImageName;
    }

    public void setAddressID(int addressID) {
        this.addressID = addressID;
    }

    public int getAddressID() {
        return addressID;
    }

   /* private String attachedFileName = "";

    public String getAttachedFileName() {
        return attachedFileName;
    }

    public void setAttachedFileName(String attachedFileName) {
        this.attachedFileName = attachedFileName;
    }*/

    public String getrField3() {
        return rField3;
    }

    public void setrField3(String rField3) {
        this.rField3 = rField3;
    }

    public double getBillLevelTaxValue() {
        return billLevelTaxValue;
    }

    public void setBillLevelTaxValue(double billLevelTaxValue) {
        this.billLevelTaxValue = billLevelTaxValue;
    }

    public double getBillLevelDiscountValue() {
        return billLevelDiscountValue;
    }

    public void setBillLevelDiscountValue(double discountValue) {
        this.billLevelDiscountValue = discountValue;
    }

    public String getOrderStatus() {
        return orderStatus;
    }

    public void setOrderStatus(String orderStatus) {
        this.orderStatus = orderStatus;
    }

    public double getOriginalOrderValue() {
        return originalOrderValue;
    }

    public void setOriginalOrderValue(double originalOrderValue) {
        this.originalOrderValue = originalOrderValue;
    }


}
