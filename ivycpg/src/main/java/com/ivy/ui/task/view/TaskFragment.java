package com.ivy.ui.task.view;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.ivy.core.base.presenter.BasePresenter;
import com.ivy.core.base.view.BaseFragment;
import com.ivy.sd.camera.CameraActivity;
import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.util.CommonDialog;
import com.ivy.sd.png.util.Commons;
import com.ivy.sd.png.util.DataMembers;
import com.ivy.sd.png.view.HomeScreenTwo;
import com.ivy.sd.png.view.ReasonPhotoDialog;
import com.ivy.ui.task.FilterViewListener;
import com.ivy.ui.task.TaskClickListener;
import com.ivy.ui.task.TaskConstant;
import com.ivy.ui.task.TaskContract;
import com.ivy.ui.task.TaskViewListener;
import com.ivy.ui.task.adapter.BottomSortListAdapter;
import com.ivy.ui.task.adapter.TaskListAdapter;
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
import java.util.Objects;

import javax.inject.Inject;

public class TaskFragment extends BaseFragment implements TaskContract.TaskListView, TabLayout.OnTabSelectedListener, TaskClickListener, ReasonCaptureDialog.OnButtonClickListener, FilterViewListener {

    private DrawerLayout drawerLayout;
    private FrameLayout drawer;
    private TabLayout tabLayout;
    private RecyclerView recyclerView;
    private TextView noDataTv;
    private LinearLayout listHeaderLL;
    private boolean isChannelWise = false;
    private String mSelectedRetailerID = "0";
    private String screenTitle;
    private String imageName = "";
    private BottomSheetBehavior bottomSheetBehavior;
    private View taskBgView;
    private int lastSelectedPos = -1;
    private String mSelectedTaskId = "0";
    private String menuCode;
    private TaskConstant.SOURCE source;
    private boolean isFromHomeSrc = false;
    private TaskViewListener taskViewListener;
    private FloatingActionButton taskCreationFAB;
    private int taskListLastSelectedPos = -1;
    private ReasonCaptureDialog reasonCaptureDialog;
    private ArrayList<TaskDataBO> taskList = new ArrayList<>();
    private Context context;
    private TaskListAdapter taskListAdapter;
    private ActionBar actionBar;
    private HashMap<String, ArrayList<Object>> selectedFilterIds;
    private HashMap<String, ArrayList<FilterBo>> filterHashMapList;
    private ArrayList<String> menuList;
    private boolean isFilterEnabled;

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
        drawerLayout = view.findViewById(R.id.drawer_layout);
        drawer = view.findViewById(R.id.right_drawer);
        taskCreationFAB = view.findViewById(R.id.fab_create_task);
        taskBgView = view.findViewById(R.id.task_bg_view);
        tabLayout = view.findViewById(R.id.tabs);
        tabLayout.addOnTabSelectedListener(this);
        listHeaderLL = view.findViewById(R.id.list_header_ll);
        noDataTv = view.findViewById(R.id.no_data_tv);
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
            if (bundle.containsKey(TaskConstant.FROM_HOME_SCREEN)) {
                source = TaskConstant.SOURCE.HOME_SCREEN;
                isFromHomeSrc = true;
            }

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

        setUpDrawerView();

        if (TaskConstant.SOURCE.RETAILER == source) {
            mSelectedRetailerID = String.valueOf(taskPresenter.getRetailerID());
        }

        taskPresenter.fetchFilterList(isFromHomeSrc);

        addTabs();
        if (!taskPresenter.isNewTask())
            taskCreationFAB.setVisibility(View.GONE);
        else {
            taskCreationFAB.setOnClickListener(v -> {
                if (!isPreVisit)
                    goToNewTaskActivity();
            });
        }

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if (dy > 0 && taskCreationFAB.getVisibility() == View.VISIBLE) {
                    taskCreationFAB.animate().scaleX(0).scaleY(0).setDuration(400).start();
                } else if (dy < 0) {
                    taskCreationFAB.animate().scaleX(1).scaleY(1).setDuration(400).start();
                }
            }
        });

    }

    @Override
    public void onResume() {
        super.onResume();

    }

    // Add tabs to Tablayout
    private void addTabs() {

        String[] reason = getResources().getStringArray(
                R.array.task_tab_header);

        if (taskPresenter.isShowServerTaskOnly()) {
            reason = new String[2];
            reason[0] = getString(R.string.all);
            reason[1] = getString(R.string.assigned);
        }

        for (String tab_name : reason) {
            tabLayout.addTab(tabLayout.newTab().setText(tab_name));
        }
    }

    private void setUpDrawerView() {

        int width = getResources().getDisplayMetrics().widthPixels;
        DrawerLayout.LayoutParams params = (DrawerLayout.LayoutParams) drawer.getLayoutParams();
        //  params.width = width;
        drawer.setLayoutParams(params);

        drawerLayout.setDrawerShadow(R.drawable.drawer_shadow,
                GravityCompat.START);
        drawerLayout.setDrawerShadow(R.drawable.drawer_shadow,
                GravityCompat.END);
        drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);

        ActionBarDrawerToggle mDrawerToggle = new ActionBarDrawerToggle(getActivity(),
                drawerLayout,
                R.string.ok,
                R.string.close
        ) {
            public void onDrawerClosed(View view) {
                if (drawerLayout != null)
                    setScreenTitle(screenTitle);

                getActivity().supportInvalidateOptionsMenu();
            }

            public void onDrawerOpened(View drawerView) {
                if (actionBar != null)
                    setScreenTitle(getResources().getString(R.string.filter));

                getActivity().supportInvalidateOptionsMenu();
            }
        };

        drawerLayout.addDrawerListener(mDrawerToggle);
    }

    private void setUpBottomSheet(View view) {
        RecyclerView bottomRecyclerView = view.findViewById(R.id.sort_recycler_view);
        bottomRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        bottomRecyclerView.setItemAnimator(new DefaultItemAnimator());
        bottomRecyclerView.setHasFixedSize(false);
        bottomRecyclerView.setNestedScrollingEnabled(false);
        bottomRecyclerView.addItemDecoration(new DividerItemDecoration(getActivity(), DividerItemDecoration.VERTICAL));
        bottomRecyclerView.setAdapter(new BottomSortListAdapter(getActivity(), getResources().getStringArray(R.array.task_sort_list), this, lastSelectedPos));

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
                        taskCreationFAB.animate().scaleX(0).scaleY(0).setDuration(400).start();
                        break;
                    case BottomSheetBehavior.STATE_HIDDEN:
                        taskBgView.setVisibility(View.GONE);
                        taskCreationFAB.animate().scaleX(1).scaleY(1).setDuration(400).start();
                        break;
                    case BottomSheetBehavior.STATE_SETTLING:
                        break;
                }
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {
            }
        });


    }


    private void hideBottomSheet() {
        bottomSheetBehavior.setHideable(true);
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
    }


    @Override
    public void onTaskExecutedClick(TaskDataBO taskDataBO) {
        if (source == TaskConstant.SOURCE.RETAILER) {
            taskPresenter.updateModuleTime();
        }
        taskPresenter.updateTaskExecution(taskDataBO, 0);
    }

    @Override
    public void onTaskButtonClick(TaskDataBO taskBO, int actionMode, int selectedListPos) {
        if (taskBO.getUsercreated().equals("0") && actionMode != 0) {
            showMessage(R.string.server_task_can_not_be_edit);
            return;
        } else if (taskBO.isChecked() && actionMode != 0) {
            showMessage(R.string.exec_task_not_allow_to_edit);
            return;
        }

        switch (actionMode) {
            case TaskConstant.TASK_DETAIL:
                taskListLastSelectedPos = selectedListPos;
                navToTaskDetailActivity(taskBO);
                break;
            case TaskConstant.TASK_EDIT:
                navToTaskCreationActivity(taskBO);
                break;
            case TaskConstant.TASK_DELETE:
                taskListLastSelectedPos = selectedListPos;
                showDeleteAlertDialog(taskBO);
                break;
        }
    }


    private void showDeleteAlertDialog(TaskDataBO deleteBo) {
        showAlert("", getString(R.string.do_you_want_to_delete_the_task)
                , new CommonDialog.PositiveClickListener() {
                    @Override
                    public void onPositiveButtonClick() {
                        taskPresenter.deleteTask(deleteBo.getTaskId(), deleteBo.getTaskOwner(), deleteBo.getServerTask());
                    }
                }, new CommonDialog.negativeOnClickListener() {
                    @Override
                    public void onNegativeButtonClick() {

                    }
                });
    }


    private void navToTaskDetailActivity(TaskDataBO taskBO) {
        Intent i = new Intent(getActivity(), TaskDetailActivity.class);
        if (taskBO.getTaskEvidenceImg() == null
                && tabLayout.getSelectedTabPosition() == 3)
            i.putExtra(TaskConstant.EVIDENCE_IMAGE, imageName);

        i.putExtra(TaskConstant.MENU_CODE, menuCode);
        i.putExtra(TaskConstant.SCREEN_TITLE, screenTitle);
        i.putExtra(TaskConstant.FROM_HOME_SCREEN, isFromHomeSrc);
        i.putExtra(TaskConstant.TAB_SELECTION, tabLayout.getSelectedTabPosition());

        if (taskBO.getRid() != 0)
            i.putExtra(TaskConstant.RETAILER_WISE_TASK, true);
        else
            i.putExtra(TaskConstant.RETAILER_WISE_TASK, (source == TaskConstant.SOURCE.RETAILER));

        i.putExtra(TaskConstant.TASK_OBJECT, taskBO);
        if (isPreVisit)
            i.putExtra(TaskConstant.TASK_PRE_VISIT, true);

        startActivityForResult(i, TaskConstant.TASK_UPDATED_SUCCESS_CODE);
    }

    private void navToTaskCreationActivity(TaskDataBO taskBO) {
        Intent i = new Intent(getActivity(), TaskCreationActivity.class);
        i.putExtra(TaskConstant.MENU_CODE, menuCode);
        i.putExtra(TaskConstant.SCREEN_TITLE, screenTitle);
        i.putExtra(TaskConstant.FROM_HOME_SCREEN, isFromHomeSrc);
        i.putExtra(TaskConstant.TASK_SCREEN_MODE, TaskConstant.EDIT_MODE_FROM_TASK_FRAGMENT_SRC);
        if (isPreVisit)
            i.putExtra(TaskConstant.TASK_PRE_VISIT, true);

        if (taskBO.getRid() != 0)
            i.putExtra(TaskConstant.RETAILER_WISE_TASK, true);
        else
            i.putExtra(TaskConstant.RETAILER_WISE_TASK, (source == TaskConstant.SOURCE.RETAILER));

        i.putExtra(TaskConstant.TASK_OBJECT, taskBO);
        startActivityForResult(i, TaskConstant.TASK_CREATED_SUCCESS_CODE);
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
        taskPresenter.orderBySortList(taskList, sortType, orderByAsc);
        lastSelectedPos = sortType;
        hideBottomSheet();
    }

    @Override
    public void showTaskNoReasonDialog(int taskListLastSelectedPos) {
        this.taskListLastSelectedPos = taskListLastSelectedPos;
        if (!taskPresenter.fetchNotCompletedTaskReasons().isEmpty()) {
            if (reasonCaptureDialog == null)
                reasonCaptureDialog = new ReasonCaptureDialog
                        .ReasonCaptureDialogBuilder(getActivity(), taskPresenter.fetchNotCompletedTaskReasons())
                        .setDialogTitles(getString(R.string.would_you_like_add_reason_to_close_task), getString(R.string.select_reason))
                        .setOnButtonClickListener(this)
                        .buildDialog();

            reasonCaptureDialog.show();
            WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
            Window window = reasonCaptureDialog.getWindow();
            lp.copyFrom(window != null ? window.getAttributes() : null);
            lp.width = DeviceUtils.getDisplayMetrics(context).widthPixels - 100;
            if (window != null) {
                window.setAttributes(lp);
            }
        } else
            showMessage(getString(R.string.data_not_mapped));
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
                TaskConstant.CAMERA_REQUEST_CODE);
    }


    private void setUpActionBar() {
        actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
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

        if (isFromHomeSrc)
            updateOptionMenus(tab.getPosition());
        else
            getActivity().invalidateOptionsMenu();

        if (taskPresenter.isShowServerTaskOnly()) {
            getView().findViewById(R.id.tv_execution).setVisibility(View.VISIBLE);
            getTaskListItem(tab.getPosition(), false);
        } else if (tab.getPosition() == 3) {
            getView().findViewById(R.id.tv_execution).setVisibility(View.GONE);
            taskPresenter.fetchCompletedTask(mSelectedRetailerID);
        } else if (tab.getPosition() == 4) {
            getView().findViewById(R.id.tv_execution).setVisibility(View.VISIBLE);
            getTaskListItem(tab.getPosition(), true);
        } else {
            getView().findViewById(R.id.tv_execution).setVisibility(View.VISIBLE);
            getTaskListItem(tab.getPosition(), false);
        }
    }

    @Override
    public void onTabUnselected(TabLayout.Tab tab) {

    }

    @Override
    public void onTabReselected(TabLayout.Tab tab) {

    }


    private void updateOptionMenus(int tabPos) {

        if (tabPos == 0
                && !isFilterEnabled) {
            isFilterEnabled = true;
            getActivity().invalidateOptionsMenu();
        } else {
            if (isFilterEnabled) {
                isFilterEnabled = false;
                getActivity().invalidateOptionsMenu();
            }
        }
    }


    @Override
    public void updateLabelNames(HashMap<String, String> labelMap) {

    }

    public void getTaskListItem(int tapPos, boolean isDelegate) {
        int userCreated = tapPos == 1 ? 0 : 1;
        if (taskPresenter.isShowServerTaskOnly())
            userCreated = 0;

        taskPresenter.getTaskListData(tapPos, userCreated, mSelectedRetailerID, isFromHomeSrc, isChannelWise, isDelegate);
    }

    @Override
    public void updateListData(ArrayList<TaskDataBO> updatedList) {
        noDataTv.setVisibility(View.GONE);
        handleVisibilty(View.VISIBLE);

        taskList.clear();
        taskList.addAll(updatedList);
        taskListAdapter = new TaskListAdapter(getActivity(), taskList, taskPresenter.outDateFormat(), this, source, taskPresenter.isShowProdLevel(), tabLayout.getSelectedTabPosition(), isPreVisit,isFromHomeSrc);
        recyclerView.setAdapter(taskListAdapter);

        if (bottomSheetBehavior.getState() == BottomSheetBehavior.STATE_EXPANDED)
            hideBottomSheet();
    }

    @Override
    public void showImageUpdateMsg() {
        showMessage(R.string.image_saved);
    }

    @Override
    public void onDeleteSuccess() {
        removeAndRefreshAdapter();
        showMessage(R.string.deleted_sucessfully);
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
        boolean drawerOpen = false;

        if (drawerLayout != null) {
            drawerOpen = drawerLayout.isDrawerOpen(GravityCompat.END);
        }

        menu.findItem(R.id.menu_notification).setVisible(true);
        menu.findItem(R.id.menu_save).setVisible(false);
        menu.findItem(R.id.menu_sort).setVisible(true);
        menu.findItem(R.id.menu_new_task).setVisible(false);
        if (isFromHomeSrc)
            menu.findItem(R.id.menu_filter).setVisible(isFilterEnabled);
        else
            menu.findItem(R.id.menu_filter).setVisible(true);

        if (source == TaskConstant.SOURCE.RETAILER)//this is applicable for store wise task
            menu.findItem(R.id.menu_reason).setVisible(taskPresenter.isNoTaskReason());

        if (drawerOpen) {
            menu.clear();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int i1 = item.getItemId();
        if (i1 == android.R.id.home) {
            if (drawerLayout.isDrawerOpen(GravityCompat.END))
                drawerLayout.closeDrawers();
            else
                backNavigation();
        } else if (i1 == R.id.menu_reason) {
            if (!isPreVisit)
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
        } else if (i1 == R.id.menu_filter) {
            showFilter();
        }
        return super.onOptionsItemSelected(item);
    }

    // Comment by Gp, Issue while going back from Activity Menu
    private void backNavigation() {

        Intent intent = new Intent(getActivity(), HomeScreenTwo.class);

        if (isPreVisit)
            intent.putExtra(TaskConstant.TASK_PRE_VISIT, true);

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
        startActivityForResult(i, TaskConstant.TASK_CREATED_SUCCESS_CODE);
    }

    private void showFilter() {
        try {

            drawerLayout.openDrawer(GravityCompat.END);

            android.support.v4.app.FragmentManager fm = getActivity().getSupportFragmentManager();
            FilterFragment frag = (FilterFragment) fm
                    .findFragmentByTag("filter");
            FragmentTransaction ft = fm
                    .beginTransaction();
            if (frag != null)
                ft.detach(frag);
            Bundle bundle = new Bundle();
            bundle.putSerializable("SelectedFilterList", selectedFilterIds);
            bundle.putSerializable("hashList", filterHashMapList);
            bundle.putSerializable("menuList", menuList);
            bundle.putBoolean(TaskConstant.FROM_HOME_SCREEN, isFromHomeSrc);

            // set FragmentClass Arguments
            FilterFragment mFragment = new FilterFragment();
            mFragment.setArguments(bundle);
            mFragment.setFilterViewListener(this);
            ft.replace(R.id.right_drawer, mFragment, "filter");
            ft.commit();
        } catch (Exception e) {
            Commons.printException(e + "");
        }
    }

    // This dialog used for when task was not completed
    private void goToNoTaskReasonDialog() {
        ReasonPhotoDialog dialog = new ReasonPhotoDialog();
        dialog.setOnDismissListener(dialog1 -> {
            if (taskPresenter.isNPPhotoReasonAvailable(String.valueOf(taskPresenter.getRetailerID()), "MENU_TASK")) {

                Intent intent = new Intent(getActivity(), HomeScreenTwo.class);

                if (isPreVisit)
                    intent.putExtra(TaskConstant.TASK_PRE_VISIT, true);

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
        if (requestCode == TaskConstant.CAMERA_REQUEST_CODE) {
            if (resultCode == 1) {
                taskPresenter.updateTaskExecutionImg(imageName, mSelectedTaskId, false);
            } else {
                imageName = "";
            }

        } else if (requestCode == TaskConstant.TASK_UPDATED_SUCCESS_CODE) {
            if (resultCode == 2) {
                removeAndRefreshAdapter();
            } else if (resultCode == TaskConstant.TASK_CREATED_SUCCESS_CODE) {
                getTaskListItem(tabLayout.getSelectedTabPosition(), tabLayout.getSelectedTabPosition() == 4);
            } else {
                if (data != null
                        && data.getExtras().containsKey(TaskConstant.TASK_EXECUTE_RESPONSE)) {
                    taskList.get(taskListLastSelectedPos).setChecked(data.getBooleanExtra(TaskConstant.TASK_EXECUTE_RESPONSE, false));
                    taskListAdapter.notifyItemChanged(taskListLastSelectedPos, taskList.get(taskListLastSelectedPos));
                }
            }
        } else if (requestCode == TaskConstant.TASK_CREATED_SUCCESS_CODE
                && resultCode == 3) {
            getTaskListItem(tabLayout.getSelectedTabPosition(), tabLayout.getSelectedTabPosition() == 4);
        }
        super.onActivityResult(requestCode, resultCode, data);
    }


    @Override
    public void showTaskUpdateMsg() {
        showMessage(R.string.task_updated_successfully);
    }

    @Override
    public void showTaskReasonUpdateMsg() {
        showMessage(getString(R.string.task_reason_updated_successfully));
        removeAndRefreshAdapter();
        reasonCaptureDialog.dismiss();
    }

    @Override
    public void showDataNotMappedMsg() {
        noDataTv.setVisibility(View.VISIBLE);
        handleVisibilty(View.GONE);
    }

    @Override
    public void setUpFilterList(HashMap<String, ArrayList<FilterBo>> filterListHashMap) {
        if (!filterListHashMap.isEmpty()) {
            filterHashMapList = new HashMap<>();
            filterHashMapList.putAll(filterListHashMap);
        }
    }

    private void handleVisibilty(int visibility) {
        listHeaderLL.setVisibility(visibility);
        recyclerView.setVisibility(visibility);
    }

    private void removeAndRefreshAdapter() {
        taskList.remove(taskListLastSelectedPos);
        taskListAdapter.notifyItemRemoved(taskListLastSelectedPos);
        taskListAdapter.notifyItemRangeChanged(taskListLastSelectedPos, taskList.size());
    }

    public void setTaskViewListener(TaskViewListener listener) {
        this.taskViewListener = listener;
    }


    @Override
    public void addReason(int selectedResId) {
        taskPresenter.updateTaskExecution(taskList.get(taskListLastSelectedPos), selectedResId);
    }

    @Override
    public void onDismiss() {
        reasonCaptureDialog.dismiss();
    }

    @Override
    public void apply(ArrayList<String> menuList, HashMap<String, ArrayList<FilterBo>> filterListHashMap, HashMap<String, ArrayList<Object>> selectedIdList, String filerName) {
        drawerLayout.closeDrawers();
        this.selectedFilterIds = selectedIdList;
        this.menuList = new ArrayList<>();
        this.menuList.addAll(menuList);

        if (selectedFilterIds != null
                && !selectedFilterIds.isEmpty())
            taskPresenter.updateFilterListData(selectedIdList, filerName.equalsIgnoreCase("Retailer"));
    }

    @Override
    public void clearAll() {
        this.selectedFilterIds = new HashMap<>();
        this.menuList = new ArrayList<>();

        if (drawerLayout.isDrawerOpen(GravityCompat.END))
            drawerLayout.closeDrawers();

        taskPresenter.fetchFilterList(isFromHomeSrc);
        taskPresenter.updateFilterListData(new HashMap<>(), false);
    }
}
