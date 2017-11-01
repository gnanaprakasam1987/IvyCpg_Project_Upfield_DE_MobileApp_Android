package com.ivy.sd.png.view;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.TextView;

import com.itextpdf.text.List;
import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.bo.OutletReportBO;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.provider.ConfigurationMasterHelper;
import com.ivy.sd.png.util.Commons;

import java.util.ArrayList;

/**
 * Created by rajkumar.s on 10/31/2017.
 */

public class SellerListFragment extends Fragment implements View.OnClickListener {

    View view;
    Context context;
    BusinessModel bmodel;
    ListView listView;
    ArrayList<OutletReportBO> lstUsers;

    CardView card_all_user;
    CheckBox chk_all_user;

    Button btnCancel,btnApply;

    private SellerSelectionInterface sellerInterface;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view=inflater.inflate(R.layout.seller_list, container, false);

        try {
            this.context = getActivity();
            bmodel = (BusinessModel) context.getApplicationContext();

            listView = (ListView) view.findViewById(R.id.listview);
            card_all_user=(CardView) view.findViewById(R.id.card_all_user);
            card_all_user.setOnClickListener(this);
            chk_all_user=(CheckBox)view.findViewById(R.id.checkbox_all_user);
            chk_all_user.setOnClickListener(this);
            chk_all_user.setClickable(false);

            btnCancel=(Button) view.findViewById(R.id.btn_cancel);
            btnCancel.setOnClickListener(this);
            btnApply=(Button) view.findViewById(R.id.btn_ok);
            btnApply.setOnClickListener(this);

            lstUsers = (ArrayList) getArguments().get("users");

            MyAdapter adapter=new MyAdapter(lstUsers);
            listView.setAdapter(adapter);
        }
        catch (Exception ex){
            Commons.printException(ex);
        }

        return view;
    }

    @Override
    public void onAttach(Context activity) {
        super.onAttach(activity);
        if (activity instanceof SellerSelectionInterface) {
            this.sellerInterface = (SellerSelectionInterface) activity;
        }
    }

    @Override
    public void onStart() {
        super.onStart();

    }

    @Override
    public void onClick(View view) {
        if(view.getId()==R.id.btn_cancel){
         sellerInterface.updateClose();
        }
        else if(view.getId()==R.id.btn_ok){

        }
        else if(view.getId()==R.id.card_all_user){
            onClickAllUser();
        }

    }

    private void onClickAllUser(){

        if(!chk_all_user.isChecked()) {
            chk_all_user.setChecked(true);
            checkAllUsers();
        }
        else {
            chk_all_user.setChecked(false);
            unCheckAllUsers();
        }
        listView.invalidateViews();
    }
    private void checkAllUsers(){
        if(lstUsers!=null) {
            for (OutletReportBO bo : lstUsers) {
                bo.setChecked(true);
            }
        }
    }
    private void unCheckAllUsers(){
        if(lstUsers!=null) {
            for (OutletReportBO bo : lstUsers) {
                bo.setChecked(false);
            }
        }
    }

    private void updateAllUserCheckboxStatus(){

        boolean isAllchecked=true;
        if(lstUsers!=null) {
            for (OutletReportBO bo : lstUsers) {
                if(!bo.isChecked()){
                    isAllchecked=false;
                    break;
                }
            }
        }

        if(isAllchecked)
            chk_all_user.setChecked(true);
        else  chk_all_user.setChecked(false);
    }
    class MyAdapter extends ArrayAdapter<OutletReportBO> {
        ArrayList<OutletReportBO> items;

        MyAdapter(ArrayList<OutletReportBO> items) {
            super(getActivity(), R.layout.row_seller_view, items);
            this.items = items;
        }

        public OutletReportBO getItem(int position) {
            return items.get(position);
        }

        public long getItemId(int position) {
            return position;
        }

        public int getCount() {
            return items.size();
        }

        @NonNull
        public View getView(int position, View convertView, @NonNull ViewGroup parent) {

            final ViewHolder holder;

            View row = convertView;
            if (row == null) {
                LayoutInflater inflater = getActivity().getLayoutInflater();
                row = inflater.inflate(R.layout.row_seller_view, parent, false);
                holder = new ViewHolder();

                holder.tv_username = (TextView) row.findViewById(R.id.tv_user);
                holder.checkBox=(CheckBox)  row.findViewById(R.id.checkbox);
                holder.checkBox.setClickable(false);

                holder.tv_username.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));

                row.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {

                        unCheckAllUsers();

                        if(!holder.checkBox.isChecked()) {
                            holder.checkBox.setChecked(true);
                            holder.bo.setChecked(true);
                        }else {
                            holder.checkBox.setChecked(false);
                            holder.bo.setChecked(false);
                        }

                        updateAllUserCheckboxStatus();

                        listView.invalidateViews();

                    }
                });


                row.setTag(holder);
            } else {
                holder = (ViewHolder) row.getTag();
            }

            holder.bo=items.get(position);
            holder.tv_username.setText(holder.bo.getUserName());
            if(holder.bo.isChecked()){
                holder.checkBox.setChecked(true);
            }
            else{
                holder.checkBox.setChecked(false);
            }

            return row;
        }
    }

    class ViewHolder {
        OutletReportBO bo;

        CheckBox checkBox;
        TextView tv_username;

    }

    public interface SellerSelectionInterface{
        void updateMapView(ArrayList<Integer> mSelectedUsers,boolean isAllUser);
        void updateClose();
    }

}
