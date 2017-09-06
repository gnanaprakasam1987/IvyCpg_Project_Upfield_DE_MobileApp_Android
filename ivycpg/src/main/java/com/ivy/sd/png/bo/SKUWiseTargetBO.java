package com.ivy.sd.png.bo;

public class SKUWiseTargetBO {


    private String productName;
    private String productShortName;
    private String retailerID;
    private String productCode;
    private String barcode;
    private double rField;
    private String type;

    public String getFreqType() {
        return freqType;
    }

    public void setFreqType(String freqType) {
        this.freqType = freqType;
    }

    private String freqType;
    private int pid, categoryId, brandId;
    private double target, achieved;
    private float convTargetPercentage;
    private float convAcheivedPercentage;
    private float calculatedPercentage;
    private int parentID,kpiID,levelID,sequence;


    public String getMonthName() {
        return monthName;
    }

    public void setMonthName(String monthName) {
        this.monthName = monthName;
    }

    private String monthName;

    public int getPid() {
        return pid;
    }


    public void setPid(int pid) {
        this.pid = pid;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getProductShortName() {
        return productShortName;
    }

    public void setProductShortName(String productShortName) {
        this.productShortName = productShortName;
    }

    public String getProductCode() {
        return productCode;
    }

    public void setProductCode(String productCode) {
        this.productCode = productCode;
    }

    public int getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(int categoryId) {
        this.categoryId = categoryId;
    }

    public int getBrandId() {
        return brandId;
    }

    public void setBrandId(int brandId) {
        this.brandId = brandId;
    }

    public String getBarcode() {
        return barcode;
    }

    public void setBarcode(String barcode) {
        this.barcode = barcode;
    }

    public String getRetailerID() {
        return retailerID;
    }

    public void setRetailerID(String retailerID) {
        this.retailerID = retailerID;
    }


    public void setTarget(int target) {
        this.target = target;
    }


    public void setAchieved(int achieved) {
        this.achieved = achieved;
    }

    public float getConvTargetPercentage() {
        return convTargetPercentage;
    }

    public void setConvTargetPercentage(float convTargetPercentage) {
        this.convTargetPercentage = convTargetPercentage;
    }

    public float getConvAcheivedPercentage() {
        return convAcheivedPercentage;
    }

    public void setConvAcheivedPercentage(float convAcheivedPercentage) {
        this.convAcheivedPercentage = convAcheivedPercentage;
    }

    public float getCalculatedPercentage() {
        return calculatedPercentage;
    }

    public void setCalculatedPercentage(float calculatedPercentage) {
        this.calculatedPercentage = calculatedPercentage;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public int getParentID() {
        return parentID;
    }

    public void setParentID(int parentID) {
        this.parentID = parentID;
    }

	public int getKpiID() {
		return kpiID;
	}

	public void setKpiID(int kpiID) {
		this.kpiID = kpiID;
	}


    public double getAchieved() {
        return achieved;
    }

    public void setAchieved(double achieved) {
        this.achieved = achieved;
    }

    public double getTarget() {
        return target;
    }

    public void setTarget(double target) {
        this.target = target;
    }


	public int getLevelID() {
		return levelID;
	}

	public void setLevelID(int levelID) {
		this.levelID = levelID;
	}

	public int getSequence() {
		return sequence;
	}

	public void setSequence(int sequence) {
		this.sequence = sequence;
	}
    public double getrField() {
        return rField;
    }

    public void setrField(double rField) {
        this.rField = rField;
    }
}
