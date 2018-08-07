package com.ivy.cpg.view.reports.closingstockreport;

/**
 * Created by ivyuser on 6/8/18.
 */

public class ClosingStockReportBo {
    private String ProductID;

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

    public int getCsCase() {
        return csCase;
    }

    public void setCsCase(int csCase) {
        this.csCase = csCase;
    }

    public int getCsOuter() {
        return csOuter;
    }

    public void setCsOuter(int csOuter) {
        this.csOuter = csOuter;
    }

    public int getCsPiece() {
        return csPiece;
    }

    public void setCsPiece(int csPiece) {
        this.csPiece = csPiece;
    }

    public int getCasesize() {
        return casesize;
    }

    public void setCasesize(int casesize) {
        this.casesize = casesize;
    }

    public int getOutersize() {
        return outersize;
    }

    public void setOutersize(int outersize) {
        this.outersize = outersize;
    }

    private String productCode;
    private String productShortName;
    private String ProductName;
    private int csCase;
    private int csOuter;
    private int csPiece;
    private int casesize;
    private int outersize;
}
