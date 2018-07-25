package com.ivy.cpg.view.reports.salesreport.salesreportdetails;


public class SalesReturnDeliveryReportBo {

    private String Uid;
    private String productName;
    private int cQty;
    private int pQty;
    private int returnValue;

    private String productId;

    private String reason;

    private String ReasonType;


    public String getReasonType() {
        return ReasonType;
    }

    public void setReasonType(String reasonType) {
        ReasonType = reasonType;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public String getUid() {
        return Uid;
    }

    public void setUid(String uid) {
        Uid = uid;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public int getCaseQty() {
        return cQty;
    }

    public void setCaseQty(int cQty) {
        this.cQty = cQty;
    }

    public int getPieceQty() {
        return pQty;
    }

    public void setPieceQty(int pQty) {
        this.pQty = pQty;
    }

    public int getReturnValue() {
        return returnValue;
    }

    public void setReturnValue(int returnValue) {
        this.returnValue = returnValue;
    }
}
