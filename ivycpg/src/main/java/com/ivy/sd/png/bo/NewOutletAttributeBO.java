package com.ivy.sd.png.bo;


public class NewOutletAttributeBO {
    private int attrId;
    private String attrName;
    private String attrParent;
    private int parentId;
    private int levelId;
    private int allowMultiple;
    private int criteriaMapped;
    private String status;

    public int getIsMandatory() {
        return isMandatory;
    }

    public void setIsMandatory(int isMandatory) {
        this.isMandatory = isMandatory;
    }

    private int isMandatory;

    public NewOutletAttributeBO(){

    }

    public NewOutletAttributeBO(int attrId, String attrName){
        this.attrId = attrId;
        this.attrName = attrName;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public int getAttrId() {
        return attrId;
    }

    public void setAttrId(int attrId) {
        this.attrId = attrId;
    }

    public String getAttrName() {
        return attrName;
    }

    public void setAttrName(String attrName) {
        this.attrName = attrName;
    }

    public int getParentId() {
        return parentId;
    }

    public void setParentId(int parentId) {
        this.parentId = parentId;
    }

    public int getLevelId() {
        return levelId;
    }

    public void setLevelId(int levelId) {
        this.levelId = levelId;
    }

    public int isAllowMultiple() {
        return allowMultiple;
    }

    public void setAllowMultiple(int allowMultiple) {
        this.allowMultiple = allowMultiple;
    }

    public int isCriteriaMapped() {
        return criteriaMapped;
    }

    public void setCriteriaMapped(int criteriaMapped) {
        this.criteriaMapped = criteriaMapped;
    }

    public String getAttrParent() {
        return attrParent;
    }

    public void setAttrParent(String attrParent) {
        this.attrParent = attrParent;
    }

    public String toString() {
        return attrName;
    }
}
