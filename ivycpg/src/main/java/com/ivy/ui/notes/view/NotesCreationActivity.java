package com.ivy.ui.notes.view;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.AppCompatEditText;
import android.support.v7.widget.AppCompatSpinner;
import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.Toolbar;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;

import com.ivy.core.base.presenter.BasePresenter;
import com.ivy.core.base.view.BaseActivity;
import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.bo.RetailerMasterBO;
import com.ivy.sd.png.commons.SDUtil;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.util.CommonDialog;
import com.ivy.sd.png.view.DataPickerDialogFragment;
import com.ivy.ui.notes.NoteConstant;
import com.ivy.ui.notes.NotesContract;
import com.ivy.ui.notes.di.DaggerNotesComponent;
import com.ivy.ui.notes.di.NotesModule;
import com.ivy.ui.notes.model.NotesBo;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Objects;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.OnItemSelected;

public class NotesCreationActivity extends BaseActivity implements NotesContract.NoteCreationView, DataPickerDialogFragment.UpdateDateInterface {


    private boolean isFromHomeScreen;
    private String screenTitle;
    private String menuCode;
    private NotesBo notesBo;
    private int mSelectedSpinnerPos = 0;

    @BindView(R.id.toolbar)
    Toolbar toolbar;

    @BindView(R.id.note_title_label_tv)
    AppCompatTextView titleLabelTv;

    @BindView(R.id.note_title_val)
    AppCompatEditText titleValEditText;

    @BindView(R.id.note_spinner_label_tv)
    AppCompatTextView retailerSelectionLabelTv;

    @BindView(R.id.retailer_selection_sp)
    AppCompatSpinner retailerSpinner;

    @BindView(R.id.note_desc_label_tv)
    AppCompatTextView descLabelTv;

    @BindView(R.id.note_desc_val)
    AppCompatEditText descValEditText;

    @BindView(R.id.btn_notes_add)
    Button addNoteBtn;

    private ArrayAdapter<RetailerMasterBO> retailerMasterArrayAdapter;

    @Inject
    NotesContract.NotesPresenter<NotesContract.NotesView> mPresenter;

    @Override
    public int getLayoutId() {
        return R.layout.activity_notes_creation;
    }

    @Override
    protected void initVariables() {

    }

    @Override
    public void initializeDi() {

        DaggerNotesComponent.builder().ivyAppComponent(((BusinessModel) this.getApplication()).getComponent())
                .notesModule(new NotesModule(this))
                .build()
                .inject(this);

        setBasePresenter((BasePresenter) mPresenter);
    }


    @Override
    protected void getMessageFromAliens() {
        Bundle bundle;
        bundle = getIntent().getExtras();

        if (bundle != null) {

            if (bundle.containsKey(NoteConstant.FROM_HOME_SCREEN))
                isFromHomeScreen = true;

            if (bundle.containsKey(NoteConstant.MENU_CODE))
                menuCode = bundle.getString(NoteConstant.MENU_CODE, "");

            if (bundle.containsKey(NoteConstant.SCREEN_TITLE))
                screenTitle = bundle.getString(NoteConstant.SCREEN_TITLE, getString(R.string.task));

            if (bundle.containsKey(NoteConstant.NOTE_OBJECT))
                notesBo = bundle.getParcelable(NoteConstant.NOTE_OBJECT);

        }
    }

    @Override
    protected void setUpViews() {
        overridePendingTransition(R.anim.trans_left_in, R.anim.trans_left_out);
        setUpToolBar();
        if (isFromHomeScreen) {
            showRetailerSelection();
            setUpRetailerAdapter();
            mPresenter.fetchRetailerNames();
        }
    }

    @OnItemSelected(R.id.retailer_selection_sp)
    public void onRetailerItemSelect(AppCompatSpinner spinner, int position) {
        TextView itemTextView = spinner.getSelectedView().findViewById(android.R.id.text1);
        itemTextView.setGravity(Gravity.START);
        itemTextView.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.font_small));
        mSelectedSpinnerPos = position;
    }


    private void setUpToolBar() {
        setSupportActionBar(toolbar);
        setUpToolbar(screenTitle);
    }


    private void setNotesData() {
        titleValEditText.setText(notesBo.getNotesTitle());
        descValEditText.setText(notesBo.getNotesDesc());
        if (isFromHomeScreen)
            retailerSpinner.setSelection(getSelectedRetailerPosition(retailerMasterArrayAdapter.getCount()));


    }

    private int getSelectedRetailerPosition(int retailerCount) {
        int i = 0;
        while (i < retailerCount) {
            if (Objects.requireNonNull(retailerMasterArrayAdapter.getItem(i)).getRetailerId()
                    == SDUtil.convertToInt(notesBo.getRetailerId()))
                return i;
            i++;
        }
        return -1;
    }

    private void showRetailerSelection() {
        retailerSelectionLabelTv.setVisibility(View.VISIBLE);
        retailerSpinner.setVisibility(View.VISIBLE);

    }

    private void setUpRetailerAdapter() {
        retailerMasterArrayAdapter = new ArrayAdapter<>(this,
                R.layout.spinner_blacktext_layout);
        retailerMasterArrayAdapter.setDropDownViewResource(R.layout.spinner_blacktext_list_item);
    }

    @OnClick(R.id.btn_notes_add)
    public void onSaveBtnClick() {
        String selectedRid;
        boolean isEditMode = false;

        if (retailerMasterArrayAdapter != null
                && retailerMasterArrayAdapter.getCount() > 0)
            selectedRid = String.valueOf(Objects.requireNonNull(retailerMasterArrayAdapter.getItem(mSelectedSpinnerPos)).getRetailerId());
        else
            selectedRid = mPresenter.getRetailerID();

        if (!mPresenter.validateData(titleValEditText.getText().toString(), descValEditText.getText().toString(), selectedRid))
            return;

        if (notesBo == null)
            notesBo = new NotesBo();
        else
            isEditMode = true;


        notesBo.setNotesTitle(titleValEditText.getText().toString());
        notesBo.setNotesDesc(descValEditText.getText().toString());
        notesBo.setRetailerId(selectedRid);

        mPresenter.createNewNotes(selectedRid
                , notesBo, isEditMode);
    }

    @Override
    public void onErrorMsg() {
        onError(R.string.something_went_wrong);
    }

    @Override
    public void onDeleteSuccess() {

    }

    @Override
    public void updateLabelNames(HashMap<String, String> labelMap) {
        if (labelMap.containsKey(NoteConstant.NOTE_TITLE_LABEL))
            titleLabelTv.setText(labelMap.get(NoteConstant.NOTE_TITLE_LABEL));

        if (labelMap.containsKey(NoteConstant.NOTE_DESC_LABEL))
            descLabelTv.setText(labelMap.get(NoteConstant.NOTE_DESC_LABEL));

        if (labelMap.containsKey(NoteConstant.NOTE_ASSIGN_TO_RETAILER_LABEL))
            retailerSelectionLabelTv.setText(labelMap.get(NoteConstant.NOTE_ASSIGN_TO_RETAILER_LABEL));

        if (labelMap.containsKey(NoteConstant.NOTE_TYPE_HERE_HINT_LABEL)) {
            titleValEditText.setHint(labelMap.get(NoteConstant.NOTE_TYPE_HERE_HINT_LABEL));
            descValEditText.setHint(labelMap.get(NoteConstant.NOTE_TYPE_HERE_HINT_LABEL));
        }
    }


    @Override
    public void updateDate(Date date, String tag) {

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int i = item.getItemId();

        if (i == android.R.id.home) {
            finish();
            overridePendingTransition(R.anim.trans_right_in, R.anim.trans_right_out);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void onHomeButtonClick() {
        if (notesBo != null) {
            Intent i = new Intent(NotesCreationActivity.this, NoteDetailActivity.class);
            setResult(NoteConstant.FINISH_REQUEST, i);
        }
        finish();
        overridePendingTransition(R.anim.trans_right_in, R.anim.trans_right_out);

    }

    @Override
    public void setRetailerListData(ArrayList<RetailerMasterBO> retailerList) {
        retailerMasterArrayAdapter.clear();
        retailerMasterArrayAdapter.add(new RetailerMasterBO(0, getString(R.string.plain_select)));
        retailerMasterArrayAdapter.addAll(retailerList);
        retailerMasterArrayAdapter.notifyDataSetChanged();
        retailerSpinner.setAdapter(retailerMasterArrayAdapter);

        if (notesBo != null)
            setNotesData();
    }

    @Override
    public void showNoteTitleError() {
        showMessage(R.string.enter_title);
    }

    @Override
    public void showNoteDescError() {
        showMessage(R.string.enter_desc);
    }

    @Override
    public void showRetailerSelectionError() {
        onError(getString(R.string.retailer_selection_mandatory));
    }

    @Override
    public void onSaveSuccess() {

        mPresenter.saveModuleCompletion(menuCode);
        showAlert("", getString(R.string.saved_successfully), new CommonDialog.PositiveClickListener() {
            @Override
            public void onPositiveButtonClick() {
                onHomeButtonClick();
            }
        });
    }

}
