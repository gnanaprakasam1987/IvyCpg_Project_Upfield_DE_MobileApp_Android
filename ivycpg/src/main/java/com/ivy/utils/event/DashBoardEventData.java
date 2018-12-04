package com.ivy.utils.event;

import com.ivy.cpg.view.dashboard.DashBoardBO;

import java.util.ArrayList;

public class DashBoardEventData {

    private String source;

    private ArrayList<DashBoardBO> eventDataList;

    private int kpiLovId;

    private DashBoardBO smpDashBoardData;

    private String selectedInterval;

    public String getSelectedInterval() {
        return selectedInterval;
    }

    public void setSelectedInterval(String selectedInterval) {
        this.selectedInterval = selectedInterval;
    }

    public DashBoardBO getSmpDashBoardData() {
        return smpDashBoardData;
    }

    public void setSmpDashBoardData(DashBoardBO smpDashBoardData) {
        this.smpDashBoardData = smpDashBoardData;
    }

    public int getKpiLovId() {
        return kpiLovId;
    }

    public void setKpiLovId(int kpiLovId) {
        this.kpiLovId = kpiLovId;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public ArrayList<DashBoardBO> getEventDataList() {
        return eventDataList;
    }

    public void setEventDataList(ArrayList<DashBoardBO> eventDataList) {
        this.eventDataList = eventDataList;
    }
}
