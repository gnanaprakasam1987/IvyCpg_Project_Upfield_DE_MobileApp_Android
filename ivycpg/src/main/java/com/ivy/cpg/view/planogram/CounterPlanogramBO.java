package com.ivy.cpg.view.planogram;

public class CounterPlanogramBO {
	
	private String RetailerId,PlanogramDesc,ImageName,Adherence,PlanogramCameraImgName="",PlanogramSuperCameraPath,Type="";
	private int parentId,PlanogramID,audit=2,counterId,imageId;
	private String ReasonID ="0";
	
	public String getReasonID() {
		return ReasonID;
	}

	public int getAudit() {
		return audit;
	}

	public void setAudit(int audit) {
		this.audit = audit;
	}

	public void setReasonID(String reasonID) {
		ReasonID = reasonID;
	}

	public int getMappingID() {
		return PlanogramID;
	}

	public void setMappingID(int planogramID) {
		PlanogramID = planogramID;
	}


	public int getParentId() {
		return parentId;
	}

	public void setParentId(int parentId) {
		this.parentId = parentId;
	}



	public String getAdherence() {
		return Adherence;
	}

	public void setAdherence(String adherence) {
		Adherence = adherence;
	}

	public String getPlanogramSuperCameraPath() {
		return PlanogramSuperCameraPath;
	}

	public void setPlanogramSuperCameraPath(String planogramSuperCameraPath) {
		PlanogramSuperCameraPath = planogramSuperCameraPath;
	}


	public String getPlanogramCameraImgName() {
		return PlanogramCameraImgName;
	}

	public void setPlanogramCameraImgName(String planogramCameraImgName) {
		PlanogramCameraImgName = planogramCameraImgName;
	}



	public String getRetailerId() {
		return RetailerId;
	}

	public void setRetailerId(String retailerId) {
		RetailerId = retailerId;
	}

	public String getPlanogramDesc() {
		return PlanogramDesc;
	}

	public void setPlanogramDesc(String planogramDesc) {
		PlanogramDesc = planogramDesc;
	}

	public String getImageName() {
		return ImageName;
	}

	public void setImageName(String imageName) {
		ImageName = imageName;
	}

	public String getType() {
		return Type;
	}

	public void setType(String type) {
		Type = type;
	}

	public int getCounterId() {
		return counterId;
	}

	public void setCounterId(int counterId) {
		this.counterId = counterId;
	}

	public int getImageId() {
		return imageId;
	}

	public void setImageId(int imageId) {
		this.imageId = imageId;
	}

	public int getPlanogramID() {
		return PlanogramID;
	}

	public void setPlanogramID(int planogramID) {
		PlanogramID = planogramID;
	}

	@Override
	public String toString() {
		return ImageName;
	}
}
