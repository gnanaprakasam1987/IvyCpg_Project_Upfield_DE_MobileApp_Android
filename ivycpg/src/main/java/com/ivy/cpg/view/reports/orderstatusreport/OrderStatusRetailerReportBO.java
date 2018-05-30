package com.ivy.cpg.view.reports.orderstatusreport;

/**
 * Created by anandasir on 29/5/18.
 */

public class OrderStatusRetailerReportBO {

    public OrderStatusRetailerReportBO() {

    }

    public OrderStatusRetailerReportBO(String retailerID, String retailerCode, String retailerName) {
        this.retailerID = retailerID;
        this.retailerCode = retailerCode;
        this.retailerName = retailerName;
    }

    private String retailerID, retailerCode, retailerName;

    public String getRetailerID() {
        return retailerID;
    }

    public void setRetailerID(String retailerID) {
        this.retailerID = retailerID;
    }

    public String getRetailerCode() {
        return retailerCode;
    }

    public void setRetailerCode(String retailerCode) {
        this.retailerCode = retailerCode;
    }

    public String getRetailerName() {
        return retailerName;
    }

    public void setRetailerName(String retailerName) {
        this.retailerName = retailerName;
    }

    @Override
    public String toString() {
        return retailerName;
    }
}
