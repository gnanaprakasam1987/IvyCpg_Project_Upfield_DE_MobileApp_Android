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
    private int docExchange = 0;
    private String docReasonId = "";
    private String docRemark     = "";
    private String docSignPath     = "";
    private String docSignImage     = "";
    private String pickListId = "";
    private String comments;

    //pending invoice
    String retailerName;
    private String collectionDate;

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

    public String getDueDays() {
        return dueDays;
    }

    public void setDueDays(String dueDays) {
        this.dueDays = dueDays;
    }

    private String dueDate,dueDays;

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

    public int getDocExchange() {
        return docExchange;
    }

    public void setDocExchange(int docExchange) {
        this.docExchange = docExchange;
    }

    public String getDocReasonId() {
        return docReasonId;
    }

    public void setDocReasonId(String docReasonId) {
        this.docReasonId = docReasonId;
    }

    public String getDocRemark() {
        return docRemark;
    }

    public void setDocRemark(String docRemark) {
        this.docRemark = docRemark;
    }

    public String getDocSignPath() {
        return docSignPath;
    }

    public void setDocSignPath(String docSignPath) {
        this.docSignPath = docSignPath;
    }

    public String getDocSignImage() {
        return docSignImage;
    }

    public void setDocSignImage(String docSignImage) {
        this.docSignImage = docSignImage;
    }

    private String invoiceRefNo;

    public String getInvoiceRefNo() {
        return invoiceRefNo;
    }

    public void setInvoiceRefNo(String invoiceRefNo) {
        this.invoiceRefNo = invoiceRefNo;
    }

    public String getPickListId() {
        return pickListId;
    }

    public void setPickListId(String pickListId) {
        this.pickListId = pickListId;
    }

    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }

    public String getCollectionDate() {
        return collectionDate;
    }

    public void setCollectionDate(String collectionDate) {
        this.collectionDate = collectionDate;
    }
}
