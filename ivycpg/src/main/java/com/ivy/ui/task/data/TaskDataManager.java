package com.ivy.ui.task.data;

import com.ivy.core.data.AppDataManagerContract;
import com.ivy.cpg.view.task.TaskDataBO;
import com.ivy.sd.png.bo.RetailerMasterBO;

import java.util.ArrayList;

import io.reactivex.Observable;
import io.reactivex.Single;

public interface TaskDataManager extends AppDataManagerContract {

    Observable<ArrayList<TaskDataBO>> fetchTaskData(String retailerId);

    Observable<ArrayList<TaskDataBO>> fetchPendingTaskData();

    Single<Integer> fetchTaskCount();

    Single<Boolean> updateTask(TaskDataBO taskDataBO, String retailerId);

    Single<Boolean> addNewTask(int channelId, TaskDataBO taskObj, String mode, ArrayList<TaskDataBO> taskImgList);

    Observable<ArrayList<RetailerMasterBO>> fetchRetailers();

    Observable<ArrayList<TaskDataBO>> fetchTaskCategories(String menuCode);

    Observable<ArrayList<TaskDataBO>> fetTaskImgData(String taskId);

    Single<Boolean> deleteTaskData(String taskId);
}
