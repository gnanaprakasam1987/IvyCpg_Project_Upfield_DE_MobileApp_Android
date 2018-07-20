package com.ivy.cpg.view.supervisor.mvp.sellerlistview;

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

import com.ivy.cpg.view.supervisor.mvp.SellerBo;
import com.ivy.cpg.view.supervisor.mvp.sellerhomescreen.SellerMapHomePresenter;
import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.commons.IvyBaseActivityNoActionBar;
import com.ivy.sd.png.util.Commons;
import com.ivy.utils.FontUtils;

import java.util.ArrayList;

public class SellerListActivity extends IvyBaseActivityNoActionBar {

    private ViewPager viewPager;
    private TabLayout tabLayout;

    private ArrayList<SellerBo> sellersList = new ArrayList<>();
    private ArrayList<SellerBo> sellersInMarketList = new ArrayList<>();
    private ArrayList<SellerBo> sellersAbsentList = new ArrayList<>();

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

        Bundle getValue = getIntent().getBundleExtra("SellerInfo");
        sellersList = getValue.getParcelableArrayList("SellerList");

        for (SellerBo sellerBo : sellersList)
            if(sellerBo.isAttendanceDone())
                sellersInMarketList.add(sellerBo);
            else
                sellersAbsentList.add(sellerBo);

        viewPager = findViewById(R.id.viewPager);

        tabLayout = findViewById(R.id.tab_layout);
        tabLayout.setupWithViewPager(viewPager);//setting tab over viewpager

        PagerAdapter adapter = new PagerAdapter
                (getSupportFragmentManager(), tabLayout.getTabCount());

        viewPager.setOffscreenPageLimit(1);
        viewPager.setAdapter(adapter);
        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewPager.setCurrentItem(tab.getPosition());

                switch (tab.getPosition()) {
                    case 0:
                        setScreenTitle("Total Sellers (" + sellersList.size() + ")");
                        break;
                    case 1:
                        setScreenTitle("InMarket Sellers (" + sellersInMarketList.size() + ")");
                        break;
                    case 2:
                        setScreenTitle("Absent Sellers (" + sellersAbsentList.size() + ")");
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

        setScreenTitle("Total Sellers (" + sellersList.size() + ")");

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
//                displaySearchItem(newText);
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
        ArrayList<SellerBo> detailsBos = prepareListValues(tabLayout.getSelectedTabPosition());

        for(int i = 0;i<detailsBos.size();i++){
            if (detailsBos.get(i).getUserName().toLowerCase()
                    .contains(searchText.toLowerCase()) ){
                detailsBos.add(detailsBos.get(i));
            }
        }
    }

    public class PagerAdapter extends FragmentStatePagerAdapter {
        int mNumOfTabs;
        String[] title = {"Total Seller", "InMarket Seller", "Absent Seller", "Seller"};

        PagerAdapter(FragmentManager fm, int NumOfTabs) {
            super(fm);
            this.mNumOfTabs = NumOfTabs;
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

    private ArrayList<SellerBo> prepareListValues(int position) {

        ArrayList<SellerBo> detailsBos = new ArrayList<>();
        switch (position) {
            case 0:
                detailsBos = sellersList;
                break;
            case 1:
                detailsBos = sellersInMarketList;
                break;
            case 2:
                detailsBos = sellersAbsentList;
                break;
        }
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

    public void filter(){

    }

    public void sort(){

    }

}
