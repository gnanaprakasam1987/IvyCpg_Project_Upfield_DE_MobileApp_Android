package com.ivy.cpg.view.retailercontact;

public class RetailerContactAvailBo {

    private String day;
    private String from;
    private String to;
    private String status="";
    private String cpaid="";

    public String getCpaid() {
        return cpaid;
    }

    public void setCpaid(String cpaid) {
        this.cpaid = cpaid;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getDay() {
        return day;
    }

    public void setDay(String day) {
        this.day = day;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public String getTo() {
        return to;
    }

    public void setTo(String to) {
        this.to = to;
    }
}
