package com.ivy.sd.png.util;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import androidx.annotation.NonNull;
import android.widget.DatePicker;

import com.ivy.sd.png.asean.view.R;

public class MyDatePickerDialog extends DatePickerDialog implements DialogInterface.OnClickListener{

    private CharSequence title;
    private OnDateSetListener callBack;
    private int mYear;
    private int mMonth;
    private int mDay;
    private DatePicker datePicker;

    public MyDatePickerDialog(Context context,int resId, OnDateSetListener callBack, int year, int monthOfYear, int dayOfMonth) {
        super(context,resId, callBack, year, monthOfYear, dayOfMonth);
        this.mYear = year;
        this.mMonth = monthOfYear;
        this.mDay = dayOfMonth;
        this.callBack = callBack;
        this.getDatePicker().setDescendantFocusability(DatePicker.FOCUS_BLOCK_DESCENDANTS);
        setButton(BUTTON_POSITIVE, context.getResources().getString(R.string.ok), this);
        setButton(BUTTON_NEGATIVE, context.getResources().getString(R.string.cancel), this);
        this.setCancelable(false);
    }

    public void setPermanentTitle(CharSequence title) {
        this.title = title;
        setTitle(title);
    }

    @Override
    public void onDateChanged(@NonNull DatePicker view, int year, int month, int day) {
        super.onDateChanged(view, year, month, day);
        this.datePicker = view;
        setTitle(title);
    }

    @Override
    public void onClick(@NonNull DialogInterface dialog, int which) {
        switch (which) {
            case BUTTON_POSITIVE:
                if (callBack != null && datePicker != null) {
                    datePicker.clearFocus();
                    callBack.onDateSet(datePicker, datePicker.getYear(),
                            datePicker.getMonth(), datePicker.getDayOfMonth());
                    mYear = datePicker.getYear();
                    mMonth = datePicker.getMonth();
                    mDay = datePicker.getDayOfMonth();
                }
                break;
            case BUTTON_NEGATIVE:
                if (callBack != null && datePicker != null)
                    callBack.onDateSet(datePicker, mYear, mMonth, mDay);

                cancel();
                break;
        }
    }
}