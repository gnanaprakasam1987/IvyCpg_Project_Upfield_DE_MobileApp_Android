package com.ivy.sd.png.bo;

import com.ivy.cpg.view.sf.SFLocationBO;

import java.util.ArrayList;

public class SOSBO {
    private int parentID;
    private int productID;
    private String productName = "";
    private int isOwn;
    private float norm;
    private int mappingId;
    private ArrayList<SFLocationBO> locations;
    // Project specific
    private int groupId;
    private int productLevelId;
    private String groupName;
    private int availability;
    private int inTarget;
    private double groupTarget;
    private String parentHierarchy;

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

    public ArrayList<SFLocationBO> getLocations() {
        return locations;
    }

    public void setLocations(ArrayList<SFLocationBO> locations) {
        this.locations = locations;
    }

    public int getGroupId() {
        return groupId;
    }

    public void setGroupId(int groupId) {
        this.groupId = groupId;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public int getProductLevelId() {
        return productLevelId;
    }

    public void setProductLevelId(int productLevelId) {
        this.productLevelId = productLevelId;
    }

    public int getAvailability() {
        return availability;
    }

    public void setAvailability(int availability) {
        this.availability = availability;
    }

    public int getInTarget() {
        return inTarget;
    }

    public void setInTarget(int inTarget) {
        this.inTarget = inTarget;
    }

    public double getGroupTarget() {
        return groupTarget;
    }

    public void setGroupTarget(double groupTarget) {
        this.groupTarget = groupTarget;
    }

    public String getParentHierarchy() {
        return parentHierarchy;
    }

    public void setParentHierarchy(String parentHierarchy) {
        this.parentHierarchy = parentHierarchy;
    }
}
