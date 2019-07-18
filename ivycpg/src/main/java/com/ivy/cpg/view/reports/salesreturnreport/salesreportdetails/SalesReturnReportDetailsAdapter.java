package com.ivy.cpg.view.reports.salesreturnreport.salesreportdetails;

import android.content.Context;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.ivy.cpg.view.salesreturn.SalesReturnHelper;
import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.commons.SDUtil;
import com.ivy.sd.png.model.BusinessModel;

import java.util.List;


public class SalesReturnReportDetailsAdapter extends RecyclerView.Adapter<SalesReturnReportDetailsAdapter.SalesReturnReportViewHolder> {
    List<SalesReturnDetailsReportBo> salesReturnReportBosList;
    private Context mContext;

    private BusinessModel businessModel;
    private SalesReturnHelper salesReturnHelper;

    /**
     * Initialize the values
     *
     * @param salesReturnReportBosList : salesReturnReportBosList reference
     */

    public SalesReturnReportDetailsAdapter(Context context, List<SalesReturnDetailsReportBo> salesReturnReportBosList) {
        this.salesReturnReportBosList = salesReturnReportBosList;
        this.mContext = context;
        businessModel = (BusinessModel) context.getApplicationContext();
        salesReturnHelper=SalesReturnHelper.getInstance(context);
    }


    /**
     * @param parent   : parent ViewPgroup
     * @param viewType : viewType
     * @return ViewHolder
     * <p>
     * Inflate the Views
     * Create the each views and Hold for Reuse
     */
    @Override
    public SalesReturnReportDetailsAdapter.SalesReturnReportViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_salesreturndetails_report, parent, false);
        SalesReturnReportViewHolder myViewHolder = new SalesReturnReportViewHolder(view);
        return myViewHolder;
    }

    /**
     * @param holder   :view Holder
     * @param position : position of each Row
     *                 set the values to the views
     */
    @Override
    public void onBindViewHolder(SalesReturnReportDetailsAdapter.SalesReturnReportViewHolder holder, int position) {
        holder.productName.setText(salesReturnReportBosList.get(position).getProductName());
        holder.caseQty.setText(String.valueOf(salesReturnReportBosList.get(position).getCaseQty()));
        holder.pieceQty.setText(String.valueOf(salesReturnReportBosList.get(position).getPieceQty()));
        holder.value.setText(String.valueOf(SDUtil.format(salesReturnReportBosList.get(position).getReturnValue(), 2, 0)));
        holder.reason.setText(salesReturnReportBosList.get(position).getReason());
        holder.reasonType.setText(salesReturnReportBosList.get(position).getReasonType());
        holder.outerQty.setText(String.valueOf(salesReturnReportBosList.get(position).getOuterQty()));


        if (!salesReturnHelper.SHOW_SALES_RET_CASE)
            holder.caseQty.setVisibility(View.GONE);
        if (!salesReturnHelper.SHOW_SALES_RET_PCS)
            holder.pieceQty.setVisibility(View.GONE);
        if (!salesReturnHelper.SHOW_SALES_RET_OUTER_CASE)
            holder.outerQty.setVisibility(View.GONE);

        if (position % 2 == 0)
            holder.view.setBackgroundColor(ContextCompat.getColor(mContext, R.color.white));
        else
            holder.view.setBackgroundColor(ContextCompat.getColor(mContext, R.color.white));

    }

    @Override
    public int getItemCount() {
        return salesReturnReportBosList.size();
    }


    /**
     * Create The view First Time and hold for reuse
     * View Holder for Create and Hold the view for ReUse the views instead of create again
     * Initialize the views
     */

    public class SalesReturnReportViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView productName, caseQty, pieceQty, outerQty, value, reason, reasonType;
        View view;

        public SalesReturnReportViewHolder(View itemView) {
            super(itemView);
            productName = itemView.findViewById(R.id.text_productName);
            caseQty = itemView.findViewById(R.id.text_caseQty);
            pieceQty = itemView.findViewById(R.id.text_pieceQty);
            value = itemView.findViewById(R.id.text_value);
            outerQty = itemView.findViewById(R.id.text_outerQty);
            reason = itemView.findViewById(R.id.text_reason);
            reasonType = itemView.findViewById(R.id.text_reasonType);
            view = itemView;
            itemView.setOnClickListener(this);
        }


        @Override
        public void onClick(View view) {
        }
    }
}
