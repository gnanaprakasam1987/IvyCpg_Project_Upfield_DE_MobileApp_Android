package com.ivy.sd.png.view.profile;

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
}
