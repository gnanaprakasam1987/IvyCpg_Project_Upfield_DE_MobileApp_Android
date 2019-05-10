package com.ivy.ui.photocapture.adapter;

import android.app.Activity;
import android.content.Context;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.support.v4.content.res.ResourcesCompat;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.ivy.ui.photocapture.model.PhotoCaptureLocationBO;
import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.util.DataMembers;
import com.ivy.utils.FontUtils;

import java.util.ArrayList;

public class PhotoGridAdapter extends BaseAdapter {

    private LayoutInflater mInflater;

    private BitmapFactory.Options options = new BitmapFactory.Options();

    private ArrayList<PhotoCaptureLocationBO> photoCaptureLocationBOS;

    private PhotoClickListener mPhotoClickListener;

    private Context mContext;
    private int width;
    private int height;

    public PhotoGridAdapter(Context context, ArrayList<PhotoCaptureLocationBO> photoCaptureLocationBOS, PhotoClickListener photoClickListener) {
        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.photoCaptureLocationBOS = photoCaptureLocationBOS;
        this.mPhotoClickListener = photoClickListener;
        this.mContext = context;
        getDisplayMetrics();
        options.inSampleSize = 4;
    }

    @Override
    public int getCount() {
        return photoCaptureLocationBOS.size();
    }

    @Override
    public Object getItem(int i) {
        return i;
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup viewGroup) {
        final ViewHolder holder;
        if (convertView == null) {
            holder = new ViewHolder();
            convertView = mInflater.inflate(R.layout.prod_grid_item_new, viewGroup, false);
            holder.prod_img = (ImageView) convertView
                    .findViewById(R.id.prod_img);

            holder.type_loc_txt = (TextView) convertView
                    .findViewById(R.id.type_loc_txt);
            holder.loc_txt = (TextView) convertView
                    .findViewById(R.id.loc_txt);
            holder.RLCheckBg = (RelativeLayout) convertView.findViewById(R.id.layout_share_select);
            holder.CBSelect = (CheckBox) convertView.findViewById(R.id.check_share_select);

            holder.type_loc_txt.setTypeface(FontUtils.getFontRoboto(mContext, FontUtils.FontType.MEDIUM));
            holder.loc_txt.setTypeface(FontUtils.getFontRoboto(mContext, FontUtils.FontType.THIN));

            holder.RLCheckBg.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (holder.CBSelect.isChecked())
                        holder.CBSelect.setChecked(false);
                    else
                        holder.CBSelect.setChecked(true);
                }
            });

            holder.CBSelect.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    String imagePath = mContext.getExternalFilesDir(Environment.DIRECTORY_PICTURES) + "/"
                            + DataMembers.photoFolderName + "/" + photoCaptureLocationBOS.get(position).getImageName();
                    if (isChecked) {
                        holder.RLCheckBg.setBackgroundColor(ResourcesCompat.getColor(mContext.getResources(), R.color.light_gray, null));
                        mPhotoClickListener.onPhotoClicked(photoCaptureLocationBOS.get(position).getProductID()
                                + "_" + photoCaptureLocationBOS.get(position).getPhotoTypeId() + "_"
                                + photoCaptureLocationBOS.get(position).getLocationId());

                    } else {
                        holder.RLCheckBg.setBackgroundResource(0);
                        mPhotoClickListener.onPhotoClickRemoved(photoCaptureLocationBOS.get(position).getProductID()
                                + "_" + photoCaptureLocationBOS.get(position).getPhotoTypeId() + "_"
                                + photoCaptureLocationBOS.get(position).getLocationId());
                    }
                }
            });
            convertView.setTag(holder);

        } else {
            holder = (ViewHolder) convertView.getTag();
        }
        holder.type_loc_txt.setText(photoCaptureLocationBOS.get(position).getmTypeName());
        holder.loc_txt.setText(photoCaptureLocationBOS.get(position).getLocationName());

        Glide.with(mContext).load(
                mContext.getExternalFilesDir(Environment.DIRECTORY_PICTURES) + "/"
                        + DataMembers.photoFolderName + "/" + photoCaptureLocationBOS.get(position).getImageName())
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .centerCrop()
                .error(R.drawable.no_image_available)
                .override(width / 2, height / 4)
                .into(holder.prod_img);

        return convertView;
    }

    private void getDisplayMetrics() {
        // To get the device screen width and height
        DisplayMetrics displayMetrics = new DisplayMetrics();
        ((Activity) mContext).getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        width = displayMetrics.widthPixels;
        height = displayMetrics.heightPixels;
    }

    class ViewHolder {
        ImageView imageview, prod_img;
        RelativeLayout RLCheckBg;
        CheckBox CBSelect;
        Button check;
        int id;
        TextView type_loc_txt, loc_txt;
    }

    public interface PhotoClickListener {

        void onPhotoClicked(String selectedItem);

        void onPhotoClickRemoved(String selectedItem);
    }

}
