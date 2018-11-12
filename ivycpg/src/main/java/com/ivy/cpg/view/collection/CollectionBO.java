package com.ivy.cpg.view.collection;

public class CollectionBO {

	private double cashamt, chequeamt, discountamt, creditamt,
			mobilePaymentamt;
	private int percent;
	private String bankId,branchId;

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

	public String getNo_of_coupon() {
		return no_of_coupon;
	}

	public void setNo_of_coupon(String no_of_coupon) {
		this.no_of_coupon = no_of_coupon;
	}

	public String no_of_coupon;

}
