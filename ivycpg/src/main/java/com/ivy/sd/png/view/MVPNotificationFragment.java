package com.ivy.sd.png.view;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Shader;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.GeneratePresignedUrlRequest;
import com.amazonaws.services.s3.model.ResponseHeaderOverrides;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.BitmapImageViewTarget;
import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.bo.MVPBadgeBO;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.provider.ConfigurationMasterHelper;
import com.ivy.sd.png.util.DataMembers;

import java.net.URL;
import java.util.ArrayList;

public class MVPNotificationFragment extends Fragment {
    private BusinessModel bmodel;
    private ArrayList<MVPBadgeBO> mMVPBadgesurlList;
    private View rootView;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        bmodel = (BusinessModel) getActivity().getApplicationContext();
        bmodel.setContext(getActivity());
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.fragment_notification_list, container, false);
        return rootView;
    }

    @Override
    public void onStart() {
        super.onStart();
        new DownloadMVPBadgeData().execute();
        ListView mMVPNotificationView = (ListView) rootView.findViewById(R.id.lvNotification);
        mMVPBadgesurlList = new ArrayList<>();
        mMVPBadgesurlList.addAll(bmodel.mvpHelper.getMVPBadgeUrlList());
        if (mMVPBadgesurlList!=null && !mMVPBadgesurlList.isEmpty()) {
            MyAdapter adapter = new MyAdapter(mMVPBadgesurlList);
            mMVPNotificationView.setAdapter(adapter);
        }
    }

    class MyAdapter extends BaseAdapter {

        private final ArrayList<MVPBadgeBO> items;

        private MyAdapter(ArrayList<MVPBadgeBO> mvpBadgeList) {
            super();
            this.items = mvpBadgeList;
        }

        @Override
        public int getCount() {
            return items.size();
        }

        @Override
        public Object getItem(int position) {
            return position;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            final ViewHolder holder;
            View row = convertView;

            if (row == null) {
                    LayoutInflater inflater = getActivity().getLayoutInflater();
                    row = inflater
                            .inflate(R.layout.mvpbadges_update_list, parent, false);
                holder = new ViewHolder();
                holder.imgMVPBadgedImage = (ImageView) row.findViewById(R.id.img_badge);
                holder.tvMVPBadgeName = (TextView) row.findViewById(R.id.tv_badge);
                holder.notiMVPTime = (TextView) row.findViewById(R.id.noti_time);

                row.setTag(holder);
            } else {
                holder = (ViewHolder) row.getTag();
            }
            holder.mvpBadgeBO = items.get(position);
            holder.tvMVPBadgeName.setText(holder.mvpBadgeBO.getBadgeName());
            if(holder.mvpBadgeBO.getImageUrl()!= null) {
                Glide.with(getActivity())
                        .load(holder.mvpBadgeBO.getImageUrl().toString())
                        .asBitmap()
                        .centerCrop()
                        .transform(bmodel.circleTransform)
                        .placeholder(R.drawable.face)
                        .into(new BitmapImageViewTarget(holder.imgMVPBadgedImage));
            }
            holder.notiMVPTime.setText(holder.mvpBadgeBO.getTimeStamp());
            return row;
        }
    }

    class ViewHolder {
        private MVPBadgeBO mvpBadgeBO;
        private TextView tvMVPBadgeName;
        private ImageView imgMVPBadgedImage;
        private TextView notiMVPTime;
    }

    private void downloadImageUrlFromAmazon() {
        AmazonS3Client s3;
        BasicAWSCredentials myCredentials = new BasicAWSCredentials(ConfigurationMasterHelper.ACCESS_KEY_ID,
                ConfigurationMasterHelper.SECRET_KEY);
        s3 = new AmazonS3Client(myCredentials);
        s3.setEndpoint(DataMembers.S3_BUCKET_REGION);
        for (MVPBadgeBO mvpBadgeBO:mMVPBadgesurlList) {
            String imageUrl = mvpBadgeBO.getBadgeURL();
            GeneratePresignedUrlRequest urlRequest = new GeneratePresignedUrlRequest(DataMembers.S3_BUCKET,imageUrl);
            urlRequest.setResponseHeaders(new ResponseHeaderOverrides());
            URL url = s3.generatePresignedUrl(urlRequest);
            mvpBadgeBO.setImageUrl(url);
        }
    }

    private class DownloadMVPBadgeData extends AsyncTask<Void, Void, Void> {

        @Override
        protected Void doInBackground(Void...params) {
            downloadImageUrlFromAmazon();
            return null;
        }
    }

//    private Bitmap getCircularBitmapFrom(Bitmap source) {
//        if (source == null || source.isRecycled()) {
//            return null;
//        }
//        float radius = source.getWidth() > source.getHeight() ? ((float) source
//                .getHeight()) / 2f : ((float) source.getWidth()) / 2f;
//        Bitmap bitmap = Bitmap.createBitmap(source.getWidth(),
//                source.getHeight(), Bitmap.Config.ARGB_8888);
//
//        Paint paint = new Paint();
//        BitmapShader shader = new BitmapShader(source, Shader.TileMode.CLAMP,
//                Shader.TileMode.CLAMP);
//        paint.setShader(shader);
//        paint.setAntiAlias(true);
//
//        Canvas canvas = new Canvas(bitmap);
//        canvas.drawCircle(source.getWidth() / 2, source.getHeight() / 2,
//                radius, paint);
//
//        return bitmap;
//    }
}

