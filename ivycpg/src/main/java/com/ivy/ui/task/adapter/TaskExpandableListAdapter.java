package com.ivy.ui.task.adapter;

import android.content.Context;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatImageView;
import android.support.v7.widget.AppCompatTextView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;

import com.ivy.sd.png.asean.view.R;
import com.ivy.ui.task.TaskConstant;
import com.ivy.ui.task.model.TaskDataBO;
import com.ivy.ui.task.model.TaskRetailerBo;
import com.ivy.utils.DateTimeUtils;

import java.util.ArrayList;
import java.util.HashMap;

public class TaskExpandableListAdapter extends BaseExpandableListAdapter {

    private Context mContext;
    private ArrayList<TaskRetailerBo> taskRetailerListBo;
    private HashMap<String, ArrayList<TaskDataBO>> taskListHashMap;
    private onClickListener onClickListener;
    private String taskNameStr;
    private String dueDateStr;

    public TaskExpandableListAdapter(Context mContext, ArrayList<TaskRetailerBo> taskRetailerListBo, HashMap<String, ArrayList<TaskDataBO>> taskListHashMap, onClickListener onClickListener) {
        this.mContext = mContext;
        this.taskRetailerListBo = taskRetailerListBo;
        this.taskListHashMap = taskListHashMap;
        this.onClickListener = onClickListener;
        taskNameStr = mContext.getString(R.string.task);
        dueDateStr = mContext.getString(R.string.next_visit_date) + "  ";
    }

    @Override
    public int getGroupCount() {
        return taskListHashMap.size();
    }

    @Override
    public int getChildrenCount(int groupPosition) {

        if (taskListHashMap != null)
            return taskListHashMap.get(taskRetailerListBo.get(groupPosition).getRetailerId()).size();
        else
            return 0;
    }

    @Override
    public Object getGroup(int position) {
        return taskRetailerListBo.get(position);
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        String retId = taskRetailerListBo.get(groupPosition).getRetailerId();
        return taskListHashMap.get(retId).get(childPosition);
    }

    @Override
    public long getGroupId(int groupPostion) {
        return groupPostion;
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public View getGroupView(final int groupPosition, final boolean isExpanded, final View convertView, final ViewGroup parent) {
        TaskRetailerBo retBo = (TaskRetailerBo) getGroup(groupPosition);
        final GroupViewHolder groupViewHolder;
        View groupRow = convertView;
        if (groupRow == null) {
            LayoutInflater inflater = ((AppCompatActivity) mContext).getLayoutInflater();
            groupRow = inflater
                    .inflate(R.layout.row_group_unplanned_task, parent, false);
            groupViewHolder = new GroupViewHolder();

            groupViewHolder.retailerNameTv = groupRow.findViewById(R.id.retailer_name_tv);
            groupViewHolder.retailerAddressTv = groupRow.findViewById(R.id.retailer_address_tv);
            groupViewHolder.nextRetailerVisitTv = groupRow.findViewById(R.id.nex_visit_date_tv);
            groupViewHolder.addPlaneImgView = groupRow.findViewById(R.id.add_plan_btn);
            groupViewHolder.expandChildViewTv = groupRow.findViewById(R.id.child_view_arrow_tv);

            groupRow.setTag(groupViewHolder);
        } else {
            groupViewHolder = (GroupViewHolder) groupRow.getTag();
        }

        groupViewHolder.retailerNameTv.setText(retBo.getRetailerName());

        groupViewHolder.retailerAddressTv.setText(retBo.getRetAddress());

        String nxtDueDateStr = dueDateStr + DateTimeUtils.convertDateTimeObjectToRequestedFormat(retBo.getLastVisitDate(), DateTimeUtils.DateFormats.SERVER_DATE_FORMAT, TaskConstant.TASK_DATE_FORMAT);

        groupViewHolder.nextRetailerVisitTv.setText(nxtDueDateStr);

        groupViewHolder.expandChildViewTv.setText(String.format(mContext.getString(R.string.next_visit_date_count), retBo.getNextVisitDaysCount()));

        groupViewHolder.addPlaneImgView.setOnClickListener(v -> onClickListener.onAddBtnClick(retBo));

        groupViewHolder.expandChildViewTv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

            }
        });

        return groupRow;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        TaskDataBO taskChildBo;

        taskChildBo = (TaskDataBO) getChild(groupPosition, childPosition);
        final ChildViewHolder holder;
        View row = convertView;
        if (row == null) {
            LayoutInflater inflater = ((AppCompatActivity) mContext).getLayoutInflater();
            row = inflater
                    .inflate(R.layout.row_child_unplanned_task, parent, false);
            holder = new ChildViewHolder();

            holder.taskNameTv = row.findViewById(R.id.task_name_tv);
            holder.taskDueDateTv = row.findViewById(R.id.task_due_date_tv);
            holder.taskTitleTv = row.findViewById(R.id.task_title_tv);
            row.setTag(holder);

        } else {
            holder = (ChildViewHolder) row.getTag();
        }

        String retNameStr = taskNameStr + String.valueOf(childPosition + 1);
        holder.taskNameTv.setText(retNameStr);
        holder.taskTitleTv.setText(taskChildBo.getTasktitle());

        holder.taskDueDateTv.setText(String.format(mContext.getString(R.string.due_in_next_days), taskChildBo.getNoOfDueDays()));

        row.setOnClickListener(v -> onClickListener.navigateDetailSrc(taskChildBo));

        return row;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return false;
    }

    private class GroupViewHolder {

        private AppCompatTextView retailerNameTv;
        private AppCompatTextView retailerAddressTv;
        private AppCompatTextView nextRetailerVisitTv;
        private AppCompatImageView addPlaneImgView;
        private AppCompatTextView expandChildViewTv;

    }


    class ChildViewHolder {

        private AppCompatTextView taskNameTv;
        private AppCompatTextView taskTitleTv;
        private AppCompatTextView taskDueDateTv;

    }


    public interface onClickListener {
        void onAddBtnClick(TaskRetailerBo retBo);

        void navigateDetailSrc(TaskDataBO detailBo);
    }
}
