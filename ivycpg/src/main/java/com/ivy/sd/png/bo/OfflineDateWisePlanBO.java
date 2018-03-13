package com.ivy.sd.png.bo;

/**
 * Created by rahul.j on 3/13/2018.
 */

public class OfflineDateWisePlanBO {
    private Integer PlanId, DistributorId, UserId, EntityId, Sequence;
    private String EntityType, Status, upload, Date;

    public Integer getPlanId() {
        return PlanId;
    }

    public void setPlanId(Integer planId) {
        PlanId = planId;
    }

    public Integer getDistributorId() {
        return DistributorId;
    }

    public void setDistributorId(Integer distributorId) {
        DistributorId = distributorId;
    }

    public Integer getUserId() {
        return UserId;
    }

    public void setUserId(Integer userId) {
        UserId = userId;
    }

    public Integer getEntityId() {
        return EntityId;
    }

    public void setEntityId(Integer entityId) {
        EntityId = entityId;
    }

    public Integer getSequence() {
        return Sequence;
    }

    public void setSequence(Integer sequence) {
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

    public String getUpload() {
        return upload;
    }

    public void setUpload(String upload) {
        this.upload = upload;
    }

    public String getDate() {
        return Date;
    }

    public void setDate(String date) {
        Date = date;
    }
}
