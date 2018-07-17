package com.ivy.ui.reports.beginstockreport.data;

/**
 * Created by abbas.a on 16/07/18.
 */

public class BeginningStockReportBO {

    private int caseQuantity;
    private int pcsQuantity;
    private int caseSize;
    private int productId;
    private int outerQty;

    public int getCaseQuantity() {
        return caseQuantity;
    }

    public void setCaseQuantity(int caseQuantity) {
        this.caseQuantity = caseQuantity;
    }

    public int getPcsQuantity() {
        return pcsQuantity;
    }

    public void setPcsQuantity(int pcsQuantity) {
        this.pcsQuantity = pcsQuantity;
    }

    public int getCaseSize() {
        return caseSize;
    }

    public void setCaseSize(int caseSize) {
        this.caseSize = caseSize;
    }

    public int getProductId() {
        return productId;
    }

    public void setProductId(int productId) {
        this.productId = productId;
    }

    public int getOuterQty() {
        return outerQty;
    }

    public void setOuterQty(int outerQty) {
        this.outerQty = outerQty;
    }

    public int getOuterSize() {
        return outerSize;
    }

    public void setOuterSize(int outerSize) {
        this.outerSize = outerSize;
    }

    public String getProductShortName() {
        return productShortName;
    }

    public void setProductShortName(String productShortName) {
        this.productShortName = productShortName;
    }

    public float getBasePrice() {
        return basePrice;
    }

    public void setBasePrice(float basePrice) {
        this.basePrice = basePrice;
    }

    private int outerSize;

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    private String productName, productShortName;
    private float basePrice;


}
