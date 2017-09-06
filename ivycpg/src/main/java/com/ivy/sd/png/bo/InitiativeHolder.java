package com.ivy.sd.png.bo;

/**
 * Initiative Report Object Holder Structure class
 * 
 */
public class InitiativeHolder {
	private int initiativeId;
	private String initiativeDesc;
	private double totalInitiative;
	private double totalMTD;
	private int hitCount;
	private int isParent;

	public int getInitiativeId() {
		return initiativeId;
	}

	public void setInitiativeId(int initiativeId) {
		this.initiativeId = initiativeId;
	}

	public String getInitiativeDesc() {
		return initiativeDesc;
	}

	public void setInitiativeDesc(String initiativeDesc) {
		this.initiativeDesc = initiativeDesc;
	}

	public double getTotalInitiative() {
		return totalInitiative;
	}

	public void setTotalInitiative(double totalInitiative) {
		this.totalInitiative = totalInitiative;
	}

	public double getTotalMTD() {
		return totalMTD;
	}

	public void setTotalMTD(double totalMTD) {
		this.totalMTD = totalMTD;
	}

	public int getHitCount() {
		return hitCount;
	}

	public void setHitCount(int hitCount) {
		this.hitCount = hitCount;
	}

	public int getIsParent() {
		return isParent;
	}

	public void setIsParent(int isParent) {
		this.isParent = isParent;
	}

}