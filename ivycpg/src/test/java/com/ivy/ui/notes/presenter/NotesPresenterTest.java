package com.ivy.ui.notes.presenter;

import com.ivy.core.data.app.AppDataProvider;
import com.ivy.core.data.datamanager.DataManager;
import com.ivy.core.data.label.LabelsDataManager;
import com.ivy.core.data.outlettime.OutletTimeStampDataManager;
import com.ivy.sd.png.bo.RetailerMasterBO;
import com.ivy.sd.png.provider.ConfigurationMasterHelper;
import com.ivy.sd.png.view.HomeScreenTwo;
import com.ivy.ui.notes.NoteConstant;
import com.ivy.ui.notes.NotesContract;
import com.ivy.ui.notes.NotesTestDataFactory;
import com.ivy.ui.notes.data.NotesDataManager;
import com.ivy.ui.notes.model.NotesBo;
import com.ivy.utils.DateTimeUtils;
import com.ivy.utils.rx.TestSchedulerProvider;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.concurrent.Callable;

import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.TestScheduler;

import static org.junit.Assert.assertEquals;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.withSettings;

@RunWith(MockitoJUnitRunner.class)
public class NotesPresenterTest {

    private NotesContract.NotesView mView;

    @Mock
    private
    DataManager mDataManager;

    private CompositeDisposable mockDisposable = new CompositeDisposable();

    @Mock
    private ConfigurationMasterHelper mockConfigurationHelper;

    private TestScheduler testScheduler = new TestScheduler();

    @Mock
    private NotesDataManager notesDataManager;

    @Mock
    private
    AppDataProvider mockAppDataProvider;

    @Mock
    private
    LabelsDataManager labelsDataManager;

    @Mock
    private OutletTimeStampDataManager mockOutletTimeStampDataManager;


    private NotesPresenterImpl<NotesContract.NotesView> mPresenter;

    @Before
    public void setUp() {
        TestSchedulerProvider testSchedulerProvider = new TestSchedulerProvider(testScheduler);
        mView = mock(NotesContract.NotesView.class, withSettings().extraInterfaces(NotesContract.NotesListView.class, NotesContract.NoteCreationView.class));

        mPresenter = new NotesPresenterImpl<>(mDataManager, testSchedulerProvider,
                mockDisposable, mockConfigurationHelper,
                mView, mockOutletTimeStampDataManager,
                mockAppDataProvider, notesDataManager, labelsDataManager);
    }


    @Test
    public void testFetchLabels() {
        HashMap<String, String> labelMap = NotesTestDataFactory.getLabels();
        given(labelsDataManager.getLabels(NoteConstant.NOTE_TITLE_LABEL,
                NoteConstant.NOTE_DESC_LABEL, NoteConstant.NOTE_CREATED_BY_LABEL,
                NoteConstant.NOTE_CREATED_DATE_LABEL, NoteConstant.NOTE_MODIFIED_BY_LABEL,
                NoteConstant.NOTE_MODIFIED_DATE_LABEL, NoteConstant.NOTE_TYPE_HERE_HINT_LABEL,
                NoteConstant.NOTE_ASSIGN_TO_RETAILER_LABEL))
                .willReturn(Observable.fromCallable(new Callable<HashMap<String, String>>() {
                    @Override
                    public HashMap<String, String> call() throws Exception {
                        return labelMap;
                    }
                }));

        mPresenter.fetchLabels();
        testScheduler.triggerActions();

        then(mView).should().updateLabelNames(labelMap);

    }


    @Test
    public void testFetchRetailerNames() {
        ArrayList<RetailerMasterBO> retailerMasterBOS = NotesTestDataFactory.getRetailerNames();
        given(notesDataManager.fetchRetailers())
                .willReturn(Observable.fromCallable(new Callable<ArrayList<RetailerMasterBO>>() {
                    @Override
                    public ArrayList<RetailerMasterBO> call() throws Exception {
                        return retailerMasterBOS;
                    }
                }));

        mPresenter.fetchRetailerNames();
        testScheduler.triggerActions();
        then(mView).should().showLoading();
        then((NotesContract.NoteCreationView) mView).should().setRetailerListData(retailerMasterBOS);
        then(mView).should().hideLoading();

    }


    @Test
    public void testFetchRetailerNamesFailed() {
        ArrayList<RetailerMasterBO> retailerMasterBOS = new ArrayList<>();
        given(notesDataManager.fetchRetailers())
                .willReturn(Observable.fromCallable(new Callable<ArrayList<RetailerMasterBO>>() {
                    @Override
                    public ArrayList<RetailerMasterBO> call() throws Exception {
                        return null;
                    }
                }));

        mPresenter.fetchRetailerNames();
        testScheduler.triggerActions();
        then(mView).should().showLoading();
        then(mView).should().hideLoading();
        then(mView).should().onErrorMsg();

    }

    @Test
    public void testFetchData() {
        NotesTestDataFactory.retailerMasterBO.setRetailerID("1");
        given(mockAppDataProvider.getRetailMaster()).willReturn(NotesTestDataFactory.retailerMasterBO);
        assertEquals(mPresenter.getRetailerID(), "1");
        ArrayList<NotesBo> notesBoArrayList = NotesTestDataFactory.getNotesList();
        given(notesDataManager.fetchNotesData("1", true))
                .willReturn(Observable.fromCallable(new Callable<ArrayList<NotesBo>>() {
                    @Override
                    public ArrayList<NotesBo> call() throws Exception {
                        return notesBoArrayList;
                    }
                }));

        mPresenter.fetchNotesData("1", true, -1, false);
        testScheduler.triggerActions();
        then(mView).should().showLoading();
        then((NotesContract.NotesListView) mView).should().updateNotesList(notesBoArrayList,false);
        then(mView).should().hideLoading();

    }

    @Test
    public void testFetchDataSortedByAscendingCreatedDate() {
        NotesTestDataFactory.retailerMasterBO.setRetailerID("1");
//        given(mockAppDataProvider.getRetailMaster()).willReturn(NotesTestDataFactory.retailerMasterBO);
        ArrayList<NotesBo> notesBoArrayList = NotesTestDataFactory.getNotesList();
        given(notesDataManager.fetchNotesData("1", true))
                .willReturn(Observable.fromCallable(new Callable<ArrayList<NotesBo>>() {
                    @Override
                    public ArrayList<NotesBo> call() throws Exception {
                        return notesBoArrayList;
                    }
                }));

        mPresenter.fetchNotesData("1", true, NoteConstant.CREATED_DATE_ASC, true);
        testScheduler.triggerActions();
        then(mView).should().showLoading();
        Collections.sort(notesBoArrayList, (fstr, sstr) -> fstr.getCreatedDate().compareToIgnoreCase(sstr.getCreatedDate()));
        then((NotesContract.NotesListView) mView).should().updateNotesList(notesBoArrayList,false);
        then(mView).should().hideLoading();
    }


    @Test
    public void testFetchDataSortedByDescendingCreatedDate() {
        NotesTestDataFactory.retailerMasterBO.setRetailerID("1");
//        given(mockAppDataProvider.getRetailMaster()).willReturn(NotesTestDataFactory.retailerMasterBO);
        ArrayList<NotesBo> notesBoArrayList = NotesTestDataFactory.getNotesList();
        given(notesDataManager.fetchNotesData("1", true))
                .willReturn(Observable.fromCallable(new Callable<ArrayList<NotesBo>>() {
                    @Override
                    public ArrayList<NotesBo> call() throws Exception {
                        return notesBoArrayList;
                    }
                }));

        mPresenter.fetchNotesData("1", true, NoteConstant.CREATED_DATE_DESC, false);
        testScheduler.triggerActions();

        then(mView).should().showLoading();
        Collections.sort(notesBoArrayList, (fstr, sstr) -> sstr.getCreatedDate().compareToIgnoreCase(fstr.getCreatedDate()));
        then((NotesContract.NotesListView) mView).should().updateNotesList(notesBoArrayList,false);
        then(mView).should().hideLoading();
    }

    @Test
    public void testFetchDataSortedByRecentlyUpdatedDec() {
        NotesTestDataFactory.retailerMasterBO.setRetailerID("1");
//        given(mockAppDataProvider.getRetailMaster()).willReturn(NotesTestDataFactory.retailerMasterBO);
        ArrayList<NotesBo> notesBoArrayList = NotesTestDataFactory.getNotesList();
        given(notesDataManager.fetchNotesData("1", true))
                .willReturn(Observable.fromCallable(new Callable<ArrayList<NotesBo>>() {
                    @Override
                    public ArrayList<NotesBo> call() throws Exception {
                        return notesBoArrayList;
                    }
                }));

        mPresenter.fetchNotesData("1", true, NoteConstant.RECENTLY_UPDATED, false);
        testScheduler.triggerActions();

        then(mView).should().showLoading();
        Collections.sort(notesBoArrayList, (fstr, sstr) -> sstr.getTid().compareToIgnoreCase(fstr.getTid()));
        then((NotesContract.NotesListView) mView).should().updateNotesList(notesBoArrayList,false);
        then(mView).should().hideLoading();
    }

    @Test
    public void testFetchDataSortedByAscendingModifiedDate() {
        NotesTestDataFactory.retailerMasterBO.setRetailerID("1");
        //given(mockAppDataProvider.getRetailMaster()).willReturn(NotesTestDataFactory.retailerMasterBO);
        ArrayList<NotesBo> notesBoArrayList = NotesTestDataFactory.getNotesList();
        given(notesDataManager.fetchNotesData("1", true))
                .willReturn(Observable.fromCallable(new Callable<ArrayList<NotesBo>>() {
                    @Override
                    public ArrayList<NotesBo> call() throws Exception {
                        return notesBoArrayList;
                    }
                }));

        mPresenter.fetchNotesData("1", true, NoteConstant.MODIFIED_DATE, true);
        testScheduler.triggerActions();
        then(mView).should().showLoading();
        Collections.sort(notesBoArrayList, (fstr, sstr) -> fstr.getModifiedDate().compareToIgnoreCase(sstr.getModifiedDate()));
        then((NotesContract.NotesListView) mView).should().updateNotesList(notesBoArrayList,false);
        then(mView).should().hideLoading();
    }

    @Test
    public void testFetchDataSortedByDescendingModifiedDate() {
        NotesTestDataFactory.retailerMasterBO.setRetailerID("1");
//        given(mockAppDataProvider.getRetailMaster()).willReturn(NotesTestDataFactory.retailerMasterBO);
        ArrayList<NotesBo> notesBoArrayList = NotesTestDataFactory.getNotesList();
        given(notesDataManager.fetchNotesData("1", true))
                .willReturn(Observable.fromCallable(new Callable<ArrayList<NotesBo>>() {
                    @Override
                    public ArrayList<NotesBo> call() throws Exception {
                        return notesBoArrayList;
                    }
                }));

        mPresenter.fetchNotesData("1", true, NoteConstant.MODIFIED_DATE, false);
        testScheduler.triggerActions();
        then(mView).should().showLoading();
        Collections.sort(notesBoArrayList, (fstr, sstr) -> sstr.getModifiedDate().compareToIgnoreCase(fstr.getModifiedDate()));
        then((NotesContract.NotesListView) mView).should().updateNotesList(notesBoArrayList,false);
        then(mView).should().hideLoading();
    }


    @Test
    public void testFetchDataFailed() {
        NotesTestDataFactory.retailerMasterBO.setRetailerID("1");
//        given(mockAppDataProvider.getRetailMaster()).willReturn(NotesTestDataFactory.retailerMasterBO);
        ArrayList<NotesBo> notesBoArrayList = null;
        given(notesDataManager.fetchNotesData("1", true))
                .willReturn(Observable.fromCallable(new Callable<ArrayList<NotesBo>>() {
                    @Override
                    public ArrayList<NotesBo> call() throws Exception {
                        return notesBoArrayList;
                    }
                }));

        mPresenter.fetchNotesData("1", true, 0, false);
        testScheduler.triggerActions();
        then(mView).should().showLoading();
        then(mView).should().hideLoading();
        then(mView).should().onErrorMsg();


    }

    @Test
    public void testSearchByMonthOrDateWiseSuccess() {
        ArrayList<NotesBo> searchNotesBos = NotesTestDataFactory.getMonthList();
        mPresenter.searchByMonthOrDateWise(NotesTestDataFactory.fromPeriod, NotesTestDataFactory.toPeriod, searchNotesBos);
        testScheduler.triggerActions();
        then((NotesContract.NotesListView)mView).should().updateNotesList(searchNotesBos,false);
    }

    @Test
    public void testSearchByMonthOrDateWiseFailed() {
        ArrayList<NotesBo> searchNotesBos = new ArrayList<>();
        mPresenter.searchByMonthOrDateWise(NotesTestDataFactory.fromPeriod, NotesTestDataFactory.toPeriod, searchNotesBos);
        testScheduler.triggerActions();
        then((NotesContract.NotesListView) mView).should().showDateSelectionErrorMsg();
    }


    @Test
    public void testGetRetailerId() {
        NotesTestDataFactory.retailerMasterBO.setRetailerID("1");
        given(mockAppDataProvider.getRetailMaster()).willReturn(NotesTestDataFactory.retailerMasterBO);
        assertEquals(mPresenter.getRetailerID(), "1");
    }

    @Test
    public void testGetRetailerIdNull() {
        given(mockAppDataProvider.getRetailMaster()).willReturn(null);
        assertEquals(mPresenter.getRetailerID(), "0");
    }

    @Test
    public void testGetUserId() {
        NotesTestDataFactory.userMasterBO.setUserid(2);
        given(mockAppDataProvider.getUser()).willReturn(NotesTestDataFactory.userMasterBO);
        assertEquals(mPresenter.getUserID(), 2);
    }

    @Test
    public void testGetUserName() {
        NotesTestDataFactory.userMasterBO.setUserName("abc");
        given(mockAppDataProvider.getUser()).willReturn(NotesTestDataFactory.userMasterBO);
        assertEquals(mPresenter.getUserName(), "abc");
    }

    @Test
    public void testValidateSuccess() {

        NotesTestDataFactory.retailerMasterBO.setRetailerID("1");
        given(mockAppDataProvider.getRetailMaster()).willReturn(NotesTestDataFactory.retailerMasterBO);
        assertEquals(mPresenter.getRetailerID(), "1");

        mPresenter.validateData("1", "abc", "1");
        testScheduler.triggerActions();
        then(mView).shouldHaveZeroInteractions();
    }


    @Test
    public void testValidateTitleFailed() {
        NotesTestDataFactory.retailerMasterBO.setRetailerID("1");
        given(mockAppDataProvider.getRetailMaster()).willReturn(NotesTestDataFactory.retailerMasterBO);
        assertEquals(mPresenter.getRetailerID(), "1");
        mPresenter.validateData("", NotesTestDataFactory.noteDesc, "1");
        testScheduler.triggerActions();
        then((NotesContract.NoteCreationView) mView).should().showNoteTitleError();

    }

    @Test
    public void testValidateDescFailed() {
        NotesTestDataFactory.retailerMasterBO.setRetailerID("1");
        given(mockAppDataProvider.getRetailMaster()).willReturn(NotesTestDataFactory.retailerMasterBO);
        assertEquals(mPresenter.getRetailerID(), "1");
        mPresenter.validateData(NotesTestDataFactory.noteTitle, "", "1");
        testScheduler.triggerActions();
        then((NotesContract.NoteCreationView) mView).should().showNoteDescError();

    }

    @Test
    public void testValidateRetailerIdFailed() {
        NotesTestDataFactory.retailerMasterBO.setRetailerID("0");
        given(mockAppDataProvider.getRetailMaster()).willReturn(NotesTestDataFactory.retailerMasterBO);
        assertEquals(mPresenter.getRetailerID(), "0");
        mPresenter.validateData(NotesTestDataFactory.noteTitle, NotesTestDataFactory.noteDesc, "0");
        testScheduler.triggerActions();
        then((NotesContract.NoteCreationView) mView).should().showRetailerSelectionError();

    }


    @Test
    public void testNotesDataSavedSuccess() {
        NotesTestDataFactory.userMasterBO.setUserName("abc");
        NotesTestDataFactory.userMasterBO.setUserid(123);
        given(mockAppDataProvider.getUser()).willReturn(NotesTestDataFactory.userMasterBO);

        given(mockAppDataProvider.getRetailMaster()).willReturn(NotesTestDataFactory.retailerMasterBO);

        NotesBo notesBo = NotesTestDataFactory.getNoteBo();
        given(notesDataManager.addAndUpdateNote(mockAppDataProvider.getRetailMaster().getRetailerID(), notesBo, "abc", 123, false)).willReturn(Single.just(true));

        mPresenter.createNewNotes(mockAppDataProvider.getRetailMaster().getRetailerID(), notesBo, false);
        testScheduler.triggerActions();

        then(mView).should().showLoading();
        then(mView).should().hideLoading();
        then((NotesContract.NoteCreationView) mView).should().onSaveSuccess();
    }

    @Test
    public void testSaveDataUpdateModuleTimeFail() {
        NotesTestDataFactory.userMasterBO.setUserName("abc");
        NotesTestDataFactory.userMasterBO.setUserid(123);
        given(mockAppDataProvider.getUser()).willReturn(NotesTestDataFactory.userMasterBO);
        assertEquals(mPresenter.getUserName(), "abc");
        assertEquals(mPresenter.getUserID(), 123);

        given(mockAppDataProvider.getRetailMaster()).willReturn(NotesTestDataFactory.retailerMasterBO);

        NotesBo notesBo = new NotesBo();
        given(notesDataManager.addAndUpdateNote(mockAppDataProvider.getRetailMaster().getRetailerID(), notesBo, "abc", 123, false)).willReturn(Single.just(false));

        mPresenter.createNewNotes(mockAppDataProvider.getRetailMaster().getRetailerID(), notesBo, false);
        testScheduler.triggerActions();
        then(mView).should().showLoading();
        then(mView).should().hideLoading();

    }


    @Test
    public void deleteNote() {
        given(notesDataManager.deleteNote("123", "12345", "5")).willReturn(Single.just(true));
        mPresenter.deleteNote("123", "12345", "5");
        testScheduler.triggerActions();
        then(mView).should().showLoading();
        then(mView).should().hideLoading();
        then(mView).should().onDeleteSuccess();
    }

    @Test
    public void testUpdateModuleTime() {
        given(mockOutletTimeStampDataManager.updateTimeStampModuleWise(DateTimeUtils.now(DateTimeUtils.TIME))).willReturn(Single.just(true));
        mPresenter.updateModuleTime();
        testScheduler.triggerActions();
        then(mView).shouldHaveZeroInteractions();
    }

    @Test
    public void testSaveModuleCompletion() {
        given(mDataManager.saveModuleCompletion(HomeScreenTwo.MENU_RTR_NOTES)).willReturn(Single.just(true));
        mPresenter.saveModuleCompletion(HomeScreenTwo.MENU_RTR_NOTES);
        testScheduler.triggerActions();
        then(mView).shouldHaveZeroInteractions();
    }

    @Test
    public void testFetchMinMaxDate() {
        String minMaxDateStr = "12/02/2019";
        given(notesDataManager.getMinMaxDate("123", true)).willReturn(Single.just(minMaxDateStr));
        mPresenter.getMinMaxDate("123", true);
        testScheduler.triggerActions();
        then((NotesContract.NotesListView) mView).should().updateMinMaxDate(minMaxDateStr);
    }


    @After
    public void tearDown() {
        mPresenter.onDetach();
    }


}
