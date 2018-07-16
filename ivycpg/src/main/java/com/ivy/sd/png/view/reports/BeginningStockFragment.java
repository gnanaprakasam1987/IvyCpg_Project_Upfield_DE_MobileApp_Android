package com.ivy.sd.png.view.reports;

import android.app.AlertDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.bo.StockReportMasterBO;
import com.ivy.sd.png.commons.IvyBaseFragment;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.util.Commons;

import java.util.Vector;

public class BeginningStockFragment extends IvyBaseFragment {

    private ListView lvwplist;
    private BusinessModel bmodel;
    private TextView productname;


    // private Vector<StockReportMasterBO> mylist;
    //  private View view;

    private void initializeBusinessModel() {
        bmodel = (BusinessModel) getActivity().getApplicationContext();
        bmodel.setContext(getActivity());

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_begining_stock, container,
                false);

        initializeBusinessModel();
        try {
            if (bmodel.labelsMasterHelper.applyLabels(view.findViewById(
                    R.id.caseTitle).getTag()) != null)
                ((TextView) view.findViewById(R.id.caseTitle))
                        .setText(bmodel.labelsMasterHelper.applyLabels(view
                                .findViewById(R.id.caseTitle).getTag()));
            if (bmodel.labelsMasterHelper.applyLabels(view.findViewById(
                    R.id.pcsTitle).getTag()) != null)
                ((TextView) view.findViewById(R.id.pcsTitle))
                        .setText(bmodel.labelsMasterHelper.applyLabels(view
                                .findViewById(R.id.pcsTitle).getTag()));
        } catch (Exception e) {
            Commons.printException(e);
        }

        if (bmodel.userMasterHelper.getUserMasterBO().getUserid() == 0) {
            Toast.makeText(getActivity(), getResources().getString(R.string.sessionout_loginagain),
                    Toast.LENGTH_SHORT).show();
            getActivity().finish();
        }

        lvwplist = view.findViewById(R.id.list);
        lvwplist.setCacheColorHint(0);

        productname = view.findViewById(R.id.productName);

        productname.setOnTouchListener(new OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                int inType = productname.getInputType();
                productname.setInputType(InputType.TYPE_NULL);
                productname.onTouchEvent(event);
                productname.setInputType(inType);
                return true;
            }
        });

        new DownloadBeginingStock().execute();

        if (!bmodel.configurationMasterHelper.SHOW_ORDER_CASE) {
            view.findViewById(R.id.caseTitle).setVisibility(View.GONE);
        } else {
            try {
                if (bmodel.labelsMasterHelper.applyLabels(view.findViewById(
                        R.id.caseTitle).getTag()) != null)
                    ((TextView) view.findViewById(R.id.caseTitle))
                            .setText(bmodel.labelsMasterHelper.applyLabels(view
                                    .findViewById(R.id.caseTitle).getTag()));
            } catch (Exception e) {
                Commons.printException(e);
            }
        }
        if (!bmodel.configurationMasterHelper.SHOW_ORDER_PCS) {
            view.findViewById(R.id.pcsTitle).setVisibility(View.GONE);
        } else {
            try {
                if (bmodel.labelsMasterHelper.applyLabels(view.findViewById(
                        R.id.pcsTitle).getTag()) != null)
                    ((TextView) view.findViewById(R.id.pcsTitle))
                            .setText(bmodel.labelsMasterHelper.applyLabels(view
                                    .findViewById(R.id.pcsTitle).getTag()));
            } catch (Exception e) {
                Commons.printException(e);
            }
        }

        try {
            if (bmodel.labelsMasterHelper.applyLabels(view.findViewById(
                    R.id.totaltitle).getTag()) != null)
                ((TextView) view.findViewById(R.id.totaltitle))
                        .setText(bmodel.labelsMasterHelper.applyLabels(view
                                .findViewById(R.id.totaltitle).getTag()));
        } catch (Exception e) {
            Commons.printException(e);
        }
        if (!bmodel.configurationMasterHelper.SHOW_OUTER_CASE)
            view.findViewById(R.id.outerTitle).setVisibility(View.GONE);

        return view;

    }

    // private StockReportMasterBO product;

    private class MyAdapter extends ArrayAdapter<StockReportMasterBO> {
        private Vector<StockReportMasterBO> items;

        public MyAdapter(Vector<StockReportMasterBO> items) {
            super(getActivity(), R.layout.row_begining_stock_listview, items);
            this.items = items;
        }

        public StockReportMasterBO getItem(int position) {
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
            StockReportMasterBO product = items.get(position);

            View row = convertView;
            if (row == null) {
                LayoutInflater inflater = getActivity().getLayoutInflater();
                row = inflater.inflate(R.layout.row_begining_stock_listview,
                        parent, false);
                holder = new ViewHolder();
                holder.psname = row.findViewById(R.id.productname);
                holder.caseqty = row.findViewById(R.id.caseqty);
                holder.pcsqty = row.findViewById(R.id.pieceqty);
                holder.unitprice = row.findViewById(R.id.unitprice);
                holder.outerqty = row.findViewById(R.id.outerqty);
                row.setOnClickListener(new OnClickListener() {
                    public void onClick(View v) {
                        productname.setText(holder.pname);
                    }
                });

                if (!bmodel.configurationMasterHelper.SHOW_ORDER_CASE)
                    holder.caseqty.setVisibility(View.GONE);
                if (!bmodel.configurationMasterHelper.SHOW_ORDER_PCS)
                    holder.pcsqty.setVisibility(View.GONE);
                if (!bmodel.configurationMasterHelper.SHOW_OUTER_CASE)
                    holder.outerqty.setVisibility(View.GONE);

                row.setTag(holder);
            } else {
                holder = (ViewHolder) row.getTag();
            }

            holder.psname.setText(product.getProductShortName());
            holder.caseqty.setText(String.valueOf(product.getCaseQuantity()));
            holder.pcsqty.setText(String.valueOf(product.getPieceQuantity()));
            holder.outerqty.setText(String.valueOf(product.getOuterQty()));
            holder.pname = product.getProductName();
            double unitprice = (product.getCaseQuantity() * product.getCaseSize() + product
                    .getPieceQuantity()) * product.getBasePrice();
            holder.unitprice.setText(bmodel.formatValue(unitprice));
            return row;
        }
    }

    class ViewHolder {
        TextView psname, caseqty, pcsqty, unitprice, outerqty;
        String pname;
    }

    class DownloadBeginingStock extends AsyncTask<Integer, Integer, Boolean> {

        //private ProgressDialog progressDialogue;
        private AlertDialog.Builder builder;
        private AlertDialog alertDialog;
        Vector<StockReportMasterBO> mylist;

        protected void onPreExecute() {
            /*progressDialogue = ProgressDialog.show(getActivity(),
                    DataMembers.SD, "Loading", true, false);*/
            builder = new AlertDialog.Builder(getActivity());

            customProgressDialog(builder, getResources().getString(R.string.loading));
            alertDialog = builder.create();
            alertDialog.show();
        }

        @Override
        protected Boolean doInBackground(Integer... params) {
            try {
                mylist = bmodel.stockreportmasterhelper
                        .downloadBeginingStockReport();
                Commons.print("size" + mylist.size());
            } catch (Exception e) {
                Commons.printException(e);
                return Boolean.FALSE;
            }
            return Boolean.TRUE; // Return your real result here
        }

        protected void onProgressUpdate(Integer... progress) {

        }

        protected void onPostExecute(Boolean result) {
            //progressDialogue.dismiss();
            alertDialog.dismiss();
            MyAdapter mSchedule = new MyAdapter(mylist);
            lvwplist.setAdapter(mSchedule);
        }

    }
}
