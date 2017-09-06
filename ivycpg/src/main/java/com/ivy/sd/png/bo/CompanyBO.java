package com.ivy.sd.png.bo;

public class CompanyBO {

	private int competitorid;
	private String competitorName = "";

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

	public String toString() {
		return this.competitorName;

	}

}
