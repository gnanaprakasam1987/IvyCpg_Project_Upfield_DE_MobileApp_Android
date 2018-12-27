package com.ivy.ui.task.data;

import android.database.Cursor;

import com.ivy.core.data.app.AppDataProvider;
import com.ivy.core.di.scope.DataBaseInfo;
import com.ivy.cpg.view.task.TaskDataBO;
import com.ivy.lib.existing.DBUtil;
import com.ivy.sd.png.util.Commons;

import java.util.ArrayList;
import java.util.concurrent.Callable;

import io.reactivex.Observable;
import io.reactivex.Single;

public class TaskDataManagerImpl implements TaskDataManager {

    private DBUtil mDbUtil;
    private AppDataProvider appDataProvider;

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


                }catch (Exception e){
                    Commons.printException(e);
                }
                return null;
            }
        });
    }

    @Override
    public Single<Boolean> addNewTask(int channelId, String taskTitleDesc, String taskDetailDesc) {
        return null;
    }

    @Override
    public void tearDown() {
        shutDownDb();
    }
}
