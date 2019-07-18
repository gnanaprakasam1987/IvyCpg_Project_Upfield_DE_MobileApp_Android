package com.ivy.cpg.view.delivery.salesreturn;


import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.ivy.sd.png.asean.view.R;

import java.util.List;
import java.util.Vector;

public class SalesReturnDeliveryAdapter extends RecyclerView.Adapter<SalesReturnDeliveryAdapter.SalesReturnDeliveryViewHolder> {
    private RecyclerViewItemClickListener recyclerViewItemClickListener;
    private List<SalesReturnDeliveryDataBo> salesReturnDeliveryDataModelsList;

    /**
     * Initialize the values
     *
     * @param recyclerViewItemClickListener : callBack Of ClickListener
     * @param salesReturnDeliveryDataModels : data
     */

    public SalesReturnDeliveryAdapter(RecyclerViewItemClickListener recyclerViewItemClickListener,
                                      Vector<SalesReturnDeliveryDataBo> salesReturnDeliveryDataModels) {
        this.recyclerViewItemClickListener = recyclerViewItemClickListener;
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
    public void onBindViewHolder(final SalesReturnDeliveryAdapter.SalesReturnDeliveryViewHolder holder, final int position) {
        holder.relativeLayout.setVisibility(View.VISIBLE);
        holder.uId.setText(("UId : ") + salesReturnDeliveryDataModelsList.get(position).getUId());
        holder.dateReturn.setText("Date : " + salesReturnDeliveryDataModelsList.get(position).getDate());
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
        TextView uId, dateReturn;
        RelativeLayout relativeLayout;


        private SalesReturnDeliveryViewHolder(View itemView) {
            super(itemView);
            uId = itemView.findViewById(R.id.txt_uid);
            dateReturn = itemView.findViewById(R.id.txt_dateReturn);
            relativeLayout = itemView.findViewById(R.id.container_salesReturnItem);

            itemView.setOnClickListener(this);
        }


        @Override
        public void onClick(View view) {
            if (recyclerViewItemClickListener != null)
                recyclerViewItemClickListener.onItemClickListener(view, this.getAdapterPosition());
        }
    }


}

