package com.ivy.sd.png.view;


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

public class DigitalContentVideoFragement extends IvyBaseFragment {


    BusinessModel bmodel;
    private RecyclerView recyclerview;
    public GridLayoutManager gridlaymanager;
    private ArrayList<DigitalContentBO> mylist;
    RecyclerViewAdapter recycleradapter;
    private int screenwidth = 0, screenheight = 0;


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
        screenwidth = displaymetrics.widthPixels;
        screenheight = displaymetrics.heightPixels;


        bmodel = (BusinessModel) getActivity().getApplicationContext();
        bmodel.setContext(getActivity());

        if (getActivity().getActionBar() != null) {
            getActivity().getActionBar().setDisplayShowTitleEnabled(false);
        }
        setScreenTitle(bmodel.mSelectedActivityName);

        recyclerview = (RecyclerView) view.findViewById(R.id.recyclerview);

        //set GridLayoutManager in recyclerview
        if (screenwidth > 400)
            gridlaymanager = new GridLayoutManager(getActivity(), 3);
        else
            gridlaymanager = new GridLayoutManager(getActivity(), 2);

        gridlaymanager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                if (screenwidth >= 400) {
                    return recycleradapter.isPositionHeader(position) ? 3 : 1;
                }
                return 1;
            }
        });
        recyclerview.setLayoutManager(gridlaymanager);

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        mylist = bmodel.planogramMasterHelper.getFilteredDigitalMaster();
        HashMap<String, ArrayList<DigitalContentBO>> month_wise_group = new HashMap<>();
        if (mylist.size() > 0) {
            ArrayList<DigitalContentBO> videoList = new ArrayList<>();
            for (DigitalContentBO bo : mylist) {
                if (bo.getImgFlag() == 3)
                    videoList.add(bo);
            }
            if (videoList.size() > 0) {
                Collections.sort(videoList, DigitalContentBO.dateCompartor);
                String today = SDUtil.now(SDUtil.DATE_GLOBAL);
                String currentday = today.split("/")[2];
                String current_month_year = today.split(currentday)[0];
                String current_month = today.split("/")[1];
                String currentyear = today.split("/")[0];
                String previous_month_year = currentyear + "/" + (Integer.parseInt(current_month) - 1) + "/";

                month_wise_group.put("THIS MONTH", new ArrayList<DigitalContentBO>());
                month_wise_group.put("LAST MONTH", new ArrayList<DigitalContentBO>());
                month_wise_group.put("OLDER", new ArrayList<DigitalContentBO>());
                ArrayList<DigitalContentBO> temp;
                for (int i = 0; i < videoList.size(); i++) {
                    if (videoList.get(i).getImageDate().startsWith(current_month_year)) {
                        temp = (month_wise_group.get("THIS MONTH"));
                        if (temp.size() < 1) {
                            DigitalContentBO digital = new DigitalContentBO();
                            digital.setHeader(true);
                            digital.setHeaderTitle("THIS MONTH");
                            temp.add(digital);
                        }

                        temp.add(videoList.get(i));
                        month_wise_group.put("THIS MONTH", temp);
                    } else if (videoList.get(i).getImageDate().startsWith(previous_month_year)) {
                        temp = (month_wise_group.get("PREVIOUS MONTH"));
                        if (temp.size() < 1) {
                            DigitalContentBO digital = new DigitalContentBO();
                            digital.setHeader(true);
                            digital.setHeaderTitle("PREVIOUS MONTH");
                            temp.add(digital);
                        }
                        temp.add(videoList.get(i));
                        month_wise_group.put("PREVIOUS MONTH", temp);
                    } else {
                        temp = (month_wise_group.get("OLDER"));
                        if (temp.size() < 1) {
                            DigitalContentBO digital = new DigitalContentBO();
                            digital.setHeader(true);
                            digital.setHeaderTitle("OLDER");
                            temp.add(digital);
                        }
                        temp.add(videoList.get(i));
                        month_wise_group.put("OLDER", temp);
                    }
                }

                videoList.clear();
                if (month_wise_group.get("THIS MONTH") != null && month_wise_group.get("THIS MONTH").size() != 0) {
                    videoList.addAll(month_wise_group.get("THIS MONTH"));
                }
                if (month_wise_group.get("PREVIOUS MONTH") != null && month_wise_group.get("PREVIOUS MONTH").size() != 0) {
                    videoList.addAll(month_wise_group.get("PREVIOUS MONTH"));
                }
                if (month_wise_group.get("OLDER") != null && month_wise_group.get("OLDER").size() != 0) {
                    videoList.addAll(month_wise_group.get("OLDER"));
                }
                recycleradapter = new RecyclerViewAdapter(videoList);
                recyclerview.setAdapter(recycleradapter);
            }
        }

    }

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
                        .inflate(R.layout.activity_digital_content_display_recyclerview_row, parent, false);
                return new VHItem(v);
            } else if (viewType == TYPE_HEADER) {
                //inflate your layout and pass it to view holder
                return new VHHeader(LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.activity_digital_content_header, parent, false));
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
            if (items.get(position).isHeader()) {
                return true;
            } else {
                return false;
            }
        }

        @Override
        public void onBindViewHolder(final RecyclerView.ViewHolder holder, int position) {
            DigitalContentBO product = items.get(position);
            if (holder instanceof VHItem) {
                if (product.getDescription() != null) {
                    String str = product.getDescription().equals("null") ? product
                            .getFileName() : product.getDescription();
                    ((VHItem) holder).pname.setText(str);
                } else {
                    ((VHItem) holder).pname.setText(product.getFileName());
                }
                ((VHItem) holder).date.setText(product.getImageDate());
                ((VHItem) holder).filename = product.getFileName();

                if (product.getProductName() != null && !(product.getProductName().equals(""))) {
                    ((VHItem) holder).prodname.setText(product.getProductName());
                    ((VHItem) holder).prodname.setVisibility(View.VISIBLE);
                } else {
                    ((VHItem) holder).prodname.setVisibility(View.GONE);
                }

                Glide
                        .with(getContext())
                        .load(Uri.fromFile(new File(
                                getActivity().getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS) + "/"
                                        + bmodel.userMasterHelper.getUserMasterBO().getUserid()
                                        + DataMembers.DIGITAL_CONTENT + "/"
                                        + DataMembers.DIGITALCONTENT + "/" + items.get(position).getFileName())))
                        .error(ContextCompat.getDrawable(getActivity().getApplicationContext(), R.drawable.ic_digital_video))
                        .into(((VHItem) holder).image);

               /* ((VHItem)holder).image.setImageDrawable(ContextCompat.getDrawable(getActivity(),
                        R.drawable.ic_digital_video));
                ((VHItem)holder).image.getLayoutParams().height = 160;*/

                ((VHItem) holder).image.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {

                        openMov(((VHItem) holder).filename);

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
            TextView pname, date, prodname, month_label;
            ImageView image, play_icon;
            String filename;

            public VHItem(View v) {
                super(v);
                pname = (TextView) v
                        .findViewById(R.id.closePRODNAME);
                image = (ImageView) v.findViewById(R.id.icon);
                date = (TextView) v.findViewById(R.id.date);
                prodname = (TextView) v.findViewById(R.id.prodName);
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
     * @param name
     */
    private void openMov(String name) {
        File file = new File(
                getActivity().getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS) + "/"
                        + bmodel.userMasterHelper.getUserMasterBO().getUserid()
                        + DataMembers.DIGITAL_CONTENT + "/"
                        + DataMembers.DIGITALCONTENT + "/" + name);
        if (file.exists()) {
            Uri path = Uri.fromFile(file);
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(path, "video/*");
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
