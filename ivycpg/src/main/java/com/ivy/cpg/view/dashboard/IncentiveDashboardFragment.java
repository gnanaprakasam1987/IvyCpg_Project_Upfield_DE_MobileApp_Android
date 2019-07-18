package com.ivy.cpg.view.dashboard;

import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import androidx.fragment.app.FragmentActivity;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.commons.IvyBaseFragment;
import com.ivy.sd.png.commons.SDUtil;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.provider.ConfigurationMasterHelper;

import java.util.ArrayList;
import java.util.HashMap;


public class IncentiveDashboardFragment extends IvyBaseFragment {
    private BusinessModel bmodel;
    HashMap<String, ArrayList<IncentiveDashboardBO>> incentiveHashMap = new HashMap<>();
    TextView tvFixedTotalPayoutEarned, tvFixedMaxpossiblePayout, tvTopUpTotalPayoutEarned, tvTopUpMaxpossiblePayout, tvTotalPayoutEarned, tvMaxpossiblePayout, tvRegularIncentive, tvTopupIncentive, tvRegularIncentiveDetails, tvTopupIncentiveDetail;
    CardView cvFirst, cvSecond;
    ImageView regularIncentiveImageView, regularIncentiveDetailsImageView, topupIncentiveImageview, topupIncentiveDetailImageView;
    ArrayList<IncentiveDashboardBO> fixedIncentiveList = new ArrayList<>();
    ArrayList<IncentiveDashboardBO> topUpIncentiveList = new ArrayList<>();
    ArrayList<IncentiveDashboardDefinitionBO> regularIncentiveDetails = new ArrayList<>();
    ArrayList<IncentiveDashboardDefinitionBO> topupIncentiveDetails = new ArrayList<>();

    String strFixed = "R", strTopUP = "T";
    DashBoardHelper dashBoardHelper;

    @Override

    public void onAttach(Context context) {
        super.onAttach(context);
        bmodel = (BusinessModel) getActivity().getApplicationContext();
        bmodel.setContext(getActivity());
        dashBoardHelper = DashBoardHelper.getInstance(getActivity());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_incentive_dashboard, container, false);

        tvFixedTotalPayoutEarned = (TextView) view.findViewById(R.id.tv_fixed_totalPayoutEarned);
        tvFixedMaxpossiblePayout = (TextView) view.findViewById(R.id.tv_fixed_max_possible_payout);
        tvTopUpTotalPayoutEarned = (TextView) view.findViewById(R.id.tv_topup_totalPayoutEarned);
        tvTopUpMaxpossiblePayout = (TextView) view.findViewById(R.id.tv_topup_max_possible_payout);
        tvTotalPayoutEarned = (TextView) view.findViewById(R.id.tv_totalPayoutEarned);
        tvMaxpossiblePayout = (TextView) view.findViewById(R.id.tv_maxPossiblepayout);
        tvRegularIncentive = (TextView) view.findViewById(R.id.tv_textRegularIncentive);
        tvTopupIncentive = (TextView) view.findViewById(R.id.tv_TopupIncentive);
        tvRegularIncentiveDetails = (TextView) view.findViewById(R.id.regularIncentiveDetailsTextView);
        tvTopupIncentiveDetail = (TextView) view.findViewById(R.id.topupIncentiveDetailTextView);
        regularIncentiveImageView = (ImageView) view.findViewById(R.id.regularIncentiveImageView);
        regularIncentiveDetailsImageView = (ImageView) view.findViewById(R.id.regularIncentiveDetailsImageView);
        topupIncentiveImageview = (ImageView) view.findViewById(R.id.topupIncentiveImageview);
        topupIncentiveDetailImageView = (ImageView) view.findViewById(R.id.topupIncentiveDetailImageView);
        cvFirst = (CardView) view.findViewById(R.id.cv_first);
        cvSecond = (CardView) view.findViewById(R.id.cv_second);

// typeface
        tvFixedTotalPayoutEarned.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
        tvFixedMaxpossiblePayout.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
        tvTopUpTotalPayoutEarned.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
        tvTopUpMaxpossiblePayout.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
        tvTotalPayoutEarned.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
        tvMaxpossiblePayout.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
        tvRegularIncentive.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
        tvTopupIncentive.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
        tvTopupIncentiveDetail.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
        tvRegularIncentiveDetails.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));


        fixedIncentiveList = dashBoardHelper.downloadIncentiveList("R");
        topUpIncentiveList = dashBoardHelper.downloadIncentiveList("T");

        regularIncentiveDetails = dashBoardHelper.downloadIncentiveDetails("R");
        topupIncentiveDetails = dashBoardHelper.downloadIncentiveDetails("T");

        IncentiveType();

        regularIncentiveImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                regularIncentive();
            }
        });

        tvRegularIncentive.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                regularIncentive();
            }
        });

        regularIncentiveDetailsImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                regularIncentiveDetail();
            }
        });

        tvRegularIncentiveDetails.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                regularIncentiveDetail();
            }
        });

        tvTopupIncentive.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                topUpIncentive();
            }
        });
        topupIncentiveImageview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                topUpIncentive();
            }
        });

        topupIncentiveDetailImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                topUpIncentiveDetails();
            }
        });
        tvTopupIncentiveDetail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                topUpIncentiveDetails();
            }
        });


        setUpActionBar();
        setHasOptionsMenu(true);
        return view;
    }

    private void regularIncentive() {
        if (fixedIncentiveList != null && fixedIncentiveList.size() > 0)
            alert(fixedIncentiveList, strFixed);
        else
            bmodel.showAlert(getResources().getString(R.string.no_data_exists), 0);
    }

    private void regularIncentiveDetail() {
        if (regularIncentiveDetails != null && regularIncentiveDetails.size() > 0)
            alertDefination(regularIncentiveDetails, strFixed);
        else
            bmodel.showAlert(getResources().getString(R.string.no_data_exists), 0);
    }

    private void topUpIncentive() {
        if (topUpIncentiveList != null && topUpIncentiveList.size() > 0)
            alert(topUpIncentiveList, strTopUP);
        else
            bmodel.showAlert(getResources().getString(R.string.no_data_exists), 0);
    }

    private void topUpIncentiveDetails() {
        if (topupIncentiveDetails != null && topupIncentiveDetails.size() > 0)
            alertDefination(topupIncentiveDetails, strTopUP);
        else
            bmodel.showAlert(getResources().getString(R.string.no_data_exists), 0);
    }


    private void setUpActionBar() {
        ((AppCompatActivity) getActivity()).getSupportActionBar();
        ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayShowTitleEnabled(false);
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            ((AppCompatActivity) getActivity()).getSupportActionBar().setElevation(0);
        }

        if (bmodel.getMenuName("MENU_DASH_INC").endsWith(""))
            bmodel.configurationMasterHelper.downloadMainMenu();
        if (getArguments().getString("screentitle") == null)
            setScreenTitle(bmodel.getMenuName("MENU_DASH_INC"));
        else
            setScreenTitle(getArguments().getString("screentitle"));

        ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayShowHomeEnabled(true);

    }

    private void IncentiveType() {

        incentiveHashMap.clear();

        for (String s : dashBoardHelper.getIncentiveType()) {
            if (s.equalsIgnoreCase("R"))
                incentiveHashMap.put("R", fixedIncentiveList);
            if (s.equalsIgnoreCase("T"))
                incentiveHashMap.put("T", topUpIncentiveList);
        }

        for (String inctype : dashBoardHelper.getIncentiveType()) {
            if (inctype.equalsIgnoreCase("R")) {

                tvRegularIncentive.setText(R.string.regular_incentive);
                tvFixedTotalPayoutEarned.setText(getResources().getString(R.string.Rs) + " " + getSum(incentiveHashMap.get("R"), false));
                tvFixedMaxpossiblePayout.setText(getResources().getString(R.string.Rs) + " " + getSum(incentiveHashMap.get("R"), true));

            }
            if (inctype.equalsIgnoreCase("T")) {

                tvTopupIncentive.setText(R.string.top_up_incentive);
                tvTopUpTotalPayoutEarned.setText(getResources().getString(R.string.Rs) + " " + getSum(incentiveHashMap.get("T"), false));
                tvTopUpMaxpossiblePayout.setText(getResources().getString(R.string.Rs) + " " + getSum(incentiveHashMap.get("T"), true));

            }
        }

        int totalpayot = (int) (getSum(incentiveHashMap.get("R"), false) + getSum(incentiveHashMap.get("T"), false));
        tvTotalPayoutEarned.setText(getResources().getString(R.string.Rs) + " " + Integer.toString(totalpayot));

        int maxPayout = (int) (getSum(incentiveHashMap.get("R"), true) + getSum(incentiveHashMap.get("T"), true));
        tvMaxpossiblePayout.setText(getResources().getString(R.string.Rs) + " " + Integer.toString(maxPayout));

    }

    private double getSum(ArrayList<IncentiveDashboardBO> incentivelist, boolean isMax) {

        double sum = 0;
        HashMap<String, String> sumMap = new HashMap<>();

        if (incentivelist != null) {
            for (IncentiveDashboardBO incentiveDashboardBO : incentivelist) {
                if (isMax) {
                    if (SDUtil.convertToDouble(incentiveDashboardBO.getMaxpayout()) > 0)
                        sum += SDUtil.convertToDouble(incentiveDashboardBO.getMaxpayout());
                } else {
                    if (SDUtil.convertToDouble(incentiveDashboardBO.getPayout()) > 0)
                        sum += SDUtil.convertToDouble(incentiveDashboardBO.getPayout());
                }
            }
        }

        return sum;

    }

    public void alert(ArrayList<IncentiveDashboardBO> tempMIncentiveList, String strTitleType) {

        final AlertDialog.Builder dialog = new AlertDialog.Builder(getActivity());

        View customView = LayoutInflater.from(getActivity()).inflate(
                R.layout.incentivedashboard_alertdialog_layout, null, false);

        ListView listItems = (ListView) customView.findViewById(R.id.lv_items);
        TextView titleTextView = (TextView) customView.findViewById(R.id.title);
        ImageView closeImageView = (ImageView) customView.findViewById(R.id.img_close);

        titleTextView.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));

        if (strTitleType.equals("R")) {
            titleTextView.setText(R.string.regular_incentive_small);

        } else {
            titleTextView.setText(R.string.top_up_incentive_small);
        }

        CustomIncentiveAdapterDialog mAdapter = new CustomIncentiveAdapterDialog(tempMIncentiveList, getActivity());
        listItems.setAdapter(mAdapter);


        dialog.setView(customView);
        final AlertDialog alertDialog = dialog.show();

        closeImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                alertDialog.dismiss();
            }
        });

    }


    public void alertDefination(ArrayList<IncentiveDashboardDefinitionBO> tempMIncentiveList, String strTitleType) {

        final AlertDialog.Builder dialog = new AlertDialog.Builder(getActivity());
        // dialog.setContentView(R.layout.alert_list_radio);

        View customView = LayoutInflater.from(getActivity()).inflate(
                R.layout.dialog_incentive_dashboard_details, null, false);

        ListView listItems = (ListView) customView.findViewById(R.id.lv_items);

        TextView titleTextView = (TextView) customView.findViewById(R.id.title);
        ImageView closeImageView = (ImageView) customView.findViewById(R.id.img_close);

        titleTextView.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));

        if (strTitleType.equals("R")) {
            titleTextView.setText(R.string.regular_incentive_detail);

        } else {
            titleTextView.setText(R.string.top_up_incentive_detail);
        }

        MyAdapter adapter = new MyAdapter(tempMIncentiveList, getActivity());
        listItems.setAdapter(adapter);

        dialog.setView(customView);
        final AlertDialog alertDialog = dialog.show();
        closeImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                alertDialog.dismiss();
            }
        });

    }


    public class CustomIncentiveAdapterDialog extends BaseAdapter {

        private ArrayList<IncentiveDashboardBO> listData;
        ViewHolder holder;
        private LayoutInflater layoutInflater;

        public CustomIncentiveAdapterDialog(ArrayList<IncentiveDashboardBO> listData, FragmentActivity context) {
            this.listData = listData;
            layoutInflater = LayoutInflater.from(context);
        }

        @Override
        public int getCount() {
            return listData.size();
        }

        @Override
        public Object getItem(int position) {
            return listData.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        public View getView(int position, View convertView, ViewGroup parent) {

            if (convertView == null) {
                convertView = layoutInflater.inflate(R.layout.row_incentivedashboard_alertdialog, parent, false);
                holder = new ViewHolder();

                holder.tvFactor = (TextView) convertView.findViewById(R.id.tv_factor);
                holder.tvTarget = (TextView) convertView.findViewById(R.id.tv_target);
                holder.tvAchived = (TextView) convertView.findViewById(R.id.tv_achived);
                holder.tvAchper = (TextView) convertView.findViewById(R.id.tv_achper);
                holder.tvPayoutEarned = (TextView) convertView.findViewById(R.id.tv_payout_earned);
                holder.tvMaxPossible = (TextView) convertView.findViewById(R.id.tv_max_possible);

                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            holder.tvFactor.setText(listData.get(position).getFactor());
            holder.tvTarget.setText(listData.get(position).getTgt());
            holder.tvAchived.setText(listData.get(position).getAch());
            holder.tvAchper.setText(listData.get(position).getAchper());

            try {
                if (Double.parseDouble(listData.get(position).getAchper().replace("%", "")) >= 100) {
                    holder.tvAchper.setTextColor(getResources().getColor(R.color.colorPrimaryDarkGreen));
                } else {
                    holder.tvAchper.setTextColor(getResources().getColor(R.color.RED));
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }

            if (listData.get(position).getIsNewGroup() || listData.get(position).getInctype().equals("T")) {
                holder.tvPayoutEarned.setText(getResources().getString(R.string.Rs) + " " + listData.get(position).getPayout());
                holder.tvMaxPossible.setText(getResources().getString(R.string.Rs) + " " + listData.get(position).getMaxpayout());
                holder.tvMaxPossible.setVisibility(View.VISIBLE);
                holder.tvPayoutEarned.setVisibility(View.VISIBLE);
            } else {
                holder.tvMaxPossible.setVisibility(View.GONE);
                holder.tvPayoutEarned.setVisibility(View.GONE);
            }


            return convertView;
        }

        public class ViewHolder {
            TextView tvFactor, tvTarget, tvAchived, tvAchper, tvPayoutEarned, tvMaxPossible;

        }

    }

    public class MyAdapter extends BaseAdapter {

        private ArrayList<IncentiveDashboardDefinitionBO> listData;
        MyAdapter.ViewHolder holder;
        private LayoutInflater layoutInflater;

        public MyAdapter(ArrayList<IncentiveDashboardDefinitionBO> list, FragmentActivity mContext) {
            this.listData = list;
            layoutInflater = LayoutInflater.from(mContext);
        }

        public int getCount() {
            return listData.size();
        }

        public Object getItem(int position) {
            return listData.get(position);
        }

        public long getItemId(int position) {
            return position;
        }

        public View getView(int position, View convertView, ViewGroup parent) {

            if (convertView == null) {
                convertView = layoutInflater.inflate(R.layout.row_incentive_dashboard_details, parent, false);
                holder = new ViewHolder();

                holder.tvFactor = (TextView) convertView.findViewById(R.id.tv_factor);
                holder.tvSalesParameter = (TextView) convertView.findViewById(R.id.tv_sales_parameter);
                holder.tvMaxOpportunities = (TextView) convertView.findViewById(R.id.tv_max_opportunity);
                holder.tvAchper = (TextView) convertView.findViewById(R.id.tv_achper);

                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            holder.tvFactor.setText(listData.get(position).getFactor());
            holder.tvSalesParameter.setText(listData.get(position).getSalesParam());
            holder.tvAchper.setText(listData.get(position).getAchPercentage());
            holder.tvMaxOpportunities.setText(listData.get(position).getMaxOpportunity());

            if (listData.get(position).getIsNewGroup()) {
                holder.tvSalesParameter.setVisibility(View.VISIBLE);
            } else {
                holder.tvSalesParameter.setVisibility(View.INVISIBLE);
            }

            if (listData.get(position).getIsNewFactor()) {
                holder.tvFactor.setVisibility(View.VISIBLE);
            } else {
                holder.tvFactor.setVisibility(View.INVISIBLE);
            }

            if (listData.get(position).isNewPackage()) {
                holder.tvAchper.setVisibility(View.VISIBLE);
                holder.tvMaxOpportunities.setVisibility(View.VISIBLE);
            } else {
                holder.tvAchper.setVisibility(View.INVISIBLE);
                holder.tvMaxOpportunities.setVisibility(View.INVISIBLE);
            }

            return convertView;
        }


        public class ViewHolder {
            TextView tvFactor, tvSalesParameter, tvAchper, tvMaxOpportunities;

        }

    }
}






