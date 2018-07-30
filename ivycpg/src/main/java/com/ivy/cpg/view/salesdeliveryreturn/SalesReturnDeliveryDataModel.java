package com.ivy.cpg.view.salesdeliveryreturn;


public class SalesReturnDeliveryDataModel {

    private String uId;

    public SalesReturnDeliveryDataModel() {
    }



    private String date;

    private String returnValue;

    private int lpc;

    private String invoiceId;


    private String productName;
    private String ProductId;

    private int returnPieceQuantity;

    private int returnCaseQuantity;

    private int actualPieceQuantity;

    private int actualCaseQuantity;

    private String reason;
    private String reasonCategory;
    private String reasonID;

    private int outerQty;
    private int caseSize;
    private int outerSize;

    private String mfgDate;
    private String expDate;
    private float srp, srpedit;

    private double oldMrp;
    private int lotNumber;
    private int status;
    private String retailerId;

    private int dUomQty;
    private int dUomId;

    private int dOUomId;
    private int dOUomQty;

    private String inVoiceNumber;
    private int totalQuantity;

    private String totalAmount;
    private int reasonType;


    private int pieceUomId;
    private String hnsCode = "";

    private int caseUomId;


    public int getCaseUomId() {
        return caseUomId;
    }

    public void setCaseUomId(int caseUomId) {
        this.caseUomId = caseUomId;
    }

    public String getHnsCode() {
        return hnsCode;
    }

    public void setHnsCode(String hnsCode) {
        this.hnsCode = hnsCode;
    }

    public int getPieceUomId() {
        return pieceUomId;
    }

    public void setPieceUomId(int pieceUomId) {
        this.pieceUomId = pieceUomId;
    }

    public int getReasonType() {
        return reasonType;
    }

    public void setReasonType(int reasonType) {
        this.reasonType = reasonType;
    }

    public String getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(String totalAmount) {
        this.totalAmount = totalAmount;
    }

    public int getTotalQuantity() {
        return totalQuantity;
    }

    public void setTotalQuantity(int totalQuantity) {
        this.totalQuantity = totalQuantity;
    }

    public String getInVoiceNumber() {
        return inVoiceNumber;
    }

    public void setInVoiceNumber(String inVoiceNumber) {
        this.inVoiceNumber = inVoiceNumber;
    }



    public int getdUomQty() {
        return dUomQty;
    }

    public void setdUomQty(int dUomQty) {
        this.dUomQty = dUomQty;
    }

    public int getdUomId() {
        return dUomId;
    }

    public void setdUomId(int dUomId) {
        this.dUomId = dUomId;
    }

    public int getdOUomId() {
        return dOUomId;
    }

    public void setdOUomId(int dOUomId) {
        this.dOUomId = dOUomId;
    }

    public int getdOUomQty() {
        return dOUomQty;
    }

    public void setdOUomQty(int dOUomQty) {
        this.dOUomQty = dOUomQty;
    }

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


    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public int getReturnPieceQuantity() {
        return returnPieceQuantity;
    }

    public void setReturnPieceQuantity(int returnPieceQuantity) {
        this.returnPieceQuantity = returnPieceQuantity;
    }

    public int getReturnCaseQuantity() {
        return returnCaseQuantity;
    }

    public void setReturnCaseQuantity(int returnCaseQuantity) {
        this.returnCaseQuantity = returnCaseQuantity;
    }

    public int getActualPieceQuantity() {
        return actualPieceQuantity;
    }

    public void setActualPieceQuantity(int actualPieceQuantity) {
        this.actualPieceQuantity = actualPieceQuantity;
    }

    public int getActualCaseQuantity() {
        return actualCaseQuantity;
    }

    public void setActualCaseQuantity(int actualCaseQuantity) {
        this.actualCaseQuantity = actualCaseQuantity;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public void setProductId(String productId) {
        ProductId = productId;
    }

    public String getProductId() {
        return ProductId;
    }


    public String getReasonCategory() {
        return reasonCategory;
    }

    public void setReasonCategory(String reasonCategory) {
        this.reasonCategory = reasonCategory;
    }

    public String getReasonID() {
        return reasonID;
    }

    public void setReasonID(String reasonID) {
        this.reasonID = reasonID;
    }

    public int getOuterQty() {
        return outerQty;
    }

    public void setOuterQty(int outerQty) {
        this.outerQty = outerQty;
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

    public String getMfgDate() {
        return mfgDate;
    }

    public void setMfgDate(String mfgDate) {
        this.mfgDate = mfgDate;
    }

    public String getExpDate() {
        return expDate;
    }

    public void setExpDate(String expDate) {
        this.expDate = expDate;
    }

    public float getSrp() {
        return srp;
    }

    public void setSrp(float srp) {
        this.srp = srp;
    }

    public float getSrpedit() {
        return srpedit;
    }

    public void setSrpedit(float srpedit) {
        this.srpedit = srpedit;
    }

    public double getOldMrp() {
        return oldMrp;
    }

    public void setOldMrp(double oldMrp) {
        this.oldMrp = oldMrp;
    }

    public int getLotNumber() {
        return lotNumber;
    }

    public void setLotNumber(int lotNumber) {
        this.lotNumber = lotNumber;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getRetailerId() {
        return retailerId;
    }

    public void setRetailerId(String retailerId) {
        this.retailerId = retailerId;
    }
}
