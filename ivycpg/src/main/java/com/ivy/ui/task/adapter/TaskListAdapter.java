package com.ivy.ui.task.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Paint;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.ivy.cpg.view.task.TaskDataBO;
import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.util.DateUtil;

import java.util.ArrayList;

public class TaskListAdapter extends RecyclerView.Adapter<TaskListAdapter.TaskListViewHolder> {

    private final ArrayList<TaskDataBO> taskDatas;
    private Context mContext;
    private String outDateFormat;
    private TaskClickListener taskClickListener;

    public TaskListAdapter(ArrayList<TaskDataBO> taskDatas, Context mContext, String outDateFormat, TaskClickListener taskClickListener) {
        this.taskDatas = taskDatas;
        this.mContext = mContext;
        this.outDateFormat = outDateFormat;
        this.taskClickListener = taskClickListener;
    }


    @NonNull
    @Override
    public TaskListAdapter.TaskListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.row_task_title, parent, false);
        return new TaskListViewHolder(itemView);
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull TaskListAdapter.TaskListViewHolder holder, int position) {
        TaskDataBO taskBo = taskDatas.get(position);

        holder.taskTaskOwner.setText(" : " + taskBo.getTaskOwner());
        holder.taskCreatedDate.setText(DateUtil.convertFromServerDateToRequestedFormat
                (taskBo.getCreatedDate(), outDateFormat) + ", ");

        holder.taskTitle.setText(taskBo.getTasktitle());
        holder.taskDescription.setText(taskBo.getTaskDesc());

        holder.layoutrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!holder.taskCB.isChecked()) {
                    holder.taskCB.setChecked(true);
                    taskBo.setChecked(true);
                } else {
                    holder.taskCB.setChecked(false);
                    taskBo.setChecked(false);
                }
                taskClickListener.onRowClick(taskBo);
            }
        });

        if (taskBo.isUpload() && taskBo.getIsdone().equals("1")) {
            holder.taskCB.setEnabled(false);
            holder.taskTitle.setPaintFlags(holder.taskTitle.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            holder.taskDescription.setPaintFlags(holder.taskDescription.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
        } else {
            holder.taskCB.setEnabled(true);
            holder.taskTitle.setPaintFlags(holder.taskTitle.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
            holder.taskDescription.setPaintFlags(holder.taskDescription.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
        }

        if (taskBo.getIsdone().equals("1") && !taskBo.isUpload()) {
            holder.taskCB.setChecked(true);
            taskBo.setChecked(true);
        } else {
            holder.taskCB.setChecked(false);
            taskBo.setChecked(false);
        }
    }

    @Override
    public int getItemCount() {
        return taskDatas.size();
    }

    public class TaskListViewHolder extends RecyclerView.ViewHolder {
        CheckBox taskCB;
        TextView taskTitle;
        TextView taskDescription;
        TextView taskTaskOwner;
        TextView taskCreatedDate;
        RelativeLayout layoutCB;
        RelativeLayout layoutrow;

        public TaskListViewHolder(View itemView) {
            super(itemView);

            taskCB = itemView.findViewById(R.id.task_title_CB);
            taskTitle = itemView.findViewById(R.id.task_title_tv);
            taskDescription = itemView.findViewById(R.id.task_description_tv);
            taskTaskOwner = itemView.findViewById(R.id.task_taskowner);
            taskCreatedDate = itemView.findViewById(R.id.task_createdOn);
            layoutCB = itemView.findViewById(R.id.layoutCB);
            layoutrow = itemView.findViewById(R.id.layoutBorder);

        }
    }


    public interface TaskClickListener {
        void onRowClick(TaskDataBO taskBO);
    }
}
