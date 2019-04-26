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
import com.ivy.cpg.view.task.TaskDataBO;
import com.ivy.sd.png.commons.IvyBaseFragment;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.provider.ConfigurationMasterHelper;
import com.ivy.sd.png.util.Commons;
import com.ivy.utils.DateTimeUtils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Vector;

public class OutletTaskReportFragment extends IvyBaseFragment {

    private RecyclerView recyclerView;
    private MyAdapter myAdapter;
    private Vector<TaskDataBO> mylist = new Vector<>();
    private BusinessModel bmodel;
    private Spinner spinnerReportRetailer, spinnerReportDate;
    private int retailerSelectedId = 0;
    private HashSet<Integer> dateSelectedRetailerId;

    private int checkRetailerSpnr,checkDateSpinner;

    private TextView tvRetailerTxt,tvDateText;


    Vector<TaskDataBO> tasklist = new Vector<>();

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        bmodel = (BusinessModel) getActivity().getApplicationContext();
        bmodel.setContext(getActivity());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_task_report_list, container, false);

        initViews(view);
        prepareScreenData(view);

        return view;
    }

    private void initViews(View view) {

        recyclerView = view.findViewById(R.id.recycler_view);

        spinnerReportRetailer = view.findViewById(R.id.spinner_retid_taskreport);
        spinnerReportDate = view.findViewById(R.id.spinner_date_taskreport);

        tvRetailerTxt = view.findViewById(R.id.select_retailer);
        tvDateText = view.findViewById(R.id.select_date);

        try{
            tvRetailerTxt.setText(
                    bmodel.labelsMasterHelper.applyLabels(tvRetailerTxt.getTag())!=null
                            ?
                            bmodel.labelsMasterHelper.applyLabels(tvRetailerTxt.getTag()):"Retailer");

            tvDateText.setText(
                    bmodel.labelsMasterHelper.applyLabels(tvDateText.getTag())!=null
                            ?
                            bmodel.labelsMasterHelper.applyLabels(tvDateText.getTag()):"Date");
        }catch(Exception e){
            Commons.printException(e);
        }


        tvDateText.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
        tvRetailerTxt.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
    }

    private void prepareScreenData(View view) {
        try {

            RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getContext().getApplicationContext());
            recyclerView.setLayoutManager(mLayoutManager);
            recyclerView.setItemAnimator(new DefaultItemAnimator());

            //Enable or disable date Filter based on Config
            if (bmodel.configurationMasterHelper.TASK_PLANNED >= 0) {
                view.findViewById(R.id.date_layout).setVisibility(View.VISIBLE);
            } else {
                view.findViewById(R.id.date_layout).setVisibility(View.GONE);
            }

            dateSelectedRetailerId = new HashSet<>();

            ArrayAdapter<TaskDataBO> spinnerRetailerAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item);
            spinnerRetailerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

            ArrayAdapter<String> spinnerDateAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item);
            spinnerDateAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

            myAdapter = new MyAdapter(tasklist);
            recyclerView.setAdapter(myAdapter);

            //Header Decoration to set Retailer name as Header for common retailer
            RecyclerSectionItemDecoration sectionItemDecoration =
                    new RecyclerSectionItemDecoration(getResources().getDimensionPixelSize(R.dimen.dimen_30dp),
                            false,
                            getSectionCallback(tasklist));
            recyclerView.addItemDecoration(sectionItemDecoration);

            //Holds all the Task data based on the config
            mylist.addAll(TaskReportHelper.getInstance(getContext()).loadTaskReport());

            loadAll();

            ArrayList<TaskDataBO> strings = new ArrayList<>();
            strings.add(new TaskDataBO(0, getActivity().getResources().getString(R.string.all),0));

            //Load the Spinner element with retailer name
            strings.addAll(TaskReportHelper.getInstance(getContext()).loadTaskReportRetailerList());

            spinnerRetailerAdapter.addAll(strings);
            spinnerReportRetailer.setAdapter(spinnerRetailerAdapter);
            spinnerReportRetailer.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                    if (++checkRetailerSpnr > 1) {
                        TaskDataBO tempBo = (TaskDataBO) parent.getSelectedItem();

                        retailerSelectedId = tempBo.getRid();

                        HashSet<Integer> integers = new HashSet<>();

                    /*
                      If Date Filter is enabled then it will check for retailer
                      spinner matching date spinner values. if no element matched then no data will be loaded.
                     */

                        if (tempBo.getRid() == 0 && dateSelectedRetailerId.size() == 0)
                            loadAll();
                        else if (tempBo.getRid() == 0 && dateSelectedRetailerId.size() > 0)
                            load(dateSelectedRetailerId);
                        else if (tempBo.getRid() != 0 && dateSelectedRetailerId.size() == 0) {
                            integers.add(retailerSelectedId);
                            load(integers);
                        } else if (tempBo.getRid() != 0 && dateSelectedRetailerId.size() > 0) {
                            if (dateSelectedRetailerId.contains(retailerSelectedId))
                                integers.add(retailerSelectedId);
                            load(integers);
                        }
                    }
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {

                }
            });

            //If Date Spinner Config is found then following block will be executed
            if (bmodel.configurationMasterHelper.TASK_PLANNED >= 0) {

                final Vector<TaskDataBO> dateWiseTask = new Vector<>();
                //Load the PlannedDate with retailer id
                dateWiseTask.addAll(TaskReportHelper.getInstance(getContext()).loadRetailerPlannedDate());
                ArrayList<String> stringVal = new ArrayList<>();

                stringVal.add(0, getActivity().getResources().getString(R.string.all));

                // If Config value is 1 then it will load only todays date
                // otherwise all the planned date with date higher than the downloadDate(UserMaster) will be loaded with Limit 7
                if (bmodel.configurationMasterHelper.TASK_PLANNED == 1) {
                    stringVal.add(DateTimeUtils.now(DateTimeUtils.DATE_GLOBAL));
                } else {
                    stringVal.addAll(bmodel.mRetailerHelper.getMaxDaysInRouteSelection());
                }

                spinnerDateAdapter.addAll(stringVal);
                spinnerReportDate.setAdapter(spinnerDateAdapter);
                spinnerReportDate.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                    @Override
                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                        if (++checkDateSpinner > 1) {
                            dateSelectedRetailerId.clear();

                            String mSeletedSpinnerDate = parent.getSelectedItem().toString();

                            HashSet<Integer> integers = new HashSet<>();

                            if (!mSeletedSpinnerDate.equalsIgnoreCase(getActivity().getResources().getString(R.string.all))) {
                                for (TaskDataBO temp : dateWiseTask) {
                                    if (mSeletedSpinnerDate.equalsIgnoreCase(temp.getPlannedDate())) {
                                        dateSelectedRetailerId.add(temp.getRid());
                                    }
                                }
                            }

                        /*
                          If Date Filter is enabled then it will check for retailer
                          spinner matching date spinner values. if no element matched then no data will be loaded.
                         */

                            if (dateSelectedRetailerId.size() == 0 && retailerSelectedId == 0) {
                                loadAll();
                            } else if (dateSelectedRetailerId.size() == 0 && retailerSelectedId != 0) {
                                integers.add(retailerSelectedId);
                                load(integers);
                            } else if (dateSelectedRetailerId.size() > 0 && retailerSelectedId == 0) {
                                integers.addAll(dateSelectedRetailerId);
                                load(integers);
                            } else if (dateSelectedRetailerId.size() > 0 && retailerSelectedId != 0) {
                                if (dateSelectedRetailerId.contains(retailerSelectedId))
                                    integers.add(retailerSelectedId);
                                load(integers);
                            }
                        }
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {

                    }
                });
            }
        }catch(Exception e){
            Commons.printException(e);
        }
    }

    //Load All the Task Data
    private void loadAll() {

        tasklist.clear();
        tasklist.addAll(mylist);
        myAdapter.notifyDataSetChanged();

    }

    //Load Specified Task Data
    private void load(HashSet<Integer> retailerIds) {

        tasklist.clear();
        if (retailerIds.size() > 0) {
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

            TextView tvRetailerName, tvTaskName, tvTaskDesc, tvCreatedBy, tvDate;
            ImageView imgStatus;

            public MyViewHolder(View view) {
                super(view);

                tvRetailerName = view.findViewById(R.id.tv_outlet_name);
                tvTaskName = view.findViewById(R.id.tv_task_name);
                tvTaskDesc = view.findViewById(R.id.tv_task_Desc);
                tvCreatedBy = view.findViewById(R.id.tv_task_created_by);
                tvDate = view.findViewById(R.id.tv_task_date);
                imgStatus = view.findViewById(R.id.task_status);

                tvRetailerName.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
                tvTaskName.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
                tvTaskDesc.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
                tvCreatedBy.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
                tvDate.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
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
            holder.tvCreatedBy.setText(items.get(position).getUsercreated()!=null?"Created by "+items.get(position).getUsercreated():"");
            holder.tvDate.setText("At "+ DateTimeUtils.convertFromServerDateToRequestedFormat(items.get(position).getCreatedDate(), ConfigurationMasterHelper.outDateFormat));

            if (!items.get(position).getIsdone().equalsIgnoreCase("0"))
                holder.imgStatus.setImageResource(R.drawable.coll_tick);
            else
                holder.imgStatus.setImageResource(android.R.color.transparent);

        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        @Override
        public int getItemViewType(int position) {
            return position;
        }
    }


}
