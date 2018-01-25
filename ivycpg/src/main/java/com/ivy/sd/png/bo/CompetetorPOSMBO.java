package com.ivy.sd.png.bo;

public class CompetetorPOSMBO {

	private int id;
	private String name;
	private boolean isExecuted;
	private int tcompetitorid;
	private String fromDate = "", toDate = "";
	private String feedBack = "";
	private String imageName = "";
	private String imagePath="";
	private int qty;
	private int reasonID;

	public CompetetorPOSMBO() {

	}

	public CompetetorPOSMBO(CompetetorPOSMBO competitorPosm) {
		this.id = competitorPosm.id;
		this.name = competitorPosm.name;
		this.tcompetitorid = competitorPosm.tcompetitorid;
		this.isExecuted = competitorPosm.isExecuted;
		this.feedBack = competitorPosm.feedBack;

	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public boolean isExecuted() {
		return isExecuted;
	}

	public void setExecuted(boolean isExecuted) {
		this.isExecuted = isExecuted;
	}

	@Override
	public String toString() {
		return name.toString();
	}

	public int getTcompetitorid() {
		return tcompetitorid;
	}

	public void setTcompetitorid(int tcompetitorid) {
		this.tcompetitorid = tcompetitorid;
	}

	public String getFromDate() {
		return fromDate;
	}

	public void setFromDate(String fromDate) {
		this.fromDate = fromDate;
	}

	public String getToDate() {
		return toDate;
	}

	public void setToDate(String toDate) {
		this.toDate = toDate;
	}

	public String getFeedBack() {
		return feedBack;
	}

	public void setFeedBack(String feedBack) {
		this.feedBack = feedBack;
	}

	public String getImageName() {
		return imageName;
	}

	public void setImageName(String imageName) {
		this.imageName = imageName;
	}

	public String getImagePath() {
		return imagePath;
	}

	public void setImagePath(String imagePath) {
		this.imagePath = imagePath;
	}

	public int getQty() {
		return qty;
	}

	public void setQty(int qty) {
		this.qty = qty;
	}

	public int getReasonID() {
		return reasonID;
	}

	public void setReasonID(int reasonID) {
		this.reasonID = reasonID;
	}
}
