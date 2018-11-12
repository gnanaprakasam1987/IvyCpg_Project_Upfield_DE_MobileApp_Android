package com.ivy.cpg.view.supervisor.mvp.outletmapview;


import android.content.Context;
import android.os.Build;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.ivy.cpg.view.supervisor.mvp.models.RetailerBo;
import com.ivy.lib.DialogFragment;
import com.ivy.sd.png.asean.view.R;
import com.ivy.utils.FontUtils;

import java.util.ArrayList;

public class OutletInfoHorizontalAdapter extends RecyclerView.Adapter<OutletInfoHorizontalAdapter.MyViewHolder> {

    private Context context;
    private ArrayList<RetailerBo> outletListBos = new ArrayList<>();
    private OutletMapViewPresenter outletMapViewPresenter;

    OutletInfoHorizontalAdapter(Context context, ArrayList<RetailerBo> outletListBos,OutletMapViewPresenter outletMapViewPresenter){
        this.context = context;
        this.outletListBos = outletListBos;
        this.outletMapViewPresenter = outletMapViewPresenter;
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {

        private TextView userName,retailerName,retailerAddress,totalOrderValue,statusText;
        private View statusView;

        public MyViewHolder(View view) {
            super(view);

            userName = view.findViewById(R.id.tv_seller_name);
            statusText = view.findViewById(R.id.tv_status_text);
            retailerName = view.findViewById(R.id.tv_store_name);
            retailerAddress = view.findViewById(R.id.tv_address);
            totalOrderValue = view.findViewById(R.id.tv_order_value);
            statusView = view.findViewById(R.id.status_color_view);

            userName.setTypeface(FontUtils.getFontRoboto(context, FontUtils.FontType.REGULAR));
            statusText.setTypeface(FontUtils.getFontRoboto(context, FontUtils.FontType.REGULAR));
            retailerName.setTypeface(FontUtils.getFontRoboto(context, FontUtils.FontType.MEDIUM));
            retailerAddress.setTypeface(FontUtils.getFontRoboto(context, FontUtils.FontType.REGULAR));
            totalOrderValue.setTypeface(FontUtils.getFontRoboto(context, FontUtils.FontType.MEDIUM));
            ((TextView) view.findViewById(R.id.tv_order_val_txt)).setTypeface(FontUtils.getFontRoboto(context, FontUtils.FontType.REGULAR));
            ((TextView) view.findViewById(R.id.tv_assigned_txt)).setTypeface(FontUtils.getFontRoboto(context, FontUtils.FontType.REGULAR));

        }
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.outlet_info_window_layout, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, final int position) {


        holder.userName.setText(outletListBos.get(position).getUserName());
        holder.retailerName.setText(outletListBos.get(position).getRetailerName());
        holder.retailerAddress.setText(outletListBos.get(position).getAddress());
        holder.totalOrderValue.setText(outletListBos.get(position).getTotalOrderValue()+"");


        int drawableId ;
        String statusTxt;
        if (!outletListBos.get(position).getIsOrdered() && outletListBos.get(position).isVisited()){
            drawableId = R.drawable.unbilled_bg_gradient;
            statusTxt = "Unbilled";
        }
        else if (!outletListBos.get(position).getIsOrdered() && !outletListBos.get(position).isVisited()){
            drawableId = R.drawable.planned_gradient_grey;
            statusTxt = "Planned";
        }
        else {
            drawableId = R.drawable.covered_green;
            statusTxt = "Covered";
        }

        holder.statusText.setText(statusTxt);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
            holder.statusView.setBackground(ContextCompat.getDrawable(context, drawableId));
        else
            holder.statusView.setBackgroundDrawable(ContextCompat.getDrawable(context, drawableId));

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(outletMapViewPresenter.getRetailerVisitDetailsByRId(outletListBos.get(position).getRetailerId()) == null){
                    Toast.makeText(context, "No visited details found for this retailer", Toast.LENGTH_SHORT).show();
                    return;
                }

                FragmentManager fm = ((FragmentActivity)context).getSupportFragmentManager();
                OutletPagerDialogFragment outletPagerDialogFragment = new OutletPagerDialogFragment(outletListBos.get(position).getRetailerId(),outletMapViewPresenter);
                outletPagerDialogFragment.setStyle(DialogFragment.STYLE_NO_FRAME, 0);
                outletPagerDialogFragment.setCancelable(false);
                outletPagerDialogFragment.show(fm,"OutletPager");
            }
        });

    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    @Override
    public int getItemCount() {
        return outletListBos.size();
    }
}