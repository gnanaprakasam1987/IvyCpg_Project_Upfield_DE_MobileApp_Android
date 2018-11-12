package com.ivy.cpg.view.offlineplanning;

/**
 * Created by rahul.j on 3/13/2018.
 */

public class OfflineDateWisePlanBO {
    private int PlanId, DistributorId, UserId, EntityId, Sequence;
    private String EntityType, Status, Date,Name;

    int getPlanId() {
        return PlanId;
    }

    void setPlanId(int planId) {
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

    int getEntityId() {
        return EntityId;
    }

    void setEntityId(int entityId) {
        EntityId = entityId;
    }

    public int getSequence() {
        return Sequence;
    }

    public void setSequence(int sequence) {
        Sequence = sequence;
    }

    String getEntityType() {
        return EntityType;
    }

    void setEntityType(String entityType) {
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
}
