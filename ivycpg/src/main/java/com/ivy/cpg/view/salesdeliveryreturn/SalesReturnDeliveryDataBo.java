package com.ivy.cpg.view.salesdeliveryreturn;


public class SalesReturnDeliveryDataBo {

    private String uId;

    private String date;

    private String returnValue;

    private int lpc;

    private String invoiceId;

    private String signaturePath = "";

    private String signatureName = "";

    private String refModuleTId = "";

    private String refModule = "";


    public String getUId() {
        return uId;
    }

    public void setUId(String uId) {
        this.uId = uId;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getReturnValue() {
        return returnValue;
    }

    public void setReturnValue(String returnValue) {
        this.returnValue = returnValue;
    }

    public int getLpc() {
        return lpc;
    }

    public void setLpc(int lpc) {
        this.lpc = lpc;
    }

    public String getInvoiceId() {
        return invoiceId;
    }

    public void setInvoiceId(String invoiceId) {
        this.invoiceId = invoiceId;
    }

    public String getSignaturePath() {
        return signaturePath;
    }

    public void setSignaturePath(String signaturePath) {
        this.signaturePath = signaturePath;
    }

    public String getSignatureName() {
        return signatureName;
    }

    public void setSignatureName(String signatureName) {
        this.signatureName = signatureName;
    }

    public String getRefModuleTId() {
        return refModuleTId;
    }

    public void setRefModuleTId(String refModuleTId) {
        this.refModuleTId = refModuleTId;
    }

    public String getRefModule() {
        return refModule;
    }

    public void setRefModule(String refModule) {
        this.refModule = refModule;
    }
}
