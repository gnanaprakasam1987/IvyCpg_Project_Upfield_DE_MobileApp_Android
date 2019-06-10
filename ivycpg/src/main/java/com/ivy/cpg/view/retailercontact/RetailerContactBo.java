package com.ivy.cpg.view.retailercontact;

import java.util.ArrayList;

/**
 * Created by mansoor.k on 30-07-2018.
 */
public class RetailerContactBo {

    private String title = "";
    private String fistname = "";
    private String lastname ="";
    private String contactNumber = "";
    private String contactMail = "";
    private int isPrimary;
    private String contactTitleLovId = "";
    private String cpId= "";
    private int isEmailPrimary;
    private String salutationTitle="";
    private String contactSalutationId="";
    private String retailerID = "";

    private ArrayList<RetailerContactAvailBo> contactAvailList = new ArrayList<>();

    public ArrayList<RetailerContactAvailBo> getContactAvailList() {
        return contactAvailList;
    }

    public void setContactAvailList(ArrayList<RetailerContactAvailBo> contactAvailList) {
        this.contactAvailList = contactAvailList;
    }

    public String getContactSalutationId() {
        return contactSalutationId;
    }

    public void setContactSalutationId(String contactSalutationId) {
        this.contactSalutationId = contactSalutationId;
    }

    public String getSalutationTitle() {
        return salutationTitle;
    }

    public void setSalutationTitle(String salutationTitle) {
        this.salutationTitle = salutationTitle;
    }

    public int getIsEmailPrimary() {
        return isEmailPrimary;
    }

    public void setIsEmailPrimary(int isEmailPrimary) {
        this.isEmailPrimary = isEmailPrimary;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    private String status= "";

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getFistname() {
        return fistname;
    }

    public void setFistname(String fistname) {
        this.fistname = fistname;
    }

    public String getLastname() {
        return lastname;
    }

    public void setLastname(String lastname) {
        this.lastname = lastname;
    }

    public String getContactNumber() {
        return contactNumber;
    }

    public void setContactNumber(String contactNumber) {
        this.contactNumber = contactNumber;
    }

    public String getContactMail() {
        return contactMail;
    }

    public void setContactMail(String contactMail) {
        this.contactMail = contactMail;
    }

    public int getIsPrimary() {
        return isPrimary;
    }

    public void setIsPrimary(int isPrimary) {
        this.isPrimary = isPrimary;
    }

    public String getContactTitleLovId() {
        return contactTitleLovId;
    }

    public void setContactTitleLovId(String contactTitleLovId) {
        this.contactTitleLovId = contactTitleLovId;
    }

    public String getCpId() {
        return cpId;
    }

    public void setCpId(String cpId) {
        this.cpId = cpId;
    }

    public String getRetailerID() {
        return retailerID;
    }

    public void setRetailerID(String retailerID) {
        this.retailerID = retailerID;
    }
}
