package com.ivy.ui.dashboard.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import java.util.ArrayList;

public class FragmentPagerAdapter extends FragmentStatePagerAdapter {
    private ArrayList<Fragment> fragmentList = new ArrayList<>();

    public FragmentPagerAdapter(FragmentManager fragmentManager, ArrayList<Fragment> fragmentList) {
        super(fragmentManager);
        this.fragmentList = fragmentList;
    }

    // Returns total number of pages
    @Override
    public int getCount() {
        return this.fragmentList.size();
    }

    // Returns the fragment to display for that page
    @Override
    public Fragment getItem(int position) {
        return this.fragmentList.get(position);
    }

}