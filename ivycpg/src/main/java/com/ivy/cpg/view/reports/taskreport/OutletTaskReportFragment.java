package com.ivy.cpg.view.reports.taskreport;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.bo.TaskDataBO;
import com.ivy.sd.png.commons.IvyBaseFragment;
import com.ivy.sd.png.commons.SDUtil;
import com.ivy.sd.png.model.BusinessModel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Vector;

public class OutletTaskReportFragment extends IvyBaseFragment {

    private RecyclerView recyclerView;
    MyAdapter myAdapter;
    private Vector<TaskDataBO> mylist = new Vector<>();
    private BusinessModel bmodel;
    private Spinner spinnerReport;
    TextView select_retailer;
    private ArrayAdapter<TaskDataBO> spinnerRetailerAdapter;
    private ArrayAdapter<String> spinnerDateAdapter;
    private HashSet<Integer> mSelectedRetailerId;

    Vector<TaskDataBO> tasklist = new Vector<TaskDataBO>();

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        bmodel = (BusinessModel) getActivity().getApplicationContext();
        bmodel.setContext(getActivity());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_task_report_list, container, false);

        recyclerView = view.findViewById(R.id.recycler_view);

        select_retailer = view.findViewById(R.id.select_retailer);
        select_retailer.setText(getActivity().getResources().getString(R.string.plain_select));

        spinnerReport = view.findViewById(R.id.spinner_retid_taskreport);

        prepareScreenData();

        return view;

    }

    private void prepareScreenData(){

        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getContext().getApplicationContext());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());

        if (bmodel.configurationMasterHelper.SHOW_DATE_ROUTE) {
            select_retailer.setText("Select Date");
        } else {
            select_retailer.setText("Select Outlet");
        }

        mSelectedRetailerId = new HashSet<>();

        spinnerDateAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item);
        spinnerDateAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        spinnerRetailerAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item);
        spinnerRetailerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        myAdapter = new MyAdapter(tasklist);
        recyclerView.setAdapter(myAdapter);

        RecyclerSectionItemDecoration sectionItemDecoration =
                new RecyclerSectionItemDecoration(getResources().getDimensionPixelSize(R.dimen.dimen_30dp),
                        false,
                        getSectionCallback(tasklist));
        recyclerView.addItemDecoration(sectionItemDecoration);

        mylist.addAll(TaskReportHelper.getInstance(getContext()).loadTaskReport());

        if (bmodel.configurationMasterHelper.SHOW_DATE_ROUTE || bmodel.configurationMasterHelper.SHOW_WEEK_ROUTE) {

            final Vector<TaskDataBO> dateWiseTask = new Vector<>();

            dateWiseTask.addAll(TaskReportHelper.getInstance(getContext()).loadRetailerPlannedDate());

            ArrayList<String> strings = new ArrayList<>();

            strings.add(0, getActivity().getResources().getString(R.string.all));

            if(bmodel.configurationMasterHelper.SHOW_WEEK_ROUTE){
                strings.add(SDUtil.now(SDUtil.DATE_GLOBAL));
            }else{
                strings.addAll(bmodel.mRetailerHelper.getMaxDaysInRouteSelection());
            }

            spinnerDateAdapter.addAll(strings);

            spinnerReport.setAdapter(spinnerDateAdapter);

            spinnerReport.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                    mSelectedRetailerId.clear();

                    String mSeletedSpinnerDate = parent.getSelectedItem().toString();

                    if (!mSeletedSpinnerDate.equalsIgnoreCase(getActivity().getResources().getString(R.string.all))) {
                        for (TaskDataBO temp : dateWiseTask) {
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

            ArrayList<TaskDataBO> strings = new ArrayList<>();
            strings.add(new TaskDataBO(0, getActivity().getResources().getString(R.string.all)));

            strings.addAll(TaskReportHelper.getInstance(getContext()).loadTaskReportRetailerList());

            spinnerRetailerAdapter.addAll(strings);
            spinnerReport.setAdapter(spinnerRetailerAdapter);
            spinnerReport.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

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

        tasklist.clear();

        if (retailerIds.size() == 0) {
            tasklist.addAll(mylist);
        } else {
            for (TaskDataBO temp : mylist) {
                if (retailerIds.contains(temp.getRid()))
                    tasklist.add(temp);
            }
        }

        myAdapter.notifyDataSetChanged();

    }

    private RecyclerSectionItemDecoration.SectionCallback getSectionCallback(final List<TaskDataBO> taskDataBOS) {
        return new RecyclerSectionItemDecoration.SectionCallback() {
            @Override
            public boolean isSection(int position) {
                return position == 0
                        || taskDataBOS.get(position)
                        .getRid() != taskDataBOS.get(position - 1)
                        .getRid();
            }

            @Override
            public CharSequence getSectionHeader(int position) {
                return taskDataBOS.get(position)
                        .getRetailerName();
            }
        };
    }


    public class MyAdapter extends RecyclerView.Adapter<MyAdapter.MyViewHolder> {

        private Vector<TaskDataBO> items;

        public MyAdapter(Vector<TaskDataBO> items) {
            this.items = items;
        }

        public class MyViewHolder extends RecyclerView.ViewHolder {

            TextView tvRetailerName,tvTaskName,tvTaskDesc,tvCreatedBy,tvDate;
            ImageView imgStatus;

            public MyViewHolder(View view) {
                super(view);

                tvRetailerName = view.findViewById(R.id.tv_outlet_name);
                tvTaskName = view.findViewById(R.id.tv_task_name);
                tvTaskDesc = view.findViewById(R.id.tv_task_Desc);
                tvCreatedBy = view.findViewById(R.id.tv_task_created_by);
                tvDate = view.findViewById(R.id.tv_task_date);
                imgStatus = view.findViewById(R.id.task_status);

            }
        }

        @Override
        public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.task_report_list_item, parent, false);

            return new MyViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(MyViewHolder holder, final int position) {

            holder.tvRetailerName.setText(items.get(position).getRetailerName());
            holder.tvTaskName.setText(items.get(position).getTasktitle());
            holder.tvTaskDesc.setText(items.get(position).getTaskDesc());
            holder.tvCreatedBy.setText(items.get(position).getUsercreated());
            holder.tvDate.setText(items.get(position).getCreatedDate());

            if (items.get(position).getIsdone().equalsIgnoreCase("0"))
                holder.imgStatus.setImageResource(R.drawable.ic_in_progress_icon);
            else
                holder.imgStatus.setImageResource(R.drawable.coll_tick);

        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        @Override
        public int getItemViewType(int position)
        {
            return position;
        }
    }

}
