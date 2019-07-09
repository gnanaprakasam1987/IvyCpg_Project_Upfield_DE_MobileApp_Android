package com.ivy.cpg.view.profile.orderandinvoicehistory;

import android.content.Intent;
import android.content.res.TypedArray;
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

import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.bo.OrderHistoryBO;
import com.ivy.sd.png.commons.IvyBaseFragment;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.util.Commons;
import com.ivy.utils.view.OnSingleClickListener;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Vector;

/**
 * Created by nivetha.s on 26-10-2015.
 */
public class OrderHistoryFragment extends IvyBaseFragment {
    private View view;
    private BusinessModel bmodel;
    private TypedArray typearr;
    //ExpandableListView orderHistoryListView;
    //LinearLayout historyLayout;
    //TableLayout orderHistoryFooterValues;
    TextView mavgOrderValue, mavgOrderLines, mpastordereddays;
    SimpleDateFormat sdf;
    RecyclerView orderHistoryList;
    TextView havgOrderLinesTxt, hOrderValueTxt;
    private HistoryViewAdapter historyViewAdapter;
    private boolean _hasLoadedOnce = false;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        getActivity().getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        bmodel = (BusinessModel) getActivity().getApplicationContext();
        bmodel.setContext(getActivity());
        //typearr = getActivity().getTheme().obtainStyledAttributes(R.styleable.MyTextView);

        view = inflater.inflate(R.layout.fragment_history_new, container,
                false);


        return view;
    }

    private void initializeViews() {
        orderHistoryList = view.findViewById(R.id.history_recyclerview);
        havgOrderLinesTxt = view.findViewById(R.id.avg_line_txt);
        hOrderValueTxt = view.findViewById(R.id.avg_val_txt);
        mavgOrderLines = view.findViewById(R.id.avg_lines_val);
        mavgOrderValue = view.findViewById(R.id.history_avg_val);

        orderHistoryList.setHasFixedSize(false);
        orderHistoryList.setNestedScrollingEnabled(false);
        orderHistoryList.setLayoutManager(new LinearLayoutManager(getActivity()));

        sdf = new SimpleDateFormat("yyyy/MM/dd");


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

        mavgOrderLines.setText(
                bmodel.profilehelper.getP4AvgOrderLines() + "");
        mavgOrderValue.setText(
                bmodel.formatValue(bmodel.profilehelper.getP4AvgOrderValue()) + "");


        // comment check
//        mpastordereddays.setText(bmodel.profilehelper.getP4AvgOrderLines()
//                + "");

        bmodel.profilehelper.getOSAmtandInvoiceCount(
                bmodel.getRetailerMasterBO().getRetailerID(),
                bmodel.getRetailerMasterBO().getRetailerCode());

        bmodel.profilehelper.downloadOrderHistory();
        Vector<OrderHistoryBO> items = bmodel.profilehelper.getParentOrderHistory();
        int siz = items.size();
        Vector<OrderHistoryBO> mylist = new Vector<>();
        int numcount = 1;
        for (int i = 0; i < siz; ++i) {
            OrderHistoryBO ret = items.elementAt(i);
            if (ret.getRetailerId().equals(
                    bmodel.getRetailerMasterBO().getRetailerID())) {
                ret.setNumid(numcount);
                numcount = numcount + 1;
                mylist.add(ret);
            }
        }

        historyViewAdapter = new HistoryViewAdapter(mylist);
        orderHistoryList.setAdapter(historyViewAdapter);

        if (!(items.size() > 0))
            ((LinearLayout) view.findViewById(R.id.parentLayout)).setVisibility(View.GONE);
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

        private Vector<OrderHistoryBO> items;

        public HistoryViewAdapter(Vector<OrderHistoryBO> items) {
            this.items = items;
        }


        @Override
        public HistoryViewAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.custom_history_list, parent, false);

            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(HistoryViewAdapter.ViewHolder holder, int position) {
            final OrderHistoryBO projectObj = items.get(position);

            if (position % 2 == 0)
                holder.listBgLayout.setBackgroundColor(ContextCompat.getColor(getActivity(), R.color.white));
            else
                holder.listBgLayout.setBackgroundColor(ContextCompat.getColor(getActivity(), R.color.white));

            holder.orderId.setText(projectObj.getOrderid());
            holder.orderDate.setText(projectObj.getOrderdate());
            holder.totLines.setText(projectObj.getLpc() + "");
            holder.totVal.setText(bmodel.formatValue(projectObj.getOrderValue()));

            holder.del_date_val.setText(projectObj.getRF1());
            holder.invoice_date_val.setText(projectObj.getRF2());
            holder.invoice_qty_val.setText(projectObj.getRF3());
            holder.del_rep_code_val.setText(projectObj.getRF4());
            holder.totVol.setText("" + projectObj.getVolume());
            holder.deliveryStatus_val.setText(projectObj.getDelieveryStatus());
            holder.paidAmtVal.setText(String.valueOf(projectObj.getPaidAmount()));
            holder.balAmtVal.setText(String.valueOf(projectObj.getBalanceAmount()));
            holder.cust_pono_val.setText(projectObj.getPoNumber());
            holder.delivery_date_val.setText(projectObj.getDelDate());
            holder.driver_name_val.setText(projectObj.getDriverName());
            holder.del_docno_val.setText(projectObj.getDelDocNum());


            try {
                Calendar c = Calendar.getInstance();
                c.setTime(sdf.parse(projectObj.getOrderdate()));
                c.add(Calendar.DATE, bmodel.retailerMasterBO.getCreditDays());
                holder.due_date_val.setText(sdf.format(c.getTime()));
            } catch (ParseException e) {
                Commons.printException(e);
                holder.due_date_val.setText("");
            }

        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {

            private final LinearLayout del_date_layout, invoice_date_layout, invoice_qty_layout, del_rep_code_layout;
            private LinearLayout listBgLayout, invViewLayout;
            private TextView orderId, orderDate, totLines, totVal, totVol, paidAmtVal, balAmtVal;
            private TextView del_date_val, invoice_date_val, invoice_qty_val, del_rep_code_val, due_date_val;
            private LinearLayout tot_val_layout, tot_vol_layout, del_status_layout, start_date_layout, due_date_layout, paid_amt_layout, bal_amt_layout;
            private LinearLayout driver_name_layout, del_docno_layout, cust_pono_layout, delivery_date_layout;
            private TextView deliveryStatus_val;
            private TextView driver_name_val, del_docno_val, cust_pono_val, delivery_date_val;

            public ViewHolder(View itemView) {
                super(itemView);

                invViewLayout = itemView.findViewById(R.id.inv_view_layout);
                due_date_layout = itemView.findViewById(R.id.due_date_layout);
                listBgLayout = itemView.findViewById(R.id.list_background);


                orderId = itemView.findViewById(R.id.order_id_val);
                orderDate = itemView.findViewById(R.id.date_val);
                totLines = itemView.findViewById(R.id.tota_lines_val);
                totVal = itemView.findViewById(R.id.tot_val);
                totVol = itemView.findViewById(R.id.tot_vol);

                del_date_val = itemView.findViewById(R.id.del_date_val);
                invoice_date_val = itemView.findViewById(R.id.invoice_date_val);
                invoice_qty_val = itemView.findViewById(R.id.invoice_qty_val);
                del_rep_code_val = itemView.findViewById(R.id.del_rep_code_val);
                due_date_val = itemView.findViewById(R.id.due_date_val);


                del_date_layout = itemView.findViewById(R.id.del_date_layout);
                invoice_date_layout = itemView.findViewById(R.id.invoice_date_layout);
                invoice_qty_layout = itemView.findViewById(R.id.invoice_qty_layout);
                del_rep_code_layout = itemView.findViewById(R.id.del_rep_code_layout);
                tot_val_layout = itemView.findViewById(R.id.tot_val_layout);
                tot_vol_layout = itemView.findViewById(R.id.tot_vol_layout);
                del_status_layout = itemView.findViewById(R.id.del_status_layout);
                start_date_layout = itemView.findViewById(R.id.date_layout);
                deliveryStatus_val = itemView.findViewById(R.id.deliveryStatusValue);
                paid_amt_layout = itemView.findViewById(R.id.paid_amt_layout);
                paidAmtVal = itemView.findViewById(R.id.paid_amt_val);
                bal_amt_layout = itemView.findViewById(R.id.bal_amt_layout);
                balAmtVal = itemView.findViewById(R.id.bal_amt_val);

                driver_name_layout = itemView.findViewById(R.id.driver_name_layout);
                del_docno_layout = itemView.findViewById(R.id.del_docno_layout);
                cust_pono_layout = itemView.findViewById(R.id.cust_pono_layout);
                delivery_date_layout = itemView.findViewById(R.id.delivery_date_layout);

                driver_name_val = itemView.findViewById(R.id.driver_name_val);
                del_docno_val = itemView.findViewById(R.id.del_docno_val);
                cust_pono_val = itemView.findViewById(R.id.cust_pono_val);
                delivery_date_val = itemView.findViewById(R.id.delivery_date_val);

                itemView.setOnClickListener(new OnSingleClickListener() {
                    @Override
                    public void onSingleClick(View v) {
                        if (bmodel.configurationMasterHelper.SHOW_ORDER_HISTORY_DETAILS) {
                            Intent intent = new Intent(getActivity(), HistoryDetailActivity.class);
                            intent.putExtra("selected_list_id", getLayoutPosition());
                            intent.putExtra("from", "OrderHistory");
                            startActivity(intent);
                        }
                    }
                });
                //label for order id
                try {
                    if (bmodel.labelsMasterHelper.applyLabels(itemView.findViewById(
                            R.id.order_id_txt).getTag()) != null)
                        ((TextView) itemView.findViewById(R.id.order_id_txt))
                                .setText(bmodel.labelsMasterHelper
                                        .applyLabels(itemView.findViewById(
                                                R.id.order_id_txt)
                                                .getTag()));

                    if (bmodel.labelsMasterHelper.applyLabels(itemView.findViewById(
                            R.id.date_txt).getTag()) != null)
                        ((TextView) itemView.findViewById(R.id.date_txt))
                                .setText(bmodel.labelsMasterHelper
                                        .applyLabels(itemView.findViewById(
                                                R.id.date_txt)
                                                .getTag()));

                    if (bmodel.labelsMasterHelper.applyLabels(itemView.findViewById(
                            R.id.driver_name_txt).getTag()) != null)
                        ((TextView) itemView.findViewById(R.id.driver_name_txt))
                                .setText(bmodel.labelsMasterHelper
                                        .applyLabels(itemView.findViewById(
                                                R.id.driver_name_txt)
                                                .getTag()));

                    if (bmodel.labelsMasterHelper.applyLabels(itemView.findViewById(
                            R.id.cust_pono_txt).getTag()) != null)
                        ((TextView) itemView.findViewById(R.id.cust_pono_txt))
                                .setText(bmodel.labelsMasterHelper
                                        .applyLabels(itemView.findViewById(
                                                R.id.cust_pono_txt)
                                                .getTag()));

                    if (bmodel.labelsMasterHelper.applyLabels(itemView.findViewById(
                            R.id.del_docno_txt).getTag()) != null)
                        ((TextView) itemView.findViewById(R.id.del_docno_txt))
                                .setText(bmodel.labelsMasterHelper
                                        .applyLabels(itemView.findViewById(
                                                R.id.del_docno_txt)
                                                .getTag()));

                    if (bmodel.labelsMasterHelper.applyLabels(itemView.findViewById(
                            R.id.del_date_txt).getTag()) != null)
                        ((TextView) itemView.findViewById(R.id.del_date_txt))
                                .setText(bmodel.labelsMasterHelper
                                        .applyLabels(itemView.findViewById(
                                                R.id.del_date_txt)
                                                .getTag()));

                    if (bmodel.labelsMasterHelper.applyLabels(itemView.findViewById(
                            R.id.invoice_date_txt).getTag()) != null)
                        ((TextView) itemView.findViewById(R.id.invoice_date_txt))
                                .setText(bmodel.labelsMasterHelper
                                        .applyLabels(itemView.findViewById(
                                                R.id.invoice_date_txt)
                                                .getTag()));

                    if (bmodel.labelsMasterHelper.applyLabels(itemView.findViewById(
                            R.id.invoice_qty_txt).getTag()) != null)
                        ((TextView) itemView.findViewById(R.id.invoice_qty_txt))
                                .setText(bmodel.labelsMasterHelper
                                        .applyLabels(itemView.findViewById(
                                                R.id.invoice_qty_txt)
                                                .getTag()));

                    if (bmodel.labelsMasterHelper.applyLabels(itemView.findViewById(
                            R.id.del_rep_code_txt).getTag()) != null)
                        ((TextView) itemView.findViewById(R.id.del_rep_code_txt))
                                .setText(bmodel.labelsMasterHelper
                                        .applyLabels(itemView.findViewById(
                                                R.id.del_rep_code_txt)
                                                .getTag()));


                    if (bmodel.labelsMasterHelper.applyLabels(itemView.findViewById(
                            R.id.delivery_date_txt).getTag()) != null)
                        ((TextView) itemView.findViewById(R.id.delivery_date_txt))
                                .setText(bmodel.labelsMasterHelper
                                        .applyLabels(itemView.findViewById(
                                                R.id.delivery_date_txt)
                                                .getTag()));
                } catch (Exception ex) {
                    Commons.printException(ex);
                }

                if (bmodel.configurationMasterHelper.SHOW_HST_DELDATE) {
                    del_date_layout.setVisibility(View.VISIBLE);
                    delivery_date_layout.setVisibility(View.VISIBLE);
                } else {
                    del_date_layout.setVisibility(View.GONE);
                    delivery_date_layout.setVisibility(View.GONE);
                }
                if (bmodel.configurationMasterHelper.SHOW_HST_INVDATE) {
                    invoice_date_layout.setVisibility(View.VISIBLE);
                } else {
                    invoice_date_layout.setVisibility(View.GONE);
                }
                if (bmodel.configurationMasterHelper.SHOW_HST_INVQTY) {
                    invoice_qty_layout.setVisibility(View.VISIBLE);
                } else {
                    invoice_qty_layout.setVisibility(View.GONE);
                }
                if (bmodel.configurationMasterHelper.SHOW_HST_REPCODE) {
                    del_rep_code_layout.setVisibility(View.VISIBLE);
                } else {
                    del_rep_code_layout.setVisibility(View.GONE);
                }
                if (bmodel.configurationMasterHelper.SHOW_HST_TOTAL) {
                    tot_val_layout.setVisibility(View.VISIBLE);
                } else {
                    tot_val_layout.setVisibility(View.GONE);
                }
                if (bmodel.configurationMasterHelper.SHOW_HST_VOLUM) {
                    tot_vol_layout.setVisibility(View.VISIBLE);
                } else {
                    tot_vol_layout.setVisibility(View.GONE);
                }
                if (bmodel.configurationMasterHelper.SHOW_HST_DELSTATUS) {
                    del_status_layout.setVisibility(View.VISIBLE);
                } else {
                    del_status_layout.setVisibility(View.GONE);
                }
                if (bmodel.configurationMasterHelper.SHOW_HST_STARTDATE) {
                    start_date_layout.setVisibility(View.VISIBLE);
                } else {
                    start_date_layout.setVisibility(View.GONE);
                }
                if (bmodel.configurationMasterHelper.SHOW_HST_DUETDATE) {
                    due_date_layout.setVisibility(View.VISIBLE);
                } else {
                    due_date_layout.setVisibility(View.GONE);
                }
                if (bmodel.configurationMasterHelper.SHOW_ORDER_HISTORY_DETAILS) {
                    invViewLayout.setVisibility(View.VISIBLE);
                } else {
                    invViewLayout.setVisibility(View.GONE);
                }
                if (bmodel.configurationMasterHelper.SHOW_HST_PAID_AMOUNT) {
                    paid_amt_layout.setVisibility(View.VISIBLE);
                } else {
                    paid_amt_layout.setVisibility(View.GONE);
                }
                if (bmodel.configurationMasterHelper.SHOW_HST_BAL_AMOUNT) {
                    bal_amt_layout.setVisibility(View.VISIBLE);
                } else {
                    bal_amt_layout.setVisibility(View.GONE);
                }
                if (bmodel.configurationMasterHelper.SHOW_HST_DRIVER_NAME) {
                    driver_name_layout.setVisibility(View.VISIBLE);
                } else {
                    driver_name_layout.setVisibility(View.GONE);
                }
                if (bmodel.configurationMasterHelper.SHOW_HST_PO_NUM) {
                    cust_pono_layout.setVisibility(View.VISIBLE);
                } else {
                    cust_pono_layout.setVisibility(View.GONE);
                }
                if (bmodel.configurationMasterHelper.SHOW_HST_DOC_NO) {
                    del_docno_layout.setVisibility(View.VISIBLE);
                } else {
                    del_docno_layout.setVisibility(View.GONE);
                }
            }
        }
    }

}
