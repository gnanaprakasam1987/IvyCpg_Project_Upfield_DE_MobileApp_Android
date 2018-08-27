package com.ivy.cpg.view.attendance;

public class AttendanceBO {

	private String date;

	private String atd_LCode, atd_LName;

	private int atd_Lid, atd_isRequired, atd_PLId, reasonid;

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


	public void setAtd_LName(String atd_LName) {
		this.atd_LName = atd_LName;
	}

	public int getAtd_Lid() {
		return atd_Lid;
	}

	public void setAtd_Lid(int atd_Lid) {
		this.atd_Lid = atd_Lid;
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
