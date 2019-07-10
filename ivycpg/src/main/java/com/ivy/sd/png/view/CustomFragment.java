package com.ivy.sd.png.view;

import android.os.Bundle;
import androidx.fragment.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.bo.StandardListBO;

import java.util.ArrayList;

public class CustomFragment extends DialogFragment {
    private String mTitle = "";


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

        view = inflater.inflate(R.layout.custom_dialog_fragment, container);
        mTitle = getArguments().getString("title");
        mRetailerSelectionList = (ArrayList<StandardListBO>) getArguments().getSerializable("mylist");
      //  getDialog().setTitle(mTitle);

        mOkBtn = view.findViewById(R.id.btn_ok);
        mDismisBtn = view.findViewById(R.id.btn_dismiss);
        mDismisBtn.setText(getActivity().getResources().getString(R.string.cancel));
        mCountLV = view.findViewById(R.id.lv_colletion_print);


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

    @Override
    public void onStart() {
        super.onStart();

        WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
        lp.width = WindowManager.LayoutParams.MATCH_PARENT;
        lp.height = WindowManager.LayoutParams.WRAP_CONTENT;
        Window window = getDialog().getWindow();
        if (window != null) {
            //lp.copyFrom(window.getAttributes()); cmd for device alignment issue
            window.setAttributes(lp);
        }


    }

    public interface RetailerSelectionListener{
        void updateRetailerSelectionType(String type);
    }

}