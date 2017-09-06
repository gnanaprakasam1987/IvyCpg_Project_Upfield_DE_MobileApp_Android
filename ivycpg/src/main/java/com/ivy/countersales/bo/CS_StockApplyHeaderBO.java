package com.ivy.countersales.bo;

/**
 * Created by subramanian on 6/14/17.
 */

public class CS_StockApplyHeaderBO {
    private String receiptId;
    private String referenceNo="";
    private String status;
    private String receiptDate="";
    private int stockTypeId=0;
    private String stockType;
    private String upload = "Y";

    public String getReceiptId() {
        return receiptId;
    }

    public void setReceiptId(String receiptId) {
        this.receiptId = receiptId;
    }

    public String getReferenceNo() {
        return referenceNo;
    }

    public void setReferenceNo(String referenceNo) {
        this.referenceNo = referenceNo;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getReceiptDate() {
        return receiptDate;
    }

    public void setReceiptDate(String receiptDate) {
        this.receiptDate = receiptDate;
    }

    public int getStockTypeId() {
        return stockTypeId;
    }

    public void setStockTypeId(int stockTypeId) {
        this.stockTypeId = stockTypeId;
    }

    public String getStockType() {
        return stockType;
    }

    public void setStockType(String stockType) {
        this.stockType = stockType;
    }

    public String getUpload() {
        return upload;
    }

    public void setUpload(String upload) {
        this.upload = upload;
    }
}
