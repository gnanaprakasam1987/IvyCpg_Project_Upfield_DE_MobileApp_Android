package com.ivy.ui.profile.data;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;

import android.app.Dialog;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import android.widget.DatePicker;

import com.ivy.sd.png.asean.view.R;

@SuppressLint("ValidFragment")
public class DatePickerFragment extends DialogFragment implements DatePickerDialog.OnDateSetListener {

    int year;
    int month;
    int day;
    String code;
    private DatePreviewListener datePreviewListener;
    private ITinDatePreviewListener iTinDatePreviewListener=null;

    public DatePickerFragment(String code, int year, int month, int day, DatePreviewListener datePreviewListener) {
        this.code = code;
        this.year = year;
        this.month = month;
        this.day = day;
        this.datePreviewListener = datePreviewListener;
    }

    public DatePickerFragment(String code, int year, int month, int day, ITinDatePreviewListener iTinDatePreviewListener) {
        this.code = code;
        this.year = year;
        this.month = month;
        this.day = day;
        this.iTinDatePreviewListener = iTinDatePreviewListener;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        return new DatePickerDialog(getActivity(), R.style.DatePickerDialogStyle, this, year, month, day);
    }

    @Override
    public void onDateSet(DatePicker view, int year, int month, int day) {
        if (code.equalsIgnoreCase("DLEXPDATE"))
            datePreviewListener.truckExpDate(year, month, day);
        else if (code.equalsIgnoreCase("FLEXPDATE"))
            datePreviewListener.foodExpiryDate(year, month, day);
        else if (code.equalsIgnoreCase("TIN_EXP_DATE"))
            if(iTinDatePreviewListener!=null)
            iTinDatePreviewListener.TinExpDate(year, month, day);
        this.year = year;
        this.day = day;
        this.month = month;
    }
}