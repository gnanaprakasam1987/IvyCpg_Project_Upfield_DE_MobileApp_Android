/**
 * 
 */
package com.ivy.sd.png.bo;

/**
 * @author sivakumar.j
 *
 */
public class OrderSplittingDetailsBO {
	
	private String retailerId,description,productName,productShortName;;
	private String productID,mbarcode;
	public String getMbarcode() {
		return mbarcode;
	}

	public void setMbarcode(String mbarcode) {
		this.mbarcode = mbarcode;
	}
	private int pieceqty,caseQty;
	private int splitting_index;
	
	private String orderId=null;
	private boolean ticked_in_dialog_check_box=true;
	
	private OrderSplitDetailsBO orderSplitDetailsBO=null;
	
	public OrderSplittingDetailsBO()
	{
		ticked_in_dialog_check_box=true;
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
	 * @return the description
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * @param description the description to set
	 */
	public void setDescription(String description) {
		this.description = description;
	}

	/**
	 * @return the pieceqty
	 */
	public int getPieceqty() {
		return pieceqty;
	}

	/**
	 * @param pieceqty the pieceqty to set
	 */
	public void setPieceqty(int pieceqty) {
		this.pieceqty = pieceqty;
	}

	/**
	 * @return the caseQty
	 */
	public int getCaseQty() {
		return caseQty;
	}

	/**
	 * @param caseQty the caseQty to set
	 */
	public void setCaseQty(int caseQty) {
		this.caseQty = caseQty;
	}

	/**
	 * @return the productID
	 */
	public String getProductID() {
		return productID;
	}

	/**
	 * @param productID the productID to set
	 */
	public void setProductID(String productID) {
		this.productID = productID;
	}

	/**
	 * @return the splitting_index
	 */
	public int getSplitting_index() {
		return splitting_index;
	}

	/**
	 * @param splitting_index the splitting_index to set
	 */
	public void setSplitting_index(int splitting_index) {
		this.splitting_index = splitting_index;
	}

	/**
	 * @return the orderId
	 */
	public String getOrderId() {
		return orderId;
	}

	/**
	 * @param orderId the orderId to set
	 */
	public void setOrderId(String orderId) {
		this.orderId = orderId;
	}

	/**
	 * @return the productName
	 */
	public String getProductName() {
		return productName;
	}

	/**
	 * @param productName the productName to set
	 */
	public void setProductName(String productName) {
		this.productName = productName;
	}

	/**
	 * @return the productShortName
	 */
	public String getProductShortName() {
		return productShortName;
	}

	/**
	 * @param productShortName the productShortName to set
	 */
	public void setProductShortName(String productShortName) {
		this.productShortName = productShortName;
	}

	/**
	 * @return the ticked_in_dialog_check_box
	 */
	public boolean isTicked_in_dialog_check_box() {
		return ticked_in_dialog_check_box;
	}

	/**
	 * @param ticked_in_dialog_check_box the ticked_in_dialog_check_box to set
	 */
	public void setTicked_in_dialog_check_box(boolean ticked_in_dialog_check_box) {
		this.ticked_in_dialog_check_box = ticked_in_dialog_check_box;
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
