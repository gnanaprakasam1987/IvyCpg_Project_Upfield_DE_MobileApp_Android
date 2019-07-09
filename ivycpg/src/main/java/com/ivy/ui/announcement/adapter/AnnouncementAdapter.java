package com.ivy.ui.announcement.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.widget.AppCompatImageView;
import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.ivy.sd.png.asean.view.R;
import com.ivy.ui.announcement.model.AnnouncementBo;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

public class AnnouncementAdapter extends RecyclerView.Adapter<AnnouncementAdapter.ViewHolder> {
    private Context mContext;
    private ArrayList<AnnouncementBo> announcementArrayList;

    public AnnouncementAdapter(Context mContext, ArrayList<AnnouncementBo> announcementArrayList) {
        this.mContext = mContext;
        this.announcementArrayList = announcementArrayList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int i) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.row_announcement, parent, false);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        AnnouncementBo announcementBo = announcementArrayList.get(position);

        holder.announcementTxt.setText(announcementBo.getDescription());
    }

    @Override
    public int getItemCount() {
        return announcementArrayList.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.type_img_view)
        AppCompatImageView announcementImgView;

        @BindView(R.id.announcement_tv)
        AppCompatTextView announcementTxt;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}
