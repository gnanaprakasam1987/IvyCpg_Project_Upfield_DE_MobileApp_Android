package com.ivy.cpg.view.acknowledgement;

import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentManager;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.commons.IvyBaseFragment;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.cpg.view.homescreen.HomeScreenActivity;
import com.ivy.utils.FontUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by anandasir.v on 9/7/2017.
 */

public class AcknowledgementFragment extends IvyBaseFragment {
    BusinessModel bmodel;
    View view;
    FragmentManager fm;
    RecyclerView dashBoardList;
    DashBoardListViewAdapter dashBoardListViewAdapter;
    String screenTitle = "";
    Toolbar toolbar;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_acknowledgement, container, false);

        if (getActivity() != null)
        getActivity().getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        bmodel = (BusinessModel) getActivity().getApplicationContext();
        bmodel.setContext(getActivity());
        fm = getActivity().getSupportFragmentManager();
        if (getActivity().getIntent().getExtras() != null) {
            screenTitle = getActivity().getIntent().getExtras().getString("screentitle");
        }
        setHasOptionsMenu(true);
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();

        ActionBar actionBar = null;
        if (getActivity() != null)
        actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();

        if (actionBar != null) {
            actionBar.setDisplayShowTitleEnabled(false);
            actionBar.setElevation(0);
            setScreenTitle(screenTitle);
        }

        dashBoardList = view.findViewById(R.id.acknowledgementLV);
        dashBoardList.setHasFixedSize(false);
        dashBoardList.setNestedScrollingEnabled(false);
        dashBoardList.setLayoutManager(new LinearLayoutManager(getActivity()));
        AcknowledgementHelper acknowledgementHelper = AcknowledgementHelper.getInstance(getActivity());
        acknowledgementHelper.loadJointCallAcknowledgementCount();
        ArrayList<JointCallAcknowledgementCountBO> joinCallAcknowledgementCountList = acknowledgementHelper.getAcknowledgementCountList();

        dashBoardListViewAdapter = new DashBoardListViewAdapter(joinCallAcknowledgementCountList);
        dashBoardList.setAdapter(dashBoardListViewAdapter);
    }

    public class DashBoardListViewAdapter extends RecyclerView.Adapter<DashBoardListViewAdapter.ViewHolder> {
        private final List<JointCallAcknowledgementCountBO> dashboardList;

        private DashBoardListViewAdapter(List<JointCallAcknowledgementCountBO> dashboardList) {
            this.dashboardList = dashboardList;
        }

        @NonNull
        @Override
        public DashBoardListViewAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.row_acknowledgementcount, parent, false);
            return new DashBoardListViewAdapter.ViewHolder(v);
        }


        @Override
        public void onBindViewHolder(@NonNull final DashBoardListViewAdapter.ViewHolder holder, int position) {
            final JointCallAcknowledgementCountBO dashboardData = dashboardList.get(position);

            holder.dashboardDataObj = dashboardData;
            //typefaces
            holder.txtUser.setTypeface(FontUtils.getFontBalooHai(getActivity(), FontUtils.FontType.REGULAR));
            holder.txtUserCount.setTypeface(FontUtils.getFontRoboto(getActivity(), FontUtils.FontType.LIGHT));

            holder.txtUser.setText(dashboardData.getUserName());
            holder.txtUserCount.setText(dashboardData.getCount());
            //for P3M trend Chart loading
            holder.detIcon.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent i = new Intent(getActivity(), AcknowledgementDetailActivity.class);
                    i.putExtra("screentitle", screenTitle);
                    i.putExtra("UserID", holder.dashboardDataObj.getUserID());
                    startActivity(i);
                    if (getActivity() != null)
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
                    if (getActivity() != null)
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
            ImageView detIcon;
            LinearLayout lnr;
            JointCallAcknowledgementCountBO dashboardDataObj;

            public ViewHolder(View row) {
                super(row);
                txtUser = row
                        .findViewById(R.id.txtUser);
                txtUserCount = row
                        .findViewById(R.id.txtUserCount);
                detIcon = row
                        .findViewById(R.id.btn_inandout);
                lnr = row
                        .findViewById(R.id.lnr);
            }
        }
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
            if (getActivity() != null)
            getActivity().finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

}
