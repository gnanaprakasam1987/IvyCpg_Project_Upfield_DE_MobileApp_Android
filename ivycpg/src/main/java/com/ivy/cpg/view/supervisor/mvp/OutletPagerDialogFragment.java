package com.ivy.cpg.view.supervisor.mvp;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.android.gms.maps.model.Marker;
import com.ivy.cpg.view.supervisor.customviews.recyclerviewpager.RecyclerViewPager;
import com.ivy.cpg.view.supervisor.customviews.scrollingpagerindicator.ScrollingPagerIndicator;
import com.ivy.cpg.view.supervisor.mvp.models.RetailerBo;
import com.ivy.cpg.view.supervisor.mvp.outletmapview.OutletMapViewPresenter;
import com.ivy.cpg.view.supervisor.mvp.sellerdetailmap.SellerDetailMapPresenter;
import com.ivy.lib.DialogFragment;
import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.util.Commons;
import com.ivy.sd.png.util.DataMembers;
import com.ivy.utils.DateTimeUtils;
import com.ivy.utils.FontUtils;

import java.io.File;
import java.util.ArrayList;

@SuppressLint("ValidFragment")
public class OutletPagerDialogFragment extends DialogFragment {

    private TextView tvStoreCount;
    private RecyclerViewPager visitedRetailerRecycleView;
    private OutletListAdapter outletListAdapter;

    private int retailerId;
    private Marker marker;
    private SellerDetailMapPresenter sellerMapViewPresenter;

    private OutletMapViewPresenter outletMapViewPresenter;

    ArrayList<RetailerBo> visitedRetailers = new ArrayList<>();


    public OutletPagerDialogFragment(){

    }

    public OutletPagerDialogFragment(Marker marker, SellerDetailMapPresenter sellerMapViewPresenter){
        this.marker = marker;
        this.sellerMapViewPresenter = sellerMapViewPresenter;
    }

    public OutletPagerDialogFragment(int retailerId, OutletMapViewPresenter outletMapViewPresenter) {
        this.retailerId = retailerId;
        this.outletMapViewPresenter = outletMapViewPresenter;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.outlet_pager_dialog_fragment, container, false);

        /*initViews(rootView);

        visitedRetailers.addAll(sellerMapViewPresenter.getVisitedRetailers());
        outletListAdapter.notifyDataSetChanged();

        scrollToPosition(marker);

        tvStoreCount.setText("Total Stores - "+visitedRetailers.size());

        rootView.findViewById(R.id.close_img).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });*/

        return rootView;
    }

    private void scrollToPosition(Marker marker){
        int pagerPos = 0;
        int count=0;
        for(RetailerBo retailerBo : visitedRetailers){
            if(retailerBo.getRetailerId() == Integer.valueOf(marker.getSnippet())){
                pagerPos = count;
                break;
            }
            count = count+1;
        }

        visitedRetailerRecycleView.scrollToPosition(pagerPos);
    }

    private void scrollToPosition(int retailerId) {
        int pagerPos = 0;
        int count = 0;
        for (RetailerBo retailerBo : visitedRetailers) {
            if (retailerBo.getRetailerId() == retailerId) {
                pagerPos = count;
                break;
            }
            count = count + 1;
        }

        visitedRetailerRecycleView.scrollToPosition(pagerPos);
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

        tvStoreCount.setTypeface(FontUtils.getFontRoboto(getContext().getApplicationContext(), FontUtils.FontType.REGULAR));

        visitedRetailerRecycleView = rootView.findViewById(R.id.viewpager);
//        visitedRetailerRecycleView.setVisibility(View.GONE);
        LinearLayoutManager layout = new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL,
                false);
        visitedRetailerRecycleView.setLayoutManager(layout);

        outletListAdapter = new OutletListAdapter(getContext().getApplicationContext(),visitedRetailers);
        visitedRetailerRecycleView.setAdapter(outletListAdapter);

        ScrollingPagerIndicator recyclerIndicator = rootView.findViewById(R.id.scroll_indicator);
        recyclerIndicator.attachToRecyclerView(visitedRetailerRecycleView);

        visitedRetailerRecycleView.setHasFixedSize(true);
        visitedRetailerRecycleView.setLongClickable(true);
        visitedRetailerRecycleView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int scrollState) {
//                updateState(scrollState);
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int i, int i2) {
                int childCount = visitedRetailerRecycleView.getChildCount();
                int width = visitedRetailerRecycleView.getChildAt(0).getWidth();
                int padding = (visitedRetailerRecycleView.getWidth() - width) / 2;

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


        visitedRetailerRecycleView.addOnPageChangedListener(new RecyclerViewPager.OnPageChangedListener() {
            @Override
            public void OnPageChanged(int oldPosition, int newPosition) {

            }
        });

        visitedRetailerRecycleView.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
                if (visitedRetailerRecycleView.getChildCount() < 3) {
                    if (visitedRetailerRecycleView.getChildAt(1) != null) {
                        if (visitedRetailerRecycleView.getCurrentPosition() == 0) {
                            View v1 = visitedRetailerRecycleView.getChildAt(1);
                            v1.setScaleY(0.9f);
                            v1.setScaleX(0.9f);
                        } else {
                            View v1 = visitedRetailerRecycleView.getChildAt(0);
                            v1.setScaleY(0.9f);
                            v1.setScaleX(0.9f);
                        }
                    }
                } else {
                    if (visitedRetailerRecycleView.getChildAt(0) != null) {
                        View v0 = visitedRetailerRecycleView.getChildAt(0);
                        v0.setScaleY(0.9f);
                        v0.setScaleX(0.9f);
                    }
                    if (visitedRetailerRecycleView.getChildAt(2) != null) {
                        View v2 = visitedRetailerRecycleView.getChildAt(2);
                        v2.setScaleY(0.9f);
                        v2.setScaleX(0.9f);
                    }
                }
            }
        });
    }


    public class OutletListAdapter extends RecyclerView.Adapter<OutletListAdapter.MyViewHolder> {

        private Context context;
        private ArrayList<RetailerBo> visitedRetailers;

        OutletListAdapter(Context context, ArrayList<RetailerBo> visitedRetailers){
            this.context = context;
            this.visitedRetailers = visitedRetailers;
        }

        public class MyViewHolder extends RecyclerView.ViewHolder {

            private TextView tvStoreName,tvStoreAddress,tvVisitStatus,tvOrderValue,tvOrderValueText;
            private RecyclerView retailerVisitedRVP;
            private View visitedStatusView;
            private ImageView retailImage,noImage;

            public MyViewHolder(View view) {
                super(view);

                tvStoreName = view.findViewById(R.id.tv_store_name);
                tvStoreAddress = view.findViewById(R.id.tv_address);
                tvVisitStatus = view.findViewById(R.id.tv_status_text);
                tvOrderValue = view.findViewById(R.id.tv_total_order_value);
                tvOrderValueText = view.findViewById(R.id.tv_total_value_txt);
                visitedStatusView = view.findViewById(R.id.status_color_view);
                retailerVisitedRVP = view.findViewById(R.id.visited_retailer_items);
                retailImage = view.findViewById(R.id.outlet_image);
                noImage = view.findViewById(R.id.outlet_no_image);

                ((TextView)view.findViewById(R.id.tv_intime_txt)).setTypeface(FontUtils.getFontRoboto(context, FontUtils.FontType.REGULAR));
                ((TextView)view.findViewById(R.id.tv_outtime_txt)).setTypeface(FontUtils.getFontRoboto(context, FontUtils.FontType.REGULAR));
                ((TextView)view.findViewById(R.id.tv_duration_txt)).setTypeface(FontUtils.getFontRoboto(context, FontUtils.FontType.REGULAR));
                ((TextView)view.findViewById(R.id.tv_order_val_txt)).setTypeface(FontUtils.getFontRoboto(context, FontUtils.FontType.REGULAR));

                tvStoreName.setTypeface(FontUtils.getFontRoboto(context, FontUtils.FontType.MEDIUM));
                tvStoreAddress.setTypeface(FontUtils.getFontRoboto(context, FontUtils.FontType.REGULAR));
                tvVisitStatus.setTypeface(FontUtils.getFontRoboto(context, FontUtils.FontType.REGULAR));
                tvOrderValue.setTypeface(FontUtils.getFontRoboto(context, FontUtils.FontType.MEDIUM));
                tvOrderValueText.setTypeface(FontUtils.getFontRoboto(context, FontUtils.FontType.REGULAR));

                LinearLayoutManager layout = new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL,
                        false);
                retailerVisitedRVP.setLayoutManager(layout);
            }
        }

        @Override
        public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.supervisor_outlet_dialog_layout, parent, false);

            return new MyViewHolder(itemView);
        }

        @SuppressLint("SetTextI18n")
        @Override
        public void onBindViewHolder(MyViewHolder holder, final int position) {

            holder.tvStoreName.setText(visitedRetailers.get(position).getRetailerName());
            holder.tvOrderValue.setText(String.valueOf(visitedRetailers.get(position).getTotalOrderValue()));
            holder.tvStoreAddress.setText(visitedRetailers.get(position).getAddress());

            ArrayList<RetailerBo> retailerVisitedDetail = sellerMapViewPresenter.getRetailerVisitDetailsByRId(visitedRetailers.get(position).getRetailerId());

            int outletDefaultDrawable = R.drawable.unbilled_bg_gradient;

            if(retailerVisitedDetail != null && retailerVisitedDetail.size() > 0) {
                VisitedOutletInfoAdapter visitedOutletInfoAdapter =
                        new VisitedOutletInfoAdapter(context, retailerVisitedDetail);
                holder.retailerVisitedRVP.setAdapter(visitedOutletInfoAdapter);


                if (!visitedRetailers.get(position).getIsOrdered()){
                    holder.tvVisitStatus.setText("UnBilled");
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
                        holder.visitedStatusView.setBackground(ContextCompat.getDrawable(context, R.drawable.unbilled_bg_gradient));
                    else
                        holder.visitedStatusView.setBackgroundDrawable(ContextCompat.getDrawable(context, R.drawable.unbilled_bg_gradient));
                }else {
                    holder.tvVisitStatus.setText("Covered");

                    outletDefaultDrawable = R.drawable.covered_outlet_bg_gradient;

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
                        holder.visitedStatusView.setBackground(ContextCompat.getDrawable(context, R.drawable.covered_green));
                    else
                        holder.visitedStatusView.setBackgroundDrawable(ContextCompat.getDrawable(context, R.drawable.covered_green));
                }
            }

            setProfileImage(holder.retailImage,
                    visitedRetailers.get(position).getImgPath(),
                    visitedRetailers.get(position).getRetailerId(),outletDefaultDrawable,holder.noImage);
        }

        @Override
        public int getItemViewType(int position) {
            return position;
        }

        @Override
        public int getItemCount() {
            return visitedRetailers.size();
        }
    }

    public class VisitedOutletInfoAdapter extends RecyclerView.Adapter<VisitedOutletInfoAdapter.MyViewHolder> {

        private Context context;
        private ArrayList<RetailerBo> visitedRetailers;

        VisitedOutletInfoAdapter(Context context, ArrayList<RetailerBo> visitedRetailers){
            this.context = context;
            this.visitedRetailers = visitedRetailers;
        }

        public class MyViewHolder extends RecyclerView.ViewHolder {

            TextView tvInTime,tvOutTime,tvDuration,tvOrderValue;

            public MyViewHolder(View view) {
                super(view);

                tvInTime = view.findViewById(R.id.tv_in_time_val);
                tvOutTime = view.findViewById(R.id.tv_out_time_val);
                tvDuration = view.findViewById(R.id.tv_duration_val);
                tvOrderValue = view.findViewById(R.id.tv_order_value);

                tvInTime.setTypeface(FontUtils.getFontRoboto(context, FontUtils.FontType.REGULAR));
                tvOutTime.setTypeface(FontUtils.getFontRoboto(context, FontUtils.FontType.REGULAR));
                tvDuration.setTypeface(FontUtils.getFontRoboto(context, FontUtils.FontType.REGULAR));
                tvOrderValue.setTypeface(FontUtils.getFontRoboto(context, FontUtils.FontType.MEDIUM));

            }
        }

        @Override
        public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.dialog_visited_outlet_item, parent, false);

            return new MyViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(MyViewHolder holder, final int position) {

            holder.tvInTime.setText(DateTimeUtils.getTimeFromMillis(visitedRetailers.get(position).getInTime()));
            holder.tvOutTime.setText(DateTimeUtils.getTimeFromMillis(visitedRetailers.get(position).getOutTime()));
            holder.tvOrderValue.setText(String.valueOf(visitedRetailers.get(position).getOrderValue()));
            holder.tvDuration.setText(
                    sellerMapViewPresenter.calculateDuration(visitedRetailers.get(position).getInTime(),
                            visitedRetailers.get(position).getOutTime()));

        }

        @Override
        public int getItemViewType(int position) {
            return position;
        }

        @Override
        public int getItemCount() {
            return visitedRetailers.size();
        }
    }

    private void setProfileImage(ImageView userView, String imagePath, int userId,int outletDefaultDrawable,ImageView noImage) {
        try {
            if (imagePath != null && !"".equals(imagePath)) {
                String[] imgPaths = imagePath.split("/");
                String path = imgPaths[imgPaths.length - 1];
                File imgFile = new File(getContext().getExternalFilesDir(
                        Environment.DIRECTORY_DOWNLOADS)
                        + "/"
                        + userId
                        + DataMembers.DIGITAL_CONTENT
                        + "/"
                        + DataMembers.USER + "/"
                        + path);
                if (imgFile.exists()) {
                    try {
                        userView.setScaleType(ImageView.ScaleType.FIT_XY);
                        userView.setAdjustViewBounds(true);

                        Glide.with(getContext())
                                .load(imgFile)
                                .centerCrop()
                                .placeholder(outletDefaultDrawable)
                                .error(outletDefaultDrawable)
                                .into(userView);

                        noImage.setVisibility(View.GONE);

                    } catch (Exception e) {
                        Commons.printException("" + e);
                    }
                } else {
                    userView
                            .setImageResource(outletDefaultDrawable);
                }
            }else {
                userView
                        .setImageResource(outletDefaultDrawable);
            }
        }catch(Exception e){
            Commons.printException(e);
        }
    }

}
