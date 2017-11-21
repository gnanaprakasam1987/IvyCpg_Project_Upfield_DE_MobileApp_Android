package com.ivy.sd.png.view.van;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.AppCompatCheckBox;
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
import android.widget.Toast;

import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.bo.StockReportMasterBO;
import com.ivy.sd.png.commons.IvyBaseActivityNoActionBar;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.provider.ConfigurationMasterHelper;
import com.ivy.sd.png.util.CommonDialog;
import com.ivy.sd.png.util.Commons;
import com.ivy.sd.png.view.HomeScreenActivity;

import java.util.HashMap;
import java.util.Vector;


public class VanLoadStockApplyActivity extends IvyBaseActivityNoActionBar implements View.OnClickListener {
    private Toolbar toolbar;
    BusinessModel bmodel;
    private Vector<StockReportMasterBO> mylist;
    private Vector<StockReportMasterBO> mylist2;
    private Button applybtn, rejectbtn;
    private TextView labelTxt1, labelTxt2, toolbarTxt;
    private String uid = null;
    Vector<String> SIHApplyById;
    private HashMap<String, Integer> mManuvalVanloadFlagByuid;
    private TypedArray typearr;
    private RecyclerView StockApplyListView;
    private LinearLayout bottomLayout;
    private StockApplyAdapter stockApplyAdapter;
    private String screenTitle = null;
    private int proTotLine;
    private Intent loadActivity;
    private boolean isFromPlanning = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        Commons.print("vanloadstockview activity");
        setContentView(R.layout.activity_van_load_stock_apply);
        overridePendingTransition(R.anim.trans_left_in, R.anim.trans_left_out);
        bmodel = (BusinessModel) this.getApplicationContext();
        bmodel.setContext(this);
        getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        toolbarTxt = (TextView) findViewById(R.id.tv_toolbar_title);
        labelTxt1 = (TextView) findViewById(R.id.tv_van_loadNo);
        labelTxt2 = (TextView) findViewById(R.id.total_linesTitle);
        applybtn = (Button) findViewById(R.id.van_btn_accept);
        rejectbtn = (Button) findViewById(R.id.van_btn_reject);
        StockApplyListView = (RecyclerView) findViewById(R.id.lvwplist);
        bottomLayout = (LinearLayout) findViewById(R.id.bottom_layout);

        toolbarTxt.setTypeface(bmodel.configurationMasterHelper.getFontBaloobhai(ConfigurationMasterHelper.FontType.REGULAR));
        labelTxt1.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
        labelTxt2.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
        applybtn.setTypeface(bmodel.configurationMasterHelper.getFontBaloobhai(ConfigurationMasterHelper.FontType.REGULAR));
        rejectbtn.setTypeface(bmodel.configurationMasterHelper.getFontBaloobhai(ConfigurationMasterHelper.FontType.REGULAR));


        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(null);
            toolbarTxt.setText(getIntent().getStringExtra("screentitle"));
            getSupportActionBar().setIcon(null);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        isFromPlanning = getIntent().getBooleanExtra("planingsub", false);
        screenTitle = getIntent().getStringExtra("screentitle");
        StockApplyListView.setHasFixedSize(true);
        StockApplyListView.setLayoutManager(new LinearLayoutManager(this));

        SIHApplyById = bmodel.configurationMasterHelper.getSIHApplyById();
        applybtn.setOnClickListener(this);
        rejectbtn.setOnClickListener(this);
        if (bmodel.userMasterHelper.getUserMasterBO().getUserid() == 0) {
            Toast.makeText(this,
                    getResources().getString(R.string.sessionout_loginagain),
                    Toast.LENGTH_SHORT).show();
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
            mylist2 = new Vector<>();
            mManuvalVanloadFlagByuid = new HashMap<String, Integer>();
            for (int i = 0; i < mylist.size(); i++) {
                mylist2.add(mylist.get(i));
                mManuvalVanloadFlagByuid.put(mylist.get(i).getUid(), mylist.get(i)
                        .getIsManuvalVanload());
            }


            for (int i = 0; i < mylist2.size(); i++) {

                for (int j = i + 1; j < mylist2.size(); j++) {
                    if (mylist2.get(i).getLoadNO().equals(mylist2.get(j).getLoadNO())) {
                        mylist2.remove(j);
                        j--;
                    }
                }
            }

            stockApplyAdapter = new StockApplyAdapter(mylist2);
            StockApplyListView.setAdapter(stockApplyAdapter);

        } catch (Resources.NotFoundException e) {
            Commons.printException("" + e);
        }


    }

    @Override
    public void onClick(View v) {
        Button view = (Button) v;
        if (view == applybtn) {

            if (uid != null && selected_position != -1) {

                new CommonDialog(getApplicationContext(), this, "", getResources().getString(
                        R.string.alert_text_accept_Stock), false, getResources().getString(R.string.ok), getResources().getString(R.string.cancel), new CommonDialog.positiveOnClickListener() {
                    @Override
                    public void onPositiveButtonClick() {
                        SIHApplyById.add(uid);
                        new UpdateSIH().execute();
                        applybtn.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.round_disabled_btn));
                        applybtn.setTextColor(Color.WHITE);
                        applybtn.setEnabled(false);
                        rejectbtn.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.round_disabled_btn));
                        rejectbtn.setTextColor(Color.WHITE);
                        rejectbtn.setEnabled(false);
                    }
                }, new CommonDialog.negativeOnClickListener() {
                    @Override
                    public void onNegativeButtonClick() {
                    }
                }).show();
            } else {
                Toast.makeText(getApplicationContext(), "Check any one list", Toast.LENGTH_SHORT).show();
            }

        } else if (view == rejectbtn) {
            if (uid != null && selected_position != -1) {

                new CommonDialog(getApplicationContext(), this, "", getResources().getString(
                        R.string.alert_text_reject_Stock), false, getResources().getString(R.string.ok), getResources().getString(R.string.cancel), new CommonDialog.positiveOnClickListener() {
                    @Override
                    public void onPositiveButtonClick() {
                        SIHApplyById.add(uid);
                        applybtn.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.round_disabled_btn));
                        applybtn.setTextColor(Color.WHITE);
                        applybtn.setEnabled(false);
                        rejectbtn.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.round_disabled_btn));
                        rejectbtn.setTextColor(Color.WHITE);
                        rejectbtn.setEnabled(false);
                        new RejectVanload().execute();
                    }
                }, new CommonDialog.negativeOnClickListener() {
                    @Override
                    public void onNegativeButtonClick() {
                    }
                }).show();
            } else {
                Toast.makeText(getApplicationContext(), "Check any one list", Toast.LENGTH_SHORT).show();
            }
        }

    }

    private int selected_position = -1;

    public class StockApplyAdapter extends RecyclerView.Adapter<StockApplyAdapter.ViewHolder> {

        private Vector<StockReportMasterBO> items;

        public StockApplyAdapter(Vector<StockReportMasterBO> items) {
            this.items = items;
        }

        @Override
        public StockApplyAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.custom_van_load_stock_listitem, parent, false);

            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(StockApplyAdapter.ViewHolder holder, final int position) {

            final StockReportMasterBO projObj = items.get(position);

            if (position % 2 == 0)
                holder.listBgLayout.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.white));
            else
                holder.listBgLayout.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.history_list_bg));


            proTotLine = bmodel.stockreportmasterhelper.getNoProductsCount(projObj.getUid());
            holder.vanLoadNoTxt.setText(projObj.getLoadNO());
            holder.vanLoadDateTxt.setText(projObj.getDate());
            holder.totalLineTxt.setText(proTotLine + "");

            if (selected_position == position) {
                holder.checkBoxList.setChecked(true);

            } else {
                holder.checkBoxList.setChecked(false);

            }
            holder.checkBoxList.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    uid = projObj.getUid();


                    if (((AppCompatCheckBox) v).isChecked()) {
                        selected_position = position;

                    } else {
                        selected_position = -1;
                    }

                    notifyDataSetChanged();
                    if (!bmodel.startjourneyclicked
                            || bmodel.configurationMasterHelper.STOCK_APPROVAL) {

                        if (SIHApplyById.contains(uid)) {
                            applybtn.setEnabled(false);
                            applybtn.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.round_disabled_btn));
                            applybtn.setTextColor(Color.WHITE);
                            rejectbtn.setEnabled(false);
                            rejectbtn.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.round_disabled_btn));
                            rejectbtn.setTextColor(Color.WHITE);
                        } else {

                            applybtn.setEnabled(true);
                            applybtn.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.button_rounded_corner_blue));
                            rejectbtn.setEnabled(true);
                            rejectbtn.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.button_rounded_corner_blue));
                        }
                    }

                }
            });

            holder.listBgLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    bmodel.stockreportmasterhelper.setStockReportMaster(mylist);
                    Intent i = new Intent(VanLoadStockApplyActivity.this, VanLoadStockView_activity.class);
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
            private AppCompatCheckBox checkBoxList;
            private TextView vanLoadNoTxt, totalLineTxt, vanLoadDateTxt;

            public ViewHolder(View itemView) {
                super(itemView);

                listBgLayout = (LinearLayout) itemView.findViewById(R.id.header_list);
                checkBoxList = (AppCompatCheckBox) itemView.findViewById(R.id.stock_apply_listview_cb);
                vanLoadNoTxt = (TextView) itemView.findViewById(R.id.vanLoad_no_list);
                vanLoadDateTxt = (TextView) itemView.findViewById(R.id.vanLoad_date);
                totalLineTxt = (TextView) itemView.findViewById(R.id.total_lines_txt);

                vanLoadNoTxt.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
                vanLoadDateTxt.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
                totalLineTxt.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));

            }
        }
    }


    @Override
    protected void onResume() {
        super.onResume();
        BusinessModel.getInstance().trackScreenView("VanLoad Stock View");
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        if (id == android.R.id.home) {

            loadActivity = new Intent(VanLoadStockApplyActivity.this, HomeScreenActivity.class);
            if (isFromPlanning)
                loadActivity.putExtra("menuCode", "MENU_PLANNING_SUB");
            else
                loadActivity.putExtra("menuCode", "MENU_LOAD_MANAGEMENT");
            startActivity(loadActivity);
            finish();
            onBackButtonClick();


        }

        return super.onOptionsItemSelected(item);
    }

    private void onBackButtonClick() {
        overridePendingTransition(R.anim.trans_right_in, R.anim.trans_right_out);
    }

    class UpdateSIH extends AsyncTask<Integer, Integer, Boolean> {
        private AlertDialog.Builder builder;
        private AlertDialog alertDialog;

        protected void onPreExecute() {
            builder = new AlertDialog.Builder(VanLoadStockApplyActivity.this);

            customProgressDialog(builder,  "Applying Vanload Stock ");
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
            Toast.makeText(VanLoadStockApplyActivity.this, "Vanload Stock Applied ",
                    Toast.LENGTH_SHORT).show();
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
            Toast.makeText(VanLoadStockApplyActivity.this, "Vanload Stock Rejected ",
                    Toast.LENGTH_SHORT).show();
        }

    }
}
