package com.ivy.cpg.view.common;

import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;


public abstract class IvyCommonActivity extends AppCompatActivity {


    private Typeface mTypeface;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTypeFace();
    }


    public void initVariable() {
    }

    public void setTypeFace() {
    }

    public void addFragment(int containerViewId, Fragment fragment, boolean addStack,
                            boolean isReplace, int animationType) {

        if (fragment == null) {
            return;
        }
    }


    public void showCommonErrorDialog() {
    }


    public void showProgress() {
    }

    public void hideProgress() {
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
