package com.ivy.cpg.view.reports.orderstatusreport;

import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.commons.IvyBaseFragment;
import com.ivy.sd.png.model.BusinessModel;

import java.util.Vector;

/**
 * Created by anandasir on 28/5/18.
 */

public class OrderStatusReportFragment extends IvyBaseFragment implements OrderStatusContractor.OrderStatusView, AdapterView.OnItemSelectedListener {

    private BusinessModel bmodel;
    public OrderStatusPresenterImpl orderStatusPresenter;
    private RecyclerView recyclerView;
    private Spinner spnRetailer;
    private TextView emptytxtview;
    private LinearLayout retailerLayout;
    private ArrayAdapter<OrderStatusRetailerReportBO> spinnerRetailerAdapter;
    private Vector<OrderStatusRetailerReportBO> strings = new Vector<>();
    boolean isOrderScreen = true;

    public static OrderStatusReportFragment newInstance(boolean screenFlag) {
        OrderStatusReportFragment fragment = new OrderStatusReportFragment();
        Bundle args = new Bundle();
        args.putBoolean("isOrder", screenFlag);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        bmodel = (BusinessModel) getActivity().getApplicationContext();
        bmodel.setContext(getActivity());

        orderStatusPresenter = new OrderStatusPresenterImpl(getActivity());
        orderStatusPresenter.setView(this);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_orderstatus_report, container, false);
        if (bmodel.userMasterHelper.getUserMasterBO().getUserid() == 0) {
            Toast.makeText(getActivity(),
                    getResources().getString(R.string.sessionout_loginagain),
                    Toast.LENGTH_SHORT).show();
            getActivity().finish();
        }
        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        initializeViews(view);
        try {
            isOrderScreen = getArguments().getBoolean("isOrder", true);
            orderStatusPresenter.downloadOrderStatusReportList(isOrderScreen);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void initializeViews(View view) {
        recyclerView = (RecyclerView) view.findViewById(R.id.recycler_view);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getContext().getApplicationContext());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());

        spnRetailer = (Spinner) view.findViewById(R.id.spinner_retid_orderstatusreport);
        spinnerRetailerAdapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_spinner_item);
        spinnerRetailerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        retailerLayout = (LinearLayout) view.findViewById(R.id.retailer_layout);
        emptytxtview = (TextView) view.findViewById(R.id.empty_view);
    }

    @Override
    public void setAdapter() {
        OrderStatusAdapter myAdapter = new OrderStatusAdapter(bmodel, orderStatusPresenter, isOrderScreen);
        recyclerView.setAdapter(myAdapter);
    }

    @Override
    public void setSpinnerAdapter() {
        strings = new Vector<>();
        strings.addAll(orderStatusPresenter.getOrderStatusRetailerReportList());
        strings.add(0, new OrderStatusRetailerReportBO("0", "", getActivity().getResources().getString(R.string.all)));
        spinnerRetailerAdapter.addAll(strings);
        spnRetailer.setAdapter(spinnerRetailerAdapter);
        spnRetailer.setOnItemSelectedListener(this);
    }

    @Override
    public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
        orderStatusPresenter.filterList(strings.get(i).getRetailerID());
    }

    @Override
    public void onNothingSelected(AdapterView<?> adapterView) {

    }

    @Override
    public void setEmptyView(String text) {
        emptytxtview.setVisibility(View.VISIBLE);
        recyclerView.setVisibility(View.GONE);
        retailerLayout.setVisibility(View.GONE);
        emptytxtview.setText(text);
    }
}
