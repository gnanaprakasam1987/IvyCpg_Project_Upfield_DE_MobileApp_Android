package com.ivy.ui.notes.view;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.ivy.core.base.presenter.BasePresenter;
import com.ivy.core.base.view.BaseActivity;
import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.ui.notes.NoteConstant;
import com.ivy.ui.notes.NotesContract;
import com.ivy.ui.notes.di.DaggerNotesComponent;
import com.ivy.ui.notes.di.NotesModule;
import com.ivy.ui.notes.model.NotesBo;

import java.util.HashMap;
import java.util.Objects;

import javax.inject.Inject;

import butterknife.BindView;

public class NoteDetailActivity extends BaseActivity implements NotesContract.NotesView {

    private boolean isFromHomeSrc;
    private String menuCode;
    private String screenTitle;
    private NotesBo noteObj;

    @BindView(R.id.toolbar)
    Toolbar toolbar;

    @BindView(R.id.note_title_label_tv)
    AppCompatTextView noteTitleLabelTv;

    @BindView(R.id.note_title__value_tv)
    AppCompatTextView noteTitleTv;

    @BindView(R.id.note_desc_label_tv)
    AppCompatTextView noteDescLabelTv;

    @BindView(R.id.note_desc_value_tv)
    AppCompatTextView noteDescTv;

    @BindView(R.id.created_by_tv)
    AppCompatTextView createdByLabelTv;

    @BindView(R.id.created_by_value_tv)
    AppCompatTextView cratedByTv;

    @BindView(R.id.created_date_tv)
    AppCompatTextView createdDateLabelTv;

    @BindView(R.id.created_date_value_tv)
    AppCompatTextView createdDateTv;

    @BindView(R.id.modified_by_tv)
    AppCompatTextView modifiedByLabelTv;

    @BindView(R.id.modified_by_value_tv)
    AppCompatTextView modifiedByTv;

    @BindView(R.id.modified_date_tv)
    AppCompatTextView modifiedDateLabelTv;

    @BindView(R.id.modified_date_value_tv)
    AppCompatTextView modifiedDateTv;

    @BindView(R.id.modified_divider)
    View modifiedDivider;

    @Inject
    NotesContract.NotesPresenter<NotesContract.NotesView> mPresenter;


    @Override
    public int getLayoutId() {
        return R.layout.activity_note_detail;
    }

    @Override
    protected void initVariables() {

    }

    @Override
    public void initializeDi() {
        DaggerNotesComponent.builder().ivyAppComponent(((BusinessModel) Objects.requireNonNull(this).getApplication()).getComponent())
                .notesModule(new NotesModule(this))
                .build()
                .inject(this);

        setBasePresenter((BasePresenter) mPresenter);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    protected void getMessageFromAliens() {

    }

    @Override
    protected void setUpViews() {
        overridePendingTransition(R.anim.trans_left_in, R.anim.trans_left_out);

        if (getIntent().getExtras() != null) {
            noteObj = getIntent().getExtras().getParcelable(NoteConstant.NOTE_OBJECT);
            screenTitle = getIntent().getExtras().getString(NoteConstant.SCREEN_TITLE, getString(R.string.note_details));
            menuCode = getIntent().getExtras().getString(NoteConstant.MENU_CODE, "MENU_NOTES");
            isFromHomeSrc = getIntent().getExtras().getBoolean(NoteConstant.FROM_HOME_SCREEN, false);

            setUpToolBar("@" + Objects.requireNonNull(noteObj).getRetailerName());
            setNotesData(noteObj);
        }
    }

    private void setUpToolBar(String titleName) {
        setSupportActionBar(toolbar);
        setUpToolbar(titleName);
    }


    private void setNotesData(NotesBo notesObj) {

        if (notesObj.getModifiedDate().isEmpty()
                && notesObj.getModifiedBy().isEmpty()) {
            handleModifiedVisibility(View.GONE);
        } else {
            handleModifiedVisibility(View.VISIBLE);
            modifiedByTv.setText(notesObj.getModifiedBy());
            modifiedDateTv.setText(notesObj.getModifiedDate());
        }

        noteTitleTv.setText(notesObj.getNotesTitle());
        noteDescTv.setText(notesObj.getNotesDesc());
        cratedByTv.setText(notesObj.getCreatedBy());
        createdDateTv.setText(notesObj.getCreatedDate());


    }

    private void handleModifiedVisibility(int visibility) {
        modifiedByLabelTv.setVisibility(visibility);
        modifiedByTv.setVisibility(visibility);
        modifiedDateLabelTv.setVisibility(visibility);
        modifiedDateTv.setVisibility(visibility);
        modifiedDivider.setVisibility(visibility);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_edit_delete, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            backNavigation();
            return true;
        } else if (id == R.id.menu_edit_note) {
            navigateToNoteCreation();
            return true;
        } else if (id == R.id.menu_delete_note) {
            showDeleteAlert();
            return true;
        }
        return false;
    }


    private void navigateToNoteCreation() {
        if (noteObj.getUserId() == mPresenter.getUserID()
                || mPresenter.isNoteEditByOtherUser()) {

            Intent i = new Intent(NoteDetailActivity.this, NotesCreationActivity.class);
            i.putExtra(NoteConstant.FROM_HOME_SCREEN, isFromHomeSrc);
            i.putExtra(NoteConstant.MENU_CODE, menuCode);
            i.putExtra(NoteConstant.SCREEN_TITLE, screenTitle);
            i.putExtra(NoteConstant.NOTE_OBJECT, noteObj);
            i.putExtra(NoteConstant.FROM_DETAIL_SRC, true);
            startActivityForResult(i, NoteConstant.FINISH_REQUEST);
        } else {
            showMessage(R.string.retailer_note_can_not_be_edit);
        }
    }

    private void backNavigation() {
        finish();
        overridePendingTransition(R.anim.trans_right_in, R.anim.trans_right_out);
    }

    @Override
    public void onErrorMsg() {
        onError(R.string.something_went_wrong);
    }

    @Override
    public void onDeleteSuccess() {
        showAlert("", getString(R.string.deleted_sucessfully), () -> backNavigation());

    }

    @Override
    public void updateLabelNames(HashMap<String, String> labelMap) {
        if (labelMap.containsKey(NoteConstant.NOTE_TITLE_LABEL))
            noteTitleLabelTv.setText(labelMap.get(NoteConstant.NOTE_TITLE_LABEL));

        if (labelMap.containsKey(NoteConstant.NOTE_DESC_LABEL))
            noteDescLabelTv.setText(labelMap.get(NoteConstant.NOTE_DESC_LABEL));

        if (labelMap.containsKey(NoteConstant.NOTE_CREATED_BY_LABEL))
            createdByLabelTv.setText(labelMap.get(NoteConstant.NOTE_CREATED_BY_LABEL));

        if (labelMap.containsKey(NoteConstant.NOTE_CREATED_DATE_LABEL))
            createdDateLabelTv.setText(labelMap.get(NoteConstant.NOTE_CREATED_DATE_LABEL));

        if (labelMap.containsKey(NoteConstant.NOTE_MODIFIED_BY_LABEL))
            modifiedByLabelTv.setText(labelMap.get(NoteConstant.NOTE_MODIFIED_BY_LABEL));

        if (labelMap.containsKey(NoteConstant.NOTE_MODIFIED_DATE_LABEL))
            modifiedDateLabelTv.setText(labelMap.get(NoteConstant.NOTE_MODIFIED_DATE_LABEL));
    }

    private void showDeleteAlert() {
        showAlert("", getString(R.string.do_you_want_to_delete_the_note),
                () -> mPresenter.deleteNote(noteObj.getRetailerId(), noteObj.getTid(), noteObj.getNoteId()),
                () -> {
                });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == NoteConstant.FINISH_REQUEST
                && resultCode == 1)
            finish();
    }
}
