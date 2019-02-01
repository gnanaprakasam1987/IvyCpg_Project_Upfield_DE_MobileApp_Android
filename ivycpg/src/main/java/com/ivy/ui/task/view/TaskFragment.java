package com.ivy.ui.task.view;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.ivy.core.base.view.BaseFragment;
import com.ivy.cpg.view.task.TaskCreation;
import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.bo.ChannelBO;
import com.ivy.sd.png.bo.RetailerMasterBO;
import com.ivy.sd.png.bo.UserMasterBO;
import com.ivy.sd.png.view.HomeScreenTwo;
import com.ivy.sd.png.view.ReasonPhotoDialog;
import com.ivy.ui.task.TaskContract;

import java.util.ArrayList;
import java.util.Objects;
import java.util.Vector;

import javax.inject.Inject;

public class TaskFragment extends BaseFragment implements TaskContract.TaskView {

    private LinearLayout mTaskContainer;
    private TabLayout tabLayout;
    private Bundle bundle;
    private boolean IsRetailerwisetask;
    private boolean fromHomeScreen;
    private String mSelectedRetailerID = "0";
    @Inject
    TaskContract.TaskPresenter<TaskContract.TaskView> taskPresenter;

    @Override
    public void initializeDi() {

    }

    @Override
    protected int setContentViewLayout() {
        return R.layout.task_fragment;
    }

    @Override
    public void initVariables(View view) {
        mTaskContainer = view.findViewById(R.id.task_cintainer_ll);

        if (taskPresenter.isMoveNextActivity())
            moveNextActivity(view);

    }

    @Override
    protected void getMessageFromAliens() {

    }

    @Override
    protected void setUpViews() {
        /*if (mTaskContainer != null)
            mTaskContainer.removeAllViews();*/
        bundle = getArguments();
        if (bundle == null)
            bundle = Objects.requireNonNull(getActivity()).getIntent().getExtras();

        String[] reason = Objects.requireNonNull(getActivity()).getResources().getStringArray(
                R.array.task_tab_header);

        if (taskPresenter.isShowServerTaskOnly()) {
            reason = new String[1];
            reason[0] = "All";
        }

        // Add tabs to Tablayout
        for (String tab_name : reason) {
            @SuppressLint("InflateParams")
            TextView tabOne = (TextView) LayoutInflater.from(getActivity()).inflate(R.layout.custom_tab, null);
            tabOne.setText(tab_name);
            tabLayout.addTab(tabLayout.newTab().setCustomView(tabOne));
        }


        if (bundle != null) {
            if (bundle.containsKey("IsRetailerwisetask")) {
                IsRetailerwisetask = bundle.getBoolean("IsRetailerwisetask");
            }

            if (bundle.containsKey("fromHomeScreen")) {
                fromHomeScreen = bundle.getBoolean("fromHomeScreen");
            }
        }

        if (IsRetailerwisetask) {
            if (taskPresenter.getRetailerID() == 0) {
                mSelectedRetailerID = "0";
            } else {
                mSelectedRetailerID = String.valueOf(taskPresenter.getRetailerID());
            }
        }

    }


    private void moveNextActivity(View view) {
        LinearLayout footer = view.findViewById(R.id.footer);
        footer.setVisibility(View.VISIBLE);

        Button btnClose = view.findViewById(R.id.btn_close);
        btnClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
    }

    private void setUpActionBar() {
        ActionBar actionBar = ((AppCompatActivity) Objects.requireNonNull(getActivity())).getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowTitleEnabled(false);
            actionBar.setDisplayShowHomeEnabled(true);

            setScreenTitle(getString(R.string.task));
        }

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

    @Override
    public void showUpdatedDialog() {

    }

    @Override
    public void updateListData() {

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

        } else if (i1 == R.id.menu_new_task) {
            taskPresenter.updateModuleTime();
            Intent i = new Intent(getActivity(), TaskCreation.class);
            i.putExtra("fromHomeScreen", fromHomeScreen);
            i.putExtra("IsRetailerwisetask", IsRetailerwisetask);
            i.putExtra("screentitle", bundle.containsKey("screentitle") ? bundle.getString("screentitle") : getResources().
                    getString(R.string.task));
            startActivity(i);
            getActivity().finish();
        } else if (i1 == R.id.menu_reason) {
            ReasonPhotoDialog dialog = new ReasonPhotoDialog();
            dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialog) {
                    /*if (bmodel.reasonHelper.isNpReasonPhotoAvaiable(bmodel.retailerMasterBO.getRetailerID(), "MENU_TASK")) {
                        if (!fromHomeScreen) {
                            taskPresenter.saveModuleCompletion("MENU_TASK");
                            startActivity(new Intent(getActivity(),
                                    HomeScreenTwo.class));
                            getActivity().finish();

                        }
                    }*/
                }
            });
            Bundle args = new Bundle();
            args.putString("modulename", "MENU_TASK");
            dialog.setCancelable(false);
            dialog.setArguments(args);
            dialog.show(getActivity().getSupportFragmentManager(), "ReasonDialogFragment");
        }

        return super.onOptionsItemSelected(item);
    }
}
