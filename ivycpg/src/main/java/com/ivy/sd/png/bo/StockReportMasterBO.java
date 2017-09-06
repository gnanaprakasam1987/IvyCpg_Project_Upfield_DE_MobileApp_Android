package com.ivy.sd.png.bo;

import java.util.Comparator;

public class StockReportMasterBO {
    private int caseqty, pcsqty, duomqty, productId, outerQty, dOuomQty,
            totalQty, BatchId;

    private String productName, productShortName;
    private float mrp, basePrice;
    private String uid;
    private String loadNO;
    private String date;
    private int isManuvalVanload;

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
        return dOuomQty;
    }

    public void setOuterSize(int dOuomQty) {
        this.dOuomQty = dOuomQty;
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

    public int getCaseqty() {
        return caseqty;
    }

    public void setCaseqty(int caseqty) {
        this.caseqty = caseqty;
    }

    public int getPieceqty() {
        return pcsqty;
    }

    public void setPieceqty(int pcsqty) {
        this.pcsqty = pcsqty;
    }

    public int getCasesize() {
        return duomqty;
    }

    public void setCasesize(int duomqty) {
        this.duomqty = duomqty;
    }

    public String getProductname() {
        return productName;
    }

    public void setProductname(String productName) {
        this.productName = productName;
    }

    public String getProductshortname() {
        return productShortName;
    }

    public void setProductshortname(String productShortName) {
        this.productShortName = productShortName;
    }

    public int getProductid() {
        return productId;
    }

    public void setProductid(int productId) {
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
    public String getBatch_number() {
        return batch_number;
    }

    /**
     * @param batch_number the batch_number to set
     */
    public void setBatch_number(String batch_number) {
        this.batch_number = batch_number;
    }

    private String batch_number;

    public float getBasePrice() {
        return basePrice;
    }

    public void setBasePrice(float basePrice) {
        this.basePrice = basePrice;
    }

    public int getIsManuvalVanload() {
        return isManuvalVanload;
    }

    public void setIsManuvalVanload(int isManuvalVanload) {
        this.isManuvalVanload = isManuvalVanload;
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

}
