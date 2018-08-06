package com.ivy.cpg.view.reports.pndInvoiceReport;

/**
 * Created by ivyuser on 24/7/18.
 */

public class PndInvoiceReportBo {

    private String invoiceNo;
    private String invoiceDate;
    private double balance;
    private double paidAmount;
    private double invoiceAmount;
    String retailerName;
    private double appliedDiscountAmount;
    private double remainingDiscountAmt;
    private String dueDate,dueDays;

    public String getDueDate() {
        return dueDate;
    }

    public void setDueDate(String dueDate) {
        this.dueDate = dueDate;
    }

    public String getDueDays() {
        return dueDays;
    }

    public void setDueDays(String dueDays) {
        this.dueDays = dueDays;
    }

    public double getAppliedDiscountAmount() {
        return appliedDiscountAmount;
    }

    public void setAppliedDiscountAmount(double appliedDiscountAmount) {
        this.appliedDiscountAmount = appliedDiscountAmount;
    }

    public double getRemainingDiscountAmt() {
        return remainingDiscountAmt;
    }

    public void setRemainingDiscountAmt(double remainingDiscountAmt) {
        this.remainingDiscountAmt = remainingDiscountAmt;
    }

    public String getInvoiceNo() {
        return invoiceNo;
    }

    public void setInvoiceNo(String invoiceNo) {
        this.invoiceNo = invoiceNo;
    }

    public String getInvoiceDate() {
        return invoiceDate;
    }

    public void setInvoiceDate(String invoiceDate) {
        this.invoiceDate = invoiceDate;
    }

    public double getBalance() {
        return balance;
    }

    public void setBalance(double balance) {
        this.balance = balance;
    }

    public double getPaidAmount() {
        return paidAmount;
    }

    public void setPaidAmount(double paidAmount) {
        this.paidAmount = paidAmount;
    }

    public double getInvoiceAmount() {
        return invoiceAmount;
    }

    public void setInvoiceAmount(double invoiceAmount) {
        this.invoiceAmount = invoiceAmount;
    }

    public String getRetailerName() {
        return retailerName;
    }

    public void setRetailerName(String retailerName) {
        this.retailerName = retailerName;
    }
}


