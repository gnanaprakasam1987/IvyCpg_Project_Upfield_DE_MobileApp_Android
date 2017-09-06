/**
 * 
 */
package com.ivy.sd.png.bo;

import java.util.List;

/**
 * @author sivakumar.j
 *
 */
public class OrderSplittingMasterBO {
	
	private String po,deliveryDate,orderId,retailerId;
	private String remarks;
	private int splitting_index;
	
	private int total_child_count=0;
	private List<OrderSplittingDetailsBO> orderSplittingDetailsBOList=null;
	private boolean checked=false;
	
	private String splittedOrderId;
	
	
	private OrderSplitMasterBO orderSplitMasterBO=null;
	
	
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
		this.po = po;
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
		this.deliveryDate = deliveryDate;
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
	 * @return the remarks
	 */
	public String getRemarks() {
		return remarks;
	}

	/**
	 * @param remarks the remarks to set
	 */
	public void setRemarks(String remarks) {
		this.remarks = remarks;
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
	 * @return the total_child_count
	 */
	public int getTotal_child_count() {
		return total_child_count;
	}

	/**
	 * @param total_child_count the total_child_count to set
	 */
	public void setTotal_child_count(int total_child_count) {
		this.total_child_count = total_child_count;
	}

	/**
	 * @return the orderSplittingDetailsBOList
	 */
	public List<OrderSplittingDetailsBO> getOrderSplittingDetailsBOList() {
		return orderSplittingDetailsBOList;
	}

	/**
	 * @param orderSplittingDetailsBOList the orderSplittingDetailsBOList to set
	 */
	public void setOrderSplittingDetailsBOList(
			List<OrderSplittingDetailsBO> orderSplittingDetailsBOList) {
		this.orderSplittingDetailsBOList = orderSplittingDetailsBOList;
	}
	
	public void addOrderSplittingDetailsBO(OrderSplittingDetailsBO ob)
	{
		if(this.orderSplittingDetailsBOList!=null) this.orderSplittingDetailsBOList.add(ob);
	}
	
	public void generateChildCount()
	{
		this.total_child_count=this.orderSplittingDetailsBOList.size();
	}

	/**
	 * @return the checked
	 */
	public boolean isChecked() {
		return checked;
	}

	/**
	 * @param checked the checked to set
	 */
	public void setChecked(boolean checked) {
		this.checked = checked;
	}
	
	public void resetBooleanInOrderSplittingDetailsBOList()
	{
		if(this.orderSplittingDetailsBOList!=null)
		{
			int size=orderSplittingDetailsBOList.size();
			if(size>0)
			{
				for(OrderSplittingDetailsBO ob1:orderSplittingDetailsBOList)
				{
					ob1.setTicked_in_dialog_check_box(false);
				}
			}
		}
	}

	/**
	 * @return the splittedOrderId
	 */
	public String getSplittedOrderId() {
		return splittedOrderId;
	}

	/**
	 * @param splittedOrderId the splittedOrderId to set
	 */
	public void setSplittedOrderId(String splittedOrderId) {
		this.splittedOrderId = splittedOrderId;
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
