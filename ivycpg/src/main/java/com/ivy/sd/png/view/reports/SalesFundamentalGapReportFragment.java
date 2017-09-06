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
import com.ivy.sd.png.bo.ReasonMaster;
import com.ivy.sd.png.bo.SalesFundamentalGapReportBO;
import com.ivy.sd.png.model.BusinessModel;

import java.util.ArrayList;

public class SalesFundamentalGapReportFragment extends Fragment {
    private BusinessModel bmodel;
    private ListView lv;
    private int beatID = 0;
    private String choice = "";
    private ArrayAdapter<BeatMasterBO> brandAdapter;
    private ArrayAdapter<String> choiceAdapter;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_report_sale_fundamental_report,
                container, false);

        bmodel = (BusinessModel) getActivity().getApplicationContext();
        bmodel.setContext(getActivity());

        lv = (ListView) view.findViewById(R.id.list);
        Spinner spnBeat = (Spinner) view.findViewById(R.id.spinnerBeat);
        Spinner spnChoice = (Spinner) view.findViewById(R.id.spinnerChoice);

        bmodel.beatMasterHealper.downloadBeats();

        brandAdapter = new ArrayAdapter<>(getActivity(),
                R.layout.call_analysis_spinner_layout);
        brandAdapter.add(new BeatMasterBO(0, getResources().getString(
                R.string.select), 0));
        for (int i = 0; i < bmodel.beatMasterHealper.getBeatMaster().size(); i++) {
            brandAdapter
                    .add(bmodel.beatMasterHealper.getBeatMaster().get(i));
        }
        brandAdapter
                .setDropDownViewResource(R.layout.call_analysis_spinner_list_item);
        spnBeat.setAdapter(brandAdapter);

        spnBeat.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long l) {
                beatID = 0;
                BeatMasterBO beatmasterbo = (BeatMasterBO) parent
                        .getItemAtPosition(position);
                if (beatmasterbo.getBeatId() != 0) {
                    bmodel.beatMasterHealper.setTodayBeatMasterBO(beatmasterbo);
                    beatID = beatmasterbo.getBeatId();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        //------------------------

        choiceAdapter = new ArrayAdapter<>(getActivity(),
                R.layout.call_analysis_spinner_layout);
        choiceAdapter.add(getResources().getString(R.string.select));
        choiceAdapter.add("SOS");
        choiceAdapter.add("SOD");
        choiceAdapter.add("SOSKU");
        choiceAdapter
                .setDropDownViewResource(R.layout.call_analysis_spinner_list_item);
        spnChoice.setAdapter(choiceAdapter);

        spnChoice.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long l) {
                choice = (String) parent
                        .getItemAtPosition(position);
                if (!choice.equals(getResources().getString(R.string.select)) &&
                        beatID != 0) {
                    loadData(beatID, choice);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        //-----------------


        return view;
    }

    private void loadData(int beatId, String filter) {
        ArrayList<SalesFundamentalGapReportBO> SFGDataList = bmodel.reportHelper.downloadSFGreport(beatId, choice);
        if (SFGDataList != null && SFGDataList.size() > 0) {
            MyAdapter adapter = new MyAdapter(SFGDataList);
            lv.setAdapter(adapter);
        }
    }

    class ViewHolder {
        SalesFundamentalGapReportBO mSKUBO;
        int position;
        TextView txtProdName;
        TextView txtsosgap;
        TextView txtsospm;
//        TextView txtsodgap;
//        TextView txtsodpm;
//        TextView txtsoskugap;
//        TextView txtsoskupm;
    }

    private class MyAdapter extends ArrayAdapter<SalesFundamentalGapReportBO> {
        private final ArrayList<SalesFundamentalGapReportBO> items;

        public MyAdapter(ArrayList<SalesFundamentalGapReportBO> items) {
            super(getActivity(), R.layout.row_sfg_report, items);
            this.items = items;
        }

        public SalesFundamentalGapReportBO getItem(int position) {
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
                convertView = inflater.inflate(R.layout.row_sfg_report, parent, false);

                holder.txtProdName = (TextView) convertView.findViewById(R.id.txtProdName);
                holder.txtProdName.setMaxLines(bmodel.configurationMasterHelper.MAX_NO_OF_PRODUCT_LINES);

                holder.txtsosgap = (TextView) convertView.findViewById(R.id.txtsosgap);
                holder.txtsospm = (TextView) convertView.findViewById(R.id.txtsospm);

//                holder.txtsodgap = (TextView) convertView.findViewById(R.id.txtsodgap);
//                holder.txtsodpm = (TextView) convertView.findViewById(R.id.txtsodpm);
//
//                holder.txtsoskugap = (TextView) convertView.findViewById(R.id.txtsoskugap);
//                holder.txtsoskupm = (TextView) convertView.findViewById(R.id.txtsoskupm);

                convertView.setTag(holder);

            } else {
                holder = (ViewHolder) convertView.getTag();
            }

            holder.mSKUBO = items.get(position);
            holder.position = position;

            holder.txtProdName.setText(holder.mSKUBO.getPName() + "");

            holder.txtsosgap.setText(holder.mSKUBO.getGap() + "");
            holder.txtsospm.setText(holder.mSKUBO.getPM() + "");

//            holder.txtsodgap.setText(holder.mSKUBO.getSODGap() + "");
//            holder.txtsodpm.setText(holder.mSKUBO.getSODPM() + "");
//
//            holder.txtsoskugap.setText(holder.mSKUBO.getSOSKUGap() + "");
//            holder.txtsoskupm.setText(holder.mSKUBO.getSOSKUPM() + "");

            return convertView;
        }
    }

}
