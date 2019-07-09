package com.ivy.cpg.view.reports.salesreturnreport;


import android.content.Context;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.commons.SDUtil;

import java.util.List;

public class SalesReturnReportAdapter extends RecyclerView.Adapter<SalesReturnReportAdapter.SalesReturnReportViewHolder> {
    private ReCyclerViewItemClickListener mreCyclerViewItemClickListener;
    List<SalesReturnReportBo> returnReportBosList;
    private Context mContext;

    /**
     * Initialize the values
     *
     * @param reCyclerViewItemClickListener : callBack Of ClickListener
     */

    public SalesReturnReportAdapter(Context context, ReCyclerViewItemClickListener reCyclerViewItemClickListener, List<SalesReturnReportBo> SalesReturnReportBoList) {
        this.mreCyclerViewItemClickListener = reCyclerViewItemClickListener;
        this.returnReportBosList = SalesReturnReportBoList;
        this.mContext = context;
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
    public SalesReturnReportAdapter.SalesReturnReportViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_salesreturn_report, parent, false);
        SalesReturnReportViewHolder myViewHolder = new SalesReturnReportViewHolder(view);
        return myViewHolder;
    }

    /**
     * @param holder   :view Holder
     * @param position : position of each Row
     *                 set the values to the views
     */
    @Override
    public void onBindViewHolder(SalesReturnReportAdapter.SalesReturnReportViewHolder holder, int position) {

        holder.uid.setText(String.valueOf(returnReportBosList.get(position).getUId()));
        holder.retailerName.setText(String.valueOf(returnReportBosList.get(position).getRetailerName()));
        holder.returnValue.setText(SDUtil.format(returnReportBosList.get(position).getReturnValue(),2,0)+"");
        holder.lpc.setText(String.valueOf(returnReportBosList.get(position).getLpc()));
        (holder.divider).setLayerType(View.LAYER_TYPE_SOFTWARE, null);
    }

    @Override
    public int getItemCount() {
        return returnReportBosList.size();
    }


    /**
     * Create The view First Time and hold for reuse
     * View Holder for Create and Hold the view for ReUse the views instead of create again
     * Initialize the views
     */

    public class SalesReturnReportViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView returnValue, lpc, retailerName, uid;
        View divider;

        public SalesReturnReportViewHolder(View itemView) {
            super(itemView);
            returnValue = itemView.findViewById(R.id.text_returnValue);
            lpc = itemView.findViewById(R.id.text_lpc);
            retailerName = itemView.findViewById(R.id.text_retailerName);
            divider = itemView.findViewById(R.id.invoiceview_doted_line);
            uid = itemView.findViewById(R.id.text_uid);
            itemView.setOnClickListener(this);
        }


        @Override
        public void onClick(View view) {
            mreCyclerViewItemClickListener.onItemClickListener(view, this.getAdapterPosition());
        }
    }
}



