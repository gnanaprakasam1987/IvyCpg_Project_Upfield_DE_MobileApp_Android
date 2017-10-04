package com.ivy.sd.png.view.profile;

import android.content.res.TypedArray;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.bo.OrderHistoryBO;
import com.ivy.sd.png.commons.IvyBaseActivityNoActionBar;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.provider.ConfigurationMasterHelper;
import com.ivy.sd.png.util.Commons;

import java.util.Vector;

/**
 * Created by mayuri.v on 5/16/2017.
 */
public class HistoryDetailActivity extends IvyBaseActivityNoActionBar {
    private BusinessModel bmodel;
    int selectedPosition = -1;
    TextView tvpiece, tvcase, tvouter;
    String fromScreen="";

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
        TextView toolBarTitle = (TextView) toolbar.findViewById(R.id.tv_toolbar_title);
        toolBarTitle.setText("Invoice");
        toolBarTitle.setTypeface(bmodel.configurationMasterHelper.getFontBaloobhai(ConfigurationMasterHelper.FontType.REGULAR));
        getSupportActionBar().setIcon(null);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        tvpiece = (TextView) findViewById(R.id.tvpiece);
        tvcase = (TextView) findViewById(R.id.tvcase);
        tvouter = (TextView) findViewById(R.id.tvouter);

        ((TextView) findViewById(R.id.tvbrand)).setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
        ((TextView) findViewById(R.id.tvorder)).setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
        tvouter.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
        tvpiece.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
        tvcase.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));

        ListView lv = (ListView) findViewById(R.id.lvwplist);
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
            if(fromScreen.equalsIgnoreCase("OrderHistory")){
                orderList = bmodel.profilehelper.getChild_orderHistoryList()
                        .get(selectedPosition);
            }else if(fromScreen.equalsIgnoreCase("InvoiceHistory")){
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
                holder.casevalue.setVisibility(View.GONE);


            } else {
                try {
                    tvcase.setVisibility(View.VISIBLE);
                    holder.casevalue.setVisibility(View.VISIBLE);
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
                holder.piecevalue.setVisibility(View.GONE);


            } else {
                try {
                    tvpiece.setVisibility(View.VISIBLE);
                    holder.piecevalue.setVisibility(View.VISIBLE);
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
                holder.outervalue.setVisibility(View.GONE);
            } else {
                try {
                    tvouter.setVisibility(View.VISIBLE);
                    holder.outervalue.setVisibility(View.VISIBLE);
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
            TextView brandname, piecevalue, casevalue, outervalue;
            public Vector<OrderHistoryBO> childHistoryList;
        }

    }

}
