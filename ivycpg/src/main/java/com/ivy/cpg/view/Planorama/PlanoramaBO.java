package com.ivy.cpg.view.Planorama;

public class PlanoramaBO {

    public String getRetailerName() {
        return retailerName;
    }

    public void setRetailerName(String retailerName) {
        this.retailerName = retailerName;
    }

    public String getRetailerCode() {
        return retailerCode;
    }

    public void setRetailerCode(String retailerCode) {
        this.retailerCode = retailerCode;
    }

    public String getVisitedTime() {
        return visitedTime;
    }

    public void setVisitedTime(String visitedTime) {
        this.visitedTime = visitedTime;
    }

    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }

    public int getNumbertOfPhotosExpected() {
        return numbertOfPhotosExpected;
    }

    public void setNumbertOfPhotosExpected(int numbertOfPhotosExpected) {
        this.numbertOfPhotosExpected = numbertOfPhotosExpected;
    }

    private String retailerName,retailerCode;
    private String visitedTime,comments;
    private int numbertOfPhotosExpected;
}
