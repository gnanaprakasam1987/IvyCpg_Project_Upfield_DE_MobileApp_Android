package com.ivy.cpg.primarysale.bo;

public class DistributorMasterBO {

	//DId,DName,CNumber,Address1,Address2,Address3,Type,TinNo
	private String DId="0",DName,CNumber,Address1,Address2,Address3,Type,TinNo;

	private String groupId;

	public String getDId() {
		return DId;
	}

	public void setDId(String DId) {
		this.DId = DId;
	}


	public String getDName() {
		return DName;
	}
	public void setDName(String DName)
	{
		this.DName=DName;
	}

	public DistributorMasterBO() {
		super();

	}

	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return DName.toString();
	}

	public DistributorMasterBO(String dId, String dName) {
		super();
		this.DId = dId;
		this.DName = dName;
	}


	public String getCNumber() {
		return CNumber;
	}

	public void setCNumber(String CNumber) {
		this.CNumber = CNumber;
	}

	public String getAddress1() {
		return Address1;
	}

	public void setAddress1(String Address1) {
		this.Address1 = Address1;
	}

	public String getAddress2() {
		return Address2;
	}

	public void setAddress2(String Address2) {
		this.Address2 = Address2;
	}

	public String getAddress3() {
		return Address3;
	}

	public void setAddress3(String Address3) {
		this.Address3 = Address3;
	}

	public String getType() {
		return Type;
	}

	public void setType(String Type) {
		this.Type = Type;
	}

	public String getTinNo() {
		return TinNo;
	}

	public void setTinNo(String TinNo) {
		this.TinNo = TinNo;
	}

	public String getGroupId() {
		return groupId;
	}

	public void setGroupId(String groupId) {
		this.groupId = groupId;
	}

	public boolean isChecked() {
		return isChecked;
	}

	public void setChecked(boolean checked) {
		isChecked = checked;
	}

	private  boolean isChecked;
}
