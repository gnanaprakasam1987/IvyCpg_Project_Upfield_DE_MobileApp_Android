package com.ivy.sd.png.view;

import android.content.res.TypedArray;
import android.os.Bundle;
import androidx.core.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ListView;
import android.widget.TextView;

import com.ivy.lib.DialogFragment;
import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.bo.RetailerMissedVisitBO;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.provider.ConfigurationMasterHelper;

import java.util.ArrayList;

public class MissedCallDialog extends DialogFragment {

    Button button;
    View v;
    private ArrayList<RetailerMissedVisitBO> mMissedRetailerDetails;
    private TypedArray typearr;
    private BusinessModel bmodel;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        if (getDialog() != null) {
            getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);
        }

        getDialog().setCancelable(false);
        this.setCancelable(false);

        v = inflater.inflate(R.layout.dialog_missed_call, container, false);
        bmodel = (BusinessModel) getActivity().getApplicationContext();
        bmodel.setContext(getActivity());

        button = (Button) v.findViewById(R.id.closeBTN);
        ListView lvMissed = (ListView) v.findViewById(R.id.lv_missed);
        typearr = getActivity().getTheme().obtainStyledAttributes(R.styleable.MyTextView);
        ((TextView)v.findViewById(R.id.headerTV)).setTypeface(bmodel.configurationMasterHelper.getFontBaloobhai(ConfigurationMasterHelper.FontType.REGULAR));
        ((TextView)v.findViewById(R.id.titleDate)).setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
        ((TextView)v.findViewById(R.id.titleReason)).setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));

        mMissedRetailerDetails = bmodel.mRetailerHelper.getmMissedRetailerDetails();

        lvMissed.setAdapter(new MyAdapter());

        button.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                getDialog().dismiss();
            }
        });

        return v;
    }


    public boolean isShowing() {
        return getDialog() != null;
    }


    @Override
    public void onStart() {
        super.onStart();
        if (getDialog() == null) {
            return;
        }

        int dialogHeight = (int) getActivity().getResources().getDimension(R.dimen.dialog_height);

        getDialog().getWindow().setLayout(LayoutParams.MATCH_PARENT, dialogHeight);


    }

    class MyAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return mMissedRetailerDetails.size();
        }

        @Override
        public Object getItem(int arg0) {
            return null;
        }

        @Override
        public long getItemId(int arg0) {
            return 0;
        }

        @Override
        public View getView(int pos, View convertView, ViewGroup parent) {
            final ViewHolder holder;
            String tvText;
            if (convertView == null) {
                LayoutInflater inflater = getActivity().getLayoutInflater();
                convertView = inflater.inflate(R.layout.row_missed_dialog,
                        parent, false);
                holder = new ViewHolder();
                holder.tvDate = (TextView) convertView
                        .findViewById(R.id.tv_date);
                holder.tvReason = (TextView) convertView
                        .findViewById(R.id.tv_reason);

                holder.tvDate.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
                holder.tvDate.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));

                convertView.setTag(holder);

            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            holder.missedRetailerBO = mMissedRetailerDetails.get(pos);
            tvText = holder.missedRetailerBO
                    .getMissedDate() + "";
            holder.tvDate.setText(tvText);

            tvText = holder.missedRetailerBO.getReasonDes()
                    + "";
            holder.tvReason.setText(tvText);


            if (pos % 2 == 0)
                convertView.setBackgroundColor(ContextCompat.getColor(getActivity(), R.color.white));
            else
                convertView.setBackgroundColor(ContextCompat.getColor(getActivity(), R.color.white));


            return convertView;
        }

    }

    class ViewHolder {
        TextView tvDate;
        TextView tvReason;
        RetailerMissedVisitBO missedRetailerBO;

    }
}
