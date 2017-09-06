/**
 * 
 */
package com.ivy.sd.png.bo;

/**
 * @author sivakumar.j
 *
 */
public class OrderSplitMasterBO {
	
	private String retailerId,retailerName,orderID,po,remark;
	private int linesPerCall=0;
	private double orderValue=0;
	private String deliveryDate;
	
	private String orderDate;
	private int route_id=0;
	private float discount=0;
	private String retailerCode;
	private String downloadedDate;
	private int free_product_count=0;
	
	private int processed;
	public final static int ORDER_PROCESSED=1;
	public final static int ORDER_NOT_PROCESSED=0;
	
	public OrderSplitMasterBO()
	{
		remark=retailerName=orderID=po=remark="";
		deliveryDate=retailerCode=downloadedDate="";
		orderDate="";
		linesPerCall=route_id=free_product_count=0;
		
		processed=ORDER_NOT_PROCESSED;
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
		
		this.retailerId =((retailerId==null)||(retailerId.length()<1))?(""):(retailerId);
		
	}

	/**
	 * @return the retailerName
	 */
	public String getRetailerName() {
		return retailerName;
	}

	/**
	 * @param retailerName the retailerName to set
	 */
	public void setRetailerName(String retailerName) {
		
		this.retailerName =((retailerName==null)||(retailerName.length()<1))?(""):(retailerName);
		
	}

	/**
	 * @return the orderID
	 */
	public String getOrderID() {
		return orderID;
	}

	/**
	 * @param orderID the orderID to set
	 */
	public void setOrderID(String orderID) {
		
		this.orderID =((orderID==null)||(orderID.length()<1))?(""):(orderID);
	}

	/**
	 * @return the po
	 */
	public String getPo() {
		return po;
	}

	/**
	 * @param po the po to set 
	 */
	public void setPo(String po) {
		
		
		this.po =((po==null)||(po.length()<1))?(""):(po);
	}

	/**
	 * @return the remark
	 */
	public String getRemark() {
		return remark;
	}

	/**
	 * @param remark the remark to set
	 */
	public void setRemark(String remark) {
		
		this.remark=((remark==null)||(remark.length()<=0))?"":remark;
		
		
	}

	/**
	 * @return the linesPerCall
	 */
	public int getLinesPerCall() {
		return linesPerCall;
	}

	/**
	 * @param linesPerCall the linesPerCall to set
	 */
	public void setLinesPerCall(int linesPerCall) {
		this.linesPerCall = linesPerCall;
	}

	/**
	 * @return the orderValue
	 */
	public double getOrderValue() {
		return orderValue;
	}

	/**
	 * @param orderValue the orderValue to set
	 */
	public void setOrderValue(double orderValue) {
		this.orderValue = orderValue;
	}

	/**
	 * @return the deliveryDate
	 */
	public String getDeliveryDate() {
		return deliveryDate;
	}

	/**
	 * @param deliveryDate the deliveryDate to set
	 */
	public void setDeliveryDate(String deliveryDate) {
		
		this.deliveryDate=((deliveryDate==null)||(deliveryDate.length()<1))?(""):(deliveryDate);
	}

	/**
	 * @return the orderDate
	 */
	public String getOrderDate() {
		return orderDate;
	}

	/**
	 * @param orderDate the orderDate to set
	 */
	public void setOrderDate(String orderDate) {
		
		this.orderDate=((orderDate==null)||(orderDate.length()<1))?(""):(orderDate);
	}

	/**
	 * @return the route_id
	 */
	public int getRoute_id() {
		return route_id;
	}

	/**
	 * @param route_id the route_id to set
	 */
	public void setRoute_id(int route_id) {
		this.route_id = route_id;
	}

	/**
	 * @return the discount
	 */
	public float getDiscount() {
		return discount;
	}

	/**
	 * @param discount the discount to set
	 */
	public void setDiscount(float discount) {
		this.discount = discount;
	}

	/**
	 * @return the retailerCode
	 */
	public String getRetailerCode() {
		return retailerCode;
	}

	/**
	 * @param retailerCode the retailerCode to set
	 */
	public void setRetailerCode(String retailerCode) {
		
		this.retailerCode=((retailerCode==null)||(retailerCode.length()<1))?(""):(retailerCode);
		
	}

	/**
	 * @return the downloadedDate
	 */
	public String getDownloadedDate() {
		return downloadedDate;
	}

	/**
	 * @param downloadedDate the downloadedDate to set
	 */
	public void setDownloadedDate(String downloadedDate) {
		
		this.downloadedDate=((downloadedDate==null)||(downloadedDate.length()<1))?(""):(downloadedDate);
	}

	/**
	 * @return the free_product_count
	 */
	public int getFree_product_count() {
		return free_product_count;
	}

	/**
	 * @param free_product_count the free_product_count to set
	 */
	public void setFree_product_count(int free_product_count) {
		this.free_product_count = free_product_count;
	}

	/**
	 * @return the processed
	 */
	public int getProcessed() {
		return processed;
	}

	/**
	 * @param processed the processed to set
	 */
	public void setProcessed(int processed) {
		this.processed = processed;
	}

}
