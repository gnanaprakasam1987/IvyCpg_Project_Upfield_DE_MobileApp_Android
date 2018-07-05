package com.ivy.cpg.view.supervisor.mvp.sellermapview;


import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.ivy.cpg.view.supervisor.helper.DetailsBo;
import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.commons.IvyBaseFragment;

import java.util.ArrayList;

public class TabViewListFragment extends IvyBaseFragment{
    private RecyclerView recyclerView;
    private ArrayList<DetailsBo> detailsBos = new ArrayList<>();
    private boolean showStatus;

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

        SellerListAdapter myAdapter = new SellerListAdapter(getContext().getApplicationContext(),showStatus,detailsBos);
        recyclerView.setAdapter(myAdapter);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getContext().getApplicationContext());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
    }

}
