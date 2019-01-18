package com.ivy.ui.task.view;

import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.ivy.core.base.view.BaseFragment;
import com.ivy.sd.png.asean.view.R;

import java.util.Objects;

public class TaskFragment extends BaseFragment {
    @Override
    public void initializeDi() {

    }

    @Override
    protected int setContentViewLayout() {
        return R.layout.task_fragment;
    }

    @Override
    public void initVariables(View view) {

    }

    @Override
    protected void getMessageFromAliens() {

    }

    @Override
    protected void setUpViews() {

    }


    private void setUpActionBar(){
        ActionBar actionBar = ((AppCompatActivity) Objects.requireNonNull(getActivity())).getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowTitleEnabled(false);
            actionBar.setDisplayShowHomeEnabled(true);

            setScreenTitle(getString(R.string.task));
        }

    }
}
