package com.ivy.sd.png.bo;

public class DistanceRetailer implements Comparable<DistanceRetailer> {

	private double distance;
	private String RetailerName;
	private String rid;

	public double getDistance() {
		return distance;
	}

	public void setDistance(double distance) {
		this.distance = distance;
	}

	public String getRetailerName() {
		return RetailerName;
	}

	public void setRetailerName(String retailerName) {
		RetailerName = retailerName;
	}

	@Override
	public int compareTo(DistanceRetailer arg0) {
		double s = this.distance - arg0.distance;
		// String lat=String.valueOf(latitude);
		int l = (int) s;
		return l;
	}

	public String getRid() {
		return rid;
	}

	public void setRid(String rid) {
		this.rid = rid;
	}
}
