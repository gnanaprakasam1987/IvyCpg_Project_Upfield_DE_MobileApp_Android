package com.ivy.cpg.view.salesdeliveryreturn;


import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.ivy.sd.png.asean.view.R;

import java.util.List;
import java.util.Vector;

public class SalesReturnDeliveryAdapter extends RecyclerView.Adapter<SalesReturnDeliveryAdapter.SalesReturnDeliveryViewHolder> {
    private RecyclerViewItemClickListener recyclerViewItemClickListener;
    private List<SalesReturnDeliveryDataModel> salesReturnDeliveryDataModelsList;
    private Context mContext;

    /**
     * Initialize the values
     *
     * @param context                       : context reference
     * @param recyclerViewItemClickListener : callBack Of ClickListener
     * @param salesReturnDeliveryDataModels : data
     */

    public SalesReturnDeliveryAdapter(Context context, RecyclerViewItemClickListener recyclerViewItemClickListener,
                                      Vector<SalesReturnDeliveryDataModel> salesReturnDeliveryDataModels) {
        this.recyclerViewItemClickListener = recyclerViewItemClickListener;
        mContext = context;
        this.salesReturnDeliveryDataModelsList = salesReturnDeliveryDataModels;
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
    public SalesReturnDeliveryAdapter.SalesReturnDeliveryViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_salesreturn_delivery, parent, false);
        return new SalesReturnDeliveryViewHolder(view);
    }

    /**
     * @param holder   :view Holder
     * @param position : position of each Row
     *                 set the values to the views
     */
    @Override
    public void onBindViewHolder(SalesReturnDeliveryAdapter.SalesReturnDeliveryViewHolder holder, int position) {
        holder.uId.setText(("UId : ") + salesReturnDeliveryDataModelsList.get(position).getUId());
        holder.returnValue.setText(("ReturnValue : "+salesReturnDeliveryDataModelsList.get(position).getReturnValue()));
        holder.lpc.setText(("LPC : "+salesReturnDeliveryDataModelsList.get(position).getLpc()));
        holder.dateReturn.setText("Date : " + salesReturnDeliveryDataModelsList.get(position).getDate());
        holder.invoice.setText("InvoiceNumber : " + salesReturnDeliveryDataModelsList.get(position).getInvoiceId());

    }

    @Override
    public int getItemCount() {
        return salesReturnDeliveryDataModelsList.size();
    }


    /**
     * Create The view First Time and hold for reuse
     * View Holder for Create and Hold the view for ReUse the views instead of create again
     * Initialize the views
     */

    public class SalesReturnDeliveryViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView uId, dateReturn, returnValue, lpc, invoice;

        private SalesReturnDeliveryViewHolder(View itemView) {
            super(itemView);
            uId = itemView.findViewById(R.id.txt_uid);
            dateReturn = itemView.findViewById(R.id.txt_dateReturn);


            returnValue = itemView.findViewById(R.id.txt_returnValue);
            lpc = itemView.findViewById(R.id.txt_lpc);
            invoice = itemView.findViewById(R.id.txt_invoice);
            itemView.setOnClickListener(this);
        }


        @Override
        public void onClick(View view) {
            recyclerViewItemClickListener.onItemClickListener(view, this.getAdapterPosition());
        }
    }
}

