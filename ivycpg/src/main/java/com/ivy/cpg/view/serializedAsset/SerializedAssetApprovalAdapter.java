package com.ivy.cpg.view.serializedAsset;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.AppCompatImageView;
import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.provider.ConfigurationMasterHelper;
import com.ivy.utils.DateTimeUtils;

import java.util.ArrayList;

public class SerializedAssetApprovalAdapter extends RecyclerView.Adapter<SerializedAssetApprovalAdapter.ViewHolder> {
    private Context mContext;
    private ArrayList<SerializedAssetBO> approvalList;
    private BusinessModel bModel;

    public SerializedAssetApprovalAdapter(Context mContext, ArrayList<SerializedAssetBO> approvalList, BusinessModel bModel) {
        this.mContext = mContext;
        this.approvalList = approvalList;
        this.bModel = bModel;
    }

    @NonNull
    @Override
    public SerializedAssetApprovalAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.row_serialized_asset_approval, parent, false);

        return new SerializedAssetApprovalAdapter.ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull SerializedAssetApprovalAdapter.ViewHolder holder, int position) {
        SerializedAssetBO approvalBo = approvalList.get(position);

        holder.assetNameTV.setText(approvalBo.getAssetName());

        //Concat serial no label name with serial no
        String serialNo = mContext.getResources().getString(R.string.serial_no);
        if (bModel.labelsMasterHelper.applyLabels(holder.serialNoTv.getTag()) != null)
            serialNo = bModel.labelsMasterHelper
                    .applyLabels(holder.serialNoTv.getTag());

        serialNo = serialNo + ": " + approvalBo.getSerialNo();
        holder.serialNoTv.setText(serialNo);

        //Concat Requested Date label name with date
        String reqDateStr = mContext.getResources().getString(R.string.requested_date);
        if (bModel.labelsMasterHelper.applyLabels(holder.requestedDateTv.getTag()) != null)
            reqDateStr = bModel.labelsMasterHelper
                    .applyLabels(holder.requestedDateTv.getTag());

        reqDateStr = reqDateStr + ": "
                + (DateTimeUtils.convertFromServerDateToRequestedFormat(approvalBo.getRequestedDate(), ConfigurationMasterHelper.outDateFormat));
        holder.requestedDateTv.setText(reqDateStr);

        //concat transfer type label name with Transfer type
        String transferTypeStr = mContext.getResources().getString(R.string.type);
        if (bModel.labelsMasterHelper.applyLabels(holder.transferType.getTag()) != null)
            transferTypeStr = bModel.labelsMasterHelper
                    .applyLabels(holder.transferType.getTag());

        transferTypeStr = transferTypeStr + ": " + approvalBo.getTransferType();
        holder.transferType.setText(transferTypeStr);

        holder.itemView.setOnClickListener(v -> {
            if (!approvalBo.isChecked()) {
                approvalBo.setChecked(true);
                approvalBo.setApprovalStatus("Approved");
            } else {
                approvalBo.setChecked(false);
                approvalBo.setApprovalStatus("Pending");
            }
            updateView(holder.checkImage, approvalBo.isChecked(), approvalBo.getApprovalStatus());
        });

        updateView(holder.checkImage, approvalBo.isChecked(), approvalBo.getApprovalStatus());
    }

    private void updateView(AppCompatImageView checkBoxImg, boolean isChecked, String approvalStatus) {
        int colorCode = 0;

        if (isChecked || approvalStatus.equalsIgnoreCase("Approved"))
            colorCode = ContextCompat.getColor(mContext, R.color.green_productivity);
        else
            colorCode = ContextCompat.getColor(mContext, R.color.light_gray);

        checkBoxImg.setColorFilter(colorCode);
    }

    @Override
    public int getItemCount() {
        return approvalList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private AppCompatImageView checkImage;
        private AppCompatTextView assetNameTV;
        private AppCompatTextView serialNoTv;
        private AppCompatTextView requestedDateTv;
        private AppCompatTextView transferType;

        public ViewHolder(View itemView) {
            super(itemView);

            checkImage = itemView.findViewById(R.id.check_img);
            assetNameTV = itemView.findViewById(R.id.asset_name_tv);
            serialNoTv = itemView.findViewById(R.id.serial_no_tv);
            requestedDateTv = itemView.findViewById(R.id.requested_date_tv);
            transferType = itemView.findViewById(R.id.requested_type_tv);


        }
    }
}
