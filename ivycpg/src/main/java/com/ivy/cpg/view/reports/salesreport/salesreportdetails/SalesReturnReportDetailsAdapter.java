package com.ivy.cpg.view.reports.salesreport.salesreportdetails;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.ivy.cpg.view.reports.salesreport.ReCyclerViewItemClickListener;
import com.ivy.cpg.view.reports.salesreport.SalesReturnReportBo;
import com.ivy.sd.png.asean.view.R;

import java.util.List;


public class SalesReturnReportDetailsAdapter extends RecyclerView.Adapter<SalesReturnReportDetailsAdapter.SalesReturnReportViewHolder> {
    private ReCyclerViewItemClickListener mreCyclerViewItemClickListener;
    private Context mContext;
    List<SalesReturnReportBo> salesReturnReportBosList;

    /**
     * Initialize the values
     *
     * @param context                       : context reference
     * @param reCyclerViewItemClickListener : callBack Of ClickListener
     */

    public SalesReturnReportDetailsAdapter(Context context, ReCyclerViewItemClickListener reCyclerViewItemClickListener, List<SalesReturnReportBo> devices) {
        this.mContext = context;
        this.mreCyclerViewItemClickListener = reCyclerViewItemClickListener;
        this.salesReturnReportBosList = devices;
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

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.row_orderdetail_report, parent, false);
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
        //  holder.deviceType.setText(salesReturnReportBosList.get(position).getDeviceType());
        //  holder.model.setText(salesReturnReportBosList.get(position).getModel());
        //  holder.name.setText(salesReturnReportBosList.get(position).getName());

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
        TextView deviceType, model, name;

        public SalesReturnReportViewHolder(View itemView) {
            super(itemView);
            //  deviceType = itemView.findViewById(R.id.txt_deviceType);
            //   model = itemView.findViewById(R.id.txt_model);
            //  name = itemView.findViewById(R.id.txt_name);
            itemView.setOnClickListener(this);
        }


        @Override
        public void onClick(View view) {
            // mreCyclerViewItemClickListener.onItemClickListener(view, this.getAdapterPosition());
        }
    }
}
