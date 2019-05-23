package com.ivy.ui.task;

import com.ivy.core.base.presenter.BaseIvyPresenter;
import com.ivy.core.base.view.BaseIvyView;
import com.ivy.core.di.scope.PerActivity;
import com.ivy.sd.png.bo.ChannelBO;
import com.ivy.sd.png.bo.RetailerMasterBO;
import com.ivy.sd.png.bo.UserMasterBO;
import com.ivy.ui.task.model.TaskDataBO;
import com.ivy.ui.task.model.TaskRetailerBo;

import java.util.ArrayList;
import java.util.HashMap;

public interface TaskContract {

    interface TaskView extends BaseIvyView {


        void updateListData(ArrayList<TaskDataBO> updatedList);

        void updateImageListAdapter(ArrayList<TaskDataBO> imageList);

        void showImageUpdateMsg();

        void onDeleteSuccess();

        void showErrorMsg();
    }

    interface TaskListView extends TaskView {

        void showTaskUpdateMsg();
    }

    interface TaskCreationView extends TaskView {

        String getTaskMode();

        void setTaskChannelListData(ArrayList<ChannelBO> channelList);

        void setTaskRetailerListData(ArrayList<RetailerMasterBO> retailerList);

        void setTaskUserListData(ArrayList<UserMasterBO> userList);

        void setTaskCategoryListData(ArrayList<TaskDataBO> categoryList);

        void showTaskTitleError();

        void showTaskDescError();

        void showTaskSaveAlertMsg();

        void showTaskDueDateError();
    }

    interface TaskUnplannedView extends TaskView {

        void updateUnplannedTaskList(ArrayList<TaskRetailerBo> retailerMasterBOS, HashMap<String, ArrayList<TaskDataBO>> taskHashMapList);
    }


    @PerActivity
    interface TaskPresenter<V extends TaskView> extends BaseIvyPresenter<V> {

        void fetchData();

        void fetchTaskCategory();

        void fetchTaskImageList(String taskId);

        void fetchCompletedTask(String retailerID);

        void updateTaskList(int userCreatedTask, String retailerID, boolean isRetailerWise, boolean isSurveywise);

        void addNewImage(String imageName);

        void onSaveButtonClick(int channelId, TaskDataBO taskObj);

        void updateTaskExecution(String retailerID, TaskDataBO taskDataBO);

        void updateTaskExecutionImg(String imageName, String taskID, boolean isFrmDetailSrc);

        ArrayList<TaskDataBO> getTaskImgList();

        void deleteTask(String taskId, String taskOwner, int serverTask);

        void updateModuleTime();

        int getUserID();

        int getRetailerID();

        void orderBySortList(int sortType, boolean orderBy);

        boolean isShowServerTaskOnly();

        boolean isNewTask();

        boolean isMoveNextActivity();

        boolean isNoTaskReason();

        boolean isShowProdLevel();

        String outDateFormat();

        boolean validate(String taskTitle, String taskView, String dueDate);

        void saveModuleCompletion(String menuCode);

        boolean isNPPhotoReasonAvailable(String retailerID, String moduleName);

        void fetchUnPlannedTask();
    }
}
