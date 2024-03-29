package com.ivy.ui.task.data;

import android.database.Cursor;
import android.database.DatabaseUtils;

import com.ivy.core.data.datamanager.DataManager;
import com.ivy.core.di.scope.DataBaseInfo;
import com.ivy.lib.existing.DBUtil;
import com.ivy.sd.png.bo.RetailerMasterBO;
import com.ivy.sd.png.commons.SDUtil;
import com.ivy.sd.png.provider.ConfigurationMasterHelper;
import com.ivy.sd.png.util.Commons;
import com.ivy.sd.png.util.DataMembers;
import com.ivy.ui.task.TaskConstant;
import com.ivy.ui.task.model.FilterBo;
import com.ivy.ui.task.model.TaskDataBO;
import com.ivy.ui.task.model.TaskRetailerBo;
import com.ivy.utils.DateTimeUtils;
import com.ivy.utils.StringUtils;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.concurrent.Callable;

import javax.inject.Inject;

import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.SingleSource;
import io.reactivex.functions.Function;

public class TaskDataManagerImpl implements TaskDataManager {

    private DBUtil mDbUtil;
    private DataManager dataManager;

    @Inject
    public TaskDataManagerImpl(@DataBaseInfo DBUtil mDbUtil, DataManager dataManager) {
        this.mDbUtil = mDbUtil;
        this.dataManager = dataManager;
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
    public Observable<ArrayList<TaskDataBO>> fetchTaskData(int tabPos, String retailerId, int userCreatedTask, boolean isDelegate) {
        return Observable.fromCallable(() -> {
            try {
                initDb();

                String userCreated = " and B.usercreated =" + userCreatedTask + " and (TD.ReasonId=0 OR TD.ReasonId IS NULL)";
                String userIdCond = "";
                String retailerIdCond = "";

                int subStrStartWith = 16 + String.valueOf(dataManager.getUser().getUserid()).length();

                if (tabPos != 0) {
                    userIdCond = " and A.UserId=" + dataManager.getUser().getUserid();
                }

                if (tabPos != 0 || !retailerId.equals("0"))
                    retailerIdCond = " and A.retailerId=" + retailerId;

                if (isDelegate)
                    userIdCond = " and A.UserId!=" + dataManager.getUser().getUserid();

                ArrayList<TaskDataBO> taskDataBOS = new ArrayList<>();
                String query = "select distinct A.taskid,B.taskcode,B.taskDesc,A.retailerId,A.upload,"
                        + "(CASE WHEN ifnull(TD.TaskId,0) >0 THEN 1 ELSE 0 END) as isDone,"
                        + "B.usercreated , B.taskowner , B.date, A.upload,A.channelid,A.userid,"
                        + "IFNULL(B.DueDate,''),B.CategoryId,IFNULL(PL.PName,''),"
                        + "B.IsServerTask,SUBSTR(TD.ImageName," + subStrStartWith + ") as eveImage,IFNULL(UM.Relationship,'') as userType,RM.retailerName"
                        + " from TaskConfigurationMaster A inner join TaskMaster B on A.taskid=B.taskid"
                        + " left join TaskExecutionDetails TD on TD.TaskId=A.taskid and TD.RetailerId = " + retailerId
                        + " left join ProductMaster PL on PL.PID=B.CategoryId"
                        + " left join RetailerMaster RM on RM.RetailerID=A.retailerId"
                        + " left join UserMaster UM on UM.userid=A.userid"
                        + " where (B.Status!='D' OR B.Status IS NULL)" + retailerIdCond + userIdCond
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
                        if (taskmasterbo.getIsdone().equals("1"))
                            taskmasterbo.setChecked(true);
                        else
                            taskmasterbo.setChecked(false);

                        if (c.getString(17).isEmpty())
                            taskmasterbo.setMode(TaskConstant.SELLER_WISE);
                        else
                            taskmasterbo.setMode(c.getString(17).toLowerCase());

                        taskmasterbo.setRetailerName(c.getString(18));

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

                    String query = "select distinct B.taskid,B.taskcode,B.taskDesc,TH.retailerId,(CASE WHEN ifnull(TH.TaskId,0) >0 THEN 1 ELSE 0 END)" +
                            "as isDone,B.usercreated , B.taskowner , B.date,A.channelid,A.userid,IFNULL(B.DueDate,''),B.CategoryId,IFNULL(PL.PName,''),TH.ExecutionDate" +
                            " from TaskMaster B" +
                            " inner join TaskHistory TH on TH.TaskId=B.taskid and TH.RetailerId =" + retailerId +
                            " left join ProductMaster PL on PL.PID=B.CategoryId " +
                            " left join RetailerMaster RM on RM.RetailerID=TH.retailerId " +
                            " left join TaskConfigurationMaster A on A.taskId = B.taskId" +

                            " UNION ALL " +

                            " select distinct B.taskid,B.taskcode,B.taskDesc,TD.retailerId,(CASE WHEN ifnull(TD.TaskId,0) >0 THEN 1 ELSE 0 END)" +
                            "as isDone,B.usercreated , B.taskowner , B.date,A.channelid,A.userid,IFNULL(B.DueDate,''),B.CategoryId,IFNULL(PL.PName,''),TD.Date" +
                            " from TaskMaster B inner join TaskExecutionDetails TD on TD.TaskId=A.taskid and  A.retailerId=" + retailerId +
                            " left join TaskConfigurationMaster A on A.taskId = B.taskId" +
                            " left join ProductMaster PL on PL.PID=B.CategoryId" +
                            " left join RetailerMaster RM on RM.RetailerID=TD.retailerId  where TD.ReasonId!=0";

                    Cursor c = mDbUtil
                            .selectSQL(query);
                    if (c != null) {
                        while (c.moveToNext()) {
                            TaskDataBO taskmasterbo = new TaskDataBO();
                            taskmasterbo.setTaskId(c.getString(0));
                            taskmasterbo.setTasktitle(c.getString(1));
                            taskmasterbo.setTaskDesc(c.getString(2));
                            taskmasterbo.setRid(c.getInt(3));

                            taskmasterbo.setIsdone(c.getString(4));
                            taskmasterbo.setUsercreated(c.getString(5));
                            taskmasterbo.setTaskOwner(c.getString(6));
                            taskmasterbo.setCreatedDate(c.getString(7));

                            taskmasterbo.setChannelId(c.getInt(8));
                            taskmasterbo.setUserId(c.getInt(9));
                            taskmasterbo.setTaskDueDate(c.getString(10));
                            taskmasterbo.setTaskCategoryID(c.getInt(11));
                            taskmasterbo.setTaskCategoryDsc(c.getString(12));
                            taskmasterbo.setTaskExecDate(c.getString(13));

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
                            + "TCM.channelid=" + dataManager.getRetailMaster().getSubchannelid() + ")and TM.taskid not in(select TEC.taskid from TaskExecutionDetails TEC where TEC.retailerId='" + dataManager.getRetailMaster().getRetailerID() + "')";
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
     * @return
     */
    @Override
    public Single<Boolean> updateTaskExecutionData(TaskDataBO taskDataBO, int reasonId) {
        return Single.fromCallable(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                try {
                    initDb();
                    mDbUtil.deleteSQL("TaskExecutionDetails", "TaskId=" + StringUtils.getStringQueryParam(taskDataBO.getTaskId()) + " and RetailerId = " + taskDataBO.getRid(), false);

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
                        if (taskDataBO.getRid() == 0)
                            uID = StringUtils.getStringQueryParam(dataManager.getUser().getUserid()
                                    + DateTimeUtils.now(DateTimeUtils.DATE_TIME_ID_MILLIS));
                        else
                            uID = StringUtils.getStringQueryParam(taskDataBO.getRid()
                                    + DateTimeUtils.now(DateTimeUtils.DATE_TIME_ID_MILLIS));


                        String columns = "TaskId,RetailerId,Date,UId,Upload,ImageName,ReasonId,Remarks";
                        String values;
                        String taskEvdImage = taskDataBO.getTaskEvidenceImg() == null ? null : StringUtils.getStringQueryParam(taskDataBO.getTaskEvidenceImg());

                        try {
                            if (taskDataBO.isChecked()
                                    || reasonId != 0) {
                                values = StringUtils.getStringQueryParam(taskDataBO.getTaskId()) + ","
                                        + taskDataBO.getRid() + ","
                                        + StringUtils.getStringQueryParam(DateTimeUtils.now(DateTimeUtils.DATE_GLOBAL)) + ","
                                        + uID + ",'N'" + ","
                                        + taskEvdImage + ","
                                        + reasonId + ","
                                        + StringUtils.getStringQueryParam(taskDataBO.getRemark());
                                mDbUtil.insertSQL("TaskExecutionDetails", columns, values);
                            } else {

                                Cursor c = mDbUtil.selectSQL("Select * from TaskExecutionDetails");
                                if (c.getCount() == 0) {

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
                            + dataManager.getUser().getDownloadDate()
                            .replace("/", "") + "/"
                            + dataManager.getUser().getUserid() + "/";

                    mDbUtil.updateSQL("UPDATE TaskExecutionDetails "
                            + " SET ImageName=" + StringUtils.getStringQueryParam(folderName + imageName)
                            + " WHERE TaskId=" + StringUtils.getStringQueryParam(taskId));

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
     * @param taskImgList
     * @return
     */
    @Override
    public Single<Boolean> saveTask(int selectedId, TaskDataBO taskObj,
                                    ArrayList<TaskDataBO> taskImgList, int linkUserId, String deletedImgIds) {

        //remove Quotes
        String title = DatabaseUtils.sqlEscapeString(taskObj.getTasktitle());
        String name = DatabaseUtils.sqlEscapeString(taskObj.getTaskDesc());


        String taskOwner = StringUtils.getStringQueryParam("self");
        String status = StringUtils.getStringQueryParam("I");
        // Generate Unique ID
        String id = StringUtils.getStringQueryParam(dataManager.getUser()
                .getUserid() + DateTimeUtils.now(DateTimeUtils.DATE_TIME_ID));

        String date = StringUtils.getStringQueryParam(DateTimeUtils.now(DateTimeUtils.DATE_GLOBAL));

        String endDate = StringUtils.getStringQueryParam(DateTimeUtils.getRequestedDateByGetType(1, Calendar.YEAR));

        String uID = StringUtils.getStringQueryParam(selectedId
                + DateTimeUtils.now(DateTimeUtils.DATE_TIME_ID_MILLIS));

        if (!taskObj.getTaskOwner().isEmpty()
                && (!taskObj.getTaskOwner().equalsIgnoreCase("Self")
                || taskObj.getUpload().equalsIgnoreCase("Y"))) {
            id = StringUtils.getStringQueryParam(taskObj.getTaskId());
            taskOwner = StringUtils.getStringQueryParam(taskObj.getTaskOwner());
            status = StringUtils.getStringQueryParam("U");
        }

        String finalTid = id;
        String finalTaskOwner = taskOwner;
        String finalStatus = status;
        return Single.fromCallable(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {

                String columns_new;
                String value_new;
                ArrayList<String> existingImageNames=new ArrayList<>();


                try {
                    initDb();

                    String sb = "Select count(taskid) from TaskMaster where taskid =" + StringUtils.getStringQueryParam(taskObj.getTaskId());

                    Cursor c = mDbUtil.selectSQL(sb);
                    if (c.getCount() > 0) {
                        if (c.moveToNext()) {

                            mDbUtil.deleteSQL("TaskMaster", "taskid=" + StringUtils.getStringQueryParam(taskObj.getTaskId()), false);

                            mDbUtil.deleteSQL(DataMembers.tbl_TaskConfigurationMaster, "taskid=" + StringUtils.getStringQueryParam(taskObj.getTaskId()), false);

                            mDbUtil.deleteSQL("TaskImageDetails", "TaskId= (Select taskid from TaskMaster where taskid =" + StringUtils.getStringQueryParam(taskObj.getTaskId()) + " and IsServerTask=0)", false);

                        }
                    }

                    // Insert Task into TaskMaster
                    columns_new = "taskid,taskcode,taskdesc,upload ,taskowner,date,usercreated,DueDate,CategoryId,EndDate,Status,IsServerTask";

                    value_new = finalTid + "," + title + "," + name + ","
                            + "'N'," + finalTaskOwner + ", " + date + ",1,"
                            + DatabaseUtils.sqlEscapeString(DateTimeUtils
                            .convertToServerDateFormat(
                                    taskObj.getTaskDueDate(),
                                    ConfigurationMasterHelper.outDateFormat)) + ","
                            + taskObj.getTaskCategoryID() + "," + endDate + "," + finalStatus + "," + taskObj.getServerTask();

                    mDbUtil.insertSQL("TaskMaster", columns_new, value_new);

                    // add existing task images to verify while update task.
                    Cursor cursor = mDbUtil.selectSQL("SELECT SUBSTR(TaskImageName,19) as ImageName from TaskImageDetails where taskid =" + StringUtils.getStringQueryParam(taskObj.getTaskId()));
                    if (cursor.getCount() > 0) {
                        while (cursor.moveToNext()) {
                            existingImageNames.add(cursor.getString(0));
                        }
                    }
                    cursor.close();

                    //add task created images into TaskImageDetails table
                    columns_new = "TaskId,TaskImageId,TaskImageName,Upload,Status,IsServerTask";
                    String imgId;
                    String folderName = "Task/"
                            + dataManager.getUser().getDownloadDate()
                            .replace("/", "") + "/"
                            + dataManager.getUser().getUserid() + "/";

                    for (TaskDataBO imgBO : taskImgList) {
                        if (!imgBO.getTaskImg().isEmpty() && !checkTaskImage(imgBO.getTaskImg(),existingImageNames)) {
                            // Generate Unique ID for image
                            imgId = StringUtils.getStringQueryParam(dataManager.getUser()
                                    .getUserid() + DateTimeUtils.now(DateTimeUtils.DATE_TIME_ID));

                            value_new = finalTid + "," + imgId + "," + StringUtils.getStringQueryParam(folderName + imgBO.getTaskImg())
                                    + "," + "'N'" + "," + "'I'" + "," + taskObj.getServerTask();

                            mDbUtil.insertSQL("TaskImageDetails", columns_new, value_new);
                        }
                    }

                    if (!deletedImgIds.isEmpty())
                        mDbUtil.updateSQL("UPDATE TaskImageDetails " +
                                "SET status='D',Upload='N' WHERE TaskImageId in(" + deletedImgIds + ")  AND TaskId=" + StringUtils.getStringQueryParam(taskObj.getTaskId()));


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
                            if (taskObj.getMode().equals(TaskConstant.RETAILER_WISE)) {

                                values = finalTid + "," + selectedId + "," + "1" + "," + "'N'," + date + "," + uID + "," + linkUserId + "," + "0";
                                mDbUtil.insertSQL(DataMembers.tbl_TaskConfigurationMaster,
                                        columns, values);
                            } else {

                                values = finalTid + "," + 0 + "," + "1" + "," + "'N'," + date + "," + uID + "," + selectedId + "," + "0";
                                mDbUtil.insertSQL(DataMembers.tbl_TaskConfigurationMaster, columns,
                                        values);
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

    private boolean checkTaskImage(String imageName,ArrayList<String> existingImages){
        try{
            for(String existingImgName: existingImages) {
                if (!existingImgName.isEmpty() && existingImgName.equals(imageName)) {
                    return true;
                }
            }
        }catch (Exception ex){
            Commons.printException(ex);
        }
        return false;
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
                    for (RetailerMasterBO retBo : dataManager.getRetailerMasters()) {

                        temp = new RetailerMasterBO();
                        temp.setRetailerID(retBo.getRetailerID());
                        temp.setRetailerName(retBo.getRetailerName());
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
    public Observable<HashMap<String, ArrayList<FilterBo>>> fetchFilterData(int prodLevelId, boolean isFromHomeSrc) {
        return Observable.fromCallable(new Callable<HashMap<String, ArrayList<FilterBo>>>() {
            @Override
            public HashMap<String, ArrayList<FilterBo>> call() throws Exception {
                try {
                    HashMap<String, ArrayList<FilterBo>> hashMapFilterList = new HashMap<>();
                    ArrayList<FilterBo> filterBoList = new ArrayList<>();
                    if (isFromHomeSrc) {
                        for (RetailerMasterBO retBo : dataManager.getRetailerMasters()) {
                            filterBoList.add(new FilterBo(SDUtil.convertToInt(retBo.getRetailerID()), retBo.getRetailerName()));
                        }
                        hashMapFilterList.put("Retailer", filterBoList);
                    }


                    initDb();
                    String query = "SELECT Distinct PM.PName, PM.PId,PM.Plid,PL.LevelName " +
                            " from ProductMaster PM" +
                            " inner join TaskMaster TM ON TM.CategoryId=PM.PID OR PM.PLid = " + prodLevelId +
                            " left join (select levelId,LevelName from ProductLevel) PL on PL.LevelId = PM.PLid";

                    Cursor c = mDbUtil
                            .selectSQL(query);
                    if (c != null) {
                        while (c.moveToNext()) {
                            FilterBo filterBo = new FilterBo();
                            filterBo.setFilterName(c.getString(0));
                            filterBo.setFilterId(c.getInt(1));
                            filterBo.setProdLevelId(c.getInt(2));

                            if (hashMapFilterList.get(c.getString(3)) != null) {
                                ArrayList<FilterBo> filterBoList2 = hashMapFilterList.get(c.getString(3));
                                filterBoList2.add(filterBo);
                            } else {
                                filterBoList = new ArrayList<>();
                                filterBoList.add(filterBo);
                                hashMapFilterList.put(c.getString(3), filterBoList);
                            }
                        }


                        c.close();
                    }
                    shutDownDb();
                    return hashMapFilterList;

                } catch (Exception ignore) {

                }
                shutDownDb();
                return new HashMap<>();
            }
        });
    }

    @Override
    public Observable<ArrayList<TaskDataBO>> fetchTaskImageData(String taskId, int userIdLength) {
        return Observable.fromCallable(new Callable<ArrayList<TaskDataBO>>() {
            @Override
            public ArrayList<TaskDataBO> call() throws Exception {
                try {
                    initDb();
                    int subStrStartWith = 16 + String.valueOf(userIdLength).length();

                    String query = "SELECT SUBSTR(TMD.TaskImageName," + subStrStartWith + ") as ImageName,TM.IsServerTask,TMD.TaskImageId" +
                            " FROM TaskImageDetails TMD" +
                            " INNER JOIN TaskMaster TM ON TM.taskId = TMD.TaskId" +
                            " WHERE (TMD.Status!='D' OR TMD.Status IS NULL) AND TMD.TaskId = " + StringUtils.getStringQueryParam(taskId);


                    ArrayList<TaskDataBO> taskImgList = new ArrayList<>();
                    Cursor c = mDbUtil
                            .selectSQL(query);
                    if (c != null) {
                        while (c.moveToNext()) {
                            TaskDataBO taskImgBo = new TaskDataBO();
                            taskImgBo.setTaskImg(c.getString(0));
                            taskImgBo.setTaskImgPath(TaskConstant.TASK_SERVER_IMG_PATH);
                            taskImgBo.setServerTask(c.getInt(1));
                            taskImgBo.setTaskImgId(c.getString(2));
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

                    Cursor c = mDbUtil.selectSQL("Select taskId from TaskMaster Where taskid=" + StringUtils.getStringQueryParam(taskId) + " And Upload='Y'");

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
                                        "SET status='D',Upload='N' WHERE taskid=" + StringUtils.getStringQueryParam(taskId));

                                mDbUtil.updateSQL("UPDATE TaskImageDetails " +
                                        "SET status='D',Upload='N' WHERE TaskId=" + StringUtils.getStringQueryParam(taskId));
                            } else {
                                mDbUtil.deleteSQL("TaskMaster", "taskid=" + StringUtils.getStringQueryParam(taskId), false);

                                mDbUtil.deleteSQL(DataMembers.tbl_TaskConfigurationMaster, "taskid=" + StringUtils.getStringQueryParam(taskId), false);

                                mDbUtil.deleteSQL("TaskImageDetails", "TaskId=" + StringUtils.getStringQueryParam(taskId), false);

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
                    int subStrStartWith = 16 + String.valueOf(dataManager.getUser().getUserid()).length();

                    String query = "SELECT SUBSTR(TaskImageName," + subStrStartWith + ") FROM TaskImageDetails"
                            + " WHERE TaskId = " + StringUtils.getStringQueryParam(taskId);

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

    @Override
    public Observable<HashMap<String, ArrayList<TaskDataBO>>> fetchUnPlanedTaskData(int toDateCount) {
        return Observable.fromCallable(() -> {
            try {
                HashMap<String, ArrayList<TaskDataBO>> unplannedTaskList = new HashMap<>();
                ArrayList<TaskDataBO> taskList = new ArrayList<>();
                initDb();
                String maxDueDate = DateTimeUtils.getRequestedDateByGetType(toDateCount, Calendar.DATE);

                String query = "select distinct A.taskid,B.taskcode,B.taskDesc,A.retailerId," +
                        "IFNULL(B.DueDate,'') as DueDate," +
                        "B.Date,B.CategoryId,IFNULL(PL.PName,''),B.taskowner" +
                        " from TaskConfigurationMaster A inner join TaskMaster B on A.taskid=B.taskid" +
                        " left join ProductMaster PL on PL.PID=B.CategoryId" +
                        " left join RetailerMaster RM on RM.RetailerID=A.retailerId" +
                        " left join DatewisePlan DWP on DWP.Date = B.DueDate" +
                        " and DWP.EntityId = A.retailerID and DWP.Status!='D' and DWP.EntityType = 'RETAILER'" +
                        " where B.DueDate<=" + StringUtils.getStringQueryParam(maxDueDate) + " and DWP.Date IS NULL and (B.Status!='D' OR B.Status IS NULL)" +
                        " and A.retailerId!=0 and A.TaskId not in (Select taskid from TaskHistory where RetailerId = A.retailerId)";

                Cursor c = mDbUtil.selectSQL(query);
                if (c != null) {
                    while (c.moveToNext()) {
                        TaskDataBO taskBo = new TaskDataBO();

                        taskBo.setTaskId(c.getString(0));
                        taskBo.setTasktitle(c.getString(1));
                        taskBo.setTaskDesc(c.getString(2));
                        taskBo.setRid(c.getInt(3));
                        taskBo.setTaskDueDate(c.getString(4));
                        taskBo.setCreatedDate(c.getString(5));
                        taskBo.setTaskCategoryID(c.getInt(6));
                        taskBo.setTaskCategoryDsc(c.getString(7));
                        taskBo.setTaskOwner(c.getString(8));

                        int daysCount = DateTimeUtils.getDateCount(DateTimeUtils.now(DateTimeUtils.DATE_GLOBAL),
                                taskBo.getTaskDueDate(), "yyyy/MM/dd");

                        taskBo.setNoOfDueDays(daysCount);

                        if (unplannedTaskList.get(c.getString(3)) != null) {
                            ArrayList<TaskDataBO> taskList2 = unplannedTaskList.get(c.getString(3));
                            taskList2.add(taskBo);
                        } else {
                            taskList = new ArrayList<>();
                            taskList.add(taskBo);
                            unplannedTaskList.put(c.getString(3), taskList);
                        }
                    }
                    c.close();
                }
                shutDownDb();
                return unplannedTaskList;

            } catch (Exception e) {
                shutDownDb();
            }
            return new HashMap<>();
        });
    }

    @Override
    public Observable<ArrayList<TaskRetailerBo>> fetchUnPlannedRetailers(int toDateCount) {
        return Observable.fromCallable(() -> {
            try {
                ArrayList<TaskRetailerBo> retailerList = new ArrayList<>();
                initDb();
                String maxDueDate = DateTimeUtils.getRequestedDateByGetType(toDateCount, Calendar.DATE);
                String query = "select distinct RM.retailerId,RM.retailerName,DWP.Date,RA.Address1,RA.Address2,RA.Address3" +
                        " from TaskConfigurationMaster A inner join TaskMaster B on A.taskid=B.taskid" +
                        " left join RetailerMaster RM on A.RetailerID=RM.retailerId" +
                        " left join RetailerAddress RA on A.RetailerID = RA.retailerID and RA.IsPrimary=1" +
                        " left join DatewisePlan DWP on DWP.Date = B.DueDate" +
                        " and DWP.EntityId = A.retailerID and DWP.Status!='D' and DWP.EntityType = 'RETAILER'" +
                        " where B.DueDate<=" + StringUtils.getStringQueryParam(maxDueDate) + " and DWP.Date IS NULL and (B.Status!='D' OR B.Status IS NULL)" +
                        " and A.retailerId!=0 and A.TaskId not in (Select taskid from TaskHistory where RetailerId = A.retailerId)";

                Cursor c = mDbUtil.selectSQL(query);
                if (c != null) {
                    while (c.moveToNext()) {
                        TaskRetailerBo retailerBo = new TaskRetailerBo();
                        retailerBo.setRetailerId(c.getString(0));
                        retailerBo.setRetailerName(c.getString(1));
                        retailerBo.setLastVisitDate(c.getString(2));
                        retailerBo.setRetAddress(c.getString(3) + "," + c.getString(4) + "," + c.getString(5));

                        if (c.getString(2) != null) {
                            int daysCount = DateTimeUtils.getDateCount(DateTimeUtils.now(DateTimeUtils.DATE_GLOBAL),
                                    retailerBo.getLastVisitDate(), "yyyy/MM/dd");
                            retailerBo.setNextVisitDaysCount(daysCount);
                        }

                        retailerList.add(retailerBo);
                    }
                    c.close();
                }
                shutDownDb();
                return retailerList;

            } catch (Exception e) {
                shutDownDb();
            }
            return new ArrayList<>();
        });
    }


    @Override
    public void tearDown() {
        shutDownDb();
    }
}
