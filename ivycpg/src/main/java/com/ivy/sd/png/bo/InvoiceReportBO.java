package com.ivy.sd.png.bo;

public class InvoiceReportBO {

	private String RetailerName, OrderID, InvoiceNumber, 
			Address, TinNumber, RetailerId;
	private int beatId, linespercall,qty=0;
	private double ordertotal,InvoiceAmount;
	private double taxValue;

	public double getDiscountValue() {
		return discountValue;
	}

	public void setDiscountValue(double discountValue) {
		this.discountValue = discountValue;
	}

	private double discountValue;



	public double getTaxValue() {
		return taxValue;
	}

	public void setTaxValue(double taxValue) {
		this.taxValue = taxValue;
	}

	public float getTotalWeight() {
		return totalWeight;
	}

	public void setTotalWeight(float totalWeight) {
		this.totalWeight = totalWeight;
	}

	private float totalWeight;
	private String dist;
	private double invoicePaidAmount;

	public String getRetailerName() {
		return RetailerName;
	}

	public void setRetailerName(String retailerName) {
		RetailerName = retailerName;
	}

	public String getOrderID() {
		return OrderID;
	}

	public void setOrderID(String orderID) {
		OrderID = orderID;
	}

	public String getInvoiceNumber() {
		return InvoiceNumber;
	}

	public void setInvoiceNumber(String invoiceNumber) {
		InvoiceNumber = invoiceNumber;
	}

	public double getInvoiceAmount() {
		return InvoiceAmount;
	}

	public void setInvoiceAmount(double invoiceAmount) {
		InvoiceAmount = invoiceAmount;
	}

	public int getBeatId() {
		return beatId;
	}

	public void setBeatId(int beatId) {
		this.beatId = beatId;
	}

	public String getRetailerAddress() {
		return Address;
	}

	public void setRetailerAddredd(String Address) {
		this.Address = Address;
	}

	public String getTinNumber() {
		return TinNumber;
	}

	public void setTinNumber(String TinNumber) {
		this.TinNumber = TinNumber;
	}

	public String getRetailerId() {
		return RetailerId;
	}

	public void setRetailerId(String RetailerId) {
		this.RetailerId = RetailerId;
	}

	public int getLinespercall() {
		return linespercall;
	}

	public void setLinespercall(int linespercall) {
		this.linespercall = linespercall;
	}

	public double getOrderTotal() {
		return ordertotal;
	}

	public void setOrderTotal(double ordertotal) {
		this.ordertotal = ordertotal;
	}

	public String getDist() {
		return dist;
	}

	public void setDist(String dist) {
		this.dist = dist;
	}

	public double getInvoicePaidAmount() {
		return invoicePaidAmount;
	}

	public void setInvoicePaidAmount(double invoicePaidAmount) {
		this.invoicePaidAmount = invoicePaidAmount;
	}
	public int getQty() {
		return qty;
	}

	public void setQty(int qty) {
		this.qty = qty;
	}
}
