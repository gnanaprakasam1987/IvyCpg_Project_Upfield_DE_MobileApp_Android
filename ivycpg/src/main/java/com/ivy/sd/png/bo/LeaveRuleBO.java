package com.ivy.sd.png.bo;

public class LeaveRuleBO {

    private int leaveTypeLovId, allowedDays, noticeDays;
    private String frequencytype, effectiveFrom, effectiveTo, autoApproval, appliedDays;
    private boolean available;

    public LeaveRuleBO() {
        this.leaveTypeLovId = 0;
        this.allowedDays = 0;
        this.noticeDays = 0;
        this.frequencytype = "";
        this.effectiveFrom = "";
        this.effectiveTo = "";
        this.autoApproval = "";
        this.appliedDays = "";
        this.available = false;
    }

    public boolean isAvailable() {
        return available;
    }

    public void setAvailable(boolean available) {
        this.available = available;
    }

    public String getAppliedDays() {
        return appliedDays;
    }

    public void setAppliedDays(String appliedDays) {
        this.appliedDays = appliedDays;
    }

    public int getLeaveTypeLovId() {
        return leaveTypeLovId;
    }

    public void setLeaveTypeLovId(int leaveTypeLovId) {
        this.leaveTypeLovId = leaveTypeLovId;
    }

    public int getAllowedDays() {
        return allowedDays;
    }

    public void setAllowedDays(int allowedDays) {
        this.allowedDays = allowedDays;
    }

    public int getNoticeDays() {
        return noticeDays;
    }

    public void setNoticeDays(int noticeDays) {
        this.noticeDays = noticeDays;
    }

    public String getFrequencytype() {
        return frequencytype;
    }

    public void setFrequencytype(String frequencytype) {
        this.frequencytype = frequencytype;
    }

    public String getEffectiveFrom() {
        return effectiveFrom;
    }

    public void setEffectiveFrom(String effectiveFrom) {
        this.effectiveFrom = effectiveFrom;
    }

    public String getEffectiveTo() {
        return effectiveTo;
    }

    public void setEffectiveTo(String effectiveTo) {
        this.effectiveTo = effectiveTo;
    }

    public String getAutoApproval() {
        return autoApproval;
    }

    public void setAutoApproval(String autoApproval) {
        this.autoApproval = autoApproval;
    }
}
