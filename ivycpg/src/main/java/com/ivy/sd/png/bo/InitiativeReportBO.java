package com.ivy.sd.png.bo;

import java.util.List;

public class InitiativeReportBO {

	private int initId, target, acheived;
	private String initDesc, initKeyWord;
	private int retailerid;
	private List<InitiativeReportBO> initlist;
	private String retailername, isdeviated;
	private int isdone;
	private int[] initiativeHit;

	public int getIsdone() {
		return isdone;
	}

	public void setIsdone(int isdone) {
		this.isdone = isdone;
	}

	public String getIsdeviated() {
		return isdeviated;
	}

	public void setIsdeviated(String isdeviated) {
		this.isdeviated = isdeviated;
	}

	private int sequence;

	public int getWalkingSequence() {
		return sequence;
	}

	public void setWalkingSequence(int sequence) {
		this.sequence = sequence;
	}

	public String getRetailername() {
		return retailername;
	}

	public void setRetailername(String retailername) {
		this.retailername = retailername;
	}

	public List<InitiativeReportBO> getInitlist() {
		return initlist;
	}

	public void setInitlist(List<InitiativeReportBO> list) {
		this.initlist = list;
	}

	public int getRetailerid() {
		return retailerid;
	}

	public void setRetailerid(int retailerid) {
		this.retailerid = retailerid;
	}

	public int getInitId() {
		return initId;
	}

	public void setInitId(int initId) {
		this.initId = initId;
	}

	public int getTarget() {
		return target;
	}

	public void setTarget(int target) {
		this.target = target;
	}

	public int getAcheived() {
		return acheived;
	}

	public void setAcheived(int acheived) {
		this.acheived = acheived;
	}

	public String getInitDesc() {
		return initDesc;
	}

	public void setInitDesc(String initDesc) {
		this.initDesc = initDesc;
	}

	public String getInitKeyWord() {
		return initKeyWord;
	}

	public void setInitKeyWord(String initKeyWord) {
		this.initKeyWord = initKeyWord;
	}

	public int[] getInitiativeHit() {
		return initiativeHit;
	}

	public void setInitiativeHit(int[] initiativeHit) {
		this.initiativeHit = initiativeHit;
	}

}
