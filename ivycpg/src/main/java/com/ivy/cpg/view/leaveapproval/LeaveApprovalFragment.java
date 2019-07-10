package com.ivy.cpg.view.leaveapproval;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import com.google.android.material.tabs.TabLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.viewpager.widget.ViewPager;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.TextView;

import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.commons.IvyBaseFragment;
import com.ivy.sd.png.util.Commons;
import com.ivy.cpg.view.homescreen.HomeScreenActivity;
import com.ivy.utils.FontUtils;
import com.ivy.utils.rx.AppSchedulerProvider;

import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Consumer;

public class LeaveApprovalFragment extends IvyBaseFragment {
    private View view;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        view = inflater.inflate(R.layout.leave_approval_fragment, container, false);

        initializeItem();
        return view;
    }

    private void initializeItem() {
        ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();


        if (actionBar != null) {
            actionBar.setDisplayShowTitleEnabled(false);
            actionBar.setIcon(null);
            actionBar.setElevation(0);
            //  actionBar.setStackedBackgroundDrawable((new ColorDrawable(ContextCompat.getColor(getActivity(),R.color.toolbar_ret_bg))));
        }

        setScreenTitle("Leave Approval");

        TabLayout tabLayout = view.findViewById(R.id.tab_layout);
        tabLayout.addTab(tabLayout.newTab().setText(getResources().getString(R.string.tab_text_pending)));
        tabLayout.addTab(tabLayout.newTab().setText(getResources().getString(R.string.tab_text_approve)));
        tabLayout.addTab(tabLayout.newTab().setText(getResources().getString(R.string.reject)));
        tabLayout.addTab(tabLayout.newTab().setText(getResources().getString(R.string.tab_text_history)));
        tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);

        ViewGroup vg = (ViewGroup) tabLayout.getChildAt(0);
        int tabsCount = vg.getChildCount();
        for (int j = 0; j < tabsCount; j++) {
            ViewGroup vgTab = (ViewGroup) vg.getChildAt(j);
            int tabChildsCount = vgTab.getChildCount();
            for (int i = 0; i < tabChildsCount; i++) {
                View tabViewChild = vgTab.getChildAt(i);
                if (tabViewChild instanceof TextView) {
                    ((TextView) tabViewChild).setTypeface(FontUtils.getFontRoboto(getActivity(), FontUtils.FontType.MEDIUM));
                }
            }
        }

        AppSchedulerProvider appSchedulerProvider = new AppSchedulerProvider();
        new CompositeDisposable().add(LeaveApprovalHelper.getInstance(getActivity()).updateLeaves()
                .subscribeOn(appSchedulerProvider.io())
                .observeOn(appSchedulerProvider.ui())
                .subscribe(new Consumer<Boolean>() {
                    @Override
                    public void accept(Boolean aBoolean) {

                    }
                }));


        final ViewPager viewPager = view.findViewById(R.id.pager);
        final PagerAdapter adapter = new PagerAdapter
                (getActivity().getSupportFragmentManager(), tabLayout.getTabCount());
        adapter.notifyDataSetChanged();
        viewPager.setAdapter(adapter);
        viewPager.setOffscreenPageLimit(0);
        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));


        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewPager.setCurrentItem(tab.getPosition());
                viewPager.getAdapter().notifyDataSetChanged();
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

    }

    public class PagerAdapter extends FragmentStatePagerAdapter {
        int mNumOfTabs;

        private PagerAdapter(FragmentManager fm, int NumOfTabs) {
            super(fm);
            this.mNumOfTabs = NumOfTabs;
        }

        @Override
        public Fragment getItem(int position) {

            switch (position) {
                case 0:
                    return new PendingLeavesFragment();
                case 1:
                    return new ApprovedLeavesFragment();
                case 2:
                    return new RejectedLeavesFragment();
                case 3:
                    return new HistoryLeavesFragment();
                default:
                    return null;
            }
        }

        @Override
        public int getItemPosition(Object object) {
            return POSITION_NONE;
        }

        @Override
        public int getCount() {
            return mNumOfTabs;
        }

    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_leave_approval, menu);
        menu.findItem(R.id.menu_history).setVisible(false);
        menu.findItem(R.id.menu_save).setVisible(false);
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            case android.R.id.home:
                startActivity(new Intent(getActivity(), HomeScreenActivity.class));
                getActivity().finish();

                break;
        }
        return true;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbindDrawables(view.findViewById(R.id.root));
    }

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
                    if (!(view instanceof AdapterView<?>))
                        ((ViewGroup) view).removeAllViews();
                } catch (Exception e) {
                    Commons.printException(e);
                }
            }
        }
    }
}
