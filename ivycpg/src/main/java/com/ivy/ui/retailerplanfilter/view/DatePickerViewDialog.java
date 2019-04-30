package com.ivy.ui.retailerplanfilter.view;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.DatePicker;

import com.ivy.sd.png.asean.view.R;

import java.util.Locale;

public class DatePickerViewDialog extends DatePickerDialog implements DialogInterface.OnClickListener{

    private DateSelectListener callBack;
    private int mYear;
    private int mMonth;
    private int mDay;
    private View view;

    private DatePicker datePicker;

    public DatePickerViewDialog(Context context, int resId, DateSelectListener callBack,
                                int year, int monthOfYear, int dayOfMonth,View view) {
        super(context,resId, callBack, year, monthOfYear, dayOfMonth);
        this.mYear = year;
        this.mMonth = monthOfYear;
        this.mDay = dayOfMonth;
        this.callBack = callBack;

        this.view = view;

        this.getDatePicker().setDescendantFocusability(DatePicker.FOCUS_BLOCK_DESCENDANTS);
        setButton(BUTTON_POSITIVE, context.getResources().getString(R.string.ok), this);
        setButton(BUTTON_NEGATIVE, context.getResources().getString(R.string.cancel), this);
        this.setCancelable(false);
    }

    @Override
    public void onDateChanged(@NonNull DatePicker view, int year, int month, int day) {
        super.onDateChanged(view, year, month, day);

        this.datePicker = view;
    }

    @Override
    public void onClick(@NonNull DialogInterface dialog, int which) {
        switch (which) {
            case BUTTON_POSITIVE:
                if (callBack != null ) {
                    datePicker.clearFocus();

                    String date = String.format(Locale.US,"%02d/%02d/%04d", datePicker.getDayOfMonth(),datePicker.getMonth(), datePicker.getYear());

                    callBack.onDateSet(view, date);
                    mYear = datePicker.getYear();
                    mMonth = datePicker.getMonth();
                    mDay = datePicker.getDayOfMonth();
                }
                break;
            case BUTTON_NEGATIVE:
                if (callBack != null )

                cancel();
                break;
        }
    }

    public interface DateSelectListener extends DatePicker.OnDateChangedListener, OnDateSetListener {
        void onDateSet(View view, String date);

        @Override
        void onDateSet(DatePicker view, int year, int month, int dayOfMonth);

        @Override
        void onDateChanged(DatePicker view, int year, int monthOfYear, int dayOfMonth);
    }

    private String convertTwoDigitFormat(int val){
        if (String.valueOf(val).length() == 1)
            return "0"+val;
        else
            return val+"";
    }
}
