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
}
