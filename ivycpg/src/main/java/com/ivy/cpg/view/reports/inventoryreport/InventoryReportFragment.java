package com.ivy.cpg.view.reports.inventoryreport;

import android.os.Bundle;
import androidx.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.bo.ConfigureBO;
import com.ivy.sd.png.bo.RetailerMasterBO;
import com.ivy.sd.png.commons.IvyBaseFragment;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.provider.ConfigurationMasterHelper;

import java.util.ArrayList;
import java.util.Vector;

/**
 * Created by rajkumar.s on 8/23/2017.
 */

public class InventoryReportFragment extends IvyBaseFragment {

    private View view;
    private BusinessModel bmodel;
    private Spinner spnRetailers;
    private Spinner spnType;
    private ArrayAdapter<RetailerMasterBO> spinnerAdapter;
    private ArrayAdapter<ConfigureBO> inventoryTypeAdapter;
    private ListView listView;
    private ArrayList<InventoryBO_Proj> lstData;
    private InventoryReportHelper inventoryReportHelper;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        getActivity().getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);


        view = inflater.inflate(R.layout.fragment_inventory_report, container, false);

        bmodel = (BusinessModel) getActivity().getApplicationContext();
        bmodel.setContext(getActivity());


        if (bmodel.userMasterHelper.getUserMasterBO().getUserid() == 0) {
            Toast.makeText(getActivity(),
                    getResources().getString(R.string.sessionout_loginagain),
                    Toast.LENGTH_SHORT).show();
            getActivity().finish();
        }

        inventoryReportHelper=new InventoryReportHelper(getContext(),bmodel);

        spnRetailers= view.findViewById(R.id.spn_retailer);
        spnType= view.findViewById(R.id.spn_type);
        listView= view.findViewById(R.id.list);

        spinnerAdapter = new ArrayAdapter<>(getActivity(),
                R.layout.spinner_bluetext_layout);
        spinnerAdapter
                .setDropDownViewResource(R.layout.spinner_bluetext_list_item);

        for(RetailerMasterBO bo:bmodel.getRetailerMaster()){
            if(bo.getIsToday()==1) {
                spinnerAdapter.add(new RetailerMasterBO(bo.getRetailerID(), bo.getRetailerName()));
            }
        }
        spnRetailers.setAdapter(spinnerAdapter);
        spnRetailers.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if(inventoryTypeAdapter.getCount()>0) {
                    lstData = inventoryReportHelper.downloadInventoryReport(spinnerAdapter.getItem(i).getRetailerID(),
                            inventoryTypeAdapter.getItem(spnType.getSelectedItemPosition()).getConfigCode());
                refreshLsit();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        Vector<ConfigureBO> filterList=bmodel.configurationMasterHelper.downloadFilterList();
        inventoryTypeAdapter= new ArrayAdapter<>(getActivity(),
                R.layout.spinner_bluetext_layout);
        inventoryTypeAdapter
                .setDropDownViewResource(R.layout.spinner_bluetext_list_item);
        // Filt11 and Filt12 reused for inventory and sbd inventory
        for(ConfigureBO configureBO:filterList){
            if(configureBO.getConfigCode().equalsIgnoreCase("Filt11")
                    ||configureBO.getConfigCode().equalsIgnoreCase("Filt12")){
                inventoryTypeAdapter.add(configureBO);
            }

        }
        spnType.setAdapter(inventoryTypeAdapter);
        spnType.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {

                if(spnRetailers.getCount()>0) {
                    lstData = inventoryReportHelper.downloadInventoryReport(spinnerAdapter.getItem(spnRetailers.getSelectedItemPosition()).getRetailerID(),
                            inventoryTypeAdapter.getItem(i).getConfigCode());
                refreshLsit();  }

            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        ((TextView) view.findViewById(R.id.tv_outlet)).setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
        ((TextView) view.findViewById(R.id.tv_type)).setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));

        ((TextView) view.findViewById(R.id.tvProductNameTitle)).setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
        ((TextView) view.findViewById(R.id.tv_header_available)).setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
        ((TextView) view.findViewById(R.id.tv_header_reason)).setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));

        return view;
    }

    private void refreshLsit(){
        MyAdapter adapter=new MyAdapter(lstData);
        listView.setAdapter(adapter);
    }
    class ViewHolder {
        TextView tv_productName,tv_availability,tv_reasonDesc;

    }

    class MyAdapter extends ArrayAdapter<InventoryBO_Proj> {
        ArrayList<InventoryBO_Proj> items;

        private MyAdapter(ArrayList<InventoryBO_Proj> items) {
            super(getActivity(), R.layout.row_order_report, items);
            this.items = items;
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            final ViewHolder holder;

            InventoryBO_Proj inventoryBo =  items
                    .get(position);
            View row = convertView;

            if (row == null) {
                LayoutInflater inflater = getActivity().getLayoutInflater();
                row = inflater
                        .inflate(R.layout.row_inventory_report, parent, false);
                holder = new ViewHolder();
                holder.tv_productName = row.findViewById(R.id.tv_productname);
                holder.tv_availability = row.findViewById(R.id.tv_availability);
                holder.tv_reasonDesc = row.findViewById(R.id.tv_reason);

                holder.tv_productName.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
                holder.tv_availability.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
                holder.tv_reasonDesc.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));


                row.setTag(holder);
            } else {
                holder = (ViewHolder) row.getTag();
            }



            holder.tv_productName.setText(inventoryBo.getProductName());
            holder.tv_productName.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));

            holder.tv_availability.setText(inventoryBo.getAvailability());
            holder.tv_reasonDesc.setText(inventoryBo.getReasonDesc());

            holder.tv_availability.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
            holder.tv_reasonDesc.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));


            return (row);
        }
    }

}
