package com.ivy.cpg.view.supervisor.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.TextView;

import com.ivy.cpg.view.supervisor.customviews.recyclerviewpager.RecyclerViewPager;
import com.ivy.cpg.view.supervisor.customviews.scrollingpagerindicator.ScrollingPagerIndicator;
import com.ivy.lib.DialogFragment;
import com.ivy.sd.png.asean.view.R;
import com.ivy.utils.FontUtils;

import java.util.ArrayList;
import java.util.List;

import me.relex.circleindicator.CircleIndicator;

public class OutletPagerDialogFragment extends DialogFragment {

    private View rootView;
    private ViewPager viewPager;
    private ViewPagerAdapter viewPagerAdapter;
    private CircleIndicator circleIndicator;
    private TextView tvStoreCount;
    private RecyclerViewPager mRecyclerView;
    private MyAdapter myAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.outlet_pager_dialog_fragment, container, false);

        initViews(rootView);


        rootView.findViewById(R.id.close_img).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        return rootView;
    }

    @Override
    public void onStart() {
        super.onStart();
        if(getDialog().getWindow()!=null)
            getDialog().getWindow()
                .setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.MATCH_PARENT);

    }
    protected void initViews(View rootView) {
        tvStoreCount = rootView.findViewById(R.id.tv_store_count);

        tvStoreCount.setTypeface(FontUtils.getFontRoboto(FontUtils.FontType.REGULAR,getContext().getApplicationContext()));

        viewPager = rootView.findViewById(R.id.outlet_view_pager);
        viewPagerAdapter = new ViewPagerAdapter(getContext(), new ArrayList<String>());
        viewPager.setAdapter(viewPagerAdapter);
        viewPagerAdapter.notifyDataSetChanged();

        circleIndicator = rootView.findViewById(R.id.indicator);
        circleIndicator.setViewPager(viewPager);


        mRecyclerView = rootView.findViewById(R.id.viewpager);
//        mRecyclerView.setVisibility(View.GONE);
        LinearLayoutManager layout = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL,
                false);
        mRecyclerView.setLayoutManager(layout);

        myAdapter = new MyAdapter(getContext().getApplicationContext());
        mRecyclerView.setAdapter(myAdapter);

        ScrollingPagerIndicator recyclerIndicator = rootView.findViewById(R.id.scroll_indicator);
        recyclerIndicator.attachToRecyclerView(mRecyclerView);

        mRecyclerView.setHasFixedSize(true);
        mRecyclerView.setLongClickable(true);
        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int scrollState) {
//                updateState(scrollState);
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int i, int i2) {
                int childCount = mRecyclerView.getChildCount();
                int width = mRecyclerView.getChildAt(0).getWidth();
                int padding = (mRecyclerView.getWidth() - width) / 2;

                for (int j = 0; j < childCount; j++) {
                    View v = recyclerView.getChildAt(j);
                    float rate = 0;
                    if (v.getLeft() <= padding) {
                        if (v.getLeft() >= padding - v.getWidth()) {
                            rate = (padding - v.getLeft()) * 1f / v.getWidth();
                        } else {
                            rate = 1;
                        }
                        v.setScaleY(1 - rate * 0.1f);
                        v.setScaleX(1 - rate * 0.1f);

                    } else {
                        if (v.getLeft() <= recyclerView.getWidth() - padding) {
                            rate = (recyclerView.getWidth() - padding - v.getLeft()) * 1f / v.getWidth();
                        }
                        v.setScaleY(0.9f + rate * 0.1f);
                        v.setScaleX(0.9f + rate * 0.1f);
                    }
                }
            }
        });



        mRecyclerView.addOnPageChangedListener(new RecyclerViewPager.OnPageChangedListener() {
            @Override
            public void OnPageChanged(int oldPosition, int newPosition) {

            }
        });

        mRecyclerView.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                if (mRecyclerView.getChildCount() < 3) {
                    if (mRecyclerView.getChildAt(1) != null) {
                        if (mRecyclerView.getCurrentPosition() == 0) {
                            View v1 = mRecyclerView.getChildAt(1);
                            v1.setScaleY(0.9f);
                            v1.setScaleX(0.9f);
                        } else {
                            View v1 = mRecyclerView.getChildAt(0);
                            v1.setScaleY(0.9f);
                            v1.setScaleX(0.9f);
                        }
                    }
                } else {
                    if (mRecyclerView.getChildAt(0) != null) {
                        View v0 = mRecyclerView.getChildAt(0);
                        v0.setScaleY(0.9f);
                        v0.setScaleX(0.9f);
                    }
                    if (mRecyclerView.getChildAt(2) != null) {
                        View v2 = mRecyclerView.getChildAt(2);
                        v2.setScaleY(0.9f);
                        v2.setScaleX(0.9f);
                    }
                }

            }
        });
    }


    public class MyAdapter extends RecyclerView.Adapter<MyAdapter.MyViewHolder> {

        private Context context;

        MyAdapter(Context context){
            this.context = context;
        }

        public class MyViewHolder extends RecyclerView.ViewHolder {

            TextView tvStoreName,tvStoreAddress,tvVisitStatus,tvInTime,tvOutTime,tvDuration,tvOrderValue;

            public MyViewHolder(View view) {
                super(view);

                tvStoreName = view.findViewById(R.id.tv_store_name);
                tvStoreAddress = view.findViewById(R.id.tv_address);
                tvVisitStatus = view.findViewById(R.id.tv_status_text);
                tvInTime = view.findViewById(R.id.tv_in_time_val);
                tvOutTime = view.findViewById(R.id.tv_out_time_val);
                tvDuration = view.findViewById(R.id.tv_duration_val);
                tvOrderValue = view.findViewById(R.id.tv_order_value);

                ((TextView)view.findViewById(R.id.tv_intime_txt)).setTypeface(FontUtils.getFontRoboto(FontUtils.FontType.REGULAR,context));
                ((TextView)view.findViewById(R.id.tv_outtime_txt)).setTypeface(FontUtils.getFontRoboto(FontUtils.FontType.REGULAR,context));
                ((TextView)view.findViewById(R.id.tv_duration_txt)).setTypeface(FontUtils.getFontRoboto(FontUtils.FontType.REGULAR,context));
                ((TextView)view.findViewById(R.id.tv_order_val_txt)).setTypeface(FontUtils.getFontRoboto(FontUtils.FontType.REGULAR,context));

                tvStoreName.setTypeface(FontUtils.getFontRoboto(FontUtils.FontType.MEDIUM,context));
                tvStoreAddress.setTypeface(FontUtils.getFontRoboto(FontUtils.FontType.REGULAR,context));
                tvVisitStatus.setTypeface(FontUtils.getFontRoboto(FontUtils.FontType.REGULAR,context));
                tvInTime.setTypeface(FontUtils.getFontRoboto(FontUtils.FontType.REGULAR,context));
                tvOutTime.setTypeface(FontUtils.getFontRoboto(FontUtils.FontType.REGULAR,context));
                tvDuration.setTypeface(FontUtils.getFontRoboto(FontUtils.FontType.REGULAR,context));
                tvOrderValue.setTypeface(FontUtils.getFontRoboto(FontUtils.FontType.MEDIUM,context));

            }
        }

        @Override
        public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.supervisor_outlet_dialog_layout, parent, false);

            return new MyViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(MyViewHolder holder, final int position) {



        }

        @Override
        public int getItemViewType(int position) {
            return position;
        }

        @Override
        public int getItemCount() {
            return 3;
        }
    }

    private class ViewPagerAdapter extends PagerAdapter {

        private Context mContext;
        private List<String> strings = new ArrayList<>();

        public ViewPagerAdapter(Context context, List<String> strings) {
            mContext = context;
            this.strings = strings;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((View) object);
        }

        @Override
        public int getCount() {
            return 15;
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }

        @Override
        public Object instantiateItem(ViewGroup container, final int position) {
            LayoutInflater inflater = LayoutInflater.from(mContext);
            ViewGroup view = (ViewGroup) inflater.inflate(R.layout.supervisor_outlet_dialog_layout, container, false);

            TextView tvStoreName,tvStoreAddress,tvVisitStatus,tvInTime,tvOutTime,tvDuration,tvOrderValue;

            tvStoreName = view.findViewById(R.id.tv_store_name);
            tvStoreAddress = view.findViewById(R.id.tv_address);
            tvVisitStatus = view.findViewById(R.id.tv_status_text);
            tvInTime = view.findViewById(R.id.tv_in_time_val);
            tvOutTime = view.findViewById(R.id.tv_out_time_val);
            tvDuration = view.findViewById(R.id.tv_duration_val);
            tvOrderValue = view.findViewById(R.id.tv_order_value);

            ((TextView)view.findViewById(R.id.tv_intime_txt)).setTypeface(FontUtils.getFontRoboto(FontUtils.FontType.REGULAR,mContext));
            ((TextView)view.findViewById(R.id.tv_outtime_txt)).setTypeface(FontUtils.getFontRoboto(FontUtils.FontType.REGULAR,mContext));
            ((TextView)view.findViewById(R.id.tv_duration_txt)).setTypeface(FontUtils.getFontRoboto(FontUtils.FontType.REGULAR,mContext));
            ((TextView)view.findViewById(R.id.tv_order_val_txt)).setTypeface(FontUtils.getFontRoboto(FontUtils.FontType.REGULAR,mContext));

            tvStoreName.setTypeface(FontUtils.getFontRoboto(FontUtils.FontType.MEDIUM,mContext));
            tvStoreAddress.setTypeface(FontUtils.getFontRoboto(FontUtils.FontType.REGULAR,mContext));
            tvVisitStatus.setTypeface(FontUtils.getFontRoboto(FontUtils.FontType.REGULAR,mContext));
            tvInTime.setTypeface(FontUtils.getFontRoboto(FontUtils.FontType.REGULAR,mContext));
            tvOutTime.setTypeface(FontUtils.getFontRoboto(FontUtils.FontType.REGULAR,mContext));
            tvDuration.setTypeface(FontUtils.getFontRoboto(FontUtils.FontType.REGULAR,mContext));
            tvOrderValue.setTypeface(FontUtils.getFontRoboto(FontUtils.FontType.MEDIUM,mContext));


            container.addView(view);
            return view;
        }
    }

}
