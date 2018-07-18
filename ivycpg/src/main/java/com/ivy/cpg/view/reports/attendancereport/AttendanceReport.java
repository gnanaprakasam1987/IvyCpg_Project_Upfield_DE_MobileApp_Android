package com.ivy.cpg.view.reports.attendancereport;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.annotation.NonNull;
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

import com.ivy.cpg.view.reports.attendancereport.AttendanceReportHelper;
import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.bo.AttendanceReportBo;
import com.ivy.sd.png.commons.IvyBaseFragment;
import com.ivy.sd.png.model.BusinessModel;

import java.util.ArrayList;

/**
 * Screen to view attendance
 * Created by mansoor.k on 12-10-2016.
 */
public class AttendanceReport extends IvyBaseFragment {

    private ListView lvwplist;
    private Spinner monthSpinner;
    private TextView tvTotalDays, tvActualDays;
    private ArrayList<AttendanceReportBo> attendanceReportBos;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_attendance_report, container,
                false);

        BusinessModel bmodel = (BusinessModel) getActivity().getApplicationContext();
        bmodel.setContext(getActivity());


        if (bmodel.userMasterHelper.getUserMasterBO().getUserid() == 0) {
            Toast.makeText(getActivity(),
                    getResources().getString(R.string.sessionout_loginagain),
                    Toast.LENGTH_SHORT).show();
            getActivity().finish();
        }

        monthSpinner = view.findViewById(R.id.monthSpinner);
        tvTotalDays = view.findViewById(R.id.tvTotalDays);
        tvActualDays = view.findViewById(R.id.tvActualDays);

        lvwplist = view.findViewById(R.id.list);
        lvwplist.setCacheColorHint(0);

        AttendanceReportHelper attendanceReportHelper =new AttendanceReportHelper(getContext());
        ArrayList<String> attendanceMonths = attendanceReportHelper.downloadAttendanceMonth(bmodel.userMasterHelper.getUserMasterBO().getUserid());
        attendanceReportBos = attendanceReportHelper.downloadAttendanceReport(bmodel.userMasterHelper.getUserMasterBO().getUserid());

        if (attendanceMonths.size() > 1) {
            ArrayAdapter<String> monthAdapter = new ArrayAdapter<>(getActivity(),
                    android.R.layout.simple_spinner_item);
            monthAdapter
                    .setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

            for (int i = 0; i < attendanceMonths.size(); i++)
                monthAdapter.add(attendanceMonths.get(i));

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
        } else if (attendanceMonths.size() == 1) {
            monthSpinner.setVisibility(View.INVISIBLE);
            getAttendance(attendanceMonths.get(0));
        } else if (attendanceMonths.size() == 0 && attendanceMonths.size() == 0)
            monthSpinner.setVisibility(View.INVISIBLE);

        return view;

    }

    private class MyAdapter extends ArrayAdapter<AttendanceReportBo> {
        private ArrayList<AttendanceReportBo> items;

        public MyAdapter(ArrayList<AttendanceReportBo> items) {
            super(getActivity(), R.layout.row_attendance_report, items);
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

        @NonNull
        public View getView(int position, View convertView, @NonNull ViewGroup parent) {

            final ViewHolder holder;
            AttendanceReportBo attendance = items.get(position);

            View row = convertView;
            if (row == null) {
                LayoutInflater inflater = getActivity().getLayoutInflater();
                row = inflater.inflate(R.layout.row_attendance_report,
                        parent, false);
                holder = new ViewHolder();
                holder.date = row.findViewById(R.id.tvDate);
                holder.day = row.findViewById(R.id.tvDay);
                holder.status = row.findViewById(R.id.tvStatus);


                row.setTag(holder);
            } else {
                holder = (ViewHolder) row.getTag();
            }
            holder.date.setText(attendance.getDate());
            holder.day.setText(attendance.getDay());

            if (attendance.getStatus().equalsIgnoreCase("P")) {
                String mPresent = "Present";
                holder.status.setText(mPresent);
                holder.status.setTextColor(ContextCompat.getColor(getActivity(), R.color.GREEN));
            } else if (attendance.getStatus().equalsIgnoreCase("H")) {
                String mHoilday = "Holiday";
                holder.status.setText(mHoilday);
                holder.status.setTextColor(ContextCompat.getColor(getActivity(), R.color.Orange));
            } else if (attendance.getStatus().equalsIgnoreCase("L") || attendance.getStatus().equalsIgnoreCase("A")) {
                String mLeave = "Leave";
                if (attendance.getStatus().equalsIgnoreCase("L"))
                    holder.status.setText(mLeave);
                String mAbsent = "Absent";
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

    @SuppressLint("SetTextI18n")
    public void getAttendance(String month) {
        ArrayList<AttendanceReportBo> mylist = new ArrayList<>();
        int presentDays = 0;
        int absentDays = 0;
        for (AttendanceReportBo attbo : attendanceReportBos) {
            if (attbo.getMonth().equalsIgnoreCase(month))
                mylist.add(attbo);
        }

        MyAdapter mSchedule = new MyAdapter(mylist);
        lvwplist.setAdapter(mSchedule);

        tvTotalDays.setText(getResources().getString(R.string.total_days) + "  " + mylist.size());

        if (mylist.size() > 0)
            for (AttendanceReportBo abo : mylist) {
                if (abo.getStatus().equalsIgnoreCase("P"))
                    presentDays++;
                if (abo.getStatus().equalsIgnoreCase("A"))
                    absentDays++;

            }
        tvActualDays.setText(getResources().getString(R.string.present_days) + "  " + presentDays + "  " +
                getResources().getString(R.string.absent_days) + " " + absentDays);

    }
}
