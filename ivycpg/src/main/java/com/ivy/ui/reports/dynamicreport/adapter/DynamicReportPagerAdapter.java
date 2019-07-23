package com.ivy.ui.reports.dynamicreport.adapter;

import android.util.SparseArray;
import android.view.ViewGroup;

import com.ivy.ui.reports.dynamicreport.model.DynamicReportBO;
import com.ivy.ui.reports.dynamicreport.view.DynamicReportTabFragment;

import java.util.ArrayList;
import java.util.HashMap;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

public class DynamicReportPagerAdapter extends FragmentStatePagerAdapter {

    private int mNumOfTabs;
    private HashMap<String, HashMap<String, HashMap<String, String>>> dataMap;
    private HashMap<String, HashMap<String, DynamicReportBO>> fieldsMap;
    private ArrayList<String> headerList;
    private SparseArray<Fragment> registeredFragments = new SparseArray<>();

    public DynamicReportPagerAdapter(FragmentManager fm, int NumOfTabs, HashMap<String, HashMap<String, DynamicReportBO>> fieldList, HashMap<String, HashMap<String, HashMap<String, String>>> dataMap,
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

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        Fragment fragment = (Fragment) super.instantiateItem(container, position);
        registeredFragments.put(position, fragment);
        return fragment;
    }

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        registeredFragments.remove(position);
        super.destroyItem(container, position, object);
    }

    public Fragment getRegisteredFragment(int position) {
        return registeredFragments.get(position);
    }
}
