package com.ivy.sd.png.bo;

public class StoreWiseDiscountBO {

	private int retailerid;
	private double appliedDiscount;

    private double minAmount,maxAmount;
	private double discountValue,discount;

	private int computeAfterTax;
	private int applyAfterTax;

	public boolean isApplied() {
		return isApplied;
	}

	public void setApplied(boolean applied) {
		isApplied = applied;
	}

	private boolean isApplied;

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

	public double getAppliedDiscount() {
		return appliedDiscount;
	}

	public void setAppliedDiscount(double appliedDiscount) {
		this.appliedDiscount = appliedDiscount;
	}

	public double getToDiscount() {
		return toDiscount;
	}

	public void setToDiscount(double toDiscount) {
		this.toDiscount = toDiscount;
	}

	private double toDiscount;
	private int ischecked,ProductId,isCompanyGiven;
	private int discountId;
	public int getProductId() {
		return ProductId;
	}

	public void setProductId(int productId) {
		ProductId = productId;
	}

	private int ApplyLevel, Module,IsPercentage;
	public int getIsPercentage() {
		return IsPercentage;
	}

	public void setIsPercentage(int isPercentage) {
		IsPercentage = isPercentage;
	}

	public int getApplyLevel() {
		return ApplyLevel;
	}

	public void setApplyLevel(int applyLevel) {
		ApplyLevel = applyLevel;
	}

	public int getModule() {
		return Module;
	}

	public void setModule(int module) {
		Module = module;
	}

	public String getDescription() {
		return Description;
	}

	public void setDescription(String description) {
		Description = description;
	}

	private String Description;

	public int getIschecked() {
		return ischecked;
	}

	public void setIschecked(int ischecked) {
		this.ischecked = ischecked;
	}

	public int getRetailerid() {
		return retailerid;
	}

	public void setRetailerid(int retailerid) {
		this.retailerid = retailerid;
	}

	public double getDiscount() {
		return discount;
	}

	public void setDiscount(double discount) {
		this.discount = discount;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}

	public int getDiscountId() {
		return discountId;
	}

	public void setDiscountId(int discountId) {
		this.discountId = discountId;
	}

	private int type;

	public int getIsCompanyGiven() {
		return isCompanyGiven;
	}

	public void setIsCompanyGiven(int isCompanyGiven) {
		this.isCompanyGiven = isCompanyGiven;
	}

	public double getDiscountValue() {
		return discountValue;
	}

	public void setDiscountValue(double discountValue) {
		this.discountValue = discountValue;
	}
	private int payTerm;

	public int getPayTerm() {
		return payTerm;
	}

	public void setPayTerm(int payTerm) {
		this.payTerm = payTerm;
	}

	public int getComputeAfterTax() {
		return computeAfterTax;
	}

	public void setComputeAfterTax(int computeAfterTax) {
		this.computeAfterTax = computeAfterTax;
	}

	public int getApplyAfterTax() {
		return applyAfterTax;
	}

	public void setApplyAfterTax(int applyAfterTax) {
		this.applyAfterTax = applyAfterTax;
	}
}
