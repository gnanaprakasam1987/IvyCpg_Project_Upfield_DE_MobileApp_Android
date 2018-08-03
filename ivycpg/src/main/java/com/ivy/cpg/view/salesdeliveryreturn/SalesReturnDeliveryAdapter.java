package com.ivy.cpg.view.salesdeliveryreturn;


import android.content.Context;
import android.support.constraint.ConstraintLayout;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.commons.SDUtil;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.provider.ConfigurationMasterHelper;
import com.ivy.sd.png.view.DeliveryManagementDetail;

import java.util.List;
import java.util.Vector;

public class SalesReturnDeliveryAdapter extends RecyclerView.Adapter<SalesReturnDeliveryAdapter.SalesReturnDeliveryViewHolder> {
    private RecyclerViewItemClickListener recyclerViewItemClickListener;
    private List<SalesReturnDeliveryDataBo> salesReturnDeliveryDataModelsList;
    private Context mContext;
    private boolean isDetails;

    private EditText QUANTITY;
    /**
     * Initialize the values
     *
     * @param context                       : context reference
     * @param recyclerViewItemClickListener : callBack Of ClickListener
     * @param salesReturnDeliveryDataModels : data
     */

    public SalesReturnDeliveryAdapter(Context context, RecyclerViewItemClickListener recyclerViewItemClickListener,
                                      Vector<SalesReturnDeliveryDataBo> salesReturnDeliveryDataModels, boolean isDetails) {
        this.recyclerViewItemClickListener = recyclerViewItemClickListener;
        mContext = context;
        this.salesReturnDeliveryDataModelsList = salesReturnDeliveryDataModels;
        this.isDetails = isDetails;
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

            holder.uId.setTypeface(((BusinessModel) mContext).configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
            holder.dateReturn.setTypeface(((BusinessModel) mContext).configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
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
        TextView uId, dateReturn ;
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

