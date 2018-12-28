package com.ivy.ui.attendance.view;

import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;

import com.ivy.core.base.view.BaseFragment;
import com.ivy.sd.png.asean.view.R;

/**
 * Created by mansoor on 27/12/2018
 */
public class TimeTrackingFragment extends BaseFragment {
    @Override
    public void initializeDi() {

    }

    @Override
    protected int setContentViewLayout() {
        return R.layout.fragment_time_tracking;
    }

    @Override
    public void initVariables(View view) {

    }

    @Override
    protected void getMessageFromAliens() {

    }

    @Override
    protected void setUpViews() {
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_nonfield_two, menu);

    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);

        menu.findItem(R.id.menu_select).setVisible(false);

    }

}
