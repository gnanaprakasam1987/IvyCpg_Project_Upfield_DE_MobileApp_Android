package com.ivy.cpg.view.digitalcontent;


import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.ivy.sd.png.asean.view.BuildConfig;
import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.bo.DigitalContentBO;
import com.ivy.sd.png.commons.IvyBaseFragment;
import com.ivy.sd.png.commons.SDUtil;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.util.Commons;
import com.ivy.sd.png.util.DataMembers;
import com.ivy.utils.DateTimeUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

public class DigitalContentOthersFragment extends IvyBaseFragment {

    BusinessModel mBModel;
    private DigitalContentHelper mDigitalContentHelper;

    private RecyclerView recyclerview;
    public GridLayoutManager mGridLayoutManager;
    RecyclerViewAdapter mRecyclerAdapter;

    private int mScreenWidth = 0;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_digitalcontent_pager,
                container, false);
        DisplayMetrics displaymetrics = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
        mScreenWidth = displaymetrics.widthPixels;

        mBModel = (BusinessModel) getActivity().getApplicationContext();
        mBModel.setContext(getActivity());
        mDigitalContentHelper = DigitalContentHelper.getInstance(getActivity());

        if (getActivity().getActionBar() != null) {
            getActivity().getActionBar().setDisplayShowTitleEnabled(false);
        }
        setScreenTitle(mDigitalContentHelper.mSelectedActivityName);

        recyclerview = (RecyclerView) view.findViewById(R.id.recyclerview);

        //set GridLayoutManager in recycler view
        if (mScreenWidth > 400)
            mGridLayoutManager = new GridLayoutManager(getActivity(), 3);
        else
            mGridLayoutManager = new GridLayoutManager(getActivity(), 2);

        mGridLayoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                if (mScreenWidth >= 400) {
                    return mRecyclerAdapter.isPositionHeader(position) ? 3 : 1;
                }
                return 1;
            }
        });
        recyclerview.setLayoutManager(mGridLayoutManager);

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        ArrayList<DigitalContentBO> mDigitalContentList = mDigitalContentHelper.getFilteredDigitalMaster();
        HashMap<String, ArrayList<DigitalContentBO>> month_wise_group = new HashMap<>();
        if (mDigitalContentList.size() > 0) {
            ArrayList<DigitalContentBO> otherList = new ArrayList<>();

            //Load other type of digital content files
            for (DigitalContentBO bo : mDigitalContentList) {
                if (bo.getImgFlag() == 6)
                    otherList.add(bo);
            }

            if (otherList.size() > 0) {
                Collections.sort(otherList, DigitalContentBO.dateCompartor);
                String today = DateTimeUtils.now(DateTimeUtils.DATE_GLOBAL);
                String mCurrentDay = today.split("/")[2];
                String current_month_year = today.split(mCurrentDay)[0];
                String current_month = today.split("/")[1];
                String mCurrentYear = today.split("/")[0];
                String previous_month_year = mCurrentYear + "/" + (SDUtil.convertToInt(current_month) - 1) + "/";

                month_wise_group.put("THIS MONTH", new ArrayList<DigitalContentBO>());
                month_wise_group.put("PREVIOUS MONTH", new ArrayList<DigitalContentBO>());
                month_wise_group.put("OLDER", new ArrayList<DigitalContentBO>());
                ArrayList<DigitalContentBO> temp;
                for (int i = 0; i < otherList.size(); i++) {
                    if (otherList.get(i).getImageDate().startsWith(current_month_year)) {
                        temp = (month_wise_group.get("THIS MONTH"));
                        if (temp.size() < 1) {
                            DigitalContentBO digital = new DigitalContentBO();
                            digital.setHeader(true);
                            digital.setHeaderTitle(getResources().getString(R.string.digital_content_header_this_month));
                            temp.add(digital);
                        }

                        temp.add(otherList.get(i));
                        month_wise_group.put("THIS MONTH", temp);
                    } else if (otherList.get(i).getImageDate().startsWith(previous_month_year)) {
                        temp = (month_wise_group.get("PREVIOUS MONTH"));
                        if (temp.size() < 1) {
                            DigitalContentBO digital = new DigitalContentBO();
                            digital.setHeader(true);
                            digital.setHeaderTitle(getResources().getString(R.string.digital_content_header_prev_month));
                            temp.add(digital);
                        }
                        temp.add(otherList.get(i));
                        month_wise_group.put("PREVIOUS MONTH", temp);
                    } else {
                        temp = (month_wise_group.get("OLDER"));
                        if (temp.size() < 1) {
                            DigitalContentBO digital = new DigitalContentBO();
                            digital.setHeader(true);
                            digital.setHeaderTitle(getResources().getString(R.string.digital_content_header_older));
                            temp.add(digital);
                        }
                        temp.add(otherList.get(i));
                        month_wise_group.put("OLDER", temp);
                    }
                }

                otherList.clear();
                if (month_wise_group.get("THIS MONTH") != null && month_wise_group.get("THIS MONTH").size() != 0) {
                    otherList.addAll(month_wise_group.get("THIS MONTH"));
                }
                if (month_wise_group.get("PREVIOUS MONTH") != null && month_wise_group.get("PREVIOUS MONTH").size() != 0) {
                    otherList.addAll(month_wise_group.get("PREVIOUS MONTH"));
                }
                if (month_wise_group.get("OLDER") != null && month_wise_group.get("OLDER").size() != 0) {
                    otherList.addAll(month_wise_group.get("OLDER"));
                }
                mRecyclerAdapter = new RecyclerViewAdapter(otherList);
                recyclerview.setAdapter(mRecyclerAdapter);
            }
        }

    }

    /**
     * Loading digital content to the recycler view
     */
    class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        private ArrayList<DigitalContentBO> items;
        private static final int TYPE_HEADER = 0;
        private static final int TYPE_ITEM = 1;

        RecyclerViewAdapter(ArrayList<DigitalContentBO> items) {
            this.items = items;
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

            if (viewType == TYPE_ITEM) {
                //inflate your layout and pass it to view holder
                View v = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.row_digital_content, parent, false);
                return new VHItem(v);
            } else if (viewType == TYPE_HEADER) {
                //inflate your layout and pass it to view holder
                return new VHHeader(LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.row_digital_content_header, parent, false));
            }
            throw new RuntimeException("there is no type that matches the type " + viewType + " + make sure your using types correctly");
        }

        @Override
        public int getItemViewType(int position) {
            if (isPositionHeader(position))
                return TYPE_HEADER;

            return TYPE_ITEM;
        }

        private boolean isPositionHeader(int position) {
            return items.get(position).isHeader();
        }


        @Override
        public void onBindViewHolder(final RecyclerView.ViewHolder holder, int position) {
            DigitalContentBO product = items.get(position);
            if (holder instanceof VHItem) {
                if (product.getDescription() != null) {
                    String str = product.getDescription().equals("null") ? product
                            .getFileName() : product.getDescription();
                    ((VHItem) holder).mPDescription.setText(str);
                } else {
                    ((VHItem) holder).mPDescription.setText(product.getFileName());
                }
                ((VHItem) holder).date.setText(product.getImageDate());
                ((VHItem) holder).filename = product.getFileName();

                if (product.getProductName() != null && !(product.getProductName().equals(""))) {
                    ((VHItem) holder).mPName.setText(product.getProductName());
                    ((VHItem) holder).mPName.setVisibility(View.VISIBLE);
                } else {
                    ((VHItem) holder).mPName.setVisibility(View.GONE);
                }

                Uri path;
                if (Build.VERSION.SDK_INT >= 24) {
                    path = FileProvider.getUriForFile(getActivity(), BuildConfig.APPLICATION_ID + ".provider", new File(
                            getActivity().getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS) + "/"
                                    + mBModel.userMasterHelper.getUserMasterBO().getUserid()
                                    + DataMembers.DIGITAL_CONTENT + "/"
                                    + DataMembers.DIGITALCONTENT + "/" + items.get(position).getFileName()));
                } else {
                    path = Uri.fromFile(new File(
                            getActivity().getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS) + "/"
                                    + mBModel.userMasterHelper.getUserMasterBO().getUserid()
                                    + DataMembers.DIGITAL_CONTENT + "/"
                                    + DataMembers.DIGITALCONTENT + "/" + items.get(position).getFileName()));
                }

                Glide
                        .with(getContext())
                        .load(path)
                        .error(ContextCompat.getDrawable(getActivity().getApplicationContext(), R.drawable.ic_digital_ppt))
                        .into(((VHItem) holder).image);
                ((VHItem) holder).image.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        openPPT(((VHItem) holder).filename);

                    }
                });

                if(product.isAllowSharing()){
                    ((RecyclerViewAdapter.VHItem) holder).imageView_share.setVisibility(View.VISIBLE);
                }
                else {
                    ((RecyclerViewAdapter.VHItem) holder).imageView_share.setVisibility(View.GONE);
                }
                ((RecyclerViewAdapter.VHItem) holder).imageView_share.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        try {
                            mDigitalContentHelper.shareDigitalContent(getActivity(), product.getDescription(), ((RecyclerViewAdapter.VHItem) holder).filename);
                        }
                        catch (Exception ex){
                            Toast.makeText(getActivity(),getResources().getString(R.string.error_in_sending_email),Toast.LENGTH_LONG).show();
                            Commons.printException(ex);
                        }
                    }
                });

            } else if (holder instanceof VHHeader) {
                ((VHHeader) holder).month_label.setText(items.get(position).getHeaderTitle());
            }

        }


        @Override
        public int getItemCount() {
            return items.size();
        }

        public class VHItem extends RecyclerView.ViewHolder {
            TextView mPDescription, date, mPName, month_label;
            ImageView image,imageView_share;
            String filename;

            public VHItem(View v) {
                super(v);
                mPDescription = (TextView) v
                        .findViewById(R.id.closePRODNAME);
                image = (ImageView) v.findViewById(R.id.icon);
                imageView_share = (ImageView) v.findViewById(R.id.imageview_share);
                date = (TextView) v.findViewById(R.id.date);
                mPName = (TextView) v.findViewById(R.id.prodName);
                month_label = (TextView) v.findViewById(R.id.month_label);

            }
        }

        class VHHeader extends RecyclerView.ViewHolder {
            TextView month_label;

            public VHHeader(View itemView) {
                super(itemView);
                month_label = (TextView) itemView.findViewById(R.id.month_label);
            }
        }
    }

    /**
     * Method to view Power point File
     *
     * @param name PPT Name
     */
    private void openPPT(String name) {

        Uri path;
        File file = new File(
                getActivity().getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS) + "/"
                        + mBModel.userMasterHelper.getUserMasterBO().getUserid()
                        + DataMembers.DIGITAL_CONTENT + "/"
                        + DataMembers.DIGITALCONTENT + "/" + name);
        if (file.exists()) {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            if (Build.VERSION.SDK_INT >= 24) {
                path = FileProvider.getUriForFile(getActivity(), BuildConfig.APPLICATION_ID + ".provider", file);
                intent.setDataAndType(path, "application/vnd.ms-powerpoint");
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            } else {
                path = Uri.fromFile(file);
                intent.setDataAndType(path, "application/vnd.ms-powerpoint");
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            }
            try {
                startActivity(intent);
            } catch (ActivityNotFoundException e) {
                Toast.makeText(
                        getActivity(),
                        getResources().getString(
                                R.string.no_application_available_to_view_ppt),
                        Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(getActivity(),
                    getResources().getString(R.string.file_not_found),
                    Toast.LENGTH_SHORT).show();
        }
    }


}
