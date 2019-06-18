package com.ivy.cpg.view.supervisor.mvp.sellerlistview;

import android.app.SearchManager;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.ivy.cpg.view.supervisor.mvp.FilterScreenFragment;
import com.ivy.cpg.view.supervisor.mvp.models.SellerBo;
import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.commons.IvyBaseActivityNoActionBar;
import com.ivy.sd.png.util.Commons;
import com.ivy.utils.FontUtils;
import com.ivy.utils.view.OnSingleClickListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class SellerListActivity extends IvyBaseActivityNoActionBar {

    private ViewPager viewPager;
    private TabLayout tabLayout;

    private ArrayList<SellerBo> sellersList = new ArrayList<>();
    private ArrayList<SellerBo> sellersInMarketList = new ArrayList<>();
    private ArrayList<SellerBo> sellersAbsentList = new ArrayList<>();
    private String selectedDate;
    private FrameLayout drawer;
    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mDrawerToggle;
    private BottomSheetBehavior bottomSheetBehavior;
    private RadioGroup sortRadioGroup;
    private PagerAdapter adapter;
    private View transparentView;

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
        selectedDate = getValue.getString("Date");

        for (SellerBo sellerBo : sellersList)
            if(sellerBo.isAttendanceDone())
                sellersInMarketList.add(sellerBo);
            else
                sellersAbsentList.add(sellerBo);

        viewPager = findViewById(R.id.viewPager);

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer = findViewById(R.id.right_drawer);
        int width = getResources().getDisplayMetrics().widthPixels;
        DrawerLayout.LayoutParams params = (android.support.v4.widget.DrawerLayout.LayoutParams) drawer.getLayoutParams();
        params.width = width;
        drawer.setLayoutParams(params);

        transparentView = findViewById(R.id.transparen_view);
        tabLayout = findViewById(R.id.tab_layout);
        tabLayout.setupWithViewPager(viewPager);//setting tab over viewpager

        adapter = new PagerAdapter
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

                if (bottomSheetBehavior.getState() == BottomSheetBehavior.STATE_EXPANDED){
                    bottomSheetBehavior.setHideable(true);
                    bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
                    transparentView.setVisibility(View.GONE);
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

        mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow,
                GravityCompat.START);
        mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow,
                GravityCompat.END);

        mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);

        mDrawerToggle = new ActionBarDrawerToggle(this, /* host Activity */
                mDrawerLayout, /* DrawerLayout object */
                R.string.ok, /* "open drawer" description for accessibility */
                R.string.close /* "close drawer" description for accessibility */
        ) {
            public void onDrawerClosed(View view) {
                setScreenTitle("");
                supportInvalidateOptionsMenu();
            }

            public void onDrawerOpened(View drawerView) {
                setScreenTitle(getResources().getString(R.string.filter));
                supportInvalidateOptionsMenu();
            }
        };
        mDrawerLayout.setDrawerListener(mDrawerToggle);

        sortRadioGroup = findViewById(R.id.sort_radio_group);

        bottomSheetBehavior = BottomSheetBehavior.from(findViewById(R.id.bottomSheetLayout));

        bottomSheetBehavior.setHideable(false);
        bottomSheetBehavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                switch (newState) {
                    case BottomSheetBehavior.STATE_COLLAPSED:
                        bottomSheetBehavior.setHideable(true);
                        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
                        break;
                    case BottomSheetBehavior.STATE_DRAGGING:
                        break;
                    case BottomSheetBehavior.STATE_EXPANDED:
                        break;
                    case BottomSheetBehavior.STATE_HIDDEN:
                        transparentView.setVisibility(View.GONE);
                        break;
                    case BottomSheetBehavior.STATE_SETTLING:
                        break;
                }
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {

            }
        });

        bottomSheetBehavior.setHideable(true);
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
        sortRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
                transparentView.setVisibility(View.GONE);

                int radioButtonID = sortRadioGroup.getCheckedRadioButtonId();
                View radioButton = sortRadioGroup.findViewById(radioButtonID);
                int idx = sortRadioGroup.indexOfChild(radioButton);

                sortList(idx,sellersList);
                sortList(idx,sellersInMarketList);
                sortList(idx,sellersAbsentList);
            }
        });

        transparentView.setOnClickListener(new OnSingleClickListener() {
            @Override
            public void onSingleClick(View v) {
                if (bottomSheetBehavior.getState() == BottomSheetBehavior.STATE_EXPANDED){
                    bottomSheetBehavior.setHideable(true);
                    bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
                    transparentView.setVisibility(View.GONE);
                }
            }
        });
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
    protected void onStart() {
        super.onStart();
        mDrawerLayout.closeDrawer(GravityCompat.END);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        menu.findItem(R.id.menu_dashboard).setVisible(false);
        menu.findItem(R.id.menu_date).setVisible(false);
        menu.findItem(R.id.menu_sort).setVisible(true);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int i = item.getItemId();
        if (i == android.R.id.home) {
            if (mDrawerLayout.isDrawerOpen(GravityCompat.END)) {
                mDrawerLayout.closeDrawers();
            } else {
                finish();
                overridePendingTransition(R.anim.trans_right_in, R.anim.trans_right_out);
            }

            return true;
        }else if(i == R.id.menu_sort){
            if (bottomSheetBehavior.getState() == BottomSheetBehavior.STATE_EXPANDED){
                bottomSheetBehavior.setHideable(true);
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
                transparentView.setVisibility(View.GONE);
            }
            else {
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                transparentView.setVisibility(View.VISIBLE);
            }
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
            return TabViewListFragment.getInstance(position, prepareListValues(position), position == 0,selectedDate);
        }

        @Override
        public int getCount() {
            return mNumOfTabs;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return title[position];
        }

        @Override
        public int getItemPosition(Object object) { return POSITION_NONE; }
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
                    ((TextView) tabViewChild).setTypeface(FontUtils.getFontRoboto(this, FontUtils.FontType.MEDIUM));
                }
            }
        }
    }

    public void filter(){
        filterFragment();
    }

    public void sort(){

    }

    private void filterFragment() {
        try {

            mDrawerLayout.openDrawer(GravityCompat.END);

            android.support.v4.app.FragmentManager fm = getSupportFragmentManager();
            FilterScreenFragment frag = (FilterScreenFragment) fm
                    .findFragmentByTag("FilterScreen");
            android.support.v4.app.FragmentTransaction ft = fm
                    .beginTransaction();
            if (frag != null)
                ft.detach(frag);

            FilterScreenFragment fragobj = new FilterScreenFragment();

            ft.replace(R.id.right_drawer, fragobj, "FilterScreen");
            ft.commit();
        } catch (Exception e) {
            Commons.printException(e);
        }
    }

    public void sortList(int sortBy,ArrayList<SellerBo> sellerBos){

        System.out.println("sortBy = " + sortBy);

        if(sortBy == 0) {
            Collections.sort(sellerBos, new Comparator<SellerBo>() {
                @Override
                public int compare(SellerBo fstr, SellerBo sstr) {
                    return fstr.getUserName().compareTo(sstr.getUserName());

                }
            });
        }else if(sortBy == 1){
            Collections.sort(sellerBos, new Comparator<SellerBo>() {
                @Override
                public int compare(SellerBo fstr, SellerBo sstr) {
                    return sstr.getUserName().compareTo(fstr.getUserName());
                }
            });
        }else if(sortBy == 2){
            Collections.sort(sellerBos, new Comparator<SellerBo>() {
                @Override
                public int compare(SellerBo fstr, SellerBo sstr) {

                    int target1 = fstr.getTarget();
                    int billed1 = fstr.getBilled();
                    int sellerProductive1 = 0;
                    if (target1 != 0) {
                        sellerProductive1 = (int)((float)billed1 / (float)target1 * 100);
                    }
                    fstr.setProductivityPercent(sellerProductive1);

                    int target2 = sstr.getTarget();
                    int billed2 = sstr.getBilled();
                    int sellerProductive2 = 0;

                    if (target2 != 0) {
                        sellerProductive2 = (int)((float)billed2 / (float)target2 * 100);
                    }

                    sstr.setProductivityPercent(sellerProductive2);

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                        return Integer.compare(sstr.getProductivityPercent(),fstr.getProductivityPercent());
                    }else
                        return Integer.valueOf(sstr.getProductivityPercent()).compareTo(fstr.getProductivityPercent());

                }
            });
        }else if(sortBy == 3){
            Collections.sort(sellerBos, new Comparator<SellerBo>() {
                @Override
                public int compare(SellerBo fstr, SellerBo sstr) {

                    int target1 = fstr.getTarget();
                    int billed1 = fstr.getBilled();
                    int sellerProductive1 = 0;
                    if (target1 != 0) {
                        sellerProductive1 = (int)((float)billed1 / (float)target1 * 100);
                    }
                    fstr.setProductivityPercent(sellerProductive1);

                    int target2 = sstr.getTarget();
                    int billed2 = sstr.getBilled();
                    int sellerProductive2 = 0;

                    if (target2 != 0) {
                        sellerProductive2 = (int)((float)billed2 / (float)target2 * 100);
                    }

                    sstr.setProductivityPercent(sellerProductive2);

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                        return Integer.compare(fstr.getProductivityPercent(),sstr.getProductivityPercent());
                    }else
                        return Integer.valueOf(fstr.getProductivityPercent()).compareTo(sstr.getProductivityPercent());

                }
            });
        }

        adapter.notifyDataSetChanged();
    }

}
