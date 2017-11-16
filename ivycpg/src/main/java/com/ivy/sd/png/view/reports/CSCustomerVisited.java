package com.ivy.sd.png.view.reports;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.annotation.StringDef;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.ivy.countersales.CShistoryDialog;
import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.commons.IvyBaseFragment;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.provider.ConfigurationMasterHelper;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

/**
 * Created by mayuri.v on 7/14/2017.
 */
public class CSCustomerVisited extends IvyBaseFragment {
    BusinessModel bModel;
    private View view;
    RecyclerView recyclerView;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.cs_customervisit_list_frag,
                container, false);
        setScreenTitle("");
        bModel = (BusinessModel) getActivity().getApplicationContext();
        bModel.setContext(getActivity());
        if (getActionBar() != null) {
            getActionBar().setDisplayShowTitleEnabled(false);
            setScreenTitle(bModel.mSelectedActivityName);
            getActionBar().setElevation(0);
            getActionBar().setIcon(0);
        }
        recyclerView = (RecyclerView) view.findViewById(R.id.lvwplist);
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        recyclerView.setAdapter(new MyAdapter());
        return view;
    }
    private ActionBar getActionBar() {
        return ((AppCompatActivity) getActivity()).getSupportActionBar();
    }
    public class MyAdapter extends RecyclerView.Adapter<MyAdapter.MyViewHolder> {

        public class MyViewHolder extends RecyclerView.ViewHolder {
            Integer position;
            TextView customer_name,customer_ph_no;
            LinearLayout lay;

            public MyViewHolder(View view) {
                super(view);
                lay = (LinearLayout) view.findViewById(R.id.lay);
                customer_name = (TextView) view.findViewById(R.id.customer_name);
                customer_name.setTypeface(bModel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
                customer_ph_no = (TextView) view.findViewById(R.id.customer_ph_no);
                customer_ph_no.setTypeface(bModel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));

            }
        }


        @Override
        public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.cs_customervisit_list_item, parent, false);

            return new MyViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(final MyViewHolder holder, int position) {
//            holder.img.setImageBitmap(BitmapFactory.decodeFile(imgList.get(position), options));


            holder.position = position;
            holder.customer_name.setText(bModel.mCounterSalesHelper.getCSCustomerVisitedNames().get(holder.position));
            holder.customer_ph_no.setText(bModel.mCounterSalesHelper.getCSCustomerVisitedPhNo().get(holder.position));
            holder.lay.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        HashMap<String,String> mHeaderLst = bModel.mCounterSalesHelper.downloadCustomerHeaderInformation(bModel.mCounterSalesHelper.getCSCustomerVisitedUID().get(holder.position),true);
                        if (mHeaderLst != null && !mHeaderLst.isEmpty()) {

                            CShistoryDialog mHistoryDialog = new CShistoryDialog(getActivity(), mHeaderLst, bModel,true);
                            mHistoryDialog.show();
                            mHistoryDialog.setCancelable(false);

                        } else {
                            Toast.makeText(getActivity(), "Data not available", Toast.LENGTH_LONG).show();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });

        }

        @Override
        public int getItemCount() {
            return bModel.mCounterSalesHelper.getCSCustomerVisitedNames().size();
        }

    }

}
