package com.ivy.sd.png.bo;

public class TaxBO {
	private String taxType;
	private double taxRate;
	private String sequence;
	private String taxDesc;
	private String parentType;
	private double totalTaxAmount;

	private double taxableAmount;
	private int pid,taxTypeId;
	private double minValue;
	private double maxValue;
	private int applyRange;
	private int groupId;

	public TaxBO() {
	}

	public TaxBO(String taxType, double taxRate, String sequence, String taxDesc, String parentType, double totalTaxAmount, int pid, int taxTypeId, double minValue, double maxValue, int applyRange, int groupId, String taxDesc2) {
		this.taxType = taxType;
		this.taxRate = taxRate;
		this.sequence = sequence;
		this.taxDesc = taxDesc;
		this.parentType = parentType;
		this.totalTaxAmount = totalTaxAmount;
		this.pid = pid;
		this.taxTypeId = taxTypeId;
		this.minValue = minValue;
		this.maxValue = maxValue;
		this.applyRange = applyRange;
		this.groupId = groupId;
		this.taxDesc2 = taxDesc2;
	}

	private String taxDesc2;

	public String getTaxDesc2() {
		return taxDesc2;
	}

	public void setTaxDesc2(String taxDesc2) {
		this.taxDesc2 = taxDesc2;
	}

	public int getGroupId() {
		return groupId;
	}

	public void setGroupId(int groupId) {
		this.groupId = groupId;
	}

	public int getPid() {
		return pid;
	}

	public void setPid(int pid) {
		this.pid = pid;
	}

	public String getTaxType() {
		return taxType;
	}

	public void setTaxType(String taxType) {
		this.taxType = taxType;
	}

	public double getTaxRate() {
		return taxRate;
	}

	public void setTaxRate(double taxRate) {
		this.taxRate = taxRate;
	}

	public String getSequence() {
		return sequence;
	}

	public void setSequence(String sequence) {
		this.sequence = sequence;
	}

	public String getTaxDesc() {
		return taxDesc;
	}

	public void setTaxDesc(String taxDesc) {
		this.taxDesc = taxDesc;
	}

	public String getParentType() {
		return parentType;
	}

	public void setParentType(String parentType) {
		this.parentType = parentType;
	}

	public double getTotalTaxAmount() {
		return totalTaxAmount;
	}

	public void setTotalTaxAmount(double totalTaxAmount) {
		this.totalTaxAmount = totalTaxAmount;
	}


	public int getTaxTypeId() {
		return taxTypeId;
	}

	public void setTaxTypeId(int taxTypeId) {
		this.taxTypeId = taxTypeId;
	}

	public double getMaxValue() {
		return maxValue;
	}

	public void setMaxValue(double maxValue) {
		this.maxValue = maxValue;
	}

	public double getMinValue() {
		return minValue;
	}

	public void setMinValue(double minValue) {
		this.minValue = minValue;
	}

	public int getApplyRange() {
		return applyRange;
	}

	public void setApplyRange(int applyRange) {
		this.applyRange = applyRange;
	}


	public double getTaxableAmount() {
		return taxableAmount;
	}

	public void setTaxableAmount(double taxableAmount) {
		this.taxableAmount = taxableAmount;
	}


	@Override
	public String toString() {
		return taxDesc;
	}
}
