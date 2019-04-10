package com.ivy.ui.task.adapter;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Paint;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.ivy.cpg.view.task.TaskDataBO;
import com.ivy.sd.png.asean.view.R;
import com.ivy.ui.task.TaskClickListener;
import com.ivy.ui.task.TaskConstant;
import com.ivy.ui.task.view.SwipeRevealLayout;
import com.ivy.ui.task.view.ViewBinderHelper;
import com.ivy.utils.AppUtils;
import com.ivy.utils.DateTimeUtils;

import java.util.ArrayList;

public class TaskListAdapter extends RecyclerView.Adapter<TaskListAdapter.TaskListViewHolder> {

    private final ArrayList<TaskDataBO> taskDatas;
    private Context mContext;
    private String outDateFormat;
    private TaskClickListener taskClickListener;
    private Boolean isFromProfileSrc;
    private Boolean isFromHomeSrc;
    private final ViewBinderHelper binderHelper = new ViewBinderHelper();

    public TaskListAdapter(Context mContext, ArrayList<TaskDataBO> taskDatas, String outDateFormat, TaskClickListener taskClickListener, boolean isFromProfileSrc, boolean fromHomeScreen) {
        this.taskDatas = taskDatas;
        this.mContext = mContext;
        this.outDateFormat = outDateFormat;
        this.taskClickListener = taskClickListener;
        this.isFromProfileSrc = isFromProfileSrc;
        this.isFromHomeSrc = fromHomeScreen;

        // to open only one row at a time
        binderHelper.setOpenOnlyOne(true);
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

        // Use ViewBindHelper to restore and save the open/close state of the SwipeRevealView
        // put an unique string id as value, can be any string which uniquely define the data
        binderHelper.bind(holder.swipeLayout, taskBo.getTaskId());


        holder.taskTitle.setText(taskBo.getTasktitle());
        holder.taskProductLevel.setText(taskBo.getTaskCategoryDsc());
        holder.taskDueDateTv.setText(DateTimeUtils.convertFromServerDateToRequestedFormat
                (taskBo.getTaskDueDate(), outDateFormat));

        holder.taskCB.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isChecked) {
                taskBo.setChecked(isChecked);
                taskClickListener.onTaskExcutedClick(taskBo);
            }
        });

        holder.layoutrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                taskClickListener.onTaskButtonClick(taskBo, TaskConstant.TASK_DETAIL);
            }
        });

        holder.btnAttachFile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                taskClickListener.onAttachFile(taskBo.getTaskId(), taskBo.getTaskCategoryID(), taskBo.isChecked());
            }
        });

        holder.btnEditTask.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                taskClickListener.onTaskButtonClick(taskBo, TaskConstant.TASK_EDIT);

            }
        });

        holder.btnDeleteTask.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (taskBo.getUsercreated().equals("0"))
                    Toast.makeText(mContext, mContext.getString(R.string.server_task_can_not_be_delete), Toast.LENGTH_SHORT).show();
                if (taskBo.isChecked())
                    Toast.makeText(mContext, mContext.getString(R.string.exec_task_not_allow_to_delete), Toast.LENGTH_SHORT).show();
                else {
                    showDeleteAlert(holder.getAdapterPosition());
                }
            }
        });

        if (taskBo.isUpload() && taskBo.getIsdone().equals("1")) {
            holder.taskCB.setEnabled(false);
            holder.taskTitle.setPaintFlags(holder.taskTitle.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            holder.taskProductLevel.setPaintFlags(holder.taskProductLevel.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
        } else {
            holder.taskCB.setEnabled(true);
            holder.taskTitle.setPaintFlags(holder.taskTitle.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
            holder.taskProductLevel.setPaintFlags(holder.taskProductLevel.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
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
    public int getItemViewType(int position) {
        return position;
    }

    @Override
    public int getItemCount() {
        return taskDatas.size();
    }

    public class TaskListViewHolder extends RecyclerView.ViewHolder {
        private SwipeRevealLayout swipeLayout;
        CheckBox taskCB;
        TextView taskTitle;
        TextView taskProductLevel;
        LinearLayout layoutCB;
        LinearLayout layoutrow;
        ImageButton btnAttachFile;
        Button btnEditTask;
        Button btnDeleteTask;
        TextView taskDueDateTv;

        public TaskListViewHolder(View itemView) {
            super(itemView);

            swipeLayout = itemView.findViewById(R.id.swipe_layout);
            taskCB = itemView.findViewById(R.id.task_title_CB);
            taskTitle = itemView.findViewById(R.id.task_title_tv);
            taskProductLevel = itemView.findViewById(R.id.task_category_tv);
            layoutCB = itemView.findViewById(R.id.layoutCB);
            layoutrow = itemView.findViewById(R.id.layoutBorder);
            btnAttachFile = itemView.findViewById(R.id.btn_attach_photo);
            btnDeleteTask = itemView.findViewById(R.id.delete_button);
            btnEditTask = itemView.findViewById(R.id.edit_button);
            taskDueDateTv = itemView.findViewById(R.id.task_due_date_tv);

            if (isFromProfileSrc) {
                taskCB.setVisibility(View.GONE);
            }

            if (isFromHomeSrc)
                taskProductLevel.setVisibility(View.GONE);

        }
    }


    /*
      Only if you need to restore open/close state when the orientation is changed.
      Call this method in {@link android.app.Activity#onSaveInstanceState(Bundle)}
     *//*
    public void saveStates(Bundle outState) {
        binderHelper.saveStates(outState);
    }

    */

    /**
     * Only if you need to restore open/close state when the orientation is changed.
     * Call this method in {@link android.app.Activity#onRestoreInstanceState(Bundle)}
     *//*
    public void restoreStates(Bundle inState) {
        binderHelper.restoreStates(inState);
    }
*/
    private void showDeleteAlert(int position) {

        AlertDialog.Builder builder = new AlertDialog.Builder(
                mContext);
        builder.setTitle("");
        builder.setMessage(mContext.getString(
                R.string.do_you_want_to_delete_the_image));

        builder.setPositiveButton(mContext.getString(R.string.ok),
                new android.content.DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        taskClickListener.onTaskButtonClick(taskDatas.get(position), 2);
                        taskDatas.remove(position);
                        notifyItemRemoved(position);
                        notifyItemRangeChanged(position, taskDatas.size());
                        dialog.dismiss();


                    }
                });

        builder.setNegativeButton(mContext.getString(R.string.cancel),
                new android.content.DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });

        builder.setCancelable(false);
        AppUtils.applyAlertDialogTheme(mContext, builder);
    }
}
