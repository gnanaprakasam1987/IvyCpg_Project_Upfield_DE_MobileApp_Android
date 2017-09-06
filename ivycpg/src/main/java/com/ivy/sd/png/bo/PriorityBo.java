package com.ivy.sd.png.bo;

/**
 * Created by mansoor.k on 19-07-2017.
 */

public class PriorityBo {
    private int priorityId;
    private int ProductID;
    private String productName;
    private double achievement;

    public int getPriorityId() {
        return priorityId;
    }

    public void setPriorityId(int priorityId) {
        this.priorityId = priorityId;
    }

    public int getProductID() {
        return ProductID;
    }

    public void setProductID(int productID) {
        ProductID = productID;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public double getAchievement() {
        return achievement;
    }

    public void setAchievement(double achievement) {
        this.achievement = achievement;
    }
}
