package com.ivy.sd.png.view;

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
import com.ivy.sd.png.bo.StandardListBO;
import com.ivy.sd.png.bo.UserMasterBO;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.model.UserDialogInterface;
import com.ivy.sd.png.provider.ConfigurationMasterHelper;

import java.util.ArrayList;

public class UserDialogue extends Dialog {
    private static final String TAG = "ReadinessUserDialogue";
    private Context mContext;
    private BusinessModel bmodel;
    private UserDialogInterface mUserDialogInterface;
    private StandardListBO mStandardListBO;
    private ListView mUserLV;
    private TextView mTitleTV;
    private Button mCloseBTN;
    private ArrayList<UserMasterBO> mUserList;

    public UserDialogue(Context context,
                        ArrayList<UserMasterBO> mechandiserlist,
                        UserDialogInterface merchandiserInterface) {
        super(context);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_users);

        // TODO Auto-generated constructor stub
        this.mContext = context;
        this.mUserList = mechandiserlist;
        this.mUserDialogInterface = merchandiserInterface;

        bmodel = (BusinessModel) mContext.getApplicationContext();
        mTitleTV = (TextView) findViewById(R.id.tv_title);
        mUserLV = (ListView) findViewById(R.id.lv_readiness_reason);
        mCloseBTN = (Button) findViewById(R.id.btn_close);

        mTitleTV.setTypeface(bmodel.configurationMasterHelper.getFontBaloobhai(ConfigurationMasterHelper.FontType.REGULAR));
        mCloseBTN.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub

                UserDialogue.this.dismiss();

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
            // TODO Auto-generated method stub
            return mUserList.size();
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
            // TODO Auto-generated method stub
            final ViewHolder holder;
            if (convertView == null) {
                LayoutInflater inflater = getLayoutInflater();
                convertView = inflater.inflate(
                        R.layout.list_user_dialogue, parent, false);
                holder = new ViewHolder();
                holder.userNameTV = (TextView) convertView
                        .findViewById(R.id.tv_user_name);
                holder.userRB = (RadioButton) convertView
                        .findViewById(R.id.rb_user);
                holder.userRB.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
                holder.userRB
                        .setOnCheckedChangeListener(new OnCheckedChangeListener() {

                            @Override
                            public void onCheckedChanged(
                                    CompoundButton buttonView, boolean isChecked) {
                                // TODO Auto-generated method stub
                                bmodel.getRetailerMasterBO()
                                        .setSelectedUserID(
                                                holder.userBO
                                                        .getUserid());
                                // if (bmodel.checkDataForSyncTeamLeader()) {
                                // onCreateDialog(1).show();
                                //
                                // } else {
                                mUserDialogInterface.updateValue();
                                // }

                                UserDialogue.this.dismiss();
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
