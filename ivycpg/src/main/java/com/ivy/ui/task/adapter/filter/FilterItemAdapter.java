package com.ivy.ui.task.adapter.filter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.ivy.sd.png.asean.view.R;
import com.ivy.ui.task.model.FilterBo;

import java.util.ArrayList;
import java.util.HashMap;

public class FilterItemAdapter extends RecyclerView.Adapter<FilterItemAdapter.ViewHolder> {
    private Context mContext;
    private ArrayList<FilterBo> filterMenuList;
    private FilterMenuAdapter.UpdateListener updateListener;
    private HashMap<String, ArrayList<Object>> selectedHashMapList;
    private String selectedName;

    public FilterItemAdapter(Context context, ArrayList<FilterBo> filterMenuList, String selectedName, FilterMenuAdapter.UpdateListener updateListener, HashMap<String, ArrayList<Object>> selectedHashMapList) {
        this.mContext = context;
        this.filterMenuList = filterMenuList;
        this.updateListener = updateListener;
        this.selectedHashMapList = selectedHashMapList;
        this.selectedName = selectedName;
    }

    @NonNull
    @Override
    public FilterItemAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int i) {

        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.filter_secondary_list_item, parent, false);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull FilterItemAdapter.ViewHolder holder, int position) {
        FilterBo listItemBo = filterMenuList.get(position);

        holder.listItemTv.setText(listItemBo.getFilterName());

        if (selectedHashMapList.get(selectedName) != null
                && !selectedHashMapList.get(selectedName).isEmpty()
                && selectedHashMapList.get(selectedName).contains(listItemBo.getFilterId()))
            listItemBo.setChecked(true);
        else
            listItemBo.setChecked(false);

        updateView(holder.listSelectionImg, listItemBo.isChecked());
        holder.itemView.setOnClickListener(v -> {
            if (!listItemBo.isChecked()) {
                listItemBo.setChecked(true);
            } else {
                listItemBo.setChecked(false);
            }
            updateView(holder.listSelectionImg, listItemBo.isChecked());
            updateListener.updateSelectedId(selectedName, listItemBo.getFilterId(), listItemBo.isChecked());
        });


    }

    private void updateView(ImageView checkBoxImg, boolean isChecked) {
        int colorCode = 0;

        if (isChecked)
            colorCode = ContextCompat.getColor(mContext, R.color.green_productivity);
        else
            colorCode = ContextCompat.getColor(mContext, R.color.light_gray);

        checkBoxImg.setColorFilter(colorCode);
    }

    @Override
    public int getItemCount() {
        return filterMenuList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private TextView listItemTv;
        private ImageView listSelectionImg;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            listItemTv = itemView.findViewById(R.id.grid_item_text);
            listSelectionImg = itemView.findViewById(R.id.selectedfilters);
        }
    }
}
