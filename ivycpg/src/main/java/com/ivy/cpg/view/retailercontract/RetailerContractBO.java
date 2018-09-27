package com.ivy.cpg.view.retailercontract;

/**
 * Created by chiranjeevulu.l on 18-04-2016.
 */
public class RetailerContractBO {
    private String Startdate;
    private String Enddate;
    private String Contractname;
    private String Contracttype;
    private String Status;
    private String Retailerid;
    private String typelovid;
    private String Tid;
    private String Contractid = "0";
    private String TemplateId;
    private String cs_id;
    private boolean renewed = false;

    private boolean uploaded = false;

    public String getTid() {
        return Tid;
    }

    public void setTid(String tid) {
        Tid = tid;
    }

    public boolean isRenewed() {
        return renewed;
    }

    public void setRenewed(boolean renewed) {
        this.renewed = renewed;
    }

    public boolean isUploaded() {
        return uploaded;
    }

    public void setUploaded(boolean uploaded) {
        this.uploaded = uploaded;
    }

    public String getTemplateId() {
        return TemplateId;
    }

    public void setTemplateId(String templateId) {
        TemplateId = templateId;
    }

    public String getContractid() {
        return Contractid;
    }

    public void setContractid(String contractid) {
        Contractid = contractid;
    }

    public String getStartdate() {
        return Startdate;
    }

    public void setStartdate(String startdate) {
        Startdate = startdate;
    }

    public String getEnddate() {
        return Enddate;
    }

    public void setEnddate(String enddate) {
        Enddate = enddate;
    }

    public String getContractname() {
        return Contractname;
    }

    public void setContractname(String contractname) {
        Contractname = contractname;
    }

    public String getContracttype() {
        return Contracttype;
    }

    public void setContracttype(String contracttype) {
        Contracttype = contracttype;
    }

    public String getStatus() {
        return Status;
    }

    public void setStatus(String status) {
        Status = status;
    }

    public String getRetailerid() {
        return Retailerid;
    }

    public void setRetailerid(String retailerid) {
        Retailerid = retailerid;
    }

    public String getTypelovid() {
        return typelovid;
    }

    public void setTypelovid(String typelovid) {
        this.typelovid = typelovid;
    }
    public String getCs_id() {
        return cs_id;
    }

    public void setCs_id(String cs_id) {
        this.cs_id = cs_id;
    }

}
