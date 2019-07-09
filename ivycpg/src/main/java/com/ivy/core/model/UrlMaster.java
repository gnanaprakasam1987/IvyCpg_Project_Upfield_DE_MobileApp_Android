package com.ivy.core.model;

public class UrlMaster {

    private String url;

    private int isMandatory;

    private int isOnDemand;

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public int getIsMandatory() {
        return isMandatory;
    }

    public void setIsMandatory(int isMandatory) {
        this.isMandatory = isMandatory;
    }

    public int getIsOnDemand() {
        return isOnDemand;
    }

    public void setIsOnDemand(int isOnDemand) {
        this.isOnDemand = isOnDemand;
    }
}
