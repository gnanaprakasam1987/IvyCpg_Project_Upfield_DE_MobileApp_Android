package com.ivy.cpg.view.collection;

import android.os.Bundle;
import com.google.android.material.tabs.TabLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;
import androidx.appcompat.widget.Toolbar;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;

import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.commons.IvyBaseActivityNoActionBar;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.model.UpdatePaymentByDateInterface;
import com.ivy.sd.png.util.Commons;
import com.ivy.sd.png.view.DataPickerDialogFragment;

import java.util.ArrayList;
import java.util.Date;

public class CollectionScreen extends IvyBaseActivityNoActionBar
        implements DataPickerDialogFragment.UpdateDateInterface,
        UpdatePaymentByDateInterface,
        ReceiptNoDialogFragment.UpdateReceiptNoInterface,
        PrintCountDialogFragment.PrintInterface {

    private Bundle instate;
    private ArrayList<Fragment> mFragmentList;
    private Fragment mSelectedFragment;
    private ViewPager viewPager;
    private BusinessModel bmodel;


    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.collection);

        getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        Toolbar toolbar =  findViewById(R.id.toolbar);
        if (toolbar != null) {
            setSupportActionBar(toolbar);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }

        bmodel = (BusinessModel) getApplicationContext();
        bmodel.setContext(this);
        overridePendingTransition(R.anim.trans_left_in, R.anim.trans_left_out);
        TabLayout tabLayout =  findViewById(R.id.tabs);
        addFragments();

        viewPager =  findViewById(R.id.pager);
        TabsPagerAdapter mAdapter = new TabsPagerAdapter(getSupportFragmentManager());
        viewPager.setAdapter(mAdapter);
        tabLayout.setupWithViewPager(viewPager);
        viewPager.setOffscreenPageLimit(1);
        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                mSelectedFragment = mFragmentList.get(position);
                mSelectedFragment.onResume();

            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
        mAdapter.notifyDataSetChanged();

        if (tabLayout != null)
            tabLayout.removeAllTabs();

        TabLayout.Tab billPaymentTab = tabLayout.newTab();
        if (bmodel.labelsMasterHelper.applyLabels("collection_title") != null)
            billPaymentTab.setText(bmodel.labelsMasterHelper.applyLabels("collection_title"));
        else
            billPaymentTab.setText(getResources().getString(R.string.bill_payment));
        tabLayout.addTab(billPaymentTab);

        if (bmodel.configurationMasterHelper.SHOW_ADVANCE_PAYMENT) {
            TabLayout.Tab advancePaymentTab = tabLayout.newTab();
            advancePaymentTab.setText(getResources().getString(R.string.advance_payment));
            tabLayout.addTab(advancePaymentTab);
        }

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewPager.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                viewPager.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    protected void passData(Bundle instate) {
        this.instate = instate;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindDrawables(findViewById(R.id.root));
    }

    /**
     * this would clear all the resources used of the layout.
     *
     * @param view parentView
     */
    private void unbindDrawables(View view) {
        if (view != null) {
            if (view.getBackground() != null) {
                view.getBackground().setCallback(null);
            }
            if (view instanceof ViewGroup) {
                for (int i = 0; i < ((ViewGroup) view).getChildCount(); i++) {
                    unbindDrawables(((ViewGroup) view).getChildAt(i));
                }
                try {
                    ((ViewGroup) view).removeAllViews();
                } catch (Exception e) {
                    Commons.printException(e);
                }
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_collection, menu);
        return true;
    }

    @Override
    public void updateDate(Date date, String tag) {
        AdvancePaymentDialogFragment paymentDialogFragment = (AdvancePaymentDialogFragment) mSelectedFragment;
        if (paymentDialogFragment != null) {
            paymentDialogFragment.updateDate(date,"" );
        }
    }

    @Override
    public void updatePaymentDetails(String date) {
    }

    @Override
    public void updateReceiptNo(String receiptno) {

    }

    @Override
    public void print(int printCount) {
        AdvancePaymentDialogFragment paymentDialogFragment = (AdvancePaymentDialogFragment) mSelectedFragment;
        if (paymentDialogFragment != null) {
            paymentDialogFragment.print(printCount);
        } else {
            CollectionFragmentNew collectionFragmentNew = (CollectionFragmentNew) mSelectedFragment;
            collectionFragmentNew.print(printCount);
        }
    }

    @Override
    public void dismiss() {
    }


    //Method used to add fragments for Tablayout
    private void addFragments() {
        mFragmentList = new ArrayList<>();
        Fragment mSelectFragment = new CollectionFragmentNew();

        Bundle bundle = new Bundle();
        bundle.putBoolean("IS_NO_COLL_REASON",getIntent().getBooleanExtra("IS_NO_COLL_REASON",false));
        mSelectFragment.setArguments(bundle);

        mFragmentList.add(mSelectFragment);

        if (bmodel.configurationMasterHelper.SHOW_ADVANCE_PAYMENT) {
            mSelectFragment = new AdvancePaymentDialogFragment();
            mFragmentList.add(mSelectFragment);
        }

    }

    /**
     * This class used to contained all the fragments.
     */
    private class TabsPagerAdapter extends FragmentPagerAdapter {

        FragmentManager fragmentManager;

        private TabsPagerAdapter(FragmentManager fm) {
            super(fm);
            this.fragmentManager = fm;
        }

        @Override
        public Fragment getItem(int index) {
            return mFragmentList.get(index);
        }

        @Override
        public int getCount() {
            return mFragmentList.size();
        }

        @Override
        public int getItemPosition(Object object) {
            return POSITION_NONE;
        }
    }
}
