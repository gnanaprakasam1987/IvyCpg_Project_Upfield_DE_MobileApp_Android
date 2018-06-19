package com.ivy.cpg.view.common;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import butterknife.ButterKnife;
import butterknife.Unbinder;

public abstract class CommonFragment extends Fragment {

    private Unbinder mUnBinder;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(setContentViewLayout(), container, false);
    }


    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mUnBinder = ButterKnife.bind(this, view);
        initVariables(view);
    }


    protected abstract int setContentViewLayout();

    public abstract void initVariables(View view);


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        try {
            mUnBinder.unbind();
            mUnBinder = null;
        } catch (Exception e) {
            e.printStackTrace();
        }

    }


}
