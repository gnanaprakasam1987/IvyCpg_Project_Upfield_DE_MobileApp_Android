package com.ivy.ui.profile.view;

import com.ivy.cpg.view.retailercontact.RetailerContactBo;
import com.ivy.sd.png.bo.AttributeBO;

import java.util.ArrayList;
import java.util.HashMap;

public class ProfileBaseBo {

    private String status;
    private String fieldName;
    private HashMap<String,?> profileFields = new HashMap<>();
    private ArrayList<RetailerContactBo> contactList = new ArrayList<>();
    private ArrayList<AttributeBO> attributeList = new ArrayList<>();

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getFieldName() {
        return fieldName;
    }

    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
    }

    public HashMap<String, ?> getProfileFields() {
        return profileFields;
    }

    public void setProfileFields(HashMap<String, ?> profileFields) {
        this.profileFields = profileFields;
    }

    public ArrayList<RetailerContactBo> getContactList() {
        return contactList;
    }

    public void setContactList(ArrayList<RetailerContactBo> contactList) {
        this.contactList = contactList;
    }

    public ArrayList<AttributeBO> getAttributeList() {
        return attributeList;
    }

    public void setAttributeList(ArrayList<AttributeBO> attributeList) {
        this.attributeList = attributeList;
    }
}
