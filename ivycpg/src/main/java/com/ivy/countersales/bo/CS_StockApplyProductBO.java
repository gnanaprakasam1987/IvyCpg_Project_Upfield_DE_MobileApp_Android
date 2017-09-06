package com.ivy.countersales.bo;

/**
 * Created by subramanian on 6/14/17.
 */

public class CS_StockApplyProductBO {

    private String receiptId;
    private int productId;
    private String productName;
    private int uomId;
    private int qty = 0;
    private int damagedQty = 0;
    private int balanceQty = 0;

    public int getIsSalable() {
        return isSalable;
    }

    public void setIsSalable(int isSalable) {
        this.isSalable = isSalable;
    }

    private int isSalable;

    public int getIsReturnable() {
        return isReturnable;
    }

    public void setIsReturnable(int isReturnable) {
        this.isReturnable = isReturnable;
    }

    private int isReturnable;
    public double getMrp() {
        return mrp;
    }

    public void setMrp(double mrp) {
        this.mrp = mrp;
    }

    private double mrp;


    public int getParentId() {
        return parentId;
    }

    public void setParentId(int parentId) {
        this.parentId = parentId;
    }

    private int parentId;

    public String getBarcode() {
        return barcode;
    }

    public void setBarcode(String barcode) {
        this.barcode = barcode;
    }

    private String barcode;

    public String getReceiptId() {
        return receiptId;
    }

    public void setReceiptId(String receiptId) {
        this.receiptId = receiptId;
    }

    public int getProductId() {
        return productId;
    }

    public void setProductId(int productId) {
        this.productId = productId;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public int getQty() {
        return qty;
    }

    public void setQty(int qty) {
        this.qty = qty;
    }

    public int getUomId() {
        return uomId;
    }

    public void setUomId(int uomId) {
        this.uomId = uomId;
    }

    public int getDamagedQty() {
        return damagedQty;
    }

    public void setDamagedQty(int damagedQty) {
        this.damagedQty = damagedQty;
    }

    public int getBalanceQty() {
        return balanceQty;
    }

    public void setBalanceQty(int balanceQty) {
        this.balanceQty = balanceQty;
    }
}
