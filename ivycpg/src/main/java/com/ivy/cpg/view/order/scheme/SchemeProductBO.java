package com.ivy.cpg.view.order.scheme;

import java.util.ArrayList;

/**
 * @author vinodh.r
 */
public class SchemeProductBO {
    private String productId;
    private String productName;
    private String productCode = "";
    private String productFullName;
    // Scheme Slab id
    private String schemeId;

    // Buy product qty
    private double buyQty;
    private double tobuyQty;

    // For free items - Downloaded from server
    private int quantityMinimum;
    private int quantityMaximum;

    // Price range - Downloaded from server
    private double priceMinimum;
    private double priceMaximum;

    // Max & Min Discount in total amount
    private double minAmount;
    private double maxAmount;
    // Max & Min Discount percentage in total amount
    private double minPercent;
    private double maxPercent;

    /**
     * Transaction
     **/

    // Calculated for per product
    // quantityActual / SchemeBO.getBuyQty - Min & Max value
    private int quantityActualCalculated;
    // quantityMaximum / SchemeBO.getBuyQty
    private int quantityMaxiumCalculated;

    // Calculated for per product
    // quantityActual / SchemeBO.getBuyQty - Min & Max value
    private double minPercentCalculated;
    // quantityMaximum / SchemeBO.getBuyQty
    private double maxPrecentCalculated;

    private double minAmountCalculated;
    private double maxAmountCalculated;

    /**
     * User entered free qty will be here
     **/
    private int quantitySelected;
    private int stock;

    // Especially for combination scheme
    private String groupName;
    private String groupLogic;

    private int uomID; // Scheme buy qty uom Type
    private String uomDescription;
    private String buyType; // SV or QTY

    private String batchId;
    private double discountValue;

    public double getLineValue() {
        return lineValue;
    }

    public void setLineValue(double lineValue) {
        this.lineValue = lineValue;
    }

    public double getTaxAmount() {
        return taxAmount;
    }

    public void setTaxAmount(double taxAmount) {
        this.taxAmount = taxAmount;
    }

    private double lineValue, taxAmount;

    public double getDiscountValue() {
        return discountValue;
    }

    public void setDiscountValue(double discountValue) {
        this.discountValue = discountValue;
    }


    private ArrayList<SchemeProductBatchQty> batchWiseQty = new ArrayList<>();


    public String getBatchId() {
        return batchId;
    }

    public void setBatchId(String batchId) {
        this.batchId = batchId;
    }

    public double getBuyQty() {
        return buyQty;
    }

    public void setBuyQty(double buyQty) {
        this.buyQty = buyQty;
    }

    public double getTobuyQty() {
        return tobuyQty;
    }

    public void setTobuyQty(double tobuyQty) {
        this.tobuyQty = tobuyQty;
    }

    /**
     * @return the productId
     */
    public String getProductId() {
        return productId;
    }

    /**
     * @return the productName
     */
    public String getProductName() {
        return productName;
    }

    /**
     * @return the productFullName
     */
    public String getProductFullName() {
        return productFullName;
    }

    /**
     * @param productFullName the productFullName to set
     */
    public void setProductFullName(String productFullName) {
        this.productFullName = productFullName;
    }

    /**
     * @return the quantityActual
     */
    public int getQuantityMinimum() {
        return quantityMinimum;
    }

    /**
     * @return the quantityMaximum
     */
    public int getQuantityMaximum() {
        return quantityMaximum;
    }

    /**
     * @return the priceActual
     */
    public double getPriceActual() {
        return priceMinimum;
    }

    /**
     * @return the priceMaximum
     */
    public double getPriceMaximum() {
        return priceMaximum;
    }

    /**
     * @param productId the productId to set
     */
    public void setProductId(String productId) {
        this.productId = productId;
    }

    /**
     * @param productName the productName to set
     */
    public void setProductName(String productName) {
        this.productName = productName;
    }

    /**
     * @param quantityActual the quantityActual to set
     */
    public void setQuantityMinimum(int quantityActual) {
        this.quantityMinimum = quantityActual;
    }

    /**
     * @param quantityMaximum the quantityMaximum to set
     */
    public void setQuantityMaximum(int quantityMaximum) {
        this.quantityMaximum = quantityMaximum;
    }

    /**
     * @param priceActual the priceActual to set
     */
    public void setPriceActual(double priceActual) {
        this.priceMinimum = priceActual;
    }

    /**
     * @param priceMaximum the priceMaximum to set
     */
    public void setPriceMaximum(double priceMaximum) {
        this.priceMaximum = priceMaximum;
    }

    /**
     * @return the quantityActualCalculated
     */
    public int getQuantityActualCalculated() {
        return quantityActualCalculated;
    }

    /**
     * @return the quantityMaxiumCalculated
     */
    public int getQuantityMaxiumCalculated() {
        return quantityMaxiumCalculated;
    }

    /**
     * @param quantityActualCalculated the quantityActualCalculated to set
     */
    public void setQuantityActualCalculated(int quantityActualCalculated) {
        this.quantityActualCalculated = quantityActualCalculated;
    }

    /**
     * @param quantityMaxiumCalculated the quantityMaxiumCalculated to set
     */
    public void setQuantityMaxiumCalculated(int quantityMaxiumCalculated) {
        this.quantityMaxiumCalculated = quantityMaxiumCalculated;
    }

    /**
     * @return the quantitySelected
     */
    public int getQuantitySelected() {
        return quantitySelected;
    }

    /**
     * @param quantitySelected the quantitySelected to set
     */
    public void setQuantitySelected(int quantitySelected) {
        this.quantitySelected = quantitySelected;
    }

    @Override
    public String toString() {
        return this.productName;
    }

    /**
     * @return the stock
     */
    public int getStock() {
        return stock;
    }

    /**
     * @param stock the stock to set
     */
    public void setStock(int stock) {
        this.stock = stock;
    }

    /**
     * Scheme Slab Id
     *
     * @return Scheme Slab Id
     */
    public String getSchemeId() {
        return schemeId;
    }

    /**
     * Set Scheme Slab Id
     *
     * @param schemeId - id of the product
     */
    public void setSchemeId(String schemeId) {
        this.schemeId = schemeId;
    }

    public double getMinAmount() {
        return minAmount;
    }

    public void setMinAmount(double minAmount) {
        this.minAmount = minAmount;
    }

    public double getMaxAmount() {
        return maxAmount;
    }

    public void setMaxAmount(double maxAmount) {
        this.maxAmount = maxAmount;
    }

    public double getMinPercent() {
        return minPercent;
    }

    public void setMinPercent(double minPercent) {
        this.minPercent = minPercent;
    }

    public double getMaxPercent() {
        return maxPercent;
    }

    public void setMaxPercent(double maxPercent) {
        this.maxPercent = maxPercent;
    }


    public ArrayList<SchemeProductBatchQty> getBatchWiseQty() {
        return batchWiseQty;
    }

    public void setBatchWiseQty(ArrayList<SchemeProductBatchQty> batchWiseQty) {
        this.batchWiseQty = batchWiseQty;
    }

    public double getMinPercentCalculated() {
        return minPercentCalculated;
    }

    public void setMinPercentCalculated(double minPercentCalculated) {
        this.minPercentCalculated = minPercentCalculated;
    }

    public double getMaxPrecentCalculated() {
        return maxPrecentCalculated;
    }

    public void setMaxPrecentCalculated(double maxPrecentCalculated) {
        this.maxPrecentCalculated = maxPrecentCalculated;
    }

    public double getMinAmountCalculated() {
        return minAmountCalculated;
    }

    public void setMinAmountCalculated(double minAmountCalculated) {
        this.minAmountCalculated = minAmountCalculated;
    }

    public double getMaxAmountCalculated() {
        return maxAmountCalculated;
    }

    public void setMaxAmountCalculated(double maxAmountCalculated) {
        this.maxAmountCalculated = maxAmountCalculated;
    }

    public String getGroupLogic() {
        return groupLogic;
    }

    public void setGroupBuyType(String groupBuyType) {
        this.groupLogic = groupBuyType;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public int getUomID() {
        return uomID;
    }

    public void setUomID(int uomID) {
        this.uomID = uomID;
    }

    public String getUomDescription() {
        return uomDescription;
    }

    public void setUomDescription(String uomDescription) {
        this.uomDescription = uomDescription;
    }

    public String getBuyType() {
        return buyType;
    }

    public void setBuyType(String buyType) {
        this.buyType = buyType;
    }
// apply range wise scheme use this toBuyQty


    public String getAccProductParentId() {
        return accProductParentId;
    }

    public void setAccProductParentId(String accProductParentId) {
        this.accProductParentId = accProductParentId;
    }

    // Scheme id-- Added to handle accumulation free products
    private String accProductParentId;

    public int getQuantityUsedForScheme() {
        return quantityUsedForScheme;
    }

    public void setQuantityUsedForScheme(int quantityUsedForScheme) {
        this.quantityUsedForScheme = quantityUsedForScheme;
    }

    private int quantityUsedForScheme;

    public int getDeliverQtyPcs() {
        return deliverQtyPcs;
    }

    public void setDeliverQtyPcs(int deliverQtyPcs) {
        this.deliverQtyPcs = deliverQtyPcs;
    }

    private int deliverQtyPcs;

    public int getDeliverQtyCase() {
        return deliverQtyCase;
    }

    public void setDeliverQtyCase(int deliverQtyCase) {
        this.deliverQtyCase = deliverQtyCase;
    }

    public int getDeliverQtyOuter() {
        return deliverQtyOuter;
    }

    public void setDeliverQtyOuter(int deliverQtyOuter) {
        this.deliverQtyOuter = deliverQtyOuter;
    }

    private int deliverQtyCase;
    private int deliverQtyOuter;

    public void setProductCode(String productCode) {
        this.productCode = productCode;
    }

    /**
     * @return product Code
     */
    public String getProductCode() {
        return productCode;
    }

    //Added for QPS Tracker
    private int orderedPcsQty, increasedPcsQty, orderedCasesQty, increasedCasesQty, totalOrderedQty;
    private float pcsPrice, casesPrice;
    private String parentID, getType;

    public int getOrderedPcsQty() {
        return orderedPcsQty;
    }

    public void setOrderedPcsQty(int orderedPcsQty) {
        this.orderedPcsQty = orderedPcsQty;
    }

    public int getOrderedCasesQty() {
        return orderedCasesQty;
    }

    public void setOrderedCasesQty(int orderedCasesQty) {
        this.orderedCasesQty = orderedCasesQty;
    }

    public int getIncreasedPcsQty() {
        return increasedPcsQty;
    }

    public void setIncreasedPcsQty(int increasedPcsQty) {
        this.increasedPcsQty = increasedPcsQty;
    }

    public int getIncreasedCasesQty() {
        return increasedCasesQty;
    }

    public void setIncreasedCasesQty(int increasedCasesQty) {
        this.increasedCasesQty = increasedCasesQty;
    }

    public float getPcsPrice() {
        return pcsPrice;
    }

    public void setPcsPrice(float pcsPrice) {
        this.pcsPrice = pcsPrice;
    }

    public float getCasesPrice() {
        return casesPrice;
    }

    public void setCasesPrice(float casesPrice) {
        this.casesPrice = casesPrice;
    }

    public int getTotalOrderedQty() {
        return totalOrderedQty;
    }

    public void setTotalOrderedQty(int totalOrderedQty) {
        this.totalOrderedQty = totalOrderedQty;
    }

    public String getParentID() {
        return parentID;
    }

    public void setParentID(String parentID) {
        this.parentID = parentID;
    }

    public String getGetType() {
        return getType;
    }

    public void setGetType(String getType) {
        this.getType = getType;
    }

    boolean pcsSelected, casesSelected;

    public boolean isPcsSelected() {
        return pcsSelected;
    }

    public void setPcsSelected(boolean pcsSelected) {
        this.pcsSelected = pcsSelected;
    }

    public boolean isCasesSelected() {
        return casesSelected;
    }

    public void setCasesSelected(boolean casesSelected) {
        this.casesSelected = casesSelected;
    }
}
