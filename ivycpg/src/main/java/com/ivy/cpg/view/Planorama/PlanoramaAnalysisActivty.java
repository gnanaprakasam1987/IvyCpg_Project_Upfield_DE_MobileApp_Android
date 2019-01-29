package com.ivy.cpg.view.Planorama;

import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toolbar;

import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.commons.IvyBaseActivityNoActionBar;
import com.ivy.sd.png.util.Commons;

import static android.content.res.Configuration.SCREENLAYOUT_SIZE_LARGE;

public class PlanoramaAnalysisActivty extends IvyBaseActivityNoActionBar {

    private TabLayout tabLayout;
    private ViewPager viewPager;
    private boolean is7InchTablet;

    private String product_tab_title,pack_tab_title,sos_tab_title;
    android.support.v7.widget.Toolbar toolbar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        setContentView(R.layout.activity_planorama_analysis);

        toolbar = findViewById(R.id.toolbar);
        if (toolbar != null) {

            setSupportActionBar(toolbar);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            setScreenTitle("Analysis Result");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        tabLayout = findViewById(R.id.tab_layout);
        viewPager = findViewById(R.id.pager);
        is7InchTablet = this.getResources().getConfiguration().isLayoutSizeAtLeast(SCREENLAYOUT_SIZE_LARGE);

        product_tab_title=getResources().getString(R.string.products);
        pack_tab_title="Pack";
        sos_tab_title="SOS";

        addTabLayout();
    }

    /**
     * Method used to add Tabs based on configurations.
     */
    private void addTabLayout() {

        try {

            tabLayout.addTab(tabLayout.newTab().setText(product_tab_title));
            //tabLayout.addTab(tabLayout.newTab().setText(pack_tab_title));
            tabLayout.addTab(tabLayout.newTab().setText(sos_tab_title));

        } catch (Exception ex) {
            Commons.printException("Error while setting label for Profile Tab", ex);
        }


        View root = tabLayout.getChildAt(0);
        if (root instanceof LinearLayout) {
            ((LinearLayout) root).setShowDividers(LinearLayout.SHOW_DIVIDER_MIDDLE);
            GradientDrawable drawable = new GradientDrawable();
            drawable.setColor(Color.parseColor("#80000000"));
            drawable.setSize(1, 1);
            ((LinearLayout) root).setDividerPadding(0);
            ((LinearLayout) root).setDividerDrawable(drawable);
        }

        tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);

        if (!is7InchTablet && tabLayout.getTabCount() > 3) {
            tabLayout.setTabMode(TabLayout.MODE_SCROLLABLE);
        }

        final ViewPagerAdapter adapter = new ViewPagerAdapter
                (this.getSupportFragmentManager(), tabLayout.getTabCount());
        viewPager.setAdapter(adapter);
        viewPager.setOffscreenPageLimit(tabLayout.getTabCount());

        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if (tab.getPosition() == 0) {

                } else {
                }
                viewPager.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }
        });

    }

    /**
     * ViewPagerAdapter class used to call fragment dynamically.
     */
    public class ViewPagerAdapter extends FragmentPagerAdapter {

        private final int mNumOfTabs;

        ViewPagerAdapter(FragmentManager fm, int NumOfTabs) {
            super(fm);
            this.mNumOfTabs = NumOfTabs;
        }

        @Override
        public Fragment getItem(int position) {

            String tabName = tabLayout.getTabAt(position).getText().toString();
            if (tabName.equals(product_tab_title)) {
                PlanoramaProductFragment productFragment=new PlanoramaProductFragment();
                return productFragment;

            } else if (tabName.equals(sos_tab_title)) {

                PlanoramaSOSFragment sosFragment=new PlanoramaSOSFragment();
                return sosFragment;
            }
            return null;
        }

        @Override
        public int getCount() {
            return mNumOfTabs;
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int i = item.getItemId();
        if (i == android.R.id.home) {

            finish();
            overridePendingTransition(R.anim.trans_right_in, R.anim.trans_right_out);
        }
        return super.onOptionsItemSelected(item);
    }

}
