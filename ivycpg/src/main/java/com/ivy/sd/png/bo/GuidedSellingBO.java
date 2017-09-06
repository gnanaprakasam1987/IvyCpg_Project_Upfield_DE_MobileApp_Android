package com.ivy.sd.png.bo;

/**
 * Created by rajkumar.s on 5/22/2017.
 */

public class GuidedSellingBO {

    private String subActivity,filterCode,applyLevel;
    private int sequance;

    public String getSubActivity() {
        return subActivity;
    }

    public void setSubActivity(String subActivity) {
        this.subActivity = subActivity;
    }

    public String getFilterCode() {
        return filterCode;
    }

    public void setFilterCode(String filterCode) {
        this.filterCode = filterCode;
    }

    public String getApplyLevel() {
        return applyLevel;
    }

    public void setApplyLevel(String applyLevel) {
        this.applyLevel = applyLevel;
    }

    public int getSequance() {
        return sequance;
    }

    public void setSequance(int sequance) {
        this.sequance = sequance;
    }

    public boolean isProductFilter() {
        return isProductFilter;
    }

    public void setProductFilter(boolean productFilter) {
        isProductFilter = productFilter;
    }

    private boolean isProductFilter;

    public boolean isDone() {
        return isDone;
    }

    public void setDone(boolean done) {
        isDone = done;
    }

    private boolean isDone;

    public boolean isCurrent() {
        return isCurrent;
    }

    public void setCurrent(boolean current) {
        isCurrent = current;
    }

    private boolean isCurrent;
}
