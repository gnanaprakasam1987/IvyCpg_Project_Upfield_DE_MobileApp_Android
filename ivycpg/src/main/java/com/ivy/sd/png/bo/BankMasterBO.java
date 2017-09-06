package com.ivy.sd.png.bo;

public class BankMasterBO {

	private String bankName;
	private int bankId;

	@Override
	public String toString() {
		return bankName;
	}

	public String getBankName() {
		return bankName;
	}

	public void setBankName(String bankName) {
		this.bankName = bankName;
	}

	public void setBankId(int bankId) {
		this.bankId = bankId;
	}

	public int getBankId() {
		return bankId;
	}
}
