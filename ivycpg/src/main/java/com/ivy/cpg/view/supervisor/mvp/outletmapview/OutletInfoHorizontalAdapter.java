package com.ivy.cpg.view.supervisor.mvp.outletmapview;


import android.content.Context;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.ivy.cpg.view.supervisor.fragments.OutletPagerDialogFragment;
import com.ivy.lib.DialogFragment;
import com.ivy.sd.png.asean.view.R;

public class OutletInfoHorizontalAdapter extends RecyclerView.Adapter<OutletInfoHorizontalAdapter.MyViewHolder> {

    private Context context;

    OutletInfoHorizontalAdapter(Context context){
        this.context = context;
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {

        private TextView userName;

        public MyViewHolder(View view) {
            super(view);

            userName = view.findViewById(R.id.tv_user_name);

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

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                FragmentManager fm = ((FragmentActivity)context).getSupportFragmentManager();

//                OutletPagerDialogFragment outletPagerDialogFragment = new OutletPagerDialogFragment();
//                outletPagerDialogFragment.setStyle(DialogFragment.STYLE_NO_FRAME, 0);
//                outletPagerDialogFragment.setCancelable(false);
//                outletPagerDialogFragment.show(fm,"OutletPager");
            }
        });

    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    @Override
    public int getItemCount() {
        return 4;
    }
}