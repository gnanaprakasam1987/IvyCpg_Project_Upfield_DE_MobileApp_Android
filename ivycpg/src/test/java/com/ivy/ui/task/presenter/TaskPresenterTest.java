package com.ivy.ui.task.presenter;

import com.ivy.core.IvyConstants;
import com.ivy.core.data.app.AppDataProvider;
import com.ivy.core.data.channel.ChannelDataManager;
import com.ivy.core.data.datamanager.DataManager;
import com.ivy.core.data.label.LabelsDataManager;
import com.ivy.core.data.outlettime.OutletTimeStampDataManager;
import com.ivy.core.data.reason.ReasonDataManager;
import com.ivy.core.data.user.UserDataManager;
import com.ivy.sd.png.bo.ReasonMaster;
import com.ivy.sd.png.bo.RetailerMasterBO;
import com.ivy.sd.png.bo.UserMasterBO;
import com.ivy.sd.png.provider.ConfigurationMasterHelper;
import com.ivy.sd.png.util.DataMembers;
import com.ivy.sd.png.view.HomeScreenTwo;
import com.ivy.ui.task.TaskConstant;
import com.ivy.ui.task.TaskContract;
import com.ivy.ui.task.TaskTestDataFactory;
import com.ivy.ui.task.data.TaskDataManager;
import com.ivy.ui.task.model.TaskDataBO;
import com.ivy.ui.task.model.TaskRetailerBo;
import com.ivy.utils.DateTimeUtils;
import com.ivy.utils.FileUtils;
import com.ivy.utils.rx.TestSchedulerProvider;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Callable;

import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.TestScheduler;

import static com.ivy.utils.DateTimeUtils.DATE_GLOBAL;
import static org.junit.Assert.assertEquals;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.withSettings;

@RunWith(MockitoJUnitRunner.class)
public class TaskPresenterTest {
    @Mock
    private
    DataManager mDataManager;

    private CompositeDisposable mockDisposable = new CompositeDisposable();

    @Mock
    private ConfigurationMasterHelper mockConfigurationHelper;

    private TestScheduler testScheduler = new TestScheduler();

    @Mock
    private TaskDataManager mockTaskDataManager;

    @Mock
    private
    AppDataProvider mockAppDataProvider;

    @Mock
    private
    LabelsDataManager mockLabelsDataManager;

    @Mock
    private OutletTimeStampDataManager mockOutletTimeStampDataManager;

    @Mock
    private UserDataManager mockUserDataManager;

    @Mock
    private ChannelDataManager mockChannelDataManager;

    @Mock
    private ReasonDataManager mockReasonDataManager;

    private TaskContract.TaskView parentView;

    private TaskPresenterImpl<TaskContract.TaskView> taskPresenter;

    @Before
    public void setUp() {
        TestSchedulerProvider testSchedulerProvider = new TestSchedulerProvider(testScheduler);
        parentView = mock(TaskContract.TaskView.class, withSettings().extraInterfaces(TaskContract.TaskListView.class, TaskContract.TaskCreationView.class, TaskContract.TaskUnplannedView.class));

        taskPresenter = new TaskPresenterImpl<>(mDataManager, testSchedulerProvider,
                mockDisposable, mockConfigurationHelper,
                parentView, mockUserDataManager,
                mockChannelDataManager, mockTaskDataManager,
                mockAppDataProvider, mockOutletTimeStampDataManager,
                mockReasonDataManager, mockLabelsDataManager);
    }

    @Test
    public void fetchLabels() {
        HashMap<String, String> labelMap = TaskTestDataFactory.getLabels();
        given(mockLabelsDataManager.getLabels(TaskConstant.TASK_TITLE_LABEL,
                TaskConstant.TASK_DUE_DATE_LABEL, TaskConstant.TASK_CREATED_BY_LABEL,
                TaskConstant.TASK_APPLICABLE_FOR_LABEL, TaskConstant.TASK_PHOTO_CAPTURE_LABEL,
                TaskConstant.TASK_DESCRIPTION_LABEL, TaskConstant.TASK_EVIDENCE_LABEL))
                .willReturn(Observable.fromCallable(new Callable<HashMap<String, String>>() {
                    @Override
                    public HashMap<String, String> call() throws Exception {
                        return labelMap;
                    }
                }));

        taskPresenter.fetchLabels();
        testScheduler.triggerActions();
        then(parentView).should().updateLabelNames(labelMap);
    }

    @Test
    public void fetchTaskCreationData() {
        TaskTestDataFactory.userMasterBO.setUserid(123);
        given(mockAppDataProvider.getUser()).willReturn(TaskTestDataFactory.userMasterBO);
        assertEquals(taskPresenter.getUserID(), 123);

        ArrayList<UserMasterBO> parentList = TaskTestDataFactory.getParentUserList();
        ArrayList<UserMasterBO> childList = TaskTestDataFactory.getChildUserList();
        ArrayList<UserMasterBO> peerList = TaskTestDataFactory.getPeerUserList();
        HashMap<String, ArrayList<UserMasterBO>> linkUSerList = TaskTestDataFactory.getLinkUserList();
        ArrayList<RetailerMasterBO> retailerList = TaskTestDataFactory.getAllRetailer();
        ArrayList<TaskDataBO> imageList = TaskTestDataFactory.getTaskImgList();


        given(mockUserDataManager.fetchParentUsers()).willReturn(Observable.fromCallable(new Callable<ArrayList<UserMasterBO>>() {
            @Override
            public ArrayList<UserMasterBO> call() throws Exception {
                return parentList;
            }
        }));

        given(mockUserDataManager.fetchChildUsers()).willReturn(Observable.fromCallable(new Callable<ArrayList<UserMasterBO>>() {
            @Override
            public ArrayList<UserMasterBO> call() throws Exception {
                return childList;
            }
        }));


        given(mockUserDataManager.fetchPeerUsers()).willReturn(Observable.fromCallable(new Callable<ArrayList<UserMasterBO>>() {
            @Override
            public ArrayList<UserMasterBO> call() throws Exception {
                return peerList;
            }
        }));

        given(mockUserDataManager.fetchLinkUsers(0)).willReturn(Observable.fromCallable(new Callable<HashMap<String, ArrayList<UserMasterBO>>>() {
            @Override
            public HashMap<String, ArrayList<UserMasterBO>> call() throws Exception {
                return linkUSerList;
            }
        }));

        given(mockTaskDataManager.fetchAllRetailers()).willReturn(Observable.fromCallable(new Callable<ArrayList<RetailerMasterBO>>() {
            @Override
            public ArrayList<RetailerMasterBO> call() throws Exception {
                return retailerList;
            }
        }));

        given(mockTaskDataManager.fetchTaskImageData("1", 123)).willReturn(Observable.fromCallable(new Callable<ArrayList<TaskDataBO>>() {
            @Override
            public ArrayList<TaskDataBO> call() throws Exception {
                return imageList;
            }
        }));

        taskPresenter.fetchTaskCreationData(0, "1");
        testScheduler.triggerActions();


        then(parentView).should().showLoading();
        then((TaskContract.TaskCreationView) parentView).should().setParentUserListData(parentList);

        then((TaskContract.TaskCreationView) parentView).should().setChildUserListData(childList);

        then((TaskContract.TaskCreationView) parentView).should().setPeerUserListData(peerList);

        then((TaskContract.TaskCreationView) parentView).should().setLinkUserListData(linkUSerList);

        then((TaskContract.TaskCreationView) parentView).should().setTaskRetailerListData(retailerList);

        then(parentView).should().updateListData(imageList);

        then(parentView).should().hideLoading();
    }

    @Test
    public void fetchTaskCategorySuccess() {

        mockConfigurationHelper.TASK_PRODUCT_LEVEL_NO = 4;
        ArrayList<TaskDataBO> mockProList = TaskTestDataFactory.getProductList();
        given(mockTaskDataManager.fetchTaskCategories(mockConfigurationHelper.TASK_PRODUCT_LEVEL_NO)).willReturn(Observable.fromCallable(new Callable<ArrayList<TaskDataBO>>() {
            @Override
            public ArrayList<TaskDataBO> call() throws Exception {
                return mockProList;
            }
        }));
  //Pre Condition
        taskPresenter.fetchTaskCategory();
        testScheduler.triggerActions();
  // Post Condition
        InOrder inOrder = Mockito.inOrder(parentView);
        then(parentView).should(inOrder).showLoading();
        then((TaskContract.TaskCreationView) parentView).should(inOrder).setTaskCategoryListData(mockProList);
        then(parentView).should(inOrder).hideLoading();


    }

    @Test
    public void fetchTaskCategoryFailed() {
        mockConfigurationHelper.TASK_PRODUCT_LEVEL_NO = 4;
        ArrayList<TaskDataBO> mockProList = new ArrayList<>();
        given(mockTaskDataManager.fetchTaskCategories(mockConfigurationHelper.TASK_PRODUCT_LEVEL_NO)).willReturn(Observable.fromCallable(new Callable<ArrayList<TaskDataBO>>() {
            @Override
            public ArrayList<TaskDataBO> call() throws Exception {
                return mockProList;
            }
        }));

        taskPresenter.fetchTaskCategory();
        testScheduler.triggerActions();

        parentView.showLoading();
        parentView.hideLoading();
    }


    @Test
    public void fetchTaskImageListSuccess() {
        TaskTestDataFactory.userMasterBO.setUserid(123);
        given(mockAppDataProvider.getUser()).willReturn(TaskTestDataFactory.userMasterBO);
        assertEquals(taskPresenter.getUserID(), 123);
        ArrayList<TaskDataBO> mockImgList = TaskTestDataFactory.getTaskImgList();
        given(mockTaskDataManager.fetchTaskImageData("1", 123)).willReturn(Observable.fromCallable(new Callable<ArrayList<TaskDataBO>>() {
            @Override
            public ArrayList<TaskDataBO> call() throws Exception {
                return mockImgList;
            }
        }));

        taskPresenter.fetchTaskImageList("1");
        testScheduler.triggerActions();

        parentView.showLoading();
        parentView.updateListData(mockImgList);
        parentView.hideLoading();
    }


    @Test
    public void fetchTaskImageListFailed() {
        TaskTestDataFactory.userMasterBO.setUserid(123);
        given(mockAppDataProvider.getUser()).willReturn(TaskTestDataFactory.userMasterBO);
        assertEquals(taskPresenter.getUserID(), 123);
        ArrayList<TaskDataBO> mockImgList = new ArrayList<>();
        given(mockTaskDataManager.fetchTaskImageData(null, 123)).willReturn(Observable.fromCallable(new Callable<ArrayList<TaskDataBO>>() {
            @Override
            public ArrayList<TaskDataBO> call() throws Exception {
                return mockImgList;
            }
        }));

        taskPresenter.fetchTaskImageList(null);
        testScheduler.triggerActions();

        parentView.showLoading();
        parentView.hideLoading();
    }

    @Test
    public void fetchCompletedTaskSuccess() {
        ArrayList<TaskDataBO> mockTaskCompList = TaskTestDataFactory.getCompletedTask();
        given(mockTaskDataManager.fetchCompletedTask("1")).willReturn(Observable.fromCallable(new Callable<ArrayList<TaskDataBO>>() {
            @Override
            public ArrayList<TaskDataBO> call() throws Exception {
                return mockTaskCompList;
            }
        }));

        //Pre Condition
        taskPresenter.fetchCompletedTask("1");
        testScheduler.triggerActions();

        //Post condition
        then(parentView).should().showLoading();
        then(parentView).should().updateListData(mockTaskCompList);
        then(parentView).should().hideLoading();
    }

    @Test
    public void fetchCompletedTaskFailed() {
        ArrayList<TaskDataBO> mockTaskCompList = new ArrayList<>();
        given(mockTaskDataManager.fetchCompletedTask(null))
                .willReturn(Observable.fromCallable(new Callable<ArrayList<TaskDataBO>>() {
                    @Override
                    public ArrayList<TaskDataBO> call() throws Exception {
                        return mockTaskCompList;
                    }
                }));

        taskPresenter.fetchCompletedTask(null);
        testScheduler.triggerActions();
        then(parentView).should().showLoading();
        then(parentView).should().hideLoading();
        then((TaskContract.TaskListView) parentView).should().showDataNotMappedMsg();
    }


    @Test
    public void fetchReasonsFromSTDSuccess() {

        ArrayList<ReasonMaster> mockReasonList = TaskTestDataFactory.getReasonList();
        given(mockReasonDataManager.fetchReasonFromStdListMasterByListCode(TaskConstant.TASK_NOT_COMPLETE_REASON))
                .willReturn(Observable.fromCallable(new Callable<ArrayList<ReasonMaster>>() {
                    @Override
                    public ArrayList<ReasonMaster> call() throws Exception {
                        return mockReasonList;
                    }
                }));
        taskPresenter.fetchReasonFromStdListMasterByListCode();
        testScheduler.triggerActions();
        then(parentView).shouldHaveZeroInteractions();
    }

    @Test
    public void testGetRetailerId() {
        TaskTestDataFactory.retailerMasterBO.setRetailerID("1");
        given(mockAppDataProvider.getRetailMaster()).willReturn(TaskTestDataFactory.retailerMasterBO);
        assertEquals(taskPresenter.getRetailerID(), 1);
    }


    @Test
    public void testGetUserId() {
        TaskTestDataFactory.userMasterBO.setUserid(2);
        given(mockAppDataProvider.getUser()).willReturn(TaskTestDataFactory.userMasterBO);
        assertEquals(taskPresenter.getUserID(), 2);
    }

    @Test
    public void fetchTaskListDataSuccess() {

        ArrayList<TaskDataBO> mockTaskListData = TaskTestDataFactory.getCompletedTask();
        ArrayList<ReasonMaster> mockReasonList = TaskTestDataFactory.getReasonList();
        String mockChannelIds = TaskTestDataFactory.getChannelIdList();

        TaskTestDataFactory.userMasterBO.setUserid(123);
        given(mockAppDataProvider.getUser()).willReturn(TaskTestDataFactory.userMasterBO);
        assertEquals(taskPresenter.getUserID(), 123);

        given(mockChannelDataManager.fetchChannelIds())
                .willReturn(Single.fromCallable(new Callable<String>() {
                    @Override
                    public String call() throws Exception {
                        return mockChannelIds;
                    }
                }));

        given(mockTaskDataManager.fetchTaskData("0", 1, false))
                .willReturn(Observable.fromCallable(new Callable<ArrayList<TaskDataBO>>() {
                    @Override
                    public ArrayList<TaskDataBO> call() throws Exception {
                        return mockTaskListData;
                    }
                }));

        given(mockReasonDataManager.fetchReasonFromStdListMasterByListCode(TaskConstant.TASK_NOT_COMPLETE_REASON))
                .willReturn(Observable.fromCallable(new Callable<ArrayList<ReasonMaster>>() {
                    @Override
                    public ArrayList<ReasonMaster> call() throws Exception {
                        return mockReasonList;
                    }
                }));
        taskPresenter.getTaskListData(1, "0", false, false, false);
        testScheduler.triggerActions();

        then(parentView).should().showLoading();
        then(parentView).should().updateListData(mockTaskListData);
        then(parentView).should().hideLoading();
    }


    @Test
    public void fetchTaskListUserIdNotMatchedSuccess() {

        ArrayList<TaskDataBO> mockTaskListData = TaskTestDataFactory.getTaskWithoutUserId();
        ArrayList<ReasonMaster> mockReasonList = TaskTestDataFactory.getReasonList();
        String mockChannelIds = TaskTestDataFactory.getChannelIdList();

        TaskTestDataFactory.userMasterBO.setUserid(123);
        given(mockAppDataProvider.getUser()).willReturn(TaskTestDataFactory.userMasterBO);
        assertEquals(taskPresenter.getUserID(), 123);

        given(mockChannelDataManager.fetchChannelIds())
                .willReturn(Single.fromCallable(new Callable<String>() {
                    @Override
                    public String call() throws Exception {
                        return mockChannelIds;
                    }
                }));

        given(mockTaskDataManager.fetchTaskData("0", 1, false))
                .willReturn(Observable.fromCallable(new Callable<ArrayList<TaskDataBO>>() {
                    @Override
                    public ArrayList<TaskDataBO> call() throws Exception {
                        return mockTaskListData;
                    }
                }));

        given(mockReasonDataManager.fetchReasonFromStdListMasterByListCode(TaskConstant.TASK_NOT_COMPLETE_REASON))
                .willReturn(Observable.fromCallable(new Callable<ArrayList<ReasonMaster>>() {
                    @Override
                    public ArrayList<ReasonMaster> call() throws Exception {
                        return mockReasonList;
                    }
                }));
        taskPresenter.getTaskListData(1, "0", false, false, false);
        testScheduler.triggerActions();

        then(parentView).should().showLoading();
        then(parentView).should().hideLoading();
        then((TaskContract.TaskListView) parentView).should().showDataNotMappedMsg();


    }

    @Test
    public void fetchTaskByRetailerWiseSuccess() {

        ArrayList<TaskDataBO> mockTaskListData = TaskTestDataFactory.getTaskByRetailerWise();
        ArrayList<ReasonMaster> mockReasonList = TaskTestDataFactory.getReasonList();
        String mockChannelIds = TaskTestDataFactory.getChannelIdList();

        TaskTestDataFactory.userMasterBO.setUserid(123);
        given(mockAppDataProvider.getUser()).willReturn(TaskTestDataFactory.userMasterBO);
        assertEquals(taskPresenter.getUserID(), 123);

        given(mockChannelDataManager.fetchChannelIds())
                .willReturn(Single.fromCallable(new Callable<String>() {
                    @Override
                    public String call() throws Exception {
                        return mockChannelIds;
                    }
                }));

        given(mockTaskDataManager.fetchTaskData("1", 1, false))
                .willReturn(Observable.fromCallable(new Callable<ArrayList<TaskDataBO>>() {
                    @Override
                    public ArrayList<TaskDataBO> call() throws Exception {
                        return mockTaskListData;
                    }
                }));

        given(mockReasonDataManager.fetchReasonFromStdListMasterByListCode(TaskConstant.TASK_NOT_COMPLETE_REASON))
                .willReturn(Observable.fromCallable(new Callable<ArrayList<ReasonMaster>>() {
                    @Override
                    public ArrayList<ReasonMaster> call() throws Exception {
                        return mockReasonList;
                    }
                }));
        taskPresenter.getTaskListData(1, "1", true, false, false);
        testScheduler.triggerActions();

        then(parentView).should().showLoading();
        then(parentView).should().updateListData(mockTaskListData);
        then(parentView).should().hideLoading();
    }

    @Test
    public void fetchTaskBySurveyWiseSuccess() {

        ArrayList<TaskDataBO> mockTaskListData = TaskTestDataFactory.getTaskByRetailerWise();
        ArrayList<ReasonMaster> mockReasonList = TaskTestDataFactory.getReasonList();
        String mockChannelIds = TaskTestDataFactory.getChannelIdList();

        TaskTestDataFactory.userMasterBO.setUserid(123);
        given(mockAppDataProvider.getUser()).willReturn(TaskTestDataFactory.userMasterBO);
        assertEquals(taskPresenter.getUserID(), 123);

        given(mockChannelDataManager.fetchChannelIds())
                .willReturn(Single.fromCallable(new Callable<String>() {
                    @Override
                    public String call() throws Exception {
                        return mockChannelIds;
                    }
                }));

        given(mockTaskDataManager.fetchTaskData("1", 1, false))
                .willReturn(Observable.fromCallable(new Callable<ArrayList<TaskDataBO>>() {
                    @Override
                    public ArrayList<TaskDataBO> call() throws Exception {
                        return mockTaskListData;
                    }
                }));

        given(mockReasonDataManager.fetchReasonFromStdListMasterByListCode(TaskConstant.TASK_NOT_COMPLETE_REASON))
                .willReturn(Observable.fromCallable(new Callable<ArrayList<ReasonMaster>>() {
                    @Override
                    public ArrayList<ReasonMaster> call() throws Exception {
                        return mockReasonList;
                    }
                }));
        taskPresenter.getTaskListData(1, "1", true, true, false);
        testScheduler.triggerActions();

        then(parentView).should().showLoading();
        then(parentView).should().updateListData(mockTaskListData);
        then(parentView).should().hideLoading();
    }

    @Test
    public void onSaveSuccess() {

        TaskTestDataFactory.userMasterBO.setUserid(123);
        given(mockAppDataProvider.getUser()).willReturn(TaskTestDataFactory.userMasterBO);
        assertEquals(taskPresenter.getUserID(), 123);


        TaskDataBO mockBo = TaskTestDataFactory.getMockTaskBo();
        ArrayList<TaskDataBO> mockImgList = null;
        given(mockTaskDataManager.saveTask(12, mockBo, mockImgList, 0))
                .willReturn(Single.fromCallable(new Callable<Boolean>() {
                    @Override
                    public Boolean call() throws Exception {
                        return true;
                    }
                }));

        taskPresenter.onSaveTask(12, mockBo, 0, 0);
        testScheduler.triggerActions();

        then(parentView).should().showLoading();
        then(parentView).should().hideLoading();
        then((TaskContract.TaskCreationView) parentView).should().showTaskSaveAlertMsg();


    }

    @Test
    public void onSaveFailed() {

        TaskTestDataFactory.userMasterBO.setUserid(123);
        given(mockAppDataProvider.getUser()).willReturn(TaskTestDataFactory.userMasterBO);
        assertEquals(taskPresenter.getUserID(), 123);


        TaskDataBO mockBo = TaskTestDataFactory.getMockTaskBoWithDueDateNull();
        ArrayList<TaskDataBO> mockImgList = null;
        given(mockTaskDataManager.saveTask(12, mockBo, mockImgList, 0))
                .willReturn(Single.fromCallable(new Callable<Boolean>() {
                    @Override
                    public Boolean call() throws Exception {
                        return false;
                    }
                }));

        taskPresenter.onSaveTask(12, mockBo, 0, 0);
        testScheduler.triggerActions();

        then(parentView).should().showLoading();
        then(parentView).should().hideLoading();
        then(parentView).should().showErrorMsg();


    }

    @Test
    public void updateTaskSuccess() {
        TaskTestDataFactory.userMasterBO.setUserid(123);
        given(mockAppDataProvider.getUser()).willReturn(TaskTestDataFactory.userMasterBO);
        assertEquals(taskPresenter.getUserID(), 123);


        TaskDataBO mockBo = TaskTestDataFactory.getMockTaskBo();
        ArrayList<TaskDataBO> mockImgList = null;

        given(mockTaskDataManager.updateTaskExecutionData(mockBo, "0", 0))
                .willReturn(Single.fromCallable(new Callable<Boolean>() {
                    @Override
                    public Boolean call() throws Exception {
                        return true;
                    }
                }));
        taskPresenter.updateTaskExecution("0", mockBo, 0);
        testScheduler.triggerActions();

        then((TaskContract.TaskListView) parentView).should().showTaskUpdateMsg();
    }

    @Test
    public void updateTaskReasonIdSuccess() {
        TaskTestDataFactory.userMasterBO.setUserid(123);
        given(mockAppDataProvider.getUser()).willReturn(TaskTestDataFactory.userMasterBO);
        assertEquals(taskPresenter.getUserID(), 123);


        TaskDataBO mockBo = TaskTestDataFactory.getMockTaskBo();
        ArrayList<TaskDataBO> mockImgList = null;

        given(mockTaskDataManager.updateTaskExecutionData(mockBo, "0", 12))
                .willReturn(Single.fromCallable(new Callable<Boolean>() {
                    @Override
                    public Boolean call() throws Exception {
                        return true;
                    }
                }));
        taskPresenter.updateTaskExecution("0", mockBo, 12);
        testScheduler.triggerActions();

        then((TaskContract.TaskListView) parentView).should().showTaskReasonUpdateMsg();
    }

    @Test
    public void updateTaskImageSuccess() {
        TaskTestDataFactory.userMasterBO.setUserid(123);
        given(mockAppDataProvider.getUser()).willReturn(TaskTestDataFactory.userMasterBO);
        assertEquals(taskPresenter.getUserID(), 123);

        TaskConstant.TASK_SERVER_IMG_PATH = "Download/"
                + taskPresenter.getUserID()
                + DataMembers.DIGITAL_CONTENT + "/"
                + DataMembers.TASK_DIGITAL_CONTENT;

        FileUtils.photoFolderPath = "Download/"
                + taskPresenter.getUserID()
                + DataMembers.DIGITAL_CONTENT + "/"
                + DataMembers.TASK_DIGITAL_CONTENT;

        given(mockTaskDataManager.updateTaskExecutionImage("image Name1", "1235"))
                .willReturn(Single.fromCallable(new Callable<Boolean>() {
                    @Override
                    public Boolean call() throws Exception {
                        return true;
                    }
                }));
        taskPresenter.updateTaskExecutionImg("image Name1", "1235", false);
        testScheduler.triggerActions();
        then(parentView).should().showImageUpdateMsg();
    }

    @Test
    public void testUpdateModuleTime() {
        String date = DateTimeUtils.now(DATE_GLOBAL) + " " + DateTimeUtils.now(DateTimeUtils.TIME);
        if (mockConfigurationHelper.IS_DISABLE_CALL_ANALYSIS_TIMER)
            date = IvyConstants.DEFAULT_TIME_CONSTANT;
        given(mockOutletTimeStampDataManager.updateTimeStampModuleWise(date)).willReturn(Single.just(true));

        taskPresenter.updateModuleTime();
        testScheduler.triggerActions();
        then(parentView).shouldHaveZeroInteractions();
    }

    @Test
    public void testSaveModuleCompletion() {
        given(mDataManager.saveModuleCompletion(HomeScreenTwo.MENU_TASK)).willReturn(Single.just(true));
        taskPresenter.saveModuleCompletion(HomeScreenTwo.MENU_TASK);
        testScheduler.triggerActions();
        then(parentView).shouldHaveZeroInteractions();
    }

    @Test
    public void testGetRetailerMasterBoSuccess() {
        ArrayList<RetailerMasterBO> mockRetailerList = TaskTestDataFactory.getAllRetailer();
        given(mockAppDataProvider.getRetailerMasters())
                .willReturn(mockRetailerList);
        assertEquals(mockAppDataProvider.getRetailerMasters(), mockRetailerList);

        taskPresenter.getRetailerMasterBo("1");
        testScheduler.triggerActions();
        then(parentView).shouldHaveZeroInteractions();
    }

    @Test
    public void testValidateTitle() {
        taskPresenter.validate("", "task Desc", "12/06/2019", 1, 2);
        testScheduler.triggerActions();
        then((TaskContract.TaskCreationView) parentView).should().showTaskTitleError();
    }

    @Test
    public void testValidateTaskDesc() {
        taskPresenter.validate("title", "", "12/06/2019", 1, 2);
        testScheduler.triggerActions();
        then((TaskContract.TaskCreationView) parentView).should().showTaskDescError();
    }

    @Test
    public void testValidateDueDate() {
        taskPresenter.validate("title", "task Desc", null, 1, 2);
        testScheduler.triggerActions();
        then((TaskContract.TaskCreationView) parentView).should().showTaskDueDateError();
    }

    @Test
    public void testValidateRetSelection() {
        taskPresenter.validate("title", "task Desc", "12/06/2019", 0, 2);
        testScheduler.triggerActions();
        then((TaskContract.TaskCreationView) parentView).should().showRetSelectionError();
    }

    @Test
    public void testValidateSpinnerSelection() {
        taskPresenter.validate("title", "task Desc", "12/06/2019", 1, 0);
        testScheduler.triggerActions();
        then((TaskContract.TaskCreationView) parentView).should().showSpinnerSelectionError();
    }


    @Test
    public void testNPReasonAvailable() {

        given(mockReasonDataManager.isNpReasonPhotoAvailable("1", "MENU_TASK"))
                .willReturn(Single.fromCallable(new Callable<Boolean>() {
                    @Override
                    public Boolean call() throws Exception {
                        return true;
                    }
                }));

        taskPresenter.isNPPhotoReasonAvailable("1", "MENU_TASK");
        testScheduler.triggerActions();

        then(parentView).shouldHaveZeroInteractions();
    }

    @Test
    public void testFetchUnplannedTaskSuccess() {
        mockConfigurationHelper.IS_TASK_DUDE_DATE_COUNT = 5;
        ArrayList<TaskRetailerBo> mockRetTaskList = TaskTestDataFactory.getMockRetTaskList();
        HashMap<String, ArrayList<TaskDataBO>> mockHashList = TaskTestDataFactory.getMockHashList();

        given(mockTaskDataManager.fetchUnPlannedRetailers(mockConfigurationHelper.IS_TASK_DUDE_DATE_COUNT))
                .willReturn(Observable.fromCallable(new Callable<ArrayList<TaskRetailerBo>>() {
                    @Override
                    public ArrayList<TaskRetailerBo> call() throws Exception {
                        return mockRetTaskList;
                    }
                }));

        given(mockTaskDataManager.fetchUnPlanedTaskData(mockConfigurationHelper.IS_TASK_DUDE_DATE_COUNT))
                .willReturn(Observable.fromCallable(new Callable<HashMap<String, ArrayList<TaskDataBO>>>() {
                    @Override
                    public HashMap<String, ArrayList<TaskDataBO>> call() throws Exception {
                        return mockHashList;
                    }
                }));

        taskPresenter.fetchUnPlannedTask();
        testScheduler.triggerActions();

        then(parentView).should().showLoading();
        then(((TaskContract.TaskUnplannedView) parentView)).should().updateUnplannedTaskList(mockRetTaskList, mockHashList);
        then(parentView).should().hideLoading();


    }

    @Test
    public void testDeleteNonApprovalTaskSuccess() {

        given(mockTaskDataManager.getDeletedImageList("123"))
                .willReturn(Observable.fromCallable(new Callable<ArrayList<String>>() {
                    @Override
                    public ArrayList<String> call() throws Exception {
                        return new ArrayList<>();
                    }
                }));

        given(mockTaskDataManager.deleteTaskData("123", "self", 0))
                .willReturn(Single.fromCallable(new Callable<Boolean>() {
                    @Override
                    public Boolean call() throws Exception {
                        return true;
                    }
                }));
        taskPresenter.deleteTask("123", "self", 0);
        testScheduler.triggerActions();

        then(parentView).should().showLoading();
        then(parentView).should().hideLoading();
        then(parentView).should().onDeleteSuccess();

    }


    @Test
    public void testDeleteNonApprovalTaskFailed() {
        given(mockTaskDataManager.getDeletedImageList(null))
                .willReturn(Observable.fromCallable(new Callable<ArrayList<String>>() {
                    @Override
                    public ArrayList<String> call() throws Exception {
                        return new ArrayList<>();
                    }
                }));

        given(mockTaskDataManager.deleteTaskData(null, "", 0))
                .willReturn(Single.fromCallable(new Callable<Boolean>() {
                    @Override
                    public Boolean call() throws Exception {
                        return false;
                    }
                }));
        taskPresenter.deleteTask(null, "", 0);
        testScheduler.triggerActions();

        then(parentView).should().showLoading();
        then(parentView).should().hideLoading();
        then(parentView).should().showErrorMsg();

    }

    @Test
    public void testSortByTaskTitle() {

        ArrayList<TaskDataBO> mockTaskListData = TaskTestDataFactory.getCompletedTask();
        String mockChannelIds = TaskTestDataFactory.getChannelIdList();

        TaskTestDataFactory.userMasterBO.setUserid(123);
        given(mockAppDataProvider.getUser()).willReturn(TaskTestDataFactory.userMasterBO);
        assertEquals(taskPresenter.getUserID(), 123);


        taskPresenter.orderBySortList(mockTaskListData, TaskConstant.TASK_TITLE_ASC, true);
        testScheduler.triggerActions();
        Collections.sort(mockTaskListData, (fstr, sstr) -> fstr.getTasktitle().compareToIgnoreCase(sstr.getTasktitle()));
        then(parentView).should().updateListData(mockTaskListData);

    }


    @Test
    public void testSortByTaskTitleDesc() {

        ArrayList<TaskDataBO> mockTaskListData = TaskTestDataFactory.getCompletedTask();
        String mockChannelIds = TaskTestDataFactory.getChannelIdList();

        TaskTestDataFactory.userMasterBO.setUserid(123);
        given(mockAppDataProvider.getUser()).willReturn(TaskTestDataFactory.userMasterBO);
        assertEquals(taskPresenter.getUserID(), 123);


        taskPresenter.orderBySortList(mockTaskListData, TaskConstant.TASK_TITLE_DESC, false);
        testScheduler.triggerActions();
        Collections.sort(mockTaskListData, (sstr, fstr) -> sstr.getTasktitle().compareToIgnoreCase(fstr.getTasktitle()));
        then(parentView).should().updateListData(mockTaskListData);

    }


    @Test
    public void testSortByProdLevelAsc() {

        ArrayList<TaskDataBO> mockTaskListData = TaskTestDataFactory.getCompletedTask();
        String mockChannelIds = TaskTestDataFactory.getChannelIdList();

        TaskTestDataFactory.userMasterBO.setUserid(123);
        given(mockAppDataProvider.getUser()).willReturn(TaskTestDataFactory.userMasterBO);
        assertEquals(taskPresenter.getUserID(), 123);


        taskPresenter.orderBySortList(mockTaskListData, TaskConstant.PRODUCT_LEVEL_ASC, true);
        testScheduler.triggerActions();

        Collections.sort(mockTaskListData, (fstr, sstr) -> fstr.getTaskCategoryDsc().compareToIgnoreCase(sstr.getTaskCategoryDsc()));
        then(parentView).should().updateListData(mockTaskListData);

    }


    @Test
    public void testSortByProdLevelDesc() {

        ArrayList<TaskDataBO> mockTaskListData = TaskTestDataFactory.getCompletedTask();
        String mockChannelIds = TaskTestDataFactory.getChannelIdList();

        TaskTestDataFactory.userMasterBO.setUserid(123);
        given(mockAppDataProvider.getUser()).willReturn(TaskTestDataFactory.userMasterBO);
        assertEquals(taskPresenter.getUserID(), 123);

        taskPresenter.orderBySortList(mockTaskListData, TaskConstant.PRODUCT_LEVEL_DESC, false);
        testScheduler.triggerActions();

        Collections.sort(mockTaskListData, (sstr, fstr) -> sstr.getTaskCategoryDsc().compareToIgnoreCase(fstr.getTaskCategoryDsc()));
        then(parentView).should().updateListData(mockTaskListData);

    }


    @Test
    public void testSortByDueDateAsc() {

        ArrayList<TaskDataBO> mockTaskListData = TaskTestDataFactory.getCompletedTask();
        String mockChannelIds = TaskTestDataFactory.getChannelIdList();

        TaskTestDataFactory.userMasterBO.setUserid(123);
        given(mockAppDataProvider.getUser()).willReturn(TaskTestDataFactory.userMasterBO);
        assertEquals(taskPresenter.getUserID(), 123);

        taskPresenter.orderBySortList(mockTaskListData, 4, true);
        testScheduler.triggerActions();

        Collections.sort(mockTaskListData, (fstr, sstr) -> fstr.getTaskDueDate().compareToIgnoreCase(sstr.getTaskDueDate()));
        then(parentView).should().updateListData(mockTaskListData);

    }

    @Test
    public void testSortByDueDateDesc() {

        ArrayList<TaskDataBO> mockTaskListData = TaskTestDataFactory.getCompletedTask();
        String mockChannelIds = TaskTestDataFactory.getChannelIdList();

        TaskTestDataFactory.userMasterBO.setUserid(123);
        given(mockAppDataProvider.getUser()).willReturn(TaskTestDataFactory.userMasterBO);
        assertEquals(taskPresenter.getUserID(), 123);

        taskPresenter.orderBySortList(mockTaskListData, 5, false);
        testScheduler.triggerActions();

        Collections.sort(mockTaskListData, (sstr, fstr) -> sstr.getTaskDueDate().compareToIgnoreCase(fstr.getTaskDueDate()));
        then(parentView).should().updateListData(mockTaskListData);

    }

    @After
    public void tearDown() {
        taskPresenter.onDetach();
    }
}
