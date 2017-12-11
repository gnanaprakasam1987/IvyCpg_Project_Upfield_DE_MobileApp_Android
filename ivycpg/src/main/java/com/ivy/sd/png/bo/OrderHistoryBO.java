package com.ivy.sd.png.bo;

public class OrderHistoryBO {

    private String retailerCode, retailerId;
    private int lpc;
    private String RF1, RF2, RF3, RF4, RF5;

    public int getNumid() {
        return numid;
    }

    public void setNumid(int numid) {
        this.numid = numid;
    }

    private int numid;
    private String refId;
    private String orderdate;
    private String orderid;

    private String invoiceId;
    private double orderValue, paidAmount;
    private int isJointCall;
    private String delieveryStatus;
    private String noorderReason;
    private String dueDate;
    private String overDueDays;
    private double outStandingAmt;
    private String volume;

    public String getNoorderReason() {
        return noorderReason;
    }

    public void setNoorderReason(String noorderReason) {
        this.noorderReason = noorderReason;
    }


    public String getRetailerCode() {
        return retailerCode;
    }

    public void setRetailerCode(String retailerCode) {
        this.retailerCode = retailerCode;
    }

    public String getRetailerId() {
        return retailerId;
    }

    public void setRetailerId(String retailerId) {
        this.retailerId = retailerId;
    }

    public int getLpc() {
        return lpc;
    }

    public void setLpc(int lpc) {
        this.lpc = lpc;
    }

    public String getRefId() {
        return refId;
    }

    public void setRefId(String refId) {
        this.refId = refId;
    }

    public String getOrderdate() {
        return orderdate;
    }

    public void setOrderdate(String orderdate) {
        this.orderdate = orderdate;
    }

    public double getOrderValue() {
        return orderValue;
    }

    public void setOrderValue(double orderValue) {
        this.orderValue = orderValue;
    }

    public int getIsJointCall() {
        return isJointCall;
    }

    public void setIsJointCall(int isJointCall) {
        this.isJointCall = isJointCall;
    }


    public String getDelieveryStatus() {
        return delieveryStatus;
    }

    public void setDelieveryStatus(String delieveryStatus) {
        this.delieveryStatus = delieveryStatus;
    }

    public double getPaidAmount() {
        return paidAmount;
    }

    public void setPaidAmount(double paidAmount) {
        this.paidAmount = paidAmount;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }


    private String productName;
    private int caseQty;
    private int pcsQty;


    public int getProductId() {
        return productId;
    }

    public void setProductId(int productId) {
        this.productId = productId;
    }

    private int productId;

    public int getOuterQty() {
        return outerQty;
    }

    public void setOuterQty(int outerQty) {
        this.outerQty = outerQty;
    }

    public int getCaseQty() {
        return caseQty;
    }

    public void setCaseQty(int caseQty) {
        this.caseQty = caseQty;
    }

    public int getPcsQty() {
        return pcsQty;
    }

    public void setPcsQty(int pcsQty) {
        this.pcsQty = pcsQty;
    }

    private int outerQty;

    public String getOrderid() {
        return orderid;
    }

    public void setOrderid(String orderid) {
        this.orderid = orderid;
    }

    public String getRF1() {
        return RF1;
    }

    public void setRF1(String RF1) {
        this.RF1 = RF1;
    }

    public String getRF2() {
        return RF2;
    }

    public void setRF2(String RF2) {
        this.RF2 = RF2;
    }

    public String getRF3() {
        return RF3;
    }

    public void setRF3(String RF3) {
        this.RF3 = RF3;
    }

    public String getRF4() {
        return RF4;
    }

    public void setRF4(String RF4) {
        this.RF4 = RF4;
    }

    public String getDueDate() {
        return dueDate;
    }

    public void setDueDate(String dueDate) {
        this.dueDate = dueDate;
    }

    public String getOverDueDays() {
        return overDueDays;
    }

    public void setOverDueDays(String overDueDays) {
        this.overDueDays = overDueDays;
    }

    public double getOutStandingAmt() {
        return outStandingAmt;
    }

    public void setOutStandingAmt(double outStandingAmt) {
        this.outStandingAmt = outStandingAmt;
    }

    public String getVolume() {
        return volume;
    }

    public void setVolume(String volume) {
        this.volume = volume;
    }


    public String getInvoiceId() {
        return invoiceId;
    }

    public void setInvoiceId(String invoiceId) {
        this.invoiceId = invoiceId;
    }

}
