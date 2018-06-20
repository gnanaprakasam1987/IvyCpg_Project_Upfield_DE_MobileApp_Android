/*
 * Copyright (C) 2014 Lucas Rocha
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.ivy.cpg.view.supervisor.adapter;

import android.content.Context;
import android.content.Intent;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.ivy.cpg.view.supervisor.activity.SellerMapViewActivity;
import com.ivy.cpg.view.supervisor.helper.DetailsBo;
import com.ivy.sd.png.asean.view.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class LayoutAdapter extends RecyclerView.Adapter<LayoutAdapter.SimpleViewHolder> {
    private static final int DEFAULT_ITEM_COUNT = 100;

    private final Context mContext;
    private final RecyclerView mRecyclerView;
    private final List<DetailsBo> mItems;
    private int mCurrentItemId = 0;

    public static class SimpleViewHolder extends RecyclerView.ViewHolder {

        private TextView userName;

        public SimpleViewHolder(View view) {
            super(view);

            userName = view.findViewById(R.id.tv_user_name);
        }
    }

    public LayoutAdapter(Context context, RecyclerView recyclerView, HashMap<String, DetailsBo> userHashmap) {
        mContext = context;
        mItems = new ArrayList<>(userHashmap.values());
//        int count = 0;
//        for (DetailsBo detailsBo:userHashmap.values()){
//            addItem(count,detailsBo);
//            count = count + 1;
//        }
        notifyDataSetChanged();
        mRecyclerView = recyclerView;
    }

    public void addItem(int position,DetailsBo detailsBo) {
        mItems.add(position, detailsBo);
        notifyItemInserted(position);
    }

    public void removeItem(int position) {
        mItems.remove(position);
        notifyItemRemoved(position);
    }

    @Override
    public SimpleViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        final View view = LayoutInflater.from(mContext).inflate(R.layout.map_seller_info_layout, parent, false);
        return new SimpleViewHolder(view);
    }

    @Override
    public void onBindViewHolder(SimpleViewHolder holder, int position) {

        final View itemView = holder.itemView;

        holder.userName.setText(mItems.get(position).getUserName()+"---"+mItems.get(position).getMarker().getSnippet());

        itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mContext, SellerMapViewActivity.class);
                intent.putExtra("SellerId", "1695");
                intent.putExtra("screentitle", "Seller");
                intent.putExtra("TrackingType", 1);
                mContext.startActivity(intent);
            }
        });
//        final DetailsBo detailsBo = mItems.get(position);
    }

    @Override
    public int getItemCount() {
        return mItems.size();
    }
}
