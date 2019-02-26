package com.ivy.ui.task.view;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
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
import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.bo.ChannelBO;
import com.ivy.sd.png.bo.RetailerMasterBO;
import com.ivy.sd.png.bo.UserMasterBO;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.util.CommonDialog;
import com.ivy.sd.png.view.HomeScreenTwo;
import com.ivy.sd.png.view.ReasonPhotoDialog;
import com.ivy.ui.task.TaskContract;
import com.ivy.ui.task.adapter.TaskListAdapter;
import com.ivy.ui.task.di.DaggerTaskComponent;
import com.ivy.ui.task.di.TaskModule;

import java.util.ArrayList;
import java.util.Objects;
import java.util.Vector;

import javax.inject.Inject;

public class TaskFragment extends BaseFragment implements TaskContract.TaskView, TabLayout.OnTabSelectedListener {

    private TabLayout tabLayout;
    private RecyclerView recyclerView;
    private Button nxtBtn;
    private LinearLayout footerLL;
    private Bundle bundle;
    private boolean IsRetailerwisetask;
    private boolean fromHomeScreen;
    private boolean isFromSurvey;
    private boolean fromProfileScreen;
    private String mSelectedRetailerID = "0";


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
    public void initVariables(View view) {

        tabLayout = view.findViewById(R.id.tabs);
        tabLayout.addOnTabSelectedListener(this);
        footerLL = view.findViewById(R.id.footer);
        nxtBtn = view.findViewById(R.id.btn_close);
        recyclerView = view.findViewById(R.id.task_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setHasFixedSize(true);
    }

    @Override
    protected void getMessageFromAliens() {
        bundle = getArguments();
        if (bundle == null)
            bundle = Objects.requireNonNull(getActivity()).getIntent().getExtras();

        if (bundle != null) {
            if (bundle.containsKey("IsRetailerwisetask")) {
                IsRetailerwisetask = bundle.getBoolean("IsRetailerwisetask", false);
            }

            if (bundle.containsKey("fromHomeScreen")) {
                fromHomeScreen = bundle.getBoolean("fromHomeScreen", false);
            }
            if (bundle.containsKey("FromSurvey")) {
                isFromSurvey = bundle.getBoolean("FromSurvey", false);
            }
            if (bundle.containsKey("fromProfileScreen")) {
                fromProfileScreen = getArguments().getBoolean("fromProfileScreen", false);
            }
        }
    }

    @Override
    protected void setUpViews() {
        if (!fromProfileScreen)
            setUpActionBar();
        addTabs();

        if (taskPresenter.isMoveNextActivity()) {
            footerLL.setVisibility(View.VISIBLE);
            nxtBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    moveNextActivity();
                }
            });
        }

        if (IsRetailerwisetask) {
            if (taskPresenter.getRetailerID() == 0) {
                mSelectedRetailerID = "0";
            } else {
                mSelectedRetailerID = String.valueOf(taskPresenter.getRetailerID());
            }
        }


        if (taskPresenter.isShowServerTaskOnly())
            taskPresenter.updateTaskList(1, mSelectedRetailerID, IsRetailerwisetask, isFromSurvey);
        else
            taskPresenter.updateTaskList(0, mSelectedRetailerID, IsRetailerwisetask, isFromSurvey);

    }

    // Add tabs to Tablayout
    private void addTabs() {

        String[] reason = getActivity().getResources().getStringArray(
                R.array.task_tab_header);

        if (taskPresenter.isShowServerTaskOnly()) {
            reason = new String[1];
            reason[0] = "All";
        }

        for (String tab_name : reason) {
            @SuppressLint("InflateParams")
            TextView tabOne = (TextView) LayoutInflater.from(getActivity()).inflate(R.layout.custom_tab, null);
            tabOne.setText(tab_name);
            tabLayout.addTab(tabLayout.newTab().setCustomView(tabOne));
        }
    }

    private void moveNextActivity() {

        showAlert(getString(R.string.move_next_activity)
                , getString(R.string.ok), new CommonDialog.PositiveClickListener() {
                    @Override
                    public void onPositiveButtonClick() {
                        Intent intent = new Intent(getActivity(), HomeScreenTwo.class);

                        if (bundle != null) {
                            intent.putExtra("IsMoveNextActivity", true);
                            intent.putExtra("CurrentActivityCode", bundle.getString("CurrentActivityCode", ""));
                        }

                        startActivity(intent);
                        getActivity().finish();
                    }
                }, true);
    }

    TaskListAdapter.TaskClickListener taskClickListener = new TaskListAdapter.TaskClickListener() {
        @Override
        public void onRowClick(TaskDataBO taskBO) {

            taskPresenter.updateModuleTime();
            if (IsRetailerwisetask) {
                taskPresenter.updateTask(taskPresenter.getRetailerID() + "", taskBO);
            } else {
                taskPresenter.updateTask(0 + "", taskBO);
            }
        }
    };


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

        taskPresenter.updateTaskList(tab.getPosition(), mSelectedRetailerID, IsRetailerwisetask, isFromSurvey);
    }

    @Override
    public void onTabUnselected(TabLayout.Tab tab) {

    }

    @Override
    public void onTabReselected(TabLayout.Tab tab) {

    }

    @Override
    public void showUpdatedDialog() {
        showMessage(R.string.task_updated_successfully);
    }

    @Override
    public void updateListData(ArrayList<TaskDataBO> updatedList) {

        recyclerView.setAdapter(new TaskListAdapter(updatedList, getActivity(), taskPresenter.outDateFormat(), taskClickListener, fromProfileScreen));
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_task, menu);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        menu.findItem(R.id.menu_save).setVisible(false);
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
        }
        return super.onOptionsItemSelected(item);
    }

    // Comment by Gp, Issue while going back from Activity Menu
    private void backNavigation() {

        if (IsRetailerwisetask) {
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
        i.putExtra("fromHomeScreen", fromHomeScreen);
        i.putExtra("IsRetailerwisetask", IsRetailerwisetask);
        i.putExtra("screentitle", bundle.containsKey("screentitle") ? bundle.getString("screentitle") : getResources().
                getString(R.string.task));
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
                        taskPresenter.saveModuleCompletion("MENU_TASK");
                        startActivity(new Intent(getActivity(),
                                HomeScreenTwo.class));
                        getActivity().finish();

                    }
                }
            }
        });
        Bundle args = new Bundle();
        args.putString("modulename", "MENU_TASK");
        dialog.setCancelable(false);
        dialog.setArguments(args);
        dialog.show(getActivity().getSupportFragmentManager(), "ReasonDialogFragment");
    }

    @Override
    public void setTaskChannelListData(Vector<ChannelBO> channelList) {

    }

    @Override
    public void setTaskRetailerListData(ArrayList<RetailerMasterBO> retailerList) {

    }

    @Override
    public void setTaskUserListData(ArrayList<UserMasterBO> userList) {

    }

    @Override
    public String getTaskMode() {
        return null;
    }
}
