package com.ivy.cpg.view.reports.orderfulfillmentreport;

import android.content.Context;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.ivy.cpg.view.orderfullfillment.OrderFullfillmentHelper;
import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.bo.OrderFulfillmentReportListBO;
import com.ivy.sd.png.commons.IvyBaseFragment;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.util.Commons;
import com.ivy.utils.FontUtils;

import java.util.ArrayList;

/**
 * Created by anandasir on 25/9/18.
 */

public class OrderFulfillmentDetailReport extends IvyBaseFragment {

    private static ListView lstOrderList;
    private static BusinessModel bmodel;
    private static Context context;
    private static OrderfullfillmentAdapter adapter;
    private static ArrayList<OrderFulfillmentReportListBO> orderFulfillmentReportList;
    String retailerID, orderID, dateStr;
    OrderFullfillmentHelper orderFullfillmentHelper;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_report_order_fulfillment,
                container, false);

        if (getArguments() != null) {
            retailerID = getArguments().getString("retid");
            orderID = getArguments().getString("orderid");
            dateStr = getArguments().getString("date");
        }

        bmodel = (BusinessModel) getActivity().getApplicationContext();
        bmodel.setContext(getActivity());

        orderFullfillmentHelper = OrderFullfillmentHelper.getInstance(getActivity());
        view.findViewById(R.id.layoutDelivery).setVisibility(View.GONE);
        lstOrderList = (ListView) view.findViewById(R.id.list);
        loadOrderList();
        setLabels(view);
        setHasOptionsMenu(true);
        return view;
    }

    private void setLabels(View view){
        try {
            ((TextView) view.findViewById(R.id.txtHeaderProductName)).setTypeface(
                    FontUtils.getFontRoboto(getActivity(), FontUtils.FontType.MEDIUM));
            if (bmodel.labelsMasterHelper.applyLabels(view.findViewById(R.id.txtHeaderProductName).getTag()) != null)
                ((TextView) view.findViewById(R.id.txtHeaderProductName)).setText(bmodel.labelsMasterHelper
                        .applyLabels(view.findViewById(R.id.txtHeaderProductName).getTag()));
        } catch (Exception e) {
            Commons.printException(e + "");
        }
        try {
            ((TextView) view.findViewById(R.id.txtHeaderOrderedCases)).setTypeface(
                    FontUtils.getFontRoboto(getActivity(), FontUtils.FontType.MEDIUM));
            if (bmodel.labelsMasterHelper.applyLabels(view.findViewById(R.id.txtHeaderOrderedCases).getTag()) != null)
                ((TextView) view.findViewById(R.id.txtHeaderOrderedCases)).setText(bmodel.labelsMasterHelper
                        .applyLabels(view.findViewById(R.id.txtHeaderOrderedCases).getTag()));
        } catch (Exception e) {
            Commons.printException(e + "");
        }
        try {
            ((TextView) view.findViewById(R.id.txtHeaderOrderedPieces)).setTypeface(
                    FontUtils.getFontRoboto(getActivity(), FontUtils.FontType.MEDIUM));
            if (bmodel.labelsMasterHelper.applyLabels(view.findViewById(R.id.txtHeaderOrderedPieces).getTag()) != null)
                ((TextView) view.findViewById(R.id.txtHeaderOrderedPieces)).setText(bmodel.labelsMasterHelper
                        .applyLabels(view.findViewById(R.id.txtHeaderOrderedPieces).getTag()));
        } catch (Exception e) {
            Commons.printException(e + "");
        }
        try {
            ((TextView) view.findViewById(R.id.txtHeaderFulfilledCases)).setTypeface(
                    FontUtils.getFontRoboto(getActivity(), FontUtils.FontType.MEDIUM));
            if (bmodel.labelsMasterHelper.applyLabels(view.findViewById(R.id.txtHeaderFulfilledCases).getTag()) != null)
                ((TextView) view.findViewById(R.id.txtHeaderFulfilledCases)).setText(bmodel.labelsMasterHelper
                        .applyLabels(view.findViewById(R.id.txtHeaderFulfilledCases).getTag()));
        } catch (Exception e) {
            Commons.printException(e + "");
        }
        try {
            ((TextView) view.findViewById(R.id.txtHeaderFulfilledPieces)).setTypeface(
                    FontUtils.getFontRoboto(getActivity(), FontUtils.FontType.MEDIUM));
            if (bmodel.labelsMasterHelper.applyLabels(view.findViewById(R.id.txtHeaderFulfilledPieces).getTag()) != null)
                ((TextView) view.findViewById(R.id.txtHeaderFulfilledPieces)).setText(bmodel.labelsMasterHelper
                        .applyLabels(view.findViewById(R.id.txtHeaderFulfilledPieces).getTag()));
        } catch (Exception e) {
            Commons.printException(e + "");
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        final ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();

        if (actionBar != null) {
            actionBar.setDisplayShowTitleEnabled(false);
            actionBar.setElevation(0);
            setScreenTitle(getResources().getString(R.string.order_fulfillment_detail));
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.context = context;
    }

    private void loadOrderList() {
        orderFullfillmentHelper.downloadOrderFullfillmentDetailReport(dateStr, orderID);
        orderFulfillmentReportList = orderFullfillmentHelper.getOrderFulfillmentDetailList();

        adapter = new OrderfullfillmentAdapter(orderFulfillmentReportList);
        lstOrderList.setAdapter(adapter);
    }

    public class OrderfullfillmentAdapter extends ArrayAdapter<OrderFulfillmentReportListBO> {

        ArrayList<OrderFulfillmentReportListBO> items;

        private OrderfullfillmentAdapter(ArrayList<OrderFulfillmentReportListBO> items) {
            super(context, R.layout.fragment_report_order_fulfillment_list, items);
            this.items = items;
        }

        public OrderFulfillmentReportListBO getItem(int position) {
            return items.get(position);
        }

        public long getItemId(int position) {
            return position;
        }

        public int getCount() {
            return items.size();
        }

        public View getView(final int position, View view, ViewGroup parent) {

            final ViewHolderDetail holder;
            if (view == null) {
                holder = new ViewHolderDetail();
                // Other views
                LayoutInflater mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                view = mInflater.inflate(R.layout.fragment_report_order_fulfillment_detail_list1, parent, false);
                holder.txtProductName = (TextView) view.findViewById(R.id.txtProductName);
                holder.txtcasesOrderedQty = (TextView) view.findViewById(R.id.txtOrderedCases);
                holder.txtpiecesOrderedQty = (TextView) view.findViewById(R.id.txtOrderedPieces);
                holder.txtcasesFulfilledQty = (TextView) view.findViewById(R.id.txtFulfilledCases);
                holder.txtpiecesFulfilledQty = (TextView) view.findViewById(R.id.txtFulfilledPieces);

                view.setTag(holder);
            } else {
                holder = (ViewHolderDetail) view.getTag();
            }

            holder.orderFulfillmentReportListBO = items.get(position);

            holder.txtProductName.setText(holder.orderFulfillmentReportListBO.getProductName());
            holder.txtcasesOrderedQty.setText(holder.orderFulfillmentReportListBO.getOrderedCases() + "");
            holder.txtpiecesOrderedQty.setText(holder.orderFulfillmentReportListBO.getOrderedPcs() + "");
            holder.txtcasesFulfilledQty.setText(holder.orderFulfillmentReportListBO.getFulfilledCases() + "");
            holder.txtpiecesFulfilledQty.setText(holder.orderFulfillmentReportListBO.getFulfilledPcs() + "");

            return view;
        }

        class ViewHolderDetail {
            private OrderFulfillmentReportListBO orderFulfillmentReportListBO;
            private TextView txtProductName, txtcasesOrderedQty, txtpiecesOrderedQty,
                    txtcasesFulfilledQty, txtpiecesFulfilledQty;
            private LinearLayout lnrSchemeHeader;
        }

    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_target_plan, menu);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        menu.findItem(R.id.menu_skutgt).setVisible(false);
        /*if (BusinessModel.dashHomeStatic)
            menu.findItem(R.id.menu_next).setVisible(true);*/
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int i = item.getItemId();
        if (i == android.R.id.home) {
            getActivity().finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

}
