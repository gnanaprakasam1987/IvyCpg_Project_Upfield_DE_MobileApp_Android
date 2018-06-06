package com.ivy.sd.png.provider;

import android.content.Context;
import android.database.Cursor;

import com.ivy.cpg.view.order.DiscountHelper;
import com.ivy.lib.existing.DBUtil;
import com.ivy.sd.png.bo.ProductMasterBO;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.util.Commons;
import com.ivy.sd.png.util.DataMembers;

import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;

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

	public LinkedList<ProductMasterBO> sort(LinkedList<ProductMasterBO> productList) {
		Collections.sort(productList, new Comparator<ProductMasterBO>() {
			@Override
			public int compare(ProductMasterBO o1, ProductMasterBO o2) {
				if (o1.getProductShortName().toLowerCase() != null && o2.getProductShortName().toLowerCase() != null)
					return o1.getProductShortName().toLowerCase().trim().compareTo(o2.getProductShortName().toLowerCase().trim());
				else
					return o1.getProductName().toLowerCase().trim().compareTo(o2.getProductName().toLowerCase().trim());
			}
		});
		return productList;
	}

	public LinkedList<ProductMasterBO> sortbyBrand(LinkedList<ProductMasterBO> orderedProductList) {
		int loopEnd = getMaxLevelID() - bmodel.getPrintSequenceLevelID();

		String productIDs = ObjectToCommaSeperated(orderedProductList);

		LinkedList<ProductMasterBO> list = new LinkedList<>();
		DBUtil db = new DBUtil(context, DataMembers.DB_NAME,
				DataMembers.DB_PATH);
		db.openDataBase();
		StringBuilder sb = new StringBuilder();
		if (loopEnd == 0) {
			sb.append("Select distinct A.pid from ProductMaster A ");
			sb.append("inner join ProductLevel F on A.PLid = F.LevelID ");
			sb.append("where F.LevelId = (Select LevelID from ProductLevel where LevelID not in (Select ParentID from ProductLevel)) ");
			sb.append("And A.pid in (" + productIDs + ") ");
			sb.append("order by case when (trim(A.psname) is NULL or length(trim(A.PsName)) = 0) then trim(A.pname) else trim(A.psname) end collate nocase");
		} else {
			String prevALias = "PM", orderByString = "";
			sb.append("Select distinct PM.pid from ProductMaster PM ");
			for (int i = 1; i <= loopEnd; i++) {
				sb.append("inner join ProductMaster PM" + i + " on " + prevALias + ".ParentId = PM" + i + ".PID ");
				prevALias = "PM" + i;
			}
			sb.append("inner join ProductLevel F on PM.PLid = F.LevelID ");
			sb.append("where F.LevelId = (Select LevelID from ProductLevel where LevelID not in (Select ParentID from ProductLevel)) ");
			sb.append("And PM.pid in (" + productIDs + ") ");
			;
			orderByString = "case when (trim(" + prevALias + ".psname) IS NULL or length(trim(" + prevALias + ".PsName)) = 0) then trim(" + prevALias + ".pname) else trim(" + prevALias + ".psname) end collate nocase ," +
					"case when (trim(PM.psname) is NULL or length(trim(PM.PsName)) = 0) then trim(PM.pname) else trim(PM.psname) end collate nocase ";
			sb.append("order by " + orderByString);
		}

		Cursor orderHeaderCursor = db.selectSQL(sb.toString());
		if (orderHeaderCursor != null) {
			while (orderHeaderCursor.moveToNext()) {
				for (ProductMasterBO productsList : orderedProductList) {
					if (productsList.getProductID().equals(orderHeaderCursor.getString(0))) {
						list.add(productsList);
					}
				}
			}
			orderHeaderCursor.close();
		}
		db.closeDB();
		return list;
	}
	
	public void updateGoldenStoreDetails(int isGoldenStore){
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

	private String ObjectToCommaSeperated(LinkedList<ProductMasterBO> orderedProductList) {
		StringBuilder csvBuilder = new StringBuilder();
		for (ProductMasterBO productBO : orderedProductList) {
			csvBuilder.append(productBO.getProductID());
			csvBuilder.append(",");
		}

		String csv = csvBuilder.toString();
		System.out.println(csv);
		csv = csv.substring(0, csv.length() - ",".length());
		System.out.println(csv);
		//OUTPUT: Milan,London,New York,San Francisco
		return csv;
	}

	private int getMaxLevelID() {
		DBUtil db = new DBUtil(context, DataMembers.DB_NAME, DataMembers.DB_PATH);
		;
		try {
			db.openDataBase();
			String sb = "Select LevelID from ProductLevel where LevelID not in (Select ParentID from ProductLevel)";

			Cursor c = db.selectSQL(sb);
			if (c != null) {
				if (c.getCount() > 0) {
					while (c.moveToNext()) {
						return c.getInt(0);
					}
				}
				c.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return 0;
	}
}
