package com.ivy.sd.png.bo;

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
    private boolean isRetailerAttributeId;
    private boolean isRetailerEditAttributeId;
    private int channelId;
    private boolean isMandatory;

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

    public int getLevelCount() {
        return levelCount;
    }

    public void setLevelCount(int levelCount) {
        this.levelCount = levelCount;
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
