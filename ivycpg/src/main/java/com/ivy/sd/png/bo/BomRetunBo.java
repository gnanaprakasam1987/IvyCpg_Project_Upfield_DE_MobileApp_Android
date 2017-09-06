package com.ivy.sd.png.bo;

import java.util.Comparator;

public class BomRetunBo {

	private String pid, barcode, productName, productShortName, TypeId,
			prodCode="";

	private int liableQty, returnQty, parentID, pieceUomId,totalReturnQty;
	private float pSrp, basePrice;

	public float getBasePrice() {
		return basePrice;
	}

	public void setBasePrice(float basePrice) {
		this.basePrice = basePrice;
	}

	public String getTypeId() {
		return TypeId;
	}

	public void setTypeId(String typeId) {
		TypeId = typeId;
	}

	public String getProductName() {
		return productName;
	}

	public void setProductName(String productName) {
		this.productName = productName;
	}

	public String getProductShortName() {
		return productShortName;
	}

	public void setProductShortName(String productShortName) {
		this.productShortName = productShortName;
	}

	public int getPieceUomId() {
		return pieceUomId;
	}

	public void setPieceUomId(int pieceUomId) {
		this.pieceUomId = pieceUomId;
	}

	public float getpSrp() {
		return pSrp;
	}

	public void setpSrp(float pSrp) {
		this.pSrp = pSrp;
	}


	public int getParentID() {
		return parentID;
	}

	public void setParentID(int parentID) {
		this.parentID = parentID;
	}

	public int getLiableQty() {
		return liableQty;
	}

	public void setLiableQty(int liableQty) {
		this.liableQty = liableQty;
	}

	public int getReturnQty() {
		return returnQty;
	}

	public void setReturnQty(int returnQty) {
		this.returnQty = returnQty;
	}

	public String getBarcode() {
		return barcode;
	}

	public void setBarcode(String barcode) {
		this.barcode = barcode;
	}

	public String getPid() {
		return pid;
	}

	public void setPid(String pid) {
		this.pid = pid;
	}

	public String getProdCode() {
		return prodCode;
	}

	public void setProdCode(String prodCode) {
		this.prodCode = prodCode;
	}

	public int getTotalReturnQty() {
		return totalReturnQty;
	}

	public void setTotalReturnQty(int totalReturnQty) {
		this.totalReturnQty = totalReturnQty;
	}

	public static final Comparator<BomRetunBo> SKUWiseAscending = new Comparator<BomRetunBo>() {

		@Override
		public int compare(BomRetunBo PM1, BomRetunBo PM2) {
			return PM1.getProductName().compareTo(PM2.getProductName());
		}
	};

}
