package com.ivy.ui.task.view;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.ivy.core.base.presenter.BasePresenter;
import com.ivy.core.base.view.BaseActivity;
import com.ivy.cpg.view.homescreen.HomeScreenActivity;
import com.ivy.sd.camera.CameraActivity;
import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.util.DataMembers;
import com.ivy.sd.png.view.RemarksDialog;
import com.ivy.ui.task.TaskConstant;
import com.ivy.ui.task.TaskContract;
import com.ivy.ui.task.adapter.TaskImgListAdapter;
import com.ivy.ui.task.di.DaggerTaskComponent;
import com.ivy.ui.task.di.TaskModule;
import com.ivy.ui.task.model.FilterBo;
import com.ivy.ui.task.model.TaskDataBO;
import com.ivy.utils.AppUtils;
import com.ivy.utils.DateTimeUtils;
import com.ivy.utils.DeviceUtils;
import com.ivy.utils.FileUtils;
import com.ivy.utils.view.Dialogs.ReasonCaptureDialog;

import java.util.ArrayList;
import java.util.HashMap;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class TaskDetailActivity extends BaseActivity implements TaskContract.TaskListView, ReasonCaptureDialog.OnButtonClickListener, RemarksDialog.RemarksListener {
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

    @BindView(R.id.footer_ll)
    LinearLayout footerLayout;

    @BindView(R.id.task_execute_btn)
    Button btnTaskExecute;

    @BindView(R.id.task_img_rl)
    RelativeLayout photoRL;

    @BindView(R.id.tsk_img_divider)
    View imageDivider;


    private TaskDataBO taskDetailBo;
    private boolean isRetailerWiseTask;
    private boolean isFromHomeSrc;
    private int tabSelection;
    private String imageName = "";
    private String menuCode;
    private String screenTitle;
    private boolean fromTaskNotification;
    private TaskConstant.SOURCE source;
    private ReasonCaptureDialog reasonCaptureDialog;
    private boolean isPreVisit = false;
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
            isFromHomeSrc = getIntent().getBooleanExtra(TaskConstant.FROM_HOME_SCREEN, false);
            isPreVisit = getIntent().getBooleanExtra("PreVisit", false);
            isRetailerWiseTask = getIntent().getBooleanExtra(TaskConstant.RETAILER_WISE_TASK, false);
            tabSelection = getIntent().getIntExtra(TaskConstant.TAB_SELECTION, 0);
            taskDetailBo = getIntent().getExtras().getParcelable(TaskConstant.TASK_OBJECT);
            menuCode = getIntent().getExtras().getString(TaskConstant.MENU_CODE, "MENU_TASK");
            screenTitle = getIntent().getExtras().getString(TaskConstant.SCREEN_TITLE, getString(R.string.task_creation));
            fromTaskNotification = getIntent().getExtras().getBoolean(TaskConstant.TASK_NOTIFICATION_SRC, false);


            if (getIntent().getExtras().containsKey(TaskConstant.FROM_HOME_SCREEN))
                source = TaskConstant.SOURCE.HOME_SCREEN;

            if (getIntent().getExtras().containsKey(TaskConstant.RETAILER_WISE_TASK))
                source = TaskConstant.SOURCE.RETAILER;

            if (getIntent().getExtras().containsKey(TaskConstant.FROM_PROFILE_SCREEN))
                source = TaskConstant.SOURCE.PROFILE_SCREEN;
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

        if (tabSelection == 3)
            hideViews();


        setUpRecyclerView();

        if (taskDetailBo.isChecked())
            btnTaskExecute.setText(getString(R.string.mark_as_un_executed));
        else
            btnTaskExecute.setText(getString(R.string.mark_as_executed));

        taskPresenter.fetchTaskImageList(taskDetailBo.getTaskId());
        taskPresenter.fetchReasonFromStdListMasterByListCode();
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
        if (!fromTaskNotification) {
            taskDueDateLabel.setText(getString(R.string.executed_date));
            evidenceRL.setVisibility(View.GONE);
        }
    }

    private void setUpRecyclerView() {
        LinearLayoutManager layoutManager1 = new LinearLayoutManager(this);
        layoutManager1.setOrientation(LinearLayoutManager.HORIZONTAL);
        taskImgRecyclerView.setHasFixedSize(true);
        taskImgRecyclerView.setNestedScrollingEnabled(false);
        taskImgRecyclerView.setLayoutManager(layoutManager1);

        if (tabSelection != 3
                && !fromTaskNotification)
            footerLayout.setVisibility(View.VISIBLE);
    }

    @OnClick(R.id.task_evidence_image_bt)
    public void addPhotoClick() {
        if (taskDetailBo.isChecked())
            prepareTaskPhotoCapture();
        else
            showMessage(getString(R.string.task_exec_mandatory));
    }

    @OnClick(R.id.task_execute_btn)
    public void onTaskExecution() {
        if (taskDetailBo.isChecked())
            taskDetailBo.setChecked(false);
        else
            taskDetailBo.setChecked(true);

        if (taskPresenter.isShowRemarks()) {
            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            RemarksDialog dialog = new RemarksDialog(taskDetailBo.getRemark(),
                    "MENU_PROMO_REMARKS", this);
            dialog.setCancelable(false);
            dialog.show(ft, "TASK");
        } else {
            updateTaskExecution();
        }
    }

    private void updateTaskExecution() {
        if (source == TaskConstant.SOURCE.RETAILER)
            taskPresenter.updateModuleTime();

        taskPresenter.updateTaskExecution(taskDetailBo, 0);
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

        taskTitleTv.setText(taskDetailBo.getTasktitle());
        taskProductLevelTv.setText(taskDetailBo.getTaskCategoryDsc());

        if (tabSelection == 3)
            taskDueDateTv.setText(DateTimeUtils.convertFromServerDateToRequestedFormat(taskDetailBo.getTaskExecDate(), taskPresenter.outDateFormat()));
        else
            taskDueDateTv.setText(DateTimeUtils.convertFromServerDateToRequestedFormat(taskDetailBo.getTaskDueDate(), taskPresenter.outDateFormat()));

        if (!updatedList.isEmpty())
            taskImgRecyclerView.setAdapter(new TaskImgListAdapter(this, updatedList, true, null));
        else {
            photoRL.setVisibility(View.GONE);
            imageDivider.setVisibility(View.GONE);
        }

        createdByValueTv.setText(taskDetailBo.getTaskOwner());
        createdDateValueTv.setText(DateTimeUtils.convertFromServerDateToRequestedFormat
                (taskDetailBo.getCreatedDate(), taskPresenter.outDateFormat()));
        taskDescTv.setText(taskDetailBo.getTaskDesc());
        setImageIntoView();

    }

    @Override
    public void showImageUpdateMsg() {
        showMessage(R.string.image_saved);
        taskDetailBo.setTaskEvidenceImg(imageName);
        setImageIntoView();
    }

    @Override
    public void onDeleteSuccess() {
        showAlert("", getString(R.string.deleted_sucessfully), () -> backNavigation(TaskConstant.TASK_UPDATED_SUCCESS_CODE));
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

        //close btn enable based on days count (over due tasks only)
        int daysCount = DateTimeUtils.getDateCount(taskDetailBo.getTaskDueDate(),
                DateTimeUtils.now(DateTimeUtils.DATE_GLOBAL), "yyyy/MM/dd");

        if (daysCount >= 1)
            menu.findItem(R.id.menu_close).setVisible(true);
        else
            menu.findItem(R.id.menu_close).setVisible(false);

        if (fromTaskNotification || tabSelection == 3 || taskDetailBo.isChecked()) {
            menu.findItem(R.id.menu_edit_note).setVisible(false);
            menu.findItem(R.id.menu_delete_note).setVisible(false);
            menu.findItem(R.id.menu_close).setVisible(false);
        }
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();
        if (id == android.R.id.home) {
            finish();
            overridePendingTransition(R.anim.trans_right_in, R.anim.trans_right_out);
            return true;
        } else if (id == R.id.menu_edit_note) {
            if(!isPreVisit)
            navigateToTaskCreation();
            return true;
        } else if (id == R.id.menu_delete_note) {
            if(!isPreVisit)
            showDeleteAlert();
            return true;
        } else if (id == R.id.menu_close) {
            if (!taskPresenter.fetchNotCompletedTaskReasons().isEmpty())
                showReasonDialog();
            else
                showMessage(getString(R.string.data_not_mapped));
        }

        return false;
    }


    private void showReasonDialog() {
        if (reasonCaptureDialog == null)
            reasonCaptureDialog = new ReasonCaptureDialog.ReasonCaptureDialogBuilder(this, taskPresenter.fetchNotCompletedTaskReasons())
                    .setDialogTitles(getString(R.string.would_you_like_add_reason_to_close_task), getString(R.string.select_reason))
                    .setOnButtonClickListener(this)
                    .buildDialog();

        reasonCaptureDialog.show();
        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        Window window = reasonCaptureDialog.getWindow();
        lp.copyFrom(window != null ? window.getAttributes() : null);
        lp.width = DeviceUtils.getDisplayMetrics(this).widthPixels - 100;
        // lp.height = (DeviceUtils.getDisplayMetrics(context).heightPixels / 2);//WindowManager.LayoutParams.WRAP_CONTENT;
        if (window != null) {
            window.setAttributes(lp);
        }
    }

    private void navigateToTaskCreation() {
        Intent i = new Intent(TaskDetailActivity.this, TaskCreationActivity.class);
        i.putExtra(TaskConstant.MENU_CODE, menuCode);
        i.putExtra(TaskConstant.SCREEN_TITLE, screenTitle);
        i.putExtra(TaskConstant.TASK_SCREEN_MODE, TaskConstant.EDIT_MODE_FROM_TASK_DETAIL_SRC);
        i.putExtra(TaskConstant.TASK_DETAIL_SRC, true);
        i.putExtra(TaskConstant.TASK_OBJECT, taskDetailBo);
        i.putExtra(TaskConstant.FROM_HOME_SCREEN, isFromHomeSrc);
        i.putExtra(TaskConstant.RETAILER_WISE_TASK, isRetailerWiseTask);
        startActivityForResult(i, TaskConstant.TASK_CREATED_SUCCESS_CODE);
    }

    private void backNavigation(int resultCode) {
        if (isFromHomeSrc)
            setResult(resultCode, new Intent(TaskDetailActivity.this,
                    HomeScreenActivity.class).putExtra(TaskConstant.TASK_EXECUTE_RESPONSE, taskDetailBo.isChecked()));
        else {

            Intent intent = new Intent(TaskDetailActivity.this,
                    TaskActivity.class);
            if (getIntent().getBooleanExtra(TaskConstant.TASK_PRE_VISIT, false))
                intent.putExtra(TaskConstant.TASK_PRE_VISIT, true);

            setResult(resultCode, intent.putExtra(TaskConstant.RETAILER_WISE_TASK, isRetailerWiseTask)
                    .putExtra(TaskConstant.TASK_EXECUTE_RESPONSE, taskDetailBo.isChecked()));
        }

        finish();
        overridePendingTransition(R.anim.trans_right_in, R.anim.trans_right_out);
    }

    private void showDeleteAlert() {
        showAlert("", getString(R.string.do_you_want_to_delete_the_task),
                () -> taskPresenter.deleteTask(taskDetailBo.getTaskId(), taskDetailBo.getTaskOwner(), taskDetailBo.getServerTask()),
                () -> {
                });
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CAMERA_REQUEST_CODE) {
            if (resultCode == 1) {
                taskPresenter.updateTaskExecutionImg(imageName, taskDetailBo.getTaskId(), true);
            }
        } else if (requestCode == TaskConstant.TASK_CREATED_SUCCESS_CODE
                && resultCode == 3) {
            backNavigation(TaskConstant.TASK_CREATED_SUCCESS_CODE);
            finish();
        }
    }

    private void setImageIntoView() {
        if (taskDetailBo.getTaskEvidenceImg() == null
                || taskDetailBo.getTaskEvidenceImg().isEmpty()) {
            evidenceImgView.setVisibility(View.GONE);
        } else {
            taskDetailBo.setTaskEvidenceImg(taskDetailBo.getTaskEvidenceImg());
            evidenceImgView.setVisibility(View.VISIBLE);
            String path = TaskConstant.TASK_SERVER_IMG_PATH + "/" + taskDetailBo.getTaskEvidenceImg();
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

        String mFirstNameStarts = taskDetailBo.getTaskEvidenceImg();

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

    @Override
    public void showTaskUpdateMsg() {
        showMessage(R.string.task_updated_successfully);
        backNavigation(0);
    }

    @Override
    public void showTaskReasonUpdateMsg() {
        showMessage(getString(R.string.task_reason_updated_successfully));
        reasonCaptureDialog.dismiss();
        backNavigation(TaskConstant.TASK_UPDATED_SUCCESS_CODE);
    }

    @Override
    public void showDataNotMappedMsg() {

    }

    @Override
    public void setUpFilterList(HashMap<String, ArrayList<FilterBo>> filterListHashMap) {

    }

    @Override
    public void updateSortList() {

    }

    @Override
    public void addReason(int selectedResId) {
        taskPresenter.updateTaskExecution(taskDetailBo, selectedResId);
    }

    @Override
    public void onDismiss() {
        reasonCaptureDialog.dismiss();
    }

    @Override
    public void updateRemarks(String remark) {
        taskDetailBo.setRemark(remark);
        updateTaskExecution();
    }
}
