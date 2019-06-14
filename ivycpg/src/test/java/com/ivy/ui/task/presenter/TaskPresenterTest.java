package com.ivy.ui.task.presenter;

import com.ivy.core.data.app.AppDataProvider;
import com.ivy.core.data.channel.ChannelDataManager;
import com.ivy.core.data.datamanager.DataManager;
import com.ivy.core.data.label.LabelsDataManager;
import com.ivy.core.data.outlettime.OutletTimeStampDataManager;
import com.ivy.core.data.reason.ReasonDataManager;
import com.ivy.core.data.user.UserDataManager;
import com.ivy.sd.png.bo.RetailerMasterBO;
import com.ivy.sd.png.bo.UserMasterBO;
import com.ivy.sd.png.provider.ConfigurationMasterHelper;
import com.ivy.ui.task.TaskConstant;
import com.ivy.ui.task.TaskContract;
import com.ivy.ui.task.TaskTestDataFactory;
import com.ivy.ui.task.data.TaskDataManager;
import com.ivy.ui.task.model.TaskDataBO;
import com.ivy.utils.rx.TestSchedulerProvider;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.Callable;

import io.reactivex.Observable;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.TestScheduler;

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

        taskPresenter.fetchTaskCategory();
        testScheduler.triggerActions();
        parentView.showLoading();
        then((TaskContract.TaskCreationView) parentView).should().setTaskCategoryListData(mockProList);
        parentView.hideLoading();


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
    public void fetchTaskImageListFailed(){
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
    public void fetchCompletedTaskSuccess(){

        given(mockTaskDataManager.fetchCompletedTask("1")).willReturn(Observable.fromCallable(new Callable<ArrayList<TaskDataBO>>() {
            @Override
            public ArrayList<TaskDataBO> call() throws Exception {
                return null;
            }
        }));
    }
}
