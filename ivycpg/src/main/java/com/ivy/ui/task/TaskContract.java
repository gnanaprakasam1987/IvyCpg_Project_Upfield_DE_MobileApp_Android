package com.ivy.ui.task;

import com.ivy.core.base.presenter.BaseIvyPresenter;
import com.ivy.core.base.view.BaseIvyView;
import com.ivy.core.di.scope.PerActivity;
import com.ivy.sd.png.bo.ReasonMaster;
import com.ivy.sd.png.bo.RetailerMasterBO;
import com.ivy.sd.png.bo.UserMasterBO;
import com.ivy.ui.task.model.TaskDataBO;
import com.ivy.ui.task.model.TaskRetailerBo;

import java.util.ArrayList;
import java.util.HashMap;

public interface TaskContract {

    interface TaskView extends BaseIvyView {

        void updateLabelNames(HashMap<String, String> labelMap);

        void updateListData(ArrayList<TaskDataBO> updatedList);


        void showImageUpdateMsg();

        void onDeleteSuccess();

        void showErrorMsg();
    }

    interface TaskListView extends TaskView {

        void showTaskUpdateMsg();

        void showTaskReasonUpdateMsg();
    }

    interface TaskCreationView extends TaskView {

        String getTaskMode();

        void setTaskRetailerListData(ArrayList<RetailerMasterBO> retailerList);

        void setParentUserListData(ArrayList<UserMasterBO> parentUserList);

        void setTaskCategoryListData(ArrayList<TaskDataBO> categoryList);

        void setChildUserListData(ArrayList<UserMasterBO> childUserList);

        void setPeerUserListData(ArrayList<UserMasterBO> peerUserList);

        void setLinkUserListData(HashMap<String, ArrayList<UserMasterBO>> linkUserListMap);

        void showTaskTitleError();

        void showTaskDescError();

        void showTaskSaveAlertMsg();

        void showTaskDueDateError();

        void updateImageListAdapter(ArrayList<TaskDataBO> imageList);

        void showSpinnerSelectionError();

        void showRetSelectionError();
    }

    interface TaskUnplannedView extends TaskView {

        void updateUnplannedTaskList(ArrayList<TaskRetailerBo> retailerMasterBOS, HashMap<String, ArrayList<TaskDataBO>> taskHashMapList);
    }


    @PerActivity
    interface TaskPresenter<V extends TaskView> extends BaseIvyPresenter<V> {

        void fetchTaskCreationConfig();

        void fetchLabels();

        void fetchData(int retailerId, String taskId);

        void fetchTaskImageList(String taskId);

        void fetchCompletedTask(String retailerID);

        void updateTaskList(int userCreatedTask, String retailerID, boolean isRetailerWise, boolean isSurveywise, boolean isDelegate);

        void addNewImage(String imageName);

        void onSaveButtonClick(int channelId, TaskDataBO taskObj, int linkUserId, int retailerId);

        void updateTaskExecution(String retailerID, TaskDataBO taskDataBO, int reasonId);

        void updateTaskExecutionImg(String imageName, String taskID, boolean isFrmDetailSrc);

        ArrayList<TaskDataBO> getTaskImgList();

        void deleteTask(String taskId, String taskOwner, int serverTask);

        void updateModuleTime();

        int getUserID();

        int getRetailerID();

        void orderBySortList(int sortType, boolean orderBy);

        boolean isShowServerTaskOnly();

        boolean isNewTask();

        boolean isNoTaskReason();

        boolean isShowProdLevel();

        String outDateFormat();

        boolean validate(String taskTitle, String taskView, String dueDate, int retSelectionId, int spinnerSelectionId);

        void saveModuleCompletion(String menuCode);

        boolean isNPPhotoReasonAvailable(String retailerID, String moduleName);

        void fetchUnPlannedTask();

        ArrayList<ReasonMaster> fetchNotCompletedTaskReasons();
    }
}
