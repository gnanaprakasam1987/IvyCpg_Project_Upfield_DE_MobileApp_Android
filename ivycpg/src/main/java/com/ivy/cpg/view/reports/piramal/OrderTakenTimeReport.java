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
import com.ivy.sd.png.bo.OrderTakenTimeBO;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.provider.ConfigurationMasterHelper;
import com.ivy.sd.png.provider.ReportHelper;
import com.ivy.utils.DateTimeUtils;

import java.util.ArrayList;

/**
 * Created by maheswaran.m on 09-10-2015.
 */
public class OrderTakenTimeReport extends Fragment {
    private TextView tv_timeperiod, tv_pc, tv_tc, tv_lines, tv_value;
    private TextView tv_total, tv_totalcall_total, tv_productive_call_total, tv_value_total, tv_lines_total;
    private ListView lvwplist;
    private ArrayList<OrderTakenTimeBO> mylist;
    private BusinessModel bmodel;
    private View view;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        getActivity().getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        view = inflater.inflate(R.layout.fragment_timetaken_report, container,
                false);
        bmodel = (BusinessModel) getActivity().getApplicationContext();
        bmodel.setContext(getActivity());

        if (bmodel.userMasterHelper.getUserMasterBO().getUserid() == 0) {
            Toast.makeText(getActivity(),
                    getResources().getString(R.string.sessionout_loginagain),
                    Toast.LENGTH_SHORT).show();
            getActivity().finish();
        }

        tv_timeperiod = (TextView) view.findViewById(R.id.tv_timeperiod);
        tv_tc = (TextView) view.findViewById(R.id.tv_total_call);
        tv_pc = (TextView) view.findViewById(R.id.tv_productive_call);
        tv_lines = (TextView) view.findViewById(R.id.tv_lines);
        tv_value = (TextView) view.findViewById(R.id.tv_value);

        tv_total = (TextView) view.findViewById(R.id.tv_total);
        tv_totalcall_total = (TextView) view.findViewById(R.id.tv_total_call_total);
        tv_productive_call_total = (TextView) view.findViewById(R.id.tv_productive_call_total);
        tv_value_total = (TextView) view.findViewById(R.id.tv_value_total);
        tv_lines_total = (TextView) view.findViewById(R.id.tv_lines_total);

        //typeface
        tv_timeperiod.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
        tv_tc.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
        tv_pc.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
        tv_lines.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
        tv_value.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
        tv_total.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
        tv_totalcall_total.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
        tv_productive_call_total.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
        tv_value_total.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
        tv_lines_total.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));

        lvwplist = (ListView) view.findViewById(R.id.list);
        mylist = new ArrayList<OrderTakenTimeBO>();
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        mylist = ReportHelper.getInstance(getActivity()).downloadDayProcesssReport(DateTimeUtils.now(DateTimeUtils.DATE_GLOBAL));

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

    class MyAdapter extends ArrayAdapter<OrderTakenTimeBO> {
        ArrayList<OrderTakenTimeBO> items;

        private MyAdapter(ArrayList<OrderTakenTimeBO> items) {
            super(getActivity(), R.layout.row_fragment_timetaken_report, items);
            this.items = items;
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            final ViewHolder holder;

            OrderTakenTimeBO mOrderTakenTimeBO = (OrderTakenTimeBO) items
                    .get(position);
            View row = convertView;

            if (row == null) {
                LayoutInflater inflater = getActivity().getLayoutInflater();
                row = inflater
                        .inflate(R.layout.row_fragment_timetaken_report, parent, false);
                holder = new ViewHolder();

                holder.tv_timeperiod = (TextView) row.findViewById(R.id.tv_timeperiod);
                holder.tv_tc = (TextView) row.findViewById(R.id.tv_total_call);
                holder.tv_pc = (TextView) row.findViewById(R.id.tv_productive_call);
                holder.tv_lines = (TextView) row.findViewById(R.id.tv_lines);
                holder.tv_value = (TextView) row.findViewById(R.id.tv_value);

                holder.tv_timeperiod.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
                holder.tv_tc.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
                holder.tv_pc.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
                holder.tv_lines.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
                holder.tv_value.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));

                row.setTag(holder);
            } else {
                holder = (ViewHolder) row.getTag();
            }


            holder.tv_timeperiod.setText(mOrderTakenTimeBO.getmTimePeriod());
            holder.tv_tc.setText(mOrderTakenTimeBO.getmTotalcall() + "");
            holder.tv_pc.setText(String.valueOf(mOrderTakenTimeBO.getmProductiveCall()));
            holder.tv_lines.setText(mOrderTakenTimeBO.getmLinesSold() + "");
            holder.tv_value.setText(mOrderTakenTimeBO.getmValues() + "");

            int total_pc = 0, total_lines = 0, total_tc = 0;
            float total_value = 0;

            if (items != null && items.size() > 0) {
                for (int i = 0; i < items.size(); i++) {
                    total_tc = total_tc + items.get(i).getmTotalcall();
                    total_pc = total_pc + items.get(i).getmProductiveCall();
                    total_value = total_value + items.get(i).getmValues();
                    total_lines = total_lines + items.get(i).getmLinesSold();
                }
            }
            tv_totalcall_total.setText("" + total_tc);
            tv_productive_call_total.setText("" + total_pc);
            tv_value_total.setText("" + total_value);
            tv_lines_total.setText("" + total_lines);

            return (row);
        }
    }

    class ViewHolder {
        private TextView tv_timeperiod, tv_tc, tv_pc, tv_lines, tv_value;

    }

}
