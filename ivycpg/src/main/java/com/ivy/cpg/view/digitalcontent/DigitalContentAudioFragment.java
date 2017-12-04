package com.ivy.cpg.view.digitalcontent;


import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
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
import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.bo.DigitalContentBO;
import com.ivy.sd.png.commons.IvyBaseFragment;
import com.ivy.sd.png.commons.SDUtil;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.util.DataMembers;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

public class DigitalContentAudioFragment extends IvyBaseFragment {


    BusinessModel mBModel;
    private DigitalContentHelper mDigitalContentHelper;

    private RecyclerView recyclerview;
    public GridLayoutManager mGridLayoutManager;
    private RecyclerViewAdapter mRecyclerAdapter;

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
            ArrayList<DigitalContentBO> audioList = new ArrayList<>();

            //Loading only audio types
            for (DigitalContentBO bo : mDigitalContentList) {
                if (bo.getImgFlag() == 2)
                    audioList.add(bo);
            }
            if (audioList.size() > 0) {
                Collections.sort(audioList, DigitalContentBO.dateCompartor);

                String today = SDUtil.now(SDUtil.DATE_GLOBAL);
                String mCurrentDay = today.split("/")[2];
                String current_month_year = today.split(mCurrentDay)[0];
                String current_month = today.split("/")[1];
                String mCurrentYear = today.split("/")[0];
                String previous_month_year = mCurrentYear + "/" + (Integer.parseInt(current_month) - 1) + "/";

                month_wise_group.put("THIS MONTH", new ArrayList<DigitalContentBO>());
                month_wise_group.put("PREVIOUS MONTH", new ArrayList<DigitalContentBO>());
                month_wise_group.put("OLDER", new ArrayList<DigitalContentBO>());
                ArrayList<DigitalContentBO> temp;
                for (int i = 0; i < audioList.size(); i++) {
                    if (audioList.get(i).getImageDate().startsWith(current_month_year)) {
                        temp = (month_wise_group.get("THIS MONTH"));
                        if (temp.size() < 1) {
                            DigitalContentBO digital = new DigitalContentBO();
                            digital.setHeader(true);
                            digital.setHeaderTitle("THIS MONTH");
                            temp.add(digital);
                        }

                        temp.add(audioList.get(i));
                        month_wise_group.put("THIS MONTH", temp);
                    } else if (audioList.get(i).getImageDate().startsWith(previous_month_year)) {
                        temp = (month_wise_group.get("PREVIOUS MONTH"));
                        if (temp.size() < 1) {
                            DigitalContentBO digital = new DigitalContentBO();
                            digital.setHeader(true);
                            digital.setHeaderTitle("PREVIOUS MONTH");
                            temp.add(digital);
                        }
                        temp.add(audioList.get(i));
                        month_wise_group.put("PREVIOUS MONTH", temp);
                    } else {
                        temp = (month_wise_group.get("OLDER"));
                        if (temp.size() < 1) {
                            DigitalContentBO digital = new DigitalContentBO();
                            digital.setHeader(true);
                            digital.setHeaderTitle("OLDER");
                            temp.add(digital);
                        }
                        temp.add(audioList.get(i));
                        month_wise_group.put("OLDER", temp);
                    }
                }
                audioList.clear();
                if (month_wise_group.get("THIS MONTH") != null && month_wise_group.get("THIS MONTH").size() != 0) {
                    audioList.addAll(month_wise_group.get("THIS MONTH"));
                }
                if (month_wise_group.get("PREVIOUS MONTH") != null && month_wise_group.get("PREVIOUS MONTH").size() != 0) {
                    audioList.addAll(month_wise_group.get("PREVIOUS MONTH"));
                }
                if (month_wise_group.get("OLDER") != null && month_wise_group.get("OLDER").size() != 0) {
                    audioList.addAll(month_wise_group.get("OLDER"));
                }
                mRecyclerAdapter = new RecyclerViewAdapter(audioList);
                recyclerview.setAdapter(mRecyclerAdapter);
            } else {
                ArrayList<DigitalContentBO> mAudioList = new ArrayList<>();
                mRecyclerAdapter = new RecyclerViewAdapter(mAudioList);
                recyclerview.setAdapter(mRecyclerAdapter);
            }
        } else {
            ArrayList<DigitalContentBO> mPDFList = new ArrayList<>();
            mRecyclerAdapter = new RecyclerViewAdapter(mPDFList);
            recyclerview.setAdapter(mRecyclerAdapter);
        }

    }

    /**
     * Loading digital content to the view
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
        public void onBindViewHolder(final RecyclerView.ViewHolder holder, int position) {
            DigitalContentBO product = items.get(position);
            if (holder instanceof VHItem) {
                if (product.getDescription() != null) {
                    String str = product.getDescription().equals("null") ? product
                            .getFileName() : product.getDescription();
                    ((VHItem) holder).mProductDescription.setText(str);
                } else {
                    ((VHItem) holder).mProductDescription.setText(product.getFileName());
                }
                ((VHItem) holder).date.setText(product.getImageDate());
                ((VHItem) holder).filename = product.getFileName();

                if (product.getProductName() != null && !(product.getProductName().equals(""))) {
                    ((VHItem) holder).mProductName.setText(product.getProductName());
                    ((VHItem) holder).mProductName.setVisibility(View.VISIBLE);
                } else {
                    ((VHItem) holder).mProductName.setVisibility(View.GONE);
                }

                Glide
                        .with(getContext())
                        .load(Uri.fromFile(new File(
                                getActivity().getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS) + "/"
                                        + mBModel.userMasterHelper.getUserMasterBO().getUserid()
                                        + DataMembers.DIGITAL_CONTENT + "/"
                                        + DataMembers.DIGITALCONTENT + "/" + items.get(position).getFileName())))
                        .error(ContextCompat.getDrawable(getActivity().getApplicationContext(), R.drawable.ic_digital_video))
                        .into(((VHItem) holder).image);
                ((VHItem) holder).image.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        openAudio(((VHItem) holder).filename);
                    }
                });
            } else if (holder instanceof VHHeader) {
                ((VHHeader) holder).month_label.setText(items.get(position).getHeaderTitle());
            }


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
        public int getItemCount() {
            return items.size();
        }

        public class VHItem extends RecyclerView.ViewHolder {
            TextView mProductDescription, date, mProductName, month_label;
            ImageView image, play_icon;
            String filename;

            public VHItem(View v) {
                super(v);
                mProductDescription = (TextView) v
                        .findViewById(R.id.closePRODNAME);
                image = (ImageView) v.findViewById(R.id.icon);
                date = (TextView) v.findViewById(R.id.date);
                mProductName = (TextView) v.findViewById(R.id.prodName);
                month_label = (TextView) v.findViewById(R.id.month_label);
                play_icon = (ImageView) v.findViewById(R.id.play_icon);
                play_icon.setVisibility(View.VISIBLE);

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
     * Method to show Video File
     *
     * @param name Audio file name
     */
    private void openAudio(String name) {
        File file = new File(
                getActivity().getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS) + "/"
                        + mBModel.userMasterHelper.getUserMasterBO().getUserid()
                        + DataMembers.DIGITAL_CONTENT + "/"
                        + DataMembers.DIGITALCONTENT + "/" + name);
        if (file.exists()) {
            Uri path = Uri.fromFile(file);
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(path, "audio/*");
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            try {
                startActivity(intent);
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
