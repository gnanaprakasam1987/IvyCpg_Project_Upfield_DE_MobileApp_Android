package com.ivy.cpg.view.reports.taskexcutionreport;

import android.content.Context;
import android.database.Cursor;

import com.ivy.lib.existing.DBUtil;
import com.ivy.sd.png.bo.RetailerMasterBO;
import com.ivy.sd.png.bo.SpinnerBO;
import com.ivy.sd.png.commons.SDUtil;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.util.Commons;
import com.ivy.sd.png.util.DataMembers;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by ivyuser on 31/7/18.
 */

public class TaskReportHelper {
    private Context mContext;
    private BusinessModel bmodel;
    private static TaskReportHelper instance = null;

    protected TaskReportHelper(Context context) {
        this.mContext = context;
        bmodel = (BusinessModel) context;
    }

    public static TaskReportHelper getInstance(Context context) {
        if (instance == null) {
            instance = new TaskReportHelper(context);
        }
        return instance;
    }

    private ArrayList<TaskReportBo> taskretailerinfo = new ArrayList<>();

    public ArrayList<TaskReportBo> getTaskretailerinfo() {
        return taskretailerinfo;
    }

    private void setTaskretailerinfo(ArrayList<TaskReportBo> taskretailerinfo) {
        this.taskretailerinfo = taskretailerinfo;
    }

    /**
     * load data if module completed or not.
     */
    public void downloadTaskExecutionReport() {
        try {
            ArrayList<SpinnerBO> retailerList = new ArrayList<>();
            DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME,
                    DataMembers.DB_PATH);
            db.openDataBase();
            StringBuilder sb = new StringBuilder();
            sb.append("select Retailerid,MENU_CODE from ModuleCompletionReport");

            Cursor c = db.selectSQL(sb.toString());

            if (c != null) {
                setTaskretailerinfo(new ArrayList<TaskReportBo>());
                while (c.moveToNext()) {
                    retailerList.add(new SpinnerBO(c.getInt(0), c.getString(1)));
                }
                c.close();
            }
            db.closeDB();

            TaskReportBo outlet;
            for (RetailerMasterBO retailerMasterBO : bmodel.getRetailerMaster()) {
                if (retailerMasterBO.getIsToday() == 1
                        || retailerMasterBO.getIsDeviated().equals("Y")) {
                    outlet = new TaskReportBo();
                    outlet.setmRetailerId(SDUtil.convertToInt(retailerMasterBO.getRetailerID()));
                    outlet.setmRetailerName(retailerMasterBO.getRetailerName());
                    if (retailerList.size() > 0) {
                        HashMap<String, Boolean> menuMap = new HashMap<>();
                        for (SpinnerBO retaielers : retailerList) {
                            if (outlet.getmRetailerId() == retaielers.getId()) {
                                menuMap.put(retaielers.getSpinnerTxt(), true);
                            }
                        }
                        outlet.setmMenuCodeMap(menuMap);
                    }
                    taskretailerinfo.add(outlet);
                }
            }


        } catch (Exception e) {
            Commons.printException(e);
        }
    }


    public ArrayList<TaskReportBo> downloadBeatNames() {
        DBUtil db = new DBUtil(mContext, DataMembers.DB_NAME,
                DataMembers.DB_PATH);
        db.openDataBase();
        ArrayList<TaskReportBo> beatinfo = new ArrayList<>();
        try {


            Cursor c = db
                    .selectSQL("select distinct BM.beatid,BM.BeatDescription from ModuleCompletionReport MCR INNER JOIN RetailerMaster RM on MCR.retailerid = RM.retailerid "
                            + " INNER JOIN BeatMaster BM on BM.beatid = RM.beatid");

            if (c != null) {
                while (c.moveToNext()) {
                    TaskReportBo beat;
                    beat = new TaskReportBo();
                    beat.setBeatId(c.getInt(0));
                    beat.setBeatDescription(c.getString(1));
                    beatinfo.add(beat);
                }
                c.close();
            }
            db.closeDB();
        } catch (Exception e) {

        }

        return beatinfo;
    }
}
