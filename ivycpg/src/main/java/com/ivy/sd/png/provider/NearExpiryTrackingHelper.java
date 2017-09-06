package com.ivy.sd.png.provider;


import android.content.Context;
import android.database.Cursor;

import com.ivy.lib.existing.DBUtil;
import com.ivy.sd.png.bo.ProductMasterBO;
import com.ivy.sd.png.commons.SDUtil;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.util.Commons;
import com.ivy.sd.png.util.DataMembers;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class NearExpiryTrackingHelper {

	private final Context context;
	private final BusinessModel bmodel;
	private static NearExpiryTrackingHelper instance = null;

	private final String mTrackingHeader = "NearExpiry_Tracking_Header";
	private final String mTrackingDetail = "NearExpiry_Tracking_Detail";

	public int mSelectedLocationIndex = 0;
	public String mSelectedLocationName = "";
	private int k = 0;


	private NearExpiryTrackingHelper(Context context) {
		this.context = context;
		this.bmodel = (BusinessModel) context;
	}

	public static NearExpiryTrackingHelper getInstance(Context context) {
		if (instance == null) {
			instance = new NearExpiryTrackingHelper(context);
		}
		return instance;
	}

	public void loadLastVisitSKUTracking() {
		DBUtil db = new DBUtil(context, DataMembers.DB_NAME, DataMembers.DB_PATH);
		try {
			db.openDataBase();

			String sql1 = "SELECT ProductId, LocId,expdate, UOMId, Qty,isOwn"
					+ " FROM LastVisitNearExpiry"
					+ " WHERE retailerid = "+bmodel.getRetailerMasterBO().getRetailerID();
			Cursor orderDetailCursor = db.selectSQL(sql1);
			if (orderDetailCursor != null) {
				int curLocId = 0;
				boolean isLocChanged;

				String curDateString = "";
				boolean isDateChanged;

				while (orderDetailCursor.moveToNext()) {
					String pid = orderDetailCursor.getString(0);
					int locationId = orderDetailCursor.getInt(1);
					String date = orderDetailCursor.getString(2);
					int uomId = orderDetailCursor.getInt(3);
					String uomQty = orderDetailCursor.getString(4);

					isLocChanged = false;
					isDateChanged = false;

					if (curLocId != locationId) {
						curLocId = locationId;
						isLocChanged = true;

						curDateString = date;

					} else if (!curDateString.equals(date)) {
							curDateString = date;
							isDateChanged = true;
						}


					setSKUTrackingDetails(pid, locationId,
							uomId, uomQty, date, isLocChanged,
							isDateChanged,false);
				}
				orderDetailCursor.close();
			}
			db.closeDB();
		} catch (Exception e) {
			Commons.printException(""+e);
			db.closeDB();
		}
	}

	public boolean hasAlreadySKUTrackingDone() {
		try {
			DBUtil db = new DBUtil(context, DataMembers.DB_NAME,
					DataMembers.DB_PATH);
			db.createDataBase();
			db.openDataBase();
			String sql = "select tid from "
					+ mTrackingHeader + " where RetailerID="
					+ bmodel.getRetailerMasterBO().getRetailerID();
			sql+=" AND date = "+ QT(SDUtil.now(SDUtil.DATE_GLOBAL));
			sql+=" and upload= 'N'";
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
			Commons.printException("hasAlreadySKUTrackinDone", e);
			return false;
		}
	}
	/**
	 * Load SKU from Detail Table
	 */
	public void loadSKUTracking(boolean isTaggedProduct) {
		DBUtil db = new DBUtil(context, DataMembers.DB_NAME, DataMembers.DB_PATH);
		try {
			db.openDataBase();
			k = -1;
			String tid;

            String sb = "SELECT Tid FROM " + mTrackingHeader+ " WHERE retailerid = "
						+bmodel.getRetailerMasterBO().getRetailerID()
						+" and (upload='N' OR refid!=0)";
			// Get Tid From Header


			Cursor orderHeaderCursor = db.selectSQL(sb);
			tid = "";
			if (orderHeaderCursor != null && orderHeaderCursor.moveToNext()) {
					tid = orderHeaderCursor.getString(0);
					orderHeaderCursor.close();
			}

			String sql1 = "SELECT PId, LocId,expdate, UOMId, UOMQty,IFNULL(Audit,'2'),isOwn"
					+ " FROM "
					+ mTrackingDetail
					+ " WHERE Tid = "
					+ QT(tid) + " order by pid, locid, expdate";
			Cursor orderDetailCursor = db.selectSQL(sql1);
			if (orderDetailCursor != null) {



				int curLocId = 0;
				boolean isLocChanged;

				String curDateString = "";
				boolean isDateChanged;

				while (orderDetailCursor.moveToNext()) {
					String pid = orderDetailCursor.getString(0);
					int locationId = orderDetailCursor.getInt(1);
					String date = orderDetailCursor.getString(2);
					int uomId = orderDetailCursor.getInt(3);
					String uomQty = orderDetailCursor.getString(4);


					isLocChanged = false;
					isDateChanged = false;

					if (curLocId != locationId) {
						curLocId = locationId;
						isLocChanged = true;

						curDateString = date;

					} else if (!curDateString.equals(date)) {
							curDateString = date;
							isDateChanged = true;
					}


					setSKUTrackingDetails(pid, locationId,
							uomId, uomQty, date, isLocChanged,
							isDateChanged,isTaggedProduct);
				}
				orderDetailCursor.close();
			}

			db.closeDB();
		} catch (Exception e) {
			Commons.printException(""+e);
			db.closeDB();
		}
	}

	/**
	 * Set the Tracking Detail
	 *
	 * @param pid
	 * @param --availabilty
	 * @param locationId
	 * @param uomId
	 * @param uomQty
	 */
	private void setSKUTrackingDetails(String pid, int locationId,
									   int uomId, String uomQty, String date,
									   boolean isLocChanged, boolean isDateChanged,boolean isTaggedProduct) {
		ProductMasterBO productBO;

		 if(isTaggedProduct) {
			 productBO = bmodel.productHelper.getTaggedProductBOById(pid);
		 }
		 else {
			 productBO = bmodel.productHelper.getProductMasterBOById(pid);
		 }
			if(productBO!=null){
				for (int j = 0; j < productBO.getLocations().size(); j++) {
					if (productBO.getLocations().get(j).getLocationId() == locationId) {

						if (isLocChanged) {
							k = 0;
						}

						if (isDateChanged) {
							k++;
						}

						productBO.getLocations().get(j).getNearexpiryDate()
								.get(k).setDate(changeMonthNoToName(date));

						if (productBO.getPcUomid() == uomId)
							productBO.getLocations().get(j).getNearexpiryDate()
									.get(k).setNearexpPC(uomQty);
						if (productBO.getOuUomid() == uomId)
							productBO.getLocations().get(j).getNearexpiryDate()
									.get(k).setNearexpOU(uomQty);
						if (productBO.getCaseUomId() == uomId)
							productBO.getLocations().get(j).getNearexpiryDate()
									.get(k).setNearexpCA(uomQty);

						return;
					}
				}
			}


	}

	/**
	 * Save Tracking Detail in Detail Table
	 */
	public void saveSKUTracking() {
		DBUtil db = new DBUtil(context, DataMembers.DB_NAME, DataMembers.DB_PATH);
		try {
			db.openDataBase();

			String tid;
			String sql;
			Cursor headerCursor;
			String refId = "0";

			String headerColumns = "Tid, RetailerId, Date, TimeZone, RefId";
			String detailColumns = "Tid, PId,LocId, UOMId, UOMQty,expdate,retailerid,audit";


			String values;
			boolean isData;



			tid = bmodel.userMasterHelper.getUserMasterBO().getUserid()
					+ ""
					+ bmodel.getRetailerMasterBO().getRetailerID()
					+ ""
					+ SDUtil.now(SDUtil.DATE_TIME_ID);

			// delete transaction if exist
			sql = "SELECT Tid, RefId FROM "
					+ mTrackingHeader
					+ " WHERE RetailerId = "
					+ bmodel.getRetailerMasterBO().getRetailerID();
			sql += " and (upload='N' OR refid!=0)";


			headerCursor = db.selectSQL(sql);

			if (headerCursor.getCount() > 0) {
				headerCursor.moveToNext();
				db.deleteSQL(mTrackingHeader,
						"Tid=" + QT(headerCursor.getString(0)), false);
				db.deleteSQL(mTrackingDetail,
						"Tid=" + QT(headerCursor.getString(0)), false);
				refId = headerCursor.getString(1);
				headerCursor.close();
			}

			// Saving Transaction Detail
			isData = false;
			for (ProductMasterBO skubo : bmodel.productHelper.getProductMaster()) {

				for (int j = 0; j < skubo.getLocations().size(); j++) {

					for (int k = 0; k < (skubo.getLocations()
							.get(j).getNearexpiryDate().size()); k++) {

						if (!"0"
								.equals(skubo.getLocations().get(j)
										.getNearexpiryDate()
										.get(k).getNearexpPC())||skubo.getLocations()
								.get(bmodel.mNearExpiryTrackingHelper.mSelectedLocationIndex).getAudit()!=2) {

							values = QT(tid)
									+ ","
									+ skubo.getProductID()
									+ ","
									+ skubo.getLocations()
									.get(j)
									.getLocationId()
									+ ","
									+ skubo.getPcUomid()
									+ ","
									+ skubo.getLocations()
									.get(j)
									.getNearexpiryDate()
									.get(k)
									.getNearexpPC()
									+ ","
									+ QT(changeMonthNameToNoyyyymmdd(skubo
									.getLocations()
									.get(j)
									.getNearexpiryDate()
									.get(k).getDate()))
									+ ","
									+ bmodel.getRetailerMasterBO()
									.getRetailerID()
									+ ","
									+ skubo.getLocations()
									.get(j).getAudit();

							db.insertSQL(mTrackingDetail,
									detailColumns, values);
							isData = true;
						}
						if (!"0"
								.equals(skubo.getLocations().get(j)
										.getNearexpiryDate()
										.get(k).getNearexpOU())||skubo.getLocations()
								.get(bmodel.mNearExpiryTrackingHelper.mSelectedLocationIndex).getAudit()!=2) {
							values = QT(tid)
									+ ","
									+ skubo.getProductID()
									+ ","
									+ skubo.getLocations()
									.get(j)
									.getLocationId()
									+ ","
									+ skubo.getOuUomid()
									+ ","
									+ skubo.getLocations()
									.get(j)
									.getNearexpiryDate()
									.get(k)
									.getNearexpOU()
									+ ","
									+ QT(changeMonthNameToNoyyyymmdd(skubo
									.getLocations()
									.get(j)
									.getNearexpiryDate()
									.get(k).getDate()))
									+ ","
									+ bmodel.getRetailerMasterBO()
									.getRetailerID()
									+ ","
									+ skubo.getLocations()
									.get(j).getAudit();

							db.insertSQL(mTrackingDetail,
									detailColumns, values);
							isData = true;
						}
						if ( !"0"
								.equals(skubo.getLocations().get(j)
										.getNearexpiryDate()
										.get(k).getNearexpCA())||skubo.getLocations()
								.get(bmodel.mNearExpiryTrackingHelper.mSelectedLocationIndex).getAudit()!=2) {
							values = QT(tid)
									+ ","
									+ skubo.getProductID()
									+ ","
									+ skubo.getLocations()
									.get(j)
									.getLocationId()
									+ ","
									+ skubo.getCaseUomId()
									+ ","
									+ skubo.getLocations()
									.get(j)
									.getNearexpiryDate()
									.get(k)
									.getNearexpCA()
									+ ","
									+ QT(changeMonthNameToNoyyyymmdd(skubo
									.getLocations()
									.get(j)
									.getNearexpiryDate()
									.get(k).getDate()))
									+ ","
									+ bmodel.getRetailerMasterBO()
									.getRetailerID()
									+ ","
									+ skubo.getLocations()
									.get(j).getAudit();
							db.insertSQL(mTrackingDetail,
									detailColumns, values);
							isData = true;
						}
					}

				}
			}

			// Saving Transaction Header if There is Any Detail
			if (isData) {
				values = QT(tid)
						+ ","
						+ bmodel.getRetailerMasterBO().getRetailerID()
						+ ","
						+ QT(SDUtil.now(SDUtil.DATE_GLOBAL)) + ","
						+ QT(bmodel.getTimeZone()) + "," + QT(refId);

				db.insertSQL(mTrackingHeader, headerColumns, values);
			}

			db.closeDB();
		} catch (Exception e) {
			Commons.printException(""+e);
			db.closeDB();
		}
	}

	public boolean checkDataToSave() {

		for (ProductMasterBO skubo : bmodel.productHelper.getProductMaster()) {
			for (int j = 0; j < skubo.getLocations().size(); j++) {
				for (int k = 0; k < (skubo.getLocations().get(j)
						.getNearexpiryDate().size()); k++) {
					if (!"0".equals(skubo.getLocations().get(j).getNearexpiryDate()
							.get(k).getNearexpPC())
							|| !"0".equals(skubo.getLocations().get(j).getNearexpiryDate()
							.get(k).getNearexpOU())
							|| !"0".equals(skubo.getLocations().get(j).getNearexpiryDate()
							.get(k).getNearexpCA())
							||skubo.getLocations()
							.get(bmodel.mNearExpiryTrackingHelper.mSelectedLocationIndex).getAudit()!=2)
						return true;
				}
			}
		}
		return false;
	}

	public String dateformat(int year, int monthOfYear, int dayOfMonth) {
		String month;
		String day;

		if (monthOfYear + 1 < 9)
			month = "0" + (monthOfYear + 1);
		else
			month = Integer.toString(monthOfYear + 1);

		if (dayOfMonth < 10)
			day = "0" + dayOfMonth;
		else
			day = Integer.toString(dayOfMonth);

		return year + "/" + month + "/" + day;

	}

	public String changeMonthNameToNoyyyymmdd(String date) {

		if (null != date && !"".equals(date))
				try {
					String[] dat = date.split(" ");

					SimpleDateFormat cf = new SimpleDateFormat("dd/MMM/yyyy",
							Locale.ENGLISH);
					Date dt = cf.parse(dat[0]);
					SimpleDateFormat sf = new SimpleDateFormat("yyyy/MM/dd",
							Locale.ENGLISH);
					return sf.format(dt);
				} catch (Exception e) {
					Commons.printException(""+e);
				}

		return "";
	}

	public String changeMonthNoToName(String date) {

		if (null != date && !"".equals(date))
				try {
					String[] dat = date.split(" ");

					SimpleDateFormat cf = new SimpleDateFormat("yyyy/MM/dd",
							Locale.ENGLISH);
					Date dt = cf.parse(dat[0]);
					SimpleDateFormat sf = new SimpleDateFormat("dd/MMM/yyyy",
							Locale.ENGLISH);
					return sf.format(dt);
				} catch (Exception e) {
					Commons.printException(""+e);
				}

		return "";
	}

	public String changeDate(String date) {

		if (null != date && !"".equals(date))
				try {
					String[] dat = date.split(" ");

					SimpleDateFormat cf = new SimpleDateFormat("yyyy/MM/dd",
							Locale.ENGLISH);
					Date dt = cf.parse(dat[0]);
					SimpleDateFormat sf = new SimpleDateFormat("MM/dd/yyyy",
							Locale.ENGLISH);
					return sf.format(dt);
				} catch (Exception e) {
					Commons.printException(""+e);
				}

		return "";
	}

	public String changeMonthNameToNommddyyyy(String date) {

		if (null != date && !"".equals(date))
				try {
					String[] dat = date.split(" ");

					SimpleDateFormat cf = new SimpleDateFormat("dd/MMM/yyyy",
							Locale.ENGLISH);
					Date dt = cf.parse(dat[0]);
					SimpleDateFormat sf = new SimpleDateFormat("MM/dd/yyyy",
							Locale.ENGLISH);
					return sf.format(dt);
				} catch (Exception e) {
					Commons.printException(""+e);
				}

		return "";
	}

	private String QT(String data) {
		return "'" + data + "'";
	}
}
