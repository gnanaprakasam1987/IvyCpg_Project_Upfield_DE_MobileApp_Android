package com.ivy.cpg.primarysale.bo;

/**
 * Created by dharmapriya.k on 22-09-2015.
 */
public class DistInvoiceDetailsBO {

    private String InvoiceId, DistId, value, lpc, Date, Upload;
    private String Status;

    public String getInvoiceId() {
        return this.InvoiceId;
    }

    public void setInvoiceId(String InvoiceId) {
        this.InvoiceId = InvoiceId;
    }

    public String getDistId() {
        return this.DistId;
    }

    public void setDistId(String DistId) {
        this.DistId = DistId;
    }

    public String getValue() {
        return this.value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getLpc() {
        return this.lpc;
    }

    public void setLpc(String lpc) {
        this.lpc = lpc;
    }

    public String getDate() {
        return this.Date;
    }

    public void setDate(String date) {
        this.Date = date;
    }

    public String getUpload() {
        return this.Upload;
    }

    public void setUpload(String upload) {
        this.Upload = upload;
    }

    public String getStatus() {
        return this.Status;
    }

    public void setStatus(String Status) {
        this.Status = Status;
    }
}
