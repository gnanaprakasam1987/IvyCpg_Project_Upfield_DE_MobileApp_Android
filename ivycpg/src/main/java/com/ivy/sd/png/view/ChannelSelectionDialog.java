package com.ivy.sd.png.view;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.view.View;
import android.view.Window;
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

public class ChannelSelectionDialog extends Dialog {
    private String mTitle;
    private String mMenuName = "";

    private ArrayList<ChannelBO> mChannelList;
    private ChannelSelectionListener channelSelectionListener;
    Context mcontext;


    public ChannelSelectionDialog(Context context, ArrayList<ChannelBO> ChannelList, String title) {
        super(context);
        this.mcontext = context;
        this.mChannelList = ChannelList;
        this.mTitle = title;
        requestWindowFeature(Window.FEATURE_NO_TITLE);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setCancelable(false);
        mMenuName = mcontext.getResources().getString(R.string.new_retailer);
        setContentView(R.layout.custom_dialog_fragment);

        TextView mTitleTV =  findViewById(R.id.title);
        Button mOkBtn = findViewById(R.id.btn_ok);
        mOkBtn.setVisibility(View.GONE);
        Button mDismisBtn =  findViewById(R.id.btn_dismiss);
        ListView mChannelLV =  findViewById(R.id.lv_colletion_print);

        mTitleTV.setText(mTitle);
        ArrayAdapter<ChannelBO> adapter = new ArrayAdapter<>(mcontext, android.R.layout.simple_list_item_single_choice, mChannelList);
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
                dismiss();
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

