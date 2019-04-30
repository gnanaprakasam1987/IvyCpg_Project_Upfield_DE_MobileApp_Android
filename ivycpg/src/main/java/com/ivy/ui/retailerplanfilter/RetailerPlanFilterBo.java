package com.ivy.ui.retailerplanfilter;

public class RetailerPlanFilterBo {

    private boolean isNotVisited;
    private FilterObjectBo taskDate;
    private FilterObjectBo lastVisitDate;



    public boolean isNotVisited() {
        return isNotVisited;
    }

    public void setNotVisited(boolean notVisited) {
        isNotVisited = notVisited;
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
}
