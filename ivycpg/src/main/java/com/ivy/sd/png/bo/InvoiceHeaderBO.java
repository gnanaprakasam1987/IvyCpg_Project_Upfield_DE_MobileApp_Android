package com.ivy.sd.png.bo;

public class InvoiceHeaderBO {

    private String invoiceNo;
    private String invoiceDate;
    private double balance;
    private double paidAmount;
    private double invoiceAmount;
    private double appliedDiscountAmount;
    private double remainingDiscountAmt;
    private int linesPerCall;

    /*Collection Reference*/
    private String contactName = "";
    private String contactNo = "";
    private String docRefNo = "";

    //pending invoice
    String retailerName;

    public boolean isDebitNote() {
        return isDebitNote;
    }

    public void setDebitNote(boolean debitNote) {
        isDebitNote = debitNote;
    }

    private boolean isDebitNote;

    public String getDueDate() {
        return dueDate;
    }

    public void setDueDate(String dueDate) {
        this.dueDate = dueDate;
    }

    private String dueDate;

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

    public int getLinesPerCall() {
        return linesPerCall;
    }

    public void setLinesPerCall(int linesPerCall) {
        this.linesPerCall = linesPerCall;
    }

    private boolean isChkBoxChecked = false;

    public double getPaidAmount() {
        return paidAmount;
    }

    public void setPaidAmount(double amount) {
        this.paidAmount = amount;
    }

    public double getBalance() {
        return balance;
    }

    public void setBalance(double balance) {
        this.balance = balance;
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

    public double getInvoiceAmount() {
        return invoiceAmount;
    }

    public void setInvoiceAmount(double invoiceAmount) {
        this.invoiceAmount = invoiceAmount;
    }

    public boolean isChkBoxChecked() {
        return isChkBoxChecked;
    }

    public void setChkBoxChecked(boolean isChkBoxChecked) {
        this.isChkBoxChecked = isChkBoxChecked;
    }

    public String getContactName() {
        return contactName;
    }

    public void setContactName(String contactName) {
        this.contactName = contactName;
    }

    public String getContactNo() {
        return contactNo;
    }

    public void setContactNo(String contactNo) {
        this.contactNo = contactNo;
    }

    public String getDocRefNo() {
        return docRefNo;
    }

    public void setDocRefNo(String docRefNo) {
        this.docRefNo = docRefNo;
    }

    public String getRetailerName() {
        return retailerName;
    }

    public void setRetailerName(String retailerName) {
        this.retailerName = retailerName;
    }
}
