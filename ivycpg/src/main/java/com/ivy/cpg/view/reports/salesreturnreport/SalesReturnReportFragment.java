package com.ivy.cpg.view.reports.salesreturnreport;


import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import com.ivy.cpg.view.reports.salesreturnreport.salesreportdetails.SalesReturnDetailsActivity;
import com.ivy.sd.png.asean.view.R;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

public class SalesReturnReportFragment extends Fragment implements ReCyclerViewItemClickListener {
    private Unbinder unbinder;

    @BindView(R.id.recycler_salesReport)
    RecyclerView recyclerView;

    @BindView(R.id.txttotal)
    TextView text_totalOrderValue;

    @BindView(R.id.txt_dist_pre_post)
    TextView text_averagePreOrPost;

    @BindView(R.id.reportheader)
    View view;

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
        int totalVal = new SalesReturnReportHelper().getTotalReturnValueHeader(getActivity());
        text_totalOrderValue.setText(String.valueOf(totalVal));
        hideViews();
    }

    private void hideViews() {

        view.findViewById(R.id.txt_dist_pre_post).setVisibility(View.GONE);
        view.findViewById(R.id.lab_dist_pre_post).setVisibility(View.GONE);
        view.findViewById(R.id.btn_export).setVisibility(View.GONE);
        view.findViewById(R.id.view1).setVisibility(View.GONE);
        view.findViewById(R.id.view0).setVisibility(View.GONE);


    }

    private void setUpAdapter() {
        SalesReturnReportHelper salesReturnReportHelper = new SalesReturnReportHelper();
        list = salesReturnReportHelper.getSalesReturnHeaderValue(getActivity());

        if (list != null && list.size() > 0) {
            SalesReturnReportAdapter salesReturnReportAdapter =
                    new SalesReturnReportAdapter(getActivity(), SalesReturnReportFragment.this, list);

            recyclerView.setAdapter(salesReturnReportAdapter);
            LinearLayoutManager mLayoutManager = new LinearLayoutManager(getActivity().getApplicationContext());
            recyclerView.setLayoutManager(mLayoutManager);
        } else {
            Toast.makeText(getActivity(), "No data Available", Toast.LENGTH_SHORT).show();
        }

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
        startActivity(intent);
        getActivity().overridePendingTransition(R.anim.trans_left_in, R.anim.trans_left_out);

    }
}
