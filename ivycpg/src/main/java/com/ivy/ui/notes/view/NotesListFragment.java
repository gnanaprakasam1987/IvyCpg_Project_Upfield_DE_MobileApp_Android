package com.ivy.ui.notes.view;

import android.content.Intent;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;

import com.ivy.core.base.presenter.BasePresenter;
import com.ivy.core.base.view.BaseFragment;
import com.ivy.lib.view.YearMonthPickerDialog;
import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.commons.SDUtil;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.view.HomeScreenTwo;
import com.ivy.ui.notes.NoteConstant;
import com.ivy.ui.notes.NoteOnclickListener;
import com.ivy.ui.notes.NotesContract;
import com.ivy.ui.notes.adapter.BottomSortListAdapter;
import com.ivy.ui.notes.adapter.NotesAdapter;
import com.ivy.ui.notes.di.DaggerNotesComponent;
import com.ivy.ui.notes.di.NotesModule;
import com.ivy.ui.notes.model.NotesBo;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Objects;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.OnClick;

public class NotesListFragment extends BaseFragment implements NotesContract.NotesListView, NoteOnclickListener {

    private boolean isFromHomeScreen;
    private String screenTitle;
    private String menuCode;
    private int minMonth;
    private int maxMonth;
    private int minYear;
    private int maxYear;
    private TypedArray typeAttr;
    private YearMonthPickerDialog fromMonthPickerDialog;
    private YearMonthPickerDialog toMonthPickerDialog;
    private int lastFilterSelectedPos = -1;
    private boolean isAscendingSort;
    BottomSheetBehavior bottomSheetBehavior;
    private NotesAdapter notesAdapter;
    private ArrayList<NotesBo> noteList = new ArrayList<>();

    @BindView(R.id.note_filter_label_tv)
    AppCompatTextView filterByTv;

    @BindView(R.id.note_from_date_btn)
    Button fromDateBtn;

    @BindView(R.id.note_to_date_btn)
    Button toDateBtn;

    @BindView(R.id.notes_recycler_view)
    RecyclerView notesRecyclerView;

    @BindView(R.id.fab_create_note)
    FloatingActionButton createNoteFabBtn;

    @BindView(R.id.bottomSheetLayout)
    LinearLayout bottomSheetLayout;

    @BindView(R.id.sort_recycler_view)
    RecyclerView bottomRecyclerView;

    @BindView(R.id.task_bg_view)
    View bgView;

    SimpleDateFormat dateFormat = new SimpleDateFormat(NoteConstant.MONTH_YEAR_FORMAT, Locale.US);

    @Inject
    NotesContract.NotesPresenter<NotesContract.NotesView> mPresenter;

    @Override
    public void initializeDi() {
        DaggerNotesComponent.builder().ivyAppComponent(((BusinessModel) Objects.requireNonNull(getActivity()).getApplication()).getComponent())
                .notesModule(new NotesModule(this))
                .build()
                .inject(this);

        setBasePresenter((BasePresenter) mPresenter);
    }

    @Override
    protected int setContentViewLayout() {
        return R.layout.notes_fragment;
    }

    @Override
    public void init(View view) {

        bottomSheetBehavior = BottomSheetBehavior.from(bottomSheetLayout);
        bottomSheetBehavior.setHideable(false);
        typeAttr = Objects.requireNonNull(getContext()).getTheme().obtainStyledAttributes(R.styleable.MyTextView);
    }


    @Override
    protected void getMessageFromAliens() {
        Bundle bundle = getArguments();
        if (bundle == null)
            bundle = Objects.requireNonNull(getActivity()).getIntent().getExtras();

        if (bundle != null) {

            if (bundle.containsKey(NoteConstant.FROM_HOME_SCREEN))
                isFromHomeScreen = true;

            if (bundle.containsKey(NoteConstant.MENU_CODE))
                menuCode = bundle.getString(NoteConstant.MENU_CODE, "");

            if (bundle.containsKey(NoteConstant.SCREEN_TITLE))
                screenTitle = bundle.getString(NoteConstant.SCREEN_TITLE, getString(R.string.note_label));
        }
    }

    @Override
    protected void setUpViews() {
        setUpToolbar(screenTitle);
        setHasOptionsMenu(true);
        setUpRecyclerView();
        setUpBottomSheet();
    }


    @Override
    public void onResume() {
        super.onResume();
        mPresenter.getMinMaxDate(mPresenter.getRetailerID(), isFromHomeScreen);
        mPresenter.fetchNotesData(mPresenter.getRetailerID(), isFromHomeScreen, lastFilterSelectedPos, isAscendingSort);
    }

    @OnClick(R.id.fab_create_note)
    public void onCreateClick() {
        navigateToNotesCreationActivity();
    }

    @OnClick(R.id.note_from_date_btn)
    public void fromDateOnClick() {

        if (fromMonthPickerDialog == null)
            createFromMonthPicker();

        fromMonthPickerDialog.show();
    }

    @OnClick(R.id.note_to_date_btn)
    public void toDateOnClick() {

        if (toMonthPickerDialog == null)
            createToMonthPicker();

        toMonthPickerDialog.show();
    }

    @OnClick(R.id.task_bg_view)
    public void onBgClick() {
        hideBottomSheet();
    }

    private void setUpBottomSheet() {

        bottomSheetBehavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                switch (newState) {
                    case BottomSheetBehavior.STATE_COLLAPSED:
                        if (notesRecyclerView.getVisibility() == View.VISIBLE) {
                            hideBottomSheet();
                        }
                        break;
                    case BottomSheetBehavior.STATE_DRAGGING:
                        break;
                    case BottomSheetBehavior.STATE_EXPANDED:
                        break;
                    case BottomSheetBehavior.STATE_HIDDEN:
                        bgView.setVisibility(View.GONE);
                        break;
                    case BottomSheetBehavior.STATE_SETTLING:
                        break;
                }
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {
            }
        });

        bottomRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        bottomRecyclerView.setItemAnimator(new DefaultItemAnimator());
        bottomRecyclerView.setHasFixedSize(false);
        bottomRecyclerView.setNestedScrollingEnabled(false);
        bottomRecyclerView.addItemDecoration(new DividerItemDecoration(Objects.requireNonNull(getActivity()), DividerItemDecoration.VERTICAL));
        bottomRecyclerView.setAdapter(new BottomSortListAdapter(getActivity(), getResources().getStringArray(R.array.note_sort_list), this, lastFilterSelectedPos, typeAttr.getColor(R.styleable.MyTextView_primarycolor, 0)));
    }


    private void hideBottomSheet() {
        bottomSheetBehavior.setHideable(true);
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
    }

    @Override
    public void updateNotesList(ArrayList<NotesBo> notesArrayList, boolean isFromSort) {
        if (!isFromSort) {
            noteList.clear();
            noteList.addAll(notesArrayList);
        }
        notesAdapter.notifyDataSetChanged();

        if (bottomSheetBehavior.getState() == BottomSheetBehavior.STATE_EXPANDED)
            hideBottomSheet();
    }

    @Override
    public void onErrorMsg() {
        showMessage(Objects.requireNonNull(getActivity()).getString(R.string.something_went_wrong));
    }

    @Override
    public void updateMinMaxDate(String minMaxDate) {
        if (!minMaxDate.isEmpty()) {
            String[] minMaxDateSplitArray = minMaxDate.split(",");
            String[] dateSplitArray = minMaxDateSplitArray[0].split("/");

            minYear = SDUtil.convertToInt(dateSplitArray[0]);
            minMonth = SDUtil.convertToInt(dateSplitArray[1]) - 1;

            dateSplitArray = minMaxDateSplitArray[1].split("/");
            maxYear = SDUtil.convertToInt(dateSplitArray[0]);
            maxMonth = SDUtil.convertToInt(dateSplitArray[1]) - 1;

            Calendar calendarMin = Calendar.getInstance();
            calendarMin.set(Calendar.YEAR, minYear == 0 ? Calendar.YEAR : minYear);
            calendarMin.set(Calendar.MONTH, minMonth);

            Calendar calendarMax = Calendar.getInstance();
            calendarMax.set(Calendar.YEAR, maxYear == 0 ? Calendar.YEAR : maxYear);
            calendarMax.set(Calendar.MONTH, maxMonth);

            fromDateBtn.setText(dateFormat.format(calendarMin.getTime()));
            toDateBtn.setText(dateFormat.format(calendarMax.getTime()));
        }
    }

    @Override
    public void showDateSelectionErrorMsg() {
        showMessage(R.string.between_range_not_avail);
    }

    @Override
    public void onDeleteSuccess() {

        showMessage(R.string.deleted_sucessfully);
    }

    @Override
    public void updateLabelNames(HashMap<String, String> labelMap) {

    }

    private void setUpRecyclerView() {
        notesRecyclerView.setHasFixedSize(true);
        notesRecyclerView.setNestedScrollingEnabled(false);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        layoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        notesRecyclerView.setLayoutManager(layoutManager);
        notesRecyclerView.addItemDecoration(new DividerItemDecoration(Objects.requireNonNull(getActivity()), DividerItemDecoration.VERTICAL));
        notesAdapter = new NotesAdapter(getActivity(), noteList, isFromHomeScreen, this);
        notesRecyclerView.setAdapter(notesAdapter);
    }


    private void createFromMonthPicker() {

        Calendar calendar = Calendar.getInstance();
        calendar.set(1, minMonth, minYear);
        fromMonthPickerDialog = new YearMonthPickerDialog(getActivity(),
                (year, month) -> {
                    Calendar calendar1 = Calendar.getInstance();
                    calendar1.set(Calendar.YEAR, year);
                    calendar1.set(Calendar.MONTH, month);


                    fromDateBtn.setText(dateFormat.format(calendar1.getTime()));

                    if (!fromDateBtn.getText().toString().isEmpty()
                            && !toDateBtn.getText().toString().isEmpty())
                        mPresenter.searchByMonthOrDateWise(fromDateBtn.getText().toString(), toDateBtn.getText().toString(), mPresenter.getNoteList());
                    else
                        showMessage(R.string.month_selection_mandatory);

                }, R.style.DialogStyleBlue, typeAttr.getColor(R.styleable.MyTextView_primarycolor, 0), calendar);

        fromMonthPickerDialog.setMinYear(minYear);
        fromMonthPickerDialog.setMaxYear(maxYear);
    }

    private void createToMonthPicker() {

        Calendar calendar = Calendar.getInstance();
        calendar.set(1, maxMonth, maxYear);
        toMonthPickerDialog = new YearMonthPickerDialog(getActivity(),
                (year, month) -> {
                    Calendar calendar1 = Calendar.getInstance();
                    calendar1.set(Calendar.YEAR, year);
                    calendar1.set(Calendar.MONTH, month);

                    toDateBtn.setText(dateFormat.format(calendar1.getTime()));

                    if (!fromDateBtn.getText().toString().isEmpty()
                            && !toDateBtn.getText().toString().isEmpty())
                        mPresenter.searchByMonthOrDateWise(fromDateBtn.getText().toString(), toDateBtn.getText().toString(), mPresenter.getNoteList());
                    else
                        showMessage(R.string.month_selection_mandatory);
                }, R.style.DialogStyleBlue, typeAttr.getColor(R.styleable.MyTextView_primarycolor, 0), calendar);

        toMonthPickerDialog.setMinYear(minYear);
        toMonthPickerDialog.setMaxYear(maxYear);
    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_notes, menu);

    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int i1 = item.getItemId();
        if (i1 == android.R.id.home) {
            onBackPressed();
        } else if (i1 == R.id.menu_sort) {
            if (bottomSheetBehavior.getState() == BottomSheetBehavior.STATE_EXPANDED) {
                hideBottomSheet();
            } else {
                bgView.setVisibility(View.VISIBLE);
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
            }

        }
        return super.onOptionsItemSelected(item);
    }

    private void onBackPressed() {
        if (!isFromHomeScreen &&
                (getActivity() != null)) {

            if (!isPreVisit)
                mPresenter.updateModuleTime();

            Intent intent = new Intent(getActivity(),HomeScreenTwo.class);
            if (isPreVisit)
                intent.putExtra("PreVisit",true);
            startActivity(intent);
        }

        Objects.requireNonNull(getActivity()).overridePendingTransition(R.anim.trans_right_in, R.anim.trans_right_out);
        getActivity().finish();
    }

    private void navigateToNotesCreationActivity() {

        Intent i = new Intent(getActivity(), NotesCreationActivity.class);
        i.putExtra(NoteConstant.FROM_HOME_SCREEN, isFromHomeScreen);
        i.putExtra(NoteConstant.MENU_CODE, menuCode);
        i.putExtra(NoteConstant.SCREEN_TITLE, screenTitle);
        startActivity(i);
    }

    @Override
    public void onClickEditNote(NotesBo noteBo) {

        if (noteBo.getUserId() == mPresenter.getUserID()
                || mPresenter.isNoteEditByOtherUser()) {

            Intent i = new Intent(getActivity(), NotesCreationActivity.class);
            i.putExtra(NoteConstant.FROM_HOME_SCREEN, isFromHomeScreen);
            i.putExtra(NoteConstant.MENU_CODE, menuCode);
            i.putExtra(NoteConstant.SCREEN_TITLE, screenTitle);
            i.putExtra(NoteConstant.FROM_EDIT_MODE, true);
            i.putExtra(NoteConstant.NOTE_OBJECT, noteBo);
            startActivity(i);

        } else {
            showMessage(R.string.retailer_note_can_not_be_edit);
        }
    }

    @Override
    public void onClickDeleteNote(NotesBo noteBo) {

        if (noteBo.getUserId() == mPresenter.getUserID())
            mPresenter.deleteNote(noteBo.getRetailerId(), noteBo.getTid(), noteBo.getNoteId());
        else
            showMessage(R.string.retailer_note_can_not_be_delete);
    }

    @Override
    public void onClickDetailView(NotesBo notesBo) {
        Intent detailIntent = new Intent(getActivity(), NoteDetailActivity.class);
        detailIntent.putExtra(NoteConstant.FROM_HOME_SCREEN, isFromHomeScreen);
        detailIntent.putExtra(NoteConstant.MENU_CODE, menuCode);
        detailIntent.putExtra(NoteConstant.SCREEN_TITLE, screenTitle);
        detailIntent.putExtra(NoteConstant.NOTE_OBJECT, notesBo);
        startActivity(detailIntent);
    }

    @Override
    public void onSortClick(int orderBy, boolean sortBy) {
        lastFilterSelectedPos = orderBy;
        isAscendingSort = sortBy;
        mPresenter.orderBySortList(orderBy, sortBy, noteList, true);
        hideBottomSheet();
    }

}

