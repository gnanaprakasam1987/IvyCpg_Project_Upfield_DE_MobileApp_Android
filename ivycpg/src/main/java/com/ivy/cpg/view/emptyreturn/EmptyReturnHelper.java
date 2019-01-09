package com.ivy.cpg.view.emptyreturn;


import android.content.Context;
import android.database.Cursor;

import com.ivy.cpg.view.emptyreconcil.EmptyReconciliationHelper;
import com.ivy.lib.existing.DBUtil;
import com.ivy.sd.png.bo.ProductMasterBO;
import com.ivy.sd.png.commons.SDUtil;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.util.Commons;
import com.ivy.sd.png.util.DataMembers;

import java.util.Vector;

public class EmptyReturnHelper {

	private Context context;
	private BusinessModel bmodel;
	private static EmptyReturnHelper instance = null;

	public int mSelectedFilter = -1;

	private Vector<ProductMasterBO> mProductType;

	private EmptyReturnHelper(Context context) {
		this.context = context;
		this.bmodel = (BusinessModel) context;
	}

	public static EmptyReturnHelper getInstance(Context context) {
		if (instance == null) {
			instance = new EmptyReturnHelper(context);
		}
		return instance;
	}

	public Vector<ProductMasterBO> getProductType() {
		return mProductType;
	}

	public void downloadProductType() {

		DBUtil db = null;
		try {
			mProductType = new Vector<>();

			db = new DBUtil(context, DataMembers.DB_NAME, DataMembers.DB_PATH);
			db.openDataBase();
			String sb = "Select Distinct PM.Pid,PM.Pname,PM.piece_uomid From ProductMaster PM INNER JOIN StandardListMaster SLM on PM.TypeId = SLM.ListId"
					+ " WHERE PM.isReturnable =1 and SLM.ListCode ='GENERIC' ORDER BY PM.Pid";
			Cursor cur = db.selectSQL(sb);

			if (cur != null) {
				while (cur.moveToNext()) {
					ProductMasterBO product = new ProductMasterBO();
					product.setProductID(cur.getString(0));
					product.setProductName(cur.getString(1));
					product.setPcUomid(cur.getInt(2));
					product.setIsReturnable(1);
					mProductType.add(product);
				}
				cur.close();
			}



			db.closeDB();
		} catch (Exception e) {
			Commons.printException(e);
			if(db!=null)
			db.closeDB();
		}

	}

	/**
	 * Save Tracking Detail in Detail Table
	 */
	public void saveEmptyReturn() {
		DBUtil db = null;
		try {
			db = new DBUtil(context, DataMembers.DB_NAME, DataMembers.DB_PATH);
			db.openDataBase();

			String tid;
			String values;
			double lineValue, returnValue = 0;
			String headerColumns = "orderid,orderdate,retailerid,ReturnValue,OFlag,ridSF,VisitId";
			String returncolumns = "OrderID,Pid,ReturnQty,Price,UomID,TypeID,LineValue,RetailerID,LiableQty,Qty";

			tid = QT(bmodel.getAppDataProvider().getUser().getUserid()
					+ SDUtil.now(SDUtil.DATE_TIME_ID));

			// save detail
			if (bmodel.configurationMasterHelper.SHOW_GROUPPRODUCTRETURN) {
				for (ProductMasterBO product : getProductType()) {
					if (product.getIsReturnable() == 1
							&& product.getRetPieceQty() > 0) {

						lineValue = product.getSrp() * product.getRetPieceQty();

						returnValue = returnValue + lineValue;

						values = tid
								+ ","
								+ QT("0")
								+ ","
								+ QT(product.getRetPieceQty() + "")
								+ ","
								+ QT(product.getSrp() + "")
								+ ","
								+ QT(product.getPcUomid() + "")
								+ ","
								+ QT(product.getProductID())
								+ ","
								+ QT(lineValue + "")
								+ ","
								+ QT(bmodel.getAppDataProvider().getRetailMaster()
										.getRetailerID()) + "," + "0" + ","
								+ "0";

						db.insertSQL(DataMembers.tbl_orderReturnDetails,
								returncolumns, values);
					}

				}
			} else {
				for (ProductMasterBO product : bmodel.productHelper
						.getProductMaster()) {
					if (product.getIsReturnable() == 1
							&& product.getRetPieceQty() > 0) {

						lineValue = product.getSrp() * product.getRetPieceQty();

						returnValue = returnValue + lineValue;

						values = tid
								+ ","
								+ QT(product.getProductID())
								+ ","
								+ QT(product.getRetPieceQty() + "")
								+ ","
								+ QT(product.getSrp() + "")
								+ ","
								+ QT(product.getPcUomid() + "")
								+ ","
								+ QT("0")
								+ ","
								+ QT(lineValue + "")
								+ ","
								+ QT(bmodel.getAppDataProvider().getRetailMaster()
										.getRetailerID()) + "," + "0" + ","
								+ "0";

						db.insertSQL(DataMembers.tbl_orderReturnDetails,
								returncolumns, values);
					}

				}
			}

			// save header with total line value
			values = tid + "," + QT(SDUtil.now(SDUtil.DATE_GLOBAL)) + ","
					+ QT(bmodel.getAppDataProvider().getRetailMaster().getRetailerID()) + ","
					+ QT(returnValue + "") + "," + 0 + "," + QT(bmodel.getAppDataProvider().getRetailMaster().getRidSF()) + ","
					+ bmodel.getAppDataProvider().getUniqueId();

			db.insertSQL(DataMembers.tbl_orderHeader, headerColumns, values);

			// update credit limit in RetailerMaster
			db.executeQ("UPDATE RetailerMaster SET RetCreditLimit = "
					+ (bmodel.getAppDataProvider().getRetailMaster().getBottle_creditLimit() + returnValue)
					+ " Where RetailerID = "
					+ bmodel.getAppDataProvider().getRetailMaster().getRetailerID());
			// Update the lastest amount in RetailerMasterBo, then only reduce
			// the amount for next order within the Screen
			bmodel.getAppDataProvider().getRetailMaster().setBottle_creditLimit(
					bmodel.getAppDataProvider().getRetailMaster().getBottle_creditLimit()
							+ returnValue);
			bmodel.getAppDataProvider().getRetailMaster().setProfile_creditLimit("" + (bmodel.getAppDataProvider().getRetailMaster().getBottle_creditLimit()
					+ returnValue));
			if (bmodel.configurationMasterHelper.SHOW_GROUPPRODUCTRETURN)
				saveTotalOrderReturnQty();
			else
				EmptyReconciliationHelper.getInstance(context).saveSKUWiseTransaction();

			db.closeDB();
		} catch (Exception e) {
			Commons.printException(e);
			if(db!=null)
			db.closeDB();
		}
	}

	public boolean hasDataTosave() {

		Vector<ProductMasterBO> products;

		if (bmodel.configurationMasterHelper.SHOW_GROUPPRODUCTRETURN)
			products = getProductType();
		else
			products = bmodel.productHelper.getProductMaster();

		for (ProductMasterBO product : products) {
			if (product.getIsReturnable() == 1 && product.getRetPieceQty() > 0)
				return true;
		}

		return false;
	}

	private void saveTotalOrderReturnQty() {
		DBUtil db = null;
		try {
			db = new DBUtil(context, DataMembers.DB_NAME, DataMembers.DB_PATH);
			db.openDataBase();
			String sql, values;
			Cursor cursor;
			String columns = "Pid,Qty";
			// Get the Values from OrderRetrun Details and Insert in to the
			// OrderReturnQty Table
			sql = "SELECT Pid FROM OrderReturnQty Group by Pid ";
			cursor = db.selectSQL(sql);
			if (cursor.getCount() > 0) {
				while (cursor.moveToNext()) {
					for (ProductMasterBO product : getProductType()) {
						if (product.getIsReturnable() == 1
								&& product.getRetPieceQty() > 0) {

							if (cursor.getString(0).equals(
									product.getProductID())) {
								sql = "UPDATE OrderReturnQty SET QTY = QTY +"
										+ product.getRetPieceQty()
										+ " WHERE PID ="
										+ product.getProductID();
								db.updateSQL(sql);
								break;
							}
						}

					}
				}
				cursor.close();
			} else {
				for (ProductMasterBO product : getProductType()) {
					values = product.getProductID() + ","
							+ product.getRetPieceQty();
					db.insertSQL("OrderReturnQty", columns, values);
				}
			}
			cursor.close();
			db.closeDB();
		} catch (Exception e) {
			Commons.printException(e);
			if(db!=null)
			db.closeDB();
		}

	}

	public String QT(String data) {
		return "'" + data + "'";
	}

}
