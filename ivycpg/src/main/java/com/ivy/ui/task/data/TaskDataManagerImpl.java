package com.ivy.ui.task.data;

import android.database.Cursor;

import com.ivy.core.data.app.AppDataProvider;
import com.ivy.core.di.scope.DataBaseInfo;
import com.ivy.cpg.view.task.TaskDataBO;
import com.ivy.lib.existing.DBUtil;
import com.ivy.sd.png.bo.RetailerMasterBO;
import com.ivy.sd.png.commons.SDUtil;
import com.ivy.sd.png.util.Commons;
import com.ivy.sd.png.util.DataMembers;
import com.ivy.utils.AppUtils;

import java.util.ArrayList;
import java.util.Vector;
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
                    initDb();
                    ArrayList<TaskDataBO> taskDataBOS = new ArrayList<>();
                    String query = "select distinct A.taskid,B.taskcode,B.taskDesc,A.retailerId,A.upload,(CASE WHEN ifnull(TD.TaskId,0) >0 THEN 1 ELSE 0 END) as isDone,"
                            + "B.usercreated , B.taskowner , B.date, A.upload,channelid,A.userid  from TaskConfigurationMaster A inner join TaskMaster B on "
                            + "A.taskid=B.taskid left join TaskExecutionDetails TD on TD.TaskId=A.taskid  and  TD.RetailerId = " + retailerId + " where A.TaskId not in (Select taskid from TaskHistory where RetailerId =" + retailerId + ")";
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

    @Override
    public Single<Integer> fetchTaskCount() {
        return Single.fromCallable(new Callable<Integer>() {
            @Override
            public Integer call() throws Exception {
                try {
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

    @Override
    public Single<Boolean> updateTask(TaskDataBO taskDataBO, String retailerId) {
        return Single.fromCallable(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                try {
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
                        String UID = AppUtils.QT(appDataProvider.getRetailMaster().getRetailerID()
                                + SDUtil.now(SDUtil.DATE_TIME_ID_MILLIS));
                        String columns = "TaskId,RetailerId,Date,UId,Upload";
                        String values;

                        try {
                            if (taskDataBO.isChecked()) {
                                values = taskDataBO.getTaskId() + "," + AppUtils.QT(retailerId) + "," + AppUtils.QT(SDUtil.now(SDUtil.DATE_GLOBAL)) + "," + UID + ",'N'";
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
    public Single<Boolean> addNewTask(int channelId, String taskTitleDesc, String taskDetailDesc, String mode) {


        return Single.fromCallable(new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {

                // Remove single quotes
                String name = taskDetailDesc.replaceAll("'", " ");
                String title = taskTitleDesc.replaceAll("'", " ");

                // Generate Unique ID
                String id = AppUtils.QT(appDataProvider.getUser()
                        .getUserid() + SDUtil.now(SDUtil.DATE_TIME_ID));

                String date = AppUtils.QT(SDUtil.now(SDUtil.DATE_GLOBAL));
                Commons.print("date :: ," + date + "");

                String columns = "taskid,retailerid,usercreated,upload,date,uid,userid";
                String uID = AppUtils.QT(appDataProvider.getRetailMaster().getRetailerID()
                        + SDUtil.now(SDUtil.DATE_TIME_ID_MILLIS));
                String values;

                try {
                    // Insert Task into TaskMaster
                    String columns_new = "taskid,taskcode,taskdesc,upload ,taskowner,date,usercreated";

                    String value_new = id + "," + AppUtils.QT(title) + "," + AppUtils.QT(name) + ","
                            + "'N'," + AppUtils.QT("self") + ", " + date + ",1";

                    mDbUtil.insertSQL("TaskMaster", columns_new, value_new);


                    if (channelId == -1) {// for all channel
                        String[] chrid = getChannelRetailerId(0);
                        for (String aChrid : chrid) {

                            values = id + "," + aChrid + "," + "1" + "," + "'N'," + date + "," + uID + "," + "0";
                            mDbUtil.insertSQL(DataMembers.tbl_TaskConfigurationMaster,
                                    columns, values);
                        }

                    } else if (mode.equals("seller")) {

                        values = id + "," + 0 + "," + "1" + "," + "'N'," + date + "," + uID + "," + channelId;
                        mDbUtil.insertSQL(DataMembers.tbl_TaskConfigurationMaster, columns,
                                values);
                    } else if (mode.equals("retailer")) {
                        if (channelId == -2) {
                            String[] chrid = getRetailerIdlist();
                            for (String aChrid : chrid) {

                                values = id + "," + aChrid + "," + "1" + ","
                                        + "'N'," + date + "," + uID + "," + "0";
                                mDbUtil.insertSQL(DataMembers.tbl_TaskConfigurationMaster,
                                        columns, values);
                            }
                        } else {
                            values = id + "," + channelId + "," + "1" + "," + "'N'," + date + "," + uID + "," + "0";
                            mDbUtil.insertSQL(DataMembers.tbl_TaskConfigurationMaster,
                                    columns, values);
                        }
                    } else {

                        String[] chrid = getChannelRetailerId(channelId);
                        for (String aChrid : chrid) {
                            values = id + "," + aChrid + "," + "1" + "," + "'N'," + date + "," + uID + "," + "0";
                            mDbUtil.insertSQL(DataMembers.tbl_TaskConfigurationMaster,
                                    columns, values);
                        }
                    }

                    return true;
                } catch (Exception e) {
                    Commons.printException(e);
                }
                return false;
            }
        });
    }

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


    private String[] getChannelRetailerId(int channelId) {
        Vector<String> channelRId = new Vector<>();
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
        Vector<String> RId = new Vector<>();
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
