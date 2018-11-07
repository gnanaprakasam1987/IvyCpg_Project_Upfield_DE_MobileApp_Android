package com.ivy.cpg.view.van.vanstockapply;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.AppCompatRadioButton;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.commons.IvyBaseActivityNoActionBar;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.util.CommonDialog;
import com.ivy.sd.png.util.Commons;
import com.ivy.utils.FontUtils;

import java.util.HashMap;
import java.util.Vector;


public class VanLoadStockApplyActivity extends IvyBaseActivityNoActionBar implements View.OnClickListener {
    private BusinessModel bmodel;
    private Vector<VanLoadStockApplyBO> mylist;
    private String uid = null;
    Vector<String> SIHApplyById;
    private HashMap<String, Integer> mManuvalVanloadFlagByuid;
    private String screenTitle = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_van_load_stock_apply);
        overridePendingTransition(R.anim.trans_left_in, R.anim.trans_left_out);
        bmodel = (BusinessModel) this.getApplicationContext();
        bmodel.setContext(this);
        getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        TextView toolbarTxt = (TextView) findViewById(R.id.tv_toolbar_title);
        TextView labelTxt1 = (TextView) findViewById(R.id.tv_van_loadNo);
        TextView labelTxt2 = (TextView) findViewById(R.id.total_linesTitle);
        Button applybtn = (Button) findViewById(R.id.van_btn_accept);
        Button rejectbtn = (Button) findViewById(R.id.van_btn_reject);
        RecyclerView stockApplyListView = (RecyclerView) findViewById(R.id.list);
        LinearLayout bottomLayout = (LinearLayout) findViewById(R.id.bottom_layout);

        toolbarTxt.setTypeface(FontUtils.getFontBalooHai(this, FontUtils.FontType.REGULAR));
        labelTxt1.setTypeface(FontUtils.getFontRoboto(this, FontUtils.FontType.MEDIUM));
        labelTxt2.setTypeface(FontUtils.getFontRoboto(this, FontUtils.FontType.MEDIUM));
        applybtn.setTypeface(FontUtils.getFontBalooHai(this, FontUtils.FontType.REGULAR));
        rejectbtn.setTypeface(FontUtils.getFontBalooHai(this, FontUtils.FontType.REGULAR));


        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(null);
            toolbarTxt.setText(getIntent().getStringExtra("screentitle"));
            getSupportActionBar().setIcon(null);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        screenTitle = getIntent().getStringExtra("screentitle");
        stockApplyListView.setHasFixedSize(true);
        stockApplyListView.setLayoutManager(new LinearLayoutManager(this));

        SIHApplyById = bmodel.configurationMasterHelper.getSIHApplyById();
        applybtn.setOnClickListener(this);
        rejectbtn.setOnClickListener(this);
        if (bmodel.userMasterHelper.getUserMasterBO().getUserid() == 0) {
            showMessage(getString(R.string.sessionout_loginagain));
            finish();
        }

        applybtn.setVisibility(View.VISIBLE);
        if (bmodel.configurationMasterHelper.IS_SHOW_REJECT_BTN)
            rejectbtn.setVisibility(View.VISIBLE);
        else
            rejectbtn.setVisibility(View.GONE);

        if (bmodel.configurationMasterHelper.HIDE_STOCK_APPLY_BUTTON)
            bottomLayout.setVisibility(View.GONE);

        mylist = bmodel.stockreportmasterhelper.getStockReportMaster();


        try {
            Vector<VanLoadStockApplyBO> mylist2 = new Vector<>();
            mManuvalVanloadFlagByuid = new HashMap<String, Integer>();
            for (int i = 0; i < mylist.size(); i++) {
                mylist2.add(mylist.get(i));
                mManuvalVanloadFlagByuid.put(mylist.get(i).getUid(), mylist.get(i)
                        .getIsManualVanload());
            }


            for (int i = 0; i < mylist2.size(); i++) {

                for (int j = i + 1; j < mylist2.size(); j++) {
                    if (mylist2.get(i).getLoadNO().equals(mylist2.get(j).getLoadNO())) {
                        mylist2.remove(j);
                        j--;
                    }
                }
            }

            StockApplyAdapter stockApplyAdapter = new StockApplyAdapter(mylist2);
            stockApplyListView.setAdapter(stockApplyAdapter);

        } catch (Resources.NotFoundException e) {
            Commons.printException("" + e);
        }


    }

    @Override
    public void onClick(View v) {
        int BtnId = v.getId();
        if (BtnId == R.id.van_btn_accept) {

            if (uid != null && selected_position != -1) {

                new CommonDialog(getApplicationContext(), this, "", getResources().getString(
                        R.string.alert_text_accept_Stock), false, getResources().getString(R.string.ok), getResources().getString(R.string.cancel), new CommonDialog.PositiveClickListener() {
                    @Override
                    public void onPositiveButtonClick() {
                        SIHApplyById.add(uid);
                        new UpdateSIH().execute();
                    }
                }, new CommonDialog.negativeOnClickListener() {
                    @Override
                    public void onNegativeButtonClick() {
                    }
                }).show();
            } else {
                showMessage(getString(R.string.check_anyone_fromlist));
            }

        } else if (BtnId == R.id.van_btn_reject) {
            if (uid != null && selected_position != -1) {

                new CommonDialog(getApplicationContext(), this, "", getResources().getString(
                        R.string.alert_text_reject_Stock), false, getResources().getString(R.string.ok), getResources().getString(R.string.cancel), new CommonDialog.PositiveClickListener() {
                    @Override
                    public void onPositiveButtonClick() {
                        SIHApplyById.add(uid);
                        new RejectVanload().execute();
                    }
                }, new CommonDialog.negativeOnClickListener() {
                    @Override
                    public void onNegativeButtonClick() {
                    }
                }).show();
            } else {
                showMessage(getString(R.string.check_anyone_fromlist));
            }
        }

    }

    private int selected_position = -1;

    public class StockApplyAdapter extends RecyclerView.Adapter<StockApplyAdapter.ViewHolder> {

        private Vector<VanLoadStockApplyBO> items;

        public StockApplyAdapter(Vector<VanLoadStockApplyBO> items) {
            this.items = items;
        }

        @Override
        public StockApplyAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.custom_van_load_stock_listitem, parent, false);

            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(StockApplyAdapter.ViewHolder holder, final int position) {

            final VanLoadStockApplyBO projObj = items.get(position);

            if (position % 2 == 0)
                holder.listBgLayout.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.white));
            else
                holder.listBgLayout.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.history_list_bg));


            int proTotLine = bmodel.stockreportmasterhelper.getNoProductsCount(projObj.getUid());
            holder.vanLoadNoTxt.setText(projObj.getLoadNO());
            holder.vanLoadDateTxt.setText(projObj.getDate());
            holder.totalLineTxt.setText(proTotLine + "");


            if (selected_position == position) {
                holder.radioButtonList.setChecked(true);
            } else {
                holder.radioButtonList.setChecked(false);
            }

            if ((bmodel.startjourneyclicked
                    && bmodel.configurationMasterHelper.STOCK_APPROVAL) || SIHApplyById.contains(projObj.getUid())) {
                holder.radioButtonList.setEnabled(false);
                if (holder.radioButtonList.isChecked())
                    holder.radioButtonList.setChecked(false);
            } else {
                holder.radioButtonList.setEnabled(true);
            }

            holder.radioButtonList.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    uid = projObj.getUid();

                    if (((AppCompatRadioButton) v).isChecked()) {
                        selected_position = position;
                    } else {
                        selected_position = -1;
                    }
                    notifyDataSetChanged();
                }
            });

            holder.listBgLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    bmodel.configurationMasterHelper.loadVanStockUOMConfiguration();
                    bmodel.stockreportmasterhelper.setStockReportMaster(mylist);
                    Intent i = new Intent(VanLoadStockApplyActivity.this, VanLoadStockViewActivity.class);
                    i.putExtra("screentitle", screenTitle);
                    i.putExtra("uid", projObj.getUid());
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
            private LinearLayout listBgLayout;
            private AppCompatRadioButton radioButtonList;
            private TextView vanLoadNoTxt, totalLineTxt, vanLoadDateTxt;

            public ViewHolder(View itemView) {
                super(itemView);

                listBgLayout = (LinearLayout) itemView.findViewById(R.id.header_list);
                radioButtonList = itemView.findViewById(R.id.stock_apply_listview_cb);
                vanLoadNoTxt = (TextView) itemView.findViewById(R.id.vanLoad_no_list);
                vanLoadDateTxt = (TextView) itemView.findViewById(R.id.vanLoad_date);
                totalLineTxt = (TextView) itemView.findViewById(R.id.total_lines_txt);

                vanLoadNoTxt.setTypeface(FontUtils.getFontRoboto(VanLoadStockApplyActivity.this, FontUtils.FontType.MEDIUM));
                vanLoadDateTxt.setTypeface(FontUtils.getFontRoboto(VanLoadStockApplyActivity.this, FontUtils.FontType.LIGHT));
                totalLineTxt.setTypeface(FontUtils.getFontRoboto(VanLoadStockApplyActivity.this, FontUtils.FontType.LIGHT));

            }
        }
    }


    @Override
    protected void onResume() {
        super.onResume();

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            finish();
            overridePendingTransition(R.anim.trans_right_in, R.anim.trans_right_out);
        }
        return super.onOptionsItemSelected(item);
    }


    class UpdateSIH extends AsyncTask<Integer, Integer, Boolean> {
        private AlertDialog.Builder builder;
        private AlertDialog alertDialog;

        protected void onPreExecute() {
            builder = new AlertDialog.Builder(VanLoadStockApplyActivity.this);

            customProgressDialog(builder, "Applying Vanload Stock ");
            alertDialog = builder.create();
            alertDialog.show();
        }

        @Override
        protected Boolean doInBackground(Integer... params) {
            try {

                int flag = 0;
                if (mManuvalVanloadFlagByuid.get(uid) != null) {
                    flag = mManuvalVanloadFlagByuid.get(uid);
                }
                bmodel.stockreportmasterhelper.downloadBatchwiseVanlod();
                bmodel.stockreportmasterhelper.updateSIHMaster(mylist,
                        SIHApplyById, uid, flag);
                if (flag == 1) {
                    bmodel.stockreportmasterhelper.updateVanload(uid);
                }


            } catch (Exception e) {
                Commons.printException("" + e);
                return Boolean.FALSE;
            }
            return Boolean.TRUE;
        }

        protected void onProgressUpdate(Integer... progress) {

        }

        protected void onPostExecute(Boolean result) {
            alertDialog.dismiss();
            showMessage(getString(R.string.vanload_stock_applied));
            finish();
            overridePendingTransition(R.anim.trans_right_in, R.anim.trans_right_out);

        }

    }

    class RejectVanload extends AsyncTask<Integer, Integer, Boolean> {

        private AlertDialog.Builder builder;
        private AlertDialog alertDialog;

        protected void onPreExecute() {
            builder = new AlertDialog.Builder(VanLoadStockApplyActivity.this);

            customProgressDialog(builder, "Applying Vanload Stock ");
            alertDialog = builder.create();
            alertDialog.show();
        }

        @Override
        protected Boolean doInBackground(Integer... params) {
            int flag = 0;
            if (mManuvalVanloadFlagByuid.get(uid) != null) {
                flag = mManuvalVanloadFlagByuid.get(uid);
            }
            bmodel.stockreportmasterhelper.rejectVanload(uid, flag);
            return true;
        }

        protected void onPostExecute(Boolean result) {

            alertDialog.dismiss();
            showMessage(getString(R.string.vanload_reject_applied));

        }

    }
}
