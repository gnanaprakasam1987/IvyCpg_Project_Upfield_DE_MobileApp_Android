package com.ivy.sd.png.bo;

import java.util.HashMap;

/**
 * Created by rajkumar.s on 16-02-2016.
 */
public class AttributeBO {

    public AttributeBO(int att_id,String att_name){
        this.attributeId=att_id;
        this.attributeName=att_name;
    }

    public AttributeBO(){

    }
    int attributeId;

    public int getAttributeLovId() {
        return attributeLovId;
    }

    public void setAttributeLovId(int attributeLovId) {
        this.attributeLovId = attributeLovId;
    }

    public int getAttributeId() {
        return attributeId;
    }

    public void setAttributeId(int attributeId) {
        this.attributeId = attributeId;
    }

    int attributeLovId;

    public int getAttributeTypeId() {
        return attributeTypeId;
    }

    public void setAttributeTypeId(int attributeTypeId) {
        this.attributeTypeId = attributeTypeId;
    }

    int attributeTypeId;

    public String getAttributeName() {
        return attributeName;
    }

    public void setAttributeName(String attributeName) {
        this.attributeName = attributeName;
    }

    String attributeName;

    public String getAttributeTypename() {
        return attributeTypename;
    }

    public void setAttributeTypename(String attributeTypename) {
        this.attributeTypename = attributeTypename;
    }

    String attributeTypename;


    public int getProductId() {
        return productId;
    }

    public void setProductId(int productId) {
        this.productId = productId;
    }

    //
    int productId;

    public int getLeastParentId() {
        return leastParentId;
    }

    public void setLeastParentId(int leastParentId) {
        this.leastParentId = leastParentId;
    }

    int leastParentId;

    @Override
    public String toString() {
        return attributeName;
    }

    private int levelCount;

    public int getLevelCount() {
        return levelCount;
    }

    public void setLevelCount(int levelCount) {
        this.levelCount = levelCount;
    }

    private HashMap<String,AttributeBO> attributeBOHashMap =new HashMap<>();

    private AttributeBO childAttributeBO;

    public HashMap<String, AttributeBO> getAttributeBOHashMap() {
        return attributeBOHashMap;
    }

    public void setAttributeBOHashMap(HashMap<String, AttributeBO> attributeBOHashMap) {
        this.attributeBOHashMap = attributeBOHashMap;
    }


    public AttributeBO getChildAttributeBO() {
        return childAttributeBO;
    }

    public void setChildAttributeBO(AttributeBO childAttributeBO) {
        this.childAttributeBO = childAttributeBO;
    }

    private boolean isAttributeSelected;

    public boolean isAttributeSelected() {
        return isAttributeSelected;
    }

    public void setAttributeSelected(boolean attributeSelected) {
        isAttributeSelected = attributeSelected;
    }

    private boolean isRetailerAttributeId;
    private boolean isRetailerEditAttributeId;
    private int channelId;
    private boolean isMandatory;
    private int levelId;
    private String status;
    private String parentId;
    private int attributeParentId;

    public int getAttributeParentId() {
        return attributeParentId;
    }

    public void setAttributeParentId(int attributeParentId) {
        this.attributeParentId = attributeParentId;
    }

    public String getParentId() {
        return parentId;
    }

    public void setParentId(String parentId) {
        this.parentId = parentId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public int getLevelId() {
        return levelId;
    }

    public void setLevelId(int levelId) {
        this.levelId = levelId;
    }

    public boolean isRetailerAttributeId() {
        return isRetailerAttributeId;
    }

    public void setRetailerAttributeId(boolean retailerAttributeId) {
        isRetailerAttributeId = retailerAttributeId;
    }

    public boolean isRetailerEditAttributeId() {
        return isRetailerEditAttributeId;
    }

    public void setRetailerEditAttributeId(boolean retailerEditAttributeId) {
        isRetailerEditAttributeId = retailerEditAttributeId;
    }

    public int getChannelId() {
        return channelId;
    }

    public void setChannelId(int channelId) {
        this.channelId = channelId;
    }

    public boolean isMandatory() {
        return isMandatory;
    }

    public void setMandatory(boolean mandatory) {
        isMandatory = mandatory;
    }
}
