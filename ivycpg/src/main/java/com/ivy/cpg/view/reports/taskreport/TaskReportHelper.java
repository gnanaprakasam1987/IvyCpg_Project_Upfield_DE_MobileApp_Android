package com.ivy.cpg.view.reports.taskreport;

import android.content.Context;
import android.database.Cursor;

import com.ivy.lib.existing.DBUtil;
import com.ivy.sd.png.bo.TaskDataBO;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.util.DataMembers;

import java.util.Vector;

public class TaskReportHelper {
    private Context context;
    private BusinessModel bmodel;
    private static TaskReportHelper instance = null;
    public String mode = "seller";
    private Vector<TaskDataBO> taskDataBO;

    protected TaskReportHelper(Context context) {
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

    public Vector<TaskDataBO> loadTaskReportRetailerList() {

        DBUtil db = new DBUtil(context, DataMembers.DB_NAME,
                DataMembers.DB_PATH);
        db.createDataBase();
        db.openDataBase();
        Cursor c = db
                .selectSQL("select distinct A.retailerId, R.RetailerName , A.date  from TaskConfigurationMaster A  " +
                        " inner join TaskMaster B on A.taskid=B.taskid" +
                        " inner join RetailerMaster r on r.RetailerID = A.retailerId " +
                        " where A.retailerId!=0 AND A.isdone=0");
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

    public Vector<TaskDataBO> loadTaskReport() {
        DBUtil db = new DBUtil(context, DataMembers.DB_NAME,
                DataMembers.DB_PATH);
        db.createDataBase();
        db.openDataBase();
        Cursor c = db
                .selectSQL("select distinct A.taskid,B.taskcode,B.taskDesc,A.retailerId,B.TaskOwner,B.Date, R.RetailerName,A.isdone from TaskConfigurationMaster A " +
                        " inner join TaskMaster B on A.taskid=B.taskid  " +
                        " inner join RetailerMaster r on r.RetailerID = A.retailerId " +
                        " where A.retailerId!=0 AND A.isdone=0 order by A.retailerId");
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
            }
            c.close();
            db.closeDB();
        }
        return taskDataBO;
    }

    public Vector<TaskDataBO> loadRetailerPlannedDate() {
        DBUtil db = new DBUtil(context, DataMembers.DB_NAME,
                DataMembers.DB_PATH);
        db.createDataBase();
        db.openDataBase();
        Cursor c = db
                .selectSQL("SELECT RID, Date FROM RetailerClientMappingMaster ORDER BY RID");
        taskDataBO = new Vector<>();
        if (c != null) {
            TaskDataBO taskmasterbo;
            while (c.moveToNext()) {
                taskmasterbo = new TaskDataBO();
                taskmasterbo.setRid(c.getInt(0));
                taskmasterbo.setPlannedDate(c.getString(1));
                taskDataBO.add(taskmasterbo);
            }
            c.close();
            db.closeDB();
        }
        return taskDataBO;
    }

    public Vector<TaskDataBO> getSellerWiseTaskReport(){

        Vector<TaskDataBO> taskDataBOS = new Vector<>();

        DBUtil db = new DBUtil(context, DataMembers.DB_NAME,
                DataMembers.DB_PATH);
        db.createDataBase();
        db.openDataBase();
        Cursor c = db
                .selectSQL("select distinct A.taskid,B.taskcode,B.taskDesc,A.UserId,B.TaskOwner,B.Date," +
                        " um.username,A.isdone from TaskConfigurationMaster A  inner join TaskMaster B on A.taskid=B.taskid  " +
                        " inner join usermaster um on um.userid = A.UserId  where A.retailerId=0 and A.userId!=0");
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
