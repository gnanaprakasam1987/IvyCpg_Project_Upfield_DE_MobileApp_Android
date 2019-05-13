package com.ivy.sd.png.bo;

public class CompanyBO {

	private int competitorid;

	public int getIsOwn() {
		return isOwn;
	}

	public void setIsOwn(int isOwn) {
		this.isOwn = isOwn;
	}

	private int isOwn;
	private String competitorName = "";

	public int getQuantity() {
		return quantity;
	}

	public void setQuantity(int quantity) {
		this.quantity = quantity;
	}

	private int quantity;

	public int getCompetitorid() {
		return competitorid;
	}

	public void setCompetitorid(int competitorid) {
		this.competitorid = competitorid;
	}

	public String getCompetitorName() {
		return competitorName;
	}

	public void setCompetitorName(String competitorName) {
		this.competitorName = competitorName;
	}

	public CompanyBO(){

	}
	public CompanyBO(CompanyBO companyBO){

		this.competitorid=companyBO.getCompetitorid();
		this.competitorName=companyBO.getCompetitorName();

	}

	public String toString() {
		return this.competitorName;

	}

}
