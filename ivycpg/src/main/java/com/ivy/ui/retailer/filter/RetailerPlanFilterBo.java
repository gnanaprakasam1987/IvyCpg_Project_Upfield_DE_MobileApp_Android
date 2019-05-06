package com.ivy.ui.retailer.filter;

import java.util.ArrayList;

public class RetailerPlanFilterBo {

    private int isNotVisited;
    private FilterObjectBo taskDate;
    private FilterObjectBo lastVisitDate;

    private ArrayList<String> retailerIds;


    public int getIsNotVisited() {
        return isNotVisited;
    }

    public void setIsNotVisited(int isNotVisited) {
        this.isNotVisited = isNotVisited;
    }

    public FilterObjectBo getTaskDate() {
        return taskDate;
    }

    public void setTaskDate(FilterObjectBo taskDate) {
        this.taskDate = taskDate;
    }

    public FilterObjectBo getLastVisitDate() {
        return lastVisitDate;
    }

    public void setLastVisitDate(FilterObjectBo lastVisitDate) {
        this.lastVisitDate = lastVisitDate;
    }

    public ArrayList<String> getRetailerIds() {
        return retailerIds;
    }

    public void setRetailerIds(ArrayList<String> retailerIds) {
        this.retailerIds = retailerIds;
    }
}
