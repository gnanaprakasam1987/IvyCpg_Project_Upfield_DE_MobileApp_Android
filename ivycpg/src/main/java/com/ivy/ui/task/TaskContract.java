package com.ivy.ui.task;

import android.support.annotation.StringRes;
import android.widget.ArrayAdapter;

import com.ivy.core.base.presenter.BaseIvyPresenter;
import com.ivy.core.base.view.BaseIvyView;
import com.ivy.core.di.scope.PerActivity;
import com.ivy.cpg.view.task.TaskDataBO;
import com.ivy.sd.png.bo.ChannelBO;
import com.ivy.sd.png.bo.RetailerMasterBO;
import com.ivy.sd.png.bo.UserMasterBO;

import java.util.ArrayList;
import java.util.Vector;

public interface TaskContract {

    interface TaskView extends BaseIvyView {

        void setTaskChannelListData(Vector<ChannelBO> channelList);

        void setTaskRetailerListData(ArrayList<RetailerMasterBO> retailerList);

        void setTaskUserListData(ArrayList<UserMasterBO> userList);

        String getTaskMode();

        void showUpdatedDialog(@StringRes int msgResId);

        void updateListData(ArrayList<TaskDataBO> updatedList);

        void setTaskCategoryListData(ArrayList<TaskDataBO> categoryList);

        void updateImageListAdapter(ArrayList<TaskDataBO> imageList);
    }


    @PerActivity
    interface TaskPresenter<V extends TaskView> extends BaseIvyPresenter<V> {

        void fetchData();

        void fetchTaskCategory(String menuCode);

        void fetchTaskImageList(String taskId);

        void updateTaskList(int taskType, String retailerID, boolean isRetailerwise, boolean isSurveywise);

        String[] getChannelIdsForSurvey();

        void addNewImage(String imageName);

        void onSaveButtonClick(int channelId,TaskDataBO taskObj);

        void updateTaskExecution(String retailerID, TaskDataBO taskDataBO);

        void updateTask(TaskDataBO taskObj);

        String getSelectedRetailerId();

        void updateModuleTime();

        ArrayList<ChannelBO> getTaskChannelList();

        ArrayList<RetailerMasterBO> getTaskRetailerList();

        ArrayList<UserMasterBO> getTaskUserList();

        ArrayList<TaskDataBO>getTaskList();

        int getUserID();

        int getRetailerID();

        boolean isShowServerTaskOnly();

        boolean isNewTask();

        boolean isMoveNextActivity();

        boolean isNoTaskReason();

        String outDateFormat();

        boolean isValidate(String taskTitle, String taskView);

        void saveModuleCompletion(String menuCode);

        boolean isNPPhotoReasonAvailable(String retailerID, String moduleName);

        ArrayList<TaskDataBO>getTaskImgList();

        void deleteTask(String taskId,String taskOwner);

    }
}
