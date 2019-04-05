package com.ivy.ui.offlineplan.calendar.view;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckedTextView;
import android.widget.ImageView;
import android.widget.TextView;

import com.ivy.calendarlibrary.monthview.MonthView;
import com.ivy.core.base.presenter.BasePresenter;
import com.ivy.core.base.view.BaseFragment;
import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.bo.CalenderBO;
import com.ivy.sd.png.bo.SpinnerBO;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.ui.offlineplan.calendar.OfflinePlanContract;
import com.ivy.ui.offlineplan.calendar.adapter.CalendarClickListner;
import com.ivy.ui.offlineplan.calendar.adapter.MonthViewAdapter;
import com.ivy.ui.offlineplan.calendar.di.DaggerOfflinePlanComponent;
import com.ivy.ui.offlineplan.calendar.di.OfflinePlanModule;

import java.util.ArrayList;
import java.util.Objects;

import javax.inject.Inject;

import butterknife.BindView;


public class OfflinePlanFragment extends BaseFragment implements OfflinePlanContract.OfflinePlanView, CalendarClickListner {

    private String screenTitle;
    private MonthViewAdapter monthViewAdapter;
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

    @BindView(R.id.rv_week)
    RecyclerView rvWeek;

    @Inject
    OfflinePlanContract.OfflinePlanPresenter<OfflinePlanContract.OfflinePlanView> presenter;

    private int mSelectedType = 0;

    @Override
    public void initializeDi() {
        DaggerOfflinePlanComponent.builder()
                .ivyAppComponent(((BusinessModel) Objects.requireNonNull(getActivity()).getApplication()).getComponent())
                .offlinePlanModule(new OfflinePlanModule(this, getActivity()))
                .build()
                .inject(OfflinePlanFragment.this);

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

        ivNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                presenter.onNextMonthClicked();
            }
        });

        ivPrev.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                presenter.onPreviousMonthClicked();
            }
        });

        btnSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showSelectionDialog();
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

        selectionAdapter.add(new SpinnerBO(0, getResources().getString(R.string.month)));
        selectionAdapter.add(new SpinnerBO(1, getResources().getString(R.string.day)));
        selectionAdapter.add(new SpinnerBO(2, getResources().getString(R.string.week)));
    }

    private void showSelectionDialog() {
        AlertDialog.Builder builder;

        builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(null);
        builder.setSingleChoiceItems(selectionAdapter, mSelectedType,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int item) {
                        mSelectedType = item;
                        switchViews();
                        dialog.dismiss();
                    }
                });

        applyAlertDialogTheme(getActivity(), builder);
    }

    private void switchViews() {
        final int MONTH = 0, DAY = 1, WEEK = 2;
        switch (mSelectedType) {
            case MONTH:
                rvCalendar.setVisibility(View.VISIBLE);
                rvWeek.setVisibility(View.GONE);
                break;
            case DAY:
                rvCalendar.setVisibility(View.GONE);
                rvWeek.setVisibility(View.VISIBLE);
                break;
            case WEEK:
                break;

        }

    }

    @Override
    public void onDateSelected(String selectedDate) {
        showMessage(selectedDate);
    }
}
