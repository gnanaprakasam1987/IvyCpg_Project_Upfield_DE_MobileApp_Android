package com.ivy.ui.task.view;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.ivy.core.base.presenter.BasePresenter;
import com.ivy.core.base.view.BaseActivity;
import com.ivy.cpg.view.homescreen.HomeScreenActivity;
import com.ivy.sd.camera.CameraActivity;
import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.util.DataMembers;
import com.ivy.ui.task.TaskConstant;
import com.ivy.ui.task.TaskContract;
import com.ivy.ui.task.adapter.TaskImgListAdapter;
import com.ivy.ui.task.di.DaggerTaskComponent;
import com.ivy.ui.task.di.TaskModule;
import com.ivy.ui.task.model.TaskDataBO;
import com.ivy.utils.AppUtils;
import com.ivy.utils.DateTimeUtils;
import com.ivy.utils.FileUtils;

import java.util.ArrayList;
import java.util.HashMap;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class TaskDetailActivity extends BaseActivity implements TaskContract.TaskView {
    private static final int CAMERA_REQUEST_CODE = 1;

    @BindView(R.id.toolbar)
    Toolbar toolbar;

    @BindView(R.id.task_title__value_tv)
    TextView taskTitleTv;

    @BindView(R.id.task_category_value_tv)
    TextView taskProductLevelTv;

    @BindView(R.id.task_category_tv)
    TextView tskProdLevelTitle;

    @BindView(R.id.task_due_date_tv)
    TextView taskDueDateLabel;

    @BindView(R.id.task_due_date_value_tv)
    TextView taskDueDateTv;

    @BindView(R.id.task_img_recycler_view)
    RecyclerView taskImgRecyclerView;

    @BindView(R.id.task_desc_value_tv)
    TextView taskDescTv;

    @BindView(R.id.task_evidence_image_bt)
    ImageButton evidenceImgButton;

    @BindView(R.id.task_evidence_image_view)
    ImageView evidenceImgView;

    @BindView(R.id.task_created_by_tv)
    TextView createdByTv;

    @BindView(R.id.task_created_by_value_tv)
    TextView createdByValueTv;

    @BindView(R.id.task_created_date_tv)
    TextView createdDateTv;

    @BindView(R.id.task_created_date_value_tv)
    TextView createdDateValueTv;

    @BindView(R.id.evidence_img_rl)
    RelativeLayout evidenceRL;


    private TaskDataBO detailBo;
    private boolean isRetailerWiseTask;
    private int tabSelection;
    private String imageName = "";
    private String menuCode;
    private String screenTitle;
    private boolean fromTaskNotification;
    @Inject
    TaskContract.TaskPresenter<TaskContract.TaskView> taskPresenter;

    @Override
    public int getLayoutId() {
        return R.layout.activity_task_detail;
    }

    @Override
    protected void initVariables() {

    }

    @Override
    public void initializeDi() {
        DaggerTaskComponent.builder()
                .ivyAppComponent(((BusinessModel) getApplication()).getComponent())
                .taskModule(new TaskModule(this, TaskDetailActivity.this))
                .build()
                .inject(this);

        setBasePresenter((BasePresenter) taskPresenter);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void getMessageFromAliens() {
        if (getIntent().getExtras() != null) {
            isRetailerWiseTask = getIntent().getBooleanExtra(TaskConstant.RETAILER_WISE_TASK, false);
            tabSelection = getIntent().getIntExtra(TaskConstant.TAB_SELECTION, 0);
            detailBo = getIntent().getExtras().getParcelable(TaskConstant.TASK_OBJECT);
            menuCode = getIntent().getExtras().getString(TaskConstant.MENU_CODE, "MENU_TASK");
            screenTitle = getIntent().getExtras().getString(TaskConstant.SCREEN_TITLE, getString(R.string.task_creation));
            fromTaskNotification = getIntent().getExtras().getBoolean(TaskConstant.TASK_NOTIFICATION_SRC, false);
        }
    }

    @Override
    protected void setUpViews() {
        setUnBinder(ButterKnife.bind(this));
        overridePendingTransition(R.anim.trans_left_in, R.anim.trans_left_out);
        setUpToolBar(getString(R.string.task_detail));
        TaskConstant.TASK_SERVER_IMG_PATH = getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS) + "/"
                + taskPresenter.getUserID()
                + DataMembers.DIGITAL_CONTENT + "/"
                + DataMembers.TASK_DIGITAL_CONTENT;
        if (tabSelection == 2)
            hideViews();

        setUpRecyclerView();
        taskPresenter.fetchTaskImageList(detailBo.getTaskId());
    }


    private void setUpToolBar(String screenTitle) {
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        // Used to remove the appLogo action bar icon and set title as home
        // (title support click)
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        // Used to hide the appLogo icon from action bar

        getSupportActionBar().setIcon(null);

        getSupportActionBar().setTitle(null);

        setScreenTitle(screenTitle);

        if (!isRetailerWiseTask) {
            tskProdLevelTitle.setVisibility(View.GONE);
            taskProductLevelTv.setVisibility(View.GONE);
        }
    }

    private void hideViews() {
        taskDueDateLabel.setText(getString(R.string.executed_date));
        evidenceRL.setVisibility(View.GONE);
    }

    private void setUpRecyclerView() {
        LinearLayoutManager layoutManager1 = new LinearLayoutManager(this);
        layoutManager1.setOrientation(LinearLayoutManager.HORIZONTAL);
        taskImgRecyclerView.setHasFixedSize(true);
        taskImgRecyclerView.setNestedScrollingEnabled(false);
        taskImgRecyclerView.setLayoutManager(layoutManager1);
    }

    @OnClick(R.id.task_evidence_image_bt)
    public void addPhotoClick() {
        if (detailBo.isChecked())
            prepareTaskPhotoCapture();
        else
            showMessage(getString(R.string.task_exec_mandatory));
    }

    @Override
    public void updateLabelNames(HashMap<String, String> labelMap) {

        if (labelMap.containsKey(TaskConstant.TASK_TITLE_LABEL))
            ((TextView) findViewById(R.id.task_title_tv)).setText(labelMap.get(TaskConstant.TASK_TITLE_LABEL));

        if (labelMap.containsKey(TaskConstant.TASK_DUE_DATE_LABEL))
            ((TextView) findViewById(R.id.task_due_date_tv)).setText(labelMap.get(TaskConstant.TASK_DUE_DATE_LABEL));

        if (labelMap.containsKey(TaskConstant.TASK_CREATED_BY_LABEL))
            tskProdLevelTitle.setText(labelMap.get(TaskConstant.TASK_CREATED_BY_LABEL));

        if (labelMap.containsKey(TaskConstant.TASK_CREATED_BY_LABEL))
            ((TextView) findViewById(R.id.task_created_by_tv)).setText(labelMap.get(TaskConstant.TASK_CREATED_BY_LABEL));

        if (labelMap.containsKey(TaskConstant.TASK_DESCRIPTION_LABEL))
            ((TextView) findViewById(R.id.task_desc_tv)).setText(TaskConstant.TASK_DESCRIPTION_LABEL);

        if (labelMap.containsKey(TaskConstant.TASK_PHOTO_CAPTURE_LABEL))
            ((TextView) findViewById(R.id.task_img_tv)).setText(TaskConstant.TASK_PHOTO_CAPTURE_LABEL);

        if (labelMap.containsKey(TaskConstant.TASK_EVIDENCE_LABEL))
            ((TextView) findViewById(R.id.evidence_img_tv)).setText(TaskConstant.TASK_EVIDENCE_LABEL);
    }

    @Override
    public void updateListData(ArrayList<TaskDataBO> updatedList) {
        taskTitleTv.setText(detailBo.getTasktitle());
        taskProductLevelTv.setText(detailBo.getTaskCategoryDsc());

        if (tabSelection == 2)
            taskDueDateTv.setText(DateTimeUtils.convertFromServerDateToRequestedFormat(detailBo.getTaskExecDate(), taskPresenter.outDateFormat()));
        else
            taskDueDateTv.setText(DateTimeUtils.convertFromServerDateToRequestedFormat(detailBo.getTaskDueDate(), taskPresenter.outDateFormat()));

        taskImgRecyclerView.setAdapter(new TaskImgListAdapter(this, updatedList, true, null));
        createdByValueTv.setText(detailBo.getTaskOwner());
        createdDateValueTv.setText(DateTimeUtils.convertFromServerDateToRequestedFormat
                (detailBo.getCreatedDate(), taskPresenter.outDateFormat()));
        taskDescTv.setText(detailBo.getTaskDesc());
        setImageIntoView();

    }

    @Override
    public void showImageUpdateMsg() {
        showMessage(R.string.image_saved);
        detailBo.setTaskEvidenceImg(imageName);
        setImageIntoView();
    }

    @Override
    public void onDeleteSuccess() {
        showAlert("", getString(R.string.deleted_sucessfully), () -> backNavigation());
    }

    @Override
    public void showErrorMsg() {

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_edit_delete, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        if (fromTaskNotification) {
            menu.findItem(R.id.menu_edit_note).setVisible(false);
            menu.findItem(R.id.menu_delete_note).setVisible(false);
        }
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();
        if (id == android.R.id.home) {
            if (!fromTaskNotification) {
                backNavigation();
            } else {
                finish();
                overridePendingTransition(R.anim.trans_right_in, R.anim.trans_right_out);
            }
            return true;
        } else if (id == R.id.menu_edit_note) {
            navigateToTaskCreation();
            return true;
        } else if (id == R.id.menu_delete_note) {
            showDeleteAlert();
            return true;
        }

        return false;
    }

    private void navigateToTaskCreation() {
        Intent i = new Intent(TaskDetailActivity.this, TaskCreationActivity.class);
        i.putExtra(TaskConstant.MENU_CODE, menuCode);
        i.putExtra(TaskConstant.SCREEN_TITLE, screenTitle);
        i.putExtra(TaskConstant.TASK_SCREEN_MODE, 1);
        i.putExtra(TaskConstant.TASK_DETAIL_SRC, true);
        i.putExtra(TaskConstant.TASK_OBJECT, detailBo);
        startActivity(i);
    }

    private void backNavigation() {
        if (!isRetailerWiseTask)
            startActivity(new Intent(TaskDetailActivity.this,
                    HomeScreenActivity.class).putExtra(TaskConstant.MENU_CODE, menuCode));
        else {

            Intent intent = new Intent(TaskDetailActivity.this,
                    TaskActivity.class);
            if (getIntent().getBooleanExtra("PreVisit", false))
                intent.putExtra("PreVisit", true);

            startActivity(intent.putExtra(TaskConstant.RETAILER_WISE_TASK, isRetailerWiseTask)
                    .putExtra(TaskConstant.SCREEN_TITLE, screenTitle)
                    .putExtra(TaskConstant.MENU_CODE, menuCode));
        }

        finish();
        overridePendingTransition(R.anim.trans_right_in, R.anim.trans_right_out);
    }

    private void showDeleteAlert() {
        showAlert("", getString(R.string.do_you_want_to_delete_the_note),
                () -> taskPresenter.deleteTask(detailBo.getTaskId(), detailBo.getTaskOwner(), detailBo.getServerTask()),
                () -> {
                });
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CAMERA_REQUEST_CODE) {
            if (resultCode == 1) {
                taskPresenter.updateTaskExecutionImg(imageName, detailBo.getTaskId(), true);
            }
        }
    }

    private void setImageIntoView() {
        if (detailBo.getTaskEvidenceImg() == null
                || detailBo.getTaskEvidenceImg().isEmpty()) {
            evidenceImgView.setVisibility(View.GONE);
        } else {
            detailBo.setTaskEvidenceImg(detailBo.getTaskEvidenceImg());
            evidenceImgView.setVisibility(View.VISIBLE);
            String path = TaskConstant.TASK_SERVER_IMG_PATH + "/" + detailBo.getTaskEvidenceImg();
            if (FileUtils.isFileExisting(path)) {
                Uri uri = FileUtils
                        .getUriFromFile(getApplicationContext(), path);
                Glide.with(this).load(uri)
                        .asBitmap()
                        .diskCacheStrategy(DiskCacheStrategy.NONE)
                        .skipMemoryCache(true)
                        .centerCrop()
                        .placeholder(R.drawable.no_image_available)
                        .error(R.drawable.no_image_available)
                        .into(AppUtils.getRoundedImageTarget(this, evidenceImgView, (float) 6));
            }
        }
    }


    private void prepareTaskPhotoCapture() {

        imageName = "TSK_" + taskPresenter.getUserID()
                + "_" + DateTimeUtils.now(DateTimeUtils.DATE_TIME_ID_MILLIS)
                + ".jpg";

        String mFirstNameStarts = detailBo.getTaskEvidenceImg();

        boolean mIsFileAvailable = FileUtils.checkForNFilesInFolder(FileUtils.photoFolderPath, 1, mFirstNameStarts);
        boolean copyFileAvailable = FileUtils.checkForNFilesInFolder(TaskConstant.TASK_SERVER_IMG_PATH, 1, mFirstNameStarts);
        if (mIsFileAvailable || copyFileAvailable) {
            showAlert("", getString(R.string.word_photocaptured_delete_retake), () -> {

                if (mIsFileAvailable)
                    FileUtils.deleteFiles(FileUtils.photoFolderPath,
                            mFirstNameStarts);

                if (copyFileAvailable)
                    FileUtils.deleteFiles(TaskConstant.TASK_SERVER_IMG_PATH
                            , mFirstNameStarts);

                navigateToCameraActivity();
            }, () -> {

            });
        } else
            navigateToCameraActivity();

    }

    private void navigateToCameraActivity() {
        Intent intent = new Intent(
                TaskDetailActivity.this,
                CameraActivity.class);
        String _path = FileUtils.photoFolderPath + "/" + imageName;
        //  intent.putExtra("quality", 40);
        intent.putExtra(TaskConstant.FILE_PATH, _path);
        startActivityForResult(intent,
                CAMERA_REQUEST_CODE);
    }

}
