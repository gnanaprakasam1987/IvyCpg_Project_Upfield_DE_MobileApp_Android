package com.ivy.ui.retailerplan.addplan;

public class DateWisePlanBo {

    private int DistributorId, UserId, EntityId, Sequence,cancelReasonId;
    private String EntityType, Status, Date="",Name,startTime="",endTime="",visitStatus,planStatus ="";
    private boolean isServerData;
    private boolean isAdhoc;
    private long PlanId;

    private String operationType;

    public String getOperationType() {
        return operationType;
    }

    public void setOperationType(String operationType) {
        this.operationType = operationType;
    }

    public boolean isServerData() {
        return isServerData;
    }

    public void setServerData(boolean serverData) {
        isServerData = serverData;
    }

    public long getPlanId() {
        return PlanId;
    }

    public void setPlanId(long planId) {
        PlanId = planId;
    }

    public int getDistributorId() {
        return DistributorId;
    }

    public void setDistributorId(int distributorId) {
        DistributorId = distributorId;
    }

    public int getUserId() {
        return UserId;
    }

    public void setUserId(int userId) {
        UserId = userId;
    }

    public int getEntityId() {
        return EntityId;
    }

    public void setEntityId(int entityId) {
        EntityId = entityId;
    }

    public int getSequence() {
        return Sequence;
    }

    public void setSequence(int sequence) {
        Sequence = sequence;
    }

    public String getEntityType() {
        return EntityType;
    }

    public void setEntityType(String entityType) {
        EntityType = entityType;
    }

    public String getStatus() {
        return Status;
    }

    public void setStatus(String status) {
        Status = status;
    }

    public String getDate() {
        return Date;
    }

    public void setDate(String date) {
        Date = date;
    }

    public String getName() {
        return Name;
    }

    public void setName(String name) {
        Name = name;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public int getCancelReasonId() {
        return cancelReasonId;
    }

    public void setCancelReasonId(int cancelReasonId) {
        this.cancelReasonId = cancelReasonId;
    }

    public String getVisitStatus() {
        return visitStatus;
    }

    public void setVisitStatus(String visitStatus) {
        this.visitStatus = visitStatus;
    }

    public String getPlanStatus() {
        return planStatus;
    }

    public void setPlanStatus(String planStatus) {
        this.planStatus = planStatus;
    }

    public boolean isAdhoc() {
        return isAdhoc;
    }

    public void setAdhoc(boolean adhoc) {
        isAdhoc = adhoc;
    }
}
