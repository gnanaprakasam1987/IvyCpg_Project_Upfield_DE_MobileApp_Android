package com.ivy.cpg.view.asset;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.bo.StandardListBO;
import com.ivy.cpg.view.asset.bo.AssetTrackingBO;
import com.ivy.sd.png.commons.IvyBaseActivityNoActionBar;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.view.MyDialogCloseListener;

import java.util.ArrayList;

public class AssetMovementActivity extends IvyBaseActivityNoActionBar implements MyDialogCloseListener {

    protected BusinessModel mBModel;
    protected RecyclerView recyclerView;
    protected ArrayList<AssetTrackingBO> mAssetTrackingList = new ArrayList<>();
    protected ArrayAdapter<StandardListBO> mLocationAdapter;
    private int mSelectedLocationIndex = -99; //-99 is the Invalid Integer
    protected StandardListBO mSelectedStandardListBO;
    protected RecyclerAdapter recyclerAdapter;
    protected MovementAssetDialog movementAssetDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_asset_movement);
        mBModel = (BusinessModel) getApplicationContext();
        mBModel.setContext(this);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            setScreenTitle(getResources().getString(R.string.moveAsset));
        }

        if (getIntent().getExtras() != null) {
            mSelectedLocationIndex = getIntent().getIntExtra("index", -99);
        }
        recyclerView = findViewById(R.id.recyclerview_move_asset);
        if (recyclerView != null) {
            recyclerView.setHasFixedSize(false);
        }
        final LinearLayoutManager mLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());


    }

    @Override
    protected void onStart() {
        super.onStart();
        mLocationAdapter = new ArrayAdapter<>(AssetMovementActivity.this, android.R.layout.select_dialog_singlechoice);

        for (StandardListBO temp : mBModel.productHelper.getInStoreLocation())
            mLocationAdapter.add(temp);
        if (mLocationAdapter.getCount() > 0 && mSelectedLocationIndex != -99) {
            mSelectedStandardListBO = mLocationAdapter.getItem(mSelectedLocationIndex);
        }
        if (mSelectedLocationIndex != -99 && mSelectedStandardListBO != null)
            updateList(getApplicationContext(), mSelectedStandardListBO);
        else {
            Toast.makeText(this, "Failed, Try Again Later", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    /**
     * update List with asset movement details
     *
     * @param standardListBO Selected Location Object
     */
    protected void updateList(Context mContext, StandardListBO standardListBO) {
        mAssetTrackingList = standardListBO.getAssetTrackingList();
        AssetTrackingHelper assetTrackingHelper = AssetTrackingHelper.getInstance(this);

        ArrayList<String> mMovedList = assetTrackingHelper.getAssetMovementDetails(mContext);
        ArrayList<Integer> toRemovePos = new ArrayList<>();
        if (mAssetTrackingList != null && mAssetTrackingList.size() > 0) {
            if (mMovedList != null && mMovedList.size() > 0) {
                for (int i = 0; i < mMovedList.size(); i++) {
                    String tempMoved = mMovedList.get(i);
                    for (int j = 0; j < mAssetTrackingList.size(); j++) {
                        if (tempMoved.equalsIgnoreCase(String.valueOf(mAssetTrackingList.get(j).getAssetID()))) {
                            toRemovePos.add(j);
                        }
                    }
                }
                ArrayList<AssetTrackingBO> assetTrackingList = new ArrayList<>();
                for (int i = 0; i < mAssetTrackingList.size(); i++) {
                    if (mBModel.configurationMasterHelper.IS_GLOBAL_CATEGORY && !mAssetTrackingList.get(i).getParentHierarchy().contains("/" + mBModel.productHelper.getmSelectedGlobalProductId() + "/"))
                        continue;
                    if (!toRemovePos.contains(i)) {
                        assetTrackingList.add(mAssetTrackingList.get(i));
                    }
                }
                if (assetTrackingList.size() > 0) {
                    mAssetTrackingList = assetTrackingList;
                }
            }
            if (mAssetTrackingList.size() > 0) {
                recyclerAdapter = new RecyclerAdapter(mAssetTrackingList);
                recyclerView.setAdapter(recyclerAdapter);
            } else {
                Toast.makeText(AssetMovementActivity.this, getResources().getString(R.string.no_assets_exists),
                        Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(AssetMovementActivity.this, getResources().getString(R.string.no_assets_exists),
                    Toast.LENGTH_SHORT).show();
        }

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (recyclerView != null) {
            recyclerView.setItemAnimator(null);
            recyclerView.setAdapter(null);
            recyclerView = null;
        }
    }

    @Override
    public void handleDialogClose(DialogInterface dialog) {
        updateList(getApplicationContext(), mSelectedStandardListBO);
    }


    class RecyclerAdapter extends RecyclerView.Adapter<RecyclerAdapter.MyViewHolder> {
        protected ArrayList<AssetTrackingBO> data;

        RecyclerAdapter(ArrayList<AssetTrackingBO> data) {
            this.data = data;
        }

        @NonNull
        @Override
        public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(
                    parent.getContext()).inflate(R.layout.row_asset_movement, parent, false);
            return new MyViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull final MyViewHolder holder, int position) {
            holder.assetTrackingBO = data.get(position);
            holder.TVAssetName.setText(holder.assetTrackingBO.getAssetName());
            holder.TVSerialNumber.setText(holder.assetTrackingBO.getSerialNo());

            holder.IVMoveIcon.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    movementAssetDialog = new MovementAssetDialog();
                    Bundle args = new Bundle();
                    args.putString("retailerName", mBModel.getRetailerMasterBO().getRetailerName());
                    args.putString("serialNo", holder.assetTrackingBO.getSerialNo());
                    args.putString("assetName", holder.assetTrackingBO.getAssetName());
                    args.putInt("assetId", holder.assetTrackingBO.getAssetID());
                    args.putString("brand", holder.assetTrackingBO.getProductId() + "");
                    movementAssetDialog.setArguments(args);
                    movementAssetDialog.show(getSupportFragmentManager(), "Asset");
                }
            });
        }

        @Override
        public int getItemCount() {
            return data.size();
        }

        class MyViewHolder extends RecyclerView.ViewHolder {
            TextView TVAssetName, TVSerialNumber;
            ImageView IVMoveIcon;
            AssetTrackingBO assetTrackingBO;

            MyViewHolder(View itemView) {
                super(itemView);
                TVAssetName = itemView.findViewById(R.id.txt_move_assetName);
                TVSerialNumber = itemView.findViewById(R.id.txt_move_serialNumber);
                IVMoveIcon = itemView.findViewById(R.id.iv_move_icon);
            }
        }
    }

}
