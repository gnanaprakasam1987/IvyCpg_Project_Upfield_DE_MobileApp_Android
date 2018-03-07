package com.ivy.sd.png.view;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.bo.ChannelBO;

import java.util.ArrayList;

/**
 * Created by Hanifa M on 21/2/18.
 */

@SuppressLint("ValidFragment")
public class ChannelSelectionDialogFragment extends android.support.v4.app.DialogFragment {
    private String mTitle = "";
    private String mMenuName = "";

    private TextView mTitleTV;
    private Button mOkBtn, mDismisBtn;
    private ListView mChannelLV;
    private ArrayList<ChannelBO> mChannelList;
    private ChannelSelectionListener channelSelectionListener;


    @SuppressLint("ValidFragment")
    public ChannelSelectionDialogFragment(ArrayList<ChannelBO> ChannelList) {
        mChannelList = ChannelList;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setCancelable(false);
        mTitle = getArguments().getString("title");
        mMenuName = getArguments().getString("screentitle");
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.custom_dialog_fragment, container, false);

        return rootView;
    }

    @Override
    public void onStart() {
        super.onStart();
        getDialog().setTitle(null);
        mTitleTV = (TextView) getView().findViewById(R.id.title);
        mOkBtn = (Button) getView().findViewById(R.id.btn_ok);
        mOkBtn.setVisibility(View.GONE);
        mDismisBtn = (Button) getView().findViewById(R.id.btn_dismiss);
        mChannelLV = (ListView) getView().findViewById(R.id.lv_colletion_print);

        mTitleTV.setText(mTitle);
        ArrayAdapter<ChannelBO> adapter = new ArrayAdapter<ChannelBO>(getActivity(), android.R.layout.simple_list_item_single_choice, mChannelList);
        mChannelLV.setAdapter(adapter);
        mChannelLV.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                channelSelectionListener.loadNewOutLet(position, mMenuName);


            }
        });
        mOkBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });
        mDismisBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getDialog().dismiss();


            }
        });


    }

    public interface ChannelSelectionListener {
        void loadNewOutLet(int position, String menuName);
    }

    public void setChannelSelectionListener(Fragment listener) {
        this.channelSelectionListener = (ChannelSelectionListener) listener;
    }


}

