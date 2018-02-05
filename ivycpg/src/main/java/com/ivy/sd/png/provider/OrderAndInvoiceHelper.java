package com.ivy.sd.png.provider;

import android.content.Context;
import android.database.Cursor;

import com.ivy.cpg.view.order.DiscountHelper;
import com.ivy.lib.existing.DBUtil;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.util.Commons;
import com.ivy.sd.png.util.DataMembers;

public class OrderAndInvoiceHelper {

	private Context context;
	private BusinessModel bmodel;
    private DiscountHelper discountHelper;

	private static OrderAndInvoiceHelper instance = null;
	public double mGolderStoreDiscountAmount = 0;
	
	protected OrderAndInvoiceHelper(Context context) {
		this.context = context;
		this.bmodel = (BusinessModel) context;
        discountHelper = DiscountHelper.getInstance(context);
    }

	public static OrderAndInvoiceHelper getInstance(Context context) {
		if (instance == null) {
			instance = new OrderAndInvoiceHelper(context);
		}
		return instance;
	}

	public String QT(String data) {
		return "'" + data + "'";
	}

	/**
	 * get the bill wise discount amount/percentage value from OrderHeader.
	 * 
	 * @param retailerId
	 * @return double
	 */
	public double restoreDiscountAmount(String retailerId) {

		double discValue = 0;
		try {
			DBUtil db = new DBUtil(context, DataMembers.DB_NAME,
					DataMembers.DB_PATH);
			db.createDataBase();
			db.openDataBase();
			// Order Header

			StringBuffer sb=new StringBuffer();
			sb.append("select percentage,value from InvoiceDiscountDetail ID inner join ");
			sb.append(" orderheader OH on ID.orderid=OH.orderid where ID.upload='N' and OH.invoicestatus='0' ");
			sb.append(" and OH.retailerid="+QT(retailerId));

			Cursor orderHeaderCursor = db.selectSQL(sb.toString());
			if (orderHeaderCursor != null) {
				if (orderHeaderCursor.moveToNext()) {
					discValue = orderHeaderCursor.getDouble(0);
                    discountHelper.getBillWiseDiscountList().get(0).setAppliedDiscount(discValue);

				}
			}
			orderHeaderCursor.close();
			db.closeDB();
		} catch (Exception e) {
			Commons.printException(e);
		}
		return discValue;

	}






	
	public void updateGoldenStoreDetails(int isGoldenStore)
	{
		try{
			DBUtil db = new DBUtil(context, DataMembers.DB_NAME,
					DataMembers.DB_PATH);
			db.createDataBase();
			db.openDataBase();
			db.updateSQL("Update DailyTargetPlanned set IsGoldenStore = "+isGoldenStore+",upload='N' Where RetailerID  = "+bmodel.getRetailerMasterBO().getRetailerID());
			db.closeDB();
		}catch(Exception e){
			Commons.printException(e);
		}
	}
	
	/**
	 * Pass the OrderID to Order Header and the How much discount applied for Golden Store
	 * and also get the Scheme Discount for Product, Amount and Percentage
	 * @param orderID
	 * @return
	 */
	public double calculateSchemeDiscountDetails(String orderID){
		DBUtil db = null;
		Cursor cursor;
		double totalSchemeDiscount = 0;
		String query;
		mGolderStoreDiscountAmount = 0;
		try {
			db = new DBUtil(context, DataMembers.DB_NAME, DataMembers.DB_PATH);
			db.openDataBase();
			String uid = new String();
			// Order Header
			query = "select OrderID,Discount from " + DataMembers.tbl_orderHeader
					+ " where upload='N' and RetailerID="
					+ bmodel.QT(bmodel.getRetailerMasterBO().getRetailerID())
					+ " and OrderId = "+orderID;

			cursor = db.selectSQL(query);
			if (cursor != null) {
				if (cursor.moveToNext()) {
					uid = cursor.getString(0);
					mGolderStoreDiscountAmount = cursor.getDouble(1);
				}
			}
			query = null;
			cursor.close();

			// calculcate Price Discount in Scheme
			query = "SELECT Distinct Qty*(Rate-SchPrice) FROM "
					+ DataMembers.tbl_orderDetails + " WHERE OrderID ="
					+ bmodel.QT(uid) + " and schPrice !=0";

			cursor = db.selectSQL(query);
			if (cursor != null) {
				while (cursor.moveToNext()) {
					totalSchemeDiscount = totalSchemeDiscount
							+ cursor.getDouble(0);
				}
				cursor.close();
			}
			query = null;
			// calculcate Amount Discount in Scheme wise
			query = "SELECT SdAmt FROM " + DataMembers.tbl_orderDetails
					+ " WHERE OrderID =" + bmodel.QT(uid)
					+ " and sdamt !=0   Group By SchID";

			cursor = db.selectSQL(query);
			if (cursor != null) {
				while (cursor.moveToNext()) {
					totalSchemeDiscount = totalSchemeDiscount
							+ cursor.getDouble(0);
				}
				cursor.close();
			}

			// calculate Percantage Discount in Scheme wise
			query = "SELECT Distinct SUM((Qty*Rate)-totalamount) FROM "
					+ DataMembers.tbl_orderDetails + " WHERE OrderID ="
					+ bmodel.QT(uid) + " and sdPer !=0";
			cursor = db.selectSQL(query);
			if (cursor != null) {
				while (cursor.moveToNext()) {
					totalSchemeDiscount = totalSchemeDiscount
							+ cursor.getDouble(0);
				}
				cursor.close();
			}
			query = null;
			db.closeDB();
		} catch (Exception e) {
			db.closeDB();
			Commons.printException(e);
		}
		return totalSchemeDiscount;

	}
	
	/**
	 * Check data is in Transaction sequence Table
	 * 
	 * @return true /false;
	 */
	public boolean hasTransactionSequence() {
		DBUtil db = null;
		boolean istransaction = false;
		try {
			db = new DBUtil(context, DataMembers.DB_NAME, DataMembers.DB_PATH);
			db.openDataBase();

			Cursor c = db.selectSQL("SELECT TypeID FROM TransactionSequence WHERE Upload = 'N'");

			if (c.getCount() > 0) {
				if (c.moveToNext()) {
					istransaction = true;
				}
			}
			c.close();
			db.closeDB();
		} catch (Exception e) {
			db.closeDB();
			Commons.printException(e);
		}

		return istransaction;
	}
}
