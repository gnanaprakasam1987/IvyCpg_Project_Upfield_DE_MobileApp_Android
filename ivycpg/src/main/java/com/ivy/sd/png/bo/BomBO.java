package com.ivy.sd.png.bo;

public class BomBO {

	private String bPid;
	private int qty, totalQty,uomID;

	public int getUomID() {
		return uomID;
	}

	public void setUomID(int uomID) {
		this.uomID = uomID;
	}

	public String getbPid() {
		return bPid;
	}

	public void setbPid(String bPid) {
		this.bPid = bPid;
	}

	public int getQty() {
		return qty;
	}

	public void setQty(int qty) {
		this.qty = qty;
	}

	public void setTotalQty(int totalQty) {
		this.totalQty = totalQty;
	}

	public int getTotalQty() {
		return totalQty;
	}

}
