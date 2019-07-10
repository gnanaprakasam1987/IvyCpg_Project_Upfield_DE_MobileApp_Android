package com.ivy.cpg.view.supervisor.mvp;

import androidx.recyclerview.widget.RecyclerView;
import android.view.ViewGroup;

import com.ivy.cpg.view.supervisor.mvp.models.ManagerialBO;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ramkumard on 28/2/19.
 */

public abstract class MultiLevelAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    List<ManagerialBO> recyclerViewItemList = new ArrayList<>();

    public MultiLevelAdapter(List<ManagerialBO> recyclerViewItems) {
        this.recyclerViewItemList = recyclerViewItems;
    }

    public void setRecyclerViewItemList(List<ManagerialBO> recyclerViewItemList) {
        this.recyclerViewItemList = recyclerViewItemList;
    }

    @Override
    public abstract RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType);

    @Override
    public abstract void onBindViewHolder(RecyclerView.ViewHolder holder, int position);

    @Override
    public int getItemCount() {
        return recyclerViewItemList.size();
    }

    @Override
    public int getItemViewType(int position) {
        return recyclerViewItemList.get(position).getLevel();
    }

    public List<ManagerialBO> getRecyclerViewItemList() {
        return recyclerViewItemList;
    }
}
