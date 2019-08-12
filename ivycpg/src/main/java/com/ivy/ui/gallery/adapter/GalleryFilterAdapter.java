package com.ivy.ui.gallery.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatCheckBox;
import androidx.appcompat.widget.AppCompatTextView;
import androidx.recyclerview.widget.RecyclerView;

import com.ivy.sd.png.asean.view.R;

import java.util.ArrayList;

public class GalleryFilterAdapter extends RecyclerView.Adapter<GalleryFilterAdapter.ViewHolder> {
    private Context mContext;
    private UpdateListener filterViewListener;
    private ArrayList<String> sectionArrayList;
    private ArrayList<String> selectedSectionList;

    public GalleryFilterAdapter(Context mContext, ArrayList<String> menuList, UpdateListener filterViewListener, ArrayList<String> lastSelectionPos) {
        this.mContext = mContext;
        this.filterViewListener = filterViewListener;
        this.sectionArrayList = menuList;
        this.selectedSectionList = lastSelectionPos;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int i) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.single_text_with_check_box, parent, false);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String menuNameStr = sectionArrayList.get(position);
        holder.filterMenuTv.setText(menuNameStr);

    /*    holder.itemView.setOnClickListener(v ->
                filterViewListener.updateListAdapter(menuNameStr, holder.checkBox.isChecked(), position));*/

        holder.checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                filterViewListener.updateListAdapter(menuNameStr, isChecked);
            }
        });


        if (!selectedSectionList.isEmpty()
                && selectedSectionList.contains(menuNameStr))
            holder.checkBox.setChecked(true);
        else
            holder.checkBox.setChecked(false);
    }


    @Override
    public int getItemCount() {
        return sectionArrayList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private AppCompatTextView filterMenuTv;
        private AppCompatCheckBox checkBox;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            filterMenuTv = itemView.findViewById(R.id.selectedfilters);
            checkBox = itemView.findViewById(R.id.check_box);
        }
    }

    public interface UpdateListener {
        void updateListAdapter(String menuName, boolean isChecked);
    }
}
