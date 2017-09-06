package com.ivyretail.views;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.bo.SOSBO;
import com.ivy.sd.png.commons.IvyBaseFragment;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.provider.ConfigurationMasterHelper;
import com.ivy.sd.png.util.Commons;
import com.ivy.sd.png.view.HomeScreenTwo;

import java.util.ArrayList;

/**
 * Created by rajkumar.s on 4/20/2017.
 */

public class SOSFragment_Proj extends IvyBaseFragment implements View.OnClickListener {

    ArrayList<SOSBO> lstSOS;
    BusinessModel bmodel;
    LinearLayout ll_content;
    Button btnSave;
    TextView lbl_prod_name, lbl_avail;
    private boolean isFromChild;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {


        bmodel = (BusinessModel) getActivity().getApplicationContext();
        bmodel.setContext(getActivity());

        View view = inflater.inflate(R.layout.fragment_sos_proj, container, false);

        ll_content = (LinearLayout) view.findViewById(R.id.ll_content);
        btnSave = (Button) view.findViewById(R.id.btn_save);
        btnSave.setOnClickListener(this);

        lbl_prod_name = (TextView) view.findViewById(R.id.tv_prod_lbl);
        lbl_prod_name.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.REGULAR));
        lbl_avail = (TextView) view.findViewById(R.id.tv_availability);
        lbl_avail.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.REGULAR));

        isFromChild = getActivity().getIntent().getBooleanExtra("isFromChild", false);
        createView();

        return view;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public void onStart() {
        super.onStart();

        final ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayShowTitleEnabled(false);
            setScreenTitle(
                    bmodel.mSelectedActivityName);
        }

        setHasOptionsMenu(true);


    }

    private void createView() {

        try {
            if (bmodel.salesFundamentalHelper.getLstSOSproj() == null)
                lstSOS = bmodel.salesFundamentalHelper.downloadSOSgroups();
            else
                lstSOS = bmodel.salesFundamentalHelper.getLstSOSproj();


            LayoutInflater inflater = LayoutInflater.from(getActivity());

            int lastGroupId = 0;
            View cardView = null, productView;
            LinearLayout ll_product_layout = null;
            for (SOSBO bo : lstSOS) {

                if (lastGroupId != bo.getGroupId()) {

                    cardView = inflater.inflate(R.layout.layout_sos_group_item, null);
                    TextView tv_groupName = (TextView) cardView.findViewById(R.id.tv_groupName);
                    tv_groupName.setText(bo.getGroupName());
                    ll_content.addView(cardView);

                }

                ll_product_layout = (LinearLayout) cardView.findViewById(R.id.ll_products);

                productView = inflater.inflate(R.layout.layout_sos_group_list_item, null);

                TextView tv_prod_name = (TextView) productView.findViewById(R.id.tv_productname);
                tv_prod_name.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.BOLD));
                tv_prod_name.setText(bo.getProductName());

                EditText edt_availability = (EditText) productView.findViewById(R.id.edt_availability);
                edt_availability.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.REGULAR));
                edt_availability.setTag(bo.getGroupId() + "" + bo.getProductID());
                edt_availability.setText(bo.getAvailability() + "");


                ll_product_layout.addView(productView);

                lastGroupId = bo.getGroupId();
            }
        } catch (Exception ex) {
            Commons.printException(ex);
        }
    }

    @Override
    public void onClick(View view) {
        try {
            if (view.getId() == R.id.btn_save) {
                setValues();

                if (checkIsDataAvailable()) {
                    Intent intent = new Intent(getActivity(), SOS_Summary_Proj.class);
                    startActivity(intent);
                    getActivity().finish();
                } else {
                    Toast.makeText(getActivity(), getResources().getString(R.string.no_data_tosave), Toast.LENGTH_LONG).show();
                }
            }
        } catch (Exception ex) {
            Commons.printException(ex);
        }
    }

    private void setValues() {
        for (SOSBO bo : lstSOS) {

            View view = getView().findViewWithTag(bo.getGroupId() + "" + bo.getProductID());
            if (view != null) {
                EditText edt = (EditText) view;
                if (!edt.getText().toString().equals(""))
                    bo.setAvailability(Integer.parseInt(edt.getText().toString()));
                else
                    bo.setAvailability(0);
            }
        }
    }

    private boolean checkIsDataAvailable() {
        for (SOSBO bo : lstSOS) {
            if (bo.getAvailability() > 0) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int i = item.getItemId();
        if (i == android.R.id.home) {

            if (bmodel.salesFundamentalHelper.getLstSOSproj() != null)
                bmodel.salesFundamentalHelper.setLstSOSproj(null);

            if (isFromChild)
                startActivity(new Intent(getActivity(), HomeScreenTwo.class)
                        .putExtra("isStoreMenu", true));
            else
                startActivity(new Intent(getActivity(), HomeScreenTwo.class));
            getActivity().finish();
            getActivity().overridePendingTransition(R.anim.trans_right_in, R.anim.trans_right_out);
            return true;
        }


        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

    }


}
