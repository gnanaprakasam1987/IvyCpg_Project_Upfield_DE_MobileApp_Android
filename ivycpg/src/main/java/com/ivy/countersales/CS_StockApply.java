package com.ivy.countersales;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.ivy.countersales.bo.CS_StockApplyHeaderBO;
import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.commons.IvyBaseActivityNoActionBar;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.provider.ConfigurationMasterHelper;
import com.ivy.sd.png.view.HomeScreenActivity;

import java.util.Vector;


public class CS_StockApply extends IvyBaseActivityNoActionBar implements View.OnClickListener {
    private Toolbar toolbar;
    private BusinessModel bmodel;
    private Vector<CS_StockApplyHeaderBO> mylist;
    private Button btnManualLoad;
    private TextView toolbarTxt;
    private RecyclerView StockApplyListView;
    private StockApplyAdapter stockApplyAdapter;
    private Intent loadActivity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        setContentView(R.layout.activity_cs_stock_apply);

        //overridePendingTransition(R.anim.trans_left_in, R.anim.trans_left_out);

        bmodel = (BusinessModel) this.getApplicationContext();
        bmodel.setContext(this);

        if (bmodel.userMasterHelper.getUserMasterBO().getUserid() == 0) {
            Toast.makeText(this,
                    getResources().getString(R.string.sessionout_loginagain),
                    Toast.LENGTH_SHORT).show();
            finish();
        }

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbarTxt = (TextView) findViewById(R.id.tv_toolbar_title);
        toolbarTxt.setTypeface(bmodel.configurationMasterHelper.getFontBaloobhai(ConfigurationMasterHelper.FontType.REGULAR));

        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(null);
            toolbarTxt.setText(bmodel.mSelectedActivityName);
            getSupportActionBar().setIcon(null);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        btnManualLoad = (Button) findViewById(R.id.manual_load);
        btnManualLoad.setTypeface(bmodel.configurationMasterHelper.getFontBaloobhai(ConfigurationMasterHelper.FontType.REGULAR));

        btnManualLoad.setOnClickListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        BusinessModel.getInstance().trackScreenView("CS Stock Apply - Header");

        //mylist = bmodel.CS_StockApplyHelper.getCSStockApplyHeader();
        Vector<CS_StockApplyHeaderBO> tempList = bmodel.CS_StockApplyHelper.getCSStockApplyHeader();

        mylist = new Vector<>();
        for(CS_StockApplyHeaderBO head : tempList){
            if(head.getStatus().equalsIgnoreCase("I"))
                mylist.add(head);
        }

        for(CS_StockApplyHeaderBO head : tempList){
            if(!head.getStatus().equalsIgnoreCase("I"))
                mylist.add(head);
        }


        stockApplyAdapter = new StockApplyAdapter(mylist);

        StockApplyListView = (RecyclerView) findViewById(R.id.lvwplist);
        StockApplyListView.setHasFixedSize(true);
        StockApplyListView.setLayoutManager(new LinearLayoutManager(this));
        StockApplyListView.setAdapter(stockApplyAdapter);
    }

    @Override
    public void onClick(View v) {
        Button view = (Button) v;
        if (view == btnManualLoad) {
            bmodel.CS_StockApplyHelper.loadProductForManualLoad();
            Intent i = new Intent(CS_StockApply.this, CS_StockApply_Detail.class);
            i.putExtra("isManualLoad",true);
            i.putExtra("screenName","Manual Receipt");
            i.putExtra("CurrentActivityCode","MENU_STOCK_APPLY_CS");
            startActivity(i);
        }
    }




    public class StockApplyAdapter extends RecyclerView.Adapter<StockApplyAdapter.ViewHolder> {

        private Vector<CS_StockApplyHeaderBO> items;

        public StockApplyAdapter(Vector<CS_StockApplyHeaderBO> items) {
            this.items = items;
        }

        @Override
        public StockApplyAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.activity_cs_stock_apply_list, parent, false);

            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(StockApplyAdapter.ViewHolder holder, final int position) {

            final CS_StockApplyHeaderBO header = items.get(position);

            holder.stock_receipt_id.setText(header.getReferenceNo());
            holder.receipt_date.setText(header.getReceiptDate());
            holder.stock_type.setText(header.getStockType());

            String status = getResources().getString(R.string.tab_text_pending);
            int statusColor = R.color.Black;
            if(header.getStatus().equalsIgnoreCase("A")){
                status = getResources().getString(R.string.tab_text_approve);
                statusColor = R.color.GREEN;
            } else if(header.getStatus().equalsIgnoreCase("P")){
                status = getResources().getString(R.string.partial_applied);
                statusColor = R.color.GREEN_LIGHT;
            } else if(header.getStatus().equalsIgnoreCase("R")){
                status = getResources().getString(R.string.rejected);
                statusColor = R.color.RED;
            }  else if(header.getStatus().equalsIgnoreCase("M")){
                status = getResources().getString(R.string.manual);
                statusColor = R.color.Burgundy;
            }

            holder.status.setText(status);
            holder.status.setTextColor(ContextCompat.getColor(CS_StockApply.this, statusColor));


            holder.cardviewlist.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent i = new Intent(CS_StockApply.this, CS_StockApply_Detail.class);
                    i.putExtra("receipt_id",header.getReceiptId());
                    i.putExtra("typeid",header.getStockTypeId());
                    startActivity(i);
                }
            });
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        @Override
        public int getItemViewType(int position) {
            return position;
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            private TextView stock_receipt_id, status;
            private TextView receipt_date, stock_type;
            private CardView cardviewlist;
            private TextView status_lebel, receipt_date_lebel, stock_type_lebel,stock_receipt_id_label;

            public ViewHolder(View itemView) {
                super(itemView);

                cardviewlist = (CardView) itemView.findViewById(R.id.cardviewlist);

                stock_receipt_id = (TextView) itemView.findViewById(R.id.stock_receipt_id);
                stock_receipt_id_label = (TextView) itemView.findViewById(R.id.stock_receipt_id_label);
                stock_receipt_id.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.BOLD));
                stock_receipt_id_label.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.REGULAR));

                status = (TextView) itemView.findViewById(R.id.status);
                status_lebel = (TextView) itemView.findViewById(R.id.status_lebel);
                status.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
                status_lebel.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.REGULAR));

                receipt_date = (TextView) itemView.findViewById(R.id.receipt_date);
                receipt_date_lebel = (TextView) itemView.findViewById(R.id.receipt_date_lebel);
                receipt_date.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
                receipt_date_lebel.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.REGULAR));

                stock_type = (TextView) itemView.findViewById(R.id.stock_type);
                stock_type_lebel = (TextView) itemView.findViewById(R.id.stock_type_lebel);
                stock_type.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
                stock_type_lebel.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.REGULAR));

            }
        }
    }




    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        if (id == android.R.id.home) {

            loadActivity = new Intent(CS_StockApply.this, HomeScreenActivity.class).putExtra("menuCode","MENU_COUNTER");
            startActivity(loadActivity);
            overridePendingTransition(R.anim.trans_right_in, R.anim.trans_right_out);
            finish();
            //onBackButtonClick();
        }

        return super.onOptionsItemSelected(item);
    }

    private void onBackButtonClick() {
        overridePendingTransition(R.anim.trans_right_in, R.anim.trans_right_out);
    }
}
