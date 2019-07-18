package com.ivy.cpg.view.profile.orderandinvoicehistory;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.ivy.core.IvyConstants;
import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.bo.OrderHistoryBO;
import com.ivy.sd.png.commons.IvyBaseFragment;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.provider.SynchronizationHelper;
import com.ivy.sd.png.util.Commons;
import com.ivy.sd.png.util.DataMembers;
import com.ivy.utils.view.OnSingleClickListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Iterator;
import java.util.Vector;

/**
 * Created by mayuri.v on 9/26/2017.
 */
public class InvoiceHistoryFragment extends IvyBaseFragment {
    private View view;
    private BusinessModel bmodel;
    TextView mavgOrderValue, mavgOrderLines;
    RecyclerView invoiceHistoryList;
    TextView havgOrderLinesTxt, hOrderValueTxt;
    private boolean _hasLoadedOnce = false;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        getActivity().getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        bmodel = (BusinessModel) getActivity().getApplicationContext();
        bmodel.setContext(getActivity());

        view = inflater.inflate(R.layout.fragment_history_new, container,
                false);


        return view;
    }

    private void initializeViews() {
        invoiceHistoryList = view.findViewById(R.id.history_recyclerview);
        havgOrderLinesTxt = view.findViewById(R.id.avg_line_txt);
        hOrderValueTxt = view.findViewById(R.id.avg_val_txt);
        mavgOrderLines = view.findViewById(R.id.avg_lines_val);
        mavgOrderValue = view.findViewById(R.id.history_avg_val);


        invoiceHistoryList.setHasFixedSize(false);
        invoiceHistoryList.setNestedScrollingEnabled(false);
        invoiceHistoryList.setLayoutManager(new LinearLayoutManager(getActivity()));

        try {
            if (bmodel.labelsMasterHelper.applyLabels(view.findViewById(
                    R.id.avg_val_txt).getTag()) != null)
                ((TextView) view.findViewById(R.id.avg_val_txt))
                        .setText(bmodel.labelsMasterHelper
                                .applyLabels(view.findViewById(
                                        R.id.avg_val_txt)
                                        .getTag()));
        } catch (Exception e) {
            Commons.printException(e);
        }

        if (bmodel.configurationMasterHelper.SHOW_INV_ONDEMAND) {
            new OnDemandHistoryDowload().execute();
        } else {
            loadDataToViews();
        }
    }

    private void loadDataToViews() {
        mavgOrderLines.setText(
                bmodel.profilehelper.getP4AvgInvoiceLines() + "");
        mavgOrderValue.setText(
                bmodel.formatValue(bmodel.profilehelper.getP4AvgInvoiceValue()) + "");


        bmodel.profilehelper.downloadInvoiceHistory();
        Vector<OrderHistoryBO> items = bmodel.profilehelper.getParentInvoiceHistory();


        HistoryViewAdapter historyViewAdapter = new HistoryViewAdapter(items);
        invoiceHistoryList.setAdapter(historyViewAdapter);
        if (!(items.size() > 0))
            (view.findViewById(R.id.parentLayout)).setVisibility(View.GONE);
    }

    class OnDemandHistoryDowload extends AsyncTask<String, String, String> {
        JSONObject jsonObject = null;
        private ProgressDialog progressDialogue;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            progressDialogue = ProgressDialog.show(getActivity(),
                    DataMembers.SD, getResources().getString(R.string.download_progress),
                    true, false);
            jsonObject = bmodel.synchronizationHelper.getCommonJsonObject();
            try {
                jsonObject.put("RetailerId", bmodel.getRetailerMasterBO().getRetailerID());
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        @Override
        protected String doInBackground(String... params) {
            bmodel.synchronizationHelper.updateAuthenticateToken(false);
            String response = bmodel.synchronizationHelper.sendPostMethod(bmodel.profilehelper.getInvoiceHistoryUrl("P4ORDERHISTORYMASTER"), jsonObject);
            try {
                JSONObject headerObject = new JSONObject(response);
                Iterator itr = headerObject.keys();
                while (itr.hasNext()) {
                    String key = (String) itr.next();
                    if (key.equals(SynchronizationHelper.ERROR_CODE)) {
                        String errorCode = headerObject.getString(key);
                        if (errorCode.equals(IvyConstants.AUTHENTICATION_SUCCESS_CODE)) {
                            bmodel.synchronizationHelper
                                    .parseJSONAndInsert(headerObject, false);
                        }
                    }
                }

                response = bmodel.synchronizationHelper.sendPostMethod(bmodel.profilehelper.getInvoiceHistoryUrl("P4ORDERHISTORYDETAIL"), jsonObject);
                headerObject = new JSONObject(response);
                itr = headerObject.keys();
                while (itr.hasNext()) {
                    String key = (String) itr.next();
                    if (key.equals(SynchronizationHelper.ERROR_CODE)) {
                        String errorCode = headerObject.getString(key);
                        if (errorCode.equals(IvyConstants.AUTHENTICATION_SUCCESS_CODE)) {
                            bmodel.synchronizationHelper
                                    .parseJSONAndInsert(headerObject, false);
                        }
                        return errorCode;
                    }
                }
            } catch (JSONException jsonExpection) {
                Commons.print(jsonExpection.getMessage());
            }
            return "E01";
        }

        @Override
        protected void onPostExecute(String errorCode) {
            super.onPostExecute(errorCode);
            progressDialogue.dismiss();
            if (bmodel.synchronizationHelper.getAuthErroCode().equals(IvyConstants.AUTHENTICATION_SUCCESS_CODE)) {
                if (errorCode
                        .equals(IvyConstants.AUTHENTICATION_SUCCESS_CODE)) {
                    loadDataToViews();

                } else {
                    String errorMessage = bmodel.synchronizationHelper
                            .getErrormessageByErrorCode().get(errorCode);
                    if (errorMessage != null) {
                        bmodel.showAlert(errorMessage, 0);
                    }
                }
            } else {
                String errorMsg = bmodel.synchronizationHelper.getErrormessageByErrorCode().get(bmodel.synchronizationHelper.getAuthErroCode());
                if (errorMsg != null) {
                    Toast.makeText(getActivity(), errorMsg, Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getActivity(), getResources().getString(R.string.data_not_downloaded), Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    @Override
    public void setUserVisibleHint(boolean isFragmentVisible_) {
        super.setUserVisibleHint(isFragmentVisible_);


        if (this.isVisible()) {
            // we check that the fragment is becoming visible
            isFragmentVisible_ = false;
            if (!isFragmentVisible_ && !_hasLoadedOnce) {
                //run your async task here since the user has just focused on your fragment
                initializeViews();
                _hasLoadedOnce = true;

            }
        }
    }


    public class HistoryViewAdapter extends RecyclerView.Adapter<HistoryViewAdapter.ViewHolder> {

        double balanceAmt;
        private Vector<OrderHistoryBO> items;

        public HistoryViewAdapter(Vector<OrderHistoryBO> items) {
            this.items = items;
        }


        @Override
        public HistoryViewAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.custom_invoice_history_list, parent, false);

            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(HistoryViewAdapter.ViewHolder holder, int position) {
            final OrderHistoryBO projectObj = items.get(position);

            if (position % 2 == 0)
                holder.listBgLayout.setBackgroundColor(ContextCompat.getColor(getActivity(), R.color.white));
            else
                holder.listBgLayout.setBackgroundColor(ContextCompat.getColor(getActivity(), R.color.white));

            holder.invoice_Id.setText(projectObj.getInvoiceId());
            holder.order_Id.setText(projectObj.getOrderid());
            holder.invDate.setText(projectObj.getOrderdate());
            holder.totLines.setText(projectObj.getLpc() + "");
            holder.totVal.setText(bmodel.formatValue(projectObj.getOrderValue()));
            holder.totalVolume.setText(projectObj.getVolume() == null ? "0" : projectObj.getVolume());
            holder.marginPrice.setText(bmodel.formatValue(projectObj.getMarginValue()));
            holder.marginPer.setText(bmodel.formatPercent(projectObj.getMaginPerc()));

            holder.del_date_val.setText(projectObj.getDueDate());
            holder.invoice_date_val.setText(bmodel.formatValue(projectObj.getOutStandingAmt()));
            holder.invoice_qty_val.setText(projectObj.getOverDueDays());
            if (projectObj.getOutStandingAmt() > 0) {
                holder.del_rep_code_val.setText(getResources().getString(R.string.open));
                holder.del_rep_code_val.setTextColor(ContextCompat.getColor(getActivity(), R.color.RED));
            } else {
                holder.del_rep_code_val.setText(getResources().getString(R.string.close));
                holder.del_rep_code_val.setTextColor(ContextCompat.getColor(getActivity(), R.color.green_productivity));
            }
            Vector<OrderHistoryBO> historyDetailList = null;
            if (bmodel.profilehelper.getChild_invoiceHistoryList() != null && bmodel.configurationMasterHelper.SHOW_INVOICE_HISTORY_DETAIL) {
                historyDetailList = bmodel.profilehelper.getChild_invoiceHistoryList()
                        .get(position);
                if (historyDetailList != null && historyDetailList.size() > 0 && (historyDetailList.get(0).getCaseQty() > 0 || historyDetailList.get(0).getPcsQty() > 0 || historyDetailList.get(0).getOuterQty() > 0))
                    holder.invViewLayout.setVisibility(View.VISIBLE);
                else
                    holder.invViewLayout.setVisibility(View.GONE);
            } else {
                holder.invViewLayout.setVisibility(View.GONE);
            }
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {


            private LinearLayout listBgLayout, invViewLayout;
            private TextView invoice_Id, invDate, totLines, totVal, totalVolume, marginPrice, marginPer;
            private TextView del_date_val, invoice_date_val, invoice_qty_val, del_rep_code_val;
            private TextView order_Id;


            public ViewHolder(View itemView) {
                super(itemView);

                invViewLayout = itemView.findViewById(R.id.inv_view_layout);
                listBgLayout = itemView.findViewById(R.id.list_background);

                order_Id = itemView.findViewById(R.id.ord_id_val);
                invoice_Id = itemView.findViewById(R.id.order_id_val);
                invDate = itemView.findViewById(R.id.date_val);
                totLines = itemView.findViewById(R.id.tota_lines_val);
                totVal = itemView.findViewById(R.id.tot_val);
                totalVolume = itemView.findViewById(R.id.tota_Volume_val);
                marginPrice = itemView.findViewById(R.id.margin_price_val);
                marginPer = itemView.findViewById(R.id.margin_per_val);

                del_date_val = itemView.findViewById(R.id.del_date_val);
                invoice_date_val = itemView.findViewById(R.id.invoice_date_val);
                invoice_qty_val = itemView.findViewById(R.id.invoice_qty_val);
                del_rep_code_val = itemView.findViewById(R.id.del_rep_code_val);


                if (bmodel.configurationMasterHelper.SHOW_INVOICE_HISTORY_DETAIL) {
                    itemView.setClickable(true);
                    itemView.setOnClickListener(new OnSingleClickListener() {
                        @Override
                        public void onSingleClick(View v) {
                            Intent intent = new Intent(getActivity(), HistoryDetailActivity.class);
                            intent.putExtra("selected_list_id", getLayoutPosition());
                            intent.putExtra("from", "InvoiceHistory");
                            startActivity(intent);
                        }
                    });
                } else {
                    invViewLayout.setVisibility(View.GONE);
                    itemView.setClickable(false);
                    itemView.setOnClickListener(null);
                }

                try {
                    if (bmodel.labelsMasterHelper.applyLabels(itemView.findViewById(
                            R.id.order_id_txt).getTag()) != null)
                        ((TextView) itemView.findViewById(R.id.order_id_txt))
                                .setText(bmodel.labelsMasterHelper
                                        .applyLabels(itemView.findViewById(
                                                R.id.order_id_txt)
                                                .getTag()));
                    if (bmodel.labelsMasterHelper.applyLabels(itemView.findViewById(
                            R.id.ord_id_txt).getTag()) != null)
                        ((TextView) itemView.findViewById(R.id.ord_id_txt))
                                .setText(bmodel.labelsMasterHelper
                                        .applyLabels(itemView.findViewById(
                                                R.id.ord_id_txt)
                                                .getTag()));
                } catch (Exception ex) {
                    Commons.printException(ex);
                }

                if (!bmodel.configurationMasterHelper.SHOW_INV_HST_ORDERID) {
                    itemView.findViewById(R.id.order_id_layout).setVisibility(View.GONE);
                }
                if (!bmodel.configurationMasterHelper.SHOW_INV_HST_INVOICEDATE) {
                    itemView.findViewById(R.id.date_layout).setVisibility(View.GONE);
                }
                if (!bmodel.configurationMasterHelper.SHOW_INV_HST_INVOICEAMOUNT) {
                    itemView.findViewById(R.id.tot_val_layout).setVisibility(View.GONE);
                }
                if (!bmodel.configurationMasterHelper.SHOW_INV_HST_TOT_LINES) {
                    itemView.findViewById(R.id.total_lines_layout).setVisibility(View.GONE);
                }
                if (!bmodel.configurationMasterHelper.SHOW_INV_HST_DUEDATE) {
                    itemView.findViewById(R.id.del_date_layout).setVisibility(View.GONE);
                }
                if (!bmodel.configurationMasterHelper.SHOW_INV_HST_OVERDUE_DAYS) {
                    itemView.findViewById(R.id.invoice_qty_layout).setVisibility(View.GONE);
                }
                if (!bmodel.configurationMasterHelper.SHOW_INV_HST_OS_AMOUNT) {
                    itemView.findViewById(R.id.invoice_date_layout).setVisibility(View.GONE);
                }
                if (!bmodel.configurationMasterHelper.SHOW_INV_HST_STATUS) {
                    itemView.findViewById(R.id.del_rep_code_layout).setVisibility(View.GONE);
                }

                if (!bmodel.configurationMasterHelper.SHOW_INV_HST_VOLUME) {
                    itemView.findViewById(R.id.total_volume_layout).setVisibility(View.GONE);
                }
                if (!bmodel.configurationMasterHelper.SHOW_INV_HST_MARGIN_PRICE) {
                    itemView.findViewById(R.id.ll_margin_price).setVisibility(View.GONE);
                }

                if (!bmodel.configurationMasterHelper.SHOW_INV_HST_MARGIN_PER) {
                    itemView.findViewById(R.id.ll_margin_per).setVisibility(View.GONE);
                }


            }
        }
    }


}

