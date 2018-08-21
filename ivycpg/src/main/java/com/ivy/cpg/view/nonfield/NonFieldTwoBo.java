package com.ivy.cpg.view.nonfield;

/**
 * Created by karthikeyan.a on 3/18/2016.
 */
public class NonFieldTwoBo {

    private String id;
    private String fromDate;
    private String outTime;
    private String inTime;
    private int reasonId;
    private String status;
    private String remarks;
    private int rowid;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getFromDate() {
        return fromDate;
    }

    public void setFromDate(String fromDate) {
        this.fromDate = fromDate;
    }

    public String getInTime() {
        return inTime;
    }

    public void setInTime(String inTime) {
        this.inTime = inTime;
    }

    public String getOutTime() {
        return outTime;
    }

    public void setOutTime(String outTime) {
        this.outTime = outTime;
    }

    public int getReasonId() {
        return reasonId;
    }

    public void setReasonId(int reasonId) {
        this.reasonId = reasonId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    public int getRowid() {
        return rowid;
    }

    public void setRowid(int rowid) {
        this.rowid = rowid;
    }

    @Override
    public String toString() {
        return "id : " + id
                + " From date : " + fromDate
                + " Out time : " + outTime
                + " To Time : " + inTime
                + " Reason : " + reasonId
                + " Status : " + status
                + " Remarks : " + remarks;
    }

    public String getReason() {
        return ReasonID;
    }

    public void setReason(String reasonID) {
        ReasonID = reasonID;
    }

    private String ReasonID;
}
