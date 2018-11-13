package com.ivy.ui.dashboard.adapter;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Point;
import android.support.constraint.ConstraintLayout;
import android.support.constraint.Group;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.text.style.UnderlineSpan;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.PercentFormatter;
import com.ivy.cpg.view.dashboard.DashBoardBO;
import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.commons.SDUtil;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.provider.ConfigurationMasterHelper;
import com.ivy.sd.png.util.Commons;
import com.ivy.ui.dashboard.DashboardClickListener;
import com.ivy.ui.dashboard.SellerDashboardContract;
import com.ivy.ui.dashboard.di.DaggerSellerDashboardComponent;
import com.ivy.ui.dashboard.di.SellerDashboardModule;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.inject.Inject;

import butterknife.BindView;
import butterknife.ButterKnife;

public class DashboardListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final List<DashBoardBO> dashboardList;

    private Context mContext;

    private HashMap<String, String> labelsMap;

    private ConfigurationMasterHelper configurationMasterHelper;

    private DashboardClickListener dashboardClickListener;


    public DashboardListAdapter(Context context, List<DashBoardBO> dashboardList, HashMap<String, String> labelsMap, DashboardClickListener dashboardClickListener) {
        this.mContext = context;
        this.dashboardList = dashboardList;
        this.labelsMap = labelsMap;
        this.dashboardClickListener = dashboardClickListener;

        configurationMasterHelper = ((BusinessModel) context.getApplicationContext()).getComponent().configurationMasterHelper();

    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType == 0) {
            return new DashBoardListViewHolder(LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.view_seller_dashboard_list_row, parent, false));
        } else {
            return new MinDashBoardListViewHolder(LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.view_seller_dashboard_without_target, parent, false));
        }

    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {

        final int current_position = position;
        final DashBoardBO dashboardData = dashboardList.get(current_position);

        if (holder.getItemViewType() == 0) {

            DashBoardListViewHolder dashBoardListViewHolder = (DashBoardListViewHolder) holder;
            dashBoardListViewHolder.factorNameTxtView.setText(dashboardData.getText());
            dashBoardListViewHolder.factorNameTxtView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    dashboardClickListener.onFactorNameClick(current_position);
                }
            });


            if (dashboardData.getSubDataCount() > 0) {
                dashBoardListViewHolder.skuGroup.setVisibility(View.VISIBLE);
                SpannableString str = new SpannableString(dashBoardListViewHolder.skuWiseTxtView
                        .getText().toString());
                str.setSpan(new UnderlineSpan(), 0, str.length(),
                        Spanned.SPAN_PARAGRAPH);
                str.setSpan(new ForegroundColorSpan(ContextCompat.getColor(mContext, R.color.Black)), 0,
                        str.length(), 0);
                dashBoardListViewHolder.skuWiseTxtView.setText(str);
                dashBoardListViewHolder.skuWiseTxtView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        dashboardClickListener.onSkuWiseClick(current_position);
                    }
                });
            } else {
                dashBoardListViewHolder.skuGroup.setVisibility(View.GONE);
            }


            if (dashboardData.getFlex1() == 1) {
                dashBoardListViewHolder.acheivedTxtView.setText(SDUtil.getWholeNumber(dashboardData.getKpiAcheived()));
                dashBoardListViewHolder.targetTxtView.setText(SDUtil.getWholeNumber(dashboardData.getKpiTarget()));
                double balanceValue = SDUtil.convertToInt(dashboardData.getKpiTarget()) - SDUtil.convertToInt(dashboardData.getKpiAcheived());
                dashBoardListViewHolder.balanceTxtView.setText(balanceValue > 0 ? SDUtil.getWholeNumber(getFormattedString(balanceValue)) : "0");
                String strCalcPercentage = dashboardData.getCalculatedPercentage() + "%";
                float temp_ach = 0;
                if (Float.parseFloat(dashboardData.getKpiTarget()) > 0)
                    temp_ach = Float.parseFloat(dashboardData.getKpiAcheived()) - Float.parseFloat(dashboardData.getKpiTarget());
                if (temp_ach > 0) {
                    int bonus = Math.round(SDUtil.convertToFloat(dashboardData.getKpiAcheived()) /
                            (SDUtil.convertToFloat(dashboardData.getKpiTarget())) * 100);
                    dashBoardListViewHolder.indexTxtView.setText(SDUtil.roundIt(bonus, 1) + "%");
                } else {
                    dashBoardListViewHolder.indexTxtView.setText(strCalcPercentage);
                }
                dashBoardListViewHolder.flexTxtView.setText(SDUtil.getWholeNumber(dashboardData.getKpiFlex()));
                dashBoardListViewHolder.incentiveTxtView.setText(SDUtil.getWholeNumber(dashboardData.getKpiIncentive()));
                dashBoardListViewHolder.scoreTxtView.setText(SDUtil.getWholeNumber(dashboardData.getKpiScore()));
            } else {
                try {
                    String strKpiAchieved = getFormattedString(SDUtil.convertToDouble(dashboardData.getKpiAcheived()));
                    dashBoardListViewHolder.acheivedTxtView.setText(strKpiAchieved);
                    String strKpiTarget = getFormattedString(SDUtil.convertToDouble(dashboardData.getKpiTarget()));
                    dashBoardListViewHolder.targetTxtView.setText(strKpiTarget);
                } catch (Exception e) {
                    Commons.printException(e + "");
                }
                String strCalcPercentage = dashboardData.getCalculatedPercentage() + "%";
                float temp_ach = 0;
                if (Float.parseFloat(dashboardData.getKpiTarget()) > 0)
                    temp_ach = Float.parseFloat(dashboardData.getKpiAcheived()) - Float.parseFloat(dashboardData.getKpiTarget());
                if (temp_ach > 0) {
                    int bonus = Math.round(SDUtil.convertToFloat(dashboardData.getKpiAcheived()) /
                            (SDUtil.convertToFloat(dashboardData.getKpiTarget())) * 100);
                    dashBoardListViewHolder.incentiveTxtView.setText(SDUtil.roundIt(bonus, 1) + "%");
                } else {
                    dashBoardListViewHolder.incentiveTxtView.setText(strCalcPercentage);
                }
                double balanceValue = SDUtil.convertToDouble(dashboardData.getKpiTarget()) - SDUtil.convertToDouble(dashboardData.getKpiAcheived());
                dashBoardListViewHolder.balanceTxtView.setText(balanceValue > 0 ? getFormattedString(balanceValue) : "0");
                dashBoardListViewHolder.flexTxtView.setText(dashboardData.getKpiFlex());
                dashBoardListViewHolder.incentiveTxtView.setText(getFormattedString(SDUtil.convertToDouble(dashboardData.getKpiIncentive() + "")));
                String strKpiScore = dashboardData.getKpiScore() + "";
                dashBoardListViewHolder.scoreTxtView.setText(strKpiScore);
                //isSemiCircleChartRequired = true;
            }

            if (!configurationMasterHelper.IS_SMP_BASED_DASH) {
                dashBoardListViewHolder.pieChart.setUsePercentValues(true);
                dashBoardListViewHolder.pieChart.getDescription().setEnabled(false);
                dashBoardListViewHolder.pieChart.setExtraOffsets(0, 0, 0, 0);

                dashBoardListViewHolder.pieChart.setDragDecelerationFrictionCoef(0.95f);

                dashBoardListViewHolder.pieChart.setDrawHoleEnabled(true);

                dashBoardListViewHolder.pieChart.setTransparentCircleColor(Color.TRANSPARENT);
                dashBoardListViewHolder.pieChart.setTransparentCircleAlpha(110);

                dashBoardListViewHolder.pieChart.setDrawCenterText(false);

                // enable rotation of the chart by touch
                dashBoardListViewHolder.pieChart.setRotationEnabled(false);
                dashBoardListViewHolder.pieChart.setHighlightPerTapEnabled(true);

                dashBoardListViewHolder.pieChart.animateXY(1400, 1400, Easing.EasingOption.EaseInOutQuad, Easing.EasingOption.EaseInOutQuad);


                Legend l = dashBoardListViewHolder.pieChart.getLegend();
                l.setVerticalAlignment(Legend.LegendVerticalAlignment.TOP);
                l.setHorizontalAlignment(Legend.LegendHorizontalAlignment.RIGHT);
                l.setOrientation(Legend.LegendOrientation.VERTICAL);
                l.setDrawInside(false);
                l.setEnabled(false);

                //if (isSemiCircleChartRequired) {
                setOffset(dashBoardListViewHolder.pieChart);
                dashBoardListViewHolder.pieChart.setHoleColor(Color.TRANSPARENT);
                dashBoardListViewHolder.pieChart.setHoleRadius(50f);
                dashBoardListViewHolder.pieChart.setTransparentCircleRadius(28f);
                dashBoardListViewHolder.pieChart.setMaxAngle(180f); // HALF CHART
                dashBoardListViewHolder.pieChart.setRotationAngle(180f);
                // entry label styling
                dashBoardListViewHolder.pieChart.setEntryLabelColor(Color.TRANSPARENT);
                dashBoardListViewHolder.pieChart.setEntryLabelTextSize(0f);
                //}

                ArrayList<PieEntry> entries = new ArrayList<PieEntry>();

                double balanceValue = SDUtil.convertToInt(dashboardData.getKpiTarget()) - SDUtil.convertToInt(dashboardData.getKpiAcheived());
                entries.add(new PieEntry(SDUtil.convertToFloat(dashboardData.getKpiAcheived())));
                entries.add(new PieEntry(balanceValue >= 0 ? SDUtil.convertToFloat(balanceValue + "") : 0));

                PieDataSet dataSet = new PieDataSet(entries, "");

                dataSet.setSliceSpace(0f);
                dataSet.setSelectionShift(5f);

                // add a lot of colors

                ArrayList<Integer> colors = new ArrayList<>();

                colors.add(ContextCompat.getColor(mContext, R.color.colorPrimary));
                colors.add(ContextCompat.getColor(mContext, R.color.Orange));

                dataSet.setColors(colors);

                PieData data = new PieData(dataSet);
                data.setValueFormatter(new PercentFormatter());
                data.setValueTextSize(11f);
                data.setValueTextColor(Color.WHITE);
                data.setValueTextSize(0f);
                dashBoardListViewHolder.pieChart.setData(data);
            }


        } else {
            MinDashBoardListViewHolder dashBoardListViewHolder = (MinDashBoardListViewHolder) holder;

            dashBoardListViewHolder.factorNameTxtView.setText(dashboardData.getText());

            dashBoardListViewHolder.factorNameTxtView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    dashboardClickListener.onFactorNameClick(current_position);
                }
            });

            if (dashboardData.getSubDataCount() > 0) {
                dashBoardListViewHolder.skuWiseTxtView.setVisibility(View.VISIBLE);
                SpannableString str = new SpannableString(dashBoardListViewHolder.skuWiseTxtView
                        .getText().toString());
                str.setSpan(new UnderlineSpan(), 0, str.length(),
                        Spanned.SPAN_PARAGRAPH);
                str.setSpan(new ForegroundColorSpan(ContextCompat.getColor(mContext, R.color.Black)), 0,
                        str.length(), 0);
                dashBoardListViewHolder.skuWiseTxtView.setText(str);
                dashBoardListViewHolder.skuWiseTxtView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        dashboardClickListener.onSkuWiseClick(current_position);
                    }
                });
            } else {
                dashBoardListViewHolder.skuWiseTxtView.setVisibility(View.GONE);
            }

            if (dashboardData.getFlex1() == 1) {
                dashBoardListViewHolder.acheivedTxtView.setText(SDUtil.getWholeNumber(dashboardData.getKpiAcheived()));
            }else {
                String strKpiAchieved = getFormattedString(SDUtil.convertToDouble(dashboardData.getKpiAcheived()));
                dashBoardListViewHolder.acheivedTxtView.setText(strKpiAchieved);
            }
        }


    }

    private String getFormattedString(double value) {
        return SDUtil.format(value,
                configurationMasterHelper.VALUE_PRECISION_COUNT,
                configurationMasterHelper.VALUE_COMMA_COUNT, configurationMasterHelper.IS_DOT_FOR_GROUP);
    }


    private void setOffset(PieChart mChart) {
        Display display = ((Activity) mContext).getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int height = size.y;

        int offset = (int) (height * 0.20); /* percent to move */

        ConstraintLayout.LayoutParams rlParams =
                (ConstraintLayout.LayoutParams) mChart.getLayoutParams();
        rlParams.setMargins(0, 10, 0, -offset);
        mChart.setLayoutParams(rlParams);
    }


    @Override
    public int getItemViewType(int position) {
        if (configurationMasterHelper.IS_SWITCH_WITH_OUT_TGT
                && (configurationMasterHelper.SELLER_KPI_CODES.contains(dashboardList.get(position).getCode()) ||
                SDUtil.convertToInt(dashboardList.get(position).getKpiTarget()) == 0))
            return 1;
        else
            return 0;
    }

    @Override
    public int getItemCount() {
        return dashboardList.size();
    }

    public class MinDashBoardListViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.factorName_dashboard_tv)
        TextView factorNameTxtView;

        @BindView(R.id.achived_title)
        TextView achived_title;

        @BindView(R.id.acheived_dashboard_tv)
        TextView acheivedTxtView;

        @BindView(R.id.achievedGroup)
        Group achievedGroup;

        @BindView(R.id.tv_skuwise)
        TextView skuWiseTxtView;


        public MinDashBoardListViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);

            if (!configurationMasterHelper.SHOW_ACHIEVED_DASH) {
                achievedGroup.setVisibility(View.GONE);
            } else {
                achievedGroup.setVisibility(View.VISIBLE);
                if (labelsMap.containsKey(achived_title.getTag().toString()))
                    achived_title.setText(labelsMap.get(achived_title.getTag().toString()));
            }

        }
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

        @BindView(R.id.incentiveGroup)
        Group incentiveGroup;

        public DashBoardListViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);

            if (!configurationMasterHelper.SHOW_INDEX_DASH) {
                indexTxtView.setVisibility(View.GONE);
            }

            if (!configurationMasterHelper.SHOW_TARGET_DASH) {
                targetGroup.setVisibility(View.GONE);
            } else {
                targetGroup.setVisibility(View.VISIBLE);
                if (labelsMap.containsKey(target_title.getTag().toString())) {
                    target_title.setText(labelsMap.get(target_title.getTag().toString()));
                }
            }

            if (!configurationMasterHelper.SHOW_ACHIEVED_DASH) {
                achievedGroup.setVisibility(View.GONE);
            } else {
                achievedGroup.setVisibility(View.VISIBLE);
                if (labelsMap.containsKey(achived_title.getTag().toString()))
                    achived_title.setText(labelsMap.get(achived_title.getTag().toString()));
            }

            if (!configurationMasterHelper.SHOW_INCENTIVE_DASH) {
                incentiveGroup.setVisibility(View.GONE);
            } else {
                incentiveGroup.setVisibility(View.VISIBLE);
                if (labelsMap.containsKey(incentive_title.getTag().toString()))
                    incentive_title.setText(labelsMap.get(incentive_title.getTag().toString()));
            }

            if (!configurationMasterHelper.SHOW_FLEX_DASH) {
                flexGroup.setVisibility(View.GONE);
            } else {
                flexGroup.setVisibility(View.VISIBLE);
                if (labelsMap.containsKey(flex_title.getTag().toString()))
                    flex_title.setText(labelsMap.get(flex_title.getTag().toString()));
            }

            if (!configurationMasterHelper.SHOW_BALANCE_DASH) {
                balanceGroup.setVisibility(View.GONE);
            } else {
                balanceGroup.setVisibility(View.VISIBLE);
                if (labelsMap.containsKey(balance_title.getTag().toString()))
                    balance_title.setText(labelsMap.get(balance_title.getTag().toString()));
            }


            if (!configurationMasterHelper.SHOW_SCORE_DASH) {
                scoreGroup.setVisibility(View.GONE);
            } else {
                scoreGroup.setVisibility(View.VISIBLE);
                if (labelsMap.containsKey(score_title.getTag().toString()))
                    score_title.setText(labelsMap.get(score_title.getTag().toString()));
            }

            if (configurationMasterHelper.IS_SMP_BASED_DASH) {
                pieChart.setVisibility(View.GONE);
            }

        }

    }
}
