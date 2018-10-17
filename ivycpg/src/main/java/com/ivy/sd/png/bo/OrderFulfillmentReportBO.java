package com.ivy.sd.png.bo;

import java.util.ArrayList;

/**
 * Created by anandasir on 26/9/18.
 */

public class OrderFulfillmentReportBO {

    private String orderID, retailerID, status;

    public String getOrderID() {
        return orderID;
    }

    public void setOrderID(String orderID) {
        this.orderID = orderID;
    }

    public String getRetailerID() {
        return retailerID;
    }

    public void setRetailerID(String retailerID) {
        this.retailerID = retailerID;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    private ArrayList<OrderFulfillmentReportListBO> orderFulfillmentReportDetailList;

    public ArrayList<OrderFulfillmentReportListBO> getOrderFulfillmentReportDetailList() {
        return orderFulfillmentReportDetailList;
    }

    public void setOrderFulfillmentReportDetailList(ArrayList<OrderFulfillmentReportListBO> orderFulfillmentReportDetailList) {
        this.orderFulfillmentReportDetailList = orderFulfillmentReportDetailList;
    }
}
