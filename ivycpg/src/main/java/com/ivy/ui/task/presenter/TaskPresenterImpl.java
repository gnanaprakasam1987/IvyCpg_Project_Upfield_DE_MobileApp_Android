package com.ivy.ui.task.presenter;

import android.arch.lifecycle.LifecycleObserver;

import com.ivy.core.base.presenter.BasePresenter;
import com.ivy.core.data.app.AppDataProvider;
import com.ivy.core.data.channel.ChannelDataManager;
import com.ivy.core.data.datamanager.DataManager;
import com.ivy.core.data.outlettime.OutletTimeStampDataManager;
import com.ivy.core.data.reason.ReasonDataManager;
import com.ivy.core.data.user.UserDataManager;
import com.ivy.core.di.scope.ChannelInfo;
import com.ivy.core.di.scope.OutletTimeStampInfo;
import com.ivy.core.di.scope.ReasonInfo;
import com.ivy.core.di.scope.UserInfo;
import com.ivy.cpg.view.survey.SurveyHelperNew;
import com.ivy.cpg.view.task.TaskDataBO;
import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.bo.ChannelBO;
import com.ivy.sd.png.bo.RetailerMasterBO;
import com.ivy.sd.png.bo.UserMasterBO;
import com.ivy.sd.png.commons.SDUtil;
import com.ivy.sd.png.provider.ConfigurationMasterHelper;
import com.ivy.ui.task.TaskContract;
import com.ivy.ui.task.data.TaskDataManager;
import com.ivy.utils.DateTimeUtils;
import com.ivy.utils.rx.SchedulerProvider;

import java.util.ArrayList;
import java.util.Vector;

import javax.inject.Inject;

import io.reactivex.Observable;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function3;
import io.reactivex.observers.DisposableObserver;

public class TaskPresenterImpl<V extends TaskContract.TaskView> extends BasePresenter<V> implements TaskContract.TaskPresenter<V>, LifecycleObserver {

    private UserDataManager mUserDataManager;
    private ChannelDataManager mChannelDataManager;
    private ConfigurationMasterHelper mConfigurationMasterHelper;
    private TaskDataManager mTaskDataManager;
    private AppDataProvider appDataProvider;
    private OutletTimeStampDataManager mOutletTimeStampDataManager;
    private SurveyHelperNew mSurveyHelperNew;
    private ReasonDataManager mReasonDataManager;
    private ArrayList<UserMasterBO> mUserListBos = new ArrayList<>();
    private Vector<ChannelBO> mChannelListBos = new Vector<>();
    private ArrayList<RetailerMasterBO> mRetailerListBos = new ArrayList<>();
    private ArrayList<TaskDataBO> mTaskImgList = new ArrayList<>();
    ArrayList<TaskDataBO> taskList = new ArrayList<>();

    @Inject
    public TaskPresenterImpl(DataManager dataManager,
                             SchedulerProvider schedulerProvider,
                             CompositeDisposable compositeDisposable,
                             ConfigurationMasterHelper configurationMasterHelper,
                             V view,
                             @UserInfo UserDataManager userDataManager,
                             @ChannelInfo ChannelDataManager channelDataManager,
                             TaskDataManager taskDataManager,
                             AppDataProvider appDataProvider,
                             @OutletTimeStampInfo OutletTimeStampDataManager mOutletTimeStampDataManager,
                             SurveyHelperNew mSurveyHelperNew,
                             @ReasonInfo ReasonDataManager mReasonDataManager) {
        super(dataManager, schedulerProvider, compositeDisposable, configurationMasterHelper, view);
        this.mConfigurationMasterHelper = configurationMasterHelper;
        this.mUserDataManager = userDataManager;
        this.mChannelDataManager = channelDataManager;
        this.mTaskDataManager = taskDataManager;
        this.appDataProvider = appDataProvider;
        this.mOutletTimeStampDataManager = mOutletTimeStampDataManager;
        this.mSurveyHelperNew = mSurveyHelperNew;
        this.mReasonDataManager = mReasonDataManager;

        /*if (view instanceof LifecycleOwner) {
            ((LifecycleOwner) view).getLifecycle().addObserver(this);
        }*/

    }


    @Override
    // @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
    public void fetchData() {
        getIvyView().showLoading();

        getCompositeDisposable().add(Observable.zip(mUserDataManager.fetchAllUsers(),
                mChannelDataManager.fetchChannels(), mTaskDataManager.fetchRetailers()
                , new Function3<ArrayList<UserMasterBO>, Vector<ChannelBO>, ArrayList<RetailerMasterBO>, Object>() {
                    @Override
                    public Boolean apply(ArrayList<UserMasterBO> userMasterBOS, Vector<ChannelBO> channelBOS, ArrayList<RetailerMasterBO> retailerMasterBOS) throws Exception {
                        mUserListBos.clear();

                        for (UserMasterBO userBo : userMasterBOS) {
                            if (userBo.getUserid() == appDataProvider.getUser().getUserid())
                                userBo.setUserName("Self");
                        }

                        mUserListBos.addAll(userMasterBOS);

                        mChannelListBos.clear();
                        mChannelListBos.addAll(channelBOS);

                        mRetailerListBos.clear();
                        mRetailerListBos.addAll(retailerMasterBOS);

                        return true;
                    }
                }).subscribeOn(getSchedulerProvider().io())
                .observeOn(getSchedulerProvider().ui())
                .subscribeWith(new DisposableObserver<Object>() {
                    @Override
                    public void onNext(Object o) {
                        getIvyView().setTaskUserListData(mUserListBos);

                        getIvyView().setTaskChannelListData(mChannelListBos);

                        getIvyView().setTaskRetailerListData(mRetailerListBos);

                        getIvyView().hideLoading();
                    }

                    @Override
                    public void onError(Throwable e) {
                        getIvyView().onError("Something went wrong");
                        getIvyView().hideLoading();
                    }

                    @Override
                    public void onComplete() {

                    }
                }));
    }

    @Override
    public void fetchTaskCategory(String menuCode) {
        getIvyView().showLoading();
        getCompositeDisposable().add(mTaskDataManager.fetchTaskCategories(menuCode)
                .subscribeOn(getSchedulerProvider().io())
                .observeOn(getSchedulerProvider().ui()).subscribeWith(new DisposableObserver<ArrayList<TaskDataBO>>() {

                    @Override
                    public void onNext(ArrayList<TaskDataBO> taskDataBOS) {
                        if (!taskDataBOS.isEmpty())
                            getIvyView().setTaskCategoryListData(taskDataBOS);

                        getIvyView().hideLoading();
                    }

                    @Override
                    public void onError(Throwable e) {
                        getIvyView().onError("Something went wrong");
                        getIvyView().hideLoading();
                    }

                    @Override
                    public void onComplete() {

                    }
                }));
    }

    @Override
    public void fetchTaskImageList(String taskId) {
        getIvyView().showLoading();
        ArrayList<TaskDataBO> taskImgList = new ArrayList<>();
        getCompositeDisposable().add(mTaskDataManager.fetTaskImgData(taskId)
                .subscribeOn(getSchedulerProvider().io())
                .observeOn(getSchedulerProvider().ui()).subscribeWith(new DisposableObserver<ArrayList<TaskDataBO>>() {
                    @Override
                    public void onNext(ArrayList<TaskDataBO> taskDataBOS) {
                        if (!taskDataBOS.isEmpty())
                            taskImgList.addAll(taskDataBOS);
                    }

                    @Override
                    public void onError(Throwable e) {
                        getIvyView().onError("Something went wrong");
                        getIvyView().hideLoading();
                    }

                    @Override
                    public void onComplete() {
                        getIvyView().updateListData(taskImgList);
                        getIvyView().hideLoading();
                    }
                }));
    }

    @Override
    public void updateTaskList(int taskType, String retailerID, boolean isRetailerwise, boolean isSurveywise) {
        ArrayList taskPreparedList = new ArrayList<>();
        getIvyView().showLoading();
        String[] channelIds = getChannelIdsForSurvey();
        getCompositeDisposable().add(mTaskDataManager.fetchTaskData(retailerID)
                .subscribeOn(getSchedulerProvider().io())
                .observeOn(getSchedulerProvider().ui()).subscribeWith(new DisposableObserver<ArrayList<TaskDataBO>>() {
                    @Override
                    public void onNext(ArrayList<TaskDataBO> taskDataBOS) {
                        for (TaskDataBO dataBO : taskDataBOS) {

                            if (isRetailerwise) {
                                if (String.valueOf(dataBO.getRid()).equals(retailerID)) {
                                    if (taskType == 1) { // server
                                        if (dataBO.getUsercreated().toUpperCase()
                                                .equals("0")) {
                                            taskPreparedList.add(dataBO);
                                        }
                                    } else if (taskType == 2) { // user
                                        if (dataBO.getUsercreated().toUpperCase()
                                                .equals("1")) {
                                            taskPreparedList.add(dataBO);
                                        }
                                    } else {
                                        taskPreparedList.add(dataBO);
                                    }
                                } else if (isSurveywise) {
                                    for (String chId : channelIds) {
                                        if (chId.equals(String.valueOf(dataBO.getChannelId()))) {
                                            if (taskType == 1) { // server
                                                if (dataBO.getUsercreated().toUpperCase()
                                                        .equals("0")) {
                                                    taskPreparedList.add(dataBO);
                                                }
                                            } else if (taskType == 2) { // user
                                                if (dataBO.getUsercreated().toUpperCase()
                                                        .equals("1")) {
                                                    taskPreparedList.add(dataBO);
                                                }
                                            } else {
                                                taskPreparedList.add(dataBO);
                                            }
                                        }
                                    }
                                }
                            } else {
                                if (dataBO.getRid() == 0
                                        && dataBO.getChannelId() == 0
                                        && (dataBO.getUserId() == getUserID()
                                        || dataBO.getUserId() == 0)) {

                                    if (taskType == 1) { // server
                                        if (dataBO.getUsercreated().toUpperCase()
                                                .equals("0")) {
                                            taskPreparedList.add(dataBO);
                                        }
                                    } else if (taskType == 2) { // user
                                        if (dataBO.getUsercreated().toUpperCase()
                                                .equals("1")) {
                                            taskPreparedList.add(dataBO);
                                        }

                                    } else {
                                        taskPreparedList.add(dataBO);
                                    }
                                }
                            }
                        }

                    }

                    @Override
                    public void onError(Throwable e) {
                        getIvyView().onError("Something went wrong");
                        getIvyView().hideLoading();
                    }

                    @Override
                    public void onComplete() {
                        if (!taskList.isEmpty())
                            taskList.clear();

                        taskList.addAll(taskPreparedList);
                        getIvyView().updateListData(taskPreparedList);
                        getIvyView().hideLoading();
                    }
                }));

    }


    @Override
    public String[] getChannelIdsForSurvey() {
        String channelIds = mSurveyHelperNew.getChannelidForSurvey();
        if (channelIds != null && channelIds.length() > 0)
            return channelIds.split(",");

        return new String[]{""};
    }

    @Override
    public void addNewImage(String imageName) {
        TaskDataBO imgBo = new TaskDataBO();
        if (imageName != null
                && !imageName.isEmpty()) {
            imgBo.setTaskImg(imageName);

            mTaskImgList.add(imgBo);


        } else {
            imgBo.setTaskImg("");
            if (mTaskImgList.isEmpty())
                mTaskImgList.add(imgBo);
        }
        getIvyView().updateImageListAdapter(getTaskImgList());
    }


    @Override
    public void onSaveButtonClick(int channelId, TaskDataBO taskObj) {
        getIvyView().showLoading();
        getCompositeDisposable().add(mTaskDataManager.addAndUpdateTask(channelId
                , taskObj, getIvyView().getTaskMode(), getTaskImgList()).subscribeOn(getSchedulerProvider().io())
                .observeOn(getSchedulerProvider().ui())
                .subscribe(new Consumer<Boolean>() {
                    @Override
                    public void accept(Boolean isAdded) throws Exception {
                        if (isAdded) {
                            getIvyView().showUpdatedDialog(R.string.saved_successfully);
                        }
                        getIvyView().hideLoading();
                    }
                }));
    }

    @Override
    public void updateTaskExecution(String retailerID, TaskDataBO taskDataBO) {

        getCompositeDisposable().add(mTaskDataManager.updateTaskExecutionData(taskDataBO, retailerID)
                .subscribeOn(getSchedulerProvider().io())
                .observeOn(getSchedulerProvider().ui())
                .subscribe(new Consumer<Boolean>() {
                    @Override
                    public void accept(Boolean isUpdated) throws Exception {
                        if (isUpdated) {
                            saveModuleCompletion("MENU_TASK");
                            getIvyView().showUpdatedDialog(R.string.task_updated_successfully);
                        }
                    }
                }));
    }


    @Override
    public String getSelectedRetailerId() {
        return null;
    }

    @Override
    public void updateModuleTime() {
        getCompositeDisposable().add(mOutletTimeStampDataManager.updateTimeStampModuleWise(DateTimeUtils
                .now(DateTimeUtils.TIME)).subscribeOn(getSchedulerProvider().io())
                .observeOn(getSchedulerProvider().ui())
                .subscribe(new Consumer<Boolean>() {
                    @Override
                    public void accept(Boolean aBoolean) {

                    }
                }));
    }

    @Override
    public ArrayList<ChannelBO> getTaskChannelList() {
        return null;
    }

    @Override
    public ArrayList<RetailerMasterBO> getTaskRetailerList() {
        return null;
    }

    @Override
    public ArrayList<UserMasterBO> getTaskUserList() {
        return null;
    }

    @Override
    public ArrayList<TaskDataBO> getTaskList() {
        return taskList;
    }

    @Override
    public int getUserID() {
        return appDataProvider.getUser().getUserid();
    }

    @Override
    public int getRetailerID() {
        return SDUtil.convertToInt(appDataProvider.getRetailMaster().getRetailerID());
    }

    @Override
    public boolean isShowServerTaskOnly() {
        return mConfigurationMasterHelper.IS_SHOW_ONLY_SERVER_TASK;
    }

    @Override
    public boolean isNewTask() {
        return mConfigurationMasterHelper.IS_NEW_TASK;
    }

    @Override
    public boolean isMoveNextActivity() {
        return mConfigurationMasterHelper.MOVE_NEXT_ACTIVITY;
    }

    @Override
    public boolean isNoTaskReason() {
        return mConfigurationMasterHelper.floating_np_reason_photo;
    }

    @Override
    public String outDateFormat() {
        return mConfigurationMasterHelper.outDateFormat;
    }

    @Override
    public boolean isValidate(String taskTitle, String taskView) {
        if (taskTitle.equals("")) {
            getIvyView().showMessage(R.string.enter_task_title);
            return false;
        } else if (taskView.equals("")) {
            getIvyView().showMessage(R.string.enter_task_description);
            return false;
        }
        return true;
    }

    @Override
    public void saveModuleCompletion(String menuCode) {
        getCompositeDisposable().add(getDataManager().saveModuleCompletion(menuCode)
                .subscribeOn(getSchedulerProvider().io())
                .observeOn(getSchedulerProvider().ui())
                .subscribe(value -> {
                }));
    }

    private boolean isAvailable = false;

    @Override
    public boolean isNPPhotoReasonAvailable(String retailerID, String moduleName) {
        getCompositeDisposable().add(mReasonDataManager.isNpReasonPhotoAvailable(retailerID, moduleName)
                .subscribeOn(getSchedulerProvider().io())
                .observeOn(getSchedulerProvider().ui())
                .subscribe(new Consumer<Boolean>() {
                    @Override
                    public void accept(Boolean aBoolean) throws Exception {
                        isAvailable = aBoolean;
                    }
                }));
        return isAvailable;
    }

    @Override
    public ArrayList<TaskDataBO> getTaskImgList() {
        return mTaskImgList;
    }

    @Override
    public void deleteTask(String taskId, String taskOwner) {
        getIvyView().showLoading();
        getCompositeDisposable().add(mTaskDataManager.deleteTaskData(taskId, taskOwner).subscribeOn(getSchedulerProvider().io())
                .observeOn(getSchedulerProvider().ui())
                .subscribe(new Consumer<Boolean>() {
                    @Override
                    public void accept(Boolean isDeleted) throws Exception {
                        if (isDeleted) {
                            getIvyView().showUpdatedDialog(R.string.deleted_sucessfully);
                        }
                        getIvyView().hideLoading();
                    }
                }));
    }

    @Override
    public void onDetach() {
        mUserDataManager.tearDown();
        mChannelDataManager.tearDown();
        mTaskDataManager.tearDown();
        mOutletTimeStampDataManager.tearDown();
        mReasonDataManager.tearDown();
        super.onDetach();
    }


}
