package com.ivy.ui.task.view;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import androidx.fragment.app.FragmentActivity;
import androidx.appcompat.widget.AppCompatImageView;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ExpandableListView;

import com.ivy.core.base.presenter.BasePresenter;
import com.ivy.core.base.view.BaseFragment;
import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.bo.RetailerMasterBO;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.ui.retailerplan.addplan.view.AddPlanDialogFragment;
import com.ivy.ui.task.TaskConstant;
import com.ivy.ui.task.TaskContract;
import com.ivy.ui.task.TaskViewListener;
import com.ivy.ui.task.adapter.TaskExpandableListAdapter;
import com.ivy.ui.task.di.DaggerTaskComponent;
import com.ivy.ui.task.di.TaskModule;
import com.ivy.ui.task.model.TaskDataBO;
import com.ivy.ui.task.model.TaskRetailerBo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;

import javax.inject.Inject;

public class TaskUnplannedFragment extends BaseFragment implements TaskContract.TaskUnplannedView, TaskExpandableListAdapter.onClickListener {

    @Inject
    TaskContract.TaskPresenter<TaskContract.TaskView> taskPresenter;

    private String screenTitle;
    private ExpandableListView taskExpandableListView;
    private TaskViewListener taskViewListener;
    private TaskExpandableListAdapter taskExpandableListAdapter;
    private Context mContext;


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.mContext = context;
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
        return R.layout.task_unplanned_fragment;
    }

    @Override
    public void init(View view) {
        taskExpandableListView = view.findViewById(R.id.task_exp_list);
    }

    @Override
    protected void getMessageFromAliens() {
        Bundle bundle = getArguments();
        if (bundle == null)
            bundle = Objects.requireNonNull(getActivity()).getIntent().getExtras();

        if (bundle.containsKey(TaskConstant.SCREEN_TITLE))
            screenTitle = bundle.getString(TaskConstant.SCREEN_TITLE, getString(R.string.pending_task));
    }

    @Override
    protected void setUpViews() {
        setUpToolbar(getString(R.string.pending_task));

        taskExpandableListView.setOnGroupClickListener((parent, v, groupPosition, l) -> {
            parent.smoothScrollToPosition(groupPosition);

            if (parent.isGroupExpanded(groupPosition)) {
                AppCompatImageView imageView = v.findViewById(R.id.child_img_view);
                imageView.setImageDrawable(getResources().getDrawable(R.drawable.ic_action_down));
            } else {
                AppCompatImageView imageView = v.findViewById(R.id.child_img_view);
                imageView.setImageDrawable(getResources().getDrawable(R.drawable.ic_action_up));
            }
            return false;
        });

    }

    @Override
    public void onResume() {
        super.onResume();
        setHasOptionsMenu(true);
        taskPresenter.fetchUnPlannedTask();
    }

    @Override
    public void updateLabelNames(HashMap<String, String> labelMap) {

    }

    @Override
    public void updateListData(ArrayList<TaskDataBO> updatedList) {

    }

    @Override
    public void showImageUpdateMsg() {

    }

    @Override
    public void onDeleteSuccess() {

    }

    @Override
    public void showErrorMsg() {
        showMessage(mContext.getString(R.string.something_went_wrong));
    }

    @Override
    public void updateUnplannedTaskList(ArrayList<TaskRetailerBo> retailerMasterBOS, HashMap<String, ArrayList<TaskDataBO>> taskHashMapList) {
        taskExpandableListAdapter = new TaskExpandableListAdapter(mContext, retailerMasterBOS, taskHashMapList, this);
        taskExpandableListView.setAdapter(taskExpandableListAdapter);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_task, menu);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        menu.findItem(R.id.menu_close).setVisible(true);
        menu.findItem(R.id.menu_notification).setVisible(false);
        menu.findItem(R.id.menu_save).setVisible(false);
        menu.findItem(R.id.menu_sort).setVisible(false);
        menu.findItem(R.id.menu_new_task).setVisible(false);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.menu_close) {
            taskViewListener.switchTaskView(false);
        }
        return super.onOptionsItemSelected(item);
    }

    public void setTaskViewListener(TaskViewListener listener) {
        this.taskViewListener = listener;
    }

    @Override
    public void onAddBtnClick(TaskRetailerBo retBo) {
        RetailerMasterBO retailerBo = taskPresenter.getRetailerMasterBo(retBo.getRetailerId());
        if (retailerBo != null) {
            AddPlanDialogFragment addPlanDialogFragment = new AddPlanDialogFragment(null, retailerBo,
                    null, new ArrayList<>());
            addPlanDialogFragment.show(((FragmentActivity) mContext).getSupportFragmentManager(),
                    "add_plan_fragment");
        } else {
            showMessage(getString(R.string.data_not_mapped));
        }
    }

    @Override
    public void navigateDetailSrc(TaskDataBO detailBo) {

        Intent i = new Intent(mContext, TaskDetailActivity.class);
        i.putExtra(TaskConstant.SCREEN_TITLE, screenTitle);
        i.putExtra(TaskConstant.RETAILER_WISE_TASK, true);
        i.putExtra(TaskConstant.TASK_NOTIFICATION_SRC, true);
        i.putExtra(TaskConstant.TASK_OBJECT, detailBo);
        startActivity(i);
    }
}
