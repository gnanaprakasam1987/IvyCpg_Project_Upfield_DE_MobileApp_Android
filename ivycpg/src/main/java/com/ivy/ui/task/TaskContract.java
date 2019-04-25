package com.ivy.ui.task;

import android.support.annotation.StringRes;

import com.ivy.core.base.presenter.BaseIvyPresenter;
import com.ivy.core.base.view.BaseIvyView;
import com.ivy.core.di.scope.PerActivity;
import com.ivy.cpg.view.task.TaskDataBO;
import com.ivy.sd.png.bo.ChannelBO;
import com.ivy.sd.png.bo.RetailerMasterBO;
import com.ivy.sd.png.bo.UserMasterBO;

import java.util.ArrayList;

public interface TaskContract {

    interface TaskView extends BaseIvyView {

        void showUpdatedDialog(@StringRes int msgResId);

        void updateListData(ArrayList<TaskDataBO> updatedList);

        void updateImageListAdapter(ArrayList<TaskDataBO> imageList);
    }

    interface  TaskDetailView extends TaskView{

        void updateImageView(String imageName);
    }

    interface TaskCreationView extends TaskView {

        String getTaskMode();

        void setTaskChannelListData(ArrayList<ChannelBO> channelList);

        void setTaskRetailerListData(ArrayList<RetailerMasterBO> retailerList);

        void setTaskUserListData(ArrayList<UserMasterBO> userList);

        void setTaskCategoryListData(ArrayList<TaskDataBO> categoryList);
    }


    @PerActivity
    interface TaskPresenter<V extends TaskView> extends BaseIvyPresenter<V> {

        void fetchData();

        void fetchTaskCategory(String menuCode);

        void fetchTaskImageList(String taskId);

        void fetchCompletedTask(String retailerID);

        void updateTaskList(int taskType, String retailerID, boolean isRetailerwise, boolean isSurveywise);

        String[] getChannelIdsForSurvey();

        void addNewImage(String imageName);

        void onSaveButtonClick(int channelId, TaskDataBO taskObj);

        void updateTaskExecution(String retailerID, TaskDataBO taskDataBO);

        void updateTaskExecutionImg(String imageName, String taskID,boolean isFrmDetailSrc);

        ArrayList<TaskDataBO> getTaskImgList();

        void deleteTask(String taskId, String taskOwner, int serverTask);

        String getSelectedRetailerId();

        void updateModuleTime();

        ArrayList<ChannelBO> getTaskChannelList();

        ArrayList<RetailerMasterBO> getTaskRetailerList();

        ArrayList<UserMasterBO> getTaskUserList();

        int getUserID();

        int getRetailerID();

        boolean isDeviceUser();

        void orderBySortList(int sortType, boolean orderBy);

        boolean isShowServerTaskOnly();

        boolean isNewTask();

        boolean isMoveNextActivity();

        boolean isNoTaskReason();

        boolean isShowProdLevel();


        String outDateFormat();

        boolean validate(String taskTitle, String taskView);

        void saveModuleCompletion(String menuCode);

        boolean isNPPhotoReasonAvailable(String retailerID, String moduleName);

        void createServerTaskImgPath(String serverPath);


    }
}
