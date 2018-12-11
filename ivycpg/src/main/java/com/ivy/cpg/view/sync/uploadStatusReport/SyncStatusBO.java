package com.ivy.cpg.view.sync.uploadStatusReport;

/**
 * Created by anbarasan on 25/4/18.
 */

public class SyncStatusBO {

    private String id;
    private String name;
    private int count;
    private int showDateTime;

    public int getShowDateTime() {
        return showDateTime;
    }

    public void setShowDateTime(int showDateTime) {
        this.showDateTime = showDateTime;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }
}
