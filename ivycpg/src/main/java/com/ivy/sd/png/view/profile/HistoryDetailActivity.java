package com.ivy.sd.png.view.profile;

import android.content.res.TypedArray;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
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
import com.ivy.sd.png.provider.ConfigurationMasterHelper;
import com.ivy.sd.png.util.Commons;
import com.ivy.utils.AppUtils;
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

        ((TextView) findViewById(R.id.tvbrand)).setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
        ((TextView) findViewById(R.id.tvorder)).setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
        tvouter.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
        tvpiece.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
        tvcase.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));

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
                holder.brandname.setTypeface(bmodel.configurationMasterHelper.getProductNameFont());

                holder.piecevalue = (TextView) row
                        .findViewById(R.id.piecevalue);
                holder.piecevalue.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));

                holder.casevalue = (TextView) row
                        .findViewById(R.id.casevalue);
                holder.casevalue.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));

                holder.outervalue = (TextView) row
                        .findViewById(R.id.outervalue);
                holder.outervalue.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));

                holder.freeQty = row.findViewById(R.id.freeQty_val);
                holder.freeQty.setTypeface(FontUtils.getFontRoboto(HistoryDetailActivity.this, FontUtils.FontType.MEDIUM));

                holder.value = row.findViewById(R.id.value_val);
                holder.value.setTypeface(FontUtils.getFontRoboto(HistoryDetailActivity.this, FontUtils.FontType.MEDIUM));

                holder.ll_piece = row.findViewById(R.id.piece_layout);
                holder.ll_case = row.findViewById(R.id.cases_layout);
                holder.ll_outer = row.findViewById(R.id.outer_layout);

                holder.piece_label = row.findViewById(R.id.pieces_txt);
                holder.piece_label.setTypeface(FontUtils.getFontRoboto(HistoryDetailActivity.this, FontUtils.FontType.REGULAR));

                holder.case_label = row.findViewById(R.id.cases_txt);
                holder.case_label.setTypeface(FontUtils.getFontRoboto(HistoryDetailActivity.this, FontUtils.FontType.REGULAR));

                holder.outer_label = row.findViewById(R.id.outer_txt);
                holder.outer_label.setTypeface(FontUtils.getFontRoboto(HistoryDetailActivity.this, FontUtils.FontType.REGULAR));

                holder.freeQty_label = row.findViewById(R.id.freeQty_txt);
                holder.freeQty_label.setTypeface(FontUtils.getFontRoboto(HistoryDetailActivity.this, FontUtils.FontType.REGULAR));

                holder.value_label = row.findViewById(R.id.value_txt);
                holder.value_label.setTypeface(FontUtils.getFontRoboto(HistoryDetailActivity.this, FontUtils.FontType.REGULAR));

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


            if (!bmodel.configurationMasterHelper.SHOW_ORDER_CASE) {
                tvcase.setVisibility(View.GONE);
                holder.ll_case.setVisibility(View.GONE);


            } else {
                try {
                    tvcase.setVisibility(View.VISIBLE);
                    holder.ll_case.setVisibility(View.VISIBLE);
                    if (bmodel.labelsMasterHelper.applyLabels(tvcase.getTag()) != null)
                        tvcase
                                .setText(bmodel.labelsMasterHelper
                                        .applyLabels(tvcase.getTag()));
                } catch (Exception e) {
                    Commons.printException(e + "");
                }
            }
            if (!bmodel.configurationMasterHelper.SHOW_ORDER_PCS) {
                tvpiece.setVisibility(View.GONE);
                holder.ll_piece.setVisibility(View.GONE);


            } else {
                try {
                    tvpiece.setVisibility(View.VISIBLE);
                    holder.ll_piece.setVisibility(View.VISIBLE);
                    if (bmodel.labelsMasterHelper.applyLabels(tvpiece.getTag()) != null)
                        tvpiece
                                .setText(bmodel.labelsMasterHelper
                                        .applyLabels(tvpiece.getTag()));
                } catch (Exception e) {
                    Commons.printException(e + "");
                }
            }
            if (!bmodel.configurationMasterHelper.SHOW_OUTER_CASE) {
                tvouter.setVisibility(View.GONE);
                holder.ll_outer.setVisibility(View.GONE);
            } else {
                try {
                    tvouter.setVisibility(View.VISIBLE);
                    holder.ll_outer.setVisibility(View.VISIBLE);
                    if (bmodel.labelsMasterHelper.applyLabels(tvouter.getTag()) != null)
                        tvouter
                                .setText(bmodel.labelsMasterHelper
                                        .applyLabels(tvouter.getTag()));
                } catch (Exception e) {
                    Commons.printException(e + "");
                }
            }

            return row;
        }

        class ViewHolder {
            TextView brandname, piecevalue, casevalue, outervalue,freeQty,value;
            private TextView piece_label,case_label,outer_label,freeQty_label,value_label;
            private LinearLayout ll_piece,ll_case,ll_outer;
            public Vector<OrderHistoryBO> childHistoryList;
        }

    }

}
