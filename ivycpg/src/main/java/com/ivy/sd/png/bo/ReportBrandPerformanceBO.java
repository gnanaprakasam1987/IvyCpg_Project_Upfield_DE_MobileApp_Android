package com.ivy.sd.png.bo;

/**
 * Created by maheswaran.m on 05-10-2015.
 */
public class ReportBrandPerformanceBO {

    private String productname;
    private int productivecall, lines;
    private float valueperday, target_achievement;

    public String getProductname() {
        return productname;
    }

    public void setProductname(String productname) {
        this.productname = productname;
    }

    public int getProductivecall() {
        return productivecall;
    }

    public void setProductivecall(int productivecall) {
        this.productivecall = productivecall;
    }

    public int getLines() {
        return lines;
    }

    public void setLines(int lines) {
        this.lines = lines;
    }

    public float getValueperday() {
        return valueperday;
    }

    public void setValueperday(float valueperday) {
        this.valueperday = valueperday;
    }

    public float getTarget_achievement() {
        return target_achievement;
    }

    public void setTarget_achievement(float target_achievement) {
        this.target_achievement = target_achievement;
    }

}
