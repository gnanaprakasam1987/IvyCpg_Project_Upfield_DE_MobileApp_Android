package com.ivy.lib.view;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.NumberPicker;
import android.widget.TextView;

import com.ivy.lib.R;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

/**
 * Creates a dialog for picking the year and month.
 */

public class YearMonthPickerDialog implements Dialog.OnClickListener {
    /**
     * The minimal year value.
     */
    private static int MIN_YEAR = 1970;

    /**
     * The maximum year value.
     */
    private static final int MAX_YEAR = 2099;

    /**
     * The minimal month value.
     */
    private static int MIN_MONTH = 0;

    /**
     * The maximum month value.
     */
    private static int MAX_MONTH = 9;


    /**
     * The Month format pattern.
     */
    private static final String MONTH_FORMAT = "MMM";

    /**
     * Array of months.
     */
    private static String[] MONTHS_LIST = null;

    /**
     * Set Init Date.
     */
    private Calendar calendar;

    /**
     * Listener for user's date picking.
     */
    private OnDateSetListener mOnDateSetListener;

    /**
     * Application's context.
     */
    private final Context mContext;

    /**
     * Specific locale for format datetime.
     */
    private static Locale mCurrentLocale = Locale.getDefault();

    /**
     * The builder for our dialog.
     */
    private AlertDialog.Builder mDialogBuilder;

    /**
     * Resulting dialog.
     */
    private AlertDialog mDialog;

    /**
     * Custom user's theme for dialog.
     */
    private int mTheme;

    /**
     * Custom user's color for title background.
     */
    private int mheaderBgAndDeviderColor;

    /**
     * Picked year.
     */
    private int mYear;

    /**
     * Picked month.
     */
    private int mMonth;

    /**
     * Allow user to set custom date
     */
    private NumberPicker mYearPicker;
    private NumberPicker monthPicker;

    /**
     * Creates a new YearMonthPickerDialog object that represents the dialog for
     * picking year and month. Specifies custom user's theme and title text color
     *
     * @param context                  The application's context.
     * @param onDateSetListener        Listener for user's date picking.
     * @param theme                    Custom user's theme for dialog.
     * @param mheaderBgAndDeviderColor Custom user's color for title text.
     */
    public YearMonthPickerDialog(Context context, OnDateSetListener onDateSetListener, int theme,
                                 int mheaderBgAndDeviderColor, Calendar calendar) {
        mContext = context;
        mOnDateSetListener = onDateSetListener;
        mTheme = theme;
        this.mheaderBgAndDeviderColor = mheaderBgAndDeviderColor;
        this.calendar = calendar;
        //Set current locale of system
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            mCurrentLocale = mContext.getResources().getConfiguration().getLocales().get(0);
        } else {
            mCurrentLocale = mContext.getResources().getConfiguration().locale;
        }


        //Builds the dialog using listed parameters.
        build();
    }

    /**
     * Listens for user's actions.
     *
     * @param dialog Current instance of dialog.
     * @param which  Id of pressed button.
     */
    @Override
    public void onClick(DialogInterface dialog, int which) {
        switch (which) {
            //If user presses positive button
            case DialogInterface.BUTTON_POSITIVE:
                //Check if user gave us a listener
                if (mOnDateSetListener != null)
                    //Set picked year and month to the listener
                    mOnDateSetListener.onYearMonthSet(mYearPicker.getValue(), monthPicker.getValue());
                break;

            //If user presses negative button
            case DialogInterface.BUTTON_NEGATIVE:
                //Exit the dialog
                dialog.cancel();
                break;
        }
    }

    /**
     * Creates and customizes a dialog.
     */
    private void build() {
        //Applying user's theme
        int currentTheme = mTheme;
        //If there is no custom theme, using default.
        if (currentTheme == -1) currentTheme = R.style.MyDialogTheme;

        //Initializing dialog builder.
        mDialogBuilder = new AlertDialog.Builder(mContext, currentTheme);

        //Creating View inflater.
        final LayoutInflater layoutInflater =
                (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

       /* //Inflating custom title view.
        final View titleView = layoutInflater.inflate(R.layout.view_dialog_title, null, false);*/
        //Inflating custom content view.
        final View contentView = layoutInflater.inflate(R.layout.view_month_year_picker, null, false);

        //Initializing year and month pickers.
        mYearPicker = contentView.findViewById(R.id.year_picker);
        monthPicker = contentView.findViewById(R.id.month_picker);

        setDividerColor(mYearPicker, monthPicker, mheaderBgAndDeviderColor);

        //Initializing title text views
        final TextView dialogTitle = contentView.findViewById(R.id.date_picker_title_tv);
        //If there is user's title color,
        if (mheaderBgAndDeviderColor != -1) {
            //Then apply it.
            setTextColor(dialogTitle);
        }

        //Setting custom title view and content to dialog.
        mDialogBuilder.setCustomTitle(null);
        mDialogBuilder.setView(contentView);

        //Setting year's picker min and max value
        mYearPicker.setMinValue(MIN_YEAR);
        mYearPicker.setMaxValue(MAX_YEAR);

        //Setting month's picker min and max value
        monthPicker.setMinValue(MIN_MONTH);
        monthPicker.setMaxValue(MAX_MONTH);

        //Setting month list.
        monthPicker.setDisplayedValues(monthsList());

        //Applying current date.
        setCurrentDate(mYearPicker, monthPicker);

        //Setting all listeners.
        setListeners(mYearPicker, monthPicker);

        //Setting titles and listeners for dialog buttons.
        mDialogBuilder.setPositiveButton("OK", this);
        mDialogBuilder.setNegativeButton("CANCEL", this);

        //Creating dialog.
        mDialog = mDialogBuilder.create();
    }

    /**
     * Sets color to given TextView.
     *
     * @param titleView Given TextView.
     */
    private void setTextColor(TextView titleView) {
        titleView.setBackgroundColor(mheaderBgAndDeviderColor);
        //titleView.setB(ContextCompat.getColor(mContext, mheaderBgAndDeviderColor));
    }

    /**
     * Sets current date for title and pickers.
     *
     * @param yearPicker  year picker.
     * @param monthPicker month picker.
     */
    private void setCurrentDate(NumberPicker yearPicker, NumberPicker monthPicker) {
        //Getting current date values from Calendar instance.
        ////Calendar calendar = Calendar.getInstance();
        mMonth = calendar.get(Calendar.MONTH);
        mYear = calendar.get(Calendar.YEAR);

        //Setting current date values to pickers.
        monthPicker.setValue(mMonth);
        yearPicker.setValue(mYear);
    }

    /**
     * Sets current date for title and pickers.
     *
     * @param yearPicker  year picker.
     * @param monthPicker month picker.
     */
    private void setListeners(final NumberPicker yearPicker, final NumberPicker monthPicker) {
//Setting listener to month picker. So it can change title text value.
        monthPicker.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                mMonth = newVal;


            }
        });

        //Setting listener to year picker. So it can change title text value.
        yearPicker.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                mYear = newVal;

            }
        });

    }


    /**
     * Allows user to show created dialog.
     */
    public void show() {
        mDialog.show();
    }

    /**
     * Sets min value of year picker widget.
     *
     * @param minYear The min value inclusive.
     */
    public void setMinYear(int minYear) {
        if (mYearPicker != null) {
            if (mYearPicker.getValue() < minYear) {
                mYearPicker.setValue(minYear);
            }
            mYearPicker.setMinValue(Math.min(minYear, mYearPicker.getMaxValue()));
        }
    }

    /**
     * Sets max value of year picker widget.
     *
     * @param maxYear The max value inclusive.
     */
    public void setMaxYear(int maxYear) {
        if (mYearPicker != null) {
            if (mYearPicker.getValue() > maxYear) {
                mYearPicker.setValue(maxYear);
            }
            mYearPicker.setMaxValue(Math.max(maxYear, mYearPicker.getMinValue()));
        }
    }


    /**
     * Interface for implementing user's pick listener.
     */
    public interface OnDateSetListener {
        /**
         * Listens for user's actions.
         */
        void onYearMonthSet(int year, int month);
    }


    /**
     * Capitalize string
     */
    private static String capitalize(final String line) {
        return Character.toUpperCase(line.charAt(0)) + line.substring(1);
    }

    /**
     * Get month name with specified locale
     */
    private static String[] monthsList() {
        if (MONTHS_LIST == null) {
        int[] months = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11};
        String[] stringMonths;
        int size = months.length;
        stringMonths = new String[size];

        for (int i = 0; i < size; i++) {
            Calendar calendar = Calendar.getInstance();

            SimpleDateFormat monthDate = new SimpleDateFormat(MONTH_FORMAT, mCurrentLocale);

            calendar.set(Calendar.MONTH, months[i]);
            String monthName = monthDate.format(calendar.getTime());


            stringMonths[i] = capitalize(monthName);
        }

        MONTHS_LIST = stringMonths;
        }

        return MONTHS_LIST;
    }

    /**
     * set deivider color
     *
     * @param monthPicker
     * @param yearPicker
     * @param color       - mTextColor
     */
    private void setDividerColor(NumberPicker monthPicker, NumberPicker yearPicker, int color) {

        java.lang.reflect.Field[] pickerFields = NumberPicker.class.getDeclaredFields();
        for (java.lang.reflect.Field pf : pickerFields) {
            if (pf.getName().equals("mSelectionDivider")) {
                pf.setAccessible(true);
                try {
                    ColorDrawable colorDrawable = new ColorDrawable(color);
                    pf.set(monthPicker, colorDrawable);
                    pf.set(yearPicker, colorDrawable);
                } catch (IllegalArgumentException e) {
                    e.printStackTrace();
                } catch (Resources.NotFoundException e) {
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
                break;
            }
        }
    }
}
