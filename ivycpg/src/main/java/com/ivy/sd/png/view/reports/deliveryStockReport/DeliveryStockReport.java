package com.ivy.sd.png.view.reports.deliveryStockReport;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.util.Commons;

import java.util.ArrayList;

/**
 * Created by rajkumar.s on 12/1/2016.
 */

public class DeliveryStockReport extends android.support.v4.app.Fragment implements View.OnClickListener {


    BusinessModel bmodel;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_delivery_report, container,
                false);

        try {
            bmodel = (BusinessModel) getActivity().getApplicationContext();
            bmodel.setContext(getActivity());

            ListView listView = (ListView) view.findViewById(R.id.list);

            try {
                if (bmodel.labelsMasterHelper.applyLabels(view.findViewById(
                        R.id.case_qty).getTag()) != null)
                    ((TextView) view.findViewById(R.id.case_qty))
                            .setText(bmodel.labelsMasterHelper
                                    .applyLabels(view.findViewById(R.id.case_qty)
                                            .getTag()));
                if (bmodel.labelsMasterHelper.applyLabels(view.findViewById(
                        R.id.piece_qty).getTag()) != null)
                    ((TextView) view.findViewById(R.id.piece_qty))
                            .setText(bmodel.labelsMasterHelper
                                    .applyLabels(view.findViewById(R.id.piece_qty)
                                            .getTag()));
                if (bmodel.labelsMasterHelper.applyLabels(view.findViewById(
                        R.id.outer_qty).getTag()) != null)
                    ((TextView) view.findViewById(R.id.outer_qty))
                            .setText(bmodel.labelsMasterHelper
                                    .applyLabels(view.findViewById(R.id.outer_qty)
                                            .getTag()));
            } catch (Exception e) {
                Commons.printException(e);
            }

            ArrayList<DeliveryStockBo> deliveryStockList = DeliveryStockHelper.getInstance(getActivity()).downloadDeliveryStock(getActivity());

            if (deliveryStockList.size() > 0) {
                MyAdapter adapter = new MyAdapter(deliveryStockList);
                listView.setAdapter(adapter);
            }
        } catch (Exception ex) {
            Commons.printException(ex);
        }

        return view;
    }

    class MyAdapter extends ArrayAdapter<DeliveryStockBo> {
        ArrayList<DeliveryStockBo> items;

        MyAdapter(ArrayList<DeliveryStockBo> items) {
            super(getActivity(), R.layout.row_delivery_report,
                    items);
            this.items = items;
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            final ViewHolder holder;

            View row = convertView;

            if (row == null) {
                LayoutInflater inflater = getActivity().getLayoutInflater();
                row = inflater.inflate(R.layout.row_delivery_report, parent,
                        false);
                holder = new ViewHolder();
                holder.tvwpsname = (TextView) row.findViewById(R.id.product_name_title);
                holder.tvwpsname.setMaxLines(bmodel.configurationMasterHelper.MAX_NO_OF_PRODUCT_LINES);

                holder.tv_case_qty = (TextView) row.findViewById(R.id.case_qty);
                holder.tv_outer_qty = (TextView) row.findViewById(R.id.outer_qty);
                holder.tv_piece_qty = (TextView) row.findViewById(R.id.piece_qty);

                row.setTag(holder);
            } else {
                holder = (ViewHolder) row.getTag();
            }
            holder.deliveryStkBO = items.get(position);

            holder.tvwpsname.setText(holder.deliveryStkBO.getProductShortName());

            holder.tv_case_qty.setText(holder.deliveryStkBO.getOrderedCaseQty() + "");
            holder.tv_outer_qty.setText(holder.deliveryStkBO.getOrderedOuterQty() + "");
            holder.tv_piece_qty.setText(holder.deliveryStkBO.getOrderedPcsQty() + "");


            return (row);
        }
    }

    class ViewHolder {

        TextView tvwpsname;
        TextView tv_case_qty, tv_piece_qty, tv_outer_qty;
        DeliveryStockBo deliveryStkBO;
    }

    @Override
    public void onClick(View view) {

    }
}
