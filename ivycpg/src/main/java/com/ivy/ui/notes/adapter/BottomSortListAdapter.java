package com.ivy.ui.notes.adapter;

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
import com.ivy.ui.notes.NoteConstant;
import com.ivy.ui.notes.NoteOnclickListener;

import java.util.HashMap;

public class BottomSortListAdapter extends RecyclerView.Adapter<BottomSortListAdapter.SortListViewHolder> {
    private Context mContext;
    private String[] mSortListData;
    private NoteOnclickListener rowClickListener;
    private int lastSelectedPos;
    private HashMap<String, Integer> menuIcons;
    private int selectedTextColor;

    public BottomSortListAdapter(Context mContext, String[] mSortListData, NoteOnclickListener rowClickListener, int lastSelectedPos, int selectedTextColor) {
        this.mContext = mContext;
        this.mSortListData = mSortListData;
        this.rowClickListener = rowClickListener;
        this.lastSelectedPos = lastSelectedPos;
        this.selectedTextColor = selectedTextColor;

        menuIcons = new HashMap<String, Integer>() {
            {
                put(mContext.getString(R.string.recently_updated), R.drawable.ic_update_black_24dp);
                put(mContext.getString(R.string.created_date) + NoteConstant.ASC_ORD_DATE, R.drawable.ic_date_picker);
                put(mContext.getString(R.string.created_date) + NoteConstant.DESC_ORD_DATE, R.drawable.ic_date_picker);
                put(mContext.getString(R.string.modified_date) + NoteConstant.ASC_ORD_DATE, R.drawable.ic_date_picker);
                put(mContext.getString(R.string.modified_date) + NoteConstant.DESC_ORD_DATE, R.drawable.ic_date_picker);
            }
        };
    }

    @NonNull
    @Override
    public BottomSortListAdapter.SortListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.bottom_sheet_row_list, parent, false);

        return new SortListViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull BottomSortListAdapter.SortListViewHolder holder, int position) {

        if (holder.getAdapterPosition() == lastSelectedPos) {
            holder.sortIcon.setColorFilter(selectedTextColor, PorterDuff.Mode.SRC_IN);
            holder.sortNameTv.setTextColor(selectedTextColor);
        } else {
            holder.sortIcon.setColorFilter(ContextCompat.getColor(mContext, R.color.black_bg1), PorterDuff.Mode.MULTIPLY);
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
                notifyDataSetChanged();
                if (lastSelectedPos % 2 != 0
                        && lastSelectedPos != 0) {
                    rowClickListener.onSortClick(lastSelectedPos, true);
                } else {
                    rowClickListener.onSortClick(lastSelectedPos, false);
                }
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
