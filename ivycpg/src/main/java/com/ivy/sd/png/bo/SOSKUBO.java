/**
 * 
 */
package com.ivy.sd.png.bo;

/**
 * @author gnanaprakasam.d
 *
 */
public class SOSKUBO {
	private int parentID;
	private int productID;
	private String productName;
	private int isOwn;
	private int parentTotal;
	private int actual;
	private String gap = "0";
	private String target = "0";
	private String percentage = "0";
	private int reasonId;
	private String imageName = "";
	private float norm;
	private int MappingId;

	public String getPercentage() {
		return percentage;
	}

	public void setPercentage(String percentage) {
		this.percentage = percentage;
	}

	public String getTarget() {
		return target;
	}

	public void setTarget(String target) {
		this.target = target;
	}

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

	public int getParentTotal() {
		return parentTotal;
	}

	public void setParentTotal(int parentTotal) {
		this.parentTotal = parentTotal;
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

	public int getReasonId() {
		return reasonId;
	}

	public void setReasonId(int reasonId) {
		this.reasonId = reasonId;
	}

	public String getImageName() {
		return imageName;
	}

	public void setImageName(String imageName) {
		this.imageName = imageName;
	}
	public int getMappingId() {
		return MappingId;
	}

	public void setMappingId(int mappingId) {
		MappingId = mappingId;
	}

	public String getImgName() {
		return imgName;
	}

	public void setImgName(String imgName) {
		this.imgName = imgName;
	}

	private String imgName = "";

}
