package com.ivy.sd.png.view.reports;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.bo.ChildLevelBo;
import com.ivy.sd.png.bo.StockReportBO;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.util.Commons;

import java.util.ArrayList;
import java.util.Vector;

@Deprecated
public class CurrentStockView extends Fragment {
    /**
     * Called when the activity is first created.
     */
    private ListView lvwplist;
    private TextView productName;
    private BusinessModel bmodel;
    //  private Vector<StockReportBO> mylist;


    private void initializeBusinessModel() {
        bmodel = (BusinessModel) getActivity().getApplicationContext();
        bmodel.setContext(getActivity());

    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        getActivity().getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        View view = inflater.inflate(R.layout.fragment_stock_report, container,
                false);

        initializeBusinessModel();

        try {
            if (bmodel.labelsMasterHelper.applyLabels(view.findViewById(R.id.sihtitle).getTag()) != null)
                ((TextView) view.findViewById(R.id.sihtitle)).setText(bmodel.labelsMasterHelper.applyLabels(view
                        .findViewById(R.id.sihtitle).getTag()));
        } catch (Exception e1) {
            Commons.printException("" + e1);
        }

        if (bmodel.userMasterHelper.getUserMasterBO().getUserid() == 0) {
            Toast.makeText(getActivity(), getResources().getString(R.string.sessionout_loginagain), Toast.LENGTH_SHORT).show();
            getActivity().finish();
        }

        if (bmodel.configurationMasterHelper.IS_EOD_STOCK_SPLIT) {
            view.findViewById(R.id.sihCaseTitle).setVisibility(View.VISIBLE);
            view.findViewById(R.id.sihOuterTitle).setVisibility(View.VISIBLE);
        } else {
            view.findViewById(R.id.sihCaseTitle).setVisibility(View.GONE);
            view.findViewById(R.id.sihOuterTitle).setVisibility(View.GONE);
        }


        Spinner spinnerbrand = view.findViewById(R.id.brandSpinner);

        productName = view.findViewById(R.id.productName);
        productName.setOnTouchListener(new OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                int inType = productName.getInputType();
                productName.setInputType(InputType.TYPE_NULL);
                productName.onTouchEvent(event);
                productName.setInputType(inType);
//                productName.performClick();
                return true;
            }


        });


        lvwplist = view.findViewById(R.id.list);
        lvwplist.setCacheColorHint(0);

        Vector<ChildLevelBo> items = new Vector<>();
        int siz = 0;
        try {
            items = bmodel.productHelper.getChildLevelBo();
            siz = items.size();
            if (siz == 0)
                return view;
        } catch (Exception e) {
            // TODO Auto-generated catch block
            Commons.printException(e);
        }

        //bmodel.productHelper.loadProducts("MENU_LOAD_MANAGEMENT", "");

        final Vector<StockReportBO> mylist = bmodel.reportHelper.downloadCurrentStockReport();
        bmodel.reportHelper.updateBaseUOM("ORDER", 2);

        ArrayAdapter<ChildLevelBo> childAdapter = new ArrayAdapter<>(
                getActivity(), android.R.layout.simple_spinner_item);
        childAdapter
                .setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        childAdapter.add(new ChildLevelBo(0, 0, getResources().getString(
                R.string.all)));
        for (int i = 0; i < siz; ++i) {
            childAdapter.add(items.elementAt(i));
        }
        spinnerbrand.setAdapter(childAdapter);
        spinnerbrand.setOnItemSelectedListener(new OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent, View view,
                                       int position, long id) {
                ChildLevelBo temp = (ChildLevelBo) parent.getSelectedItem();
                updateStockReportGrid(temp.getProductid(), mylist);
                productName.setText("");
            }

            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        return view;
    }

    private void updateStockReportGrid(int brandId, Vector<StockReportBO> mylist) {

        if (mylist == null) {
            bmodel.showAlert(
                    getResources().getString(R.string.no_products_exists), 0);
            return;
        }
        int siz = mylist.size();
        ArrayList<StockReportBO> temp = new ArrayList<>();
        for (int i = 0; i < siz; ++i) {
            StockReportBO ret = mylist.get(i);
            if (brandId == 0 || brandId == ret.getBrandId()) {
                temp.add(ret);
            }
        }
        MyAdapter mSchedule = new MyAdapter(temp);
        lvwplist.setAdapter(mSchedule);
    }


    private class MyAdapter extends ArrayAdapter<StockReportBO> {
        private ArrayList<StockReportBO> items;

        public MyAdapter(ArrayList<StockReportBO> items) {
            super(getActivity(), R.layout.row_stock_report, items);
            this.items = items;
        }

        public StockReportBO getItem(int position) {
            return items.get(position);
        }

        public long getItemId(int position) {
            return position;
        }

        public int getCount() {
            return items.size();
        }

        @NonNull
        public View getView(int position, View convertView, @NonNull ViewGroup parent) {
            final ViewHolder holder;
            StockReportBO product = items.get(position);
            View row = convertView;
            if (row == null) {
                LayoutInflater inflater = getActivity().getLayoutInflater();
                row = inflater
                        .inflate(R.layout.row_stock_report, parent, false);
                holder = new ViewHolder();

                holder.psname = row.findViewById(R.id.orderPRODNAME);
                holder.psname.setMaxLines(bmodel.configurationMasterHelper.MAX_NO_OF_PRODUCT_LINES);
                holder.sih = row.findViewById(R.id.sih);
                holder.sihCase = row.findViewById(R.id.sih_case);
                holder.sihOuter = row.findViewById(R.id.sih_outer);
                holder.prodcode = row.findViewById(R.id.prdcode);
                row.setOnClickListener(new OnClickListener() {
                    public void onClick(View v) {
                        productName.setText(holder.pname);
                    }
                });

                if (bmodel.configurationMasterHelper.IS_EOD_STOCK_SPLIT) {
                    holder.sihCase.setVisibility(View.VISIBLE);
                    holder.sihOuter.setVisibility(View.VISIBLE);
                } else {
                    holder.sihCase.setVisibility(View.GONE);
                    holder.sihOuter.setVisibility(View.GONE);
                }
                row.setTag(holder);
            } else {
                holder = (ViewHolder) row.getTag();
            }

            holder.ref = position;
            holder.psname.setText(product.getProductShortName());
            holder.pname = product.getProductName();
            if (bmodel.configurationMasterHelper.IS_EOD_STOCK_SPLIT) {
                boolean isUomWiseSplitted = false;
                int rem_sih = 0;
                int totalQty = product.getSih();

                if (product.isBaseUomCaseWise() && product.getCaseSize() != 0) {
                    isUomWiseSplitted = true;

                    holder.sihCase.setText(String.valueOf(totalQty / product.getCaseSize()));
                    rem_sih = totalQty % product.getCaseSize();
                }
                if (product.isBaseUomOuterWise() && product.getOuterSize() != 0) {
                    if (isUomWiseSplitted) {
                        holder.sihOuter.setText(String.valueOf(rem_sih / product.getOuterSize()));
                        rem_sih = rem_sih % product.getOuterSize();
                    } else {
                        isUomWiseSplitted = true;
                        holder.sihOuter.setText(String.valueOf(totalQty / product.getOuterSize()));
                        rem_sih = totalQty % product.getOuterSize();
                    }
                }

                if (isUomWiseSplitted) {
                    holder.sih.setText(String.valueOf(rem_sih));
                } else {
                    holder.sih.setText(String.valueOf(product.getSih()));
                }

            } else {
                holder.sih.setText(String.valueOf(product.getSih()));
            }

            holder.prodcode.setText(product.getProductCode());

            return (row);
        }
    }

    class ViewHolder {
        private String pname;
        private TextView psname, sih, sihCase, sihOuter, prodcode;
        int ref;
    }

}