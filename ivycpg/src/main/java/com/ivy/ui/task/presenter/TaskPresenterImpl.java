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
import com.ivy.sd.png.util.Commons;
import com.ivy.ui.task.TaskConstant;
import com.ivy.ui.task.TaskContract;
import com.ivy.ui.task.data.TaskDataManager;
import com.ivy.utils.DateTimeUtils;
import com.ivy.utils.FileUtils;
import com.ivy.utils.rx.SchedulerProvider;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Collections;

import javax.inject.Inject;

import io.reactivex.Observable;
import io.reactivex.disposables.CompositeDisposable;
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
    private ArrayList<ChannelBO> mChannelListBos = new ArrayList<>();
    private ArrayList<RetailerMasterBO> mRetailerListBos = new ArrayList<>();
    private ArrayList<TaskDataBO> mTaskImgList = new ArrayList<>();
    ArrayList<TaskDataBO> taskPreparedList = new ArrayList<>();

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
                , (Function3<ArrayList<UserMasterBO>, ArrayList<ChannelBO>, ArrayList<RetailerMasterBO>, Object>) (userMasterBOS, channelBOS, retailerMasterBOS) -> {
                    mUserListBos.clear();
                    for (UserMasterBO userBo : userMasterBOS) {
                        if (userBo.getUserid() == appDataProvider.getUser().getUserid()) {
                            userBo.setUserName("Self");
                            break;
                        }
                    }
                    mUserListBos.addAll(userMasterBOS);


                    mChannelListBos.clear();
                    mChannelListBos.addAll(channelBOS);

                    mRetailerListBos.clear();
                    mRetailerListBos.addAll(retailerMasterBOS);

                    return true;
                }).subscribeOn(getSchedulerProvider().io())
                .observeOn(getSchedulerProvider().ui())
                .subscribeWith(new DisposableObserver<Object>() {
                    @Override
                    public void onNext(Object o) {
                        ((TaskContract.TaskCreationView) getIvyView()).setTaskUserListData(mUserListBos);

                        ((TaskContract.TaskCreationView) getIvyView()).setTaskChannelListData(mChannelListBos);

                        ((TaskContract.TaskCreationView) getIvyView()).setTaskRetailerListData(mRetailerListBos);

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
                            ((TaskContract.TaskCreationView) getIvyView()).setTaskCategoryListData(taskDataBOS);

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
        ArrayList<TaskDataBO> mTaskImgList = new ArrayList<>();
        getCompositeDisposable().add(mTaskDataManager.fetTaskImgData(taskId)
                .subscribeOn(getSchedulerProvider().io())
                .observeOn(getSchedulerProvider().ui()).subscribeWith(new DisposableObserver<ArrayList<TaskDataBO>>() {
                    @Override
                    public void onNext(ArrayList<TaskDataBO> taskDataBOS) {
                        if (!taskDataBOS.isEmpty())
                            mTaskImgList.addAll(taskDataBOS);
                    }

                    @Override
                    public void onError(Throwable e) {
                        getIvyView().onError("Something went wrong");
                        getIvyView().hideLoading();
                    }

                    @Override
                    public void onComplete() {
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
                .subscribeWith(new DisposableObserver<ArrayList<TaskDataBO>>() {
                    @Override
                    public void onNext(ArrayList<TaskDataBO> taskDataBOS) {
                        taskPreparedList.clear();
                        taskPreparedList.addAll(taskDataBOS);
                    }

                    @Override
                    public void onError(Throwable e) {
                        getIvyView().onError("Something went wrong");
                        getIvyView().hideLoading();
                    }

                    @Override
                    public void onComplete() {
                        getIvyView().updateListData(taskPreparedList);
                        getIvyView().hideLoading();
                    }
                }));

    }

    @Override
    public void updateTaskList(int taskType, String retailerID, boolean isRetailerwise, boolean isSurveywise) {
        taskPreparedList = new ArrayList<>();
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
                                        if (dataBO.getUsercreated()
                                                .equals("0")) {
                                            taskPreparedList.add(dataBO);
                                        }
                                    } else if (taskType == 2) { // user
                                        if (dataBO.getUsercreated()
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
                                                if (dataBO.getUsercreated()
                                                        .equals("0")) {
                                                    taskPreparedList.add(dataBO);
                                                }
                                            } else if (taskType == 2) { // user
                                                if (dataBO.getUsercreated()
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
            imgBo.setTaskImgPath(TaskConstant.TASK_SERVER_IMG_PATH);

            mTaskImgList.add(imgBo);
            try {
                writeToFile(imageName);
            } catch (IOException e) {
                e.printStackTrace();
            }


        } else {
            imgBo.setTaskImgPath(TaskConstant.TASK_SERVER_IMG_PATH);
            imgBo.setTaskImg("");
            if (mTaskImgList.isEmpty())
                mTaskImgList.add(imgBo);

        }
        getIvyView().updateImageListAdapter(getTaskImgList());
    }


    public void writeToFile(String filename) throws IOException {
        String path = FileUtils.photoFolderPath;

        File folder = new File(path);
        if (!folder.exists()) {
            folder.mkdirs();
        }

        File newFile = new File(path, filename);
        if (!newFile.exists())
            newFile.createNewFile();
        String destpath = TaskConstant.TASK_SERVER_IMG_PATH;
        copyFile(newFile, destpath, filename);
    }


    private void copyFile(File sourceFile, String path, String filename) {

        File folder = new File(path);
        if (!folder.exists()) {
            folder.mkdirs();
        }
        File destFile = new File(path, filename);
        FileChannel source = null;
        FileChannel destination = null;
        try {

            source = new FileInputStream(sourceFile).getChannel();
            destination = new FileOutputStream(destFile).getChannel();
            destination.transferFrom(source, 0, source.size());
            source.close();
            destination.close();
        } catch (FileNotFoundException e) {
            Commons.printException(e.getMessage());
        } catch (IOException e) {
            Commons.printException(e.getMessage());
        }
    }


    @Override
    public void onSaveButtonClick(int channelId, TaskDataBO taskObj) {
        getIvyView().showLoading();
        getCompositeDisposable().add(mTaskDataManager.addAndUpdateTask(channelId
                , taskObj, ((TaskContract.TaskCreationView) getIvyView()).getTaskMode(), getTaskImgList()).subscribeOn(getSchedulerProvider().io())
                .observeOn(getSchedulerProvider().ui())
                .subscribe(isAdded -> {
                    getIvyView().hideLoading();
                    if (isAdded) {
                        getIvyView().showUpdatedDialog(R.string.saved_successfully);
                    }
                }));
    }

    @Override
    public void updateTaskExecution(String retailerID, TaskDataBO taskDataBO) {

        getCompositeDisposable().add(mTaskDataManager.updateTaskExecutionData(taskDataBO, retailerID)
                .subscribeOn(getSchedulerProvider().io())
                .observeOn(getSchedulerProvider().ui())
                .subscribe(isUpdated -> {
                    if (isUpdated) {
                        saveModuleCompletion("MENU_TASK");
                        getIvyView().showUpdatedDialog(R.string.task_updated_successfully);
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
                        writeToFile(imageName);
                        getIvyView().showMessage(R.string.image_saved);

                        if (isFrmDetailSrc)
                            ((TaskContract.TaskDetailView) getIvyView()).updateImageView(imageName);
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
                .subscribe(aBoolean -> {

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
    public int getUserID() {
        return appDataProvider.getUser().getUserid();
    }

    @Override
    public int getRetailerID() {
        return appDataProvider.getRetailMaster().getRetailerID() == null ? 0 : SDUtil.convertToInt(appDataProvider.getRetailMaster().getRetailerID());
    }

    @Override
    public boolean isDeviceUser() {
        return true;
    }

    @Override
    public void orderBySortList(int sortType, boolean orderBy) {
        if (orderBy) {
            Collections.sort(taskPreparedList, (fstr, sstr) -> {
                if (sortType == TaskConstant.TASK_TITLE_ASC)
                    return fstr.getTasktitle().compareToIgnoreCase(sstr.getTasktitle());
                else if (sortType == TaskConstant.PRODUCT_LEVEL_ASC)
                    return fstr.getTaskCategoryDsc().compareToIgnoreCase(sstr.getTaskCategoryDsc());
                else
                    return fstr.getTaskDueDate().compareToIgnoreCase(sstr.getTaskDueDate());
            });

        } else {
            Collections.sort(taskPreparedList, (fstr, sstr) -> {
                if (sortType == TaskConstant.TASK_TITLE_DESC)
                    return sstr.getTasktitle().compareToIgnoreCase(fstr.getTasktitle());
                else if (sortType == TaskConstant.PRODUCT_LEVEL_DESC)
                    return sstr.getTaskCategoryDsc().compareToIgnoreCase(fstr.getTaskCategoryDsc());
                else
                    return sstr.getTaskDueDate().compareToIgnoreCase(fstr.getTaskDueDate());
            });
        }
        getIvyView().updateListData(taskPreparedList);
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
    public boolean isShowProdLevel() {
        return mConfigurationMasterHelper.IS_SHOW_TASK_PRODUCT_LEVEL;
    }

    @Override
    public String outDateFormat() {
        return ConfigurationMasterHelper.outDateFormat;
    }

    @Override
    public boolean validate(String taskTitle, String taskView) {
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
                .subscribe(aBoolean -> isAvailable = aBoolean));
        return isAvailable;
    }

    @Override
    public void createServerTaskImgPath(String serverPath) {
        TaskConstant.TASK_SERVER_IMG_PATH = serverPath;
    }

    @Override
    public ArrayList<TaskDataBO> getTaskImgList() {
        return mTaskImgList;
    }

    @Override
    public void deleteTask(String taskId, String taskOwner, int serverTask) {
        getIvyView().showLoading();
        getCompositeDisposable().add(mTaskDataManager.deleteTaskData(taskId, taskOwner, serverTask).subscribeOn(getSchedulerProvider().io())
                .observeOn(getSchedulerProvider().ui())
                .subscribe(isDeleted -> {
                    getIvyView().hideLoading();
                    if (isDeleted) {
                        deleteTaskImages(taskId);
                    } else {
                        getIvyView().onError("Something went wrong");
                    }
                }));
    }


    public void deleteTaskImages(String taskId) {
        getIvyView().showLoading();
        getCompositeDisposable().add(mTaskDataManager.getDeletedImageList(taskId)
                .subscribeOn(getSchedulerProvider().io())
                .observeOn(getSchedulerProvider().ui())
                .subscribeWith(new DisposableObserver<ArrayList<String>>() {
                    @Override
                    public void onNext(ArrayList<String> imgList) {

                        for (String imageName : imgList)
                            FileUtils.deleteFiles(FileUtils.photoFolderPath, imageName);
                    }

                    @Override
                    public void onError(Throwable e) {
                        getIvyView().onError("Something went wrong");
                        getIvyView().hideLoading();
                    }

                    @Override
                    public void onComplete() {
                        getIvyView().hideLoading();
                        getIvyView().showUpdatedDialog(R.string.deleted_sucessfully);
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
