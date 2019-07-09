package com.ivy.ui.notes.presenter;

import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.OnLifecycleEvent;

import com.ivy.core.base.presenter.BasePresenter;
import com.ivy.core.data.app.AppDataProvider;
import com.ivy.core.data.datamanager.DataManager;
import com.ivy.core.data.label.LabelsDataManager;
import com.ivy.core.data.outlettime.OutletTimeStampDataManager;
import com.ivy.core.di.scope.LabelMasterInfo;
import com.ivy.core.di.scope.OutletTimeStampInfo;
import com.ivy.sd.png.bo.RetailerMasterBO;
import com.ivy.sd.png.provider.ConfigurationMasterHelper;
import com.ivy.ui.notes.NoteConstant;
import com.ivy.ui.notes.NotesContract;
import com.ivy.ui.notes.data.NotesDataManager;
import com.ivy.ui.notes.model.NotesBo;
import com.ivy.utils.DateTimeUtils;
import com.ivy.utils.rx.SchedulerProvider;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;

import javax.inject.Inject;

import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.observers.DisposableObserver;

public class NotesPresenterImpl<V extends NotesContract.NotesView> extends BasePresenter<V> implements NotesContract.NotesPresenter<V>, LifecycleObserver {

    private OutletTimeStampDataManager mOutletTimeStampDataManager;

    private ConfigurationMasterHelper mConfigurationMasterHelper;

    private AppDataProvider appDataProvider;

    private NotesDataManager mNotesDataManager;

    private LabelsDataManager mLabelsDataManager;

    private ArrayList<NotesBo> notesArrayList = new ArrayList<>();


    @Inject
    public NotesPresenterImpl(DataManager dataManager, SchedulerProvider schedulerProvider,
                              CompositeDisposable compositeDisposable,
                              ConfigurationMasterHelper configurationMasterHelper,
                              V view, @OutletTimeStampInfo OutletTimeStampDataManager mOutletTimeStampDataManager,
                              AppDataProvider mAppDataProvider, NotesDataManager notesDataManager, @LabelMasterInfo LabelsDataManager labelsDataManager) {

        super(dataManager, schedulerProvider, compositeDisposable, configurationMasterHelper, view);

        this.mConfigurationMasterHelper = configurationMasterHelper;
        this.appDataProvider = mAppDataProvider;
        this.mOutletTimeStampDataManager = mOutletTimeStampDataManager;
        this.mNotesDataManager = notesDataManager;
        this.mLabelsDataManager = labelsDataManager;
    }

    @Override
    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    public void fetchLabels() {
        HashMap<String, String> labelsMap = new HashMap<>();
        getCompositeDisposable().add(mLabelsDataManager.getLabels(NoteConstant.NOTE_TITLE_LABEL, NoteConstant.NOTE_DESC_LABEL,
                NoteConstant.NOTE_CREATED_BY_LABEL, NoteConstant.NOTE_CREATED_DATE_LABEL,
                NoteConstant.NOTE_MODIFIED_BY_LABEL, NoteConstant.NOTE_MODIFIED_DATE_LABEL, NoteConstant.NOTE_TYPE_HERE_HINT_LABEL, NoteConstant.NOTE_ASSIGN_TO_RETAILER_LABEL).subscribeOn(getSchedulerProvider().io())
                .observeOn(getSchedulerProvider().ui())
                .subscribeWith(new DisposableObserver<HashMap<String, String>>() {
                    @Override
                    public void onNext(HashMap<String, String> labels) {

                        labelsMap.clear();
                        labelsMap.putAll(labels);
                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onComplete() {
                        if (!labelsMap.isEmpty())
                            getIvyView().updateLabelNames(labelsMap);
                    }
                }));
    }

    @Override
    public void fetchNotesData(String retailerId, boolean isFromHomeSrc, int lastFilterSelectedPos, boolean sortBy) {
        getIvyView().showLoading();
        notesArrayList = new ArrayList<>();
        getCompositeDisposable().add(mNotesDataManager.
                fetchNotesData(retailerId, isFromHomeSrc)
                .subscribeOn(getSchedulerProvider().io())
                .observeOn(getSchedulerProvider().ui())
                .subscribeWith(new DisposableObserver<ArrayList<NotesBo>>() {

                    @Override
                    public void onNext(ArrayList<NotesBo> notesBoArrayList) {
                        notesArrayList.clear();
                        notesArrayList.addAll(notesBoArrayList);

                    }

                    @Override
                    public void onError(Throwable e) {
                        getIvyView().hideLoading();
                        getIvyView().onErrorMsg();
                    }

                    @Override
                    public void onComplete() {
                        if (lastFilterSelectedPos == -1)
                            ((NotesContract.NotesListView) getIvyView()).updateNotesList(notesArrayList, false);
                        else
                            orderBySortList(lastFilterSelectedPos, sortBy, notesArrayList, false);
                        getIvyView().hideLoading();
                    }
                }));

    }

    @Override
    public void fetchRetailerNames() {
        getIvyView().showLoading();
        ArrayList<RetailerMasterBO> retailerNameArrayList = new ArrayList<>();
        getCompositeDisposable().add(mNotesDataManager.fetchRetailers()
                .subscribeOn(getSchedulerProvider().io())
                .observeOn(getSchedulerProvider().ui())
                .subscribeWith(new DisposableObserver<ArrayList<RetailerMasterBO>>() {
                    @Override
                    public void onNext(ArrayList<RetailerMasterBO> retailerMasterBOS) {
                        retailerNameArrayList.clear();
                        retailerNameArrayList.addAll(retailerMasterBOS);
                    }

                    @Override
                    public void onError(Throwable e) {
                        getIvyView().hideLoading();
                        getIvyView().onErrorMsg();
                    }

                    @Override
                    public void onComplete() {
                        ((NotesContract.NoteCreationView) getIvyView()).setRetailerListData(retailerNameArrayList);
                        getIvyView().hideLoading();
                    }
                }));
    }

    @Override
    public void searchByMonthOrDateWise(String fromPeriod, String toPeriod, ArrayList<NotesBo> searchBoArrayList) {
        ArrayList<NotesBo> searchArrayList = new ArrayList<>();
        for (NotesBo noteObject : searchBoArrayList) {
            if (isBetweenMonthRange(fromPeriod, toPeriod, noteObject.getCreatedDate()))
                searchArrayList.add(noteObject);
        }

        if (!searchArrayList.isEmpty())
            ((NotesContract.NotesListView) getIvyView()).updateNotesList(searchArrayList, false);
        else
            ((NotesContract.NotesListView) getIvyView()).showDateSelectionErrorMsg();
    }


    private boolean isBetweenMonthRange(String fromPeriod, String toPeriod, String compareMonth) {

        Calendar fromCalendar = Calendar.getInstance();
        fromCalendar.setTime(DateTimeUtils.convertStringToDateObject(fromPeriod, NoteConstant.MONTH_YEAR_FORMAT));

        Calendar toCalendar = Calendar.getInstance();
        toCalendar.setTime(DateTimeUtils.convertStringToDateObject(toPeriod, NoteConstant.MONTH_YEAR_FORMAT));

        compareMonth = DateTimeUtils.convertDateTimeObjectToRequestedFormat(compareMonth, DateTimeUtils.DateFormats.SERVER_DATE_FORMAT, NoteConstant.MONTH_YEAR_FORMAT);

        Calendar compareCalendar = Calendar.getInstance();
        compareCalendar.setTime(DateTimeUtils.convertStringToDateObject(compareMonth, NoteConstant.MONTH_YEAR_FORMAT));

        return compareCalendar.getTimeInMillis() >= fromCalendar.getTimeInMillis() && compareCalendar.getTimeInMillis() <= toCalendar.getTimeInMillis();
    }

    @Override
    public void createNewNotes(String retailerId, NotesBo notesBo, boolean isEdit) {
        getIvyView().showLoading();
        getCompositeDisposable().add(mNotesDataManager.addAndUpdateNote(retailerId, notesBo, getUserName(), getUserID(), isEdit)
                .subscribeOn(getSchedulerProvider().io())
                .observeOn(getSchedulerProvider().ui())
                .subscribe(isAdded -> {
                    getIvyView().hideLoading();
                    if (isAdded) {
                        ((NotesContract.NoteCreationView) getIvyView()).onSaveSuccess();
                    }
                }));
    }

    @Override
    public void deleteNote(String retailerId, String tid, String noteId) {
        getIvyView().showLoading();
        getCompositeDisposable().add(mNotesDataManager.deleteNote(retailerId, tid, noteId)
                .subscribeOn(getSchedulerProvider().io())
                .observeOn(getSchedulerProvider().ui())
                .subscribe(isDeleted -> {
                    getIvyView().hideLoading();
                    if (isDeleted)
                        getIvyView().onDeleteSuccess();

                }));
    }

    @Override
    public ArrayList<NotesBo> getNoteList() {
        return notesArrayList;
    }

    @Override
    public int getUserID() {
        return appDataProvider.getUser().getUserid();
    }

    @Override
    public String getRetailerID() {
        return appDataProvider.getRetailMaster() == null ? "0"
                : appDataProvider.getRetailMaster().getRetailerID();
    }

    @Override
    public String getUserName() {
        return appDataProvider.getUser().getUserName();
    }

    @Override
    public String outDateFormat() {
        return mConfigurationMasterHelper.outDateFormat;
    }

    @Override
    public void saveModuleCompletion(String menuCode) {
        getCompositeDisposable().add(getDataManager().saveModuleCompletion(menuCode)
                .subscribeOn(getSchedulerProvider().io())
                .observeOn(getSchedulerProvider().ui())
                .subscribe(value -> {
                }));
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
    public boolean validateData(String noteTitle, String noteDesc, String retailerId) {
        if (noteTitle.equals("")) {
            ((NotesContract.NoteCreationView) getIvyView()).showNoteTitleError();
            return false;
        } else if (noteDesc.equals("")) {
            ((NotesContract.NoteCreationView) getIvyView()).showNoteDescError();
            return false;
        } else if (retailerId != null
                && retailerId.equals("0")) {
            ((NotesContract.NoteCreationView) getIvyView()).showRetailerSelectionError();
            return false;
        }
        return true;
    }

    /**
     * Order By Date ASc or DESC
     *
     * @param sortCategory true - ASC false - DESCfewtch
     */
    @Override
    public void orderBySortList(int sortCategory, boolean isAscending, ArrayList<NotesBo> sortNotesBoArrayList, boolean isFromSort) {
        if (isAscending) {
            Collections.sort(sortNotesBoArrayList, (fstr, sstr) -> {
                if (sortCategory == NoteConstant.CREATED_DATE_ASC)
                    return fstr.getCreatedDate().compareToIgnoreCase(sstr.getCreatedDate());
                else
                    return fstr.getModifiedDate().compareToIgnoreCase(sstr.getModifiedDate());

            });
        } else {
            Collections.sort(sortNotesBoArrayList, (fstr, sstr) -> {
                if (sortCategory == NoteConstant.RECENTLY_UPDATED)
                    return sstr.getTid().compareToIgnoreCase(fstr.getTid());
                else if (sortCategory == NoteConstant.CREATED_DATE_DESC)
                    return sstr.getCreatedDate().compareToIgnoreCase(fstr.getCreatedDate());
                else
                    return sstr.getModifiedDate().compareToIgnoreCase(fstr.getModifiedDate());
            });
        }
        ((NotesContract.NotesListView) getIvyView()).updateNotesList(sortNotesBoArrayList, isFromSort);
    }

    @Override
    public void getMinMaxDate(String retailerId, boolean isFromHomeSrc) {
        getCompositeDisposable().add(mNotesDataManager.getMinMaxDate(retailerId, isFromHomeSrc)
                .subscribeOn(getSchedulerProvider().io())
                .observeOn(getSchedulerProvider().ui())
                .subscribe(minMaxDate -> ((NotesContract.NotesListView) getIvyView()).updateMinMaxDate(minMaxDate)));
    }

    @Override
    public boolean isNoteEditByOtherUser() {
        return mConfigurationMasterHelper.IS_ENABLE_EDIT_OPTION_FOR_OTHER_USER;
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mNotesDataManager.tearDown();
        mOutletTimeStampDataManager.tearDown();
    }
}
