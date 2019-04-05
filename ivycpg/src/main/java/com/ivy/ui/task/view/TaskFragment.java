package com.ivy.ui.task.view;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
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
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.ivy.core.base.presenter.BasePresenter;
import com.ivy.core.base.view.BaseFragment;
import com.ivy.cpg.view.homescreen.HomeScreenActivity;
import com.ivy.cpg.view.task.TaskDataBO;
import com.ivy.sd.camera.CameraActivity;
import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.util.CommonDialog;
import com.ivy.sd.png.util.Commons;
import com.ivy.sd.png.view.HomeScreenTwo;
import com.ivy.sd.png.view.ReasonPhotoDialog;
import com.ivy.ui.task.TaskClickListener;
import com.ivy.ui.task.TaskConstant;
import com.ivy.ui.task.TaskContract;
import com.ivy.ui.task.adapter.BottomSortListAdapter;
import com.ivy.ui.task.adapter.TaskListAdapter;
import com.ivy.ui.task.di.DaggerTaskComponent;
import com.ivy.ui.task.di.TaskModule;
import com.ivy.utils.AppUtils;
import com.ivy.utils.DateTimeUtils;
import com.ivy.utils.FileUtils;

import java.util.ArrayList;
import java.util.Objects;

import javax.inject.Inject;

public class TaskFragment extends BaseFragment implements TaskContract.TaskView, TabLayout.OnTabSelectedListener {

    private TabLayout tabLayout;
    private RecyclerView recyclerView;
    private Button nxtBtn;
    private LinearLayout footerLL;
    private boolean isRetailerWiseTask;
    private boolean fromHomeScreen;
    private boolean isFromSurvey;
    private boolean fromProfileScreen;
    private String mSelectedRetailerID = "0";
    private String currentActivityCode;
    private String screenTitle;
    private String imageName = "";
    private static final int CAMERA_REQUEST_CODE = 1;
    private BottomSheetBehavior bottomSheetBehavior;
    private View taskBgView;
    private int lastSelectedPos = -1;
    private String mSelectedTaskId = "0";
    private String menuCode;


    @Inject
    TaskContract.TaskPresenter<TaskContract.TaskView> taskPresenter;

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
        footerLL = view.findViewById(R.id.footer);
        nxtBtn = view.findViewById(R.id.btn_close);
        recyclerView = view.findViewById(R.id.task_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setHasFixedSize(false);
        recyclerView.setNestedScrollingEnabled(false);
        recyclerView.addItemDecoration(new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL));
        bottomSheetBehavior = BottomSheetBehavior.from(view.findViewById(R.id.bottomSheetLayout));
        bottomSheetBehavior.setHideable(false);

        setUpBottomSheet(view);
    }

    @Override
    protected void getMessageFromAliens() {
        Bundle bundle = getArguments();
        if (bundle == null)
            bundle = Objects.requireNonNull(getActivity()).getIntent().getExtras();

        if (bundle != null) {
            if (bundle.containsKey(TaskConstant.RETAILER_WISE_TASK))
                isRetailerWiseTask = bundle.getBoolean(TaskConstant.RETAILER_WISE_TASK, false);

            if (bundle.containsKey(TaskConstant.FROM_HOME_SCREEN))
                fromHomeScreen = bundle.getBoolean(TaskConstant.FROM_HOME_SCREEN, false);

            if (bundle.containsKey(TaskConstant.FORM_SURVEY_SCREEN))
                isFromSurvey = bundle.getBoolean(TaskConstant.FORM_SURVEY_SCREEN, false);

            if (bundle.containsKey(TaskConstant.FROM_PROFILE_SCREEN))
                fromProfileScreen = getArguments().getBoolean(TaskConstant.FROM_PROFILE_SCREEN, false);

            if (bundle.containsKey(TaskConstant.CURRENT_ACTIVITY_CODE))
                currentActivityCode = bundle.getString(TaskConstant.CURRENT_ACTIVITY_CODE, "");

            if (bundle.containsKey(TaskConstant.SCREEN_TITLE))
                screenTitle = bundle.getString(TaskConstant.SCREEN_TITLE, getString(R.string.task));

            if (bundle.containsKey(TaskConstant.MENU_CODE))
                menuCode = bundle.getString(TaskConstant.MENU_CODE);
        }
    }

    @Override
    protected void setUpViews() {
        if (!fromProfileScreen)
            setUpActionBar();

        if (taskPresenter.isMoveNextActivity()) {
            footerLL.setVisibility(View.VISIBLE);
            nxtBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    moveNextActivity();
                }
            });
        }

        if (isRetailerWiseTask) {
            mSelectedRetailerID = String.valueOf(taskPresenter.getRetailerID());
        }

        addTabs();

        if (taskPresenter.isShowServerTaskOnly())
            taskPresenter.updateTaskList(TaskConstant.SERVER_TASK, mSelectedRetailerID, isRetailerWiseTask, isFromSurvey);
        else
            taskPresenter.updateTaskList(TaskConstant.ALL_TASK, mSelectedRetailerID, isRetailerWiseTask, isFromSurvey);

    }

    // Add tabs to Tablayout
    private void addTabs() {

        String[] reason = getActivity().getResources().getStringArray(
                R.array.task_tab_header);

        if (taskPresenter.isShowServerTaskOnly()) {
            reason = new String[1];
            reason[0] = getString(R.string.all);
        }

        for (String tab_name : reason) {
            @SuppressLint("InflateParams")
            TextView tabOne = (TextView) LayoutInflater.from(getActivity()).inflate(R.layout.custom_tab, null);
            tabOne.setText(tab_name);
            tabLayout.addTab(tabLayout.newTab().setCustomView(tabOne));
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
        bottomRecyclerView.setAdapter(new BottomSortListAdapter(getActivity(), getResources().getStringArray(R.array.task_sort_list), taskClickListener, lastSelectedPos));
    }


    private void hideBottomSheet() {
        bottomSheetBehavior.setHideable(true);
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
    }

    private void moveNextActivity() {

        showAlert(getString(R.string.move_next_activity)
                , getString(R.string.ok), new CommonDialog.PositiveClickListener() {
                    @Override
                    public void onPositiveButtonClick() {
                        Intent intent = new Intent(getActivity(), HomeScreenTwo.class);
                        intent.putExtra(TaskConstant.MOVE_NEXT_ACTIVITY, true);
                        intent.putExtra(TaskConstant.CURRENT_ACTIVITY_CODE, currentActivityCode);
                        startActivity(intent);
                        getActivity().finish();
                    }
                }, true);
    }

    final TaskClickListener taskClickListener = new TaskClickListener() {
        @Override
        public void onTaskExcutedClick(TaskDataBO taskDataBO) {
            taskPresenter.updateModuleTime();
            if (isRetailerWiseTask) {
                taskPresenter.updateTaskExecution(taskPresenter.getRetailerID() + "", taskDataBO);
            } else {
                taskPresenter.updateTaskExecution(0 + "", taskDataBO);
            }
        }

        /**
         * this method used to screen navigation by isType
         * @param taskBO
         * @param isType 0- {@link TaskDetailActivity}
         *               1- {@link TaskCreationActivity}
         *
         */
        @Override
        public void onTaskButtonClick(TaskDataBO taskBO, int isType) {
            Intent i = null;
            switch (isType) {
                case 0:
                    i = new Intent(getActivity(), TaskDetailActivity.class);
                    i.putExtra(TaskConstant.EVIDENCE_IMAGE, imageName);
                    break;
                case 1:
                    i = new Intent(getActivity(), TaskCreationActivity.class);
                    i.putExtra(TaskConstant.RETAILER_WISE_TASK, isRetailerWiseTask);
                    i.putExtra(TaskConstant.MENU_CODE, menuCode);
                    i.putExtra(TaskConstant.SCREEN_TITLE, screenTitle);
                    i.putExtra(TaskConstant.TASK_SCREEN_MODE, isType);
                    break;
                case 2:
                    taskPresenter.deleteTask(taskBO.getTaskId(), taskBO.getTaskOwner());
                    break;
            }

            if (i != null) {
                i.putExtra(TaskConstant.FROM_HOME_SCREEN, fromHomeScreen);
                i.putExtra(TaskConstant.TASK_OBJECT, taskBO);
                startActivity(i);
            }
        }

        @Override
        public void onAttachFile(String taskId, int productLevelId) {
            mSelectedTaskId = taskId;

            imageName = "TSK_" + taskId + "_" + productLevelId
                    + "_" + DateTimeUtils.now(DateTimeUtils.DATE_TIME_ID_MILLIS)
                    + ".jpg";

            String mFirstNameStarts = "TE_" + taskId
                    + "_" + productLevelId
                    + "_" + Commons.now(Commons.DATE);

            boolean mIsFileAvailable = FileUtils.checkForNFilesInFolder(FileUtils.photoFolderPath, 1, mFirstNameStarts);

            if (mIsFileAvailable)
                showFileDeleteAlert(mFirstNameStarts);
            else
                navigateToCameraActivity();

        }

        @Override
        public void onSortItemClicked(int sortType, boolean orderByAsc) {
            taskPresenter.orderBySortList(sortType, orderByAsc);
            lastSelectedPos = sortType;
            hideBottomSheet();
        }
    };

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
                new android.content.DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {

                        FileUtils.deleteFiles(FileUtils.photoFolderPath,
                                imageNameStarts);
                        dialog.dismiss();
                        navigateToCameraActivity();

                    }
                });

        builder.setNegativeButton(getResources().getString(R.string.cancel),
                new android.content.DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });

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

            setScreenTitle(getString(R.string.task));
        }
        setHasOptionsMenu(true);

    }


    @Override
    public void onTabSelected(TabLayout.Tab tab) {
        if (bottomSheetBehavior.getState() == BottomSheetBehavior.STATE_EXPANDED)
            hideBottomSheet();
        lastSelectedPos = -1;
        taskPresenter.updateTaskList(tab.getPosition(), mSelectedRetailerID, isRetailerWiseTask, isFromSurvey);
    }

    @Override
    public void onTabUnselected(TabLayout.Tab tab) {

    }

    @Override
    public void onTabReselected(TabLayout.Tab tab) {

    }


    @Override
    public void updateListData(ArrayList<TaskDataBO> updatedList) {

        recyclerView.setAdapter(new TaskListAdapter(getActivity(), updatedList, taskPresenter.outDateFormat(), taskClickListener, fromProfileScreen, fromHomeScreen));

        if (bottomSheetBehavior.getState() == BottomSheetBehavior.STATE_EXPANDED)
            hideBottomSheet();
    }

    @Override
    public void updateImageListAdapter(ArrayList<TaskDataBO> imageList) {

    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_task, menu);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        menu.findItem(R.id.menu_save).setVisible(false);
        menu.findItem(R.id.menu_sort).setVisible(true);
        if (!taskPresenter.isNewTask())
            menu.findItem(R.id.menu_new_task).setVisible(false);

        if (!fromHomeScreen)//this is applicable for store wise task
            menu.findItem(R.id.menu_reason).setVisible(taskPresenter.isNoTaskReason());

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int i1 = item.getItemId();
        if (i1 == android.R.id.home) {
            backNavigation();
        } else if (i1 == R.id.menu_new_task) {
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
        }
        return super.onOptionsItemSelected(item);
    }

    // Comment by Gp, Issue while going back from Activity Menu
    private void backNavigation() {

        if (isRetailerWiseTask) {
            taskPresenter.updateModuleTime();
        }
        if (fromHomeScreen)
            startActivity(new Intent(getActivity(), HomeScreenActivity.class));

        getActivity().overridePendingTransition(R.anim.trans_right_in, R.anim.trans_right_out);
        getActivity().finish();
    }

    private void goToNewTaskActivity() {

        taskPresenter.updateModuleTime();
        Intent i = new Intent(getActivity(), TaskCreationActivity.class);
        i.putExtra(TaskConstant.FROM_HOME_SCREEN, fromHomeScreen);
        i.putExtra(TaskConstant.RETAILER_WISE_TASK, isRetailerWiseTask);
        i.putExtra(TaskConstant.MENU_CODE, menuCode);
        i.putExtra(TaskConstant.SCREEN_TITLE, screenTitle);
        startActivity(i);
        getActivity().finish();
    }

    // This dialog used for when task was not completed
    private void goToNoTaskReasonDialog() {
        ReasonPhotoDialog dialog = new ReasonPhotoDialog();
        dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                if (taskPresenter.isNPPhotoReasonAvailable(String.valueOf(taskPresenter.getRetailerID()), "MENU_TASK")) {
                    if (!fromHomeScreen) {
                        taskPresenter.saveModuleCompletion(menuCode);
                        startActivity(new Intent(getActivity(),
                                HomeScreenTwo.class));
                        getActivity().finish();

                    }
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
    public void showUpdatedDialog(int msgResId) {

        showMessage(getString(msgResId));
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == CAMERA_REQUEST_CODE) {
            if (resultCode == 1) {
                taskPresenter.updateTaskExecutionImg(imageName, mSelectedTaskId);
                showMessage(getString(R.string.photo_captured_successfully));
            } else {
                imageName = "";
            }

        }
        super.onActivityResult(requestCode, resultCode, data);
    }
}
