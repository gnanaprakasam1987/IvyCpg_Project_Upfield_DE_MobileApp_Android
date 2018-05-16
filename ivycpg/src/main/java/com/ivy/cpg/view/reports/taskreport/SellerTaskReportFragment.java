package com.ivy.cpg.view.reports.taskreport;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.bo.TaskDataBO;
import com.ivy.sd.png.commons.IvyBaseFragment;
import com.ivy.sd.png.model.BusinessModel;

import java.util.HashSet;
import java.util.List;
import java.util.Vector;

public class SellerTaskReportFragment extends IvyBaseFragment {

    private RecyclerView recyclerView;
    MyAdapter myAdapter;
    private Vector<TaskDataBO> mylist = new Vector<>();
    private BusinessModel bmodel;

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
        view.findViewById(R.id.spinner_layout).setVisibility(View.GONE);

        prepareScreenData();

        return view;

    }

    private void prepareScreenData(){

        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getContext().getApplicationContext());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());

        myAdapter = new MyAdapter(mylist);
        recyclerView.setAdapter(myAdapter);

        mylist.addAll(TaskReportHelper.getInstance(getContext()).getSellerWiseTaskReport());

        RecyclerSectionItemDecoration sectionItemDecoration =
                new RecyclerSectionItemDecoration(getResources().getDimensionPixelSize(R.dimen.dimen_30dp),
                        false,
                        getSectionCallback(mylist));
        recyclerView.addItemDecoration(sectionItemDecoration);

        myAdapter.notifyDataSetChanged();

    }

    private RecyclerSectionItemDecoration.SectionCallback getSectionCallback(final List<TaskDataBO> taskDataBOS) {
        return new RecyclerSectionItemDecoration.SectionCallback() {
            @Override
            public boolean isSection(int position) {
                return position == 0
                        || taskDataBOS.get(position)
                        .getUserId() != taskDataBOS.get(position - 1)
                        .getUserId();
            }

            @Override
            public CharSequence getSectionHeader(int position) {
                return taskDataBOS.get(position)
                        .getUserName();
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

        }

        @Override
        public int getItemCount() {
            return items.size();
        }
    }

}
