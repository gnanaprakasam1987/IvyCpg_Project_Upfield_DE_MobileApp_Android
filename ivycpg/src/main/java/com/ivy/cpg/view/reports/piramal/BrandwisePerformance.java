package com.ivy.cpg.view.reports.piramal;

import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.bo.ReportBrandPerformanceBO;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.provider.ConfigurationMasterHelper;
import com.ivy.sd.png.provider.ReportHelper;
import com.ivy.utils.DateTimeUtils;

import java.util.ArrayList;

/**
 * Created by maheswaran.m on 05-10-2015.
 */
public class BrandwisePerformance extends Fragment {
    private TextView tv_brandname, tv_pc, tv_vpd, tv_lines, tv_target_achievement;
    private TextView tv_productive_call_total, tv_value_per_day_total, tv_lines_total;
    private ListView lvwplist;
    private ArrayList<ReportBrandPerformanceBO> mylist;
    private BusinessModel bmodel;
    private View view;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        getActivity().getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        view = inflater.inflate(R.layout.fragment_brand_performance_report, container,
                false);
        bmodel = (BusinessModel) getActivity().getApplicationContext();
        bmodel.setContext(getActivity());

        if (bmodel.userMasterHelper.getUserMasterBO().getUserid() == 0) {
            Toast.makeText(getActivity(),
                    getResources().getString(R.string.sessionout_loginagain),
                    Toast.LENGTH_SHORT).show();
            getActivity().finish();
        }

        tv_brandname = (TextView) view.findViewById(R.id.tv_brandname);
        tv_pc = (TextView) view.findViewById(R.id.tv_productive_call);
        tv_vpd = (TextView) view.findViewById(R.id.tv_value_per_day);
        tv_lines = (TextView) view.findViewById(R.id.tv_lines);
        tv_target_achievement = (TextView) view.findViewById(R.id.tv_target_achievement);

        tv_productive_call_total = (TextView) view.findViewById(R.id.tv_productive_call_total);
        tv_value_per_day_total = (TextView) view.findViewById(R.id.tv_value_per_day_total);
        tv_lines_total = (TextView) view.findViewById(R.id.tv_lines_total);

        tv_brandname.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
        tv_pc.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
        tv_vpd.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
        tv_lines.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
        tv_target_achievement.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
        tv_productive_call_total.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
        tv_value_per_day_total.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
        tv_lines_total.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));

        lvwplist = (ListView) view.findViewById(R.id.list);
        mylist = new ArrayList<ReportBrandPerformanceBO>();
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        mylist = ReportHelper.getInstance(getActivity()).downloadBrandPerformanceReport(DateTimeUtils.now(DateTimeUtils.DATE_GLOBAL));

        // Show alert if error loading data.
        if (mylist == null) {
            Toast.makeText(getActivity(),
                    getResources().getString(R.string.unable_to_load_data),
                    Toast.LENGTH_SHORT).show();
            return;
        } else if (mylist.size() == 0) {
            Toast.makeText(getActivity(),
                    getResources().getString(R.string.no_visit_today),
                    Toast.LENGTH_SHORT).show();
            return;
        } else if (mylist != null) {
            MyAdapter mSchedule = new MyAdapter(mylist);
            lvwplist.setAdapter(mSchedule);
        }
    }

    class MyAdapter extends ArrayAdapter<ReportBrandPerformanceBO> {
        ArrayList<ReportBrandPerformanceBO> items;

        private MyAdapter(ArrayList<ReportBrandPerformanceBO> items) {
            super(getActivity(), R.layout.row_brand_performance_report, items);
            this.items = items;
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            final ViewHolder holder;

            ReportBrandPerformanceBO brandPerformanceReport = (ReportBrandPerformanceBO) items
                    .get(position);
            View row = convertView;

            if (row == null) {
                LayoutInflater inflater = getActivity().getLayoutInflater();
                row = inflater
                        .inflate(R.layout.row_brand_performance_report, parent, false);
                holder = new ViewHolder();

                holder.tv_brandname = (TextView) row.findViewById(R.id.tv_brandname);
                holder.tv_pc = (TextView) row.findViewById(R.id.tv_productive_call);
                holder.tv_vpd = (TextView) row.findViewById(R.id.tv_value_per_day);
                holder.tv_lines = (TextView) row.findViewById(R.id.tv_lines);
                holder.tv_target_achievement = (TextView) row.findViewById(R.id.tv_target_achievement);

                holder.tv_brandname.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
                holder.tv_pc.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
                holder.tv_vpd.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
                holder.tv_lines.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
                holder.tv_target_achievement.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));

                row.setTag(holder);
            } else {
                holder = (ViewHolder) row.getTag();
            }


            holder.tv_brandname.setText(brandPerformanceReport.getProductname());
            holder.tv_pc.setText(brandPerformanceReport.getProductivecall() + "");
            holder.tv_vpd.setText(String.valueOf(brandPerformanceReport.getValueperday()));
            holder.tv_lines.setText(brandPerformanceReport.getLines() + "");
            holder.tv_target_achievement.setText(String.valueOf(brandPerformanceReport.getTarget_achievement()));

            int total_pc = 0, total_lines = 0;
            float total_vpd = 0;

            if (items != null && items.size() > 0) {
                for (int i = 0; i < items.size(); i++) {
                    total_pc = total_pc + items.get(i).getProductivecall();
                    total_vpd = total_vpd + items.get(i).getValueperday();
                    total_lines = total_lines + items.get(i).getLines();
                }
            }
            tv_productive_call_total.setText("" + total_pc);
            tv_value_per_day_total.setText(String.valueOf(total_vpd));
            tv_lines_total.setText("" + total_lines);

            return (row);
        }
    }

    class ViewHolder {
        private TextView tv_brandname, tv_pc, tv_vpd, tv_lines, tv_target_achievement;

    }

}
