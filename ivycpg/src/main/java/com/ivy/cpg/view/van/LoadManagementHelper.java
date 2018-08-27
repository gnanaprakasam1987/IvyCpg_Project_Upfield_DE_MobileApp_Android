package com.ivy.cpg.view.van;

import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.SQLException;

import com.ivy.lib.existing.DBUtil;
import com.ivy.location.LocationUtil;
import com.ivy.sd.png.bo.BomBO;
import com.ivy.sd.png.bo.BomMasterBO;
import com.ivy.sd.png.bo.BomReturnBO;
import com.ivy.sd.png.bo.LoadManagementBO;
import com.ivy.sd.png.bo.SubDepotBo;
import com.ivy.sd.png.bo.VanLoadMasterBO;
import com.ivy.sd.png.commons.SDUtil;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.util.Commons;
import com.ivy.sd.png.util.DataMembers;

import java.util.ArrayList;
import java.util.Vector;

public class LoadManagementHelper {

    private static LoadManagementHelper instance = null;
    private Context context;
    private BusinessModel bmodel;
    private ArrayList<SubDepotBo> subDepotList = null;
    private ArrayList<SubDepotBo> distributorList = null;

    public LoadManagementHelper(Context context) {
        this.context = context;
        this.bmodel = (BusinessModel) context;

    }

    public static LoadManagementHelper getInstance(Context context) {
        if (instance == null) {
            instance = new LoadManagementHelper(context);
        }
        return instance;
    }


    /**
     * DownLoad the SubDepots from Distribution Master
     */
    public void downloadSubDepots() {
        SubDepotBo subDepots;
        DBUtil db = null;
        Cursor cursor;
        try {
            db = new DBUtil(context, DataMembers.DB_NAME, DataMembers.DB_PATH);
            db.openDataBase();
            db.createDataBase();

            cursor = db.selectSQL("Select DISTINCT Did,Dname,IFNULL(CNumber,''),IFNULL(Address1,''),IFNULL(Address2,''),IFNULL(Address3,''), type from DistributorMaster");
            if (cursor != null) {
                subDepotList = new ArrayList<>();
                distributorList = new ArrayList<>();


                subDepots = new SubDepotBo();
                subDepots.setSubDepotId(0);
                subDepots.setdName("Select Distributor");
                subDepotList.add(subDepots);
                distributorList.add(subDepots);

                while (cursor.moveToNext()) {
                    subDepots = new SubDepotBo();

                    subDepots.setSubDepotId(cursor.getInt(0));

                    subDepots.setContactNumber(cursor.getString(2));
                    subDepots.setAddress1(cursor.getString(3));
                    subDepots.setAddress2(cursor.getString(4));
                    subDepots.setAddress3(cursor.getString(5));

                    String type = cursor.getString(6);
                    if (type != null && type.equalsIgnoreCase("distributor")) {
                        if (bmodel.userMasterHelper.getUserMasterBO().getDistributorid() == subDepots.getSubDepotId()) {
                            subDepots.setdName(cursor.getString(1)
                                    + "- Primary");
                        } else {
                            subDepots.setdName(cursor.getString(1)
                                    + "- Secondary");
                        }
                        distributorList.add(subDepots);

                    } else {
                        subDepots.setdName(cursor.getString(1));
                        subDepotList.add(subDepots);
                    }


                }

                cursor.close();
            }
            db.closeDB();
        } catch (Exception e) {
            Commons.printException(e);
            if (db != null) {
                db.closeDB();
            }
        }
    }

    public VanLoadMasterBO downloadOdameter() {
        VanLoadMasterBO temp = null;
        try {
            DBUtil db = new DBUtil(context, DataMembers.DB_NAME,
                    DataMembers.DB_PATH);
            db.openDataBase();
            Cursor c = db
                    .selectSQL("SELECT o.uid,o.date,o.start,o.end,o.isended,o.isstarted,o.starttime,o.endtime,IFNULL(o.startImage,''),IFNULL(O.endImage,'') FROM Odameter o");
            if (c != null) {

                while (c.moveToNext()) {
                    temp = new VanLoadMasterBO();
                    temp.setOdameteruid(c.getInt(0));
                    temp.setOdameterdate(c.getString(1));
                    temp.setOdameterstart(c.getDouble(2));
                    temp.setOdameterend(c.getDouble(3));
                    temp.setIsended(c.getInt(4));
                    temp.setIsstarted(c.getInt(5));
                    temp.setStartdatetime(c.getString(6));
                    temp.setEndtime(c.getString(7));

                    if (c.getString(8).length() > 0) {
                        String[] imjObj = c.getString(8).split("/");
                        if (imjObj.length > 3)
                            temp.setStartTripImg(imjObj[3]);
                    }
                    if (c.getString(9).length() > 0) {
                        String[] imjObj = c.getString(9).split("/");
                        if (imjObj.length > 3)
                            temp.setEndTripImg(imjObj[3]);
                    }

                }
                c.close();
            }
            db.closeDB();
        } catch (Exception e) {
            Commons.printException(e);
        }
        return temp;
    }

    public ArrayList<VanLoadMasterBO> downloadExistingUid() {
        DBUtil db = null;
        ArrayList<VanLoadMasterBO> mUidList = new ArrayList<>();
        VanLoadMasterBO vanBo;
        try {
            db = new DBUtil(context, DataMembers.DB_NAME, DataMembers.DB_PATH);
            db.openDataBase();
            Cursor cursor = db
                    .selectSQL("SELECT Distinct Uid FROM VanLoad ORDER BY Uid");
            if (cursor != null) {
                while (cursor.moveToNext()) {
                    vanBo = new VanLoadMasterBO();
                    vanBo.setRfield1(cursor.getString(0));
                    mUidList.add(vanBo);
                }
                cursor.close();
            }
            db.closeDB();
        } catch (Exception e) {
            Commons.printException(e);
            if (db != null) {
                db.closeDB();
            }
        }
        return mUidList;

    }


    public ArrayList<SubDepotBo> getSubDepotList() {
        return subDepotList;
    }

    public ArrayList<SubDepotBo> getDistributorList() {
        if (distributorList != null) {
            return distributorList;
        }
        return new ArrayList<>();
    }




    public boolean isSecondaryDistributorDone() {
        DBUtil db = null;
        Cursor cursor;
        try {
            db = new DBUtil(context, DataMembers.DB_NAME, DataMembers.DB_PATH);
            db.openDataBase();
            db.createDataBase();
            for (SubDepotBo bo : getDistributorList()) {
                if (bmodel.userMasterHelper.getUserMasterBO()
                        .getDistributorid() != bo.getSubDepotId()) {
                    cursor = db
                            .selectSQL("Select pid from vanload where subdepotid="
                                    + bo.getSubDepotId());
                    if (cursor != null) {
                        if (cursor.getCount() > 0) {
                            return true;
                        }
                        cursor.close();

                    }
                }
            }

            db.closeDB();
        } catch (Exception e) {
            Commons.printException(e);
            if (db != null) {
                db.closeDB();
            }
        }
        return false;
    }



    public float checkIsAllowed(String menuString) {
        try {
            DBUtil db = new DBUtil(context,
                    DataMembers.DB_NAME, DataMembers.DB_PATH);
            db.createDataBase();
            db.openDataBase();
            Cursor c = db
                    .selectSQL("SELECT latitude, longitude FROM WarehouseActivityMapping WHERE activity_code = "
                            + DatabaseUtils.sqlEscapeString(menuString));
            double wareLatitude = 0;
            double wareLongitude = 0;
            if (c != null) {
                if (c.moveToNext()) {
                    wareLatitude = c.getDouble(0);
                    wareLongitude = c.getDouble(1);
                }
                c.close();
            }
            db.closeDB();

            if (wareLatitude == 0 && wareLongitude == 0) {
                return -1;
            } else if (LocationUtil.latitude == 0
                    && LocationUtil.longitude == 0) {
                return -2;
            } else {

                float distance = LocationUtil.calculateDistance(wareLatitude,
                        wareLongitude);
                return distance;
            }
        } catch (Exception e) {
            Commons.printException("" + e);
        }
        return -3;
    }


}
