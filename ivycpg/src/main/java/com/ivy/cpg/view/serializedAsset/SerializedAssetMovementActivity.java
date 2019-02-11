package com.ivy.cpg.view.serializedAsset;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.commons.IvyBaseActivityNoActionBar;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.view.MyDialogCloseListener;

import java.util.ArrayList;

public class SerializedAssetMovementActivity extends IvyBaseActivityNoActionBar implements MyDialogCloseListener {

    protected BusinessModel mBModel;
    protected RecyclerView recyclerView;
    protected ArrayList<SerializedAssetBO> mAssetTrackingList = new ArrayList<>();
    protected RecyclerAdapter recyclerAdapter;
    protected SerializedAssetMovementDialog movementAssetDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_asset_movement);
        mBModel = (BusinessModel) getApplicationContext();
        mBModel.setContext(this);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        if(getSupportActionBar()!=null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            setScreenTitle(getResources().getString(R.string.moveAsset));
        }

        recyclerView = (RecyclerView) findViewById(R.id.recyclerview_move_asset);
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

            updateList(getApplicationContext());

    }

    /**
     * update List with asset movement details
     * @param
     */
    protected void updateList(Context mContext) {
        SerializedAssetHelper assetTrackingHelper = SerializedAssetHelper.getInstance(this);
        //mAssetTrackingList=assetTrackingHelper.getAssetTrackingList();
        assetTrackingHelper.loadDataForAssetPOSM(getApplicationContext(), "MENU_SERIALIZED_ASSET");
        mAssetTrackingList = assetTrackingHelper.removeMovedAsset(this);


        if(mAssetTrackingList != null && mAssetTrackingList.size()>0) {
            recyclerAdapter = new RecyclerAdapter(mAssetTrackingList);
            recyclerView.setAdapter(recyclerAdapter);
        }
        else {
            Toast.makeText(SerializedAssetMovementActivity.this, getResources().getString(R.string.no_assets_exists),
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
        updateList(getApplicationContext());
    }


    class RecyclerAdapter extends RecyclerView.Adapter<RecyclerAdapter.MyViewHolder> {
        protected ArrayList<SerializedAssetBO> data;

        RecyclerAdapter(ArrayList<SerializedAssetBO> data) {
            this.data = data;
        }

        @Override
        public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(
                    parent.getContext()).inflate(R.layout.row_asset_movement, parent, false);
            return new MyViewHolder(view);
        }

        @Override
        public void onBindViewHolder(final MyViewHolder holder, int position) {
            holder.assetTrackingBO = data.get(position);
            holder.TVAssetName.setText(holder.assetTrackingBO.getAssetName());
            holder.TVSerialNumber.setText(holder.assetTrackingBO.getSerialNo());

            holder.IVMoveIcon.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    movementAssetDialog = new SerializedAssetMovementDialog();
                    Bundle args = new Bundle();
                    args.putString("retailerName", mBModel.getRetailerMasterBO().getRetailerName());
                    args.putString("serialNo", holder.assetTrackingBO.getSerialNo());
                    args.putString("assetName", holder.assetTrackingBO.getAssetName());
                    args.putInt("assetId", holder.assetTrackingBO.getAssetID());
                    //args.putString("brand", holder.assetTrackingBO.getProductId()+"");
                    args.putInt("referenceId", holder.assetTrackingBO.getReferenceId());
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
            SerializedAssetBO assetTrackingBO;

            MyViewHolder(View itemView) {
                super(itemView);
                TVAssetName = (TextView) itemView.findViewById(R.id.txt_move_assetName);
                TVSerialNumber = (TextView) itemView.findViewById(R.id.txt_move_serialNumber);
                IVMoveIcon = (ImageView) itemView.findViewById(R.id.iv_move_icon);
            }
        }
    }

}
