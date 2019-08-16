package com.ivy.ui.retailer.filter;

import com.ivy.sd.png.bo.AttributeBO;

import java.util.ArrayList;
import java.util.HashMap;

public class RetailerPlanFilterBo {

    private int isNotVisited;
    private FilterObjectBo taskDate;
    private FilterObjectBo lastVisitDate;

    private ArrayList<String> retailerIds;

    private ArrayList<String> filterAttributeIds;

    private HashMap<String,AttributeBO> filterAttributeIdMap = new HashMap<>();

    private String selectedDate;

    public String getSelectedDate() {
        return selectedDate;
    }

    public void setSelectedDate(String selectedDate) {
        this.selectedDate = selectedDate;
    }

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

    public ArrayList<String> getFilterAttributeIds() {
        return filterAttributeIds;
    }

    public void setFilterAttributeIds(ArrayList<String> filterAttributeIds) {
        this.filterAttributeIds = filterAttributeIds;
    }

    public HashMap<String, AttributeBO> getFilterAttributeIdMap() {
        return filterAttributeIdMap;
    }

    public void setFilterAttributeIdMap(HashMap<String, AttributeBO> filterAttributeIdMap) {
        this.filterAttributeIdMap = filterAttributeIdMap;
    }

    /**
     * 1 - Sort by City Asc
     * 2 - Sort by City Desc
     * 3 - Sort by Pincode Asc
     * 4 - Sort by Pincode Desc
     */
    private int sortBy;

    public int getSortBy() {
        return sortBy;
    }

    public void setSortBy(int sortBy) {
        this.sortBy = sortBy;
    }
}
