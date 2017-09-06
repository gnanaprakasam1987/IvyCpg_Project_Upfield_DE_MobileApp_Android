/**
 * 
 */
package com.ivy.sd.png.provider;

import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;

import com.ivy.lib.existing.DBUtil;
import com.ivy.sd.png.bo.OrderDetailForOrderSplit;
import com.ivy.sd.png.bo.OrderHeaderForOrderSplit;
import com.ivy.sd.png.bo.OrderSplitDetailsBO;
import com.ivy.sd.png.bo.OrderSplitMasterBO;
import com.ivy.sd.png.bo.OrderSplittingDetailsBO;
import com.ivy.sd.png.bo.OrderSplittingMasterBO;
import com.ivy.sd.png.commons.SDUtil;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.util.Commons;
import com.ivy.sd.png.util.DataMembers;
import com.ivy.sd.png.util.DateUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * @author sivakumar.j
 * 
 */
public class OrderSplitHelper {

	private List<OrderSplitMasterBO> orderSplitMasterBOList = null; // Order
																	// split bo
																	// list
	private Context ctx;
	private BusinessModel bmodel;
	private static OrderSplitHelper instance = null;

	private List<OrderSplitDetailsBO> targetOrderSplitDetailsBOList = null;
	private List<OrderSplitDetailsBO> orderSplitDetailsBOList = null;
	private String selectedRetailerId, selectedOrderId;

	private int last_split_master_index = 0;
	private List<OrderSplitDetailsBO> currentlyTickedOrderSplitDetailsBOList = null;
	private List<OrderSplittingMasterBO> orderSplittingMasterBOList = null;

	private OrderSplitMasterBO selectedOrderSplitMasterBO = null;
	private OrderSplittingMasterBO currentlySelectedOrderSplittingMasterBO = null;
	private List<OrderSplittingMasterBO> targetOrderSplittingMasterBOList = null;
	private OrderSplittingMasterBO currentlySelectedOrderSplittingMasterBOForEdit = null;

	private List<OrderSplittingDetailsBO> needToMoveLeftSideOrderSplittingDetailsBOList = null;

	private int currently_selected_orderSplittingDetails_count_for_re_add = -1;

	private List<OrderSplittingMasterBO> targetOrderSplittingMasterBOListForSaving = null;
	private List<OrderHeaderForOrderSplit> orderHeaderForOrderSplitList = null;

	private String srcOrderId = null;

	private List<OrderSplitDetailsBO> srcOrderSplitDetailsBOList = null;

	private int currently_selected_brand_id_from_filter = -1;
	public static final int ALL = -1;
	private int currently_selected_category_id_from_filter = ALL;

	public static final int MAXIMUM_NUMBER_OF_CHILD_IN_SINGLE_ORDER_SPLIT = 10;
	private int total_future_split_count = 0;

	public boolean isOrderSplitDialogExecuted = false;
	public boolean isOrderSplitScreenExecuted = false;

	protected OrderSplitHelper(Context ctx) {
		this.ctx = ctx;
		bmodel = (BusinessModel) ctx;
	}

	/**
	 * @return instance of the OrderSplitHelper
	 */
	public static OrderSplitHelper getInstance(Context context) {
		if (instance == null) {
			instance = new OrderSplitHelper(context);
		}
		return instance;
	}

	public static OrderSplitHelper clearInstance() {
		if (instance != null)
			instance.clearAll();
		instance = null;
		return instance;
	}

	/**
	 * @return the orderSplitMasterBOList
	 */
	public List<OrderSplitMasterBO> getOrderSplitMasterBOList() {
		return orderSplitMasterBOList;
	}

	/**
	 * @param orderSplitMasterBOList
	 *            the orderSplitMasterBOList to set
	 */
	public void setOrderSplitMasterBOList(
			List<OrderSplitMasterBO> orderSplitMasterBOList) {
		this.orderSplitMasterBOList = orderSplitMasterBOList;
	}

	/**
	 * It loads the order split bo from the DB (from the table order header)
	 */
	public void loadOrderSplitMasterBOListFromDB() {
		createNewOrderSplitMasterBOList();
		DBUtil db = null;
		Cursor c = null;

		try {
			db = new DBUtil(ctx, DataMembers.DB_NAME, DataMembers.DB_PATH);
			db.openDataBase();

			// String
			// sql="SELECT * FROM OrderHeader WHERE upload='N' AND is_processed=0";
			String sql = "SELECT * FROM OrderHeader WHERE is_processed!=1";
			c = db.selectSQL(sql);
			sql = null;

			if ((c != null) && (c.getCount() > 0)) {
				// If atleas 1 row is there

				while (c.moveToNext()) {
					OrderSplitMasterBO orderSplitMasterBO = new OrderSplitMasterBO();

					orderSplitMasterBO.setLinesPerCall(c.getInt(5));
					orderSplitMasterBO.setOrderID(c.getString(0));
					orderSplitMasterBO.setOrderValue(c.getDouble(4));
					orderSplitMasterBO.setPo(c.getString(14));
					orderSplitMasterBO.setRemark(c.getString(15));
					orderSplitMasterBO.setRetailerId(c.getString(2));
					orderSplitMasterBO.setRetailerName(c.getString(12));
					orderSplitMasterBO
							.setDeliveryDate(DateUtil.convertFromServerDateToRequestedFormat(
									c.getString(9),
									bmodel.configurationMasterHelper.outDateFormat));

					try {
						orderSplitMasterBO.setDiscount(c.getFloat(8));
					} catch (Exception ex) {
						Commons.print(DataMembers.SD+
								",Exception in orderSplitMasterBO.setDiscount(c.getFloat(8)) : "
										+ ex.getMessage());
						ex.printStackTrace();
					}

					orderSplitMasterBO.setDownloadedDate(c.getString(13));

					try {
						orderSplitMasterBO.setFree_product_count(c.getInt(16));
					} catch (Exception ex) {
						Commons.print(DataMembers.SD+
								",Exception in orderSplitMasterBO.setFree_product_count(c.getInt(16)) : "
										+ ex.getMessage());
						ex.printStackTrace();
					}

					orderSplitMasterBO.setRetailerCode(c.getString(11));

					try {
						orderSplitMasterBO.setRoute_id(c.getInt(3));
					} catch (Exception ex) {
						Commons.print(DataMembers.SD+
								",Exception in orderSplitMasterBO.setRoute_id(c.getInt(3)); : "
										+ ex.getMessage());
						ex.printStackTrace();
					}
					orderSplitMasterBOList.add(orderSplitMasterBO);

				}
			}

			db.closeDB();
		} catch (Exception e) {
			Commons.printException(e);
		}
	}

	/**
	 * This method clears the order split list
	 */
	public void clearOrderSplitMasterBOList() {
		if (orderSplitMasterBOList != null)
			orderSplitMasterBOList.clear();
		orderSplitMasterBOList = null;
	}

	/**
	 * This method creates the new order split list
	 */
	public void createNewOrderSplitMasterBOList() {
		this.clearOrderSplitMasterBOList();
		orderSplitMasterBOList = new ArrayList<OrderSplitMasterBO>();
	}

	/**
	 * @return the orderSplittingDetailsBOList
	 */
	public List<OrderSplitDetailsBO> getOrderSplitDetailsBOList() {
		return this.orderSplitDetailsBOList;
	}

	/**
	 * @param orderSplittingDetailsBOList
	 *            the orderSplittingDetailsBOList to set
	 */
	public void setOrderSplitDetailsBOList(
			List<OrderSplitDetailsBO> orderSplittingDetailsBOList) {
		this.orderSplitDetailsBOList = orderSplittingDetailsBOList;
	}

	/**
	 * This method clears the order split details list
	 */
	public void clearOrderSplitDetailsBOList() {
		if (orderSplitDetailsBOList != null)
			orderSplitDetailsBOList.clear();
		this.orderSplitDetailsBOList = null;
	}

	/**
	 * This method create new order split details list
	 */
	public void createNewOrderSplitDetailsBOList() {
		this.clearOrderSplitDetailsBOList();
		orderSplitDetailsBOList = new ArrayList<OrderSplitDetailsBO>();
	}

	public void loadOrderSplitDetailsBOListFromDB(String retailerId,
			String orderId) {
		createNewOrderSplitDetailsBOList();
		DBUtil db = null;
		Cursor c = null;

		try {
			db = new DBUtil(ctx, DataMembers.DB_NAME, DataMembers.DB_PATH);
			db.openDataBase();
			String sql = "SELECT * FROM OrderDetail WHERE retailerid=\'"
					+ retailerId + "\' AND OrderID=\'" + orderId + "\'";
			c = db.selectSQL(sql);
			sql = null;
			if ((c != null) && (c.getCount() > 0)) {
				// If atleas 1 row is there

				while (c.moveToNext()) {
					OrderSplitDetailsBO orderSplittingDetailsBO = new OrderSplitDetailsBO();

					orderSplittingDetailsBO.setCaseQty(c.getInt(7));
					orderSplittingDetailsBO.setPieceqty(c.getInt(6));
					orderSplittingDetailsBO.setProductID(c.getString(1));
					orderSplittingDetailsBO.setProductName(c.getString(12));
					orderSplittingDetailsBO
							.setProductShortName(c.getString(14));
					orderSplittingDetailsBO.setRetailerId(retailerId);
					orderSplittingDetailsBO.setOrderID(orderId);
					// getTargetOrderSplittingDetailsBOList().add(orderSplittingDetailsBO);

					try {
						orderSplittingDetailsBO.setD1(c.getInt(16));
					} catch (Exception ex) {
						Commons.print(DataMembers.SD+
								",Exception in orderSplittingDetailsBO.setD1(c.getInt(16)) : "
										+ ex.getMessage());
						ex.toString();
					}

					try {
						orderSplittingDetailsBO.setD2(c.getInt(17));
					} catch (Exception ex) {
						Commons.print(DataMembers.SD+
								",Exception in orderSplittingDetailsBO.setD1(c.getInt(17)) : "
										+ ex.getMessage());
						ex.toString();
					}

					try {
						orderSplittingDetailsBO.setD3(c.getInt(18));
					} catch (Exception ex) {
						Commons.print(DataMembers.SD+
								",Exception in orderSplittingDetailsBO.setD1(c.getInt(18)) : "
										+ ex.getMessage());
						ex.toString();
					}

					try {
						orderSplittingDetailsBO.setDa(c.getInt(19));
					} catch (Exception ex) {
						Commons.print(DataMembers.SD+
								",Exception in orderSplittingDetailsBO.setD1(c.getInt(19)) : "
										+ ex.getMessage());
						ex.toString();
					}

					try {
						orderSplittingDetailsBO.setQty(c.getInt(2));
					} catch (Exception ex) {
						Commons.print(DataMembers.SD+
								",Exception in orderSplittingDetailsBO.setD1(c.getInt(2)) : "
										+ ex.getMessage());
						ex.toString();
					}

					try {
						orderSplittingDetailsBO.setUom_count(c.getInt(5));
					} catch (Exception ex) {
						Commons.print(DataMembers.SD+
								",Exception in orderSplittingDetailsBO.setD1(c.getInt(5)) : "
										+ ex.getMessage());
						ex.toString();
					}

					try {
						orderSplittingDetailsBO.setUom_id(c.getInt(8));
					} catch (Exception ex) {
						Commons.print(DataMembers.SD+
								",Exception in orderSplittingDetailsBO.setUom_id(c.getInt(8)) : "
										+ ex.getMessage());
						ex.toString();
					}

					try {
						orderSplittingDetailsBO.setRetailer_id(c.getInt(9));
					} catch (Exception ex) {
						Commons.print(DataMembers.SD+
								",Exception in orderSplittingDetailsBO.setRetailer_id(c.getInt(9)); : "
										+ ex.getMessage());
						ex.toString();
					}

					try {
						orderSplittingDetailsBO
								.setIs_free_product(c.getInt(15));
					} catch (Exception ex) {
						Commons.print(DataMembers.SD+
								",Exception in orderSplittingDetailsBO.setIs_free_product(c.getInt(15)) : "
										+ ex.getMessage());
						ex.toString();
					}

					try {
						orderSplittingDetailsBO.setSd_per(c.getDouble(24));
					} catch (Exception ex) {
						Commons.print(DataMembers.SD+
								",Exception orderSplittingDetailsBO.setSd_per(c.getDouble(24)) : "
										+ ex.getMessage());
						ex.toString();
					}

					try {
						orderSplittingDetailsBO.setSd_amt(c.getDouble(25));
					} catch (Exception ex) {
						Commons.print(DataMembers.SD+
								",ExceptionorderSplittingDetailsBO.setSd_amt(c.getDouble(25)) : "
										+ ex.getMessage());
						ex.toString();
					}

					try {
						orderSplittingDetailsBO.setSch_price(c.getDouble(26));
					} catch (Exception ex) {
						Commons.print(DataMembers.SD+
								",Exception orderSplittingDetailsBO.setSch_price(c.getDouble(26)) : "
										+ ex.getMessage());
						ex.toString();
					}

					orderSplittingDetailsBO.setMs_qty(c.getInt(10));
					orderSplittingDetailsBO.setTotal_amt(c.getFloat(11));
					orderSplittingDetailsBO.setOuter_qty(c.getInt(20));

					orderSplittingDetailsBO.setD_ouom_qty(c.getInt(21));
					orderSplittingDetailsBO.setD_ouom_qty(c.getInt(22));

					orderSplittingDetailsBO.setRate(c.getString(3));
					orderSplittingDetailsBO.setSchId(c.getString(23));

					orderSplittingDetailsBO.setpCode(c.getString(13));
					orderSplittingDetailsBO.setSo_piece(c.getInt(27));
					orderSplittingDetailsBO.setSo_case(c.getInt(28));
					this.orderSplitDetailsBOList.add(orderSplittingDetailsBO);
				}
			}
			db.closeDB();
		} catch (Exception ex) {
			ex.printStackTrace();
		}

		this.createNewTargetOrderSplitDetailsBOList();
		this.targetOrderSplitDetailsBOList.addAll(this.orderSplitDetailsBOList);
	}

	public void loadOrderSplitDetailsWithBrandIdAndCategoryIdBOListFromDB(
			String retailerId, String orderId) {
		createNewOrderSplitDetailsBOList();
		DBUtil db = null;
		Cursor c = null;

		try {
			db = new DBUtil(ctx, DataMembers.DB_NAME, DataMembers.DB_PATH);
			db.openDataBase();
			String sql = null;

			StringBuilder sbuilder = new StringBuilder();
			sbuilder.append("SELECT OD.OrderID,OD.ProductID,OD.Qty,OD.Rate,OD.upload,OD.uomcount,OD.pieceqty,OD.caseQty,");
			sbuilder.append("OD.uomid,OD.retailerid,OD.msqqty,OD.totalamount,OD.ProductName,OD.Pcode,");
			sbuilder.append("OD.ProductShortName,OD.isFreeProduct,OD.d1,OD.d2,OD.d3,OD.DA,OD.outerQty,");
			sbuilder.append("OD.dOuomQty,OD.dOuomid,OD.schID,OD.sdPer,OD.sdAmt,OD.schPrice,PM.bid,PM.cid,PM.barcode,OD.soPiece,OD.soCase");
			sbuilder.append(" FROM OrderDetail OD JOIN ProductMaster PM ON OD.ProductID=PM.PID WHERE OD.retailerid=");
			sbuilder.append("\'");
			sbuilder.append(retailerId);
			sbuilder.append("\'");
			sbuilder.append(" AND OD.OrderID=");
			sbuilder.append("\'");
			sbuilder.append(orderId);
			sbuilder.append("\'");

			sql = sbuilder.toString();
			sbuilder = null;

			c = db.selectSQL(sql);
			sql = null;
			if ((c != null) && (c.getCount() > 0)) {
				// If atleas 1 row is there

				while (c.moveToNext()) {
					OrderSplitDetailsBO orderSplittingDetailsBO = new OrderSplitDetailsBO();

					orderSplittingDetailsBO.setCaseQty(c.getInt(7));
					orderSplittingDetailsBO.setPieceqty(c.getInt(6));
					orderSplittingDetailsBO.setProductID(c.getString(1));
					orderSplittingDetailsBO.setProductName(c.getString(12));
					orderSplittingDetailsBO
							.setProductShortName(c.getString(14));
					orderSplittingDetailsBO.setRetailerId(retailerId);
					orderSplittingDetailsBO.setOrderID(orderId);
					orderSplittingDetailsBO.setMbarcode(c.getString(29));
					// getTargetOrderSplittingDetailsBOList().add(orderSplittingDetailsBO);

					try {
						orderSplittingDetailsBO.setD1(c.getInt(16));
					} catch (Exception ex) {
						Commons.print(DataMembers.SD+
								",Exception in orderSplittingDetailsBO.setD1(c.getInt(16)) : "
										+ ex.getMessage());
						ex.toString();
					}

					try {
						orderSplittingDetailsBO.setD2(c.getInt(17));
					} catch (Exception ex) {
						Commons.print(DataMembers.SD+
								",Exception in orderSplittingDetailsBO.setD1(c.getInt(17)) : "
										+ ex.getMessage());
						ex.toString();
					}

					try {
						orderSplittingDetailsBO.setD3(c.getInt(18));
					} catch (Exception ex) {
						Commons.print(DataMembers.SD+
								",Exception in orderSplittingDetailsBO.setD1(c.getInt(18)) : "
										+ ex.getMessage());
						ex.toString();
					}

					try {
						orderSplittingDetailsBO.setDa(c.getInt(19));
					} catch (Exception ex) {
						Commons.print(DataMembers.SD+
								",Exception in orderSplittingDetailsBO.setD1(c.getInt(19)) : "
										+ ex.getMessage());
						ex.toString();
					}

					try {
						orderSplittingDetailsBO.setQty(c.getInt(2));
					} catch (Exception ex) {
						Commons.print(DataMembers.SD+
								",Exception in orderSplittingDetailsBO.setD1(c.getInt(2)) : "
										+ ex.getMessage());
						ex.toString();
					}

					try {
						orderSplittingDetailsBO.setUom_count(c.getInt(5));
					} catch (Exception ex) {
						Commons.print(DataMembers.SD+
								",Exception in orderSplittingDetailsBO.setD1(c.getInt(5)) : "
										+ ex.getMessage());
						ex.toString();
					}

					try {
						orderSplittingDetailsBO.setUom_id(c.getInt(8));
					} catch (Exception ex) {
						Commons.print(DataMembers.SD+
								",Exception in orderSplittingDetailsBO.setUom_id(c.getInt(8)) : "
										+ ex.getMessage());
						ex.toString();
					}

					try {
						orderSplittingDetailsBO.setRetailer_id(c.getInt(9));
					} catch (Exception ex) {
						Commons.print(DataMembers.SD+
								",Exception in orderSplittingDetailsBO.setRetailer_id(c.getInt(9)); : "
										+ ex.getMessage());
						ex.toString();
					}

					try {
						orderSplittingDetailsBO
								.setIs_free_product(c.getInt(15));
					} catch (Exception ex) {
						Commons.print(DataMembers.SD+
								",Exception in orderSplittingDetailsBO.setIs_free_product(c.getInt(15)) : "
										+ ex.getMessage());
						ex.toString();
					}

					try {
						orderSplittingDetailsBO.setSd_per(c.getDouble(24));
					} catch (Exception ex) {
						Commons.print(DataMembers.SD+
								",Exception orderSplittingDetailsBO.setSd_per(c.getDouble(24)) : "
										+ ex.getMessage());
						ex.toString();
					}

					try {
						orderSplittingDetailsBO.setSd_amt(c.getDouble(25));
					} catch (Exception ex) {
						Commons.print(DataMembers.SD+
								",ExceptionorderSplittingDetailsBO.setSd_amt(c.getDouble(25)) : "
										+ ex.getMessage());
						ex.toString();
					}

					try {
						orderSplittingDetailsBO.setSch_price(c.getDouble(26));
					} catch (Exception ex) {
						Commons.print(DataMembers.SD+
								",Exception orderSplittingDetailsBO.setSch_price(c.getDouble(26)) : "
										+ ex.getMessage());
						ex.toString();
					}

					orderSplittingDetailsBO.setMs_qty(c.getInt(10));
					orderSplittingDetailsBO.setTotal_amt(c.getFloat(11));
					orderSplittingDetailsBO.setOuter_qty(c.getInt(20));

					orderSplittingDetailsBO.setD_ouom_qty(c.getInt(21));
					orderSplittingDetailsBO.setD_ouom_qty(c.getInt(22));

					orderSplittingDetailsBO.setRate(c.getString(3));
					orderSplittingDetailsBO.setSchId(c.getString(23));

					orderSplittingDetailsBO.setpCode(c.getString(13));

					orderSplittingDetailsBO.setTicked(false);
					orderSplittingDetailsBO.setSo_piece(c.getInt(30));
					orderSplittingDetailsBO.setSo_case(c.getInt(31));

					this.orderSplitDetailsBOList.add(orderSplittingDetailsBO);

					try {
						orderSplittingDetailsBO.setBrandId(c.getInt(27));
					} catch (Exception ex) {
						Commons.print(DataMembers.SD+
								",Exception orderSplittingDetailsBO.setBatchId(c.getInt(27)); : "
										+ ex.getMessage());
						ex.toString();
					}

					try {
						orderSplittingDetailsBO.setCategoryId(c.getInt(28));
					} catch (Exception ex) {
						Commons.print(DataMembers.SD+
								",Exception orderSplittingDetailsBO.setCategory_id(c.getInt(28)); : "
										+ ex.getMessage());
						ex.toString();
					}

				}
			}
			db.closeDB();
		} catch (Exception ex) {
			ex.printStackTrace();
		}

		this.createNewTargetOrderSplitDetailsBOList();
		this.targetOrderSplitDetailsBOList.addAll(this.orderSplitDetailsBOList);

		this.createNewSrcOrderSplitDetailsBOList();
		this.srcOrderSplitDetailsBOList.addAll(targetOrderSplitDetailsBOList);
	}

	public void clearAll() {
		this.clearOrderSplitMasterBOList();
		this.clearOrderSplitDetailsBOList();

		this.selectedOrderId = this.selectedRetailerId = null;
		this.selectedOrderSplitMasterBO = null;
		this.currentlySelectedOrderSplittingMasterBO = null;
		currentlySelectedOrderSplittingMasterBOForEdit = null;

		this.srcOrderId = null;

		this.clearTargetOrderSplittingMasterBOList();

		this.clearCurrentlyTickedOrderSplitDetailsBOList();
		this.clearOrderSplittingMasterBOList();

		clearNeedToMoveLeftSideOrderSplittingDetailsBOList();
		clearTargetOrderSplittingMasterBOListForSaving();
		this.clearOrderHeaderForOrderSplitList();

		this.clearSrcOrderSplitDetailsBOList();
	}

	/**
	 * @return the selectedRetailerId
	 */
	public String getSelectedRetailerId() {
		return selectedRetailerId;
	}

	/**
	 * @param selectedRetailerId
	 *            the selectedRetailerId to set
	 */
	public void setSelectedRetailerId(String selectedRetailerId) {
		this.selectedRetailerId = selectedRetailerId;
	}

	/**
	 * @return the selectedOrderId
	 */
	public String getSelectedOrderId() {
		return selectedOrderId;
	}

	/**
	 * @param selectedOrderId
	 *            the selectedOrderId to set
	 */
	public void setSelectedOrderId(String selectedOrderId) {
		this.selectedOrderId = selectedOrderId;
	}

	public void clearSelectedOrderIdAndRetailerId() {
		this.selectedOrderId = this.selectedRetailerId = null;
	}

	/**
	 * @return the targetOrderSplittingDetailsBOList
	 */
	public List<OrderSplitDetailsBO> getTargetOrderSplitDetailsBOList() {
		return targetOrderSplitDetailsBOList;
	}

	/**
	 * @param targetOrderSplittingDetailsBOList
	 *            the targetOrderSplittingDetailsBOList to set
	 */
	public void setTargetOrderSplitDetailsBOList(
			List<OrderSplitDetailsBO> targetOrderSplittingDetailsBOList) {
		this.targetOrderSplitDetailsBOList = targetOrderSplittingDetailsBOList;
	}

	public void clearTargetOrderSplitDetailsBOList() {
		if (this.targetOrderSplitDetailsBOList != null)
			targetOrderSplitDetailsBOList.clear();
		targetOrderSplitDetailsBOList = null;
	}

	public void createNewTargetOrderSplitDetailsBOList() {
		clearTargetOrderSplitDetailsBOList();
		targetOrderSplitDetailsBOList = new ArrayList<OrderSplitDetailsBO>();
	}

	/**
	 * @return the last_split_master_index
	 */
	public int getLast_split_master_index() {
		return last_split_master_index;
	}

	/**
	 * @param last_split_master_index
	 *            the last_split_master_index to set
	 */
	public void setLast_split_master_index(int last_split_master_index) {
		this.last_split_master_index = last_split_master_index;
	}

	/**
	 * @return the currentlyTickedOrderSplitDetailsBOList
	 */
	public List<OrderSplitDetailsBO> getCurrentlyTickedOrderSplitDetailsBOList() {
		return currentlyTickedOrderSplitDetailsBOList;
	}

	/**
	 * @param currentlyTickedOrderSplitDetailsBOList
	 *            the currentlyTickedOrderSplitDetailsBOList to set
	 */
	public void setCurrentlyTickedOrderSplitDetailsBOList(
			List<OrderSplitDetailsBO> currentlyTickedOrderSplitDetailsBOList) {
		this.currentlyTickedOrderSplitDetailsBOList = currentlyTickedOrderSplitDetailsBOList;
	}

	public void clearCurrentlyTickedOrderSplitDetailsBOList() {
		if (currentlyTickedOrderSplitDetailsBOList != null)
			currentlyTickedOrderSplitDetailsBOList.clear();
		currentlyTickedOrderSplitDetailsBOList = null;
	}

	public void createNewCurrentlyTickedOrderSplitDetailsBOList() {
		clearCurrentlyTickedOrderSplitDetailsBOList();
		currentlyTickedOrderSplitDetailsBOList = new ArrayList<OrderSplitDetailsBO>();
	}

	public void appendCurrentlyTickedOrderSplitDetailsBOList(
			OrderSplitDetailsBO ob1) {
		currentlyTickedOrderSplitDetailsBOList.add(ob1);
	}

	/**
	 * @return the orderSplittingMasterBOList
	 */
	public List<OrderSplittingMasterBO> getOrderSplittingMasterBOList() {
		return orderSplittingMasterBOList;
	}

	/**
	 * @param orderSplittingMasterBOList
	 *            the orderSplittingMasterBOList to set
	 */
	public void setOrderSplittingMasterBOList(
			List<OrderSplittingMasterBO> orderSplittingMasterBOList) {
		this.orderSplittingMasterBOList = orderSplittingMasterBOList;
	}

	public void clearOrderSplittingMasterBOList() {
		if (orderSplittingMasterBOList != null)
			orderSplittingMasterBOList.clear();
		orderSplittingMasterBOList = null;
	}

	public void createNewOrderSplittingMasterBOList() {
		clearOrderSplittingMasterBOList();
		orderSplittingMasterBOList = new ArrayList<OrderSplittingMasterBO>();
	}

	/**
	 * @return the selectedOrderSplitMasterBO
	 */
	public OrderSplitMasterBO getSelectedOrderSplitMasterBO() {
		return selectedOrderSplitMasterBO;
	}

	/**
	 * @param selectedOrderSplitMasterBO
	 *            the selectedOrderSplitMasterBO to set
	 */
	public void setSelectedOrderSplitMasterBO(
			OrderSplitMasterBO selectedOrderSplitMasterBO) {
		this.selectedOrderSplitMasterBO = selectedOrderSplitMasterBO;
	}

	/**
	 * @return the currentlyOrderSplittingMasterBO
	 */
	public OrderSplittingMasterBO getCurrentlySelectedOrderSplittingMasterBO() {
		return currentlySelectedOrderSplittingMasterBO;
	}

	/**
	 * @param currentlyOrderSplittingMasterBO
	 *            the currentlyOrderSplittingMasterBO to set
	 */
	public void setCurrentlySelectedOrderSplittingMasterBO(
			OrderSplittingMasterBO currentlyOrderSplittingMasterBO) {
		this.currentlySelectedOrderSplittingMasterBO = currentlyOrderSplittingMasterBO;
	}

	/**
	 * @return the targetOrderSplittingMasterBOList
	 */
	public List<OrderSplittingMasterBO> getTargetOrderSplittingMasterBOList() {
		return targetOrderSplittingMasterBOList;
	}

	/**
	 * @param targetOrderSplittingMasterBOList
	 *            the targetOrderSplittingMasterBOList to set
	 */
	public void setTargetOrderSplittingMasterBOList(
			List<OrderSplittingMasterBO> targetOrderSplittingMasterBOList) {
		this.targetOrderSplittingMasterBOList = targetOrderSplittingMasterBOList;
	}

	public void clearTargetOrderSplittingMasterBOList() {
		if (this.targetOrderSplittingMasterBOList != null)
			targetOrderSplittingMasterBOList.clear();
		targetOrderSplittingMasterBOList = null;
	}

	public void createNewTargetOrderSplittingMasterBOList() {
		clearTargetOrderSplittingMasterBOList();
		targetOrderSplittingMasterBOList = new ArrayList<OrderSplittingMasterBO>();
	}

	/**
	 * cretes the new targetorder splitting master bo if it does not already
	 * exist. if it is exist then it do nothing
	 */
	public void createNewIfNotExistTargetOrderSplittingMasterBOList() {
		if (targetOrderSplittingMasterBOList == null)
			targetOrderSplittingMasterBOList = new ArrayList<OrderSplittingMasterBO>();
	}

	public OrderSplittingMasterBO generateNewOrderSplittingMaster() {

		int size_of_currently_ticked_order_split_details = 0;

		size_of_currently_ticked_order_split_details = ((currentlyTickedOrderSplitDetailsBOList != null) ? (currentlyTickedOrderSplitDetailsBOList
				.size()) : (0));
		if (size_of_currently_ticked_order_split_details <= 0)
			return null;

		// int child_splitting_index=last_split_master_index;

		OrderSplittingMasterBO orderSplittingMasterBO = new OrderSplittingMasterBO();
		orderSplittingMasterBO.setDeliveryDate(selectedOrderSplitMasterBO
				.getDeliveryDate());
		orderSplittingMasterBO.setOrderId(this.selectedOrderId);
		orderSplittingMasterBO.setPo(selectedOrderSplitMasterBO.getPo());
		orderSplittingMasterBO.setRemarks(selectedOrderSplitMasterBO
				.getRemark());
		orderSplittingMasterBO.setRetailerId(selectedRetailerId);
		orderSplittingMasterBO
				.setSplitting_index(this.last_split_master_index + 1);

		int child_splitting_index = last_split_master_index + 1;
		last_split_master_index = last_split_master_index + 1;

		orderSplittingMasterBO
				.setOrderSplittingDetailsBOList(new ArrayList<OrderSplittingDetailsBO>());

		for (OrderSplitDetailsBO ob1 : currentlyTickedOrderSplitDetailsBOList) {
			OrderSplittingDetailsBO orderSplittingDetailsBO = new OrderSplittingDetailsBO();
			orderSplittingDetailsBO.setCaseQty(ob1.getCaseQty());
			orderSplittingDetailsBO.setDescription(ob1.getProductName());
			orderSplittingDetailsBO.setOrderId(selectedOrderId);
			orderSplittingDetailsBO.setPieceqty(ob1.getPieceqty());
			orderSplittingDetailsBO.setProductID(ob1.getProductID());
			orderSplittingDetailsBO.setMbarcode(ob1.getMbarcode());
			orderSplittingDetailsBO.setProductName(ob1.getProductName());
			orderSplittingDetailsBO.setProductShortName(ob1
					.getProductShortName());
			orderSplittingDetailsBO.setRetailerId(selectedRetailerId);
			orderSplittingDetailsBO.setSplitting_index(child_splitting_index);

			orderSplittingDetailsBO.setOrderSplitDetailsBO(ob1);

			orderSplittingMasterBO
					.addOrderSplittingDetailsBO(orderSplittingDetailsBO);
		}

		orderSplittingMasterBO.generateChildCount();

		orderSplittingMasterBO.setOrderSplitMasterBO(this
				.getSelectedOrderSplitMasterBO());

		return orderSplittingMasterBO;
	}

	public OrderSplittingMasterBO generateNewEmptyOrderSplittingMaster() {
		OrderSplittingMasterBO orderSplittingMasterBO = new OrderSplittingMasterBO();
		orderSplittingMasterBO.setDeliveryDate(selectedOrderSplitMasterBO
				.getDeliveryDate());
		orderSplittingMasterBO.setOrderId(this.selectedOrderId);
		orderSplittingMasterBO.setPo(selectedOrderSplitMasterBO.getPo());
		orderSplittingMasterBO.setRemarks(selectedOrderSplitMasterBO
				.getRemark());
		orderSplittingMasterBO.setRetailerId(selectedRetailerId);
		orderSplittingMasterBO
				.setSplitting_index(this.last_split_master_index + 1);

		last_split_master_index = last_split_master_index + 1;

		orderSplittingMasterBO
				.setOrderSplittingDetailsBOList(new ArrayList<OrderSplittingDetailsBO>());

		orderSplittingMasterBO.generateChildCount();

		orderSplittingMasterBO.setOrderSplitMasterBO(this
				.getSelectedOrderSplitMasterBO());

		return orderSplittingMasterBO;
	}

	public OrderSplittingMasterBO getUpdatedOrderSplittingMasterBO() {
		int size_of_currently_ticked_order_split_details = 0;

		size_of_currently_ticked_order_split_details = ((currentlyTickedOrderSplitDetailsBOList != null) ? (currentlyTickedOrderSplitDetailsBOList
				.size()) : (0));
		if (size_of_currently_ticked_order_split_details <= 0) {
			currentlySelectedOrderSplittingMasterBO
					.setOrderSplitMasterBO(selectedOrderSplitMasterBO);

			return this.currentlySelectedOrderSplittingMasterBO;
		}

		List<OrderSplittingDetailsBO> tempList = currentlySelectedOrderSplittingMasterBO
				.getOrderSplittingDetailsBOList();
		for (OrderSplitDetailsBO ob1 : currentlyTickedOrderSplitDetailsBOList) {
			OrderSplittingDetailsBO orderSplittingDetailsBO = new OrderSplittingDetailsBO();
			orderSplittingDetailsBO.setCaseQty(ob1.getCaseQty());
			orderSplittingDetailsBO.setDescription(ob1.getProductName());
			orderSplittingDetailsBO.setOrderId(selectedOrderId);
			orderSplittingDetailsBO.setPieceqty(ob1.getPieceqty());
			orderSplittingDetailsBO.setProductID(ob1.getProductID());
			orderSplittingDetailsBO.setMbarcode(ob1.getMbarcode());
			orderSplittingDetailsBO.setProductName(ob1.getProductName());
			orderSplittingDetailsBO.setProductShortName(ob1
					.getProductShortName());
			orderSplittingDetailsBO.setRetailerId(selectedRetailerId);
			orderSplittingDetailsBO
					.setSplitting_index(currentlySelectedOrderSplittingMasterBO
							.getSplitting_index());

			orderSplittingDetailsBO.setOrderSplitDetailsBO(ob1);

			tempList.add(orderSplittingDetailsBO);
		}
		currentlySelectedOrderSplittingMasterBO
				.setOrderSplittingDetailsBOList(tempList);
		currentlySelectedOrderSplittingMasterBO.generateChildCount();

		currentlySelectedOrderSplittingMasterBO
				.setOrderSplitMasterBO(selectedOrderSplitMasterBO);

		return currentlySelectedOrderSplittingMasterBO;
	}

	public void moveOrderSplitDetailsToOrderSplittingMaster() {

		// Adding the right side start
		int size_of_currently_ticked_order_split_details = 0;
		createNewIfNotExistTargetOrderSplittingMasterBOList();

		size_of_currently_ticked_order_split_details = ((currentlyTickedOrderSplitDetailsBOList != null) ? (currentlyTickedOrderSplitDetailsBOList
				.size()) : (0));
		if (size_of_currently_ticked_order_split_details <= 0)
			return;

		if (this.currentlySelectedOrderSplittingMasterBO == null) {
			Commons.print(DataMembers.SD+
					",currentlySelectedOrderSplittingMasterBO is null");

			OrderSplittingMasterBO orderSplittingMasterBO = generateNewOrderSplittingMaster();
			targetOrderSplittingMasterBOList.add(orderSplittingMasterBO);
		} else {
			Commons.print(DataMembers.SD+
					",currentlySelectedOrderSplittingMasterBO is not null");

			currentlySelectedOrderSplittingMasterBO = getUpdatedOrderSplittingMasterBO();
			int location = currentlySelectedOrderSplittingMasterBO
					.getSplitting_index() - 1;
			// Replacing the old one
			targetOrderSplittingMasterBOList.set(location,
					currentlySelectedOrderSplittingMasterBO);
		}
		// Adding the right side end

		// Deleting from left side list start
		deleteTickedItemsInTargetOrderSplitDetailsBOList();
		// Deleting from left side list end

	}

	public void deleteTickedItemsInTargetOrderSplitDetailsBOList1() {
		int size = 0;
		int ticked_size = 0;

		if (currentlyTickedOrderSplitDetailsBOList != null)
			ticked_size = currentlyTickedOrderSplitDetailsBOList.size();

		if (ticked_size <= 0)
			return;

		if (targetOrderSplitDetailsBOList != null) {
			boolean b = true;
			boolean come_out_of_for_loop = false;
			while (b) {
				come_out_of_for_loop = false;
				size = targetOrderSplitDetailsBOList.size();
				if (size > 0) {
					for (int i = 0; i < size; i++) {
						come_out_of_for_loop = false;

						OrderSplitDetailsBO orderSplitDetailsBo1 = targetOrderSplitDetailsBOList
								.get(i);
						if (orderSplitDetailsBo1.isTicked()) {
							targetOrderSplitDetailsBOList.remove(i);

							come_out_of_for_loop = true;
							break;
						}
					}

					b = come_out_of_for_loop;

					/*
					 * //Delete from src list start int index=-1; int
					 * list_size=srcOrderSplitDetailsBOList.size();
					 * 
					 * if(this.currently_selected_brand_id_from_filter!=-1) {
					 * for(int j=0;j<list_size;j++) { OrderSplitDetailsBO
					 * orderSplitDetailsBO=srcOrderSplitDetailsBOList.get(j);
					 * if(orderSplitDetailsBO.getBrandId()==this.
					 * currently_selected_brand_id_from_filter) { index++;
					 * 
					 * if(index==index_in_target_list) {
					 * srcOrderSplitDetailsBOList.remove(j); break; } } } } else
					 * { for(int j=0;j<list_size;j++) { //OrderSplitDetailsBO
					 * orderSplitDetailsBO=srcOrderSplitDetailsBOList.get(j);
					 * 
					 * index++;
					 * 
					 * if(index==index_in_target_list) {
					 * srcOrderSplitDetailsBOList.remove(j); break; }
					 * 
					 * } } //Delete from src list end
					 */

				} else {
					b = false;
				}
			}
		}
	}

	public void deleteTickedItemsInTargetOrderSplitDetailsBOList() {
		int size = 0;
		int ticked_size = 0;

		if (currentlyTickedOrderSplitDetailsBOList != null)
			ticked_size = currentlyTickedOrderSplitDetailsBOList.size();

		if (ticked_size <= 0)
			return;

		if (this.srcOrderSplitDetailsBOList != null) {
			boolean b = true;
			boolean come_out_of_for_loop = false;

			while (b) {
				come_out_of_for_loop = false;
				size = srcOrderSplitDetailsBOList.size();
				if (size > 0) {
					for (int i = 0; i < size; i++) {
						come_out_of_for_loop = false;

						OrderSplitDetailsBO orderSplitDetailsBo1 = srcOrderSplitDetailsBOList
								.get(i);
						if (orderSplitDetailsBo1.isTicked()) {
							srcOrderSplitDetailsBOList.remove(i);

							come_out_of_for_loop = true;
							break;
						}
					}

					b = come_out_of_for_loop;

					/*
					 * //Delete from src list start int index=-1; int
					 * list_size=srcOrderSplitDetailsBOList.size();
					 * 
					 * if(this.currently_selected_brand_id_from_filter!=-1) {
					 * for(int j=0;j<list_size;j++) { OrderSplitDetailsBO
					 * orderSplitDetailsBO=srcOrderSplitDetailsBOList.get(j);
					 * if(orderSplitDetailsBO.getBrandId()==this.
					 * currently_selected_brand_id_from_filter) { index++;
					 * 
					 * if(index==index_in_target_list) {
					 * srcOrderSplitDetailsBOList.remove(j); break; } } } } else
					 * { for(int j=0;j<list_size;j++) { //OrderSplitDetailsBO
					 * orderSplitDetailsBO=srcOrderSplitDetailsBOList.get(j);
					 * 
					 * index++;
					 * 
					 * if(index==index_in_target_list) {
					 * srcOrderSplitDetailsBOList.remove(j); break; }
					 * 
					 * } } //Delete from src list end
					 */

				} else {
					b = false;
				}
			}
		}

	}

	/**
	 * @return the currentlySelectedOrderSplittingMasterBOForEdit
	 */
	public OrderSplittingMasterBO getCurrentlySelectedOrderSplittingMasterBOForEdit() {
		return currentlySelectedOrderSplittingMasterBOForEdit;
	}

	/**
	 * @param currentlySelectedOrderSplittingMasterBOForEdit
	 *            the currentlySelectedOrderSplittingMasterBOForEdit to set
	 */
	public void setCurrentlySelectedOrderSplittingMasterBOForEdit(
			OrderSplittingMasterBO currentlySelectedOrderSplittingMasterBOForEdit) {
		this.currentlySelectedOrderSplittingMasterBOForEdit = currentlySelectedOrderSplittingMasterBOForEdit;
	}

	/**
	 * @return the needToMoveLeftSideOrderSplittingDetailsBO
	 */
	public List<OrderSplittingDetailsBO> getNeedToMoveLeftSideOrderSplittingDetailsBOList() {
		return needToMoveLeftSideOrderSplittingDetailsBOList;
	}

	/**
	 * @param needToMoveLeftSideOrderSplittingDetailsBO
	 *            the needToMoveLeftSideOrderSplittingDetailsBO to set
	 */
	public void setNeedToMoveLeftSideOrderSplittingDetailsBOList(
			List<OrderSplittingDetailsBO> needToMoveLeftSideOrderSplittingDetailsBO) {
		this.needToMoveLeftSideOrderSplittingDetailsBOList = needToMoveLeftSideOrderSplittingDetailsBO;
	}

	public void clearNeedToMoveLeftSideOrderSplittingDetailsBOList() {
		if (this.needToMoveLeftSideOrderSplittingDetailsBOList != null)
			needToMoveLeftSideOrderSplittingDetailsBOList.clear();
		needToMoveLeftSideOrderSplittingDetailsBOList = null;
	}

	public void createNewNeedToMoveLeftSideOrderSplittingDetailsBOList() {
		clearNeedToMoveLeftSideOrderSplittingDetailsBOList();
		this.needToMoveLeftSideOrderSplittingDetailsBOList = new ArrayList<OrderSplittingDetailsBO>();
	}

	/**
	 * @return the currently_selected_orderSplittingDetails_count_for_re_add
	 */
	public int getCurrently_selected_orderSplittingDetails_count_for_re_add() {
		return currently_selected_orderSplittingDetails_count_for_re_add;
	}

	/**
	 * @param currently_selected_orderSplittingDetails_count_for_re_add
	 *            the currently_selected_orderSplittingDetails_count_for_re_add
	 *            to set
	 */
	public void setCurrently_selected_orderSplittingDetails_count_for_re_add(
			int currently_selected_orderSplittingDetails_count_for_re_add) {
		this.currently_selected_orderSplittingDetails_count_for_re_add = currently_selected_orderSplittingDetails_count_for_re_add;
	}

	public void generateCurrently_selected_orderSplittingDetails_count_for_re_add() {
		int count = 0;
		if (currentlySelectedOrderSplittingMasterBOForEdit != null) {
			List<OrderSplittingDetailsBO> orderSplittingDetailsBOList1 = currentlySelectedOrderSplittingMasterBOForEdit
					.getOrderSplittingDetailsBOList();
			if (orderSplittingDetailsBOList1 != null) {
				int size = orderSplittingDetailsBOList1.size();
				if (size > 0) {
					for (OrderSplittingDetailsBO ob1 : orderSplittingDetailsBOList1) {

						if (ob1.isTicked_in_dialog_check_box())
							count++;
					}
				}
			}
		}

		currently_selected_orderSplittingDetails_count_for_re_add = count;

	}

	public void moveFromRightToLeft() {
		reAddToTheTargetOrderSplitDetailsBOListAndDeleteFromRightSide();
		this.setCurrentlySelectedOrderSplittingMasterBOForEdit(null);
	}

	public void reAddToTheTargetOrderSplitDetailsBOListAndDeleteFromRightSideB4AddFilter() {

		if (this.targetOrderSplitDetailsBOList == null)
			this.createNewTargetOrderSplitDetailsBOList();
		if (currentlySelectedOrderSplittingMasterBOForEdit != null) {
			List<OrderSplittingDetailsBO> orderSplittingDetailsBOList1 = currentlySelectedOrderSplittingMasterBOForEdit
					.getOrderSplittingDetailsBOList();
			if (orderSplittingDetailsBOList1 != null) {
				int size = orderSplittingDetailsBOList1.size();
				if (size > 0) {
					for (OrderSplittingDetailsBO ob1 : orderSplittingDetailsBOList1) {
						if (ob1.isTicked_in_dialog_check_box()) {
							// targetOrderSplitDetailsBOList.add(ob1);

							OrderSplitDetailsBO ob2 = generateOrderSplitDetailsFromOrderSplittingMaster(ob1);
							targetOrderSplitDetailsBOList.add(ob2);
						}
					}
				}

				// Delete from right side
				if (this.getCurrentlySelectedOrderSplittingMasterBOForEdit() != null) {
					int child_size = getCurrentlySelectedOrderSplittingMasterBOForEdit()
							.getOrderSplittingDetailsBOList().size();
					if (child_size > 0) {

						int index = 0;
						while (index < getCurrentlySelectedOrderSplittingMasterBOForEdit()
								.getOrderSplittingDetailsBOList().size()) {
							OrderSplittingDetailsBO ob3 = getCurrentlySelectedOrderSplittingMasterBOForEdit()
									.getOrderSplittingDetailsBOList()
									.get(index);
							if (ob3.isTicked_in_dialog_check_box()) {
								getCurrentlySelectedOrderSplittingMasterBOForEdit()
										.getOrderSplittingDetailsBOList()
										.remove(index);
							} else
								index++;
						}
					}
					getCurrentlySelectedOrderSplittingMasterBOForEdit()
							.generateChildCount();
				}
			}
		}
	}

	public void reAddToTheTargetOrderSplitDetailsBOListAndDeleteFromRightSide() {

		if (this.srcOrderSplitDetailsBOList == null)
			this.createNewSrcOrderSplitDetailsBOList();
		if (currentlySelectedOrderSplittingMasterBOForEdit != null) {
			List<OrderSplittingDetailsBO> orderSplittingDetailsBOList1 = currentlySelectedOrderSplittingMasterBOForEdit
					.getOrderSplittingDetailsBOList();
			if (orderSplittingDetailsBOList1 != null) {
				int size = orderSplittingDetailsBOList1.size();
				if (size > 0) {
					for (OrderSplittingDetailsBO ob1 : orderSplittingDetailsBOList1) {
						if (ob1.isTicked_in_dialog_check_box()) {
							// targetOrderSplitDetailsBOList.add(ob1);

							OrderSplitDetailsBO ob2 = generateOrderSplitDetailsFromOrderSplittingMaster(ob1);
							srcOrderSplitDetailsBOList.add(ob2);
						}
					}
				}

				// Delete from right side
				if (this.getCurrentlySelectedOrderSplittingMasterBOForEdit() != null) {
					int child_size = getCurrentlySelectedOrderSplittingMasterBOForEdit()
							.getOrderSplittingDetailsBOList().size();
					if (child_size > 0) {

						int index = 0;
						while (index < getCurrentlySelectedOrderSplittingMasterBOForEdit()
								.getOrderSplittingDetailsBOList().size()) {
							OrderSplittingDetailsBO ob3 = getCurrentlySelectedOrderSplittingMasterBOForEdit()
									.getOrderSplittingDetailsBOList()
									.get(index);
							if (ob3.isTicked_in_dialog_check_box()) {
								getCurrentlySelectedOrderSplittingMasterBOForEdit()
										.getOrderSplittingDetailsBOList()
										.remove(index);
							} else
								index++;
						}
					}
					getCurrentlySelectedOrderSplittingMasterBOForEdit()
							.generateChildCount();
				}
			}
		}
	}

	public OrderSplitDetailsBO generateOrderSplitDetailsFromOrderSplittingMaster(
			OrderSplittingDetailsBO ob) {
		OrderSplitDetailsBO orderSplitDetailsBO2 = new OrderSplitDetailsBO();
		orderSplitDetailsBO2.setCaseQty(ob.getCaseQty());
		orderSplitDetailsBO2.setOrderID(ob.getOrderId());
		orderSplitDetailsBO2.setPieceqty(ob.getPieceqty());
		orderSplitDetailsBO2.setProductID(ob.getProductID());
		orderSplitDetailsBO2.setProductName(ob.getProductName());
		orderSplitDetailsBO2.setProductShortName(ob.getProductShortName());
		orderSplitDetailsBO2.setRetailerId(ob.getRetailerId());
		orderSplitDetailsBO2.setTicked(false);
		orderSplitDetailsBO2.setMbarcode(ob.getMbarcode());
		orderSplitDetailsBO2.setD1(ob.getOrderSplitDetailsBO().getD1());
		orderSplitDetailsBO2.setD2(ob.getOrderSplitDetailsBO().getD2());
		orderSplitDetailsBO2.setD3(ob.getOrderSplitDetailsBO().getD3());
		orderSplitDetailsBO2.setDa(ob.getOrderSplitDetailsBO().getDa());

		orderSplitDetailsBO2.setD_ouom_id(ob.getOrderSplitDetailsBO()
				.getD_ouom_id());
		orderSplitDetailsBO2.setD_ouom_qty(ob.getOrderSplitDetailsBO()
				.getD_ouom_qty());
		orderSplitDetailsBO2.setIs_free_product(ob.getOrderSplitDetailsBO()
				.getIs_free_product());
		orderSplitDetailsBO2.setMs_qty(ob.getOrderSplitDetailsBO().getMs_qty());
		orderSplitDetailsBO2.setOuter_qty(ob.getOrderSplitDetailsBO()
				.getOuter_qty());
		orderSplitDetailsBO2.setpCode(ob.getOrderSplitDetailsBO().getpCode());
		orderSplitDetailsBO2.setQty(ob.getOrderSplitDetailsBO().getQty());
		orderSplitDetailsBO2.setRate(ob.getOrderSplitDetailsBO().getRate());
		orderSplitDetailsBO2
				.setRetailer_id(Integer.parseInt(ob.getRetailerId()));
		orderSplitDetailsBO2.setSch_price(ob.getOrderSplitDetailsBO()
				.getSch_price());
		orderSplitDetailsBO2.setSchId(ob.getOrderSplitDetailsBO().getSchId());
		orderSplitDetailsBO2.setSd_amt(ob.getOrderSplitDetailsBO().getSd_amt());
		orderSplitDetailsBO2.setSd_per(ob.getOrderSplitDetailsBO().getSd_per());
		orderSplitDetailsBO2.setUom_count(ob.getOrderSplitDetailsBO()
				.getUom_count());
		orderSplitDetailsBO2.setUom_id(ob.getOrderSplitDetailsBO().getUom_id());
		orderSplitDetailsBO2.setSo_piece(ob.getOrderSplitDetailsBO()
				.getSo_piece());
		orderSplitDetailsBO2.setSo_case(ob.getOrderSplitDetailsBO()
				.getSo_case());
		orderSplitDetailsBO2.setTotal_amt(ob.getOrderSplitDetailsBO()
				.getTotal_amt());

		return orderSplitDetailsBO2;
	}

	public void reSetCurrentlySelectedOrderSplittingMasterBOForEditChildFlag() {
		int size = getCurrentlySelectedOrderSplittingMasterBOForEdit()
				.getOrderSplittingDetailsBOList().size();
		if (size > 0) {
			List<OrderSplittingDetailsBO> ob1 = getCurrentlySelectedOrderSplittingMasterBOForEdit()
					.getOrderSplittingDetailsBOList();
			for (OrderSplittingDetailsBO ob2 : ob1) {
				ob2.setTicked_in_dialog_check_box(false);
			}
		}
	}

	public void saveOrder(String retailerId) {
		// deleteTheSrcOrderDetailsForOrderSplit(uid); //Deleting src order
		generateTargetOrderSplittingMasterBOListForSaving();
		createNewOrderHeaderForOrderSplitList();

		generateHeadersAndDetailsForSavingSplittedOrder(retailerId);
		generateTotalOrderValues();

		boolean is_uploaded = this.isUploaded(this.srcOrderId);
		this.deleteTheSrcOrderDetailsForOrderSplit(this.srcOrderId); // Deleting
																		// sourc
																		// records
																		// in
																		// order
																		// header
																		// &
																		// order
																		// details

		saveHeaderAndDetailsForSplittedOrder(retailerId);

		if (is_uploaded)
			this.insertSplittedOrder(retailerId, this.srcOrderId);
	}

	public void saveHeaderAndDetailsForSplittedOrder(String retailerId) {
		DBUtil db = new DBUtil(ctx, DataMembers.DB_NAME, DataMembers.DB_PATH);
		db.createDataBase();
		db.openDataBase();
		// For Malaysian User is_Process is 1 and IS is_process 0, it will mot
		// affect the already working malaysian users
		int isProcess = 0;
		if (bmodel.configurationMasterHelper.SHOW_ORDER_PROCESS_DIALOG)
			isProcess = 0;
		else
			isProcess = 1;
		int order_header_size = this.orderHeaderForOrderSplitList.size();
		if (order_header_size > 0) {
			for (OrderHeaderForOrderSplit orderHeaderForOrderSplit : orderHeaderForOrderSplitList) {
				String columns = "orderid,orderdate,retailerid,ordervalue,RouteId,linespercall,discount,"
						+ "deliveryDate,isToday,retailerCode,retailerName,downloadDate,po,remark,is_splitted_order,is_processed";
				// /*
				String values = orderHeaderForOrderSplit.getOrderIdForSplit()
						+ ","
						+ bmodel.QT(SDUtil.now(SDUtil.DATE_GLOBAL))
						+ ","
						+ bmodel.QT(retailerId)
						+ ","
						+ bmodel.QT(SDUtil.format(
								orderHeaderForOrderSplit.getOrderValue(), 2, 0)
								+ "")
						+ ","
						+ orderHeaderForOrderSplit.getOrderSplitMasterBO()
								.getRoute_id() // +
												// getRetailerMasterBO().getBeatID()
						+ ","
						+ orderHeaderForOrderSplit
								.getOrderDetailForOrderSplitList().size()
						+ ","
						+ orderHeaderForOrderSplit.getOrderSplitMasterBO()
								.getDiscount() // + orderHeaderBO.getDiscount()
						+ ","
						+ bmodel.QT(DateUtil.convertToServerDateFormat(
								orderHeaderForOrderSplit.getDeliveryDate(),
								bmodel.configurationMasterHelper.outDateFormat))
						+ ","
						+ "0" // + (getRetailerMasterBO().getIsToday())
						+ ","
						+ DatabaseUtils
								.sqlEscapeString(orderHeaderForOrderSplit
										.getOrderSplitMasterBO()
										.getRetailerCode()) // +
															// DatabaseUtils.sqlEscapeString(getRetailerMasterBO().getRetailerCode())
						+ ","
						+ DatabaseUtils
								.sqlEscapeString(orderHeaderForOrderSplit
										.getOrderSplitMasterBO()
										.getRetailerName()) // +
															// DatabaseUtils.sqlEscapeString(getRetailerMasterBO().getRetailerName())
						+ ","
						+ bmodel.QT(bmodel.userMasterHelper.getUserMasterBO()
								.getDownloadDate()) + ","
						+ bmodel.QT(orderHeaderForOrderSplit.getPO()) + ","
						+ bmodel.QT(orderHeaderForOrderSplit.getRemark()) + ","
						+ "1" // For splitted order flag
						+ "," + isProcess;

				db.insertSQL(DataMembers.tbl_orderHeader, columns, values);

				// Saving details start
				List<OrderDetailForOrderSplit> orderDetailForOrderSplitList = orderHeaderForOrderSplit
						.getOrderDetailForOrderSplitList();
				for (OrderDetailForOrderSplit orderDetailForOrderSplit : orderDetailForOrderSplitList) {
					columns = "orderid,productid,qty,rate,uomcount,pieceqty,caseqty,uomid,retailerid, msqqty, totalamount,ProductName,ProductshortName,pcode, isFreeProduct,D1,D2,D3,DA,outerQty,dOuomQty,dOuomid,schPrice,schID,sdPer,sdAmt,soPiece,soCase,OrderType";
					values = orderHeaderForOrderSplit.getOrderIdForSplit()
							+ ","
							+ bmodel.QT(orderDetailForOrderSplit.getProductID()) // +
																					// bmodel.QT(product.getProductID())
							+ ","
							+ orderDetailForOrderSplit.getOrderSplitDetailsBO()
									.getQty()// + pieceCount
							+ ","
							+ bmodel.QT(orderDetailForOrderSplit
									.getOrderSplitDetailsBO().getRate()) // +
																			// product.getSrp()

							+ ","
							+ orderDetailForOrderSplit.getOrderSplitDetailsBO()
									.getUom_count() // + product.getCaseSize()
							+ ","
							+ orderDetailForOrderSplit.getOrderSplitDetailsBO()
									.getPieceqty() // +
													// product.getOrderedPcsQty()
							+ ","
							+ orderDetailForOrderSplit.getOrderSplitDetailsBO()
									.getCaseQty() // +
													// product.getOrderedCaseQty()
							+ ","
							+ orderDetailForOrderSplit.getOrderSplitDetailsBO()
									.getUom_id() // + product.getOuUomId()
							+ ","
							+ retailerId // +
											// bmodel.QT(getRetailerMasterBO().getRetailerID())
							+ ", "
							+ orderDetailForOrderSplit.getOrderSplitDetailsBO()
									.getMs_qty() // + product.getMSQty()
							+ ","
							+ orderDetailForOrderSplit.getOrderSplitDetailsBO()
									.getTotal_amt() // +
													// product.getDiscount_order_value()
							+ ","
							+ DatabaseUtils
									.sqlEscapeString(orderDetailForOrderSplit
											.getOrderSplitDetailsBO()
											.getProductName()) // +
																// DatabaseUtils.sqlEscapeString(product.getProductName())
							+ ","
							+ DatabaseUtils
									.sqlEscapeString(orderDetailForOrderSplit
											.getOrderSplitDetailsBO()
											.getProductShortName()) // +
																	// DatabaseUtils.sqlEscapeString(product.getProductShortName())
							+ ","
							+ DatabaseUtils
									.sqlEscapeString(orderDetailForOrderSplit
											.getOrderSplitDetailsBO()
											.getpCode()) // +
															// DatabaseUtils.sqlEscapeString(product.getProductCode())
							+ ","
							+ 0
							+ ","
							+ orderDetailForOrderSplit.getOrderSplitDetailsBO()
									.getD1() // + product.getD1()
							+ ","
							+ orderDetailForOrderSplit.getOrderSplitDetailsBO()
									.getD2() // + product.getD2()
							+ ","
							+ orderDetailForOrderSplit.getOrderSplitDetailsBO()
									.getD3() // + product.getD3()
							+ ","
							+ orderDetailForOrderSplit.getOrderSplitDetailsBO()
									.getDa() // + product.getDA()
							+ ","
							+ orderDetailForOrderSplit.getOrderSplitDetailsBO()
									.getOuter_qty() // +
													// product.getOrderedOuterQty()
							+ ","
							+ orderDetailForOrderSplit.getOrderSplitDetailsBO()
									.getD_ouom_qty() // + product.getOutersize()
							+ ","
							+ orderDetailForOrderSplit.getOrderSplitDetailsBO()
									.getD_ouom_id() // + product.getDouomid()
							+ ","
							+ orderDetailForOrderSplit.getOrderSplitDetailsBO()
									.getSch_price() // + piecePrice
							+ ","
							+ bmodel.QT(orderDetailForOrderSplit
									.getOrderSplitDetailsBO().getSchId()) // +
																			// QT(schemeID)
							+ ","
							+ orderDetailForOrderSplit.getOrderSplitDetailsBO()
									.getSd_per() // + discountPercent
							+ ","
							+ orderDetailForOrderSplit.getOrderSplitDetailsBO()
									.getSd_amt() // + discountAmount;
							+ ","
							+ orderDetailForOrderSplit.getOrderSplitDetailsBO()
									.getSo_piece() // Suggested Order Piece
							+ ","
							+ orderDetailForOrderSplit.getOrderSplitDetailsBO()
									.getSo_case() // Suggested Order Case
							+ "," + 0; // order type
					db.insertSQL(DataMembers.tbl_orderDetails, columns, values);

				}
				// Saving details end

				// */
			}
		}

		db.closeDB();
	}

	public void generateTotalOrderValues() {
		int order_header_size = this.orderHeaderForOrderSplitList.size();
		if (order_header_size > 0) {
			for (OrderHeaderForOrderSplit orderHeaderForOrderSplit : orderHeaderForOrderSplitList) {
				double order_value = 0;

				List<OrderDetailForOrderSplit> orderDetailForOrderSplitList = orderHeaderForOrderSplit
						.getOrderDetailForOrderSplitList();
				for (OrderDetailForOrderSplit orderDetailForOrderSplit : orderDetailForOrderSplitList) {
					order_value = (double) (((double) order_value) + ((double) orderDetailForOrderSplit
							.getOrderSplitDetailsBO().getTotal_amt()));
				}
				orderHeaderForOrderSplit.setOrderValue(order_value);
			}
		}
	}

	public void generateHeadersAndDetailsForSavingSplittedOrder(
			String retailerId) {

		if (this.targetOrderSplittingMasterBOListForSaving != null) {
			int size = targetOrderSplittingMasterBOListForSaving.size();
			if (size > 0) {
				// Saving order
				for (OrderSplittingMasterBO orderSplittingMasterBO : targetOrderSplittingMasterBOListForSaving) {
					String splittedOrderId = generateNewOrderId();
					orderSplittingMasterBO.setSplittedOrderId(splittedOrderId);

					OrderHeaderForOrderSplit orderHeaderForOrderSplit = generateNewOrderHeaderForOrderSplit(splittedOrderId);

					orderHeaderForOrderSplit
							.setDeliveryDate(orderSplittingMasterBO
									.getDeliveryDate());
					orderHeaderForOrderSplit.setPO(orderSplittingMasterBO
							.getPo());
					orderHeaderForOrderSplit.setRemark(orderSplittingMasterBO
							.getRemarks());
					orderHeaderForOrderSplit.setRetailerId(retailerId);

					orderHeaderForOrderSplit
							.setOrderSplitMasterBO(orderSplittingMasterBO
									.getOrderSplitMasterBO());

					List<OrderSplittingDetailsBO> orderSplittingDetailsBOList = orderSplittingMasterBO
							.getOrderSplittingDetailsBOList();

					List<OrderDetailForOrderSplit> orderDetailForOrderSplitList = new ArrayList<OrderDetailForOrderSplit>();

					for (OrderSplittingDetailsBO orderSplittingDetailsBO : orderSplittingDetailsBOList) {
						OrderDetailForOrderSplit orderDetailForOrderSplit = new OrderDetailForOrderSplit();
						orderDetailForOrderSplit.setOrderID(splittedOrderId);

						orderDetailForOrderSplit
								.setProductID(orderSplittingDetailsBO
										.getProductID());
						// orderDetailForOrderSplit.setRate(rate)

						orderDetailForOrderSplit
								.setOrderSplitDetailsBO(orderSplittingDetailsBO
										.getOrderSplitDetailsBO());
						orderDetailForOrderSplitList
								.add(orderDetailForOrderSplit);
					}

					orderHeaderForOrderSplit
							.setOrderDetailForOrderSplitList(orderDetailForOrderSplitList);

					this.orderHeaderForOrderSplitList
							.add(orderHeaderForOrderSplit);
				}

			}
		}

	}

	public String generateNewOrderId() {
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			Commons.printException(e);
		}
		return (bmodel.QT(bmodel.userMasterHelper.getUserMasterBO().getUserid()
				+ SDUtil.now(SDUtil.DATE_TIME_ID)));
	}

	public OrderHeaderForOrderSplit generateNewOrderHeaderForOrderSplit(
			String orderId) {
		OrderHeaderForOrderSplit orderHeaderForOrderSplit = new OrderHeaderForOrderSplit();
		orderHeaderForOrderSplit.setOrderIdForSplit(orderId);

		return orderHeaderForOrderSplit;
	}

	/**
	 * Check weather order is placed for the particular retailer and its't sync
	 * yet or not.
	 * 
	 * @param retailerId
	 * @return true|false
	 */
	public boolean hasAlreadyOrdered(String retailerId) {
		try {
			DBUtil db = new DBUtil(ctx, DataMembers.DB_NAME,
					DataMembers.DB_PATH);
			db.createDataBase();
			db.openDataBase();
			// Order Header
			String sql = "select OrderID from " + DataMembers.tbl_orderHeader
					+ " where upload='N' and RetailerID="
					+ bmodel.QT(retailerId) + " and invoiceStatus=0";
			Cursor orderHeaderCursor = db.selectSQL(sql);
			if (orderHeaderCursor.getCount() > 0) {
				orderHeaderCursor.close();
				db.closeDB();
				return true;
			} else {
				orderHeaderCursor.close();
				db.closeDB();
				return false;
			}
		} catch (Exception e) {
			// TODO: handle exception
			Commons.printException(e);
			return false;
		}
	}

	public void deleteTheSrcOrderDetailsForOrderSplit(String uid) {
		DBUtil db = new DBUtil(ctx, DataMembers.DB_NAME, DataMembers.DB_PATH);
		db.createDataBase();
		db.openDataBase();

		db.deleteSQL("OrderHeader", "OrderID=" + uid, false);
		db.deleteSQL("OrderDetail", "OrderID=" + uid, false);

		db.closeDB();
	}

	/**
	 * @return the targetOrderSplittingMasterBOListForSaving
	 */
	public List<OrderSplittingMasterBO> getTargetOrderSplittingMasterBOListForSaving() {
		return targetOrderSplittingMasterBOListForSaving;
	}

	/**
	 * @param targetOrderSplittingMasterBOListForSaving
	 *            the targetOrderSplittingMasterBOListForSaving to set
	 */
	public void setTargetOrderSplittingMasterBOListForSaving(
			List<OrderSplittingMasterBO> targetOrderSplittingMasterBOListForSaving) {
		this.targetOrderSplittingMasterBOListForSaving = targetOrderSplittingMasterBOListForSaving;
	}

	public void clearTargetOrderSplittingMasterBOListForSaving() {

		targetOrderSplittingMasterBOListForSaving = null;
	}

	public void generateTargetOrderSplittingMasterBOListForSaving() {
		clearTargetOrderSplittingMasterBOListForSaving();
		if (targetOrderSplittingMasterBOList != null) {
			int size = targetOrderSplittingMasterBOList.size();
			if (size > 0) {
				targetOrderSplittingMasterBOListForSaving = targetOrderSplittingMasterBOList;

				int index = 0;
				while ((index >= 0)
						&& (index < targetOrderSplittingMasterBOListForSaving
								.size())) {
					OrderSplittingMasterBO orderSplittingMasterBO = targetOrderSplittingMasterBOListForSaving
							.get(index);
					if (orderSplittingMasterBO.getTotal_child_count() < 1) {
						targetOrderSplittingMasterBOListForSaving.remove(index);
					} else
						index++;
				}
			}
		}
	}

	/**
	 * @return the orderHeaderForOrderSplitList
	 */
	public List<OrderHeaderForOrderSplit> getOrderHeaderForOrderSplitList() {
		return orderHeaderForOrderSplitList;
	}

	/**
	 * @param orderHeaderForOrderSplitList
	 *            the orderHeaderForOrderSplitList to set
	 */
	public void setOrderHeaderForOrderSplitList(
			List<OrderHeaderForOrderSplit> orderHeaderForOrderSplitList) {
		this.orderHeaderForOrderSplitList = orderHeaderForOrderSplitList;
	}

	public void clearOrderHeaderForOrderSplitList() {
		if (orderHeaderForOrderSplitList != null)
			orderHeaderForOrderSplitList.clear();
		orderHeaderForOrderSplitList = null;
	}

	public void createNewOrderHeaderForOrderSplitList() {
		clearOrderHeaderForOrderSplitList();
		this.orderHeaderForOrderSplitList = new ArrayList<OrderHeaderForOrderSplit>();
	}

	/**
	 * @return the srcOrderId
	 */
	public String getSrcOrderId() {
		return srcOrderId;
	}

	/**
	 * @param srcOrderId
	 *            the srcOrderId to set
	 */
	public void setSrcOrderId(String srcOrderId) {
		this.srcOrderId = srcOrderId;
	}

	public boolean isAnyOrderTickedForSplit() {
		List<OrderSplittingDetailsBO> orderSplittingDetailsBOList1 = currentlySelectedOrderSplittingMasterBOForEdit
				.getOrderSplittingDetailsBOList();
		if ((orderSplittingDetailsBOList1 != null)
				&& (orderSplittingDetailsBOList1.size() > 0)) {
			for (OrderSplittingDetailsBO ob1 : orderSplittingDetailsBOList1) {
				if (ob1.isTicked_in_dialog_check_box() == false) {
					// targetOrderSplitDetailsBOList.add(ob1);

					return true;

				}
			}
		}

		return false;
	}

	public boolean isThereAnyOrderToSplit() {
		createNewOrderSplitMasterBOList();
		DBUtil db = null;
		Cursor c = null;

		int count = 0;
		boolean result = false;

		try {
			db = new DBUtil(ctx, DataMembers.DB_NAME, DataMembers.DB_PATH);
			db.openDataBase();

			String sql = "SELECT count(*) FROM OrderHeader";
			c = db.selectSQL(sql);
			sql = null;

			if ((c != null) && (c.getCount() > 0)) {
				// If atleas 1 row is there

				c.moveToNext();
				count = c.getInt(0);
			}

			db.closeDB();
		} catch (Exception e) {
			Commons.printException(e);
		}

		result = (count < 1) ? (false) : (true);
		return result;
	}

	/**
	 * @return the srcOrderSplitDetailsBOList
	 */
	public List<OrderSplitDetailsBO> getSrcOrderSplitDetailsBOList() {
		return srcOrderSplitDetailsBOList;
	}

	/**
	 * @param srcOrderSplitDetailsBOList
	 *            the srcOrderSplitDetailsBOList to set
	 */
	public void setSrcOrderSplitDetailsBOList(
			List<OrderSplitDetailsBO> srcOrderSplitDetailsBOList) {
		this.srcOrderSplitDetailsBOList = srcOrderSplitDetailsBOList;
	}

	public void clearSrcOrderSplitDetailsBOList() {
		if (this.srcOrderSplitDetailsBOList != null)
			srcOrderSplitDetailsBOList.clear();
		srcOrderSplitDetailsBOList = null;
	}

	public void createNewSrcOrderSplitDetailsBOList() {
		clearSrcOrderSplitDetailsBOList();
		srcOrderSplitDetailsBOList = new ArrayList<OrderSplitDetailsBO>();
	}

	public void generateTargetOrderSplitDetailsBOList(int category_id,
			int brand_id) {
		this.createNewTargetOrderSplitDetailsBOList();
		if (this.srcOrderSplitDetailsBOList != null) {
			int size = this.srcOrderSplitDetailsBOList.size();
			if (size > 0) {
				for (OrderSplitDetailsBO orderSplitDetailsBO : srcOrderSplitDetailsBOList) {
					if ((orderSplitDetailsBO.getBrandId() == brand_id)
							&& (orderSplitDetailsBO.getCategoryId() == category_id))
						this.targetOrderSplitDetailsBOList
								.add(orderSplitDetailsBO);
				}
			}
		}
	}

	public void generateTargetOrderSplitDetailsBOListForCategoryId(
			int category_id) {
		this.createNewTargetOrderSplitDetailsBOList();

		if (category_id == -1) {
			targetOrderSplitDetailsBOList.addAll(srcOrderSplitDetailsBOList);
			return;
		}

		if (this.srcOrderSplitDetailsBOList != null) {
			int size = this.srcOrderSplitDetailsBOList.size();
			if (size > 0) {
				for (OrderSplitDetailsBO orderSplitDetailsBO : srcOrderSplitDetailsBOList) {
					if (orderSplitDetailsBO.getCategoryId() == category_id)
						this.targetOrderSplitDetailsBOList
								.add(orderSplitDetailsBO);
				}
			}
		}
	}

	public void generateTargetOrderSplitDetailsBOListForBrandId(int brand_id) {
		this.createNewTargetOrderSplitDetailsBOList();

		if (brand_id == -1) {
			targetOrderSplitDetailsBOList.addAll(srcOrderSplitDetailsBOList);
			return;
		}

		if (this.srcOrderSplitDetailsBOList != null) {
			int size = this.srcOrderSplitDetailsBOList.size();
			if (size > 0) {
				for (OrderSplitDetailsBO orderSplitDetailsBO : srcOrderSplitDetailsBOList) {
					if (orderSplitDetailsBO.getBrandId() == brand_id)
						this.targetOrderSplitDetailsBOList
								.add(orderSplitDetailsBO);
				}
			}
		}
	}

	public void generateTargetOrderSplitDetailsBOListForBrandId() {
		generateTargetOrderSplitDetailsBOListForBrandId(this.currently_selected_brand_id_from_filter);
	}

	/**
	 * @return the currently_selected_brand_id_from_filter
	 */
	public int getCurrently_selected_brand_id_from_filter() {
		return currently_selected_brand_id_from_filter;
	}

	/**
	 * @param currently_selected_brand_id_from_filter
	 *            the currently_selected_brand_id_from_filter to set
	 */
	public void setCurrently_selected_brand_id_from_filter(
			int currently_selected_brand_id_from_filter) {
		this.currently_selected_brand_id_from_filter = currently_selected_brand_id_from_filter;
	}

	public void addNewOrderSplittingMasterInList() {
		OrderSplittingMasterBO orderSplittingMasterBO = this
				.generateNewEmptyOrderSplittingMaster();
		targetOrderSplittingMasterBOList.add(orderSplittingMasterBO);
	}

	/**
	 * @return the currently_selected_category_id_from_filter
	 */
	public int getCurrently_selected_category_id_from_filter() {
		return currently_selected_category_id_from_filter;
	}

	/**
	 * @param currently_selected_category_id_from_filter
	 *            the currently_selected_category_id_from_filter to set
	 */
	public void setCurrently_selected_category_id_from_filter(
			int currently_selected_category_id_from_filter) {
		this.currently_selected_category_id_from_filter = currently_selected_category_id_from_filter;
	}

	public void resetAllSrcOrderSplitDetailsBOList() {
		if (this.srcOrderSplitDetailsBOList != null) {
			int size = this.srcOrderSplitDetailsBOList.size();
			if (size > 0) {
				for (OrderSplitDetailsBO orderSplitDetailsBO : srcOrderSplitDetailsBOList) {
					orderSplitDetailsBO.setTicked(false);
				}
			}
		}
	}

	public void selectAllInTargetOrderSplitDetailsBOList() {
		if (this.targetOrderSplitDetailsBOList != null) {
			int size = this.targetOrderSplitDetailsBOList.size();
			if (size > 0) {
				for (OrderSplitDetailsBO orderSplitDetailsBO : targetOrderSplitDetailsBOList) {
					orderSplitDetailsBO.setTicked(true);
				}
			}
		}
	}

	public void loadOrderSplitMasterBOListFromDBForSync() {
		createNewOrderSplitMasterBOList();
		DBUtil db = null;
		Cursor c = null;

		try {
			db = new DBUtil(ctx, DataMembers.DB_NAME, DataMembers.DB_PATH);
			db.openDataBase();

			// String sql="SELECT * FROM OrderHeader WHERE upload='N'";
			String sql = "SELECT * FROM OrderHeader WHERE is_processed!=1";
			c = db.selectSQL(sql);
			sql = null;

			if ((c != null) && (c.getCount() > 0)) {
				// If atleas 1 row is there

				while (c.moveToNext()) {
					OrderSplitMasterBO orderSplitMasterBO = new OrderSplitMasterBO();

					orderSplitMasterBO.setLinesPerCall(c.getInt(5));
					orderSplitMasterBO.setOrderID(c.getString(0));
					orderSplitMasterBO.setOrderValue(c.getDouble(4));
					orderSplitMasterBO.setPo(c.getString(14));
					orderSplitMasterBO.setRemark(c.getString(15));
					orderSplitMasterBO.setRetailerId(c.getString(2));
					orderSplitMasterBO.setRetailerName(c.getString(12));
					orderSplitMasterBO.setDeliveryDate(c.getString(9));

					orderSplitMasterBO.setProcessed(c.getInt(19));

					try {
						orderSplitMasterBO.setDiscount(c.getFloat(8));
					} catch (Exception ex) {
						Commons.print(DataMembers.SD+
								",Exception in orderSplitMasterBO.setDiscount(c.getFloat(8)) : "
										+ ex.getMessage());
						ex.printStackTrace();
					}

					orderSplitMasterBO.setDownloadedDate(c.getString(13));

					try {
						orderSplitMasterBO.setFree_product_count(c.getInt(16));
					} catch (Exception ex) {
						Commons.print(DataMembers.SD+
								",Exception in orderSplitMasterBO.setFree_product_count(c.getInt(16)) : "
										+ ex.getMessage());
						ex.printStackTrace();
					}

					orderSplitMasterBO.setRetailerCode(c.getString(11));

					try {
						orderSplitMasterBO.setRoute_id(c.getInt(3));
					} catch (Exception ex) {
						Commons.print(DataMembers.SD+
								",Exception in orderSplitMasterBO.setRoute_id(c.getInt(3)); : "
										+ ex.getMessage());
						ex.printStackTrace();
					}
					orderSplitMasterBOList.add(orderSplitMasterBO);

				}
			}

			db.closeDB();
		} catch (Exception e) {
			Commons.printException(e);
		}
	}

	public void updateIsprocessedFlag(String orderId, int is_processed) {
		StringBuilder sbuilder = new StringBuilder();
		sbuilder.append("UPDATE OrderHeader SET is_processed=");
		sbuilder.append(is_processed);

		sbuilder.append(",");
		sbuilder.append("upload=");
		sbuilder.append("\'N\'");

		sbuilder.append(" WHERE OrderID=");
		sbuilder.append("\'");
		sbuilder.append(orderId);
		sbuilder.append("\'");

		String query = sbuilder.toString();
		sbuilder = null;

		DBUtil db = null;
		db = new DBUtil(ctx, DataMembers.DB_NAME, DataMembers.DB_PATH);
		db.openDataBase();
		db.updateSQL(query);
		db.updateSQL("UPDATE OrderDetail SET upload = 'N' WHERE OrderID ='"
				+ orderId + "'");
		db.closeDB();
		query = null;
		db = null;
	}

	public void updateIsprocessedFlagForAll(int is_processed) {
		StringBuilder sbuilder = new StringBuilder();
		sbuilder.append("UPDATE OrderHeader SET is_processed=");
		sbuilder.append(is_processed);
		sbuilder.append(",");
		sbuilder.append("upload=");
		sbuilder.append("\'N\'");
		sbuilder.append(" WHERE is_processed!=");
		sbuilder.append(OrderSplitMasterBO.ORDER_PROCESSED);

		String query = sbuilder.toString();
		sbuilder = null;

		DBUtil db = null;
		db = new DBUtil(ctx, DataMembers.DB_NAME, DataMembers.DB_PATH);
		db.openDataBase();
		db.updateSQL(query);
		db.updateSQL("UPDATE OrderDetail SET upload = 'N'");
		db.closeDB();
		query = null;
		db = null;
	}

	public String QT(String data) {
		return "'" + data + "'";
	}

	public void updateEditOrderUploadFlag(String orderID) {
		StringBuilder sbuilder = new StringBuilder();
		sbuilder.append("UPDATE OrderHeader SET upload=");
		sbuilder.append("\'N\'");
		sbuilder.append(" WHERE OrderID=");
		sbuilder.append(QT(orderID));

		String query = sbuilder.toString();

		StringBuilder sbuilder1 = new StringBuilder();
		sbuilder1.append("UPDATE OrderDetail SET upload=");
		sbuilder1.append("\'N\'");
		sbuilder1.append(" WHERE OrderID=");
		sbuilder1.append(QT(orderID));
		String query1 = sbuilder1.toString();
		sbuilder = null;
		sbuilder1 = null;
		DBUtil db = null;
		db = new DBUtil(ctx, DataMembers.DB_NAME, DataMembers.DB_PATH);
		db.openDataBase();
		db.updateSQL(query);
		db.updateSQL(query1);

		db.closeDB();
		query = null;
		db = null;
	}

	public void updateEditOrderUploadFlagAsY(String orderID) {
		StringBuilder sbuilder = new StringBuilder();
		sbuilder.append("UPDATE OrderHeader SET upload=");
		sbuilder.append("\'Y\'");
		sbuilder.append(" WHERE OrderID=");
		sbuilder.append(QT(orderID));

		String query = sbuilder.toString();

		StringBuilder sbuilder1 = new StringBuilder();
		sbuilder1.append("UPDATE OrderDetail SET upload=");
		sbuilder1.append("\'Y\'");
		sbuilder1.append(" WHERE OrderID=");
		sbuilder1.append(QT(orderID));
		String query1 = sbuilder1.toString();
		sbuilder = null;
		sbuilder1 = null;
		DBUtil db = null;
		db = new DBUtil(ctx, DataMembers.DB_NAME, DataMembers.DB_PATH);
		db.openDataBase();
		db.updateSQL(query);
		db.updateSQL(query1);

		db.closeDB();
		query = null;
		db = null;
	}

	public boolean isAtleast1CheckedInOrderSplitMasterList() {
		if ((this.orderSplitMasterBOList == null)
				|| (orderSplitMasterBOList.size() < 1))
			return true;
		else {
			for (OrderSplitMasterBO orderSplitMasterBO : orderSplitMasterBOList) {
				if (orderSplitMasterBO.getProcessed() == OrderSplitMasterBO.ORDER_PROCESSED)
					return true;
			}
		}

		return false;
	}

	public void updateOrderProcessing() {
		if ((this.orderSplitMasterBOList == null)
				|| (orderSplitMasterBOList.size() < 1))
			return;
		if (isAtAllCheckedInOrderSplitMasterList()) {
			updateIsprocessedFlagForAll(OrderSplitMasterBO.ORDER_PROCESSED);
		} else {
			for (OrderSplitMasterBO orderSplitMasterBO : orderSplitMasterBOList) {
				String orderId = orderSplitMasterBO.getOrderID();
				int processed = orderSplitMasterBO.getProcessed();

				updateIsprocessedFlag(orderId, processed);
			}
		}

	}

	public boolean isAtAllCheckedInOrderSplitMasterList() {
		if ((this.orderSplitMasterBOList == null)
				|| (orderSplitMasterBOList.size() < 1))
			return true;
		else {
			for (OrderSplitMasterBO orderSplitMasterBO : orderSplitMasterBOList) {
				if (orderSplitMasterBO.getProcessed() == OrderSplitMasterBO.ORDER_NOT_PROCESSED)
					return false;
			}
		}

		return true;
	}

	public int getChildCountOfCurrentlySelectedOrderSplittingMasterBO() {
		if (currentlySelectedOrderSplittingMasterBO == null)
			return 0;
		currentlySelectedOrderSplittingMasterBO.generateChildCount();
		return currentlySelectedOrderSplittingMasterBO.getTotal_child_count();
	}

	public boolean isReachedMaximumChildCountInSplit() {
		if (getChildCountOfCurrentlySelectedOrderSplittingMasterBO() == MAXIMUM_NUMBER_OF_CHILD_IN_SINGLE_ORDER_SPLIT)
			return true;
		return false;
	}

	/**
	 * @return the total_future_split_count
	 */
	public int getTotal_future_split_count() {
		return total_future_split_count;
	}

	/**
	 * @param total_future_split_count
	 *            the total_future_split_count to set
	 */
	public void setTotal_future_split_count(int total_future_split_count) {
		this.total_future_split_count = total_future_split_count;
	}

	public void insertSplittedOrder(String retailerId, String orderId) {
		DBUtil db = null;
		try {
			db = new DBUtil(ctx, DataMembers.DB_NAME, DataMembers.DB_PATH);
			db.openDataBase();

			StringBuilder sbuilder = new StringBuilder();
			sbuilder.append("INSERT INTO SplittedOrder VALUES (");
			sbuilder.append("\'");
			sbuilder.append(retailerId);
			sbuilder.append("\'");
			sbuilder.append(",");
			sbuilder.append("\'");
			sbuilder.append(orderId);
			sbuilder.append("\'");

			sbuilder.append(",");
			sbuilder.append("\'");
			sbuilder.append("N");
			sbuilder.append("\'");
			sbuilder.append(")");
			String query = sbuilder.toString();
			db.executeQ(query);

			db.closeDB();

		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	public boolean isUploaded(String orderId) {
		boolean result = false;
		DBUtil db = null;
		Cursor c = null;
		try {
			db = new DBUtil(ctx, DataMembers.DB_NAME, DataMembers.DB_PATH);
			db.openDataBase();

			String query = "SELECT * FROM OrderHeader WHERE OrderID=\'"
					+ orderId + "\'";
			c = db.selectSQL(query);
			if (c != null) {
				if (c.getCount() > 0) {
					c.moveToNext();
					if (c.getString(6).equals("Y"))
						result = true;
					else
						result = false;
				}
			}

		} catch (Exception ex) {
			ex.printStackTrace();
		}

		if (db != null)
			db.closeDB();
		return result;
	}

	/**
	 * Deletes OrderHeader and OrderDetail for given OrderID
	 * 
	 * @param orderId
	 */
	public void deleteOrder(String orderId) {
		try {
			DBUtil db = null;
			db = new DBUtil(ctx, DataMembers.DB_NAME, DataMembers.DB_PATH);
			db.openDataBase();
			db.deleteSQL("OrderDetail", "OrderID = '" + orderId + "'", false);
			db.deleteSQL("OrderHeader", "OrderID = '" + orderId + "'", false);
			db.closeDB();
			db = null;
		} catch (Exception e) {
			Commons.printException(e);
		}
	}

	public boolean isRemarkandPoNoEntered() {

		for (OrderSplittingMasterBO orderSplitMaster : targetOrderSplittingMasterBOList) {

			if (orderSplitMaster.getTotal_child_count() > 0) {
				if (orderSplitMaster.getPo().equals("")
						|| orderSplitMaster.getRemarks().equals("")) {
					return false;
				}
			}
		}
		return true;

	}

	public boolean isAllOrderTickAvailableSplit() {
		boolean returnFlag = false;
		try {
			List<OrderSplittingDetailsBO> orderSplittingDetailsBOList1 = currentlySelectedOrderSplittingMasterBOForEdit
					.getOrderSplittingDetailsBOList();
			if ((orderSplittingDetailsBOList1 != null)
					&& (orderSplittingDetailsBOList1.size() > 0)) {
				for (OrderSplittingDetailsBO ob1 : orderSplittingDetailsBOList1) {
					if (ob1.isTicked_in_dialog_check_box() == false) {
						// targetOrderSplitDetailsBOList.add(ob1);
						returnFlag = true;
					} else {
						returnFlag = false;
					}
				}
			}
		} catch (Exception e) {
			Commons.printException(e);
			return false;
		}
		return returnFlag;
	}

}
