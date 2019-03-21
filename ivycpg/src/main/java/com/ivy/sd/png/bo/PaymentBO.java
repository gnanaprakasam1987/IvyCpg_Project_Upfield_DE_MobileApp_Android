package com.ivy.sd.png.bo;

import java.util.Comparator;

public class PaymentBO {
	private String BillNumber;
	private String ReceiptDate;
	private String CashMode;
	private String ChequeNumber;
	private String BranchId = "";
	private String BankID = "";
	private String ChequeDate = "";
	private String UserId;
	private String BeatID;
	private String retailerName;
	private String retailerCode;
	private String invoiceDate;
	private String collectionDate;
	private String listName;
	private String imageName;
	private String AccountNumber;
	private double InvoiceAmount;
	private double Balance;
	private double Amount;
	private double discountedAmount;
	private double appliedDiscountAmount;
	private double updatePayableamt;
	private double previousPaidAmount;
	private boolean creditBalancePayment;

    //for Others type Selected in Bankname spinner
    private String BankName;
    private String BranchName;

    private String uid;


	public String getReferenceNumber() {
		return referenceNumber;
	}

	public void setReferenceNumber(String referenceNumber) {
		this.referenceNumber = referenceNumber;
	}

	private String referenceNumber;
	public String getCollectionDateTime() {
		return collectionDateTime;
	}

	public void setCollectionDateTime(String collectionDateTime) {
		this.collectionDateTime = collectionDateTime;
	}

	private String collectionDateTime;

	public String getRetailerCode() {
		return retailerCode;
	}

	public void setRetailerCode(String retailerCode) {
		this.retailerCode = retailerCode;
	}
	public String getGroupId() {
		return groupId;
	}

	public void setGroupId(String groupId) {
		this.groupId = groupId;
	}

	private String groupId;

	public double getUpdatePayableamt() {
		return updatePayableamt;
	}

	public void setUpdatePayableamt(double updatePayableamt) {
		this.updatePayableamt = updatePayableamt;
	}

	public double getAppliedDiscountAmount() {
		return appliedDiscountAmount;
	}

	public void setAppliedDiscountAmount(double appliedDiscountAmount) {
		this.appliedDiscountAmount = appliedDiscountAmount;
	}

	public double getPreviousPaidAmount() {
		return previousPaidAmount;
	}

	public void setPreviousPaidAmount(double previousPaidAmount) {
		this.previousPaidAmount = previousPaidAmount;
	}

	public boolean isCreditBalancePayment() {
		return creditBalancePayment;
	}

	public void setCreditBalancePayment(boolean creditBalancePayment) {
		this.creditBalancePayment = creditBalancePayment;
	}

	/* paymentTransactioID used to store listid of normal payment or advanced payment*/
	private String paymentTransactioMode;

	public String getBranchId() {
		return BranchId;
	}

	public void setBranchId(String branchId) {
		this.BranchId = branchId;
	}

	public String getBeatID() {
		return BeatID;
	}

	public void setBeatID(String beatID) {
		this.BeatID = beatID;
	}

	public String getBillNumber() {
		return BillNumber;
	}

	public void setBillNumber(String billNumber) {
		BillNumber = billNumber;
	}

	public String getReceiptDate() {
		return ReceiptDate;
	}

	public void setReceiptDate(String receiptDate) {
		ReceiptDate = receiptDate;
	}

	public double getInvoiceAmount() {
		return InvoiceAmount;
	}

	public void setInvoiceAmount(double invoiceAmount) {
		InvoiceAmount = invoiceAmount;
	}

	public double getBalance() {
		return Balance;
	}

	public void setBalance(double balance) {
		Balance = balance;
	}

	public String getCashMode() {
		return CashMode;
	}

	public void setCashMode(String cashMode) {
		CashMode = cashMode;
	}

	public String getChequeNumber() {
		return ChequeNumber;
	}

	public void setChequeNumber(String chequeNumber) {
		ChequeNumber = chequeNumber;
	}

	public double getAmount() {
		return Amount;
	}

	public void setAmount(double amount) {
		Amount = amount;
	}

	public String getBankID() {
		return BankID;
	}

	public void setBankID(String bankID) {
		BankID = bankID;
	}

	public String getChequeDate() {
		return ChequeDate;
	}

	public void setChequeDate(String chequeDate) {
		ChequeDate = chequeDate;
	}

	public String getUserId() {
		return UserId;
	}

	public void setUserId(String userId) {
		UserId = userId;
	}

	public String getRetailerName() {
		return retailerName;
	}

	public void setRetailerName(String retailerName) {
		this.retailerName = retailerName;
	}

	public String getInvoiceDate() {
		return invoiceDate;
	}

	public void setInvoiceDate(String invoiceDate) {
		this.invoiceDate = invoiceDate;
	}

	public String getCollectionDate() {
		return collectionDate;
	}

	public void setCollectionDate(String collectionDate) {
		this.collectionDate = collectionDate;
	}

	public String getListName() {
		return listName;
	}

	public void setListName(String listName) {
		this.listName = listName;
	}

	public double getDiscountedAmount() {
		return discountedAmount;
	}

	public void setDiscountedAmount(double discountedAmount) {
		this.discountedAmount = discountedAmount;
	}

	public String getPaymentTransactioMode() {
		return paymentTransactioMode;
	}

	public void setPaymentTransactioMode(String paymentTransactioMode) {
		this.paymentTransactioMode = paymentTransactioMode;
	}

	public String getImageName() {
		return imageName;
	}

	public void setImageName(String imageName) {
		this.imageName = imageName;
	}

	public String getAccountNumber() {
		return AccountNumber;
	}

	public void setAccountNumber(String accountNumber) {
		AccountNumber = accountNumber;
	}

	// For AdvancePayment
	public String getAdvancePaymentId() {
		return advancePaymentId;
	}

	public void setAdvancePaymentId(String advancePaymentId) {
		this.advancePaymentId = advancePaymentId;
	}

	public String getAdvancePaymentDate() {
		return advancePaymentDate;
	}

	public void setAdvancePaymentDate(String advancePaymentDate) {
		this.advancePaymentDate = advancePaymentDate;
	}

	private String advancePaymentId,advancePaymentDate;

	public static final Comparator<PaymentBO> RetailerNameComparator = new Comparator<PaymentBO>() {

		public int compare(PaymentBO mPayBO1, PaymentBO mPayBO2) {

			return mPayBO1.getRetailerName().compareTo(mPayBO2.getRetailerName());
		}

	};

    public String getBankName() {
        return BankName;
    }

    public void setBankName(String bankName) {
        BankName = bankName;
    }

    public String getBranchName() {
        return BranchName;
    }

    public void setBranchName(String branchName) {
        BranchName = branchName;
    }

	public String getUid() {
		return uid;
	}

	public void setUid(String uid) {
		this.uid = uid;
	}
}
