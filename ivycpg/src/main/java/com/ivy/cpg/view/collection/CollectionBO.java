package com.ivy.cpg.view.collection;

import android.os.Parcel;
import android.os.Parcelable;

public class CollectionBO implements Parcelable {

	private double cashamt, chequeamt, discountamt, creditamt,
			mobilePaymentamt;
	private int percent;
	private String bankId,branchId;

	public CollectionBO(){

	}

	protected CollectionBO(Parcel in) {
		cashamt = in.readDouble();
		chequeamt = in.readDouble();
		discountamt = in.readDouble();
		creditamt = in.readDouble();
		mobilePaymentamt = in.readDouble();
		percent = in.readInt();
		bankId = in.readString();
		branchId = in.readString();
		description = in.readString();
		no_of_coupon = in.readString();
	}

	public static final Creator<CollectionBO> CREATOR = new Creator<CollectionBO>() {
		@Override
		public CollectionBO createFromParcel(Parcel in) {
			return new CollectionBO(in);
		}

		@Override
		public CollectionBO[] newArray(int size) {
			return new CollectionBO[size];
		}
	};

	public int getPercent() {
		return percent;
	}

	public void setPercent(int percent) {
		this.percent = percent;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	private String description;

	public double getCashamt() {
		return cashamt;
	}

	public void setCashamt(double cashamt) {
		this.cashamt = cashamt;
	}

	public double getChequeamt() {
		return chequeamt;
	}

	public void setChequeamt(double chequeamt) {
		this.chequeamt = chequeamt;
	}

	public double getDiscountamt() {
		return discountamt;
	}

	public void setDiscountamt(double discountamt) {
		this.discountamt = discountamt;
	}

	public double getCreditamt() {
		return creditamt;
	}

	public void setCreditamt(double creditamt) {
		this.creditamt = creditamt;
	}

	public double getMobilePaymentamt() {
		return mobilePaymentamt;
	}

	public void setMobilePaymentamt(double mobilePaymentamt) {
		this.mobilePaymentamt = mobilePaymentamt;
	}

	public String getBankId() {
		return bankId;
	}

	public void setBankId(String bankId) {
		this.bankId = bankId;
	}

	public String getBranchId() { 
		return branchId;
	}

	public void setBranchId(String branchId) {
		this.branchId = branchId;
	}

	public String getNo_of_coupon() {
		return no_of_coupon;
	}

	public void setNo_of_coupon(String no_of_coupon) {
		this.no_of_coupon = no_of_coupon;
	}

	public String no_of_coupon;

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeDouble(cashamt);
		dest.writeDouble(chequeamt);
		dest.writeDouble(discountamt);
		dest.writeDouble(creditamt);
		dest.writeDouble(mobilePaymentamt);
		dest.writeInt(percent);
		dest.writeString(bankId);
		dest.writeString(branchId);
		dest.writeString(description);
		dest.writeString(no_of_coupon);
	}
}
