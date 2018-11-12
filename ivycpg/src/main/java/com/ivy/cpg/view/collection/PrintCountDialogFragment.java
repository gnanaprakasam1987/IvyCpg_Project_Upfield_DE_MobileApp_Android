package com.ivy.cpg.view.collection;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.ivy.lib.DialogFragment;
import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.view.HomeScreenTwo;

import java.util.ArrayList;
import java.util.Arrays;

public class PrintCountDialogFragment extends DialogFragment {

    private PrintInterface printInterface;

    public interface PrintInterface {
        void print(int printCount);
        void dismiss();
    }

    private BusinessModel bmodel;
    private String mTitle = "";
    private String mTextViewTitle="";
    private int isFrom=0;


    private TextView mTitleTV;
    private Button mOkBtn, mDismisBtn;
    private ListView mCountLV;
    private boolean isClicked = false;

    private String[] mPrintCountArray;
    private View rootView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        bmodel = (BusinessModel) getActivity().getApplicationContext();
        bmodel.setContext(getActivity());

        mTitle = getArguments().getString("title");
        mTextViewTitle=getArguments().getString("textviewTitle");
        isFrom=getArguments().getInt("isfrom",0);

        try {
            printInterface = (PrintInterface) getActivity();
        } catch (ClassCastException e) {
            throw new ClassCastException("Calling Fragment must implement OnAddFriendListener");
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.custom_dialog_fragment, container, false);
        return rootView;
    }

    @Override
    public void onStart() {
        super.onStart();
        getDialog().setTitle(mTitle);
        getDialog().setCancelable(false);
        mTitleTV =  rootView.findViewById(R.id.title);
        mOkBtn =  rootView.findViewById(R.id.btn_ok);
        mDismisBtn =  rootView.findViewById(R.id.btn_dismiss);
        mCountLV =  rootView.findViewById(R.id.lv_colletion_print);
        mCountLV.setVisibility(View.GONE);
        mTitleTV.setText(mTextViewTitle);
        mPrintCountArray = bmodel.printHelper.getPrintCountArray();
        mOkBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isClicked) {
                    mTitleTV.setVisibility(View.GONE);
                    mCountLV.setVisibility(View.VISIBLE);
                    ArrayList<String> countList = new ArrayList<>(Arrays.asList(mPrintCountArray));

                    ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_single_choice, countList);
                    mCountLV.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                        @Override
                        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        printInterface.print(position);
                            dismiss();
                        }
                    });

                    mCountLV.setAdapter(adapter);
                    mOkBtn.setVisibility(View.GONE);
                    mDismisBtn.setVisibility(View.GONE);
                    isClicked = true;
                }
            }
        });
        mDismisBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(isFrom==0) {
                    Intent i = new Intent(getActivity(), HomeScreenTwo.class);
                    startActivity(i);
                    getActivity().finish();
                }else if(isFrom==1){
                    dismiss();
                    AdvancePaymentDialogFragment paymentDialogFragment =(AdvancePaymentDialogFragment) getActivity().getSupportFragmentManager().findFragmentByTag("Advance Payment");
                    if(paymentDialogFragment!=null) {
                        paymentDialogFragment.dismiss();
                    }
                }
            }
        });
    }
}
