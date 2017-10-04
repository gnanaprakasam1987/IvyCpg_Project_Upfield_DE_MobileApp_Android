package com.ivy.sd.png.provider;


import android.content.Context;
import android.database.Cursor;

import com.ivy.lib.existing.DBUtil;
import com.ivy.sd.png.bo.InStoreActivityBO;
import com.ivy.sd.png.bo.TaskDataBO;
import com.ivy.sd.png.commons.SDUtil;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.util.Commons;
import com.ivy.sd.png.util.DataMembers;

import java.util.Vector;

public class TaskHelper {

    private Context context;
    private BusinessModel bmodel;
    private static TaskHelper instance = null;
    public String mode = "seller";
    private InStoreActivityBO instoreBO;
    private Vector<TaskDataBO> taskDataBO;

    protected TaskHelper(Context context) {
        this.context = context;
        this.bmodel = (BusinessModel) context;
        setTaskDataBO(new Vector<TaskDataBO>());
        instoreBO = new InStoreActivityBO();
    }

    public static TaskHelper getInstance(Context context) {
        if (instance == null) {
            instance = new TaskHelper(context);
        }
        return instance;
    }


    private void setTaskDataBO(Vector<TaskDataBO> taskDataBO) {
        this.taskDataBO = taskDataBO;
    }


    public String QT(String data) {
        return "'" + data + "'";
    }

    /**
     * Save New User created task. task can be either seller wise or retailer
     * wise If channelId = -1 - Apply task for all channel retailers If
     * channelId = 0 - Apply this task as Seller task If channelId = valid
     * channel id - Then apply to all retailers comming under this channel
     *
     * @param channelId
     * @param taskTitleDesc
     * @param taskDetailDesc
     */
    public void saveTask(int channelId, String taskTitleDesc,
                         String taskDetailDesc) {
        try {
            DBUtil db = new DBUtil(context, DataMembers.DB_NAME,
                    DataMembers.DB_PATH);
            String values;
            db.createDataBase();
            db.openDataBase();

            // Remove single quotes
            String name = taskDetailDesc.replaceAll("'", " ");
            String title = taskTitleDesc.replaceAll("'", " ");

            // Generate Unique ID
            String id = QT(bmodel.userMasterHelper.getUserMasterBO()
                    .getUserid() + SDUtil.now(SDUtil.DATE_TIME_ID));

            String date = QT(SDUtil.now(SDUtil.DATE_GLOBAL));
            Commons.print("date :: ," + date + "");

            // Insert Task into TaskMaster
            String columns_new = "taskid,taskcode,taskdesc,upload ,taskowner,date,usercreated";
            String value_new = id + "," + QT(title) + "," + QT(name) + ","
                    + "'N'," + QT("self") + ", " + date + ",1";
            db.insertSQL("TaskMaster", columns_new, value_new);

            String columns = "taskid,retailerid,usercreated,upload,date,uid";
            String UID = QT(bmodel.getRetailerMasterBO().getRetailerID()
                    + SDUtil.now(SDUtil.DATE_TIME_ID_MILLIS));

            if (channelId == -1) {// for all channel
                String[] chrid = this.getChannelRetailerId(0);
                for (int i = 0; i < chrid.length; i++) {

                    values = id + "," + chrid[i] + "," + "1" + "," + "'N'," + date + "," + UID;
                    db.insertSQL(DataMembers.tbl_TaskConfigurationMaster,
                            columns, values);
                }

            } else if (channelId == 0) {

                values = id + "," + 0 + "," + "1" + "," + "'N'," + date + "," + UID;
                db.insertSQL(DataMembers.tbl_TaskConfigurationMaster, columns,
                        values);
            } else if (bmodel.taskHelper.mode.equals("retailer")) {
                if (channelId == -2) {
                    String[] chrid = this.getRetailerIdlist();
                    for (int i = 0; i < chrid.length; i++) {

                        values = id + "," + chrid[i] + "," + "1" + ","
                                + "'N'," + date + "," + UID;
                        db.insertSQL(DataMembers.tbl_TaskConfigurationMaster,
                                columns, values);
                    }
                } else {
                    values = id + "," + channelId + "," + "1" + "," + "'N'," + date + "," + UID;
                    db.insertSQL(DataMembers.tbl_TaskConfigurationMaster,
                            columns, values);
                }
            } else {

                String[] chrid = this.getChannelRetailerId(channelId);
                for (int i = 0; i < chrid.length; i++) {
                    values = id + "," + chrid[i] + "," + "1" + "," + "'N'," + date + "," + UID;
                    db.insertSQL(DataMembers.tbl_TaskConfigurationMaster,
                            columns, values);
                }
            }

            db.closeDB();
        } catch (Exception e) {
            Commons.printException(e);
        }
    }

    private String[] getChannelRetailerId(int channelId) {
        Vector<String> channelRId = new Vector<>();
        int siz = bmodel.getRetailerMaster().size();
        if (channelId == 0) {
            for (int ii = 0; ii < siz; ii++) {

                if (((bmodel.getRetailerMaster().get(ii).getIsToday() == 1))
                        || bmodel.getRetailerMaster().get(ii).getIsDeviated()
                        .equals("Y")) {
                    channelRId.add(bmodel.getRetailerMaster().get(ii)
                            .getRetailerID());
                }
            }
        } else {
            for (int ii = 0; ii < siz; ii++) {
                if (((bmodel.getRetailerMaster().get(ii).getIsToday() == 1) || bmodel
                        .getRetailerMaster().get(ii).getIsDeviated()
                        .equals("Y"))
                        && bmodel.getRetailerMaster().get(ii).getChannelID() == channelId) {
                    channelRId.add(bmodel.getRetailerMaster().get(ii)
                            .getRetailerID());
                }
            }
        }
        String data[] = new String[channelRId.size()];
        for (int i = 0; i < channelRId.size(); i++) {
            data[i] = channelRId.get(i);
        }
        return data;

    }

    private String[] getRetailerIdlist() {
        Vector<String> RId = new Vector<>();
        int siz = bmodel.getRetailerMaster().size();
        for (int ii = 0; ii < siz; ii++) {
            if (((bmodel.getRetailerMaster().get(ii).getIsToday() == 1))
                    || bmodel.getRetailerMaster().get(ii).getIsDeviated()
                    .equals("Y")) {
                RId.add(bmodel.getRetailerMaster().get(ii).getRetailerID());
            }
        }

        String data[] = new String[RId.size()];
        for (int i = 0; i < RId.size(); i++) {
            data[i] = RId.get(i);
        }
        return data;

    }


    public boolean isTaskDone() {
        boolean flag = false;
        try {
            DBUtil db = new DBUtil(context, DataMembers.DB_NAME,
                    DataMembers.DB_PATH);
            db.openDataBase();
            Cursor c = db.selectSQL("select taskid from TaskExecutionDetails"
                    + " where RetailerId="
                    + QT(bmodel.getRetailerMasterBO().getRetailerID()));
            if (c != null) {
                if (c.getCount() > 0) {
                    flag = true;
                }
            }

            c.close();
            db.closeDB();
        } catch (Exception e) {
            Commons.printException(e);
        }
        return flag;
    }

    public Vector<TaskDataBO> getTaskData(String retailerId) {
        DBUtil db = new DBUtil(context, DataMembers.DB_NAME,
                DataMembers.DB_PATH);
        db.createDataBase();
        db.openDataBase();
        Cursor c = db
                .selectSQL("select distinct A.taskid,B.taskcode,B.taskDesc,A.retailerId,A.upload,(CASE WHEN ifnull(TD.TaskId,0) >0 THEN 1 ELSE 0 END) as isDone,"
                        + "B.usercreated , B.taskowner , B.date, A.upload,channelid  from TaskConfigurationMaster A inner join TaskMaster B on "
                        + "A.taskid=B.taskid left join TaskExecutionDetails TD on TD.TaskId=A.taskid  and  A.TaskId not in (Select taskid from TaskHistory where RetailerId =" + retailerId + " ) and TD.RetailerId = " + retailerId); //where A.upload!='Y'
        taskDataBO = new Vector<>();
        if (c != null) {
            while (c.moveToNext()) {
                TaskDataBO taskmasterbo = new TaskDataBO();
                taskmasterbo.setTaskId(c.getString(0));
                taskmasterbo.setTasktitle(c.getString(1));
                taskmasterbo.setTaskDesc(c.getString(2));
                taskmasterbo.setRid(c.getInt(3));
                taskmasterbo.setUpload(c.getString(4));
                taskmasterbo.setIsdone(c.getString(5));
                taskmasterbo.setUsercreated(c.getString(6));
                taskmasterbo.setTaskOwner(c.getString(7));
                taskmasterbo.setCreatedDate(c.getString(8));
                if (c.getString(9).equals("Y"))
                    taskmasterbo.setIsUpload(true);
                else
                    taskmasterbo.setIsUpload(false);
                taskmasterbo.setChannelId(c.getInt(10));
                taskDataBO.add(taskmasterbo);
            }
        }
        c.close();
        db.closeDB();
        return taskDataBO;

    }

    public Vector<TaskDataBO> getPendingTaskData() {
        DBUtil db = new DBUtil(context, DataMembers.DB_NAME,
                DataMembers.DB_PATH);
        db.createDataBase();
        db.openDataBase();
        Cursor c = db
                .selectSQL("Select distinct TM.* from TaskMaster TM inner join TaskConfigurationMaster TCM on TM.taskid=TCM.taskid where ("
                        + "TCM.channelid=" + bmodel.getRetailerMasterBO().getSubchannelid() + ")and TM.taskid not in(select TEC.taskid from TaskExecutionDetails TEC where TEC.retailerId='" + bmodel.getRetailerMasterBO().getRetailerID() + "')"); //where A.upload!='Y'
        taskDataBO = new Vector<>();
        if (c != null) {
            while (c.moveToNext()) {
                TaskDataBO taskmasterbo = new TaskDataBO();
                taskmasterbo.setTaskId(c.getString(0));
                taskmasterbo.setTasktitle(c.getString(1));
                taskmasterbo.setTaskDesc(c.getString(2));
                taskmasterbo.setTaskOwner(c.getString(3));
                taskDataBO.add(taskmasterbo);
            }
        }
        c.close();
        db.closeDB();
        return taskDataBO;

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
        }
        c.close();
        db.closeDB();
        return taskDataBO;

    }

    public Vector<TaskDataBO> loadTaskReport() {
        DBUtil db = new DBUtil(context, DataMembers.DB_NAME,
                DataMembers.DB_PATH);
        db.createDataBase();
        db.openDataBase();
        Cursor c = db
                .selectSQL("select distinct A.taskid,B.taskcode,B.taskDesc,A.retailerId,B.TaskOwner,B.Date, R.RetailerName from TaskConfigurationMaster A " +
                        " inner join TaskMaster B on A.taskid=B.taskid  " +
                        " inner join RetailerMaster r on r.RetailerID = A.retailerId " +
                        " where A.retailerId!=0 AND A.isdone=0");
        taskDataBO = new Vector<>();
        if (c != null) {
            TaskDataBO taskmasterbo = null;
            while (c.moveToNext()) {
                taskmasterbo = new TaskDataBO();
                taskmasterbo.setTaskId(c.getString(0));
                taskmasterbo.setTasktitle(c.getString(1));
                taskmasterbo.setTaskDesc(c.getString(2));
                taskmasterbo.setRid(c.getInt(3));
                taskmasterbo.setTaskOwner(c.getString(4));
                taskmasterbo.setCreatedDate(c.getString(5));
                taskmasterbo.setRetailerName(c.getString(6));
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
            TaskDataBO taskmasterbo = null;
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

    /**
     * This method will return the count of distinct tasks. Its a sum of task
     * downloaded from the server and user created task. If the user created
     * task assigned to all the retailers, count will be considered as 1 and
     * user needs to execute the task for all the retailers to consider it as
     * completed.
     *
     * @return int taskCount
     */
    public int getTaskCount() {
        int i = 0;
        try {
            DBUtil db = new DBUtil(context, DataMembers.DB_NAME,
                    DataMembers.DB_PATH);
            db.createDataBase();
            db.openDataBase();

            Cursor c = db
                    .selectSQL("select count(distinct A.taskid) from TaskConfigurationMaster  A inner join TaskMaster B on A.taskid=B.taskid where A.retailerid=0 and  A.channelid=0");
            if (c.getCount() > 0) {
                if (c.moveToNext()) {
                    i = c.getInt(0);
                }
            }
            c.close();
            db.closeDB();

        } catch (Exception e) {
            Commons.printException(e);
            return 0;
        }
        return i;
    }

    public void saveTask(String retailerid, TaskDataBO taskBO) {

        if (taskBO.getUsercreated() != null) {
            if (taskBO.getUsercreated().equals("1")) {
                updateTask(taskBO, true, retailerid);

            } else {
                updateTask(taskBO, false, retailerid);
            }

        } else {
            updateTask(taskBO, false, retailerid);
        }
    }

    private void updateTask(TaskDataBO taskBO, boolean isNew,
                            String retailerid) {
        DBUtil db = null;
        db = new DBUtil(context, DataMembers.DB_NAME,
                DataMembers.DB_PATH);
        db.createDataBase();
        db.openDataBase();

        String UID = QT(bmodel.getRetailerMasterBO().getRetailerID()
                + SDUtil.now(SDUtil.DATE_TIME_ID_MILLIS));
        String columns = "TaskId,RetailerId,Date,UId,Upload";
        String values;


        if (taskBO.isChecked()) {
            values = taskBO.getTaskId() + "," + QT(retailerid) + "," + QT(SDUtil.now(SDUtil.DATE_GLOBAL)) + "," + UID + ",'N'";
            db.insertSQL("TaskExecutionDetails", columns, values);
            bmodel.saveModuleCompletion("MENU_TASK");
        } else {
            db.deleteSQL("TaskExecutionDetails", "TaskId=" + taskBO.getTaskId() + " and RetailerId = " + retailerid, false);

            Cursor c = db.selectSQL("Select * from TaskExecutionDetails");
            if (c.getCount() == 0) {
                bmodel.deleteModuleCompletion("MENU_TASK");
                c.close();
            }
        }

        db.closeDB();
    }

}
