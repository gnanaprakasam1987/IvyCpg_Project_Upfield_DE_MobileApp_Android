package com.ivy.cpg.view.loyality;

/**
 * Created by hanifa.m on 10/6/2016.
 */

public class LoyaltyBenifitsBO {

    private int benifitsId;
    private String benifitDescription;
    private String imagePath;
    private int benifitsPoints;
    private int benifitQty;

    public LoyaltyBenifitsBO(LoyaltyBenifitsBO benifitsObj) {

        this.benifitsId = benifitsObj.benifitsId;
        this.benifitDescription = benifitsObj.benifitDescription;
        this.imagePath = benifitsObj.imagePath;
        this.benifitsPoints = benifitsObj.benifitsPoints;
        this.pointTypeId=benifitsObj.getPointTypeId();
        this.benifitQty=benifitsObj.getBenifitQty();
    }

    public LoyaltyBenifitsBO() {

    }

    int getBenifitsId() {
        return benifitsId;
    }

    public void setBenifitsId(int benifitsId) {
        this.benifitsId = benifitsId;
    }

    String getBenifitDescription() {
        return benifitDescription;
    }

    public void setBenifitDescription(String benifitDescription) {
        this.benifitDescription = benifitDescription;
    }

    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(String imagePath) {
        this.imagePath = imagePath;
    }

    int getBenifitPoints() {
        return benifitsPoints;
    }

    public void setBenifitPoints(int benifitPoints) {
        this.benifitsPoints = benifitPoints;
    }

    int getBenifitQty() {
        return benifitQty;
    }

    void setBenifitQty(int benifitQty) {
        this.benifitQty = benifitQty;
    }

    public int getPointTypeId() {
        return pointTypeId;
    }

    public void setPointTypeId(int pointTypeId) {
        this.pointTypeId = pointTypeId;
    }

    private  int pointTypeId;
}
