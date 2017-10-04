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
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.bo.OrderHistoryBO;
import com.ivy.sd.png.commons.IvyBaseFragment;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.provider.ConfigurationMasterHelper;
import com.ivy.sd.png.util.Commons;
import com.ivy.sd.png.util.DateUtil;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
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

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        getActivity().getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        bmodel = (BusinessModel) getActivity().getApplicationContext();
        bmodel.setContext(getActivity());

        view = inflater.inflate(R.layout.fragment_history_new, container,
                false);

        invoiceHistoryList = (RecyclerView) view.findViewById(R.id.history_recyclerview);
        havgOrderLinesTxt = (TextView) view.findViewById(R.id.avg_line_txt);
        hOrderValueTxt = (TextView) view.findViewById(R.id.avg_val_txt);
        mavgOrderLines = (TextView) view.findViewById(R.id.avg_lines_val);
        mavgOrderValue = (TextView) view.findViewById(R.id.history_avg_val);

        havgOrderLinesTxt.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.BOLD));
        hOrderValueTxt.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.BOLD));
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
        Vector<OrderHistoryBO> items = bmodel.profilehelper.getInvoiceHistoryList();

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
        return view;
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

            holder.orderId.setText(projectObj.getOrderid());
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
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd");
                Calendar c = Calendar.getInstance();
                c.setTime(sdf.parse(projectObj.getOrderdate()));
                holder.date.setText(DateUtil.convertDateObjectToRequestedFormat(
                        sdf.parse(sdf.format(c.getTime())), bmodel.configurationMasterHelper.outDateFormat));
            } catch (ParseException e) {
                Commons.printException(e);
                holder.date.setText("");
            }

        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {

            private final TextView del_date_txt, invoice_date_txt, invoice_qty_txt, del_rep_code_txt;
            private LinearLayout listBgLayout, invViewLayout;
            private TextView orderIdTxt, dateTxt, totLinesTxt, totValTxt;
            private TextView orderId, date, totLines, totVal;
            private TextView invViewBtn, del_date_val, invoice_date_val, invoice_qty_val, del_rep_code_val;

            public ViewHolder(View itemView) {
                super(itemView);

                invViewLayout = (LinearLayout) itemView.findViewById(R.id.inv_view_layout);
                listBgLayout = (LinearLayout) itemView.findViewById(R.id.list_background);
                orderIdTxt = (TextView) itemView.findViewById(R.id.order_id_txt);
                dateTxt = (TextView) itemView.findViewById(R.id.date_txt);
                totLinesTxt = (TextView) itemView.findViewById(R.id.tot_lines_txt);
                totValTxt = (TextView) itemView.findViewById(R.id.tot_val_txt);


                orderId = (TextView) itemView.findViewById(R.id.order_id_val);
                date = (TextView) itemView.findViewById(R.id.date_val);
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

                if(bmodel.configurationMasterHelper.SHOW_HISTORY_DETAIL){
                    invViewLayout.setVisibility(View.VISIBLE);
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
                }else{
                    invViewLayout.setVisibility(View.GONE);
                    itemView.setClickable(false);
                    itemView.setOnClickListener(null);
                }

                //typeface for label text
                orderIdTxt.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.REGULAR));
                dateTxt.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.REGULAR));
                totLinesTxt.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.REGULAR));
                totValTxt.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.REGULAR));

                del_date_txt.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.REGULAR));
                invoice_date_txt.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.REGULAR));
                invoice_qty_txt.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.REGULAR));
                del_rep_code_txt.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.REGULAR));


                //typeface for value text font
                orderId.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
                date.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
                totLines.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
                totVal.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
                invViewBtn.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.BOLD));

                del_date_val.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
                invoice_date_val.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
                invoice_qty_val.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
                del_rep_code_val.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));


            }
        }
    }


}

