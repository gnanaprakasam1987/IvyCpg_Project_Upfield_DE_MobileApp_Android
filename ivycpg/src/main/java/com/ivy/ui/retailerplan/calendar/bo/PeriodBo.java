package com.ivy.ui.retailerplan.calendar.bo;

import com.ivy.utils.StringUtils;

/**
 * Created by mansoor on 20/05/2019
 */
public class PeriodBo {

    public PeriodBo(){

    }

    private String startDate;
    private String endDate;
    private String description;

    public String getStartDate() {
        return startDate;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public String getEndDate() {
        return endDate;
    }

    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public String toString() {
        return description;
    }
}
