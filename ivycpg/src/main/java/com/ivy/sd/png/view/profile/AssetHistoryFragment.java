package com.ivy.sd.png.view.profile;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.bo.AssetHistoryBO;
import com.ivy.sd.png.commons.IvyBaseFragment;
import com.ivy.sd.png.model.BusinessModel;

import java.util.Vector;

/**
 * Created by anish.k on 9/28/2017.
 *
 *
 */

public class AssetHistoryFragment extends IvyBaseFragment {

    protected BusinessModel mBModel;
    protected RecyclerView recyclerView;
    protected RecyclerAdapter recyclerAdapter;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        getActivity().getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        mBModel = (BusinessModel) getActivity().getApplicationContext();
        mBModel.setContext(getActivity());
        View view = inflater.inflate(R.layout.fragment_asset_history, container,
                false);
        recyclerView = (RecyclerView) view.findViewById(R.id.recycler_asset_history);
        if (recyclerView != null) {
            recyclerView.setHasFixedSize(false);
        }
        final LinearLayoutManager mLayoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());

        loadListData();
        return view;
    }

    private void loadListData() {
        mBModel.profilehelper.downloadAssetHistory(mBModel.getRetailerMasterBO().getRetailerID());
        Vector<AssetHistoryBO> items = mBModel.profilehelper.getAssetHistoryList();
        if(items!=null && items.size()>0)
        {
            recyclerAdapter = new RecyclerAdapter(items);
            recyclerView.setAdapter(recyclerAdapter);
        }
    }

    class RecyclerAdapter extends RecyclerView.Adapter<RecyclerAdapter.MyViewHolder> {
        protected Vector<AssetHistoryBO> data;

        RecyclerAdapter(Vector<AssetHistoryBO> data) {
            this.data = data;
        }

        @Override
        public RecyclerAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(
                    parent.getContext()).inflate(R.layout.row_asset_history_list, parent, false);
            return new RecyclerAdapter.MyViewHolder(view);
        }

        @Override
        public void onBindViewHolder(final RecyclerAdapter.MyViewHolder holder, int position) {
            holder.assetHistoryBO = data.get(position);
            if (position % 2 == 0)
                holder.layoutBackground.setBackgroundColor(ContextCompat.getColor(getActivity(), R.color.white));
            else
                holder.layoutBackground.setBackgroundColor(ContextCompat.getColor(getActivity(), R.color.history_list_bg));
            holder.TVAssetName.setText(holder.assetHistoryBO.getAssetName());
            holder.TVSerialNumber.setText(holder.assetHistoryBO.getAssetSerialNo());
            holder.TVDate.setText(holder.assetHistoryBO.getAssetDate());

        }

        @Override
        public int getItemCount() {
            return data.size();
        }

        class MyViewHolder extends RecyclerView.ViewHolder {
            TextView TVAssetName, TVSerialNumber,TVDate;
            AssetHistoryBO assetHistoryBO;
            LinearLayout layoutBackground;

            MyViewHolder(View itemView) {
                super(itemView);
                TVAssetName = (TextView) itemView.findViewById(R.id.txt_history_assetName);
                TVSerialNumber = (TextView) itemView.findViewById(R.id.txt_history_serialNo);
                TVDate=(TextView)itemView.findViewById(R.id.txt_history_date);
                layoutBackground=(LinearLayout)itemView.findViewById(R.id.list_background);
            }
        }
    }
}
