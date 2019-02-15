package com.ivy.cpg.view.emptyreconcil;

import android.content.Context;
import android.database.Cursor;

import com.ivy.lib.existing.DBUtil;
import com.ivy.sd.png.bo.BomReturnBO;
import com.ivy.sd.png.bo.ProductMasterBO;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.util.Commons;
import com.ivy.sd.png.util.DataMembers;
import com.ivy.utils.DateTimeUtils;

import java.util.ArrayList;

public class EmptyReconciliationHelper {

	private Context context;
	private BusinessModel bmodel;
	private static EmptyReconciliationHelper instance = null;

	private String mTransactionHeader = "EmptyReconciliationHeader";
	private String mTransactionDetail = "EmptyReconciliationDetail";

	public int mSelectedFilter = -1;

	private EmptyReconciliationHelper(Context context) {
		this.context = context;
		this.bmodel = (BusinessModel) context.getApplicationContext();
	}

	public static EmptyReconciliationHelper getInstance(Context context) {
		if (instance == null) {
			instance = new EmptyReconciliationHelper(context);
		}
		return instance;
	}

	public class SKUTypeBO {

		private String typeName;
		private int typeID, qty;

		public String getTypeName() {
			return typeName;
		}

		public void setTypeName(String typeName) {
			this.typeName = typeName;
		}

		public int getTypeID() {
			return typeID;
		}

		public void setTypeID(int typeID) {
			this.typeID = typeID;
		}

		public int getQty() {
			return qty;
		}

		public void setQty(int qty) {
			this.qty = qty;
		}

		public String toString() {
			return typeName;
		}
	}

	ArrayList<SKUTypeBO> SkuTypeBO;

	public ArrayList<SKUTypeBO> getSkuTypeBO() {
		return SkuTypeBO;
	}

	public void setSkuTypeBO(ArrayList<SKUTypeBO> skuTypeBO) {
		SkuTypeBO = skuTypeBO;
	}

	public void downloadReturnProductsTypeNew() {

		DBUtil db = new DBUtil(context, DataMembers.DB_NAME);
		try {
			db.openDataBase();
			String sb = "SELECT DISTINCT O.Pid,P.Pname,IFNULL(O.QTY,0) As OQty,IFNULL(D.QTY,0)  AS VQty From ProductMaster P "+
			" LEFT JOIN (SELECT PId, SUM(Qty) AS QTY FROM OrderReturnQty Group By Pid) AS O ON  P.pid = O.Pid "+
			" INNER JOIN StandardListMaster SLM on P.TypeId = SLM.ListId"+
			" LEFT JOIN (SELECT TypeID, SUM(pcsqty) AS QTY FROM VanUnloadDetails WHERE SubDepotID != 0 Group By TypeID) AS D ON D.TypeID = P.pid"+
			" WHERE P.isReturnable = 1 and SLM.ListCode ='GENERIC' Order by P.Pid";
			
			Cursor cur = db.selectSQL(sb);
			if (cur != null) {
				setSkuTypeBO(new ArrayList<EmptyReconciliationHelper.SKUTypeBO>());
				while (cur.moveToNext()) {
					SKUTypeBO type = new SKUTypeBO();
					type.setTypeID(cur.getInt(0));
					type.setTypeName(cur.getString(1));
					type.setQty(cur.getInt(2));
					if (type.getQty() > 0)
						getSkuTypeBO().add(type);
				}
				cur.close();
			}

			db.closeDB();
		} catch (Exception e) {
			Commons.printException(""+e);
			db.closeDB();
		}

	}

	ArrayList<ProductMasterBO> productBO;

	public ArrayList<ProductMasterBO> getProductBO() {
		return productBO;
	}

	public void setProductBO(ArrayList<ProductMasterBO> productBO) {
		this.productBO = productBO;
	}

	public void downloadProducts() {
		DBUtil db = new DBUtil(context, DataMembers.DB_NAME);
		try {
			db.openDataBase();
			String sb = "SELECT A.PID, A.PName, A.barcode, A.TypeID, A.Pname, A.baseprice, A.piece_uomid FROM ProductMaster A"+
			" WHERE A.TypeId NOT IN (SELECT ListID FROM StandardListMaster WHERE  ListCode ='GENERIC') and A.isReturnable = 1  Order by A.Pid";

			Cursor cur = db.selectSQL(sb);
			if (cur != null) {
				setProductBO(new ArrayList<ProductMasterBO>());
				while (cur.moveToNext()) {
					ProductMasterBO sku = new ProductMasterBO();
					sku.setProductID(cur.getString(0));
					sku.setProductName(cur.getString(1));
					sku.setBarCode(cur.getString(2));
					sku.setTypeID(cur.getInt(3));
					sku.setTypeName(cur.getString(4));
					if(cur.getString(5)!=null)
					 sku.setPrice(cur.getString(5));
					sku.setPcUomid(cur.getInt(6));
					getProductBO().add(sku);
				}
				cur.close();
			}
			db.closeDB();
		} catch (Exception e) {
			Commons.printException(""+e);
			db.closeDB();
		}
	}

	/**
	 * Download Generic SKu Poducts and its's Id from ProductMaster and
	 * BomMaster Table
	 */
	public void downloadNonGenericProductID() {
		DBUtil db = new DBUtil(context, DataMembers.DB_NAME);
		try {
			db.openDataBase();
			Cursor cur = db
					.selectSQL("SELECT  B.Bpid,A.PID,A.Pname  FROM ProductMaster A inner JOIN BomMaster B on A.pid = B.pid WHERE A.isReturnable = '1' Order by A.pid");

			if (cur != null) {

				while (cur.moveToNext()) {
					for (ProductMasterBO product : getProductBO()) {

						if (product.getProductID().equals(cur.getString(0))) {
							product.setTypeID(cur.getInt(1));
							product.setTypeName(cur.getString(2));
							break;
						}
					}

				}
				cur.close();
			}
			db.closeDB();
		} catch (Exception e) {
			Commons.printException(""+e);
			db.closeDB();
		}
	}

	public boolean isDataToSave() {
		try {
			for (ProductMasterBO sku : getProductBO()) {
				if (sku.getRetPieceQty() > 0)
					return true;
			}
		} catch (Exception e) {
			Commons.printException(""+e);

		}
		return false;
	}

	public boolean checkDataTosave() {
		boolean status = false;
		int mTypeTotal;
		for (SKUTypeBO type : getSkuTypeBO()) {
			mTypeTotal = 0;
			for (ProductMasterBO sku : getProductBO()) {
				if (sku.getTypeID() == type.getTypeID())
					mTypeTotal = mTypeTotal + sku.getRetPieceQty();
			}
			if (mTypeTotal > 0) {
				if (mTypeTotal <= type.getQty())
					status = true;
				else
					status = false;
			}
		}

		return status;
	}

	/**
	 * Save Tracking Detail in Detail Table
	 */
	public void saveTransaction() {
		DBUtil db = new DBUtil(context, DataMembers.DB_NAME);
		try {
			db.openDataBase();
			String tid;
			String sql;
			Cursor headerCursor;
			String headerColumns = "Tid, Date, TimeZone, Value";
			String detailColumns = "Tid, PId, Qty, Price, UomId, UomCount, LineValue";
			String values;

			tid = bmodel.userMasterHelper.getUserMasterBO().getUserid() + ""
					+ DateTimeUtils.now(DateTimeUtils.DATE_TIME_ID);
			// delete transaction if exist
			sql = "SELECT Tid FROM " + mTransactionHeader + " WHERE Date = "
					+ QT(DateTimeUtils.now(DateTimeUtils.DATE_GLOBAL));

			headerCursor = db.selectSQL(sql);
			// delete transaction if exist
			if (headerCursor.getCount() > 0) {
				headerCursor.moveToNext();
				db.deleteSQL(mTransactionHeader,
						"Tid=" + QT(headerCursor.getString(0)), false);
				headerCursor.close();
			}

			// save header
			values = QT(tid) + "," + QT(DateTimeUtils.now(DateTimeUtils.DATE_GLOBAL)) + ","
					+ QT(bmodel.getTimeZone()) + "," + 0;

			db.insertSQL(mTransactionHeader, headerColumns, values);

			for (ProductMasterBO sku : getProductBO()) {
				if (sku.getRetPieceQty() > 0) {
					if (checkProductID(sku.getProductID())) {
						db.updateSQL("UPDATE " + mTransactionDetail
								+ " SET Qty = Qty +" + sku.getRetPieceQty()
								+ " , Tid =" + QT(tid) + " WHERE Pid = "
								+ sku.getProductID());
					} else {
						values = QT(tid) + "," + sku.getProductID() + ","
								+ sku.getRetPieceQty() + "," + sku.getPrice()
								+ "," + sku.getPcUomid() + "," + '1' + ","
								+ QT(sku.getTotalamount() + "");

						db.insertSQL(mTransactionDetail, detailColumns, values);
					}
				}

			}

			db.updateSQL("UPDATE EmptyReconciliationHeader SET Value = "
					+ " (SELECT SUM(LineValue) FROM EmptyReconciliationDetail"
					+ " WHERE TId = " + QT(tid) + " ) WHERE Tid = " + QT(tid));

			for (SKUTypeBO type : getSkuTypeBO()) {
				int mTypeTotal = 0;
				for (ProductMasterBO sku : getProductBO()) {
					if (sku.getRetPieceQty() > 0){
						if (sku.getTypeID() == type.getTypeID())
							mTypeTotal = mTypeTotal + sku.getRetPieceQty();
					}
				}

				String query = "Update OrderReturnQty set Qty = Qty-"
						+ mTypeTotal + " Where Pid = " + type.getTypeID();
				db.updateSQL(query);
			}
			db.closeDB();
		} catch (Exception e) {
			Commons.printException(""+e);
			db.closeDB();
		}
	}

	private boolean checkProductID(String productID) {
		DBUtil db = new DBUtil(context, DataMembers.DB_NAME);
		try {
			db.openDataBase();
			Cursor cursor = db
					.selectSQL("SELECT Qty FROM EmptyReconciliationDetail WHERE Pid ="
							+ productID);
			if (cursor.getCount() > 0) {
				cursor.close();
				db.closeDB();
				return true;
			}
			cursor.close();
			db.closeDB();
		} catch (Exception e) {
			Commons.printException(""+e);
			db.closeDB();
		}
		return false;

	}
	
	
	/**
	 * Save SKU Return Products Detail Group by All SKU in Day wise Record
	 */
	public void saveSKUWiseTransaction() {
		DBUtil db = new DBUtil(context, DataMembers.DB_NAME);
		try {
			db.openDataBase();
			String tid;
			String sql;
			Cursor cursor;
			Cursor headerCursor;
			boolean isData;
			String headerColumns = "Tid, Date, TimeZone, Value,Upload";
			String detailColumns = "Tid, PId, Qty, Price, UomId, UomCount, LineValue,Upload";
			String values;
			tid = bmodel.userMasterHelper.getUserMasterBO().getUserid() + ""
					+ DateTimeUtils.now(DateTimeUtils.DATE_TIME_ID);

			// delete transaction if exist
			sql = "SELECT Tid FROM " + mTransactionHeader + " WHERE Date = "
					+ QT(DateTimeUtils.now(DateTimeUtils.DATE_GLOBAL));

			headerCursor = db.selectSQL(sql);
			// delete transaction if exist
			if (headerCursor.getCount() > 0) {
				headerCursor.moveToNext();
				db.deleteSQL(mTransactionHeader,
						"Tid=" + QT(headerCursor.getString(0)), false);


				// Empties are added in stock, so while deleting SIH reduced
				sql = "SELECT pid,qty FROM " + mTransactionDetail + " WHERE tid = "
						+ QT(headerCursor.getString(0));
				cursor = db.selectSQL(sql);
				if (cursor != null) {
					while (cursor.moveToNext()) {

						db.updateSQL("update ProductMaster set sih=sih- " + (cursor.getInt(1))
								+ " where PID = " + cursor.getString(0));
						db.updateSQL("update StockInHandMaster set qty=(qty-" + (cursor.getInt(1)) + ") where pid=" + cursor.getString(0));
						int SIH=bmodel.productHelper.getProductMasterBOById(cursor.getString(0)).getSIH();
						bmodel.productHelper.getProductMasterBOById(cursor.getString(0)).setSIH((SIH-(cursor.getInt(1))));

					}
				}
				//

				db.deleteSQL(mTransactionDetail,
						"Tid=" + QT(headerCursor.getString(0)), false);
				headerCursor.close();
			}
			StringBuffer sb;
			// Get the Values from OrderRetrun Details and Insert in to the
			// EmptyReconciliationDetail Table
			sql = "SELECT E.Pid,  IFNULL(SUM(E.ReturnQty),0) as EODQty,"
					+ " IFNULL((SELECT  IFNULL(SUM(pcsqty),0) AS VanQty"
					+ " FROM VanUnloadDetails WHERE Pid = E.pid GRoup by pid) ,0) AS VanQty,"
					+ " E.Price, E.UomId  FROM OrderReturnDetail  E"
					+ " GRoup by E.pid";
			isData = false;
			cursor = db.selectSQL(sql);
			if (cursor != null) {
				while (cursor.moveToNext()) {
					sb = new StringBuffer();
					sb.append(QT(tid));
					sb.append(",");
					sb.append(cursor.getString(0));
					sb.append(",");
					sb.append(cursor.getInt(1) - cursor.getInt(2));
					sb.append(",");
					sb.append(cursor.getString(3));
					sb.append(",");
					sb.append(cursor.getString(4));
					sb.append( ",");
					sb.append("1");
					sb.append(",");
					sb.append(((cursor.getInt(1) - cursor.getInt(2))
							* cursor.getFloat(3)));
					sb.append(",");
					sb.append("'X'");

					db.insertSQL(mTransactionDetail, detailColumns,
							sb.toString());


					// update SIH
					db.updateSQL("update ProductMaster set sih=sih+ " + (cursor.getInt(1) - cursor.getInt(2))
							+ " where PID = " + cursor.getString(0));

					int SIH=bmodel.productHelper.getProductMasterBOById(cursor.getString(0)).getSIH();
					bmodel.productHelper.getProductMasterBOById(cursor.getString(0)).setSIH((SIH+(cursor.getInt(1) - cursor.getInt(2))));

					if(isProductAvailableinSIHmaster(cursor.getString(0))) {

						db.updateSQL("update StockInHandMaster set qty=(qty+" + (cursor.getInt(1) - cursor.getInt(2)) + ") where pid=" + cursor.getString(0));
					}
					else{
						db.insertSQL("StockInHandMaster",
								"pid,qty", cursor.getString(0)+","+(cursor.getInt(1) - cursor.getInt(2)));
					}


					isData = true;
				}
				cursor.close();
			}
			// Saving Transaction Header if There is Any Detail
			if (isData) {
				// save header
				values = QT(tid) + "," + QT(DateTimeUtils.now(DateTimeUtils.DATE_GLOBAL))
						+ "," + QT(bmodel.getTimeZone()) + "," + 0 + ","
						+ "'X'";

				db.insertSQL(mTransactionHeader, headerColumns, values);
			}
			db.closeDB();
		} catch (Exception e) {
			Commons.printException(""+e);
			db.closeDB();
		}
	}
	
	/**
	 * Save Tracking Detail in Detail Table
	 */
	public void updateEmptyReconilationTable() {
		DBUtil db = new DBUtil(context, DataMembers.DB_NAME);
		try {
            ArrayList<BomReturnBO> returnProducts = bmodel.productHelper
                    .getBomReturnProducts();
            db.openDataBase();
			Cursor cursor;
			cursor = db
					.selectSQL("SELECT Pid,Qty FROM EmptyReconciliationDetail ORDER BY Pid");
			if (cursor.getCount() > 0) {
				while (cursor.moveToNext()) {
                    for (BomReturnBO bomReturnBo : returnProducts) {
                        if (bomReturnBo.getReturnQty() > 0) {
                            if (bomReturnBo.getPid()
									.equals(cursor.getString(0))) {

								//update unloading SIH
								db.updateSQL("update ProductMaster set sih=sih- " + bomReturnBo.getReturnQty()
										+ " where PID = " + bomReturnBo.getPid());
								//if(bmodel.deliveryManagementHelper.isProductAvailableinSIHmaster(bomReturnBo.getPid())) {
								db.updateSQL("update StockInHandMaster set qty=(qty-" + bomReturnBo.getReturnQty() + ") where pid=" + bomReturnBo.getPid());
								//}


								if (cursor.getInt(1)
										- bomReturnBo.getReturnQty() == 0) {
									db.deleteSQL(mTransactionDetail, "Pid ="
											+ cursor.getInt(0), false);
								} else {
									db.updateSQL("UPDATE " + mTransactionDetail
											+ " SET Qty = Qty - "
											+ bomReturnBo.getReturnQty()
											+ " WHERE Pid = "
											+ bomReturnBo.getPid());
								}
								break;
							}
						}

					}

				}
			}
			cursor = db
					.selectSQL("SELECT Pid FROM EmptyReconciliationDetail");
			if (cursor.getCount() == 0) {
				db.deleteSQL(mTransactionHeader,null,true);
			}

			cursor.close();
			db.closeDB();
		} catch (Exception e) {
			Commons.printException(""+e);
			db.closeDB();
		}
	}
	

	/**
	 * Delete the Today's Empty Reconciliation Order while deleting the Order
	 */
	public void deleteEmptyReconciliationOrder() {
		DBUtil db = new DBUtil(context, DataMembers.DB_NAME);
		try {
			db.openDataBase();
			// delete transaction if exist
			String sql = "SELECT Tid FROM " + mTransactionHeader
					+ " WHERE Date = " + QT(DateTimeUtils.now(DateTimeUtils.DATE_GLOBAL))
					+ " And Upload='X' ";

			Cursor cursor = db.selectSQL(sql);
			// delete transaction if exist
			if (cursor.getCount() > 0) {
				cursor.moveToNext();

				db.deleteSQL(mTransactionHeader,
						"Tid=" + QT(cursor.getString(0)) + " and Upload='X'",
						false);
				db.deleteSQL(mTransactionDetail,
						"Tid=" + QT(cursor.getString(0)) + " and Upload='X'",
						false);
				cursor.close();
			}
			cursor.close();
			db.closeDB();
		} catch (Exception e) {
			Commons.printException(""+e);
			db.closeDB();
		}

	}

	// Before Day Close update the Uplaod Column to N to ready for Uplaod. by
	// default to X
	public void updateTable() {
		try {
			DBUtil db = new DBUtil(context, DataMembers.DB_NAME
            );
			db.createDataBase();
			db.openDataBase();
			db.updateSQL("UPDATE " + DataMembers.tbl_EmptyReconciliationHeader
					+ " SET Upload =  'N'");
			db.updateSQL("UPDATE " + DataMembers.tbl_EmptyReconciliationDetail
					+ " SET Upload =  'N'");
			db.closeDB();
		} catch (Exception e) {
			Commons.printException(""+e);
		}
	}

	public String QT(String data) {
		return "'" + data + "'";
	}

	private boolean isProductAvailableinSIHmaster(String productId) {
		DBUtil db = null;
		try {
			db = new DBUtil(context, DataMembers.DB_NAME);
			db.openDataBase();
			Cursor c = db.selectSQL("select qty from StockInHandMaster where pid=" + productId);
			if (c != null) {
				if (c.getCount() > 0) {
					return true;
				}
				c.close();
			}
		} catch (Exception e) {
			Commons.print(e.getMessage());
		} finally {
			db.closeDB();
		}
		return false;
	}

}
