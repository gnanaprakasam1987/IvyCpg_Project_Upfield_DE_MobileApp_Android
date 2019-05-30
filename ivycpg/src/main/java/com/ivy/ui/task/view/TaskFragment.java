package com.ivy.ui.task.view;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.TabLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.ivy.core.base.presenter.BasePresenter;
import com.ivy.core.base.view.BaseFragment;
import com.ivy.cpg.view.homescreen.HomeScreenActivity;
import com.ivy.sd.camera.CameraActivity;
import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.util.DataMembers;
import com.ivy.sd.png.view.HomeScreenTwo;
import com.ivy.sd.png.view.ReasonPhotoDialog;
import com.ivy.ui.task.TaskClickListener;
import com.ivy.ui.task.TaskConstant;
import com.ivy.ui.task.TaskContract;
import com.ivy.ui.task.TaskViewListener;
import com.ivy.ui.task.adapter.BottomSortListAdapter;
import com.ivy.ui.task.adapter.TaskListAdapter;
import com.ivy.ui.task.di.DaggerTaskComponent;
import com.ivy.ui.task.di.TaskModule;
import com.ivy.ui.task.model.TaskDataBO;
import com.ivy.utils.AppUtils;
import com.ivy.utils.DateTimeUtils;
import com.ivy.utils.FileUtils;

import java.util.ArrayList;
import java.util.Objects;

import javax.inject.Inject;

public class TaskFragment extends BaseFragment implements TaskContract.TaskListView, TabLayout.OnTabSelectedListener, TaskClickListener {

    private TabLayout tabLayout;
    private RecyclerView recyclerView;
    private boolean isChannelWise = false;
    private String mSelectedRetailerID = "0";
    private String screenTitle;
    private String imageName = "";
    private static final int CAMERA_REQUEST_CODE = 1;
    private BottomSheetBehavior bottomSheetBehavior;
    private View taskBgView;
    private int lastSelectedPos = -1;
    private String mSelectedTaskId = "0";
    private String menuCode;
    private TaskConstant.SOURCE source;
    private TaskViewListener taskViewListener;

    private Context context;

    @Inject
    TaskContract.TaskPresenter<TaskContract.TaskView> taskPresenter;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.context = context;
    }

    @Override
    public void initializeDi() {
        DaggerTaskComponent.builder()
                .ivyAppComponent(((BusinessModel) getActivity().getApplication()).getComponent())
                .taskModule(new TaskModule(this, getActivity()))
                .build()
                .inject(this);

        setBasePresenter((BasePresenter) taskPresenter);
    }

    @Override
    protected int setContentViewLayout() {
        return R.layout.task_fragment_new;
    }

    @Override
    public void init(View view) {
        taskBgView = view.findViewById(R.id.task_bg_view);
        tabLayout = view.findViewById(R.id.tabs);
        tabLayout.addOnTabSelectedListener(this);
        recyclerView = view.findViewById(R.id.task_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setHasFixedSize(false);
        recyclerView.setNestedScrollingEnabled(false);
        recyclerView.addItemDecoration(new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL));
        bottomSheetBehavior = BottomSheetBehavior.from(view.findViewById(R.id.bottomSheetLayout));
        bottomSheetBehavior.setHideable(false);
        view.findViewById(R.id.task_bg_view).setOnClickListener(v -> hideBottomSheet());

        setUpBottomSheet(view);
    }

    @Override
    protected void getMessageFromAliens() {
        Bundle bundle = getArguments();
        if (bundle == null)
            bundle = Objects.requireNonNull(getActivity()).getIntent().getExtras();

        if (bundle != null) {
            if (bundle.containsKey(TaskConstant.FROM_HOME_SCREEN))
                source = TaskConstant.SOURCE.HOME_SCREEN;

            if (bundle.containsKey(TaskConstant.RETAILER_WISE_TASK))
                source = TaskConstant.SOURCE.RETAILER;

            if (bundle.containsKey(TaskConstant.FORM_CHANNEL_WISE))
                isChannelWise = true;

            if (bundle.containsKey(TaskConstant.FROM_PROFILE_SCREEN))
                source = TaskConstant.SOURCE.PROFILE_SCREEN;

            if (bundle.containsKey(TaskConstant.SCREEN_TITLE))
                screenTitle = bundle.getString(TaskConstant.SCREEN_TITLE, getString(R.string.task));

            if (bundle.containsKey(TaskConstant.MENU_CODE))
                menuCode = bundle.getString(TaskConstant.MENU_CODE);
        }

    }

    @Override
    protected void setUpViews() {
        TaskConstant.TASK_SERVER_IMG_PATH = getActivity().getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS) + "/"
                + taskPresenter.getUserID()
                + DataMembers.DIGITAL_CONTENT + "/"
                + DataMembers.TASK_DIGITAL_CONTENT;

        if (TaskConstant.SOURCE.PROFILE_SCREEN != source)
            setUpActionBar();

        if (TaskConstant.SOURCE.RETAILER == source) {
            mSelectedRetailerID = String.valueOf(taskPresenter.getRetailerID());
        }

        addTabs();


    }

    @Override
    public void onResume() {
        super.onResume();
        getActivity().invalidateOptionsMenu();
    }

    // Add tabs to Tablayout
    private void addTabs() {

        String[] reason = getResources().getStringArray(
                R.array.task_tab_header);

        if (taskPresenter.isShowServerTaskOnly()) {
            reason = new String[1];
            reason[0] = getString(R.string.all);
        }

        for (String tab_name : reason) {
            tabLayout.addTab(tabLayout.newTab().setText(tab_name));
        }
    }


    private void setUpBottomSheet(View view) {

        bottomSheetBehavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                switch (newState) {
                    case BottomSheetBehavior.STATE_COLLAPSED:
                        if (recyclerView.getVisibility() == View.VISIBLE) {
                            hideBottomSheet();
                        }
                        break;
                    case BottomSheetBehavior.STATE_DRAGGING:
                        break;
                    case BottomSheetBehavior.STATE_EXPANDED:
                        break;
                    case BottomSheetBehavior.STATE_HIDDEN:
                        taskBgView.setVisibility(View.GONE);
                        break;
                    case BottomSheetBehavior.STATE_SETTLING:
                        break;
                }
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {
            }
        });

        RecyclerView bottomRecyclerView = view.findViewById(R.id.sort_recycler_view);
        bottomRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        bottomRecyclerView.setItemAnimator(new DefaultItemAnimator());
        bottomRecyclerView.setHasFixedSize(false);
        bottomRecyclerView.setNestedScrollingEnabled(false);
        bottomRecyclerView.addItemDecoration(new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL));
        bottomRecyclerView.setAdapter(new BottomSortListAdapter(getActivity(), getResources().getStringArray(R.array.task_sort_list), this, lastSelectedPos));
    }


    private void hideBottomSheet() {
        bottomSheetBehavior.setHideable(true);
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
    }


    @Override
    public void onTaskExecutedClick(TaskDataBO taskDataBO) {
        if (source == TaskConstant.SOURCE.RETAILER) {
            taskPresenter.updateModuleTime();
            taskPresenter.updateTaskExecution(taskPresenter.getRetailerID() + "", taskDataBO);
        } else {
            taskPresenter.updateTaskExecution(0 + "", taskDataBO);
        }
    }

    @Override
    public void onTaskButtonClick(TaskDataBO taskBO, int isType) {
        if (taskBO.getUsercreated().equals("0") && isType != 0) {
            showMessage(R.string.server_task_can_not_be_edit);
            return;
        } else if (taskBO.isChecked() && isType != 0) {
            showMessage(R.string.exec_task_not_allow_to_edit);
            return;
        }


        Intent i = null;
        switch (isType) {
            case 0:
                i = new Intent(getActivity(), TaskDetailActivity.class);
                if (taskBO.getTaskEvidenceImg() == null
                        && tabLayout.getSelectedTabPosition() == 3)
                    i.putExtra(TaskConstant.EVIDENCE_IMAGE, imageName);

                i.putExtra(TaskConstant.MENU_CODE, menuCode);
                i.putExtra(TaskConstant.SCREEN_TITLE, screenTitle);
                i.putExtra(TaskConstant.TAB_SELECTION, tabLayout.getSelectedTabPosition());
                break;
            case 1:
                i = new Intent(getActivity(), TaskCreationActivity.class);
                i.putExtra(TaskConstant.MENU_CODE, menuCode);
                i.putExtra(TaskConstant.SCREEN_TITLE, screenTitle);
                i.putExtra(TaskConstant.TASK_SCREEN_MODE, isType);
                break;
            case 2:
                taskPresenter.deleteTask(taskBO.getTaskId(), taskBO.getTaskOwner(), taskBO.getServerTask());
                break;
        }

        if (i != null) {

            if (isPreVisit)
                i.putExtra("PreVisit",true);

            i.putExtra(TaskConstant.RETAILER_WISE_TASK, (source == TaskConstant.SOURCE.RETAILER));
            i.putExtra(TaskConstant.TASK_OBJECT, taskBO);
            startActivity(i);
            getActivity().finish();
        }
    }

    @Override
    public void onAttachFile(TaskDataBO taskBO) {
        if (!taskBO.isChecked()) {
            showMessage(R.string.task_exec_mandatory);
            return;
        }


        mSelectedTaskId = taskBO.getTaskId();

        imageName = "TSK_" + taskPresenter.getUserID()
                + "_" + DateTimeUtils.now(DateTimeUtils.DATE_TIME_ID_MILLIS)
                + ".jpg";


        String mFirstNameStarts = taskBO.getTaskEvidenceImg();


        boolean mIsFileAvailable = FileUtils.checkForNFilesInFolder(FileUtils.photoFolderPath, 1, mFirstNameStarts);
        boolean copyFileAvailable = FileUtils.checkForNFilesInFolder(TaskConstant.TASK_SERVER_IMG_PATH, 1, mFirstNameStarts);


        if (mIsFileAvailable || copyFileAvailable)
            showFileDeleteAlert(mFirstNameStarts);
        else
            navigateToCameraActivity();

        taskBO.setTaskEvidenceImg(imageName);
    }

    @Override
    public void onSortItemClicked(int sortType, boolean orderByAsc) {
        taskPresenter.orderBySortList(sortType, orderByAsc);
        lastSelectedPos = sortType;
        hideBottomSheet();
    }

    /**
     * Alert dialog for deleting image
     *
     * @param imageNameStarts
     */
    private void showFileDeleteAlert(final String imageNameStarts) {

        AlertDialog.Builder builder = new AlertDialog.Builder(
                getActivity());
        builder.setTitle("");
        builder.setMessage(getResources().getString(
                R.string.word_photocaptured_delete_retake));

        builder.setPositiveButton(getResources().getString(R.string.ok),
                (dialog, which) -> {

                    FileUtils.deleteFiles(FileUtils.photoFolderPath,
                            imageNameStarts);
                    FileUtils.deleteFiles(TaskConstant.TASK_SERVER_IMG_PATH
                            , imageNameStarts);

                    dialog.dismiss();
                    navigateToCameraActivity();

                });

        builder.setNegativeButton(getResources().getString(R.string.cancel),
                (dialog, which) -> dialog.dismiss());

        builder.setCancelable(false);
        AppUtils.applyAlertDialogTheme(getActivity(), builder);
    }


    private void navigateToCameraActivity() {
        Intent intent = new Intent(
                getActivity(),
                CameraActivity.class);
        String _path = FileUtils.photoFolderPath + "/" + imageName;
        intent.putExtra(TaskConstant.FILE_PATH, _path);
        startActivityForResult(intent,
                CAMERA_REQUEST_CODE);
    }


    private void setUpActionBar() {
        ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowTitleEnabled(false);
            actionBar.setDisplayShowHomeEnabled(true);

            setScreenTitle(screenTitle);
        }
        setHasOptionsMenu(true);

    }



    @Override
    public void onTabSelected(TabLayout.Tab tab) {
        if (bottomSheetBehavior.getState() == BottomSheetBehavior.STATE_EXPANDED)
            hideBottomSheet();
        lastSelectedPos = -1;

        if (taskPresenter.isShowServerTaskOnly()) {
            getView().findViewById(R.id.tv_execution).setVisibility(View.VISIBLE);
            taskPresenter.updateTaskList(TaskConstant.SERVER_TASK, mSelectedRetailerID, (source == TaskConstant.SOURCE.RETAILER), isChannelWise);
        } else if (tab.getPosition() == 3) {
            getView().findViewById(R.id.tv_execution).setVisibility(View.GONE);
            taskPresenter.fetchCompletedTask(mSelectedRetailerID);
        } else {
            getView().findViewById(R.id.tv_execution).setVisibility(View.VISIBLE);
            taskPresenter.updateTaskList(tab.getPosition(), mSelectedRetailerID, (source == TaskConstant.SOURCE.RETAILER), isChannelWise);
        }

    }

    @Override
    public void onTabUnselected(TabLayout.Tab tab) {

    }

    @Override
    public void onTabReselected(TabLayout.Tab tab) {

    }


    @Override
    public void updateListData(ArrayList<TaskDataBO> updatedList) {

        recyclerView.setAdapter(new TaskListAdapter(getActivity(), updatedList, taskPresenter.outDateFormat(), this, source, (source == TaskConstant.SOURCE.RETAILER), tabLayout.getSelectedTabPosition(), isPreVisit));

        if (bottomSheetBehavior.getState() == BottomSheetBehavior.STATE_EXPANDED)
            hideBottomSheet();
    }

    @Override
    public void updateImageListAdapter(ArrayList<TaskDataBO> imageList) {

    }

    @Override
    public void showImageUpdateMsg() {
        showMessage(R.string.image_saved);
    }

    @Override
    public void onDeleteSuccess() {
        showMessage(R.string.saved_successfully);
    }

    @Override
    public void showErrorMsg() {

    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_task, menu);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        menu.findItem(R.id.menu_notification).setVisible(true);
        menu.findItem(R.id.menu_save).setVisible(false);
        menu.findItem(R.id.menu_sort).setVisible(true);
        if (!taskPresenter.isNewTask())
            menu.findItem(R.id.menu_new_task).setVisible(false);

        if (source == TaskConstant.SOURCE.RETAILER)//this is applicable for store wise task
            menu.findItem(R.id.menu_reason).setVisible(taskPresenter.isNoTaskReason());

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int i1 = item.getItemId();
        if (i1 == android.R.id.home) {
            backNavigation();
        } else if (i1 == R.id.menu_new_task) {
            if (!isPreVisit)
                goToNewTaskActivity();
        } else if (i1 == R.id.menu_reason) {
            goToNoTaskReasonDialog();
        } else if (i1 == R.id.menu_sort) {
            if (bottomSheetBehavior.getState() == BottomSheetBehavior.STATE_EXPANDED) {
                hideBottomSheet();
            } else {
                taskBgView.setVisibility(View.VISIBLE);
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
            }
        } else if (i1 == R.id.menu_notification) {
            taskViewListener.switchTaskView(true);
        }
        return super.onOptionsItemSelected(item);
    }

    // Comment by Gp, Issue while going back from Activity Menu
    private void backNavigation() {

        Intent intent = new Intent(getActivity(), HomeScreenTwo.class);

        if (isPreVisit)
            intent.putExtra("PreVisit",true);

        if (source == TaskConstant.SOURCE.RETAILER) {
            if (!isPreVisit)
                taskPresenter.updateModuleTime();
            startActivity(intent);
        } else
            startActivity(intent);

        getActivity().overridePendingTransition(R.anim.trans_right_in, R.anim.trans_right_out);
        getActivity().finish();
    }

    private void goToNewTaskActivity() {

        taskPresenter.updateModuleTime();
        Intent i = new Intent(getActivity(), TaskCreationActivity.class);
        i.putExtra(TaskConstant.RETAILER_WISE_TASK, source == TaskConstant.SOURCE.RETAILER);
        i.putExtra(TaskConstant.MENU_CODE, menuCode);
        i.putExtra(TaskConstant.SCREEN_TITLE, screenTitle);
        startActivity(i);
        getActivity().finish();
    }

    // This dialog used for when task was not completed
    private void goToNoTaskReasonDialog() {
        ReasonPhotoDialog dialog = new ReasonPhotoDialog();
        dialog.setOnDismissListener(dialog1 -> {
            if (taskPresenter.isNPPhotoReasonAvailable(String.valueOf(taskPresenter.getRetailerID()), "MENU_TASK")) {

                Intent intent = new Intent(getActivity(), HomeScreenTwo.class);

                if (isPreVisit)
                    intent.putExtra("PreVisit",true);

                if (source == TaskConstant.SOURCE.RETAILER) {
                    if (!isPreVisit)
                        taskPresenter.saveModuleCompletion(menuCode);
                    startActivity(intent);

                    getActivity().finish();
                }
            }
        });
        Bundle args = new Bundle();
        args.putString(TaskConstant.MODULE_NAME, menuCode);
        dialog.setCancelable(false);
        dialog.setArguments(args);
        dialog.show(getActivity().getSupportFragmentManager(), TaskConstant.PHOTO_CAPTURE_DIALOG_TAG);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CAMERA_REQUEST_CODE) {
            if (resultCode == 1) {
                taskPresenter.updateTaskExecutionImg(imageName, mSelectedTaskId, false);
            } else {
                imageName = "";
            }

        }
        super.onActivityResult(requestCode, resultCode, data);
    }


    @Override
    public void showTaskUpdateMsg() {
        showMessage(R.string.task_updated_successfully);
    }


    public void setTaskViewListener(TaskViewListener listener) {
        this.taskViewListener = listener;
    }


}
