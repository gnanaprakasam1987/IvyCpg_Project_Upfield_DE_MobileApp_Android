package com.ivy.sd.png.view.profile;

import android.content.Intent;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
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
import com.ivy.sd.png.provider.ConfigurationMasterHelper;
import com.ivy.sd.png.util.Commons;

import java.util.Vector;

/**
 * Created by mayuri.v on 9/26/2017.
 */
public class InvoiceHistoryFragment extends IvyBaseFragment {
    private View view;
    private BusinessModel bmodel;
    private TypedArray typearr;
    TextView mavgOrderValue, mavgOrderLines;
    RecyclerView invoiceHistoryList;
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

        view = inflater.inflate(R.layout.fragment_history_new, container,
                false);


        return view;
    }

    private void initializeViews() {
        invoiceHistoryList = (RecyclerView) view.findViewById(R.id.history_recyclerview);
        havgOrderLinesTxt = (TextView) view.findViewById(R.id.avg_line_txt);
        hOrderValueTxt = (TextView) view.findViewById(R.id.avg_val_txt);
        mavgOrderLines = (TextView) view.findViewById(R.id.avg_lines_val);
        mavgOrderValue = (TextView) view.findViewById(R.id.history_avg_val);

        havgOrderLinesTxt.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
        hOrderValueTxt.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
        mavgOrderLines.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.THIN));
        mavgOrderValue.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.THIN));

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

        mavgOrderLines.setText(
                bmodel.profilehelper.getP4AvgInvoiceLines() + "");
        mavgOrderValue.setText(
                bmodel.formatValue(bmodel.profilehelper.getP4AvgInvoiceValue()) + "");


//        bmodel.profilehelper.getOSAmtandInvoiceCount(
//                bmodel.getRetailerMasterBO().getRetailerID(),
//                bmodel.getRetailerMasterBO().getRetailerCode());

        bmodel.profilehelper.downloadInvoiceHistory();
        Vector<OrderHistoryBO> items = bmodel.profilehelper.getParentInvoiceHistory();

//        int siz = items.size();
//        Vector<OrderHistoryBO> mylist = new Vector<>();
//        int numcount = 1;
//        for (int i = 0; i < siz; ++i) {
//            OrderHistoryBO ret = items.elementAt(i);
//            if (ret.getRetailerId().equals(
//                    bmodel.getRetailerMasterBO().getRetailerID())) {
//                ret.setNumid(numcount);
//                numcount = numcount + 1;
//                mylist.add(ret);
//            }
//        }

        historyViewAdapter = new HistoryViewAdapter(items);
        invoiceHistoryList.setAdapter(historyViewAdapter);
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
                holder.listBgLayout.setBackgroundColor(ContextCompat.getColor(getActivity(), R.color.history_list_bg));

            holder.invoice_Id.setText(projectObj.getInvoiceId());
            holder.order_Id.setText(projectObj.getOrderid());
            holder.invDate.setText(projectObj.getOrderdate());
            holder.totLines.setText(projectObj.getLpc() + "");
            holder.totVal.setText(bmodel.formatValue(projectObj.getOrderValue()));

            holder.del_date_val.setText(projectObj.getDueDate());
            holder.invoice_date_val.setText(bmodel.formatValue(projectObj.getOutStandingAmt()));
            holder.invoice_qty_val.setText(projectObj.getOverDueDays());
            if (projectObj.getOutStandingAmt() > 0) {
                holder.del_rep_code_val.setText(getResources().getString(R.string.open));
                holder.del_rep_code_val.setTextColor(ContextCompat.getColor(getActivity(), R.color.RED));
            } else {
                holder.del_rep_code_val.setText(getResources().getString(R.string.close));
                holder.del_rep_code_val.setTextColor(ContextCompat.getColor(getActivity(), R.color.GREEN));
            }
            Vector<OrderHistoryBO> historyDetailList = null;
            if (bmodel.profilehelper.getChild_invoiceHistoryList() != null && bmodel.configurationMasterHelper.SHOW_HISTORY_DETAIL) {
                historyDetailList = bmodel.profilehelper.getChild_invoiceHistoryList()
                        .get(position);
                if (historyDetailList != null && historyDetailList.size() > 0 && (historyDetailList.get(0).getCaseQty() > 0 || historyDetailList.get(0).getPcsQty() > 0 || historyDetailList.get(0).getOuterQty() > 0))
                    holder.invViewLayout.setVisibility(View.VISIBLE);
                else
                    holder.invViewLayout.setVisibility(View.GONE);
            } else {
                holder.invViewLayout.setVisibility(View.GONE);
            }

            /*try {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd");
                Calendar c = Calendar.getInstance();
                c.setTime(sdf.parse(projectObj.getOrderdate()));
                holder.invDate.setText(DateUtil.convertDateObjectToRequestedFormat(
                        sdf.parse(sdf.format(c.getTime())), bmodel.configurationMasterHelper.outDateFormat));
            } catch (ParseException e) {
                Commons.printException(e);
                holder.invDate.setText("");
            }
*/
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {

            private final TextView del_date_txt, invoice_date_txt, invoice_qty_txt, del_rep_code_txt;
            private LinearLayout listBgLayout, invViewLayout;
            private TextView invoice_Id_label, dateTxt, totLinesTxt, totValTxt;
            private TextView invoice_Id, invDate, totLines, totVal;
            private TextView invViewBtn, del_date_val, invoice_date_val, invoice_qty_val, del_rep_code_val;
            private TextView order_Id, order_Id_label;

            public ViewHolder(View itemView) {
                super(itemView);

                invViewLayout = (LinearLayout) itemView.findViewById(R.id.inv_view_layout);
                listBgLayout = (LinearLayout) itemView.findViewById(R.id.list_background);
                invoice_Id_label = (TextView) itemView.findViewById(R.id.order_id_txt);
                order_Id_label = (TextView) itemView.findViewById(R.id.ord_id_txt);
                dateTxt = (TextView) itemView.findViewById(R.id.date_txt);
                totLinesTxt = (TextView) itemView.findViewById(R.id.tot_lines_txt);
                totValTxt = (TextView) itemView.findViewById(R.id.tot_val_txt);

                order_Id = (TextView) itemView.findViewById(R.id.ord_id_val);
                invoice_Id = (TextView) itemView.findViewById(R.id.order_id_val);
                invDate = (TextView) itemView.findViewById(R.id.date_val);
                totLines = (TextView) itemView.findViewById(R.id.tota_lines_val);
                totVal = (TextView) itemView.findViewById(R.id.tot_val);
                invViewBtn = (TextView) itemView.findViewById(R.id.inv_view_btn);

                del_date_val = (TextView) itemView.findViewById(R.id.del_date_val);
                invoice_date_val = (TextView) itemView.findViewById(R.id.invoice_date_val);
                invoice_qty_val = (TextView) itemView.findViewById(R.id.invoice_qty_val);
                del_rep_code_val = (TextView) itemView.findViewById(R.id.del_rep_code_val);

                del_date_txt = (TextView) itemView.findViewById(R.id.del_date_txt);
                invoice_date_txt = (TextView) itemView.findViewById(R.id.invoice_date_txt);
                invoice_qty_txt = (TextView) itemView.findViewById(R.id.invoice_qty_txt);
                del_rep_code_txt = (TextView) itemView.findViewById(R.id.del_rep_code_txt);

                if (bmodel.configurationMasterHelper.SHOW_HISTORY_DETAIL) {
                    itemView.setClickable(true);
                    itemView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
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

                //typeface for label text
                invoice_Id_label.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
                order_Id_label.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
                dateTxt.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
                totLinesTxt.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
                totValTxt.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));

                del_date_txt.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
                invoice_date_txt.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
                invoice_qty_txt.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
                del_rep_code_txt.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));


                //typeface for value text font
                invoice_Id.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
                order_Id.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
                invDate.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
                totLines.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
                totVal.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
                invViewBtn.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));

                del_date_val.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
                invoice_date_val.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
                invoice_qty_val.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
                del_rep_code_val.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));

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

            }
        }
    }


}

