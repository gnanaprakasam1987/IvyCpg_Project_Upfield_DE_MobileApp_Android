package com.ivy.ui.task;

import com.ivy.cpg.view.task.TaskDataBO;

public interface TaskClickListener {

    void onTaskExcutedClick(TaskDataBO taskDataBO);

    void onTaskButtonClick(TaskDataBO taskBO, int isType);

    void onAttachFile(TaskDataBO taskBO);

    void onSortItemClicked(int sortType, boolean orderByAsc);
}
