package com.ivy.sd.png.bo;

import java.util.ArrayList;

/**
 * Created by nivetha.s on 11-08-2015.
 */
public class OrderFullfillmentBO {

    private String orderNo;

    public String getOrderNo() {
        return orderNo;
    }

    public void setOrderNo(String morderNo) {
        orderNo = morderNo;
    }

    public String getRetailername() {
        return Retailername;
    }

    public void setRetailername(String retailername) {
        Retailername = retailername;
    }

    public String Retailername;

    public int getPid() {
        return pid;
    }

    public void setPid(int pid) {
        this.pid = pid;
    }


    private int pid;
    public String getRetailerid() {
        return retailerid;
    }

    public void setRetailerid(String retailerid) {
        this.retailerid = retailerid;
    }

    private String retailerid;

    public int getOuterqty() {
        return outerqty;
    }

    public void setOuterqty(int outerqty) {
        this.outerqty = outerqty;
    }

    private int outerqty;

    public int getPieceqty() {
        return pieceqty;
    }

    public void setPieceqty(int pieceqty) {
        this.pieceqty = pieceqty;
    }

    private int pieceqty;

    public int getCaseqty() {
        return caseqty;
    }

    public void setCaseqty(int caseqty) {
        this.caseqty = caseqty;
    }

    private int caseqty;
    private double visit_frequencey;

    public double getVisit_frequencey() {
        return visit_frequencey;
    }

    public void setVisit_frequencey(double visit_frequencey) {
        this.visit_frequencey = visit_frequencey;
    }

    public String getOrderId() {
        return orderId;
    }

    public void setOrderId(String orderId) {
        this.orderId = orderId;
    }

    private  String orderId;

    public double getLineval() {
        return lineval;
    }

    public void setLineval(double lineval) {
        this.lineval = lineval;
    }

    private double lineval;

    public double getValue() {
        return value;
    }

    public void setValue(double value) {
        this.value = value;
    }

    private double value;


    public String getDeliverydate() {
        return deliverydate;
    }

    public void setDeliverydate(String deliverydate) {
        this.deliverydate = deliverydate;
    }

    private String deliverydate;

    public String getReasonId() {
        return reasonid;
    }

    public void setReasonid(String reasonid) {
        this.reasonid = reasonid;
    }

    private String reasonid;

    public int getUomid() {
        return uomid;
    }

    public void setUomid(int uomid) {
        this.uomid = uomid;

    }

    private  int uomid;

    public int getQty() {
        return qty;
    }

    public void setQty(int qty) {
        this.qty = qty;
    }

    private  int qty;

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    private double price;

    public String getPname() {
        return pname;
    }

    public void setPname(String pname) {
        this.pname = pname;
    }

    private  String pname;

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    private String status = "D";

    public ArrayList<OrderFullfillmentBO> getPartialdetailslist() {
        return partialdetailslist;
    }

    public void setPartialdetailslist(ArrayList<OrderFullfillmentBO> partialdetailslist) {
        this.partialdetailslist = partialdetailslist;
    }

    public ArrayList<OrderFullfillmentBO> partialdetailslist;

    public int getDeliveredQty() {
        return DeliveredQty;
    }

    public void setDeliveredQty(int deliveredQty) {
        DeliveredQty = deliveredQty;
    }

    private int DeliveredQty;

    public boolean isFlagrej() {
        return flagrej;
    }

    public void setFlagrej(boolean flagrej) {
        this.flagrej = flagrej;
    }

    private boolean flagrej=false;

    public boolean isFlagpartial() {
        return flagpartial;
    }

    public void setFlagpartial(boolean flagpartial) {
        this.flagpartial = flagpartial;
    }

    private boolean flagpartial=false;

    public boolean isFlagfullfilled() {
        return flagfullfilled;
    }

    public void setFlagfullfilled(boolean flagfullfilled) {
        this.flagfullfilled = flagfullfilled;
    }

    private boolean flagfullfilled=false;
}
