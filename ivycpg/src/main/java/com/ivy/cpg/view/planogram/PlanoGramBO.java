package com.ivy.cpg.view.planogram;

import java.util.ArrayList;

public class PlanoGramBO {

    private String imageName;
    private String adherence;
    private String planoGramCameraImgName = "";
    private int pid;
    private int planogramID;
    private int locationID;
	private int audit=2;
	private String reasonID ="0";

	public String getPercentageId() {
		return percentageId;
	}

	public void setPercentageId(String percentageId) {
		this.percentageId = percentageId;
	}

	private String percentageId ="0";
	private String productName;
	private int imageId;

	public boolean isLoadedFromLastVisitTran() {
		return isLoadedFromLastVisitTran;
	}

	public void setLoadedFromLastVisitTran(boolean loadedFromLastVisitTran) {
		isLoadedFromLastVisitTran = loadedFromLastVisitTran;
	}

	private boolean isLoadedFromLastVisitTran;

	private ArrayList<String> planoGramCameraImgList = new ArrayList<>();
	
	public String getReasonID() {
		return reasonID;
	}

	public int getAudit() {
		return audit;
	}

	public void setAudit(int audit) {
		this.audit = audit;
	}

	public void setReasonID(String reasonID) {
		this.reasonID = reasonID;
	}

	public int getMappingID() {
		return planogramID;
	}

	public void setMappingID(int planogramID) {
		this.planogramID = planogramID;
	}

	public int getLocationID() {
		return locationID;
	}

	public void setLocationID(int locationID) {
		this.locationID = locationID;
	}

	public String getAdherence() {
		return adherence;
	}

	public void setAdherence(String adherence) {
		this.adherence = adherence;
	}

	public String getPlanogramCameraImgName() {
        return planoGramCameraImgName;
    }

	public void setPlanogramCameraImgName(String planogramCameraImgName) {
        this.planoGramCameraImgName = planogramCameraImgName;
    }

	public int getPid() {
		return pid;
	}

	public void setPid(int pid) {
		this.pid = pid;
	}

	public String getImageName() {
		return imageName;
	}

	public void setImageName(String imageName) {
		this.imageName = imageName;
	}

	public String getProductName() {
		return productName;
	}

	public void setProductName(String productName) {
		this.productName = productName;
	}

	public ArrayList<String> getPlanoGramCameraImgList() {
		return planoGramCameraImgList;
	}

	public void setPlanoGramCameraImgList(ArrayList<String> planoGramCameraImgList) {
		this.planoGramCameraImgList = planoGramCameraImgList;
	}

	public int getImageId() {
		return imageId;
	}

	public void setImageId(int imageId) {
		this.imageId = imageId;
	}

	private String parentHierarchy;

	public String getParentHierarchy() {
		return parentHierarchy;
	}

	public void setParentHierarchy(String parentHierarchy) {
		this.parentHierarchy = parentHierarchy;
	}
}
