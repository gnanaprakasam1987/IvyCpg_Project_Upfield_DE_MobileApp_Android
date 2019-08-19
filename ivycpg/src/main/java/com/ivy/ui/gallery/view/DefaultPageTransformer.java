package com.ivy.ui.gallery.view;

import android.view.View;

import androidx.annotation.NonNull;
import androidx.viewpager.widget.ViewPager;

public class DefaultPageTransformer implements ViewPager.PageTransformer {

    public void transformPage(@NonNull View view, float position) {

        view.setRotationX(0);
        view.setRotationY(0);
        view.setRotation(0);
        view.setScaleX(1);
        view.setScaleY(1);
        view.setPivotX(0);
        view.setPivotY(0);
        view.setTranslationY(0);
        view.setTranslationX(0f);
        view.setAlpha(position <= -1f || position >= 1f ? 0f : 1f);
    }
}
