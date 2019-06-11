package com.ivy.cpg.view.supervisor.mvp.sellerlistview;


import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;
import android.widget.RadioGroup;

import com.ivy.cpg.view.supervisor.mvp.models.SellerBo;
import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.commons.IvyBaseFragment;
import com.ivy.sd.png.model.HideShowScrollListener;

import java.util.ArrayList;

public class TabViewListFragment extends IvyBaseFragment{
    private RecyclerView recyclerView;
    private ArrayList<SellerBo> sellerListBos = new ArrayList<>();
    private boolean showStatus;

    private String selectedDate;

    private FloatingActionButton sortView;
    private BottomSheetBehavior bottomSheetBehavior;
    private RadioGroup sortRadioGroup;

    public TabViewListFragment() {
    }

    public static TabViewListFragment getInstance(int position, ArrayList<SellerBo> sellerListBos, boolean showStatus,String selectedDate) {
        TabViewListFragment tabViewListFragment = new TabViewListFragment();
        Bundle args = new Bundle();
        args.putSerializable("Sellers", sellerListBos);
        args.putInt("position", position);
        args.putBoolean("ShowStatus",showStatus);
        args.putString("Date",selectedDate);
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

        sortView = view.findViewById(R.id.fab);

        prepareScreenData(view);

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
        this.sellerListBos = (ArrayList<SellerBo>) args.getSerializable("Sellers");
        this.showStatus = args.getBoolean("ShowStatus");
        this.selectedDate = args.getString("Date");
    }

    private void prepareScreenData(View view){

        recyclerView = view.findViewById(R.id.seller_list);

        sortRadioGroup = view.findViewById(R.id.sort_radio_group);

        bottomSheetBehavior = BottomSheetBehavior.from(view.findViewById(R.id.bottomSheetLayout));
        bottomSheetBehavior.setHideable(false);
        bottomSheetBehavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                switch (newState) {
                    case BottomSheetBehavior.STATE_COLLAPSED:
                        bottomSheetBehavior.setHideable(true);
                        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);
                        break;
                    case BottomSheetBehavior.STATE_DRAGGING:
                        break;
                    case BottomSheetBehavior.STATE_EXPANDED:
                        break;
                    case BottomSheetBehavior.STATE_HIDDEN:
                        break;
                    case BottomSheetBehavior.STATE_SETTLING:
                        break;
                }
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {
            }
        });

        bottomSheetBehavior.setHideable(true);
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);

        sortView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_EXPANDED);
            }
        });

        sortRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);

                int radioButtonID = sortRadioGroup.getCheckedRadioButtonId();
                View radioButton = sortRadioGroup.findViewById(radioButtonID);
                int idx = sortRadioGroup.indexOfChild(radioButton);
            }
        });

        updateSellerAdapter();
    }

    private void updateSellerAdapter(){
        SellerListAdapter myAdapter = new SellerListAdapter(getContext().getApplicationContext(),showStatus,sellerListBos,selectedDate);
        recyclerView.setAdapter(myAdapter);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getContext().getApplicationContext());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
    }

}
