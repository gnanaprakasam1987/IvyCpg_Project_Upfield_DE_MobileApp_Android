package com.ivy.sd.png.bo;

public class SubDepotBo {

	private int subDepotId;

	public String getContactNumber() {
		return contactNumber;
	}

	public void setContactNumber(String contactNumber) {
		this.contactNumber = contactNumber;
	}

	private String contactNumber;
	private String dName, address1, address2, address3;

	

	public int getSubDepotId() {
		return subDepotId;
	}

	public void setSubDepotId(int subDepotId) {
		this.subDepotId = subDepotId;
	}



	public String getdName() {
		return dName;
	}

	public void setdName(String dName) {
		this.dName = dName;
	}

	public String getAddress1() {
		return address1;
	}

	public void setAddress1(String address1) {
		this.address1 = address1;
	}

	public String getAddress2() {
		return address2;
	}

	public void setAddress2(String address2) {
		this.address2 = address2;
	}

	public String getAddress3() {
		return address3;
	}

	public void setAddress3(String address3) {
		this.address3 = address3;
	}

	@Override
	public String toString() {
		return dName;
	}
}
