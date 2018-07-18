package com.ivy.cpg.view.salesdeliveryreturn;


public class SalesReturnDeliveryDataModel {

    public SalesReturnDeliveryDataModel() {
    }

    private int uId;

    private String date;

    private String returnValue;

    private int lpc;

    private String invoiceId;


    public int getUId() {
        return uId;
    }

    public void setUId(int uId) {
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
}
