package com.ivy.ui.task.adapter;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.ColorStateList;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.AppCompatImageButton;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.ivy.lib.view.RibbonView;
import com.ivy.sd.png.asean.view.R;
import com.ivy.ui.task.TaskClickListener;
import com.ivy.ui.task.TaskConstant;
import com.ivy.ui.task.model.TaskDataBO;
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
    private TaskConstant.SOURCE source;
    private Boolean isShowProdLevel;
    private int mTabPosition;
    private final ViewBinderHelper binderHelper = new ViewBinderHelper();
    private boolean isPreVisit = false;

    public TaskListAdapter(Context mContext, ArrayList<TaskDataBO> taskDatas, String outDateFormat, TaskClickListener taskClickListener, TaskConstant.SOURCE source, boolean isShowProdLevel, int mTabPosition, boolean isPreVisit) {
        this.taskDatas = taskDatas;
        this.mContext = mContext;
        this.outDateFormat = outDateFormat;
        this.taskClickListener = taskClickListener;
        this.source = source;
        this.isShowProdLevel = isShowProdLevel;
        this.mTabPosition = mTabPosition;

        this.isPreVisit = isPreVisit;

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
    public void onBindViewHolder(@NonNull TaskListViewHolder holder, int position) {
        TaskDataBO taskBo = taskDatas.get(position);

        // Use ViewBindHelper to restore and save the open/close state of the SwipeRevealView
        // put an unique string id as value, can be any string which uniquely define the data
        binderHelper.bind(holder.swipeLayout, taskBo.getTaskId());

        if (mTabPosition == 2
                || taskBo.isChecked()
                || (taskBo.isUpload() && taskBo.getIsdone().equals("1")))
            binderHelper.lockSwipe(taskBo.getTaskId());


        holder.taskTitle.setText(taskBo.getTasktitle());
        holder.taskProductLevel.setText(taskBo.getTaskCategoryDsc());
        if (mTabPosition == 2)
            holder.taskDueDateTv.setText(DateTimeUtils.convertFromServerDateToRequestedFormat
                    (taskBo.getTaskExecDate(), outDateFormat));
        else
            holder.taskDueDateTv.setText(DateTimeUtils.convertFromServerDateToRequestedFormat
                    (taskBo.getTaskDueDate(), outDateFormat));

        int daysCount = DateTimeUtils.getDateCount(taskBo.getTaskDueDate(),
                DateTimeUtils.now(DateTimeUtils.DATE_GLOBAL), "yyyy/MM/dd");
        if (daysCount >= 1) {
            holder.dueDaysTv.setVisibility(View.VISIBLE);
            holder.dueDaysTv.setText(mContext.getString(R.string.over_due));
        } else {
            holder.dueDaysTv.setVisibility(View.GONE);
        }
        holder.taskCB.setOnClickListener(v -> {
            if (!taskBo.isChecked()) {
                holder.taskCB.setChecked(true);
                taskBo.setChecked(true);
                binderHelper.lockSwipe(taskBo.getTaskId());
            } else {
                holder.taskCB.setChecked(false);
                taskBo.setChecked(false);
                binderHelper.unlockSwipe(taskBo.getTaskId());
            }
            taskClickListener.onTaskExecutedClick(taskBo);

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
                taskClickListener.onAttachFile(taskBo);
                if (taskBo.isChecked())
                    notifyItemChanged(holder.getAdapterPosition());
            }
        });

        holder.btnEditTask.setOnClickListener(
                v -> taskClickListener.onTaskButtonClick(taskBo, TaskConstant.TASK_EDIT));

        holder.btnDeleteTask.setOnClickListener(v -> {
            if (taskBo.getUsercreated().equals("0"))
                Toast.makeText(mContext, mContext.getString(R.string.server_task_can_not_be_delete), Toast.LENGTH_SHORT).show();
            else if (taskBo.isChecked())
                Toast.makeText(mContext, mContext.getString(R.string.exec_task_not_allow_to_delete), Toast.LENGTH_SHORT).show();
            else {
                showDeleteAlert(holder.getAdapterPosition());
            }
        });

        holder.btnCloseTask.setOnClickListener(
                v -> taskClickListener.showTaskNoReasonDialog(holder.getAdapterPosition()));

        if (taskBo.isUpload() && taskBo.getIsdone().equals("1")) {
            holder.taskCB.setEnabled(false);
            holder.taskTitle.setPaintFlags(holder.taskTitle.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            holder.taskProductLevel.setPaintFlags(holder.taskProductLevel.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
        } else {
            if (!isPreVisit)
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

        if (taskBo.getTaskEvidenceImg() != null
                && !taskBo.getTaskEvidenceImg().isEmpty()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                holder.btnAttachFile.setImageTintList(ColorStateList.valueOf(ContextCompat.getColor(mContext, R.color.colorAccent)));
            else
                holder.btnAttachFile.setColorFilter(ContextCompat.getColor(mContext, R.color.colorAccent), PorterDuff.Mode.SRC_ATOP);
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                holder.btnAttachFile.setImageTintList(ColorStateList.valueOf(ContextCompat.getColor(mContext, R.color.colorPrimary)));
            else
                holder.btnAttachFile.setColorFilter(ContextCompat.getColor(mContext, R.color.colorPrimary), PorterDuff.Mode.SRC_ATOP);
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
        RibbonView dueDaysTv;
        LinearLayout layoutCB;
        LinearLayout layoutrow;
        AppCompatImageButton btnAttachFile;
        Button btnEditTask;
        Button btnDeleteTask;
        Button btnCloseTask;
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
            btnCloseTask = itemView.findViewById(R.id.close_button);
            taskDueDateTv = itemView.findViewById(R.id.task_due_date_tv);
            dueDaysTv = itemView.findViewById(R.id.due_days_Tv);

            if (TaskConstant.SOURCE.PROFILE_SCREEN == source) {
                btnAttachFile.setVisibility(View.GONE);
                layoutCB.setVisibility(View.GONE);
            }

            if (!isShowProdLevel
                    && (source == TaskConstant.SOURCE.RETAILER))
                taskProductLevel.setVisibility(View.GONE);

            if (mTabPosition == 2) {
                btnAttachFile.setVisibility(View.GONE);
                taskCB.setVisibility(View.GONE);
            }

        }
    }

    private void showDeleteAlert(int position) {

        AlertDialog.Builder builder = new AlertDialog.Builder(
                mContext);
        builder.setTitle("");
        builder.setMessage(mContext.getString(
                R.string.do_you_want_to_delete_the_task));

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
