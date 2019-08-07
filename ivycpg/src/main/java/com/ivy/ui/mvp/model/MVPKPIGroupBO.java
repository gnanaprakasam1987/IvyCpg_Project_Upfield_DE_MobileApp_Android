package com.ivy.ui.mvp.model;

public class MVPKPIGroupBO {
    private String mvpKpiName;
    private int mvpKPIID;

    public String getMvpKpiName() {
        return mvpKpiName;
    }

    public void setMvpKpiName(String mvpKpiName) {
        this.mvpKpiName = mvpKpiName;
    }

    public int getMvpKPIID() {
        return mvpKPIID;
    }

    public void setMvpKPIID(int mvpKPIID) {
        this.mvpKPIID = mvpKPIID;
    }

    @Override
    public boolean equals(Object object) {
        //return (object).equals(mvpKpiName);
        return object instanceof MVPKPIGroupBO && ((MVPKPIGroupBO) object).mvpKpiName.equals(mvpKpiName);
    }

    @Override
    public int hashCode() {
        return mvpKpiName.hashCode();
    }
}
