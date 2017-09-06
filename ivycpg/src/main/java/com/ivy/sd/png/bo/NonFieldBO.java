package com.ivy.sd.png.bo;

public class NonFieldBO {
    private String frmDate;
    private String toDate;
    private String session;
    private String reason;
    private String description;
    private String tid;
    private String code;
    private String status;
    private String sessionCode;
    private int reasonID;
    private int sessionID;
    private int isRequired;
    private int pLevelId;
    private int subReasonId;
    private int leaveLovId;
    private double totalDays;
    private String upload;

    private int jointUserId;
    private String timeSpent;

    private boolean isDeleteRequest;

    public String getMonthName() {
        return monthName;
    }

    public void setMonthName(String monthName) {
        this.monthName = monthName;
    }

    private String monthName;

    public NonFieldBO() {
        // no operation
    }

    public String getTimeSpent() {
        return timeSpent;
    }

    public void setTimeSpent(String timeSpent) {
        this.timeSpent = timeSpent;
    }

    public String getTid() {
        return tid;
    }

    public void setTid(String tid) {
        this.tid = tid;
    }

    public double getTotalDays() {
        return totalDays;
    }

    public void setTotalDays(double totalDays) {
        this.totalDays = totalDays;
    }

    public int getLeaveLovId() {
        return leaveLovId;
    }

    public void setLeaveLovId(int leaveLovId) {
        this.leaveLovId = leaveLovId;
    }

    public int getReasonID() {
        return reasonID;
    }

    public void setReasonID(int id) {
        this.reasonID = id;
    }

    public int getsessionID() {
        return sessionID;
    }

    public void setSessionID(int id) {
        this.sessionID = id;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getFrmDate() {
        return frmDate;
    }

    public void setFrmDate(String frmdate) {
        this.frmDate = frmdate;
    }

    public String getToDate() {
        return toDate;
    }

    public void setToDate(String todate) {
        this.toDate = todate;
    }

    public String getSession() {
        return session;
    }

    public void setSession(String sesn) {
        this.session = sesn;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String descr) {
        this.description = descr;
    }

    public boolean isDeleteRequest() {
        return isDeleteRequest;
    }

    public void setDeleteRequest(boolean isDeleteRequest) {
        this.isDeleteRequest = isDeleteRequest;
    }

    public int getIsRequired() {
        return isRequired;
    }

    public void setIsRequired(int isRequired) {
        this.isRequired = isRequired;
    }

    public int getpLevelId() {
        return pLevelId;
    }

    public void setpLevelId(int pLevelId) {
        this.pLevelId = pLevelId;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public int getSubReasonId() {
        return subReasonId;
    }

    public void setSubReasonId(int subReasonId) {
        this.subReasonId = subReasonId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return reason;
    }

    public int getJointUserId() {
        return jointUserId;
    }

    public void setJointUserId(int jointUserId) {
        this.jointUserId = jointUserId;
    }

    public String getSessionCode() {
        return sessionCode;
    }

    public void setSessionCode(String sessionCode) {
        this.sessionCode = sessionCode;
    }

    public String getUpload() {
        return upload;
    }

    public void setUpload(String upload) {
        this.upload = upload;
    }
}
