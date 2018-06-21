package com.ivy.cpg.view.dashboard;

/**
 * Created by anbarasan on 2/5/18.
 */

public class IncentiveDashboardDefinitionBO {

    String factor;
    String salesParam;
    String achPercentage;
    String maxOpportunity;
    String groups;
    String incentiveType;
    boolean isNewFactor;
    boolean isNewPackage;

    public boolean isNewPackage() {
        return isNewPackage;
    }

    public void setNewPackage(boolean newPackage) {
        isNewPackage = newPackage;
    }

    public boolean getIsNewFactor() {
        return isNewFactor;
    }


    public void setIsNewFactor(boolean isNewFactor) {
        this.isNewFactor = isNewFactor;
    }

    private boolean isNewGroup;


    public boolean getIsNewGroup() {
        return isNewGroup;
    }

    public void setIsNewGroup(boolean isNewGroup) {
        this.isNewGroup = isNewGroup;
    }

    public String getFactor() {
        return factor;
    }

    public void setFactor(String factor) {
        this.factor = factor;
    }

    public String getSalesParam() {
        return salesParam;
    }

    public void setSalesParam(String salesParam) {
        this.salesParam = salesParam;
    }

    public String getAchPercentage() {
        return achPercentage;
    }

    public void setAchPercentage(String achPercentage) {
        this.achPercentage = achPercentage;
    }

    public String getMaxOpportunity() {
        return maxOpportunity;
    }

    public void setMaxOpportunity(String maxOpportunity) {
        this.maxOpportunity = maxOpportunity;
    }

    public String getGroups() {
        return groups;
    }

    public void setGroups(String groups) {
        this.groups = groups;
    }

    public String getIncentiveType() {
        return incentiveType;
    }

    public void setIncentiveType(String incentiveType) {
        this.incentiveType = incentiveType;
    }
}
