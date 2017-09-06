package com.ivy.sd.png.bo;

public class CollectionBO {

	private double cashamt, chequeamt, discountamt, creditamt,
			mobilePaymentamt;
	private int rangeFrom, rangeTo, percent;
	private String bankId,branchId;
  
	public int getRangeFrom() {
		return rangeFrom;
	}

	public void setRangeFrom(int rangeFrom) {
		this.rangeFrom = rangeFrom;
	}

	public int getRangeTo() {
		return rangeTo;
	}

	public void setRangeTo(int rangeTo) {
		this.rangeTo = rangeTo;
	}

	public int getPercent() {
		return percent;
	}

	public void setPercent(int percent) {
		this.percent = percent;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	private String description;

	public double getCashamt() {
		return cashamt;
	}

	public void setCashamt(double cashamt) {
		this.cashamt = cashamt;
	}

	public double getChequeamt() {
		return chequeamt;
	}

	public void setChequeamt(double chequeamt) {
		this.chequeamt = chequeamt;
	}

	public double getDiscountamt() {
		return discountamt;
	}

	public void setDiscountamt(double discountamt) {
		this.discountamt = discountamt;
	}

	public double getCreditamt() {
		return creditamt;
	}

	public void setCreditamt(double creditamt) {
		this.creditamt = creditamt;
	}

	public double getMobilePaymentamt() {
		return mobilePaymentamt;
	}

	public void setMobilePaymentamt(double mobilePaymentamt) {
		this.mobilePaymentamt = mobilePaymentamt;
	}

	public String getBankId() {
		return bankId;
	}

	public void setBankId(String bankId) {
		this.bankId = bankId;
	}

	public String getBranchId() { 
		return branchId;
	}

	public void setBranchId(String branchId) {
		this.branchId = branchId;
	}
	
	private double rtgsamt ;

	public void setRtgsamt(double convertToDouble) {
		rtgsamt = convertToDouble;
	}

	public double getRtgsamt() {
		return rtgsamt;
	}

	private double demandDraft ;

	public double getDemandDraftAmt() {
		return demandDraft;
	}

	public void setDemandDraftAmt(double demandDraft) {
		this.demandDraft = demandDraft;
	}

	public double getCouponamount() {
		return couponamount;
	}

	public void setCouponamount(double couponamount) {
		this.couponamount = couponamount;
	}

	public double couponamount;

	public String getReference_no() {
		return reference_no;
	}

	public void setReference_no(String reference_no) {
		this.reference_no = reference_no;
	}

	public String reference_no;

	public String getNo_of_coupon() {
		return no_of_coupon;
	}

	public void setNo_of_coupon(String no_of_coupon) {
		this.no_of_coupon = no_of_coupon;
	}

	public String no_of_coupon;

}
