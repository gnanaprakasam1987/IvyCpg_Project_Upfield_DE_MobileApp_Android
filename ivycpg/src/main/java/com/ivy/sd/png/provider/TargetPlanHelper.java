package com.ivy.sd.png.provider;

import android.content.Context;
import android.database.Cursor;

import com.ivy.lib.existing.DBUtil;
import com.ivy.sd.png.bo.TargetPlanBO;
import com.ivy.sd.png.commons.SDUtil;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.util.Commons;
import com.ivy.sd.png.util.DataMembers;

public class TargetPlanHelper {

	private Context context;
	private TargetPlanBO targetplanBO;



	private BusinessModel bmodel;

	private static TargetPlanHelper instance = null;

	protected TargetPlanHelper(Context context) {
		this.context = context;
		this.bmodel = (BusinessModel) context;
	}

	public static TargetPlanHelper getInstance(Context context) {
		if (instance == null) {
			instance = new TargetPlanHelper(context);
		}
		return instance;

	}

	/**
	 * Download target plan details of a specific retailer.
	 */
	public void downloadTargetPlan() {
		try {
			targetplanBO = new TargetPlanBO();
			DBUtil db = new DBUtil(context, DataMembers.DB_NAME,
					DataMembers.DB_PATH);
			db.openDataBase();
			Cursor c = db
					.selectSQL("select retailerId,base_value,sbd,base_edt,sbd_edt,suggestedTgt from "
							+ DataMembers.tbl_DTPMaster
							+ " where retailerID = "
							+ bmodel.retailerMasterBO.getRetailerID());

			if (c != null) {
				if (c.moveToNext()) {
					targetplanBO.setRetailerid(c.getString(0));
					targetplanBO.setBase(c.getDouble(1));
					targetplanBO.setSbd(c.getDouble(2));
					targetplanBO.setBaseEdit(c.getDouble(3));
					targetplanBO.setSbdEdit(c.getDouble(4));
					targetplanBO.setSuggestTraget(c.getDouble(5));
				}
			}
			c.close();

			c = db.selectSQL("SELECT IFNULL(hvp3m,0) FROM RetailerMaster WHERE RetailerID = "
					+ bmodel.retailerMasterBO.getRetailerID());

			if (c != null) {
				if (c.moveToNext()) {
					targetplanBO.setHvp3m(c.getDouble(0));
				}
			}
			c.close();

			c = db.selectSQL("select InitId,InitDesc,balance,balanceedit from InitiativeBalance where retailerID = "
					+ bmodel.retailerMasterBO.getRetailerID() + " limit 10");

			int count = 0;
			if (c != null) {
				while (c.moveToNext()) {

					switch (count) {
					case 0:
						targetplanBO.setInitId1(c.getInt(0));
						targetplanBO.setInit1Desc(c.getString(1));
						targetplanBO.setInitBalance1(c.getDouble(2));
						targetplanBO.setInitBalance1Edit(c.getDouble(3));
						break;

					case 1:
						targetplanBO.setInitId2(c.getInt(0));
						targetplanBO.setInit2Desc(c.getString(1));
						targetplanBO.setInitBalance2(c.getDouble(2));
						targetplanBO.setInitBalance2Edit(c.getDouble(3));
						break;

					case 2:
						targetplanBO.setInitId3(c.getInt(0));
						targetplanBO.setInit3Desc(c.getString(1));
						targetplanBO.setInitBalance3(c.getDouble(2));
						targetplanBO.setInitBalance3Edit(c.getDouble(3));
						break;
					case 3:
						targetplanBO.setInitId4(c.getInt(0));
						targetplanBO.setInit4Desc(c.getString(1));
						targetplanBO.setInitBalance4(c.getDouble(2));
						targetplanBO.setInitBalance4Edit(c.getDouble(3));
						break;
					case 4:
						targetplanBO.setInitId5(c.getInt(0));
						targetplanBO.setInit5Desc(c.getString(1));
						targetplanBO.setInitBalance5(c.getDouble(2));
						targetplanBO.setInitBalance5Edit(c.getDouble(3));
						break;
					case 6:
						targetplanBO.setInitId7(c.getInt(0));
						targetplanBO.setInit7Desc(c.getString(1));
						targetplanBO.setInit7(c.getDouble(2));
						targetplanBO.setInit7_edt(c.getDouble(3));
						break;
					case 7:
						targetplanBO.setInitId8(c.getInt(0));
						targetplanBO.setInit8Desc(c.getString(1));
						targetplanBO.setInit8(c.getDouble(2));
						targetplanBO.setInit8_edt(c.getDouble(3));
						break;
					case 8:
						targetplanBO.setInitId9(c.getInt(0));
						targetplanBO.setInit9Desc(c.getString(1));
						targetplanBO.setInit9(c.getDouble(2));
						targetplanBO.setInit9_edt(c.getDouble(3));
						break;
					case 9:
						targetplanBO.setInitId10(c.getInt(0));
						targetplanBO.setInit10Desc(c.getString(1));
						targetplanBO.setInit10(c.getDouble(2));
						targetplanBO.setInit10_edt(c.getDouble(3));
						break;
					default:
						break;
					}

					count = count + 1;
				}
			}
			c.close();

			db.closeDB();
		} catch (Exception e) {
			Commons.printException(e);
		}
	}

	public boolean hasDataInDTPMaster() {

		try {
			targetplanBO = new TargetPlanBO();
			DBUtil db = new DBUtil(context, DataMembers.DB_NAME,
					DataMembers.DB_PATH);
			db.openDataBase();
			Cursor c = db.selectSQL("select * from "
					+ DataMembers.tbl_DTPMaster + " where retailerID = "
					+ bmodel.retailerMasterBO.getRetailerID());

			if (c != null) {
				if (c.moveToNext()) {
					c.close();
					db.closeDB();
					return true;
				}
			}
			c.close();
			db.closeDB();
		} catch (Exception e) {
			// TODO: handle exception
		}
		return false;

	}

	/**
	 * This method will called as soon as download completed and after
	 * initiative balance calculation. This will insert the DAILY_TARGET_PLANNED
	 * from RetailerMaster to DailyTargetPlanned table for upload.
	 */
	/*public void updateDailyTargetPlanned() {
		try {
			DBUtil db = new DBUtil(context, DataMembers.DB_NAME,
					DataMembers.DB_PATH);
			db.openDataBase();
			Cursor c = db
					.selectSQL("select RM.RetailerId,RM.DAILY_TARGET_PLANNED from Retailermaster RM inner join RetailerMasterInfo RMI"
							+ " on RM.RetailerId=RMI.RetailerId  where RMI.isToday=1");
		
			int i = 0;
			if (c != null) {
				while (c.moveToNext()) {
					i++;
					String uid = QT(bmodel.userMasterHelper.getUserMasterBO()
							.getUserid() + i + SDUtil.now(SDUtil.DATE_TIME_ID));
					String value = uid
							+ ", "
							+ c.getString(0)
							+ ", "
							+ c.getDouble(1)
							+ ", "
							+ QT(bmodel.userMasterHelper.getUserMasterBO()
									.getDownloadDate());
					Commons.print("target value" + c.getFloat(1));
					db.executeQ("insert into DailyTargetPlanned (TargetID,RetailerID,TargetValue,Date) values("
							+ value + ")");
				}
				c.close();
			}
			db.closeDB();
		} catch (Exception e) {
			Commons.printException(e);
		}
	}*/

	/**
	 * This method will save the Planned Daily target in DailytargetMaster.
	 * 
	 * @param retailerid
	 */
	public void saveDailyTrgetPalnned(String retailerid) {
		try {
			DBUtil db = new DBUtil(context, DataMembers.DB_NAME,
					DataMembers.DB_PATH);
			db.openDataBase();
			// TargetID" TEXT, "RetailerID" nVarchar(20), "TargetValue"  float, "Date"
			// TEXT
			Cursor c = db.selectSQL("select * from "
					+ DataMembers.tbl_DailyTargetPlanned
					+ " where RetailerID= " + retailerid);

			if (c != null) {

				if (c.getCount() > 0) {
					db.executeQ("update "
							+ DataMembers.tbl_DailyTargetPlanned
							+ " set TargetValue= "
							+ bmodel.getRetailerMasterBO()
									.getDaily_target_planned()
							+ " , upload='N' " + " where RetailerID ="
							+ bmodel.getRetailerMasterBO().getRetailerID());

				} else {
					String columns = "TargetID, RetailerID, TargetValue, Date";

					String dayPlanningID = QT("DTP"
							+ bmodel.userMasterHelper.getUserMasterBO()
									.getUserid()
							+ SDUtil.now(SDUtil.DATE_TIME_ID));

					String values = dayPlanningID
							+ ", "
							+ bmodel.getRetailerMasterBO().getRetailerID()
							+ ", "
							+ bmodel.getRetailerMasterBO()
									.getDaily_target_planned()
							+ ", "
							+ QT(bmodel.userMasterHelper.getUserMasterBO()
									.getDownloadDate());

					db.insertSQL(DataMembers.tbl_DailyTargetPlanned, columns,
							values);

				}

			}
			db.closeDB();
		} catch (Exception e) {
			// TODO: handle exception
			Commons.printException(e);
		}

	}

	public String QT(String data) // Quote
	{
		return "'" + data + "'";
	}

	/**
	 * Store the Daily_Targte_Planned value in RetailerMaster
	 */
	public void updateDailyTargetPlan() {
		try {
			DBUtil db = new DBUtil(context, DataMembers.DB_NAME,
					DataMembers.DB_PATH);
			db.openDataBase();
			db.executeQ("update retailerMaster set daily_target_planned= "
					+ bmodel.getRetailerMasterBO().getDaily_target_planned()
					+ " where retailerid ="
					+ bmodel.getRetailerMasterBO().getRetailerID()
					+ " and beatid= "
					+ bmodel.getRetailerMasterBO().getBeatID());
			db.closeDB();

		} catch (Exception e) {
			Commons.printException(e);
		}
	}

	public void updateTargetPlanEdit(String retailerid) {
		try {
			DBUtil db = new DBUtil(context, DataMembers.DB_NAME,
					DataMembers.DB_PATH);
			db.openDataBase();

			Cursor c = db.selectSQL("select * from "
					+ DataMembers.tbl_DTPMaster + " where retailerID= "
					+ retailerid);

			if (c != null) {

				if (c.getCount() > 0) {

					db.executeQ("update DTPMaster set base_edt= "
							+ targetplanBO.getBaseEdit() + ",sbd_edt= "
							+ targetplanBO.getSbdEdit() + " where retailerid ="
							+ retailerid);

				}
			}

			db.executeQ("update InitiativeBalance set balanceEdit="
					+ targetplanBO.getInitBalnce1Edit() + " where retailerid="
					+ retailerid + " and initId=" + targetplanBO.getInitId1());
			db.executeQ("update InitiativeBalance set balanceEdit="
					+ targetplanBO.getInitBalnce2Edit() + " where retailerid="
					+ retailerid + " and initId=" + targetplanBO.getInitId2());
			db.executeQ("update InitiativeBalance set balanceEdit="
					+ targetplanBO.getInitBalnce3Edit() + " where retailerid="
					+ retailerid + " and initId=" + targetplanBO.getInitId3());
			db.executeQ("update InitiativeBalance set balanceEdit="
					+ targetplanBO.getInitBalnce4Edit() + " where retailerid="
					+ retailerid + " and initId=" + targetplanBO.getInitId4());
			db.executeQ("update InitiativeBalance set balanceEdit="
					+ targetplanBO.getInitBalnce5Edit() + " where retailerid="
					+ retailerid + " and initId=" + targetplanBO.getInitId5());
			db.executeQ("update InitiativeBalance set balanceEdit="
					+ targetplanBO.getInit6_edt() + " where retailerid="
					+ retailerid + " and initId=" + targetplanBO.getInitId6());
			db.executeQ("update InitiativeBalance set balanceEdit="
					+ targetplanBO.getInit7_edt() + " where retailerid="
					+ retailerid + " and initId=" + targetplanBO.getInitId7());
			db.executeQ("update InitiativeBalance set balanceEdit="
					+ targetplanBO.getInit8_edt() + " where retailerid="
					+ retailerid + " and initId=" + targetplanBO.getInitId8());
			db.executeQ("update InitiativeBalance set balanceEdit="
					+ targetplanBO.getInit9_edt() + " where retailerid="
					+ retailerid + " and initId=" + targetplanBO.getInitId9());
			db.executeQ("update InitiativeBalance set balanceEdit="
					+ targetplanBO.getInit10_edt() + " where retailerid="
					+ retailerid + " and initId=" + targetplanBO.getInitId10());
			c.close();
			db.closeDB();

		} catch (Exception e) {
			Commons.printException(e);
		}
	}

	/**
	 * This method will update base_edit,sbd_edit by base and sbd value received
	 * from server.
	 */
	/*public void updateDTPMaster() {
		try {
			DBUtil db = new DBUtil(context, DataMembers.DB_NAME,
					DataMembers.DB_PATH);
			db.openDataBase();
			// Set base value
			db.executeQ("update DTPMaster set base_edt=base_value , sbd_edt=sbd");
			db.closeDB();
		} catch (Exception e) {
			Commons.printException(e);
		}
	}*/

	public TargetPlanBO getTargetplanBO() {
		return targetplanBO;
	}

	public void setTargetplanBO(TargetPlanBO targetplanBO) {
		this.targetplanBO = targetplanBO;
	}

}
