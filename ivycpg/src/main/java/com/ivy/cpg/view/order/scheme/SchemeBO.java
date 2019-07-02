package com.ivy.cpg.view.order.scheme;

import java.util.ArrayList;
import java.util.List;

/**
 * @author vinodh.r
 */

/**
 * @author vinodh.r
 *
 */
public class SchemeBO {

    /** Download From tables **/

    private String schemeId;
    private String schemeDescription;
    // SV/QTY
    private String buyType;

    // AND/ANY/ONLY
    private String parentLogic;
    // Scheme product name or scheme group item name
    private String schemeParentName;
    // Buy Quantity - This should actually be in SchemeProductBO
    private int quantity;
    // Not Used
    private String channelId;
    // This can RetailerId/Channelid/SubChannelID
    private String subChannelId;

    // From BuyMaster
    private List<SchemeProductBO> buyingProducts;
    // From FreeMaster
    private List<SchemeProductBO> freeProducts;

    // parent id to the schemeid, multiple scheme will be grouped .
    private int parentId;
    // How many times the scheme should apply in mobile.
    private int noOfTimesApply;

    // Selected Scheme type - either quantity or price based
    private boolean isQuantityTypeSelected;
    private boolean isPriceTypeSeleted;
    private boolean isAmountTypeSelected;
    private boolean isDiscountPrecentSelected;

    /** max and min Qty calculated based in the Order **/
    private int actualQuantity;
    private int maximumQuantity;
    /** Not used for scheme **/
    private int selectedQuantity;

    /** user entered price **/
    private double selectedPrice;
    /** min and max price **/
    private double actualPrice;
    private double maximumPrice;

    /** user entered amount **/
    private double selectedAmount;
    /** max and min amount **/
    private double minimumAmount;
    private double maximumAmount;

    /** user entered precent **/
    private double selectedPrecent;
    /** max and min amount **/
    private double minimumPrecent;
    private double maximumPrecent;

    /** total case and pcs and outer of group products **/
    private String skuBuyProdID;
    private String skuBuyProdName;

    private String groupName;
    private String groupType;

    private int isCombination = 0;
    private int isFreeCombination = 0;
    private int isAutoApply = 0;
    private String freeType;
    private double maximumSlab;

    public boolean isSihAvailableForFreeProducts() {
        return sihAvailableForFreeProducts;
    }

    public void setSihAvailableForFreeProducts(boolean sihAvailableForFreeProducts) {
        this.sihAvailableForFreeProducts = sihAvailableForFreeProducts;
    }

    private boolean sihAvailableForFreeProducts;
    /** EveryUomid and EveyQty object used for calculate amount based scheme to apply multiple time */
    private int everyUomId;
    private int everyQty;

    public int getEveryUomId() {
        return everyUomId;
    }

    public void setEveryUomId(int everyUomId) {
        this.everyUomId = everyUomId;
    }

    public int getEveryQty() {
        return everyQty;
    }

    public void setEveryQty(int everyQty) {
        this.everyQty = everyQty;
    }

    public double getBalancePercent() {
        return balancePercent;
    }

    public void setBalancePercent(double balancePercent) {
        this.balancePercent = balancePercent;
    }

    private double balancePercent;

    public String getProcessType() {
        return processType;
    }

    public void setProcessType(String processType) {
        this.processType = processType;
    }

    private String processType;

    public boolean isBatchWise() {
        return isBatchWise;
    }

    public void setBatchWise(boolean batchWise) {
        isBatchWise = batchWise;
    }

    private boolean isBatchWise;

    public boolean isAccumulationScheme() {
        return isAccumulationScheme;
    }

    public void setAccumulationScheme(boolean accumulationScheme) {
        isAccumulationScheme = accumulationScheme;
    }

    private boolean isAccumulationScheme;

    public int getOrderedProductCount() {
        return orderedProductCount;
    }

    public void setOrderedProductCount(int orderedProductCount) {
        this.orderedProductCount = orderedProductCount;
    }

    // used to get count of ordered product  in scheme
    private int orderedProductCount;

    /**
     * @return the schemeId
     */
    public String getSchemeId() {
        return schemeId;
    }

    /**
     * @return the scheme
     */
    public String getScheme() {
        return schemeDescription;
    }

    /**
     * @return the type
     */
    public String getParentLogic() {
        return parentLogic;
    }

    /**
     * @return the productName
     */
    public String getProductName() {
        return schemeParentName;
    }

    /**
     * @return the buy quantity, for AND, it will be the sum of all the buy
     *         products
     */
    public int getQuantity() {
        return quantity;
    }

    /**
     * @return the channelId
     */
    public String getChannelId() {
        return channelId;
    }

    /**
     * @return the subChannelId
     */
    public String getSubChannelId() {
        return subChannelId;
    }

    /**
     * @return the buyingProducts
     */
    public List<SchemeProductBO> getBuyingProducts() {
        return buyingProducts;
    }

    /**
     * @return the freeProducts
     */
    public List<SchemeProductBO> getFreeProducts() {
        if (freeProducts == null)
            freeProducts=new ArrayList<>();

        return freeProducts;
    }

    /**
     * @param schemeId
     *            the schemeId to set
     */
    public void setSchemeId(String schemeId) {
        this.schemeId = schemeId;
    }

    /**
     * @param scheme
     *            the scheme to set
     */
    public void setSchemeDescription(String scheme) {
        this.schemeDescription = scheme;
    }

    /**
     * @param type
     *            the type to set
     */
    public void setParentLogic(String type) {
        this.parentLogic = type;
    }

    /**
     * @param productName
     *            the productName to set
     */
    public void setSchemeParentName(String productName) {
        this.schemeParentName = productName;
    }

    /**
     * @param quantity
     *            free product qty. If AND logic, then sum will be here
     */
    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    /**
     * @param channelId
     *            the channelId to set
     */
    public void setChannelId(String channelId) {
        this.channelId = channelId;
    }

    /**
     * @param subChannelId
     *            the subChannelId to set
     */
    public void setSubChannelId(String subChannelId) {
        this.subChannelId = subChannelId;
    }

    /**
     * @param buyingProducts
     *            the buyingProducts to set
     */
    public void setBuyingProducts(List<SchemeProductBO> buyingProducts) {
        this.buyingProducts = buyingProducts;
    }

    /**
     * @param freeProducts
     *            the freeProducts to set
     */
    public void setFreeProducts(List<SchemeProductBO> freeProducts) {
        this.freeProducts = freeProducts;
    }

    /**
     * @return the selectedQuantity
     */
    public int getSelectedQuantity() {
        return selectedQuantity;
    }

    /**
     * @param selectedQuantity
     *            the selectedQuantity to set
     */
    public void setSelectedQuantity(int selectedQuantity) {
        this.selectedQuantity = selectedQuantity;
    }

    /**
     * @return the selectedPrice
     */
    public double getSelectedPrice() {
        return selectedPrice;
    }

    /**
     * @param selectedPrice
     *            the selectedPrice to set
     */
    public void setSelectedPrice(double selectedPrice) {
        this.selectedPrice = selectedPrice;
    }

    @Override
    public String toString() {
        return this.schemeDescription;
    }

    /**
     * @return the isQuantityTypeSelected
     */
    public boolean isQuantityTypeSelected() {
        return isQuantityTypeSelected;
    }

    /**
     * @return the isPriceTypeSeleted
     */
    public boolean isPriceTypeSeleted() {
        return isPriceTypeSeleted;
    }

    /**
     * @param isQuantityTypeSelected
     *            the isQuantityTypeSelected to set
     */
    public void setQuantityTypeSelected(boolean isQuantityTypeSelected) {
        this.isQuantityTypeSelected = isQuantityTypeSelected;
    }

    /**
     * @param isPriceTypeSeleted
     *            the isPriceTypeSeleted to set
     */
    public void setPriceTypeSeleted(boolean isPriceTypeSeleted) {
        this.isPriceTypeSeleted = isPriceTypeSeleted;
    }

    /**
     * @return the actualQuantity
     */
    public int getActualQuantity() {
        return actualQuantity;
    }

    /**
     * @return the maximumQuantity
     */
    public int getMaximumQuantity() {
        return maximumQuantity;
    }

    /**
     * @param actualQuantity
     *            the actualQuantity to set
     */
    public void setActualQuantity(int actualQuantity) {
        this.actualQuantity = actualQuantity;
    }

    /**
     * @param maximumQuantity
     *            the maximumQuantity to set
     */
    public void setMaximumQuantity(int maximumQuantity) {
        this.maximumQuantity = maximumQuantity;
    }

    /**
     * @return the actualPrice
     */
    public double getActualPrice() {
        return actualPrice;
    }

    /**
     * @return the maximumPrice
     */
    public double getMaximumPrice() {
        return maximumPrice;
    }

    /**
     * @param actualPrice
     *            the actualPrice to set
     */
    public void setActualPrice(double actualPrice) {
        this.actualPrice = actualPrice;
    }

    /**
     * @param maximumPrice
     *            the maximumPrice to set
     */
    public void setMaximumPrice(double maximumPrice) {
        this.maximumPrice = maximumPrice;
    }

    public int getNoOfTimesApply() {
        return noOfTimesApply;
    }

    public void setNoOfTimesApply(int noOfTimesApply) {
        this.noOfTimesApply = noOfTimesApply;
    }

    public int getParentId() {
        return parentId;
    }

    public void setParentId(int parentId) {
        this.parentId = parentId;
    }

    public boolean isAmountTypeSelected() {
        return isAmountTypeSelected;
    }

    public void setAmountTypeSelected(boolean isAmountTypeSelected) {
        this.isAmountTypeSelected = isAmountTypeSelected;
    }

    public boolean isDiscountPrecentSelected() {
        return isDiscountPrecentSelected;
    }

    public void setDiscountPrecentSelected(boolean isDiscountPrecentSelected) {
        this.isDiscountPrecentSelected = isDiscountPrecentSelected;
    }

    public double getSelectedAmount() {
        return selectedAmount;
    }

    public void setSelectedAmount(double selectedAmount) {
        this.selectedAmount = selectedAmount;
    }

    public double getMinimumAmount() {
        return minimumAmount;
    }

    public void setMinimumAmount(double actualAmount) {
        this.minimumAmount = actualAmount;
    }

    public double getMaximumAmount() {
        return maximumAmount;
    }

    public void setMaximumAmount(double maximumAmount) {
        this.maximumAmount = maximumAmount;
    }

    public double getSelectedPrecent() {
        return selectedPrecent;
    }

    public void setSelectedPrecent(double selectedPrecent) {
        this.selectedPrecent = selectedPrecent;
    }

    public double getMaximumPrecent() {
        return maximumPrecent;
    }

    public void setMaximumPrecent(double maximumPrecent) {
        this.maximumPrecent = maximumPrecent;
    }

    public double getMinimumPrecent() {
        return minimumPrecent;
    }

    public void setMinimumPrecent(double actualPrecent) {
        this.minimumPrecent = actualPrecent;
    }

    public String getSkuBuyProdID() {
        return skuBuyProdID;
    }

    public void setSkuBuyProdID(String skuBuyProdID) {
        this.skuBuyProdID = skuBuyProdID;
    }

    public String getSkuBuyProdName() {
        return skuBuyProdName;
    }

    public void setSkuBuyProdName(String skuBuyProdName) {
        this.skuBuyProdName = skuBuyProdName;
    }

    public String getBuyType() {
        return buyType;
    }

    /**
     *
     * @param buyType
     *            the buyType set whether SV 0r QTY
     */
    public void setBuyType(String buyType) {
        this.buyType = buyType;
    }

    public int getIsCombination() {
        return isCombination;
    }

    public void setIsCombination(int isCombination) {
        this.isCombination = isCombination;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public String getGroupType() {
        return groupType;
    }

    public void setGroupType(String groupType) {
        this.groupType = groupType;
    }

    public int getIsFreeCombination() {
        return isFreeCombination;
    }

    public void setIsFreeCombination(int isFreeCombination) {
        this.isFreeCombination = isFreeCombination;
    }

    public String getFreeType() {
        return freeType;
    }

    public void setFreeType(String freeType) {
        this.freeType = freeType;
    }

    private int IsOnInvoice = 1;
    private int applyCount;

    public int getApplyCount() {
        return applyCount;
    }

    public void setApplyCount(int applyCount) {
        this.applyCount = applyCount;
    }

    public int getIsOnInvoice() {
        return IsOnInvoice;
    }

    public void setIsOnInvoice(int isOnInvoice) {
        IsOnInvoice = isOnInvoice;
    }

    public boolean isOffScheme() {
        return isOffScheme;
    }

    public void setOffScheme(boolean offScheme) {
        isOffScheme = offScheme;
    }

    private boolean isOffScheme;

    public String getGetType() {
        return getType;
    }

    public void setGetType(String getType) {
        this.getType = getType;
    }

    private String getType = "";

    public int getIsAutoApply() {
        return isAutoApply;
    }

    public void setIsAutoApply(int isAutoApply) {
        this.isAutoApply = isAutoApply;
    }


    // Display scheme fields
    public String getDisplayPeriodStart() {
        return displayPeriodStart;
    }

    public void setDisplayPeriodStart(String displayPeriodStart) {
        this.displayPeriodStart = displayPeriodStart;
    }

    public String getDisplayPeriodEnd() {
        return displayPeriodEnd;
    }

    public void setDisplayPeriodEnd(String displayPeriodEnd) {
        this.displayPeriodEnd = displayPeriodEnd;
    }

    public String getBookingPeriodStart() {
        return BookingPeriodStart;
    }

    public void setBookingPeriodStart(String bookingPeriodStart) {
        BookingPeriodStart = bookingPeriodStart;
    }

    public String getBookingPeriodEnd() {
        return BookingPeriodEnd;
    }

    public void setBookingPeriodEnd(String bookingPeriodEnd) {
        BookingPeriodEnd = bookingPeriodEnd;
    }

    public String getPayoutFrequency() {
        return payoutFrequency;
    }

    public void setPayoutFrequency(String payoutFrequency) {
        this.payoutFrequency = payoutFrequency;
    }

    public String getQualifier() {
        return qualifier;
    }

    public void setQualifier(String qualifier) {
        this.qualifier = qualifier;
    }

    private String displayPeriodStart, displayPeriodEnd, BookingPeriodStart, BookingPeriodEnd, payoutFrequency, qualifier;

    public String getDisplaySchemeValue() {
        return displaySchemeValue;
    }

    public void setDisplaySchemeValue(String displaySchemeValue) {
        this.displaySchemeValue = displaySchemeValue;
    }

    private String displaySchemeValue;

    public boolean isSchemeSelected() {
        return isSchemeSelected;
    }

    public void setSchemeSelected(boolean schemeSelected) {
        isSchemeSelected = schemeSelected;
    }

    private boolean isSchemeSelected;

    private boolean isChecked = false;

    public void setChecked(boolean isChecked) {
        this.isChecked = isChecked;
    }

    public boolean isChecked() {
        return isChecked;
    }

    private double totalPieceQty, totalCaseQty;

    public double getTotalPieceQty() {
        return totalPieceQty;
    }

    public void setTotalPieceQty(double totalPieceQty) {
        this.totalPieceQty = totalPieceQty;
    }

    public double getTotalCaseQty() {
        return totalCaseQty;
    }

    public void setTotalCaseQty(double totalCaseQty) {
        this.totalCaseQty = totalCaseQty;
    }

    private double totalPieceEveryQty, totalCaseEveryQty;

    public double getTotalPieceEveryQty() {
        return totalPieceEveryQty;
    }

    public void setTotalPieceEveryQty(double totalPieceEveryQty) {
        this.totalPieceEveryQty = totalPieceEveryQty;
    }

    public double getTotalCaseEveryQty() {
        return totalCaseEveryQty;
    }

    public void setTotalCaseEveryQty(double totalCaseEveryQty) {
        this.totalCaseEveryQty = totalCaseEveryQty;
    }

    private double totalPcsPriceQty, totalCasesPriceQty;

    public double getTotalPcsPriceQty() {
        return totalPcsPriceQty;
    }

    public void setTotalPcsPriceQty(double totalPcsPriceQty) {
        this.totalPcsPriceQty = totalPcsPriceQty;
    }

    public double getTotalCasesPriceQty() {
        return totalCasesPriceQty;
    }

    public void setTotalCasesPriceQty(double totalCasesPriceQty) {
        this.totalCasesPriceQty = totalCasesPriceQty;
    }

    private boolean isCurrentSlab;

    public boolean isCurrentSlab() {
        return isCurrentSlab;
    }

    public void setCurrentSlab(boolean currentSlab) {
        isCurrentSlab = currentSlab;
    }

    private boolean isNextSlab;

    public boolean isNextSlab() {
        return isNextSlab;
    }

    public void setNextSlab(boolean nextSlab) {
        isNextSlab = nextSlab;
    }

    private double fromQty, toQty;

    public double getFromQty() {
        return fromQty;
    }

    public void setFromQty(double fromQty) {
        this.fromQty = fromQty;
    }

    public double getToQty() {
        return toQty;
    }

    public void setToQty(double toQty) {
        this.toQty = toQty;
    }

    private boolean everyCaseUOM, isCaseScheme;

    public boolean isEveryCaseUOM() {
        return everyCaseUOM;
    }

    public void setEveryCaseUOM(boolean everyCaseUOM) {
        this.everyCaseUOM = everyCaseUOM;
    }

    public boolean isCaseScheme() {
        return isCaseScheme;
    }

    public void setCaseScheme(boolean caseScheme) {
        isCaseScheme = caseScheme;
    }

    private String fromDate, toDate;

    public String getFromDate() {
        return fromDate;
    }

    public void setFromDate(String fromDate) {
        this.fromDate = fromDate;
    }

    public String getToDate() {
        return toDate;
    }

    public void setToDate(String toDate) {
        this.toDate = toDate;
    }

    private int variantCount;

    public int getVariantCount() {
        return variantCount;
    }

    public void setVariantCount(int variantCount) {
        this.variantCount = variantCount;
    }

    public double getMaximumSlab() {
        return maximumSlab;
    }

    public void setMaximumSlab(double maximumSlab) {
        this.maximumSlab = maximumSlab;
    }
}
