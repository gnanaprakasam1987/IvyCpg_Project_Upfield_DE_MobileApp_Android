package com.ivy.cpg.view.profile.assetHistory;

import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.ivy.sd.png.asean.view.R;
import com.ivy.cpg.view.asset.bo.AssetHistoryBO;
import com.ivy.sd.png.commons.IvyBaseFragment;
import com.ivy.sd.png.model.BusinessModel;

import java.util.Vector;

/**
 * Created by anish.k on 9/28/2017.
 * This screen shows asset history in profile screen
 *
 */

public class AssetHistoryFragment extends IvyBaseFragment {

    protected BusinessModel mBModel;
    protected RecyclerView recyclerView;
    protected RecyclerAdapter recyclerAdapter;
    private boolean _hasLoadedOnce = false;
    private View view;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        getActivity().getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        mBModel = (BusinessModel) getActivity().getApplicationContext();
        mBModel.setContext(getActivity());
        view = inflater.inflate(R.layout.fragment_asset_history, container,
                false);

        return view;
    }

    private void initializeViews() {
        if (view != null) {
            recyclerView = view.findViewById(R.id.recycler_asset_history);
            if (recyclerView != null) {
                recyclerView.setHasFixedSize(false);
            }
            final LinearLayoutManager mLayoutManager = new LinearLayoutManager(getActivity());
            recyclerView.setLayoutManager(mLayoutManager);
            recyclerView.setItemAnimator(new DefaultItemAnimator());

            loadListData();
        }
    }

    @Override
    public void setUserVisibleHint(boolean isFragmentVisible_) {
        super.setUserVisibleHint(isFragmentVisible_);


        if (this.isVisible()) {
            // we check that the fragment is becoming visible
            isFragmentVisible_ = false;
            if (!isFragmentVisible_ && !_hasLoadedOnce) {
                //run your async task here since the user has just focused on your fragment
                initializeViews();
                _hasLoadedOnce = true;

            }
        }
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
                holder.layoutBackground.setBackgroundColor(ContextCompat.getColor(getActivity(), R.color.white));
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
                TVAssetName = itemView.findViewById(R.id.txt_history_assetName);
                TVSerialNumber = itemView.findViewById(R.id.txt_history_serialNo);
                TVDate= itemView.findViewById(R.id.txt_history_date);
                layoutBackground= itemView.findViewById(R.id.list_background);
            }
        }
    }
}
