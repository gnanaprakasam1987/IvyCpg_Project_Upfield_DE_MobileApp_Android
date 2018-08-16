package com.ivy.sd.png.bo;

public class ReasonMaster {

    private String reasonID;
    private String conditionId;
    private String reasonDesc;
    private String reasonCategory;
    private int isPlanned;

    public ReasonMaster() {
    }

    public ReasonMaster(String reasonId, String reasonDesc) {
        this.reasonID = reasonId;
        this.reasonDesc = reasonDesc;
    }

    public String getConditionID() {
        return conditionId;
    }

    public void setConditionID(String conditionId) {
        this.conditionId = conditionId;
    }

    public String getReasonCategory() {
        return reasonCategory;
    }

    public void setReasonCategory(String reasonCategory) {
        this.reasonCategory = reasonCategory;
    }

    @Override
    public String toString() {
        return reasonDesc;
    }

    public String getReasonID() {
        return reasonID;
    }

    public void setReasonID(String reasonID) {
        this.reasonID = reasonID;
    }

    public String getReasonDesc() {
        return reasonDesc;
    }

    public void setReasonDesc(String reasonDesc) {
        this.reasonDesc = reasonDesc;
    }

    public int getIsPlanned() {
        return isPlanned;
    }

    public void setIsPlanned(int isPlanned) {
        this.isPlanned = isPlanned;
    }
}