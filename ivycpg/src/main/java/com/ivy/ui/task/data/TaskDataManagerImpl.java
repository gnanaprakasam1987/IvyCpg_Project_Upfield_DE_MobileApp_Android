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
    public Observable<ArrayList<TaskDataBO>> fetchTaskData(String retailerId) {
        return Observable.fromCallable(new Callable<ArrayList<TaskDataBO>>() {
            @Override
            public ArrayList<TaskDataBO> call() {
                try {
                    if (mDbUtil.isDbNullOrClosed())
                        initDb();

                    ArrayList<TaskDataBO> taskDataBOS = new ArrayList<>();
                    String query = "select distinct A.taskid,B.taskcode,B.taskDesc,A.retailerId,A.upload,"
                            + "(CASE WHEN ifnull(TD.TaskId,0) >0 THEN 1 ELSE 0 END) as isDone,"
                            + "B.usercreated , B.taskowner , B.date, A.upload,A.channelid,A.userid,B.DueDate,B.CategoryId,PL.PName"
                            + " from TaskConfigurationMaster A inner join TaskMaster B on A.taskid=B.taskid"
                            + " left join TaskExecutionDetails TD on TD.TaskId=A.taskid and TD.RetailerId = " + retailerId
                            + " left join ProductMaster PL on PL.PID=B.CategoryId"
                            + " left join RetailerMaster RM on RM.RetailerID=A.retailerId"
                            + " where B.Status!='D'"
                            + " and A.TaskId not in (Select taskid from TaskHistory where RetailerId =" + retailerId + ")";

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

                            if (taskmasterbo.getUserId() != 0)
                                taskmasterbo.setMode("seller");
                            else if (taskmasterbo.getChannelId() != 0)
                                taskmasterbo.setMode("channel");
                            else
                                taskmasterbo.setMode("retailer");

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
            }
        });
    }

    @Override
    public Observable<ArrayList<TaskDataBO>> fetchCompletedTask(String retailerId) {
        return null;
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
                    if (mDbUtil.isDbNullOrClosed())
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
                    if (mDbUtil.isDbNullOrClosed())
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
                    if (mDbUtil.isDbNullOrClosed())
                        initDb();
                    mDbUtil.deleteSQL("TaskExecutionDetails", "TaskId=" + taskDataBO.getTaskId() + " and RetailerId = " + retailerId, false);

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


                        String columns = "TaskId,RetailerId,Date,UId,Upload,ImageName";
                        String values;

                        try {
                            if (taskDataBO.isChecked()) {
                                values = taskDataBO.getTaskId() + ","
                                        + StringUtils.QT(retailerId) + ","
                                        + StringUtils.QT(DateTimeUtils.now(DateTimeUtils.DATE_GLOBAL)) + ","
                                        + uID + ",'N'" + ","
                                        + StringUtils.QT(taskDataBO.getTaskEvidenceImg());
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

        // Remove single quotes
        String name = taskObj.getTaskDesc().replaceAll("'", " ");
        String title = taskObj.getTasktitle().replaceAll("'", " ");
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
                && !taskObj.getTaskOwner().equalsIgnoreCase("Self")) {
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
                    if (mDbUtil.isDbNullOrClosed())
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
                    columns_new = "taskid,taskcode,taskdesc,upload ,taskowner,date,usercreated,DueDate,CategoryId,EndDate,Status";

                    value_new = finalTid + "," + StringUtils.QT(title) + "," + StringUtils.QT(name) + ","
                            + "'N'," + finalTaskOwner + ", " + date + ",1,"
                            + DatabaseUtils.sqlEscapeString(DateTimeUtils
                            .convertToServerDateFormat(
                                    taskObj.getTaskDueDate(),
                                    ConfigurationMasterHelper.outDateFormat)) + ","
                            + taskObj.getTaskCategoryID() + "," + endDate + "," + finalStatus;

                    mDbUtil.insertSQL("TaskMaster", columns_new, value_new);


                    //add task created images into TaskImageDetails table
                    columns_new = "TaskId,TaskImageId,TaskImageName,ImageType,Upload,Status";
                    String imgId;
                    for (TaskDataBO imgBO : taskImgList) {
                        if (!imgBO.getTaskImg().isEmpty()) {
                            // Generate Unique ID for image
                            imgId = StringUtils.QT(appDataProvider.getUser()
                                    .getUserid() + DateTimeUtils.now(DateTimeUtils.DATE_TIME_ID));

                            value_new = finalTid + "," + imgId + "," + StringUtils.QT(imgBO.getTaskImg())
                                    + "," + imgBO.getTaskImgType() + "," + "'N'" + "," + finalStatus;

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
        }).

                flatMap(new Function<Boolean, SingleSource<? extends Boolean>>() {
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
    public Observable<ArrayList<RetailerMasterBO>> fetchRetailers() {
        return Observable.fromCallable(new Callable<ArrayList<RetailerMasterBO>>() {
            @Override
            public ArrayList<RetailerMasterBO> call() throws Exception {
                try {
                    RetailerMasterBO temp;
                    ArrayList<RetailerMasterBO> retailerMaster = new ArrayList<>();
                    int siz = appDataProvider.getRetailerMasters().size();
                    for (int ii = 0; ii < siz; ii++) {
                        if (((appDataProvider
                                .getRetailerMasters().get(ii).getIsToday() == 1)) || appDataProvider.getRetailerMasters().get(ii).getIsDeviated()
                                .equals("Y")) {
                            temp = new RetailerMasterBO();
                            temp.setTretailerId(SDUtil.convertToInt(appDataProvider.getRetailerMasters().get(ii).getRetailerID()));
                            temp.setTretailerName(appDataProvider.getRetailerMasters().get(ii).getRetailerName());
                            retailerMaster.add(temp);
                        }
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
     * @param menuCode
     * @return
     */
    @Override
    public Observable<ArrayList<TaskDataBO>> fetchTaskCategories(String menuCode) {
        return Observable.fromCallable(new Callable<ArrayList<TaskDataBO>>() {
            @Override
            public ArrayList<TaskDataBO> call() throws Exception {
                try {
                    if (mDbUtil.isDbNullOrClosed())
                        initDb();

                    String query = "SELECT Distinct PName, PId FROM ProductMaster PM" +
                            " INNER JOIN ConfigActivityFilter CAF ON CAF.ProductFilter1 = PM.PLid" +
                            " WHERE CAF.ActivityCode = " + StringUtils.QT(menuCode);

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
    public Observable<ArrayList<TaskDataBO>> fetTaskImgData(String taskId) {
        return Observable.fromCallable(new Callable<ArrayList<TaskDataBO>>() {
            @Override
            public ArrayList<TaskDataBO> call() throws Exception {
                try {
                    if (mDbUtil.isDbNullOrClosed())
                        initDb();

                    String query = "SELECT TaskImageName,ImageType FROM TaskImageDetails"
                            + " WHERE Status!='D' AND TaskId = " + StringUtils.QT(taskId);

                    ArrayList<TaskDataBO> taskImgList = new ArrayList<>();
                    Cursor c = mDbUtil
                            .selectSQL(query);
                    if (c != null) {
                        while (c.moveToNext()) {
                            TaskDataBO taskImgBo = new TaskDataBO();
                            taskImgBo.setTaskImg(c.getString(0));
                            taskImgBo.setTaskImgType(c.getString(1));
                            taskImgList.add(taskImgBo);
                        }
                        c.close();
                        shutDownDb();
                        return taskImgList;
                    }

                } catch (Exception ignore) {

                }
                shutDownDb();
                return new ArrayList<>();
            }
        });
    }

    @Override
    public Single<Boolean> deleteTaskData(String taskId, String taskOwner) {
        return Single.fromCallable(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                try {
                    if (mDbUtil.isDbNullOrClosed())
                        initDb();

                    if (!taskOwner.equalsIgnoreCase("self")) {

                        mDbUtil.updateSQL("UPDATE TaskMaster " +
                                "SET status='D' WHERE taskid=" + StringUtils.QT(taskId));

                        mDbUtil.updateSQL("UPDATE TaskImageDetails " +
                                "SET status='D' WHERE TaskId=" + StringUtils.QT(taskId));
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

    @Override
    public Observable<ArrayList<String>> getDeletedImageList(String taskId) {
        return Observable.fromCallable(new Callable<ArrayList<String>>() {
            @Override
            public ArrayList<String> call() throws Exception {
                try {
                    if (mDbUtil.isDbNullOrClosed())
                        initDb();

                    String query = "SELECT TaskImageName FROM TaskImageDetails"
                            + " WHERE TaskId = " + StringUtils.QT(taskId);

                    ArrayList<String> deletedImgList = new ArrayList<>();
                    Cursor c = mDbUtil.selectSQL(query);
                    if (c != null) {
                        while (c.moveToNext()) {
                            deletedImgList.add(c.getString(0));
                        }
                        c.close();
                        shutDownDb();
                        return deletedImgList;
                    }

                } catch (Exception ignore) {

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
