package com.ivy.ui.retailerplanfilter.view;

import android.app.DatePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.DatePicker;

import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.util.Commons;
import com.ivy.utils.DateTimeUtils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class DatePickerViewDialog extends DatePickerDialog implements DialogInterface.OnClickListener{

    private DateSelectListener callBack;
    private int mYear;
    private int mMonth;
    private int mDay;
    private View view;

    private DatePicker datePicker;

    private String compareDate;
    private boolean isFromDate;

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

    public void compareDate(String compareDate,boolean isFromDate){
        this.compareDate = compareDate;
        this.isFromDate =  isFromDate;
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

                    String date = String.format(Locale.US,"%02d/%02d/%04d",
                            datePicker.getDayOfMonth(),
                            datePicker.getMonth() + 1,
                            datePicker.getYear());

                    mYear = datePicker.getYear();
                    mMonth = datePicker.getMonth();
                    mDay = datePicker.getDayOfMonth();

                    if (compareDate != null && !compareDate.isEmpty()){

                        if (isFromDate && DateTimeUtils.getDate(date,"dd/MM/yyyy").after(DateTimeUtils.getDate(compareDate,"dd/MM/yyyy"))){
                            callBack.dateValidationError("From Date should not be more than Till Date");
                            return;
                        }else if(DateTimeUtils.getDate(date,"dd/MM/yyyy").before(DateTimeUtils.getDate(compareDate,"dd/MM/yyyy"))){
                            callBack.dateValidationError("Till Date should not be less than From Date");
                            return;
                        }
                    }

                    callBack.onDateSet(view, date);

                }
                break;
            case BUTTON_NEGATIVE:
                cancel();
                break;
        }
    }

    public interface DateSelectListener extends DatePicker.OnDateChangedListener, OnDateSetListener {
        void onDateSet(View view, String date);

        void dateValidationError(String error);

        @Override
        void onDateSet(DatePicker view, int year, int month, int dayOfMonth);

        @Override
        void onDateChanged(DatePicker view, int year, int monthOfYear, int dayOfMonth);
    }


}
