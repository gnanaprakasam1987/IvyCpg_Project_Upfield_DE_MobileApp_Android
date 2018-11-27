package com.ivy.cpg.view.reports.slaesvolumereport;

/**
 * Created by Hanifa on 31/7/18.
 */

public class SalesVolumeBo {
    private String ProductID;
    private String productCode;
    private String productShortName;
    private String ProductName;
    private int cParentid;
    private int parentid;
    private int totalQty;
    private int isSaleable;
    private String brandname;
    private double totalamount;
    private String parentHierarchy;
    private float totalWeight;


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

    public int getcParentid() {
        return cParentid;
    }

    public void setcParentid(int cParentid) {
        this.cParentid = cParentid;
    }

    public int getParentid() {
        return parentid;
    }

    public void setParentid(int parentid) {
        this.parentid = parentid;
    }

    public int getTotalQty() {
        return totalQty;
    }

    public void setTotalQty(int totalQty) {
        this.totalQty = totalQty;
    }

    public int getIsSaleable() {
        return isSaleable;
    }

    public void setIsSaleable(int isSaleable) {
        this.isSaleable = isSaleable;
    }

    public String getBrandname() {
        return brandname;
    }

    public void setBrandname(String brandname) {
        this.brandname = brandname;
    }

    public double getTotalamount() {
        return totalamount;
    }

    public void setTotalamount(double totalamount) {
        this.totalamount = totalamount;
    }

    public String getParentHierarchy() {
        return parentHierarchy;
    }

    public void setParentHierarchy(String parentHierarchy) {
        this.parentHierarchy = parentHierarchy;
    }

    public float getTotalWeight() {
        return totalWeight;
    }

    public void setTotalWeight(float totalWeight) {
        this.totalWeight = totalWeight;
    }
}
