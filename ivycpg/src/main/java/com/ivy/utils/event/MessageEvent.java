package com.ivy.utils.event;

import java.util.ArrayList;

public class MessageEvent {

    private String source;

    private ArrayList<Object> eventDataList;

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public ArrayList<Object> getEventDataList() {
        return eventDataList;
    }

    public void setEventDataList(ArrayList<Object> eventDataList) {
        this.eventDataList = eventDataList;
    }
}
