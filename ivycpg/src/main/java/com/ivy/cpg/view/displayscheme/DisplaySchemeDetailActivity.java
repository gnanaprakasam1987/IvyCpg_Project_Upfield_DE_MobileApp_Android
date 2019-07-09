package com.ivy.cpg.view.displayscheme;

import android.os.Bundle;
import com.google.android.material.tabs.TabLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;
import androidx.appcompat.widget.Toolbar;
import android.view.MenuItem;

import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.commons.IvyBaseActivityNoActionBar;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.util.Commons;

import static android.content.res.Configuration.SCREENLAYOUT_SIZE_LARGE;
import static com.ivy.sd.png.asean.view.R.id.tab_layout;

/**
 * Created by Rajkumar on 2/1/18.
 * Display scheme detail screen
 */

public class DisplaySchemeDetailActivity extends IvyBaseActivityNoActionBar {

    BusinessModel businessModel;
    Toolbar toolbar;
    String mSelectedSchemeId;
    private TabLayout tabLayout;
    private ViewPager viewPager;
    boolean is7InchTablet;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {
            setContentView(R.layout.activity_display_scheme_detail);
            overridePendingTransition(R.anim.trans_left_in, R.anim.trans_left_out);

            businessModel = (BusinessModel) getApplicationContext();
            businessModel.setContext(this);

            toolbar = findViewById(R.id.toolbar);
            setSupportActionBar(toolbar);
            if (getSupportActionBar() != null) {
                getSupportActionBar().setDisplayShowTitleEnabled(false);
                getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                getSupportActionBar().setDisplayShowHomeEnabled(true);
            }

            Bundle extras = getIntent().getExtras();
            if (extras != null) {
                String title = extras.getString("schemeName") == null ? "" : extras.getString("schemeName");
                setScreenTitle(title);

                mSelectedSchemeId = extras.getString("schemeId");
            }

            tabLayout =  findViewById(tab_layout);
            viewPager =  findViewById(R.id.pager);

            tabLayout.addTab(tabLayout.newTab()
                    .setText("Info"));
            tabLayout.addTab(tabLayout.newTab()
                    .setText("Slab"));

            tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);
            is7InchTablet = this.getResources().getConfiguration()
                    .isLayoutSizeAtLeast(SCREENLAYOUT_SIZE_LARGE);
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
                    viewPager.setCurrentItem(tab.getPosition());
                }

                @Override
                public void onTabUnselected(TabLayout.Tab tab) {
                }

                @Override
                public void onTabReselected(TabLayout.Tab tab) {
                }
            });

        } catch (Exception ex) {
            Commons.printException(ex);
        }
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

            if (tabLayout.getTabAt(position) != null && tabLayout.getTabAt(position).getText() != null) {
                String tabName = tabLayout.getTabAt(position).getText().toString();
                if (tabName.equalsIgnoreCase("Info")) {
                    DisplaySchemeInfoFragment fragment = new DisplaySchemeInfoFragment();
                    Bundle bundle = new Bundle();
                    bundle.putString("schemeId", mSelectedSchemeId);
                    fragment.setArguments(bundle);
                    return fragment;
                } else if (tabName.equals("Slab")) {
                    return new DisplaySchemeSlabFragment();
                }
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
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
