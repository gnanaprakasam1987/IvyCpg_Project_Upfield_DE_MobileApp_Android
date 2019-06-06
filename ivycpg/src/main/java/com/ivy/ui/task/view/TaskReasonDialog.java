package com.ivy.ui.task.view;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.support.annotation.NonNull;

public class TaskReasonDialog extends Dialog {
    public TaskReasonDialog(@NonNull Context context) {
        super(context);
    }

    public TaskReasonDialog(@NonNull Context context, int themeResId) {
        super(context, themeResId);
    }

    protected TaskReasonDialog(@NonNull Context context, boolean cancelable, @NonNull DialogInterface.OnCancelListener cancelListener) {
        super(context, cancelable, cancelListener);
    }
}
