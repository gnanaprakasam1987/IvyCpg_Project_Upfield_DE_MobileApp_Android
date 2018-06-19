package com.ivy.view.activation;


import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.ivy.sd.png.view.Fragment1;

public class ViewPagerAdapter extends FragmentPagerAdapter {
    private int NUM_ITEMS = 0;

    public ViewPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                Fragment1 fragment1 = new Fragment1();
                return fragment1;
            case 1:
                Fragment1 fragment2 = new Fragment1();
                return fragment2;
            case 2:
                Fragment1 fragment3 = new Fragment1();
                return fragment3;
            default:
                return null;
        }
    }

    @Override
    public int getCount() {
        return NUM_ITEMS;
    }
}

