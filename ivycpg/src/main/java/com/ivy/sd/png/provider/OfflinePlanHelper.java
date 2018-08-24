package com.ivy.sd.png.provider;

import android.content.Context;
import android.database.Cursor;

import com.ivy.lib.existing.DBUtil;
import com.ivy.cpg.view.nonfield.NonFieldBO;
import com.ivy.sd.png.bo.OfflineDateWisePlanBO;
import com.ivy.sd.png.util.Commons;
import com.ivy.sd.png.util.DataMembers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Vector;

/**
 * Created by rahul.j on 3/14/2018.
 */

public class OfflinePlanHelper {
    private static OfflinePlanHelper instance = null;
    private Context context;
    private HashMap<String, ArrayList<OfflineDateWisePlanBO>> mHashMapData = new HashMap<>();
    private ArrayList<OfflineDateWisePlanBO> offlineList = new ArrayList<>();


    public OfflinePlanHelper(Context context) {
        this.context = context;
    }

    public static OfflinePlanHelper getInstance(Context context) {
        if (instance == null) {
            instance = new OfflinePlanHelper(context);
        }
        return instance;
    }


    public void downloadOfflinePlanList() {
        DBUtil db = new DBUtil(context, DataMembers.DB_NAME,
                DataMembers.DB_PATH);
        if (mHashMapData != null && mHashMapData.size() > 0) {
            mHashMapData.clear();
        }
        if (offlineList != null && offlineList.size() > 0) {
            offlineList.clear();
        }

        try {

            String sql = "SELECT PlanId,DistributorId,UserId,Date,EntityId,EntityType,Status,Sequence," +
                    "(CASE WHEN EntityType = 'RETAILER' THEN IFNULL((SELECT RetailerName from RetailerMaster where RetailerID = EntityId),'')" +
                    " WHEN EntityType = 'DIST' THEN IFNULL((SELECT RetailerName from RetailerMaster where SubDId = EntityId),'')" +
                    " WHEN EntityType = 'NFA' THEN IFNULL((SELECT ListName from StandardListMaster where ListId = EntityId),'')" +
                    " ELSE '' END) as Name FROM " + DataMembers.tbl_date_wise_plan + " Where status != 'D' ";

            db.openDataBase();

            Cursor c = db.selectSQL(sql);
            if (c != null && c.getCount() != 0) {
                OfflineDateWisePlanBO dateWisePlanBO;
                mHashMapData = new HashMap<>();
                offlineList = new ArrayList<>();

                while (c.moveToNext()) {
                    dateWisePlanBO = new OfflineDateWisePlanBO();

                    dateWisePlanBO.setPlanId(c.getInt(0));
                    dateWisePlanBO.setDistributorId(c.getInt(1));
                    dateWisePlanBO.setUserId(c.getInt(2));
                    dateWisePlanBO.setDate(c.getString(3));
                    dateWisePlanBO.setEntityId(c.getInt(4));
                    dateWisePlanBO.setEntityType(c.getString(5));
                    dateWisePlanBO.setStatus(c.getString(6));
                    dateWisePlanBO.setSequence(c.getInt(7));
                    dateWisePlanBO.setName(c.getString(c.getColumnIndex("Name")));

                    if (mHashMapData.get(dateWisePlanBO.getDate()) == null) {
                        if (!c.getString(c.getColumnIndex("Status")).equals("D")) {
                            ArrayList<OfflineDateWisePlanBO> plannedList = new ArrayList<>();
                            plannedList.add(dateWisePlanBO);
                            mHashMapData.put(dateWisePlanBO.getDate(), plannedList);
                        }
                    } else {
                        if (!c.getString(c.getColumnIndex("Status")).equals("D")) {
                            ArrayList<OfflineDateWisePlanBO> plannedList = mHashMapData.get(dateWisePlanBO.getDate());
                            plannedList.add(dateWisePlanBO);
                            mHashMapData.put(dateWisePlanBO.getDate(), plannedList);
                        }
                    }
                    offlineList.add(dateWisePlanBO);
                }
                c.close();
            }

        } catch (Exception e) {
            Commons.printException("Unable to get dateWisePlan " + e);
        }
        db.closeDB();
    }


    public HashMap<String, ArrayList<OfflineDateWisePlanBO>> getmHashMapData() {
        if (mHashMapData == null) {
            return new HashMap<>();
        }
        return mHashMapData;
    }

    public void setmHashMapData(HashMap<String, ArrayList<OfflineDateWisePlanBO>> mHashMapData) {
        this.mHashMapData = mHashMapData;
    }

    public ArrayList<OfflineDateWisePlanBO> getCallSchedulingList() {
        return offlineList;
    }

    public void savePlan(Context mContext, OfflineDateWisePlanBO offlineDateWisePlanBO) {
        DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME, DataMembers.DB_PATH);
        try {
            db.openDataBase();

            String values = offlineDateWisePlanBO.getPlanId() + ","
                    + offlineDateWisePlanBO.getDistributorId() + ","
                    + offlineDateWisePlanBO.getUserId() + ","
                    + QT(offlineDateWisePlanBO.getDate()) + ","
                    + offlineDateWisePlanBO.getEntityId() + ","
                    + QT(offlineDateWisePlanBO.getEntityType()) + ","
                    + QT(offlineDateWisePlanBO.getStatus()) + ","
                    + offlineDateWisePlanBO.getSequence();

            db.insertSQL(DataMembers.tbl_date_wise_plan, DataMembers.tbl_date_wise_plan_cols, values);

            db.closeDB();
        } catch (Exception e) {
            Commons.printException("" + e);
            db.closeDB();
        }
    }

    public void updatePlan(Context mContext, OfflineDateWisePlanBO offlineDateWisePlanBO) {
        DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME, DataMembers.DB_PATH);
        try {
            db.openDataBase();

            if (offlineDateWisePlanBO.getPlanId() > 0) {
                db.updateSQL("UPDATE " + DataMembers.tbl_date_wise_plan
                        + " SET status = 'D' "
                        + " , upload = 'N'"
                        + " WHERE " + " PlanId =" + offlineDateWisePlanBO.getPlanId() + " and EntityType = " + QT(offlineDateWisePlanBO.getEntityType()));
            } else {
                db.deleteSQL(DataMembers.tbl_date_wise_plan, "EntityId=" + offlineDateWisePlanBO.getEntityId() +
                        " and Date = " + QT(offlineDateWisePlanBO.getDate()) + " and EntityType = " + QT(offlineDateWisePlanBO.getEntityType()), false);
            }

            db.closeDB();
        } catch (Exception e) {
            Commons.printException("" + e);
            db.closeDB();
        }
    }

    public String QT(String data) {
        return "'" + data + "'";
    }

    public Vector<NonFieldBO> downLoadNonFieldList() {
        Vector<NonFieldBO> nonFieldList = new Vector<>();
        try {

            DBUtil db = new DBUtil(context, DataMembers.DB_NAME,
                    DataMembers.DB_PATH);
            db.openDataBase();
            Cursor c = db.selectSQL("SELECT ListId,ListCode,ListName,isRequired,ParentId from StandardListMaster where ListType='FIELD_PLAN_TYPE'");
            if (c != null) {
                NonFieldBO reasonBO = new NonFieldBO();
                while (c.moveToNext()) {
                    reasonBO = new NonFieldBO();
                    reasonBO.setReasonID(c.getInt(0));
                    reasonBO.setCode(c.getString(1));
                    reasonBO.setReason(c.getString(2));
                    reasonBO.setIsRequired(c.getInt(3));
                    reasonBO.setpLevelId(c.getInt(4));
                    nonFieldList.add(reasonBO);
                }
                c.close();
            }

            db.closeDB();
        } catch (Exception e) {
            Commons.printException("Non Field Work Loading Exception", e);
        }

        return nonFieldList;

    }
}
