package com.ivy.sd.png.view.profile.userSelection;

import android.app.Dialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.TextView;

import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.bo.UserMasterBO;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.model.UserDialogInterface;
import com.ivy.sd.png.provider.ConfigurationMasterHelper;

import java.util.ArrayList;

public class UserSelectionDialogue extends Dialog {

    private BusinessModel bmodel;
    private UserDialogInterface mUserDialogInterface;
    private ListView mUserLV;
    private TextView mTitleTV;
    private ArrayList<UserMasterBO> mUserList;

    public UserSelectionDialogue(Context context,
                                 ArrayList<UserMasterBO> mechandiserlist,
                                 UserDialogInterface merchandiserInterface) {
        super(context);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_users);

        Context mContext = context;
        this.mUserList = mechandiserlist;
        this.mUserDialogInterface = merchandiserInterface;

        bmodel = (BusinessModel) mContext.getApplicationContext();

        mTitleTV = findViewById(R.id.tv_title);
        mUserLV = findViewById(R.id.lv_readiness_reason);
        Button mCloseBTN = findViewById(R.id.btn_close);

        mTitleTV.setTypeface(bmodel.configurationMasterHelper.getFontBaloobhai(ConfigurationMasterHelper.FontType.REGULAR));
        mCloseBTN.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                UserSelectionDialogue.this.dismiss();
            }
        });
        loadData();
    }

    public void loadData() {
        mTitleTV.setText("Choose Merchandiser");

        MyAdapter adapter = new MyAdapter();
        mUserLV.setAdapter(adapter);

    }

    class MyAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return mUserList.size();
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
            if (convertView == null) {
                LayoutInflater inflater = getLayoutInflater();
                convertView = inflater.inflate(
                        R.layout.list_user_dialogue, parent, false);
                holder = new ViewHolder();
                holder.userNameTV = convertView
                        .findViewById(R.id.tv_user_name);
                holder.userRB = convertView
                        .findViewById(R.id.rb_user);
                holder.userRB.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
                holder.userRB
                        .setOnCheckedChangeListener(new OnCheckedChangeListener() {

                            @Override
                            public void onCheckedChanged(
                                    CompoundButton buttonView, boolean isChecked) {
                                bmodel.getRetailerMasterBO()
                                        .setSelectedUserID(
                                                holder.userBO
                                                        .getUserid());

                                mUserDialogInterface.updateValue();
                                UserSelectionDialogue.this.dismiss();
                            }
                        });

                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            holder.userBO = mUserList.get(position);

            holder.userNameTV.setText(holder.userBO
                    .getUserName());

            return convertView;
        }

    }

    class ViewHolder {
        UserMasterBO userBO;
        RadioButton userRB;
        TextView userNameTV;
    }


}
