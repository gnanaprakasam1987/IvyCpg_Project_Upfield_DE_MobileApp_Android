package com.ivy.sd.png.view.reports;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.bo.BeatMasterBO;
import com.ivy.sd.png.bo.PromotionTrackingReportBO;
import com.ivy.sd.png.bo.ReasonMaster;
import com.ivy.sd.png.bo.RetailerMasterBO;
import com.ivy.sd.png.bo.SalesFundamentalGapReportBO;
import com.ivy.sd.png.model.BusinessModel;

import java.util.ArrayList;

public class PromotionTrackingReport extends Fragment {
    private BusinessModel bmodel;
    private ListView lv;
    private int retailerID = 0;
    private ArrayAdapter<RetailerMasterBO> brandAdapter;
    private ArrayList<RetailerMasterBO> retailerMasterList;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_report_promotion_tracking_report,
                container, false);

        bmodel = (BusinessModel) getActivity().getApplicationContext();
        bmodel.setContext(getActivity());

        lv = (ListView) view.findViewById(R.id.list);
        Spinner spnBeat = (Spinner) view.findViewById(R.id.spinnerBeat);

        retailerMasterList = bmodel.reportHelper.downloadPromotionTrackingRetailerMaster();

        brandAdapter = new ArrayAdapter<>(getActivity(),
                R.layout.call_analysis_spinner_layout);
        brandAdapter.add(new RetailerMasterBO(0, getResources().getString(
                R.string.select)));
        for (int i = 0; i < retailerMasterList.size(); i++) {
            brandAdapter
                    .add(new RetailerMasterBO(Integer.parseInt(retailerMasterList.get(i).getRetailerID()),
                            retailerMasterList.get(i).getRetailerName()));
        }
        brandAdapter
                .setDropDownViewResource(R.layout.call_analysis_spinner_list_item);
        spnBeat.setAdapter(brandAdapter);

        spnBeat.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long l) {
                retailerID = ((RetailerMasterBO) parent
                        .getItemAtPosition(position)).getTretailerId();
                loadData(retailerID);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        return view;
    }

    private void loadData(int retailerId) {
        ArrayList<PromotionTrackingReportBO> SFGDataList = bmodel.reportHelper.downloadPromotionTrackingreport(retailerId);
        if (SFGDataList != null && SFGDataList.size() > 0) {
            MyAdapter adapter = new MyAdapter(SFGDataList);
            lv.setAdapter(adapter);
        }
    }

    class ViewHolder {
        PromotionTrackingReportBO mPromotionTrackingReportBO;
        int position;
        TextView txtProdName;
        TextView txtPromoName;
        TextView txtisExecuted, txtHasAnnouncer, txtReason;
    }

    private class MyAdapter extends ArrayAdapter<PromotionTrackingReportBO> {
        private final ArrayList<PromotionTrackingReportBO> items;

        public MyAdapter(ArrayList<PromotionTrackingReportBO> items) {
            super(getActivity(), R.layout.row_promotion_tracking_report, items);
            this.items = items;
        }

        public PromotionTrackingReportBO getItem(int position) {
            return items.get(position);
        }

        public long getItemId(int position) {
            return position;
        }

        public int getCount() {
            return items.size();
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            final ViewHolder holder;

            if (convertView == null) {

                holder = new ViewHolder();

                LayoutInflater inflater = LayoutInflater.from(getActivity().getBaseContext());
                convertView = inflater.inflate(R.layout.row_promotion_tracking_report, parent, false);

                holder.txtProdName = (TextView) convertView.findViewById(R.id.txtProdName);
                holder.txtProdName.setMaxLines(bmodel.configurationMasterHelper.MAX_NO_OF_PRODUCT_LINES);

                holder.txtPromoName = (TextView) convertView.findViewById(R.id.txtPromoName);
                holder.txtisExecuted = (TextView) convertView.findViewById(R.id.txtisExecuted);
                holder.txtHasAnnouncer = (TextView) convertView.findViewById(R.id.txthasAnnouncer);
                holder.txtReason = (TextView) convertView.findViewById(R.id.txtReason);

                convertView.setTag(holder);

            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            holder.mPromotionTrackingReportBO = items.get(position);
            holder.position = position;

            holder.txtProdName.setText(holder.mPromotionTrackingReportBO.getBrandName() + "");

            holder.txtPromoName.setText(holder.mPromotionTrackingReportBO.getPromoName() + "");
            holder.txtisExecuted.setText(holder.mPromotionTrackingReportBO.getIsExecuted() + "");
            holder.txtHasAnnouncer.setText(holder.mPromotionTrackingReportBO.getHasAnnouncer() + "");
            holder.txtReason.setText(holder.mPromotionTrackingReportBO.getReason() + "");

//            holder.txtsodgap.setText(holder.mSKUBO.getSODGap() + "");
//            holder.txtsodpm.setText(holder.mSKUBO.getSODPM() + "");
//
//            holder.txtsoskugap.setText(holder.mSKUBO.getSOSKUGap() + "");
//            holder.txtsoskupm.setText(holder.mSKUBO.getSOSKUPM() + "");

            return convertView;
        }
    }

}

