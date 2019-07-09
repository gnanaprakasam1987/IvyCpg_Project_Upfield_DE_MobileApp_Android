package com.ivy.ui.reports.syncreport.model;

import androidx.annotation.NonNull;

public class SyncReportBO implements Comparable<SyncReportBO> {

    private String tid;
    private String apiname;
    private String startTime;
    private String endTime;
    private String tablename;
    private int recordCount;

    public String getTid() {
        return tid;
    }

    public void setTid(String tid) {
        this.tid = tid;
    }

    public String getApiname() {
        return apiname;
    }

    public void setApiname(String apiname) {
        this.apiname = apiname;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public String getTablename() {
        return tablename;
    }

    public void setTablename(String tablename) {
        this.tablename = tablename;
    }

    public int getRecordCount() {
        return recordCount;
    }

    public void setRecordCount(int recordCount) {
        this.recordCount = recordCount;
    }

    @Override
    public int compareTo(@NonNull SyncReportBO o) {
        return this.getTablename().compareTo(o.getTablename());
    }

    @Override
    public String toString() {
        return tablename;
    }
}
