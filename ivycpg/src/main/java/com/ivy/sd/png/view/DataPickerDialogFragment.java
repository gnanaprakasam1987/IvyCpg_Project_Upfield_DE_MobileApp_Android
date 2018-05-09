package com.ivy.sd.png.view;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.widget.DatePicker;

import com.ivy.lib.DialogFragment;
import com.ivy.sd.png.asean.view.R;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

public class DataPickerDialogFragment extends DialogFragment implements DatePickerDialog.OnDateSetListener {
    private UpdateDateInterface updateDateInterface;
    private int minDate = 0, maxDate = 0;
    private String moduleName = "";

    public interface UpdateDateInterface {
        void updateDate(Date date, String tag);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try {
            updateDateInterface = (UpdateDateInterface) getActivity();
        } catch (ClassCastException e) {
            throw new ClassCastException("Calling Fragment must implement OnAddFriendListener");
        }
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final Calendar c = Calendar.getInstance();
        if (getArguments().getString("MODULE") != null) {
            moduleName = getArguments().getString("MODULE");
            minDate = (getArguments().getInt("CHQMINDATE") != 0) ? getArguments().getInt("CHQMINDATE") : 0;
            maxDate = (getArguments().getInt("CHQMAXDATE") != 0) ? getArguments().getInt("CHQMAXDATE") : 0;
        }
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);
        DatePickerDialog dpd = new DatePickerDialog(getActivity(), R.style.DatePickerDialogStyle, this, year, month, day);
        setMinMaxDate(dpd);
        dpd.setTitle(getActivity().getResources().getString(R.string.choose_date));
        return dpd;
    }

    @Override
    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
        Calendar selectedDate = new GregorianCalendar(year, monthOfYear, dayOfMonth);
        updateDateInterface.updateDate(selectedDate.getTime(), this.getTag());
    }

    private void setMinMaxDate(DatePickerDialog dpd) {
        if (moduleName.equals("CHEQUE")) {
            if (minDate != 0 || maxDate != 0) {
                Calendar c = Calendar.getInstance();
                c.add(Calendar.DATE, minDate); // subtract 2 years from now
                dpd.getDatePicker().setMinDate(c.getTimeInMillis());
                c = Calendar.getInstance();
                c.add(Calendar.DATE, maxDate); // add 4 years to min date to have 2 years after now
                dpd.getDatePicker().setMaxDate(c.getTimeInMillis());
            }
        }
    }
}
