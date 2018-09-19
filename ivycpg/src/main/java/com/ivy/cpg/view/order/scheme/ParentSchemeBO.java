package com.ivy.cpg.view.order.scheme;

/**
 * Created by anandasir on 19/7/18.
 */

public class ParentSchemeBO {

    // For QPS Tracker Header
    private int schemeID;
    private String schemeDesc, schemeShortDesc, buyType;
    private double cumulativePurchase, curSlabCumSchAmt, curSlabrsorPer, nextSlabBalance,
            nextSlabCumSchAmt, nextSlabrsorPer;
    private boolean selected;

    public int getSchemeID() {
        return schemeID;
    }

    public void setSchemeID(int schemeID) {
        this.schemeID = schemeID;
    }

    public String getSchemeDesc() {
        return schemeDesc;
    }

    public void setSchemeDesc(String schemeDesc) {
        this.schemeDesc = schemeDesc;
    }

    public String getSchemeShortDesc() {
        return schemeShortDesc;
    }

    public void setSchemeShortDesc(String schemeShortDesc) {
        this.schemeShortDesc = schemeShortDesc;
    }

    public String getBuyType() {
        return buyType;
    }

    public void setBuyType(String buyType) {
        this.buyType = buyType;
    }

    public double getCumulativePurchase() {
        return cumulativePurchase;
    }

    public void setCumulativePurchase(double cumulativePurchase) {
        this.cumulativePurchase = cumulativePurchase;
    }

    public double getCurSlabCumSchAmt() {
        return curSlabCumSchAmt;
    }

    public void setCurSlabCumSchAmt(double curSlabCumSchAmt) {
        this.curSlabCumSchAmt = curSlabCumSchAmt;
    }

    public double getCurSlabrsorPer() {
        return curSlabrsorPer;
    }

    public void setCurSlabrsorPer(double curSlabrsorPer) {
        this.curSlabrsorPer = curSlabrsorPer;
    }

    public double getNextSlabBalance() {
        return nextSlabBalance;
    }

    public void setNextSlabBalance(double nextSlabBalance) {
        this.nextSlabBalance = nextSlabBalance;
    }

    public double getNextSlabCumSchAmt() {
        return nextSlabCumSchAmt;
    }

    public void setNextSlabCumSchAmt(double nextSlabCumSchAmt) {
        this.nextSlabCumSchAmt = nextSlabCumSchAmt;
    }

    public double getNextSlabrsorPer() {
        return nextSlabrsorPer;
    }

    public void setNextSlabrsorPer(double nextSlabrsorPer) {
        this.nextSlabrsorPer = nextSlabrsorPer;
    }

    // For QPS Tracker Calculation
    private double calculatedCumulativePurchase, calculatedcurSlabCumSchAmt, calculatedcurSlabrsorPer, calculatednextSlabBalance,
            calculatednextSlabCumSchAmt, calculatednextSlabrsorPer;

    public double getCalculatedCumulativePurchase() {
        return calculatedCumulativePurchase;
    }

    public void setCalculatedCumulativePurchase(double calculatedCumulativePurchase) {
        this.calculatedCumulativePurchase = calculatedCumulativePurchase;
    }

    public double getCalculatedcurSlabCumSchAmt() {
        return calculatedcurSlabCumSchAmt;
    }

    public void setCalculatedcurSlabCumSchAmt(double calculatedcurSlabCumSchAmt) {
        this.calculatedcurSlabCumSchAmt = calculatedcurSlabCumSchAmt;
    }

    public double getCalculatedcurSlabrsorPer() {
        return calculatedcurSlabrsorPer;
    }

    public void setCalculatedcurSlabrsorPer(double calculatedcurSlabrsorPer) {
        this.calculatedcurSlabrsorPer = calculatedcurSlabrsorPer;
    }

    public double getCalculatednextSlabBalance() {
        return calculatednextSlabBalance;
    }

    public void setCalculatednextSlabBalance(double calculatednextSlabBalance) {
        this.calculatednextSlabBalance = calculatednextSlabBalance;
    }

    public double getCalculatednextSlabCumSchAmt() {
        return calculatednextSlabCumSchAmt;
    }

    public void setCalculatednextSlabCumSchAmt(double calculatednextSlabCumSchAmt) {
        this.calculatednextSlabCumSchAmt = calculatednextSlabCumSchAmt;
    }

    public double getCalculatednextSlabrsorPer() {
        return calculatednextSlabrsorPer;
    }

    public void setCalculatednextSlabrsorPer(double calculatednextSlabrsorPer) {
        this.calculatednextSlabrsorPer = calculatednextSlabrsorPer;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    private String fromDate, toDate;

    public String getFromDate() {
        return fromDate;
    }

    public void setFromDate(String fromDate) {
        this.fromDate = fromDate;
    }

    public String getToDate() {
        return toDate;
    }

    public void setToDate(String toDate) {
        this.toDate = toDate;
    }
}
