package com.ivy.cpg.view.sync.largefiledownload;

import android.content.Context;
import android.graphics.Bitmap;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.ivy.sd.png.asean.view.BuildConfig;
import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.util.DataMembers;
import com.ivy.utils.FontUtils;

import java.io.File;
import java.text.DecimalFormat;
import java.util.ArrayList;

import static android.provider.MediaStore.Video.Thumbnails.MINI_KIND;

public class VideoDownloadListAdapter extends RecyclerView.Adapter<VideoDownloadListAdapter.MyViewHolder> {

    private ArrayList<DigitalContentModel> digitalContentBOS;
    private Context context;
    private DecimalFormat df = new DecimalFormat("0.000");

    VideoDownloadListAdapter(Context context,ArrayList<DigitalContentModel> digitalContentBOS){
        this.digitalContentBOS = digitalContentBOS;
        this.context = context;
    }

    @NonNull
    @Override
    public VideoDownloadListAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.video_download_list_item, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull VideoDownloadListAdapter.MyViewHolder holder, int position) {

        int index = digitalContentBOS.get(position).getImgUrl().lastIndexOf('/');
        String mFileName = "";
        if (index >= 0) {
            mFileName = digitalContentBOS.get(position).getImgUrl().substring(index + 1);
        }

        holder.playIcon.setVisibility(View.GONE);

        if (mFileName.endsWith(".png") || mFileName.endsWith(".jpeg")
                || mFileName.endsWith(".jpg") || mFileName.endsWith(".JPG")
                || mFileName.endsWith(".PNG")) {
            holder.defaultImg.setImageResource(R.drawable.ic_default_image);
        } else if (mFileName.endsWith("xls") || mFileName.endsWith("xlsx")){
            holder.defaultImg.setImageResource(R.drawable.ic_digital_excel);
        }else if (mFileName.endsWith("pdf")){
            holder.defaultImg.setImageResource(R.drawable.ic_digital_pdf);
        }else if (mFileName.endsWith(".mp4") || mFileName.endsWith("3gp")){

            Uri path;
            if (Build.VERSION.SDK_INT >= 24) {
                path = FileProvider.getUriForFile(context, BuildConfig.APPLICATION_ID + ".provider", new File(
                        context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS) + "/"
                                + digitalContentBOS.get(position).getUserId()
                                + DataMembers.DIGITAL_CONTENT + "/"
                                + DataMembers.DIGITALCONTENT + "/" + mFileName));
            } else {
                path = Uri.fromFile(new File(
                        context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS) + "/"
                                + digitalContentBOS.get(position).getUserId()
                                + DataMembers.DIGITAL_CONTENT + "/"
                                + DataMembers.DIGITALCONTENT + "/" + mFileName));
            }

            Glide.with(context)
                    .load(path)
                    .placeholder(ContextCompat.getDrawable(context.getApplicationContext(), R.drawable.ic_digital_video))
                    .error(ContextCompat.getDrawable(context.getApplicationContext(), R.drawable.ic_digital_video))
                    .into(holder.defaultImg);

            if (digitalContentBOS.get(position).getPercent() == 100)
                holder.playIcon.setVisibility(View.GONE);
            else
                holder.playIcon.setVisibility(View.GONE);

        }else if (mFileName.endsWith(".mp3") || mFileName.endsWith(".wma")
                || mFileName.endsWith(".wav") || mFileName.endsWith(".ogg")){
            holder.defaultImg.setImageResource(R.drawable.ic_default_audio);
        }

        holder.videoNameTextView.setText(mFileName);

        if (digitalContentBOS.get(position).getDownloadDetail() == null){
            double bytesTotalMB = 0.0;
            if (digitalContentBOS.get(position).getFileSize() != null){
                bytesTotalMB = Double.parseDouble(digitalContentBOS.get(position).getFileSize()) / (double) FileDownloadProvider.MB_IN_BYTES;
            }
            holder.detailTextView.setText("0.0 MB/"+df.format(bytesTotalMB)+"MB ≈");
        }else if (digitalContentBOS.get(position).getDownloadDetail()!=null)
            holder.detailTextView.setText(digitalContentBOS.get(position).getDownloadDetail());

        holder.percentTextview.setText(digitalContentBOS.get(position).getPercent()+"%");
        holder.progressBar.setProgress(digitalContentBOS.get(position).getPercent());

        if (digitalContentBOS.get(position).getPercent() == 100)
            holder.startBtn.setVisibility(View.GONE);

        holder.startBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

    }

    @Override
    public int getItemCount() {
        return digitalContentBOS.size();
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {

        private TextView videoNameTextView,detailTextView,percentTextview;
        private ProgressBar progressBar;
        private ImageView startBtn,defaultImg,playIcon;

        public MyViewHolder(View itemView) {
            super(itemView);

            videoNameTextView = itemView.findViewById(R.id.video_name);
            detailTextView = itemView.findViewById(R.id.tv_percent_txt);
            percentTextview = itemView.findViewById(R.id.percent_tv);
            progressBar = itemView.findViewById(R.id.progressBar);
            startBtn = itemView.findViewById(R.id.start_pause_img);
            defaultImg = itemView.findViewById(R.id.default_img);
            playIcon = itemView.findViewById(R.id.play_icon);

            videoNameTextView.setTypeface(FontUtils.getFontRoboto(context, FontUtils.FontType.LIGHT));
            detailTextView.setTypeface(FontUtils.getFontRoboto(context, FontUtils.FontType.LIGHT));
            percentTextview.setTypeface(FontUtils.getFontRoboto(context, FontUtils.FontType.LIGHT));

        }
    }

}
