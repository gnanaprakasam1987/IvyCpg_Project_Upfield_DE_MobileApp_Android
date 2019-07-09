package com.ivy.ui.reports.dynamicreport.adapter;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

import com.ivy.ui.reports.dynamicreport.view.DynamicReportTabFragment;

import java.util.ArrayList;
import java.util.HashMap;

public class DynamicReportPagerAdapter extends FragmentStatePagerAdapter {

    private int mNumOfTabs;
    private HashMap<String, HashMap<String, HashMap<String, String>>> dataMap;
    private HashMap<String, HashMap<String, String>> fieldsMap;
    private ArrayList<String> headerList;

    public DynamicReportPagerAdapter(FragmentManager fm, int NumOfTabs, HashMap<String, HashMap<String, String>> fieldList, HashMap<String, HashMap<String, HashMap<String, String>>> dataMap,
                                     ArrayList<String> headerList) {
        super(fm);
        this.mNumOfTabs = NumOfTabs;
        this.dataMap = dataMap;
        this.fieldsMap = fieldList;
        this.headerList = headerList;
    }

    @Override
    public Fragment getItem(int position) {
        return DynamicReportTabFragment.newInstance(headerList.get(position), dataMap, fieldsMap);
    }

    @Override
    public int getCount() {
        return mNumOfTabs;
    }
}
