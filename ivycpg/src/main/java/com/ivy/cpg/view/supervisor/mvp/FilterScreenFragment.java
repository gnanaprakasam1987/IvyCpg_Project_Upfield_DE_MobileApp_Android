package com.ivy.cpg.view.supervisor.mvp;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.ivy.lib.existing.DBUtil;
import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.bo.LevelBO;
import com.ivy.sd.png.provider.ConfigurationMasterHelper;
import com.ivy.sd.png.util.Commons;
import com.ivy.sd.png.util.DataMembers;
import com.ivy.utils.FontUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Vector;


public class FilterScreenFragment extends Fragment implements OnItemClickListener {

    private Context context;
    private RecyclerView levelFileteRV,listFilterRv;
    private Vector<LevelBO> channelMaster = new Vector<>();
    private FilterListAdapter filterListAdapter;

    // Variable to pass back to calling activity to restore the last selected value.
    private HashMap<Integer, Integer> mSelectedIdByLevelId = new HashMap<>();

    private ArrayList<String> levelList = new ArrayList<>(Arrays.asList("Channel"));
    private LevelBO mSelectedLevelBO = new LevelBO();

    public FilterScreenFragment() {

    }

    @SuppressWarnings("unchecked")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.channel_product_filter, container, false);

        this.context = getActivity();

        levelFileteRV = view.findViewById(R.id.filter_level_rv);
        listFilterRv = view.findViewById(R.id.filter_list_rv);

        FilterLevelAdapter filterLevelAdapter = new FilterLevelAdapter();
        levelFileteRV.setAdapter(filterLevelAdapter);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getContext().getApplicationContext());
        levelFileteRV.setLayoutManager(mLayoutManager);
        levelFileteRV.setItemAnimator(new DefaultItemAnimator());

        filterListAdapter = new FilterListAdapter(channelMaster);
        listFilterRv.setAdapter(filterListAdapter);
        RecyclerView.LayoutManager mLayoutManager1 = new LinearLayoutManager(getContext().getApplicationContext());
        listFilterRv.setLayoutManager(mLayoutManager1);
        listFilterRv.setItemAnimator(new DefaultItemAnimator());

        downloadChannel();

        return view;
    }


    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

    }

    public class FilterLevelAdapter extends RecyclerView.Adapter<FilterLevelAdapter.MyViewHolder> {

        public class MyViewHolder extends RecyclerView.ViewHolder {

            private TextView text;
            private ImageView selectedfilters;
            private LinearLayout gridItem;

            public MyViewHolder(View view) {
                super(view);
                text = view.findViewById(R.id.grid_item_text);
                selectedfilters = view.findViewById(R.id.selectedfilters);
                gridItem = view.findViewById(R.id.GridItem);

                text.setTypeface(FontUtils.getFontRoboto(FontUtils.FontType.MEDIUM,context));

                text.setTextColor(ContextCompat.getColor(getActivity(), R.color.WHITE));
                gridItem.setBackgroundColor(ContextCompat.getColor(getActivity(), R.color.Black));
            }
        }

        @Override
        public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.filter_grid_item, parent, false);

            return new MyViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(final MyViewHolder holder, final int position) {

            holder.text.setText(levelList.get(position));
        }

        @Override
        public int getItemCount() {
            return levelList.size();
        }
    }

    public class FilterListAdapter extends RecyclerView.Adapter<FilterListAdapter.MyViewHolder> {

        private Vector<LevelBO> filteritem;
        private LevelBO levelBO;

        FilterListAdapter(Vector<LevelBO> channelMaster){
            this.filteritem = channelMaster;
        }

        public class MyViewHolder extends RecyclerView.ViewHolder {

            private TextView text;
            private ImageView selectedfilters;
            private LinearLayout gridItem;


            public MyViewHolder(View view) {
                super(view);

                text = view.findViewById(R.id.grid_item_text);
                selectedfilters = view.findViewById(R.id.selectedfilters);
                gridItem = view.findViewById(R.id.GridItem);

                text.setTypeface(FontUtils.getFontRoboto(FontUtils.FontType.LIGHT,context));
            }
        }

        @Override
        public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.filter_secondary_list_item, parent, false);

            return new MyViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(final MyViewHolder holder, final int position) {

            levelBO = filteritem.get(position);

            holder.text.setText(channelMaster.get(position).getLevelName());

            if (mSelectedIdByLevelId != null) {

                int selectedLevelId = 0;
                if (mSelectedIdByLevelId.get(mSelectedLevelBO
                        .getProductID()) != null) {
                    selectedLevelId = mSelectedIdByLevelId.get(mSelectedLevelBO
                            .getProductID());
                }

                if (selectedLevelId == levelBO.getProductID()) {
                    holder.selectedfilters.setVisibility(View.VISIBLE);
                } else {
                    holder.selectedfilters.setVisibility(View.GONE);
                }
            } else {
                holder.text.setTextColor(Color.BLACK);
            }

            holder.gridItem.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {

                    if(mSelectedIdByLevelId.get(channelMaster.get(holder.getAdapterPosition()).getProductID()) == null) {
                        mSelectedIdByLevelId.put(channelMaster.get(holder.getAdapterPosition()).getProductID(), 0);
                        holder.selectedfilters.setVisibility(View.VISIBLE);
                    }else{
                        mSelectedIdByLevelId.remove(channelMaster.get(holder.getAdapterPosition()).getProductID());
                        holder.selectedfilters.setVisibility(View.GONE);
                    }
                }
            });
        }

        @Override
        public int getItemCount() {
            return channelMaster.size();
        }
    }

    private void downloadChannel() {
        Vector<LevelBO> channelBo = new Vector<>();
        try {
            LevelBO temp;
            DBUtil db = new DBUtil(context, DataMembers.DB_NAME,
                    DataMembers.DB_PATH);
            db.openDataBase();
            Cursor c = db.selectSQL("select levelid from channellevel order by Sequence DESC limit 2");
            int leveid = 0;
            if (c.getCount() > 0) {
                if (c.moveToFirst()) {
                    leveid = c.getInt(0);
                }
            }

            c = db.selectSQL("SELECT chid, chName FROM ChannelHierarchy where levelid=" + leveid +" and chId in("+getChannelToShow("2018/08/02")+")");
            if (c != null) {

                while (c.moveToNext()) {
                    temp = new LevelBO();
                    temp.setProductID(c.getInt(0));
                    temp.setLevelName(c.getString(1));
                    channelBo.add(temp);
                }
                c.close();
            }
            db.closeDB();
        } catch (Exception e) {

            Commons.printException(e);
        }

        channelMaster.addAll(channelBo);
        filterListAdapter.notifyDataSetChanged();
    }

    private String getChannelToShow(String date){
        String chIds ="";
        try {
            DBUtil db = new DBUtil(context, DataMembers.DB_NAME,
                    DataMembers.DB_PATH);
            db.openDataBase();
            Cursor c = db.selectSQL("select GROUP_CONCAT(distinct channelId) from supRetailerMaster where date ='"+date+"'");
            if (c.getCount() > 0) {
                if (c.moveToLast()) {
                    chIds = c.getString(0);
                }
            }

            db.closeDB();
        } catch (Exception e) {

            Commons.printException(e);
        }
        return chIds;
    }

    interface ItemSelectedListener{
        void selectedChannels(ArrayList<String> ids);
    }

}
