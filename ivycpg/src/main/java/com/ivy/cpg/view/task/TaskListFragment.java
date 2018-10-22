package com.ivy.cpg.view.task;

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
import com.ivy.sd.png.commons.SDUtil;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.provider.ConfigurationMasterHelper;
import com.ivy.sd.png.util.Commons;
import com.ivy.sd.png.util.DateUtil;
import com.ivy.utils.FontUtils;

import java.util.Vector;


public class TaskListFragment extends Fragment {

    private Vector<TaskDataBO> taskDataBOForAdapter = new Vector<>();
    private LinearLayout mTaskContainer;
    private Vector<TaskDataBO> taskDataBO;
    private String mSelectedRetailerID = "0";
    private TextView mSelectedTaskTV;
    private boolean IsRetailerwisetask = false;
    private boolean fromProfileScreen = false;
    private int tasktype = 0;
    private BusinessModel bmodel;
    private boolean bool;
    private boolean hide_new_menu = true;
    String[] chids;
    private SurveyHelperNew surveyHelperNew;
    private TaskHelper taskHelper;

    @Override
    public void onStart() {
        super.onStart();
        bmodel = (BusinessModel) getActivity().getApplicationContext();
        bmodel.setContext(getActivity());
        taskHelper = TaskHelper.getInstance(getActivity());
        surveyHelperNew = SurveyHelperNew.getInstance(getActivity());
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        tasktype = getArguments().getInt("type");
        IsRetailerwisetask = getArguments().getBoolean("isRetailer");
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

        taskDataBO = taskHelper.getTaskData(mSelectedRetailerID);


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

        taskDataBO = taskHelper.getTaskData(mSelectedRetailerID);
        tasktype = getArguments().getInt("type");
        if (IsRetailerwisetask) {
            surveyHelperNew = SurveyHelperNew.getInstance(getActivity());
            String chnanelIdForSurvey = surveyHelperNew.getChannelidForSurvey();
            if (chnanelIdForSurvey != null)
                if (chnanelIdForSurvey.length() > 0) {
                    bool = true;
                    chids = chnanelIdForSurvey.split(",");
                }
        }
        updateTasks(tasktype);
        return rootView;
    }

    @Override
    public void onResume() {
        super.onResume();
        Commons.print("TaskListFragment ," + " On Resume called ");
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


                            for (String chid1 : chids) {
                                chid = SDUtil.convertToInt(chid1);
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
        TextView task_tv =  view.findViewById(R.id.task_tv);
        task_tv.setTypeface(FontUtils.getFontRoboto(FontUtils.FontType.MEDIUM,getActivity()));
        TextView task_created =  view.findViewById(R.id.task_created_on);
        task_created.setTypeface(FontUtils.getFontRoboto(FontUtils.FontType.MEDIUM,getActivity()));
        TextView task_execution =  view.findViewById(R.id.task_execution);
        task_execution.setTypeface(FontUtils.getFontRoboto(FontUtils.FontType.MEDIUM,getActivity()));
        LinearLayout layoutTaskExecution =  view.findViewById(R.id.layoutTaskExecution);
        LinearLayout layoutTaskHeader =  view.findViewById(R.id.layoutTaskHeader);

        if (fromProfileScreen) {
            layoutTaskExecution.setVisibility(View.GONE);
            layoutTaskHeader.setVisibility(View.GONE);
        } else {
            layoutTaskExecution.setVisibility(View.VISIBLE);
            layoutTaskHeader.setVisibility(View.VISIBLE);
        }
        mTaskContainer.addView(view);

        if (taskDataBOForAdapter != null) {

            for (int i = 0; i < taskDataBOForAdapter.size(); i++) {
                final ViewHolder holder = new ViewHolder();
                holder.taskBO = taskDataBOForAdapter.get(i);
                TaskDataBO task = holder.taskBO;

                View v = inflater.inflate(R.layout.row_task_title, null);

                holder.taskCB =  v.findViewById(R.id.task_title_CB);
                holder.taskTaskOwner =  v.findViewById(R.id.task_taskowner);
                holder.taskCreatedDate =  v.findViewById(R.id.task_createdOn);
                holder.layoutCB =  v.findViewById(R.id.layoutCB);
                holder.layoutrow =  v.findViewById(R.id.layoutBorder);
                if (fromProfileScreen) {
                    holder.layoutCB.setVisibility(View.GONE);
                    if (i % 2 == 0)
                        holder.layoutrow.setBackgroundColor(ContextCompat.getColor(getActivity(), R.color.white));
                    else
                        holder.layoutrow.setBackgroundColor(ContextCompat.getColor(getActivity(), R.color.history_list_bg));

                } else
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
                                    taskHelper.saveTask(bmodel
                                            .getRetailerMasterBO()
                                            .getRetailerID(), holder.taskBO);
                                } else {
                                    taskHelper.saveTask(0 + "",
                                            holder.taskBO);
                                }
                            }
                        });

                holder.taskTitle =  v
                        .findViewById(R.id.task_title_tv);
                holder.taskTitle.setTypeface(FontUtils.getFontRoboto(FontUtils.FontType.MEDIUM,getActivity()));
                holder.taskCreatedDate.setTypeface(FontUtils.getFontRoboto(FontUtils.FontType.MEDIUM,getActivity()));
                holder.taskTaskOwner.setTypeface(FontUtils.getFontRoboto(FontUtils.FontType.LIGHT,getActivity()));
                holder.taskTitle.setText(task.getTasktitle());
                holder.taskTitle.setOnClickListener(new OnClickListener() {

                    @Override
                    public void onClick(View v) {
                        mSelectedTaskTV = holder.taskDescription;
                        mSelectedTaskTV.setVisibility(View.VISIBLE);
                    }
                });

                holder.taskDescription =  v
                        .findViewById(R.id.task_description_tv);
                holder.taskDescription.setTypeface(FontUtils.getFontRoboto(FontUtils.FontType.LIGHT,getActivity()));
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