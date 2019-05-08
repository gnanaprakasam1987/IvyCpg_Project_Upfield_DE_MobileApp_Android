package com.ivy.ui.offlineplan.calendar.view;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.RectF;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.ivy.calendarlibrary.monthview.MonthView;
import com.ivy.calendarlibrary.weekview.DateTimeInterpreter;
import com.ivy.calendarlibrary.weekview.MonthLoader;
import com.ivy.calendarlibrary.weekview.WeekView;
import com.ivy.calendarlibrary.weekview.WeekViewEvent;
import com.ivy.core.base.presenter.BasePresenter;
import com.ivy.core.base.view.BaseFragment;
import com.ivy.cpg.view.homescreen.HomeScreenActivity;
import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.ui.offlineplan.addplan.DateWisePlanBo;
import com.ivy.ui.offlineplan.calendar.CalendarPlanContract;
import com.ivy.ui.offlineplan.calendar.adapter.CalendarClickListner;
import com.ivy.ui.offlineplan.calendar.adapter.MonthViewAdapter;
import com.ivy.ui.offlineplan.calendar.adapter.WeekFilterAdapter;
import com.ivy.ui.offlineplan.calendar.bo.CalenderBO;
import com.ivy.ui.offlineplan.calendar.di.CalendarPlanModule;
import com.ivy.ui.offlineplan.calendar.di.DaggerCalendarPlanComponent;
import com.ivy.ui.retailer.view.map.RetailerMapFragment;
import com.ivy.utils.DateTimeUtils;
import com.ivy.utils.DeviceUtils;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Objects;

import javax.inject.Inject;

import butterknife.BindView;

import static com.ivy.cpg.view.homescreen.HomeMenuConstants.MENU_MAP_PLAN;


public class CalendarPlanFragment extends BaseFragment implements CalendarPlanContract.CalendarPlanView, CalendarClickListner, WeekView.EventClickListener, MonthLoader.MonthChangeListener, WeekView.EventLongPressListener, WeekView.EmptyViewLongPressListener {

    private String screenTitle;

    @BindView(R.id.rv_calendar)
    MonthView rvCalendar;

    @BindView(R.id.img_prev)
    ImageView ivPrev;

    @BindView(R.id.img_next)
    ImageView ivNext;

    @BindView(R.id.tv_month)
    TextView tvMonth;

    @BindView(R.id.ll_titleList)
    LinearLayout llWeekTitle;

    @BindView(R.id.fab_retailer)
    FloatingActionButton fabAddRetailer;

    @BindView(R.id.week_view)
    WeekView mWeekView;

    @BindView(R.id.rv_week)
    RecyclerView rvWeek;

    @BindView(R.id.radioGrp)
    RadioGroup rbgSelection;

    @Inject
    CalendarPlanContract.CalendarPlanPresenter<CalendarPlanContract.CalendarPlanView> presenter;

    private int mSelectedType = 0;
    private final int MONTH = 0, DAY = 1, WEEK = 2;
    private Context mContext;
    private MonthViewAdapter monthViewAdapter;

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
    public void init(View view) {
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
        setHasOptionsMenu(true);
        presenter.setPlanDates();
        presenter.loadCalendar();

        if (DeviceUtils.isTabletDevice(Objects.requireNonNull(getActivity())))
            mWeekView.setHeaderColumnPadding((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, getResources().getDimension(R.dimen.time_column_width), getResources().getDisplayMetrics()));
        else
            mWeekView.setHeaderColumnPadding((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8, getResources().getDisplayMetrics()));

        // Show a toast message about the touched event.
        mWeekView.setOnEventClickListener(this);

        // The week view has infinite scrolling horizontally. We have to provide the events of a
        // month every time the month changes on the week view.
        mWeekView.setMonthChangeListener(this);

        // Set long press listener for events.
        mWeekView.setEventLongPressListener(this);

        // Set long press listener for empty view
        mWeekView.setEmptyViewLongPressListener(this);
        mWeekView.setXScrollingSpeed(0);
        mWeekView.setTimeTextPaint(getResources().getColor(R.color.black_bg1));

        ivNext.setOnClickListener(v -> {
            if (mSelectedType == WEEK || mSelectedType == DAY)
                presenter.onNextWeekClicked(mSelectedType == DAY);
            else
                presenter.onNextMonthClicked();

        });

        ivPrev.setOnClickListener(v -> {
            if (mSelectedType == WEEK || mSelectedType == DAY)
                presenter.onPreviousWeekClicked(mSelectedType == DAY);
            else
                presenter.onPreviousMonthClicked();

        });


        rbgSelection.setOnCheckedChangeListener((group, checkedId) -> {
            RadioButton checkedRadioButton = group.findViewById(checkedId);
            if (checkedRadioButton.isChecked()) {
                if (checkedRadioButton.getText().equals(getString(R.string.month))) {
                    mSelectedType = MONTH;
                    rvCalendar.setVisibility(View.VISIBLE);
                    llWeekTitle.setVisibility(View.VISIBLE);
                    fabAddRetailer.setVisibility(View.VISIBLE);
                    mWeekView.setVisibility(View.GONE);
                    rvWeek.setVisibility(View.GONE);
                    presenter.loadCalendar();
                } else if (checkedRadioButton.getText().equals(getString(R.string.day))) {
                    mSelectedType = DAY;
                    rvCalendar.setVisibility(View.GONE);
                    llWeekTitle.setVisibility(View.GONE);
                    fabAddRetailer.setVisibility(View.GONE);
                    rvWeek.setVisibility(View.VISIBLE);
                    mWeekView.setVisibility(View.VISIBLE);
                    presenter.loadADay();
                } else {
                    mSelectedType = WEEK;
                    rvCalendar.setVisibility(View.GONE);
                    llWeekTitle.setVisibility(View.GONE);
                    fabAddRetailer.setVisibility(View.GONE);
                    rvWeek.setVisibility(View.VISIBLE);
                    mWeekView.setVisibility(View.VISIBLE);
                    presenter.loadAWeek();
                }


            }
        });
    }

    @Override
    public void loadCalendarView(ArrayList<String> mAllowedDates, int dayInWeekCount, ArrayList<CalenderBO> mCalenderAllList) {
        monthViewAdapter = new MonthViewAdapter(getActivity(), dayInWeekCount, mCalenderAllList, mAllowedDates, this);
        rvCalendar.setAdapter(monthViewAdapter);
    }

    @Override
    public void setMonthName(String monthName) {
        tvMonth.setText(monthName);
    }

    @Override
    public void loadDayView(Calendar date) {
        setupDateTimeInterpreter();
        mWeekView.setColumnGap((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8, getResources().getDisplayMetrics()));
        mWeekView.setNumberOfVisibleDays(1);
        mWeekView.goToDate(date);
    }

    @Override
    public void loadWeekView(Calendar date) {
        setupDateTimeInterpreter();
        mWeekView.setColumnGap((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 2, getResources().getDisplayMetrics()));
        mWeekView.setNumberOfVisibleDays(7);
        mWeekView.goToDate(date);
    }

    @Override
    public void refreshGrid() {
        monthViewAdapter.notifyDataSetChanged();
    }

    @Override
    public void loadTopWeekView(ArrayList<CalenderBO> mCalenderAllList, ArrayList<String> mAllowedDates) {
        int itemWidth = DeviceUtils.getDeviceWidth(Objects.requireNonNull(getActivity())) / 8;
        rvWeek.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false));
        WeekFilterAdapter weekFilterAdapter = new WeekFilterAdapter(getActivity(), mCalenderAllList, mAllowedDates, this, itemWidth, mSelectedType == DAY);
        rvWeek.setAdapter(weekFilterAdapter);
    }


    @Override
    public void onDateSelected(String selectedDate) {
        showMessage(selectedDate);
        presenter.setSelectedDate(selectedDate);
        rbgSelection.check(R.id.rbDay);
    }

    @Override
    public void onWeekDateSelected(String selectedDate) {
        showMessage(selectedDate);
        presenter.setSelectedDate(selectedDate);
        if (mSelectedType == DAY)
            presenter.loadADay();
    }

    @Override
    public ArrayList<DateWisePlanBo> getDaysPlan(String date) {
        return presenter.getADayPlan(date);
    }


    @Override
    public List<? extends WeekViewEvent> onMonthChange(int newYear, int newMonth) {
        List<WeekViewEvent> events = new ArrayList<>();
        Calendar startTime = Calendar.getInstance();
        startTime.setTime(DateTimeUtils.convertStringToDateObject("2019/05/01", "yyyy/MM/dd"));
        if (newMonth == startTime.get(Calendar.MONTH) + 1) {
            startTime.set(Calendar.HOUR_OF_DAY, 3);
            startTime.set(Calendar.MINUTE, 0);
            Calendar endTime = (Calendar) startTime.clone();
            endTime.add(Calendar.HOUR, 1);
            WeekViewEvent event = new WeekViewEvent(1, "Retailer 123", "Retailer Address", startTime, endTime);
            event.setColor(R.attr.colorPrimary);
            events.add(event);
        }


        startTime = Calendar.getInstance();
        startTime.setTime(DateTimeUtils.convertStringToDateObject("2019/05/01", "yyyy/MM/dd"));
        if (newMonth == startTime.get(Calendar.MONTH) + 1) {
            startTime.set(Calendar.HOUR_OF_DAY, 4);
            startTime.set(Calendar.MINUTE, 0);
            Calendar endTime = (Calendar) startTime.clone();
            endTime.add(Calendar.HOUR, 1);
            WeekViewEvent event = new WeekViewEvent(1, "Retailer 123", "Retailer Address", startTime, endTime);
            event.setColor(R.attr.colorPrimary);
            events.add(event);
        }

        startTime = Calendar.getInstance();
        startTime.setTime(DateTimeUtils.convertStringToDateObject("2019/05/02", "yyyy/MM/dd"));
        if (newMonth == startTime.get(Calendar.MONTH) + 1) {
            startTime.set(Calendar.HOUR_OF_DAY, 4);
            startTime.set(Calendar.MINUTE, 0);
            Calendar endTime = (Calendar) startTime.clone();
            endTime.add(Calendar.HOUR, 1);
            WeekViewEvent event = new WeekViewEvent(1, "Retailer 123", "Retailer Address", startTime, endTime);
            event.setColor(R.attr.colorPrimary);
            events.add(event);
        }

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

    private void setupDateTimeInterpreter() {
        mWeekView.setDateTimeInterpreter(new DateTimeInterpreter() {
            @Override
            public String interpretDate(Calendar date) {
                return "";
            }

            @Override
            public String interpretTime(int hour) {
                Calendar calendar = Calendar.getInstance();
                calendar.set(Calendar.HOUR_OF_DAY, hour);
                calendar.set(Calendar.MINUTE, 0);
                try {
                    return DateTimeUtils.convertDateObjectToRequestedFormat(calendar.getTime(), "HH:mm");
                } catch (Exception e) {
                    e.printStackTrace();
                    return "";
                }
            }
        });
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);

        menu.findItem(R.id.filter).setVisible(false);
        menu.findItem(R.id.calendar).setVisible(false);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_retailer_plan, menu);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            startActivity(new Intent(getActivity(),
                    HomeScreenActivity.class));
            ((Activity) mContext).finish();
            return true;
        } else if (item.getItemId() == R.id.map_retailer) {

            FragmentManager fm = ((FragmentActivity) mContext).getSupportFragmentManager();
            FragmentTransaction ft = fm.beginTransaction();

            RetailerMapFragment fragment = new RetailerMapFragment();
            ft.replace(R.id.fragment_content, fragment, MENU_MAP_PLAN);
            ft.commit();
            return true;
        }

        return false;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = context;
    }
}
