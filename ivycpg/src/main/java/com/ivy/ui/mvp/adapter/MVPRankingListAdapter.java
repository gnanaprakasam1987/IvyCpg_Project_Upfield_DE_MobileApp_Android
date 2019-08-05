package com.ivy.ui.mvp.adapter;

import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.ivy.cpg.view.digitalcontent.DigitalContentImagesFragment;
import com.ivy.cpg.view.mvp.MvpBO;
import com.ivy.sd.png.asean.view.BuildConfig;
import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.util.DataMembers;
import com.ivy.ui.mvp.MVPContractor;

import java.io.File;
import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MVPRankingListAdapter extends RecyclerView.Adapter<MVPRankingListAdapter.MVPRankingViewHolder> {
    private Context mContext;
    private ArrayList<MvpBO> mvpList;
    String userID;

    public MVPRankingListAdapter(Context mContext, ArrayList<MvpBO> mvpList, String userID) {
        this.mContext = mContext;
        this.mvpList = mvpList;
        this.userID = userID;
    }

    @Override
    public MVPRankingListAdapter.MVPRankingViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.mvpranking_list_item, parent, false);
        return new MVPRankingListAdapter.MVPRankingViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final MVPRankingListAdapter.MVPRankingViewHolder holder, int position) {
        MvpBO mvpObj = mvpList.get(position);
        holder.txtName.setText(mvpObj.getUsername());
        holder.txtScore.setText(mvpObj.getRank() + "");
        holder.txtSNo.setText((position + 1)+ "");

        String imgPath = mvpObj.getImageName().substring(mvpObj.getImageName().lastIndexOf("/") + 1,
                mvpObj.getImageName().length());
        Uri path;
        File file = new File(
                mContext.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS) + "/"
                        + userID
                        + DataMembers.DIGITAL_CONTENT + "/"
                        + DataMembers.USER + "/" + imgPath);
        if (Build.VERSION.SDK_INT >= 24) {
            path = FileProvider.getUriForFile(mContext, BuildConfig.APPLICATION_ID + ".provider", file);
        } else {
            path = Uri.fromFile(file);
        }
        Glide.with(mContext)
                .load(path)
                .error(ContextCompat.getDrawable(mContext.getApplicationContext(), R.drawable.no_image_available))
                .dontAnimate()
                .diskCacheStrategy(DiskCacheStrategy.RESULT)
                .into(holder.imgUser);

    }

    @Override
    public int getItemCount() {
        return mvpList.size();
    }

    class MVPRankingViewHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.txtSNo)
        TextView txtSNo;

        @BindView(R.id.txtName)
        TextView txtName;

        @BindView(R.id.txtScore)
        TextView txtScore;

        @BindView(R.id.imgUser)
        ImageView imgUser;

        @BindView(R.id.imgBadge)
        ImageView imgBadge;

        MVPRankingViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}

