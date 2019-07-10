package com.ivy.cpg.view.van.manualvanload.manualvanloadbatchentrydialog;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import android.widget.DatePicker;

import com.ivy.sd.png.asean.view.R;

import java.util.Calendar;

/**
 * Created by Hanifa on 21/8/18.
 */

public class DatePickerFragment extends DialogFragment implements DatePickerDialog.OnDateSetListener {
    private DatePickerInterface mListener;
    public DatePickerFragment() {

    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);
        return new DatePickerDialog(getActivity(), R.style.DatePickerDialogStyle, this, year, month, day);
    }

    @Override
    public void onDateSet(DatePicker datePicker, int year, int month, int day) {
        callListener(year, month, day);
    }

    public DialogFragment setCallbackListener(DatePickerInterface listener) {
        this.mListener = listener;
        return null;
    }

    private void callListener(int year, int month, int day) {
        if (mListener != null) mListener.onDataSet(year, month, day);
    }

}
