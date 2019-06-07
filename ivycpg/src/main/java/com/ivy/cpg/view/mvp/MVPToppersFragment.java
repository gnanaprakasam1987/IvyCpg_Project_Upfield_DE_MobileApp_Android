package com.ivy.cpg.view.mvp;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.content.ContextCompat;
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

public class MVPToppersFragment extends IvyBaseFragment {

    private MVPHelper mvpHelper;
    private BusinessModel bmodel;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        bmodel = (BusinessModel) getActivity().getApplicationContext();
        bmodel.setContext(getActivity());
        mvpHelper = MVPHelper.getInstance(getActivity());
        mvpHelper.loadMVPToppersData();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.mvp_topper_fragment, container, false);
        RecyclerView recyclerView = view.findViewById(R.id.rc_mvpkpi);
        recyclerView.setHasFixedSize(false);

        final LinearLayoutManager mLayoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        RecyclerAdapter recyclerAdapter = new RecyclerAdapter(mvpHelper.getMvpToppersList());
        recyclerView.setAdapter(recyclerAdapter);
        return view;
    }

    class RecyclerAdapter extends RecyclerView.Adapter<RecyclerAdapter.MyViewHolder> {

        List<MVPToppersBO> toppersList;

        RecyclerAdapter(List<MVPToppersBO> toppersList) {
            this.toppersList = toppersList;
        }

        @Override
        public RecyclerAdapter.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(
                    parent.getContext()).inflate(R.layout.mvp_topper_list_item, parent, false);
            return new RecyclerAdapter.MyViewHolder(view);
        }

        @Override
        public void onBindViewHolder(final RecyclerAdapter.MyViewHolder holder, int position) {
            holder.mvpTopperBO = toppersList.get(position);
            holder.tv_activityName.setText(holder.mvpTopperBO.getName());
            String subText = (holder.mvpTopperBO.getDistributorname().trim().length()) > 0 ?
                    holder.mvpTopperBO.getDistributorname() + "," : "";
            holder.tv_subactivityname.setText(subText + holder.mvpTopperBO.getLocationname());
            holder.tv_score.setText(holder.mvpTopperBO.getScore() + "%");

            if (holder.mvpTopperBO.getBadge() != null) {
                Bitmap bitmap = getBitMapImage(holder.mvpTopperBO.getBadge());
                holder.badge.setImageBitmap(bitmap);
            } else {
                holder.badge.setImageDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.ic_badge));
            }

            holder.tv_rank.setText(holder.mvpTopperBO.getRank() + "");
        }

        @Override
        public int getItemCount() {
            return toppersList.size();
        }

        class MyViewHolder extends RecyclerView.ViewHolder {

            TextView tv_activityName;
            TextView tv_subactivityname;
            TextView tv_score;
            TextView tv_rank;
            ImageView badge;
            MVPToppersBO mvpTopperBO;

            MyViewHolder(View itemView) {
                super(itemView);
                tv_activityName = itemView.findViewById(R.id.activityName);
                tv_subactivityname = itemView.findViewById(R.id.subactivityName);
                tv_score = itemView.findViewById(R.id.score);
                tv_rank = itemView.findViewById(R.id.rank);
                badge = itemView.findViewById(R.id.badge);
            }
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
