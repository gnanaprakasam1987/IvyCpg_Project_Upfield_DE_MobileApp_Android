package com.ivy.cpg.view.supervisor.mvp.sellermapview;

import android.app.SearchManager;
import android.content.Context;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.ivy.cpg.view.supervisor.helper.DetailsBo;
import com.ivy.cpg.view.supervisor.helper.SupervisorActivityHelper;
import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.commons.IvyBaseActivityNoActionBar;
import com.ivy.sd.png.util.Commons;
import com.ivy.utils.FontUtils;

import java.util.ArrayList;
import java.util.HashMap;

public class SellerListActivity extends IvyBaseActivityNoActionBar {

    private ViewPager viewPager;
    private ArrayList<DetailsBo> detailsBos = new ArrayList<>();
    private HashMap<Integer,Integer> integerHashMap = new HashMap<>();
    private TabLayout tabLayout;

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

        tabLayout = findViewById(R.id.tab_layout);
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

        changeTabsFont(tabLayout);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_supervisor_screen, menu);

        SearchManager searchManager = (SearchManager) getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView = (SearchView) menu.findItem(R.id.menu_search).getActionView();
        ImageView searchClose = searchView.findViewById(android.support.v7.appcompat.R.id.search_close_btn);
        searchClose.setImageResource(android.R.drawable.ic_menu_close_clear_cancel);
        searchView.setSearchableInfo(searchManager != null ? searchManager.getSearchableInfo(getComponentName()) : null);
        SearchView.OnQueryTextListener textChangeListener = new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextChange(String newText) {
                displaySearchItem(newText);
                return true;
            }

            @Override
            public boolean onQueryTextSubmit(String query) {
                return true;
            }
        };
        searchView.setOnQueryTextListener(textChangeListener);

        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        menu.findItem(R.id.menu_dashboard).setVisible(false);
        menu.findItem(R.id.menu_date).setVisible(false);
        return true;
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

    private void displaySearchItem(String searchText){
        int pos = tabLayout.getSelectedTabPosition();
        ArrayList<DetailsBo> detailsBos = prepareListValues(pos);

        for(int i = 0;i<detailsBos.size();i++){
            if (searchText != null) {
                this.detailsBos.clear();
                if (detailsBos.get(i).getUserName().toLowerCase()
                        .contains(searchText.toLowerCase()) ){
                    this.detailsBos.add(detailsBos.get(i));
                }
            }
        }
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
            return TabViewListFragment.getInstance(position, prepareListValues(position), position == 0);
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

    private void changeTabsFont(TabLayout tabLayout) {

        ViewGroup vg = (ViewGroup) tabLayout.getChildAt(0);
        int tabsCount = vg.getChildCount();
        for (int j = 0; j < tabsCount; j++) {
            ViewGroup vgTab = (ViewGroup) vg.getChildAt(j);
            int tabChildCount = vgTab.getChildCount();
            for (int i = 0; i < tabChildCount; i++) {
                View tabViewChild = vgTab.getChildAt(i);
                if (tabViewChild instanceof TextView) {
                    ((TextView) tabViewChild).setTypeface(FontUtils.getFontRoboto(FontUtils.FontType.MEDIUM,this));
                }
            }
        }
    }

}
