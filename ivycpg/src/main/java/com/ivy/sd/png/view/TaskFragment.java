package com.ivy.sd.png.view;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.commons.IvyBaseFragment;
import com.ivy.sd.png.commons.SDUtil;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.provider.ConfigurationMasterHelper;
import com.ivy.sd.png.util.CommonDialog;
import com.ivy.sd.png.util.Commons;

import java.util.ArrayList;

/**
 * Created by ramkumard on 3/4/18
 */

public class TaskFragment extends IvyBaseFragment {

    private BusinessModel bmodel;
    private boolean bool;
    private boolean hide_new_menu = true;
    private boolean IsRetailerwisetask = false;
    private ViewPager viewPager;
    private boolean fromReviewScreen = false;
    private boolean fromHomeScreen = false;
    private Fragment mSelectedFragment;
    private ArrayList<Fragment> mFragmentList;
    private DrawerLayout mDrawerLayout;
    private View view;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        bmodel = (BusinessModel) getActivity().getApplicationContext();
        bmodel.setContext(getActivity());
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        view = inflater.inflate(R.layout.task_fragment, container, false);

        bmodel = (BusinessModel) getActivity().getApplicationContext();
        bmodel.setContext(getActivity());

        ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setDisplayShowTitleEnabled(false);
            actionBar.setDisplayShowHomeEnabled(true);
        }

        Bundle extras = getArguments();
        //Set Screen Title
        try {
            if (getArguments() == null || !getArguments().containsKey("screentitle")
                    || getArguments().getString("screentitle") == null)
                setScreenTitle(bmodel.getMenuName("MENU_TASK_NEW"));
            else
                setScreenTitle(getArguments().getString("screentitle"));
        } catch (Exception e) {

            setScreenTitle(getResources().
                    getString(R.string.task));
            Commons.printException(e);
        }
        mDrawerLayout = view.findViewById(R.id.drawer_layout);
        // set a custom shadow that overlays the main content when the drawer
        // opens
        mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow,
                GravityCompat.START);
        mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow,
                GravityCompat.END);
        mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
        ActionBarDrawerToggle mDrawerToggle = new ActionBarDrawerToggle(getActivity(), /* host Activity */
                mDrawerLayout, /* DrawerLayout object */
                R.string.ok, /* "open drawer" description for accessibility */
                R.string.close /* "close drawer" description for accessibility */
        ) {
            public void onDrawerClosed(View view) {
                //if (((AppCompatActivity) getActivity()).getSupportActionBar() != null)
                setScreenTitle(bmodel.mSelectedActivityName);
                getActivity().supportInvalidateOptionsMenu();
            }

            public void onDrawerOpened(View drawerView) {
                getActivity().supportInvalidateOptionsMenu();
            }
        };
        mDrawerLayout.addDrawerListener(mDrawerToggle);
        viewPager = view.findViewById(R.id.pager);
        final TabLayout tabLayout = view.findViewById(R.id.tabs);

        TabsPagerAdapter mAdapter = new TabsPagerAdapter(getActivity().getSupportFragmentManager());
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

             //   mSelectedFragment.onResume();

            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
        mAdapter.notifyDataSetChanged();

        tabLayout.removeAllTabs();

        String[] reason = getActivity().getResources().getStringArray(
                R.array.task_tab_header);

        if (bmodel.configurationMasterHelper.IS_SHOW_ONLY_SERVER_TASK) {
            reason = new String[1];
            reason[0] = "All";
        }

        int first_tab = 0;
        // Add tabs to Tablayout
        for (String tab_name : reason) {
            TextView tabOne = (TextView) LayoutInflater.from(getActivity()).inflate(R.layout.custom_tab, null);
            tabOne.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
            tabOne.setText(tab_name);
            tabOne.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.font_small));
            tabLayout.addTab(tabLayout.newTab().setCustomView(tabOne));
            if (first_tab == 0) {
                tabOne.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
            }
            first_tab++;
        }

        if (extras != null) {
            if (extras.containsKey("IsRetailerwisetask")) {
                IsRetailerwisetask = extras.getBoolean("IsRetailerwisetask");
            }
            if (extras.containsKey("fromReviewScreen")) {
                fromReviewScreen = extras.getBoolean("fromReviewScreen");
            }
            if (extras.containsKey("fromHomeScreen")) {
                fromHomeScreen = extras.getBoolean("fromHomeScreen");
            }
        }

        if (IsRetailerwisetask) {
            setScreenTitle("Task");
        } else if (extras != null){
            String screenTitle = extras.getString("screentitle");
            setScreenTitle(screenTitle);
        }

        if (bmodel.configurationMasterHelper.IS_SHOW_ONLY_SERVER_TASK) {
            addServerTaskFragments();
        } else {
            addTaskFragments();
        }

        if (!bmodel.configurationMasterHelper.IS_NEW_TASK) {
            hideNewTaskMenu();
        }

        tabLayout.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
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

        if (bmodel.configurationMasterHelper.MOVE_NEXT_ACTIVITY) {
            LinearLayout footer = view.findViewById(R.id.footer);
            footer.setVisibility(View.VISIBLE);

            Button btnClose = view.findViewById(R.id.btn_close);
            btnClose.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    new CommonDialog(getActivity().getApplicationContext(), getActivity(),
                            "", getResources().getString(R.string.move_next_activity),
                            false, getResources().getString(R.string.ok),
                            getResources().getString(R.string.cancel), new CommonDialog.positiveOnClickListener() {
                        @Override
                        public void onPositiveButtonClick() {
                            Intent intent = new Intent(getActivity(), HomeScreenTwo.class);

                            Bundle extras = getArguments();
                            if (extras != null) {
                                intent.putExtra("IsMoveNextActivity", true);
                                intent.putExtra("CurrentActivityCode", extras.getString("CurrentActivityCode", ""));
                            }

                            startActivity(intent);
                            getActivity().finish();
                        }
                    }, new CommonDialog.negativeOnClickListener() {
                        @Override
                        public void onNegativeButtonClick() {

                        }
                    }).show();

                }
            });
        }

        return view;
    }


    /**
     * This class used to contained all the fragments.
     */
    private class TabsPagerAdapter extends FragmentPagerAdapter {

        FragmentManager fragmentManager;

        public TabsPagerAdapter(FragmentManager fm) {
            super(fm);
            this.fragmentManager = fm;
        }

        @Override
        public Fragment getItem(int index) {

            return mFragmentList.get(index);
        }

        @Override
        public int getCount() {

            if (bmodel.configurationMasterHelper.IS_SHOW_ONLY_SERVER_TASK) {
                return 1;
            } else {
                return 3;
            }
        }

        @Override
        public int getItemPosition(Object object) {
            return POSITION_NONE;
        }
    }

    @Override
    public void onStart() {
        super.onStart();

        if (bmodel.userMasterHelper.getUserMasterBO().getUserid() == 0) {
            Toast.makeText(getActivity(),
                    getResources().getString(R.string.sessionout_loginagain),
                    Toast.LENGTH_SHORT).show();
            getActivity().finish();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        bool = false;
        bmodel = (BusinessModel) getActivity().getApplicationContext();
        bmodel.setContext(getActivity());

        if (bmodel.userMasterHelper.getUserMasterBO().getUserid() == 0) {
            Toast.makeText(getActivity(),
                    getResources().getString(R.string.sessionout_loginagain),
                    Toast.LENGTH_SHORT).show();
            getActivity().finish();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbindDrawables(view.findViewById(R.id.root));
    }

    /**
     * this would clear all the resources used of the layout.
     *
     * @param view
     */
    private void unbindDrawables(View view) {
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

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        // Inflate the menu; getActivity() adds items to the action bar if it is present.
        inflater.inflate(R.menu.menu_task, menu);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        // Funcnality Change, While clicking task in checkbox, save will happen
        boolean drawerOpen = false;
        if (mDrawerLayout != null)
            drawerOpen = mDrawerLayout.isDrawerOpen(GravityCompat.END);
        menu.findItem(R.id.menu_save).setVisible(false);
        if (!hide_new_menu)
            menu.findItem(R.id.menu_new_task).setVisible(false);
        menu.findItem(R.id.menu_reason).setVisible(bmodel.configurationMasterHelper.floating_np_reason_photo);
        if (drawerOpen)
            menu.clear();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int i1 = item.getItemId();
        if (i1 == android.R.id.home) {
            if (!bool) {
                // old code if (IsRetailerwisetask && !fromReviewScreen) {
                // Comment by Gp, Issue while going back from Activity Menu
                if (IsRetailerwisetask) {
                    bmodel.outletTimeStampHelper.updateTimeStampModuleWise(SDUtil.now(SDUtil.TIME));
                }
                bool = true;
                if (fromHomeScreen)
                    startActivity(new Intent(getActivity(), HomeScreenActivity.class));
            }
            getActivity().overridePendingTransition(R.anim.trans_right_in, R.anim.trans_right_out);
            getActivity().finish();
            return true;
        } else if (i1 == R.id.menu_new_task) {
            bmodel.outletTimeStampHelper.updateTimeStampModuleWise(SDUtil
                    .now(SDUtil.TIME));
            Intent i = new Intent(getActivity(), TaskCreation.class);
            i.putExtra("fromHomeScreen", fromHomeScreen);
            i.putExtra("IsRetailerwisetask", IsRetailerwisetask);
            startActivity(i);
            getActivity().finish();
            return true;
        } else if (i1 == R.id.menu_reason) {
            bmodel.reasonHelper.downloadNpReason(bmodel.retailerMasterBO.getRetailerID(), "MENU_TASK");
            ReasonPhotoDialog dialog = new ReasonPhotoDialog();
            dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialog) {
                    if (bmodel.reasonHelper.isNpReasonPhotoAvaiable(bmodel.retailerMasterBO.getRetailerID(), "MENU_TASK")) {
                        if (!fromHomeScreen) {
                            bmodel.saveModuleCompletion("MENU_TASK");
                            startActivity(new Intent(getActivity(),
                                    HomeScreenTwo.class));
                        }
                        getActivity().finish();
                    }
                }
            });
            Bundle args = new Bundle();
            args.putString("modulename", "MENU_TASK");
            dialog.setCancelable(false);
            dialog.setArguments(args);
            dialog.show(getActivity().getSupportFragmentManager(), "ReasonDialogFragment");
            return true;
        }
        return false;
    }

    public void hideNewTaskMenu() {
        hide_new_menu = false;
    }

    private void addTaskFragments() {

        mFragmentList = new ArrayList<>();
        mSelectedFragment = new TaskListFragment();
        Bundle args = new Bundle();
        args.putInt("type", 0);
        args.putBoolean("isRetailer", IsRetailerwisetask);
        args.putBoolean("fromReview", fromReviewScreen);
        args.putBoolean("fromProfileScreen", false);
        mSelectedFragment.setArguments(args);
        mFragmentList.add(mSelectedFragment);


        mSelectedFragment = new TaskListFragment();
        Bundle args1 = new Bundle();
        args1.putInt("type", 1);
        args1.putBoolean("isRetailer", IsRetailerwisetask);
        args1.putBoolean("fromReview", fromReviewScreen);
        args1.putBoolean("fromProfileScreen", false);
        mSelectedFragment.setArguments(args1);
        mFragmentList.add(mSelectedFragment);


        mSelectedFragment = new TaskListFragment();
        Bundle args2 = new Bundle();
        args2.putInt("type", 2);
        args2.putBoolean("isRetailer", IsRetailerwisetask);
        args2.putBoolean("fromReview", fromReviewScreen);
        args2.putBoolean("fromProfileScreen", false);
        mSelectedFragment.setArguments(args2);
        mFragmentList.add(mSelectedFragment);


    }

    private void addServerTaskFragments() {
        mFragmentList = new ArrayList<>();
        mSelectedFragment = new TaskListFragment();
        Bundle args1 = new Bundle();
        args1.putInt("type", 1);
        args1.putBoolean("isRetailer", IsRetailerwisetask);
        args1.putBoolean("fromReview", fromReviewScreen);
        args1.putBoolean("fromProfileScreen", false);
        mSelectedFragment.setArguments(args1);
        mFragmentList.add(mSelectedFragment);

    }
}
