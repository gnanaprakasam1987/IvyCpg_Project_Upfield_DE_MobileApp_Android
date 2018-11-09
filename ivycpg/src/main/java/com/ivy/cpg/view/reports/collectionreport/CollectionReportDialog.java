package com.ivy.cpg.view.reports.collectionreport;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;

import com.ivy.lib.DialogFragment;
import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.bo.SpinnerBO;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.provider.ConfigurationMasterHelper;
import com.ivy.sd.print.CollectionPreviewScreen;

import java.util.ArrayList;

/**
 * Created by rajkumar.s on 10/26/2016.
 */

public class CollectionReportDialog extends DialogFragment {

    private View rootView;
    BusinessModel bmodel;
    ArrayList<SpinnerBO> lstRetailers;
    ArrayList<String> lstGroups;
    ArrayAdapter<SpinnerBO> mRetailerAdapter;
    ArrayAdapter<String> mGroupAdapter;
    Spinner spnRetailer,spnGroupId;
    String mSelectedGroupId;
    TextView tv_collection_label;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        bmodel = (BusinessModel) getActivity().getApplicationContext();
        bmodel.setContext(getActivity());


    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

       if (getDialog() != null) {
            getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);
        }

        rootView = inflater.inflate(R.layout.collection_report_dialog, container, false);
        return rootView;
    }

    @Override
    public void onStart() {
        super.onStart();
        //getDialog().setTitle(R.string.print);

        lstRetailers=new ArrayList<>();
        lstRetailers.add(0,new SpinnerBO(0,"ALL"));
        lstRetailers.addAll(bmodel.reportHelper.downloadCollectionReportRetailer());

        mRetailerAdapter = new ArrayAdapter<SpinnerBO>(getActivity(),
                R.layout.spinner_bluetext_layout,lstRetailers);
        mRetailerAdapter
                .setDropDownViewResource(R.layout.spinner_bluetext_list_item);


        tv_collection_label=(TextView)  rootView.findViewById(R.id.tv_collection_label);
        spnRetailer=(Spinner) rootView.findViewById(R.id.spn_retailer);
        spnRetailer.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {
                mSelectedGroupId="";
                if(lstRetailers.get(position).getId()!=0) {

                    tv_collection_label.setVisibility(View.VISIBLE);
                    spnGroupId.setVisibility(View.VISIBLE);

                    mGroupAdapter = new ArrayAdapter<String>(getActivity(),
                            R.layout.spinner_blacktext_layout, bmodel.reportHelper.downloadCollectionReportGroups(lstRetailers.get(position).getId()));
                    mGroupAdapter
                            .setDropDownViewResource(R.layout.spinner_bluetext_list_item);
                    spnGroupId.setAdapter(mGroupAdapter);
                }
                else{
                    tv_collection_label.setVisibility(View.GONE);
                    spnGroupId.setVisibility(View.GONE);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        spnRetailer.setAdapter(mRetailerAdapter);

        spnGroupId=(Spinner) rootView.findViewById(R.id.spn_groupid);
        spnGroupId.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                mSelectedGroupId=bmodel.reportHelper.getLstCollectionGroups().get(i);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });


        Button mOkBtn=(Button)rootView.findViewById(R.id.btn_ok);
        mOkBtn.setTypeface(bmodel.configurationMasterHelper.getFontBaloobhai(ConfigurationMasterHelper.FontType.REGULAR));
        Button mCancelBtn=(Button)rootView.findViewById(R.id.btn_cancel);
        mCancelBtn.setTypeface(bmodel.configurationMasterHelper.getFontBaloobhai(ConfigurationMasterHelper.FontType.REGULAR));
        mCancelBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getDialog().dismiss();

            }
        });

        mOkBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                getDialog().dismiss();
                Intent i = new Intent(getActivity(),
						CollectionPreviewScreen.class);
				i.putExtra("Retailer",((SpinnerBO)spnRetailer.getSelectedItem()).getSpinnerTxt());
                i.putExtra("GroupId",mSelectedGroupId);
				startActivity(i);
            }
        });
    }
}
