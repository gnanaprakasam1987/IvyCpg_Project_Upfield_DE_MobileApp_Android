package com.ivy.sd.png.bo;

import java.io.Serializable;
import java.util.ArrayList;

public class CompetitorBO implements Serializable{

	private int companyID, productid, plevelid, competitorpid;
	private String productcode;
	private String productname;
	private String feedBack = "";
	private String imageName = "";
	private boolean isAchieved;

	public String getImagePath() {
		return imagePath;
	}

	public void setImagePath(String imagePath) {
		this.imagePath = imagePath;
	}

	private String imagePath="";
	private ArrayList<CompetetorPOSMBO> competitoreason = new ArrayList<CompetetorPOSMBO>();

	public int getCompanyID() {
		return companyID;
	}

	public void setCompanyID(int companyID) {
		this.companyID = companyID;
	}

	public int getCompetitorpid() {
		return competitorpid;
	}

	public void setCompetitorpid(int competitorpid) {
		this.competitorpid = competitorpid;
	}

	public String getImageName() {
		return imageName;
	}

	public void setImageName(String imageName) {
		this.imageName = imageName;
	}

	public String getFeedBack() {
		return feedBack;
	}

	public void setFeedBack(String feedBack) {
		this.feedBack = feedBack;
	}

	public int getProductid() {
		return productid;
	}

	public void setProductid(int productid) {
		this.productid = productid;
	}

	public int getPlevelid() {
		return plevelid;
	}

	public void setPlevelid(int plevelid) {
		this.plevelid = plevelid;
	}

	public String getProductcode() {
		return productcode;
	}

	public void setProductcode(String productcode) {
		this.productcode = productcode;
	}

	public String getProductname() {
		return productname;
	}

	public void setProductname(String productname) {
		this.productname = productname;
	}

	public String toString() {
		return this.productname;

	}

	public ArrayList<CompetetorPOSMBO> getCompetitoreason() {
		return competitoreason;
	}

	public void setCompetitoreason(ArrayList<CompetetorPOSMBO> competitoreason) {
		this.competitoreason = competitoreason;
	}

	public boolean isAchieved() {
		return isAchieved;
	}

	public void setAchieved(boolean achieved) {
		isAchieved = achieved;
	}
}
