package com.ivy.ui.task.view;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.ivy.core.base.presenter.BasePresenter;
import com.ivy.core.base.view.BaseActivity;
import com.ivy.cpg.view.task.TaskDataBO;
import com.ivy.sd.camera.CameraActivity;
import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.util.CommonDialog;
import com.ivy.sd.png.util.Commons;
import com.ivy.ui.task.TaskConstant;
import com.ivy.ui.task.TaskContract;
import com.ivy.ui.task.adapter.TaskImgListAdapter;
import com.ivy.ui.task.di.DaggerTaskComponent;
import com.ivy.ui.task.di.TaskModule;
import com.ivy.utils.AppUtils;
import com.ivy.utils.DateTimeUtils;
import com.ivy.utils.FileUtils;

import java.util.ArrayList;

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


    private TaskDataBO detailBo;
    private boolean isFromHomeSrc;
    private String imageName = "";
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
            detailBo = getIntent().getExtras().getParcelable(TaskConstant.TASK_OBJECT);

        }
    }

    @Override
    protected void setUpViews() {
        setUnBinder(ButterKnife.bind(this));
        setUpToolBar(getString(R.string.task_detail));
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

        if (isFromHomeSrc) {
            tskProdLevelTitle.setVisibility(View.GONE);
            taskProductLevelTv.setVisibility(View.GONE);
        }
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
        prepareTaskPhotoCapture();
    }


    @Override
    public void showUpdatedDialog(int msgResId) {

    }

    @Override
    public void updateListData(ArrayList<TaskDataBO> updatedList) {
        taskTitleTv.setText(detailBo.getTasktitle());
        taskProductLevelTv.setText(detailBo.getTaskCategoryDsc());
        taskDueDateTv.setText(DateTimeUtils.convertFromServerDateToRequestedFormat(detailBo.getTaskDueDate(), taskPresenter.outDateFormat()));
        taskImgRecyclerView.setAdapter(new TaskImgListAdapter(this, updatedList, true, null));
        createdByValueTv.setText(detailBo.getTaskOwner());
        createdDateValueTv.setText(DateTimeUtils.convertFromServerDateToRequestedFormat
                (detailBo.getCreatedDate(), taskPresenter.outDateFormat()));
        taskDescTv.setText(detailBo.getTaskDesc());
        setImageIntoView(getIntent().getExtras().getString(TaskConstant.EVIDENCE_IMAGE));

    }

    @Override
    public void updateImageListAdapter(ArrayList<TaskDataBO> imageList) {

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();
        if (id == android.R.id.home) {
            finish();
            return true;
        }

        return false;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CAMERA_REQUEST_CODE) {
            if (resultCode == 1) {
                // detailBo.setTaskEvidenceImg(imageName);
                taskPresenter.updateTaskExecutionImg(imageName, detailBo.getTaskId());
                setImageIntoView(imageName);
            }

        }
    }

    private void setImageIntoView(String imageName) {
        if (imageName.isEmpty()) {
            evidenceImgView.setVisibility(View.GONE);
        } else {
            detailBo.setTaskEvidenceImg(imageName);
            evidenceImgView.setVisibility(View.VISIBLE);
            String path = FileUtils.photoFolderPath + "/" + imageName;
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
       /* int id = 0;
        switch (detailBo.getMode()) {
            case "seller":
                id = detailBo.getUserId();
                break;
            case "retailer":
                id = detailBo.getRid();
                break;
            default:
                id = detailBo.getChannelId();
        }*/

        imageName = "TSK_" + detailBo.getTaskId() + "_" + detailBo.getTaskCategoryID()
                + "_" + DateTimeUtils.now(DateTimeUtils.DATE_TIME_ID_MILLIS)
                + ".jpg";

        String mFirstNameStarts = "TSK_" + detailBo.getTaskId()
                + "_" + detailBo.getTaskCategoryID()
                + "_" + Commons.now(Commons.DATE);

        boolean mIsFileAvailable = FileUtils.checkForNFilesInFolder(FileUtils.photoFolderPath, 1, mFirstNameStarts);

        if (mIsFileAvailable) {
            showAlert("", getString(R.string.word_photocaptured_delete_retake), new CommonDialog.PositiveClickListener() {
                @Override
                public void onPositiveButtonClick() {

                    FileUtils.deleteFiles(FileUtils.photoFolderPath,
                            mFirstNameStarts);
                    navigateToCameraActivity();
                }
            }, new CommonDialog.negativeOnClickListener() {
                @Override
                public void onNegativeButtonClick() {

                }
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
