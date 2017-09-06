package com.ivy.sd.png.bo;

public class OrderDetailForOrderSplit {
	private String OrderID;
	private String ProductID;
	private int Qty;
	private float Rate;
	private String batchID;
	private String priceID;

	private OrderSplitDetailsBO orderSplitDetailsBO=null;
	
	public OrderDetailForOrderSplit()
	{
		OrderID=ProductID=batchID=priceID="";
		Qty=0;
		Rate=0;
	}
	
	public String getOrderID() {
		return OrderID;
	}

	public void setOrderID(String orderID) {
		OrderID = orderID;
	}

	public String getProductID() {
		return ProductID;
	}

	public void setProductID(String productID) {
		ProductID = productID;
	}

	public int getQty() {
		return Qty;
	}

	public void setQty(int qty) {
		Qty = qty;
	}

	public float getRate() {
		return Rate;
	}

	public void setRate(float rate) {
		Rate = rate;
	}

	public String getBatchID() {
		return batchID;
	}

	public void setBatchID(String batchID) {
		this.batchID = batchID;
	}

	public String getPriceID() {
		return priceID;
	}

	public void setPriceID(String priceID) {
		this.priceID = priceID;
	}

	/**
	 * @return the orderSplitDetailsBO
	 */
	public OrderSplitDetailsBO getOrderSplitDetailsBO() {
		return orderSplitDetailsBO;
	}

	/**
	 * @param orderSplitDetailsBO the orderSplitDetailsBO to set
	 */
	public void setOrderSplitDetailsBO(OrderSplitDetailsBO orderSplitDetailsBO) {
		this.orderSplitDetailsBO = orderSplitDetailsBO;
	}
}