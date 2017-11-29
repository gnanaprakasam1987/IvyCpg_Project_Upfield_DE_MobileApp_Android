package com.ivy.sd.png.view;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.commons.IvyBaseActivityNoActionBar;
import com.ivy.sd.png.commons.SDUtil;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.provider.ConfigurationMasterHelper;
import com.ivy.sd.png.util.CommonDialog;
import com.ivy.sd.png.util.Commons;

import java.util.ArrayList;

public class Task extends IvyBaseActivityNoActionBar implements OnClickListener {

    private BusinessModel bmodel;
    private boolean bool;
    private boolean hide_new_menu = true;
    private boolean IsRetailerwisetask = false;
    private ViewPager viewPager;
    private boolean fromReviewScreen = false;
    private boolean fromHomeScreen = false;
    private Fragment mSelectedFragment;
    private String mSelectedRetailerID;
    private ArrayList<Fragment> mFragmentList;
    private String screenTitle = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_task);

        viewPager = (ViewPager) findViewById(R.id.pager);
        final TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        Button newtask = (Button) findViewById(R.id.newtask);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);

        newtask.setOnClickListener(this);

        bmodel = (BusinessModel) getApplicationContext();
        bmodel.setContext(this);
        overridePendingTransition(R.anim.trans_left_in, R.anim.trans_left_out);
        Bundle extras = getIntent().getExtras();
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(null);
        getSupportActionBar().setHomeButtonEnabled(false);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        getSupportActionBar().setElevation(0);

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

        String[] reason = Task.this.getResources().getStringArray(
                R.array.task_tab_header);

        if (bmodel.configurationMasterHelper.IS_SHOW_ONLY_SERVER_TASK) {
            reason = new String[1];
            reason[0] = "All";
        }

        int first_tab = 0;
        // Add tabs to Tablayout
        for (String tab_name : reason) {
            if (tabLayout != null) {
                TextView tabOne = (TextView) LayoutInflater.from(this).inflate(R.layout.custom_tab, null);
                tabOne.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
                tabOne.setText(tab_name);
                tabOne.setTextSize(getResources().getDimensionPixelSize(R.dimen.font_nano_small));
                tabLayout.addTab(tabLayout.newTab().setCustomView(tabOne));
                if (first_tab == 0) {
                    tabOne.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
                }
                first_tab++;
            }
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
        } else {
            screenTitle = extras.getString("screentitle");
            setScreenTitle(screenTitle);
        }

        if (bmodel.configurationMasterHelper.IS_SHOW_ONLY_SERVER_TASK) {
            addServerTaskFragments();
        } else {
            addTaskFragments();
        }


        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        if (!bmodel.configurationMasterHelper.IS_NEW_TASK) {
            hideNewTaskMenu();
        }

        tabLayout.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewPager.setCurrentItem(tab.getPosition());
                TextView text = (TextView) tab.getCustomView();
                text.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                viewPager.setCurrentItem(tab.getPosition());
                TextView text = (TextView) tab.getCustomView();
                text.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

        if (bmodel.configurationMasterHelper.MOVE_NEXT_ACTIVITY) {
            LinearLayout footer = (LinearLayout) findViewById(R.id.footer);
            footer.setVisibility(View.VISIBLE);

            Button btnClose = (Button) findViewById(R.id.btn_close);
            btnClose.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View view) {
                    new CommonDialog(getApplicationContext(), Task.this,
                            "", getResources().getString(R.string.move_next_activity),
                            false, getResources().getString(R.string.ok),
                            getResources().getString(R.string.cancel), new CommonDialog.positiveOnClickListener() {
                        @Override
                        public void onPositiveButtonClick() {
                            Intent intent = new Intent(Task.this, HomeScreenTwo.class);

                            Bundle extras = getIntent().getExtras();
                            if (extras != null) {
                                intent.putExtra("IsMoveNextActivity", true);
                                intent.putExtra("CurrentActivityCode", extras.getString("CurrentActivityCode", ""));
                            }

                            startActivity(intent);
                            finish();
                        }
                    }, new CommonDialog.negativeOnClickListener() {
                        @Override
                        public void onNegativeButtonClick() {

                        }
                    }).show();

                }
            });
        }
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

            TaskListFragment taskListFragment = (TaskListFragment) mFragmentList.get(index);

            return taskListFragment;
        }

        @Override
        public int getCount() {

            if (bmodel.configurationMasterHelper.IS_SHOW_ONLY_SERVER_TASK) {
                return 1;
            }else{
                return 3;
            }
        }

        @Override
        public int getItemPosition(Object object) {
            return POSITION_NONE;
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        if (bmodel.userMasterHelper.getUserMasterBO().getUserid() == 0) {
            Toast.makeText(this,
                    getResources().getString(R.string.sessionout_loginagain),
                    Toast.LENGTH_SHORT).show();
            finish();
        }

        if (IsRetailerwisetask) {
            if (bmodel.getRetailerMasterBO().getRetailerID().equals("null")) {
                mSelectedRetailerID = "0";
            } else {
                mSelectedRetailerID = (bmodel.getRetailerMasterBO()
                        .getRetailerID());
            }
        }
    }

    @Override
    protected void onResume() {
        // TODO Auto-generated method stub
        super.onResume();
        bool = false;
        bmodel = (BusinessModel) getApplicationContext();
        bmodel.setContext(this);

        if (bmodel.userMasterHelper.getUserMasterBO().getUserid() == 0) {
            Toast.makeText(this,
                    getResources().getString(R.string.sessionout_loginagain),
                    Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindDrawables(findViewById(R.id.root));
        System.gc();
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
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_task, menu);
        return super.onCreateOptionsMenu(menu);
    }

    // Called whenever we call invalidateOptionsMenu()
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {

        // Funcnality Change, While clicking task in checkbox, save will happen
        menu.findItem(R.id.menu_save).setVisible(false);
        if (hide_new_menu == false)
            menu.findItem(R.id.menu_new_task).setVisible(false);
        menu.findItem(R.id.menu_reason).setVisible(bmodel.configurationMasterHelper.floating_np_reason_photo);
        return super.onPrepareOptionsMenu(menu);
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
                    startActivity(new Intent(this, HomeScreenActivity.class));
                finish();
            }
            overridePendingTransition(R.anim.trans_right_in, R.anim.trans_right_out);
            finish();
            return true;
        } else if (i1 == R.id.menu_new_task) {
            bmodel.outletTimeStampHelper.updateTimeStampModuleWise(SDUtil
                    .now(SDUtil.TIME));
            Intent i = new Intent(Task.this, TaskCreation.class);
            i.putExtra("fromHomeScreen", fromHomeScreen);
            startActivity(i);
            return true;
        } else if (i1 == R.id.menu_reason) {
            bmodel.reasonHelper.downloadNpReason(bmodel.retailerMasterBO.getRetailerID(), "MENU_TASK");
            ReasonPhotoDialog dialog = new ReasonPhotoDialog();
            dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialog) {
                    if (bmodel.reasonHelper.isNpReasonPhotoAvaiable(bmodel.retailerMasterBO.getRetailerID(), "MENU_TASK")) {
                        bmodel.saveModuleCompletion("MENU_TASK");
                        finish();
                    }
                }
            });
            Bundle args = new Bundle();
            args.putString("modulename", "MENU_TASK");
            dialog.setCancelable(false);
            dialog.setArguments(args);
            dialog.show(getSupportFragmentManager(), "ReasonDialogFragment");
            return true;
        }
        return false;
    }

    public void hideNewTaskMenu() {
        hide_new_menu = false;
    }

    @Override
    public void onClick(View v) {
        // TODO Auto-generated method stub
    }

    private void addTaskFragments() {
        mFragmentList = new ArrayList<>();
        mSelectedFragment = new TaskListFragment();
        Bundle args = new Bundle();
        args.putInt("type", 0);
        args.putBoolean("isRetailer", IsRetailerwisetask);
        args.putBoolean("fromReview", fromReviewScreen);
        mSelectedFragment.setArguments(args);
        mFragmentList.add(mSelectedFragment);


        mSelectedFragment = new TaskListFragment();
        Bundle args1 = new Bundle();
        args1.putInt("type", 1);
        args1.putBoolean("isRetailer", IsRetailerwisetask);
        args1.putBoolean("fromReview", fromReviewScreen);
        mSelectedFragment.setArguments(args1);
        mFragmentList.add(mSelectedFragment);


        mSelectedFragment = new TaskListFragment();
        Bundle args2 = new Bundle();
        args2.putInt("type", 2);
        args2.putBoolean("isRetailer", IsRetailerwisetask);
        args2.putBoolean("fromReview", fromReviewScreen);
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
        mSelectedFragment.setArguments(args1);
        mFragmentList.add(mSelectedFragment);

    }


}
