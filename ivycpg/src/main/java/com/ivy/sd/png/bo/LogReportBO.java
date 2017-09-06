package com.ivy.sd.png.bo;

public class LogReportBO {

	private String retailerName,inTime,outTime;
	private boolean interval;

	public boolean isInterval() {
		return interval;
	}

	public void setInterval(boolean interval) {
		this.interval = interval;
	}

	public String getOutTime() {
		return outTime;
	}

	public void setOutTime(String outTime) {
		this.outTime = outTime;
	}

	public String getInTime() {
		return inTime;
	}

	public void setInTime(String inTime) {
		this.inTime = inTime;
	}

	public String getRetailerName() {
		return retailerName;
	}

	public void setRetailerName(String retailerName) {
		this.retailerName = retailerName;
	}
}
