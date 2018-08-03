package com.ivy.sd.png.provider;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.Cursor;
import android.location.Location;
import android.os.BatteryManager;

import com.ivy.core.data.app.AppDataProviderImpl;
import com.ivy.core.data.outlettime.OutletTimeStampDataManagerImpl;
import com.ivy.lib.existing.DBUtil;
import com.ivy.location.LocationUtil;
import com.ivy.sd.png.bo.UserMasterBO;
import com.ivy.sd.png.commons.SDUtil;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.util.Commons;
import com.ivy.sd.png.util.DataMembers;

import java.io.File;
import java.util.ArrayList;

public class OutletTimeStampHelper {

    private final Context context;
    private final BusinessModel bmodel;
    private String uid;

    // timein is used to update timeout
    private String timeIn;
    private String timeInModuleWise;

    private int lastRetailerSequence = 0;
    private double lastRetailerLattitude = 0;
    private double lastRetailerLongitude = 0;
    private int lastRetailerId = 0;

    private static OutletTimeStampHelper instance = null;

    private int getLastRetailerId() {
        return lastRetailerId;
    }

    public double getLastRetailerLattitude() {
        return lastRetailerLattitude;
    }

    public double getLastRetailerLongitude() {
        return lastRetailerLongitude;
    }

    private int getLastRetailerSequence() {
        return lastRetailerSequence;
    }

    private OutletTimeStampHelper(Context context) {
        this.context = context;
        this.bmodel = (BusinessModel) context;
    }

    public static OutletTimeStampHelper getInstance(Context context) {
        if (instance == null) {
            instance = new OutletTimeStampHelper(context);
        }
        return instance;
    }

    /**
     * @See {@link AppDataProviderImpl#getInTime()}
     * @deprecated
     */
    public String getTimeIn() {
        return timeIn;
    }

    /**
     * @param timeIn
     * @See {@link com.ivy.core.data.app.AppDataProviderImpl#setInTime(String)}
     * @deprecated
     */
    public void setTimeIn(String timeIn) {
        // Until all the code is refactored, Timein is updated in the Appdataprovider and business model
        bmodel.codeCleanUpUtil.setInTime(timeIn);
        this.timeIn = timeIn;
    }

    /**
     * @See {@link AppDataProviderImpl#getUniqueId()}
     * @deprecated
     */
    public String getUid() {
        return uid;
    }

    /**
     * @param uid Unique Identifier
     * @See {@link com.ivy.core.data.app.AppDataProviderImpl#setUniqueId(String)}
     * @deprecated
     */
    public void setUid(String uid) {
        // Until all the code is refactored, Unique is updated in the Appdataprovider and business model
        bmodel.codeCleanUpUtil.setUniqueId(uid);
        this.uid = uid;
    }

    private String QT(String data) {
        return "'" + data + "'";
    }

    public void setTimeInModuleWise(String timeInModuleWise) {
        this.timeInModuleWise = timeInModuleWise;
    }


    /**
     * Used to delete timeStamp.
     *
     * @See {@link OutletTimeStampDataManagerImpl#deleteTimeStamps()}
     * @deprecated
     */
    public void deleteTimeStamp() {

        try {
            DBUtil db = new DBUtil(context, DataMembers.DB_NAME,
                    DataMembers.DB_PATH);
            db.createDataBase();
            db.openDataBase();
            db.deleteSQL(DataMembers.tbl_OutletTimestamp, "retailerid="
                    + bmodel.retailerMasterBO.getRetailerID(), false);
            db.closeDB();
        } catch (Exception e) {
            Commons.printException(e);
        }
    }

    /**
     * Used to delete timeStamp.
     *
     * @See {@link OutletTimeStampDataManagerImpl#deleteTimeStamps()}
     * @deprecated
     */
    public void deleteTimeStampAllModule() {
        try {
            DBUtil db = new DBUtil(context, DataMembers.DB_NAME,
                    DataMembers.DB_PATH);
            db.createDataBase();
            db.openDataBase();
            db.deleteSQL(DataMembers.tbl_outlet_time_stamp_detail, "retailerid="
                    + bmodel.retailerMasterBO.getRetailerID() + " AND UID=" + getUid(), false);
            db.closeDB();
        } catch (Exception e) {
            Commons.printException(e);
        }
    }

    /**
     * Used to delete timeStamp.
     *
     * @See {@link OutletTimeStampDataManagerImpl#deleteTimeStamps()}
     * @deprecated
     */
    public void deleteTimeStampImages() {
        try {
            DBUtil db = new DBUtil(context, DataMembers.DB_NAME,
                    DataMembers.DB_PATH);
            db.createDataBase();
            db.openDataBase();
            db.deleteSQL(DataMembers.tbl_OutletTimestamp_images, "uid="
                    + getUid(), false);
            db.closeDB();
        } catch (Exception e) {
            Commons.printException(e);
        }
    }

	/**
	 * Used to set Time Stamp.
	 * 
	 * @param date date of last user visited retailer
	 * @param timeIn time of last user visited retailer
	 */
	public boolean saveTimeStamp(String date, String timeIn,float distance,String folderPath,String fName,String mVisitMode,String mNFCREasonId) {
		ArrayList<UserMasterBO> joinCallList=bmodel.userMasterHelper.getUserMasterBO().getJoinCallUserList();boolean sucessFlag=true;
        try {
		try {
			if(bmodel.configurationMasterHelper.IS_RETAILER_PHOTO_NEEDED)
			  saveOutletTimeStampImages(folderPath,fName);} catch (Exception e) {
                Commons.printException(e);
            }

			float dist = 0f;
            try {
                dist =LocationUtil.calculateDistance(
					bmodel.getRetailerMasterBO().getLatitude(), bmodel.getRetailerMasterBO().getLongitude());} catch (Exception e) {
                Commons.printException(e);
            }

			int joinCallFlag=0;
			if (isJointCall(joinCallList)) {  // check join call or not
                joinCallFlag = 1;
            }DBUtil db = new DBUtil(context, DataMembers.DB_NAME,
					DataMembers.DB_PATH);
			db.createDataBase();
			db.openDataBase();
			
			String columns = " VisitID , BeatID , VisitDate , RetailerID , TimeIn ,TimeOut,RetailerName,RetailerCode,latitude,longitude,JFlag,gpsaccuracy,gpsdistance,gpsCompliance,sequence,DistributorID,Battery,LocationProvider,IsLocationEnabled,IsDeviated,OrderValue";

            if (isJointCall(joinCallList)) {  // check join call or not
                joinCallFlag = 1;
            }

			String values = getUid() + ","
					+ bmodel.retailerMasterBO.getBeatID() + "," + QT(date)+ ","
					+  QT(bmodel.retailerMasterBO.getRetailerID()) + ","
					+ QT(date + " " + timeIn) + "," + QT(date + " " + timeIn)+ ","
					+  QT("0") + ","
					+ QT(bmodel.retailerMasterBO.getRetailerCode()) + ","
					+ QT(LocationUtil.latitude + "") + ","
					+ QT(LocationUtil.longitude + "")+","
					+ joinCallFlag+","
					+ QT(LocationUtil.accuracy+"")+","
					+ QT(distance+"")+","
					+ (dist<bmodel.getRetailerMasterBO().getGpsDistance()?1:0)+","
					+ (getLastRetailerId()==SDUtil.convertToInt(bmodel.getRetailerMasterBO().getRetailerID())? getLastRetailerSequence():(getLastRetailerSequence()+1))+ ","
					+bmodel.retailerMasterBO.getDistributorId()+ ","
					+getBatteryPercentage(context)+ ","
					+QT(LocationUtil.mProviderName)+ ","
					+QT(String.valueOf(bmodel.locationUtil.isGPSProviderEnabled()))+ ","
					+QT(String.valueOf(bmodel.retailerMasterBO.getIsDeviated()))+ ","
					+QT("0");

			db.insertSQL("OutletTimestamp", columns, values);
			
			if(joinCallFlag==1){  // insert join call details
				for(UserMasterBO userBo:joinCallList){
					if(userBo.getIsJointCall()==1){
				String joinCallColumns="timestampid,supid";
				
				String joinCallValues=getUid()+","+userBo.getUserid();
				db.insertSQL("OutletJoinCall", joinCallColumns, joinCallValues);
				}
				}
			}

            if (!("".equals(mVisitMode))) {
                String ret_columns = "UId, EntryMode, ReasonId, RetailerId";

                String ret_values = getUid() + "," + QT(mVisitMode) + "," + QT(mNFCREasonId) + "," + QT(bmodel.retailerMasterBO.getRetailerID());

                db.insertSQL("RetailerEntryDetails", ret_columns, ret_values);
            }

			db.closeDB();
		} catch (Exception e) {
			Commons.printException(e);
		sucessFlag = false;
        }
        return sucessFlag;
	}

    /**
     * Set Time Out
     *
     * @param timeOut    Module timeout
     * @param reasonDesc reason for closing
     */
    public void updateTimeStamp(String timeOut, String reasonDesc) {
        try {
            DBUtil db = new DBUtil(context, DataMembers.DB_NAME,
                    DataMembers.DB_PATH);
            db.createDataBase();
            db.openDataBase();
            String dateTime = SDUtil.now(SDUtil.DATE_GLOBAL) + " " + timeOut;
            String query = "UPDATE OutletTimeStamp SET TimeOut = '" + dateTime
                    + "',feedback=" + bmodel.QT(reasonDesc)
                    + ", OrderValue = " + QT(String.valueOf(bmodel.getOrderValue()))
                    + ", outLatitude = " + QT(LocationUtil.latitude + "")
                    + ", outLongitude = " + QT(LocationUtil.longitude + "")
                    + ", LocationProvider = " + QT(LocationUtil.mProviderName)
                    + ", gpsAccuracy = " + QT(LocationUtil.accuracy + "")
                    + ", Battery = " + getBatteryPercentage(context)
                    + ", IsLocationEnabled = " + QT(String.valueOf(bmodel.locationUtil.isGPSProviderEnabled()))
                    + ", IsDeviated = " + QT(String.valueOf(bmodel.retailerMasterBO.getIsDeviated()))
                    + "  WHERE RetailerID = '"
                    + bmodel.retailerMasterBO.getRetailerID()
                    + "' AND TimeIn = '" + getTimeIn() + "'";
            db.updateSQL(query);
            db.closeDB();
        } catch (Exception e) {
            Commons.printException(e);
        }
    }


    /**
     * Used to set Time Stamp.
     *
     * @param date   module start-in date
     * @param timeIn module start-in time
     * @See {@link com.ivy.core.data.outlettime.OutletTimeStampDataManagerImpl#saveTimeStampModuleWise(String, String, String)}
     * @deprecated This has been Migrated to MVP pattern
     */
    public void saveTimeStampModuleWise(String date, String timeIn, String moduleCode) {
        try {
            DBUtil db = new DBUtil(context, DataMembers.DB_NAME,
                    DataMembers.DB_PATH);
            db.createDataBase();
            db.openDataBase();

            timeInModuleWise = QT(date + " " + timeIn);

            bmodel.codeCleanUpUtil.setModuleTime(timeInModuleWise);
            String values = getUid() + ","
                    + QT(moduleCode) + ","
                    + timeInModuleWise + "," + timeInModuleWise
                    + ","
                    + QT(bmodel.retailerMasterBO.getRetailerID());
            db.insertSQL(DataMembers.tbl_outlet_time_stamp_detail, DataMembers.tbl_outlet_time_stamp_detail_cols, values);
            db.closeDB();
        } catch (Exception e) {
            Commons.printException(e);
        }
    }


    /**
     * @param timeOut module exit time
     * @See {@link com.ivy.core.data.outlettime.OutletTimeStampDataManagerImpl#updateTimeStampModuleWise(String)}
     * Set Time Out
     * @deprecated This has been Migrated to MVP pattern
     */
    public void updateTimeStampModuleWise(String timeOut) {
        try {
            DBUtil db = new DBUtil(context, DataMembers.DB_NAME,
                    DataMembers.DB_PATH);
            db.createDataBase();
            db.openDataBase();
            String dateTime = com.ivy.sd.png.commons.SDUtil.now(com.ivy.sd.png.commons.SDUtil.DATE_GLOBAL) + " " + timeOut;
            String query = "UPDATE OutletTimeStampDetail SET TimeOut = '" + dateTime
                    + "'  WHERE RetailerID = '"
                    + bmodel.retailerMasterBO.getRetailerID()
                    + "' AND TimeIn = " + timeInModuleWise + " AND UID = " + getUid();
            db.updateSQL(query);
            db.closeDB();
        } catch (Exception e) {
            Commons.printException(e);
        }
    }

    private void saveOutletTimeStampImages(String folderPath,
                                           String fNameStarts) {
        try {
            DBUtil db = new DBUtil(context, DataMembers.DB_NAME,
                    DataMembers.DB_PATH);
            db.createDataBase();
            db.openDataBase();

            File folder = new File(folderPath);
            //noinspection ConstantConditions
            if ((folder != null) || (folder.exists())) {
                String fnames[] = folder.list();
                if (fnames != null) {
                    String columns = "uid,imageName";
                    for (String str : fnames) {

                        if ((str != null) && (str.length() > 0)) {
                            if (str.startsWith(fNameStarts)) {
                                String values = getUid() + "," +
                                        QT("/Retail/"
                                                + bmodel.userMasterHelper.getUserMasterBO().getDownloadDate()
                                                .replace("/", "")
                                                + "/"
                                                + bmodel.userMasterHelper.getUserMasterBO()
                                                .getUserid() + "/"
                                                + str);
                                db.insertSQL("OutletTimestampImages", columns, values);
                            }
                        }
                    }
                }
            }
            db.closeDB();
        } catch (Exception e) {
            Commons.printException(e);
        }
    }

    public void getlastRetailerDatas() {
        try {
            DBUtil db = new DBUtil(context, DataMembers.DB_NAME,
                    DataMembers.DB_PATH);
            db.createDataBase();
            db.openDataBase();

            Cursor c = db.selectSQL("SELECT distinct retailerid,latitude,longitude,sequence FROM OutletTimestamp order by rowid"
            );
            if (c.getCount() > 0) {
                if (c.moveToLast()) {
                    lastRetailerId = c.getInt(0);
                    lastRetailerLattitude = c.getDouble(1);
                    lastRetailerLongitude = c.getDouble(2);
                    lastRetailerSequence = c.getInt(3);
                }
            }
            c.close();
            db.close();
        } catch (Exception e) {
            Commons.printException(e);
        }
    }

    /**
     * Method to update joint call inforamation module wise
     *
     * @param menuCode menu item code
     * @param uid      user id
     * @param oldUid   last user id
     */
    public void updateJointCallDetailsByModuleWise(String menuCode, String uid, String oldUid) {
        DBUtil db = null;
        try {
            db = new DBUtil(context, DataMembers.DB_NAME,
                    DataMembers.DB_PATH);
            db.createDataBase();
            db.openDataBase();
            StringBuilder sb = new StringBuilder();
            sb.append("select uid from ActivityJointCall where ");
            sb.append(" menuCode=" + bmodel.QT(menuCode));
            sb.append(" and uid=" + bmodel.QT(oldUid));
            Cursor c = db.selectSQL(sb.toString());
            if (c.getCount() > 0) {
                c.moveToNext();

                db.deleteSQL("ActivityJointCall",
                        "uid = " + QT(c.getString(0)), false);
            }

            ArrayList<UserMasterBO> joinCallUserList = bmodel.userMasterHelper
                    .getUserMasterBO().getJoinCallUserList();
            if (joinCallUserList != null) {
                String columns = "menucode,uid,supervisorid";
                for (UserMasterBO userMasterBO : joinCallUserList) {
                    if (userMasterBO.getIsJointCall() == 1) {
                        StringBuilder values = new StringBuilder();

                        values.append(bmodel.QT(menuCode) + "," + bmodel.QT(uid) + ",");
                        values.append(userMasterBO.getUserid());

                        db.insertSQL("ActivityJointCall", columns, values.toString());
                    }
                }
            }
        } catch (Exception e) {
            Commons.printException(e);
        } finally {
            if (db != null)
                db.closeDB();
        }
    }

    public boolean isJointCall(ArrayList<UserMasterBO> joinCallList) {
        if (joinCallList != null) {
            for (UserMasterBO userBO : joinCallList) {
                if (userBO.getIsJointCall() == 1)
                    return true;
            }
        }
        return false;
    }

    /**
     * @param retailerId
     * @return
     * @See {@link com.ivy.core.data.outlettime.OutletTimeStampDataManagerImpl#isVisited(String)}
     * @deprecated This has been Migrated to MVP pattern
     */
    public boolean isVisited(String retailerId) {
        DBUtil db = null;
        try {
            db = new DBUtil(context, DataMembers.DB_NAME,
                    DataMembers.DB_PATH);
            db.createDataBase();
            db.openDataBase();
            StringBuilder sb = new StringBuilder();
            sb.append("select VisitID from OutletTimestamp where ");
            sb.append(" RetailerID=" + bmodel.QT(retailerId));
            Cursor c = db.selectSQL(sb.toString());
            return c.getCount() > 0;
        } catch (Exception e) {
            Commons.printException(e);
            return false;
        } finally {
            if (db != null)
                db.closeDB();
        }
    }

    public void deleteTimeStampModuleWise(String modulecode) {
        try {
            DBUtil db = new DBUtil(context, DataMembers.DB_NAME,
                    DataMembers.DB_PATH);
            db.createDataBase();
            db.openDataBase();
            db.deleteSQL(DataMembers.tbl_outlet_time_stamp_detail, "retailerid="
                    + bmodel.retailerMasterBO.getRetailerID() + " AND UID=" + getUid() + " AND ModuleCode=" + QT(modulecode), false);
            db.closeDB();
        } catch (Exception e) {
            Commons.printException(e);
        }
    }

    /**
     * Get current battery percentage
     * @deprecated
     * @See {@link com.ivy.utils.DeviceUtils#getBatteryPercentage(Context)}
     */
    @Deprecated
    private int getBatteryPercentage(Context context) {

		int batteryPercentage = 0;
        try {IntentFilter iFilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
		Intent batteryStatus = context.registerReceiver(null, iFilter);

		int level = batteryStatus != null ? batteryStatus.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) : -1;
		int scale = batteryStatus != null ? batteryStatus.getIntExtra(BatteryManager.EXTRA_SCALE, -1) : -1;

		float batteryPct = level / (float) scale;

		batteryPercentage = (int) (batteryPct * 100);
	}catch (Exception e) {
            Commons.printException(e);
        }
        return batteryPercentage;

    }
}
