package com.ivy.sd.png.bo;

public class SBDMerchandisingBO {

	private int sbdid, brandid, visibilityListId, typeListId;
	private String value, listCode, listName, valueText, brandName;
	private boolean isDone;

	private String wits_flag;

	public String getWits_flag() {
		return wits_flag;
	}

	public void setWits_flag(String wits_flag) {
		this.wits_flag = wits_flag;
	}

	public int getBrandid() {
		return brandid;
	}

	public void setBrandid(int brandid) {
		this.brandid = brandid;
	}

	public int getVisibilityListId() {
		return visibilityListId;
	}

	public void setVisibilityListId(int visibilityListId) {
		this.visibilityListId = visibilityListId;
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public String getListCode() {
		return listCode;
	}

	public void setListCode(String listCode) {
		this.listCode = listCode;
	}

	public String getListName() {
		return listName;
	}

	public void setListName(String listName) {
		this.listName = listName;
	}

	public String getValueText() {
		return valueText;
	}

	public void setValueText(String valueText) {
		this.valueText = valueText;
	}

	public boolean isDone() {
		return isDone;
	}

	public void setDone(boolean isDone) {
		this.isDone = isDone;
	}

	public int getSbdid() {
		return sbdid;
	}

	public void setSbdid(int sbdid) {
		this.sbdid = sbdid;
	}

	public String getBrandName() {
		return brandName;
	}

	public void setBrandName(String brandName) {
		this.brandName = brandName;
	}

	public int getTypeListId() {
		return typeListId;
	}

	public void setTypeListId(int typeListId) {
		this.typeListId = typeListId;
	}
}
