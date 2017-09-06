package com.ivy.sd.png.bo;

public class StatusSpinnerBO {

	private String id,spinnerTxt;
	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getSpinnerTxt() {
		return spinnerTxt;
	}

	public void setSpinnerTxt(String spinnerTxt) {
		this.spinnerTxt = spinnerTxt;
	}

	public StatusSpinnerBO(String id, String spinnerTxt) {
		super();
		this.id = id;
		this.spinnerTxt = spinnerTxt;
	}

	@Override
	public String toString() {
		return spinnerTxt;
	}

}
