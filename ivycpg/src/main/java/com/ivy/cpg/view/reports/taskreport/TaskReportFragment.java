package com.ivy.cpg.view.reports.taskreport;

import android.os.Bundle;
import com.google.android.material.tabs.TabLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.viewpager.widget.ViewPager;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.commons.IvyBaseFragment;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.provider.ConfigurationMasterHelper;
import com.ivy.sd.png.util.Commons;

public class TaskReportFragment extends IvyBaseFragment {


    private BusinessModel bmodel;
    private ViewPager viewPager;
    private TabLayout tabLayout;
    private String[] title = {"Outlet","Seller"};

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        bmodel = (BusinessModel) getActivity().getApplicationContext();
        bmodel.setContext(getActivity());

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_task_report_new, container, false);
        if (bmodel.userMasterHelper.getUserMasterBO().getUserid() == 0) {
            Toast.makeText(getActivity(),
                    getResources().getString(R.string.sessionout_loginagain),
                    Toast.LENGTH_SHORT).show();
            getActivity().finish();
        }

        viewPager = view.findViewById(R.id.viewPager);
        tabLayout = view.findViewById(R.id.tab_layout);

        //If Seller wise report is not configured then tab will be removed
        if (!bmodel.configurationMasterHelper.IS_SELLER_TASK_RPT)
            tabLayout.removeTabAt(1);

        tabLayout.setupWithViewPager(viewPager);//setting tab over viewpager

        //Tab name from Label master
        try{
            title[0] = bmodel.labelsMasterHelper.applyLabels("Outlet_Task_Tag")!=null?bmodel.labelsMasterHelper.applyLabels("Outlet_Task_Tag"):"Outlet";
            title[1] = bmodel.labelsMasterHelper.applyLabels("Seller_Task_Tag")!=null?bmodel.labelsMasterHelper.applyLabels("Seller_Task_Tag"):"Seller";
        }catch(Exception e){
            Commons.printException(e);
        }

        PagerAdapter adapter = new PagerAdapter
                (getChildFragmentManager(), tabLayout.getTabCount());

        viewPager.setOffscreenPageLimit(1);
        viewPager.setAdapter(adapter);
        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewPager.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }
        });

        changeTabsFont();

        return view;

    }

    private void changeTabsFont() {
        ViewGroup vg = (ViewGroup) tabLayout.getChildAt(0);
        int tabsCount = vg.getChildCount();
        for (int j = 0; j < tabsCount; j++) {
            ViewGroup vgTab = (ViewGroup) vg.getChildAt(j);
            int tabChildsCount = vgTab.getChildCount();
            for (int i = 0; i < tabChildsCount; i++) {
                View tabViewChild = vgTab.getChildAt(i);
                if (tabViewChild instanceof TextView) {
                    ((TextView) tabViewChild).setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
                    if (!bmodel.configurationMasterHelper.IS_SELLER_TASK_RPT) {
                        Display display = getActivity().getWindowManager().getDefaultDisplay();
                        ((TextView) tabViewChild).setWidth(display.getWidth());
                    }
                }
            }
        }
    }

    public class PagerAdapter extends FragmentStatePagerAdapter {
        int mNumOfTabs;

        PagerAdapter(FragmentManager fm, int NumOfTabs) {
            super(fm);
            this.mNumOfTabs = NumOfTabs;
        }

        @Override
        public Fragment getItem(int position) {
            if(position == 0)
                return new OutletTaskReportFragment();
            else
                return new SellerTaskReportFragment();

        }

        @Override
        public int getCount() {
            return mNumOfTabs;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return title[position];
        }
    }

    @Override
    public void onStart() {
        super.onStart();
    }

}
