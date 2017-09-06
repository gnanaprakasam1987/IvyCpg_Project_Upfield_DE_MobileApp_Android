package com.ivy.sd.png.bo;

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
    }

    public LoyaltyBenifitsBO() {

    }

    public int getBenifitsId() {
        return benifitsId;
    }

    public void setBenifitsId(int benifitsId) {
        this.benifitsId = benifitsId;
    }

    public String getBenifitDescription() {
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

    public int getBenifitPoints() {
        return benifitsPoints;
    }

    public void setBenifitPoints(int benifitPoints) {
        this.benifitsPoints = benifitPoints;
    }

    public int getBenifitQty() {
        return benifitQty;
    }

    public void setBenifitQty(int benifitQty) {
        this.benifitQty = benifitQty;
    }
}
