package com.ivy.sd.png.bo;

public class TaxTempBO {
	private String taxType;
	private String taxDesc;
	private double taxRate;
	private double totalProdTaxAmount, totalProdAmount;

	public String getTaxDesc() {
		return taxDesc;
	}

	public void setTaxDesc(String taxDesc) {
		this.taxDesc = taxDesc;
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

	public double getTotalProdTaxAmount() {
		return totalProdTaxAmount;
	}

	public void setTotalProdTaxAmount(double totalProdTaxAmount) {
		this.totalProdTaxAmount = totalProdTaxAmount;
	}

	public double getTotalProdAmount() {
		return totalProdAmount;
	}

	public void setTotalProdAmount(double totalProdAmount) {
		this.totalProdAmount = totalProdAmount;
	}

}
