package com.ivy.cpg.view.retailercontact.customview;

import android.content.Context;
import androidx.annotation.NonNull;
import android.util.AttributeSet;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import static com.ivy.utils.DateTimeUtils.getHour;
import static com.ivy.utils.DateTimeUtils.todayDate;


public class WheelHourPicker extends WheelPicker<String> {

    private int minHour;
    private int maxHour;
    private int hoursStep;

    protected boolean isAmPm;
    private FinishedLoopListener finishedLoopListener;
    private OnHourChangedListener hourChangedListener;

    public static final int MIN_HOUR_DEFAULT = 0;
    public static final int MAX_HOUR_DEFAULT = 23;
    public static final int MAX_HOUR_AM_PM = 12;
    public static final int STEP_HOURS_DEFAULT = 1;

    public WheelHourPicker(Context context) {
        super(context);
    }

    public WheelHourPicker(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    protected void init() {
        isAmPm = false;
        minHour = MIN_HOUR_DEFAULT;
        maxHour = MAX_HOUR_DEFAULT;
        hoursStep = STEP_HOURS_DEFAULT;
    }

    @Override
    protected String initDefault() {
        return String.valueOf(getHour(todayDate(), isAmPm));
    }

    @Override
    protected List<String> generateAdapterValues() {
        final List<String> hours = new ArrayList<>();

        if (isAmPm) {
            hours.add(getFormattedValue(12)+":00");
            for (int hour = hoursStep; hour < maxHour; hour += hoursStep) {
                hours.add(getFormattedValue(hour)+":00");
            }
        } else {
            for (int hour = minHour; hour <= maxHour; hour += hoursStep) {
                hours.add(getFormattedValue(hour)+":00");
            }
        }

        return hours;
    }

    @Override
    public int findIndexOfDate(@NonNull Date date) {
        if (isAmPm) {
            final int hours = date.getHours();
            if (hours >= MAX_HOUR_AM_PM) {
                Date copy = new Date(date.getTime());
                copy.setHours(hours % MAX_HOUR_AM_PM);
                return super.findIndexOfDate(copy);
            }
        }
        return super.findIndexOfDate(date);
    }

    protected String getFormattedValue(Object value) {
        Object valueItem = value;
        if (value instanceof Date) {
            Calendar instance = Calendar.getInstance();
            instance.setTime((Date) value);
            valueItem = instance.get(Calendar.HOUR_OF_DAY);
        }

        return String.format(getCurrentLocale(), FORMAT, valueItem);
    }

    @Override
    public void setDefault(String defaultValue) {
        try {

            Object hour;
            if (!defaultValue.contains(":")) {
                int hourVal = Integer.parseInt(defaultValue);
                if (isAmPm && (int)hourVal >= MAX_HOUR_AM_PM) {
                    hourVal -= MAX_HOUR_AM_PM;
                }

                hour = hourVal;
                super.setDefault(getFormattedValue(hour));
            }
            else {
                super.setDefault(defaultValue);
            }
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    public void setIsAmPm(boolean isAmPm) {
        this.isAmPm = isAmPm;
        if (isAmPm) {
            setMaxHour(MAX_HOUR_AM_PM);
        } else {
            setMaxHour(MAX_HOUR_DEFAULT);
        }
        updateAdapter();
    }

    public void setMaxHour(int maxHour) {
        if (maxHour >= MIN_HOUR_DEFAULT && maxHour <= MAX_HOUR_DEFAULT) {
            this.maxHour = maxHour;
        }
        notifyDatasetChanged();
    }

    public void setMinHour(int minHour) {
        if (minHour >= MIN_HOUR_DEFAULT && minHour <= MAX_HOUR_DEFAULT) {
            this.minHour = minHour;
        }
        notifyDatasetChanged();
    }

    public void setHoursStep(int hoursStep) {
        if (hoursStep >= MIN_HOUR_DEFAULT && hoursStep <= MAX_HOUR_DEFAULT) {
            this.hoursStep = hoursStep;
        }
        notifyDatasetChanged();
    }

    private int convertItemToHour(Object item) {
        Integer hour = Integer.valueOf(String.valueOf(item));
        if (!isAmPm) {
            return hour;
        }

        if (hour == 12) {
            hour = 0;
        }

        return hour;
    }

    public int getCurrentHour() {
        return convertItemToHour(adapter.getItem(getCurrentItemPosition()).replace(":00",""));
    }

    public String getSelectedTime(){
        return adapter.getItem(getCurrentItemPosition());
    }


    @Override
    protected void onItemSelected(int position, String item) {
        super.onItemSelected(position, item);

        if (hourChangedListener != null) {
            hourChangedListener.onHourChanged(this, convertItemToHour(item.replace(":00","")));
        }
    }

    public WheelHourPicker setOnFinishedLoopListener(FinishedLoopListener finishedLoopListener) {
        this.finishedLoopListener = finishedLoopListener;
        return this;
    }

    public WheelHourPicker setHourChangedListener(OnHourChangedListener hourChangedListener) {
        this.hourChangedListener = hourChangedListener;
        return this;
    }

    @Override
    protected void onFinishedLoop() {
        super.onFinishedLoop();
        if (finishedLoopListener != null) {
            finishedLoopListener.onFinishedLoop(this);
        }
    }

    public interface FinishedLoopListener {
        void onFinishedLoop(WheelHourPicker picker);
    }

    public interface OnHourChangedListener {
        void onHourChanged(WheelHourPicker picker, int hour);
    }
}