package com.ivy.cpg.view.dashboard;

import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.commons.IvyBaseFragment;
import com.ivy.sd.png.commons.SDUtil;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.provider.ConfigurationMasterHelper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


public class IncentiveDashboardFragment extends IvyBaseFragment {
    private BusinessModel bmodel;
    HashMap<String, ArrayList<IncentiveDashboardBO>> incentiveHashMap = new HashMap<>();
    ArrayList<IncentiveDashboardBO> mIncentiveList = new ArrayList<>();
    TextView tvFixedTotalPayoutEarned, tvFixedMaxpossiblePayout, tvTopUpTotalPayoutEarned, tvTopUpMaxpossiblePayout, tvTotalPayoutEarned, tvMaxpossiblePayout, tvRegularIncentive, tvTopupIncentive;
    CardView cvFirst, cvSecond;
    ArrayList<IncentiveDashboardBO> fixedIncentiveList = new ArrayList<>();
    ArrayList<IncentiveDashboardBO> topUpIncentiveList = new ArrayList<>();


    String strFixed = "Fixed", strTopUP = "Top Up";

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        bmodel = (BusinessModel) getActivity().getApplicationContext();
        bmodel.setContext(getActivity());
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


        bmodel.dashBoardHelper.downloadIncentiveList();
        mIncentiveList = bmodel.dashBoardHelper.getIncentiveList();

        IncentiveType();

        cvFirst.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                aletrt(fixedIncentiveList, strFixed);

            }
        });
        cvSecond.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                aletrt(topUpIncentiveList, strTopUP);
            }
        });

        setUpActionBar();
        setHasOptionsMenu(true);
        return view;
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

//        if (!BusinessModel.dashHomeStatic)
        ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayShowHomeEnabled(true);

    }

    private void IncentiveType() {

        incentiveHashMap.clear();
        fixedIncentiveList.clear();
        topUpIncentiveList.clear();


        for (int i = 0; i < mIncentiveList.size(); i++) {

            if (mIncentiveList.get(i).getInctype().equalsIgnoreCase("Fixed"))
                fixedIncentiveList.add(mIncentiveList.get(i));
            if (mIncentiveList.get(i).getInctype().equalsIgnoreCase("Top Up"))
                topUpIncentiveList.add(mIncentiveList.get(i));
        }
        for (String s : bmodel.dashBoardHelper.getIncentiveType()) {
            if (s.equalsIgnoreCase("Fixed"))
                incentiveHashMap.put(s, fixedIncentiveList);
            if (s.equalsIgnoreCase("Top Up"))
                incentiveHashMap.put(s, topUpIncentiveList);
        }


        for (String inctype : bmodel.dashBoardHelper.getIncentiveType()) {
            if (inctype.equalsIgnoreCase("Fixed")) {

                tvRegularIncentive.setText(R.string.regular_incentive);
                tvFixedTotalPayoutEarned.setText(getResources().getString(R.string.Rs) + " " + getSum(incentiveHashMap.get("Fixed"), false));
                tvFixedMaxpossiblePayout.setText(getResources().getString(R.string.Rs) + " " + getSum(incentiveHashMap.get("Fixed"), true));

            }
            if (inctype.equalsIgnoreCase("Top Up")) {

                tvTopupIncentive.setText(R.string.top_up_incentive);
                tvTopUpTotalPayoutEarned.setText(getResources().getString(R.string.Rs) + " " + getSum(incentiveHashMap.get("Top Up"), false));
                tvTopUpMaxpossiblePayout.setText(getResources().getString(R.string.Rs) + " " + getSum(incentiveHashMap.get("Top Up"), true));

            }
        }

        int totalpayot = (int) (getSum(incentiveHashMap.get("Fixed"), false) + getSum(incentiveHashMap.get("Top Up"), false));
        tvTotalPayoutEarned.setText(getResources().getString(R.string.Rs) + " " + Integer.toString(totalpayot));

        int maxPayout = (int) (getSum(incentiveHashMap.get("Fixed"), true) + getSum(incentiveHashMap.get("Top Up"), true));
        tvMaxpossiblePayout.setText(getResources().getString(R.string.Rs) + " " + Integer.toString(maxPayout));


    }

    private double getSum(ArrayList<IncentiveDashboardBO> incentivelist, boolean isMax) {

        double sum = 0;
        HashMap<String, Double> sumMap = new HashMap<>();

        for (IncentiveDashboardBO incentiveDashboardBO : incentivelist) {
            if (isMax)
                sumMap.put(incentiveDashboardBO.getGroups(), SDUtil.convertToDouble(incentiveDashboardBO.getMaxpayout()));
            else
                sumMap.put(incentiveDashboardBO.getGroups(), SDUtil.convertToDouble(incentiveDashboardBO.getPayout()));
        }

        for (Map.Entry<String, Double> entry : sumMap.entrySet())
            sum += entry.getValue();

        return sum;

    }

    public void aletrt(ArrayList<IncentiveDashboardBO> tempMIncentiveList, String strTitleType) {

        AlertDialog.Builder dialog = new AlertDialog.Builder(getActivity());
        // dialog.setContentView(R.layout.alert_list_radio);

        TextView textView = new TextView(getActivity());
        textView.setTextSize(getResources().getDimensionPixelSize(R.dimen.font_nano_small));
        textView.setGravity(Gravity.CENTER);
        textView.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
        dialog.setCustomTitle(textView);
        if (strTitleType.equals("Fixed")) {
            textView.setText(R.string.regular_incentive_small);

        } else {
            textView.setText(R.string.top_up_incentive_small);
        }


        View customView = LayoutInflater.from(getActivity()).inflate(
                R.layout.incentivedashboard_alertdialog_layout, null, false);

        ListView listItems = (ListView) customView.findViewById(R.id.lv_items);

        CustomIncentiveAdapterDialog mAdapter = new CustomIncentiveAdapterDialog(tempMIncentiveList, getActivity());
        listItems.setAdapter(mAdapter);

        dialog.setView(customView);
        dialog.show();

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
                //convertView=layoutInflater.inflate()
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
            holder.tvPayoutEarned.setText(getResources().getString(R.string.Rs) + " " + listData.get(position).getPayout());
            holder.tvMaxPossible.setText(getResources().getString(R.string.Rs) + " " + listData.get(position).getMaxpayout());

            return convertView;
        }

      /*  @Override
        public View getView(int position, View convertView, ViewGroup parent) {


            convertView = layoutInflater.inflate(R.layout.row_incentivedashboard_alertdialog, null);

            TextView txt = (TextView) convertView.findViewById(R.id.text);

            txt.setText(data[position]);


            return convertView;
        }*/

        public class ViewHolder {
            TextView tvFactor, tvTarget, tvAchived, tvAchper, tvPayoutEarned, tvMaxPossible;

        }

    }
}



