package com.ivy.utils.event;

import com.ivy.cpg.view.dashboard.DashBoardBO;

import java.util.ArrayList;

public class MessageEvent {

    private String source;

    private ArrayList<DashBoardBO> eventDataList;

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
