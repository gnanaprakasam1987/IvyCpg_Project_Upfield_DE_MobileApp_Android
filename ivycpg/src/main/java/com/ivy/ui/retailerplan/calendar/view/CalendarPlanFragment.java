package com.ivy.ui.retailerplan.calendar.view;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.RectF;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.CoordinatorLayout;
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
import android.widget.ArrayAdapter;
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
import com.ivy.sd.png.bo.RetailerMasterBO;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.util.Commons;
import com.ivy.sd.png.util.MyDatePickerDialog;
import com.ivy.ui.retailer.viewretailers.view.list.RetailerListActivity;
import com.ivy.ui.retailer.viewretailers.view.map.RetailerMapFragment;
import com.ivy.ui.retailerplan.addplan.DateWisePlanBo;
import com.ivy.ui.retailerplan.addplan.view.AddPlanDialogFragment;
import com.ivy.ui.retailerplan.calendar.CalendarPlanContract;
import com.ivy.ui.retailerplan.calendar.adapter.BottmSheetRetailerInfoAdapter;
import com.ivy.ui.retailerplan.calendar.adapter.CalendarClickListner;
import com.ivy.ui.retailerplan.calendar.adapter.MonthViewAdapter;
import com.ivy.ui.retailerplan.calendar.adapter.WeekFilterAdapter;
import com.ivy.ui.retailerplan.calendar.bo.CalenderBO;
import com.ivy.ui.retailerplan.calendar.di.CalendarPlanModule;
import com.ivy.ui.retailerplan.calendar.di.DaggerCalendarPlanComponent;
import com.ivy.utils.DateTimeUtils;
import com.ivy.utils.DeviceUtils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

import javax.inject.Inject;

import butterknife.BindView;

import static android.app.Activity.RESULT_OK;
import static com.ivy.cpg.view.homescreen.HomeMenuConstants.MENU_MAP_PLAN;


public class CalendarPlanFragment extends BaseFragment implements CalendarPlanContract.CalendarPlanView, CalendarClickListner,
        WeekView.EventClickListener, MonthLoader.MonthChangeListener, WeekView.EventLongPressListener,
        WeekView.EmptyViewClickListener {

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

    @BindView(R.id.coordinator)
    CoordinatorLayout coordinatorLayout;

    @BindView(R.id.txt_1)
    TextView txtDay1;
    @BindView(R.id.txt_2)
    TextView txtDay2;
    @BindView(R.id.txt_3)
    TextView txtDay3;
    @BindView(R.id.txt_4)
    TextView txtDay4;
    @BindView(R.id.txt_5)
    TextView txtDay5;
    @BindView(R.id.txt_6)
    TextView txtDay6;
    @BindView(R.id.txt_7)
    TextView txtDay7;


    RecyclerView rvRetailerInfo;
    TextView tvNoPlan, tvNoVisit, tvFromDate, tvToDate, tvCopyPlan, tvToWeek;

    @Inject
    CalendarPlanContract.CalendarPlanPresenter<CalendarPlanContract.CalendarPlanView> presenter;

    private String generalPattern = "yyyy/MM/dd";

    private int mSelectedType = 0;
    private final int MONTH = 0, DAY = 1, WEEK = 2;
    private Context mContext;
    private BottomSheetBehavior rtlInfoBehavior, copyPlanBehavior;
    public static final int REQUEST_CODE = 1;
    private int mSelectedIndex = 0;

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
        initRetailerInfoBottmSheet();
        initCopyPlanBottmSheet();
        presenter.fetchEventsFromDb(true);

        rvWeek.setLayoutManager(new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false));

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
        mWeekView.setEmptyViewClickListener(this);
        mWeekView.setXScrollingSpeed(0);
        mWeekView.setTimeTextPaint(getResources().getColor(R.color.black_bg1));

        ivNext.setOnClickListener(v -> {
            hideCopyPlanBottomSheet();
            if (mSelectedType == WEEK || mSelectedType == DAY)
                presenter.onNextWeekClicked(mSelectedType == DAY);
            else
                presenter.onNextMonthClicked();

        });

        ivPrev.setOnClickListener(v -> {
            hideCopyPlanBottomSheet();
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
                    hideRetailerInfoBottomSheet();
                    presenter.loadADay();
                } else {
                    mSelectedType = WEEK;
                    rvCalendar.setVisibility(View.GONE);
                    llWeekTitle.setVisibility(View.GONE);
                    fabAddRetailer.setVisibility(View.GONE);
                    rvWeek.setVisibility(View.VISIBLE);
                    mWeekView.setVisibility(View.VISIBLE);
                    hideRetailerInfoBottomSheet();
                    presenter.loadAWeek();
                }
            }
            getActivity().invalidateOptionsMenu();
        });

        fabAddRetailer.setOnClickListener(v -> {
            Intent i = new Intent(getActivity(), RetailerListActivity.class);
            i.putExtra("selectedDate", presenter.getSelectedDate());
            startActivityForResult(i, REQUEST_CODE);
        });

        tvToDate.setOnClickListener(v -> {
            showDatePickerDialog();
        });

        tvToWeek.setOnClickListener(v -> {
            showWeekSelectionDialog();
        });

        tvCopyPlan.setOnClickListener(v -> {
            if (mSelectedType == DAY && tvToDate.getText().toString().equals(mContext.getResources().getString(R.string.select_date)))
                showMessage(mContext.getResources().getString(R.string.select_date));
            else if (mSelectedType == WEEK && tvToWeek.getText().toString().equals(mContext.getResources().getString(R.string.select_week)))
                showMessage(mContext.getResources().getString(R.string.select_week));
            else if (mSelectedType == DAY && tvFromDate.getText().toString().equals(tvToDate.getText().toString()))
                showMessage(mContext.getResources().getString(R.string.from_to_date));
            else if (mSelectedType == DAY && tvFromDate.getText().toString().equals(tvToWeek.getText().toString()))
                showMessage(mContext.getResources().getString(R.string.from_to_week));
            else {
                if (mSelectedType == DAY)
                    proceesCopyPlan(tvFromDate.getText().toString(), tvToDate.getText().toString());
                else
                    processWeekCopyPlan(tvFromDate.getText().toString(), tvToWeek.getText().toString());
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE && resultCode == RESULT_OK) {
            presenter.fetchEventsFromDb(false);
        }
    }

    private void initRetailerInfoBottmSheet() {
        View retailerInfoBtmSheet = coordinatorLayout.findViewById(R.id.bottomsheet);
        tvNoPlan = retailerInfoBtmSheet.findViewById(R.id.tv_no_plan);
        rvRetailerInfo = retailerInfoBtmSheet.findViewById(R.id.rvRetailerInfo);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.VERTICAL, false);
        rvRetailerInfo.setLayoutManager(linearLayoutManager);

        rtlInfoBehavior = BottomSheetBehavior.from(retailerInfoBtmSheet);
        rtlInfoBehavior.setSkipCollapsed(true);
        rtlInfoBehavior.setHideable(false);

        if (rtlInfoBehavior != null)
            rtlInfoBehavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
                @Override
                public void onStateChanged(@NonNull View bottomSheet, int newState) {
                    switch (newState) {
                        case BottomSheetBehavior.STATE_HIDDEN:
                            break;
                        case BottomSheetBehavior.STATE_EXPANDED:
                            break;
                        case BottomSheetBehavior.STATE_COLLAPSED:
                            break;
                        case BottomSheetBehavior.STATE_DRAGGING:
                            break;
                        case BottomSheetBehavior.STATE_SETTLING:
                            break;
                    }
                }

                @Override
                public void onSlide(@NonNull View bottomSheet, float slideOffset) {
                }
            });
    }

    private void initCopyPlanBottmSheet() {
        View copyPlanBtmSheet = coordinatorLayout.findViewById(R.id.copy_plan_btm_sheet);
        tvNoVisit = copyPlanBtmSheet.findViewById(R.id.tv_no_visit_value);
        tvFromDate = copyPlanBtmSheet.findViewById(R.id.tv_from_date_value);
        tvToDate = copyPlanBtmSheet.findViewById(R.id.tv_to_date_value);
        tvCopyPlan = copyPlanBtmSheet.findViewById(R.id.tv_copy_plan);
        tvToWeek = copyPlanBtmSheet.findViewById(R.id.tv_to_week_value);

        copyPlanBehavior = BottomSheetBehavior.from(copyPlanBtmSheet);

        if (copyPlanBehavior != null)
            copyPlanBehavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
                @Override
                public void onStateChanged(@NonNull View bottomSheet, int newState) {
                    switch (newState) {
                        case BottomSheetBehavior.STATE_HIDDEN:
                            break;
                        case BottomSheetBehavior.STATE_EXPANDED:
                            break;
                        case BottomSheetBehavior.STATE_COLLAPSED:
                            break;
                        case BottomSheetBehavior.STATE_DRAGGING:
                            break;
                        case BottomSheetBehavior.STATE_SETTLING:
                            break;
                    }
                }

                @Override
                public void onSlide(@NonNull View bottomSheet, float slideOffset) {

                }
            });

    }

    @Override
    public void loadCalendarView(ArrayList<String> mAllowedDates, int dayInWeekCount, ArrayList<CalenderBO> mCalenderAllList, List<String> weekNoList) {
        MonthViewAdapter monthViewAdapter = new MonthViewAdapter(getActivity(), dayInWeekCount, mCalenderAllList,
                mAllowedDates, this, weekNoList);
        rvCalendar.setAdapter(monthViewAdapter);
        if (presenter.isPastDate(presenter.getSelectedDate()))
            fabAddRetailer.setVisibility(View.GONE);
        else
            fabAddRetailer.setVisibility(View.VISIBLE);
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
    public void loadTopWeekView(ArrayList<CalenderBO> mCalenderAllList, ArrayList<String> mAllowedDates) {
        int itemWidth = DeviceUtils.getDeviceWidth(Objects.requireNonNull(getActivity())) / 8;
        rvWeek.setAdapter(new WeekFilterAdapter(getActivity(), mCalenderAllList, mAllowedDates, this, itemWidth, mSelectedType == DAY));
    }

    @Override
    public void loadRetailerInfoBtmSheet(List<DateWisePlanBo> retailerInfoList) {
        if (rtlInfoBehavior.getState() == BottomSheetBehavior.STATE_COLLAPSED) {
            rtlInfoBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
        }
        if (retailerInfoList.size() > 0) {
            tvNoPlan.setVisibility(View.GONE);
            rvRetailerInfo.setVisibility(View.VISIBLE);
            rvRetailerInfo.setAdapter(new BottmSheetRetailerInfoAdapter(getActivity(), retailerInfoList));
        } else {
            tvNoPlan.setVisibility(View.VISIBLE);
            rvRetailerInfo.setVisibility(View.GONE);
        }
    }

    @Override
    public void reloadView() {
        switch (mSelectedType) {
            case MONTH:
                presenter.loadCalendar();
                break;
            case DAY:
                mWeekView.notifyDatasetChanged();
                hideRetailerInfoBottomSheet();
                break;
            case WEEK:
                mWeekView.notifyDatasetChanged();
                hideRetailerInfoBottomSheet();
                break;
        }
    }

    @Override
    public void loadAddPlanDialog(String date, RetailerMasterBO retailerMasterBO) {
        AddPlanDialogFragment addPlanDialogFragment;
        ArrayList<DateWisePlanBo> planList = presenter.getSelectedDateRetailerPlanList();
        addPlanDialogFragment = new AddPlanDialogFragment(date, retailerMasterBO,
                presenter.getSelectedRetailerPlan(retailerMasterBO.getRetailerID()), planList);
        addPlanDialogFragment.show(((FragmentActivity) mContext).getSupportFragmentManager(),
                "add_plan_fragment");
    }

    @Override
    public void setWeekDayText(List<String> weekDayText) {

        for (int i = 0; i < weekDayText.size(); i++) {
            switch (i) {
                case 0:
                    txtDay1.setText(weekDayText.get(i));
                    break;
                case 1:
                    txtDay2.setText(weekDayText.get(i));
                    break;
                case 2:
                    txtDay3.setText(weekDayText.get(i));
                    break;
                case 3:
                    txtDay4.setText(weekDayText.get(i));
                    break;
                case 4:
                    txtDay5.setText(weekDayText.get(i));
                    break;
                case 5:
                    txtDay6.setText(weekDayText.get(i));
                    break;
                case 6:
                    txtDay7.setText(weekDayText.get(i));
                    break;
            }
        }

    }

    private void hideRetailerInfoBottomSheet() {
        if (rtlInfoBehavior.getState() == BottomSheetBehavior.STATE_EXPANDED) {
            rtlInfoBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
        }
    }

    private void hideCopyPlanBottomSheet() {
        if (copyPlanBehavior.getState() == BottomSheetBehavior.STATE_EXPANDED) {
            copyPlanBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
        }
    }

    @Override
    public void onADayRetailerSelected(String selectedDate) {
        showMessage(selectedDate);
        presenter.setSelectedDate(selectedDate);
        rbgSelection.check(R.id.rbDay);
    }

    @Override
    public void onWeekDateSelected(String selectedDate) {
        hideCopyPlanBottomSheet();
        showMessage(selectedDate);
        presenter.setSelectedDate(selectedDate);
        if (mSelectedType == DAY)
            presenter.loadADay();
    }

    @Override
    public void onDateNoSelected(String selectedDate, List<DateWisePlanBo> planList) {
        showMessage(selectedDate);
        presenter.setSelectedDate(selectedDate);
        if (presenter.isPastDate(selectedDate))
            fabAddRetailer.setVisibility(View.GONE);
        else
            fabAddRetailer.setVisibility(View.VISIBLE);
        loadRetailerInfoBtmSheet(planList);

    }


    @Override
    public List<? extends WeekViewEvent> onMonthChange(int newYear, int newMonth) {
        return presenter.getPlannedEvents(newYear, newMonth);
    }

    @Override
    public void onEmptyViewClicked(Calendar time) {
        hideCopyPlanBottomSheet();
        Commons.print(String.format(Locale.ENGLISH, "Event of %02d:%02d %s/%d",
                time.get(Calendar.HOUR_OF_DAY), time.get(Calendar.MINUTE),
                time.get(Calendar.MONTH) + 1, time.get(Calendar.DAY_OF_MONTH)));
        if (presenter.isPastDate(DateTimeUtils.convertDateObjectToRequestedFormat(time.getTime(), generalPattern))) {
            showMessage(Objects.requireNonNull(getActivity()).getResources().getString(R.string.not_allowed_to_add_plan));
        } else {
            Intent i = new Intent(getActivity(), RetailerListActivity.class);
            i.putExtra("selectedDate", DateTimeUtils.convertDateObjectToRequestedFormat(time.getTime(), generalPattern));
            i.putExtra("startTime", String.format(Locale.ENGLISH, "%02d", time.get(Calendar.HOUR_OF_DAY)));
            startActivityForResult(i, REQUEST_CODE);
        }
    }

    @Override
    public void onEventClick(WeekViewEvent event, RectF eventRect) {
        hideCopyPlanBottomSheet();
        RetailerMasterBO retailerMasterBO = presenter.getPlanedRetailerBo(event.getRetailerId());
        presenter.setRetailerMasterBo(retailerMasterBO);
        if (retailerMasterBO != null) {
            presenter.fetchSelectedDateRetailerPlan(DateTimeUtils.convertDateObjectToRequestedFormat
                    (event.getStartTime().getTime(), generalPattern), retailerMasterBO);
        }
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
        menu.findItem(R.id.search).setVisible(false);
        menu.findItem(R.id.copy).setVisible(mSelectedType == DAY || (mSelectedType == WEEK && presenter.getWeekList().size() > 0));
        hideCopyPlanBottomSheet();
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
            Bundle bndl = new Bundle();
            bndl.putString("screentitle", screenTitle);
            bndl.putString("selectedDate", presenter.getSelectedDate());
            fragment.setArguments(bndl);
            ft.replace(R.id.fragment_content, fragment, MENU_MAP_PLAN);
            ft.commit();
            return true;
        } else if (item.getItemId() == R.id.copy) {
            loadCopyPlanBtmSheet();
            return true;
        }

        return false;
    }

    private void loadCopyPlanBtmSheet() {
        int no_of_visits = 0;
        if (mSelectedType == DAY)
            no_of_visits = presenter.getADayPlan(presenter.getSelectedDate()).size();
        else
            no_of_visits = presenter.getWeeksPlanCount(presenter.getWeekNo(presenter.getSelectedDate()));
        if (no_of_visits > 0) {
            if (copyPlanBehavior.getState() == BottomSheetBehavior.STATE_COLLAPSED) {
                copyPlanBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                if (mSelectedType == DAY) {
                    tvFromDate.setText(presenter.getSelectedDate());
                    tvNoVisit.setText("" + no_of_visits);
                    tvToDate.setVisibility(View.VISIBLE);
                    tvToWeek.setVisibility(View.INVISIBLE);
                    tvToDate.setText(mContext.getResources().getString(R.string.select_date));
                } else {
                    //week
                    tvFromDate.setText(presenter.getWeekNo(presenter.getSelectedDate()));
                    tvNoVisit.setText("" + no_of_visits);
                    tvToDate.setVisibility(View.INVISIBLE);
                    tvToWeek.setVisibility(View.VISIBLE);
                    tvToWeek.setText(mContext.getResources().getString(R.string.select_week));
                }
            }
        } else
            showMessage(mContext.getResources().getString(R.string.plan_not_available));
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = context;
    }

    @Override
    public void onStart() {
        super.onStart();
        if (!EventBus.getDefault().isRegistered(this))
            EventBus.getDefault().register(this);

    }

    @Override
    public void onStop() {
        if (EventBus.getDefault().isRegistered(this))
            EventBus.getDefault().unregister(this);
        super.onStop();
    }

    @Subscribe
    public void onMessageEvent(Object obj) {
        presenter.fetchEventsFromDb(false);
    }

    private void showDatePickerDialog() {
        Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);

        MyDatePickerDialog d = new MyDatePickerDialog(mContext, R.style.SellerDatePickerStyle,
                mDateSetListener, year, month, day);
        d.setPermanentTitle(getString(R.string.choose_date));
        d.getDatePicker().setMinDate(c.getTimeInMillis());
        d.getDatePicker().setMaxDate(presenter.getMaxPlanDate());
        d.show();
    }

    private DatePickerDialog.OnDateSetListener mDateSetListener = (view, year, month, day) -> {
        Calendar selectedDate = new GregorianCalendar(year, month, day);
        tvToDate.setText(DateTimeUtils.convertDateObjectToRequestedFormat(selectedDate.getTime(), generalPattern));

    };

    private void proceesCopyPlan(String fromDate, String toDate) {
        if (presenter.getADayPlan(toDate).size() > 0) {
            showAlert("", mContext.getResources().getString(R.string.do_you_want_delete_plan_copy),
                    () -> {
                        hideCopyPlanBottomSheet();
                        presenter.deleteAndCopyPlan(fromDate, toDate);
                    }, () -> {

                    });
        } else {
            hideCopyPlanBottomSheet();
            presenter.copyPlan(fromDate, toDate);
        }

    }

    private void processWeekCopyPlan(String fromWeek, String toWeek) {
        if (presenter.getWeeksPlanCount(toWeek) > 0) {
            showAlert("", mContext.getResources().getString(R.string.do_you_want_delete_plan_copy),
                    () -> {
                        mSelectedIndex = 0;
                        hideCopyPlanBottomSheet();
                        presenter.deleteCopyWeekPlan(fromWeek, toWeek);
                    }, () -> {

                    });
        } else {
            mSelectedIndex = 0;
            hideCopyPlanBottomSheet();
            presenter.copyWeekPlan(fromWeek, toWeek);
        }

    }

    private void showWeekSelectionDialog() {

        ArrayAdapter<String> mWeekAdapter = new ArrayAdapter<>(mContext, android.R.layout.select_dialog_singlechoice);
        mWeekAdapter.addAll(presenter.getWeekNoList());

        AlertDialog.Builder builder;

        builder = new AlertDialog.Builder(mContext);
        builder.setTitle(mContext.getResources().getString(R.string.select_week));
        builder.setSingleChoiceItems(mWeekAdapter, mSelectedIndex,
                (dialog, item) -> {
                    mSelectedIndex = item;
                    tvToWeek.setText(mWeekAdapter.getItem(item));
                    dialog.dismiss();
                });

        applyAlertDialogTheme(mContext, builder);
    }
}
