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
        DatePickerDialog dpd = new DatePickerDialog(getActivity(), R.style.DatePickerDialogStyle, this, year, month, day);
        dpd.setTitle(getActivity().getResources().getString(R.string.choose_date));
        return dpd;
    }

    @Override
    public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
        Calendar selectedDate = new GregorianCalendar(year, monthOfYear, dayOfMonth);
        updateDateInterface.updateDate(selectedDate.getTime(), this.getTag());
    }
}
