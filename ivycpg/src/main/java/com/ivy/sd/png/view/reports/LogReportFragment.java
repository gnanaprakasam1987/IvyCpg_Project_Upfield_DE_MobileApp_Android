package com.ivy.sd.png.view.reports;


import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.bo.LogReportBO;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.util.Commons;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.concurrent.TimeUnit;


/**
 * A simple {@link Fragment} subclass.
 */
public class LogReportFragment extends Fragment {

    BusinessModel bmodel;
    private ListView list;
    private TextView tvTotalHrs;
    private static final String FORMAT = "%02d:%02d";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_report_log,
                container, false);

        bmodel = (BusinessModel) getActivity().getApplicationContext();
        bmodel.setContext(getActivity());

        bmodel.reportHelper.downloadLogReport();

        ArrayList<LogReportBO> myList = bmodel.reportHelper.getLogReport();

        list = (ListView) view.findViewById(R.id.list);
        tvTotalHrs = (TextView) view.findViewById(R.id.tvTotalHrs);

        if (myList.size() > 0) {
            Collections.sort(myList, new Comparator<LogReportBO>() {
                SimpleDateFormat formatter = new SimpleDateFormat("HH:mm:ss");

                public int compare(LogReportBO o1, LogReportBO o2) {
                    try {
                        return formatter.parse(o1.getOutTime()).compareTo(formatter.parse(o2.getOutTime()));
                    } catch (ParseException e) {
                        throw new IllegalArgumentException(e);
                    }
                }
            });
            list.setAdapter(new MyAdapter(myList));
            for (LogReportBO logBo : myList) {
                calculateHrsSpent(logBo.getInTime(), logBo.getOutTime());
            }
            String hrsMin = parseTime(totMinutes);
            tvTotalHrs.setText(hrsMin);
        } else {
            Toast.makeText(getActivity(), getString(R.string.alert_activity_log), Toast.LENGTH_LONG).show();
        }


        return view;
    }

    long totMinutes = 0;

    private void calculateHrsSpent(String startTime, String endTime) {

        SimpleDateFormat sdf1 = new SimpleDateFormat("HH:mm:ss");
        try {
            Date date1 = sdf1.parse(startTime);
            Date date2 = sdf1.parse(endTime);

            long durationInMillis = date2.getTime() - date1.getTime();

            totMinutes = totMinutes + durationInMillis;


        } catch (ParseException e) {
            Commons.printException(e);
        }
    }

    public static String parseTime(long milliseconds) {
        return String.format(FORMAT,
                TimeUnit.MILLISECONDS.toHours(milliseconds),
                TimeUnit.MILLISECONDS.toMinutes(milliseconds) - TimeUnit.HOURS.toMinutes(
                        TimeUnit.MILLISECONDS.toHours(milliseconds)));
    }

    class ViewHolder {
        Button btnStatus;
        TextView tvRetailerName, tvInterval;
        LogReportBO logReportBO;
    }

    private class MyAdapter extends ArrayAdapter<LogReportBO> {
        private ArrayList<LogReportBO> items;

        public MyAdapter(ArrayList<LogReportBO> items) {
            super(getActivity(), R.layout.row_report_log);
            this.items = items;
        }

        public LogReportBO getItem(int position) {
            return items.get(position);
        }

        public long getItemId(int position) {
            return position;
        }

        public int getCount() {
            return items.size();
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            final ViewHolder holder;

            if (convertView == null) {

                holder = new ViewHolder();

                LayoutInflater inflater = LayoutInflater.from(getActivity().getBaseContext());

                convertView = (View) inflater.inflate(R.layout.row_report_log, null);

                holder.tvRetailerName = (TextView) convertView.findViewById(R.id.tv_retailer_name);
                holder.tvInterval = (TextView) convertView.findViewById(R.id.tv_time_interval);
                holder.btnStatus = (Button) convertView.findViewById(R.id.btnStatus);

                convertView.setTag(holder);

            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            holder.logReportBO = items.get(position);

            holder.tvRetailerName.setText(holder.logReportBO.getRetailerName());
            if (holder.logReportBO.getInTime().length() > 0)
                holder.tvInterval.setText(to12hrFormat(holder.logReportBO.getInTime()) + " - " +
                        to12hrFormat(holder.logReportBO.getOutTime()));
            else
                holder.tvInterval.setText(to12hrFormat(holder.logReportBO.getOutTime()));

            holder.btnStatus.setText(position + 1 + "");

            if (holder.logReportBO.isInterval())
                holder.btnStatus.setBackgroundColor(ContextCompat.getColor(getActivity(), R.color.Burgundy));
            else
                holder.btnStatus.setBackgroundColor(ContextCompat.getColor(getActivity(), R.color.ink_blue));

            return convertView;
        }
    }

    private String to12hrFormat(String time) {
        DateFormat f1 = new SimpleDateFormat("HH:mm:ss"); //HH for hour of the day (0 - 23)
        DateFormat f2 = new SimpleDateFormat("h:mm a");
        String time_converted = "";
        try {
            Date d = f1.parse(time);
            time_converted = f2.format(d).toLowerCase(); // "12:18am"
        } catch (ParseException e) {
            Commons.printException(e);
        }

        return time_converted;

    }

}
