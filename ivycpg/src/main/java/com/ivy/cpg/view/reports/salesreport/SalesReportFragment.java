package com.ivy.cpg.view.reports.salesreport;


import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;

import com.ivy.cpg.view.reports.orderstatusreport.OrderStatusReportFragment;
import com.ivy.sd.png.asean.view.R;

import butterknife.ButterKnife;
import butterknife.Unbinder;

public class SalesReportFragment extends Fragment {
    private Unbinder unbinder;


    public static SalesReportFragment newInstance(boolean screenFlag) {
        SalesReportFragment fragment = new SalesReportFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        View view = inflater.inflate(R.layout.fragment_order_report, container, false);
        unbinder = ButterKnife.bind(this, view);
        return view;
    }
}
