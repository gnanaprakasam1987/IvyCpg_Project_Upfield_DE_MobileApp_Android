package com.ivy.cpg.view.homescreen;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.FragmentManager;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.LinearLayout;

import com.ivy.cpg.view.van.stockproposal.StockProposalFragment;
import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.commons.IvyBaseActivityNoActionBar;
import com.ivy.sd.png.model.BrandDialogInterface;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.model.FiveLevelFilterCallBack;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by rajkumar.s on 12/30/2016.
 */

public class HomeScreenActivity extends IvyBaseActivityNoActionBar implements HomeScreenItemClickedListener,
        BrandDialogInterface, FiveLevelFilterCallBack {

    private DrawerLayout mDrawerLayout;
    private LinearLayout content_frame;
    private Handler handler;
    private HomeScreenFragment mHomeScreenFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);


        setContentView(R.layout.activity_homescreen);

        BusinessModel bmodel = (BusinessModel) getApplicationContext();
        bmodel.setContext(this);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        content_frame = findViewById(R.id.root);


        getSupportActionBar().setTitle(null);
        getSupportActionBar().setIcon(null);
        getSupportActionBar().setDisplayShowTitleEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setDisplayUseLogoEnabled(true);

        mDrawerLayout = findViewById(R.id.drawer_layout);

        ActionBarDrawerToggle mDrawerToggle = new ActionBarDrawerToggle(this,
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
                if (imm != null && imm.isActive())
                    imm.hideSoftInputFromWindow(drawerView.getWindowToken(), 0);

                content_frame.setTranslationX(moveFactor);
            }
        };

        mDrawerToggle.syncState();
        mDrawerLayout.addDrawerListener(mDrawerToggle);


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
    public void onListItemSelected() {
        if (mDrawerLayout.isDrawerVisible(GravityCompat.START))
            mDrawerLayout.closeDrawers();
    }

    public void numberPressed(View vw) {
        FragmentManager fm = getSupportFragmentManager();
        StockProposalFragment asf = (StockProposalFragment) fm
                .findFragmentById(R.id.fragment_content);
        asf.numberPressed(vw);
    }

    @Override
    public void updateBrandText(String mFilterText, int id) {
        FragmentManager fm = getSupportFragmentManager();
        StockProposalFragment asf = (StockProposalFragment) fm
                .findFragmentById(R.id.fragment_content);
        asf.updateBrandText(mFilterText, id);
    }

    @Override
    public void updateGeneralText(String mFilterText) {
        FragmentManager fm = getSupportFragmentManager();
        StockProposalFragment asf = (StockProposalFragment) fm
                .findFragmentById(R.id.fragment_content);
        asf.updateGeneralText(mFilterText);
    }

    @Override
    public void updateCancel() {
        FragmentManager fm = getSupportFragmentManager();
        StockProposalFragment asf = (StockProposalFragment) fm
                .findFragmentById(R.id.fragment_content);
        asf.updateCancel();
    }

    @Override
    public void updateFromFiveLevelFilter(int mProductId, HashMap<Integer, Integer> mSelectedIdByLevelId, ArrayList<Integer> mAttributeProducts, String mFilterText) {
        FragmentManager fm = getSupportFragmentManager();
        StockProposalFragment asf = (StockProposalFragment) fm
                .findFragmentById(R.id.fragment_content);
        asf.updateFromFiveLevelFilter(mProductId, mSelectedIdByLevelId, mAttributeProducts, mFilterText);
    }
}
