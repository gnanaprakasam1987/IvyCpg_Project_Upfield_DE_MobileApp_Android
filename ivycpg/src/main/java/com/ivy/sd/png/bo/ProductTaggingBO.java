package com.ivy.sd.png.bo;

/**
 * Created by anandasir on 8/5/18.
 */

public class ProductTaggingBO {

    private String headerID, pid;
    private int fromNorm, toNorm, weightage;

    public String getHeaderID() {
        return headerID;
    }

    public void setHeaderID(String headerID) {
        this.headerID = headerID;
    }

    public String getPid() {
        return pid;
    }

    public void setPid(String pid) {
        this.pid = pid;
    }

    public int getFromNorm() {
        return fromNorm;
    }

    public void setFromNorm(int fromNorm) {
        this.fromNorm = fromNorm;
    }

    public int getToNorm() {
        return toNorm;
    }

    public void setToNorm(int toNorm) {
        this.toNorm = toNorm;
    }

    public int getWeightage() {
        return weightage;
    }

    public void setWeightage(int weightage) {
        this.weightage = weightage;
    }
}
