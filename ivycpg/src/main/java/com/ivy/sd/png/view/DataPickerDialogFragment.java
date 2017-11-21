package com.ivy.sd.png.view;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.widget.DatePicker;

import com.ivy.lib.DialogFragment;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

public class DataPickerDialogFragment extends DialogFragment implements DatePickerDialog.OnDateSetListener {
    private UpdateDateInterface updateDateInterface;
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
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);
        return new DatePickerDialog(getActivity(), this, year, month, day);
    }

    @Override
    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
        Calendar selectedDate = new GregorianCalendar(year, monthOfYear, dayOfMonth);
        updateDateInterface.updateDate(selectedDate.getTime(),this.getTag());
    }
}
