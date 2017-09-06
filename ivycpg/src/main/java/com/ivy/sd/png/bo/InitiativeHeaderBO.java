package com.ivy.sd.png.bo;

public class InitiativeHeaderBO {

	private int isCombination,isParent;
	private String description, type, keyword,initiativeId;
	private boolean isDone, isCovered;
	private float sum,valueAchieved,valueBalance;

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

}
