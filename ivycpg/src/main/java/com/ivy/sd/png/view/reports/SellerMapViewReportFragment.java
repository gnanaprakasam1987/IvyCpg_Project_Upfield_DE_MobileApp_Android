package com.ivy.sd.png.view.reports;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.google.android.gms.maps.GoogleMap;
import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.bo.OutletReportBO;
import com.ivy.sd.png.commons.CustomMapFragment;
import com.ivy.sd.png.commons.IvyBaseFragment;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.view.HomeScreenActivity;
import com.ivy.sd.png.view.SellerListFragment;

import java.util.ArrayList;

/**
 * Created by rajkumar.s on 10/27/2017.
 */

public class SellerMapViewReportFragment extends IvyBaseFragment implements SellerListFragment.SellerSelectionInterface{

    View view;
    BusinessModel bmodel;

    private DrawerLayout mDrawerLayout;
    FrameLayout drawer;

    private ArrayList<OutletReportBO> lstUsers;
    private ArrayList<OutletReportBO>lstReports;

    private GoogleMap mMap;
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        getActivity().getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        view = inflater.inflate(R.layout.fragment_seller_mapview, container, false);

        bmodel = (BusinessModel) getActivity().getApplicationContext();
        bmodel.setContext(getActivity());

        if (bmodel.userMasterHelper.getUserMasterBO().getUserid() == 0) {
            Toast.makeText(getActivity(),
                    getResources().getString(R.string.sessionout_loginagain),
                    Toast.LENGTH_SHORT).show();
            getActivity().finish();
        }


        if(lstUsers==null)
        lstUsers=bmodel.reportHelper.downloadUsers();

        if(lstReports==null)
            lstReports=bmodel.reportHelper.downloadOutletReports();

        return view;
    }


    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);


    }

    @Override
    public void onStart() {
        super.onStart();
        setHasOptionsMenu(true);

        ActionBarDrawerToggle mDrawerToggle;
        drawer = (FrameLayout) getView().findViewById(R.id.right_drawer);

      /*  int width = getResources().getDisplayMetrics().widthPixels;
        DrawerLayout.LayoutParams params = (android.support.v4.widget.DrawerLayout.LayoutParams) drawer.getLayoutParams();
        params.width = width;
        drawer.setLayoutParams(params);*/

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
                if (getActionBar() != null) {
                    setScreenTitle(bmodel.mSelectedActivityName);
                }

                getActivity().supportInvalidateOptionsMenu();
            }

            public void onDrawerOpened(View drawerView) {
                if (getActionBar() != null) {
                    setScreenTitle(getResources().getString(R.string.filter));
                }

                getActivity().supportInvalidateOptionsMenu();
            }
        };

        mDrawerLayout.addDrawerListener(mDrawerToggle);
        mDrawerLayout.closeDrawer(GravityCompat.END);


        CustomMapFragment mCustomMapFragment = ((CustomMapFragment) getActivity().getSupportFragmentManager().findFragmentById(R.id.seller_map));
      //  mCustomMapFragment.setOnDragListener(ProfileActivity.this);
      //  mMap = mCustomMapFragment.getMap();



    }

    private ActionBar getActionBar() {
        return ((AppCompatActivity) getActivity()).getSupportActionBar();
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
        if (i == R.id.menu_users) {
            loadUsers();
        }
        else if(i==android.R.id.home){
            if (mDrawerLayout.isDrawerOpen(GravityCompat.END))
                mDrawerLayout.closeDrawers();
            else {
                onBackButtonClick();
            }
        }
        return super.onOptionsItemSelected(item);
    }

    private void onBackButtonClick() {
        Intent i = new Intent(getActivity(), HomeScreenActivity.class);
        i.putExtra("menuCode", "MENU_REPORT");
        i.putExtra("title", "aaa");
        startActivity(i);
        getActivity().finish();
        getActivity().overridePendingTransition(R.anim.trans_right_in, R.anim.trans_right_out);

    }

    private void loadUsers(){

        mDrawerLayout.openDrawer(GravityCompat.END);

        android.support.v4.app.FragmentManager fm = getActivity().getSupportFragmentManager();
        SellerListFragment frag = (SellerListFragment) fm.findFragmentByTag("filter");
        android.support.v4.app.FragmentTransaction ft = fm.beginTransaction();
        if (frag != null)
            ft.detach(frag);

        SellerListFragment fragment=new SellerListFragment();
        Bundle bundle = new Bundle();
        bundle.putSerializable("users",lstUsers);
        fragment.setArguments(bundle);

        ft.replace(R.id.right_drawer, fragment, "filter");
        ft.commit();
    }

    @Override
    public void updateMapView(ArrayList<Integer> mSelectedUsers,boolean isAlluser) {

    }

    @Override
    public void updateClose() {
        mDrawerLayout.closeDrawers();
    }
}
