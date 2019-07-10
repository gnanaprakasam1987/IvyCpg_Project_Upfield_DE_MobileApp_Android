package com.ivy.cpg.view.reports.attendancereport;

import android.annotation.SuppressLint;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
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
import com.ivy.sd.png.commons.IvyBaseFragment;
import com.ivy.sd.png.model.BusinessModel;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.BiFunction;
import io.reactivex.observers.DisposableObserver;
import io.reactivex.schedulers.Schedulers;

/**
 * Screen to view attendance
 * Created by mansoor.k on 12-10-2016.
 */
public class AttendanceReport extends IvyBaseFragment {


    private ArrayList<AttendanceReportBo> attendanceReportBos;
    private Unbinder unbinder;
    CompositeDisposable compositeDisposable;

    @BindView(R.id.list)
    ListView lvwplist;

    @BindView(R.id.monthSpinner)
    Spinner monthSpinner;

    @BindView(R.id.tvTotalDays)
    TextView tvTotalDays;

    @BindView(R.id.tvActualDays)
    TextView tvActualDays;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_attendance_report, container,
                false);
        unbinder = ButterKnife.bind(this, view);

        BusinessModel bmodel = (BusinessModel) getActivity().getApplicationContext();
        bmodel.setContext(getActivity());


        if (bmodel.userMasterHelper.getUserMasterBO().getUserid() == 0) {
            Toast.makeText(getActivity(),
                    getResources().getString(R.string.sessionout_loginagain),
                    Toast.LENGTH_SHORT).show();
            getActivity().finish();
        }

        lvwplist.setCacheColorHint(0);
        getAttendanceRptData(bmodel.userMasterHelper.getUserMasterBO().getUserid());

        return view;
    }

    /**
     * get Attendance Report data from DB
     *
     * @param userId
     */
    private void getAttendanceRptData(int userId) {
        final ArrayList<String> attendanceMonths = new ArrayList<>();
        attendanceReportBos = new ArrayList<>();
        AttendanceReportHelper attendanceReportHelper = new AttendanceReportHelper();
     /*   final AlertDialog alertDialog;
        AlertDialog.Builder builder;
        builder = new AlertDialog.Builder(getActivity());*/
        compositeDisposable = new CompositeDisposable();
       /* customProgressDialog(builder, getActivity().getResources().getString(R.string.loading));
        alertDialog = builder.create();
        alertDialog.show();*/

        compositeDisposable.add((Disposable) Observable.zip(attendanceReportHelper.downloadAttendanceMonth(userId, getActivity()),
                attendanceReportHelper.downloadAttendanceReport(userId, getActivity())
                , new BiFunction<ArrayList<String>, ArrayList<AttendanceReportBo>, Boolean>() {
                    @Override
                    public Boolean apply(ArrayList<String> monthList, ArrayList<AttendanceReportBo> attendanceReportList) throws Exception {

                        if (monthList.size() > 0
                                && attendanceReportList.size() > 0) {
                            attendanceMonths.addAll(monthList);
                            attendanceReportBos = attendanceReportList;
                            return true;
                        }
                        return false;
                    }
                }).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeWith(new DisposableObserver<Boolean>() {
                    @Override
                    public void onNext(Boolean isFlag) {

                        if (isFlag) {
                            setUpMonthSpinner(attendanceMonths);
                        } else
                            Toast.makeText(getActivity(), getResources().getString(R.string.data_not_mapped), Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onError(Throwable e) {
                        //alertDialog.dismiss();
                        Toast.makeText(getActivity(), getResources().getString(R.string.unable_to_load_data), Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onComplete() {
                        //alertDialog.dismiss();
                    }
                }));

    }

    /**
     * load data into list view
     *
     * @param attendanceMonths
     */
    private void setUpMonthSpinner(ArrayList<String> attendanceMonths) {
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
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
        if (compositeDisposable != null
                && !compositeDisposable.isDisposed()) {
            compositeDisposable.dispose();
        }
        unbinder.unbind();
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
                holder = new ViewHolder(row);
                row.setTag(holder);
            } else {
                holder = (ViewHolder) row.getTag();
            }
            holder.date.setText(attendance.getDate());
            holder.day.setText(attendance.getDay());

            if (attendance.getStatus().equalsIgnoreCase("P")) {
                String mPresent = "Present";
                holder.status.setText(mPresent);
                holder.status.setTextColor(ContextCompat.getColor(getActivity(), R.color.green_productivity));
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

        @BindView(R.id.tvDate)
        TextView date;

        @BindView(R.id.tvDay)
        TextView day;

        @BindView(R.id.tvStatus)
        TextView status;

        ViewHolder(View view) {
            ButterKnife.bind(this, view);
        }
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
