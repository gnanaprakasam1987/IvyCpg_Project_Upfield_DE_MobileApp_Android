package com.ivy.ui.task.adapter.filter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.ivy.sd.png.asean.view.R;

import java.util.ArrayList;
import java.util.HashMap;

public class FilterMenuAdapter extends RecyclerView.Adapter<FilterMenuAdapter.ViewHolder> {
    private Context mContext;
    private ArrayList<String> menuList;
    private UpdateListener filterViewListener;
    private HashMap<String, ArrayList<Object>> selectedHashMap;

    public FilterMenuAdapter(Context mContext, ArrayList<String> menuList, UpdateListener filterViewListener, HashMap<String, ArrayList<Object>> selectedHashMap) {
        this.mContext = mContext;
        this.menuList = menuList;
        this.filterViewListener = filterViewListener;
        this.selectedHashMap = selectedHashMap;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int i) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.filter_grid_item, parent, false);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String menuNameStr = menuList.get(position);
        holder.itemView.setOnClickListener(v -> {

            if (holder.filterMenuTv.getCurrentTextColor()
                    == ContextCompat.getColor(mContext, R.color.list_item_primary_text_color)) {

                if (!selectedHashMap.containsKey(menuNameStr))
                    selectedHashMap.put(menuNameStr, new ArrayList<>());
            }

            menuList.set(position, menuList.get(position));
            updateListView(holder.getAdapterPosition(), holder.filterMenuTv);

        });

        holder.filterMenuTv.setText(menuNameStr);
        updateListView(holder.getAdapterPosition(), holder.filterMenuTv);

    }

    private void updateListView(int position, TextView filterMenuTv) {

        if (selectedHashMap.get(menuList.get(position)) != null) {
            filterMenuTv.setTextColor(ContextCompat.getColor(mContext, R.color.colorPrimary));
        } else {
            filterMenuTv.setTextColor(ContextCompat.getColor(mContext, R.color.list_item_primary_text_color));
        }


        filterViewListener.updateListAdapter(menuList.get(position), selectedHashMap);
    }

    @Override
    public int getItemCount() {
        return menuList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        private TextView filterMenuTv;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            filterMenuTv = itemView.findViewById(R.id.grid_item_text);
        }
    }

    public interface UpdateListener {

        void updateListAdapter(String menuName, HashMap<String, ArrayList<Object>> selectedHashMap);

        void updateSelectedId(String menuName, Object filterIds, boolean isChecked);

    }
}
