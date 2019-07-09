package com.ivy.cpg.view.van.vanunload;

import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.bo.LoadManagementBO;
import com.ivy.sd.png.commons.IvyBaseActivityNoActionBar;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.util.Commons;
import com.ivy.sd.png.util.DataMembers;
import com.ivy.sd.png.util.StandardListMasterConstants;
import com.ivy.sd.print.CommonPrintPreviewActivity;

import java.util.ArrayList;

public class VanUnloadHistoryActivity extends IvyBaseActivityNoActionBar {

    // Declare Businness Model Class
    private BusinessModel bmodel;
    private VanUnLoadModuleHelper vanUnLoadModuleHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.vanunload_history_dialog);
        overridePendingTransition(R.anim.trans_left_in, R.anim.trans_left_out);

        bmodel = (BusinessModel) getApplicationContext();
        bmodel.setContext(this);
        vanUnLoadModuleHelper = VanUnLoadModuleHelper.getInstance(this.getApplicationContext());
        Toolbar toolbar = findViewById(R.id.toolbar);

        if (toolbar != null) {

            setSupportActionBar(toolbar);
            getSupportActionBar().setIcon(null);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            // Used to on / off the back arrow icon
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            // Used to remove the app logo actionbar icon and set title as home
            // (title support click)
            getSupportActionBar().setDisplayShowHomeEnabled(true);

            setScreenTitle(getString(R.string.history));
        }


        //initialize view
        RecyclerView unloadRecyclerView = findViewById(R.id.unload_history_recyclerView);
        unloadRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        unloadRecyclerView.setItemAnimator(new DefaultItemAnimator());

        unloadRecyclerView.setAdapter(new RecyclerAdapter(vanUnLoadModuleHelper.getUnloadHistoryList()));

        Button closeBtn = findViewById(R.id.closeButton);

        closeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                backButtonClick();
            }
        });
    }


    class RecyclerAdapter extends RecyclerView.Adapter<RecyclerAdapter.MyViewHolder> {
        private ArrayList<LoadManagementBO> items;

        public RecyclerAdapter(ArrayList<LoadManagementBO> items) {
            this.items = items;
        }

        @NonNull
        @Override
        public RecyclerAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(
                    parent.getContext()).inflate(R.layout.unload_history_row_item, parent, false);
            return new RecyclerAdapter.MyViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull RecyclerAdapter.MyViewHolder holder, int position) {
            final LoadManagementBO historyBo = items.get(position);

            if (position % 2 == 0)
                holder.layoutBackground.setBackgroundColor(ContextCompat.getColor(VanUnloadHistoryActivity.this, R.color.list_even_item_bg));
            else
                holder.layoutBackground.setBackgroundColor(ContextCompat.getColor(VanUnloadHistoryActivity.this, R.color.list_odd_item_bg));

            holder.invNoTv.setText(historyBo.getTransactionId());
            holder.totSalableQtyTv.setText(String.valueOf(historyBo.getMaxQty()));
            holder.totNSQtyTv.setText(String.valueOf(historyBo.getNonSalableQty()));


            holder.layoutBackground.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    if (bmodel.configurationMasterHelper.COMMON_PRINT_BIXOLON
                            || bmodel.configurationMasterHelper.COMMON_PRINT_ZEBRA
                            || bmodel.configurationMasterHelper.COMMON_PRINT_SCRYBE
                            || bmodel.configurationMasterHelper.COMMON_PRINT_LOGON
                            || bmodel.configurationMasterHelper.COMMON_PRINT_INTERMEC
                            || bmodel.configurationMasterHelper.COMMON_PRINT_MAESTROS) {

                        // Print file already saved.so not need to reload the object.we can get the object from print text file
                        bmodel.mCommonPrintHelper.readBuilder(StandardListMasterConstants.PRINT_FILE_UNLOAD + historyBo.getTransactionId() + ".txt",
                                DataMembers.PRINT_FILE_PATH);

                        Intent intent = new Intent(VanUnloadHistoryActivity.this,
                                CommonPrintPreviewActivity.class);
                        intent.putExtra("isHomeBtnEnable", true);
                        intent.putExtra("isFromVanUnload", true);
                        startActivity(intent);
                    }
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


        public class MyViewHolder extends RecyclerView.ViewHolder {
            TextView labelInvNoTV, labelSalableQtyTv, labelNSQtyTv;
            TextView invNoTv, totSalableQtyTv, totNSQtyTv;
            LinearLayout layoutBackground;

            public MyViewHolder(View itemView) {
                super(itemView);
                labelInvNoTV = itemView.findViewById(R.id.txt_label_inv_no);
                labelSalableQtyTv = itemView.findViewById(R.id.txt_label_salable_qty);
                labelNSQtyTv = itemView.findViewById(R.id.txt_label_ns_qty);

                invNoTv = itemView.findViewById(R.id.txt_history_inv_no);
                totSalableQtyTv = itemView.findViewById(R.id.txt_history_salable_qty);
                totNSQtyTv = itemView.findViewById(R.id.txt_history_ns_qty);

                layoutBackground = itemView.findViewById(R.id.list_background);


                try {
                    if (bmodel.labelsMasterHelper.applyLabels(labelInvNoTV.getTag()) != null)
                        labelInvNoTV.setText(bmodel.labelsMasterHelper
                                .applyLabels(labelInvNoTV
                                        .getTag()));

                    if (bmodel.labelsMasterHelper.applyLabels(labelNSQtyTv.getTag()) != null)
                        labelNSQtyTv.setText(bmodel.labelsMasterHelper
                                .applyLabels(labelNSQtyTv
                                        .getTag()));

                    if (bmodel.labelsMasterHelper.applyLabels(labelSalableQtyTv.getTag()) != null)
                        labelSalableQtyTv.setText(bmodel.labelsMasterHelper
                                .applyLabels(labelSalableQtyTv
                                        .getTag()));
                } catch (Exception e) {
                    Commons.printException("" + e);
                }


            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int i = item.getItemId();
        if (i == android.R.id.home) {
            backButtonClick();
        }
        return super.onOptionsItemSelected(item);
    }

    private void backButtonClick() {
        finish();
        overridePendingTransition(R.anim.trans_right_in, R.anim.trans_right_out);
    }
}
