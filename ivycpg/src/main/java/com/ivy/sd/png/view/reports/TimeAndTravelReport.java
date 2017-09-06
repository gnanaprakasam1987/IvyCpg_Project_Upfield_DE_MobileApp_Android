package com.ivy.sd.png.view.reports;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTabHost;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.provider.ConfigurationMasterHelper;

/**
 * Created by maheswaran.m on 16-10-2015.
 */
public class TimeAndTravelReport extends Fragment {

    private FragmentTabHost mTabHost;
    private boolean createdTab = false;
    private BusinessModel bmodel;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        bmodel = (BusinessModel) getActivity().getApplicationContext();
        bmodel.setContext(getActivity());
        mTabHost = new FragmentTabHost(getActivity());
        mTabHost.setup(getActivity(), getChildFragmentManager(), R.id.fragment_container);
        mTabHost.setForegroundGravity(Gravity.CENTER);
        mTabHost.addTab(mTabHost.newTabSpec("Time Taken").setIndicator("Time Taken"),
                OrderTakenTimeReport.class, null);
        mTabHost.addTab(mTabHost.newTabSpec("Travel On Map").setIndicator("Travel On Map"),
                TravelOnMap.class, null);


        for (int i = 0; i < mTabHost.getTabWidget().getChildCount(); i++) {
            //Unselected Tabs
            mTabHost.getTabWidget().getChildAt(i).setBackgroundResource(R.drawable.btn);
            TextView tv = (TextView) mTabHost.getTabWidget().getChildAt(i).findViewById(android.R.id.title);
            tv.setTextColor(Color.parseColor("#000000"));
            tv.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.font_small));
            tv.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
        }

        //Unselected Tabs
        mTabHost.getTabWidget().getChildAt(mTabHost.getCurrentTab()).setBackgroundResource(R.drawable.btn);
        TextView tv = (TextView) mTabHost.getTabWidget().findViewById(android.R.id.title);
        tv.setTextColor(Color.parseColor("#FFFFFF"));
        tv.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.font_small));
        tv.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
        return mTabHost;
    }

    public void onResume() {
        super.onResume();
        if (!createdTab) {
            createdTab = true;
            mTabHost.setup(getActivity(), getChildFragmentManager(), R.id.fragment_container);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mTabHost = null;
    }
}
