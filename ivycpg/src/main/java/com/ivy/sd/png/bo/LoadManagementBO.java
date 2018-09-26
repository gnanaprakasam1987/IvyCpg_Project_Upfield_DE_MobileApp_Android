package com.ivy.sd.png.bo;

import com.ivy.cpg.view.salesreturn.SalesReturnReasonBO;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

public class LoadManagementBO {

    private int parentid, productid, suggestqty, dUomQty, sih, wsih, isalloc,
            dOuomQty, isMust, maxQty, stkprototalQty, stkpropcsqty,
            stkprocaseqty, stkproouterqty, outerQty;
    private String batchNo;
    private Vector<LoadManagementBO> batchnolist;
    private Vector<LoadManagementBO> batchlist;
    private String parentHierarchy;
    private String productCode = "";

    public Vector<LoadManagementBO> getBatchlist() {
        return batchlist;
    }

    public void setBatchlist(Vector<LoadManagementBO> batchno) {
        this.batchlist = batchno;
    }

    public int getOuterQty() {
        return outerQty;
    }

    public void setOuterQty(int outerQty) {
        this.outerQty = outerQty;
    }

    public LoadManagementBO(LoadManagementBO item) {
        this.productid = item.productid;

        this.dUomQty = item.dUomQty;
        this.dUomid = item.dUomid;
        this.caseqty = item.caseqty;
        this.piece_uomid = item.piece_uomid;
        this.pieceqty = item.pieceqty;
        this.sih = item.sih;

        this.outerQty = item.outerQty;
        this.dOuomQty = item.dOuomQty;
        this.dOuonid = item.dOuonid;
        this.msqQty = item.msqQty;
        this.baseprice = item.baseprice;
        this.productname = item.productname;
        this.productshortname = item.productshortname;
        this.barcode = item.barcode;
        this.pCode = item.pCode;
        this.RField1 = item.RField1;

        this.batchNo = item.batchNo;
        this.batchId = item.batchId;
        this.manualBatchNo = item.manualBatchNo;
        this.mfgDate = item.mfgDate;
        this.expDate = item.expDate;
        this.batchlist = item.batchlist;
        this.batchnolist = item.batchnolist;
        this.productCode = item.productCode;
    }

    private String manualBatchNo, mfgDate, expDate;

    public String getManualBatchNo() {
        return manualBatchNo;
    }

    public void setManualBatchNo(String manualBatchNo) {
        this.manualBatchNo = manualBatchNo;
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

    public LoadManagementBO() {
        // TODO Auto-generated constructor stub
    }

    public String getBatchNo() {
        return batchNo;
    }

    public void setBatchNo(String batchNo) {
        this.batchNo = batchNo;
    }

    public int getStkpropcsqty() {
        return stkpropcsqty;
    }

    public void setStkpropcsqty(int stkpropcsqty) {
        this.stkpropcsqty = stkpropcsqty;
    }

    public int getStkprocaseqty() {
        return stkprocaseqty;
    }

    public void setStkprocaseqty(int stkprocaseqty) {
        this.stkprocaseqty = stkprocaseqty;
    }

    public int getStkproouterqty() {
        return stkproouterqty;
    }

    public void setStkproouterqty(int stkproouterqty) {
        this.stkproouterqty = stkproouterqty;
    }

    public int getStkprototalQty() {
        return stkprototalQty;
    }

    public void setStkprototalQty(int stkprototalQty) {
        this.stkprototalQty = stkprototalQty;
    }

    public int getMaxQty() {
        return maxQty;
    }

    public void setMaxQty(int maxQty) {
        this.maxQty = maxQty;
    }

    public int getParentid() {
        return parentid;
    }

    public void setParentid(int parentid) {
        this.parentid = parentid;
    }

    public int getProductid() {
        return productid;
    }

    public void setProductid(int productid) {
        this.productid = productid;
    }

    public int getSuggestqty() {
        return suggestqty;
    }

    public void setSuggestqty(int suggestqty) {
        this.suggestqty = suggestqty;
    }

    public int getCaseSize() {
        return dUomQty;
    }

    public void setCaseSize(int dUomQty) {
        this.dUomQty = dUomQty;
    }

    public int getSih() {
        return sih;
    }

    public void setSih(int sih) {
        this.sih = sih;
    }

    public int getWsih() {
        return wsih;
    }

    public void setWsih(int wsih) {
        this.wsih = wsih;
    }

    public int getIsalloc() {
        return isalloc;
    }

    public void setIsalloc(int isalloc) {
        this.isalloc = isalloc;
    }

    public int getOuterSize() {
        return dOuomQty;
    }

    public void setOuterSize(int dOuomQty) {
        this.dOuomQty = dOuomQty;
    }

    public int getIsMust() {
        return isMust;
    }

    public void setIsMust(int isMust) {
        this.isMust = isMust;
    }

    public int getStdpcs() {
        return stdpcs;
    }

    public void setStdpcs(int stdpcs) {
        this.stdpcs = stdpcs;
    }

    public int getStdcase() {
        return stdcase;
    }

    public void setStdcase(int stdcase) {
        this.stdcase = stdcase;
    }

    public int getStdouter() {
        return stdouter;
    }

    public void setStdouter(int stdouter) {
        this.stdouter = stdouter;
    }

    public int getdUomid() {
        return dUomid;
    }

    public void setdUomid(int dUomid) {
        this.dUomid = dUomid;
    }

    public int getdOuonid() {
        return dOuonid;
    }

    public void setdOuonid(int dOuonid) {
        this.dOuonid = dOuonid;
    }

    public int getPiece_uomid() {
        return piece_uomid;
    }

    public void setPiece_uomid(int piece_uomid) {
        this.piece_uomid = piece_uomid;
    }

    public int getPLid() {
        return PLid;
    }

    public void setPLid(int pLid) {
        PLid = pLid;
    }

    public int getMsqQty() {
        return msqQty;
    }

    public void setMsqQty(int msqQty) {
        this.msqQty = msqQty;
    }

    public int getIssalable() {
        return issalable;
    }

    public void setIssalable(int issalable) {
        this.issalable = issalable;
    }

    public double getMrp() {
        return mrp;
    }

    public void setMrp(double mrp) {
        this.mrp = mrp;
    }

    public double getBaseprice() {
        return baseprice;
    }

    public void setBaseprice(double baseprice) {
        this.baseprice = baseprice;
    }

    public String getProductname() {
        return productname;
    }

    public void setProductname(String productname) {
        this.productname = productname;
    }

    public String getProductshortname() {
        return productshortname;
    }

    public void setProductshortname(String productshortname) {
        this.productshortname = productshortname;
    }

    public String getBarcode() {
        return barcode;
    }

    public void setBarcode(String barcode) {
        this.barcode = barcode;
    }

    public String getRField1() {
        return RField1;
    }

    public void setRField1(String rField1) {
        RField1 = rField1;
    }

    public String getpCode() {
        return pCode;
    }

    public void setpCode(String pCode) {
        this.pCode = pCode;
    }

    private int stdpcs, stdcase, stdouter, dUomid, dOuonid, piece_uomid, PLid,
            msqQty, issalable;
    private double mrp, baseprice;
    private String productname, productshortname, barcode, RField1, pCode;
    private int isInitiativeProduct;

    public int isAllocation() {
        return isAllocation;
    }

    public void setAllocation(int isAllocation) {
        this.isAllocation = isAllocation;
    }

    public boolean isSBDAcheived() {
        return isSBDAcheived;
    }

    public void setSBDAcheived(boolean isSBDAcheived) {
        this.isSBDAcheived = isSBDAcheived;
    }

    public boolean isRPS() {
        return isSBD;
    }

    public void setSBDProduct(boolean isRPS) {
        this.isSBD = isRPS;
    }

    public int getOrderedCaseQty() {
        return orderedCaseQty;
    }

    public void setOrderedCaseQty(int order_ou_qty) {
        this.orderedCaseQty = order_ou_qty;
    }

    public int getOrderedPcsQty() {
        return orderedPcsQty;
    }

    public void setOrderedPcsQty(int order_msq_qty) {
        this.orderedPcsQty = order_msq_qty;
    }

    public int getIsPurchased() {
        return isPurchased;
    }

    public void setIsPurchased(int isPurchased) {
        this.isPurchased = isPurchased;
    }

    public int getIsInitiativeProduct() {
        return isInitiativeProduct;
    }

    public void setIsInitiativeProduct(int isInitiativeProduct) {
        this.isInitiativeProduct = isInitiativeProduct;
    }

    public boolean isPromo() {
        return isPromo();
    }

    public void setIsPromo(boolean ispromo) {
        this.ispromo = ispromo;
    }

    public int getOuterOrderedCaseQty() {
        return outerOrderedCaseQty;
    }

    public void setOuterOrderedCaseQty(int outerOrderedCaseQty) {
        this.outerOrderedCaseQty = outerOrderedCaseQty;
    }

    boolean ispromo, isSBDAcheived, isSBD;
    int isPurchased, orderedCaseQty, orderedPcsQty, isAllocation,
            outerOrderedCaseQty;
    int caseqty, pieceqty;
    String batchId;

    public int getCaseqty() {
        return caseqty;
    }

    public void setCaseqty(int caseqty) {
        this.caseqty = caseqty;
    }

    public int getPieceqty() {
        return pieceqty;
    }

    public void setPieceqty(int pieceqty) {
        this.pieceqty = pieceqty;

    }

    public String getBatchId() {
        return batchId;
    }

    public void setBatchId(String batchId) {
        this.batchId = batchId;
    }

    public Vector<LoadManagementBO> getBatchnolist() {
        return batchnolist;
    }

    public void setBatchnolist(Vector<LoadManagementBO> batchnolist) {
        this.batchnolist = batchnolist;
    }

    private int adjusted_sih, stocksih, d;

    public int getStocksih() {
        return stocksih;
    }

    public void setStocksih(int stocksih) {
        this.stocksih = stocksih;
    }

    /**
     * @return the adjusted_sih
     */
    public int getAdjusted_sih() {
        return adjusted_sih;
    }

    /**
     * @param adjusted_sih the adjusted_sih to set
     */
    public void setAdjusted_sih(int adjusted_sih) {
        this.adjusted_sih = adjusted_sih;
    }

    /**
     * @return the old_diff_sih
     */
    public int getOld_diff_sih() {
        return old_diff_sih;
    }

    /**
     * @param old_diff_sih the old_diff_sih to set
     */
    public void setOld_diff_sih(int old_diff_sih) {
        this.old_diff_sih = old_diff_sih;
    }

    private int old_diff_sih, diff_sih;

    /**
     * @return the diff_sih
     */
    public int getDiff_sih() {
        return diff_sih;
    }

    /**
     * @param diff_sih the diff_sih to set
     */
    public void setDiff_sih(int diff_sih) {
        this.diff_sih = diff_sih;
    }

    public void calculateDifferenceInSih() {
        this.diff_sih = this.adjusted_sih - this.stocksih;

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

    // UOM's allowed for a product.
    private boolean isPiece = true;
    private boolean isCase = true;
    private boolean isOuter = true;

    public boolean isOuterMapped() {
        return isOuter;
    }

    public void setOuterMapped(boolean outer) {
        isOuter = outer;
    }

    public boolean isPieceMapped() {
        return isPiece;
    }

    public void setPieceMapped(boolean piece) {
        isPiece = piece;
    }

    public boolean isCaseMapped() {
        return isCase;
    }

    public void setCaseMapped(boolean aCase) {
        isCase = aCase;
    }

    public String getParentHierarchy() {
        return parentHierarchy;
    }

    public void setParentHierarchy(String parentHierarchy) {
        this.parentHierarchy = parentHierarchy;
    }

    /**
     * @return product Code
     */
    public String getProductCode() {
        return productCode;
    }

    public void setProductCode(String productCode) {
        this.productCode = productCode;
    }


    private List salesReturnReasonList = null;
    private int nonSalableQty;

    public int getNonSalableQty() {
        return nonSalableQty;
    }

    public void setNonSalableQty(int nonSalableQty) {
        this.nonSalableQty = nonSalableQty;
    }

    public List<SalesReturnReasonBO> getSalesReturnReasonList() {
        if (salesReturnReasonList == null)
            return new ArrayList<>();
        return salesReturnReasonList;
    }

    public void setSalesReturnReasonList(
            List<SalesReturnReasonBO> salesReturnReasonList) {
        this.salesReturnReasonList = salesReturnReasonList;
    }

    private int typeId;
    private String TransactionId = "";

    public int getTypeId() {
        return typeId;
    }

    public void setTypeId(int typeId) {
        this.typeId = typeId;
    }


    public String getTransactionId() {
        return TransactionId;
    }

    public void setTransactionId(String transactionId) {
        TransactionId = transactionId;
    }
}
