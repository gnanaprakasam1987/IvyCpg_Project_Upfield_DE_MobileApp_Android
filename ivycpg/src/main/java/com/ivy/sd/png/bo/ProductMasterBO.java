package com.ivy.sd.png.bo;

import android.support.annotation.NonNull;

import com.ivy.cpg.view.order.tax.TaxBO;
import com.ivy.cpg.view.salesreturn.SalesReturnReasonBO;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class ProductMasterBO implements Comparable {

    // Download from product master
    private String ProductID;
    private String productCode;
    private String productShortName;
    private String ProductName;
    private int cParentid;
    private int parentid;
    private String barCode;
    private int SIH;
    private int isfocus;
    private float vat;
    private int WSIH;
    private int isAllocation;
    private double MRP;                 //Case UOMID
    private int defaultUOMSize;
    private int msqQty;
    private int caseUomId;
    // Download From PriceMaster
    private float srp;
    // Download Form UOMMaster
    private String OU;

    // Transaction
    private int orderedCaseQty;
    private int orderedPcsQty;
    private int retPieceQty;
    private int retOuterQty;

    private boolean isCheked;
    private String retailerWiseProductWiseP4Qty = "0,0,0,0";
    private String retailerWiseP4StockQty = "0,0,0,0";
    private int soInventory;
    private int ico;
    private int socInventory;
    private boolean isSBD;
    private boolean isSBDAcheived;
    private boolean isSBDAcheivedLocal;
    private String sbdGroupName;
    private int dropQty;
    private int isListed;
    private int isDistributed;
    private int init_caseqty;
    private int init_pieceqty;
    private int isPurchased;
    private int isInitiativeProduct;
    private int initDropSize;
    private double applyValue;
    // rep means stock replacement
    private int repPieceQty;
    private int repOuterQty;
    private int repCaseQty;
    private int orderedBatchCount;
    private int io_op;
    private int io_oc;
    private int qty_klgs;
    private int rfield1_klgs;
    private int rfield2_klgs;
    private String calc_klgs;


    private int io_oo;
    private int indicativeOrder_op;
    private int indicativeOrder_oc;
    private int indicativeOrder_oo;
    private int soreasonId = 0;
    private double csTotal = 0;

    private int deliveredCaseQty;
    private int deliveredPcsQty;
    private int deliveredOuterQty;
    private int companyId = 0;
    private int DSIH;

    public ArrayList<Integer> getTaggedLocations() {
        if(taggedLocations==null)
        {
            taggedLocations=new ArrayList<>();
        }
        return taggedLocations;
    }

    public void setTaggedLocations(ArrayList<Integer> taggedLocations) {
        this.taggedLocations = taggedLocations;
    }

    private ArrayList<Integer> taggedLocations;

    private boolean isSeparateBill;

    private String marginPrice;

    public String getMarginPrice() {
        return marginPrice;
    }

    public void setMarginPrice(String marginPrice) {
        this.marginPrice = marginPrice;
    }




    // To maintain original SRP value given in master, in case of updating @srp with some other values(SRP without tax).
    private float originalSrp;
    private int isDrug;
    private String parentHierarchy = "";

    public int getTotalStockQty() {
        return totalStockQty;
    }

    public void setTotalStockQty(int totalStockQty) {
        this.totalStockQty = totalStockQty;
    }

    private int totalStockQty;

    public double getDistiributorSchemeDiscount() {
        return distiributorSchemeDiscount;
    }

    public void setDistiributorSchemeDiscount(double distiributorSchemeDiscount) {
        this.distiributorSchemeDiscount = distiributorSchemeDiscount;
    }

    private double distiributorSchemeDiscount;

    private int foc;

    public ProductMasterBO() {
        //to avoid compile time error when object with no parameter/s created
    }

    public int getOrderedBatchCount() {
        return orderedBatchCount;
    }

    public void setOrderedBatchCount(int orderedBatchCount) {
        this.orderedBatchCount = orderedBatchCount;
    }

    public int getSelectedSalesReturnPosition() {
        return selectedSalesReturnPosition;
    }

    public void setSelectedSalesReturnPosition(int selectedSalesReturnPosition) {
        this.selectedSalesReturnPosition = selectedSalesReturnPosition;
    }

    private int selectedSalesReturnPosition;

    public int getRepPieceQty() {
        return repPieceQty;
    }

    public void setRepPieceQty(int repPieceQty) {
        this.repPieceQty = repPieceQty;
    }

    public int getRepOuterQty() {
        return repOuterQty;
    }

    public void setRepOuterQty(int repOuterQty) {
        this.repOuterQty = repOuterQty;
    }

    public int getRepCaseQty() {
        return repCaseQty;
    }

    public void setRepCaseQty(int repCaseQty) {
        this.repCaseQty = repCaseQty;
    }




    private String prevPrice_ca = "0";
    private String prevPrice_pc = "0";
    private String prevPrice_oo = "0";
    private String price_ca = "0";
    private String price_pc = "0";
    private String price_oo = "0";

    /**
     * added for Scheme
     **/
    private int isscheme;

    // To store selected scheme (free) products
    private List<SchemeProductBO> schemeProducts;
    private SchemeBO schemeBO;

    private double orderPricePiece;
    private double mschemeper;

    public double getMschemeper() {
        return mschemeper;
    }

    public void setMschemeper(double mschemeper) {
        this.mschemeper = mschemeper;
    }

    private String RField1;





    public int getTotalOrderedQtyInPieces() {
        return totalOrderedQtyInPieces;
    }

    public void setTotalOrderedQtyInPieces(int totalOrderedQtyInPieces) {
        this.totalOrderedQtyInPieces = totalOrderedQtyInPieces;
    }

    private int totalOrderedQtyInPieces;



    private boolean ispromo;
    private int isMustSell;
    private int isFocusBrand;
    private int isFocusBrand2;
    private int isFocusBrand3;
    private int isFocusBrand4;
    private int isSMP;
    private int isNMustSell;
    private int mDeadProduct;
    private int mTradePromotion;

    /**
     * Total scheme discount amount applied on line value.
     *
     * @return
     */
    public double getSchemeDiscAmount() {
        return schemeDiscAmount;
    }

    public void setSchemeDiscAmount(double schemeDiscAmount) {
        this.schemeDiscAmount = schemeDiscAmount;
    }


    public double getProductLevelDiscountValue() {
        return productLevelDiscountValue;
    }

    public void setProductLevelDiscountValue(double productDiscAmount) {
        this.productLevelDiscountValue = productDiscAmount;
    }

    public double getTaxAmount() {
        return taxAmount;
    }

    public void setTaxAmount(double taxAmount) {
        this.taxAmount = taxAmount;
    }

    public double getTaxableAmount() {
        return taxableAmount;
    }

    public void setTaxableAmount(double taxableAmount) {
        this.taxableAmount = taxableAmount;
    }
    public double getNetValue() {
        return netValue;
    }

    public void setNetValue(double netValue) {
        this.netValue = netValue;
    }
    public double getLineValue() {
        return lineValue;
    }
    /**
     * Product Original Line Value
     * @param lineValue
     */
    public void setLineValue(double lineValue) {
        this.lineValue = lineValue;
    }
    public double getLineValueAfterSchemeApplied() {
        return lineValueAfterSchemeApplied;
    }

    public void setLineValueAfterSchemeApplied(double schemeAppliedValue) {
        this.lineValueAfterSchemeApplied = schemeAppliedValue;
    }

    private double taxAmount;
    private double productLevelDiscountValue = 0;
    private double schemeDiscAmount = 0;

    private double taxableAmount;
    private double lineValue;
    private double lineValueAfterSchemeApplied;
    private double netValue;







    private int isDiscountable;
    private String mfgDate;
    private String expDate;
    private int textColor;
    private double totalamount;
    //Outer UOMID  //Pc UomID
    private int outersize;
    private int ouUomid;
    private int pcUomid;
    private float csrp;
    private float osrp;
    private float baseprice;

    //Default Uomid
    private int defaultUomId;
    private int selectedUomId;

    private int orderedOuterQty;
    private int crownOrderedOuterQty;
    private int crownOrderedCaseQty;
    private int crownOrderedPieceQty;
    private int freePieceQty;
    private int freeCaseQty;
    private int freeOuterQty;

    private int init_outerQty;
    private String casebarcode;
    private String outerbarcode;
    private List<SalesReturnReasonBO> salesReturnReasonList;

    /**
     * create for batch allocation
     */
    private String batchid;
    private String batchNo;
    private double batchwiseTotal;
    private int batchwiseProductCount;
    private int warehouseouter;
    private String invoiceno;
    private String ReasonDesc;
    private String ReasonID = "0";
    private String priceChangeReasonID = "0";
    private int oos = -2;

    private int priceChanged;
    private String price = "0";
    private int priceCompliance;
    private int own = 1;
    private int minprice;
    private int maxPrice;
    private int isSaleable;
    private int isReturnable;
    private int typeID;
    private String typeName;
    private String brandname;
    private double priceoffvalue;
    private int PriceOffId;

    public float getASRP() {
        return priceWithTax;
    }

    public void setASRP(float priceWithTax) {
        this.priceWithTax = priceWithTax;
    }

    private float priceWithTax;

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    private String description;

    private String mrp_ca = "0", mrp_ou = "0", mrp_pc = "0";
    private String prevMRP_ca = "0";

    public String getMrp_ca() {
        return mrp_ca;
    }

    public void setMrp_ca(String mrp_ca) {
        this.mrp_ca = mrp_ca;
    }

    public String getMrp_ou() {
        return mrp_ou;
    }

    public void setMrp_ou(String mrp_ou) {
        this.mrp_ou = mrp_ou;
    }

    public String getMrp_pc() {
        return mrp_pc;
    }

    public void setMrp_pc(String mrp_pc) {
        this.mrp_pc = mrp_pc;
    }

    public String getPrevMRP_ca() {
        return prevMRP_ca;
    }

    public void setPrevMRP_ca(String prevMRP_ca) {
        this.prevMRP_ca = prevMRP_ca;
    }

    public String getPrevMRP_ou() {
        return prevMRP_ou;
    }

    public void setPrevMRP_ou(String prevMRP_ou) {
        this.prevMRP_ou = prevMRP_ou;
    }

    public String getPrevMRP_pc() {
        return prevMRP_pc;
    }

    public void setPrevMRP_pc(String prevMRP_pc) {
        this.prevMRP_pc = prevMRP_pc;
    }

    private String prevMRP_ou = "0";
    private String prevMRP_pc = "0";

    public int getIsNearExpiryTaggedProduct() {
        return isNearExpiryTaggedProduct;
    }

    public void setIsNearExpiryTaggedProduct(int isNearExpiryTaggedProduct) {
        this.isNearExpiryTaggedProduct = isNearExpiryTaggedProduct;
    }

    private int isNearExpiryTaggedProduct = 0;

    public String getPriceMOP() {
        return priceMOP;
    }

    public void setPriceMOP(String priceMOP) {
        this.priceMOP = priceMOP;
    }

    private String priceMOP = "0";

    public int getTotalQty() {
        return totalQty;
    }

    public void setTotalQty(int totalQty) {
        this.totalQty = totalQty;
    }

    private int totalQty;

    public int getTotalScannedQty() {
        return totalScannedQty;
    }

    public void setTotalScannedQty(int totalScannedQty) {
        this.totalScannedQty = totalScannedQty;
    }

    private int totalScannedQty;

    public int getScannedProduct() {
        return scannedProduct;
    }

    public void setScannedProduct(int scannedProduct) {
        this.scannedProduct = scannedProduct;
    }

    private int scannedProduct;

    public float getWeight() {
        return weight;
    }

    public void setWeight(float weight) {
        this.weight = weight;
    }

    private float weight;

    public String getBrandname() {
        return brandname;
    }

    public void setBrandname(String brandname) {
        this.brandname = brandname;
    }

    private int cpsih;

    public int getCpsih() {
        return cpsih;
    }

    public void setCpsih(int cpsih) {
        this.cpsih = cpsih;
    }

    private boolean cbsihAvailable;

    public boolean isCbsihAvailable() {
        return cbsihAvailable;
    }

    public void setCbsihAvailable(boolean cbsihAvailable) {
        this.cbsihAvailable = cbsihAvailable;
    }

    public ProductMasterBO(ProductMasterBO productBO) {
        //
        this.ProductID = productBO.ProductID;
        this.ProductName = productBO.ProductName;
        this.productCode = productBO.productCode;
        this.productShortName = productBO.productShortName;
        this.cParentid = productBO.cParentid;
        this.parentid = productBO.parentid;
        this.barCode = productBO.barCode;
        this.SIH = productBO.SIH;
        this.isfocus = productBO.isfocus;
        this.vat = productBO.vat;
        this.WSIH = productBO.WSIH;
        this.isAllocation = productBO.isAllocation;
        this.MRP = productBO.MRP;
        this.defaultUOMSize = productBO.defaultUOMSize;
        this.msqQty = productBO.msqQty;
        this.caseUomId = productBO.caseUomId;
        this.srp = productBO.srp;
        this.OU = productBO.OU;

        this.orderedCaseQty = productBO.orderedCaseQty;
        this.orderedPcsQty = productBO.orderedPcsQty;
        this.retOuterQty = productBO.retOuterQty;
        this.retPieceQty = productBO.retPieceQty;
        this.isCheked = productBO.isCheked;
        this.retailerWiseProductWiseP4Qty = productBO.retailerWiseProductWiseP4Qty;
        this.retailerWiseP4StockQty = productBO.retailerWiseP4StockQty;
        this.socInventory = productBO.socInventory;
        this.soInventory = productBO.soInventory;
        this.isSBD = productBO.isSBD;
        this.isSBDAcheived = productBO.isSBDAcheived;
        this.isSBDAcheivedLocal = productBO.isSBDAcheivedLocal;
        this.sbdGroupName = productBO.sbdGroupName;
        this.dropQty = productBO.dropQty;
        this.init_caseqty = productBO.init_caseqty;
        this.init_pieceqty = productBO.init_pieceqty;
        this.isPurchased = productBO.isPurchased;
        this.isInitiativeProduct = productBO.isInitiativeProduct;
        this.initDropSize = productBO.initDropSize;
        this.isscheme = productBO.isscheme;
        this.schemeProducts = productBO.schemeProducts;
        this.schemeBO = productBO.schemeBO;
        this.orderPricePiece = productBO.orderPricePiece;
        this.RField1 = productBO.RField1;
        this.netValue = productBO.netValue;
        this.ispromo = productBO.ispromo;
        this.isMustSell = productBO.isMustSell;
        this.isFocusBrand = productBO.isFocusBrand;
        this.isFocusBrand2 = productBO.isFocusBrand2;
        this.isNMustSell = productBO.isNMustSell;
        this.mfgDate = productBO.mfgDate;
        this.expDate = productBO.expDate;
        this.textColor = productBO.textColor;
        this.totalamount = productBO.totalamount;

        this.outersize = productBO.outersize;
        this.ouUomid = productBO.ouUomid;
        this.pcUomid = productBO.pcUomid;
        this.csrp = productBO.csrp;
        this.osrp = productBO.osrp;
        this.orderedOuterQty = productBO.orderedOuterQty;
        this.init_outerQty = productBO.init_outerQty;
        this.casebarcode = productBO.casebarcode;
        this.outerbarcode = productBO.outerbarcode;
        this.salesReturnReasonList = productBO.salesReturnReasonList;
        this.batchid = productBO.batchid;
        this.batchNo = productBO.batchNo;
        this.batchwiseTotal = productBO.batchwiseTotal;
        this.batchwiseProductCount = productBO.batchwiseProductCount;
        this.warehouseouter = productBO.warehouseouter;
        this.invoiceno = productBO.invoiceno;

        this.ReasonDesc = productBO.ReasonDesc;
        this.ReasonID = productBO.ReasonID;
        this.priceChangeReasonID = productBO.priceChangeReasonID;
        this.oos = productBO.oos;
        this.taxableAmount = productBO.taxableAmount;
        this.lineValueAfterSchemeApplied = productBO.lineValueAfterSchemeApplied;
        this.own = productBO.own;

        this.csPiece = productBO.getCsPiece();
        this.csCase = productBO.getCsCase();
        this.csOuter = productBO.getCsOuter();
        this.soreasonId = productBO.getSoreasonId();
        this.csFreePiece = productBO.getCsFreePiece();
        this.csTotal = productBO.getCsTotal();
        this.isSchemeDiscount = productBO.isSchemeDiscount();
        this.hsnId = productBO.getHsnId();
        this.hsnCode = productBO.getHsnCode();
        this.defaultUomId = productBO.getDefaultUomId();
        this.selectedUomId = productBO.getSelectedUomId();
        this.mDeadProduct = productBO.getmDeadProduct();
        this.isDistributed = productBO.getIsDistributed();
    }

    // ******* Location ********
    public int getIsListed() {
        return isListed;
    }

    public void setIsListed(int isListed) {
        this.isListed = isListed;
    }

    public int getIsDistributed() {
        return isDistributed;
    }

    public void setIsDistributed(int isDistributed) {
        this.isDistributed = isDistributed;
    }

    private ArrayList<LocationBO> locations;

    public ArrayList<LocationBO> getLocations() {
        return locations;
    }

    public void setLocations(ArrayList<LocationBO> locations) {
        this.locations = locations;
    }

    // ******* Taxes ********
    private final ArrayList<TaxBO> taxes = new ArrayList<>();

    public ArrayList<TaxBO> getTaxes() {
        return taxes;
    }

    // **************************************
    public int getSocInventory() {
        return socInventory;
    }

    public void setSocInventory(int socInventory) {
        this.socInventory = socInventory;
    }

    public String getReasonID() {
        return ReasonID;
    }

    public void setReasonID(String reasonID) {
        ReasonID = reasonID;
    }

    public String getCasebarcode() {
        return casebarcode;
    }

    public void setCasebarcode(String casebarcode) {
        this.casebarcode = casebarcode;
    }

    public String getOuterbarcode() {
        return outerbarcode;
    }

    public void setOuterbarcode(String outerbarcode) {
        this.outerbarcode = outerbarcode;
    }

    public int getOrderedOuterQty() {
        return orderedOuterQty;
    }

    public void setOrderedOuterQty(int outerOrderedCaseQty) {
        this.orderedOuterQty = outerOrderedCaseQty;
    }

    public int getOutersize() {
        return outersize;
    }

    public void setOutersize(int outersize) {
        this.outersize = outersize;
    }

    public int getPcUomid() {
        return pcUomid;
    }

    public void setPcUomid(int dpcuomid) {
        this.pcUomid = dpcuomid;
    }

    public int getOuUomid() {
        return ouUomid;
    }

    public void setOuUomid(int douomid) {
        this.ouUomid = douomid;
    }

    public float getCsrp() {
        return csrp;
    }

    public void setCsrp(float csrp) {
        this.csrp = csrp;
    }

    public float getOsrp() {
        return osrp;
    }

    public void setOsrp(float osrp) {
        this.osrp = osrp;
    }

    /**
     * This is a value of line item after subracting scheme and Discounts.
     *
     * @return
     */


    private double d1;
    private double d2;
    private double d3;
    private double da;

    public void setD1(double d1) {
        this.d1 = d1;
    }

    public double getD1() {
        return d1;
    }

    public double getD2() {
        return d2;
    }

    public double getD3() {
        return d3;
    }

    public void setD2(double d2) {
        this.d2 = d2;
    }

    public void setD3(double d3) {
        this.d3 = d3;
    }

    public String getRField1() {
        return RField1;
    }

    public void setRField1(String rField1) {
        RField1 = rField1;
    }

    public List<SchemeProductBO> getSchemeProducts() {
        return schemeProducts;
    }

    public void setSchemeProducts(List<SchemeProductBO> schemeProducts) {
        this.schemeProducts = schemeProducts;
    }

    public SchemeBO getSchemeBO() {
        return schemeBO;
    }

    public void setSchemeBO(SchemeBO schemeBO) {
        this.schemeBO = schemeBO;
    }

    public int getCaseUomId() {
        return caseUomId;
    }

    public void setCaseUomId(int ouUomId) {
        this.caseUomId = ouUomId;
    }

    public String getOU() {
        return OU;
    }

    public void setOU(String oU) {
        OU = oU;
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

    public int getCaseSize() {
        return defaultUOMSize;
    }

    public void setCaseSize(int oU_QTY) {
        defaultUOMSize = oU_QTY;
    }

    public int getMSQty() {
        return msqQty;
    }

    public void setMSQty(int mSQ_Qty) {
        msqQty = mSQ_Qty;
    }

    public float getSrp() {
        return srp;
    }

    public void setSrp(float price_Value) {
        srp = price_Value;
    }

    public double getCompanyTypeDiscount() {
        return companyTypeDiscount;
    }

    public void setCompanyTypeDiscount(double companyTypeDiscount) {
        this.companyTypeDiscount = companyTypeDiscount;
    }

    public double getDistributorTypeDiscount() {
        return distributorTypeDiscount;
    }

    public void setDistributorTypeDiscount(double distributorTypeDiscount) {
        this.distributorTypeDiscount = distributorTypeDiscount;
    }

    private double companyTypeDiscount;
    private double distributorTypeDiscount;

    @Override
    public String toString() {
        if (ProductName != null)
            return ProductName;
        else
            return "";
    }

    public String getProductID() {
        return ProductID;
    }

    public void setProductID(String productID) {
        ProductID = productID;
    }

    public String getProductName() {
        return ProductName;
    }

    public void setProductName(String productName) {
        ProductName = productName;
    }

    public int getParentid() {
        return parentid;
    }

    public void setParentid(int parentid) {
        this.parentid = parentid;
    }

    public int getcParentid() {
        return cParentid;
    }

    public void setcParentid(int cParentid) {
        this.cParentid = cParentid;
    }

    public double getMRP() {
        return MRP;
    }

    public void setMRP(double mrp) {
        MRP = mrp;
    }

    public int getSIH() {
        return SIH;
    }

    public void setSIH(int sih) {
        SIH = sih;
    }

    public void setProductShortName(String productShortName) {
        this.productShortName = productShortName;
    }

    public String getProductShortName() {
        return productShortName;
    }

    public void setProductCode(String productCode) {
        this.productCode = productCode;
    }

    public String getProductCode() {
        return productCode;
    }

    public void setCheked(boolean isCheked) {
        this.isCheked = isCheked;
    }

    public boolean isCheked() {
        return isCheked;
    }

    public void setBarCode(String barCode) {
        this.barCode = barCode;
    }

    public String getBarCode() {
        return barCode;
    }

    public float getVat() {
        return vat;
    }

    public void setVat(float vat) {
        this.vat = vat;
    }

    public String getRetailerWiseProductWiseP4Qty() {
        return retailerWiseProductWiseP4Qty;
    }

    public void setRetailerWiseProductWiseP4Qty(
            String retailerWiseProductWiseP4Qty) {
        this.retailerWiseProductWiseP4Qty = retailerWiseProductWiseP4Qty;
    }

    public String getRetailerWiseP4StockQty() {
        return retailerWiseP4StockQty;
    }

    public void setRetailerWiseP4StockQty(String retailerWiseP4StockQty) {
        this.retailerWiseP4StockQty = retailerWiseP4StockQty;
    }

    public int getRetPieceQty() {
        return retPieceQty;
    }

    public void setRetPieceQty(int retPieceQty) {
        this.retPieceQty = retPieceQty;
    }

    public int getSoInventory() {
        return soInventory;
    }

    public void setSoInventory(int soInventory) {
        this.soInventory = soInventory;
    }

    public boolean isRPS() {
        return isSBD;
    }

    public void setSBDProduct(boolean isRPS) {
        this.isSBD = isRPS;
    }

    public int getInit_caseqty() {
        return init_caseqty;
    }

    public void setLocalOrderCaseqty(int init_caseqty) {
        this.init_caseqty = init_caseqty;
    }

    public int getInit_pieceqty() {
        return init_pieceqty;
    }

    public void setLocalOrderPieceqty(int init_pieceqty) {
        this.init_pieceqty = init_pieceqty;
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

    public int getIco() {
        return ico;
    }

    public void setICO(int soInventory_temp) {
        this.ico = soInventory_temp;
    }

    public boolean isSBDAcheived() {
        return isSBDAcheived;
    }

    public void setSBDAcheived(boolean isSBDAcheived) {
        this.isSBDAcheived = isSBDAcheived;
    }

    public String getSbdGroupName() {
        return sbdGroupName;
    }

    public void setSbdGroupName(String sbdGroupName) {
        this.sbdGroupName = sbdGroupName;
    }

    public int getDropQty() {
        return dropQty;
    }

    public void setDropQty(int dropQty) {
        this.dropQty = dropQty;
    }

    public boolean isSBDAcheivedLocal() {
        return isSBDAcheivedLocal;
    }

    public void setSBDAcheivedLocal(boolean isSBDAcheivedLocal) {
        this.isSBDAcheivedLocal = isSBDAcheivedLocal;
    }

    public int getInitDropSize() {
        return initDropSize;
    }

    public void setInitDropSize(int initDropSize) {
        this.initDropSize = initDropSize;
    }

    public double getOrderPricePiece() {
        return orderPricePiece;
    }

    public void setOrderPricePiece(double orderPricePiece) {
        this.orderPricePiece = orderPricePiece;
    }

    public int getWSIH() {
        return WSIH;
    }

    public void setWSIH(int wSIH) {
        WSIH = wSIH;
    }

    public int isAllocation() {
        return isAllocation;
    }

    public void setAllocation(int isAllocation) {
        this.isAllocation = isAllocation;
    }

    public boolean isPromo() {
        return ispromo;
    }

    public void setIsPromo(boolean ispromo) {
        this.ispromo = ispromo;
    }

    public double getDA() {
        return da;
    }

    public void setDA(double da) {
        this.da = da;
    }

    public int getIsMustSell() {
        return isMustSell;
    }

    public void setIsMustSell(int isMustSell) {
        this.isMustSell = isMustSell;
    }

    public String getMfgDate() {
        return mfgDate;
    }

    public String getBatchid() {
        return batchid;
    }

    public void setBatchid(String batchid) {
        this.batchid = batchid;
    }

    public String getBatchNo() {
        return batchNo;
    }

    public void setBatchNo(String batchNo) {
        this.batchNo = batchNo;
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

    public int getTextColor() {
        return textColor;
    }

    public void setTextColor(int textColor) {
        this.textColor = textColor;
    }

    public int getIsFocusBrand() {
        return isFocusBrand;
    }

    public void setIsFocusBrand(int isFocusBrand) {
        this.isFocusBrand = isFocusBrand;
    }

    public int getIsFocusBrand2() {
        return isFocusBrand2;
    }

    public void setIsFocusBrand2(int isFocuBrand2) {
        this.isFocusBrand2 = isFocuBrand2;
    }

    public double getTotalamount() {
        return totalamount;
    }

    public void setTotalamount(double totalamount) {
        this.totalamount = totalamount;
    }

    public void setLocalOrderOuterQty(int init_outerQty) {
        this.init_outerQty = init_outerQty;
    }

    public int getInit_OuterQty() {
        return init_outerQty;
    }

    public double getBatchwiseTotal() {
        return batchwiseTotal;
    }

    public void setBatchwiseTotal(double batchwiseTotal) {
        this.batchwiseTotal = batchwiseTotal;
    }

    public static final Comparator<ProductMasterBO> SKUWiseAscending = new Comparator<ProductMasterBO>() {

        @Override
        public int compare(ProductMasterBO PM1, ProductMasterBO PM2) {
            return PM1.getProductName().compareTo(PM2.getProductName());
        }
    };

    public List<SalesReturnReasonBO> getSalesReturnReasonList() {
        if (salesReturnReasonList == null)
            return new ArrayList<>();
        return salesReturnReasonList;
    }

    public void setSalesReturnReasonList(
            List<SalesReturnReasonBO> salesReturnReasonList) {
        this.salesReturnReasonList = salesReturnReasonList;
    }

    public int getBatchwiseProductCount() {
        return batchwiseProductCount;
    }

    public void setBatchwiseProductCount(int batchwiseProductCount) {
        this.batchwiseProductCount = batchwiseProductCount;
    }

    public int getOos() {
        return oos;
    }

    public void setOos(int oos) {
        this.oos = oos;
    }

    public int getPriceChanged() {
        return priceChanged;
    }

    public void setPriceChanged(int priceChanged) {
        this.priceChanged = priceChanged;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public int getPriceCompliance() {
        return priceCompliance;
    }

    public void setPriceCompliance(int priceCompliance) {
        this.priceCompliance = priceCompliance;
    }

    public int getOwn() {
        return own;
    }

    public void setOwn(int own) {
        this.own = own;
    }

    public int getMinprice() {
        return minprice;
    }

    public void setMinprice(int minprice) {
        this.minprice = minprice;
    }

    public int getMaxPrice() {
        return maxPrice;
    }

    public void setMaxPrice(int maxPrice) {
        this.maxPrice = maxPrice;
    }

    public int getIsSaleable() {
        return isSaleable;
    }

    public void setIsSaleable(int isSaleable) {
        this.isSaleable = isSaleable;
    }

    public int getIsReturnable() {
        return isReturnable;
    }

    public void setIsReturnable(int isReturnable) {
        this.isReturnable = isReturnable;
    }

    public int getTypeID() {
        return typeID;
    }

    public void setTypeID(int typeID) {
        this.typeID = typeID;
    }

    public int getCrownOrderedOuterQty() {
        return crownOrderedOuterQty;
    }

    public void setCrownOrderedOuterQty(int crownOrderedOuterQty) {
        this.crownOrderedOuterQty = crownOrderedOuterQty;
    }

    public int getCrownOrderedCaseQty() {
        return crownOrderedCaseQty;
    }

    public void setCrownOrderedCaseQty(int crownOrderedCaseQty) {
        this.crownOrderedCaseQty = crownOrderedCaseQty;
    }

    public int getCrownOrderedPieceQty() {
        return crownOrderedPieceQty;
    }

    public void setCrownOrderedPieceQty(int crownOrderedPieceQty) {
        this.crownOrderedPieceQty = crownOrderedPieceQty;
    }

    public String getTypeName() {
        return typeName;
    }

    public void setTypeName(String typeName) {
        this.typeName = typeName;
    }

    public int getFreePieceQty() {
        return freePieceQty;
    }

    public void setFreePieceQty(int freePieceQty) {
        this.freePieceQty = freePieceQty;
    }

    public int getFreeCaseQty() {
        return freeCaseQty;
    }

    public void setFreeCaseQty(int freeCaseQty) {
        this.freeCaseQty = freeCaseQty;
    }

    public int getFreeOuterQty() {
        return freeOuterQty;
    }

    public void setFreeOuterQty(int freeOuterQty) {
        this.freeOuterQty = freeOuterQty;
    }

    public float getBaseprice() {
        return baseprice;
    }

    public void setBaseprice(float baseprice) {
        this.baseprice = baseprice;
    }

    public String getPrevPrice_ca() {
        return prevPrice_ca;
    }

    public void setPrevPrice_ca(String prevPrice_ca) {
        this.prevPrice_ca = prevPrice_ca;
    }

    public String getPrevPrice_pc() {
        return prevPrice_pc;
    }

    public void setPrevPrice_pc(String prevPrice_pc) {
        this.prevPrice_pc = prevPrice_pc;
    }

    public String getPrevPrice_oo() {
        return prevPrice_oo;
    }

    public void setPrevPrice_oo(String prevPrice_oo) {
        this.prevPrice_oo = prevPrice_oo;
    }

    public String getPrice_ca() {
        return price_ca;
    }

    public void setPrice_ca(String price_ca) {
        this.price_ca = price_ca;
    }

    public String getPrice_pc() {
        return price_pc;
    }

    public void setPrice_pc(String price_pc) {
        this.price_pc = price_pc;
    }

    public String getPrice_oo() {
        return price_oo;
    }

    public void setPrice_oo(String price_oo) {
        this.price_oo = price_oo;
    }






    public double getPriceoffvalue() {
        return priceoffvalue;
    }

    public void setPriceoffvalue(double priceoffvalue) {
        this.priceoffvalue = priceoffvalue;
    }

    public int getPriceOffId() {
        return PriceOffId;
    }

    public void setPriceOffId(int priceOffId) {
        PriceOffId = priceOffId;
    }

    public double getApplyValue() {
        return applyValue;
    }

    public void setApplyValue(double applyValue) {
        this.applyValue = applyValue;
    }

    public int getIsNMustSell() {
        return isNMustSell;
    }

    public void setIsNMustSell(int isNMustSell) {
        this.isNMustSell = isNMustSell;
    }

    public int getIsDiscountable() {
        return isDiscountable;
    }

    public void setIsDiscountable(int isDiscountable) {
        this.isDiscountable = isDiscountable;
    }

    public int getCsPiece() {
        return csPiece;
    }

    public void setCsPiece(int csPiece) {
        this.csPiece = csPiece;
    }

    private int csPiece;

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

    private int csCase;
    private int csOuter;

    private int groupid;
    private int from_range;
    private int to_range;

    public int getCsFreePiece() {
        return csFreePiece;
    }

    public void setCsFreePiece(int csFreePiece) {
        this.csFreePiece = csFreePiece;
    }

    private int csFreePiece;

    public int getTo_range() {
        return to_range;
    }

    public void setTo_range(int to_range) {
        this.to_range = to_range;
    }

    public int getGroupid() {
        return groupid;
    }

    public void setGroupid(int groupid) {
        this.groupid = groupid;
    }

    public int getFrom_range() {
        return from_range;
    }

    public void setFrom_range(int from_range) {
        this.from_range = from_range;
    }

    public int getIsFocusBrand3() {
        return isFocusBrand3;
    }

    public void setIsFocusBrand3(int isFocusBrand3) {
        this.isFocusBrand3 = isFocusBrand3;
    }

    public int getIsFocusBrand4() {
        return isFocusBrand4;
    }

    public void setIsFocusBrand4(int isFocusBrand4) {
        this.isFocusBrand4 = isFocusBrand4;
    }

    public int getIsSMP() {
        return isSMP;
    }

    public void setIsSMP(int isSMP) {
        this.isSMP = isSMP;
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


    // Own productId mapped to competitor product
    // Used only in competitor list
    private String ownPID;

    public String getOwnPID() {
        return ownPID;
    }

    public void setOwnPID(String ownPID) {
        this.ownPID = ownPID;
    }


    //

    public int getIndicative_flex_oo() {
        return io_oo;
    }

    public void setIndicative_flex_oo(int io_oo) {
        this.io_oo = io_oo;
    }

    public int getIndicative_flex_op() {
        return io_op;
    }

    public void setIndicative_flex_op(int io_op) {
        this.io_op = io_op;
    }

    public int getIndicative_flex_oc() {
        return io_oc;
    }


    public void setIndicative_flex_oc(int io_oc) {
        this.io_oc = io_oc;
    }

    //
    public int getIndicativeOrder_oo() {
        return indicativeOrder_oo;
    }

    public void setIndicativeOrder_oo(int indicativeOrder_oo) {
        this.indicativeOrder_oo = indicativeOrder_oo;
    }

    public int getIndicativeOrder_op() {
        return indicativeOrder_op;
    }

    public void setIndicativeOrder_op(int indicativeOrder_op) {
        this.indicativeOrder_op = indicativeOrder_op;
    }


    public int getIndicativeOrder_oc() {
        return indicativeOrder_oc;
    }

    public void setIndicativeOrder_oc(int indicativeOrder_oc) {
        this.indicativeOrder_oc = indicativeOrder_oc;
    }


    public int getSoreasonId() {
        return soreasonId;
    }

    public void setSoreasonId(int soreasonId) {
        this.soreasonId = soreasonId;
    }

    @Override
    public boolean equals(Object object) {
        boolean isContains = false;

        if (object != null && object instanceof ProductMasterBO) {
            isContains = this.ProductID == ((ProductMasterBO) object).ProductID;
        }

        return isContains;
    }

    private int freeSIH;

    public int getFreeSIH() {
        return freeSIH;
    }

    public void setFreeSIH(int freeSIH) {
        this.freeSIH = freeSIH;
    }


    public double getCsTotal() {
        return csTotal;
    }

    public void setCsTotal(double csTotal) {
        this.csTotal = csTotal;
    }

    public int getQty_klgs() {
        return qty_klgs;
    }

    public void setQty_klgs(int qty_klgs) {
        this.qty_klgs = qty_klgs;
    }

    public int getRfield1_klgs() {
        return rfield1_klgs;
    }

    public void setRfield1_klgs(int rfield1_klgs) {
        this.rfield1_klgs = rfield1_klgs;
    }

    public int getRfield2_klgs() {
        return rfield2_klgs;
    }

    public void setRfield2_klgs(int rfield2_klgs) {
        this.rfield2_klgs = rfield2_klgs;
    }

    public String getCalc_klgs() {
        return calc_klgs;
    }

    public void setCalc_klgs(String calc_klgs) {
        this.calc_klgs = calc_klgs;
    }

    private double csFreeTotal;

    public double getCsFreeTotal() {
        return csFreeTotal;
    }

    public void setCsFreeTotal(double csFreeTotal) {
        this.csFreeTotal = csFreeTotal;
    }

    public boolean isAccessory() {
        return isAccessory;
    }

    public void setAccessory(boolean accessory) {
        isAccessory = accessory;
    }

    boolean isAccessory;

    public String getColorCode() {
        return colorCode;
    }

    public void setColorCode(String colorCode) {
        this.colorCode = colorCode;
    }

    public int getProductSequence() {
        return productSequence;
    }

    public void setProductSequence(int productSequence) {
        this.productSequence = productSequence;
    }

    private String colorCode;
    int productSequence;

    public static final Comparator<ProductMasterBO> SequenceComparator = new Comparator<ProductMasterBO>() {

        public int compare(ProductMasterBO mProductBO1, ProductMasterBO mProductBO2) {

            int sequence1 = mProductBO1.getProductSequence();
            int sequence2 = mProductBO2.getProductSequence();

            if (sequence1 == 0 || sequence2 == 0) {
                return mProductBO1.getProductShortName().compareToIgnoreCase(mProductBO2.getProductShortName());
            }
            return sequence1 - sequence2;
        }

    };
    public static final Comparator<ProductMasterBO> ProductNameComparator = new Comparator<ProductMasterBO>() {

        public int compare(ProductMasterBO mProduct1, ProductMasterBO mProduct2) {

            return mProduct1.getProductShortName().compareTo(mProduct2.getProductShortName());
        }

    };

    // used for counter sales
    public boolean isChildProduct() {
        return isChildProduct;
    }

    public void setChildProduct(boolean childProduct) {
        isChildProduct = childProduct;
    }

    boolean isChildProduct;

    boolean isSchemeDiscount;

    public boolean isSchemeDiscount() {
        return isSchemeDiscount;
    }

    public void setSchemeDiscount(boolean schemeDiscount) {
        isSchemeDiscount = schemeDiscount;
    }

    public int getDeliveredCaseQty() {
        return deliveredCaseQty;
    }

    public void setDeliveredCaseQty(int deliveredCaseQty) {
        this.deliveredCaseQty = deliveredCaseQty;
    }

    public int getDeliveredPcsQty() {
        return deliveredPcsQty;
    }

    public void setDeliveredPcsQty(int deliveredPcsQty) {
        this.deliveredPcsQty = deliveredPcsQty;
    }

    public int getDeliveredOuterQty() {
        return deliveredOuterQty;
    }

    public void setDeliveredOuterQty(int deliveredOuterQty) {
        this.deliveredOuterQty = deliveredOuterQty;
    }

    boolean isAvailableinWareHouse;

    public boolean isAvailableinWareHouse() {
        return isAvailableinWareHouse;
    }

    public void setAvailableinWareHouse(boolean availableinWareHouse) {
        isAvailableinWareHouse = availableinWareHouse;
    }

    public float getOriginalSrp() {
        return originalSrp;
    }

    public void setOriginalSrp(float originalSrp) {
        this.originalSrp = originalSrp;
    }

    public int getCompanyId() {
        return companyId;
    }

    public void setCompanyId(int companyId) {
        this.companyId = companyId;
    }

    public int getCompParentId() {
        return compParentId;
    }

    public void setCompParentId(int compParentId) {
        this.compParentId = compParentId;
    }

    private int compParentId;

    private int hsnId;
    private String hsnCode = "";

    public int getHsnId() {
        return hsnId;
    }

    public void setHsnId(int hsnId) {
        this.hsnId = hsnId;
    }

    public String getHsnCode() {
        return hsnCode;
    }

    public void setHsnCode(String hsnCode) {
        this.hsnCode = hsnCode;
    }

    public void setIsDrug(int isDrug) {
        this.isDrug = isDrug;
    }

    public int getIsDrug() {
        return isDrug;
    }

    public int getFoc() {
        return foc;
    }

    public void setFoc(int foc) {
        this.foc = foc;
    }

    public int getDSIH() {
        return DSIH;
    }

    public void setDSIH(int DSIH) {
        this.DSIH = DSIH;
    }

    public String getParentHierarchy() {
        return parentHierarchy;
    }

    public void setParentHierarchy(String parentHierarchy) {
        this.parentHierarchy = parentHierarchy;
    }

    public boolean isSeparateBill() {
        return isSeparateBill;
    }

    public void setSeparateBill(boolean separateBill) {
        isSeparateBill = separateBill;
    }

    String remarks = "";

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    public String getRemarks() {
        return remarks;
    }

    private String allocationQty;

    public String getAllocationQty() {
        return allocationQty;
    }

    public void setAllocationQty(String allocationQty) {
        this.allocationQty = allocationQty;
    }

    public void setDefaultUomId(int defaultUomId) {
        this.defaultUomId = defaultUomId;
    }

    public int getDefaultUomId() {
        return defaultUomId;
    }

    public void setSelectedUomId(int selectedUomId) {
        this.selectedUomId = selectedUomId;
    }

    public int getSelectedUomId() {
        return selectedUomId;
    }

    private ArrayList<StandardListBO> productWiseUomList;

    public void setProductWiseUomList(ArrayList<StandardListBO> productWiseUomList) {
        this.productWiseUomList = productWiseUomList;
    }

    public ArrayList<StandardListBO> getProductWiseUomList() {
        return productWiseUomList;
    }

    private int selectedUomPosition;

    public int getSelectedUomPosition() {
        return selectedUomPosition;
    }

    public void setSelectedUomPosition(int selectedUomPosition) {
        this.selectedUomPosition = selectedUomPosition;
    }

    public String getPriceChangeReasonID() {
        return priceChangeReasonID;
    }

    public void setPriceChangeReasonID(String priceChangeReasonID) {
        this.priceChangeReasonID = priceChangeReasonID;
    }

    public int getmDeadProduct() {
        return mDeadProduct;
    }

    public void setmDeadProduct(int mDeadProduct) {
        this.mDeadProduct = mDeadProduct;
    }

    private int increasedPcs;

    public int getIncreasedPcs() {
        return increasedPcs;
    }

    public void setIncreasedPcs(int increasedPcs) {
        this.increasedPcs = increasedPcs;
    }

    private float tempSrp;

    public float getTempSrp() {
        return tempSrp;
    }

    public void setTempSrp(float tempSrp) {
        this.tempSrp = tempSrp;
    }

    int lastVisitColor,lastVisit1Color,lastVisit2Color,lastVisit3Color;

    public int getLastVisitColor() {
        return lastVisitColor;
    }

    public void setLastVisitColor(int lastVisitColor) {
        this.lastVisitColor = lastVisitColor;
    }

    public int getLastVisit1Color() {
        return lastVisit1Color;
    }

    public void setLastVisit1Color(int lastVisit1Color) {
        this.lastVisit1Color = lastVisit1Color;
    }

    public int getLastVisit2Color() {
        return lastVisit2Color;
    }

    public void setLastVisit2Color(int lastVisit2Color) {
        this.lastVisit2Color = lastVisit2Color;
    }

    public int getLastVisit3Color() {
        return lastVisit3Color;
    }

    public void setLastVisit3Color(int lastVisit3Color) {
        this.lastVisit3Color = lastVisit3Color;
    }

    private int available;

    public int getAvailable() {
        return available;
    }

    public void setAvailable(int available) {
        this.available = available;
    }

    @Override
    public int compareTo(@NonNull Object o) {
        int available=((ProductMasterBO)o).getAvailable();
        /* For Ascending order*/
        return this.available-available;
    }

    public int getmTradePromotion() {
        return mTradePromotion;
    }

    public void setmTradePromotion(int mTradePromotion) {
        this.mTradePromotion = mTradePromotion;
    }
}
