package com.ivy.cpg.view.supervisor.mvp.sellerlistview;


import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;

import com.ivy.cpg.view.supervisor.mvp.SupervisorModelBo;
import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.commons.IvyBaseFragment;
import com.ivy.sd.png.model.HideShowScrollListener;

import java.util.ArrayList;

public class TabViewListFragment extends IvyBaseFragment{
    private RecyclerView recyclerView;
    private ArrayList<SupervisorModelBo> sellerListBos = new ArrayList<>();
    private boolean showStatus;

    private Animation slide_down, slide_up;
    private LinearLayout bottomLayout;

    public TabViewListFragment() {
    }

    public static TabViewListFragment getInstance(int position, ArrayList<SupervisorModelBo> sellerListBos, boolean showStatus) {
        TabViewListFragment tabViewListFragment = new TabViewListFragment();
        Bundle args = new Bundle();
        args.putSerializable("Sellers", sellerListBos);
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

        bottomLayout = view.findViewById(R.id.bottom_layout);
        slide_down = AnimationUtils.loadAnimation(getContext(),
                R.anim.out_to_bottom);
        slide_up = AnimationUtils.loadAnimation(getContext(),
                R.anim.bottom_layout_slideup);


        recyclerView.addOnScrollListener(new HideShowScrollListener() {
            @Override
            public void onHide() {
                if (bottomLayout.getVisibility() == View.VISIBLE) {
                    bottomLayout.startAnimation(slide_down);
                    bottomLayout.setVisibility(View.GONE);
                }
            }

            @Override
            public void onShow() {
                if (bottomLayout.getVisibility() == View.GONE) {
                    bottomLayout.setVisibility(View.VISIBLE);
                    bottomLayout.startAnimation(slide_up);
                }
            }

            @Override
            public void onScrolled() {
                // To load more data
            }

        });

        prepareScreenData();

        view.findViewById(R.id.filter_layout).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((SellerListActivity)getActivity()).filter();
            }
        });

        view.findViewById(R.id.sort_layout).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ((SellerListActivity)getActivity()).sort();
            }
        });

        return view;
    }

    @Override
    public void setArguments(Bundle args) {
        super.setArguments(args);
        this.sellerListBos = (ArrayList<SupervisorModelBo>) args.getSerializable("Sellers");
        this.showStatus = args.getBoolean("ShowStatus");
    }

    private void prepareScreenData(){

        SellerListAdapter myAdapter = new SellerListAdapter(getContext().getApplicationContext(),showStatus,sellerListBos);
        recyclerView.setAdapter(myAdapter);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getContext().getApplicationContext());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
    }

}
