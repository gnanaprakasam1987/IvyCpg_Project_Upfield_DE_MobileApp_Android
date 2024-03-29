package com.ivy.sd.png.bo;

public class StockReportBO {

    private String productName, productShortName, productCode = "", barcode,
            rfield1, batchNo = "";
    private int categoryId;
    private int brandId;
    private int sih;



    private int freeSIH;
    private int vanLoadQty;



    private int vanLoadFreeQty;
    private int soldQty;
    private int freeIssuedQty;
    private int emptyBottleQty;

    public StockReportBO() {

    }

    public StockReportBO(StockReportBO bo) {
        this.freeIssuedQty_cs = bo.freeIssuedQty_cs;
        this.freeIssuedQty_ou = bo.freeIssuedQty_ou;
        this.freeIssuedQty_pc = bo.freeIssuedQty_pc;
        this.emptyBottleQty_cs = bo.emptyBottleQty_cs;
        this.emptyBottleQty_ou = bo.emptyBottleQty_ou;
        this.emptyBottleQty_pc = bo.emptyBottleQty_pc;
        this.returnQty_cs = bo.returnQty_cs;
        this.returnQty_ou = bo.returnQty_ou;
        this.returnQty_pc = bo.returnQty_pc;
        this.replacementQty_cs = bo.replacementQty_cs;
        this.replacementQty_pc = bo.replacementQty_pc;
        this.replacemnetQty_ou = bo.replacemnetQty_ou;
        this.soldQty_cs = bo.soldQty_cs;
        this.soldQty_ou = bo.soldQty_ou;
        this.soldQty_pc = bo.soldQty_pc;
        this.vanLoadQty_cs = bo.vanLoadQty_cs;
        this.vanLoadQty_ou = bo.vanLoadQty_ou;
        this.vanLoadQty_pc = bo.vanLoadQty_pc;
        this.productName = bo.productName;
        this.productShortName = bo.productShortName;
        this.productCode = bo.productCode;
        this.batchNo = bo.batchNo;
        this.batchId = bo.batchId;
        this.freeIssuedQty = bo.freeIssuedQty;
        this.emptyBottleQty = bo.emptyBottleQty;
        this.soldQty = bo.soldQty;
        this.vanLoadQty = bo.vanLoadQty;
        this.brandId = bo.brandId;
        this.categoryId = bo.categoryId;
        this.rfield1 = bo.rfield1;
        this.barcode = bo.barcode;
        this.sih = bo.sih;
        this.returnQty = bo.returnQty;
        this.replacementQty = bo.replacementQty;
        this.caseSize = bo.caseSize;
        this.outerSize = bo.outerSize;
        this.productID = bo.productID;
        this.isBaseUomPieceWise = bo.isBaseUomPieceWise;
        this.isBaseUomCaseWise = bo.isBaseUomCaseWise;
        this.isBaseUomOuterWise = bo.isBaseUomOuterWise;
        this.piece_uomid = bo.piece_uomid;
        this.dUomid = bo.dUomid;
        this.dOuomid = bo.dOuomid;
        this.sih_cs = bo.sih_cs;
        this.sih_ou = bo.sih_ou;
        this.sih_pc = bo.sih_pc;
        this.freeSIH = bo.freeSIH;
        this.vanLoadFreeQty = bo.vanLoadFreeQty;
        this.vanLoadFree_cs = bo.vanLoadFree_cs;
        this.vanLoadFree_ou = bo.vanLoadFree_ou;
        this.vanLoadFree_pc = bo.vanLoadFree_pc;
        this.freeSIH_cs = bo.freeSIH_cs;
        this.freeSIH_ou = bo.freeSIH_ou;
        this.freeSIH_pc = bo.freeSIH_pc;
    }

    public String getBatchId() {
        return batchId;
    }

    public void setBatchId(String batchId) {
        this.batchId = batchId;
    }

    private String batchId;

    public int getReturnQty() {
        return returnQty;
    }

    public void setReturnQty(int returnQty) {
        this.returnQty = returnQty;
    }

    private int returnQty;

    public int getReplacementQty() {
        return replacementQty;
    }

    public void setReplacementQty(int replacementQty) {
        this.replacementQty = replacementQty;
    }

    private int replacementQty;
    private int caseSize, outerSize;
    private String productID;

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

    public String getProductCode() {
        return productCode;
    }

    public void setProductCode(String productCode) {
        this.productCode = productCode;
    }

    public int getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(int categoryId) {
        this.categoryId = categoryId;
    }

    public int getBrandId() {
        return brandId;
    }

    public void setBrandId(int brandId) {
        this.brandId = brandId;
    }

    public int getSih() {
        return sih;
    }

    public void setSih(int sih) {
        this.sih = sih;
    }

    public String getBarcode() {
        return barcode;
    }

    public void setBarcode(String barcode) {
        this.barcode = barcode;
    }

    public String getRfield1() {
        return rfield1;
    }

    public void setRfield1(String rfield1) {
        this.rfield1 = rfield1;
    }

    public String getBatchNo() {
        return batchNo;
    }

    public void setBatchNo(String batchNo) {
        this.batchNo = batchNo;
    }

    public int getVanLoadQty() {
        return vanLoadQty;
    }

    public void setVanLoadQty(int vanLoadQty) {
        this.vanLoadQty = vanLoadQty;
    }

    public int getSoldQty() {
        return soldQty;
    }

    public void setSoldQty(int soldQty) {
        this.soldQty = soldQty;
    }

    public int getFreeIssuedQty() {
        return freeIssuedQty;
    }

    public void setFreeIssuedQty(int freeIssuedQty) {
        this.freeIssuedQty = freeIssuedQty;
    }

    public int getEmptyBottleQty() {
        return emptyBottleQty;
    }

    public void setEmptyBottleQty(int emptyBottleQty) {
        this.emptyBottleQty = emptyBottleQty;
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

    public String getProductID() {
        return productID;
    }

    public void setProductID(String productID) {
        this.productID = productID;
    }

    private boolean isBaseUomPieceWise;
    private boolean isBaseUomCaseWise;
    private boolean isBaseUomOuterWise;

    public boolean isBaseUomOuterWise() {
        return isBaseUomOuterWise;
    }

    public void setBaseUomOuterWise(boolean baseUomOuterWise) {
        isBaseUomOuterWise = baseUomOuterWise;
    }

    public boolean isBaseUomPieceWise() {
        return isBaseUomPieceWise;
    }

    public void setBaseUomPieceWise(boolean baseUomPieceWise) {
        isBaseUomPieceWise = baseUomPieceWise;
    }

    public boolean isBaseUomCaseWise() {
        return isBaseUomCaseWise;
    }

    public void setBaseUomCaseWise(boolean baseUomCaseWise) {
        isBaseUomCaseWise = baseUomCaseWise;
    }


    public int getPiece_uomid() {
        return piece_uomid;
    }

    public void setPiece_uomid(int piece_uomid) {
        this.piece_uomid = piece_uomid;
    }

    public int getdUomid() {
        return dUomid;
    }

    public void setdUomid(int dUomid) {
        this.dUomid = dUomid;
    }

    public int getdOuomid() {
        return dOuomid;
    }

    public void setdOuomid(int dOuomid) {
        this.dOuomid = dOuomid;
    }

    private int piece_uomid, dUomid, dOuomid;

    //For uom wise split configuration
    private int sih_cs, sih_ou, sih_pc;
    private int vanLoadQty_cs, vanLoadQty_ou, vanLoadQty_pc;
    private int soldQty_cs, soldQty_ou, soldQty_pc;
    private int freeIssuedQty_cs, freeIssuedQty_ou, freeIssuedQty_pc;
    private int emptyBottleQty_cs, emptyBottleQty_ou, emptyBottleQty_pc;
    private int returnQty_cs, returnQty_ou, returnQty_pc;
    private int replacementQty_cs;
    private int replacementQty_pc;
    private int replacemnetQty_ou;
    private int vanLoadFree_cs;
    private int vanLoadFree_ou;
    private int vanLoadFree_pc;
    private int freeSIH_cs, freeSIH_ou, freeSIH_pc;

    public int getVanLoadFree_cs() {
        return vanLoadFree_cs;
    }

    public void setVanLoadFree_cs(int vanLoadFree_cs) {
        this.vanLoadFree_cs = vanLoadFree_cs;
    }

    public int getVanLoadFree_ou() {
        return vanLoadFree_ou;
    }

    public void setVanLoadFree_ou(int vanLoadFree_ou) {
        this.vanLoadFree_ou = vanLoadFree_ou;
    }

    public int getVanLoadFree_pc() {
        return vanLoadFree_pc;
    }

    public void setVanLoadFree_pc(int vanLoadFree_pc) {
        this.vanLoadFree_pc = vanLoadFree_pc;
    }

    public int getFreeSIH_cs() {
        return freeSIH_cs;
    }

    public void setFreeSIH_cs(int freeSIH_cs) {
        this.freeSIH_cs = freeSIH_cs;
    }

    public int getFreeSIH_ou() {
        return freeSIH_ou;
    }

    public void setFreeSIH_ou(int freeSIH_ou) {
        this.freeSIH_ou = freeSIH_ou;
    }

    public int getFreeSIH_pc() {
        return freeSIH_pc;
    }

    public void setFreeSIH_pc(int freeSIH_pc) {
        this.freeSIH_pc = freeSIH_pc;
    }

    public int getReplacementQty_pc() {
        return replacementQty_pc;
    }

    public void setReplacementQty_pc(int replacementQty_pc) {
        this.replacementQty_pc = replacementQty_pc;
    }






    public int getSih_pc() {
        return sih_pc;
    }

    public void setSih_pc(int sih_pc) {
        this.sih_pc = sih_pc;
    }

    public int getVanLoadQty_pc() {
        return vanLoadQty_pc;
    }

    public void setVanLoadQty_pc(int vanLoadQty_pc) {
        this.vanLoadQty_pc = vanLoadQty_pc;
    }

    public int getSoldQty_pc() {
        return soldQty_pc;
    }

    public void setSoldQty_pc(int soldQty_pc) {
        this.soldQty_pc = soldQty_pc;
    }

    public int getFreeIssuedQty_pc() {
        return freeIssuedQty_pc;
    }

    public void setFreeIssuedQty_pc(int freeIssuedQty_pc) {
        this.freeIssuedQty_pc = freeIssuedQty_pc;
    }

    public int getEmptyBottleQty_pc() {
        return emptyBottleQty_pc;
    }

    public void setEmptyBottleQty_pc(int emptyBottleQty_pc) {
        this.emptyBottleQty_pc = emptyBottleQty_pc;
    }

    public int getReturnQty_pc() {
        return returnQty_pc;
    }

    public void setReturnQty_pc(int returnQty_pc) {
        this.returnQty_pc = returnQty_pc;
    }


    public int getReturnQty_cs() {
        return returnQty_cs;
    }

    public void setReturnQty_cs(int returnQty_cs) {
        this.returnQty_cs = returnQty_cs;
    }

    public int getReturnQty_ou() {
        return returnQty_ou;
    }

    public void setReturnQty_ou(int returnQty_ou) {
        this.returnQty_ou = returnQty_ou;
    }

    public int getReplacementQty_cs() {
        return replacementQty_cs;
    }

    public void setReplacementQty_cs(int replacementQty_cs) {
        this.replacementQty_cs = replacementQty_cs;
    }

    public int getReplacemnetQty_ou() {
        return replacemnetQty_ou;
    }

    public void setReplacemnetQty_ou(int replacemnetQty_ou) {
        this.replacemnetQty_ou = replacemnetQty_ou;
    }


    public int getSih_cs() {
        return sih_cs;
    }

    public void setSih_cs(int sih_cs) {
        this.sih_cs = sih_cs;
    }

    public int getEmptyBottleQty_ou() {
        return emptyBottleQty_ou;
    }

    public void setEmptyBottleQty_ou(int emptyBottleQty_ou) {
        this.emptyBottleQty_ou = emptyBottleQty_ou;
    }

    public int getSih_ou() {
        return sih_ou;
    }

    public void setSih_ou(int sih_ou) {
        this.sih_ou = sih_ou;
    }

    public int getVanLoadQty_cs() {
        return vanLoadQty_cs;
    }

    public void setVanLoadQty_cs(int vanLoadQty_cs) {
        this.vanLoadQty_cs = vanLoadQty_cs;
    }

    public int getVanLoadQty_ou() {
        return vanLoadQty_ou;
    }

    public void setVanLoadQty_ou(int vanLoadQty_ou) {
        this.vanLoadQty_ou = vanLoadQty_ou;
    }

    public int getSoldQty_cs() {
        return soldQty_cs;
    }

    public void setSoldQty_cs(int soldQty_cs) {
        this.soldQty_cs = soldQty_cs;
    }

    public int getSoldQty_ou() {
        return soldQty_ou;
    }

    public void setSoldQty_ou(int soldQty_ou) {
        this.soldQty_ou = soldQty_ou;
    }

    public int getFreeIssuedQty_cs() {
        return freeIssuedQty_cs;
    }

    public void setFreeIssuedQty_cs(int freeIssuedQty_cs) {
        this.freeIssuedQty_cs = freeIssuedQty_cs;
    }

    public int getFreeIssuedQty_ou() {
        return freeIssuedQty_ou;
    }

    public void setFreeIssuedQty_ou(int freeIssuedQty_ou) {
        this.freeIssuedQty_ou = freeIssuedQty_ou;
    }

    public int getEmptyBottleQty_cs() {
        return emptyBottleQty_cs;
    }

    public void setEmptyBottleQty_cs(int emptyBottleQty_cs) {
        this.emptyBottleQty_cs = emptyBottleQty_cs;
    }

    private int nonsalableQty_pc, nonsalableQty_cs, nonsalableQty_ou;
    private int nonSalableQty;

    public int getNonsalableQty_pc() {
        return nonsalableQty_pc;
    }

    public void setNonsalableQty_pc(int nonsalableQty_pc) {
        this.nonsalableQty_pc = nonsalableQty_pc;
    }

    public int getNonsalableQty_cs() {
        return nonsalableQty_cs;
    }

    public void setNonsalableQty_cs(int nonsalableQty_cs) {
        this.nonsalableQty_cs = nonsalableQty_cs;
    }

    public int getNonsalableQty_ou() {
        return nonsalableQty_ou;
    }

    public void setNonsalableQty_ou(int nonsalableQty_ou) {
        this.nonsalableQty_ou = nonsalableQty_ou;
    }

    public int getNonSalableQty() {
        return nonSalableQty;
    }

    public void setNonSalableQty(int nonSalableQty) {
        this.nonSalableQty = nonSalableQty;
    }

    private int vanUnloadQty;
    private int vanUnloadQty_pc, vanUnloadQty_cs, vanUnloadQty_ou;

    public int getVanUnloadQty() {
        return vanUnloadQty;
    }

    public void setVanUnloadQty(int vanUnloadQty) {
        this.vanUnloadQty = vanUnloadQty;
    }

    public int getVanUnloadQty_pc() {
        return vanUnloadQty_pc;
    }

    public void setVanUnloadQty_pc(int vanUnloadQty_pc) {
        this.vanUnloadQty_pc = vanUnloadQty_pc;
    }

    public int getVanUnloadQty_cs() {
        return vanUnloadQty_cs;
    }

    public void setVanUnloadQty_cs(int vanUnloadQty_cs) {
        this.vanUnloadQty_cs = vanUnloadQty_cs;
    }

    public int getVanUnloadQty_ou() {
        return vanUnloadQty_ou;
    }

    public void setVanUnloadQty_ou(int vanUnloadQty_ou) {
        this.vanUnloadQty_ou = vanUnloadQty_ou;
    }

    public int getFreeSIH() {
        return freeSIH;
    }

    public void setFreeSIH(int freeSIH) {
        this.freeSIH = freeSIH;
    }

    public int getVanLoadFreeQty() {
        return vanLoadFreeQty;
    }

    public void setVanLoadFreeQty(int vanLoadFreeQty) {
        this.vanLoadFreeQty = vanLoadFreeQty;
    }

}
