package com.ivy.ui.retailerplan.calendar.bo;

/**
 * Created by nagaganesh.n on 8/4/2016.
 */

import com.ivy.sd.png.bo.BeatMasterBO;
import com.ivy.sd.png.bo.RetailerMasterBO;

import java.util.ArrayList;
import java.util.List;

public class CalenderBO {

    private int date;
    private String day;
    private String cal_date;
    private List<RetailerMasterBO> retailerMasterBOArrayList = new ArrayList<RetailerMasterBO>();
    private List<BeatMasterBO> beatMasterBOArrayList =new ArrayList<>();
    private RetailerMasterBO retailerMasterBO;
    private boolean isToday;
    private boolean isSelected;
    private String weekDate;

    public CalenderBO() {

    }


    public String getCal_date() {
        return cal_date;
    }

    public void setCal_date(String cal_date) {
        this.cal_date = cal_date;
    }

    public int getDate() {
        return date;
    }

    public void setDate(int date) {
        this.date = date;
    }

    public String getDay() {
        return day;
    }

    public void setDay(String day) {
        this.day = day;
    }

    public List<RetailerMasterBO> getRetailerMasterBOArrayList() {
        return retailerMasterBOArrayList;
    }

    public RetailerMasterBO getRetailerMasterBO() {
        return retailerMasterBO;
    }

    public void setRetailerMasterBO(RetailerMasterBO retailerMasterBO) {
        this.retailerMasterBO = retailerMasterBO;
    }

    public List<BeatMasterBO> getBeatMasterBOArrayList(){
        return beatMasterBOArrayList;
    }

    public void setRetailerMasterBOArrayList(RetailerMasterBO retailerMasterBOArrayList) {
        this.retailerMasterBOArrayList.add(0, retailerMasterBOArrayList);
    }

    public void setBeatMasterBOArrayList(BeatMasterBO beatMasterBOArrayList) {
        this.beatMasterBOArrayList.add(0, beatMasterBOArrayList);
    }

    @Override
    public String toString() {
        return this.retailerMasterBOArrayList.toString();
    }

    public boolean isToday() {
        return isToday;
    }

    public void setToday(boolean today) {
        isToday = today;
    }

    public boolean isSelected() {
        return isSelected;
    }

    public void setSelected(boolean selected) {
        isSelected = selected;
    }

    public String getWeekDate() {
        return weekDate;
    }

    public void setWeekDate(String weekDate) {
        this.weekDate = weekDate;
    }
}

