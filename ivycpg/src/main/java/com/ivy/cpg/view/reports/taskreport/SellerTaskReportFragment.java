package com.ivy.cpg.view.reports.taskreport;

import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.ivy.sd.png.asean.view.R;
import com.ivy.ui.task.model.TaskDataBO;
import com.ivy.sd.png.commons.IvyBaseFragment;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.provider.ConfigurationMasterHelper;
import com.ivy.sd.png.util.Commons;
import com.ivy.utils.DateTimeUtils;

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
        view.findViewById(R.id.retailer_layout).setVisibility(View.GONE);
        view.findViewById(R.id.date_layout).setVisibility(View.GONE);

        prepareScreenData();

        return view;

    }

    private void prepareScreenData(){

        try {

            RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getContext().getApplicationContext());
            recyclerView.setLayoutManager(mLayoutManager);
            recyclerView.setItemAnimator(new DefaultItemAnimator());

            myAdapter = new MyAdapter(mylist);
            recyclerView.setAdapter(myAdapter);

            //Loading Userwise data
            mylist.addAll(TaskReportHelper.getInstance(getContext()).getSellerWiseTaskReport());

            //Item Decoration for Header as User name
            RecyclerSectionItemDecoration sectionItemDecoration =
                    new RecyclerSectionItemDecoration(getResources().getDimensionPixelSize(R.dimen.dimen_30dp),
                            false,
                            getSectionCallback(mylist));
            recyclerView.addItemDecoration(sectionItemDecoration);

            myAdapter.notifyDataSetChanged();

        }catch(Exception e){
            Commons.printException(e);
        }

    }

    //This method will check id to set the Header
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
            holder.tvDate.setText(DateTimeUtils.convertFromServerDateToRequestedFormat(items.get(position).getCreatedDate(), ConfigurationMasterHelper.outDateFormat));

            if (!items.get(position).getIsdone().equalsIgnoreCase("0"))
                holder.imgStatus.setImageResource(R.drawable.coll_tick);
            else
                holder.imgStatus.setImageResource(android.R.color.transparent);

        }

        @Override
        public int getItemCount() {
            return items.size();
        }
    }

}
