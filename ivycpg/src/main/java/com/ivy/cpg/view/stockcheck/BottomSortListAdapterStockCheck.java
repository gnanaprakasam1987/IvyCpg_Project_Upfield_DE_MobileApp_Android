package com.ivy.cpg.view.stockcheck;

import android.content.Context;
import android.graphics.PorterDuff;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.ivy.sd.png.asean.view.R;

import java.util.HashMap;

public class BottomSortListAdapterStockCheck extends RecyclerView.Adapter<BottomSortListAdapterStockCheck.SortListViewHolder> {
    private Context mContext;
    private String[] mSortListData;
    private StockCheckClickListener rowClickListener;
    private int lastSelectedPos;
    private HashMap<String, Integer> menuIcons;

    public BottomSortListAdapterStockCheck(Context mContext, String[] mSortListData, StockCheckClickListener rowClickListener, int lastSelectedPos) {
        this.mContext = mContext;
        this.mSortListData = mSortListData;
        this.rowClickListener = rowClickListener;
        this.lastSelectedPos = lastSelectedPos;

        menuIcons = new HashMap<String, Integer>() {
            {
                put("Availability Asc", R.drawable.ic_ascending_sort);
                put("Availability Desc", R.drawable.ic_descending_sort);
                put("IsDistribution Asc", R.drawable.ic_ascending_sort);
                put("IsDistribution Desc", R.drawable.ic_descending_sort);
                put("Default", R.drawable.ic_rollback);
            }
        };
    }

    @NonNull
    @Override
    public BottomSortListAdapterStockCheck.SortListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.row_task_sort, parent, false);

        return new BottomSortListAdapterStockCheck.SortListViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull BottomSortListAdapterStockCheck.SortListViewHolder holder, int position) {

        if (holder.getAdapterPosition() == lastSelectedPos) {
            holder.sortIcon.setColorFilter(ContextCompat.getColor(mContext, R.color.colorPrimary), PorterDuff.Mode.SRC_IN);
            holder.sortNameTv.setTextColor(ContextCompat.getColor(mContext, R.color.colorPrimary));
        } else {
            holder.sortIcon.setColorFilter(ContextCompat.getColor(mContext, R.color.black_bg1), android.graphics.PorterDuff.Mode.MULTIPLY);
            holder.sortNameTv.setTextColor(ContextCompat.getColor(mContext, R.color.caption_text_color));
        }

        Integer i = menuIcons.get(mSortListData[position]);
        if (i != null)
            holder.sortIcon.setImageResource(i);
        else
            holder.sortIcon.setImageResource(menuIcons.get(mSortListData[position]));

        holder.sortNameTv.setText(mSortListData[position]);


        holder.sortLL.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                lastSelectedPos = holder.getAdapterPosition();
                rowClickListener.onSortItemClicked(lastSelectedPos);
            }
        });


    }

    @Override
    public int getItemCount() {
        return mSortListData.length;
    }

    public class SortListViewHolder extends RecyclerView.ViewHolder {
        LinearLayout sortLL;
        ImageView sortIcon;
        TextView sortNameTv;

        public SortListViewHolder(View itemView) {
            super(itemView);
            sortLL = itemView.findViewById(R.id.row_sort_ll);
            sortIcon = itemView.findViewById(R.id.list_item_icon_sort);
            sortNameTv = itemView.findViewById(R.id.list_item_menu_sort_tv);
        }
    }

}
