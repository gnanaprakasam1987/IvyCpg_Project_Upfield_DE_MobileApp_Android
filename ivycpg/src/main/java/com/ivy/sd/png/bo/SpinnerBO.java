package com.ivy.sd.png.bo;

public class SpinnerBO {

	private int id;
	private String spinnerTxt;

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getSpinnerTxt() {
		return spinnerTxt;
	}

	public void setSpinnerTxt(String spinnerTxt) {
		this.spinnerTxt = spinnerTxt;
	}

	public SpinnerBO(int id, String spinnerTxt) {
		super();
		this.id = id;
		this.spinnerTxt = spinnerTxt;
	}

	@Override
	public String toString() {
		return spinnerTxt;
	}

}
