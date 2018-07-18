package com.ivy.cpg.view.reports.asset;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import com.ivy.cpg.view.reports.promotion.RetailerNamesBO;
import com.ivy.sd.png.asean.view.R;
import com.ivy.cpg.view.reports.asset.AssetTrackingBrandBO;
import com.ivy.cpg.view.reports.asset.AssetTrackingReportBO;
import com.ivy.sd.png.bo.RetailerMasterBO;
import com.ivy.sd.png.commons.IvyBaseFragment;
import com.ivy.sd.png.model.BusinessModel;

import java.util.ArrayList;

/**
 * Created by anandasir.v on 8/31/2017.
 * 
 */

public class AssetTrackingReportFragment extends IvyBaseFragment {

    private BusinessModel bmodel;
    private ListView lv;

    private int retailerID = 0;
    private int brandID = 0;
    AssetTrackingReportsHelper assetTrackingReportsHelper;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_report_asset_tracking_report,
                container, false);

        bmodel = (BusinessModel) getActivity().getApplicationContext();
        bmodel.setContext(getActivity());

        lv = view.findViewById(R.id.list);
        Spinner spnBeat = view.findViewById(R.id.spinnerStore);
        Spinner spnChoice = view.findViewById(R.id.spinnerBrand);

        assetTrackingReportsHelper = new AssetTrackingReportsHelper(getContext());
        ArrayList<RetailerNamesBO> assetRetailerList = assetTrackingReportsHelper.downloadAssetTrackingRetailerMaster();
        ArrayList<AssetTrackingBrandBO> assetbrandList=assetTrackingReportsHelper.downloadAssetTrackingBrandMaster();

        ArrayAdapter<RetailerMasterBO> brandAdapter = new ArrayAdapter<>(getActivity(),
                R.layout.call_analysis_spinner_layout);
        brandAdapter.add(new RetailerMasterBO(0, getResources().getString(
                R.string.select)));
        for (int i = 0; i < assetRetailerList.size(); i++) {
            brandAdapter
                    .add(new RetailerMasterBO(assetRetailerList.get(i).getRetailerId(),
                            assetRetailerList.get(i).getRetailerName()));
        }
        brandAdapter
                .setDropDownViewResource(R.layout.call_analysis_spinner_list_item);
        spnBeat.setAdapter(brandAdapter);

        spnBeat.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long l) {
                System.out.println("Executing SpnBeat");
                retailerID = ((RetailerMasterBO) parent
                        .getItemAtPosition(position)).getTretailerId();
                loadData(brandID, retailerID);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        ArrayAdapter<AssetTrackingBrandBO> choiceAdapter = new ArrayAdapter<>(getActivity(),
                R.layout.call_analysis_spinner_layout);
        choiceAdapter.add(new AssetTrackingBrandBO(0, getResources().getString(
                R.string.all)));
        for (int i = 0; i < assetbrandList.size(); i++) {
            choiceAdapter
                    .add(assetbrandList.get(i));
        }
        choiceAdapter
                .setDropDownViewResource(R.layout.call_analysis_spinner_list_item);
        spnChoice.setAdapter(choiceAdapter);

        spnChoice.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long l) {
                System.out.println("Executing SpnChoice");
                brandID = ((AssetTrackingBrandBO) parent
                        .getItemAtPosition(position)).getBrandID();
                loadData(brandID, retailerID);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        return view;
    }

    private void loadData(int brandID, int RetailerID) {
        ArrayList<AssetTrackingReportBO> SFGDataList = assetTrackingReportsHelper.downloadAssetTrackingreport(RetailerID, brandID);
        MyAdapter adapter = new MyAdapter(SFGDataList);
        lv.setAdapter(adapter);
    }

    class ViewHolder {
        AssetTrackingReportBO mAssetTrackingReportBO;
        int position;
        TextView txtAsset;
        TextView txtBrand;
        TextView txtTarget;
        TextView txtActual;
        TextView txtReason;
    }

    private class MyAdapter extends ArrayAdapter<AssetTrackingReportBO> {
        private final ArrayList<AssetTrackingReportBO> items;

        public MyAdapter(ArrayList<AssetTrackingReportBO> items) {
            super(getActivity(), R.layout.row_asset_tracking_report, items);
            this.items = items;
        }

        public AssetTrackingReportBO getItem(int position) {
            return items.get(position);
        }

        public long getItemId(int position) {
            return position;
        }

        public int getCount() {
            return items.size();
        }

        @SuppressLint("SetTextI18n")
        @NonNull
        @Override
        public View getView(int position, View convertView, @NonNull ViewGroup parent) {
            final ViewHolder holder;

            if (convertView == null) {

                holder = new ViewHolder();

                LayoutInflater inflater = LayoutInflater.from(getActivity().getBaseContext());
                convertView = inflater.inflate(R.layout.row_asset_tracking_report, parent, false);

                holder.txtAsset = convertView.findViewById(R.id.txtAsset);
                holder.txtAsset.setMaxLines(bmodel.configurationMasterHelper.MAX_NO_OF_PRODUCT_LINES);
                holder.txtBrand = convertView.findViewById(R.id.txtBrand);
                holder.txtTarget = convertView.findViewById(R.id.txtTarget);
                holder.txtActual = convertView.findViewById(R.id.txtActual);
                holder.txtReason = convertView.findViewById(R.id.txtReason);
                convertView.setTag(holder);

            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            holder.mAssetTrackingReportBO = items.get(position);
            holder.position = position;

            holder.txtAsset.setText(holder.mAssetTrackingReportBO.getAssetDescription() + "");

            holder.txtBrand.setText(holder.mAssetTrackingReportBO.getBrandname() + "");
            holder.txtTarget.setText(holder.mAssetTrackingReportBO.getTarget() + "");
            holder.txtActual.setText(holder.mAssetTrackingReportBO.getActual() + "");
            holder.txtReason.setText(holder.mAssetTrackingReportBO.getReason() + "");

            return convertView;
        }
    }
}
