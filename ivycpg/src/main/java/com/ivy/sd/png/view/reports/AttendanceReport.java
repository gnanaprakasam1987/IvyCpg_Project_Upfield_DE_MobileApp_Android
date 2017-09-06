package com.ivy.sd.png.view.reports;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.bo.AttendanceReportBo;
import com.ivy.sd.png.model.BusinessModel;

import java.util.Vector;

/**
 * Created by mansoor.k on 12-10-2016.
 */
public class AttendanceReport extends Fragment {

    private ListView lvwplist;
    private BusinessModel bmodel;
    private Vector<AttendanceReportBo> mylist;
    private View view;
    private Spinner monthSpinner;
    private ArrayAdapter<String> monthAdapter;
    private TextView tvTotalDays, tvActualDays;
    private int presentdays = 0, absentdays = 0;
    private String mPresent = "Present", mAbsent = "Absent", mHoilday = "Holiday", mLeave = "Leave";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_attendance_report, container,
                false);

        bmodel = (BusinessModel) getActivity().getApplicationContext();
        bmodel.setContext(getActivity());


        if (bmodel.userMasterHelper.getUserMasterBO().getUserid() == 0) {
            Toast.makeText(getActivity(),
                    getResources().getString(R.string.sessionout_loginagain),
                    Toast.LENGTH_SHORT).show();
            getActivity().finish();
        }
        monthSpinner = (Spinner) view.findViewById(R.id.monthSpinner);
        tvTotalDays = (TextView) view.findViewById(R.id.tvTotalDays);
        tvActualDays = (TextView) view.findViewById(R.id.tvActualDays);
        lvwplist = (ListView) view.findViewById(R.id.lvwplist);
        lvwplist.setCacheColorHint(0);

        if (bmodel.reportHelper.getAttendanceMonth().size() > 1) {
            monthAdapter = new ArrayAdapter<String>(getActivity(),
                    android.R.layout.simple_spinner_item);
            monthAdapter
                    .setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            for (int i = 0; i < bmodel.reportHelper.getAttendanceMonth().size(); i++)
                monthAdapter.add(bmodel.reportHelper.getAttendanceMonth().get(i));
            monthSpinner.setAdapter(monthAdapter);

            monthSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {
                    getAttendance(monthSpinner.getSelectedItem().toString());
                }

                @Override
                public void onNothingSelected(AdapterView<?> adapterView) {

                }
            });
        } else if (bmodel.reportHelper.getAttendanceMonth().size() == 1) {
            monthSpinner.setVisibility(View.INVISIBLE);
            getAttendance(bmodel.reportHelper.getAttendanceMonth().get(0));
        } else if (bmodel.reportHelper.getAttendanceMonth().size() == 0 && bmodel.reportHelper.getAttendanceList().size() == 0)
            monthSpinner.setVisibility(View.INVISIBLE);

        return view;

    }

    private AttendanceReportBo attendance;

    private class MyAdapter extends ArrayAdapter<AttendanceReportBo> {
        private Vector<AttendanceReportBo> items;

        public MyAdapter(Vector<AttendanceReportBo> items) {
            super(getActivity(), R.layout.row_begining_stock_listview, items);
            this.items = items;
        }

        public AttendanceReportBo getItem(int position) {
            return items.get(position);
        }

        public long getItemId(int position) {
            return position;
        }

        public int getCount() {
            return items.size();
        }

        public View getView(int position, View convertView, ViewGroup parent) {

            final ViewHolder holder;
            attendance = (AttendanceReportBo) items.get(position);

            View row = convertView;
            if (row == null) {
                LayoutInflater inflater = getActivity().getLayoutInflater();
                row = inflater.inflate(R.layout.row_attendance_report,
                        parent, false);
                holder = new ViewHolder();
                holder.date = (TextView) row.findViewById(R.id.tvDate);
                holder.day = (TextView) row.findViewById(R.id.tvDay);
                holder.status = (TextView) row.findViewById(R.id.tvStatus);


                row.setTag(holder);
            } else {
                holder = (ViewHolder) row.getTag();
            }
            holder.date.setText(attendance.getDate());
            holder.day.setText(attendance.getDay());

            if (attendance.getStatus().equalsIgnoreCase("P")) {
                holder.status.setText(mPresent);
                holder.status.setTextColor(ContextCompat.getColor(getActivity(), R.color.GREEN));
            } else if (attendance.getStatus().equalsIgnoreCase("H")) {
                holder.status.setText(mHoilday);
                holder.status.setTextColor(ContextCompat.getColor(getActivity(), R.color.Orange));
            } else if (attendance.getStatus().equalsIgnoreCase("L") || attendance.getStatus().equalsIgnoreCase("A")) {
                if (attendance.getStatus().equalsIgnoreCase("L"))
                    holder.status.setText(mLeave);
                if (attendance.getStatus().equalsIgnoreCase("A"))
                    holder.status.setText(mAbsent);
                holder.status.setTextColor(ContextCompat.getColor(getActivity(), R.color.RED));
            }
            return row;
        }
    }

    class ViewHolder {
        TextView date, day, status;
    }

    public void getAttendance(String month) {
        mylist = new Vector<>();
        presentdays = 0;
        absentdays = 0;
        for (AttendanceReportBo attbo : bmodel.reportHelper.getAttendanceList()) {
            if (attbo.getMonth().equalsIgnoreCase(month))
                mylist.add(attbo);
        }

        MyAdapter mSchedule = new MyAdapter(mylist);
        lvwplist.setAdapter(mSchedule);

        tvTotalDays.setText(getResources().getString(R.string.total_days) + "  " + mylist.size());

        if (mylist.size() > 0)
            for (AttendanceReportBo abo : mylist) {
                if (abo.getStatus().equalsIgnoreCase("P"))
                    presentdays++;
                if (abo.getStatus().equalsIgnoreCase("A"))
                    absentdays++;

            }
        tvActualDays.setText(getResources().getString(R.string.present_days) + "  " + presentdays + "  " +
                getResources().getString(R.string.absent_days) + " " + absentdays);

    }
}
