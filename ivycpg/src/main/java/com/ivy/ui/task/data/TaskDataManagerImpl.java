package com.ivy.ui.task.data;

import android.database.Cursor;
import android.database.DatabaseUtils;

import com.ivy.core.data.app.AppDataProvider;
import com.ivy.core.di.scope.DataBaseInfo;
import com.ivy.cpg.view.task.TaskDataBO;
import com.ivy.lib.existing.DBUtil;
import com.ivy.sd.png.bo.RetailerMasterBO;
import com.ivy.sd.png.commons.SDUtil;
import com.ivy.sd.png.provider.ConfigurationMasterHelper;
import com.ivy.sd.png.util.Commons;
import com.ivy.sd.png.util.DataMembers;
import com.ivy.ui.task.TaskConstant;
import com.ivy.utils.DateTimeUtils;
import com.ivy.utils.StringUtils;

import java.util.ArrayList;
import java.util.concurrent.Callable;

import javax.inject.Inject;

import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.SingleSource;
import io.reactivex.functions.Function;

public class TaskDataManagerImpl implements TaskDataManager {

    private DBUtil mDbUtil;
    private AppDataProvider appDataProvider;

    @Inject
    public TaskDataManagerImpl(@DataBaseInfo DBUtil mDbUtil, AppDataProvider appDataProvider) {
        this.mDbUtil = mDbUtil;
        this.appDataProvider = appDataProvider;
    }

    private void initDb() {
        mDbUtil.createDataBase();
        if (mDbUtil.isDbNullOrClosed())
            mDbUtil.openDataBase();
    }

    private void shutDownDb() {
        mDbUtil.closeDB();
    }


    @Override
    public Observable<ArrayList<TaskDataBO>> fetchTaskData(String retailerId, int userCreatedTask) {
        return Observable.fromCallable(() -> {
            try {
                initDb();

                String userCreated = "";

                if (userCreatedTask != 0)
                    userCreated = " and B.usercreated =" + (userCreatedTask == 2 ? 1 : 0);

                ArrayList<TaskDataBO> taskDataBOS = new ArrayList<>();
                String query = "select distinct A.taskid,B.taskcode,B.taskDesc,A.retailerId,A.upload,"
                        + "(CASE WHEN ifnull(TD.TaskId,0) >0 THEN 1 ELSE 0 END) as isDone,"
                        + "B.usercreated , B.taskowner , B.date, A.upload,A.channelid,A.userid,"
                        + "IFNULL(B.DueDate,''),B.CategoryId,IFNULL(PL.PName,''),B.IsServerTask,SUBSTR(TD.ImageName,18) as eveImage"
                        + " from TaskConfigurationMaster A inner join TaskMaster B on A.taskid=B.taskid"
                        + " left join TaskExecutionDetails TD on TD.TaskId=A.taskid and TD.RetailerId = " + retailerId
                        + " left join ProductMaster PL on PL.PID=B.CategoryId"
                        + " left join RetailerMaster RM on RM.RetailerID=A.retailerId"
                        + " where (B.Status!='D' OR B.Status IS NULL) and A.retailerId=" + retailerId
                        + " and A.TaskId not in (Select taskid from TaskHistory where RetailerId =" + retailerId + ")"
                        + userCreated;

                Cursor c = mDbUtil
                        .selectSQL(query);
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
                        taskmasterbo.setUserId(c.getInt(11));
                        taskmasterbo.setTaskDueDate(c.getString(12));
                        taskmasterbo.setTaskCategoryID(c.getInt(13));
                        taskmasterbo.setTaskCategoryDsc(c.getString(14));
                        taskmasterbo.setServerTask(c.getInt(15));
                        taskmasterbo.setTaskEvidenceImg(c.getString(16));

                        if (taskmasterbo.getRid() != 0
                                || taskmasterbo.getChannelId() != 0)
                            taskmasterbo.setMode("retailer");
                        else
                            taskmasterbo.setMode("seller");

                        taskDataBOS.add(taskmasterbo);
                    }
                    c.close();
                    shutDownDb();
                    return taskDataBOS;
                }
            } catch (Exception e) {
                Commons.printException(e);
            }
            shutDownDb();
            return new ArrayList<>();
        });
    }

    @Override
    public Observable<ArrayList<TaskDataBO>> fetchCompletedTask(String retailerId) {
        return Observable.fromCallable(new Callable<ArrayList<TaskDataBO>>() {
            @Override
            public ArrayList<TaskDataBO> call() throws Exception {
                ArrayList<TaskDataBO> taskCompleteBos = new ArrayList<>();
                try {
                    initDb();

                    String query = "select distinct A.taskid,B.taskcode,B.taskDesc,A.retailerId,"
                            + "(CASE WHEN ifnull(TH.TaskId,0) >0 THEN 1 ELSE 0 END) as isDone,"
                            + "B.usercreated , B.taskowner , B.date, A.upload,A.channelid,A.userid,"
                            + "IFNULL(B.DueDate,''),B.CategoryId,IFNULL(PL.PName,''),TH.ExecutionDate"
                            + " from TaskConfigurationMaster A inner join TaskMaster B on A.taskid=B.taskid"
                            + " inner join TaskHistory TH on TH.TaskId=A.taskid and TH.RetailerId = " + retailerId
                            + " left join ProductMaster PL on PL.PID=B.CategoryId"
                            + " left join RetailerMaster RM on RM.RetailerID=A.retailerId"
                            + " where A.retailerId=" + retailerId;

                    Cursor c = mDbUtil
                            .selectSQL(query);
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

                            taskmasterbo.setChannelId(c.getInt(9));
                            taskmasterbo.setUserId(c.getInt(10));
                            taskmasterbo.setTaskDueDate(c.getString(11));
                            taskmasterbo.setTaskCategoryID(c.getInt(12));
                            taskmasterbo.setTaskCategoryDsc(c.getString(13));
                            taskmasterbo.setTaskExecDate(c.getString(14));

                            if (taskmasterbo.getUserId() != 0 || taskmasterbo.getUsercreated().equals("0"))
                                taskmasterbo.setMode("seller");
                            else if (taskmasterbo.getChannelId() != 0)
                                taskmasterbo.setMode("channel");
                            else
                                taskmasterbo.setMode("retailer");

                            taskCompleteBos.add(taskmasterbo);
                        }

                        c.close();
                    }
                    shutDownDb();
                    return taskCompleteBos;


                } catch (Exception e) {
                    Commons.printException(e);
                }
                shutDownDb();
                return taskCompleteBos;
            }
        });
    }

    /**
     * This method used to fetch pending task data
     *
     * @return ArrayList
     */
    @Override
    public Observable<ArrayList<TaskDataBO>> fetchPendingTaskData() {
        return Observable.fromCallable(new Callable<ArrayList<TaskDataBO>>() {
            @Override
            public ArrayList<TaskDataBO> call() {
                try {
                    initDb();

                    ArrayList<TaskDataBO> pendingTaskDataBOS = new ArrayList<>();
                    String pndTskQuery = "Select distinct TM.* from TaskMaster TM inner join TaskConfigurationMaster TCM on TM.taskid=TCM.taskid where ("
                            + "TCM.channelid=" + appDataProvider.getRetailMaster().getSubchannelid() + ")and TM.taskid not in(select TEC.taskid from TaskExecutionDetails TEC where TEC.retailerId='" + appDataProvider.getRetailMaster().getRetailerID() + "')";
                    Cursor c = mDbUtil
                            .selectSQL(pndTskQuery);
                    if (c != null) {
                        while (c.moveToNext()) {
                            TaskDataBO taskmasterbo = new TaskDataBO();
                            taskmasterbo.setTaskId(c.getString(0));
                            taskmasterbo.setTasktitle(c.getString(1));
                            taskmasterbo.setTaskDesc(c.getString(2));
                            taskmasterbo.setTaskOwner(c.getString(3));
                            pendingTaskDataBOS.add(taskmasterbo);
                        }
                        c.close();
                        shutDownDb();
                        return pendingTaskDataBOS;
                    }

                } catch (Exception e) {
                    Commons.printException(e);
                }
                shutDownDb();
                return new ArrayList<>();
            }
        });
    }

    /**
     * This method used to fetch Task Count
     *
     * @return int
     */
    @Override
    public Single<Integer> fetchTaskCount() {
        return Single.fromCallable(new Callable<Integer>() {
            @Override
            public Integer call() throws Exception {
                try {
                    initDb();
                    int i = 0;
                    String tskCntQuery = "select count(distinct A.taskid) from TaskConfigurationMaster  A inner join TaskMaster B on A.taskid=B.taskid where A.retailerid=0 and  A.channelid=0";

                    Cursor c = mDbUtil.selectSQL(tskCntQuery);
                    if (c.getCount() > 0) {
                        if (c.moveToNext()) {
                            i = c.getInt(0);
                        }
                    }
                    c.close();
                    shutDownDb();
                    return i;
                } catch (Exception e) {
                    Commons.printException(e);
                }
                shutDownDb();
                return 0;
            }
        });
    }

    /**
     * This method used to update task execution
     *
     * @param taskDataBO
     * @param retailerId
     * @return
     */
    @Override
    public Single<Boolean> updateTaskExecutionData(TaskDataBO taskDataBO, String retailerId) {
        return Single.fromCallable(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                try {
                    initDb();
                    mDbUtil.deleteSQL("TaskExecutionDetails", "TaskId=" + StringUtils.QT(taskDataBO.getTaskId()) + " and RetailerId = " + retailerId, false);

                    return true;
                } catch (Exception e) {
                    Commons.printException(e);
                }
                return false;
            }
        }).flatMap(new Function<Boolean, SingleSource<? extends Boolean>>() {
            @Override
            public SingleSource<? extends Boolean> apply(Boolean aBoolean) throws Exception {
                return Single.fromCallable(new Callable<Boolean>() {
                    @Override
                    public Boolean call() throws Exception {
                        String uID;
                        if (retailerId.equals("0"))
                            uID = StringUtils.QT(appDataProvider.getUser().getUserid()
                                    + DateTimeUtils.now(DateTimeUtils.DATE_TIME_ID_MILLIS));
                        else
                            uID = StringUtils.QT(appDataProvider.getRetailMaster().getRetailerID()
                                    + DateTimeUtils.now(DateTimeUtils.DATE_TIME_ID_MILLIS));

                        String taskEvdImage = taskDataBO.getTaskEvidenceImg() == null ? null : StringUtils.QT(taskDataBO.getTaskEvidenceImg());
                        String columns = "TaskId,RetailerId,Date,UId,Upload,ImageName";
                        String values;

                        try {
                            if (taskDataBO.isChecked()) {
                                values = StringUtils.QT(taskDataBO.getTaskId()) + ","
                                        + StringUtils.QT(retailerId) + ","
                                        + StringUtils.QT(DateTimeUtils.now(DateTimeUtils.DATE_GLOBAL)) + ","
                                        + uID + ",'N'" + ","
                                        + taskEvdImage;
                                mDbUtil.insertSQL("TaskExecutionDetails", columns, values);
                                //bmodel.saveModuleCompletion("MENU_TASK");
                            } else {

                                Cursor c = mDbUtil.selectSQL("Select * from TaskExecutionDetails");
                                if (c.getCount() == 0) {
                                    //bmodel.deleteModuleCompletion("MENU_TASK");
                                    c.close();
                                }
                            }
                            shutDownDb();
                            return true;

                        } catch (Exception e) {
                            Commons.printException(e);
                        }
                        shutDownDb();
                        return false;
                    }
                });
            }
        });
    }

    @Override
    public Single<Boolean> updateTaskExecutionImage(String imageName, String taskId) {
        return Single.fromCallable(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                try {
                    initDb();

                    String folderName = "Task/"
                            + appDataProvider.getUser().getDownloadDate()
                            .replace("/", "") + "/"
                            + appDataProvider.getUser().getUserid() + "/";

                    mDbUtil.updateSQL("UPDATE TaskExecutionDetails "
                            + " SET ImageName=" + StringUtils.QT(folderName + imageName)
                            + " WHERE TaskId=" + StringUtils.QT(taskId));

                    shutDownDb();
                    return true;
                } catch (Exception ignore) {

                }
                shutDownDb();
                return false;
            }
        });
    }

    /**
     * Add and Update Task
     *
     * @param selectedId
     * @param taskObj
     * @param mode
     * @param taskImgList
     * @return
     */
    @Override
    public Single<Boolean> addAndUpdateTask(int selectedId, TaskDataBO taskObj, String mode, ArrayList<TaskDataBO> taskImgList) {

        //remove Quotes
        String title = StringUtils.removeQuotes(taskObj.getTasktitle());
        String name = StringUtils.removeQuotes(taskObj.getTaskDesc());


        String taskOwner = StringUtils.QT("self");
        String status = StringUtils.QT("I");
        // Generate Unique ID
        String id = StringUtils.QT(appDataProvider.getUser()
                .getUserid() + DateTimeUtils.now(DateTimeUtils.DATE_TIME_ID));

        String date = StringUtils.QT(DateTimeUtils.now(DateTimeUtils.DATE_GLOBAL));

        String endDate = StringUtils.QT(DateTimeUtils.addDateToYear(1));

        String uID = StringUtils.QT(selectedId
                + DateTimeUtils.now(DateTimeUtils.DATE_TIME_ID_MILLIS));

        if (!taskObj.getTaskOwner().isEmpty()
                && (!taskObj.getTaskOwner().equalsIgnoreCase("Self")
                || taskObj.getUpload().equalsIgnoreCase("Y"))) {
            id = StringUtils.QT(taskObj.getTaskId());
            taskOwner = StringUtils.QT(taskObj.getTaskOwner());
            status = StringUtils.QT("U");
        }

        String finalTid = id;
        String finalTaskOwner = taskOwner;
        String finalStatus = status;
        return Single.fromCallable(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {

                String columns_new;
                String value_new;


                try {
                    initDb();

                    String sb = "Select count(taskid) from TaskMaster where taskid =" + StringUtils.QT(taskObj.getTaskId());

                    Cursor c = mDbUtil.selectSQL(sb);
                    if (c.getCount() > 0) {
                        if (c.moveToNext()) {

                            mDbUtil.deleteSQL("TaskMaster", "taskid=" + StringUtils.QT(taskObj.getTaskId()), false);

                            mDbUtil.deleteSQL(DataMembers.tbl_TaskConfigurationMaster, "taskid=" + StringUtils.QT(taskObj.getTaskId()), false);

                            mDbUtil.deleteSQL("TaskImageDetails", "TaskId=" + StringUtils.QT(taskObj.getTaskId()), false);

                        }
                    }

                    // Insert Task into TaskMaster
                    columns_new = "taskid,taskcode,taskdesc,upload ,taskowner,date,usercreated,DueDate,CategoryId,EndDate,Status,IsServerTask";

                    value_new = finalTid + "," + StringUtils.QT(title) + "," + StringUtils.QT(name) + ","
                            + "'N'," + finalTaskOwner + ", " + date + ",1,"
                            + DatabaseUtils.sqlEscapeString(DateTimeUtils
                            .convertToServerDateFormat(
                                    taskObj.getTaskDueDate(),
                                    ConfigurationMasterHelper.outDateFormat)) + ","
                            + taskObj.getTaskCategoryID() + "," + endDate + "," + finalStatus + "," + 0;

                    mDbUtil.insertSQL("TaskMaster", columns_new, value_new);


                    //add task created images into TaskImageDetails table
                    columns_new = "TaskId,TaskImageId,TaskImageName,Upload,Status";
                    String imgId;
                    String folderName = "Task/"
                            + appDataProvider.getUser().getDownloadDate()
                            .replace("/", "") + "/"
                            + appDataProvider.getUser().getUserid() + "/";

                    for (TaskDataBO imgBO : taskImgList) {
                        if (!imgBO.getTaskImg().isEmpty()) {
                            // Generate Unique ID for image
                            imgId = StringUtils.QT(appDataProvider.getUser()
                                    .getUserid() + DateTimeUtils.now(DateTimeUtils.DATE_TIME_ID));

                            value_new = finalTid + "," + imgId + "," + StringUtils.QT(folderName + imgBO.getTaskImg())
                                    + "," + "'N'" + "," + finalStatus;

                            mDbUtil.insertSQL("TaskImageDetails", columns_new, value_new);
                        }
                    }


                    return true;
                } catch (
                        Exception e) {
                    Commons.printException(e);
                }
                return false;
            }
        }).flatMap(new Function<Boolean, SingleSource<? extends Boolean>>() {
            @Override
            public SingleSource<? extends Boolean> apply(Boolean aBoolean) throws Exception {

                return Single.fromCallable(new Callable<Boolean>() {
                    @Override
                    public Boolean call() throws Exception {


                        try {
                            String columns = "taskid,retailerid,usercreated,upload,date,uid,userid,channelid";
                            String values;
                            if (selectedId == -1) {// for all channel
                                String[] chrid = getChannelRetailerId(0);
                                for (String aChrid : chrid) {

                                    values = finalTid + "," + aChrid + "," + "1" + "," + "'N'," + date + "," + uID + "," + "0" + "," + "0";
                                    mDbUtil.insertSQL(DataMembers.tbl_TaskConfigurationMaster,
                                            columns, values);
                                }

                            } else if (mode.equals("seller")) {

                                values = finalTid + "," + 0 + "," + "1" + "," + "'N'," + date + "," + uID + "," + selectedId + "," + "0";
                                mDbUtil.insertSQL(DataMembers.tbl_TaskConfigurationMaster, columns,
                                        values);
                            } else if (mode.equals("retailer")) {
                                if (selectedId == -2) {
                                    String[] chrid = getRetailerIdlist();
                                    for (String aChrid : chrid) {

                                        values = finalTid + "," + aChrid + "," + "1" + ","
                                                + "'N'," + date + "," + uID + "," + "0" + "," + "0";
                                        mDbUtil.insertSQL(DataMembers.tbl_TaskConfigurationMaster,
                                                columns, values);
                                    }
                                } else {
                                    values = finalTid + "," + selectedId + "," + "1" + "," + "'N'," + date + "," + uID + "," + "0" + "," + "0";
                                    mDbUtil.insertSQL(DataMembers.tbl_TaskConfigurationMaster,
                                            columns, values);
                                }
                            } else {

                                String[] chrid = getChannelRetailerId(selectedId);
                                for (String aChrid : chrid) {
                                    values = finalTid + "," + aChrid + "," + "1" + "," + "'N'," + date + "," + uID + "," + "0" + "," + selectedId;
                                    mDbUtil.insertSQL(DataMembers.tbl_TaskConfigurationMaster,
                                            columns, values);
                                }
                            }
                            shutDownDb();
                            return true;
                        } catch (Exception e) {
                            Commons.printException(e);
                        }
                        shutDownDb();
                        return false;
                    }
                });
            }
        });
    }

    /**
     * Fetch Retailer's
     *
     * @return ArrayList
     */
    @Override
    public Observable<ArrayList<RetailerMasterBO>> fetchAllRetailers() {
        return Observable.fromCallable(new Callable<ArrayList<RetailerMasterBO>>() {
            @Override
            public ArrayList<RetailerMasterBO> call() throws Exception {
                try {
                    RetailerMasterBO temp;
                    ArrayList<RetailerMasterBO> retailerMaster = new ArrayList<>();
                    for (RetailerMasterBO retBo : appDataProvider.getRetailerMasters()) {

                        temp = new RetailerMasterBO();
                        temp.setRetailerID(retBo.getRetailerID());
                        temp.setTretailerName(retBo.getRetailerName());
                        retailerMaster.add(temp);
                    }
                    return retailerMaster;
                } catch (Exception e) {
                    Commons.printException(e);
                }
                return new ArrayList<>();
            }
        });
    }

    /**
     * This method used to fetch task product level
     *
     * @param prodLevelId
     * @return
     */
    @Override
    public Observable<ArrayList<TaskDataBO>> fetchTaskCategories(int prodLevelId) {
        return Observable.fromCallable(new Callable<ArrayList<TaskDataBO>>() {
            @Override
            public ArrayList<TaskDataBO> call() throws Exception {
                try {
                    initDb();

                    String query = "SELECT Distinct PName, PId FROM ProductMaster" +
                            " WHERE PLid = " + prodLevelId;

                    ArrayList<TaskDataBO> taskCategoryList = new ArrayList<>();
                    Cursor c = mDbUtil
                            .selectSQL(query);
                    if (c != null) {
                        while (c.moveToNext()) {
                            TaskDataBO taskmasterbo = new TaskDataBO();
                            taskmasterbo.setTaskCategoryDsc(c.getString(0));
                            taskmasterbo.setTaskCategoryID(c.getInt(1));
                            taskmasterbo.setFlag(1);
                            taskCategoryList.add(taskmasterbo);
                        }
                        c.close();
                        shutDownDb();
                        return taskCategoryList;
                    }

                } catch (Exception ignore) {

                }
                shutDownDb();
                return new ArrayList<>();
            }
        });
    }

    @Override
    public Observable<ArrayList<TaskDataBO>> fetTaskImgData(String taskId, int userIdLength) {
        return Observable.fromCallable(new Callable<ArrayList<TaskDataBO>>() {
            @Override
            public ArrayList<TaskDataBO> call() throws Exception {
                try {
                    initDb();
                    int subStrStartWith = 15 + String.valueOf(userIdLength).length();

                    String query = "SELECT SUBSTR(TMD.TaskImageName," + subStrStartWith + ") as ImageName FROM TaskImageDetails TMD"
                            + " INNER JOIN TaskMaster TM ON TM.taskId = TMD.TaskId"
                            + " WHERE (TMD.Status!='D' OR TMD.Status IS NULL) AND TMD.TaskId = " + StringUtils.QT(taskId);


                    ArrayList<TaskDataBO> taskImgList = new ArrayList<>();
                    Cursor c = mDbUtil
                            .selectSQL(query);
                    if (c != null) {
                        while (c.moveToNext()) {
                            TaskDataBO taskImgBo = new TaskDataBO();
                            taskImgBo.setTaskImg(c.getString(0));
                            taskImgBo.setTaskImgPath(TaskConstant.TASK_SERVER_IMG_PATH);
                            taskImgList.add(taskImgBo);
                        }
                        c.close();
                        shutDownDb();
                        return taskImgList;
                    }

                } catch (Exception e) {
                    Commons.printException(e);
                }
                shutDownDb();
                return new ArrayList<>();
            }
        });
    }


    @Override
    public Single<Boolean> deleteTaskData(String taskId, String taskOwner, int serverTask) {
        return Single.fromCallable(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                try {
                    boolean isFlag;
                    initDb();

                    Cursor c = mDbUtil.selectSQL("Select taskId from TaskMaster Where taskid=" + StringUtils.QT(taskId) + " And Upload='Y'");

                    if (c.getCount() > 0) {
                        isFlag = true;
                        c.close();
                    } else {
                        isFlag = false;
                    }
                    return isFlag;
                } catch (Exception e) {
                    Commons.printException(e);
                }
                return false;
            }
        }).flatMap(new Function<Boolean, SingleSource<? extends Boolean>>() {
            @Override
            public SingleSource<? extends Boolean> apply(Boolean isUploaded) throws Exception {
                return Single.fromCallable(new Callable<Boolean>() {
                    @Override
                    public Boolean call() throws Exception {
                        try {
                            if (serverTask == 1 || isUploaded) {

                                mDbUtil.updateSQL("UPDATE TaskMaster " +
                                        "SET status='D',Upload='N' WHERE taskid=" + StringUtils.QT(taskId));

                                mDbUtil.updateSQL("UPDATE TaskImageDetails " +
                                        "SET status='D',Upload='N' WHERE TaskId=" + StringUtils.QT(taskId));
                            } else {
                                mDbUtil.deleteSQL("TaskMaster", "taskid=" + StringUtils.QT(taskId), false);

                                mDbUtil.deleteSQL(DataMembers.tbl_TaskConfigurationMaster, "taskid=" + StringUtils.QT(taskId), false);

                                mDbUtil.deleteSQL("TaskImageDetails", "TaskId=" + StringUtils.QT(taskId), false);

                            }
                            shutDownDb();
                            return true;
                        } catch (Exception ignore) {

                        }
                        shutDownDb();
                        return false;
                    }
                });
            }
        });
    }

    @Override
    public Observable<ArrayList<String>> getDeletedImageList(String taskId) {
        return Observable.fromCallable(new Callable<ArrayList<String>>() {
            @Override
            public ArrayList<String> call() throws Exception {
                try {
                    ArrayList<String> deletedImgList = new ArrayList<>();

                    initDb();

                    String query = "SELECT SUBSTR(TaskImageName,18) FROM TaskImageDetails"
                            + " WHERE TaskId = " + StringUtils.QT(taskId);

                    Cursor c = mDbUtil.selectSQL(query);
                    if (c != null) {
                        while (c.moveToNext()) {
                            deletedImgList.add(c.getString(0));
                        }
                        c.close();
                        shutDownDb();
                        return deletedImgList;
                    }

                } catch (Exception e) {
                    Commons.printException(e);
                }
                shutDownDb();
                return new ArrayList<>();
            }
        });
    }


    private String[] getChannelRetailerId(int channelId) {
        ArrayList<String> channelRId = new ArrayList<>();
        int siz = appDataProvider.getRetailerMasters().size();
        if (channelId == 0) {
            for (int ii = 0; ii < siz; ii++) {

                if (((appDataProvider.getRetailerMasters().get(ii).getIsToday() == 1))
                        || appDataProvider.getRetailerMasters().get(ii).getIsDeviated()
                        .equals("Y")) {
                    channelRId.add(appDataProvider.getRetailerMasters().get(ii)
                            .getRetailerID());
                }
            }
        } else {
            for (int ii = 0; ii < siz; ii++) {
                if (((appDataProvider.getRetailerMasters().get(ii).getIsToday() == 1) || appDataProvider
                        .getRetailerMasters().get(ii).getIsDeviated()
                        .equals("Y"))
                        && appDataProvider.getRetailerMasters().get(ii).getChannelID() == channelId) {
                    channelRId.add(appDataProvider.getRetailerMasters().get(ii)
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
        ArrayList<String> RId = new ArrayList<>();
        int siz = appDataProvider.getRetailerMasters().size();
        for (int ii = 0; ii < siz; ii++) {
            if (((appDataProvider.getRetailerMasters().get(ii).getIsToday() == 1))
                    || appDataProvider.getRetailerMasters().get(ii).getIsDeviated()
                    .equals("Y")) {
                RId.add(appDataProvider.getRetailerMasters().get(ii).getRetailerID());
            }
        }

        String data[] = new String[RId.size()];
        for (int i = 0; i < RId.size(); i++) {
            data[i] = RId.get(i);
        }
        return data;

    }

    @Override
    public void tearDown() {
        shutDownDb();
    }
}
