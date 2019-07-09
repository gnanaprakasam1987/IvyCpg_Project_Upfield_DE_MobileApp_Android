package com.ivy.cpg.view.mvp;

import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
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
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.provider.ConfigurationMasterHelper;
import com.ivy.sd.png.util.Commons;
import com.ivy.sd.png.util.DataMembers;
import com.ivy.utils.rx.AppSchedulerProvider;

import java.net.URL;
import java.util.ArrayList;
import java.util.concurrent.Callable;

import io.reactivex.Single;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Consumer;

public class MVPNotificationFragment extends Fragment {
    private BusinessModel bmodel;
    private ArrayList<MVPBadgeBO> mMVPBadgesurlList;
    private View rootView;
    private AppSchedulerProvider appSchedulerProvider;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        bmodel = (BusinessModel) getActivity().getApplicationContext();
        bmodel.setContext(getActivity());
        appSchedulerProvider = new AppSchedulerProvider();
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

        new CompositeDisposable().add(downloadImageUrlFromAmazon()
                .subscribeOn(appSchedulerProvider.io())
                .observeOn(appSchedulerProvider.ui())
                .subscribe(new Consumer<Boolean>() {
                    @Override
                    public void accept(Boolean response) {
                        // just to run in background thread
                    }
                }));
        ListView mMVPNotificationView = rootView.findViewById(R.id.lvNotification);
        mMVPBadgesurlList = new ArrayList<>();
        mMVPBadgesurlList.addAll(MVPHelper.getInstance(getActivity()).getMVPBadgeUrlList());
        if (mMVPBadgesurlList != null && !mMVPBadgesurlList.isEmpty()) {
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
                holder.imgMVPBadgedImage = row.findViewById(R.id.img_badge);
                holder.tvMVPBadgeName = row.findViewById(R.id.tv_badge);
                holder.notiMVPTime = row.findViewById(R.id.noti_time);

                row.setTag(holder);
            } else {
                holder = (ViewHolder) row.getTag();
            }
            holder.mvpBadgeBO = items.get(position);
            holder.tvMVPBadgeName.setText(holder.mvpBadgeBO.getBadgeName());
            if (holder.mvpBadgeBO.getImageUrl() != null) {
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


    private Single<Boolean> downloadImageUrlFromAmazon() {

        return Single.fromCallable(new Callable<Boolean>() {
            @Override
            public Boolean call() {
                try {
                    BasicAWSCredentials myCredentials = new BasicAWSCredentials(ConfigurationMasterHelper.ACCESS_KEY_ID,
                            ConfigurationMasterHelper.SECRET_KEY);
                    AmazonS3Client s3 = new AmazonS3Client(myCredentials);
                    s3.setEndpoint(DataMembers.S3_BUCKET_REGION);
                    for (MVPBadgeBO mvpBadgeBO : mMVPBadgesurlList) {
                        String imageUrl = mvpBadgeBO.getBadgeURL();
                        GeneratePresignedUrlRequest urlRequest = new GeneratePresignedUrlRequest(DataMembers.S3_BUCKET, imageUrl);
                        urlRequest.setResponseHeaders(new ResponseHeaderOverrides());
                        URL url = s3.generatePresignedUrl(urlRequest);
                        mvpBadgeBO.setImageUrl(url);
                    }
                } catch (Exception e) {
                    Commons.print("response Code code getting null value");
                    return false;
                }
                return false;
            }
        });
    }

}

