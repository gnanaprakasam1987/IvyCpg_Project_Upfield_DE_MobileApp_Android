package com.ivy.cpg.view.van.vanstockapply;

import java.util.Comparator;

public class VanLoadStockApplyBO {
    private int caseQuantity, pcsQuantity, caseSize, productId, outerQty, outerSize,
            totalQty, BatchId;
    private String batchNumber;

    private String productName, productShortName;
    private float mrp, basePrice;
    private String uid;
    private String loadNO;
    private String date;
    private int isManualVanload;
    private String ProductCode;
    private int isFree;

    public int getBatchId() {
        return BatchId;
    }

    public void setBatchId(int batchId) {
        BatchId = batchId;
    }

    public int getTotalQty() {
        return totalQty;
    }

    public void setTotalQty(int totalQty) {
        this.totalQty = totalQty;
    }

    public int getOuterSize() {
        return outerSize;
    }

    public void setOuterSize(int dOuomQty) {
        this.outerSize = dOuomQty;
    }

    public int getOuterQty() {
        return outerQty;
    }

    public void setOuterQty(int outerQty) {
        this.outerQty = outerQty;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public int getCaseQuantity() {
        return caseQuantity;
    }

    public void setCaseQuantity(int caseQuantity) {
        this.caseQuantity = caseQuantity;
    }

    public int getPieceQuantity() {
        return pcsQuantity;
    }

    public void setPieceQuantity(int pcsqty) {
        this.pcsQuantity = pcsqty;
    }

    public int getCaseSize() {
        return caseSize;
    }

    public void setCaseSize(int duomqty) {
        this.caseSize = duomqty;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getProductShortName() {
        return productShortName;
    }

    public void setProductShortName(String productShortName) {
        this.productShortName = productShortName;
    }

    public int getProductId() {
        return productId;
    }

    public void setProductId(int productId) {
        this.productId = productId;

    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public float getMrp() {
        return mrp;
    }

    public void setMrp(float mrp) {
        this.mrp = mrp;
    }

    /**
     * @return the batch_number
     */
    public String getBatchNumber() {
        return batchNumber;
    }

    /**
     * @param batchNumber the batch_number to set
     */
    public void setBatchNumber(String batchNumber) {
        this.batchNumber = batchNumber;
    }

    public float getBasePrice() {
        return basePrice;
    }

    public void setBasePrice(float basePrice) {
        this.basePrice = basePrice;
    }

    public int getIsManualVanload() {
        return isManualVanload;
    }

    public void setIsManualVanload(int isManualVanload) {
        this.isManualVanload = isManualVanload;
    }

    public String getLoadNO() {
        return loadNO;
    }

    public void setLoadNO(String loadNO) {
        this.loadNO = loadNO;
    }

    public static final Comparator<VanLoadSpinnerBO> uIDComparator = new Comparator<VanLoadSpinnerBO>() {

        public int compare(VanLoadSpinnerBO fruit1, VanLoadSpinnerBO fruit2) {

            return fruit2.getSpinnerTxt().compareToIgnoreCase(fruit1.getSpinnerTxt());
        }

    };

    public void setProductCode(String productCode) {
        ProductCode = productCode;
    }

    public String getProductCode() {
        return ProductCode;
    }

    public int getIsFree() {
        return isFree;
    }

    public void setIsFree(int isFree) {
        this.isFree = isFree;
    }
}
