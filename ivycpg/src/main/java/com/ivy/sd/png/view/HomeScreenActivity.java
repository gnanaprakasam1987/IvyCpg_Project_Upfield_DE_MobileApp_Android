package com.ivy.sd.png.view;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.TranslateAnimation;
import android.view.inputmethod.InputMethodManager;
import android.widget.LinearLayout;

import com.ivy.cpg.view.van.LoadManagementFragment;
import com.ivy.cpg.view.van.PlanningSubScreenFragment;
import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.commons.IvyBaseActivityNoActionBar;
import com.ivy.sd.png.commons.IvyBaseFragment;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.util.Commons;

/**
 * Created by rajkumar.s on 12/30/2016.
 */

public class HomeScreenActivity extends IvyBaseActivityNoActionBar implements HomeScreenFragment.homeScreenItemClickedListener {

    private ActionBarDrawerToggle mDrawerToggle;
    private DrawerLayout mDrawerLayout;
    BusinessModel bmodel;
    Toolbar toolbar;
    LinearLayout content_frame;
    private float lastTranslate = 0.0f;
    Handler handler;
    HomeScreenFragment mHomeScreenFragment;
    IvyBaseFragment baseFragment;
    Intent i = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        setContentView(R.layout.activity_homescreen);

        bmodel = (BusinessModel) getApplicationContext();
        bmodel.setContext(this);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        content_frame = (LinearLayout) findViewById(R.id.root);


        getSupportActionBar().setTitle(null);
        getSupportActionBar().setIcon(null);
        getSupportActionBar().setDisplayShowTitleEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayUseLogoEnabled(true);

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        //  mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_OPEN);

        mDrawerToggle = new ActionBarDrawerToggle(this,
                mDrawerLayout, /* DrawerLayout object */
                R.string.ok, R.string.close) {
            public void onDrawerClosed(View view) {
                supportInvalidateOptionsMenu();
            }

            public void onDrawerOpened(View drawerView) {
                supportInvalidateOptionsMenu();
            }


            @Override
            public void onDrawerSlide(View drawerView, float slideOffset) {
                super.onDrawerSlide(drawerView, slideOffset);

                float moveFactor = (drawerView.getWidth() * slideOffset);

                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                if(imm!=null && imm.isActive())
                    imm.hideSoftInputFromWindow(drawerView.getWindowToken(), 0);

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                    content_frame.setTranslationX(moveFactor);
                } else {
                    TranslateAnimation anim = new TranslateAnimation(lastTranslate, moveFactor, 0.0f, 0.0f);
                    anim.setDuration(0);
                    anim.setFillAfter(true);
                    content_frame.startAnimation(anim);

                    lastTranslate = moveFactor;
                }
            }
        };

        mDrawerToggle.syncState();
        mDrawerLayout.addDrawerListener(mDrawerToggle);
        //mDrawerLayout.openDrawer(GravityCompat.START);

        mHomeScreenFragment = (HomeScreenFragment) getSupportFragmentManager()
                .findFragmentById(R.id.homescreen_fragment);
        mHomeScreenFragment.setmHomeScreenItemClickedListener(this);

    }

    public Handler getHandler() {
        if (mHomeScreenFragment.getHandler() != null) {
            handler = mHomeScreenFragment.getHandler();
        }
        return handler;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();
        if (id == android.R.id.home) {
            if (mDrawerLayout.isDrawerOpen(GravityCompat.START)) {
                mDrawerLayout.closeDrawer(GravityCompat.START);
            } else {
                mDrawerLayout.openDrawer(GravityCompat.START);
            }
            return true;
        }

        return super.onOptionsItemSelected(item);


    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }


    @Override
    public void onListItemSelected() {
        if (mDrawerLayout.isDrawerVisible(GravityCompat.START))
            mDrawerLayout.closeDrawers();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        if (baseFragment instanceof LoadManagementFragment) {
            LoadManagementFragment loadmgtFrag = (LoadManagementFragment) baseFragment;
            try {
                if (i != null && loadmgtFrag.mSelectedBarCodemodule != null) {
                    loadmgtFrag.checkBarcodeData(i);
                    loadmgtFrag.mSelectedBarCodemodule = null;

                }
            } catch (Exception e) {
                Commons.printException("" + e);
            }

        } else if (baseFragment instanceof PlanningSubScreenFragment) {
            PlanningSubScreenFragment planningSubScreenFragment = (PlanningSubScreenFragment) baseFragment;
            try {
                if (i != null && planningSubScreenFragment.mSelectedBarCodemodule != null) {
                    planningSubScreenFragment.checkBarcodeData(i);
                    planningSubScreenFragment.mSelectedBarCodemodule = null;
                }
            } catch (Exception e) {
                Commons.printException("" + e);
            }
        }
    }
}
