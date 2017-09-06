/**
 * 
 */
package com.ivy.sd.png.bo;

/**
 * @author gnanaprakasam.d
 * 
 */
public class MonthlyInitiativeBo {

	private int rId, initId, subChannelId;
	private double target;

	public double getTarget() {
		return target;
	}

	public void setTarget(double target) {
		this.target = target;
	}

	public int getrId() {
		return rId;
	}

	public void setrId(int rId) {
		this.rId = rId;
	}

	private String initDesc;

	public int getInitId() {
		return initId;
	}

	public void setInitId(int initId) {
		this.initId = initId;
	}

	public String getInitDesc() {
		return initDesc;
	}

	public void setInitDesc(String initDesc) {
		this.initDesc = initDesc;
	}

	public int getSubChannelId() {
		return subChannelId;
	}

	public void setSubChannelId(int subChannelId) {
		this.subChannelId = subChannelId;
	}

	@Override
	public String toString() {
		return initDesc;
	}
}
