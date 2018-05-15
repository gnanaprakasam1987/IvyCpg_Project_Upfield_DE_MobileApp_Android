package com.ivy.cpg.view.reports.taskreport;

import android.support.v7.widget.RecyclerView;

import com.ivy.sd.png.bo.TaskDataBO;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * Created by sajena on 15/5/18.
 */

public abstract class RecyclerItemAdapter extends RecyclerView.Adapter {

    List items = new ArrayList<>();
    RecyclerItemAdapter(){
        setHasStableIds(true);
    }


    public void add(TaskDataBO object) {
        items.add(object);
        notifyDataSetChanged();
    }

    public void add(int index, TaskDataBO object) {
        items.add(index, object);
        notifyDataSetChanged();
    }

    public void addAll(Collection collection) {
        if (collection != null) {
            items.addAll(collection);
            notifyDataSetChanged();
        }
    }

    public void addAll(TaskDataBO... items) {
        addAll(Arrays.asList(items));
    }

    public void clear() {
        items.clear();
        notifyDataSetChanged();
    }

    public void remove(TaskDataBO object) {
        items.remove(object);
        notifyDataSetChanged();
    }

}