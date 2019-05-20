package com.ivy.ui.AssetServiceRequest;

import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RadioButton;
import android.widget.TextView;

import com.ivy.cpg.view.asset.bo.AssetTrackingBO;
import com.ivy.sd.png.asean.view.R;

import java.util.ArrayList;

public class AssetServiceRequest extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_asset_service_request);
    }


    class RecyclerAdapter extends RecyclerView.Adapter<RecyclerAdapter.MyViewHolder> {

        ArrayList<AssetTrackingBO> data;

        RecyclerAdapter(ArrayList<UserMasterBO> data) {
            this.data = data;
        }

        @NonNull
        @Override
        public RecyclerAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(
                    parent.getContext()).inflate(R.layout.list_item_backupseller, parent, false);
            return new RecyclerAdapter.MyViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull final RecyclerAdapter.MyViewHolder holder, int position) {
            holder.userMasterBO = data.get(position);
            holder.tv_username.setText(holder.userMasterBO.getUserName());
            holder.radioButton.setChecked(holder.userMasterBO.isBackup());
            holder.radioButton.setChecked(lastCheckedPos == position);
        }

        @Override
        public int getItemCount() {
            return data.size();
        }

        class MyViewHolder extends RecyclerView.ViewHolder {
            TextView tv_username;
            RadioButton radioButton;
            UserMasterBO userMasterBO;

            MyViewHolder(View itemView) {
                super(itemView);
                tv_username = itemView.findViewById(R.id.tv_user_name);
                radioButton = itemView.findViewById(R.id.rb_seller);
                radioButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        lastCheckedPos = getAdapterPosition();
                        userMasterBO.setBackup(true);
                        notifyDataSetChanged();
                    }
                });
            }
        }
    }
}
