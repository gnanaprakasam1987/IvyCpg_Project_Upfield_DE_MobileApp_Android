package com.ivy.sd.png.view.reports.beginstockreport;

import android.app.AlertDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.bo.StockReportMasterBO;
import com.ivy.sd.png.commons.IvyBaseFragment;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.util.Commons;

import java.util.Vector;

public class BeginningStockFragment extends IvyBaseFragment implements BeginningStockAdapter.BeginningStockAdapterCallback {
    private BusinessModel bModel;
    private ListView lvWpList;
    private TextView productName;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initializeBusinessModel();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_begining_stock, container,
                false);


        try {
            if (bModel.labelsMasterHelper.applyLabels(view.findViewById(
                    R.id.caseTitle).getTag()) != null)
                ((TextView) view.findViewById(R.id.caseTitle))
                        .setText(bModel.labelsMasterHelper.applyLabels(view
                                .findViewById(R.id.caseTitle).getTag()));
            if (bModel.labelsMasterHelper.applyLabels(view.findViewById(
                    R.id.pcsTitle).getTag()) != null)
                ((TextView) view.findViewById(R.id.pcsTitle))
                        .setText(bModel.labelsMasterHelper.applyLabels(view
                                .findViewById(R.id.pcsTitle).getTag()));
        } catch (Exception e) {
            Commons.printException(e);
        }

        if (bModel.userMasterHelper.getUserMasterBO().getUserid() == 0) {
            Toast.makeText(getActivity(), getResources().getString(R.string.sessionout_loginagain),
                    Toast.LENGTH_SHORT).show();
            getActivity().finish();
        }

        lvWpList = view.findViewById(R.id.list);
        lvWpList.setCacheColorHint(0);


        productName = view.findViewById(R.id.productName);

        productName.setOnTouchListener(new View.OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                int inType = productName.getInputType();
                productName.setInputType(InputType.TYPE_NULL);
                productName.onTouchEvent(event);
                productName.setInputType(inType);
                return true;
            }
        });

        new DownloadBeginingStock().execute();

        if (!bModel.configurationMasterHelper.SHOW_ORDER_CASE) {
            view.findViewById(R.id.caseTitle).setVisibility(View.GONE);
        } else {
            try {
                if (bModel.labelsMasterHelper.applyLabels(view.findViewById(
                        R.id.caseTitle).getTag()) != null)
                    ((TextView) view.findViewById(R.id.caseTitle))
                            .setText(bModel.labelsMasterHelper.applyLabels(view
                                    .findViewById(R.id.caseTitle).getTag()));
            } catch (Exception e) {
                Commons.printException(e);
            }
        }
        if (!bModel.configurationMasterHelper.SHOW_ORDER_PCS) {
            view.findViewById(R.id.pcsTitle).setVisibility(View.GONE);
        } else {
            try {
                if (bModel.labelsMasterHelper.applyLabels(view.findViewById(
                        R.id.pcsTitle).getTag()) != null)
                    ((TextView) view.findViewById(R.id.pcsTitle))
                            .setText(bModel.labelsMasterHelper.applyLabels(view
                                    .findViewById(R.id.pcsTitle).getTag()));
            } catch (Exception e) {
                Commons.printException(e);
            }
        }

        try {
            if (bModel.labelsMasterHelper.applyLabels(view.findViewById(
                    R.id.totaltitle).getTag()) != null)
                ((TextView) view.findViewById(R.id.totaltitle))
                        .setText(bModel.labelsMasterHelper.applyLabels(view
                                .findViewById(R.id.totaltitle).getTag()));
        } catch (Exception e) {
            Commons.printException(e);
        }
        if (!bModel.configurationMasterHelper.SHOW_OUTER_CASE)
            view.findViewById(R.id.outerTitle).setVisibility(View.GONE);


        return view;

    }

    private void initializeBusinessModel() {
        bModel = (BusinessModel) getActivity().getApplicationContext();
        bModel.setContext(getActivity());
    }

    @Override
    public void productName(String pName) {
        productName.setText(pName);
    }


    class DownloadBeginingStock extends AsyncTask<Integer, Integer, Boolean> {

        private AlertDialog.Builder builder;
        private AlertDialog alertDialog;
        Vector<StockReportMasterBO> mylist;

        protected void onPreExecute() {
            builder = new AlertDialog.Builder(getActivity());
            customProgressDialog(builder, getResources().getString(R.string.loading));
            alertDialog = builder.create();
            alertDialog.show();
        }

        @Override
        protected Boolean doInBackground(Integer... params) {
            try {
                mylist = bModel.stockreportmasterhelper
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
            alertDialog.dismiss();
            BeginningStockAdapter mSchedule = new BeginningStockAdapter(mylist, getActivity(), bModel);
            mSchedule.setBeginningStockAdapterCallback(BeginningStockFragment.this);
            lvWpList.setAdapter(mSchedule);
        }

    }
}
