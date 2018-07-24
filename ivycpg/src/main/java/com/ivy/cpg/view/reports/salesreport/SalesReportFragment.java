package com.ivy.cpg.view.reports.salesreport;


import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.TextView;

import com.ivy.cpg.view.reports.salesreport.salesreportdetails.SalesReturnDetailsActivity;
import com.ivy.sd.png.asean.view.R;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

public class SalesReportFragment extends Fragment implements ReCyclerViewItemClickListener {
    private Unbinder unbinder;

    @BindView(R.id.recycler_salesReport)
    RecyclerView recyclerView;

    @BindView(R.id.txttotal)
    TextView text_totalOrderValue;

    @BindView(R.id.txt_dist_pre_post)
    TextView text_averagePreOrPost;


    private List<SalesReturnReportBo> list;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        View view = inflater.inflate(R.layout.fragment_salesreturn_report, container, false);
        unbinder = ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setUpAdapter();
        setTotalValue();
    }

    private void setTotalValue() {


    }

    private void setUpAdapter() {
        SalesReportHelper salesReportHelper = new SalesReportHelper();
        list = salesReportHelper.getSalesReturnHeaderValue(getActivity());
        SalesReturnReportAdapter salesReturnReportAdapter =
                new SalesReturnReportAdapter(getActivity(),SalesReportFragment.this, list);

        recyclerView.setAdapter(salesReturnReportAdapter);
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(getActivity().getApplicationContext());
        recyclerView.setLayoutManager(mLayoutManager);

    }

    @Override
    public void onDestroy() {
        unbinder.unbind();
        super.onDestroy();
    }


    @Override
    public void onItemClickListener(View view, int pos) {

        list.get(pos).getUId();
        Intent intent = new Intent(getActivity(), SalesReturnDetailsActivity.class);
        intent.putExtra("UID", list.get(pos).getUId());
        intent.putExtra("RETAILERID",list.get(pos).getRetailerId());
        startActivity(intent);

    }
}
