package com.ivy.sd.png.bo;

public class AttendanceBO {

	private String date;

	private String atd_LCode, atd_LName, atd_LType;

	private int atd_Lid, atd_AccountID, atd_isRequired, atd_PLId, reasonid;

	public String getDate() {
		return date;
	}

	public void setDate(String date) {
		this.date = date;
	}

	public String getAtd_LCode() {
		return atd_LCode;
	}

	public void setAtd_LCode(String atd_LCode) {
		this.atd_LCode = atd_LCode;
	}

	public String getAtd_LName() {
		return atd_LName;
	}

	public void setAtd_LName(String atd_LName) {
		this.atd_LName = atd_LName;
	}

	public String getAtd_LType() {
		return atd_LType;
	}

	public void setAtd_LType(String atd_LType) {
		this.atd_LType = atd_LType;
	}

	public int getAtd_Lid() {
		return atd_Lid;
	}

	public void setAtd_Lid(int atd_Lid) {
		this.atd_Lid = atd_Lid;
	}

	public int getAtd_AccountID() {
		return atd_AccountID;
	}

	public void setAtd_AccountID(int atd_AccountID) {
		this.atd_AccountID = atd_AccountID;
	}

	public int getAtd_isRequired() {
		return atd_isRequired;
	}

	public void setAtd_isRequired(int atd_isRequired) {
		this.atd_isRequired = atd_isRequired;
	}

	public int getAtd_PLId() {
		return atd_PLId;
	}

	public void setAtd_PLId(int atd_PLId) {
		this.atd_PLId = atd_PLId;
	}

	public int getReasonid() {
		return reasonid;
	}

	public void setReasonid(int reasonid) {
		this.reasonid = reasonid;
	}

	@Override
	public String toString() {
		// TODO Auto-generated method stub
		return this.atd_LName;
	}

}
