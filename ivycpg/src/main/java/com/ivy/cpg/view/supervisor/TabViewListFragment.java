package com.ivy.cpg.view.supervisor;


import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.commons.IvyBaseFragment;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.provider.ConfigurationMasterHelper;

import java.util.ArrayList;

public class TabViewListFragment extends IvyBaseFragment{
    private RecyclerView recyclerView;
    private ArrayList<DetailsBo> detailsBos = new ArrayList<>();
    private boolean showStatus;
    private BusinessModel bmodel;

    public TabViewListFragment() {
    }

    public static TabViewListFragment getInstance(int position, ArrayList<DetailsBo> detailsBos,boolean showStatus) {
        TabViewListFragment tabViewListFragment = new TabViewListFragment();
        Bundle args = new Bundle();
        args.putSerializable("Sellers", detailsBos);
        args.putInt("position", position);
        args.putBoolean("ShowStatus",showStatus);
        tabViewListFragment.setArguments(args);
        return tabViewListFragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        bmodel = (BusinessModel) getActivity().getApplicationContext();
        bmodel.setContext(getActivity());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.seller_tab_view_layout, container, false);

        recyclerView = view.findViewById(R.id.seller_list);
        prepareScreenData();
        return view;
    }

    @Override
    public void setArguments(Bundle args) {
        super.setArguments(args);
        this.detailsBos = (ArrayList<DetailsBo>) args.getSerializable("Sellers");
        this.showStatus = args.getBoolean("ShowStatus");
    }

    private void prepareScreenData(){

        MyAdapter myAdapter = new MyAdapter();
        recyclerView.setAdapter(myAdapter);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getContext().getApplicationContext());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
    }

    public class MyAdapter extends RecyclerView.Adapter<MyAdapter.MyViewHolder> {

        public class MyViewHolder extends RecyclerView.ViewHolder {
            private TextView userName,routeText,statusTextview,performancePercent,outletCoveredTxt,messageText;
            private RelativeLayout statusLayout;

            public MyViewHolder(View view) {
                super(view);
                userName = view.findViewById(R.id.tv_user_name);
                routeText = view.findViewById(R.id.tv_route);
                statusTextview = view.findViewById(R.id.tv_status);
                statusLayout = view.findViewById(R.id.status_layout);
                performancePercent = view.findViewById(R.id.tv_percent_txt);
                outletCoveredTxt = view.findViewById(R.id.tv_outlet_covered);
                messageText = view.findViewById(R.id.tv_message);

                userName.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.REGULAR));
                routeText.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.REGULAR));
                statusTextview.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.REGULAR));
                performancePercent.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.REGULAR));
                outletCoveredTxt.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.REGULAR));
                messageText.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.REGULAR));

            }
        }

        @Override
        public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.seller_recycler_item_, parent, false);

            return new MyViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(MyViewHolder holder, final int position) {
            holder.userName.setText(detailsBos.get(position).getUserName());

            if(!showStatus)
                holder.statusLayout.setVisibility(View.GONE);

            if(detailsBos.get(position).getStatus().equalsIgnoreCase("In Market")){
                holder.statusTextview.setText(detailsBos.get(position).getStatus());
                holder.statusLayout.setBackgroundDrawable(ContextCompat.getDrawable(getContext(),R.drawable.covered_bg_gradient));
            }else{
                holder.statusTextview.setText(detailsBos.get(position).getStatus());
                holder.statusLayout.setBackgroundDrawable(ContextCompat.getDrawable(getContext(),R.drawable.absent_seller_text_bg_gradient));
            }

            holder.routeText.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(getContext(), SellerMapViewActivity.class);
                    intent.putExtra("SellerId", String.valueOf(detailsBos.get(position).getUserId()));
                    intent.putExtra("screentitle", detailsBos.get(position).getUserName() );
                    intent.putExtra("TrackingType", 2);
                    startActivity(intent);
                }
            });
        }

        @Override
        public int getItemCount() {
            return detailsBos.size();
        }
    }

}
