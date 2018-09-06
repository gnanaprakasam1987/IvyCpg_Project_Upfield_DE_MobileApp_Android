package com.ivy.cpg.view.collection;

public class NoCollectionReasonBo {

    private String invoiceNo;
    private String invoiceDate;
    private double paidAmount;
    private double invoiceAmount;
    private String retailerId;

    private String refNo = "";
    private String noCollectionReasonId="";
    private String noCollectionReason="";

    public String getInvoiceNo() {
        return invoiceNo;
    }

    public void setInvoiceNo(String invoiceNo) {
        this.invoiceNo = invoiceNo;
    }

    public String getInvoiceDate() {
        return invoiceDate;
    }

    public void setInvoiceDate(String invoiceDate) {
        this.invoiceDate = invoiceDate;
    }

    public double getPaidAmount() {
        return paidAmount;
    }

    public void setPaidAmount(double paidAmount) {
        this.paidAmount = paidAmount;
    }

    public double getInvoiceAmount() {
        return invoiceAmount;
    }

    public void setInvoiceAmount(double invoiceAmount) {
        this.invoiceAmount = invoiceAmount;
    }

    public String getRetailerId() {
        return retailerId;
    }

    public void setRetailerId(String retailerId) {
        this.retailerId = retailerId;
    }

    public String getRefNo() {
        return refNo;
    }

    public void setRefNo(String refNo) {
        this.refNo = refNo;
    }

    public String getNoCollectionReasonId() {
        return noCollectionReasonId;
    }

    public void setNoCollectionReasonId(String noCollectionReasonId) {
        this.noCollectionReasonId = noCollectionReasonId;
    }

    public String getNoCollectionReason() {
        return noCollectionReason;
    }

    public void setNoCollectionReason(String noCollectionReason) {
        this.noCollectionReason = noCollectionReason;
    }
}
