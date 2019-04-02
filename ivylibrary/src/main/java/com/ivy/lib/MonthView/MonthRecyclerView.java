package com.ivy.lib.MonthView;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.ivy.lib.R;

/**
 * Created by mansoor on 02/04/2019
 */
public class MonthRecyclerView extends FrameLayout {
    private MonthRecyclerImpl recycler;
    private int columnCount = 7;

    public MonthRecyclerView(@NonNull Context context) {
        super(context);
        init(context, null);
    }

    public MonthRecyclerView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public MonthRecyclerView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    private void init(@NonNull Context context, AttributeSet attrs) {
        inflate(context, R.layout.month_recycler_view, this);
        recycler = findViewById(R.id.recycler);
    }

    public void setColumnCount (int columnCount){
        this.columnCount = columnCount;
    }

    public void setLayoutManager(Context context){
        recycler.setHasFixedSize(true);
        recycler.setHasFixedSize(true);
        recycler.addItemDecoration(new DividerItemDecoration(context,
                DividerItemDecoration.HORIZONTAL));
        recycler.addItemDecoration(new DividerItemDecoration(context,
                DividerItemDecoration.VERTICAL));
        recycler.setLayoutManager(new GridLayoutManager(context, columnCount));
    }

    public void setAdapter(Adapter adapter){
        recycler.setAdapter(adapter);
    }

    public static abstract class Adapter<VH extends RecyclerView.ViewHolder> extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return onCreateHolder(parent);
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            onBindHolder((VH) holder, position);
        }

        @Override
        public int getItemCount() {
            return getCount();
        }

        public abstract VH onCreateHolder(ViewGroup parent);

        public abstract int getCount();

        public abstract void onBindHolder(VH holder, int position);
    }
}
