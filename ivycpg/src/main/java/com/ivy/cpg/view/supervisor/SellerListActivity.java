package com.ivy.cpg.view.supervisor;

import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.commons.IvyBaseActivityNoActionBar;
import com.ivy.sd.png.util.Commons;

import java.util.ArrayList;
import java.util.HashMap;

public class SellerListActivity extends IvyBaseActivityNoActionBar {

    private ViewPager viewPager;
    private ArrayList<DetailsBo> detailsBos = new ArrayList<>();
    private HashMap<Integer,Integer> integerHashMap = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_seller_list);

        overridePendingTransition(R.anim.trans_left_in, R.anim.trans_left_out);

        Toolbar toolbar = findViewById(R.id.toolbar);

        if (toolbar != null) {
            setSupportActionBar(toolbar);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        integerHashMap.put(0,0);
        integerHashMap.put(1,0);
        integerHashMap.put(2,0);

        setScreenTitle("Total Sellers");

        viewPager = findViewById(R.id.viewPager);

        TabLayout tabLayout = findViewById(R.id.tab_layout);
        tabLayout.setupWithViewPager(viewPager);//setting tab over viewpager

        detailsBos.addAll(SupervisorActivityHelper.getInstance().getDetailsBoHashMap().values());

        PagerAdapter adapter = new PagerAdapter
                (getSupportFragmentManager(), tabLayout.getTabCount(), detailsBos);

        viewPager.setOffscreenPageLimit(1);
        viewPager.setAdapter(adapter);
        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewPager.setCurrentItem(tab.getPosition());

                switch (tab.getPosition()) {
                    case 0:
                        setScreenTitle("Total Sellers (" + integerHashMap.get(tab.getPosition()) + ")");
                        break;
                    case 1:
                        setScreenTitle("Absent Sellers (" + integerHashMap.get(tab.getPosition()) + ")");
                        break;
                    case 2:
                        setScreenTitle("InMarket Sellers (" + integerHashMap.get(tab.getPosition()) + ")");
                        break;
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }
        });

        try {
            int pos = getIntent().getExtras()!=null?getIntent().getExtras().getInt("TabPos"):0;
            viewPager.setCurrentItem(pos);
        } catch (Exception e) {
            Commons.printException(e);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int i = item.getItemId();
        if (i == android.R.id.home) {
            finish();
            overridePendingTransition(R.anim.trans_right_in, R.anim.trans_right_out);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public class PagerAdapter extends FragmentStatePagerAdapter {
        int mNumOfTabs;
        ArrayList<DetailsBo> detailsBos;
        String[] title = {"Total Seller", "Absent Seller", "InMarket Seller", "Seller"};

        PagerAdapter(FragmentManager fm, int NumOfTabs, ArrayList<DetailsBo> detailsBos) {
            super(fm);
            this.mNumOfTabs = NumOfTabs;
            this.detailsBos = detailsBos;
        }

        @Override
        public Fragment getItem(int position) {
            return TabViewListFragment.getInstance(position, prepareListValues(position),position == 0?true:false);
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

    private ArrayList<DetailsBo> prepareListValues(int position) {

        ArrayList<DetailsBo> detailsBos = new ArrayList<>();
        switch (position) {
            case 0:
                detailsBos = new ArrayList<>(SupervisorActivityHelper.getInstance().getDetailsBoHashMap().values());
                break;
            case 1:
                ArrayList<DetailsBo> detailsBosTemp = new ArrayList<>();
                for (DetailsBo detailsBo : SupervisorActivityHelper.getInstance().getDetailsBoHashMap().values()) {
                    if (detailsBo.getStatus() != null && detailsBo.getStatus().equalsIgnoreCase("Absent"))
                        detailsBosTemp.add(detailsBo);
                }
                detailsBos = detailsBosTemp;
                break;
            case 2:
                ArrayList<DetailsBo> detailsBosMarketTemp = new ArrayList<>();
                for (DetailsBo detailsBo : SupervisorActivityHelper.getInstance().getDetailsBoHashMap().values()) {
                    if (detailsBo.getStatus() != null && detailsBo.getStatus().equalsIgnoreCase("In Market"))
                        detailsBosMarketTemp.add(detailsBo);
                }
                detailsBos = detailsBosMarketTemp;
                break;
        }

        this.detailsBos = detailsBos;

        integerHashMap.put(position,detailsBos.size());

        return detailsBos;
    }
}
