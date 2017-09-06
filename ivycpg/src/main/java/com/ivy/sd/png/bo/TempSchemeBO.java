package com.ivy.sd.png.bo;

/**
 * @author vinodh.r
 * 
 */
public class TempSchemeBO {

	private double schemePrice;
	private double schemeAmount;
	private double schemePercentage;
	private String schemeID;
	private String ProductID;
	private boolean isSchemeApplied;

	public double getSchemePrice() {
		return schemePrice;
	}

	public void setSchemePrice(double schemePrice) {
		this.schemePrice = schemePrice;
	}

	public double getSchemeAmount() {
		return schemeAmount;
	}

	public void setSchemeAmount(double schemeAmount) {
		this.schemeAmount = schemeAmount;
	}

	public double getSchemePercentage() {
		return schemePercentage;
	}

	public void setSchemePercentage(double schemePercentage) {
		this.schemePercentage = schemePercentage;
	}

	public String getSchemeID() {
		return schemeID;
	}

	public void setSchemeID(String schemeID) {
		this.schemeID = schemeID;
	}

	public String getProductID() {
		return ProductID;
	}

	public void setProductID(String productID) {
		ProductID = productID;
	}

	public boolean isSchemeApplied() {
		return isSchemeApplied;
	}

	public void setSchemeApplied(boolean isSchemeApplied) {
		this.isSchemeApplied = isSchemeApplied;
	}

}
