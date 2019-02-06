package com.ivy.cpg.view.reports.deliveryStockReport;

/**
 * Created by Hanifa on 24/7/18.
 */

public class DeliveryStockBo {
    private String ProductID;
    private String productCode;
    private String productShortName;
    private String ProductName;
    private int pcUomid;
    private int caseUomId;
    private int ouUomid;
    private int orderedCaseQty;
    private int orderedPcsQty;
    private int orderedOuterQty;
    private int outerSize;
    private int caseSize;

    public int getOuterSize() {
        return outerSize;
    }

    public void setOuterSize(int outerSize) {
        this.outerSize = outerSize;
    }

    public int getCaseSize() {
        return caseSize;
    }

    public void setCaseSize(int caseSize) {
        this.caseSize = caseSize;
    }

    public String getProductID() {
        return ProductID;
    }

    public void setProductID(String productID) {
        ProductID = productID;
    }

    public String getProductCode() {
        return productCode;
    }

    public void setProductCode(String productCode) {
        this.productCode = productCode;
    }

    public String getProductShortName() {
        return productShortName;
    }

    public void setProductShortName(String productShortName) {
        this.productShortName = productShortName;
    }

    public String getProductName() {
        return ProductName;
    }

    public void setProductName(String productName) {
        ProductName = productName;
    }

    public int getPcUomid() {
        return pcUomid;
    }

    public void setPcUomid(int pcUomid) {
        this.pcUomid = pcUomid;
    }

    public int getCaseUomId() {
        return caseUomId;
    }

    public void setCaseUomId(int caseUomId) {
        this.caseUomId = caseUomId;
    }

    public int getOuUomid() {
        return ouUomid;
    }

    public void setOuUomid(int ouUomid) {
        this.ouUomid = ouUomid;
    }

    public int getOrderedCaseQty() {
        return orderedCaseQty;
    }

    public void setOrderedCaseQty(int orderedCaseQty) {
        this.orderedCaseQty = orderedCaseQty;
    }

    public int getOrderedPcsQty() {
        return orderedPcsQty;
    }

    public void setOrderedPcsQty(int orderedPcsQty) {
        this.orderedPcsQty = orderedPcsQty;
    }

    public int getOrderedOuterQty() {
        return orderedOuterQty;
    }

    public void setOrderedOuterQty(int orderedOuterQty) {
        this.orderedOuterQty = orderedOuterQty;
    }


}
