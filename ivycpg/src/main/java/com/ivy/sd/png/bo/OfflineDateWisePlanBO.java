package com.ivy.sd.png.bo;

/**
 * Created by rahul.j on 3/13/2018.
 */

public class OfflineDateWisePlanBO {
    private int PlanId, DistributorId, UserId, EntityId, Sequence;
    private String EntityType, Status, upload, Date,Name;

    public int getPlanId() {
        return PlanId;
    }

    public void setPlanId(int planId) {
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

    public String getName() {
        return Name;
    }

    public void setName(String name) {
        Name = name;
    }
}
