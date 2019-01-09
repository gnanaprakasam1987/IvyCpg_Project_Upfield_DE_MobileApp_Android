package com.ivy.sd.png.view;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.bo.StandardListBO;

import java.util.ArrayList;

public class CustomFragment extends DialogFragment {
    private String mTitle = "";


    private TextView mTitleTV;
    private Button mOkBtn;
    private Button mDismisBtn;
    private ListView mCountLV;
    View view;

    private int mSelectedPostion = -1;
    private StandardListBO mSelectedMenuBO;
    ArrayList<StandardListBO> mRetailerSelectionList;

    RetailerSelectionListener retailerSelectionListener;

    public void setCallback(RetailerSelectionListener retailerSelectionListener) {
        this.retailerSelectionListener = retailerSelectionListener;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (getDialog() != null) {
            getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);
        }

        view = inflater.inflate(R.layout.custom_dialog_fragment, container, false);
        mTitle = getArguments().getString("title");
        mRetailerSelectionList = (ArrayList<StandardListBO>) getArguments().getSerializable("mylist");
        getDialog().setTitle(mTitle);

        mTitleTV = view.findViewById(R.id.title);
        mOkBtn = view.findViewById(R.id.btn_ok);
        mDismisBtn = view.findViewById(R.id.btn_dismiss);
        mDismisBtn.setText(getActivity().getResources().getString(R.string.cancel));
        mCountLV = view.findViewById(R.id.lv_colletion_print);

        mTitleTV.setVisibility(View.GONE);

        ArrayAdapter<StandardListBO> adapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_single_choice, mRetailerSelectionList);
        mCountLV.setAdapter(adapter);
        mCountLV.setChoiceMode(ListView.CHOICE_MODE_SINGLE);
        if (mSelectedPostion != -1)
            mCountLV.setItemChecked(mSelectedPostion, true);
        mCountLV.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                dismiss();
                mSelectedMenuBO = mRetailerSelectionList.get(position);
                mSelectedPostion = position;
                retailerSelectionListener.updateRetailerSelectionType(mSelectedMenuBO.getListCode());
            }
        });


        //mCountLV.setAdapter(adapter);
        mOkBtn.setVisibility(View.GONE);
        mDismisBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        return view;
    }

    public interface RetailerSelectionListener{
        void updateRetailerSelectionType(String type);
    }

}