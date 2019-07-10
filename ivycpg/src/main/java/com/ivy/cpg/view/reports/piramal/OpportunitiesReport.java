package com.ivy.cpg.view.reports.piramal;

import android.content.res.TypedArray;
import android.graphics.Color;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTabHost;
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
 * Created by maheswaran.m on 08-10-2015.
 */
public class OpportunitiesReport extends Fragment {
    private FragmentTabHost mTabHost;
    private boolean createdTab = false;
    private BusinessModel bmodel;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {


        bmodel = (BusinessModel) getActivity().getApplicationContext();
        bmodel.setContext(getActivity());

        mTabHost = new FragmentTabHost(getActivity());
        mTabHost.setup(getActivity(), getChildFragmentManager(), R.id.fragment_content);
        mTabHost.setForegroundGravity(Gravity.CENTER);
        mTabHost.addTab(mTabHost.newTabSpec("Top 10").setIndicator("Top 10"),
                TopTenRetailers.class, null);
        mTabHost.addTab(mTabHost.newTabSpec("Remaining").setIndicator("Opportunity"),
                TopOpportunities.class, null);

        TypedArray typearr = getActivity().getTheme().obtainStyledAttributes(R.styleable.MyTextView);
        for (int i = 0; i < mTabHost.getTabWidget().getChildCount(); i++) {
            //Unselected Tabs
            mTabHost.getTabWidget().getChildAt(i).setBackgroundColor(typearr.getColor(R.styleable.MyTextView_tablayout, 0));
            TextView tv = (TextView) mTabHost.getTabWidget().getChildAt(i).findViewById(android.R.id.title);
            tv.setTextColor(Color.parseColor("#000000"));
            tv.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.font_small));
            tv.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
        }

        //Unselected Tabs
        mTabHost.getTabWidget().getChildAt(mTabHost.getCurrentTab()).setBackgroundColor(typearr.getColor(R.styleable.MyTextView_tablayout, 0));
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
            mTabHost.setup(getActivity(), getChildFragmentManager(), R.id.fragment_content);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mTabHost = null;
    }

}
