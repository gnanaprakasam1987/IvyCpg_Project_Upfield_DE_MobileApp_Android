package com.ivy.sd.png.provider;

import android.content.Context;
import android.database.Cursor;

import com.ivy.lib.existing.DBUtil;
import com.ivy.sd.png.bo.OfflineDateWisePlanBO;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.util.Commons;
import com.ivy.sd.png.util.DataMembers;

import java.util.ArrayList;

/**
 * Created by rahul.j on 3/14/2018.
 */

public class OfflinePlanHelper {
    private static OfflinePlanHelper instance = null;
    private Context context;
    private final BusinessModel bmodel;
    private ArrayList<OfflineDateWisePlanBO> offlineList = new ArrayList<>();


    public OfflinePlanHelper(Context context) {
        this.context = context;
        this.bmodel = (BusinessModel) context.getApplicationContext();
    }

    public static OfflinePlanHelper getInstance(Context context) {
        if (instance == null) {
            instance = new OfflinePlanHelper(context);
        }
        return instance;
    }

    public ArrayList<OfflineDateWisePlanBO> getOfflineList() {
        return offlineList;
    }

    public void downloadOfflinePlanList() {
        DBUtil db = new DBUtil(context, DataMembers.DB_NAME,
                DataMembers.DB_PATH);
        if (offlineList != null && offlineList.size() > 0) {
            offlineList.clear();
        }
        try {
//            String sql = "select cmp_id,name,fromDate,toDate from CampaignMaster order by cmp_id";

            String sql = "SELECT PlanId,DistributorId,UserId,Date,EntityId,EntityType,Status,Sequence,Upload FROM DatewisePlan";

            db.openDataBase();

            Cursor c = db.selectSQL(sql);
            if (c != null && c.getCount() != 0) {
                OfflineDateWisePlanBO dateWisePlanBO;
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
                    dateWisePlanBO.setUpload(c.getString(9));

                    offlineList.add(dateWisePlanBO);
                }
                c.close();
            }

        } catch (Exception e) {
            Commons.printException("Unable to get campaigns " + e);
        }
        db.closeDB();
    }
}
