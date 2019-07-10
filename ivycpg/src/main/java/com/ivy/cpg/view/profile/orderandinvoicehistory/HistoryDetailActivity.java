package com.ivy.cpg.view.profile.orderandinvoicehistory;

import android.content.res.TypedArray;
import android.os.Bundle;
import androidx.appcompat.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.bo.OrderHistoryBO;
import com.ivy.sd.png.commons.IvyBaseActivityNoActionBar;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.util.Commons;
import com.ivy.utils.FontUtils;

import java.util.Vector;

/**
 * Created by mayuri.v on 5/16/2017.
 */
public class HistoryDetailActivity extends IvyBaseActivityNoActionBar {
    private BusinessModel bmodel;
    int selectedPosition = -1;
    TextView tvpiece, tvcase, tvouter;
    String fromScreen = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        selectedPosition = getIntent().getIntExtra("selected_list_id", -1);
        fromScreen = getIntent().getStringExtra("from");
        bmodel = (BusinessModel) getApplicationContext();
        bmodel.setContext(this);
        setContentView(R.layout.history_detail_layout);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        if (toolbar != null)
            setSupportActionBar(toolbar);

        getSupportActionBar().setTitle(null);
        getSupportActionBar().setIcon(null);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        if (fromScreen.equalsIgnoreCase("OrderHistory"))
            setScreenTitle(getResources().getString(R.string.order_history_detail));
        else if (fromScreen.equalsIgnoreCase("InvoiceHistory"))
            setScreenTitle(getResources().getString(R.string.invoice_history_detail));

        tvpiece = (TextView) findViewById(R.id.tvpiece);
        tvcase = (TextView) findViewById(R.id.tvcase);
        tvouter = (TextView) findViewById(R.id.tvouter);

        ((TextView) findViewById(R.id.tvbrand)).setTypeface(FontUtils.getFontRoboto(HistoryDetailActivity.this, FontUtils.FontType.MEDIUM));
        ((TextView) findViewById(R.id.tvorder)).setTypeface(FontUtils.getFontRoboto(HistoryDetailActivity.this, FontUtils.FontType.MEDIUM));
        tvouter.setTypeface(FontUtils.getFontRoboto(HistoryDetailActivity.this, FontUtils.FontType.MEDIUM));
        tvpiece.setTypeface(FontUtils.getFontRoboto(HistoryDetailActivity.this, FontUtils.FontType.MEDIUM));
        tvcase.setTypeface(FontUtils.getFontRoboto(HistoryDetailActivity.this, FontUtils.FontType.MEDIUM));

        ListView lv = (ListView) findViewById(R.id.list);
        lv.setCacheColorHint(0);
        if (selectedPosition >= 0)
            lv.setAdapter(new MyAdapter());
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int i = item.getItemId();
        if (i == android.R.id.home) {
            finish();
//            return true;
        }
        return super.onOptionsItemSelected(item);
    }


    private class MyAdapter extends BaseAdapter {
        LayoutInflater inflater = LayoutInflater.from(getBaseContext());
        TypedArray typearr = getTheme().obtainStyledAttributes(R.styleable.MyTextView);
        Vector<OrderHistoryBO> orderList;

        public MyAdapter() {
            if (fromScreen.equalsIgnoreCase("OrderHistory")) {
                orderList = bmodel.profilehelper.getChild_orderHistoryList()
                        .get(selectedPosition);
            } else if (fromScreen.equalsIgnoreCase("InvoiceHistory")) {
                orderList = bmodel.profilehelper.getChild_invoiceHistoryList()
                        .get(selectedPosition);
            }

        }

        @Override
        public int getCount() {
            return orderList.size();
        }

        @Override
        public Object getItem(int position) {
            return orderList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            final ViewHolder holder;
            View row = convertView;

            if (row == null) {
                holder = new ViewHolder();

                row = inflater.inflate(R.layout.history_detail_row, parent, false);

                holder.brandname = (TextView) row
                        .findViewById(R.id.brandname);
                //holder.brandname.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.BOLD));
                holder.brandname.setTypeface(FontUtils.getProductNameFont(HistoryDetailActivity.this));

                holder.piecevalue = (TextView) row
                        .findViewById(R.id.piecevalue);
                holder.piecevalue.setTypeface(FontUtils.getFontRoboto(HistoryDetailActivity.this, FontUtils.FontType.MEDIUM));

                holder.casevalue = (TextView) row
                        .findViewById(R.id.casevalue);
                holder.casevalue.setTypeface(FontUtils.getFontRoboto(HistoryDetailActivity.this, FontUtils.FontType.MEDIUM));

                holder.outervalue = (TextView) row
                        .findViewById(R.id.outervalue);
                holder.outervalue.setTypeface(FontUtils.getFontRoboto(HistoryDetailActivity.this, FontUtils.FontType.MEDIUM));

                holder.freeQty = row.findViewById(R.id.freeQty_val);
                holder.freeQty.setTypeface(FontUtils.getFontRoboto(HistoryDetailActivity.this, FontUtils.FontType.MEDIUM));

                holder.value = row.findViewById(R.id.value_val);
                holder.value.setTypeface(FontUtils.getFontRoboto(HistoryDetailActivity.this, FontUtils.FontType.MEDIUM));

                holder.ll_piece = row.findViewById(R.id.piece_layout);
                holder.ll_case = row.findViewById(R.id.cases_layout);
                holder.ll_outer = row.findViewById(R.id.outer_layout);
                holder.ll_freeQty = row.findViewById(R.id.freeQty_layout);
                holder.ll_value = row.findViewById(R.id.value_layout);

                holder.piece_label = row.findViewById(R.id.pieces_txt);
                holder.case_label = row.findViewById(R.id.cases_txt);
                holder.outer_label = row.findViewById(R.id.outer_txt);
                holder.freeQty_label = row.findViewById(R.id.freeQty_txt);
                holder.value_label = row.findViewById(R.id.value_txt);

                row.setTag(holder);

            } else {
                holder = (ViewHolder) row.getTag();
            }

            if (position % 2 == 0)
                row.setBackgroundColor(typearr.getColor(R.styleable.MyTextView_listcolor, 0));
            else
                row.setBackgroundColor(typearr.getColor(R.styleable.MyTextView_listcolor_alt, 0));

            holder.childHistoryList = orderList;
            holder.brandname.setText(holder.childHistoryList.get(position).getProductName());
            holder.piecevalue.setText(holder.childHistoryList.get(position).getPcsQty() + "");
            holder.casevalue.setText(holder.childHistoryList.get(position).getCaseQty() + "");
            holder.outervalue.setText(holder.childHistoryList.get(position).getOuterQty() + "");
            holder.value.setText(holder.childHistoryList.get(position).getValue() + "");
            holder.freeQty.setText(holder.childHistoryList.get(position).getFreeQty() + "");


            if (!bmodel.configurationMasterHelper.SHOW_ORDER_CASE) {
                holder.ll_case.setVisibility(View.GONE);
            } else {
                try {
                    holder.ll_case.setVisibility(View.VISIBLE);
                    if (bmodel.labelsMasterHelper.applyLabels(holder.case_label.getTag()) != null)
                        holder.case_label
                                .setText(bmodel.labelsMasterHelper
                                        .applyLabels(holder.case_label.getTag()));
                } catch (Exception e) {
                    Commons.printException(e);
                }
            }
            if (!bmodel.configurationMasterHelper.SHOW_ORDER_PCS) {
                holder.ll_piece.setVisibility(View.GONE);
            } else {
                try {
                    holder.ll_piece.setVisibility(View.VISIBLE);
                    if (bmodel.labelsMasterHelper.applyLabels(holder.piece_label.getTag()) != null)
                        holder.piece_label
                                .setText(bmodel.labelsMasterHelper
                                        .applyLabels(holder.piece_label.getTag()));
                } catch (Exception e) {
                    Commons.printException(e);
                }
            }
            if (!bmodel.configurationMasterHelper.SHOW_OUTER_CASE) {
                holder.ll_outer.setVisibility(View.GONE);
            } else {
                try {
                    holder.ll_outer.setVisibility(View.VISIBLE);
                    if (bmodel.labelsMasterHelper.applyLabels(holder.outer_label.getTag()) != null)
                        holder.outer_label
                                .setText(bmodel.labelsMasterHelper
                                        .applyLabels(holder.outer_label.getTag()));
                } catch (Exception e) {
                    Commons.printException(e);
                }
            }

                try {
                    holder.ll_freeQty.setVisibility(View.VISIBLE);
                    if (bmodel.labelsMasterHelper.applyLabels(holder.freeQty_label.getTag()) != null)
                        holder.freeQty_label
                                .setText(bmodel.labelsMasterHelper
                                        .applyLabels(holder.freeQty_label.getTag()));
                } catch (Exception e) {
                    Commons.printException(e);
                }

                try {
                    holder.ll_value.setVisibility(View.VISIBLE);
                    if (bmodel.labelsMasterHelper.applyLabels(holder.value_label.getTag()) != null)
                        holder.value_label
                                .setText(bmodel.labelsMasterHelper
                                        .applyLabels(holder.value_label.getTag()));
                } catch (Exception e) {
                    Commons.printException(e);
                }

            return row;
        }

        class ViewHolder {
            TextView brandname, piecevalue, casevalue, outervalue,freeQty,value;
            private TextView piece_label,case_label,outer_label,freeQty_label,value_label;
            private LinearLayout ll_piece,ll_case,ll_outer, ll_freeQty, ll_value;
            private Vector<OrderHistoryBO> childHistoryList;
        }

    }

}
