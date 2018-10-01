package com.ivy.sd.png.bo;

/**
 * Created by anandasir on 26/9/18.
 */

public class OrderFulfillmentReportListBO {

    private String orderID, productName, orderedCases, orderedPcs, fulfilledCases, fulfilledPcs;

    public String getOrderID() {
        return orderID;
    }

    public void setOrderID(String orderID) {
        this.orderID = orderID;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getOrderedCases() {
        return orderedCases;
    }

    public void setOrderedCases(String orderedCases) {
        this.orderedCases = orderedCases;
    }

    public String getOrderedPcs() {
        return orderedPcs;
    }

    public void setOrderedPcs(String orderedPcs) {
        this.orderedPcs = orderedPcs;
    }

    public String getFulfilledCases() {
        return fulfilledCases;
    }

    public void setFulfilledCases(String fulfilledCases) {
        this.fulfilledCases = fulfilledCases;
    }

    public String getFulfilledPcs() {
        return fulfilledPcs;
    }

    public void setFulfilledPcs(String fulfilledPcs) {
        this.fulfilledPcs = fulfilledPcs;
    }
}
