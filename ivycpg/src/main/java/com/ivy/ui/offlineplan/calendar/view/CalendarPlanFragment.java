package com.ivy.ui.offlineplan.calendar.view;

import android.app.AlertDialog;
import android.graphics.RectF;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckedTextView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.alamkanak.weekview.DateTimeInterpreter;
import com.alamkanak.weekview.MonthLoader;
import com.alamkanak.weekview.WeekView;
import com.alamkanak.weekview.WeekViewEvent;
import com.ivy.calendarlibrary.monthview.MonthView;
import com.ivy.core.base.presenter.BasePresenter;
import com.ivy.core.base.view.BaseFragment;
import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.bo.SpinnerBO;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.ui.offlineplan.calendar.CalendarPlanContract;
import com.ivy.ui.offlineplan.calendar.adapter.CalendarClickListner;
import com.ivy.ui.offlineplan.calendar.adapter.MonthViewAdapter;
import com.ivy.ui.offlineplan.calendar.bo.CalenderBO;
import com.ivy.ui.offlineplan.calendar.di.CalendarPlanModule;
import com.ivy.ui.offlineplan.calendar.di.DaggerCalendarPlanComponent;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import javax.inject.Inject;

import butterknife.BindView;


public class CalendarPlanFragment extends BaseFragment implements CalendarPlanContract.CalendarPlanView, CalendarClickListner, WeekView.EventClickListener, MonthLoader.MonthChangeListener, WeekView.EventLongPressListener, WeekView.EmptyViewLongPressListener {

    private String screenTitle;
    private ArrayAdapter<SpinnerBO> selectionAdapter;

    @BindView(R.id.rv_calendar)
    MonthView rvCalendar;

    @BindView(R.id.img_prev)
    ImageView ivPrev;

    @BindView(R.id.img_next)
    ImageView ivNext;

    @BindView(R.id.tv_month)
    TextView tvMonth;

    @BindView(R.id.btn_switch)
    Button btnSwitch;

    @BindView(R.id.ll_titleList)
    LinearLayout llWeekTitle;

    @BindView(R.id.fab_retailer)
    FloatingActionButton fabAddRetailer;

    @BindView(R.id.week_view)
    WeekView mWeekView;


    @Inject
    CalendarPlanContract.CalendarPlanPresenter<CalendarPlanContract.CalendarPlanView> presenter;

    private int mSelectedType = 0;
    private final int MONTH = 0, DAY = 1, WEEK = 2;

    @Override
    public void initializeDi() {
        DaggerCalendarPlanComponent.builder()
                .ivyAppComponent(((BusinessModel) Objects.requireNonNull(getActivity()).getApplication()).getComponent())
                .calendarPlanModule(new CalendarPlanModule(this, getActivity()))
                .build()
                .inject(CalendarPlanFragment.this);

        setBasePresenter((BasePresenter) presenter);
    }

    @Override
    protected int setContentViewLayout() {
        return R.layout.fragment_offline;
    }

    @Override
    public void initVariables(View view) {
        rvCalendar.setLayoutManager(getActivity());
    }

    @Override
    protected void getMessageFromAliens() {
        if (getArguments() != null)
            screenTitle = getArguments().getString("screentitle");
    }

    @Override
    protected void setUpViews() {

        setUpToolbar(screenTitle);
        presenter.setPlanDates();
        presenter.loadCalendar();
        setSelectionAdapter();

        // Show a toast message about the touched event.
        mWeekView.setOnEventClickListener(this);

        // The week view has infinite scrolling horizontally. We have to provide the events of a
        // month every time the month changes on the week view.
        mWeekView.setMonthChangeListener(this);

        // Set long press listener for events.
        mWeekView.setEventLongPressListener(this);

        // Set long press listener for empty view
        mWeekView.setEmptyViewLongPressListener(this);

        ivNext.setOnClickListener(v -> {
            presenter.onNextMonthClicked();


        });

        ivPrev.setOnClickListener(v -> {
            presenter.onPreviousMonthClicked();

        });

        btnSwitch.setOnClickListener(v -> showSelectionDialog());

    }

    @Override
    public void loadCalendarView(ArrayList<String> mAllowedDates, int dayInWeekCount, ArrayList<CalenderBO> mCalenderAllList) {
        MonthViewAdapter monthViewAdapter = new MonthViewAdapter(getActivity(), dayInWeekCount, mCalenderAllList, mAllowedDates, this);
        rvCalendar.setAdapter(monthViewAdapter);
    }

    @Override
    public void setMonthName(String monthName) {
        tvMonth.setText(monthName);
    }

    @Override
    public void loadDayView(Calendar date) {
        setupDateTimeInterpreter(false);
        mWeekView.setColumnGap((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8, getResources().getDisplayMetrics()));
        mWeekView.setNumberOfVisibleDays(1);
        mWeekView.goToDate(date);

    }

    @Override
    public void loadWeekView(Calendar date) {
        setupDateTimeInterpreter(true);
        mWeekView.setColumnGap((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 2, getResources().getDisplayMetrics()));
        mWeekView.setNumberOfVisibleDays(7);
        mWeekView.goToDate(date);
        mWeekView.setFirstDayOfWeek(Calendar.MONDAY);
    }

    private void setSelectionAdapter() {
        selectionAdapter = new ArrayAdapter<SpinnerBO>(Objects.requireNonNull(getActivity()), android.R.layout.select_dialog_singlechoice) {
            @NonNull
            @Override
            public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                CheckedTextView view = (CheckedTextView) super.getView(position, convertView, parent);
                // Replace text with my own
                view.setText(Objects.requireNonNull(getItem(position)).getSpinnerTxt());
                return view;
            }
        };

        selectionAdapter.add(new SpinnerBO(MONTH, getResources().getString(R.string.month)));
        selectionAdapter.add(new SpinnerBO(DAY, getResources().getString(R.string.day)));
        selectionAdapter.add(new SpinnerBO(WEEK, getResources().getString(R.string.week)));
    }

    private void showSelectionDialog() {
        AlertDialog.Builder builder;

        builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(null);
        builder.setSingleChoiceItems(selectionAdapter, mSelectedType,
                (dialog, item) -> {
                    mSelectedType = item;
                    switchViews();
                    dialog.dismiss();
                });

        applyAlertDialogTheme(getActivity(), builder);
    }

    private void switchViews() {
        switch (mSelectedType) {
            case MONTH:
                rvCalendar.setVisibility(View.VISIBLE);
                llWeekTitle.setVisibility(View.VISIBLE);
                fabAddRetailer.setVisibility(View.VISIBLE);
                mWeekView.setVisibility(View.GONE);
                btnSwitch.setText(getResources().getString(R.string.month));
                presenter.loadCalendar();
                break;
            case DAY:
                rvCalendar.setVisibility(View.GONE);
                llWeekTitle.setVisibility(View.GONE);
                fabAddRetailer.setVisibility(View.GONE);
                mWeekView.setVisibility(View.VISIBLE);
                btnSwitch.setText(getResources().getString(R.string.day));
                presenter.loadADay();
                break;
            case WEEK:
                rvCalendar.setVisibility(View.GONE);
                llWeekTitle.setVisibility(View.GONE);
                fabAddRetailer.setVisibility(View.GONE);
                mWeekView.setVisibility(View.VISIBLE);
                presenter.loadAWeek();
                btnSwitch.setText(getResources().getString(R.string.week));
                break;
        }

    }

    @Override
    public void onDateSelected(String selectedDate) {
        showMessage(selectedDate);
        presenter.setSelectedDate(selectedDate);
        mSelectedType = DAY;
        switchViews();
    }

    @Override
    public List<? extends WeekViewEvent> onMonthChange(int newYear, int newMonth) {
        List<WeekViewEvent> events = new ArrayList<>();
        Calendar startTime = Calendar.getInstance();
        startTime.set(Calendar.HOUR_OF_DAY, 3);
        startTime.set(Calendar.MINUTE, 0);
        startTime.set(Calendar.MONTH, newMonth - 1);
        startTime.set(Calendar.YEAR, newYear);
        Calendar endTime = (Calendar) startTime.clone();
        endTime.add(Calendar.HOUR, 1);
        endTime.set(Calendar.MONTH, newMonth - 1);
        WeekViewEvent event = new WeekViewEvent(1, "Retailer 123", startTime, endTime);
        event.setColor(getResources().getColor(R.color.Orange));
        events.add(event);


        startTime = Calendar.getInstance();
        startTime.set(Calendar.HOUR_OF_DAY, 5);
        startTime.set(Calendar.MINUTE, 0);
        startTime.set(Calendar.MONTH, newMonth - 1);
        startTime.set(Calendar.YEAR, newYear);
        endTime = (Calendar) startTime.clone();
        endTime.set(Calendar.HOUR_OF_DAY, 7);
        endTime.set(Calendar.MINUTE, 0);
        endTime.set(Calendar.MONTH, newMonth - 1);
        event = new WeekViewEvent(10, "Retailer 007", startTime, endTime);
        event.setColor(getResources().getColor(R.color.colorPrimaryDarkGreen));
        events.add(event);

        return events;
    }

    @Override
    public void onEmptyViewLongPress(Calendar time) {

    }

    @Override
    public void onEventClick(WeekViewEvent event, RectF eventRect) {

    }

    @Override
    public void onEventLongPress(WeekViewEvent event, RectF eventRect) {

    }

    private void setupDateTimeInterpreter(final boolean shortDate) {
        mWeekView.setDateTimeInterpreter(new DateTimeInterpreter() {
            @Override
            public String interpretDate(Calendar date) {
                SimpleDateFormat weekdayNameFormat = new SimpleDateFormat("EEE", Locale.getDefault());
                String weekday = weekdayNameFormat.format(date.getTime());
                SimpleDateFormat format = new SimpleDateFormat(" M/d", Locale.getDefault());

                // All android api level do not have a standard way of getting the first letter of
                // the week day name. Hence we get the first char programmatically.
                // Details: http://stackoverflow.com/questions/16959502/get-one-letter-abbreviation-of-week-day-of-a-date-in-java#answer-16959657
                if (shortDate)
                    weekday = String.valueOf(weekday.charAt(0));
                return weekday.toUpperCase() + format.format(date.getTime());
            }

            @Override
            public String interpretTime(int hour) {
                return hour > 11 ? (hour - 12) + " PM" : (hour == 0 ? "12 AM" : hour + " AM");
            }
        });
    }
}
