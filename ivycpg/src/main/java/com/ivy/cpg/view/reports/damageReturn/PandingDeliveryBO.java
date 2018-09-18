package com.ivy.cpg.view.reports.damageReturn;

/**
 * Created by murugan on 17/9/18.
 */

public class PandingDeliveryBO {

    private String InvoiceNo;
    private String InvoiceDate;
    private String Retailerid;
    private String invNetamount;
    private String upload;
    private String LinesPerCall;
    private String PickListId;
    private String InvoiceRefNo;

    public String getStatus() {
        return status;
    }

    private String status;

    public String getRetailerName() {
        return retailerName;
    }

    private String retailerName;

    public String getInvoiceNo() {
        return InvoiceNo;
    }

    public void setInvoiceNo(String invoiceNo) {
        InvoiceNo = invoiceNo;
    }

    public String getInvoiceDate() {
        return InvoiceDate;
    }

    public void setInvoiceDate(String invoiceDate) {
        InvoiceDate = invoiceDate;
    }

    public String getRetailerid() {
        return Retailerid;
    }

    public void setRetailerid(String retailerid) {
        Retailerid = retailerid;
    }

    public String getInvNetamount() {
        return invNetamount;
    }

    public void setInvNetamount(String invNetamount) {
        this.invNetamount = invNetamount;
    }

    public String getUpload() {
        return upload;
    }

    public void setUpload(String upload) {
        this.upload = upload;
    }

    public String getLinesPerCall() {
        return LinesPerCall;
    }

    public void setLinesPerCall(String linesPerCall) {
        LinesPerCall = linesPerCall;
    }

    public String getPickListId() {
        return PickListId;
    }

    public void setPickListId(String pickListId) {
        PickListId = pickListId;
    }

    public String getInvoiceRefNo() {
        return InvoiceRefNo;
    }

    public void setInvoiceRefNo(String invoiceRefNo) {
        InvoiceRefNo = invoiceRefNo;
    }

    public void setRetailerName(String retailerName) {
        this.retailerName = retailerName;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
