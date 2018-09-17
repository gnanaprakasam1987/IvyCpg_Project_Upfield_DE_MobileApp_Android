package com.ivy.ui.dashboard.adapter;

import android.content.Context;
import android.support.constraint.Group;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.github.mikephil.charting.charts.PieChart;
import com.ivy.cpg.view.dashboard.DashBoardBO;
import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.provider.ConfigurationMasterHelper;

import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;

public class DashboardListAdapter extends RecyclerView.Adapter<DashboardListAdapter.DashBoardListViewHolder> {

    private final List<DashBoardBO> dashboardList;

    private Context mContext;

    @Inject
    ConfigurationMasterHelper configurationMasterHelper;

    public DashboardListAdapter(Context context, List<DashBoardBO> dashboardList) {
        this.mContext = context;
        this.dashboardList = dashboardList;

        boolean allow_back_date = configurationMasterHelper.ALLOW_BACK_DATE;
    }

    @Override
    public DashBoardListViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return null;
    }

    @Override
    public void onBindViewHolder(DashBoardListViewHolder holder, int position) {

    }

    @Override
    public int getItemCount() {
        return dashboardList.size();
    }

    public class DashBoardListViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.factorName_dashboard_tv)
        TextView factorNameTxtView;

        @BindView(R.id.incentive_title)
        TextView incentive_title;

        @BindView(R.id.initiative_dashboard_tv)
        TextView incentiveTxtView;

        @BindView(R.id.score_title)
        TextView score_title;

        @BindView(R.id.score_dashboard_tv)
        TextView scoreTxtView;

        @BindView(R.id.achived_title)
        TextView achived_title;

        @BindView(R.id.acheived_dashboard_tv)
        TextView acheivedTxtView;

        @BindView(R.id.balance_title)
        TextView balance_title;

        @BindView(R.id.balance_dashboard_tv)
        TextView balanceTxtView;

        @BindView(R.id.flex_title)
        TextView flex_title;

        @BindView(R.id.flex_dashboard_tv)
        TextView flexTxtView;

        @BindView(R.id.target_title)
        TextView target_title;

        @BindView(R.id.target_dashboard_tv)
        TextView targetTxtView;

        @BindView(R.id.tv_skuwise)
        TextView skuWiseTxtView;

        @BindView(R.id.index_dashboard_tv)
        TextView indexTxtView;

        @BindView(R.id.pieChart)
        PieChart pieChart;

        @BindView(R.id.scoreGroup)
        Group scoreGroup;

        @BindView(R.id.initiativeGroup)
        Group initiativeGroup;

        @BindView(R.id.targetGroup)
        Group targetGroup;

        @BindView(R.id.achievedGroup)
        Group achievedGroup;

        @BindView(R.id.balanceGroup)
        Group balanceGroup;

        @BindView(R.id.flexGroup)
        Group flexGroup;

        @BindView(R.id.skuGroup)
        Group skuGroup;

        public DashBoardListViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);

        }
    }
}
