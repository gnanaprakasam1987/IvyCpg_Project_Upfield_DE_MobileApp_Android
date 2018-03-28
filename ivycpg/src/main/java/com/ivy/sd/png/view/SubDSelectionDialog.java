package com.ivy.sd.png.view;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ListView;
import android.widget.TextView;

import com.ivy.lib.DialogFragment;
import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.bo.RetailerMasterBO;
import com.ivy.sd.png.bo.StandardListBO;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.util.Commons;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;

public class SubDSelectionDialog extends DialogFragment {

    private BusinessModel bmodel;
    View v;
    private ListView mCountLV;
    private TextView mTitleTV;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        if (getDialog() != null) {
            getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);
        }

        getDialog().setCancelable(false);
        this.setCancelable(false);

        v = inflater.inflate(R.layout.subdselect_dialog_fragment, container, false);

        bmodel = (BusinessModel) getActivity().getApplicationContext();
        bmodel.setContext(getActivity());


        mTitleTV =  v.findViewById(R.id.title);
        mCountLV =  v.findViewById(R.id.lvSubd);

        ArrayAdapter<RetailerMasterBO> adapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_single_choice, bmodel.getSubDMaster());
        mCountLV.setAdapter(adapter);
        mCountLV.setChoiceMode(ListView.CHOICE_MODE_SINGLE);

        return v;
    }


    public boolean isShowing() {
        if (getDialog() != null) {
            return true;
        }
        return false;
    }


    @Override
    public void onStart() {
        super.onStart();

        // safety check
        if (getDialog() == null) {
            return;
        }
        int dialogHeight = (int) getActivity().getResources().getDimension(R.dimen.dialog_height); // specify a value here

        getDialog().getWindow().setLayout(LayoutParams.MATCH_PARENT, dialogHeight);

    }



    private DialogInterface.OnDismissListener onDismissListener;

    public void setOnDismissListener(DialogInterface.OnDismissListener onDismissListener) {
        this.onDismissListener = onDismissListener;
    }

    @Override
    public void onDismiss(DialogInterface dialog) {
        super.onDismiss(dialog);
        if (onDismissListener != null) {
            onDismissListener.onDismiss(dialog);
        }
    }




}
