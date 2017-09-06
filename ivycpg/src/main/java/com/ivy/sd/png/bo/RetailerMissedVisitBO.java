package com.ivy.sd.png.bo;

public class RetailerMissedVisitBO {
    private String retailerId;
    private String retailerName;
    private String missedDate;
    private String reasonDes;
    private int beatId;
    private int plannedVisitCount = 0;
    private int missedCount = 0;

    public RetailerMissedVisitBO() {
        super();
    }

    public RetailerMissedVisitBO(String retailerId, String retailerName, int plannedVisitCount, int missedCount) {
        super();
        this.retailerId = retailerId;
        this.retailerName = retailerName;
        this.plannedVisitCount = plannedVisitCount;
        this.missedCount = missedCount;
    }

    public String getRetailerId() {
        return retailerId;
    }

    public void setRetailerId(String retailerId) {
        this.retailerId = retailerId;
    }

    public String getRetailerName() {
        return retailerName;
    }

    public void setRetailerName(String retailerName) {
        this.retailerName = retailerName;
    }

    public String getMissedDate() {
        return missedDate;
    }

    public void setMissedDate(String missedDate) {
        this.missedDate = missedDate;
    }

    public String getReasonDes() {
        return reasonDes;
    }

    public void setReasonDes(String reasonDes) {
        this.reasonDes = reasonDes;
    }

    @Override
    public String toString() {
        return retailerName;
    }

    public int getBeatId() {
        return beatId;
    }

    public void setBeatId(int beatId) {
        this.beatId = beatId;
    }

    public int getPlannedVisitCount() {
        return plannedVisitCount;
    }

    public void setPlannedVisitCount(int plannedVisitCount) {
        this.plannedVisitCount = plannedVisitCount;
    }

    public int getMissedCount() {
        return missedCount;
    }

    public void setMissedCount(int missedCount) {
        this.missedCount = missedCount;
    }
}
