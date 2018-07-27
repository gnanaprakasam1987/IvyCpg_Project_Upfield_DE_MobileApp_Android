package com.ivy.sd.png.view;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.TextView;

import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.bo.StandardListBO;

import java.util.ArrayList;

/**
 * Created by Hanifa M on 23/7/18.
 */

public class PrioritySelectionDialog extends Dialog {
    private String Title = "";
    private TextView mTitleTV;
    private Button mOkBtn, mDismisBtn;
    private ListView mPriorityproductLV;
    private Context mContext;

    int mSelectedpostion;
    private int mhasLink = 0;
    ArrayList<StandardListBO> mPriorityProductList = new ArrayList<>();
    private PrioritySelectionListener prioritySelectionListener;

    public PrioritySelectionDialog(@NonNull Context context, String mTitle, int hasLink, int mSelectedposition, ArrayList<StandardListBO> PriorityProductList) {
        super(context);
        mContext = context;
        Title = mTitle;
        mhasLink = hasLink;
        mSelectedpostion = mSelectedposition;
        mPriorityProductList = PriorityProductList;
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setCancelable(false);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setCancelable(false);
        setContentView(R.layout.custom_dialog_fragment);


        mTitleTV = (TextView) findViewById(R.id.title);
        mOkBtn = (Button) findViewById(R.id.btn_ok);

        if (mhasLink == 0)
            mOkBtn.setVisibility(View.GONE);

        mDismisBtn = (Button) findViewById(R.id.btn_dismiss);
        mDismisBtn.setVisibility(View.GONE);

        mPriorityproductLV = (ListView) findViewById(R.id.lv_colletion_print);

        mTitleTV.setText(Title);


        if (mhasLink == 0) {
            ArrayAdapter<StandardListBO> adapter = new ArrayAdapter<>(mContext, android.R.layout.simple_list_item_single_choice, mPriorityProductList);
            mPriorityproductLV.setAdapter(adapter);
            mPriorityproductLV.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
            if (mSelectedpostion != -1)
                mPriorityproductLV.setItemChecked(mSelectedpostion, true);
            mPriorityproductLV.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                    StandardListBO standardListBO = mPriorityProductList.get(position);
                    prioritySelectionListener.updateSelectedItems(position, standardListBO);
                }
            });
        } else if (mhasLink == 1) {
            MyAdapter adapter = new MyAdapter();
            mPriorityproductLV.setAdapter(adapter);
        }


        mOkBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                prioritySelectionListener.updatePriorityProducts(mPriorityProductList);
            }
        });
    }


    class MyAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return mPriorityProductList.size();
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
            LayoutInflater inflater = (LayoutInflater) mContext
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            final ViewHolder holder;
            if (convertView == null) {
                holder = new ViewHolder();
                convertView = inflater.inflate(R.layout.list_priotityproduct,
                        parent, false);
                holder.productNameTV = (TextView) convertView.findViewById(R.id.tv_product_name);
                holder.productSelectCB = (CheckBox) convertView.findViewById(R.id.cb_productselect);
                holder.productSelectCB.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        holder.standardListBO.setChecked(isChecked);
                    }
                });
                convertView.setTag(holder);

            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            holder.standardListBO = mPriorityProductList.get(position);
            holder.productNameTV.setText(holder.standardListBO.getListName());
            holder.productSelectCB.setChecked(holder.standardListBO.isChecked());
            return convertView;
        }
    }

    class ViewHolder {
        StandardListBO standardListBO;
        TextView productNameTV;
        CheckBox productSelectCB;

    }

    public interface PrioritySelectionListener {
        void updateSelectedItems(int position, StandardListBO standardListBO);

        void updatePriorityProducts(ArrayList<StandardListBO> mPriorityProductList);
    }

    public void setPrioritySelectionListener(Fragment Listner) {
        this.prioritySelectionListener = (PrioritySelectionListener) Listner;
    }
}


