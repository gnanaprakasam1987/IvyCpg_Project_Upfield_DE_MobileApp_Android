package com.ivy.cpg.view.reports.taskreport;

import android.content.Context;
import android.database.Cursor;

import com.ivy.lib.existing.DBUtil;
import com.ivy.sd.png.bo.TaskDataBO;
import com.ivy.sd.png.commons.SDUtil;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.util.DataMembers;

import java.util.HashMap;
import java.util.Vector;

public class TaskReportHelper {
    private Context context;
    private BusinessModel bmodel;
    private static TaskReportHelper instance = null;
    public String mode = "seller";
    private Vector<TaskDataBO> taskDataBO;

    private HashMap<String,Vector<TaskDataBO>> taskDataBORetailer = new HashMap<>();
    private HashMap<String,Vector<TaskDataBO>> taskDataBODate = new HashMap<>();


    private TaskReportHelper(Context context) {
        this.context = context;
        this.bmodel = (BusinessModel) context.getApplicationContext();
        setTaskDataBO(new Vector<TaskDataBO>());
    }

    public static TaskReportHelper getInstance(Context context) {
        if (instance == null) {
            instance = new TaskReportHelper(context);
        }
        return instance;
    }


    private void setTaskDataBO(Vector<TaskDataBO> taskDataBO) {
        this.taskDataBO = taskDataBO;
    }


    public String QT(String data) {
        return "'" + data + "'";
    }

    Vector<TaskDataBO> loadTaskReportRetailerList() {

        DBUtil db = new DBUtil(context, DataMembers.DB_NAME,
                DataMembers.DB_PATH);
        db.createDataBase();
        db.openDataBase();

        String concatQuery ;
        concatQuery = ",ifnull((Select count (TED.TaskId) from TaskExecutionDetails TED where TED.TaskId = A.taskid and TED.RetailerId = A.retailerId),0) as taskStatus ";
        String conditionStr = "";

        if(bmodel.configurationMasterHelper.TASK_OPEN == 1){
            conditionStr = " And taskStatus = 0 ";
        }else if(bmodel.configurationMasterHelper.TASK_OPEN == 2){
            conditionStr = " And taskStatus = 1 ";
        }else{
            concatQuery ="";
        }

        Cursor c = db
                .selectSQL("select distinct A.retailerId, R.RetailerName , A.date "+concatQuery+" from TaskConfigurationMaster A  " +
                        " inner join TaskMaster B on A.taskid=B.taskid" +
                        " inner join RetailerMaster r on r.RetailerID = A.retailerId " +
                        " where A.retailerId!=0"+conditionStr);
        taskDataBO = new Vector<>();

        if (c != null) {
            while (c.moveToNext()) {
                TaskDataBO taskmasterbo = new TaskDataBO();
                taskmasterbo.setRid(c.getInt(0));
                taskmasterbo.setTaskOwner(c.getString(1));
                taskDataBO.add(taskmasterbo);
            }
            c.close();
        }
        db.closeDB();
        return taskDataBO;

    }

    Vector<TaskDataBO> loadTaskReport() {
        DBUtil db = new DBUtil(context, DataMembers.DB_NAME,
                DataMembers.DB_PATH);
        db.createDataBase();
        db.openDataBase();

        String concatQuery ;
        concatQuery = ",ifnull((Select count (TED.TaskId) from TaskExecutionDetails TED where TED.TaskId = A.taskid and TED.RetailerId = A.retailerId),0) as taskStatus ";
        String conditionStr = "";

        if(bmodel.configurationMasterHelper.TASK_OPEN == 1){
            conditionStr = " and  A.TaskId not in (Select taskid from TaskHistory where RetailerId = A.retailerId and taskid = A.taskid) and taskStatus = 0 ";
        }else if(bmodel.configurationMasterHelper.TASK_OPEN == 2){
            conditionStr = " And taskStatus = 1 ";
        }

        String query = "select distinct A.taskid,B.taskcode,B.taskDesc,A.retailerId,B.TaskOwner,B.Date, R.RetailerName "+concatQuery+" from TaskConfigurationMaster A " +
                " inner join TaskMaster B on A.taskid=B.taskid  " +
                " inner join RetailerMaster r on r.RetailerID = A.retailerId " +
                " where A.retailerId!=0 "+conditionStr+" order by A.retailerId";

        Cursor c = db
                .selectSQL(query);
        taskDataBO = new Vector<>();
        if (c != null) {
            TaskDataBO taskmasterbo;
            while (c.moveToNext()) {
                taskmasterbo = new TaskDataBO();
                taskmasterbo.setTaskId(c.getString(0));
                taskmasterbo.setTasktitle(c.getString(1));
                taskmasterbo.setTaskDesc(c.getString(2));
                taskmasterbo.setRid(c.getInt(3));
                taskmasterbo.setTaskOwner(c.getString(4));
                taskmasterbo.setCreatedDate(c.getString(5));
                taskmasterbo.setRetailerName(c.getString(6));
                taskmasterbo.setIsdone(c.getString(7));
                taskDataBO.add(taskmasterbo);

                Vector<TaskDataBO> taskDataBO = new Vector<>();
                taskDataBO.add(taskmasterbo);

                if(taskDataBORetailer.get(taskmasterbo.getPlannedDate()) == null)
                    taskDataBORetailer.put(String.valueOf(taskmasterbo.getRid()), taskDataBO);
                else
                    taskDataBORetailer.get(String.valueOf(taskmasterbo.getRid())).add(taskmasterbo);
            }
            c.close();
            db.closeDB();
        }
        return taskDataBO;
    }

    Vector<TaskDataBO> loadRetailerPlannedDate() {
        DBUtil db = new DBUtil(context, DataMembers.DB_NAME,
                DataMembers.DB_PATH);
        db.createDataBase();
        db.openDataBase();

        String condtionStr = "";

        if(bmodel.configurationMasterHelper.TASK_PLANNED == 1) {
            String date = SDUtil.now(SDUtil.DATE_GLOBAL);
            condtionStr = " where Date = "+QT(date);
        }

        Cursor c = db
                .selectSQL("SELECT RID, Date FROM RetailerClientMappingMaster "+condtionStr+" ORDER BY RID");
        taskDataBO = new Vector<>();
        if (c != null) {
            TaskDataBO taskmasterbo;
            while (c.moveToNext()) {
                taskmasterbo = new TaskDataBO();
                taskmasterbo.setRid(c.getInt(0));
                taskmasterbo.setPlannedDate(c.getString(1));
                taskDataBO.add(taskmasterbo);

                Vector<TaskDataBO> taskDataBO = new Vector<>();
                taskDataBO.add(taskmasterbo);

                if(taskDataBODate.get(taskmasterbo.getPlannedDate()) == null)
                    taskDataBODate.put(String.valueOf(taskmasterbo.getRid()), taskDataBO);
                else
                    taskDataBODate.get(String.valueOf(taskmasterbo.getRid())).add(taskmasterbo);

            }
            c.close();
            db.closeDB();
        }
        return taskDataBO;
    }

    Vector<TaskDataBO> getSellerWiseTaskReport(){

        Vector<TaskDataBO> taskDataBOS = new Vector<>();

        DBUtil db = new DBUtil(context, DataMembers.DB_NAME,
                DataMembers.DB_PATH);
        db.createDataBase();
        db.openDataBase();

        String concatQuery ;
        concatQuery = ",ifnull((Select count (TED.TaskId) from TaskExecutionDetails TED where TED.TaskId = A.taskid and TED.RetailerId = A.retailerId),0) as taskStatus ";
        String conditionStr = "";

        if(bmodel.configurationMasterHelper.TASK_OPEN == 1){
            conditionStr = " and  A.TaskId not in (Select taskid from TaskHistory where RetailerId = A.retailerId and taskid = A.taskid) and taskStatus = 0 ";
        }else if(bmodel.configurationMasterHelper.TASK_OPEN == 2){
            conditionStr = " And taskStatus = 1 ";
        }

        Cursor c = db
                .selectSQL("select distinct A.taskid,B.taskcode,B.taskDesc,A.UserId,B.TaskOwner,B.Date," +
                        " um.username"+concatQuery+" from TaskConfigurationMaster A inner join TaskMaster B on A.taskid=B.taskid  " +
                        " inner join usermaster um on um.userid = A.UserId  where A.retailerId=0 and A.userId!=0 "+conditionStr+" order by A.UserId");
        taskDataBO = new Vector<>();
        if (c != null) {
            TaskDataBO taskmasterbo;
            while (c.moveToNext()) {
                taskmasterbo = new TaskDataBO();
                taskmasterbo.setTaskId(c.getString(0));
                taskmasterbo.setTasktitle(c.getString(1));
                taskmasterbo.setTaskDesc(c.getString(2));
                taskmasterbo.setUserId(c.getInt(3));
                taskmasterbo.setRid(0);
                taskmasterbo.setTaskOwner(c.getString(4));
                taskmasterbo.setCreatedDate(c.getString(5));
                taskmasterbo.setUserName(c.getString(6));
                taskmasterbo.setIsdone(c.getString(7));
                taskDataBOS.add(taskmasterbo);
            }
            c.close();
            db.closeDB();
        }

        return taskDataBOS;

    }

}
