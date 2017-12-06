package com.ivy.cpg.view.planogram;

public class PlanoGramBO {

    private String imageName;
	private String adherence;
    private String planoGramCameraImgName = "";
    private int pid;
	private int planogramID;
	private int locationID;
	private int audit=2;
	private String reasonID ="0";
	private String productName;
	
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

}
