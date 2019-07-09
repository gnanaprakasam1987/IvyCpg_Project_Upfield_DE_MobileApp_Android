package com.ivy.sd.png.view;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.ivy.sd.png.asean.view.R;

import java.util.ArrayList;
import java.util.HashMap;

public class BrandsAdapter extends RecyclerView.Adapter<BrandsAdapter.MyViewHolder> {
    public interface OnItemClickListener {
        void onItemClick(String item);
    }

    private final OnItemClickListener listener;
    private final ArrayList<String> items;
    private final Context context;
    private HashMap<String, HashMap<String, Object>> mBrandNameColorCount;
    private String mSelectedBrandName;

    public BrandsAdapter(Context context, ArrayList<String> items, HashMap<String, HashMap<String, Object>> mBrandNameColorCount, String mSelectedBrandName, OnItemClickListener listener) {
        this.items = items;
        this.listener = listener;
        this.context = context;
        this.mBrandNameColorCount = mBrandNameColorCount;
        this.mSelectedBrandName = mSelectedBrandName;
    }


    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView text;
        public LinearLayout ll_base;
        public ImageView ivIndicator;

        public MyViewHolder(View itemView) {
            super(itemView);

            text = (TextView) itemView
                    .findViewById(R.id.txtBrandNameListItem);
            ll_base = (LinearLayout) itemView
                    .findViewById(R.id.ll_base);
            ivIndicator = (ImageView) itemView
                    .findViewById(R.id.ivIndicator);
        }

        @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
        public void bind(final String item, final OnItemClickListener listener) {
            text.setTypeface(Typeface.createFromAsset(context.getAssets(), "font/Roboto-Medium.ttf"));
            text.setText(item);
            if (mSelectedBrandName != null) {
                if (mSelectedBrandName.equalsIgnoreCase(item)) {
                    ll_base.setBackground(ContextCompat.getDrawable(context, R.drawable.layout_rounded_corner_blue));
                } else {
                    ll_base.setBackground(ContextCompat.getDrawable(context, R.drawable.button_round_corner_transparent));
                }
            }
            ((GradientDrawable) ivIndicator.getBackground()).setColor((Integer) mBrandNameColorCount.get(item).get("color"));

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.onItemClick(item);
                }
            });
        }

    }


    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.list_item_brands, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        holder.bind(items.get(position), listener);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    //Method used to set selected brand item.
    public void setSelectedItem(String item) {
        mSelectedBrandName= item;
    }


}