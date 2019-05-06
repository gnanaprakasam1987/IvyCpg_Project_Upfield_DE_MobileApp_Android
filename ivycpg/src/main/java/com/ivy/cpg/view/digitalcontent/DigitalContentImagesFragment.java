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
import com.bumptech.glide.load.engine.DiskCacheStrategy;
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

public class DigitalContentImagesFragment extends IvyBaseFragment {

    private BusinessModel mBModel;
    private DigitalContentHelper mDigitalContentHelper;

    private RecyclerView recyclerview;
    public GridLayoutManager mGridLayoutManager;
    private RecyclerViewAdapter mRecyclerAdapter;
    private int mScreenWidth = 0;
    private static final String THIS_MONTH = "This Month";
    private static final String PREVIOUS_MONTH = "Previous Month";
    private static final String OLDER = "Older";

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
                    if (mRecyclerAdapter.getItemViewType(position) == RecyclerViewAdapter.TYPE_GROUP)
                        return 3;
                    else if (mRecyclerAdapter.getItemViewType(position) == RecyclerViewAdapter.TYPE_HEADER)
                        return 3;
                    else
                        return 1;
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
        ArrayList<DigitalContentBO> mDigitalContentList;
        ArrayList<DigitalContentBO> mImageList = new ArrayList<>();
        HashMap<String, ArrayList<DigitalContentBO>> month_wise_group = new HashMap<>();
        HashMap<String, ArrayList<DigitalContentBO>> group_wise_group = new HashMap<>();
        mDigitalContentList = mDigitalContentHelper.getFilteredDigitalMaster();

        if (mDigitalContentList != null && !mDigitalContentList.isEmpty()) {

            // load only images to the list
            for (DigitalContentBO bo : mDigitalContentList) {
                if (bo.getImgFlag() == 1)
                    mImageList.add(bo);
            }

            if (mImageList.size() > 0) {
                Collections.sort(mImageList, DigitalContentBO.sequenceComparotr);

                String today = DateTimeUtils.now(DateTimeUtils.DATE_GLOBAL);
                String mCurrentDay = today.split("/")[2];
                String current_month = today.split("/")[1];
                String mCurrentYear = today.split("/")[0];
                String current_month_year = mCurrentYear + "/" + current_month + "/";
                String previous_month_year = mCurrentYear + "/" + (SDUtil.convertToInt(current_month) - 1) + "/";


                ArrayList<String> mGroupList = new ArrayList<>();
                for (int i = 0; i < mImageList.size(); i++) {
                    String groupName = mImageList.get(i).getGroupName();
                    if (!month_wise_group.containsKey(groupName) && !groupName.equalsIgnoreCase("")) {
                        month_wise_group.put(groupName, new ArrayList<DigitalContentBO>());
                        mGroupList.add(groupName);
                    }
                }
                Collections.sort(mImageList, DigitalContentBO.dateCompartor);

                ArrayList<DigitalContentBO> temp;
                ArrayList<DigitalContentBO> tempGp;
                for (int i = 0; i < mGroupList.size(); i++) {
                    month_wise_group.put(THIS_MONTH, new ArrayList<DigitalContentBO>());
                    month_wise_group.put(PREVIOUS_MONTH, new ArrayList<DigitalContentBO>());
                    month_wise_group.put(OLDER, new ArrayList<DigitalContentBO>());
                    for (int j = 0; j < mImageList.size(); j++) {
                        if (mImageList.get(j).getGroupName().equalsIgnoreCase(mGroupList.get(i))) {
                            tempGp = (month_wise_group.get(mGroupList.get(i)));
                            if (tempGp.size() == 0) {
                                if (!mGroupList.get(i).equalsIgnoreCase("NA") && mGroupList.size() > 1) {
                                    DigitalContentBO digital = new DigitalContentBO();
                                    digital.setGroupName(mGroupList.get(i));
                                    digital.setGroupHeader(true);
                                    tempGp.add(digital);
                                }
                            }
                            if (mImageList.get(j).getImageDate().startsWith(current_month_year)) {
                                temp = (month_wise_group.get(THIS_MONTH));
                                if (temp.size() == 0) {
                                    DigitalContentBO digital = new DigitalContentBO();
                                    digital.setHeader(true);
                                    digital.setHeaderTitle(getResources().getString(R.string.digital_content_header_this_month));
                                    temp.add(digital);
                                    tempGp.add(digital);
                                }
                                temp.add(mImageList.get(j));
                                tempGp.add(mImageList.get(j));
                                group_wise_group.put(mGroupList.get(i), tempGp);
                            } else if (mImageList.get(j).getImageDate().startsWith(previous_month_year)) {
                                temp = (month_wise_group.get(PREVIOUS_MONTH));
                                if (temp.size() == 0) {
                                    DigitalContentBO digital = new DigitalContentBO();
                                    digital.setHeader(true);
                                    digital.setHeaderTitle(getResources().getString(R.string.digital_content_header_prev_month));
                                    temp.add(digital);
                                    tempGp.add(digital);
                                }
                                temp.add(mImageList.get(j));
                                tempGp.add(mImageList.get(j));
                                group_wise_group.put(mGroupList.get(i), tempGp);
                            } else {
                                temp = (month_wise_group.get(OLDER));
                                if (temp.size() == 0) {
                                    DigitalContentBO digital = new DigitalContentBO();
                                    digital.setHeader(true);
                                    digital.setHeaderTitle(getResources().getString(R.string.digital_content_header_older));
                                    temp.add(digital);
                                    tempGp.add(digital);
                                }
                                temp.add(mImageList.get(j));
                                tempGp.add(mImageList.get(j));
                                group_wise_group.put(mGroupList.get(i), tempGp);
                            }
                        }
                    }
                }
                mImageList.clear();

                for (int i = 0; i < mGroupList.size(); i++) {
                    if (group_wise_group.get(mGroupList.get(i)) != null && group_wise_group.get(mGroupList.get(i)).size() != 0) {
                        mImageList.addAll(group_wise_group.get(mGroupList.get(i)));
                    }
                }
                mRecyclerAdapter = new RecyclerViewAdapter(mImageList);
                recyclerview.setAdapter(mRecyclerAdapter);


            } else {
                ArrayList<DigitalContentBO> mList = new ArrayList<>();
                mRecyclerAdapter = new RecyclerViewAdapter(mList);
                recyclerview.setAdapter(mRecyclerAdapter);
            }
        } else {
            ArrayList<DigitalContentBO> mList = new ArrayList<>();
            mRecyclerAdapter = new RecyclerViewAdapter(mList);
            recyclerview.setAdapter(mRecyclerAdapter);
        }

    }

    /**
     * Load Images
     */
    class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {


        ArrayList<DigitalContentBO> items;
        private static final int TYPE_HEADER = 0;
        private static final int TYPE_ITEM = 1;
        private static final int TYPE_GROUP = 2;

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
            } else if (viewType == TYPE_GROUP) {
                //inflate your layout and pass it to view holder
                return new VHGroupHeader(LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.row_digital_content_group_header, parent, false));
            }
            throw new RuntimeException("there is no type that matches the type " + viewType + " + make sure your using types correctly");

        }

        @Override
        public void onBindViewHolder(final RecyclerView.ViewHolder holder, int position) {
            DigitalContentBO product = items.get(position);
            if (holder instanceof VHItem) {
                //cast holder to VHItem and set data
                if (product.getDescription() != null) {
                    String str = product.getDescription().equals("null") ? product
                            .getFileName() : product.getDescription();
                    ((VHItem) holder).mProductNameDescription.setText(str);
                } else {
                    ((VHItem) holder).mProductNameDescription.setText(product.getFileName());
                }
                ((VHItem) holder).date.setText(product.getImageDate());
                ((VHItem) holder).filename = product.getFileName();

                if (product.getProductName() != null && !(product.getProductName().equals(""))) {
                    ((VHItem) holder).mProductName.setText(product.getProductName());
                    ((VHItem) holder).mProductName.setVisibility(View.VISIBLE);
                } else {
                    ((VHItem) holder).mProductName.setVisibility(View.GONE);
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
                Glide.with(getActivity())
                        .load(path)
                        .error(ContextCompat.getDrawable(getActivity().getApplicationContext(), R.drawable.no_image_available))
                        .dontAnimate()
                        .diskCacheStrategy(DiskCacheStrategy.RESULT)
                        .into(((VHItem) holder).image);

                ((VHItem) holder).image.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        openImages(((VHItem) holder).filename,product);
                    }
                });

                if(product.isAllowSharing()){
                    ((VHItem) holder).imageView_share.setVisibility(View.VISIBLE);
                }
                else {
                    ((VHItem) holder).imageView_share.setVisibility(View.GONE);
                }
                ((VHItem) holder).imageView_share.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mDigitalContentHelper.shareDigitalContent(getActivity(),product.getDescription(),((VHItem) holder).filename);
                    }
                });


            } else if (holder instanceof VHHeader) {
                ((VHHeader) holder).month_label.setText(items.get(position).getHeaderTitle());
            } else if (holder instanceof VHGroupHeader) {
                ((VHGroupHeader) holder).TVGroupLabel.setText(items.get(position).getGroupName());
            }


        }

        @Override
        public int getItemViewType(int position) {
            if (isPositionHeader(position))
                return TYPE_HEADER;
            else if (isPosGroupHeader(position))
                return TYPE_GROUP;
            else
                return TYPE_ITEM;
        }

        private boolean isPositionHeader(int position) {
            return items.get(position).isHeader();
        }

        private boolean isPosGroupHeader(int position) {
            return items.get(position).isGroupHeader();
        }


        @Override
        public int getItemCount() {
            return items.size();
        }

        class VHItem extends RecyclerView.ViewHolder {
            TextView mProductNameDescription, date, mProductName, month_label;
            ImageView image,imageView_share;
            String filename;

            public VHItem(View v) {
                super(v);
                mProductNameDescription = (TextView) v
                        .findViewById(R.id.closePRODNAME);
                image = (ImageView) v.findViewById(R.id.icon);
                imageView_share = (ImageView) v.findViewById(R.id.imageview_share);
                date = (TextView) v.findViewById(R.id.date);
                mProductName = (TextView) v.findViewById(R.id.prodName);
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

        class VHGroupHeader extends RecyclerView.ViewHolder {
            TextView TVGroupLabel;

            public VHGroupHeader(View view) {
                super(view);
                TVGroupLabel = (TextView) view.findViewById(R.id.group_label);
            }
        }
    }

    /**
     * Method to view Image File
     *
     * @param name Image name
     */
    private void openImages(String name,DigitalContentBO digitalContentBO) {
        Uri path;
        File file = new File(
                getActivity().getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS) + "/"
                        + mBModel.userMasterHelper.getUserMasterBO().getUserid()
                        + DataMembers.DIGITAL_CONTENT + "/"
                        + DataMembers.DIGITALCONTENT + "/" + name);
        Commons.print("image" + file.getAbsolutePath());
        if (file.exists()) {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            if (Build.VERSION.SDK_INT >= 24) {
                path = FileProvider.getUriForFile(getActivity(), BuildConfig.APPLICATION_ID + ".provider", file);
                intent.setDataAndType(path, "image/*");
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            } else {
                path = Uri.fromFile(file);
                intent.setDataAndType(path, "image/*");
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            }

            try {
                startActivity(intent);

                String vStart_Time = "" + DateTimeUtils.now(DateTimeUtils.DATE) + " " + DateTimeUtils.now(DateTimeUtils.TIME);

                DigitalContentHelper.getInstance(getContext()).
                        saveDigitalContentDetails(getContext(),String.valueOf(digitalContentBO.getImageID()),
                                String.valueOf(digitalContentBO.getProductID()),
                                vStart_Time,"",false);

            } catch (ActivityNotFoundException e) {
                Toast.makeText(
                        getActivity(),
                        getResources()
                                .getString(
                                        R.string.no_application_available_to_view_video),
                        Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(getActivity(),
                    getResources().getString(R.string.file_not_found),
                    Toast.LENGTH_SHORT).show();
        }
    }
}
