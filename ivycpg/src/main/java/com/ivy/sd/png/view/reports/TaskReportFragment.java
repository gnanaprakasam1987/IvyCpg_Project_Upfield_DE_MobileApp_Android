package com.ivy.sd.png.view.reports;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.bo.TaskDataBO;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.cpg.view.reports.taskreport.TaskReportHelper;

import java.util.HashSet;
import java.util.List;
import java.util.Vector;

public class TaskReportFragment extends Fragment {

    private ListView lvwplist;
    private BusinessModel bmodel;
    private Vector<TaskDataBO> mylist, mRetailerPlannedDate;
    private View view;
    private ArrayAdapter<TaskDataBO> retailerSpinnerAdapter;
    private ArrayAdapter<String> retailer_dateAdapter;
    private HashSet<Integer> mSelectedRetailerId;
    private Spinner spinner_retailertaskreport;
    MyAdapter mSchedule;
    TextView select_retailer, select_date;
    private List<TaskDataBO> retailerID;
    private List<String> dateList;
    private Spinner spinner_date_taskreport;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        // TODO Auto-generated method stub
        super.onCreate(savedInstanceState);
        bmodel = (BusinessModel) getActivity().getApplicationContext();
        bmodel.setContext(getActivity());

        mylist = new Vector<TaskDataBO>();
        retailerID = new Vector<TaskDataBO>();
        dateList = new Vector<String>();
        mRetailerPlannedDate = new Vector<TaskDataBO>();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_task_report, container, false);

        if (bmodel.userMasterHelper.getUserMasterBO().getUserid() == 0) {
            Toast.makeText(getActivity(),
                    getResources().getString(R.string.sessionout_loginagain),
                    Toast.LENGTH_SHORT).show();
            getActivity().finish();
        }
        select_retailer = (TextView) view.findViewById(R.id.select_retailer);
        select_retailer.setText(getActivity().getResources().getString(R.string.plain_select));

        select_date = (TextView) view.findViewById(R.id.select_date);
        select_date.setText(getActivity().getResources().getString(R.string.plain_select));

        lvwplist = (ListView) view.findViewById(R.id.lv_taskreport_list);
        lvwplist.setCacheColorHint(0);

        spinner_retailertaskreport = (Spinner) view.findViewById(R.id.spinner_retid_taskreport);
        spinner_retailertaskreport.setVisibility(View.GONE);
        select_retailer.setVisibility(View.GONE);

        spinner_date_taskreport = (Spinner) view.findViewById(R.id.spinner_date_taskreport);
        spinner_date_taskreport.setVisibility(View.GONE);
        select_date.setVisibility(View.GONE);

        if (bmodel.configurationMasterHelper.SHOW_DATE_ROUTE) {
            spinner_date_taskreport.setVisibility(View.VISIBLE);
            select_date.setVisibility(View.VISIBLE);
        } else {
            spinner_retailertaskreport.setVisibility(View.VISIBLE);
            select_retailer.setVisibility(View.VISIBLE);
        }

        return view;

    }


    @Override
    public void onStart() {
        // TODO Auto-generated method stub
        super.onStart();
        mylist.clear();
        dateList.clear();
        mRetailerPlannedDate.clear();

        mSelectedRetailerId = new HashSet<>();

        retailer_dateAdapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item);
        retailer_dateAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        retailerSpinnerAdapter = new ArrayAdapter<TaskDataBO>(getActivity(), android.R.layout.simple_spinner_item);
        retailerSpinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        mylist.addAll(TaskReportHelper.getInstance(getContext()).loadTaskReport());
        mRetailerPlannedDate.addAll(TaskReportHelper.getInstance(getContext()).loadRetailerPlannedDate());

        if (bmodel.configurationMasterHelper.SHOW_DATE_ROUTE) {

            dateList.add(0, getActivity().getResources().getString(R.string.all));
            dateList.addAll(bmodel.mRetailerHelper.getMaxDaysInRouteSelection());
            for (String date : dateList)
                retailer_dateAdapter.add(date);

            spinner_date_taskreport.setAdapter(retailer_dateAdapter);

            spinner_date_taskreport.setOnItemSelectedListener(new OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                    //TaskDataBO tempBo = (TaskDataBO) parent.getSelectedItem();

                    mSelectedRetailerId.clear();

                    String mSeletedSpinnerDate = parent.getSelectedItem().toString();

                    if (!mSeletedSpinnerDate.equalsIgnoreCase(getActivity().getResources().getString(R.string.all))) {
                        for (TaskDataBO temp : mRetailerPlannedDate) {
                            if (mSeletedSpinnerDate.equalsIgnoreCase(temp.getPlannedDate())) {
                                mSelectedRetailerId.add(temp.getRid());
                            }
                        }
                    }
                    load(mSelectedRetailerId);
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {

                }
            });
        } else {
            retailerSpinnerAdapter.add(new TaskDataBO(0, getActivity().getResources().getString(R.string.all)));
            retailerID.addAll(TaskReportHelper.getInstance(getContext()).loadTaskReportRetailerList());
            for (TaskDataBO task : retailerID)
                retailerSpinnerAdapter.add(task);
            spinner_retailertaskreport.setAdapter(retailerSpinnerAdapter);
            spinner_retailertaskreport.setOnItemSelectedListener(new OnItemSelectedListener() {

                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                    TaskDataBO tempBo = (TaskDataBO) parent.getSelectedItem();

                    mSelectedRetailerId.clear();

                    if (tempBo.getRid() != 0)
                        mSelectedRetailerId.add(tempBo.getRid());

                    load(mSelectedRetailerId);
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {

                    // TODO Auto-generated method stub

                }
            });
        }
    }

    private void load(HashSet<Integer> retailerIds) {

        Vector<TaskDataBO> tasklist = new Vector<TaskDataBO>();

        if (retailerIds.size() == 0) {
            tasklist.addAll(mylist);
        } else {
            for (TaskDataBO temp : mylist) {
                if (retailerIds.contains(temp.getRid()))
                    tasklist.add(temp);
            }
        }
        mSchedule = new MyAdapter(tasklist);
        lvwplist.setAdapter(mSchedule);
    }

    private TaskDataBO taskBo;

    private class MyAdapter extends ArrayAdapter<TaskDataBO> {
        private Vector<TaskDataBO> items;

        public MyAdapter(Vector<TaskDataBO> items) {
            super(getActivity(), R.layout.row_task_reportlistview, items);
            this.items = items;
        }

        public TaskDataBO getItem(int position) {
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
            taskBo = (TaskDataBO) items.get(position);
            View row = convertView;
            if (row == null) {
                LayoutInflater inflater = getActivity().getLayoutInflater();
                row = inflater.inflate(R.layout.row_task_reportlistview,
                        parent, false);
                holder = new ViewHolder();
                holder.taskRetailername = (TextView) row
                        .findViewById(R.id.tv_taskretailername);
                holder.taskName = (TextView) row.findViewById(R.id.tv_taskname);
                holder.taskDesc = (TextView) row.findViewById(R.id.tv_taskdesc);
                holder.taskCreatedBy = (TextView) row.findViewById(R.id.tv_taskcreated_by);
                holder.taskCreatedDate = (TextView) row.findViewById(R.id.tv_taskcreated_date);
                row.setOnClickListener(new OnClickListener() {
                    public void onClick(View v) {

                    }
                });
                row.setTag(holder);
            } else {
                holder = (ViewHolder) row.getTag();
            }

            holder.taskRetailername.setText(taskBo.getRetailerName());
            holder.taskName.setText(taskBo.getTasktitle());
            holder.taskDesc.setText(taskBo.getTaskDesc());
            holder.taskCreatedBy.setText(taskBo.getTaskOwner());
            holder.taskCreatedDate.setText(taskBo.getCreatedDate());


            return row;
        }
    }

    class ViewHolder {
        TextView taskName, taskDesc, taskRetailername, taskCreatedBy, taskCreatedDate;
        String pname;
    }

}
