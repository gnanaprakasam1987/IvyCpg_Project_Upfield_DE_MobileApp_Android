package com.ivy.sd.png.bo;

import android.os.Parcel;
import android.os.Parcelable;

public class SyncRetailerBO implements Parcelable {
	
	private String retailerId;
	private String retailerName;
	private boolean isChecked;

	public SyncRetailerBO() {

	}
	protected SyncRetailerBO(Parcel in) {
		retailerId = in.readString();
		retailerName = in.readString();
		isChecked = in.readByte() != 0;
	}

	public static final Creator<SyncRetailerBO> CREATOR = new Creator<SyncRetailerBO>() {
		@Override
		public SyncRetailerBO createFromParcel(Parcel in) {
			return new SyncRetailerBO(in);
		}

		@Override
		public SyncRetailerBO[] newArray(int size) {
			return new SyncRetailerBO[size];
		}
	};

	public String getRetailerId() {
		return retailerId;
	}

	public void setRetailerId(String retailerId) {
		this.retailerId = retailerId;
	}

	public boolean isChecked() {
		return isChecked;
	}

	public void setChecked(boolean isChecked) {
		this.isChecked = isChecked;
	}

	public String getRetailerName() {
		return retailerName;
	}

	public void setRetailerName(String retailerName) {
		this.retailerName = retailerName;
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(retailerId);
		dest.writeString(retailerName);
		dest.writeByte((byte) (isChecked ? 1 : 0));
	}
}
