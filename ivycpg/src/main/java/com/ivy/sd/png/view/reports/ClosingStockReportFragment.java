package com.ivy.sd.png.view.reports;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.bo.ChildLevelBo;
import com.ivy.sd.png.bo.LoadManagementBO;
import com.ivy.sd.png.bo.RetailerMasterBO;
import com.ivy.sd.png.bo.StockReportBO;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.provider.ConfigurationMasterHelper;
import com.ivy.sd.png.util.Commons;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Vector;

public class ClosingStockReportFragment extends Fragment {

    BusinessModel bmodel;
    ListView lvwplist;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        getActivity().getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        View view = inflater.inflate(R.layout.closing_stk_report_fragment,
                container, false);
        bmodel = (BusinessModel) getActivity().getApplicationContext();
        bmodel.setContext(getActivity());

        bmodel.reportHelper.getRetailers();

        Vector<RetailerMasterBO> items = new Vector<RetailerMasterBO>();
        int siz = 0;
        try {
            items = bmodel.reportHelper.getRetailerMaster();
            siz = items.size();
            if (siz == 0)
                return view;
        } catch (Exception e) {
            Commons.printException(e);
        }

        Spinner spinnerbrand = (Spinner) view.findViewById(R.id.spn_retailer_closing_stk);
        lvwplist = (ListView) view.findViewById(R.id.lvwpList);
        lvwplist.setCacheColorHint(0);

        ArrayAdapter<RetailerMasterBO> retailerAdapter = new ArrayAdapter<RetailerMasterBO>(
                getActivity(), android.R.layout.simple_spinner_item);
        retailerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        retailerAdapter.add(new RetailerMasterBO(0, getResources().getString(R.string.select)));

        for (int i = 0; i < siz; ++i) {
            retailerAdapter.add(items.elementAt(i));
        }
        spinnerbrand.setAdapter(retailerAdapter);

        spinnerbrand.setAdapter(retailerAdapter);
        spinnerbrand.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent, View view,
                                       int position, long id) {
                RetailerMasterBO reBo = (RetailerMasterBO) parent.getSelectedItem();
                updateStockReportGrid(reBo.getTretailerId());
            }

            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        return view;
    }

    private void updateStockReportGrid(int retailerId) {

        ArrayList<LoadManagementBO> loadManagementBOs = bmodel.reportHelper.downloadClosingStock(retailerId);

        if (loadManagementBOs == null || loadManagementBOs.size() == 0) {
            bmodel.showAlert(getResources().getString(R.string.no_products_exists), 0);
            return;
        }

        MyAdapter listDatas = new MyAdapter(loadManagementBOs);
        lvwplist.setAdapter(listDatas);
    }

    private LoadManagementBO product;
    class MyAdapter extends ArrayAdapter {

        private ArrayList<LoadManagementBO> items;

        public MyAdapter(ArrayList<LoadManagementBO> items) {
            super(getActivity(), R.layout.row_closing_stk_report, items);
            this.items = items;
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            final ViewHolder holder;
            product =  items.get(position);
            View row = convertView;
            if (row == null) {
                LayoutInflater inflater = getActivity().getLayoutInflater();
                row = inflater
                        .inflate(R.layout.row_closing_stk_report, parent, false);
                holder = new ViewHolder();

                holder.pname = (TextView) row.findViewById(R.id.skuname);
                holder.pieceQty = (TextView) row.findViewById(R.id.piece_qty);
                holder.caseQty = (TextView) row.findViewById(R.id.case_qty);
                holder.outerQty = (TextView) row.findViewById(R.id.outer_qty);
                holder.prodcode = (TextView) row.findViewById(R.id.skucode);
                row.setTag(holder);
            } else {
                holder = (ViewHolder) row.getTag();
            }

            holder.ref = position;
            holder.pname.setText(product.getProductname());
            holder.pieceQty.setText(product.getPieceqty()+"");
            holder.caseQty.setText(product.getCaseqty()+"");
            holder.outerQty.setText(product.getOuterQty()+"");
            holder.prodcode.setText(product.getProductid()+"");

            return (row);
        }

        public LoadManagementBO getItem(int position) {
            return items.get(position);
        }

        public long getItemId(int position) {
            return position;
        }

        public int getCount() {
            return items.size();
        }
    }

    class ViewHolder {
        private TextView pname, pieceQty, caseQty, outerQty, prodcode;
        int ref;
    }
}
