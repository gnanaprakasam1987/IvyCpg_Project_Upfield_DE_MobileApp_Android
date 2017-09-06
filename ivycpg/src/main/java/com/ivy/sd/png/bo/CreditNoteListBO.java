package com.ivy.sd.png.bo;

public class CreditNoteListBO {
	private String id;
	private String refno;
	private String retailerId;
	private String retailerName;
	private double amount;
	private boolean isChecked;
	private boolean isUsed;
	private int typeId;

	public int getRefid() {
		return refid;
	}

	public void setRefid(int refid) {
		this.refid = refid;
	}

	private int refid;

	public double getAppliedAmount() {
		return appliedAmount;
	}

	public void setAppliedAmount(double appliedAmount) {
		this.appliedAmount = appliedAmount;
	}

	private double appliedAmount;

	public int getTypeId() {
		return typeId;
	}

	public void setTypeId(int typeId) {
		this.typeId = typeId;
	}

	public boolean isChecked() {
		return isChecked;
	}

	public void setChecked(boolean isChecked) {
		this.isChecked = isChecked;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getRefno() {
		return refno;
	}

	public void setRefno(String refno) {
		this.refno = refno;
	}

	public double getAmount() {
		return amount;
	}

	public void setAmount(double amount) {
		this.amount = amount;
	}

	public boolean isUsed() {
		return isUsed;
	}

	public void setUsed(boolean isUsed) {
		this.isUsed = isUsed;
	}

	public String getRetailerId() {
		return retailerId;
	}

	public void setRetailerId(String retailerId) {
		this.retailerId = retailerId;
	}

	public String getRetailerName() {
		return retailerName;
	}

	public void setRetailerName(String retailerName) {
		this.retailerName = retailerName;
	}
}
