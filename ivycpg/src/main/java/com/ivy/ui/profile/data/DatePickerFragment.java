package com.ivy.ui.profile.data;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;

import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.widget.DatePicker;
import android.widget.Toast;

import com.ivy.sd.png.asean.view.R;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.Locale;

@SuppressLint("ValidFragment")
public  class DatePickerFragment extends DialogFragment implements DatePickerDialog.OnDateSetListener {
        
        int year;
        int month;
        int day;
        String code;

    DatePreviewListener datePreviewListener;

    DatePickerFragment(String code, int year, int month, int day, DatePreviewListener datePreviewListener) {
            this.code = code;
            this.year = year;
            this.month = month;
            this.day = day;
            this.datePreviewListener=datePreviewListener;
        }
        
        @NonNull
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            return new DatePickerDialog(getActivity(), R.style.DatePickerDialogStyle, this, year, month, day);
        }

        @Override
        public void onDateSet(DatePicker view, int year, int month, int day) {

            Calendar selectedDate = new GregorianCalendar(year, month, day);
            if (selectedDate.after(Calendar.getInstance())) {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd", Locale.ENGLISH);
                if (code.equalsIgnoreCase("DLEXPDATE"))
                    datePreviewListener.druckExpDate(sdf.format(selectedDate.getTime()));
                   // dlExpDateTextView.setText(sdf.format(selectedDate.getTime()));

                else if (code.equalsIgnoreCase("FLEXPDATE"))
                    datePreviewListener.foodxpDate(sdf.format(selectedDate.getTime()));
                    //flExpDateTextView.setText(sdf.format(selectedDate.getTime()));
                this.year = year;
                this.day = day;
                this.month = month;
            } else {
                Toast.makeText(getActivity(), "Select future date", Toast.LENGTH_SHORT).show();
            }
        }
    }