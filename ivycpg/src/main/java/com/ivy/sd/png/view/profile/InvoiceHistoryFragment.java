package com.ivy.sd.png.view.profile;

import android.content.Intent;
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
import com.ivy.utils.FontUtils;

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
        invoiceHistoryList = view.findViewById(R.id.history_recyclerview);
        havgOrderLinesTxt = view.findViewById(R.id.avg_line_txt);
        hOrderValueTxt = view.findViewById(R.id.avg_val_txt);
        mavgOrderLines = view.findViewById(R.id.avg_lines_val);
        mavgOrderValue = view.findViewById(R.id.history_avg_val);

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



        bmodel.profilehelper.downloadInvoiceHistory();
        Vector<OrderHistoryBO> items = bmodel.profilehelper.getParentInvoiceHistory();


        historyViewAdapter = new HistoryViewAdapter(items);
        invoiceHistoryList.setAdapter(historyViewAdapter);
        if (!(items.size() > 0))
            (view.findViewById(R.id.parentLayout)).setVisibility(View.GONE);
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
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {

            private final TextView del_date_txt, invoice_date_txt, invoice_qty_txt, del_rep_code_txt;
            private LinearLayout listBgLayout, invViewLayout;
            private TextView invoice_Id_label, dateTxt, totLinesTxt, totValTxt, totalVolumeTxt, marginPriceTxt, marginPerTxt;
            private TextView invoice_Id, invDate, totLines, totVal, totalVolume, marginPrice, marginPer;
            private TextView invViewBtn, del_date_val, invoice_date_val, invoice_qty_val, del_rep_code_val;
            private TextView order_Id, order_Id_label;


            public ViewHolder(View itemView) {
                super(itemView);

                invViewLayout = itemView.findViewById(R.id.inv_view_layout);
                listBgLayout = itemView.findViewById(R.id.list_background);
                invoice_Id_label = itemView.findViewById(R.id.order_id_txt);
                order_Id_label = itemView.findViewById(R.id.ord_id_txt);
                dateTxt = itemView.findViewById(R.id.date_txt);
                totLinesTxt = itemView.findViewById(R.id.tot_lines_txt);
                totValTxt = itemView.findViewById(R.id.tot_val_txt);
                totalVolumeTxt = itemView.findViewById(R.id.tot_volume_txt);
                marginPriceTxt = itemView.findViewById(R.id.margin_price_txt);
                marginPerTxt = itemView.findViewById(R.id.margin_per_txt);


                order_Id = itemView.findViewById(R.id.ord_id_val);
                invoice_Id = itemView.findViewById(R.id.order_id_val);
                invDate = itemView.findViewById(R.id.date_val);
                totLines = itemView.findViewById(R.id.tota_lines_val);
                totVal = itemView.findViewById(R.id.tot_val);
                invViewBtn = itemView.findViewById(R.id.inv_view_btn);
                totalVolume = itemView.findViewById(R.id.tota_Volume_val);
                marginPrice = itemView.findViewById(R.id.margin_price_val);
                marginPer = itemView.findViewById(R.id.margin_per_val);

                del_date_val = itemView.findViewById(R.id.del_date_val);
                invoice_date_val = itemView.findViewById(R.id.invoice_date_val);
                invoice_qty_val = itemView.findViewById(R.id.invoice_qty_val);
                del_rep_code_val = itemView.findViewById(R.id.del_rep_code_val);

                del_date_txt = itemView.findViewById(R.id.del_date_txt);
                invoice_date_txt = itemView.findViewById(R.id.invoice_date_txt);
                invoice_qty_txt = itemView.findViewById(R.id.invoice_qty_txt);
                del_rep_code_txt = itemView.findViewById(R.id.del_rep_code_txt);

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
                invoice_Id_label.setTypeface(FontUtils.getFontRoboto(getActivity(), FontUtils.FontType.LIGHT));
                order_Id_label.setTypeface(FontUtils.getFontRoboto(getActivity(), FontUtils.FontType.LIGHT));
                dateTxt.setTypeface(FontUtils.getFontRoboto(getActivity(), FontUtils.FontType.LIGHT));
                totLinesTxt.setTypeface(FontUtils.getFontRoboto(getActivity(), FontUtils.FontType.LIGHT));
                totValTxt.setTypeface(FontUtils.getFontRoboto(getActivity(), FontUtils.FontType.LIGHT));
                totalVolumeTxt.setTypeface(FontUtils.getFontRoboto(getActivity(), FontUtils.FontType.LIGHT));
                del_date_txt.setTypeface(FontUtils.getFontRoboto(getActivity(), FontUtils.FontType.LIGHT));
                invoice_date_txt.setTypeface(FontUtils.getFontRoboto(getActivity(), FontUtils.FontType.LIGHT));
                invoice_qty_txt.setTypeface(FontUtils.getFontRoboto(getActivity(), FontUtils.FontType.LIGHT));
                del_rep_code_txt.setTypeface(FontUtils.getFontRoboto(getActivity(), FontUtils.FontType.LIGHT));
                marginPriceTxt.setTypeface(FontUtils.getFontRoboto(getActivity(), FontUtils.FontType.LIGHT));
                marginPerTxt.setTypeface(FontUtils.getFontRoboto(getActivity(), FontUtils.FontType.LIGHT));


                //typeface for value text font
                invoice_Id.setTypeface(FontUtils.getFontRoboto(getActivity(), FontUtils.FontType.MEDIUM));
                order_Id.setTypeface(FontUtils.getFontRoboto(getActivity(), FontUtils.FontType.MEDIUM));
                invDate.setTypeface(FontUtils.getFontRoboto(getActivity(), FontUtils.FontType.MEDIUM));
                totLines.setTypeface(FontUtils.getFontRoboto(getActivity(), FontUtils.FontType.MEDIUM));
                totVal.setTypeface(FontUtils.getFontRoboto(getActivity(), FontUtils.FontType.MEDIUM));
                invViewBtn.setTypeface(FontUtils.getFontRoboto(getActivity(), FontUtils.FontType.MEDIUM));
                totalVolume.setTypeface(FontUtils.getFontRoboto(getActivity(), FontUtils.FontType.MEDIUM));
                del_date_val.setTypeface(FontUtils.getFontRoboto(getActivity(), FontUtils.FontType.MEDIUM));
                invoice_date_val.setTypeface(FontUtils.getFontRoboto(getActivity(), FontUtils.FontType.MEDIUM));
                invoice_qty_val.setTypeface(FontUtils.getFontRoboto(getActivity(), FontUtils.FontType.MEDIUM));
                del_rep_code_val.setTypeface(FontUtils.getFontRoboto(getActivity(), FontUtils.FontType.MEDIUM));
                marginPrice.setTypeface(FontUtils.getFontRoboto(getActivity(), FontUtils.FontType.MEDIUM));
                marginPer.setTypeface(FontUtils.getFontRoboto(getActivity(), FontUtils.FontType.MEDIUM));

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

