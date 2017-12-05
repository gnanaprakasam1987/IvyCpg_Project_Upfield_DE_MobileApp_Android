package com.ivy.cpg.view.nearexpiry;



public class NearExpiryDateBO {

	private String nearexpPC = "0";
	private String nearexpCA = "0";
	private String nearexpOU = "0";
	private String date = "0";
	private int dateID=0;
	
	
	
	public NearExpiryDateBO(){
		
	}
	public NearExpiryDateBO(NearExpiryDateBO dateObj){
		this.date=dateObj.date;
		this.dateID=dateObj.dateID;
		this.nearexpPC=dateObj.nearexpPC;
		this.nearexpCA=dateObj.nearexpCA;
		this.nearexpOU=dateObj.nearexpOU;
		
	}

	// Near Expiry

	public String getNearexpPC() {
		return nearexpPC;
	}

	public void setNearexpPC(String nearexpPC) {
		this.nearexpPC = nearexpPC;
	}

	public String getNearexpCA() {
		return nearexpCA;
	}

	public void setNearexpCA(String nearexpCA) {
		this.nearexpCA = nearexpCA;
	}

	public String getNearexpOU() {
		return nearexpOU;
	}

	public void setNearexpOU(String nearexpOU) {
		this.nearexpOU = nearexpOU;
	}

	public String getDate() {
		return date;
	}

	public void setDate(String date) {
		this.date = date;
	}

	public void setDateID(int dateID) {
		this.dateID = dateID;
	}

}
