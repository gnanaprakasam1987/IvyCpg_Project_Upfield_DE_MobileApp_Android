package com.ivy.cpg.view.sf;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
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
    private EditText QUANTITY;
    private InputMethodManager inputManager;
    private String append = "";
    SalesFundamentalHelper mSFHelper;

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
        lbl_prod_name.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
        lbl_avail = (TextView) view.findViewById(R.id.tv_availability);
        lbl_avail.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
        inputManager = (InputMethodManager) getActivity().getSystemService(
                Context.INPUT_METHOD_SERVICE);
        isFromChild = getActivity().getIntent().getBooleanExtra("isFromChild", false);
        createView();

        return view;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mSFHelper = SalesFundamentalHelper.getInstance(getActivity());

    }

    @Override
    public void onStart() {
        super.onStart();

        final ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayShowTitleEnabled(false);
            setScreenTitle(
                    mSFHelper.mSelectedActivityName);
        }

        setHasOptionsMenu(true);


    }

    private void createView() {

        try {
            if (mSFHelper.getLstSOSproj() == null)
                lstSOS = mSFHelper.downloadSOSgroups();
            else
                lstSOS = mSFHelper.getLstSOSproj();


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
                tv_prod_name.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
                tv_prod_name.setText(bo.getProductName());

                final EditText edt_availability = (EditText) productView.findViewById(R.id.edt_availability);
                edt_availability.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
                edt_availability.setTag(bo.getGroupId() + "" + bo.getProductID());
                edt_availability.setText(bo.getAvailability() + "");

                edt_availability.setOnTouchListener(new View.OnTouchListener() {

                    @Override
                    public boolean onTouch(View v, MotionEvent event) {


                        QUANTITY = edt_availability;
                        edt_availability.onTouchEvent(event);
                        edt_availability.selectAll();
                        edt_availability.requestFocus();
                        inputManager.hideSoftInputFromWindow(
                                edt_availability
                                        .getWindowToken(), 0);
                        return true;
                    }
                });


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

            if (mSFHelper.getLstSOSproj() != null)
                mSFHelper.setLstSOSproj(null);

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

    public void numberPressed(View vw) {
        if (QUANTITY == null) {
            bmodel.showAlert(
                    getResources().getString(R.string.please_select_item), 0);
        } else {
            int id = vw.getId();
            if (id == R.id.calcdel) {
                String s = QUANTITY.getText().toString();
                if (!(s.length() == 0)) {
                    s = s.substring(0, s.length() - 1);
                    if (s.length() == 0) {
                        s = "";
                    }
                }
                QUANTITY.setText(s);
            } else {
                if (getView() != null) {
                    Button ed = (Button) getView().findViewById(vw.getId());
                    append = ed.getText().toString();
                }
                eff();
            }
        }
    }

    private void eff() {
        String s = QUANTITY.getText().toString();
        if (!"0".equals(s) && !"0.0".equals(s)) {
            String strQuantity = QUANTITY.getText() + append;
            QUANTITY.setText(strQuantity);
        } else
            QUANTITY.setText(append);
    }

}
