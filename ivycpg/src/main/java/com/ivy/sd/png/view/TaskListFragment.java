package com.ivy.sd.png.view;

import android.content.Context;
import android.graphics.Paint;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.ivy.cpg.view.survey.SurveyHelperNew;
import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.bo.TaskDataBO;
import com.ivy.sd.png.commons.SDUtil;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.provider.ConfigurationMasterHelper;
import com.ivy.sd.png.util.Commons;
import com.ivy.sd.png.util.DateUtil;

import java.util.Vector;


public class TaskListFragment extends Fragment {

    private Vector<TaskDataBO> taskDataBOForAdapter = new Vector<>();
    private LinearLayout mTaskContainer;
    private Vector<TaskDataBO> taskDataBO;
    private String taskDes[][];
    private String mSelectedRetailerID = "0";
    private TextView mSelectedTaskTV;
    private boolean IsRetailerwisetask = false;
    private boolean fromReviewScreen = false;
    private boolean fromProfileScreen = false;
    private int tasktype = 0;
    private BusinessModel bmodel;
    private boolean bool;
    private boolean hide_new_menu = true;
    String[] chids;
    private SurveyHelperNew surveyHelperNew;

    @Override
    public void onStart() {
        super.onStart();
        bmodel = (BusinessModel) getActivity().getApplicationContext();
        bmodel.setContext(getActivity());
        surveyHelperNew = SurveyHelperNew.getInstance(getActivity());


    }

    static TaskListFragment init(int pos, boolean isRetailerwisetask, boolean fromReviewScreen, boolean fromProfileScreen) {
        TaskListFragment taskListFragment = new TaskListFragment();
        Bundle args = new Bundle();
        args.putInt("type", 0);
        args.putBoolean("isRetailer", isRetailerwisetask);
        args.putBoolean("fromReview", fromReviewScreen);
        args.putBoolean("fromProfileScreen",fromProfileScreen);
        taskListFragment.setArguments(args);
        return taskListFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        tasktype = getArguments().getInt("type");
        IsRetailerwisetask = getArguments().getBoolean("isRetailer");
        fromReviewScreen = getArguments().getBoolean("fromReview");
        fromProfileScreen = getArguments().getBoolean("fromProfileScreen");

        bmodel = (BusinessModel) getActivity().getApplicationContext();
        bmodel.setContext(getActivity());
        if (!bmodel.configurationMasterHelper.IS_NEW_TASK) {
            hideNewTaskMenu();
            // invalidateOptionsMenu();
        }

        if (IsRetailerwisetask) {
            hideNewTaskMenu();
            if (bmodel.getRetailerMasterBO() != null)
                if (bmodel.getRetailerMasterBO().getRetailerID().equals("null")) {
                    mSelectedRetailerID = "0";
                } else {
                    mSelectedRetailerID = (bmodel.getRetailerMasterBO()
                            .getRetailerID());
                }
            else
                mSelectedRetailerID = "0";
        }

        taskDataBO = bmodel.taskHelper.getTaskData(mSelectedRetailerID);


    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_task_list,
                container, false);
        mTaskContainer = rootView.findViewById(
                R.id.task_cintainer_ll);
        if (mTaskContainer != null)
            mTaskContainer.removeAllViews();

        taskDataBO = bmodel.taskHelper.getTaskData(mSelectedRetailerID);
        tasktype = getArguments().getInt("type");
        if (IsRetailerwisetask)
            if (surveyHelperNew.getChannelidForSurvey() != null && surveyHelperNew.getChannelidForSurvey().length() > 0) {
                bool = true;
                chids = surveyHelperNew.getChannelidForSurvey().split(",");
            }
        updateTasks(tasktype);
        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        Commons.print("TaskListFragment ," + " On Resume called ");
       /* if (getView() != null)
        mTaskContainer = getView().findViewById(
                R.id.task_cintainer_ll);
        if (mTaskContainer != null)
            mTaskContainer.removeAllViews();

        taskDataBO = bmodel.taskHelper.getTaskData(mSelectedRetailerID);
        tasktype = getArguments().getInt("type");
        if (IsRetailerwisetask)
            if (surveyHelperNew.getChannelidForSurvey() != null && surveyHelperNew.getChannelidForSurvey().length() > 0) {
                bool = true;
                chids = surveyHelperNew.getChannelidForSurvey().split(",");
            }
        updateTasks(tasktype);*/
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_task, menu);
    }

    // Called whenever we call invalidateOptionsMenu()
    @Override
    public void onPrepareOptionsMenu(Menu menu) {

        super.onPrepareOptionsMenu(menu);
        if (!hide_new_menu)
            menu.findItem(R.id.menu_new_task).setVisible(false);

    }

    private void updateTasks(int taskType) {
        taskDataBOForAdapter.clear();
        try {
            if (taskDataBO != null) {
                int size = taskDataBO.size();
                taskDes = new String[size][1];

                TaskDataBO taskData;
                for (int i = 0; i < size; i++) {
                    taskData = taskDataBO.elementAt(i);

                    if (IsRetailerwisetask) {
                        if (mSelectedRetailerID.equals(taskData.getRid() + "")) {
                            if (taskType == 1) { // server
                                if (taskData.getUsercreated().toUpperCase()
                                        .equals("0")) {
                                    taskDataBOForAdapter.add(taskData);
                                }
                            } else if (taskType == 2) { // user
                                if (taskData.getUsercreated().toUpperCase()
                                        .equals("1")) {
                                    taskDataBOForAdapter.add(taskData);
                                }
                            } else {
                                taskDataBOForAdapter.add(taskData);
                            }
                        } else if (bool) {
                            int chid;


                            for (int j = 0; j < chids.length; j++) {
                                chid = SDUtil.convertToInt(chids[j]);
                                if (taskData.getChannelId() == chid) {
                                    if (taskType == 1) { // server
                                        if (taskData.getUsercreated().toUpperCase()
                                                .equals("0")) {
                                            taskDataBOForAdapter.add(taskData);
                                        }
                                    } else if (taskType == 2) { // user
                                        if (taskData.getUsercreated().toUpperCase()
                                                .equals("1")) {
                                            taskDataBOForAdapter.add(taskData);
                                        }
                                    } else {
                                        taskDataBOForAdapter.add(taskData);
                                    }
                                }
                            }

                        }
                    } else {

                        if (taskData.getRid() == 0 && taskData.getChannelId() == 0 && (taskData.getUserId() == bmodel.userMasterHelper.getUserMasterBO().getUserid() || taskData.getUserId() == 0)) {

                            if (taskType == 1) { // server
                                if (taskData.getUsercreated().toUpperCase()
                                        .equals("0")) {
                                    taskDataBOForAdapter.add(taskData);
                                }
                            } else if (taskType == 2) { // user
                                if (taskData.getUsercreated().toUpperCase()
                                        .equals("1")) {
                                    taskDataBOForAdapter.add(taskData);
                                }
                            } else {
                                taskDataBOForAdapter.add(taskData);
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            Commons.printException(e);
        }
        mTaskContainer.removeAllViews();

        LayoutInflater inflater = getActivity().getLayoutInflater();

        View view = inflater.inflate(R.layout.row_task_heading, null);
        TextView task_tv = (TextView) view.findViewById(R.id.task_tv);
        task_tv.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
        TextView task_created = (TextView) view.findViewById(R.id.task_created_on);
        task_created.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
        TextView task_execution = (TextView) view.findViewById(R.id.task_execution);
        task_execution.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
        LinearLayout layoutTaskExecution = (LinearLayout) view.findViewById(R.id.layoutTaskExecution);
        LinearLayout layoutTaskHeader = (LinearLayout) view.findViewById(R.id.layoutTaskHeader);

        if(fromProfileScreen) {
            layoutTaskExecution.setVisibility(View.GONE);
            layoutTaskHeader.setVisibility(View.GONE);
        }
        else {
            layoutTaskExecution.setVisibility(View.VISIBLE);
            layoutTaskHeader.setVisibility(View.VISIBLE);
        }
        mTaskContainer.addView(view);

        if (taskDataBOForAdapter != null) {

            int size = taskDataBOForAdapter.size();
            taskDes = new String[size][1];
            int j = 0;
            for(int i=0;i<taskDataBOForAdapter.size();i++) {
                final ViewHolder holder = new ViewHolder();
                holder.taskBO = taskDataBOForAdapter.get(i);
                TaskDataBO task = holder.taskBO;

                View v = inflater.inflate(R.layout.row_task_title, null);

                holder.taskCB = (CheckBox) v.findViewById(R.id.task_title_CB);
                holder.taskTaskOwner = (TextView) v.findViewById(R.id.task_taskowner);
                holder.taskCreatedDate = (TextView) v.findViewById(R.id.task_createdOn);
                holder.layoutCB = (LinearLayout) v.findViewById(R.id.layoutCB);
                holder.layoutrow = (LinearLayout)v.findViewById(R.id.layoutBorder);
                if(fromProfileScreen) {
                    holder.layoutCB.setVisibility(View.GONE);
                    if (i % 2 == 0)
                        holder.layoutrow.setBackgroundColor(ContextCompat.getColor(getActivity(), R.color.white));
                    else
                        holder.layoutrow.setBackgroundColor(ContextCompat.getColor(getActivity(), R.color.history_list_bg));

                }
                else
                    holder.layoutCB.setVisibility(View.VISIBLE);
                holder.taskTaskOwner.setText(task.getTaskOwner());
                holder.taskCreatedDate.setText("" + DateUtil.convertFromServerDateToRequestedFormat(task.getCreatedDate(), ConfigurationMasterHelper.outDateFormat));

                if (task.getIsdone().equals("1") && !holder.taskBO.isUpload()) {
                    holder.taskCB.setChecked(true);
                    holder.taskBO.setChecked(true);
                } else {
                    holder.taskCB.setChecked(false);
                    holder.taskBO.setChecked(false);
                }

                holder.taskCB
                        .setOnCheckedChangeListener(new OnCheckedChangeListener() {

                            @Override
                            public void onCheckedChanged(
                                    CompoundButton buttonView, boolean isChecked) {
                                holder.taskBO.setChecked(isChecked);
                                bmodel.outletTimeStampHelper.updateTimeStampModuleWise(SDUtil.now(SDUtil.TIME));
                                if (IsRetailerwisetask) {
                                    bmodel.taskHelper.saveTask(bmodel
                                            .getRetailerMasterBO()
                                            .getRetailerID(), holder.taskBO);
                                } else {
                                    bmodel.taskHelper.saveTask(0 + "",
                                            holder.taskBO);
                                }
                            }
                        });

                holder.taskTitle = (TextView) v
                        .findViewById(R.id.task_title_tv);
                holder.taskTitle.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
                holder.taskCreatedDate.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
                holder.taskTaskOwner.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
                holder.taskTitle.setText(task.getTasktitle());
                holder.taskTitle.setOnClickListener(new OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        mSelectedTaskTV = holder.taskDescription;
                        mSelectedTaskTV.setVisibility(View.VISIBLE);
                    }
                });

                holder.taskDescription = (TextView) v
                        .findViewById(R.id.task_description_tv);
                holder.taskDescription.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
                holder.taskDescription.setText(task.getTaskDesc());
                if (holder.taskBO.isUpload() && task.getIsdone().equals("1")) {
                    holder.taskCB.setEnabled(false);
                    holder.taskTitle.setPaintFlags(holder.taskTitle.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                    holder.taskDescription.setPaintFlags(holder.taskDescription.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                } else {
                    holder.taskCB.setEnabled(true);
                    holder.taskTitle.setPaintFlags(holder.taskTitle.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
                    holder.taskDescription.setPaintFlags(holder.taskDescription.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
                }
                mTaskContainer.addView(v);

                taskDes[j++][0] = task.getTaskDesc();
            }
            mTaskContainer.invalidate();
        }
    }

    class ViewHolder {
        TaskDataBO taskBO;
        CheckBox taskCB;
        TextView taskTitle;
        TextView taskDescription;
        TextView taskTaskOwner;
        TextView taskCreatedDate;
        LinearLayout layoutCB;
        LinearLayout layoutrow;
    }

    public void hideNewTaskMenu() {
        hide_new_menu = false;
    }

}