package com.ivy.ui.task.data;

import com.ivy.core.data.AppDataManagerContract;
import com.ivy.sd.png.bo.RetailerMasterBO;
import com.ivy.ui.task.model.FilterBo;
import com.ivy.ui.task.model.TaskDataBO;
import com.ivy.ui.task.model.TaskRetailerBo;

import java.util.ArrayList;
import java.util.HashMap;

import io.reactivex.Observable;
import io.reactivex.Single;

public interface TaskDataManager extends AppDataManagerContract {

    Observable<ArrayList<TaskDataBO>> fetchTaskData(int tabPos, String retailerId, int userCreatedTask, boolean isDelegate);

    Observable<ArrayList<TaskDataBO>> fetchCompletedTask(String retailerId);

    Observable<ArrayList<TaskDataBO>> fetchPendingTaskData();

    Single<Integer> fetchTaskCount();

    Single<Boolean> updateTaskExecutionData(TaskDataBO taskDataBO, String retailerId, int reasonId);

    Single<Boolean> updateTaskExecutionImage(String imageName, String taskId);

    Single<Boolean> saveTask(int channelId, TaskDataBO taskObj, ArrayList<TaskDataBO> taskImgList, int linkUserId);

    Observable<ArrayList<RetailerMasterBO>> fetchAllRetailers();

    Observable<ArrayList<TaskDataBO>> fetchTaskCategories(int prodLevelId);

    Observable<HashMap<String, ArrayList<FilterBo>>> fetchFilterData(int prodLevelId, boolean isFromHomeSrc);

    Observable<ArrayList<TaskDataBO>> fetchTaskImageData(String taskId, int userIdLength);

    Single<Boolean> deleteTaskData(String taskId, String taskOwner, int serverTask);

    Observable<ArrayList<String>> getDeletedImageList(String taskId);

    Observable<HashMap<String, ArrayList<TaskDataBO>>> fetchUnPlanedTaskData(int toDateCount);

    Observable<ArrayList<TaskRetailerBo>> fetchUnPlannedRetailers(int toDateCount);


}
