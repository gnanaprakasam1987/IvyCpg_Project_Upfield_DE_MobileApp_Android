package com.ivy.sd.png.bo;

import java.util.List;

public class OrderHeaderForOrderSplit {

	private double OrderValue;
	private int LinesPerCall;
	private double discount;
	private String deliveryDate;
	private String PO = "";
	private String Remark = "";
	private int totalFreeProductsCount;
	private float totalFreeProductsAmount;
	
	private String orderIdForSplit;
	private List<OrderDetailForOrderSplit> orderDetailForOrderSplitList=null;
	
	private String retailerId=null;
	private OrderSplitMasterBO orderSplitMasterBO=null;
	
	public OrderHeaderForOrderSplit()
	{
		deliveryDate=PO =Remark = orderIdForSplit=retailerId="";
		setOrderValue(discount=0);
		LinesPerCall=totalFreeProductsCount=0;
		totalFreeProductsAmount=0;
	}
	
	public int getTotalFreeProductsCount() {
		return totalFreeProductsCount;
	}

	public void setTotalFreeProductsCount(int totalFreeProductsCount) {
		this.totalFreeProductsCount = totalFreeProductsCount;
	}

	public String getPO() {
		return PO;
	}

	public void setPO(String pO) {
		PO = pO;
	}

	public String getRemark() {
		return Remark;
	}

	public void setRemark(String remark) {
		Remark = remark;
	}

	public double getOrderValue() {
		return OrderValue;
	}

	public void setOrderValue(double orderValue) {
		OrderValue = orderValue;
	}

	public int getLinesPerCall() {
		return LinesPerCall;
	}

	public void setLinesPerCall(int linesPerCall) {
		LinesPerCall = linesPerCall;
	}

	public double getDiscount() {
		return discount;
	}

	public void setDiscount(double discount) {
		this.discount = discount;
	}

	public String getDeliveryDate() {
		return deliveryDate;
	}

	public void setDeliveryDate(String deliveryDate) {
		this.deliveryDate = deliveryDate;
	}

	public float getTotalFreeProductsAmount() {
		return totalFreeProductsAmount;
	}

	public void setTotalFreeProductsAmount(float totalFreeProductsAmount) {
		this.totalFreeProductsAmount = totalFreeProductsAmount;
	}

	/**
	 * @return the orderIdForSplit
	 */
	public String getOrderIdForSplit() {
		return orderIdForSplit;
	}

	/**
	 * @param orderIdForSplit the orderIdForSplit to set
	 */
	public void setOrderIdForSplit(String orderIdForSplit) {
		this.orderIdForSplit = orderIdForSplit;
	}

	/**
	 * @return the orderDetailForOrderSplitList
	 */
	public List<OrderDetailForOrderSplit> getOrderDetailForOrderSplitList() {
		return orderDetailForOrderSplitList;
	}

	/**
	 * @param orderDetailForOrderSplitList the orderDetailForOrderSplitList to set
	 */
	public void setOrderDetailForOrderSplitList(
			List<OrderDetailForOrderSplit> orderDetailForOrderSplitList) {
		this.orderDetailForOrderSplitList = orderDetailForOrderSplitList;
	}

	/**
	 * @return the retailerId
	 */
	public String getRetailerId() {
		return retailerId;
	}

	/**
	 * @param retailerId the retailerId to set
	 */
	public void setRetailerId(String retailerId) {
		this.retailerId = retailerId;
	}

	/**
	 * @return the orderSplitMasterBO
	 */
	public OrderSplitMasterBO getOrderSplitMasterBO() {
		return orderSplitMasterBO;
	}

	/**
	 * @param orderSplitMasterBO the orderSplitMasterBO to set
	 */
	public void setOrderSplitMasterBO(OrderSplitMasterBO orderSplitMasterBO) {
		this.orderSplitMasterBO = orderSplitMasterBO;
	}

	
}
