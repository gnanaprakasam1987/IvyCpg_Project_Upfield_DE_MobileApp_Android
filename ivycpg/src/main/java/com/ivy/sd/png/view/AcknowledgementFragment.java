package com.ivy.sd.png.view;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.bo.JointCallAcknowledgementCountBO;
import com.ivy.sd.png.commons.IvyBaseFragment;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.provider.ConfigurationMasterHelper;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by anandasir.v on 9/7/2017.
 */

public class AcknowledgementFragment extends IvyBaseFragment {
    BusinessModel bmodel;
    View view;
    FragmentManager fm;
    boolean isFromHomeScreenTwo = false;
    private ArrayList<JointCallAcknowledgementCountBO> joinCallAcknowledgementCountList;
    RecyclerView dashBoardList;
    DashBoardListViewAdapter dashBoardListViewAdapter;
    String screenTitle = "";
    Toolbar toolbar;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_acknowledgement, container, false);

        getActivity().getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        bmodel = (BusinessModel) getActivity().getApplicationContext();
        bmodel.setContext(getActivity());
        fm = getActivity().getSupportFragmentManager();
        if (getActivity().getIntent().getExtras() != null) {
            screenTitle = getActivity().getIntent().getExtras().getString("screentitle");
        }
        setHasOptionsMenu(true);
//        setUpActionBar();
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();

        final ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();

        if (actionBar != null) {
            actionBar.setDisplayShowTitleEnabled(false);
            actionBar.setElevation(0);
            setScreenTitle(screenTitle);
        }

        dashBoardList = (RecyclerView) view.findViewById(R.id.acknowledgementLV);
        dashBoardList.setHasFixedSize(false);
        dashBoardList.setNestedScrollingEnabled(false);
        dashBoardList.setLayoutManager(new LinearLayoutManager(getActivity()));
        bmodel.acknowledgeHelper.loadJointCallAcknowledgementCount();
        joinCallAcknowledgementCountList = bmodel.acknowledgeHelper.getAcknowledgementCountList();

        dashBoardListViewAdapter = new DashBoardListViewAdapter(joinCallAcknowledgementCountList);
        dashBoardList.setAdapter(dashBoardListViewAdapter);
    }

    public class DashBoardListViewAdapter extends RecyclerView.Adapter<DashBoardListViewAdapter.ViewHolder> {
        private final List<JointCallAcknowledgementCountBO> dashboardList;

        public DashBoardListViewAdapter(List<JointCallAcknowledgementCountBO> dashboardList) {
            this.dashboardList = dashboardList;
        }

        @Override
        public DashBoardListViewAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.row_acknowledgementcount, parent, false);
            return new DashBoardListViewAdapter.ViewHolder(v);
        }


        @Override
        public void onBindViewHolder(final DashBoardListViewAdapter.ViewHolder holder, int position) {
            final JointCallAcknowledgementCountBO dashboardData = dashboardList.get(position);

            holder.dashboardDataObj = dashboardData;
            //typefaces
            holder.txtUser.setTypeface(bmodel.configurationMasterHelper.getFontBaloobhai(ConfigurationMasterHelper.FontType.REGULAR));
            holder.txtUserCount.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));

            holder.txtUser.setText(dashboardData.getUserName());
            holder.txtUserCount.setText(dashboardData.getCount());
            //for P3M trend Chart loading
            holder.icon_ll.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent i = new Intent(getActivity(), AcknowledgementDetailActivity.class);
                    i.putExtra("screentitle", screenTitle);
                    i.putExtra("UserID", holder.dashboardDataObj.getUserID());
                    startActivity(i);
                    getActivity().finish();
                }
            });
            holder.lnr.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent i = new Intent(getActivity(), AcknowledgementDetailActivity.class);
                    i.putExtra("screentitle", screenTitle);
                    i.putExtra("UserID", holder.dashboardDataObj.getUserID());
                    startActivity(i);
                    getActivity().finish();
                }
            });

        }

        @Override
        public int getItemViewType(int position) {
            return position;
        }

        @Override
        public int getItemCount() {
            return dashboardList.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {

            TextView txtUser;
            TextView txtUserCount;
            LinearLayout icon_ll;
            LinearLayout lnr;
            JointCallAcknowledgementCountBO dashboardDataObj;

            public ViewHolder(View row) {
                super(row);
                txtUser = (TextView) row
                        .findViewById(R.id.txtUser);
                txtUserCount = (TextView) row
                        .findViewById(R.id.txtUserCount);
                icon_ll = (LinearLayout) row
                        .findViewById(R.id.icon_ll);
                lnr = (LinearLayout) row
                        .findViewById(R.id.lnr);
            }
        }
    }

    private void setUpActionBar() {
        ((AppCompatActivity) getActivity()).getSupportActionBar();
        ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayShowTitleEnabled(false);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            ((AppCompatActivity) getActivity()).getSupportActionBar().setElevation(0);
        }

//screen title set based previous screen
        if (!isFromHomeScreenTwo) {
            if (bmodel.getMenuName("MENU_DASH").endsWith(""))
                bmodel.configurationMasterHelper.downloadMainMenu();
            if (getArguments().getString("screentitle") == null)
                setScreenTitle(bmodel.getMenuName("MENU_DASH"));
            else
                setScreenTitle(getArguments().getString("screentitle"));
        } else {
            if (getArguments().getString("screentitle").toString().isEmpty()) {
                bmodel.configurationMasterHelper
                        .downloadNewActivityMenu(ConfigurationMasterHelper.MENU_ACTIVITY);
                setScreenTitle(bmodel.getMenuName("MENU_RTR_KPI"));
            } else
                setScreenTitle(getActivity().getIntent().getStringExtra("screentitle"));
        }
        //if (!BusinessModel.dashHomeStatic)
            ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayShowHomeEnabled(true);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {

        super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int i = item.getItemId();
        if (i == android.R.id.home) {
            startActivity(new Intent(getActivity(),
                    HomeScreenActivity.class));
            getActivity().finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

}
