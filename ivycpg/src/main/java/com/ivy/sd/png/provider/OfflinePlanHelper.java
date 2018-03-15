package com.ivy.sd.png.provider;

import android.content.Context;
import android.database.Cursor;

import com.ivy.lib.existing.DBUtil;
import com.ivy.sd.png.bo.DateWisePlanBO;
import com.ivy.sd.png.bo.OfflineDateWisePlanBO;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.util.Commons;
import com.ivy.sd.png.util.DataMembers;

import java.util.ArrayList;
import java.util.HashMap;

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

            String sql = "SELECT PlanId,DistributorId,UserId,Date,EntityId,EntityType,Status,Sequence FROM DatewisePlan";

            db.openDataBase();

            Cursor c = db.selectSQL(sql);
            if (c != null && c.getCount() != 0) {
                OfflineDateWisePlanBO dateWisePlanBO;
                mHashMapData = new HashMap<>();
                offlineList = new ArrayList<>();

                while (c.moveToNext()) {
                    dateWisePlanBO = new OfflineDateWisePlanBO();

                    dateWisePlanBO.setPlanId(c.getInt(1));
                    dateWisePlanBO.setDistributorId(c.getInt(2));
                    dateWisePlanBO.setUserId(c.getInt(3));
                    dateWisePlanBO.setDate(c.getString(4));
                    dateWisePlanBO.setEntityId(c.getInt(5));
                    dateWisePlanBO.setEntityType(c.getString(6));
                    dateWisePlanBO.setStatus(c.getString(7));
                    dateWisePlanBO.setSequence(c.getInt(8));
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
}
