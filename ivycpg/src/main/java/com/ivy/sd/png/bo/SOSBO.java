package com.ivy.sd.png.bo;

import java.util.ArrayList;

public class SOSBO{
	private int parentID;
	private int productID;
	private String productName="";
	private int isOwn;
	private float norm;
	private int mappingId;
	private ArrayList<LocationBO> locations;

	public int getParentID() {
		return parentID;
	}

	public void setParentID(int parentID) {
		this.parentID = parentID;
	}

	public int getProductID() {
		return productID;
	}

	public void setProductID(int productID) {
		this.productID = productID;
	}

	public String getProductName() {
		return productName;
	}

	public void setProductName(String productName) {
		this.productName = productName;
	}

	public int getIsOwn() {
		return isOwn;
	}

	public void setIsOwn(int isOwn) {
		this.isOwn = isOwn;
	}

	public float getNorm() {
		return norm;
	}

	public void setNorm(float norm) {
		this.norm = norm;
	}

	public int getMappingId() {
		return mappingId;
	}

	public void setMappingId(int mappingId) {
		this.mappingId = mappingId;
	}

	public ArrayList<LocationBO> getLocations() {
		return locations;
	}

	public void setLocations(ArrayList<LocationBO> locations) {
		this.locations = locations;
	}


	// Project specific
	int groupId;

	public int getGroupId() {
		return groupId;
	}

	public void setGroupId(int groupId) {
		this.groupId = groupId;
	}

	public int getProductLevelId() {
		return productLevelId;
	}

	public void setProductLevelId(int productLevelId) {
		this.productLevelId = productLevelId;
	}

	public String getGroupName() {
		return groupName;
	}

	public void setGroupName(String groupName) {
		this.groupName = groupName;
	}

	int productLevelId;
	String groupName;

	public int getAvailability() {
		return availability;
	}

	public void setAvailability(int availability) {
		this.availability = availability;
	}

	int availability;

	public int getInTarget() {
		return inTarget;
	}

	public void setInTarget(int inTarget) {
		this.inTarget = inTarget;
	}

	int inTarget;

	public int getGroupTarget() {
		return groupTarget;
	}

	public void setGroupTarget(int groupTarget) {
		this.groupTarget = groupTarget;
	}

	int groupTarget;
}
