package com.ivy.sd.png.view;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.bo.MVPBadgeBO;
import com.ivy.sd.png.model.BusinessModel;

import java.util.List;

/**
 * Created by rajesh.k on 18-11-2015.
 */
public class MVPDialogFragment extends DialogFragment {
    private static final String TAG="MVPDialogFragment";
    private BusinessModel bmodel;
    private Context mContext;
    private GridView mMVPImagesView;
    private List<MVPBadgeBO> mMvpBadgeInfoList;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_dialog_mvp, container, false);

        this.mContext = getActivity();
        bmodel = (BusinessModel) mContext.getApplicationContext();
        return rootView;
    }

    @Override
    public void onStart() {
        super.onStart();
        getDialog().setTitle(getActivity().getResources().getString(R.string.mvpbadges));
        mMvpBadgeInfoList=bmodel.mvpHelper.getMvpBadgeInfoList();
        mMVPImagesView=(GridView)getView().findViewById(R.id.grid_mvpImages);
        mMVPImagesView.setAdapter(new MyAdapter());
        Button cancel=(Button)getView().findViewById(R.id.btn_cancel);
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });







    }
    class MyAdapter extends BaseAdapter{

        @Override
        public int getCount() {
            return mMvpBadgeInfoList.size();
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            final ViewHolder holder;
            View row = convertView;

                if (row == null) {
                    LayoutInflater inflater = getActivity().getLayoutInflater();
                    row = inflater
                            .inflate(R.layout.mvpbadges_list, parent, false);
                    holder = new ViewHolder();
                    holder.imgMVPBadgedImage=(ImageView)row.findViewById(R.id.badge);
                    holder.tvMVPBadgeCount=(TextView)row.findViewById(R.id.badge_count);
                    row.setTag(holder);
                }else{
                    holder = (ViewHolder) row.getTag();
                }
            holder.mvpBadgeBO=mMvpBadgeInfoList.get(position);
            holder.tvMVPBadgeCount.setText(holder.mvpBadgeBO.getBadgeCount()+"");
            if(holder.mvpBadgeBO.getBadgeBitmap()!=null){
                holder.imgMVPBadgedImage.setImageBitmap(holder.mvpBadgeBO.getBadgeBitmap());
            }else{
                holder.imgMVPBadgedImage.setImageDrawable(getResources().getDrawable(R.drawable.badge_circle));
            }




            return row;
        }
    }

    class ViewHolder{
        private MVPBadgeBO mvpBadgeBO;
        private TextView tvMVPBadgeCount;
        private ImageView imgMVPBadgedImage;
    }
}
