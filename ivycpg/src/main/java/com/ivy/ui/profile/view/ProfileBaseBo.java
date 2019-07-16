package com.ivy.ui.profile.view;

import java.util.ArrayList;
import java.util.HashMap;

public class ProfileBaseBo {

    private String status;
    private String fieldName;
    private HashMap<String,Object> profileFields = new HashMap<>();
    private ArrayList<?> contactList = new ArrayList<>();
    private ArrayList<Object> attributeList = new ArrayList<>();

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

    public HashMap<String, Object> getProfileFields() {
        return profileFields;
    }

    public void setProfileFields(HashMap<String, Object> profileFields) {
        this.profileFields = profileFields;
    }

    public ArrayList<?> getContactList() {
        return contactList;
    }

    public void setContactList(ArrayList<?> contactList) {
        this.contactList = contactList;
    }

    public ArrayList<Object> getAttributeList() {
        return attributeList;
    }

    public void setAttributeList(ArrayList<Object> attributeList) {
        this.attributeList = attributeList;
    }
}
