package com.ivy.sd.png.bo;

public class SalesReturnReasonBO {

    private String reasonID;
    private String reasonDesc;
    private String reasonCategory;

    private int outerQty;
    private int caseQty;
    private int pieceQty;
    private int caseSize;
    private int outerSize;

    private int srOuterQty;
    private int srCaseQty;
    private int srPieceQty;
    private String mfgDate;
    private String expDate;
    private float srp,srpedit;

    private double oldMrp;
    private String lotNumber="";
    private String invoiceno="0";

    private String ProductName;
    private String productShortName;

    public SalesReturnReasonBO() {
        //to avoid compile time error when object with no parameter/s created
    }

    public String getReasonCategory() {
        return reasonCategory;
    }

    public void setReasonCategory(String reasonCategory) {
        this.reasonCategory = reasonCategory;
    }

    public int getSrOuterQty() {
        return srOuterQty;
    }

    public void setSrOuterQty(int srOuterQty) {
        this.srOuterQty = srOuterQty;
    }

    public int getSrCaseQty() {
        return srCaseQty;
    }

    public void setSrCaseQty(int srCaseQty) {
        this.srCaseQty = srCaseQty;
    }

    public int getSrPieceQty() {
        return srPieceQty;
    }

    public void setSrPieceQty(int srPieceQty) {
        this.srPieceQty = srPieceQty;
    }

    public int getCaseSize() {
        return caseSize;
    }

    public void setCaseSize(int caseSize) {
        this.caseSize = caseSize;
    }

    public int getOuterSize() {
        return outerSize;
    }

    public void setOuterSize(int outerSize) {
        this.outerSize = outerSize;
    }

    public SalesReturnReasonBO(SalesReturnReasonBO item) {
        this.reasonID = item.reasonID;
        this.reasonDesc = item.reasonDesc;
        this.reasonCategory = item.reasonCategory;
        this.outerQty = item.outerQty;
        this.caseQty = item.caseQty;
        this.pieceQty = item.pieceQty;
        this.srCaseQty=item.srCaseQty;
        this.srOuterQty=item.srOuterQty;
        this.srPieceQty=item.srPieceQty;

        this.srpedit=item.srpedit;
        this.oldMrp=item.oldMrp;
        this.caseSize=item.caseSize;
        this.outerSize=item.outerSize;

        this.productShortName = item.getProductShortName();
    }

    @Override
    public String toString() {
        return reasonDesc;
    }

    public String getReasonID() {
        return reasonID;
    }

    public void setReasonID(String reasonID) {
        this.reasonID = reasonID;
    }

    public String getReasonDesc() {
        return reasonDesc;
    }

    public void setReasonDesc(String reasonDesc) {
        this.reasonDesc = reasonDesc;
    }

    public int getOuterQty() {
        return outerQty;
    }

    public void setOuterQty(int outerQty) {
        this.outerQty = outerQty;
    }

    public int getCaseQty() {
        return caseQty;
    }

    public void setCaseQty(int caseQty) {
        this.caseQty = caseQty;
    }

    public int getPieceQty() {
        return pieceQty;
    }

    public void setPieceQty(int pieceQty) {
        this.pieceQty = pieceQty;
    }

    public String getLotNumber() {
        return lotNumber;
    }

    public void setLotNumber(String lotNumber) {
        this.lotNumber = lotNumber;
    }

    public String getInvoiceno() {
        return invoiceno;
    }

    public void setInvoiceno(String invoiceno) {
        this.invoiceno = invoiceno;
    }

    public double getOldMrp() {
        return oldMrp;
    }

    public void setOldMrp(double oldMrp) {
        this.oldMrp = oldMrp;
    }

    public float getSrpedit() {
        return srpedit;
    }

    public void setSrpedit(float srpedit) {
        this.srpedit = srpedit;
    }

    public String getMfgDate() {
        return mfgDate;
    }

    public void setMfgDate(String mfgDate) {
        this.mfgDate = mfgDate;
    }

    public float getSrp() {
        return srp;
    }

    public void setSrp(float price_Value) {
        srp = price_Value;
    }

    public String getExpDate() {
        return expDate;
    }

    public void setExpDate(String expDate) {
        this.expDate = expDate;
    }

    public String getProductName() {
        return ProductName;
    }

    public void setProductName(String productName) {
        ProductName = productName;
    }

    public void setProductShortName(String productShortName) {
        this.productShortName = productShortName;
    }

    public String getProductShortName() {
        return productShortName;
    }
}
