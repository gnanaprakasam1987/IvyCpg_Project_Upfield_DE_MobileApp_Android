package com.ivy.sd.png.bo;

public class BranchMasterBO {
	private String branchID;
	private String branchName;
	private String bankID;
	private String BankbranchCode;

	public String getBankbranchCode() {
		return BankbranchCode;
	}

	public void setBankbranchCode(String bankbranchCode) {
		BankbranchCode = bankbranchCode;
	}

	public String getBranchName() {
		return branchName;
	}

	public void setBranchName(String branchName) {
		this.branchName = branchName;
	}

	public String getBankID() {
		return bankID;
	}

	public void setBankID(String bankid) {
		this.bankID = bankid;
	}

	public String getBranchID() {
		return branchID;
	}

	public void setBranchID(String branchID) {
		this.branchID = branchID;
	}

	@Override
	public String toString() {
		return branchName;
	}
}
