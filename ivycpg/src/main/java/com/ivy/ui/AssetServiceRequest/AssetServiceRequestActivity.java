package com.ivy.ui.AssetServiceRequest;

import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.TextView;

import com.ivy.cpg.view.asset.bo.AssetTrackingBO;
import com.ivy.cpg.view.serializedAsset.SerializedAssetBO;
import com.ivy.sd.png.asean.view.R;

import java.util.ArrayList;

public class AssetServiceRequestActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_asset_service_request);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }

    class RecyclerAdapter extends RecyclerView.Adapter<RecyclerAdapter.MyViewHolder> {

        ArrayList<SerializedAssetBO> data;

        RecyclerAdapter(ArrayList<SerializedAssetBO> data) {
            this.data = data;
        }

        @NonNull
        @Override
        public RecyclerAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(
                    parent.getContext()).inflate(R.layout.row_asset_service_request, parent, false);
            return new RecyclerAdapter.MyViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull final RecyclerAdapter.MyViewHolder holder, int position) {
            holder.assetTrackingBO = data.get(position);
            holder.tv_assetName.setText(holder.assetTrackingBO.getAssetName());
            holder.tv_serialNum.setText(holder.assetTrackingBO.getSerialNo());
            holder.tv_status.setText(holder.assetTrackingBO.getAssetServiceReqStatus());
        }

        @Override
        public int getItemCount() {
            return data.size();
        }

        class MyViewHolder extends RecyclerView.ViewHolder {
            TextView tv_assetName,tv_serialNum,tv_status;
            SerializedAssetBO assetTrackingBO;

            MyViewHolder(View itemView) {
                super(itemView);
                tv_assetName = itemView.findViewById(R.id.tv_asset_name);
                tv_serialNum = itemView.findViewById(R.id.tv_serialNo);
                tv_status = itemView.findViewById(R.id.tv_status);

            }
        }
    }
}
