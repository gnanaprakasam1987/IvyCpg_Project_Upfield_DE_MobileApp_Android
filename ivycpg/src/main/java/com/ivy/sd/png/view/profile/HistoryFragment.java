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

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Vector;

/**
 * Created by nivetha.s on 26-10-2015.
 */
public class HistoryFragment extends IvyBaseFragment {
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
        orderHistoryList = (RecyclerView) view.findViewById(R.id.history_recyclerview);
        havgOrderLinesTxt = (TextView) view.findViewById(R.id.avg_line_txt);
        hOrderValueTxt = (TextView) view.findViewById(R.id.avg_val_txt);
        mavgOrderLines = (TextView) view.findViewById(R.id.avg_lines_val);
        mavgOrderValue = (TextView) view.findViewById(R.id.history_avg_val);

        havgOrderLinesTxt.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.BOLD));
        hOrderValueTxt.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.BOLD));
        mavgOrderLines.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.THIN));
        mavgOrderValue.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.THIN));

        orderHistoryList.setHasFixedSize(false);
        orderHistoryList.setNestedScrollingEnabled(false);
        orderHistoryList.setLayoutManager(new LinearLayoutManager(getActivity()));


//        orderHistoryListView = (ExpandableListView) view.findViewById(R.id.history_lvwplist);
//        orderHistoryListView.setCacheColorHint(0);
//        //   orderHistoryListView.setOnItemClickListener(getActivity());
//        historyLayout = (LinearLayout) view.findViewById(R.id.history_lhis);
//        orderHistoryFooterValues = (TableLayout) view.findViewById(R.id.history_history_footer_values);

        // mpastordereddays = (TextView) view.findViewById(R.id.history_orderdays);
        sdf = new SimpleDateFormat("yyyy/MM/dd");
       /* try {
            if (bmodel.labelsMasterHelper.applyLabels(view.findViewById(
                    R.id.tv_totalvalue).getTag()) != null)
                ((TextView) view.findViewById(R.id.tv_totalvalue))
                        .setText(bmodel.labelsMasterHelper
                                .applyLabels(view.findViewById(
                                        R.id.tv_totalvalue)
                                        .getTag()));
        } catch (Exception e) {
            Commons.printException(e);
        }*/


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
//        bmodel.profilehelper.downloadOrderHistory();
//        Vector<OrderHistoryBO> items = bmodel.profilehelper.getParentOrderHistory();

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
//        if (historyLayout != null)
//            historyLayout
//                    .setVisibility(View.VISIBLE);
//
//        if (orderHistoryFooterValues != null)
//            orderHistoryFooterValues
//                    .setVisibility(View.VISIBLE);
      /*  MyAdapterForHistory mSchedule = new MyAdapterForHistory(mylist);
        orderHistoryListView.setAdapter(mSchedule);*/

//        HistoryAdapter adapter = new HistoryAdapter(mylist);
//        orderHistoryListView.setAdapter(adapter);
//
//
//        historyLayout.setVisibility(View.VISIBLE);
//        orderHistoryFooterValues.setVisibility(View.VISIBLE);

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
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.custom_history_list, parent, false);

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
            holder.orderDate.setText(projectObj.getOrderdate());
            holder.totLines.setText(projectObj.getLpc() + "");
            holder.totVal.setText(bmodel.formatValue(projectObj.getOrderValue()));

            holder.del_date_val.setText(projectObj.getRF1());
            holder.invoice_date_val.setText(projectObj.getRF2());
            holder.invoice_qty_val.setText(projectObj.getRF3());
            holder.del_rep_code_val.setText(projectObj.getRF4());
            holder.totVol.setText("" + projectObj.getVolume());
            holder.deliveryStatus_val.setText(projectObj.getDelieveryStatus());

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

            private final TextView del_date_txt, invoice_date_txt, invoice_qty_txt, del_rep_code_txt;
            private final LinearLayout del_date_layout, invoice_date_layout, invoice_qty_layout, del_rep_code_layout;
            private LinearLayout listBgLayout, invViewLayout;
            private TextView orderIdTxt, dateTxt, totLinesTxt, totValTxt, totVolTxt, dueDateTxt;
            private TextView orderId, orderDate, totLines, totVal, totVol;
            private TextView invViewBtn, del_date_val, invoice_date_val, invoice_qty_val, del_rep_code_val, due_date_val;
            private LinearLayout tot_val_layout, tot_vol_layout, del_status_layout, start_date_layout, due_date_layout;
            private TextView deliveryStatus_txt, deliveryStatus_val;

            public ViewHolder(View itemView) {
                super(itemView);

                invViewLayout = (LinearLayout) itemView.findViewById(R.id.inv_view_layout);
                due_date_layout = (LinearLayout) itemView.findViewById(R.id.due_date_layout);
                listBgLayout = (LinearLayout) itemView.findViewById(R.id.list_background);
                orderIdTxt = (TextView) itemView.findViewById(R.id.order_id_txt);
                dateTxt = (TextView) itemView.findViewById(R.id.date_txt);
                totLinesTxt = (TextView) itemView.findViewById(R.id.tot_lines_txt);
                totValTxt = (TextView) itemView.findViewById(R.id.tot_val_txt);
                totVolTxt = (TextView) itemView.findViewById(R.id.tot_vol_txt);
                dueDateTxt = (TextView) itemView.findViewById(R.id.due_date_txt);


                orderId = (TextView) itemView.findViewById(R.id.order_id_val);
                orderDate = (TextView) itemView.findViewById(R.id.date_val);
                totLines = (TextView) itemView.findViewById(R.id.tota_lines_val);
                totVal = (TextView) itemView.findViewById(R.id.tot_val);
                totVol = (TextView) itemView.findViewById(R.id.tot_vol);
                invViewBtn = (TextView) itemView.findViewById(R.id.inv_view_btn);

                del_date_val = (TextView) itemView.findViewById(R.id.del_date_val);
                invoice_date_val = (TextView) itemView.findViewById(R.id.invoice_date_val);
                invoice_qty_val = (TextView) itemView.findViewById(R.id.invoice_qty_val);
                del_rep_code_val = (TextView) itemView.findViewById(R.id.del_rep_code_val);
                due_date_val = (TextView) itemView.findViewById(R.id.due_date_val);

                del_date_txt = (TextView) itemView.findViewById(R.id.del_date_txt);
                invoice_date_txt = (TextView) itemView.findViewById(R.id.invoice_date_txt);
                invoice_qty_txt = (TextView) itemView.findViewById(R.id.invoice_qty_txt);
                del_rep_code_txt = (TextView) itemView.findViewById(R.id.del_rep_code_txt);

                del_date_layout = (LinearLayout) itemView.findViewById(R.id.del_date_layout);
                invoice_date_layout = (LinearLayout) itemView.findViewById(R.id.invoice_date_layout);
                invoice_qty_layout = (LinearLayout) itemView.findViewById(R.id.invoice_qty_layout);
                del_rep_code_layout = (LinearLayout) itemView.findViewById(R.id.del_rep_code_layout);
                tot_val_layout = (LinearLayout) itemView.findViewById(R.id.tot_val_layout);
                tot_vol_layout = (LinearLayout) itemView.findViewById(R.id.tot_vol_layout);
                del_status_layout = (LinearLayout) itemView.findViewById(R.id.del_status_layout);
                start_date_layout = (LinearLayout) itemView.findViewById(R.id.date_layout);
                deliveryStatus_txt = (TextView) itemView.findViewById(R.id.del_status_txt);
                deliveryStatus_val = (TextView) itemView.findViewById(R.id.deliveryStatusValue);

                /*if (!bmodel.configurationMasterHelper.SHOW_TOTAL_VALUE_ORDER) {
                    totValTxt.setVisibility(View.GONE);
                    totVal.setVisibility(View.GONE);
                    invViewLayout.setVisibility(View.GONE);
                    tot_val_layout.setVisibility(View.GONE);
                    itemView.setClickable(false);
                    itemView.setOnClickListener(null);
                    ((LinearLayout) itemView.findViewById(R.id.tot_val_layout)).setVisibility(View.GONE);
                } else {
                    totValTxt.setVisibility(View.VISIBLE);
                    totVal.setVisibility(View.VISIBLE);
                    ((LinearLayout) itemView.findViewById(R.id.tot_val_layout)).setVisibility(View.VISIBLE);
                    if (bmodel.configurationMasterHelper.SHOW_HISTORY_DETAIL)
                        invViewLayout.setVisibility(View.VISIBLE);
                }*/


                itemView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (bmodel.configurationMasterHelper.SHOW_HST_INVDET) {
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
                } catch (Exception ex) {

                }


                //typeface for label text
                orderIdTxt.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.REGULAR));
                dateTxt.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.REGULAR));
                totLinesTxt.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.REGULAR));
                totValTxt.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.REGULAR));
                totVolTxt.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.REGULAR));
                dueDateTxt.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.REGULAR));
                deliveryStatus_txt.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.REGULAR));

                due_date_val.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
                del_date_txt.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.REGULAR));
                invoice_date_txt.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.REGULAR));
                invoice_qty_txt.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.REGULAR));
                del_rep_code_txt.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.REGULAR));


                //typeface for value text font
                orderId.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
                orderDate.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
                totLines.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
                totVal.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
                totVol.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
                invViewBtn.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.BOLD));

                del_date_val.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
                invoice_date_val.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
                invoice_qty_val.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
                del_rep_code_val.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
                deliveryStatus_val.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.BOLD));


                if (bmodel.configurationMasterHelper.SHOW_HST_DELDATE) {
                    del_date_layout.setVisibility(View.VISIBLE);
                } else {
                    del_date_layout.setVisibility(View.GONE);
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
                if (bmodel.configurationMasterHelper.SHOW_HST_INVDET) {
                    invViewLayout.setVisibility(View.VISIBLE);
                } else {
                    invViewLayout.setVisibility(View.GONE);
                }
            }
        }
    }


//    private OrderHistoryBO orderHistory;
//
//
//    class HistoryAdapter extends BaseExpandableListAdapter {
//        double balanceAmt;
//        Vector<OrderHistoryBO> lstParent;
//
//        public HistoryAdapter(Vector<OrderHistoryBO> items) {
//            this.lstParent = items;
//        }
//
//        @Override
//        public Object getChild(int groupPosition, int childPosition) {
//            return null;
//        }
//
//        @Override
//        public long getChildId(int groupPosition, int childPosition) {
//            return 0;
//        }
//
//        @Override
//        public int getChildrenCount(int groupPosition) {
//            if (lstParent.get(groupPosition).getRefId() != null && !lstParent.get(groupPosition).getRefId().equals("") && !lstParent.get(groupPosition).getRefId().equals("0")) {
//
//                return bmodel.profilehelper.getChild_orderHistoryList()
//                        .get(groupPosition).size();
//            } else {
//                return 0;
//            }
//
//        }
//
//        @Override
//        public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View view, ViewGroup parent) {
//
//            View row = view;
//            Vector<OrderHistoryBO> orderHistory = bmodel.profilehelper.getChild_orderHistoryList()
//                    .get(groupPosition);
//            ChildHolder childHolder;
//            if (row == null) {
//                LayoutInflater inflater = getActivity().getLayoutInflater();
//                row = inflater.inflate(R.layout.row_orderhistory_child,
//                        parent, false);
//                childHolder = new ChildHolder();
//
//                childHolder.tv_prodName = (TextView) row.findViewById(R.id.tv_prod_name);
//                childHolder.ll_header = (LinearLayout) row.findViewById(R.id.ll_header);
//                childHolder.tv_pcsQty = (TextView) row.findViewById(R.id.tv_qty_pc);
//                childHolder.tv_CaseQty = (TextView) row.findViewById(R.id.tv_qty_cs);
//                childHolder.tv_OuterQty = (TextView) row.findViewById(R.id.tv_qty_ou);
//
//                childHolder.lbl_prodName = (TextView) row.findViewById(R.id.lbl_prod_name);
//                childHolder.lbl_pcsQty = (TextView) row.findViewById(R.id.lbl_qty_pc);
//                childHolder.lbl_caseQty = (TextView) row.findViewById(R.id.lbl_qty_cs);
//                childHolder.lbl_outerQty = (TextView) row.findViewById(R.id.lbl_qty_ou);
//
//
//                try {
//                    if (bmodel.labelsMasterHelper.applyLabels(row.findViewById(
//                            R.id.lbl_qty_pc).getTag()) != null)
//                        ((TextView) row.findViewById(R.id.lbl_qty_pc))
//                                .setText(bmodel.labelsMasterHelper
//                                        .applyLabels(row.findViewById(
//                                                R.id.lbl_qty_pc)
//                                                .getTag()));
//                } catch (Exception e) {
//                    Commons.printException(e);
//                }
//
//                try {
//                    if (bmodel.labelsMasterHelper.applyLabels(row.findViewById(
//                            R.id.lbl_qty_cs).getTag()) != null)
//                        ((TextView) row.findViewById(R.id.lbl_qty_cs))
//                                .setText(bmodel.labelsMasterHelper
//                                        .applyLabels(row.findViewById(
//                                                R.id.lbl_qty_cs)
//                                                .getTag()));
//                } catch (Exception e) {
//                    Commons.printException(e);
//                }
//
//                try {
//                    if (bmodel.labelsMasterHelper.applyLabels(row.findViewById(
//                            R.id.lbl_qty_ou).getTag()) != null)
//                        ((TextView) row.findViewById(R.id.lbl_qty_ou))
//                                .setText(bmodel.labelsMasterHelper
//                                        .applyLabels(row.findViewById(
//                                                R.id.lbl_qty_ou)
//                                                .getTag()));
//                } catch (Exception e) {
//                    Commons.printException(e);
//                }
//
//
//                row.setTag(childHolder);
//            } else {
//                childHolder = (ChildHolder) row.getTag();
//            }
///*
//            if (groupPosition % 2 == 0)
//                row.setBackgroundColor(typearr.getColor(R.styleable.MyTextView_listcolor, 0));
//            else
//                row.setBackgroundColor(typearr.getColor(R.styleable.MyTextView_listcolor_alt, 0));*/
//
//            if (childPosition == 0) {
//                childHolder.ll_header.setVisibility(View.VISIBLE);
//            } else {
//                childHolder.ll_header.setVisibility(View.GONE);
//            }
//            childHolder.childHistoryList = orderHistory;
//            childHolder.tv_prodName.setText(childHolder.childHistoryList.get(childPosition).getProductName());
//            childHolder.tv_pcsQty.setText(childHolder.childHistoryList.get(childPosition).getPcsQty() + "");
//            childHolder.tv_CaseQty.setText(childHolder.childHistoryList.get(childPosition).getCaseQty() + "");
//            childHolder.tv_OuterQty.setText(childHolder.childHistoryList.get(childPosition).getOuterQty() + "");
//
//            return row;
//        }
//
//        @Override
//        public Object getGroup(int groupPosition) {
//            return lstParent.get(groupPosition);
//        }
//
//        @Override
//        public int getGroupCount() {
//            return lstParent.size();
//        }
//
//        @Override
//        public long getGroupId(int i) {
//            return 0;
//        }
//
//        @Override
//        public int getGroupTypeCount() {
//            return super.getGroupTypeCount();
//        }
//
//        @Override
//        public int getGroupType(int groupPosition) {
//            return super.getGroupType(groupPosition);
//        }
//
//        @Override
//        public View getGroupView(int groupPosition, boolean b, View view, ViewGroup parent) {
//            View row = view;
//            OrderHistoryBO orderHistory = lstParent
//                    .get(groupPosition);
//            ViewHolder holder;
//            if (row == null) {
//                LayoutInflater inflater = getActivity().getLayoutInflater();
//                row = inflater.inflate(R.layout.row_orderhistory,
//                        parent, false);
//                holder = new ViewHolder();
//
//                holder.orderid = (TextView) row.findViewById(R.id.orderid);
//                holder.orderdate = (TextView) row.findViewById(R.id.orderdatee);
//                holder.totalvalue = (TextView) row
//                        .findViewById(R.id.ordervalue);
//                holder.lpc = (TextView) row.findViewById(R.id.orderlpc);
//                holder.joincallImg = (ImageView) row
//                        .findViewById(R.id.img_joincall);
//                holder.delieveryStatusValue = (ImageView) row.findViewById(R.id.deliveryStatusValue);
//                holder.paidAmount = (TextView) row.findViewById(R.id.paidAmount);
//                holder.balanceAmount = (TextView) row.findViewById(R.id.balanceAmount);
//               /* holder.mnumid = (TextView) row.findViewById(R.id.lbl_id);*/
//                holder.ll_no_order_reason = (LinearLayout) row.findViewById(R.id.ll_no_order_reason);
//                holder.ll_order_one = (LinearLayout) row.findViewById(R.id.ll_order_one);
//                holder.ll_order_two = (LinearLayout) row.findViewById(R.id.ll_order_two);
//                holder.mresonid = (TextView) row.findViewById(R.id.resonid);
//                holder.duedate = (TextView) row.findViewById(R.id.dueDate);
//
//                holder.ll_image = (LinearLayout) row.findViewById(R.id.ll_image);
//
//
//                row.setTag(holder);
//            } else {
//                holder = (ViewHolder) row.getTag();
//            }
//
//            holder.ref = groupPosition;
//
//            try {
//                if (bmodel.labelsMasterHelper.applyLabels(row.findViewById(
//                        R.id.lbl_orderid).getTag()) != null)
//                    ((TextView) row.findViewById(R.id.lbl_orderid))
//                            .setText(bmodel.labelsMasterHelper
//                                    .applyLabels(row.findViewById(
//                                            R.id.lbl_orderid)
//                                            .getTag()));
//            } catch (Exception ex) {
//
//            }
//
//            holder.orderdate.setText(orderHistory.getOrderdate() + " ");
//            holder.orderid.setText(orderHistory.getOrderid() + " ");
//
//            holder.totalvalue.setText(bmodel.formatValue(orderHistory.getOrderValue()));
//            holder.lpc.setText(orderHistory.getLpc() + " ");
//            //  holder.mnumid.setText(orderHistory.getNumid() + "");
//            holder.mresonid.setText(orderHistory.getNoorderReason() + "");
//            try {
//                Calendar c = Calendar.getInstance();
//                c.setTime(sdf.parse(orderHistory.getOrderdate()));
//                c.add(Calendar.DATE, bmodel.retailerMasterBO.getCreditDays());
//                holder.duedate.setText(sdf.format(c.getTime()));
//            } catch (ParseException e) {
//                Commons.printException(e);
//                holder.duedate.setText("");
//            }
//           /* if (groupPosition % 2 == 0)
//                row.setBackgroundColor(typearr.getColor(R.styleable.MyTextView_listcolor, 0));
//            else
//                row.setBackgroundColor(typearr.getColor(R.styleable.MyTextView_listcolor_alt, 0));*/
//
//            if (orderHistory.getIsJointCall() == 1) {
//                holder.joincallImg.setVisibility(View.VISIBLE);
//
//            } else {
//                holder.joincallImg.setVisibility(View.GONE);
//
//            }
//            if (orderHistory.getDelieveryStatus().equalsIgnoreCase("Y")) {
//                holder.delieveryStatusValue.setVisibility(View.VISIBLE);
//            } else {
//                holder.delieveryStatusValue.setVisibility(View.GONE);
//            }
//
//            if (orderHistory.getIsJointCall() == 1 || orderHistory.getDelieveryStatus().equalsIgnoreCase("Y")) {
//                holder.ll_image.setVisibility(View.VISIBLE);
//            } else {
//                holder.ll_image.setVisibility(View.GONE);
//            }
//            if (orderHistory.getRefId() != null && !orderHistory.getRefId().equals("") && !orderHistory.getRefId().equals("0")) {
//                holder.ll_order_one.setVisibility(View.VISIBLE);
//                holder.ll_order_two.setVisibility(View.VISIBLE);
//                holder.ll_no_order_reason.setVisibility(View.GONE);
//            } else {
//                holder.ll_order_one.setVisibility(View.GONE);
//                holder.ll_order_two.setVisibility(View.GONE);
//                holder.ll_no_order_reason.setVisibility(View.VISIBLE);
//            }
//
//            holder.paidAmount.setText(bmodel.formatValue(orderHistory.getPaidAmount()));
//
//            balanceAmt = orderHistory.getOrderValue() - orderHistory.getPaidAmount();
//
//            if (orderHistory.getOrderValue() != 0)
//                holder.balanceAmount.setText(bmodel.formatValue(balanceAmt));
//            else
//                holder.balanceAmount.setText(0.0 + "");
//
//            return row;
//        }
//
//        @Override
//        public boolean isChildSelectable(int i, int i1) {
//            return false;
//        }
//
//        @Override
//        public boolean hasStableIds() {
//            return false;
//        }
//    }
//
//    class ViewHolder {
//        TextView orderid, orderdate, totalvalue, lpc, paidAmount, balanceAmount, mnumid, mresonid, duedate;
//        int ref;
//        ImageView joincallImg, delieveryStatusValue;
//        LinearLayout ll_no_order_reason, ll_order_one, ll_order_two, ll_image;
//    }

//    class ChildHolder {
//        TextView tv_prodName, tv_pcsQty, tv_CaseQty, tv_OuterQty;
//        TextView lbl_prodName, lbl_pcsQty, lbl_outerQty, lbl_caseQty;
//        Vector<OrderHistoryBO> childHistoryList;
//        LinearLayout ll_header;
//    }

}
