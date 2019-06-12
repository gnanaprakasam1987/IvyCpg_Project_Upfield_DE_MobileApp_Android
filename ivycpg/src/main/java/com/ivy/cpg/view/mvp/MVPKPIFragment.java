package com.ivy.cpg.view.mvp;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.commons.IvyBaseFragment;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.util.DataMembers;
import com.ivy.utils.FileUtils;

import java.io.File;
import java.util.List;

public class MVPKPIFragment extends IvyBaseFragment {

    private MVPHelper mvpHelper;
    private BusinessModel bmodel;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        bmodel = (BusinessModel) getActivity().getApplicationContext();
        bmodel.setContext(getActivity());
        mvpHelper = MVPHelper.getInstance(getActivity());
        mvpHelper.loadMVPKPIData(bmodel.getAppDataProvider().getUser().getUserid());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.mvp_kpi_fragment, container, false);
        RecyclerView recyclerView = view.findViewById(R.id.rc_mvpkpi);
        recyclerView.setHasFixedSize(false);

        final LinearLayoutManager mLayoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        RecyclerAdapter recyclerAdapter = new RecyclerAdapter(mvpHelper.getMvpKPIList());
        recyclerView.setAdapter(recyclerAdapter);
        return view;
    }

    class RecyclerAdapter extends RecyclerView.Adapter<RecyclerAdapter.MyViewHolder> {

        List<MvpBO> data;

        RecyclerAdapter(List<MvpBO> kpiList) {
            this.data = kpiList;
        }

        @Override
        public RecyclerAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(
                    parent.getContext()).inflate(R.layout.mvp_kpi_list_item, parent, false);
            return new RecyclerAdapter.MyViewHolder(view);
        }

        @Override
        public void onBindViewHolder(final RecyclerAdapter.MyViewHolder holder, int position) {
            holder.mvpBO = data.get(position);
            holder.tv_rank.setText(String.valueOf(holder.mvpBO.getTotalRank()));
            holder.tv_Achv.setText(String.valueOf(holder.mvpBO.getTotalScore()));
            holder.tv_kpi.setText(holder.mvpBO.getKpiName());
            Drawable bitmapDrawable = new BitmapDrawable(getBitMapImage(holder.mvpBO.getBatchURL()));
            holder.img_badge.setImageDrawable(bitmapDrawable);
        }

        @Override
        public int getItemCount() {
            return data.size();
        }

        class MyViewHolder extends RecyclerView.ViewHolder {

            TextView tv_kpi;
            TextView tv_Achv;
            TextView tv_rank;
            ImageView img_badge;
            MvpBO mvpBO;

            MyViewHolder(View itemView) {
                super(itemView);
                tv_kpi = itemView.findViewById(R.id.tv_kpi);
                tv_Achv = itemView.findViewById(R.id.tv_achv);
                tv_rank = itemView.findViewById(R.id.tv_rank);
                img_badge = itemView.findViewById(R.id.img_badge);
            }
        }

        private Bitmap getBitMapImage(String name) {
            File file = new File(
                    getActivity().getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS) + "/"
                            + bmodel.userMasterHelper.getUserMasterBO().getUserid()
                            + DataMembers.DIGITAL_CONTENT + "/"
                            + DataMembers.MVP + "/" + name);
            return FileUtils.decodeFile(file);
        }
    }
}
