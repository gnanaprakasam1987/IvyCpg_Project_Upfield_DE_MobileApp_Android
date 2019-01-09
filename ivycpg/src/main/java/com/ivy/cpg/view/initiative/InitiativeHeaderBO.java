package com.ivy.cpg.view.initiative;

import android.os.Parcel;
import android.os.Parcelable;

public class InitiativeHeaderBO implements Parcelable {

	private int isCombination,isParent;
	private String description, type, keyword,initiativeId;
	private boolean isDone, isCovered;
	private float sum,valueAchieved,valueBalance;

	public InitiativeHeaderBO(){

	}

	protected InitiativeHeaderBO(Parcel in) {
		isCombination = in.readInt();
		isParent = in.readInt();
		description = in.readString();
		type = in.readString();
		keyword = in.readString();
		initiativeId = in.readString();
		isDone = in.readByte() != 0;
		isCovered = in.readByte() != 0;
		sum = in.readFloat();
		valueAchieved = in.readFloat();
		valueBalance = in.readFloat();
	}

	public static final Creator<InitiativeHeaderBO> CREATOR = new Creator<InitiativeHeaderBO>() {
		@Override
		public InitiativeHeaderBO createFromParcel(Parcel in) {
			return new InitiativeHeaderBO(in);
		}

		@Override
		public InitiativeHeaderBO[] newArray(int size) {
			return new InitiativeHeaderBO[size];
		}
	};

	public String getInitiativeId() {
		return initiativeId;
	}

	public void setInitiativeId(String initiativeId) {
		this.initiativeId = initiativeId;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getKeyword() {
		return keyword;
	}

	public void setKeyword(String keyword) {
		this.keyword = keyword;
	}

	public boolean isDone() {
		return isDone;
	}

	public void setDone(boolean isDone) {
		this.isDone = isDone;
	}

	public boolean isDistributed() {
		return isCovered;
	}

	public void setDistributed(boolean isCovered) {
		this.isCovered = isCovered;
	}

	public int getIsCombination() {
		return isCombination;
	}

	public void setIsCombination(int isCombination) {
		this.isCombination = isCombination;
	}

	public float getSum() {
		return sum;
	}

	public void setSum(float sum) {
		this.sum = sum;
	}

	public float getDropValueAchieved() {
		return valueAchieved;
	}

	public void setDropValueAchieved(float valueAchieved) {
		this.valueAchieved = valueAchieved;
	}

	public int getIsParent() {
		return isParent;
	}

	public void setIsParent(int isParent) {
		this.isParent = isParent;
	}

	public void setValueBalance(float valueBalance) {
		this.valueBalance = valueBalance;
	}

	public float getValueBalance() {
		return valueBalance;
	}

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeInt(isCombination);
		dest.writeInt(isParent);
		dest.writeString(description);
		dest.writeString(type);
		dest.writeString(keyword);
		dest.writeString(initiativeId);
		dest.writeByte((byte) (isDone ? 1 : 0));
		dest.writeByte((byte) (isCovered ? 1 : 0));
		dest.writeFloat(sum);
		dest.writeFloat(valueAchieved);
		dest.writeFloat(valueBalance);
	}
}
