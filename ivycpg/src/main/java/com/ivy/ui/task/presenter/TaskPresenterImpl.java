package com.ivy.ui.task.presenter;

import android.arch.lifecycle.LifecycleObserver;

import com.ivy.core.IvyConstants;
import com.ivy.core.base.presenter.BasePresenter;
import com.ivy.core.data.channel.ChannelDataManager;
import com.ivy.core.data.datamanager.DataManager;
import com.ivy.core.data.label.LabelsDataManager;
import com.ivy.core.data.outlettime.OutletTimeStampDataManager;
import com.ivy.core.data.reason.ReasonDataManager;
import com.ivy.core.data.user.UserDataManager;
import com.ivy.core.di.scope.ChannelInfo;
import com.ivy.core.di.scope.LabelMasterInfo;
import com.ivy.core.di.scope.OutletTimeStampInfo;
import com.ivy.core.di.scope.ReasonInfo;
import com.ivy.core.di.scope.UserInfo;
import com.ivy.sd.png.bo.ReasonMaster;
import com.ivy.sd.png.bo.RetailerMasterBO;
import com.ivy.sd.png.bo.UserMasterBO;
import com.ivy.sd.png.commons.SDUtil;
import com.ivy.sd.png.provider.ConfigurationMasterHelper;
import com.ivy.ui.task.TaskConstant;
import com.ivy.ui.task.TaskContract;
import com.ivy.ui.task.data.TaskDataManager;
import com.ivy.ui.task.model.FilterBo;
import com.ivy.ui.task.model.TaskDataBO;
import com.ivy.ui.task.model.TaskRetailerBo;
import com.ivy.utils.DateTimeUtils;
import com.ivy.utils.FileUtils;
import com.ivy.utils.rx.SchedulerProvider;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import javax.inject.Inject;

import io.reactivex.Observable;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.BiFunction;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function6;
import io.reactivex.observers.DisposableObserver;

import static com.ivy.utils.DateTimeUtils.DATE_GLOBAL;

public class TaskPresenterImpl<V extends TaskContract.TaskView> extends BasePresenter<V> implements TaskContract.TaskPresenter<V>, LifecycleObserver {

    private DataManager mDataManager;
    private UserDataManager mUserDataManager;
    private ChannelDataManager mChannelDataManager;
    private ConfigurationMasterHelper mConfigurationMasterHelper;
    private TaskDataManager mTaskDataManager;
    private OutletTimeStampDataManager mOutletTimeStampDataManager;
    private ReasonDataManager mReasonDataManager;
    private LabelsDataManager mLabelsDataManager;
    private ArrayList<UserMasterBO> parentUserListBos;
    private ArrayList<UserMasterBO> childUserListBos;
    private ArrayList<UserMasterBO> peerUSerListBos;
    private HashMap<String, ArrayList<UserMasterBO>> linkUserListMap;
    private ArrayList<RetailerMasterBO> mRetailerListBos;
    private ArrayList<TaskDataBO> mTaskImgList;
    ArrayList<TaskDataBO> taskPreparedList;
    ArrayList<String> deletedImageList;
    ArrayList<TaskRetailerBo> taskRetailerListBo;
    HashMap<String, ArrayList<TaskDataBO>> taskListHashMap;
    HashMap<String, ArrayList<FilterBo>> filterhashMapList;
    ArrayList<ReasonMaster> taskNonCompleteReasonList;

    private int TASK_PRODUCT_LEVEL_NO;
    private boolean IS_SHOW_TASK_PRODUCT_LEVEL;
    private boolean IS_DISABLE_CALL_ANALYSIS_TIMER;
    private boolean IS_SHOW_ONLY_SERVER_TASK;
    private boolean IS_NEW_TASK;

    @Inject
    public TaskPresenterImpl(DataManager dataManager,
                             SchedulerProvider schedulerProvider,
                             CompositeDisposable compositeDisposable,
                             ConfigurationMasterHelper configurationMasterHelper,
                             V view,
                             @UserInfo UserDataManager userDataManager,
                             @ChannelInfo ChannelDataManager channelDataManager,
                             TaskDataManager taskDataManager,
                             @OutletTimeStampInfo OutletTimeStampDataManager mOutletTimeStampDataManager,
                             @ReasonInfo ReasonDataManager mReasonDataManager,
                             @LabelMasterInfo LabelsDataManager labelsDataManager) {
        super(dataManager, schedulerProvider, compositeDisposable, configurationMasterHelper, view);

        this.mDataManager = dataManager;
        this.mConfigurationMasterHelper = configurationMasterHelper;
        this.mUserDataManager = userDataManager;
        this.mChannelDataManager = channelDataManager;
        this.mTaskDataManager = taskDataManager;
        this.mOutletTimeStampDataManager = mOutletTimeStampDataManager;
        this.mReasonDataManager = mReasonDataManager;
        this.mLabelsDataManager = labelsDataManager;

    }


    @Override
    public void fetchTaskCreationConfig() {

    }

    @Override
    public void fetchLabels() {
        getCompositeDisposable().add(mLabelsDataManager.getLabels(TaskConstant.TASK_TITLE_LABEL,
                TaskConstant.TASK_DUE_DATE_LABEL, TaskConstant.TASK_CREATED_BY_LABEL,
                TaskConstant.TASK_APPLICABLE_FOR_LABEL, TaskConstant.TASK_PHOTO_CAPTURE_LABEL,
                TaskConstant.TASK_DESCRIPTION_LABEL, TaskConstant.TASK_EVIDENCE_LABEL).subscribeOn(getSchedulerProvider().io())
                .observeOn(getSchedulerProvider().ui())
                .subscribe(new Consumer<HashMap<String, String>>() {
                    @Override
                    public void accept(HashMap<String, String> labelHashMap) throws Exception {
                        if (!labelHashMap.isEmpty())
                            getIvyView().updateLabelNames(labelHashMap);
                    }
                }));
    }

    @Override
    public void fetchTaskCreationData(int retailerId, String taskId) {
        getIvyView().showLoading();
        parentUserListBos = new ArrayList<>();
        childUserListBos = new ArrayList<>();
        peerUSerListBos = new ArrayList<>();
        linkUserListMap = new HashMap<>();
        mRetailerListBos = new ArrayList<>();
        mTaskImgList = new ArrayList<>();
        getCompositeDisposable().add(Observable.zip(mUserDataManager.fetchParentUsers()
                , mUserDataManager.fetchChildUsers(), mUserDataManager.fetchPeerUsers(),
                mUserDataManager.fetchLinkUsers(retailerId),
                mTaskDataManager.fetchAllRetailers()
                , mTaskDataManager.fetchTaskImageData(taskId, getUserID()),
                new Function6<ArrayList<UserMasterBO>, ArrayList<UserMasterBO>, ArrayList<UserMasterBO>, HashMap<String, ArrayList<UserMasterBO>>, ArrayList<RetailerMasterBO>, ArrayList<TaskDataBO>, Object>() {
                    @Override
                    public Object apply(ArrayList<UserMasterBO> parentUserMasterBOs, ArrayList<UserMasterBO> childUserMasterBOs,
                                        ArrayList<UserMasterBO> peerUserMasterBOs, HashMap<String, ArrayList<UserMasterBO>> linkUserMasterListMap,
                                        ArrayList<RetailerMasterBO> retailerMasterBOs, ArrayList<TaskDataBO> taskImageBos) throws Exception {


                        parentUserListBos.clear();
                        parentUserListBos.addAll(parentUserMasterBOs);

                        childUserListBos.clear();
                        childUserListBos.addAll(childUserMasterBOs);

                        peerUSerListBos.clear();
                        peerUSerListBos.addAll(peerUserMasterBOs);

                        linkUserListMap.clear();
                        linkUserListMap.putAll(linkUserMasterListMap);

                        mRetailerListBos.clear();
                        mRetailerListBos.addAll(retailerMasterBOs);

                        mTaskImgList.clear();
                        mTaskImgList.addAll(taskImageBos);

                        return true;
                    }
                }).subscribeOn(getSchedulerProvider().io())
                .observeOn(getSchedulerProvider().ui())
                .subscribeWith(new DisposableObserver<Object>() {
                    @Override
                    public void onNext(Object o) {

                    }

                    @Override
                    public void onError(Throwable e) {
                        getIvyView().hideLoading();
                        getIvyView().showErrorMsg();
                    }

                    @Override
                    public void onComplete() {

                        ((TaskContract.TaskCreationView) getIvyView()).setParentUserListData(parentUserListBos);

                        ((TaskContract.TaskCreationView) getIvyView()).setChildUserListData(childUserListBos);

                        ((TaskContract.TaskCreationView) getIvyView()).setPeerUserListData(peerUSerListBos);

                        ((TaskContract.TaskCreationView) getIvyView()).setLinkUserListData(linkUserListMap);

                        ((TaskContract.TaskCreationView) getIvyView()).setTaskRetailerListData(mRetailerListBos);

                        getIvyView().updateListData(mTaskImgList);

                        getIvyView().hideLoading();
                    }
                }));


    }

    @Override
    public void fetchTaskCategory() {
        getIvyView().showLoading();
        getCompositeDisposable().add(mTaskDataManager.fetchTaskCategories(mConfigurationMasterHelper.TASK_PRODUCT_LEVEL_NO)
                .subscribeOn(getSchedulerProvider().io())
                .observeOn(getSchedulerProvider().ui())
                .subscribe(new Consumer<ArrayList<TaskDataBO>>() {
                    @Override
                    public void accept(ArrayList<TaskDataBO> productLevelBos) throws Exception {

                        if (!productLevelBos.isEmpty())
                            ((TaskContract.TaskCreationView) getIvyView()).setTaskCategoryListData(productLevelBos);
                        getIvyView().hideLoading();

                    }
                }));
    }

    @Override
    public void fetchTaskImageList(String taskId) {
        getIvyView().showLoading();
        ArrayList<TaskDataBO> mTaskImgList = new ArrayList<>();
        getCompositeDisposable().add(mTaskDataManager.fetchTaskImageData(taskId, getUserID())
                .subscribeOn(getSchedulerProvider().io())
                .observeOn(getSchedulerProvider().ui()).subscribe(new Consumer<ArrayList<TaskDataBO>>() {
                    @Override
                    public void accept(ArrayList<TaskDataBO> taskDataBOS) throws Exception {
                        if (!taskDataBOS.isEmpty())
                            mTaskImgList.addAll(taskDataBOS);

                        getIvyView().updateListData(mTaskImgList);
                        getIvyView().hideLoading();
                    }
                }));
    }

    @Override
    public void fetchCompletedTask(String retailerID) {
        taskPreparedList = new ArrayList<>();
        getIvyView().showLoading();
        getCompositeDisposable().add(mTaskDataManager.fetchCompletedTask(retailerID)
                .subscribeOn(getSchedulerProvider().io())
                .observeOn(getSchedulerProvider().ui())
                .subscribe(new Consumer<ArrayList<TaskDataBO>>() {
                    @Override
                    public void accept(ArrayList<TaskDataBO> taskDataBOS) {
                        if (!taskDataBOS.isEmpty()) {
                            taskPreparedList.clear();
                            taskPreparedList.addAll(taskDataBOS);
                            getIvyView().updateListData(taskPreparedList);
                            getIvyView().hideLoading();
                        } else {
                            getIvyView().hideLoading();
                            ((TaskContract.TaskListView) getIvyView()).showDataNotMappedMsg();
                        }

                    }
                }));

    }

    @Override
    public void fetchReasonFromStdListMasterByListCode() {
        taskNonCompleteReasonList = new ArrayList<>();
        getCompositeDisposable().add(mReasonDataManager.fetchReasonFromStdListMasterByListCode(TaskConstant.TASK_NOT_COMPLETE_REASON)
                .subscribeOn(getSchedulerProvider().io())
                .observeOn(getSchedulerProvider().ui())
                .subscribe(new Consumer<ArrayList<ReasonMaster>>() {
                    @Override
                    public void accept(ArrayList<ReasonMaster> reasonMasterArrayList) throws Exception {
                        taskNonCompleteReasonList.clear();
                        taskNonCompleteReasonList.addAll(reasonMasterArrayList);
                    }
                }));
    }

    @Override
    public void getTaskListData(int tapPos, int userCreatedTask, String retailerID, boolean isFromHomeSrc, boolean isSurveywise, boolean isDelegate) {
        taskPreparedList = new ArrayList<>();
        taskNonCompleteReasonList = new ArrayList<>();
        ArrayList<String> filterMenuList = new ArrayList<>();
        getIvyView().showLoading();
        getCompositeDisposable().add(Observable.zip(mTaskDataManager.fetchTaskData(tapPos, retailerID, userCreatedTask, isDelegate)
                , mReasonDataManager.fetchReasonFromStdListMasterByListCode(TaskConstant.TASK_NOT_COMPLETE_REASON)
                , new BiFunction<ArrayList<TaskDataBO>, ArrayList<ReasonMaster>, Boolean>() {
                    @Override
                    public Boolean apply(ArrayList<TaskDataBO> taskDataBOS, ArrayList<ReasonMaster> reasonMasterArrayList) throws Exception {

                        taskPreparedList.clear();
                        taskPreparedList.addAll(taskDataBOS);

                        taskNonCompleteReasonList.clear();
                        taskNonCompleteReasonList.addAll(reasonMasterArrayList);


                        return true;
                    }
                }).subscribeOn(getSchedulerProvider().io())
                .observeOn(getSchedulerProvider().ui())
                .subscribe(new Consumer<Boolean>() {
                    @Override
                    public void accept(Boolean aBoolean) {
                        if (!taskPreparedList.isEmpty()) {
                            getIvyView().updateListData(taskPreparedList);
                            getIvyView().hideLoading();
                        } else {
                            getIvyView().hideLoading();
                            ((TaskContract.TaskListView) getIvyView()).showDataNotMappedMsg();
                        }
                    }
                }));
    }

    @Override
    public void addNewImage(String imageName) {
        TaskDataBO imgBo = new TaskDataBO();
        if (imageName != null
                && !imageName.isEmpty()) {
            imgBo.setTaskImg(imageName);
            imgBo.setTaskImgPath(TaskConstant.TASK_SERVER_IMG_PATH);

            mTaskImgList.add(imgBo);
            FileUtils.copyFile(new File(FileUtils.photoFolderPath, imageName), TaskConstant.TASK_SERVER_IMG_PATH, imageName);
        } else {
            mTaskImgList = new ArrayList<>();
            imgBo.setTaskImgPath(TaskConstant.TASK_SERVER_IMG_PATH);
            imgBo.setTaskImg("");
            if (mTaskImgList.isEmpty())
                mTaskImgList.add(imgBo);
        }
        ((TaskContract.TaskCreationView) getIvyView()).updateImageListAdapter(mTaskImgList);
    }

    @Override
    public void onSaveTask(int channelId, TaskDataBO taskObj, int linkUserId, int retailerId) {
        getIvyView().showLoading();
        getCompositeDisposable().add(mTaskDataManager.saveTask(channelId
                , taskObj, getTaskImgList(), linkUserId).subscribeOn(getSchedulerProvider().io())
                .observeOn(getSchedulerProvider().ui())
                .subscribe(isAdded -> {
                    getIvyView().hideLoading();
                    if (isAdded) {
                        ((TaskContract.TaskCreationView) getIvyView()).showTaskSaveAlertMsg();
                    } else {
                        getIvyView().showErrorMsg();
                    }
                }));
    }

    @Override
    public void updateTaskExecution(TaskDataBO taskDataBO, int reasonId) {
        String retailerId = String.valueOf(taskDataBO.getRid());
        getCompositeDisposable().add(mTaskDataManager.updateTaskExecutionData(taskDataBO, retailerId, reasonId)
                .subscribeOn(getSchedulerProvider().io())
                .observeOn(getSchedulerProvider().ui())
                .subscribe(isUpdated -> {
                    if (isUpdated) {
                        if (!retailerId.equals("0"))// This method required for into the store task creation
                            saveModuleCompletion("MENU_TASK");

                        if (reasonId == 0)
                            ((TaskContract.TaskListView) getIvyView()).showTaskUpdateMsg();
                        else
                            ((TaskContract.TaskListView) getIvyView()).showTaskReasonUpdateMsg();
                    }
                }));
    }

    @Override
    public void updateTaskExecutionImg(String imageName, String taskID, boolean isFrmDetailSrc) {
        getCompositeDisposable().add(mTaskDataManager.updateTaskExecutionImage(imageName, taskID)
                .subscribeOn(getSchedulerProvider().io())
                .observeOn(getSchedulerProvider().ui())
                .subscribe(imgUpdated -> {
                    if (imgUpdated) {
                        FileUtils.copyFile(new File(FileUtils.photoFolderPath, imageName), TaskConstant.TASK_SERVER_IMG_PATH, imageName);
                        getIvyView().showImageUpdateMsg();
                    }
                }));
    }

    @Override
    public void updateFilterListData(HashMap<String, ArrayList<Object>> selectedIds, boolean isRetailerWise) {

        ArrayList<TaskDataBO> filteredList = new ArrayList<>();
        if (selectedIds != null
                && !selectedIds.isEmpty()) {
            for (Map.Entry<String, ArrayList<Object>> entry : selectedIds.entrySet()) {
                for (TaskDataBO taskBo : taskPreparedList) {
                    if (taskBo.getRid() != 0
                            && (entry.getValue().contains(taskBo.getRid())
                            || entry.getValue().contains(taskBo.getTaskCategoryID()))) {
                        if (!filteredList.contains(taskBo))
                            filteredList.add(taskBo);
                    } else if (taskBo.getRid() == 0) {
                        if (entry.getValue().contains(taskBo.getTaskCategoryID()))
                            filteredList.add(taskBo);
                    }
                }
            }
            getIvyView().updateListData(filteredList);
        } else {
            getIvyView().updateListData(taskPreparedList);
        }

    }


    @Override
    public void updateModuleTime() {
        String date = DateTimeUtils.now(DATE_GLOBAL) + " " + DateTimeUtils
                .now(DateTimeUtils.TIME);
        if (mConfigurationMasterHelper.IS_DISABLE_CALL_ANALYSIS_TIMER)
            date = IvyConstants.DEFAULT_TIME_CONSTANT;
        getCompositeDisposable().add(mOutletTimeStampDataManager.updateTimeStampModuleWise(date)
                .subscribeOn(getSchedulerProvider().io())
                .observeOn(getSchedulerProvider().ui())
                .subscribe(aBoolean -> {

                }));
    }

    @Override
    public int getUserID() {
        return mDataManager.getUser().getUserid();
    }

    @Override
    public int getRetailerID() {
        return mDataManager.getRetailMaster() == null ? 0 : SDUtil.convertToInt(mDataManager.getRetailMaster().getRetailerID());
    }

    @Override
    public RetailerMasterBO getRetailerMasterBo(String retailerId) {
        for (RetailerMasterBO retBo : mDataManager.getRetailerMasters()) {
            if (retBo.getRetailerID().equals(retailerId)) {
                mDataManager.setRetailerMaster(retBo);
                return retBo;
            }
        }
        return null;
    }


    @Override
    public void orderBySortList(ArrayList<TaskDataBO> taskList, int sortType, boolean orderBy) {
        if (orderBy) {
            Collections.sort(taskList, (fstr, sstr) -> {
                if (sortType == TaskConstant.TASK_TITLE_ASC)
                    return fstr.getTasktitle().compareToIgnoreCase(sstr.getTasktitle());
                else if (sortType == TaskConstant.PRODUCT_LEVEL_ASC)
                    return fstr.getTaskCategoryDsc().compareToIgnoreCase(sstr.getTaskCategoryDsc());
                else
                    return fstr.getTaskDueDate().compareToIgnoreCase(sstr.getTaskDueDate());
            });

        } else {
            Collections.sort(taskList, (fstr, sstr) -> {
                if (sortType == TaskConstant.TASK_TITLE_DESC)
                    return sstr.getTasktitle().compareToIgnoreCase(fstr.getTasktitle());
                else if (sortType == TaskConstant.PRODUCT_LEVEL_DESC)
                    return sstr.getTaskCategoryDsc().compareToIgnoreCase(fstr.getTaskCategoryDsc());
                else
                    return sstr.getTaskDueDate().compareToIgnoreCase(fstr.getTaskDueDate());
            });
        }
        getIvyView().updateListData(taskList);
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
    public boolean isNoTaskReason() {
        return mConfigurationMasterHelper.floating_np_reason_photo;
    }

    @Override
    public boolean isShowProdLevel() {
        return mConfigurationMasterHelper.IS_SHOW_TASK_PRODUCT_LEVEL;
    }

    @Override
    public String outDateFormat() {
        return ConfigurationMasterHelper.outDateFormat;
    }

    @Override
    public boolean validate(String taskTitle, String taskView, String dueDate, int retSelectionId, int spinnerSelectionId) {

        if (taskTitle.equals("")) {
            ((TaskContract.TaskCreationView) getIvyView()).showTaskTitleError();
            return false;
        } else if (dueDate == null) {
            ((TaskContract.TaskCreationView) getIvyView()).showTaskDueDateError();
            return false;
        } else if (retSelectionId == 0) {
            ((TaskContract.TaskCreationView) getIvyView()).showRetSelectionError();
        } else if (spinnerSelectionId == 0) {
            ((TaskContract.TaskCreationView) getIvyView()).showSpinnerSelectionError();
        } else if (taskView.equals("")) {
            ((TaskContract.TaskCreationView) getIvyView()).showTaskDescError();
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
                .subscribe(aBoolean -> isAvailable = aBoolean));
        return isAvailable;
    }

    @Override
    public void fetchUnPlannedTask() {
        getIvyView().showLoading();
        taskRetailerListBo = new ArrayList<>();
        taskListHashMap = new HashMap<>();
        ArrayList<String> filterMenuList = new ArrayList<>();
        getCompositeDisposable().add(Observable.zip(mTaskDataManager.fetchUnPlannedRetailers(mConfigurationMasterHelper.IS_TASK_DUDE_DATE_COUNT),
                mTaskDataManager.fetchUnPlanedTaskData(mConfigurationMasterHelper.IS_TASK_DUDE_DATE_COUNT)
                , new BiFunction<ArrayList<TaskRetailerBo>, HashMap<String, ArrayList<TaskDataBO>>, Boolean>() {
                    @Override
                    public Boolean apply(ArrayList<TaskRetailerBo> retailerMasterBOArrayList, HashMap<String, ArrayList<TaskDataBO>> taskRetailerList) throws Exception {

                        taskRetailerListBo.clear();
                        taskRetailerListBo.addAll(retailerMasterBOArrayList);

                        taskListHashMap.clear();
                        taskListHashMap.putAll(taskRetailerList);

                        return true;
                    }
                }).subscribeOn(getSchedulerProvider().io())
                .observeOn(getSchedulerProvider().ui())
                .subscribeWith(new DisposableObserver<Boolean>() {
                    @Override
                    public void onNext(Boolean isFlag) {

                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onComplete() {
                        ((TaskContract.TaskUnplannedView) getIvyView()).updateUnplannedTaskList(taskRetailerListBo, taskListHashMap);
                        getIvyView().hideLoading();
                    }


                }));
    }


    @Override
    public ArrayList<ReasonMaster> fetchNotCompletedTaskReasons() {
        if (!taskNonCompleteReasonList.isEmpty())
            return taskNonCompleteReasonList;
        else
            return new ArrayList<>();
    }

    @Override
    public void fetchFilterList(boolean isFromHomeSrc) {
        filterhashMapList = new HashMap<>();
        getCompositeDisposable()
                .add(mTaskDataManager
                        .fetchFilterData(mConfigurationMasterHelper.TASK_PRODUCT_LEVEL_NO, isFromHomeSrc)
                        .subscribeOn(getSchedulerProvider().io())
                        .observeOn(getSchedulerProvider().ui())
                        .subscribe(new Consumer<HashMap<String, ArrayList<FilterBo>>>() {
                            @Override
                            public void accept(HashMap<String, ArrayList<FilterBo>> filterList) throws Exception {
                                filterhashMapList.clear();
                                filterhashMapList.putAll(filterList);

                                ((TaskContract.TaskListView) getIvyView()).setUpFilterList(filterhashMapList);
                            }
                        }));
    }

    @Override
    public ArrayList<TaskDataBO> getTaskImgList() {
        return mTaskImgList;
    }

    @Override
    public void deleteTask(String taskId, String taskOwner, int serverTask) {
        getIvyView().showLoading();
        getDeletedImages(taskId);
        getCompositeDisposable().add(mTaskDataManager.deleteTaskData(taskId, taskOwner, serverTask).subscribeOn(getSchedulerProvider().io())
                .observeOn(getSchedulerProvider().ui())
                .subscribe(isDeleted -> {
                    if (isDeleted) {
                        for (String imageName : deletedImageList) {
                            FileUtils.deleteFiles(FileUtils.photoFolderPath, imageName);
                            FileUtils.deleteFiles(TaskConstant.TASK_SERVER_IMG_PATH, imageName);
                        }
                        getIvyView().hideLoading();
                        getIvyView().onDeleteSuccess();
                    } else {
                        getIvyView().hideLoading();
                        getIvyView().showErrorMsg();
                    }
                }));
    }


    private void getDeletedImages(String taskId) {
        deletedImageList = new ArrayList<>();
        getCompositeDisposable().add(mTaskDataManager.getDeletedImageList(taskId)
                .subscribeOn(getSchedulerProvider().io())
                .observeOn(getSchedulerProvider().ui())
                .subscribe(new Consumer<ArrayList<String>>() {
                    @Override
                    public void accept(ArrayList<String> imgList) throws Exception {
                        deletedImageList.clear();
                        deletedImageList.addAll(imgList);
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
        mLabelsDataManager.tearDown();
        super.onDetach();
    }


}
