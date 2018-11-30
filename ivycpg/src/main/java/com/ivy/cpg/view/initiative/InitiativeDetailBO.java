package com.ivy.cpg.view.initiative;

public class InitiativeDetailBO {

	private String initId, productId;
	private float initiativeBalanceValue, initiativeValue,dropQtyValueAcheivement,dropQtyValueTarget;
	private boolean isCovered, isDistributed;
	

	private float acheivedValue;

	public String getInitId() {
		return initId;
	}

	public void setInitId(String initId) {
		this.initId = initId;
	}

	public String getProductId() {
		return productId;
	}

	public void setProductId(String productId) {
		this.productId = productId;
	}

	public float getInitiativeBalanceValue() {
		return initiativeBalanceValue;
	}

	public void setInitiativeBalanceValue(float value) {
		this.initiativeBalanceValue = value;
	}

	public boolean isDone() {
		return isCovered;
	}

	public void setDone(boolean isCovered) {
		this.isCovered = isCovered;
	}

	public float getAcheivedValue() {
		return acheivedValue;
	}

	public void setAcheivedValue(float acheivedValue) {
		this.acheivedValue = acheivedValue;
	}

	public float getInitiativeValue() {
		return initiativeValue;
	}

	public void setInitiativeValue(float initiativeValue) {
		this.initiativeValue = initiativeValue;
	}

	public boolean isDistributed() {
		return isDistributed;
	}

	public void setDistributed(boolean isDistributed) {
		this.isDistributed = isDistributed;
	}

	public float getDropQtyValueAcheivement() {
		return dropQtyValueAcheivement;
	}

	public void setDropQtyValueAcheivement(float dropQtyValueAcheivement) {
		this.dropQtyValueAcheivement = dropQtyValueAcheivement;
	}

	public float getDropQtyValueTarget() {
		return dropQtyValueTarget;
	}

	public void setDropQtyValueTarget(float dropQtyValueTarget) {
		this.dropQtyValueTarget = dropQtyValueTarget;
	}

}
