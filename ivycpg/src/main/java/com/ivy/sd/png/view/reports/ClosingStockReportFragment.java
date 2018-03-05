package com.ivy.sd.png.view.reports;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.bo.ProductMasterBO;
import com.ivy.sd.png.bo.RetailerMasterBO;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.provider.ConfigurationMasterHelper;
import com.ivy.sd.png.util.Commons;

import java.util.ArrayList;
import java.util.Vector;

public class ClosingStockReportFragment extends Fragment {

    BusinessModel bmodel;
    ListView lvwplist;
    LinearLayout linearLayout;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        getActivity().getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        View view = inflater.inflate(R.layout.closing_stk_report_fragment,
                container, false);
        bmodel = (BusinessModel) getActivity().getApplicationContext();
        bmodel.setContext(getActivity());

        bmodel.reportHelper.downloadClosingStock();

        Vector<RetailerMasterBO> items = new Vector<>();
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
        linearLayout = (LinearLayout)view.findViewById(R.id.orderScreenListRow);
        lvwplist = (ListView) view.findViewById(R.id.lvwpList);
        lvwplist.setCacheColorHint(0);

        if (!bmodel.configurationMasterHelper.SHOW_SHELF_OUTER
                && !bmodel.configurationMasterHelper.SHOW_STOCK_SP
                && !bmodel.configurationMasterHelper.SHOW_STOCK_SC) {
            view.findViewById(R.id.case_qty).setVisibility(View.GONE);
            view.findViewById(R.id.piece_qty).setVisibility(View.GONE);
            view.findViewById(R.id.outer_qty).setVisibility(View.GONE);

        }

        if (!bmodel.configurationMasterHelper.SHOW_SHELF_OUTER)
            view.findViewById(R.id.outer_qty).setVisibility(View.GONE);

        if (!bmodel.configurationMasterHelper.SHOW_STOCK_SP)
            view.findViewById(R.id.piece_qty).setVisibility(View.GONE);

        if (!bmodel.configurationMasterHelper.SHOW_STOCK_SC)
            view.findViewById(R.id.case_qty).setVisibility(View.GONE);


        try {
            ((TextView) view.findViewById(R.id.skucode)).setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
            if (bmodel.labelsMasterHelper.applyLabels(view.findViewById(R.id.skucode).getTag()) != null)
                ((TextView)view.findViewById(R.id.skucode))
                        .setText(bmodel.labelsMasterHelper
                                .applyLabels(view.findViewById(
                                        R.id.skucode).getTag()));

            ((TextView) view.findViewById(R.id.skuname)).setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
            if (bmodel.labelsMasterHelper.applyLabels(view.findViewById(R.id.skuname).getTag()) != null)
                ((TextView)view.findViewById(R.id.skuname))
                        .setText(bmodel.labelsMasterHelper
                                .applyLabels(view.findViewById(
                                        R.id.skuname).getTag()));

            ((TextView) view.findViewById(R.id.piece_qty)).setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
            if (bmodel.labelsMasterHelper.applyLabels(view.findViewById(R.id.piece_qty).getTag()) != null)
                ((TextView)view.findViewById(R.id.piece_qty))
                        .setText(bmodel.labelsMasterHelper
                                .applyLabels(view.findViewById(
                                        R.id.piece_qty).getTag()));

        } catch (Exception e) {
            Commons.printException(e + "");
        }



        ArrayAdapter<RetailerMasterBO> retailerAdapter = new ArrayAdapter<>(
                getActivity(), android.R.layout.simple_spinner_item);
        retailerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

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

        ArrayList<ProductMasterBO> closingStkList = new ArrayList<>();
        try {
            closingStkList = bmodel.reportHelper.getClosingStkReport(retailerId + "");
        }catch(Exception e){
            Commons.printException(e);
        }

        if (closingStkList == null || closingStkList.size() == 0) {
            bmodel.showAlert(getResources().getString(R.string.no_products_exists), 0);
            linearLayout.setVisibility(View.GONE);
            lvwplist.setVisibility(View.GONE);
            return;
        }

        linearLayout.setVisibility(View.VISIBLE);
        lvwplist.setVisibility(View.VISIBLE);

        MyAdapter listDatas = new MyAdapter(closingStkList);
        lvwplist.setAdapter(listDatas);
    }

    class MyAdapter extends ArrayAdapter {

        private ArrayList<ProductMasterBO> items;

        public MyAdapter(ArrayList<ProductMasterBO> items) {
            super(getActivity(), R.layout.row_closing_stk_report, items);
            this.items = items;
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            final ViewHolder holder;
            ProductMasterBO product = items.get(position);
            View row = convertView;
            if (row == null) {
                LayoutInflater inflater = getActivity().getLayoutInflater();
                row = inflater
                        .inflate(R.layout.row_closing_stk_report, parent, false);
                holder = new ViewHolder();

                if (!bmodel.configurationMasterHelper.SHOW_SHELF_OUTER
                        && !bmodel.configurationMasterHelper.SHOW_STOCK_SP
                        && !bmodel.configurationMasterHelper.SHOW_STOCK_SC) {
                    holder.caseQty.setVisibility(View.GONE);
                    holder.pieceQty.setVisibility(View.GONE);
                    holder.outerQty.setVisibility(View.GONE);

                }

                if (!bmodel.configurationMasterHelper.SHOW_SHELF_OUTER)
                    holder.outerQty.findViewById(R.id.outer_qty).setVisibility(View.GONE);

                if (!bmodel.configurationMasterHelper.SHOW_STOCK_SP)
                    holder.pieceQty.findViewById(R.id.piece_qty).setVisibility(View.GONE);

                if (!bmodel.configurationMasterHelper.SHOW_STOCK_SC)
                    holder.caseQty.findViewById(R.id.case_qty).setVisibility(View.GONE);

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
            holder.pname.setText(product.getProductName());
            holder.prodcode.setText(product.getProductCode()+"");

            holder.caseQty.setVisibility(View.GONE);
            holder.outerQty.setVisibility(View.GONE);


//            holder.caseQty.setText(product.getCsCase()+"");
//            holder.outerQty.setText(product.getCsOuter()+"");

            int total = 0;
            if(product.getCsPiece() > 0)
                total = product.getCsPiece();
            if(product.getCsCase() > 0)
                total = total+(product.getCsCase()*product.getCaseSize());
            if(product.getCsOuter() > 0)
                total = total+(product.getCsOuter()*product.getOutersize());

            holder.pieceQty.setText(total+"");

//            Commons.print(total+"");

            return (row);
        }

        public ProductMasterBO getItem(int position) {
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
