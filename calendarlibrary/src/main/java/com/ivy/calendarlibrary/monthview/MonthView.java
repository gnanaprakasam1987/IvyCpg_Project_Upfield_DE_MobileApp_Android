package com.ivy.calendarlibrary.monthview;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.ivy.calendarlibrary.R;

/**
 * Created by mansoor on 02/04/2019
 */
public class MonthView extends FrameLayout {
    private MonthRecyclerImpl recycler;
    private int columnCount = 8;

    public MonthView(@NonNull Context context) {
        super(context);
        init(context, null);
    }

    public MonthView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public MonthView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    private void init(@NonNull Context context, AttributeSet attrs) {
        inflate(context, R.layout.month_recycler_view, this);
        recycler = findViewById(R.id.recycler);
    }

    public void setColumnCount(int columnCount) {
        this.columnCount = columnCount;
    }

    public void setLayoutManager(Context context) {

        recycler.setHasFixedSize(true);
        recycler.setLayoutManager(new GridLayoutManager(context, columnCount, LinearLayoutManager.VERTICAL,false));
        recycler.setItemAnimator(new DefaultItemAnimator());

    }

    public void setAdapter(Adapter adapter) {
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
