/**
 * 
 */
package com.ivy.sd.png.bo;

import java.util.ArrayList;

/**
 * @author gnanaprakasam.d
 * 
 */
public class SODBO {
	private int parentID;
	private int productID;
	private String productName;
	private int isOwn;
	private int actual;
	private String gap = "0";
	private float norm;
	private int MappingId;
	private ArrayList<LocationBO> locations;

	public int getParentID() {
		return parentID;
	}

	public void setParentID(int parentID) {
		this.parentID = parentID;
	}

	public int getProductID() {
		return productID;
	}

	public void setProductID(int productID) {
		this.productID = productID;
	}

	public String getProductName() {
		return productName;
	}

	public void setProductName(String productName) {
		this.productName = productName;
	}

	public int getIsOwn() {
		return isOwn;
	}

	public void setIsOwn(int isOwn) {
		this.isOwn = isOwn;
	}

	public int getActual() {
		return actual;
	}

	public void setActual(int actual) {
		this.actual = actual;
	}

	public float getNorm() {
		return norm;
	}

	public void setNorm(float norm) {
		this.norm = norm;
	}

	public String getGap() {
		return gap;
	}

	public void setGap(String gap) {
		this.gap = gap;
	}

	public int getMappingId() {
		return MappingId;
	}

	public void setMappingId(int mappingId) {
		MappingId = mappingId;
	}

	public ArrayList<LocationBO> getLocations() {
		return locations;
	}

	public void setLocations(ArrayList<LocationBO> locations) {
		this.locations = locations;
	}
}
