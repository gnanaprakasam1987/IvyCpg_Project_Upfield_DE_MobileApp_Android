package com.ivy.sd.png.view;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.TextView;

import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.bo.TaskAssignBO;
import com.ivy.sd.png.model.RemoveRetailerInterface;

import java.util.ArrayList;

/**
 * Created by rajesh.k on 12-05-2016.
 */

public class RemovePresentRetailerDialog extends DialogFragment {
    private static final String TAG="RemoverPresentREtailer";
    private ListView mRetailerLV;
    private Button mCancelBTN,mDeleteBTN;
    private ArrayList<TaskAssignBO> retailerList;
    private RemoveRetailerInterface mRemoveRetailerInterface;
    private String mUserId;

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        if (activity instanceof RemoveRetailerInterface) {
            this.mRemoveRetailerInterface = (RemoveRetailerInterface) activity;
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_remove_retailer, container, false);

        return rootView;
    }

    @Override
    public void onStart() {
        super.onStart();
        getDialog().setTitle(getArguments().getString("title"));
        this.retailerList=(ArrayList<TaskAssignBO>)getArguments().getSerializable("retailerlist");
        this.mUserId=getArguments().getString("userid");

        mCancelBTN=(Button)getView().findViewById(R.id.btn_cancel);
        mDeleteBTN=(Button)getView().findViewById(R.id.btn_delete);
        mRetailerLV=(ListView)getView().findViewById(R.id.lv_retailers);
        mCancelBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();

            }
        });
        mDeleteBTN.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                  mRemoveRetailerInterface.removeRetailer(retailerList,mUserId);
                  dismiss();

            }
        });
        clearRetailerSelection();
        RetailerAdapter adapter=new RetailerAdapter();
        mRetailerLV.setAdapter(adapter);

    }
    private void clearRetailerSelection(){
        for(TaskAssignBO taskAssignBO:retailerList){
            taskAssignBO.setChecked(false);
        }
    }


    private boolean isRetailerRemoved(){
        for(TaskAssignBO taskAssignBO:retailerList){
            if(taskAssignBO.isChecked()){
                return true;
            }
        }
        return false;
    }


    @SuppressLint("NewApi")
    class RetailerAdapter extends BaseAdapter {
        private ArrayList<String> presentList;

        public RetailerAdapter() {
            // TODO Auto-generated constructor stub
            super();


        }

        @Override
        public int getCount() {
            // TODO Auto-generated method stub
            return retailerList.size();
        }

        @Override
        public Object getItem(int position) {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public long getItemId(int position) {
            // TODO Auto-generated method stub
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            final ViewHolder holder;
            if (convertView == null) {
                LayoutInflater inflater = getActivity().getLayoutInflater();
                convertView = inflater.inflate(R.layout.list_retailer,
                        parent, false);
                holder = new ViewHolder();
                holder.retailerTV=(TextView)convertView.findViewById(R.id.tv_retailer_name);
                holder.retailerCBOX=(CheckBox)convertView.findViewById(R.id.cb_retailer_select);
                holder.retailerCBOX.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        holder.taskAssignBO.setChecked(isChecked);
                        if(isRetailerRemoved()){
                            mDeleteBTN.setVisibility(View.VISIBLE);
                        }else{
                            mDeleteBTN.setVisibility(View.GONE);
                        }
                    }
                });





                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            holder.taskAssignBO=retailerList.get(position);
            holder.retailerTV.setText(holder.taskAssignBO.getRetailerName());
            holder.retailerCBOX.setChecked(holder.taskAssignBO.isChecked());

            return convertView;
        }
    }
    class ViewHolder{
        TextView retailerTV;
        CheckBox retailerCBOX;
        TaskAssignBO taskAssignBO;
    }
}
