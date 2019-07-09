package com.ivy.cpg.view.dashboard.olddashboard;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.text.style.UnderlineSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.PercentFormatter;
import com.ivy.cpg.view.dashboard.DashBoardBO;
import com.ivy.cpg.view.dashboard.DashBoardHelper;
import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.commons.SDUtil;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.provider.ConfigurationMasterHelper;
import com.ivy.sd.png.util.Commons;

import java.util.ArrayList;
import java.util.List;

public class DashBoardListViewAdapter extends RecyclerView.Adapter<DashBoardListViewAdapter.ViewHolder> {
    private final List<DashBoardBO> dashboardList;
    private static final String MONTH_TYPE = "MONTH";
    private static final String YEAR_TYPE = "YEAR";
    private static final String DAY_TYPE = "DAY";
    private static final String MONTH_TAG = "incentive_month";
    private static final String YEAR_TAG = "incentive_year";

    private static final String ACH_MONTH_TAG = "achieved_month";
    private static final String ACH_YEAR_TAG = "achieved_year";

    private static final String TGT_MONTH_TAG = "target_month";
    private static final String TGT_YEAR_TAG = "target_year";
    private String type;
    private String retailerId;
    private String monthName;
    View view;
    Context context;
    private BusinessModel bmodel;
    private DashBoardHelper dashBoardHelper;

    public DashBoardListViewAdapter(BusinessModel bmodel, List<DashBoardBO> dashboardList, String type, String retailerId, String monthName) {
        this.dashboardList = dashboardList;
        this.type = type;
        this.retailerId = retailerId;
        this.monthName = monthName;
        this.bmodel = bmodel;
    }

    @Override
    public DashBoardListViewAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.seller_dashboard_row_layout, parent, false);
        context = parent.getContext();
        dashBoardHelper = DashBoardHelper.getInstance(context);
        return new DashBoardListViewAdapter.ViewHolder(view);
    }


    @Override
    public void onBindViewHolder(final DashBoardListViewAdapter.ViewHolder holder, int position) {
        final DashBoardBO dashboardData = dashboardList.get(position);
        if (!bmodel.configurationMasterHelper.SHOW_INCENTIVE_DASH && bmodel.configurationMasterHelper.SHOW_SCORE_DASH) {
            holder.incentive.setVisibility(View.GONE);
            holder.incentiveTitle.setVisibility(View.GONE);
        } else {
            holder.incentive.setVisibility(View.VISIBLE);
            holder.incentiveTitle.setVisibility(View.VISIBLE);
        }

        holder.dashboardDataObj = dashboardData;
        //typefaces

        holder.factorName.setTypeface(bmodel.configurationMasterHelper.getFontBaloobhai(ConfigurationMasterHelper.FontType.REGULAR));
        holder.target.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
        holder.acheived.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
        holder.balance.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
        holder.index.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
        holder.incentive.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
        holder.score.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
        holder.incentiveTitle.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
        holder.scoreTitle.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
        holder.targetTitle.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
        holder.acheivedTitle.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
        holder.balanceTitle.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
        holder.tvSkuWise.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));


        holder.factorName.setText(dashboardData.getText());

        if (bmodel.configurationMasterHelper.SHOW_LINK_DASH_SKUTGT
                && ("MONTH".equalsIgnoreCase(dashboardData.getType()) || "DAY"
                .equalsIgnoreCase(dashboardData.getType()))
                && dashboardData.getRouteID() == 0
                && dashboardData.getIsFlip() == 0
                && ("SV".equalsIgnoreCase(dashboardData.getCode()) || "VOL".equalsIgnoreCase(dashboardData
                .getCode()) || "SVSKU".equalsIgnoreCase(dashboardData.getCode()))) {
            holder.tvSkuWise.setVisibility(View.VISIBLE);
            holder.verticalSkuWise.setVisibility(View.VISIBLE);
            SpannableString str = new SpannableString(holder.tvSkuWise
                    .getText().toString());
            str.setSpan(new UnderlineSpan(), 0, str.length(),
                    Spanned.SPAN_PARAGRAPH);
            str.setSpan(new ForegroundColorSpan(ContextCompat.getColor(context, R.color.half_Black)), 0,
                    str.length(), 0);
            holder.tvSkuWise.setText(str);
        } else {
            holder.tvSkuWise.setVisibility(View.GONE);
            holder.verticalSkuWise.setVisibility(View.GONE);
        }


        holder.tvSkuWise.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                try {
                    if (bmodel.configurationMasterHelper.SHOW_LINK_DASH_SKUTGT
                            && (holder.dashboardDataObj.getType()
                            .equalsIgnoreCase(MONTH_TYPE) || holder.dashboardDataObj
                            .getType().equalsIgnoreCase(DAY_TYPE) || holder.dashboardDataObj.getType().equalsIgnoreCase(YEAR_TYPE))
                            && holder.dashboardDataObj.getRouteID() == 0
                            && holder.dashboardDataObj.getIsFlip() == 0
                            && ("SV".equalsIgnoreCase(holder.dashboardDataObj.getCode()) || "VOL".equalsIgnoreCase(holder.dashboardDataObj
                            .getCode()) || "SVSKU".equalsIgnoreCase(holder.dashboardDataObj.getCode()))) {
                        Intent i = new Intent(context,
                                SKUWiseTargetActivity.class);
                        i.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                        i.putExtra("screentitle",
                                bmodel.getMenuName("MENU_SKUWISESTGT"));
                        i.putExtra("screentitlebk",
                                ((AppCompatActivity) context).getSupportActionBar().getTitle());
                        i.putExtra("from", "4");
                        i.putExtra("flex1", holder.dashboardDataObj.getFlex1());
                        i.putExtra("rid", retailerId);
                        i.putExtra("type", holder.dashboardDataObj.getType());
                        i.putExtra("code",
                                holder.dashboardDataObj.getCode());
                        i.putExtra("pid",
                                holder.dashboardDataObj.getPId());
                        i.putExtra("isFromDash", true);
                        if (monthName != null)
                            i.putExtra("month_name", monthName);

                        context.startActivity(i);
                    } else {
                        holder.factorName.setClickable(false);
                    }
                } catch (Exception e) {
                    Commons.printException(e + "");
                }
            }
        });

        if (dashboardData.getFlex1() == 1) {
            if ("PDC".equalsIgnoreCase(dashboardData.getCode())) {
                //after decimal point value
                String dec_target;
                String dec_ach;
                String dec_balance;
                String dec_inc;
                //before decimal point value
                String target;
                String ach;
                String balance;
                String inc;

                dec_target = appendZero(String.valueOf(dashboardData.getTarget()).substring(String.valueOf(dashboardData.getTarget()).indexOf(".")).substring(1));
                target = dashBoardHelper.getWhole(dashboardData.getTarget() + "");

                dec_ach = appendZero(String.valueOf(dashboardData.getAcheived()).substring(String.valueOf(dashboardData.getAcheived()).indexOf(".")).substring(1));
                ach = dashBoardHelper.getWhole(dashboardData.getAcheived() + "");

                dec_balance = appendZero(String.valueOf(dashboardData.getTarget() - dashboardData.getAcheived()).substring
                        (String.valueOf(dashboardData.getTarget() - dashboardData.getAcheived()).indexOf(".")).substring(1));
                balance = dashBoardHelper.getWhole((dashboardData.getTarget() - dashboardData.getAcheived()) + "");

                dec_inc = appendZero(String.valueOf(dashboardData.getIncentive()).substring(String.valueOf(dashboardData.getIncentive()).indexOf(".")).substring(1));
                inc = dashBoardHelper.getWhole(dashboardData.getIncentive() + "");

                if (SDUtil.convertToInt(dec_target) >= 25)
                    holder.target.setText(dashBoardHelper.getWhole(Math.ceil(dashboardData.getTarget()) + ""));
                else
                    holder.target.setText(target);
                if (SDUtil.convertToInt(dec_ach) >= 25)
                    holder.acheived.setText(dashBoardHelper.getWhole(Math.ceil(dashboardData.getAcheived()) + ""));
                else
                    holder.acheived.setText(ach);
                if (SDUtil.convertToInt(dec_balance) >= 25)
                    holder.balance.setText(dashBoardHelper.getWhole(Math.ceil(dashboardData.getTarget() - dashboardData.getAcheived()) + ""));
                else
                    holder.balance.setText(balance);
                if (SDUtil.convertToInt(dec_inc) >= 25)
                    holder.incentive.setText(dashBoardHelper.getWhole(Math.ceil(dashboardData.getIncentive()) + ""));
                else
                    holder.incentive.setText(inc);
            } else {
                holder.target.setText(SDUtil.format(dashboardData.getTarget(), 0, bmodel.configurationMasterHelper.VALUE_COMMA_COUNT));
                holder.acheived.setText(SDUtil.format(dashboardData.getAcheived(), 0, bmodel.configurationMasterHelper.VALUE_COMMA_COUNT));
                holder.balance.setText(SDUtil.format(dashboardData.getTarget() - dashboardData.getAcheived(), 0, bmodel.configurationMasterHelper.VALUE_COMMA_COUNT));
                holder.incentive.setText(SDUtil.format(dashboardData.getIncentive(), 0, bmodel.configurationMasterHelper.VALUE_COMMA_COUNT));
            }

            String strCalcPercentage = bmodel.formatPercent(dashboardData.getCalculatedPercentage()) + "%";
            holder.index.setText(strCalcPercentage);

        } else {
            if ("PDC".equalsIgnoreCase(dashboardData.getCode())) {
                //after decimal point value
                String dec_target;
                String dec_ach;
                String dec_balance;
                String dec_inc;
                //  before decimal point value
                String target;
                String ach;
                String balance;
                String inc;

                dec_target = appendZero(String.valueOf(dashboardData.getTarget()).substring(String.valueOf(dashboardData.getTarget()).indexOf(".")).substring(1));
                target = dashBoardHelper.getWhole(dashboardData.getTarget() + "");

                dec_ach = appendZero(String.valueOf(dashboardData.getAcheived()).substring(String.valueOf(dashboardData.getAcheived()).indexOf(".")).substring(1));
                ach = dashBoardHelper.getWhole(dashboardData.getAcheived() + "");

                dec_balance = appendZero(String.valueOf(dashboardData.getTarget() - dashboardData.getAcheived()).substring
                        (String.valueOf(dashboardData.getTarget() - dashboardData.getAcheived()).indexOf(".")).substring(1));
                balance = dashBoardHelper.getWhole((dashboardData.getTarget() - dashboardData.getAcheived()) + "");

                dec_inc = appendZero(String.valueOf(dashboardData.getIncentive()).substring(String.valueOf(dashboardData.getIncentive()).indexOf(".")).substring(1));
                inc = dashBoardHelper.getWhole(dashboardData.getIncentive() + "");

                if (SDUtil.convertToInt(dec_target) >= 25)
                    holder.target.setText(dashBoardHelper.getWhole(Math.ceil(dashboardData.getTarget()) + ""));
                else
                    holder.target.setText(target);
                if (SDUtil.convertToInt(dec_ach) >= 25)
                    holder.acheived.setText(dashBoardHelper.getWhole(Math.ceil(dashboardData.getAcheived()) + ""));
                else
                    holder.acheived.setText(ach);
                if (SDUtil.convertToInt(dec_balance) >= 25)
                    holder.balance.setText(dashBoardHelper.getWhole(Math.ceil(dashboardData.getTarget() - dashboardData.getAcheived()) + ""));
                else
                    holder.balance.setText(balance);
                if (SDUtil.convertToInt(dec_inc) >= 25)
                    holder.incentive.setText(dashBoardHelper.getWhole(Math.ceil(dashboardData.getIncentive()) + ""));
                else
                    holder.incentive.setText(inc);
            } else {
                holder.target.setText(bmodel.formatValue(dashboardData.getTarget()));
                holder.acheived.setText(bmodel.formatValue(dashboardData.getAcheived()));
                holder.balance.setText(bmodel.formatValue(dashboardData.getTarget() - dashboardData.getAcheived()));
                String strIncentive = bmodel.formatValue(dashboardData.getIncentive()) + "";
                holder.incentive.setText(strIncentive);
            }

            String strCalcPercentage = bmodel.formatPercent(dashboardData.getCalculatedPercentage()) + "%";
            holder.index.setText(strCalcPercentage);
        }

        ArrayList<PieEntry> entries = new ArrayList<>();

        entries.add(new PieEntry(dashboardData.getConvAcheivedPercentage()));
        entries.add(new PieEntry(dashboardData.getConvTargetPercentage()));

        PieDataSet dataSet = new PieDataSet(entries, "");

        dataSet.setSliceSpace(3f);
        dataSet.setSelectionShift(5f);

        // add a lot of colors

        ArrayList<Integer> colors = new ArrayList<>();

        colors.add(ContextCompat.getColor(context, R.color.colorPrimary));
        colors.add(ContextCompat.getColor(context, R.color.Orange));

        dataSet.setColors(colors);

        PieData data = new PieData(dataSet);
        data.setValueFormatter(new PercentFormatter());
        data.setValueTextSize(11f);
        data.setValueTextColor(Color.WHITE);
        data.setValueTextSize(0f);
        holder.mChart.setData(data);

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

        TextView factorName;
        TextView target, targetTitle;
        TextView acheived, acheivedTitle;
        TextView index;
        TextView incentive, incentiveTitle;
        TextView score, scoreTitle;
        TextView balance, balanceTitle;
        TextView tvSkuWise;
        PieChart mChart;
        DashBoardBO dashboardDataObj;
        View rowDotBlue, rowDotOrange, rowDotGreen, verticalSeparatorTarget, verticalSeparatorBalance, verticalSkuWise;

        public ViewHolder(View row) {
            super(row);
            factorName = row
                    .findViewById(R.id.factorName_dashboard_tv);
            target = row
                    .findViewById(R.id.target_dashboard_tv);
            acheived = row
                    .findViewById(R.id.acheived_dashboard_tv);
            balance = row
                    .findViewById(R.id.balance_dashboard_tv);
            index = row
                    .findViewById(R.id.index_dashboard_tv);
            score = row
                    .findViewById(R.id.score_dashboard_tv);
            incentive = row
                    .findViewById(R.id.initiative_dashboard_tv);

            mChart = row
                    .findViewById(R.id.pieChart);

            targetTitle = row
                    .findViewById(R.id.target_title);
            acheivedTitle = row
                    .findViewById(R.id.achived_title);
            incentiveTitle = row
                    .findViewById(R.id.incentive_title);

            balanceTitle = row
                    .findViewById(R.id.balance_title);
            scoreTitle = row
                    .findViewById(R.id.score_title);

            rowDotBlue = row
                    .findViewById(R.id.row_dot_blue);
            rowDotOrange = row
                    .findViewById(R.id.row_dot_orange);
            rowDotGreen = row
                    .findViewById(R.id.row_dot_green);
            verticalSeparatorTarget = row
                    .findViewById(R.id.verticalSeparatorTarget);
            verticalSeparatorBalance = row
                    .findViewById(R.id.verticalSeparatorBalance);
            tvSkuWise = row
                    .findViewById(R.id.tv_skuwise);
            verticalSkuWise = row
                    .findViewById(R.id.verticalSeparatorSkuWise);

            if (!bmodel.configurationMasterHelper.SHOW_INDEX_DASH) {
                index.setVisibility(View.GONE);
            }
            if (!bmodel.configurationMasterHelper.SHOW_TARGET_DASH) {
                target.setVisibility(View.GONE);
                targetTitle.setVisibility(View.GONE);
                verticalSeparatorTarget.setVisibility(View.GONE);
                rowDotGreen.setVisibility(View.GONE);
            } else {
                switch (type) {
                    case DAY_TYPE:
                        try {
                            if (bmodel.labelsMasterHelper.applyLabels(view.findViewById(
                                    R.id.target_title).getTag()) != null)
                                ((TextView) view.findViewById(R.id.target_title))
                                        .setText(bmodel.labelsMasterHelper.
                                                applyLabels(view.findViewById(R.id.target_title)
                                                        .getTag()));
                            else
                                ((TextView) view.findViewById(R.id.target_title))
                                        .setText(context.getResources().getString(R.string.target));
                        } catch (Exception e) {
                            Commons.printException(e + "");
                        }
                        break;
                    case MONTH_TYPE:
                        try {
                            if (bmodel.labelsMasterHelper.applyLabels(TGT_MONTH_TAG) != null)
                                ((TextView) view.findViewById(R.id.target_title))
                                        .setText(bmodel.labelsMasterHelper
                                                .applyLabels(TGT_MONTH_TAG));
                            else
                                ((TextView) view.findViewById(R.id.target_title))
                                        .setText(context.getResources().getString(R.string.target));
                        } catch (Exception e) {
                            Commons.printException(e + "");
                        }
                        break;
                    case YEAR_TYPE:
                        try {
                            if (bmodel.labelsMasterHelper.applyLabels(TGT_YEAR_TAG) != null)
                                ((TextView) view.findViewById(R.id.target_title))
                                        .setText(bmodel.labelsMasterHelper
                                                .applyLabels(TGT_YEAR_TAG));
                            else
                                ((TextView) view.findViewById(R.id.target_title))
                                        .setText(context.getResources().getString(R.string.target));
                        } catch (Exception e) {
                            Commons.printException(e + "");
                        }
                        break;
                    default:
                        try {
                            if (bmodel.labelsMasterHelper.applyLabels(view.findViewById(
                                    R.id.target_title).getTag()) != null)
                                ((TextView) view.findViewById(R.id.target_title))
                                        .setText(bmodel.labelsMasterHelper.
                                                applyLabels(view.findViewById(R.id.target_title)
                                                        .getTag()));
                            else
                                ((TextView) view.findViewById(R.id.target_title))
                                        .setText(context.getResources().getString(R.string.target));
                        } catch (Exception e) {
                            Commons.printException(e + "");
                        }
                        break;
                }

            }
            if (!bmodel.configurationMasterHelper.SHOW_ACHIEVED_DASH) {
                acheived.setVisibility(View.GONE);
                acheivedTitle.setVisibility(View.GONE);
                rowDotBlue.setVisibility(View.GONE);
            } else {
                switch (type) {
                    case DAY_TYPE:
                        try {
                            if (bmodel.labelsMasterHelper.applyLabels(view.findViewById(
                                    R.id.achived_title).getTag()) != null)
                                ((TextView) view.findViewById(R.id.achived_title))
                                        .setText(bmodel.labelsMasterHelper.
                                                applyLabels(view.findViewById(R.id.achived_title)
                                                        .getTag()));
                            else
                                ((TextView) view.findViewById(R.id.achived_title))
                                        .setText(context.getResources().getString(R.string.achieved));
                        } catch (Exception e) {
                            Commons.printException(e + "");
                        }
                        break;
                    case MONTH_TYPE:
                        try {
                            if (bmodel.labelsMasterHelper.applyLabels(ACH_MONTH_TAG) != null)
                                ((TextView) view.findViewById(R.id.achived_title))
                                        .setText(bmodel.labelsMasterHelper
                                                .applyLabels(ACH_MONTH_TAG));
                            else
                                ((TextView) view.findViewById(R.id.achived_title))
                                        .setText(context.getResources().getString(R.string.achieved));
                        } catch (Exception e) {
                            Commons.printException(e + "");
                        }
                        break;
                    case YEAR_TYPE:
                        try {
                            if (bmodel.labelsMasterHelper.applyLabels(ACH_YEAR_TAG) != null)
                                ((TextView) view.findViewById(R.id.achived_title))
                                        .setText(bmodel.labelsMasterHelper
                                                .applyLabels(ACH_YEAR_TAG));
                            else
                                ((TextView) view.findViewById(R.id.achived_title))
                                        .setText(context.getResources().getString(R.string.achieved));
                        } catch (Exception e) {
                            Commons.printException(e + "");
                        }
                        break;
                    default:
                        try {
                            if (bmodel.labelsMasterHelper.applyLabels(view.findViewById(
                                    R.id.achived_title).getTag()) != null)
                                ((TextView) view.findViewById(R.id.achived_title))
                                        .setText(bmodel.labelsMasterHelper.
                                                applyLabels(view.findViewById(R.id.achived_title)
                                                        .getTag()));
                            else
                                ((TextView) view.findViewById(R.id.achived_title))
                                        .setText(context.getResources().getString(R.string.achieved));
                        } catch (Exception e) {
                            Commons.printException(e + "");
                        }
                        break;
                }

            }
            if (!bmodel.configurationMasterHelper.SHOW_BALANCE_DASH) {
                balance.setVisibility(View.GONE);
                balanceTitle.setVisibility(View.GONE);
                verticalSeparatorBalance.setVisibility(View.GONE);
                rowDotOrange.setVisibility(View.GONE);
            } else {
                try {
                    if (bmodel.labelsMasterHelper.applyLabels(view.findViewById(
                            R.id.balance_title).getTag()) != null)
                        ((TextView) view.findViewById(R.id.balance_title))
                                .setText(bmodel.labelsMasterHelper
                                        .applyLabels(view.findViewById(R.id.balance_title)
                                                .getTag()));
                } catch (Exception e) {
                    Commons.printException(e + "");
                }

            }
            if (!bmodel.configurationMasterHelper.SHOW_INCENTIVE_DASH) {
                incentive.setVisibility(View.GONE);
                incentiveTitle.setVisibility(View.GONE);
            } else {
                switch (type) {
                    case DAY_TYPE:
                        try {
                            if (bmodel.labelsMasterHelper.applyLabels(view.findViewById(
                                    R.id.incentive_title).getTag()) != null)
                                ((TextView) view.findViewById(R.id.incentive_title))
                                        .setText(bmodel.labelsMasterHelper
                                                .applyLabels(view.findViewById(R.id.incentive_title)
                                                        .getTag()));
                            else
                                ((TextView) view.findViewById(R.id.incentive_title))
                                        .setText(context.getResources().getString(R.string.incentive));
                        } catch (Exception e) {
                            Commons.printException(e + "");
                        }
                        break;
                    case MONTH_TYPE:
                        try {
                            if (bmodel.labelsMasterHelper.applyLabels(MONTH_TAG) != null)
                                ((TextView) view.findViewById(R.id.incentive_title))
                                        .setText(bmodel.labelsMasterHelper
                                                .applyLabels(MONTH_TAG));
                            else
                                ((TextView) view.findViewById(R.id.incentive_title))
                                        .setText(context.getResources().getString(R.string.incentive));
                        } catch (Exception e) {
                            Commons.printException(e + "");
                        }
                        break;
                    case YEAR_TYPE:
                        try {
                            if (bmodel.labelsMasterHelper.applyLabels(YEAR_TAG) != null)
                                ((TextView) view.findViewById(R.id.incentive_title))
                                        .setText(bmodel.labelsMasterHelper
                                                .applyLabels(YEAR_TAG));
                            else
                                ((TextView) view.findViewById(R.id.incentive_title))
                                        .setText(context.getResources().getString(R.string.incentive));
                        } catch (Exception e) {
                            Commons.printException(e + "");
                        }
                        break;
                    default:
                        try {
                            if (bmodel.labelsMasterHelper.applyLabels(view.findViewById(
                                    R.id.incentive_title).getTag()) != null)
                                ((TextView) view.findViewById(R.id.incentive_title))
                                        .setText(bmodel.labelsMasterHelper
                                                .applyLabels(view.findViewById(R.id.incentive_title)
                                                        .getTag()));
                            else
                                ((TextView) view.findViewById(R.id.incentive_title))
                                        .setText(context.getResources().getString(R.string.incentive));
                        } catch (Exception e) {
                            Commons.printException(e + "");
                        }
                        break;
                }


            }

            if (!bmodel.configurationMasterHelper.SHOW_FLEX_DASH) {
                row.findViewById(R.id.flex_dashboard_tv).setVisibility(View.GONE);
                row.findViewById(R.id.flex_title).setVisibility(View.GONE);
                row.findViewById(R.id.verticalSeparatorFlex).setVisibility(View.GONE);
                row.findViewById(R.id.row_dot_orange1).setVisibility(View.GONE);
            }
            //common row layout used - old dashboard score not available
            score.setVisibility(View.GONE);
            scoreTitle.setVisibility(View.GONE);

            mChart.setUsePercentValues(true);
            mChart.getDescription().setEnabled(false);
            mChart.setExtraOffsets(0, 0, 0, 0);

            mChart.setDragDecelerationFrictionCoef(0.95f);

            mChart.setDrawHoleEnabled(false);

            mChart.setTransparentCircleColor(Color.WHITE);
            mChart.setTransparentCircleAlpha(110);

            mChart.setDrawCenterText(false);

            // enable rotation of the chart by touch
            mChart.setRotationEnabled(false);
            mChart.setHighlightPerTapEnabled(true);

            mChart.animateXY(1400, 1400, Easing.EasingOption.EaseInOutQuad, Easing.EasingOption.EaseInOutQuad);


            Legend l = mChart.getLegend();
            l.setVerticalAlignment(Legend.LegendVerticalAlignment.TOP);
            l.setHorizontalAlignment(Legend.LegendHorizontalAlignment.RIGHT);
            l.setOrientation(Legend.LegendOrientation.VERTICAL);
            l.setDrawInside(false);
            l.setEnabled(false);

        }
    }

    private String appendZero(String value) {
        if (value.length() == 1)
            value = value + "0";
        return value;
    }
}
