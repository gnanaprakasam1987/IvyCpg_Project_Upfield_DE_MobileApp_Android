package com.ivy.sd.png.view.reports;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.bo.OutletReportBO;
import com.ivy.sd.png.commons.IvyBaseFragment;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.util.Commons;
import com.ivy.sd.png.view.HomeScreenActivity;
import com.ivy.sd.png.view.SellerListFragment;

import java.util.ArrayList;

/**
 * Created by rajkumar.s on 11/3/2017.
 */

public class OutletPerformanceReportFragmnet extends IvyBaseFragment implements SellerListFragment.SellerSelectionInterface {

    View view;
    private BusinessModel bmodel;
    private DrawerLayout mDrawerLayout;
    FrameLayout drawer;

    private ArrayList<OutletReportBO> lstUsers;
    private ArrayList<OutletReportBO>lstReports;

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        getActivity().getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);


        view = inflater.inflate(R.layout.fragment_outlet_performance_report, container, false);
        bmodel = (BusinessModel) getActivity().getApplicationContext();
        bmodel.setContext(getActivity());

        if (bmodel.userMasterHelper.getUserMasterBO().getUserid() == 0) {
            Toast.makeText(getActivity(),
                    getResources().getString(R.string.sessionout_loginagain),
                    Toast.LENGTH_SHORT).show();
            getActivity().finish();
        }



            lstUsers = bmodel.reportHelper.downloadUsers();
            lstReports = bmodel.reportHelper.downloadOutletReports();

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();

        try {
            setHasOptionsMenu(true);

            ActionBarDrawerToggle mDrawerToggle;
            drawer = (FrameLayout) getView().findViewById(R.id.right_drawer);

            mDrawerLayout = (DrawerLayout) getView().findViewById(
                    R.id.drawer_layout);

            mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow,
                    GravityCompat.START);
            mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow,
                    GravityCompat.END);
            mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);

            mDrawerToggle = new ActionBarDrawerToggle(getActivity(),
                    mDrawerLayout,
                    R.string.ok,
                    R.string.close
            ) {
                public void onDrawerClosed(View view) {
                        ((TextView)getActivity(). findViewById(R.id.tv_toolbar_title)).setText(bmodel.mSelectedActivityName);
                    getActivity().supportInvalidateOptionsMenu();
                }

                public void onDrawerOpened(View drawerView) {
                        ((TextView)getActivity(). findViewById(R.id.tv_toolbar_title)).setText(getResources().getString(R.string.filter));

                    getActivity().supportInvalidateOptionsMenu();
                }
            };

            mDrawerLayout.addDrawerListener(mDrawerToggle);
            mDrawerLayout.closeDrawer(GravityCompat.END);


        }
        catch (Exception ex){
            Commons.printException(ex);
        }


    }

    private void updateView(ArrayList<OutletReportBO> mSelectedUsers,boolean isAllUser){

        if(isAllUser||mSelectedUsers!=null){

        }

    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_seller_mapview, menu);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);

        boolean drawerOpen = mDrawerLayout.isDrawerOpen(GravityCompat.END);
        menu.findItem(R.id.menu_users).setVisible(!drawerOpen);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int i = item.getItemId();
        if(i==android.R.id.home){
            if (mDrawerLayout.isDrawerOpen(GravityCompat.END))
                mDrawerLayout.closeDrawers();
            else {
                onBackButtonClick();
            }
        }
        else if (i == R.id.menu_users) {
            loadUsers();
        }

        return super.onOptionsItemSelected(item);
    }

    private void loadUsers(){

        try {
            mDrawerLayout.openDrawer(GravityCompat.END);

            android.support.v4.app.FragmentManager fm = getActivity().getSupportFragmentManager();
            SellerListFragment frag = (SellerListFragment) fm.findFragmentByTag("filter");
            android.support.v4.app.FragmentTransaction ft = fm.beginTransaction();
            if (frag != null)
                ft.detach(frag);

            SellerListFragment fragment = new SellerListFragment();
            Bundle bundle = new Bundle();
            bundle.putSerializable("users", lstUsers);
            fragment.setArguments(bundle);

            ft.replace(R.id.right_drawer, fragment, "filter");
            ft.commit();

            mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_OPEN);
        }
        catch (Exception ex){

        }
    }

    private void onBackButtonClick() {
        Intent i = new Intent(getActivity(), HomeScreenActivity.class);
        i.putExtra("menuCode", "MENU_REPORT");
        i.putExtra("title", "");
        startActivity(i);
        getActivity().finish();
        getActivity().overridePendingTransition(R.anim.trans_right_in, R.anim.trans_right_out);

    }

    @Override
    public void updateUserSelection(ArrayList<Integer> mSelectedUsers, boolean isAllUser) {


        mDrawerLayout.closeDrawers();
    }

    @Override
    public void updateClose() {
        mDrawerLayout.closeDrawers();
    }
}
