package com.ivy.ui.offlineplan.view;

import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.animation.AnimationSet;
import android.view.animation.TranslateAnimation;

import com.ivy.core.base.presenter.BasePresenter;
import com.ivy.core.base.view.BaseFragment;
import com.ivy.lib.MonthView.MonthRecyclerView;
import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.bo.CalenderBO;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.ui.offlineplan.OfflinePlanContract;
import com.ivy.ui.offlineplan.adapter.CalendarClickListner;
import com.ivy.ui.offlineplan.adapter.MonthViewAdapter;
import com.ivy.ui.offlineplan.di.DaggerOfflinePlanComponent;
import com.ivy.ui.offlineplan.di.OfflinePlanModule;

import java.util.ArrayList;
import java.util.Objects;

import javax.inject.Inject;

import butterknife.BindView;


public class OfflinePlanFragment extends BaseFragment implements OfflinePlanContract.OfflinePlanView, CalendarClickListner {

    private String screenTitle;
    private MonthViewAdapter monthViewAdapter;
    @BindView(R.id.rv_calendar)
    MonthRecyclerView rvCalendar;

    @Inject
    OfflinePlanContract.OfflinePlanPresenter<OfflinePlanContract.OfflinePlanView> presenter;

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
    }

    @Override
    public void loadCalendarView(ArrayList<String> mAllowedDates, int dayInWeekCount, ArrayList<CalenderBO> mCalenderAllList) {
        monthViewAdapter = new MonthViewAdapter(getActivity(), dayInWeekCount, mCalenderAllList, mAllowedDates, this);
        rvCalendar.setAdapter(monthViewAdapter);
    }

    @Override
    public double getContainerHeight(double ratio) {
        return 0;
    }
}
