package com.ivy.sd.png.bo;

import java.util.ArrayList;

public class BomMasterBO {

	private String pid;
	private ArrayList<BomBO> bomBO;
	private int uomID;

	
	public int getUomID() {
		return uomID;
	}

	public void setUomID(int uomID) {
		this.uomID = uomID;
	}

	public ArrayList<BomBO> getBomBO() {
		return bomBO;
	}

	public void setBomBO(ArrayList<BomBO> bomBO) {
		this.bomBO = bomBO;
	}

	public String getPid() {
		return pid;
	}

	public void setPid(String pid) {
		this.pid = pid;
	}

}
