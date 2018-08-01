package com.ivy.sd.png.bo;

public class NonproductivereasonBO {

	private String retailerid, retailerName, date, reasonid;
	private String reasontype, collectionReasonID, collectionReasonType, moduleCode, imagePath="", imageName="";
	private int beatId,distributorID;

	public NonproductivereasonBO() {
		// TODO Auto-generated constructor stub
	}

	public NonproductivereasonBO(String reasonid) {
		// TODO Auto-generated method stub
		this.retailerid = "";
		this.reasonid = reasonid;
		this.reasontype = "";
		this.date = "";
		this.collectionReasonID = "";
		this.collectionReasonType = "";
		this.moduleCode = "";
		this.imagePath = "";
		this.imageName = "";
	}

	public String getReasontype() {
		return reasontype;
	}

	public void setReasontype(String reasontype) {
		this.reasontype = reasontype;
	}

	public String getCollectionReasonID() {
		return collectionReasonID;
	}

	public void setCollectionReasonID(String collectionReasonID) {
		this.collectionReasonID = collectionReasonID;
	}

	public String getRetailerid() {
		return retailerid;
	}

	public void setRetailerid(String retailerid) {
		this.retailerid = retailerid;
	}

	public String getDate() {
		return date;
	}

	public void setDate(String date) {
		this.date = date;
	}

	public String getReasonid() {
		return reasonid;
	}

	public void setReasonid(String reasonid) {
		this.reasonid = reasonid;
	}

	public String getRetailerName() {
		return retailerName;
	}

	public void setRetailerName(String retailerName) {
		this.retailerName = retailerName;
	}

	public int getBeatId() {
		return beatId;
	}

	public void setBeatId(int beatId) {
		this.beatId = beatId;
	}

	public String getCollectionReasonType() {
		return collectionReasonType;
	}

	public void setCollectionReasonType(String collectionReasonType) {
		this.collectionReasonType = collectionReasonType;
	}

	public int getDistributorID() {
		return distributorID;
	}

	public void setDistributorID(int distributorID) {
		this.distributorID = distributorID;
	}
	public String getModuleCode() {
		return moduleCode;
	}

	public void setModuleCode(String moduleCode) {
		this.moduleCode = moduleCode;
	}

	public String getImagePath() {
		return imagePath;
	}

	public void setImagePath(String imagePath) {
		this.imagePath = imagePath;
	}

	public String getImageName() {
		return imageName;
	}

	public void setImageName(String imageName) {
		this.imageName = imageName;
	}
}
