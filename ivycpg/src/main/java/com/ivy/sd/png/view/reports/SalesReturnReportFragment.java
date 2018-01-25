package com.ivy.sd.png.view.reports;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.ivy.cpg.view.salesreturn.SalesReturnReasonBO;
import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.bo.RetailerMasterBO;
//import com.ivy.sd.png.bo.SalesReturnReasonBO;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.provider.ConfigurationMasterHelper;

import java.util.ArrayList;

public class SalesReturnReportFragment extends Fragment {

    BusinessModel bmodel;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.sales_return_report_fragment,
                container, false);
        bmodel = (BusinessModel) getActivity().getApplicationContext();
        bmodel.setContext(getActivity());

        ListView salesListView = (ListView) view.findViewById(R.id.lvwpList);
        MyAdapter adapter = new MyAdapter(bmodel.reportHelper.getSalesReturnRetailerList());
        salesListView.setAdapter(adapter);

        return view;
    }

    class MyAdapter extends ArrayAdapter<SalesReturnReasonBO> {

        private final ArrayList<SalesReturnReasonBO> items;

        private MyAdapter(ArrayList<SalesReturnReasonBO> items) {
            super(getActivity(), R.layout.row_sales_return_retailer_report, items);
            this.items = items;
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            final ViewHolder holder;

            final SalesReturnReasonBO salesReport = items.get(position);
            View row = convertView;

            if (row == null) {
                LayoutInflater inflater = getActivity().getLayoutInflater();
                row = inflater.inflate(R.layout.row_sales_return_retailer_report, parent, false);

                holder = new ViewHolder();
                holder.rlContent = (RelativeLayout) row.findViewById(R.id.rl_content);
                holder.retailerName = (TextView) row.findViewById(R.id.retailerNameTv);
                holder.productName = (TextView) row.findViewById(R.id.productName);

                row.setTag(holder);
            } else {
                holder = (ViewHolder) row.getTag();
            }

            holder.retailerName.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
            holder.productName.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));

            RetailerMasterBO retailerBO = bmodel.getRetailerBoByRetailerID().get(salesReport.getRetailerId() + "");

            holder.retailerName.setText(retailerBO.getRetailerName());
            holder.productName.setText(salesReport.getProductName());

            holder.rlContent.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(getActivity(), SalesReturnValidationReport.class);
                    intent.putExtra("RetailerId", salesReport.getRetailerId());
                    intent.putExtra("ProductId", salesReport.getProductId());
                    intent.putExtra("ProductCode", salesReport.getProductCode());
                    startActivity(intent);
                }
            });

            return (row);
        }
    }

    class ViewHolder {
        TextView retailerName, productName;
        RelativeLayout rlContent;
    }
}
