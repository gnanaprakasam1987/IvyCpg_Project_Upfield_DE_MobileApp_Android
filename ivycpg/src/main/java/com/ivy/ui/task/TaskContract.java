package com.ivy.ui.task;

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

        void showUpdatedDialog();

        void updateListData(ArrayList<TaskDataBO> updatedList);
    }


    @PerActivity
    interface TaskPresenter<V extends TaskView> extends BaseIvyPresenter<V> {

        void fetchData();

        void updateTaskList(int taskType,String retailerID,boolean isRetailerwise,boolean isSurveywise);

        String[] getChannelIdsForSurvey();

        void onSaveButtonClick(int channelId, String taskTitleDesc, String taskDetailDesc);

        void updateTask(String retailerID,TaskDataBO taskDataBO);

        String getSelectedRetailerId();

        void updateModuleTime();

        ArrayList<ChannelBO> getTaskChannelList();

        ArrayList<RetailerMasterBO> getTaskRetailerList();

        ArrayList<UserMasterBO> getTaskUserList();

        int getUserID();

        int getRetailerID();

        boolean isShowServerTaskOnly();

        boolean isNewTask();

        boolean isMoveNextActivity();

        boolean isNoTaskReason();

        String outDateFormat();

        boolean isValidate(String taskTitle, String taskView);

        void saveModuleCompletion(String menuCode);

        boolean isNPPhotoReasonAvailable(String retailerID,String moduleName);
    }
}
